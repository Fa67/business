package com.eayun.common.tools;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class SpringBeanUtil {
    private static final Log          log     = LogFactory.getLog(SpringBeanUtil.class);

    private static ApplicationContext context = null;

    private static ApplicationContext getContext() {
        if (context == null) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getRequest();
            if (request == null) {
                throw new RuntimeException(
                    "无法获取http request,请确保在servlet线程中执行,并配置 org.springframework.web.context.request.RequestContextListener");
            }

            context = WebApplicationContextUtils.getWebApplicationContext(request
                .getServletContext());
        }

        return context;
    }

    public static Object getBean(String name) {
        try {
            return getContext().getBean(name);
        } catch (BeansException ex) {
            log.error("get bean failed", ex);
            return null;
        }
    }
    
    public static <T> T getBean(Class<T> clazz) {
        try {
            return getContext().getBean(clazz);
        } catch (BeansException ex) {
            log.error("get bean failed", ex);
            return null;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Object getBean(String name, Class clazz) {
        try {
            return getContext().getBean(name, clazz);
        } catch (BeansException ex) {
            log.error("get bean failed", ex);
            return null;
        }
    }
}
