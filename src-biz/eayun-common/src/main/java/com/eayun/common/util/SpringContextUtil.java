package com.eayun.common.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext context ;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (context == null){
            context = applicationContext ;
        }
    }

    public static ApplicationContext getContextInstance(){
        return context ;
    }

    public static Object getBean(Class<?> cla){
        return context.getBean(cla) ;
    }
}