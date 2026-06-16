package com.example.loudhotel.service.impl;

import com.example.loudhotel.dto.request.RoomTypeRequest;
import com.example.loudhotel.dto.response.ImageResponse;
import com.example.loudhotel.dto.response.RoomTypeResponse;
import com.example.loudhotel.dto.response.UtilitiesResponse;
import com.example.loudhotel.entity.Hotel;
import com.example.loudhotel.entity.RoomType;
import com.example.loudhotel.entity.RoomTypeImage;
import com.example.loudhotel.exception.BadRequestException;
import com.example.loudhotel.exception.ResourceNotFoundException;
import com.example.loudhotel.repository.HotelRepository;
import com.example.loudhotel.repository.RoomRepository;
import com.example.loudhotel.repository.RoomTypeRepository;
import com.example.loudhotel.repository.UtilitiesRoomTypeRepository;
import com.example.loudhotel.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final UtilitiesRoomTypeRepository utilitiesRoomTypeRepository;

    private RoomTypeResponse map(RoomType rt) {

        List<ImageResponse> images = List.of();
        String mainImage = null;

        if (rt.getImages() != null && !rt.getImages().isEmpty()) {

            images = rt.getImages().stream().map(img -> {
                ImageResponse r = new ImageResponse();
                r.setImageId(img.getImageId());
                r.setImageUrl(img.getImageUrl());
                r.setMain(Boolean.TRUE.equals(img.getIsMain()));
                return r;
            }).toList();

            mainImage = rt.getImages().stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsMain()))
                    .map(img -> img.getImageUrl())
                    .findFirst()
                    .orElse(images.isEmpty() ? null : images.get(0).getImageUrl());
        }

        List<UtilitiesResponse> utilities =
                utilitiesRoomTypeRepository
                        .findByTypeId(rt.getTypeId(), null, Pageable.unpaged())
                        .getContent()
                        .stream()
                        .map(urt -> UtilitiesResponse.builder()
                                .id(urt.getUtilities().getUtilitiesId())
                                .name(urt.getUtilities().getUtilitiesName())
                                .build())
                        .toList();

        return RoomTypeResponse.builder()
                .typeId(rt.getTypeId())
                .typeName(rt.getTypeName())
                .capacity(rt.getCapacity())
                .price(rt.getPrice())
                .description(rt.getDescription())
                .bedType(rt.getBedType().name())
                .bedCount(rt.getBedCount())
                .area(rt.getArea())
                .hotelId(rt.getHotel().getHotelId())
                .hotelStatus(rt.getHotel().getHotelStatus().name())
                .hotelName(rt.getHotel().getHotelName())
                .createdAt(rt.getCreatedAt())
                .updatedAt(rt.getUpdatedAt())
                .mainImage(mainImage)
                .images(images)
                .utilities(utilities)
                .build();
    }

    @Override
    public List<RoomTypeResponse> getAll() {
        return roomTypeRepository.findByIsDeletedFalse()
                .stream()
                .map(this::map)
                .toList();
    }

    /** Map Vietnamese bed type keyword to English enum name for DB search */
    private String mapBedTypeKeyword(String keyword) {
        if (keyword == null) return null;
        String kw = keyword.toLowerCase().trim();
        if (kw.contains("đơn") || kw.contains("single")) return "SINGLE";
        if (kw.contains("đôi") || kw.contains("double")) return "DOUBLE";
        if (kw.contains("queen")) return "QUEEN";
        if (kw.contains("king")) return "KING";
        return null;
    }

    @Override
    public org.springframework.data.domain.Page<RoomTypeResponse> getAll(String keyword, org.springframework.data.domain.Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            String bedKeyword = mapBedTypeKeyword(keyword);
            return roomTypeRepository.searchAll(keyword, bedKeyword, pageable).map(this::map);
        }
        return roomTypeRepository.findByIsDeletedFalse(pageable).map(this::map);
    }

    @Override
    public RoomTypeResponse create(Long hotelId, RoomTypeRequest request) {

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        boolean exists = roomTypeRepository
                .existsByHotel_HotelIdAndTypeNameAndIsDeletedFalse(
                        hotelId,
                        request.getTypeName()
                );

        if (exists) {
            throw new BadRequestException("Tên loại phòng đã tồn tại trong khách sạn này");
        }

        RoomType roomType = RoomType.builder()
                .typeName(request.getTypeName())
                .capacity(request.getCapacity())
                .price(request.getPrice())
                .description(request.getDescription())
                .bedType(RoomType.BedType.valueOf(request.getBedType()))
                .bedCount(request.getBedCount())
                .area(request.getArea())
                .hotel(hotel)
                .isDeleted(false)
                .build();

        return map(roomTypeRepository.save(roomType));
    }

    @Override
    public RoomTypeResponse update(Long id, RoomTypeRequest request) {

        RoomType rt = roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RoomType not found"));

        boolean exists = roomTypeRepository
                .existsByHotel_HotelIdAndTypeNameAndIsDeletedFalse(
                        rt.getHotel().getHotelId(),
                        request.getTypeName()
                );

        // tránh check chính nó
        if (exists && !rt.getTypeName().equals(request.getTypeName())) {
            throw new BadRequestException("Tên loại phòng đã tồn tại trong khách sạn này");
        }

        rt.setTypeName(request.getTypeName());
        rt.setCapacity(request.getCapacity());
        rt.setPrice(request.getPrice());
        rt.setDescription(request.getDescription());
        rt.setBedType(RoomType.BedType.valueOf(request.getBedType()));
        rt.setBedCount(request.getBedCount());
        rt.setArea(request.getArea());

        return map(roomTypeRepository.save(rt));
    }

    @Override
    public void delete(Long id) {

        RoomType rt = roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RoomType not found"));

        // ❗ CHECK QUAN TRỌNG
        boolean hasRooms = roomRepository
                .existsByRoomType_TypeIdAndIsDeletedFalse(id);

        if (hasRooms) {
            throw new BadRequestException("Không thể xóa loại phòng đang có phòng");
        }

        rt.setIsDeleted(true);
        roomTypeRepository.save(rt);
    }

    @Override
    public List<RoomTypeResponse> getByHotel(Long hotelId) {

        return roomTypeRepository
                .findByHotel_HotelIdAndIsDeletedFalse(hotelId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public RoomTypeResponse getById(Long id) {

        RoomType rt = roomTypeRepository
                .findByTypeIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("RoomType not found"));

        return map(rt);
    }

    @Override
    public List<RoomTypeResponse> getRoomTypesByManager(Long managerId) {

        return roomTypeRepository.findAll()
                .stream()
                .filter(rt -> !rt.getIsDeleted())
                .filter(rt -> rt.getHotel() != null)
                .filter(rt -> rt.getHotel().getManager() != null) // 👈 FIX
                .filter(rt -> rt.getHotel().getManager().getUserId().equals(managerId))
                .map(this::map)
                .toList();
    }

    @Override
    public org.springframework.data.domain.Page<RoomTypeResponse> getRoomTypesByManager(Long managerId, String keyword, org.springframework.data.domain.Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            String bedKeyword = mapBedTypeKeyword(keyword);
            return roomTypeRepository.searchByManagerUserId(managerId, keyword, bedKeyword, pageable).map(this::map);
        }
        return roomTypeRepository.findByManagerUserId(managerId, pageable).map(this::map);
    }
}