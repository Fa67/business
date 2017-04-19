package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.SwiftObject;
import com.eayun.eayunstack.service.OpenstackSwiftService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;

@Service
public class OpenstackSwiftServiceImpl extends OpenstackBaseServiceImpl<SwiftObject>implements OpenstackSwiftService {

	@Override
	public List<SwiftObject> listAll(String datacenterId) throws AppException {
		return null;
	}

	@Override
	public List<SwiftObject> list(String datacenterId, String projectId) throws AppException {
		return null;
	}

	@Override
	public SwiftObject getById(String datacenterId, String projectId, String id) throws AppException {
		return null;
	}

	@Override
	public SwiftObject create(String datacenterId, String projectId, JSONObject data) throws AppException {
		return null;
	}

	@Override
	public boolean delete(String datacenterId, String projectId, String id) throws AppException {
		return false;
	}

	@Override
	public SwiftObject update(String datacenterId, String projectId, JSONObject data, String id) throws AppException {
		return null;
	}

	
	/**
	 * <p>获取token用于下载Swift上的日志</p>
	 * ----------------------
	 * @author zhouhaitao
	 * @param dcId				数据中心ID
	 * @param prjId				项目ID
	 * @return
	 * @throws Exception 
	 */
	@Override
	public String download(String dcId,String prjId) throws Exception{
		RestTokenBean restTokenBean = getRestTokenBean(dcId, prjId,
				OpenstackUriConstant.SWIFT_SERVICE_URI);
		
		RestTokenBean token = restService.getToken(restTokenBean);
		return token.getEndpoint();
	}
	
	
	/**
	 * <p>查询实例的指定类型的RDS的日志文件列表</p>
	 * --------------------------------------
	 * @author zhouhaitao
	 * 
	 * @param dcId				数据中心ID
	 * @param prjId				项目ID
	 * @param rdsInstanceIs		RDS实例ID
	 * @param type				日志类型
	 * @param fileCount			文件个数
	 * @return
	 */
	public List<SwiftObject> list(String dcId,String prjId,String rdsInstanceIs,String type,int fileCount){
		List<SwiftObject> list = new ArrayList<SwiftObject>();
		RestTokenBean restTokenBean = getRestTokenBean(dcId, prjId,
				OpenstackUriConstant.SWIFT_SERVICE_URI);
		restTokenBean.setUrl(ConstantClazz.RDS_LOG_SWIFT_CONTAINER_PREFIX+prjId+
				"?limit="+(fileCount+ConstantClazz.SYNC_MAX_COUNT)+"&format=json&prefix="+rdsInstanceIs+"/"+ConstantClazz.RDS_DATASTORE_MYSQL+"-"+type+"/");
		JSONArray jsonArray = restService.getJsonArray(restTokenBean);
		for(Object obj : jsonArray){
			JSONObject json = JSONObject.parseObject(obj.toString());
			SwiftObject obs = restService.json2bean(json, SwiftObject.class);
			
			list.add(obs);
		}
		return list;
	}
	
	/**
	 * <p>修改Container的meta信息</p>
	 * -----------------------
	 * @author zhouhaitao
	 * 
	 * @param dcId				数据中心ID
	 * @param prjId				项目ID
	 * @param container			Container名称
	 * @param data				参数信息
	 */
	public void update(String dcId,String prjId,String container ,JSONObject data){
		RestTokenBean restTokenBean = getRestTokenBean(dcId, prjId,
				OpenstackUriConstant.SWIFT_SERVICE_URI);
		restTokenBean.setUrl(container);
		restService.create(restTokenBean, null, data);
	}
	
	/**
	 * <p>删除Container或者Object</p>
	 * --------------------
	 * @author zhouhaitao
	 * 
	 * @param dcId				数据中心ID
	 * @param prjId				项目ID
	 * @param container			Container名称或者Object名称
	 */
	public void deleteContainer(String dcId,String prjId,String container){
		RestTokenBean restTokenBean = getRestTokenBean(dcId, prjId,
				OpenstackUriConstant.SWIFT_SERVICE_URI);
		restTokenBean.setUrl("/"+container);
		restService.delete(restTokenBean);
	}
}
