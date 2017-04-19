package com.eayun.schedule.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.datacenter.service.DataCenterService;
import com.eayun.schedule.ScheduleResourceStartup;
import com.eayun.schedule.service.CloudVolumeRetypeService;
import com.eayun.schedule.service.CloudVolumeTypeService;
import com.eayun.schedule.service.SyncVolumeRetypeService;
import com.eayun.virtualization.model.BaseCloudVolumeType;

@Service
public class SyncVolumeRetypeServiceImpl implements SyncVolumeRetypeService {
	@Autowired
	private DataCenterService dataCenterService;
	@Autowired
	private CloudVolumeTypeService cloudVolumeTypeService;
	
	@Override
	public void sync(){
		StringBuffer sql = new StringBuffer();
		sql.append(" from BaseDcDataCenter ");
		List<BaseDcDataCenter> dcList = dataCenterService.getDcList(sql.toString(), new Object[]{});
		for(BaseDcDataCenter dataCenter :dcList){
			try {
				List<BaseCloudVolumeType> typeList=cloudVolumeTypeService.getVolumeTypesByDcId(dataCenter.getId());
				if(null!=typeList&&typeList.size()>0){
					CloudVolumeRetypeService service = ScheduleResourceStartup.context.getBean(CloudVolumeRetypeService.class);
					service.synchAllData(dataCenter);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		
	}

}
