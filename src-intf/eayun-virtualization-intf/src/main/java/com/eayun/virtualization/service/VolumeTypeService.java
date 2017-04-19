package com.eayun.virtualization.service;

import java.util.List;

import com.eayun.virtualization.model.BaseCloudVolumeType;
import com.eayun.virtualization.model.CloudVolumeType;

public interface VolumeTypeService {

	//获取指定项目下启用的云硬盘类型
	public List<CloudVolumeType> getVolumeTypeList(String dcId);
	
	//根据typeid获取指定的云硬盘类型
	public  CloudVolumeType getVolumeTypeById(String dcId,String typeId);
	
	//根据类型名称去获取指定的云硬盘类型
	public  CloudVolumeType getVolumeTypeByName(String dcId,String typeName);
	
	//获取指定数据中心下有效的云硬盘类型
	public CloudVolumeType getVolumeTypeByType(String dcId,String type);




}
