package com.example.loudhotel.repository;

import com.example.loudhotel.entity.BillExtraFee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BillExtraFeeRepository extends JpaRepository<BillExtraFee, Long> {
    List<BillExtraFee> findByBill_BillId(Long billId);
}
