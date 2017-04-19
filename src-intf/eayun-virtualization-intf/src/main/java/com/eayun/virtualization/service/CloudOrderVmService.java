package com.eayun.virtualization.service;

import java.util.Date;
import java.util.List;

import com.eayun.virtualization.model.CloudOrderVm;

public interface CloudOrderVmService {
	
	/**
	 * <p>根据订单编号查询云主机订单信息</p>
	 * --------------------
	 * @author zhouhaitao
	 * 
	 * @param orderNo
	 * 			订单编号
	 */
	public CloudOrderVm getByOrder(String orderNo);
	
	/**
	 * <p>保存云主机订单信息</p>
	 * 
	 * @author zhouhaitao
	 * @param orderVm
	 */
	public void saveOrUpdate(CloudOrderVm orderVm);
	
	/**
	 * 修改云主机订单表已创建订单的状态
	 * 
	 * @author zhouhaitao
	 * 
	 * @param orderNo
	 * 				订单编号
	 * @param resourceJson
	 * 				资源id的JSON
	 * 
	 * @return
	 * 
	 */
	public boolean updateOrderResources(String orderNo,String resourceJson);
	
	/**
	 * 见该批次的资源全部置为显示状态
	 * 
	 * @param vmIds
	 * @param floatIds
	 * @param completeDate 
	 * @return
	 */
	public boolean modifyResourceForVisable(List<String> vmIds,List<String> floatIds,Date completeDate);
	
	/**
	 * 根据镜像ID查询正在处理中的云主机列表
	 * ------------------------------
	 * @author zhouhaitao
	 * @param imageId
	 * @return
	 */
	public boolean checkOrderVmByImage(String imageId);
	
	/**
	 * 校验是否存在未完成的云主机订单使用了SSH密钥
	 * ------------------------------
	 * @author zhouhaitao
	 * @param secretkeyId
	 * 
	 * @return  存在 true;否则返回 false
	 */
	public boolean checkOrderVmBySecretkeyId(String secretkeyId);
}

