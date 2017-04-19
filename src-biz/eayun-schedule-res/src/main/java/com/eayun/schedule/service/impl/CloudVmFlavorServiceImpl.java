package com.eayun.schedule.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.constant.SyncProgress.SyncByDatacenterTypes;
import com.eayun.common.constant.SyncProgress.SyncType;
import com.eayun.common.sync.SyncProgressUtil;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.service.OpenstackFlavorService;
import com.eayun.schedule.service.CloudVmFlavorService;
import com.eayun.virtualization.dao.CloudFlavorDao;
import com.eayun.virtualization.model.BaseCloudFlavor;

@Transactional
@Service
public class CloudVmFlavorServiceImpl implements CloudVmFlavorService {
    private static final Logger log = LoggerFactory.getLogger(CloudVmFlavorServiceImpl.class);
	@Autowired
	private OpenstackFlavorService openstackService;
	@Autowired
	private CloudFlavorDao cloudFlavorDao ;
	@Autowired
    private SyncProgressUtil syncProgressUtil;
	
	@Override
	public void synchData(BaseDcDataCenter dataCenter) {

		Map<String,BaseCloudFlavor> dbMap=new HashMap<String,BaseCloudFlavor>();             
		Map<String,BaseCloudFlavor> stackMap=new HashMap<String,BaseCloudFlavor>(); 
		
		List<BaseCloudFlavor> dbList=queryCloudFlavorListByDcId(dataCenter.getId());
		List<BaseCloudFlavor> list=openstackService.getStackList(dataCenter);
		
		                                                                         
		if(null!=dbList){                                                        
			for(BaseCloudFlavor flavor:dbList){                                           
				dbMap.put(flavor.getFlavorId(), flavor);                                      
			}                                                                      
		}                                                                        
		long total = list == null ? 0L : list.size();
        syncProgressUtil.initResourceTotal(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.FLAVOR, total);                                                                         
		if(null!=list){                                                          
			for(BaseCloudFlavor cfi:list){                                     
				//底层数据存在本地数据库中 更新本地数据                              
				if(dbMap.containsKey(cfi.getFlavorId())){                       
					updateCloudFlavorByStack(cfi);            
				}                                                                    
				else{
					cfi.setId(UUID.randomUUID().toString());
					cloudFlavorDao.save(cfi);                                  
				}                                                                    
				stackMap.put(cfi.getFlavorId(), cfi);                   
				syncProgressUtil.incrResourceDone(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.FLAVOR);
			}                                                                      
		}                                                                        
		                                                                         
		if(null!=dbList){                                                        
			for(BaseCloudFlavor cfi:dbList){                                           
				//删除本地数据库中不存在于底层的数据                                 
				if(!stackMap.containsKey(cfi.getFlavorId())){                           
					cloudFlavorDao.delete(cfi.getId());                                    
				}                                                                    
			}                                                                      
		}  
	
	}
	
	@SuppressWarnings("unchecked")
	public List<BaseCloudFlavor> queryCloudFlavorListByDcId (String dcId){
		StringBuffer hql = new StringBuffer();
		hql.append(" from BaseCloudFlavor  ");
		hql.append(" where dcId = ? ");
		
		return cloudFlavorDao.find(hql.toString(), new Object[]{dcId});
	}
	
	public boolean updateCloudFlavorByStack(BaseCloudFlavor flavor){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append("  update  cloud_flavor set   ");
			sql.append("  flavor_name = ?  , ");
			sql.append("  flavor_vcpus = ?  , ");
			sql.append("  flavor_ram = ?  , ");
			sql.append("  flavor_disk = ?   ");
			sql.append("  where dc_id =? and flavor_id = ?   ");
			
			cloudFlavorDao.execSQL(sql.toString(), new Object[]{
					flavor.getFlavorName(),
					flavor.getFlavorVcpus(),
					flavor.getFlavorRam(),
					flavor.getFlavorDisk(),
					flavor.getDcId(),
					flavor.getFlavorId()
			});
			flag = true ;
		}catch(Exception e){
			flag = false;
			log.error(e.getMessage(),e);
			throw e;
		}
		
		return flag ;
	}

}
