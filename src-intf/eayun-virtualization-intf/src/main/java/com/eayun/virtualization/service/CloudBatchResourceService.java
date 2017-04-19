package com.eayun.virtualization.service;

import java.util.List;

import com.eayun.notice.model.MessageOrderResourceNotice;
import com.eayun.virtualization.model.BaseCloudBatchResource;
import com.eayun.virtualization.model.CloudBatchResource;

public interface CloudBatchResourceService {
	
	/**
	 * 根据订单编号订单已创建资源
	 * 
	 * @author zhouhaitao
	 * @param orderNo
	 * @return
	 */
	public List<BaseCloudBatchResource> queryListByOrder(String orderNo);
	
	/**
	 * 删除同一订单下的资源
	 * 
	 * @author zhouhaitao
	 * @param orderNo
	 * @return
	 */
	public boolean deleteByOrder(String orderNo);
	
	/**
	 * 保存资源与订单信息
	 * 
	 * @author zhouhaitao
	 * @param resource
	 */
	public void save(BaseCloudBatchResource resource);
	
	/**
	 * 删除订单编号下云主机
	 * @author zhouhaitao
	 * 
	 * @param cloudBatchResource
	 * @return
	 */
	public boolean delete(CloudBatchResource cloudBatchResource);
	
	/**
	 * 
	 * @param orderNo
	 * @return
	 */
	public List<MessageOrderResourceNotice> queryResourceByOrder(String orderNo);
	
}
