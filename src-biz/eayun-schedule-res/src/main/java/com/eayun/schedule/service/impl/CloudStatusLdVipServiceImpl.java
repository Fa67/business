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
import com.eayun.common.util.StringUtil;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.service.OpenstackVipService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.schedule.service.CloudLdVipService;
import com.eayun.virtualization.dao.CloudLdVipDao;
import com.eayun.virtualization.model.BaseCloudLdVip;
import com.eayun.virtualization.model.CloudLdVip;
import com.eayun.virtualization.service.PoolService;

@Transactional
@Service
public class CloudStatusLdVipServiceImpl implements CloudLdVipService{
    private static final Logger log = LoggerFactory.getLogger(CloudStatusLdVipServiceImpl.class);
    @Autowired
    private JedisUtil jedisUtil; 
	@Autowired
	private OpenstackVipService openStackVimService;
	@Autowired
	private CloudLdVipDao vipDao;
	@Autowired
    private EcmcLogService ecmcLogService;
	@Autowired
	private PoolService poolService;
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
			JSONObject json = openStackVimService.get(valueJson.getString("dcId"),
					valueJson.getString("vipId"));
			if(null!=json){
				String jsonStr = json.toJSONString();
				boolean isDeleted=jsonStr.contains("NotFound");
				if(!isDeleted){
					result=json.getJSONObject(OpenstackUriConstant.VIP_DATA_NAME);
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
	 * 修改负载均衡VIP信息
	 * @param cloudVm
	 * @return
	 */
	public boolean  updateLdv(CloudLdVip cloudLdv) throws Exception{
		boolean flag = false ;
		try{
			BaseCloudLdVip ldv = vipDao.findOne(cloudLdv.getVipId());
			ldv.setVipStatus(cloudLdv.getVipStatus());
			vipDao.saveOrUpdate(ldv);
			if("ACTIVE".equals(cloudLdv.getVipStatus())){
				String pool=jedisUtil.get(RedisKey.CLOUDLDPOOLSYNC+ldv.getPoolId());
				JSONObject poolObj=new JSONObject();
				if(!StringUtil.isEmpty(pool)){
					poolObj=JSONObject.parseObject(pool);
				}
				if(poolObj!=null&&"ACTIVE".equals(poolObj.getString("poolStatus"))){
					poolService.poolCreateSuccessHandleForSync(poolObj.getString("poolId"), poolObj.getString("orderNo"), poolObj.getString("cusId"), poolObj.getLongValue("connectionLimit"));
				}else{
					jedisUtil.set(RedisKey.CLOUDLDVIPSYNC+cloudLdv.getVipId(), "ACTIVE");
				}
			}
			flag = true ;
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			flag = false;
			throw e;
		}
		return flag ;
	}

	@Override
	public void synchData(BaseDcDataCenter dataCenter) throws Exception {
		Map<String,BaseCloudLdVip> dbMap=new HashMap<String,BaseCloudLdVip>();             
		Map<String,BaseCloudLdVip> stackMap=new HashMap<String,BaseCloudLdVip>();      
		List<BaseCloudLdVip> dbList=queryCloudLdvipListByDcId(dataCenter.getId());
		List<BaseCloudLdVip> list=openStackVimService.getStackList(dataCenter);
		                                                                         
		if(null!=dbList){                                                        
			for(BaseCloudLdVip cfr:dbList){                                           
				dbMap.put(cfr.getVipId(), cfr);                                      
			}                                                                      
		}                                                                        
		long total = list == null ? 0L : list.size();
        syncProgressUtil.initResourceTotal(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.LB_VIP, total);                                                                         
		if(null!=list){                                                          
			for(BaseCloudLdVip cloudFwRule:list){                                     
				//底层数据存在本地数据库中 更新本地数据                              
				if(dbMap.containsKey(cloudFwRule.getVipId())){                       
					updateCloudLdvipFromStack(cloudFwRule);            
				}                                                                    
				else{                                                                
					vipDao.save(cloudFwRule);                                  
				}                                                                    
				stackMap.put(cloudFwRule.getVipId(), cloudFwRule);                   
				syncProgressUtil.incrResourceDone(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.LB_VIP);
			}                                                                      
		}                                                                        
		                                                                         
		if(null!=dbList){                                                        
			for(BaseCloudLdVip cfr:dbList){                                           
				//删除本地数据库中不存在于底层的数据                                 
				if(!stackMap.containsKey(cfr.getVipId())){                           
					vipDao.delete(cfr.getVipId());
					ecmcLogService.addLog("同步资源清除数据", toType(cfr), cfr.getVipName(), cfr.getPrjId(), 1, cfr.getVipId(), null);
					
					JSONObject json = new JSONObject();
					json.put("resourceType", ResourceSyncConstant.LDVIP);
					json.put("resourceId", cfr.getVipId());
					json.put("resourceName", cfr.getVipName());
					json.put("synTime", new Date());
					jedisUtil.push(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, json.toJSONString());
				}                                                                    
			}                                                                      
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<BaseCloudLdVip> queryCloudLdvipListByDcId (String dcId){
		StringBuffer hql = new StringBuffer();
		hql.append(" from  BaseCloudLdVip ");
		hql.append(" where dcId = ? ");
		
		return vipDao.find(hql.toString(), new Object[]{dcId});
	}
	
	public boolean updateCloudLdvipFromStack(BaseCloudLdVip cldv){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_ldvip set ");
			sql.append("	vip_name = ?,          "); 
			sql.append("	subnet_id = ?,         "); 
			sql.append("	pool_id = ?,           "); 
			sql.append("	prj_id = ?,            "); 
			sql.append("	dc_id = ?,             "); 
			sql.append("	protocol_port =? ,     "); 
			sql.append("	vip_protocol = ?,      "); 
			sql.append("	vip_status = ?,        "); 
			sql.append("	connection_limit = ?,  "); 
			sql.append("	admin_stateup = ?,     ");  
			sql.append("	port_id = ?,     ");  
			sql.append("	vip_address = ?        ");  
			sql.append(" where vip_id = ? ");
			
			
			vipDao.execSQL(sql.toString(), new Object[]{
					cldv.getVipName(),
					cldv.getSubnetId(),
					cldv.getPoolId(),
					cldv.getPrjId(),
					cldv.getDcId(),
					cldv.getProtocolPort(),
					cldv.getVipProtocol(),
					cldv.getVipStatus(),
					cldv.getConnectionLimit(),
					cldv.getAdminStateup()+"",
					cldv.getPortId(),
					cldv.getVipAddress(),
					cldv.getVipId()	
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
	 * 删除底层不存在的负载均衡VIP
	 * @param cloudVip
	 * @return
	 */
	public boolean deleteVip (CloudLdVip cloudVip){
		boolean flag = false;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" delete from cloud_ldvip ");
			sql.append(" where vip_id = ? ");
			
			vipDao.execSQL(sql.toString(), new Object[]{
				cloudVip.getVipId()	
			});
			flag = true;
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			flag = false;
		}
		return flag;
	}
	
	private String toType(BaseCloudLdVip vip) {
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuffer resourceType = new StringBuffer();
        resourceType.append(ResourceSyncConstant.LDVIP);
        if(null != vip && null != vip.getCreateTime()){
        	resourceType.append(ResourceSyncConstant.SEPARATOR).append("创建时间：").append(sdf.format(vip.getCreateTime()));
        }
        return resourceType.toString();
	}
}
