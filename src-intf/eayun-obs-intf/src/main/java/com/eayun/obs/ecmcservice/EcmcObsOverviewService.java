package com.eayun.obs.ecmcservice;


import java.util.Date;
import java.util.List;

import com.eayun.common.model.EayunResponseJson;
import com.eayun.obs.ecmcmodel.EcmcObsEchartsBean;
import com.eayun.obs.ecmcmodel.EcmcObsTopModel;
import com.eayun.obs.model.ObsUsedType;

/**
 * EcmcObsUsedService
 * 
 * @Filename: EcmcObsUsedService.java
 * @Description:
 * @Version: 1.0
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br> <li>Date: 2016年3月28日</li> <li>Version: 1.1</li>
 */
public interface EcmcObsOverviewService {
	
	
/**
 * 获取24小时内新增
 */
public ObsUsedType getObs24Used()throws Exception;
/**
 * 获取“存储概览”信息 
 */
public ObsUsedType getObsView() throws Exception;
//获取生成图表所需数据
public EcmcObsEchartsBean getChart(String type, Date start, Date end)throws Exception;
//设置阈值
public String  setThreshold (String storage, String flow, String requestCount);
/**
 * 获取阈值
 * @return
 */
public ObsUsedType getThreshold();
//获取存储量、流量、请求次数排行
public List<EcmcObsTopModel> getTop10(String type) throws Exception;

/**
 * 同步obs用户
 */
public EayunResponseJson syncObsUser() throws Exception;
}
