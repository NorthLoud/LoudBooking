package com.example.loudhotel.entity;

import com.example.loudhotel.enums.UtilityType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "utilities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilities {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long utilitiesId;

    @Column(nullable = false, unique = true)
    private String utilitiesName;

    @Enumerated(EnumType.STRING)
    @Column(name = "utility_type")
    private UtilityType utilityType;

}
