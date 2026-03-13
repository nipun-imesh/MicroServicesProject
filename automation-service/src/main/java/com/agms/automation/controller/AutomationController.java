package com.agms.automation.controller;

import com.agms.automation.client.ZoneClient;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/automation")
public class AutomationController {

    private final ZoneClient zoneClient;
    private final Map<String, Object> state = new ConcurrentHashMap<>();

    public AutomationController(ZoneClient zoneClient) {
        this.zoneClient = zoneClient;
    }

    @PostMapping("/trigger/{zoneId}")
    public Map<String, Object> triggerAction(@PathVariable String zoneId, @RequestBody Map<String, Object> payload) {
        Map<String, Object> zoneInfo = zoneClient.getZoneStatus(zoneId);
        state.put(zoneId, payload);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Action triggered successfully!");
        response.put("zoneData", zoneInfo);
        response.put("action", payload);
        return response;
    }

    @GetMapping("/state")
    public Map<String, Object> getState() {
        return state;
    }
}
