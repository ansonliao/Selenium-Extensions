package com.github.ansonliao.selenium.utils;

import com.google.common.base.Strings;

public class StringUtils {

    public synchronized static String removeQuoteMark(String s) {
        return Strings.isNullOrEmpty(s) ? s : s.replaceAll("(^\"|\"$|^'|'$)", "");
    }
}
