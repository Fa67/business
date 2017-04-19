package com.eayun.virtualization.model;

import java.util.Date;
import java.util.Set;

public class CloudVm extends BaseCloudVm {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int cpus;	//cpu配置
	private int rams;	//内存配置
	private int disks;	//硬盘配置
	private int dataDisks;//数据盘配置
	private int dataCapacity;	//数据盘容量
	private int number;			//批量创建时创建个数
	private String prjName;		//所属项目名称
	private int volCount;		//挂载的云硬盘数量
	private String netName;		//网络名称
	private String createTimeForDis;//前端显示的时间格式
	private String perStatus;	//操作之前的状态
	private String isExsit;		//是否存在中间状态    0 没有   	1有	
	private String isDeleting;	//是否正在删除
	private String floatIp;		//公网Ip
	private String floatId;		//公网id
	private String dcName;		//数据中心名称
	private String imageName;	//自定义镜像名称
	private String imageDesc;   //自定义镜像描述
	private String securityGroups; //安全组名称列表
	private int count;	//同步使用
	private BaseCloudSecurityGroup [] bcsgs;//关联的安全组
	private String vmStatusStr;	//转义后的状态名称
	private	String subnetName;	//子网名称
	private String dataVolumeId;//新建的数据盘ID
	private String password;	//云主机密码
	private String username;    //管理员账户
	
	private String cusOrg;		//所属客户
	/*************记录当前vm冻结挂起是否成功***********/
	private String vmBlockStatus; //两种 true false
	private String payTypeStr;
	private String selfSubnetName;//自管子网名称
	private String sgId;		  //默认安全组ID
	private String orderNo;		  //订单编号
	private String buyFloatIp;	  //是否购买公网IP
	private String cusId;		  //客户ID
	private Date opDate;		  //云主机操作时间
	private String deleteType;  //是否是软件删除   0 软删除  1强制删除   2回收站删除
	private String sysTypeEn;	//镜像类型
	private int cycleCount;	//剩余天数
	private String sourceType;  //自定义镜像来源类型
	private String professionType; //市场镜像的业务类型
	private String sourceId;       //原始镜像ID
	
	private String sysTypeId;//系统盘类型id
	
	//同步用到
	private String isAttch;//是否执行了挂载
	private String vmSure;//云主机是否同步完成
	private String volumeSure;//云硬盘同步完成
	private String volTypeId;//系统盘类型id
	private String volType;//系统盘类型 1普通型 2性能型
	private String loginType ;				//登陆方式
	private String secretPublicKey;			// SSH密钥-Public 内容
	private Set<String> csks;				//选择的SSH密钥
	private String routeId;					//子网绑定的路由ID
	private int sshCount;				    //SSH密钥绑定个数
	private int sshAddCount;				//SSH密钥绑定个数
	private int sshDelCount;				//SSH密钥解绑个数
	
	
	
	public String getVmBlockStatus() {
		return vmBlockStatus;
	}
	public void setVmBlockStatus(String vmBlockStatus) {
		this.vmBlockStatus = vmBlockStatus;
	}
	public String getDataVolumeId() {
		return dataVolumeId;
	}
	public void setDataVolumeId(String dataVolumeId) {
		this.dataVolumeId = dataVolumeId;
	}
	public String getFloatId() {
		return floatId;
	}
	public void setFloatId(String floatId) {
		this.floatId = floatId;
	}
	public String getSubnetName() {
		return subnetName;
	}
	public void setSubnetName(String subnetName) {
		this.subnetName = subnetName;
	}
	public String getVmStatusStr() {
		return vmStatusStr;
	}
	public void setVmStatusStr(String vmStatusStr) {
		this.vmStatusStr = vmStatusStr;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public BaseCloudSecurityGroup[] getBcsgs() {
		return bcsgs;
	}
	public void setBcsgs(BaseCloudSecurityGroup[] bcsgs) {
		this.bcsgs = bcsgs;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public int getCpus() {
		return cpus;
	}
	public void setCpus(int cpus) {
		this.cpus = cpus;
	}
	public int getRams() {
		return rams;
	}
	public void setRams(int rams) {
		this.rams = rams;
	}
	public int getDisks() {
		return disks;
	}
	public void setDisks(int disks) {
		this.disks = disks;
	}
	public int getDataCapacity() {
		return dataCapacity;
	}
	public void setDataCapacity(int dataCapacity) {
		this.dataCapacity = dataCapacity;
	}
	public String getPrjName() {
		return prjName;
	}
	public void setPrjName(String prjName) {
		this.prjName = prjName;
	}
	public int getVolCount() {
		return volCount;
	}
	public void setVolCount(int volCount) {
		this.volCount = volCount;
	}
	public String getNetName() {
		return netName;
	}
	public void setNetName(String netName) {
		this.netName = netName;
	}
	public String getCreateTimeForDis() {
		return createTimeForDis;
	}
	public void setCreateTimeForDis(String createTimeForDis) {
		this.createTimeForDis = createTimeForDis;
	}
	public String getPerStatus() {
		return perStatus;
	}
	public void setPerStatus(String perStatus) {
		this.perStatus = perStatus;
	}
	public String getIsExsit() {
		return isExsit;
	}
	public void setIsExsit(String isExsit) {
		this.isExsit = isExsit;
	}
	public String getIsDeleting() {
		return isDeleting;
	}
	public void setIsDeleting(String isDeleting) {
		this.isDeleting = isDeleting;
	}
	public String getFloatIp() {
		return floatIp;
	}
	public void setFloatIp(String floatIp) {
		this.floatIp = floatIp;
	}
	public CloudVm() {
		super();
	}
	
	
	public String getSecurityGroups() {
		return securityGroups;
	}
	public void setSecurityGroups(String securityGroups) {
		this.securityGroups = securityGroups;
	}
	public String getImageName() {
		return imageName;
	}
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	public String getDcName() {
		return dcName;
	}
	public void setDcName(String dcName) {
		this.dcName = dcName;
	}
	public CloudVm(String vmId, String osType, Date endTime, String createName,
			String dcId, String prjId, String sysType, Date createTime,
			String vmName, String vmStatus, String fromImageId,
			String fromVolumeId, String flavorId, String netId, String vmIp,
			String hostId, String hostName, String vmDescripstion,
			String vmFrom, String isDeleted, Date deleteTime,
			String deleteUser, int cpus, int rams, int disks, String prjName,
			int volCount, String netName, String createTimeForDis,
			String perStatus, String isExsit, String isDeleting) {
		super(vmId, osType, endTime, createName, dcId, prjId, sysType,
				createTime, vmName, vmStatus, fromImageId, fromVolumeId,
				flavorId, netId, vmIp, hostId, hostName, vmDescripstion,
				vmFrom, isDeleted, deleteTime, deleteUser);
		this.cpus = cpus;
		this.rams = rams;
		this.disks = disks;
		this.prjName = prjName;
		this.volCount = volCount;
		this.netName = netName;
		this.createTimeForDis = createTimeForDis;
		this.perStatus = perStatus;
		this.isExsit = isExsit;
		this.isDeleting = isDeleting;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getCusOrg() {
		return cusOrg;
	}
	public void setCusOrg(String cusOrg) {
		this.cusOrg = cusOrg;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getImageDesc() {
		return imageDesc;
	}
	public String getPayTypeStr() {
		return payTypeStr;
	}
	public void setPayTypeStr(String payTypeStr) {
		this.payTypeStr = payTypeStr;
	}
	public void setImageDesc(String imageDesc) {
		this.imageDesc = imageDesc;
	}
	public String getSelfSubnetName() {
		return selfSubnetName;
	}
	public void setSelfSubnetName(String selfSubnetName) {
		this.selfSubnetName = selfSubnetName;
	}
	public String getSgId() {
		return sgId;
	}
	public void setSgId(String sgId) {
		this.sgId = sgId;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getBuyFloatIp() {
		return buyFloatIp;
	}
	public void setBuyFloatIp(String buyFloatIp) {
		this.buyFloatIp = buyFloatIp;
	}
	public String getCusId() {
		return cusId;
	}
	public void setCusId(String cusId) {
		this.cusId = cusId;
	}
	public Date getOpDate() {
		return opDate;
	}
	public void setOpDate(Date opDate) {
		this.opDate = opDate;
	}
	public String getDeleteType() {
		return deleteType;
	}
	public void setDeleteType(String deleteType) {
		this.deleteType = deleteType;
	}
	public String getSysTypeEn() {
		return sysTypeEn;
	}
	public void setSysTypeEn(String sysTypeEn) {
		this.sysTypeEn = sysTypeEn;
	}
	public int getCycleCount() {
		return cycleCount;
	}
	public void setCycleCount(int cycleCount) {
		this.cycleCount = cycleCount;
	}
	public String getSourceType() {
		return sourceType;
	}
	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}
	public String getProfessionType() {
		return professionType;
	}
	public void setProfessionType(String professionType) {
		this.professionType = professionType;
	}
	public String getSourceId() {
		return sourceId;
	}
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	public int getDataDisks() {
		return dataDisks;
	}
	public void setDataDisks(int dataDisks) {
		this.dataDisks = dataDisks;
	}
	public String getIsAttch() {
		return isAttch;
	}
	public void setIsAttch(String isAttch) {
		this.isAttch = isAttch;
	}
	public String getSysTypeId() {
		return sysTypeId;
	}
	public void setSysTypeId(String sysTypeId) {
		this.sysTypeId = sysTypeId;
	}
	public String getVmSure() {
		return vmSure;
	}
	public void setVmSure(String vmSure) {
		this.vmSure = vmSure;
	}
	public String getVolumeSure() {
		return volumeSure;
	}
	public void setVolumeSure(String volumeSure) {
		this.volumeSure = volumeSure;
	}
	public String getLoginType() {
		return loginType;
	}
	public void setLoginType(String loginType) {
		this.loginType = loginType;
	}
	public String getSecretPublicKey() {
		return secretPublicKey;
	}
	public void setSecretPublicKey(String secretPublicKey) {
		this.secretPublicKey = secretPublicKey;
	}
	public Set<String> getCsks() {
		return csks;
	}
	public void setCsks(Set<String> csks) {
		this.csks = csks;
	}
	public String getRouteId() {
		return routeId;
	}
	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}
	public int getSshCount() {
		return sshCount;
	}
	public void setSshCount(int sshCount) {
		this.sshCount = sshCount;
	}
	public int getSshAddCount() {
		return sshAddCount;
	}
	public void setSshAddCount(int sshAddCount) {
		this.sshAddCount = sshAddCount;
	}
	public int getSshDelCount() {
		return sshDelCount;
	}
	public void setSshDelCount(int sshDelCount) {
		this.sshDelCount = sshDelCount;
	}
	public String getVolTypeId() {
		return volTypeId;
	}
	public void setVolTypeId(String volTypeId) {
		this.volTypeId = volTypeId;
	}
	public String getVolType() {
		return volType;
	}
	public void setVolType(String volType) {
		this.volType = volType;
	}
	
	
	
	
}