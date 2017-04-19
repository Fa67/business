package com.eayun.schedule.service;

import com.eayun.datacenter.model.BaseDcDataCenter;

public interface CloudPortMappingService {

    /**
     * 同步底层数据中心下的端口映射资源
     * @author gaoxiang
     * @date 2016-09-01
     * @param dataCenter
     */
    public void synchData(BaseDcDataCenter dataCenter) throws Exception;
    
}
