package com.eayun.virtualization.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;

import com.eayun.eayunstack.model.Flavor;
/**
 * 云主机类型                
 * @Filename: BaseCloudFlavor.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2015年11月11日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "cloud_flavor")
public class BaseCloudFlavor implements java.io.Serializable {
	private static final long serialVersionUID = -1116765119289877206L;

	private String id;         //主键id
	private String flavorId;   //模板id
	private String flavorName; //模板名称
	private int flavorVcpus;   //cpu核数
	private int flavorRam;     //内存大小
	private int flavorDisk;    //硬盘大小
	private String dcId;       //数据中心id
	private String prjId;      //项目id
	private String createName; //创建人姓名

	@Id
	@Column(name = "id", unique = true, nullable = false, length = 100)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "flavor_id", length = 100)
	public String getFlavorId() {
		return flavorId;
	}

	public void setFlavorId(String flavorId) {
		this.flavorId = flavorId;
	}

	@Column(name = "flavor_name", length = 100)
	public String getFlavorName() {
		return flavorName;
	}

	public void setFlavorName(String flavorName) {
		this.flavorName = flavorName;
	}

	@Column(name = "flavor_vcpus", length = 20)
	public int getFlavorVcpus() {
		return flavorVcpus;
	}

	public void setFlavorVcpus(int flavorVcpus) {
		this.flavorVcpus = flavorVcpus;
	}

	@Column(name = "flavor_ram", length = 20)
	public int getFlavorRam() {
		return flavorRam;
	}

	public void setFlavorRam(int flavorRam) {
		this.flavorRam = flavorRam;
	}

	@Column(name = "flavor_disk", length = 20)
	public int getFlavorDisk() {
		return flavorDisk;
	}

	public void setFlavorDisk(int flavorDisk) {
		this.flavorDisk = flavorDisk;
	}

	@Column(name = "dc_id", length = 100)
	public String getDcId() {
		return dcId;
	}

	public void setDcId(String dcId) {
		this.dcId = dcId;
	}

	@Column(name = "prj_id", length = 100)
	public String getPrjId() {
		return prjId;
	}

	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}

	@Column(name = "create_name", length = 100)
	public String getCreateName() {
		return createName;
	}

	public void setCreateName(String createName) {
		this.createName = createName;
	}

	public BaseCloudFlavor(String flavorId, String flavorName, int flavorVcpus, int flavorRam, int flavorDisk,
			String dcId, String prjId, String createName) {
		super();
		this.flavorId = flavorId;
		this.flavorName = flavorName;
		this.flavorVcpus = flavorVcpus;
		this.flavorRam = flavorRam;
		this.flavorDisk = flavorDisk;
		this.dcId = dcId;
		this.prjId = prjId;
		this.createName = createName;
	}

	public BaseCloudFlavor() {
		super();
	}

	public BaseCloudFlavor(String flavorId) {
		super();
		this.flavorId = flavorId;
	}

	public BaseCloudFlavor(Flavor flavor, String dcId) {
		if (null != flavor) {
			this.flavorId = flavor.getId();
			this.flavorName = flavor.getName();
			if (!StringUtils.isEmpty(flavor.getVcpus()))
				this.flavorVcpus = Integer.parseInt(flavor.getVcpus());
			if (!StringUtils.isEmpty(flavor.getRam()))
				this.flavorRam = Integer.parseInt(flavor.getRam());
			if (!StringUtils.isEmpty(flavor.getDisk()))
				this.flavorDisk = Integer.parseInt(flavor.getDisk());
			this.dcId = dcId;
		}
	}

}
