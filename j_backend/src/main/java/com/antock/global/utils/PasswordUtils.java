package com.antock.global.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtils {

    private static PasswordEncoder passwordEncoder;

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        PasswordUtils.passwordEncoder = passwordEncoder;
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        if (passwordEncoder == null) {
            throw new IllegalStateException("PasswordEncoder가 초기화되지 않았습니다.");
        }

        if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$") || encodedPassword.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, encodedPassword);
        } else {
            return encodedPassword.equals(rawPassword);
        }
    }

    public static String encode(String rawPassword) {
        if (passwordEncoder == null) {
            throw new IllegalStateException("PasswordEncoder가 초기화되지 않았습니다.");
        }
        return passwordEncoder.encode(rawPassword);
    }
}