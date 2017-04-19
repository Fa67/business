package com.eayun.sms.ecmccontroller;

import java.util.Date;
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
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.DateUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.sms.ecmcservice.EcmcSmsService;
import com.eayun.sms.model.SMS;

@Controller
@RequestMapping("/ecmc/system/sms")
public class EcmcSmsController extends BaseController {
	private static final Logger log = LoggerFactory
			.getLogger(EcmcSmsController.class);

	@Autowired
	private EcmcSmsService smsService;
	@Autowired
	private EcmcLogService ecmcLogService;

	@RequestMapping(value = "/getsmslist", method = RequestMethod.POST)
	@ResponseBody
	public String getSmsList(HttpServletRequest request, Page page,
			@RequestBody ParamsMap map) throws Exception {
		try {
			String begin = map.getParams().get("begin").toString();
			Date beginTime = DateUtil.timestampToDate(begin);
			String end = map.getParams().get("end").toString();
			Date endTime = DateUtil.timestampToDate(end);
			String mobile = map.getParams().get("mobile").toString();
			String status = map.getParams().get("status").toString();

			int pageSize = map.getPageSize();
			int pageNumber = map.getPageNumber();
			QueryMap queryMap = new QueryMap();
			queryMap.setPageNum(pageNumber);
			queryMap.setCURRENT_ROWS_SIZE(pageSize);

			page = smsService.getSmsList(page, beginTime, endTime, mobile,
					status, queryMap);
		} catch (Exception e) {
			throw e;
		}
		return JSONObject.toJSONString(page);
	}

	@RequestMapping(value = "/createsms", method = RequestMethod.POST)
	@ResponseBody
	public Object createSms(HttpServletRequest request, @RequestBody SMS sms)
			throws Exception {
		EayunResponseJson respJson = new EayunResponseJson();
		try {
			boolean createFlag = smsService.createSms(sms);
			respJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			respJson.setData(createFlag);
			ecmcLogService.addLog("创建短信", "短信管理", "", null, 1, "", null);
		} catch (Exception e) {
			respJson.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("创建短信", "短信管理", "", null, 0, "", e);
			throw e;
		}
		return respJson;
	}

	@RequestMapping(value = "/resendsms", method = RequestMethod.POST)
	@ResponseBody
	public Object resendSms(HttpServletRequest request,
			@RequestBody Map<String, String> map) throws Exception {
	    log.info("重发短信");
		EayunResponseJson respJson = new EayunResponseJson();
		String id = map.get("id");
		try {
			boolean flag = smsService.resendSms(id);
			respJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			respJson.setData(flag);
			ecmcLogService.addLog("重发短信", "短信管理", "", null, 1, id, null);
		} catch (Exception e) {
			respJson.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("重发短信", "短信管理", "", null, 0, id, e);
			throw e;
		}
		return respJson;
	}
}
