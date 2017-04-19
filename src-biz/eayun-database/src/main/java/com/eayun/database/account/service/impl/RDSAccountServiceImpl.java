package com.eayun.database.account.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.StringUtil;
import com.eayun.database.account.dao.RDSAccountDao;
import com.eayun.database.account.model.BaseCloudRDSAccount;
import com.eayun.database.account.model.CloudRDSAccount;
import com.eayun.database.account.service.RDSAccountService;
import com.eayun.database.database.model.CloudRDSDatabase;
import com.eayun.database.database.service.RDSDatabaseService;
import com.eayun.database.relation.model.CloudRDSRelation;
import com.eayun.database.relation.service.RDSRelationService;
import com.eayun.eayunstack.model.RDSDBUser;
import com.eayun.eayunstack.service.OpenstackDBUserService;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.project.service.ProjectService;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.CloudProject;

@Service
@Transactional
public class RDSAccountServiceImpl implements RDSAccountService {
    private static final Logger log = LoggerFactory.getLogger(RDSAccountServiceImpl.class);
    @Autowired
    private RDSAccountDao accountDao;
    @Autowired
    private RDSDatabaseService databaseService;
    @Autowired
    private RDSRelationService relationService;
    @Autowired
    private OpenstackDBUserService openstackDBUserService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private JedisUtil jedisUtil;
    @Autowired
    private MessageCenterService messageCenterService;
    
    @Override
    public Page getAccountPageList (Page page, ParamsMap paramsMap) throws Exception {
        List<String> list = new ArrayList<String>();
        list.add(String.valueOf(paramsMap.getParams().get("instanceId")));
        String status = String.valueOf(paramsMap.getParams().get("status"));
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT                                     ");
        sql.append("    account.account_id                      ");
        sql.append("    ,account.account_name                   ");
        sql.append("    ,account.instance_id                    ");
        sql.append("    ,instance.rds_name                      ");
        sql.append("    ,instance.rds_status                    ");
        sql.append("    ,instance.charge_state                  ");
        sql.append("    ,account.prj_id                         ");
        sql.append("    ,account.dc_id                          ");
        sql.append("    ,account.create_time                    ");
        sql.append("    ,account.remark                         ");
        sql.append(" FROM                                       ");
        sql.append("    cloud_rdsdbaccount account              ");
        sql.append(" LEFT JOIN                                  ");
        sql.append("    cloud_rdsinstance instance              ");
        sql.append(" ON                                         ");
        sql.append("    account.instance_id = instance.rds_id   ");
        sql.append(" WHERE                                      ");
        sql.append("    account.instance_id = ?                 ");
        sql.append(" ORDER BY create_time DESC                  ");
        QueryMap queryMap = new QueryMap();
        queryMap.setPageNum(paramsMap.getPageNumber());
        queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
        page = accountDao.pagedNativeQuery(sql.toString(), queryMap, list.toArray());
        List<Object> result = (List<Object>) page.getResult();
        List<Object> showList = new ArrayList<Object>();
        boolean hasRoot = false;
        if (null != result && result.size() > 0) {
            showList.add(new Object());
            for (Object obj : result) {
                Object[] objs = (Object[]) obj;
                CloudRDSAccount account = new CloudRDSAccount();
                int index = 0;
                account.setAccountId(String.valueOf(objs[index++]));
                account.setAccountName(String.valueOf(objs[index++]));
                account.setInstanceId(String.valueOf(objs[index++]));
                account.setInstanceName(String.valueOf(objs[index++]));
                account.setStatus(String.valueOf(objs[index++]));
                account.setChargeState(String.valueOf(objs[index++]));
                account.setPrjId(String.valueOf(objs[index++]));
                account.setDcId(String.valueOf(objs[index++]));
                account.setCreateTime((Date) objs[index++]);
                String remark = String.valueOf(objs[index++]);
                account.setRemark(StringUtil.isEmpty(remark) ? "" : remark);
                List<String> dbIdList = new ArrayList<String>();
                List<String> dbNameList = new ArrayList<String>();
                if ("root".equals(account.getAccountName())) {
                    List<CloudRDSDatabase> dbList = databaseService.getDatabaseList(account.getInstanceId());
                    if (null != dbList && dbList.size() > 0) {
                        for (CloudRDSDatabase db : dbList) {
                            dbIdList.add(db.getDatabaseId());
                            dbNameList.add(db.getDatabaseName());
                        }
                    }
                    account.setDbIdList(dbIdList);
                    account.setDbNameList(dbNameList);
                    if (("0".equals(status) && account.getDbIdList().size() > 0) || ("1".equals(status) && account.getDbIdList().size() == 0)) {
                        continue;
                    }
                    showList.set(0, account);
                    hasRoot = true;
                } else {
                    List<CloudRDSRelation> relationList = relationService.getRDSRelationList(account.getAccountId(), null);
                    if (null != relationList && relationList.size() > 0) {
                        for (CloudRDSRelation relation : relationList) {
                            String dbName = databaseService.getDBNameById(relation.getDatabaseId());
                            dbIdList.add(relation.getDatabaseId());
                            dbNameList.add(dbName);
                        }
                    }
                    account.setDbIdList(dbIdList);
                    account.setDbNameList(dbNameList);
                    if (("0".equals(status) && account.getDbIdList().size() > 0) || ("1".equals(status) && account.getDbIdList().size() == 0)) {
                        continue;
                    }
                    showList.add(account);
                }
            }
            if (!hasRoot) {
                showList.remove(0);
            }
            page.setResult(showList);
        }
        return page;
    }
    
    private List<CloudRDSAccount> getAllAccountByInstanceId (String instanceId) {
        List<CloudRDSAccount> accountList = new ArrayList<CloudRDSAccount>();
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT                 ");
        sql.append("    account_id          ");
        sql.append("    ,account_name       ");
        sql.append("    ,instance_id        ");
        sql.append("    ,dc_id              ");
        sql.append(" FROM                   ");
        sql.append("    cloud_rdsdbaccount  ");
        sql.append(" WHERE                  ");
        sql.append("    instance_id = ?     ");
        List<Object> result = accountDao.createSQLNativeQuery(sql.toString(), instanceId).getResultList();
        for (Object obj : result) {
            Object[] objs = (Object[]) obj;
            CloudRDSAccount account = new CloudRDSAccount();
            account.setAccountId(String.valueOf(objs[0]));
            account.setAccountName(String.valueOf(objs[1]));
            account.setInstanceId(String.valueOf(objs[2]));
            account.setDcId(String.valueOf(objs[3]));
            accountList.add(account);
        }
        return accountList;
    }
    
    @Override
    public boolean checkAccountNameExist (CloudRDSAccount account) {
        if (StringUtil.isEmpty(account.getInstanceId()) || StringUtil.isEmpty(account.getAccountName())) {
            return true;
        }
        List<CloudRDSAccount> accountList = getAllAccountByInstanceId(account.getInstanceId());
        for (CloudRDSAccount user : accountList) {
            if (user.getAccountName().equals(account.getAccountName())) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public CloudRDSAccount createAccount (CloudRDSAccount account) throws AppException {
        if ("slave_".equals(account.getAccountName().substring(0, Math.min(account.getAccountName().length(), 6)))) {
            throw new AppException("不支持创建以slave_开头为名字的数据库账户");
        }
        JSONObject json = new JSONObject();
        JSONArray users = new JSONArray();
        JSONObject data = new JSONObject();
        JSONArray databases = new JSONArray();
        if (null != account.getDbNameList() && account.getDbNameList().size() > 0) {
            for (String dbName : account.getDbNameList()) {
                JSONObject database = new JSONObject();
                database.put("name", dbName);
                databases.add(database);
            }
        }
        data.put("databases", databases);
        data.put("name", account.getAccountName());
        data.put("password", account.getPassword());
        users.add(data);
        json.put("users", users);
        try {
            openstackDBUserService.create(account.getDcId(), account.getPrjId(), account.getInstanceId(), json);
            return createAccountInDB(account, false, null);
        } catch (AppException e) {
            log.error(e.toString(), e);
            throw e;
        }
    }
    
    @Override
    public CloudRDSAccount createAccountInDB (CloudRDSAccount account, boolean isFromStack, Date date) {
        BaseCloudRDSAccount baseAccount = new BaseCloudRDSAccount();
        BeanUtils.copyPropertiesByModel(baseAccount, account);
        if (!isFromStack) {
            baseAccount.setCreateTime(new Date());
        } else {
            baseAccount.setCreateTime(date);
        }
        baseAccount = accountDao.save(baseAccount);
        if (isFromStack) {
            account = assembleDbIdList(account);
        }
        relationService.addRDSRelations(baseAccount.getAccountId(), account.getDbIdList());
        return account;
    }
    /**
     * 如果是底层同步上来的账号数据，那么他的数据库id列表必然是空的，此时需要重新为他组装dbIdList
     * @param account
     */
    private CloudRDSAccount assembleDbIdList (CloudRDSAccount account) {
        List<String> dbIdList = new ArrayList<String>();
        for (String dbName : account.getDbNameList()) {
            if (!"test".equals(dbName.toLowerCase().substring(0, Math.min(dbName.length(), 4)))) {
                String dbId = databaseService.getDBIdByName(account.getInstanceId(), dbName);
                dbIdList.add(dbId);
            }
        }
        account.setDbIdList(dbIdList);
        return account;
    }
    
    @Override
    public CloudRDSAccount createAccountRoot (CloudRDSAccount root) throws AppException {
        BaseCloudRDSAccount baseRoot = new BaseCloudRDSAccount();
        BeanUtils.copyPropertiesByModel(baseRoot, root);
        baseRoot.setPassword("");
        baseRoot.setCreateTime(new Date());
        accountDao.save(baseRoot);
        if (null != root.getPassword() && !"null".equals(root.getPassword())) {
            resetRootPassword(root, false);
        }
        return root;
    }
    
    @Override
    public boolean resetRootPassword (CloudRDSAccount root, boolean isReset) throws AppException {
        try {
            JSONObject data = new JSONObject();
            data.put("password", root.getPassword());
            RDSDBUser dbUser = openstackDBUserService.enableRootUser(root.getDcId(), root.getPrjId(), root.getInstanceId(), data);
            log.info("云数据库" + root.getInstanceId() + "root账户重置密码为：" + dbUser.getPassword());
            if (isReset) {
                BaseCloudProject project = projectService.findProject(root.getPrjId());
                messageCenterService.ResetcloudDataBaseRootPassWord(project.getCustomerId(), root.getInstanceName());
            }
        } catch (AppException e) {
            log.error(e.toString(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.toString(), e);
            throw new AppException(e.getMessage());
        }
        return true;
    }
    
    @Override
    public boolean deleteAccount (CloudRDSAccount account) throws AppException {
        BaseCloudRDSAccount isNull = accountDao.findOne(account.getAccountId());
        if (isNull == null) {
            throw new AppException("account does not exist");
        }
        if (!"root".equals(account.getAccountName())) {
            boolean deleteFlag = openstackDBUserService.delete(account.getDcId(), account.getInstanceId(), account.getAccountName());
            if (deleteFlag) {
                deleteAccountInDB(account, false);
            }
            return deleteFlag;
        } else {
            deleteAccountInDB(account, false);
            return false;
        }
    }
    
    @Override
    public boolean deleteAccountInDB (CloudRDSAccount account, boolean isFromStack) {
        if (isFromStack) {
            account.setAccountId(getAccountIdByName(account.getInstanceId(), account.getAccountName()));
        }
        if (!StringUtil.isEmpty(account.getAccountId())) {
            relationService.deleteRDSRelations(account.getAccountId(), null);
            accountDao.delete(account.getAccountId());
            return true;
        }
        return false;
    }
    
    private String getAccountIdByName (String instanceId, String name) {
        StringBuffer hql = new StringBuffer();
        hql.append(" FROM                   ");
        hql.append("    BaseCloudRDSAccount ");
        hql.append(" WHERE                  ");
        hql.append("    instanceId = ?      ");
        hql.append("    and accountName = ? ");
        List<BaseCloudRDSAccount> list = accountDao.find(hql.toString(), new Object[]{instanceId, name});
        if (null != list && list.size() > 0) {
            return list.get(0).getAccountId();
        }
        return null;
    }
    
    @Override
    public boolean updateAccessAccount (CloudRDSAccount account, boolean isFromStack) throws AppException {
        List<String> adds = new ArrayList<String>();
        List<String> dels = new ArrayList<String>();
        Map<String, String> oriMap = new HashMap<String, String>();
        Map<String, String> map = new HashMap<String, String>();
        List<CloudRDSRelation> oriList = new ArrayList<CloudRDSRelation>();
        if (isFromStack) {
            getAccountIdByName(account.getInstanceId(), account.getAccountName());
            account = assembleDbIdList(account);
        }
        oriList = relationService.getRDSRelationList(account.getAccountId(), null);
        
        List<String> list = account.getDbIdList();
        
        if (null != oriList && oriList.size() > 0) {
            for (CloudRDSRelation relation : oriList) {
                oriMap.put(relation.getDatabaseId(), null);
            }
        }
        
        if (null != list && list.size() > 0) {
            for (String dbId : list) {
                if (!oriMap.containsKey(dbId)) {
                    adds.add(dbId);
                }
                map.put(dbId, null);
            }
        }
        
        if (null != oriList && oriList.size() > 0) {
            for (CloudRDSRelation relation : oriList) {
                if (!map.containsKey(relation.getDatabaseId())) {
                    dels.add(relation.getDatabaseId());
                }
            }
        }
        
        if (null != adds && adds.size() > 0) {
            grantRDSAccount(adds, account, isFromStack);
        }
        
        if (null != dels && dels.size() > 0) {
            revokeRDSAccount(dels, account, isFromStack);
        }
        
        return true;
    }
    
    /**
     * 添加权限
     * @param dbIdList
     * @param account
     * @throws AppException
     */
    private void grantRDSAccount (List<String> dbIdList, CloudRDSAccount account, boolean isFromStack) throws AppException {
        if (!isFromStack) {
            JSONObject json = new JSONObject();
            JSONArray datbases = new JSONArray();
            if (null != dbIdList && dbIdList.size() > 0) {
                for (String dbId : dbIdList) {
                    JSONObject database = new JSONObject();
                    String dbName = databaseService.getDBNameById(dbId);
                    if (null == dbName) {
                        throw new AppException("数据库已经不存在");
                    }
                    database.put("name", dbName);
                    datbases.add(database);
                }
            }
            json.put("databases", datbases);
            openstackDBUserService.grantAccess(account.getDcId(), account.getInstanceId(), account.getAccountName(), json);
        } else {
            account.setAccountId(getAccountIdByName(account.getInstanceId(), account.getAccountName()));
        }
        relationService.addRDSRelations(account.getAccountId(), dbIdList);
    }
    
    /**
     * 撤销权限
     * @param dbIdList
     * @param account
     * @throws AppException
     */
    private void revokeRDSAccount (List<String> dbIdList, CloudRDSAccount account, boolean isFromStack) throws AppException {
        if (null != dbIdList && dbIdList.size() > 0) {
            for (String dbId : dbIdList) {
                if (!isFromStack) {
                    String dbName = databaseService.getDBNameById(dbId);
                    openstackDBUserService.revokeAccess(account.getDcId(), account.getInstanceId(), account.getAccountName(), dbName);
                } else {
                    account.setAccountId(getAccountIdByName(account.getInstanceId(), account.getAccountName()));
                }
                relationService.deleteRDSRelations(account.getAccountId(), dbId);
            }
        }
    }
    
    @Override
    public boolean updatePasswordAccount (CloudRDSAccount account) throws AppException {
        CloudProject project = projectService.findProject(account.getPrjId());
        
        JSONObject json = new JSONObject();
        JSONArray users = new JSONArray();
        JSONObject user = new JSONObject();
        user.put("name", account.getAccountName());
        user.put("password", account.getPassword());
        users.add(user);
        json.put("users", users);
        
        openstackDBUserService.update(account.getDcId(), account.getInstanceId(), json);
        
        List<String> params = new ArrayList<String>();
        StringBuffer sql = new StringBuffer();
        sql.append(" UPDATE cloud_rdsdbaccount ");
        sql.append(" SET password = ? ");
        sql.append(" WHERE account_id = ? ");
        params.add(account.getPassword());
        params.add(account.getAccountId());
        
        accountDao.execSQL(sql.toString(), params.toArray());
        messageCenterService.cloudDataBaseNoAdminPassWordUpdate(project.getCustomerId(), account.getInstanceName(), account.getAccountName());
        return true;
    }
    
    @Override
    public List<CloudRDSDatabase> getAllDatabaseListByInstanceId (String instanceId) throws Exception {
        return databaseService.getDatabaseList(instanceId);
    }
    
    @Override
    public List<CloudRDSRelation> getDatabaseListManagedByAccount (String accountId) {
        return relationService.getRDSRelationList(accountId, null);
    }
    
    @Override
    public void deleteAllAccountByInstanceId (String instanceId) throws AppException {
        List<CloudRDSAccount> list = getAllAccountByInstanceId(instanceId);
        for (CloudRDSAccount account : list) {
//            deleteAccount(account);这里只是上层做用户删除的业务，底层不删除
            deleteAccountInDB(account, false);
        }
    }
    
    @Override
    public void synchronAccountCreate (String dcId, String prjId, String instanceId) throws Exception {
        List<CloudRDSAccount> stackList = openstackDBUserService.getStackList(dcId, prjId, instanceId);
        
        if (null != stackList && stackList.size() > 0) {
            for (CloudRDSAccount account : stackList) {
                if (!"slave_".equals(account.getAccountName().toLowerCase().substring(0, Math.min(account.getAccountName().length(), 6)))) {
                    createAccountInDB(account, true, new Date());
                }
            }
        }
    }
}
