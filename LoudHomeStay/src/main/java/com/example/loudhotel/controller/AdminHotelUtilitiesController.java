package com.example.loudhotel.controller;

import com.example.loudhotel.dto.response.HotelUtilitySummaryResponse;
import com.example.loudhotel.dto.response.UtilitiesResponse;
import com.example.loudhotel.service.UtilitiesHotelService;
import com.example.loudhotel.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/hotel-utilities")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminHotelUtilitiesController {

    private final UtilitiesHotelService utilitiesHotelService;

    @GetMapping
    public Page<UtilitiesResponse> getAll(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) String hotelStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "hotelId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return utilitiesHotelService.getAll(
                keyword, hotelStatus, page, sortBy, sortDir
        );
    }

    @GetMapping("/{hotelId}")
    public List<UtilitiesResponse> getByHotel(@PathVariable Long hotelId) {
        return utilitiesHotelService.getUtilitiesByHotel(hotelId);
    }

    @GetMapping("/summary")
    public Page<HotelUtilitySummaryResponse> getSummary(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String hotelStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "hotelId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return utilitiesHotelService.getSummary(
                keyword, hotelStatus, page, sortBy, sortDir
        );
    }

    @PostMapping("/{hotelId}/{utilityId}")
    public void assign(@PathVariable Long hotelId,
                       @PathVariable Long utilityId) {

        utilitiesHotelService.assignUtilityToHotel(hotelId, utilityId);
    }

    @DeleteMapping("/{hotelId}/{utilityId}")
    public void remove(@PathVariable Long hotelId,
                       @PathVariable Long utilityId) {

        utilitiesHotelService.removeUtilityFromHotel(hotelId, utilityId);
    }
}