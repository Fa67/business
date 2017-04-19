package com.eayun.virtualization.service;

import com.eayun.common.exception.AppException;
import com.eayun.virtualization.model.BaseCloudLdVip;
import com.eayun.virtualization.model.CloudLdVip;

/**
 * VipService
 * 
 * @Filename: VipService.java
 * @Description:
 * @Version: 1.0
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br> <li>Date: 2015年11月11日</li> <li>Version: 1.0</li> <li>Content:
 *               create</li>
 * 
 */
public interface VipService {
	
	public BaseCloudLdVip getVipById(String vipId); 
	
	public boolean updateVip(CloudLdVip cloudLdv);
	
	/**
	 * 添加vip
	 * 
	 * @author zhouhaitao
	 * @param vip
	 * @throws AppException
	 */
	public BaseCloudLdVip addVip(CloudLdVip vip) throws AppException;
	
	/**
	 * 修改vip
	 * 
	 * @author zhouhaitao
	 * @param vip
	 * @throws AppException
	 */
	public BaseCloudLdVip modifyVip(CloudLdVip vip) throws AppException;
	
	/**
	 * 删除vip
	 * 
	 * @author zhouhaitao
	 * @param vip
	 * @return
	 */
	public boolean deleteVip(CloudLdVip vip);
}
