package com.eayun.obs.controller;

import java.util.Date;
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
import com.eayun.common.controller.BaseController;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.util.DateUtil;
import com.eayun.obs.model.ObsUsedType;
import com.eayun.obs.service.ObsUsedService;

@Controller
@RequestMapping("/obs/used")
public class ObsUsedController extends BaseController {
	private static final Logger log = LoggerFactory.getLogger(ObsUsedController.class);
	@Autowired
	private ObsUsedService obsUsedService;

	/**
	 * 用于对象存储用量统计
	 * @param request
	 * @param map
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
    @RequestMapping(value = "/getObsResources", method = RequestMethod.POST)
	@ResponseBody
	public String getObsResources(HttpServletRequest request,@RequestBody Map map) throws Exception {
		log.info("获取对象存储统计资源列表");
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		String cusId=sessionUser.getCusId();
		String start = map.get("startTime").toString();
		String end = map.get("endTime").toString();
		Date startTime = DateUtil.timestampToDate(start);
		Date endTime = DateUtil.timestampToDate(end);
		List<ObsUsedType> typeList = obsUsedService.getObsUsedList(startTime, endTime,cusId);
		return JSONObject.toJSONString(typeList);
	}

}
