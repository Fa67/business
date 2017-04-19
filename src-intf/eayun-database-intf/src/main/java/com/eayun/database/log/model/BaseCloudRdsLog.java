package com.eayun.database.log.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "cloud_rdslog")
public class BaseCloudRdsLog {
	@Id
	@Column(name = "rdslog_id", unique = true, nullable = false, length = 100)
	private String rdsLogId;				//日志ID
	@Column(name = "log_name", length = 100)
	private String logName;				    //日志文件名称
	@Column(name = "dc_id", length = 100)
	private String dcId;					//数据中心ID
	@Column(name = "prj_id", length = 100)
	private String prjId;					//项目ID
	@Column(name = "rdsinstance_id", length = 100)
	private String rdsInstanceId;			//RDS实例ID
	@Column(name = "log_type", length = 10)
	private String logType;			//RDS实例ID
	@Column(name = "container", length = 200)
	private String container;				//obs对象所在的Container
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "publish_time", length = 25)
	private Date publishTime;				//OBS对象发布时间
	@Column(name = "url", length = 500)
	private String url;						//日志文件的完成OBS对象的url
	@Column(name = "size", length = 200)
	private long size;						//日志文件大小（单位 byte）
	
	public String getRdsLogId() {
		return rdsLogId;
	}
	public void setRdsLogId(String rdsLogId) {
		this.rdsLogId = rdsLogId;
	}
	public String getLogName() {
		return logName;
	}
	public void setLogName(String logName) {
		this.logName = logName;
	}
	public String getDcId() {
		return dcId;
	}
	public void setDcId(String dcId) {
		this.dcId = dcId;
	}
	public String getPrjId() {
		return prjId;
	}
	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}
	public String getRdsInstanceId() {
		return rdsInstanceId;
	}
	public void setRdsInstanceId(String rdsInstanceId) {
		this.rdsInstanceId = rdsInstanceId;
	}
	public String getContainer() {
		return container;
	}
	public void setContainer(String container) {
		this.container = container;
	}
	public Date getPublishTime() {
		return publishTime;
	}
	public void setPublishTime(Date publishTime) {
		this.publishTime = publishTime;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getLogType() {
		return logType;
	}
	public void setLogType(String logType) {
		this.logType = logType;
	}
}
