package com.eayun.common.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

/**
 * 停服务Service
 *                       
 * @Filename: ShutdownServiceImpl.java
 * @Description: 
 * @Version: 1.0
 * @Author: chenhao
 * @Email: hao.chen@eayun.com
 * @History:<br>
 *<li>Date: 2016年8月29日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Service
public class ShutdownServiceImpl implements DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(ShutdownServiceImpl.class);
    @Override
    public void destroy() throws Exception {
        log.info("Service is now shutting down！！！！！");  
    }

}
