package com.example.loudhotel.repository;

import com.example.loudhotel.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByVoucherCodeAndIsDeletedFalse(String voucherCode);

    List<Voucher> findAllByStatusAndEndDateBefore(Voucher.VoucherStatus status, java.time.LocalDateTime now);
}
