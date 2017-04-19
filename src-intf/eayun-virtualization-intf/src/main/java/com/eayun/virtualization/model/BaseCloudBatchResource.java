package com.eayun.virtualization.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
/**
 * 批量创建订单
 * @author zhouhaitao
 *
 */

@Entity
@Table(name = "cloud_batchresource")
public class BaseCloudBatchResource implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6113680267060046342L;
	@Id
	@Column(name = "resource_id",  length = 100)
	private String resourceId;
	@Column(name = "resource_type",  length = 1)
	private String resourceType;
	@Id
	@Column(name = "order_no",  length = 18)
	private String orderNo;
	
	
	public String getResourceId() {
		return resourceId;
	}
	public String getResourceType() {
		return resourceType;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
}
