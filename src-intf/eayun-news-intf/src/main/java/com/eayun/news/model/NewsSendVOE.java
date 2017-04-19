package com.eayun.news.model;

import java.util.Date;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年3月31日
 */
public class NewsSendVOE extends NewsSend{
	/**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -7114356967628786454L;
    private String recPerson;
	private String recName;
	private String endDate;
	private Date readDate;
	private String infoId;
	private String cusName;
	private String cusCpname;
	private String isSysSend;
	
	public String getIsSysSend() {
		return isSysSend;
	}
	public void setIsSysSend(String isSysSend) {
		this.isSysSend = isSysSend;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public String getRecName() {
		return recName;
	}
	public void setRecName(String recName) {
		this.recName = recName;
	}
	public String getCusName() {
		return cusName;
	}
	public void setCusName(String cusName) {
		this.cusName = cusName;
	}
	public String getInfoId() {
		return infoId;
	}
	public void setInfoId(String infoId) {
		this.infoId = infoId;
	}
	public String getRecPerson() {
		return recPerson;
	}
	public void setRecPerson(String recPerson) {
		this.recPerson = recPerson;
	}
	public Date getReadDate() {
		return readDate;
	}
	public void setReadDate(Date readDate) {
		this.readDate = readDate;
	}
	public String getCusCpname() {
		return cusCpname;
	}
	public void setCusCpname(String cusCpname) {
		this.cusCpname = cusCpname;
	}
}
