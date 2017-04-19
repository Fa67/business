package com.eayun.ecmcdepartment.model;

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
 * @author zengbo 部门表映射实体类
 *
 */
@Entity
@Table(name = "ecmc_sys_department")
public class BaseEcmcSysDepartment implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "id", unique = true, nullable = false, length = 32)
	private String id;																	//ID

	@Column(name = "name", length = 100)
	private String name;															//名称

	@Column(name = "description", length = 300)
	private String description;												//描述

	@Column(name = "parentid", length = 32)
	private String parentId;														//父级ID

	@Column(name = "address", length = 100)
	private String address;														//地址

	@Column(name = "tel", length = 50)
	private String tel;																//电话

	@Column(name = "linkman", length = 50)
	private String linkMan;														//联系人

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "createtime", nullable = false, length = 19, updatable=false)
	private Date createTime;													//创建时间

	@Column(name = "createdby", length = 32, updatable=false)
	private String createdBy;													//创建者ID

	@Column(name = "enableflag", length = 1)
	private boolean enableFlag;												//是否启用

	@Column(name = "code", length = 50)
	private String code;															//编号

	@Column(name = "fax", length = 50)
	private String fax;																//传真
	
	public BaseEcmcSysDepartment(){
		
	}
	
	public BaseEcmcSysDepartment(String id, String name, String description, String parentId){
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.parentId = parentId;
	}

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

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getLinkMan() {
		return linkMan;
	}

	public void setLinkMan(String linkMan) {
		this.linkMan = linkMan;
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

	public boolean getEnableFlag() {
		return enableFlag;
	}

	public void setEnableFlag(boolean enableFlag) {
		this.enableFlag = enableFlag;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}
}
