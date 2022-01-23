package org.simple.software.infrastructure;

public class StringUtils {
    public static boolean isBlank(String string) {
        return string == null || string.isEmpty() || string.trim().isEmpty();
    }

    public static long count(String string, char c) {
        return string.chars()
                .filter(sc -> sc == c)
                .count();
    }
}
