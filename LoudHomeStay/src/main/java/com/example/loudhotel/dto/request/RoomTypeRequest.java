package com.example.loudhotel.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomTypeRequest {

    @NotBlank(message = "Tên loại phòng không được để trống")
    private String typeName;

    @NotNull(message = "Sức chứa không được để trống")
    @Min(value = 1, message = "Sức chứa tối thiểu là 1 người")
    private Integer capacity;

    @NotNull(message = "Giá phòng không được để trống")
    @Min(value = 0, message = "Giá phòng không được nhỏ hơn 0")
    private Double price;

    private String description;

    @NotBlank(message = "Loại giường không được để trống")
    private String bedType;

    @NotNull(message = "Số lượng giường không được để trống")
    @Min(value = 1, message = "Số lượng giường tối thiểu là 1")
    private Integer bedCount;

    @NotNull(message = "Diện tích không được để trống")
    @DecimalMin(value = "1.0", message = "Diện tích tối thiểu là 1 m²")
    private Double area;
}