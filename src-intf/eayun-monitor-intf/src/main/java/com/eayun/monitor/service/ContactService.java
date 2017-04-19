package com.eayun.monitor.service;

import java.util.List;
import java.util.Map;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.monitor.model.Contact;
import com.eayun.monitor.model.ContactGroup;

public interface ContactService {

    public List<Contact> getContactList(String cusId) throws AppException;

    public Page getPagedContactList(String cusId, String name, Page page, QueryMap queryMap) throws AppException;

    public Contact addContact(Contact contact) throws AppException;

    public boolean updateNotifyMethod(String mode, String isChecked, String contactId) throws AppException;

    public boolean updateContact(Contact contact) throws AppException;

    public boolean deleteContact(String contactId) throws AppException;

    public boolean isLinkedToAlarmRule(String contactId) throws AppException;

    public List<ContactGroup> getContactGroupList(String cusId) throws AppException;

    public ContactGroup addContactGroup(ContactGroup contactGroup) throws AppException;

    public boolean checkContactGroupName(String cusId, String contactGroupName, String contactGroupId) throws AppException;

    public List<Contact> getContactListInGroup(String cusId, String name) throws AppException;

    public Page getPagedContactListInGroup(String cusId, String name, Page page, QueryMap queryMap) throws AppException;

    public boolean updateContactGroup(ContactGroup contactGroup) throws AppException;

    public boolean deleteContactGroup(String ctcGrpId) throws AppException;

    public List<Contact> getContactListOutOfGroup(String cusId, String contactGroupName) throws AppException;

    public boolean removeContactFromGroup(String contactGroupId, String contactId) throws AppException;

    public boolean addContact2Group(String cusId, String contactGroupName, String contactId) throws AppException;

    public boolean checkContactName(String cusId, String contactName, String contactId) throws AppException;

    public Map<String,String> getAdminContact(String customerId, String projectId);
}
