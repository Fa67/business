package com.eayun.virtualization.service.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.ConstantClazz;
import com.eayun.common.util.BeanUtils;
import com.eayun.virtualization.dao.CloudOrderVmDao;
import com.eayun.virtualization.model.BaseCloudOrderVm;
import com.eayun.virtualization.model.CloudOrderVm;
import com.eayun.virtualization.service.CloudOrderVmService;

@Service
@Transactional
public class CloudOrderVmServiceImpl implements CloudOrderVmService {
    private static final Logger log = LoggerFactory.getLogger(CloudOrderVmServiceImpl.class);
	@Autowired
	private CloudOrderVmDao orderVmDao;
	
	/**
	 * <p>根据订单编号查询云主机订单信息</p>
	 * --------------------
	 * @author zhouhaitao
	 * 
	 * @param orderNo
	 * 			订单编号
	 */
	public CloudOrderVm getByOrder(String orderNo){
		StringBuffer hql = new StringBuffer();
		hql.append(" from BaseCloudOrderVm where orderNo = ? ");
		BaseCloudOrderVm baseCloudorderVm = (BaseCloudOrderVm) orderVmDao.findUnique(hql.toString(), new Object[]{orderNo});
		CloudOrderVm orderVm = new CloudOrderVm();
		
		BeanUtils.copyPropertiesByModel(orderVm, baseCloudorderVm);
		int buyCycle = orderVm.getBuyCycle();
		orderVm.setCycleType(ConstantClazz.DICT_CLOUD_MONTHLY_BUYCYCLE_NODE_ID);
		if(buyCycle>11){
			orderVm.setCycleType(ConstantClazz.DICT_CLOUD_YEARLY_BUYCYCLE_NODE_ID);
		}
		return orderVm;
	}
	
	/**
	 * <p>保存云主机订单信息</p>
	 * 
	 * @author zhouhaitao
	 * @param orderVm
	 */
	public void saveOrUpdate(CloudOrderVm orderVm){
		BaseCloudOrderVm order = new BaseCloudOrderVm();
		BeanUtils.copyPropertiesByModel(order, orderVm);
		order.setOrdervmId(UUID.randomUUID()+"");
		
		orderVmDao.saveOrUpdate(order);
	}
	
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
	public boolean updateOrderResources(String orderNo,String resourceJson){
		StringBuffer hql = new StringBuffer();
		boolean isSuccess = false;
		try{
			hql.append(" update BaseCloudOrderVm b set b.orderResources = ? where b.orderNo = ? ");
			
			orderVmDao.executeUpdate(hql.toString(), new Object[]{resourceJson ,orderNo});
			isSuccess = true;
		}catch(Exception e){
			isSuccess = false;
			log.error(e.toString(),e);
			throw e;
		}
		
		return isSuccess;
	}
	
	/**
	 * 见该批次的资源全部置为显示状态
	 * 
	 * @param vmIds
	 * @param floatIds
	 * @param completeDate 
	 * @return
	 */
	public boolean modifyResourceForVisable(List<String> vmIds,List<String> floatIds,Date completeDate){
		StringBuffer vmsql = new StringBuffer ();
		StringBuffer volSql = new StringBuffer ();
		StringBuffer floatSql = new StringBuffer ();
		boolean isSuccess = false;
		try{
			vmsql.append(" update BaseCloudVm set isVisable = '1',endTime = ? where vmId = ? ");
			volSql.append(" update BaseCloudVolume set isVisable = '1',endTime = ? where vmId = ? ");
			if(vmIds != null && vmIds.size() >0 ){
				for(String vmId:vmIds){
					orderVmDao.executeUpdate(vmsql.toString(), new Object[]{completeDate,vmId});
					
					orderVmDao.executeUpdate(volSql.toString(), new Object[]{completeDate,vmId});
				}
			}
			
			if(floatIds != null && floatIds.size() >0 ){
				floatSql.append(" update BaseCloudFloatIp set isVisable = '1',endTime = ? where floId = ? ");
				for(String floId:floatIds){
					orderVmDao.executeUpdate(floatSql.toString(), new Object[]{completeDate,floId});
				}
				
			}
			
			isSuccess = true;
		}catch(Exception e){
			isSuccess = false;
			log.error(e.toString(),e);
		}
		return isSuccess;
	}
	
	/**
	 * 根据镜像ID查询正在处理中的云主机列表
	 * ------------------------------
	 * @author zhouhaitao
	 * @param imageId
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public boolean checkOrderVmByImage(String imageId){
		boolean isExist = false;
		StringBuffer sql = new StringBuffer();
		sql.append("	SELECT                                                 ");
		sql.append("		cov.ordervm_id                                     ");
		sql.append("	FROM                                                   ");
		sql.append("		cloudorder_vm cov                                  ");
		sql.append("	LEFT JOIN order_info oi ON oi.order_no = cov.order_no  ");
		sql.append("	WHERE                                                  ");
		sql.append("		cov.image_id = ?                                   ");
		sql.append("	AND oi.order_state in ('1','2');                       ");
		
		javax.persistence.Query query = orderVmDao.createSQLNativeQuery(sql.toString(), new Object[]{imageId});
		List list = query.getResultList();
		isExist = list!=null && list.size()>0;
		return isExist;
	}
	
	/**
	 * 校验是否存在未完成的云主机订单使用了SSH密钥
	 * ------------------------------
	 * @author zhouhaitao
	 * @param secretkeyId
	 * 
	 * @return  存在 true;否则返回 false
	 */
	public boolean checkOrderVmBySecretkeyId(String secretkeyId){
		boolean isExist = false;
		StringBuffer sql = new StringBuffer();
		sql.append("	SELECT                                                 ");
		sql.append("		cov.ordervm_id                                     ");
		sql.append("	FROM                                                   ");
		sql.append("		cloudorder_vm cov                                  ");
		sql.append("	LEFT JOIN order_info oi ON oi.order_no = cov.order_no  ");
		sql.append("	WHERE                                                  ");
		sql.append("		cov.secret_key = ?                                 ");
		sql.append("	AND cov.order_type = '0'                               ");
		sql.append("	AND oi.order_state in ('1','2')                        ");
		
		javax.persistence.Query query = orderVmDao.createSQLNativeQuery(sql.toString(), new Object[]{secretkeyId});
		@SuppressWarnings("rawtypes")
		List list = query.getResultList();
		isExist = list!=null && list.size()>0;
		return isExist;
	}
}
