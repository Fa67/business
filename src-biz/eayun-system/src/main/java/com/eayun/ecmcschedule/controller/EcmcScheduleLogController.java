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
import com.eayun.ecmcschedule.service.EcmcScheduleLogService;

@Controller
@RequestMapping("/ecmc/system/schedule/log")
@Scope("prototype")
public class EcmcScheduleLogController {

	@Autowired
	private EcmcScheduleLogService ecmcScheduleLogService;

	@RequestMapping(value = "/getloglist")
	@ResponseBody
	public Object getLogList(@RequestBody ParamsMap paramsMap) {
		try {
			QueryMap queryMap = new QueryMap();
			Map<String, Object> params = paramsMap.getParams();
			queryMap.setPageNum(paramsMap.getPageNumber() == null ? 1 : paramsMap.getPageNumber());
			if (paramsMap.getPageSize() != null) {
				queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
			}
			String triggerName = MapUtils.getString(params, "triggerName");
			String jobName = MapUtils.getString(params, "jobName");
			String queryStr = MapUtils.getString(params, "queryStr");
			String startTime = MapUtils.getString(params, "startTime");
			String endTime = MapUtils.getString(params, "endTime");
			String isSuccess = MapUtils.getString(params, "isSuccess");
			return ecmcScheduleLogService.getLogList(triggerName, jobName, startTime, endTime, queryStr, isSuccess, queryMap);
		} catch (Exception e) {
			throw e;
		}
	}

}
