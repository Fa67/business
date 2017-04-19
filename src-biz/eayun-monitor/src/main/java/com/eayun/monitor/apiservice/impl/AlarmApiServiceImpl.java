package com.eayun.monitor.apiservice.impl;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.constant.RedisKey;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.monitor.apiservice.AlarmApiService;
import com.eayun.monitor.apiservice.MonitorAlarmApiService;
import com.eayun.monitor.baseservice.BaseAlarmService;
import com.eayun.monitor.dao.AlarmMessageDao;
import com.eayun.monitor.dao.AlarmObjectDao;
import com.eayun.monitor.model.AlarmObject;
import com.eayun.monitor.model.BaseAlarmMessage;
import com.eayun.monitor.model.BaseAlarmObject;

/**
 * 
 * 云主机监控API业务<br>
 * -----------------
 * @author chengxiaodong
 * @date 2016-12-2
 *
 */

@Service
@Transactional
public class AlarmApiServiceImpl extends BaseAlarmService implements AlarmApiService {

	@Autowired
	private MonitorAlarmApiService  monitorAlarmApiService;
	@Autowired
    private AlarmObjectDao alarmObjectDao;
	@Autowired
	private AlarmMessageDao alarmMessageDao;
	@Autowired
	private JedisUtil jedisUtil;
	
	
	
	@Override
	public boolean cleanAlarmDataAfterDeletingVM(String vmId) {
		 /*
         * 如果主机被删除，则报警对象（即某个主机）和报警对象产生的报警信息都要删除
         * 删除报警对象中删了对应报警对象的缓存和数据表中的数据，还删除了监控报警项
         */
        String findObjSQL = " from BaseAlarmObject where vmId=?";
        List<BaseAlarmObject> objList = alarmObjectDao.find(findObjSQL, vmId);
        for (BaseAlarmObject baseAlarmObject : objList) {
            AlarmObject alarmObject = new AlarmObject();
            BeanUtils.copyProperties(baseAlarmObject, alarmObject);
            deleteAlarmObject(alarmObject);
        }
        
        return true;
	}
	
	
	/**
	 * 删除报警对象
	 * @param alarmObject
	 * @return
	 * @throws AppException
	 */
	public boolean deleteAlarmObject(AlarmObject alarmObject){
        
        boolean isDeleted = false;
        try {
            isDeleted = jedisUtil.delete(RedisKey.ALARM_OBJECT+alarmObject.getId());
            if(isDeleted){
                alarmObjectDao.delete(alarmObject.getId());
            }
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
        //删除报警对象需要删除指定alarmruleId和objectId的监控报警项
        monitorAlarmApiService.deleteMonitorAlarmItemByAlarmObject(alarmObject);
        deleteAlarmMessage(alarmObject);
        return isDeleted;
    }
	
	
	/**
	 * 删除报警对象产生的报警信息
	 * @param alarmObject
	 */
	@SuppressWarnings("unchecked")
    private void deleteAlarmMessage(AlarmObject alarmObject) {
        String sql = " from BaseAlarmMessage where vmId=? and alarmRuleId=?";
        List<BaseAlarmMessage> msgList = alarmMessageDao.find(sql, alarmObject.getVmId(), alarmObject.getAlarmRuleId());
        for (BaseAlarmMessage baseAlarmMessage : msgList) {
            alarmMessageDao.delete(baseAlarmMessage);
        }
    }
     
}
