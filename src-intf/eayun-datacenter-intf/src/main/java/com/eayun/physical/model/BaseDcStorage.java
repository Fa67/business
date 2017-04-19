package com.eayun.physical.model;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "dc_storage")
public class BaseDcStorage implements Serializable {

    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -8636344680205316887L;

    @GenericGenerator(name="generator", strategy="uuid.hex")
    @Id @GeneratedValue(generator="generator")
    @Column(name="id", unique=true, nullable=false, length=50)
	private String id;//随机生成 记录id
	
	@Column(name="name", length=50)
	private String name;//存储名称
	
	@Column(name="storage_model")
	private String storageModel;//存储型号
	
	@Column(name="storage_value")
	private String storageValue;//存储容量
	
	@Column(name="storage_unit", length=50)
	private String storageUnit;//存储容量单位
	
	@Column(name="data_rate")
	private String dataRate;//数据传输率
	
	@Column(name="data_center_id", length=50)
	private String dataCenterId; //数据中心ID
	
	@Column(name="cabinet_id",length=50)
	private String cabinetId;//机柜ID
	
	@Column(name="respon_person",length=50)
	private String responPerson;//责任人
	
	@Column(name="respon_person_mobile",length=50)
	private String responPersonMobile;//责任人联系电话
	
	@Column(name="cache",length=50)
	private String cache;//高速缓存(M)
	
	@Column(name="raid_support",length=50)
	private String raidSupport;//raid支持
	
	@Column(name="cre_user",length=50)
	private String creUser;//创建人ID
	
	@Column(name="cre_date",length=6)
	private String creDate;//创建时间
	
	@Column(name="spec")
	private Integer spec;//规格
	
	@Column(name="memo", length=1000)
	private String memo ;//描述
	
	@Column(name="storage_id",length=50)
	private String storage_id ;//存储id
	
	public BaseDcStorage() {
	}
	
	public BaseDcStorage(String id, String name, String storageModel,
			String storageValue, String storageUnit, String dataRate,
			String dataCenterId, String cabinetId, String responPerson,
			String responPersonMobile, String cache, String raidSupport,
			String creUser, String creDate, Integer spec, String memo,String storage_id) {
		super();
		this.id = id;
		this.name = name;
		this.storageModel = storageModel;
		this.storageValue = storageValue;
		this.storageUnit = storageUnit;
		this.dataRate = dataRate;
		this.dataCenterId = dataCenterId;
		this.cabinetId = cabinetId;
		this.responPerson = responPerson;
		this.responPersonMobile = responPersonMobile;
		this.cache = cache;
		this.raidSupport = raidSupport;
		this.creUser = creUser;
		this.creDate = creDate;
		this.spec = spec;
		this.memo = memo;
		this.storage_id = storage_id;
	}




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

	public String getStorageModel() {
		return storageModel;
	}

	public void setStorageModel(String storageModel) {
		this.storageModel = storageModel;
	}

	public String getStorageValue() {
		return storageValue;
	}

	public void setStorageValue(String storageValue) {
		this.storageValue = storageValue;
	}

	public String getStorageUnit() {
		return storageUnit;
	}

	public void setStorageUnit(String storageUnit) {
		this.storageUnit = storageUnit;
	}

	public String getDataRate() {
		return dataRate;
	}

	public void setDataRate(String dataRate) {
		this.dataRate = dataRate;
	}

	public String getDataCenterId() {
		return dataCenterId;
	}

	public void setDataCenterId(String dataCenterId) {
		this.dataCenterId = dataCenterId;
	}

	public String getCabinetId() {
		return cabinetId;
	}

	public void setCabinetId(String cabinetId) {
		this.cabinetId = cabinetId;
	}

	public String getResponPerson() {
		return responPerson;
	}

	public void setResponPerson(String responPerson) {
		this.responPerson = responPerson;
	}

	public String getResponPersonMobile() {
		return responPersonMobile;
	}

	public void setResponPersonMobile(String responPersonMobile) {
		this.responPersonMobile = responPersonMobile;
	}

	public String getCache() {
		return cache;
	}

	public void setCache(String cache) {
		this.cache = cache;
	}

	public String getRaidSupport() {
		return raidSupport;
	}

	public void setRaidSupport(String raidSupport) {
		this.raidSupport = raidSupport;
	}

	public String getCreUser() {
		return creUser;
	}

	public void setCreUser(String creUser) {
		this.creUser = creUser;
	}

	public String getCreDate() {
		return creDate;
	}

	public void setCreDate(String creDate) {
		this.creDate = creDate;
	}

	public Integer getSpec() {
		return spec;
	}

	public void setSpec(Integer spec) {
		this.spec = spec;
	}
	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}
	
	public String getStorage_id() {
		return storage_id;
	}

	public void setStorage_id(String storage_id) {
		this.storage_id = storage_id;
	}
}
