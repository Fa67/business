package com.eayun.mail.model;

import java.util.List;

public class EcmcMailBll extends BaseMail{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 2866280417527596281L;
	private List<String> userMailList;
    private String title;
    private String statusName;
	public List<String> getUserMailList() {
		return userMailList;
	}
	public void setUserMailList(List<String> userMailList) {
		this.userMailList = userMailList;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getStatusName() {
		return statusName;
	}
	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}
    

    
}
