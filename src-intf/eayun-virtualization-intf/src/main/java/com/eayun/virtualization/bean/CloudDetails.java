package com.eayun.virtualization.bean;

import java.util.Date;
/**
 * 资源统计特定类型对象信息列表                 
 * @Filename: CloudDetails.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2015年11月11日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class CloudDetails {
	
	private String Type;            //资源统计类型        net:网络；vm:云主机；vol:云硬盘
	
	private String vmFlavorSys;       //操作系统

    private String vmName;          //云主机名称
    
    private String vmFlavorCpu;       //cpu核数
    
    private String vmFlavorRam;       //内存大小
    
    private int vmvolCount;         //挂接云硬盘数量
    
    private long vmHour;            //云主机累计时间(单个)
    
    
    
    private String volumeName;      //云硬盘名称
    
    private int volumeSize;         //云硬盘大小 
    
    private String volvmName;       //挂接云主机名称
    
    private long volHour;           //云硬盘累计时间(单个)
    
    
    private String everyDate;       //查询时间
    
    private double upCount;         //上行流量
    
    private double downCount;       //下行流量
    
    
    private Date startTime;         //开始时间
    
    private Date endTime;           //截止时间

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public String getVmFlavorCpu() {
        return vmFlavorCpu;
    }

    public void setVmFlavorCpu(String vmFlavorCpu) {
        this.vmFlavorCpu = vmFlavorCpu;
    }

    public String getVmFlavorRam() {
        return vmFlavorRam;
    }

    public void setVmFlavorRam(String vmFlavorRam) {
        this.vmFlavorRam = vmFlavorRam;
    }

    public int getVmvolCount() {
        return vmvolCount;
    }

    public void setVmvolCount(int vmvolCount) {
        this.vmvolCount = vmvolCount;
    }

    public long getVmHour() {
        return vmHour;
    }

    public void setVmHour(long vmHour) {
        this.vmHour = vmHour;
    }

    public String getVolumeName() {
        return volumeName;
    }

    public void setVolumeName(String volumeName) {
        this.volumeName = volumeName;
    }

    public int getVolumeSize() {
        return volumeSize;
    }

    public void setVolumeSize(int volumeSize) {
        this.volumeSize = volumeSize;
    }

    public String getVolvmName() {
        return volvmName;
    }

    public void setVolvmName(String volvmName) {
        this.volvmName = volvmName;
    }

    public long getVolHour() {
        return volHour;
    }

    public void setVolHour(long volHour) {
        this.volHour = volHour;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getEveryDate() {
        return everyDate;
    }

    public void setEveryDate(String everyDate) {
        this.everyDate = everyDate;
    }

    public double getUpCount() {
        return upCount;
    }

    public void setUpCount(double upCount) {
        this.upCount = upCount;
    }

    public double getDownCount() {
        return downCount;
    }

    public void setDownCount(double downCount) {
        this.downCount = downCount;
    }

	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}

	public String getVmFlavorSys() {
		return vmFlavorSys;
	}

	public void setVmFlavorSys(String vmFlavorSys) {
		this.vmFlavorSys = vmFlavorSys;
	}
    
}
