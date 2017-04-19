/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.pay.model;

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
 * @Filename: BasePayRecord.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2016年7月27日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "pay_record")
public class BasePayRecord implements Serializable {

    private static final long serialVersionUID = -6019174454199070952L;

    @Id
    @Column(name = "pay_id", length = 32)
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String            payId;                    //支付记录ID

    @Column(name = "third_id", length = 32)
    private String            thirdId;                  //第三方支付流水号

    @Column(name = "pay_type", length = 1)
    private String            payType;                  //支付类型 => 0:余额支付 1:支付宝

    @Column(name = "trade_type", length = 1)
    private String            tradeType;                //交易类型 => 1:订单支付 2:钱包充值             

    @Column(name = "trade_no", length = 18)
    private String            tradeNo;                  //支付流水号(18位) => 02+日期(8位)+当日次数(8位)

    @Column(name = "pay_amount")
    private BigDecimal        payAmount;                //支付金额

    @Column(name = "pay_status", length = 1)
    private String            payStatus;                //支付状态 => 0:支付中 1:支付成功  2:支付失败

    @Column(name = "create_time")
    private Date              createTime;               //支付创建时间

    @Column(name = "finish_time")
    private Date              finishTime;               //支付完成时间

    @Column(name = "third_result", length = 1000)
    private String            thirdResult;              //第三方支付交易结果

    @Column(name = "prod_name", length = 256)
    private String            prodName;                 //产品名称

    @Column(name = "prod_desc", length = 1000)
    private String            prodDesc;                 //产品描述

    @Column(name = "cus_id", length = 32)
    private String            cusId;                    //客户ID

    @Column(name = "user_id", length = 32)
    private String            userId;                   //用户ID

    public String getPayId() {
        return payId;
    }

    public void setPayId(String payId) {
        this.payId = payId;
    }

    public String getThirdId() {
        return thirdId;
    }

    public void setThirdId(String thirdId) {
        this.thirdId = thirdId;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }

    public String getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(String payStatus) {
        this.payStatus = payStatus;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public String getThirdResult() {
        return thirdResult;
    }

    public void setThirdResult(String thirdResult) {
        this.thirdResult = thirdResult;
    }

    public String getProdName() {
        return prodName;
    }

    public void setProdName(String prodName) {
        this.prodName = prodName;
    }

    public String getProdDesc() {
        return prodDesc;
    }

    public void setProdDesc(String prodDesc) {
        this.prodDesc = prodDesc;
    }

    public String getCusId() {
        return cusId;
    }

    public void setCusId(String cusId) {
        this.cusId = cusId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
