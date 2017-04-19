package com.eayun.price.bean;

/***
 * 升级配置时的价格计算接口参数
 * 以下业务参数除负载均衡外，均为升级前后的差值；如CPU1核升4核，cpuSize传入3
 * @Filename: UpgradeBean.java
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
public class UpgradeBean {

	private String dcId;	//数据中心
	
	private Integer cycleCount;	//合同剩余天数
	
	private Integer cpuSize;	//CPU核数
	
	private Integer ramCapacity;	//内存大小
	
	/**需要删除（但暂时保留）*/
	private Integer dataDiskCapacity;	//数据盘容量
	
	private Integer bandValue;	//私有网络带宽值
	
	private Long oldConnCount;	//旧负载均衡连接数
	
	private Long newConnCount;	//新负载均衡连接数
	
	
	private Integer cloudMySQLCPU;			//mysql实例CPU核数
    
    private Integer cloudMySQLRAM;			//mysql实例内存大小
    
    private Integer storageMySQLOrdinary;	//实例存储_普通型（10GB）
    
    private Integer storageMySQLBetter;		//实例存储_性能型（10GB）
    
    private Integer storageMySQLBest;		//实例存储_超高性能型（10GB）
    
    private Integer dataDiskOrdinary;		//数据盘_普通型（10GB）
    
    private Integer dataDiskBetter;			//数据盘_性能型（10GB）
    
    private Integer dataDiskBest;			//数据盘_超高性能型（10GB）
    
    

	public String getDcId() {
		return dcId;
	}

	public void setDcId(String dcId) {
		this.dcId = dcId;
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

	public Integer getBandValue() {
		return bandValue;
	}

	public void setBandValue(Integer bandValue) {
		this.bandValue = bandValue;
	}

	public Long getOldConnCount() {
		return oldConnCount;
	}

	public void setOldConnCount(Long oldConnCount) {
		this.oldConnCount = oldConnCount;
	}

	public Long getNewConnCount() {
		return newConnCount;
	}

	public void setNewConnCount(Long newConnCount) {
		this.newConnCount = newConnCount;
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
