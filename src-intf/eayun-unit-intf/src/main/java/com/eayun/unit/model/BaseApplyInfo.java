package com.eayun.unit.model;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
/**
 * 
 * 
 * 
 * 
 * */
@Entity
@Table(name="apply_info")
public class BaseApplyInfo  implements Serializable{
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name="apply_id",length=32,unique=true,nullable=false)
	private String applyId;//id
	
	@Column(name="unit_id",length=32)
	private String unitId;//主办单位ID
	
	
	
	@Column(name="status",length=1)
	private Integer status;//备案状态：1等待初审、2初审通过、3初审未通过、4复审通过、5复审未通过、6管局审核、7管局未通过、8备案成功
	
	@Column(name="record_type",length=1)
	private Integer recordType;//备案类型：1 首次备案、2 新增网站、3 新增接入、4 变更备案
	
	@Column(name="create_time")
	private Date createTime;//创建时间
	
	@Column(name="return_type",length=1)
	private Integer returnType;//管局返回状态：1信息不对、2资料不全

	public BaseApplyInfo(String applyId,String unitId, Integer status, Integer recordType, String newUnitid, Date createTime,
			Integer returnType) {
		super();
		this.applyId = applyId;
		this.unitId = unitId;
		this.status = status;
		this.recordType = recordType;
		this.createTime = createTime;
		this.returnType = returnType;
	}

	public Integer getReturnType() {
		return returnType;
	}

	public void setReturnType(Integer returnType) {
		this.returnType = returnType;
	}

	public String getApplyId() {
		return applyId;
	}

	public void setApplyId(String applyId) {
		this.applyId = applyId;
	}

	public String getUnitId() {
		return unitId;
	}

	public void setUnitId(String unitId) {
		this.unitId = unitId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getRecordType() {
		return recordType;
	}

	public void setRecordType(Integer recordType) {
		this.recordType = recordType;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}



	public BaseApplyInfo() {
		super();
		// TODO Auto-generated constructor stub
	}


}
