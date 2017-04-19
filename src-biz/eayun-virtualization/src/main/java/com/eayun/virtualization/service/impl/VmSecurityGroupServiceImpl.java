package com.eayun.virtualization.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.virtualization.dao.CloudVmSecurityGroupDao;
import com.eayun.virtualization.model.BaseCloudVmSgroup;
import com.eayun.virtualization.service.VmSecurityGroupService;

@Service
@Transactional
public class VmSecurityGroupServiceImpl implements VmSecurityGroupService{
	@Autowired
	private CloudVmSecurityGroupDao vmSgDao;
	/**
	 * 新增或修改保存
	 * 
	 * @author zhouhaitao
	 * @param vmsg
	 */
	public void saveOrUpdate(BaseCloudVmSgroup vmsg){
		vmSgDao.saveOrUpdate(vmsg);
	}
	
	/**
	 * 修改
	 * 
	 * @author zhouhaitao
	 * @param vmsg
	 */
	public void merge(BaseCloudVmSgroup vmsg){
		vmSgDao.merge(vmsg);
	}
	
	/**
	 * 删除云主机关联的安全组
	 * 
	 * @author zhouhaitao
	 * @param vmId
	 */
	public void deleteByVmId(String vmId){
		vmSgDao.deleteByVmId(vmId);
	}

}
