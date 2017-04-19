package com.eayun.virtualization.service;

import java.util.Date;

import org.springframework.data.mongodb.core.MongoTemplate;

import com.alibaba.fastjson.JSONArray;

/**
 * 虚拟机监控服务
 *                       
 * @Filename: VmMonitorService.java
 * @Description: 
 * @Version: 1.0
 * @Author: chenhao
 * @Email: hao.chen@eayun.com
 * @History:<br>
 *<li>Date: 2015年12月16日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public interface VmMonitorService {

    /**
     * 指标采集
     * 
     * @param meter [0]:field [1]:value
     * @param dataCenterId
     * @param projectId
     * @param vmId
     * @return
     */
    public JSONArray getMeter(String[] meter, String dataCenterId, String projectId , String now , String late);
    
    public JSONArray getMeterTwo(String[] meter, String dataCenterId, String projectId, String vmId);

    /**
     * 定时汇总
     * 
     * @param mongoTemplate
     * @param meter
     * @param projectId
     * @param vmId
     * @param interval
     */
    public void summary(MongoTemplate mongoTemplate, String meter, String projectId, String vmId,
                        String interval,Date now);

}
