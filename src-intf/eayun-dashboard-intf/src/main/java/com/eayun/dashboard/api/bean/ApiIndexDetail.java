package com.eayun.dashboard.api.bean;

import java.util.Date;

/**
 * API概览指标信息实体类
 *                       
 * @Filename: ApiIndexDetail.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2016年11月25日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class ApiIndexDetail {
	
	private String cusId;		//客户id  OperatorId  Status  CreateTime
	
	private long successCount;	//成功次数
	
	private long failCount;		//失败次数
	
	private long totalCount;	//总次数
	
	private Date timestamp;		//访问时间
	
	private long maxCount;		//最大次数
	
	private long minCount;		//最小次数

	public String getCusId() {
		return cusId;
	}

	public void setCusId(String cusId) {
		this.cusId = cusId;
	}

	public long getSuccessCount() {
		return successCount;
	}

	public void setSuccessCount(long successCount) {
		this.successCount = successCount;
	}

	public long getFailCount() {
		return failCount;
	}

	public void setFailCount(long failCount) {
		this.failCount = failCount;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public long getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(long maxCount) {
		this.maxCount = maxCount;
	}

	public long getMinCount() {
		return minCount;
	}

	public void setMinCount(long minCount) {
		this.minCount = minCount;
	}
	
	
	
}
