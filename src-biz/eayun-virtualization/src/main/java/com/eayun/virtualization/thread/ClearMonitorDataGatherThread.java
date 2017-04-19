package com.eayun.virtualization.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.monitor.bean.MonitorAlarmUtil;

public class ClearMonitorDataGatherThread implements Runnable {

	private static final Logger     log    = LoggerFactory.getLogger(ClearMonitorDataGatherThread.class);

	private MongoTemplate           mongoTemplate;
	private JedisUtil               jedisUtil;
	private JSONObject              json;

	public ClearMonitorDataGatherThread(MongoTemplate mongoTemplate,
			JedisUtil jedisUtil, JSONObject json) {
		this.mongoTemplate = mongoTemplate;
		this.jedisUtil = jedisUtil;
		this.json = json;
	}
	
	
	/**
	 * 云主机指标详情及汇总级和名称前缀
	 */
	private static String[]     prefixs = new String[] {
		"cpu_util",
		"memory.usage",
        "disk.read.bytes.rate",
        "disk.write.bytes.rate",
        "network.incoming.bytes.rate",
        "network.outgoing.bytes.rate"
        };
	
	/**
	 * /磁盘使用率指标详情及汇总集合名称前缀
	 */
	private static String       volusedPrefix = "volume.used";
	
	/**
	 * 指标详情及汇总集合名称后缀
	 */
	private static String[]     suffixs = new String[] {
		".detail",
		".3min",
		".5min",
        ".10min",
        ".1h",
        ".1d"
        };
	
	/**
	 * 负载均衡的指标详情集合名称
	 */
	private static String poolCollection = MongoCollectionName.MONITOR_LD_POOL_DETAIL;
	
	/**
	 * 云主机及实例最新（次新）指标redisKey
	 */
	private static String[]     vmRedisKeys = new String[] {
		RedisKey.MONITOR_CPU,
		RedisKey.MONITOR_CPU_LAST,
		RedisKey.MONITOR_MEMORY,
		RedisKey.MONITOR_MEMORY_LAST,
		RedisKey.MONITOR_DISK_READ,
		RedisKey.MONITOR_DISK_READ_LAST,
		RedisKey.MONITOR_DISK_WRITE,
		RedisKey.MONITOR_DISK_WRITE_LAST,
		RedisKey.MONITOR_NETWORK_INCOMING,
		RedisKey.MONITOR_NETWORK_INCOMING_LAST,
		RedisKey.MONITOR_NETWORK_OUTGOING,
		RedisKey.MONITOR_NETWORK_OUTGOING_LAST
        };
	
	/**
	 * 数据库实例磁盘使用率最新（次新）指标redisKey
	 */
	private static String[]     volRedisKeys = new String[] {
		RedisKey.MONITOR_VOLUME_USED,
		RedisKey.MONITOR_VOLUME_USED_LAST
        };
	
	/**
	 * 负载均衡最新（次新）指标redisKey
	 */
	private static String[]     poolRedisKeys = new String[] {
		RedisKey.MONITOR_EXP_LDPOOL,
		RedisKey.MONITOR_EXP_LDPOOL_LAST
        };
	
	@Override
	public void run() {
		String type = json.getString("type");
		String resourceId = json.getString("resourceId");
		if(StringUtil.isEmpty(type) || StringUtil.isEmpty(resourceId)){
			log.error("redis信息错误："+json.toJSONString());
		}
		log.info("开始清除"+type+"资源:"+resourceId+"的监控指标数据...");
		if(MonitorAlarmUtil.MonitorResourceType.VM.toString().equals(type)){
			this.clearVmMonitorData(resourceId);
		}else if(MonitorAlarmUtil.MonitorResourceType.RDS.toString().equals(type)){
			this.clearVmMonitorData(resourceId);
			this.clearRdsMonitorData(resourceId);
		}else if(MonitorAlarmUtil.MonitorResourceType.POOL.toString().equals(type)){
			this.clearPoolMonitorData(resourceId);
		}
	}
	/**
	 * 清除云主机相关的六项指标的mongo及redis数据
	 * 也包含云数据库实例的六项普通指标
	 * @Author: duanbinbin
	 *<li>Date: 2017年3月16日</li>
	 */
	private void clearVmMonitorData(String resourceId){
		
		for(String prefix : prefixs ){
			for(String suffix : suffixs ){
				mongoTemplate.remove(new Query(Criteria.where("vm_id").is(resourceId)), prefix+suffix);
			}
		}
		for(String vmRedisKey : vmRedisKeys ){
			try {
				jedisUtil.delete(vmRedisKey+resourceId);
			} catch (Exception e) {
				log.error("删除普通指标"+vmRedisKey+"的redis指标数据失败："+resourceId,e);
			}
		}
	}
	/**
	 * 清除数据库实例的磁盘使用率指标的mongo及redis数据
	 * @Author: duanbinbin
	 *<li>Date: 2017年3月16日</li>
	 */
	private void clearRdsMonitorData(String resourceId){
		
		for(String suffix : suffixs ){
			mongoTemplate.remove(new Query(Criteria.where("vm_id").is(resourceId)), volusedPrefix+suffix);
		}
		for(String volRedisKey : volRedisKeys ){
			try {
				jedisUtil.delete(volRedisKey+resourceId);
			} catch (Exception e) {
				log.error("删除磁盘使用率redis指标数据失败："+resourceId,e);
			}
		}

	}
	/**
	 * 清除负载均衡的指标的mongo及redis数据
	 * @Author: duanbinbin
	 *<li>Date: 2017年3月16日</li>
	 */
	private void clearPoolMonitorData(String resourceId){
		mongoTemplate.remove(new Query(Criteria.where("ldPoolId").is(resourceId)), poolCollection);
		for(String poolRedisKey : poolRedisKeys ){
			try {
				jedisUtil.delete(poolRedisKey+resourceId);
			} catch (Exception e) {
				log.error("删除负载均衡redis指标数据失败："+resourceId,e);
			}
		}
	}
}
