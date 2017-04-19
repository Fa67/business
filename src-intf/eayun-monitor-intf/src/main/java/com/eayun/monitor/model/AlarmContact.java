package com.eayun.monitor.model;

public class AlarmContact extends BaseAlarmContact {

    private static final long serialVersionUID = 4251668347275336533L;

    private String            contactGroupId;
    private String            contactGroupName;                       //联系组名
    private String            contactName;                            //联系人姓名
    private String            contactMethod;                          //联系方式——"短信 邮件"、"短信"、"邮件"
    
    private String            alarmRuleName;							//报警规则名称

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

	public String getAlarmRuleName() {
		return alarmRuleName;
	}

	public void setAlarmRuleName(String alarmRuleName) {
		this.alarmRuleName = alarmRuleName;
	}
    
    
}
