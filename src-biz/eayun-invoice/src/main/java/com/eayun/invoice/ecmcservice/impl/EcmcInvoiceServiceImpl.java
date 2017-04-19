/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.invoice.ecmcservice.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.eayun.customer.model.Customer;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.file.model.EayunFile;
import com.eayun.file.service.FileService;
import com.eayun.invoice.dao.InvoiceApplyDao;
import com.eayun.invoice.ecmcservice.EcmcInvoiceService;
import com.eayun.invoice.model.BaseInvoiceApply;
import com.eayun.invoice.model.InvoiceApply;
import com.eayun.invoice.service.InvoiceService;
import com.eayun.notice.service.MessageCenterService;
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
@Service
@Transactional
public class EcmcInvoiceServiceImpl implements EcmcInvoiceService {

    /**
     *zip下载文件的连接字符串
     */
    private static final String  DOWNLOAD_LINK_NAME   = "扫描件";

    /**
     *连接字符'.'
     */
    private static final String  DOWNLOAD_LINK_DOT    = ".";

    /**
     * zip文件名中的时间格式化字符串
     */
    private static final String  DOWNLOAD_DATE_FORMAT = "yyyyMMdd";

    /**
     * zip文件后缀名
     */
    private static final String  DOWNLOAD_SUFFIX_ZIP  = ".zip";

    /**
     *下划线
     */
    private static final String  DLOWNLOAD_UNDERLINE  = "_";

    /**
     *一般纳税人资格证
     */
    private static final String  TAXPAYER_FILENAME    = "一般纳税人资格证";

    /**
     *银行开户许可证
     */
    private static final String  BANK_FILENAME        = "银行开户许可证";

    /**
     *营业执照副本
     */
    private static final String  BIZ_FILENAME         = "营业执照副本";

    private final static Logger  logger               = LoggerFactory.getLogger(EcmcInvoiceServiceImpl.class);

    @Autowired
    private InvoiceApplyDao      invoiceApplyDao;

    @Autowired
    private CustomerService      customerService;

    @Autowired
    private InvoiceService       invoiceService;

    @Autowired
    private FileService          fileService;

    @Autowired
    private MessageCenterService messageCenterService;

    public Page queryInvoiceApplys(QueryMap queryMap, String cusName, String status, String startTime, String endTime) throws Exception {
        logger.info("查询开票申请");
        StringBuffer sqlBuffer = new StringBuffer();
        List<Object> params = new ArrayList<Object>();
        sqlBuffer.append("select a.id, a.cus_id, c.cus_org, a.invoice_title, a.invoice_type, a.create_time, a.status");
        sqlBuffer.append(", a.used_express, a.express_name, a.express_no, a.noexpress_tips, a.cancel_reason, a.amount");
        sqlBuffer.append(" from  invoice_apply a left join sys_selfcustomer c on a.cus_id = c.cus_id where 1 = 1 ");
        if (!StringUtil.isEmpty(startTime)) {
            sqlBuffer.append("and a.create_time >= ? ");
            params.add(DateUtil.timestampToDate(startTime));
        }
        if (!StringUtil.isEmpty(endTime)) {
            sqlBuffer.append("and a.create_time <= ? ");
            params.add(DateUtil.addDay(DateUtil.timestampToDate(endTime), new int[] { 0, 0, 1 }));
        }
        if (!StringUtil.isEmpty(cusName)) {
            sqlBuffer.append("and c.cus_org like ? escape '/' ");
            params.add("%" + escapeSpecialChar(cusName) + "%");
        }
        if (!StringUtil.isEmpty(status)) {
            if (StringUtils.contains(status, ",")) {
                //针对取消(用户取消、ecmc取消)
                String[] statusArr = StringUtils.split(status, ",");
                sqlBuffer.append("and (a.status = ? or a.status = ? ) ");
                params.add(statusArr[0]);
                params.add(statusArr[1]);
            } else {
                sqlBuffer.append("and a.status = ? ");
                params.add(status);
            }
        }
        sqlBuffer.append("order by a.create_time desc");
        Page page = invoiceApplyDao.pagedNativeQuery(sqlBuffer.toString(), queryMap, params.toArray());
        @SuppressWarnings("unchecked")
        List<Object[]> result = (List<Object[]>) page.getResult();
        List<InvoiceApply> resultList = new ArrayList<InvoiceApply>();
        if (result != null && result.size() > 0) {
            for (Object[] objects : result) {
                InvoiceApply invoiceApply = new InvoiceApply();
                invoiceApply.setId(ObjectUtils.toString(objects[0], null));
                invoiceApply.setCusId(ObjectUtils.toString(objects[1], null));
                invoiceApply.setCusName(ObjectUtils.toString(objects[2], null));
                invoiceApply.setInvoiceTitle(ObjectUtils.toString(objects[3], null));
                invoiceApply.setInvoiceType(ObjectUtils.toString(objects[4], null));
                invoiceApply.setCreateTime(DateUtil.stringToDate(ObjectUtils.toString(objects[5], null)));
                invoiceApply.setStatus(ObjectUtils.toString(objects[6], null));
                invoiceApply.setUsedExpress(ObjectUtils.toString(objects[7], null));
                invoiceApply.setExpressName(ObjectUtils.toString(objects[8], null));
                invoiceApply.setExpressNo(ObjectUtils.toString(objects[9], null));
                invoiceApply.setNoExpressTips(ObjectUtils.toString(objects[10], null));
                invoiceApply.setCancelReason(ObjectUtils.toString(objects[11], null));
                invoiceApply.setAmount(new BigDecimal(ObjectUtils.toString(objects[12], null)));
                resultList.add(invoiceApply);
            }
            page.setResult(resultList);
        }
        return page;
    }

    public void changeToProcessing(String id) throws Exception {
        BaseInvoiceApply baseInvoiceApply = invoiceApplyDao.findOne(id);
        if (baseInvoiceApply == null) {
            throw new AppException("找不到该申请", new String[] { "找不到该申请" });
        }
        if (!Invoice.Status.PENDING.equals(baseInvoiceApply.getStatus())) {
            throw new AppException("无法执行该操作，请刷新后重试", new String[] { "无法执行该操作，请刷新后重试" });
        }
        baseInvoiceApply.setStatus(Invoice.Status.PROCESSING);
        invoiceApplyDao.saveOrUpdate(baseInvoiceApply);
    }

    public void changeToInvoiced(InvoiceApply invoiceApply) throws Exception {
        String id = invoiceApply.getId();
        BaseInvoiceApply baseInvoiceApply = invoiceApplyDao.findOne(id);
        if (baseInvoiceApply == null) {
            throw new AppException("找不到该申请", new String[] { "找不到该申请" });
        }
        if (!Status.PROCESSING.equals(baseInvoiceApply.getStatus())) {
            throw new AppException("无法执行该操作，请刷新后重试", new String[] { "无法执行该操作，请刷新后重试" });
        }
        if (Invoice.UsedExpress.YES.equals(invoiceApply.getUsedExpress())) {
            baseInvoiceApply.setExpressName(invoiceApply.getExpressName());
            baseInvoiceApply.setExpressNo(invoiceApply.getExpressNo());
            baseInvoiceApply.setUsedExpress(Invoice.UsedExpress.YES);
        } else {
            String noExpressTips = getNoExpressTips();
            baseInvoiceApply.setUsedExpress(Invoice.UsedExpress.NO);
            baseInvoiceApply.setNoExpressTips(noExpressTips);
        }
        baseInvoiceApply.setStatus(Invoice.Status.INVOICED);
        invoiceApplyDao.saveOrUpdate(baseInvoiceApply);
        BeanUtils.copyPropertiesByModel(invoiceApply, baseInvoiceApply);
        //减少冻结金额
        invoiceService.decrBillableFrozenAmount(invoiceApply.getCusId(), invoiceApply.getAmount());
        //增加已开票金额
        invoiceService.incrBillableInvoicedAmount(invoiceApply.getCusId(), invoiceApply.getAmount());
        messageCenterService.yesOpenReceipt(invoiceApply.getCusId(), invoiceApply.getAmount(), invoiceApply.getInvoiceTypeName(), invoiceApply.getInvoiceTitle(), invoiceApply.getAddress(), invoiceApply.getStatusName(), invoiceApply.getCreateTime());
    }

    public void cancelApply(InvoiceApply invoiceApply) throws Exception {
        String id = invoiceApply.getId();
        BaseInvoiceApply baseInvoiceApply = invoiceApplyDao.findOne(id);
        if (baseInvoiceApply == null) {
            throw new AppException("找不到该申请", new String[] { "找不到该申请" });
        }
        if (!Status.PENDING.equals(baseInvoiceApply.getStatus()) && !Status.PROCESSING.equals(baseInvoiceApply.getStatus())) {
            throw new AppException("无法执行该操作，请刷新后重试", new String[] { "无法执行该操作，请刷新后重试" });
        }
        baseInvoiceApply.setCancelReason(invoiceApply.getCancelReason());
        baseInvoiceApply.setStatus(Invoice.Status.CANCEL_BY_ECMC);
        invoiceApplyDao.saveOrUpdate(baseInvoiceApply);
        invoiceService.decrBillableFrozenAmount(baseInvoiceApply.getCusId(), baseInvoiceApply.getAmount());
        BeanUtils.copyPropertiesByModel(invoiceApply, baseInvoiceApply);
        messageCenterService.ecmcCancelReceipt(invoiceApply.getCusId(), invoiceApply.getAmount(), invoiceApply.getInvoiceTypeName(), invoiceApply.getInvoiceTitle(), invoiceApply.getAddress(), invoiceApply.getStatusName(), invoiceApply.getCreateTime(), invoiceApply.getCancelReason());
    }

    public InvoiceApply getInvoiceApplyDetail(String id) throws Exception {
        BaseInvoiceApply baseInvoiceApply = invoiceApplyDao.findOne(id);
        if (baseInvoiceApply == null) {
            throw new AppException("找不到该申请", new String[] { "找不到该申请" });
        }
        Customer customer = customerService.findCustomerById(baseInvoiceApply.getCusId());
        InvoiceApply invoiceApply = new InvoiceApply();
        BeanUtils.copyPropertiesByModel(invoiceApply, baseInvoiceApply);
        //客户名称
        invoiceApply.setCusName(customer.getCusOrg());
        return invoiceApply;
    }

    public List<SysDataTree> getCancelReasons() throws Exception {
        return DictUtil.getDataTreeByParentId(Invoice.NodeId.CANCEL_REASONS);
    }

    public List<SysDataTree> getExpressNames() throws Exception {
        return DictUtil.getDataTreeByParentId(Invoice.NodeId.EXPRESS_NAMES);
    }

    public File getLicensesZipFile(String applyId) throws Exception {
        InvoiceApply detail = getInvoiceApplyDetail(applyId);
        String cusName = detail.getCusName();
        File tempZipFile = new File(FileUtils.getTempDirectory(), zipFileName(cusName));
        ZipOutputStream zipOutputStream = null;
        try {
            zipOutputStream = new ZipOutputStream(new FileOutputStream(tempZipFile));
            this.putZipStream(zipOutputStream, detail.getBizLicenseFileId(), BIZ_FILENAME);
            this.putZipStream(zipOutputStream, detail.getTaxpayerLicenseFileId(), TAXPAYER_FILENAME);
            this.putZipStream(zipOutputStream, detail.getBankLicenseFileId(), BANK_FILENAME);
        } finally {
            IOUtils.closeQuietly(zipOutputStream);
        }
        return tempZipFile;
    }

    protected ZipOutputStream putZipStream(ZipOutputStream zipOutputStream, String fileId, String fileName) {
        try {
            InputStream input = null;
            EayunFile eayunfile = fileService.findOneById(fileId);
            if (null != eayunfile) {
                input = fileService.downloadFile(fileId);
                zipOutputStream.putNextEntry(new ZipEntry(new String(fileName.getBytes()) + DOWNLOAD_LINK_DOT + eayunfile.getFileType()));
                IOUtils.copy(input, zipOutputStream);
                IOUtils.closeQuietly(input);
            }
        } catch (Exception e) {
            logger.error("download invoice apply's file failed, fileId:" + fileId, e);
        }
        return zipOutputStream;
    }

    protected String zipFileName(String cusName) {
        SimpleDateFormat format = new SimpleDateFormat(DOWNLOAD_DATE_FORMAT);
        String dateStr = format.format(new Date());
        StringBuffer sb = new StringBuffer();
        return sb.append(cusName).append(DOWNLOAD_LINK_NAME).append(DLOWNLOAD_UNDERLINE).append(dateStr).append(DOWNLOAD_SUFFIX_ZIP).toString();
    }

    protected String getNoExpressTips() throws Exception {
        SysDataTree node = DictUtil.getDataTreeByNodeId(Invoice.NodeId.NOEXPRESS_TIPS);
        return node == null ? "" : node.getPara1();
    }

    protected String escapeSpecialChar(String str) {
        if (StringUtils.isNotBlank(str)) {
            String[] specialChars = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|", "%" };
            for (String key : specialChars) {
                if (str.contains(key)) {
                    str = str.replace(key, "/" + key);
                }
            }
        }
        return str;
    }
}
