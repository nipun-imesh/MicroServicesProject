package com.agms.sensor.controller;

import com.agms.sensor.TelemetryFetcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sensors")
public class SensorController {

    private final TelemetryFetcher fetcher;

    public SensorController(TelemetryFetcher fetcher) {
        this.fetcher = fetcher;
    }

    @GetMapping("/latest")
    public Object getLatestReading() {
        return fetcher.getLast();
    }
}

