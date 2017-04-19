package com.eayun.monitor.ecmcservice;

import java.util.Date;
import java.util.List;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.monitor.bean.EcmcVmIndicator;
import com.eayun.monitor.bean.MonitorMngData;

public interface EcmcVmIndicatorService {
	
	public List<MonitorMngData> getInterval(String parentId);
	
	public Page getVmListforLive(Page page , QueryMap queryMap , String queryType , String queryName,String dcName,String orderBy,String sort);

	public Page getVmListforLast(Page page , QueryMap queryMap , String queryType , 
			String queryName , Date endDate , int mins , String orderBy , String sort,String dcName);

	public List<EcmcVmIndicator> getDataById(Date endTime, int cou, String vmId,
			String type);

	public List<MonitorMngData> getChartTypes(String parentId);

	public List<MonitorMngData> getChartTimes(String parentId);

	public EcmcVmIndicator getvmById(String vmId);

}
