package com.eayun.ecmcmenu.model;

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
 * 菜单
* @Author:fangjun.yang
* @Date:2016年3月1日
*/
@Entity
@Table(name = "ecmc_sys_menu")
public class BaseEcmcSysMenu implements Serializable{
	
	private static final long serialVersionUID = -3665701249581622409L;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "id", unique = true, nullable = false, length = 32)
	private String id;				//ID
	
	@Column(name = "name", nullable = false, length = 100)
	private String name;			//名称
	
	@Column(name = "description", length = 300)
	private String description;		//描述
	
	@Column(name = "url", length = 100)
	private String url;				//页面地址
	
	@Column(name = "parentid", length = 32)
	private String parentId;		//父级ID
	
	@Column(name = "orderno")
	private Integer orderNo;		//排序号
	
	@Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "createtime", updatable = false)
	private Date createTime;		//创建时间
	
	@Column(name = "createdby", length = 32, updatable = false)
	private String createdBy;		//创建人
	
	@Column(name = "enableflag", length = 1)
	private Character enableFlag;	//是否启用
	
	@Column(name = "icon", length = 100)
	private String icon;           //图标地址
	
	@Column(name = "`lock`", length = 1, updatable = false)
	private Character lock = '0';        //是否加锁，0:false 可被分配分配给角色、删除;1：true 不可被分配给角色、删除

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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
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

	public Character getEnableFlag() {
		return enableFlag;
	}

	public void setEnableFlag(Character enableFlag) {
		this.enableFlag = enableFlag;
	}

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Character getLock() {
        return lock;
    }

    public void setLock(Character lock) {
        this.lock = lock;
    }
	
}

