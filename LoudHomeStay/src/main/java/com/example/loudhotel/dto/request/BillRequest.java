package com.example.loudhotel.dto.request;

import com.example.loudhotel.entity.Bill;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BillRequest {

    @NotNull
    private Long hotelId;

    private String orderName;
    private String orderEmail;
    private String orderPhone;

    @NotNull
    private LocalDate checkInDate;

    @NotNull
    private LocalDate checkOutDate;

    private String paymentMethod;

    private String idCardCode;

    private String voucherCode;

    // ✅ sửa lại
    @NotNull
    private List<RoomSelection> rooms;

    @Data
    public static class RoomSelection {
        private Long typeId;     // đổi tên
        private Integer quantity;
        private Integer guestCount;
    }
}
