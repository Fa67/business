package com.eayun.ecmcdepartment.model;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EcmcSysDepartment extends BaseEcmcSysDepartment {
    private static final Logger log = LoggerFactory.getLogger(EcmcSysDepartment.class);
	private static final long serialVersionUID = 1L;

	private ArrayList<EcmcSysDepartment> children = new ArrayList<EcmcSysDepartment>();

	public ArrayList<EcmcSysDepartment> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<EcmcSysDepartment> children) {
		this.children = children;
	}
	
	public void addChild(EcmcSysDepartment department) {
		try {
			if (this.children == null) {
				this.children = new ArrayList<EcmcSysDepartment>();
			}
			this.children.add(department);
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
		}
	}
	
}
