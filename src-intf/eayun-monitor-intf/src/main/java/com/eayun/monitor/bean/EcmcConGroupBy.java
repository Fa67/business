package com.eayun.monitor.bean;

import java.util.List;

import com.eayun.monitor.model.EcmcAlarmContact;
/**
 * 运维各联系组下的联系人
 *                       
 * @Filename: EcmcConGroupBy.java
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
public class EcmcConGroupBy {

	private String contactGroupId;	//联系组id
	
	private String contactGroupName;//联系组名称
	
	private List<EcmcAlarmContact> contactList;//组内的联系人集合

	public String getContactGroupId() {
		return contactGroupId;
	}

	public void setContactGroupId(String contactGroupId) {
		this.contactGroupId = contactGroupId;
	}

	public String getContactGroupName() {
		return contactGroupName;
	}

	public void setContactGroupName(String contactGroupName) {
		this.contactGroupName = contactGroupName;
	}

	public List<EcmcAlarmContact> getContactList() {
		return contactList;
	}

	public void setContactList(List<EcmcAlarmContact> contactList) {
		this.contactList = contactList;
	}
	
	
}
