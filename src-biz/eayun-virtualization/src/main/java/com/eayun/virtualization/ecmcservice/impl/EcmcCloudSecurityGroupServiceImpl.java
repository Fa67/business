package com.eayun.virtualization.ecmcservice.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.datacenter.ecmcservice.EcmcDataCenterService;
import com.eayun.datacenter.model.DcDataCenter;
import com.eayun.eayunstack.model.Rule;
import com.eayun.eayunstack.model.SecurityGroup;
import com.eayun.eayunstack.model.Tenant;
import com.eayun.eayunstack.service.OpenstackSecurityGroupService;
import com.eayun.eayunstack.service.OpenstackTenantService;
import com.eayun.project.ecmcservice.EcmcProjectService;
import com.eayun.virtualization.dao.CloudSecurityGroupDao;
import com.eayun.virtualization.dao.CloudSecurityGroupRuleDao;
import com.eayun.virtualization.ecmcservice.EcmcCloudSecurityGroupService;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.BaseCloudSecurityGroup;
import com.eayun.virtualization.model.BaseCloudSecurityGroupRule;
import com.eayun.virtualization.model.BaseCloudVmSgroup;
import com.eayun.virtualization.model.CloudSecurityGroup;
import com.eayun.virtualization.model.CloudSecurityGroupRule;
import com.eayun.virtualization.service.SecurityGroupService;
import com.eayun.virtualization.service.TagService;

@Service
@Transactional
public class EcmcCloudSecurityGroupServiceImpl implements EcmcCloudSecurityGroupService {

	private final static Logger log = LoggerFactory.getLogger(EcmcCloudSecurityGroupServiceImpl.class);

	@Autowired
	private CloudSecurityGroupDao cloudSecurityGroupDao;

	@Autowired
	private CloudSecurityGroupRuleDao cloudSecurityGroupRuleDao;

	@Autowired
	private TagService tagService;

	@Autowired
	private OpenstackSecurityGroupService openstackSecurityGroupService;
	
	@Autowired
	private OpenstackTenantService openstackTenantService;
	
	@Autowired
	private EcmcProjectService EcmcProjectService;
	@Autowired
	private SecurityGroupService securityGroupService;
	@Autowired
	private EcmcDataCenterService ecmcDataCenterService;

	@Override
	public BaseCloudSecurityGroup updateToDB(BaseCloudSecurityGroup baseCloudSecurityGroup) {
		return cloudSecurityGroupDao.save(baseCloudSecurityGroup);
	}

	public boolean checkSecurityGroupName(String dcId, String prjId, String sgName, String sgId) throws AppException {
		return cloudSecurityGroupDao.countBySecurityGroupName(dcId, prjId, StringUtils.trim(sgName), sgId) > 0 ? true
				: false;
	}

	public BaseCloudSecurityGroup addSecurityGroup(String dcId, String prjId, String name, String description, String ceateName)
			throws AppException {
		// 此处根据数据中心id,项目id，查找其包含的所有的安全组
		// 1、判断安全组名称是否重复
		boolean isExist = this.checkSecurityGroupName(dcId, prjId, name, null);
		if (isExist == true) {
			throw new AppException("该安全组名称在当前数据中心中已存在！");
		}

		// 2、调用底层新增安全组
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("name", name);
		temp.put("description", description);
		data.put("security_group", temp);
		BaseCloudSecurityGroup groupEntity = null;
		try {
			// 创建安全组
			SecurityGroup result = openstackSecurityGroupService.create(dcId, prjId, data);
			// 3、保存至数据库
			if (result != null) {
				groupEntity = new BaseCloudSecurityGroup();
				groupEntity.setSgId(result.getId());
				groupEntity.setSgName(result.getName());
				// 从session中获取当前用户名
				groupEntity.setCreateName(ceateName);
				groupEntity.setCreateTime(new Date());
				groupEntity.setPrjId(result.getTenant_id());
				groupEntity.setDcId(dcId);
				groupEntity.setSgDescription(result.getDescription());
				groupEntity.setDefaultGroup("not");
				cloudSecurityGroupDao.save(groupEntity);
				
				
			
				if (result.getSecurity_group_rules().length > 0) {
					for (int i = 0; i < result.getSecurity_group_rules().length; i++) {
						Rule rules = result.getSecurity_group_rules()[i];
						// 因为创建安全组之后，默认创建出来的规则只有三个字段有值，可以只限定设置保存三个字段；
						BaseCloudSecurityGroupRule baseGroupRule = new BaseCloudSecurityGroupRule();
						baseGroupRule.setSgrId(rules.getId());
						baseGroupRule.setCreateTime(groupEntity.getCreateTime());
						baseGroupRule.setCreateName(ceateName);
						baseGroupRule.setDcId(dcId);
						baseGroupRule.setPrjId(result.getTenant_id());
						baseGroupRule.setSgId(result.getId());
						baseGroupRule.setDirection(rules.getDirection());// 方向
																			// 非空
						baseGroupRule.setEthertype(rules.getEthertype());// ipv4、6
																			// 非空
						//baseGroupRule.setIcMp("--");
						if (rules.getProtocol() != null && !rules.getProtocol().equals("")) {
							baseGroupRule.setProtocol(rules.getProtocol());
						}
						cloudSecurityGroupRuleDao.save(baseGroupRule);
					}
				}
			}
			return groupEntity;
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			// 出现未知异常时，返回空
			return null;
		}
	}

	@Override
	public int getCountByPrjId(String prjId) {
		return cloudSecurityGroupDao.getCountByPrjId(prjId);
	}

	@Override
	public List<BaseCloudSecurityGroup> getByPrjId(String prjId) {
		return cloudSecurityGroupDao.findByPrjId(prjId);
	}

	public Page getSecurityGroupList(String datacenterId, String prjName, String cusOrg, String name, QueryMap queryMap)
			throws AppException {
		try {
			int index = 0;
			Object[] args = new Object[4];
			StringBuffer sql = new StringBuffer();
			sql.append("select csg.sg_id as sgId,csg.dc_id as dcId,csg.prj_id as prjId,");
			sql.append(" csg.sg_name as sgName,csg.sg_description as sgDescription,cp.prj_name as prjName,csg.default_group as defaultGroup,csg.create_time as createTime");
			sql.append(" ,dc.dc_name as dcName, ss.cus_id as cusId, ss.cus_name as cusName,ss.cus_org as cusOrg ");
			sql.append(" from cloud_securitygroup csg");
			sql.append(" left join cloud_project cp on csg.prj_id=cp.prj_id");
			sql.append(" left join sys_selfcustomer ss on cp.customer_id=ss.cus_id");
			sql.append(" left join dc_datacenter dc on csg.dc_id = dc.id");
			sql.append(" where 1=1");

			if (!"null".equals(prjName) && null != prjName && !"".equals(prjName)
					&& !"undefined".equals(prjName)) {
				sql.append(" and cp.prj_name in(?").append(index+1).append(") ");
				args[index] = Arrays.asList(StringUtils.split(prjName, ","));
				index++;
			}
			if (!"null".equals(datacenterId) && null != datacenterId && !"".equals(datacenterId)
					&& !"undefined".equals(datacenterId)) {
				sql.append(" and csg.dc_id= ?").append(index+1);
				args[index] = datacenterId;
				index++;
			}
			if (!"null".equals(cusOrg) && null != cusOrg && !"".equals(cusOrg)
					&& !"undefined".equals(cusOrg)) {
				sql.append(" and ss.cus_org in (?").append(index+1).append(") ");
				args[index] = Arrays.asList(StringUtils.split(cusOrg, ","));
				index++;
			}
			if (!"null".equals(name) && null != name && !"".equals(name) && !"undefined".equals(name)) {
				sql.append(" and  binary (case csg.sg_name  when  binary 'default' then '默认安全组' else csg.sg_name end) like ?").append(index+1).append(" ");
				//name = name.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
				args[index] = "%" + escapeSpecialChar(name) + "%";
				index++;
			}

			sql.append(" order by IF (ISNULL(csg.create_time),1,0), csg.create_time desc ");
			Object[] params = new Object[index];
			System.arraycopy(args, 0, params, 0, index);

			Page page = cloudSecurityGroupDao.pagedNativeQuery(sql.toString(), queryMap, params);
			@SuppressWarnings("unchecked")
			List<Object> listResult = (List<Object>) page.getResult();
			for (int i = 0; i < listResult.size(); i++) {
				Object[] objs = (Object[]) listResult.get(i);
				CloudSecurityGroup group = new CloudSecurityGroup();
				group.setSgId(ObjectUtils.toString(objs[0]));
				group.setDcId(ObjectUtils.toString(objs[1]));
				group.setPrjId(ObjectUtils.toString(objs[2]));
				group.setSgName(ObjectUtils.toString(objs[3]));
				group.setSgDescription(ObjectUtils.toString(objs[4]));
				group.setPrjName(ObjectUtils.toString(objs[5]));
				group.setDefaultGroup(ObjectUtils.toString(objs[6]));
				group.setCreateTime(DateUtil.stringToDate(objs[7] == null ? "" : ObjectUtils.toString(objs[7])));
				group.setDcName(ObjectUtils.toString(objs[8]));
				group.setCusId(ObjectUtils.toString(objs[9]));
				group.setCusName(ObjectUtils.toString(objs[10]));
				group.setCusOrg(ObjectUtils.toString(objs[11]));
				group.setIsDeleting("not");
				listResult.set(i, group);
			}
			return page;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public List<CloudSecurityGroup> listAllGroups(String datacenterId, String projectId) throws AppException {
		try {
			int index = 0;
			Object[] args = new Object[3];
			StringBuffer sql = new StringBuffer();
			sql.append("select csg.sg_id as sgId,csg.dc_id as dcId,csg.prj_id as prjId,");
			sql.append(" csg.sg_name as sgName,csg.sg_description as sgDescription,cp.prj_name as prjName,csg.default_group as defaultGroup,csg.create_time as createTime ");
			sql.append(" from cloud_securitygroup csg");
			sql.append(" left join cloud_project cp on csg.prj_id=cp.prj_id");
			sql.append(" where 1=1");
			if (!"null".equals(projectId) && null != projectId && !"".equals(projectId)
					&& !"undefined".equals(projectId)) {
				sql.append(" and csg.prj_id= ? ");
				args[index] = projectId;
				index++;
			}

			if (!"null".equals(datacenterId) && null != datacenterId && !"".equals(datacenterId)
					&& !"undefined".equals(datacenterId)) {
				sql.append(" and csg.dc_id= ? ");
				args[index] = datacenterId;
				index++;
			}

			sql.append(" order by IF (ISNULL(csg.create_time),1,0), csg.create_time desc ");
			Object[] params = new Object[index];
			System.arraycopy(args, 0, params, 0, index);
			@SuppressWarnings("unchecked")
			List<Object[]> queryList = cloudSecurityGroupDao.createSQLNativeQuery(sql.toString(), params)
					.getResultList();
			List<CloudSecurityGroup> resultList = new ArrayList<CloudSecurityGroup>();
			for (Object[] objs : queryList) {
				CloudSecurityGroup group = new CloudSecurityGroup();
				group.setSgId(ObjectUtils.toString(objs[0]));
				group.setDcId(ObjectUtils.toString(objs[1]));
				group.setPrjId(ObjectUtils.toString(objs[2]));
				group.setSgName(ObjectUtils.toString(objs[3]));
				group.setSgDescription(ObjectUtils.toString(objs[4]));
				group.setPrjName(ObjectUtils.toString(objs[5]));
				group.setDefaultGroup(ObjectUtils.toString(objs[6]));
				group.setCreateTime(
						DateUtil.stringToDate(objs[7] == null ? "" : ObjectUtils.toString(objs[7])));
				group.setIsDeleting("not");
				resultList.add(group);
			}
			return resultList;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public boolean deleteSecurityGroup(String datacenterId, String id) throws AppException {
		if (openstackSecurityGroupService.delete(datacenterId, null, id)) {
			
			cloudSecurityGroupDao.delete(id);
			cloudSecurityGroupRuleDao.deleteBySgId(id);
			cloudSecurityGroupRuleDao.deletedsgrule(id);
			// 删除资源后更新缓存接口
			tagService.refreshCacheAftDelRes("securityGroup", id);
			return true;
		}
		return false;
	}

	public boolean updateSecurityGroup(CloudSecurityGroup cloudSecurityGroup) throws AppException {
		try {
			// 如果存在现有的安全组，返回null，提示已存在这个安全组；
			// 通过datacenterId，projectId，查找对应所有的安全组；

			// 1、判断镜像名称是否重复
			boolean isExist = this.checkSecurityGroupName(cloudSecurityGroup.getDcId(), cloudSecurityGroup.getPrjId(),
					cloudSecurityGroup.getSgName(), cloudSecurityGroup.getSgId());
			if (isExist == true) {
				throw new AppException("该安全组名称在当前数据中心中已存在！");// 需要可以改为在当前项目下已存在
			}
			// 2、将当前编辑的数据提交到底层更新
			JSONObject data = new JSONObject();
			JSONObject temp = new JSONObject();
			temp.put("name", cloudSecurityGroup.getSgName());
			temp.put("description", cloudSecurityGroup.getSgDescription());
			data.put("security_group", temp);
			// 执行openstack修改操作
			SecurityGroup securityGroup = openstackSecurityGroupService.update(cloudSecurityGroup.getDcId(),
					cloudSecurityGroup.getPrjId(), data, cloudSecurityGroup.getSgId());
			// 3、底层更新成功后，再更新数据库
			if (securityGroup != null) {
				// 从数据库中查询该id的实体
				BaseCloudSecurityGroup baseSecurityGroup = cloudSecurityGroupDao.findOne(cloudSecurityGroup.getSgId());
				// 数据库中不存在是，新建一个实体对象
				if (baseSecurityGroup != null) {
					baseSecurityGroup.setSgName(cloudSecurityGroup.getSgName());
					baseSecurityGroup.setSgDescription(cloudSecurityGroup.getSgDescription());
					// 数据库保存操作
					cloudSecurityGroupDao.saveOrUpdate(baseSecurityGroup);
				}
				return true;
			} else {
				return false;
			}
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}
	
	public Map<String, Object> getBaseCloudSecurityGroupById(String sgId) throws AppException{
		return cloudSecurityGroupDao.findTop1BySgId(sgId);
	}
	
	public Page getSecurityGroupRulesBySgId(String datacenterId,String projectId,String groupId, QueryMap queryMap) throws AppException {
		int index=0;
		Object [] args=new Object[3];
		StringBuffer sql=new StringBuffer();
		sql.append("select gr.sgr_id as sgrId,gr.prj_id as prjId,gr.dc_id as dcId,gr.sg_id as sgId,");
		sql.append(" gr.direction as direction,gr.ethertype as ethertype,gr.protocol as protocol,");
		sql.append(" gr.port_rangemin as portRangeMin,gr.port_rangemax as portRangeMax,gr.remote_ipprefix as remoteIpPrefix,csg.sg_name as sgName");
		sql.append(" ,gr.protocol_expand as protocolExpand ,gr.icmp as icmp");
		sql.append(" from cloud_grouprule gr");
		sql.append(" left join cloud_securitygroup csg on gr.remote_groupid=csg.sg_id");
		sql.append(" where 1=1 and gr.ethertype != 'IPv6' ");
		
		if (!"null".equals(projectId)&&null!=projectId&&!"".equals(projectId)&&!"undefined".equals(projectId)) {
			sql.append(" and gr.prj_id= ? ");
			args[index]=projectId;
			index++;
		}
		if (!"null".equals(datacenterId)&&null!=datacenterId&&!"".equals(datacenterId)&&!"undefined".equals(datacenterId)) {
			sql.append(" and gr.dc_id= ? ");
			args[index]=datacenterId;
			index++;
		}
		 if(!"null".equals(groupId)&&null!=groupId&&!"".equals(groupId)&&!"undefined".equals(groupId)){
		    	sql.append(" and gr.sg_id = ? ");
		    	args[index]=groupId;
				index++;
		    }
		
		sql.append(" order by gr.create_time desc ");
		Object[] params = new Object[index]; 
		System.arraycopy(args, 0, params, 0, index);
		Page page = cloudSecurityGroupRuleDao.pagedNativeQuery(sql.toString(), queryMap, params);
		@SuppressWarnings("unchecked")
		List<Object[]> queryData = (List<Object[]>)page.getResult();
		List<CloudSecurityGroupRule> resultList = new ArrayList<CloudSecurityGroupRule>();
		for (Object[] objs : queryData) {
			CloudSecurityGroupRule baseSecurityGroupRule = new CloudSecurityGroupRule();
			baseSecurityGroupRule.setSgrId(ObjectUtils.toString(objs[0]));
			baseSecurityGroupRule.setPrjId(ObjectUtils.toString(objs[1]));
			baseSecurityGroupRule.setDcId(ObjectUtils.toString(objs[2]));
			baseSecurityGroupRule.setSgId(ObjectUtils.toString(objs[3]));
			baseSecurityGroupRule.setDirection(ObjectUtils.toString(objs[4]));
			baseSecurityGroupRule.setEthertype(ObjectUtils.toString(objs[5]));
			baseSecurityGroupRule.setProtocol(ObjectUtils.toString(objs[6]));
			baseSecurityGroupRule.setPortRangeMin(ObjectUtils.toString(objs[7]));
			baseSecurityGroupRule.setPortRangeMax(ObjectUtils.toString(objs[8]));
			baseSecurityGroupRule.setRemoteIpPrefix(ObjectUtils.toString(objs[9]));
			baseSecurityGroupRule.setRemoteGroupName(ObjectUtils.toString(objs[10]));
			baseSecurityGroupRule.setProtocolExpand(null!=ObjectUtils.toString(objs[11])?ObjectUtils.toString(objs[11]):"");
			baseSecurityGroupRule.setIcMp(ObjectUtils.toString(objs[12]));
			resultList.add(baseSecurityGroupRule);
		}
		page.setResult(resultList);
		return page;
	}

	@Override
	public BaseCloudSecurityGroup getGroupBySgId(String sgId) {
		return cloudSecurityGroupDao.getGroupBySgId(sgId);
	}

	@Override
	public List<BaseCloudVmSgroup> getVmByPrjId(String sgid) {
		return cloudSecurityGroupDao.getVmBysgId(sgid);
		
	}

	@Override
	@Transactional(propagation=Propagation.NEVER)
	public Object updateEcscSecurityGroup() {
		
		List<Map> upprjlist = new ArrayList<Map>();
		Map<String, Object> mapup = new HashMap<String, Object>();
		mapup.put("开始执行修改默认安全组", "修改默认安全组描述开始");
		upprjlist.add(mapup);
	
		
	
		List<Tenant> tenantlist = new ArrayList<Tenant>();
		List<DcDataCenter> dclist = ecmcDataCenterService.getAllList();
		try {
			for (int d = 0; d < dclist.size(); d++) {
				List<Tenant> ten = openstackTenantService.listAll(dclist.get(d).getId());
				tenantlist.addAll(ten);
			}
		} catch (AppException e) {

		}

		List<BaseCloudProject> prjlist = cloudSecurityGroupDao.getListProject();// 获取所有项目

		List<BaseCloudSecurityGroup> sglist = cloudSecurityGroupDao.getsgList();// 获取所有默认安全组
		
		 upprjlist.addAll(this.updefultsg(sglist)) ;
		 mapup.put("结束执行修改默认安全组", "修改默认安全组描述结束");
		 upprjlist.add(mapup);
		
		
		
		

		for (int i = 0; i < prjlist.size(); i++) {

			Map<String, Object> map = new HashMap<String, Object>();
			BaseCloudProject prj = prjlist.get(i);
			int count = 0;
			boolean fag = false;
			for (int j = 0; j < sglist.size(); j++) {
				
				if ((prj.getProjectId()).equals(sglist.get(j).getPrjId())) {// 判断默认安全数量
					//if(sglist.get(j).getSgName().equals("default")&&sglist.get(j).getDefaultGroup().equals("defaultGroup")){
						count++;
					//}
					
				}
				
				
					
				}

			for (int t = 0; t < tenantlist.size(); t++) {
				if (prj.getProjectId().equals(tenantlist.get(t).getId())) {
					fag = true;
				}
			}
			if (count < 3 && fag) {
				
				
				
				
				int sg = prj.getSafeGroup();
				prj.setSafeGroup(sg + 2);// 修改项目安全组
				try {
					BaseCloudProject upsg = EcmcProjectService.updateProject(prj);// 修改配额
					System.out.println("开始安全组");
					securityGroupService.addDefault22SecurityGroup(upsg.getProjectId(), upsg.getDcId());
					securityGroupService.addDefault3389SecurityGroup(upsg.getProjectId(), upsg.getDcId());
					map.put("项目ID：", prj.getProjectId());
					map.put("项目名称：", prj.getPrjName());
					map.put("数据中心ID", prj.getDcId());
					map.put("执行情况", "默认安全组添加成功");
				} catch (Exception e) {
					map.put("项目ID：", prj.getProjectId());
					map.put("项目名称：", prj.getPrjName());
					map.put("数据中心ID", prj.getDcId());
					map.put("执行情况", "默认安全组添加失败");
					log.error("", e);

				}
			}

			if (!fag) {
				map.put("项目ID：", prj.getProjectId());
				map.put("项目名称：", prj.getPrjName());
				map.put("数据中心ID", prj.getDcId());
				map.put("执行情况", "底层不存在项目，但数据库存在项目");
			}

			if(map.size()!=0){
				upprjlist.add(map);
			}
			

		}

		return upprjlist;
	}
	
	
	private List updefultsg(List<BaseCloudSecurityGroup> sglist){
		List<Map> upprjlist = new ArrayList<Map>();
		for(int i=0;i<sglist.size();i++){
			
			if(sglist.get(i).getSgName().equals("default")&&sglist.get(i).getDefaultGroup().equals("defaultGroup")){
			Map<String, Object> map = new HashMap<String, Object>();
			CloudSecurityGroup cloudSecurityGroup	=new CloudSecurityGroup();
			//cloudSecurityGroup.setSgId("2972bbdf-3d6f-4edf-b07a-e04242cf9b51");
			BeanUtils.copyPropertiesByModel(cloudSecurityGroup, sglist.get(i));
			cloudSecurityGroup.setSgDescription("出方向允许所有出站流量；入方向仅允许来自其他与默认安全组相关联的云主机的入站流量。");
			try{
			updateSecurityGroup1(cloudSecurityGroup);
			map.put("项目ID：", cloudSecurityGroup.getPrjId());
			map.put("项目名称：",cloudSecurityGroup.getPrjName());
			map.put("数据中心ID", cloudSecurityGroup.getDcId());
			map.put("执行情况", "成功");
			map.put("安全组名称", cloudSecurityGroup.getSgName());
			map.put("安全组ID", cloudSecurityGroup.getSgId());
			
			upprjlist.add(map);
			}catch(Exception e){
				map.put("项目ID：", cloudSecurityGroup.getPrjId());
				map.put("项目名称：",cloudSecurityGroup.getPrjName());
				map.put("数据中心ID", cloudSecurityGroup.getDcId());
				map.put("执行情况", "失败");
				map.put("安全组名称", cloudSecurityGroup.getSgName());
				map.put("安全组ID", cloudSecurityGroup.getSgId());
				log.error("", e);
				upprjlist.add(map);
			}
			
		}
		}
		return upprjlist;
		
	}
	
	
	public boolean updateSecurityGroup1(CloudSecurityGroup cloudSecurityGroup) throws AppException {
		try {
			// 如果存在现有的安全组，返回null，提示已存在这个安全组；
			// 通过datacenterId，projectId，查找对应所有的安全组；

			// 1、判断镜像名称是否重复
			boolean isExist = this.checkSecurityGroupName(cloudSecurityGroup.getDcId(), cloudSecurityGroup.getPrjId(),
					cloudSecurityGroup.getSgName(), cloudSecurityGroup.getSgId());
			if (isExist == true) {
				throw new AppException("该安全组名称在当前数据中心中已存在！");// 需要可以改为在当前项目下已存在
			}
			// 2、将当前编辑的数据提交到底层更新
			JSONObject data = new JSONObject();
			JSONObject temp = new JSONObject();
			//temp.put("name", cloudSecurityGroup.getSgName());
			temp.put("description", cloudSecurityGroup.getSgDescription());
			data.put("security_group", temp);
			// 执行openstack修改操作
			SecurityGroup securityGroup = openstackSecurityGroupService.update(cloudSecurityGroup.getDcId(),
					cloudSecurityGroup.getPrjId(), data, cloudSecurityGroup.getSgId());
			// 3、底层更新成功后，再更新数据库
			if (securityGroup != null) {
				// 从数据库中查询该id的实体
				BaseCloudSecurityGroup baseSecurityGroup = cloudSecurityGroupDao.findOne(cloudSecurityGroup.getSgId());
				// 数据库中不存在是，新建一个实体对象
				if (baseSecurityGroup != null) {
					baseSecurityGroup.setSgName(cloudSecurityGroup.getSgName());
					baseSecurityGroup.setSgDescription(cloudSecurityGroup.getSgDescription());
					// 数据库保存操作
					cloudSecurityGroupDao.saveOrUpdate(baseSecurityGroup);
				}
				return true;
			} else {
				return false;
			}
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
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
