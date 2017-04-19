package com.eayun.monitor.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

/**
 * 运维报警规则
 *                       
 * @Filename: BaseEcmcAlarmrule.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2016年3月24日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "ecmc_alarmrule")
public class BaseEcmcAlarmRule implements Serializable {

	/**
	 *Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 7693846079931981021L;

	@Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "ar_id", unique = true, nullable = false, length = 32)
    private String            id;

	@Column(name = "ar_name", length = 64)
    private String            name;			//规则名称

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = "ar_mtime")
    private Date              modifyTime;	//最后修改时间

	@Column(name = "ar_monitoritem", length = 32)
    private String            monitorItem;	//监控项（nodeId）

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	
	public String getMonitorItem() {
		return monitorItem;
	}

	public void setMonitorItem(String monitorItem) {
		this.monitorItem = monitorItem;
	}
}
