package com.eayun.virtualization.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eayun.virtualization.dao.CloudSecretKeyVmDao;
import com.eayun.virtualization.model.BaseSecretkeyVm;
import com.eayun.virtualization.service.SecretkeyVmService;

@Service
public class SecretkeyVmServiceImpl implements SecretkeyVmService{
	@Autowired
	private CloudSecretKeyVmDao secretkeyVmDao;
	/**
	 * 
	 * <p>保存SSH密钥与VM之间的关系</p>
	 * -----------------------
	 * @author zhouhaitao
	 * @param bsv 
	 */
	public void saveOrUpdate(BaseSecretkeyVm bsv){
		secretkeyVmDao.save(bsv);
	}
	
	/**
	 * 
	 * 删除云主机下的所有SSH密钥的关系
	 * ------------------
	 * @author zhouhaitao
	 * @param vmId
	 * @return
	 */
	public int deleteByVm(String vmId){
		StringBuffer hql = new StringBuffer();
		
		hql.append(" delete from BaseSecretkeyVm where vmId=?  ");
		return secretkeyVmDao.executeUpdate(hql.toString(), new Object[]{vmId});
	}
	
	/**
	 * <p>统计云主机绑定SSH密钥的个数</p>
	 * 
	 * @author zhouhaitao
	 * @param vmId
	 * @return
	 */
	public int SSHCountbyVm(String vmId){
		return secretkeyVmDao.getSSHcountByVm(vmId);
	}
}
