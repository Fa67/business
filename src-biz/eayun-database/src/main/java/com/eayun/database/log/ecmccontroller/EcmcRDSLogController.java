package com.eayun.database.log.ecmccontroller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.database.log.ecmcservice.EcmcRDSLogService;
import com.eayun.database.log.model.CloudRDSLog;


/**
 * <p>ECMC 管理控制台</p>
 * @author zhouhaitao
 *
 */
@Controller
@RequestMapping("/ecmc/rds/log")
@Scope("prototype")
public class EcmcRDSLogController {
	private static final Logger log = LoggerFactory.getLogger(EcmcRDSLogController.class);
	
	@Autowired
	private EcmcRDSLogService ecmcRDSLogService;
	
	/**
	 * <p>查询指定实例下指定类型的所有日志文件</p>
	 * -------------------------
	 * @author zhouhaitao
	 * 
	 * @param request
	 * @param rdsLog
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value= "/getlist" , method = RequestMethod.POST)
    @ResponseBody
    public String getLogList(HttpServletRequest request,Page page,@RequestBody ParamsMap map) throws Exception{
		JSONObject json = new JSONObject();
		log.info("查询RDS实例的日志");
		try{
			int pageSize = map.getPageSize();
    		int pageNumber = map.getPageNumber();
    		
    		QueryMap queryMap=new QueryMap();
    		queryMap.setPageNum(pageNumber);
    		queryMap.setCURRENT_ROWS_SIZE(pageSize);
    		
    		page = ecmcRDSLogService.getLogByInstance(page,map,queryMap);
		}catch(Exception e){
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
			throw e;
		}
		return JSONObject.toJSONString(page);
	}
	
	/**
	 * <p>发布指定类型的RDS实例的日志到OBS上</p>
	 * ------------------------------
	 * @author zhouhaitao
	 * 
	 * @param request
	 * @param rdsLog
	 * @return
	 */
	@RequestMapping(value= "/publish" , method = RequestMethod.POST)
    @ResponseBody
	public String publish(HttpServletRequest request,@RequestBody CloudRDSLog rdsLog){
		log.info("发布RDS实例的日志");
		JSONObject json = new JSONObject();
		try{
			ecmcRDSLogService.publishLog(rdsLog,false);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
		}catch(Exception e){
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
			throw e;
		}
		
		return json.toJSONString();
	}
	
	/**
	 * <p>下载RDS实例的日志</p>
	 * -------------------
	 * @author zhouhaitao
	 * 
	 * @param request
	 * @param rdsLog
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value= "/download")
    @ResponseBody
	public String download(HttpServletRequest request,@RequestBody CloudRDSLog rdsLog) throws Exception{
		log.info("查询OBS对象存储的domain信息");
		JSONObject json = new JSONObject();
		try{
			String url = ecmcRDSLogService.download(rdsLog);
			json.put("data", url);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
		}catch(Exception e){
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
			throw e;
		}
		return json.toJSONString();
	}
	
	/**
	 * <p>下载RDS实例的日志</p>
	 * -------------------
	 * @author zhouhaitao
	 * 
	 * @param request
	 * @param rdsLog
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value= "/checklogpublishstate")
	@ResponseBody
	public String checkLogPublishState(HttpServletRequest request,@RequestBody String rdsId) throws Exception{
		log.info("查询该数据库实例的日志发布状态");
		JSONObject json = new JSONObject();
		try{
			boolean flag = ecmcRDSLogService.checkRdsInstancePublishing(rdsId);
			json.put("data", flag);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
		}catch(Exception e){
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
			throw e;
		}
		return json.toJSONString();
	}
}
