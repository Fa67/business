package com.eayun.notice.model;

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
 * @Filename: BaseNotice.java
 * @Description: 
 * @Version: 1.0
 * @Author:wang ning
 * @Email: ning.wang@eayun.com
 * @History:<br>
 *<li>Date: 2015年10月9日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "notice_info")
public class BaseNotice implements java.io.Serializable{

    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 6112047727160812264L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name="notice_id", length=32)
    private String id;   //id
    
    @Column(name="memo",length=100)
    private String memo;//摘要
    
    @Column(name="valid_time",length=7)
    private Date validTime; //生效时间
    
    @Column(name="invalid_time",length=100)
    private Date invalidTime;//失效时间
    
    @Column(name="is_used",length=1)
    private String isUsed;//是否启用，1启用0，未启用
    
    @Column(name="notice_url",length=255)
    private String url;//路径
    
    @Column(name="title",length=100)
    private String title;//标题
    
    @Column(name="content",length=2000)
    private String content;//内容
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public Date getValidTime() {
        return validTime;
    }
    public void setValidTime(Date validTime) {
        this.validTime = validTime;
    }
    public Date getInvalidTime() {
        return invalidTime;
    }
    public void setInvalidTime(Date invalidTime) {
        this.invalidTime = invalidTime;
    }
    public String getIsUsed() {
        return isUsed;
    }
    public void setIsUsed(String isUsed) {
        this.isUsed = isUsed;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
   
    
}
