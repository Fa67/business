package com.eayun.common.constant;

/**
 * MongoDB CollectionName常量类
 *                       
 * @Filename: MongoCollectionName.java
 * @Description: 
 * @Version: 1.0
 * @Author: chenhao
 * @Email: hao.chen@eayun.com
 * @History:<br>
 *<li>Date: 2016年9月2日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class MongoCollectionName {
	/**
	 * =========================================schedule log Begin===================================
	 */

    /**
     * log.schedule
     * 
     *Comment for <code>LOG_SCHEDULE</code>
     */
    public static final String LOG_SCHEDULE                      = "log.schedule";
    
    /**
	 * =========================================schedule log End======================================
	 */
    /**
	 * =========================================ecmc log Begin========================================
	 */
    /**
     * log.ecmc
     * 
     *Comment for <code>LOG_ECMC</code>
     */
    public static final String LOG_ECMC                          = "log.ecmc";
    
    /**
	 * =========================================ecmc log End==========================================
	 */
    /**
	 * =========================================ecsc log Begin========================================
	 */
    /**
     * log.ecsc
     * 
     *Comment for <code>LOG_ECSC</code>
     */
    public static final String LOG_ECSC                          = "log.ecsc";
    /**
	 * =========================================ecsc log End==========================================
	 */
    /**
	 * =========================================money treatment Begin=================================
	 */
    /**
     * log.money.failed
     * 
     *Comment for <code>LOG_MONEY_FAILED</code>
     */
    public static final String LOG_MONEY_FAILED                  = "log.money.failed";

    /**
     * log.money.success
     * 
     *Comment for <code>LOG_MONEY_SUCCESS</code>
     */
    public static final String LOG_MONEY_SUCCESS                 = "log.money.success";
    
    /**
     * log.chargeRecord.op.fail
     */
    public static final String LOG_CHARGE_RECORD_OP_FAIL         = "log.chargeRecord.op.fail";
    
    /**
	 * =========================================money treatment End==================================
	 */
    /**
	 * =========================================obs Begin============================================
	 */
    /**
     * log.api.dns
     */
    public static final String LOG_API_DNS						 ="log.api.dns";
    
    /**
     * log.api.cdn
     */
    public static final String LOG_API_CDN						 ="log.api.cdn";
    
    /**
     * cdn.refresh.bucket
     */
    public static final String CDN_REFRESH_BUCKET				 ="cdn.refresh.bucket";
    /**
     * cdn.refresh.bucket
     */
    public static final String CDN_REFRESH_OBJECT				 ="cdn.refresh.object";
    
    /**
     * obs.cdn.1d
     */
    public static final String OBS_CDN_1D			 			 ="obs.cdn.1d";
    
    /**
     * obs.cdn.1h
     */
    public static final String OBS_CDN_1H			 			 ="obs.cdn.1h";
    
    /**
     * obs.cdn.1mon
     */
    public static final String OBS_CDN_1MON			 			 ="obs.cdn.1mon";
    
    /**
     * log.obs.notice
     */
    public static final String LOG_OBS_NOTICE					 ="log.obs.notice";
    
    /**
     * obs.used.1h
     * 
     *Comment for <code>OBS_USED_1H</code>
     */
    public static final String OBS_USED_1H                       = "obs.used.1h";

    /**
     * obs.used.24h
     * 
     *Comment for <code>OBS_USED_24H</code>
     */
    public static final String OBS_USED_24H                      = "obs.used.24h";

    /**
     * obs.used.1month
     * 
     *Comment for <code>OBS_USED_1MONTH</code>
     */
    public static final String OBS_USED_1MONTH                   = "obs.used.1month";

    /**
     * obs.storageUsed.1h
     * 
     *Comment for <code>OBS_STORAGE_1H</code>
     */
    public static final String OBS_STORAGE_1H                    = "obs.storageUsed.1h";

    /**
     * obs.storageUsed.1month
     * 
     *Comment for <code>OBS_STORAGE_1MONTH</code>
     */
    public static final String OBS_STORAGE_1MONTH                = "obs.storageUsed.1month";

    /**
     * obs.storageUsed.24h
     * 
     *Comment for <code>OBS_STORAGE_24H</code>
     */
    public static final String OBS_STORAGE_24H                   = "obs.storageUsed.24h";
    /**
     * 回源流量API返回详细信息
     */
    public static final String CDN_BACKSOURCE_API_DETAILS    	= "cdn.backsource.api.details";
    /**
     * 回源流量统计
     */
    public static final String CDN_BACKSOURCE_1H                 = "cdn.backsource.1h";
    
    public static final String CDN_BACKSOURCE_1D                 = "cdn.backsource.1d";
    
    public static final String CDN_BACKSOURCE_1MONTH             = "cdn.backsource.1month";
    
    /**
   	 * =========================================obs End==============================================
   	 */
    /**
   	 * =========================================ecmc monitor alarm Begin=============================
   	 */
    /**
     * ecmc.monitor.alarm.item
     * 
     *Comment for <code>ECMC_MONITOR_ALARM_ITEM</code>
     */
    public static final String ECMC_MONITOR_ALARM_ITEM           = "ecmc.monitor.alarm.item";
    
    /**
   	 * =========================================ecmc monitor alarm End===============================
   	 */
    /**
   	 * =========================================ecsc monitor alarm Begin=============================
   	 */
    /**
     * monitor.alarm.item
     * 
     *Comment for <code>MONITOR_ALARM_ITEM</code>
     */
    public static final String MONITOR_ALARM_ITEM                = "monitor.alarm.item";

    /**
   	 * =========================================ecsc monitor  alarm End=============================
   	 */
    /**
   	 * =========================================monitor Begin=======================================
   	 */
    /**
     * bandwidth.network.incoming.detail
     * 
     *Comment for <code>BANDWIDTH_NETWORK_INCOMING_DETAIL</code>
     */
    public static final String BANDWIDTH_NETWORK_INCOMING_DETAIL = "bandwidth.network.incoming.detail";

    /**
     * bandwidth.network.outgoing.detail
     * 
     *Comment for <code>BANDWIDTH_NETWORK_OUTGOING_DETAIL</code>
     */
    public static final String BANDWIDTH_NETWORK_OUTGOING_DETAIL = "bandwidth.network.outgoing.detail";
    /**
   	 * =========================================monitor End==========================================
   	 */

    /**
     * 资源计费失败的日志记录集合名称
     */
    public static final String LOG_CHARGE_FAILED = "log.charge.failed";
    
    /**
     * 管理系统计划任务漏跑信息的日志记录集合名称
     */
    public static final String SCHEDULE_LOST_JOB = "schedule.lost.job";
    
    
    /**
   	 * =========================================资源监控指标 Begin=======================================
   	 */
    /**
     * 云主机（数据库实例）CPU利用率指标详情
     */
    public static final String MONITOR_CPU_DETAIL = "cpu_util.detail";
    
    /**
     * 云主机（数据库实例）内存占用率指标详情
     */
    public static final String MONITOR_MEMORY_DETAIL = "memory.usage.detail";
    
    /**
     * 云主机（数据库实例）磁盘读吞吐指标详情
     */
    public static final String MONITOR_DISK_READ_DETAIL = "disk.read.bytes.rate.detail";
    
    /**
     * 云主机（数据库实例）磁盘写吞吐指标详情
     */
    public static final String MONITOR_DISK_WRITE_DETAIL = "disk.write.bytes.rate.detail";
    
    /**
     * 云主机（数据库实例）网卡上行速率指标详情
     */
    public static final String MONITOR_NET_IN_DETAIL = "network.incoming.bytes.rate.detail";
    
    /**
     * 云主机（数据库实例）网卡下行速率指标详情
     */
    public static final String MONITOR_NET_OUT_DETAIL = "network.outgoing.bytes.rate.detail";
    
    /**
     * 云数据库实例磁盘使用率指标详情
     */
    public static final String MONITOR_VOLUME_USED_DETAIL = "volume.used.detail";
    
    /**
     * 负载均衡指标详情
     */
    public static final String MONITOR_LD_POOL_DETAIL = "ldpool.monitor.detail";
    
    /**
     * 负载均衡成员异常记录
     */
    public static final String MONITOR_LD_POOL_MEMBER_EXP = "ldpool.monitor.member.exp";
    /**
   	 * =========================================资源监控指标 End==========================================
   	 */
}