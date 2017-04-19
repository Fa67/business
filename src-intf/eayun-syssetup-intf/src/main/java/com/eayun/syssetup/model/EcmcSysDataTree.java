package com.eayun.syssetup.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EcmcSysDataTree extends BaseEcmcSysDataTree {
    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -8547283853913340189L;

    private static final Logger log = LoggerFactory.getLogger(EcmcSysDataTree.class);
    
	private boolean leaf;
	
	private boolean closed;
	
	private boolean checked;
	
	private List<EcmcSysDataTree> children = new ArrayList<EcmcSysDataTree>();
	
	private int childrenSize;			//子节点数量（Added by zengbo）
	
	private String nodeNameZh;	//节点中文名称（Added by zengbo）
	
	public String getNodeNameZh() {
		return nodeNameZh;
	}

	public void setNodeNameZh(String nodeNameZh) {
		this.nodeNameZh = nodeNameZh;
	}

	public int getChildrenSize() {
		return childrenSize;
	}

	public void setChildrenSize(int childrenSize) {
		this.childrenSize = childrenSize;
	}

	public boolean isLeaf() {
//		if (this.children.size() > 0) {
//			return false;
//		}
		return leaf;
	}

	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public List<EcmcSysDataTree> getChildren() {
		return children;
	}

	public void setChildren(List<EcmcSysDataTree> children) {
		this.children = children;
	}
	
	public void addChild(EcmcSysDataTree dataTree) {
		try {
			if (this.children == null) {
				this.children = new ArrayList<EcmcSysDataTree>();
			}
			this.children.add(dataTree);
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
		}
		
	}
}
