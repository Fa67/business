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
import com.eayun.eayunstack.service.OpenstackFirewallService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.schedule.service.CloudFirewallService;
import com.eayun.virtualization.dao.CloudFireWallDao;
import com.eayun.virtualization.model.BaseCloudFireWall;
import com.eayun.virtualization.model.CloudFireWall;

@Transactional
@Service
public class CloudStatusFirewallServiceImpl implements CloudFirewallService{
    private static final Logger log = LoggerFactory.getLogger(CloudStatusFirewallServiceImpl.class);
    @Autowired
    private JedisUtil jedisUtil; 
	@Autowired
	private EcmcLogService ecmcLogService;
	@Autowired
	private OpenstackFirewallService openStackFwService;
	@Autowired
	private CloudFireWallDao fireWallDao;
	@Autowired
    private SyncProgressUtil syncProgressUtil;

	@Override
	public String pop(String groupKey) {
		String value = null;
        try {
            value = jedisUtil.pop(groupKey);
        } catch (Exception e){
            log.error(e.getMessage(),e);
            return null;
        }
        return value;
	}

	/**
	 * 重新放入队列
	 * @param groupKey
	 * @param value
	 * @return
	 */
	@Override
	public	boolean push(String groupKey,String value){
		boolean flag = false;
		try {
			flag=  jedisUtil.push(groupKey, value);
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			flag = false;
		}
		return flag;
	}
	
	/**
	 * 查询当前队列的长度
	 * @param groupKey
	 * @return
	 */
	public long size (String groupKey){
		return jedisUtil.sizeOfList(groupKey);
	}
	
	/**
	 * 获取底层指定ID的资源，底层异常为null
	 * ------------------
	 * @author zhouhaitao
	 * @param value
	 * 
	 */
	@Override
	public JSONObject get(JSONObject valueJson) throws Exception{
		JSONObject result = null ;
		if(null!=valueJson){
			JSONObject json = openStackFwService.get(valueJson.getString("dcId"),
					valueJson.getString("fwId"));
			if(null!=json){
				String jsonStr = json.toJSONString();
				boolean isDeleted=jsonStr.contains("NotFound");
				if(!isDeleted){
					result=json.getJSONObject(OpenstackUriConstant.FIREWALL_DATA_NAME);
				}
				else{
					result =new JSONObject();
					result.put("deletingStatus", isDeleted+"");
				}
			}
		}
		return result;
		
	}
	
	/**
	 * 修改防火墙信息
	 * @param cloudVm
	 * @return
	 */
	public boolean updateFw(CloudFireWall cloudFw) throws Exception {
		boolean flag = false;
		try{
			BaseCloudFireWall baseCloudFireWall=fireWallDao.findOne(cloudFw.getFwId());
			baseCloudFireWall.setFwStatus(cloudFw.getFwStatus());
			
			fireWallDao.saveOrUpdate(baseCloudFireWall);
		}catch(Exception e){
			flag = false;
			throw e;
		}
		
		return flag ;
	}

	@SuppressWarnings("unchecked")
	public List<BaseCloudFireWall> queryCloudFirewallListByDcId (String dcId){
		StringBuffer hql = new StringBuffer ();
		hql.append(" from BaseCloudFireWall where dcId = ? ");
		return fireWallDao.find(hql.toString(), new Object []{dcId});
	}
	
	public boolean updateCloudFirewallFromStack(BaseCloudFireWall firewall){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_firewall set ");
			sql.append("	fw_name = ?,       ");
			sql.append("	prj_id = ?,        ");
			sql.append("	dc_id = ?,         ");
			sql.append("	description = ?,   ");
			sql.append("	is_shared = ?,     ");
			sql.append("	admin_stateup = ?, ");
			sql.append("	fw_status = ?,     ");
			sql.append("	fwp_id = ?        ");
			sql.append(" where fw_id = ? ");
			
			fireWallDao.execSQL(sql.toString(), new Object[]{
					firewall.getFwName(),
					firewall.getPrjId(),
					firewall.getDcId(),
					firewall.getDescription(),
					firewall.getIsShared(),
					firewall.getAdminStateup(),
					firewall.getFwStatus(),
					firewall.getFwpId(),
					firewall.getFwId()
			});
			flag = true ;
		}catch(Exception e){
			flag = false;
			log.error(e.getMessage(),e);
			throw e;
		}
		
		return flag ;
	}
	
	@Override
	public void synchData(BaseDcDataCenter dataCenter) throws Exception {
		Map<String,BaseCloudFireWall> dbMap=new HashMap<String,BaseCloudFireWall>();             
		Map<String,BaseCloudFireWall> stackMap=new HashMap<String,BaseCloudFireWall>();      
		List<BaseCloudFireWall> dbList=queryCloudFirewallListByDcId(dataCenter.getId());
		List<BaseCloudFireWall> stackList=openStackFwService.getStackList(dataCenter);
		                                                                         
		if(null!=dbList){                                                        
			for(BaseCloudFireWall firewall:dbList){                                           
				dbMap.put(firewall.getFwId(), firewall);                                      
			}                                                                      
		}                          
		long total = stackList == null ? 0L : stackList.size();
		syncProgressUtil.initResourceTotal(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.FIREWALL, total);                                                             
		if(null!=stackList){                                                          
			for(BaseCloudFireWall firewall:stackList){                                     
				//底层数据存在本地数据库中 更新本地数据                              
				if(dbMap.containsKey(firewall.getFwId())){                       
					updateCloudFirewallFromStack(firewall);            
				}                                                                    
				else{                                                                
					fireWallDao.saveOrUpdate(firewall);                                  
				}                                                                    
				stackMap.put(firewall.getFwId(), firewall);
				syncProgressUtil.incrResourceDone(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.FIREWALL);
			}                                                                      
		}                                                                        
		                                                                         
		if(null!=dbList){                                                        
			for(BaseCloudFireWall firewall:dbList){                                           
				//删除本地数据库中不存在于底层的数据                                 
				if(!stackMap.containsKey(firewall.getFwId())){                           
					fireWallDao.delete(firewall.getFwId());   
					ecmcLogService.addLog("同步资源清除数据",  toType(firewall), firewall.getFwName(), firewall.getPrjId(),1,firewall.getFwId(),null);
					JSONObject json = new JSONObject();
					json.put("resourceType", ResourceSyncConstant.FIREWALL);
					json.put("resourceId", firewall.getFwId());
					json.put("resourceName", firewall.getFwName());
					json.put("synTime", new Date());
					jedisUtil.push(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, json.toJSONString());
				}                                                                    
			}                                                                      
		}  
	}
	
	/**
	 * 删除底层不存在的防火墙
	 * @param cloudFw
	 * @return
	 */
	public boolean deleteFw (CloudFireWall cloudFw){
		boolean flag = false;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" delete from cloud_firewall ");
			sql.append(" where fw_id = ? ");
			
			fireWallDao.execSQL(sql.toString(), new Object[]{
				cloudFw.getFwId()
			});
			flag = true;
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			flag = false;
		}
		return flag;
	}
	private String toType(BaseCloudFireWall cfp){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StringBuffer resourceType = new StringBuffer();
		resourceType.append(ResourceSyncConstant.FIREWALL);
		if(null != cfp && null != cfp.getCreateTime()){
			resourceType.append(ResourceSyncConstant.SEPARATOR).append("创建时间：").append(sdf.format(cfp.getCreateTime()));
		}
		
		return resourceType.toString();
	}
}
