package com.eayun.charge.service;

import com.eayun.charge.model.ChargeRecord;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 根据计费清单执行扣费的扣费Service
 *
 * @Filename: ChargeService.java
 * @Description:
 * @Version: 1.0
 * @Author: zhangfan
 * @Email: fan.zhang@eayun.com
 * @History:<br> <li>Date: 2016年8月3日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
public interface ChargeService {
    /**
     * 计费
     * @param currentTime
     * @param chargeRecord
     * @return
     */
    BigDecimal doCharge(Date currentTime, ChargeRecord chargeRecord) ;
}
