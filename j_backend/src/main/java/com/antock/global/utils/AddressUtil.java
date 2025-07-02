package com.antock.global.utils;

public class AddressUtil {

    public static String extractAddress(String address) {
        if (address == null) {
            return "";
        }

        String[] tokens = address.trim().split("\\s+");

        String result = "";
        if (tokens.length < 3) {
            result = address;
        } else {
            result = String.join(" ", tokens[0], tokens[1], tokens[2]);
        }

        return result;
    }
}