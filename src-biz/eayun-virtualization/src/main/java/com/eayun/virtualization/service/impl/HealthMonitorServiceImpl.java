package com.eayun.virtualization.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.StringUtil;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.CompletionHook;
import com.eayun.common.util.TransactionHookUtil.Hook;
import com.eayun.eayunstack.model.HealthMonitor;
import com.eayun.eayunstack.service.OpenstackHealthMonitorService;
import com.eayun.eayunstack.service.OpenstackPoolService;
import com.eayun.monitor.model.BaseCloudLdpoolExp;
import com.eayun.monitor.service.AlarmService;
import com.eayun.monitor.service.LdPoolAlarmMonitorService;
import com.eayun.virtualization.dao.CloudLdMonitorDao;
import com.eayun.virtualization.model.BaseCloudLdMember;
import com.eayun.virtualization.model.BaseCloudLdMonitor;
import com.eayun.virtualization.model.BaseCloudLdPoolMonitor;
import com.eayun.virtualization.model.CloudLdMember;
import com.eayun.virtualization.model.CloudLdMonitor;
import com.eayun.virtualization.model.CloudLdPool;
import com.eayun.virtualization.service.HealthMonitorService;
import com.eayun.virtualization.service.LdPoolMonitorService;
import com.eayun.virtualization.service.MemberService;
import com.eayun.virtualization.service.TagService;

/**
 * HealthMonitorServiceImpl
 * 
 * @Filename: HealthMonitorServiceImpl.java
 * @Description:
 * @Version: 1.0
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br> <li>Date: 2015年11月12日</li> <li>Version: 1.0</li> <li>Content:
 *               create</li>
 * 
 */
@Service
@Transactional
public class HealthMonitorServiceImpl implements HealthMonitorService {
	private static final Logger log = LoggerFactory.getLogger(HealthMonitorServiceImpl.class);
	@Autowired
	private CloudLdMonitorDao monitorDao;
	@Autowired
	private OpenstackHealthMonitorService openstackMonitorService;
	@Autowired
	private OpenstackPoolService openstackPoolService;
	@Autowired
	private TagService tagService;
	@Autowired
	private LdPoolMonitorService poolMonitorService;
	@Autowired
	private MemberService memberService;
	@Autowired
	private AlarmService alarmService;
	@Autowired
	private JedisUtil jedisUtil;
	
	/**
	 * 分页查询项目的健康检查
	 * -----------------------
	 * @author zhouhaitao
	 * @param page
	 * @param dcId
	 * @param prjId
	 * @param ldmName
	 * @param queryMap
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Page getMonitorList(Page page,String dcId,String prjId,String ldmName ,QueryMap queryMap){
		StringBuffer sql = new StringBuffer();
		Object[] args = new Object[10];
		int index = 0;
		
		sql.append("   SELECT                        ");
		sql.append("   	clm.dc_id,                   ");
		sql.append("   	clm.prj_id,                  ");
		sql.append("   	clm.ldm_id,                  ");
		sql.append("   	clm.ldm_name,                ");
		sql.append("   	clm.ldm_type,                ");
		sql.append("   	clm.ldm_delay,               ");
		sql.append("   	clm.max_retries,             ");
		sql.append("   	clm.ldm_timeout,             ");
		sql.append("   	clm.url_path,                ");
		sql.append("   	p.poolCount                  ");
		sql.append("   FROM                          ");
		sql.append("   	cloud_ldmonitor clm          ");
		sql.append("   LEFT JOIN (                   ");
		sql.append("   	SELECT                       ");
		sql.append("   		count(1) AS poolCount,   ");
		sql.append("   		ldpm.ldm_id              ");
		sql.append("   	FROM                         ");
		sql.append("   		cloud_ldpoolldmonitor ldpm");
		sql.append("   	GROUP BY                     ");
		sql.append("   		ldpm.ldm_id              ");
		sql.append("   ) p ON p.ldm_id = clm.ldm_id  ");
		sql.append("   WHERE 1=1                     ");
		sql.append("   AND clm.prj_Id = ?   ");
		args[index++] = prjId;
		if(!StringUtils.isEmpty(ldmName)){
			sql.append("   AND clm.ldm_name like ?   ");
			ldmName = ldmName.replaceAll("\\_", "\\\\_");
			args[index++] = "%" + ldmName + "%";
		}
		sql.append("   ORDER BY                      ");
		sql.append("   	clm.create_time DESC         ");
		
		
		Object[] params = new Object[index];
		System.arraycopy(args, 0, params, 0, index);
		page = monitorDao.pagedNativeQuery(sql.toString(), queryMap, params);
		
		List newList = (List) page.getResult();
		for (int i =0;i<newList.size();i++) {
			Object [] obj = (Object []) newList.get(i);
			index = 0;
			CloudLdMonitor cloudMonitor = new CloudLdMonitor();
			cloudMonitor.setDcId((String)obj[index++]);
			cloudMonitor.setPrjId((String)obj[index++]);
			cloudMonitor.setLdmId((String)obj[index++]);
			cloudMonitor.setLdmName((String)obj[index++]);
			cloudMonitor.setLdmType((String)obj[index++]);
			cloudMonitor.setLdmDelay(Long.parseLong(obj[index++]==null?"0":String.valueOf(obj[index-1])));
			cloudMonitor.setMaxRetries(Long.parseLong(obj[index++]==null?"0":String.valueOf(obj[index-1])));
			cloudMonitor.setLdmTimeout(Long.parseLong(obj[index++]==null?"0":String.valueOf(obj[index-1])));
			cloudMonitor.setUrlPath((String)obj[index++]);
			cloudMonitor.setPoolNum(obj[index++]==null?"0":obj[index-1]+"");
			
			String tag=tagService.getResourceTagForShowcase("ldMonitor", cloudMonitor.getLdmId());
			cloudMonitor.setTagName(tag);
			
			newList.set(i, cloudMonitor);
		}
		return page;

	}
	
	/*
	 *根据prjId查询个数 
	 */
	public int getCountByPrjId(String prjId){
		return monitorDao.getCountByPrjId(prjId);
	}
	
	
	/**
	 * 查询项目下的健康检查
	 * 
	 * @author zhouhaitao
	 * @param pool
	 * @return
	 */
	@SuppressWarnings({"rawtypes" })
	public List<CloudLdMonitor> getMonitorListByPool(CloudLdPool pool){
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
		
		Query query = monitorDao.createSQLNativeQuery(sql.toString(), new Object []{pool.getPoolId(),pool.getPrjId()});
		
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

	// 根据资源池Id删除监控
	public int deleteHealthMonitorByPoolId(String poolId) {
		StringBuffer sb = new StringBuffer();
		sb.append("delete BaseCloudLdMember where poolId = ?");
		int num = monitorDao.executeUpdate(sb.toString(), poolId);
		return num;
	}

	/**
	 * 添加健康检查
	 * ------------------
	 * @author zhouhaitao
	 * @param monitor
	 * @param sessionUser
	 */
	public BaseCloudLdMonitor addHealthMonitor(CloudLdMonitor ldMonitor,SessionUserInfo sessionUser) {
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
		HealthMonitor result = openstackMonitorService.create(ldMonitor.getDcId(),
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
			monitor.setCreateName(sessionUser.getUserName());
			monitor.setCreateTime(new Date());
			if (result.isAdmin_state_up()) {
				monitor.setAdminStateup('1');
			} else {
				monitor.setAdminStateup('0');
			}
			if(!StringUtil.isEmpty(ldMonitor.getUrlPath())){
				monitor.setUrlPath(result.getUrl_path());
			}
			monitorDao.saveOrUpdate(monitor);

		}
		return monitor;

	}

	/**
	 * 修改健康检查
	 * --------------------
	 * @author zhouhaitao
	 * @param monitor
	 * @return
	 */
	public BaseCloudLdMonitor updateMonitor(CloudLdMonitor monitor) {
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("delay", monitor.getLdmDelay());
		temp.put("max_retries", monitor.getMaxRetries());
		temp.put("timeout", monitor.getLdmTimeout());
		if(!StringUtil.isEmpty(monitor.getUrlPath())){
			temp.put("url_path", monitor.getUrlPath());
		}
		data.put("health_monitor", temp);
		BaseCloudLdMonitor ldm = new BaseCloudLdMonitor();

		HealthMonitor result = openstackMonitorService.update(monitor.getDcId(),
				monitor.getPrjId(), data, monitor.getLdmId());
		if (result != null) {
			ldm = monitorDao.findOne(monitor.getLdmId());
			ldm.setLdmDelay(Long.parseLong(result.getDelay()));
			ldm.setLdmTimeout(Long.parseLong(result.getTimeout()));
			ldm.setMaxRetries(Long.parseLong(result.getMax_retries()));
			ldm.setLdmName(monitor.getLdmName());
			if (result.isAdmin_state_up()) {
				ldm.setAdminStateup('1');
			} else {
				ldm.setAdminStateup('0');
			}
			if(!StringUtil.isEmpty(monitor.getUrlPath())){
				ldm.setUrlPath(result.getUrl_path());
			}
				
			monitorDao.saveOrUpdate(ldm);
			
		}
		return ldm;

	}
	/**
	 * 删除健康检查
	 * ------------------
	 * @author zhouhaitao
	 * @param monitor
	 * @return
	 */
	public boolean deleteMonitor(CloudLdMonitor monitor){
		if (openstackMonitorService.delete(monitor.getDcId(), monitor.getPrjId(), monitor.getLdmId())) {
			monitorDao.delete(monitor.getLdmId());
			
			//删除资源后更新缓存接口
			tagService.refreshCacheAftDelRes("ldMonitor", monitor.getLdmId());
			alarmService.clearExpAfterDeleteHealth(monitor.getLdmId());
			return true;
		}
		return false;
		
	}
	
	/**
	 * 健康检查
	 * ------------------
	 * @author zhouhaitao
	 * @param pool
	 * @return
	 * @throws AppException
	 */
	public List<CloudLdMonitor> bindHealthMonitor(CloudLdPool pool){
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
	
	/**
	 * 校验健康检查重名
	 * ------------------
	 * @author zhouhaitao
	 * @param monitor
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean bindHealthMonitor(CloudLdMonitor monitor){
		StringBuffer hql = new StringBuffer ();
		Object [] params = new Object []{monitor.getPrjId(),monitor.getLdmName()};
		hql.append(" from BaseCloudLdMonitor  ");
		hql.append(" where 1=1  ");
		hql.append(" and prjId = ?");
		hql.append(" and binary(ldmName) = ?");
		if(!StringUtils.isEmpty(monitor.getLdmId())){
			hql.append(" and ldmId <> ?");
			params = new Object []{monitor.getPrjId(),monitor.getLdmName(),monitor.getLdmId()};
		}
		List<BaseCloudLdMonitor> list = monitorDao.find(hql.toString(), params);
		
		return null==list||list.size()==0;
	}

	@Override
	public CloudLdMonitor getHealthMonitor(String monitor) throws Exception {
		BaseCloudLdMonitor baseCloudLdMonitor=monitorDao.findOne(monitor);
		CloudLdMonitor cloudLdMonitor=new CloudLdMonitor();
		BeanUtils.copyPropertiesByModel(cloudLdMonitor,baseCloudLdMonitor);
		return cloudLdMonitor;
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
	private Map<String,List<String>> handleMonitor(CloudLdPool pool){
		Map<String,List<String>> map = new HashMap<String,List<String>>();
		List<String> addList = new ArrayList<String>();
		List<String> deleteList = new ArrayList<String>();
		Set<String> newSet = new HashSet<String>();
		Set<String> oldSet = new HashSet<String>();
		
		List<String> newmonitors = pool.getMonitors();
		if(null!=newmonitors&&newmonitors.size()>10){
			throw new AppException("error.openstack.message", new String[] {"最多只能关联10条健康检查"});
		}
		List<BaseCloudLdPoolMonitor> oldMonitors = poolMonitorService.getMonitorByPool(pool.getPoolId());
		
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
				
				poolMonitorService.saveEntiry(poolMonitor);
			}
		}
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
			boolean flag = openstackMonitorService.detachHealthMonitor(pool.getDcId(), pool.getPrjId(), pool.getPoolId(),monitorId);
			if(flag){
				BaseCloudLdPoolMonitor poolMonitor = new BaseCloudLdPoolMonitor();
				poolMonitor.setPoolId(pool.getPoolId());
				poolMonitor.setLdmId(monitorId);
				
//				poolMonitorService.saveEntiry(poolMonitor);
				poolMonitorService.deleteByPoolIdAndMonitorId(null, pool.getPoolId(), monitorId);
				alarmService.doAfterUnbundHealth(pool.getPoolId(), monitorId);
			}
		}
	}

	@Override
	public CloudLdMonitor bindHealthMonitorForPool(CloudLdPool pool) {
		String monitorId=pool.getMonitor();
		boolean flag = openstackPoolService.bind(pool.getDcId(), pool.getPrjId(), pool.getPoolId(),monitorId);
		CloudLdMonitor cloudLdMonitor=new CloudLdMonitor();
		if(flag){
			BaseCloudLdPoolMonitor poolMonitor = new BaseCloudLdPoolMonitor();
			poolMonitor.setPoolId(pool.getPoolId());
			poolMonitor.setLdmId(monitorId);
			BaseCloudLdPoolMonitor baseCloudLdPoolMonitor= poolMonitorService.saveEntiry(poolMonitor);
			BeanUtils.copyPropertiesByModel(cloudLdMonitor, baseCloudLdPoolMonitor);
		}
		return cloudLdMonitor;
	}

	@Override
	public List<CloudLdMonitor> unBindHealthMonitorForPool(final CloudLdPool pool) throws Exception{
		jedisUtil.set(RedisKey.MEMBER_UNBIND_POOLiD+pool.getPoolId(),"1");
		List<BaseCloudLdPoolMonitor> list=poolMonitorService.getMonitorByPool(pool.getPoolId());
		List<CloudLdMonitor> resultData=new ArrayList<CloudLdMonitor>();
//		memberService.changeMembersStatus(pool.getPoolId());
		for (BaseCloudLdPoolMonitor baseCloudLdPoolMonitor : list) {
			String monitorId=baseCloudLdPoolMonitor.getLdmId();
			boolean flag = openstackMonitorService.detachHealthMonitor(pool.getDcId(), pool.getPrjId(), pool.getPoolId(),monitorId);
			if(flag){
				poolMonitorService.deleteByPoolIdAndMonitorId(null, pool.getPoolId(), monitorId);
				CloudLdMonitor cloudLdMonitor=new CloudLdMonitor();
				BeanUtils.copyPropertiesByModel(cloudLdMonitor, baseCloudLdPoolMonitor);
				resultData.add(cloudLdMonitor);
				alarmService.doAfterUnbundHealth(pool.getPoolId(), baseCloudLdPoolMonitor.getLdmId());
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
	@Override
	public CloudLdMonitor getHealthMonitorByPool(CloudLdPool pool)
			throws Exception {
		StringBuffer sql=new StringBuffer();
		sql.append("  SELECT                              ");
		sql.append("  	m.ldm_id,                         ");
		sql.append("  	m.ldm_name,                       ");
		sql.append("  	m.ldm_type,                       ");
		sql.append("  	m.ldm_delay,                      ");
		sql.append("  	m.max_retries,                    ");
		sql.append("  	m.ldm_timeout,                    ");
		sql.append("  	m.prj_id,                            ");
		sql.append("  	m.dc_id,                            ");
		sql.append("  	m.admin_stateup                           ");
		sql.append("  FROM                                ");
		sql.append("  	cloud_ldmonitor m                 ");
		sql.append("  LEFT JOIN                 ");
		sql.append("    	cloud_ldpoolldmonitor ldpm                ");
		sql.append(" 	ON                ");
		sql.append("  	ldpm.ldm_id = m.ldm_id                ");
		sql.append("  WHERE                ");
		sql.append("  	ldpm.pool_id =?                 "); 
		sql.append("  	AND m.prj_id =?                 "); 
		Query query=monitorDao.createSQLNativeQuery(sql.toString(), new Object[]{pool.getPoolId(),pool.getPrjId()});
		List  resultList = query.getResultList();
		CloudLdMonitor monitor = new CloudLdMonitor();
		if(null!=resultList&&resultList.size()>0){
			for(int i = 0 ;i<resultList.size();i++){
				Object [] obj = (Object []) resultList.get(i);
				int index = 0;
				
				monitor.setLdmId((String)obj[index++]);
				monitor.setLdmName((String)obj[index++]);
				monitor.setLdmType((String)obj[index++]);
				monitor.setLdmDelay(Long.parseLong(obj[index++]==null?"0":String.valueOf(obj[index-1])));
				monitor.setMaxRetries(Long.parseLong(obj[index++]==null?"0":String.valueOf(obj[index-1])));
				monitor.setLdmTimeout(Long.parseLong(obj[index++]==null?"0":String.valueOf(obj[index-1])));
				monitor.setPrjId((String)obj[index++]);
				monitor.setDcId((String)obj[index++]);
				monitor.setAdminStateup((Character)obj[index++]);
			}
		}
		return monitor;
	}
}
