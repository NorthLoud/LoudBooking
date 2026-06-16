package com.example.loudhotel.dto.response;

import com.example.loudhotel.entity.Voucher;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class VoucherResponse {
    private Long voucherId;
    private String voucherCode;
    private String title;
    private String description;
    private Voucher.DiscountType discountType;
    private Double discountValue;
    private Double minBillAmount;
    private Double maxDiscountAmount;
    private Integer quantity;
    private Integer usedCount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Voucher.VoucherStatus status;
    private List<Long> hotelIds;
}
