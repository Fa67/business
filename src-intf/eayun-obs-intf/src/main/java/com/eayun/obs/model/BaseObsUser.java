package com.eayun.obs.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 对象存储用户实体类
 * 
 * @author xiangyu.cao@eayun.com
 *
 */
@Entity
@Table(name = "obs_user")
public class BaseObsUser implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "user_id")
	private String userId;		//存储对象用户id
	
	@Column(name = "display_name")		
	private String displayName;		//新创建用户的显示名字
	
	@Column(name = "suspended")
	private String suspended;		//新创建用户是否被暂停服务
	
	@Column(name = "cus_id")
	private String cusId;		//当前客户id
	
	@Column(name = "max_buckets")
	private int maxBuckets;		//用户能创建的最大bucket数目
	
	@Column(name = "subusers")
	private String subUsers;		//关联到这个数组的子用户
	
	
	
	@Column(name = "swift_keys")
	private String swiftKeys;		//关联到该用户的swift key
	
	@Column(name = "caps")
	private String caps;		//用户具有的系统权限

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getSuspended() {
		return suspended;
	}

	public void setSuspended(String suspended) {
		this.suspended = suspended;
	}

	public String getCusId() {
		return cusId;
	}

	public void setCusId(String cusId) {
		this.cusId = cusId;
	}

	public int getMaxBuckets() {
		return maxBuckets;
	}

	public void setMaxBuckets(int maxBuckets) {
		this.maxBuckets = maxBuckets;
	}

	public String getSubUsers() {
		return subUsers;
	}

	public void setSubUsers(String subUsers) {
		this.subUsers = subUsers;
	}

	

	public String getSwiftKeys() {
		return swiftKeys;
	}

	public void setSwiftKeys(String swiftKeys) {
		this.swiftKeys = swiftKeys;
	}

	public String getCaps() {
		return caps;
	}

	public void setCaps(String caps) {
		this.caps = caps;
	}

}
