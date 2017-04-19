package com.eayun.virtualization.ecmcservice;

import com.eayun.common.exception.AppException;
import com.eayun.virtualization.model.BaseCloudFlavor;

public interface EcmcCloudFlavorService {

	/**
	 * 创建云主机模板
	 * @Author: duanbinbin
	 * @param cloudFlavor
	 * @return
	 * @throws AppException
	 *<li>Date: 2016年4月26日</li>
	 */
	public  BaseCloudFlavor createFlavor(BaseCloudFlavor cloudFlavor )throws AppException;
}
