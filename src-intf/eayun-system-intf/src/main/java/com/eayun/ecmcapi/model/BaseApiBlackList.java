package com.eayun.ecmcapi.model;

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
@Table(name = "api_blacklist")
public class BaseApiBlackList implements Serializable{

	private static final long serialVersionUID = 1916210935993738316L;
	
	
	private String apiId;       //apiId
	private String apiType;     //类型
	private String apiValue;    //cusId或用户IP
	private String memo;        //描述
	private Date createTime;    //创建时间
	
	@Id
	@GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "api_id", unique = true, nullable = false, length = 32)
	public String getApiId() {
		return apiId;
	}
	public void setApiId(String apiId) {
		this.apiId = apiId;
	}
	
	@Column(name = "api_type",length = 32)
	public String getApiType() {
		return apiType;
	}
	public void setApiType(String apiType) {
		this.apiType = apiType;
	}
	
	@Column(name = "api_value",length = 32)
	public String getApiValue() {
		return apiValue;
	}
	public void setApiValue(String apiValue) {
		this.apiValue = apiValue;
	}
	
	@Column(name = "memo",length = 2000)
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_time" , length = 19)
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	
	
	
	
	
	
	
	
	
	
}
