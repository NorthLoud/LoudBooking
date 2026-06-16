package com.example.loudhotel.service.impl;

import com.example.loudhotel.dto.request.UtilitiesRequest;
import com.example.loudhotel.dto.response.UtilitiesResponse;
import com.example.loudhotel.entity.Utilities;
import com.example.loudhotel.enums.UtilityType;
import com.example.loudhotel.exception.BadRequestException;
import com.example.loudhotel.exception.ResourceNotFoundException;
import com.example.loudhotel.repository.UtilitiesRepository;
import com.example.loudhotel.service.UtilitiesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UtilitiesServiceImpl implements UtilitiesService {

    private final UtilitiesRepository utilitiesRepository;

    @Override
    public Page<UtilitiesResponse> getAllUtilities(String keyword, UtilityType utilityType, Pageable pageable) {

        Page<Utilities> page;

        if (keyword != null && !keyword.isBlank()) {
            if (utilityType != null) {
                page = utilitiesRepository.findByUtilitiesNameContainingIgnoreCaseAndUtilityType(keyword, utilityType, pageable);
            } else {
                page = utilitiesRepository.findByUtilitiesNameContainingIgnoreCase(keyword, pageable);
            }
        } else {
            if (utilityType != null) {
                page = utilitiesRepository.findByUtilityType(utilityType, pageable);
            } else {
                page = utilitiesRepository.findAll(pageable);
            }
        }

        return page.map(u -> UtilitiesResponse.builder()
                .id(u.getUtilitiesId())
                .name(u.getUtilitiesName())
                .utilityType(u.getUtilityType())
                .build());
    }

    @Override
    public void create(UtilitiesRequest request) {

        if (utilitiesRepository.existsByUtilitiesName(request.getUtilitiesName())) {
            throw new BadRequestException("Tiện ích đã tồn tại");
        }

        utilitiesRepository.save(
                Utilities.builder()
                        .utilitiesName(request.getUtilitiesName())
                        .utilityType(request.getUtilityType())
                        .build()
        );
    }

    @Override
    public void update(Long id, UtilitiesRequest request) {

        Utilities utilities = utilitiesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tiện ích không tồn tại"));

        utilities.setUtilitiesName(request.getUtilitiesName());
        utilities.setUtilityType(request.getUtilityType());
        utilitiesRepository.save(utilities);
    }

    @Override
    public void deleteById(Long id) {

        Utilities utilities = utilitiesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tiện ích không tồn tại"));

        utilitiesRepository.delete(utilities);
    }
}
