package com.eayun.virtualization.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.StringUtils;

import com.eayun.eayunstack.model.Volume;



/**
 * 云硬盘          
 * @Filename: BaseCloudVolume.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2015年11月11日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "cloud_volume")
public class BaseCloudVolume implements java.io.Serializable {

private static final long serialVersionUID = -1116765119289877206L;
	
	private String volId;      //主键ID
	private String volName;    //云硬盘名称
	private String createName; //创建人姓名
	private Date createTime;   //创建时间
	private String prjId;      //项目id
	private String dcId;       //数据中心id
	private String volBootable;//云硬盘属性1:系统盘  0:数据盘
	private String osType;     //操作系统
	private String sysType;    //系统类型（如：WINXPx32，WIN7x64,WIN10x64）
	private String diskFrom;   //云硬盘创建自（如：全局镜像、自定义镜像、云硬盘备份、空白盘）
	private String vmId;       //挂载的云主机id
	private String fromImageId;//镜像id
	private String fromSnapId; //备份id
	private int volSize;       //云硬盘大小
	private String volStatus;  //云硬盘状态
	private String volDescription; //云硬盘描述
	private String isDeleted;      //是否删除
	private Date deleteTime;       //删除时间
	private String deleteUser;     //执行删除的用户
	private String bindPoint ;     //挂载点
	private String chargeState;    //计费状态 0 正常  1 余额不足  2 已到期  3已到期(停服务)
	private String payType;        //计费模式  1  包年包月   2 按需计费
	private String isVisable;      //0 不显示   1显示
	private Date endTime;      //到期时间
	private String volTypeId;  //云硬盘类型id
	private String typeSuccess;//是否成功设置了volume_type 1代表设置成功
	
	
	@Id
	@Column(name = "vol_id", unique = true, nullable = false, length = 100)
	public String getVolId() {
		return volId;
	}
	public void setVolId(String volId) {
		this.volId = volId;
	}
	@Column(name = "vol_name", length = 100)
	public String getVolName() {
		return volName;
	}
	public void setVolName(String volName) {
		this.volName = volName;
	}
	@Column(name = "create_name", length = 50)
	public String getCreateName() {
		return createName;
	}
	public void setCreateName(String createName) {
		this.createName = createName;
	}
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_time", length = 19)
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	@Column(name = "prj_id", length = 100)
	public String getPrjId() {
		return prjId;
	}
	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}
	@Column(name = "dc_id", length = 100)
	public String getDcId() {
		return dcId;
	}
	public void setDcId(String dcId) {
		this.dcId = dcId;
	}
	@Column(name = "vol_bootable", length = 1)
	public String getVolBootable() {
		return volBootable;
	}
	public void setVolBootable(String volBootable) {
		this.volBootable = volBootable;
	}
	@Column(name = "os_type", length = 50)
	public String getOsType() {
		return osType;
	}
	public void setOsType(String osType) {
		this.osType = osType;
	}
	@Column(name = "sys_type", length = 50)
	public String getSysType() {
		return sysType;
	}
	public void setSysType(String sysType) {
		this.sysType = sysType;
	}
	@Column(name = "disk_from", length = 50)
	public String getDiskFrom() {
		return diskFrom;
	}
	public void setDiskFrom(String diskFrom) {
		this.diskFrom = diskFrom;
	}
	@Column(name = "vm_id", length = 100)
	public String getVmId() {
		return vmId;
	}
	public void setVmId(String vmId) {
		this.vmId = vmId;
	}
	@Column(name = "from_imageid", length = 100)
	public String getFromImageId() {
		return fromImageId;
	}
	public void setFromImageId(String fromImageId) {
		this.fromImageId = fromImageId;
	}
	@Column(name = "from_snapid", length = 100)
	public String getFromSnapId() {
		return fromSnapId;
	}
	public void setFromSnapId(String fromSnapId) {
		this.fromSnapId = fromSnapId;
	}
	@Column(name = "vol_size", length = 20)
	public int getVolSize() {
		return volSize;
	}
	public void setVolSize(int volSize) {
		this.volSize = volSize;
	}
	@Column(name = "vol_description", length = 1000)
	public String getVolDescription() {
		return volDescription;
	}
	public void setVolDescription(String volDescription) {
		this.volDescription = volDescription;
	}
	@Column(name = "vol_status", length = 50)
	public String getVolStatus() {
		return volStatus;
	}
	public void setVolStatus(String volStatus) {
		this.volStatus = volStatus;
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

	@Column(name = "delete_user", length = 50)
	public String getDeleteUser() {
		return deleteUser;
	}

	public void setDeleteUser(String deleteUser) {
		this.deleteUser = deleteUser;
	}
	@Column(name = "bind_point", length = 50)
	public String getBindPoint() {
		return bindPoint;
	}
	public void setBindPoint(String bindPoint) {
		this.bindPoint = bindPoint;
	}
	@Column(name = "charge_state", length = 1)
	public String getChargeState() {
		return chargeState;
	}
	public void setChargeState(String chargeState) {
		this.chargeState = chargeState;
	}
	@Column(name = "pay_type", length = 1)
	public String getPayType() {
		return payType;
	}
	public void setPayType(String payType) {
		this.payType = payType;
	}
	@Column(name = "is_visable", length = 1)
	public String getIsVisable() {
		return isVisable;
	}
	public void setIsVisable(String isVisable) {
		this.isVisable = isVisable;
	}
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "end_time", length = 19)
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	@Column(name = "vol_typeid", length = 100)
	public String getVolTypeId() {
		return volTypeId;
	}
	public void setVolTypeId(String volTypeId) {
		this.volTypeId = volTypeId;
	}
	@Column(name = "type_success", length = 1)
	public String getTypeSuccess() {
		return typeSuccess;
	}
	public void setTypeSuccess(String typeSuccess) {
		this.typeSuccess = typeSuccess;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public BaseCloudVolume (){}
	
	public BaseCloudVolume(Volume volume,String dcId,String projectId) throws Exception{
		if(null!=volume){
			SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			this.volId=volume.getId();
			this.volName=volume.getName();
			if(StringUtils.isEmpty(volName)){
				volName = volId;
			}
			this.prjId=projectId;
			this.dcId=dcId;
			this.volBootable=volume.getBootable()?"1":"0";
			if(null!=volume.getAttachments()&&volume.getAttachments().length>0&&null!=volume.getAttachments()[0]){
				this.vmId=volume.getAttachments()[0].getServer_id();
				this.bindPoint = volume.getAttachments()[0].getDevice();
			}
			if(!StringUtils.isEmpty(volume.getSize())){
				this.volSize=Integer.parseInt(volume.getSize());
			}
			if(!StringUtils.isEmpty(volume.getVolume_type())){
				this.volTypeId=(null!=volume.getVolume_type()?volume.getVolume_type():null);
			}
			this.fromImageId=volume.getImage_id();
			this.fromSnapId=volume.getSnapshot_id();
			this.volDescription=volume.getDescription();
			this.volStatus=volume.getStatus()!=null?volume.getStatus().toUpperCase():"";
			if(!StringUtils.isEmpty(volume.getCreated_at())){
				this.createTime=sdf.parse(volume.getCreated_at().replace("T", " ").substring(0, volume.getCreated_at().lastIndexOf(".")));
			}
			
		}
	}
	

}
