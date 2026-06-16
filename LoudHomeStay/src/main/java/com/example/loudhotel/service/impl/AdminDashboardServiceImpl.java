package com.example.loudhotel.service.impl;

import com.example.loudhotel.dto.response.AdminDashboardDTO;
import com.example.loudhotel.entity.Bill;
import com.example.loudhotel.entity.BillDetail;
import com.example.loudhotel.entity.Hotel;
import com.example.loudhotel.entity.Room;
import com.example.loudhotel.entity.User;
import com.example.loudhotel.repository.*;
import com.example.loudhotel.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final BillRepository billRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final UtilitiesRepository utilitiesRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public AdminDashboardDTO getOverview() {
        AdminDashboardDTO dto = new AdminDashboardDTO();

        List<Bill> bills = billRepository.findAll();
        
        dto.setTotalIncomeHotel(bills.stream()
                .filter(b -> b.getBillStatus() == Bill.BillStatus.PAID)
                .mapToDouble(b -> b.getTotalCost() != null ? b.getTotalCost() : 0.0)
                .sum());
        dto.setTotalCompleted(bills.stream().filter(b -> b.getBillStatus() == Bill.BillStatus.PAID).count());
        dto.setTotalCanceled(bills.stream().filter(b -> b.getBillStatus() == Bill.BillStatus.CANCELED).count());
        
        dto.setTotalUtil(utilitiesRepository.count());

        List<User> users = userRepository.findAll();

        long totalUser = users.stream()
                .filter(u -> u.getIsDeleted() == null || !u.getIsDeleted())
                .count();

        long activeUser = users.stream()
                .filter(u -> (u.getIsDeleted() == null || !u.getIsDeleted())
                        && u.getStatus() == User.Status.ACTIVE)
                .count();

        dto.setTotalUser(totalUser);
        dto.setActiveUser(activeUser);

        List<Hotel> hotels = hotelRepository.findAll();

        long totalHotel = hotels.stream()
                .filter(h -> h.getIsDeleted() == null || !h.getIsDeleted())
                .count();

        long activeHotel = hotels.stream()
                .filter(h -> (h.getIsDeleted() == null || !h.getIsDeleted())
                        && h.getHotelStatus() == Hotel.HotelStatus.ACTIVE)
                .count();

        dto.setTotalHotel(totalHotel);
        dto.setActiveHotel(activeHotel);

        List<Room> rooms = roomRepository.findAll();
        dto.setTotalRoom((long) rooms.size());
        dto.setActiveRoom(rooms.stream().filter(r -> (r.getIsDeleted() == null || !r.getIsDeleted()) && r.getRoomStatus() == Room.RoomStatus.AVAILABLE).count());

        return dto;
    }

    @Override
    public AdminDashboardDTO getChartByRange(LocalDate startDate, LocalDate endDate, Long hotelId) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Bill> bills;
        if (hotelId != null) {
            bills = billRepository.findByHotel_HotelId(hotelId);
        } else {
            bills = billRepository.findAll();
        }

        List<Bill> rangeBills = bills.stream()
                .filter(b -> b.getCreatedAt() != null && !b.getCreatedAt().isBefore(start) && !b.getCreatedAt().isAfter(end))
                .collect(Collectors.toList());

        AdminDashboardDTO dto = new AdminDashboardDTO();
        dto.setIncomeHotel(rangeBills.stream()
                .filter(b -> b.getBillStatus() == Bill.BillStatus.PAID)
                .mapToDouble(b -> b.getTotalCost() != null ? b.getTotalCost() : 0.0)
                .sum());
        dto.setCompleted(rangeBills.stream().filter(b -> b.getBillStatus() == Bill.BillStatus.PAID).count());
        dto.setCanceled(rangeBills.stream().filter(b -> b.getBillStatus() == Bill.BillStatus.CANCELED).count());

        long reviewCount = reviewRepository.findAll().stream()
                .filter(r -> hotelId == null || r.getBill().getHotel().getHotelId().equals(hotelId))
                .filter(r -> r.getCreatedAt() != null && !r.getCreatedAt().isBefore(start) && !r.getCreatedAt().isAfter(end))
                .count();
        dto.setReview(reviewCount);

        // --- Gom nhóm dữ liệu biểu đồ theo range ---
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        List<String> chartLabels = new ArrayList<>();
        List<Double> hotelIncome;
        List<Double> adminIncome;

        java.util.Map<String, Double> hotelRevenueMap = new java.util.HashMap<>();
        java.util.Map<String, Long> roomTypeBookingMap = new java.util.HashMap<>();

        if (daysBetween <= 31) {
            // Gom nhóm theo ngày
            java.util.Map<LocalDate, Integer> dateToIndex = new java.util.HashMap<>();
            LocalDate current = startDate;
            int index = 0;
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM");
            while (!current.isAfter(endDate)) {
                chartLabels.add(current.format(formatter));
                dateToIndex.put(current, index);
                current = current.plusDays(1);
                index++;
            }

            hotelIncome = new ArrayList<>(Collections.nCopies(index, 0.0));
            adminIncome = new ArrayList<>(Collections.nCopies(index, 0.0));

            for (Bill b : rangeBills) {
                LocalDate billDate = b.getCreatedAt().toLocalDate();
                if (dateToIndex.containsKey(billDate)) {
                    int idx = dateToIndex.get(billDate);
                    if (b.getBillStatus() == Bill.BillStatus.PAID) {
                        double cost = b.getTotalCost() != null ? b.getTotalCost() : 0.0;
                        hotelIncome.set(idx, hotelIncome.get(idx) + cost);
                        adminIncome.set(idx, adminIncome.get(idx) + cost * 0.1);

                        // Cộng doanh thu khách sạn (cho top 5)
                        String hotelName = b.getHotel() != null ? b.getHotel().getHotelName() : "Không xác định";
                        hotelRevenueMap.put(hotelName, hotelRevenueMap.getOrDefault(hotelName, 0.0) + cost);

                        // Cộng dồn lượt đặt loại phòng
                        if (b.getBillDetails() != null) {
                            for (BillDetail bd : b.getBillDetails()) {
                                if (bd.getRoomType() != null) {
                                    String roomTypeName = bd.getRoomType().getTypeName();

                                    String key = roomTypeName + " (" + hotelName + ")";

                                    roomTypeBookingMap.put(
                                            key,
                                            roomTypeBookingMap.getOrDefault(key, 0L) + 1L
                                    );
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Gom nhóm theo tháng
            java.time.YearMonth startYM = java.time.YearMonth.from(startDate);
            java.time.YearMonth endYM = java.time.YearMonth.from(endDate);
            java.util.Map<java.time.YearMonth, Integer> ymToIndex = new java.util.HashMap<>();
            java.time.YearMonth currentYM = startYM;
            int index = 0;
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MM/yyyy");
            while (!currentYM.isAfter(endYM)) {
                chartLabels.add(currentYM.format(formatter));
                ymToIndex.put(currentYM, index);
                currentYM = currentYM.plusMonths(1);
                index++;
            }

            hotelIncome = new ArrayList<>(Collections.nCopies(index, 0.0));
            adminIncome = new ArrayList<>(Collections.nCopies(index, 0.0));

            for (Bill b : rangeBills) {
                java.time.YearMonth billYM = java.time.YearMonth.from(b.getCreatedAt().toLocalDate());
                if (ymToIndex.containsKey(billYM)) {
                    int idx = ymToIndex.get(billYM);
                    if (b.getBillStatus() == Bill.BillStatus.PAID) {
                        double cost = b.getTotalCost() != null ? b.getTotalCost() : 0.0;
                        hotelIncome.set(idx, hotelIncome.get(idx) + cost);
                        adminIncome.set(idx, adminIncome.get(idx) + cost * 0.1);

                        // Cộng doanh thu khách sạn (cho top 5)
                        String hotelName = b.getHotel() != null ? b.getHotel().getHotelName() : "Không xác định";
                        hotelRevenueMap.put(hotelName, hotelRevenueMap.getOrDefault(hotelName, 0.0) + cost);

                        // Cộng dồn lượt đặt loại phòng
                        if (b.getBillDetails() != null) {
                            for (BillDetail bd : b.getBillDetails()) {
                                if (bd.getRoomType() != null) {
                                    String roomTypeName = bd.getRoomType().getTypeName();


                                    String key = roomTypeName + " (" + hotelName + ")";

                                    roomTypeBookingMap.put(
                                            key,
                                            roomTypeBookingMap.getOrDefault(key, 0L) + 1L
                                    );
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sắp xếp lấy Top 5 khách sạn doanh thu cao nhất
        List<java.util.Map.Entry<String, Double>> sortedHotels = hotelRevenueMap.entrySet().stream()
                .sorted(java.util.Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<String> topHotelNames = new ArrayList<>();
        List<Double> topHotelRevenues = new ArrayList<>();
        for (java.util.Map.Entry<String, Double> entry : sortedHotels) {
            topHotelNames.add(entry.getKey());
            topHotelRevenues.add(entry.getValue());
        }

        // Sắp xếp lấy Top 5 loại phòng được đặt nhiều nhất
        List<java.util.Map.Entry<String, Long>> sortedRoomTypes = roomTypeBookingMap.entrySet().stream()
                .sorted(java.util.Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<String> topRoomTypeNames = new ArrayList<>();
        List<Long> topRoomTypeBookings = new ArrayList<>();
        for (java.util.Map.Entry<String, Long> entry : sortedRoomTypes) {
            topRoomTypeNames.add(entry.getKey());
            topRoomTypeBookings.add(entry.getValue());
        }

        dto.setChartLabels(chartLabels);
        dto.setHotelIncome(hotelIncome);
        dto.setAdminIncome(adminIncome);
        dto.setTopHotelNames(topHotelNames);
        dto.setTopHotelRevenues(topHotelRevenues);
        dto.setTopRoomTypeNames(topRoomTypeNames);
        dto.setTopRoomTypeBookings(topRoomTypeBookings);

        return dto;
    }

    @Override
    public AdminDashboardDTO getChartYear(Integer year, Long hotelId) {
        if (year == null) year = LocalDate.now().getYear();
        int finalYear = year;

        List<Bill> bills;
        if (hotelId != null) {
            bills = billRepository.findByHotel_HotelId(hotelId);
        } else {
            bills = billRepository.findAll();
        }

        List<Bill> yearBills = bills.stream()
                .filter(b -> b.getCreatedAt() != null && b.getCreatedAt().getYear() == finalYear)
                .collect(Collectors.toList());

        List<User> yearUsers = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().getYear() == finalYear)
                .collect(Collectors.toList());

        AdminDashboardDTO dto = new AdminDashboardDTO();
        
        List<Double> adminIncome = new ArrayList<>(Collections.nCopies(12, 0.0));
        List<Double> hotelIncome = new ArrayList<>(Collections.nCopies(12, 0.0));
        List<Long> ordersCompleted = new ArrayList<>(Collections.nCopies(12, 0L));
        List<Long> ordersCanceled = new ArrayList<>(Collections.nCopies(12, 0L));
        List<Long> usersList = new ArrayList<>(Collections.nCopies(12, 0L));
        List<Long> managersList = new ArrayList<>(Collections.nCopies(12, 0L));

        // Maps lưu thông tin phân tích top
        java.util.Map<String, Double> hotelRevenueMap = new java.util.HashMap<>();
        java.util.Map<String, Long> roomTypeBookingMap = new java.util.HashMap<>();

        for (Bill b : yearBills) {
            int month = b.getCreatedAt().getMonthValue() - 1;
            if (b.getBillStatus() == Bill.BillStatus.PAID) {
                double cost = b.getTotalCost() != null ? b.getTotalCost() : 0.0;
                hotelIncome.set(month, hotelIncome.get(month) + cost);
                adminIncome.set(month, adminIncome.get(month) + cost * 0.1); // Admin takes 10% fee
                ordersCompleted.set(month, ordersCompleted.get(month) + 1);

                // Cộng doanh thu khách sạn
                String hotelName = b.getHotel() != null ? b.getHotel().getHotelName() : "Không xác định";
                hotelRevenueMap.put(hotelName, hotelRevenueMap.getOrDefault(hotelName, 0.0) + cost);

                // Cộng dồn số lượt đặt loại phòng
                if (b.getBillDetails() != null) {
                    for (BillDetail bd : b.getBillDetails()) {
                        if (bd.getRoomType() != null) {
                            String roomTypeName = bd.getRoomType().getTypeName();


                            String key = roomTypeName + " (" + hotelName + ")";

                            roomTypeBookingMap.put(
                                    key,
                                    roomTypeBookingMap.getOrDefault(key, 0L) + 1L
                            );
                        }
                    }
                }
            } else if (b.getBillStatus() == Bill.BillStatus.CANCELED) {
                ordersCanceled.set(month, ordersCanceled.get(month) + 1);
            }
        }

        for (User u : yearUsers) {
            int month = u.getCreatedAt().getMonthValue() - 1;
            if (u.getRole() == User.Role.USER) {
                usersList.set(month, usersList.get(month) + 1);
            } else if (u.getRole() == User.Role.MANAGER) {
                managersList.set(month, managersList.get(month) + 1);
            }
        }

        // Xử lý sắp xếp lấy Top 5 khách sạn doanh thu cao nhất
        List<java.util.Map.Entry<String, Double>> sortedHotels = hotelRevenueMap.entrySet().stream()
                .sorted(java.util.Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<String> topHotelNames = new ArrayList<>();
        List<Double> topHotelRevenues = new ArrayList<>();
        for (java.util.Map.Entry<String, Double> entry : sortedHotels) {
            topHotelNames.add(entry.getKey());
            topHotelRevenues.add(entry.getValue());
        }

        // Xử lý sắp xếp lấy Top 5 loại phòng được đặt nhiều nhất
        List<java.util.Map.Entry<String, Long>> sortedRoomTypes = roomTypeBookingMap.entrySet().stream()
                .sorted(java.util.Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<String> topRoomTypeNames = new ArrayList<>();
        List<Long> topRoomTypeBookings = new ArrayList<>();
        for (java.util.Map.Entry<String, Long> entry : sortedRoomTypes) {
            topRoomTypeNames.add(entry.getKey());
            topRoomTypeBookings.add(entry.getValue());
        }

        dto.setChartLabels(java.util.Arrays.asList("T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"));
        dto.setAdminIncome(adminIncome);
        dto.setHotelIncome(hotelIncome);
        dto.setOrdersCompleted(ordersCompleted);
        dto.setOrdersCanceled(ordersCanceled);
        dto.setUsers(usersList);
        dto.setManagers(managersList);

        // Đưa dữ liệu phân tích top vào DTO
        dto.setTopHotelNames(topHotelNames);
        dto.setTopHotelRevenues(topHotelRevenues);
        dto.setTopRoomTypeNames(topRoomTypeNames);
        dto.setTopRoomTypeBookings(topRoomTypeBookings);

        return dto;
    }
}
