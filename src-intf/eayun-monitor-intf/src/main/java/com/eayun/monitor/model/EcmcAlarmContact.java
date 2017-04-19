package com.eayun.monitor.model;
/**
 * 运维报警规则-联系人
 *                       
 * @Filename: EcmcAlarmContact.java
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
public class EcmcAlarmContact extends BaseEcmcAlarmContact {

	/**
	 *Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -7400490131021384662L;
	
	private String contactGroupId;	//联系组id,目前无处用到（16-04-07）
	
	private String contactGroupName;//联系组名称,列表显示时，若属于多个联系组，则都显示出来，之间用逗号分隔
	
	private String contactName;		//联系人名称
	
	private String contactMethod;	//联系方式

	private String contactPhone ;   //联系人电话

	private String contactEmail ;   //联系人电子邮件

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

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactMethod() {
		return contactMethod;
	}

	public void setContactMethod(String contactMethod) {
		this.contactMethod = contactMethod;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}

	public String getContactPhone() {
		return contactPhone;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getContactEmail() {
		return contactEmail;
	}
}
