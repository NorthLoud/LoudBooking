package com.example.loudhotel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private Double totalIncome;
    private Long totalCompleted;
    private Long totalCanceled;
    private Long totalReview;

    private List<Double> monthlyIncome;
    private List<Long> monthlyBooking;
    private List<Long> monthlyCanceled;
    private List<Double> monthlyRating;
    private List<String> chartLabels;
    private List<String> topRoomNames;
    private List<Long> topRoomBookingCounts;
}