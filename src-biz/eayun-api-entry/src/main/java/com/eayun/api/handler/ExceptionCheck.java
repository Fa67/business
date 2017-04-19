package com.eayun.api.handler;

import com.alibaba.fastjson.JSONException;
import com.eayun.api.service.ApiActionService;
import com.eayun.common.exception.ApiException;
import com.eayun.common.model.ApiRequestResult;
import com.eayun.common.model.ApiServiceLog;
import com.eayun.common.constant.ApiConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ExceptionCheck {

    @Autowired
    private MongoTemplate mongoTemplate ;
    @Autowired
    private ApiActionService apiActionService ;
    private static final Logger logger = LoggerFactory.getLogger(ExceptionCheck.class);

    @ExceptionHandler(value= Exception.class)
    @ResponseBody
    public Object exceptionCheck(HttpServletRequest request, Exception e) {

        logger.info("API服务调用出现异常，进行异常统一处理开始") ;
        //获取保存在当前线程中的API日志信息
        ApiServiceLog apiServiceLog = (ApiServiceLog) request.getAttribute(ApiConstant.LOG_IDENTIFY);
        Object result = null;
        ApiException apiException = null ;
        logger.info(e.getClass().getSimpleName() + "-->" + e.getMessage());
        if (ApiException.class == e.getClass()) {
            apiException = (ApiException) e ;
        }
        if (JSONException.class == e.getClass()){
            apiException = ApiException.createApiException(ApiConstant.HTTP_BODY_ERROR_CODE) ;
        }
        if (HttpRequestMethodNotSupportedException.class == e.getClass()){
            apiException = ApiException.createApiException(ApiConstant.HTTP_METHOD_NOT_SUPPORTED_ERROR_CODE) ;
        }
        if (apiException == null) {
            apiException = ApiException.createApiException(ApiConstant.SERVER_RUNTIME_ERROR_CODE) ;
        }
        if (apiServiceLog != null) {
            apiServiceLog.seterrCode(apiException.getErrCode());
            //组装正确的数据返回格式
            result = ApiRequestResult.errorReturn(apiServiceLog.getjob_Id(), String.valueOf(request.getAttribute(ApiConstant.TAG_ACTION)), apiException);
            //异常出现，再次更新日志信息
            apiActionService.exceptionCheckSaveLog(request, apiServiceLog, result);
        }else {
            Object tempAction = request.getAttribute(ApiConstant.TAG_ACTION) ;
            result = ApiRequestResult.errorReturn( null, null, apiException);
        }
        logger.info("API服务调用出现异常，进行异常统一处理结束");
        return result ;

    }
}