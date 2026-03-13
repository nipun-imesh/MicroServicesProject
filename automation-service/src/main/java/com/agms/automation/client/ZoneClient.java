package com.agms.automation.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

@FeignClient(name = "zone-service")
public interface ZoneClient {
    @GetMapping("/api/zones/{id}")
    Map<String, Object> getZoneStatus(@PathVariable("id") String id);
}
