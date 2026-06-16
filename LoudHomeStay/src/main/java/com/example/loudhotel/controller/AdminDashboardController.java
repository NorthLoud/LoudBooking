package com.example.loudhotel.controller;

import com.example.loudhotel.dto.response.AdminDashboardDTO;
import com.example.loudhotel.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/overview")
    public ResponseEntity<AdminDashboardDTO> getOverview() {
        return ResponseEntity.ok(adminDashboardService.getOverview());
    }

    @GetMapping("/range")
    public ResponseEntity<AdminDashboardDTO> getChartByRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "hotelId", required = false) Long hotelId) {
        return ResponseEntity.ok(adminDashboardService.getChartByRange(startDate, endDate, hotelId));
    }

    @GetMapping("/chart-year")
    public ResponseEntity<AdminDashboardDTO> getChartYear(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "hotelId", required = false) Long hotelId) {
        return ResponseEntity.ok(adminDashboardService.getChartYear(year, hotelId));
    }
}
