package com.example.aichat;

import java.security.SecureRandom;
import java.util.Locale;

/**
 * Test-only verification code generator. No SMS or external service is used;
 * the generated code is shown on screen so the login flow can be tested.
 */
public final class OtpManager {
    private static final long TTL_MS = 120_000L;
    private static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private static String phone;
    private static String code;
    private static long expiresAt;
    private static int attempts;

    private OtpManager() {}

    public static synchronized String generate(String p) {
        phone = p;
        code = String.format(Locale.US, "%06d", RANDOM.nextInt(1_000_000));
        expiresAt = System.currentTimeMillis() + TTL_MS;
        attempts = 0;
        return code;
    }

    public static synchronized String getPhone() {
        return phone;
    }

    public static synchronized String currentCode() {
        return code;
    }

    public static synchronized long remainingSeconds() {
        if (code == null) return 0;
        return Math.max(0, (expiresAt - System.currentTimeMillis()) / 1000);
    }

    public static synchronized boolean verify(String p, String input) {
        if (code == null || p == null || !p.equals(phone)) return false;
        if (System.currentTimeMillis() > expiresAt) {
            code = null;
            return false;
        }
        if (++attempts > MAX_ATTEMPTS) {
            code = null;
            return false;
        }
        if (code.equals(input)) {
            code = null;
            return true;
        }
        return false;
    }

    /** Converts Persian/Arabic digits to ASCII and keeps only digits and a leading '+'. */
    public static String normalizePhone(String raw) {
        StringBuilder sb = new StringBuilder();
        for (char c : raw.trim().toCharArray()) {
            if (c >= '\u06F0' && c <= '\u06F9') c = (char) ('0' + (c - '\u06F0'));
            else if (c >= '\u0660' && c <= '\u0669') c = (char) ('0' + (c - '\u0660'));
            if ((c >= '0' && c <= '9') || c == '+') sb.append(c);
        }
        return sb.toString();
    }

    public static boolean isValidPhone(String p) {
        return p != null && p.matches("^\\+?\\d{10,15}$");
    }
}
