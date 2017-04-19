package com.eayun.cdn.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.eayun.cdn.impl.ALiDNS;
import com.eayun.cdn.impl.DNS;


/**
 * @Filename: DNSUtil.java
 * @Description: DNS工具类
 * @Version: 1.0
 * @Author: fan.zhang
 * @Email: fan.zhang@eayun.com
 * @History:<br> <li>Date: 2016年6月17日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
@Component
public class DNSUtil {
    private static final Logger log = LoggerFactory.getLogger(DNSUtil.class);
    /**
     * 在正常的工程项目中，可直接通过DNSUtil.getInstance获取DNS实例，但是在计划任务xxJob中，只能通过applicationContext.getBean(DNS.class)的形式获取指定DNS实例
     * @return
     */
    public static DNS getInstance(){
        DNS dns = null;
        WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
        try {
            dns = context.getBean(DNS.class);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return dns;
    }
    
    public static ALiDNS getALiDNS(){
    	ALiDNS dns = null;
        WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
        try {
            dns = context.getBean(ALiDNS.class);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return dns;
    }
}
