package com.eayun.ecmcschedule.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "schedule_statistics")
public class BaseEcmcScheduleStatistics implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public BaseEcmcScheduleStatistics(){
		
	}
	
	public BaseEcmcScheduleStatistics(String triggerName, Date statisticsDate){
		this.triggerName = triggerName;
		this.statisticsDate = statisticsDate;
		this.totalCount = 0;
		this.sucCount = 0;
		this.falCount = 0;
	}
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "id", unique = true, nullable = false, length = 32)
	private String id;
	
	@Column(name = "trigger_name", nullable = false, length = 200)
	private String triggerName;
	
	@Column(name = "statistics_date", nullable = false)
	private Date statisticsDate;
	
	@Column(name = "total_count", nullable = false, length = 11)
	private int totalCount;
	
	@Column(name = "suc_count", nullable = false, length = 11)
	private int sucCount;
	
	@Column(name = "fal_count", nullable = false, length = 11)
	private int falCount;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTriggerName() {
		return triggerName;
	}

	public void setTriggerName(String triggerName) {
		this.triggerName = triggerName;
	}

	public Date getStatisticsDate() {
		return statisticsDate;
	}

	public void setStatisticsDate(Date statisticsDate) {
		this.statisticsDate = statisticsDate;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getSucCount() {
		return sucCount;
	}

	public void setSucCount(int sucCount) {
		this.sucCount = sucCount;
	}

	public int getFalCount() {
		return falCount;
	}

	public void setFalCount(int falCount) {
		this.falCount = falCount;
	}
	
}
