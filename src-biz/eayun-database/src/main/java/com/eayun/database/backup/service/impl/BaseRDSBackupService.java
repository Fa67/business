package com.eayun.database.backup.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceSyncConstant;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.database.backup.bean.BackupStatus;
import com.eayun.database.backup.dao.CloudRDSBackupDao;
import com.eayun.database.backup.dao.CloudRDSBackupScheduleDao;
import com.eayun.database.backup.model.BaseCloudRDSBackup;
import com.eayun.database.backup.model.BaseCloudRDSBackupSchedule;
import com.eayun.database.backup.model.CloudRDSBackup;
import com.eayun.database.backup.model.CloudRDSBackupSchedule;
import com.eayun.database.instance.service.RDSInstanceService;
import com.eayun.eayunstack.model.Datastore;
import com.eayun.eayunstack.model.RDSBackup;
import com.eayun.eayunstack.service.OpenstackTroveBackupService;
import com.eayun.notice.model.MessageCloudDataBaseDeletedFailModel;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.project.service.ProjectService;
import com.eayun.virtualization.model.CloudProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ECSC云数据库备份基础Service实现类
 *
 * @author fan.zhang
 */
@Service
@Transactional
public class BaseRDSBackupService {
    private static final Logger log = LoggerFactory.getLogger(BaseRDSBackupService.class);

    @Autowired
    private CloudRDSBackupDao rdsBackupDao;

    @Autowired
    private CloudRDSBackupScheduleDao rdsBackupScheduleDao;

    @Autowired
    private OpenstackTroveBackupService troveBackupService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private JedisUtil jedisUtil;

    @Autowired
    private RDSInstanceService rdsInstanceService;

    @Autowired
    private MessageCenterService messageCenterService;

    public Page getBackups(Page page, ParamsMap map, SessionUserInfo sessionUser, QueryMap queryMap) throws Exception {
        String datacenterId = "";
        String projectId = "";
        String searchKey = "";
        String searchValue = "";
        String category = "";
        String instanceId = "";
        String status = "";
        String versionName = "";
        if (null != map && null != map.getParams()) {
            datacenterId = map.getParams().get("datacenterId") == null ? "" : map.getParams().get("datacenterId").toString();
            projectId = map.getParams().get("projectId") == null ? "" : map.getParams().get("projectId").toString();
            searchKey = map.getParams().get("searchKey") != null ? map.getParams().get("searchKey") + "" : "";
            searchValue = map.getParams().get("searchValue") != null ? map.getParams().get("searchValue") + "" : "";
            category = map.getParams().get("category") == null ? "" : map.getParams().get("category").toString();
            versionName = map.getParams().get("versionName") == null ? "" : map.getParams().get("versionName").toString();
            instanceId = map.getParams().get("instanceId") == null ? "" : map.getParams().get("instanceId").toString();
            status = map.getParams().get("status") == null ? "" : map.getParams().get("status").toString();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT 				            ");
        sb.append("    bak.backup_id,               ");
        sb.append("    bak.create_time,             ");
        sb.append("    bak.update_time,             ");
        sb.append("    bak.status,                  ");
        sb.append("    bak.description,             ");
        sb.append("    bak.name as backupName,      ");
        sb.append("    bak.instance_id,             ");
        sb.append("    ins.rds_name,                ");
        sb.append("    bak.size,                    ");
        sb.append("    bak.location_ref,            ");
        sb.append("    bak.category,                ");
        sb.append("    dbv.datastore_id,            ");
        sb.append("    dbs.name as datastoreName,   ");
        sb.append("    dbv.name as versionName,     ");
        sb.append("    bak.version_id,              ");
        sb.append("    bak.datacenter_id,           ");
        sb.append("    bak.project_id,              ");
        sb.append("    bak.instance_exist,          ");
        sb.append("    bak.parent_id,               ");
        sb.append("    cus.cus_org,                 ");
        sb.append("    prj.prj_name,                ");
        sb.append("    dc.dc_name                   ");
        sb.append(" FROM                            ");
        sb.append("    cloud_rdsbackup bak          ");
        sb.append("        LEFT JOIN                ");
        sb.append("    cloud_datastoreversion dbv ON bak.version_id = dbv.id    ");
        sb.append("        LEFT JOIN                ");
        sb.append("    cloud_datastore dbs ON dbv.datastore_id = dbs.id         ");
        sb.append("        LEFT JOIN                ");
        sb.append("    cloud_rdsinstance ins ON bak.instance_id = ins.rds_id    ");
        sb.append("        LEFT JOIN                ");
        sb.append("    cloud_project prj ON bak.project_id = prj.prj_id         ");
        sb.append("        LEFT JOIN                ");
        sb.append("    dc_datacenter dc ON bak.datacenter_id = dc.id            ");
        sb.append("        LEFT JOIN                ");
        sb.append("    sys_selfcustomer cus ON prj.customer_id = cus.cus_id     ");
        sb.append(" WHERE                           ");
        sb.append("    bak.is_visible = '1'         ");
        int index = 0;
        Object[] params = new Object[10];
        if (!StringUtils.isEmpty(datacenterId)) {
            sb.append(" and bak.datacenter_id = ? ");
            params[index++] = datacenterId;
        }
        if (!StringUtils.isEmpty(projectId)) {
            sb.append(" and bak.project_id = ? ");
            params[index++] = projectId;
        }
        if (!StringUtils.isEmpty(searchKey)) {
            if (searchKey.equals("backupName")) {
                sb.append(" and bak.name like ?     ");
                searchValue = searchValue.replaceAll("\\_", "\\\\_").replaceAll("\\%", "\\\\%");
                params[index++] = "%" + searchValue + "%";
            } else if (searchKey.equals("instanceName")) {
                sb.append(" and ins.rds_name like ? ");
                searchValue = searchValue.replaceAll("\\_", "\\\\_").replaceAll("\\%", "\\\\%");
                params[index++] = "%" + searchValue + "%";
            }
        }
        if (!StringUtils.isEmpty(category)) {
            sb.append(" and bak.category = ?    ");
            params[index++] = category;
        }
        if (!StringUtils.isEmpty(versionName)) {
            sb.append(" and dbv.name = ?  ");
            params[index++] = versionName;
        }
        if (!StringUtils.isEmpty(instanceId)) {
            sb.append(" and bak.instance_id = ?  ");
            params[index++] = instanceId;
        }
        if (!StringUtils.isEmpty(status)) {
            if(status.equals("BUILD")){
                //ecmc传来的创建中的查询，需要查询NEW和BUILDING
                sb.append(" and bak.status = ? or bak.status = ?");
                params[index++] = BackupStatus.NEW;
                params[index++] = BackupStatus.BUILDING;
            }else{
                //留给ecsc，如果ecsc的状态列加表头筛选，则可以与上面相同
                sb.append(" and bak.status = ?  ");
                params[index++] = status;
            }

        }
        sb.append(" ORDER BY bak.create_time DESC   ");
        Object[] args = new Object[index];
        System.arraycopy(params, 0, args, 0, index);
        page = rdsBackupDao.pagedNativeQuery(sb.toString(), queryMap, args);
        List resultList = (List) page.getResult();
        for (int i = 0; i < resultList.size(); i++) {
            Object[] objs = (Object[]) resultList.get(i);
            CloudRDSBackup cloudRDSBackup = new CloudRDSBackup();
            int pos = 0;
            cloudRDSBackup.setBackupId(String.valueOf(objs[pos++]));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            cloudRDSBackup.setCreateTime(sdf.parse(sdf.format(objs[pos++])));
            cloudRDSBackup.setUpdateTime(sdf.parse(sdf.format(objs[pos++])));
            cloudRDSBackup.setStatus(String.valueOf(objs[pos++]));
            cloudRDSBackup.setStatusCN(escapeStatus(cloudRDSBackup.getStatus()));
            cloudRDSBackup.setDescription(String.valueOf(objs[pos++]));
            cloudRDSBackup.setName(String.valueOf(objs[pos++]));
            cloudRDSBackup.setInstanceId(String.valueOf(objs[pos++]));
            cloudRDSBackup.setInstanceName(String.valueOf(objs[pos++]));
            BigDecimal size = (BigDecimal) (objs[pos++]);
            cloudRDSBackup.setSize(size.doubleValue());
            cloudRDSBackup.setLocationRef(String.valueOf(objs[pos++]));
            cloudRDSBackup.setCategory(String.valueOf(objs[pos++]));
            cloudRDSBackup.setDatastoreId(String.valueOf(objs[pos++]));
            cloudRDSBackup.setVersionType(beautify(String.valueOf(objs[pos++])));//e.g:mysql 通过beautify转MySQL
            cloudRDSBackup.setVersion(String.valueOf(objs[pos++]));//e.g:5.5
            cloudRDSBackup.setVersionId(String.valueOf(objs[pos++]));
            cloudRDSBackup.setDatastoreId(String.valueOf(objs[pos++]));
            cloudRDSBackup.setProjectId(String.valueOf(objs[pos++]));
            cloudRDSBackup.setInstanceExist(String.valueOf(objs[pos++]));
            cloudRDSBackup.setParentId(String.valueOf(objs[pos++]));
            cloudRDSBackup.setCusOrg(String.valueOf(objs[pos++]));
            cloudRDSBackup.setPrjName(String.valueOf(objs[pos++]));
            cloudRDSBackup.setDcName(String.valueOf(objs[pos++]));

            resultList.set(i, cloudRDSBackup);
        }
        return page;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Page getBackupsByInstanceId(Page page, ParamsMap map, QueryMap queryMap) throws Exception {
        String instanceId = "";
        if (null != map && null != map.getParams()) {
            instanceId = map.getParams().get("instanceId") == null ? "" : map.getParams().get("instanceId").toString();
        }
        log.info("分页查询实例[" + instanceId + "]的备份开始");

        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT 				            ");
        sb.append("    bak.backup_id,               ");
        sb.append("    bak.create_time,             ");
        sb.append("    bak.update_time,             ");
        sb.append("    bak.status,                  ");
        sb.append("    bak.description,             ");
        sb.append("    bak.name,                    ");
        sb.append("    bak.instance_id,             ");
        sb.append("    ins.rds_name,                ");
        sb.append("    bak.size,                    ");
        sb.append("    bak.location_ref,            ");
        sb.append("    bak.category,                ");
        sb.append("    dbv.datastore_id,            ");
        sb.append("    dbs.name,                    ");
        sb.append("    dbv.name,                    ");
        sb.append("    bak.version_id,              ");
        sb.append("    bak.datacenter_id,           ");
        sb.append("    bak.project_id,              ");
        sb.append("    bak.instance_exist,          ");
        sb.append("    bak.parent_id                ");
        sb.append(" FROM                            ");
        sb.append("    cloud_rdsbackup bak          ");
        sb.append("        LEFT JOIN                ");
        sb.append("    cloud_datastoreversion dbv ON bak.version_id = dbv.id    ");
        sb.append("        LEFT JOIN                ");
        sb.append("    cloud_datastore dbs ON dbv.datastore_id = dbs.id         ");
        sb.append("        LEFT JOIN                ");
        sb.append("    cloud_rdsinstance ins ON bak.instance_id = ins.rds_id    ");
        sb.append(" WHERE                           ");
        sb.append("    bak.is_visible = '1'         ");
        sb.append("    and bak.instance_id = ?      ");
        sb.append(" ORDER BY bak.create_time DESC   ");
        page = rdsBackupDao.pagedNativeQuery(sb.toString(), queryMap, instanceId);
        List resultList = (List) page.getResult();
        for (int i = 0; i < resultList.size(); i++) {
            Object[] objs = (Object[]) resultList.get(i);
            CloudRDSBackup cloudRDSBackup = new CloudRDSBackup();
            int index = 0;
            cloudRDSBackup.setBackupId(String.valueOf(objs[index++]));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            cloudRDSBackup.setCreateTime(sdf.parse(sdf.format(objs[index++])));
            cloudRDSBackup.setUpdateTime(sdf.parse(sdf.format(objs[index++])));
            cloudRDSBackup.setStatus(String.valueOf(objs[index++]));
            cloudRDSBackup.setStatusCN(escapeStatus(cloudRDSBackup.getStatus()));
            cloudRDSBackup.setDescription(String.valueOf(objs[index++]));
            cloudRDSBackup.setName(String.valueOf(objs[index++]));
            cloudRDSBackup.setInstanceId(String.valueOf(objs[index++]));
            cloudRDSBackup.setInstanceName(String.valueOf(objs[index++]));
            cloudRDSBackup.setSize((Double) objs[index++]);
            cloudRDSBackup.setLocationRef(String.valueOf(objs[index++]));
            cloudRDSBackup.setCategory(String.valueOf(objs[index++]));
            cloudRDSBackup.setDatastoreId(String.valueOf(objs[index++]));
            cloudRDSBackup.setVersionType(beautify(String.valueOf(objs[index++])));//e.g:mysql 通过beautify转MySQL
            cloudRDSBackup.setVersion(String.valueOf(objs[index++]));//e.g:5.5
            cloudRDSBackup.setVersionId(String.valueOf(objs[index++]));
            cloudRDSBackup.setDatastoreId(String.valueOf(objs[index++]));
            cloudRDSBackup.setProjectId(String.valueOf(objs[index++]));
            cloudRDSBackup.setInstanceExist(String.valueOf(objs[index++]));
            cloudRDSBackup.setParentId(String.valueOf(objs[index]));

            resultList.set(i, cloudRDSBackup);
        }
        return page;
    }

    private String beautify(String type) {
        String result = "";
        switch (type){
            case "mysql":
                result = "MySQL";
                break;
        }
        return result;
    }

    public CloudRDSBackup createBackup(String datacenterId, String projectId, String name, String instanceId, String parentId, String category) throws Exception {
        log.info("为实例[" + instanceId + "]创建[" + category + "]备份，父备份为[" + parentId + "]");
        JSONObject req = new JSONObject();
        req.put("name", name);
        req.put("instance", instanceId);
        req.put("description", null);
        if (!StringUtils.isEmpty(parentId)) {
            req.put("parent_id", parentId);
        }
        JSONObject requestBody = new JSONObject();
        requestBody.put("backup", req);
        RDSBackup rdsBackup = troveBackupService.create(datacenterId, projectId, requestBody);

        CloudRDSBackup cloudRDSBackup = getCloudRDSBackup(datacenterId, projectId, category, rdsBackup);

        if (rdsBackup.getParent_id() != null && !rdsBackup.getParent_id().equals("")) {
            cloudRDSBackup.setParentId(rdsBackup.getParent_id());
        }

        BaseCloudRDSBackup baseBean = new BaseCloudRDSBackup();
        BeanUtils.copyProperties(baseBean, cloudRDSBackup);
        rdsBackupDao.save(baseBean);
        rdsInstanceService.setRdsInstanceStatusToBackup(instanceId);


        JSONObject json = new JSONObject();
        json.put("datacenter_id", datacenterId);
        json.put("project_id", projectId);
        json.put("backup_id", cloudRDSBackup.getBackupId());
        json.put("category", category);
        json.put("retry_count", 0);
        final JSONObject task = json;
        TransactionHookUtil.registAfterCommitHook(new TransactionHookUtil.Hook() {
            @Override
            public void execute() {
                try {
                    log.info("将RDS备份记录推入状态同步队列");
                    jedisUtil.push(RedisKey.rdsBackupSyncKey, task.toJSONString());
                } catch (Exception e) {
                    log.error("将RDS备份记录推入状态同步队列失败", e);
                }
            }
        });
        return cloudRDSBackup;
    }

    /**
     * 将底层云数据库备份信息转换为上层备份实体对象
     *
     * @param datacenterId
     * @param projectId
     * @param category
     * @param rdsBackup
     * @return
     * @throws ParseException
     */
    private CloudRDSBackup getCloudRDSBackup(String datacenterId, String projectId, String category, RDSBackup rdsBackup) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        CloudRDSBackup cloudRDSBackup = new CloudRDSBackup();
        cloudRDSBackup.setBackupId(rdsBackup.getId());
        Date createTimeUTC = sdf.parse(rdsBackup.getCreated());
        Date createTime = DateUtil.addDay(createTimeUTC, new int[]{0, 0, 0, +8});
        cloudRDSBackup.setCreateTime(createTime);
        Date updateTimeUTC = sdf.parse(rdsBackup.getUpdated());
        Date updateTime = DateUtil.addDay(updateTimeUTC, new int[]{0, 0, 0, +8});
        cloudRDSBackup.setUpdateTime(updateTime);
        cloudRDSBackup.setStatus(rdsBackup.getStatus());
        cloudRDSBackup.setStatusCN(escapeStatus(rdsBackup.getStatus()));
        cloudRDSBackup.setDescription(rdsBackup.getDescription());
        cloudRDSBackup.setName(rdsBackup.getName());
        cloudRDSBackup.setInstanceId(rdsBackup.getInstance_id());
        cloudRDSBackup.setInstanceName(getInstanceNameById(rdsBackup.getInstance_id()));
        cloudRDSBackup.setConfigId(getConfigIdByInstance(rdsBackup.getInstance_id()));
        cloudRDSBackup.setCategory(category);

        Datastore datastore = rdsBackup.getDatastore();
        cloudRDSBackup.setVersionId(datastore.getVersion_id());
        cloudRDSBackup.setVersion(datastore.getVersion());
        cloudRDSBackup.setVersionType(datastore.getType());

        cloudRDSBackup.setProjectId(projectId);
        cloudRDSBackup.setDatacenterId(datacenterId);
        cloudRDSBackup.setIsVisible("1");
        cloudRDSBackup.setInstanceExist("1");

        cloudRDSBackup.setSize(rdsBackup.getSize());
        if (rdsBackup.getLocationRef() != null && !rdsBackup.getLocationRef().equals("")) {
            cloudRDSBackup.setLocationRef(rdsBackup.getLocationRef());
        }
        if (rdsBackup.getParent_id() != null && !rdsBackup.getParent_id().equals("")) {
            cloudRDSBackup.setParentId(rdsBackup.getParent_id());
        }

        return cloudRDSBackup;
    }

    private String getConfigIdByInstance(String instanceId) {
        StringBuilder sb = new StringBuilder();
        sb.append(" select config_id from cloud_rdsinstance where rds_id = ?");
        Query query = rdsBackupDao.createSQLNativeQuery(sb.toString(), instanceId);
        Object obj = query.getResultList().get(0);
        String configId = String.valueOf(obj);
        return configId;
    }

    public String getInstanceNameById(String instanceId) {
        StringBuilder sb = new StringBuilder();
        sb.append(" select rds_name from cloud_rdsinstance where rds_id = ?");
        Query query = rdsBackupDao.createSQLNativeQuery(sb.toString(), instanceId);
        Object obj = query.getResultList().get(0);
        String instanceName = String.valueOf(obj);
        return instanceName;
    }

    private String escapeStatus(String status) {
        String statusCN = "";
        switch (status) {
            case "NEW":
                statusCN = "创建中";
                break;
            case "COMPLETED":
                statusCN = "可用";
                break;
            case "FAILED":
                statusCN = "错误";
                break;
            case "BUILDING":
                statusCN = "创建中";
                break;
        }
        return statusCN;
    }

    public void updateBackup(String backupId, String name) throws Exception {
        rdsBackupDao.updateBackup(name, backupId);
    }

    public Map<String, Object> deleteBackup(String backupId){
        Map<String, Object> map = new HashMap<>();
        BaseCloudRDSBackup baseBackup = rdsBackupDao.findOne(backupId);
        CloudRDSBackup cloudRDSBackup = new CloudRDSBackup();
        boolean isSuccess = false;
        try {
            BeanUtils.copyProperties(cloudRDSBackup, baseBackup);
            cloudRDSBackup.setInstanceName(getInstanceNameById(cloudRDSBackup.getInstanceId()));
            if (cloudRDSBackup != null) {
                isSuccess = troveBackupService.delete(cloudRDSBackup.getDatacenterId(), cloudRDSBackup.getProjectId(), backupId);
            }
        } catch (AppException e){
            //截获exception，判断如果是itemNotFound，则删除成功，否则删除失败
            String[] args = e.getArgsMessage();
            String msgs = args.length>=1 ? args[0]:"";
            if(!StringUtils.isEmpty(msgs)){
                String errorMsg = msgs.substring(0, msgs.indexOf('-'));
                if("itemNotFound".equals(errorMsg)){
                    isSuccess = true;
                }else {
                    isSuccess = false;
                    log.error("底层删除备份[" + cloudRDSBackup.getBackupId() + "]失败", e);
                }
            }
        } catch (Exception e) {
            //如果底层删除失败，向上抛异常，这里要捕获异常，把isSuccess置为false，方便下面处理备份状态
            isSuccess = false;
            log.error("底层删除备份[" + cloudRDSBackup.getBackupId() + "]失败", e);
        }
        //底层无论删除成功与否都要在上层把数据库中的这条记录删掉，放置留下脏数据。
        rdsBackupDao.delete(baseBackup);
        if(isSuccess){
            log.info("底层删除备份[" + cloudRDSBackup.getBackupId() + "]成功");
        }
        map.put("isSuccess", isSuccess);
        map.put("rdsBackup", cloudRDSBackup);
        return map;
    }

    public void deleteBackup(BaseCloudRDSBackup backup){
        rdsBackupDao.delete(backup);
    }

    public int enableAutomaticBackup(String instanceId) throws Exception {
        int rows = rdsBackupScheduleDao.updateBackupScheduleStatus("1", instanceId);
        return rows;
    }

    public int disableAutomaticBackup(String instanceId) throws Exception {
        return rdsBackupScheduleDao.updateBackupScheduleStatus("0", instanceId);
    }

    public void addBackupSchedule(String datacenterId, String projectId, String customerId, String instanceId) throws Exception {
        log.info("创建云数据库实例[" + instanceId + "]成功，添加备份计划，默认开启自动备份");
        CloudProject project = projectService.findProject(projectId);
        CloudRDSBackupSchedule backupSchedule = new CloudRDSBackupSchedule();
        backupSchedule.setDatacenterId(datacenterId);
        backupSchedule.setProjectId(projectId);
        backupSchedule.setCustomerId(customerId);
        backupSchedule.setInstanceId(instanceId);
        backupSchedule.setUpTime(new Date());
        backupSchedule.setIsEnabled("1");

        String scheduleTime = project.getAutoBackupTime() == null ? "02:00" : project.getAutoBackupTime();
        backupSchedule.setScheduleTime(scheduleTime);

        BaseCloudRDSBackupSchedule baseBackupSchedule = new BaseCloudRDSBackupSchedule();
        BeanUtils.copyProperties(baseBackupSchedule, backupSchedule);
        rdsBackupScheduleDao.saveEntity(baseBackupSchedule);
    }

    public void handleBackupsOfDeletedInstance(String instanceId) throws Exception {
        //两件事——1.删除该实例的备份计划；2.更新该实例的备份的instance_exist和instance_deletetime
        rdsBackupScheduleDao.deleteBackupScheduleByInstanceId(instanceId);
        //new Date()日期格式 可能 存在问题 - 2017-03-14 18:22:51更新 测试通过没问题
        rdsBackupDao.updateBackupStatus("0", new Date(), instanceId);
    }

    public boolean verifyBackupName(String datacenterId, String projectId, String backupName) {
        log.info("校验数据中心同一个项目下备份是否重名");
        StringBuilder sb = new StringBuilder();
        sb.append("select count(*) from BaseCloudRDSBackup where datacenterId=? and projectId=? and name=?");
        List<Long> list = rdsBackupDao.find(sb.toString(), datacenterId, projectId, backupName);
        long count = list.get(0);
        boolean isDuplicated = count == 0 ? false : true;
        log.info("重名校验结果：" + isDuplicated);
        return isDuplicated;
    }

    public CloudRDSBackup syncBackupStatus(String datacenterId, String projectId, String backupId, String category) throws Exception {
        log.info("获取底层云数据库备份[" + backupId + "]信息");

        RDSBackup rdsBackup = troveBackupService.getById(datacenterId, projectId, backupId);
        CloudRDSBackup cloudRDSBackup = getCloudRDSBackup(datacenterId, projectId, category, rdsBackup);
        return cloudRDSBackup;
    }

    /**
     * 获取实例备份类型为category的备份列表，按照创建时间正序排列
     *
     * @param instanceId
     * @param category
     * @return
     */
    public List<BaseCloudRDSBackup> getBackupListByInstanceId(String instanceId, String category) {
        return rdsBackupDao.getBackupListByInstanceId(instanceId, category);
    }

    public void updateBackup(BaseCloudRDSBackup backup) throws Exception {
        log.info("update BaseCloudRDSBackup...");
        rdsBackupDao.saveOrUpdate(backup);
    }

    public long getMaxManualBackupCount(String projectId) throws Exception {
        CloudProject project = projectService.findProject(projectId);
        long count = project.getMaxBackupByHand();
        return count;
    }

    public long getCurrentManualBackupCount(String instanceId) throws Exception {
        return rdsBackupDao.getCurrentManualBackupCount(instanceId, "MANUAL");
    }

    public Map<String, String> getAutoBackupEnableStatus(String instanceId) throws Exception {
        Map<String, String> map = new HashMap<>();
        //查询备份计划表
        BaseCloudRDSBackupSchedule backupSchedule = rdsBackupScheduleDao.getBackupScheduleByInstanceId(instanceId);
        if (backupSchedule != null) {
            String isAutoBackupEnabled = backupSchedule.getIsEnabled().equals("1") ? "true" : "false";
            String scheduleTime = backupSchedule.getScheduleTime();
            map.put("isAutoBackupEnabled", isAutoBackupEnabled);
            map.put("scheduleTime", scheduleTime);
        } else {
            map.put("isAutoBackupEnabled", "false");
            map.put("scheduleTime", null);
        }
        return map;
    }

    public Map<String, String> getInfoForLog(String instanceId) {
        StringBuilder sb = new StringBuilder();
        sb.append("select rds_name, prj_id from cloud_rdsinstance where rds_id=? ");
        Query query = rdsBackupDao.createSQLNativeQuery(sb.toString(), instanceId);
        Object[] objs = (Object[]) query.getResultList().get(0);
        String instanceName = String.valueOf(objs[0]);
        String projectId = String.valueOf(objs[1]);
        Map<String, String> info = new HashMap<>();
        info.put("instanceName", instanceName);
        info.put("projectId",projectId);
        return info;
    }

    public void notifyDevOps(CloudRDSBackup backup) {
        log.info("开始发送云数据库备份单备份文件删除失败邮件");
        if(backup!=null){
            String projectId = backup.getProjectId();
            MessageCloudDataBaseDeletedFailModel m = new MessageCloudDataBaseDeletedFailModel();
            m.setInstanceId(backup.getInstanceId());
            m.setResourcesType(ResourceSyncConstant.RDS);
            m.setResourcesName(backup.getName());
            m.setResourcesId(backup.getBackupId());
            List<MessageCloudDataBaseDeletedFailModel> list = new ArrayList<>();
            list.add(m);
            messageCenterService.cloudDataBaseBackupDeletedFail(projectId, list);
        }
    }

    /**
     * 通知管理员和运维云数据库备份删除失败
     * @param backup 删除失败的某个子备份
     * @param backupsList 这个删除失败的子备份的所有父畚份列表
     */
    public void notifyDevOps(BaseCloudRDSBackup backup, List<BaseCloudRDSBackup> backupsList) {
        log.info("开始发送云数据库备份删除失败邮件，备份文件列表大小为["+backupsList.size()+"]");
        List<MessageCloudDataBaseDeletedFailModel> list = new ArrayList<>();

        String projectId = backup.getProjectId();
        MessageCloudDataBaseDeletedFailModel childBackupModel = new MessageCloudDataBaseDeletedFailModel();
        childBackupModel.setResourcesType(ResourceSyncConstant.RDS);
        childBackupModel.setResourcesId(backup.getBackupId());
        childBackupModel.setResourcesName(backup.getName());
        childBackupModel.setInstanceId(backup.getInstanceId());
        list.add(childBackupModel);

        for(int i=0; i<backupsList.size(); i++){
            BaseCloudRDSBackup b = backupsList.get(i);
            if(StringUtils.isEmpty(projectId)){
                projectId = b.getProjectId();
            }
            MessageCloudDataBaseDeletedFailModel m = new MessageCloudDataBaseDeletedFailModel();
            m.setResourcesType(ResourceSyncConstant.RDS);
            m.setResourcesId(b.getBackupId());
            m.setResourcesName(b.getName());
            m.setInstanceId(b.getInstanceId());
            list.add(m);
        }
        messageCenterService.cloudDataBaseBackupDeletedFail(projectId, list);
    }

    public void notifyDevOps(String projectId, List<RDSBackup> failDeletingBackups) {
        List<MessageCloudDataBaseDeletedFailModel> list = new ArrayList<>();
        for(int i=0; i<failDeletingBackups.size(); i++){
            RDSBackup b = failDeletingBackups.get(i);
            MessageCloudDataBaseDeletedFailModel m = new MessageCloudDataBaseDeletedFailModel();
            m.setResourcesType(ResourceSyncConstant.RDS);
            m.setResourcesId(b.getId());
            m.setResourcesName(b.getName());
            m.setInstanceId(b.getInstance_id());
            list.add(m);
        }
        messageCenterService.cloudDataBaseBackupDeletedFail(projectId, list);
    }
}
