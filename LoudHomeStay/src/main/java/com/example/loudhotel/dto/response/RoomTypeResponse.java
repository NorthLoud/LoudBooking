package com.example.loudhotel.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RoomTypeResponse {

    private Long typeId;
    private String typeName;
    private Integer capacity;
    private Double price;

    private String description;

    private String bedType;
    private Integer bedCount;
    private Double area;

    private Long hotelId;
    private String hotelStatus;
    private String hotelName;

    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    private String mainImage;
    private List<ImageResponse> images;

    private List<UtilitiesResponse> utilities;
}