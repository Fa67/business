package com.eayun.unit.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CollectionId;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="unit_info")
public class BaseUnitInfo implements Serializable {

		@Id
		@GeneratedValue(generator = "system-uuid")
		@GenericGenerator(name = "system-uuid", strategy = "uuid")
		@Column(name="unit_id",length=32,unique=true,nullable= false)
		private  String unitId;//id
		
		@Column(name="unit_name",length=100)
		private  String unitName;//主办单位名称
		
		@Column(name="unit_nature",length=1)
		private Integer unitNature;//主办单位性质：1 个人、2企业、3政府机关、4事业单位、5社会群体、6军队
		
		@Column(name="head_name",length=100)
		private String headName;//法人名称
		
		@Column(name="unit_area",length=32)
		private String unitArea;//主办单位所属区域
		
		@Column(name="unit_address",length=100)
		private String unitAddress;//主办单位通讯地址
		
		@Column(name="certificate_type",length=1)
		private Integer certificateType;//主办单位证件类型：1工商营业执照、2组织机构代码，证书
		
		@Column(name="certificate_no",length=100)
		private String certificateNo;//证件号
		
		@Column(name="certificate_address",length=100)
		private String certificateAddress;//证件所在地
		
		@Column(name="duty_name",length=100)
		private String dutyName;//主体负责人姓名
		
		@Column(name="duty_certificate_type",length=1)
		private Integer dutyCertificateType;//负责人证件类型：1身份证、2护照、3台胞证、4军官证
		
		@Column(name="duty_certificate_no",length=100)
		private String dutyCertificateNo;//负责人证件号
		
		@Column(name="phone",length=32)
		private String phone;//办公室电话
		
		@Column(name="duty_phone",length=11)
		private String dutyPhone;//负责人手机
		
		@Column(name="duty_email",length=100)
		private String dutyEmail;//负责人邮箱
		
		@Column(name="duty_qq",length=20)
		private String dutyQQ;//负责人QQ
		
		@Column(name="remark",length=1000)
		private String remark;//备注
		
		@Column(name="create_time")
		private Date createTime;//创建时间
		
		@Column(name="update_time")
		private Date updateTime;//上次修改时间
		
		@Column(name="record_type",length=1)
		private Integer recordType;//备案类型:1首次备案、2 新增网站、3 新增接入、	4 变更备案
		
		
		
		@Column(name="record_no",length=100)
		private String recordNo;//主体备案号
		
		@Column(name="record_password",length=100)
		private String recordPassWord;//管局密码
		
		@Column(name="cus_id",length=32)
		private String cusId;//客户id
		
		@Column(name="cus_org",length=32)
		private String cusOrg;//客户名称
		
		@Column(name="business_fileId",length=32)
		private String businessFileId;//营业执照证件照ID
		
		@Column(name="duty_fileId",length=32)
		private String dutyFileId;//负责人证件照 ID

		public String getUnitId() {
			return unitId;
		}

		public void setUnitId(String unitId) {
			this.unitId = unitId;
		}

		public String getUnitName() {
			return unitName;
		}

		public void setUnitName(String unitName) {
			this.unitName = unitName;
		}

		public Integer getUnitNature() {
			return unitNature;
		}

		public void setUnitNature(Integer unitNature) {
			this.unitNature = unitNature;
		}

		public String getHeadName() {
			return headName;
		}

		public void setHeadName(String headName) {
			this.headName = headName;
		}

		public String getUnitArea() {
			return unitArea;
		}

		public void setUnitArea(String unitArea) {
			this.unitArea = unitArea;
		}

		public String getUnitAddress() {
			return unitAddress;
		}

		public void setUnitAddress(String unitAddress) {
			this.unitAddress = unitAddress;
		}

		public Integer getCertificateType() {
			return certificateType;
		}

		public void setCertificateType(Integer certificateType) {
			this.certificateType = certificateType;
		}

		public String getCertificateNo() {
			return certificateNo;
		}

		public void setCertificateNo(String certificateNo) {
			this.certificateNo = certificateNo;
		}

		public String getCertificateAddress() {
			return certificateAddress;
		}

		public void setCertificateAddress(String certificateAddress) {
			this.certificateAddress = certificateAddress;
		}

		public String getDutyName() {
			return dutyName;
		}

		public void setDutyName(String dutyName) {
			this.dutyName = dutyName;
		}

		public Integer getDutyCertificateType() {
			return dutyCertificateType;
		}

		public void setDutyCertificateType(Integer dutyCertificateType) {
			this.dutyCertificateType = dutyCertificateType;
		}

		public String getDutyCertificateNo() {
			return dutyCertificateNo;
		}

		public void setDutyCertificateNo(String dutyCertificateNo) {
			this.dutyCertificateNo = dutyCertificateNo;
		}

		public String getPhone() {
			return phone;
		}

		public void setPhone(String phone) {
			this.phone = phone;
		}

		public String getDutyPhone() {
			return dutyPhone;
		}

		public void setDutyPhone(String dutyPhone) {
			this.dutyPhone = dutyPhone;
		}

		public String getDutyEmail() {
			return dutyEmail;
		}

		public void setDutyEmail(String dutyEmail) {
			this.dutyEmail = dutyEmail;
		}

		public String getDutyQQ() {
			return dutyQQ;
		}

		public void setDutyQQ(String dutyQQ) {
			this.dutyQQ = dutyQQ;
		}

		public String getRemark() {
			return remark;
		}

		public void setRemark(String remark) {
			this.remark = remark;
		}

		public Date getCreateTime() {
			return createTime;
		}

		public void setCreateTime(Date createTime) {
			this.createTime = createTime;
		}

		public Date getUpdateTime() {
			return updateTime;
		}

		public void setUpdateTime(Date updateTime) {
			this.updateTime = updateTime;
		}

		public Integer getRecordType() {
			return recordType;
		}

		public void setRecordType(Integer recordType) {
			this.recordType = recordType;
		}

		

		public String getRecordNo() {
			return recordNo;
		}

		public void setRecordNo(String recordNo) {
			this.recordNo = recordNo;
		}

		public String getRecordPassWord() {
			return recordPassWord;
		}

		public void setRecordPassWord(String recordPassWord) {
			this.recordPassWord = recordPassWord;
		}

		public String getCusId() {
			return cusId;
		}

		public void setCusId(String cusId) {
			this.cusId = cusId;
		}

		public String getCusOrg() {
			return cusOrg;
		}

		public void setCusOrg(String cusOrg) {
			this.cusOrg = cusOrg;
		}
		
		public String getBusinessFileId() {
			return businessFileId;
		}

		public void setBusinessFileId(String businessFileId) {
			this.businessFileId = businessFileId;
		}

		public String getDutyFileId() {
			return dutyFileId;
		}

		public void setDutyFileId(String dutyFileId) {
			this.dutyFileId = dutyFileId;
		}

		public BaseUnitInfo() {
			super();
			// TODO Auto-generated constructor stub
		}

		public BaseUnitInfo(String unitId, String unitName, Integer unitNature, String headName, String unitArea,
				String unitAddress, Integer certificateType, String certificateNo, String certificateAddress,
				String dutyName, Integer dutyCertificateType, String dutyCertificateNo, String phone, String dutyPhone,
				String dutyEmail, String dutyQQ, String remark, Date createTime, Date updateTime, Integer recordType,
				 String recordNo, String recordPassWord, String cusId, String cusOrg) {
			super();
			this.unitId = unitId;
			this.unitName = unitName;
			this.unitNature = unitNature;
			this.headName = headName;
			this.unitArea = unitArea;
			this.unitAddress = unitAddress;
			this.certificateType = certificateType;
			this.certificateNo = certificateNo;
			this.certificateAddress = certificateAddress;
			this.dutyName = dutyName;
			this.dutyCertificateType = dutyCertificateType;
			this.dutyCertificateNo = dutyCertificateNo;
			this.phone = phone;
			this.dutyPhone = dutyPhone;
			this.dutyEmail = dutyEmail;
			this.dutyQQ = dutyQQ;
			this.remark = remark;
			this.createTime = createTime;
			this.updateTime = updateTime;
			this.recordType = recordType;
			
			this.recordNo = recordNo;
			this.recordPassWord = recordPassWord;
			this.cusId = cusId;
			this.cusOrg = cusOrg;
		}

	
		
	
}
