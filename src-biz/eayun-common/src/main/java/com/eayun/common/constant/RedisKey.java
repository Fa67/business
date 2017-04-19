package com.eayun.common.constant;

/**
 * RedisKey常量类
 *                       
 * @Filename: RedisKey.java
 * @Description: 
 * @Version: 1.0
 * @Author: chenhao
 * @Email: hao.chen@eayun.com
 * @History:<br>
 *<li>Date: 2016年8月31日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class RedisKey {

    /**
     * PRICE
     * 
     *Comment for <code>PRICE</code>
     */
    public static final String PRICE                         = "price:";

    /**
     * SERIALNUM
     * 
     *Comment for <code>SERIALNUM</code>
     */
    public static final String SERIALNUM                     = "serialnum:";

    /**
     * IP
     * 
     *Comment for <code>IP</code>
     */
    public static final String IP                            = "ip:";

    /**
     * CUSSERVICESTATE_CUSID
     * 
     *Comment for <code>CUSSERVICESTATE_CUSID</code>
     */
    public static final String CUSSERVICESTATE_CUSID         = "cusservicestate:cusid:";

    /**
     * ecsc sessionid
     * 
     *Comment for <code>ECSC_SESSIONID</code>
     */
    public static final String ECSC_SESSIONID                = "ECSCSessionID:";

    /**
     * SMS
     * 
     *Comment for <code>QUOTA_MSGQUEUE</code>
     */
    public static final String QUOTA_MSGQUEUE                = "EAYUN_QUOTA_MSGQUEUE";

    /**
     * =========================================短信配额相关开始========================================
     */

    /**
     * SMS
     * 
     *Comment for <code>SMS_QUATA</code>
     */
    public static final String SMS_QUOTA                     = "smsQuota:";

    /**
     * SMS
     * 
     *Comment for <code>SMS_QUATA_TOTAL</code>
     */
    public static final String SMS_QUOTA_TOTAL               = "smsQuota:total:";

    /**
     * SMS
     * 
     *Comment for <code>SMS_QUATA_SENT</code>
     */
    public static final String SMS_QUOTA_SENT                = "smsQuota:sent:";

    /**
     * =========================================短信配额相关结束========================================
     */

    /**
     * =========================================标签相关开始========================================
     */
    /**
     * 标签
     * 
     *Comment for <code>CUS_TGRP</code>
     */
    public static final String CUS_TGRP                      = "cus:tgrp:";

    /**
     * 标签
     * 
     *Comment for <code>TAG_GROUP</code>
     */
    public static final String TAG_GROUP                     = "taggroup:";

    /**
     * 标签
     * 
     *Comment for <code>TGRP_TG</code>
     */
    public static final String TGRP_TG                       = "tgrp:tg:";

    /**
     * 标签
     * 
     *Comment for <code>TAG</code>
     */
    public static final String TAG                           = "tag:";

    /**
     * 标签
     * 
     *Comment for <code>TAG_RES</code>
     */
    public static final String TAG_RES                       = "tg:res:";

    /**
     * 标签
     * 
     *Comment for <code>RES_TAG</code>
     */
    public static final String RES_TAG                       = "res:tg:";
    /**
     * =========================================标签相关结束========================================
     */

    /**
     * bucket
     * 
     *Comment for <code>BUCKET_START</code>
     */
    public static final String BUCKET_START                  = "bucket_start:";

    /**
     * bucket
     * 
     *Comment for <code>BUCKET_END</code>
     */
    public static final String BUCKET_END                    = "bucket_end:";

    /**
     * obs
     * 
     *Comment for <code>OBSUSER_CUSID</code>
     */
    public static final String OBSUSER_CUSID                 = "obsuser:cusid:";

    /**
     * =========================================枚举字典相关开始========================================
     */
    /**
     * 枚举字典
     * 
     *Comment for <code>SYS_DATA_TREE</code>
     */
    public static final String SYS_DATA_TREE                 = "sys_data_tree:";

    /**
     * 枚举字典
     * 
     *Comment for <code>SYS_DATA_TREE_STATUS</code>
     */
    public static final String SYS_DATA_TREE_STATUS          = "sys_data_tree:statues:";

    /**
     * 枚举字典
     * 
     *Comment for <code>SYS_DATA_TREE_PARENT_NODEID</code>
     */
    public static final String SYS_DATA_TREE_PARENT_NODEID   = "sys_data_tree:parent:node_id:";
    /**
     * =========================================枚举字典相关结束========================================
     */

    /**
     * ak/sk
     * 
     *Comment for <code>AK_CUSID</code>
     */
    public static final String AK_CUSID                      = "ak:cusid:";

    /**
     * ak/sk
     * 
     *Comment for <code>AK_AKID</code>
     */
    public static final String AK_AKID                       = "ak:akid:";

    /**
     * ak/sk
     * 
     *Comment for <code>AK</code>
     */
    public static final String AK                            = "ak:";
    
    /**
     * ak/sk
     * 
     *Comment for <code>AK</code>
     */
    public static final String AK_ACCESSKEY                            = "ak:accesskey:";
    
    /**
     * 计费周期开始到开始计费时间（月初1号凌晨00:00到chargeFrom）的下载流量缓存Key
     */
    public static final String DOWNLOAD_CACHE_KEY_PREFIX     = "charge:obs:download:";

    /**
     * 计费周期开始到开始计费时间月初1号凌晨00:00到chargeFrom）的请求数流量缓存Key
     */
    public static final String OPS_CACHE_KEY_PREFIX          = "charge:obs:ops:";

    /**
     * =========================================OBS相关开始========================================
     */
    /**
     * obs
     * 
     *Comment for <code>OBS_SORT_BY_STORAGE_USED</code>
     */
    public static final String OBS_SORT_BY_STORAGE_USED      = "obs:obsSortByStorageUsed";

    /**
     * obs
     * 
     *Comment for <code>OBS_SORT_BY_DOWNLOAD</code>
     */
    public static final String OBS_SORT_BY_DOWNLOAD          = "obs:obsSortByDownload";

    /**
     * obs
     * 
     *Comment for <code>OBS_SORT_BY_COUNT_REQUEST</code>
     */
    public static final String OBS_SORT_BY_COUNT_REQUEST     = "obs:obsSortByCountRequest";
    /**
     * =========================================OBS相关结束========================================
     */

    /**
     * =========================================云资源同步相关开始========================================
     */
    /**
     * 云主机 消息队列的KEY
     */
    public static final String vmKey                         = "CLOUD_RESOURCE:SYNCVM";

    /**
     * 云硬盘 消息队列的KEY
     */
    public static final String volKey                        = "CLOUD_RESOURCE:SYNCVOL";

    /**
     * 云硬盘备份 消息队列的KEY
     */
    public static final String volSphKey                     = "CLOUD_RESOURCE:SYNCVOLSPH";

    /**
     * 自定义镜像 消息队列的KEY
     */
    public static final String imageKey                      = "CLOUD_RESOURCE:SYNCIMAGE";

    /**
     * 负载均衡资源池 消息队列的KEY
     */
    public static final String fwKey                         = "CLOUD_RESOURCE:SYNCFIREWALL";

    /**
     * 自定义镜像 消息队列的KEY
     */
    public static final String ldPoolKey                     = "CLOUD_RESOURCE:SYNCLDPOOL";

    /**
     * 负载均衡成员 消息队列的KEY
     */
    public static final String ldMemberKey                   = "CLOUD_RESOURCE:SYNCLDMEMBER";
    /**
     * 负载均衡成员 消息队列的KEY(负载均衡主备模式新增)
     */
    public static final String ldMemberKeyRefresh                   = "memberstatus:";

    /**
     * 负载均衡VIP 消息队列的KEY
     */
    public static final String ldVipKey                      = "CLOUD_RESOURCE:SYNCLDVIP";

    /**
     * 创建云主机绑定云硬盘 消息队列的KEY
     */
    public static final String volAttVmKey                   = "CLOUD_RESOURCE:SYNCVMATTVOL";
    /**
     * vpn  消息队列key
     */
    public static final String vpnKey                        = "CLOUD_RESOURCE:SYNCVPN";

    /**
     * 云数据库备份同步队列Key
     */
    public static final String rdsBackupSyncKey                        = "CLOUD_RESOURCE:SYNCBACKUP";
    /**
     * 云数据库实例 消息队列的key
     */
    public static final String rdsKey                        = "CLOUD_RESOURCE:SYNCRDS";
    /**
     * 确保vip状态为ACITVE后,才置负载均衡状态为ACITVE
     */
    public static final String CLOUDLDVIPSYNC			="cloudLdVipSync:";
    /**
     * 创建负载均衡时,负载均衡状态已经为ACTIVE,而vip状态为pending_create,则在vip状态同步计划任务中修改订单状态
     */
    public static final String CLOUDLDPOOLSYNC				="cloudLdPoolSync:";
    /**
     * =========================================云资源同步相关结束========================================
     */

    /**
     * =========================================监控指标相关开始========================================
     */
    /**
     * monitor
     * 
     *Comment for <code>MONITOR_CPU</code>
     */
    public static final String MONITOR_CPU                   = "monitor:cpu_util:";

    /**
     * monitor
     * 
     *Comment for <code>MONITOR_CPU_LAST</code>
     */
    public static final String MONITOR_CPU_LAST              = "monitor:cpu_util:last";

    /**
     * monitor
     * 
     *Comment for <code>MONITOR_MEMORY</code>
     */
    public static final String MONITOR_MEMORY                = "monitor:memory.usage:";
    /**
     * monitor
     * 
     *Comment for <code>MONITOR_MEMORY_LAST</code>
     */
    public static final String MONITOR_MEMORY_LAST           = "monitor:memory.usage:last";

    /**
     * monitor
     * 
     *Comment for <code>MONITOR_DISK_READ</code>
     */
    public static final String MONITOR_DISK_READ             = "monitor:disk.read.bytes.rate:";

    /**
     * monitor
     * 
     *Comment for <code>MONITOR_DISK_READ_LAST</code>
     */
    public static final String MONITOR_DISK_READ_LAST        = "monitor:disk.read.bytes.rate:last";

    /**
     * monitor
     * 
     *Comment for <code>MONITOR_DISK_WRITE</code>
     */
    public static final String MONITOR_DISK_WRITE            = "monitor:disk.write.bytes.rate:";

    /**
     * monitor
     * 
     *Comment for <code>MONITOR_DISK_WRITE_LAST</code>
     */
    public static final String MONITOR_DISK_WRITE_LAST       = "monitor:disk.write.bytes.rate:last";

    /**
     * monitor
     * 
     *Comment for <code>MONITOR_NETWORK_INCOMING</code>
     */
    public static final String MONITOR_NETWORK_INCOMING      = "monitor:network.incoming.bytes.rate:";

    /**
     * monitor
     * 
     *Comment for <code>MONITOR_NETWORK_INCOMING_LAST</code>
     */
    public static final String MONITOR_NETWORK_INCOMING_LAST = "monitor:network.incoming.bytes.rate:last";

    /**
     * monitor
     * 
     *Comment for <code>MONITOR_NETWORK_OUTGOING</code>
     */
    public static final String MONITOR_NETWORK_OUTGOING      = "monitor:network.outgoing.bytes.rate:";

    /**
     * monitor
     * 
     *Comment for <code>MONITOR_NETWORK_OUTGOING_LAST</code>
     */
    public static final String MONITOR_NETWORK_OUTGOING_LAST = "monitor:network.outgoing.bytes.rate:last";
    
    /**
     * monitor
     * 云数据库磁盘使用率最新指标
     *Comment for <code>MONITOR_VOLUME_USED</code>
     */
    public static final String MONITOR_VOLUME_USED = "monitor:volume_used:";
    
    /**
     * monitor
     * 云数据库磁盘使用率次新指标
     *Comment for <code>MONITOR_VOLUME_USED_LAST</code>
     */
    public static final String MONITOR_VOLUME_USED_LAST = "monitor:volume_used:last:";
    
    /**
     * monitor
     * 负载均衡监控最新指标
     *Comment for <code>MONITOR_EXP_LDPOOL</code>
     */
    public static final String MONITOR_EXP_LDPOOL = "monitor:exp:ldpool:";
    
    /**
     * monitor
     * 负载均衡监控次新指标
     *Comment for <code>MONITOR_EXP_LDPOOL_LAST</code>
     */
    public static final String MONITOR_EXP_LDPOOL_LAST = "monitor:exp:ldpool:last:";
    
    /**
     * =========================================监控指标相关结束========================================
     */

    /**
     * =========================================监控报警相关开始========================================
     */
    /**
     * alarm
     * 
     *Comment for <code>ALARM_OBJECT</code>
     */
    public static final String ALARM_OBJECT                  = "alarmObject:";

    /**
     * alarm
     * 
     *Comment for <code>ECMC_ALARM_OBJECT</code>
     */
    public static final String ECMC_ALARM_OBJECT             = "ecmcAlarmObject:";

    /**
     * alarm
     * 
     *Comment for <code>ALARM_TRIGGER</code>
     */
    public static final String ALARM_TRIGGER                 = "alarmTrigger:";

    /**
     * alarm
     * 
     *Comment for <code>ECMC_ALARM_TRIGGER</code>
     */
    public static final String ECMC_ALARM_TRIGGER            = "ecmcAlarmTrigger:";
    /**
     * =========================================监控报警相关结束========================================
     */

    /**
     * =========================================消息相关开始========================================
     */
    /**
     * message
     * 
     *Comment for <code>MESSAGE_COLLECT_COUNT</code>
     */
    public static final String MESSAGE_COLLECT_COUNT         = "message:collect:count:MSG_";

    /**
     * message
     * 
     *Comment for <code>MESSAGE_STATUS_COUNT</code>
     */
    public static final String MESSAGE_STATUS_COUNT          = "message:statu:count:MSG_";

    /**
     * message
     * 
     *Comment for <code>MESSAGE_UNCOLLECT_COUNT</code>
     */
    public static final String MESSAGE_UNCOLLECT_COUNT       = "message:uncollect:count:MSG_";

    /**
     * message
     * 
     *Comment for <code>MESSAGE_STATUS_QUEUE</code>
     */
    public static final String MESSAGE_STATUS_QUEUE          = "message:statu:MSG_STATU_QUEUE";

    /**
     * message
     * 
     *Comment for <code>MESSAGE_UNCOLLECT_QUEUE</code>
     */
    public static final String MESSAGE_UNCOLLECT_QUEUE       = "message:uncollect:MSG_UNCOLLECT_QUEUE";

    /**
     * message
     * 
     *Comment for <code>MESSAGE_COLLECT_QUEUE</code>
     */
    public static final String MESSAGE_COLLECT_QUEUE         = "message:collect:MSG_COLLECT_QUEUE";
    /**
     * =========================================消息相关结束========================================
     */

    /**
     * =========================================忘记密码相关开始========================================
     */
    /**
     * forgotcode
     * 
     *Comment for <code>FORGOTCODE_CODECHECK</code>
     */
    public static final String FORGOTCODE_CODECHECK          = "forgotcode:codeCheck:";

    /**
     * forgotcode
     * 
     *Comment for <code>FORGOTCODE_PHONECHECK</code>
     */
    public static final String FORGOTCODE_PHONECHECK         = "forgotcode:phoneCheck:";
    /**
     * =========================================忘记密码相关结束========================================
     */
    
    /**
     * =========================================客户冻结查询key相关开始========================================
     */
    /**
     * forgotcode
     * 
     *Comment for <code>FORGOTCODE_CODECHECK</code>
     */
    public static final String CUS_BLOCK  = "cus:block:";

    /**
     * =========================================客户冻结查询key相关结束========================================
     */
    	 
    /**
     * =========================================保证后付费资源超过保留时长只发一次消息提醒开始========================================
     */
    /**
     * 保证后付费资源超过保留时长只发一次消息提醒
     * 
     *Comment for <code>FORGOTCODE_CODECHECK</code>
     */
    public static final String OUT_RENTENTION_TIME  = "outRententionTime:";

    /**
     * =========================================保证后付费资源超过保留时长只发一次消息提醒结束========================================
     */
    
    /**
     * =========================================保证后付费资源首次达到信用额度时只发一次消息提醒开始========================================
     */
    /**
     * 保证后付费资源首次达到信用额度时只发一次消息提醒
     * 
     *Comment for <code>FORGOTCODE_CODECHECK</code>
     */
    public static final String REACH_CREDIST_LIMIT  = "reachCreditLimit:";

    /**
     * =========================================保证后付费资源首次达到信用额度时只发一次消息提醒开始========================================
     */
    
    /**
     * =========================================scheduleLostTask(计划任务漏跑信息相关KEY)开始========================================
     */
    //标识指定的计划任务是否需要通知相关人员
    public static final String  REDIS_KEY_STORE_SCHEDULE_LOST_INFORMATION = "schedule_task_lost:store_schedule_lost_information" ;
    //判断Spring配置的循环检测任务是否已经被一台服务器占用执行的标识
    public static final String SCHEDULE_LOST_IS_CHECKED = "schedule_task_lost:checked" ;
    /**
     * =========================================scheduleLostTask(计划任务漏跑信息相关KEY)开始========================================
     */
    
    /**
     * =========================================同步数据中心已删除资源-开始========================================
     */
    
    /**
     * 同步数据中心已删除资源队列
     */
    public static final String DATACENTER_SYNC_DELETED_RESOURCE = "datacenter_sync_deleted_resources";
    /**
     * =========================================同步数据中心已删除资源-结束========================================
     */

    /*
     *  -------------------------------------- OBS计费相关RedisKey BEGIN-------------------------------------------------
     */
    /**
     * 对象存储存储容量统计成功标识Key
     */
    public static final String CHARGE_OBS_STORAGE = "charge:obs:storage:";
    /**
     * 对象存储使用量（下载流量、请求数）统计成功标识Key
     */
    public static final String CHARGE_OBS_USED = "charge:obs:used:";
    /**
     * 对象存储开通CDN后回源流量统计成功标识Key
     */
    public static final String CHARGE_OBS_CDN_BACKSOURCE = "charge:obs:backsource:";

    /**
     * 云资源计费结束的标识，用于OBS计费开始的前提条件之一
     */
    public static final String CHARGE_OBS_CLOUD_RES_CHARGE_DONE = "charge:obs:chargedone:";
    /**
     * CDN用量统计（下载流量、动态请求数和HTTPS请求数）结束的标识，用于OBS计费开始的前提条件之一
     */
    public static final String CHARGE_OBS_CDN_DETAIL_GATHER_DONE = "charge:obs:gatherdone:";
    /*
     *  -------------------------------------- OBS计费相关RedisKey END-------------------------------------------------
     */
    /**
     * =========================================网络模块计费相关RedisKey-开始========================================
     */
    public static final String CHARGE_NETWORK_RESTRICTED = "charge:network:restricted:";
    /**
     * =========================================网络模块计费相关RedisKey-结束========================================
     */
    /**
     * =========================================RDS计费相关RedisKey-BEGIN=======================================
     */
    public static final  String CHARGE_RDS_RESTRICTED = "charge:rds:restricted";
    /**
     *  =========================================RDS计费相关RedisKey-END========================================
     */
    /**
     *  -------------------------------------- API黑名单相关RedisKey BEGIN-------------------------------------------------
     */
    
    /**
     * 查询黑名单客户标识Key  
     */
    public static final String API_BLACK_BLACKCUS = "api:black:black_cus:";
    
    /**
     * 查询黑名单ip标识Key  
     */
    public static final String API_BLACK_BLACKIP = "api:black:black_ip:";
    
    /**
     *  -------------------------------------- API黑名单相关RedisKey END-------------------------------------------------
     */
    
    
    /**
     * =========================================API开关及最高权限手机号码相关KEY-开始========================================
     */
    /**
     * API开关状态key前缀
     */
    public static final String API_SWITCH_STATUS = "api:switch:status:";
    /**
     * 操作API开关所用验证码key前缀
     */
    public static final String API_SWITCH_CODE = "api:switch:code:";
    /**
     * 修改API开关最高权限手机号码时，向新手机发送的验证码key前缀
     */
    public static final String API_SWITCH_PHONE_NEW = "api:switch:phone:new:";
    /**
     * 修改API开关最高权限手机号码时，向旧手机发送的验证码key前缀
     */
    public static final String API_SWITCH_PHONE_OLD = "api:switch:phone:old:";
    /**
     * 根据数据中心API代号存储的数据中心信息Key前缀
     */
    public static final String API_DC_CODE = "api:dc:code:";
    /**
     * 根据数据中心id存储的数据中心API代号信息KEY前缀
     */
    public static final String API_DC_DCID = "api:dc:dcId:";
    
    /**
     * =========================================API开关及最高权限手机号码相关KEY-结束========================================
     */

    /**
        * =========================================API访问限制相关KEY-开始===================================================
        */
       /**
        * api访问限制(客户)
        */
       public static final String API_REQUEST_COUNT="apiRequestCount:";
       
       /**
        * api默认访问限制
        */
       public static final String API_REQUEST_COUNT_DEFAULT="apiRequestCountDefault:";

       /**
        * api 客户当前小时已访问次数
        */
       public static final String REQUEST_COUNT="requestCount:";
       /**
        * =========================================API访问限制相关KEY-结束===================================================
        */

    /**
     * =========================================API日志信息Redis数据-开始========================================
     */
    public static final String API_MONITORINGALARM_SERVICE_LOG_BASE_AVAILABLENUMBER = "api:monitoringalarm:service:log:base:availableNumber:" ;
    public static final String API_MONITORINGALARM_SERVICE_LOG_BASE_RIGHTNUMBER     = "api:monitoringalarm:service:log:base:rightNumber:" ;
    public static final String API_MONITORINGALARM_SERVICE_LOG_BASE_ALLNUMBER       = "api:monitoringalarm:service:log:base:allNumber:" ;
    public static final String API_MONITORINGALARM_SERVICE_LOG_BASE_DATENUMBER      = "api:monitoringalarm:service:log:base:dateNumber:" ;
    /**
     * =========================================API日志信息Redis数据-结束========================================
     */
    /**
     * =========================================存放上一个时刻维度数据的指标值-开始========================================
     */
    public static final String API_MONITORINGALARM_PREV = "api:monitoringalarm:prev" ;
    public static final String API_MONITORINGALARM_PREV_TIME = "api:monitoringalarm:prev:time" ;
    /**
     * =========================================存放上一个时刻维度数据的指标值-结束========================================
     */
    /**
     * =========================================报警触发条件分析数据-开始========================================
     */
    public static final String API_MONITORINGALARM_DEALTIME       = "api:monitoringalarm:dealTime:" ;
    public static final String API_MONITORINGALARM_AVAILABILITY   = "api:monitoringalarm:availability:" ;
    public static final String API_MONITORINGALARM_REQUESTSNUMBER = "api:monitoringalarm:requestsNumber:" ;
    public static final String API_MONITORINGALARM_CORRECT        = "api:monitoringalarm:correct:" ;
    /**
     * =========================================报警触发条件分析数据-结束========================================
     */
    /**
     * =========================================是否触发报警动作以及发送短信邮件提醒依赖数据结构-开始========================================
     */
    public static final String API_MONITORINGALARM_ISNEEDNOTIFY = "api:monitoringalarm:isNeedNotify:" ;
    /**
     * =========================================是否触发报警动作以及发送短信邮件提醒依赖数据结构-结束========================================
     */
    
    /**
     * =========================================云硬盘分类限速最高权限手机号码相关KEY-开始========================================
     */
    
    /**
     * 操作云硬盘分类限速验证码key前缀
     */
    public static final String VOLUMETYPE_CODE = "volumetype:code:";
    
    /**
     * 修改云硬盘分类限速最高权限手机号码时，向新手机发送的验证码key前缀
     */
    public static final String VOLUMETYPE_PHONE_NEW = "volumetype:phone:new:";
    
    /**
     * =========================================云硬盘分类限速最高权限手机号码相关KEY-结束========================================
     */
    /**
     * =========================================成员状态同步删除资源相关key-开始========================================
     */
    /**
     * 成员状态同步已删除资源队列
     */
    public static final String MEMBER_STAUS_SYNC_DELETED_RESOURCE = "member_status_sync_deleted_resource";
    /**
     * =========================================成员状态同步相关key-结束========================================
     */
    
    
    /**
     * =========================================清除资源监控指标数据key_开始========================================
     */
    public static final String MONITOR_ITEM_DELETE = "monitor:item:delete";
    
    /**
     * =========================================清除资源监控指标数据key_结束========================================
     */
    /**
     * =========================================负载均衡主备成员人为操作相关key_开始========================================
     */
    public static final String MEMBER_ADD = "member:add:";
    public static final String MEMBER_DELETE = "member:delete:";
    public static final String MEMBER_DELETE_POOLID = "member:delete:poolId:";
    public static final String MEMBER_UPDATE= "member:update:";
    public static final String MEMBER_UNBIND_POOLiD = "member:unbind:poolId:";
    public static final String MEMBER_SYNC = "member:sync24h:";
    public static final String DELETE_POOL="delete:pool:";
    
    /**
     * =========================================负载均衡主备成员人为操作相关key_结束========================================
     */

}