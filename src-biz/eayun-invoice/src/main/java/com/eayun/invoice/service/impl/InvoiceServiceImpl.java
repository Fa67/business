/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.invoice.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.constant.Invoice;
import com.eayun.common.constant.Invoice.Status;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.common.zk.DistributedLockBean;
import com.eayun.common.zk.DistributedLockService;
import com.eayun.common.zk.LockService;
import com.eayun.invoice.dao.InvoiceApplyDao;
import com.eayun.invoice.dao.InvoiceBillableAmountDao;
import com.eayun.invoice.model.BaseInvoiceApply;
import com.eayun.invoice.model.BaseInvoiceBillableAmount;
import com.eayun.invoice.model.InvoiceApply;
import com.eayun.invoice.model.InvoiceBillableAmount;
import com.eayun.invoice.service.InvoiceService;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.sys.model.SysDataTree;

/**
 *                       
 * @Filename: InvoiceServiceImpl.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2017年2月24日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Service
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    @Autowired
    private InvoiceApplyDao          invoiceApplyDao;

    @Autowired
    private InvoiceBillableAmountDao billableAmountDao;

    @Autowired
    private MessageCenterService     messageCenterService;

    @Autowired
    DistributedLockService           distributedLockService;

    public void addInvoiceApply(final InvoiceApply apply) throws Exception {
        DistributedLockBean dlBean = new DistributedLockBean();
        dlBean.setGranularity("invoice:" + apply.getCusId());
        dlBean.setLockService(new LockService() {
            @Override
            public Object doService() throws Exception {
                processAddInvoiceApply(apply);
                return null;
            }
        });
        distributedLockService.doServiceByLock(dlBean, true);
    }

    protected void processAddInvoiceApply(InvoiceApply apply) throws Exception {
        this.validateBillableAmount(apply);
        apply.setStatus(Status.PENDING);
        apply.setCreateTime(new Date());
        BaseInvoiceApply baseInvoiceApply = new BaseInvoiceApply();
        BeanUtils.copyPropertiesByModel(baseInvoiceApply, apply);
        invoiceApplyDao.save(baseInvoiceApply);
        incrBillableFrozenAmount(apply.getCusId(), apply.getAmount());
        //发邮件通知财务
        messageCenterService.newReceiptInfo(apply.getCusId(), apply.getAmount(), apply.getInvoiceTypeName(), apply.getInvoiceTitle(), apply.getAddress(), apply.getStatusName(), apply.getCreateTime());
    }

    protected void validateBillableAmount(InvoiceApply apply) throws Exception {
        InvoiceBillableAmount amount = getBillableAmount(apply.getCusId());
        amount.getBillableAmount();
        if (amount.getBillableAmount().doubleValue() < apply.getAmount().doubleValue()) {
            throw new AppException("可开票金额不足", new String[] { "可开票金额不足" });
        }
    }

    public Page queryInvoiceApplys(QueryMap queryMap, String cusId, String status, String startTime, String endTime) throws Exception {
        StringBuffer hqlBuffer = new StringBuffer();
        List<Object> params = new ArrayList<Object>();
        hqlBuffer.append("from BaseInvoiceApply t where 1 = 1 ");
        if (!StringUtil.isEmpty(startTime)) {
            hqlBuffer.append("and t.createTime >= ? ");
            params.add(DateUtil.timestampToDate(startTime));
        }
        if (!StringUtil.isEmpty(endTime)) {
            hqlBuffer.append("and t.createTime <= ? ");
            params.add(DateUtil.addDay(DateUtil.timestampToDate(endTime), new int[] { 0, 0, 1 }));
        }
        if (!StringUtil.isEmpty(cusId)) {
            hqlBuffer.append("and t.cusId = ? ");
            params.add(cusId);
        }
        if (!StringUtil.isEmpty(status)) {
            if (StringUtils.contains(status, ",")) {
                //针对取消(用户取消、ecmc取消)
                String[] statusArr = StringUtils.split(status, ",");
                hqlBuffer.append("and (t.status = ? or t.status = ? ) ");
                params.add(statusArr[0]);
                params.add(statusArr[1]);
            } else {
                hqlBuffer.append("and t.status = ? ");
                params.add(status);
            }
        }
        hqlBuffer.append("order by t.createTime desc");
        Page page = invoiceApplyDao.pagedQuery(hqlBuffer.toString(), queryMap, params.toArray());
        @SuppressWarnings("unchecked")
        List<BaseInvoiceApply> baseResultList = (List<BaseInvoiceApply>) page.getResult();
        List<InvoiceApply> resultList = new ArrayList<InvoiceApply>();
        if (baseResultList != null && baseResultList.size() > 0) {
            for (BaseInvoiceApply baseInvoiceApply : baseResultList) {
                InvoiceApply invoiceApply = new InvoiceApply();
                BeanUtils.copyPropertiesByModel(invoiceApply, baseInvoiceApply);
                resultList.add(invoiceApply);
            }
            page.setResult(resultList);
        }
        return page;
    }

    public void cancelInvoiceApply(String id) throws Exception {
        if (StringUtils.isBlank(id)) {
            throw new AppException("找不到开票申请", new String[] { "找不到开票申请" });
        }
        BaseInvoiceApply baseInvoiceApply = invoiceApplyDao.findOne(id);
        if (baseInvoiceApply == null) {
            throw new AppException("找不到开票申请", new String[] { "找不到开票申请" });
        }
        if (!Status.PENDING.equals(baseInvoiceApply.getStatus())) {
            throw new AppException("该开票申请无法取消", new String[] { "该开票申请无法取消" });
        }
        baseInvoiceApply.setStatus(Status.CANCEL_BY_USER);
        invoiceApplyDao.saveOrUpdate(baseInvoiceApply);
        decrBillableFrozenAmount(baseInvoiceApply.getCusId(), baseInvoiceApply.getAmount());
    }

    public InvoiceApply getInvoiceApply(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        BaseInvoiceApply baseInvoiceApply = invoiceApplyDao.findOne(id);
        if (baseInvoiceApply == null) {
            return null;
        }
        InvoiceApply invoiceApply = new InvoiceApply();
        BeanUtils.copyPropertiesByModel(invoiceApply, baseInvoiceApply);
        return invoiceApply;
    }

    public InvoiceBillableAmount getBillableAmount(String cusId) {
        List<BaseInvoiceBillableAmount> list = billableAmountDao.findByCusId(cusId);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        InvoiceBillableAmount invoiceBillableAmount = new InvoiceBillableAmount();
        BeanUtils.copyPropertiesByModel(invoiceBillableAmount, list.get(0));
        return invoiceBillableAmount;
    }

    public void initBillableAmount(String cusId) {
        if (!existsBillableAmount(cusId)) {
            BaseInvoiceBillableAmount baseInvoiceBillableAmount = new BaseInvoiceBillableAmount();
            BigDecimal zeroAmount = new BigDecimal(0.00d);
            baseInvoiceBillableAmount.setCusId(cusId);
            baseInvoiceBillableAmount.setTotalAmount(zeroAmount);
            baseInvoiceBillableAmount.setInvoicedAmount(zeroAmount);
            baseInvoiceBillableAmount.setFrozenAmount(zeroAmount);
            billableAmountDao.save(baseInvoiceBillableAmount);
        }
    }

    protected boolean existsBillableAmount(String cusId) {
        return billableAmountDao.countByCusId(cusId) > 0L ? true : false;
    }

    public void incrBillableTotalAmount(String cusId, BigDecimal incrAmount) throws Exception {
        billableAmountDao.incrTotalAmount(cusId, incrAmount);
    }

    public void incrBillableInvoicedAmount(String cusId, BigDecimal incrAmount) throws Exception {
        billableAmountDao.incrInvoicedAmount(cusId, incrAmount);
    }

    public void incrBillableFrozenAmount(String cusId, BigDecimal incrAmount) throws Exception {
        billableAmountDao.incrFrozenAmount(cusId, incrAmount);
    }

    public void decrBillableFrozenAmount(String cusId, BigDecimal decrAmount) throws Exception {
        billableAmountDao.decrFrozenAmount(cusId, decrAmount);
    }

    public String getBillableLowerLimit() {
        SysDataTree node = DictUtil.getDataTreeByNodeId(Invoice.NodeId.BILLABLE_AMOUNT_LOWER_LIMIT);
        return node == null ? "0.00" : node.getPara1();
    }

}
