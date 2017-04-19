package com.eayun.ecmcschedule.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eayun.common.dao.ParamsMap;
import com.eayun.ecmcschedule.service.ScheduleLostJobListenerService;

@Controller
@RequestMapping("/ecmc/system/schedule")
@Scope("prototype")
public class ScheduleLostJobListenerController {
	
	@Autowired
	private ScheduleLostJobListenerService scheduleLostJobListenerService ;
	private static final Logger log = LoggerFactory.getLogger(ScheduleLostJobListenerController.class);
	/**
	 * 查询保存在Mongodb数据库中的计划任务漏跑信息,ECMC前端展示
	 * @param requstMap
	 * @return	返回结果集
	 * @throws Exception
	 */
	@RequestMapping(value = "/getLeakageTaskList", method = RequestMethod.POST)
	@ResponseBody
	public Object getTaskList(@RequestBody ParamsMap paramsMap) throws Exception {
		log.info("ECMC查询全部计划任务漏跑信息列表");
		return this.scheduleLostJobListenerService.getTaskList(paramsMap) ;
	}
}