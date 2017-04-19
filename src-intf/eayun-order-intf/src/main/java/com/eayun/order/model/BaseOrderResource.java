package com.eayun.order.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 *                       
 * @Filename: OrderResource.java
 * @Description: 订单资源
 * @Version: 1.0
 * @Author: bo.zeng
 * @Email: bo.zeng@eayun.com
 * @History:<br>
 *<li>Date: 2016年8月12日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "order_resource")
public class BaseOrderResource implements java.io.Serializable {
	
	/**
	 *Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 主键UUID
	 */
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "id", unique = true, nullable = false, length = 32)
	private String id;
	
	/**
	 *Comment for <code>orderNo</code>
	 *订单编号
	 */
	@Column(name = "order_no", length = 18, updatable=false, nullable=false)
	private String orderNo;
	
	/**
	 *Comment for <code>resourceId</code>
	 *资源ID
	 */
	@Column(name = "resource_id", length = 32, updatable=false, nullable=false)
	private String resourceId;

	/**
	 *Comment for <code>resourceName</code>
	 *资源名称
	 */
	@Column(name = "resource_name", length = 64, updatable=false, nullable=false)
	private String resourceName;
	
	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

}
