package com.eayun.virtualization.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.virtualization.dao.CloudRecycleDao;
import com.eayun.virtualization.model.BaseCloudRecycle;
import com.eayun.virtualization.service.CloudRecycleService;

@Service
@Transactional
public class CloudRecycleServiceImpl implements CloudRecycleService{
	
	@Autowired
	private CloudRecycleDao cloudRecycleDao;
	
	/**
	 * <p>查询系统中回收站配置（一条数据）</p>
	 * 若无 返回   <blockquote>null</blockquote>;
	 * 
	 * @author zhouhaitao
	 * @return
	 */
	public BaseCloudRecycle get(){
		StringBuffer hql = new StringBuffer();
		
		hql.append("from BaseCloudRecycle");
		return (BaseCloudRecycle) cloudRecycleDao.findUnique(hql.toString(), new Object[]{});
	}
	
	/**
	 * <p>保存回收站配置到数据库</p>
	 * 
	 * @author zhouhaitao
	 * @param cloudRecycle
	 */
	public void saveOrUpdate(BaseCloudRecycle cloudRecycle){
		cloudRecycleDao.saveOrUpdate(cloudRecycle);
	}
}
