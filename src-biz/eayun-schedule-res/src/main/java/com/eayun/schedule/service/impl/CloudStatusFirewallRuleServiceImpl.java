package com.eayun.schedule.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.eayun.eayunstack.service.OpenstackFirewallRuleService;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.schedule.service.CloudFirewallRuleService;
import com.eayun.virtualization.dao.CloudFwRuleDao;
import com.eayun.virtualization.model.BaseCloudFwRule;

@Transactional
@Service
public class CloudStatusFirewallRuleServiceImpl implements CloudFirewallRuleService {
    private static final Logger log = LoggerFactory.getLogger(CloudStatusFirewallRuleServiceImpl.class);
	@Autowired
	private OpenstackFirewallRuleService openstackService ;
	@Autowired
	private  CloudFwRuleDao fwRuleDao;
	@Autowired
    private JedisUtil jedisUtil; 
	@Autowired
	private EcmcLogService ecmcLogService;
	@Autowired
    private SyncProgressUtil syncProgressUtil;
	
	@Override
	public void synchData(BaseDcDataCenter dataCenter) throws Exception {
		List<BaseCloudFwRule> dbList= new ArrayList<BaseCloudFwRule>();
		List<BaseCloudFwRule> stackList = new ArrayList<BaseCloudFwRule>();
		Map<String,BaseCloudFwRule> dbMap=new HashMap<String,BaseCloudFwRule>();
		Map<String,BaseCloudFwRule> stackMap=new HashMap<String,BaseCloudFwRule>();
		stackList = openstackService.getStackList(dataCenter);
		dbList = getFwRulesByDcId(dataCenter.getId());
		
		if(null!=dbList){
			for(BaseCloudFwRule cfr:dbList){
				dbMap.put(cfr.getFwrId(), cfr);
			}
		}
		long total = stackList == null ? 0L : stackList.size();
        syncProgressUtil.initResourceTotal(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.FIREWALL_RULE, total);
		if(null!=stackList){
			for(BaseCloudFwRule cloudFwRule:stackList){
				//底层数据存在本地数据库中 更新本地数据
				if(dbMap.containsKey(cloudFwRule.getFwrId())){
					updateCloudFwRuleFromStack(cloudFwRule);
				}
				else{
					fwRuleDao.saveOrUpdate(cloudFwRule);
				}
				stackMap.put(cloudFwRule.getFwrId(), cloudFwRule);
				syncProgressUtil.incrResourceDone(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.FIREWALL_RULE);
			}
		}
		
		if(null!=dbList){
			for(BaseCloudFwRule cfr:dbList){
				//删除本地数据库中不存在于底层的数据
				if(!stackMap.containsKey(cfr.getFwrId())){
					fwRuleDao.delete(cfr.getFwrId());
					ecmcLogService.addLog("同步资源清除数据",  toType(cfr), cfr.getFwrName(), cfr.getPrjId(),1,cfr.getFwrId(),null);
					JSONObject json = new JSONObject();
					json.put("resourceType", ResourceSyncConstant.FIREWALLRULE);
					json.put("resourceId", cfr.getFwrId());
					json.put("resourceName", cfr.getFwrName());
					json.put("synTime", new Date());
					jedisUtil.push(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, json.toJSONString());
				}
			}
		}
	}

	
	@SuppressWarnings("unchecked")
	public List<BaseCloudFwRule> getFwRulesByDcId(String dcId){
		StringBuffer hql = new StringBuffer();
		hql.append(" from  BaseCloudFwRule where dcId = ?");
		return fwRuleDao.find(hql.toString(), new Object []{dcId});
	}
	
	public boolean updateCloudFwRuleFromStack(BaseCloudFwRule cfr){
		boolean flag = false;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_fwrule set ");
			sql.append("	fwr_name = ?,               ");
			sql.append("	prj_id = ?,                 ");
			sql.append("	dc_id = ?,                  ");
			sql.append("	description = ?,            ");
			sql.append("	is_shared = ?,              ");
			sql.append("	fwr_status = ?,             ");
			sql.append("	protocol = ?,               ");
			sql.append("	source_port = ?,            ");
			sql.append("	source_ipaddress = ?,       ");
			sql.append("	destination_port = ?,       ");
			sql.append("	destination_ipaddress = ?,  ");
			sql.append("	ip_version = ?,             ");
			sql.append("	fwr_action = ?,             ");
			sql.append("	fwr_enabled = ?,            ");
			sql.append("	fwp_id = ?                  ");
			sql.append(" where fwr_id = ? ");
			
			fwRuleDao.execSQL(sql.toString(), new Object []{
					cfr.getFwrName(),
					cfr.getPrjId(),
					cfr.getDcId(),
					cfr.getDescription(),
					cfr.getIsShared(),
					cfr.getFwrStatus(),
					cfr.getProtocol(),
					cfr.getSourcePort(),
					cfr.getSourceIpaddress(),
					cfr.getDestinationPort(),
					cfr.getDestinationIpaddress(),
					cfr.getIpVersion(),
					cfr.getFwrAction(),
					cfr.getFwrEnabled(),
					cfr.getFwpId(),
					cfr.getFwrId()
			});
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			flag = false;
		}
		return flag;
	}
	private String toType(BaseCloudFwRule cfp){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StringBuffer resourceType = new StringBuffer();
		resourceType.append(ResourceSyncConstant.FIREWALLRULE);
		if(null != cfp && null != cfp.getCreateTime()){
			resourceType.append(ResourceSyncConstant.SEPARATOR).append("创建时间：").append(sdf.format(cfp.getCreateTime()));
		}
		
		
		return resourceType.toString();
	}
}
