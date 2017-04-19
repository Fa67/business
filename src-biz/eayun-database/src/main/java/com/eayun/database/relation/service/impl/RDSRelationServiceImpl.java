package com.eayun.database.relation.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.database.relation.dao.RDSRelationDao;
import com.eayun.database.relation.model.BaseCloudRDSRelation;
import com.eayun.database.relation.model.CloudRDSRelation;
import com.eayun.database.relation.service.RDSRelationService;

@Service
@Transactional
public class RDSRelationServiceImpl implements RDSRelationService {
    @Autowired
    private RDSRelationDao relationDao;
    @Override
    public List<CloudRDSRelation> getRDSRelationList(String accountId, String databaseId) {
        List<CloudRDSRelation> relationList = new ArrayList<CloudRDSRelation>();
        List<String> params = new ArrayList<String>();
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT                                     ");
        sql.append("    relation.relation_id                    ");
        sql.append("    ,relation.account_id                    ");
        sql.append("    ,account.account_name                   ");
        sql.append("    ,relation.database_id                   ");
        sql.append("    ,db.database_name                       ");
        sql.append(" FROM                                       ");
        sql.append("    cloud_rdsdbaccountrelation relation     ");
        sql.append(" LEFT JOIN                                  ");
        sql.append("    cloud_rdsdatabase db                    ");
        sql.append(" ON                                         ");
        sql.append("    relation.database_id = db.database_id   ");
        sql.append(" LEFT JOIN                                  ");
        sql.append("    cloud_rdsdbaccount account              ");
        sql.append(" ON                                         ");
        sql.append("    relation.account_id = account.account_id");
        sql.append(" WHERE                                      ");
        sql.append("    1 = 1                                   ");
        if (!StringUtils.isEmpty(accountId)) {
            sql.append(" AND relation.account_id = ?            ");
            params.add(accountId);
        }
        if (!StringUtils.isEmpty(databaseId)) {
            sql.append(" AND relation.database_id = ?           ");
            params.add(databaseId);
        }
        List<Object> result = relationDao.createSQLNativeQuery(sql.toString(), params.toArray()).getResultList();
        for (Object obj : result) {
            Object[] objs = (Object[]) obj;
            CloudRDSRelation relation = new CloudRDSRelation();
            int index = 0;
            relation.setRelationId(String.valueOf(objs[index++]));
            relation.setAccountId(String.valueOf(objs[index++]));
            relation.setAccountName(String.valueOf(objs[index++]));
            relation.setDatabaseId(String.valueOf(objs[index++]));
            relation.setDatabaseName(String.valueOf(objs[index++]));
            relationList.add(relation);
        }
        return relationList;
    }
    @Override
    public void addRDSRelations (String accountId, List<String> dbIdList) {
        if (null != dbIdList && dbIdList.size() > 0) {
            for (String dbId : dbIdList) {
                BaseCloudRDSRelation baseRelation = new BaseCloudRDSRelation();
                baseRelation.setAccountId(accountId);
                baseRelation.setDatabaseId(dbId);
                relationDao.save(baseRelation);
            }
        }
    }
    @Override
    public boolean deleteRDSRelations (String accountId, String databaseId) {
        if (StringUtils.isEmpty(accountId) && StringUtils.isEmpty(databaseId)) {
            return false;
        }
        List<String> params = new ArrayList<String>();
        StringBuffer sql = new StringBuffer();
        sql.append(" DELETE FROM cloud_rdsdbaccountrelation ");
        sql.append(" WHERE 1 = 1 ");
        if (!StringUtils.isEmpty(accountId)) {
            sql.append(" and account_id = ? ");
            params.add(accountId);
        }
        if (!StringUtils.isEmpty(databaseId)) {
            sql.append(" and database_id = ? ");
            params.add(databaseId);
        }
        relationDao.execSQL(sql.toString(), params.toArray());
        return true;
    }
}
