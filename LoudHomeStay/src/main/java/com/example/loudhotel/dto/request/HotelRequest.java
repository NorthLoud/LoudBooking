package com.example.loudhotel.dto.request;

import com.example.loudhotel.entity.Hotel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class HotelRequest {

    @NotBlank(message = "Tên khách sạn không được để trống")
    @Size(min = 2, max = 100, message = "Tên khách sạn từ 2 - 100 ký tự")
    private String hotelName;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    @Size(max = 1000, message = "Mô tả không quá 1000 ký tự")
    private String introduction;

    @NotNull(message = "Trạng thái không được để trống")
    private Hotel.HotelStatus hotelStatus;

    private List<Long> utilitiesIds;

    private Long managerId;
}