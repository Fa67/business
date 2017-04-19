package com.eayun.schedule.service.impl;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.PayType;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceSyncConstant;
import com.eayun.common.constant.SyncProgress.SyncByProjectTypes;
import com.eayun.common.constant.SyncProgress.SyncType;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.sync.SyncProgressUtil;
import com.eayun.common.util.CloudResourceUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.Restore;
import com.eayun.eayunstack.model.Volume;
import com.eayun.eayunstack.service.OpenstackVolumeService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.schedule.service.CloudVolumeService;
import com.eayun.virtualization.dao.CloudImageDao;
import com.eayun.virtualization.dao.CloudSnapshotDao;
import com.eayun.virtualization.dao.CloudVolumeDao;
import com.eayun.virtualization.model.BaseCloudImage;
import com.eayun.virtualization.model.BaseCloudSnapshot;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.CloudOrderVolume;
import com.eayun.virtualization.model.CloudVolume;
import com.eayun.virtualization.model.CloudVolumeType;
import com.eayun.virtualization.service.VolumeService;
import com.eayun.virtualization.service.VolumeTypeService;

@Transactional
@Service
public class CloudVolumeServiceImpl implements CloudVolumeService{
    private static final Logger log = LoggerFactory.getLogger(CloudVolumeServiceImpl.class);
    @Autowired
    private JedisUtil jedisUtil; 
	
	@Autowired
	private OpenstackVolumeService openStackVolumeService;
	@Autowired
	private EcmcLogService ecmcLogService;
	@Autowired
	private VolumeService volService;
	@Autowired
	private CloudVolumeDao volumeDao;
	@Autowired
	private CloudImageDao cloudImageDao;
	@Autowired
	private CloudSnapshotDao cloudSnapDao;
	@Autowired
	private VolumeTypeService volumeTypeService;
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
		if(null!=valueJson){
			JSONObject json = openStackVolumeService.get(valueJson.getString("dcId"), 
					valueJson.getString("prjId"), valueJson.getString("volId"));
			if(null!=json){
				boolean isDeleted=json.containsKey("itemNotFound");
				if(!isDeleted){
					result=json.getJSONObject(OpenstackUriConstant.DISK_DATA_NAME);
				}
				else{
					result =new JSONObject();
					result.put("deletingStatus", isDeleted+"");
				}
			}
		}
		return result;
		
	}
	
	
	
	
	/**
	 * 查询订单下创建成功的主硬盘
	 * 
	 * @param orderNo
	 * 				订单编号
	 * @return
	 */
	private List<BaseCloudVolume> queryVolListByOrder(String orderNo){
		List<BaseCloudVolume> volList = new ArrayList<BaseCloudVolume>();
		StringBuffer sql = new StringBuffer();
		
		sql.append("	SELECT                                                ");
		sql.append("		vol.vol_id,                                       ");
		sql.append("		vol.dc_id,                                        ");
		sql.append("		vol.prj_id,                                       ");
		sql.append("		vol.vol_name,                                     ");
		sql.append("		vol.vol_size,                                     ");
		sql.append("		vol.from_snapid,                                  ");
		sql.append("		vol.vol_description,                              ");
		sql.append("		vol.vol_status,                                   ");
		sql.append("		vol.vol_typeid                                    ");
		sql.append("	FROM                                                  ");
		sql.append("		cloud_batchresource cbr                           ");
		sql.append("	LEFT JOIN cloud_volume vol ON cbr.resource_id = vol.vol_id   ");
		sql.append("	AND cbr.resource_type = 'volume'                          ");
		sql.append("	where vol.is_deleted='0'                               ");
		sql.append("	and cbr.order_no = ?                                  ");
		
		javax.persistence.Query query = volumeDao.createSQLNativeQuery(sql.toString(), new Object[]{orderNo});
		
		@SuppressWarnings("rawtypes")
		List result = query.getResultList();
		if(null != result && result.size()>0){
			for(int i =0 ;i<result.size();i++){
				int index = 0;
				Object [] objs = (Object []) result.get(i);
				BaseCloudVolume volume = new BaseCloudVolume();
				volume.setVolId(String.valueOf(objs[index++]));
				volume.setDcId(String.valueOf(objs[index++]));
				volume.setPrjId(String.valueOf(objs[index++]));
				volume.setVolName(String.valueOf(objs[index++]));
				volume.setVolSize(Integer.parseInt(String.valueOf(objs[index++])));
				volume.setFromSnapId(String.valueOf(objs[index++]));
				Object obj=objs[index++];
				volume.setVolDescription(null==obj?null:String.valueOf(obj));
				volume.setVolStatus(String.valueOf(objs[index++]));
				volume.setVolTypeId(String.valueOf(objs[index++]));
				volList.add(volume);
			}
		}
		return volList;
	}
	
	
	
	
	/**
	 * 同步根据备份创建中的云硬盘
	 * 
	 * @author chengxiaodong
	 * @param cloudVolume
	 * @throws Exception 
	 */
	public boolean syncVolumeByBackUpInBuild(CloudVolume cloudVolume) throws Exception{
		boolean isAllSuccess = false;
		List<BaseCloudVolume> cloudVolumeList =null;
		try{
			List<Volume> volList = openStackVolumeService.list(cloudVolume.getDcId(), cloudVolume.getPrjId());
			cloudVolumeList = queryVolListByOrder(cloudVolume.getOrderNo());
			if(null==cloudVolumeList||cloudVolumeList.size()==0){
				isAllSuccess = true;
				return isAllSuccess;
			}
			int index =0;
			step:
			for(BaseCloudVolume cvol:cloudVolumeList){
				for(Volume vol:volList){
					if(vol.getId().equals(cvol.getVolId())){
						if("ERROR".equalsIgnoreCase(vol.getStatus())){
							CloudOrderVolume orderVol = new CloudOrderVolume();
							orderVol.setCreateUser(cloudVolume.getCreateName());
							orderVol.setOrderNo(cloudVolume.getOrderNo());
							orderVol.setCusId(cloudVolume.getCusId());
							volService.volOrderFail(cloudVolumeList, orderVol);
							isAllSuccess = true;
							break step;
						}
						if("AVAILABLE".equalsIgnoreCase(vol.getStatus())){
							if(null!=cvol.getFromSnapId()&&!"".equals(cvol.getFromSnapId())&&!"null".equals(cvol.getFromSnapId())){
								JSONObject data = new JSONObject();
								JSONObject json = new JSONObject();
								json.put("backup_id", cvol.getFromSnapId());
								json.put("volume_id", cvol.getVolId());
								data.put("restore", json);
								Restore restore=openStackVolumeService.restoreVolume(cvol.getDcId(), cvol.getPrjId(),cvol.getFromSnapId(), data);
							
								if(null!=restore){
									JSONObject bson =new JSONObject();
									bson.put("orderNo", cloudVolume.getOrderNo());
									bson.put("volNumber", cloudVolume.getVolNumber());
									bson.put("payType", cloudVolume.getPayType());
									bson.put("cusId",cloudVolume.getCusId());
									bson.put("createName",cloudVolume.getCreateName());
									bson.put("createTime",cloudVolume.getCreateTime());
									bson.put("dcId",cloudVolume.getDcId());
									bson.put("volBootable","0");
									bson.put("prjId", cloudVolume.getPrjId());
									bson.put("volStatus","CREATING");
									bson.put("count", "0");
									this.push(RedisKey.volKey, bson.toJSONString());
								}
								
								BaseCloudSnapshot baseCloudSnapshot=cloudSnapDao.findOne(cvol.getFromSnapId());
								if(null!=baseCloudSnapshot){
									baseCloudSnapshot.setSnapStatus("RESTORING");	
									cloudSnapDao.saveOrUpdate(baseCloudSnapshot);
									//TODO 同步新增云硬盘备份状态（回滚中变为正常）
									JSONObject jsons =new JSONObject();
									jsons.put("snapId",baseCloudSnapshot.getSnapId());
									jsons.put("dcId",baseCloudSnapshot.getDcId());
									jsons.put("prjId",baseCloudSnapshot.getPrjId());
									jsons.put("snapStatus",baseCloudSnapshot.getSnapStatus());
									jsons.put("count", "0");
								    this.push(RedisKey.volSphKey, jsons.toJSONString());
							    }
							    index++;
						   }
					     }
				         break;
				      }
			      }
			}
			
			if(index == cloudVolume.getVolNumber()){
				isAllSuccess = true;
			}
		
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			CloudOrderVolume order = new CloudOrderVolume();
			order.setCreateUser(cloudVolume.getCreateName());
			order.setOrderNo(cloudVolume.getOrderNo());
			order.setPayType(cloudVolume.getPayType());
			order.setCusId(cloudVolume.getCusId());
			volService.volOrderFail(cloudVolumeList, order);
			throw e;
		}
		return isAllSuccess;
	}
	
	
	
	
	/**
	 * 同步创建中的云硬盘
	 * 
	 * @author chengxiaodong
	 * @param cloudVolume
	 * @throws Exception 
	 */
	public boolean syncVolumeInBuild(CloudVolume cloudVolume) throws Exception{
		boolean isAllSuccess = false;
		List<BaseCloudVolume> cloudVolumeList =null;
		try{
			
			List<Volume> volList = openStackVolumeService.list(cloudVolume.getDcId(), cloudVolume.getPrjId());
			cloudVolumeList = queryVolListByOrder(cloudVolume.getOrderNo());
			
			if(null==cloudVolumeList||cloudVolumeList.size()==0){
				isAllSuccess = true;
				return isAllSuccess;
			}
			
			int index =0;
			step:
			for(BaseCloudVolume cvol:cloudVolumeList){
				for(Volume vol:volList){
					if(vol.getId().equals(cvol.getVolId())){
						if("ERROR".equalsIgnoreCase(vol.getStatus())){
							CloudOrderVolume orderVol = new CloudOrderVolume();
							orderVol.setCreateUser(cloudVolume.getCreateName());
							orderVol.setOrderNo(cloudVolume.getOrderNo());
							orderVol.setCusId(cloudVolume.getCusId());
							volService.volOrderFail(cloudVolumeList, orderVol);
							isAllSuccess = true;
							break step;
						}
						if("AVAILABLE".equalsIgnoreCase(vol.getStatus())){
							if(null!=cvol.getFromSnapId()&&!"".equals(cvol.getFromSnapId())&&!"null".equals(cvol.getFromSnapId())){
								JSONObject data=new JSONObject();
								JSONObject temp=new JSONObject();
								temp.put("name", cvol.getVolName());
								if(null!=cvol.getVolDescription()){
									temp.put("description", cvol.getVolDescription());
								}
								data.put("volume", temp);
								openStackVolumeService.update(cvol.getDcId(), cvol.getPrjId(), data, cvol.getVolId());
							}
							
							index++;
						}
						if("AVAILABLE".equalsIgnoreCase(vol.getStatus()) && !"AVAILABLE".equalsIgnoreCase(cvol.getVolStatus())){
							cvol.setVolStatus("AVAILABLE");
							BaseCloudVolume baseVol=volumeDao.findOne(cvol.getVolId());
							baseVol.setVolStatus("AVAILABLE");
							volumeDao.saveOrUpdate(baseVol);
						}
						break;
					}
				}
			}
			
			if(index == cloudVolume.getVolNumber()){
				CloudOrderVolume orderVol = new CloudOrderVolume();
				orderVol.setCreateUser(cloudVolume.getCreateName());
				orderVol.setOrderNo(cloudVolume.getOrderNo());
				orderVol.setPayType(cloudVolume.getPayType());
				orderVol.setCusId(cloudVolume.getCusId());
				volService.volOrderSuccess(orderVol,cloudVolumeList);
				isAllSuccess = true;
			}
		
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			CloudOrderVolume order = new CloudOrderVolume();
			order.setCreateUser(cloudVolume.getCreateName());
			order.setOrderNo(cloudVolume.getOrderNo());
			order.setPayType(cloudVolume.getPayType());
			order.setCusId(cloudVolume.getCusId());
			volService.volOrderFail(cloudVolumeList, order);
			throw e;
		}
		return isAllSuccess;
	}
	
	
	
	/**
	 * 修改云硬盘挂载信息
	 * 如果volume最终状态不为in-use
	 * 将数据库中的vm_id设为null
	 */
	public boolean updateBindVol(CloudVolume cloudVolume) {
		boolean flag = false ;
		try{
			BaseCloudVolume volume = volumeDao.findOne(cloudVolume.getVolId());
			volume.setVolStatus(cloudVolume.getVolStatus());
			volume.setVmId(null);
			if(!StringUtils.isEmpty(cloudVolume.getVolBootable())){
				volume.setVolBootable(cloudVolume.getVolBootable());
			}
			if(!StringUtils.isEmpty(cloudVolume.getBindPoint())){
				volume.setBindPoint(cloudVolume.getBindPoint());
			}
			if(null!=cloudVolume.getVolTypeId()&&!"".equals(cloudVolume.getVolTypeId())){
				volume.setVolTypeId(cloudVolume.getVolTypeId());
			}
			volumeDao.saveOrUpdate(volume);
			flag = true ;
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			flag = false;
		}
		return flag ;
		
	}
	
	
	
	/**
	 * 修改云硬盘信息
	 * @param cloudVm
	 * @return
	 */
	public boolean  updateVol(CloudVolume cloudVolume){
    	boolean flag = false ;
		try{
			BaseCloudVolume volume = volumeDao.findOne(cloudVolume.getVolId());
			volume.setVolStatus(cloudVolume.getVolStatus());
			if(!StringUtils.isEmpty(cloudVolume.getVolBootable())){
				volume.setVolBootable(cloudVolume.getVolBootable());
			}
			if(!StringUtils.isEmpty(cloudVolume.getBindPoint())){
				volume.setBindPoint(cloudVolume.getBindPoint());
			}
			if(("AVAILABLE".equals(cloudVolume.getVolStatus())||"IN-USE".equals(cloudVolume.getVolStatus()))&&"0".equals(volume.getVolBootable())){
				/*if("IN-USE".equals(cloudVolume.getVolStatus())){
					volume.setVmId(cloudVolume.getVmId());
				}*/
				if(null!=cloudVolume.getVolTypeId()&&!"".equals(cloudVolume.getVolTypeId())&&!"null".equals(cloudVolume.getVolTypeId())){
					volume.setVolTypeId(cloudVolume.getVolTypeId());
				}
			}
			volumeDao.saveOrUpdate(volume);
			flag = true ;
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			flag = false;
		}
		return flag ;
    }

	@Override
	public void synchData(BaseDcDataCenter dataCenter, String prjId) throws Exception{
		try {
			
			Map<String,BaseCloudVolume> dbMap=new HashMap<String,BaseCloudVolume>();
			Map<String,BaseCloudVolume> stackMap=new HashMap<String,BaseCloudVolume>();
			List<BaseCloudVolume> dbList=queryCloudVolumeByPrjId(prjId);
			List<BaseCloudVolume> list = getStackList(dataCenter, prjId);
			
			if(null!=dbList){
				for(BaseCloudVolume cv:dbList){
					dbMap.put(cv.getVolId(), cv);
				}
			}
			long total = list == null ? 0L : list.size();
	        syncProgressUtil.initResourceTotal(dataCenter.getId(), SyncType.PROJECT, SyncByProjectTypes.DISK, total);
			if(null!=list){
				for(BaseCloudVolume c:list){
					stackMap.put(c.getVolId(),c);
					
					//同步云硬盘类型数据
					String volTypeName=null!=c.getVolTypeId()?c.getVolTypeId():null;
					if(null!=volTypeName&&!"".equals(volTypeName)&&!"null".equals(volTypeName)){
						CloudVolumeType type=volumeTypeService.getVolumeTypeByName(c.getDcId(), volTypeName);
						if(null!=type&&null!=type.getTypeId()&&!"".equals(type.getTypeId())&&!"null".equals(type.getTypeId())){
							c.setVolTypeId(type.getTypeId());
							c.setTypeSuccess("1");
						}else{
							c.setVolTypeId(null);
						}
					}else{
						c.setVolTypeId(null);
					}
					//底层数据存在本地 更新到本地数据
					if(dbMap.containsKey(c.getVolId())){
						updateVolumeFromStack(c);
					}
					//底层数据不存在于本地 新增到本地数据库中
					else{
						c.setIsDeleted("0");
						c.setChargeState("0");
						c.setIsVisable("0");
						volumeDao.save(c);
					}
					syncProgressUtil.incrResourceDone(dataCenter.getId(), SyncType.PROJECT, SyncByProjectTypes.DISK);
				}
			}
			
			if(null!=dbList){
				for(BaseCloudVolume cv:dbList){
					//本地数据不存在于底层数据 状态置为删除状态
					if(!stackMap.containsKey(cv.getVolId()) && cv.getIsDeleted().equals("2")){
						cv.setIsDeleted("1");
						updateDeteleStatus(cv);
						
						//同步时删除本地底层不存在的资源，记录ecmc日志
						ecmcLogService.addLog("同步资源清除数据",  toType(cv), cv.getVolName(), cv.getPrjId(),1,cv.getVolId(),null);
						
						//将所删除的数据存入redis,以供发邮件使用
						JSONObject json = new JSONObject();
						json.put("resourceType", ResourceSyncConstant.VOLUME);
						json.put("resourceId", cv.getVolId());
						json.put("resourceName", cv.getVolName());
						json.put("synTime", new Date());
						jedisUtil.push(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, json.toJSONString());
	
					}
					if(!stackMap.containsKey(cv.getVolId()) && cv.getIsDeleted().equals("0")){
						CloudVolume volume = new CloudVolume();
						volume.setIsDeleted("1");
						volume.setDeleteTime(new Date());
						volume.setDeleteUser("----");
						volume.setVolId(cv.getVolId());
						
						deleteVol(volume);
						
						//同步时删除本地底层不存在的资源，记录ecmc日志
						ecmcLogService.addLog("同步资源清除数据",  toType(cv), cv.getVolName(), cv.getPrjId(),1,cv.getVolId(),null);
						
						//将所删除的数据存入redis,以供发邮件使用
						JSONObject json = new JSONObject();
						json.put("resourceType", ResourceSyncConstant.VOLUME);
						json.put("resourceId", cv.getVolId());
						json.put("resourceName", cv.getVolName());
						json.put("synTime", new Date());
						jedisUtil.push(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, json.toJSONString());
					}
				}
			}
			
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			throw e;
		} 
	}
	private List<BaseCloudVolume> getStackList(BaseDcDataCenter dataCenter,String prjId) throws Exception {
    	List<BaseCloudVolume> list = new ArrayList<BaseCloudVolume> ();
    	List<JSONObject> result = openStackVolumeService.getStackList(dataCenter, prjId);
    	if (result != null && result.size() > 0) {                                                              
			for (JSONObject jsonObject : result) {                                                                
				 Volume data = openStackVolumeService.json2bean(jsonObject,                                             
						 Volume.class);                                                                            
				 BaseCloudVolume ccn=new BaseCloudVolume(data,dataCenter.getId(),prjId);                                 
				 initDiskFrom(ccn,jsonObject);
				list.add(ccn);
				
				}                                                                                                   
			}                                                                                                     
    	return list;
    }
	
	/**
	 * 查询初始化 CloudVolume 的 diskFrom 字段
	 * @param cv
	 */
	private void initDiskFrom(BaseCloudVolume cv,JSONObject json){
		String str="blank";
		if(!StringUtils.isEmpty(cv.getFromSnapId())){
			str="snapshot";
		}
		if(null!=json){
			JSONObject imageJson = json.getJSONObject("volume_image_metadata");
			if(null!=imageJson&&!StringUtils.isEmpty(imageJson.getString("image_id"))){
				cv.setFromImageId(imageJson.getString("image_id"));
			}
			if(!StringUtils.isEmpty(cv.getFromImageId())){
				BaseCloudImage cloudImage=cloudImageDao.findOne(cv.getFromImageId());
				if(null!=cloudImage){
					if("1".equals(""+cloudImage.getImageIspublic())){
						str="publicImage";
					}
					else if("2".equals(""+cloudImage.getImageIspublic())){
						str="privateImage";
					}
				}
			}
		}
		cv.setDiskFrom(str);
	}
	
	@SuppressWarnings("unchecked")
	public List<BaseCloudVolume>  queryCloudVolumeByPrjId(String prjId){
		StringBuffer hql = new StringBuffer();
		hql.append(" from  BaseCloudVolume ");
		hql.append(" where prjId = ? ");
		
		return volumeDao.find(hql.toString(), new Object[]{prjId});
	}
	
	public boolean updateVolumeFromStack(BaseCloudVolume cv){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_volume set ");
			sql.append("	vol_name = ?,        ");
			sql.append("	prj_id = ?,          ");
			sql.append("	dc_id = ?,           ");
			sql.append("	vol_bootable = ?,    ");
			sql.append("	vm_id =? ,           ");
			sql.append("	bind_point = ?,      ");
			sql.append("	from_imageid = ?,    ");
			sql.append("	from_snapid = ?,     ");
			sql.append("	vol_size = ?,        ");
			sql.append("	vol_description = ?, ");
			sql.append("	vol_status = ?,      ");
			sql.append("	vol_typeid = ?,      ");
			sql.append("	type_success = ?     ");
			sql.append("    where vol_id = ?     ");
			
			
			volumeDao.execSQL(sql.toString(), new Object[]{
					cv.getVolName(),
					cv.getPrjId(),
					cv.getDcId(),
					cv.getVolBootable(),
					cv.getVmId(),
					cv.getBindPoint(),
					cv.getFromImageId(),
					cv.getFromSnapId(),
					cv.getVolSize(), 
					cv.getVolDescription(),
				    cv.getVolStatus(),
				    cv.getVolTypeId(),
				    cv.getTypeSuccess(),
					cv.getVolId()
			});
			flag = true ;
		}catch(Exception e){
			flag = false;
			log.error(e.getMessage(),e);
			throw e;
		}
		
		return flag ;
	}
	
	public boolean updateDeteleStatus(BaseCloudVolume cv){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_volume set  ");
			sql.append("	is_deleted =?         ");
			sql.append(" where vol_id = ? ");
			
			volumeDao.execSQL(sql.toString(), new Object[]{
					 cv.getIsDeleted(),
					 cv.getVolId()
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
	 * 删除底层不存在云硬盘
	 * @param cloudVolume
	 * @return
	 */
	public boolean deleteVol(CloudVolume cloudVolume){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_volume set  ");
			sql.append("	is_deleted =?,         ");
			sql.append("	delete_time =?,         ");
			sql.append("	delete_user =?         ");
			sql.append(" where vol_id = ? ");
			
			volumeDao.execSQL(sql.toString(), new Object[]{
					"1",
					cloudVolume.getDeleteTime(),
					cloudVolume.getDeleteUser(),
					cloudVolume.getVolId()
			});
			flag = true ;
		}catch(Exception e){
			flag = false;
			log.error(e.getMessage(),e);
			throw e;
		}
		
		return flag ;
	}
	
	
	
	private String toType(BaseCloudVolume volume){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StringBuffer resourceType = new StringBuffer();
		resourceType.append(ResourceSyncConstant.VOLUME);
		resourceType.append("-").append(CloudResourceUtil.escapePayType(volume.getPayType())).append(ResourceSyncConstant.SEPARATOR);
		resourceType.append("创建时间：").append(sdf.format(volume.getCreateTime()));
		if(PayType.PAYBEFORE.equals(volume.getPayType()) && null != volume.getEndTime()){
			resourceType.append(ResourceSyncConstant.SEPARATOR).append("到期时间：").append(sdf.format(volume.getEndTime()));
		}
		
		return resourceType.toString();
	}

	
	
	
	@SuppressWarnings("unchecked")
	public List<BaseCloudVolume>  queryVolumes(String prjId){
		StringBuffer hql = new StringBuffer();
		hql.append(" from  BaseCloudVolume ");
		hql.append(" where prjId = ? and isDeleted <> '1' and isVisable='1' and typeSuccess is null ");
		
		return volumeDao.find(hql.toString(), new Object[]{prjId});
	}
	
	
	public boolean updateVolumeType(BaseCloudVolume cv){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_volume set  ");
			sql.append(" vol_typeid =?,type_success=? ");
			sql.append(" where vol_id = ? ");
			
			volumeDao.execSQL(sql.toString(), new Object[]{
					 cv.getVolTypeId(),
					 cv.getTypeSuccess(),
					 cv.getVolId()
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
	 * 为未添加volume_type的云硬盘添加type
	 * @author chengxiaodong
	 */
	@Override
	public void synchVolumeRetype(BaseDcDataCenter dataCenter, String prjId)
			throws Exception {
		try {
			
			List<BaseCloudVolume> dbList=queryVolumes(prjId);
			List<BaseCloudVolume> list = getStackList(dataCenter, prjId);
			
			CloudVolumeType volumeType=volumeTypeService.getVolumeTypeByType(dataCenter.getId(), "1");
			
			if(null!=volumeType.getTypeId()&&!"".equals(volumeType.getTypeId())&&!"null".equals(volumeType.getTypeId())){
				if(null!=volumeType.getQosId()&&!"".equals(volumeType.getQosId())&&!"null".equals(volumeType.getQosId())){
					if(null!=dbList&&null!=list){
						for(BaseCloudVolume cv:dbList){
							
							for(BaseCloudVolume vol:list){
								if(!StringUtil.isEmpty(cv.getVolId())&&!StringUtil.isEmpty(vol.getVolId())&&
								   cv.getVolId().equals(vol.getVolId())){
									
									try{
										//同步云硬盘类型数据
										String volTypeName=null!=vol.getVolTypeId()?vol.getVolTypeId():null;
										if(null!=volTypeName&&!"".equals(volTypeName)&&!"null".equals(volTypeName)){
											CloudVolumeType type=volumeTypeService.getVolumeTypeByName(cv.getDcId(), volTypeName);
											if(null!=type&&null!=type.getTypeId()&&!"".equals(type.getTypeId())&&!"null".equals(type.getTypeId())){
												cv.setVolTypeId(type.getTypeId());
												cv.setTypeSuccess("1");
												updateVolumeType(cv);
											}
										}else{
											if(null==cv.getTypeSuccess()||"".equals(cv.getTypeSuccess())||"null".equals(cv.getTypeSuccess())){
												if(null!=cv.getVolTypeId()&&!"".equals(cv.getVolTypeId())&&!"null".equals(cv.getVolTypeId())){
													JSONObject data=new JSONObject();
													JSONObject temp=new JSONObject();
													data.put("new_type", cv.getVolTypeId());
													temp.put("os-retype", data);
													openStackVolumeService.retype(dataCenter.getId(), prjId, cv.getVolId(), temp);
													cv.setTypeSuccess("1");
													updateVolumeType(cv);
												}else{
													JSONObject data=new JSONObject();
													JSONObject temp=new JSONObject();
													data.put("new_type", volumeType.getTypeId());
													temp.put("os-retype", data);
													openStackVolumeService.retype(dataCenter.getId(), prjId, cv.getVolId(), temp);
													cv.setVolTypeId(volumeType.getTypeId());
													cv.setTypeSuccess("1");
													updateVolumeType(cv);
												}
												
											}
											
										}
									}catch(AppException e){
										log.error(e.getMessage(),e);
										log.info("项目ID：" + vol.getPrjId() + "下的云硬盘"+vol.getVolId()+"retype出错！！！");
									}catch (Exception e) {
									    log.error(e.getMessage(),e);
									    log.info("项目ID：" + vol.getPrjId() + "下的云硬盘"+vol.getVolId()+"retype出错！！！");
									} 
									break;
								}   	
							}
							
						}
					}

				}
				
			}
			
		}catch (AppException e) {
		    log.error(e.getMessage(),e);
			throw e;
		}catch (Exception e) {
		    log.error(e.getMessage(),e);
			throw e;
		} 
	}


}
