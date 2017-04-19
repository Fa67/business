package com.eayun.news.model;


/**
 * 
 *                       
 * @Filename: ShowNews.java
 * @Description: 
 * @Version: 1.0
 * @Author:wang ning
 * @Email: ning.wang@eayun.com
 * @History:<br>
 *<li>Date: 2015年9月14日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class NewsRec extends BaseNewsRec{

    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -1254778129908323464L;

    private String newsTitle;  //消息标题
    private String memo;   //消息内容
    private String sendPerson;   //发件人姓名
    private String sendDate;//发送时间
    private String recType;
    private String cusId;
    private String cusName;
    private String issyssend;
	
    public String getIssyssend() {
		return issyssend;
	}
	public void setIssyssend(String issyssend) {
		this.issyssend = issyssend;
	}
	public String getRecType() {
		return recType;
	}
	public void setRecType(String recType) {
		this.recType = recType;
	}
	public String getCusId() {
		return cusId;
	}
	public void setCusId(String cusId) {
		this.cusId = cusId;
	}
	public String getCusName() {
		return cusName;
	}
	public void setCusName(String cusName) {
		this.cusName = cusName;
	}
	public String getSendDate() {
        return sendDate;
    }
    public void setSendDate(String sendDate) {
        this.sendDate = sendDate;
    }
    
    public String getNewsTitle() {
        return newsTitle;
    }
    public void setNewsTitle(String newsTitle) {
        this.newsTitle = newsTitle;
    }
    public String getMemo() {
        return memo;
    }
    public void setMemo(String memo) {
        this.memo = memo;
    }
	public String getSendPerson() {
		return sendPerson;
	}
	public void setSendPerson(String sendPerson) {
		this.sendPerson = sendPerson;
	}
    
}
