package com.example.loudhotel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "bill_extra_fees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillExtraFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long extraFeeId;

    @ManyToOne
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String reason;

    private LocalDateTime createdAt;
    
    @Builder.Default
    private boolean isPaid = false;
}
