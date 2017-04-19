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
import com.eayun.common.constant.PayType;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceSyncConstant;
import com.eayun.common.constant.SyncProgress.SyncByDatacenterTypes;
import com.eayun.common.constant.SyncProgress.SyncType;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.sync.SyncProgressUtil;
import com.eayun.common.util.CloudResourceUtil;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.service.OpenstackNetworkService;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.schedule.service.CloudNetworkService;
import com.eayun.virtualization.dao.CloudNetWorkDao;
import com.eayun.virtualization.model.BaseCloudNetwork;

@Transactional
@Service
public class CloudStatusNetworkServiceImpl implements CloudNetworkService {
    private static final Logger log = LoggerFactory.getLogger(CloudStatusNetworkServiceImpl.class);
    @Autowired
    private JedisUtil jedisUtil; 
	@Autowired
	private OpenstackNetworkService openstackService;
	@Autowired
	private CloudNetWorkDao cloudNetworkDao;
	@Autowired
    private EcmcLogService ecmcLogService;
	@Autowired
    private SyncProgressUtil syncProgressUtil;

	@Override
	public void synchData(BaseDcDataCenter dataCenter) throws Exception {
		Map<String,BaseCloudNetwork> dbMap=new HashMap<String,BaseCloudNetwork>();             
		Map<String,BaseCloudNetwork> stackMap=new HashMap<String,BaseCloudNetwork>();      
		List<BaseCloudNetwork> dbList=queryCloudNetworkListByDcId(dataCenter.getId());
		List<BaseCloudNetwork> list=openstackService.getStackList(dataCenter);
		                                                                         
		if(null!=dbList){                                                        
			for(BaseCloudNetwork network:dbList){                                           
				dbMap.put(network.getNetId(), network);                                      
			}                                                                      
		}                                                                        
		long total = list == null ? 0L : list.size();
        syncProgressUtil.initResourceTotal(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.NET, total);                                                                         
		if(null!=list){                                                          
			for(BaseCloudNetwork network:list){                                     
				//底层数据存在本地数据库中 更新本地数据                              
				if(dbMap.containsKey(network.getNetId())){                       
					updateCloudNetworkFromStack(network);            
				}                                                                    
				else{
				    network.setIsVisible("0");
					cloudNetworkDao.save(network);                                  
				}                                                                    
				stackMap.put(network.getNetId(), network);                   
				syncProgressUtil.incrResourceDone(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.NET);
			}                                                                      
		}                                                                        
		                                                                         
		if (null != dbList) {
			for (BaseCloudNetwork network : dbList) {
				//删除本地数据库中不存在于底层的数据                                 
				if (!stackMap.containsKey(network.getNetId())) {
				    cloudNetworkDao.delete(network.getNetId());
				    ecmcLogService.addLog("同步资源清除数据", toType(network), network.getNetName(), network.getPrjId(), 1, network.getNetId(), null);
				    
				    JSONObject json = new JSONObject();
				    json.put("resourceType", ResourceSyncConstant.NETWORK);
				    json.put("resourceId", network.getNetId());
				    json.put("resourceName", network.getNetName());
				    json.put("synTime", new Date());
				    jedisUtil.push(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, json.toJSONString());
				}
			}                                                                      
		} 
	}

	
	@SuppressWarnings("unchecked")
	public List<BaseCloudNetwork>  queryCloudNetworkListByDcId(String dcId){
		StringBuffer hql = new StringBuffer();
		hql.append(" from BaseCloudNetwork  ");
		hql.append(" where dcId = ? ");
		
		return cloudNetworkDao.find(hql.toString(), new Object[]{dcId});
	}
	
	public boolean updateCloudNetworkFromStack(BaseCloudNetwork network){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_network set ");
			sql.append("	net_name = ?,         ");
			sql.append("	prj_id = ?,           ");
			sql.append("	dc_id = ?,            ");
			sql.append("	net_status = ?,       ");
			sql.append("	admin_stateup = ?,    ");
			sql.append("	is_shared = ?,        ");
			sql.append("	router_external = ?   ");
			sql.append(" where net_id = ? ");
			
			cloudNetworkDao.execSQL(sql.toString(), new Object[]{
					network.getNetName(),
					network.getPrjId(),
					network.getDcId(),
					network.getNetStatus(),
					network.getAdminStateup(),
					network.getIsShared(),
					network.getRouterExternal(),
					network.getNetId()
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
	 * 拼装同步删除发送日志的资源类型
	 * @author gaoxiang
	 * @param network
	 * @return
	 */
	private String toType(BaseCloudNetwork network) {
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    StringBuffer resourceType = new StringBuffer();
	    resourceType.append(ResourceSyncConstant.NETWORK);
	    resourceType.append("-").append(CloudResourceUtil.escapePayType(network.getPayType()));
	    if(null != network && null != network.getCreateTime()){
	    	resourceType.append(ResourceSyncConstant.SEPARATOR).append("创建时间：").append(sdf.format(network.getCreateTime()));
	    }
	    if (PayType.PAYBEFORE.equals(network.getPayType())) {
	        resourceType.append(ResourceSyncConstant.SEPARATOR).append("到期时间：").append(sdf.format(network.getEndTime()));
	    }
	    return resourceType.toString();
	}
}
