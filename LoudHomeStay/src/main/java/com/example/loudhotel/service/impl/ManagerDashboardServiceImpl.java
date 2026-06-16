package com.example.loudhotel.service.impl;

import com.example.loudhotel.dto.response.DashboardDTO;
import com.example.loudhotel.entity.Bill;
import com.example.loudhotel.entity.Review;
import com.example.loudhotel.entity.BillDetail;
import com.example.loudhotel.repository.BillRepository;
import com.example.loudhotel.repository.ReviewRepository;
import com.example.loudhotel.service.ManagerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerDashboardServiceImpl implements ManagerDashboardService {

    private final BillRepository billRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public DashboardDTO getOverview(Long managerId) {
        List<Bill> bills = billRepository.findByHotel_Manager_UserId(managerId);
        List<Review> reviews = reviewRepository.findAllByManagerId(managerId);

        double totalIncome = bills.stream()
                .filter(b -> b.getBillStatus() == Bill.BillStatus.PAID)
                .mapToDouble(b -> b.getTotalCost() != null ? b.getTotalCost() : 0.0)
                .sum();
                
        long totalCompleted = bills.stream()
                .filter(b -> b.getBillStatus() == Bill.BillStatus.PAID)
                .count();
                
        long totalCanceled = bills.stream()
                .filter(b -> b.getBillStatus() == Bill.BillStatus.CANCELED)
                .count();
                
        long totalReview = reviews.size();

        DashboardDTO dto = new DashboardDTO();
        dto.setTotalIncome(totalIncome);
        dto.setTotalCompleted(totalCompleted);
        dto.setTotalCanceled(totalCanceled);
        dto.setTotalReview(totalReview);
        return dto;
    }

    @Override
    public DashboardDTO getChartYear(Long managerId) {
        List<Bill> bills = billRepository.findByHotel_Manager_UserId(managerId);
        List<Review> reviews = reviewRepository.findAllByManagerId(managerId);

        int currentYear = LocalDate.now().getYear();

        List<Bill> yearBills = bills.stream()
                .filter(b -> b.getCreatedAt() != null && b.getCreatedAt().getYear() == currentYear)
                .collect(Collectors.toList());
                
        List<Review> yearReviews = reviews.stream()
                .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().getYear() == currentYear)
                .collect(Collectors.toList());

        return buildChartData(yearBills, yearReviews);
    }

    @Override
    public DashboardDTO getChartByRange(Long managerId, LocalDate startDate, LocalDate endDate) {
        List<Bill> bills = billRepository.findByHotel_Manager_UserId(managerId);
        List<Review> reviews = reviewRepository.findAllByManagerId(managerId);
        
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Bill> rangeBills = bills.stream()
                .filter(b -> b.getCreatedAt() != null && !b.getCreatedAt().isBefore(start) && !b.getCreatedAt().isAfter(end))
                .collect(Collectors.toList());
                
        List<Review> rangeReviews = reviews.stream()
                .filter(r -> r.getCreatedAt() != null && !r.getCreatedAt().isBefore(start) && !r.getCreatedAt().isAfter(end))
                .collect(Collectors.toList());

        DashboardDTO dto = buildChartDataRange(rangeBills, rangeReviews, startDate, endDate);
        
        dto.setTotalIncome(rangeBills.stream().filter(b -> b.getBillStatus() == Bill.BillStatus.PAID).mapToDouble(b -> b.getTotalCost() != null ? b.getTotalCost() : 0.0).sum());
        dto.setTotalCompleted(rangeBills.stream().filter(b -> b.getBillStatus() == Bill.BillStatus.PAID).count());
        dto.setTotalCanceled(rangeBills.stream().filter(b -> b.getBillStatus() == Bill.BillStatus.CANCELED).count());
        dto.setTotalReview((long) rangeReviews.size());
        
        return dto;
    }

    private DashboardDTO buildChartData(List<Bill> bills, List<Review> reviews) {
        List<Double> monthlyIncome = new ArrayList<>(Collections.nCopies(12, 0.0));
        List<Long> monthlyBooking = new ArrayList<>(Collections.nCopies(12, 0L));
        List<Long> monthlyCanceled = new ArrayList<>(Collections.nCopies(12, 0L));
        List<Double> monthlyRating = new ArrayList<>(Collections.nCopies(12, 0.0));
        
        Map<Integer, Double> sumRatingMap = new HashMap<>();
        Map<Integer, Integer> countRatingMap = new HashMap<>();

        for (Bill b : bills) {
            if (b.getCreatedAt() == null) continue;
            int month = b.getCreatedAt().getMonthValue() - 1; // 0-indexed
            
            monthlyBooking.set(month, monthlyBooking.get(month) + 1);
            
            if (b.getBillStatus() == Bill.BillStatus.PAID) {
                double cost = b.getTotalCost() != null ? b.getTotalCost() : 0.0;
                monthlyIncome.set(month, monthlyIncome.get(month) + cost);
            }
            if (b.getBillStatus() == Bill.BillStatus.CANCELED) {
                monthlyCanceled.set(month, monthlyCanceled.get(month) + 1);
            }
        }

        for (Review r : reviews) {
            if (r.getCreatedAt() == null || r.getRate() == null) continue;
            int month = r.getCreatedAt().getMonthValue() - 1;
            sumRatingMap.put(month, sumRatingMap.getOrDefault(month, 0.0) + r.getRate());
            countRatingMap.put(month, countRatingMap.getOrDefault(month, 0) + 1);
        }

        for (int i = 0; i < 12; i++) {
            if (countRatingMap.containsKey(i)) {
                monthlyRating.set(i, sumRatingMap.get(i) / countRatingMap.get(i));
            }
        }

        Map<String, Long> roomTypeCounts = new HashMap<>();
        for (Bill b : bills) {
            if (b.getBillStatus() == Bill.BillStatus.CANCELED) continue;
            if (b.getBillDetails() != null) {
                for (BillDetail bd : b.getBillDetails()) {
                    if (bd.getRoomType() != null && bd.getRoomType().getTypeName() != null) {
                        String name = bd.getRoomType().getTypeName();
                        roomTypeCounts.put(name, roomTypeCounts.getOrDefault(name, 0L) + 1);
                    }
                }
            }
        }

        List<Map.Entry<String, Long>> sortedRooms = roomTypeCounts.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .collect(Collectors.toList());

        List<String> topRoomNames = new ArrayList<>();
        List<Long> topRoomBookingCounts = new ArrayList<>();
        for (Map.Entry<String, Long> entry : sortedRooms) {
            topRoomNames.add(entry.getKey());
            topRoomBookingCounts.add(entry.getValue());
        }

        DashboardDTO dto = new DashboardDTO();
        dto.setMonthlyIncome(monthlyIncome);
        dto.setMonthlyBooking(monthlyBooking);
        dto.setMonthlyCanceled(monthlyCanceled);
        dto.setMonthlyRating(monthlyRating);
        dto.setChartLabels(Arrays.asList("T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"));
        dto.setTopRoomNames(topRoomNames);
        dto.setTopRoomBookingCounts(topRoomBookingCounts);

        return dto;
    }

    private DashboardDTO buildChartDataRange(List<Bill> bills, List<Review> reviews, LocalDate startDate, LocalDate endDate) {
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        
        List<String> chartLabels = new ArrayList<>();
        List<Double> monthlyIncome;
        List<Long> monthlyBooking;
        List<Long> monthlyCanceled;
        List<Double> monthlyRating;
        
        Map<Integer, Double> sumRatingMap = new HashMap<>();
        Map<Integer, Integer> countRatingMap = new HashMap<>();

        if (daysBetween <= 31) {
            // Gom nhóm theo ngày
            Map<LocalDate, Integer> dateToIndex = new HashMap<>();
            LocalDate current = startDate;
            int index = 0;
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM");
            while (!current.isAfter(endDate)) {
                chartLabels.add(current.format(formatter));
                dateToIndex.put(current, index);
                current = current.plusDays(1);
                index++;
            }
            
            monthlyIncome = new ArrayList<>(Collections.nCopies(index, 0.0));
            monthlyBooking = new ArrayList<>(Collections.nCopies(index, 0L));
            monthlyCanceled = new ArrayList<>(Collections.nCopies(index, 0L));
            monthlyRating = new ArrayList<>(Collections.nCopies(index, 0.0));
            
            for (Bill b : bills) {
                if (b.getCreatedAt() == null) continue;
                LocalDate billDate = b.getCreatedAt().toLocalDate();
                if (dateToIndex.containsKey(billDate)) {
                    int idx = dateToIndex.get(billDate);
                    monthlyBooking.set(idx, monthlyBooking.get(idx) + 1);
                    if (b.getBillStatus() == Bill.BillStatus.PAID) {
                        double cost = b.getTotalCost() != null ? b.getTotalCost() : 0.0;
                        monthlyIncome.set(idx, monthlyIncome.get(idx) + cost);
                    }
                    if (b.getBillStatus() == Bill.BillStatus.CANCELED) {
                        monthlyCanceled.set(idx, monthlyCanceled.get(idx) + 1);
                    }
                }
            }
            
            for (Review r : reviews) {
                if (r.getCreatedAt() == null || r.getRate() == null) continue;
                LocalDate reviewDate = r.getCreatedAt().toLocalDate();
                if (dateToIndex.containsKey(reviewDate)) {
                    int idx = dateToIndex.get(reviewDate);
                    sumRatingMap.put(idx, sumRatingMap.getOrDefault(idx, 0.0) + r.getRate());
                    countRatingMap.put(idx, countRatingMap.getOrDefault(idx, 0) + 1);
                }
            }
            
            for (int i = 0; i < index; i++) {
                if (countRatingMap.containsKey(i)) {
                    monthlyRating.set(i, sumRatingMap.get(i) / countRatingMap.get(i));
                }
            }
            
        } else {
            // Gom nhóm theo tháng
            java.time.YearMonth startYM = java.time.YearMonth.from(startDate);
            java.time.YearMonth endYM = java.time.YearMonth.from(endDate);
            Map<java.time.YearMonth, Integer> ymToIndex = new HashMap<>();
            java.time.YearMonth currentYM = startYM;
            int index = 0;
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MM/yyyy");
            while (!currentYM.isAfter(endYM)) {
                chartLabels.add(currentYM.format(formatter));
                ymToIndex.put(currentYM, index);
                currentYM = currentYM.plusMonths(1);
                index++;
            }
            
            monthlyIncome = new ArrayList<>(Collections.nCopies(index, 0.0));
            monthlyBooking = new ArrayList<>(Collections.nCopies(index, 0L));
            monthlyCanceled = new ArrayList<>(Collections.nCopies(index, 0L));
            monthlyRating = new ArrayList<>(Collections.nCopies(index, 0.0));
            
            for (Bill b : bills) {
                if (b.getCreatedAt() == null) continue;
                java.time.YearMonth billYM = java.time.YearMonth.from(b.getCreatedAt().toLocalDate());
                if (ymToIndex.containsKey(billYM)) {
                    int idx = ymToIndex.get(billYM);
                    monthlyBooking.set(idx, monthlyBooking.get(idx) + 1);
                    if (b.getBillStatus() == Bill.BillStatus.PAID) {
                        double cost = b.getTotalCost() != null ? b.getTotalCost() : 0.0;
                        monthlyIncome.set(idx, monthlyIncome.get(idx) + cost);
                    }
                    if (b.getBillStatus() == Bill.BillStatus.CANCELED) {
                        monthlyCanceled.set(idx, monthlyCanceled.get(idx) + 1);
                    }
                }
            }
            
            for (Review r : reviews) {
                if (r.getCreatedAt() == null || r.getRate() == null) continue;
                java.time.YearMonth reviewYM = java.time.YearMonth.from(r.getCreatedAt().toLocalDate());
                if (ymToIndex.containsKey(reviewYM)) {
                    int idx = ymToIndex.get(reviewYM);
                    sumRatingMap.put(idx, sumRatingMap.getOrDefault(idx, 0.0) + r.getRate());
                    countRatingMap.put(idx, countRatingMap.getOrDefault(idx, 0) + 1);
                }
            }
            
            for (int i = 0; i < index; i++) {
                if (countRatingMap.containsKey(i)) {
                    monthlyRating.set(i, sumRatingMap.get(i) / countRatingMap.get(i));
                }
            }
        }
        
        // Tính top room types
        Map<String, Long> roomTypeCounts = new HashMap<>();
        for (Bill b : bills) {
            if (b.getBillStatus() == Bill.BillStatus.CANCELED) continue;
            if (b.getBillDetails() != null) {
                for (BillDetail bd : b.getBillDetails()) {
                    if (bd.getRoomType() != null && bd.getRoomType().getTypeName() != null) {
                        String name = bd.getRoomType().getTypeName();
                        roomTypeCounts.put(name, roomTypeCounts.getOrDefault(name, 0L) + 1);
                    }
                }
            }
        }

        List<Map.Entry<String, Long>> sortedRooms = roomTypeCounts.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .collect(Collectors.toList());

        List<String> topRoomNames = new ArrayList<>();
        List<Long> topRoomBookingCounts = new ArrayList<>();
        for (Map.Entry<String, Long> entry : sortedRooms) {
            topRoomNames.add(entry.getKey());
            topRoomBookingCounts.add(entry.getValue());
        }
        
        DashboardDTO dto = new DashboardDTO();
        dto.setMonthlyIncome(monthlyIncome);
        dto.setMonthlyBooking(monthlyBooking);
        dto.setMonthlyCanceled(monthlyCanceled);
        dto.setMonthlyRating(monthlyRating);
        dto.setChartLabels(chartLabels);
        dto.setTopRoomNames(topRoomNames);
        dto.setTopRoomBookingCounts(topRoomBookingCounts);
        
        return dto;
    }
}