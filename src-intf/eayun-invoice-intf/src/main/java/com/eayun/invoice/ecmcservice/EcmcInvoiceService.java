/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.invoice.ecmcservice;

import java.io.File;
import java.util.List;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.invoice.model.InvoiceApply;
import com.eayun.sys.model.SysDataTree;

/**
 *                       
 * @Filename: EcmcInvoiceService.java
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
public interface EcmcInvoiceService {

    /**
     * 分页查询开票申请列表
     * @param queryMap
     * @param cusName
     * @param status
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    public Page queryInvoiceApplys(QueryMap queryMap, String cusName, String status, String startTime, String endTime) throws Exception;

    /**
     * 更改状态为处理中
     * @param id
     * @throws Exception
     */
    public void changeToProcessing(String id) throws Exception;

    /**
     * 更改状态为已开票
     * @param invoiceApply
     * @throws Exception
     */
    public void changeToInvoiced(InvoiceApply invoiceApply) throws Exception;

    /**
     * 取消开票
     * @param invoiceApply
     * @throws Exception
     */
    public void cancelApply(InvoiceApply invoiceApply) throws Exception;

    /**
     * 获取开票申请详情
     * @param id
     * @return
     * @throws Exception
     */
    public InvoiceApply getInvoiceApplyDetail(String id) throws Exception;

    /**
     * 获取系统配置的取消开票申请可选的原因列表
     * @return
     * @throws Exception
     */
    public List<SysDataTree> getCancelReasons() throws Exception;

    /**
     * 获取快递公司名称列表
     * @return
     * @throws Exception
     */
    public List<SysDataTree> getExpressNames() throws Exception;
    
    /**
     * 获取证书附件打包的zip文件
     * @param applyId
     * @return
     * @throws Exception
     */
    public File getLicensesZipFile(String applyId) throws Exception;

}