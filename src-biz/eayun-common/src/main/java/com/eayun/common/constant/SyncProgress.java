/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.common.constant;

/**
 *                       
 * @Filename: SyncProgress.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2017年3月28日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class SyncProgress {

    public interface SyncByProjectTypes {
        String VM       = "vm";
        String DISK     = "disk";
        String FLOAT_IP = "floatip";
        String RDS      = "rds";
        String RDS_BAK  = "rdsbak";
    }

    public interface SyncByProjectNames {
        String VM       = "云主机";
        String DISK     = "云硬盘";
        String FLOAT_IP = "弹性公网IP";
        String RDS      = "RDS";
        String RDS_BAK  = "RDS备份";
    }

    public interface SyncByDatacenterTypes {
        String PROJECT             = "project";
        String NET                 = "net";
        String SECURITY_GROUP      = "securitygroup";
        String SECURITY_GROUP_RULE = "securitygrouprule";
        String IMAGE               = "image";
        String VOLUME_TYPE         = "volumetype";
        String COMPUTE_NODE        = "computenode";
        String FIREWALL            = "firewall";
        String FIREWALL_POLICY     = "firewallpolicy";
        String FIREWALL_RULE       = "firewallrule";
        String FLAVOR              = "flavor";
        String SUBNET              = "subnet";
        String ROUTER              = "router";
        String LB_MEMBER           = "lbmember";
        String LB_POOL             = "lbpool";
        String LB_MONITOR          = "lbmonitor";
        String LB_VIP              = "lbvip";
        String DISK_SNAPSHOT       = "disksnapshot";
        String LOAD_BALANCER       = "loadbalancer";
        String OUT_IP              = "outip";
        String VPN                 = "vpn";
        String PORT_MAPPING        = "portmapping";
    }

    public interface SyncByDatacenterNames {
        String PROJECT             = "项目";
        String NET                 = "网络";
        String SECURITY_GROUP      = "安全组";
        String SECURITY_GROUP_RULE = "安全组规则";
        String IMAGE               = "镜像";
        String VOLUME_TYPE         = "云硬盘类型";
        String COMPUTE_NODE        = "计算节点";
        String FIREWALL            = "防火墙";
        String FIREWALL_POLICY     = "防火墙策略";
        String FIREWALL_RULE       = "防火墙规则";
        String FLAVOR              = "云主机类型";
        String SUBNET              = "子网";
        String ROUTER              = "路由";
        String LB_MEMBER           = "负载均衡成员";
        String LB_POOL             = "负载均衡资源池";
        String LB_MONITOR          = "负载均衡监控";
        String LB_VIP              = "负载均衡VIP";
        String DISK_SNAPSHOT       = "云硬盘备份";
        String LOAD_BALANCER       = "负载均衡-floatIP";
        String OUT_IP              = "OutIp";
        String VPN                 = "VPN";
        String PORT_MAPPING        = "端口映射";
    }

    public interface SyncType {
        String DATA_CENTER = "1";
        String PROJECT     = "2";
    }
}
