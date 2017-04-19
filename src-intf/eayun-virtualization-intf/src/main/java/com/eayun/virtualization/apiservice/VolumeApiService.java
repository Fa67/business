package com.eayun.virtualization.apiservice;

import com.eayun.common.exception.AppException;
import com.eayun.virtualization.model.CloudVolume;


public interface VolumeApiService {
	
	/**
	 * 解绑云硬盘
	 * @author chengxiaodong
	 * @param vol
	 * @return
	 * @throws AppException
	 */
	public boolean debindVolume(CloudVolume vol);
	
	/**
	 * 解绑指定云主机下所有数据盘
	 * @author chengxiaodong
	 * @param vmId
	 * @return
	 */
	public boolean debindVolsByVmId(String vmId);
	
	
	/**
	 * 删除指定云主机的系统盘
	 * @param vmId
	 * @param deleteUser
	 */
	public void deleteVolumeByVm(String vmId,String deleteUser);
}
