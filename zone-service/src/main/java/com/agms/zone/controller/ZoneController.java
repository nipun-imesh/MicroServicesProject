package com.agms.zone.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/zones")
public class ZoneController {


    private final WebClient webClient;
    private final Map<String, Map<String, Object>> store = new ConcurrentHashMap<>();

    @Value("${external.iot.token:}")
    private String externalToken;

    public ZoneController(WebClient.Builder webClientBuilder, @Value("${external.iot.base-url}") String externalBase) {
        this.webClient = webClientBuilder.baseUrl(externalBase).build();
    }

    @PostMapping
    public Mono<Map<String, Object>> createZone(@RequestBody Map<String, Object> payload) {
        Double minTemp = ((Number) payload.get("minTemp")).doubleValue();
        Double maxTemp = ((Number) payload.get("maxTemp")).doubleValue();

        if (minTemp >= maxTemp) {
            return Mono.error(new IllegalArgumentException("minTemp must be strictly less than maxTemp"));
        }

        String zoneId = UUID.randomUUID().toString();
        payload.put("zoneId", zoneId);

        // Register device in external IoT
        Map<String, Object> devicePayload = Map.of(
            "name", "Device-" + zoneId,
            "zoneId", zoneId
        );

        return webClient.post()
            .uri("/devices")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(h -> h.setBearerAuth(externalToken))
            .bodyValue(devicePayload)
            .retrieve()
            .bodyToMono(Map.class)
            .map(deviceResp -> {
                payload.put("deviceId", deviceResp.get("deviceId"));
                store.put(zoneId, payload);
                return payload;
            });
    }

    @GetMapping("/{id}")
    public Map<String, Object> getZone(@PathVariable String id) {
        return store.get(id);
    }

    @DeleteMapping("/{id}")
    public void deleteZone(@PathVariable String id) {
        store.remove(id);
    }
}
