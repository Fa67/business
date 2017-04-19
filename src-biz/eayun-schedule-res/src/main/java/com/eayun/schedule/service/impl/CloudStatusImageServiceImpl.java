package com.eayun.schedule.service.impl;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceSyncConstant;
import com.eayun.common.constant.SyncProgress.SyncByDatacenterTypes;
import com.eayun.common.constant.SyncProgress.SyncType;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.sync.SyncProgressUtil;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.service.OpenstackImageService;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.schedule.service.CloudImageService;
import com.eayun.virtualization.dao.CloudImageDao;
import com.eayun.virtualization.model.BaseCloudImage;
import com.eayun.virtualization.model.CloudImage;

@Transactional
@Service
public class CloudStatusImageServiceImpl implements CloudImageService{
    private static final Logger log = LoggerFactory.getLogger(CloudStatusImageServiceImpl.class);
    @Autowired
    private JedisUtil jedisUtil; 
	
	@Autowired
	private OpenstackImageService openStackImageService;
	@Autowired
	private EcmcLogService ecmcLogService;
	@Autowired
	private CloudImageDao cloudImageDao;
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
		JSONObject json = null;
		if(null!=valueJson){
			String resData = openStackImageService.get(valueJson.getString("dcId"),
					valueJson.getString("imageId"));
			if(null!=resData&&resData.contains("404 Not Found")){
				result =new JSONObject();
				result.put("deletingStatus", "true");
			}
			else{
				json = JSONObject.parseObject(resData);
				if(null!=json){
					boolean isDeleted=json.containsKey("itemNotFound");
					if(!isDeleted){
						result=json;
					}
					else{
						result =new JSONObject();
						result.put("deletingStatus", isDeleted+"");
					}
				}
			}
		}
		return result;
		
	}
	
	/**
	 * 删除自定义镜像
	 * @param cloudImage
	 * @return
	 * @throws Exception 
	 */
	public boolean deleteImage(CloudImage cloudImage) throws Exception{
		boolean isTrue=false;
		try{
			BaseCloudImage image=cloudImageDao.findOne(cloudImage.getImageId());
			if(null==image){
				isTrue =  true;
			}else{
				cloudImageDao.delete(cloudImage.getImageId());
				isTrue =  true;
			}
		}catch(Exception e){
			isTrue =  false;
			throw e;
		}
		return isTrue;
	}
	
	/**
	 * 修改自定义镜像信息
	 * @param cloudImage
	 * @return
	 * @throws Exception 
	 */
	public boolean  updateImage(CloudImage cloudImage) throws Exception{
		boolean isTrue=false;
		try{
			BaseCloudImage baseCloudImage = cloudImageDao.findOne(cloudImage.getImageId());
			baseCloudImage.setImageStatus(cloudImage.getImageStatus());
			baseCloudImage.setPrjId(cloudImage.getPrjId());
			cloudImageDao.saveOrUpdate(baseCloudImage);
			
			isTrue =  true;
		}catch(Exception e){
			isTrue =  false;
			throw e;
		}
		return isTrue;
	}

	@Override
	public void synchData(BaseDcDataCenter dataCenter) throws Exception {
		Map<String,BaseCloudImage> map=new HashMap<String,BaseCloudImage>();
		Map<String,BaseCloudImage> mapVoe=new HashMap<String,BaseCloudImage>();
		List<BaseCloudImage> imageList =getImageListByDcId(dataCenter.getId());
		List<BaseCloudImage> list =openStackImageService.getStackList(dataCenter);
		
		if(null!=imageList){
			for(BaseCloudImage imageVoe:imageList){
				mapVoe.put(imageVoe.getImageId(), imageVoe);
			}
		}
		
		if (null != list) {
			for (BaseCloudImage cloudImage : list) {
				map.put(cloudImage.getImageId(), cloudImage);
			}
		}
		long total = list == null ? 0L : list.size();
        syncProgressUtil.initResourceTotal(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.IMAGE, total);
		if(list != null){
		      for (BaseCloudImage cloudImage : list) {
		            //本地数据存在于底层
		            if(mapVoe.containsKey(cloudImage.getImageId())){
		                updateFromOpenstack(cloudImage);
		            }
		            //底层数据新增到本地数据库
		            else{
		            	if('1' == cloudImage.getImageIspublic()){
		            		cloudImage.setImageIspublic('9');
		            		cloudImage.setIsUse('0');
		            		if(null==cloudImage.getCreatedTime()){
		            			cloudImage.setCreatedTime(new Date());
		            		}
		            	}
		                cloudImageDao.save(cloudImage);
		            }
		            syncProgressUtil.incrResourceDone(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.IMAGE);
		        }
		}
		if(imageList != null){
		      for(BaseCloudImage image:imageList){
		            if(!map.containsKey(image.getImageId())){
		                //删除不存在于底层的本地数据
		                cloudImageDao.delete(image.getImageId());
		                
		                //同步时删除本地底层不存在的资源，记录ecmc日志
						ecmcLogService.addLog("同步资源清除数据",  toType(image), image.getImageName(), image.getPrjId(),1,image.getImageId(),null);
						
						//将所删除的数据存入redis,以供发邮件使用
						JSONObject json = new JSONObject();
						json.put("resourceType", ResourceSyncConstant.IMAGE);
						json.put("resourceId", image.getImageId());
						json.put("resourceName", image.getImageName());
						json.put("synTime", new Date());
						jedisUtil.push(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, json.toJSONString());
						
		            }
		        }
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<BaseCloudImage> getImageListByDcId(String dcId){
		StringBuffer hql = new StringBuffer();
		hql.append(" from BaseCloudImage  ");
		hql.append(" where dcId = ? ");
		
		return cloudImageDao.find(hql.toString(), new Object[]{dcId});
	}
	
	public boolean updateFromOpenstack(BaseCloudImage cloudImage){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_image set ");
			sql.append("	image_name=?,       ");
			sql.append("	image_size=?,       ");
			sql.append("	image_status=?,     ");
			sql.append("	from_vmid=?,        ");
			sql.append("	disk_format=?,      ");
			sql.append("	container_format=?, ");
			sql.append("	check_sum=?,        ");
			sql.append("	owner_id=?,         ");
			sql.append("	min_disk=?,         ");
			sql.append("	min_ram=?,          ");
			sql.append("	is_protected=?,     ");
			sql.append("	prj_id=?,           ");
			sql.append("	dc_id=?            ");
			sql.append(" where image_id=? ");
			
			cloudImageDao.execSQL(sql.toString(), new Object[]{
					cloudImage.getImageName(),
					cloudImage.getImageSize(),
					cloudImage.getImageStatus(),
					cloudImage.getFromVmId(),
					cloudImage.getDiskFormat(),
					cloudImage.getContainerFormat(),
					cloudImage.getCheckSum(),
					cloudImage.getOwnerId(),
					cloudImage.getMinDisk(),
					cloudImage.getMinRam(),
					cloudImage.getIsProtected()+"",
					cloudImage.getPrjId(),
					cloudImage.getDcId(),
					cloudImage.getImageId()
			});
			flag = true ;
		}catch(Exception e){
			flag = false;
			log.error(e.getMessage(),e);
			throw e;
		}
		
		return flag ;
	}
	
	
	private String toType(BaseCloudImage image){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StringBuffer resourceType = new StringBuffer();
		resourceType.append(ResourceSyncConstant.IMAGE);
		if(null !=image && null != image.getCreatedTime()){
			resourceType.append(ResourceSyncConstant.SEPARATOR).append("创建时间：").append(sdf.format(image.getCreatedTime()));
		}
		return resourceType.toString();
	}
	
}
