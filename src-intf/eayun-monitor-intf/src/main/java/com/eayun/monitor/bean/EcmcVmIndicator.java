package com.eayun.monitor.bean;

import java.util.Date;
/**
 * 运维云主机（云数据库）资源监控项Model
 *                       
 * @Filename: EcmcVmIndicator.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2016年3月30日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class EcmcVmIndicator {
	
	private String vmId;    //云主机Id
    
    private String vmName;  //云主机名称
    
    private String netName; //网络名称
    
    private String vmIp;    //云主机IP
    
    private String osType;  //操作系统
    
    private String dcName;  //数据中心名称
    
    private String prjName;  //项目名称
    
    private String cusName;  //客户名称
    
    private double cpu;     //CPU利用率
    
    private int cpuDiff;    //CPU利用率变化
    
    private double ram;     //内存占用率
    
    private int ramDiff;    //内存占用率变化
    
    private double netIn;   //网卡入流量(Mb/s)
    
    private int netinDiff;  //网卡入流量(Mb/s)变化
    
    private double netOut;  //网卡出流量(Mb/s)
    
    private int netoutDiff; //网卡出流量(Mb/s)变化
    
    private double diskWrite;   //磁盘入流量(Mb/s)
    
    private int writeDiff;      //磁盘入流量(Mb/s)变化
    
    private double diskRead;    //磁盘出流量(Mb/s)
    
    private int readDiff;       //磁盘出流量(Mb/s)变化
    
    private Date timestamp;    //此条数据的采集时间
    
    private boolean mongodb;    //此条数据是否为从mongo中取得
    
    private boolean mongodbTwo;    //此条数据是否为从mongo中取得
    
    
    private String instanceId;    	//实例Id
    
    private String instanceName;  	//实例名称
    
    private String dataVersionName; //实例版本名称
    
    private double volumeUsed;    	//磁盘使用率
    
    private int volumeUsedDiff;     //磁盘使用率变化
    
    private String isMaster;    	//是否主库
    

	public String getVmId() {
		return vmId;
	}

	public void setVmId(String vmId) {
		this.vmId = vmId;
	}

	public String getVmName() {
		return vmName;
	}

	public void setVmName(String vmName) {
		this.vmName = vmName;
	}

	public String getNetName() {
		return netName;
	}

	public void setNetName(String netName) {
		this.netName = netName;
	}

	public String getVmIp() {
		return vmIp;
	}

	public void setVmIp(String vmIp) {
		this.vmIp = vmIp;
	}

	public String getOsType() {
		return osType;
	}

	public void setOsType(String osType) {
		this.osType = osType;
	}

	public String getDcName() {
		return dcName;
	}

	public void setDcName(String dcName) {
		this.dcName = dcName;
	}

	public String getPrjName() {
		return prjName;
	}

	public void setPrjName(String prjName) {
		this.prjName = prjName;
	}

	public String getCusName() {
		return cusName;
	}

	public void setCusName(String cusName) {
		this.cusName = cusName;
	}

	public double getCpu() {
		return cpu;
	}

	public void setCpu(double cpu) {
		this.cpu = cpu;
	}

	public int getCpuDiff() {
		return cpuDiff;
	}

	public void setCpuDiff(int cpuDiff) {
		this.cpuDiff = cpuDiff;
	}

	public double getRam() {
		return ram;
	}

	public void setRam(double ram) {
		this.ram = ram;
	}

	public int getRamDiff() {
		return ramDiff;
	}

	public void setRamDiff(int ramDiff) {
		this.ramDiff = ramDiff;
	}

	public double getNetIn() {
		return netIn;
	}

	public void setNetIn(double netIn) {
		this.netIn = netIn;
	}

	public int getNetinDiff() {
		return netinDiff;
	}

	public void setNetinDiff(int netinDiff) {
		this.netinDiff = netinDiff;
	}

	public double getNetOut() {
		return netOut;
	}

	public void setNetOut(double netOut) {
		this.netOut = netOut;
	}

	public int getNetoutDiff() {
		return netoutDiff;
	}

	public void setNetoutDiff(int netoutDiff) {
		this.netoutDiff = netoutDiff;
	}

	public double getDiskWrite() {
		return diskWrite;
	}

	public void setDiskWrite(double diskWrite) {
		this.diskWrite = diskWrite;
	}

	public int getWriteDiff() {
		return writeDiff;
	}

	public void setWriteDiff(int writeDiff) {
		this.writeDiff = writeDiff;
	}

	public double getDiskRead() {
		return diskRead;
	}

	public void setDiskRead(double diskRead) {
		this.diskRead = diskRead;
	}

	public int getReadDiff() {
		return readDiff;
	}

	public void setReadDiff(int readDiff) {
		this.readDiff = readDiff;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public boolean isMongodb() {
		return mongodb;
	}

	public void setMongodb(boolean mongodb) {
		this.mongodb = mongodb;
	}

	public boolean isMongodbTwo() {
		return mongodbTwo;
	}

	public void setMongodbTwo(boolean mongodbTwo) {
		this.mongodbTwo = mongodbTwo;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public String getDataVersionName() {
		return dataVersionName;
	}

	public void setDataVersionName(String dataVersionName) {
		this.dataVersionName = dataVersionName;
	}

	public double getVolumeUsed() {
		return volumeUsed;
	}

	public void setVolumeUsed(double volumeUsed) {
		this.volumeUsed = volumeUsed;
	}

	public int getVolumeUsedDiff() {
		return volumeUsedDiff;
	}

	public void setVolumeUsedDiff(int volumeUsedDiff) {
		this.volumeUsedDiff = volumeUsedDiff;
	}

	public String getIsMaster() {
		return isMaster;
	}

	public void setIsMaster(String isMaster) {
		this.isMaster = isMaster;
	}
	
}
