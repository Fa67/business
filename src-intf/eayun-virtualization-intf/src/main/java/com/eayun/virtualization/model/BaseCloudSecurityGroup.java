package com.eayun.virtualization.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.eayun.eayunstack.model.SecurityGroup;


@Entity
@Table(name = "cloud_securitygroup")
public class BaseCloudSecurityGroup implements java.io.Serializable {

	private static final long serialVersionUID = 7883304662653382785L;
	private String sgId;//安全组id
	private String sgName;//名称
	private String createName;//创建人
	private String dcId;//数据中心id
	private Date createTime;//创建时间
	private String prjId;//项目id
	private String sgDescription;
	private String defaultGroup;//给默认安全组赋值，用于删除按钮不可点
	
	@Id
	@Column(name = "sg_id", unique = true, nullable = false, length = 100)
	public String getSgId() {
		return sgId;
	}
	@Column(name = "sg_name", length = 100)
	public String getSgName() {
		return sgName;
	}
	@Column(name = "create_name", length = 100)
	public String getCreateName() {
		return createName;
	}
	@Column(name = "dc_id", length = 100)
	public String getDcId() {
		return dcId;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_time", length = 19)
	public Date getCreateTime() {
		return createTime;
	}
	@Column(name = "prj_id", length = 100)
	public String getPrjId() {
		return prjId;
	}
	@Column(name = "sg_description", length = 1000)
	public String getSgDescription() {
		return sgDescription;
	}
	@Column(name = "default_group", length = 100)
	public String getDefaultGroup() {
		return defaultGroup;
	}
	
	
	public void setSgId(String sgId) {
		this.sgId = sgId;
	}
	
	public void setSgName(String sgName) {
		this.sgName = sgName;
	}
	
	
	
	public void setDcId(String dcId) {
		this.dcId = dcId;
	}
	
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}
	
	public void setSgDescription(String sgDescription) {
		this.sgDescription = sgDescription;
	}
	public void setCreateName(String createName) {
		this.createName = createName;
	}
	
	public void setDefaultGroup(String defaultGroup) {
		this.defaultGroup = defaultGroup;
	}
	
	public BaseCloudSecurityGroup(){}
	
	public BaseCloudSecurityGroup (SecurityGroup sg,String dcId){
		if(null!=sg){
			this.sgId = sg.getId();
			this.sgName = sg.getName();
			this.dcId = dcId;
			this.prjId = sg.getTenant_id();
			this.sgDescription = sg.getDescription();
			if("default".equals(sg.getName().trim())){
				this.defaultGroup="defaultGroup";
			}
		}
	}
	
	

}
