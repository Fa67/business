package com.eayun.eayunstack.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.RDSLog;
import com.eayun.eayunstack.service.OpenstackTroveLogService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;

@Service
public class OpenstackTroveLogServiceImpl extends OpenstackBaseServiceImpl<RDSLog>
		implements OpenstackTroveLogService {
	private static final Logger log = LoggerFactory.getLogger(OpenstackTroveLogServiceImpl.class);
	@Override
	public List<RDSLog> listAll(String datacenterId) throws AppException {
		return null;
	}

	@Override
	public List<RDSLog> list(String datacenterId, String projectId) throws AppException {
		return null;
	}

	@Override
	public RDSLog getById(String datacenterId, String projectId, String id) throws AppException {
		return null;
	}

	@Override
	public RDSLog create(String datacenterId, String projectId, JSONObject data) throws AppException {
		return null;
	}
	
	@Override
	public boolean delete(String datacenterId, String projectId, String id) throws AppException {
		return false;
	}

	@Override
	public RDSLog update(String datacenterId, String projectId, JSONObject data, String id) throws AppException {
		return null;
	}

	
	
	/**
	 * 启动日志<br>
	 * ------------------
	 * @author zhouhaitao
	 * 
	 * @param dcId				数据中心ID
	 * @param prjId				项目ID
	 * @param rdsInstanceId		RDS实例ID
	 * @param data				requestbody 参数
	 */
	public void  enableLog(String dcId,String prjId,String rdsInstanceId,JSONObject data){
		RestTokenBean restTokenBean = getRestTokenBean(dcId, prjId,
				OpenstackUriConstant.TROVE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.RDS_INSTANCES_URI+"/"+rdsInstanceId+"/log");
		restService.create(restTokenBean,null, data);
	}
	
	/**
	 * 发布某一类型的日志到Swift上<br>
	 * ------------------
	 * @author zhouhaitao
	 * @param dcId				数据中心ID
	 * @param prjId				项目ID
	 * @param rdsInstanceId		RDS实例ID
	 * @param data				requestbody 参数
	 */
	public void publishLog(String dcId,String prjId,String rdsInstanceId,JSONObject data){
		RestTokenBean restTokenBean = getRestTokenBean(dcId, prjId,
				OpenstackUriConstant.TROVE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.RDS_INSTANCES_URI+"/"+rdsInstanceId+"/log");
		restService.create(restTokenBean,null, data);
	}
	
	/**
	 * <p>删除Swift上已发布的指定类型的日志</p>
	 * ------------------
	 * @author zhouhaitao
	 * @param dcId				数据中心ID
	 * @param prjId				项目ID
	 * @param rdsInstanceId		RDS实例ID
	 * @param data				requestbody 参数
	 */
	public void discardLog(String dcId,String prjId,String rdsInstanceId,JSONObject data){
		RestTokenBean restTokenBean = getRestTokenBean(dcId, prjId,
				OpenstackUriConstant.TROVE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.RDS_INSTANCES_URI+"/"+rdsInstanceId+"/log");
		restService.create(restTokenBean,null, data);
	}
	
	/**
	 * <p>查询发布的指定类型的日志的状态</p>
	 * ------------------
	 * @author zhouhaitao
	 * @param dcId				数据中心ID
	 * @param prjId				项目ID
	 * @param rdsInstanceId		RDS实例ID
	 * @param type				 日志类型
	 */
	public JSONObject getPublishState(String dcId,String prjId,String rdsInstanceId,String type){
		RestTokenBean restTokenBean = getRestTokenBean(dcId, prjId,
				OpenstackUriConstant.TROVE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.RDS_INSTANCES_URI+"/"+rdsInstanceId+"/log/"+type);
		return restService.get(restTokenBean, OpenstackUriConstant.RDS_LOG_NAMES);
	}
	
}
