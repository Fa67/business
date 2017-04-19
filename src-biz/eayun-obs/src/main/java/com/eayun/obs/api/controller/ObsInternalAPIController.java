package com.eayun.obs.api.controller;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.accesskey.model.BaseAccessKey;
import com.eayun.accesskey.service.AccessKeyService;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.HmacSHA1Util;
import com.eayun.customer.model.Customer;
import com.eayun.customer.serivce.CustomerService;

/**
 * @Filename: ObsInternalAPIController.java
 * @Description: 提供给底层的Object和Bucket变更通知接口
 * @Version: 1.0
 * @Author: fan.zhang
 * @Email: fan.zhang@eayun.com
 * @History:<br> <li>Date: 2016年6月17日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
@Controller
@RequestMapping("/api")
@Scope("prototype")
public class ObsInternalAPIController {
    private static final Logger log = LoggerFactory.getLogger(ObsInternalAPIController.class);
    @Autowired
    private JedisUtil jedisUtil;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private AccessKeyService accessKeyService;
    @Autowired
    private MongoTemplate mongoTemplate;

    @RequestMapping(method = RequestMethod.POST, value = "/v1/obs/notice")
    @ResponseBody
    public String objectNoticeV1(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
        //1. 鉴权和校验，在doCheckParams中进行
        String date = request.getHeader("x-date");
        String authorization = request.getHeader("Authorization");
        String customerId = request.getHeader("Customer");

        JSONObject result = doCheckRequestHeaders(customerId, date, authorization, response);
        if (!result.getString("keyword").equals("Success")) {
            doLog("object",request, response, result, requestBody);
            return result.toJSONString();
        }
        result = doCheckRequestBody(response, requestBody);
        if (!result.getString("keyword").equals("Success")) {
            doLog("object",request, response, result, requestBody);
            return result.toJSONString();
        }

        //2. 组织参数放入任务队列
        JSONObject json = JSONObject.parseObject(requestBody);
        json.put("customerId", customerId);
        try {
            pushIntoTaskQueue("CDN_REFRESH:SYNCOBJECT", json);
        } catch (Exception e) {
            response.setStatus(500);
            result.put("keyword", "InternalServerError");
            result.put("message", "Oops, we encountered an internal error");
            log.error(e.getMessage(),e);
            return result.toString();
        }
        response.setStatus(200);
        result.put("keyword", "Success");
        result.put("message", "Task submitted successfully");

        doLog("object",request, response, result, requestBody);
        return result.toJSONString();

    }

    private void doLog(String opType, HttpServletRequest request, HttpServletResponse response, JSONObject result, String param) {
        JSONObject headerJ = new JSONObject();
        headerJ.put("x-date",request.getHeader("x-date"));
        headerJ.put("authorization",request.getHeader("Authorization"));
        headerJ.put("customer",request.getHeader("Customer"));

        JSONObject requestJ = new JSONObject();
        requestJ.put("headers",headerJ);
        requestJ.put("uri",request.getRequestURI());
        requestJ.put("http_method",request.getMethod());
        requestJ.put("remote_address",request.getRemoteAddr());
        requestJ.put("protocol",request.getProtocol());
        requestJ.put("param", param);

        result.put("status",response.getStatus());

        JSONObject log = new JSONObject();
        log.put("op_type",opType);
        log.put("request",requestJ);
        log.put("response",result);
        log.put("timestamp",new Date());

        mongoTemplate.insert(log, MongoCollectionName.LOG_OBS_NOTICE);

    }

    private JSONObject doCheckRequestBody(HttpServletResponse response, String requestBody) {
        JSONObject result = new JSONObject();
        //5. 检查请求体
        if (requestBody == null || "".equals(requestBody) || "{}".equals(requestBody)) {
            response.setStatus(400);
            result.put("keyword", "BadRequest");
            result.put("message", "Invalid request params");
        } else {
            JSONObject json = new JSONObject();
            try {
                json = JSONObject.parseObject(requestBody);
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                response.setStatus(400);
                result.put("keyword", "BadRequest");
                result.put("message", "Invalid request params");
                return result;
            }
            String bucketName = json.getString("bucket");
            if (bucketName == null || "".equals(bucketName)) {
                response.setStatus(400);
                result.put("keyword", "BadRequest");
                result.put("message", "The bucket specified is invalid");
            } else {
                JSONArray objects = json.getJSONArray("objects");
                if (objects == null || objects.isEmpty()) {
                    response.setStatus(400);
                    result.put("keyword", "BadRequest");
                    result.put("message", "The objects array specified is invalid");
                } else {
                    response.setStatus(200);
                    result.put("keyword", "Success");
                    result.put("message", "Check request headers successfully");
                }
            }
        }
        return result;
    }

    private JSONObject doCheckRequestHeaders(String customerId, String date, String authorization, HttpServletResponse response) {
        JSONObject result = new JSONObject();
        try {
            //1. 参数是否为空
            if (date == null || authorization == null || customerId == null) {
                response.setStatus(401);
                result.put("keyword", "Unauthorized");
                result.put("message", "Access is denied due to invalid credentials");
                return result;
            }

            //2. 客户是否存在
            boolean isCustomerExist = isCustomerExist(customerId);
            if (!isCustomerExist) {
                response.setStatus(401);
                result.put("keyword", "Unauthorized");
                result.put("message", "No such customer id");
                return result;
            }

            //3. 日期是否在当前服务器时间前后30min范围内

            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date headerDate = null;
            headerDate = sdf.parse(date);
            long timeOffset = 30 * 60 * 1000;
            long dateCeiling = now.getTime() + timeOffset;
            long dateFloor = now.getTime() - timeOffset;
            if (headerDate.getTime() < dateFloor || headerDate.getTime() > dateCeiling) {
                response.setStatus(400);
                result.put("keyword", "BadRequest");
                result.put("message", "Invalid date period");
                return result;
            }

            //4. 验证Header中的授权信息，即Authorization字段。
            String auth = authorization.substring(authorization.indexOf(" ") + 1);
            String decodedAuth = new String(Base64.decodeBase64(auth.getBytes()), StandardCharsets.UTF_8);
            String ak = decodedAuth.substring(0, decodedAuth.indexOf(":"));
            String signature = decodedAuth.substring(decodedAuth.indexOf(":") + 1, decodedAuth.length());

            List<BaseAccessKey> akList = accessKeyService.getAkList(ak);
            //如果获取的AK列表为空，或者获取到的AK不属于该用户，则授权失败，结束。
            if (akList.isEmpty() || !akList.get(0).getUserId().equals(customerId)) {
                response.setStatus(401);
                result.put("keyword", "Unauthorized");
                result.put("message", "Access is denied due to invalid credentials");
                return result;
            }
            String sk = akList.get(0).getSecretKey();
            String recodedSignature = null;
            recodedSignature = HmacSHA1Util.getEncrypt(date, sk);
            if (!signature.equals(recodedSignature)) {
                response.setStatus(401);
                result.put("keyword", "Unauthorized");
                result.put("message", "Access is denied due to invalid credentials");
                return result;
            }
            response.setStatus(200);
            result.put("keyword", "Success");
            result.put("message", "Check request headers successfully");
        } catch (ParseException e) {
            log.error(e.getMessage(),e);
            response.setStatus(400);
            result.put("keyword", "BadRequest");
            result.put("message", "Invalid date format");
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            response.setStatus(500);
            result.put("keyword", "InternalServerError");
            result.put("message", "Oops, we encountered an internal error");
        }
        return result;
    }

    private void pushIntoTaskQueue(String queueName, JSONObject requestJSON) throws Exception {
        jedisUtil.push(queueName, requestJSON.toJSONString());
        //写入mongodb用作日志以作备查
        requestJSON.put("timestamp", new Date());
        if (queueName.contains("BUCKET")) {
            mongoTemplate.insert(requestJSON, MongoCollectionName.CDN_REFRESH_BUCKET);
        } else if (queueName.contains("OBJECT")) {
            mongoTemplate.insert(requestJSON, MongoCollectionName.CDN_REFRESH_OBJECT);
        }

    }

    private boolean isCustomerExist(String customerId) {
        Customer customer = customerService.findCustomerById(customerId);
        if (customer == null || customer.getCusId() == null || "".equals(customer.getCusId())) {
            return false;
        }
        return true;
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/v1/obs/notice/{bucketName}")
    @ResponseBody
    public String bucketNoticeV1(HttpServletRequest request, HttpServletResponse response, @PathVariable String bucketName) {
        //1. 鉴权
        String date = request.getHeader("x-date");
        String authorization = request.getHeader("Authorization");
        String customerId = request.getHeader("Customer");

        JSONObject result = doCheckRequestHeaders(customerId, date, authorization, response);
        if (!result.getString("keyword").equals("Success")) {
            doLog("bucket",request, response, result, bucketName);
            return result.toJSONString();
        }
        //2. 组织参数放入任务队列
        JSONObject json = new JSONObject();
        json.put("customerId", customerId);
        json.put("bucket", bucketName);
        try {
            pushIntoTaskQueue("CDN_REFRESH:SYNCBUCKET", json);
        } catch (Exception e) {
            response.setStatus(500);
            result.put("keyword", "InternalServerError");
            result.put("message", "Oops, we encountered an internal error");
            log.error(e.getMessage(),e);
        }

        response.setStatus(200);
        result.put("keyword", "Success");
        result.put("message", "Task submitted successfully");
        doLog("bucket",request, response, result, bucketName);
        return result.toJSONString();
    }
}
