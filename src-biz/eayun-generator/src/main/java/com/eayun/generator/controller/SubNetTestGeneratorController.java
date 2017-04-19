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
import com.eayun.generator.service.SubNetTestGeneratorService;

@Controller
@RequestMapping("/subnet/test")
public class SubNetTestGeneratorController {
	
	private final static Logger log = LoggerFactory.getLogger(SubNetTestGeneratorController.class);

	@Autowired
    private  SubNetTestGeneratorService subNetTestGeneratorService;
	
	@RequestMapping(value = "/createbatchsubnet" , method = RequestMethod.GET)
    @ResponseBody
    public String  createBatchSubnet(HttpServletRequest request){
    	EayunResponseJson json = new EayunResponseJson();
    	try {
    		subNetTestGeneratorService.createBatchSubnet();
			
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			log.info("批量创建私有网络子网成功");
    	} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
        	log.error("批量创建私有网络子网失败",e);
        	throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
}
