package com.eayun.ecmcschedule.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author zengbo 任务信息表映射实体类
 *
 */
@Entity
@Table(name = "schedule_info")
public class BaseEcmcScheduleInfo implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	
	//ID（关联）
	@Id
	@Column(name = "trigger_name", unique = true, nullable = false, length = 200)
	private String triggerName;
	
	//任务名称
	@Column(name = "task_name", unique = true, nullable = false, length = 200)
	private String taskName;
	
	//创建时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_time", nullable = false, updatable=false)
	private Date createTime;
	
	//创建人ID
	@Column(name = "create_by", length = 32, updatable=false)
	private String createBy;

	public String getTriggerName() {
		return triggerName;
	}

	public void setTriggerName(String triggerName) {
		this.triggerName = triggerName;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	

}
