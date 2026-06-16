package com.example.loudhotel.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilitiesRoomTypeId implements Serializable {

    private Long typeId;
    private Long utilitiesId;
}
