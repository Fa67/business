package com.eayun.log.service.impl;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.util.DateUtil;
import com.eayun.log.dao.SysLogDao;
import com.eayun.log.service.DeleteLogService;
import com.mongodb.WriteResult;

@Transactional
@Service
public class DeleteLogServiceImpl implements DeleteLogService {
    
    private static final Logger log = LoggerFactory.getLogger(DeleteLogServiceImpl.class);
    
    @Autowired
    private SysLogDao           logDao;
    
    @Autowired
    private MongoTemplate mongoTemplate;

    @SuppressWarnings("unused")
    @Override
    public void deleteLog() {
        log.info("删除日志");
        Date d = new Date();
        Date day = DateUtil.addDay(d, new int[] {0,0,-180});
        /*SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String sql = "delete from sys_log where act_time<'"
                     + df.format(day) + "'";
        logDao.execSQL(sql);*/
        
        Query query = new Query();
        query.addCriteria(Criteria.where("actTime").lt(day));
        WriteResult result = mongoTemplate.remove(query, JSONObject.class, MongoCollectionName.LOG_ECSC);
    }

}
