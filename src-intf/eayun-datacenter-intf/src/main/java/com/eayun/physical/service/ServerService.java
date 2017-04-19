package com.eayun.physical.service;

import java.util.List;

import com.eayun.physical.model.DcServer;

public interface ServerService {
    
    /**
     * 根据数据中心得到项目
     * 
     * @return
     */
    List<DcServer> getServerListByDataCenter(String datacenterId);
    
    
    /**
     * 
     * */

}
