package com.nexamart.nexamart.util;

import java.util.regex.Pattern;

public final class ValidationUtil {
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private ValidationUtil() {}

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL.matcher(email).matches();
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isValidRole(String role) {
        return "BUYER".equals(role) || "SELLER".equals(role);
    }

    public static boolean isPositiveInt(int n) {
        return n > 0;
    }
}
