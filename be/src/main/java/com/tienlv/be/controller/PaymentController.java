package com.tienlv.be.controller;

import com.tienlv.be.dto.payment.CreatePaymentResponse;
import com.tienlv.be.security.AuthenticatedUser;
import com.tienlv.be.service.PaymentService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    @Value("${vnp.pay.url}") private String vnp_PayUrl;
    @Value("${vnp.return.url}") private String vnp_ReturnUrl;
    @Value("${vnp.tmn.code}") private String vnp_TmnCode;
    @Value("${vnp.hash.secret}") private String vnp_HashSecret;

    private final PaymentService paymentService;

    @PostMapping("/create")
    public CreatePaymentResponse createPayment(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            HttpServletRequest request) throws Exception {

        return paymentService.createPayment(
                authenticatedUser.userId(),
                request
        );
    }



    @GetMapping("/vnpay-callback")
    public void callback(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        boolean success =
                paymentService.processCallback(request);
        if (success) {
            response.sendRedirect("http://localhost:3000/?payment=success");
        } else {
            response.sendRedirect("http://localhost:3000/?payment=failed");
        }

    }
}