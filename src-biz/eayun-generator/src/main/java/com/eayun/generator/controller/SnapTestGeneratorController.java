package com.eayun.generator.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.generator.service.SnapTestGeneratorService;

@Controller
@RequestMapping("/snap/test")
public class SnapTestGeneratorController {

	private final static Logger log = LoggerFactory.getLogger(SnapTestGeneratorController.class);

	@Autowired
    private  SnapTestGeneratorService snapTestGeneratorService;
	
	@RequestMapping(value = "/createbatchsnap" , method = RequestMethod.GET)
    @ResponseBody
    public String  createBatchSnap(HttpServletRequest request){
    	EayunResponseJson json = new EayunResponseJson();
    	try {
    		snapTestGeneratorService.createBatchSnap();
			
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			log.info("批量创建云硬盘备份成功");
    	} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
        	log.error("批量创建云硬盘备份失败",e);
        	throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
}
