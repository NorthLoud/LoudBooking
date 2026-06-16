package com.example.loudhotel.service.impl;

import com.example.loudhotel.dto.request.ReviewRequest;
import com.example.loudhotel.dto.response.ReviewResponse;
import com.example.loudhotel.entity.*;
import com.example.loudhotel.exception.ResourceNotFoundException;
import com.example.loudhotel.repository.BillRepository;
import com.example.loudhotel.repository.HotelRepository;
import com.example.loudhotel.repository.ReviewRepository;
import com.example.loudhotel.repository.UserRepository;
import com.example.loudhotel.service.ReviewService;
import com.example.loudhotel.utils.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final BillRepository billRepository;

    private ReviewResponse mapToResponse(Review review) {
        Long currentUserId = null;
        try {
            currentUserId = SecurityUtil.getCurrentUserId();
        } catch (Exception ignored) {}

        String roomTypeNames = "";
        if (review.getBill() != null && review.getBill().getBillDetails() != null) {
            roomTypeNames = review.getBill().getBillDetails().stream()
                .map(d -> d.getRoomType().getTypeName())
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        }

        Integer nights = 0;
        if (review.getBill() != null && review.getBill().getBillDetails() != null) {
            nights = review.getBill().getBillDetails().stream()
                .map(BillDetail::getNights)
                .filter(n -> n != null)
                .findFirst()
                .orElse(0);
        }

        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .username(review.getUser() != null ? review.getUser().getUsername() : null)
                .hotelName(review.getBill().getHotel().getHotelName())
                .hotelId(review.getBill().getHotel().getHotelId())
                .billId(review.getBill().getBillId())
                .rate(review.getRate())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt() != null ? review.getUpdatedAt() : review.getCreatedAt())
                .isMine(
                        currentUserId != null &&
                                review.getUser() != null &&
                                review.getUser().getUserId().equals(currentUserId)
                )
                .roomTypeNames(roomTypeNames)
                .nights(nights)
                .status("ACTIVE")
                .build();
    }

    private void updateAvg(Long hotelId) {
        List<Review> reviews = reviewRepository.findByHotel_HotelId(hotelId);

        double avg = reviews.stream()
                .mapToDouble(Review::getRate)
                .average()
                .orElse(0);

        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow();
        hotel.setAverageRating(avg);
        hotelRepository.save(hotel);
    }

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {

        Long userId = SecurityUtil.getCurrentUserId();

        Bill bill = billRepository.findById(request.getBillId())
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        // 1. Kiểm tra chính chủ
        if (!bill.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền đánh giá đơn đặt này");
        }

        // 2. Kiểm tra đã ở xong chưa (actualCheckOutTime != null)
        if (bill.getActualCheckOutTime() == null) {
            throw new RuntimeException("Bạn chỉ có thể đánh giá sau khi đã trả phòng");
        }

        // 3. Kiểm tra đã đánh giá chưa
        reviewRepository.findByUser_UserIdAndBill_BillId(userId, request.getBillId())
                .ifPresent(r -> {
                    throw new RuntimeException("Bạn đã đánh giá đơn đặt này rồi");
                });

        User user = userRepository.findById(userId).orElseThrow();

        Review review = Review.builder()
                .user(user)
                .bill(bill)
                .rate(request.getRate())
                .comment(request.getComment())
                .build();

        reviewRepository.save(review);

        updateAvg(bill.getHotel().getHotelId());

        return mapToResponse(review);
    }

    @Override
    public List<ReviewResponse> getReviewsByHotel(Long hotelId) {
        return reviewRepository.findByHotel_HotelId(hotelId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewRequest request) {

        Long userId = SecurityUtil.getCurrentUserId();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Không có quyền sửa");
        }

        review.setRate(request.getRate());
        review.setComment(request.getComment());

        reviewRepository.save(review);

        updateAvg(review.getBill().getHotel().getHotelId());

        return mapToResponse(review);
    }

    @Override
    public Page<ReviewResponse> getReviews(String keyword,
                                           Double rate,
                                           Double minRate,
                                           Double maxRate,
                                           String hotelStatus,
                                           int page,
                                           int size,
                                           String sortBy,
                                           String direction) {

        Long userId = null;
        String role = null;

        try {
            userId = SecurityUtil.getCurrentUserId();
            role = SecurityContextHolder.getContext().getAuthentication()
                    .getAuthorities().iterator().next().getAuthority();
        } catch (Exception ignored) {}

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Hotel.HotelStatus statusEnum = null;
        if (hotelStatus != null && !hotelStatus.isEmpty()) {
            try {
                statusEnum = Hotel.HotelStatus.valueOf(hotelStatus.toUpperCase());
            } catch (Exception ignored) {}
        }

        Page<Review> reviewPage;

        if ("ROLE_ADMIN".equals(role)) {
            reviewPage = reviewRepository.searchReviews(
                    keyword, rate, minRate, maxRate, statusEnum, pageable
            );
        } else {
            reviewPage = reviewRepository.searchReviewsByManager(
                    userId, keyword, rate, minRate, maxRate, statusEnum, pageable
            );
        }

        return reviewPage.map(this::mapToResponse);
    }

    @Override
    public Page<ReviewResponse> getReviewsByManager(String keyword,
                                                  Double rate,
                                                  Double minRate,
                                                  Double maxRate,
                                                  String hotelStatus,
                                                  int page,
                                                  int size,
                                                  String sortBy,
                                                  String direction) {

        Long managerId = SecurityUtil.getCurrentUserId();

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Hotel.HotelStatus statusEnum = null;
        if (hotelStatus != null && !hotelStatus.isEmpty()) {
            try {
                statusEnum = Hotel.HotelStatus.valueOf(hotelStatus.toUpperCase());
            } catch (Exception e) {}
        }

        Page<Review> reviewPage = reviewRepository.searchReviewsByManager(
                managerId, keyword, rate, minRate, maxRate, statusEnum, pageable
        );

        return reviewPage.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        Long hotelId = review.getBill().getHotel().getHotelId();
        reviewRepository.delete(review);
        updateAvg(hotelId);
    }
}
