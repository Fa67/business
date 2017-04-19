package com.eayun.syssetup.ecmcservice;


import com.alibaba.fastjson.JSONObject;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;

public interface EcmcCusOLStatisticsService {

	public Page getCusOLPage(Page page, QueryMap queryMap);

	public JSONObject getOLCusAmount();

	public JSONObject getExcelDataList();

}
