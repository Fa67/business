package com.eayun.work.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

/**
 * 
 *                       
 * @Filename: BaseNoSendSms.java
 * @Description: 未发送短信类
 * @Version: 1.0
 * @Author: 陈鹏飞
 * @Email: pengfei.chen@eayun.com
 * @History:<br>
 *<li>Date: 2015年11月12日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "sys_no_send_sms")
public class BaseNoSendSms implements Serializable{
    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 7445775297337473364L;
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "sms_id", length = 32)
    private String smsId;
    @Column(name = "sms_phone", length = 255)
    private String smsPhone;
    @Column(name = "sms_content", length = 255)
    private String smsContent;
    @Column(name = "sms_start")
    @Temporal(TemporalType.TIMESTAMP)
    private Date smsStart;
    @Column(name = "sms_end")
    @Temporal(TemporalType.TIMESTAMP)
    private Date smsEnd;
    public String getSmsId() {
        return smsId;
    }
    public void setSmsId(String smsId) {
        this.smsId = smsId;
    }
    
    public String getSmsPhone() {
        return smsPhone;
    }
    public void setSmsPhone(String smsPhone) {
        this.smsPhone = smsPhone;
    }
    public String getSmsContent() {
        return smsContent;
    }
    public void setSmsContent(String smsContent) {
        this.smsContent = smsContent;
    }
    public Date getSmsStart() {
        return smsStart;
    }
    public void setSmsStart(Date smsStart) {
        this.smsStart = smsStart;
    }
    public Date getSmsEnd() {
        return smsEnd;
    }
    public void setSmsEnd(Date smsEnd) {
        this.smsEnd = smsEnd;
    }
    
}
