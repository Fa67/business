/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.invoice.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.constant.Invoice.DeliverAddress.DefaultItem;
import com.eayun.common.exception.AppException;
import com.eayun.common.util.BeanUtils;
import com.eayun.invoice.dao.DeliveryAddressDao;
import com.eayun.invoice.model.BaseInvoiceDeliveryAddress;
import com.eayun.invoice.model.InvoiceDeliveryAddress;
import com.eayun.invoice.service.DeliveryAddressService;

/**
 *                       
 * @Filename: DeliveryAddressServiceImpl.java
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
@Service
@Transactional
public class DeliveryAddressServiceImpl implements DeliveryAddressService {

    @Autowired
    private DeliveryAddressDao deliveryAddressDao;

    public void addDeliveryAddress(InvoiceDeliveryAddress address) {
        BaseInvoiceDeliveryAddress baseInvoiceDeliveryAddress = new BaseInvoiceDeliveryAddress();
        BeanUtils.copyPropertiesByModel(baseInvoiceDeliveryAddress, address);
        if (hasAddress(baseInvoiceDeliveryAddress.getCusId())) {
            baseInvoiceDeliveryAddress.setDefaultItem(DefaultItem.NO);
        } else {
            baseInvoiceDeliveryAddress.setDefaultItem(DefaultItem.YES);
        }
        Date date = new Date();
        baseInvoiceDeliveryAddress.setCreateTime(date);
        baseInvoiceDeliveryAddress.setUpdateTime(date);
        deliveryAddressDao.save(baseInvoiceDeliveryAddress);
    }

    public List<InvoiceDeliveryAddress> getDeliveryAddressList(String cusId) {
        List<BaseInvoiceDeliveryAddress> baseList = deliveryAddressDao.findByCusIdOrderByCreateTimeDesc(cusId);
        if (CollectionUtils.isEmpty(baseList)) {
            return Collections.<InvoiceDeliveryAddress>emptyList();
        }
        List<InvoiceDeliveryAddress> resultList = new ArrayList<>();
        for (BaseInvoiceDeliveryAddress base : baseList) {
            InvoiceDeliveryAddress dest = new InvoiceDeliveryAddress();
            BeanUtils.copyPropertiesByModel(dest, base);
            resultList.add(dest);
        }
        return resultList;
    }

    public void updateDeliveryAddress(InvoiceDeliveryAddress address) {
        BaseInvoiceDeliveryAddress baseInvoiceDeliveryAddress = new BaseInvoiceDeliveryAddress();
        BeanUtils.copyPropertiesByModel(baseInvoiceDeliveryAddress, address);
        baseInvoiceDeliveryAddress.setUpdateTime(new Date());
        deliveryAddressDao.saveOrUpdate(baseInvoiceDeliveryAddress);
    }

    public void setDefaultDeliveryAddress(String id) throws Exception {
        BaseInvoiceDeliveryAddress destDefault = deliveryAddressDao.findOne(id);
        if (destDefault == null) {
            throw new AppException("邮寄地址不存在", new String[] { "邮寄地址不存在" });
        }
        BaseInvoiceDeliveryAddress orignDefault = deliveryAddressDao.findDefaultDeliveryAddress(destDefault.getCusId());
        if (orignDefault != null && !StringUtils.equals(orignDefault.getId(), destDefault.getId())) {
            orignDefault.setDefaultItem(DefaultItem.NO);
        }
        destDefault.setDefaultItem(DefaultItem.YES);
        deliveryAddressDao.saveOrUpdate(destDefault);
    }

    public InvoiceDeliveryAddress getDefaultDeliveryAddress(String cusId) {
        BaseInvoiceDeliveryAddress baseInvoiceDeliveryAddress = deliveryAddressDao.findDefaultDeliveryAddress(cusId);
        InvoiceDeliveryAddress result = null;
        if (baseInvoiceDeliveryAddress != null) {
            result = new InvoiceDeliveryAddress();
            BeanUtils.copyPropertiesByModel(result, baseInvoiceDeliveryAddress);
        }
        return result;
    }

    public void deleteDeliveryAddress(String id) throws Exception {
        if (StringUtils.isEmpty(id)) {
            throw new Exception("ID不能为空");
        }
        dealDefaultItemBeforeDelAddress(id);
        deliveryAddressDao.delete(id);
    }

    protected void dealDefaultItemBeforeDelAddress(String id) {
        BaseInvoiceDeliveryAddress willDelAddress = deliveryAddressDao.findOne(id);
        if(DefaultItem.YES.equals(willDelAddress.getDefaultItem())){
            setTheOldestAsDefaultItem(willDelAddress.getCusId(), id);
        }
    }
    
    protected void setTheOldestAsDefaultItem(String cusId, String currentId) {
        List<BaseInvoiceDeliveryAddress> ascList = deliveryAddressDao.findByCusIdOrderByCreateTimeAsc(cusId);
        if(CollectionUtils.isNotEmpty(ascList)){
            for (BaseInvoiceDeliveryAddress baseInvoiceDeliveryAddress : ascList) {
                if(!StringUtils.equals(baseInvoiceDeliveryAddress.getId(), currentId)){
                    baseInvoiceDeliveryAddress.setDefaultItem(DefaultItem.YES);
                    deliveryAddressDao.saveOrUpdate(baseInvoiceDeliveryAddress);
                    break;
                }
            }
        }
    }

    protected boolean hasAddress(String cusId) {
        return deliveryAddressDao.countByCusId(cusId).longValue() > 0 ? true : false;
    }
}
