package com.eayun.monitor.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "ecsc_contact")
public class BaseContact implements Serializable {

    private static final long serialVersionUID = -4597280181562900612L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "c_id", unique = true, nullable = false, length = 32)
    private String            id;

    @Column(name = "c_cusid", length = 32)
    private String            cusId;

    @Column(name = "c_name", length = 64)
    private String            name;

    @Column(name = "c_phone", length = 11)
    private String            phone;

    @Column(name = "c_email", length = 100)
    private String            email;

    @Column(name = "c_smsnotify", length = 1)
    private String            smsNotify;

    @Column(name = "c_mailnotify", length = 1)
    private String            mailNotify;

    @Column(name = "c_isadmin", length = 1)
    private String            isAdmin;                                  //是否是超级管理员

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSmsNotify() {
        return smsNotify;
    }

    public void setSmsNotify(String smsNotify) {
        this.smsNotify = smsNotify;
    }

    public String getMailNotify() {
        return mailNotify;
    }

    public void setMailNotify(String mailNotify) {
        this.mailNotify = mailNotify;
    }

    public String getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(String isAdmin) {
        this.isAdmin = isAdmin;
    }

}
