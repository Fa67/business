package com.eayun.monitor.bean;
/**
 * 监控管理数据字典
 *                       
 * @Filename: MonitorMngData.java
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
public class MonitorMngData {

	private String name;	//中文名称
	
	private String nameEN;	//英文名称
	
	private String nodeId;	//nodeId
	
	private String parentId;//上级nodeId
	
	private String param1;	//编程参数1
	
	private String param2;	//编程参数2

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameEN() {
		return nameEN;
	}

	public void setNameEN(String nameEN) {
		this.nameEN = nameEN;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getParam1() {
		return param1;
	}

	public void setParam1(String param1) {
		this.param1 = param1;
	}

	public String getParam2() {
		return param2;
	}

	public void setParam2(String param2) {
		this.param2 = param2;
	}
	
	
}
