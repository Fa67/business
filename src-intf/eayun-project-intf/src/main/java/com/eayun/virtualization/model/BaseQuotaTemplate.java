package com.eayun.virtualization.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

/**
 * 配额模板实体类
 * @Filename: BaseQuotaTemplate.java
 * @Description: 
 * @Version: 1.0
 * @Author: bo.zeng
 * @Email: bo.zeng@eayun.com
 * @History:<br>
 *<li>Date: 2016年8月1日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */

@Entity
@Table(name = "quota_template")
public class BaseQuotaTemplate implements java.io.Serializable{

	private static final long serialVersionUID = 1L;
	
	/**
	 *Comment for <code>qtId</code>
	 *主键UUID
	 */
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "qt_id", unique = true, nullable = false, length = 32)
	private String qtId;
	/**
	 *Comment for <code>qtName</code>
	 *模板名称
	 */
	@Column(name = "qt_name", length = 64, nullable=false)
	private String qtName;
	/**
	 *Comment for <code>cpuCount</code>
	 *CPU数量
	 */
	@Column(name = "cpu_count")
	private int cpuCount;
	/**
	 *Comment for <code>memory</code>
	 *内存数量
	 */
	@Column(name = "memory")
	private int memory;
	/**
	 *Comment for <code>hostCount</code>
	 *云主机数量
	 */
	@Column(name = "host_count")
	private int hostCount;
	/**
	 *Comment for <code>diskCount</code>
	 *云硬盘数量
	 */
	@Column(name = "disk_count")
	private int diskCount;
	/**
	 *Comment for <code>diskSnapshot</code>
	 *云硬盘备份数量
	 */
	@Column(name = "disk_snapshot")
	private int diskSnapshot;
	/**
	 *Comment for <code>diskSize</code>
	 *云硬盘大小
	 */
	@Column(name = "disk_capacity")
	private int diskCapacity;
	/**
	 *Comment for <code>snapshotSize</code>
	 *云备份大小
	 */
	@Column(name = "snapshot_size")
	private int snapshotSize;
	/**
	 *Comment for <code>countVPN</code>
	 *VPN数量
	 */
	@Column(name = "count_vpn")
	private int countVpn;
	/**
	 *Comment for <code>portmappingCount</code>
	 *端口映射数量
	 */
	@Column(name = "portmapping_count")
	private int portMappingCount;
	/**
	 *Comment for <code>imageCount</code>
	 *镜像数量
	 */
	@Column(name = "image_count")
	private int imageCount;
	/**
	 *Comment for <code>countBand</code>
	 *带宽大小
	 */
	@Column(name = "count_band")
	private int countBand;
	/**
	 *Comment for <code>netWork</code>
	 *网络数量
	 */
	@Column(name = "net_work")
	private int netWork;
	/**
	 *Comment for <code>subnetCount</code>
	 *子网数量
	 */
	@Column(name = "subnet_count")
	private int subnetCount;
	/**
	 *Comment for <code>outerip</code>
	 *公网IP数量
	 */
	@Column(name = "outerip")
	private int outerIP;
	/**
	 *Comment for <code>safeGroup</code>
	 *安全组数量
	 */
	@Column(name = "safe_group")
	private int safeGroup;
	/**
	 *Comment for <code>quotaPool</code>
	 *负载均衡数量
	 */
	@Column(name = "quota_pool")
	private int quotaPool;
	/**
	 *Comment for <code>smsCount</code>
	 *报警短信数量
	 */
	@Column(name = "sms_count")
	private int smsCount;
	/**
	 *Comment for <code>qtDesc</code>
	 *描述
	 */
	@Column(name = "qt_desc", length = 200)
	private String qtDesc;
	/**
	 *Comment for <code>createTime</code>
	 *创建时间
	 */
	@Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time", length = 7, updatable=false)
    private Date createTime;
	/** @author zhouhaitao @since RDS V1.0 begin **/
	/**
	 *Comment for <code>maxMasterInstance</code>
	 *最大主数据库实例的配额
	 */
	@Column(name = "max_masterinstance")
	private int maxMasterInstance;	
	/**
	 *Comment for <code>maxSlaveIOfCluster</code>
	 *一个主实例允许创建从实例配额
	 */
	@Column(name = "max_slaveofcluster")								
	private int maxSlaveIOfCluster;										
	/**
	 *Comment for <code>maxBackupByHand</code>
	 *手动备份的配额
	 */
	@Column(name = "max_backupbyhand")
	private int maxBackupByHand;										
	/**
	 *Comment for <code>maxBackupByAuto</code>
	 *自动备份的配额
	 */
	@Column(name = "max_backupbyauto")
	private int maxBackupByAuto;										
	/** @author zhouhaitao @since RDS V1.0 end **/
	
	/** @author zengbo end**/
	@Column(name = "ssh_key_count")
	private int sshKeyCount;
	
	public String getQtId() {
		return qtId;
	}
	public void setQtId(String qtId) {
		this.qtId = qtId;
	}
	public String getQtName() {
		return qtName;
	}
	public void setQtName(String qtName) {
		this.qtName = qtName;
	}
	public int getCpuCount() {
		return cpuCount;
	}
	public void setCpuCount(int cpuCount) {
		this.cpuCount = cpuCount;
	}
	public int getMemory() {
		return memory;
	}
	public void setMemory(int memory) {
		this.memory = memory;
	}
	public int getHostCount() {
		return hostCount;
	}
	public void setHostCount(int hostCount) {
		this.hostCount = hostCount;
	}
	public int getDiskCount() {
		return diskCount;
	}
	public void setDiskCount(int diskCount) {
		this.diskCount = diskCount;
	}
	public int getDiskSnapshot() {
		return diskSnapshot;
	}
	public void setDiskSnapshot(int diskSnapshot) {
		this.diskSnapshot = diskSnapshot;
	}
	public int getCountBand() {
		return countBand;
	}
	public void setCountBand(int countBand) {
		this.countBand = countBand;
	}
	public int getNetWork() {
		return netWork;
	}
	public void setNetWork(int netWork) {
		this.netWork = netWork;
	}
	public int getSubnetCount() {
		return subnetCount;
	}
	public void setSubnetCount(int subnetCount) {
		this.subnetCount = subnetCount;
	}
	public int getOuterIP() {
		return outerIP;
	}
	public void setOuterIP(int outerIP) {
		this.outerIP = outerIP;
	}
	public int getSafeGroup() {
		return safeGroup;
	}
	public void setSafeGroup(int safeGroup) {
		this.safeGroup = safeGroup;
	}
	public int getQuotaPool() {
		return quotaPool;
	}
	public void setQuotaPool(int quotaPool) {
		this.quotaPool = quotaPool;
	}
	public int getSmsCount() {
		return smsCount;
	}
	public void setSmsCount(int smsCount) {
		this.smsCount = smsCount;
	}
	public String getQtDesc() {
		return qtDesc;
	}
	public void setQtDesc(String qtDesc) {
		this.qtDesc = qtDesc;
	}
	public int getDiskCapacity() {
		return diskCapacity;
	}
	public void setDiskCapacity(int diskCapacity) {
		this.diskCapacity = diskCapacity;
	}
	public int getSnapshotSize() {
		return snapshotSize;
	}
	public void setSnapshotSize(int snapshotSize) {
		this.snapshotSize = snapshotSize;
	}
	public int getCountVpn() {
		return countVpn;
	}
	public void setCountVpn(int countVpn) {
		this.countVpn = countVpn;
	}
	public int getPortMappingCount() {
		return portMappingCount;
	}
	public void setPortMappingCount(int portMappingCount) {
		this.portMappingCount = portMappingCount;
	}
	public int getImageCount() {
		return imageCount;
	}
	public void setImageCount(int imageCount) {
		this.imageCount = imageCount;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public int getMaxMasterInstance() {
		return maxMasterInstance;
	}
	public void setMaxMasterInstance(int maxMasterInstance) {
		this.maxMasterInstance = maxMasterInstance;
	}
	public int getMaxSlaveIOfCluster() {
		return maxSlaveIOfCluster;
	}
	public void setMaxSlaveIOfCluster(int maxSlaveIOfCluster) {
		this.maxSlaveIOfCluster = maxSlaveIOfCluster;
	}
	public int getMaxBackupByHand() {
		return maxBackupByHand;
	}
	public void setMaxBackupByHand(int maxBackupByHand) {
		this.maxBackupByHand = maxBackupByHand;
	}
	public int getMaxBackupByAuto() {
		return maxBackupByAuto;
	}
	public void setMaxBackupByAuto(int maxBackupByAuto) {
		this.maxBackupByAuto = maxBackupByAuto;
	}
	public int getSshKeyCount() {
		return sshKeyCount;
	}
	public void setSshKeyCount(int sshKeyCount) {
		this.sshKeyCount = sshKeyCount;
	}

}
