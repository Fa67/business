/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.invoice.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eayun.common.ConstantClazz;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.BeanUtils;
import com.eayun.customer.serivce.UserService;
import com.eayun.invoice.model.InvoiceDeliveryAddress;
import com.eayun.invoice.service.DeliveryAddressService;
import com.eayun.log.service.LogService;

/**
 *                       
 * @Filename: DeliveryAddressController.java
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
@RequestMapping("/invoice/address")
@Scope("prototype")
public class DeliveryAddressController {

    /**
     *Comment for <code>PARAM_KEY_ID</code>
     */
    private static final String    PARAM_KEY_ID           = "id";

    /**
     *Comment for <code>LOG_ADDRESS_DELETE</code>
     */
    private static final String    LOG_ADDRESS_DELETE     = "删除邮寄地址";

    /**
     *Comment for <code>LOG_ADDRESS_DEFAULT</code>
     */
    private static final String    LOG_ADDRESS_SETDEFAULT = "设置默认地址";

    /**
     *Comment for <code>LOG_ADDRESS_UPDATE</code>
     */
    private static final String    LOG_ADDRESS_UPDATE     = "编辑邮寄地址";

    /**
     *Comment for <code>LOG_ADDRESS_ADD</code>
     */
    private static final String    LOG_ADDRESS_ADD        = "创建邮寄地址";

    private final static Logger    logger                 = LoggerFactory.getLogger(DeliveryAddressController.class);

    @Autowired
    private DeliveryAddressService addressService;

    @Autowired
    private LogService             logService;

    @Autowired
    private UserService            userService;

    /**
     * 添加邮寄地址
     * @param params
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/adddeliveryaddress")
    @ResponseBody
    public Object addDeliveryAddress(@RequestBody Map<String, Object> params, HttpServletRequest request) throws Exception {
        try {
            InvoiceDeliveryAddress address = new InvoiceDeliveryAddress();
            BeanUtils.mapToBean(address, params);
            address.setCusId(getSessionUserInfo(request).getCusId());
            addressService.addDeliveryAddress(address);
            logService.addLog(LOG_ADDRESS_ADD, ConstantClazz.LOG_TYPE_INVOICE, null, null, ConstantClazz.LOG_STATU_SUCCESS, null);
            return newResultJson(null, ConstantClazz.SUCCESS_CODE, "创建邮寄地址成功");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logService.addLog(LOG_ADDRESS_ADD, ConstantClazz.LOG_TYPE_INVOICE, null, null, ConstantClazz.LOG_STATU_SUCCESS, e);
            return newResultJson(null, ConstantClazz.ERROR_CODE, "创建邮寄地址失败");
        }
    }

    /**
     * 获取客户邮寄地址列表
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/getdeliveryaddresslist")
    @ResponseBody
    public Object getDeliveryAddressList(HttpServletRequest request) throws Exception {
        List<InvoiceDeliveryAddress> deliveryAddressList = addressService.getDeliveryAddressList(getSessionUserInfo(request).getCusId());
        return newResultJson(deliveryAddressList, ConstantClazz.SUCCESS_CODE, null);
    }

    /**
     * 修改邮寄地址
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping("/updatedeliveryaddress")
    @ResponseBody
    public Object updateDeliveryAddress(@RequestBody Map<String, Object> params) throws Exception {
        try {
            InvoiceDeliveryAddress address = new InvoiceDeliveryAddress();
            BeanUtils.mapToBean(address, params);
            addressService.updateDeliveryAddress(address);
            logService.addLog(LOG_ADDRESS_UPDATE, ConstantClazz.LOG_TYPE_INVOICE, null, null, ConstantClazz.LOG_STATU_SUCCESS, null);
            return newResultJson(true, ConstantClazz.SUCCESS_CODE, "更新邮寄地址成功");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logService.addLog(LOG_ADDRESS_UPDATE, ConstantClazz.LOG_TYPE_INVOICE, null, null, ConstantClazz.LOG_STATU_ERROR, e);
            return newResultJson(false, ConstantClazz.ERROR_CODE, "更新邮寄地址失败");
        }
    }

    /**
     * 设置默认邮寄地址
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping("/setdefaultdeliveryaddress")
    @ResponseBody
    public Object setDefaultDeliveryAddress(@RequestBody Map<String, String> params) throws Exception {
        try {
            addressService.setDefaultDeliveryAddress(params.get(PARAM_KEY_ID));
            logService.addLog(LOG_ADDRESS_SETDEFAULT, ConstantClazz.LOG_TYPE_INVOICE, null, null, ConstantClazz.LOG_STATU_SUCCESS, null);
            return newResultJson(true, ConstantClazz.SUCCESS_CODE, "更新邮寄地址成功");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logService.addLog(LOG_ADDRESS_SETDEFAULT, ConstantClazz.LOG_TYPE_INVOICE, null, null, ConstantClazz.LOG_STATU_ERROR, e);
            return newResultJson(false, ConstantClazz.ERROR_CODE, "设置默认地址失败");
        }
    }

    /**
     * 删除邮寄地址
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping("/deletedeliveryaddress")
    @ResponseBody
    public Object deleteDeliveryAddress(@RequestBody Map<String, String> params) throws Exception {
        try {
            addressService.deleteDeliveryAddress(params.get(PARAM_KEY_ID));
            logService.addLog(LOG_ADDRESS_DELETE, ConstantClazz.LOG_TYPE_INVOICE, null, null, ConstantClazz.LOG_STATU_SUCCESS, null);
            return newResultJson(true, ConstantClazz.SUCCESS_CODE, "删除邮寄地址成功");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logService.addLog(LOG_ADDRESS_DELETE, ConstantClazz.LOG_TYPE_INVOICE, null, null, ConstantClazz.LOG_STATU_ERROR, e);
            return newResultJson(false, ConstantClazz.ERROR_CODE, "删除邮寄地址失败");
        }
    }

    /**
     * 获取默认邮寄地址
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/getdefaultdeliveryaddress")
    @ResponseBody
    public Object getDefaultDeliveryAddress(HttpServletRequest request) throws Exception {
        String cusId = getSessionUserInfo(request).getCusId();
        InvoiceDeliveryAddress defaultDeliveryAddress = addressService.getDefaultDeliveryAddress(cusId);
        return newResultJson(defaultDeliveryAddress, ConstantClazz.SUCCESS_CODE, null);
    }
    
    /**
     * 获取客户的管理员信息
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/getadmininfo")
    @ResponseBody
    public Object getAdminInfo(HttpServletRequest request) throws Exception {
        String cusId = getSessionUserInfo(request).getCusId();
        return newResultJson(userService.queryAdminByCusId(cusId), ConstantClazz.SUCCESS_CODE, null);
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
