/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.invoice.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 *                       
 * @Filename: BaseInvoiceApply.java
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
@Entity
@Table(name = "invoice_apply")
public class BaseInvoiceApply implements Serializable {

    private static final long serialVersionUID = 6915425891759777643L;
    @Id
    @Column(name = "id", length = 32)
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String id;                      //ID
    
    @Column(name = "cus_id", length = 32)
    private String cusId;                   //客户ID
    
    @Column(name = "invoice_title", length = 500)
    private String invoiceTitle;            //发票抬头
    
    @Column(name = "invoice_type", length = 1)
    private String invoiceType;             //发票类型
    
    @Column(name = "amount")
    private BigDecimal amount;              //金额
    
    @Column(name = "create_time", updatable = false)
    private Date createTime;                //创建时间
    
    @Column(name = "status", length = 2)
    private String status;                  //状态
    
    @Column(name = "used_express", length = 500)
    private String usedExpress;             //是否使用了快递公司
    
    @Column(name = "express_name", length = 500)
    private String expressName;             //快递公司名称
    
    @Column(name = "express_no", length = 500)
    private String expressNo;               //快递单号
    
    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;            //取消申请的原因
    
    @Column(name = "receiver_name", length = 20)
    private String receiverName;            //收件人姓名
    
    @Column(name = "receiver_tel", length = 500)
    private String receiverTel;             //收件人电话
    
    @Column(name = "address", length = 500)
    private String address;                 //收件地址
    
    @Column(name = "zip_code", length = 500)
    private String zipCode;                 //邮编
    
    @Column(name = "base_bankname", length = 500)
    private String baseBankName;            //基本开户行名称
    
    @Column(name = "base_bankaccount", length = 500)
    private String baseBankAccount;         //基本开户行账号
    
    @Column(name = "tax_account", length = 500)
    private String taxAccount;              //税务登记账号
    
    @Column(name = "reg_address", length = 500)
    private String regAddress;              //注册场所地址
    
    @Column(name = "reg_tel", length = 500)
    private String regTel;                  //注册固定电话
    
    @Column(name = "biz_licensefileid", length = 32)
    private String bizLicenseFileId;        //营业执照副本扫描件
    
    @Column(name = "taxpayer_licensefileid", length = 32)
    private String taxpayerLicenseFileId;   //一般纳税人资格证扫描件
    
    @Column(name = "bank_licensefileid", length = 32)
    private String bankLicenseFileId;       //银行开户许可证扫描件
    
    @Column(name = "noexpress_tips", length = 500)
    private String noExpressTips;           //未勾选使用快递的提示语

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCusId() {
        return cusId;
    }

    public void setCusId(String cusId) {
        this.cusId = cusId;
    }

    public String getInvoiceTitle() {
        return invoiceTitle;
    }

    public void setInvoiceTitle(String invoiceTitle) {
        this.invoiceTitle = invoiceTitle;
    }

    public String getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(String invoiceType) {
        this.invoiceType = invoiceType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsedExpress() {
        return usedExpress;
    }

    public void setUsedExpress(String usedExpress) {
        this.usedExpress = usedExpress;
    }

    public String getExpressName() {
        return expressName;
    }

    public void setExpressName(String expressName) {
        this.expressName = expressName;
    }

    public String getExpressNo() {
        return expressNo;
    }

    public void setExpressNo(String expressNo) {
        this.expressNo = expressNo;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverTel() {
        return receiverTel;
    }

    public void setReceiverTel(String receiverTel) {
        this.receiverTel = receiverTel;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getBaseBankName() {
        return baseBankName;
    }

    public void setBaseBankName(String baseBankName) {
        this.baseBankName = baseBankName;
    }

    public String getBaseBankAccount() {
        return baseBankAccount;
    }

    public void setBaseBankAccount(String baseBankAccount) {
        this.baseBankAccount = baseBankAccount;
    }

    public String getTaxAccount() {
        return taxAccount;
    }

    public void setTaxAccount(String taxAccount) {
        this.taxAccount = taxAccount;
    }

    public String getRegAddress() {
        return regAddress;
    }

    public void setRegAddress(String regAddress) {
        this.regAddress = regAddress;
    }

    public String getRegTel() {
        return regTel;
    }

    public void setRegTel(String regTel) {
        this.regTel = regTel;
    }

    public String getBizLicenseFileId() {
        return bizLicenseFileId;
    }

    public void setBizLicenseFileId(String bizLicenseFileId) {
        this.bizLicenseFileId = bizLicenseFileId;
    }

    public String getTaxpayerLicenseFileId() {
        return taxpayerLicenseFileId;
    }

    public void setTaxpayerLicenseFileId(String taxpayerLicenseFileId) {
        this.taxpayerLicenseFileId = taxpayerLicenseFileId;
    }

    public String getBankLicenseFileId() {
        return bankLicenseFileId;
    }

    public void setBankLicenseFileId(String bankLicenseFileId) {
        this.bankLicenseFileId = bankLicenseFileId;
    }

    public String getNoExpressTips() {
        return noExpressTips;
    }

    public void setNoExpressTips(String noExpressTips) {
        this.noExpressTips = noExpressTips;
    }

    
}
