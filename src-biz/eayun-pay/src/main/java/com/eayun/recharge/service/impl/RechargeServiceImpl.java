/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.recharge.service.impl;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.exception.AppException;
import com.eayun.pay.service.EayunPaymentService;
import com.eayun.recharge.service.RechargeService;

/**
 *                       
 * @Filename: RechargeServiceImpl.java
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
@Service
@Transactional
public class RechargeServiceImpl implements RechargeService {
    
    @Autowired
    private EayunPaymentService eayunPaymentService;

    public String createRecharge(String cusId, String userId, BigDecimal amount,  String payType) throws AppException {
        return eayunPaymentService.createRechargeForm(cusId, userId, amount, payType);
    }

}
