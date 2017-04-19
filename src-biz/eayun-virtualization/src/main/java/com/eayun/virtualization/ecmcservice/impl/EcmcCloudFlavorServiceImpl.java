package com.eayun.virtualization.ecmcservice.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.Flavor;
import com.eayun.eayunstack.service.OpenstackVmService;
import com.eayun.virtualization.dao.CloudFlavorDao;
import com.eayun.virtualization.ecmcservice.EcmcCloudFlavorService;
import com.eayun.virtualization.model.BaseCloudFlavor;
/**
 * 云主机模板
 * @Filename: EcmcCloudFlavorServiceImpl.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2016年4月26日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Service
@Transactional
public class EcmcCloudFlavorServiceImpl implements EcmcCloudFlavorService {
	
	private static final Logger log = LoggerFactory.getLogger(EcmcCloudFlavorServiceImpl.class);

	@Autowired
	private OpenstackVmService openstackVmService;
	
	@Autowired
	private CloudFlavorDao cloudFlavorDao ;
	/**
	 * 创建云主机模板
	 * @Author: duanbinbin
	 * @param cloudFlavor
	 * @return
	 * @throws AppException
	 *<li>Date: 2016年4月26日</li>
	 */
	@Override
	public BaseCloudFlavor createFlavor(BaseCloudFlavor cloudFlavor)
			throws AppException {
		log.info("创建云主机模板");
		Flavor flavor = openstackVmService.createFlavor(cloudFlavor);
		
		cloudFlavor.setId(UUID.randomUUID()+"");
		cloudFlavor.setFlavorName(flavor.getName());
		cloudFlavor.setFlavorId(flavor.getId());
		
		cloudFlavorDao.saveEntity(cloudFlavor);
		
		return cloudFlavor;
	}

}
