package com.eayun.monitor.bean;

import com.eayun.common.RedisNodeIdConstant;
import com.eayun.common.constant.MongoCollectionName;

/**
 * 监控相关常量工具类
 * @Filename: MonitorConstantClazzUtil.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2017年3月2日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class MonitorAlarmUtil {

	/**
	 * 负载均衡指标相关的监控项nodeId数组
	 */
	public final static String[] LDPOOL_MONITOR_ZB_AGGS = {RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDCOMMON,
		RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDMASTER,RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDCOMMON,
		RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDMASTER};
	/**
	 * 负载均衡不活跃成员（节点）指标nodeId数组
	 */
	public final static String[] LDPOOL_MONITOR_MEMBER_AGGS ={"0008003001","0008004001",
		"0010003005001","0010003006001"};
	/**
	 * 负载均衡不活跃主节点指标nodeId数组
	 */
	public final static String[] LDPOOL_MONITOR_MASTER_AGGS ={"0008004002","0010003006002"};
	/**
	 * 负载均衡不活跃从节点指标nodeId数组
	 */
	public final static String[] LDPOOL_MONITOR_SALVE_AGGS ={"0008004003","0010003006003"};
	
	
	public final static String LDPOOL_MODE_COMMON ="0";		//负载均衡普通模式
	public final static String LDPOOL_MODE_MASTER ="1";		//负载均衡主备模式
	
	public final static String MEMBER_ROLE_ACTIVE ="Active";		//主节点
	public final static String MEMBER_ROLE_BACKUP ="Backup";		//从节点
	
	
	public static enum MonitorResourceType{	//监控资源类型
    	VM,			//云主机
    	RDS,		//数据库实例
    	POOL		//负载均衡
	}
	
	/**
	 * 判断一个元素是否在一个数组内
	 * @Author: duanbinbin
	 * @param element
	 * @param array
	 * @return
	 *<li>Date: 2017年3月3日</li>
	 */
	public static boolean checkArray(String element,String[] array){
		for(String way : array){
			if(element.equals(way)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 根据监控项nodeId匹配报警类型所属的监控项nodeId
	 * @Author: duanbinbin
	 * @param monitorType
	 * @return
	 *<li>Date: 2017年3月2日</li>
	 */
	public static String getAlarmTypeParentId(String monitorType) {
		String alarmTypeParentId = "";
        switch (monitorType) {
            case RedisNodeIdConstant.ECSC_MONITOR_TYPE_VM:		//ECSC监控项-云主机
            case RedisNodeIdConstant.ECMC_MONITOR_VM_NODEID:	//ECMC监控项-云主机
            	alarmTypeParentId = RedisNodeIdConstant.ALARM_TYPE_MONITOR_VM;
                break;
            case RedisNodeIdConstant.ECSC_MONITOR_TYPE_CLOUDDATA:	//ECSC监控项-云数据库
            case RedisNodeIdConstant.ECMC_MONITOR_TYPE_CLOUDDATA:	//ECMC监控项-云数据库
            	alarmTypeParentId = RedisNodeIdConstant.ALARM_TYPE_MONITOR_CLOUDDATA;
                break;
            case RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDCOMMON:	//ECSC监控项-负载均衡普通模式
            case RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDCOMMON:	//ECMC监控项-负载均衡普通模式
            	alarmTypeParentId = RedisNodeIdConstant.ALARM_TYPE_MONITOR_LDCOMMON;
                break;
            case RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDMASTER:	//ECSC监控项-负载均衡主备模式
            case RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDMASTER:	//ECSC监控项-负载均衡主备模式
            	alarmTypeParentId = RedisNodeIdConstant.ALARM_TYPE_MONITOR_LDMASTER;
                break;
        }
        return alarmTypeParentId;
	}
	/**
	 * 确定每一个指标的报警类型
	 * @Author: duanbinbin
	 * @param zb
	 * @return
	 *<li>Date: 2017年3月3日</li>
	 */
	public static String getAlarmTypeByZb(String zbNodeId) {
		String alarmTypeNodeId = "";
        switch (zbNodeId) {
        case "0008001002":		//ECSC监控项-云主机-CPU利用率
        case "0010003001001":	//ECMC监控项-云主机-CPU利用率
        	alarmTypeNodeId = "0010008001001";
            break;
        case "0008001003":		//ECSC监控项-云主机-内存占用率
        case "0010003001002":	//ECMC监控项-云主机-内存占用率
        	alarmTypeNodeId = "0010008001002";
            break;
        case "0008001004":	//ECSC监控项-云主机-磁盘读吞吐
        case "0010003001003":	//ECMC监控项-云主机-磁盘读吞吐
        case "0008001005":	//ECSC监控项-云主机-磁盘写吞吐
        case "0010003001004":	//ECMC监控项-云主机-磁盘写吞吐
        	alarmTypeNodeId = "0010008001003";
            break;
        case "0008001006":	//ECSC监控项-云主机-网卡下行速率
        case "0010003001005":	//ECMC监控项-云主机-网卡下行速率
        case "0008001007":	//ECSC监控项-云主机-网卡上行速率
        case "0010003001006":	//ECMC监控项-云主机-网卡上行速率
        	alarmTypeNodeId = "0010008001004";
            break;
        case "0008002001":		//ECSC监控项-云数据库-CPU利用率
        case "0010003004001":	//ECMC监控项-云数据库-CPU利用率
        	alarmTypeNodeId = "0010008002001";
        	break;
        case "0008002002":		//ECSC监控项-云数据库-内存占用率
        case "0010003004002":	//ECMC监控项-云数据库-内存占用率
        	alarmTypeNodeId = "0010008002002";
        	break;
        case "0008002003":	//ECSC监控项-云数据库-磁盘读吞吐
        case "0010003004003":	//ECMC监控项-云数据库-磁盘读吞吐
        case "0008002004":	//ECSC监控项-云数据库-磁盘写吞吐
        case "0010003004004":	//ECMC监控项-云数据库-磁盘写吞吐
        	alarmTypeNodeId = "0010008002003";
        	break;
        case "0008002005":	//ECSC监控项-云数据库-网卡下行速率
        case "0010003004005":	//ECMC监控项-云数据库-网卡下行速率
        case "0008002006":	//ECSC监控项-云数据库-网卡上行速率
        case "0010003004006":	//ECMC监控项-云数据库-网卡上行速率
        	alarmTypeNodeId = "0010008002004";
        	break;
        case "0008002007":		//ECSC监控项-云数据库-磁盘使用率
        case "0010003004007":	//ECMC监控项-云数据库-磁盘使用率
        	alarmTypeNodeId = "0010008002005";
            break;
        case "0008003001":		//ECSC监控项-负载均衡普通模式-不活跃成员百分比
        case "0010003005001":	//ECMC监控项-负载均衡普通模式-不活跃成员百分比
        	alarmTypeNodeId = "0010008003001";
            break;
        case "0008004001":		//ECSC监控项-负载均衡主备模式-不活跃节点百分比
        case "0010003006001":	//ECMC监控项-负载均衡主备模式-不活跃节点百分比
        	alarmTypeNodeId = "0010008004001";
            break;
        case "0008004002":		//ECSC监控项-负载均衡主备模式-不活跃主节点百分比
        case "0010003006002":	//ECMC监控项-负载均衡主备模式-不活跃主节点百分比
        	alarmTypeNodeId = "0010008004002";
            break;
        case "0008004003":		//ECSC监控项-负载均衡主备模式-不活跃从节点百分比
        case "0010003006003":	//ECMC监控项-负载均衡主备模式-不活跃从节点百分比
        	alarmTypeNodeId = "0010008004003";
            break;
        }
        return alarmTypeNodeId;
	}
	
	/**
	 * 确定每个指标的mongo集合名称
	 * @Author: duanbinbin
	 * @param zbNodeId
	 * @return
	 *<li>Date: 2017年3月3日</li>
	 */
	public static String getMongoByZb(String zbNodeId) {
		String collectionName = "";
        switch (zbNodeId) {
        case "0008001002":	//ECSC监控项-云主机-CPU利用率
        case "0008002001":	//ECSC监控项-云数据库-CPU利用率
        	
        case "0010003001001":	//ECMC监控项-云主机-CPU利用率
        case "0010003004001":	//ECMC监控项-云数据库-CPU利用率
        	collectionName = MongoCollectionName.MONITOR_CPU_DETAIL;
            break;
        case "0008001003":	//ECSC监控项-云主机-内存占用率
        case "0008002002":	//ECSC监控项-云数据库-内存占用率
        	
        case "0010003001002":	//ECMC监控项-云主机-内存占用率
        case "0010003004002":	//ECMC监控项-云数据库-内存占用率
        	collectionName = MongoCollectionName.MONITOR_MEMORY_DETAIL;
            break;
        case "0008001004":	//ECSC监控项-云主机-磁盘读吞吐
        case "0008002003":	//ECSC监控项-云数据库-磁盘读吞吐
        	
        case "0010003001003":	//ECMC监控项-云主机-磁盘读吞吐
        case "0010003004003":	//ECMC监控项-云数据库-磁盘读吞吐
        	collectionName = MongoCollectionName.MONITOR_DISK_READ_DETAIL;
            break;
        case "0008001005":	//ECSC监控项-云主机-磁盘写吞吐
        case "0008002004":	//ECSC监控项-云数据库-磁盘写吞吐
        	
        case "0010003001004":	//ECMC监控项-云主机-磁盘写吞吐
        case "0010003004004":	//ECMC监控项-云数据库-磁盘写吞吐
        	collectionName = MongoCollectionName.MONITOR_DISK_WRITE_DETAIL;
            break;
        case "0008001006":	//ECSC监控项-云主机-网卡下行速率
        case "0008002005":	//ECSC监控项-云数据库-网卡下行速率
        	
        case "0010003001005":	//ECMC监控项-云主机-网卡下行速率
        case "0010003004005":	//ECMC监控项-云数据库-网卡下行速率
        	collectionName = MongoCollectionName.MONITOR_NET_OUT_DETAIL;
            break;
        case "0008001007":	//ECSC监控项-云主机-网卡上行速率
        case "0008002006":	//ECSC监控项-云数据库-网卡上行速率
        	
        case "0010003001006":	//ECMC监控项-云主机-网卡上行速率
        case "0010003004006":	//ECMC监控项-云数据库-网卡上行速率
        	collectionName = MongoCollectionName.MONITOR_NET_IN_DETAIL;
            break;
        case "0008002007":	//ECSC监控项-云数据库-磁盘使用率
        	
        case "0010003004007":	//ECMC监控项-云数据库-磁盘使用率
        	collectionName = MongoCollectionName.MONITOR_VOLUME_USED_DETAIL;
            break;
        case "0008003001":	//ECSC监控项-负载均衡普通模式-不活跃成员百分比
        case "0008004001":	//ECSC监控项-负载均衡主备模式-不活跃节点百分比
        case "0008004002":	//ECSC监控项-负载均衡主备模式-不活跃主节点百分比
        case "0008004003":	//ECSC监控项-负载均衡主备模式-不活跃从节点百分比
        	
        case "0010008003001":	//ECMC监控项-负载均衡普通模式-不活跃成员百分比
        case "0010008004001":	//ECMC监控项-负载均衡主备模式-不活跃节点百分比
        case "0010008004002":	//ECMC监控项-负载均衡主备模式-不活跃主节点百分比
        case "0010008004003":	//ECMC监控项-负载均衡主备模式-不活跃从节点百分比
        	collectionName = MongoCollectionName.MONITOR_LD_POOL_DETAIL;
            break;
        }
        return collectionName;
	}
	/**
	 * 根据指标nodeId获取指标单位
	 * 用于ECSC，ECMC指标单位存储于指标数据字典的para1中
	 * @Author: duanbinbin
	 * @param zbNodeId
	 * @return
	 *<li>Date: 2017年3月9日</li>
	 */
	public static String getZbUnitByZb(String zbNodeId) {
		String zbUnit = "";
        switch (zbNodeId) {
        case "0008001002":		//ECSC监控项-云主机-CPU利用率
        case "0010003001001":	//ECMC监控项-云主机-CPU利用率
        	
        case "0008001003":		//ECSC监控项-云主机-内存占用率
        case "0010003001002":	//ECMC监控项-云主机-内存占用率
        	
        case "0008002001":		//ECSC监控项-云数据库-CPU利用率
        case "0010003004001":	//ECMC监控项-云数据库-CPU利用率
        	
        case "0008002002":		//ECSC监控项-云数据库-内存占用率
        case "0010003004002":	//ECMC监控项-云数据库-内存占用率
        	
        case "0008002007":		//ECSC监控项-云数据库-磁盘使用率
        case "0010003004007":	//ECMC监控项-云数据库-磁盘使用率
        	
        case "0008003001":		//ECSC监控项-负载均衡普通模式-不活跃成员百分比
        case "0010008003001":	//ECMC监控项-负载均衡普通模式-不活跃成员百分比
        	
        case "0008004001":		//ECSC监控项-负载均衡主备模式-不活跃节点百分比
        case "0010008004001":	//ECMC监控项-负载均衡主备模式-不活跃节点百分比
        	
        case "0008004002":		//ECSC监控项-负载均衡主备模式-不活跃主节点百分比
        case "0010008004002":	//ECMC监控项-负载均衡主备模式-不活跃主节点百分比
        	
        case "0008004003":		//ECSC监控项-负载均衡主备模式-不活跃从节点百分比
        case "0010008004003":	//ECMC监控项-负载均衡主备模式-不活跃从节点百分比
        	zbUnit = "%";
            break;
        case "0008001004":	//ECSC监控项-云主机-磁盘读吞吐
        case "0010003001003":	//ECMC监控项-云主机-磁盘读吞吐
        case "0008001005":	//ECSC监控项-云主机-磁盘写吞吐
        case "0010003001004":	//ECMC监控项-云主机-磁盘写吞吐
        	
        case "0008002003":	//ECSC监控项-云数据库-磁盘读吞吐
        case "0010003004003":	//ECMC监控项-云数据库-磁盘读吞吐
        case "0008002004":	//ECSC监控项-云数据库-磁盘写吞吐
        case "0010003004004":	//ECMC监控项-云数据库-磁盘写吞吐
        	zbUnit = "MB/s";
            break;
        case "0008001006":	//ECSC监控项-云主机-网卡下行速率
        case "0010003001005":	//ECMC监控项-云主机-网卡下行速率
        case "0008001007":	//ECSC监控项-云主机-网卡上行速率
        case "0010003001006":	//ECMC监控项-云主机-网卡上行速率
        	
        case "0008002005":	//ECSC监控项-云数据库-网卡下行速率
        case "0010003004005":	//ECMC监控项-云数据库-网卡下行速率
        case "0008002006":	//ECSC监控项-云数据库-网卡上行速率
        case "0010003004006":	//ECMC监控项-云数据库-网卡上行速率
        	zbUnit = "Mb/s";
        }
        return zbUnit;
	}
	/**
	 * 根据监控项nodeId获取名称
	 * 用于邮件短信模板
	 * @Author: duanbinbin
	 * @param monitorType
	 * @return
	 *<li>Date: 2017年3月3日</li>
	 */
	public static String getMonitorNameByType(String monitorType) {
		String resourceType = "";
		if(RedisNodeIdConstant.ECSC_MONITOR_TYPE_VM.equals(monitorType)
				||RedisNodeIdConstant.ECMC_MONITOR_VM_NODEID.equals(monitorType)){
			resourceType = "云主机";
		}else if(RedisNodeIdConstant.ECSC_MONITOR_TYPE_CLOUDDATA.equals(monitorType)
				||RedisNodeIdConstant.ECMC_MONITOR_TYPE_CLOUDDATA.equals(monitorType)){
			resourceType = "MySQL实例";
		}else if(RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDCOMMON.equals(monitorType)
				||RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDCOMMON.equals(monitorType)){
			resourceType = "负载均衡（普通模式）";
		}else if(RedisNodeIdConstant.ECSC_MONITOR_TYPE_LDMASTER.equals(monitorType)
				||RedisNodeIdConstant.ECMC_MONITOR_TYPE_LDMASTER.equals(monitorType)){
			resourceType = "负载均衡（主备模式）";
		}else if(RedisNodeIdConstant.ECMC_MONITOR_TYPE_API.equals(monitorType)){
			resourceType = "API";
		}
		return resourceType;
	}
	/**
	 * 实例版本名称转义
	 * @Author: duanbinbin
	 * @param datastoreversion
	 * @return
	 *<li>Date: 2017年3月16日</li>
	 */
	public static String transferDatastoreName(String datastoreversion) {
		if("mysql".equals(datastoreversion)){
			return "MySQL ";
		}
		return "MySQL ";
	}
}
