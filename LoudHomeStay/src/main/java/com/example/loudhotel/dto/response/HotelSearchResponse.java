package com.example.loudhotel.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HotelSearchResponse {

    private Long hotelId;

    private String hotelName;

    private String address;

    private Double averageRating;

    private String mainImage;

    private Double minPrice;

    private Integer availableRooms;

    private List<String> utilities;

    private String introduction;

}