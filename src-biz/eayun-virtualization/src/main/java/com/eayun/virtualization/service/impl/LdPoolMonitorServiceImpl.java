package com.eayun.virtualization.service.impl;

import java.util.List;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.util.BeanUtils;
import com.eayun.virtualization.dao.CloudLdPoolMonitorDao;
import com.eayun.virtualization.model.BaseCloudLdPoolMonitor;
import com.eayun.virtualization.service.LdPoolMonitorService;
@Service
@Transactional
public class LdPoolMonitorServiceImpl implements LdPoolMonitorService {
	private static final Logger log = LoggerFactory
			.getLogger(LdPoolMonitorServiceImpl.class);
	@Autowired
	private CloudLdPoolMonitorDao poolMonitorDao;
	public BaseCloudLdPoolMonitor saveEntiry(BaseCloudLdPoolMonitor entity){
		BaseCloudLdPoolMonitor baseCloudLd = new BaseCloudLdPoolMonitor();
		BeanUtils.copyPropertiesByModel(baseCloudLd, entity);
		poolMonitorDao.saveEntity(baseCloudLd);
		return baseCloudLd;
	}
	public int deleteByPoolIdAndMonitorId(String sql, String poolId,String monitorId){
		int num = 0;
		 StringBuffer hql = new StringBuffer();
	        hql.append("delete BaseCloudLdPoolMonitor where poolId = ? and ldmId = ?");
	        num = poolMonitorDao.executeUpdate(hql.toString(), poolId,monitorId);
		return num;
	}
	
	/**
	 * 查询负载均衡器下的健康检查
	 * -------------------
	 * @author zhouhaitao
	 * @param poolId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<BaseCloudLdPoolMonitor> getMonitorByPool(String poolId){
		StringBuffer hql =new StringBuffer();
		hql.append(" from BaseCloudLdPoolMonitor where poolId = ? ");
		return poolMonitorDao.find(hql.toString(), new Object[]{poolId});
	}
	
	@Override
	public boolean hasTakeEffectHealthMonitor(String poolId) throws Exception {
		StringBuffer sql=new StringBuffer("		select ldm_id from cloud_ldpoolldmonitor where pool_id=?	");
		Query query=poolMonitorDao.createSQLNativeQuery(sql.toString(), poolId);
		List listResult =query.getResultList();
		if(listResult!=null&&listResult.size()>0){
			return true;
		}else{
			return false;
		}
	}
}
