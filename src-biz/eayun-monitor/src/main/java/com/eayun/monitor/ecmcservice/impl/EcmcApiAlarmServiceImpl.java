package com.eayun.monitor.ecmcservice.impl;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.ApiConstant;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.ApiUtil;
import com.eayun.monitor.dao.EcmcAlarmRuleDao;
import com.eayun.monitor.dao.EcmcAlarmTriggerDao;
import com.eayun.monitor.ecmcservice.EcmcApiAlarmService;
import com.eayun.monitor.job.CountTask;
import com.eayun.monitor.model.*;
import com.eayun.sys.model.SysDataTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

/**
 * Created by Administrator on 2017/1/4.
 */
@Service
@Transactional
public class EcmcApiAlarmServiceImpl implements EcmcApiAlarmService {


    @Autowired
    private JedisUtil jedisUtil ;
    @Autowired
    private MongoTemplate mongoTemplate ;
    @Autowired
    private EcmcAlarmTriggerDao ecmcAlarmTriggerDao ;
    @Autowired
    private EcmcAlarmRuleDao ecmcAlarmRuleDao ;
    private static Logger logger = LoggerFactory.getLogger(EcmcApiAlarmServiceImpl.class) ;
    private static final String PREV_MINUTE_NO_DATE_DESC = "上一分钟不存在对应监测指标的实时数据，程序执行结束！" ;


    /**
     * 整理当前时刻（分钟）内的历史数据
     * @param now   计划任务执行时间，每分钟整分时刻执行
     *              RedisKey.API_MONITORINGALARM_SERVICE_LOG_BASE_ALLNUMBER       + redisKeyTimestamp
     * @return
     */
    @Override
    public boolean createRealTimeData(Date now){
        //上一分钟时间戳
        String redisTimestampKey = ApiUtil.redisKeyTimestamp(new Date(now.getTime() - 60*1000)) ;
        logger.info("上一分钟时间戳 redisTimestampKey:" + redisTimestampKey) ;
        //上一分钟内的所有API访问维度信息
        Set<String> allWeidu = jedisUtil.hMapKeys(RedisKey.API_MONITORINGALARM_SERVICE_LOG_BASE_ALLNUMBER + redisTimestampKey);
        logger.info("上一分钟内的所有API访问维度信息:" + allWeidu);
        if (allWeidu.size() == 0) {
            logger.info(PREV_MINUTE_NO_DATE_DESC);
            //现在将报警提醒的标识加了过期时间，让其自动失效
            return false;
        }

        //定义一个集合，封装监测的指标数据
        List<ApiMonitorData> apiMonitorDataList = new ArrayList<>() ;

        for (String weidu : allWeidu) {
            Integer availableValueChange = null, //可用率变化趋势
                    rightValueChange = null, //正确率变化趋势
                    dateValueChange = null, //平均处理时间变化趋势
                    allValueChange = null; //总访问次数变化趋势
            logger.info("循环判断当前的维度为:" + weidu);
            //循环处理每一个维度对应指标值的数据

            String allNumberMessage = jedisUtil.getHashMapKeyValue(            RedisKey.API_MONITORINGALARM_SERVICE_LOG_BASE_ALLNUMBER + redisTimestampKey, weidu);
            String dateNumberMessage = jedisUtil.getHashMapKeyValue(          RedisKey.API_MONITORINGALARM_SERVICE_LOG_BASE_DATENUMBER + redisTimestampKey, weidu);
            String availableNumberMessage = jedisUtil.getHashMapKeyValue(RedisKey.API_MONITORINGALARM_SERVICE_LOG_BASE_AVAILABLENUMBER + redisTimestampKey, weidu);
            String rightNumberMessage = jedisUtil.getHashMapKeyValue(        RedisKey.API_MONITORINGALARM_SERVICE_LOG_BASE_RIGHTNUMBER + redisTimestampKey, weidu);

            //总访问次数
            Long allNumber = Long.parseLong(allNumberMessage);
            //总访问时间
            Long dateNumber = Long.parseLong(dateNumberMessage);
            //总可用访问次数
            Long availableNumber = availableNumberMessage == null ? 0L : Long.parseLong(availableNumberMessage);
            //总正确访问次数
            Long rightNumber = rightNumberMessage == null ? 0L : Long.parseLong(rightNumberMessage);

            logger.info("总访问次数:" + allNumber);
            logger.info("总访问时间:" + dateNumber);
            logger.info("总可用访问次数:" + availableNumber);
            logger.info("总正确访问次数:" + rightNumber);

            Double availableValue = parseValue(100.0 * availableNumber / allNumber, false);   //可用率
            Double dateValue = parseValue(1.0 * dateNumber / allNumber, true);   //平均处理时间
            Double rightValue = parseValue(100.0 * rightNumber / allNumber, false);   //正确率
            Long allNumberValue = allNumber;                                                //访问次数值，长整型

            logger.info("可用率:" + availableValue);
            logger.info("平均处理时间:" + dateValue);
            logger.info("正确率：" + rightValue);
            logger.info("总访问次数：" + allNumberValue);

            //api:monitoringalarm:prev 取出上一时刻存储的指标数据值，可能为空
            String indicitorData = jedisUtil.getHashMapKeyValue(RedisKey.API_MONITORINGALARM_PREV, weidu);
            //输出上一时刻的同一维度的指标数据值，判断具体变化范围趋势
            logger.info("上一时刻所存储的数据为:" + indicitorData);
            if (indicitorData == null) {
                //上一分钟内，该维度没有对应的访问数据，此时四个变化趋势都为0
                logger.info("不存在上一个时刻的指标值数据，将四个指标值的变化趋势都定义为0");
                availableValueChange = 0;
                rightValueChange = 0;
                dateValueChange = 0;
                allValueChange = 0;
            } else {
                //如果有对应的指标数据，则需要分别判断其大小的变化趋势
                logger.info("查询出上一个时刻的四个指标值，然后对指标值分别进行比较");
                //将上一时刻指标数据转换为对应实体，使用指定方法进行判断
                ApiMonitorDataIndicitor apiMonitorDataIndicitor = JSONObject.parseObject(indicitorData, ApiMonitorDataIndicitor.class);
                availableValueChange = apiMonitorDataIndicitor.availableValueChange(availableValue);
                rightValueChange = apiMonitorDataIndicitor.rightValueChange(rightValue);
                dateValueChange = apiMonitorDataIndicitor.dateValueChange(dateValue);
                allValueChange = apiMonitorDataIndicitor.allValueChange(allNumberValue);
            }

            //上述步骤完成之后得到，当前时刻当前维度的数据指标信息
            apiMonitorDataList.add(
                    new ApiMonitorData(weidu,
                            availableValue, rightValue, allNumberValue, dateValue,
                            now,
                            availableNumber, rightNumber, dateNumber,
                            availableValueChange, rightValueChange, allValueChange, dateValueChange)
            );
        }
        updateRedisPrevData(now, apiMonitorDataList);
        logger.info("当前所获取到的所有的实时数据集合为：" + apiMonitorDataList) ;
        return true ;
    }


    /**
     * 根据报警规则和触发条件进行操作
     * @param now
     * @return
     */
    @Override
    public Set<String> createRedisDataForCheckIsSatisfyWarningCondition(Date now) {
        //0010003003  获取API监控项的子节点
        List<SysDataTree> sysDataTrees = DictUtil.getDataTreeByParentId(ApiConstant.API_ZB_NODE_ID);
        //保存各个监控项的NodeId编号
        List<String> allZBIds = new ArrayList<>() ;
        for (SysDataTree sysDataTree : sysDataTrees) {
            allZBIds.add(sysDataTree.getNodeId());
        }

        //获取属于API指标的所有报警触发条件
        List<BaseEcmcAlarmTrigger> baseEcmcAlarmTriggers = null ;
        try {
            baseEcmcAlarmTriggers = ecmcAlarmTriggerDao.getAllListByTriggerType(allZBIds);
        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }
        //如果还未配置任何的触发条件，则程序退出
        if (baseEcmcAlarmTriggers == null || baseEcmcAlarmTriggers.size() == 0){
            logger.info("当前系统中未配置任何的触发条件，系统执行退出！");
            return null;
        }

        //取得所有触发条件中配置时间的最大值
        Integer maxTime = getMaxTriggerConfigTime(baseEcmcAlarmTriggers) ;
        //根据最大时间推算出最大时间范围的开始时间值
        Date start = new Date(now.getTime() - (maxTime/60 - 1) * 60 * 1000) ;
        //根据时间范围查询出在指定的时间范围内所包含的数据信息
        List<ApiMonitorData> datas = queryMonitorDataByTimeRange(start, now) ;
        if (datas == null || datas.size() == 0){
            //若是没有任何指标数据，则程序退出
            return null ;
        }else {

            //指定时间段范围内所有的维度信息
            Set<String> allDatasWeiduMessages = new HashSet<>();
            //首先清除上一时刻的缓存数据
            preClearApiMonitoringalarmData(
                    RedisKey.API_MONITORINGALARM_AVAILABILITY,
                    RedisKey.API_MONITORINGALARM_CORRECT,
                    RedisKey.API_MONITORINGALARM_DEALTIME,
                    RedisKey.API_MONITORINGALARM_REQUESTSNUMBER);


            for (ApiMonitorData apiMonitorData : datas){
                //遍历每一项查询得到的指标数据，分别处理更新缓存内容
                //维度
                String weidu = apiMonitorData.getWeiduMessage();
                allDatasWeiduMessages.add(weidu);
                //数据的得分
                long score = apiMonitorData.getTimestamp().getTime();
                jedisUtil.addToSortedSet(RedisKey.API_MONITORINGALARM_AVAILABILITY +   weidu, score, String.valueOf(apiMonitorData.getAvailability()));
                jedisUtil.addToSortedSet(RedisKey.API_MONITORINGALARM_CORRECT +        weidu, score, String.valueOf(apiMonitorData.getCorrect()));
                jedisUtil.addToSortedSet(RedisKey.API_MONITORINGALARM_DEALTIME +       weidu, score, String.valueOf(apiMonitorData.getAvgdealTime()));
                jedisUtil.addToSortedSet(RedisKey.API_MONITORINGALARM_REQUESTSNUMBER + weidu, score, String.valueOf(apiMonitorData.getRequestsNumber()));
            }
            logger.info("当前所解析得到的所有维度数据信息为 : " + allDatasWeiduMessages);
            return allDatasWeiduMessages;
        }
    }

    /**
     * 获取解析需要报警的报警信息
     * @param now
     * @param allWeidus
     * @return
     */
    @Override
    public Map<String,List<BaseEcmcAlarmMessage>> checkIsSatisfyWarning(Date now, Set<String> allWeidus) {

        //查询出对应API类型的所有报警规则
        List<BaseEcmcAlarmRule> baseEcmcAlarmRules = ecmcAlarmRuleDao.getAllListByRuleType(ApiConstant.API_ZB_NODE_ID) ;
        //定义一个Map集合存储报警规则与报警触发条件的对应关系
        Map<String, List<BaseEcmcAlarmTrigger>> ruleAndTriggerRelations = new HashMap<>() ;
        //向该Map集合中存入指定的信息
        for (BaseEcmcAlarmRule baseEcmcAlarmRule : baseEcmcAlarmRules) {
            ruleAndTriggerRelations.put(baseEcmcAlarmRule.getId(), ecmcAlarmTriggerDao.getAllListByRuleId(baseEcmcAlarmRule.getId()));
        }

        //利用FormJoin的方式处理分析过程
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        //将来获得的结果为Map集合：报警规则 --> 对应的报警信息
        Future<Map<String,List<BaseEcmcAlarmMessage>>> resultMessages = null ;
        Map<String,List<BaseEcmcAlarmMessage>> alarmMessages = null;
        try {
            resultMessages = forkJoinPool.submit(new CountTask(allWeidus, ruleAndTriggerRelations, now, baseEcmcAlarmRules));
            alarmMessages = resultMessages.get();//阻塞方法，等待获取结果
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return alarmMessages ;
    }

    @Override
    public void sendWarningMessagesByPhoneAndEmail(Map<String, List<BaseEcmcAlarmMessage>> alarmMessages) {
        Set<Map.Entry<String,List<BaseEcmcAlarmMessage>>> set = alarmMessages.entrySet() ;
        for (Map.Entry<String,List<BaseEcmcAlarmMessage>> entity : set){

        }
    }

    /**
     * 清楚Redis中存储的用于判断是否需要发送报警信息提醒的标识数据
     */
    private void clearIsNoticedFlag(){
        try {
            JedisUtil jedisUtil = JedisUtil.getInstance() ;
            //api:monitoringalarm:isNeedNotify:
            for (String key : jedisUtil.keys(RedisKey.API_MONITORINGALARM_ISNEEDNOTIFY + "*")) {
                jedisUtil.delete(key);
            }
        } catch (Exception e) {
            //未知异常
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 指标值四舍五入取值，保留小数点后一位
     * @param origin    对应指标值
     * @return
     */
    private Double parseValue(Double origin, boolean ... isTime){
        //可变参数长度为1，并且为true，保留一位小数
        if (isTime.length == 1 && isTime[0] == true) {
            return new BigDecimal(origin).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        }else {
            return new BigDecimal(origin).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
    }
    /**
     * 将Redis中上一时刻的实时数据指标值更新为当前时刻对应的数据指标值
     * @param operateDate           计划任务当前的操作时间
     * @param apiMonitorDataList    计划任务解析得到的当前时刻实时数据的集合
     */
    private void updateRedisPrevData(Date operateDate, List<ApiMonitorData> apiMonitorDataList){
        try {
            jedisUtil.delete(RedisKey.API_MONITORINGALARM_PREV);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        for (ApiMonitorData apiMonitorData : apiMonitorDataList) {
            //保存每一项指标数据
            mongoTemplate.save(apiMonitorData);
            //分别保存上一时刻的数据指标值，便于下一时刻比较指标值的变化趋势
            jedisUtil.hSetHashMapValue(RedisKey.API_MONITORINGALARM_PREV, apiMonitorData.getWeiduMessage(), JSONObject.toJSONString(apiMonitorData.getApiIndicitorData()));
        }
        //设置超时时间让其对应的缓存标识数据自动过期
        jedisUtil.expireKey(RedisKey.API_MONITORINGALARM_PREV, 90L) ;
    }

    /**
     * 取得所有配置的触发条件对象当中最大的配置时间
     * @param baseEcmcAlarmTriggers
     * @return
     */
    private int getMaxTriggerConfigTime(List<BaseEcmcAlarmTrigger> baseEcmcAlarmTriggers){
        //首先取得第一个触发条件的时间值
        int maxTime = baseEcmcAlarmTriggers.get(0).getLastTime() ;
        for (BaseEcmcAlarmTrigger trigger : baseEcmcAlarmTriggers){
            if (trigger.getLastTime() > maxTime){
                maxTime = trigger.getLastTime();
            }
        }
        return maxTime ;
    }

    /**
     * 根据时间范围查询出符合条件的全部实时数据
     * @param start 开始时间
     * @param end   结束时间
     * @return
     */
    private List<ApiMonitorData> queryMonitorDataByTimeRange(Date start, Date end){
        Query query = new Query(new Criteria().andOperator(
                Criteria.where("timestamp").gte(start),
                Criteria.where("timestamp").lte(end)
        )) ;
        return mongoTemplate.find(query, ApiMonitorData.class) ;
    }

    /**
     * 清楚指定的一系列Key的全部数据内容
     * @param keyAgs    指定的一系列Key
     */
    private void preClearApiMonitoringalarmData(String ... keyAgs){
        //清空Redis指定缓存数据
        Set<String> allKeys = null ;
        for (String key : keyAgs){
            try {
                allKeys = jedisUtil.keys(key + "*") ;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            for (String specialKey : allKeys){
                try {
                    jedisUtil.delete(specialKey);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }
}