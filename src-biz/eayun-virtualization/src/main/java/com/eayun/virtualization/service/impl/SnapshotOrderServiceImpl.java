package com.eayun.virtualization.service.impl;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.util.BeanUtils;
import com.eayun.virtualization.dao.CloudSnapshotOrderDao;
import com.eayun.virtualization.model.BaseCloudOrderSnapshot;
import com.eayun.virtualization.model.CloudOrderSnapshot;
import com.eayun.virtualization.service.SnapshotOrderService;

@Service
@Transactional
public class SnapshotOrderServiceImpl implements SnapshotOrderService {
    private static final Logger log = LoggerFactory.getLogger(SnapshotOrderServiceImpl.class);
	@Autowired
	private CloudSnapshotOrderDao snapOrderDao;

	@Override
	public CloudOrderSnapshot getSnapOrderByOrderNo(String orderNo)
			throws Exception {
		BaseCloudOrderSnapshot orderSnapshot=snapOrderDao.getSnapOrderByOrderNo(orderNo);
		CloudOrderSnapshot snapOrder=new CloudOrderSnapshot();
		BeanUtils.copyPropertiesByModel(snapOrder, orderSnapshot);
		return snapOrder;
	}

	@Override
	public void addOrderSnapshot(CloudOrderSnapshot orderSnapshot)
			throws Exception {
		BaseCloudOrderSnapshot baseSnapOrder=new BaseCloudOrderSnapshot();
		BeanUtils.copyPropertiesByModel(baseSnapOrder, orderSnapshot);
		
		baseSnapOrder.setOrderSnapId(UUID.randomUUID()+"");
		snapOrderDao.saveOrUpdate(baseSnapOrder);

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
			hql.append(" update BaseCloudOrderSnapshot set orderResources = ? where orderNo = ? ");
			snapOrderDao.executeUpdate(hql.toString(), new Object[]{resourceJson,orderNo});
			isSuccess = true;
		}catch(Exception e){
			isSuccess = false;
			log.error(e.toString(),e);
			throw e;
		}
		
		return isSuccess;
	}

	/**
	 * 根据订单号查询资源创建信息
	 */
	@Override
	public CloudOrderSnapshot getOrderByOrderNo(String orderNo) {
		StringBuffer sql=new  StringBuffer();
		sql.append("SELECT ord.order_no AS orderNo,ord.prj_id AS prjId,ord.dc_id AS dcId,ord.order_type AS orderType,");
		sql.append(" ord.vol_id AS volId, ord.pay_type AS payType,ord.snap_size AS snapSize,ord.snap_name AS snapName,");
		sql.append(" ord.snap_description AS snapDescription,ord.snap_type AS snapType,vol.vol_name AS volName,dc.dc_name AS dcName");
	    sql.append(" FROM cloudorder_snapshot ord LEFT JOIN cloud_volume vol ON ord.vol_id = vol.vol_id LEFT JOIN dc_datacenter dc ON ord.dc_id = dc.id");
	    sql.append("  WHERE ord.order_no = ? ");
	    Object[] params = new Object[1];
	    params[0]=orderNo;
	    int index=0;
	    javax.persistence.Query query = snapOrderDao.createSQLNativeQuery(sql.toString(), params);
		List listResult = query.getResultList();
        	Object[] objs = (Object[])listResult.get(0);
        	CloudOrderSnapshot snapOrder=new CloudOrderSnapshot();
        	snapOrder.setOrderNo(String.valueOf(objs[index++]));
        	snapOrder.setPrjId(String.valueOf(objs[index++]));
        	snapOrder.setDcId(String.valueOf(objs[index++]));
        	snapOrder.setOrderType(String.valueOf(objs[index++]));
        	snapOrder.setVolId(String.valueOf(objs[index++]));
        	snapOrder.setPayType(String.valueOf(objs[index++]));
        	snapOrder.setSnapSize(Integer.parseInt(String.valueOf(objs[index++])));
        	snapOrder.setSnapName(String.valueOf(objs[index++]));
        	snapOrder.setSnapDescription(String.valueOf(objs[index++]));
        	snapOrder.setSnapType(String.valueOf(objs[index++]));
        	snapOrder.setVolName(String.valueOf(objs[index++]));
        	snapOrder.setDcName(String.valueOf(objs[index++]));
		return snapOrder;
	}

	


}
