package com.eayun.schedule.service.impl;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.SyncProgress.SyncByDatacenterTypes;
import com.eayun.common.constant.SyncProgress.SyncType;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.sync.SyncProgressUtil;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.BackUp;
import com.eayun.eayunstack.model.QosAssociation;
import com.eayun.eayunstack.model.Specs;
import com.eayun.eayunstack.model.VolumeQos;
import com.eayun.eayunstack.model.VolumeType;
import com.eayun.eayunstack.service.OpenstackVolumeService;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.schedule.service.CloudVolumeTypeService;
import com.eayun.virtualization.dao.CloudVolumeDao;
import com.eayun.virtualization.dao.CloudVolumeTypeDao;
import com.eayun.virtualization.model.BaseCloudVolumeType;
import com.eayun.virtualization.model.CloudVolumeType;

@Transactional
@Service
public class CloudVolumeTypeServiceImpl implements CloudVolumeTypeService{
    private static final Logger log = LoggerFactory.getLogger(CloudVolumeTypeServiceImpl.class);
    @Autowired
    private JedisUtil jedisUtil; 
	
	@Autowired
	private EcmcLogService ecmcLogService;
	@Autowired
	private OpenstackVolumeService volumeService;
	@Autowired
	private CloudVolumeTypeDao volTypeDao;
	@Autowired
    private SyncProgressUtil syncProgressUtil;

	@Override
	public String pop(String groupKey) {
		String value = null;
        try {
            value = jedisUtil.pop(groupKey);
        } catch (Exception e){
            log.error(e.getMessage(),e);
            return null;
        }
        return value;
	}

	/**
	 * 重新放入队列
	 * @param groupKey
	 * @param value
	 * @return
	 */
	@Override
	public	boolean push(String groupKey,String value){
		boolean flag = false;
		try {
			flag=  jedisUtil.push(groupKey, value);
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			flag = false;
		}
		return flag;
	}
	
	/**
	 * 查询当前队列的长度
	 * @param groupKey
	 * @return
	 */
	public long size (String groupKey){
		return jedisUtil.sizeOfList(groupKey);
	}
	
	/**
	 * 获取底层指定ID的资源，底层异常为null
	 * ------------------
	 * @author zhouhaitao
	 * @param value
	 * 
	 */
	@Override
	public JSONObject get(JSONObject valueJson) throws Exception{
		JSONObject result = null ;
		try{
		if(null!=valueJson){
			BackUp json = volumeService.getBackUp(valueJson.getString("dcId"), valueJson.getString("prjId"), valueJson.getString("snapId"));
			if(null!=json){
				result =new JSONObject();
				result.put("id", json.getId());
				result.put("status", json.getStatus());
				result.put("volume_id", json.getVolume_id());
				result.put("size", json.getSize());
			}else{
				result =new JSONObject();
				result.put("deletingStatus", true+"");
			}
		}
		}catch(AppException e){
		    log.error(e.getMessage(),e);
			result =new JSONObject();
			result.put("deletingStatus", true+"");
		}
		return result;
	}
	
	/**
	 * 删除云硬盘类型
	 * @param 
	 * @return
	 */
	public boolean deleteVolumeType(CloudVolumeType cloudVolumeType){
		boolean flag = false ;
		try{
			volTypeDao.delete(cloudVolumeType.getId());
			flag = true ;
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			flag = false;
		}
		return flag ;
	}
	
	
	
	/**
	 * 修改云硬盘类型信息
	 * @param 
	 * @return
	 */
	public boolean  updateVolumeType(CloudVolumeType cloudVolumeType){
		boolean flag = false ;
		try{
			BaseCloudVolumeType baseCloudVolumeType = volTypeDao.findOne(cloudVolumeType.getId());
			volTypeDao.saveOrUpdate(baseCloudVolumeType);
			flag = true ;
		}catch(Exception e){
		    log.error(e.getMessage(),e);
		    flag = false;
		}
		return flag ;
	}

	/**
	 * 同步底层云硬盘类型
	 * @author chengxiaodong
	 * @throws Exception 
	 */
	@SuppressWarnings("unused")
	public void synchData(BaseDcDataCenter dataCenter) throws Exception {
		
		try {
			
			List<BaseCloudVolumeType> volumeTypeOne=getType(dataCenter.getId(),"1");
			if(null==volumeTypeOne||0==volumeTypeOne.size()){
				BaseCloudVolumeType baseType=new BaseCloudVolumeType();
				UUID uuid  =  UUID.randomUUID(); 
				String s = UUID.randomUUID().toString();
				baseType.setId(s);
				baseType.setDcId(dataCenter.getId());
				baseType.setIsUse("0");
				baseType.setVolumeType("1");
				baseType.setMaxSize(5000);
				baseType.setMaxIops(500);
				baseType.setMaxThroughput(50);
				baseType.setUpdateTime(new Date());
				baseType.setTypeName("Normal");
				volTypeDao.save(baseType);
			}
			List<BaseCloudVolumeType> volumeTypeTwo=getType(dataCenter.getId(),"2");
			if(null==volumeTypeTwo||0==volumeTypeTwo.size()){
				BaseCloudVolumeType baseType=new BaseCloudVolumeType();
				UUID uuid  =  UUID.randomUUID(); 
				String s = UUID.randomUUID().toString();
				baseType.setId(s);
				baseType.setDcId(dataCenter.getId());
				baseType.setIsUse("0");
				baseType.setVolumeType("2");
				baseType.setMaxSize(2000);
				baseType.setMaxIops(4000);
				baseType.setMaxThroughput(120);
				baseType.setUpdateTime(new Date());
				baseType.setTypeName("Medium");
				volTypeDao.save(baseType);
			}
			List<BaseCloudVolumeType> volumeTypeThree=getType(dataCenter.getId(),"3");
			if(null==volumeTypeThree||0==volumeTypeThree.size()){
				BaseCloudVolumeType baseType=new BaseCloudVolumeType();
				UUID uuid  =  UUID.randomUUID(); 
				String s = UUID.randomUUID().toString();
				baseType.setId(s);
				baseType.setDcId(dataCenter.getId());
				baseType.setIsUse("0");
				baseType.setVolumeType("3");
				baseType.setUpdateTime(new Date());	
				baseType.setTypeName("High");
				volTypeDao.save(baseType);
			}
			
			Map<String,BaseCloudVolumeType> voeMap=new HashMap<String,BaseCloudVolumeType>();
			Map<String,VolumeType> map=new HashMap<String,VolumeType>();
			
			List<BaseCloudVolumeType> voeList=queryVolumeTypesByDcId(dataCenter.getId());
			List<VolumeType> list = volumeService.getVolumeTypes(dataCenter.getId());
			
			
			if(null!=voeList&&voeList.size()>0){
				for(BaseCloudVolumeType voe:voeList){
					voeMap.put(voe.getTypeId(), voe);
				}
			}
			
			if(null!=list&&list.size()>0){
				for(VolumeType c:list){
					map.put(c.getId(), c);
				}
			}
			
			long total = list == null ? 0L : list.size();
	        syncProgressUtil.initResourceTotal(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.VOLUME_TYPE, total);
			if(null!=list&&list.size()>0){
				for(VolumeType volumeType:list){
					//底层数据存在于本地的数据库中 修改本地数据
					if(voeMap.containsKey(volumeType.getId())){
						BaseCloudVolumeType baseType=new BaseCloudVolumeType();
						baseType.setTypeId(volumeType.getId());
						baseType.setTypeName(volumeType.getName());
						updateFromOpenstack(baseType);
					}
					//底层数据不存在于本地数据库 新增本地数据
					else{
						BaseCloudVolumeType volType=null;
						if(null!=volumeType.getName()&&"Normal".equals(volumeType.getName())){
							volType=getVolumeType(dataCenter.getId(),"1");
						}else if(volumeType.getName()!=null&&"Medium".equals(volumeType.getName())){
							volType=getVolumeType(dataCenter.getId(),"2");
						}else if(volumeType.getName()!=null&&"High".equals(volumeType.getName())){
							volType=getVolumeType(dataCenter.getId(),"3");
						}
						
						if(null!=volType){
							volType.setIsUse("0");
							volType.setDcId(dataCenter.getId());
							volType.setTypeId(volumeType.getId());
							volType.setTypeName(volumeType.getName());
							volTypeDao.saveOrUpdate(volType);
						}
						
					}
					syncProgressUtil.incrResourceDone(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.VOLUME_TYPE);
				}
			}
			//删除本地数据不存在于底层的数据
			voeList=queryVolumeTypesByDcId(dataCenter.getId());
			if(null!=voeList&&voeList.size()>0){
				for(BaseCloudVolumeType voe:voeList){
					if(!map.containsKey(voe.getTypeId())){
						voe.setTypeId(null);
						voe.setQosId(null);
						voe.setMaxSize(0);
						voe.setMaxIops(0);
						voe.setMaxThroughput(0);
						volTypeDao.saveOrUpdate(voe);
					}
				}
			}
			
			//同步云硬盘qos
			List<VolumeQos> qoslist = volumeService.getAllVolumeQos(dataCenter.getId());
			List<BaseCloudVolumeType> typeList=queryVolumeTypesByDcId(dataCenter.getId());
			
			if(null!=qoslist){
				for(int i=0;i<qoslist.size();i++){
					VolumeQos volumeQos=qoslist.get(i);
					List<QosAssociation> qosAssList=volumeService.getAllAssociationsForQoS(dataCenter.getId(), volumeQos.getId());
					if(null!=qosAssList){
						for(int j=0;j<qosAssList.size();j++){
							QosAssociation qosAss=qosAssList.get(j);
							if(null!=qosAss.getAssociation_type()&&"volume_type".equals(qosAss.getAssociation_type())){
								if(null!=qosAss.getId()){
									for(BaseCloudVolumeType type :typeList){
										if(qosAss.getId().equals(type.getTypeId())){
											type.setQosId(volumeQos.getId());
											if(null!=volumeQos.getSpecs()){
												Specs specs=volumeQos.getSpecs();
												if(null!=specs.getTotal_iops_sec()&&!"".equals(specs.getTotal_iops_sec())){
													type.setMaxIops(Integer.parseInt(specs.getTotal_iops_sec()));
												}else{
													type.setMaxIops(0);
												}
												if(null!=specs.getTotal_bytes_sec()&&!"".equals(specs.getTotal_bytes_sec())){
													type.setMaxThroughput(Integer.parseInt(specs.getTotal_bytes_sec())/1048576);
												}else{
													type.setMaxThroughput(0);
												}
											}
											volTypeDao.saveOrUpdate(type);
											break;
										}
										
									}
									
								}
								
							}
							
						}
					}
					
				}
				
			}
	
			
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			throw e;
		} 
	}
	
	

	
	@SuppressWarnings("unchecked")
	public List<BaseCloudVolumeType> queryVolumeTypesByDcId (String dcId){
		StringBuffer hql = new StringBuffer();
		hql.append(" from  BaseCloudVolumeType ");
		hql.append(" where dcId = ? ");
		
		return volTypeDao.find(hql.toString(), new Object[]{dcId});
	}
	
	@SuppressWarnings("unchecked")
	public List<BaseCloudVolumeType> getType (String dcId,String volumetype){
		StringBuffer hql = new StringBuffer();
		hql.append(" from  BaseCloudVolumeType ");
		hql.append(" where dcId = ? and volumeType = ? ");
		
		return volTypeDao.find(hql.toString(), new Object[]{dcId,volumetype});
	}
	
	public BaseCloudVolumeType getVolumeType (String dcId,String volumetype){
		return volTypeDao.getTypeByDcId(dcId, volumetype);
	}
	
	public boolean updateFromOpenstack(BaseCloudVolumeType cloudVolumeType){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_volumetype set  ");
			sql.append("   type_name = ?     ");
			sql.append(" where type_id = ? ");
			
			volTypeDao.execSQL(sql.toString(), new Object[]{
				    cloudVolumeType.getTypeName(),
				    cloudVolumeType.getTypeId()
			});
			flag = true ;
		}catch(Exception e){
			flag = false;
			log.error(e.getMessage(),e);
			throw e;
		}
		
		return flag ;
	}

	
	/**
	 * 获取指定数据中心下有效的云硬盘类型
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<BaseCloudVolumeType> getVolumeTypesByDcId(String dcId)
			throws Exception {
		StringBuffer hql = new StringBuffer();
		hql.append(" from  BaseCloudVolumeType ");
		hql.append(" where dcId = ? and typeId is not null and qosId is not null");
		
		return volTypeDao.find(hql.toString(), new Object[]{dcId});
		
	}

	
	
}
