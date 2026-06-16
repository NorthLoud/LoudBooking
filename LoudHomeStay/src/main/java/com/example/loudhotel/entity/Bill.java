package com.example.loudhotel.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long billId;

    @Column(name = "bill_code", unique = true, nullable = false)
    private String billCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BillDetail> billDetails;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BillExtraFee> extraFees;

    private String orderName;
    private String orderEmail;
    private String orderPhone;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    private LocalDateTime actualCheckInTime;
    private LocalDateTime actualCheckOutTime;

    private Double totalCost;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private BillStatus billStatus;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private CancelReason cancelReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @Column(name = "discount_amount")
    private Double discountAmount;


    public enum BillStatus {
        PENDING, PAID, CANCELED
    }

    public enum PaymentMethod {
        VNPAY, CASH
    }

    public enum CancelReason {
        USER_CANCEL,
        HOTEL_CANCEL,
        NO_SHOW,
        VNPAY_CANCEL
    }

    private String vnpTxnRef;   // mã giao dịch gửi sang VNPay
    private String vnpTransactionNo; // mã VNPay trả về

    @Column(name = "id_card_code")
    private String idCardCode;
}