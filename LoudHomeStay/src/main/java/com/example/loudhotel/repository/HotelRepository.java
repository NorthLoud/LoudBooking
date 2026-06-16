package com.example.loudhotel.repository;

import com.example.loudhotel.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    List<Hotel> findByIsDeletedFalse();
    Page<Hotel> findByIsDeletedFalse(Pageable pageable);

    @Query("""
    SELECT h FROM Hotel h
    WHERE h.isDeleted = false AND (
        CAST(h.hotelId AS string) LIKE %:keyword%
        OR LOWER(h.hotelName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(h.address) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(h.introduction) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR CAST(h.averageRating AS string) LIKE %:keyword%
    )
""")
    List<Hotel> searchAll(@Param("keyword") String keyword);
    @Query("""
    SELECT h FROM Hotel h
    WHERE h.isDeleted = false AND (
        CAST(h.hotelId AS string) LIKE %:keyword%
        OR LOWER(h.hotelName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(h.address) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(h.introduction) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR CAST(h.averageRating AS string) LIKE %:keyword%
    )
""")
    Page<Hotel> searchAll(@Param("keyword") String keyword, Pageable pageable);

    Optional<Hotel> findByHotelIdAndIsDeletedFalse(Long id);

    @Query("SELECT h FROM Hotel h LEFT JOIN FETCH h.images WHERE h.isDeleted = false")
    List<Hotel> findAllWithImages();

    List<Hotel> findByManager_UserIdAndIsDeletedFalse(Long userId);
    Page<Hotel> findByManager_UserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    @Query("""
    SELECT h FROM Hotel h
    WHERE h.isDeleted = false AND h.manager.userId = :userId AND (
        CAST(h.hotelId AS string) LIKE %:keyword%
        OR LOWER(h.hotelName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(h.address) LIKE LOWER(CONCAT('%', :keyword, '%'))
    )
    """)
    Page<Hotel> searchMyHotels(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);

    boolean existsByManager_UserIdAndIsDeletedFalse(Long userId);

    Optional<Hotel> findByManager_UserId(Long userId);
}
