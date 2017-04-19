package com.eayun.virtualization.bean;

import java.util.Date;

/**
 * 即将到期资源
 *                       
 * @Filename: AboutToExpire.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2016年8月2日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class AboutToExpire {
	
	private String resourcesId;		//资源ID

	private String resourcesName;	//资源名称
	
	private String resourcesType;	//资源类型
	
	private Date createTime;		//资源创建时间
	
	private Date endTime;			//资源到期时间
	
	private String prjId;			//资源所属项目id
	
	private String prjName;    //所属项目（added by zengbo）

	public String getResourcesId() {
		return resourcesId;
	}

	public void setResourcesId(String resourcesId) {
		this.resourcesId = resourcesId;
	}

	public String getResourcesName() {
		return resourcesName;
	}

	public void setResourcesName(String resourcesName) {
		this.resourcesName = resourcesName;
	}

	public String getResourcesType() {
		return resourcesType;
	}

	public void setResourcesType(String resourcesType) {
		this.resourcesType = resourcesType;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getPrjId() {
		return prjId;
	}

	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}

	public String getPrjName() {
		return prjName;
	}

	public void setPrjName(String prjName) {
		this.prjName = prjName;
	}
	
	
}
