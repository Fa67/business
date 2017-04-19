package com.eayun.sms.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

/**
 * @Filename: BaseSMS.java
 * @Description:
 * @Version: 1.0
 * @Author: fan.zhang
 * @Email: fan.zhang@eayun.com
 * @History:<br>
 * <li>Date: 2015年11月2日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
@Entity
@Table(name = "sys_sms")
public class BaseSMS implements Serializable {
    private static final long serialVersionUID = -9123497630348218725L;
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "sms_id", unique = true, nullable = false, length = 32)
    private String            id;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "sms_inserttime")
    private Date              insertTime;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "sms_updatetime")
    private Date              updateTime;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Type(type = "text")
    @Column(name = "sms_detail")
    private String            detail;
    @Column(name = "sms_status", length = 32)
    private String            status;
    @Column(name = "sms_cust", length = 32)
    private String            customerId;                              // 客户ID
    @Column(name = "sms_proj", length = 32)
    private String            projectId;                               // 项目ID
    @Column(name = "sms_biz", length = 32)
    private String            biz;                                     // 业务来源
    @Column(name = "sms_sent")
    @Type(type = "int")
    private int              sent;                                    // 配额内正常发送的短信数量
    @Column(name = "sms_oversent")
    @Type(type = "int")
    private int              overSent;                                // 超过配额发送的短信数量--属于我们额外赠送的

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getBiz() {
        return biz;
    }

    public void setBiz(String biz) {
        this.biz = biz;
    }

    public int getSent() {
        return sent;
    }

    public void setSent(int sent) {
        this.sent = sent;
    }

    public int getOverSent() {
        return overSent;
    }

    public void setOverSent(int overSent) {
        this.overSent = overSent;
    }
}
