package com.supplychainmultiwarehousetracker.controller;

import com.supplychainmultiwarehousetracker.dto.ForecastingMetricDto;
import com.supplychainmultiwarehousetracker.service.DemandForecastingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final DemandForecastingService forecastingService;

    @GetMapping("/forecasting")
    public ResponseEntity<List<ForecastingMetricDto>> getForecastingMetrics(@RequestParam(required = false, defaultValue = "30") Integer days) {
        return ResponseEntity.ok(forecastingService.getDemandForecastingMetrics(days));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getAnalyticsSummary() {
        return ResponseEntity.ok(forecastingService.getAnalyticsSummary());
    }
}
