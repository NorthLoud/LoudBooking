package com.example.loudhotel.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilitiesHotelId implements Serializable {
    private Long hotelId;
    private Long utilitiesId;
}
