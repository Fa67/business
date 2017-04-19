package com.eayun.database.account.service.impl;

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
import com.eayun.common.util.BeanUtils;
import com.eayun.database.account.dao.RDSAccountDao;
import com.eayun.database.account.model.BaseCloudRDSAccount;
import com.eayun.database.account.model.CloudRDSAccount;
import com.eayun.database.account.service.CloudDBUserService;
import com.eayun.database.account.service.RDSAccountService;
import com.eayun.eayunstack.service.OpenstackDBUserService;
import com.eayun.log.ecmcsevice.EcmcLogService;
@Transactional
@Service
public class CloudDBUserServiceImpl implements CloudDBUserService {

    @Autowired
    private JedisUtil jedisUtil;
    
    @Autowired
    private RDSAccountDao accountDao;
    
    @Autowired
    private RDSAccountService accountService;
    
    @Autowired
    private OpenstackDBUserService openstackDBUserService;
    
    @Autowired
    private EcmcLogService ecmcLogService;
    
    public void synchData(String datacenterId, String projectId, String instanceId) throws Exception {
        /**
         * 1、分别获取底层账户数据资源列表和上层账户数据资源列表
         * 2、进行比对：
         *      ！如果底层存在上层也存在，则依照底层账户授权的数据库列表更新上层数据库账户关系表，并修改账户状态
         *      ！！如果底层存在上层不存在的资源，则在上层数据库当中添加记录
         *      ！！！如果上层存在底层不存在的资源，则调用上层数据库删除记录的接口，并向Redis同步数据中心已删除资源队列当中插入删除记录
         */
        Map<String, BaseCloudRDSAccount> dbMap = new HashMap<String, BaseCloudRDSAccount>();
        Map<String, CloudRDSAccount> stackMap = new HashMap<String, CloudRDSAccount>();
        
        List<BaseCloudRDSAccount> dbList = queryCloudRDSAccountListByDcId(instanceId);
        
        List<CloudRDSAccount> stackList = openstackDBUserService.getStackList(datacenterId, projectId, instanceId);
        
        /*map存储上层数据库资源数据*/
        if (null != dbList) {
            for (BaseCloudRDSAccount account : dbList) {
                dbMap.put(account.getAccountName(), account);
            }
        }
        
        /*底层数据更新本地数据库*/
        if (null != stackList) {
            for (CloudRDSAccount account : stackList) {
                if(dbMap.containsKey(account.getAccountName())) {
                    //底层数据存在本地数据库中 更新本地数据
                    //TODO
//                    updateCloudPortMappingFromStack(baseDB);
                    accountService.updateAccessAccount(account, true);
                } else if (!"slave_".equals(account.getAccountName().substring(0, Math.min(account.getAccountName().length(), 6)))) {
                    /*底层有 上层没有的数据 添加进本地数据库*/
                    accountService.createAccountInDB(account, true, null);
                }
                stackMap.put(account.getAccountName(), account);
            }
        }
        
        /*删除本地存在 底层不存在的数据资源*/
        if (null != dbList) {
            for (BaseCloudRDSAccount account : dbList) {
                //删除本地数据库中不存在于底层的数据
                if (!stackMap.containsKey(account.getAccountName()) && !"root".equals(account.getAccountName())) {
                    CloudRDSAccount acc = new CloudRDSAccount();
                    BeanUtils.copyPropertiesByModel(acc, account);
                    accountService.deleteAccountInDB(acc, true);
                    ecmcLogService.addLog("同步资源清除数据", toType(account), "数据库", account.getPrjId(), 1, account.getAccountId(), null);
                    
                    JSONObject json = new JSONObject();
                    json.put("resourceType", ResourceSyncConstant.DBACCOUNT);
                    json.put("resourceId", account.getAccountId());
                    json.put("resourceName", "数据库账户");
                    json.put("synTime", new Date());
                    jedisUtil.push(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, json.toJSONString());
                }
            }
        }
    }
    
    private List<BaseCloudRDSAccount> queryCloudRDSAccountListByDcId (String instanceId) {
        StringBuffer hql = new StringBuffer();
        hql.append(" from BaseCloudRDSAccount ");
        hql.append(" where instanceId = ? ");
        return accountDao.find(hql.toString(), new Object[]{instanceId});
    }
    
    /**
     * 拼装同步删除发送日志的资源类型
     * @author gaoxiang
     * @param database
     * @return
     */
    private String toType(BaseCloudRDSAccount account) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuffer resourceType = new StringBuffer();
        resourceType.append(ResourceSyncConstant.DBACCOUNT);
        if(null != account && null != account.getCreateTime()){
            resourceType.append(ResourceSyncConstant.SEPARATOR).append("创建时间：").append(sdf.format(account.getCreateTime()));
        }
        return resourceType.toString();
    }
}
