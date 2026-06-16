package com.example.loudhotel.service;

import com.example.loudhotel.config.VnPayConfig;
import com.example.loudhotel.dto.request.BillRequest;
import com.example.loudhotel.entity.Bill;
import com.example.loudhotel.entity.Room;
import com.example.loudhotel.repository.BillRepository;

import com.example.loudhotel.repository.HotelRepository;
import com.example.loudhotel.repository.RoomRepository;
import com.example.loudhotel.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VNPayService {

    private final VnPayConfig vnPayConfig;
    private final BillRepository billRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;

    public String createPaymentUrl(Long billId, HttpServletRequest requestHttp) {

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill không tồn tại"));

        String txnRef = bill.getVnpTxnRef();

        Map<String, String> params = new HashMap<>();

        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnPayConfig.getTmnCode());

        params.put("vnp_Amount", String.valueOf((long) (bill.getTotalCost() * 100)));
        params.put("vnp_CurrCode", "VND");

        params.put("vnp_TxnRef", txnRef);

        params.put("vnp_OrderInfo", "Thanh toan bill #" + bill.getBillId());

        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrlUser());
        params.put("vnp_IpAddr", VnPayConfig.getIpAddress(requestHttp));

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

        params.put("vnp_CreateDate", formatter.format(cal.getTime()));

        // ✅ 15 PHÚT HẾT HẠN
        cal.add(Calendar.MINUTE, 10);
        params.put("vnp_ExpireDate", formatter.format(cal.getTime()));

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        boolean first = true;

        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);

            if (fieldValue != null && !fieldValue.isEmpty()) {

                if (!first) {
                    hashData.append("&");
                    query.append("&");
                }

                hashData.append(fieldName)
                        .append("=")
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));

                query.append(fieldName)
                        .append("=")
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));

                first = false;
            }
        }

        String secureHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());

        return vnPayConfig.getPayUrl()
                + "?"
                + query
                + "&vnp_SecureHash="
                + secureHash;
    }

    public boolean verifyPayment(Map<String, String> params) {

        String vnpSecureHash = params.get("vnp_SecureHash");

        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();

        boolean first = true;

        for (String fieldName : fieldNames) {

            String value = params.get(fieldName);

            if (value != null && !value.isEmpty()) {

                if (!first) {
                    hashData.append("&");
                }

                hashData.append(fieldName)
                        .append("=")
                        .append(URLEncoder.encode(value, StandardCharsets.UTF_8));

                first = false;
            }
        }

        String signValue = hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());

        return signValue.equals(vnpSecureHash);
    }

    private String hmacSHA512(String key, String data) {

        try {

            Mac mac = Mac.getInstance("HmacSHA512");

            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA512");

            mac.init(secretKey);

            byte[] raw = mac.doFinal(data.getBytes());

            StringBuilder hex = new StringBuilder();

            for (byte b : raw) {

                String hexStr = Integer.toHexString(0xff & b);

                if (hexStr.length() == 1) hex.append('0');

                hex.append(hexStr);
            }

            return hex.toString();

        } catch (Exception e) {

            throw new RuntimeException("Cannot generate hash", e);
        }
    }
}