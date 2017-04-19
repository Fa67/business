package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.Image;
import com.eayun.eayunstack.service.OpenstackImageService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudImage;
import com.eayun.virtualization.model.CloudImage;

@Service
public class OpenstackImageServiceImpl extends OpenstackBaseServiceImpl<Image>
		implements OpenstackImageService {
	private static final Logger log = LoggerFactory
			.getLogger(OpenstackImageServiceImpl.class);

	private void initData(Image data, JSONObject object) {

	}

	public List<Image> list(String datacenterId, String projectId)
			throws AppException {
		List<Image> list = this.listAll(datacenterId);
		if (list != null && list.size() > 0) {
			List<Image> resultList = new ArrayList<Image>();
			for (Image image : list) {
				if (image.getOwner().equals(projectId)) {
					resultList.add(image);
				}
			}
			return resultList;
		}
		return null;
	}

	public List<Image> listAll(String datacenterId) throws AppException {
		List<Image> list = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.IMAGE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.IMAGE_URI);
		List<JSONObject> result = restService.list(restTokenBean,
				OpenstackUriConstant.IMAGE_DATA_NAMES);
		if (result != null && result.size() > 0) {
			for (JSONObject jsonObject : result) {
				if (list == null) {
					list = new ArrayList<Image>();
				}
				Image image = restService.json2bean(jsonObject, Image.class);
				initData(image, jsonObject);
				list.add(image);
			}
		}
		return list;
	}

	/**
	 * 获取指定数据中心下的指定id的镜像的详情
	 */
	public Image getById(String datacenterId, String projectId, String id)
			throws AppException {
		Image result = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.IMAGE_SERVICE_URI);
		JSONObject json = restService.getById(restTokenBean,
				OpenstackUriConstant.IMAGE_URI + "/", null, id);
		if (json != null) {
			result = restService.json2bean(json, Image.class);
			initData(result, json);
		}
		return result;
	}

	public Image create(String datacenterId, String projectId, JSONObject data)
			throws AppException {
		Image image = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,OpenstackUriConstant.IMAGE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.DISK_URI);
		JSONObject result = restService.create(restTokenBean,
				OpenstackUriConstant.DISK_DATA_NAME, data);
		image = restService.json2bean(result, Image.class);
		return image;
	}

	public boolean delete(String datacenterId, String projectId, String id)
			throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.IMAGE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.IMAGE_URI + "/" + id);
		return restService.delete(restTokenBean);
	}


	@Override
	public Image update(CloudImage img, String data) {
		Image image = null;
		RestTokenBean restTokenBean = getRestTokenBean(img.getDcId(),
				OpenstackUriConstant.IMAGE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.IMAGE_URI + "/" + img.getImageId());
		JSONObject result = restService.patch(restTokenBean, data);
		image = restService.json2bean(result, Image.class);
		return image;
	}

	@Override
	public Image update(String datacenterId, String projectId, JSONObject data,
			String id) throws AppException {
		return null;
	}

	public String get(String dcId,String imageId)throws Exception{
		RestTokenBean restTokenBean = getRestTokenBean(dcId,OpenstackUriConstant.IMAGE_SERVICE_URI);
		return restService.getStringById(restTokenBean, OpenstackUriConstant.IMAGE_URI+ "/", imageId);
		
	}
	
	/**                                                                                                         
	 * 获取底层数据中心下的镜像                                                            
	 * -----------------                                                                                      
	 * @author zhouhaitao                                                                                     
	 * @param dataCenter                                                                                      
	 *                                                                                                        
	 * @return                                                                                                
	 * @throws Exception 
	 *                                                                                                        
	 */                                                                                                       
	public List<BaseCloudImage> getStackList(BaseDcDataCenter dataCenter) throws Exception {                                  
		List <BaseCloudImage> list = new ArrayList<BaseCloudImage>();                                         
		RestTokenBean restTokenBean = getRestTokenBean(dataCenter.getId(),                                              
				OpenstackUriConstant.IMAGE_SERVICE_URI); 
		restTokenBean.setUrl(OpenstackUriConstant.IMAGE_URI);
		forEachToStack(restTokenBean, OpenstackUriConstant.IMAGE_DATA_NAMES, dataCenter.getId(), list);
		
		return list;                                                                                            
	}
	
	/**
	 * 将从底层获取的Json 转化为JavaBeen
	 * 
	 * @author zhouhaitao
	 * @param response
	 * @param serviceName
	 * @param dcId
	 * @param list
	 * @throws Exception
	 */
	private void jsonParseJavabeen(JSONObject response, String serviceName,
			String dcId, List<BaseCloudImage> list) throws Exception {
		JSONArray array = response.getJSONArray(serviceName);
		if (array != null && array.size() > 0) {
			for (int i = 0; i < array.size(); i++) {
				Image image = restService.json2bean(array.getJSONObject(i),
						Image.class);
				BaseCloudImage ccn = new BaseCloudImage(image, dcId);
				list.add(ccn);
			}
		}
	}
	
	private void forEachToStack(RestTokenBean restTokenBean,String serviceName,String dcId,List <BaseCloudImage> list) throws Exception{
		JSONObject json = restService.getResponse(restTokenBean);
		
		jsonParseJavabeen(json,serviceName,
				dcId, list);
		String nextUri = json.getString("next");
		if(!StringUtils.isEmpty(nextUri)){
			restTokenBean.setUrl(nextUri);
			forEachToStack(restTokenBean, serviceName, dcId, list);
		}
	}
}
