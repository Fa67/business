package com.eayun.virtualization.service.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.ConstantClazz;
import com.eayun.common.util.BeanUtils;
import com.eayun.virtualization.dao.CloudVolumeOrderDao;
import com.eayun.virtualization.model.BaseCloudOrderVolume;
import com.eayun.virtualization.model.CloudOrderVolume;
import com.eayun.virtualization.service.VolumeOrderService;


@Service
@Transactional
public class VolumeOrderServiceImpl implements VolumeOrderService {
    private static final Logger log = LoggerFactory.getLogger(VolumeOrderServiceImpl.class);
	@Autowired
	private CloudVolumeOrderDao volumeOrderDao;
	
	
	/**
	 * 根据订单编号查询具体配置
	 */
	@Override
	public CloudOrderVolume getVolOrderByOrderNo(String orderNo)
			throws Exception {
		
		StringBuffer hql = new StringBuffer();
		hql.append(" from BaseCloudOrderVolume where orderNo = ? ");
		BaseCloudOrderVolume baseCloudorderVolume = (BaseCloudOrderVolume) volumeOrderDao.findUnique(hql.toString(), new Object[]{orderNo});
		CloudOrderVolume orderVolume = new CloudOrderVolume();
		BeanUtils.copyPropertiesByModel(orderVolume, baseCloudorderVolume);
		return orderVolume;
	
	}


	/**
	 * 保存订单中云硬盘配置
	 */
	@Override
	public void addOrderVolume(CloudOrderVolume orderVolume) throws Exception {
		BaseCloudOrderVolume baseOrder=new BaseCloudOrderVolume();
		BeanUtils.copyPropertiesByModel(baseOrder, orderVolume);
		baseOrder.setOrderVolId(UUID.randomUUID()+"");
		volumeOrderDao.saveOrUpdate(baseOrder);
		
	}
	
	
	
	/**
	 * 修改订单表已创建订单的资源ids
	 * 
	 * @author chengxiaodong
	 * 
	 * @param orderNo
	 * 				订单编号
	 * @param resourceJson
	 * 				资源id的JSON
	 * 
	 * @return
	 * 
	 */
	public boolean updateOrderResources(String orderNo,String resourceJson)throws Exception{
		StringBuffer hql = new StringBuffer();
		boolean isSuccess = false;
		try{
			hql.append(" update BaseCloudOrderVolume set orderResources = ? where orderNo = ? ");
			volumeOrderDao.executeUpdate(hql.toString(), new Object[]{resourceJson,orderNo});
			isSuccess = true;
		}catch(Exception e){
			isSuccess = false;
			log.error(e.toString(),e);
			throw e;
		}
		
		return isSuccess;
	}
	
	
	/**
	 * 根据订单编号查询云硬盘订单信息
	 * @author chengxiaodong
	 * @param orderNo
	 * 			订单编号
	 */
	public CloudOrderVolume getByOrder(String orderNo){
		StringBuffer hql = new StringBuffer();
		hql.append(" from BaseCloudOrderVolume where orderNo = ? ");
		BaseCloudOrderVolume baseCloudorderVolume = (BaseCloudOrderVolume) volumeOrderDao.findUnique(hql.toString(), new Object[]{orderNo});
		CloudOrderVolume orderVolume = new CloudOrderVolume();
		
		BeanUtils.copyPropertiesByModel(orderVolume, baseCloudorderVolume);
		int buyCycle = orderVolume.getBuyCycle();
		orderVolume.setCycleType(ConstantClazz.DICT_CLOUD_MONTHLY_BUYCYCLE_NODE_ID);
		if(buyCycle>11){
			orderVolume.setCycleType(ConstantClazz.DICT_CLOUD_YEARLY_BUYCYCLE_NODE_ID);
		}
		return orderVolume;
	}

}
