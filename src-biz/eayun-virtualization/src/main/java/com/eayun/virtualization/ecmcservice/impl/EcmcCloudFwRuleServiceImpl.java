package com.eayun.virtualization.ecmcservice.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.util.BeanUtils;
import com.eayun.eayunstack.model.FirewallRule;
import com.eayun.eayunstack.service.OpenstackFirewallRuleService;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.virtualization.dao.CloudFwRuleDao;
import com.eayun.virtualization.ecmcservice.EcmcCloudFireWallPoliyService;
import com.eayun.virtualization.ecmcservice.EcmcCloudFwRuleService;
import com.eayun.virtualization.model.BaseCloudFwRule;
import com.eayun.virtualization.model.CloudFwPolicy;
import com.eayun.virtualization.model.CloudFwRule;

/**
 * @author jingang.liu@eayun.com to beijing
 * @date 2016年4月12日
 */
@Service
@Transactional
public class EcmcCloudFwRuleServiceImpl implements EcmcCloudFwRuleService {
    private static final Logger log = LoggerFactory.getLogger(EcmcCloudFwRuleServiceImpl.class);
	@Autowired
	private CloudFwRuleDao fwruledao;
	@Autowired
	private OpenstackFirewallRuleService openstackfwruleservice;
	@Autowired
	private EcmcCloudFireWallPoliyService fwpService; 
	
	@Override
 	public Page list(Page page, String datacenterId,String prjName, String name,String cusOrg, QueryMap querymap,String fwpId) throws AppException {
		int index = 0;
		Object[] args = new Object[4];
		
		StringBuffer sql=new StringBuffer();
		sql.append("select fwr.fwr_id as fwrId,fwr.fwr_name as fwrName,fwr.protocol as protocol,fwr.source_port as sourcePort,");
		sql.append(" fwr.source_ipaddress as sourceIpaddress,fwr.destination_ipaddress as destinationIpaddress,fwr.destination_port as destinationPort,");
		sql.append(" fwr.ip_version as ipVersion,fwr.fwr_action as fwrAction,fwr.fwr_enabled as fwrEnabled,fwr.is_shared as isShared,fwr.description as description,"
				+ " fwp.fwp_name as fwpName,cp.prj_name as prjName,dc.id as dcId,dc.dc_name as dcName,ct.cus_name as cusName,cp.prj_id as prjId,ct.cus_org as cusOrg,"
				+ "fwr.fwr_priority as fwr_priority,fwr.fwp_id as fwpId,fwr.create_time as createTime ");
		sql.append(" from cloud_fwrule fwr");
		sql.append(" left outer join cloud_project cp on fwr.prj_id=cp.prj_id");
		sql.append(" left outer join sys_selfcustomer ct on ct.cus_id=cp.customer_id");
		sql.append(" left outer join cloud_fwpolicy fwp on fwr.fwp_id=fwp.fwp_id");
		sql.append(" left outer join dc_datacenter dc on fwr.dc_id=dc.id");
		sql.append(" where 1=1");
		
	 	if (null!=prjName&&!"".equals(prjName)) {
	 		sql.append(" and cp.prj_name in (?").append(index+1).append(") ");
			args[index] = Arrays.asList(StringUtils.split(prjName, ","));
			index++;
	 		
		}
	 	if (null != cusOrg && !"".equals(cusOrg)) {
			sql.append(" and ct.cus_org in (?").append(index+1).append(") ");
	 		args[index] = Arrays.asList(StringUtils.split(cusOrg, ","));
			index++;
		}
	 	if (!"null".equals(datacenterId)&&null!=datacenterId&&!"".equals(datacenterId)&&!"undefined".equals(datacenterId)) {
	 		sql.append(" and fwr.dc_id in (?").append(index+1).append(") ");
	    	args[index] = datacenterId;
			index++;
		}
	    if(null!=name&&!"".equals(name)){
	    	sql.append(" and binary fwr.fwr_name like (?").append(index+1).append(") ");
	    	name = name.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	    	args[index] = "%" + name + "%";
			index++;
	    }
	    if(!"null".equals(fwpId) && null!=fwpId && !"".equals(fwpId) && !"undefined".equals(fwpId)){
	    	sql.append(" and fwp.fwp_id = ? ");
	    	args[index] = fwpId;
	    	index++;
	    }
	    Object[] params = new Object[index];
		System.arraycopy(args, 0, params, 0, index);
	    sql.append(" order by fwr.fwr_priority ");
		
	    page = fwruledao.pagedNativeQuery(sql.toString(), querymap, params);
	    List newlist = (List) page.getResult();
        int a = newlist.size();
        for (int i = 0; i < a; i++) {
            Object[] objs = (Object[]) newlist.get(i);
            CloudFwRule cfi = new CloudFwRule();
            cfi.setFwrId(ObjectUtils.toString(objs[0]));
            cfi.setFwrName(ObjectUtils.toString(objs[1]));
            cfi.setProtocol(ObjectUtils.toString(objs[2]));
            cfi.setSourcePort(ObjectUtils.toString(objs[3]));
            cfi.setSourceIpaddress(ObjectUtils.toString(objs[4]));
            cfi.setDestinationIpaddress(ObjectUtils.toString(objs[5]));
            cfi.setDestinationPort(ObjectUtils.toString(objs[6]));
            cfi.setIpVersion(ObjectUtils.toString(objs[7]));
            cfi.setFwrAction(ObjectUtils.toString(objs[8]));
            cfi.setFwrEnabled(ObjectUtils.toString(objs[9]));
            cfi.setIsShared(ObjectUtils.toString(objs[10]));
            cfi.setDescription(ObjectUtils.toString(objs[11]));
            cfi.setFwpName(ObjectUtils.toString(objs[12]));
            cfi.setPrjName(ObjectUtils.toString(objs[13]));
            cfi.setDcId(ObjectUtils.toString(objs[14]));
            cfi.setDcName(ObjectUtils.toString(objs[15]));
            cfi.setCusName(ObjectUtils.toString(objs[16]));
            cfi.setPrjId(ObjectUtils.toString(objs[17]));
            cfi.setCusOrg(ObjectUtils.toString(objs[18]));
            cfi.setPriority(ObjectUtils.toString(objs[19]));
            cfi.setFwpId(ObjectUtils.toString(objs[20]));
            cfi.setCreateTime((Date)objs[21]);
            newlist.set(i, cfi);
        }
		return page;
	}

	@Override
	public boolean checkFwRuleName(String datacenterId, String projectId, String fwrName, String fwrId)
			throws AppException {
		boolean isExist = false;
		StringBuffer sql = new StringBuffer();
		int index=0;
		Object [] args=new Object[4];
		sql.append("select count(*) from cloud_fwrule  as fwr  where 1=1 ");
		//项目
		if (!"null".equals(projectId)&&null!=projectId&&!"".equals(projectId)&&!"undefined".equals(projectId)) {
			sql.append(" and fwr.prj_id= ? ");
			args[index]=projectId;
			index++;
		}
		//数据中心
		if (!"".equals(datacenterId)&&datacenterId!=null&&!"undefined".equals(datacenterId)&&!"null".equals(datacenterId)) {
			sql.append("and fwr.dc_id = ? ");
			args[index]=datacenterId;
			index++;
		}
		//规则名称
		if (!"".equals(fwrName)&&fwrName!=null) {
			sql.append("and binary fwr.fwr_name = ? ");
			args[index]=fwrName.trim();
			index++;
		}
		//规则ID
		if (!"".equals(fwrId)&&fwrId!=null&&!"undefined".equals(fwrId)&&!"null".equals(fwrId)) {
			sql.append("and fwr.fwr_id <> ? ");
			args[index]=fwrId.trim();
			index++;
		}
						
		Object[] params = new Object[index];  
        System.arraycopy(args, 0, params, 0, index);
		int ret = ((Number)fwruledao.createSQLNativeQuery(sql.toString(), params).getSingleResult()).intValue();
		if(ret>0){
			isExist = true;//返回true 代表存在此名称
		}
		return isExist;
	}
	
	@Override
	public List<BaseCloudFwRule> listForPolicy(String projectId,String datacenterId) throws AppException {
		return fwruledao.find("from CloudFwRule as fwr where fwr.fwpId is null and fwr.prjId = ? ", projectId);
	}
	
	@Override
	public CloudFwRule getById(String id) throws AppException {
		CloudFwRule cloudFwRule = null;
		try{	
			int index=0;
			Object [] args=new Object[3];
			StringBuffer sql=new StringBuffer();
			sql.append("select fwr.fwr_name as fwrName,");
			sql.append(" fwr.protocol as protocol ,");
			sql.append(" fwr.source_ipaddress as sourceIpaddress, ");
			sql.append(" fwr.source_port as sourcePort, ");
			sql.append(" fwr.destination_ipaddress as destinationIpaddress, ");
			sql.append(" fwr.destination_port as destinationPort, ");
			sql.append(" fwr.ip_version as ipVersion, ");
			sql.append(" fwr.fwr_action as fwrAction, ");
			sql.append(" fwr.fwr_enabled as fwrEnabled, ");
			sql.append(" fwp.fwp_name as fwpName, ");
			sql.append(" fwr.description as description, ");
			sql.append(" fwr.fwr_id as fwrId ,");
			sql.append(" fwr.is_shared as isShared ,");
			sql.append(" fwr.fwp_id as fwpId,fwr.prj_id as prjId ");
			sql.append(" from cloud_fwrule fwr ");
			sql.append(" left outer join cloud_fwpolicy fwp on fwr.fwp_id=fwp.fwp_id");
			sql.append(" where 1=1");
		    if(!"null".equals(id)&&null!=id&&!"".equals(id)&&!"undefined".equals(id)){
		    	sql.append(" and fwr.fwr_id =?");
		    	args[index]=id;
				index++;
		    }
		    
		    Object[] params = new Object[index];  
	        System.arraycopy(args, 0, params, 0, index);
	        List list =  fwruledao.createSQLNativeQuery(sql.toString(),params).getResultList();
	        if(list!=null && list.size()>0){
	        	Object[] objs = (Object[])list.get(0);
	        	cloudFwRule = new CloudFwRule();
	        	cloudFwRule.setFwrName(ObjectUtils.toString(objs[0]));
	        	cloudFwRule.setProtocol(ObjectUtils.toString(objs[1]));
	        	cloudFwRule.setSourceIpaddress(ObjectUtils.toString(objs[2]));
	        	cloudFwRule.setSourcePort(ObjectUtils.toString(objs[3]));
	        	cloudFwRule.setDestinationIpaddress(ObjectUtils.toString(objs[4]));
	        	cloudFwRule.setDestinationPort(ObjectUtils.toString(objs[5]));
	        	cloudFwRule.setIpVersion(ObjectUtils.toString(objs[6]));
	        	cloudFwRule.setFwrAction(ObjectUtils.toString(objs[7]));
	        	cloudFwRule.setFwrEnabled(ObjectUtils.toString(objs[8]));
	        	cloudFwRule.setFwpName(ObjectUtils.toString(objs[9]));
	        	cloudFwRule.setDescription(ObjectUtils.toString(objs[10]));
	        	cloudFwRule.setFwrId(ObjectUtils.toString(objs[11]));
	        	cloudFwRule.setIsShared(ObjectUtils.toString(objs[12]));
	        	cloudFwRule.setFwpId(ObjectUtils.toString(objs[13]));
	        	cloudFwRule.setPrjId(ObjectUtils.toString(objs[14]));
	        }
			return cloudFwRule ;
		}catch(AppException ae){
			throw ae;
		}catch (Exception e) {
			log.error(e.toString(),e);
			//当执行出现未知异常时，打印异常信息，并返回空
			return null;
		}
	}
	
	@Override
	public FirewallRule createCloudFwRule(Map<String, String> parmes) throws AppException {
		String name = parmes.get("name");
		String protocol =  parmes.get("protocol");
		String sourcePort =  parmes.get("source_port");
		String sourceIpaddress =  parmes.get("source_ip_address");
		String destinationPort =  parmes.get("destination_port");
		String destinationIpaddress =  parmes.get("destination_ip_address");
		String ipVersion =  parmes.get("ip_version");
		String fwrAction =  parmes.get("action");
//		String fwrEnabled =  parmes.get("enabled");
//		String description =  parmes.get("description");
		String datacenterId = parmes.get("datacenterId");
		String projectId = parmes.get("projectId");
		//防火墙规则数据
		JSONObject fwrule = new JSONObject();			
		fwrule.put("name", name);
		if("any".equals(protocol)){
			if ("".equals(protocol)||null==protocol) {
				protocol="任何";
				}
		}else{
			fwrule.put("protocol", protocol);
		}
		if(sourcePort==null || "".equals(sourcePort)){
			fwrule.put("source_port", null);
		}else{
			fwrule.put("source_port", sourcePort);
		}
		if(sourceIpaddress==null || "".equals(sourceIpaddress)){
			fwrule.put("source_ip_address", null);
		}else{
			fwrule.put("source_ip_address", sourceIpaddress);
		}
		if(destinationPort==null || "".equals(destinationPort)){
			fwrule.put("destination_port", null);
		}else{
			fwrule.put("destination_port", destinationPort);
		}
		if(destinationIpaddress==null || "".equals(destinationIpaddress)){
			fwrule.put("destination_ip_address", null);
		}else{
		fwrule.put("destination_ip_address", destinationIpaddress);
		}
		fwrule.put("ip_version", ipVersion);
		fwrule.put("action", fwrAction);
		fwrule.put("enabled", "1");
//		fwrule.put("description", description);
		//用于提交的完整数据
		JSONObject resultData = new JSONObject();
		resultData.put("firewall_rule", fwrule);
		try {
			//openstack创建网络
			FirewallRule firewallRule = openstackfwruleservice.create(datacenterId, projectId, resultData);
			//保存至数据库
			if(firewallRule!=null){
				BaseCloudFwRule cloudFwRule=new BaseCloudFwRule();
				cloudFwRule.setFwrId(firewallRule.getId());
				cloudFwRule.setFwrName(firewallRule.getName());
				cloudFwRule.setCreateName(EcmcSessionUtil.getUser() != null ? EcmcSessionUtil.getUser().getAccount() : null);
				cloudFwRule.setCreateTime(new Date());		
				cloudFwRule.setPrjId(projectId);
				cloudFwRule.setDcId(datacenterId);
				cloudFwRule.setProtocol(protocol);
				cloudFwRule.setSourcePort(firewallRule.getSource_port());
				cloudFwRule.setSourceIpaddress(firewallRule.getSource_ip_address());
				cloudFwRule.setDestinationPort(firewallRule.getDestination_port());
				cloudFwRule.setDestinationIpaddress(firewallRule.getDestination_ip_address());
				cloudFwRule.setIpVersion(firewallRule.getIp_version());
				cloudFwRule.setFwrAction(firewallRule.getAction());
				//cloudFwRule.setFwrEnabled(fwrEnabled);
//				cloudFwRule.setDescription(firewallRule.getDescription());
				cloudFwRule.setFwrEnabled("1");//1是，0否
				fwruledao.save(cloudFwRule);
				
			}
			return firewallRule;
		}catch (AppException e) {
			throw e;
		}catch (Exception e) {
		    log.error(e.toString(),e);
			return null;
		}
	}
	@Override
	public FirewallRule createFwRuleToPoliy(Map<String, String> parmes) throws AppException {
		FirewallRule result = createCloudFwRule(parmes);
		if (result!=null) {
			//查询出原策略下的所有规则
			List<BaseCloudFwRule> listcr = fwpService.getByFwrId(parmes.get("fwpId"));
			//绑定规则到策略
			List<CloudFwRule> firewallRules = new ArrayList<CloudFwRule>();
			CloudFwRule rule = new CloudFwRule();
			rule.setFwrId(result.getId());
			firewallRules.add(rule);
			for(BaseCloudFwRule cr : listcr){
				rule = new CloudFwRule();
				rule.setFwrId(cr.getFwrId());
				firewallRules.add(rule);
			}
			CloudFwPolicy fwp = new CloudFwPolicy();
			fwp.setFwpId(parmes.get("fwpId"));
			fwp.setDcId(parmes.get("datacenterId"));
			fwp.setPrjId(parmes.get("projectId"));
			fwp.setFirewallRules(firewallRules);
			fwpService.toDoFwRule(fwp);//这个方法是修改规则，不是添加规则
		}
		return result;
	}
	@Override
	public FirewallRule updateCloudFwRule(Map<String, String> parmes) throws AppException {
		String name = parmes.get("name");
		String protocol =  parmes.get("protocol");
		String sourcePort =  parmes.get("source_port");
		String sourceIpaddress =  parmes.get("source_ip_address");
		String destinationPort =  parmes.get("destination_port");
		String destinationIpaddress =  parmes.get("destination_ip_address");
		String ipVersion =  parmes.get("ip_version");
		String fwrAction =  parmes.get("action");
		String datacenterId = parmes.get("datacenterId");
		String projectId = parmes.get("projectId");
		String id = parmes.get("id");
		//防火墙规则数据
		net.sf.json.JSONObject fwrule = new net.sf.json.JSONObject();			
		fwrule.put("name", name);
		if ("any".equals(protocol)||null==protocol) {
			fwrule.put("protocol","null");
		}else{
			fwrule.put("protocol", protocol);
		}
		if("".equals(sourcePort)||null==sourcePort){
			fwrule.put("source_port", "null");
		}else{
			if("tcp".equals(protocol)||"udp".equals(protocol)){
				fwrule.put("source_port", sourcePort);
			}else{
				fwrule.put("source_port", "null");
			}
			
		}
		if(sourceIpaddress!=null &&!"".equals(sourceIpaddress)){
			fwrule.put("source_ip_address", sourceIpaddress);
		}else{
			fwrule.put("source_ip_address", "null");
		}
		if("".equals(destinationPort)||null==destinationPort){
			fwrule.put("destination_port", "null");
		}else {
			if("tcp".equals(protocol)||"udp".equals(protocol)){
				fwrule.put("destination_port", destinationPort);
			}else{
				fwrule.put("destination_port", "null");
			}
			
		}
		if(destinationIpaddress!=null && !"".equals(destinationIpaddress)){
			fwrule.put("destination_ip_address", destinationIpaddress);
		}else{
			fwrule.put("destination_ip_address", "null");
		}
		fwrule.put("ip_version", ipVersion);
		fwrule.put("action", fwrAction);
		//用于提交的完整数据
		net.sf.json.JSONObject resultData = new net.sf.json.JSONObject();
		resultData.put("firewall_rule", fwrule);
		BaseCloudFwRule cloudFwRule = null;
		try {
			//openstack创建网络
			FirewallRule firewallRule = openstackfwruleservice.updateByNetJson(datacenterId,projectId,resultData,id);
			//保存至数据库
			if(firewallRule!=null){
				cloudFwRule = fwruledao.findOne(id);
				if(cloudFwRule==null){
					throw new AppException("BaseCloudFwRule is null");
				}
				cloudFwRule.setFwrId(firewallRule.getId());
				cloudFwRule.setFwrName(firewallRule.getName());
				cloudFwRule.setCreateName(EcmcSessionUtil.getUser() != null ? EcmcSessionUtil.getUser().getAccount() : null);
				cloudFwRule.setPrjId(projectId);
				cloudFwRule.setDcId(datacenterId);
				if(firewallRule.getProtocol()!=null){
					cloudFwRule.setProtocol(firewallRule.getProtocol());
				}else{
					cloudFwRule.setProtocol("any");
				}
				cloudFwRule.setSourcePort(firewallRule.getSource_port());
				cloudFwRule.setSourceIpaddress(firewallRule.getSource_ip_address());
				cloudFwRule.setDestinationPort(firewallRule.getDestination_port());
				cloudFwRule.setDestinationIpaddress(firewallRule.getDestination_ip_address());
				cloudFwRule.setIpVersion(firewallRule.getIp_version());
				cloudFwRule.setFwrAction(firewallRule.getAction());
//				cloudFwRule.setFwrEnabled(fwrEnabled);
//				cloudFwRule.setDescription(firewallRule.getDescription());
				cloudFwRule.setFwrEnabled("1");//1是，0否
				fwruledao.saveOrUpdate(cloudFwRule);
				
			}
			return firewallRule;
		}catch (AppException e) {
			throw e;
		}catch (Exception e) {
		    log.error(e.toString(),e);
			return null;
		}
	}
	
	@Override
	public boolean delete(String datacenterId,String projectId, String id) throws AppException {
		try {
			//执行openstack删除操作成功后，进行后续操作
			if (openstackfwruleservice.delete(datacenterId, projectId, id)) {
				fwruledao.delete(id);
			}
			return true;
		}catch (AppException e) {
			throw e;
		}catch (Exception e) {
		    log.error(e.toString(),e);
			return false;
		}
	}
	
	@Override
	public boolean deleteFwRuletoPolicy(String datacenterId, String projectId, String id, String fwpId)
			throws AppException {
		List<BaseCloudFwRule> listRule = fwpService.getByFwrId(fwpId);
		List<CloudFwRule> rules = new ArrayList<CloudFwRule>();
		CloudFwRule cfwr = null; 
		for(int i=0;i<listRule.size();i++){
			if(!id.equals(listRule.get(i).getFwrId())){//移除当前规则
				cfwr = new CloudFwRule(); 
				cfwr.setFwrId(listRule.get(i).getFwrId());
				rules.add(cfwr);
			}
		}
		CloudFwPolicy policy = new CloudFwPolicy();
		policy.setFwpId(fwpId);
		policy.setDcId(datacenterId);
		policy.setPrjId(projectId);
		policy.setFirewallRules(rules);
		if(fwpService.toDoFwRule(policy)){//底层解绑策略（这里不是全部解绑，所以是修改策略关联的规则）
			return delete(datacenterId,projectId,id);
		}
		return false;
	}
	
	@Override
	public List<CloudFwRule> getFwRulesByPrjId(String dcId, String prjId)
			throws AppException {
		List<BaseCloudFwRule> list=fwruledao.getFwRulesByPrjId(dcId,prjId);
		List<CloudFwRule> result = new ArrayList<CloudFwRule>();
		for (BaseCloudFwRule baseCloudFwRule : list) {
			CloudFwRule fwr = new CloudFwRule();
			BeanUtils.copyPropertiesByModel(fwr, baseCloudFwRule);
			result.add(fwr);
		}
		return result;
	}
	
	@Override
	public FirewallRule isEnabled(Map<String, String> parmes) throws AppException {
		String enabled = parmes.get("fwrEnabled");
		String datacenterId = parmes.get("dcId");
		String projectId = parmes.get("prjId");
		String id = parmes.get("fwrId");
		//防火墙规则数据
		net.sf.json.JSONObject fwrule = new net.sf.json.JSONObject();			
		fwrule.put("enabled", enabled);
		//用于提交的完整数据
		net.sf.json.JSONObject resultData = new net.sf.json.JSONObject();
		resultData.put("firewall_rule", fwrule);
		BaseCloudFwRule cloudFwRule = null;
		try {
			//openstack创建网络
			FirewallRule firewallRule = openstackfwruleservice.updateByNetJson(datacenterId,projectId,resultData,id);
			//保存至数据库
			if(firewallRule!=null){
				cloudFwRule = fwruledao.findOne(id);
				if(cloudFwRule==null){
					throw new AppException("BaseCloudFwRule is null");
				}
				cloudFwRule.setFwrId(firewallRule.getId());
				cloudFwRule.setFwrName(firewallRule.getName());
				cloudFwRule.setCreateName(EcmcSessionUtil.getUser() != null ? EcmcSessionUtil.getUser().getAccount() : null);
				cloudFwRule.setPrjId(projectId);
				cloudFwRule.setDcId(datacenterId);
				if(firewallRule.getProtocol()!=null){
					cloudFwRule.setProtocol(firewallRule.getProtocol());
				}else{
					cloudFwRule.setProtocol("any");
				}
				cloudFwRule.setSourcePort(firewallRule.getSource_port());
				cloudFwRule.setSourceIpaddress(firewallRule.getSource_ip_address());
				cloudFwRule.setDestinationPort(firewallRule.getDestination_port());
				cloudFwRule.setDestinationIpaddress(firewallRule.getDestination_ip_address());
				cloudFwRule.setIpVersion(firewallRule.getIp_version());
				cloudFwRule.setFwrAction(firewallRule.getAction());
				cloudFwRule.setFwrEnabled(enabled);//1是，0否
				fwruledao.saveOrUpdate(cloudFwRule);
				
			}
			return firewallRule;
		}catch (AppException e) {
			throw e;
		}catch (Exception e) {
		    log.error(e.toString(),e);
			return null;
		}
	}
}
