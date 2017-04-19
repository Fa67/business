package com.eayun.database.instance.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import com.eayun.database.log.service.RDSLogService;
import org.apache.commons.collections.iterators.ObjectArrayIterator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.charge.model.ChargeRecord;
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.EayunQueueConstant;
import com.eayun.common.constant.PayType;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.CloudResourceUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.Hook;
import com.eayun.database.configgroup.service.RdsConfigurationService;
import com.eayun.database.instance.dao.CloudRDSInstanceDao;
import com.eayun.database.instance.model.BaseCloudRDSInstance;
import com.eayun.database.instance.model.CloudOrderRDSInstance;
import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.eayunstack.model.RDSInstance;
import com.eayun.eayunstack.service.OpenstackConfigurationGroupService;
import com.eayun.eayunstack.service.OpenstackRDSInstanceService;
import com.eayun.project.service.ProjectService;
import com.eayun.virtualization.model.BaseCloudProject;

@Transactional
@Service
public class BaseRDSInstanceService {

	@Autowired
	private CloudRDSInstanceDao cloudRDSInstanceDao;
	@Autowired
	private OpenstackRDSInstanceService openstackRDSInstanceService;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private OpenstackConfigurationGroupService openstackConfigurationGroupService;
	@Autowired
	private EayunRabbitTemplate rabbitTemplate;
	@Autowired
	private RdsConfigurationService rdsConfigurationService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private RDSLogService rdsLogService;

	/**
	 * 根据项目ID获取该项目的主库数量
	 * 注：从库处于分离中时，此时占用主库的配额
	 * @param prjId
	 *              项目ID
	 * @return
	 */
	public int getMasterCountByPrjId(String prjId) {
		int detachCount = this.getDetachRdsInstanceCountByMasterId(prjId, null); // 从库处于分离中的数量
		int orderCount = this.getRdsInstanceCountInOrder(prjId, "1"); // 处理中和待支付状态的订单的数量
		return cloudRDSInstanceDao.getCountByPrjId(prjId) + detachCount + orderCount;
	}

	/**
	 * 根据项目ID获取该项目的从库的数量
	 * 注：从库处于分离中时，此时占用主库的配额,而不占用从库的配额
	 * @param prjId
	 *               项目ID
	 * @return
	 */
	public int getSlaveCountByPrjId(String prjId) {
		int detachCount = this.getDetachRdsInstanceCountByMasterId(prjId, null); // 从库处于分离中的数量
		int orderCount = this.getRdsInstanceCountInOrder(prjId, "0"); // 处理中和待支付状态的订单的数量
		return cloudRDSInstanceDao.getSlaveCountByPrjId(prjId) - detachCount + orderCount;
	}
	
	/**
	 * 获取当前客户下的所有数据库实例（符合条件）
	 * @param page
	 * @param map  -- 请求参数
	 * @param sessionUser
	 * @param queryMap
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Page getList(Page page, ParamsMap map, SessionUserInfo sessionUser, QueryMap queryMap) 
			throws Exception {
		int index = 0;
		Object[] args = new Object[10];
		String projectId = "";
		String datacenterId = "";
		String name = "";
		String type = "";
		String status = "";
		String versionId = "";

		if (null != map && null != map.getParams()) {
			projectId = map.getParams().get("prjId") != null ? map.getParams().get("prjId") + "" : "";
			datacenterId = map.getParams().get("dcId") != null ? map.getParams().get("dcId") + "" : "";
			name = map.getParams().get("title") != null ? map.getParams().get("title") + "" : "";
			type = map.getParams().get("queryType") != null ? map.getParams().get("queryType") + "" : "";
			status = map.getParams().get("status") != null ? "" + map.getParams().get("status") : "";
			versionId = map.getParams().get("versionId") != null ? map.getParams().get("versionId") + "" : "";
		}
		StringBuffer sql = getQuerySqlAboutList();
		sql.append(" and cr.is_master = 1 ");
		sql.append(" and cr.prj_id = ? ");
		args[index++] = projectId;
		sql.append(" and cr.dc_id = ? ");
		args[index++] = datacenterId;
		// 状态查询
		if (!StringUtils.isEmpty(status)) {
			if ("1".equals(status)) {
				sql.append(" and (cr.charge_state = ? or cri.charge_state = ?)");
			} else if ("2".equals(status)) {
				sql.append(" and ((cr.charge_state = '3' or cr.charge_state = ?) or (cri.charge_state = '3' or cri.charge_state = ?)) ");
			} else {
				sql.append(" and (cr.rds_status = ? or cri.rds_status = ?) ");
				sql.append(" and (cr.charge_state = '0' or cri.charge_state = '0') ");
			}
			args[index++] = status;
			args[index++] = status;
		}
		if (!StringUtils.isEmpty(versionId)) {
			sql.append(" and cdv.id = ? ");
			args[index] = versionId;
			index++;
		}
		if (!StringUtils.isEmpty(name) && "name".equals(type)) { //名称
			sql.append(" and (binary cr.rds_name like ? or binary cri.rds_name like ?)");
			name = name.replaceAll("\\_", "\\\\_").replaceAll("\\%", "\\\\%");
			args[index++] = "%" + name + "%";
			args[index++] = "%" + name + "%";
		} else if (!StringUtils.isEmpty(name) && "ips".equals(type)) { // ip地址（受管子网）
			sql.append(" and (cr.rds_ip like ? or cri.rds_ip like ? )");
			name = name.replaceAll("\\_", "\\\\_").replaceAll("\\%", "\\\\%");
			args[index++] = "%" + name + "%";
			args[index++] = "%" + name + "%";
		}
		sql.append(" ORDER BY cr.create_time DESC ");
		Object[] params = new Object[index];
		System.arraycopy(args, 0, params, 0, index);
		page = cloudRDSInstanceDao.pagedNativeQuery(sql.toString(), queryMap, params);
		List newList = (List) page.getResult();
		for (int i = 0; i < newList.size(); i++) {
			Object[] objs = (Object[]) newList.get(i);
			CloudRDSInstance cloudRdsInstance = objectToCloudRDSInstance(objs);
			StringBuffer slaveSql = getQuerySqlAboutList();
			slaveSql.append(" and cr.is_master = 0 ");
			slaveSql.append(" and cr.master_id = ? ");
			slaveSql.append(" ORDER BY cr.create_time DESC ");
			javax.persistence.Query query = cloudRDSInstanceDao.createSQLNativeQuery(slaveSql.toString(), new String[]{cloudRdsInstance.getRdsId()});
			if(null != query){
				List list = query.getResultList();
				if(null != list && list.size() > 0){
					List slaveList = new ArrayList();
					for(int k = 0; k < list.size(); k++){
						Object[] slaveObjs = (Object[]) list.get(k);
						slaveList.add(objectToCloudRDSInstance(slaveObjs));
					}
					cloudRdsInstance.setChildren(slaveList);
				}
			}
			newList.set(i, cloudRdsInstance);
		}
		return page;
	}
	
	public StringBuffer getQuerySqlAboutList(){
		StringBuffer sql = new StringBuffer();
		
		sql.append("   SELECT  DISTINCT                                                           ");
		sql.append("       cr.rds_id,                                                             ");
		sql.append("       cr.rds_name,                                                           ");
		sql.append("       cr.is_master,                                                          ");
		sql.append("       cr.master_id,                                                          ");
		sql.append("       cp.prj_name,                                                           ");
		sql.append("       cdv.name as version,                                                   ");
		sql.append("       cd.name as type,                                                       ");
		sql.append("       cf.flavor_vcpus,                                                       ");
		sql.append("       cf.flavor_ram,                                                         ");
		sql.append("       cr.volume_size,                                                        ");
		sql.append("       cr.rds_ip,                                                             ");
		sql.append("       cr.pay_type,                                                           ");
		sql.append("       cr.charge_state,                                                       ");
		sql.append("       cr.create_time,                                                        ");
		sql.append("       cr.end_time,                                                           ");
		sql.append("       dc.dc_name,                                                            ");
		sql.append("       cr.rds_status,                                                         ");
		sql.append("       cr.prj_id,                                                             ");
		sql.append("       cr.dc_id,                                                              ");
		sql.append("       cv.type_name                                                           ");
		sql.append("   FROM cloud_rdsinstance cr LEFT JOIN dc_datacenter dc on cr.dc_id = dc.id   ");
		sql.append("   LEFT JOIN cloud_project cp on cr.prj_id =cp.prj_id                         ");
		sql.append("   LEFT JOIN cloud_datastoreversion cdv on cr.version_id = cdv.id             ");
		sql.append("   LEFT JOIN cloud_datastore cd on cdv.datastore_id = cd.id                   ");
		sql.append("   LEFT JOIN cloud_flavor cf on cr.flavor_id = cf.flavor_id                   ");
		sql.append("   LEFT JOIN cloud_rdsinstance cri on (cr.rds_id = cri.master_id              ");
		sql.append("   and cri.is_visible = '1' and cri.is_deleted = '0')                         ");
		sql.append("   LEFT JOIN cloud_volumetype cv on cr.volume_type = cv.type_id               ");
		sql.append("   WHERE cr.is_deleted = '0'                                                  ");
		sql.append("   and cr.is_visible = '1'                                                    ");
		
		return sql;
	}
	
	public CloudRDSInstance objectToCloudRDSInstance(Object[] objs){
		int ind = 0;
		CloudRDSInstance cloudRdsInstance = new CloudRDSInstance();
		cloudRdsInstance.setRdsId(String.valueOf(objs[ind++]));
		cloudRdsInstance.setRdsName(String.valueOf(objs[ind++]));
		cloudRdsInstance.setIsMaster(String.valueOf(objs[ind++]));
		cloudRdsInstance.setMasterId(objs[ind++] != null ? String.valueOf(objs[ind-1]) : null);
		cloudRdsInstance.setPrjName(String.valueOf(objs[ind++]));
		cloudRdsInstance.setVersion(String.valueOf(objs[ind++]));
		cloudRdsInstance.setType(String.valueOf(objs[ind++]));
		cloudRdsInstance.setCpu(Integer.parseInt(objs[ind++] != null ? String.valueOf(objs[ind-1]) : "0"));
		cloudRdsInstance.setRam(Integer.parseInt(objs[ind++] != null ? String.valueOf(objs[ind-1]) : "0"));
		cloudRdsInstance.setVolumeSize(Integer.parseInt(objs[ind++] != null ? String.valueOf(objs[ind-1]) : "0"));
		cloudRdsInstance.setRdsIp(String.valueOf(objs[ind++]));
		cloudRdsInstance.setPayType(String.valueOf(objs[ind++]));
		cloudRdsInstance.setChargeState(String.valueOf(objs[ind++]));
		cloudRdsInstance.setCreateTime((Date) objs[ind++]);
		cloudRdsInstance.setEndTime((Date) objs[ind++]);
		cloudRdsInstance.setDcName(String.valueOf(objs[ind++]));
		cloudRdsInstance.setRdsStatus(String.valueOf(objs[ind++]));
		cloudRdsInstance.setPrjId(String.valueOf(objs[ind++]));
		cloudRdsInstance.setDcId(String.valueOf(objs[ind++]));
		cloudRdsInstance.setVolumeTypeName(String.valueOf(objs[ind++]));
		cloudRdsInstance.setPayTypeStr(CloudResourceUtil.escapePayType(cloudRdsInstance.getPayType()));
		cloudRdsInstance.setStatusStr(CloudResourceUtil.escapseChargeState(cloudRdsInstance.getChargeState()));
		if (CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(cloudRdsInstance.getChargeState())) {
			cloudRdsInstance.setStatusStr(DictUtil.getStatusByNodeEn("rds", cloudRdsInstance.getRdsStatus()));
		}
		return cloudRdsInstance;
	}
	/**
	 * 根据数据库实例获取详情
	 * @param rdsId -- 数据库实例ID
	 * @return
	 * @throws Exception
	 */
	public CloudRDSInstance getRdsById(String rdsId) throws Exception {
		StringBuffer sql = new StringBuffer();
		
		sql.append("   SELECT cr.rds_id,                                                ");
		sql.append("   cr.rds_name,                                                     ");
		sql.append("   cr.rds_status,                                                   ");
		sql.append("   cr.config_id,                                                    ");
		sql.append("   cr.volume_size,                                                  ");
		sql.append("   cr.volume_type,                                                  ");
		sql.append("   cvt.type_name as volumeTypeName,                                 ");
		sql.append("   cn.net_id as netId,                                              ");
		sql.append("   cn.net_name as netName,                                          ");
		sql.append("   cs.subnet_id as subnetId,                                        ");
		sql.append("   cs.subnet_name as subnetName,                                    ");
		sql.append("   cr.create_time,                                                  ");
		sql.append("   cr.end_time,                                                     ");
		sql.append("   cr.rds_description,                                              ");
		sql.append("   cr.is_master,                                                    ");
		sql.append("   cds.name as version,                                             ");
		sql.append("   cd.name as type,                                                 ");
		sql.append("   (select count(1)                                                 ");
		sql.append("   from cloud_rdsinstance cr1                                       ");
		sql.append("   where cr1.master_id = cr.rds_id                                  ");
		sql.append("   and cr1.is_deleted = '0' and cr1.is_visible = '1'                ");
		sql.append("   ) as slaveCount,                                                 ");
		sql.append("   cf.flavor_vcpus,                                                 ");
		sql.append("   cf.flavor_ram,                                                   ");
		sql.append("   dc.dc_name as dcName,                                            ");
		sql.append("   cr.rds_ip,                                                       ");
		sql.append("   cr.pay_type,                                                     ");
		sql.append("   cr.charge_state,                                                 ");
		sql.append("   cp.prj_name as prjName,                                          ");
		sql.append("   cr.prj_id,                                                       ");
		sql.append("   cr.dc_id,                                                        ");
		sql.append("   cr.version_id,                                                   ");
		sql.append("   crc.config_name,                                                 ");
		sql.append("   cs.cidr,                                                         ");
		sql.append("   cr.master_id,                                                    ");
		sql.append("   cri.rds_name as masterName,                                      ");
		sql.append("   cr.vm_id                                                         ");
		sql.append("   FROM cloud_rdsinstance cr                                        ");
		sql.append("   LEFT JOIN cloud_project cp on cr.prj_id = cp.prj_id              ");
		sql.append("   LEFT JOIN dc_datacenter dc on cr.dc_id = dc.id                   ");
		sql.append("   LEFT JOIN cloud_volumetype cvt on cr.volume_type = cvt.type_id   ");
		sql.append("   LEFT JOIN cloud_network cn on cr.net_id = cn.net_id              ");
		sql.append("   LEFT JOIN cloud_subnetwork cs on cr.subnet_id = cs.subnet_id     ");
		sql.append("   LEFT JOIN cloud_datastoreversion cds on cr.version_id = cds.id   ");
		sql.append("   LEFT JOIN cloud_datastore cd on cds.datastore_id = cd.id         ");
		sql.append("   LEFT JOIN cloud_flavor cf on cr.flavor_id = cf.flavor_id         ");
		sql.append("   LEFT JOIN cloud_rdsconfigfile crc on cr.config_id = crc.config_id");
		sql.append("   LEFT JOIN cloud_rdsinstance cri on cr.master_id = cri.rds_id     ");
		sql.append("   WHERE cr.is_visible = '1'                                        ");
		sql.append("   and cr.is_deleted = '0'                                          ");
		sql.append("   and cr.rds_id = ?                                                ");

		CloudRDSInstance cloudRdsInstance = null;
		javax.persistence.Query query = cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(), new Object[] { rdsId });
		@SuppressWarnings("rawtypes")
		List list = new ArrayList();
		if (null != query) {
			list = query.getResultList();
		}
		if (null != list && list.size() == 1) {
			Object[] objs = (Object[]) list.get(0);
			cloudRdsInstance = new CloudRDSInstance();
			int ind = 0;
			cloudRdsInstance.setRdsId(String.valueOf(objs[ind++]));
			cloudRdsInstance.setRdsName(String.valueOf(objs[ind++]));
			cloudRdsInstance.setRdsStatus(String.valueOf(objs[ind++]));
			cloudRdsInstance.setConfigId(String.valueOf(objs[ind++]));
			cloudRdsInstance.setVolumeSize(Integer.parseInt(objs[ind++] != null ? String.valueOf(objs[ind-1]) : "0"));
			cloudRdsInstance.setVolumeType(String.valueOf(objs[ind++]));
			cloudRdsInstance.setVolumeTypeName(String.valueOf(objs[ind++]));
			cloudRdsInstance.setNetId(String.valueOf(objs[ind++]));
			cloudRdsInstance.setNetName(String.valueOf(objs[ind++]));
			cloudRdsInstance.setSubnetId(String.valueOf(objs[ind++]));
			cloudRdsInstance.setSubnetName(String.valueOf(objs[ind++]));
			cloudRdsInstance.setCreateTime((Date) objs[ind++]);
			cloudRdsInstance.setEndTime((Date) objs[ind++]);
			cloudRdsInstance.setRdsDescription(String.valueOf(objs[ind++] == null?"":objs[ind-1]));
			cloudRdsInstance.setIsMaster(String.valueOf(objs[ind++]));
			cloudRdsInstance.setVersion(String.valueOf(objs[ind++]));
			cloudRdsInstance.setType(String.valueOf(objs[ind++]));
			cloudRdsInstance.setSlaveCount(Integer.parseInt(objs[ind++] != null ? String.valueOf(objs[ind-1]) : "0"));
			cloudRdsInstance.setCpu(Integer.parseInt(objs[ind++] != null ? String.valueOf(objs[ind-1]) : "0"));
			cloudRdsInstance.setRam(Integer.parseInt(objs[ind++] != null ? String.valueOf(objs[ind-1]) : "0"));
			cloudRdsInstance.setDcName(String.valueOf(objs[ind++]));
			cloudRdsInstance.setRdsIp(String.valueOf(objs[ind++]));
			cloudRdsInstance.setPayType(String.valueOf(objs[ind++]));
			cloudRdsInstance.setChargeState(String.valueOf(objs[ind++]));
			cloudRdsInstance.setPrjName(String.valueOf(objs[ind++]));
			cloudRdsInstance.setPrjId(String.valueOf(objs[ind++]));
			cloudRdsInstance.setDcId(String.valueOf(objs[ind++]));
			cloudRdsInstance.setVersionId(String.valueOf(objs[ind++]));
			cloudRdsInstance.setConfigName(String.valueOf(objs[ind++]));
			cloudRdsInstance.setSubnetCidr(String.valueOf(objs[ind++]));
			cloudRdsInstance.setMasterId(String.valueOf(objs[ind++]));
			cloudRdsInstance.setMasterName(String.valueOf(objs[ind++]));
			cloudRdsInstance.setVmId(String.valueOf(objs[ind++]));
			cloudRdsInstance.setPayTypeStr(CloudResourceUtil.escapePayType(cloudRdsInstance.getPayType()));
			cloudRdsInstance.setStatusStr(CloudResourceUtil.escapseChargeState(cloudRdsInstance.getChargeState()));
			if (CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(cloudRdsInstance.getChargeState())) {
				cloudRdsInstance.setStatusStr(DictUtil.getStatusByNodeEn("rds", cloudRdsInstance.getRdsStatus()));
			}
		}
		return cloudRdsInstance;
	}
	/**
	 * 编辑数据库实例 （名称和描述） ------------------
	 * 
	 * @param cloudRDSInstance
	 * 							-- 修改的数据库实例的信息
	 * @throws AppException
	 */
	public void modifyRdsInstance(CloudRDSInstance cloudRDSInstance) throws AppException {
		try {
			BaseCloudRDSInstance rds = new BaseCloudRDSInstance();
			rds = cloudRDSInstanceDao.findOne(cloudRDSInstance.getRdsId());
			if (!cloudRDSInstance.getRdsName().equals(rds.getRdsName())) {
				openstackRDSInstanceService.edit(rds.getDcId(), rds.getPrjId(), rds.getRdsId(), cloudRDSInstance.getRdsName());
			}

			rds.setRdsName(cloudRDSInstance.getRdsName());
			rds.setRdsDescription(cloudRDSInstance.getRdsDescription());

			cloudRDSInstanceDao.merge(rds);

		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}
	}
	/**
	 * 重启数据库实例
	 * 
	 * @param cloudRDSInstance
	 * 							-- 需要重启的数据库实例的信息
	 * 
	 * @throws AppException
	 */
	public void restart(CloudRDSInstance cloudRDSInstance) throws AppException {
		try {
			openstackRDSInstanceService.restartRdsInstance(cloudRDSInstance.getDcId(), 
					cloudRDSInstance.getPrjId(), cloudRDSInstance.getRdsId());

			BaseCloudRDSInstance rds = new BaseCloudRDSInstance();
			rds = cloudRDSInstanceDao.findOne(cloudRDSInstance.getRdsId());
			rds.setRdsStatus("REBOOT");

			cloudRDSInstanceDao.merge(rds);

			JSONObject json = new JSONObject();
			json.put("rdsId", rds.getRdsId());
			json.put("dcId", rds.getDcId());
			json.put("prjId", rds.getPrjId());
			json.put("rdsStatus", rds.getRdsStatus());
			json.put("isNeedAttach", "0");
			json.put("count", "0");

			final JSONObject data = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.rdsKey, data.toJSONString());
				}
				
			});

		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}
	}
	/**
	 * 从库提升为主库
	 * 
	 * @param cloudRDSInstance
	 * 							-- 从库信息
	 * @throws AppException
	 */
	public void detachReplica(CloudRDSInstance cloudRDSInstance) throws AppException{
		try{
			openstackRDSInstanceService.detachReplica(cloudRDSInstance.getDcId(), 
					cloudRDSInstance.getPrjId(), cloudRDSInstance.getRdsId());

			BaseCloudRDSInstance rds = new BaseCloudRDSInstance();
			rds = cloudRDSInstanceDao.findOne(cloudRDSInstance.getRdsId());
			rds.setRdsStatus("DETACH");
			cloudRDSInstanceDao.merge(rds);
			
			JSONObject json = new JSONObject();
			json.put("rdsId", rds.getRdsId());
			json.put("dcId", rds.getDcId());
			json.put("prjId", rds.getPrjId());
			json.put("rdsName", rds.getRdsName());
			json.put("rdsStatus", rds.getRdsStatus());
			json.put("count", "0");

			final JSONObject data = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.rdsKey, data.toJSONString());
				}
				
			});
		}catch(AppException e){
			throw e;
		}catch(Exception e){
			throw new AppException("error.openstack.message");
		}
	}
	/**
	 * 修改云数据库实例的配置信息
	 * 
	 * @param cloudRDSInstance
	 * 							-- 数据库实例信息
	 * @throws AppException
	 */
	public EayunResponseJson modifyRdsInstanceConfiguraion(CloudRDSInstance cloudRDSInstance) throws AppException {
		// 解绑原来的配置文件
		try{
			BaseCloudRDSInstance rds = new BaseCloudRDSInstance();
			EayunResponseJson responsejson = new EayunResponseJson();
			// 检查该配置文件是否被删除
			StringBuffer sql = new StringBuffer();
			sql.append("   SELECT COUNT(1)               ");
			sql.append("   FROM cloud_rdsconfigfile      ");
			sql.append("   WHERE config_projectid = ?    ");
			sql.append("   AND config_datacenterid = ?   ");
			sql.append("   AND config_id = ?             ");
			javax.persistence.Query query = cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(), new Object[]{cloudRDSInstance.getPrjId(), cloudRDSInstance.getDcId(), cloudRDSInstance.getConfigId()});
			if(query != null){
				int count = Integer.valueOf(query.getSingleResult().toString());
				if(count == 0){
					responsejson.setRespCode(ConstantClazz.ERROR_CODE);
					responsejson.setMessage("该配置文件不存在，请选择其他配置文件");
					return responsejson;
				}
			}

			rds = cloudRDSInstanceDao.findOne(cloudRDSInstance.getRdsId());
			rds.setRdsStatus("REBOOT");
			cloudRDSInstanceDao.merge(rds);
			openstackConfigurationGroupService.detachConfigurationGroupToInstance(cloudRDSInstance.getDcId(),
					cloudRDSInstance.getPrjId(), cloudRDSInstance.getRdsId());
			RDSInstance rdsInstance = openstackRDSInstanceService.getById(cloudRDSInstance.getDcId(),
					cloudRDSInstance.getPrjId(), cloudRDSInstance.getRdsId());
			// 需要重启
			if("RESTART_REQUIRED".equals(rdsInstance.getStatus())){
				// 重启数据库实例
				openstackRDSInstanceService.restartRdsInstance(cloudRDSInstance.getDcId(), 
						cloudRDSInstance.getPrjId(), cloudRDSInstance.getRdsId());
				JSONObject json = new JSONObject();
				json.put("rdsId", rds.getRdsId());
				json.put("dcId", rds.getDcId());
				json.put("prjId", rds.getPrjId());
				json.put("configId", cloudRDSInstance.getConfigId());
				json.put("rdsStatus", "REBOOT");
				json.put("isNeedAttach", "1");
				json.put("count", "0");

				final JSONObject data = json;
				TransactionHookUtil.registAfterCommitHook(new Hook() {
					@Override
					public void execute() {
						jedisUtil.addUnique(RedisKey.rdsKey, data.toJSONString());
					}
				});
			}else{
				this.rdsInstanceAttachConfiguration(cloudRDSInstance);
			}
			responsejson.setRespCode(ConstantClazz.SUCCESS_CODE);
			return responsejson;
		}catch(AppException e){
			throw e;
		}catch(Exception e){
			throw new AppException("error.openstack.message");
		}
	}
	/**
	 * 云数据库实例的绑定操作
	 * 
	 * @param cloudRDSInstance
	 * 						-- 需要操作的数据库实例的信息
	 * @throws AppException
	 */
	public void rdsInstanceAttachConfiguration(CloudRDSInstance cloudRDSInstance) throws AppException {
		try{
			// 绑定修改后的配置文件
			openstackConfigurationGroupService.attachConfigurationGroupToInstance(cloudRDSInstance.getDcId(), 
					cloudRDSInstance.getPrjId(), cloudRDSInstance.getConfigId(), cloudRDSInstance.getRdsId());
			RDSInstance rdsInstance = openstackRDSInstanceService.getById(cloudRDSInstance.getDcId(),
					cloudRDSInstance.getPrjId(), cloudRDSInstance.getRdsId());
			// 需要重启
			if("RESTART_REQUIRED".equals(rdsInstance.getStatus())){
				// 重启数据库实例
				openstackRDSInstanceService.restartRdsInstance(cloudRDSInstance.getDcId(), 
						cloudRDSInstance.getPrjId(), cloudRDSInstance.getRdsId());
				JSONObject json = new JSONObject();
				json.put("rdsId", cloudRDSInstance.getRdsId());
				json.put("dcId", cloudRDSInstance.getDcId());
				json.put("prjId", cloudRDSInstance.getPrjId());
				json.put("configId", cloudRDSInstance.getConfigId());
				json.put("rdsStatus", "REBOOT");
				json.put("isNeedAttach", "2");
				json.put("count", "0");

				final JSONObject data = json;
				TransactionHookUtil.registAfterCommitHook(new Hook() {
					@Override
					public void execute() {
						jedisUtil.addUnique(RedisKey.rdsKey, data.toJSONString());
					}
					
				});
			}else{
				// 修改配置文件成功
				BaseCloudRDSInstance rds = new BaseCloudRDSInstance();
				rds = cloudRDSInstanceDao.findOne(cloudRDSInstance.getRdsId());
				rds.setRdsStatus("ACTIVE");
				rds.setConfigId(cloudRDSInstance.getConfigId());
				cloudRDSInstanceDao.merge(rds);
			}
		}catch(AppException e){
			throw e;
		}catch(Exception e){
			throw new AppException("error.openstack.message");
		}
	}
	
	public boolean checkRdsNameExist(String rdsId, String rdsName, String prjId){
		// 查询cloud_rdsinstance是否有重名
		Object [] params = new Object[10];
		int index = 0;
		StringBuffer sql = new StringBuffer();

		sql.append("   select COUNT(1)                ");
		sql.append("   from cloud_rdsinstance cri     ");
		sql.append("   where cri.prj_id = ?           ");
		sql.append("   and binary(cri.rds_name) = ?   ");
		sql.append("   and is_deleted = '0'           ");
		sql.append("   and is_visible = '1'           ");
		params[index++] = prjId;
		params[index++] = rdsName;
		if(rdsId != null){
			sql.append("   and cri.rds_id <> ?            ");
			params[index++] = rdsId;
		}
		Object[] args = new Object[index];
		System.arraycopy(params, 0, args, 0, index);
		javax.persistence.Query query = cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(), args);
		if(null != query){
			int count = Integer.valueOf(query.getSingleResult().toString());
			if(count > 0){
				// 查询订单表
				return false;
			}
		}
		return checkRdsNameInOrder(rdsName, prjId);
	}

	public boolean checkRdsNameInOrder(String rdsName, String prjId) {
		StringBuffer sql = new StringBuffer();
		
		sql.append("   select count(1)                                         ");
		sql.append("   from cloudorder_rdsinstance cri                         ");
		sql.append("   LEFT JOIN order_info oi on cri.order_no = oi.order_no   ");
		sql.append("   where oi.order_type = 0                                 ");
		sql.append("   and (oi.order_state = 1 or oi.order_state = 2)          ");
		sql.append("   and cri.rds_name = ?                                    ");
		sql.append("   and cri.prj_id = ?                                      ");

		javax.persistence.Query query = cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(), new String[]{rdsName, prjId});
		if(null != query){
			int count = Integer.valueOf(query.getSingleResult().toString());
			if(count > 0)
				return false;
		}
		return true;
	}
	/**
	 * 校验主库或从库是否超过配额，如果超过，则返回超配信息，正常发则返回null
	 * @param cloudOrder(prjId, isMaster, masterId)
	 * @return
	 */
	public String checkInstanceQuota (CloudOrderRDSInstance cloudOrder) {
        BaseCloudProject basePrj = projectService.findProject(cloudOrder.getPrjId());
        int orderCount = getRdsInstanceCountInOrder(cloudOrder);
        int usedCount = 0;
        if (cloudOrder.getIsMaster().equals("1")) {
            usedCount = cloudRDSInstanceDao.getCountByPrjId(cloudOrder.getPrjId()) + orderCount +
					this.getDetachRdsInstanceCountByMasterId(cloudOrder.getPrjId(), null);
            if (basePrj.getMaxMasterInstance() - usedCount <= 0) {
                return "OUT_OF_MASTER_QUOTA";
            }
        } else {
            usedCount = cloudRDSInstanceDao.getSlaveCountByMasterId(cloudOrder.getMasterId()) + orderCount -
					this.getDetachRdsInstanceCountByMasterId(cloudOrder.getPrjId(),  cloudOrder.getMasterId());
            if (basePrj.getMaxSlaveIOfCluster() - usedCount <= 0) {
                return "OUT_OF_SLAVE_QUOTA";
            }
        }
        return null;
	}
	/**
	 * 获取订单中待创建或创建中的的资源数量
	 * @param cloudOrder(prjId, isMaster, masterId)
	 * @return
	 */
	private int getRdsInstanceCountInOrder(CloudOrderRDSInstance cloudOrder){
        StringBuffer sql = new StringBuffer();
        
        sql.append("   SELECT                                           ");
        sql.append("        count(1)                                    ");
        sql.append("   FROM cloudorder_rdsinstance cor                  ");
        sql.append("   LEFT JOIN order_info oi                          ");
        sql.append("    on oi.order_no = cor.order_no                   ");
        sql.append("   WHERE oi.order_type = '0'                        ");
        sql.append("   and (oi.order_state = 1 or oi.order_state = 2)   ");
        sql.append("   and cor.prj_id = ?                               ");
        
        List<Object> values = new ArrayList<Object>();
        values.add(cloudOrder.getPrjId());
        if(!StringUtil.isEmpty(cloudOrder.getIsMaster()) && cloudOrder.getIsMaster().equals("0")){
            sql.append(" and cor.is_master = 0 ");
            sql.append(" and cor.master_id =  ? ");
            values.add(cloudOrder.getMasterId());
        }
        Query query = cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(), values.toArray());
        Object result = query.getSingleResult();
        int orderCount = result == null ? 0 : Integer.parseInt(result.toString());
        return orderCount;
    }
	
	/**
	 * 删除云数据库实例
	 * @param cloudRdsInstance -- 要删除的云数据库实例的信息
	 * @param sessionUser -- 当前用户信息
	 * @throws Exception
	 * 
	 */
	public EayunResponseJson deleteRdsInstance(CloudRDSInstance cloudRdsInstance, SessionUserInfo sessionUser)
			throws Exception {
		EayunResponseJson responseJson = new EayunResponseJson();
		// 删除前的验证操作 1:主库存在从库；2：该资源有未完成的订单
		int count = checkForDel(cloudRdsInstance);
		switch (count) {
			case 1:
				responseJson.setMessage("该实例已创建从库，请先删除从库后再进行操作！");
				break;
			case 2:
				responseJson.setMessage("该资源有未完成的订单，请取消订单后再进行删除操作！");
				break;
			default:
				break;
		}
		if (count > 0) {
			responseJson.setRespCode(ConstantClazz.ERROR_CODE);
			return responseJson;
		}
		// 删除实例对应的日志
		openstackRDSInstanceService.delete(cloudRdsInstance.getDcId(), 
				cloudRdsInstance.getPrjId(), cloudRdsInstance.getRdsId());
		BaseCloudRDSInstance rds = new BaseCloudRDSInstance();
		rds = cloudRDSInstanceDao.findOne(cloudRdsInstance.getRdsId());
		rds.setRdsStatus("SHUTDOWN");
		rds.setDeleteTime(new Date());
		rds.setDeleteUser(sessionUser.getUserName());

		cloudRDSInstanceDao.merge(rds);
		
		
		JSONObject json = new JSONObject();
		json.put("rdsId", rds.getRdsId());
		json.put("dcId", rds.getDcId());
		json.put("prjId", rds.getPrjId());
		json.put("rdsStatus", rds.getRdsStatus());
		json.put("vmId", rds.getVmId()); // 用于清除报警信息
		json.put("count", "0");
		
		final JSONObject data = json;
		TransactionHookUtil.registAfterCommitHook(new Hook() {
			@Override
			public void execute() {
				jedisUtil.addUnique(RedisKey.rdsKey, data.toJSONString());
			}
			
		});
		cloudRdsInstance.setOpDate(rds.getDeleteTime());
		cloudRdsInstance.setCusId(sessionUser.getCusId());
		cloudRdsInstance.setRdsName(rds.getRdsName());
		// 按需资源删除需要发送计费消息
		if(PayType.PAYAFTER.equals(rds.getPayType())){
			rdsInstanceOptionCharge(cloudRdsInstance);
		}
		return null;
	}
	/**
	 * 删除云数据库实例后发送计费消息
	 * 
	 * @param cloudRdsInstance
	 * 						-- 删除的数据库实例的信息
	 */
	public void rdsInstanceOptionCharge(final CloudRDSInstance cloudRdsInstance) {
		TransactionHookUtil.registAfterCommitHook(new Hook() {
			@Override
			public void execute() {
				ChargeRecord record = new ChargeRecord();
				String queueName = null;
				
				record.setResourceId(cloudRdsInstance.getRdsId());
				record.setOpTime(cloudRdsInstance.getOpDate());
				record.setDatecenterId(cloudRdsInstance.getDcId());
				record.setCusId(cloudRdsInstance.getCusId());
				record.setResourceType(ResourceType.RDS);
				record.setResourceName(cloudRdsInstance.getRdsName());
				queueName = EayunQueueConstant.QUEUE_BILL_RESOURCE_DELETE;
				rabbitTemplate.send(queueName, JSONObject.toJSONString(record));
			}
		});
	}
	/**
	 * 删除校验
	 * @param cloudRdsInstance
	 * @return 1：该实例已创建从库；2：该资源有未完成的订单
	 */
	public int checkForDel(CloudRDSInstance cloudRdsInstance) {
		StringBuffer sql = new StringBuffer();
		if("1".equals(cloudRdsInstance.getIsMaster())){
			sql.append("   select count(1)                                   ");
			sql.append("   from cloud_rdsinstance rds                        ");
			sql.append("   where rds.is_master = '0' and rds.master_id = ?   ");
			sql.append("   and rds.is_deleted = '0' and rds.is_visible = '1' ");
			javax.persistence.Query query = cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(), new String[]{cloudRdsInstance.getRdsId()});
			sql.delete(0, sql.length() - 1);
			if(null != query){
				int count = Integer.valueOf(query.getSingleResult().toString());
				if(count > 0)
					return 1;
			}
		}
		sql.append("   select count(1)                                                           ");
		sql.append("   from cloudorder_rdsinstance cri                                           ");
		sql.append("   LEFT JOIN order_info oi on cri.order_no = oi.order_no                     ");
		sql.append("   where (oi.order_state = '1' or oi.order_state = '2') and cri.rds_id = ?   ");
		javax.persistence.Query query = cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(), new String[]{cloudRdsInstance.getRdsId()});
		sql.delete(0, sql.length() - 1);
		if(null != query){
			int count = Integer.valueOf(query.getSingleResult().toString());
			if(count > 0)
				return 2;
		}
		return 0;
	}
	/**
	 * 通过rdsId获取实例信息
	 * @author gaoxiang
	 * @param rdsId
	 * @return
	 */
	public CloudRDSInstance getInstanceByRdsId (String rdsId) {
	    CloudRDSInstance instance = new CloudRDSInstance();
		StringBuffer hql = new StringBuffer();
		hql.append("from BaseCloudRDSInstance cr where cr.rdsId = ? and cr.isVisible = '1' and cr.isDeleted = '0'");
		BaseCloudRDSInstance baseRds = (BaseCloudRDSInstance)cloudRDSInstanceDao.findUnique(hql.toString(), new Object[]{rdsId});
	    if (baseRds != null) {
	        BeanUtils.copyPropertiesByModel(instance, baseRds);
	        return instance;
	    }
	    return null;
	}
	
	public EayunResponseJson getConfigList(String prjId, String versionId){
		return rdsConfigurationService.queryConfigFileByPrjAndVersion(prjId, versionId);
	}
	/**
	 * 通过私有网络id获取关联的存在的云数据库实例数量
	 * @author gaoxiang
	 * @param netId
	 * @return
	 */
	public int getRdsInstanceCountByNetId (String netId) {
	    StringBuffer sql = new StringBuffer();
	    sql.append(" SELECT                    ");
	    sql.append("   COUNT(*)                ");
	    sql.append(" FROM cloud_rdsinstance    ");
	    sql.append(" WHERE                     ");
	    sql.append("   net_id = ?              ");
	    sql.append("   AND is_deleted = '0'    ");
	    sql.append("   AND is_visible = '1'    ");
	    Query query = cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(), new Object[] {netId});
	    if (null != query && !StringUtil.isEmpty(query.getSingleResult().toString())) {
	        return Integer.parseInt(query.getSingleResult().toString());
	    }
	    return 0;
	}
	/**
	 * 通过私有网络id获取关联的待支付或处理中的云数据库实例数量
	 * @author gaoxiang
	 * @param netId
	 * @return
	 */
	public int getRdsInstanceToBeCreatedByNetId (String netId) {
	    StringBuffer sql = new StringBuffer();
	    sql.append(" SELECT                                                    ");
	    sql.append("   COUNT(*)                                                ");
	    sql.append(" FROM cloudorder_rdsinstance instance                      ");
	    sql.append(" LEFT JOIN order_info info                                 ");
	    sql.append(" ON instance.order_no = info.order_no                      ");
	    sql.append(" WHERE                                                     ");
	    sql.append("   instance.net_id = ?                                     ");
	    sql.append("   AND info.order_type = '0'                               ");
	    sql.append("   AND (info.order_state = '1' OR info.order_state = '2')  ");
	    Query query = cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(), new Object[] {netId});
	    if (null != query && !StringUtil.isEmpty(query.getSingleResult().toString())) {
	        return Integer.parseInt(query.getSingleResult().toString());
	    }
	    return 0;
	}
	/**
	 * 通过受管子网id获取关联的存在的云数据库实例的数量
	 * @author gaoxiang
	 * @param subnetId
	 * @return
	 */
	public int getRdsInstanceCountBySubnet (String subnetId) {
	    StringBuffer sql = new StringBuffer();
	    sql.append(" SELECT                    ");
	    sql.append("   COUNT(*)                ");
	    sql.append(" FROM cloud_rdsinstance    ");
	    sql.append(" WHERE                     ");
	    sql.append("   subnet_id = ?           ");
	    sql.append("   AND is_deleted = '0'    ");
	    sql.append("   AND is_visible = '1'    ");
	    Query query = cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(), new Object[] {subnetId});
	    if (null != query && !StringUtil.isEmpty(query.getSingleResult().toString())) {
	        return Integer.parseInt(query.getSingleResult().toString());
	    }
	    return 0;
	}
	/**
	 * 通过受管子网id获取关联的待支付或处理中的云数据库实例的数量
	 * @author gaoxiang
	 * @param subnetId
	 * @return
	 */
	public int getRdsInstanceToBeCreatedBySubnet (String subnetId) {
	    StringBuffer sql = new StringBuffer();
	    sql.append(" SELECT                                                    ");
	    sql.append("   COUNT(*)                                                ");
	    sql.append(" FROM cloudorder_rdsinstance instance                      ");
	    sql.append(" LEFT JOIN order_info info                                 ");
	    sql.append(" ON instance.order_no = info.order_no                      ");
	    sql.append(" WHERE                                                     ");
	    sql.append("   instance.subnet_id = ?                                  ");
	    sql.append("   AND info.order_type = '0'                               ");
	    sql.append("   AND (info.order_state = '1' OR info.order_state = '2')  ");
	    Query query = cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(), new Object[] {subnetId});
        if (null != query && !StringUtil.isEmpty(query.getSingleResult().toString())) {
            return Integer.parseInt(query.getSingleResult().toString());
        }
	    return 0;
	}

	/**
	 * 获取正在分离中的实例
	 * @param prjId 项目ID
	 * @param masterId 主库实例ID
     * @return
     */
	private int getDetachRdsInstanceCountByMasterId(String prjId, String masterId){
		StringBuffer sql = new StringBuffer();
		sql.append("   SELECT count(1)                ");
		sql.append("   FROM cloud_rdsinstance cr      ");
		sql.append("   WHERE cr.prj_id = ?            ");
		sql.append("   AND cr.rds_status = 'DETACH'   ");
		sql.append("   AND cr.is_deleted = '0'        ");
		sql.append("   AND cr.is_visible = '1'        ");
		if(null == masterId){
			sql.append("   AND cr.is_master = ?       ");
		}else{
			sql.append("   AND cr.master_id = ?       ");
		}
		javax.persistence.Query query = cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(),new Object[]{prjId, masterId == null ? "0":masterId});
		if(query != null){
			int count = Integer.valueOf(query.getSingleResult().toString());
			if(count > 0)
				return count;
		}
		return 0;
	}

	/**
	 *
	 * @param rdsList -- 实例集合
	 * @param status -- 修改后的状态
     */
	public void updateRdsInstanceStatus(List<CloudRDSInstance> rdsList, String status){
		if(rdsList != null && rdsList.size() > 0){
			StringBuffer hql = new StringBuffer("update BaseCloudRDSInstance set rdsStatus = ? where rdsId = ?");
			for(CloudRDSInstance rdsInstance : rdsList){
				cloudRDSInstanceDao.executeUpdate(hql.toString(), new Object[]{status, rdsInstance.getRdsId()});
			}
		}
	}

	/**
	 * 根据云硬盘类型名称获取云硬盘中文名称
	 * @param volumeType
	 * @return
     */
	public String getVolumeTypeStrByVolumeTypeName(String volumeType){
		if(volumeType == null)
			return null;
		switch (volumeType) {
			case "Normal":
				return "普通型";
			case "Medium":
				return "性能型";
			default:
				return null;
		}
	}

	/**
	 * 获取待支付和处理中的订单的数量（RDS）
	 * @param prjId -- 项目ID
	 * @param isMaster -- 是否为主库 1：主库，0：从库
     * @return
     */
	private int getRdsInstanceCountInOrder(String prjId, String isMaster){
		StringBuffer sql = new StringBuffer();

		sql.append("   SELECT                                           ");
		sql.append("        count(1)                                    ");
		sql.append("   FROM cloudorder_rdsinstance cor                  ");
		sql.append("   LEFT JOIN order_info oi                          ");
		sql.append("    on oi.order_no = cor.order_no                   ");
		sql.append("   WHERE oi.order_type = '0'                        ");
		sql.append("   and (oi.order_state = 1 or oi.order_state = 2)   ");
		sql.append("   and cor.prj_id = ?                               ");
		sql.append("   and cor.is_master = ?                            ");

		List<Object> values = new ArrayList<Object>();
		values.add(prjId);
		values.add(isMaster);
		Query query = cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(), values.toArray());
		Object result = query.getSingleResult();
		int orderCount = result == null ? 0 : Integer.parseInt(result.toString());
		return orderCount;
	}
}
