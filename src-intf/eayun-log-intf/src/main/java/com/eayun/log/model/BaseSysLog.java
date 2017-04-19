package com.eayun.log.model;

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
 * @Filename: BaseSysLog.java
 * @Description: 
 * @Version: 1.0
 * @Author:wang ning
 * @Email: ning.wang@eayun.com
 * @History:<br>
 *<li>Date: 2015年9月14日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "sys_log")
public class BaseSysLog implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8511483927018055308L;
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "log_id", unique = true, nullable = false, length = 32)
    private String            id;                                      //日志id

    @Column(name = "act_item", length = 32)
    private String            actItem;                                 //操作项

    @Column(name = "act_person", length = 32)
    private String            actPerson;                               //操作者

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "act_time", length = 7)
    private Date              actTime;                                 //操作时间

    @Column(name = "log_prjid", length = 100)
    private String            prjId;                                   //项目id

    @Column(name = "log_cusid", length = 32)
    private String            cusId;                                   //客户id

    @Column(name = "statu", length = 1)
    private String            statu;                                   //状态  1：成功，0：失败

    @Column(name = "resource_type", length = 64)
    private String            resourceType;                            //资源类型

    @Column(name = "resource_name", length = 64)
    private String            resourceName;                            //资源名称

    @Column(name = "log_ip", length = 100)
    private String            ip;                                      // IP

    @Column(name = "log_detail", length = 100)
    private String            detail;                                  // 详情
    

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActItem() {
        return actItem;
    }

    public void setActItem(String actItem) {
        this.actItem = actItem;
    }

    public String getActPerson() {
        return actPerson;
    }

    public void setActPerson(String actPerson) {
        this.actPerson = actPerson;
    }

    public Date getActTime() {
        return actTime;
    }

    public void setActTime(Date actTime) {
        this.actTime = actTime;
    }

    public String getPrjId() {
        return prjId;
    }

    public void setPrjId(String prjId) {
        this.prjId = prjId;
    }

    public String getCusId() {
        return cusId;
    }

    public void setCusId(String cusId) {
        this.cusId = cusId;
    }

    public String getStatu() {
        return statu;
    }

    public void setStatu(String statu) {
        this.statu = statu;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    
}
