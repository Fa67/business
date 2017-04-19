package com.eayun.monitor.ecmcservice;

import java.util.List;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.monitor.model.EcmcContact;
import com.eayun.monitor.model.EcmcContactGroup;

public interface EcmcContactService {

	public Page getPagedContactList(String name, String type, String cusId, Page page, QueryMap queryMap) throws AppException;

	public EcmcContact addContact(EcmcContact ecmcContact) throws AppException;

	public EcmcContact editContact(EcmcContact ecmcContact) throws AppException;

	public void deleteContact(String contactId) throws AppException;

	public void updateNoticeMethod(String type, String isChecked, String contactId) throws AppException;

	public boolean checkContactName(String contactId, String contactName) throws AppException;

	public List<EcmcContactGroup> getContactGroupList() throws AppException;

	public EcmcContactGroup addContactGroup(EcmcContactGroup ecmcContactGroup) throws AppException;

	public boolean checkContactGroupName(String contactGroupId, String contactGroupName) throws AppException;

	public EcmcContactGroup editContactGroup(EcmcContactGroup ecmcContactGroup) throws AppException;

	public void deleteContactGroup(String contactGroupId) throws AppException;

	public Page getPagedContactListInGroup(String groupId, Page page, QueryMap queryMap) throws AppException;

	public List<EcmcContact> getContactListOutOfGroup(String groupId) throws AppException;

	public void removeContactFromGroup(String contactGroupId, String contactId) throws AppException;

	public void addContactToGroup(String contactGroupId, String contactId) throws AppException;

}
