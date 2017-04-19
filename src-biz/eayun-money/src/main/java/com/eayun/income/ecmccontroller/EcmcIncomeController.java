/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.income.ecmccontroller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.util.BeanUtils;
import com.eayun.income.bean.IncomeInfoSearchParams;
import com.eayun.income.service.EcmcIncomeService;

/**
 *                       
 * @Filename: EcmcIncomeController.java
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
@Controller
@RequestMapping("/ecmc/income")
public class EcmcIncomeController {

    private final static Logger logger = LoggerFactory.getLogger(EcmcIncomeController.class);

    @Autowired
    private EcmcIncomeService   ecmcIncomeService;

    @RequestMapping("/queryincomeinfo")
    @ResponseBody
    public Object queryIncomeInfo(HttpServletRequest request, @RequestBody ParamsMap paramsMap) throws Exception {
        logger.info("查询收入信息");
        QueryMap queryMap = new QueryMap();
        Map<String, Object> params = paramsMap.getParams();
        queryMap.setPageNum(paramsMap.getPageNumber() == null ? 1 : paramsMap.getPageNumber());
        if (paramsMap.getPageSize() != null) {
            queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
        }
        IncomeInfoSearchParams searchParams = new IncomeInfoSearchParams();
        BeanUtils.mapToBean(searchParams, params);
        return ecmcIncomeService.queryIncomeInfo(queryMap, searchParams);
    }

    @RequestMapping("exportexcel")
    public void exportExcel(HttpServletRequest request, @RequestBody ParamsMap paramsMap) {
        
    }

}
