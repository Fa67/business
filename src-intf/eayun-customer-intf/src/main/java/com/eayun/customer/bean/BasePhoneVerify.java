package com.eayun.customer.bean;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "sys_phoneverify")
public class BasePhoneVerify implements java.io.Serializable {

    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -732311845442719103L;
    
    private String Id;          //记录id
    
    private String userId;      //用户id
    
    private String phone;       //手机号码
    
    private String phoneCode;   //手机验证码
    
    private Date sendTime;      //发送时间
    
    private Date verifyTime;    //验证时间
    
    private Date invalidTime;   //失效时间
    
    private boolean isVerify;   //是否已验证
    
    private boolean isNewphone; //是否是修改的新手机号码

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "id", unique = true, nullable = false, length = 32)
    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    @Column(name = "user_id")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Column(name = "phone")
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    @Column(name = "phone_code")
    public String getPhoneCode() {
        return phoneCode;
    }

    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }

    @Column(name = "invalid_time")
    public Date getInvalidTime() {
        return invalidTime;
    }

    public void setInvalidTime(Date invalidTime) {
        this.invalidTime = invalidTime;
    }

    @Column(name = "send_time")
    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    @Column(name = "is_verify")
    public boolean isVerify() {
        return isVerify;
    }

    public void setVerify(boolean isVerify) {
        this.isVerify = isVerify;
    }

    @Column(name = "verify_time")
    public Date getVerifyTime() {
        return verifyTime;
    }

    public void setVerifyTime(Date verifyTime) {
        this.verifyTime = verifyTime;
    }

    @Column(name = "is_newphone")
    public boolean isNewphone() {
        return isNewphone;
    }

    public void setNewphone(boolean isNewphone) {
        this.isNewphone = isNewphone;
    }
    
}
