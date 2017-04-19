package com.eayun.physical.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * DcServer entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name = "dc_server")
public class BaseDcServer implements java.io.Serializable {

    // Fields

    private static final long serialVersionUID = 130007972579866142L;

    private String            id;                                    //随机生成id，主键
    private String            name;                                  //物理服务器名称
    private String            serverUses;                            //物理服务器用途
    private String            serverInnetIp;                         //物理服务器内网ip
    private String            cabinetId;                             //机柜id，所属机柜
    private String            datacenterId;                          //数据中心id
    private String            cpu;                                   //cpu规格
    private Double            memory;                                //内存规格,值
    private Double            diskCapacity;                          //硬盘容量
    private Double            spec;                                  //规格，单位：U
    private String            memo;                                  //用途说明
    private String            isMonitor;                             //是否启用监控，1为启用监控，0为不启用监控
    private String            memoryUnit;                            //内存规格单位，例：GB、MB
    private String            diskUnit;                              //硬盘规格单位，例：GB、TB
    private String            responPerson;                          //责任人
    private String            serverModelId;                         //物理服务器型号ID
    private String            responPersonMobile;                    //责任人电话
    private String            serverId;                              //物理服务器id
    private String            isComputenode;                         //是否为计算节点，不可新增删除(0:计算节点 1:非计算节点)
    private String            serverOutnetIp;                        //物理服务器外网ip
    private String            creUser;                               //创建人
    private Timestamp         creDate;                               //创建时间
    private String            nodeId;                                //计算节点id

    //-------------end-------------
    // Constructors

    /** default constructor */
    public BaseDcServer() {
    }

    /** minimal constructor */
    public BaseDcServer(String id) {
        this.id = id;
    }

    // Property accessors
    @Id
    @Column(name = "id", unique = true, nullable = false, length = 50)
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "name", length = 50)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "server_uses", length = 50)
    public String getServerUses() {
        return this.serverUses;
    }

    public void setServerUses(String serverUses) {
        this.serverUses = serverUses;
    }

    @Column(name = "server_innet_ip", length = 50)
    public String getServerInnetIp() {
        return this.serverInnetIp;
    }

    public void setServerInnetIp(String serverInnetIp) {
        this.serverInnetIp = serverInnetIp;
    }

    @Column(name = "cabinet_id", length = 50)
    public String getCabinetId() {
        return this.cabinetId;
    }

    public void setCabinetId(String cabinetId) {
        this.cabinetId = cabinetId;
    }

    @Column(name = "datacenter_id", length = 50)
    public String getDatacenterId() {
        return this.datacenterId;
    }

    public void setDatacenterId(String datacenterId) {
        this.datacenterId = datacenterId;
    }

    @Column(name = "cpu", length = 50)
    public String getCpu() {
        return this.cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    @Column(name = "memory", precision = 0)
    public Double getMemory() {
        return this.memory;
    }

    public void setMemory(Double memory) {
        this.memory = memory;
    }

    @Column(name = "disk_capacity", precision = 0)
    public Double getDiskCapacity() {
        return this.diskCapacity;
    }

    public void setDiskCapacity(Double diskCapacity) {
        this.diskCapacity = diskCapacity;
    }

    @Column(name = "spec", precision = 0)
    public Double getSpec() {
        return this.spec;
    }

    public void setSpec(Double spec) {
        this.spec = spec;
    }

    @Column(name = "memo", length = 500)
    public String getMemo() {
        return this.memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    @Column(name = "is_monitor", length = 1)
    public String getIsMonitor() {
        return this.isMonitor;
    }

    public void setIsMonitor(String isMonitor) {
        this.isMonitor = isMonitor;
    }

    @Column(name = "memory_unit", length = 50)
    public String getMemoryUnit() {
        return this.memoryUnit;
    }

    public void setMemoryUnit(String memoryUnit) {
        this.memoryUnit = memoryUnit;
    }

    @Column(name = "disk_unit", length = 50)
    public String getDiskUnit() {
        return this.diskUnit;
    }

    public void setDiskUnit(String diskUnit) {
        this.diskUnit = diskUnit;
    }

    @Column(name = "respon_person", length = 50)
    public String getResponPerson() {
        return this.responPerson;
    }

    public void setResponPerson(String responPerson) {
        this.responPerson = responPerson;
    }

    @Column(name = "server_model_id", length = 50)
    public String getServerModelId() {
        return this.serverModelId;
    }

    public void setServerModelId(String serverModelId) {
        this.serverModelId = serverModelId;
    }

    @Column(name = "respon_person_mobile", length = 50)
    public String getResponPersonMobile() {
        return this.responPersonMobile;
    }

    public void setResponPersonMobile(String responPersonMobile) {
        this.responPersonMobile = responPersonMobile;
    }

    @Column(name = "server_id", length = 50)
    public String getServerId() {
        return this.serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    @Column(name = "is_computenode", length = 1)
    public String getIsComputenode() {
        return this.isComputenode;
    }

    public void setIsComputenode(String isComputenode) {
        this.isComputenode = isComputenode;
    }

    @Column(name = "server_outnet_ip", length = 50)
    public String getServerOutnetIp() {
        return this.serverOutnetIp;
    }

    public void setServerOutnetIp(String serverOutnetIp) {
        this.serverOutnetIp = serverOutnetIp;
    }

    @Column(name = "cre_user", length = 50)
    public String getCreUser() {
        return this.creUser;
    }

    public void setCreUser(String creUser) {
        this.creUser = creUser;
    }

    @Column(name = "cre_date", length = 11)
    public Timestamp getCreDate() {
        return this.creDate;
    }

    public void setCreDate(Timestamp creDate) {
        this.creDate = creDate;
    }

    @Column(name = "node_id", length = 50)
    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

}