package com.eayun.obs.model;

import java.math.BigDecimal;
import java.util.Date;
/**
 * bucket存储用量
 * @Filename: BucketStorageBean.java
 * @Description: 
 * @Version: 1.0
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br>
 *<li>Date: 2016年1月26日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class BucketStorageBean {
	private Date timestamp;    //此条数据的采集时间
	private BigDecimal bucketStorage;     //bucketStorage存储空间
	private BigDecimal minStorage;
	private BigDecimal maxStorage;
	private BigDecimal cdnFlow;		//CDN下载流量
	
	
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public BigDecimal getBucketStorage() {
		return bucketStorage;
	}
	public void setBucketStorage(BigDecimal bucketStorage) {
		this.bucketStorage = bucketStorage;
	}
	public BigDecimal getMinStorage() {
		return minStorage;
	}
	public void setMinStorage(BigDecimal minStorage) {
		this.minStorage = minStorage;
	}
	public BigDecimal getMaxStorage() {
		return maxStorage;
	}
	public void setMaxStorage(BigDecimal maxStorage) {
		this.maxStorage = maxStorage;
	}
	public BigDecimal getCdnFlow() {
		return cdnFlow;
	}
	public void setCdnFlow(BigDecimal cdnFlow) {
		this.cdnFlow = cdnFlow;
	}
	
	
}
