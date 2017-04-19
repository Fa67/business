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
import com.eayun.common.constant.SyncProgress.SyncByProjectTypes;
import com.eayun.common.constant.SyncProgress.SyncType;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.sync.SyncProgressUtil;
import com.eayun.common.util.CloudResourceUtil;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.service.OpenstackFloatIpService;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.schedule.service.CloudFloatIpService;
import com.eayun.virtualization.dao.CloudFloatIpDao;
import com.eayun.virtualization.model.BaseCloudFloatIp;

@Transactional
@Service
public class CloudStatusFloatIpServiceImpl implements CloudFloatIpService{
    private static final Logger log = LoggerFactory.getLogger(CloudStatusFloatIpServiceImpl.class);
    @Autowired
    private JedisUtil jedisUtil;
	@Autowired
	private OpenstackFloatIpService openstackService ;
	@Autowired
	private CloudFloatIpDao cloudFloatIpDao;
	@Autowired
    private EcmcLogService ecmcLogService;
	@Autowired
	private SyncProgressUtil syncProgressUtil;
	@Override
	public void synchData(BaseDcDataCenter dataCenter, String prjId) throws Exception{
		Map<String,BaseCloudFloatIp> dbMap=new HashMap<String,BaseCloudFloatIp>();             
		Map<String,BaseCloudFloatIp> stackMap=new HashMap<String,BaseCloudFloatIp>();      
		List<BaseCloudFloatIp> dbList=queryCloudFloatipListByPrjId(prjId);
		List<BaseCloudFloatIp> list=openstackService.getStackList(dataCenter, prjId);
		                                                                         
		if(null!=dbList){                                                        
			for(BaseCloudFloatIp cfi:dbList){                                           
				dbMap.put(cfi.getFloId(), cfi);                                      
			}                                                                      
		}
		long total = list == null ? 0L : list.size();
        syncProgressUtil.initResourceTotal(dataCenter.getId(), SyncType.PROJECT, SyncByProjectTypes.FLOAT_IP, total);                                                                       
		if(null!=list){                                                          
			for(BaseCloudFloatIp cfi:list){                                     
				//底层数据存在本地数据库中 更新本地数据                              
				if(dbMap.containsKey(cfi.getFloId())){                       
					updateCloudFloatipFromStack(cfi);            
				}                                                                    
				else{                                                                
					cfi.setIsDeleted("0");
					cfi.setIsVisable("0");
					cloudFloatIpDao.save(cfi);                                  
				}                                                                    
				stackMap.put(cfi.getFloId(), cfi);
				syncProgressUtil.incrResourceDone(dataCenter.getId(), SyncType.PROJECT, SyncByProjectTypes.FLOAT_IP);
			}                                                                      
		}                                                                        
		                                                                         
		if(null!=dbList){                                                        
			for(BaseCloudFloatIp cfi:dbList){                                           
				//删除本地数据库中不存在于底层的数据                                 
				if(!stackMap.containsKey(cfi.getFloId()) && "0".equals(cfi.getIsDeleted())){                           
					deleteFloatIp(cfi);
					ecmcLogService.addLog("同步资源清除数据", toType(cfi), cfi.getFloIp(), cfi.getPrjId(), 1, cfi.getFloId(), null);
					
					JSONObject json = new JSONObject();
					json.put("resourceType", ResourceSyncConstant.FLOATIP);
					json.put("resourceId", cfi.getFloId());
					json.put("resourceName", cfi.getFloIp());
					json.put("synTime", new Date());
					jedisUtil.push(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, json.toJSONString());
				}                                                                    
			}                                                                      
		}  
	}

	
	@SuppressWarnings("unchecked")
	public List<BaseCloudFloatIp> queryCloudFloatipListByPrjId(String prjId){
		StringBuffer hql = new StringBuffer();
		hql.append(" from BaseCloudFloatIp ");
		hql.append(" where prjId = ? ");
		
		return cloudFloatIpDao.find(hql.toString(), new Object []{prjId});
	}
	
	
	public boolean updateCloudFloatipFromStack(BaseCloudFloatIp cfi){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_floatip set ");
			sql.append("	flo_ip = ?,         ");    
			sql.append("	prj_id = ?,         ");    
			sql.append("	dc_id = ?,          ");    
			sql.append("	resource_id = ?,    ");    
			sql.append("	resource_type = ?,  ");    
			sql.append("	is_deleted = ?  ");    
			sql.append(" where flo_id = ? ");
			
			cloudFloatIpDao.execSQL(sql.toString(), new Object[]{
					cfi.getFloIp(),
					cfi.getPrjId(),
					cfi.getDcId(),
					cfi.getResourceId(),
					cfi.getResourceType(),
					"0",
					cfi.getFloId()
			});
			flag = true ;
		}catch(Exception e){
			flag = false;
			log.error(e.getMessage(),e);
			throw e;
		}
		
		return flag ;
	}
	
	public boolean deleteFloatIp(BaseCloudFloatIp cfi){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_floatip set ");
			sql.append("	delete_time = ?,  ");    
			sql.append("	is_deleted = ?  ");    
			sql.append(" where flo_id = ? ");
			
			cloudFloatIpDao.execSQL(sql.toString(), new Object[]{
					new Date(),
					"1",
					cfi.getFloId()
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
	 * @param cfi
	 * @return
	 */
	private String toType(BaseCloudFloatIp cfi) {
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuffer resourceType = new StringBuffer();
        resourceType.append(ResourceSyncConstant.FLOATIP);
        resourceType.append("-").append(CloudResourceUtil.escapePayType(cfi.getPayType()));
        if(null != cfi && null != cfi.getCreateTime()){
        	resourceType.append(ResourceSyncConstant.SEPARATOR).append("创建时间：").append(sdf.format(cfi.getCreateTime()));
        }
        if (PayType.PAYBEFORE.equals(cfi.getPayType())) {
            resourceType.append(ResourceSyncConstant.SEPARATOR).append("到期时间：").append(sdf.format(cfi.getEndTime()));
        }
        return resourceType.toString();
	}
}
