package com.eayun.database.log.controller;

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
import com.eayun.database.log.model.CloudRDSLog;
import com.eayun.database.log.service.RDSLogService;
import com.eayun.log.service.LogService;

/**
 * <p>RDS 实例的日志</p>
 * @author zhouhaitao
 *
 */

@Controller
@RequestMapping("/rds/log")
@Scope("prototype")
public class RDSLogController {
	private static final Logger log = LoggerFactory.getLogger(RDSLogController.class);
	@Autowired
	private RDSLogService rdsLogService;
	@Autowired
    private LogService logService;
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
	@RequestMapping(value= "/getloglist" , method = RequestMethod.POST)
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
    		
    		page = rdsLogService.getLogByInstance(page,map,queryMap);
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
			rdsLogService.publishLog(rdsLog,false);
			json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
			logService.addLog("发布日志", ConstantClazz.LOG_TYPE_RDS,rdsLog.getRdsName(),rdsLog.getPrjId(),  
        			ConstantClazz.LOG_STATU_SUCCESS,null);
		}catch(Exception e){
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
			logService.addLog("发布日志", ConstantClazz.LOG_TYPE_RDS,rdsLog.getRdsName(),rdsLog.getPrjId(),  
        			ConstantClazz.LOG_STATU_ERROR,e);
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
			String url = rdsLogService.download(rdsLog);
			json.put("data", url);
			json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
			logService.addLog("下载日志", ConstantClazz.LOG_TYPE_RDS,rdsLog.getRdsName(),rdsLog.getPrjId(),  
        			ConstantClazz.LOG_STATU_SUCCESS,null);
		}catch(Exception e){
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
			logService.addLog("下载日志", ConstantClazz.LOG_TYPE_RDS,rdsLog.getRdsName(),rdsLog.getPrjId(),  
        			ConstantClazz.LOG_STATU_ERROR,e);
			throw e;
		}
		
		return json.toJSONString();
	}
	
	/**
	 * <p>查询该数据库实例的日志发布状态</p>
	 * -------------------
	 * @author zhouhaitao
	 * 
	 * @param request
	 * @param rdsId
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value= "/checkLogpublishing")
	@ResponseBody
	public String checkLogpublishing(HttpServletRequest request,@RequestBody String rdsId) throws Exception{
		log.info("查询该数据库实例的日志发布状态");
		JSONObject json = new JSONObject();
		try{
			boolean  flag = rdsLogService.checkRdsInstancePublishing(rdsId);
			json.put("data", flag);
		}catch(Exception e){
			json.put("respCode", ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
			throw e;
		}
		
		return json.toJSONString();
	}
	
}
