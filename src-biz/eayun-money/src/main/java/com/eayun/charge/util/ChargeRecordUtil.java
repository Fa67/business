package com.eayun.charge.util;

import java.util.Date;

import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eayun.charge.model.ChargeRecord;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.price.bean.ParamBean;

/**
 * 计费清单工单类
 *
 * @Filename: ChargeRecordUtil.java
 * @Description:
 * @Version: 1.0
 * @Author: zhangfan
 * @Email: fan.zhang@eayun.com
 * @History:<br> <li>Date: 2016年8月30日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
@Component
public class ChargeRecordUtil {

    @Autowired
    private MongoTemplate mongoTemplate;

    public ChargeRecord parseToObject(Message msg){
        byte[] bytes = msg.getBody();
        String msgStr = new String(bytes);
        JSONObject msgJSON = JSONObject.parseObject(msgStr);
        ChargeRecord chargeRecord = JSON.toJavaObject(msgJSON, ChargeRecord.class);
        ParamBean pb = chargeRecord.getParam();
        if(pb!=null){
            chargeRecord.setBillingFactorStr(JSONObject.toJSONString(pb));
        }
        return chargeRecord;
    }

    public JSONObject parseToJSON(Message msg){
        byte[] bytes = msg.getBody();
        String msgStr = new String(bytes);
        JSONObject msgJSON = JSONObject.parseObject(msgStr);
        return msgJSON;
    }

    public void doLog(Message msg, Exception e){
        JSONObject log = parseToJSON(msg);
        log.put("timestamp", new Date());
        log.put("exception", e.getMessage());
        mongoTemplate.insert(log, MongoCollectionName.LOG_CHARGE_RECORD_OP_FAIL);
    }

    public void doLog(Message msg, Exception e, String opType) {
        JSONObject log = parseToJSON(msg);
        log.put("timestamp", new Date());
        log.put("exception", e.getMessage());
        log.put("opType", opType);
        mongoTemplate.insert(log, MongoCollectionName.LOG_CHARGE_RECORD_OP_FAIL);
    }
}
