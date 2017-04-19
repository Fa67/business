package com.eayun.virtualization.service;

import com.eayun.virtualization.model.BaseCloudVmSgroup;

public interface VmSecurityGroupService {
	
	/**
	 * 新增或修改保存
	 * 
	 * @author zhouhaitao
	 * @param vmsg
	 */
	public void saveOrUpdate(BaseCloudVmSgroup vmsg);
	
	/**
	 * 修改
	 * 
	 * @author zhouhaitao
	 * @param vmsg
	 */
	public void merge(BaseCloudVmSgroup vmsg);
	
	/**
	 * 删除云主机关联的安全组
	 * 
	 * @author zhouhaitao
	 * @param vmId
	 */
	public void deleteByVmId(String vmId);
}
