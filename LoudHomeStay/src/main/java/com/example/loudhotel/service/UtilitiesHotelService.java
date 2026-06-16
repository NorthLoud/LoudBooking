package com.example.loudhotel.service;

import com.example.loudhotel.dto.response.HotelUtilitySummaryResponse;
import com.example.loudhotel.dto.response.UtilitiesResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UtilitiesHotelService {


    void assignUtilityToHotel(Long hotelId, Long utilityId);

    void removeUtilityFromHotel(Long hotelId, Long utilityId);

    List<UtilitiesResponse> getUtilitiesByHotel(Long hotelId);

    Page<UtilitiesResponse> getAll(String keyword, String hotelStatus, int page, String sortBy, String sortDir);

    Page<HotelUtilitySummaryResponse> getSummary(
            String keyword,
            String hotelStatus,
            int page,
            String sortBy,
            String sortDir
    );

    Page<HotelUtilitySummaryResponse> getSummaryByManager(
            Long managerId,
            String keyword,
            String hotelStatus,
            int page,
            String sortBy,
            String sortDir
    );

    List<UtilitiesResponse> getUtilitiesByHotelPublic(Long hotelId);


}
