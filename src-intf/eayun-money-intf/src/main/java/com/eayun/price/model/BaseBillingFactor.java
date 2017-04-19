package com.eayun.price.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * 计费因子-价格实体类
 *                       
 * @Filename: BaseBillingFactor.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2016年7月25日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "money_billing_factor")
public class BaseBillingFactor implements java.io.Serializable{

	/**
	 *Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -5988335802007448861L;
	
	@Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name="id", length=36)
    private String id;
    
    @Column(name="create_time")
    private Date createTime; 		//创建时间
    
    @Column(name="resources_type",length=36)
    private String resourcesType;	//资源类型，nodeId		表示镜像价格时，为固定的"IMAGE"
    
    @Column(name="billing_factor",length=36)
    private String billingFactor;	//计费因子，nodeId		表示镜像价格时，为null
    
    @Column(name="factor_unit",length=36)
    private String factorUnit;		//计费单位，nodeId		表示镜像价格时，为镜像的id
    
    @Column(name="start_num")
    private Long startNum;			//计费区间开始
    
    @Column(name="end_num")
    private Long endNum;			//计费区间结束
    
    @Column(name="pay_type",length=1)
    private String payType;			//付费方式,1：预付费；2：后付费
    
    @Column(name="price")
    private BigDecimal price;			//价格
    
    @Column(name="dc_id",length=36)
    private String dcId;			//数据中心id

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getResourcesType() {
		return resourcesType;
	}

	public void setResourcesType(String resourcesType) {
		this.resourcesType = resourcesType;
	}

	public String getBillingFactor() {
		return billingFactor;
	}

	public void setBillingFactor(String billingFactor) {
		this.billingFactor = billingFactor;
	}

	public String getFactorUnit() {
		return factorUnit;
	}

	public void setFactorUnit(String factorUnit) {
		this.factorUnit = factorUnit;
	}

	public Long getStartNum() {
		return startNum;
	}

	public void setStartNum(Long startNum) {
		this.startNum = startNum;
	}

	public Long getEndNum() {
		return endNum;
	}

	public void setEndNum(Long endNum) {
		this.endNum = endNum;
	}

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getDcId() {
		return dcId;
	}

	public void setDcId(String dcId) {
		this.dcId = dcId;
	}
    
    

}
