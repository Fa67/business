package com.eayun.database.log.service;

import java.util.List;

import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.database.log.model.CloudRDSLog;

public interface RDSLogService {
	
	/**
	 * <p>启用SYS类型的MySQL的日志（用户实例创建成功时）</p>
	 * -----------------------------------------------
	 * @author zhouhaitao
	 * 
	 * @param dcId				数据中心ID
	 * @param prjId				项目ID
	 * @param rdsInstanceId		RDS实例ID
	 */
	public void enableLog(String dcId,String prjId,String rdsInstanceId);
	
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
	 * 
	 * @return 
	 */
	public String download(CloudRDSLog rdsLog) throws Exception;
	
	
	/**
	 * <p>设置Trove日志的Container的访问权限</p>
	 * ---------------------------
	 * @author zhouhaitao
	 * 
	 * @param dcId				数据中心ID
	 * @param prjId				项目ID
	 */
	public void modifyReadAcl(String dcId,String prjId);
	
	/**
	 * <p>根据发布状态查询RDS实例列表</p>
	 * -------------------------------
	 * @author zhouhaitao
	 * 
	 * @param isPublishing   false发布完成     true发布中
	 * @return
	 * 
	 */
	public List <CloudRDSInstance> queryRdsInstanceForPublish(boolean isPublishing);
	
	/**
	 * <p>修改RDS实例的日志发布状态</p>
	 * -----------------------------------
	 * @author zhouhaitao
	 * @param rdsInstanceId				RDS实例ID
	 * @param logType					RDS日志类型
	 */
	public void modifyInstancePublishState(String rdsInstanceId,String logType);
	
	/**
	 * <p>校验rds同步状态</p>
	 * --------------------
	 * @author zhouhaitao
	 * @param rdsLog
	 */
	public boolean checkRdsLogPublish(CloudRDSLog rdsLog);
	
	/**
	 * <p>同步RDS 日志状态信息</p>
	 * --------------------
	 * @author zhouhaitao
	 * @param rdsLog
	 */
	public void syncLog(CloudRDSLog rdsLog);
	
	/**
	 * <p>同步RDS 日志状态信息</p>
	 * --------------------
	 * @author zhouhaitao
	 * @param rdsId			实例ID
	 */
	public boolean checkRdsInstancePublishing(String rdsId);
}
