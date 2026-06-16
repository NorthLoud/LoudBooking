package com.example.loudhotel.controller;

import com.example.loudhotel.dto.response.RoomTypeUtilitySummaryResponse;
import com.example.loudhotel.dto.response.UtilitiesResponse;
import com.example.loudhotel.service.UtilitiesRoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/room-type-utilities")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoomTypeUtilitiesController {

    private final UtilitiesRoomTypeService service;

    @GetMapping("/{typeId}")
    public Page<UtilitiesResponse> getByRoomType(
            @PathVariable Long typeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "utilities.utilitiesName") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return service.getUtilitiesByRoomType(typeId, keyword, pageable);
    }

    @PostMapping("/{typeId}/{utilityId}")
    public void assign(@PathVariable Long typeId,
                       @PathVariable Long utilityId) {

        // ✅ admin không cần check ownership
        service.assignUtilityToRoomType(typeId, utilityId);
    }

    @DeleteMapping("/{typeId}/{utilityId}")
    public void remove(@PathVariable Long typeId,
                       @PathVariable Long utilityId) {

        service.removeUtilityFromRoomType(typeId, utilityId);
    }

    @GetMapping("/summary")
    public Page<RoomTypeUtilitySummaryResponse> summary(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "hotelId") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return service.getSummary(keyword, pageable);
    }
}