package com.eayun.versions.ecmccontroller;


import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.dubbo.common.json.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.sms.ecmccontroller.EcmcSmsController;
import com.eayun.versions.ecmcservice.EcmcVersionsService;


@Controller
@RequestMapping("/ecmc/system/versions")
public class EcmcVersionsController extends BaseController {
	private static final Logger log = LoggerFactory.getLogger(EcmcVersionsController.class);
	
		@Autowired 
		public EcmcVersionsService ecmcVersionsService; 
		@RequestMapping("/query")
		@ResponseBody
		public Object queryVersions(HttpServletRequest request){
			EayunResponseJson reJson = new EayunResponseJson();
			reJson.setData(ecmcVersionsService.queryVersions());
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			return reJson;
		}
		
	
	

}
