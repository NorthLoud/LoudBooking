package com.example.loudhotel.repository;

import com.example.loudhotel.entity.Hotel;
import com.example.loudhotel.entity.UtilitiesHotel;
import com.example.loudhotel.entity.UtilitiesHotelId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UtilitiesHotelRepository
        extends JpaRepository<UtilitiesHotel, UtilitiesHotelId> {

    @Query("""
SELECT uh FROM UtilitiesHotel uh
WHERE uh.hotel.isDeleted = false
AND (:hotelStatus IS NULL OR uh.hotel.hotelStatus = :hotelStatus)
AND (
    :keyword IS NULL OR
    CAST(uh.hotel.hotelId AS string) LIKE %:keyword% OR
    LOWER(uh.hotel.hotelName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
    LOWER(uh.utilities.utilitiesName) LIKE LOWER(CONCAT('%', :keyword, '%'))
)
""")
    Page<UtilitiesHotel> searchAll(
            @Param("keyword") String keyword,
            @Param("hotelStatus") Hotel.HotelStatus hotelStatus,
            Pageable pageable
    );

    @Query("""
SELECT uh FROM UtilitiesHotel uh
WHERE uh.hotel.hotelId = :hotelId
AND uh.hotel.isDeleted = false
""")
    List<UtilitiesHotel> findByHotelActive(@Param("hotelId") Long hotelId);

    @Query("""
SELECT h.hotelId,
       h.hotelName,
       h.hotelStatus,
       COUNT(uh.utilities.utilitiesId) as utilityCount
FROM Hotel h
LEFT JOIN UtilitiesHotel uh
       ON uh.hotel.hotelId = h.hotelId
WHERE h.isDeleted = false
  AND (:status IS NULL OR h.hotelStatus = :status)
GROUP BY h.hotelId, h.hotelName, h.hotelStatus
HAVING (
      :keyword IS NULL OR :keyword = ''
      OR CAST(h.hotelId AS string) LIKE %:keyword%
      OR LOWER(h.hotelName) LIKE LOWER(CONCAT('%', :keyword, '%'))
)
ORDER BY
    CASE WHEN :sortBy = 'hotelId' AND :sortDir = 'asc' THEN h.hotelId END ASC,
    CASE WHEN :sortBy = 'hotelId' AND :sortDir = 'desc' THEN h.hotelId END DESC,

    CASE WHEN :sortBy = 'hotelName' AND :sortDir = 'asc' THEN h.hotelName END ASC,
    CASE WHEN :sortBy = 'hotelName' AND :sortDir = 'desc' THEN h.hotelName END DESC,

    CASE WHEN :sortBy = 'utilityCount' AND :sortDir = 'asc' THEN COUNT(uh.utilities.utilitiesId) END ASC,
    CASE WHEN :sortBy = 'utilityCount' AND :sortDir = 'desc' THEN COUNT(uh.utilities.utilitiesId) END DESC
""")
    Page<Object[]> countSummary(
            @Param("keyword") String keyword,
            @Param("status") Hotel.HotelStatus status,
            @Param("sortBy") String sortBy,
            @Param("sortDir") String sortDir,
            Pageable pageable
    );

    @Query("""
SELECT h.hotelId,
       h.hotelName,
       h.hotelStatus,
       COUNT(uh.utilities.utilitiesId)
FROM Hotel h
LEFT JOIN UtilitiesHotel uh
       ON uh.hotel.hotelId = h.hotelId
WHERE h.isDeleted = false
  AND h.manager.userId = :managerId
  AND (:status IS NULL OR h.hotelStatus = :status)
GROUP BY h.hotelId, h.hotelName, h.hotelStatus
HAVING (
      :keyword IS NULL OR :keyword = ''
      OR CAST(h.hotelId AS string) LIKE %:keyword%
      OR LOWER(h.hotelName) LIKE LOWER(CONCAT('%', :keyword, '%'))
)
ORDER BY
    CASE WHEN :sortBy = 'hotelId' AND :sortDir = 'asc' THEN h.hotelId END ASC,
    CASE WHEN :sortBy = 'hotelId' AND :sortDir = 'desc' THEN h.hotelId END DESC,

    CASE WHEN :sortBy = 'hotelName' AND :sortDir = 'asc' THEN h.hotelName END ASC,
    CASE WHEN :sortBy = 'hotelName' AND :sortDir = 'desc' THEN h.hotelName END DESC,

    CASE WHEN :sortBy = 'utilityCount' AND :sortDir = 'asc' THEN COUNT(uh.utilities.utilitiesId) END ASC,
    CASE WHEN :sortBy = 'utilityCount' AND :sortDir = 'desc' THEN COUNT(uh.utilities.utilitiesId) END DESC
""")
    Page<Object[]> countSummaryByManager(
            @Param("managerId") Long managerId,
            @Param("keyword") String keyword,
            @Param("status") Hotel.HotelStatus status,
            @Param("sortBy") String sortBy,
            @Param("sortDir") String sortDir,
            Pageable pageable
    );

    void deleteByHotel_HotelId(Long hotelId);
}
