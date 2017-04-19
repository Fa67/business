package com.eayun.work.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "work_opinion")
public class BaseWorkOpinion implements Serializable{

	private static final long serialVersionUID = 1902181914963984575L;
	@Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "id", unique = true, nullable = false, length = 32)
	private String opinionId;	//ID
	@Column(name = "workid", length = 32)
	private String workId;	//工单id
	@Column(name = "opinion_content", length = 12000)
	private String opinionContent;	//回复内容
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "cre_date")
	private Date opinionTime;	//回复时间
	
	@Column(name = "cre_user_id", length = 32)
	private String creUser;	//回复者
	@Column(name = "reply_user", length = 32)
	private String replyUser;	//回复人 
	@Column(name = "flag", length = 32)
	private String flag;	//回复时当时的工单状态状态
	@Column(name = "opinion_state", length = 50)
	private String opinionState;// 特殊状态0：转办，1回退
	@Column(name = "is_ecmc_cre", length = 50)
	private String ecmcCre;//0：是，1：不是
	@Column(name = "cre_username", length = 50)
	private String creUserName;//创建者名称
	@Column(name = "reply_username", length = 50)
	private String replyUserName;//接收者名称
	
	public String getOpinionId() {
		return opinionId;
	}
	public void setOpinionId(String opinionId) {
		this.opinionId = opinionId;
	}
	public String getWorkId() {
		return workId;
	}
	public void setWorkId(String workId) {
		this.workId = workId;
	}
	public String getOpinionContent() {
		return opinionContent;
	}
	public void setOpinionContent(String opinionContent) {
		this.opinionContent = opinionContent;
	}
	
	public Date getOpinionTime() {
		return opinionTime;
	}
	public void setOpinionTime(Date opinionTime) {
		this.opinionTime = opinionTime;
	}
	public String getCreUser() {
		return creUser;
	}
	public void setCreUser(String creUser) {
		this.creUser = creUser;
	}
	public String getReplyUser() {
		return replyUser;
	}
	public void setReplyUser(String replyUser) {
		this.replyUser = replyUser;
	}
    public String getFlag() {
        return flag;
    }
    public void setFlag(String flag) {
        this.flag = flag;
    }
	public String getOpinionState() {
		return opinionState;
	}
	public void setOpinionState(String opinionState) {
		this.opinionState = opinionState;
	}
	public String getEcmcCre() {
		return ecmcCre;
	}
	public void setEcmcCre(String ecmcCre) {
		this.ecmcCre = ecmcCre;
	}

	public String getCreUserName() {
		return creUserName;
	}

	public void setCreUserName(String creUserName) {
		this.creUserName = creUserName;
	}

	public String getReplyUserName() {
		return replyUserName;
	}

	public void setReplyUserName(String replyUserName) {
		this.replyUserName = replyUserName;
	}
}
