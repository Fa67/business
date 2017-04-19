package com.eayun.log.model;

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
 * LOperLog entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name = "sys_oper_log")
public class OperLog implements java.io.Serializable{
	
	private static final long serialVersionUID = 1L;
	// Fields
	private String id;             //主键
	private String operId;         //操作人员的登陆账号ossAcount
	private Date operDate;         //操作日期
	private String busiName;       //业务名称 
	private String busiCode;       //业务代码
	private Integer returnFlag;    //成功/失败标志 0-失败 1-成功  
	private String mesCode;        //信息代码
	private String operContent;    //操作内容
	private String ipAddr;         //ip地址
	private String fileName;       //文件名
	private String temp1;
	private String temp2;
	private String temp3;
	private String temp4;
	private String temp5;
	private String temp6;          //
	
	private String resourceType;                            //资源类型
	
    private String resourceName;                            //资源名称
	
    private String prjId;                                   //项目id
	
    private String resourceID;							    //被操作对象ID

	// Constructors

	/** default constructor */
	public OperLog() {
	}

	/** full constructor */
	public OperLog(String operId, Date operDate, String busiName,String busiCode,Integer returnFlag,String mesCode, 
			String operContent, String ipAddr, String fileName, String temp1,
			String temp2, String temp3, String temp4, String temp5, String temp6) {
		this.operId = operId;
		this.operDate = operDate;
		this.busiName = busiName;
		this.busiCode = busiCode;
		this.returnFlag = returnFlag;
		this.mesCode = mesCode;
		this.operContent = operContent;
		this.ipAddr = ipAddr;
		this.fileName = fileName;
		this.temp1 = temp1;
		this.temp2 = temp2;
		this.temp3 = temp3;
		this.temp4 = temp4;
		this.temp5 = temp5;
		this.temp6 = temp6;
	}

	// Property accessors
	@Id
	@GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "ID", unique = true, nullable = false, length = 32)
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "OPER_ID", length = 32)
	public String getOperId() {
		return this.operId;
	}

	public void setOperId(String operId) {
		this.operId = operId;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "OPER_DATE", length = 7)
	public Date getOperDate() {
		return this.operDate;
	}

	public void setOperDate(Date operDate) {
		this.operDate = operDate;
	}

	@Column(name = "BUSI_NAME", length = 100)
	public String getBusiName() {
		return this.busiName;
	}

	public void setBusiName(String busiName) {
		this.busiName = busiName;
	}

	@Column(name = "BUSI_CODE", length = 50)
	public String getBusiCode() {
		return this.busiCode;
	}

	public void setBusiCode(String busiCode) {
		this.busiCode = busiCode;
	}

	@Column(name = "RETURN_FLAG", precision = 22, scale = 0)
	public Integer getReturnFlag() {
		return this.returnFlag;
	}

	public void setReturnFlag(Integer returnFlag) {
		this.returnFlag = returnFlag;
	}

	@Column(name = "MES_CODE", length = 200)
	public String getMesCode() {
		return this.mesCode;
	}

	public void setMesCode(String mesCode) {
		this.mesCode = mesCode;
	}

	@Column(name = "OPER_CONTENT", length = 1000)
	public String getOperContent() {
		return this.operContent;
	}

	public void setOperContent(String operContent) {
		this.operContent = operContent;
	}

	@Column(name = "IP_ADDR", length = 50)
	public String getIpAddr() {
		return this.ipAddr;
	}

	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}

	@Column(name = "FILE_NAME", length = 200)
	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Column(name = "TEMP1", length = 1000)
	public String getTemp1() {
		return this.temp1;
	}

	public void setTemp1(String temp1) {
		this.temp1 = temp1;
	}

	@Column(name = "TEMP2", length = 1000)
	public String getTemp2() {
		return this.temp2;
	}

	public void setTemp2(String temp2) {
		this.temp2 = temp2;
	}

	@Column(name = "TEMP3", length = 1000)
	public String getTemp3() {
		return this.temp3;
	}

	public void setTemp3(String temp3) {
		this.temp3 = temp3;
	}

	@Column(name = "TEMP4", length = 1000)
	public String getTemp4() {
		return this.temp4;
	}

	public void setTemp4(String temp4) {
		this.temp4 = temp4;
	}

	@Column(name = "TEMP5", length = 1000)
	public String getTemp5() {
		return this.temp5;
	}

	public void setTemp5(String temp5) {
		this.temp5 = temp5;
	}

	@Column(name = "TEMP6", length = 1000)
	public String getTemp6() {
		return this.temp6;
	}

	public void setTemp6(String temp6) {
		this.temp6 = temp6;
	}

	@Column(name = "resource_type", length = 64)
	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	@Column(name = "resource_name", length = 64)
	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	@Column(name = "log_prjid", length = 100)
	public String getPrjId() {
		return prjId;
	}

	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}

	@Column(name = "resource_id", length = 100)
	public String getResourceID() {
		return resourceID;
	}

	public void setResourceID(String resourceID) {
		this.resourceID = resourceID;
	}

	
}