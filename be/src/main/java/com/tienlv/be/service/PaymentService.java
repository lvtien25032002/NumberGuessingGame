package com.tienlv.be.service;

import com.tienlv.be.VnpayConfig;
import com.tienlv.be.dto.payment.CreatePaymentResponse;
import com.tienlv.be.entity.Payment;
import com.tienlv.be.entity.User;
import com.tienlv.be.enums.PaymentMethod;
import com.tienlv.be.enums.PaymentStatus;
import com.tienlv.be.exception.NotFoundException;
import com.tienlv.be.repository.PaymentRepository;
import com.tienlv.be.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private final UserRepository userRepository;

    @Value("${vnp.pay.url}")
    private String vnpPayUrl;

    @Value("${vnp.return.url}")
    private String vnpReturnUrl;

    @Value("${vnp.tmn.code}")
    private String vnpTmnCode;

    @Value("${vnp.hash.secret}")
    private String vnpHashSecret;


    @Transactional
    public CreatePaymentResponse createPayment(
            Long userId,
            HttpServletRequest request
    ) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Payment payment = createPendingPayment(user);
        String paymentUrl =
                buildPaymentUrl(payment, request);
        return new CreatePaymentResponse(paymentUrl, "00", "success");
    }

    private Payment createPendingPayment(User user) {
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setOrderId(generateOrderId());
        payment.setAmount(50000L);
        payment.setTurns(5);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(PaymentMethod.VNPAY);
        payment.setCreatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    private String generateOrderId() {
        return "PAY-" +
                LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-"
                + UUID.randomUUID().toString().substring(0,8);
    }

    public String buildPaymentUrl(Payment payment, HttpServletRequest request) throws UnsupportedEncodingException {

        String vnpVersion = "2.1.0";
        String vnpCommand = "pay";
        String orderType = "other";
        String locale = "vn";
        String currCode = "VND";
        String txnRef = String.valueOf(System.currentTimeMillis());
        String orderInfo = "Thanh toan";

        long amount = payment.getAmount() * 100L;
        Map<String, String> vnpParams = new HashMap<>();


        String ipAddr = VnpayConfig.getIpAddress(request);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

        String createDate = formatter.format(calendar.getTime());

        calendar.add(Calendar.MINUTE, 15);
        String expireDate = formatter.format(calendar.getTime());



        vnpParams.put("vnp_Version", vnpVersion);
        vnpParams.put("vnp_Command", vnpCommand);
        vnpParams.put("vnp_TmnCode", vnpTmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amount));
        vnpParams.put("vnp_CurrCode", currCode);
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", orderType);
        vnpParams.put("vnp_Locale", locale);
        vnpParams.put("vnp_ReturnUrl", vnpReturnUrl);
        vnpParams.put("vnp_IpAddr", ipAddr);
        vnpParams.put("vnp_CreateDate", createDate);
        vnpParams.put("vnp_ExpireDate", expireDate);

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder query = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                if (query.length() > 0) {
                    query.append("&");
                }
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()))
                     .append("=")
                     .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
            }
        }
        
        String secureHash = VnpayConfig.hashAllFields(vnpParams, vnpHashSecret);

        // Thêm SecureHash vào cuối URL
        query.append("&vnp_SecureHash=");
        query.append(secureHash);
        return vnpPayUrl + "?" + query;
    }

    @Transactional
    public boolean processCallback(HttpServletRequest request) {
        if (!VnpayConfig.verifySecureHash(request, vnpHashSecret)) {
            return false;
        }

        String orderId = request.getParameter("vnp_TxnRef");

        Payment payment = paymentRepository
                .findByOrderIdForUpdate(orderId)
                .orElseThrow(() ->
                        new NotFoundException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            return false;
        }

        // verify amount between database and callback is equal.
        long amount = Long.parseLong(request.getParameter("vnp_Amount")) / 100;
        if (!payment.getAmount().equals(amount)) {
            return false;
        }

        // verify Transaction Status.
        String responseCode = request.getParameter("vnp_ResponseCode");
        String transactionStatus = request.getParameter("vnp_TransactionStatus");
        if (!"00".equals(responseCode) || !"00".equals(transactionStatus)) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            return false;
        }


        // payment successful
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        payment.setTransactionNo(
                request.getParameter("vnp_TransactionNo")
        );
        User user = payment.getUser();
        user.setTurns(
                user.getTurns() + payment.getTurns()
        );
        paymentRepository.save(payment);
        userRepository.save(user);
        return true;
    }
}