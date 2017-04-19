package com.eayun.price.bean;

import java.math.BigDecimal;

/**
 * 一次业务价格计算接口参数bean
 * 
 * 前四个公共参数为必填项，业务没有就赋默认值1
 * 
 * 其余与本次调用业务相关的计费单位赋值，与本次调用业务无关的计费单位禁止赋值
 * 
 * @Filename: ParamBean.java
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
public class ParamBean {

	private String dcId;		//数据中心id		必填
	
	private String payType;		//付费类型			必填
	
	private Integer number;		//批量个数			必填，默认1
	
	private Integer cycleCount;	//预付费月数，或后付费时小时数		必填，默认1
	
	private Integer cpuSize;	//CPU核数
	
	private Integer ramCapacity;		//内存大小（GB）
	
	/**需要删除（但暂时保留）*/
	private Integer dataDiskCapacity;	//数据盘容量（GB）
	
	/**需要删除（但暂时保留）*/
	private Integer sysDiskCapacity;	//系统盘容量（GB）
	
	private Integer snapshotSize;		//备份大小（GB）
	
	private String imageId;				//镜像id
	
	private Integer cusImageSize;		//自定义镜像大小
	
	private Integer bandValue;			//私有网络带宽值
	
	private Long connCount;				//负载均衡连接数
	
	private Integer vpnCount;			//VPN个数，（资源本身个数计数，值赋为1，批量个数根据情况赋值）
	
	private Integer ipCount;			//弹性公网IP个数，（资源本身个数计数，值赋为1，批量个数根据情况赋值）
	
	private Double spaceCapacity;		//存储空间（GB/h）
	
	private Double[] downValue;			//下行流量（GB）
	
	private Long[] requestCount;		//请求次数（次）

    private Long cdnDownloadFlow;   //CDN下载流量（B）

    private Long dreqsCount;      //CDN动态请求数（次）

    private Long hreqsCount;      //CDN-Https请求数（次）
    
    
    private Integer cloudMySQLCPU;			//mysql实例CPU核数
    
    private Integer cloudMySQLRAM;			//mysql实例内存大小
    
    private Integer storageMySQLOrdinary;	//实例存储_普通型（10GB）
    
    private Integer storageMySQLBetter;		//实例存储_性能型（10GB）
    
    private Integer storageMySQLBest;		//实例存储_超高性能型（10GB）
    
    private Integer sysDiskOrdinary;		//系统盘_普通型（10GB）
    
    private Integer sysDiskBetter;			//系统盘_性能型（10GB）
    
    private Integer sysDiskBest;			//系统盘_超高性能型（10GB）
    
    private Integer dataDiskOrdinary;		//数据盘_普通型（10GB）
    
    private Integer dataDiskBetter;			//数据盘_性能型（10GB）
    
    private Integer dataDiskBest;			//数据盘_超高性能型（10GB）
    
    
    

    public Long getCdnDownloadFlow() {
        return cdnDownloadFlow;
    }

    public void setCdnDownloadFlow(Long cdnDownloadFlow) {
        this.cdnDownloadFlow = cdnDownloadFlow;
    }

    public Long getDreqsCount() {
        return dreqsCount;
    }

    public void setDreqsCount(Long dreqsCount) {
        this.dreqsCount = dreqsCount;
    }

    public Long getHreqsCount() {
        return hreqsCount;
    }

    public void setHreqsCount(Long hreqsCount) {
        this.hreqsCount = hreqsCount;
    }

    public String getDcId() {
		return dcId;
	}

	public void setDcId(String dcId) {
		this.dcId = dcId;
	}

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public Integer getCycleCount() {
		return cycleCount;
	}

	public void setCycleCount(Integer cycleCount) {
		this.cycleCount = cycleCount;
	}

	public Integer getCpuSize() {
		return cpuSize;
	}

	public void setCpuSize(Integer cpuSize) {
		this.cpuSize = cpuSize;
	}

	public Integer getRamCapacity() {
		return ramCapacity;
	}

	public void setRamCapacity(Integer ramCapacity) {
		this.ramCapacity = ramCapacity;
	}

	public Integer getDataDiskCapacity() {
		return dataDiskCapacity;
	}

	public void setDataDiskCapacity(Integer dataDiskCapacity) {
		this.dataDiskCapacity = dataDiskCapacity;
	}

	public Integer getSysDiskCapacity() {
		return sysDiskCapacity;
	}

	public void setSysDiskCapacity(Integer sysDiskCapacity) {
		this.sysDiskCapacity = sysDiskCapacity;
	}

	public Integer getSnapshotSize() {
		return snapshotSize;
	}

	public void setSnapshotSize(Integer snapshotSize) {
		this.snapshotSize = snapshotSize;
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	public Integer getCusImageSize() {
		return cusImageSize;
	}

	public void setCusImageSize(Integer cusImageSize) {
		this.cusImageSize = cusImageSize;
	}

	public Integer getBandValue() {
		return bandValue;
	}

	public void setBandValue(Integer bandValue) {
		this.bandValue = bandValue;
	}

	public Long getConnCount() {
		return connCount;
	}

	public void setConnCount(Long connCount) {
		this.connCount = connCount;
	}

	public Integer getVpnCount() {
		return vpnCount;
	}

	public void setVpnCount(Integer vpnCount) {
		this.vpnCount = vpnCount;
	}

	public Integer getIpCount() {
		return ipCount;
	}

	public void setIpCount(Integer ipCount) {
		this.ipCount = ipCount;
	}

	public Double getSpaceCapacity() {
		return spaceCapacity;
	}

	public void setSpaceCapacity(Double spaceCapacity) {
		this.spaceCapacity = spaceCapacity;
	}

	public Double[] getDownValue() {
		return downValue;
	}

	public void setDownValue(Double[] downValue) {
		this.downValue = downValue;
	}

	public Long[] getRequestCount() {
		return requestCount;
	}

	public void setRequestCount(Long[] requestCount) {
		this.requestCount = requestCount;
	}

	public Integer getCloudMySQLCPU() {
		return cloudMySQLCPU;
	}

	public void setCloudMySQLCPU(Integer cloudMySQLCPU) {
		this.cloudMySQLCPU = cloudMySQLCPU;
	}

	public Integer getCloudMySQLRAM() {
		return cloudMySQLRAM;
	}

	public void setCloudMySQLRAM(Integer cloudMySQLRAM) {
		this.cloudMySQLRAM = cloudMySQLRAM;
	}

	public Integer getStorageMySQLOrdinary() {
		return storageMySQLOrdinary;
	}

	public void setStorageMySQLOrdinary(Integer storageMySQLOrdinary) {
		this.storageMySQLOrdinary = storageMySQLOrdinary;
	}

	public Integer getStorageMySQLBetter() {
		return storageMySQLBetter;
	}

	public void setStorageMySQLBetter(Integer storageMySQLBetter) {
		this.storageMySQLBetter = storageMySQLBetter;
	}

	public Integer getStorageMySQLBest() {
		return storageMySQLBest;
	}

	public void setStorageMySQLBest(Integer storageMySQLBest) {
		this.storageMySQLBest = storageMySQLBest;
	}

	public Integer getSysDiskOrdinary() {
		return sysDiskOrdinary;
	}

	public void setSysDiskOrdinary(Integer sysDiskOrdinary) {
		this.sysDiskOrdinary = sysDiskOrdinary;
	}

	public Integer getSysDiskBetter() {
		return sysDiskBetter;
	}

	public void setSysDiskBetter(Integer sysDiskBetter) {
		this.sysDiskBetter = sysDiskBetter;
	}

	public Integer getSysDiskBest() {
		return sysDiskBest;
	}

	public void setSysDiskBest(Integer sysDiskBest) {
		this.sysDiskBest = sysDiskBest;
	}

	public Integer getDataDiskOrdinary() {
		return dataDiskOrdinary;
	}

	public void setDataDiskOrdinary(Integer dataDiskOrdinary) {
		this.dataDiskOrdinary = dataDiskOrdinary;
	}

	public Integer getDataDiskBetter() {
		return dataDiskBetter;
	}

	public void setDataDiskBetter(Integer dataDiskBetter) {
		this.dataDiskBetter = dataDiskBetter;
	}

	public Integer getDataDiskBest() {
		return dataDiskBest;
	}

	public void setDataDiskBest(Integer dataDiskBest) {
		this.dataDiskBest = dataDiskBest;
	}
	
	
	
	
}
