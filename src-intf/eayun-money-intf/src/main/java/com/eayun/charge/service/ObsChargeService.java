package com.eayun.charge.service;


import com.eayun.charge.model.ObsStatsBean;

import java.math.BigDecimal;

/**
 * 对象存储执行扣费的扣费Service
 *
 * @Filename: ObsChargeService.java
 * @Description:
 * @Version: 1.0
 * @Author: zhangfan
 * @Email: fan.zhang@eayun.com
 * @History:<br> <li>Date: 2016年8月10日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
public interface ObsChargeService {

    BigDecimal doCharge(ObsStatsBean obsStats)throws Exception;
}
