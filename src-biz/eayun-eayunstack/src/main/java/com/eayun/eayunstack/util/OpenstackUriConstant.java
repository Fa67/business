package com.eayun.eayunstack.util;

public class OpenstackUriConstant {

	// openstcak各服务入口的名称
	public static final String IDENTITY_SERVICE_URI = "identity";
	public static final String COMPUTE_SERVICE_URI = "compute";
	public static final String NETWORK_SERVICE_URI = "network";
	public static final String IMAGE_SERVICE_URI = "image";
	public static final String VOLUME_SERVICE_URI = "volumev2";
	public static final String METER_SERVICE_URI = "metering";
	public static final String TROVE_SERVICE_URI = "database";
	public static final String SWIFT_SERVICE_URI = "object-store";
	
	public static final String USER_URI = "/users";
	public static final String ROOT_URI = "/root";
	public static final String USER_DATA_NAME = "user";
	public static final String USER_DATA_NAMES = "users";

	// openstack项目（租户）API对应的URI
	public static final String TENANT_URI = "/tenants";
	public static final String TENANT_DATA_NAMES = "tenants";
	public static final String TENANT_DATA_NAME = "tenant";
	// 计算节点API的URI
	public static final String HYPERVISOR_URI = "/os-hypervisors/detail";
	public static final String HYPERVISOR_DATA_NAMES = "hypervisors";
	public static final String HYPERVISOR_DATA_NAME = "hypervisor";

	// 云主机相关API的URI
	public static final String VM_URI = "/servers";
	public static final String VM_BOOT_URI = "/os-volumes_boot";//从镜像启动（创建一个新卷）
	public static final String VM_DETAIL_URI = "/servers/detail";
	public static final String VM_DATA_NAMES = "servers";
	public static final String VM_DATA_NAME = "server";
	
	//云主机端口相关API
	public static final String OS_INTERFACE_URI = "/os-interface";
	public static final String OS_INTERFACE_NAME = "interfaceAttachment";
	
	// 云网络相关API的URI
	// /v2.0/networks
	public static final String NETWORK_URI = "/v2.0/networks";
	public static final String NETWORK_DATA_NAMES = "networks";
	public static final String NETWORK_DATA_NAME = "network";

	// 云网络相关API的URI
	// /v2.0/networks
	public static final String SUBNETWORK_URI = "v2.0/subnets";
	public static final String SUBNETWORK_DATA_NAMES = "subnets";
	public static final String SUBNETWORK_DATA_NAME = "subnet";
	
	// 端口映射相关API的URI
	// /v2.0/portmappings
	public static final String PORTMAPPING_URI = "v2.0/portmappings";
	public static final String PORTMAPPING_DATA_NAMES = "portmappings";
	public static final String PORTMAPPING_DATA_NAME = "portmapping";

	// 路由相关API的URI
	// /v2.0/networks
	public static final String ROUTE_URI = "v2.0/routers";
	public static final String ROUTE_DATA_NAMES = "routers";
	public static final String ROUTE_DATA_NAME = "router";
	
	public static final String PORT_URI = "v2.0/ports";
	public static final String PORT_DATA_NAMES = "ports";
	public static final String PORT_DATA_NAME = "port";

	
	//路由带宽相关的URI
	public static final String ROUTE_BANDWIDTH_URI = "v2.0/eayun_qos/qoss";
	public static final String ROUTE_BANDWIDTH_DATA_NAME = "qos";
	// flavor相关API的URI
	public static final String FLAVOR_URI = "/flavors";
	
	public static final String QUERY_SAMPLES_URI = "/v2/query/samples";

	// 云硬盘相关API的URI
	public static final String DISK_URI = "/volumes/detail";
	public static final String DISK_DATA_NAMES = "volumes";
	public static final String DISK_DATA_NAME = "volume";

	// 镜像相关API的URI
	public static final String IMAGE_URI = "/v2.0/images";
	public static final String NOVA_IMAGE_URI = "/images";
	public static final String IMAGE_DATA_NAMES = "images";
	public static final String IMAGE_DATA_NAME = "image";

	//ceilometer相关服务API的URI
    public static final String CEILOMETER_URI="metering";
    public static final String METERING_LABELS_CREATE="/v2.0/metering/metering-labels";
    public static final String METERING_LABELS_DATANAME="metering_label";
	public static final String METERING_LABELS_RULE_CREATE="/v2.0/metering/metering-label-rules";
	public static final String METERING_LABELS_RULE_DATANAME="metering_label_rule";
		
	// 安全组相关API的URI
	// public static final String SECRURITY_GROUP_URI="v2.0/vms";
	public static final String SECURITY_GROUP_URI = "v2.0/security-groups";
	public static final String SECURITY_GROUP_URI_NAMES = "security_groups";
	public static final String SECURITY_GROUP_URI_NAME = "security_group";

	// 安全组规则相关API的URI
	public static final String SECURITY_GROUP_RULES_URI = "v2.0/security-group-rules";
	public static final String SECURITY_GROUP_RULES_NAMES = "security-group-rules";
	public static final String SECURITY_GROUP_RULES_NAME = "security_group_rule";

	// 防火墙相关API的URI
	public static final String FIREWALL_URI = "v2.0/fw/firewalls";
	public static final String FIREWALL_DATA_NAMES = "firewalls";
	public static final String FIREWALL_DATA_NAME = "firewall";
	// 防火墙策略相关API的URI
	public static final String FIREWALL_POLICY_URI = "v2.0/fw/firewall_policies";
	public static final String FIREWALL_POLICY_DATA_NAMES = "firewall_policies";
	public static final String FIREWALL_POLICY_DATA_NAME = "firewall_policy";
	// 防火墙规则相关API的URI
	public static final String FIREWALL_RULE_URI = "v2.0/fw/firewall_rules";
	public static final String FIREWALL_RULE_DATA_NAMES = "firewall_rules";
	public static final String FIREWALL_RULE_DATA_NAME = "firewall_rule";
	// 浮动IP相关API的URL
	public static final String FLOATIP_URL = "/os-floating-ips";
	public static final String FLOATIP_DATA_NAMES = "floating_ips";
	public static final String FLOATIP_DATA_NAME = "floating_ip";

	// 安全组相关API的URI
	// public static final String NETWORK_URI="v2.0/vms";

	// 路由相关API的URI
	public static final String ROUTER_URI = "v2.0/vms";

	// 负载均衡相关API的URI
	public static final String LOADBALANCE_URI = "v2.0/loadbalances";
	public static final String LOADBALANCE_DATA_NAMES = "loadbalances";
	public static final String LOADBALANCE_DATA_NAME = "loadbalance";

	public static final String LISTENER_URI = "v2.0/lb/listeners";
	public static final String LISTENER_DATA_NAMES = "listeners";
	public static final String LISTENER_DATA_NAME = "listener";

	public static final String POOL_URI = "v2.0/lb/pools";
	public static final String POOL_DATA_NAMES = "pools";
	public static final String POOL_DATA_NAME = "pool";

	public static final String MEMBER_URI = "v2.0/lb/members";
	public static final String MEMBER_DATA_NAMES = "members";
	public static final String MEMBER_DATA_NAME = "member";

	public static final String HEALTH_MONITOR_URI = "v2.0/lb/health_monitors";
	public static final String HEALTH_MONITOR_DATA_NAMES = "health_monitors";
	public static final String HEALTH_MONITOR_DATA_NAME = "health_monitor";

	public static final String VIP_URI = "v2.0/lb/vips";
	public static final String VIP_DATA_NAMES = "vips";
	public static final String VIP_DATA_NAME = "vip";
	
	public static final String SNAPSHOT_DATA_NAME = "snapshot";

	// vpn相关API的URI
	public static final String VPN_URI = "/v2.0/vpn/vpnservices";
	public static final String VPN_DATA_NAME = "vpnservice";
	public static final String VPN_DATA_NAMES = "vpnservices";
	
	public static final String IKE_URI = "/v2.0/vpn/ikepolicies";
	public static final String IKE_DATA_NAME = "ikepolicy";
	public static final String IKE_DATA_NAMES = "ikepolicys";
	
	public static final String IPSEC_URI = "/v2.0/vpn/ipsecpolicies";
	public static final String IPSEC_DATA_NAME = "ipsecpolicy";
	public static final String IPSEC_DATA_NAMES = "ipsecpolicys";
	
	public static final String IPSEC_CONNECTION_URI = "/v2.0/vpn/ipsec-site-connections";
	public static final String IPSEC_CONNECTION_DATA_NAME = "ipsec_site_connection";
	public static final String IPSEC_CONNECTION_DATA_NAMES = "ipsec_site_connections";

	// token url类型
	public static final String ADMIN_URL_TYPE = "adminURL";
	public static final String PUBLIC_URL_TYPE = "publicURL";
	public static final String INTERNAL_URL_TYPE = "internalURL";
	
	//eayun_qos API的URI
	public static final String EAYUN_QOS_URI = "v2.0/eayun_qos/qoss";
	public static final String EAYUN_QOS_QUEUE_URI = "v2.0/eayun_qos/qos-queues";
	public static final String EAYUN_QOS_FILTER_URI = "v2.0/eayun_qos/qos-filters";
	public static final String EAYUN_QOS_DATA_NAME = "qos";
	public static final String EAYUN_QOS_QUEUE_DATA_NAME = "qos_queue";
	public static final String EAYUN_QOS_FILTER_DATA_NAME = "qos_filter";

	// RDS 相关API的URI
	public static final String RDS_DATABASE_URI = "/databases";
    public static final String RDS_DATABASE_DATA_NAME = "database";
    public static final String RDS_DATABASE_DATA_NAMES = "databases";
    public static final String RDS_USER_DATA_NAME = "user";
	public static final String RDS_BACKUPS_URI = "/backups";
	public static final String RDS_INSTANCES_URI = "/instances";
	public static final String RDS_DATA_NAMES = "instances";
	public static final String RDS_DATA_NAME = "instance";
	public static final String RDS_DATASTORE_NAMES = "datastores" ;
	public static final String RDS_DATASTORES_URI = "/datastores" ;
	public static final String RDS_LOG_NAMES = "logs" ;
	// 数据库中各种云资源对应的类型字段的值
	public static final String PROJECT_TYPE = "project";
	public static final String PRIVATE_IMAGE_TYPE = "private_image";
	public static final String PUBLIC_IMAGE_TYPE = "public_image";
	public static final String HOST_TYPE = "host";
	public static final String DISK_TYPE = "disk";
	public static final String DISK_SNAPSHOT_TYPE = "disk_snapshot";
	public static final String SECURITY_GROUP_TYPE = "security_group";
	public static final String SECURITY_GROUP_RULE_TYPE = "security_group_rule";
	public static final String FIREWALL_TYPE = "firewall";
	public static final String FIREWALL_POLICY_TYPE = "firewall_policy";
	public static final String FIREWALL_RULE_TYPE = "firewall_rule";
	public static final String OUT_NET_TYPE = "out_net";
	public static final String NET_TYPE = "net";
	public static final String SUBNET_TYPE = "subnet";
	public static final String ROUTE_TYPE = "route";
	public static final String LB_LOADBALANCE_TYPE = "lb_loadbalance";
	public static final String LB_POOL_TYPE = "lb_pool";
	public static final String LB_MEMBER_TYPE = "lb_member";
	public static final String LB_HEALTH_MONITOR_TYPE = "lb_health_monitor";
	public static final String LB_LISTENER_TYPE = "lb_listener";
	public static final String LB_VIP_TYPE = "lb_vip";
	public static final String FLOATIP = "floatIp";
	
	//SSH密钥
    public static final String SSH_SECRETKEY_URL = "/os-keypairs";
    public static final String SSH_SECRETKEY_DATA_NAME = "keypairs";
    public static final String SSH_SECRETKEY_DETAILS_NAME = "keypair";
    public static final String SSH_SECRETKEY_BIND_URL = "/eayun-userdata";
    public static final String SSH_SECREKEY_USERDATA_NAME = "user_data";
    public static final String SSH_METADATA_URL = "/metadata";
    public static final String SSH_METADATA_DATA_NAME = "metadata";
    public static final String SSH_METADATA_KEY = "/reset_sshkey";
}
