package com.example.loudhotel.service.impl;

import com.example.loudhotel.dto.response.*;
import com.example.loudhotel.entity.*;
import com.example.loudhotel.exception.*;
import com.example.loudhotel.repository.*;
import com.example.loudhotel.service.UtilitiesRoomTypeService;
import com.example.loudhotel.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UtilitiesRoomTypeServiceImpl implements UtilitiesRoomTypeService {

    private final UtilitiesRoomTypeRepository repository;
    private final RoomTypeRepository roomTypeRepository;
    private final UtilitiesRepository utilitiesRepository;

    @Override
    public void assignUtilityToRoomType(Long typeId, Long utilityId) {

        RoomType rt = roomTypeRepository.findById(typeId)
                .orElseThrow(() -> new ResourceNotFoundException("RoomType không tồn tại"));

        boolean isAdmin = SecurityUtil.hasRole("ADMIN");

        if (!isAdmin) {
            Long userId = SecurityUtil.getCurrentUserId();
            if (!rt.getHotel().getManager().getUserId().equals(userId)) {
                throw new BadRequestException("Không có quyền");
            }
        }

        Utilities utility = utilitiesRepository.findById(utilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Utility không tồn tại"));

        UtilitiesRoomTypeId id = new UtilitiesRoomTypeId(typeId, utilityId);

        if (repository.existsById(id)) {
            throw new BadRequestException("Đã tồn tại");
        }

        repository.save(UtilitiesRoomType.builder()
                .id(id)
                .roomType(rt)
                .utilities(utility)
                .build());
    }

    @Override
    public void removeUtilityFromRoomType(Long typeId, Long utilityId) {

        UtilitiesRoomTypeId id = new UtilitiesRoomTypeId(typeId, utilityId);

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Không tồn tại");
        }

        repository.deleteById(id);
    }

    @Override
    public Page<UtilitiesResponse> getUtilitiesByRoomType(Long typeId, String keyword, Pageable pageable) {

        boolean isAdmin = SecurityUtil.hasRole("ADMIN");

        if (!isAdmin) {
            Long userId = SecurityUtil.getCurrentUserId();

            return repository.findByTypeIdAndManager(typeId, userId, keyword, pageable)
                    .map(urt -> UtilitiesResponse.builder()
                            .id(urt.getUtilities().getUtilitiesId())
                            .name(urt.getUtilities().getUtilitiesName())
                            .build());
        }

        return repository.findByTypeId(typeId, keyword, pageable)
                .map(urt -> UtilitiesResponse.builder()
                        .id(urt.getUtilities().getUtilitiesId())
                        .name(urt.getUtilities().getUtilitiesName())
                        .build());
    }

    @Override
    public Page<RoomTypeUtilitySummaryResponse> getSummary(String keyword, Pageable pageable) {

        return repository.getSummaryRaw(keyword, pageable)
                .map(r -> new RoomTypeUtilitySummaryResponse(
                        ((Number) r[0]).longValue(),
                        (String) r[1],
                        ((Number) r[2]).longValue(),
                        (String) r[3],
                        ((Number) r[4]).longValue()
                ));
    }

    @Override
    public Page<RoomTypeUtilitySummaryResponse> getSummaryByManager(
            Long managerId,
            String keyword,
            Pageable pageable
    ) {

        Page<Object[]> page = repository.getSummaryByManagerRaw(managerId, keyword, Pageable.unpaged());

        List<RoomTypeUtilitySummaryResponse> list = page.stream()
                .map(r -> new RoomTypeUtilitySummaryResponse(
                        ((Number) r[0]).longValue(),
                        (String) r[1],
                        ((Number) r[2]).longValue(),
                        (String) r[3],
                        ((Number) r[4]).longValue()
                ))
                .toList();

        // 🔥 SORT TAY
        if (pageable.getSort().isSorted()) {
            Sort.Order order = pageable.getSort().iterator().next();

            if (order.getProperty().equals("utilityCount")) {
                list = list.stream()
                        .sorted((a, b) -> order.isAscending()
                                ? Long.compare(a.getUtilityCount(), b.getUtilityCount())
                                : Long.compare(b.getUtilityCount(), a.getUtilityCount()))
                        .toList();
            }

            if (order.getProperty().equals("typeName")) {
                list = list.stream()
                        .sorted((a, b) -> order.isAscending()
                                ? a.getTypeName().compareTo(b.getTypeName())
                                : b.getTypeName().compareTo(a.getTypeName()))
                        .toList();
            }
        }

        //  PAGINATION TAY
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());

        return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }

    @Override
    public List<RoomTypeUtilitySummaryResponse> getSummaryByHotelId(Long hotelId) {

        return repository.getSummaryByHotelId(hotelId)
                .stream()
                .map(r -> new RoomTypeUtilitySummaryResponse(
                        ((Number) r[0]).longValue(),
                        (String) r[1],
                        ((Number) r[2]).longValue(),
                        (String) r[3],
                        ((Number) r[4]).longValue()
                ))
                .toList();
    }

}