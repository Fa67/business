package com.eayun.work.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 
 *                       
 * @Filename: BaseEcmcUser.java
 * @Description: 简易类，不包含数据库所有字段
 * @Version: 1.0
 * @Author: 陈鹏飞
 * @Email: pengfei.chen@eayun.com
 * @History:<br>
 *<li>Date: 2015年10月30日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "sys_user")
public class BaseEcmcUser implements Serializable{
    private static final long serialVersionUID = -2951295816604715373L;
    
    @Id
    @Column(name = "user_id", length = 32)
    private String ecmcUserId;
    @Column(name = "user_name", length = 32)
    private String ecmcUserName;
    @Column(name = "user_phone", length = 32)
    private String ecmcUserPhone;
    @Column(name = "user_mobile", length = 32)
    private String ecmcuserMobile;
    @Column(name = "user_mail", length = 32)
    private String ecmcUserMail;
    public String getEcmcUserId() {
        return ecmcUserId;
    }
    public void setEcmcUserId(String ecmcUserId) {
        this.ecmcUserId = ecmcUserId;
    }
    public String getEcmcUserName() {
        return ecmcUserName;
    }
    public void setEcmcUserName(String ecmcUserName) {
        this.ecmcUserName = ecmcUserName;
    }
    public String getEcmcUserPhone() {
        return ecmcUserPhone;
    }
    public void setEcmcUserPhone(String ecmcUserPhone) {
        this.ecmcUserPhone = ecmcUserPhone;
    }
    public String getEcmcuserMobile() {
        return ecmcuserMobile;
    }
    public void setEcmcuserMobile(String ecmcuserMobile) {
        this.ecmcuserMobile = ecmcuserMobile;
    }
    public String getEcmcUserMail() {
        return ecmcUserMail;
    }
    public void setEcmcUserMail(String ecmcUserMail) {
        this.ecmcUserMail = ecmcUserMail;
    }
    
    
}
