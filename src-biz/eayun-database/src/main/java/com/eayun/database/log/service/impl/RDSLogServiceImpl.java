package com.eayun.database.log.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.database.log.baseservice.BaseRDSLogService;
import com.eayun.database.log.model.CloudRDSLog;
import com.eayun.database.log.service.RDSLogService;
import com.eayun.eayunstack.service.OpenstackTroveLogService;

@Service
public class RDSLogServiceImpl extends BaseRDSLogService implements RDSLogService{
	private static final Logger log = LoggerFactory.getLogger(RDSLogServiceImpl.class);
	@Autowired
	private OpenstackTroveLogService openstackTroveLogService;
	
	/**
	 * <p>启用SYS类型的MySQL的日志（用户实例创建成功时）</p>
	 * -----------------------------------------------
	 * @author zhouhaitao
	 * 
	 * @param dcId				数据中心ID
	 * @param prjId				项目ID
	 * @param rdsInstanceId		RDS实例ID
	 */
	public void enableLog(String dcId,String prjId,String rdsInstanceId){
		log.info("启用实例："+rdsInstanceId+"的SYS类型日志");
		JSONObject data = new JSONObject();
		//启用 general 类型的日志
		data.put("enable", 1);
		data.put("name", "general");
		openstackTroveLogService.enableLog(dcId,prjId,rdsInstanceId,data);
		
		//启用 slow_query 类型的日志
		data.put("name", "slow_query");
		openstackTroveLogService.enableLog(dcId,prjId,rdsInstanceId,data);
	}
	
	/**
	 * <p>校验rds同步状态</p>
	 * --------------------
	 * @author zhouhaitao
	 * @param rdsLog
	 */
	public boolean checkRdsLogPublish(CloudRDSLog rdsLog){
		JSONObject result = openstackTroveLogService.getPublishState(rdsLog.getDcId(),rdsLog.getPrjId(),
				rdsLog.getRdsInstanceId(),escapeType(rdsLog.getLogType()));
		
		rdsLog.setPublishFileCount(result.getInteger("files_published_attime"));
		return !result.getBooleanValue("is_publishing");
	}
	
	/**
	 * <p>同步RDS 日志状态信息</p>
	 * --------------------
	 * @author zhouhaitao
	 * @param rdsLog
	 */
	public void syncLog(CloudRDSLog rdsLog){
		if(ConstantClazz.RDS_LOG_TYPE_ALL.equals(rdsLog.getLogType())){
			rdsLog.setLogType(ConstantClazz.RDS_LOG_TYPE_DBLOG);
			boolean dbLogPublish = checkRdsLogPublish(rdsLog);
			int dbLogFileCount = rdsLog.getPublishFileCount();
			rdsLog.setLogType(ConstantClazz.RDS_LOG_TYPE_SLOWLOG);
			int slowLogFileCount = rdsLog.getPublishFileCount();
			boolean slowLogPublish = checkRdsLogPublish(rdsLog);
			rdsLog.setLogType(ConstantClazz.RDS_LOG_TYPE_ERRORLOG);
			boolean errorLogPublish = checkRdsLogPublish(rdsLog);
			int errorLogFileCount = rdsLog.getPublishFileCount();
			if(dbLogPublish && slowLogPublish && errorLogPublish){
				rdsLog.setLogType(ConstantClazz.RDS_LOG_TYPE_DBLOG);
				rdsLog.setPublishFileCount(dbLogFileCount);
				syncRdsLogFromSwift(rdsLog);
				
				rdsLog.setLogType(ConstantClazz.RDS_LOG_TYPE_SLOWLOG);
				rdsLog.setPublishFileCount(slowLogFileCount);
				syncRdsLogFromSwift(rdsLog);
				
				rdsLog.setLogType(ConstantClazz.RDS_LOG_TYPE_ERRORLOG);
				rdsLog.setPublishFileCount(errorLogFileCount);
				syncRdsLogFromSwift(rdsLog);
				
				modifyInstancePublishState(rdsLog.getRdsInstanceId(), null);
			}
		}
		else{
			if(checkRdsLogPublish(rdsLog)){
				syncRdsLogFromSwift(rdsLog);
				modifyInstancePublishState(rdsLog.getRdsInstanceId(), null);
			}
		}
	}
}
