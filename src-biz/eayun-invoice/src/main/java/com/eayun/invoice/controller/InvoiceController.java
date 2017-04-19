/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.invoice.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.BeanUtils;
import com.eayun.invoice.model.InvoiceApply;
import com.eayun.invoice.model.InvoiceBillableAmount;
import com.eayun.invoice.service.InvoiceService;
import com.eayun.log.service.LogService;

/**
 *                       
 * @Filename: InvoiceController.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2017年2月27日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Controller
@RequestMapping("/invoice")
@Scope("prototype")
public class InvoiceController {

    private static final String LOG_INVOICE_CANCEL = "取消开票申请";

    private static final String LOG_INVOICE_APPLY  = "申请发票";

    private final static Logger logger             = LoggerFactory.getLogger(InvoiceController.class);

    @Autowired
    private InvoiceService      invoiceService;

    @Autowired
    private LogService          logService;

    /**
     * 获取可开票金额
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/getbillableamount")
    public EayunResponseJson getBillableAmount(HttpServletRequest request) {
        String cusId = getUserInfo(request).getCusId();
        InvoiceBillableAmount billableAmount = invoiceService.getBillableAmount(cusId);
        EayunResponseJson resultJson = new EayunResponseJson();
        resultJson.setData(billableAmount);
        resultJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        return resultJson;
    }

    /**
     * 查询开票申请列表
     * @param paramsMap
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryinvoiceapply")
    @ResponseBody
    public Object queryInvoiceApply(@RequestBody ParamsMap paramsMap, HttpServletRequest request) throws Exception {
        logger.info("查询开票申请");
        try {
            QueryMap queryMap = new QueryMap();
            Map<String, Object> params = paramsMap.getParams();
            queryMap.setPageNum(paramsMap.getPageNumber() == null ? 1 : paramsMap.getPageNumber());
            if (paramsMap.getPageSize() != null) {
                queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
            }
            String cusId = getCusId(request);
            String startTime = MapUtils.getString(params, "startTime");
            String endTime = MapUtils.getString(params, "endTime");
            String status = MapUtils.getString(params, "status");
            return invoiceService.queryInvoiceApplys(queryMap, cusId, status, startTime, endTime);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 申请开票
     * @param params
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/applyinvoice")
    @ResponseBody
    public Object applyInvoice(@RequestBody Map<String, Object> params, HttpServletRequest request) throws Exception {
        logger.info("添加开票申请");
        InvoiceApply invoiceApply = new InvoiceApply();
        try {
            BeanUtils.mapToBean(invoiceApply, params);
            invoiceApply.setCusId(getCusId(request));
            invoiceService.addInvoiceApply(invoiceApply);
            String resourceName = getResourceName(invoiceApply);
            logService.addLog(LOG_INVOICE_APPLY, ConstantClazz.LOG_TYPE_INVOICE, resourceName, null, ConstantClazz.LOG_STATU_SUCCESS, null);
            return newResultJson(true, ConstantClazz.SUCCESS_CODE, "申请开发票成功");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            String resourceName = getResourceName(invoiceApply);
            logService.addLog(LOG_INVOICE_APPLY, ConstantClazz.LOG_TYPE_INVOICE, resourceName, null, ConstantClazz.LOG_STATU_ERROR, e);
            return newResultJson(false, ConstantClazz.ERROR_CODE, e.getMessage());
        }
    }

    /**
     * 取消开票申请
     * @param params
     * @return
     */
    @RequestMapping("/cancelapply")
    @ResponseBody
    public Object cancelApply(@RequestBody Map<String, String> params) {
        logger.info("取消开票申请");
        String id = params.get("id");
        InvoiceApply invoiceApply = invoiceService.getInvoiceApply(id);
        if (invoiceApply == null) {
            logService.addLog(LOG_INVOICE_APPLY, ConstantClazz.LOG_TYPE_INVOICE, "找不到申请", null, ConstantClazz.LOG_STATU_ERROR, null);
            return newResultJson(false, ConstantClazz.ERROR_CODE, "找不到该开票申请");
        }
        String resourceName = getResourceName(invoiceApply);
        try {
            invoiceService.cancelInvoiceApply(id);
            logService.addLog(LOG_INVOICE_CANCEL, ConstantClazz.LOG_TYPE_INVOICE, resourceName, null, ConstantClazz.LOG_STATU_SUCCESS, null);
            return newResultJson(true, ConstantClazz.SUCCESS_CODE, "取消发票成功");
        } catch (Exception e) {
            logService.addLog(LOG_INVOICE_APPLY, ConstantClazz.LOG_TYPE_INVOICE, resourceName, null, ConstantClazz.LOG_STATU_ERROR, e);
            return newResultJson(false, ConstantClazz.ERROR_CODE, e.getMessage());
        }
    }

    /**
     * 获取可开票金额下限
     * @return
     * @throws Exception
     */
    @RequestMapping("/getbillablelowerlimit")
    @ResponseBody
    public Object getBillableLowerLimit() throws Exception {
        String lowerLimit = invoiceService.getBillableLowerLimit();
        return newResultJson(lowerLimit, ConstantClazz.SUCCESS_CODE, null);
    }

    protected String getResourceName(InvoiceApply invoiceApply) {
        return invoiceApply == null ? "" : invoiceApply.getInvoiceTypeName() + "_￥" + invoiceApply.getAmount().setScale(2);
    }

    protected String getCusId(HttpServletRequest request) {
        return getUserInfo(request).getCusId();
    }

    protected SessionUserInfo getUserInfo(HttpServletRequest request) {
        return (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    }

    protected EayunResponseJson newResultJson(Object data, String code, String message) {
        EayunResponseJson eayunResponseJson = new EayunResponseJson();
        eayunResponseJson.setData(data);
        eayunResponseJson.setRespCode(code);
        eayunResponseJson.setMessage(message);
        return eayunResponseJson;
    }
}
