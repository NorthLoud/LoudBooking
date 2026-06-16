package com.example.loudhotel.dto.response;

import com.example.loudhotel.entity.Hotel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class HotelResponse {

    private Long hotelId;
    private String hotelName;
    private String address;
    private String introduction;
    private Double averageRating;
    private Integer roomsTotal;
    private Hotel.HotelStatus hotelStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String managerEmail;
    private String mainImage;
    private List<ImageResponse> images;

    private List<String> utilities;
    private String managerFullName;
    private Long managerId;

}
