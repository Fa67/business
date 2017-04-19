/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.invoice.service;

import java.util.List;

import com.eayun.invoice.model.InvoiceDeliveryAddress;

/**
 *                       
 * @Filename: DeliveryAddressService.java
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
public interface DeliveryAddressService {

    /**
     * 添加邮寄地址
     * @param address
     */
    public void addDeliveryAddress(InvoiceDeliveryAddress address);

    /**
     * 获取邮寄地址列表
     * @param cusId
     * @return
     */
    public List<InvoiceDeliveryAddress> getDeliveryAddressList(String cusId);

    /**
     * 修改邮寄地址
     * @param address
     */
    public void updateDeliveryAddress(InvoiceDeliveryAddress address);

    /**
     * 设置默认邮寄地址
     * @param id
     * @throws Exception
     */
    public void setDefaultDeliveryAddress(String id) throws Exception;

    /**
     * 获取默认邮寄地址
     * @param cusId
     * @return
     */
    public InvoiceDeliveryAddress getDefaultDeliveryAddress(String cusId);

    /**
     * 删除邮寄地址
     * @param id
     * @throws Exception
     */
    public void deleteDeliveryAddress(String id) throws Exception;

}
