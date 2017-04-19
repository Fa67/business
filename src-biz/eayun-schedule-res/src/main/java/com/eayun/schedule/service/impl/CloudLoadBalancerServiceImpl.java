package com.eayun.schedule.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.constant.SyncProgress.SyncByDatacenterTypes;
import com.eayun.common.constant.SyncProgress.SyncType;
import com.eayun.common.sync.SyncProgressUtil;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.service.OpenstackFloatIpService;
import com.eayun.schedule.service.CloudLoadBalancerService;
import com.eayun.virtualization.dao.CloudFloatIpDao;
import com.eayun.virtualization.model.BaseCloudFloatIp;
import com.eayun.virtualization.model.BaseCloudLdVip;
import com.eayun.virtualization.model.CloudFloatIp;

@Transactional
@Service
public class CloudLoadBalancerServiceImpl implements CloudLoadBalancerService {

	@Autowired
	private OpenstackFloatIpService openstackService ;
	@Autowired
	private CloudFloatIpDao cloudFloatIpDao;
	@Autowired
    private SyncProgressUtil syncProgressUtil;
	/**
	 * 同步底层负载均衡器的浮动IP
	 * -----------------
	 * @author zhouhaitao
	 * @param dataCenter
	 */
	public void synchData(BaseDcDataCenter dataCenter) throws Exception {
		List<CloudFloatIp> list=openstackService.getPoolFloatIpList(dataCenter);
		long total = list == null ? 0L : list.size();
        syncProgressUtil.initResourceTotal(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.LOAD_BALANCER, total);
		if(null!=list&&list.size()>0){
			for(CloudFloatIp floatIp : list){
				String poolId = queryPoolByPort(floatIp.getPortId(),floatIp.getSubnetIp());
				BaseCloudFloatIp cloudFloatIp = cloudFloatIpDao.findOne(floatIp.getFloId());
				if(null!=cloudFloatIp&&!StringUtils.isEmpty(cloudFloatIp.getFloId())){
					cloudFloatIp.setNetId(floatIp.getNetId());
					if(!StringUtils.isEmpty(poolId)){
						cloudFloatIp.setResourceId(poolId);
						cloudFloatIp.setResourceType("lb");
					}
					cloudFloatIpDao.saveOrUpdate(cloudFloatIp);
				}
				syncProgressUtil.incrResourceDone(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.LOAD_BALANCER);
			}
		}
	
	}

	
	@SuppressWarnings("unchecked")
	private String queryPoolByPort(String portId,String subnetIp){
		String poolId = null;
		StringBuffer hql = new StringBuffer ();
		
		hql.append(" from BaseCloudLdVip where portId = ? and  vipAddress = ?");
		
		List<BaseCloudLdVip> vips = cloudFloatIpDao.find(hql.toString(), new Object[]{
				portId,subnetIp
		});
		
		if(null!=vips&&vips.size()==1){
			poolId = vips.get(0).getPoolId();
		}
		
		return poolId;
	}
	
}
