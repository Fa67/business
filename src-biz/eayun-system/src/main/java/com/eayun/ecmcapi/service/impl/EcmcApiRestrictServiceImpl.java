package com.eayun.ecmcapi.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.redis.JedisUtil;
import com.eayun.customer.ecmcservice.ApiRestrictService;
import com.eayun.customer.model.ApiCountRestrict;
import com.eayun.ecmcapi.dao.ApiDefaultCountDao;
import com.eayun.ecmcapi.model.ApiDefaultCount;
import com.eayun.ecmcapi.model.BaseApiDefaultCount;
import com.eayun.ecmcapi.service.EcmcApiRestrictService;
@Service
@Transactional
public class EcmcApiRestrictServiceImpl implements EcmcApiRestrictService {
	private static final Logger log = LoggerFactory
			.getLogger(EcmcApiRestrictServiceImpl.class);
	@Autowired
	private ApiDefaultCountDao apiDefaultCountDao;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private ApiRestrictService apiRestrictService;
	@Override
	public void syncApiCount() throws Exception {
		//同步默认访问限制
		syncDefaultApiCount();
		//同步非默认访问限制
		syncNoDefaultApiCount();
	}

	private void syncNoDefaultApiCount() throws Exception{
		Set<String> keys=jedisUtil.keys(RedisKey.API_REQUEST_COUNT+"*");
		for (String key : keys) {
			jedisUtil.delete(key);
		}
		List<ApiCountRestrict> list=apiRestrictService.getAllApiRestrict();
		for (ApiCountRestrict apiCountRestrict : list) {
			jedisUtil.set(RedisKey.API_REQUEST_COUNT+apiCountRestrict.getCusId()+":"+apiCountRestrict.getVersion()+":"+apiCountRestrict.getAction(), JSONObject.toJSONString(apiCountRestrict));
		}
	}

	private void syncDefaultApiCount() throws Exception{
		Set<String> keys=jedisUtil.keys(RedisKey.API_REQUEST_COUNT_DEFAULT+"*");
		for (String key : keys) {
			jedisUtil.delete(key);
		}
		List<ApiCountRestrict> apiTypes=apiRestrictService.getApiType();
		for (ApiCountRestrict apiCountRestrict : apiTypes) {
			List<Object> params=new ArrayList<Object>();
			StringBuffer hql = genSql(params,apiCountRestrict.getVersion(),apiCountRestrict.getApiType());
			Query query=apiDefaultCountDao.createSQLNativeQuery(hql.toString(), params.toArray());
			List<Object[]> list=query.getResultList();
			for (int i=0;i<list.size();i++) {
				Object[] objs=list.get(i);
				ApiDefaultCount apiDefaultCount=new ApiDefaultCount();
				String action=objs[5]==null?null:String.valueOf(objs[5]);
				apiDefaultCount.setAction(action);
				String actionName=objs[2]==null?null:String.valueOf(objs[2]);
				apiDefaultCount.setActionName(actionName);
				String apiTypeStr=objs[4]==null?null:String.valueOf(objs[4]);
				apiDefaultCount.setApiType(apiTypeStr);
				String apiTypeName=objs[1]==null?null:String.valueOf(objs[1]);
				apiDefaultCount.setApiTypeName(apiTypeName);
				String count=objs[3]==null?"0":String.valueOf(objs[3]);
				apiDefaultCount.setCount(Integer.valueOf(count));
				String versionStr=objs[0]==null?null:String.valueOf(objs[0]);
				apiDefaultCount.setVersion(versionStr);
				String id=objs[6]==null?null:String.valueOf(objs[6]);
				apiDefaultCount.setId(id);
				jedisUtil.set(RedisKey.API_REQUEST_COUNT_DEFAULT+apiDefaultCount.getVersion()+":"+apiDefaultCount.getAction(), JSONObject.toJSONString(apiDefaultCount));
				jedisUtil.addToSortedSet(RedisKey.API_REQUEST_COUNT_DEFAULT+apiDefaultCount.getVersion()+":"+apiDefaultCount.getApiType(),i, JSONObject.toJSONString(apiDefaultCount));
			}
		}
	}

	@Override
	public Page getApiDefaultCount(String version,
			String apiType,QueryMap queryMap, Page page) throws Exception {
		List<Object> params=new ArrayList<Object>();
		StringBuffer hql = genSql(params,version,apiType);
		page=apiDefaultCountDao.pagedNativeQuery(hql.toString(), queryMap, params.toArray());
		List resultList = (List) page.getResult();
		for (int i=0;i<resultList.size();i++) {
			Object[] objs = (Object[]) resultList.get(i);
			ApiDefaultCount apiDefaultCount=new ApiDefaultCount();
			String action=objs[5]==null?null:String.valueOf(objs[5]);
			apiDefaultCount.setAction(action);
			String actionName=objs[2]==null?null:String.valueOf(objs[2]);
			apiDefaultCount.setActionName(actionName);
			String apiTypeStr=objs[4]==null?null:String.valueOf(objs[4]);
			apiDefaultCount.setApiType(apiTypeStr);
			String apiTypeName=objs[1]==null?null:String.valueOf(objs[1]);
			apiDefaultCount.setApiTypeName(apiTypeName);
			String count=objs[3]==null?"0":String.valueOf(objs[3]);
			apiDefaultCount.setCount(Integer.valueOf(count));
			String versionStr=objs[0]==null?null:String.valueOf(objs[0]);
			apiDefaultCount.setVersion(versionStr);
			String id=objs[6]==null?null:String.valueOf(objs[6]);
			apiDefaultCount.setId(id);
			resultList.set(i, apiDefaultCount);
		}
		return page;
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
	public List<ApiDefaultCount> getApiDefaultCountList(String version,
			String apiType) throws Exception {
		List<Object> params=new ArrayList<Object>();
		StringBuffer hql = genSql(params,version,apiType);
		Query query=apiDefaultCountDao.createSQLNativeQuery(hql.toString(), params.toArray());
		List resultList = (List) query.getResultList();
		List<ApiDefaultCount> result=new ArrayList<ApiDefaultCount>();
		for (int i=0;i<resultList.size();i++) {
			Object[] objs = (Object[]) resultList.get(i);
			ApiDefaultCount apiDefaultCount=new ApiDefaultCount();
			String action=objs[5]==null?null:String.valueOf(objs[5]);
			apiDefaultCount.setAction(action);
			String actionName=objs[2]==null?null:String.valueOf(objs[2]);
			apiDefaultCount.setActionName(actionName);
			String apiTypeStr=objs[4]==null?null:String.valueOf(objs[4]);
			apiDefaultCount.setApiType(apiTypeStr);
			String apiTypeName=objs[1]==null?null:String.valueOf(objs[1]);
			apiDefaultCount.setApiTypeName(apiTypeName);
			String count=objs[3]==null?"0":String.valueOf(objs[3]);
			apiDefaultCount.setCount(Integer.valueOf(count));
			String versionStr=objs[0]==null?null:String.valueOf(objs[0]);
			apiDefaultCount.setVersion(versionStr);
			String id=objs[6]==null?null:String.valueOf(objs[6]);
			apiDefaultCount.setId(id);
			result.add(apiDefaultCount);
		}
		return result;
	}

	@Override
	public void updateApiDefaultCount(List<Map> actions,String version ,String apiType)
			throws Exception {
		if(actions!=null&&actions.size()>0){
			jedisUtil.delete(RedisKey.API_REQUEST_COUNT_DEFAULT+version+":"+apiType);
			for (int i= 0;i<actions.size();i++) {
				Map<String, Object> apiDefaultCount =actions.get(i);
				BaseApiDefaultCount baseApiDefaultCount =new BaseApiDefaultCount();
				baseApiDefaultCount.setAction(String.valueOf(apiDefaultCount.get("action")));
				baseApiDefaultCount.setActionName(String.valueOf(apiDefaultCount.get("actionName")));
				baseApiDefaultCount.setApiType(String.valueOf(apiDefaultCount.get("apiType")));
				baseApiDefaultCount.setApiTypeName(String.valueOf(apiDefaultCount.get("apiTypeName")));
				baseApiDefaultCount.setCount(Integer.valueOf(String.valueOf(apiDefaultCount.get("count"))));
				baseApiDefaultCount.setId(apiDefaultCount.get("id")==null?null:String.valueOf(apiDefaultCount.get("id")));
				baseApiDefaultCount.setVersion(String.valueOf(apiDefaultCount.get("version")));
				baseApiDefaultCount=apiDefaultCountDao.save(baseApiDefaultCount);
				jedisUtil.set(RedisKey.API_REQUEST_COUNT_DEFAULT+String.valueOf(apiDefaultCount.get("version"))+":"+String.valueOf(apiDefaultCount.get("action")), JSONObject.toJSONString(baseApiDefaultCount));
				jedisUtil.addToSortedSet(RedisKey.API_REQUEST_COUNT_DEFAULT+baseApiDefaultCount.getVersion()+":"+baseApiDefaultCount.getApiType(),i, JSONObject.toJSONString(baseApiDefaultCount));
			}
		}
	}
}
