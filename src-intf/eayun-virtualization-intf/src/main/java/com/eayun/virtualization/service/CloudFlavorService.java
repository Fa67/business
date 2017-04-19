package com.eayun.virtualization.service;

import com.eayun.common.exception.AppException;
import com.eayun.virtualization.model.BaseCloudFlavor;

public interface CloudFlavorService {
	
	/**
	 * 创建云主机模板
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudFlavor
	 * 			云主机类型模板
	 * @return
	 * 
	 * @throws AppException
	 */
	public  BaseCloudFlavor createFlavor(BaseCloudFlavor cloudFlavor )throws AppException;
	
	
	/**
	 * 查询CloudFlavor的信息
	 * @param flavorId
	 * @return
	 */
	public BaseCloudFlavor queryFlavorByFlavorId(String flavorId);
}
