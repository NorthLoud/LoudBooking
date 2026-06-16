package com.example.loudhotel.controller;

import com.example.loudhotel.entity.Bill;
import com.example.loudhotel.repository.BillRepository;
import com.example.loudhotel.service.VNPayService;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayService vnPayService;
    private final BillRepository billRepository;

    @PostMapping("/vnpay")
    public String createPayment(@RequestParam Long billId,
                                HttpServletRequest httpRequest) {

        return vnPayService.createPaymentUrl(billId, httpRequest);
    }

    @GetMapping("/vnpay-return")
    public void paymentReturn(
            @RequestParam Map<String, String> params,
            HttpServletResponse response
    ) throws IOException {

        boolean valid = vnPayService.verifyPayment(params);

        if (!valid) {
            response.sendRedirect(
                    "http://localhost:8080/user/html/bill.html?payment=invalid"
            );
            return;
        }

        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef");

        Bill bill = billRepository.findByVnpTxnRef(txnRef)
                .orElseThrow(
                        () -> new RuntimeException("Không tìm thấy bill")
                );

        // tránh xử lý lại
        if (bill.getBillStatus() == Bill.BillStatus.PENDING) {

            if ("00".equals(responseCode)) {

                bill.setBillStatus(
                        Bill.BillStatus.PAID
                );

            } else {

                bill.setBillStatus(
                        Bill.BillStatus.CANCELED
                );

                bill.setCancelReason(
                        Bill.CancelReason.VNPAY_CANCEL
                );
            }

            bill.setVnpTransactionNo(
                    params.get("vnp_TransactionNo")
            );

            bill.setUpdatedAt(
                    LocalDateTime.now()
            );

            billRepository.save(bill);
        }

        // redirect về FE
        if ("00".equals(responseCode)) {

            response.sendRedirect(
                    "http://localhost:8080/user/html/bill.html?payment=success"
            );

        } else {

            response.sendRedirect(
                    "http://localhost:8080/user/html/bill.html?payment=failed"
            );
        }
    }
}