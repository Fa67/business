package com.eayun.mail.model;

import com.eayun.sms.model.SMS;

public class EcmcSmsModel extends SMS {
	
	/**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 806792843907787126L;
    
    private String projectName;

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
}
