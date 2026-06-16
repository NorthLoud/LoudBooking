package com.example.loudhotel.controller;

import com.example.loudhotel.dto.response.DashboardDTO;
import com.example.loudhotel.service.ManagerDashboardService;
import com.example.loudhotel.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/manager/dashboard")
@RequiredArgsConstructor
public class ManagerDashboardController {

    private final ManagerDashboardService dashboardService;

    @GetMapping("/overview")
    public ResponseEntity<DashboardDTO> getOverview() {
        Long managerId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(dashboardService.getOverview(managerId));
    }

    @GetMapping("/chart-year")
    public ResponseEntity<DashboardDTO> getChartYear() {
        Long managerId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(dashboardService.getChartYear(managerId));
    }

    @GetMapping("/range")
    public ResponseEntity<DashboardDTO> getChartByRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long managerId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(dashboardService.getChartByRange(managerId, startDate, endDate));
    }
}