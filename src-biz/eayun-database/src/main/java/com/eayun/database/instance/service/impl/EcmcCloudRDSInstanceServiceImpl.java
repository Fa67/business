package com.eayun.database.instance.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.PayType;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.CloudResourceUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.Hook;
import com.eayun.database.instance.dao.CloudRDSInstanceDao;
import com.eayun.database.instance.model.BaseCloudRDSInstance;
import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.database.instance.service.EcmcCloudRDSInstanceService;
import com.eayun.eayunstack.service.OpenstackRDSInstanceService;

/**
 * ECMC云数据库实例相关操作service实现类
 *                       
 * @Filename: EcmcCloudRDSInstanceServiceImpl.java
 * @Description: 
 * @Version: 1.0
 * @Author: LiuZhuangzhuang
 * @Email: zhuangzhuang.liu@eayun.com
 * @History:<br>
 *<li>Date: 2017年2月22日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Transactional
@Service
public class EcmcCloudRDSInstanceServiceImpl extends BaseRDSInstanceService 
				implements EcmcCloudRDSInstanceService{

	@Autowired
	private CloudRDSInstanceDao cloudRDSInstanceDao;
	@Autowired
	private OpenstackRDSInstanceService openstackRDSInstanceService;
	@Autowired
	private JedisUtil jedisUtil;
	/**
	 * @author zhouhaitao
	 * --------------------------<br>
	 * @desc:
	 * 统计数据中心的RDS使用情况 <br>
	 * 		CPU：CPU的已使用量<br>
	 * 		Ram: 内存的已使用量<br>
	 * 		Volume:硬盘容量的已使用量<br>
	 * 		Instances:已创建的RDS实力数量<br>
	 * 
	 * @param dcId 数据中心ID
	 * @return
	 */
	public Map<String,String> getRDSInstanceUsedInfoByDcId(String dcId){
		Map<String,String> map = new HashMap<String,String>();
		StringBuffer sql = new StringBuffer();
		
		sql.append("			SELECT                                   ");
		sql.append("				COUNT(1) AS instanceUsed,            ");
		sql.append("				SUM(cf.flavor_vcpus) AS cpuUsed,     ");
		sql.append("				SUM(cf.flavor_ram) AS ramUsed,       ");
		sql.append("				SUM(crds.volume_size) AS volumeUsed  ");
		sql.append("			FROM                                     ");
		sql.append("				cloud_rdsinstance crds               ");
		sql.append("			LEFT JOIN cloud_flavor cf                ");
		sql.append("			ON crds.flavor_id = cf.flavor_id         ");
		sql.append("			AND crds.dc_id = cf.dc_id                ");
		sql.append("			WHERE crds.is_deleted = '0'              ");
		sql.append("			AND crds.dc_id = ?                       ");
		
		javax.persistence.Query query = cloudRDSInstanceDao.createSQLNativeQuery(
				sql.toString(), new Object[]{dcId});
		@SuppressWarnings("rawtypes")
		List resultList = query.getResultList();
		
		if(null != resultList && resultList.size() ==1){
			Object [] objs = (Object [])resultList.get(0);
			map.put("Instances", objs[0] != null ? String.valueOf(objs[0]) : "0");
			map.put("CPU", objs[1] != null ? String.valueOf(objs[1]) : "0");
			map.put("Ram", objs[2] != null ? String.valueOf(objs[2]) : "0");
			map.put("Volumes", objs[3] != null ? String.valueOf(objs[3]) : "0");
		}
		return map;
	}
	
	public Page getList(Page page, ParamsMap paramsMap) throws Exception {
	    List<String> params = new ArrayList<String>();
	    StringBuffer sql = new StringBuffer();
	    String queryType = paramsMap.getParams().get("queryType") != null ? paramsMap.getParams().get("queryType").toString() : "";
	    String queryValue = paramsMap.getParams().get("queryValue") != null ? paramsMap.getParams().get("queryValue").toString() : "";
	    String dcId = paramsMap.getParams().get("dcId") != null ? paramsMap.getParams().get("dcId").toString() : "";
	    String status = paramsMap.getParams().get("status") != null ? paramsMap.getParams().get("status").toString() : "";
	    String version = paramsMap.getParams().get("version") != null ? paramsMap.getParams().get("version").toString() : "";
	    sql.append(" SELECT DISTINCT ");
	    sql.append("   instance.rds_id                                                 ");//0
	    sql.append("   ,instance.rds_name                                              ");//1
	    sql.append("   ,instance.rds_status                                            ");//2
	    sql.append("   ,instance.is_master                                             ");//3
	    sql.append("   ,instance.prj_id                                                ");//4
	    sql.append("   ,prj.prj_name                                                   ");//5
	    sql.append("   ,prj.customer_id                                                ");//6
	    sql.append("   ,customer.cus_org                                               ");//7
	    sql.append("   ,instance.dc_id                                                 ");//8
	    sql.append("   ,dc.dc_name                                                     ");//9
	    sql.append("   ,instance.version_id                                            ");//10
	    sql.append("   ,version.name as version                                        ");//11
	    sql.append("   ,datastore.name as type                                         ");//12
	    sql.append("   ,instance.flavor_id                                             ");//13
	    sql.append("   ,flavor.flavor_vcpus                                            ");//14
	    sql.append("   ,flavor.flavor_ram                                              ");//15
	    sql.append("   ,instance.volume_size                                           ");//16
	    sql.append("   ,instance.rds_ip                                                ");//17
	    sql.append("   ,instance.create_time                                           ");//18
	    sql.append("   ,instance.pay_type                                              ");//19
	    sql.append("   ,instance.charge_state                                          ");//20
	    sql.append("   ,instance.end_time                                              ");//21
	    sql.append(" FROM                                                              ");
	    sql.append("   cloud_rdsinstance instance                                      ");
	    sql.append(" LEFT JOIN                                                         ");
	    sql.append("   dc_datacenter dc ON instance.dc_id = dc.id                      ");
	    sql.append(" LEFT JOIN                                                         ");
	    sql.append("   cloud_project prj ON instance.prj_id = prj.prj_id               ");
	    sql.append(" LEFT JOIN                                                         ");
	    sql.append("   sys_selfcustomer customer ON prj.customer_id = customer.cus_id  ");
	    sql.append(" LEFT JOIN                                                         ");
	    sql.append("   cloud_datastoreversion version ON instance.version_id = version.id ");
	    sql.append(" LEFT JOIN                                                         ");
	    sql.append("   cloud_datastore datastore ON version.datastore_id = datastore.id ");
	    sql.append(" LEFT JOIN                                                         ");
	    sql.append("   cloud_flavor flavor ON instance.flavor_id = flavor.flavor_id    ");
	    sql.append(" LEFT JOIN                                                         ");
	    sql.append("   cloud_rdsinstance slave ON instance.rds_id = slave.master_id    ");
	    sql.append(" WHERE                                                             ");
	    sql.append("   instance.is_visible = '1'                                       ");
	    sql.append("   AND instance.is_deleted = '0'                                   ");
	    sql.append("   AND instance.is_master = '1'                                    ");
	    if (!StringUtil.isEmpty(dcId) && !"null".equals(dcId)) {
	        sql.append(" AND instance.dc_id = ? ");
	        params.add(dcId);
	    }
	    if (!StringUtil.isEmpty(version) && !"null".equals(version)) {
	        sql.append(" AND instance.version_id = ? ");
	        params.add(version);
	    }
	    if (!StringUtil.isEmpty(status) && !"null".equals(status)) {
	        switch (status) {
	        case "1":
	            sql.append(" AND (instance.charge_state = ? OR slave.charge_state = ?) ");
	            break;
	        case "2":
	            sql.append(" AND ((instance.charge_state = '3' or instance.charge_state = ?) ");
	            sql.append(" OR (slave.charge_state = '3' or slave.charge_state = ?)) ");
	            break;
            default:
	            sql.append(" AND ((instance.charge_state = '0' AND instance.rds_status = ?) ");
	            sql.append(" OR (slave.charge_state = '0' AND slave.rds_status = ?)) ");
	        }
	        params.add(status);
	        params.add(status);
	    }
	    if (!StringUtil.isEmpty(queryType)) {
	        switch (queryType) {
	        case "rdsName":
	            sql.append(" AND (instance.rds_name like ? OR slave.rds_name like ?) ");
	            queryValue = queryValue.replaceAll("\\_", "\\\\_").replaceAll("\\%", "\\\\%");
	            params.add("%" + queryValue + "%");
	            params.add("%" + queryValue + "%");
	            break;
	        case "cusOrg":
	            String[] cusOrgs = queryValue.split(",");
	            if (cusOrgs.length > 0 && !StringUtil.isEmpty(cusOrgs[0])) {
	                sql.append(" AND ( ");
	                for (String cusOrg : cusOrgs) {
	                    sql.append(" binary customer.cus_org = ? OR ");
	                    params.add(cusOrg);
	                }
	                sql.append(" 1 = 2 ) ");
	            }
	            break;
	        case "prjName":
	            String[] prjNames = queryValue.split(",");
	            if (prjNames.length > 0 && !StringUtil.isEmpty(prjNames[0])) {
	                sql.append(" AND ( ");
	                for (String prjName : prjNames) {
	                    sql.append(" binary prj.prj_name = ? OR ");
	                    params.add(prjName);
	                }
	                sql.append(" 1 = 2 ) ");
	            }
	            break;
	        case "rdsIps":
	            sql.append(" AND (instance.rds_ip like ? OR slave.rds_ip like ?) ");
	            queryValue = queryValue.replaceAll("\\_", "\\\\_").replaceAll("\\%", "\\\\%");
	            params.add("%" + queryValue + "%");
	            params.add("%" + queryValue + "%");
	            break;
	        }
	    }
	    sql.append(" ORDER BY instance.create_time DESC ");
	    QueryMap queryMap = new QueryMap();
        queryMap.setPageNum(paramsMap.getPageNumber());
        queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
	    page = cloudRDSInstanceDao.pagedNativeQuery(sql.toString(), queryMap, params.toArray());
	    List<Object> result = (List<Object>) page.getResult();
	    for (int i = 0; i < result.size(); i++) {
	        Object[] objs = (Object []) result.get(i);
	        CloudRDSInstance instance = new CloudRDSInstance();
	        int index = 0;
	        instance.setRdsId(String.valueOf(objs[index++]));
	        instance.setRdsName(String.valueOf(objs[index++]));
	        instance.setRdsStatus(String.valueOf(objs[index++]));
	        instance.setIsMaster(String.valueOf(objs[index++]));
	        instance.setPrjId(String.valueOf(objs[index++]));
	        instance.setPrjName(String.valueOf(objs[index++]));
	        instance.setCusId(String.valueOf(objs[index++]));
	        instance.setCusOrg(String.valueOf(objs[index++]));
	        instance.setDcId(String.valueOf(objs[index++]));
	        instance.setDcName(String.valueOf(objs[index++]));
	        instance.setVersionId(String.valueOf(objs[index++]));
	        instance.setVersion(String.valueOf(objs[index++]));
	        instance.setType(String.valueOf(objs[index++]));
	        instance.setFlavorId(String.valueOf(objs[index++]));
	        instance.setCpu(Integer.parseInt(null != objs[index] ? String.valueOf(objs[index]) : "0"));
	        index++;
	        instance.setRam(Integer.parseInt(null != objs[index] ? String.valueOf(objs[index]) : "0"));
	        index++;
	        instance.setVolumeSize(Integer.parseInt(null != objs[index] ? String.valueOf(objs[index]) : "0"));
	        index++;
	        instance.setRdsIp(String.valueOf(objs[index++]));
	        instance.setCreateTime((Date) objs[index++]);
	        instance.setPayType(String.valueOf(objs[index++]));
	        instance.setPayTypeStr(CloudResourceUtil.escapePayType(instance.getPayType()));
	        instance.setChargeState(String.valueOf(objs[index++]));
	        instance.setEndTime((Date) objs[index++]);
	        instance.setChildren(getSlaveByMasterId(instance.getRdsId()));
	        instance.setStatusStr(CloudResourceUtil.escapseChargeState(instance.getChargeState()));
	        if (CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(instance.getChargeState())) {
	            instance.setStatusStr(DictUtil.getStatusByNodeEn("rds", instance.getRdsStatus()));
	        }
	        result.set(i, instance);
	    }
	    return page;
	}
	
	private List<CloudRDSInstance> getSlaveByMasterId (String instanceId) {
	    StringBuffer sql = new StringBuffer();
	    sql.append(" SELECT ");
        sql.append("   instance.rds_id                                                 ");//0
        sql.append("   ,instance.rds_name                                              ");//1
        sql.append("   ,instance.rds_status                                            ");//2
        sql.append("   ,instance.is_master                                             ");//3
        sql.append("   ,master.rds_id AS masterId                                      ");//4
        sql.append("   ,master.rds_name AS masterName                                  ");//5
        sql.append("   ,instance.prj_id                                                ");//6
        sql.append("   ,prj.prj_name                                                   ");//7
        sql.append("   ,prj.customer_id                                                ");//8
        sql.append("   ,customer.cus_org                                               ");//9
        sql.append("   ,instance.dc_id                                                 ");//10
        sql.append("   ,dc.dc_name                                                     ");//11
        sql.append("   ,instance.version_id                                            ");//12
        sql.append("   ,version.name AS version                                        ");//13
        sql.append("   ,datastore.name AS type                                         ");//14
        sql.append("   ,instance.flavor_id                                             ");//15
        sql.append("   ,flavor.flavor_vcpus                                            ");//16
        sql.append("   ,flavor.flavor_ram                                              ");//17
        sql.append("   ,instance.volume_size                                           ");//18
        sql.append("   ,instance.rds_ip                                                ");//19
        sql.append("   ,instance.create_time                                           ");//20
        sql.append("   ,instance.pay_type                                              ");//21
        sql.append("   ,instance.charge_state                                          ");//22
        sql.append("   ,instance.end_time                                              ");//23
        sql.append(" FROM                                                              ");
        sql.append("   cloud_rdsinstance instance                                      ");
        sql.append(" LEFT JOIN                                                         ");
        sql.append("   dc_datacenter dc ON instance.dc_id = dc.id                      ");
        sql.append(" LEFT JOIN                                                         ");
        sql.append("   cloud_project prj ON instance.prj_id = prj.prj_id               ");
        sql.append(" LEFT JOIN                                                         ");
        sql.append("   sys_selfcustomer customer ON prj.customer_id = customer.cus_id  ");
        sql.append(" LEFT JOIN                                                         ");
        sql.append("   cloud_datastoreversion version ON instance.version_id = version.id ");
        sql.append(" LEFT JOIN                                                         ");
        sql.append("   cloud_datastore datastore ON version.datastore_id = datastore.id ");
        sql.append(" LEFT JOIN                                                         ");
        sql.append("   cloud_flavor flavor ON instance.flavor_id = flavor.flavor_id    ");
        sql.append(" LEFT JOIN                                                         ");
        sql.append("   cloud_rdsinstance master ON instance.master_id = master.rds_id  ");
        sql.append(" WHERE                                                             ");
        sql.append("   instance.is_visible = '1'                                       ");
        sql.append("   AND instance.is_deleted = '0'                                   ");
        sql.append("   AND instance.is_master = '0'                                    ");
        sql.append("   AND instance.master_id = ?                                      ");
        javax.persistence.Query query = cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(), instanceId);
        List<CloudRDSInstance> list = new ArrayList<CloudRDSInstance>();
        if (null != query) {
            List<Object> result = query.getResultList();
            if (null != result && result.size() > 0) {
                for (Object obj : result) {
                    Object[] objs = (Object []) obj;
                    CloudRDSInstance instance = new CloudRDSInstance();
                    int index = 0;
                    instance.setRdsId(String.valueOf(objs[index++]));
                    instance.setRdsName(String.valueOf(objs[index++]));
                    instance.setRdsStatus(String.valueOf(objs[index++]));
                    instance.setIsMaster(String.valueOf(objs[index++]));
                    instance.setMasterId(String.valueOf(objs[index++]));
                    instance.setMasterName(String.valueOf(objs[index++]));
                    instance.setPrjId(String.valueOf(objs[index++]));
                    instance.setPrjName(String.valueOf(objs[index++]));
                    instance.setCusId(String.valueOf(objs[index++]));
                    instance.setCusOrg(String.valueOf(objs[index++]));
                    instance.setDcId(String.valueOf(objs[index++]));
                    instance.setDcName(String.valueOf(objs[index++]));
                    instance.setVersionId(String.valueOf(objs[index++]));
                    instance.setVersion(String.valueOf(objs[index++]));
                    instance.setType(String.valueOf(objs[index++]));
                    instance.setFlavorId(String.valueOf(objs[index++]));
                    instance.setCpu(Integer.parseInt(null != objs[index] ? String.valueOf(objs[index]) : "0"));
                    index++;
                    instance.setRam(Integer.parseInt(null != objs[index] ? String.valueOf(objs[index]) : "0"));
                    index++;
                    instance.setVolumeSize(Integer.parseInt(null != objs[index] ? String.valueOf(objs[index]) : "0"));
                    index++;
                    instance.setRdsIp(String.valueOf(objs[index++]));
                    instance.setCreateTime((Date) objs[index++]);
                    instance.setPayType(String.valueOf(objs[index++]));
                    instance.setPayTypeStr(CloudResourceUtil.escapePayType(instance.getPayType()));
                    instance.setChargeState(String.valueOf(objs[index++]));
                    instance.setEndTime((Date) objs[index++]);
                    instance.setStatusStr(CloudResourceUtil.escapseChargeState(instance.getChargeState()));
                    if (CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(instance.getChargeState())) {
                        instance.setStatusStr(DictUtil.getStatusByNodeEn("rds", instance.getRdsStatus()));
                    }
                    list.add(instance);
                }
            }
        }
	    return list;
	}
	
	public CloudRDSInstance getInstanceById (String instanceId) {
	    StringBuffer sql = new StringBuffer();
	    sql.append(" SELECT                                                                ");
	    sql.append("   instance.rds_id                                                     ");
	    sql.append("   ,instance.rds_name                                                  ");
	    sql.append("   ,instance.rds_status                                                ");
	    sql.append("   ,instance.is_master                                                 ");
	    sql.append("   ,master.rds_id AS masterId                                          ");
	    sql.append("   ,master.rds_name AS masterName                                      ");
	    sql.append("   ,instance.rds_description                                           ");
	    sql.append("   ,instance.prj_id                                                    ");
	    sql.append("   ,prj.prj_name                                                       ");
	    sql.append("   ,instance.dc_id                                                     ");
	    sql.append("   ,dc.dc_name                                                         ");
	    sql.append("   ,customer.cus_id                                                    ");
	    sql.append("   ,customer.cus_org                                                   ");
	    sql.append("   ,instance.config_id                                                 ");
	    sql.append("   ,file.config_name                                                   ");
	    sql.append("   ,instance.version_id                                                ");
	    sql.append("   ,version.name as Version                                            ");
	    sql.append("   ,datastore.name as Type                                             ");
	    sql.append("   ,(SELECT COUNT(1) FROM cloud_rdsinstance slave                      ");
	    sql.append("     WHERE slave.master_id = instance.rds_id AND slave.is_deleted = '0'");
	    sql.append("     AND slave.is_visible = '1') as slaveCount                         ");
	    sql.append("   ,instance.flavor_id                                                 ");
	    sql.append("   ,flavor.flavor_vcpus                                                ");
	    sql.append("   ,flavor.flavor_ram                                                  ");
	    sql.append("   ,instance.volume_size                                               ");
	    sql.append("   ,instance.volume_type                                               ");
	    sql.append("   ,volume.type_name                                                   ");
	    sql.append("   ,instance.net_id                                                    ");
	    sql.append("   ,network.net_name                                                   ");
	    sql.append("   ,instance.subnet_id                                                 ");
	    sql.append("   ,subnet.subnet_name                                                 ");
	    sql.append("   ,instance.rds_ip                                                    ");
	    sql.append("   ,instance.create_time                                               ");
	    sql.append("   ,instance.pay_type                                                  ");
	    sql.append("   ,instance.charge_state                                              ");
	    sql.append("   ,instance.end_time                                                  ");
	    sql.append(" FROM                                                                  ");
	    sql.append("   cloud_rdsinstance instance                                          ");
	    sql.append(" LEFT JOIN                                                             ");
	    sql.append("   dc_datacenter dc ON instance.dc_id = dc.id                          ");
	    sql.append(" LEFT JOIN                                                             ");
	    sql.append("   cloud_project prj ON instance.prj_id = prj.prj_id                   ");
	    sql.append(" LEFT JOIN                                                             ");
	    sql.append("   sys_selfcustomer customer ON prj.customer_id = customer.cus_id      ");
	    sql.append(" LEFT JOIN                                                             ");
	    sql.append("   cloud_datastoreversion version ON instance.version_id = version.id  ");
	    sql.append(" LEFT JOIN                                                             ");
	    sql.append("   cloud_datastore datastore ON version.datastore_id = datastore.id    ");
	    sql.append(" LEFT JOIN                                                             ");
	    sql.append("   cloud_flavor flavor ON instance.flavor_id = flavor.flavor_id        ");
	    sql.append(" LEFT JOIN                                                             ");
	    sql.append("   cloud_network network ON instance.net_id = network.net_id           ");
	    sql.append(" LEFT JOIN                                                             ");
	    sql.append("   cloud_subnetwork subnet ON instance.subnet_id = subnet.subnet_id    ");
	    sql.append(" LEFT JOIN                                                             ");
	    sql.append("   cloud_volumetype volume ON instance.volume_type = volume.type_id    ");
	    sql.append(" LEFT JOIN                                                             ");
	    sql.append("   cloud_rdsconfigfile file ON instance.config_id = file.config_id     ");
	    sql.append(" LEFT JOIN                                                             ");
	    sql.append("   cloud_rdsinstance master ON master.rds_id = instance.master_id      ");
	    sql.append(" WHERE                                                                 ");
	    sql.append("   instance.rds_id = ?                                                 ");
	    javax.persistence.Query query = cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(), instanceId);
	    CloudRDSInstance instance = new CloudRDSInstance();
	    if (null != query) {
	        List<Object> result = query.getResultList();
	        if (null != result && result.size() > 0) {
	            for (Object obj : result) {
	                Object [] objs = (Object []) obj;
	                int index = 0;
	                instance.setRdsId(String.valueOf(objs[index++]));
	                instance.setRdsName(String.valueOf(objs[index++]));
	                instance.setRdsStatus(String.valueOf(objs[index++]));
	                instance.setIsMaster(String.valueOf(objs[index++]));
	                instance.setMasterId(String.valueOf(objs[index++]));
	                instance.setMasterName(String.valueOf(objs[index++]));
	                instance.setRdsDescription(String.valueOf(objs[index++]));
	                instance.setPrjId(String.valueOf(objs[index++]));
	                instance.setPrjName(String.valueOf(objs[index++]));
	                instance.setDcId(String.valueOf(objs[index++]));
	                instance.setDcName(String.valueOf(objs[index++]));
	                instance.setCusId(String.valueOf(objs[index++]));
	                instance.setCusOrg(String.valueOf(objs[index++]));
	                instance.setConfigId(String.valueOf(objs[index++]));
	                instance.setConfigName(String.valueOf(objs[index++]));
	                instance.setVersionId(String.valueOf(objs[index++]));
	                instance.setVersion(String.valueOf(objs[index++]));
	                instance.setType(String.valueOf(objs[index++]));
	                instance.setSlaveCount(Integer.parseInt(null != objs[index] ? String.valueOf(objs[index]) : "0"));
	                index++;
	                instance.setFlavorId(String.valueOf(objs[index++]));
	                instance.setCpu(Integer.parseInt(null != objs[index] ? String.valueOf(objs[index]) : "0"));
	                index++;
	                instance.setRam(Integer.parseInt(null != objs[index] ? String.valueOf(objs[index]) : "0"));
	                index++;
	                instance.setVolumeSize(Integer.parseInt(null != objs[index] ? String.valueOf(objs[index]) : "0"));
	                index++;
	                instance.setVolumeType(String.valueOf(objs[index++]));
	                instance.setVolumeTypeName(String.valueOf(objs[index++]));
	                instance.setNetId(String.valueOf(objs[index++]));
	                instance.setNetName(String.valueOf(objs[index++]));
	                instance.setSubnetId(String.valueOf(objs[index++]));
	                instance.setSubnetName(String.valueOf(objs[index++]));
	                instance.setRdsIp(String.valueOf(objs[index++]));
	                instance.setCreateTime((Date) objs[index++]);
	                instance.setPayType(String.valueOf(objs[index++]));
	                instance.setPayTypeStr(CloudResourceUtil.escapePayType(instance.getPayType()));
	                instance.setChargeState(String.valueOf(objs[index++]));
	                instance.setEndTime((Date) objs[index++]);
	                instance.setStatusStr(CloudResourceUtil.escapseChargeState(instance.getChargeState()));
	                if (CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(instance.getChargeState())) {
                        instance.setStatusStr(DictUtil.getStatusByNodeEn("rds", instance.getRdsStatus()));
                    }
	            }
	        }
	    }
	    return instance;
	}
	
	public EayunResponseJson deleteRdsInstance (CloudRDSInstance cloudRdsInstance, String userName) throws Exception {
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
            responseJson.setRespCode(ConstantClazz.WARNING_CODE);
            return responseJson;
        }
        openstackRDSInstanceService.delete(cloudRdsInstance.getDcId(), cloudRdsInstance.getPrjId(), cloudRdsInstance.getRdsId());
        BaseCloudRDSInstance rds = new BaseCloudRDSInstance();
        rds = cloudRDSInstanceDao.findOne(cloudRdsInstance.getRdsId());
        rds.setRdsStatus("SHUTDOWN");
        rds.setDeleteTime(new Date());
        rds.setDeleteUser(userName);

        cloudRDSInstanceDao.merge(rds);
        
        
        JSONObject json = new JSONObject();
        json.put("rdsId", rds.getRdsId());
        json.put("vmId", rds.getVmId());
        json.put("dcId", rds.getDcId());
        json.put("prjId", rds.getPrjId());
        json.put("rdsStatus", rds.getRdsStatus());
        json.put("count", "0");
        
        final JSONObject data = json;
        TransactionHookUtil.registAfterCommitHook(new Hook() {
            @Override
            public void execute() {
                jedisUtil.addUnique(RedisKey.rdsKey, data.toJSONString());
            }
            
        });
        cloudRdsInstance.setOpDate(rds.getDeleteTime());
        cloudRdsInstance.setCusId(getCusIdByPrjId(cloudRdsInstance.getPrjId()));
        cloudRdsInstance.setRdsName(rds.getRdsName());
        // 按需资源删除需要发送计费消息
        if(PayType.PAYAFTER.equals(rds.getPayType())){
            rdsInstanceOptionCharge(cloudRdsInstance);
        }
        responseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        return responseJson;
    }
	
	private String getCusIdByPrjId (String prjId) {
	    StringBuffer sql = new StringBuffer();
	    sql.append(" SELECT ");
	    sql.append("   customer_id ");
	    sql.append(" FROM ");
	    sql.append("   cloud_project ");
	    sql.append(" WHERE ");
	    sql.append("   prj_id = ? ");
	    javax.persistence.Query query = cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(), prjId);
	    Object result = query.getSingleResult();
        String cusId = result == null ? "" : String.valueOf(result.toString());
	    return cusId;
	}
	@Override
	public List<JSONObject> getAllDBVersion () {
	    List<JSONObject> list = new ArrayList<JSONObject>();
	    List<String> params = new ArrayList<String>();
	    StringBuffer sql = new StringBuffer();
	    sql.append(" SELECT                                                ");
	    sql.append("   CONCAT(datastore.name, version.name) AS versionName ");
	    sql.append("   ,version.id AS versionId                            ");
	    sql.append(" FROM cloud_datastoreversion version                   ");
	    sql.append(" LEFT JOIN                                             ");
	    sql.append("   cloud_datastore datastore                           ");
	    sql.append(" ON datastore.id = version.datastore_id                ");
	    javax.persistence.Query query = cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(), params.toArray());
	    if (null != query && query.getResultList().size() > 0) {
	        List<Object> result = query.getResultList();
	        for (int i = 0; i < result.size(); i++) {
	            Object[] objs = (Object[]) result.get(i);
	            JSONObject json = new JSONObject();
	            json.put("versionName", String.valueOf(objs[0]));
	            json.put("versionId", String.valueOf(objs[1]));
	            list.add(json);
	        }
	    }
	    return list;
	}

	/**
	 * 根据项目ID获取实例的数量（包括创建中和删除中的）。
	 * @param prjId
	 * @return
     */
	@Override
	public int getRdsInstanceCountByPrjId(String prjId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT                    ");
		sql.append("   COUNT(1)                ");
		sql.append(" FROM cloud_rdsinstance    ");
		sql.append(" WHERE                     ");
		sql.append("   prj_id = ?              ");
		sql.append("   AND is_deleted = '0'    ");
		javax.persistence.Query query = cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(), new Object[] {prjId});
		if (null != query && !StringUtil.isEmpty(query.getSingleResult().toString())) {
			return Integer.parseInt(query.getSingleResult().toString());
		}
		return 0;
	}
}
