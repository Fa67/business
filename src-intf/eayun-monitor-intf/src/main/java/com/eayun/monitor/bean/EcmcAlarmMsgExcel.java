package com.eayun.monitor.bean;

import com.eayun.common.tools.ExcelTitle;

/**
 * 导出运维报警信息model
 *                       
 * @Filename: EcmcAlarmMsgExcel.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2016年3月31日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class EcmcAlarmMsgExcel {
	
	@ExcelTitle(name="分类")
    private String type;
	
	@ExcelTitle(name="对象名称")
    private String objName;
	
	@ExcelTitle(name="客户")
    private String cusName;
	
	@ExcelTitle(name="数据中心")
    private String dcName;
	
	@ExcelTitle(name="项目")
    private String projectName;
    
    @ExcelTitle(name="报警时间")
    private String alarmTime;
    
    @ExcelTitle(name="报警信息")
    private String alarmDetail;
    
    @ExcelTitle(name="报警标识")
    private String alarmSign;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getObjName() {
		return objName;
	}

	public void setObjName(String objName) {
		this.objName = objName;
	}

	public String getCusName() {
		return cusName;
	}

	public void setCusName(String cusName) {
		this.cusName = cusName;
	}

	public String getDcName() {
		return dcName;
	}

	public void setDcName(String dcName) {
		this.dcName = dcName;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
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
    
}
