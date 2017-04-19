package com.eayun.dashboard.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * 总览收入统计图表映射表
 * @author bo.zeng@eayun.com
 *
 */
@Entity
@Table(name = "overview_income_chart")
public class BaseOverviewIncomeChart implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "id", unique = true, nullable = false, length = 32)
	private String id;
	
	/**
	 * 客户ID
	 */
	@Column(name = "cus_id")
	private String cusId;
	
	/**
	 * 金额
	 */
	@Column(name = "money")
	private BigDecimal money = new BigDecimal(0.000);
	
	/**
	 * 记录时间
	 */
	@Column(name = "record_time")
	private Date recordTime;
	
	/**
	 * 统计开始时间
	 */
	@Column(name = "start_time")
	private Date startTime;
	
	/**
	 * 统计结束时间
	 */
	@Column(name = "end_time")
	private Date endTime;
	
	/**
	 * 排序
	 */
	@Column(name = "sort")
	private int sort;
	
	/**
	 * 统计类型(0-按收入金额统计; 1-按按需消费金额统计)
	 */
	@Column(name = "statistic_type")
	private String statisticType;
	
	/**
	 * 统计周期类型(0-全部; 1-昨日; 2-近7日; 3-近30日; 4-近90日; 5-年; )
	 */
	@Column(name = "period_type")
	private String periodType;
	
	/**
	 * 年份(当period_type为5时生效)
	 */
	@Column(name = "year")
	private String year;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCusId() {
		return cusId;
	}

	public void setCusId(String cusId) {
		this.cusId = cusId;
	}

	public BigDecimal getMoney() {
		return money;
	}

	public void setMoney(BigDecimal money) {
		this.money = money;
	}

	public Date getRecordTime() {
		return recordTime;
	}

	public void setRecordTime(Date recordTime) {
		this.recordTime = recordTime;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}

	public String getStatisticType() {
		return statisticType;
	}

	public void setStatisticType(String statisticType) {
		this.statisticType = statisticType;
	}

	public String getPeriodType() {
		return periodType;
	}

	public void setPeriodType(String periodType) {
		this.periodType = periodType;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

}
