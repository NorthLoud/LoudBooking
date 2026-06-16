package com.example.loudhotel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.example.loudhotel.enums.UtilityType;
import lombok.Data;

@Data
public class UtilitiesRequest {

    @NotBlank(message = "Tên tiện ích không được để trống")
    private String utilitiesName;

    @NotNull(message = "Loại tiện ích không được để trống")
    private UtilityType utilityType;
}
