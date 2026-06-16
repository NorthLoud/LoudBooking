package com.example.loudhotel.service.impl;

import com.example.loudhotel.dto.request.VoucherRequest;
import com.example.loudhotel.dto.response.VoucherResponse;
import com.example.loudhotel.entity.Hotel;
import com.example.loudhotel.entity.User;
import com.example.loudhotel.entity.Voucher;
import com.example.loudhotel.exception.ResourceNotFoundException;
import com.example.loudhotel.repository.HotelRepository;
import com.example.loudhotel.repository.UserRepository;
import com.example.loudhotel.repository.VoucherRepository;
import com.example.loudhotel.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;

    private VoucherResponse toResponse(Voucher v) {
        return VoucherResponse.builder()
                .voucherId(v.getVoucherId())
                .voucherCode(v.getVoucherCode())
                .title(v.getTitle())
                .description(v.getDescription())
                .discountType(v.getDiscountType())
                .discountValue(v.getDiscountValue())
                .minBillAmount(v.getMinBillAmount())
                .maxDiscountAmount(v.getMaxDiscountAmount())
                .quantity(v.getQuantity())
                .usedCount(v.getUsedCount())
                .startDate(v.getStartDate())
                .endDate(v.getEndDate())
                .status(v.getStatus())
                .hotelIds(v.getHotels() != null ? v.getHotels().stream().map(Hotel::getHotelId).toList() : null)
                .build();
    }

    @Override
    public List<VoucherResponse> getAllVouchers() {
        return voucherRepository.findAll().stream()
                .filter(v -> v.getIsDeleted() == null || !v.getIsDeleted())
                .sorted((v1, v2) -> Long.compare(v2.getVoucherId(), v1.getVoucherId()))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public VoucherResponse getVoucherById(Long id) {
        Voucher v = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
        return toResponse(v);
    }

    @Override
    public VoucherResponse createVoucher(VoucherRequest request) {

        Voucher.VoucherBuilder builder = Voucher.builder()
                .voucherCode(request.getVoucherCode())
                .title(request.getTitle())
                .description(request.getDescription())
                .discountType(Voucher.DiscountType.valueOf(request.getDiscountType()))
                .discountValue(request.getDiscountValue())
                .minBillAmount(request.getMinBillAmount())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .quantity(request.getQuantity())
                .usedCount(0)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(Voucher.VoucherStatus.valueOf(request.getStatus()))
                .isDeleted(false);

        if (request.getHotelIds() != null && !request.getHotelIds().isEmpty()) {
            List<Hotel> hotels = hotelRepository.findAllById(request.getHotelIds());
            builder.hotels(hotels);
        }

        Voucher voucher = builder.build();

        return toResponse(voucherRepository.save(voucher));
    }

    @Override
    public VoucherResponse updateVoucher(Long id, VoucherRequest request) {
        Voucher v = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
        v.setVoucherCode(request.getVoucherCode());
        v.setTitle(request.getTitle());
        v.setDescription(request.getDescription());
        v.setDiscountType(Voucher.DiscountType.valueOf(request.getDiscountType()));
        v.setDiscountValue(request.getDiscountValue());
        v.setMinBillAmount(request.getMinBillAmount());
        v.setMaxDiscountAmount(request.getMaxDiscountAmount());
        v.setQuantity(request.getQuantity());
        v.setStartDate(request.getStartDate());
        v.setEndDate(request.getEndDate());
        v.setStatus(Voucher.VoucherStatus.valueOf(request.getStatus()));

        if (request.getHotelIds() != null && !request.getHotelIds().isEmpty()) {
            List<Hotel> hotels = hotelRepository.findAllById(request.getHotelIds());
            v.setHotels(hotels);
        } else {
            v.setHotels(null);
        }

        return toResponse(voucherRepository.save(v));
    }

    @Override
    public void deleteVoucher(Long id) {
        Voucher v = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
        v.setIsDeleted(true);
        voucherRepository.save(v);
    }

    @Override
    public List<VoucherResponse> getPublicVouchers(Long hotelId) {
        return voucherRepository.findAll().stream()
                .filter(v -> v.getIsDeleted() == null || !v.getIsDeleted())
                .filter(v -> v.getStatus() == Voucher.VoucherStatus.ACTIVE)
                .filter(v -> {
                    if (hotelId == null) return true;
                    return v.getHotels() == null || v.getHotels().isEmpty() || 
                           v.getHotels().stream().anyMatch(h -> h.getHotelId().equals(hotelId));
                })
                .map(this::toResponse)
                .toList();
    }

    @Scheduled(fixedRate = 60000) // check every 1 minute
    public void updateExpiredVouchers() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        List<Voucher> vouchers = voucherRepository.findAllByStatusAndEndDateBefore(Voucher.VoucherStatus.ACTIVE, now);
        if (!vouchers.isEmpty()) {
            for (Voucher v : vouchers) {
                v.setStatus(Voucher.VoucherStatus.EXPIRED);
            }
            voucherRepository.saveAll(vouchers);
        }
    }

    @Override
    public List<VoucherResponse> getManagerVouchers() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User manager = userRepository
                .findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        Hotel myHotel = hotelRepository
                .findByManager_UserId(manager.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        return voucherRepository.findAll()
                .stream()
                .filter(v -> v.getIsDeleted() == null || !v.getIsDeleted())
                .filter(v -> {

                    // voucher global
                    if (v.getHotels() == null || v.getHotels().isEmpty()) {
                        return true;
                    }

                    // voucher thuộc hotel manager
                    return v.getHotels()
                            .stream()
                            .anyMatch(h -> h.getHotelId().equals(myHotel.getHotelId()));
                })
                .map(this::toResponse)
                .toList();
    }
}
