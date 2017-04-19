/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.invoice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.invoice.model.BaseInvoiceInfo;

/**
 *                       
 * @Filename: InvoiceInfoDao.java
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
public interface InvoiceInfoDao extends IRepository<BaseInvoiceInfo, String> {

    @Query("from BaseInvoiceInfo info where info.cusId = ? and info.defaultItem = '1'")
    public List<BaseInvoiceInfo> findDefaultInvoiceInfoByCusId(String cusId);
    
    public Long countByCusId(String cusId);
    
    public List<BaseInvoiceInfo> findByCusIdOrderByCreateTimeDesc(String cusId);
    
    public List<BaseInvoiceInfo> findByCusIdOrderByCreateTimeAsc(String cusId);
    
}
