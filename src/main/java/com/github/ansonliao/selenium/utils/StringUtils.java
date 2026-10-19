package com.github.ansonliao.selenium.utils;

import com.google.common.base.Strings;

public class StringUtils {

    public synchronized static String removeQuoteMark(String s) {
        return Strings.isNullOrEmpty(s) ? s : s.replaceAll("^[\"']|[\"']$", "");
    }

    public synchronized static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public synchronized static boolean isNotNullAndNotEmpty(String s) {
        return !isNullOrEmpty(s);
    }
}
