package com.example.loudhotel.dto.response;

import lombok.Data;

@Data
public class ImageResponse {

    private Long imageId;

    private String imageUrl;

    private boolean isMain;
}
