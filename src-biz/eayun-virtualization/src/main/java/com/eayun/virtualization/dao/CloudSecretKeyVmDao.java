package com.eayun.virtualization.dao;

import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseSecretkeyVm;

public interface CloudSecretKeyVmDao  extends IRepository<BaseSecretkeyVm, String>{
	
	@Query("from BaseSecretkeyVm where secretkeyId=?")
	public List<BaseSecretkeyVm> getVmListByskId(String skid);
	
	@Query("from BaseSecretkeyVm where vmId=?")
	public List<BaseSecretkeyVm> getVmListByvmId(String vmid);
	
	@Query("from BaseSecretkeyVm where secretkeyId=? and vmId=? ")
    public List<BaseSecretkeyVm> getVmListBySecretkeyIdAndVmId(String secretkeyId,String vmId);
	
	@SQLDelete(sql = "delete from BaseSecretkeyVm where secretkeyId=?")
    public int deleteBySecretkeyId(String secretkeyId);
	
	@SQLDelete(sql = "delete from BaseSecretkeyVm where vmId=?")
    public int deleteByvmId(String secretkeyId);
	
	@Modifying
	@Query("delete from BaseSecretkeyVm where vmId=? and secretkeyId=?")
    public int deleteByVmId(String VmId,String secretkeyId);

	@Query("select count(*) from BaseSecretkeyVm where vmId=?")
	public int getSSHcountByVm(String vmId);
}
