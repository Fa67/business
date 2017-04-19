/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.recharge.controller;

import java.io.IOException;
import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.recharge.service.RechargeService;

/**
 *                       
 * @Filename: RechargeController.java
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
@RequestMapping("/pay/recharge")
@Scope("prototype")
public class RechargeController extends BaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(RechargeController.class);

    @Autowired
    private RechargeService rechargeService;

    /**
     * 余额充值
     * @param request
     * @param params
     * @return
     * @throws AppException
     */
    @RequestMapping("/rechargeBalance")
    public void rechargeBalance(HttpServletRequest request, HttpServletResponse response, BigDecimal amount, String payType) throws AppException {
        SessionUserInfo sessionUserInfo = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try {
            if(amount == null || StringUtils.isBlank(payType)){
                response.sendRedirect(request.getContextPath() + "/#/error");
            }else{
                String form = rechargeService.createRecharge(sessionUserInfo.getCusId(), sessionUserInfo.getUserId(), amount, payType);
                if(logger.isDebugEnabled()){
                    logger.debug(form);
                }
                response.getWriter().write(form);
                response.getWriter().close();
            }
        } catch (Exception e) {
            logger.error("充值失败", e);
            try {
                response.sendRedirect(request.getContextPath() + "/#/error");
            } catch (IOException e1) {
                //ignore e1
            }
        }
    }

}
