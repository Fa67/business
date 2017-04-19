package com.eayun.virtualization.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.RedisNodeIdConstant;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.eayunstack.model.Image;
import com.eayun.eayunstack.service.OpenstackImageService;
import com.eayun.sys.model.SysDataTree;
import com.eayun.virtualization.dao.CloudImageDao;
import com.eayun.virtualization.model.BaseCloudImage;
import com.eayun.virtualization.model.CloudImage;
import com.eayun.virtualization.service.CloudOrderVmService;
import com.eayun.virtualization.service.ImageService;
import com.eayun.virtualization.service.TagService;
import com.eayun.virtualization.service.VmService;

@Service
@Transactional
public class ImageServiceImpl implements ImageService {
	@Autowired
	private OpenstackImageService imageService;
	@Autowired
	private TagService tagService;
	@Autowired
	private CloudImageDao imageDao;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private CloudOrderVmService orderVmService;
	@Autowired
	private VmService vmService;

	

	@Override
	public Page getImageList(Page page, String prjId, String dcId,
			String imageName, QueryMap queryMap) throws Exception {
		imageName=imageName.replaceAll("\\_", "\\\\_");
		int index=0;
		Object [] args=new Object[4];
		StringBuffer sql = new StringBuffer();
		sql.append("select ci.image_id as imageId,ci.image_name as imageName,sdt.node_name as sysTypeName,count(cv.vm_id) as vmNum,ci.created_time as createTime, ");
		sql.append("ci.image_status as imageStatus,ci.prj_id as prjId,cp.prj_name as prjName,ci.dc_id as dcId,dc.dc_name as dcName,ci.image_description as imageDesc, ");
		sql.append("ci.os_type as osType,ci.from_vmid as fromVmId,ci.min_cpu as minCpu,ci.min_ram as minRam,ci.min_disk as minDisk, ");
		sql.append("ci.max_cpu as maxCpu,ci.max_ram as maxRam, ");
		sql.append("ci.source_id as sourceId, ");
		sql.append("img.image_name as sourceName, ");
		sql.append("img.image_ispublic as sourceType  ");
		sql.append("from cloud_image as ci ");
		sql.append("join cloud_image as img on ci.source_id = img.image_id ");
		sql.append("left join cloud_project as cp on ci.prj_id = cp.prj_id ");
		sql.append("left join dc_datacenter as dc on ci.dc_id = dc.id ");
		sql.append("left join sys_data_tree as sdt  on ci.sys_type = sdt.node_id ");
		sql.append("left join cloud_vm as cv on cv.from_imageid = ci.image_id  and (cv.is_deleted <> '1' and cv.is_visable='1')");
		sql.append("where ci.image_ispublic='2' ");
		//数据中心
		if (!"".equals(dcId)&&dcId!=null) {
			sql.append("and ci.dc_id = ? ");
			args[index]=dcId;
			index++;
		}
		//项目id为空时，查询所有项目的资源列表
		if (!"".equals(prjId)&&prjId!=null) {
			sql.append("and ci.prj_id = ? ");
			args[index]=prjId;
			index++;
		}
		
		//镜像名称
		if (!"".equals(imageName)&&imageName!=null) {
			sql.append("and binary ci.image_name like ? ");
			args[index]="%"+imageName+"%";
			index++;
		}
		
		sql.append("group by ci.image_id order by ci.created_time desc");
		
		Object[] params = new Object[index];  
        System.arraycopy(args, 0, params, 0, index);
        page = imageDao.pagedNativeQuery(sql.toString(),queryMap,params);
		 List newList = (List)page.getResult();
	        for(int i=0;i<newList.size();i++){
	        	Object[] objs = (Object[])newList.get(i);
	        	CloudImage image=new CloudImage();
	        	image.setImageId(String.valueOf(objs[0]));
	        	image.setImageName(String.valueOf(objs[1]));
	        	image.setSysTypeName(String.valueOf(objs[2]));
	        	image.setVmNum(Integer.parseInt(String.valueOf(objs[3])));
	        	image.setCreateTimeForDis(DateUtil.dateToString((Date)objs[4]));
	        	image.setImageStatus(String.valueOf(objs[5]));
	        	image.setPrjId(String.valueOf(objs[6]));
	        	image.setPrjName(String.valueOf(objs[7]));
	        	image.setDcId(String.valueOf(objs[8]));
	        	image.setDcName(String.valueOf(objs[9]));
	        	image.setImageDescription(String.valueOf(objs[10]));
	        	image.setOsType(String.valueOf(objs[11]));
	        	image.setFromVmId(String.valueOf(objs[12]));
	        	
	        	image.setMinCpu(Long.parseLong(null != objs[13] ? String.valueOf(objs[13]) : "0"));
				int number=Integer.parseInt(null != objs[14] ? String.valueOf(objs[14]) : "0");
				if(0==number){
					image.setMinRam(Long.parseLong(null != objs[14] ? String.valueOf(objs[14]) : "0")/1024);//库里存的是MB，转换为GB
				}else{
					if((Integer.parseInt(null != objs[14] ? String.valueOf(objs[14]) : "0")%1024)==0){
						image.setMinRam(Long.parseLong(null != objs[14] ? String.valueOf(objs[14]) : "0")/1024);
					}else{
						image.setMinRam(Long.parseLong(null != objs[14] ? String.valueOf(objs[14]) : "0")/1024+1);//库里存的是MB，转换为GB,向上取整，例如：1000MB=1GB
					}	
				}
				image.setMinDisk(Long.parseLong(null != objs[15] ? String.valueOf(objs[15]) : "0"));
				
				image.setMaxCpu(null == objs[16] || "".equals(String.valueOf(objs[16]))? null : Integer.valueOf(String.valueOf(objs[16])));
				image.setMaxRam(null == objs[17] || "".equals(String.valueOf(objs[17]))? null : Integer.valueOf(String.valueOf(objs[17]))/1024);
				image.setSourceId(String.valueOf(objs[18]));
				image.setSourceName(String.valueOf(objs[19]));
				image.setSourceType(String.valueOf(objs[20]));
	        	image.setStatusForDis(DictUtil.getStatusByNodeEn("image",image.getImageStatus()));
	        	
	        	newList.set(i, image);
	        }
		return page;
	}



	@Override
	public boolean deleteImage(CloudImage image) throws AppException {
		boolean isTrue=false;
		try{
			BaseCloudImage img=imageDao.findOne(image.getImageId());
			if(null==img){
				throw new AppException("error.openstack.message", new String[]{"此镜像已被删除"});
			}else if(null!=img&&"DELETING".equals(img.getImageStatus())){
				throw new AppException("error.openstack.message", new String[]{"镜像正在删除中，请稍后"});
			}
			
            int countVm=vmService.countVmByImageId(image.getImageId());
			if(countVm>0){
				throw new AppException("error.openstack.message", new String[]{"有基于该镜像创建的云主机无法删除"});
			}
			
			boolean isExist = orderVmService.checkOrderVmByImage(image.getImageId());
			if(isExist){
				throw new AppException("error.openstack.message", new String[]{"您有待创建的主机占用该镜像，无法删除"});
			}
			isTrue=imageService.delete(image.getDcId(), image.getPrjId(), image.getImageId());
			if(isTrue){
				img.setImageStatus("DELETING");
				imageDao.saveOrUpdate(img);
				tagService.refreshCacheAftDelRes("privateImage",image.getImageId());
				//TODO 删除镜像的自动任务
				JSONObject json =new JSONObject();
				json.put("imageId",img.getImageId());
				json.put("dcId",img.getDcId());
				json.put("prjId",img.getPrjId());
				json.put("imageStatus",img.getImageStatus());
				json.put("count", "0");
				jedisUtil.addUnique(RedisKey.imageKey, json.toJSONString());
			}
		}catch(AppException e){
			throw e;
		}
		return isTrue;
	}



	@Override
	public boolean getImageByName(CloudImage image) throws AppException {
		boolean isExist = false;
		StringBuffer sql = new StringBuffer();
		int index=0;
		Object [] args=new Object[4];
		sql.append("select ci.image_id,ci.image_name from cloud_image as ci where 1=1 ");
		//根据镜像类型过滤镜像信息，查询自定义镜像
		sql.append("and ci.image_ispublic='2' ");
		
		//数据中心
		if (!"".equals(image.getDcId())&&image.getDcId()!=null&&!"undefined".equals(image.getDcId())&&!"null".equals(image.getDcId())) {
			sql.append("and ci.dc_id = ? ");
			args[index]=image.getDcId();
			index++;
		}
		//项目
		if (!"".equals(image.getPrjId())&&image.getPrjId()!=null&&!"undefined".equals(image.getPrjId())&&!"null".equals(image.getPrjId())) {
			sql.append("and ci.prj_id = ? ");
			args[index]=image.getPrjId();
			index++;
		}
		//镜像名称
		if (!"".equals(image.getImageName())&&image.getImageName()!=null&&!"undefined".equals(image.getImageName())&&!"null".equals(image.getImageName())) {
			sql.append("and binary ci.image_name = ? ");
			args[index]=image.getImageName().trim();
			index++;
		}
		//镜像ID
		if (!"".equals(image.getImageId())&&image.getImageId()!=null&&!"undefined".equals(image.getImageId())&&!"null".equals(image.getImageId())) {
			sql.append("and ci.image_id <> ? ");
			args[index]=image.getImageId().trim();
			index++;
		}
		
		Object[] params = new Object[index];  
        System.arraycopy(args, 0, params, 0, index);
        javax.persistence.Query query = imageDao.createSQLNativeQuery(sql.toString(), params);
        List listResult = query.getResultList();

        if(listResult.size()>0){
			isExist = true;//返回true 代表存在此名称
		}
		return isExist;
        
	}



	@Override
	public boolean updateImage(CloudImage image) throws AppException {
		boolean isTrue=false;
		try{
			String imageId = image.getImageId();
	        String imageName =image.getImageName();
	        String description =image.getImageDescription();
	        
	        //拼装数据传给底层
	       JSONObject net = new JSONObject();			
			net.put("op", "replace");
			net.put("path", "/name");		
			net.put("value", imageName);
			net.put("description", description);
			JSONArray  resultData = new JSONArray();		
			resultData.add(net);
			Image img=imageService.update(image, resultData.toString());
			if(null!=img){
				BaseCloudImage baseCloudImage = imageDao.findOne(img.getId());
				baseCloudImage.setImageName(img.getName());
				baseCloudImage.setImageDescription(description);
				imageDao.saveOrUpdate(baseCloudImage);
				isTrue=true;
			}
		}catch(AppException e){
			throw e;
		}
		return isTrue;
	}

	public boolean deleteImageById(CloudImage cloudImage)throws Exception{
		boolean isTrue=false;
		try{
			imageDao.delete(cloudImage.getImageId());
			isTrue =  true;
		}catch(Exception e){
			isTrue =  false;
			throw e;
		}
		return isTrue;
	}
	
	public boolean modifyImageById(CloudImage cloudImage)throws Exception{
		boolean isTrue=false;
		try{
			BaseCloudImage baseCloudImage = imageDao.findOne(cloudImage.getImageId());
			baseCloudImage.setImageStatus(cloudImage.getImageStatus());
			imageDao.saveOrUpdate(baseCloudImage);
			
			isTrue =  true;
		}catch(Exception e){
			isTrue =  false;
			throw e;
		}
		return isTrue;
	}


	@Override
	public int countImageByPrjId(String prjId) throws AppException {
		int countImage=imageDao.countImageByPrjId(prjId);
		return countImage;
	}






	/**
	 * 查询公共镜像
	 */
	@Override
	public Page getPublicImageList(Page page, String dcId, String imageName,
			String isUse,String sysType, QueryMap queryMap) {
		imageName=imageName.replaceAll("\\_", "\\\\_");
		int index=0;
		Object [] args=new Object[4];
		StringBuffer sql = new StringBuffer();
		sql.append("select ci.image_id as imageId,ci.image_name as imageName,sdt.node_name as sysTypeName,ci.created_time as createTime, ");
		sql.append("ci.image_status as imageStatus,ci.dc_id as dcId,dc.dc_name as dcName,ci.image_description as imageDesc, ");
		sql.append("ci.os_type as osType,ci.min_cpu as minCpu,ci.min_ram as minRam,ci.min_disk as minDisk, ");
		sql.append("ci.max_cpu as maxCpu,ci.max_ram as maxRam,ci.is_use as isUse,ci.sys_detail as sysDetail ");
		sql.append("from cloud_image as ci ");
		sql.append("left join dc_datacenter as dc on ci.dc_id = dc.id ");
		sql.append("left join sys_data_tree as sdt  on ci.sys_type = sdt.node_id ");
		sql.append("where ci.image_ispublic='1' ");
		//数据中心
		if (!"".equals(dcId)&&dcId!=null) {
			sql.append("and ci.dc_id = ? ");
			args[index]=dcId;
			index++;
		}
		
		//镜像名称
		if (!"".equals(imageName)&&imageName!=null) {
			sql.append("and binary ci.image_name like ? ");
			args[index]="%"+imageName+"%";
			index++;
		}
		
		//系统类型
		if(!"".equals(sysType)){
			if(ConstantClazz.DICT_CLOUD_OS_WINDOWS_NODE_ID.equals(sysType)){
				sql.append("and ci.os_type = ? ");
			}else{
				sql.append("and ci.sys_type = ? ");
			}
			
			args[index]=sysType;
			index++;
		}
		
		//启用或停用
		if (!"".equals(isUse)&&isUse!=null) {
			sql.append("and  ci.is_use = ? ");
			args[index]=isUse;
			index++;
		}else{
			
			sql.append("and (ci.is_use='1' or ci.is_use='2' )");
			
		}
		
		sql.append("group by ci.image_id order by ci.is_use,ci.sys_type,ci.created_time desc");
		
		Object[] params = new Object[index];  
        System.arraycopy(args, 0, params, 0, index);
        page = imageDao.pagedNativeQuery(sql.toString(),queryMap,params);
		 List newList = (List)page.getResult();
	        for(int i=0;i<newList.size();i++){
	        	Object[] objs = (Object[])newList.get(i);
	        	CloudImage image=new CloudImage();
	        	image.setImageId(String.valueOf(objs[0]));
	        	image.setImageName(String.valueOf(objs[1]));
	        	image.setSysTypeName(String.valueOf(objs[2]));
	        	
	        	image.setCreateTimeForDis(DateUtil.dateToString((Date)objs[3]));
	        	image.setImageStatus(String.valueOf(objs[4]));
	        	
	        	image.setDcId(String.valueOf(objs[5]));
	        	image.setDcName(String.valueOf(objs[6]));
	        	image.setImageDescription(String.valueOf(objs[7]));
	        	image.setOsType(String.valueOf(objs[8]));
	        	
	        	image.setMinCpu(Long.parseLong(null != objs[9] ? String.valueOf(objs[9]) : "0"));
				int number=Integer.parseInt(null != objs[10] ? String.valueOf(objs[10]) : "0");
				if(0==number){
					image.setMinRam(Long.parseLong(null != objs[10] ? String.valueOf(objs[10]) : "0")/1024);//库里存的是MB，转换为GB
				}else{
					if((Integer.parseInt(null != objs[10] ? String.valueOf(objs[10]) : "0")%1024)==0){
						image.setMinRam(Long.parseLong(null != objs[10] ? String.valueOf(objs[10]) : "0")/1024);
					}else{
						image.setMinRam(Long.parseLong(null != objs[10] ? String.valueOf(objs[10]) : "0")/1024+1);//库里存的是MB，转换为GB,向上取整，例如：1000MB=1GB
					}	
				}
				image.setMinDisk(Long.parseLong(null != objs[11] ? String.valueOf(objs[11]) : "0"));
				
				image.setMaxCpu(null == objs[12] || "".equals(String.valueOf(objs[12]))? null : Integer.valueOf(String.valueOf(objs[12])));
				image.setMaxRam(null == objs[13] || "".equals(String.valueOf(objs[13]))? null : Integer.valueOf(String.valueOf(objs[13]))/1024);
				image.setIsUse((Character)objs[14]);
				image.setSysDetail(String.valueOf(objs[15]));
	        	image.setStatusForDis(escapseImageState(image));
	        	
	        	newList.set(i, image);
	        }
		return page;
	}


	

	/**
	 * 查询镜像系统类型
	 */
	@Override
	public List<SysDataTree> getOsTypeList() {
		List<SysDataTree> sysList = new ArrayList<SysDataTree>();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT node_id,node_name, parent_id ");
		sql.append(" FROM sys_data_tree WHERE ");
		sql.append(" node_id = ? OR	parent_id = ? ");
		javax.persistence.Query query = imageDao.createSQLNativeQuery(sql.toString(), new Object[]{ConstantClazz.DICT_CLOUD_OS_WINDOWS_NODE_ID,ConstantClazz.DICT_CLOUD_OS_LINUX_NODE_ID});
		@SuppressWarnings("rawtypes")
		List result = query.getResultList();
		if (result != null && result.size() > 0) {
			for (int i = 0; i < result.size(); i++) {
				int index = 0;
				Object[] objs = (Object[]) result.get(i);
				SysDataTree sdt = new SysDataTree();
				sdt.setNodeId(String.valueOf(objs[index++]));
				sdt.setNodeName(String.valueOf(objs[index++]));
				sdt.setParentId(String.valueOf(objs[index++]));
				sysList.add(sdt);
			}
		}
		return sysList;
	}
	
	
	/**
	 * 查询市场镜像类型
	 */
	@Override
	public List<SysDataTree> getMarketTypeList() {
		return getMonitorMngData(RedisNodeIdConstant.MARKET_BUSINESS_TYPE);
	}
	
	public List<SysDataTree> getMonitorMngData(String parentId) {
        List<SysDataTree> dataList = new ArrayList<SysDataTree>();
        Set<String> mngDataSet = null;
        List<String> mngDataList = new ArrayList<String>();
        try {
            mngDataSet = jedisUtil.getSet(RedisKey.SYS_DATA_TREE_PARENT_NODEID + parentId);
            for (String mngData : mngDataSet) {
                mngDataList.add(mngData);
            }
            Collections.sort(mngDataList);
            for (String mngData : mngDataList) {
                String jsonMng = jedisUtil.get(RedisKey.SYS_DATA_TREE + mngData);

                JSONObject monitorMng = JSONObject.parseObject(jsonMng);
                SysDataTree treeData = new SysDataTree();
                treeData.setNodeName(monitorMng.getString("nodeName"));
                treeData.setNodeId(monitorMng.getString("nodeId"));
                treeData.setParentId(monitorMng.getString("parentId"));
                dataList.add(treeData);
            }
        } catch (Exception e) {
            throw new AppException("查询redis数据异常：" + parentId);
        }
        return dataList;
    }

	
	
	//镜像状态转换
	private String escapseImageState(CloudImage cloudImage) {
		String status="";
		
		if(null!=cloudImage.getIsUse()&&"0".equals(cloudImage.getIsUse().toString())){
			status="未启用";
		}else if(null!=cloudImage.getIsUse()&&"1".equals(cloudImage.getIsUse().toString())&&"ACTIVE".equals(cloudImage.getImageStatus())){
			status="正常";
		}else if(null!=cloudImage.getIsUse()&&"2".equals(cloudImage.getIsUse().toString())){
			status="已停用";
		}else{
			status=DictUtil.getStatusByNodeEn("image", cloudImage.getImageStatus());
		}
		
		
		return status;
	}
	
	
	/**
	 * 查询市场镜像
	 */
	@Override
	public Page getMarketImageList(Page page, String dcId, String imageName,
			String isUse,String sysType, String professionType,QueryMap queryMap) {
		imageName=imageName.replaceAll("\\_", "\\\\_");
		int index=0;
		Object [] args=new Object[5];
		StringBuffer sql = new StringBuffer();
		sql.append("select ci.image_id as imageId,ci.image_name as imageName,sdt.node_name as sysTypeName,ci.created_time as createTime, ");
		sql.append("ci.image_status as imageStatus,ci.dc_id as dcId,dc.dc_name as dcName,ci.image_description as imageDesc, ");
		sql.append("ci.os_type as osType,ci.min_cpu as minCpu,ci.min_ram as minRam,ci.min_disk as minDisk, ");
		sql.append("ci.max_cpu as maxCpu,ci.max_ram as maxRam,ci.is_use as isUse,ci.sys_detail as sysDetail, ");
		sql.append("ci.profession_type as professionType,tree.node_name as treeName ");
		sql.append("from cloud_image as ci ");
		sql.append("left join dc_datacenter as dc on ci.dc_id = dc.id ");
		sql.append("left join sys_data_tree as sdt  on ci.sys_type = sdt.node_id ");
		sql.append("left join sys_data_tree tree ON ci.profession_type = tree.node_id ");
		sql.append("where ci.image_ispublic='3' ");
		//数据中心
		if (!"".equals(dcId)&&dcId!=null) {
			sql.append("and ci.dc_id = ? ");
			args[index]=dcId;
			index++;
		}
		
		//镜像名称
		if (!"".equals(imageName)&&imageName!=null) {
			sql.append("and binary ci.image_name like ? ");
			args[index]="%"+imageName+"%";
			index++;
		}
		
		//系统类型
		if(!"".equals(sysType)){
			if(ConstantClazz.DICT_CLOUD_OS_WINDOWS_NODE_ID.equals(sysType)){
				sql.append("and ci.os_type = ? ");
			}else{
				sql.append("and ci.sys_type = ? ");
			}
			
			args[index]=sysType;
			index++;
		}
		
		//市场镜像业务类型
		if(!"".equals(professionType)){
			sql.append("and ci.profession_type = ? ");
			args[index]=professionType;
			index++;
		}
		
		//启用或停用
		if (!"".equals(isUse)&&isUse!=null) {
			sql.append("and  ci.is_use = ? ");
			args[index]=isUse;
			index++;
		}else{
			
			sql.append("and (ci.is_use='1' or ci.is_use='2' )");
			
		}
		
		sql.append("group by ci.image_id order by ci.is_use,ci.profession_type, ci.created_time desc");
		
		Object[] params = new Object[index];  
        System.arraycopy(args, 0, params, 0, index);
        page = imageDao.pagedNativeQuery(sql.toString(),queryMap,params);
		 List newList = (List)page.getResult();
	        for(int i=0;i<newList.size();i++){
	        	Object[] objs = (Object[])newList.get(i);
	        	CloudImage image=new CloudImage();
	        	image.setImageId(String.valueOf(objs[0]));
	        	image.setImageName(String.valueOf(objs[1]));
	        	image.setSysTypeName(String.valueOf(objs[2]));
	        	
	        	image.setCreateTimeForDis(DateUtil.dateToString((Date)objs[3]));
	        	image.setImageStatus(String.valueOf(objs[4]));
	        	
	        	image.setDcId(String.valueOf(objs[5]));
	        	image.setDcName(String.valueOf(objs[6]));
	        	image.setImageDescription(String.valueOf(objs[7]));
	        	image.setOsType(String.valueOf(objs[8]));
	        	
	        	image.setMinCpu(Long.parseLong(null != objs[9] ? String.valueOf(objs[9]) : "0"));
				int number=Integer.parseInt(null != objs[10] ? String.valueOf(objs[10]) : "0");
				if(0==number){
					image.setMinRam(Long.parseLong(null != objs[10] ? String.valueOf(objs[10]) : "0")/1024);//库里存的是MB，转换为GB
				}else{
					if((Integer.parseInt(null != objs[10] ? String.valueOf(objs[10]) : "0")%1024)==0){
						image.setMinRam(Long.parseLong(null != objs[10] ? String.valueOf(objs[10]) : "0")/1024);
					}else{
						image.setMinRam(Long.parseLong(null != objs[10] ? String.valueOf(objs[10]) : "0")/1024+1);//库里存的是MB，转换为GB,向上取整，例如：1000MB=1GB
					}	
				}
				image.setMinDisk(Long.parseLong(null != objs[11] ? String.valueOf(objs[11]) : "0"));
				
				image.setMaxCpu(null == objs[12] || "".equals(String.valueOf(objs[12]))? null : Integer.valueOf(String.valueOf(objs[12])));
				image.setMaxRam(null == objs[13] || "".equals(String.valueOf(objs[13]))? null : Integer.valueOf(String.valueOf(objs[13]))/1024);
				image.setIsUse((Character)objs[14]);
				image.setSysDetail(String.valueOf(objs[15]));
				image.setProfessionType(String.valueOf(objs[16]));
				image.setProfessionName(String.valueOf(objs[17]));
	        	image.setStatusForDis(escapseImageState(image));
	        	
	        	newList.set(i, image);
	        }
		return page;
	}



	
	/**
	 * 查看市场镜像详情
	 * @Author: chengxiaodong
	 * @param imageId
	 * @return
	 */
	@Override
	public CloudImage getMarketImageById(String imageId) {
		CloudImage image = new CloudImage();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append("ci.image_id, ");
		sql.append("ci.image_name, ");
		sql.append("ci.image_status, ");
		sql.append("ci.os_type, ");
		sql.append("ci.sys_type, ");
		sql.append("ci.created_time, ");
		sql.append("ci.min_cpu, ");
		sql.append("ci.min_ram, ");
		sql.append("ci.min_disk, ");
		sql.append("ci.disk_format, ");
		sql.append("sdt.node_name, ");
		sql.append("dc.dc_name, ");
		sql.append("ci.dc_id, ");
		sql.append("ci.image_description, ");
		sql.append("ci.max_cpu, ");
		sql.append("ci.max_ram, ");
		sql.append("ci.sys_detail, ");
		sql.append("ci.is_use, ");
		sql.append("ci.sysdisk_size, ");
		sql.append("ci.provider, ");
		sql.append("ci.integrated_software, ");
		sql.append("ci.profession_type, ");
		sql.append("ci.marketimage_depict, ");
		sql.append("tree.node_name as treeName ");
		sql.append("FROM cloud_image ci ");
		sql.append("LEFT JOIN dc_datacenter dc ON ci.dc_id = dc.id ");
		sql.append("LEFT JOIN sys_data_tree sdt  ON ci.sys_type = sdt.node_id ");
		sql.append("LEFT JOIN sys_data_tree tree ON ci.profession_type = tree.node_id ");
		sql.append("WHERE ci.image_ispublic = '3' ");
		sql.append("AND ci.image_id = ?");
		
		javax.persistence.Query query = imageDao.createSQLNativeQuery(sql.toString(), imageId);
		
		if(null != query && query.getResultList().size() > 0){
			Object[] obj = (Object[]) query.getResultList().get(0);
			image.setImageId(String.valueOf(obj[0]));
			image.setImageName(String.valueOf(obj[1]));
			image.setImageStatus(String.valueOf(obj[2]));
			String osType = String.valueOf(obj[3]);
			image.setOsType(osType);
			image.setSysType(String.valueOf(obj[4]));
			image.setCreatedTime((Date)obj[5]);
			image.setMinCpu(Long.parseLong(null != obj[6] ? String.valueOf(obj[6]) : "0"));
			int number=Integer.parseInt(null != obj[7] ? String.valueOf(obj[7]) : "0");
			if(0==number){
				image.setMinRam(Long.parseLong(null != obj[7] ? String.valueOf(obj[7]) : "0")/1024);//库里存的是MB，转换为GB
			}else{
				if((Integer.parseInt(null != obj[7] ? String.valueOf(obj[7]) : "0")%1024)==0){
					image.setMinRam(Long.parseLong(null != obj[7] ? String.valueOf(obj[7]) : "0")/1024);
				}else{
					image.setMinRam(Long.parseLong(null != obj[7] ? String.valueOf(obj[7]) : "0")/1024+1);//库里存的是MB，转换为GB,向上取整，例如：1000MB=1GB
				}	
			}
			image.setMinDisk(Long.parseLong(null != obj[8] ? String.valueOf(obj[8]) : "0"));
			image.setDiskFormat(String.valueOf(obj[9]));
			image.setSysTypeName(String.valueOf(obj[10]));
			image.setDcName(String.valueOf(obj[11]));
			image.setDcId(String.valueOf(obj[12]));
			image.setImageDescription(String.valueOf(obj[13]));
			image.setMaxCpu(null == obj[14] || "".equals(String.valueOf(obj[14]))? null : Integer.valueOf(String.valueOf(obj[14])));
			image.setMaxRam(null == obj[15] || "".equals(String.valueOf(obj[15]))? null : Integer.valueOf(String.valueOf(obj[15]))/1024);
			image.setSysDetail(String.valueOf(obj[16]));
			image.setIsUse((Character)obj[17]);
			image.setSysdiskSize(Long.parseLong(null != obj[18] ? String.valueOf(obj[18]) : "0"));
			image.setProvider(String.valueOf(obj[19]));
			image.setIntegratedSoftware(String.valueOf(obj[20]));
			image.setProfessionType(String.valueOf(obj[21]));
			image.setMarketimageDepict(null!=obj[22]?String.valueOf(obj[22]):"");
			image.setProfessionName(String.valueOf(obj[23]));
			if(null!=osType&&!"".equals(osType)&&!"null".equals(osType)){
				image.setOsTypeName(DictUtil.getDataTreeByNodeId(osType).getNodeName());
			}
			image.setStatusForDis(escapseImageState(image));
			
		}
		return image;
	}
	


}
