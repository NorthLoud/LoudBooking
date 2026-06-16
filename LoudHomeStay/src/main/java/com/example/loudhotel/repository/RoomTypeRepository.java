package com.example.loudhotel.repository;

import com.example.loudhotel.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

    @Query("SELECT rt FROM RoomType rt LEFT JOIN FETCH rt.images WHERE rt.hotel.hotelId = :hotelId AND rt.isDeleted = false")
    List<RoomType> findByHotel_HotelIdAndIsDeletedFalse(@Param("hotelId") Long hotelId);

    Optional<RoomType> findByTypeIdAndIsDeletedFalse(Long id);

    List<RoomType> findByIsDeletedFalse();
    org.springframework.data.domain.Page<RoomType> findByIsDeletedFalse(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT rt FROM RoomType rt WHERE rt.isDeleted = false AND (LOWER(rt.typeName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(rt.hotel.hotelName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(CAST(rt.bedType AS string)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR (:bedKeyword IS NOT NULL AND LOWER(CAST(rt.bedType AS string)) LIKE LOWER(CONCAT('%', :bedKeyword, '%'))))")
    org.springframework.data.domain.Page<RoomType> searchAll(@Param("keyword") String keyword, @Param("bedKeyword") String bedKeyword, org.springframework.data.domain.Pageable pageable);

    // Keep old signature for backward compatibility
    default org.springframework.data.domain.Page<RoomType> searchAll(String keyword, org.springframework.data.domain.Pageable pageable) {
        return searchAll(keyword, null, pageable);
    }

    @Query("SELECT rt FROM RoomType rt WHERE rt.hotel.manager.userId = :userId AND rt.isDeleted = false")
    org.springframework.data.domain.Page<RoomType> findByManagerUserId(@Param("userId") Long userId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT rt FROM RoomType rt WHERE rt.hotel.manager.userId = :userId AND rt.isDeleted = false AND (LOWER(rt.typeName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(rt.hotel.hotelName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(CAST(rt.bedType AS string)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR (:bedKeyword IS NOT NULL AND LOWER(CAST(rt.bedType AS string)) LIKE LOWER(CONCAT('%', :bedKeyword, '%'))))")
    org.springframework.data.domain.Page<RoomType> searchByManagerUserId(@Param("userId") Long userId, @Param("keyword") String keyword, @Param("bedKeyword") String bedKeyword, org.springframework.data.domain.Pageable pageable);

    // Keep old signature for backward compatibility
    default org.springframework.data.domain.Page<RoomType> searchByManagerUserId(Long userId, String keyword, org.springframework.data.domain.Pageable pageable) {
        return searchByManagerUserId(userId, keyword, null, pageable);
    }
    boolean existsByHotel_HotelIdAndTypeNameAndIsDeletedFalse(Long hotelId, String typeName);

    boolean existsByHotel_HotelIdAndIsDeletedFalse(Long hotelId);

    @Query("""
SELECT MIN(rt.price)
FROM RoomType rt

WHERE rt.hotel.hotelId = :hotelId
AND rt.isDeleted = false
""")
    Double findMinPriceByHotelId(@Param("hotelId") Long hotelId);

}
