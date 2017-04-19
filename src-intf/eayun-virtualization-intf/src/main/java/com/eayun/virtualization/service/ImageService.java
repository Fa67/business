package com.eayun.virtualization.service;

import java.util.List;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.sys.model.SysDataTree;
import com.eayun.virtualization.model.CloudImage;


public interface ImageService {

	public Page getImageList(Page page, String prjId, String dcId, String imageName,
			QueryMap queryMap)throws Exception;

	public boolean deleteImage(CloudImage image)throws AppException;

	public boolean getImageByName(CloudImage image)throws AppException;

	public boolean updateImage(CloudImage image)throws AppException;
	
	public int countImageByPrjId(String prjId)throws AppException;
	
	public boolean deleteImageById(CloudImage cloudImage)throws Exception;
	
	public boolean modifyImageById(CloudImage cloudImage)throws Exception;

	//查询公共镜像列表
	public Page getPublicImageList(Page page, String dcId, String imageName,
			String isUse,String sysType, QueryMap queryMap);

	//查询镜像系统类别
	public List<SysDataTree> getOsTypeList();

	//查询市场镜像
	public Page getMarketImageList(Page page, String dcId, String imageName,
			String isUse,String sysType, String professionType,QueryMap queryMap);

	//查询市场镜像类别
	public List<SysDataTree> getMarketTypeList();

	//查询市场镜像详情
	public CloudImage getMarketImageById(String imageId);



}
