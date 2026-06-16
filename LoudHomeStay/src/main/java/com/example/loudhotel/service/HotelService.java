package com.example.loudhotel.service;

import com.example.loudhotel.dto.request.HotelRequest;
import com.example.loudhotel.dto.response.HotelResponse;
import com.example.loudhotel.dto.response.HotelSearchResponse;

import java.time.LocalDate;
import java.util.List;

public interface HotelService {

    HotelResponse createHotel(HotelRequest request);

    HotelResponse getHotelById(Long hotelId);

    List<HotelResponse> getAll();
    org.springframework.data.domain.Page<HotelResponse> getAll(org.springframework.data.domain.Pageable pageable);

    List<HotelResponse> searchAll(String keyword);
    org.springframework.data.domain.Page<HotelResponse> searchAll(String keyword, org.springframework.data.domain.Pageable pageable);

    void deleteHotel(Long id);

    HotelResponse updateHotel(Long id, HotelRequest request);

    List<HotelResponse> getMyHotels();
    org.springframework.data.domain.Page<HotelResponse> getMyHotels(String keyword, org.springframework.data.domain.Pageable pageable);

    List<HotelSearchResponse> searchAvailableHotels(
            String keyword,
            LocalDate checkIn,
            LocalDate checkOut,
            Integer roomCount
    );
}
