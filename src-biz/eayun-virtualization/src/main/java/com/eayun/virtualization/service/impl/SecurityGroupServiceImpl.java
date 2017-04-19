package com.eayun.virtualization.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.util.DateUtil;
import com.eayun.eayunstack.model.Rule;
import com.eayun.eayunstack.model.SecurityGroup;
import com.eayun.eayunstack.service.OpenstackSecurityGroupRuleService;
import com.eayun.eayunstack.service.OpenstackSecurityGroupService;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.virtualization.dao.CloudSecurityGroupDao;
import com.eayun.virtualization.dao.CloudSecurityGroupRuleDao;
import com.eayun.virtualization.model.BaseCloudSecurityGroup;
import com.eayun.virtualization.model.BaseCloudSecurityGroupRule;
import com.eayun.virtualization.model.CloudSecurityGroup;
import com.eayun.virtualization.model.CloudSecurityGroupRule;
import com.eayun.virtualization.service.SecurityGroupService;
import com.eayun.virtualization.service.TagService;

/**
 * SecurityGroupServiceImpl
 * 
 * @Filename: SecurityGroupServiceImpl.java
 * @Description:
 * @Version: 1.0
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br> <li>Date: 2015年10月16日</li> <li>Version: 1.0</li> <li>Content:
 *               create</li>
 * 
 */
@Service
@Transactional
public class SecurityGroupServiceImpl implements SecurityGroupService {
	private static final Logger log = LoggerFactory
			.getLogger(SecurityGroupServiceImpl.class);
	@Autowired
	private CloudSecurityGroupDao securityGroupDao;
	@Autowired
	private CloudSecurityGroupRuleDao securityGroupRuleDao;
	@Autowired
	private OpenstackSecurityGroupService openstackService;
	@Autowired
	private OpenstackSecurityGroupRuleService openstackSecurityGroupRuleService;
	
	@Autowired
	private TagService tagService;
	/*
	 *根据prjId查询个数 
	 */
	public int getCountByPrjId(String prjId){
		return securityGroupDao.getCountByPrjId(prjId);
	}
	
	/*根据数据中心、项目、安全组Id获取实体*/
	public BaseCloudSecurityGroup getGroup(String dcId, String prjId, String sgId) {
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();
		List<BaseCloudSecurityGroup> listGroup = new ArrayList<BaseCloudSecurityGroup>();
		sql.append(" select sg.sg_id,sg.sg_name,sg.sg_description,dc.dc_name,cp.prj_name,sg.default_group ,sg.create_time from cloud_securitygroup  as sg ");
		sql.append(" left join cloud_project as cp on sg.prj_id=cp.prj_id");
		sql.append(" left join dc_datacenter as dc on dc.id=sg.dc_id");
		sql.append(" where 1=1 ");
		// 数据中心
		if (!"".equals(dcId) && dcId != null && !"undefined".equals(dcId)
				&& !"null".equals(dcId)) {
			sql.append("and sg.dc_id = ? ");
			list.add(dcId);
		}
		// 安全组ID
		if (!"null".equals(prjId) && null != prjId && !"".equals(prjId)
				&& !"undefined".equals(prjId)) {
			sql.append(" and sg.prj_id = ? ");
			list.add(prjId);
		}
		// 安全组ID
		if (!"null".equals(sgId) && null != sgId && !"".equals(sgId)
				&& !"undefined".equals(sgId)) {
			sql.append(" and sg.sg_id = ? ");
			list.add(sgId);
		}
		javax.persistence.Query query =securityGroupDao.createSQLNativeQuery(sql.toString(), list.toArray());
		List listResult = query.getResultList();
		for (int i = 0; i < listResult.size(); i++) {
			Object[] objs = (Object[]) listResult.get(i);
			CloudSecurityGroup securityGroup = new CloudSecurityGroup();
			securityGroup.setSgId(String.valueOf(objs[0]));
			securityGroup.setSgName(String.valueOf(objs[1]));
			securityGroup.setSgDescription(String.valueOf(objs[2]));
			securityGroup.setDcName(String.valueOf(objs[3]));
			securityGroup.setPrjName(String.valueOf(objs[4]));
			securityGroup.setDefaultGroup(String.valueOf(objs[5]));
			securityGroup.setCreateTime(DateUtil.stringToDate(String.valueOf(objs[6])));
			listGroup.add(securityGroup);
		}
		if(listGroup.size()>0){
			return listGroup.get(0);
		}
		
		return null;
	}

	@Override
	// 用于判断重名--创建时
	public boolean getGroupByName(String prjId, String sgId, String sgName) {
		boolean isExist = true;
		StringBuffer sql = new StringBuffer();
		List<Object> list = new ArrayList<Object>();

		sql.append("select sg.* from cloud_securitygroup  as sg  where 1=1 ");
		// 项目
		if (!"".equals(prjId) && prjId != null && !"undefined".equals(prjId)
				&& !"null".equals(prjId)) {
			sql.append("and sg.prj_id = ? ");
			list.add(prjId);
		}
		// 安全组ID
		if (!"null".equals(sgId) && null != sgId && !"".equals(sgId)
				&& !"undefined".equals(sgId)) {
			sql.append(" and sg.sg_id<> ? ");
			list.add(sgId);
		}

		// 安全组名称
		if (!"".equals(sgName) && sgName != null && !"undefined".equals(sgName)) {
			sql.append("and binary sg.sg_name = ? ");
			list.add(sgName);
		}
		javax.persistence.Query query = securityGroupDao.createSQLNativeQuery(
				sql.toString(), list.toArray());
		List listResult = query.getResultList();
		if (listResult.size() > 0) {
			isExist = false;// 返回false 代表存在此名称
		}
		return isExist;

		
	}
	// 用于判断重名--编辑时
		public boolean getGroupById(String prjId, String sgId, String sgName) {
			boolean isExist = false;
			StringBuffer sql = new StringBuffer();
			List<Object> list = new ArrayList<Object>();

			sql.append("select sg.* from cloud_securitygroup  as sg  where 1=1 ");
			// 项目
			if (!"".equals(prjId) && prjId != null && !"undefined".equals(prjId)
					&& !"null".equals(prjId)) {
				sql.append("and sg.prj_id = ? ");
				list.add(prjId);
			}
			// 安全组ID
			if (!"null".equals(sgId) && null != sgId && !"".equals(sgId)
					&& !"undefined".equals(sgId)) {
				sql.append(" and sg.sg_id<> ? ");
				list.add(sgId);
			}

			// 安全组名称
			if (!"".equals(sgName) && sgName != null && !"undefined".equals(sgName)) {
				sql.append("and binary sg.sg_name = ? ");
				list.add(sgName);
			}
			javax.persistence.Query query = securityGroupDao.createSQLNativeQuery(
					sql.toString(), list.toArray());
			List listResult = query.getResultList();
			if (listResult.size() > 0) {
				isExist = true;// 返回true 代表存在此名称
			}
			return isExist;

			
		}

	@Override
	public Page getSecurityGroupList(Page page, String prjId, String dcId,
			String name, QueryMap queryMap) {
		log.info("查询安全组列表");

		List<Object> list = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("select csg.sg_id as sgId,csg.dc_id as dcId,csg.prj_id as prjId,");
		sql.append(" csg.sg_name as sgName,csg.sg_description as sgDescription,cp.prj_name as prjName,csg.default_group as defaultGroup,csg.create_time as createTime ");
		sql.append(" ,count(vmSeGroup.vm_id) as vmCount");
		sql.append(" from cloud_securitygroup as csg");
		sql.append(" left join cloud_project as cp on csg.prj_id=cp.prj_id");
		
		sql.append(" left join cloud_vmsecuritygroup as vmSeGroup on csg.sg_id=vmSeGroup.sg_id ");
		
		sql.append(" where 1=1");

		if (!"null".equals(prjId) && null != prjId && !"".equals(prjId)
				&& !"undefined".equals(prjId)) {
			sql.append(" and csg.prj_id= ? ");
			list.add(prjId);
		}
		if (!"null".equals(dcId) && null != dcId && !"".equals(dcId)
				&& !"undefined".equals(dcId)) {
			sql.append(" and csg.dc_id= ? ");
			list.add(dcId);
		}
		if (null != name && !"".equals(name)
				&& !"undefined".equals(name)) {
			//name = name.replaceAll("\\_", "\\\\_");
			sql.append(" and binary (case sg_name  when  binary 'default' then '默认安全组' else sg_name end) like ? ");
			list.add("%" + escapeSpecialChar(name) + "%");
		}

		sql.append(" group by csg.sg_id order by IF (ISNULL(csg.create_time),1,0), csg.create_time desc ");
		// 返回page：1.原生sql 2. pagedNativeQuery
		page = securityGroupDao.pagedNativeQuery(sql.toString(), queryMap,
				list.toArray());
		List listResult = (List) page.getResult();
		for (int i = 0; i < listResult.size(); i++) {
			Object[] objs = (Object[]) listResult.get(i);
			CloudSecurityGroup securityGroup = new CloudSecurityGroup();
			securityGroup.setSgId(String.valueOf(objs[0]));
			String tag=tagService.getResourceTagForShowcase("securityGroup", String.valueOf(objs[0]));
			securityGroup.setTagName(tag);
			securityGroup.setDcId(String.valueOf(objs[1]));
			securityGroup.setPrjId(String.valueOf(objs[2]));
			securityGroup.setSgName(String.valueOf(objs[3]));
			securityGroup.setSgDescription(String.valueOf(objs[4]));
			securityGroup.setPrjName(String.valueOf(objs[5]));
			securityGroup.setDefaultGroup(String.valueOf(objs[6]));
			securityGroup
					.setCreateTime(DateUtil.stringToDate(String
							.valueOf(objs[7]) == "null" ? "" : String
							.valueOf(objs[7])));
			securityGroup.setVmCount(String.valueOf(objs[8]) == "null" ? "" :String.valueOf(objs[8]));
			securityGroup.setIsDeleting("not");
			
			listResult.set(i, securityGroup);
		}
		return page;

	}

	public BaseCloudSecurityGroup addSecurityGroup(HttpServletRequest request,
			@RequestBody Map map) {
		String name = map.get("name").toString();
		String description ="";
		if(map.containsKey("description")){
			description = map.get("description").toString();
		}
		
		Map projectMap = (Map) map.get("project");

		String dcId = projectMap.get("dcId").toString();
		String prjId = projectMap.get("projectId").toString();

		// 2、调用底层新增安全组
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("name", name);
		temp.put("description", description);
		data.put("security_group", temp);
		BaseCloudSecurityGroup groupEntity = null;

		// 创建安全组
		SecurityGroup result = openstackService.create(dcId, prjId, data);
		// 3、保存至数据库
		if (result != null) {
			groupEntity = new BaseCloudSecurityGroup();
			groupEntity.setSgId(result.getId());
			groupEntity.setSgName(result.getName());
			// 从session中获取当前用户名
			SessionUserInfo sessionUser = (SessionUserInfo) request
					.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
			String cusId = sessionUser.getCusId();
			String userName = sessionUser.getUserName();

			groupEntity.setCreateName(userName);
			groupEntity.setCreateTime(new Date());
			groupEntity.setPrjId(result.getTenant_id());
			groupEntity.setDcId(dcId);
			groupEntity.setSgDescription(result.getDescription());
			groupEntity.setDefaultGroup("not");
			securityGroupDao.save(groupEntity);
			

			if (result.getSecurity_group_rules().length > 0) {
				for (int i = 0; i < result.getSecurity_group_rules().length; i++) {
					Rule rules = result.getSecurity_group_rules()[i];
					// 因为创建安全组之后，默认创建出来的规则只有三个字段有值，可以只限定设置保存三个字段；
					BaseCloudSecurityGroupRule rulex = new BaseCloudSecurityGroupRule();
					rulex.setSgrId(rules.getId());
					rulex.setCreateTime(groupEntity.getCreateTime());
					rulex.setCreateName(userName);
					rulex.setDcId(dcId);
					rulex.setProtocol("All");
					rulex.setIcMp("--");
					rulex.setPrjId(result.getTenant_id());
					rulex.setSgId(result.getId());
					rulex.setDirection(rules.getDirection());// 方向 非空
					rulex.setEthertype(rules.getEthertype());// ipv4、6 非空
					rulex.setRemoteIpPrefix("0.0.0.0/0");
					if (rules.getProtocol() != null
							&& !rules.getProtocol().equals("")) {
						rulex.setProtocol(rules.getProtocol());
					}

					securityGroupRuleDao.save(rulex);
				}
			}

		}
		return groupEntity;
	}

	public BaseCloudSecurityGroup updateSecurityGroup(
			HttpServletRequest request, @RequestBody Map map) {

		// 1、判断镜像名称是否重复
		// boolean isExist =this.checkSecurityGroupName(datacenterId, projectId,
		// name, id);
		// if(isExist==true){
		// throw new AppException("该安全组名称在当前数据中心中已存在！");//需要可以改为在当前项目下已存在
		// }
		// 2、将当前编辑的数据提交到底层更新
		String prjId = map.get("prjId").toString();
		String dcId = map.get("dcId").toString();
		String groupName = map.get("sgName").toString();
		String groupId = map.get("sgId").toString();
		String sgDescription = map.get("sgDescription").toString();
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("name", groupName);
		temp.put("description", sgDescription);
		data.put("security_group", temp);
		// 执行openstack修改操作
		SecurityGroup result = openstackService.update(dcId, prjId, data,
				groupId);
		// 3、底层更新成功后，再更新数据库
		BaseCloudSecurityGroup group = null;
		if (result != null) {
			// 从数据库中查询该id的实体
			group = securityGroupDao.findOne(groupId);
			// 数据库中不存在是，新建一个实体对象
			if (group != null) {
				group.setSgName(groupName);
				group.setSgDescription(sgDescription);

				// 数据库保存操作
				securityGroupDao.saveOrUpdate(group);

			}
			return group;
		}
		return group;
	}

	// 删除安全组方法；
	public boolean deleteGroup(String dcId, String prjId, String groupId) {
		boolean flag = openstackService.delete(dcId, prjId, groupId);
		if (flag) {
			if (securityGroupDao.findOne(groupId) != null) {
				securityGroupDao.delete(groupId);
				List<Object> list = new ArrayList<Object>();
				StringBuffer hql = new StringBuffer(
						"from BaseCloudSecurityGroupRule gr where 1=1 "); // 组合条件查询
				if (null != groupId && !groupId.trim().equals("")) {
					hql.append(" and gr.sgId= ? ");
					list.add(groupId);
				}
				List<BaseCloudSecurityGroupRule> listRule = securityGroupDao
						.find(hql.toString(), list.toArray());

				for (BaseCloudSecurityGroupRule rule : listRule) {
					securityGroupRuleDao.delete(rule.getSgrId());
				}
				securityGroupRuleDao.deletedsgrule(groupId);
				//删除资源后更新缓存接口
				tagService.refreshCacheAftDelRes("securityGroup", groupId);
				return true;
			}
		}
		return false;
	}

	/**
	 * 根据安全组id查寻其所有本地库的规则
	 * 
	 * @param dcId
	 * @param prjId
	 * @param groupId
	 * @return 返回指定安全组id的规则列表
	 */
	public Page getRules(Page page, String dcId, String prjId, String sgId,
			QueryMap queryMap) {

		List<CloudSecurityGroupRule> listRule = null;
		List<Object> list = new ArrayList<Object>();

		StringBuffer sql = new StringBuffer();
		sql.append("select gr.sgr_id as sgrId,gr.prj_id as prjId,gr.dc_id as dcId,gr.sg_id as sgId,");
		sql.append(" gr.direction as direction,gr.ethertype as ethertype,gr.protocol as protocol,");
		sql.append(" gr.port_rangemin as portRangeMin,gr.port_rangemax as portRangeMax,gr.remote_ipprefix as remoteIpPrefix,csg.sg_name as sgName,");
		sql.append(" gr.protocol_expand as protocolExpand,gr.icmp as icmp ");
		sql.append(" from cloud_grouprule gr");
		sql.append(" left join cloud_securitygroup csg on gr.remote_groupid=csg.sg_id");
		sql.append(" where 1=1 and gr.ethertype='IPv4'");

		if (!"null".equals(dcId) && null != dcId && !"".equals(dcId)
				&& !"undefined".equals(dcId)) {
			sql.append(" and gr.dc_id= ? ");
			list.add(dcId);
		}
		if (!"null".equals(prjId) && null != prjId && !"".equals(prjId)
				&& !"undefined".equals(prjId)) {
			sql.append(" and gr.prj_id= ? ");
			list.add(prjId);
		}
		if (!"null".equals(sgId) && null != sgId && !"".equals(sgId)
				&& !"undefined".equals(sgId)) {
			sql.append(" and gr.sg_id = ? ");
			list.add(sgId);
		}

		sql.append(" order by gr.create_time desc ");

		page = securityGroupRuleDao.pagedNativeQuery(sql.toString(), queryMap,
				list.toArray());
		List listResult = (List) page.getResult();
		for (int i = 0; i < listResult.size(); i++) {
			Object[] objs = (Object[]) listResult.get(i);
			CloudSecurityGroupRule groupRule = new CloudSecurityGroupRule();
			groupRule.setSgrId(String.valueOf(objs[0]));
			groupRule.setPrjId(String.valueOf(objs[1]));
			groupRule.setDcId(String.valueOf(objs[2]));
			groupRule.setSgId(String.valueOf(objs[3]));
			groupRule.setDirection(String.valueOf(objs[4]));
			groupRule.setEthertype(String.valueOf(objs[5]));
			groupRule.setProtocol(String.valueOf(objs[6]));
			groupRule.setPortRangeMin(String.valueOf(objs[7]));
			groupRule.setPortRangeMax(String.valueOf(objs[8]));
			groupRule.setRemoteIpPrefix(String.valueOf(objs[9]));
			groupRule.setRemoteGroupName(null!=objs[10]?objs[10].toString():"");
			groupRule.setProtocolExpand(null!=String.valueOf(objs[11])?String.valueOf(objs[11]):"");
			groupRule.setIcMp(String.valueOf(objs[12]));
			
			listResult.set(i, groupRule);
		}
		return page;
	}

	/**
	 * 根据数据中心ID，项目ID，查询其所有的安全组,无分页的返回List查询
	 * 
	 * @param datacenterId
	 *            ,datacenterId
	 * @return 返回指定安全组列表
	 */
	public List<BaseCloudSecurityGroup> getGroupsByProjectId(
			HttpServletRequest request, @RequestBody Map map) {
		// 从session中获取当前用户名
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession()
				.getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		String cusId = sessionUser.getCusId();
		String dcId = map.get("dcId").toString();
		String prjId = map.get("prjId").toString();

		List<BaseCloudSecurityGroup> listDb = null;
		List<Object> list = new ArrayList<Object>();

		StringBuffer sql = new StringBuffer(
				"select s.sg_id,s.sg_name from  cloud_securitygroup as s left join cloud_project as p  on p.prj_Id=s.prj_Id where 1=1 ");

		if (null != cusId && !"".equals(cusId)) {
			sql.append(" and p.customer_id = ? ");
			list.add(cusId);
		}
		if (null != dcId && !"".equals(dcId)) {
			sql.append(" and s.dc_id = ? ");
			list.add(dcId);
		}
		if (null != prjId && !"".equals(prjId) && !"undefined".equals(prjId)) {
			sql.append(" and s.prj_id = ? ");
			list.add(prjId);
		}

		sql.append(" order by s.create_time desc");
		javax.persistence.Query query = securityGroupDao.createSQLNativeQuery(
				sql.toString(), list.toArray());
		List<BaseCloudSecurityGroup> groupList = new ArrayList<BaseCloudSecurityGroup>();
		List listQuery = query.getResultList();
		for (int i = 0; i < listQuery.size(); i++) {
			Object[] obj = (Object[]) listQuery.get(i);
			BaseCloudSecurityGroup group = new BaseCloudSecurityGroup();
			group.setSgId(String.valueOf(obj[0]));
			group.setSgName(String.valueOf(obj[1]));
			groupList.add(group);
		}

		return groupList;

	}
	
	
	

	public void addDefault3389SecurityGroup(String prjId, String dcId) throws AppException {
		BaseCloudSecurityGroup securityGroup = new BaseCloudSecurityGroup();
		securityGroup.setSgName("Windows安全组放通3389端口");
		securityGroup.setPrjId(prjId);
		securityGroup.setDcId(dcId);
		securityGroup.setCreateTime(new Date());
		securityGroup.setDefaultGroup("defaultGroup");
		securityGroup.setSgDescription("开放Windows远程桌面连接端口");
		
		try{
		// 2、调用底层新增安全组
				JSONObject data = new JSONObject();
				JSONObject temp = new JSONObject();
				temp.put("name", "Windows安全组放通3389端口");
				temp.put("description", "开放Windows远程桌面连接端口");
				data.put("security_group", temp);
				// 创建安全组
				SecurityGroup result = openstackService.create(dcId, prjId, data);
				// 3、保存至数据库
				securityGroup.setSgId(result.getId());
				securityGroupDao.save(securityGroup);
					
					
					String sgid="";

					if (result.getSecurity_group_rules().length > 0) {
						for (int i = 0; i < result.getSecurity_group_rules().length; i++) {
							Rule rules = result.getSecurity_group_rules()[i];
							// 因为创建安全组之后，默认创建出来的规则只有三个字段有值，可以只限定设置保存三个字段；
							BaseCloudSecurityGroupRule baseGroupRule = new BaseCloudSecurityGroupRule();
							baseGroupRule.setSgrId(rules.getId());
							baseGroupRule.setCreateTime(securityGroup.getCreateTime());
							
							baseGroupRule.setDcId(dcId);
							baseGroupRule.setPrjId(result.getTenant_id());
							baseGroupRule.setSgId(result.getId());
							baseGroupRule.setDirection(rules.getDirection());// 方向
							sgid=result.getId();													// 非空
							baseGroupRule.setEthertype(rules.getEthertype());// ipv4、6
																				// 非空

							if (rules.getProtocol() != null && !rules.getProtocol().equals("") ) {
								baseGroupRule.setProtocol(rules.getProtocol());
							}
							securityGroupRuleDao.save(baseGroupRule);
						}
						
						//create3389Ruleegress(dcId,prjId,sgid);
						create3389Ruleingress(dcId, prjId, sgid);
						create3389RuleTcp(dcId, prjId,sgid);
				
					}
		}catch(AppException e){
			throw e;
		}
				
				
		
	}

	
	public void addDefault22SecurityGroup(String prjId, String dcId) throws AppException {
		BaseCloudSecurityGroup securityGroup = new BaseCloudSecurityGroup();
		securityGroup.setSgName("Linux安全组放通22端口");
		securityGroup.setPrjId(prjId);
		securityGroup.setDcId(dcId);
		securityGroup.setCreateTime(new Date());
		securityGroup.setDefaultGroup("defaultGroup");
		securityGroup.setSgDescription("开放SSH远程连接端口");
		
		try{
		// 2、调用底层新增安全组
				JSONObject data = new JSONObject();
				JSONObject temp = new JSONObject();
				temp.put("name", "Linux安全组放通22端口");
				temp.put("description", "开放SSH远程连接端口");
				data.put("security_group", temp);
				// 创建安全组
				SecurityGroup result = openstackService.create(dcId, prjId, data);
				// 3、保存至数据库
				securityGroup.setSgId(result.getId());
				securityGroupDao.save(securityGroup);

					if (result.getSecurity_group_rules().length > 0) {
						for (int i = 0; i < result.getSecurity_group_rules().length; i++) {
							Rule rules = result.getSecurity_group_rules()[i];
							// 因为创建安全组之后，默认创建出来的规则只有三个字段有值，可以只限定设置保存三个字段；
							BaseCloudSecurityGroupRule baseGroupRule = new BaseCloudSecurityGroupRule();
							baseGroupRule.setSgrId(rules.getId());
							baseGroupRule.setCreateTime(securityGroup.getCreateTime());
							
							baseGroupRule.setDcId(dcId);
							baseGroupRule.setPrjId(result.getTenant_id());
							baseGroupRule.setSgId(result.getId());
							baseGroupRule.setDirection(rules.getDirection());// 方向
																				// 非空
							baseGroupRule.setEthertype(rules.getEthertype());// ipv4、6
																				// 非空

							if (rules.getProtocol() != null && !rules.getProtocol().equals("") ) {
								
								baseGroupRule.setProtocol(rules.getProtocol());
							}
							securityGroupRuleDao.save(baseGroupRule);
						}
						
						//create22Ruleegress(dcId,prjId,result.getId());
						create22Ruleingress(dcId, prjId, result.getId());
						create22RuleTcp(dcId, prjId,result.getId());
				
					}
		}catch(AppException e){
			throw e;
		}
			
		
	}
	
	public void create3389Ruleegress(String dcId,String prjId,String sgId) throws AppException {
		// 网络数据
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
			temp.put("direction", "egress");//出口
			temp.put("remote_ip_prefix","0.0.0.0/0");
			temp.put("security_group_id", sgId);
			temp.put("ethertype", "IPv4");

		// 此处需要改进volume 变为 security_group;参考PDF
		data.put("security_group_rule", temp);
		try {
			// 创建安全组规则
			Rule result = openstackSecurityGroupRuleService.createRule(dcId,
					prjId, data);

			if (result != null) {
				BaseCloudSecurityGroupRule rule = new BaseCloudSecurityGroupRule();
				// 从OpenStack差寻到的数据 设置到DB的cloudResource表中，对应在DB中也保存一份；
				rule.setSgrId(result.getId());
				rule.setPrjId(result.getTenant_id());
				rule.setDcId(dcId);
				rule.setCreateTime(new Date());
				// 从session中获取当前用户名
				//rule.setCreateName(EcmcSessionUtil.getUser().getAccount());
				rule.setSgId(result.getSecurity_group_id());
				if (null != result.getRemote_group_id() && !"".equals(result.getRemote_group_id())) {
					rule.setRemoteGroupId(result.getRemote_group_id());
				}
				// 设置方向
				rule.setDirection(result.getDirection());
				// 设置IPv4 或 IPv6
				if (null != result.getEthertype() && !"".equals(result.getEthertype())) {
					rule.setEthertype(result.getEthertype());
				}
				// 设置IP协议
				//if (null != result.getProtocol() && !"".equals(result.getProtocol())) {
					rule.setProtocol("All");
					
				//}
				// 设置最小端口
				if (null != result.getPort_range_min() && !"".equals(result.getPort_range_min())) {
					rule.setPortRangeMin(result.getPort_range_min());
				}
				// 设置最大端口
				if (null != result.getPort_range_max() && !"".equals(result.getPort_range_max())) {
					rule.setPortRangeMax(result.getPort_range_max());
				}
				// 设置远程CIDR
				if (null != result.getRemote_ip_prefix() && !"".equals(result.getRemote_ip_prefix())) {
					rule.setRemoteIpPrefix(result.getRemote_ip_prefix());
				}

				securityGroupRuleDao.save(rule);
			}
			
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			// 出现未知异常时，返回空
			
		}
	}
	
	public void create3389Ruleingress(String dcId,String prjId,String sgid) throws AppException {
		// 网络数据
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
			temp.put("direction", "ingress");//出口
			//temp.put("remote_ip_prefix","0.0.0.0/0");
			//temp.put("security_group_id", sgid);

			// security_group_id代表的是安全组自己 属性的id号；所属的那个项目下的这个安全组的id号；
			temp.put("security_group_id", sgid);
			// 这个属性是指在创建规则时，给这个规则赋予指向哪个安全组的id
			temp.put("remote_group_id", sgid);
			temp.put("ethertype", "IPv4");
		
		// 此处需要改进volume 变为 security_group;参考PDF
		data.put("security_group_rule", temp);
		try {
			// 创建安全组规则
			Rule result = openstackSecurityGroupRuleService.createRule(dcId,
					prjId, data);

			if (result != null) {
				BaseCloudSecurityGroupRule rule = new BaseCloudSecurityGroupRule();
				// 从OpenStack差寻到的数据 设置到DB的cloudResource表中，对应在DB中也保存一份；
				rule.setSgrId(result.getId());
				rule.setPrjId(result.getTenant_id());
				rule.setDcId(dcId);
				rule.setCreateTime(new Date());
				// 从session中获取当前用户名
				//rule.setCreateName(EcmcSessionUtil.getUser().getAccount());
				rule.setSgId(result.getSecurity_group_id());
				if (null != result.getRemote_group_id() && !"".equals(result.getRemote_group_id())) {
					rule.setRemoteGroupId(result.getRemote_group_id());
				}
				// 设置方向
				rule.setDirection(result.getDirection());
				// 设置IPv4 或 IPv6
				if (null != result.getEthertype() && !"".equals(result.getEthertype())) {
					rule.setEthertype(result.getEthertype());
				}
				// 设置IP协议
				//if (null != result.getProtocol() && !"".equals(result.getProtocol())) {
					rule.setProtocol("All");
				//}
				// 设置最小端口
				if (null != result.getPort_range_min() && !"".equals(result.getPort_range_min())) {
					rule.setPortRangeMin(result.getPort_range_min());
				}
				// 设置最大端口
				if (null != result.getPort_range_max() && !"".equals(result.getPort_range_max())) {
					rule.setPortRangeMax(result.getPort_range_max());
				}
				// 设置远程CIDR
				if (null != result.getRemote_ip_prefix() && !"".equals(result.getRemote_ip_prefix())) {
					rule.setRemoteIpPrefix(result.getRemote_ip_prefix());
				}

				securityGroupRuleDao.save(rule);
			}
			
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			// 出现未知异常时，返回空
			
		}
	}
	
	public void create3389RuleTcp(String dcId,String prjId,String sgid) throws AppException {
		// 网络数据
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("protocol", "TCP");
		temp.put("port_range_min", "3389");
		temp.put("port_range_max", "3389");
			temp.put("direction", "ingress");//入口
			temp.put("remote_ip_prefix","0.0.0.0/0");
			temp.put("security_group_id", sgid);
			temp.put("ethertype", "IPv4");
		// 此处需要改进volume 变为 security_group;参考PDF
		data.put("security_group_rule", temp);
		try {
			// 创建安全组规则
			Rule result = openstackSecurityGroupRuleService.createRule(dcId,
					prjId, data);

			if (result != null) {
				BaseCloudSecurityGroupRule rule = new BaseCloudSecurityGroupRule();
				// 从OpenStack差寻到的数据 设置到DB的cloudResource表中，对应在DB中也保存一份；
				rule.setSgrId(result.getId());
				rule.setPrjId(result.getTenant_id());
				rule.setDcId(dcId);
				rule.setCreateTime(new Date());
				// 从session中获取当前用户名
				//rule.setCreateName(EcmcSessionUtil.getUser().getAccount());
				rule.setSgId(result.getSecurity_group_id());
				if (null != result.getRemote_group_id() && !"".equals(result.getRemote_group_id())) {
					rule.setRemoteGroupId(result.getRemote_group_id());
				}
				// 设置方向
				rule.setDirection(result.getDirection());
				// 设置IPv4 或 IPv6
				if (null != result.getEthertype() && !"".equals(result.getEthertype())) {
					rule.setEthertype(result.getEthertype());
				}
				// 设置IP协议
				//if (null != result.getProtocol() && !"".equals(result.getProtocol())) {
					rule.setProtocol("TCP");
				//}
				// 设置最小端口
				if (null != result.getPort_range_min() && !"".equals(result.getPort_range_min())) {
					rule.setPortRangeMin(result.getPort_range_min());
				}
				// 设置最大端口
				if (null != result.getPort_range_max() && !"".equals(result.getPort_range_max())) {
					rule.setPortRangeMax(result.getPort_range_max());
				}
				// 设置远程CIDR
				if (null != result.getRemote_ip_prefix() && !"".equals(result.getRemote_ip_prefix())) {
					rule.setRemoteIpPrefix(result.getRemote_ip_prefix());
				}
				//规则列表页扩展端口范围展示字段
				
					rule.setProtocolExpand("TCP");
				
//				if(null!=params.get("icmp")&& !"".equals(params.get("icmp"))){
//					rule.setIcMp(params.get("icmp"));
//				}
//				if(null!=params.get("protocolExpand")){
					rule.setProtocolExpand("3389(RDP)");
//				}
					securityGroupRuleDao.save(rule);
			}
			
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			// 出现未知异常时，返回空
			
		}
	}
	
	public void create22Ruleegress(String dcId,String prjId,String sgId) throws AppException {
		// 网络数据
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
			temp.put("direction", "egress");//出口
			temp.put("remote_ip_prefix","0.0.0.0/0");
			temp.put("security_group_id", sgId);
			temp.put("ethertype", "IPv4");

		// 此处需要改进volume 变为 security_group;参考PDF
		data.put("security_group_rule", temp);
		try {
			// 创建安全组规则
			Rule result = openstackSecurityGroupRuleService.createRule(dcId,
					prjId, data);

			if (result != null) {
				BaseCloudSecurityGroupRule rule = new BaseCloudSecurityGroupRule();
				// 从OpenStack差寻到的数据 设置到DB的cloudResource表中，对应在DB中也保存一份；
				rule.setSgrId(result.getId());
				rule.setPrjId(result.getTenant_id());
				rule.setDcId(dcId);
				rule.setCreateTime(new Date());
				// 从session中获取当前用户名
				//rule.setCreateName(EcmcSessionUtil.getUser().getAccount());
				rule.setSgId(result.getSecurity_group_id());
				if (null != result.getRemote_group_id() && !"".equals(result.getRemote_group_id())) {
					rule.setRemoteGroupId(result.getRemote_group_id());
				}
				// 设置方向
				rule.setDirection(result.getDirection());
				// 设置IPv4 或 IPv6
				if (null != result.getEthertype() && !"".equals(result.getEthertype())) {
					rule.setEthertype(result.getEthertype());
				}
				// 设置IP协议
				if (null != result.getProtocol() && !"".equals(result.getProtocol())) {
					rule.setProtocol(result.getProtocol());
				}
				// 设置最小端口
				if (null != result.getPort_range_min() && !"".equals(result.getPort_range_min())) {
					rule.setPortRangeMin(result.getPort_range_min());
				}
				// 设置最大端口
				if (null != result.getPort_range_max() && !"".equals(result.getPort_range_max())) {
					rule.setPortRangeMax(result.getPort_range_max());
				}
				// 设置远程CIDR
				if (null != result.getRemote_ip_prefix() && !"".equals(result.getRemote_ip_prefix())) {
					rule.setRemoteIpPrefix(result.getRemote_ip_prefix());
				}

				securityGroupRuleDao.save(rule);
			}
			
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			// 出现未知异常时，返回空
			
		}
	}
	
	public void create22Ruleingress(String dcId,String prjId,String sgid) throws AppException {
		// 网络数据
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
			temp.put("direction", "ingress");//出口
			//temp.put("remote_ip_prefix","0.0.0.0/0");
			//temp.put("security_group_id", sgid);

			// security_group_id代表的是安全组自己 属性的id号；所属的那个项目下的这个安全组的id号；
			temp.put("security_group_id", sgid);
			// 这个属性是指在创建规则时，给这个规则赋予指向哪个安全组的id
			temp.put("remote_group_id", sgid);
			temp.put("ethertype", "IPv4");
		
		// 此处需要改进volume 变为 security_group;参考PDF
		data.put("security_group_rule", temp);
		try {
			// 创建安全组规则
			Rule result = openstackSecurityGroupRuleService.createRule(dcId,
					prjId, data);

			if (result != null) {
				BaseCloudSecurityGroupRule rule = new BaseCloudSecurityGroupRule();
				// 从OpenStack差寻到的数据 设置到DB的cloudResource表中，对应在DB中也保存一份；
				rule.setSgrId(result.getId());
				rule.setPrjId(result.getTenant_id());
				rule.setDcId(dcId);
				rule.setCreateTime(new Date());
				// 从session中获取当前用户名
				//rule.setCreateName(EcmcSessionUtil.getUser().getAccount());
				rule.setSgId(result.getSecurity_group_id());
				if (null != result.getRemote_group_id() && !"".equals(result.getRemote_group_id())) {
					rule.setRemoteGroupId(result.getRemote_group_id());
				}
				// 设置方向
				rule.setDirection(result.getDirection());
				// 设置IPv4 或 IPv6
				if (null != result.getEthertype() && !"".equals(result.getEthertype())) {
					rule.setEthertype(result.getEthertype());
				}
				// 设置IP协议
				//if (null != result.getProtocol() && !"".equals(result.getProtocol())) {
					rule.setProtocol("All");
				//}
				// 设置最小端口
				if (null != result.getPort_range_min() && !"".equals(result.getPort_range_min())) {
					rule.setPortRangeMin(result.getPort_range_min());
				}
				// 设置最大端口
				if (null != result.getPort_range_max() && !"".equals(result.getPort_range_max())) {
					rule.setPortRangeMax(result.getPort_range_max());
				}
				// 设置远程CIDR
				if (null != result.getRemote_ip_prefix() && !"".equals(result.getRemote_ip_prefix())) {
					rule.setRemoteIpPrefix(result.getRemote_ip_prefix());
				}

				securityGroupRuleDao.save(rule);
			}
			
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			// 出现未知异常时，返回空
			
		}
	}
	
	public void create22RuleTcp(String dcId,String prjId,String sgid) throws AppException {
		// 网络数据
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("protocol", "TCP");
		temp.put("port_range_min", "22");
		temp.put("port_range_max", "22");
			temp.put("direction", "ingress");//出口
			temp.put("remote_ip_prefix","0.0.0.0/0");
			temp.put("security_group_id", sgid);
			temp.put("ethertype", "IPv4");
		// 此处需要改进volume 变为 security_group;参考PDF
		data.put("security_group_rule", temp);
		try {
			// 创建安全组规则
			Rule result = openstackSecurityGroupRuleService.createRule(dcId,
					prjId, data);

			if (result != null) {
				BaseCloudSecurityGroupRule rule = new BaseCloudSecurityGroupRule();
				// 从OpenStack差寻到的数据 设置到DB的cloudResource表中，对应在DB中也保存一份；
				rule.setSgrId(result.getId());
				rule.setPrjId(result.getTenant_id());
				rule.setDcId(dcId);
				rule.setCreateTime(new Date());
				// 从session中获取当前用户名
				//rule.setCreateName(EcmcSessionUtil.getUser().getAccount());
				rule.setSgId(result.getSecurity_group_id());
				if (null != result.getRemote_group_id() && !"".equals(result.getRemote_group_id())) {
					rule.setRemoteGroupId(result.getRemote_group_id());
				}
				// 设置方向
				rule.setDirection(result.getDirection());
				// 设置IPv4 或 IPv6
				if (null != result.getEthertype() && !"".equals(result.getEthertype())) {
					rule.setEthertype(result.getEthertype());
				}
				// 设置IP协议
				//if (null != result.getProtocol() && !"".equals(result.getProtocol())) {
					rule.setProtocol("TCP");
				//}
				// 设置最小端口
				if (null != result.getPort_range_min() && !"".equals(result.getPort_range_min())) {
					rule.setPortRangeMin(result.getPort_range_min());
				}
				// 设置最大端口
				if (null != result.getPort_range_max() && !"".equals(result.getPort_range_max())) {
					rule.setPortRangeMax(result.getPort_range_max());
				}
				// 设置远程CIDR
				if (null != result.getRemote_ip_prefix() && !"".equals(result.getRemote_ip_prefix())) {
					rule.setRemoteIpPrefix(result.getRemote_ip_prefix());
				}
				//规则列表页扩展端口范围展示字段
				
					rule.setProtocolExpand("TCP");
				
//				if(null!=params.get("icmp")&& !"".equals(params.get("icmp"))){
//					rule.setIcMp(params.get("icmp"));
//				}
//				if(null!=params.get("protocolExpand")){
					rule.setProtocolExpand("22(SSH)");
//				}
					securityGroupRuleDao.save(rule);
			}
			
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			// 出现未知异常时，返回空
			
		}
	}
	
	private String escapeSpecialChar(String str) {
		if (StringUtils.isNotBlank(str)) {
			String[] specialChars = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|", "%" };
			for (String key : specialChars) {
				if (str.contains(key)) {
					str = str.replace(key, "/" + key);
				}
			}
		}
		return str;
	}
	

}