/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.invoice.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 *                       
 * @Filename: BaseInvoiceDeliveryAddress.java
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
@Table(name = "invoice_delivery_address")
public class BaseInvoiceDeliveryAddress implements Serializable {

    private static final long serialVersionUID = 2557459673161915244L;

    @Id
    @Column(name = "id", length = 32)
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String            id;                                     //ID

    @Column(name = "cus_id", length = 32)
    private String            cusId;                                  //客户ID

    @Column(name = "receiver_name", length = 20)
    private String            receiverName;                           //收件人姓名

    @Column(name = "receiver_tel", length = 500)
    private String            receiverTel;                            //收件人电话

    @Column(name = "address", length = 500)
    private String            address;                                //收件地址

    @Column(name = "zip_code", length = 500)
    private String            zipCode;                                //邮编

    @Column(name = "create_time", updatable = false)
    private Date              createTime;                             //创建时间

    @Column(name = "update_time")
    private Date              updateTime;                             //更新时间

    @Column(name = "default_item", length = 1)
    private String            defaultItem;                            //是否默认

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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getDefaultItem() {
        return defaultItem;
    }

    public void setDefaultItem(String defaultItem) {
        this.defaultItem = defaultItem;
    }
    
}
