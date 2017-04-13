package com.github.ansonliao.selenium.utils;

import com.github.ansonliao.selenium.annotations.RemoteAddress;

import java.lang.reflect.Method;

/**
 * Created by ansonliao on 31/3/2017.
 */
public class RemoteAddressUtils {

    public static synchronized String getRemoteAddress(Method method) {
        RemoteAddress remoteAddress = method.getDeclaredAnnotation(RemoteAddress.class);
        return remoteAddress == null ? null : remoteAddress.value().trim();
    }

    public static synchronized String getRemoteAddress(Class clazz) {
        RemoteAddress remoteAddress = (RemoteAddress) clazz.getDeclaredAnnotation(RemoteAddress.class);
        return remoteAddress == null ? null : remoteAddress.value().trim();
    }

    public static synchronized String getRemoteAddress(Class clazz, Method method) {
        String remoteAddressOfClass = getRemoteAddress(clazz);
        String remoteAddressOfMethod = getRemoteAddress(method);
        if (remoteAddressOfMethod != null) {
            return remoteAddressOfMethod;
        } else if (remoteAddressOfClass != null) {
            return remoteAddressOfClass;
        } else {
            return null;
        }
    }
}
