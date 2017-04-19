package com.eayun.common;

/**
 * 所有使用到的redis记录中nodeId常量类
 *                       
 * @Filename: RedisNodeIdConstant.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2017年1月4日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class RedisNodeIdConstant {
	
	/************价格配置开始********************************************************/
	/**
	 * 基础资源价格配置类型根节点，下级为云主机、云硬盘、云硬盘备份...等
	 */
	public final static String PRICE_CONFIG = "0007004";
	
	/**
	 * 云数据库价格配置根节点，其下为MySQL，以后可能会增加mongo等
	 */
	public final static String CLOUD_DATA_PRICE_CONFIG = "0007005";
	
	/**
	 * 市场镜像业务类别根节点nodeId
	 */
	public final static String MARKET_BUSINESS_TYPE = "0007002019";
	/************价格配置结束********************************************************/
	
	/************监控报警开始********************************************************/
	/**
	 * ECSC云监控项类别节点nodeId，下级为云主机、云数据库、负载均衡...等
	 */
	public final static String MONITOR_TYPE_NODE_ID = "0008";
	
	/**
	 * ECSC云主机资源监控项根节点，下为CPU利用率、内存占用率等监控指标
	 */
	public final static String ECSC_MONITOR_TYPE_VM = "0008001";
	
	/**
	 * ECSC云数据库资源监控项根节点，下为磁盘使用率等监控指标
	 */
	public final static String ECSC_MONITOR_TYPE_CLOUDDATA = "0008002";
	
	/**
	 * ECSC负载均衡普通模式资源监控项根节点，下为不活跃成员百分比等监控指标
	 */
	public final static String ECSC_MONITOR_TYPE_LDCOMMON = "0008003";
	
	/**
	 * ECSC负载均衡主备模式资源监控项根节点，下为不活跃节点百分比等监控指标
	 */
	public final static String ECSC_MONITOR_TYPE_LDMASTER = "0008004";
	
	/**
	 * 运维云监控项类别节点nodeId，下级为云主机、API...等
	 */
	public final static String ECMC_MONITOR_TYPE_NODE_ID = "0010003";
	
	/**
	 * ECMC监控项-云主机类别根节点
	 */
	public final static String ECMC_MONITOR_VM_NODEID = "0010003001";
	
	/**
	 * ECMC API资源监控项根节点
	 */
	public final static String ECMC_MONITOR_TYPE_API = "0010003003";
	
	/**
	 * ECMC云数据库资源监控项根节点，下为磁盘使用率等监控指标
	 */
	public final static String ECMC_MONITOR_TYPE_CLOUDDATA = "0010003004";
	
	/**
	 * ECMC负载均衡普通模式资源监控项根节点，下为不活跃成员百分比等监控指标
	 */
	public final static String ECMC_MONITOR_TYPE_LDCOMMON = "0010003005";
	
	/**
	 * ECMC负载均衡主备模式资源监控项根节点，下为不活跃节点百分比等监控指标
	 */
	public final static String ECMC_MONITOR_TYPE_LDMASTER = "0010003006";
	
	/**
	 * 触发条件操作符根节点
	 */
	public final static String ECMC_TRIGGER_OPER_NODE_ID = "0010004";
	
	/**
	 * 触发条件持续时间根节点
	 */
	public final static String ECMC_TRIGGER_TIME_NODE_ID = "0010005";
	
	/**
	 * 云主机监控列表历史时间范围根节点
	 */
	public final static String VM_MONITOR_TIME = "0010001";
	
	/**
	 * 主机监控详情类别根节点
	 */
	public final static String VM_MONITOR_DETAILS_TYPE = "0010006";
	
	/**
	 * 主机监控详情时间范围根节点
	 */
	public final static String VM_MONITOR_DETAILS_TIME = "0010007";
	
	/**
	 * 报警类型-云主机根节点，下为内存、磁盘等报警类型
	 */
	public final static String ALARM_TYPE_MONITOR_VM = "0010008001";
	
	/**
	 * 报警类型-云数据库根节点，下为内存、磁盘使用率等报警类型
	 */
	public final static String ALARM_TYPE_MONITOR_CLOUDDATA = "0010008002";
	
	/**
	 * 报警类型-负载均衡普通模式根节点，下为后端不活跃成员报警类型
	 */
	public final static String ALARM_TYPE_MONITOR_LDCOMMON = "0010008003";
	
	/**
	 * 报警类型-负载均衡主备模式根节点，下为后端不活跃节点等报警类型
	 */
	public final static String ALARM_TYPE_MONITOR_LDMASTER = "0010008004";
	/************监控报警结束********************************************************/
	
	/**
	 * CDN加速地址节点
	 */
	public final static String CDN_ACCELERATE_ADDRESS = "0009005";
	
	/**
	 * CDN加速缓存设置
	 */
	public final static String CDN_CACHE_CONFIG = "0009006";
	
	/**
	 * 总览资源类型节点node_id
	 */
	public final static String DC_RESOURCE_TYPE_NODEID = "0007002015";
}
