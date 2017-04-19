package com.eayun.balancechange.service;

import java.math.BigDecimal;

/**
 * 欠费记录补齐Service
 *
 * @Filename: PaybackService.java
 * @Description:
 * @Version: 1.0
 * @Author: zhangfan
 * @Email: fan.zhang@eayun.com
 * @History:<br> <li>Date: 2016年8月12日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
public interface PaybackService {
    /**
     * 补齐客户的欠电费记录
     * @param cusId
     * @param revenue
     */
    void doPayback(String cusId, BigDecimal revenue);
}
