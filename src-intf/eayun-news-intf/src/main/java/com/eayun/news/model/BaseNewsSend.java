package com.eayun.news.model;

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
 * @Filename: BaseNewsSend.java
 * @Description: 
 * @Version: 1.0
 * @Author:wang ning
 * @Email: ning.wang@eayun.com
 * @History:<br>
 *<li>Date: 2015年10月8日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "news_sendinfo")
public class BaseNewsSend implements java.io.Serializable{

    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 5025668497209843746L;
    
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name="news_id", length=32)
    private String id;
    
    @Column(name="send_date", length=7)
    private Date sendDate;    //发送时间
    
    @Column(name="send_person", length=32)
    private String sendPerson;   //发件人
    
    @Column(name="news_title", length=128)
    private String  newsTitle; //消息标题
    
    @Column(name="memo",length=5000)
    private String memo; //消息内容
    
    @Column(name="rec_type",length=1)
    private String recType; // 接收人类型 1：所有人 2：所有管理员 3：指定用户下的所有人 4：指定用户下的管理员
    
    @Column(name="is_sended",length=1)
    private String isSended;  //是否发送1：已发送2：未发送
    
    @Column(name="readed", length=7)
    private int readed;  //已读数量
    
    @Column(name="collected", length=7)
    private int collected;  //已收藏数量
    
    @Column(name="sended", length=7)
    private int sended; //发送数量
    
    @Column(name="cus_id",columnDefinition="mediumtext")
    private String cusId; //客户id
    
    @Column(name="is_syssend",length=32)
    private String is_syssend;//是否系统发送  0手工   1系统
    
    
    public String getIs_syssend() {
		return is_syssend;
	}

	public void setIs_syssend(String is_syssend) {
		this.is_syssend = is_syssend;
	}

	public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public String getSendPerson() {
        return sendPerson;
    }

    public void setSendPerson(String sendPerson) {
        this.sendPerson = sendPerson;
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

    public String getRecType() {
        return recType;
    }

    public void setRecType(String recType) {
        this.recType = recType;
    }

    public String getIsSended() {
        return isSended;
    }

    public void setIsSended(String isSended) {
        this.isSended = isSended;
    }

    public int getReaded() {
        return readed;
    }

    public void setReaded(int readed) {
        this.readed = readed;
    }

    public int getCollected() {
		return collected;
	}

	public void setCollected(int collected) {
		this.collected = collected;
	}

	public int getSended() {
        return sended;
    }

    public void setSended(int sended) {
        this.sended = sended;
    }

    public String getCusId() {
        return cusId;
    }

    public void setCusId(String cusId) {
        this.cusId = cusId;
    }

    


}
