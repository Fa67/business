package com.eayun.sys.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sys_data_tree")
public class BaseSysDataTree implements java.io.Serializable {

	private static final long serialVersionUID = 1344550711144463974L;

	@Id
	@Column(name = "NODE_ID")
	private String nodeId;

	@Column(name = "NODE_NAME")
	private String nodeName;

	@Column(name = "NODE_NAME_EN")
	private String nodeNameEn;

	@Column(name = "PARENT_ID")
	private String parentId;

	@Column(name = "SORT")
	private long sort;

	@Column(name = "IS_ROOT")
	private String isRoot;

	@Column(name = "MEMO")
	private String memo;

	@Column(name = "FLAG")
	private String flag;

	@Column(name = "PARA1")
	private String para1;

	@Column(name = "PARA2")
	private String para2;
	
	@Column(name = "ICON")
	private String icon;
	
	@Column(name = "image_path")
	private String imagePath;
	
	public String getNodeNameEn() {
		return nodeNameEn;
	}

	public void setNodeNameEn(String nodeNameEn) {
		this.nodeNameEn = nodeNameEn;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public long getSort() {
		return sort;
	}

	public void setSort(long sort) {
		this.sort = sort;
	}

	public String getIsRoot() {
		return isRoot;
	}

	public void setIsRoot(String isRoot) {
		this.isRoot = isRoot;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getPara1() {
		return para1;
	}

	public void setPara1(String para1) {
		this.para1 = para1;
	}

	public String getPara2() {
		return para2;
	}

	public void setPara2(String para2) {
		this.para2 = para2;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	
	
}