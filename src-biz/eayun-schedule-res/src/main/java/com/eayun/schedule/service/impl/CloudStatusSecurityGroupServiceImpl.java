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
import com.eayun.eayunstack.service.OpenstackSecurityGroupService;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.schedule.service.CloudSecurityGroupService;
import com.eayun.virtualization.dao.CloudSecurityGroupDao;
import com.eayun.virtualization.dao.CloudSecurityGroupRuleDao;
import com.eayun.virtualization.model.BaseCloudSecurityGroup;
import com.eayun.virtualization.model.BaseCloudSecurityGroupRule;

@Transactional
@Service
public class CloudStatusSecurityGroupServiceImpl implements CloudSecurityGroupService {
    private static final Logger log = LoggerFactory.getLogger(CloudStatusSecurityGroupServiceImpl.class);
    @Autowired
    private JedisUtil jedisUtil; 
	@Autowired
	private OpenstackSecurityGroupService openstackService;
	@Autowired
	private CloudSecurityGroupDao cloudSecurityDao;
	@Autowired
	private CloudSecurityGroupRuleDao cloudSecurityRuleDao ;
	@Autowired
	private EcmcLogService ecmcLogService;
	@Autowired
    private SyncProgressUtil syncProgressUtil;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void synchData(BaseDcDataCenter dataCenter) throws Exception{
		Map<String,List> map = openstackService.getStackList(dataCenter);
		List<BaseCloudSecurityGroup> groupList=map.get("GroupList");
		List<BaseCloudSecurityGroupRule> ruleList=map.get("RuleList");
		synchSecurityGroup(groupList,dataCenter.getId());
		syncProgressUtil.incrByDatacenterDone(dataCenter.getId());
		
		synchSecurityRule(ruleList,dataCenter.getId());
		syncProgressUtil.incrByDatacenterDone(dataCenter.getId());
	}

	/**
	 * 同步安全组
	 * @author zhouhaitao
	 * @param groupList
	 * @throws Exception 
	 */
	private void synchSecurityGroup(List<BaseCloudSecurityGroup> groupList,String dcId) throws Exception{
		Map<String,BaseCloudSecurityGroup> dbMap=new HashMap<String,BaseCloudSecurityGroup>();             
		Map<String,BaseCloudSecurityGroup> stackMap=new HashMap<String,BaseCloudSecurityGroup>();      
		List<BaseCloudSecurityGroup> dbList=queryCloudSecurityGroupListByDcId(dcId);
		                                                                         
		if(null!=dbList){                                                        
			for(BaseCloudSecurityGroup cgs:dbList){                                           
				dbMap.put(cgs.getSgId(), cgs);                                      
			}                                                                      
		}                                                                        
		long total = groupList == null ? 0L : groupList.size();
        syncProgressUtil.initResourceTotal(dcId, SyncType.DATA_CENTER, SyncByDatacenterTypes.SECURITY_GROUP, total);                                                                         
		if(null!=groupList){                                                          
			for(BaseCloudSecurityGroup csg:groupList){                                     
				//底层数据存在本地数据库中 更新本地数据                              
				if(dbMap.containsKey(csg.getSgId())){                       
					updateCloudSecurityGroupFromStack(csg);            
				}                                                                    
				else{                                                                
					cloudSecurityDao.save(csg);                                  
				}                                                                    
				stackMap.put(csg.getSgId(), csg);                   
				syncProgressUtil.incrResourceDone(dcId, SyncType.DATA_CENTER, SyncByDatacenterTypes.SECURITY_GROUP);
			}                                                                      
		}                                                                        
		                                                                         
		if(null!=dbList){                                                        
			for(BaseCloudSecurityGroup csg:dbList){                                           
				//删除本地数据库中不存在于底层的数据                                 
				if(!stackMap.containsKey(csg.getSgId())){                           
					cloudSecurityDao.delete(csg.getSgId());  
					ecmcLogService.addLog("同步资源清除数据",  toType(csg), csg.getSgName(), csg.getPrjId(),1,csg.getSgId(),null);
					JSONObject json = new JSONObject();
					json.put("resourceType", ResourceSyncConstant.SECURITYGROUP);
					json.put("resourceId", csg.getSgId());
					json.put("resourceName", csg.getSgName());
					json.put("synTime", new Date());
					jedisUtil.push(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, json.toJSONString());
				}                                                                    
			}                                                                      
		}  
	}
	/**
	 * 同步安全组规则
	 * @param ruleList
	 * @throws Exception 
	 */
	private void synchSecurityRule(List<BaseCloudSecurityGroupRule> ruleList,String dcId) throws Exception{
		Map<String,BaseCloudSecurityGroupRule> dbMap=new HashMap<String,BaseCloudSecurityGroupRule>();             
		Map<String,BaseCloudSecurityGroupRule> stackMap=new HashMap<String,BaseCloudSecurityGroupRule>();      
		List<BaseCloudSecurityGroupRule> dbList=queryCloudSecurityRuleListByDcId(dcId);
		                                                                         
		if(null!=dbList){                                                        
			for(BaseCloudSecurityGroupRule cgr:dbList){                                           
				dbMap.put(cgr.getSgrId(), cgr);                                      
			}                                                                      
		}                                                                        
		long total = ruleList == null ? 0L : ruleList.size();
        syncProgressUtil.initResourceTotal(dcId, SyncType.DATA_CENTER, SyncByDatacenterTypes.SECURITY_GROUP_RULE, total);                                                                      
		if(null!=ruleList){                                                          
			for(BaseCloudSecurityGroupRule cloudFwRule:ruleList){                                     
				//底层数据存在本地数据库中 更新本地数据                              
				if(dbMap.containsKey(cloudFwRule.getSgrId())){                       
					updateCloudSecurityRuleFromStack(cloudFwRule);            
				}                                                                    
				else{                                                                
					cloudSecurityRuleDao.save(cloudFwRule);                                  
				}                                                                    
				stackMap.put(cloudFwRule.getSgrId(), cloudFwRule);                   
				syncProgressUtil.incrResourceDone(dcId, SyncType.DATA_CENTER, SyncByDatacenterTypes.SECURITY_GROUP_RULE);
			}                                                                      
		}                                                                        
		                                                                         
		if(null!=dbList){                                                        
			for(BaseCloudSecurityGroupRule cfr:dbList){                                           
				//删除本地数据库中不存在于底层的数据                                 
				if(!stackMap.containsKey(cfr.getSgrId())){                           
					cloudSecurityRuleDao.delete(cfr.getSgrId());
					ecmcLogService.addLog("同步资源清除数据",  toType(cfr), cfr.getCreateName(), cfr.getPrjId(),1,cfr.getSgrId(),null);
					JSONObject json = new JSONObject();
					json.put("resourceType", ResourceSyncConstant.SECURITYGROUPRULE);
					json.put("resourceId", cfr.getSgrId());
					json.put("resourceName", ResourceSyncConstant.SECURITYGROUPRULE);
					json.put("synTime", new Date());
					jedisUtil.push(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, json.toJSONString());
				}                                                                    
			}                                                                      
		}  
	}
	
	@SuppressWarnings("unchecked")
	public List<BaseCloudSecurityGroup>  queryCloudSecurityGroupListByDcId(String dcId){
		StringBuffer hql = new StringBuffer();
		hql.append(" from  BaseCloudSecurityGroup ");
		hql.append(" where dcId = ? ");
		
		return cloudSecurityDao.find(hql.toString(), new Object[]{dcId});
	}
	
	public boolean updateCloudSecurityGroupFromStack(BaseCloudSecurityGroup csg){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_securitygroup set ");
			sql.append("	sg_name = ?,         ");
			sql.append("	prj_id = ?,          ");
			sql.append("	dc_id = ?,           ");
			sql.append("	sg_description = ?  ");
			sql.append(" where sg_id = ? ");
			
			cloudSecurityDao.execSQL(sql.toString(), new Object[]{
					csg.getSgName(),
					csg.getPrjId(),
					csg.getDcId(),
					csg.getSgDescription(),
					csg.getSgId()
			});
			flag = true ;
		}catch(Exception e){
			flag = false;
			log.error(e.getMessage(),e);
			throw e;
		}
		
		return flag ;
	}
	
	@SuppressWarnings("unchecked")
	public List<BaseCloudSecurityGroupRule> queryCloudSecurityRuleListByDcId (String dcId){
		StringBuffer hql = new StringBuffer();
		hql.append(" from BaseCloudSecurityGroupRule  ");
		hql.append(" where dcId = ? ");
		
		return cloudSecurityRuleDao.find(hql.toString(), new Object[]{dcId});
	}
	
	public boolean updateCloudSecurityRuleFromStack(BaseCloudSecurityGroupRule cfr){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_grouprule set ");
			sql.append("	prj_id = ?,          ");  
			sql.append("	dc_id = ?,           ");  
			sql.append("	sg_id = ?,           ");  
			sql.append("	remote_groupid = ?,  ");  
			sql.append("	direction = ?,       ");  
			sql.append("	ethertype = ?,       ");  
			sql.append("	protocol = ?,         "); 
			sql.append("	port_rangemin = ?,    "); 
			sql.append("	port_rangemax = ?,    "); 
			sql.append("	remote_ipprefix = ?  "); 
			sql.append(" where sgr_id = ? ");
			
			cloudSecurityRuleDao.execSQL(sql.toString(), new Object[]{
					cfr.getPrjId(),
					cfr.getDcId(),
					cfr.getSgId(),
					cfr.getRemoteGroupId(),
					cfr.getDirection(),
					cfr.getEthertype(),
					cfr.getProtocol(),
					cfr.getPortRangeMin(),
					cfr.getPortRangeMax(),
					cfr.getRemoteIpPrefix(),
					cfr.getSgrId()
			});
			flag = true ;
		}catch(Exception e){
			flag = false;
			log.error(e.getMessage(),e);
			throw e;
		}
		
		return flag ;
	}
	
	private String toType(BaseCloudSecurityGroup csg){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StringBuffer resourceType = new StringBuffer();
		resourceType.append(ResourceSyncConstant.SECURITYGROUP);
		if(null != csg && null != csg.getCreateTime()){
			resourceType.append(ResourceSyncConstant.SEPARATOR);
			resourceType.append("创建时间：").append(sdf.format(csg.getCreateTime()));
		}
		
		return resourceType.toString();
	}
	private String toType(BaseCloudSecurityGroupRule rule){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StringBuffer resourceType = new StringBuffer();
		resourceType.append(ResourceSyncConstant.SECURITYGROUPRULE);
		if(null != rule && null != rule.getCreateTime()){
			resourceType.append(ResourceSyncConstant.SEPARATOR);
			resourceType.append("创建时间：").append(sdf.format(rule.getCreateTime()));
		}
		
		return resourceType.toString();
	}
}
