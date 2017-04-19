package com.eayun.virtualization.model;

import com.eayun.common.util.BeanUtils;

public class CloudProject extends BaseCloudProject {

	private static final long serialVersionUID = 4226267038677335379L;
	
	private String dcName;//数据中心名称
	private int usedDataCapacity;//已使用数据盘大小（云硬盘+云硬盘备份）
	private int usedVmPrecent;//云主机使用百分比
	private int usedCpuPrecent;//CPU核数使用百分比
	private int usedRamPrecent;//内存使用百分比
	private int usedDataDiskPrecent;//数据盘使用百分比
	
	private int usedVmCount;	//项目下已创建的云主机数据量;
    private int usedCpuCount;	//已使用CPU总核数
    private int usedRam;		//已使用的内存总量
    private int usedDiskCapacity;		//已创建的云硬盘容量(GB)
    private int usedSnapshotCapacity;	//已创建的云硬盘备份的容量(GB)
	
    private int               diskCountUse;                            //云硬盘数量使用量（个）
    private int               diskSnapshotUse;                         //云硬盘备份数量使用量(个)
    
    private int               netWorkUse;                              //网络数量使用量
    private int               subnetCountUse;                          //子网数量使用量
    private int               outerIPUse;                              //浮动ip数量使用量
    private int               routeCountUse;                           //路由使用量
    private int               safeGroupUse;                            //安全组使用量
    private int               countBandUse;                            //带宽使用量
    private int               countVpnUse;                             //VPN使用量
    private String            metadataEntriesUse;                      //元数据条目使用量
    private String            fileCountUse;                            //注入文件使用量
    private String            fileBytesUse;                            //注入文件内容字节数使用量
    private String            safeGroupRuleUse;                        //安全组规则使用量
    private String            portCountUse;                            //端口使用量
    private int            portMappingUse;                       		//端口映射使用量
    private int            	  usedPool;                            		//资源池
    private int            	  smsQuota;                            		//资源池
    private String  customerName;										//所属客户的客户名称
    private String cusOrg;												//所属客户
    private String createTime ;											//项目创建时间
    private float usedMemory;											//已使用的内存
    private int alarmCount;  											//报警数量（zengbo）
    private int usedSmsCount;  											//短信数量（zengbo）
    /*** @author LiuZhuangzhuang**/
    private int               imageCountUse;                            //自定义镜像使用量 
    /*** @author LiuZhuangzhuang end**/
    private int masterInstanceUse;										//RDS主实例使用量
    private int totalInstanceUse;										//MySQL实例总使用量(主+从)
    /** @author zengbo **/
    private int sshKeyUsedCount;    //ssh密钥使用量
    
    public int getUsedSmsCount() {
		return usedSmsCount;
	}

	public void setUsedSmsCount(int usedSmsCount) {
		this.usedSmsCount = usedSmsCount;
	}

	public CloudProject(){super();}
    
    public CloudProject(BaseCloudProject base, String dcName){
    	super();
    	BeanUtils.copyPropertiesByModel(this, base);
    	this.dcName = dcName;
    }
    
	public String getCusOrg() {
		return cusOrg;
	}
	public void setCusOrg(String cusOrg) {
		this.cusOrg = cusOrg;
	}
	public int getAlarmCount() {
		return alarmCount;
	}
	public void setAlarmCount(int alarmCount) {
		this.alarmCount = alarmCount;
	}
	public String getDcName() {
		return dcName;
	}
	public void setDcName(String dcName) {
		this.dcName = dcName;
	}
	public int getUsedVmCount() {
		return usedVmCount;
	}
	public void setUsedVmCount(int usedVmCount) {
		this.usedVmCount = usedVmCount;
	}
	public int getUsedCpuCount() {
		return usedCpuCount;
	}
	public void setUsedCpuCount(int usedCpuCount) {
		this.usedCpuCount = usedCpuCount;
	}
	public int getUsedRam() {
		return usedRam;
	}
	public void setUsedRam(int usedRam) {
		this.usedRam = usedRam;
	}
	public int getUsedDiskCapacity() {
		return usedDiskCapacity;
	}
	public void setUsedDiskCapacity(int usedDiskCapacity) {
		this.usedDiskCapacity = usedDiskCapacity;
	}
	public int getUsedSnapshotCapacity() {
		return usedSnapshotCapacity;
	}
	public void setUsedSnapshotCapacity(int usedSnapshotCapacity) {
		this.usedSnapshotCapacity = usedSnapshotCapacity;
	}
	public int getUsedDataCapacity() {
		return usedDataCapacity;
	}
	public void setUsedDataCapacity(int usedDataCapacity) {
		this.usedDataCapacity = usedDataCapacity;
	}
	public int getUsedVmPrecent() {
		return usedVmPrecent;
	}
	public void setUsedVmPrecent(int usedVmPrecent) {
		this.usedVmPrecent = usedVmPrecent;
	}
	public int getUsedCpuPrecent() {
		return usedCpuPrecent;
	}
	public void setUsedCpuPrecent(int usedCpuPrecent) {
		this.usedCpuPrecent = usedCpuPrecent;
	}
	public int getUsedRamPrecent() {
		return usedRamPrecent;
	}
	public void setUsedRamPrecent(int usedRamPrecent) {
		this.usedRamPrecent = usedRamPrecent;
	}
	public int getUsedDataDiskPrecent() {
		return usedDataDiskPrecent;
	}
	public void setUsedDataDiskPrecent(int usedDataDiskPrecent) {
		this.usedDataDiskPrecent = usedDataDiskPrecent;
	}
    public int getDiskCountUse() {
        return diskCountUse;
    }
    public void setDiskCountUse(int diskCountUse) {
        this.diskCountUse = diskCountUse;
    }
    public int getDiskSnapshotUse() {
        return diskSnapshotUse;
    }
    public void setDiskSnapshotUse(int diskSnapshotUse) {
        this.diskSnapshotUse = diskSnapshotUse;
    }
    public int getNetWorkUse() {
        return netWorkUse;
    }
    public void setNetWorkUse(int netWorkUse) {
        this.netWorkUse = netWorkUse;
    }
    public int getSubnetCountUse() {
        return subnetCountUse;
    }
    public void setSubnetCountUse(int subnetCountUse) {
        this.subnetCountUse = subnetCountUse;
    }
    public int getOuterIPUse() {
        return outerIPUse;
    }
    public void setOuterIPUse(int outerIPUse) {
        this.outerIPUse = outerIPUse;
    }
    public String getMetadataEntriesUse() {
        return metadataEntriesUse;
    }
    public void setMetadataEntriesUse(String metadataEntriesUse) {
        this.metadataEntriesUse = metadataEntriesUse;
    }
    public String getFileCountUse() {
        return fileCountUse;
    }
    public void setFileCountUse(String fileCountUse) {
        this.fileCountUse = fileCountUse;
    }
    public String getFileBytesUse() {
        return fileBytesUse;
    }
    public void setFileBytesUse(String fileBytesUse) {
        this.fileBytesUse = fileBytesUse;
    }
    
    public int getSafeGroupUse() {
        return safeGroupUse;
    }
    public void setSafeGroupUse(int safeGroupUse) {
        this.safeGroupUse = safeGroupUse;
    }
    public String getSafeGroupRuleUse() {
        return safeGroupRuleUse;
    }
    public void setSafeGroupRuleUse(String safeGroupRuleUse) {
        this.safeGroupRuleUse = safeGroupRuleUse;
    }
    
    public int getRouteCountUse() {
        return routeCountUse;
    }
    public void setRouteCountUse(int routeCountUse) {
        this.routeCountUse = routeCountUse;
    }
    public String getPortCountUse() {
        return portCountUse;
    }
    public void setPortCountUse(String portCountUse) {
        this.portCountUse = portCountUse;
    }
    public int getCountBandUse() {
        return countBandUse;
    }
    public void setCountBandUse(int countBandUse) {
        this.countBandUse = countBandUse;
    }
    public int getCountVpnUse() {
        return countVpnUse;
    }
    public void setCountVpnUse(int countVpnUse) {
        this.countVpnUse = countVpnUse;
    }
	public int getUsedPool() {
		return usedPool;
	}
	public void setUsedPool(int usedPool) {
		this.usedPool = usedPool;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public float getUsedMemory() {
		return usedMemory;
	}
	public void setUsedMemory(float usedMemory) {
		this.usedMemory = usedMemory;
	}
	public int getSmsQuota() {
		return smsQuota;
	}
	public void setSmsQuota(int smsQuota) {
		this.smsQuota = smsQuota;
	}

	public int getPortMappingUse() {
		return portMappingUse;
	}

	public void setPortMappingUse(int portMappingUse) {
		this.portMappingUse = portMappingUse;
	}

	public int getImageCountUse() {
		return imageCountUse;
	}

	public void setImageCountUse(int imageCountUse) {
		this.imageCountUse = imageCountUse;
	}

	public int getMasterInstanceUse() {
		return masterInstanceUse;
	}

	public void setMasterInstanceUse(int masterInstanceUse) {
		this.masterInstanceUse = masterInstanceUse;
	}

	public int getTotalInstanceUse() {
		return totalInstanceUse;
	}

	public void setTotalInstanceUse(int totalInstanceUse) {
		this.totalInstanceUse = totalInstanceUse;
	}

	public int getSshKeyUsedCount() {
		return sshKeyUsedCount;
	}

	public void setSshKeyUsedCount(int sshKeyUsedCount) {
		this.sshKeyUsedCount = sshKeyUsedCount;
	}
	
}

