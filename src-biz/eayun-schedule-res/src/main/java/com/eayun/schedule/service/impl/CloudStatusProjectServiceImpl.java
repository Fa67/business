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
import com.eayun.eayunstack.model.Role;
import com.eayun.eayunstack.model.User;
import com.eayun.eayunstack.service.OpenstackTenantService;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.project.service.ProjectService;
import com.eayun.schedule.service.CloudProjectService;
import com.eayun.virtualization.model.BaseCloudProject;

@Transactional
@Service
public class CloudStatusProjectServiceImpl implements CloudProjectService {
    private static final Logger log = LoggerFactory.getLogger(CloudStatusProjectServiceImpl.class);
	@Autowired
	private OpenstackTenantService openstackService ;
	@Autowired
	private ProjectService projectService ;
	@Autowired
	private EcmcLogService ecmcLogService;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
    private SyncProgressUtil syncProgressUtil;

	@Override
	public void synchData(BaseDcDataCenter dataCenter) throws Exception{
		Map<String,BaseCloudProject> dbMap=new HashMap<String,BaseCloudProject>();             
		Map<String,BaseCloudProject> stackMap=new HashMap<String,BaseCloudProject>();      

		// 获取虚拟化平台"admin"角色
		Role role = openstackService.getRole(dataCenter, "admin");
		// 获取指定数据中心下面管理员
		User user = openstackService.getUserByName(dataCenter,dataCenter.getVCenterUsername());
		
		List<BaseCloudProject> dbList=queryCloudProjectListByDcId(dataCenter.getId());
		List<BaseCloudProject> list = openstackService.getStackList(dataCenter);
		                                                                         
		if(null!=dbList){                                                        
			for(BaseCloudProject project:dbList){                                           
				dbMap.put(project.getProjectId(), project);                                      
			}                                                                      
		}                                                                        
		long total = list == null ? 0L : list.size();
        syncProgressUtil.initResourceTotal(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.PROJECT, total);                                                                         
		if(null!=list){                                                          
			for(BaseCloudProject project:list){                                     
				//底层数据存在本地数据库中 更新本地数据                              
				if(dbMap.containsKey(project.getProjectId())){                       
					updateCloudProjectFromStack(project);            
				}                                                                    
				else{                                                                
					openstackService.bindAdminToDcmanager(dataCenter,role, user, project.getProjectId());
					project.setIsSynchronized("1");
					projectService.save(project); 
				}                                                                    
				stackMap.put(project.getProjectId(), project);                   
				syncProgressUtil.incrResourceDone(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.PROJECT);
			}                                                                      
		}                                                                        
		                                                                         
		if(null!=dbList){                                                        
			for(BaseCloudProject project:dbList){                                           
				//删除本地数据库中不存在于底层的数据                                 
				if(!stackMap.containsKey(project.getProjectId())){                           
					projectService.delete(project.getProjectId());
					ecmcLogService.addLog("同步资源清除数据",  toType(project), project.getPrjName(), project.getProjectId(),1,project.getProjectId(),null);
					
					JSONObject json = new JSONObject();
					json.put("resourceType", ResourceSyncConstant.PROJECT);
					json.put("resourceId", project.getProjectId());
					json.put("resourceName", project.getPrjName());
					json.put("synTime", new Date());
					jedisUtil.push(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, json.toJSONString());
				}                                                                    
			}                                                                      
		}
		
		
	}
	
	private String toType(BaseCloudProject project){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StringBuffer resourceType = new StringBuffer();
		resourceType.append(ResourceSyncConstant.PROJECT);
		if(null != project && null != project.getCreateDate()){
			resourceType.append(ResourceSyncConstant.SEPARATOR);
			resourceType.append("创建时间：").append(sdf.format(project.getCreateDate()));
		}
		
		return resourceType.toString();
	}
	
	public List<BaseCloudProject> queryCloudProjectListByDcId (String dcId){
		StringBuffer hql = new StringBuffer();
		hql.append(" from  BaseCloudProject ");
		hql.append(" where dcId = ? ");
		
		return projectService.find(hql.toString(), new Object[]{dcId});
	}
	
	public boolean updateCloudProjectFromStack(BaseCloudProject project){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_project set ");
			sql.append("	prj_name = ? ,          ");
			sql.append("	prj_desc = ?,          ");
			sql.append("	dc_id = ?,             ");
			sql.append("	cpu_count = ?,         ");
			sql.append("	host_count = ?,        ");
			sql.append("	disk_count = ?,        ");
			sql.append("	disk_snapshot = ?,     ");
			sql.append("	disk_capacity = ?,     ");
			sql.append("	memory = ?,            ");
			sql.append("	net_work = ?,          ");
			sql.append("	subnet_count = ?,      ");
			sql.append("	outerip = ?,           ");
			sql.append("	route_count = ?,           ");
			sql.append("	safe_group = ?,           ");
			sql.append("	quota_pool = ?,           ");
			sql.append("	is_synchronized = ?    ");
			sql.append(" where prj_id  = ? ");
			
			projectService.execSQL(sql.toString(), new Object[]{
					project.getPrjName(),
					project.getProjectDesc(),
					project.getDcId(),
					project.getCpuCount(),
					project.getHostCount(),
					project.getDiskCount(),
					project.getDiskSnapshot(),
					project.getDiskCapacity(),
					project.getMemory(),
					project.getNetWork(),
					project.getSubnetCount(),
					project.getOuterIP(),
					project.getRouteCount(),
					project.getSafeGroup(),
					project.getQuotaPool(),
					"0",
					project.getProjectId()
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
	public List<BaseCloudProject> getAllProjectsByDcId(String dcId) {
		
		return queryCloudProjectListByDcId(dcId);
	}

	
}
