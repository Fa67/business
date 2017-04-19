/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.income.service;

import java.io.OutputStream;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.income.bean.IncomeInfoSearchParams;

/**
 *                       
 * @Filename: EcmcIncomeService.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2017年4月14日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public interface EcmcIncomeService {

    public Page queryIncomeInfo(QueryMap queryMap, IncomeInfoSearchParams searchParams);

    public void exportIncomeInfoExcel(OutputStream out, IncomeInfoSearchParams searchParams);

}