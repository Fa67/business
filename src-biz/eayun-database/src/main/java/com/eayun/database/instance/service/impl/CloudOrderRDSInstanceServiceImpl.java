package com.eayun.database.instance.service.impl;

import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.ConstantClazz;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.StringUtil;
import com.eayun.database.instance.dao.CloudOrderRDSInstanceDao;
import com.eayun.database.instance.model.BaseCloudOrderRDSInstance;
import com.eayun.database.instance.model.CloudOrderRDSInstance;
import com.eayun.database.instance.service.CloudOrderRDSInstanceService;
import com.eayun.virtualization.model.CloudVolumeType;
import com.eayun.virtualization.service.VolumeTypeService;

/**
 * 云数据库实例订单的service实现类
 *                       
 * @Filename: CloudOrderRDSInstanceServiceImpl.java
 * @Description: 
 * @Version: 1.0
 * @Author: LiuZhuangzhuang
 * @Email: zhuangzhuang.liu@eayun.com
 * @History:<br>
 *<li>Date: 2017年2月22日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Service
@Transactional
public class CloudOrderRDSInstanceServiceImpl implements CloudOrderRDSInstanceService{
	
	private static final Logger log = LoggerFactory.getLogger(CloudOrderRDSInstanceServiceImpl.class);

	@Autowired
	private CloudOrderRDSInstanceDao rdsInstanceDao;
	@Autowired
	private VolumeTypeService volumeTypeService;
	/**
	 * <p>保存云主机订单信息</p>
	 * 
	 * @author liuzhuangzhuang
	 * @param order -- 数据库实例订单
	 */
	@Override
	public void saveOrUpdate(CloudOrderRDSInstance order) {
		BaseCloudOrderRDSInstance orderRdsInstance = new BaseCloudOrderRDSInstance();
		BeanUtils.copyPropertiesByModel(orderRdsInstance, order);
		orderRdsInstance.setOrderRdsId(UUID.randomUUID()+"");
		rdsInstanceDao.saveOrUpdate(orderRdsInstance);
	}
	/**
	 * 
	 * @param rdsId  -- 云数据库实例ID
	 * @param completeDate  -- 到期时间
	 */
	@Override
	public boolean modifyResourceForVisable(String rdsId, Date completeDate, String vmId) {
		StringBuffer sql = new StringBuffer ();
		boolean isSuccess = false;
		try{
			sql.append(" update BaseCloudRDSInstance set isVisible = '1',rdsStatus = 'ACTIVE',vmId = ?,endTime = ? where rdsId = ? ");
			rdsInstanceDao.executeUpdate(sql.toString(), new Object[]{vmId, completeDate, rdsId});
			isSuccess = true;
		}catch(Exception e){
			isSuccess = false;
			log.error(e.toString(),e);
		}
		return isSuccess;		
	}
	/**
	 * 根据订单编号查询订单信息
	 * @param orderNo
	 *                订单编号
	 * @return
	 */
	@Override
	public CloudOrderRDSInstance getRdsOrderByOrderNo(String orderNo) {
		StringBuffer hql = new StringBuffer();
        hql.append("from BaseCloudOrderRDSInstance orderRds where orderRds.orderNo = ?");
        BaseCloudOrderRDSInstance order = (BaseCloudOrderRDSInstance)rdsInstanceDao.findUnique(hql.toString(), orderNo);
        CloudOrderRDSInstance cloudOrder = new CloudOrderRDSInstance();
        BeanUtils.copyPropertiesByModel(cloudOrder, order);
        CloudVolumeType volumeType = volumeTypeService.getVolumeTypeById(cloudOrder.getDcId(), cloudOrder.getVolumeType());
        if (null != volumeType && !StringUtil.isEmpty(volumeType.getTypeName())) {
            cloudOrder.setVolumeTypeName(volumeType.getTypeName());
        }
        int buyCycle = cloudOrder.getBuyCycle();
        cloudOrder.setCycleType(ConstantClazz.DICT_CLOUD_MONTHLY_BUYCYCLE_NODE_ID);
		if(buyCycle>11){
			cloudOrder.setCycleType(ConstantClazz.DICT_CLOUD_YEARLY_BUYCYCLE_NODE_ID);
		}
        return cloudOrder;
	}

	/**
	 * 更新实例订单表的rdsId字段
	 * @param order
     */
	@Override
	public void udpateOrder(CloudOrderRDSInstance order) {
		StringBuffer hql = new StringBuffer();
		hql.append(" update BaseCloudOrderRDSInstance b set b.rdsId = ? where b.orderNo = ? ");
		rdsInstanceDao.executeUpdate(hql.toString(), new Object[]{order.getRdsId() ,order.getOrderNo()});
	}

}
