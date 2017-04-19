package com.eayun.virtualization.ecmccontroller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.virtualization.ecmcservice.EcmcTagService;


/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年5月4日
 */
@Controller
@RequestMapping("/ecmc/virtual/tag")
public class EcmcTagController {

	private static final Logger log = LoggerFactory.getLogger(EcmcTagController.class);
	@Autowired
    private EcmcTagService tagService;
	@RequestMapping(value = "/syncRedisWithDB.do")
    @ResponseBody
    public Object syncRedisWithDB(HttpServletRequest request) throws Exception {
        log.info("同步Redis和DB开始");
        EayunResponseJson reJson = new EayunResponseJson();
        String msg = tagService.synchronizeRedisWithDB();
        if("000000".equals(msg)){
        	reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        }else{
        	reJson.setRespCode(ConstantClazz.ERROR_CODE);
        	reJson.setMessage(msg);
        }
        return reJson;
    }
}
