package com.eayun.unit.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;


@Entity
@Table(name="website_info")
public class BaseWebSiteInfo implements Serializable,Cloneable {
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name="web_id",length=32,unique=true,nullable=false)
	private String webId;//id
	
	@Column(name="web_name",length=100)
	private String webName;//网站名称
	
	@Column(name="domain_name",length=2100)
	private String domainName;//网站域名
	
	@Column(name="domain_url",length=2100)
	private String domainUrl;//网站地址
	
	@Column(name="web_service",length=32)
	private Integer webService;//备案云服务 :1云服务、2负载均衡
	
	@Column(name="service_ip",length=20)
	private String serviceIp;//云服务的ip
	
	@Column(name="service_content",length=1000)
	private String serviceContent;//服务内容
	
	@Column(name="web_language",length=1000)
	private String webLanguage;//网站语言
	
	@Column(name="web_special",length=1)
	private Integer webSpecial;//前置或专项内容审批：1新闻、2出版、3教育、4医疗保健、5药品和医疗器械、6文化、7广播电影电视节目
	
	@Column(name="special_no",length=100)
	private String specialNo;//前置审批号
	
	@Column(name="web_duty_name",length=100)
	private String webDutyName;//网站负责人姓名
	
	@Column(name="duty_certificate_type",length=1)
	private Integer dutyCertificateType;//负责人证件类型:1身份证、2护照、3台胞证、4军官证
	
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
	
	@Column(name="create_time")
	private Date crateTime;//创建时间 
	
	@Column(name="update_time")
	private Date updateTime;//上次修改时间
	
	@Column(name="progress",length=1)
	private Integer progress;//备案进展

	@Column(name="web_record_no",length=100)
	private String webRecordNo;//备案号
	
	@Column(name="unit_id",length=32)
	private String unitId;//主办单位id
	
	@Column(name="duty_fileId",length=32)
	private String dutyFileId;//负责人证件照 ID
	
	@Column(name="domain_fileId",length=32)
	private String domainFileId;//域名照
	
	@Column(name="special_fileId",length=32)
	private String specialFileId;//审批照
	
	
	@Column(name="dc_id",length=32)
	private String dcID;//数据中心
	
	@Column(name="isChange",length=1)
	private String isChange;//是否变更
	
	@Column(name="web_password",length=100)
	private String webPassword;//新增接入ICP密码
	
	@Column(name="remark",length=200)
	private String remark;//备注

	public String getWebId() {
		return webId;
	}

	public void setWebId(String webId) {
		this.webId = webId;
	}

	public String getWebName() {
		return webName;
	}

	public void setWebName(String webName) {
		this.webName = webName;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getDomainUrl() {
		return domainUrl;
	}

	public void setDomainUrl(String domainUrl) {
		this.domainUrl = domainUrl;
	}

	public Integer getWebService() {
		return webService;
	}

	public void setWebService(Integer webService) {
		this.webService = webService;
	}

	public String getServiceIp() {
		return serviceIp;
	}

	public void setServiceIp(String serviceIp) {
		this.serviceIp = serviceIp;
	}

	public String getServiceContent() {
		return serviceContent;
	}

	public void setServiceContent(String serviceContent) {
		this.serviceContent = serviceContent;
	}

	public String getWebLanguage() {
		return webLanguage;
	}

	public void setWebLanguage(String webLanguage) {
		this.webLanguage = webLanguage;
	}

	public Integer getWebSpecial() {
		return webSpecial;
	}

	public void setWebSpecial(Integer webSpecial) {
		this.webSpecial = webSpecial;
	}

	public String getSpecialNo() {
		return specialNo;
	}

	public void setSpecialNo(String specialNo) {
		this.specialNo = specialNo;
	}

	public String getWebDutyName() {
		return webDutyName;
	}

	public void setWebDutyName(String webDutyName) {
		this.webDutyName = webDutyName;
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

	public Date getCrateTime() {
		return crateTime;
	}

	public void setCrateTime(Date crateTime) {
		this.crateTime = crateTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Integer getProgress() {
		return progress;
	}

	public void setProgress(Integer progress) {
		this.progress = progress;
	}

	public String getWebRecordNo() {
		return webRecordNo;
	}

	public void setWebRecordNo(String webRecordNo) {
		this.webRecordNo = webRecordNo;
	}

	public String getUnitId() {
		return unitId;
	}

	public void setUnitId(String unitId) {
		this.unitId = unitId;
	}
	
	public String getDutyFileId() {
		return dutyFileId;
	}

	public void setDutyFileId(String dutyFileId) {
		this.dutyFileId = dutyFileId;
	}

	public String getDomainFileId() {
		return domainFileId;
	}

	public void setDomainFileId(String domainFileId) {
		this.domainFileId = domainFileId;
	}

	public String getSpecialFileId() {
		return specialFileId;
	}

	public void setSpecialFileId(String specialFileId) {
		this.specialFileId = specialFileId;
	}
	
	public String getDcID() {
		return dcID;
	}

	public void setDcID(String dcID) {
		this.dcID = dcID;
	}

	
	
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getIsChange() {
		return isChange;
	}

	public void setIsChange(String isChange) {
		this.isChange = isChange;
	}
	
	public String getWebPassword() {
		return webPassword;
	}

	public void setWebPassword(String webPassword) {
		this.webPassword = webPassword;
	}

	public BaseWebSiteInfo() {
		super();
		// TODO Auto-generated constructor stub
	}

	public BaseWebSiteInfo(String webId, String webName, String domainName, String domainUrl, Integer webService,
			String serviceIp, String serviceContent, String webLanguage, Integer webSpecial, String specialNo,
			String webDutyName, Integer dutyCertificateType, String dutyCertificateNo, String phone, String dutyPhone,
			String dutyEmail, String dutyQQ, Date crateTime, Date updateTime, Integer progress, String webRecordNo,
			String unitId) {
		super();
		this.webId = webId;
		this.webName = webName;
		this.domainName = domainName;
		this.domainUrl = domainUrl;
		this.webService = webService;
		this.serviceIp = serviceIp;
		this.serviceContent = serviceContent;
		this.webLanguage = webLanguage;
		this.webSpecial = webSpecial;
		this.specialNo = specialNo;
		this.webDutyName = webDutyName;
		this.dutyCertificateType = dutyCertificateType;
		this.dutyCertificateNo = dutyCertificateNo;
		this.phone = phone;
		this.dutyPhone = dutyPhone;
		this.dutyEmail = dutyEmail;
		this.dutyQQ = dutyQQ;
		this.crateTime = crateTime;
		this.updateTime = updateTime;
		this.progress = progress;
		this.webRecordNo = webRecordNo;
		this.unitId = unitId;
	}
	@Override
	public Object clone() {
		BaseWebSiteInfo stu = null;  
        try{  
            stu = (BaseWebSiteInfo)super.clone();  
        }catch(CloneNotSupportedException e) {  
            e.printStackTrace();  
        }  
        return stu; 
	}
	
}
