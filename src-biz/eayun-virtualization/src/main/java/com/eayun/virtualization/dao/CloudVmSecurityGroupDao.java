package com.eayun.virtualization.dao;

import org.hibernate.annotations.SQLDelete;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseCloudVmSgroup;

public interface CloudVmSecurityGroupDao extends IRepository<BaseCloudVmSgroup, String> {
	@SQLDelete(sql = "delete from BaseCloudVmSgroup where vmId = ? ")
	public int deleteByVmId(String vmId);
}
