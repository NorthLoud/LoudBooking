package com.example.loudhotel.controller;


import com.example.loudhotel.dto.request.RoomTypeRequest;
import com.example.loudhotel.dto.response.ImageResponse;
import com.example.loudhotel.dto.response.RoomTypeResponse;
import com.example.loudhotel.entity.User;
import com.example.loudhotel.repository.UserRepository;
import com.example.loudhotel.service.ImageService;
import com.example.loudhotel.service.RoomTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/api/room-types")
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeService roomTypeService;
    private final ImageService imageService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<RoomTypeResponse> getAll() {
        return roomTypeService.getAll();
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public org.springframework.data.domain.Page<RoomTypeResponse> summary(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "typeId") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase("desc")
                ? org.springframework.data.domain.Sort.by(sortBy).descending()
                : org.springframework.data.domain.Sort.by(sortBy).ascending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return roomTypeService.getAll(keyword, pageable);
    }

    @GetMapping("/{id}")
    public RoomTypeResponse getById(@PathVariable Long id) {
        return roomTypeService.getById(id);
    }

    @PostMapping("/hotel/{hotelId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public RoomTypeResponse create(
            @PathVariable Long hotelId,
            @Valid @RequestBody RoomTypeRequest request
    ) {
        return roomTypeService.create(hotelId, request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public RoomTypeResponse update(@PathVariable Long id,
                         @Valid @RequestBody RoomTypeRequest request) {
        return roomTypeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(@PathVariable Long id) {
        roomTypeService.delete(id);
    }

    @GetMapping("/hotel/{hotelId}")
    public List<RoomTypeResponse> getByHotel(@PathVariable Long hotelId) {
        return roomTypeService.getByHotel(hotelId);
    }

    @PostMapping("/{typeId}/images")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<ImageResponse>> upload(
            @PathVariable Long typeId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam Integer mainIndex) {

        return ResponseEntity.ok(
                imageService.uploadRoomTypeImages(typeId, files, mainIndex)
        );
    }

    @GetMapping("/{typeId}/images")
    public List<ImageResponse> getImages(@PathVariable Long typeId) {
        return imageService.getImagesByRoomType(typeId);
    }

    @PutMapping("/images/{imageId}/main")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void setMain(@PathVariable Long imageId) {
        imageService.setMainRoomTypeImage(imageId);
    }

    @DeleteMapping("/images/{imageId}")
    public void deleteImage(@PathVariable Long imageId) {
        imageService.deleteRoomTypeImage(imageId);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('MANAGER')")
    public List<RoomTypeResponse> getMyRoomTypes(Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository
                .findByEmailAndIsDeletedFalse(email)
                .orElseThrow();

        return roomTypeService.getRoomTypesByManager(user.getUserId());
    }

    @GetMapping("/my/summary")
    @PreAuthorize("hasRole('MANAGER')")
    public org.springframework.data.domain.Page<RoomTypeResponse> getMyRoomTypesSummary(
            Authentication authentication,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "typeId") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        String email = authentication.getName();
        User user = userRepository.findByEmailAndIsDeletedFalse(email).orElseThrow();
        
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase("desc")
                ? org.springframework.data.domain.Sort.by(sortBy).descending()
                : org.springframework.data.domain.Sort.by(sortBy).ascending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        
        return roomTypeService.getRoomTypesByManager(user.getUserId(), keyword, pageable);
    }
}