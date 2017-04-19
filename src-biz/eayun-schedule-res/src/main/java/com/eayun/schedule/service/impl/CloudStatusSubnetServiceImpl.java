package com.eayun.schedule.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.eayun.eayunstack.service.OpenstackSubNetworkService;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.schedule.service.CloudSubnetService;
import com.eayun.virtualization.dao.CloudSubNetWorkDao;
import com.eayun.virtualization.model.BaseCloudSubNetWork;

@Transactional
@Service
public class CloudStatusSubnetServiceImpl implements CloudSubnetService {
    private static final Logger log = LoggerFactory.getLogger(CloudStatusSubnetServiceImpl.class);
    @Autowired
    private JedisUtil jedisUtil;
	@Autowired
	private OpenstackSubNetworkService openstackService ;
	@Autowired
	private CloudSubNetWorkDao cloudNetsubworkDao;
	@Autowired
    private EcmcLogService ecmcLogService;
	@Autowired
    private SyncProgressUtil syncProgressUtil;
	@Override
	public void synchData(BaseDcDataCenter dataCenter) throws Exception {
		Map<String,BaseCloudSubNetWork> dbMap=new HashMap<String,BaseCloudSubNetWork>();             
		Map<String,BaseCloudSubNetWork> stackMap=new HashMap<String,BaseCloudSubNetWork>();      
		List<BaseCloudSubNetWork> dbList=queryCloudSubNetworkListByDcId(dataCenter.getId());
		List<BaseCloudSubNetWork> list = openstackService.getStackList(dataCenter);
		                                                                         
		if(null!=dbList){                                                        
			for(BaseCloudSubNetWork cfr:dbList){                                           
				dbMap.put(cfr.getSubnetId(), cfr);                                      
			}                                                                      
		}                                                                        
		long total = list == null ? 0L : list.size();
        syncProgressUtil.initResourceTotal(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.SUBNET, total);                                                                         
		if(null!=list){                                                          
			for(BaseCloudSubNetWork cloudFwRule:list){                                     
				//底层数据存在本地数据库中 更新本地数据                              
				if(dbMap.containsKey(cloudFwRule.getSubnetId())){                       
					updateCloudSubNetworkFromStack(cloudFwRule);            
				}                                                                    
				else{                                                                
					cloudNetsubworkDao.save(cloudFwRule);                                  
				}                                                                    
				stackMap.put(cloudFwRule.getSubnetId(), cloudFwRule);                   
				syncProgressUtil.incrResourceDone(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.SUBNET);
			}                                                                      
		}                                                                        
		                                                                         
		if(null!=dbList){                                                        
			for(BaseCloudSubNetWork cfr:dbList){                                           
				//删除本地数据库中不存在于底层的数据                                 
				if(!stackMap.containsKey(cfr.getSubnetId())){                           
					cloudNetsubworkDao.delete(cfr.getSubnetId());
					ecmcLogService.addLog("同步资源清除数据", toType(cfr), cfr.getSubnetName(), cfr.getPrjId(), 1, cfr.getSubnetId(), null);
					
					JSONObject json = new JSONObject();
					json.put("resourceType", ResourceSyncConstant.SUBNET);
					json.put("resourceId", cfr.getSubnetId());
					json.put("resourceName", cfr.getSubnetName());
					json.put("synTime", new Date());
					jedisUtil.push(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, json.toJSONString());
				}                                                                    
			}                                                                      
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<BaseCloudSubNetWork> queryCloudSubNetworkListByDcId (String dcId){
		StringBuffer hql = new StringBuffer();
		hql.append(" from  BaseCloudSubNetWork ");
		hql.append(" where dcId = ? ");
		
		return cloudNetsubworkDao.find(hql.toString(), new Object[]{dcId});
	}
	
	public boolean updateCloudSubNetworkFromStack(BaseCloudSubNetWork subNetwork){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_subnetwork set ");
			sql.append("	subnet_name = ?,     ");    
			sql.append("	prj_id = ?,          ");    
			sql.append("	dc_id = ?,           ");    
			sql.append("	net_id = ?,          ");    
			sql.append("	ip_version = ?,      ");   
			sql.append("	cidr = ?,            ");   
			sql.append("	gateway_ip = ?,      ");   
			sql.append("	pooldata = ?,        ");   
			sql.append("	route_id = ? ,       ");   
			sql.append("	is_forbiddengw = ?   ");   
			sql.append(" where subnet_id = ?     ");
			
			cloudNetsubworkDao.execSQL(sql.toString(), new Object[]{
					subNetwork.getSubnetName(),
					subNetwork.getPrjId(),
					subNetwork.getDcId(),
					subNetwork.getNetId(),
					subNetwork.getIpVersion(),
					subNetwork.getCidr(),
					subNetwork.getGatewayIp(),
					subNetwork.getPooldata(),
					subNetwork.getRouteId(),
					subNetwork.getIsForbiddengw(),
					subNetwork.getSubnetId()
			});
			flag = true ;
		}catch(Exception e){
			flag = false;
			log.error(e.getMessage(),e);
			throw e;
		}
		
		return flag ;
	}

	private String toType(BaseCloudSubNetWork csn) {
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuffer resourceType = new StringBuffer();
        resourceType.append(ResourceSyncConstant.SUBNET);
        if(null != csn && null != csn.getCreateTime()){
        	resourceType.append(ResourceSyncConstant.SEPARATOR).append("创建时间：").append(sdf.format(csn.getCreateTime()));
        }
        return resourceType.toString();
	}
}
