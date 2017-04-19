package com.eayun.database.configgroup.model.configfile;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by Administrator on 2017/2/21.
 */

/**
 * 配置文件基本信息【包含了默认配置文件以及客户配置文件两种】
 */
@Entity
@Table(name = "cloud_rdsconfigfile")
public class CloudRdsconfigfile implements Serializable {

    @Id
    @Column(name = "config_id", unique = true, nullable = false, length = 36)
    private String configId ;  // 数据库ID编号 == 配置组ID编号，调用底层API接口生成

    @Column(name = "config_version", length = 10)
    private String configVersion;   //数据库Datastore的version id

    @Column(name = "config_name", length = 20)
    private String configName;  //配置文件的名称

    @Column(name = "config_type", length = 1)
    private String configType;  //配置文件的类型 0:历史默认配置文件 1:最新默认配置文件 2:客户自建配置文件

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "config_date")
    private Date configDate;    //配置文件创建时间

    @Column(name = "config_datacenterid", length = 13)
    private String configDatacenterid;  //所位于数据中心ID

    @Column(name = "config_projectid", length = 32)
    private String configProjectid;     //所属项目ID

    @Column(name = "is_defaultlatest", length = 1)
    private String isDefaultlatest;     //是否为最新版本默认配置文件的标识

    @Column(name = "config_versionname", length = 36)
    private String configVersionname ;

    public String getConfigVersion() {
        return configVersion;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigVersion(String configVersion) {
        this.configVersion = configVersion;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public Date getConfigDate() {
        return configDate;
    }

    public void setConfigDate(Date configDate) {
        this.configDate = configDate;
    }

    public String getConfigDatacenterid() {
        return configDatacenterid;
    }

    public void setConfigDatacenterid(String configDatacenterid) {
        this.configDatacenterid = configDatacenterid;
    }

    public String getConfigProjectid() {
        return configProjectid;
    }

    public void setConfigProjectid(String configProjectid) {
        this.configProjectid = configProjectid;
    }

    public String getIsDefaultlatest() {
        return isDefaultlatest;
    }

    public void setIsDefaultlatest(String isDefaultlatest) {
        this.isDefaultlatest = isDefaultlatest;
    }

    public String getConfigVersionname() {
        return configVersionname;
    }

    public void setConfigVersionname(String configVersionname) {
        this.configVersionname = configVersionname;
    }
}
