package com.eayun.monitor.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "ecmc_contactgroup")
public class BaseEcmcContactGroup implements Serializable {

    private static final long serialVersionUID = -1832712846906271219L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "mg_id", unique = true, nullable = false, length = 32)
    private String            id;
    @Column(name = "mg_name", length = 64)
    private String            name;

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
    

}
