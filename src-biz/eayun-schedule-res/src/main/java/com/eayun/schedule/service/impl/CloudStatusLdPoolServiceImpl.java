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
import com.eayun.eayunstack.service.OpenstackPoolService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.schedule.service.CloudLdPoolService;
import com.eayun.virtualization.dao.CloudLdPoolDao;
import com.eayun.virtualization.model.BaseCloudLdPool;
import com.eayun.virtualization.model.CloudLdPool;
import com.eayun.virtualization.service.PoolService;

@Transactional
@Service
public class CloudStatusLdPoolServiceImpl implements CloudLdPoolService{
    private static final Logger log = LoggerFactory.getLogger(CloudStatusLdPoolServiceImpl.class);
    @Autowired
    private JedisUtil jedisUtil; 
	@Autowired
	private OpenstackPoolService openStackPoolService;
	@Autowired
	private CloudLdPoolDao cloudLdPoolDao;
	@Autowired
	private PoolService poolService;
	@Autowired
    private EcmcLogService ecmcLogService;
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
			JSONObject json = openStackPoolService.get(valueJson.getString("dcId"), 
					valueJson.getString("poolId"));
			if(null!=json){
				String jsonStr = json.toJSONString();
				boolean isDeleted=jsonStr.contains("NotFound");
				if(!isDeleted){
					result=json.getJSONObject(OpenstackUriConstant.POOL_DATA_NAME);
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
	 * 修改负载均衡资源池信息
	 * @param cloudVm
	 * @return
	 */
	public boolean  updateLdp(CloudLdPool cloudLdp) throws Exception {
		boolean flag = false ;
		try{
			String vipStatus=jedisUtil.get(RedisKey.CLOUDLDVIPSYNC+cloudLdp.getVipId());
			BaseCloudLdPool ldp = cloudLdPoolDao.findOne(cloudLdp.getPoolId());
			if ("ACTIVE".equals(cloudLdp.getPoolStatus()) && "PENDING_CREATE".equals(ldp.getPoolStatus())) {
				if("ACTIVE".equals(vipStatus)){
					ldp.setIsVisible("1");
					poolService.poolCreateSuccessHandle(cloudLdp.getDcId(), 
							cloudLdp.getOrderNo(), 
							cloudLdp.getCusId(), 
							ldp.getPoolId(), 
							ldp.getPoolName(), 
							cloudLdp.getConnectionLimit(), 
							ldp.getPayType());
					jedisUtil.delete(RedisKey.CLOUDLDVIPSYNC+cloudLdp.getVipId());
				}else{
					cloudLdp.setPoolStatus("PENDING_CREATE");
					jedisUtil.push(RedisKey.ldPoolKey,JSONObject.toJSONString(cloudLdp));
				}
			}
			ldp.setPoolStatus(cloudLdp.getPoolStatus());
			cloudLdPoolDao.saveOrUpdate(ldp);
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
		Map<String,BaseCloudLdPool> dbMap=new HashMap<String,BaseCloudLdPool>();             
		Map<String,BaseCloudLdPool> stackMap=new HashMap<String,BaseCloudLdPool>();      
		List<BaseCloudLdPool> dbList=queryCloudLdpoolListByDcId(dataCenter.getId());
		List<BaseCloudLdPool> list=openStackPoolService.getStackList(dataCenter);
		                                                                         
		if(null!=dbList){                                                        
			for(BaseCloudLdPool cldp:dbList){                                           
				dbMap.put(cldp.getPoolId(), cldp);                                      
			}                                                                      
		}                                                                        
		long total = list == null ? 0L : list.size();
        syncProgressUtil.initResourceTotal(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.LB_POOL, total);                                                                         
		if(null!=list){                                                          
			for(BaseCloudLdPool cldp:list){                                     
				//底层数据存在本地数据库中 更新本地数据                              
				if(dbMap.containsKey(cldp.getPoolId())){                       
					updateCloudLdpoolFromStack(cldp);            
				}                                                                    
				else{
				    cldp.setIsVisible("0");
					cloudLdPoolDao.save(cldp);                                  
				}                                                                    
				stackMap.put(cldp.getPoolId(), cldp);                   
				syncProgressUtil.incrResourceDone(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.LB_POOL);
			}                                                                      
		}                                                                        
		                                                                         
		if (null != dbList) {
			for (BaseCloudLdPool cldp : dbList) {
				//删除本地数据库中不存在于底层的数据                                 
				if (!stackMap.containsKey(cldp.getPoolId())) {
					cloudLdPoolDao.delete(cldp.getPoolId());
					ecmcLogService.addLog("同步资源清除数据", toType(cldp), cldp.getPoolName(), cldp.getPrjId(), 1, cldp.getPoolId(), null);
					
					JSONObject json = new JSONObject();
					json.put("resourceType", ResourceSyncConstant.LDPOOL);
					json.put("resourceId", cldp.getPoolId());
					json.put("resourceName", cldp.getPoolName());
					json.put("synTime", new Date());
					jedisUtil.push(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, json.toJSONString());
				}
			}
		}  
	}
	
	@SuppressWarnings("unchecked")
	public List<BaseCloudLdPool>  queryCloudLdpoolListByDcId(String dcId){
		StringBuffer hql = new StringBuffer();
		hql.append(" from  BaseCloudLdPool ");
		hql.append(" where dcId = ? ");
		
		return cloudLdPoolDao.find(hql.toString(), new Object[]{dcId});
	}
	
	public boolean updateCloudLdpoolFromStack(BaseCloudLdPool cldp){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_ldpool set ");
			sql.append("	pool_name = ?,         ");
			sql.append("	prj_id =? ,            ");
			sql.append("	dc_id = ?,             ");
			sql.append("	pool_description = ?,  ");
			sql.append("	pool_provider = ?,     ");
			sql.append("	subnet_id = ?,         ");
			sql.append("	vip_id =? ,            ");
			sql.append("	pool_protocol =? ,     ");
			sql.append("	lb_method = ?,         ");
			sql.append("	pool_status = ?,       ");
			sql.append("	admin_stateup = ?     "); 
			sql.append(" where pool_id = ? ");
			
			cloudLdPoolDao.execSQL(sql.toString(), new Object[]{
					cldp.getPoolName(),
					cldp.getPrjId(),
					cldp.getDcId(),
					cldp.getPoolDescription(),
					cldp.getPoolProvider(),
					cldp.getSubnetId(),
					cldp.getVipId(),
					cldp.getPoolProtocol(),
					cldp.getLbMethod(),
					cldp.getPoolStatus(),
					cldp.getAdminStateup()+"",
					cldp.getPoolId()
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
	 * 删除底层不存在的负载均衡的资源池
	 * @param cloudPool
	 * @return
	 */
	public boolean deletePool (CloudLdPool cloudPool){
		boolean flag = false;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" delete from cloud_ldpool ");
			sql.append(" where pool_id = ? ");
			
			cloudLdPoolDao.execSQL(sql.toString(), new Object[]{
				cloudPool.getPoolId()
			});
			flag = true;
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			flag = false;
		}
		return flag;
	}
	/**
	 * 拼装同步删除发送日志的资源类型
	 * @author gaoxiang
	 * @param pool
	 * @return
	 */
	private String toType(BaseCloudLdPool pool) {
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuffer resourceType = new StringBuffer();
        resourceType.append(ResourceSyncConstant.LDPOOL);
        resourceType.append("-").append(CloudResourceUtil.escapePayType(pool.getPayType()));
        if(null != pool && null != pool.getCreateTime()){
        	resourceType.append(ResourceSyncConstant.SEPARATOR).append("创建时间：").append(sdf.format(pool.getCreateTime()));
        }
        if (PayType.PAYBEFORE.equals(pool.getPayType())) {
            resourceType.append(ResourceSyncConstant.SEPARATOR).append("到期时间：").append(sdf.format(pool.getEndTime()));
        }
        return resourceType.toString();
	}
}
