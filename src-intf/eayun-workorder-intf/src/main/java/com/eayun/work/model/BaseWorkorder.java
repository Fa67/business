package com.eayun.work.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
@Entity
@Table(name = "workorder")
public class BaseWorkorder implements Serializable{
	
	private static final long serialVersionUID = 2673047480840924946L;
	
	
	@Id
    @Column(name = "workid", unique = true, nullable = false, length = 32)
	private String workId;	//ID
	
	@Column(name = "order_num", length = 32)
	private String workNum;	//工单编号
	
    @Column(name = "title", length = 200)
	private String workTitle;	//工单标题
	
    @Column(name = "content", length = 2000)
	private String workContent;	//工单内容
	
    @Column(name = "order_type", length = 50)
	private String workType;	//工单类别
    
    @Column(name = "order_level", length = 100)
	private String workLevel;	//工单级别
	
    @Column(name = "apply_customer", length = 500)
    private String applyCustomer; //工单申请客户
    
    @Column(name = "send_mes_flag", length = 50)
    private String sendMesFlag; //发送短信状态0:未发送1:受理状态是已发送响应状态是未发送2:响应状态已发送3:没有发送过短信4:发送过短信(3和4状态用在给客户发送)
    
    @Column(name = "cre_user", length = 50)
	private String workCreUser;	//工单创建人
	
    @Column(name = "order_flag", length = 1)
	private String workCreRole="2";	//创建工单角色--0:客服创建1:运维或者管理员创建2:控制台创建
	
	@Column(name = "apply_user", length = 32)
	private String workApplyUser;	//工单申请人
	
	@Column(name = "phone", length = 32)
	private String workPhone;	//联系电话
	
	@Column(name = "head_user", length = 32)
	private String workHeadUser;	//负责人
	
	@Column(name = "cre_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date workCreTime;	//创建时间
	
	@Column(name = "workorder_assess_id", length = 32)
	private String flowId;	//工单当前流程
	
	@Column(name = "flag", length = 50)
	private String workFalg="0";	//ecmc工单当前状态:0待处理1:处理中2:已解决3:已完结 4:已取消 5:已删除
	
	@Column(name = "work_phone_time", length = 1)
    private String workPhoneTime;    //接受短信提醒时间段
    
    @Column(name = "work_email", length = 100)
    private String workEmail;   //联系邮箱
    
    @Column(name = "work_falg", length = 32)
    private String workEcscFalg="0";   //ecsc工单状态:0:待受理1:处理中2:待反馈3:待确认 4:待评价5:已关闭 6:已删除
    
    @Column(name = "work_complain", length = 32)
    private String workComplain="0";   //是否投诉 0:未投诉 1:已投诉
    
    @Column(name = "work_highly", length = 32)
    private String workHighly;   //评价 0:不满意 1:满意
    
    @Column(name = "work_state", length = 32)
    private String workState;   //评价 0/null:未审核 1:审核通过2:审核未通过

	@Column(name = "apply_username", length = 32)
    private String workApplyUserName;   //工单申请人

	@Column(name = "head_username", length = 32)
	private String workHeadUserName;   //工单责任人

	@Column(name = "cre_username", length = 32)
	private String workCreUserName;   //工单创建人

	/**
	 * 工单是否被自动确认的标识
	 */
	@Column(name = "is_autoconfirm", length = 1)
	private String isAutoconfirm ; //是否为自动确认

	/**
	 * 工单是否被自动评价的标识
	 */
	@Column(name = "is_autoevaluation", length = 1)
	private String isAutoevaluation ; //是否为自动评价


	public String getWorkId() {
		return workId;
	}
	public void setWorkId(String workId) {
		this.workId = workId;
	}
	public String getWorkNum() {
		return workNum;
	}
	public void setWorkNum(String workNum) {
		this.workNum = workNum;
	}
	public String getWorkTitle() {
		return workTitle;
	}
	public void setWorkTitle(String workTitle) {
		this.workTitle = workTitle;
	}
	public String getWorkContent() {
		return workContent;
	}
	public void setWorkContent(String workContent) {
		this.workContent = workContent;
	}
	public String getWorkType() {
		return workType;
	}
	public void setWorkType(String workType) {
		this.workType = workType;
	}
	public String getWorkLevel() {
		return workLevel;
	}
	public void setWorkLevel(String workLevel) {
		this.workLevel = workLevel;
	}
	public String getWorkCreUser() {
		return workCreUser;
	}
	public void setWorkCreUser(String workCreUser) {
		this.workCreUser = workCreUser;
	}
	
	public String getWorkCreRole() {
        return workCreRole;
    }
    public void setWorkCreRole(String workCreRole) {
        this.workCreRole = workCreRole;
    }
    public String getWorkApplyUser() {
		return workApplyUser;
	}
	public void setWorkApplyUser(String workApplyUser) {
		this.workApplyUser = workApplyUser;
	}
	public String getWorkPhone() {
		return workPhone;
	}
	public void setWorkPhone(String workPhone) {
		this.workPhone = workPhone;
	}
	
	public String getApplyCustomer() {
        return applyCustomer;
    }
    public void setApplyCustomer(String applyCustomer) {
        this.applyCustomer = applyCustomer;
    }
    public String getSendMesFlag() {
        return sendMesFlag;
    }
    public void setSendMesFlag(String sendMesFlag) {
        this.sendMesFlag = sendMesFlag;
    }
    public String getWorkPhoneTime() {
        return workPhoneTime;
    }
    public void setWorkPhoneTime(String workPhoneTime) {
        this.workPhoneTime = workPhoneTime;
    }
    public String getWorkEmail() {
		return workEmail;
	}
	public void setWorkEmail(String workEmail) {
		this.workEmail = workEmail;
	}
	public Date getWorkCreTime() {
		return workCreTime;
	}
	public void setWorkCreTime(Date workCreTime) {
		this.workCreTime = workCreTime;
	}
	public String getFlowId() {
		return flowId;
	}
	public void setFlowId(String flowId) {
		this.flowId = flowId;
	}
	public String getWorkFalg() {
		return workFalg;
	}
	public void setWorkFalg(String workFalg) {
		this.workFalg = workFalg;
	}
    public String getWorkEcscFalg() {
        return workEcscFalg;
    }
    public void setWorkEcscFalg(String workEcscFalg) {
        this.workEcscFalg = workEcscFalg;
    }
    public String getWorkComplain() {
        return workComplain;
    }
    public void setWorkComplain(String workComplain) {
        this.workComplain = workComplain;
    }
    public String getWorkHighly() {
        return workHighly;
    }
    public void setWorkHighly(String workHighly) {
        this.workHighly = workHighly;
    }
    public String getWorkHeadUser() {
        return workHeadUser;
    }
    public void setWorkHeadUser(String workHeadUser) {
        this.workHeadUser = workHeadUser;
    }
	public String getWorkState() {
		return workState;
	}
	public void setWorkState(String workState) {
		this.workState = workState;
	}

	public String getWorkApplyUserName() {
		return workApplyUserName;
	}

	public void setWorkApplyUserName(String workApplyUserName) {
		this.workApplyUserName = workApplyUserName;
	}

	public String getWorkHeadUserName() {
		return workHeadUserName;
	}

	public void setWorkHeadUserName(String workHeadUserName) {
		this.workHeadUserName = workHeadUserName;
	}

	public String getWorkCreUserName() {
		return workCreUserName;
	}

	public void setWorkCreUserName(String workCreUserName) {
		this.workCreUserName = workCreUserName;
	}

	public String getIsAutoconfirm() {
		return isAutoconfirm;
	}

	public void setIsAutoconfirm(String isAutoconfirm) {
		this.isAutoconfirm = isAutoconfirm;
	}

	public String getIsAutoevaluation() {
		return isAutoevaluation;
	}

	public void setIsAutoevaluation(String isAutoevaluation) {
		this.isAutoevaluation = isAutoevaluation;
	}

	@Override
	public String toString() {
		return "BaseWorkorder{" +
				"workId='" + workId + '\'' +
				", workNum='" + workNum + '\'' +
				", workTitle='" + workTitle + '\'' +
				", workContent='" + workContent + '\'' +
				", workType='" + workType + '\'' +
				", workLevel='" + workLevel + '\'' +
				", applyCustomer='" + applyCustomer + '\'' +
				", sendMesFlag='" + sendMesFlag + '\'' +
				", workCreUser='" + workCreUser + '\'' +
				", workCreRole='" + workCreRole + '\'' +
				", workApplyUser='" + workApplyUser + '\'' +
				", workPhone='" + workPhone + '\'' +
				", workHeadUser='" + workHeadUser + '\'' +
				", workCreTime=" + workCreTime +
				", flowId='" + flowId + '\'' +
				", workFalg='" + workFalg + '\'' +
				", workPhoneTime='" + workPhoneTime + '\'' +
				", workEmail='" + workEmail + '\'' +
				", workEcscFalg='" + workEcscFalg + '\'' +
				", workComplain='" + workComplain + '\'' +
				", workHighly='" + workHighly + '\'' +
				", workState='" + workState + '\'' +
				", workApplyUserName='" + workApplyUserName + '\'' +
				", workHeadUserName='" + workHeadUserName + '\'' +
				", workCreUserName='" + workCreUserName + '\'' +
				'}';
	}
}
