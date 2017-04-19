package com.eayun.virtualization.service;

import com.eayun.virtualization.model.BaseSecretkeyVm;

public interface SecretkeyVmService {

	/**
	 * 
	 * <p>保存SSH密钥与VM之间的关系</p>
	 * -----------------------
	 * @author zhouhaitao
	 * @param bsv 
	 */
	public void saveOrUpdate(BaseSecretkeyVm bsv);
	
	/**
	 * 
	 * 删除云主机下的所有SSH密钥的关系
	 * ------------------
	 * @author zhouhaitao
	 * @param vmId
	 * @return
	 */
	public int deleteByVm(String vmId);
	
	/**
	 * <p>统计云主机绑定SSH密钥的个数</p>
	 * 
	 * @author zhouhaitao
	 * @param vmId
	 * @return
	 */
	public int SSHCountbyVm(String vmId);
}
