package com.eayun.virtualization.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.util.BeanUtils;
import com.eayun.eayunstack.model.FirewallRule;
import com.eayun.eayunstack.service.OpenstackFirewallRuleService;
import com.eayun.eayunstack.service.OpenstackFirewallService;
import com.eayun.eayunstack.service.impl.OpenstackFirewallRuleServiceImpl;
import com.eayun.virtualization.dao.CloudFireWallDao;
import com.eayun.virtualization.dao.CloudFwRuleDao;
import com.eayun.virtualization.model.BaseCloudFwPolicy;
import com.eayun.virtualization.model.BaseCloudFwRule;
import com.eayun.virtualization.model.CloudFireWall;
import com.eayun.virtualization.model.CloudFwPolicy;
import com.eayun.virtualization.model.CloudFwRule;
import com.eayun.virtualization.service.FireWallRuleService;
import com.eayun.virtualization.service.FwPolicyService;

@Service
@Transactional
public class FireWallRuleServiceImpl implements FireWallRuleService {
	@Autowired
	private OpenstackFirewallRuleService fwrService;
	@Autowired
	private CloudFwRuleDao fwrDao;
	@Autowired
	private FwPolicyService fwpService;
	@Override
	public Page getFireWallRuleList(Page page, String prjId, String dcId,
			String fireRuleName, QueryMap queryMap,String fwpId) throws AppException {
		try {
			fireRuleName=fireRuleName.replaceAll("\\_", "\\\\_");
			int index=0;
			Object [] args=new Object[3];
			StringBuffer sql=new StringBuffer();
			sql.append("select fwr.fwr_id as fwrId,fwr.fwr_name as fwrName,fwr.protocol as protocol,fwr.source_port as sourcePort,");
			sql.append(" fwr.source_ipaddress as sourceIpaddress,fwr.destination_ipaddress as destinationIpaddress,fwr.destination_port as destinationPort,");
			sql.append(" fwr.ip_version as ipVersion,fwr.fwr_action as fwrAction,fwr.fwr_enabled as fwrEnabled,fwr.is_shared as isShared,");
			sql.append(" fwp.fwp_name as fwpName,fwr.prj_id as prjId,cp.prj_name as prjName,fwr.dc_id as dcId,dc.dc_name as dcName,fwr.fwp_id as fwpId,"
					+ "fwr.fwr_priority as priority,ct.cus_org as cusOrg,fwr.create_time as createTime ");
			sql.append(" from cloud_fwrule fwr");
			sql.append(" left outer join cloud_project cp on fwr.prj_id=cp.prj_id");
			sql.append(" left outer join sys_selfcustomer ct on ct.cus_id=cp.customer_id");
			sql.append(" left outer join cloud_fwpolicy fwp on fwr.fwp_id=fwp.fwp_id");
			sql.append(" left outer join dc_datacenter dc on fwr.dc_id=dc.id");
			sql.append(" where 1=1");
			 if (!"null".equals(dcId)&&null!=dcId&&!"".equals(dcId)&&!"undefined".equals(dcId)) {
					sql.append(" and fwr.dc_id = ?");
					args[index]=dcId;
					index++;
				}
		 	if (!"null".equals(prjId)&&null!=prjId&&!"".equals(prjId)&&!"undefined".equals(prjId)) {
		 		sql.append(" and fwr.prj_id = ?");
		 		args[index]=prjId;
				index++;
			}
		 	if (!"null".equals(fwpId)&&null!=fwpId&&!"".equals(fwpId)&&!"undefined".equals(fwpId)) {
		 		sql.append(" and fwr.fwp_id = ?");
		 		args[index]=fwpId;
				index++;
			}
		    if(null!=fireRuleName&&!"".equals(fireRuleName)){
		    	sql.append(" and binary fwr.fwr_name like ?");
		    	args[index]="%"+fireRuleName+"%";
				index++;
		    }
		    sql.append(" group by fwr.fwr_id order by fwr.fwr_priority ");
		    
		    Object[] params = new Object[index];  
	        System.arraycopy(args, 0, params, 0, index);
	        page = fwrDao.pagedNativeQuery(sql.toString(),queryMap,params);
			 List newList = (List)page.getResult();
		        for(int i=0;i<newList.size();i++){
		        	Object[] objs = (Object[])newList.get(i);
		        	CloudFwRule fwRule=new CloudFwRule();
		        	fwRule.setFwrId(String.valueOf(objs[0]));
		        	fwRule.setFwrName(String.valueOf(objs[1]));
		        	fwRule.setProtocol(String.valueOf(objs[2]));
		        	fwRule.setSourcePort(String.valueOf(objs[3]));
		        	fwRule.setSourceIpaddress(String.valueOf(objs[4]));
		        	fwRule.setDestinationIpaddress(String.valueOf(objs[5]));
		        	fwRule.setDestinationPort(String.valueOf(objs[6]));
		        	fwRule.setIpVersion(String.valueOf(objs[7]));
		        	fwRule.setFwrAction(String.valueOf(objs[8]));
		        	fwRule.setFwrEnabled(String.valueOf(objs[9]));
		        	fwRule.setIsShared(String.valueOf(objs[10]));
		        	fwRule.setFwpName(String.valueOf(objs[11]));
		        	fwRule.setPrjId(String.valueOf(objs[12]));
		        	fwRule.setPrjName(String.valueOf(objs[13]));
		        	fwRule.setDcId(String.valueOf(objs[14]));
		        	fwRule.setDcName(String.valueOf(objs[15]));
		        	fwRule.setFwpId(String.valueOf(objs[16]));
		        	fwRule.setPriority(String.valueOf(objs[17]));
		        	fwRule.setCusOrg(String.valueOf(objs[18]));
		        	fwRule.setCreateTime((Date)objs[19]);
		        	newList.set(i, fwRule);
		        }
		       
		}catch (AppException e) {
			throw e;
		}
		return page;
	}

	
	
	@Override
	public List<CloudFwRule> getFwRulesByPrjId(String dcId, String prjId)
			throws AppException {
		List<BaseCloudFwRule> list=fwrDao.getFwRulesByPrjId(dcId,prjId);
		List<CloudFwRule> result = new ArrayList<CloudFwRule>();
		for (BaseCloudFwRule baseCloudFwRule : list) {
			CloudFwRule fwr = new CloudFwRule();
			BeanUtils.copyPropertiesByModel(fwr, baseCloudFwRule);
			result.add(fwr);
		}
		return result;
	}



	@Override
	public List<CloudFwRule> getFwRulesByfwpId(CloudFwPolicy fwp)
			throws AppException {
		List<BaseCloudFwRule> list=fwrDao.getFwRulesByfwpId(fwp.getDcId(),fwp.getPrjId(),fwp.getFwpId());
		List<CloudFwRule> result = new ArrayList<CloudFwRule>();
		for (BaseCloudFwRule baseCloudFwRule : list) {
			CloudFwRule fwr = new CloudFwRule();
			BeanUtils.copyPropertiesByModel(fwr, baseCloudFwRule);
			result.add(fwr);
		}
		return result;
	}



	@Override
	public boolean deleteFwRule(CloudFwRule fwr) throws AppException {
		boolean isTrue=false;
		try{
			isTrue=fwrService.delete(fwr.getDcId(), fwr.getPrjId(), fwr.getFwrId());
			if(isTrue){
				fwrDao.delete(fwr.getFwrId());
			}
		}catch(AppException e){
			throw e;
		}
		return isTrue;
	}

	@Override
	public boolean deleteFwRuletoPolicy(CloudFwRule fwr) throws AppException {
		List<BaseCloudFwRule> listRule = fwpService.getRuleByFwpId(fwr.getFwpId());
		List<CloudFwRule> rules = new ArrayList<CloudFwRule>();
		CloudFwRule cfwr = null; 
		for(int i=0;i<listRule.size();i++){
			if(!fwr.getFwrId().equals(listRule.get(i).getFwrId())){//移除当前规则
				cfwr = new CloudFwRule(); 
				cfwr.setFwrId(listRule.get(i).getFwrId());
				rules.add(cfwr);
			}
		}
		CloudFwPolicy policy = new CloudFwPolicy();
		policy.setFwpId(fwr.getFwpId());
		policy.setDcId(fwr.getDcId());
		policy.setPrjId(fwr.getPrjId());
		policy.setFirewallRules(rules);
		if(fwpService.toDoFwRule(policy)){//底层解绑策略（这里不是全部解绑，所以是修改策略关联的规则）
			return deleteFwRule(fwr);
		}
		return false;
	}

	
	@Override
	public BaseCloudFwRule addFwRule(String createName, Map map)
			throws AppException {
		try{
			  Map project = (Map)map.get("project");
	          String dcId=project.get("dcId").toString();
	          String prjId=project.get("projectId").toString();
	          String name = map.get("name").toString();
		  	  String protocol =map.get("protocol").toString();
		  	  String sourcePort =map.get("source_port")!=null?map.get("source_port").toString():null;
		  	  String sourceIpaddress = map.get("source_ip_address")!=null?map.get("source_ip_address").toString():null;
		  	  String destinationPort = map.get("destination_port")!=null? map.get("destination_port").toString():null;
		  	  String destinationIpaddress = map.get("destination_ip_address")!=null?map.get("destination_ip_address").toString():null;
		  	  String fwrAction = map.get("action").toString();
		  	 
		  	  	//防火墙规则数据
			    JSONObject fwrule = new JSONObject();			
				fwrule.put("name", name);
				if("any".equals(protocol)||null==protocol||"".equals(protocol)){
				    protocol="any";
				}else{
				 fwrule.put("protocol", protocol);
				}
				 
			  	if("".equals(sourcePort)||null==sourcePort){
			  		fwrule.put("source_port", null);
			  	}else{
			  		fwrule.put("source_port", sourcePort);
			  	}
			  	
			  	if("".equals(sourceIpaddress)||null==sourceIpaddress){
			  		fwrule.put("source_ip_address", null);
			  	}else{
			  		fwrule.put("source_ip_address", sourceIpaddress);
			  	}
				
			  	if("".equals(destinationPort)||null==destinationPort){
			  		fwrule.put("destination_port", null);
			  	}else{
			  		fwrule.put("destination_port", destinationPort);
			  	}
			  	
			  	if("".equals(destinationIpaddress)||null==destinationIpaddress){
			  		fwrule.put("destination_ip_address", null);
			  	}else{
			  		fwrule.put("destination_ip_address", destinationIpaddress);
			  	}
			
				fwrule.put("ip_version", 4);
				fwrule.put("action", fwrAction);
				fwrule.put("enabled", 1);
				fwrule.put("shared",1);
				//用于提交的完整数据
				JSONObject resultData = new JSONObject();
				resultData.put("firewall_rule", fwrule);
				FirewallRule firewallRule =fwrService.create(dcId, prjId, resultData);
				if(firewallRule!=null){
					String fwpId = map.get("fwpId") != null ?  map.get("fwpId").toString() : null;
					BaseCloudFwRule cloudFwRule=new BaseCloudFwRule();
					cloudFwRule.setFwrId(firewallRule.getId());
					cloudFwRule.setFwrName(firewallRule.getName());
					cloudFwRule.setCreateName(createName);
					cloudFwRule.setCreateTime(new Date());		
					cloudFwRule.setPrjId(prjId);
					cloudFwRule.setDcId(dcId);
					cloudFwRule.setProtocol(protocol);
					cloudFwRule.setSourcePort(firewallRule.getSource_port());
					cloudFwRule.setSourceIpaddress(firewallRule.getSource_ip_address());
					cloudFwRule.setDestinationPort(firewallRule.getDestination_port());
					cloudFwRule.setDestinationIpaddress(firewallRule.getDestination_ip_address());
					cloudFwRule.setIpVersion(firewallRule.getIp_version());
					cloudFwRule.setFwrAction(firewallRule.getAction());
					cloudFwRule.setFwrEnabled("1");
					cloudFwRule.setIsShared("1");
					cloudFwRule.setFwpId(fwpId);
					fwrDao.save(cloudFwRule);
					if(fwpId!=null){
						//查询出原策略下的所有规则
						List<BaseCloudFwRule> listcr = fwpService.getRuleByFwpId(fwpId);
						//绑定规则到策略
						List<CloudFwRule> firewallRules = new ArrayList<CloudFwRule>();
						CloudFwRule rule = new CloudFwRule();
						for(BaseCloudFwRule cr : listcr){
							rule = new CloudFwRule();
							rule.setFwrId(cr.getFwrId());
							firewallRules.add(rule);
						}
						CloudFwPolicy fwp = new CloudFwPolicy();
						fwp.setFwpId(fwpId);
						fwp.setDcId(dcId);
						fwp.setPrjId(prjId);
						fwp.setFirewallRules(firewallRules);
						fwpService.toDoFwRule(fwp);//这个方法是修改策略关联规则，不是添加规则
					}
					return cloudFwRule;
				}
		}catch(AppException e){
			throw e;
		}
		return null;
	}

	@Override
	public FirewallRule addFwRule(Map map) throws AppException {
		Map project = (Map)map.get("project");
        String dcId=project.get("dcId").toString();
        String prjId=project.get("projectId").toString();
        String name = map.get("name").toString();
	  	String protocol =map.get("protocol").toString();
	    String sourcePort =map.get("source_port")!=null?map.get("source_port").toString():null;
	  	String sourceIpaddress = map.get("source_ip_address")!=null?map.get("source_ip_address").toString():null;
	  	String destinationPort = map.get("destination_port")!=null? map.get("destination_port").toString():null;
	  	String destinationIpaddress = map.get("destination_ip_address")!=null?map.get("destination_ip_address").toString():null;
	  	String fwrAction = map.get("action").toString();
	  	 
	  	  	//防火墙规则数据
		    JSONObject fwrule = new JSONObject();			
			fwrule.put("name", name);
			if("any".equals(protocol)||null==protocol||"".equals(protocol)){
			    protocol="any";
			}else{
			 fwrule.put("protocol", protocol);
			}
			 
		  	if("".equals(sourcePort)||null==sourcePort){
		  		fwrule.put("source_port", null);
		  	}else{
		  		fwrule.put("source_port", sourcePort);
		  	}
		  	
		  	if("".equals(sourceIpaddress)||null==sourceIpaddress){
		  		fwrule.put("source_ip_address", null);
		  	}else{
		  		fwrule.put("source_ip_address", sourceIpaddress);
		  	}
			
		  	if("".equals(destinationPort)||null==destinationPort){
		  		fwrule.put("destination_port", null);
		  	}else{
		  		fwrule.put("destination_port", destinationPort);
		  	}
		  	
		  	if("".equals(destinationIpaddress)||null==destinationIpaddress){
		  		fwrule.put("destination_ip_address", null);
		  	}else{
		  		fwrule.put("destination_ip_address", destinationIpaddress);
		  	}
		
			fwrule.put("ip_version", 4);
			fwrule.put("action", fwrAction);
			fwrule.put("enabled", 1);
			fwrule.put("shared",1);
			//用于提交的完整数据
			JSONObject resultData = new JSONObject();
			resultData.put("firewall_rule", fwrule);
			FirewallRule firewallRule =fwrService.create(dcId, prjId, resultData);
		return firewallRule;
	}

	@Override
	public boolean getFwRuleByName(Map map) throws AppException{
		boolean isExist = false;
		try{
			String dcId=null;
			String fwrName=null;
			String fwrId=null;
			String prjId=null;
			if(null==map.get("project")){
				dcId=map.get("dcId").toString();
				prjId=map.get("prjId").toString();
				fwrName=map.get("fwrName").toString();
				fwrId=map.get("fwrId")!=null?map.get("fwrId").toString():null;
			}else{
				Map project = (Map)map.get("project");
	    		dcId=project.get("dcId").toString();
	    		prjId=project.get("projectId").toString();
	    		fwrName=map.get("name").toString();
			}
			
			StringBuffer sql = new StringBuffer();
			int index=0;
			Object [] args=new Object[4];
			sql.append("select fwr.fwr_id,fwr.fwr_name from cloud_fwrule fwr where 1=1 ");
			//数据中心
			if (!"".equals(dcId)&&dcId!=null&&!"undefined".equals(dcId)&&!"null".equals(dcId)) {
				sql.append("and fwr.dc_id = ? ");
				args[index]=dcId;
				index++;
			}
			//项目
			if (!"".equals(prjId)&&prjId!=null&&!"undefined".equals(prjId)&&!"null".equals(prjId)) {
				sql.append("and fwr.prj_id = ? ");
				args[index]=prjId;
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
	        javax.persistence.Query query = fwrDao.createSQLNativeQuery(sql.toString(), params);
	        List listResult = query.getResultList();
	      
			if(listResult.size()>0){
				isExist = true;//返回true 代表存在此名称
			}
		}catch(AppException e){
			throw e;
		}  
		return isExist;
	}


	

	@Override
	public boolean updateFwRule(CloudFwRule fwr) throws AppException {
		try{
			
			//防火墙规则数据
			net.sf.json.JSONObject  fwrule=new net.sf.json.JSONObject();
			//JSONObject fwrule = new JSONObject();			
			fwrule.put("name", fwr.getFwrName());
			if ("any".equals(fwr.getProtocol())||null==fwr.getProtocol()) {
					fwrule.put("protocol","null");
			}else{
				fwrule.put("protocol", fwr.getProtocol());
			}
			
			if(null==fwr.getSourceIpaddress()||"".equals(fwr.getSourceIpaddress())){
				fwrule.put("source_ip_address", "null");
				fwr.setSourceIpaddress(null);
			}else{
				fwrule.put("source_ip_address", fwr.getSourceIpaddress());
			}
			
			if(null==fwr.getSourcePort()||"".equals(fwr.getSourcePort())){
				fwrule.put("source_port", "null");
				fwr.setSourcePort(null);
			}else{
				fwrule.put("source_port", fwr.getSourcePort());
			}
			
			if(null==fwr.getDestinationIpaddress()||"".equals(fwr.getDestinationIpaddress())){
				fwrule.put("destination_ip_address", "null");
				fwr.setDestinationIpaddress(null);
			}else{
				fwrule.put("destination_ip_address", fwr.getDestinationIpaddress());
			}
			
			if(null==fwr.getDestinationPort()||"".equals(fwr.getDestinationPort())){
				fwrule.put("destination_port", "null");
				fwr.setDestinationPort(null);
			}else{
				fwrule.put("destination_port", fwr.getDestinationPort());
			}

			fwrule.put("ip_version",4);
			fwrule.put("action", fwr.getFwrAction());
			fwrule.put("enabled",1);
			fwrule.put("shared",1);
			//用于提交的完整数据
			//JSONObject resultData = new JSONObject();
			net.sf.json.JSONObject  resultData=new net.sf.json.JSONObject();
			resultData.put("firewall_rule", fwrule);
			FirewallRule fireWallRule=fwrService.updateByNetJson(fwr.getDcId(), fwr.getPrjId(), resultData, fwr.getFwrId());
			if(null!=fireWallRule){
				BaseCloudFwRule cloudFwr=fwrDao.findOne(fwr.getFwrId());
				cloudFwr.setDestinationIpaddress(fwr.getDestinationIpaddress());
				cloudFwr.setDestinationPort(fwr.getDestinationPort());
				cloudFwr.setSourceIpaddress(fwr.getSourceIpaddress());
				cloudFwr.setSourcePort(fwr.getSourcePort());
				cloudFwr.setFwrAction(fwr.getFwrAction());
				cloudFwr.setProtocol(fwr.getProtocol());
				cloudFwr.setFwrName(fwr.getFwrName());
				fwrDao.saveOrUpdate(cloudFwr);
				return true;
			}
		}catch(AppException e){
			throw e;
		}
		return false;	
	}

	
	@SuppressWarnings("unchecked")
	public List<BaseCloudFwRule> getFwRulesByDcId(String dcId){
		StringBuffer hql = new StringBuffer();
		hql.append(" from  BaseCloudFwRule where dcId = ?");
		return fwrDao.find(hql.toString(), new Object []{dcId});
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
			
			fwrDao.executeUpdate(sql.toString(), new Object []{
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
			flag = false;
		}
		return flag;
	}
	
	@Override
	public boolean updateIsEnabled(BaseCloudFwRule fwr) throws AppException {
		try{
			//防火墙规则数据
			net.sf.json.JSONObject  fwrule=new net.sf.json.JSONObject();
			fwrule.put("enabled",fwr.getFwrEnabled());
			//用于提交的完整数据
			net.sf.json.JSONObject  resultData=new net.sf.json.JSONObject();
			resultData.put("firewall_rule", fwrule);
			FirewallRule fireWallRule = fwrService.updateByNetJson(fwr.getDcId(), fwr.getPrjId(), resultData, fwr.getFwrId());
			if(null!=fireWallRule){
				BaseCloudFwRule cloudFwr=fwrDao.findOne(fwr.getFwrId());
				cloudFwr.setFwrEnabled(fwr.getFwrEnabled());
				fwrDao.saveOrUpdate(cloudFwr);
				return true;
			}
		}catch(AppException e){
			throw e;
		}
		return false;	
	}
	
}
