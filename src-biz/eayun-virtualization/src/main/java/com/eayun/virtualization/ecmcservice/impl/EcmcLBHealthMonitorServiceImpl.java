/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.virtualization.ecmcservice.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceSyncConstant;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.CompletionHook;
import com.eayun.eayunstack.model.HealthMonitor;
import com.eayun.eayunstack.service.OpenstackHealthMonitorService;
import com.eayun.eayunstack.service.OpenstackPoolService;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.monitor.ecmcservice.EcmcAlarmService;
import com.eayun.monitor.service.AlarmService;
import com.eayun.virtualization.dao.CloudLdMonitorDao;
import com.eayun.virtualization.dao.CloudLdPoolMonitorDao;
import com.eayun.virtualization.ecmcservice.EcmcLBHealthMonitorService;
import com.eayun.virtualization.ecmcservice.EcmcLBMemberService;
import com.eayun.virtualization.model.BaseCloudLdMonitor;
import com.eayun.virtualization.model.BaseCloudLdPoolMonitor;
import com.eayun.virtualization.model.CloudLdMonitor;
import com.eayun.virtualization.model.CloudLdPool;
import com.eayun.virtualization.service.TagService;

/**
 *
 * @Filename: EcmcLBHealthMonitorController.java
 * @Description:
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2016年4月8日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Service
@Transactional
public class EcmcLBHealthMonitorServiceImpl implements EcmcLBHealthMonitorService {

    private final static Logger           log = LoggerFactory.getLogger(EcmcLBHealthMonitorServiceImpl.class);

    @Autowired
    private CloudLdMonitorDao             cloudLdMonitorDao;

    @Autowired
    private CloudLdPoolMonitorDao         cloudLdPoolMonitorDao;

    @Autowired
    private OpenstackHealthMonitorService openstackHealthMonitorService;

    @Autowired
    private OpenstackPoolService          openstackPoolService;

    @Autowired
    private TagService                    tagService;
    
    @Autowired
    private EcmcLogService ecmcLogService;
    
    @Autowired
    private JedisUtil jedisUtil ;
    
    @Autowired
    private EcmcAlarmService ecmcAlarmService;
    
    @Autowired
    private EcmcLBMemberService ecmcLBMemberService;

    public Page listMonitor(ParamsMap paramsMap) throws AppException {
    	List<Object> paramValues = new ArrayList<>(4);
        Map<String, Object> params = paramsMap.getParams();
        StringBuffer sql = new StringBuffer();
        sql.append("select cl.ldm_id as ldmId,cl.ldm_type as ldmType,cl.admin_stateup as adminStateup,cl.create_time as createTime,");
        sql.append(" cl.ldm_timeout as ldmTimeout,cl.max_retries as maxRetries,cl.ldm_delay as ldmDelay,cl.prj_id as prjId,cp.prj_name as prjName,");
        sql.append(" dc.dc_name as dcName,count(cldp.pool_name) as poolNum,cl.dc_id as dcId ");
        sql.append(", ss.cus_id as cusId, ss.cus_org as cusOrg, cl.ldm_name as ldmName,cl.url_path as urlPath");
        sql.append(" from cloud_ldmonitor as cl ");
        sql.append(" left join cloud_project as cp on cl.prj_id = cp.prj_id ");
        sql.append(" left join dc_datacenter as dc on dc.id=cl.dc_id");
        sql.append(" left join sys_selfcustomer as ss on cp.customer_id=ss.cus_id");
        sql.append(" left join ( select cldm.ldm_id as ldm_id ,cldm.pool_id as pool_id ,ldp.pool_name as pool_name from cloud_ldpoolldmonitor as cldm LEFT JOIN cloud_ldpool as ldp on ldp.pool_id=cldm.pool_id ) as cldp on cl.ldm_id=cldp.ldm_id");
        sql.append(" where 1=1 ");
        int idx = 0;
        //数据中心
        if (params != null && params.containsKey("dcId") && StringUtils.isNotBlank((String)params.get("dcId"))) {
            sql.append(" and cl.dc_id = ?").append(++idx);
            paramValues.add(params.get("dcId"));
        }

        //项目名称
        if (params != null && params.containsKey("prjName") && StringUtils.isNotBlank((String)params.get("prjName"))) {
            sql.append(" and cp.prj_name in(?").append(++idx).append(")");
            List tmpList = Arrays.asList(StringUtils.split((String)params.get("prjName"),","));//passing list to IN clause in HQL in this way
            paramValues.add(tmpList);
        }
        //客户名称
        if (params != null && params.containsKey("cusOrg") && StringUtils.isNotBlank((String)params.get("cusOrg"))) {
            sql.append(" and ss.cus_org in(?").append(++idx).append(")");
            List tmpList = Arrays.asList(StringUtils.split((String)params.get("cusOrg"),","));
            paramValues.add(tmpList);
        }

        //健康检查名称
        if (params != null && params.containsKey("ldmName") && StringUtils.isNotBlank((String)params.get("ldmName"))) {
            sql.append(" and binary cl.ldm_name like ?").append(++idx);
            String name = ((String) params.get("ldmName")).replaceAll("\\_", "\\\\_").replaceAll("\\%", "\\\\%");
            paramValues.add("%" + name + "%");
        }
        sql.append(" group by cl.ldm_id order by cl.create_time desc");

		Page page = cloudLdMonitorDao.pagedNativeQuery(sql.toString(), getQueryMap(paramsMap) , paramValues.toArray());
		@SuppressWarnings("unchecked")
		List<Object> dataList = (List<Object>)page.getResult();
		for (int i = 0; i < dataList.size(); i++) {
			Object[] objs = (Object[])dataList.get(i);
			CloudLdMonitor monitor = new CloudLdMonitor();
			monitor.setLdmId(ObjectUtils.toString(objs[0]));
			monitor.setLdmType(ObjectUtils.toString(objs[1]));
			monitor.setAdminStateup(objs[2] == null ? null : Character.valueOf((char)objs[2]));
			monitor.setCreateTime(DateUtil.stringToDate(objs[3] == null ? "" : ObjectUtils.toString(objs[3])));
			monitor.setLdmTimeout(objs[4] == null ? null : ((BigDecimal)objs[4]).longValue());
			monitor.setMaxRetries(objs[5] == null ? null : ((BigDecimal)objs[5]).longValue());
			monitor.setLdmDelay(objs[6] == null ? null : ((BigDecimal)objs[6]).longValue());
			monitor.setPrjId(ObjectUtils.toString(objs[7]));
			monitor.setPrjName(ObjectUtils.toString(objs[8]));
			monitor.setDcName(ObjectUtils.toString(objs[9]));
			monitor.setPoolNum(ObjectUtils.toString(objs[10]));
			monitor.setDcId(ObjectUtils.toString(objs[11]));
			monitor.setCusId(ObjectUtils.toString(objs[12]));
			monitor.setCusOrg(ObjectUtils.toString(objs[13]));
			monitor.setLdmName(ObjectUtils.toString(objs[14]));
			monitor.setUrlPath(ObjectUtils.toString(objs[15]));
			dataList.set(i, monitor);
		}
        return page;
    }

    protected QueryMap getQueryMap(ParamsMap paramsMap){
    	QueryMap queryMap = new QueryMap();
    	queryMap.setPageNum(paramsMap.getPageNumber() == null ? 1 : paramsMap.getPageNumber());
    	queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize() == null ? 10 : paramsMap.getPageSize());
    	return queryMap;
    }

    public String getMonitorBindPoolNames(String dcId, String prjId, String ldmId) throws AppException {
        try {
            List<String> nameList = cloudLdPoolMonitorDao.findMonitorBindPoolNames(dcId, prjId, ldmId);
            return StringUtils.join(nameList, "、");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public Page getBindMonitorList(ParamsMap paramsMap) throws AppException {
        try {
        	List<Object> paramValues = new ArrayList<>(4);
            Map<String, Object> params = paramsMap.getParams();

            StringBuffer sql = new StringBuffer();
            sql.append("select cl.ldm_id as ldmId,cl.ldm_type as ldmType,cl.admin_stateup as adminStateup,cl.create_time as createTime,");
            sql.append(" cl.ldm_timeout as ldmTimeout,cl.max_retries as maxRetries,cl.ldm_delay as ldmDelay,cl.prj_id as prjId");
            sql.append(" from cloud_ldmonitor as cl ");
            sql.append(" left join cloud_project as cp on cl.prj_id = cp.prj_id ");
            sql.append(" where cl.ldm_id not in (select pm.ldm_id from  cloud_ldpoolldmonitor as pm where pm.pool_id=?)");
            paramValues.add(params.get("poolId"));
            //数据中心
            if (params.containsKey("datacenterId")) {
                sql.append("and cl.dc_id = ? ");
                paramValues.add(params.get("datacenterId"));
            }

            //项目id为空时，查询所有项目的资源列表
            if (params.containsKey("projectId")) {
                sql.append("and cl.prj_id = ? ");
                paramValues.add(params.get("projectId"));
            }
            sql.append(" order by cl.create_time desc");
            Page page = cloudLdMonitorDao.pagedNativeQuery(sql.toString(), getQueryMap(paramsMap) , paramValues.toArray());
            @SuppressWarnings("unchecked")
    		List<Object> dataList = (List<Object>)page.getResult();
    		for (int i = 0; i < dataList.size(); i++) {
    			Object[] objs = (Object[])dataList.get(i);
    			CloudLdMonitor monitor = new CloudLdMonitor();
    			monitor.setLdmId(ObjectUtils.toString(objs[0]));
    			monitor.setLdmType(ObjectUtils.toString(objs[1]));
    			monitor.setAdminStateup(objs[2] == null ? null : Character.valueOf((char)objs[2]));
    			monitor.setCreateTime(DateUtil.stringToDate(objs[9] == null ? "" : ObjectUtils.toString(objs[9])));
    			monitor.setLdmTimeout(objs[3] == null ? null : ((BigDecimal)objs[3]).longValue());
    			monitor.setMaxRetries(objs[4] == null ? null : ((BigDecimal)objs[4]).longValue());
    			monitor.setLdmDelay(objs[5] == null ? null : ((BigDecimal)objs[5]).longValue());
    			monitor.setPrjId(ObjectUtils.toString(objs[6]));
    			dataList.set(i, monitor);
    		}
            return page;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public List<CloudLdMonitor> getNotBindMonitorListByPool(CloudLdPool pool) throws AppException {
		List<CloudLdMonitor> list =null;
		StringBuffer sql = new StringBuffer();
		sql.append("select cl.ldm_id as ldmId,cl.ldm_type as ldmType,cl.ldm_name as ldmName,");
		sql.append(" cl.ldm_timeout as ldmTimeout,cl.max_retries as maxRetries,cl.ldm_delay as ldmDelay,cl.prj_id as prjId");
		sql.append(" from cloud_ldmonitor as cl ");
		sql.append(" left join cloud_project as cp on cl.prj_id = cp.prj_id ");
		sql.append(" where cl.ldm_id not in (select pm.ldm_id from  cloud_ldpoolldmonitor as pm where pm.pool_id=?)");
		sql.append(" and cl.prj_id = ?");

		Query query = cloudLdMonitorDao.createSQLNativeQuery(sql.toString(), new Object []{pool.getPoolId(),pool.getPrjId()});

		@SuppressWarnings("rawtypes")
		List  resultList = query.getResultList();
		if(null!=resultList&&resultList.size()>0){
			list = new ArrayList<CloudLdMonitor>();
			for(int i = 0 ;i<resultList.size();i++){
				Object [] obj = (Object []) resultList.get(i);
				int index = 0;

				CloudLdMonitor monitor = new CloudLdMonitor();
				monitor.setLdmId((String)obj[index++]);
				monitor.setLdmType((String)obj[index++]);
				monitor.setLdmName((String)obj[index++]);
				monitor.setLdmTimeout(Long.parseLong(obj[index++]==null?"0":ObjectUtils.toString(obj[index-1])));
				monitor.setMaxRetries(Long.parseLong(obj[index++]==null?"0":ObjectUtils.toString(obj[index-1])));
				monitor.setLdmDelay(Long.parseLong(obj[index++]==null?"0":ObjectUtils.toString(obj[index-1])));
				monitor.setPrjId((String)obj[index++]);
				monitor.setText(monitor.getLdmName() + "(" + monitor.getLdmType()+"延迟:"+monitor.getLdmDelay()+" 重试:"+monitor.getMaxRetries()+" 超时:"+monitor.getLdmTimeout() + ")");
				list.add(monitor);
			}
		}
		return list;

	}

    public List<CloudLdMonitor> poolMonitorList(Map<String, String> params) throws AppException {
        try {
        	List<Object> paramValues = new ArrayList<>(4);
            StringBuffer sql = new StringBuffer();
            sql.append("select cl.ldm_id as ldmId,cl.ldm_type as ldmType,cl.ldm_name as ldmName,cl.admin_stateup as adminStateup,cl.create_time as createTime,");
            sql.append(" cl.ldm_timeout as ldmTimeout,cl.max_retries as maxRetries,cl.ldm_delay as ldmDelay,cl.prj_id as prjId");
            sql.append(" from cloud_ldmonitor as cl ");
            sql.append(" left join cloud_ldpoolldmonitor as cpm on cl.ldm_id = cpm.ldm_id ");
            sql.append(" where 1=1 ");

            if (params != null && params.containsKey("datacenterId")) {
                sql.append("and cl.dc_id = ? ");
                paramValues.add(params.get("datacenterId"));
            }

            //项目id为空时，查询所有项目的资源列表
            if (params != null && params.containsKey("projectId")) {
                sql.append("and cl.prj_id = ? ");
                paramValues.add(params.get("projectId"));
            }

            if (params != null && params.containsKey("poolId")) {
                sql.append("and cpm.pool_id = ? ");
                paramValues.add(params.get("poolId"));
            }
            sql.append(" order by cl.create_time desc");

            @SuppressWarnings("unchecked")
    		List<Object> dataList = cloudLdMonitorDao.createSQLNativeQuery(sql.toString(), paramValues.toArray()).getResultList();
            List<CloudLdMonitor> resultList = new ArrayList<>();
    		for (int i = 0; i < dataList.size(); i++) {
    			Object[] objs = (Object[])dataList.get(i);
    			CloudLdMonitor monitor = new CloudLdMonitor();
    			int idx = 0;
    			monitor.setLdmId(ObjectUtils.toString(objs[idx++]));
    			monitor.setLdmType(ObjectUtils.toString(objs[idx++]));
    			monitor.setLdmName((String)objs[idx++]);
    			monitor.setAdminStateup(objs[idx++] == null ? null : Character.valueOf((char)objs[idx-1]));
    			monitor.setCreateTime(DateUtil.stringToDate(objs[idx++] == null ? "" : ObjectUtils.toString(objs[idx-1])));
    			monitor.setLdmTimeout(objs[idx++] == null ? null : ((BigDecimal)objs[idx-1]).longValue());
    			monitor.setMaxRetries(objs[idx++] == null ? null : ((BigDecimal)objs[idx-1]).longValue());
    			monitor.setLdmDelay(objs[idx++] == null ? null : ((BigDecimal)objs[idx-1]).longValue());
    			monitor.setPrjId(ObjectUtils.toString(objs[idx++]));
				monitor.setText(monitor.getLdmName() + "(" + monitor.getLdmType()+"延迟:"+monitor.getLdmDelay()+" 重试:"+monitor.getMaxRetries()+" 超时:"+monitor.getLdmTimeout() + ")");
				resultList.add(monitor);
    		}
            return resultList;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public BaseCloudLdMonitor createMonitor(CloudLdMonitor ldMonitor) throws AppException {
    	BaseCloudLdMonitor monitor = null;

		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("admin_state_up", "1");
		temp.put("delay", ldMonitor.getLdmDelay());
		temp.put("max_retries", ldMonitor.getMaxRetries());
		temp.put("timeout", ldMonitor.getLdmTimeout());
		temp.put("type", ldMonitor.getLdmType());
		if(!StringUtils.isEmpty(ldMonitor.getUrlPath())){
			temp.put("url_path", ldMonitor.getUrlPath());
		}
		data.put("health_monitor", temp);
		HealthMonitor result = openstackHealthMonitorService.create(ldMonitor.getDcId(),
				ldMonitor.getPrjId(), data);
		if (result != null) {
			monitor = new BaseCloudLdMonitor();
			monitor.setLdmDelay(Long.parseLong(result.getDelay()));
			monitor.setLdmName(ldMonitor.getLdmName());
			monitor.setLdmId(result.getId());
			monitor.setMaxRetries(Long.parseLong(result.getMax_retries()));
			monitor.setLdmTimeout(Long.parseLong(result.getTimeout()));
			monitor.setLdmType(result.getType());
			monitor.setPrjId(result.getTenant_id());
			monitor.setDcId(ldMonitor.getDcId());
			monitor.setCreateName(ldMonitor.getCreateName());
			monitor.setCreateTime(new Date());
			if (result.isAdmin_state_up()) {
				monitor.setAdminStateup('1');
			} else {
				monitor.setAdminStateup('0');
			}
			if(!StringUtils.isEmpty(ldMonitor.getUrlPath())){
				monitor.setUrlPath(result.getUrl_path());
			}
			cloudLdMonitorDao.save(monitor);
		}
		return monitor;
    }

    public boolean deleteMonitor(String datacenterId, String projectId, String id) throws AppException {
        try {
            //执行openstack删除操作成功后，进行后续操作
            if (openstackHealthMonitorService.delete(datacenterId, projectId, id)) {
                //当删除数据库操作失败时，线程等待1秒后尝试重新删除，最多重试10次
                for (int i = 0; i < 10; i++) {
                    try {
                        cloudLdMonitorDao.delete(id);
                        //删除资源后更新缓存接口
                        tagService.refreshCacheAftDelRes("ldMonitor", id);
            			ecmcAlarmService.clearExpAfterDeleteHealth(id);
                        break;
                    } catch (Exception e) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e2) {
                            log.error(e2.getMessage(), e);
                        }
                    }
                }
            }
            return true;
        } catch (AppException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean detachHealthMonitor(String datacenterId, String projectId, String poolId, String monitorId) throws AppException {
        try {
            //执行openstack删除操作成功后，进行后续操作
            if (openstackHealthMonitorService.detachHealthMonitor(datacenterId, projectId, poolId, monitorId)) {
                //当删除数据库操作失败时，线程等待1秒后尝试重新删除，最多重试10次
                for (int i = 0; i < 10; i++) {
                    try {
                        cloudLdPoolMonitorDao.deleteByLdmIdAndPoolId(monitorId, poolId);
                        break;
                    } catch (Exception e) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e2) {
                            log.error(e2.getMessage(), e);
                        }
                    }
                }
            }
            return true;
        } catch (AppException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public BaseCloudLdMonitor updateMonitor(CloudLdMonitor monitor) throws AppException {
        //拼装用于提交的数据
        JSONObject data = new JSONObject();
        JSONObject temp = new JSONObject();

        temp.put("delay", monitor.getLdmDelay());
        temp.put("max_retries", monitor.getMaxRetries());
        temp.put("timeout", monitor.getLdmTimeout());
        temp.put("url_path", monitor.getUrlPath());

        data.put("health_monitor", temp);
        BaseCloudLdMonitor ldm = new BaseCloudLdMonitor();
        try {
            HealthMonitor result = openstackHealthMonitorService.update(monitor.getDcId(), monitor.getPrjId(), data, monitor.getLdmId());
            if (result != null) {
                ldm = cloudLdMonitorDao.findOne(monitor.getLdmId());
                ldm.setLdmDelay(Long.parseLong(result.getDelay()));
                ldm.setLdmTimeout(Long.parseLong(result.getTimeout()));
                ldm.setMaxRetries(Long.parseLong(result.getMax_retries()));
                ldm.setLdmName(monitor.getLdmName());
                if (result.isAdmin_state_up()) {
                    ldm.setAdminStateup('1');
                } else {
                    ldm.setAdminStateup('0');
                }
                if(!StringUtils.isEmpty(monitor.getUrlPath())){
    				ldm.setUrlPath(result.getUrl_path());
    			}
                cloudLdMonitorDao.saveOrUpdate(ldm);
            }
            return ldm;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public boolean checkHealthMonitorName(String prjId, String ldmName, String ldmId) throws AppException {
    	return cloudLdMonitorDao.countMultMonitorName(prjId, ldmName, ldmId) > 0 ? true : false;
    }

    @Override
    public List<CloudLdMonitor> getMonitorListByPool(CloudLdPool pool) throws AppException {
        List<CloudLdMonitor> list =null;
        StringBuffer sql = new StringBuffer();

        sql.append("  SELECT                              ");
        sql.append("  	m.ldm_id,                         ");
        sql.append("  	m.ldm_name,                       ");
        sql.append("  	m.ldm_type,                       ");
        sql.append("  	m.ldm_delay,                      ");
        sql.append("  	m.max_retries,                    ");
        sql.append("  	m.ldm_timeout,                    ");
		sql.append("  	ldpm.ldm_id  AS checkRadio,                            ");
		sql.append("  	m.prj_id,                            ");
		sql.append("  	m.dc_id                            ");
        sql.append("  FROM                                ");
        sql.append("  	cloud_ldmonitor m                 ");
    	sql.append("  LEFT JOIN (SELECT                ");
		sql.append("    	ldm_id                ");
		sql.append("  FROM                ");
		sql.append("  	cloud_ldpoolldmonitor                ");
		sql.append("  WHERE                ");
		sql.append("  	pool_id =?                 "); 
		sql.append("  GROUP BY                 "); 
		sql.append("  	ldm_id) ldpm ON ldpm.ldm_id = m.ldm_id                 "); 
		sql.append("  WHERE                 "); 
		sql.append("  	m.prj_id = ?                "); 

        Query query = cloudLdMonitorDao.createSQLNativeQuery(sql.toString(), new Object []{pool.getPoolId(),pool.getPrjId()});

        List  resultList = query.getResultList();
        if(null!=resultList&&resultList.size()>0){
            list = new ArrayList<CloudLdMonitor>();
            for(int i = 0 ;i<resultList.size();i++){
                Object [] obj = (Object []) resultList.get(i);
                int index = 0;

                CloudLdMonitor monitor = new CloudLdMonitor();
                monitor.setLdmId((String)obj[index++]);
                monitor.setLdmName((String)obj[index++]);
                monitor.setLdmType((String)obj[index++]);
                monitor.setLdmDelay(Long.parseLong(obj[index++]==null?"0":String.valueOf(obj[index-1])));
                monitor.setMaxRetries(Long.parseLong(obj[index++]==null?"0":String.valueOf(obj[index-1])));
                monitor.setLdmTimeout(Long.parseLong(obj[index++]==null?"0":String.valueOf(obj[index-1])));
                monitor.setCheckRadio((String)obj[index++]);
				monitor.setPrjId((String)obj[index++]);
				monitor.setDcId((String)obj[index++]);

                list.add(monitor);
            }
        }
        return list;
    }

    /**
     * 绑定和解绑健康检查
     * @param pool
     * @return
     * @throws AppException
     */
    @Override
    public List<CloudLdMonitor> bindHealthMonitor(CloudLdPool pool) throws AppException {
    	try {
			jedisUtil.delete(RedisKey.MEMBER_UNBIND_POOLiD+pool.getPoolId());
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
        List<CloudLdMonitor> list = new ArrayList<CloudLdMonitor>();
        List<String> addMonitorList = new ArrayList<String>();
        List<String> deleteMonitorList = new ArrayList<String>();

        Map<String,List<String>> map = handleMonitor(pool);
        addMonitorList = map.get("addList");
        deleteMonitorList = map.get("deleteList");

        unbungingPool(deleteMonitorList,pool);

        bindPool(addMonitorList,pool);

        return list;
    }

    public List<BaseCloudLdPoolMonitor> getMonitorByPool(String poolId) {
        StringBuffer hql =new StringBuffer();
        hql.append(" from BaseCloudLdPoolMonitor where poolId = ? ");
        return cloudLdMonitorDao.find(hql.toString(), new Object[]{poolId});
    }

    public BaseCloudLdPoolMonitor saveEntiry(BaseCloudLdPoolMonitor poolMonitor) {
        BaseCloudLdPoolMonitor baseCloudLd = new BaseCloudLdPoolMonitor();
        BeanUtils.copyPropertiesByModel(baseCloudLd, poolMonitor);
        cloudLdMonitorDao.saveEntity(baseCloudLd);
        return baseCloudLd;
    }

    public int deleteByPoolIdAndMonitorId(Object o, String poolId, String monitorId) {
        int num = 0;
        StringBuffer hql = new StringBuffer();
        hql.append("delete BaseCloudLdPoolMonitor where poolId = ? and ldmId = ?");
        num = cloudLdMonitorDao.executeUpdate(hql.toString(), poolId,monitorId);
        return num;
    }

    /**
     * 对比健康检查数据 需要
     * 需要新增的健康检查addList
     * 需要删除的健康检查deleteList
     * -----------------------
     * @author zhouhaitao
     * @param pool
     * @return
     */
    private Map<String,List<String>> handleMonitor(CloudLdPool pool) throws AppException {
        Map<String,List<String>> map = new HashMap<String,List<String>>();
        List<String> addList = new ArrayList<String>();
        List<String> deleteList = new ArrayList<String>();
        Set<String> newSet = new HashSet<String>();
        Set<String> oldSet = new HashSet<String>();

        List<String> newmonitors = pool.getMonitors();
        if(null!=newmonitors&&newmonitors.size()>10){
            throw new AppException("error.openstack.message", new String[] {"最多只能关联10条健康检查"});
        }
        List<BaseCloudLdPoolMonitor> oldMonitors = getMonitorByPool(pool.getPoolId());

        if(null!=newmonitors&&newmonitors.size()>0){
            newSet.addAll(newmonitors);
        }

        if(null!=oldMonitors&&oldMonitors.size()>0){
            for(BaseCloudLdPoolMonitor bcldpm:oldMonitors){
                oldSet.add(bcldpm.getLdmId());
            }
        }

        for(String newMoni:newSet){
            if(!oldSet.contains(newMoni)){
                addList.add(newMoni);
            }
        }

        for(String oldMoni:oldSet){
            if(!newSet.contains(oldMoni)){
                deleteList.add(oldMoni);
            }
        }

        map.put("addList", addList);
        map.put("deleteList", deleteList);
        return map;
    }

    /**
     * 解绑资源池
     * ------------------
     * @author zhouhaitao
     * @param deleteList
     * @param pool
     */
    private void unbungingPool(List<String> deleteList,CloudLdPool pool){
        for(String monitorId:deleteList){
            boolean flag = openstackHealthMonitorService.detachHealthMonitor(pool.getDcId(), pool.getPrjId(), pool.getPoolId(),monitorId);
            if(flag){
                BaseCloudLdPoolMonitor poolMonitor = new BaseCloudLdPoolMonitor();
                poolMonitor.setPoolId(pool.getPoolId());
                poolMonitor.setLdmId(monitorId);

//                saveEntiry(poolMonitor);
                deleteByPoolIdAndMonitorId(null, pool.getPoolId(), monitorId);
            }
        }
    }

    /**
     * 绑定资源池
     * ------------------
     * @author zhouhaitao
     * @param addList
     * @param pool
     */
    private void bindPool(List<String> addList,CloudLdPool pool){
        for(String monitorId:addList){
            boolean flag = openstackPoolService.bind(pool.getDcId(), pool.getPrjId(), pool.getPoolId(),monitorId);
            if(flag){
                BaseCloudLdPoolMonitor poolMonitor = new BaseCloudLdPoolMonitor();
                poolMonitor.setPoolId(pool.getPoolId());
                poolMonitor.setLdmId(monitorId);

                saveEntiry(poolMonitor);
            }
        }
    }
    
	public int getCountByPrjId(String prjId){
		return cloudLdMonitorDao.getCountByPrjId(prjId);
	}
	
	public void deleteHealthMonitor(CloudLdMonitor cloudLdMonitor) throws Exception{
		cloudLdMonitorDao.delete(cloudLdMonitor.getLdmId());
		StringBuffer hql = new StringBuffer();
        hql.append("delete BaseCloudLdPoolMonitor where ldmId = ?");
        cloudLdMonitorDao.executeUpdate(hql.toString(), cloudLdMonitor.getLdmId());
		ecmcLogService.addLog("同步资源清除数据", toType(cloudLdMonitor), cloudLdMonitor.getLdmName(), cloudLdMonitor.getPrjId(), 1, cloudLdMonitor.getLdmId(), null);
		
		JSONObject json = new JSONObject();
		json.put("resourceType", ResourceSyncConstant.LDMONITOR);
		json.put("resourceId", cloudLdMonitor.getLdmId());
		json.put("resourceName", cloudLdMonitor.getLdmName());
		json.put("synTime", new Date());
		jedisUtil.push(RedisKey.MEMBER_STAUS_SYNC_DELETED_RESOURCE, json.toJSONString());
		ecmcAlarmService.clearExpAfterDeleteHealth(cloudLdMonitor.getLdmId());
	}
	/**
	 * 拼装同步删除发送日志的资源类型
	 * @param monitor
	 * @return
	 */
	private String toType(BaseCloudLdMonitor monitor) {
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuffer resourceType = new StringBuffer();
        resourceType.append(ResourceSyncConstant.LDMEMBER);
        if(null != monitor && null != monitor.getCreateTime()){
        	resourceType.append(ResourceSyncConstant.SEPARATOR).append("创建时间：").append(sdf.format(monitor.getCreateTime()));
        }
        return resourceType.toString();
	}

	@Override
	public List<CloudLdMonitor> unBindHealthMonitorForPool(final CloudLdPool pool) throws Exception{
		jedisUtil.set(RedisKey.MEMBER_UNBIND_POOLiD+pool.getPoolId(),"1");
		StringBuffer hql =new StringBuffer();
		hql.append(" from BaseCloudLdPoolMonitor where poolId = ? ");
		List<BaseCloudLdPoolMonitor> list=cloudLdPoolMonitorDao.find(hql.toString(), new Object[]{pool.getPoolId()});
		List<CloudLdMonitor> resultData=new ArrayList<CloudLdMonitor>();
//		ecmcLBMemberService.changeMembersStatus(pool.getPoolId());
		for (BaseCloudLdPoolMonitor baseCloudLdPoolMonitor : list) {
			String monitorId=baseCloudLdPoolMonitor.getLdmId();
			boolean flag = openstackHealthMonitorService.detachHealthMonitor(pool.getDcId(), pool.getPrjId(), pool.getPoolId(),monitorId);
			if(flag){
				deleteByPoolIdAndMonitorId(null, pool.getPoolId(), monitorId);
				CloudLdMonitor cloudLdMonitor=new CloudLdMonitor();
				BeanUtils.copyPropertiesByModel(cloudLdMonitor, baseCloudLdPoolMonitor);
				resultData.add(cloudLdMonitor);
				ecmcAlarmService.doAfterUnbundHealth(pool.getPoolId(), baseCloudLdPoolMonitor.getLdmId());
				TransactionHookUtil.registAfterCompletionHook(new CompletionHook() {
					
					@Override
					public void execute(int status) {
						try {
							if(status==0){
								jedisUtil.set(RedisKey.MEMBER_UNBIND_POOLiD+pool.getPoolId(),"2");
							}else{
								jedisUtil.delete(RedisKey.MEMBER_UNBIND_POOLiD+pool.getPoolId());
							}
						} catch (Exception e) {
							log.error(e.getMessage(),e);
						}
					}
				});
			}
		}
		return resultData;
	}

}
