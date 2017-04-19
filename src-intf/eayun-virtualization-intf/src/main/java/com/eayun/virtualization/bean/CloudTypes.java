package com.eayun.virtualization.bean;

import java.util.Date;
import java.util.List;
/**
 * 资源统计大类别列表           
 * @Filename: CloudTitle.java
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
public class CloudTypes {
    
    private String Type;            //资源统计类型        net:网络；vm:云主机；vol:云硬盘
    
    private String vmFlavorName;      //云主机类型名称  
    
    private String vmFlavorCpu;       //cpu核数
    
    private String vmFlavorRam;       //内存大小
    
    private String vmFlavorSys;       //操作系统
    
    private int vmCount;            //云主机数量
    
    private long vmTimeHours;       //云主机累计时间(总计)
    
    
    
    private String volumeTypeNmae;     //云硬盘类型名
    
    private int volumeSize;         //云硬盘大小
    
    private int volumeCount;        //云硬盘数量
    
    private long volTimeHours;      //云硬盘累计时间(总计)
    
    
    private double netUpFlowCount;       //上行累计
    
    private double netDownFlowCount;     //下行累计
    
    private Date startTime;         //开始时间
    
    private Date endTime;           //截止时间
    
    
    
    
    private List<CloudDetails> detailsList; //详情信息列表
    
    

    


    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getVmFlavorName() {
        return vmFlavorName;
    }

    public void setVmFlavorName(String vmFlavorName) {
        this.vmFlavorName = vmFlavorName;
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

    public String getVmFlavorSys() {
        return vmFlavorSys;
    }

    public void setVmFlavorSys(String vmFlavorSys) {
        this.vmFlavorSys = vmFlavorSys;
    }

    public int getVmCount() {
        return vmCount;
    }

    public void setVmCount(int vmCount) {
        this.vmCount = vmCount;
    }

    public long getVmTimeHours() {
        return vmTimeHours;
    }

    public void setVmTimeHours(long vmTimeHours) {
        this.vmTimeHours = vmTimeHours;
    }

    public String getVolumeTypeNmae() {
        return volumeTypeNmae;
    }

    public void setVolumeTypeNmae(String volumeTypeNmae) {
        this.volumeTypeNmae = volumeTypeNmae;
    }

    public int getVolumeSize() {
        return volumeSize;
    }

    public void setVolumeSize(int volumeSize) {
        this.volumeSize = volumeSize;
    }

    public int getVolumeCount() {
        return volumeCount;
    }

    public void setVolumeCount(int volumeCount) {
        this.volumeCount = volumeCount;
    }

    public long getVolTimeHours() {
        return volTimeHours;
    }

    public void setVolTimeHours(long volTimeHours) {
        this.volTimeHours = volTimeHours;
    }

    public double getNetUpFlowCount() {
        return netUpFlowCount;
    }

    public void setNetUpFlowCount(double netUpFlowCount) {
        this.netUpFlowCount = netUpFlowCount;
    }

    public double getNetDownFlowCount() {
        return netDownFlowCount;
    }

    public void setNetDownFlowCount(double netDownFlowCount) {
        this.netDownFlowCount = netDownFlowCount;
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

    public List<CloudDetails> getDetailsList() {
        return detailsList;
    }

    public void setDetailsList(List<CloudDetails> detailsList) {
        this.detailsList = detailsList;
    }

    
}
