/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.common.sync;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eayun.common.constant.SyncProgress.SyncByDatacenterNames;
import com.eayun.common.constant.SyncProgress.SyncByDatacenterTypes;
import com.eayun.common.constant.SyncProgress.SyncByProjectNames;
import com.eayun.common.constant.SyncProgress.SyncByProjectTypes;
import com.eayun.common.constant.SyncProgress.SyncType;
import com.eayun.common.redis.JedisUtil;

/**
 *                       
 * @Filename: SyncProgressTool.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2017年3月29日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Component
public class SyncProgressUtil {

    /**
     *Comment for <code>MINUS_ONE</code>
     */
    private static final long DECREASE_ONE = -1L;

    /**
     *存放异常的field key
     */
    public static final String               FIELD_KEY_EXCEPTION           = "exception";

    /**
     *同步进度的redis hash key 过期时间（单位：秒）
     */
    public static final long                 SYNC_KEY_EXPIRE_SECONDS       = DateUtils.MILLIS_PER_MINUTE / 1000 * 5;

    /**
     *按项目同步，项目的总数
     */
    public static final String               BYPROJECT_TOTAL               = "projectTotal";

    /**
     *按项目同步，同步完成的项目数
     */
    public static final String               BYPROJECT_DONE                = "projectDone";

    /**
     * 每同步成功后的增长数
     */
    public static final long                 INCREASE_ONE             = 1L;

    /**
     * '0'
     */
    public static final String               ZERO                          = "0";

    /**
     * 按数据中心同步的资源类型总数
     */
    public static final String               BY_DATACETNER_ITEMS_TOTAL     = "22";

    /**
     * 按项目同步，同步中的项目ID
     */
    public static final String               PROCESS_PROJECT_ID_FIELDKEY   = "syncingProjectId";

    /**
     * 按项目同步，同步中的项目名称
     */
    public static final String               PROCESS_PROJECT_NAME_FIELDKEY = "syncingProjectName";

    /**
     * 总数
     */
    public static final String               FIELD_KEY_TOTAL               = "total";

    /**
     * 符号：':'
     */
    public static final String               SYMBOL_COLON                  = ":";

    /**
     * 同步hash key前缀
     */
    public static final String               SYNC_KEY_PREFIX               = "sync:";

    /**
     * 已完成 field key
     */
    public static final String               FIELD_KEY_DONE                = "done";

    /**
     * 按项目同步项field key 前缀
     */
    public static final String               FEILD_PREFIX_PRJ              = "byproject:";

    /**
     * 按项目同步的资源类型
     */
    private final static Map<String, String> syncByProjectResources        = new LinkedHashMap<String, String>();

    /**
     * 按数据中心同步的资源类型
     */
    private final static Map<String, String> syncByDatacenterResources     = new LinkedHashMap<String, String>();

    static {
        //注意： 这里的顺序须按照数据同步的先后顺序设置
        //按照项目同步的项
        syncByProjectResources.put(SyncByProjectTypes.VM, SyncByProjectNames.VM);
        syncByProjectResources.put(SyncByProjectTypes.DISK, SyncByProjectNames.DISK);
        syncByProjectResources.put(SyncByProjectTypes.FLOAT_IP, SyncByProjectNames.FLOAT_IP);
        syncByProjectResources.put(SyncByProjectTypes.RDS, SyncByProjectNames.RDS);
        syncByProjectResources.put(SyncByProjectTypes.RDS_BAK, SyncByProjectNames.RDS_BAK);

        //按照数据中心同步的项
        syncByDatacenterResources.put(SyncByDatacenterTypes.PROJECT, SyncByDatacenterNames.PROJECT);
        syncByDatacenterResources.put(SyncByDatacenterTypes.NET, SyncByDatacenterNames.NET);
        syncByDatacenterResources.put(SyncByDatacenterTypes.SECURITY_GROUP, SyncByDatacenterNames.SECURITY_GROUP);
        syncByDatacenterResources.put(SyncByDatacenterTypes.SECURITY_GROUP_RULE, SyncByDatacenterNames.SECURITY_GROUP_RULE);
        syncByDatacenterResources.put(SyncByDatacenterTypes.IMAGE, SyncByDatacenterNames.IMAGE);
        syncByDatacenterResources.put(SyncByDatacenterTypes.VOLUME_TYPE, SyncByDatacenterNames.VOLUME_TYPE);
        syncByDatacenterResources.put(SyncByDatacenterTypes.COMPUTE_NODE, SyncByDatacenterNames.COMPUTE_NODE);
        syncByDatacenterResources.put(SyncByDatacenterTypes.FIREWALL, SyncByDatacenterNames.FIREWALL);
        syncByDatacenterResources.put(SyncByDatacenterTypes.FIREWALL_POLICY, SyncByDatacenterNames.FIREWALL_POLICY);
        syncByDatacenterResources.put(SyncByDatacenterTypes.FIREWALL_RULE, SyncByDatacenterNames.FIREWALL_RULE);
        syncByDatacenterResources.put(SyncByDatacenterTypes.FLAVOR, SyncByDatacenterNames.FLAVOR);
        syncByDatacenterResources.put(SyncByDatacenterTypes.SUBNET, SyncByDatacenterNames.SUBNET);
        syncByDatacenterResources.put(SyncByDatacenterTypes.ROUTER, SyncByDatacenterNames.ROUTER);
        syncByDatacenterResources.put(SyncByDatacenterTypes.LB_MEMBER, SyncByDatacenterNames.LB_MEMBER);
        syncByDatacenterResources.put(SyncByDatacenterTypes.LB_POOL, SyncByDatacenterNames.LB_POOL);
        syncByDatacenterResources.put(SyncByDatacenterTypes.LB_MONITOR, SyncByDatacenterNames.LB_MONITOR);
        syncByDatacenterResources.put(SyncByDatacenterTypes.LB_VIP, SyncByDatacenterNames.LB_VIP);
        syncByDatacenterResources.put(SyncByDatacenterTypes.DISK_SNAPSHOT, SyncByDatacenterNames.DISK_SNAPSHOT);
        syncByDatacenterResources.put(SyncByDatacenterTypes.LOAD_BALANCER, SyncByDatacenterNames.LOAD_BALANCER);
        syncByDatacenterResources.put(SyncByDatacenterTypes.OUT_IP, SyncByDatacenterNames.OUT_IP);
        syncByDatacenterResources.put(SyncByDatacenterTypes.VPN, SyncByDatacenterNames.VPN);
        syncByDatacenterResources.put(SyncByDatacenterTypes.PORT_MAPPING, SyncByDatacenterNames.PORT_MAPPING);
    }

    @Autowired
    private JedisUtil jedisUtil;

    public void init(String dcId) {
        jedisUtil.hSetHashMapValue(getSyncKey(dcId), FIELD_KEY_DONE, ZERO);
        jedisUtil.hSetHashMapValue(getSyncKey(dcId), FIELD_KEY_TOTAL, BY_DATACETNER_ITEMS_TOTAL);
        //设置key的过期时间，防止程序crash后无法清理数据
        this.refreshExpireTime(dcId);
    }

    protected void refreshExpireTime(String dcId) {
        if (StringUtils.isNotEmpty(dcId)) {
            jedisUtil.expireKey(getSyncKey(dcId), SYNC_KEY_EXPIRE_SECONDS);
        }
    }

    public void initResourceTotal(String dcId, String syncType, String resourceType, Long total) {
        jedisUtil.hSetHashMapValue(getSyncKey(dcId), getTotalFieldKey(resourceType, syncType), String.valueOf(total));
        jedisUtil.hSetHashMapValue(getSyncKey(dcId), getDoneFieldKey(resourceType, syncType), ZERO);
    }

    /**
     * 部分资源不完全按照项目、数据中心的维度同步，在自身同步维度的循环中调用该方法动态增加资源总数
     * @param dcId
     * @param syncType
     * @param resourceType
     * @param incrTotal
     */
    public void incrResourceTotal(String dcId, String syncType, String resourceType, Long incrTotal) {
        jedisUtil.increaseHMap(getSyncKey(dcId), getTotalFieldKey(resourceType, syncType), incrTotal);
    }

    public void incrResourceDone(String dcId, String syncType, String resourceType) {
        jedisUtil.increaseHMap(getSyncKey(dcId), getDoneFieldKey(resourceType, syncType), INCREASE_ONE);
        //每次资源同步后，更新hash key的过期时间
        this.refreshExpireTime(dcId);
    }

    public void setProcessingProject(String dcId, String projectId, String projectName) {
        jedisUtil.hSetHashMapValue(getSyncKey(dcId), PROCESS_PROJECT_ID_FIELDKEY, projectId);
        jedisUtil.hSetHashMapValue(getSyncKey(dcId), PROCESS_PROJECT_NAME_FIELDKEY, projectName);
    }

    public void initByProjectTotal(String dcId, Long total) {
        jedisUtil.hSetHashMapValue(getSyncKey(dcId), BYPROJECT_TOTAL, String.valueOf(total));
        jedisUtil.hSetHashMapValue(getSyncKey(dcId), BYPROJECT_DONE, ZERO);
    }
    
    public void decrByProjectTotal(String dcId){
        jedisUtil.increaseHMap(getSyncKey(dcId), BYPROJECT_TOTAL, DECREASE_ONE);
    }

    public void incrByProjectDone(String dcId) {
        jedisUtil.increaseHMap(getSyncKey(dcId), BYPROJECT_DONE, INCREASE_ONE);
    }

    public void incrByDatacenterDone(String dcId) {
        jedisUtil.increaseHMap(getSyncKey(dcId), FIELD_KEY_DONE, INCREASE_ONE);
    }

    public void saveExceptionStackTrace(String dcId, Throwable e) {
        if (dcId != null && e != null) {
            StringWriter out = new StringWriter();
            PrintWriter pw = new PrintWriter(out);
            e.printStackTrace(pw);
            jedisUtil.hSetHashMapValue(getSyncKey(dcId), FIELD_KEY_EXCEPTION, out.toString());
        }
    }

    public String getExceptionStackTrace(String dcId) {
        return jedisUtil.getHashMapKeyValue(getSyncKey(dcId), FIELD_KEY_EXCEPTION);
    }
    
    /**
     * 清除按项目同步的所有资源的同步进度
     * @param dcId
     */
    public void clearByProjectResourcesProgress(String dcId){
        Iterator<String> iterator = syncByProjectResources.keySet().iterator();
        while (iterator.hasNext()) {
            String resourceType = iterator.next();
            jedisUtil.hDelHashMapField(getSyncKey(dcId), getTotalFieldKey(resourceType, SyncType.PROJECT));
            jedisUtil.hDelHashMapField(getSyncKey(dcId), getDoneFieldKey(resourceType, SyncType.PROJECT));
        }
    }

    public boolean isSyncing(String dcId) {
        return jedisUtil.isKeyExisted(getSyncKey(dcId)) && getExceptionStackTrace(dcId) == null;
    }

    public Object getSyncProgress(String dcId) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("byProjectItems", getSyncByProjectItems(dcId));
        resultMap.put("byDatacenterItems", getSyncByDatacenterItems(dcId));
        resultMap.put(FIELD_KEY_DONE, jedisUtil.getHashMapKeyValue(getSyncKey(dcId), FIELD_KEY_DONE));
        resultMap.put(FIELD_KEY_TOTAL, jedisUtil.getHashMapKeyValue(getSyncKey(dcId), FIELD_KEY_TOTAL));
        resultMap.put(BYPROJECT_DONE, jedisUtil.getHashMapKeyValue(getSyncKey(dcId), BYPROJECT_DONE));
        resultMap.put(BYPROJECT_TOTAL, jedisUtil.getHashMapKeyValue(getSyncKey(dcId), BYPROJECT_TOTAL));
        resultMap.put(PROCESS_PROJECT_ID_FIELDKEY, jedisUtil.getHashMapKeyValue(getSyncKey(dcId), PROCESS_PROJECT_ID_FIELDKEY));
        resultMap.put(PROCESS_PROJECT_NAME_FIELDKEY, jedisUtil.getHashMapKeyValue(getSyncKey(dcId), PROCESS_PROJECT_NAME_FIELDKEY));
        resultMap.put(FIELD_KEY_EXCEPTION, getExceptionStackTrace(dcId));
        return resultMap;
    }

    protected List<Object> getSyncByProjectItems(String dcId) {
        Iterator<String> iterator = syncByProjectResources.keySet().iterator();
        Map<String, Object> item = null;
        List<Object> byProjectItems = new ArrayList<Object>();
        while (iterator.hasNext()) {
            item = new HashMap<String, Object>();
            String key = iterator.next();
            String name = syncByProjectResources.get(key);
            String done = jedisUtil.getHashMapKeyValue(getSyncKey(dcId), getDoneFieldKey(key, SyncType.PROJECT));
            String total = jedisUtil.getHashMapKeyValue(getSyncKey(dcId), getTotalFieldKey(key, SyncType.PROJECT));
            item.put("resourceType", key);
            item.put("resourceTypeName", name);
            item.put(FIELD_KEY_DONE, done);
            item.put(FIELD_KEY_TOTAL, total);
            byProjectItems.add(item);
        }
        return byProjectItems;
    }

    protected List<Object> getSyncByDatacenterItems(String dcId) {
        Iterator<String> iterator = syncByDatacenterResources.keySet().iterator();
        Map<String, Object> item = null;
        List<Object> byProjectItems = new ArrayList<Object>();
        while (iterator.hasNext()) {
            item = new HashMap<String, Object>();
            String key = iterator.next();
            String name = syncByDatacenterResources.get(key);
            String done = jedisUtil.getHashMapKeyValue(getSyncKey(dcId), getDoneFieldKey(key, SyncType.DATA_CENTER));
            String total = jedisUtil.getHashMapKeyValue(getSyncKey(dcId), getTotalFieldKey(key, SyncType.DATA_CENTER));
            item.put("resourceType", key);
            item.put("resourceTypeName", name);
            item.put(FIELD_KEY_DONE, done);
            item.put(FIELD_KEY_TOTAL, total);
            byProjectItems.add(item);
        }
        return byProjectItems;
    }

    protected String getSyncKey(String dcId) {
        return SYNC_KEY_PREFIX + dcId;
    }

    protected String getTotalFieldKey(String resourceType, String syncType) {
        if (StringUtils.equals(syncType, SyncType.DATA_CENTER)) {
            return resourceType + SYMBOL_COLON + FIELD_KEY_TOTAL;
        } else if (StringUtils.equals(syncType, SyncType.PROJECT)) {
            return FEILD_PREFIX_PRJ + resourceType + SYMBOL_COLON + FIELD_KEY_TOTAL;
        }
        return "";
    }

    protected String getDoneFieldKey(String resourceType, String syncType) {
        if (StringUtils.equals(syncType, SyncType.DATA_CENTER)) {
            return resourceType + SYMBOL_COLON + FIELD_KEY_DONE;
        } else if (StringUtils.equals(syncType, SyncType.PROJECT)) {
            return FEILD_PREFIX_PRJ + resourceType + SYMBOL_COLON + FIELD_KEY_DONE;
        }
        return "";
    }

}
