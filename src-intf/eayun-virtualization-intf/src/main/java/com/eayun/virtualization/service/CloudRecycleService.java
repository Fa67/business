package com.eayun.virtualization.service;

import com.eayun.virtualization.model.BaseCloudRecycle;

public interface CloudRecycleService {
	
	/**
	 * <p>查询系统中回收站配置（一条数据）</p>
	 * 若无 返回   <blockquote>null</blockquote>;
	 * 
	 * @author zhouhaitao
	 * @return
	 */
	public BaseCloudRecycle get();
	
	/**
	 * <p>保存回收站配置到数据库</p>
	 * 
	 * @author zhouhaitao
	 * @param cloudRecycle
	 */
	public void saveOrUpdate(BaseCloudRecycle cloudRecycle);
	
}
