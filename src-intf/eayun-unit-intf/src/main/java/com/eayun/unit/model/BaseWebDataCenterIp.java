package com.eayun.unit.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="unit_web_datacenter")
public class BaseWebDataCenterIp implements Serializable,Cloneable{
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name="id",length=32,unique=true,nullable=false)
	private String id;
	
	@Column(name="web_id",length=32)
	private String webId;
	
	@Column(name="dc_id",length=32)
	private String dcId;
	
	@Column(name="webservice",length=32)
	private String webService;
	
	
	@Column(name="ip",length=32)
	private String ip;


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getWebId() {
		return webId;
	}


	public void setWebId(String webId) {
		this.webId = webId;
	}


	public String getDcId() {
		return dcId;
	}


	public void setDcId(String dcId) {
		this.dcId = dcId;
	}


	public String getWebService() {
		return webService;
	}


	public void setWebService(String webService) {
		this.webService = webService;
	}


	public String getIp() {
		return ip;
	}


	public void setIp(String ip) {
		this.ip = ip;
	}
	
	@Override
    public Object clone() {
	    BaseWebDataCenterIp stu = null;  
        try{  
            stu = (BaseWebDataCenterIp)super.clone();  
        }catch(CloneNotSupportedException e) {  
            e.printStackTrace();  
        }  
        return stu; 
    }

}
