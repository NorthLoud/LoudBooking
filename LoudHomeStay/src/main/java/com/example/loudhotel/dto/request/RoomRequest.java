package com.example.loudhotel.dto.request;

import com.example.loudhotel.entity.Room;
import lombok.Data;

import java.util.List;

@Data
public class RoomRequest {

    private Long hotelId;

    private Long roomTypeId;

    private Integer roomNumber;

    private Room.RoomStatus roomStatus;

    private List<Long> utilitiesIds;

}
