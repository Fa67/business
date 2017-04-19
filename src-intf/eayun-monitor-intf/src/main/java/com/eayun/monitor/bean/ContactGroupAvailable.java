package com.eayun.monitor.bean;

import java.util.List;

import com.eayun.monitor.model.AlarmContact;
/**
 * 用于添加报警联系人页面中的待选联系人的分组及人员展示
 *                       
 * @Filename: ContactGroupsAvailable.java
 * @Description: 
 * @Version: 1.0
 * @Author: fan.zhang
 * @Email: fan.zhang@eayun.com
 * @History:<br>
 *<li>Date: 2015年12月21日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class ContactGroupAvailable {
    
    private String contactGroupId;
    private String contactGroupName;
    private List<AlarmContact> contactList;
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
    public List<AlarmContact> getContactList() {
        return contactList;
    }
    public void setContactList(List<AlarmContact> contactList) {
        this.contactList = contactList;
    }

}
