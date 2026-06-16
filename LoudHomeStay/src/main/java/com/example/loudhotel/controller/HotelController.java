package com.example.loudhotel.controller;

import com.example.loudhotel.dto.request.HotelRequest;
import com.example.loudhotel.dto.response.HotelResponse;
import com.example.loudhotel.dto.response.HotelSearchResponse;
import com.example.loudhotel.dto.response.ImageResponse;
import com.example.loudhotel.dto.response.UtilitiesResponse;
import com.example.loudhotel.service.HotelService;
import com.example.loudhotel.service.ImageService;
import com.example.loudhotel.service.UtilitiesHotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;
    private final ImageService imageService;
    private final UtilitiesHotelService utilitiesHotelService;

    // Hiển thị theo manager
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public List<HotelResponse> getMyHotels(){

        return hotelService.getMyHotels();

    }

    @GetMapping("/my/summary")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public org.springframework.data.domain.Page<HotelResponse> getMyHotelsSummary(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "hotelId") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase("desc")
                ? org.springframework.data.domain.Sort.by(sortBy).descending()
                : org.springframework.data.domain.Sort.by(sortBy).ascending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return hotelService.getMyHotels(keyword, pageable);
    }

    // CREATE -> chỉ MANAGER
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public HotelResponse create(
            @Valid @RequestBody HotelRequest request) {

        return hotelService.createHotel(request);
    }

    // UPDATE -> ADMIN + MANAGER
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public HotelResponse update(
            @PathVariable Long id,
            @Valid @RequestBody HotelRequest request) {

        return hotelService.updateHotel(id, request);
    }

    // DELETE -> ADMIN + MANAGER
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        hotelService.deleteHotel(id);
    }

    // PUBLIC
    @GetMapping("/{id}")
    public HotelResponse get(@PathVariable Long id) {
        return hotelService.getHotelById(id);
    }

    @GetMapping
    public List<HotelResponse> getAll() {
        return hotelService.getAll();
    }

    @GetMapping("/summary")
    public org.springframework.data.domain.Page<HotelResponse> summary(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "hotelId") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase("desc")
                ? org.springframework.data.domain.Sort.by(sortBy).descending()
                : org.springframework.data.domain.Sort.by(sortBy).ascending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        if (keyword != null && !keyword.trim().isEmpty()) {
            return hotelService.searchAll(keyword, pageable);
        }
        return hotelService.getAll(pageable);
    }

    @GetMapping("/search")
    public ResponseEntity<List<HotelResponse>> search(
            @RequestParam String keyword) {
        return ResponseEntity.ok(hotelService.searchAll(keyword));
    }

    // IMAGE
    @PostMapping("/{id}/images")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<ImageResponse>> uploadImages(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam Integer mainIndex) {

        return ResponseEntity.ok(
                imageService.uploadHotelImages(id, files, mainIndex)
        );
    }

    @PutMapping("/images/{imageId}/main")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void setMain(@PathVariable Long imageId) {
        imageService.setMainImage(imageId);
    }

    @DeleteMapping("/images/{imageId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void deleteImage(@PathVariable Long imageId) {
        imageService.deleteImage(imageId);
    }

    @GetMapping("/{id}/images")
    public List<ImageResponse> getImages(@PathVariable Long id) {
        return imageService.getImagesByHotel(id);
    }

    @GetMapping("/{id}/utilities")
    public List<UtilitiesResponse> getUtilitiesByHotel(@PathVariable Long id) {
        return utilitiesHotelService.getUtilitiesByHotelPublic(id);
    }

    @GetMapping("/search-available")
    public ResponseEntity<List<HotelSearchResponse>> searchAvailable(
            @RequestParam String keyword,
            @RequestParam String checkIn,
            @RequestParam String checkOut,
            @RequestParam Integer roomCount
    ) {

        return ResponseEntity.ok(
                hotelService.searchAvailableHotels(
                        keyword,
                        java.time.LocalDate.parse(checkIn),
                        java.time.LocalDate.parse(checkOut),
                        roomCount
                )
        );
    }

}