package com.eayun.customer.ecmcservice.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.StringUtil;
import com.eayun.customer.dao.ApiCountRestrictDao;
import com.eayun.customer.ecmcservice.ApiRestrictService;
import com.eayun.customer.model.ApiCountRestrict;
import com.eayun.customer.model.BaseApiCountRestrict;
import com.eayun.sys.model.SysDataTree;
@Service
@Transactional
public class ApiRestrictServiceImpl implements ApiRestrictService {
	private static final Logger log = LoggerFactory.getLogger(ApiRestrictServiceImpl.class);
	@Autowired
	private ApiCountRestrictDao apiCountRestrictDao;
	@Autowired
	private JedisUtil jedisUtil;

	@Override
	public List<ApiCountRestrict> getAllApiRestrict() throws Exception {
		Iterable<BaseApiCountRestrict> iterable=apiCountRestrictDao.findAll();
		List<ApiCountRestrict> result=new ArrayList<ApiCountRestrict>();
		for (BaseApiCountRestrict baseApiCountRestrict : iterable) {
			ApiCountRestrict apiCountRestrict =new ApiCountRestrict();
			BeanUtils.copyPropertiesByModel(apiCountRestrict, baseApiCountRestrict);
			result.add(apiCountRestrict);
		}
		return result;
	}

	@Override
	public List<ApiCountRestrict> getApiType() throws Exception {
		List<SysDataTree> versionList=DictUtil.getDataTreeByParentId("0016001");
		List<ApiCountRestrict> allTypes=new ArrayList<ApiCountRestrict>();
		for (SysDataTree version : versionList) {
			List<SysDataTree> apiTypeList=DictUtil.getDataTreeByParentId(version.getNodeId());
			for (SysDataTree apiType : apiTypeList) {
				ApiCountRestrict apiCountRestrict=new ApiCountRestrict();
				apiCountRestrict.setApiTypeName(apiType.getNodeName());		//云主机
				apiCountRestrict.setApiType(apiType.getNodeNameEn());		//instance
				apiCountRestrict.setVersion(version.getNodeName());			//v1
				allTypes.add(apiCountRestrict);
			}
		}
		return allTypes;
	}

	@Override
	public List<ApiCountRestrict> getRestrictRequestCount(String cusId,
			String version, String apiType) throws Exception {
		List<ApiCountRestrict> apiCountList=new ArrayList<ApiCountRestrict>();
		List<Object> params=new ArrayList<Object>();
		StringBuffer sb=new StringBuffer();
		sb.append(" from BaseApiCountRestrict ");
		sb.append(" where cusId=? ");
		params.add(cusId);
		sb.append(" and version=?");
		params.add(version);
		sb.append(" and apiType=?");
		params.add(apiType);
		List<BaseApiCountRestrict> baseApiCountRestrictList=apiCountRestrictDao.find(sb.toString(), params.toArray());
		if(baseApiCountRestrictList!=null&&baseApiCountRestrictList.size()>0){
			for (BaseApiCountRestrict baseApiCountRestrict : baseApiCountRestrictList) {
				ApiCountRestrict apiCountRestrict=new ApiCountRestrict();
				BeanUtils.copyPropertiesByModel(apiCountRestrict, baseApiCountRestrict);
				apiCountList.add(apiCountRestrict);
			}
			return apiCountList;
		}else{
			//该客户未自定义访问次数,采用系统默认的
			List<String> results=jedisUtil.getZSetByRange(RedisKey.API_REQUEST_COUNT_DEFAULT+version+":"+apiType,0,-1);
			if(results!=null&&results.size()>0){
				//设置过默认值
				for (String result : results) {
					if(!StringUtil.isEmpty(result)){
						JSONObject json=JSONObject.parseObject(result);
						ApiCountRestrict apiCountRestrict=new ApiCountRestrict();
						apiCountRestrict.setAction(json.getString("action"));
						apiCountRestrict.setActionName(json.getString("actionName"));
						apiCountRestrict.setApiType(json.getString("apiType"));
						apiCountRestrict.setApiTypeName(json.getString("apiTypeName"));
						apiCountRestrict.setCount(json.getIntValue("count"));
						apiCountRestrict.setCusId(cusId);
						apiCountRestrict.setVersion(json.getString("version"));
						apiCountList.add(apiCountRestrict);
					}
				}
			}else{
				//未设置过默认值
				List<Object> par=new ArrayList<Object>();
				StringBuffer hql=genSql(par, version, apiType);
				List<Object> result=apiCountRestrictDao.createSQLNativeQuery(hql.toString(), par.toArray()).getResultList();
				for (int i=0;i<result.size();i++) {
					Object[] objs = (Object[]) result.get(i);
					ApiCountRestrict apiCountRestrict=new ApiCountRestrict();
					String action=objs[5]==null?null:String.valueOf(objs[5]);
					apiCountRestrict.setAction(action);
					String actionName=objs[2]==null?null:String.valueOf(objs[2]);
					apiCountRestrict.setActionName(actionName);
					String apiTypeStr=objs[4]==null?null:String.valueOf(objs[4]);
					apiCountRestrict.setApiType(apiTypeStr);
					String apiTypeName=objs[1]==null?null:String.valueOf(objs[1]);
					apiCountRestrict.setApiTypeName(apiTypeName);
					String count=objs[3]==null?"0":String.valueOf(objs[3]);
					apiCountRestrict.setCount(Integer.valueOf(count));
					String versionStr=objs[0]==null?null:String.valueOf(objs[0]);
					apiCountRestrict.setVersion(versionStr);
					apiCountRestrict.setCusId(cusId);
					apiCountList.add(apiCountRestrict);
				}
			}
			return apiCountList;
		}
	}
	private StringBuffer genSql(List<Object> params,String version ,String apiType) {
		StringBuffer hql=new StringBuffer();
		hql.append(" SELECT t5.node_name AS VERSION,t4.apiTypeName,t4.actionName,t4.count,t4.apiType,t4.action,t4.ID ");
		hql.append(" FROM sys_data_tree t5 ");
		hql.append(" LEFT JOIN ");
		hql.append(" (SELECT t3.node_name AS apiTypeName, ");
		hql.append(" t3.parent_id AS parent, ");
		hql.append(" t2.node_name AS actionName, ");
		hql.append(" t2.dc_count COUNT, ");
		hql.append(" t3.node_name_en AS apiType, ");
		hql.append(" t2.node_name_en AS action, ");
		hql.append(" t2.dc_id AS ID ");
		hql.append(" FROM sys_data_tree t3 ");
		hql.append(" LEFT JOIN ");
		hql.append(" (SELECT tree.parent_id, ");
		hql.append(" tree.node_name, ");
		hql.append(" tree.node_name_en, ");
		hql.append(" api.dc_count, ");
		hql.append(" api.dc_id ");
		hql.append(" FROM sys_data_tree tree ");
		hql.append(" LEFT JOIN api_defaultcount api ON tree.node_name_en=api.dc_action  AND api.dc_version=?");
		hql.append(" WHERE tree.parent_id IN ");
		hql.append(" (SELECT node_id ");
		hql.append(" FROM sys_data_tree ");
		hql.append(" WHERE parent_id IN ");
		hql.append(" (SELECT t1.node_id ");
		hql.append(" FROM sys_data_tree t1 ");
		hql.append(" WHERE t1.parent_id='0016001' and t1.node_name_en=? ))) t2 ON t3.node_id=t2.parent_id ");
		hql.append(" WHERE t2.parent_id=t3.node_id) t4 ON t5.node_id=t4.parent ");
		hql.append(" WHERE t5.node_id=t4.parent and t4.apiType=?");
		params.add(version);
		params.add(version);
		params.add(apiType);
		return hql;
	}
	@Override
	public void updateRestrictRequestCount(List<Map<String, Object>> actions)
			throws Exception {
		for (Map<String, Object> apiCountRestrict : actions) {
			BaseApiCountRestrict baseApiCountRestrict =new BaseApiCountRestrict();
			baseApiCountRestrict.setAction(String.valueOf(apiCountRestrict.get("action")));
			baseApiCountRestrict.setActionName(String.valueOf(apiCountRestrict.get("actionName")));
			baseApiCountRestrict.setApiType(String.valueOf(apiCountRestrict.get("apiType")));
			baseApiCountRestrict.setApiTypeName(String.valueOf(apiCountRestrict.get("apiTypeName")));
			baseApiCountRestrict.setCount(Integer.valueOf(String.valueOf(apiCountRestrict.get("count"))));
			baseApiCountRestrict.setId(apiCountRestrict.get("id")==null?null:String.valueOf(apiCountRestrict.get("id")));
			baseApiCountRestrict.setCusId(String.valueOf(apiCountRestrict.get("cusId")));
			baseApiCountRestrict.setVersion(String.valueOf(apiCountRestrict.get("version")));
			apiCountRestrictDao.saveOrUpdate(baseApiCountRestrict);
			jedisUtil.set(RedisKey.API_REQUEST_COUNT+String.valueOf(apiCountRestrict.get("cusId"))+":"+String.valueOf(apiCountRestrict.get("version"))+":"+String.valueOf(apiCountRestrict.get("action")), JSONObject.toJSONString(baseApiCountRestrict));
		}
	}
}
