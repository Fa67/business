package com.eayun.api.service;


import com.alibaba.fastjson.JSONObject;
import com.eayun.common.model.ApiServiceLog;
import com.eayun.customer.serivce.CustomerService;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

public interface ApiActionService {

    /**
     * 反射方法调用。
     * @param className     类完全名称
     * @param methodName    映射方法名称
     * @param params        业务参数JSONObject
     * @return
     */
    JSONObject doAction(String className, String methodName, JSONObject params) throws Exception ;


    /**
     * 当鉴权验证失败时，保存必需参数
     * @param request
     * @param failParam
     * @return
     */
    public void authorizationFailedSaveLog(HttpServletRequest request, JSONObject failParam, String version) ;

    /**
     * afterCompletion方法执行完成后更新日志
     * @param request
     */
    Date afterCompletionSaveLog(HttpServletRequest request, long takeTime);

    /**
     * 异常处理完毕之后需要再次更新日志信息
     * @param result
     */
    public void exceptionCheckSaveLog(HttpServletRequest request, ApiServiceLog apiServiceLog, Object result);

    /**
     * 当一个指定的API服务首先进入拦截器的时候初始化对应的日志信息
     */
    void initSaveLog(HttpServletRequest request, String cusIP, String postContent, String version);

    /**
     * 鉴权通过之后需要重新更新一次日志信息
     * @param authResult
     * @return 返回对应的Action值
     */
    String authorizationCompletionSaveLog(HttpServletRequest request, JSONObject authResult, String version);

    /**
     * 当特定业务执行完毕之后，需要执行的操作。
     * @param request
     * @param businessResult
     */
    void rightResultReturn(HttpServletRequest request, JSONObject businessResult);
}