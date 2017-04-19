package com.eayun.monitor.job;

import com.eayun.common.RedisNodeIdConstant;
import com.eayun.common.constant.ApiConstant;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.tools.DictUtil;
import com.eayun.monitor.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.RecursiveTask;

public class CountTask extends RecursiveTask<Map<String, List<BaseEcmcAlarmMessage>>>{

    //计划任务执行的当前时间
    private Date lastDate ;
    //解析得到的所有维度信息
    private Set<String> weidus ;
    //存储报警规则与触发条件信息的对应关系
    private Map<String, List<BaseEcmcAlarmTrigger>> relations ;
    //存储所有的报警规则信息
    private List<BaseEcmcAlarmRule> baseEcmcAlarmRules ;
    private JedisUtil jedisUtil = JedisUtil.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(CountTask.class) ;


    public CountTask(Set<String> weidus, Map<String, List<BaseEcmcAlarmTrigger>> relations, Date lastDate, List<BaseEcmcAlarmRule> baseEcmcAlarmRules){
        this.weidus = weidus ;
        this.relations = relations ;
        this.lastDate = lastDate ;
        this.baseEcmcAlarmRules = baseEcmcAlarmRules ;
    }

    @Override
    protected Map<String, List<BaseEcmcAlarmMessage>> compute(){
        //按照报警规则的粒度对线程进行划分
        if (baseEcmcAlarmRules.size() == 1){
            return analysisWarning(baseEcmcAlarmRules.get(0).getId());
        }
        if (baseEcmcAlarmRules.size() > 1){
            //定义一个Task集合一遍集合调用
            List<CountTask> allTask = new ArrayList<>();
            for (BaseEcmcAlarmRule baseEcmcAlarmRule : baseEcmcAlarmRules){
                    allTask.add(new CountTask(weidus, relations, lastDate, Arrays.asList(baseEcmcAlarmRule)));
            }
            //将所有的线程进行分发执行
            for (CountTask countTask : allTask){
                countTask.fork();
            }
            Map<String, List<BaseEcmcAlarmMessage>> rs = new HashMap<>();
            for (CountTask countTask : allTask){
                //每一个线程单独返回的结果
                Map<String, List<BaseEcmcAlarmMessage>> threadResult = countTask.join() ;
                if (threadResult != null && threadResult.size() != 0) {
                    //确定有返回值的话，保存数据
                    rs.putAll(threadResult);//保留报警信息
                }
            }
            return rs ;
        }
        return null;
    }

    /**
     * 按照报警规则的粒度开辟新的线程，进行任务的计算
     * @param ruleId    报警规则Id
     * @return
     */
    private Map<String, List<BaseEcmcAlarmMessage>> analysisWarning(String ruleId){
        JedisUtil jedisUtil = JedisUtil.getInstance() ;
        //获取该报警规则下的所有触发条件信息
        List<BaseEcmcAlarmTrigger> triggers = relations.get(ruleId);
        //定义一个集合保存所有的报警信息
        List<BaseEcmcAlarmMessage> result = new ArrayList<>();


        //遍历维度信息，分别处理对应数据
        for (String weidu : weidus) {

            //当前时间范围内，维度对应的指标个数值
            long currentWeiduElementCount = 0;
            try {
                currentWeiduElementCount = jedisUtil.getSizeOfZSet(RedisKey.API_MONITORINGALARM_REQUESTSNUMBER + weidu);//总请求次数
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            //首先筛选出一部分可能满足条件的维度数据，使用一个集合保存
            List<BaseEcmcAlarmTrigger> maybeTrigger = new ArrayList<>();
            for (BaseEcmcAlarmTrigger trigger : triggers) {
                if (trigger.getLastTime() / 60 <= currentWeiduElementCount) {
                    //必须满足此条件
                    maybeTrigger.add(trigger);
                }
            }

            //通过上述判断，如果得到的集合不为空集合的话
            if (maybeTrigger.size() != 0) {
                for (BaseEcmcAlarmTrigger trigger : maybeTrigger) {

                    //此时便需要遍历触发器来进一步判断是否满足触发条件
                    long endScore   = lastDate.getTime();//最终得分即为当前时间
                    long startScore = lastDate.getTime() - (trigger.getLastTime() / 60 - 1) * 60 * 1000;//开始得分业务根据规则计算的时间

                    //定义一个集合保存查询出的所有指标值
                    List<String> allCheckValues = null;

                    //正确率触发条件
                    if (ApiConstant.API_MONITORINGALARM_CORRECT_ZB.equals(trigger.getZb())) {
                        allCheckValues = jedisUtil.getZSetByScoresDESC(RedisKey.API_MONITORINGALARM_CORRECT        + weidu, startScore, endScore);
                    }
                    //可用率触发条件
                    if (ApiConstant.API_MONITORINGALARM_AVAILABILITY_ZB.equals(trigger.getZb())) {
                        allCheckValues = jedisUtil.getZSetByScoresDESC(RedisKey.API_MONITORINGALARM_AVAILABILITY   + weidu, startScore, endScore);
                    }
                    //请求次数触发条件
                    if (ApiConstant.API_MONITORINGALARM_REQUESTSNUMBER_ZB.equals(trigger.getZb())) {
                        allCheckValues = jedisUtil.getZSetByScoresDESC(RedisKey.API_MONITORINGALARM_REQUESTSNUMBER + weidu, startScore, endScore);
                    }
                    //平均处理时间触发条件
                    if (ApiConstant.API_MONITORINGALARM_DEALTIME_ZB.equals(trigger.getZb())) {
                        allCheckValues = jedisUtil.getZSetByScoresDESC(RedisKey.API_MONITORINGALARM_DEALTIME       + weidu, startScore, endScore);
                    }
                    if (allCheckValues == null || allCheckValues.size() != trigger.getLastTime() / 60){
                        //如果集合为空，或者集合值个数不恰好等于触发条件对应的分钟数，则循环跳出继续执行
                        continue;
                    }
                    //定义一个布尔标识
                    boolean isCheck = true;
                    switch (trigger.getOperator()){
                        //获取触发条件的描述符，进行指定操作

                        case ">":
                            for (String ck : allCheckValues) {
                                if (Double.parseDouble(ck) <= trigger.getThreshold()) {
                                    isCheck = false;
                                    break;
                                }
                            }
                            break;
                        case "<":
                            for (String ck : allCheckValues) {
                                if (Double.parseDouble(ck) >= trigger.getThreshold()) {
                                    isCheck = false;
                                    break;
                                }
                            }
                            break;
                        case "=":
                            for (String ck : allCheckValues) {
                                if (Double.parseDouble(ck) != trigger.getThreshold()) {
                                    isCheck = false;
                                    break;
                                }
                            }
                            break;
                    }
                    //isCheck 是否应该报警信息的布尔标识
                    if (isCheck) {
                        BaseEcmcAlarmMessage message = new BaseEcmcAlarmMessage();
                        message.setMonitorType(RedisNodeIdConstant.ECMC_MONITOR_TYPE_API);
                        message.setAlarmRuleId(ruleId);//需要更新删除报警规则的时候要删除报警信息
                        message.setCusId(weidu.split(":")[0]);
                        message.setIp(   weidu.split(":")[1]);
                        message.setDcId( weidu.split(":")[2]);
                        message.setTime(lastDate);
                        message.setIsProcessed("0");
                        message.setAm_alarmtriggerid(trigger.getId());
                        message.setDetail(parseWarningConditionMessage(trigger));
                        result.add(message);
                    }
                }
            }
        }
        //筛选出需要报警的信息
        if (result.size() == 0){
            //程序退出
            return null;
        }else {
            //如果不为空，则遍历进行分析
            List<BaseEcmcAlarmMessage> needNoticeMessages = new ArrayList<>() ;

            //api:monitoringalarm:isNeedNotify:
            for (BaseEcmcAlarmMessage baseEcmcAlarmMessage : result) {
                String redisKeyStringPrev = (RedisKey.API_MONITORINGALARM_ISNEEDNOTIFY +
                        baseEcmcAlarmMessage.getCusId() + ":" +
                        baseEcmcAlarmMessage.getIp()    + ":" +
                        baseEcmcAlarmMessage.getDcId()  + ":" +
                        baseEcmcAlarmMessage.getAm_alarmtriggerid()) ;
                try {
                    if (jedisUtil.get(redisKeyStringPrev) == null) {
                        //上一时刻没有发送提醒，即Redis中没有对应的缓存数据
                        jedisUtil.set(redisKeyStringPrev, "Warning");
                        logger.info("Class Is Notify : " + baseEcmcAlarmMessage);
                        needNoticeMessages.add(baseEcmcAlarmMessage);
                    }else {
                        //上一时刻已经发送过提醒了，此时只需要更新缓存的保留时间
                    }
                    //更新缓存的保留时长
                    jedisUtil.expireKey(redisKeyStringPrev, 90L) ;//保留到当前时刻的下一分钟自动过期
                }catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (needNoticeMessages.size() == 0){
                return null ;
            }else {
                Map<String,List<BaseEcmcAlarmMessage>> innerRs = new HashMap<>();
                innerRs.put(ruleId, needNoticeMessages);
                return innerRs ;
            }
        }
    }
    /**
     * 将触发条件对象转换为方便展示的文字描述信息
     * @param baseEcmcAlarmTrigger
     * @return
     */
    private String parseWarningConditionMessage(BaseEcmcAlarmTrigger baseEcmcAlarmTrigger){
        StringBuilder builder = new StringBuilder() ;
        builder.append(DictUtil.getDataTreeByNodeId(baseEcmcAlarmTrigger.getZb()).getNodeName()).append(" ");
        builder.append(baseEcmcAlarmTrigger.getOperator()).append(" ");
        String thresholdString = null ;
        if ("0010003003003".equals(baseEcmcAlarmTrigger.getZb())){
        //若为请求次数，应该显示整数
            thresholdString = String.valueOf((int)baseEcmcAlarmTrigger.getThreshold()) ;
        }else if ("0010003003004".equals(baseEcmcAlarmTrigger.getZb())){
            thresholdString = baseEcmcAlarmTrigger.getThreshold() + "0" ;
        }else {
            thresholdString = String.valueOf(baseEcmcAlarmTrigger.getThreshold()) ;
        }
        builder.append(thresholdString) ;
        builder.append("(");
        builder.append(baseEcmcAlarmTrigger.getUnit());
        builder.append(")").append(" ");
        builder.append("已持续");
        builder.append(baseEcmcAlarmTrigger.getLastTime()/60);
        builder.append("分钟") ;
        return builder.toString() ;
    }
}