package com.example.loudhotel.dto.response;

import com.example.loudhotel.entity.Room;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RoomResponse {

    private Long roomId;

    private Long roomTypeId;
    private String roomTypeName;
    private Integer capacity;
    private Double price;
    private String bedType;
    private Integer bedCount;
    private Double area;

    private Integer roomNumber;
    private Room.RoomStatus roomStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long hotelId;
    private String hotelStatus;

    private String mainImage;
    private List<ImageResponse> images;

}
