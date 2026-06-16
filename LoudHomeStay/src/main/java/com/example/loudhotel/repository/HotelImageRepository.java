package com.example.loudhotel.repository;

import com.example.loudhotel.entity.HotelImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HotelImageRepository extends JpaRepository<HotelImage, Long> {

    List<HotelImage> findByHotel_HotelId(Long hotelId);

    Optional<HotelImage> findByHotel_HotelIdAndIsMainTrue(Long hotelId);

    void deleteByHotel_HotelId(Long hotelId);
}
