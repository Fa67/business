package com.eayun.virtualization.service.impl;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.exception.AppException;
import com.eayun.common.tools.OrderGenerator;
import com.eayun.eayunstack.model.Flavor;
import com.eayun.eayunstack.service.OpenstackVmService;
import com.eayun.virtualization.dao.CloudFlavorDao;
import com.eayun.virtualization.model.BaseCloudFlavor;
import com.eayun.virtualization.service.CloudFlavorService;

@Service
@Transactional
public class CloudFlavorServiceImpl implements CloudFlavorService{
	@Autowired
	private OpenstackVmService openstackVmService;
	@Autowired
	private CloudFlavorDao cloudFlavorDao ;
	
	/**
	 * 创建云主机模板
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudFlavor
	 * 			云主机类型模板
	 * @return
	 * 
	 * @throws AppException
	 */
	@Override
	public BaseCloudFlavor createFlavor(BaseCloudFlavor cloudFlavor)throws AppException{
		getBaseCloudFlavorByDcId(cloudFlavor);
		if(StringUtils.isEmpty(cloudFlavor.getFlavorId())){
			String no=OrderGenerator.newOrder();
			cloudFlavor.setFlavorName("temp"+no);
			
			Flavor flavor = openstackVmService.createFlavor(cloudFlavor);
			
			cloudFlavor.setId(UUID.randomUUID()+"");
			cloudFlavor.setFlavorName(flavor.getName());
			cloudFlavor.setFlavorId(flavor.getId());
			
			cloudFlavorDao.saveEntity(cloudFlavor);
		}
		
		return cloudFlavor;
	}
	
	private void getBaseCloudFlavorByDcId(BaseCloudFlavor cloudFlavor){
		StringBuffer sql = new StringBuffer();
		
		sql.append(" select                ");
		sql.append(" 	flavor_id,         ");
		sql.append(" 	flavor_name        ");
		sql.append(" from cloud_flavor     ");
		sql.append(" where dc_id = ?       ");
		sql.append(" and flavor_vcpus = ?  ");
		sql.append(" and flavor_ram = ?    ");
		sql.append(" and flavor_disk = ?    ");
		
		javax.persistence.Query query = cloudFlavorDao.createSQLNativeQuery(sql.toString(), new Object[]{
				cloudFlavor.getDcId(),
				cloudFlavor.getFlavorVcpus(),
				cloudFlavor.getFlavorRam(),
				cloudFlavor.getFlavorDisk()
		});
   		@SuppressWarnings("rawtypes")
		List listResult = query.getResultList();
   		if(null != listResult && listResult.size() > 0){
   			Object [] objs = (Object [])listResult.get(0);
   			cloudFlavor.setFlavorId(String.valueOf(objs[0]));
   			cloudFlavor.setFlavorName(String.valueOf(objs[1]));
   		}
	}
	
	/**
	 * 查询CloudFlavor的信息
	 * @param flavorId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public BaseCloudFlavor queryFlavorByFlavorId(String flavorId){
		StringBuffer hql = new StringBuffer();
		hql.append("from BaseCloudFlavor where flavorId = ?");
		
		List<BaseCloudFlavor> list = cloudFlavorDao.find(hql.toString(), new Object[]{flavorId});
		
		if(null != list && list.size()>0){
			return list.get(0);
		}
		
		return null;
	}
}
