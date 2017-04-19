/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.invoice.service.impl;

import static com.eayun.common.constant.Invoice.InvoiceInfo.MAX_CREATEABLE_COUNT;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.eayun.common.constant.Invoice.InvoiceInfo.DefaultItem;
import com.eayun.common.exception.AppException;
import com.eayun.common.util.BeanUtils;
import com.eayun.file.service.FileService;
import com.eayun.invoice.dao.InvoiceInfoDao;
import com.eayun.invoice.model.BaseInvoiceInfo;
import com.eayun.invoice.model.InvoiceInfo;
import com.eayun.invoice.service.InvoiceInfoService;

/**
 *                       
 * @Filename: InvoiceInfoServiceImpl.java
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
public class InvoiceInfoServiceImpl implements InvoiceInfoService {

    @Autowired
    private InvoiceInfoDao invoiceInfoDao;
    
    @Autowired
    private FileService fileService;

    public void addInvoiceInfo(InvoiceInfo invoiceInfo) throws Exception {
        long existsCount = invoiceInfoDao.countByCusId(invoiceInfo.getCusId());
        if(existsCount >= MAX_CREATEABLE_COUNT){
            throw new AppException("开票信息已达到最大创建数", new String[]{"开票信息已达到最大创建数"});
        }
        BaseInvoiceInfo baseInvoiceInfo = new BaseInvoiceInfo();
        BeanUtils.copyPropertiesByModel(baseInvoiceInfo, invoiceInfo);
        if (containsInvoiceInfo(invoiceInfo.getCusId())) {
            baseInvoiceInfo.setDefaultItem(DefaultItem.NO);
        } else {
            baseInvoiceInfo.setDefaultItem(DefaultItem.YES);
        }
        Date date = new Date();
        baseInvoiceInfo.setCreateTime(date);
        baseInvoiceInfo.setUpdateTime(date);
        invoiceInfoDao.save(baseInvoiceInfo);
    }
    
    public List<InvoiceInfo> getInvoiceInfoList(String cusId) {
        List<BaseInvoiceInfo> baselist = invoiceInfoDao.findByCusIdOrderByCreateTimeDesc(cusId);
        List<InvoiceInfo> resultList = new ArrayList<InvoiceInfo>();
        if(CollectionUtils.isNotEmpty(baselist)){
            InvoiceInfo invoiceInfo = null;
            for (BaseInvoiceInfo baseInvoiceInfo : baselist) {
                invoiceInfo = new InvoiceInfo();
                BeanUtils.copyPropertiesByModel(invoiceInfo, baseInvoiceInfo);
                resultList.add(invoiceInfo);
            }
        }
        return resultList;
    }

    public void updateInvoiceInfo(InvoiceInfo invoiceInfo) {
        BaseInvoiceInfo baseInvoiceInfo = new BaseInvoiceInfo();
        BeanUtils.copyPropertiesByModel(baseInvoiceInfo, invoiceInfo);
        baseInvoiceInfo.setUpdateTime(new Date());
        invoiceInfoDao.saveOrUpdate(baseInvoiceInfo);
    }

    public InvoiceInfo getDefaultInvoiceInfo(String cusId) {
        BaseInvoiceInfo defaultBaseInvoiceInfo = getDefaultBaseInvoiceInfo(cusId);
        if(defaultBaseInvoiceInfo == null){
            return null;
        }
        InvoiceInfo defaultInvoiceInfo = new InvoiceInfo();
        BeanUtils.copyPropertiesByModel(defaultInvoiceInfo, defaultBaseInvoiceInfo);
        return defaultInvoiceInfo;
    }
    
    protected BaseInvoiceInfo getDefaultBaseInvoiceInfo(String cusId) {
        List<BaseInvoiceInfo> list = invoiceInfoDao.findDefaultInvoiceInfoByCusId(cusId);
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    public void setDefaultInvoiceInfo(String id) {
        BaseInvoiceInfo destDefaultInvoiceInfo = invoiceInfoDao.findOne(id);
        BaseInvoiceInfo originDefaultInvoiceInfo = getDefaultBaseInvoiceInfo(destDefaultInvoiceInfo.getCusId());
        if (originDefaultInvoiceInfo != null && !originDefaultInvoiceInfo.getId().equals(destDefaultInvoiceInfo.getId())) {
            originDefaultInvoiceInfo.setDefaultItem(DefaultItem.NO);
            invoiceInfoDao.save(originDefaultInvoiceInfo);
        }
        destDefaultInvoiceInfo.setDefaultItem(DefaultItem.YES);
        invoiceInfoDao.save(destDefaultInvoiceInfo);
    }
    
    public void deleteInvoiceInfo(String id) {
        BaseInvoiceInfo destInvoiceInfo = invoiceInfoDao.findOne(id);
        //如果被删除的开票信息是默认开票信息，查找最早的一个开票信息设置为默认
        if(DefaultItem.YES.equals(destInvoiceInfo.getDefaultItem())){
            setTheOldestAsDefaultItem(id, destInvoiceInfo.getCusId());
        }
        invoiceInfoDao.delete(id);
    }
    /**
     * 重置最早的开票信息为默认开票信息
     * @param deletedId
     * @param cusId
     */
    protected void setTheOldestAsDefaultItem(String deletedId, String cusId){
        List<BaseInvoiceInfo> ascList = invoiceInfoDao.findByCusIdOrderByCreateTimeAsc(cusId);
        if(CollectionUtils.isNotEmpty(ascList)){
            for (BaseInvoiceInfo baseInvoiceInfo : ascList) {
                if(!StringUtils.equals(baseInvoiceInfo.getId(), deletedId)){
                    baseInvoiceInfo.setDefaultItem(DefaultItem.YES);
                    invoiceInfoDao.saveOrUpdate(baseInvoiceInfo);
                    break;
                }
            }
        }
    }
    
    public String uploadImageFile(MultipartFile file, String userAccount) throws Exception {
        return fileService.uploadFile(file, userAccount);
    }
    
    protected boolean containsInvoiceInfo(String cusId) {
        return invoiceInfoDao.countByCusId(cusId).longValue() > 0 ? true : false;
    }
}
