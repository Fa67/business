package com.eayun.ecmcrole.model;

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
 * @author zengbo 用户角色表映射实体类
 *
 */
@Entity
@Table(name = "ecmc_sys_role")
public class BaseEcmcSysRole implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "id", unique = true, nullable = false, length = 32)
	private String id;																				//ID

	@Column(name = "name", length = 100)
	private String name;																		//名称

	@Column(name = "description", length = 300)
	private String description;															//描述

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "createtime", nullable = false, length = 19, updatable=false)
	private Date createTime;																//创建时间

	@Column(name = "createdby", length = 32, updatable=false)
	private String createdBy;																//创建者ID

	@Column(name = "enableflag", length = 1)
	private boolean enableFlag;														      //是否启用
	
	@Column(name = "protect", length = 1, updatable = false)
	private String protect = "0";                                                              //是否受保护，0:false 可配置权限、删除;1：true 受保护，不可配置权限、删除
	
	@Column(name = "hide", length = 1, updatable = false)
	private String hide = "0";                                                                 //是否隐藏，0:否，不隐藏;1：是，隐藏
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public boolean isEnableFlag() {
		return enableFlag;
	}

	public void setEnableFlag(boolean enableFlag) {
		this.enableFlag = enableFlag;
	}

    public String getProtect() {
        return protect;
    }

    public void setProtect(String protect) {
        this.protect = protect;
    }

    public String getHide() {
        return hide;
    }

    public void setHide(String hide) {
        this.hide = hide;
    }
	
}
