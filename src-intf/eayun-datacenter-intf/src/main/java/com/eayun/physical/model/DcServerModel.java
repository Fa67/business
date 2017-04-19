package com.eayun.physical.model;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * DcServerModel entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name = "dc_server_model")
public class DcServerModel  implements
		java.io.Serializable {

	// Fields

	/**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 6568399523409880497L;
    private String id;  //id
	private String name;//服务器型号名称
	private String cpu;//
	private String memory;//内存配额
	private String disk;//硬盘配额
	private String spec;//规格
	private String processor;//处理器
	private String creUser;//创建人
	private Date creDate;//创建时间

	// Constructors

	/** default constructor */
	public DcServerModel() {
	}

	/** minimal constructor */
	public DcServerModel(String id) {
		this.id = id;
	}

	/** full constructor */
	public DcServerModel(String id, String name, String cpu, String memory,
			String disk, String spec, String processor, String creUser,
			Timestamp creDate) {
		this.id = id;
		this.name = name;
		this.cpu = cpu;
		this.memory = memory;
		this.disk = disk;
		this.spec = spec;
		this.processor = processor;
		this.creUser = creUser;
		this.creDate = creDate;
	}

	// Property accessors
	@Id
	@Column(name = "ID", unique = true, nullable = false, length = 50)
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "NAME", length = 50)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "CPU", length = 50)
	public String getCpu() {
		return this.cpu;
	}

	public void setCpu(String cpu) {
		this.cpu = cpu;
	}

	@Column(name = "MEMORY", length = 50)
	public String getMemory() {
		return this.memory;
	}

	public void setMemory(String memory) {
		this.memory = memory;
	}

	@Column(name = "DISK", length = 50)
	public String getDisk() {
		return this.disk;
	}

	public void setDisk(String disk) {
		this.disk = disk;
	}

	@Column(name = "SPEC", length = 50)
	public String getSpec() {
		return this.spec;
	}

	public void setSpec(String spec) {
		this.spec = spec;
	}

	@Column(name = "PROCESSOR", length = 100)
	public String getProcessor() {
		return this.processor;
	}

	public void setProcessor(String processor) {
		this.processor = processor;
	}

	@Column(name = "CRE_USER", length = 50)
	public String getCreUser() {
		return this.creUser;
	}

	public void setCreUser(String creUser) {
		this.creUser = creUser;
	}

	@Column(name = "CRE_DATE", length = 7)
	public Date getCreDate() {
		return this.creDate;
	}

	public void setCreDate(Date creDate) {
		this.creDate = creDate;
	}

}