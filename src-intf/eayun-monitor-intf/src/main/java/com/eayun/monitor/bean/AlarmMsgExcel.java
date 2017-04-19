package com.eayun.monitor.bean;

import com.eayun.common.tools.ExcelTitle;

/**
 * ECSC报警信息导出Excel模板类
 *                       
 * @Filename: AlarmMsgExcel.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2017年3月2日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class AlarmMsgExcel {

    @ExcelTitle(name="数据中心")
    private String projectName;
    @ExcelTitle(name="资源类型")
    private String monitorType;
    @ExcelTitle(name="资源名称")
    private String vmName;
    @ExcelTitle(name="报警类型")
    private String alarmType;
    @ExcelTitle(name="报警时间")
    private String alarmTime;
    @ExcelTitle(name="报警信息")
    private String alarmDetail;
    @ExcelTitle(name="报警标识")
    private String alarmSign;
    public String getProjectName() {
        return projectName;
    }
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    public String getVmName() {
        return vmName;
    }
    public void setVmName(String vmName) {
        this.vmName = vmName;
    }
    public String getAlarmType() {
        return alarmType;
    }
    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }
    public String getAlarmTime() {
        return alarmTime;
    }
    public void setAlarmTime(String alarmTime) {
        this.alarmTime = alarmTime;
    }
    public String getAlarmDetail() {
        return alarmDetail;
    }
    public void setAlarmDetail(String alarmDetail) {
        this.alarmDetail = alarmDetail;
    }
    public String getAlarmSign() {
        return alarmSign;
    }
    public void setAlarmSign(String alarmSign) {
        this.alarmSign = alarmSign;
    }
	public String getMonitorType() {
		return monitorType;
	}
	public void setMonitorType(String monitorType) {
		this.monitorType = monitorType;
	}
    
}
