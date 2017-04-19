package com.eayun.common.controller;

import javax.servlet.ServletContextEvent;

import org.springframework.web.context.ContextLoaderListener;

import com.eayun.common.ConstantClazz;
import com.eayun.common.util.StringUtil;


public class InitController extends ContextLoaderListener{
    
    public void contextInitialized(ServletContextEvent event) {
        if(StringUtil.isEmpty(ConstantClazz.WEB_PATH)){
            ConstantClazz.WEB_PATH =event.getServletContext().getRealPath("");
        }
        super.contextInitialized(event);
    }
}
