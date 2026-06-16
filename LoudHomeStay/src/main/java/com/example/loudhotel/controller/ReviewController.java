package com.example.loudhotel.controller;

import com.example.loudhotel.dto.request.ReviewRequest;
import com.example.loudhotel.dto.response.ReviewResponse;
import com.example.loudhotel.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/manager")
    @PreAuthorize("hasRole('MANAGER')")
    public Page<ReviewResponse> getMyHotelReviews(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double rate,
            @RequestParam(required = false) Double minRate,
            @RequestParam(required = false) Double maxRate,
            @RequestParam(required = false) String hotelStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        return reviewService.getReviewsByManager(
                keyword, rate, minRate, maxRate, hotelStatus,
                page, size, sortBy, direction
        );
    }

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<?> getReviewsByHotel(@PathVariable Long hotelId) {
        return ResponseEntity.ok(reviewService.getReviewsByHotel(hotelId));
    }

    @PutMapping("/{id}")
    public ReviewResponse updateReview(
            @PathVariable Long id,
            @RequestBody ReviewRequest request
    ) {
        return reviewService.updateReview(id, request);
    }

    @PostMapping
    public ReviewResponse create(
            @jakarta.validation.Valid @RequestBody ReviewRequest request
    ) {
        return reviewService.createReview(request);
    }

    @GetMapping
    public Page<ReviewResponse> getReviews(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double rate,
            @RequestParam(required = false) Double minRate,
            @RequestParam(required = false) Double maxRate,
            @RequestParam(required = false) String hotelStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        return reviewService.getReviews(keyword, rate, minRate, maxRate, hotelStatus, page, size, sortBy, direction);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok("Deleted");
    }

}

