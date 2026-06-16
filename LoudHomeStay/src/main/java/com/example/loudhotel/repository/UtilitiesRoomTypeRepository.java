package com.example.loudhotel.repository;

import com.example.loudhotel.entity.UtilitiesRoomType;
import com.example.loudhotel.entity.UtilitiesRoomTypeId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UtilitiesRoomTypeRepository
        extends JpaRepository<UtilitiesRoomType, UtilitiesRoomTypeId> {

    @Query("""
    SELECT urt FROM UtilitiesRoomType urt
    WHERE urt.roomType.typeId = :typeId
    AND urt.roomType.isDeleted = false
    AND (:keyword IS NULL OR LOWER(urt.utilities.utilitiesName) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    Page<UtilitiesRoomType> findByTypeId(
            @Param("typeId") Long typeId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
    SELECT urt FROM UtilitiesRoomType urt
    WHERE urt.roomType.typeId = :typeId
    AND urt.roomType.hotel.manager.userId = :managerId
    AND (:keyword IS NULL OR LOWER(urt.utilities.utilitiesName) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    Page<UtilitiesRoomType> findByTypeIdAndManager(
            @Param("typeId") Long typeId,
            @Param("managerId") Long managerId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 🔥 SUMMARY
    @Query("""
SELECT rt.typeId AS typeId,
       rt.typeName AS typeName,
       COUNT(urt.utilities.utilitiesId) AS utilityCount,
       rt.hotel.hotelName AS hotelName,
       rt.hotel.hotelId AS hotelId
FROM RoomType rt
LEFT JOIN UtilitiesRoomType urt
       ON urt.roomType.typeId = rt.typeId
WHERE rt.isDeleted = false
AND rt.hotel.isDeleted = false
AND (:keyword IS NULL OR :keyword = '' OR
     LOWER(rt.typeName) LIKE LOWER(CONCAT('%', :keyword, '%')))
GROUP BY rt.typeId, rt.typeName, rt.hotel.hotelName, rt.hotel.hotelId
""")
    Page<Object[]> getSummaryRaw(String keyword, Pageable pageable);

    @Query("""
    SELECT rt.typeId,
           rt.typeName,
           COUNT(urt.utilities.utilitiesId),
           rt.hotel.hotelName,
           rt.hotel.hotelId
    FROM RoomType rt
    LEFT JOIN UtilitiesRoomType urt
           ON urt.roomType.typeId = rt.typeId
    WHERE rt.isDeleted = false
    AND rt.hotel.manager.userId = :managerId
    AND (
        :keyword IS NULL OR :keyword = '' OR
        LOWER(rt.typeName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    )
    GROUP BY rt.typeId, rt.typeName, rt.hotel.hotelName, rt.hotel.hotelId
    """)
    Page<Object[]> getSummaryByManagerRaw(
            @Param("managerId") Long managerId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
SELECT rt.typeId,
       rt.typeName,
       COUNT(urt.utilities.utilitiesId),
       rt.hotel.hotelName,
       rt.hotel.hotelId
FROM RoomType rt
LEFT JOIN UtilitiesRoomType urt
       ON urt.roomType.typeId = rt.typeId
WHERE rt.isDeleted = false
AND rt.hotel.isDeleted = false
AND rt.hotel.hotelId = :hotelId
GROUP BY rt.typeId, rt.typeName, rt.hotel.hotelName, rt.hotel.hotelId
""")
    List<Object[]> getSummaryByHotelId(
            @Param("hotelId") Long hotelId
    );
}