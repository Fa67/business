package com.eayun.database.configgroup.model.configfile;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by Administrator on 2017/2/21.
 */
@Entity
@Table(name = "cloud_rdsconfigparams")
public class CloudRdsconfigparams implements Serializable {

    @Id
    @Column(name = "config_id", unique = true, nullable = false, length = 36)
    private String configId ;    //配置组ID编号

    @Column(name = "config_content", length = 6000)
    private String configContent;   //配置参数具体内容，包含了参数所有基本属性的JSON字符串

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getConfigContent() {
        return configContent;
    }

    public void setConfigContent(String configContent) {
        this.configContent = configContent;
    }
}
