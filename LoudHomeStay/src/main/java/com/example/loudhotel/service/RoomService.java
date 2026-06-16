package com.example.loudhotel.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.loudhotel.dto.request.RoomRequest;
import com.example.loudhotel.dto.response.RoomResponse;
import com.example.loudhotel.entity.Room.RoomStatus;

public interface RoomService {

    RoomResponse create(Long hotelId, RoomRequest request);

    List<RoomResponse> getByHotel(Long hotelId);

    void deleteRoom(Long roomId);

    List<RoomResponse> getAllRooms();

    Page<RoomResponse> getAllRooms(String keyword, Long hotelId, Long typeId, RoomStatus roomStatus, Pageable pageable);

    Page<RoomResponse> getRoomsByManager(Long managerId, String keyword, Long typeId, RoomStatus roomStatus, Pageable pageable);

    RoomResponse getRoomById(Long id);

    RoomResponse update(Long id, RoomRequest request);

    List<RoomResponse> getAvailableRooms(
            Long hotelId,
            LocalDate checkIn,
            LocalDate checkOut,
            Integer guest);

    List<RoomResponse> getRoomsByManager(Long managerId);

}
