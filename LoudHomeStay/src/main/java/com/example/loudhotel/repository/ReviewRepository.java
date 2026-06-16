package com.example.loudhotel.repository;

import com.example.loudhotel.entity.Hotel;
import com.example.loudhotel.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.bill.hotel.hotelId = :hotelId")
    List<Review> findByHotel_HotelId(@Param("hotelId") Long hotelId);

    @Query("SELECT r FROM Review r WHERE r.bill.hotel.manager.userId = :managerId")
    List<Review> findAllByManagerId(@Param("managerId") Long managerId);

    Optional<Review> findByUser_UserIdAndBill_BillId(Long userId, Long billId);
    boolean existsByBill_BillId(Long billId);

    @Query("SELECT r FROM Review r WHERE r.bill.hotel.isDeleted = false")
    Page<Review> findAllVisible(Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.reviewId = :reviewId AND r.bill.hotel.manager.userId = :managerId")
    Optional<Review> findByIdAndManagerId(@Param("reviewId") Long reviewId, @Param("managerId") Long managerId);

    @Query("SELECT r FROM Review r WHERE r.bill.hotel.isDeleted = false " +
            "AND (:hotelStatus IS NULL OR r.bill.hotel.hotelStatus = :hotelStatus) " +
            "AND (:keyword IS NULL OR LOWER(r.user.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(r.bill.hotel.hotelName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(r.comment) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:rate IS NULL OR r.rate = :rate) " +
            "AND (:minRate IS NULL OR r.rate >= :minRate) " +
            "AND (:maxRate IS NULL OR r.rate < :maxRate)")
    Page<Review> searchReviews(@Param("keyword") String keyword,
                               @Param("rate") Double rate,
                               @Param("minRate") Double minRate,
                               @Param("maxRate") Double maxRate,
                               @Param("hotelStatus") Hotel.HotelStatus hotelStatus,
                               Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.bill.hotel.isDeleted = false " +
            "AND r.bill.hotel.manager.userId = :managerId " +
            "AND (:hotelStatus IS NULL OR r.bill.hotel.hotelStatus = :hotelStatus) " +
            "AND (:keyword IS NULL OR LOWER(r.user.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(r.bill.hotel.hotelName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(r.comment) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:rate IS NULL OR r.rate = :rate) " +
            "AND (:minRate IS NULL OR r.rate >= :minRate) " +
            "AND (:maxRate IS NULL OR r.rate < :maxRate)")
    Page<Review> searchReviewsByManager(
            @Param("managerId") Long managerId,
            @Param("keyword") String keyword,
            @Param("rate") Double rate,
            @Param("minRate") Double minRate,
            @Param("maxRate") Double maxRate,
            @Param("hotelStatus") Hotel.HotelStatus hotelStatus,
            Pageable pageable
    );


}
