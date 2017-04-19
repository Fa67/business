package com.eayun.costcenter.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "money_record")
public class BaseMoneyRecord implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name="mon_id", length=32)
    private String monId;					//主键id
	
	@Column(name="serial_number")
	private String serialNumber;			//交易流水号
	
	@Column(name="mon_time")
	private Date monTime;					//交易发生时间
	
	@Column(name="income_type")
	private String incomeType;				//收入类型 1为收入  2为支出
	
	@Column(name="mon_ecscremark")
	private String monEcscRemark;			//ecsc备注
	
	@Column(name="mon_ecmcremark")
	private String monEcmcRemark;			//ecmc备注
	
	@Column(name="mon_money")
	private BigDecimal money;				//交易金额
	
	@Column(name="account_balance")
	private BigDecimal accountBalance;		//账户余额
	
	@Column(name="mon_contract")
	private String monContract;				//合同编号
	
	@Column(name="pay_type")		
	private String payType;					//付款方式 1为预付费  2为后付费
	
	@Column(name="pay_state")
	private String payState;				//支付状态  1为已支付  2为已欠费
	
	@Column(name="product_name")
	private String productName;				//产品名
	
	@Column(name="order_no")
	private String orderNo;					//订单编号
	
	@Column(name="resource_id")
	private String resourceId;				//资源id
	
	@Column(name="cus_id")
	private String cusId;					//客户id
		
	@Column(name="resource_name")
	private String resourceName;			//资源名称
	
	@Column(name="mon_paymonth")
	private String monPaymonth;				//账期
	
	@Column(name="mon_realpay")
	private BigDecimal monRealPay;			//实际支付
	
	@Column(name="mon_configure")
	private String monConfigure;			//后付费资源配置
	
	@Column(name="mon_start")
	private Date monStart;					//费用开始时间
	
	@Column(name="mon_end")
	private Date monEnd;					//费用结束时间
	
	@Column(name="oper_type")
	private String operType;				//操作类型  1.	仅充值   2.	退款       3.	消费(购买,续费,升级)   4.	系统操作

	
	@Column(name="resource_type")
	private String resourceType;			//0、云主机  1、云硬盘  2、云硬盘备份  3、私有网络  4、负载均衡  5、弹性公网IP  6、对象存储  7、	VPN

	@Column(name="dc_id")
	private String dcId;					//数据中心id
	
	@Column(name="is_success")
	private String isSuccess;				//该笔交易是否已完成
	
	
	public String getIsSuccess() {
		return isSuccess;
	}

	public void setIsSuccess(String isSuccess) {
		this.isSuccess = isSuccess;
	}

	public String getDcId() {
		return dcId;
	}

	public void setDcId(String dcId) {
		this.dcId = dcId;
	}

	public String getMonId() {
		return monId;
	}

	public void setMonId(String monId) {
		this.monId = monId;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public Date getMonTime() {
		return monTime;
	}

	public void setMonTime(Date monTime) {
		this.monTime = monTime;
	}

	public String getIncomeType() {
		return incomeType;
	}

	public void setIncomeType(String incomeType) {
		this.incomeType = incomeType;
	}

	public String getMonEcscRemark() {
		return monEcscRemark;
	}

	public void setMonEcscRemark(String monEcscRemark) {
		this.monEcscRemark = monEcscRemark;
	}

	public String getMonEcmcRemark() {
		return monEcmcRemark;
	}

	public void setMonEcmcRemark(String monEcmcRemark) {
		this.monEcmcRemark = monEcmcRemark;
	}

	public BigDecimal getAccountBalance() {
		return accountBalance;
	}

	public void setAccountBalance(BigDecimal accountBalance) {
		this.accountBalance = accountBalance;
	}

	public String getMonContract() {
		return monContract;
	}

	public void setMonContract(String monContract) {
		this.monContract = monContract;
	}

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	public String getPayState() {
		return payState;
	}

	public void setPayState(String payState) {
		this.payState = payState;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

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

	public String getMonPaymonth() {
		return monPaymonth;
	}

	public void setMonPaymonth(String monPaymonth) {
		this.monPaymonth = monPaymonth;
	}

	public BigDecimal getMonRealPay() {
		return monRealPay;
	}

	public void setMonRealPay(BigDecimal monRealPay) {
		this.monRealPay = monRealPay;
	}

	public String getMonConfigure() {
		return monConfigure;
	}

	public void setMonConfigure(String monConfigure) {
		this.monConfigure = monConfigure;
	}

	public String getOperType() {
		return operType;
	}

	public void setOperType(String operType) {
		this.operType = operType;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}


	public BigDecimal getMoney() {
		return money;
	}

	public void setMoney(BigDecimal money) {
		this.money = money;
	}

	public String getCusId() {
		return cusId;
	}

	public void setCusId(String cusId) {
		this.cusId = cusId;
	}

	public Date getMonStart() {
		return monStart;
	}

	public void setMonStart(Date monStart) {
		this.monStart = monStart;
	}

	public Date getMonEnd() {
		return monEnd;
	}

	public void setMonEnd(Date monEnd) {
		this.monEnd = monEnd;
	}
	
}
