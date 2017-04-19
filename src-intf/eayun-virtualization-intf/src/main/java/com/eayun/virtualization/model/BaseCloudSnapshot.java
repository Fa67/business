package com.eayun.virtualization.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.StringUtils;

import com.eayun.eayunstack.model.Snapshot;



@Entity
@Table(name = "cloud_disksnapshot")
public class BaseCloudSnapshot implements java.io.Serializable{
	
private static final long serialVersionUID = 1L;
	
	private String snapId;
	private String snapName;
	private String createName;
	private Date createTime;
	private String prjId;
	private String dcId;
	private int snapSize;
	private String volId;
	private String snapStatus;
	private String snapDescription;
	/*private String reserve1;
	private String reserve2;
	private String reserve3;
	private String reserve4;
	private String reserve5;*/
	
	private String payType;
	private String chargeState;
	private String isDeleted;
	private Date deleteTime;
	private String deleteUser;
	private String snapType;
	private String isVisable;

	@Id
	@Column(name = "snap_id", unique = true, nullable = false, length = 100)
	public String getSnapId() {
		return this.snapId;
	}

	public void setSnapId(String snapId) {
		this.snapId = snapId;
	}

	@Column(name = "snap_name", length = 100)
	public String getSnapName() {
		return this.snapName;
	}

	public void setSnapName(String snapName) {
		this.snapName = snapName;
	}

	@Column(name = "create_name", length = 50)
	public String getCreateName() {
		return this.createName;
	}

	public void setCreateName(String createName) {
		this.createName = createName;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_time", length = 19)
	public Date getCreateTime() {
		return this.createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Column(name = "prj_id", length = 100)
	public String getPrjId() {
		return this.prjId;
	}

	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}

	@Column(name = "dc_id", length = 100)
	public String getDcId() {
		return this.dcId;
	}

	public void setDcId(String dcId) {
		this.dcId = dcId;
	}

	@Column(name = "snap_size", length = 20)
	public int getSnapSize() {
		return this.snapSize;
	}

	public void setSnapSize(int i) {
		this.snapSize = i;
	}

	@Column(name = "vol_id", length = 100)
	public String getVolId() {
		return this.volId;
	}

	public void setVolId(String volId) {
		this.volId = volId;
	}

	@Column(name = "snap_status", length = 50)
	public String getSnapStatus() {
		return this.snapStatus;
	}

	public void setSnapStatus(String snapStatus) {
		this.snapStatus = snapStatus;
	}

	@Column(name = "snap_description", length = 1000)
	public String getSnapDescription() {
		return this.snapDescription;
	}

	public void setSnapDescription(String snapDescription) {
		this.snapDescription = snapDescription;
	}

	/*@Column(name = "reserve1", length = 100)
	public String getReserve1() {
		return this.reserve1;
	}

	public void setReserve1(String reserve1) {
		this.reserve1 = reserve1;
	}

	@Column(name = "reserve2", length = 100)
	public String getReserve2() {
		return this.reserve2;
	}

	public void setReserve2(String reserve2) {
		this.reserve2 = reserve2;
	}

	@Column(name = "reserve3", length = 100)
	public String getReserve3() {
		return this.reserve3;
	}

	public void setReserve3(String reserve3) {
		this.reserve3 = reserve3;
	}

	@Column(name = "reserve4", length = 100)
	public String getReserve4() {
		return this.reserve4;
	}

	public void setReserve4(String reserve4) {
		this.reserve4 = reserve4;
	}

	@Column(name = "reserve5", length = 100)
	public String getReserve5() {
		return this.reserve5;
	}

	public void setReserve5(String reserve5) {
		this.reserve5 = reserve5;
	}*/
	

	
	@Column(name = "pay_type", length = 1)
	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}
	@Column(name = "charge_state", length = 1)
	public String getChargeState() {
		return chargeState;
	}

	public void setChargeState(String chargeState) {
		this.chargeState = chargeState;
	}
	@Column(name = "is_deleted", length = 1)
	public String getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted;
	}
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "delete_time", length = 19)
	public Date getDeleteTime() {
		return deleteTime;
	}

	public void setDeleteTime(Date deleteTime) {
		this.deleteTime = deleteTime;
	}
	@Column(name = "delete_user", length = 32)
	public String getDeleteUser() {
		return deleteUser;
	}

	public void setDeleteUser(String deleteUser) {
		this.deleteUser = deleteUser;
	}
	@Column(name = "snap_type", length = 1)
	public String getSnapType() {
		return snapType;
	}

	public void setSnapType(String snapType) {
		this.snapType = snapType;
	}
	@Column(name = "is_visable", length = 1)
	public String getIsVisable() {
		return isVisable;
	}

	public void setIsVisable(String isVisable) {
		this.isVisable = isVisable;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	
	
	public BaseCloudSnapshot() {
		super();
	}
	
	public BaseCloudSnapshot(String snapId, String snapName, String createName,
			Date createTime, String prjId, String dcId, int snapSize,
			String volId, String snapStatus, String snapDescription,
			String reserve1, String reserve2, String reserve3, String reserve4,
			String reserve5) {
		super();
		this.snapId = snapId;
		this.snapName = snapName;
		this.createName = createName;
		this.createTime = createTime;
		this.prjId = prjId;
		this.dcId = dcId;
		this.snapSize = snapSize;
		this.volId = volId;
		this.snapStatus = snapStatus;
		this.snapDescription = snapDescription;
		/*this.reserve1 = reserve1;
		this.reserve2 = reserve2;
		this.reserve3 = reserve3;
		this.reserve4 = reserve4;
		this.reserve5 = reserve5;*/
	}
	
	public BaseCloudSnapshot(Snapshot snap,String dcId,String projectId){
		if(null!=snap){
			this.snapId=snap.getId();
			this.snapName=snap.getName();
			this.prjId=projectId;
			this.dcId=dcId;
			if(!StringUtils.isEmpty(snap.getSize())){
				this.snapSize=Integer.parseInt(snap.getSize());
			}
			this.volId=snap.getVolume_id();
			this.snapStatus=snap.getStatus()!=null?snap.getStatus().toUpperCase():"";
			this.snapDescription=snap.getDescription();
		}
	}


}
