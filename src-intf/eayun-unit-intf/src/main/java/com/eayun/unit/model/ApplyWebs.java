package com.eayun.unit.model;

import java.util.Date;
import java.util.List;


/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年12月21日
 */
public class ApplyWebs extends BaseApplyInfo{

	private static final long serialVersionUID = 1L;
	private List<WebSiteIP> websiteList;                //网站集合
	private  String unitName;                                 //主办单位名称
	private String headName;                                  //法人名称
	private Integer unitNature;//主办单位性质：1 个人、2企业、3政府机关、4事业单位、5社会群体、6军队
	private String unitArea;//主办单位所属区域
	private String unitAddress;//主办单位通讯地址
	private Integer certificateType;//主办单位证件类型：1工商营业执照、2组织机构代码，证书
	private String certificateNo;//证件号
	private String certificateAddress;//证件所在地
	private String dutyName;//主体负责人姓名
	private Integer dutyCertificateType;//负责人证件类型：1身份证、2护照、3台胞证、4军官证
	private String dutyCertificateNo;//负责人证件号
	private String phone;//办公室电话
	private String dutyPhone;//负责人手机
	private String dutyEmail;//负责人邮箱
	private String dutyQQ;//负责人QQ
	private String remark;//备注
	private Date createTime;//创建时间
	private Date updateTime;//上次修改时间
	private Integer recordType;//备案类型:1首次备案、2 新增网站、3 新增接入、	4 变更备案
	private String recordNo;//主体备案号
	private String recordPassWord;//管局密码
	private String cusId;//客户id
	private String cusOrg;//客户名称
	private String businessFileId;//营业执照证件照ID
	private String dutyFileId;//负责人证件照 ID

	public List<WebSiteIP> getWebsiteList() {
		return websiteList;
	}

	public void setWebsiteList(List<WebSiteIP> websiteList) {
		this.websiteList = websiteList;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public String getHeadName() {
		return headName;
	}

	public void setHeadName(String headName) {
		this.headName = headName;
	}

	public Integer getUnitNature() {
		return unitNature;
	}

	public void setUnitNature(Integer unitNature) {
		this.unitNature = unitNature;
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

	
}
