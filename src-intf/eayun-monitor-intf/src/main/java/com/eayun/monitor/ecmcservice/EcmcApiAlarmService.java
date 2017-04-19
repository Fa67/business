package com.eayun.monitor.ecmcservice;

import com.eayun.monitor.model.BaseEcmcAlarmMessage;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2017/1/4.
 */
public interface EcmcApiAlarmService {

    /**
     * 生成实时数据
     * @param now   计划任务执行时间，每分钟整分时刻执行
     */
    boolean createRealTimeData(Date now) ;

    /**
     * 生成用于判断是否触发报警信息条件的依赖数据
     * @param now
     * @return
     */
    Set<String> createRedisDataForCheckIsSatisfyWarningCondition(Date now) ;

    /**
     * 判断是否满足报警条件以及解析报警信息
     */
    Map<String,List<BaseEcmcAlarmMessage>> checkIsSatisfyWarning(Date now, Set<String> allWeidus) ;


    void sendWarningMessagesByPhoneAndEmail(Map<String,List<BaseEcmcAlarmMessage>> alarmMessages) ;

}
