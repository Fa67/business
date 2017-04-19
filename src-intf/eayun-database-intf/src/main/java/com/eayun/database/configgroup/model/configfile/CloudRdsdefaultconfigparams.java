package com.eayun.database.configgroup.model.configfile;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Administrator on 2017/2/21.
 */
@Entity
@Table(name = "cloud_rdsdefaultconfigparams")
public class CloudRdsdefaultconfigparams implements Serializable {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "id", unique = true, nullable = false, length = 32)
    private String id ;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "type", length = 10)
    private String type;

    @Column(name = "min_size", length = 50)
    private String minSize;

    @Column(name = "max_size", length = 50)
    private String maxSize;

    @Column(name = "restart", length = 1)
    private String restart;

    @Column(name = "override", length = 1)
    private String override;

    @Column(name = "default_value", length = 50)
    private String defaultValue;

    @Column(name = "version", length = 32)
    private String version;

    @Column(name = "is_static", length = 1)
    private String isStatic ;

    @Column(name = "option_values", length = 100)
    private String optionValues ;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMinSize() {
        return minSize;
    }

    public void setMinSize(String minSize) {
        this.minSize = minSize;
    }

    public String getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(String maxSize) {
        this.maxSize = maxSize;
    }

    public String getRestart() {
        return restart;
    }

    public void setRestart(String restart) {
        this.restart = restart;
    }

    public String getOverride() {
        return override;
    }

    public void setOverride(String override) {
        this.override = override;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getIsStatic() {
        return isStatic;
    }

    public void setIsStatic(String isStatic) {
        this.isStatic = isStatic;
    }

    public String getOptionValues() {
        return optionValues;
    }

    public void setOptionValues(String optionValues) {
        this.optionValues = optionValues;
    }
}