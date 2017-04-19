package com.eayun.virtualization.dao;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseCloudImage;

public interface CloudImageDao extends IRepository<BaseCloudImage, String> {
	
	@Query("select count(*) from BaseCloudImage image where image.prjId = ?")
	public int countImageByPrjId(String prjId);
	
	@Query("select count(*) from BaseCloudImage image where (image.imageIspublic='1' or image.imageIspublic='3') and image.imageName = ? and image.dcId = ? ")
	public int getCountByNameDcId(String name,String dcId);
	
	@Query("select count(*) from BaseCloudImage image where image.imageIspublic='2' and image.imageName = ? and image.prjId = ?")
	public int getCountByNamePrjId(String name,String prjId);
	
	@Query("select count(*) from BaseCloudImage image where (image.imageIspublic='1' or image.imageIspublic='3') and image.imageName = ? and image.dcId = ? and image.imageId <> ?")
	public int getCountByNameDcIdImageId(String name,String dcId,String imageId);
	
	@Query("select count(*) from BaseCloudImage image where image.imageIspublic='2' and image.imageName = ? and image.prjId = ? and image.imageId <> ?")
	public int getCountByNamePrjIdImageId(String name,String prjId,String imageId);

	@Query("select count(*) from BaseCloudImage image where image.prjId = ? and image.imageIspublic = ?")
	public int getImageCountByPrjId(String prjId,char isPublic);
	
	@Query("select count(imageId) from BaseCloudImage image where image.sourceId = ?")
	public int countImageBySourceId(String imageId);
	  
}
