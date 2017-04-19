package com.eayun.monitor.ecmccontroller;

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
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.monitor.ecmcservice.EcmcContactService;
import com.eayun.monitor.model.EcmcContact;
import com.eayun.monitor.model.EcmcContactGroup;

@Controller
@RequestMapping("/ecmc/monitor/contact")
@SuppressWarnings("rawtypes")
public class EcmcContactController {

	private static final Logger log = LoggerFactory.getLogger(EcmcContactController.class);

	@Autowired
	private EcmcContactService ecmcContactService;
	@Autowired
	private EcmcLogService ecmcLogService;
	@RequestMapping(value = "/getecmcpagecontactlist", method = RequestMethod.POST)
	@ResponseBody
	public String getPagedContactList(HttpServletRequest request, Page page, @RequestBody ParamsMap map) {
		log.info("分页获取所有联系人列表开始");
		String name = map.getParams().get("name") == null ? "" : map.getParams().get("name").toString();
		String type = map.getParams().get("type") == null ? "0" : map.getParams().get("type").toString();
		String cusId = map.getParams().get("cusId") == null ? "" : map.getParams().get("cusId").toString();
		int pageSize = map.getPageSize();
		int pageNumber = map.getPageNumber();

		QueryMap queryMap = new QueryMap();
		queryMap.setPageNum(pageNumber);
		queryMap.setCURRENT_ROWS_SIZE(pageSize);

		page = ecmcContactService.getPagedContactList(name, type, cusId, page, queryMap);
		return JSONObject.toJSONString(page);

	}

	@RequestMapping(value = "/addecmccontact", method = RequestMethod.POST)
	@ResponseBody
	public String addContact(HttpServletRequest request, @RequestBody EcmcContact ecmcContact) {
		log.info("创建运维联系人");
		JSONObject json = new JSONObject();
		// 默认指定联系人联系方式：短信和邮件。
		ecmcContact.setSmsNotify("1");
		ecmcContact.setMailNotify("1");
		try {
			ecmcContact = ecmcContactService.addContact(ecmcContact);
			ecmcLogService.addLog("创建联系人", ConstantClazz.LOG_TYPE_CONTACT, ecmcContact.getName(), null, 1, ecmcContact.getId(), null);

			json.put("respCode", ConstantClazz.SUCCESS_CODE);
			json.put("data", ecmcContact);
		} catch (AppException e) {
			ecmcLogService.addLog("创建联系人", ConstantClazz.LOG_TYPE_CONTACT, ecmcContact.getName(), null, 0, ecmcContact.getId(), e);

			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error("创建运维联系人异常", e);
		}
		return json.toJSONString();
	}

	@RequestMapping(value = "/editecmccontact", method = RequestMethod.POST)
	@ResponseBody
	public String editContact(HttpServletRequest request, @RequestBody EcmcContact ecmcContact) {
		log.info("编辑运维联系人");
		JSONObject json = new JSONObject();
		try {
			ecmcContact = ecmcContactService.editContact(ecmcContact);
			ecmcLogService.addLog("编辑联系人", ConstantClazz.LOG_TYPE_CONTACT, ecmcContact.getName(), null, 1, ecmcContact.getId(), null);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
		} catch (AppException e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("编辑联系人", ConstantClazz.LOG_TYPE_CONTACT, ecmcContact.getName(), null, 0, ecmcContact.getId(), e);
			log.error("编辑运维联系人异常", e);
		}
		return json.toJSONString();
	}

	@RequestMapping(value = "/deleteecmccontact", method = RequestMethod.POST)
	@ResponseBody
	public String deleteContact(HttpServletRequest request, @RequestBody Map map) {
		log.info("删除运维联系人");
		JSONObject json = new JSONObject();
		String contactName=map.get("contactName") == null ? "" : map.get("contactName").toString();
		String contactId = map.get("contactId") == null ? "" : map.get("contactId").toString();
		try {
			
			ecmcContactService.deleteContact(contactId);
			ecmcLogService.addLog("删除联系人", ConstantClazz.LOG_TYPE_CONTACT, contactName, null, 1, contactId, null);

			json.put("respCode", ConstantClazz.SUCCESS_CODE);
		} catch (AppException e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("删除联系人", ConstantClazz.LOG_TYPE_CONTACT, contactName, null, 0, contactId, null);

			log.error("删除运维联系人异常", e);
		}
		return json.toJSONString();
	}

	@RequestMapping(value = "/updateecmcselection", method = RequestMethod.POST)
	@ResponseBody
	public String updateNoticeMethod(HttpServletRequest request, @RequestBody Map map) {
		log.info("更新运维联系人通知方式");
		JSONObject json = new JSONObject();
		String contactName=map.get("mcName").toString();
		String contactId = map.get("mcId").toString();
		String isChecked = map.get("isChecked").toString();
		String type = map.get("type").toString();
		try {
			ecmcContactService.updateNoticeMethod(type, isChecked, contactId);
			ecmcLogService.addLog("更新通知方式", ConstantClazz.LOG_TYPE_CONTACT, contactName, null, 1, contactId, null);

			json.put("respCode", ConstantClazz.SUCCESS_CODE);
		} catch (AppException e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("更新通知方式", ConstantClazz.LOG_TYPE_CONTACT, contactName, null, 0, contactId, null);

			log.error("更新运维联系人通知方式异常", e);
		}
		return json.toJSONString();
	}

	@RequestMapping(value = "/checkecmcconname", method = RequestMethod.POST)
	@ResponseBody
	public String checkContactName(HttpServletRequest request, @RequestBody Map map) {
		log.info("验证运维联系人名称重复开始");
		JSONObject json = new JSONObject();
		String contactId = map.get("id") == null ? null : map.get("id").toString();
		String contactName = map.get("name").toString();
		boolean isOneAndOnly = false;
		try {
			isOneAndOnly = ecmcContactService.checkContactName(contactId, contactName);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
		} catch (AppException e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error("验证运维联系人名称重复异常", e);
		}
		json.put("result", isOneAndOnly);
		return json.toJSONString();
	}
	
	@RequestMapping(value="/getecmcgrouplist", method=RequestMethod.POST)
	@ResponseBody
	public String getContactGroupList(HttpServletRequest request){
		log.info("获取运维联系组列表开始");
		List<EcmcContactGroup> contactGroupList = ecmcContactService.getContactGroupList();
		return JSONObject.toJSONString(contactGroupList);
	}
	
	@RequestMapping(value="/addecmcgroup", method = RequestMethod.POST)
    @ResponseBody
    public String addContactGroup(HttpServletRequest request, @RequestBody EcmcContactGroup ecmcContactGroup){
        log.info("添加运维联系组开始");
        JSONObject json = new JSONObject();
        try {
            ecmcContactGroup.setContactNum(0);
            ecmcContactGroup = ecmcContactService.addContactGroup(ecmcContactGroup);
            ecmcLogService.addLog("添加联系组", ConstantClazz.LOG_TYPE_CONTACT_GROUP, ecmcContactGroup.getName(), null, 1, ecmcContactGroup.getId(), null);

            json.put("respCode", ConstantClazz.SUCCESS_CODE);
        } catch (AppException e) {
        	json.put("respCode", ConstantClazz.ERROR_CODE);
            ecmcLogService.addLog("添加联系组", ConstantClazz.LOG_TYPE_CONTACT_GROUP, ecmcContactGroup.getName(), null, 0, ecmcContactGroup.getId(), e);

			log.error("添加运维联系组异常", e);
        }
        return json.toJSONString();
    }
	
	@RequestMapping(value = "/checkecmcgroupname", method = RequestMethod.POST)
	@ResponseBody
	public String checkGroupName(HttpServletRequest request, @RequestBody Map map) {
		log.info("验证运维联系组名称重复开始");
		JSONObject json = new JSONObject();
		String ctcGrpId = map.get("id") == null ? "" : map.get("id").toString();
		String ctcGrpName = map.get("name").toString();
		boolean isOneAndOnly = false;
		try {
			isOneAndOnly = ecmcContactService.checkContactGroupName(ctcGrpId, ctcGrpName);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
		} catch (AppException e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error("验证运维联系组名称重复异常", e);
		}
		json.put("result", isOneAndOnly);
		return json.toJSONString();
	}
	
	@RequestMapping(value= "/editecmcgroup" , method = RequestMethod.POST)
    @ResponseBody
    public String editContactGroup(HttpServletRequest request,@RequestBody EcmcContactGroup ecmcContactGroup)throws AppException{
        log.info("编辑联系组开始");
        JSONObject json = new JSONObject();
        try{
            ecmcContactService.editContactGroup(ecmcContactGroup);
            ecmcLogService.addLog("编辑联系组", ConstantClazz.LOG_TYPE_CONTACT_GROUP, ecmcContactGroup.getName(), null, 1, ecmcContactGroup.getId(), null);

            json.put("respCode", ConstantClazz.SUCCESS_CODE);
        }catch(AppException e){
        	json.put("respCode", ConstantClazz.ERROR_CODE);
            ecmcLogService.addLog("编辑联系组", ConstantClazz.LOG_TYPE_CONTACT_GROUP, ecmcContactGroup.getName(), null, 0, ecmcContactGroup.getId(), null);

            log.error("编辑联系组异常",e);
        }
        return json.toJSONString();
    }
	
	@RequestMapping(value = "/deleteecmcgroup" , method = RequestMethod.POST)
    @ResponseBody
    public String deleteContactGroup(HttpServletRequest request , @RequestBody Map map) throws Exception {
        log.info("删除联系组开始");
        JSONObject json = new JSONObject();
        String ctcGrpId = map.get("id").toString();
        String ctcGrpName = map.get("name").toString();
        try {
            
            ecmcContactService.deleteContactGroup(ctcGrpId);
            ecmcLogService.addLog("删除联系组", ConstantClazz.LOG_TYPE_CONTACT_GROUP, ctcGrpName, null, 1, ctcGrpId, null);

            json.put("respCode", ConstantClazz.SUCCESS_CODE);
        } catch (AppException e) {
        	json.put("respCode", ConstantClazz.ERROR_CODE);
            ecmcLogService.addLog("删除联系组", ConstantClazz.LOG_TYPE_CONTACT_GROUP, ctcGrpName, null, 0, ctcGrpId, e);

            log.error("删除联系组开始",e);
        }
        return json.toJSONString();
    }
	
	@RequestMapping(value = "/getecmcconpageingroup", method = RequestMethod.POST)
    @ResponseBody
    public String getPagedContactListInGroup(HttpServletRequest request,Page page,@RequestBody ParamsMap map) throws Exception {
        log.info("联系组内联系人列表查询开始");
        String groupId = map.getParams().get("groupId")==null?"":map.getParams().get("groupId").toString();
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        page = ecmcContactService.getPagedContactListInGroup(groupId,page,queryMap);
        return JSONObject.toJSONString(page);
    }
	
	@RequestMapping(value = "/getecmcconlistoutgroup", method = RequestMethod.POST)
    @ResponseBody
    public String getContactListOutOfGroup(HttpServletRequest request,@RequestBody Map map) throws Exception {
        log.info("联系组外联系人列表查询开始");
        String groupId = map.get("groupId")==null?"":map.get("groupId").toString();
        List<EcmcContact> ctcList = ecmcContactService.getContactListOutOfGroup(groupId);
        return JSONObject.toJSONString(ctcList);
    }
	
	@RequestMapping(value = "/removeecmcconfromgroup", method = RequestMethod.POST)
    @ResponseBody
    public String removeContactFromGroup(HttpServletRequest request,@RequestBody Map map) throws Exception {
        log.info("将联系人在联系组中移除开始");
        JSONObject json = new JSONObject();
        String contactGroupName = map.get("groupName")==null?"":map.get("groupName").toString();
        String contactGroupId = map.get("groupId")==null?"":map.get("groupId").toString();
        String contactId = map.get("contactId")==null?"":map.get("contactId").toString();
        try {
        	ecmcContactService.removeContactFromGroup(contactGroupId, contactId);
            ecmcLogService.addLog("删除联系人", ConstantClazz.LOG_TYPE_CONTACT_GROUP, contactGroupName, null, 1, contactGroupId, null);

        	json.put("respCode", ConstantClazz.SUCCESS_CODE);
		} catch (AppException e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
            ecmcLogService.addLog("删除联系人", ConstantClazz.LOG_TYPE_CONTACT_GROUP, contactGroupName, null, 0, contactGroupId, e);

			log.error("将联系人在联系组中移除异常", e);
		}
        return json.toJSONString();
    }
    
    @RequestMapping(value = "/addecmccontogroup", method = RequestMethod.POST)
    @ResponseBody
    public String addContctToGroup(HttpServletRequest request,@RequestBody Map map) throws Exception {
        log.info("将联系人添加到联系组开始");
        JSONObject json = new JSONObject();
        String contactGroupName = map.get("groupName")==null?"":map.get("groupName").toString();
        String contactGroupId = map.get("groupId")==null?"":map.get("groupId").toString();
        String contactId = map.get("contactId")==null?"":map.get("contactId").toString();
        try {
        	ecmcContactService.addContactToGroup(contactGroupId, contactId);
            ecmcLogService.addLog("添加联系人", ConstantClazz.LOG_TYPE_CONTACT_GROUP, contactGroupName, null, 1, contactGroupId, null);

        	json.put("respCode", ConstantClazz.SUCCESS_CODE);
		} catch (AppException e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("添加联系人", ConstantClazz.LOG_TYPE_CONTACT_GROUP, contactGroupName, null, 0, contactGroupId, e);
			log.error("将联系人添加到联系组异常",e);
			throw e;
		}
        return json.toJSONString();
    }
	
	
	
}
