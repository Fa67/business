package com.eayun.virtualization.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.constant.ResourceType;
import com.eayun.notice.model.MessageOrderResourceNotice;
import com.eayun.virtualization.dao.CloudBatchResourceDao;
import com.eayun.virtualization.model.BaseCloudBatchResource;
import com.eayun.virtualization.model.CloudBatchResource;
import com.eayun.virtualization.service.CloudBatchResourceService;

@Service
@Transactional
public class CloudBatchResourceServiceImpl implements CloudBatchResourceService{
    private static final Logger log = LoggerFactory.getLogger(CloudBatchResourceServiceImpl.class);
	@Autowired
	private CloudBatchResourceDao cloudBatchResourceDao;
	
	/**
	 * 根据订单编号订单已创建资源
	 * 
	 * @author zhouhaitao
	 * @param orderNo
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<BaseCloudBatchResource> queryListByOrder(String orderNo){
		StringBuffer hql = new StringBuffer();
		
		hql.append(" from  BaseCloudBatchResource where orderNo= ? ");
		return cloudBatchResourceDao.find(hql.toString(), new Object[]{orderNo});
	}
	
	@SuppressWarnings("rawtypes")
	public List<MessageOrderResourceNotice> queryResourceByOrder(String orderNo){
		List<MessageOrderResourceNotice> resourceList = new ArrayList<MessageOrderResourceNotice>();
		StringBuffer sql = new StringBuffer();
		sql.append("		SELECT                                 ");
		sql.append("			cb.order_no,                       ");
		sql.append("			cb.resource_id,                    ");
		sql.append("			cb.resource_type,                  ");
		sql.append("			CASE                               ");
		sql.append("		WHEN cb.resource_type = 'vm' THEN      ");
		sql.append("			cv.vm_name                         ");
		sql.append("		WHEN cb.resource_type = 'volume' THEN  ");
		sql.append("			cvol.vol_name                      ");
		sql.append("		WHEN cb.resource_type = 'floatip' THEN ");
		sql.append("			cf.flo_ip                          ");
		sql.append("		END AS resourceName                    ");
		sql.append("		FROM                                   ");
		sql.append("			cloud_batchresource cb             ");
		sql.append("		LEFT JOIN cloud_vm cv                  ");
		sql.append("		ON cb.resource_id = cv.vm_id           ");
		sql.append("		AND cb.resource_type = 'vm'            ");
		sql.append("		LEFT JOIN cloud_volume cvol            ");
		sql.append("		ON cb.resource_id = cvol.vol_id        ");
		sql.append("		AND cb.resource_type = 'volume'        ");
		sql.append("		LEFT JOIN cloud_floatip cf             ");
		sql.append("		ON cb.resource_id = cf.flo_id          ");
		sql.append("		AND cb.resource_type = 'floatip'       ");
		sql.append("		WHERE                                  ");
		sql.append("			cb.order_no = ?                    ");
		
		
		javax.persistence.Query query = cloudBatchResourceDao.createSQLNativeQuery(
				sql.toString(), new Object[]{orderNo});
		List list = query.getResultList();
		if(null != list && list.size()>0){
			for(int i = 0; i<list.size();i++){
				int index = 0;
				Object [] objs = (Object []) list.get(i); 
				
				MessageOrderResourceNotice resource = new MessageOrderResourceNotice();
				resource.setOrderNo(String.valueOf(objs[index++]));
				resource.setResourceId(String.valueOf(objs[index++]));
				String type = String.valueOf(objs[index++]);
				if("vm".equals(type)){
					resource.setResourceType(ResourceType.getName(ResourceType.VM));
				}else if("volume".equals(type)){
					resource.setResourceType(ResourceType.getName(ResourceType.VDISK));
				}
				else if("floatip".equals(type)){
					resource.setResourceType(ResourceType.getName(ResourceType.FLOATIP));
				}
				resource.setResourceName(String.valueOf(objs[index++]));
				
				resourceList.add(resource);
			}
		}
		
		return resourceList;
	}
	
	/**
	 * 删除同一订单下的资源
	 * 
	 * @author zhouhaitao
	 * @param orderNo
	 * @return
	 */
	public boolean deleteByOrder(String orderNo){
		StringBuffer hql = new StringBuffer();
		boolean isSuccess = false;
		try{
			hql.append(" delete from BaseCloudBatchResource where orderNo= ? ");
			cloudBatchResourceDao.executeUpdate(hql.toString(), new Object[]{orderNo});
			isSuccess = true;
		}catch(Exception e){
			isSuccess = false ;
			log.error(e.toString(),e);
			throw e;
		}
		return isSuccess;
	}
	
	/**
	 * 保存资源与订单信息
	 * 
	 * @author zhouhaitao
	 * @param resource
	 */
	public void save(BaseCloudBatchResource resource){
		cloudBatchResourceDao.saveOrUpdate(resource);
	}
	
	/**
	 * 删除订单编号下云主机
	 * @author zhouhaitao
	 * 
	 * @param cloudBatchResourceService
	 * @return
	 */
	public boolean delete(CloudBatchResource cloudBatchResource){
		StringBuffer hql = new StringBuffer();
		boolean isSuccess = false;
		try{
			hql.append(" delete from BaseCloudBatchResource where orderNo= ? and resourceId = ? ");
			cloudBatchResourceDao.executeUpdate(hql.toString(), new Object[]{cloudBatchResource.getOrderNo(),cloudBatchResource.getResourceId()});
			isSuccess = true;
		}catch(Exception e){
			isSuccess = false ;
			log.error(e.toString(),e);
			throw e;
		}
		return isSuccess;
	}

}
