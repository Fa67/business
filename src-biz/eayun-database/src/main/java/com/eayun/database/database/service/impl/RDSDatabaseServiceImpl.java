package com.eayun.database.database.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.StringUtil;
import com.eayun.database.database.dao.RDSDatabaseDao;
import com.eayun.database.database.model.BaseCloudRDSDatabase;
import com.eayun.database.database.model.CloudRDSDatabase;
import com.eayun.database.database.service.RDSDatabaseService;
import com.eayun.database.relation.model.CloudRDSRelation;
import com.eayun.database.relation.service.RDSRelationService;
import com.eayun.eayunstack.service.OpenstackDBUserService;
import com.eayun.eayunstack.service.OpenstackDatabaseService;

@Service
@Transactional
public class RDSDatabaseServiceImpl implements RDSDatabaseService {
    private static final Logger log = LoggerFactory.getLogger(RDSDatabaseServiceImpl.class);
    @Autowired
    private RDSDatabaseDao databaseDao;
    @Autowired
    private RDSRelationService relationService;
    @Autowired
    private OpenstackDatabaseService openstackDBService;
    @Autowired
    private OpenstackDBUserService openstackDBUserService;
    
    @Override
    public Page getDatabasePageList (Page page, ParamsMap paramsMap) throws Exception {
        List<String> list = new ArrayList<String>();
        list.add(String.valueOf(paramsMap.getParams().get("instanceId")));
        String charSet = String.valueOf(paramsMap.getParams().get("characterSet"));
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT                             ");
        sql.append("    db.database_id                  ");
        sql.append("    ,db.database_name               ");
        sql.append("    ,db.instance_id                 ");
        sql.append("    ,instance.rds_name              ");
        sql.append("    ,instance.rds_status            ");
        sql.append("    ,instance.charge_state          ");
        sql.append("    ,db.prj_id                      ");
        sql.append("    ,db.dc_id                       ");
        sql.append("    ,db.character_set               ");
        sql.append("    ,db.create_time                 ");
        sql.append("    ,db.remark                      ");
        sql.append(" FROM                               ");
        sql.append("    cloud_rdsdatabase db            ");
        sql.append(" LEFT JOIN                          ");
        sql.append("    cloud_rdsinstance instance      ");
        sql.append(" ON                                 ");
        sql.append("    db.instance_id = instance.rds_id");
        sql.append(" WHERE                              ");
        sql.append("    db.instance_id = ?              ");
        if (!StringUtil.isEmpty(charSet) && !"null".equals(charSet)) {
            sql.append(" AND db.character_set = ?       ");
            list.add(charSet);
        }
        sql.append(" ORDER BY db.create_time DESC       ");
        QueryMap queryMap = new QueryMap();
        queryMap.setPageNum(paramsMap.getPageNumber());
        queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
        page = databaseDao.pagedNativeQuery(sql.toString(), queryMap, list.toArray());
        List<Object> result = (List<Object>) page.getResult();
        int i = 0;
        for (Object obj : result) {
            Object[] objs = (Object[]) obj;
            CloudRDSDatabase db = new CloudRDSDatabase();
            int index = 0;
            db.setDatabaseId(String.valueOf(objs[index++]));
            db.setDatabaseName(String.valueOf(objs[index++]));
            db.setInstanceId(String.valueOf(objs[index++]));
            db.setInstanceName(String.valueOf(objs[index++]));
            db.setStatus(String.valueOf(objs[index++]));
            db.setChargeState(String.valueOf(objs[index++]));
            db.setPrjId(String.valueOf(objs[index++]));
            db.setDcId(String.valueOf(objs[index++]));
            db.setCharacterSet(String.valueOf(objs[index++]));
            db.setCreateTime((Date)objs[index++]);
            db.setRemark(String.valueOf(objs[index++]));
            List<CloudRDSRelation> relationList = relationService.getRDSRelationList(null, db.getDatabaseId());
            List<String> accountList = new ArrayList<String>();
            for (CloudRDSRelation relation : relationList) {
                accountList.add(relation.getAccountName());
            }
            db.setAccessAccountList(accountList);
            result.set(i, db);
            i++;
        }
        return page;
    }
    
    @Override
    public List<CloudRDSDatabase> getDatabaseList (String instanceId) throws Exception {
        List<CloudRDSDatabase> list = new ArrayList<CloudRDSDatabase>();
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT                 ");
        sql.append("    database_id         ");
        sql.append("    ,database_name      ");
        sql.append("    ,instance_id        ");
        sql.append("    ,prj_id             ");
        sql.append("    ,dc_id              ");
        sql.append(" FROM                   ");
        sql.append("    cloud_rdsdatabase   ");
        sql.append(" WHERE                  ");
        sql.append("    instance_id = ?     ");
        javax.persistence.Query query = databaseDao.createSQLNativeQuery(sql.toString(), instanceId);
        if (null != query) {
            List<Object> result = query.getResultList();
            if (null != result && result.size() > 0) {
                for (Object obj : result) {
                    Object[] objs = (Object[]) obj;
                    CloudRDSDatabase db = new CloudRDSDatabase();
                    db.setDatabaseId(String.valueOf(objs[0]));
                    db.setDatabaseName(String.valueOf(objs[1]));
                    db.setInstanceId(String.valueOf(objs[2]));
                    db.setPrjId(String.valueOf(objs[3]));
                    db.setDcId(String.valueOf(objs[4]));
                    list.add(db);
                }
            }
        }
        return list;
    }
    
    @Override
    public boolean checkDBNameExist (CloudRDSDatabase database) throws Exception {
        //防止前台instanceId或databaseName传空
        if (StringUtil.isEmpty(database.getInstanceId()) || StringUtil.isEmpty(database.getDatabaseName())) {
            return true;
        }
        List<CloudRDSDatabase> dblist = getDatabaseList(database.getInstanceId());
        for (CloudRDSDatabase db : dblist) {
            if (db.getDatabaseName().equals(database.getDatabaseName())) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public CloudRDSDatabase createDatabase (CloudRDSDatabase database) throws AppException {
        JSONObject json = new JSONObject();
        JSONArray databases = new JSONArray();
        JSONObject data = new JSONObject();
        data.put("name", database.getDatabaseName());
        data.put("character_set", database.getCharacterSet());
        databases.add(data);
        json.put("databases", databases);
        try {
            openstackDBService.create(database.getDcId(), database.getPrjId(), database.getInstanceId(), json);
            BaseCloudRDSDatabase db = new BaseCloudRDSDatabase();
            BeanUtils.copyPropertiesByModel(db, database);
            db.setCreateTime(new Date());
            databaseDao.save(db);
        } catch (AppException e) {
            log.error(e.toString(), e);
            throw e;
        }
        return new CloudRDSDatabase();
    }
    
    @Override
    public boolean deleteDatabase (CloudRDSDatabase database) throws AppException {
        BaseCloudRDSDatabase isNull = databaseDao.findOne(database.getDatabaseId());
        if (null == isNull) {
            throw new AppException("database does not exist");
        }
        try {
            boolean revokeFlag = revokeAccessDatabase(database);
            boolean deleteFlag = openstackDBService.delete(database.getDcId(), database.getPrjId(), database.getInstanceId(), database.getDatabaseName());
            if (revokeFlag && deleteFlag) {
                delDatabaseInDB(database.getDatabaseId());
            }
            return revokeFlag && deleteFlag;
        } catch (AppException e) {
            log.error(e.toString(), e);
            throw e;
        }
    }
    
    /**
     * 底层删除数据库之前，撤销该数据库的全部账户权限，因为底层删除不会自动去除
     * @param database
     * @return
     * @throws AppException
     */
    private boolean revokeAccessDatabase (CloudRDSDatabase database) throws AppException {
        List<CloudRDSRelation> relationList = relationService.getRDSRelationList(null, database.getDatabaseId());
        if (null != relationList && relationList.size() > 0) {
            for (CloudRDSRelation relation : relationList) {
                openstackDBUserService.revokeAccess(database.getDcId(), database.getInstanceId(), relation.getAccountName(), database.getDatabaseName());
            }
        }
        return true;
    }
    
    @Override
    public boolean delDatabaseInDB (String databaseId) {
        databaseDao.delete(databaseId);
        relationService.deleteRDSRelations(null, databaseId);
        return true;
    }
    
    @Override
    public String getDBNameById (String id) {
        BaseCloudRDSDatabase database = databaseDao.findOne(id);
        if (null != database) {
            return database.getDatabaseName();
        }
        return null;
    }
    
    @Override
    public String getDBIdByName (String instanceId, String name) {
        StringBuffer hql = new StringBuffer();
        hql.append(" FROM                       ");
        hql.append("    BaseCloudRDSDatabase    ");
        hql.append(" WHERE                      ");
        hql.append("    instanceId = ?          ");
        hql.append("    and databaseName = ?    ");
        List<BaseCloudRDSDatabase> dbList = databaseDao.find(hql.toString(), new Object[]{instanceId, name});
        if (null != dbList && dbList.size() > 0) {
            return dbList.get(0).getDatabaseId();
        }
        return null;
    }
    
    @Override
    public void deleteAllDatabaseByInstanceId (String instanceId) throws Exception {
        List<CloudRDSDatabase> list = getDatabaseList(instanceId);
        for (CloudRDSDatabase database : list) {
            delDatabaseInDB(database.getDatabaseId());
        }
    }
    
    @Override
    public void synchronDBCreate (String dcId, String prjId, String instanceId) throws Exception {
        List<BaseCloudRDSDatabase> stackList = openstackDBService.getStackList(dcId, prjId, instanceId);
        if (null != stackList && stackList.size() > 0) {
            for (BaseCloudRDSDatabase database : stackList) {
                if (!"test".equals(database.getDatabaseName().toLowerCase().substring(0, Math.min(database.getDatabaseName().length(), 4)))) {
                    databaseDao.save(database);
                }
            }
        }
    }
    
}
