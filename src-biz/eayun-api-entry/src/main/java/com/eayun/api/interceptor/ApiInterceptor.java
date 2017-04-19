package com.eayun.api.interceptor;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.eayun.api.service.ApiActionService;
import com.eayun.apiverify.service.AuthorizationService;
import com.eayun.common.exception.ApiException;
import com.eayun.common.constant.ApiConstant;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.HttpUtil;
import com.eayun.common.util.ApiUtil;
import com.eayun.customer.serivce.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ApiInterceptor extends HandlerInterceptorAdapter{

    @Autowired
    private MongoTemplate mongoTemplate ;
    @Autowired
    private JedisUtil jedisUtil ;
    @Autowired
    private CustomerService customerService ;
    @Autowired
    private ApiActionService apiActionService ;
    @Autowired
    private AuthorizationService authorizationService ;
    //日志信息打印
    private static Logger logger = LoggerFactory.getLogger(ApiInterceptor.class);

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {

            logger.info("API拦截器任务开始执行");
            long start = System.currentTimeMillis() ;
            logger.info("start : " + start);
            request.setAttribute("startTime",start);
            String cusIP = HttpUtil.getRequestIP(request);
            logger.info("cusIP : " + cusIP);
            String postContent = ApiUtil.postContent(request.getInputStream());
            logger.info("postContent : " + postContent);
            String contentType = request.getContentType();
            logger.info("contentType : " + contentType);
            String version = request.getRequestURI().substring(1);
            logger.info("version : " + version) ;

            // TODO: 2017/3/2 初始化日志操作：设置API当前版本
            this.apiActionService.initSaveLog(request, cusIP, postContent, version);
            JSONObject postParameters = JSONObject.parseObject(postContent);
            if (postParameters == null ||                 //如果请求内容不合法，则抛出JSON异常
                    postParameters.size() == 0){
                throw new JSONException();
            }
            //调用鉴权接口，传递四个必需参数，返回鉴权解析后实际需要的参数
            JSONObject realParameters = null ;
            try {
                realParameters = this.authorizationService.checkAuthorization(postParameters, contentType, cusIP, version);
            }catch (ApiException e){
                if (e.getBaseInformation() != null) {
                    // TODO: 2017/3/2 需要调用当鉴权失败时，保留必要数据信息的接口(需要联合测试)
                    this.apiActionService.authorizationFailedSaveLog(request, e.getBaseInformation(), version);
                }
                throw e ;
            }
            //鉴权完毕，更新日志信息
            String action = apiActionService.authorizationCompletionSaveLog(request,realParameters,version) ;
            // TODO: 2017/3/2 需要注意的是，在当鉴权操作完成之后，若遇到了：客户超过配额次数限制，数据中心API服务关闭，客户被拉进黑名单，将ip地址地址拉进黑名单，客户被冻结 之中的任意一种的话，也是应该保留其对应的客户、数据中心和Action信息的
            //鉴权部分会校验操作项Action的合法性，在代码层面，还需要进行一次单独的校验，是由于数据字典的配置与实际程序代码的配置是互相独立的
            String mappingResult = ApiUtil.getMappingMessageByAction(version + "/" + action) ;
            logger.info("mappingResult : " + mappingResult);
            if (mappingResult == null){
                // TODO: 2017/3/2 不存在值定Action的话，需要跑出对应的错误信息
                throw ApiException.createApiException(ApiConstant.SYSTEM_ACTION_NOT_SET_ERROR_CODE);
            }else {
                request.setAttribute(ApiConstant.TAG_API_SERVICE_PARAMS_JSONOBJECT, realParameters);
                request.setAttribute(ApiConstant.TAG_ACTION       , action);
                request.setAttribute(ApiConstant.TAG_CLASS_NAME   , mappingResult.split(":")[0]);
                request.setAttribute(ApiConstant.TAG_METHOD_NAME  , mappingResult.split(":")[1]);
            }
        }catch (Exception e){
            logger.error(e.getClass().getSimpleName() + " ---> " + e.getMessage());
            throw e;
        }
        return true ;
    }
}