package com.eayun.virtualization.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.eayun.eayunstack.model.Tenant;

@Entity
@Table(name = "cloud_project")
public class BaseCloudProject implements java.io.Serializable {

    private static final long serialVersionUID = 3066406915559794650L;
    @Id
    @Column(name = "PRJ_ID", unique = true, nullable = false, length = 32)
    private String            projectId;                               //项目ID，openstatck返回值回写
    @Column(name = "PRJ_NAME", length = 100)
    private String            prjName;                                 //项目名称
    @Column(name = "BELONG_ORG", length = 100)
    private String            belongOrg;                               //所属组织
    @Column(name = "PRJ_DESC", length = 1000)
    private String            projectDesc;                             //项目描述
    @Column(name = "DC_ID", length = 32)
    private String            dcId;                                    //数据中心ID
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATE_DATE", length = 7, updatable=false)
    private Date              createDate;                              //创建时间
    @Column(name = "IS_SYNCHRONIZED", length = 10)
    private String            isSynchronized   = "0";                  //是否底层同步  1是
    @Column(name = "HOST_COUNT")
    private int               hostCount=0;                               //每个项目对应的云主机数量
    @Column(name = "CPU_COUNT")
    private int               cpuCount=0;                                //cpu核数
    @Column(name = "MEMORY")
    private float             memory;                                  //内存大小
    @Column(name = "DISK_COUNT")
    private int               diskCount;                               //云硬盘数量
    @Column(name = "DISK_SNAPSHOT")
    private int               diskSnapshot;                            //云硬盘备份数量
    @Column(name = "DISK_CAPACITY")
    private int             diskCapacity;                            //云硬盘大小
    /*** @author LiuZhuangzhuang**/
    @Column(name = "SNAPSHOT_SIZE")
    private int             snapshotSize;                            //云备份大小
    @Column(name = "PORTMAPPING_COUNT")
    private int             portMappingCount;                        //端口映射数量
    @Column(name = "IMAGE_COUNT")
    private int             imageCount;                              //自定义镜像数量
    /*** @author LiuZhuangzhuang end**/
    @Column(name = "NET_WORK")
    private int               netWork;                                 //网络数量
    @Column(name = "SUBNET_COUNT")
    private int               subnetCount;                             //子网数量
    @Column(name = "OUTERIP")
    private int               outerIP;                                 //浮动ip数量
    @Column(name = "CUSTOMER_ID")
    private String            customerId;                              //所属客户
    @Column(name = "METADATA_ENTRIES")
    private String            metadataEntries;                         //元数据条目
    @Column(name = "FILE_COUNT")
    private String            fileCount;                               //注入文件
    @Column(name = "FILE_BYTES")
    private String            fileBytes;                               //注入文件内容字节数
    @Column(name = "SAFE_GROUP")
    private int            safeGroup=0;                               //安全组
    @Column(name = "SAFE_GROUP_RULE")
    private int            safeGroupRule=0;                           //安全组规则
    @Column(name = "ROUTE_COUNT")
    private int            routeCount;                              //路由
    @Column(name = "PORT_COUNT")
    private String            portCount;                               //端口
    @Column(name = "IS_HASWORK" ,length=1)
    private String            isHaswork;                               //是否有违解决的特殊工单 0没有1有
    @Column(name = "COUNT_BAND" ,length=10)
    private int            countBand;                               //带宽
    @Column(name = "COUNT_VPN" ,length=32)
    private int            countVpn=0;                                //vpn
    @Column(name = "QUOTA_POOL" ,length=32)
    private int            quotaPool=0;                                //资源池(负载均衡)
    @Column(name = "LABEL_IN_ID" ,length=100)
    private String         labelInId;										//label_in_id标签
    @Column(name = "LABEL_OUT_ID" ,length=100)
    private String		   labelOutId;										//label_out_id标签
    /*** @author Zengbo**/
    @Column(name = "SMS_COUNT" ,length=10)
    private int smsCount;														//短信配额
	@Column(name = "CABINET_ID" ,length=100)
	private String cabinetId;												//机柜ID
	/*** @author Zengbo end**/
	/** @author zhouhaitao @since RDS V1.0 begin **/
	@Column(name = "max_masterinstance")
	private int maxMasterInstance;										//最大主数据库实例的配额
	@Column(name = "max_slaveofcluster")								
	private int maxSlaveIOfCluster;										//一个主实例允许创建从实例配额
	@Column(name = "max_backupbyhand")
	private int maxBackupByHand;										//手动备份的配额
	@Column(name = "max_backupbyauto")
	private int maxBackupByAuto;										//自动备份的配额
	@Column(name = "auto_backuptime",length=20)
	private String autoBackupTime;										//自动备份的时间点
	/** @author zengbo **/
	@Column(name = "ssh_key_count")
	private int sshKeyCount; //SSH密钥数量
	/** @author zhouhaitao @since RDS V1.0 end **/
	public int getSmsCount() {
		return smsCount;
	}

	public void setSmsCount(int smsCount) {
		this.smsCount = smsCount;
	}

	public String getCabinetId() {
		return cabinetId;
	}

	public void setCabinetId(String cabinetId) {
		this.cabinetId = cabinetId;
	}
	
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getPrjName() {
        return prjName;
    }

    public void setPrjName(String prjName) {
        this.prjName = prjName;
    }

    public String getBelongOrg() {
        return belongOrg;
    }

    public void setBelongOrg(String belongOrg) {
        this.belongOrg = belongOrg;
    }

    public String getProjectDesc() {
        return projectDesc;
    }

    public void setProjectDesc(String projectDesc) {
        this.projectDesc = projectDesc;
    }

    public String getDcId() {
        return dcId;
    }

    public void setDcId(String dcid) {
        this.dcId = dcid;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getIsSynchronized() {
        return isSynchronized;
    }

    public void setIsSynchronized(String isSynchronized) {
        this.isSynchronized = isSynchronized;
    }

    public int getHostCount() {
        return hostCount;
    }

    public void setHostCount(int hostCount) {
        this.hostCount = hostCount;
    }

    public int getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(int cpuCount) {
        this.cpuCount = cpuCount;
    }

    public float getMemory() {
        return memory;
    }

    public void setMemory(float memory) {
        this.memory = memory;
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

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getMetadataEntries() {
        return metadataEntries;
    }

    public void setMetadataEntries(String metadataEntries) {
        this.metadataEntries = metadataEntries;
    }

    public String getFileCount() {
        return fileCount;
    }

    public void setFileCount(String fileCount) {
        this.fileCount = fileCount;
    }

    public String getFileBytes() {
        return fileBytes;
    }

    public void setFileBytes(String fileBytes) {
        this.fileBytes = fileBytes;
    }

    public String getPortCount() {
        return portCount;
    }

    public void setPortCount(String portCount) {
        this.portCount = portCount;
    }
    

    public int getSafeGroup() {
		return safeGroup;
	}

	public void setSafeGroup(int safeGroup) {
		this.safeGroup = safeGroup;
	}

	public int getSafeGroupRule() {
		return safeGroupRule;
	}

	public void setSafeGroupRule(int safeGroupRule) {
		this.safeGroupRule = safeGroupRule;
	}

	public int getRouteCount() {
		return routeCount;
	}

	public void setRouteCount(int routeCount) {
		this.routeCount = routeCount;
	}

	public int getCountBand() {
		return countBand;
	}

	public void setCountBand(int countBand) {
		this.countBand = countBand;
	}

	public int getCountVpn() {
		return countVpn;
	}

	public void setCountVpn(int countVpn) {
		this.countVpn = countVpn;
	}

	public BaseCloudProject(){}
    
    public BaseCloudProject(Tenant project,String dcId){
		this.projectId = project.getId();
		this.prjName = project.getName();
		this.projectDesc = project.getDescription();
		this.dcId = dcId;
	}

    public String getIsHaswork() {
        return isHaswork;
    }

    public void setIsHaswork(String isHaswork) {
        this.isHaswork = isHaswork;
    }

	public int getQuotaPool() {
		return quotaPool;
	}

	public void setQuotaPool(int quotaPool) {
		this.quotaPool = quotaPool;
	}

	public String getLabelInId() {
		return labelInId;
	}

	public void setLabelInId(String labelInId) {
		this.labelInId = labelInId;
	}
	
	public String getLabelOutId() {
		return labelOutId;
	}
	
	public void setLabelOutId(String labelOutId) {
		this.labelOutId = labelOutId;
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

	public String getAutoBackupTime() {
		return autoBackupTime;
	}

	public void setAutoBackupTime(String autoBackupTime) {
		this.autoBackupTime = autoBackupTime;
	}

	public int getSshKeyCount() {
		return sshKeyCount;
	}

	public void setSshKeyCount(int sshKeyCount) {
		this.sshKeyCount = sshKeyCount;
	}
}