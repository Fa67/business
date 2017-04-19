package com.eayun.notice.model;

import java.math.BigDecimal;

public class MessageOperateModel {
	
	
	private String DayTime;

	
	private  String  onedayfileidmoney;//金额文件ID
	private String 	onedayfileidorder;//订单文件ID
	
	private BigDecimal onedaynewincome;//一天新增收入
	
	private Integer onedayneworder;//一天新增订单
	
	private Integer onedaycus;//一天新增客户
	
	private String onedaycusname;//最高充值客户名称

	private BigDecimal onedaycusmoney;//最高充值客户金额
	
	private String onedayRecusname;//按需付费资源最高客户名称
	
	private BigDecimal onedayRecusmoney;//按需付费资源最高客户金额
	
	private Integer onedaynewvmcount;//新增云主机数量
	
	private Integer onedaynewvolumecount;//新增数据盘数量
	
	private Integer onedaynewbackups;//新增云硬盘备份数量
	
	private Integer onedaynewbalanc;//新增负载均衡数量
	
	private Integer onedaynewvpn;//新增VPN数量
	
	private Integer onedaynewmysql;//新增mysql数量
	
	
	private  String  alldayfileidmoney;//金额文件ID
	private String 	alldayfileidorder;//订单文件ID
	
	private BigDecimal alldaynewincome;//累计收入
	
	private Integer alldayneworder;//累计订单
	
	private Integer alldaycus;//飞冻结客户数量
	
	private String alldaycusname;//累计最高充值客户名称

	private BigDecimal alldaycusmoney;//累计最高充值客户金额
	
	private String alldayRecusname;//按需付费资源最高客户名称
	
	private BigDecimal alldayRecusmoney;//按需付费资源最高客户金额
	
	private String alldayVmcusname;//云主机保有量最高客户名称
	
	private Integer alldayVmcount;//云主机数量
	
	private String alldayIPcusname;//弹性公网IP保有量最高客户
	
	private Integer alldayIPcount;//弹性公网ip数量
	
	
	
	
	private Integer[] alldaycpu;//cpu使用超额比
	
	private Integer[] alldaymemory;//内存使用超额比
	
	private Integer[] alldaydisk;//磁盘使用超额比
	
	private Integer[] alldayip;//弹性公网ip使用超额比
	
	private Integer  alldayvmcount;//云主机总数量
	
	private Integer alldaydatacount;//数据盘数量
	
	private Integer alldaydatabackupcount;//云硬盘备份数量

	private Integer alldaybalanccount;//负载均衡数量
	
	private Integer alldayVPNcount;//vpn数量
	
	private Integer[]  alldayobject;//对象存储   已使用/总量
	
	private Integer alldaymysqlcount;//mysql数量

	public BigDecimal getOnedaynewincome() {
		return onedaynewincome;
	}

	public void setOnedaynewincome(BigDecimal onedaynewincome) {
		this.onedaynewincome = onedaynewincome;
	}

	public Integer getOnedayneworder() {
		return onedayneworder;
	}

	public void setOnedayneworder(Integer onedayneworder) {
		this.onedayneworder = onedayneworder;
	}

	public Integer getOnedaycus() {
		return onedaycus;
	}

	public void setOnedaycus(Integer onedaycus) {
		this.onedaycus = onedaycus;
	}

	public String getOnedaycusname() {
		return onedaycusname;
	}

	public void setOnedaycusname(String onedaycusname) {
		this.onedaycusname = onedaycusname;
	}

	public BigDecimal getOnedaycusmoney() {
		return onedaycusmoney;
	}

	public void setOnedaycusmoney(BigDecimal onedaycusmoney) {
		this.onedaycusmoney = onedaycusmoney;
	}

	public String getOnedayRecusname() {
		return onedayRecusname;
	}

	public void setOnedayRecusname(String onedayRecusname) {
		this.onedayRecusname = onedayRecusname;
	}

	public BigDecimal getOnedayRecusmoney() {
		return onedayRecusmoney;
	}

	public void setOnedayRecusmoney(BigDecimal onedayRecusmoney) {
		this.onedayRecusmoney = onedayRecusmoney;
	}

	public Integer getOnedaynewvmcount() {
		return onedaynewvmcount;
	}

	public void setOnedaynewvmcount(Integer onedaynewvmcount) {
		this.onedaynewvmcount = onedaynewvmcount;
	}

	public Integer getOnedaynewvolumecount() {
		return onedaynewvolumecount;
	}

	public void setOnedaynewvolumecount(Integer onedaynewvolumecount) {
		this.onedaynewvolumecount = onedaynewvolumecount;
	}

	public Integer getOnedaynewbackups() {
		return onedaynewbackups;
	}

	public void setOnedaynewbackups(Integer onedaynewbackups) {
		this.onedaynewbackups = onedaynewbackups;
	}

	public Integer getOnedaynewbalanc() {
		return onedaynewbalanc;
	}

	public void setOnedaynewbalanc(Integer onedaynewbalanc) {
		this.onedaynewbalanc = onedaynewbalanc;
	}

	public Integer getOnedaynewvpn() {
		return onedaynewvpn;
	}

	public void setOnedaynewvpn(Integer onedaynewvpn) {
		this.onedaynewvpn = onedaynewvpn;
	}

	public Integer getOnedaynewmysql() {
		return onedaynewmysql;
	}

	public void setOnedaynewmysql(Integer onedaynewmysql) {
		this.onedaynewmysql = onedaynewmysql;
	}

	public BigDecimal getAlldaynewincome() {
		return alldaynewincome;
	}

	public void setAlldaynewincome(BigDecimal alldaynewincome) {
		this.alldaynewincome = alldaynewincome;
	}

	public Integer getAlldayneworder() {
		return alldayneworder;
	}

	public void setAlldayneworder(Integer alldayneworder) {
		this.alldayneworder = alldayneworder;
	}

	public Integer getAlldaycus() {
		return alldaycus;
	}

	public void setAlldaycus(Integer alldaycus) {
		this.alldaycus = alldaycus;
	}

	public String getAlldaycusname() {
		return alldaycusname;
	}

	public void setAlldaycusname(String alldaycusname) {
		this.alldaycusname = alldaycusname;
	}

	public BigDecimal getAlldaycusmoney() {
		return alldaycusmoney;
	}

	public void setAlldaycusmoney(BigDecimal alldaycusmoney) {
		this.alldaycusmoney = alldaycusmoney;
	}

	public String getAlldayRecusname() {
		return alldayRecusname;
	}

	public void setAlldayRecusname(String alldayRecusname) {
		this.alldayRecusname = alldayRecusname;
	}

	public BigDecimal getAlldayRecusmoney() {
		return alldayRecusmoney;
	}

	public void setAlldayRecusmoney(BigDecimal alldayRecusmoney) {
		this.alldayRecusmoney = alldayRecusmoney;
	}

	public String getAlldayVmcusname() {
		return alldayVmcusname;
	}

	public void setAlldayVmcusname(String alldayVmcusname) {
		this.alldayVmcusname = alldayVmcusname;
	}

	public Integer getAlldayVmcount() {
		return alldayVmcount;
	}

	public void setAlldayVmcount(Integer alldayVmcount) {
		this.alldayVmcount = alldayVmcount;
	}

	public String getAlldayIPcusname() {
		return alldayIPcusname;
	}

	public void setAlldayIPcusname(String alldayIPcusname) {
		this.alldayIPcusname = alldayIPcusname;
	}

	public Integer getAlldayIPcount() {
		return alldayIPcount;
	}

	public void setAlldayIPcount(Integer alldayIPcount) {
		this.alldayIPcount = alldayIPcount;
	}

	

	public Integer getAlldayvmcount() {
		return alldayvmcount;
	}

	public void setAlldayvmcount(Integer alldayvmcount) {
		this.alldayvmcount = alldayvmcount;
	}

	public Integer getAlldaydatacount() {
		return alldaydatacount;
	}

	public void setAlldaydatacount(Integer alldaydatacount) {
		this.alldaydatacount = alldaydatacount;
	}

	public Integer getAlldaydatabackupcount() {
		return alldaydatabackupcount;
	}

	public void setAlldaydatabackupcount(Integer alldaydatabackupcount) {
		this.alldaydatabackupcount = alldaydatabackupcount;
	}

	public Integer getAlldaybalanccount() {
		return alldaybalanccount;
	}

	public void setAlldaybalanccount(Integer alldaybalanccount) {
		this.alldaybalanccount = alldaybalanccount;
	}

	public Integer getAlldayVPNcount() {
		return alldayVPNcount;
	}

	public void setAlldayVPNcount(Integer alldayVPNcount) {
		this.alldayVPNcount = alldayVPNcount;
	}

	

	public Integer[] getAlldaycpu() {
		return alldaycpu;
	}

	public void setAlldaycpu(Integer[] alldaycpu) {
		this.alldaycpu = alldaycpu;
	}

	public Integer[] getAlldaymemory() {
		return alldaymemory;
	}

	public void setAlldaymemory(Integer[] alldaymemory) {
		this.alldaymemory = alldaymemory;
	}

	public Integer[] getAlldaydisk() {
		return alldaydisk;
	}

	public void setAlldaydisk(Integer[] alldaydisk) {
		this.alldaydisk = alldaydisk;
	}

	public Integer[] getAlldayip() {
		return alldayip;
	}

	public void setAlldayip(Integer[] alldayip) {
		this.alldayip = alldayip;
	}

	public Integer[] getAlldayobject() {
		return alldayobject;
	}

	public void setAlldayobject(Integer[] alldayobject) {
		this.alldayobject = alldayobject;
	}

	public Integer getAlldaymysqlcount() {
		return alldaymysqlcount;
	}

	public void setAlldaymysqlcount(Integer alldaymysqlcount) {
		this.alldaymysqlcount = alldaymysqlcount;
	}

	public String getOnedayfileidmoney() {
		return onedayfileidmoney;
	}

	public void setOnedayfileidmoney(String onedayfileidmoney) {
		this.onedayfileidmoney = onedayfileidmoney;
	}

	public String getOnedayfileidorder() {
		return onedayfileidorder;
	}

	public void setOnedayfileidorder(String onedayfileidorder) {
		this.onedayfileidorder = onedayfileidorder;
	}

	public String getAlldayfileidmoney() {
		return alldayfileidmoney;
	}

	public void setAlldayfileidmoney(String alldayfileidmoney) {
		this.alldayfileidmoney = alldayfileidmoney;
	}

	public String getAlldayfileidorder() {
		return alldayfileidorder;
	}

	public void setAlldayfileidorder(String alldayfileidorder) {
		this.alldayfileidorder = alldayfileidorder;
	}

	public String getDayTime() {
		return DayTime;
	}

	public void setDayTime(String dayTime) {
		DayTime = dayTime;
	}

	
	
	
	
	
	
	
	
	
	
	
	
}
