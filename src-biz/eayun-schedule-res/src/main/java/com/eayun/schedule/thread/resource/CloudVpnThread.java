package com.eayun.schedule.thread.resource;

import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.schedule.service.CloudVpnService;

public class CloudVpnThread implements Callable<String> {
    private final static Log logger = LogFactory
            .getLog(CloudVpnThread.class);
    
    private BaseDcDataCenter dataCenter;
    
    private CloudVpnService service;
    
    public CloudVpnThread(CloudVpnService service) {
        this.service = service;
    }
    
    @Override
    public String call() throws Exception {
        logger.info("执行VPN资源同步---开始");
        try {
            service.synchData(dataCenter);
            logger.info("执行VPN资源同步---结束");
            return "success";
        } catch (AppException e) {
            logger.error("执行VPN资源同步出错:" + e.getMessage(),e);
            return "failed";
        }
    }
    
    public BaseDcDataCenter getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(BaseDcDataCenter dataCenter) {
        this.dataCenter = dataCenter;
    }
}
