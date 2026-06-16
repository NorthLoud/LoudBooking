package com.example.loudhotel.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HotelUtilitySummaryResponse {

    private Long hotelId;
    private String hotelName;
    private Integer utilityCount;
    private String hotelStatus;
}