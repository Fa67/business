package com.eayun.order.model;

import java.util.Date;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.eayun.common.constant.BillingCycleType;
import com.eayun.common.constant.OrderStateType;
import com.eayun.common.constant.OrderType;
import com.eayun.common.constant.PayType;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.constant.ThirdPartType;

/**
 *                       
 * @Filename: Order.java
 * @Description: BaseOrder扩展类
 * @Version: 1.0
 * @Author: bo.zeng
 * @Email: bo.zeng@eayun.com
 * @History:<br>
 *<li>Date: 2016年7月27日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class Order extends BaseOrder {
	
	/**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 5626079125367406969L;

    /**
	 *Comment for <code>businessParams</code>
	 *业务参数
	 */
	private Map<String, Object> businessParams;
	
	/**
	 *Comment for <code>cusName</code>
	 *客户名称
	 */
	private String cusName;
	
	/**
	 *Comment for <code>orderTypeName</code>
	 *订单类型中文名称
	 */
	private String orderTypeName;
	
	/**
	 *Comment for <code>orderPayTypeName</code>
	 *订单付费类型中文名称
	 */
	private String payTypeName;
	
	/**
	 *Comment for <code>billingCycleTypeName</code>
	 *订单计费周期中文名称
	 */
	private String billingCycleTypeName;
	
	/**
	 *Comment for <code>resourceTypeName</code>
	 *资源类型中文名称
	 */
	private String resourceTypeName;
	
	/**
	 *Comment for <code>orderStateName</code>
	 *订单状态中文名称
	 */
	private String orderStateName;
	
	/**
	 *Comment for <code>thirdPartTypeName</code>
	 *第三方支付名称
	 */
	private String thirdPartTypeName;
	
	/**
	 *Comment for <code>origExpireTime</code>
	 *资源原到期时间（针对续费订单）
	 */
	private Date origExpireTime;

	public String getCusName() {
		return cusName;
	}

	public void setCusName(String cusName) {
		this.cusName = cusName;
	}

	public Map<String, Object> getBusinessParams() {
		return businessParams;
	}

	public void setBusinessParams(Map<String, Object> businessParams) {
		super.setParams(JSON.toJSONString(businessParams));
		this.businessParams = businessParams;
	}

	public String getOrderTypeName() {
		return orderTypeName;
	}

	public void setOrderTypeName(String orderTypeName) {
		this.orderTypeName = orderTypeName;
	}

	public String getPayTypeName() {
		return payTypeName;
	}

	public void setPayTypeName(String payTypeName) {
		this.payTypeName = payTypeName;
	}

	public String getBillingCycleTypeName() {
		return billingCycleTypeName;
	}

	public void setBillingCycleTypeName(String billingCycleTypeName) {
		this.billingCycleTypeName = billingCycleTypeName;
	}

	public String getResourceTypeName() {
		return resourceTypeName;
	}

	public void setResourceTypeName(String resourceTypeName) {
		this.resourceTypeName = resourceTypeName;
	}

	public String getOrderStateName() {
		return orderStateName;
	}

	public void setOrderStateName(String orderStateName) {
		this.orderStateName = orderStateName;
	}
	
	public String getThirdPartTypeName() {
		return thirdPartTypeName;
	}

	public void setThirdPartTypeName(String thirdPartTypeName) {
		this.thirdPartTypeName = thirdPartTypeName;
	}

	public Date getOrigExpireTime() {
		return origExpireTime;
	}

	public void setOrigExpireTime(Date origExpireTime) {
		this.origExpireTime = origExpireTime;
	}

	public void getTypeName(){
		setOrderTypeName(OrderType.getName(super.getOrderType() == null ? "" : super.getOrderType()));
		setPayTypeName(PayType.getName(super.getPayType() == null ? "" : super.getPayType()));
		setBillingCycleTypeName(BillingCycleType.getName(super.getBillingCycle() == null ? "" : super.getBillingCycle()));
		setResourceTypeName(ResourceType.getName(super.getResourceType() == null ? "" : super.getResourceType()));
		setOrderStateName(OrderStateType.getName(super.getOrderState() == null ? "" : super.getOrderState()));
		setThirdPartTypeName(ThirdPartType.getName(super.getThirdPartType() == null ? "" : super.getThirdPartType()));
	}
	
}
