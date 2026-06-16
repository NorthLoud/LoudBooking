package com.example.loudhotel.dto.request;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VoucherRequest {
    private String voucherCode;
    private String title;
    private String description;
    private String discountType; // PERCENT, FIXED
    private Double discountValue;
    private Double minBillAmount;
    private Double maxDiscountAmount;
    private Integer quantity;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status; // ACTIVE, INACTIVE, EXPIRED
    private List<Long> hotelIds;
}
