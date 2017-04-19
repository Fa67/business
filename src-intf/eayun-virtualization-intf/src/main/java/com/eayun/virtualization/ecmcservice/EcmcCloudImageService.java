package com.eayun.virtualization.ecmcservice;

import java.io.InputStream;
import java.util.List;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.sys.model.SysDataTree;
import com.eayun.virtualization.model.CloudImage;

public interface EcmcCloudImageService {

	Page getImagePageList(Page page, QueryMap queryMap, String dcId,String sourceType,
			String queryType, String queryName);

	Page getConImagePageList(Page page, QueryMap queryMap, String dcId,
			String sysType, String imageName,String isUse);

	CloudImage createPublicImage(CloudImage cloudImage,InputStream is);

	public boolean updatePersonImage(CloudImage cloudImage);
	
	public boolean updatePublicImage(CloudImage cloudImage);

	public boolean deleteImage(CloudImage cloudImage);

	public CloudImage getPersonImageById(String imageId);
	
	public CloudImage getPublicImageById(String imageId);

	List<SysDataTree> getImageFormatList();

	boolean checkImageName(CloudImage cloudImage);

	public int getImageCountByPrjId(String prjId);

	//启用镜像接口
	public boolean useImage(CloudImage cloudImage);

	//停用镜像接口
	public boolean closeImage(CloudImage cloudImage);

	public List<SysDataTree> getOsTypeList();

	public List<SysDataTree> getMarketTypeList();

	//查询市场镜像列表
	Page getMarketImagePageList(Page page, QueryMap queryMap, String dcId,String professionType, String sysType,String imageName, String isUse);
	
	//编辑市场镜像
	public boolean updateMarketImage(CloudImage cloudImage);

	//上传市场镜像
	public CloudImage createMarketImage(CloudImage cloudImage, InputStream is);
	
	//查询未分类镜像列表
	Page getUnclassifiedImagePageList(Page page, QueryMap queryMap,String dcId, String imageName);

	//编辑未分类镜像
	public boolean updateUnclassifiedImage(CloudImage cloudImage);

	//查询市场镜像详情
	public CloudImage getMarketImageById(String imageId);

	//编辑市场镜像描述
	public boolean updateMarketImageDesc(CloudImage cloudImage);

}
