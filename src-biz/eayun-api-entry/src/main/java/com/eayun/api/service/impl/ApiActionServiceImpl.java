package com.eayun.api.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.eayun.api.service.ApiActionService;
import com.eayun.common.constant.ApiConstant;
import com.eayun.common.exception.ApiException;
import com.eayun.common.model.ApiServiceLog;
import com.eayun.common.util.ApiUtil;
import com.eayun.common.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

@Service
public class ApiActionServiceImpl implements ApiActionService {

    @Autowired
    private MongoTemplate mongoTemplate ;
    private static Logger logger = LoggerFactory.getLogger(ApiActionServiceImpl.class) ;

    public JSONObject doAction(String className, String methodName, JSONObject params) throws Exception {
        Class<?> requestClass = Class.forName(className);
        Object object = SpringContextUtil.getBean(requestClass);
        Method method = requestClass.getDeclaredMethod(methodName, JSONObject.class);
        Object result = null ;
        try {
            result = method.invoke(object, params);
        }catch (InvocationTargetException e){
            Throwable throwable = e.getTargetException() ;
            if (throwable instanceof ApiException) {
                throw (ApiException)throwable ;
            }else {
                throw (Exception) throwable ;
            }
        }
        if (result != null){
            return (JSONObject) result ;
        }else {
            return null ;
        }
    }

    @Override
    public Date afterCompletionSaveLog(HttpServletRequest request, long takeTime) {
        Date taskOKDate = new Date() ;
        //取出保存的APIServiceLog实例对象
        ApiServiceLog apiServiceLog = (ApiServiceLog) request.getAttribute(ApiConstant.LOG_IDENTIFY);
        //设置处理时间
        apiServiceLog.settakeTime(takeTime);
        //设置状态更新时间
        apiServiceLog.setstatus_time(taskOKDate);
        mongoTemplate.save(apiServiceLog, ApiConstant.API_LOG_COLLECTION_NAME);
        return taskOKDate ;
    }

    @Override
    public void exceptionCheckSaveLog(HttpServletRequest request, ApiServiceLog apiServiceLog, Object result) {

        logger.info("异常出现，更新日志信息开始");
        Date taskOKDate = new Date() ;
        //设置API的执行状态为失败
        apiServiceLog.setstatus("0");
        //设置其的返回内容信息为错误提示返回信息
        apiServiceLog.setresponseBody(JSONObject.toJSONString(result));
        apiServiceLog.setstatus_time(taskOKDate);
        apiServiceLog.settakeTime(taskOKDate.getTime() - (long)request.getAttribute("startTime"));
        //更新Log日志信息
        mongoTemplate.save(apiServiceLog, ApiConstant.API_LOG_COLLECTION_NAME);
        ApiUtil.storeRedisDataForAPILog(
                ApiUtil.redisKeyTimestamp(taskOKDate),
                apiServiceLog
        );
        logger.info("异常出现，更新日志信息结束");
    }

    /**
     * 拦截器接收到API访问请求时，需要执行的初始化日志对象操作
     * @param request
     * @param cusIP
     * @param postContent
     * @param version
     */
    @Override
    public void initSaveLog(HttpServletRequest request,String cusIP, String postContent, String version) {
        logger.info("初始化日志信息开始");
        ApiServiceLog apiServiceLog = new ApiServiceLog(cusIP, postContent);
        apiServiceLog.setversion(version);
        mongoTemplate.save(apiServiceLog, ApiConstant.API_LOG_COLLECTION_NAME);
        String RequestId = apiServiceLog.getjob_Id();
        request.setAttribute(ApiConstant.TAG_REQUEST_ID, RequestId);
        request.setAttribute(ApiConstant.LOG_IDENTIFY  , apiServiceLog);
        logger.info("初始化日志信息结束");
    }

    /**
     * 当鉴权操作执行完毕之后，后续需要执行的操作
     * @param request
     * @param realParameters
     * @param version
     * @return
     */
    @Override
    public String authorizationCompletionSaveLog(HttpServletRequest request, JSONObject realParameters, String version) {
        logger.info("鉴权完毕，更新日志信息开始");
        ApiServiceLog apiServiceLog = (ApiServiceLog) request.getAttribute(ApiConstant.LOG_IDENTIFY);
        String cusID = String.valueOf(realParameters.get(ApiConstant.TAG_CUS_ID)) ;
        String action = String.valueOf(realParameters.get(ApiConstant.TAG_ACTION)) ;
        apiServiceLog.setoperatorId(cusID);
        String resourceType = String.valueOf(realParameters.get(ApiConstant.TAG_RESOURCE_TYPE)) ;
        apiServiceLog.setresourceTypeNodeId(ApiUtil.getNodeIdByRedisData(version,resourceType,true));
        String apiName = String.valueOf(realParameters.get(ApiConstant.TAG_API_NAME)) ;
        apiServiceLog.setapiNameNodeId(ApiUtil.getNodeIdByRedisData(version,apiName,false));
        apiServiceLog.setregionId(String.valueOf(realParameters.get(ApiConstant.TAG_DC_ID)));
        mongoTemplate.save(apiServiceLog, ApiConstant.API_LOG_COLLECTION_NAME);
        logger.info("鉴权完毕，更新日志信息结束");
        return action;
    }

    @Override
    public void authorizationFailedSaveLog(HttpServletRequest request, JSONObject failParam, String version) {
        logger.info("鉴权完毕，更新日志信息开始");
        ApiServiceLog apiServiceLog = (ApiServiceLog) request.getAttribute(ApiConstant.LOG_IDENTIFY);

        if (failParam.get(ApiConstant.TAG_CUS_ID) != null){
            apiServiceLog.setoperatorId(String.valueOf(failParam.get(ApiConstant.TAG_CUS_ID)));
        }
        if (failParam.get(ApiConstant.TAG_DC_ID) != null){
            apiServiceLog.setregionId(String.valueOf(failParam.get(ApiConstant.TAG_DC_ID)));
        }
        if (failParam.get(ApiConstant.TAG_RESOURCE_TYPE) != null){
            apiServiceLog.setresourceTypeNodeId(ApiUtil.getNodeIdByRedisData(version,String.valueOf(failParam.get(ApiConstant.TAG_RESOURCE_TYPE)),true));
        }
        if (failParam.get(ApiConstant.TAG_API_NAME) != null){
            apiServiceLog.setapiNameNodeId(ApiUtil.getNodeIdByRedisData(version,String.valueOf(failParam.get(ApiConstant.TAG_API_NAME)),false));
        }
        mongoTemplate.save(apiServiceLog, ApiConstant.API_LOG_COLLECTION_NAME);
    }

    /**
     * 当指定的具体业务成功执行并且返回数据之后，执行的操作
     * @param request
     * @param businessResult
     */
    @Override
    public void rightResultReturn(HttpServletRequest request, JSONObject businessResult){
        logger.info("API服务请求执行完成，更新日志信息开始");
        ApiServiceLog apiServiceLog = (ApiServiceLog) request.getAttribute(ApiConstant.LOG_IDENTIFY);
        Date taskOKDate = new Date() ;
        if (businessResult != null){
            apiServiceLog.setresponseBody(JSONObject.toJSONString(businessResult));
        }
        if (businessResult != null && businessResult.containsKey("ResourceId")){
            apiServiceLog.setresourceId(String.valueOf(businessResult.get("ResourceId")));
        }
        apiServiceLog.setstatus("1");
        apiServiceLog.settakeTime(taskOKDate.getTime() - (long) request.getAttribute("startTime"));
        apiServiceLog.setstatus_time(taskOKDate);
        mongoTemplate.save(apiServiceLog, ApiConstant.API_LOG_COLLECTION_NAME);
        ApiUtil.storeRedisDataForAPILog(
                ApiUtil.redisKeyTimestamp(taskOKDate),
                (ApiServiceLog) request.getAttribute(ApiConstant.LOG_IDENTIFY)
        ) ;
        logger.info("API服务请求执行完成，更新日志信息结束");
    }
}