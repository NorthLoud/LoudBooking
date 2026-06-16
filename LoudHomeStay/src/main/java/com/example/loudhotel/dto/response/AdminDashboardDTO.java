package com.example.loudhotel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {
    // Overview
    private Double totalIncomeHotel;
    private Long totalCompleted;
    private Long totalCanceled;
    private Long totalUtil;
    private Long activeUser;
    private Long totalUser;
    private Long activeHotel;
    private Long totalHotel;
    private Long activeRoom;
    private Long totalRoom;

    // Range specific
    private Double incomeHotel;
    private Long completed;
    private Long canceled;
    private Long review;

    // Chart
    private List<String> chartLabels;
    private List<Double> adminIncome;
    private List<Double> hotelIncome;
    private List<Long> ordersCompleted;
    private List<Long> ordersCanceled;
    private List<Long> users;
    private List<Long> managers;

    // Top analysis
    private List<String> topHotelNames;
    private List<Double> topHotelRevenues;
    private List<String> topRoomTypeNames;
    private List<Long> topRoomTypeBookings;
}
