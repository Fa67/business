package com.eayun.virtualization.model;
/**
 * @author jingang.liu@eayun.com to beijing
 * @date 2016年4月13日
 */
public class CloudOutIp extends BaseCloudOutIp {

	
	/**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -4966213375692505909L;
    private String dcName;				//数据中心名称
	private String prjId;				//项目Id
	private String prjName;				//项目名称
	private String useName;				//客户名称
	private String resourceId;			//资源ID
	private String resourceType;		//资源类型
	private String resoutceName;		//资源名称
	private String vmIp;				//云主机IP（内网IP）
	private String routeId;           //路由Id
	
	public CloudOutIp() {
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
	public String getUseName() {
		return useName;
	}
	public void setUseName(String useName) {
		this.useName = useName;
	}
	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	public String getResourceType() {
		return resourceType;
	}
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	public String getResoutceName() {
		return resoutceName;
	}
	public void setResoutceName(String resoutceName) {
		this.resoutceName = resoutceName;
	}
	public String getVmIp() {
		return vmIp;
	}
	public void setVmIp(String vmIp) {
		this.vmIp = vmIp;
	}
	public String getPrjId() {
		return prjId;
	}
	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}
	public String getRouteId() {
		return routeId;
	}
	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}
	
	
}
