package com.eayun.virtualization.ecmcservice.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.tools.DictUtil;
import com.eayun.eayunstack.model.Firewall;
import com.eayun.eayunstack.model.FirewallPolicy;
import com.eayun.eayunstack.model.FirewallRule;
import com.eayun.eayunstack.service.OpenstackFirewallService;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.project.ecmcservice.EcmcProjectService;
import com.eayun.virtualization.dao.CloudFireWallDao;
import com.eayun.virtualization.ecmcservice.EcmcCloudFireWallPoliyService;
import com.eayun.virtualization.ecmcservice.EcmcCloudFireWallService;
import com.eayun.virtualization.ecmcservice.EcmcCloudFwRuleService;
import com.eayun.virtualization.model.BaseCloudFireWall;
import com.eayun.virtualization.model.BaseCloudFwRule;
import com.eayun.virtualization.model.CloudFireWall;
import com.eayun.virtualization.model.CloudFwPolicy;
import com.eayun.virtualization.model.CloudFwRule;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.service.TagService;


/**
 * @author jingang.liu@eayun.com to beijing
 * @date 2016年4月12日
 */
@Service
@Transactional
public class EcmcCloudFireWallServiceImpl implements EcmcCloudFireWallService {

	private static final Logger log = LoggerFactory.getLogger(EcmcCloudFireWallServiceImpl.class);
	
	@Autowired
	private OpenstackFirewallService service;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private CloudFireWallDao cloudfirewalldao;
	@Autowired
	private TagService tagService;
	@Autowired
	private EcmcProjectService projectservice;
	/**创建策略*/
	@Autowired
	private EcmcCloudFireWallPoliyService poliyservice;
	/**创建规则*/
	@Autowired
	private EcmcCloudFwRuleService ruleservice;
	@Override
	public Page list(Page page ,String datacenterId, String prjName, String name,String cusOrg,QueryMap querymap) throws AppException {
		log.info("防火墙分页查询");
		int index = 0;
		Object[] args = new Object[4];
		
		StringBuffer sql=new StringBuffer();
		sql.append("select fw.fw_id as fwId,fw.fw_name as fwName,fwp.fwp_id as fwpId,fwp.fwp_name as fwpName,fw.fw_status as fwStatus,"
				+ "fw.admin_stateup as adminStateup,fw.description as description,cp.prj_name as prjName,cp.prj_id as prjId,dc.dc_name as dcName,"
				+ "dc.id as dcId,ss.cus_name as cusName,ss.cus_org as cusOrg,fw.create_time as createTime ");
		sql.append(" from cloud_firewall fw");
		sql.append(" left join cloud_project cp on fw.prj_id=cp.prj_id ");
		sql.append(" left join cloud_fwpolicy fwp on fw.fwp_id=fwp.fwp_id ");
		sql.append(" left join dc_datacenter dc on fw.dc_id=dc.id ");
		sql.append(" left join sys_selfcustomer ss on cp.customer_id = ss.cus_id ");
		sql.append(" where 1=1 ");
	 	if (null!=prjName&&!"".equals(prjName)) {
	 		sql.append(" and cp.prj_name in (?").append(index+1).append(") ");
			args[index] = Arrays.asList(StringUtils.split(prjName, ","));
			index++;
		}
	    if (!"null".equals(datacenterId)&&null!=datacenterId&&!"".equals(datacenterId)&&!"undefined".equals(datacenterId)) {
	    	sql.append(" and fw.dc_id = ?").append(index+1);
	    	args[index] = datacenterId;
			index++;
		}
	    if(null!=name&&!"".equals(name)){
	    	sql.append(" and binary fw.fw_name like ?").append(index+1).append(" ");
	    	name = name.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	    	args[index] = "%" + name + "%";
			index++;
	    }
	    if (null!=cusOrg&&!"".equals(cusOrg)) {
	    	sql.append(" and ss.cus_org in (?").append(index+1).append(") ");
	 		args[index] = Arrays.asList(StringUtils.split(cusOrg, ","));
			index++;
		}
	    Object[] params = new Object[index];
		System.arraycopy(args, 0, params, 0, index);
	    sql.append(" group by fw.fw_id order by fw.create_time desc ");
	    page = cloudfirewalldao.pagedNativeQuery(sql.toString(), querymap, params);
	    List newlist = (List) page.getResult();
	    int a = newlist.size();
        for (int i = 0; i < a; i++) {
            Object[] objs = (Object[]) newlist.get(i);
            CloudFireWall firewall = new CloudFireWall();
            firewall.setFwId(ObjectUtils.toString(objs[0]));
            firewall.setFwName(ObjectUtils.toString(objs[1]));
            firewall.setFwpId(ObjectUtils.toString(objs[2]));
            firewall.setFwpName(ObjectUtils.toString(objs[3]));
            firewall.setFwStatus(ObjectUtils.toString(objs[4]));
            firewall.setAdminStateup(ObjectUtils.toString(objs[5]));
            firewall.setDescription(ObjectUtils.toString(objs[6]));
            firewall.setPrjName(ObjectUtils.toString(objs[7]));
            firewall.setPrjId(ObjectUtils.toString(objs[8]));
            firewall.setDcName(ObjectUtils.toString(objs[9]));
            firewall.setDcId(ObjectUtils.toString(objs[10]));
            firewall.setCusName(ObjectUtils.toString(objs[11]));
            firewall.setCusOrg(ObjectUtils.toString(objs[12]));
            firewall.setCreateTime(((Date)objs[13]));
            firewall.setStatusForDis(DictUtil.getStatusByNodeEn("fireWall", firewall.getFwStatus()));
            newlist.set(i, firewall);
        }
		return page;
	}
	@Override
	public boolean checkName(String name,String datacenterId,String projectId,String fwId) throws AppException {
		log.info("检查防火墙名称");
		boolean isExist = false;
		StringBuffer sql = new StringBuffer();
		int index=0;
		Object [] args=new Object[4];
		sql.append("select count(*) from cloud_firewall  as fw  where 1=1 ");
		//项目
		if (!"null".equals(projectId)&&null!=projectId&&!"".equals(projectId)&&!"undefined".equals(projectId)) {
			sql.append(" and fw.prj_id= ? ");
			args[index]=projectId;
			index++;
		}
		//数据中心
		if (!"".equals(datacenterId)&&datacenterId!=null&&!"undefined".equals(datacenterId)&&!"null".equals(datacenterId)) {
			sql.append("and fw.dc_id = ? ");
			args[index]=datacenterId;
			index++;
		}
		//防火墙名称
		if (!"".equals(name)&&name!=null) {
			sql.append("and binary fw.fw_name = ? ");
			args[index]=name.trim();
			index++;
		}
		//防火墙ID
		if (!"".equals(fwId)&&fwId!=null&&!"undefined".equals(fwId)&&!"null".equals(fwId)) {
			sql.append("and fw.fw_id <> ? ");
			args[index]=fwId.trim();
			index++;
			}
						
		Object[] params = new Object[index];  
        System.arraycopy(args, 0, params, 0, index);
		int ret = ((Number)cloudfirewalldao.createSQLNativeQuery(sql.toString(), params).getSingleResult()).intValue();
		if(ret>0){
			isExist = true;//返回true 代表存在此名称
		}
		return isExist;
	}
	
	@Override
	public CloudFireWall getFWById(String id) throws AppException {
		log.info("根据ID获取防火墙");
		CloudFireWall cloudFirewall=null;
		try{	
			List<String> params = new ArrayList<>();
			StringBuffer sql=new StringBuffer();
			sql.append("select fw.fw_id as fwId, fw.fw_name as fwName,fwp.fwp_id as fwpId,fwp.fwp_name as fwpName,fw.fw_status as fwStatus,"
					+ "fw.admin_stateup as adminStateup,fw.description as description");
			sql.append(" from cloud_firewall fw,cloud_fwpolicy fwp");
			sql.append(" where fw.fwp_id=fwp.fwp_id");
		    if(!"null".equals(id)&&null!=id&&!"".equals(id)&&!"undefined".equals(id)){
		    	sql.append(" and fw.fw_id = ? ");
		    	params.add(id);
		    }
		    List list = cloudfirewalldao.createSQLNativeQuery(sql.toString(), params.toArray()).getResultList();
		    cloudFirewall =  (CloudFireWall)cloudfirewalldao.findUnique(sql.toString(), params.toArray());
		    if(list!=null && list.size()>0){
		    	cloudFirewall = new CloudFireWall();
		    	Object[] objs = (Object[]) list.get(0);
		    	cloudFirewall.setFwId(ObjectUtils.toString(objs[0]));
		    	cloudFirewall.setFwName(ObjectUtils.toString(objs[1]));
		    	cloudFirewall.setFwpId(ObjectUtils.toString(objs[2]));
		    	cloudFirewall.setFwpName(ObjectUtils.toString(objs[3]));
		    	cloudFirewall.setFwStatus(ObjectUtils.toString(objs[4]));
		    	cloudFirewall.setAdminStateup(ObjectUtils.toString(objs[5]));
		    	cloudFirewall.setDescription(ObjectUtils.toString(objs[6]));
		    	
		    }
			return cloudFirewall ;
		}catch(AppException ae){
			throw ae;
		}catch (Exception e) {
			log.error("根据ID获取防火墙失败：", e);
			//当执行出现未知异常时，打印异常信息，并返回空
			return null;
		}
	}
	
	@Override
	public Firewall create(Map<String, String> parmes) throws AppException {
		String name = parmes.get("name");
		String policy = parmes.get("policy");
		String datacenterId = parmes.get("datacenterId");
		String projectId = parmes.get("projectId");
		//1、判断防火墙名称是否重复
        boolean isExist = this.checkName(name,datacenterId,projectId,null);
		if(isExist==true){
			throw new AppException("该防火墙规则名称在当前数据中心中已存在！");
		}
		//防火墙数据
		JSONObject fw = new JSONObject();			
		fw.put("name", name);
		fw.put("firewall_policy_id", policy);
		fw.put("admin_state_up", "1");
		//用于提交的完整数据
		JSONObject resultData = new JSONObject();
		resultData.put("firewall", fw);
			
		try {
			//openstack创建网络
			Firewall firewall = service.create(datacenterId,projectId, resultData);
			//保存至数据库
			if(firewall!=null){
				BaseCloudFireWall cloudFirewall=new BaseCloudFireWall();
				cloudFirewall.setFwId(firewall.getId());
				cloudFirewall.setFwName(firewall.getName());
				cloudFirewall.setCreateName(EcmcSessionUtil.getUser() != null ? EcmcSessionUtil.getUser().getAccount() : null);
				cloudFirewall.setCreateTime(new Date());		
				cloudFirewall.setPrjId(projectId);
				cloudFirewall.setDcId(datacenterId);
				cloudFirewall.setAdminStateup("1");
				if(!StringUtils.isEmpty(firewall.getStatus())){
					cloudFirewall.setFwStatus(firewall.getStatus().toUpperCase());
				}
				cloudFirewall.setFwpId(policy);
				cloudfirewalldao.save(cloudFirewall);
				
				if(null!=cloudFirewall.getFwStatus()&&!"ACTIVE".equals(cloudFirewall.getFwStatus())){
					//TODO 同步新增防火墙状态
					JSONObject json =new JSONObject();
					json.put("fwId",cloudFirewall.getFwId());
					json.put("dcId",cloudFirewall.getDcId());
					json.put("prjId", cloudFirewall.getPrjId());
					json.put("fwStatus",cloudFirewall.getFwStatus());
					json.put("count", "0");
					jedisUtil.push(RedisKey.fwKey, json.toString());
				}
			}
			return firewall;
		}catch (AppException e) {
			log.error("创建防火墙异常：",e);
			throw e;
		}catch (Exception e) {
			log.error("创建防火墙异常：",e);
			return null;
		}
	}
	
	@Override
	public Firewall update(Map<String, String> parmes) throws AppException {
		String name = parmes.get("name");
		String policy = parmes.get("policy");
		String description = parmes.get("description");
		String datacenterId = parmes.get("datacenterId");
		String id = parmes.get("id");
		String projectId = parmes.get("projectId");
		//1、判断防火墙规则名称是否重复
        boolean isExist = this.checkName(name,datacenterId,projectId,id);
		if(isExist==true){
			throw new AppException("该防火墙规则名称在当前数据中心中已存在！");
		}
		
		JSONObject fw = new JSONObject();			
		fw.put("name", name);
		fw.put("firewall_policy_id", policy);
		fw.put("description", description);
		//用于提交的完整数据
		JSONObject resultData = new JSONObject();
		resultData.put("firewall", fw);
		try {
			Firewall firewall = service.update(datacenterId,projectId,resultData, id);
			//保存至数据库
			if(firewall!=null){
				BaseCloudFireWall cloudFirewall=cloudfirewalldao.findOne(id);
				cloudFirewall.setFwName(firewall.getName());
				cloudFirewall.setFwpId(policy);
				cloudFirewall.setFwStatus(firewall.getStatus());
				cloudFirewall.setDescription(firewall.getDescription());
				cloudfirewalldao.saveOrUpdate(cloudFirewall);	
				//同步防火墙状态
				if(null!=cloudFirewall.getFwStatus()&&!"ACTIVE".equals(cloudFirewall.getFwStatus())){
					JSONObject json =new JSONObject();
					json.put("fwId",cloudFirewall.getFwId());
					json.put("dcId",cloudFirewall.getDcId());
					json.put("prjId", cloudFirewall.getPrjId());
					json.put("fwStatus",cloudFirewall.getFwStatus());
					json.put("count", "0");
					jedisUtil.push(RedisKey.fwKey, json.toString());
				}
			}
			
			return firewall;
		}catch (AppException e) {
			log.error("修改防火墙异常：",e);
			throw e;
		}catch (Exception e) {
			log.error("修改防火墙异常：",e);
			return null;
		}
	}
	
	@Override
	public boolean delete(String datacenterId,String projectId, String id) throws AppException {
		try {
			//执行openstack删除操作成功后，进行后续操作
			if (service.delete(datacenterId,projectId, id)) {
				BaseCloudFireWall fireWall = cloudfirewalldao.findOne(id);
				fireWall.setFwStatus("PENDING_DELETE");
				cloudfirewalldao.saveOrUpdate(fireWall);
				tagService.refreshCacheAftDelRes("firewall",id);
				JSONObject json =new JSONObject();
				json.put("fwId",fireWall.getFwId());
				json.put("dcId",fireWall.getDcId());
				json.put("prjId", fireWall.getPrjId());
				json.put("fwStatus",fireWall.getFwStatus());
				json.put("count", "0");
				jedisUtil.addUnique(RedisKey.fwKey, json.toString());
			}
			return true;
		}catch (AppException e) {
			log.error("删除防火墙异常：",e);
			throw e;
		}catch (Exception e) {
			log.error("删除防火墙异常：",e);
			return false;
		}
	}
	@Override
	public List<BaseCloudFireWall> getfirewallBydcId(String datacenterId) throws AppException {
		return cloudfirewalldao.find("from BaseCloudFireWall fw where fw.dcId = ? ", datacenterId);
	}
	@Override
	public List<CloudProject> projects(String datacenterId) throws AppException {
		List<CloudProject> projectlist = null;
		try {
			projectlist = projectservice.firewallProject(datacenterId);
			List<BaseCloudFireWall> firewalllist = this.getfirewallBydcId(datacenterId);
			if(projectlist!=null && projectlist.size()>0 && firewalllist!=null && firewalllist.size()>0){
				Iterator it = projectlist.iterator();//动态移除list元素必须使用迭代  
				for(BaseCloudFireWall fw : firewalllist){
					while(it.hasNext()) {
						CloudProject p = (CloudProject)it.next();
						if(p.getProjectId().equals(fw.getPrjId())){
							it.remove();
						}           
					}
				}
			}
			log.info("projects配置列表："+JSONArray.toJSONString(projectlist).toString());
		} catch (AppException ae) {
			throw ae;
		} catch (Exception e) {
			log.error("error.ecmc.cloud.firewall.getproject",e);
		}
		return projectlist;
	}
	
	@Override
	public int countFireWallByPrjId(String prjId) throws AppException {
		int countFirewall=cloudfirewalldao.countFireWallByPrjId(prjId);
		return countFirewall;
	}

	@Override
	public Firewall createFwAndFwpAndRule(Map<String, String> parmes) throws AppException {
		FirewallRule rule = null;
		FirewallPolicy policy =null;
		String datacenterId = parmes.get("datacenterId");
		String projectId = parmes.get("projectId");
		try{
			parmes.put("name", parmes.get("rulename"));//设置规则的名称
			//创建规则
			rule = ruleservice.createCloudFwRule(parmes);
			parmes.put("ruleids", rule.getId());//为创建策略绑定规则
			parmes.put("name", parmes.get("fwname")+"poliy");//设置默认策略的名称
			//创建策略
			policy = poliyservice.create(parmes);
			parmes.put("policy", policy.getId());//为创建防火墙绑定策略
			parmes.put("name", parmes.get("fwname"));//设置防火墙的名称
			//创建防火墙
			return create(parmes);
		}catch(Exception e){
			if(rule!=null){
				if(policy!=null){
					CloudFwPolicy fwp = new CloudFwPolicy();
					fwp.setFwpId(policy.getId());
					fwp.setDcId(datacenterId);
					fwp.setPrjId(projectId);
					poliyservice.releaseFwRule(fwp);//解绑规则
				}
				ruleservice.delete(datacenterId, projectId, rule.getId());//删除规则
			}
			if(policy!=null){
				poliyservice.deletePolicy(datacenterId, projectId, policy.getId());//删除策略
			}
			throw e;
		}
	}
	
	@Override
	public boolean deleteFwAndFwpAndRule(Map<String, String> parmes) throws AppException {
		Firewall fw = null;
		String datacenterId = parmes.get("datacenterId");
		String projectId = parmes.get("projectId");
		fw = service.getById(datacenterId, projectId, parmes.get("id"));
		if(!"ACTIVE".equals(fw.getStatus()) && !"ERROR".equals(fw.getStatus())){
			throw new AppException("只有正常状态和错误状态下的防火墙允许删除。");
		}
		boolean fwresult = delete(datacenterId, projectId, parmes.get("id"));//删除防火墙
		if(!fwresult)return false;
		while(true){//等待底层防火墙删除
			try{
				fw = service.getById(datacenterId, projectId, parmes.get("id"));
				if(null==fw){
					break;
				}
				try {
					log.debug("等待底层删除防火墙");
					Thread.sleep(500);
				} catch (InterruptedException e) {
				    log.error(e.getMessage(), e);
				}
			}catch(AppException e){
				//出现异常说明防火墙已被删除
				break;
			}
			
		}
		List<BaseCloudFwRule> listcr = poliyservice.getByFwrId(parmes.get("fwpId"));//获取策略下所有规则
		CloudFwPolicy fwp = new CloudFwPolicy();
		fwp.setFwpId(parmes.get("fwpId"));
		fwp.setDcId(datacenterId);
		fwp.setPrjId(projectId);
		poliyservice.releaseFwRule(fwp);//解绑规则
		boolean ruleresult = true;
		for(BaseCloudFwRule cr : listcr){
			ruleresult = ruleservice.delete(datacenterId, projectId,cr.getFwrId());//删除规则
			if(!ruleresult){
				break;
			}
		}
		if(!ruleresult)return false;
		
		boolean poliyresult = poliyservice.deletePolicy(datacenterId, projectId, parmes.get("fwpId"));//删除策略
		
		if(ruleresult && poliyresult && fwresult){
			return true;
		}else{
			return false;
		}
	}
	
	@Override
	public CloudFireWall getFwByIdDetail(String id) throws AppException {
		
		CloudFireWall firewall=null;
		List<String> params = new ArrayList<>();
		StringBuffer sql=new StringBuffer();
		sql.append("select fw.fw_id as fwId,fw.fw_name as fwName,fwp.fwp_id as fwpId,fwp.fwp_name as fwpName,fw.fw_status as fwStatus,"
				+ "fw.admin_stateup as adminStateup,fw.description as description,cp.prj_name as prjName,cp.prj_id as prjId,dc.dc_name as dcName,"
				+ "dc.id as dcId,ss.cus_name as cusName,ss.cus_org as cusOrg,fw.create_time as createTime ");
		sql.append(" from cloud_firewall fw");
		sql.append(" left join cloud_project cp on fw.prj_id=cp.prj_id ");
		sql.append(" left join cloud_fwpolicy fwp on fw.fwp_id=fwp.fwp_id ");
		sql.append(" left join dc_datacenter dc on fw.dc_id=dc.id ");
		sql.append(" left join sys_selfcustomer ss on cp.customer_id = ss.cus_id ");
		sql.append(" where 1=1 ");
		
		if(!"null".equals(id)&&null!=id&&!"".equals(id)&&!"undefined".equals(id)){
	    	sql.append(" and fw.fwp_id = ? ");
	    	params.add(id);
	    }
	    List list = cloudfirewalldao.createSQLNativeQuery(sql.toString(), params.toArray()).getResultList();
	    //firewall =  (CloudFireWall)cloudfirewalldao.findUnique(sql.toString(), params.toArray());
	    if(list!=null && list.size()>0){
	    	firewall = new CloudFireWall();
	    	Object[] objs = (Object[]) list.get(0);
	    	firewall.setFwId(ObjectUtils.toString(objs[0]));
            firewall.setFwName(ObjectUtils.toString(objs[1]));
            firewall.setFwpId(ObjectUtils.toString(objs[2]));
            firewall.setFwpName(ObjectUtils.toString(objs[3]));
            firewall.setFwStatus(ObjectUtils.toString(objs[4]));
            firewall.setAdminStateup(ObjectUtils.toString(objs[5]));
            firewall.setDescription(ObjectUtils.toString(objs[6]));
            firewall.setPrjName(ObjectUtils.toString(objs[7]));
            firewall.setPrjId(ObjectUtils.toString(objs[8]));
            firewall.setDcName(ObjectUtils.toString(objs[9]));
            firewall.setDcId(ObjectUtils.toString(objs[10]));
            firewall.setCusName(ObjectUtils.toString(objs[11]));
            firewall.setCusOrg(ObjectUtils.toString(objs[12]));
            firewall.setCreateTime(((Date)objs[13]));
            Firewall fw = service.getById(firewall.getDcId(), firewall.getPrjId(), firewall.getFwId());
            if(!firewall.getFwStatus().equals(fw.getStatus())){
            	cloudfirewalldao.executeUpdate(" update BaseCloudFireWall set fwStatus = ? where fwId = ? ", fw.getStatus(),fw.getId());
            	firewall.setFwStatus(fw.getStatus());
            }
            firewall.setStatusForDis(DictUtil.getStatusByNodeEn("fireWall", firewall.getFwStatus()));
	    }
		return firewall;
	}
}
