package com.eayun.cdn.bean;

import java.io.Serializable;

/**
 * @Filename: DNSRecord.java
 * @Description: DNS记录实体类
 * @Version: 1.0
 * @Author: fan.zhang
 * @Email: fan.zhang@eayun.com
 * @History:<br>
 * <li>Date: 2016年6月17日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
public class DNSRecord implements Serializable{
    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 2052607020183263777L;
    private String recordId;	//添加记录之后得到的id
    private String domainName;	//域名名称
    private String subDomain;	//必填，主机记录，如果要解析@.exmaple.com，主机记录要填写"@”，而不是空
    private String recordType;	//必选，"A", "CNAME", "MX", "TXT", "NS", "AAAA", "SRV", "显性URL", "隐性URL"
    private String recordLine;	//必选，默认"default"。"默认", "国内", "国外", "电信", "联通", "教育网",  "移动", "百度", "谷歌", "搜搜", "有道", "必应", "搜狗", "奇虎", "搜索引擎"
    private String value;		//必选，IP:200.200.200.200, CNAME: cname.dnspod.com., MX: mail.dnspod.com.
    private int Priority;		//MX记录的优先级，取值范围[1,10]，记录类型为MX记录时，此参数必须
    private long ttl;			//生存时间，默认为600秒（10分钟）
    
    
    /*原DNSPod参数*/
    private int weight;		//可选，权重信息0-100的整数，仅企业 VIP 域名可用，0 表示关闭，留空或者不传该参数，表示不设置权重信息
    private String domainId;
    private String status;	//可选，记录初始状态，默认为”enable”，如果传入”disable”，解析不会生效，也不会验证负载均衡的限制
	public String getRecordId() {
		return recordId;
	}
	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}
	public String getDomainName() {
		return domainName;
	}
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	public String getSubDomain() {
		return subDomain;
	}
	public void setSubDomain(String subDomain) {
		this.subDomain = subDomain;
	}
	public String getRecordType() {
		return recordType;
	}
	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}
	public String getRecordLine() {
		return recordLine;
	}
	public void setRecordLine(String recordLine) {
		this.recordLine = recordLine;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getPriority() {
		return Priority;
	}
	public void setPriority(int priority) {
		Priority = priority;
	}
	public long getTtl() {
		return ttl;
	}
	public void setTtl(long ttl) {
		this.ttl = ttl;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	public String getDomainId() {
		return domainId;
	}
	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
    
    
}