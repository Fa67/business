package com.eayun.virtualization.ecmccontroller;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.virtualization.ecmcservice.EcmcCloudOutIpService;

/**
 * @author jingang.liu@eayun.com to beijing
 * @date 2016年4月14日
 */
@Controller
@RequestMapping("/ecmc/virtual/cloudoutip")
public class EcmcCloudOutIpController {

	private static final Log log = LogFactory.getLog(EcmcCloudOutIpController.class);
	@Autowired
	private EcmcCloudOutIpService outipservice;
	
	@RequestMapping("/outiplist")
	@ResponseBody
	public Object getoutipList(Page page,@RequestBody ParamsMap parems) throws AppException{
		log.info("查询IP列表");
		String datacenterId = parems.getParams().get("datacenterId") == null ? null : parems.getParams().get("datacenterId").toString();
		String usestauts = parems.getParams().get("usestauts") == null ? null : parems.getParams().get("usestauts").toString();
		String distribution = parems.getParams().get("distribution") == null ? null : parems.getParams().get("distribution").toString();
		String ip = parems.getParams().get("ip") == null ? null : parems.getParams().get("ip").toString();
		String prjName = parems.getParams().get("prjName") == null ? null : parems.getParams().get("prjName").toString();
		String[] pns = null;
		if(prjName!=null && !"".equals(prjName)){
			pns = prjName.split(",");
		}
		String[] cuss = null;
		String cusName = parems.getParams().get("cusName") == null ? "" : parems.getParams().get("cusName").toString();
		if(cusName!=null && !"".equals(cusName)){
			cuss = cusName.split(",");
		}
		
		QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(parems.getPageNumber());
        queryMap.setCURRENT_ROWS_SIZE(parems.getPageSize());
        page = outipservice.list(page, datacenterId, usestauts, distribution, ip,pns,cuss, queryMap);
        return page;
	}
	@RequestMapping("/getOutip")
	@ResponseBody
	public Object getoutipOne(@RequestBody Map<String, String> parems) throws AppException{
		String id = parems.get("id") == null ? null : parems.get("id").toString();
		if(id==null || "".equals(id))return null;
		EayunResponseJson res = new EayunResponseJson();
		try {
			res.setRespCode(ConstantClazz.SUCCESS_CODE);
			res.setData(outipservice.queryByOne(id));
		} catch (Exception e) {
		    log.error(e.toString(),e);
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setMessage("ecmc.virtual.cloudoutip:"+e.getMessage());
		}
		return res;
	}
}
