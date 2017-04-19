package com.eayun.schedule.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.datacenter.service.DataCenterService;
import com.eayun.schedule.ScheduleResourceStartup;
import com.eayun.schedule.service.CloudResourceService;
import com.eayun.schedule.service.SyncDataCenterService;

@Service
public class SyncDataCenterServiceImpl implements SyncDataCenterService {
	@Autowired
	private DataCenterService dataCenterService;
	
	@Override
	public void sync(){
		StringBuffer sql = new StringBuffer();
		sql.append(" from BaseDcDataCenter ");
		List<BaseDcDataCenter> dcList = dataCenterService.getDcList(sql.toString(), new Object[]{});
		for(BaseDcDataCenter dataCenter :dcList){
			CloudResourceService service = ScheduleResourceStartup.context.getBean(CloudResourceService.class);
			service.synchAllData(dataCenter);
		}
		
		
	}

}
