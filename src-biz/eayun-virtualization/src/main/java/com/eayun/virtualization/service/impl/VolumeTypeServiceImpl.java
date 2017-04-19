package com.eayun.virtualization.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.util.BeanUtils;
import com.eayun.virtualization.dao.CloudVolumeTypeDao;
import com.eayun.virtualization.model.BaseCloudVolumeType;
import com.eayun.virtualization.model.CloudVolumeType;
import com.eayun.virtualization.service.VolumeTypeService;

@Service
@Transactional
public class VolumeTypeServiceImpl implements VolumeTypeService {
	@Autowired
	private CloudVolumeTypeDao cloudVolumeTypeDao;
	
	
	
	

	@Override
	public List<CloudVolumeType> getVolumeTypeList(String dcId) {
		List<Object> list = new ArrayList<Object>();
		List<CloudVolumeType> listType = new ArrayList<CloudVolumeType>();
		
		StringBuffer sql = new StringBuffer();
		sql.append("select tp.id AS id,tp.type_id AS typeId,tp.type_name AS typeName,tp.update_time AS updateTime,tp.volume_type as volumeType,tp.dc_id as dcId,tp.max_size as maxSize,tp.max_iops as maxIops, tp.max_throughput as maxThroughPut,tp.qos_id as qosId,tp.is_use as isUse");
		sql.append(" from cloud_volumetype tp ");
		sql.append(" where 1=1 and tp.is_use= '1'");
		if (!"null".equals(dcId) && null != dcId && !"".equals(dcId)&& !"undefined".equals(dcId)) {
			sql.append(" and tp.dc_id=?");
			list.add(dcId);
		}
		sql.append(" group by tp.id order by tp.volume_type");
		javax.persistence.Query query = cloudVolumeTypeDao.createSQLNativeQuery(sql.toString(), list.toArray());
		@SuppressWarnings("rawtypes")
		List listResult = query.getResultList();
		for (int i = 0; i < listResult.size(); i++) {
			Object[] objs = (Object[]) listResult.get(i);
			CloudVolumeType volumeType=new CloudVolumeType();
			volumeType.setId(String.valueOf(objs[0]));
			volumeType.setTypeId(String.valueOf(objs[1]));
			volumeType.setTypeName(String.valueOf(objs[2]));
			volumeType.setUpdateTime((Date)objs[3]);
			volumeType.setVolumeType(String.valueOf(objs[4]));
			volumeType.setVolumeTypeAs(getVolumeTypeForDis(String.valueOf(objs[4])));
			volumeType.setDcId(String.valueOf(objs[5]));
			volumeType.setMaxSize(Integer.parseInt(String.valueOf(objs[6])));
			volumeType.setMaxIops(Integer.parseInt(String.valueOf(objs[7])));
			volumeType.setMaxThroughput(Integer.parseInt(String.valueOf(objs[8])));
			volumeType.setQosId(String.valueOf(objs[9]));
			volumeType.setIsUse(String.valueOf(objs[10]));
			listType.add(volumeType);
		}
		return listType;
		
	}
	
	
	public  CloudVolumeType getVolumeTypeById(String dcId,String typeId){
		CloudVolumeType type=null;
		BaseCloudVolumeType baseType =cloudVolumeTypeDao.getVolumeTypeByTypeId(dcId, typeId);
		if(null!=baseType){
			type=new CloudVolumeType();
			BeanUtils.copyPropertiesByModel(type, baseType);
		}

		return type;
	}
	
	
	private String  getVolumeTypeForDis(String volumeType){
		String after="";
		
		if(null!=volumeType){
			if("1".equals(volumeType)){
				after="普通型"; 
			}else if("2".equals(volumeType)){
				after="性能型"; 
			}else if("3".equals(volumeType)){
				after="超高性能型"; 
			}else{
				after=volumeType;
			}
		}
		
		return after;

	}


	/**
	 * 根据指定名称查询指定类型
	 */
	@Override
	public CloudVolumeType getVolumeTypeByName(String dcId, String typeName) {
		CloudVolumeType type=new CloudVolumeType();
		BaseCloudVolumeType baseType =cloudVolumeTypeDao.getVolumeTypeByName(dcId, typeName);
		BeanUtils.copyPropertiesByModel(type, baseType);
		return type;
	}


	@Override
	public CloudVolumeType getVolumeTypeByType(String dcId, String type) {
		CloudVolumeType volType=new CloudVolumeType();
		BaseCloudVolumeType baseType =cloudVolumeTypeDao.getTypeByDcId(dcId, type);
		BeanUtils.copyPropertiesByModel(volType, baseType);
		return volType;
	}

}
