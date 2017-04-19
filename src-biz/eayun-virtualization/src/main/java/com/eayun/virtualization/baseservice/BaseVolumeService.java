package com.eayun.virtualization.baseservice;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.Hook;
import com.eayun.eayunstack.model.Volume;
import com.eayun.eayunstack.service.OpenstackVolumeService;
import com.eayun.virtualization.dao.CloudVolumeDao;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.CloudVolume;
import com.eayun.virtualization.service.TagService;

@Transactional
@Service
public class BaseVolumeService {
	@Autowired
	private CloudVolumeDao volumeDao;
	@Autowired
	private OpenstackVolumeService openStackVolumeService;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private TagService tagService;
	
	
	
	
	
	
	
	
	
	/**
	 * 编辑云硬盘
	 * @author chengxiaodong
	 * @param vol
	 * @return
	 * @throws AppException
	 */
	public boolean updateVolume(CloudVolume volume)throws AppException {
		BaseCloudVolume vol=null;
		try{
			//拼装用于提交的数据
			JSONObject data=new JSONObject();
			JSONObject temp=new JSONObject();
			temp.put("name", volume.getVolName());
			temp.put("description", volume.getVolDescription());
			data.put("volume", temp);
			Volume result=openStackVolumeService.update(volume.getDcId(), volume.getPrjId(), data, volume.getVolId());
			
			if(null!=result){
				vol=volumeDao.findOne(volume.getVolId());
				vol.setVolName(volume.getVolName());
				vol.setVolDescription(volume.getVolDescription());
				volumeDao.saveOrUpdate(vol);
				return true;
			}
		
		
		}catch(AppException e){
			throw e;
		}
		return false;
		
	}
	
	
	/**
	 * 挂载云硬盘
	 * @author chengxiaodong
	 * @param vol
	 * @return
	 * @throws AppException
	 */
	public boolean bindVolume(CloudVolume vol) throws AppException{
		boolean isTrue=false;
		try {
			isTrue=openStackVolumeService.bind(vol.getDcId(), vol.getPrjId(), vol.getVolId(), vol.getVmId());
			if(isTrue){
				BaseCloudVolume volume=volumeDao.findOne(vol.getVolId());
				volume.setVolStatus("ATTACHING");
				volume.setVmId(vol.getVmId());
				volumeDao.saveOrUpdate(volume);
				
				//TODO 挂载云硬盘的自动任务
				JSONObject json =new JSONObject();
				json.put("volId", volume.getVolId());
				json.put("dcId",volume.getDcId());
				json.put("prjId", volume.getPrjId());
				json.put("volStatus",volume.getVolStatus());
				json.put("count", "0");
				
				
				final JSONObject datas = json;
				TransactionHookUtil.registAfterCommitHook(new Hook() {
					@Override
					public void execute() {
						jedisUtil.addUnique(RedisKey.volKey, datas.toJSONString());
					}
				});
				
			}
			
		}catch (AppException e) {
			throw e;
		}
		return isTrue;
	}
	
	
	
	/**
	 * 解绑云硬盘
	 * @author chengxiaodong
	 * @param vol
	 * @return
	 * @throws AppException
	 */
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public boolean debindVolume(CloudVolume vol){
		boolean isTrue=false;
		try {
			isTrue=openStackVolumeService.debind(vol.getDcId(), vol.getPrjId(), vol.getVolId(), vol.getVmId());
			if(isTrue){
				BaseCloudVolume volume=volumeDao.findOne(vol.getVolId());
				volume.setVmId(null);
				volume.setBindPoint(null);
				volume.setVolStatus("DETACHING");
				volumeDao.saveOrUpdate(volume);
				
				//TODO 解绑云硬盘的自动任务
				JSONObject json =new JSONObject();
				json.put("volId", volume.getVolId());
				json.put("dcId",volume.getDcId());
				json.put("prjId", volume.getPrjId());
				json.put("volStatus",volume.getVolStatus());
				json.put("count", "0");
				
				final JSONObject data = json;
				TransactionHookUtil.registAfterCommitHook(new Hook() {
					@Override
					public void execute() {
						jedisUtil.addUnique(RedisKey.volKey, data.toJSONString());
					}
				});
			}
			
		}catch (AppException e) {
			throw e;
		}
		return isTrue;
	}

	
	
	/**
	 * 解绑指定云主机下所有的数据盘
	 * @author chengxiaodong
	 * @param vmId
	 * @return
	 * @throws AppException
	 */
	public boolean debindVolsByVmId(String vmId){
		StringBuffer hql = new StringBuffer();
		hql.append(" from  ");
		hql.append(" BaseCloudVolume v where 1=1 and v.volBootable= :volBootable");
		hql.append(" and v.vmId = :vmId ");
		hql.append(" and v.isDeleted = :delFlag ");
		org.hibernate.Query query = volumeDao.getHibernateSession().createQuery(hql.toString());
		query.setParameter("volBootable", "0");
		query.setParameter("vmId", vmId);
		query.setParameter("delFlag", "0");
		@SuppressWarnings("unchecked")
		List<BaseCloudVolume> queryList = query.list();
		int count=0;
		if(queryList.size()>0){
			for(BaseCloudVolume baseVolume :queryList){
				CloudVolume volume=new CloudVolume();
				BeanUtils.copyPropertiesByModel(volume, baseVolume);
				debindVolume(volume);
				count++;
			}
		}
		
		if(count==queryList.size()){
			return true;
		}else{
			return false;
		}
		
	}
	
	
	
	/**
	 * 删除指定云主机的系统盘
	 */
	public void deleteVolumeByVm(String vmId,String deleteUser){
		StringBuffer querySql = new StringBuffer();
		querySql.append("from BaseCloudVolume where vmId = ? and volBootable ='1'");
		@SuppressWarnings("unchecked")
		List<BaseCloudVolume> volumeList = volumeDao.find(querySql.toString(), new Object[]{vmId});
		for(BaseCloudVolume vol:volumeList){
			vol.setVolStatus("DELETING");
			vol.setDeleteTime(new Date());
			vol.setDeleteUser(deleteUser);
			volumeDao.merge(vol);
			
			tagService.refreshCacheAftDelRes("volume",vol.getVolId());
			
			JSONObject json =new JSONObject();
			json.put("volId", vol.getVolId());
			json.put("dcId",vol.getDcId());
			json.put("prjId", vol.getPrjId());
			json.put("volStatus",vol.getVolStatus());
			json.put("count", "0");
			
			final JSONObject datas = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.volKey, datas.toJSONString());
				}
			});

		}
	}
	
	
	/**
	 * 查询指定云硬盘是否有已经存在的未处理订单
	 * @param volId
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public boolean checkVolOrderExsit(String volId){
		StringBuffer sql = new StringBuffer();
		sql.append("		SELECT                                               ");
		sql.append("			vol.ordervol_id                                  ");
		sql.append("		FROM                                                 ");
		sql.append("			cloudorder_volume vol                            ");
		sql.append("		LEFT JOIN order_info oi                              ");
		sql.append("		ON vol.order_no = oi.order_no                        ");
		sql.append("		WHERE                                                ");
		sql.append("			vol.vol_id = ?                                   ");
		sql.append("		AND (                                                ");
		sql.append("			oi.order_state = '1'                             ");
		sql.append("			OR oi.order_state = '2'                          ");
		sql.append("		)                                                    ");
		sql.append("		AND (                                                ");
		sql.append("			vol.order_type = '1'                             ");
		sql.append("			OR vol.order_type = '2'                          ");
		sql.append("		)                                                    ");
		
		javax.persistence.Query query = volumeDao.createSQLNativeQuery(sql.toString(), new Object[]{volId});
		List resultList = query.getResultList();
		return resultList != null && resultList.size()>0;
	}

	
}
