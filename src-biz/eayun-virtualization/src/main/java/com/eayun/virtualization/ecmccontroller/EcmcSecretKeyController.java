package com.eayun.virtualization.ecmccontroller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.virtualization.ecmcservice.EcmcSecretKeyService;
import com.eayun.virtualization.ecmcvo.CloudSecretKeyVoe;

@Controller
@RequestMapping("/ecmc/virtual/secretkey")
@Scope("prototype")
public class EcmcSecretKeyController {
	private final static Logger log = LoggerFactory.getLogger(EcmcRouteController.class);

	@Autowired
	public EcmcSecretKeyService ecmcSecretKeyService;

	@RequestMapping("/getsecretkeylist")
	@ResponseBody
	public Object getsecretkeylist(@RequestBody Map<String, Object> requstMap) throws AppException {
		log.info("ecmc查询SSH列表开始");
		Map<String, Object> map = (Map<String, Object>) requstMap.get("params");
		String prjName = MapUtils.getString(map, "prjName");
		String dcid = MapUtils.getString(map, "dcId");
		String cusName = MapUtils.getString(map, "cusOrg");

		String queryName = MapUtils.getString(map, "name");
		int pageSize = MapUtils.getIntValue(requstMap, "pageSize");
		int pageNo = MapUtils.getIntValue(requstMap, "pageNumber");

		QueryMap queryMap = new QueryMap();
		if (pageNo == 0) {
			queryMap.setPageNum(1);
		} else {
			queryMap.setPageNum(pageNo);
		}
		if (pageSize == 0) {
			queryMap.setCURRENT_ROWS_SIZE(10);
		} else {
			queryMap.setCURRENT_ROWS_SIZE(pageSize);
		}

		Page page=ecmcSecretKeyService.getecscsecretkeylist(queryMap, prjName, dcid, cusName, queryName);

		return page;

	}

	@RequestMapping("/getsecretkeybyid")
	@ResponseBody
	public Object getSecretKeyById(@RequestBody Map<String, Object> requstMap){
		log.info("ecmc查询SSH详情开始");
		String skid = MapUtils.getString(requstMap, "skid");
		List<CloudSecretKeyVoe> voe =new ArrayList<>();
		voe.add(ecmcSecretKeyService.getSrcretKeyById(skid));
		return voe;
		
	}
	

	@RequestMapping("/getsecretkeyByIdAndVmList")
	@ResponseBody
	public Object getsecretkeyByIdAndVmList(@RequestBody Map<String, Object> requstMap){
		log.info("ecmc查询SSH详情云主机列表开始");
		Map<String, Object> map = (Map<String, Object>) requstMap.get("params");
		String skid = MapUtils.getString(map, "skid");
		int pageSize = MapUtils.getIntValue(requstMap, "pageSize");
		pageSize=5;
		int pageNo = MapUtils.getIntValue(requstMap, "pageNumber");
		QueryMap queryMap = new QueryMap();
		if (pageNo == 0) {
			queryMap.setPageNum(1);
		} else {
			queryMap.setPageNum(pageNo);
		}
		if (pageSize == 0) {
			queryMap.setCURRENT_ROWS_SIZE(5);
		} else {
			queryMap.setCURRENT_ROWS_SIZE(pageSize);
		}
		
		return ecmcSecretKeyService.getSrcretKeyByIdAndVmList(skid,queryMap);
		
	}
	
	
	
}
