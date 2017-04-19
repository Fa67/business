package com.eayun.monitor.model;
/**
 * 运维报警对象扩展类
 *                       
 * @Filename: EcmcAlarmobject.java
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
public class EcmcAlarmObject extends BaseEcmcAlarmObject {

	/**
	 *Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -2434136121448328011L;
	
	private String cusName;	//客户名称（所属客户）
	
	private String prjName;	//项目名称
	
	private String dcName;	//数据中心名称
	
	private String network;	//所属网络名称
	
	private String vmIp;	//受管子网IP
	
	private String floatIp;	//弹性公网IP
	
	private String objName; //对象资源名称
	
	/*****************RDS1.0&网络1.3新增*************/
	
	private String dataVersionName;//数据库实例版本
	private String selfSubIp;   //自管子网IP
	private String config;		//负载均衡配置
	private String isMaster;	//是否主库（数据库实例）
    private String mode;		//负载均衡模式
	
	private Boolean isDeleted;	//对象资源是否已删除（解决垃圾数据问题）
	/*****************RDS1.0&网络1.3新增*************/

	public String getCusName() {
		return cusName;
	}

	public void setCusName(String cusName) {
		this.cusName = cusName;
	}

	public String getPrjName() {
		return prjName;
	}

	public void setPrjName(String prjName) {
		this.prjName = prjName;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public String getVmIp() {
		return vmIp;
	}

	public void setVmIp(String vmIp) {
		this.vmIp = vmIp;
	}

	public String getFloatIp() {
		return floatIp;
	}

	public void setFloatIp(String floatIp) {
		this.floatIp = floatIp;
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

	public String getDataVersionName() {
		return dataVersionName;
	}

	public void setDataVersionName(String dataVersionName) {
		this.dataVersionName = dataVersionName;
	}

	public String getSelfSubIp() {
		return selfSubIp;
	}

	public void setSelfSubIp(String selfSubIp) {
		this.selfSubIp = selfSubIp;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getIsMaster() {
		return isMaster;
	}

	public void setIsMaster(String isMaster) {
		this.isMaster = isMaster;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}
	
	
	
}
