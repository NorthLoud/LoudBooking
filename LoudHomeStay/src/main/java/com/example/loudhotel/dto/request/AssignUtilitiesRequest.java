package com.example.loudhotel.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class AssignUtilitiesRequest {

    private String newUtilityName;
    private List<Long> utilityIds;
    private Long hotelId;
}
