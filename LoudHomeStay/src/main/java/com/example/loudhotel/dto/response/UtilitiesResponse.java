package com.example.loudhotel.dto.response;

import com.example.loudhotel.enums.UtilityType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UtilitiesResponse {

    private Long id;          // utilities_id
    private String name;      // utilities_name
    private UtilityType utilityType;

    private Long hotelId;  // homestay_id
    private String hotelName;
    private String hotelStatus;
}
