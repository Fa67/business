package com.eayun.common.util;

import javax.servlet.http.HttpServletRequest;

public class HttpUtil {
    public static String getRequestIP(HttpServletRequest request) {
        try {
            String ip = request.getHeader("x-forwarded-for");
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            return ip;
        } catch (IllegalStateException e) {
            // 如果不是通过controller层过来，取header会报错，故加上try-catch
        }
        return "";
    }
}