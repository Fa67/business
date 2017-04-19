package com.eayun.common.constant;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.eayun.common.util.DateUtil;

/**
 * 资源类型枚举
 * @Filename: ResourceType.java
 * @Description: 
 * @Version: 1.0
 * @Author: bo.zeng
 * @Email: bo.zeng@eayun.com
 * @History:<br>
 *<li>Date: 2016年8月5日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class ResourceType {
	/**
	 * Comment for <code>VM</code> 云主机
	 */
	public static final String VM = "0";
	/**
	 * Comment for <code>VDISK</code> 云硬盘
	 */
	public static final String VDISK = "1";
	/**
	 * Comment for <code>DISKSNAPSHOT</code> 云硬盘备份
	 */
	public static final String DISKSNAPSHOT = "2";
	/**
	 * Comment for <code>NETWORK</code> 私有网络
	 */
	public static final String NETWORK = "3";
	/**
	 * Comment for <code>QUOTAPOOL</code> 负载均衡
	 */
	public static final String QUOTAPOOL = "4";
	/**
	 * Comment for <code>FLOATIP</code> 弹性公网IP
	 */
	public static final String FLOATIP = "5";
	/**
	 * Comment for <code>OBS</code> 对象存储
	 */
	public static final String OBS = "6";
	/**
	 * Comment for <code>VPN</code> VPN
	 */
	public static final String VPN = "7";
	
	/**
	 * Comment for <code>RDS</code> 云数据库
	 */
	public static final String RDS = "8";

	/**
	 * 获取资源类型中文名称
	 * @param value 类型值
	 * @return 类型中文名称
	 */
	public static String getName(String value) {
		switch (value) {
		case VM:
			return "云主机";
		case DISKSNAPSHOT:
			return "云硬盘备份";
		case FLOATIP:
			return "弹性公网IP";
		case NETWORK:
			return "私有网络";
		case OBS:
			return "对象存储";
		case QUOTAPOOL:
			return "负载均衡器";
		case VDISK:
			return "云硬盘";
		case VPN:
			return "VPN";
		case RDS:
			return "云数据库";
		default:
			return "";
		}
	}
	
	public static List<String> getAllTypeValues() {
		List<String> allTypeValues = new ArrayList<String>();
		try {
			Class clazz = ResourceType.class;
			Field[] fields = clazz.getDeclaredFields();
			if (fields.length > 0) {
				for (Field field : fields) {
					String fieldValue = (String)field.get(null);
					allTypeValues.add(fieldValue);
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return allTypeValues;
	}

}
