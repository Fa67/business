package com.eayun.ecmcschedule.controller;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.ecmcschedule.service.EcmcScheduleStatisticsService;

@Controller
@RequestMapping("/ecmc/system/schedule/statistics")
@Scope("prototype")
public class EcmcScheduleStatisticsController {

	@Autowired
	private EcmcScheduleStatisticsService ecmcScheduleStatisticsService;
	
	@RequestMapping(value = "/getstatisticslist")
	@ResponseBody
	public Object getStatisticsList(@RequestBody ParamsMap paramsMap) {
		try {
			QueryMap queryMap = new QueryMap();
			Map<String, Object> params = paramsMap.getParams();
			queryMap.setPageNum(paramsMap.getPageNumber() == null ? 1 : paramsMap.getPageNumber());
			if (paramsMap.getPageSize() != null) {
				queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
			}
			String taskId = MapUtils.getString(params, "taskId");
			String startTime = MapUtils.getString(params, "startTime");
			String endTime = MapUtils.getString(params, "endTime");
			return ecmcScheduleStatisticsService.getByTriggerName(taskId, startTime, endTime, queryMap);
		} catch (Exception e) {
			throw e;
		}
	}
	
	@RequestMapping(value = "/getchartdata")
	@ResponseBody
	public Object getChartData(@RequestBody Map<String, Object> requstMap){
		try {
			String taskId = MapUtils.getString(requstMap, "taskId");
			String startTime = MapUtils.getString(requstMap, "startTime");
			String endTime = MapUtils.getString(requstMap, "endTime");
			return ecmcScheduleStatisticsService.getChartData(taskId, startTime, endTime);
		} catch (Exception e) {
			throw e;
		}
		
	}
}
