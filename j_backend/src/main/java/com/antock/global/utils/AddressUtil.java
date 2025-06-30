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

        String result = "";
        if(tokens.length < 3) { //읍/면/동까지 없는경우
            result = address;
        }
        else {
            result = String.join(" ", tokens[0], tokens[1], tokens[2]);
        }

        return result;
    }
}