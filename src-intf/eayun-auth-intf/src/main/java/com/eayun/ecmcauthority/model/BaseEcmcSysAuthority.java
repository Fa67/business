package com.eayun.ecmcauthority.model;

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
 * 权限
* @Author:fangjun.yang
* @Date:2016年3月1日
*/
@Entity
@Table(name = "ecmc_sys_authority")
public class BaseEcmcSysAuthority implements Serializable{

	private static final long serialVersionUID = -4616586087890015651L;
	@Id
	@GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "id", unique = true, nullable = false, length = 32)
	private String id;													//id
	
	@Column(name = "name", nullable = false, length = 100)
	private String name;												//名称
	
	@Column(name = "description", length = 300)
	private String description;											//描述
	
	@Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "createtime", updatable = false)
	private Date createTime;											//创建时间
	
	@Column(name = "createdby", length = 32, updatable = false)
	private String createdBy;											//创建者ID
	
	@Column(name = "enableflag", length = 1)
	private String enableFlag;							               //是否启用
	
	@Column(name = "permission")
	private String permission;											//许可
	
	@Column(name = "menu_id", length = 32)
	private String menuId;												//菜单
	
	@Column(name = "`lock`", length = 1, updatable = false)
    private String lock = "0";                                                //是否加锁，0:false 可被分配分配给角色、删除;1：true 不可被分配给角色、删除
	
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
	public String getEnableFlag() {
		return enableFlag;
	}
	public void setEnableFlag(String enableFlag) {
		this.enableFlag = enableFlag;
	}
	public String getPermission() {
		return permission;
	}
	public void setPermission(String permission) {
		this.permission = permission;
	}
	public String getMenuId() {
		return menuId;
	}
	public void setMenuId(String menuId) {
		this.menuId = menuId;
	}
    public String getLock() {
        return lock;
    }
    public void setLock(String lock) {
        this.lock = lock;
    }
	
}

