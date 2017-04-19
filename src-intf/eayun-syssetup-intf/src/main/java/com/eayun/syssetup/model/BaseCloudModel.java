package com.eayun.syssetup.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.eayun.base.BaseMessage;
/**            
 * @Filename: BaseCloudModel.java
 * @Description: 云主机型号实体类
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2015年9月22日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "cloud_model")
public class BaseCloudModel extends BaseMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3701952743579017440L;
	
	private String modelId;            //模板id
	
	private String modelName;          //模板名称
	
	private int modelVcpus;            //Cpu核数
	
	private int modelRam;              //内存大小
	
	private String modelCusid;         //用户id

	@Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "model_id", unique = true, nullable = false, length = 32)
	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	@Column(name = "model_name",length = 100)
	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	@Column(name = "model_vcpus", length = 20)
	public int getModelVcpus() {
		return modelVcpus;
	}

	public void setModelVcpus(int modelVcpus) {
		this.modelVcpus = modelVcpus;
	}

	@Column(name = "model_ram", length = 20)
	public int getModelRam() {
		return modelRam;
	}

	public void setModelRam(int modelRam) {
		this.modelRam = modelRam;
	}

	@Column(name = "model_cusid", length = 32)
	public String getModelCusid() {
		return modelCusid;
	}

	public void setModelCusid(String modelCusid) {
		this.modelCusid = modelCusid;
	}
	
}
