package com.eayun.virtualization.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *                       
 * @Filename: NetworkFlowService.java
 * @Description: 
 * @Version: 1.0
 * @Author: chenhao
 * @Email: hao.chen@eayun.com
 * @History:<br>
 *<li>Date: 2015年12月8日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public interface NetworkFlowService {
    
    /**
     * 以MB为单位返回指定时间范围，指定项目的入流量
     * 
     * @param starttime
     * @param endtime
     * @param projectId
     * @return
     */   
    public List<Map<String,Object>> getIncomingData(Date starttime,Date endtime,String projectId);
    
    
    /**
     * 以MB为单位返回指定时间范围，指定项目的出流量
     * 
     * @param starttime
     * @param endtime
     * @param projectId
     * @return
     */
    public List<Map<String,Object>> getOutgoingData(Date starttime,Date endtime,String projectId);
    

}
