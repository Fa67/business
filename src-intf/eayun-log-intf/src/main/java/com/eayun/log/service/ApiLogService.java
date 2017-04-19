package com.eayun.log.service;


import com.alibaba.fastjson.JSONObject;

public interface ApiLogService {

    /**
     * API服务（日志查询）
     * @param params    参数为JSONObject
     * @return          返回值类型JSONObject
     * @throws Exception
     */
    public JSONObject OperationLog(JSONObject params)throws Exception ;

}