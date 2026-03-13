package com.agms.crop;

import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/crops")
public class CropController {

    private final Map<String, Map<String, Object>> inventory = new ConcurrentHashMap<>();

    @PostMapping
    public Map<String, Object> registerBatch(@RequestBody Map<String, Object> crop) {
        String id = UUID.randomUUID().toString();
        crop.put("id", id);
        crop.put("status", "SEEDLING");
        inventory.put(id, crop);
        return crop;
    }

    @PutMapping("/{id}/status")
    public Map<String, Object> updateStatus(@PathVariable String id, @RequestBody Map<String, String> statusBody) {
        Map<String, Object> crop = inventory.get(id);
        if (crop != null && statusBody.containsKey("status")) {
            crop.put("status", statusBody.get("status"));
        }
        return crop;
    }

    @GetMapping
    public Collection<Map<String, Object>> getInventory() {
        return inventory.values();
    }
}
