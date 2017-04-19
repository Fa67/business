package com.eayun.schedule.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceSyncConstant;
import com.eayun.common.constant.SyncProgress.SyncByDatacenterTypes;
import com.eayun.common.constant.SyncProgress.SyncType;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.sync.SyncProgressUtil;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.service.OpenstackHealthMonitorService;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.schedule.service.CloudLdMonitorService;
import com.eayun.virtualization.dao.CloudLdMonitorDao;
import com.eayun.virtualization.model.BaseCloudLdMonitor;

@Transactional
@Service
public class CloudStatusLdMonitorServiceImpl implements CloudLdMonitorService {
    private static final Logger log = LoggerFactory.getLogger(CloudStatusLdMonitorServiceImpl.class);
    @Autowired
    private JedisUtil jedisUtil;
	@Autowired
	private OpenstackHealthMonitorService openstackService;
	@Autowired
	private CloudLdMonitorDao cloudLdMonitorDao ;
	@Autowired
    private EcmcLogService ecmcLogService;
	@Autowired
    private SyncProgressUtil syncProgressUtil;
	
	@SuppressWarnings("unchecked")
	@Override
	public void synchData(BaseDcDataCenter dataCenter) throws Exception {
		Map<String,Object> map =openstackService.getStackList(dataCenter);
		List<BaseCloudLdMonitor> list=(List<BaseCloudLdMonitor>) map.get("MonitorList");
		Map<String,List<String>> poolMap=(Map<String, List<String>>) map.get("PoolMap");
		Map<String,BaseCloudLdMonitor> dbMap=new HashMap<String,BaseCloudLdMonitor>();             
		Map<String,BaseCloudLdMonitor> stackMap=new HashMap<String,BaseCloudLdMonitor>();      
		List<BaseCloudLdMonitor> dbList=queryCloudLdmonitorListByDcId(dataCenter.getId());
		                                                                         
		if(null!=dbList){                                                        
			for(BaseCloudLdMonitor cldm:dbList){                                           
				dbMap.put(cldm.getLdmId(), cldm);                                      
			}                                                                      
		}                                                                        
		long total = list == null ? 0L : list.size();
        syncProgressUtil.initResourceTotal(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.LB_MONITOR, total);                                                                         
		if(null!=list){                                                          
			for(BaseCloudLdMonitor cldm:list){                                     
				//底层数据存在本地数据库中 更新本地数据                              
				if(dbMap.containsKey(cldm.getLdmId())){                       
					updateCloudLdmonitorFromStack(cldm);            
				}                                                                    
				else{
					if(StringUtils.isEmpty(cldm.getLdmName())){
						cldm.setLdmName(cldm.getLdmId());
					}
					cloudLdMonitorDao.save(cldm);                                  
				}                                                                    
				stackMap.put(cldm.getLdmId(), cldm);
				syncProgressUtil.incrResourceDone(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.LB_MONITOR);
			}                                                                      
		}                                                                        
		                                                                         
		if(null!=dbList){                                                        
			for(BaseCloudLdMonitor cldm:dbList){                    
				
				//删除本地数据库中不存在于底层的数据                                 
				if(!stackMap.containsKey(cldm.getLdmId())){                           
					cloudLdMonitorDao.delete(cldm.getLdmId());
					ecmcLogService.addLog("同步资源清除数据", toType(cldm), cldm.getLdmName(), cldm.getPrjId(), 1, cldm.getLdmId(), null);
					
					JSONObject json = new JSONObject();
					json.put("resourceType", ResourceSyncConstant.LDMONITOR);
					json.put("resourceId", cldm.getLdmId());
					json.put("resourceName", cldm.getLdmName());
					json.put("synTime", new Date());
					jedisUtil.push(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, json.toJSONString());
				}                                                                    
			}                                                                      
		}
		
		handleRelateOnPool(poolMap);
	
	}

	@SuppressWarnings("unchecked")
	public List<BaseCloudLdMonitor>  queryCloudLdmonitorListByDcId(String dcId){
		StringBuffer hql = new StringBuffer();
		hql.append(" from BaseCloudLdMonitor  ");
		hql.append(" where dcId = ? ");
		
		return cloudLdMonitorDao.find(hql.toString(), new Object[]{dcId});
	}
	
	public boolean updateCloudLdmonitorFromStack(BaseCloudLdMonitor cldm){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_ldmonitor set ");
			sql.append("	prj_id = ?,        ");
			sql.append("	dc_id = ?,         ");
			sql.append("	ldm_type = ?,      ");
			sql.append("	ldm_delay = ?,     ");
			sql.append("	ldm_timeout = ?,   ");
			sql.append("	max_retries = ?,   ");
			sql.append("	admin_stateup = ?  ");
			sql.append(" where ldm_id = ? ");
			
			cloudLdMonitorDao.execSQL(sql.toString(), new Object[]{
					cldm.getPrjId(),
					cldm.getDcId(),
					cldm.getLdmType(),
					cldm.getLdmDelay(),
					cldm.getLdmTimeout(),
					cldm.getMaxRetries(),
					cldm.getAdminStateup()+"",
					cldm.getLdmId()
			});
			flag = true ;
		}catch(Exception e){
			flag = false;
			log.error(e.getMessage(),e);
			throw e;
		}
		
		return flag ;
	}
	
	/**
	 * 处理的资源池与监控之间的关联关系
	 * 		1.删除监控下的资源池的关联关系
	 * 		2.新增监控与资源池的关系
	 * @param poolMap
	 */
	private void handleRelateOnPool(Map<String,List<String>> poolMap){
		if(null!=poolMap){
			Set<String> monSet=poolMap.keySet();
			for(String monId:monSet){
				List<String> list = poolMap.get(monId);
				deleteByLdmId(monId);
				if(null!=list&&list.size()>0){
					for(String poolId:list){
						addLdPool(monId,poolId);
					}
				}
		}
		}
	}
	
	public boolean addLdPool(String monId,String poolId){
		boolean flag = false ;
		try{
			StringBuffer sql =new StringBuffer();
			sql.append(" insert into  cloud_ldpoolldmonitor (ldm_id,pool_id) values (?,?) ");
			
			cloudLdMonitorDao.execSQL(sql.toString(), new Object[]{monId,poolId});
			flag = true ;
		}catch(Exception e){
			flag = false;
			log.error(e.getMessage(),e);
			throw e;
		}
		
		return flag ;
	}
	
	public boolean deleteByLdmId(String monId){
		boolean flag = false ;
		try{
			StringBuffer sql =new StringBuffer();
			sql.append(" delete from  cloud_ldpoolldmonitor  ");
			sql.append(" where ldm_id = ? ");
			
			cloudLdMonitorDao.execSQL(sql.toString(), new Object[]{monId});
			flag = true ;
		}catch(Exception e){
			flag = false;
			log.error(e.getMessage(),e);
			throw e;
		}
		
		return flag ;
	}
	/**
	 * 拼装同步删除发送日志的资源类型
	 * @author gaoxiang
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
}
