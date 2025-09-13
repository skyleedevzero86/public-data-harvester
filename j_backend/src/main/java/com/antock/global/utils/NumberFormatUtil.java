package com.antock.global.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class NumberFormatUtil {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###");
    private static final NumberFormat KOREAN_FORMAT = NumberFormat.getNumberInstance(Locale.KOREA);

    public static String formatNumber(long number) {
        return DECIMAL_FORMAT.format(number);
    }

    public static String formatLong(long number) {
        return DECIMAL_FORMAT.format(number);
    }

    public static String formatNumber(int number) {
        return DECIMAL_FORMAT.format(number);
    }

    public static String formatNumber(double number) {
        return DECIMAL_FORMAT.format(number);
    }

    public static String formatNumberKorean(long number) {
        return KOREAN_FORMAT.format(number);
    }

    public static String formatNumberKorean(int number) {
        return KOREAN_FORMAT.format(number);
    }

    public static String formatNumberKorean(double number) {
        return KOREAN_FORMAT.format(number);
    }

    public static String formatCurrency(long number) {
        return String.format("₩%,d", number);
    }

    public static String formatCurrency(int number) {
        return String.format("₩%,d", number);
    }

    public static String formatCurrency(double number) {
        return String.format("₩%,.0f", number);
    }

    public static String formatPercent(double number) {
        return String.format("%.2f%%", number * 100);
    }

    public static String formatPercent(double number, boolean isDecimal) {
        if (isDecimal) {
            return String.format("%.2f%%", number * 100);
        } else {
            return String.format("%.2f%%", number);
        }
    }

    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    public static String formatSimple(long number) {
        if (number < 1000) {
            return String.valueOf(number);
        } else if (number < 1000000) {
            return String.format("%.1fK", number / 1000.0);
        } else if (number < 1000000000) {
            return String.format("%.1fM", number / 1000000.0);
        } else {
            return String.format("%.1fB", number / 1000000000.0);
        }
    }

    public static String formatSimple(int number) {
        return formatSimple((long) number);
    }

    public static String formatSimple(double number) {
        if (number < 1000) {
            return String.format("%.1f", number);
        } else if (number < 1000000) {
            return String.format("%.1fK", number / 1000.0);
        } else if (number < 1000000000) {
            return String.format("%.1fM", number / 1000000.0);
        } else {
            return String.format("%.1fB", number / 1000000000.0);
        }
    }

    public static String formatLong(Long number) {
        if (number == null) {
            return "0";
        }
        return DECIMAL_FORMAT.format(number);
    }

    public static String formatInteger(Integer number) {
        if (number == null) {
            return "0";
        }
        return DECIMAL_FORMAT.format(number);
    }

    public static String formatDouble(Double number) {
        if (number == null) {
            return "0";
        }
        return DECIMAL_FORMAT.format(number);
    }

    public static String formatStringNumber(String numberStr) {
        if (numberStr == null || numberStr.trim().isEmpty()) {
            return "0";
        }

        try {
            String cleanNumber = numberStr.replaceAll("[^0-9]", "");
            if (cleanNumber.isEmpty()) {
                return "0";
            }

            long number = Long.parseLong(cleanNumber);
            return DECIMAL_FORMAT.format(number);
        } catch (NumberFormatException e) {
            return numberStr;
        }
    }

    public static Long parseLong(String numberStr) {
        if (numberStr == null || numberStr.trim().isEmpty()) {
            return 0L;
        }

        try {
            String cleanNumber = numberStr.replaceAll("[^0-9]", "");
            if (cleanNumber.isEmpty()) {
                return 0L;
            }

            return Long.parseLong(cleanNumber);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public static Integer parseInt(String numberStr) {
        if (numberStr == null || numberStr.trim().isEmpty()) {
            return 0;
        }

        try {
            String cleanNumber = numberStr.replaceAll("[^0-9]", "");
            if (cleanNumber.isEmpty()) {
                return 0;
            }

            return Integer.parseInt(cleanNumber);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }

        try {
            String cleanNumber = str.replaceAll("[^0-9]", "");
            if (cleanNumber.isEmpty()) {
                return false;
            }

            Long.parseLong(cleanNumber);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}