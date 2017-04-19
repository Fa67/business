package com.eayun.obs.model;

import java.math.BigDecimal;
import java.util.Date;

public class ObsUsedType {
	private long download;//下载流量
	private long countRequest;//请求次数
	private double storageUsed;//存储容量
	private String requestCount;
	private String loadDown;
	private String usedStorage;
	private Date time;
	private String timeDis;
	private Date startTime;         //开始时间
	private Date endTime;           //截止时间
	//ecmc1.1
	private BigDecimal ecmcStorageUsed; //存储容量
	private BigDecimal ecmcCountRequest; //请求次数
	private BigDecimal ecmcDownLoad;//下载流量
	private long bucketCount;		//bucket总数
	private String bucketCountStr;  //String类型：bucket总数
	private long objectCount;		//object总数
	private long sent;				//下载流量
	private long received;			//上传流量
	private long putRequestCount;	//put请求次数
	private long getRequestCount;	//get请求次数
	private long deleteRequestCount;	//delete请求次数
	private double totalStorage; //总存储量
	private String objectCountStr;
	private String sentStr;
	private String receivedStr;
	private String putRequestCountStr;
	private String getRequestCountStr;
	private String deleteRequestCountStr;
	private String totalStorageStr;
	private double unStorageUsed; 		//未用流量
	private String unUsedStorage;	//未用总存储
	private String cost;//费用
	
	private String avgCountRequest;//消费统计专用
	private String avgDownLoad;//消费统计专用
	private String avgStorageUsed;//消费统计专用
	
	private long cdnFlow;		//CDN下载流量
	private String cdnFlowStr;	//CDN下载流量（用于显示，加上单位）
	
	private long backsource;		//回源流量
	private String backsourceStr;	//回源流量（用于显示，加上单位）
	
	private long cdnHreqs;	//https请求数
	private String cdnHreqsStr;//https请求数（用于显示，加上单位）
	
	private long cdnDreqs;	//动态请求数
	private String cdnDreqsStr;//动态请求数（用于显示，加上单位）
	
	private String sumBackFlow;	//资源详情 回源流量
	private String sumCdnFlow;		//资源详情 cdn下载
	private String sumCdnDreqs;	//资源详情 动态请求数
	private String sumCdnHreqs;	//资源详情 https请求数
	
	
	
	
	public String getSumBackFlow() {
		return sumBackFlow;
	}
	public void setSumBackFlow(String sumBackFlow) {
		this.sumBackFlow = sumBackFlow;
	}
	public String getSumCdnFlow() {
		return sumCdnFlow;
	}
	public void setSumCdnFlow(String sumCdnFlow) {
		this.sumCdnFlow = sumCdnFlow;
	}
	public String getSumCdnDreqs() {
		return sumCdnDreqs;
	}
	public void setSumCdnDreqs(String sumCdnDreqs) {
		this.sumCdnDreqs = sumCdnDreqs;
	}
	public String getSumCdnHreqs() {
		return sumCdnHreqs;
	}
	public void setSumCdnHreqs(String sumCdnHreqs) {
		this.sumCdnHreqs = sumCdnHreqs;
	}
	public long getCdnHreqs() {
		return cdnHreqs;
	}
	public void setCdnHreqs(long cdnHreqs) {
		this.cdnHreqs = cdnHreqs;
	}
	public String getCdnHreqsStr() {
		return cdnHreqsStr;
	}
	public void setCdnHreqsStr(String cdnHreqsStr) {
		this.cdnHreqsStr = cdnHreqsStr;
	}
	public long getCdnDreqs() {
		return cdnDreqs;
	}
	public void setCdnDreqs(long cdnDreqs) {
		this.cdnDreqs = cdnDreqs;
	}
	public String getCdnDreqsStr() {
		return cdnDreqsStr;
	}
	public void setCdnDreqsStr(String cdnDreqsStr) {
		this.cdnDreqsStr = cdnDreqsStr;
	}
	public String getCost() {
		return cost;
	}
	public void setCost(String cost) {
		this.cost = cost;
	}
	public String getAvgCountRequest() {
		return avgCountRequest;
	}
	public void setAvgCountRequest(String avgCountRequest) {
		this.avgCountRequest = avgCountRequest;
	}
	
	public String getAvgDownLoad() {
		return avgDownLoad;
	}
	public void setAvgDownLoad(String avgDownLoad) {
		this.avgDownLoad = avgDownLoad;
	}
	public String getAvgStorageUsed() {
		return avgStorageUsed;
	}
	public void setAvgStorageUsed(String avgStorageUsed) {
		this.avgStorageUsed = avgStorageUsed;
	}
	public BigDecimal getEcmcDownLoad() {
		return ecmcDownLoad;
	}
	public void setEcmcDownLoad(BigDecimal ecmcDownLoad) {
		this.ecmcDownLoad = ecmcDownLoad;
	}
	public BigDecimal getEcmcCountRequest() {
		return ecmcCountRequest;
	}
	public void setEcmcCountRequest(BigDecimal ecmcCountRequest) {
		this.ecmcCountRequest = ecmcCountRequest;
	}
	public BigDecimal getEcmcStorageUsed() {
		return ecmcStorageUsed;
	}
	public void setEcmcStorageUsed(BigDecimal ecmcStorageUsed) {
		this.ecmcStorageUsed = ecmcStorageUsed;
	}
	public String getUnUsedStorage() {
		return unUsedStorage;
	}
	public void setUnUsedStorage(String unUsedStorage) {
		this.unUsedStorage = unUsedStorage;
	}
	public double getUnStorageUsed() {
		return unStorageUsed;
	}
	public void setUnStorageUsed(double unStorageUsed) {
		this.unStorageUsed = unStorageUsed;
	}
	public String getObjectCountStr() {
		return objectCountStr;
	}
	public void setObjectCountStr(String objectCountStr) {
		this.objectCountStr = objectCountStr;
	}
	public String getSentStr() {
		return sentStr;
	}
	public void setSentStr(String sentStr) {
		this.sentStr = sentStr;
	}
	public String getReceivedStr() {
		return receivedStr;
	}
	public void setReceivedStr(String receivedStr) {
		this.receivedStr = receivedStr;
	}
	public String getPutRequestCountStr() {
		return putRequestCountStr;
	}
	public void setPutRequestCountStr(String putRequestCountStr) {
		this.putRequestCountStr = putRequestCountStr;
	}
	public String getGetRequestCountStr() {
		return getRequestCountStr;
	}
	public void setGetRequestCountStr(String getRequestCountStr) {
		this.getRequestCountStr = getRequestCountStr;
	}
	public String getDeleteRequestCountStr() {
		return deleteRequestCountStr;
	}
	public void setDeleteRequestCountStr(String deleteRequestCountStr) {
		this.deleteRequestCountStr = deleteRequestCountStr;
	}
	public String getTotalStorageStr() {
		return totalStorageStr;
	}
	public void setTotalStorageStr(String totalStorageStr) {
		this.totalStorageStr = totalStorageStr;
	}
	public String getBucketCountStr() {
		return bucketCountStr;
	}
	public void setBucketCountStr(String bucketCountStr) {
		this.bucketCountStr = bucketCountStr;
	}
	public double getTotalStorage() {
		return totalStorage;
	}
	public void setTotalStorage(double totalStorage) {
		this.totalStorage = totalStorage;
	}
	public long getBucketCount() {
		return bucketCount;
	}
	public void setBucketCount(long bucketCount) {
		this.bucketCount = bucketCount;
	}
	public long getObjectCount() {
		return objectCount;
	}
	public void setObjectCount(long objectCount) {
		this.objectCount = objectCount;
	}
	public long getSent() {
		return sent;
	}
	public void setSent(long sent) {
		this.sent = sent;
	}
	public long getReceived() {
		return received;
	}
	public void setReceived(long received) {
		this.received = received;
	}
	public long getPutRequestCount() {
		return putRequestCount;
	}
	public void setPutRequestCount(long putRequestCount) {
		this.putRequestCount = putRequestCount;
	}
	public long getGetRequestCount() {
		return getRequestCount;
	}
	public void setGetRequestCount(long getRequestCount) {
		this.getRequestCount = getRequestCount;
	}
	public long getDeleteRequestCount() {
		return deleteRequestCount;
	}
	public void setDeleteRequestCount(long deleteRequestCount) {
		this.deleteRequestCount = deleteRequestCount;
	}
	public long getDownload() {
		return download;
	}
	public void setDownload(long download) {
		this.download = download;
	}
	
	public double getStorageUsed() {
		return storageUsed;
	}
	public void setStorageUsed(double storageUsed) {
		this.storageUsed = storageUsed;
	}
	public long getCountRequest() {
		return countRequest;
	}
	public void setCountRequest(long countRequest) {
		this.countRequest = countRequest;
	}
	public String getRequestCount() {
		return requestCount;
	}
	public void setRequestCount(String requestCount) {
		this.requestCount = requestCount;
	}
	public String getLoadDown() {
		return loadDown;
	}
	public void setLoadDown(String loadDown) {
		this.loadDown = loadDown;
	}
	public String getUsedStorage() {
		return usedStorage;
	}
	public void setUsedStorage(String usedStorage) {
		this.usedStorage = usedStorage;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public String getTimeDis() {
		return timeDis;
	}
	public void setTimeDis(String timeDis) {
		this.timeDis = timeDis;
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
	public long getCdnFlow() {
		return cdnFlow;
	}
	public void setCdnFlow(long cdnFlow) {
		this.cdnFlow = cdnFlow;
	}
	public String getCdnFlowStr() {
		return cdnFlowStr;
	}
	public void setCdnFlowStr(String cdnFlowStr) {
		this.cdnFlowStr = cdnFlowStr;
	}
	public long getBacksource() {
		return backsource;
	}
	public void setBacksource(long backsource) {
		this.backsource = backsource;
	}
	public String getBacksourceStr() {
		return backsourceStr;
	}
	public void setBacksourceStr(String backsourceStr) {
		this.backsourceStr = backsourceStr;
	}
	
	
	
	
	
	
	
	    

}
