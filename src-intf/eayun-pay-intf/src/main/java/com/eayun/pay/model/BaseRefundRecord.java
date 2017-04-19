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
 * @Filename: BaseRefundRecord.java
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
@Table(name = "pay_refundrecord")
public class BaseRefundRecord implements Serializable {

    private static final long serialVersionUID = -3894129078577888904L;

    @Id
    @Column(name = "refund_id", length = 32)
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String            refundId;     //退款记录ID

    @Column(name = "refund_amount")
    private BigDecimal        refundAmount; //退款金额

    @Column(name = "refund_type", length = 1)
    private String            refundType;   //退款类型 => 0：余额退款 1：支付宝退款

    @Column(name = "batch_no", length = 64)
    private String            batchNo;      //批量退款，批次号

    @Column(name = "batch_num")
    private Integer           batchNum;     //批量退款，退款数量

    @Column(name = "detail_data", length = 4000)
    private String            detailData;   //支付宝批量退款，数据集

    @Column(name = "create_time")
    private Date              createTime;   //退款发起时间
   
    @Column(name = "finish_time")
    private Date              finishTime;   //退款完成时间

    @Column(name = "refund_status", length = 1)
    private String            refundStatus; //退款状态 => 0:退款中 1:退款成功 2: 退款失败

    @Column(name = "order_no", length = 18)
    private String            orderNo;      //订单号

    @Column(name = "cus_id", length = 32)
    private String            cusId;        //客户ID

    public String getRefundId() {
        return refundId;
    }

    public void setRefundId(String refundId) {
        this.refundId = refundId;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getRefundType() {
        return refundType;
    }

    public void setRefundType(String refundType) {
        this.refundType = refundType;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public Integer getBatchNum() {
        return batchNum;
    }

    public void setBatchNum(Integer batchNum) {
        this.batchNum = batchNum;
    }

    public String getDetailData() {
        return detailData;
    }

    public void setDetailData(String detailData) {
        this.detailData = detailData;
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

    public String getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(String refundStatus) {
        this.refundStatus = refundStatus;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getCusId() {
        return cusId;
    }

    public void setCusId(String cusId) {
        this.cusId = cusId;
    }

}
