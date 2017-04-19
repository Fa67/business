package com.eayun.api.controller;

import com.alibaba.fastjson.JSONObject;
import com.eayun.api.service.ApiActionService;
import com.eayun.common.exception.ApiException;
import com.eayun.common.model.ApiRequestResult;
import com.eayun.common.constant.ApiConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;

@Controller
public class ApiController {

    @Autowired
    private ApiActionService apiActionService ;
    @Autowired
    private MongoTemplate mongoTemplate ;
    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @RequestMapping(value = "V1", method = RequestMethod.POST)
    @ResponseBody
    public Object apiService(HttpServletRequest request) throws Exception{

        logger.info("API服务程序执行主体开始");
        JSONObject result ;
        try {
            result = apiActionService.doAction(String.valueOf(request.getAttribute(ApiConstant.TAG_CLASS_NAME)), String.valueOf(request.getAttribute(ApiConstant.TAG_METHOD_NAME)), (JSONObject) request.getAttribute(ApiConstant.TAG_API_SERVICE_PARAMS_JSONOBJECT));
        }catch (Exception e){
            throw e ;
        }

        result = ApiRequestResult.normalReturn(String.valueOf(request.getAttribute(ApiConstant.TAG_REQUEST_ID)), String.valueOf(request.getAttribute(ApiConstant.TAG_ACTION)), result) ;
        //API服务正常调用结束，更新日志信息
        apiActionService.rightResultReturn(request, result) ;

        logger.info("API服务程序执行主体结束");
        return result ;

    }

    /**
     * 处理404之类的错误，不返回错误页面内容，而返回指定的错误信息
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/error_404")
    @ResponseBody
    public Object error_404(HttpServletRequest request) throws Exception{
        return ApiRequestResult.errorReturn(null, null, ApiException.createApiException("100016"));
    }

    /**
     * 处理500之类的错误，不返回错误页面内容，而返回指定的错误信息
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/error_500")
    @ResponseBody
    public Object error_500() throws Exception {
        return ApiRequestResult.errorReturn(null, null, ApiException.createApiException("100016"));
    }
}