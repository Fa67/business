/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.recharge.service;

import java.math.BigDecimal;

import com.eayun.common.exception.AppException;

/**
 *                       
 * @Filename: RechargeService.java
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
public interface RechargeService {
    
    public String createRecharge(String cusId, String userId, BigDecimal amount,  String payType) throws AppException;

}
