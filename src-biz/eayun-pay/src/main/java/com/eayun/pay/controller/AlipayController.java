/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.pay.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.druid.support.json.JSONUtils;
import com.alipay.util.AlipayNotify;
import com.eayun.common.controller.BaseController;
import com.eayun.pay.model.AlipayConstants;
import com.eayun.pay.model.AlipayConstants.ParamKeys;
import com.eayun.pay.service.AlipayPaymentService;

/**
 *                       
 * @Filename: AlipayController.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2016年7月28日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Controller
@RequestMapping("/pay/alipay")
@Scope("prototype")
public class AlipayController extends BaseController {

    private final static Logger  logger = LoggerFactory.getLogger(AlipayController.class);

    @Autowired
    private AlipayPaymentService alipayPaymentService;
    
    /**
     * 支付宝即时到账交易回调通知接口
     * @param request
     * @param response
     */
    @RequestMapping("/tradeNotify")
    public void tradeNotify(HttpServletRequest request, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        //获取支付宝交易异步通知的参数
        Map<String, String> params = getParameters(request, false);
        String result = "fail";
        try {
            String paramsJson = JSONUtils.toJSONString(params);
            //验证签名
            if (AlipayNotify.verify(params)) {
                logger.info("received an alipay notify, outTradeNo[{}], params:{}", params.get(ParamKeys.OUT_TRADE_NO), paramsJson);
                alipayPaymentService.processTradeNotify(params);
                result = "success";
            } else {
                logger.warn("received an illegal alipay notify:{}", paramsJson);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        } finally {
            try {
                response.getWriter().write(result);
                response.getWriter().close();
            } catch (Exception e2) {
                logger.error(e2.getMessage(),e2);
            }
        }
    }
    @RequestMapping("/tradeReturn")
    public void tradeReturn(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        //获取支付宝交易异步通知的参数
        Map<String, String> params = getParameters(request, true);
        try {
            String paramsJson = JSONUtils.toJSONString(params);
            
            //验证签名
            if (AlipayNotify.verify(params)) {
                logger.info("received an alipay return, outTradeNo[{}], params:{}", params.get(ParamKeys.OUT_TRADE_NO), paramsJson);
                String subject = params.get(AlipayConstants.ParamKeys.SUBJECT);
                alipayPaymentService.processTradeNotify(params);
                //跳转到支付结果展示界面
                response.sendRedirect(request.getContextPath() + "/#/pay/result/"+ URLEncoder.encode(subject, "UTF-8"));
            } else {
                logger.warn("received an illegal alipay return:{}", paramsJson);
                response.sendRedirect(request.getContextPath() + "/#/error");
            }
            
        } catch (Exception e) {
            logger.error("支付宝同步通知出错", e);
            response.sendRedirect(request.getContextPath() + "/#/error");
        }
    }

    @SuppressWarnings("rawtypes")   
    public Map<String, String> getParameters(HttpServletRequest request, boolean transcoding) {
        Map<String, String> params = new HashMap<String, String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            try {
                if(transcoding){
                    valueStr = new String(valueStr.getBytes("ISO-8859-1"), "UTF-8");
                }
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(),e);
            }
            params.put(name, valueStr);
        }
        return params;
    }
}
