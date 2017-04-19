package com.eayun.virtualization.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.FirewallPolicy;
import com.eayun.eayunstack.service.OpenstackFirewallPolicyService;
import com.eayun.virtualization.dao.CloudFwPolicyDao;
import com.eayun.virtualization.dao.CloudFwRuleDao;
import com.eayun.virtualization.model.BaseCloudFwPolicy;
import com.eayun.virtualization.model.BaseCloudFwRule;
import com.eayun.virtualization.model.CloudFwPolicy;
import com.eayun.virtualization.service.FwPolicyService;
@Service
@Transactional
public class FwPolicyServiceImpl implements FwPolicyService {
	@Autowired
	private OpenstackFirewallPolicyService fwpService;
	@Autowired
	private CloudFwPolicyDao fwpDao;
	@Autowired
	private CloudFwRuleDao fwrDao;
	
	
	@Override
	public List<CloudFwPolicy> getFwpListByPrjId(String dcId, String prjId)
			throws AppException {
		List<CloudFwPolicy> listFw=new ArrayList<CloudFwPolicy>();
		int index=0;
		Object [] args=new Object[2];
		StringBuffer sql=new StringBuffer();
		sql.append("select fwp.fwp_id as fwpId,fwp.fwp_name as fwpName,fwp.audited as audited,fwp.prj_id as prjId,fwp.dc_id as dcId,fw.fw_id as fwId");
		sql.append(" from cloud_fwpolicy fwp");
		sql.append(" left outer join cloud_firewall fw on fwp.fwp_id=fw.fwp_id");
		sql.append(" where 1=1");
		if (!"null".equals(dcId)&&null!=dcId&&!"".equals(dcId)&&!"undefined".equals(dcId)) {
			sql.append(" and fwp.dc_id = ?");
			args[index]=dcId;
			index++;
		}
	 	if (!"null".equals(prjId)&&null!=prjId&&!"".equals(prjId)&&!"undefined".equals(prjId)) {
	 		sql.append(" and fwp.prj_id = ?");
	 		args[index]=prjId;
			index++;
		}
	    Object[] params = new Object[index];  
        System.arraycopy(args, 0, params, 0, index);
		Query query=fwpDao.createSQLNativeQuery(sql.toString(),params);
	        for(int i=0;i<query.getResultList().size();i++){
	        	Object[] objs = (Object[])query.getResultList().get(i);
	        	CloudFwPolicy fwp=new CloudFwPolicy();
	        	fwp.setFwpId(String.valueOf(objs[0]));
	        	fwp.setFwpName(String.valueOf(objs[1]));
	        	fwp.setAudited(String.valueOf(objs[2]));
	        	fwp.setPrjId(String.valueOf(objs[3]));
	        	fwp.setDcId(String.valueOf(objs[4]));
	        	fwp.setFwId(String.valueOf(objs[5]));
	        	listFw.add(fwp);
	        };
		return listFw;
		/*List<BaseCloudFwPolicy> list=fwpDao.getFwpListByPrjId(dcId,prjId);
		List<CloudFwPolicy> result = new ArrayList<CloudFwPolicy>();
		for (BaseCloudFwPolicy baseCloudFwPolicy : list) {
			CloudFwPolicy fwp = new CloudFwPolicy();
			BeanUtils.copyPropertiesByModel(fwp, baseCloudFwPolicy);
			result.add(fwp);
		}
		return result;*/
	}
	
	
	@Override
	public Page getFwpList(Page page, String prjId, String dcId,
			String fwpName, QueryMap queryMap) throws AppException {
		try {
			fwpName=fwpName.replaceAll("\\_", "\\\\_");
			int index=0;
			Object [] args=new Object[3];
			StringBuffer sql=new StringBuffer();
			sql.append("select fwp.fwp_id as fwpId,fwp.fwp_name as fwpName,fwp.audited as audited,cp.prj_name as prjName,cp.prj_id as prjId,fwp.rules as rules,dc.id as dcId,dc.dc_name as dcName,fw.fw_id as fwId");
			sql.append(" from cloud_fwpolicy fwp");
			sql.append(" left outer join cloud_project cp on fwp.prj_id=cp.prj_id");
			sql.append(" left outer join dc_datacenter dc on fwp.dc_id=dc.id");
			sql.append(" left outer join cloud_fwrule fwr on fwr.fwp_id=fwp.fwp_id");
			sql.append(" left outer join cloud_firewall fw on fwp.fwp_id=fw.fwp_id");
			sql.append(" where 1=1");
		 	if (!"null".equals(prjId)&&null!=prjId&&!"".equals(prjId)&&!"undefined".equals(prjId)) {
		 		sql.append(" and fwp.prj_id = ?");
		 		args[index]=prjId;
				index++;
			}
		    if (!"null".equals(dcId)&&null!=dcId&&!"".equals(dcId)&&!"undefined".equals(dcId)) {
				sql.append(" and fwp.dc_id = ?");
				args[index]=dcId;
				index++;
			}
		    if(null!=fwpName&&!"".equals(fwpName)){
		    	sql.append(" and binary fwp.fwp_name like ?");
		    	args[index]="%"+fwpName+"%";
				index++;
		    }
		    sql.append(" group by fwp.fwp_id order by fwp.create_time desc ");
		    
		    Object[] params = new Object[index];  
	        System.arraycopy(args, 0, params, 0, index);
			page= fwpDao.pagedNativeQuery(sql.toString(),queryMap,params);
			 List newList = (List)page.getResult();
		        for(int i=0;i<newList.size();i++){
		        	Object[] objs = (Object[])newList.get(i);
		        	CloudFwPolicy fwp=new CloudFwPolicy();
		        	
		        	fwp.setFwpId(String.valueOf(objs[0]));
		        	fwp.setFwpName(String.valueOf(objs[1]));
		        	fwp.setAudited(String.valueOf(objs[2]));
		        	fwp.setPrjName(String.valueOf(objs[3]));
		        	fwp.setPrjId(String.valueOf(objs[4]));
		        	fwp.setRules(String.valueOf(objs[5]));
		        	fwp.setDcId(String.valueOf(objs[6]));
		        	fwp.setDcName(String.valueOf(objs[7]));
		        	fwp.setFwId(String.valueOf(objs[8]));
		        	List<BaseCloudFwRule> rules=fwrDao.getFwRulesByfwpId(dcId, prjId, fwp.getFwpId());
		        	StringBuilder  rulesShow = new StringBuilder(); 
		    		for (int j=0;j<rules.size();j++) {
		    			rulesShow.append(rules.get(j).getFwrName());
		    			if(j<rules.size()-1){
		    				rulesShow.append(",");
						}
		    		}
		        	fwp.setRules(rulesShow.toString());
		        	newList.set(i, fwp);
		        };
			return page;
		}catch (AppException e) {
			throw e;
		}
	}
	
	
	
	@Override
	public boolean deleteFwp(CloudFwPolicy fwp) throws AppException {
		boolean isTrue=false;
		try{
			isTrue=fwpService.delete(fwp.getDcId(), fwp.getPrjId(), fwp.getFwpId());
			if(isTrue){
				fwpDao.delete(fwp.getFwpId());
				//把相对应的规则释放掉
				StringBuilder hql=new StringBuilder();
				hql.append("update BaseCloudFwRule t set fwpId =null where  fwpId=?");
				List<String> values = new ArrayList<String>();
				values.add(fwp.getFwpId());
				fwrDao.executeUpdate(hql.toString(), values.toArray());
			}
			
		}catch(AppException e){
			throw e;
		}
		return isTrue;
	}
	
	
	
	@Override
	public BaseCloudFwPolicy addFwPolicy(String dcId, String prjId,
			String createName, String fwpName)
			throws AppException {
		BaseCloudFwPolicy baseCloudFwPolicy=null;
		try{
			//防火墙策略数据
			JSONObject fwp = new JSONObject();			
			fwp.put("name", fwpName);			
			fwp.put("audited", "1");
			//用于提交的完整数据
			JSONObject resultData = new JSONObject();
			resultData.put("firewall_policy", fwp);
			FirewallPolicy fireWallPolicy=fwpService.create(dcId, prjId, resultData);
			
			if(null!=fireWallPolicy){
				baseCloudFwPolicy=new BaseCloudFwPolicy();
				baseCloudFwPolicy.setFwpId(fireWallPolicy.getId());
				baseCloudFwPolicy.setFwpName(fireWallPolicy.getName());
				baseCloudFwPolicy.setAudited("1");
				baseCloudFwPolicy.setDcId(dcId);
				baseCloudFwPolicy.setPrjId(prjId);
				baseCloudFwPolicy.setCreateTime(new Date());
				baseCloudFwPolicy.setCreateName(createName);
				fwpDao.save(baseCloudFwPolicy);
			}
		}catch(AppException e){
			throw e;
		}
		return baseCloudFwPolicy;
	}
	
	@Override
	public List<BaseCloudFwRule> getRuleByFwpId(String fwpId) throws AppException {
		return fwpDao.getByFwrId(fwpId);
	}
	
	@Override
	public boolean updateFwPolicy(CloudFwPolicy fwp) throws AppException {
		try{
			//防火墙策略数据
			JSONObject data = new JSONObject();			
			data.put("name", fwp.getFwpName());
			data.put("audited", "1");
			//用于提交的完整数据
			JSONObject resultData = new JSONObject();
			resultData.put("firewall_policy", data);
			FirewallPolicy fwPolicy=fwpService.update(fwp.getDcId(), fwp.getPrjId(), resultData, fwp.getFwpId());
			
			if(null!=fwPolicy){
				BaseCloudFwPolicy baseCloudFwPolicy=fwpDao.findOne(fwPolicy.getId());
				baseCloudFwPolicy.setFwpName(fwPolicy.getName());
				fwpDao.saveOrUpdate(baseCloudFwPolicy);
				return true;
			}
		}catch(AppException e){
			throw e;
		}
		return false;
	}
	
	
	
	@Override
	public boolean toDoFwRule(CloudFwPolicy fwp) throws AppException {
		try{
			if(null==fwp.getFirewallRules()){
				return true;
			}
			int ArrLength=fwp.getFirewallRules().size();
			String [] rulesFormodify = new String[ArrLength];
			for(int i=0;i<fwp.getFirewallRules().size();i++){
				rulesFormodify[i]=fwp.getFirewallRules().get(i).getFwrId();
			}
			//防火墙策略数据
			JSONObject data = new JSONObject();
			data.put("firewall_rules", rulesFormodify);
			data.put("audited", "1");
			//用于提交的完整数据
			JSONObject resultData = new JSONObject();
			resultData.put("firewall_policy", data);
			FirewallPolicy fwPolicy=fwpService.update(fwp.getDcId(), fwp.getPrjId(), resultData, fwp.getFwpId());
			
			if(null!=fwPolicy){
				StringBuilder hql=new StringBuilder();
				hql.append("update BaseCloudFwRule t set fwpId =null where  fwpId=?");
				List<String> values = new ArrayList<String>();
				values.add(fwp.getFwpId());
				fwrDao.executeUpdate(hql.toString(), values.toArray());
				
				StringBuilder hq=new StringBuilder();
				hq.append("update BaseCloudFwRule t set fwpId =? , priority = ? where  fwrId=?");
				List<String> params=null;
				for(int j=0;j<rulesFormodify.length;j++){
					params = new ArrayList<String>();
					params.add(fwp.getFwpId());
					params.add((j+1)+"");
					params.add(rulesFormodify[j]);
					fwrDao.executeUpdate(hq.toString(), params.toArray());
					params=null;
				}
				
				BaseCloudFwPolicy baseCloudFwPolicy=fwpDao.findOne(fwp.getFwpId());
				fwpDao.saveOrUpdate(baseCloudFwPolicy);
				
				return true;
			}
			
		}catch(AppException e){
			throw e;
		}
		return false;
	}
	
	
	
	@Override
	public boolean getFwpByName(Map map) throws AppException {
		boolean isExist = false;
		try{
			String dcId=null;
			String fwpName=null;
			String fwpId=null;
			String prjId=null;
			if(null==map.get("project")){
				dcId=map.get("dcId").toString();
				prjId=map.get("prjId").toString();
				fwpName=map.get("fwpName").toString();
				fwpId=map.get("fwpId")!=null?map.get("fwpId").toString():null;
			}else{
				Map project = (Map)map.get("project");
	    		dcId=project.get("dcId").toString();
	    		prjId=project.get("projectId").toString();
	    		fwpName=map.get("name").toString();
			}
			
			StringBuffer sql = new StringBuffer();
			int index=0;
			Object [] args=new Object[4];
			sql.append("select fwp.fwp_id,fwp.fwp_name from cloud_fwpolicy fwp where 1=1 ");
			//数据中心
			if (!"".equals(dcId)&&dcId!=null&&!"undefined".equals(dcId)&&!"null".equals(dcId)) {
				sql.append("and fwp.dc_id = ? ");
				args[index]=dcId;
				index++;
			}
			//项目
			if (!"".equals(prjId)&&prjId!=null&&!"undefined".equals(prjId)&&!"null".equals(prjId)) {
				sql.append("and fwp.prj_id = ? ");
				args[index]=prjId;
				index++;
			}
			//防火墙策略名称
			if (!"".equals(fwpName)&&fwpName!=null) {
				sql.append("and binary fwp.fwp_name = ? ");
				args[index]=fwpName.trim();
				index++;
			}
			
			//防火墙策略ID
			if (!"".equals(fwpId)&&fwpId!=null&&!"undefined".equals(fwpId)&&!"null".equals(fwpId)) {
				sql.append("and fwp.fwp_id <> ? ");
				args[index]=fwpId.trim();
				index++;
			}
			
			Object[] params = new Object[index];  
	        System.arraycopy(args, 0, params, 0, index);
	        javax.persistence.Query query = fwpDao.createSQLNativeQuery(sql.toString(), params);
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
	public BaseCloudFwPolicy addFwPolicyRule(String dcId, String prjId,
			String createName, String fwpName,String rule)
			throws AppException {
		BaseCloudFwPolicy baseCloudFwPolicy=null;
		try{
			Map<String, String> map = new HashMap<>();
			map.put("dcId", dcId);
			map.put("prjId", prjId);
			map.put("fwpName", fwpName);
			if(getFwpByName(map)){throw new AppException("该项目下防火墙已存在，不可重复创建");}
			String [] rules = new String[]{};
			if(rule!=null && !"".equals(rule)){
				rules= rule.split(",");	
			}
			//防火墙策略数据
			JSONObject fwp = new JSONObject();			
			fwp.put("name", fwpName);
			fwp.put("firewall_rules", rules);
			fwp.put("audited", "1");
			//用于提交的完整数据
			JSONObject resultData = new JSONObject();
			resultData.put("firewall_policy", fwp);
			FirewallPolicy fireWallPolicy=fwpService.create(dcId, prjId, resultData);
			
			if(null!=fireWallPolicy){
				baseCloudFwPolicy=new BaseCloudFwPolicy();
				baseCloudFwPolicy.setFwpId(fireWallPolicy.getId());
				baseCloudFwPolicy.setFwpName(fireWallPolicy.getName());
				baseCloudFwPolicy.setAudited("1");
				baseCloudFwPolicy.setDcId(dcId);
				baseCloudFwPolicy.setPrjId(prjId);
				baseCloudFwPolicy.setCreateTime(new Date());
				baseCloudFwPolicy.setCreateName(createName);
				fwpDao.save(baseCloudFwPolicy);
				for(int i=0;i<rules.length;i++){
					BaseCloudFwRule cloudFwRule = fwrDao.findOne(rules[i]);
					cloudFwRule.setFwpId(baseCloudFwPolicy.getFwpId());
					cloudFwRule.setFwrId(rules[i]);
					cloudFwRule.setPriority((i+1)+"");
					fwrDao.saveOrUpdate(cloudFwRule);
				}
			}
		}catch(AppException e){
			throw e;
		}
		return baseCloudFwPolicy;
	}

	@Override
	public FirewallPolicy addFwPolicyRule(String dcId, String prjId, String fwpName,String rule) throws AppException {
		Map<String, String> map = new HashMap<>();
		map.put("dcId", dcId);
		map.put("prjId", prjId);
		map.put("fwpName", fwpName);
		if(getFwpByName(map)){throw new AppException("策略名称已存在");}
		String [] rules = new String[]{};
		if(rule!=null && !"".equals(rule)){
			rules= rule.split(",");	
		}
		//防火墙策略数据
		JSONObject fwp = new JSONObject();			
		fwp.put("name", fwpName);
		fwp.put("firewall_rules", rules);
		fwp.put("audited", "1");
		//用于提交的完整数据
		JSONObject resultData = new JSONObject();
		resultData.put("firewall_policy", fwp);
		FirewallPolicy fireWallPolicy=fwpService.create(dcId, prjId, resultData);
		return fireWallPolicy;
	}
	
	/**
	 * 调换数组指定字段的位置
	 * 
	 * @param rules  数组
	 * @param local  前置or后置
	 * @param target 目标对象
	 * @param reference 参照对象
	 * @return
	 */
	public String[] adjustPriority(List<String> rules,String local,String target,String reference){
		int ruleslength = rules.size();
		int targetnum = 0,referencenum=0;
		String[] rulesFormodify = new String[ruleslength];
		for(int i=0;i<ruleslength;i++){
			if(target.equals(rules.get(i))){
				targetnum=i;//得到目标物的位置
			}
		}
		rules.remove(targetnum);//先移除目标
		ruleslength = rules.size();
		for(int i=0;i<ruleslength;i++){
			if(reference.equals(rules.get(i))){
				referencenum=i;//得到参照物位置
			}
		}
		if("pre".equals(local)){//前置
			if(referencenum==0){
				rules.add(0, target);
			}else{
				rules.add(referencenum, target);
			}
		}else{//后置
			if(referencenum==ruleslength){
				rules.add(ruleslength, target);
			}else{
				rules.add(referencenum+1, target);
			}
		}
		for(int i=0;i<rules.size();i++){
			rulesFormodify[i] = rules.get(i);
		}
		return rulesFormodify;
	}
	
	@Override
	public BaseCloudFwPolicy getFwpById(String fwpId) throws AppException {
		return fwpDao.findOne(fwpId);
	}
	
	@Override
	public boolean updateRuleSequence(BaseCloudFwPolicy fwp, String local, String target, String reference)
			throws AppException {
		try{
			List<BaseCloudFwRule> rules = getRuleByFwpId(fwp.getFwpId());
			int ArrLength=rules.size();
			List<String> ruleslistid = new ArrayList<>();
			for(int i=0;i<ArrLength;i++){
				ruleslistid.add(rules.get(i).getFwrId());
			}
			String [] rulesFormodify = adjustPriority(ruleslistid,local,target,reference);
			//防火墙策略数据
			JSONObject data = new JSONObject();
			data.put("firewall_rules", rulesFormodify);
			data.put("audited", "1");
			//用于提交的完整数据
			JSONObject resultData = new JSONObject();
			resultData.put("firewall_policy", data);
			FirewallPolicy fwPolicy = fwpService.update(fwp.getDcId(), fwp.getPrjId(), resultData, fwp.getFwpId());
			
			if(null!=fwPolicy){
				StringBuilder hql=new StringBuilder();
				hql.append("update BaseCloudFwRule t set fwpId =null where  fwpId=?");
				List<String> values = new ArrayList<String>();
				values.add(fwp.getFwpId());
				fwrDao.executeUpdate(hql.toString(), values.toArray());
				
				StringBuilder hq=new StringBuilder();
				hq.append("update BaseCloudFwRule t set fwpId = ? , priority = ? where  fwrId=?");
				List<String> params=null;
				for(int j=0;j<rulesFormodify.length;j++){
					params = new ArrayList<String>();
					params.add(fwp.getFwpId());
					params.add((j+1)+"");
					params.add(rulesFormodify[j]);
					fwrDao.executeUpdate(hq.toString(), params.toArray());
					params=null;
				}
				
				BaseCloudFwPolicy baseCloudFwPolicy=fwpDao.findOne(fwp.getFwpId());
				fwpDao.saveOrUpdate(baseCloudFwPolicy);
				
				return true;
			}
			
		}catch(AppException e){
			throw e;
		}
		return false;
	}
	@Override
	public boolean releaseFwRule(CloudFwPolicy fwp) throws AppException {
		try{
			//防火墙策略数据
			JSONObject data = new JSONObject();
			data.put("firewall_rules", new String[0]);
			data.put("audited", "1");
			//用于提交的完整数据
			JSONObject resultData = new JSONObject();
			resultData.put("firewall_policy", data);
			FirewallPolicy fwPolicy = fwpService.update(fwp.getDcId(), fwp.getPrjId(), resultData, fwp.getFwpId());
			
			if(null!=fwPolicy){
				StringBuilder hql=new StringBuilder();
				hql.append("update BaseCloudFwRule t set fwpId =null where  fwpId=?");
				List<String> values = new ArrayList<String>();
				values.add(fwp.getFwpId());
				fwrDao.executeUpdate(hql.toString(), values.toArray());
				
				BaseCloudFwPolicy baseCloudFwPolicy=fwpDao.findOne(fwp.getFwpId());
				fwpDao.saveOrUpdate(baseCloudFwPolicy);
				
				return true;
			}
			
		}catch(AppException e){
			throw e;
		}
		return false;
	}
}
