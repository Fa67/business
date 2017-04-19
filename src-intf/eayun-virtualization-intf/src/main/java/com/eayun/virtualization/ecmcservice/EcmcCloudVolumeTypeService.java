package com.eayun.virtualization.ecmcservice;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.virtualization.model.CloudVolumeType;

public interface EcmcCloudVolumeTypeService {

	//获取云硬盘类型列表
	public Page getVolumeTypeList(Page page, String dcId, QueryMap queryMap);

	//编辑云硬盘类型
	public boolean updateVolumeType(CloudVolumeType cloudVolumeType);

	//启用或停用
	public boolean changeUse(CloudVolumeType cloudVolumeType);


}
