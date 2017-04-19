package com.eayun.obs.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Bucket请求次数与使用量
 * 
 * @Filename: BucketStorageBean.java
 * @Description:
 * @Version: 1.0
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br> <li>Date: 2016年1月27日</li> <li>Version: 1.0</li> <li>Content:
 *               create</li>
 * 
 */
public class BucketUesdAndRequestBean {
	private BigDecimal netIn;  //double
	private BigDecimal netOut;//double
	private BigDecimal netMax;//double
	private BigDecimal netMin;//double
	private BigDecimal requestDelete;//long
	private BigDecimal requestPut;//long
	private BigDecimal requestGet;//long
	private BigDecimal requestMinTimes;//double
	private BigDecimal requestMaxTimes;//double
	private Date timestamp;    //此条数据的采集时间
	private String type;//请求次数或为流量流入流出


	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public BigDecimal getNetIn() {
		return netIn;
	}

	public void setNetIn(BigDecimal netIn) {
		this.netIn = netIn;
	}

	public BigDecimal getNetOut() {
		return netOut;
	}

	public void setNetOut(BigDecimal netOut) {
		this.netOut = netOut;
	}

	public BigDecimal getNetMax() {
		return netMax;
	}

	public void setNetMax(BigDecimal netMax) {
		this.netMax = netMax;
	}

	public BigDecimal getNetMin() {
		return netMin;
	}

	public void setNetMin(BigDecimal netMin) {
		this.netMin = netMin;
	}

	public BigDecimal getRequestDelete() {
		return requestDelete;
	}

	public void setRequestDelete(BigDecimal requestDelete) {
		this.requestDelete = requestDelete;
	}

	public BigDecimal getRequestPut() {
		return requestPut;
	}

	public void setRequestPut(BigDecimal requestPut) {
		this.requestPut = requestPut;
	}

	public BigDecimal getRequestGet() {
		return requestGet;
	}

	public void setRequestGet(BigDecimal requestGet) {
		this.requestGet = requestGet;
	}

	public BigDecimal getRequestMinTimes() {
		return requestMinTimes;
	}

	public void setRequestMinTimes(BigDecimal requestMinTimes) {
		this.requestMinTimes = requestMinTimes;
	}

	public BigDecimal getRequestMaxTimes() {
		return requestMaxTimes;
	}

	public void setRequestMaxTimes(BigDecimal requestMaxTimes) {
		this.requestMaxTimes = requestMaxTimes;
	}


}
