/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.invoice.controller;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.eayun.common.ConstantClazz;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.BeanUtils;
import com.eayun.file.service.FileService;
import com.eayun.invoice.model.InvoiceInfo;
import com.eayun.invoice.service.InvoiceInfoService;
import com.eayun.log.service.LogService;

/**
 *                       
 * @Filename: InvoiceInfoController.java
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
@RequestMapping("/invoice/info")
@Scope("prototype")
public class InvoiceInfoController {

    private static final String LOG_INVOICE_INFO_ADD     = "创建开票信息";

    private static final String LOG_INVOICE_INFO_UPDATE  = "编辑开票信息";

    private static final String LOG_INVOICE_INFO_DEFAULT = "设置默认信息";

    private static final String LOG_INVOICE_INFO_DEL     = "删除开票信息";

    private final static Logger logger                   = LoggerFactory.getLogger(InvoiceInfoController.class);

    @Autowired
    private InvoiceInfoService  invoiceInfoService;

    @Autowired
    private FileService         fileService;

    @Autowired
    private LogService          logService;

    /**
     * 上传图片附件
     * @param request
     * @return
     */
    @RequestMapping("/uploadimagefile")
    @ResponseBody
    public Object uploadImageFile(MultipartHttpServletRequest request) {
        EayunResponseJson resultJson = new EayunResponseJson();
        SessionUserInfo sessionUser = getSessionUserInfo(request);
        Iterator<String> fileNames = request.getFileNames();
        if (fileNames != null && fileNames.hasNext()) {
            try {
                String fileId = invoiceInfoService.uploadImageFile(request.getFile(fileNames.next()), sessionUser.getUserId());
                resultJson.setData(fileId);
                resultJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                resultJson.setRespCode(ConstantClazz.ERROR_CODE);
                resultJson.setMessage("上传失败");
            }
        } else {
            resultJson.setRespCode(ConstantClazz.ERROR_CODE);
            resultJson.setMessage("请选择文件");
        }
        return resultJson;
    }

    /**
     * 通过fileId预览图片，返回图片的文件流
     * @param fileId
     * @param response
     * @throws Exception
     */
    @RequestMapping("/image")
    public void image(String fileId, HttpServletResponse response) throws Exception {
        InputStream input = fileService.downloadFile(fileId);
        ServletOutputStream output = response.getOutputStream();
        IOUtils.copy(input, output);
        IOUtils.closeQuietly(input);
        IOUtils.closeQuietly(input);
    }

    /**
     * 添加开票信息
     * @param params
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/addinvoiceinfo")
    @ResponseBody
    public Object addInvoiceInfo(@RequestBody Map<String, Object> params, HttpServletRequest request) throws Exception {
        try {
            InvoiceInfo invoiceInfo = new InvoiceInfo();
            BeanUtils.mapToBean(invoiceInfo, params);
            invoiceInfo.setCusId(getSessionUserInfo(request).getCusId());
            invoiceInfoService.addInvoiceInfo(invoiceInfo);
            logService.addLog(LOG_INVOICE_INFO_ADD, ConstantClazz.LOG_TYPE_INVOICE, null, null, ConstantClazz.LOG_STATU_SUCCESS, null);
            return newResultJson(null, ConstantClazz.SUCCESS_CODE, null);
        } catch (Exception e) {
            logger.error("创建开票信息失败", e);
            logService.addLog(LOG_INVOICE_INFO_ADD, ConstantClazz.LOG_TYPE_INVOICE, null, null, ConstantClazz.LOG_STATU_ERROR, e);
            return newResultJson(null, ConstantClazz.ERROR_CODE, "创建开票信息失败");
        }
    }

    /**
     * 修改开票信息
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping("/updateinvoiceinfo")
    @ResponseBody
    public Object updateInvoiceInfo(@RequestBody Map<String, Object> params) throws Exception {
        try {
            InvoiceInfo invoiceInfo = new InvoiceInfo();
            BeanUtils.mapToBean(invoiceInfo, params);
            invoiceInfoService.updateInvoiceInfo(invoiceInfo);
            logService.addLog(LOG_INVOICE_INFO_UPDATE, ConstantClazz.LOG_TYPE_INVOICE, null, null, ConstantClazz.LOG_STATU_SUCCESS, null);
            return newResultJson(null, ConstantClazz.SUCCESS_CODE, null);
        } catch (Exception e) {
            logger.error("修改开票信息失败", e);
            logService.addLog(LOG_INVOICE_INFO_UPDATE, ConstantClazz.LOG_TYPE_INVOICE, null, null, ConstantClazz.LOG_STATU_ERROR, e);
            return newResultJson(null, ConstantClazz.ERROR_CODE, "修改开票信息失败");
        }
    }

    /**
     * 获取开票信息列表
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/getinvoiceinfolist")
    @ResponseBody
    public Object getInvoiceInfoList(HttpServletRequest request) throws Exception {
        List<InvoiceInfo> resultList = invoiceInfoService.getInvoiceInfoList(getSessionUserInfo(request).getCusId());
        return newResultJson(resultList, ConstantClazz.SUCCESS_CODE, null);
    }

    /**
     * 删除开票信息
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping("/deleteinvoiceinfo")
    @ResponseBody
    public Object deleteInvoiceInfo(@RequestBody Map<String, String> params) throws Exception {
        try {
            invoiceInfoService.deleteInvoiceInfo(params.get("id"));
            logService.addLog(LOG_INVOICE_INFO_DEL, ConstantClazz.LOG_TYPE_INVOICE, null, null, ConstantClazz.LOG_STATU_SUCCESS, null);
            return newResultJson(null, ConstantClazz.SUCCESS_CODE, null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logService.addLog(LOG_INVOICE_INFO_DEL, ConstantClazz.LOG_TYPE_INVOICE, null, null, ConstantClazz.LOG_STATU_ERROR, e);
            return newResultJson(null, ConstantClazz.ERROR_CODE, null);
        }
    }

    /**
     * 设置默认开票信息
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping("/setdefaultinvoiceinfo")
    @ResponseBody
    public Object setDefaultInvoiceInfo(@RequestBody Map<String, String> params) throws Exception {
        try {
            invoiceInfoService.setDefaultInvoiceInfo(params.get("id"));
            logService.addLog(LOG_INVOICE_INFO_DEFAULT, ConstantClazz.LOG_TYPE_INVOICE, null, null, ConstantClazz.LOG_STATU_SUCCESS, null);
            return newResultJson(null, ConstantClazz.SUCCESS_CODE, null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logService.addLog(LOG_INVOICE_INFO_DEFAULT, ConstantClazz.LOG_TYPE_INVOICE, null, null, ConstantClazz.LOG_STATU_ERROR, e);
            return newResultJson(null, ConstantClazz.ERROR_CODE, null);
        }
    }

    /**
     * 获取默认开票信息
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/getdefaultinvoiceinfo")
    @ResponseBody
    public Object getDefaultInvoiceInfo(HttpServletRequest request) throws Exception {
        InvoiceInfo defaultInvoiceInfo = invoiceInfoService.getDefaultInvoiceInfo(getSessionUserInfo(request).getCusId());
        return newResultJson(defaultInvoiceInfo, ConstantClazz.SUCCESS_CODE, null);
    }

    /**
     * 获取客户的公司名称
     * @param request
     * @return
     */
    @RequestMapping("/getcuscpname")
    @ResponseBody
    public Object getCusCpName(HttpServletRequest request) {
        SessionUserInfo sessionUserInfo = getSessionUserInfo(request);
        return newResultJson(sessionUserInfo.getCusCpname(), ConstantClazz.SUCCESS_CODE, null);
    }

    protected SessionUserInfo getSessionUserInfo(HttpServletRequest request) {
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
