package com.example.loudhotel.service.impl;

import com.example.loudhotel.config.FileStorageConfig;
import com.example.loudhotel.dto.response.ImageResponse;
import com.example.loudhotel.entity.*;
import com.example.loudhotel.repository.*;
import com.example.loudhotel.service.ImageService;
import com.example.loudhotel.utils.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final FileUploadUtil fileUploadUtil;
    private final RoomTypeImageRepository roomTypeImageRepository;
    private final HotelImageRepository hotelImageRepository;
    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;

    // Hotel
    @Override
    public List<ImageResponse> getImagesByHotel(Long hotelId) {
        return hotelImageRepository.findByHotel_HotelId(hotelId)
                .stream()
                .map(img -> {
                    ImageResponse r = new ImageResponse();
                    r.setImageId(img.getImageId());
                    r.setImageUrl(img.getImageUrl());
                    r.setMain(Boolean.TRUE.equals(img.getIsMain()));
                    return r;
                })
                .toList();
    }

    @Override
    public List<ImageResponse> uploadHotelImages(Long hotelId,
                                                    List<MultipartFile> files,
                                                    Integer mainIndex) {

        boolean hasNewMain = mainIndex != null && mainIndex >= 0 && mainIndex < files.size();

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow();

        // reset ảnh chính cũ
        if (hasNewMain) {
            hotelImageRepository
                    .findByHotel_HotelId(hotelId)
                    .forEach(img -> {
                        img.setIsMain(false);
                        hotelImageRepository.save(img);
                    });
        }

        List<ImageResponse> result = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {

            String fileName = FileUploadUtil.upload(
                    files.get(i),
                    FileStorageConfig.HOTEL_PATH
            );

            HotelImage image = HotelImage.builder()
                    .hotel(hotel)
                    .imageUrl("/images/hotels/" + fileName)
                    .isMain(hasNewMain && i == mainIndex)
                    .createdAt(LocalDateTime.now())
                    .build();

            hotelImageRepository.save(image);

            ImageResponse res = new ImageResponse();
            res.setImageId(image.getImageId());
            res.setImageUrl(image.getImageUrl());
            res.setMain(image.getIsMain());

            result.add(res);
        }

        return result;
    }

    public void setMainImage(Long imageId) {

        HotelImage image = hotelImageRepository.findById(imageId)
                .orElseThrow();

        Long hotelId = image.getHotel().getHotelId();

        hotelImageRepository
                .findByHotel_HotelId(hotelId)
                .forEach(i -> {
                    i.setIsMain(false);
                    hotelImageRepository.save(i);
                });

        image.setIsMain(true);
        hotelImageRepository.save(image);
    }

    public void deleteImage(Long imageId) {

        HotelImage img = hotelImageRepository.findById(imageId)
                .orElseThrow();

        String fileName = img.getImageUrl()
                .replace("/images/hotels/", "");

        FileUploadUtil.deleteFile(
                FileStorageConfig.HOTEL_PATH + fileName
        );

        hotelImageRepository.delete(img);
    }

    //Room
    @Override
    public List<ImageResponse> getImagesByRoomType(Long typeId) {
        return roomTypeImageRepository.findByRoomType_TypeId(typeId)
                .stream()
                .map(img -> {
                    ImageResponse r = new ImageResponse();
                    r.setImageId(img.getImageId());
                    r.setImageUrl(img.getImageUrl());
                    r.setMain(Boolean.TRUE.equals(img.getIsMain()));
                    return r;
                })
                .toList();
    }

    @Override
    public List<ImageResponse> uploadRoomTypeImages(
            Long typeId,
            List<MultipartFile> files,
            Integer mainIndex) {

        boolean hasNewMain = mainIndex != null && mainIndex >= 0 && mainIndex < files.size();

        RoomType roomType = roomTypeRepository.findById(typeId)
                .orElseThrow();

        if (hasNewMain) {
            roomTypeImageRepository.findByRoomType_TypeId(typeId)
                    .forEach(img -> {
                        img.setIsMain(false);
                        roomTypeImageRepository.save(img);
                    });
        }

        List<ImageResponse> result = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {

            String fileName = FileUploadUtil.upload(
                    files.get(i),
                    FileStorageConfig.ROOM_TYPE_PATH
            );

            RoomTypeImage image = RoomTypeImage.builder()
                    .roomType(roomType)
                    .imageUrl("/images/room-types/" + fileName)
                    .isMain(hasNewMain && i == mainIndex)
                    .createdAt(LocalDateTime.now())
                    .build();

            roomTypeImageRepository.save(image);

            ImageResponse res = new ImageResponse();
            res.setImageId(image.getImageId());
            res.setImageUrl(image.getImageUrl());
            res.setMain(image.getIsMain());

            result.add(res);
        }

        return result;
    }


    public void setMainRoomTypeImage(Long imageId) {

        RoomTypeImage image = roomTypeImageRepository.findById(imageId)
                .orElseThrow();

        Long typeId = image.getRoomType().getTypeId();

        roomTypeImageRepository.findByRoomType_TypeId(typeId)
                .forEach(img -> {
                    img.setIsMain(false);
                    roomTypeImageRepository.save(img);
                });

        image.setIsMain(true);
        roomTypeImageRepository.save(image);
    }

    public void deleteRoomTypeImage(Long imageId) {

        RoomTypeImage img = roomTypeImageRepository.findById(imageId)
                .orElseThrow();

        String fileName = img.getImageUrl()
                .replace("/images/room-types/", "");

        FileUploadUtil.deleteFile(
                FileStorageConfig.ROOM_TYPE_PATH + fileName
        );

        roomTypeImageRepository.delete(img);
    }

}
