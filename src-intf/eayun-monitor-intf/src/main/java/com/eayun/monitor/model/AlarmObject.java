package com.eayun.monitor.model;

/**
 * ECSC报警对象
 * RDS1.0新增数据库实例版本
 *                       
 * @Filename: AlarmObject.java
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
public class AlarmObject extends BaseAlarmObject {

    private static final long serialVersionUID = -3001215047358155490L;

    private String            vmName;                                  //报警对象资源名称
    private String            network;                                 //所属网络，主机所在的专有网络和子网，例如“network  subnet1”
    private String            vmIp;                                    //受管子网IP
    private String            floatIp;									//弹性公网IP
    private String            alarmRuleName;							//报警规则名称
    
    /*****************RDS1.0&网络1.3新增*************/
    private String            dataVersionName;							//数据库实例版本
    private String            isMaster;									//是否主库（数据库实例）
    private String            mode;										//负载均衡模式
    private String            selfSubIp;                               	//自管子网IP
    private String 			  dcName;									//数据中心名称
    private String 			  config;									//负载均衡配置
    
    private String 			  prjId;									//项目id
    private String 			  dcId;										//数据中心id
    
    private Boolean 		  isDeleted;								//对象资源是否已删除（解决垃圾数据问题）
    /*****************RDS1.0&网络1.3新增*************/

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getVmIp() {
        return vmIp;
    }

    public void setVmIp(String vmIp) {
        this.vmIp = vmIp;
    }

    public String getFloatIp() {
        return floatIp;
    }

    public void setFloatIp(String floatIp) {
        this.floatIp = floatIp;
    }

	public String getAlarmRuleName() {
		return alarmRuleName;
	}

	public void setAlarmRuleName(String alarmRuleName) {
		this.alarmRuleName = alarmRuleName;
	}

	public String getDataVersionName() {
		return dataVersionName;
	}

	public void setDataVersionName(String dataVersionName) {
		this.dataVersionName = dataVersionName;
	}

	public String getDcName() {
		return dcName;
	}

	public void setDcName(String dcName) {
		this.dcName = dcName;
	}

	public String getSelfSubIp() {
		return selfSubIp;
	}

	public void setSelfSubIp(String selfSubIp) {
		this.selfSubIp = selfSubIp;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public String getIsMaster() {
		return isMaster;
	}

	public void setIsMaster(String isMaster) {
		this.isMaster = isMaster;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getPrjId() {
		return prjId;
	}

	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}

	public String getDcId() {
		return dcId;
	}

	public void setDcId(String dcId) {
		this.dcId = dcId;
	}

}
