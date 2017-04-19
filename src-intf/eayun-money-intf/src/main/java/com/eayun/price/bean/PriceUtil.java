package com.eayun.price.bean;

/**
 * 
 * 数组内元素内容和元素顺序都会对代码产生影响，谨慎修改                      
 * @Filename: PriceUtil.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2016年7月28日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class PriceUtil {

	
	public final static String[] UNIT_PRICE = {"cpuSize","ramCapacity","snapshotSize",
			"bandValue","vpnCount","ipCount","cdnDownloadFlow","dreqsCount","hreqsCount",
			"cloudMySQLCPU","cloudMySQLRAM","sysDiskOrdinary","sysDiskBetter","sysDiskBest","dataDiskOrdinary",
			"dataDiskBetter","dataDiskBest","storageMySQLOrdinary","storageMySQLBetter","storageMySQLBest"};	//单价计价
	
	/**单价计价,原来的数据盘和系统盘单独抽出，需要删除但暂时保留*/
	public final static String[] UNIT_PRICE_TO_DELETE = {"dataDiskCapacity","sysDiskCapacity"};
	
	public final static String[] RANGE_PRICE = {"cusImageSize","connCount"};	//区间计价
	
	public final static String[] LADDER_PRICE = {"spaceCapacity"};				//阶梯计价
	
	public final static String[] DIFF_PRICE = {"downValue","requestCount"};		//差值阶梯计价
	
	
	public final static String[] COUNT_PRICE = {"vpnCount","ipCount"};			//按照个数计费的类型
	
	
	public final static String[] UPDATE_UNIT = {"cpuSize","ramCapacity","dataDiskCapacity","bandValue",
		"cloudMySQLCPU","cloudMySQLRAM","dataDiskOrdinary","dataDiskBetter","dataDiskBest",
		"storageMySQLOrdinary","storageMySQLBetter","storageMySQLBest"};		//升级时除负载均衡连接数的指标
	
	public final static String[] UPDATE_CONN = {"oldConnCount","newConnCount"};	//升级前后负载均衡连接数

	public final static String ConnCount = "connCount";		//升级配置时的特殊处理
	
	public final static String ImageId = "imageId";			//镜像做特殊处理
	
	public final static String IMAGE_PRICE_TYPE = "IMAGE";		//表示镜像价格
	
	public final static String[] UNIT_TEN_TIMES = {"sysDiskOrdinary","sysDiskBetter","sysDiskBest","dataDiskOrdinary",
		"dataDiskBetter","dataDiskBest","storageMySQLOrdinary","storageMySQLBetter","storageMySQLBest","snapshotSize"};	//计费单位为10GB的计费因子,增加了云硬盘备份
	
	public static enum priceType{	//导出价格配置类型
    	BASIC,			//基础资源
    	CLOUD			//云数据库
	}
}
