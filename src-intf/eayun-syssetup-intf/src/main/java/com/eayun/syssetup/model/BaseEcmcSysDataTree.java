package com.eayun.syssetup.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sys_data_tree")
public class BaseEcmcSysDataTree implements Serializable {
	
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "node_id", unique = true, nullable = false, length = 200)
	private String nodeId;																				//ID
	
	@Column(name = "node_name", length = 200)
	private String nodeName;	
	
	@Column(name = "parent_id", length = 50)
	private String parentId;	
	
	@Column(name = "sort", length = 22)
	private long sort;
	
	@Column(name = "is_root", length = 50)
	private String isRoot;
	
	@Column(name = "memo", length = 2000)
	private String memo;
	
	@Column(name = "flag", length = 50)
	private String flag;
	
	@Column(name = "para1", length = 500)
	private String para1;
	
	@Column(name = "para2", length = 200)
	private String para2;
	
	@Column(name = "image_path", length = 500)
	private String imagePath;
	
	@Column(name = "node_name_en", length = 200)
	private String nodeNameEn;
	
	@Column(name = "icon", length = 50)
	private String icon;
	
	public BaseEcmcSysDataTree(){
		
	}
	
	public BaseEcmcSysDataTree(String nodeId, String parentId){
		this.nodeId = nodeId;
		this.parentId = parentId;
	}
	
	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
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
}
