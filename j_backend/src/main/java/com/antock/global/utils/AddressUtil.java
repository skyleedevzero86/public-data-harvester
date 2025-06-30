package com.antock.global.utils;

/**
 * 주소 처리 유틸
 */
public class AddressUtil {

    public static String extractAddress(String address) {
        if(address == null) {
            return "";
        }
        // 공백 기준으로 나누기
        String[] tokens = address.trim().split("\\s+");

        return String.join(" ", tokens[0], tokens[1], tokens[2]);
    }
}