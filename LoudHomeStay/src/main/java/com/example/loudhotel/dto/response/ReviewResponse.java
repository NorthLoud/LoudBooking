package com.example.loudhotel.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {

    private Long reviewId;
    private String username;
    private Double rate;
    private String comment;
    @JsonFormat(pattern = "HH:mm:ss dd/MM/yyyy", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "HH:mm:ss dd/MM/yyyy", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime updatedAt;
    private String status; // ACTIVE | INACTIVE | PENDING
    private String hotelName;
    private boolean isMine;

    private Long hotelId;
    private Long billId;
    private Long managerId; // hoặc hotel.managerId

    private String roomTypeNames;
    private Integer nights;
}
