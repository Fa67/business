package com.eayun.schedule.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceSyncConstant;
import com.eayun.common.constant.SyncProgress.SyncByDatacenterTypes;
import com.eayun.common.constant.SyncProgress.SyncType;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.sync.SyncProgressUtil;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.service.OpenstackFirewallPolicyService;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.schedule.service.CloudFirewallPolicyService;
import com.eayun.virtualization.dao.CloudFwPolicyDao;
import com.eayun.virtualization.model.BaseCloudFwPolicy;

@Transactional
@Service
public class CloudStatusFirewallPolicyServiceImpl implements CloudFirewallPolicyService {
    private static final Logger log = LoggerFactory.getLogger(CloudStatusFirewallPolicyServiceImpl.class);
	@Autowired
	private OpenstackFirewallPolicyService openstackService;
	@Autowired
	private CloudFwPolicyDao cloudFwPolicyDao;
	@Autowired
    private JedisUtil jedisUtil; 
	@Autowired
	private EcmcLogService ecmcLogService;
    @Autowired
    private SyncProgressUtil syncProgressUtil;
	
	@Override
	public void synchData(BaseDcDataCenter dataCenter) throws Exception {

		Map<String,BaseCloudFwPolicy> dbMap=new HashMap<String,BaseCloudFwPolicy>();
		Map<String,BaseCloudFwPolicy> stackMap=new HashMap<String,BaseCloudFwPolicy>();
		List<BaseCloudFwPolicy> dbList=queryCloudFwPolicyListByDcId(dataCenter.getId());
		List<BaseCloudFwPolicy> list = openstackService.getStackList(dataCenter);
		
		if(null!=dbList){
			for(BaseCloudFwPolicy cfp:dbList){
				dbMap.put(cfp.getFwpId(), cfp);
			}
		}
		long total = list == null ? 0L : list.size();
        syncProgressUtil.initResourceTotal(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.FIREWALL_POLICY, total);
		if(null!=list){
			for(BaseCloudFwPolicy cloudFwPolicy:list){
				//底层数据存在本地数据库中 更新本地数据
				if(dbMap.containsKey(cloudFwPolicy.getFwpId())){
					updateCloudFwPolicyFromStack(cloudFwPolicy);
				}
				else{
					cloudFwPolicyDao.saveOrUpdate(cloudFwPolicy);
				}
				stackMap.put(cloudFwPolicy.getFwpId(), cloudFwPolicy);
				syncProgressUtil.incrResourceDone(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.FIREWALL_POLICY);
			}
		}
		
		if(null!=dbList){
			for(BaseCloudFwPolicy cfp:dbList){
				//删除本地数据库中不存在于底层的数据
				if(!stackMap.containsKey(cfp.getFwpId())){
					cloudFwPolicyDao.delete(cfp.getFwpId());
					ecmcLogService.addLog("同步资源清除数据",  toType(cfp), cfp.getFwpName(), cfp.getPrjId(),1,cfp.getFwpId(),null);
					JSONObject json = new JSONObject();
					json.put("resourceType", ResourceSyncConstant.FIREWALLPOLICY);
					json.put("resourceId", cfp.getFwpId());
					json.put("resourceName", cfp.getFwpName());
					json.put("synTime", new Date());
					jedisUtil.push(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, json.toJSONString());
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public List<BaseCloudFwPolicy> queryCloudFwPolicyListByDcId (String dcId){
		StringBuffer hql = new StringBuffer();
		hql.append(" from  BaseCloudFwPolicy where dcId = ? ");
		return cloudFwPolicyDao.find(hql.toString(), new Object[]{dcId});
	}
	
	public boolean updateCloudFwPolicyFromStack (BaseCloudFwPolicy cloudFwPolicy){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_fwpolicy set ");
			sql.append("	fwp_name = ?,    "); 
			sql.append("	prj_id = ?,      "); 
			sql.append("	dc_id =? ,       "); 
			sql.append("	description =? , "); 
			sql.append("	is_shared = ?,   "); 
			sql.append("	audited = ?,     "); 
			sql.append("	fwp_status = ?  "); 
			sql.append(" where fwp_id = ? ");
			
			cloudFwPolicyDao.execSQL(sql.toString(), new Object []{
					cloudFwPolicy.getFwpName(),
					cloudFwPolicy.getPrjId(),
					cloudFwPolicy.getDcId(),
					cloudFwPolicy.getDescription(),
					cloudFwPolicy.getIsShared(),
					cloudFwPolicy.getAudited(),
					cloudFwPolicy.getFwpStatus(),
					cloudFwPolicy.getFwpId()
			});
			flag = true;
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			flag = false ;
		}
		return flag;
	}
	private String toType(BaseCloudFwPolicy cfp){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StringBuffer resourceType = new StringBuffer();
		resourceType.append(ResourceSyncConstant.FIREWALLPOLICY);
		if(null != cfp && null != cfp.getCreateTime()){
			resourceType.append(ResourceSyncConstant.SEPARATOR).append("创建时间：").append(sdf.format(cfp.getCreateTime()));
		}
		
		return resourceType.toString();
	}
}
