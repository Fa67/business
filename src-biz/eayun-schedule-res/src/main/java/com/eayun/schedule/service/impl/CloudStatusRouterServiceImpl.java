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
import com.eayun.eayunstack.service.OpenstackRouterService;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.schedule.service.CloudRouterService;
import com.eayun.virtualization.dao.CloudRouteDao;
import com.eayun.virtualization.dao.CloudSubNetWorkDao;
import com.eayun.virtualization.model.BaseCloudRoute;
import com.eayun.virtualization.model.BaseCloudSubNetWork;

@Transactional
@Service
public class CloudStatusRouterServiceImpl implements CloudRouterService {
    private static final Logger log = LoggerFactory.getLogger(CloudStatusRouterServiceImpl.class);
    @Autowired
    private JedisUtil jedisUtil;
	@Autowired
	private OpenstackRouterService openstackRouteService ;
	@Autowired
	private CloudRouteDao cloudRouteDao;
	@Autowired
	private CloudSubNetWorkDao cloudSubnetDao;
	@Autowired
    private EcmcLogService ecmcLogService;
	@Autowired
    private SyncProgressUtil syncProgressUtil;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void synchData(BaseDcDataCenter dataCenter) throws Exception {
		Map<String, List> map = openstackRouteService.getStackList(dataCenter);
		List<BaseCloudRoute> list=map.get("RouteList");
		List<BaseCloudSubNetWork> subList=map.get("SubList");
		Map<String,BaseCloudRoute> dbMap=new HashMap<String,BaseCloudRoute>();             
		Map<String,BaseCloudRoute> stackMap=new HashMap<String,BaseCloudRoute>();      
		List<BaseCloudRoute> dbList=queryCloudRouteListByDcId(dataCenter.getId());
		                                                                         
		if(null!=dbList){                                                        
			for(BaseCloudRoute cloudRoute:dbList){                                           
				dbMap.put(cloudRoute.getRouteId(), cloudRoute);                                      
			}                                                                      
		}                                                                        
		long total = list == null ? 0L : list.size();
        syncProgressUtil.initResourceTotal(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.ROUTER, total);                                                                         
		if(null!=list){                                                          
			for(BaseCloudRoute cloudRoute:list){                                     
				//底层数据存在本地数据库中 更新本地数据                              
				if(dbMap.containsKey(cloudRoute.getRouteId())){                       
					updateCloudRouteFromStack(cloudRoute);            
				}                                                                    
				else{                                                                
					cloudRouteDao.save(cloudRoute);                                  
				}                                                                    
				stackMap.put(cloudRoute.getRouteId(), cloudRoute);
				syncProgressUtil.incrResourceDone(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.ROUTER);
			}                                                                      
		}                                                                        
		                                                                         
		if(null!=dbList){                                                        
			for(BaseCloudRoute cloudRoute:dbList){                                           
				//删除本地数据库中不存在于底层的数据                                 
				if(!stackMap.containsKey(cloudRoute.getRouteId())){                           
					cloudRouteDao.delete(cloudRoute.getRouteId());
					ecmcLogService.addLog("同步资源清除数据", toType(cloudRoute), cloudRoute.getRouteName(), cloudRoute.getPrjId(), 1, cloudRoute.getRouteId(), null);
					
					JSONObject json = new JSONObject();
					json.put("resourceType", ResourceSyncConstant.ROUTER);
					json.put("resourceId", cloudRoute.getRouteId());
					json.put("resourceName", cloudRoute.getRouteName());
					json.put("synTime", new Date());
					jedisUtil.push(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, json.toJSONString());
				}                                                                    
			}                                                                      
		}  
		
		//处理路由连接的子网
		handleSubnet(subList,dataCenter.getId());
	
	}
	
	@SuppressWarnings("unchecked")
	public List<BaseCloudRoute> queryCloudRouteListByDcId (String dcId){
		StringBuffer hql = new StringBuffer();
		hql.append(" from  BaseCloudRoute ");
		hql.append(" where dcId = ? ");
		
		return cloudRouteDao.find(hql.toString(), new Object[]{dcId});
	}
	
	public boolean updateCloudRouteFromStack(BaseCloudRoute cloudRoute){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_route set ");
			sql.append("	route_name = ?,    ");
			sql.append("	prj_id = ?,        ");
			sql.append("	dc_id = ?,         ");
			sql.append("	route_status = ?,  ");
			sql.append("	gateway_ip = ?,  ");
			sql.append("	net_id = ?        ");
			sql.append(" where route_id = ? ");
			
			cloudRouteDao.execSQL(sql.toString(), new Object[]{
					cloudRoute.getRouteName(),
					cloudRoute.getPrjId(),
					cloudRoute.getDcId(),
					cloudRoute.getRouteStatus(),
					cloudRoute.getGatewayIp(),
					cloudRoute.getNetId(),
					cloudRoute.getRouteId()
			});
			flag = true ;
		}catch(Exception e){
			flag = false;
			log.error(e.getMessage(),e);
			throw e;
		}
		
		return flag ;
	}

	private void handleSubnet(List<BaseCloudSubNetWork> subList,String dcId){
		Map<String,BaseCloudSubNetWork> map=new HashMap<String,BaseCloudSubNetWork>();
		//获取子网列表
		List<BaseCloudSubNetWork> dbList=queryCloudSubNetworkListByDcId(dcId);
		if(null!=dbList){
			for(BaseCloudSubNetWork net :dbList){
				map.put(net.getSubnetId(), net);
			}
		}
		if(null!=subList){
			for(BaseCloudSubNetWork subnet:subList){
				if(map.containsKey(subnet.getSubnetId())){
					updateRoute(subnet);
				}
				else{
					cloudSubnetDao.save(subnet);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<BaseCloudSubNetWork> queryCloudSubNetworkListByDcId (String dcId){
		StringBuffer hql = new StringBuffer();
		hql.append(" from BaseCloudSubNetWork  ");
		hql.append(" where dcId = ? ");
		
		return cloudSubnetDao.find(hql.toString(), new Object[]{dcId});
	}
	
	public boolean updateRoute(BaseCloudSubNetWork net){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_subnetwork set ");
			sql.append("	route_id = ?   ");   
			sql.append(" where subnet_id = ?     ");
			
			cloudSubnetDao.execSQL(sql.toString(), new Object[]{
					net.getRouteId(),
					net.getSubnetId()
			});
			flag = true ;
		}catch(Exception e){
			flag = false;
			log.error(e.getMessage(),e);
			throw e;
		}
		
		return flag ;
	}
	
	private String toType(BaseCloudRoute cloudRoute) {
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuffer resourceType = new StringBuffer();
        resourceType.append(ResourceSyncConstant.ROUTER);
        if(null != cloudRoute && null != cloudRoute.getCreateTime()){
        	resourceType.append(ResourceSyncConstant.SEPARATOR).append("创建时间：").append(sdf.format(cloudRoute.getCreateTime()));
        }
        return resourceType.toString();
	}
}
