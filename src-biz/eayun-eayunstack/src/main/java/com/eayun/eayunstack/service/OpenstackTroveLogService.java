package com.eayun.eayunstack.service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.eayunstack.model.RDSLog;

public interface OpenstackTroveLogService extends OpenstackBaseService<RDSLog>{

	/**
	 * 启动日志<br>
	 * ------------------
	 * @author zhouhaitao
	 * 
	 * @param dcId				数据中心ID
	 * @param prjId				项目ID
	 * @param rdsInatanceId		RDS实例ID
	 * @param data				requestbody 参数
	 */
	public void  enableLog(String dcId,String prjId,String rdsInatanceId,JSONObject data);
	
	/**
	 * 发布某一类型的日志到Swift上<br>
	 * ------------------
	 * @author zhouhaitao
	 * @param dcId				数据中心ID
	 * @param prjId				项目ID
	 * @param rdsInstanceId		RDS实例ID
	 * @param data				requestbody 参数
	 */
	public void publishLog(String dcId,String prjId,String rdsInstanceId,JSONObject data);
	
	/**
	 * <p>删除Swift上已发布的指定类型的日志</p>
	 * ------------------
	 * @author zhouhaitao
	 * @param dcId				数据中心ID
	 * @param prjId				项目ID
	 * @param rdsInstanceId		RDS实例ID
	 * @param data				requestbody 参数
	 */
	public void discardLog(String dcId,String prjId,String rdsInstanceId,JSONObject data);
	
	/**
	 * <p>查询发布的指定类型的日志的状态</p>
	 * ------------------
	 * @author zhouhaitao
	 * @param dcId				数据中心ID
	 * @param prjId				项目ID
	 * @param rdsInstanceId		RDS实例ID
	 * @param type				 日志类型
	 */
	public JSONObject getPublishState(String dcId,String prjId,String rdsInstanceId,String type);
	
}
