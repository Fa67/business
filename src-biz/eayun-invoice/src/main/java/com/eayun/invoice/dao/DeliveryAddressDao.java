/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.invoice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.invoice.model.BaseInvoiceDeliveryAddress;

/**
 *                       
 * @Filename: DeliveryAddressDao.java
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
public interface DeliveryAddressDao extends IRepository<BaseInvoiceDeliveryAddress, String> {
    
    public Long countByCusId(String cusId);
    
    public List<BaseInvoiceDeliveryAddress> findByCusIdOrderByCreateTimeDesc(String cusId);
    
    @Query("from BaseInvoiceDeliveryAddress a where a.cusId = ? and a.defaultItem = '1'")
    public BaseInvoiceDeliveryAddress findDefaultDeliveryAddress(String cusId);
    
    public List<BaseInvoiceDeliveryAddress> findByCusIdOrderByCreateTimeAsc(String cusId);

}
