package com.eayun.customer.ecmcservice;

import com.eayun.common.exception.AppException;
import com.eayun.customer.model.CusBlockResourceVoe;



/**
 * @author yanchao.li@eayun.com 
 * @date 2016年7月21日
 */
public interface BlockCloudResService {

	
	/**
	 * 冻结资源：
	 * @param cusId
	 * @throws AppException 
	 */
	public CusBlockResourceVoe blockCloudResource(String cusId) throws Exception;
	/**
	 * 解冻资源：
	 * @param cusId
	 * @throws AppException 
	 */
	public CusBlockResourceVoe unblockCloudResource(String cusId) throws Exception;
	
	
	
}
