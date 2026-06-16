package com.example.loudhotel.controller;

import com.example.loudhotel.dto.request.RoomRequest;
import com.example.loudhotel.dto.response.ImageResponse;
import com.example.loudhotel.dto.response.RoomResponse;
import com.example.loudhotel.entity.User;
import com.example.loudhotel.repository.UserRepository;
import com.example.loudhotel.service.ImageService;
import com.example.loudhotel.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final ImageService imageService;
    private final UserRepository userRepository;

    @GetMapping("/my")
    @PreAuthorize("hasRole('MANAGER')")
    public List<RoomResponse> getMyRooms(Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository
                .findByEmailAndIsDeletedFalse(email)
                .orElseThrow();

        return roomService.getRoomsByManager(user.getUserId());
    }

    @GetMapping("/my/summary")
    @PreAuthorize("hasRole('MANAGER')")
    public org.springframework.data.domain.Page<RoomResponse> getMyRoomsSummary(
            Authentication authentication,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) com.example.loudhotel.entity.Room.RoomStatus roomStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "roomId") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        String email = authentication.getName();
        User user = userRepository.findByEmailAndIsDeletedFalse(email).orElseThrow();
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase("desc")
                ? org.springframework.data.domain.Sort.by(sortBy).descending()
                : org.springframework.data.domain.Sort.by(sortBy).ascending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return roomService.getRoomsByManager(user.getUserId(), keyword, typeId, roomStatus, pageable);
    }

    @GetMapping
    public List<RoomResponse> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public org.springframework.data.domain.Page<RoomResponse> summary(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long hotelId,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) com.example.loudhotel.entity.Room.RoomStatus roomStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "roomId") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase("desc")
                ? org.springframework.data.domain.Sort.by(sortBy).descending()
                : org.springframework.data.domain.Sort.by(sortBy).ascending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return roomService.getAllRooms(keyword, hotelId, typeId, roomStatus, pageable);
    }

    @GetMapping("/hotels/{hotelId}")
    public List<RoomResponse> getRoomsByHotel(@PathVariable Long hotelId) {
        return roomService.getByHotel(hotelId);
    }

    @GetMapping("/{id}")
    public RoomResponse getRoomById(@PathVariable Long id){
        return roomService.getRoomById(id);
    }

    @PostMapping("/hotel/{hotelId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<RoomResponse> createRoom(
            @PathVariable Long hotelId,
            @Valid @RequestBody RoomRequest request) {

        return ResponseEntity.ok(roomService.create(hotelId, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable Long id,
            @RequestBody RoomRequest request) {

        return ResponseEntity.ok(roomService.update(id, request));
    }

    @DeleteMapping("/{roomId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.ok("Xóa phòng thành công");
    }

    @GetMapping("/available")
    public List<RoomResponse> getAvailableRooms(
            @RequestParam Long hotelId,
            @RequestParam String checkIn,
            @RequestParam String checkOut,
            @RequestParam(required = false) Integer guest
    ) {
        return roomService.getAvailableRooms(
                hotelId,
                LocalDate.parse(checkIn),
                LocalDate.parse(checkOut),
                guest
        );
    }
}
