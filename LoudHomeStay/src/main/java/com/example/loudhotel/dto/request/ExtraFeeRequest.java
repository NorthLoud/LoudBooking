package com.example.loudhotel.dto.request;

import lombok.Data;

@Data
public class ExtraFeeRequest {
    private Double amount;
    private String reason;
}
