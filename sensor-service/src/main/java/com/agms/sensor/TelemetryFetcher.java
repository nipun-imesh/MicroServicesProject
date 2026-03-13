package com.agms.sensor;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TelemetryFetcher - Scheduled task that:
 * 1. Authenticates with the External IoT API every 10 seconds
 * 2. Fetches telemetry for all registered devices
 * 3. Pushes telemetry data to the Automation Service for rule processing
 */
@Component
public class TelemetryFetcher {

    private static final Logger log = LoggerFactory.getLogger(TelemetryFetcher.class);

    private final WebClient extClient;
    private WebClient automationClient;

    private final AtomicReference<Map<String, Object>> last = new AtomicReference<>();

    @Value("${external.iot.username:agms_user}")
    private String username;

    @Value("${external.iot.password:123456}")
    private String password;

    @Value("${automation.service.url:http://localhost:8083}")
    private String automationServiceUrl;

    public TelemetryFetcher(WebClient.Builder builder,
                            @Value("${external.iot.base-url}") String externalBase) {
        this.extClient = builder.baseUrl(externalBase).build();
    }

    @PostConstruct
    public void init() {
        this.automationClient = WebClient.create(automationServiceUrl);
        log.info("TelemetryFetcher initialized. Automation endpoint: {}/api/automation/process", automationServiceUrl);
    }

    /**
     * Runs every 10 seconds: login to external IoT, fetch all device telemetry,
     * and push each reading to the Automation Service.
     */
    @Scheduled(fixedDelay = 10000)
    public void fetchAndPush() {
        log.debug("Scheduled telemetry fetch triggered...");

        // Step 1: Login to External IoT API
        String token = extClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("username", username, "password", password))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(resp -> (String) resp.get("accessToken"))
                .onErrorResume(e -> {
                    log.error("External IoT login failed: {}", e.getMessage());
                    return Mono.empty();
                })
                .block();

        if (token == null || token.isBlank()) {
            log.warn("No access token received from external IoT. Skipping this cycle.");
            return;
        }

        // Step 2: Fetch all registered IoT devices
        List<Map<String, Object>> devices = extClient.get()
                .uri("/devices")
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .onErrorResume(e -> {
                    log.error("Failed to fetch IoT devices: {}", e.getMessage());
                    return Mono.empty();
                })
                .block();

        if (devices == null || devices.isEmpty()) {
            log.info("No IoT devices found. Create a zone first to register a device.");
            return;
        }

        log.debug("Found {} IoT device(s). Fetching telemetry...", devices.size());

        // Step 3: For each device, get telemetry and push to Automation Service
        for (Map<String, Object> device : devices) {
            String deviceId = (String) device.get("deviceId");
            if (deviceId == null) continue;

            Map<String, Object> telemetry = extClient.get()
                    .uri("/devices/telemetry/{id}", deviceId)
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .onErrorResume(e -> {
                        log.error("Failed to fetch telemetry for device {}: {}", deviceId, e.getMessage());
                        return Mono.empty();
                    })
                    .block();

            if (telemetry == null) continue;

            // Cache the last reading for the debug endpoint
            last.set(telemetry);
            log.info("Telemetry received for device {}: {}", deviceId, telemetry.get("value"));

            // Push to Automation Service for rule evaluation
            automationClient.post()
                    .uri("/api/automation/process")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(telemetry)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .onErrorResume(e -> {
                        log.error("Failed to push telemetry to Automation Service: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .block();
        }
    }

    public Map<String, Object> getLast() {
        return last.get();
    }
}
