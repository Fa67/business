package com.eayun.database.database.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceSyncConstant;
import com.eayun.common.redis.JedisUtil;
import com.eayun.database.database.dao.RDSDatabaseDao;
import com.eayun.database.database.model.BaseCloudRDSDatabase;
import com.eayun.database.database.service.CloudDatabaseService;
import com.eayun.database.database.service.RDSDatabaseService;
import com.eayun.eayunstack.service.OpenstackDatabaseService;
import com.eayun.log.ecmcsevice.EcmcLogService;
@Transactional
@Service
public class CloudDatabaseServiceImpl implements CloudDatabaseService {

    @Autowired
    private JedisUtil jedisUtil;
    
    @Autowired
    private RDSDatabaseDao databaseDao;
    
    @Autowired
    private RDSDatabaseService databaseService;
    
    @Autowired
    private OpenstackDatabaseService openstackDBService;
    
    @Autowired
    private EcmcLogService ecmcLogService;
    
    public void synchData(String datacenterId, String projectId, String instanceId) throws Exception {
        /**
         * 1、分别获取底层数据库数据资源列表和上层数据库数据资源列表
         * 2、进行比对：
         *      ！如果底层存在上层不存在的资源，则在上层数据库当中添加记录
         *      ！！如果上层存在底层不存在的资源，则调用上层数据库删除记录的接口，并向Redis同步数据中心已删除资源队列当中插入删除记录
         */
        Map<String, BaseCloudRDSDatabase> dbMap = new HashMap<String, BaseCloudRDSDatabase>();
        Map<String, BaseCloudRDSDatabase> stackMap = new HashMap<String, BaseCloudRDSDatabase>();
        
        List<BaseCloudRDSDatabase> dbList = queryCloudRDSDatabaseListByDcId(instanceId);
        
        List<BaseCloudRDSDatabase> stackList = openstackDBService.getStackList(datacenterId, projectId, instanceId);
        
        /*map存储上层数据库资源数据*/
        if (null != dbList) {
            for (BaseCloudRDSDatabase database : dbList) {
                dbMap.put(database.getDatabaseName(), database);
            }
        }
        
        /*底层数据更新本地数据库*/
        if (null != stackList) {
            for (BaseCloudRDSDatabase baseDB : stackList) {
                if(dbMap.containsKey(baseDB.getDatabaseName())) {
                    //底层数据存在本地数据库中 更新本地数据
                    updateCloudRDSDatabaseFromStack(baseDB);
                } else if (!"test".equals(baseDB.getDatabaseName().toLowerCase().substring(0, Math.min(baseDB.getDatabaseName().length(), 4)))) {
                    /*底层有 上层没有的数据 添加进本地数据库*/
                    databaseDao.save(baseDB);
                }
                stackMap.put(baseDB.getDatabaseName(), baseDB);
            }
        }
        
        /*删除本地存在 底层不存在的数据资源*/
        if (null != dbList) {
            for (BaseCloudRDSDatabase database : dbList) {
                //删除本地数据库中不存在于底层的数据
                if (!stackMap.containsKey(database.getDatabaseName())) {
                    databaseService.delDatabaseInDB(database.getDatabaseId());
                    ecmcLogService.addLog("同步资源清除数据", toType(database), "数据库", database.getPrjId(), 1, database.getDatabaseId(), null);
                    
                    JSONObject json = new JSONObject();
                    json.put("resourceType", ResourceSyncConstant.DATABASE);
                    json.put("resourceId", database.getDatabaseId());
                    json.put("resourceName", "数据库");
                    json.put("synTime", new Date());
                    jedisUtil.push(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, json.toJSONString());
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<BaseCloudRDSDatabase> queryCloudRDSDatabaseListByDcId(String instanceId) {
        StringBuffer hql = new StringBuffer();
        hql.append(" from BaseCloudRDSDatabase ");
        hql.append(" where instanceId = ? ");
        return databaseDao.find(hql.toString(), new Object[]{instanceId});
    }
    
    private void updateCloudRDSDatabaseFromStack (BaseCloudRDSDatabase baseDB) {
        StringBuffer sql = new StringBuffer();
        sql.append(" UPDATE                 ");
        sql.append("    cloud_rdsdatabase   ");
        sql.append(" SET                    ");
        sql.append("    character_set = ?   ");
        sql.append(" WHERE                  ");
        sql.append("    database_name = ?   ");
        sql.append("    AND instance_id = ? ");
        databaseDao.execSQL(sql.toString(), new Object[]{
            baseDB.getCharacterSet()
            ,baseDB.getDatabaseName()
            ,baseDB.getInstanceId()
        });
    }
    
    /**
     * 拼装同步删除发送日志的资源类型
     * @author gaoxiang
     * @param database
     * @return
     */
    private String toType(BaseCloudRDSDatabase database) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuffer resourceType = new StringBuffer();
        resourceType.append(ResourceSyncConstant.DATABASE);
        if(null != database && null != database.getCreateTime()){
            resourceType.append(ResourceSyncConstant.SEPARATOR).append("创建时间：").append(sdf.format(database.getCreateTime()));
        }
        return resourceType.toString();
    }
    
}
