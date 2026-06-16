package com.example.loudhotel.repository;

import com.example.loudhotel.entity.RoomTypeImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomTypeImageRepository extends JpaRepository<RoomTypeImage, Long> {

    List<RoomTypeImage> findByRoomType_TypeId(Long typeId);

    Optional<RoomTypeImage> findByRoomType_TypeIdAndIsMainTrue(Long typeId);

    void deleteByRoomType_TypeId(Long typeId);
}