package com.example.loudhotel.controller;

import com.example.loudhotel.dto.response.RoomTypeUtilitySummaryResponse;
import com.example.loudhotel.service.UtilitiesRoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilities-room-types")
@RequiredArgsConstructor
public class RoomTypeUtilitiesController {

    private final UtilitiesRoomTypeService service;

    @GetMapping("/hotel/{hotelId}/summary")
    public List<RoomTypeUtilitySummaryResponse> summaryByHotel(
            @PathVariable Long hotelId
    ) {
        return service.getSummaryByHotelId(hotelId);
    }

}