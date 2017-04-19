package com.eayun.eayunstack.service;

import java.util.List;

import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.Image;
import com.eayun.virtualization.model.BaseCloudImage;
import com.eayun.virtualization.model.CloudImage;

public interface OpenstackImageService extends OpenstackBaseService<Image> {
	
	public Image update(CloudImage image, String string)throws AppException;
	
	public String get(String dcId,String imageId)throws Exception;
	
	/**                                                        
	 * 查询底层 数据中心的云资源                             
	 * -------------------                                   
	 * @author zhouhaitao                                    
	 * @param dataCenter                                     
	 * @return                                               
	 */                                                      
	public List<BaseCloudImage> getStackList (BaseDcDataCenter dataCenter) throws Exception;

}
