package com.example.loudhotel.service;

import com.example.loudhotel.dto.response.DashboardDTO;

import java.util.List;
import java.util.Map;

import java.time.LocalDate;

public interface ManagerDashboardService {
    DashboardDTO getOverview(Long managerId);
    DashboardDTO getChartYear(Long managerId);
    DashboardDTO getChartByRange(Long managerId, LocalDate startDate, LocalDate endDate);
}