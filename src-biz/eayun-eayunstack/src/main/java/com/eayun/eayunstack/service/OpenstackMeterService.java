package com.eayun.eayunstack.service;

import com.alibaba.fastjson.JSONArray;


public interface OpenstackMeterService {

    /**
     * @param meter 等于某指标
     * @param dcId 
     * @param projectId 等于某项目
     * @param startTime 大于等于开始时间
     * @param endTime 小于结束时间
     * @return
     */
    public JSONArray getNetworkFlow(String meter, String dcId, String projectId, String startTime,
                                    String endTime);
    /**
     * 
     * @param meter
     * @param dcId      数据中心id
     * @param projectId 项目id
     * @param vmId      云主机id
     * @param startTime 开始时间
     * @param endTime   截止时间
     * @return
     */
    public JSONArray getNetwork(String lebelId, String dcId, String projectId,  String startTime,
                                     String endTime);
    
}
