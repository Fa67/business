package com.eayun.monitor.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.log.service.LogService;
import com.eayun.monitor.model.Contact;
import com.eayun.monitor.model.ContactGroup;
import com.eayun.monitor.service.ContactService;

@Controller
@RequestMapping("/monitor/contact")
@SuppressWarnings("rawtypes")
public class ContactController {
    
    private static final Logger log = LoggerFactory.getLogger(ContactController.class);
    
    @Autowired
    private ContactService contactService;
    
    @Autowired
	private LogService logService;

    @RequestMapping(value = "/getContactList", method = RequestMethod.POST)
    @ResponseBody
    public String getContactList(HttpServletRequest request, Page page,@RequestBody ParamsMap map) throws Exception {
        log.info("联系人列表查询开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        String name = map.getParams().get("name")==null?"":map.getParams().get("name").toString();
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        
        page = contactService.getPagedContactList(cusId, name, page, queryMap);
        return JSONObject.toJSONString(page);
    }
    
    @RequestMapping(value = "/getAllContactsList", method = RequestMethod.POST)
    @ResponseBody
    public String getAllContactsList(HttpServletRequest request) throws Exception {
        log.info("联系人列表查询开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        List<Contact> contactList= contactService.getContactList(cusId);
        return JSONObject.toJSONString(contactList);
    }
    
    @RequestMapping(value = "/addContact", method = RequestMethod.POST)
    @ResponseBody
    public String addContact(HttpServletRequest request, @RequestBody Contact contact)
            throws Exception {
        log.info("添加联系人开始");
        try {
            SessionUserInfo sessionUserInfo = (SessionUserInfo) request.getSession().getAttribute(
                ConstantClazz.SYS_SESSION_USERINFO);
            String cusId = sessionUserInfo.getCusId();
            contact.setCusId(cusId);
            contact.setSmsNotify("1");
            contact.setMailNotify("1");
            contact.setIsAdmin("0");
            contact = contactService.addContact(contact);
            logService.addLog("创建联系人", "管理联系人", contact.getName(), null,ConstantClazz.LOG_STATU_SUCCESS, null);
        } catch (Exception e) {
        	logService.addLog("创建联系人", "管理联系人", contact.getName(), null,ConstantClazz.LOG_STATU_ERROR, e);
			log.error(e.toString(), e);
        }
        return JSONObject.toJSONString(contact);
    }
    
    @RequestMapping(value= "/editContact" , method = RequestMethod.POST)
    @ResponseBody
    public String editContact(HttpServletRequest request,@RequestBody Contact contact)throws AppException{
        log.info("编辑联系人开始");
        boolean isSucceed=false;
        try{
            SessionUserInfo sessionUserInfo = (SessionUserInfo) request.getSession().getAttribute(
                ConstantClazz.SYS_SESSION_USERINFO);
            String cusId = sessionUserInfo.getCusId();
            contact.setCusId(cusId);
            isSucceed=contactService.updateContact(contact);
            logService.addLog("编辑联系人", "管理联系人", contact.getName(), null,ConstantClazz.LOG_STATU_SUCCESS, null);
        }catch(AppException e){
        	logService.addLog("编辑联系人", "管理联系人", contact.getName(), null,ConstantClazz.LOG_STATU_ERROR, e);
			log.error(e.toString(), e);
        }
        return JSONObject.toJSONString(isSucceed);
    }
    
    @RequestMapping(value = "/deleteContact" , method = RequestMethod.POST)
    @ResponseBody
    public String deleteContact(HttpServletRequest request , @RequestBody Map map) throws Exception {
        log.info("删除联系人开始");
        boolean isDeleted = false;
        String contactName = map.get("contactName").toString();
        try {
            String contactId = map.get("contactId").toString();
            isDeleted = contactService.deleteContact(contactId);
            logService.addLog("删除联系人", "管理联系人", contactName, null,ConstantClazz.LOG_STATU_SUCCESS, null);
        } catch (Exception e) {
        	logService.addLog("删除联系人", "管理联系人", contactName, null,ConstantClazz.LOG_STATU_ERROR, e);
			log.error(e.toString(), e);
        }
        return JSONObject.toJSONString(isDeleted);
    }
    
    
    @RequestMapping(value="/updateMailSelection", method=RequestMethod.POST)
    @ResponseBody
    public boolean updateMailSelection(HttpServletRequest request, @RequestBody Map map) throws Exception{
        String contactId = map.get("contactId").toString();
        String isChecked = map.get("isChecked").toString();
        String contactName = map.get("contactName").toString();
        boolean isok = false;
		try {
			isok = contactService.updateNotifyMethod("email",isChecked,contactId);
			logService.addLog("更新通知方式", "管理联系人", contactName, null,ConstantClazz.LOG_STATU_SUCCESS, null);
        } catch (Exception e) {
        	logService.addLog("更新通知方式", "管理联系人", contactName, null,ConstantClazz.LOG_STATU_ERROR, e);
			log.error(e.toString(), e);
		}
        return isok;
    }
    
    @RequestMapping(value="/updateSmsSelection", method=RequestMethod.POST)
    @ResponseBody
    public boolean updateSmsSelection(HttpServletRequest request, @RequestBody Map map) throws Exception{
        String contactId = map.get("contactId").toString();
        String isChecked = map.get("isChecked").toString();
        String contactName = map.get("contactName").toString();
        boolean isok = false;
		try {
			isok = contactService.updateNotifyMethod("sms",isChecked,contactId);
			logService.addLog("更新通知方式", "管理联系人", contactName, null,ConstantClazz.LOG_STATU_SUCCESS, null);
        } catch (Exception e) {
        	logService.addLog("更新通知方式", "管理联系人", contactName, null,ConstantClazz.LOG_STATU_ERROR, e);
			log.error(e.toString(), e);
		}
        return isok;
    }
    
    @RequestMapping(value = "/getContactGroupList", method = RequestMethod.POST)
    @ResponseBody
    public String getContactGroupList(HttpServletRequest request) throws Exception {
        log.info("联系组列表查询开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        List<ContactGroup> contactGroupList = contactService.getContactGroupList(cusId);
        return JSONObject.toJSONString(contactGroupList);
    }
    
    @RequestMapping(value = "/getContactListInGroup", method = RequestMethod.POST)
    @ResponseBody
    public String getContactListInGroup(HttpServletRequest request,Page page,@RequestBody ParamsMap map) throws Exception {
        log.info("查询联系组内联系人列表查询开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        String name = map.getParams().get("name")==null?"":map.getParams().get("name").toString();
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        page = contactService.getPagedContactListInGroup(cusId, name,page,queryMap);
        return JSONObject.toJSONString(page);
    }
    
    @RequestMapping(value = "/getContactListOutOfGroup", method = RequestMethod.POST)
    @ResponseBody
    public String getContactListOutOfGroup(HttpServletRequest request,@RequestBody Map map) throws Exception {
        log.info("查询联系组外联系人列表查询开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        String contactGroupName = map.get("contactGroupName")==null?"":map.get("contactGroupName").toString();
        
        List<Contact> ctcList = contactService.getContactListOutOfGroup(cusId, contactGroupName);
        return JSONObject.toJSONString(ctcList);
    }
    
    @RequestMapping(value = "/removeContactFromGroup", method = RequestMethod.POST)
    @ResponseBody
    public String removeContactFromGroup(HttpServletRequest request,@RequestBody Map map) throws Exception {
        log.info("将联系人移除联系组开始");
        boolean isSucceed = false;
        String contactGroupId = map.get("groupId")==null?"":map.get("groupId").toString();
        String contactId = map.get("contactId")==null?"":map.get("contactId").toString();
        String groupName = map.get("groupName")==null?"":map.get("groupName").toString();
        try {
			isSucceed = contactService.removeContactFromGroup(contactGroupId, contactId);
			logService.addLog("移除联系人", "管理联系组", groupName, null,ConstantClazz.LOG_STATU_SUCCESS, null);
        } catch (Exception e) {
        	logService.addLog("移除联系人", "管理联系组", groupName, null,ConstantClazz.LOG_STATU_ERROR, e);
			log.error(e.toString(), e);
		}
        return JSONObject.toJSONString(isSucceed);
    }
    
    @RequestMapping(value = "/addContct2Group", method = RequestMethod.POST)
    @ResponseBody
    public String addContct2Group(HttpServletRequest request,@RequestBody Map map) throws Exception {
        log.info("将联系人添加到联系组开始");
        boolean isSucceed = false;
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        String contactGroupName = map.get("contactGroupName")==null?"":map.get("contactGroupName").toString();
        String contactId = map.get("contactId")==null?"":map.get("contactId").toString();
        
        try {
			isSucceed = contactService.addContact2Group(cusId,contactGroupName, contactId);
			logService.addLog("添加联系人", "管理联系组", contactGroupName, null,ConstantClazz.LOG_STATU_SUCCESS, null);
        } catch (Exception e) {
        	logService.addLog("添加联系人", "管理联系组", contactGroupName, null,ConstantClazz.LOG_STATU_ERROR, e);
			log.error(e.toString(), e);
		}
        return JSONObject.toJSONString(isSucceed);
    }
    
    @RequestMapping(value="/addContactGroup", method = RequestMethod.POST)
    @ResponseBody
    public String addContactGroup(HttpServletRequest request, @RequestBody ContactGroup contactGroup) throws Exception{
        log.info("添加联系组开始");
        try {
            SessionUserInfo sessionUserInfo = (SessionUserInfo) request.getSession().getAttribute(
                ConstantClazz.SYS_SESSION_USERINFO);
            String cusId = sessionUserInfo.getCusId();
            contactGroup.setCusId(cusId);
            contactGroup.setContactNum(0);
            contactGroup = contactService.addContactGroup(contactGroup);
            logService.addLog("添加联系组", "管理联系组", contactGroup.getName(), null,ConstantClazz.LOG_STATU_SUCCESS, null);
        } catch (Exception e) {
        	logService.addLog("添加联系组", "管理联系组", contactGroup.getName(), null,ConstantClazz.LOG_STATU_ERROR, e);
			log.error(e.toString(), e);
        }
        return JSONObject.toJSONString(contactGroup);
    }
    
    @RequestMapping(value= "/editContactGroup" , method = RequestMethod.POST)
    @ResponseBody
    public String editContactGroup(HttpServletRequest request,@RequestBody ContactGroup contactGroup)throws AppException{
        log.info("编辑联系组开始");
        boolean isSucceed=false;
        try{
            SessionUserInfo sessionUserInfo = (SessionUserInfo) request.getSession().getAttribute(
                ConstantClazz.SYS_SESSION_USERINFO);
            String cusId = sessionUserInfo.getCusId();
            contactGroup.setCusId(cusId);
            isSucceed=contactService.updateContactGroup(contactGroup);
            logService.addLog("编辑联系组", "管理联系组", contactGroup.getName(), null,ConstantClazz.LOG_STATU_SUCCESS, null);
        }catch(AppException e){
        	logService.addLog("编辑联系组", "管理联系组", contactGroup.getName(), null,ConstantClazz.LOG_STATU_ERROR, e);
			log.error(e.toString(), e);
        }
        return JSONObject.toJSONString(isSucceed);
    }
    
    @RequestMapping(value = "/deleteContactGroup" , method = RequestMethod.POST)
    @ResponseBody
    public String deleteContactGroup(HttpServletRequest request , @RequestBody Map map) throws Exception {
        log.info("删除联系组开始");
        boolean isDeleted = false;
        String groupName = map.get("groupName").toString();
        try {
            String ctcGrpId = map.get("contactGroupId").toString();
            isDeleted = contactService.deleteContactGroup(ctcGrpId);
            logService.addLog("删除联系组", "管理联系组", groupName, null,ConstantClazz.LOG_STATU_SUCCESS, null);
        } catch (Exception e) {
        	logService.addLog("删除联系组", "管理联系组", groupName, null,ConstantClazz.LOG_STATU_ERROR, e);
			log.error(e.toString(), e);
        }
        return JSONObject.toJSONString(isDeleted);
    }
    
    @RequestMapping(value = "/checkContactGroupName" , method = RequestMethod.POST)
    @ResponseBody
    public boolean checkContactGroupName(HttpServletRequest request, @RequestBody Map map) throws Exception {
        log.info("验证联系组名称重复开始");
        String contactGroupId = map.get("contactGroupId")==null?null:map.get("contactGroupId").toString();
        String contactGroupName = map.get("contactGroupName").toString();
        SessionUserInfo sessionUserInfo = (SessionUserInfo) request.getSession().getAttribute(
            ConstantClazz.SYS_SESSION_USERINFO);
        return contactService.checkContactGroupName(sessionUserInfo.getCusId(), contactGroupName, contactGroupId);
    }
    @RequestMapping(value = "/checkContactName" , method = RequestMethod.POST)
    @ResponseBody
    public boolean checkContactName(HttpServletRequest request, @RequestBody Map map) throws Exception {
        log.info("验证联系人名称重复开始");
        String contactId = map.get("contactId")==null?null:map.get("contactId").toString();
        String contactName = map.get("contactName").toString();
        SessionUserInfo sessionUserInfo = (SessionUserInfo) request.getSession().getAttribute(
            ConstantClazz.SYS_SESSION_USERINFO);
        return contactService.checkContactName(sessionUserInfo.getCusId(), contactName, contactId);
    }
    
}
