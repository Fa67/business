/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.invoice.ecmccontroller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.Invoice;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.BeanUtils;
import com.eayun.invoice.ecmcservice.EcmcInvoiceService;
import com.eayun.invoice.model.InvoiceApply;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.sys.model.SysDataTree;

/**
 *                       
 * @Filename: EcmcInvoiceController.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2017年3月10日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Controller
@RequestMapping("/ecmc/invoice")
public class EcmcInvoiceController {

    /**
     *Comment for <code>ID</code>
     */
    private static final String PARAM_KEY_ID       = "id";

    /**
     *Comment for <code>LOG_INVOICE_CANCEL</code>
     */
    private static final String LOG_INVOICE_CANCEL = "取消发票";

    /**
     *Comment for <code>UNDERLINE</code>
     */
    private static final String UNDERLINE          = "_";

    private static final String COIN               = "￥";

    private static final String LOG_INVOICE_CHANGE = "更改发票状态 ";

    private final static Logger logger             = LoggerFactory.getLogger(EcmcInvoiceController.class);

    @Autowired
    private EcmcInvoiceService  ecmcInvoiceService;

    @Autowired
    private EcmcLogService      logService;

    /**
     * 查询开票申请列表
     * @param paramsMap
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryinvoiceapplys")
    @ResponseBody
    public Object queryInvoiceApplys(@RequestBody ParamsMap paramsMap) throws Exception {
        try {
            QueryMap queryMap = new QueryMap();
            Map<String, Object> params = paramsMap.getParams();
            queryMap.setPageNum(paramsMap.getPageNumber() == null ? 1 : paramsMap.getPageNumber());
            if (paramsMap.getPageSize() != null) {
                queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
            }
            String startTime = MapUtils.getString(params, "startTime");
            String endTime = MapUtils.getString(params, "endTime");
            String status = MapUtils.getString(params, "status");
            String cusName = MapUtils.getString(params, "cusName");
            return ecmcInvoiceService.queryInvoiceApplys(queryMap, cusName, status, startTime, endTime);
        } catch (Exception e) {
            logger.error("查询开票申请失败", e);
            throw e;
        }
    }

    /**
     * 获取开票可选的快递公司名称列表
     * @return
     * @throws Exception
     */
    @RequestMapping("/getexpressnames")
    @ResponseBody
    public Object getExpressNames() throws Exception {
        logger.info("获取快递公司名称");
        List<SysDataTree> resultList = ecmcInvoiceService.getExpressNames();
        return newResultJson(resultList, ConstantClazz.SUCCESS_CODE, null);
    }

    /**
     * 获取取消开票可选的取消原因
     * @return
     * @throws Exception
     */
    @RequestMapping("/getcancelreasons")
    @ResponseBody
    public Object getCancelReasons() throws Exception {
        logger.info("获取取消开票原因列表");
        List<SysDataTree> resultList = ecmcInvoiceService.getCancelReasons();
        return newResultJson(resultList, ConstantClazz.SUCCESS_CODE, null);
    }

    /**
     * 获取开票申请详情
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping("/getinvoiceapplydetail")
    @ResponseBody
    public Object getInvoiceApplyDetail(@RequestBody Map<String, String> params) throws Exception {
        InvoiceApply invoiceApplyDetail = ecmcInvoiceService.getInvoiceApplyDetail(params.get(PARAM_KEY_ID));
        return newResultJson(invoiceApplyDetail, ConstantClazz.SUCCESS_CODE, null);
    }

    /**
     * 更改状态 ->处理中
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping("/changetoprocessing")
    @ResponseBody
    public Object changeToProcessing(@RequestBody Map<String, Object> params) throws Exception {
        InvoiceApply invoiceApply = new InvoiceApply();
        BeanUtils.mapToBean(invoiceApply, params);
        String resourceName = getChangeStatusResourceName(invoiceApply.getInvoiceTypeName(), invoiceApply.getAmount(), Invoice.StatusName.PROCESSING);
        try {
            ecmcInvoiceService.changeToProcessing((String) params.get(PARAM_KEY_ID));
            logService.addLog(LOG_INVOICE_CHANGE, ConstantClazz.LOG_TYPE_INVOICE, resourceName, null, 1, null, null);
            return newResultJson(true, ConstantClazz.SUCCESS_CODE, null);
        } catch (Exception e) {
            logService.addLog(LOG_INVOICE_CHANGE, ConstantClazz.LOG_TYPE_INVOICE, resourceName, null, 0, null, e);
            return newResultJson(false, ConstantClazz.ERROR_CODE, e.getMessage());
        }
    }

    /**
     * 更改状态 -> 已开票
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping("/changetoinvoiced")
    @ResponseBody
    public Object changeToInvoiced(@RequestBody Map<String, Object> params) throws Exception {
        InvoiceApply invoiceApply = new InvoiceApply();
        BeanUtils.mapToBean(invoiceApply, params);
        String resourceName = getChangeStatusResourceName(invoiceApply.getInvoiceTypeName(), invoiceApply.getAmount(), Invoice.StatusName.INVOICED);
        try {
            ecmcInvoiceService.changeToInvoiced(invoiceApply);
            logService.addLog(LOG_INVOICE_CHANGE, ConstantClazz.LOG_TYPE_INVOICE, resourceName, null, 1, null, null);
            return newResultJson(true, ConstantClazz.SUCCESS_CODE, null);
        } catch (Exception e) {
            logService.addLog(LOG_INVOICE_CHANGE, ConstantClazz.LOG_TYPE_INVOICE, resourceName, null, 0, null, e);
            return newResultJson(false, ConstantClazz.ERROR_CODE, e.getMessage());
        }
    }

    /**
     * 取消开票
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping("/cancelapply")
    @ResponseBody
    public Object cancelApply(@RequestBody Map<String, Object> params) throws Exception {
        InvoiceApply invoiceApply = new InvoiceApply();
        BeanUtils.mapToBean(invoiceApply, params);
        String resourceName = getCancelResourceName(invoiceApply.getInvoiceTypeName(), invoiceApply.getAmount());
        try {
            ecmcInvoiceService.cancelApply(invoiceApply);
            logService.addLog(LOG_INVOICE_CANCEL, ConstantClazz.LOG_TYPE_INVOICE, resourceName, null, 1, null, null);
            return newResultJson(true, ConstantClazz.SUCCESS_CODE, null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logService.addLog(LOG_INVOICE_CANCEL, ConstantClazz.LOG_TYPE_INVOICE, resourceName, null, 0, null, e);
            return newResultJson(false, ConstantClazz.ERROR_CODE, e.getMessage());
        }
    }
    
    @RequestMapping("/download")
    public void download(String id, HttpServletResponse response) throws Exception {
        InputStream input = null;
        try {
            
            File licensesZipFile = ecmcInvoiceService.getLicensesZipFile(id);
            input = new FileInputStream(licensesZipFile);
            String filename= licensesZipFile.getName();
            response.setHeader("content-disposition", "attachment;filename=" +new String(filename.getBytes("utf-8"), "ISO8859-1"));  
            IOUtils.copy(input, response.getOutputStream());
        } catch (Exception e) {
            throw e;
        } finally {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(response.getOutputStream());
        }
    }

    protected String getChangeStatusResourceName(String invoiceTypeName, BigDecimal amount, String statusName) {
        StringBuffer sb = new StringBuffer();
        sb.append(invoiceTypeName);
        sb.append(UNDERLINE);
        sb.append(COIN);
        sb.append(amount.setScale(2));
        sb.append(UNDERLINE);
        sb.append(statusName);
        return sb.toString();
    }

    protected String getCancelResourceName(String invoiceTypeName, BigDecimal amount) {
        StringBuffer sb = new StringBuffer();
        sb.append(invoiceTypeName);
        sb.append(UNDERLINE);
        sb.append(COIN);
        sb.append(amount.setScale(2));
        return sb.toString();
    }

    protected EayunResponseJson newResultJson(Object data, String code, String message) {
        EayunResponseJson eayunResponseJson = new EayunResponseJson();
        eayunResponseJson.setData(data);
        eayunResponseJson.setRespCode(code);
        eayunResponseJson.setMessage(message);
        return eayunResponseJson;
    }

}
