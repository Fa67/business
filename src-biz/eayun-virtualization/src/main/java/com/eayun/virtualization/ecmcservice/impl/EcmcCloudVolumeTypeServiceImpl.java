package com.eayun.virtualization.ecmcservice.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.eayunstack.service.OpenstackVolumeService;
import com.eayun.virtualization.dao.CloudVolumeTypeDao;
import com.eayun.virtualization.ecmcservice.EcmcCloudVolumeTypeService;
import com.eayun.virtualization.model.BaseCloudVolumeType;
import com.eayun.virtualization.model.CloudVolumeType;

@Service
@Transactional
public class EcmcCloudVolumeTypeServiceImpl implements EcmcCloudVolumeTypeService {
	
	private static final Logger log = LoggerFactory.getLogger(EcmcCloudVolumeTypeServiceImpl.class);

	@Autowired
	private OpenstackVolumeService volumeService;
	@Autowired
	private CloudVolumeTypeDao cloudVolumeTypeDao;
	@Autowired
	private JedisUtil jedisUtil;
	
	
	/**
	 * 查询云硬盘类型列表
	 */
	@Override
	public Page getVolumeTypeList(Page page, String dcId, QueryMap queryMap) {
		List<Object> list = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("select tp.id AS id,tp.type_id AS typeId,tp.type_name AS typeName,tp.update_time AS updateTime,tp.volume_type as volumeType,tp.dc_id as dcId,tp.max_size as maxSize,tp.max_iops as maxIops, tp.max_throughput as maxThroughPut,tp.qos_id as qosId,tp.is_use as isUse,dc.dc_name as dcName");
		sql.append(" from cloud_volumetype tp ");
		sql.append(" left join dc_datacenter dc  ON tp.dc_id=dc.id where 1=1");
		if (!"null".equals(dcId) && null != dcId && !"".equals(dcId)&& !"undefined".equals(dcId)) {
			sql.append(" and tp.dc_id=?");
			list.add(dcId);
		}
		sql.append(" group by tp.id order by tp.dc_id,tp.volume_type");
		page = cloudVolumeTypeDao.pagedNativeQuery(sql.toString(), queryMap, list.toArray());
		List newList = (List) page.getResult();
		for (int i = 0; i < newList.size(); i++) {
			Object[] objs = (Object[]) newList.get(i);
			CloudVolumeType volumeType=new CloudVolumeType();
			volumeType.setId(String.valueOf(objs[0]));
			volumeType.setTypeId(null!=objs[1]?String.valueOf(objs[1]):null);
			volumeType.setTypeName(null!=objs[2]?String.valueOf(objs[2]):null);
			volumeType.setUpdateTime((Date)objs[3]);
			volumeType.setVolumeType(String.valueOf(objs[4]));
			volumeType.setVolumeTypeAs(getVolumeTypeForDis(String.valueOf(objs[4])));
			volumeType.setDcId(String.valueOf(objs[5]));
			volumeType.setMaxSize(null!=objs[6]?Integer.parseInt(String.valueOf(objs[6])):null);
			volumeType.setMaxIops(null!=objs[7]?Integer.parseInt(String.valueOf(objs[7])):null);
			volumeType.setMaxThroughput(null!=objs[8]?Integer.parseInt(String.valueOf(objs[8])):null);
			volumeType.setQosId(null!=objs[9]?String.valueOf(objs[9]):null);
			volumeType.setIsUse(String.valueOf(objs[10]));
			volumeType.setDcName(String.valueOf(objs[11]));
			newList.set(i, volumeType);
		}
		return page;
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

	@Override
	public boolean updateVolumeType(CloudVolumeType cloudVolumeType) {
		int maxSize=0;
		int maxThroughput=0;
		int maxIops=0;
		if(null!=cloudVolumeType&&cloudVolumeType.getMaxSize()>0){
			maxSize=cloudVolumeType.getMaxSize();
		}
		if(null!=cloudVolumeType&&cloudVolumeType.getMaxThroughput()>0){
			maxThroughput=cloudVolumeType.getMaxThroughput();
		}
		if(null!=cloudVolumeType&&cloudVolumeType.getMaxIops()>0){
			maxIops=cloudVolumeType.getMaxIops();
		}
		String dcId=null!=cloudVolumeType.getDcId()?cloudVolumeType.getDcId():null;
		String qosId=null!=cloudVolumeType.getQosId()?cloudVolumeType.getQosId():null;
		
		if(null==cloudVolumeType.getQosId()||"null".equals(cloudVolumeType.getQosId())||"".equals(cloudVolumeType.getQosId())){
			String [] args= new String []{cloudVolumeType.getDcName()+"数据中心下无该类型相关数据，请同步后操作，若仍然有问题请联系底层人员创建磁盘类型"};
			throw new AppException("error.openstack.message", args);
		}
		 
		try{
			JSONObject data = new JSONObject();
			JSONObject json = new JSONObject();
			json.put("total_bytes_sec",maxThroughput*1024*1024 );
			json.put("total_iops_sec", maxIops);
			data.put("qos_specs", json);
			if(null!=dcId&&!"".equals(dcId)&&null!=qosId&&!"".equals(qosId)){
				volumeService.setQosKeys(dcId, qosId, data);
			}
			
			BaseCloudVolumeType volumeType=cloudVolumeTypeDao.findOne(cloudVolumeType.getId());
			volumeType.setMaxSize(maxSize);
			volumeType.setMaxThroughput(maxThroughput);
			volumeType.setMaxIops(maxIops);
			volumeType.setUpdateTime(new Date());
			cloudVolumeTypeDao.saveOrUpdate(volumeType);
			return true;
			
		}catch(AppException e){
			throw e;
		}
	
	}

	
	
	@Override
	public boolean changeUse(CloudVolumeType cloudVolumeType) {
		BaseCloudVolumeType type=cloudVolumeTypeDao.findOne(cloudVolumeType.getId());
		if(null!=cloudVolumeType.getIsUse()){
			if("1".equals(cloudVolumeType.getIsUse())&&(null==type.getTypeId()||"".equals(type.getTypeId()))){
				String [] args= new String []{"无法启用，请编辑完善类型后再操作"};
				throw new AppException("error.openstack.message", args);
			}
			type.setIsUse(cloudVolumeType.getIsUse());
		}
		cloudVolumeTypeDao.saveOrUpdate(type);
		return true;
		
	}




}
