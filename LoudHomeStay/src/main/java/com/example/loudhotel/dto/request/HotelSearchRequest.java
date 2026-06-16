package com.example.loudhotel.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class HotelSearchRequest {

    private String keyword;

    private LocalDate checkIn;

    private LocalDate checkOut;

    private Integer roomCount;

}