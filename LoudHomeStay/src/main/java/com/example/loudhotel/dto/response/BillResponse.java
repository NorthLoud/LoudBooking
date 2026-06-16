package com.example.loudhotel.dto.response;

import com.example.loudhotel.entity.Bill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillResponse {

    private Long billId;
    private String billCode;

    private Long userId;
    private String userName;

    private Long hotelId;
    private String hotelName;

    private String orderName;
    private String orderEmail;
    private String orderPhone;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    private Double totalCost;
    private Double roomTotal;
    private Double extraFeeTotal;
    private Double discountAmount;
    private String voucherCode;

    private Bill.BillStatus billStatus;
    private Bill.PaymentMethod paymentMethod;

    private LocalDateTime createdAt;
    private LocalDateTime actualCheckInTime;
    private LocalDateTime actualCheckOutTime;
    private LocalDateTime updatedAt;

    private Bill.CancelReason cancelReason;
    private boolean isReviewed;

    private String hotelAddress;
    private String managerEmail;

    private String idCardCode;
    private Integer guestCount;

    private List<RoomItem> rooms;
    private List<BillDetailItem> details;
    private List<ExtraFeeItem> extraFees;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomItem {
        private Long roomId;
        private String roomName;
        private Integer nights;
        private String roomType;
        private Integer capacity;
        private Integer roomNumber;
        private Integer guestCount;
        private Double price;
        private Double subtotal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillDetailItem {
        private Long typeId;
        private String typeName;
        private Integer quantity;
        private Double priceAtBooking;
        private Integer nights;
        private Integer guestCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtraFeeItem {
        private Long extraFeeId;
        private Double amount;
        private String reason;
        private LocalDateTime createdAt;
        private boolean isPaid;
    }
}
