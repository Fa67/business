package com.eayun.mail.model;

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
 * 
 *                       
 * @Filename: BaseMail.java
 * @Description: 
 * @Version: 1.0
 * @Author: fan.zhang
 * @Email: fan.zhang@eayun.com
 * @History:<br>
 *<li>Date: 2015年10月29日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "sys_mail")
public class BaseMail implements Serializable {

    private static final long serialVersionUID = -8049012126952605868L;
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "mail_id", unique = true, nullable = false, length = 32)
    private String            id;                                      //邮件ID

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "mail_inserttime")
    private Date              insertTime;                              //插入时间

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "mail_updatetime")
    private Date              updateTime;                              //更新时间

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Type(type = "text")
    @Column(name = "mail_detail")
    private String            detail;                                  //邮件详情

    @Column(name = "mail_status", length = 1)
    private String            status;                                  //邮件状态  0:未发送  1:发送成功  2:发送失败

    @Column(name = "mail_cause")
    private String            cause;                                   //邮件发送失败原因

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

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

}
