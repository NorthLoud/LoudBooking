package com.example.loudhotel.service;

import com.example.loudhotel.dto.response.AdminDashboardDTO;
import java.time.LocalDate;

public interface AdminDashboardService {
    AdminDashboardDTO getOverview();
    AdminDashboardDTO getChartByRange(LocalDate startDate, LocalDate endDate, Long hotelId);
    AdminDashboardDTO getChartYear(Integer year, Long hotelId);
}