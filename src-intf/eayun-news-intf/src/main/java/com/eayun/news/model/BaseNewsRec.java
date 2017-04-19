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
 * @Filename: BaseShowNews.java
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
@Entity
@Table(name = "news_recinfo")
public class BaseNewsRec implements java.io.Serializable{
	 
	private static final long serialVersionUID = 8396272820854892269L;

	@Id
	@GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name="info_id", length=32)
	private String id;   //id

    @Column(name="news_id", length=64)
	private String newsId;   //消息id
	
    @Column(name="send_date", length=7)
    private Date sendTime;
    
	@Column(name="read_date", length=7)
	private Date readDate;    //阅读时间
	
	@Column(name="rec_person", length=32)
	private String recPerson;	//收件人id
	
	@Column(name="is_collect", length=1)
	private String  isCollect; //是否收藏   1：收藏0：未收藏
	
	@Column(name="statu",length=1)
	private String statu; //状态   1:已读  0：未读
	
	
	@Column(name="is_delete",length=1)
	private String isDelete;  //是否已删除  1：已删除 0：未删除
	
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNewsId() {
        return newsId;
    }

    public void setNewsId(String newsId) {
        this.newsId = newsId;
    }

    public Date getReadDate() {
        return readDate;
    }

    public void setReadDate(Date readDate) {
        this.readDate = readDate;
    }

    public String getRecPerson() {
        return recPerson;
    }

    public void setRecPerson(String recPerson) {
        this.recPerson = recPerson;
    }

    public String getIsCollect() {
        return isCollect;
    }

    public void setIsCollect(String isCollect) {
        this.isCollect = isCollect;
    }

    public String getStatu() {
        return statu;
    }

    public void setStatu(String statu) {
        this.statu = statu;
    }


    public String getIsDelete() {
        return isDelete;
    }
    public void setIsDelete(String isDelete) {
        this.isDelete = isDelete;
    }

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	
}
