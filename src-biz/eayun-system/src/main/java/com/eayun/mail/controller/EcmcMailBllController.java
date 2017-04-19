package com.eayun.mail.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.mail.service.EcmcMailBllService;
import com.eayun.sys.model.SysDataTree;
@Controller
@RequestMapping("/ecmc/mailbll")
@Scope("prototype")
public class EcmcMailBllController {
	@Autowired
	private EcmcMailBllService mailService;
	@Autowired
	private EcmcLogService ecmcLogService;
	
	@RequestMapping(value= "/getmaillist" , method = RequestMethod.POST)
    @ResponseBody
	public String getMailList(HttpServletRequest request,Page page,@RequestBody ParamsMap paramsMap){
		page = mailService.getMailList(page,paramsMap);
		return JSONObject.toJSONString(page);
	}
	@RequestMapping(value= "/getmailstatuslist" , method = RequestMethod.POST)
    @ResponseBody
	public String getMailStatusList(HttpServletRequest request){
		List<SysDataTree> dataTreeList= mailService.getMailStatusList();
		EayunResponseJson reJson = new EayunResponseJson();
		reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		reJson.setData(dataTreeList);
		return JSONObject.toJSONString(reJson);
	}
	@SuppressWarnings("unchecked")
	@RequestMapping(value= "/sendmailbyuser" , method = RequestMethod.POST)
    @ResponseBody
	public String sendMailByUser(HttpServletRequest request,@RequestBody Map<String,Object> map)throws Exception {
		String mailId = String.valueOf(map.get("mailId"));
		String title = String.valueOf(map.get("title"));
		List<String> userMailList = (List<String>) map.get("userMailList");
		boolean bool;
		EayunResponseJson reJson = new EayunResponseJson();
		try {
			bool = mailService.sendMailByUser(mailId,userMailList);
			if(bool){
				reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
				reJson.setData(bool);
				ecmcLogService.addLog("重发邮件", "邮件管理", title, null, 1, mailId, null);
			}
		} catch (Exception e) {
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
			reJson.setData(e.getMessage());
			ecmcLogService.addLog("重发邮件", "邮件管理", title, null, 0, mailId, e);
			throw e;
		}
		
		
		return JSONObject.toJSONString(reJson);
	}
}
