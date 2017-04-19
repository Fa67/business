package com.eayun.virtualization.ecmcservice.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

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
import com.eayun.eayunstack.model.FirewallPolicy;
import com.eayun.eayunstack.service.OpenstackFirewallPolicyService;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.virtualization.dao.CloudFwPolicyDao;
import com.eayun.virtualization.dao.CloudFwRuleDao;
import com.eayun.virtualization.ecmcservice.EcmcCloudFireWallPoliyService;
import com.eayun.virtualization.model.BaseCloudFwPolicy;
import com.eayun.virtualization.model.BaseCloudFwRule;
import com.eayun.virtualization.model.CloudFwPolicy;

/**
 * @author jingang.liu@eayun.com to beijing
 * @date 2016年4月12日
 */
@Service
@Transactional
public class EcmcCloudFireWallPoliyServiceImpl implements EcmcCloudFireWallPoliyService {
    private static final Logger log = LoggerFactory.getLogger(EcmcCloudFireWallPoliyServiceImpl.class);
	@Autowired
	private CloudFwPolicyDao cfpdao;
	@Autowired
	private CloudFwRuleDao cfrdao;
	@Autowired
	private OpenstackFirewallPolicyService service;
	
	@Override
	public boolean checkFwPolicyName(String fwpName,String projectId,String datacenterId,String fwpId) throws AppException {
		boolean isExist = false;
		StringBuffer sql = new StringBuffer();
		int index=0;
		Object [] args=new Object[4];
		sql.append("select count(*) from cloud_fwpolicy  as fwp  where 1=1 ");
		//项目
		if (!"null".equals(projectId)&&null!=projectId&&!"".equals(projectId)&&!"undefined".equals(projectId)) {
			sql.append(" and fwp.prj_id= ? ");
			args[index]=projectId;
			index++;
		}
		//数据中心
		if (!"".equals(datacenterId)&&datacenterId!=null&&!"undefined".equals(datacenterId)&&!"null".equals(datacenterId)) {
			sql.append("and fwp.dc_id = ? ");
			args[index]=datacenterId;
			index++;
		}
		//策略名称
		if (!"".equals(fwpName)&&fwpName!=null&&!"undefined".equals(fwpName)&&!"null".equals(fwpName)) {
			sql.append("and binary fwp.fwp_name = ? ");
			args[index]=fwpName.trim();
			index++;
		}
		//策略ID
		if (!"".equals(fwpId)&&fwpId!=null&&!"undefined".equals(fwpId)&&!"null".equals(fwpId)) {
			sql.append("and fwp.fwp_id <> ? ");
			args[index]=fwpId.trim();
			index++;
			}
						
		Object[] params = new Object[index];  
        System.arraycopy(args, 0, params, 0, index);
		int ret = ((Number)cfpdao.createSQLNativeQuery(sql.toString(), params).getSingleResult()).intValue();
		if(ret>0){
			isExist = true;//返回true 代表存在此名称
		}
		return isExist;
	}
	
	@Override
	public Page list(Page page, String prjName, String name,String cusOrg,String datacenterId, QueryMap querymap) throws AppException {
		int index = 0;
		Object[] args = new Object[4];
		
		StringBuffer sql=new StringBuffer();
		sql.append("select fwp.fwp_id as fwpId,fwp.fwp_name as fwpName,fwp.audited as audited,fwp.description as description,cp.prj_name as prjName,"
				+ "cp.prj_id as prjId,count(fwr.fwr_id) as rulenum,fw.fw_id as fwId,dc.id as dcId,dc.dc_name as dcName,ct.cus_name as cusName,fwp.rules as rules,ct.cus_org as cusOrg");
		sql.append(" from cloud_fwpolicy fwp");
		sql.append(" left outer join cloud_project cp on fwp.prj_id=cp.prj_id");
		sql.append(" left outer join sys_selfcustomer ct on ct.cus_id=cp.customer_id");
		sql.append(" left outer join dc_datacenter dc on fwp.dc_id=dc.id");
		sql.append(" left outer join cloud_fwrule fwr on fwr.fwp_id=fwp.fwp_id");
		sql.append(" left outer join cloud_firewall fw on fwp.fwp_id=fw.fwp_id");
		sql.append(" where 1=1 ");
		
		if (null!=prjName&&!"".equals(prjName)) {
			sql.append(" and cp.prj_name in (?").append(index+1).append(") ");
			args[index] = Arrays.asList(StringUtils.split(prjName, ","));
			index++;
		}
		if (!"null".equals(datacenterId)&&null!=datacenterId&&!"".equals(datacenterId)&&!"undefined".equals(datacenterId)) {
			sql.append(" and fwp.dc_id = ?").append(index+1);
	    	args[index] = datacenterId;
			index++;
		}
	    if(null!=name&&!"".equals(name)){
	    	sql.append(" and binary fwp.fwp_name like ?").append(index+1).append(" ");
	    	name = name.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	    	args[index] = "%" + name + "%";
			index++;
	    }
	    if (null!=cusOrg&&!"".equals(cusOrg)) {
	    	sql.append(" and ct.cus_org in (?").append(index+1).append(") ");
	 		args[index] = Arrays.asList(StringUtils.split(cusOrg, ","));
			index++;
		}
	    Object[] params = new Object[index];
		System.arraycopy(args, 0, params, 0, index);
		
	    sql.append(" group by fwp.fwp_id order by fwp.create_time desc ");
	    page = cfpdao.pagedNativeQuery(sql.toString(), querymap, params);
	    List newlist = (List) page.getResult();
	    
	    int a = newlist.size();
        for (int i = 0; i < a; i++) {
            Object[] objs = (Object[]) newlist.get(i);
            CloudFwPolicy cfi = new CloudFwPolicy();
            
          //根据查到的策略id 去找对应的规则名称
        	StringBuffer ruleSql=new StringBuffer();
        	ruleSql.append(" SELECT policy.fwp_id as fwpId,rule.fwr_name as ruleName ");
        	ruleSql.append(" FROM cloud_fwpolicy policy ");
        	ruleSql.append(" LEFT JOIN cloud_fwrule rule  ON rule.fwp_id = policy.fwp_id ");
        	ruleSql.append(" WHERE 1=1 and policy.fwp_id = ? ");
            javax.persistence.Query query = cfpdao.createSQLNativeQuery(ruleSql.toString(), ObjectUtils.toString(objs[0]));
            List listResult = query.getResultList();
            StringBuffer rulename=new StringBuffer("");
            for (int ii = 0; ii < listResult.size(); ii++) {
            	Object[] objRules = (Object[]) listResult.get(ii);
            	String fwrName = "";
            	fwrName = ObjectUtils.toString(objRules[1]);
            	if(null!=fwrName &&!"".equals(fwrName)){
            		rulename.append(fwrName+",");
            	}
            }
            cfi.setFwrName(rulename.toString());
            if(null!=cfi.getFwrName()&&!"".equals(cfi.getFwrName())){
            	cfi.setFwrName(cfi.getFwrName().substring(0, cfi.getFwrName().length()-1));
            }
            cfi.setFwpId(ObjectUtils.toString(objs[0]));
            cfi.setFwpName(ObjectUtils.toString(objs[1]));
            cfi.setAudited(ObjectUtils.toString(objs[2]));
            cfi.setDescription(ObjectUtils.toString(objs[3]));
            cfi.setPrjName(ObjectUtils.toString(objs[4]));
            cfi.setPrjId(ObjectUtils.toString(objs[5]));
            cfi.setRulenum(ObjectUtils.toString(objs[6]));
            cfi.setFwId(ObjectUtils.toString(objs[7]));
            cfi.setDcId(ObjectUtils.toString(objs[8]));
            cfi.setDcName(ObjectUtils.toString(objs[9]));
            cfi.setCusName(ObjectUtils.toString(objs[10]));
            cfi.setRules(ObjectUtils.toString(objs[11]));
            cfi.setCusOrg(ObjectUtils.toString(objs[12]));
            newlist.set(i, cfi);
        }
		return page;
	}

	
	@Override
	public List queryIdandName(String prjId) throws AppException {
		return cfpdao.queryIdandName(prjId);
	}
	
	@Override
	public BaseCloudFwPolicy getById(String id) throws AppException {
		return cfpdao.findOne(id);
	}
	@Override
	public FirewallPolicy create(Map<String, String> parmes) throws AppException {
		String name = parmes.get("name");
		String rule = parmes.get("ruleids");
		String projectId = parmes.get("projectId");
		String datacenterId = parmes.get("datacenterId");
		String [] rules = new String[]{};
		if(rule!=null && !"".equals(rule)){
			rules= rule.split(",");	
		}
		if(checkFwPolicyName(name, projectId, datacenterId, null)){throw new AppException("该项目下防火墙已存在，不可重复创建");}
		//防火墙策略数据
		JSONObject fwp = new JSONObject();			
		fwp.put("name", name);
		fwp.put("firewall_rules", rules);
		fwp.put("audited", "1");
		//用于提交的完整数据
		JSONObject resultData = new JSONObject();
		resultData.put("firewall_policy", fwp);
			
		try {
			//openstack创建网络
			FirewallPolicy firewallPolicy =service.create(datacenterId,projectId,resultData);
			//保存至数据库
			if(firewallPolicy!=null){
				BaseCloudFwPolicy cloudFwPolicy=new BaseCloudFwPolicy();
				//CloudFwRule cloudFwRule = new CloudFwRule();
				cloudFwPolicy.setFwpId(firewallPolicy.getId());
				cloudFwPolicy.setFwpName(firewallPolicy.getName());
				cloudFwPolicy.setCreateName(EcmcSessionUtil.getUser() != null ? EcmcSessionUtil.getUser().getAccount() : null);
				cloudFwPolicy.setCreateTime(new Date());		
				cloudFwPolicy.setPrjId(projectId);
				cloudFwPolicy.setDcId(datacenterId);
				cloudFwPolicy.setDescription(firewallPolicy.getDescription());
				cloudFwPolicy.setAudited("1");
				cfpdao.save(cloudFwPolicy);
				for(int i=0;i<rules.length;i++){
					BaseCloudFwRule cloudFwRule = cfrdao.findOne(rules[i]);
					cloudFwRule.setFwpId(cloudFwPolicy.getFwpId());
					cloudFwRule.setFwrId(rules[i]);
					cloudFwRule.setPriority((i+1)+"");
					cfpdao.saveOrUpdate(cloudFwRule);
				}
			}
			return firewallPolicy;
		}catch (AppException ae) {
			throw ae;
		}catch (Exception e) {
		    log.error(e.toString(),e);
			return null;
		}
	}
	
	@Override
	public List<BaseCloudFwRule> getByFwrId(String fwpId) throws AppException {
		return cfpdao.getByFwrId(fwpId);
	}
	@Override
	public List<BaseCloudFwRule> getByFwrIdList(String projectId) throws AppException {
		return cfpdao.getByprojectId(projectId);
	}
	
	@Override
	public FirewallPolicy update(Map<String, String> parmes) throws AppException {
		String name = parmes.get("name");
		String rule = parmes.get("ruleids");
		String datacenterId = parmes.get("datacenterId");
		String projectId = parmes.get("projectId");
		String id = parmes.get("id");
		String [] rulesFormodify = new String[]{};
		if(rule!=null && !"".equals(rule)){
			rulesFormodify= rule.split(",");	
		}
		//防火墙策略数据
		JSONObject fwp = new JSONObject();			
		fwp.put("name", name);
		//用于提交的完整数据
		JSONObject resultData = new JSONObject();
		resultData.put("firewall_policy", fwp);
		
		try {
			//openstack修改策略
			FirewallPolicy firewallPolicy = service.update(datacenterId,projectId,resultData, id);
			//保存至数据库
			if(firewallPolicy!=null){
				BaseCloudFwPolicy cloudFwPolicy=cfpdao.findOne(id);
				cloudFwPolicy.setFwpName(firewallPolicy.getName());
				cfpdao.saveOrUpdate(cloudFwPolicy);
				    //判断已选择规则是否为空
					if(rulesFormodify.length>0){	
						//清除原来的规则
						cfrdao.removePolicy(id);
						//添加新的规则
						BaseCloudFwRule cloudFwRule = null;
						for(int i=0;i<rulesFormodify.length;i++){
							cloudFwRule = cfrdao.findOne(rulesFormodify[i]);	
							cloudFwRule.setFwpId(cloudFwPolicy.getFwpId());//设置新的规则ID
							cloudFwRule.setFwrId(rulesFormodify[i]);
							cfrdao.saveOrUpdate(cloudFwRule);
						}
					}
			}
			return firewallPolicy;
		}catch (AppException ae) {
			throw ae;
		}catch (Exception e) {
		    log.error(e.toString(),e);
			return null;
		}
	}
	
	@Override
	public boolean deletePolicy(String datacenterId,String projectId,String fwpId) throws AppException {
		try {
			//执行openstack删除操作成功后，进行后续操作
			if (service.delete(datacenterId, projectId, fwpId)) {
				//当删除数据库操作失败时，线程等待1秒后尝试重新删除，最多重试10次
					cfpdao.delete(fwpId);
					String sql=" update cloud_fwrule fr set fr.fwp_id=null where fr.fwp_id='"+fwpId+"'";
					cfpdao.createSQLNativeQuery(sql).executeUpdate();
			}
			return true;
		} catch (AppException ae) {
			throw ae;
		} catch (Exception e){
		    log.error(e.toString(),e);
			return false;
		}
	}
	
	@Override
	public boolean toDoFwRule(CloudFwPolicy fwp) throws AppException {
		try{
			if(null==fwp.getFirewallRules()){
				return true;
			}
			int ArrLength=fwp.getFirewallRules().size();
			String [] rulesFormodify = new String[ArrLength];
			for(int i=0;i<ArrLength;i++){
				rulesFormodify[i]=fwp.getFirewallRules().get(i).getFwrId();
			}
			//防火墙策略数据
			JSONObject data = new JSONObject();
			data.put("firewall_rules", rulesFormodify);
			data.put("audited", "1");
			//用于提交的完整数据
			JSONObject resultData = new JSONObject();
			resultData.put("firewall_policy", data);
			FirewallPolicy fwPolicy=service.update(fwp.getDcId(), fwp.getPrjId(), resultData, fwp.getFwpId());
			
			if(null!=fwPolicy){
				StringBuilder hql=new StringBuilder();
				hql.append("update BaseCloudFwRule t set fwpId =null where  fwpId=?");
				List<String> values = new ArrayList<String>();
				values.add(fwp.getFwpId());
				cfrdao.executeUpdate(hql.toString(), values.toArray());
				
				StringBuilder hq=new StringBuilder();
				hq.append("update BaseCloudFwRule t set fwpId =? , priority = ? where  fwrId=?");
				List<String> params=null;
				for(int j=0;j<rulesFormodify.length;j++){
					params = new ArrayList<String>();
					params.add(fwp.getFwpId());
					params.add((j+1)+"");
					params.add(rulesFormodify[j]);
					cfrdao.executeUpdate(hq.toString(), params.toArray());
					params=null;
				}
				
				BaseCloudFwPolicy baseCloudFwPolicy=cfpdao.findOne(fwp.getFwpId());
				cfpdao.saveOrUpdate(baseCloudFwPolicy);
				
				return true;
			}
			
		}catch(AppException e){
		    log.error(e.toString(),e);
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
			FirewallPolicy fwPolicy=service.update(fwp.getDcId(), fwp.getPrjId(), resultData, fwp.getFwpId());
			
			if(null!=fwPolicy){
				StringBuilder hql=new StringBuilder();
				hql.append("update BaseCloudFwRule t set fwpId =null where  fwpId=?");
				List<String> values = new ArrayList<String>();
				values.add(fwp.getFwpId());
				cfrdao.executeUpdate(hql.toString(), values.toArray());
				
				BaseCloudFwPolicy baseCloudFwPolicy=cfpdao.findOne(fwp.getFwpId());
				cfpdao.saveOrUpdate(baseCloudFwPolicy);
				
				return true;
			}
			
		}catch(AppException e){
		    log.error(e.toString(),e);
			throw e;
		}
		return false;
	}
	
	@Override
	public List<CloudFwPolicy> getFwpListByPrjId(String dcId, String prjId)
			throws AppException {

		List<CloudFwPolicy> listFw=new ArrayList<CloudFwPolicy>();
		List<CloudFwPolicy> policyList=new ArrayList<CloudFwPolicy>();
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
		Query query=cfpdao.createSQLNativeQuery(sql.toString(),params);
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
        for(int i=0;i<listFw.size();i++){
			if(!"null".equals(listFw.get(i).getFwId())){
				continue;
			}
			policyList.add(listFw.get(i));
		}
		return policyList;
		
	}
	public List<CloudFwPolicy> getPolicyListByDcIdPrjId(String dcId, String prjId)throws AppException {

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
		Query query=cfpdao.createSQLNativeQuery(sql.toString(),params);
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
		
	}
	
	@Override
	public boolean updateRuleSequence(CloudFwPolicy fwp,String local,String target,String reference) throws AppException {
		try{
			List<BaseCloudFwRule> rules = getByFwrId(fwp.getFwpId());
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
			FirewallPolicy fwPolicy=service.update(fwp.getDcId(), fwp.getPrjId(), resultData, fwp.getFwpId());
			
			if(null!=fwPolicy){
				StringBuilder hql=new StringBuilder();
				hql.append("update BaseCloudFwRule t set fwpId =null where  fwpId=?");
				List<String> values = new ArrayList<String>();
				values.add(fwp.getFwpId());
				cfrdao.executeUpdate(hql.toString(), values.toArray());
				
				StringBuilder hq=new StringBuilder();
				hq.append("update BaseCloudFwRule t set fwpId = ? , priority = ? where  fwrId=?");
				List<String> params=null;
				for(int j=0;j<rulesFormodify.length;j++){
					params = new ArrayList<String>();
					params.add(fwp.getFwpId());
					params.add((j+1)+"");
					params.add(rulesFormodify[j]);
					cfrdao.executeUpdate(hq.toString(), params.toArray());
					params=null;
				}
				
				BaseCloudFwPolicy baseCloudFwPolicy=cfpdao.findOne(fwp.getFwpId());
				cfpdao.saveOrUpdate(baseCloudFwPolicy);
				
				return true;
			}
			
		}catch(AppException e){
		    log.error(e.toString(),e);
			throw e;
		}
		return false;
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
	public static void main(String[] args) {
		String target = "猪";//目标
		String reference = "马";//参照物
		String local = "pre1";
		int targetnum = 0,referencenum=0;
		List<String> rules = new ArrayList<>();
		rules.add("猪");
		rules.add("狗");
		rules.add("牛");
		rules.add("羊");
		rules.add("鸡");
		rules.add("马");
		rules.add("兔");
		rules.add("猫");
		int ruleslength = rules.size();
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
		System.out.println(rules);
	}
}
