package com.eayun.obs.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.eayun.obs.model.ObsUsedType;

/**
 * ObsUsedService
 * 
 * @Filename: ObsBucketService.java
 * @Description:
 * @Version: 1.0
 * @Author: cxiaodong
 * @Email: xiaodong.cheng@eayun.com
 * @History:<br> <li>Date: 2016年1月15日</li> <li>Version: 1.0</li> <li>Content:
 *               create</li>
 */
public interface ObsUsedService {
	public List<ObsUsedType> getObsUsedList(Date startTime, Date endTime,String cusId)throws Exception;

	/**
	 * 获取指定客户指定时间范围（e.g 2016-08-10 14:00 ~ 2016-06-10 15:00）之间的OBS存储容量数据，用于OBS计费
	 * @author fan.zhang
	 * @param cusId
	 * @param startTime
	 * @param endTime
	 * @return
     * @throws Exception
     */
	double getObsStorage(String cusId, Date startTime, Date endTime) throws Exception;
	
	Map<String, Object> getObsUsed(String cusId, Date startTime, Date endTime) throws Exception;
	
}
