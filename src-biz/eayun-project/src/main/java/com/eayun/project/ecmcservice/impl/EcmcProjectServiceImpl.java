package com.eayun.project.ecmcservice.impl;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.tools.SeqTool;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.customer.ecmcservice.EcmcCustomerService;
import com.eayun.customer.model.Customer;
import com.eayun.database.instance.service.EcmcCloudRDSInstanceService;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.datacenter.service.DataCenterService;
import com.eayun.eayunstack.model.MeteringLabel;
import com.eayun.eayunstack.model.Rule;
import com.eayun.eayunstack.model.SecurityGroup;
import com.eayun.eayunstack.model.Tenant;
import com.eayun.eayunstack.service.OpenstackMeterLabelService;
import com.eayun.eayunstack.service.OpenstackSecurityGroupService;
import com.eayun.eayunstack.service.OpenstackTenantService;
import com.eayun.invoice.service.InvoiceService;
import com.eayun.project.dao.CloudProjectDao;
import com.eayun.project.ecmcservice.EcmcProjectService;
import com.eayun.virtualization.ecmcservice.EcmcCloudFireWallPoliyService;
import com.eayun.virtualization.ecmcservice.EcmcCloudFloatIPService;
import com.eayun.virtualization.ecmcservice.EcmcCloudFwRuleService;
import com.eayun.virtualization.ecmcservice.EcmcCloudImageService;
import com.eayun.virtualization.ecmcservice.EcmcCloudSecurityGroupRuleService;
import com.eayun.virtualization.ecmcservice.EcmcCloudSecurityGroupService;
import com.eayun.virtualization.ecmcservice.EcmcCloudSnapshotService;
import com.eayun.virtualization.ecmcservice.EcmcCloudVmService;
import com.eayun.virtualization.ecmcservice.EcmcCloudVolumeService;
import com.eayun.virtualization.ecmcservice.EcmcLBHealthMonitorService;
import com.eayun.virtualization.ecmcservice.EcmcLBPoolService;
import com.eayun.virtualization.ecmcservice.EcmcNetworkService;
import com.eayun.virtualization.ecmcservice.EcmcPortMappingService;
import com.eayun.virtualization.ecmcservice.EcmcRouteService;
import com.eayun.virtualization.ecmcservice.EcmcSubNetworkService;
import com.eayun.virtualization.ecmcservice.EcmcVpnService;
import com.eayun.virtualization.model.BaseCloudFireWall;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.BaseCloudSecurityGroup;
import com.eayun.virtualization.model.BaseCloudSecurityGroupRule;
import com.eayun.virtualization.model.CloudFwPolicy;
import com.eayun.virtualization.model.CloudFwRule;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.service.CloudSecretKeyService;
import com.eayun.virtualization.service.SecurityGroupService;

@Service
@Transactional
public class EcmcProjectServiceImpl implements EcmcProjectService {

	private static final Logger log = LoggerFactory.getLogger(EcmcProjectServiceImpl.class);
	 
	@Autowired
	private DataCenterService dataCenterService;
	@Autowired
	private OpenstackTenantService openstackTenantService;
	@Autowired
	private EcmcCustomerService ecmcCustomerService;
	@Autowired
	private OpenstackMeterLabelService openstackMeterLabelService;
	@Autowired
	private CloudProjectDao cloudProjectDao;
	@Autowired
	private OpenstackSecurityGroupService openstackSecurityGroupService;
	@Autowired
	private EcmcCloudSecurityGroupService ecmcCloudSecurityGroupService;
	@Autowired
	private EcmcCloudSecurityGroupRuleService ecmcCloudSecurityGroupRuleService;
	@Autowired
	private EcmcRouteService ecmcRouteService;
	@Autowired
	private EcmcCloudSnapshotService ecmcCloudSnapshotService;
	@Autowired
	private EcmcCloudVolumeService ecmcCloudVolumeService;
	@Autowired
	private EcmcNetworkService ecmcNetworkService;
	@Autowired
	private EcmcSubNetworkService ecmcSubNetworkService;
	@Autowired
	private EcmcCloudFloatIPService ecmcCloudFloatIPService;
	@Autowired
	private EcmcLBPoolService ecmcLBPoolService;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private EcmcCloudVmService ecmcCloudVmService;
	@Autowired
	private EcmcCloudImageService ecmcCloudImageService;
	@Autowired
	private EcmcLBHealthMonitorService ecmcLBHealthMonitorService;
	@Autowired
	private EcmcCloudFwRuleService ecmcCloudFwRuleService;
	@Autowired
	private EcmcCloudFireWallPoliyService ecmcCloudFireWallPoliyService;
	@Autowired
	private EcmcVpnService ecmcVpnService;
	@Autowired
	private EcmcPortMappingService ecmcPortMappingService;
	@Autowired
	private SecurityGroupService securityGroupService;
	@Autowired
	private EcmcCloudRDSInstanceService ecmcCloudRDSInstanceService;
	@Autowired
	private CloudSecretKeyService cloudSecretKeyService;
	@Autowired
    private InvoiceService invoiceService;
	
	@Override
	public Map<String, Object> createProject(CloudProject cloudProject, Customer customer, boolean isProjectOnly) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		customer.setCusNumber(customer.getCusNumber()+"Admin");
		cloudProject.setBelongOrg(customer.getCusOrg());
		cloudProject.setRouteCount(cloudProject.getNetWork());
		//cloudProject.setDiskCapacity(cloudProject.getDiskSize()+cloudProject.getSnapshotSize());//云硬盘和备份大小=云硬盘大小+云硬盘备份大小
		if(cloudProject.getSafeGroup()<3){
			cloudProject.setSafeGroup(3);
		}
		SeqTool.generateNextNumber(customer.getCusOrg(), "_", 2);//确认保存序号+1
		String dcId = cloudProject.getDcId();// 获取数据中心ID
		BaseDcDataCenter datacenter = dataCenterService.getById(dcId);// 获取数据中心
		String mainProjectId = datacenter.getOsAdminProjectId();
		try {
			// 封装要保存到虚拟化平台的数据
			JSONObject data = createTenantData(cloudProject);
			Tenant tenant = openstackTenantService.create(dcId, mainProjectId, data);// 创建项目到虚拟化平台

			// 客户和项目信息入库
			if (tenant != null) {
				if (!isProjectOnly) {
					// 保存客户信息
					customer.setIsBlocked(false);
					customer.setBlockopStatus(false);
					if (customer.getCusId() == null) {
						customer = ecmcCustomerService.addCustomer(customer);
					} else {
						ecmcCustomerService.updateCustomer(customer, true);
					}
					returnMap.put("customer", customer);
				}
				//设置项目所属客户ID
				cloudProject.setCustomerId(customer.getCusId());
				// 设置项目ID
				cloudProject.setProjectId(tenant.getId());
				// 设置标签
				cloudProject.setLabelInId(labelCreate(cloudProject, cloudProject.getProjectId() + "-in"));
				cloudProject.setLabelOutId(labelCreate(cloudProject, cloudProject.getProjectId() + "-out"));
				// 修改虚拟化平台配额数据
				saveOrUpdateToVP(cloudProject);
				// 保存项目到数据库
				BaseCloudProject baseProject = new BaseCloudProject();
				BeanUtils.copyPropertiesByModel(baseProject, cloudProject);
				baseProject.setCreateDate(new Date());
				baseProject=cloudProjectDao.save(baseProject);
				//短信总数配额存入缓存
				jedisUtil.set(RedisKey.SMS_QUOTA_TOTAL+customer.getCusId()+":"+cloudProject.getProjectId(), String.valueOf(cloudProject.getSmsCount()));
				// 创建安全组
				createSecurityGroup(cloudProject);
				returnMap.put("project", baseProject);
				//初始化客户可开票金额
				invoiceService.initBillableAmount(customer.getCusId());
				return returnMap;
			}
		} catch (Exception e) {
			log.info("创建项目发生异常：{}", e.getMessage());
			if(cloudProject.getProjectId()!=null){
				openstackTenantService.delete(dcId, mainProjectId, cloudProject.getProjectId());
			}
			if(cloudProject.getLabelInId()!=null){
				openstackMeterLabelService.labelDelete(dcId, cloudProject.getLabelInId());
			}
			if(cloudProject.getLabelOutId()!=null){
				openstackMeterLabelService.labelDelete(dcId, cloudProject.getLabelOutId());
			}
			throw e;
		}
		return null;
	}
	
	/**
	 * 封装租户数据（用于：修改虚拟化平台中的项目名）
	 * @param baseCloudProject
	 * @return
	 */
	private JSONObject createTenantData(BaseCloudProject baseCloudProject){
		JSONObject data = new JSONObject();// 项目信息
		JSONObject jsonTenant = new JSONObject();
		jsonTenant.put("name", baseCloudProject.getPrjName());// 项目名称
		jsonTenant.put("description", baseCloudProject.getProjectDesc());// 项目描述
		data.put("tenant", jsonTenant);
		return data;
	}
	
	/**
	 * 创建标签
	 * @param baseCloudProject
	 * @param labelName
	 * @return
	 */
	private String labelCreate(BaseCloudProject baseCloudProject, String labelName) {
		JSONObject data = new JSONObject();
		JSONObject params = new JSONObject();
		params.put("name", labelName);
		params.put("description", "description");
		params.put("tenant_id", baseCloudProject.getProjectId());
		data.put("metering_label", params);
		try {
			// 创建label
			MeteringLabel result = openstackMeterLabelService.labelCreate(baseCloudProject.getDcId(), data);
			return result.getId();
		} catch (AppException e) {
			throw e;
		}
	}
	
	/**
	 * 更新虚拟化平台数据
	 * @param baseCloudProject
	 * @throws AppException
	 */
	private void saveOrUpdateToVP(BaseCloudProject baseCloudProject) throws AppException {
		BaseDcDataCenter datacenter = dataCenterService.getById(baseCloudProject.getDcId());// 获取数据中心
		String datacenterId = datacenter.getId();
		String projectId = datacenter.getOsAdminProjectId();
		String id = baseCloudProject.getProjectId();

		JSONObject data = new JSONObject();// 方法参数
		JSONObject jsonQuota = new JSONObject();// json参数

		// cpu&内存&云主机数量
		jsonQuota.put("cores", baseCloudProject.getCpuCount());// cpu OK
		jsonQuota.put("ram", ((int)baseCloudProject.getMemory()) * 1024);// 内存 OK
		jsonQuota.put("instances", baseCloudProject.getHostCount());// 云主机数量 OK
		data.put("quota_set", jsonQuota);// 配额参数
		openstackTenantService.editQuota(datacenterId, projectId, id, data);
		// 云硬盘相关(云硬盘大小和数量)
		data.clear();
		jsonQuota.clear();
		jsonQuota.put("volumes", baseCloudProject.getDiskCount());// 云硬盘数量 volumes
		//jsonQuota.put("snapshots", baseCloudProject.getDiskSnapshot());// 云硬盘备份 snapshots
		jsonQuota.put("gigabytes", baseCloudProject.getDiskCapacity());// 云硬盘和云硬盘备份数量
		data.put("quota_set", jsonQuota);
		openstackTenantService.editQuotaVolumes(datacenterId, projectId, id, data);
		//云硬盘备份相关(数量)
		data.clear();
		jsonQuota.clear();
		jsonQuota.put("tenant_id", baseCloudProject.getProjectId());//项目ID projectId
		jsonQuota.put("backups", baseCloudProject.getDiskSnapshot());//云硬盘备份数量
		data.put("quota_set", jsonQuota);
		openstackTenantService.editQuotaVolumes(datacenterId, projectId, id, data);
		//云硬盘备份相关(大小)
		data.clear();
		jsonQuota.clear();
		jsonQuota.put("tenant_id", baseCloudProject.getProjectId());//项目ID projectId
		jsonQuota.put("backup_gigabytes", baseCloudProject.getSnapshotSize());//云备份大小
		data.put("quota_set", jsonQuota);
		openstackTenantService.editQuotaVolumes(datacenterId, projectId, id, data);
		// 网络相关
		data.clear();
		jsonQuota.clear();
		jsonQuota.put("network", baseCloudProject.getNetWork());// 网络数量 network
		jsonQuota.put("subnet", baseCloudProject.getSubnetCount());// 子网数量
		jsonQuota.put("floatingip", baseCloudProject.getOuterIP());// 浮动IP数量
		jsonQuota.put("router", baseCloudProject.getRouteCount());// 路由
		jsonQuota.put("port", baseCloudProject.getPortCount() == null ? -1 : baseCloudProject.getPortCount());// 端口
		jsonQuota.put("security_group", baseCloudProject.getSafeGroup());// 安全组
		jsonQuota.put("pool", baseCloudProject.getQuotaPool());// 负载均衡
		jsonQuota.put("vpnservice", baseCloudProject.getCountVpn());//VPN数量
		jsonQuota.put("vip", baseCloudProject.getQuotaPool());//负载均衡虚拟IP数量=负载均衡数量
		jsonQuota.put("portmapping", baseCloudProject.getPortMappingCount());//端口映射数量
		data.put("quota", jsonQuota);
		openstackTenantService.editQuotaNetwork(datacenterId, projectId, id, data);
		
		//RDS相关
		data.clear();
		jsonQuota.clear();
		long instances = baseCloudProject.getMaxMasterInstance()*(baseCloudProject.getMaxSlaveIOfCluster()+1);
		jsonQuota.put("instances", instances);
		jsonQuota.put("backups", instances*(2+2*ConstantClazz.RDS_MAX_AUTO_BACKUP+baseCloudProject.getMaxBackupByHand()));
		jsonQuota.put("volumes", instances*5000);
		data.put("quotas", jsonQuota);
		openstackTenantService.editTroveQuota(datacenterId, id, data);
	}
	
	/**
	 * 创建安全组和规则
	 * @param baseCloudProject
	 */
	private void createSecurityGroup(BaseCloudProject baseCloudProject) {
		List<SecurityGroup> groupList = openstackSecurityGroupService.list(baseCloudProject.getDcId(),
				baseCloudProject.getProjectId());
		if (groupList != null && groupList.size() > 0) {
			for (SecurityGroup securityGroup : groupList) {
				// 修改底层默认安全组描述信息
				JSONObject data = new JSONObject();
				JSONObject temp = new JSONObject();
				// temp.put("name", cloudSecurityGroup.getSgName());
				temp.put("description", "出方向允许所有出站流量；入方向仅允许来自其他与默认安全组相关联的云主机的入站流量。");
				data.put("security_group", temp);
				// 执行openstack修改操作
				securityGroup = openstackSecurityGroupService.update(baseCloudProject.getDcId(),
						securityGroup.getTenant_id(), data, securityGroup.getId());

				// 安全组
				BaseCloudSecurityGroup cloudSg = new BaseCloudSecurityGroup(securityGroup, baseCloudProject.getDcId());
				cloudSg.setDefaultGroup("defaultGroup");
				cloudSg.setCreateTime(new Date());
				ecmcCloudSecurityGroupService.updateToDB(cloudSg);

				// 安全规则
				Rule[] rules = securityGroup.getSecurity_group_rules();
				for (Rule rule : rules) {
					BaseCloudSecurityGroupRule cloudGr = new BaseCloudSecurityGroupRule(rule,
							baseCloudProject.getDcId());
					ecmcCloudSecurityGroupRuleService.addSecurityGroupRule(cloudGr);
				}
			}
		}
		// 创建默认安全组
		try {
			securityGroupService.addDefault22SecurityGroup(baseCloudProject.getProjectId(), baseCloudProject.getDcId());
			securityGroupService.addDefault3389SecurityGroup(baseCloudProject.getProjectId(),
					baseCloudProject.getDcId());
		} catch (Exception e) {
			throw e;
		}
	}


	@Override
	public List<CloudProject> getProjectByCustomer(String customerId) {
		StringBuffer sqlBuffer = new StringBuffer();
		List<CloudProject> projects = new ArrayList<CloudProject>();
		sqlBuffer.append("SELECT");
		sqlBuffer.append(" cp.prj_id AS projectId,"); // 项目ID
		sqlBuffer.append(" cp.prj_name AS prjName,"); // 项目名称
		sqlBuffer.append(" dd.dc_name AS dcName,"); // 数据中心名称
		sqlBuffer.append(" cp.create_date AS createDate,"); // 创建时间
		sqlBuffer.append(" cp.prj_desc AS projectDesc,"); // 项目描述
		sqlBuffer.append(" count(distinct cv.vm_id) AS usedVmCount,"); // 云主机数量
		sqlBuffer.append(" count(distinct cn.subnet_id) AS subnetCountUse,"); // 子网数量
		sqlBuffer.append(" count(distinct ea.am_id) AS alarmCount,"); // 告警数量
		sqlBuffer.append(" cp.dc_id as dcId ");
		sqlBuffer.append(" FROM cloud_project cp");
		sqlBuffer.append(" LEFT JOIN cloud_vm cv ON cp.prj_id = cv.prj_id and (cv.is_deleted='0' or cv.is_deleted='2') and cv.is_visable = '1'");
		sqlBuffer.append(" LEFT JOIN cloud_subnetwork cn ON cp.prj_id = cn.prj_id and cn.subnet_type is not null");
		sqlBuffer.append(" LEFT JOIN ecmc_alarmmessage ea ON cp.prj_id = ea.prj_id and ea.am_isprocessed='0'");
		sqlBuffer.append(" LEFT JOIN dc_datacenter dd ON cp.dc_id = dd.id");
		sqlBuffer.append(" WHERE cp.customer_id = ?");
		sqlBuffer.append(" GROUP BY cp.prj_id");
		List<String> params = new ArrayList<String>();
		params.add(customerId);
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = cloudProjectDao.createSQLNativeQuery(sqlBuffer.toString(), params.toArray()).getResultList();
		if (!CollectionUtils.isEmpty(resultList)) {
			for (Object[] obj : resultList) {
				CloudProject cloudProject = new CloudProject();
				cloudProject.setProjectId(ObjectUtils.toString(obj[0], null));
				cloudProject.setPrjName(ObjectUtils.toString(obj[1], null));
				cloudProject.setDcName(ObjectUtils.toString(obj[2], null));
				cloudProject.setCreateDate(DateUtil.stringToDate(ObjectUtils.toString(obj[3], null)));
				cloudProject.setProjectDesc(ObjectUtils.toString(obj[4], null));
				cloudProject.setHostCount(Integer.parseInt(ObjectUtils.toString(obj[5], null)));
				cloudProject.setSubnetCount(Integer.parseInt(ObjectUtils.toString(obj[6], "0")));
				cloudProject.setAlarmCount(Integer.parseInt(ObjectUtils.toString(obj[7], "0")));
				cloudProject.setDcId(ObjectUtils.toString(obj[8], null));
				// 获取项目ssh密钥已使用量
				try {
					cloudProject.setSshKeyUsedCount(cloudSecretKeyService.getAllSecretkey(cloudProject.getProjectId()));
				} catch (Exception e) {
					throw new AppException("获取项目ssh密钥使用量异常！");
				}
				projects.add(cloudProject);
			}
		}
		return projects;
	}


	@Override
	public CloudProject getProjectById(String projectId) throws Exception {
		BaseCloudProject baseCloudProject = cloudProjectDao.findOne(projectId);
		CloudProject cloudProject = new CloudProject();
		BeanUtils.copyPropertiesByModel(cloudProject, baseCloudProject);
		int usedSmsCount = StringUtil.getAsInt(jedisUtil.get(RedisKey.SMS_QUOTA_SENT+cloudProject.getCustomerId()+":"+cloudProject.getProjectId()), 0);
		cloudProject.setUsedSmsCount(usedSmsCount);
		BaseDcDataCenter dataCenter = dataCenterService.getById(cloudProject.getDcId());
		cloudProject.setDcName(dataCenter.getName());
		return cloudProject;
	}


	@Override
	public CloudProject getProjectQuotaPool(String projectId) throws Exception {
		try {
			// 查询项目基本信息
			BaseCloudProject baseCloudProject = cloudProjectDao.findOne(projectId);
			// 查询项目资源使用情况（云主机、CPU、内存等）
			CloudProject cloudProject = this.findProjectQuotaPool(projectId);
			BeanUtils.copyPropertiesByModel(cloudProject, baseCloudProject);
			//数据中心名称
			BaseDcDataCenter dataCenter = dataCenterService.getById(baseCloudProject.getDcId());
			cloudProject.setDcName(dataCenter.getName());
			Map<String, Integer> map = this.getPrjQuato(projectId);
			/*云主机使用量*/
			cloudProject.setUsedVmCount(map.get("usedHostCount"));
			/*CPU使用量*/
			cloudProject.setUsedCpuCount(map.get("usedCpu"));
			/*内存使用量*/
			cloudProject.setUsedRam(map.get("usedRam")/1024);
			// 带宽使用量
			int usedBandCount = ecmcRouteService.getQosNumByPrjId(projectId);
			cloudProject.setCountBandUse(usedBandCount);
			// 备份使用量(数量)
			int usedSnapshotCount = ecmcCloudSnapshotService.getUsedSnapshotCount(projectId);
			cloudProject.setDiskSnapshotUse(usedSnapshotCount);
			// 备份使用量(容量)
			int usedSnapshotCapacity = ecmcCloudSnapshotService.getUsedSnapshotCapacity(projectId);
			cloudProject.setUsedSnapshotCapacity(usedSnapshotCapacity);
			// 路由使用量
			int usedRouterCount = ecmcRouteService.getCountByPrjId(projectId);
			cloudProject.setRouteCountUse(usedRouterCount);
			// 安全组使用量
			int usedSafeGroupCount = ecmcCloudSecurityGroupService.getCountByPrjId(projectId);
			cloudProject.setSafeGroupUse(usedSafeGroupCount);
			// 云硬盘使用量(数量)
			int usedVolumeCount = ecmcCloudVolumeService.getUsedVolumeCountByPrjId(projectId);
			cloudProject.setDiskCountUse(usedVolumeCount);
			// 云硬盘使用量(容量)
			int usedVolumeCapacity = ecmcCloudVolumeService.getUsedVolumeCapacityByPrjId(projectId);;
			cloudProject.setUsedDiskCapacity(usedVolumeCapacity);
			// 网络使用量
			int usedNetWorkCount = ecmcNetworkService.getCountByPrjId(projectId);
			cloudProject.setNetWorkUse(usedNetWorkCount);
			// 子网使用量
			int usedSubNetWorkCount = ecmcSubNetworkService.getCountByPrjId(projectId);
			cloudProject.setSubnetCountUse(usedSubNetWorkCount);
			// 浮动IP使用量
			int usedFloatIpCount = ecmcCloudFloatIPService.findBindCountByPriId(projectId);
			cloudProject.setOuterIPUse(usedFloatIpCount);
			// 资源池使用量
			int usedPoolCount = ecmcLBPoolService.getCountByPrjId(projectId);
			cloudProject.setUsedPool(usedPoolCount);
			// 短信使用量
			int usedSmsCount = StringUtil.getAsInt(jedisUtil.get(RedisKey.SMS_QUOTA_SENT+cloudProject.getCustomerId()+":"+cloudProject.getProjectId()), 0);
			cloudProject.setUsedSmsCount(usedSmsCount);
			// VPN使用量
			int usedVpnCount = ecmcVpnService.getCountByPrjId(projectId);
			cloudProject.setCountVpnUse(usedVpnCount);
			//端口映射使用量
			int portMappingUse = ecmcPortMappingService.getCountByPrjId(projectId);
			cloudProject.setPortMappingUse(portMappingUse);
			//自定义镜像使用量
			int imageCountUse = ecmcCloudImageService.getImageCountByPrjId(projectId);
			cloudProject.setImageCountUse(imageCountUse);
			
			//项目下主实例个数
			int masterInstanceUse = ecmcCloudRDSInstanceService.getMasterCountByPrjId(projectId);
			cloudProject.setMasterInstanceUse(masterInstanceUse);
			//项目下从实例总数
			int slaveInstanceCount = ecmcCloudRDSInstanceService.getSlaveCountByPrjId(projectId);
			cloudProject.setTotalInstanceUse(masterInstanceUse+slaveInstanceCount);
			
			int sshKeyUsedCount = cloudSecretKeyService.getAllSecretkey(projectId);
			cloudProject.setSshKeyUsedCount(sshKeyUsedCount);
			return cloudProject;
		} catch (Exception e) {
			throw e;
		}
	}


	@Override
	public BaseCloudProject updateProject(BaseCloudProject baseProject) throws Exception {
		try {
			BaseCloudProject origProject = cloudProjectDao.findOne(baseProject.getProjectId());
			baseProject.setRouteCount(baseProject.getNetWork());
			//baseProject.setDiskCapacity(baseProject.getDiskSize() + baseProject.getSnapshotSize());//更新云硬盘和备份大小
			// 如果项目描述与目前不一致，则需修改底层项目描述（项目名称由系统自动生成，且不可更改）
			if (!StringUtils.equals(baseProject.getProjectDesc(), origProject.getProjectDesc())) {
				JSONObject data = createTenantData(baseProject);
				openstackTenantService.edit(baseProject.getDcId(), baseProject.getProjectId(),
						baseProject.getProjectId(), data);
			}
			// 修改虚拟化平台配额数据
			saveOrUpdateToVP(baseProject);
			jedisUtil.set(RedisKey.SMS_QUOTA_TOTAL+baseProject.getCustomerId()+":"+baseProject.getProjectId(), String.valueOf(baseProject.getSmsCount()));
			// 入数据库
			baseProject = (BaseCloudProject) cloudProjectDao.merge(baseProject);
			return baseProject;
		} catch (Exception e) {
			throw e;
		}
	}
	
	@Override
	public BaseCloudProject deleteProject(String projectId) {
		try {
			BaseCloudProject baseCloudProject = cloudProjectDao.findOne(projectId);// 项目id
			// 删除标签
			if (!StringUtil.isEmpty(baseCloudProject.getLabelInId())) {
				openstackMeterLabelService.labelDelete(baseCloudProject.getDcId(), baseCloudProject.getLabelInId());
			}
			if( !StringUtil.isEmpty(baseCloudProject.getLabelOutId())){
				openstackMeterLabelService.labelDelete(baseCloudProject.getDcId(), baseCloudProject.getLabelOutId());
			}
			//删除租户
			boolean tenantDeleted = openstackTenantService.delete(baseCloudProject.getDcId(), projectId, projectId);
			if (tenantDeleted) {
				//删除安全组
				List<BaseCloudSecurityGroup> securityGroupList = ecmcCloudSecurityGroupService.getByPrjId(projectId);
				if(securityGroupList != null){
					for (BaseCloudSecurityGroup baseCloudSecurityGroup : securityGroupList) {
						ecmcCloudSecurityGroupService.deleteSecurityGroup(baseCloudProject.getDcId(), baseCloudSecurityGroup.getSgId());
					}
				}
				//删除项目
				cloudProjectDao.delete(projectId);
			}
			return baseCloudProject;
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public int getCountByDataCenterId(String dcId) {
		List<BaseCloudProject> projects = cloudProjectDao.getListByDataCenter(dcId);
		if (projects != null && projects.size() > 0) {
			return projects.size();
		}
		return 0;
	}
	
	@Override
	public List<CloudProject> firewallProject(String datacenterId) throws Exception {
		List<BaseCloudProject> baseProjects = cloudProjectDao.find(" from BaseCloudProject p where p.dcId = ? ", datacenterId);
		List<CloudProject> projects = new ArrayList<CloudProject>();
		if(baseProjects!=null && baseProjects.size()>0){
			try {
				for (BaseCloudProject baseCloudProject : baseProjects) {
					CloudProject project = new CloudProject();
					BeanUtils.copyProperties(project, baseCloudProject);
					projects.add(project);
				}
				return projects;
			} catch (Exception e) {
				throw e;
			}
		}
		return null;
	}
	
	private CloudProject findProjectQuotaPool(String projectId){
		CloudProject cloudProject = new CloudProject();
		List<String> params = new ArrayList<String>();
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("SELECT ");
		sqlBuffer.append("cp.prj_id AS prjId, ");
		sqlBuffer.append("cp.prj_name AS prjName, ");
		sqlBuffer.append("cp.dc_id AS dcId, ");
		sqlBuffer.append("cp.cpu_count AS cpuCount, ");
		sqlBuffer.append("cp.host_count AS hostCount, ");
		sqlBuffer.append("cp.memory AS memory, ");
		sqlBuffer.append("cp.disk_capacity AS diskCapacity, ");
		sqlBuffer.append("v.usedVmCount, ");
		sqlBuffer.append("v.usedCpuCount, ");
		sqlBuffer.append("v.usedRam, ");
		sqlBuffer.append("vol.usedDiskCapacity, ");
		sqlBuffer.append("snap.usedSnapshotCapacity, ");
		sqlBuffer.append("cp.count_vpn, ");
		//sqlBuffer.append("cp.disk_size, ");
		sqlBuffer.append("cp.snapshot_size, ");
		sqlBuffer.append("cp.portmapping_count, ");
		sqlBuffer.append("cp.image_count ");
		sqlBuffer.append("FROM cloud_project cp ");
		sqlBuffer.append("LEFT JOIN ( ");
		sqlBuffer.append("SELECT vm.prj_id, count(vm.vm_id) AS usedVmCount, sum(cf.flavor_vcpus) AS usedCpuCount, sum(cf.flavor_ram) AS usedRam ");
		sqlBuffer.append("FROM ( ");
		sqlBuffer.append("SELECT cv.vm_id, cv.dc_id, cv.prj_id, cv.is_deleted,cv.is_visable, CASE WHEN cv.resize_id IS NOT NULL THEN cv.resize_id ELSE cv.flavor_id END AS flavor_id ");
		sqlBuffer.append("FROM cloud_vm cv ");
		sqlBuffer.append(") vm ");
		sqlBuffer.append("LEFT JOIN cloud_flavor cf ON vm.flavor_id = cf.flavor_id ");
		sqlBuffer.append("AND vm.dc_id = cf.dc_id ");
		sqlBuffer.append("WHERE vm.is_deleted = '0' and vm.is_visable ");
		sqlBuffer.append("GROUP BY vm.prj_id ");
		sqlBuffer.append(") v ON cp.prj_id = v.prj_id ");
		sqlBuffer.append("LEFT JOIN ( ");
		sqlBuffer.append("SELECT vo.prj_id, sum(vo.vol_size) AS usedDiskCapacity ");
		sqlBuffer.append("FROM cloud_volume vo ");
		sqlBuffer.append("WHERE vo.is_deleted = '0' ");
		sqlBuffer.append("GROUP BY vo.prj_id ");
		sqlBuffer.append(") vol ON cp.prj_id = vol.prj_id ");
		sqlBuffer.append("LEFT JOIN ( ");
		sqlBuffer.append("SELECT snap.prj_id, sum(snap.snap_size) AS usedSnapshotCapacity ");
		sqlBuffer.append("FROM cloud_disksnapshot snap ");
		sqlBuffer.append("GROUP BY snap.prj_id ");
		sqlBuffer.append(") snap ON cp.prj_id = snap.prj_id ");
		sqlBuffer.append("WHERE cp.prj_id = ? ");
		params.add(projectId);
		List<Object[]> cloudProjectObj = cloudProjectDao.createSQLNativeQuery(sqlBuffer.toString(), params.toArray()).getResultList();
		if (cloudProjectObj != null && cloudProjectObj.size() > 0) {
			Object[] object = cloudProjectObj.get(0);
			cloudProject.setProjectId(ObjectUtils.toString(object[0], null));
			cloudProject.setPrjName(ObjectUtils.toString(object[1], null));
			cloudProject.setDcId(ObjectUtils.toString(object[2], null));
			cloudProject.setCpuCount(Integer.parseInt(ObjectUtils.toString(object[3], "0")));
			cloudProject.setHostCount(Integer.parseInt(ObjectUtils.toString(object[4], "0")));
			cloudProject.setMemory(Float.parseFloat(ObjectUtils.toString(object[5], "0")));
			cloudProject.setDiskCapacity(Integer.parseInt(ObjectUtils.toString(object[6], "0")));
			cloudProject.setUsedVmCount(Integer.parseInt(ObjectUtils.toString(object[7], "0")));
			cloudProject.setUsedCpuCount(Integer.parseInt(ObjectUtils.toString(object[8], "0")));
			cloudProject.setUsedRam(Integer.parseInt(ObjectUtils.toString(object[9], "0"))/1024);
			cloudProject.setUsedDiskCapacity(Integer.parseInt(ObjectUtils.toString(object[10], "0")));
			cloudProject.setUsedSnapshotCapacity(Integer.parseInt(ObjectUtils.toString(object[11], "0")));
			cloudProject.setCountVpn(Integer.parseInt(ObjectUtils.toString(object[12], "0")));
			//cloudProject.setDiskSize(Float.parseFloat(ObjectUtils.toString(object[13], "0")));//云硬盘大小
			cloudProject.setSnapshotSize(Integer.parseInt(ObjectUtils.toString(object[13], "0")));//云硬盘备份大小
			cloudProject.setPortMappingCount(Integer.parseInt(ObjectUtils.toString(object[14], "0")));//端口映射数量
			cloudProject.setImageCount(Integer.parseInt(ObjectUtils.toString(object[15], "0")));//自定义镜像数量
			//云硬盘和备份(已使用)大小 = 云硬盘已使用大小 + 云备份已使用大小
			//cloudProject.setUsedDataCapacity(cloudProject.getUsedDiskCapacity() + cloudProject.getUsedSnapshotCapacity());
		}
		return cloudProject;
	}

	@Override
	public boolean hasProjectByCustomerAndDc(String dcId, String cusId) {
		int count = cloudProjectDao.findProByDcId(cusId, dcId);
		return count == 0 ? false : true;
	}
	
	@Override
	public Map<String, String> hasResource(String prjId) throws Exception {
		BaseCloudProject project = cloudProjectDao.findOne(prjId);
		Map<String, String> resMap = new HashMap<String, String>();
		int count = 0;
		// 获取项目配额 云主机/云硬盘/云备份/网络/自定义镜像/防火墙/资源池
		// 路由、安全组、网络、公网IP、
		count = ecmcCloudVmService.getUnDeletedVmCountByProject(prjId);
		if (count != 0) {
			resMap.put("error", "云主机");
			return resMap;
		}
		count = ecmcCloudImageService.getImageCountByPrjId(prjId);
		if (count != 0) {
			resMap.put("error", "自定义镜像");
			return resMap;
		}
		count = ecmcCloudSnapshotService.countSnapshotByPrjId(prjId);
		if (count != 0) {
			resMap.put("error", "云硬盘备份");
			return resMap;
		}
		count = ecmcCloudVolumeService.getCountByPrjId(prjId);
		if (count != 0) {
			resMap.put("error", "云硬盘");
			return resMap;
		}
		count = ecmcLBPoolService.getCountByPrjId(prjId);
		if (count != 0) {
			resMap.put("error", "负载均衡器");
			return resMap;
		}
		count = ecmcNetworkService.getCountByPrjId(prjId);
		if (count != 0) {
			resMap.put("error", "网络");
			return resMap;
		}
		count = ecmcRouteService.getCountByPrjId(prjId);
		if (count != 0) {
			resMap.put("error", "路由器");
			return resMap;
		}
		count = ecmcCloudFloatIPService.getCountByPrjId(prjId);
		if (count != 0) {
			resMap.put("error", "公网IP");
			return resMap;
		}
		//防火墙规则
		List<CloudFwRule> rules = ecmcCloudFwRuleService.getFwRulesByPrjId(project.getDcId(), prjId);
		count = (rules == null ? 0 : rules.size());
		if (count != 0) {
			resMap.put("error", "防火墙规则");
			return resMap;
		}
		//防火墙策略
		List<CloudFwPolicy> policyList = ecmcCloudFireWallPoliyService.getFwpListByPrjId(project.getDcId(), prjId);
		count = (policyList == null ? 0 : policyList.size());
		if (count != 0) {
			resMap.put("error", "防火墙策略");
			return resMap;
		}
		//防火墙
		String hql = "from BaseCloudFireWall fw where fw.prjId = ?";
		List<String> params = new ArrayList<String>();
		params.add(prjId);
		List<BaseCloudFireWall> firewallList = cloudProjectDao.createQuery(hql, params.toArray()).list();
 		count = (firewallList == null ? 0 : firewallList.size());
		if (count != 0) {
			resMap.put("error", "防火墙");
			return resMap;
		}
		
		List<BaseCloudSecurityGroup> securityGroupList = ecmcCloudSecurityGroupService.getByPrjId(prjId);
		if (securityGroupList != null) {
			for (BaseCloudSecurityGroup baseCloudSecurityGroup : securityGroupList) {
				if (!baseCloudSecurityGroup.getDefaultGroup().equals("defaultGroup")) {
					count = 1;
					break;
				}
			}
		}
		if (count != 0) {
			resMap.put("error", "安全组");
			return resMap;
		}
		count = ecmcLBHealthMonitorService.getCountByPrjId(prjId);
		if (count != 0) {
			resMap.put("error", "监控");
			return resMap;
		}
		
		return resMap;
	}

	@Override
	public List<BaseCloudProject> getByDataCenterId(String dcId) throws AppException {
		List<BaseCloudProject> projects = cloudProjectDao.getListByDataCenter(dcId);
		return projects ;
	}

	/**
	 * 查询项目下的云主机配额信息和已使用量统计 (云主机，CPU，内存)
	 * ----------------------------------
	 * 
	 * @author liuzhuangzhuang
	 * 
	 * @param prjId
	 *            项目ID
	 *         
	 * @return 项目配额使用情况信息
	 */
	private Map<String, Integer> getPrjQuato(String prjId) {
		StringBuffer sqlBuf = new StringBuffer();
		sqlBuf.append(" SELECT ");
		sqlBuf.append(" vm.usedHostCount,vm.usedCpu,vm.usedRam, ");
		sqlBuf.append(" ordervm.usedHostCount as usedHostCount1,ordervm.usedCpu as usedCpu2, ");
		sqlBuf.append(" ordervm.usedRam as usedRam2");
		sqlBuf.append(" FROM cloud_project cp   ");
		sqlBuf.append(" LEFT JOIN ( ");
		sqlBuf.append(" SELECT vm.prj_id,count(1) AS usedHostCount,sum(flavor.flavor_vcpus) AS usedCpu,sum(flavor.flavor_ram) AS usedRam  ");
		sqlBuf.append(" FROM  cloud_vm vm  LEFT JOIN cloud_flavor flavor ON flavor.flavor_id = vm.flavor_id  AND flavor.dc_id = vm.dc_id  ");
		sqlBuf.append(" WHERE vm.prj_id = ? AND vm.is_deleted = '0' AND vm.is_visable = '1' ");
		sqlBuf.append("     )  ");
		sqlBuf.append(" vm ON cp.prj_id = vm.prj_id");
		sqlBuf.append(" LEFT JOIN (");
		sqlBuf.append(" SELECT                                                        	   		 ");
		sqlBuf.append(" 		ordervm.prj_id,                                             		 ");
		sqlBuf.append(" 		sum(ordervm.count) AS usedHostCount,                     	   		 ");
		sqlBuf.append(" 		sum(ordervm.cpu) AS usedCpu,                                         ");
		sqlBuf.append(" 		sum(ordervm.ram) AS usedRam,                    		             ");
		sqlBuf.append(" 		sum(ordervm.disk) AS usedDisk                  	   	                 ");
		sqlBuf.append(" FROM(                                                      	   		 ");
		sqlBuf.append(" SELECT                                                                       ");
		sqlBuf.append(" 	cov.prj_id,                                                              ");
		sqlBuf.append(" 	cov.order_no,                                                            ");
		sqlBuf.append(" 	cov.order_type,                                                          ");
		sqlBuf.append(" 	CASE cov.order_type                                                      ");
		sqlBuf.append(" WHEN '2' THEN                                                                ");
		sqlBuf.append(" 	0                                                                        ");
		sqlBuf.append(" ELSE                                                                         ");
		sqlBuf.append(" 	cov.count                                                                ");
		sqlBuf.append(" END AS count,                                                                ");
		sqlBuf.append(" 	CASE cov.order_type                                                      ");
		sqlBuf.append(" WHEN '2' THEN                                                                ");
		sqlBuf.append(" 	(cov.cpu - cf.flavor_vcpus)                                              ");
		sqlBuf.append(" ELSE                                                                         ");
		sqlBuf.append(" 	cov.count*cov.cpu                                                        ");
		sqlBuf.append(" END AS cpu,                                                                  ");
		sqlBuf.append("  CASE cov.order_type                                                         ");
		sqlBuf.append(" WHEN '2' THEN                                                                ");
		sqlBuf.append(" 	(cov.ram - cf.flavor_ram)                                                ");
		sqlBuf.append(" ELSE                                                                         ");
		sqlBuf.append(" 	cov.count*cov.ram                                                        ");
		sqlBuf.append(" END AS ram,                                                                  ");
		sqlBuf.append("  CASE cov.order_type                                                         ");
		sqlBuf.append(" WHEN '2' THEN                                                                ");
		sqlBuf.append(" 	0                                                                        ");
		sqlBuf.append(" ELSE                                                                         ");
		sqlBuf.append(" 	cov.count*cov.disk                                                       ");
		sqlBuf.append(" END AS disk                                                                  ");
		sqlBuf.append(" FROM                                                                         ");
		sqlBuf.append(" 	cloudorder_vm cov                                                        ");
		sqlBuf.append(" LEFT JOIN cloud_vm vm ON cov.vm_id = vm.vm_id                                ");
		sqlBuf.append(" LEFT JOIN cloud_flavor cf ON vm.flavor_id = cf.flavor_id                     ");
		sqlBuf.append(" 		) ordervm                                     	   		             ");
		sqlBuf.append(" 	LEFT JOIN order_info info ON info.order_no = ordervm.order_no 	   		 ");
		sqlBuf.append(" 	WHERE                                                       	   		 ");
		sqlBuf.append(" 		ordervm.prj_id = ?                                     	   		     ");
		sqlBuf.append(" 	AND (                                                         	   		 ");
		sqlBuf.append(" 		info.order_state = '1'                                        		 ");
		sqlBuf.append(" 		OR info.order_state = '2'                                   		 ");
		sqlBuf.append(" 	)                                                             	   		 ");
		sqlBuf.append(" 	AND (                                                         	   		 ");
		sqlBuf.append(" 		ordervm.order_type = '0'                                       		 ");
		sqlBuf.append(" 		OR ordervm.order_type = '2'                                   		 ");
		sqlBuf.append(" 	)                    	   		 ");
		sqlBuf.append(" 		) ordervm ON ordervm.prj_id = cp.prj_id ");
		sqlBuf.append(" where cp.prj_id = ? ");
		javax.persistence.Query query = cloudProjectDao.createSQLNativeQuery(sqlBuf.toString(),
				new Object[] { prjId, prjId, prjId});
		Map<String, Integer> quotasMap = new HashMap<String, Integer>();
		@SuppressWarnings("rawtypes")
		List result = query.getResultList();
		if (result != null && result.size() == 1) {
			Object[] objs = (Object[]) result.get(0);
			quotasMap.put("usedHostCount", StringUtil.getAsInt(String.valueOf(objs[0]), 0) + StringUtil.getAsInt(String.valueOf(objs[3]), 0));
			quotasMap.put("usedCpu", StringUtil.getAsInt(String.valueOf(objs[1]), 0) + StringUtil.getAsInt(String.valueOf(objs[4]), 0));
			quotasMap.put("usedRam", StringUtil.getAsInt(String.valueOf(objs[2]), 0) + StringUtil.getAsInt(String.valueOf(objs[5]), 0));
		}
		return quotasMap;
	}
}
