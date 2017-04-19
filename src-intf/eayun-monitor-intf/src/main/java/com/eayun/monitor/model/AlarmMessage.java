package com.eayun.monitor.model;

public class AlarmMessage extends BaseAlarmMessage {

    private static final long serialVersionUID = -8197975346404414413L;

    private String projectName;                              //所属项目
    private String vmName;                                   //报警对象资源名称
    private String alarmTime;                                //报警时间，用于展现
    private String alarmSign;                                //报警标识
    
    /*****************RDS1.0&网络1.3新增*************/
    private String alarmTypeName;                            //报警类型名称
    private String monitorTypeName;                          //监控项名称
    /*****************RDS1.0&网络1.3新增*************/

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public String getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(String alarmTime) {
        this.alarmTime = alarmTime;
    }

    public String getAlarmSign() {
        return alarmSign;
    }

    public void setAlarmSign(String alarmSign) {
        this.alarmSign = alarmSign;
    }

	public String getAlarmTypeName() {
		return alarmTypeName;
	}

	public void setAlarmTypeName(String alarmTypeName) {
		this.alarmTypeName = alarmTypeName;
	}

	public String getMonitorTypeName() {
		return monitorTypeName;
	}

	public void setMonitorTypeName(String monitorTypeName) {
		this.monitorTypeName = monitorTypeName;
	}
    
}
