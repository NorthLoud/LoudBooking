package com.example.loudhotel.repository;

import com.example.loudhotel.entity.Hotel;
import com.example.loudhotel.entity.Room;
import com.example.loudhotel.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByRoomType_HotelAndRoomNumberAndIsDeletedFalse(Hotel hotel, Integer roomNumber);

    List<Room> findByIsDeletedFalse();

    List<Room> findByRoomType_HotelAndIsDeletedFalse(Hotel hotel);

    long countByRoomType_HotelAndIsDeletedFalse(Hotel hotel);

    @Query("""
            SELECT r FROM Room r
            WHERE r.isDeleted = false
            AND r.roomType.hotel.isDeleted = false
            """)
    List<Room> findAllActiveRooms();

    @Query("""
            SELECT r FROM Room r
            WHERE r.roomType.hotel = :hotel
            AND r.isDeleted = false
            AND r.roomType.hotel.isDeleted = false
            """)
    List<Room> findActiveByHotel(Hotel hotel);

    @Query("""
            SELECT r FROM Room r
            WHERE r.isDeleted = false
            AND r.roomType.hotel.isDeleted = false
            AND r.roomType.hotel.manager.userId = :managerId
            """)
    List<Room> findByManagerId(Long managerId);

    @Query("""
            SELECT r FROM Room r
            WHERE r.isDeleted = false
            AND r.roomType.hotel.isDeleted = false
            AND (:keyword IS NULL OR CAST(r.roomNumber AS string) LIKE %:keyword% OR LOWER(r.roomType.typeName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(r.roomType.hotel.hotelName) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:hotelId IS NULL OR r.roomType.hotel.hotelId = :hotelId)
            AND (:typeId IS NULL OR r.roomType.typeId = :typeId)
            AND (:roomStatus IS NULL OR r.roomStatus = :roomStatus)
            """)
    Page<Room> findAllActiveRooms(
            @Param("keyword") String keyword,
            @Param("hotelId") Long hotelId,
            @Param("typeId") Long typeId,
            @Param("roomStatus") Room.RoomStatus roomStatus,
            Pageable pageable);

    @Query("""
            SELECT r FROM Room r
            WHERE r.isDeleted = false
            AND r.roomType.hotel.isDeleted = false
            AND r.roomType.hotel.manager.userId = :managerId
            AND (:keyword IS NULL OR CAST(r.roomNumber AS string) LIKE %:keyword% OR LOWER(r.roomType.typeName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(r.roomType.hotel.hotelName) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:typeId IS NULL OR r.roomType.typeId = :typeId)
            AND (:roomStatus IS NULL OR r.roomStatus = :roomStatus)
            """)
    Page<Room> findByManagerId(
            @Param("managerId") Long managerId, 
            @Param("keyword") String keyword, 
            @Param("typeId") Long typeId,
            @Param("roomStatus") Room.RoomStatus roomStatus,
            Pageable pageable);

    @Query("""
            SELECT r
            FROM Room r
            WHERE r.roomType.typeId = :typeId
            AND r.isDeleted = false
            AND r.roomStatus NOT IN ('MAINTENANCE', 'INACTIVE')
            AND r.roomType.hotel.isDeleted = false

            AND r.roomId NOT IN (

                SELECT ra.room.roomId
                FROM RoomAssignment ra
                JOIN ra.billDetail bd
                JOIN bd.bill b

                WHERE b.billStatus IN ('PENDING', 'PAID')

                AND (
                    b.checkInDate < :checkOut
                    AND b.checkOutDate > :checkIn
                )
            )
            """)
    List<Room> findAvailableRoomsByType(
            @Param("typeId") Long typeId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    @Query("""
            SELECT COUNT(bd)
            FROM BillDetail bd
            JOIN bd.bill b
            WHERE bd.roomType.typeId = :typeId

            AND b.billStatus IN ('PENDING', 'PAID')

            AND (

                b.checkInDate < :checkOut

                AND

                (
                    CASE
                        WHEN b.actualCheckOutTime IS NOT NULL
                        THEN DATE(b.actualCheckOutTime)
                        ELSE b.checkOutDate
                    END
                ) > :checkIn
            )
            """)
    int countBookedRooms(
            @Param("typeId") Long typeId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    boolean existsByRoomType_TypeIdAndIsDeletedFalse(Long typeId);

    int countByRoomType_TypeId(Long typeId);

    @Query("""
                SELECT COUNT(r) FROM Room r
                WHERE r.roomType.typeId = :typeId
                AND r.isDeleted = false
                AND r.roomStatus NOT IN ('MAINTENANCE', 'INACTIVE')
            """)
    int countTotalRoomsForBooking(@Param("typeId") Long typeId);

    @Query("""
            SELECT DISTINCT h
            FROM Hotel h

            WHERE h.isDeleted = false
            AND h.hotelStatus = 'ACTIVE'

            AND (
                :keyword IS NULL
                OR :keyword = ''
                OR LOWER(h.hotelName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(h.address) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )

            AND (
                (
                    SELECT COUNT(r)
                    FROM Room r

                    WHERE r.roomType.hotel.hotelId = h.hotelId
                    AND r.isDeleted = false
                    AND r.roomStatus NOT IN ('MAINTENANCE', 'INACTIVE')
                )
                -
                (
                    SELECT COUNT(bd)
                    FROM BillDetail bd
                    JOIN bd.bill b
                    JOIN bd.roomType rt

                    WHERE rt.hotel.hotelId = h.hotelId
                    AND b.billStatus IN ('PENDING', 'PAID')

                    AND (
                        b.checkInDate < :checkOut
                        AND (
                            CASE
                                WHEN b.actualCheckOutTime IS NOT NULL
                                THEN DATE(b.actualCheckOutTime)
                                ELSE b.checkOutDate
                            END
                        ) > :checkIn
                    )
                )
            ) >= :guestCount
            """)
    List<Hotel> searchAvailableHotels(
            @Param("keyword") String keyword,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("guestCount") Integer guestCount);

    @Query("""
            SELECT (
                SELECT COUNT(r)
                FROM Room r
                WHERE r.roomType.hotel.hotelId = :hotelId
                AND r.isDeleted = false
                AND r.roomStatus NOT IN ('MAINTENANCE', 'INACTIVE')
            )
            -
            (
                SELECT COUNT(bd)
                FROM BillDetail bd
                JOIN bd.bill b
                JOIN bd.roomType rt

                WHERE rt.hotel.hotelId = :hotelId
                AND b.billStatus IN ('PENDING', 'PAID')

                AND (
                    b.checkInDate < :checkOut
                    AND (
                        CASE
                            WHEN b.actualCheckOutTime IS NOT NULL
                            THEN DATE(b.actualCheckOutTime)
                            ELSE b.checkOutDate
                        END
                    ) > :checkIn
                )
            )
            """)
    int countAvailableRooms(
            @Param("hotelId") Long hotelId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);
}
