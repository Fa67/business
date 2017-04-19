package com.eayun.schedule.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceSyncConstant;
import com.eayun.common.constant.SyncProgress.SyncByDatacenterTypes;
import com.eayun.common.constant.SyncProgress.SyncType;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.sync.SyncProgressUtil;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.service.OpenstackPortMappingService;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.schedule.service.CloudPortMappingService;
import com.eayun.virtualization.dao.PortMappingDao;
import com.eayun.virtualization.model.BaseCloudPortMapping;
@Transactional
@Service
public class CloudPortMappingServiceImpl implements CloudPortMappingService {
    private static final Logger log = LoggerFactory
            .getLogger(CloudPortMappingServiceImpl.class);
    @Autowired
    private JedisUtil jedisUtil;
    @Autowired
    private PortMappingDao pmDao;
    @Autowired
    private OpenstackPortMappingService openstackPortMappingService;
    @Autowired
    private EcmcLogService ecmcLogService;
    @Autowired
    private SyncProgressUtil syncProgressUtil;
    @Override
    public void synchData(BaseDcDataCenter dataCenter) throws Exception {
        Map<String, BaseCloudPortMapping> dbMap = new HashMap<String, BaseCloudPortMapping>();
        Map<String, BaseCloudPortMapping> stackMap = new HashMap<String, BaseCloudPortMapping>();
        
        List<BaseCloudPortMapping> dbList = queryCloudPortMappingListByDcId(dataCenter.getId());
        
        List<BaseCloudPortMapping> stackList = openstackPortMappingService.getStackList(dataCenter);
        
        /*map存储上层数据库资源数据*/
        if (null != dbList) {
            for (BaseCloudPortMapping portMapping : dbList) {
                dbMap.put(portMapping.getPmId(), portMapping);
            }
        }
        long total = stackList == null ? 0L : stackList.size();
        syncProgressUtil.initResourceTotal(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.PORT_MAPPING, total);
        /*底层数据更新本地数据库*/
        if (null != stackList) {
            for (BaseCloudPortMapping portMapping : stackList) {
                if(dbMap.containsKey(portMapping.getPmId())) {
                    //底层数据存在本地数据库中 更新本地数据
                    updateCloudPortMappingFromStack(portMapping);
                } else {
                    /*底层有 上层没有的数据 添加进本地数据库*/
                    pmDao.save(portMapping);
                }
                stackMap.put(portMapping.getPmId(), portMapping);
                syncProgressUtil.incrResourceDone(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.PORT_MAPPING);
            }
        }
        
        /*删除本地存在 底层不存在的数据资源*/
        if (null != dbList) {
            for (BaseCloudPortMapping portMapping : dbList) {
                //删除本地数据库中不存在于底层的数据
                if (!stackMap.containsKey(portMapping.getPmId())) {
                    pmDao.delete(portMapping.getPmId());
                    ecmcLogService.addLog("同步资源清除数据", toType(portMapping), "端口映射", portMapping.getPrjId(), 1, portMapping.getPmId(), null);
                    
                    JSONObject json = new JSONObject();
                    json.put("resourceType", ResourceSyncConstant.PORTMAPPING);
                    json.put("resourceId", portMapping.getPmId());
                    json.put("resourceName", "端口映射");
                    json.put("synTime", new Date());
                    jedisUtil.push(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, json.toJSONString());
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<BaseCloudPortMapping> queryCloudPortMappingListByDcId(String datacenterId) {
        StringBuffer hql = new StringBuffer();
        hql.append(" from BaseCloudPortMapping ");
        hql.append(" where dcId = ? ");
        return pmDao.find(hql.toString(), new Object[]{datacenterId});
    }
    
    private boolean updateCloudPortMappingFromStack(BaseCloudPortMapping portMapping) {
        boolean flag = false;
        try {
            StringBuffer sql = new StringBuffer();
            sql.append(" update cloud_portmapping ");
            sql.append(" set ");
            sql.append("    dc_id = ? ");
            sql.append("    ,prj_id = ? ");
            sql.append("    ,protocol = ? ");
            sql.append("    ,resource_id = ? ");
            sql.append("    ,resource_port = ? ");
            sql.append("    ,destiny_ip = ? ");
            sql.append("    ,destiny_port = ? ");
            sql.append(" where ");
            sql.append("    pm_id = ? ");
            pmDao.execSQL(sql.toString(), new Object[]{
                portMapping.getDcId(),
                portMapping.getPrjId(),
                portMapping.getProtocol(),
                portMapping.getResourceId(),
                portMapping.getResourcePort(),
                portMapping.getDestinyIp(),
                portMapping.getDestinyPort(),
                
                portMapping.getPmId()
            });
        } catch (Exception e) {
            flag = false;
            log.error(e.getMessage(),e);
            throw e;
        }
        return flag;
    }
    /**
     * 拼装同步删除发送日志的资源类型
     * @author gaoxiang
     * @param portMapping
     * @return
     */
    private String toType(BaseCloudPortMapping portMapping) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuffer resourceType = new StringBuffer();
        resourceType.append(ResourceSyncConstant.PORTMAPPING);
        if(null != portMapping && null != portMapping.getCreateTime()){
        	resourceType.append(ResourceSyncConstant.SEPARATOR).append("创建时间：").append(sdf.format(portMapping.getCreateTime()));
        }
        return resourceType.toString();
    }
}
