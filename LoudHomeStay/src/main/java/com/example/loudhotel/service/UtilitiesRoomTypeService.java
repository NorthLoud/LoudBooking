package com.example.loudhotel.service;

import com.example.loudhotel.dto.response.RoomTypeUtilitySummaryResponse;
import com.example.loudhotel.dto.response.UtilitiesResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UtilitiesRoomTypeService {

    void assignUtilityToRoomType(Long typeId, Long utilityId);

    void removeUtilityFromRoomType(Long typeId, Long utilityId);

    Page<UtilitiesResponse> getUtilitiesByRoomType(
            Long typeId,
            String keyword,
            Pageable pageable
    );

    Page<RoomTypeUtilitySummaryResponse> getSummary(
            String keyword,
            Pageable pageable
    );

    Page<RoomTypeUtilitySummaryResponse> getSummaryByManager(
            Long managerId,
            String keyword,
            Pageable pageable
    );

    List<RoomTypeUtilitySummaryResponse> getSummaryByHotelId(Long hotelId);

}