package com.example.loudhotel.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequest {

    @NotNull
    private Long billId;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("10.0")
    private Double rate;

    private String comment;
}
