package com.eayun.monitor.model;
/**
 * 运维报警信息
 *                       
 * @Filename: EcmcAlarmMessage.java
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
public class EcmcAlarmMessage extends BaseEcmcAlarmMessage {

	/**
	 *Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 5968064668596274354L;
	
	private String            projectName;	//所属项目名称
    private String            cusName;		//客户名称（所属客户）
    private String            dcName;		//数据中心名称
    private String 			  objName;		//对象名称
    
    /*****************RDS1.0&网络1.3新增*************/
    private String 			alarmTypeName;         //报警类型名称
    private String 			monitorTypeName;       //监控项名称
    /*****************RDS1.0&网络1.3新增*************/
    
    
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
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
	public String getObjName() {
		return objName;
	}
	public void setObjName(String objName) {
		this.objName = objName;
	}
	public String getAlarmTypeName() {
		return alarmTypeName;
	}
	public void setAlarmTypeName(String alarmTypeName) {
		this.alarmTypeName = alarmTypeName;
	}
	public String getMonitorTypeName() {
		return monitorTypeName;
	}
	public void setMonitorTypeName(String monitorTypeName) {
		this.monitorTypeName = monitorTypeName;
	}

}
