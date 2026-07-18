package com.tienlv.be.dto.payment;

public record PaymentCallbackResponse(
        boolean success,
        String message
) {
}