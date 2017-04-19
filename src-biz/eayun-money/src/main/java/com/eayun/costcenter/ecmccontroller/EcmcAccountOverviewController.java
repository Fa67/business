package com.eayun.costcenter.ecmccontroller;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.costcenter.model.MoneyAccount;
import com.eayun.costcenter.service.AccountOverviewService;
@Controller
@RequestMapping("/ecmc/costcenter/accountoverview")
public class EcmcAccountOverviewController extends BaseController {
	
	
	private static final Logger log = LoggerFactory.getLogger(EcmcAccountOverviewController.class);
	@Autowired
	private AccountOverviewService accountOverviewSerivce;
	
	@ResponseBody
	@RequestMapping("/getaccountbalance")
	public String getAcckListPage(@RequestBody Map<String, String> map) throws Exception{
		log.info("开始获取账户余额");
		String cusId = MapUtils.getString(map, "cusId");
		EayunResponseJson json = new EayunResponseJson();
		try {
			MoneyAccount accountMoney=accountOverviewSerivce.getAccountBalance(cusId);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			json.setData(accountMoney);
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.toString());
		}
	
		return JSONObject.toJSONString(json);
	}

}
