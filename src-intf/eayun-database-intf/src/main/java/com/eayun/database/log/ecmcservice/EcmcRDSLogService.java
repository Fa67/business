package com.eayun.database.log.ecmcservice;

import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.database.log.model.CloudRDSLog;

public interface EcmcRDSLogService {

	/**
	 * <p>查询RDS实例储存于OBS上的的某一类型Trove日志</p>
	 * -----------------------------------
	 * 
	 * @author zhouhaitao
	 * @param page 			page 分页结果集	
	 * @param map 			实例日志（{dcId,prjId,rdsInstanceId}）
	 * @param queryMap 		分页信息	
	 * @return 
	 */
	public Page getLogByInstance(Page page,ParamsMap map,QueryMap queryMap);
	
	
	/**
	 * <p>发布RDS实例的某一类型的Trove日志</p>
	 * -----------------------------------
	 * 
	 * @author zhouhaitao
	 * @param rdsLog 		RDS实例日志	
	 * @param isSyncAll 	是否同步所有类型的日志
	 */
	public void publishLog(CloudRDSLog rdsLog,boolean isSyncAll);
	
	/**
	 * <p>下载某一个OBS上的日志文件</p>
	 * -----------------------------------
	 * 
	 * @author zhouhaitao
	 * @param rdsLog 		RDS实例日志	
	 * @throws Exception 
	 */
	public String download(CloudRDSLog rdsLog) throws Exception;
	
	/**
	 * <p>校验日志是否发布中</p>
	 * @author zhouhaitao
	 * ----------------------------
	 * @param rdsInstanceId
	 * @return
	 */
	public boolean checkRdsInstancePublishing(String rdsInstanceId);
}
