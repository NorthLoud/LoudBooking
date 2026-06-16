package com.example.loudhotel.service;

import com.example.loudhotel.dto.response.ImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {

    List<ImageResponse> getImagesByHotel(Long hotelId);


    List<ImageResponse> uploadHotelImages(Long hotelId,
                                             List<MultipartFile> files,
                                             Integer mainIndex);

    void setMainImage(Long imageId);

    void deleteImage(Long imageId);

    List<ImageResponse> uploadRoomTypeImages(Long typeId, List<MultipartFile> files, Integer mainIndex);

    List<ImageResponse> getImagesByRoomType(Long typeId);

    void setMainRoomTypeImage(Long imageId);

    void deleteRoomTypeImage(Long imageId);
}
