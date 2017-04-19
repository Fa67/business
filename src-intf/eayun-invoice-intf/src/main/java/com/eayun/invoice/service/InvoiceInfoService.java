/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.invoice.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.eayun.invoice.model.BaseInvoiceInfo;
import com.eayun.invoice.model.InvoiceInfo;

/**
 *                       
 * @Filename: InvoiceInfoService.java
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
public interface InvoiceInfoService {
    
    /**
     * 添加开票信息
     * @param invoiceInfo
     * @throws Exception
     */
    public void addInvoiceInfo(InvoiceInfo invoiceInfo) throws Exception;

    /**
     * 获取开票信息列表
     * @param cusId
     * @return
     */
    public List<InvoiceInfo> getInvoiceInfoList(String cusId);

    /**
     * 修改开票信息
     * @param invoiceInfo
     */
    public void updateInvoiceInfo(InvoiceInfo invoiceInfo);

    /**
     * 获取默认开票信息
     * @param cusId
     * @return
     */
    public InvoiceInfo getDefaultInvoiceInfo(String cusId);

    /**
     * 设置默认开票信息
     * @param id
     */
    public void setDefaultInvoiceInfo(String id);

    /**
     * 删除开票信息
     * @param id
     */
    public void deleteInvoiceInfo(String id);

    /**
     * 上传图片附件
     * @param file
     * @param userAccount
     * @return
     * @throws Exception
     */
    public String uploadImageFile(MultipartFile file, String userAccount) throws Exception;

}
