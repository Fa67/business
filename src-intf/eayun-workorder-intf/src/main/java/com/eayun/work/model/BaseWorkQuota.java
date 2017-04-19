package com.eayun.work.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
/**
 * 
 *                       
 * @Filename: BaseWorkQuota.java
 * @Description: 配额类工单的配额
 * @Version: 1.0
 * @Author: 陈鹏飞
 * @Email: pengfei.chen@eayun.com
 * @History:<br>
 *<li>Date: 2015年11月23日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "work_quota")
public class BaseWorkQuota implements Serializable{
	
	private static final long serialVersionUID = 2673047480840924946L;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "quota_id", unique = true, nullable = false, length = 32)
	private String quotaId;
	@Column(name = "quota_vm", length = 10)
	private int quotaVm;//云主机配额
	
	@Column(name = "quota_cpu", length = 10)
	private int quotaCpu;			//CPU配额
	
	@Column(name = "quota_memory", length = 10)
	private int quotaMemory;		//内存配额
	
	@Column(name = "quota_disk", length = 10)
	private int quotaDisk;			//云硬盘配额（个）
	
	@Column(name = "quota_Snapshot", length = 10)
	private int quotaSnapshot;		//云备份配额（个）
	
	@Column(name = "quota_diskshot", length = 10)
	private int quotaDiskshot;		//云硬盘和快照配额	(此选项已废弃不用)
	
	@Column(name = "quota_band", length = 10)
	private int quotaBand;			//带宽配额
	
	@Column(name = "quota_vpn", length = 10)
	private int quotaVpn;			//VPN配额
	
	@Column(name = "quota_net", length = 10)
	private int quotaNet;			//网络配额
	
	@Column(name = "quota_subnet", length = 10)
	private int quotaSubnet;		//子网配额
	
	@Column(name = "quota_firewall", length = 10)
	private int quotaFirewall;		//防火墙配额
	
	@Column(name = "quota_route", length = 10)
	private int quotaRoute;			//路由配额
	
	@Column(name = "quota_secGroup", length = 10)
	private int quotaSecGroup;		//安全组配额
	
	@Column(name = "quota_floatIp", length = 10)
	private int quotaFloatIp;		//浮动IP配额
	
	@Column(name = "quota_balance", length = 10)
	private int quotaBalance;		//负载均衡配额
		
	@Column(name = "quota_sms_num", length = 10)
	private int quotaSms;			//负载均衡配额
	
	@Column(name = "prj_id", length = 32)
	private String prjId;			//所属项目
	
	@Column(name = "work_id", length = 32)
    private String workId;			//所属工单
	
	/**************** 2016.08.18 段彬彬*************/
	@Column(name = "quota_portmapping", length = 10)
    private int quotaPortMapping;			//端口映射
	
	@Column(name = "quota_disksize", length = 10)
	private int quotaDiskSize;		//云硬盘容量配额（GB）
	
	@Column(name = "quota_shotsize", length = 10)
	private int quotaShotSize;		//云硬盘备份容量配额（GB）
	/**************** 2016.08.18 段彬彬*************/
	
	@Column(name = "quota_masterinstance", length = 10)
	private int quotaMasterInstance;									// RDS 主实例的配额
	
	@Column(name = "quota_slaveinstance", length = 10)
	private int quotaSlaveInstance;										// RDS 主实例允许创建从实例的配额
	
	@Column(name = "quota_backupbyhand", length = 10)
	private int quotaBackupByHand;										//RDS 手动备份的配额
	
	@Column(name = "quota_backupbyauto", length = 10)
	private int quotaBackupByAuto;										//RDS 自动备份的配额
	
	
	
    public String getQuotaId() {
        return quotaId;
    }
    public void setQuotaId(String quotaId) {
        this.quotaId = quotaId;
    }
    
    public int getQuotaVm() {
		return quotaVm;
	}
	public void setQuotaVm(int quotaVm) {
		this.quotaVm = quotaVm;
	}
	public int getQuotaCpu() {
		return quotaCpu;
	}
	public void setQuotaCpu(int quotaCpu) {
		this.quotaCpu = quotaCpu;
	}
	public int getQuotaMemory() {
		return quotaMemory;
	}
	public void setQuotaMemory(int quotaMemory) {
		this.quotaMemory = quotaMemory;
	}
	public int getQuotaDisk() {
		return quotaDisk;
	}
	public void setQuotaDisk(int quotaDisk) {
		this.quotaDisk = quotaDisk;
	}
	public int getQuotaSnapshot() {
		return quotaSnapshot;
	}
	public void setQuotaSnapshot(int quotaSnapshot) {
		this.quotaSnapshot = quotaSnapshot;
	}
	public int getQuotaDiskshot() {
		return quotaDiskshot;
	}
	public void setQuotaDiskshot(int quotaDiskshot) {
		this.quotaDiskshot = quotaDiskshot;
	}
	public int getQuotaBand() {
		return quotaBand;
	}
	public void setQuotaBand(int quotaBand) {
		this.quotaBand = quotaBand;
	}
	public int getQuotaVpn() {
		return quotaVpn;
	}
	public void setQuotaVpn(int quotaVpn) {
		this.quotaVpn = quotaVpn;
	}
	public int getQuotaNet() {
		return quotaNet;
	}
	public void setQuotaNet(int quotaNet) {
		this.quotaNet = quotaNet;
	}
	public int getQuotaSubnet() {
		return quotaSubnet;
	}
	public void setQuotaSubnet(int quotaSubnet) {
		this.quotaSubnet = quotaSubnet;
	}
	public int getQuotaFirewall() {
		return quotaFirewall;
	}
	public void setQuotaFirewall(int quotaFirewall) {
		this.quotaFirewall = quotaFirewall;
	}
	public int getQuotaRoute() {
		return quotaRoute;
	}
	public void setQuotaRoute(int quotaRoute) {
		this.quotaRoute = quotaRoute;
	}
	public int getQuotaSecGroup() {
		return quotaSecGroup;
	}
	public void setQuotaSecGroup(int quotaSecGroup) {
		this.quotaSecGroup = quotaSecGroup;
	}
	public int getQuotaFloatIp() {
		return quotaFloatIp;
	}
	public void setQuotaFloatIp(int quotaFloatIp) {
		this.quotaFloatIp = quotaFloatIp;
	}
	public int getQuotaBalance() {
		return quotaBalance;
	}
	public void setQuotaBalance(int quotaBalance) {
		this.quotaBalance = quotaBalance;
	}
	public String getPrjId() {
        return prjId;
    }
    public void setPrjId(String prjId) {
        this.prjId = prjId;
    }
    public String getWorkId() {
        return workId;
    }
    public void setWorkId(String workId) {
        this.workId = workId;
    }
	public int getQuotaSms() {
		return quotaSms;
	}
	public void setQuotaSms(int quotaSms) {
		this.quotaSms = quotaSms;
	}
	public int getQuotaDiskSize() {
		return quotaDiskSize;
	}
	public void setQuotaDiskSize(int quotaDiskSize) {
		this.quotaDiskSize = quotaDiskSize;
	}
	public int getQuotaShotSize() {
		return quotaShotSize;
	}
	public void setQuotaShotSize(int quotaShotSize) {
		this.quotaShotSize = quotaShotSize;
	}
	public int getQuotaPortMapping() {
		return quotaPortMapping;
	}
	public void setQuotaPortMapping(int quotaPortMapping) {
		this.quotaPortMapping = quotaPortMapping;
	}
	public int getQuotaMasterInstance() {
		return quotaMasterInstance;
	}
	public void setQuotaMasterInstance(int quotaMasterInstance) {
		this.quotaMasterInstance = quotaMasterInstance;
	}
	public int getQuotaSlaveInstance() {
		return quotaSlaveInstance;
	}
	public void setQuotaSlaveInstance(int quotaSlaveInstance) {
		this.quotaSlaveInstance = quotaSlaveInstance;
	}
	public int getQuotaBackupByHand() {
		return quotaBackupByHand;
	}
	public void setQuotaBackupByHand(int quotaBackupByHand) {
		this.quotaBackupByHand = quotaBackupByHand;
	}
	public int getQuotaBackupByAuto() {
		return quotaBackupByAuto;
	}
	public void setQuotaBackupByAuto(int quotaBackupByAuto) {
		this.quotaBackupByAuto = quotaBackupByAuto;
	}
	
}
