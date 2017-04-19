package com.eayun.dashboard.ecmcservice;

import java.util.Date;
import java.util.List;

import org.quartz.JobDataMap;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.customer.model.BaseCustomer;
import com.eayun.dashboard.model.OverviewIncomeData;
import com.eayun.datacenter.model.DcDataCenter;
import com.eayun.sys.model.SysDataTree;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.model.CloudProjectType;

public interface EcmcOverviewService {

	public List<SysDataTree> getResourceTypeList();
	
	public List<DcDataCenter> getDcResourceList(String resourceType, String sortType);
	
	public Page getListPrjResource(Page page,ParamsMap map,QueryMap queryMap) throws Exception;
	
	public List<BaseCustomer> getAllCustomerList();
	
	public List<CloudProject> getAllProjectList();

	public List<CloudProject> getprojectListByCusId(String cusId);

	public List<DcDataCenter> getAlldcList();

	public List<CloudProject> getprojectListByDcId(String dcId);

	public JSONObject getNowTime() throws Exception;
	
	public CloudProjectType getAllProjectsType(); 
	
	public CloudProjectType getNowCusToMonths(String type) throws Exception;
	
	public List<String> getYears();
	
	/**
	 * 查询总览收入统计数据
	 * @author bo.zeng@eayun.com
	 * @return
	 */
	public OverviewIncomeData getIncomeData(String periodType, String searchYear);
	
	/**
	 * 采集总览收入统计数据（除图表数据以外的）
	 * @author bo.zeng@eayun.com
	 */
	public void gatherOverviewIncomeData();
	
	/**
	 * 采集总览收入统计（图表数据）
	 * @author bo.zeng@eayun.com
	 */
	public void gatherOverviewIncomeChart();
}
