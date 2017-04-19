package com.eayun.virtualization.apiservice;


public interface MemberApiService {

	/**
	 * 删除主机时 级联删除成员信息
	 * 
	 * @author chengxiaodong
	 * @param vmId
	 */
	public void deleteMemberByVm(String vmId);
	
	
}
