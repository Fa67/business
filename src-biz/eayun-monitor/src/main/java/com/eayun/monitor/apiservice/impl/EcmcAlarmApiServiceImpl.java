package com.eayun.monitor.apiservice.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.annotation.ApiService;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.monitor.apiservice.EcmcAlarmApiService;
import com.eayun.monitor.apiservice.EcmcMonitorAlarmApiService;
import com.eayun.monitor.dao.EcmcAlarmMessageDao;
import com.eayun.monitor.dao.EcmcAlarmObjectDao;
import com.eayun.monitor.model.BaseEcmcAlarmMessage;
import com.eayun.monitor.model.BaseEcmcAlarmObject;
import com.eayun.monitor.model.EcmcAlarmObject;

/**
 * 
 * Ecmc云主机监控API业务<br>
 * -----------------
 * @author chengxiaodong
 * @date 2016-12-2
 *
 */

@Service
@Transactional
public class EcmcAlarmApiServiceImpl  implements EcmcAlarmApiService {

	 @Autowired
	 private EcmcMonitorAlarmApiService  ecmcMonitorAlarmApiService;
	 @Autowired
	 private EcmcAlarmObjectDao ecmcAlarmObjectDao;
	 @Autowired
	 private EcmcAlarmMessageDao ecmcAlarmMessageDao;
	 @Autowired
	 private JedisUtil jedisUtil;
	 
	

	 /**
     * 如删除对象，则需删除运维报警对象及其关联的报警信息、报警监控项等
     *
     * @param objectId
     */
    @Override
    public void cleanAlarmDataAfterDeletingObject(String objectId) {
        String hql = "from BaseEcmcAlarmObject where aoObjectId = ?";
        List<BaseEcmcAlarmObject> baseEcmcAlarmObjList = ecmcAlarmObjectDao.find(hql, objectId);
        for (BaseEcmcAlarmObject baseEcmcAlarmObj : baseEcmcAlarmObjList) {
            EcmcAlarmObject ecmcAlarmObject = new EcmcAlarmObject();
            BeanUtils.copyPropertiesByModel(ecmcAlarmObject, baseEcmcAlarmObj);
            //删除单个报警对象的操作
            deleteAlarmObject(ecmcAlarmObject);
        }
    }
    
    
    /***
     * 删除单个运维报警对象
     * 1.删除报警对象的redis信息
     * 2.删除报警对象的数据库记录
     * 3.删除对象的监控报警项
     * 4.删除对象的报警信息
     *
     * @param alarmObject
     * @return
     * @throws AppException
     */

    public boolean deleteAlarmObject(EcmcAlarmObject alarmObject) throws AppException {
        boolean isDeleted = false;
        try {
            isDeleted = jedisUtil.delete(RedisKey.ECMC_ALARM_OBJECT + alarmObject.getId());
            if (isDeleted) {
                ecmcAlarmObjectDao.delete(alarmObject.getId());
            }
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
        ecmcMonitorAlarmApiService.deleteMonAlarmItemByAlarmObject(alarmObject);
        deleteAlarmMessage(alarmObject);//mydoing?报警对象与其他关联的id到底是什么
        return isDeleted;
    }
    
    
    /**
     * 删除某报警对象产生的报警信息
     *
     * @param alarmObject
     */
    private void deleteAlarmMessage(EcmcAlarmObject alarmObject) {
        String sql = " from BaseEcmcAlarmMessage where objId = ? and alarmRuleId = ?";
        List<BaseEcmcAlarmMessage> msgList = ecmcAlarmMessageDao.find(sql, alarmObject.getAoObjectId(), alarmObject.getAlarmRuleId());
        for (BaseEcmcAlarmMessage baseAlarmMessage : msgList) {
            ecmcAlarmMessageDao.delete(baseAlarmMessage);
        }
    }

}
