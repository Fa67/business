package com.eayun.common;

public class ConstantClazz {
	public static final String MODULEID = "0005";// 模块管理常量
	public static final String PLATFORM_BG = "bg";// 标识后台
	public static final String LOG_FLAG_PORTAL = "0";//
	public static final String SER_NAME = System.getProperty("weblogic.Name");// Server
																				// Bane
	public static final String OVER_LOAD_LOG_TABLE = "l_cust_log";// 前台日志表
	public static final String ADMIN = "admin";// 管理员
	public static final String ROOT = "root";// 超级管理员
	public static final String COMMON_ROLE = "common_role";// 通用角色，创建用户是的默认角色。
	public static final String MVC_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";// spring
																		// mvc
																		// 框架日期格式,只适应于mvc过滤器
	public final static String SPLITCHARACTER = ";";// 分号分隔符；
	public final static String CHARACTERENCOD = "UTF-8";// 工程编码设置
	public final static String BUSI_SCHEME = "0001,0002,0003,0006,0008,0009,0007"; // 业务方案类别id串。调用数据字典下显示网上营业厅
	public final static String PROJECTNAME = "EAYUNCLOUDV2.0";// 工程名称
	public final static String SUCCESS_CODE_DELETE = "100000";// 删除成功标识
	public final static String SUCCESS_CODE_ADD = "000000";// 添加成功标识
	public final static String SUCCESS_CODE_UPDATE = "200000";// 修改成功标识
	public final static String SUCCESS_CODE_OP = "400000";// 增，删，改之外操作成功标识
	public final static String SUCCESS_CODE_ARCHIVE = "500000";// 归档成功标识
	public final static String SUCCESS_CODE_OPERATE = "600000";// 归档成功标识
	public final static String DANGER_CODE = "010119";// 致命错误标识
	public final static String ERROR_CODE = "010120";// 错误标识
	public final static String SUCCESS_CODE = "000000";// 成功标识
	public final static String WARNING_CODE = "010110";// 警告标识
	public final static String INFO_CODE = "010114";// 普通信息提示标识
	public final static String DICT_CLOUD_HOST_NODE_ID = "0007004";// 数据字典中基础配置下，云主机配置节点
	public final static String DICT_CLOUD_HOST_MIRROR_NODE_ID = "0007002002";//数据字典中基础配置下，云主机下镜像类型配置根节点
	public final static String DICT_CLOUD_OS_WINDOWS_NODE_ID = "0007002002001";// WINDOWS操作系统类型节点
	public final static String DICT_CLOUD_OS_LINUX_NODE_ID = "0007002002002";//LINUX操作系统类型节点
	public final static String DICT_CLOUD_CPU_TYPE_NODE_ID = "0007002001";// 数据字典中基础配置下，CPU核数配置的根节点
	public final static String DICT_CLOUD_VMSTAUS_TYPE_NODE_ID = "0007002004";// 数据字典中基础配置下，云主机状态的列表
	public final static String DICT_CLOUD_SYS_TYPE_NODE_ID = "000700200200";// 数据字典中基础配置下，操作系统类型的ID前缀
	public final static String DICT_CLOUD_QTOS_NODE_ID = "0007002002003";// 数据字典中基础配置下，'其他'系统
	public final static String DICT_CLOUD_QTSYS_NODE_ID = "0007002002003001";// 数据字典中基础配置下，'其他'操作系统
	public final static String DICT_CLOUD_BUYCYCLE_NODE_ID = "0007002017";// 数据字典中基础配置下，购买周期节点ID
	public final static String DICT_CLOUD_MONTHLY_BUYCYCLE_NODE_ID = "0007002017001";// 数据字典中基础配置下，包月购买周期节点ID
	public final static String DICT_CLOUD_YEARLY_BUYCYCLE_NODE_ID = "0007002017002";// 数据字典中基础配置下，包年购买周期节点ID
	public final static String DICT_CLOUD_IMAGE_MARKETIMAGE__NODE_ID = "0007002019";// 数据字典中基础配置下，市场镜像的业务类型节点ID
	
	
	/**
	 * 常用的日志操作项
	 */
	public final static String LOG_LOGIN = "登录";
	public final static String LOG_LOGOUT = "退出";
	public final static String LOG_DELETE = "删除";
	public final static String LOG_ADD = "添加";
	public final static String LOG_UPDATE = "修改";
	public final static String LOG_QUERY = "查询";
	public final static String LOG_OPERATE = "操作";
	/**
	 * 日志状态
	 */
	public final static String LOG_STATU_SUCCESS = "1";//成功
	public final static String LOG_STATU_ERROR = "0";//失败
	
	/**
     * 日志资源类型(仅记录云资源相关)
     */
    
    public final static String LOG_TYPE_HOST = "云主机";
    public final static String LOG_TYPE_DISK = "云硬盘";
    public final static String LOG_TYPE_DISKSNAPSHOT = "云硬盘备份";
    public final static String LOG_TYPE_MIRROR = "镜像";
    public final static String LOG_TYPE_MIRROR_CUS = "自定义镜像";
    public final static String LOG_TYPE_MIRROR_PUBLIC = "公共镜像";
    public final static String LOG_TYPE_MIRROR_MARKET = "市场镜像";
    public final static String LOG_TYPE_MIRROR_UNCLASSIFIED = "未分类镜像";
    public final static String LOG_TYPE_NET = "网络";
    public final static String LOG_TYPE_SUBNET = "子网";
    public final static String LOG_TYPE_ROUTE = "路由";
    public final static String LOG_TYPE_POOL = "负载均衡";
    public final static String LOG_TYPE_MEMBER = "成员";
    public final static String LOG_TYPE_HEALTHMIR = "健康检查";
    public final static String LOG_TYPE_VIP = "VIP";
    public final static String LOG_TYPE_LISTENER = "监听";
    public final static String LOG_TYPE_FLOATIP = "弹性公网IP";
    public final static String LOG_TYPE_FIREWALL = "防火墙";
    public final static String LOG_TYPE_FIREPOLICY = "防火墙策略";
    public final static String LOG_TYPE_FIRERULE = "防火墙规则";
    public final static String LOG_TYPE_GROUP = "安全组";
    public final static String LOG_TYPE_GROUPRULE = "安全组规则";
    public final static String LOG_TYPE_MONITOR = "报警规则";
    public final static String LOG_TYPE_CONTACT = "管理联系人";
    public final static String LOG_TYPE_CONTACT_GROUP = "管理联系组";
    public final static String LOG_TYPE_SYSSETUP = "业务参数";
    public final static String LOG_TYPE_PROJECT = "项目";
    public final static String LOG_TYPE_QTEMPLATE = "配额模板";
    public final static String LOG_TYPE_CUSTOMER = "客户";
	public final static String LOG_TYPE_WORKORDER = "工单";
	public final static String LOG_TYPE_ORDER = "订单";
	public final static String LOG_TYPE_VPN = "VPN";
	public final static String LOG_TYPE_RDS = "云数据库";
	public final static String LOG_TYPE_ACCOUNT = "账户";
	public final static String LOG_TYPE_APIRESTRICT="客户API访问限制";
	public final static String LOG_TYPE_APIRESTRICT_DEFAULT="默认API访问次数";
    public final static String LOG_TYPE_VOLUME_TYPE = "分类限速";
    public final static String LOG_TYPE_OPERATIONAL = "操作验证管理";
    public final static String LOG_TYPE_INVOICE = "发票";
    public final static String LOG_TYPE_KEYPAIRS = "SSH密钥";
	/**邮件状态：未发送*/
	public final static String MAIL_STATUS_UNSENT = "0";
	/**邮件状态：发送成功*/
	public final static String MAIL_STATUS_SENT = "1";//发送成功
	/**邮件状态：发送失败*/
	public final static String MAIL_STATUS_FAILED = "2";//发送失败
	
	/**
	 * 短信业务来源：监控
	 */
	public final static String SMS_BIZ_MONITOR = "MONITOR";
	
    /**
     * 系統默认权限
     */
    public final static String[] DEFAULT_POWER = {"overview","user_","syssetup_","message_"};
    /**
     * 资源统计导出Excel标示
     */
    public final static String STATIS_COUNT_NET = "net";    //网络
    public final static String STATIS_COUNT_VM = "vm";      //云主机
    public final static String STATIS_COUNT_VOL = "vol";    //云硬盘
    
    public final static String STATIS_EXCEL_NET = "网络";
    public final static String STATIS_EXCEL_VM = "云主机";
    public final static String STATIS_EXCEL_VOL = "云硬盘";
    public final static String STATIS_EXCEL_ALL = "汇总";
    
    /*登录session信息 */
    public final static String SYS_SESSION_USERINFO = "USERINFO";
    public final static String PASSWORD_SESSION = "PASSKEY";
    
    public final static String FILTER_BLOCK_CODE = "666666";// 已被冻结用户拦截错误标示
    public final static String FILTER_ERROR_CODE = "999999";// 拦截错误标示
    
    public static String WEB_REAL_PATH = "";
    
    public static String WEB_PATH = "";         //系统跟路径,Listener初始化赋值
    
    
    public static String CURRENT_PHONE = "current_phone";//当时获取到的API最高权限手机号码session信息Key
    public static String VOlUMETYPE_PHONE = "volumetype_phone";//当时获取到的volumetype最高权限手机号码session信息Key
    public static String VOlUMETYPE_PHONE_SUCCESS = "volumetype_phone_success";//获取到的volumetype最高权限手机号码并验证通过session信息Key
    
    public static String TROVE_MANAGED_TENANT = "trove-managed-tenant";   // 云数据库实例的云主机所隶属的公共号租户的名称
    public static int RDS_FLAVOR_DISK = 60;          // 云数据库隶属主机的系统盘大小

    public static int RDS_MAX_AUTO_BACKUP = 7;      //云数据库自动备份最大值
    
    public final static String RDS_LOG_SWIFT_CONTAINER_PREFIX ="/database_logs_";		//RDS 日志OBS对象container的前缀
    public final static String RDS_DATASTORE_MYSQL = "mysql";
    
    /**
     * 云主机的密码方式登录
     */
    public final static String VM_LOGIN_TYPE_PWD ="pwd";
    
    /**
     * 云主机的SSH密钥方式登录
     */
    public final static String VM_LOGIN_TYPE_SSH = "ssh";
    
    /**
     * RDS 实例的日志类型-DBLog
     */
    public final static String RDS_LOG_TYPE_DBLOG = "DBLog";
    /**
     * RDS 实例的日志类型-DBLog
     */
    public final static String RDS_LOG_TYPE_SLOWLOG = "SlowLog";
    /**
     * RDS 实例的日志类型-DBLog
     */
    public final static String RDS_LOG_TYPE_ERRORLOG = "ErrorLog";
    /**
     * RDS 实例的日志类型-DBLog
     */
    public final static String RDS_LOG_TYPE_ALL = "All";
    
    public final static int SYNC_MAX_COUNT = 10;
}
