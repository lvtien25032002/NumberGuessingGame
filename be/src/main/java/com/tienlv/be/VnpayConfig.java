package com.tienlv.be;

import jakarta.servlet.http.HttpServletRequest;

import java.net.URLEncoder;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class VnpayConfig {


    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey =
                    new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }
            return hash.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String hashAllFields(Map<String, String> fields, String secretKey) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                try {
                    sb.append(fieldName)
                      .append("=")
                      .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return hmacSHA512(secretKey, sb.toString());
    }

    public static boolean verifySecureHash(
            HttpServletRequest request,
            String secretKey
    ) {

        Map<String, String> fields = new HashMap<>();
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String fieldName = params.nextElement();
            if (!"vnp_SecureHash".equals(fieldName)
                    && !"vnp_SecureHashType".equals(fieldName)) {
                fields.put(fieldName, request.getParameter(fieldName)
                );
            }
        }

        String secureHash = request.getParameter("vnp_SecureHash");
        return hashAllFields(fields, secretKey)
                .equalsIgnoreCase(secureHash);
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || ipAddress.isBlank()) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}