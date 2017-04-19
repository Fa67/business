package com.eayun.common.constant;

import java.util.HashMap;
import java.util.Map;

import com.eayun.common.util.CloudResourceUtil;

/**
 * 云主机的API参数
 * @author zhouhaitao
 *
 */
public interface ApiInstanceConstant {
	/**
	 * 公网IP
	 */
	String BUY_FLOATIP_NO  = "0";						//不购买公网IP
	String BUY_FLOATIP_YES  = "1";						//购买公网IP
	
	/**
	 * 镜像类型
	 */
	String IMAGE_TYPE_PUBLILC = "0"; 					//公共镜像
	String IMAGE_TYPE_PRIVATE = "1"; 					//自定义镜像
	String IMAGE_TYPE_MARKET = "2"; 					//市场镜像
	
	/**
	 * 云主机登录方式
	 */
	String LOGIN_MODEL_KEY = "keypair";					//秘钥方式登录
	String LOGIN_MODEL_PWD = "passwd";					//密码方式登录
	
	/**
	 * 管理员用户名称
	 */
	String LINUX_USERNAME = "root";
	String WINDOWS_USERNAME = "Administrator";
	
	/**
	 * 默认安全组类型
	 */
	String SECURITYGROUP_DEFAULT = "0";					//default安全组
	String SECURITYGROUP_LINUX = "1";					//开放Linux 22端口的安全组
	String SECURITYGROUP_WINDOWS = "2";					//开放 Windows 3389端口的安全组
	String SECURITYGROUP_DEFAULT_NAME = "default";							//default安全组
	String SECURITYGROUP_LINUX_NAME = "Linux安全组放通22端口";					//开放Linux 22端口的安全组
	String SECURITYGROUP_WINDOWS_NAME = "Windows安全组放通3389端口";			//开放 Windows 3389端口的安全组
	
	/**
	 * 订单的产品名称
	 */
	String PAYBEFORE_VM_PRODNAME = "云主机-包年包月";
	String PAYAFTER_VM_PRODNAME = "云主机-按需计费";
	String UPGRADE_VM_PRODNAME = "云主机-调整配置";
	
	/**
	 * 计费模式
	 */
	String PAYTYPE_MONTH = "Pay_Month";					//包月付费
	String PAYTYPE_DYNAMIC = "Pay_Dynamic";				//按需付费
	
	String REGEX_UUID_CONSTAINS_ = "^[0-9a-z]{8}-([0-9a-z]{4}-){3}[0-9a-z]{12}$";
	String REGEX_UUID_NOCONSTAINS_ = "^[0-9a-z]{8}([0-9a-z]{4}){3}[0-9a-z]{12}$";
	
	String PASSWORD_REGEX = "^[0-9a-zA-Z~@#%+-=/(_)*&<>\\[\\]\\\";'|$^?!.{}`/,]{8,30}$";
	String NUMBER_REGEX = "^[0-9]$";
	String LOWER_REGEX = "^[a-z]$";
	String UPPER_REGEX = "^[A-Z]$";
	String SPEC_CHAR_REGEX = "^[~@#%+-=/(_)*&<>\\[\\]\\\";'|$^?!.{}`/,]$";
	String IP_REGEX = "^(1|([1-9]{1,2}|[1-9]0)|(1[0-9]{2}|2[0-5]{2}))((.(0|([1-9]{1,2}|[1-9]0)|(1[0-9]{2}|2[0-5]{2}))){2}).(1|([1-9]{1,2}|[1-9]0)|(1[0-9]{2}|2[0-5]{2}))$";
	
	String CLOUD_RESOURCE_NAME_REGEX = "^[\\u4e00-\\u9fa5a-zA-Z0-9]([\\u4e00-\\u9fa5_a-zA-Z0-9\\s]{0,18}[\\u4e00-\\u9fa5a-zA-Z0-9]){0,1}$";
	String VM_BATCH_COUNT_REGEX = "^([1-9]|1[0-9]?)|20$";
	String VM_PAYDURATION_REGEX = "([1-9]|1[0-2]|24|36)";
	
	/**
	 * 置空操作符
	 */
	String SET_NULL = "";
	
	/**
	 * API云主机参数
	 * @author zhouhaitao
	 *
	 */
	interface Instance {
		String REGION = "Region";						//数据中心简称
		String CUSID = "CusId";							//客户ID
		String DCID = "DcId";							//数据中心ID
		String INSTANCEID = "InstanceId";				//云主机ID
		String VPCID = "VPCId";							//私有网络ID
		String MSUBNETID = "MsubNetId";					//受管子网ID
		String UMSUBNETID = "UmsubNetId";				//自管子网ID
		String FLOATIP = "FloatIP";						//是否购买公网IP
		String IMAGETYPE = "ImageType";					//镜像类型
		String IMAGEID = "ImageId";						//镜像ID
		String CPU = "CPU";								//云主机CPU
		String MEMORY = "Memory";						//云主机内存
		String LOGINMODE = "LoginMode";					//云主机登录方式
		String KEYPAIR = "Keypair";						//秘钥ID
		String PASSWORD = "Password";					//云主机密码
		String INSTANCENAME = "InstanceName";			//云主机名称
		String INSTANCEREMARK = "InstanceRemark";		//云主机描述
		String SECURITYGROUPNAME = "SecurityGroupName";	//安全组名称
		String COUNT = "Count";							//安全组名称
		String PAYTYPE = "PayType";						//计费方式
		String PAYDURATION = "Payduration";				//计费周期
		String STATUS = "Status";						//云主机状态
		String SYSNAME = "SysName";						//系统名称
		String VOLUMECOUNT = "VolumeCount";				//挂载云硬盘数量
		String SYSVLOUMESIZE = "SysVloumeSize";			//系统盘大小
		String VOLUMESIZE = "VolumeSize";				//挂载的数据盘大小
		String MSUBNETIP = "MsubNetIP";					//受管子网IP
		String UMSUBNETIP = "UmsubNetIP";				//自管子网IP
		String PUBLICIP = "PublicIP";					//公网IP
		String CREATETIME = "CreateTime";				//云主机创建时间
		String EXPIRTIME = "ExpirTime";					//到期时间
		String SECURITYGROUPID = "SecurityGroupId";		//安全组ID
		
		String TOTALCOUNT = "Totalcount";				//总个数
		String INSTANCEIDS = "InstanceIds";				//云主机ID数组
		String IMAGEIDS = "ImageIds";                   //镜像ID数组
		String INSTANCESET = "InstanceSet";				//云主机结果集
		String SEARCHWORD = "SearchWord";				//查询关键字
		String IP = "IP";                               //IP查询
		String OFFSET = "Offset";						//偏移量
		String LIMIT = "Limit";							//返回数量
		
		String ISRECYCLE="IsRecycle";                   //是否放入回收站（0进入回收站 1不进入回收站）
	}
	
	/**
	 * API安全组参数
	 * @author liuzhuangzhuang
	 *
	 */
	interface SecurityGroup{
		String SECURITYGROUPIDS = "SecurityGroupIds";	    //安全组ID数据
		String SECURITYGROUPSET = "SecurityGroupset";	    //安全组结果集
		String SECURITYGROUPID = "SecurityGroupId";		    //安全组ID
		String SECURITYGROUPNAME = "SecurityGroupName";	    //安全组名称
		String SECURITYGROUPREMARK = "SecurityGroupRemark"; //安全组描述信息
		String ISDEFAULT = "IsDefault";                     //是否为默认安全组
		String RULECOUNT = "RuleCount";                     //安全组中规则的数量
		String ATTACHRULESET = "AttachRuleSet";             //安全组中关联的规则
		String ATTACHINSTANCEID = "AttachInstanceId";       //安全组中加入的主机ID
	}
	
	/**
	 * API安全组规则参数
	 * @author liuzhuangzhuang
	 *
	 */
	interface SecurityGroupRule{
		String DIRECTION = "Direction";    //规则的方向
		String ETHERTYPE = "EtherType";    //以太网类型
		String IPPROTOCOl = "IpProtocol";  //协议类型
		String PORTRANGE = "PortRange";    //端口范围
		String ICMPTYPE = "ICMPType";      //ICMP协议类型
		String ICMPCODE = "ICMPCode";      //ICMP协议编码
		String SOURCE = "Source";          //源地址
	}
	
	/**
	 * API云主机状态
	 * @author zhouhaitao
	 *
	 */
	public enum InstanceStatus{
		
		ACTIVE("ACTIVE","ACTIVE"),								//运行中
		BUILDING("BUILDING","BUILDING"),						//创建中	
		DELETING("DELETING","DELETING"),						//删除中
		STARTING("STARTING","STARTING"),						//启动中
		ERROR("ERROR","ERROR"),									//故障
		SHUTOFFING("SHUTOFFING","SHUTOFFING"),					//关机中	
		SHUTOFF("SHUTOFF","SHUTOFF"),							//已关机
		SUSPENDEDING("SUSPENDEDING","SUSPENDEDING"),			//暂停服务中	
		SUSPENDED("SUSPENDED","SUSPENDED"),						//暂停服务	
		RESUMING("RESUMING","RESUMING"),						//恢复中	
		REBOOT("REBOOT","REBOOT"),								//重启中
		RESIZE("RESIZE","RESIZE"),								//升级中
		VERIFY_RESIZE("VERIFY_RESIZE","RESIZE"),				//升级中
		RESIZED("RESIZED","RESIZE"),							//升级中
		SOFT_DELETING("SOFT_DELETING","DELETING"),				//删除中
		SOFT_RESUME("SOFT_RESUME","RECOVERING"),				//恢复中
		SOFT_DELETED("SOFT_DELETED","SOFT_DELETE");				//已删除
		 
		private String status ;
		private String vmStatus;
		
		private InstanceStatus(String status,String vmStatus){
			this.status = status;
			this.vmStatus = vmStatus;
		}
		
		public static String getVmStatus(String status){
			for (InstanceStatus instanceStatus : InstanceStatus.values()) {
				if (instanceStatus.getStatus().equals(status)) {
				    return instanceStatus.getVmStatus();
				}
			}
			
			return null;
		}
		
		public String getStatus() {
			return status;
		}
		
		public void setStatus(String status) {
			this.status = status;
		}
		
		public String getVmStatus() {
			return vmStatus;
		}
		
		public void setVmStatus(String vmStatus) {
			this.vmStatus = vmStatus;
		}
		
		
	}
	
	/**
	 * Api云主机状态查询参数
	 * @author gaoxiang
	 * 
	 */
	public final static Map SearchMap = new HashMap() {{
	    put("ACTIVE", "ACTIVE");
	    put("SHUTOFF", "SHUTOFF");
	    put("ERROR", "ERROR");
	    put("SUSPENDED", "SUSPENDED");
	    put("SOFT_DELETED", "SOFT_DELETE");
	    put("ARREARS", CloudResourceUtil.CLOUD_CHARGESTATE_NSF_CODE);
	    put("EXPIRE", CloudResourceUtil.CLOUD_CHARGESTATE_EXPIRED_CODE);
	}};
	
}
