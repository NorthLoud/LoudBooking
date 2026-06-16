package com.example.loudhotel.service.impl;

import com.example.loudhotel.dto.response.HotelUtilitySummaryResponse;
import com.example.loudhotel.dto.response.UtilitiesResponse;
import com.example.loudhotel.entity.Hotel;
import com.example.loudhotel.entity.Utilities;
import com.example.loudhotel.entity.UtilitiesHotel;
import com.example.loudhotel.entity.UtilitiesHotelId;
import com.example.loudhotel.exception.BadRequestException;
import com.example.loudhotel.exception.ResourceNotFoundException;
import com.example.loudhotel.repository.HotelRepository;
import com.example.loudhotel.repository.UtilitiesHotelRepository;
import com.example.loudhotel.repository.UtilitiesRepository;
import com.example.loudhotel.service.UtilitiesHotelService;
import com.example.loudhotel.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UtilitiesHotelServiceImpl implements UtilitiesHotelService {

    private final UtilitiesHotelRepository utilitiesHotelRepository;
    private final HotelRepository hotelRepository;
    private final UtilitiesRepository utilitiesRepository;

    private void validateHotelActive(Hotel hotel) {
        if (hotel.getHotelStatus().name().equals("INACTIVE")) {
            throw new BadRequestException("Khách sạn đã ngừng hoạt động!");
        }
    }

    @Override
    public void assignUtilityToHotel(Long hotelId, Long utilityId) {

        Hotel hotel = hotelRepository.findByHotelIdAndIsDeletedFalse(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Khách sạn không tồn tại!"));

        validateHotelActive(hotel);

        boolean isAdmin = SecurityUtil.hasRole("ADMIN");

        if(!isAdmin){
            Long currentUserId = SecurityUtil.getCurrentUserId();

            if(!hotel.getManager().getUserId().equals(currentUserId)){
                throw new BadRequestException("Bạn không sở hữu khách sạn này");
            }
        }

        Utilities utility = utilitiesRepository.findById(utilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Tiện ích không tồn tại!"));

        UtilitiesHotelId id = new UtilitiesHotelId(hotelId, utilityId);

        if (utilitiesHotelRepository.existsById(id)) {
            throw new BadRequestException("Tiện ích đã được gán!");
        }

        UtilitiesHotel utilitiesHotel = UtilitiesHotel.builder()
                .id(id)
                .hotel(hotel)
                .utilities(utility)
                .build();

        utilitiesHotelRepository.save(utilitiesHotel);
    }

    @Override
    public void removeUtilityFromHotel(Long hotelId, Long utilityId) {

        Hotel hotel = hotelRepository.findByHotelIdAndIsDeletedFalse(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Khách sạn không tồn tại!"));

        boolean isAdmin = SecurityUtil.hasRole("ADMIN");

        if(!isAdmin){
            Long currentUserId = SecurityUtil.getCurrentUserId();

            if(!hotel.getManager().getUserId().equals(currentUserId)){
                throw new BadRequestException("Bạn không sở hữu khách sạn này");
            }
        }

        UtilitiesHotelId id = new UtilitiesHotelId(hotelId, utilityId);

        if (!utilitiesHotelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Quan hệ không tồn tại!");
        }

        utilitiesHotelRepository.deleteById(id);
    }

    public List<UtilitiesResponse> getUtilitiesByHotelPublic(Long hotelId) {

        Hotel hotel = hotelRepository.findByHotelIdAndIsDeletedFalse(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Khách sạn không tồn tại"));

        // chỉ cần check hotel còn hoạt động
        if (hotel.getHotelStatus() == Hotel.HotelStatus.INACTIVE) {
            return List.of();
        }

        return utilitiesHotelRepository
                .findByHotelActive(hotelId)
                .stream()
                .map(uh -> UtilitiesResponse.builder()
                        .id(uh.getUtilities().getUtilitiesId())
                        .name(uh.getUtilities().getUtilitiesName())
                        .hotelId(hotelId)
                        .hotelName(hotel.getHotelName())
                        .hotelStatus(hotel.getHotelStatus().name())
                        .build())
                .toList();
    }

    @Override
    public List<UtilitiesResponse> getUtilitiesByHotel(Long hotelId) {

        Hotel hotel = hotelRepository.findByHotelIdAndIsDeletedFalse(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Khách sạn không tồn tại"));

        boolean isAdmin = SecurityUtil.hasRole("ADMIN");

        if(!isAdmin){

            Long currentUserId = SecurityUtil.getCurrentUserId();

            if(!hotel.getManager().getUserId().equals(currentUserId)){
                throw new BadRequestException("Bạn không sở hữu khách sạn này");
            }
        }

        return utilitiesHotelRepository
                .findByHotelActive(hotelId)
                .stream()
                .map(uh -> UtilitiesResponse.builder()
                        .id(uh.getUtilities().getUtilitiesId())
                        .name(uh.getUtilities().getUtilitiesName())
                        .hotelId(hotelId)
                        .hotelName(hotel.getHotelName())
                        .hotelStatus(hotel.getHotelStatus().name())
                        .build())
                .toList();
    }

    @Override
    public Page<UtilitiesResponse> getAll(String keyword,
                                          String hotelStatus,
                                          int page,
                                          String sortBy,
                                          String sortDir) {

        Pageable pageable = PageRequest.of(page, 5);

        Hotel.HotelStatus statusEnum = null;

        if (hotelStatus != null && !hotelStatus.isEmpty()) {
            try {
                statusEnum = Hotel.HotelStatus.valueOf(hotelStatus.toUpperCase());
            } catch (Exception ignored) {}
        }

        Page<UtilitiesHotel> result = utilitiesHotelRepository.searchAll(
                keyword,
                statusEnum,
                pageable
        );

        return result.map(uh -> UtilitiesResponse.builder()
                .id(uh.getUtilities().getUtilitiesId())
                .name(uh.getUtilities().getUtilitiesName())
                .hotelId(uh.getHotel().getHotelId())
                .hotelName(uh.getHotel().getHotelName())
                .hotelStatus(uh.getHotel().getHotelStatus().name())
                .build());
    }

    @Override
    public Page<HotelUtilitySummaryResponse> getSummary(
            String keyword,
            String hotelStatus,
            int page,
            String sortBy,
            String sortDir
    ) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, 5);

        Hotel.HotelStatus statusEnum = null;

        if (hotelStatus != null && !hotelStatus.isBlank()) {
            statusEnum = Hotel.HotelStatus.valueOf(hotelStatus);
        }


        Page<Object[]> result = utilitiesHotelRepository.countSummary(
                keyword,
                statusEnum,
                sortBy,
                sortDir,
                pageable
        );

        return result.map(r -> HotelUtilitySummaryResponse.builder()
                .hotelId((Long) r[0])
                .hotelName((String) r[1])
                .hotelStatus(((Hotel.HotelStatus) r[2]).name())
                .utilityCount(((Long) r[3]).intValue())
                .build()
        );
    }

    @Override
    public Page<HotelUtilitySummaryResponse> getSummaryByManager(
            Long managerId,
            String keyword,
            String hotelStatus,
            int page,
            String sortBy,
            String sortDir
    ) {

        Pageable pageable = PageRequest.of(page, 10);

        Hotel.HotelStatus statusEnum = null;

        if (hotelStatus != null && !hotelStatus.isBlank()) {
            statusEnum = Hotel.HotelStatus.valueOf(hotelStatus);
        }

        Page<Object[]> result = utilitiesHotelRepository.countSummaryByManager(
                managerId,
                keyword,
                statusEnum,
                sortBy,
                sortDir,
                pageable
        );

        return result.map(r -> HotelUtilitySummaryResponse.builder()
                .hotelId((Long) r[0])
                .hotelName((String) r[1])
                .hotelStatus(((Hotel.HotelStatus) r[2]).name())
                .utilityCount(((Long) r[3]).intValue())
                .build()
        );
    }

}
