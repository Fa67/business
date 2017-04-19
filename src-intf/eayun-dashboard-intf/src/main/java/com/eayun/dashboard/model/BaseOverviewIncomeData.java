package com.eayun.dashboard.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * 总览收入统计数据映射表
 * @author bo.zeng@eayun.com
 *
 */
@Entity
@Table(name = "overview_income_data")
public class BaseOverviewIncomeData implements java.io.Serializable  {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 主键
	 */
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "id", unique = true, nullable = false, length = 32)
	private String id;
	
	/**
	 * 总收入
	 */
	@Column(name = "total_income")
	private BigDecimal totalIncome = new BigDecimal(0.000);
	
	/**
	 * 总订单量
	 */
	@Column(name = "total_order")
	private int totalOrder;
	
	/**
	 * 支付宝充值金额
	 */
	@Column(name = "alipay_recharge")
	private BigDecimal alipayRecharge = new BigDecimal(0.000);
	
	/**
	 * 支付宝付款金额
	 */
	@Column(name = "alipay_buy")
	private BigDecimal alipayBuy = new BigDecimal(0.000);
	
	/**
	 * ecmc实际充值
	 */
	@Column(name = "ecmc_recharge")
	private BigDecimal ecmcRecharge = new BigDecimal(0.000);
	
	/**
	 * 云主机类订单量
	 */
	@Column(name = "vm_order")
	private int vmOrder;
	
	/**
	 * 数据盘类订单量
	 */
	@Column(name = "vdisk_order")
	private int vdiskOrder;
	
	/**
	 * 云硬盘备份类订单量
	 */
	@Column(name = "disksnapshot_order")
	private int disksnapshotOrder;
	
	/**
	 * 网络类订单量
	 */
	@Column(name = "network_order")
	private int networkOrder;
	
	/**
	 * 负载均衡类订单量
	 */
	@Column(name = "quotapool_order")
	private int quotapoolOrder;
	
	/**
	 * 弹性公网IP类订单量
	 */
	@Column(name = "floatip_order")
	private int floatipOrder;
	
	/**
	 * VPN类订单量
	 */
	@Column(name = "vpn_order")
	private int vpnOrder;
	
	/**
	 * 云数据库类订单量
	 */
	@Column(name = "rds_order")
	private int rdsOrder;
	
	/**
	 * 异常订单量
	 */
	@Column(name = "exceptional_order")
	private int exceptionalOrder;
	
	/**
	 * 记录时间
	 */
	@Column(name = "data_time")
	private String dataTime;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public BigDecimal getTotalIncome() {
		return totalIncome;
	}

	public void setTotalIncome(BigDecimal totalIncome) {
		this.totalIncome = totalIncome;
	}

	public int getTotalOrder() {
		return totalOrder;
	}

	public void setTotalOrder(int totalOrder) {
		this.totalOrder = totalOrder;
	}

	public BigDecimal getAlipayRecharge() {
		return alipayRecharge;
	}

	public void setAlipayRecharge(BigDecimal alipayRecharge) {
		this.alipayRecharge = alipayRecharge;
	}

	public BigDecimal getAlipayBuy() {
		return alipayBuy;
	}

	public void setAlipayBuy(BigDecimal alipayBuy) {
		this.alipayBuy = alipayBuy;
	}

	public BigDecimal getEcmcRecharge() {
		return ecmcRecharge;
	}

	public void setEcmcRecharge(BigDecimal ecmcRecharge) {
		this.ecmcRecharge = ecmcRecharge;
	}

	public int getVmOrder() {
		return vmOrder;
	}

	public void setVmOrder(int vmOrder) {
		this.vmOrder = vmOrder;
	}

	public int getVdiskOrder() {
		return vdiskOrder;
	}

	public void setVdiskOrder(int vdiskOrder) {
		this.vdiskOrder = vdiskOrder;
	}

	public int getDisksnapshotOrder() {
		return disksnapshotOrder;
	}

	public void setDisksnapshotOrder(int disksnapshotOrder) {
		this.disksnapshotOrder = disksnapshotOrder;
	}

	public int getNetworkOrder() {
		return networkOrder;
	}

	public void setNetworkOrder(int networkOrder) {
		this.networkOrder = networkOrder;
	}

	public int getQuotapoolOrder() {
		return quotapoolOrder;
	}

	public void setQuotapoolOrder(int quotapoolOrder) {
		this.quotapoolOrder = quotapoolOrder;
	}

	public int getFloatipOrder() {
		return floatipOrder;
	}

	public void setFloatipOrder(int floatipOrder) {
		this.floatipOrder = floatipOrder;
	}

	public int getVpnOrder() {
		return vpnOrder;
	}

	public void setVpnOrder(int vpnOrder) {
		this.vpnOrder = vpnOrder;
	}

	public int getRdsOrder() {
		return rdsOrder;
	}

	public void setRdsOrder(int rdsOrder) {
		this.rdsOrder = rdsOrder;
	}

	public int getExceptionalOrder() {
		return exceptionalOrder;
	}

	public void setExceptionalOrder(int exceptionalOrder) {
		this.exceptionalOrder = exceptionalOrder;
	}

	public String getDataTime() {
		return dataTime;
	}

	public void setDataTime(String dataTime) {
		this.dataTime = dataTime;
	}



}
