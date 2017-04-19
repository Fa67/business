package com.eayun.charge.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 计费清单实体
 *
 * @Filename: BaseChargeRecord.java
 * @Description:
 * @Version: 1.0
 * @Author: zhangfan
 * @Email: fan.zhang@eayun.com
 * @History:<br> <li>Date: 2016年8月1日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
@Entity
@Table(name = "money_chargerecord")
public class BaseChargeRecord implements Serializable {

    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -6152853469711297722L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "cr_id", length = 32)
    private String id;

    @Column(name = "cr_resourcename", length = 100)
    private String resourceName;

    @Column(name = "cr_ordernumber", length = 20)
    private String orderNumber;

    @Column(name = "cr_datacenterid", length = 100)
    private String datecenterId;

    @Column(name = "cr_cusid", length = 32)
    private String cusId;

    @Column(name = "cr_resourceid", length = 100)
    private String resourceId;

    @Column(name = "cr_resourcetype", length = 100)
    private String resourceType;

    @Column(name = "cr_factors")
    private String billingFactorStr;

    @Column(name = "cr_chargefrom")
    private Date chargeFrom;

    @Column(name = "cr_isvalid", length = 1)
    private String isValid;

    @Column(name = "cr_resourcestatus", length = 1)
    private String resourceStatus;

    @Column(name = "cr_changetime")
    private Date changeTime;

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getDatecenterId() {
        return datecenterId;
    }

    public void setDatecenterId(String datecenterId) {
        this.datecenterId = datecenterId;
    }

    public String getCusId() {
        return cusId;
    }

    public void setCusId(String cusId) {
        this.cusId = cusId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getBillingFactorStr() {
        return billingFactorStr;
    }

    public void setBillingFactorStr(String billingFactorStr) {
        this.billingFactorStr = billingFactorStr;
    }

    public Date getChargeFrom() {
        return chargeFrom;
    }

    public void setChargeFrom(Date chargeFrom) {
        this.chargeFrom = chargeFrom;
    }

    public String getIsValid() {
        return isValid;
    }

    public void setIsValid(String isValid) {
        this.isValid = isValid;
    }

    public String getResourceStatus() {
        return resourceStatus;
    }

    public void setResourceStatus(String resourceStatus) {
        this.resourceStatus = resourceStatus;
    }

    public Date getChangeTime() {
        return changeTime;
    }

    public void setChangeTime(Date changeTime) {
        this.changeTime = changeTime;
    }
}
