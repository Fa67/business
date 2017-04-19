package com.eayun.virtualization.ecmcservice.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.charge.model.ChargeRecord;
import com.eayun.common.constant.PayType;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.CloudResourceUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.eayunstack.model.VpnConnection;
import com.eayun.eayunstack.service.OpenstackVpnConnectionService;
import com.eayun.eayunstack.service.OpenstackVpnIkePolicyService;
import com.eayun.eayunstack.service.OpenstackVpnIpSecPolicyService;
import com.eayun.eayunstack.service.OpenstackVpnService;
import com.eayun.virtualization.baseservice.BaseVpnService;
import com.eayun.virtualization.dao.CloudVpnConnDao;
import com.eayun.virtualization.dao.CloudVpnDao;
import com.eayun.virtualization.dao.CloudVpnIkePolicyDao;
import com.eayun.virtualization.dao.CloudVpnIpSecPolicyDao;
import com.eayun.virtualization.ecmcservice.EcmcVpnService;
import com.eayun.virtualization.model.BaseCloudVpnConn;
import com.eayun.virtualization.model.CloudOrderVpn;
import com.eayun.virtualization.model.CloudVpn;

@Service
@Transactional
public class EcmcVpnServiceImpl extends BaseVpnService implements EcmcVpnService {
    private static final Logger log = LoggerFactory.getLogger(EcmcVpnServiceImpl.class);
    @Autowired
    private CloudVpnDao vpnDao;
    @Autowired
    private CloudVpnIkePolicyDao ikeDao;
    @Autowired
    private CloudVpnIpSecPolicyDao ipSecDao;
    @Autowired
    private CloudVpnConnDao vpnConnDao;
    @Autowired
    private OpenstackVpnService openstackVpnService;
    @Autowired
    private OpenstackVpnIkePolicyService openstackVpnIkePolicyService;
    @Autowired
    private OpenstackVpnIpSecPolicyService openstackVpnIpSecPolicyService;
    @Autowired
    private OpenstackVpnConnectionService openstackVpnConnectionService;
    @Autowired
    private EayunRabbitTemplate rabbitTemplate;

    @Override
    public Page getVpnList(Page page, ParamsMap paramsMap) {
        String vpnName = paramsMap.getParams().get("vpnName") == null ? null : paramsMap.getParams().get("vpnName").toString();
        String cusName = paramsMap.getParams().get("cusName") == null ? null : paramsMap.getParams().get("cusName").toString();
        String prjName = paramsMap.getParams().get("prjName") == null ? null : paramsMap.getParams().get("prjName").toString();
        String datacenterId = paramsMap.getParams().get("datacenterId") == null ? null : paramsMap.getParams().get("datacenterId").toString();
        List<String> values = new ArrayList<String>();
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT ");
        sql.append("        conn.vpn_id");
        sql.append("        ,conn.vpnservice_id");
        sql.append("        ,conn.ike_id");
        sql.append("        ,conn.ipsec_id");
        sql.append("        ,conn.prj_id");
        sql.append("        ,conn.vpn_name");
        sql.append("        ,vpn.vpn_status");
        sql.append("        ,network.net_id");
        sql.append("        ,network.net_name");
        sql.append("        ,route.gateway_ip");
        sql.append("        ,subnet.subnet_id");
        sql.append("        ,subnet.subnet_name");
        sql.append("        ,subnet.cidr");
        sql.append("        ,conn.peer_address");
        sql.append("        ,conn.peer_cidrs");
        sql.append("        ,conn.peer_id");
        sql.append("        ,conn.psk_key");
        sql.append("        ,conn.mtu");
        sql.append("        ,conn.dpd_action");
        sql.append("        ,conn.dpd_interval");
        sql.append("        ,conn.dpd_timeout");
        sql.append("        ,conn.initiator");
        sql.append("        ,conn.pay_type");
        sql.append("        ,conn.charge_state");
        sql.append("        ,conn.create_time");
        sql.append("        ,datacenter.dc_name");
        sql.append("        ,project.prj_name");
        sql.append("        ,customer.cus_name");
        sql.append("        ,datacenter.id");
        sql.append("        ,conn.end_time ");
        sql.append(" FROM ");
        sql.append("        cloud_vpnconn conn");
        sql.append(" LEFT OUTER JOIN ");
        sql.append("        cloud_vpnservice vpn");
        sql.append("        ON conn.vpnservice_id = vpn.vpnservice_id");
        sql.append(" LEFT OUTER JOIN ");
        sql.append("        cloud_network network");
        sql.append("        ON vpn.network_id = network.net_id");
        sql.append(" LEFT OUTER JOIN ");
        sql.append("        cloud_route route");
        sql.append("        ON vpn.route_id = route.route_id");
        sql.append(" LEFT OUTER JOIN ");
        sql.append("        cloud_subnetwork subnet");
        sql.append("        ON vpn.subnet_id = subnet.subnet_id");
        sql.append(" LEFT OUTER JOIN ");
        sql.append("    	cloud_project project ");
        sql.append(" 		ON conn.prj_id = project.prj_id ");
        sql.append(" LEFT OUTER JOIN ");
        sql.append("    	dc_datacenter datacenter ");
        sql.append(" 		ON project.dc_id = datacenter.id ");
        sql.append(" LEFT OUTER JOIN ");
        sql.append("    	sys_selfcustomer customer ");
        sql.append(" 		ON project.customer_id = customer.cus_id ");
        sql.append(" WHERE 1=1 and conn.is_visible = '1'");

        if (!StringUtil.isEmpty(vpnName)) {
            vpnName = vpnName.replaceAll("\\_", "\\\\_");
            sql.append(" and conn.vpn_name like ?");
            values.add("%" + vpnName + "%");
        }
        //数据中心
  		if (!"".equals(datacenterId)&&datacenterId!=null&&!"undefined".equals(datacenterId)&&!"null".equals(datacenterId)) {
  			sql.append(" and project.dc_id = ? ");
  			values.add(datacenterId);
  		}
  		if(!"".equals(cusName)&&cusName!=null&&!"undefined".equals(cusName)&&!"null".equals(cusName)){
			//根据所属客户精确查询
			String[] cusOrgs = cusName.split(",");
			sql.append(" and ( ");
			for(String org:cusOrgs){
				sql.append(" binary customer.cus_org = ? or ");
				values.add(org);
			}
			sql.append(" 1 = 2 ) ");
  		}
		if(!"".equals(prjName)&&prjName!=null&&!"undefined".equals(prjName)&&!"null".equals(prjName)){
			//根据项目名称精确查询
			String[] prjNames = prjName.split(",");
			sql.append(" and ( ");
			for(String prj:prjNames){
				sql.append(" binary project.prj_name = ? or ");
				values.add(prj);
			}
			sql.append(" 1 = 2 ) ");
		}
        sql.append(" order by conn.dc_id , conn.prj_id ,conn.create_time desc");

        QueryMap queryMap = new QueryMap();
        queryMap.setPageNum(paramsMap.getPageNumber());
        queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());

        page = vpnDao.pagedNativeQuery(sql.toString(), queryMap,
                values.toArray());

        List<Object> resultList = (List<Object>) page.getResult();

        for (int i = 0; i < resultList.size(); i++) {
            Object[] objs = (Object[]) resultList.get(i);
            CloudVpn vpn = new CloudVpn();
            int index = 0;
            vpn.setVpnId(String.valueOf(objs[index++]));
            vpn.setVpnserviceId(String.valueOf(objs[index++]));
            vpn.setIkeId(String.valueOf(objs[index++]));
            vpn.setIpsecId(String.valueOf(objs[index++]));
            vpn.setPrjId(String.valueOf(objs[index++]));
            vpn.setVpnName(String.valueOf(objs[index++]));
            vpn.setVpnStatus(String.valueOf(objs[index++]));
            vpn.setNetworkId(String.valueOf(objs[index++]));
            vpn.setNetworkName(String.valueOf(objs[index++]));
            vpn.setGatewayIp(String.valueOf(objs[index++]));
            vpn.setSubnetId(String.valueOf(objs[index++]));
            vpn.setSubnetName(String.valueOf(objs[index++]));
            vpn.setSubnetCidr(String.valueOf(objs[index++]));
            vpn.setPeerAddress(String.valueOf(objs[index++]));
            vpn.setPeerCidrs(String.valueOf(objs[index++]));
            vpn.setPeerId(String.valueOf(objs[index++]));
            vpn.setPskKey(String.valueOf(objs[index++]));
            vpn.setMtu(Long.valueOf(objs[index++].toString()));
            vpn.setDpdAction(String.valueOf(objs[index++]));
            vpn.setDpdInterval(Long.valueOf(objs[index++].toString()));
            vpn.setDpdTimeout(Long.valueOf(objs[index++].toString()));
            vpn.setInitiator(String.valueOf(objs[index++]));
            vpn.setPayType(String.valueOf(objs[index++]));
            vpn.setChargeState(String.valueOf(objs[index++]));
            vpn.setCreateTime((Date) objs[index++]);
            vpn.setDcName(String.valueOf(objs[index++]));
            vpn.setPrjName(String.valueOf(objs[index++]));
            vpn.setCusName(String.valueOf(objs[index++]));
            vpn.setDcId(String.valueOf(objs[index++]));
            vpn.setEndTime((Date)objs[index++]);
            vpn.setPayTypeStr(CloudResourceUtil.escapePayType(vpn.getPayType()));

            if (CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(vpn
                    .getChargeState())) {
                vpn.setVpnStatusStr(DictUtil.getStatusByNodeEn("vpn", vpn.getVpnStatus()));
            } else {
                vpn.setVpnStatusStr(CloudResourceUtil.escapseChargeState(vpn
                        .getChargeState()));
            }
            resultList.set(i, vpn);
        }
        return page;
    }

    @Override
    public CloudVpn getVpnInfo(String vpnId) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT ");
        sql.append("    conn.vpn_id");
        sql.append("    ,conn.vpn_name");
        sql.append("    ,network.net_id ");
        sql.append("    ,network.net_name ");
        sql.append("    ,route.gateway_ip ");
        sql.append("    ,subnet.subnet_id ");
        sql.append("    ,subnet.subnet_name ");
        sql.append("    ,subnet.cidr ");
        sql.append("    ,conn.psk_key ");
        sql.append("    ,datacenter.id ");
        sql.append("    ,datacenter.dc_name ");
        sql.append("    ,conn.create_time ");
        sql.append("    ,vpn.vpn_status ");
        sql.append("    ,conn.peer_address ");
        sql.append("    ,conn.peer_id ");
        sql.append("    ,conn.peer_cidrs ");
        sql.append("    ,conn.pay_type ");
        sql.append("    ,conn.end_time ");

        sql.append("    ,ike.ike_id ");
        sql.append("    ,ike.ike_version ");
        sql.append("    ,ike.negotiation_mode ");
        sql.append("    ,ike.auth_algorithm AS ikeAuthAlgorithm ");
        sql.append("    ,ike.encryption_algorithm AS ikeEncryption ");
        sql.append("    ,ike.dh_algorithm AS ikeDhAlgorithm ");
        sql.append("    ,ike.lifetime_value AS ikeLifetimeValue ");

        sql.append("    ,ipsec.ipsec_id ");
        sql.append("    ,ipsec.transform_protocol ");
        sql.append("    ,ipsec.encapsulation_mode ");
        sql.append("    ,ipsec.auth_algorithm AS ipsecAuthAlgorithm ");
        sql.append("    ,ipsec.encryption_algorithm AS ipsecEncryption ");
        sql.append("    ,ipsec.dh_algorithm AS ipsecDhAlgorithm ");
        sql.append("    ,ipsec.lifetime_value AS ipsecLifetimeValue ");

        sql.append("    ,conn.mtu ");
        sql.append("    ,conn.dpd_action ");
        sql.append("    ,conn.dpd_interval ");
        sql.append("    ,conn.dpd_timeout ");
        sql.append("    ,conn.initiator ");
        sql.append("    ,conn.charge_state ");
        sql.append("    ,project.prj_name ");
        sql.append("    ,customer.cus_name ");
        sql.append(" FROM ");
        sql.append("    cloud_vpnconn conn ");
        sql.append(" LEFT OUTER JOIN ");
        sql.append("    cloud_vpnservice vpn ");
        sql.append(" ON conn.vpnservice_id = vpn.vpnservice_id ");
        sql.append(" LEFT OUTER JOIN ");
        sql.append("    cloud_network network ");
        sql.append(" ON vpn.network_id = network.net_id ");
        sql.append(" LEFT OUTER JOIN ");
        sql.append("    cloud_route route ");
        sql.append(" ON vpn.route_id = route.route_id ");
        sql.append(" LEFT OUTER JOIN ");
        sql.append("    cloud_subnetwork subnet ");
        sql.append(" ON vpn.subnet_id = subnet.subnet_id ");
        sql.append(" LEFT OUTER JOIN ");
        sql.append("    cloud_project project ");
        sql.append(" ON conn.prj_id = project.prj_id ");
        sql.append(" LEFT OUTER JOIN ");
        sql.append("    dc_datacenter datacenter ");
        sql.append(" ON project.dc_id = datacenter.id ");
        sql.append(" LEFT OUTER JOIN ");
        sql.append("    cloud_vpnikepolicy ike ");
        sql.append(" ON conn.ike_id = ike.ike_id ");
        sql.append(" LEFT OUTER JOIN ");
        sql.append("    cloud_vpnipsecpolicy ipsec ");
        sql.append(" ON conn.ipsec_id = ipsec.ipsec_id ");
        sql.append(" LEFT OUTER JOIN ");
        sql.append("    	sys_selfcustomer customer ");
        sql.append(" 		ON project.customer_id = customer.cus_id ");
        sql.append(" WHERE ");
        sql.append("    conn.vpn_id = ?");
        javax.persistence.Query query = vpnDao.createSQLNativeQuery(
                sql.toString(), vpnId);
        CloudVpn cloudVpn = new CloudVpn();
        if (query.getResultList() != null && query.getResultList().size() > 0) {
            Object[] objs = (Object[]) query.getResultList().get(0);
            int index = 0;
            cloudVpn.setVpnId(String.valueOf(objs[index++]));
            cloudVpn.setVpnName(String.valueOf(objs[index++]));
            cloudVpn.setNetworkId(String.valueOf(objs[index++]));
            cloudVpn.setNetworkName(String.valueOf(objs[index++]));
            cloudVpn.setGatewayIp(String.valueOf(objs[index++]));
            cloudVpn.setSubnetId(String.valueOf(objs[index++]));
            cloudVpn.setSubnetName(String.valueOf(objs[index++]));
            cloudVpn.setSubnetCidr(String.valueOf(objs[index++]));
            cloudVpn.setPskKey(String.valueOf(objs[index++]));
            cloudVpn.setDcId(String.valueOf(objs[index++]));
            cloudVpn.setDcName(String.valueOf(objs[index++]));
            cloudVpn.setCreateTime((Date) objs[index++]);
            cloudVpn.setVpnStatus(String.valueOf(objs[index++]));
            cloudVpn.setPeerAddress(String.valueOf(objs[index++]));
            cloudVpn.setPeerId(String.valueOf(objs[index++]));
            cloudVpn.setPeerCidrs(String.valueOf(objs[index++]));
            cloudVpn.setPayType(String.valueOf(objs[index++]));
            cloudVpn.setEndTime((Date) objs[index++]);

            cloudVpn.setIkeId(String.valueOf(objs[index++]));
            cloudVpn.setIkeVersion(String.valueOf(objs[index++]));
            cloudVpn.setIkeNegotiation(String.valueOf(objs[index++]));
            cloudVpn.setIkeAuthAlgorithm(String.valueOf(objs[index++]));
            cloudVpn.setIkeEncryption(String.valueOf(objs[index++]));
            cloudVpn.setIkeDhAlgorithm(String.valueOf(objs[index++]));
            cloudVpn.setIkeLifetimeValue(Long.valueOf(objs[index++].toString()));

            cloudVpn.setIpsecId(String.valueOf(objs[index++]));
            cloudVpn.setIpSecTransform(String.valueOf(objs[index++]));
            cloudVpn.setIpSecEncapsulation(String.valueOf(objs[index++]));
            cloudVpn.setIpSecAuthAlgorithm(String.valueOf(objs[index++]));
            cloudVpn.setIpSecEncryption(String.valueOf(objs[index++]));
            cloudVpn.setIpSecDhAlgorithm(String.valueOf(objs[index++]));
            cloudVpn.setIpSecLifetimeValue(Long.valueOf(objs[index++]
                    .toString()));

            cloudVpn.setMtu(Long.valueOf(objs[index++].toString()));
            cloudVpn.setDpdAction(String.valueOf(objs[index++]));
            cloudVpn.setDpdInterval(Long.valueOf(objs[index++].toString()));
            cloudVpn.setDpdTimeout(Long.valueOf(objs[index++].toString()));
            cloudVpn.setInitiator(String.valueOf(objs[index++]));
            
            cloudVpn.setChargeState(String.valueOf(objs[index++]));
            cloudVpn.setPrjName(String.valueOf(objs[index++]));
            cloudVpn.setCusName(String.valueOf(objs[index++]));

            cloudVpn.setPayTypeStr(CloudResourceUtil.escapePayType(cloudVpn.getPayType()));

            if (CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(cloudVpn
                    .getChargeState())) {
            	cloudVpn.setVpnStatusStr(DictUtil.getStatusByNodeEn("vpn", cloudVpn.getVpnStatus()));
            } else {
            	cloudVpn.setVpnStatusStr(CloudResourceUtil.escapseChargeState(cloudVpn
                        .getChargeState()));
            }
        }
        return cloudVpn;
    }
	@Override
	public boolean deleteVpn(CloudOrderVpn cloudOrderVpn) throws AppException{
		// TODO 删除VPN
		
		/**
    	 * 判断资源是否有未完成的订单 --@author zhouhaitao
    	 */
    	if(checkVpnOrderExist(cloudOrderVpn.getVpnId())){
    		throw new AppException("该资源有未完成的订单，请取消订单后再进行删除操作！");
    	}
		
		int deleteStep  = 0;
		try {
			
            boolean vpnConn = deleteVpnConnection(cloudOrderVpn);
            if (vpnConn) {
                vpnConnDao.delete(cloudOrderVpn.getVpnId());
            }
            boolean ipsec = deleteIpSecPolicy(cloudOrderVpn);
            if (ipsec) {
                ipSecDao.delete(cloudOrderVpn.getIpsecId());
            }
            boolean ike = deleteIkePolicy(cloudOrderVpn);
            if (ike) {
                ikeDao.delete(cloudOrderVpn.getIkeId());
            }
            boolean vpn = deleteVpnService(cloudOrderVpn);
            if (vpn) {
                vpnDao.delete(cloudOrderVpn.getVpnserviceId());
            }
            deleteStep++;
        } catch (AppException e) {
            log.error(e.toString(),e);
            throw e;
        } finally {
            // TODO 底层资源删除失败的处理
        	String cusId = this.getCusIdBuyPrjId(cloudOrderVpn.getPrjId());
        	cloudOrderVpn.setCusId(cusId);
        	vpnDeleteHandle(cloudOrderVpn, deleteStep);
        }
        return true;
	}
	/**
     * 删除vpn后的业务处理
     * @author gaoxiang
     * @param cloudOrderVpn
     * @param deleteStep
     */
    private void vpnDeleteHandle(CloudOrderVpn cloudOrderVpn, int deleteStep) {
        if (deleteStep == 1) {
            if (PayType.PAYAFTER.equals(cloudOrderVpn.getPayType())) {
                /* 调用消息队列发送接口 */
                String key = "BILL_RESOURCE_DELETE";
                ChargeRecord chargeRecord = new ChargeRecord();
                chargeRecord.setDatecenterId(cloudOrderVpn.getDcId());
                chargeRecord.setResourceId(cloudOrderVpn.getVpnId());
                chargeRecord.setResourceName(cloudOrderVpn.getVpnName());
                chargeRecord.setResourceType(ResourceType.VPN);
                chargeRecord.setCusId(cloudOrderVpn.getCusId());
                chargeRecord.setOpTime(new Date());
                rabbitTemplate.send(key, JSONObject.toJSONString(chargeRecord));
            }
        }
    }
	/**
     * 底层删除vpn管道数据私有接口
     *
     * @param cloudOrderVpn
     * @return
     * @author gaoxiang
     */
    private boolean deleteVpnConnection(CloudOrderVpn cloudOrderVpn) {
        return openstackVpnConnectionService.delete(cloudOrderVpn.getDcId(),
                cloudOrderVpn.getPrjId(), cloudOrderVpn.getVpnId());
    }
    /**
     * 底层删除ipsec策略私有接口
     *
     * @param cloudOrderVpn
     * @return
     * @author gaoxiang
     */
    private boolean deleteIpSecPolicy(CloudOrderVpn cloudOrderVpn) {
        return openstackVpnIpSecPolicyService.delete(cloudOrderVpn.getDcId(),
                cloudOrderVpn.getPrjId(), cloudOrderVpn.getIpsecId());
    }
    /**
     * 底层删除ike策略私有接口
     *
     * @param cloudOrderVpn
     * @return
     * @author gaoxiang
     */
    private boolean deleteIkePolicy(CloudOrderVpn cloudOrderVpn) {
        return openstackVpnIkePolicyService.delete(cloudOrderVpn.getDcId(),
                cloudOrderVpn.getPrjId(), cloudOrderVpn.getIkeId());
    }
    /**
     * 底层删除vpn基础配置私有接口
     *
     * @param cloudOrderVpn
     * @return
     * @author gaoxiang
     */
    private boolean deleteVpnService(CloudOrderVpn cloudOrderVpn) {
        return openstackVpnService.delete(cloudOrderVpn.getDcId(),
                cloudOrderVpn.getPrjId(), cloudOrderVpn.getVpnserviceId());
    }

	@Override
	public int getCountByPrjId(String prjId) {
        int vpnCount = vpnDao.getCountByPrjId(prjId);
        int orderCount = getVpnCountInOrder(prjId);
        return vpnCount + orderCount;
	}
	/**
     * 获取订单状态为待创建或者创建中的资源的个数
     * @author gaoxiang
     * @param prjId
     * @return
     */
    private int getVpnCountInOrder(String prjId) {
        StringBuffer sql = new StringBuffer();
        sql.append("select ");
        sql.append("  count(*) ");
        sql.append("from ");
        sql.append("  order_info ");
        sql.append("left join ");
        sql.append("  cloudorder_vpn vpn ");
        sql.append("on ");
        sql.append("  order_info.order_no = vpn.order_no ");
        sql.append("where ");
        sql.append("  order_info.order_type = 0 ");
        sql.append("  and order_info.resource_type = 7");
        sql.append("  and (order_info.order_state = 1 or order_info.order_state = 2)");
        sql.append("  and vpn.prj_id = ?");
        Query query = vpnDao.createSQLNativeQuery(sql.toString(), prjId);
        Object result = query.getSingleResult();
        int orderCount = result == null ? 0 : Integer.parseInt(result.toString());
        return orderCount; 
    }

	@Override
	public CloudVpn updateVpn(CloudVpn cloudVpn) throws AppException{
		try {
            updateVpnConnection(cloudVpn);
        } catch (AppException e) {
            log.error(e.toString(),e);
            throw e;
        }
        return cloudVpn;
	}
	/**
     * 更新vpn管道数据的私有接口
     *
     * @param cloudVpn
     * @return
     * @author gaoxiang
     */
    private VpnConnection updateVpnConnection(CloudVpn cloudVpn) {
        JSONObject dpd = new JSONObject();
        dpd.put("action", cloudVpn.getDpdAction());
        dpd.put("interval", cloudVpn.getDpdInterval());
        dpd.put("timeout", cloudVpn.getDpdTimeout());
        JSONObject vpnconnection = new JSONObject();
        vpnconnection.put("psk", cloudVpn.getPskKey());
        vpnconnection.put("initiator", cloudVpn.getInitiator());
        vpnconnection.put("peer_id", cloudVpn.getPeerId());
        vpnconnection.put("peer_cidrs", cloudVpn.getPeerCidrs().split(","));
        vpnconnection.put("peer_address", cloudVpn.getPeerAddress());
        vpnconnection.put("mtu", cloudVpn.getMtu());
        vpnconnection.put("name", cloudVpn.getVpnName());
        vpnconnection.put("dpd", dpd);
        JSONObject data = new JSONObject();
        data.put("ipsec_site_connection", vpnconnection);
        VpnConnection result = openstackVpnConnectionService.update(
                cloudVpn.getDcId(), cloudVpn.getPrjId(), data,
                cloudVpn.getVpnId());
        if (result != null) {
            BaseCloudVpnConn conn = new BaseCloudVpnConn();
            conn.setVpnId(cloudVpn.getVpnId());
            conn.setVpnName(result.getName());
            conn.setVpnStatus(result.getStatus());
            conn.setPrjId(result.getTenant_id());
            conn.setPeerAddress(result.getPeer_address());
            String peerCidrs = new String();
            for (int i = 0; i < result.getPeer_cidrs().length; i++) {
                if (i > 0) {
                    peerCidrs += ",";
                }
                peerCidrs += result.getPeer_cidrs()[i];
            }
            conn.setPeerCidrs(peerCidrs);
            conn.setPeerId(result.getPeer_id());
            conn.setPskKey(result.getPsk());
            conn.setMtu(Long.valueOf(result.getMtu()));
            conn.setDpdAction(result.getDpd().getAction());
            conn.setDpdInterval(Long.valueOf(result.getDpd().getInterval()));
            conn.setDpdTimeout(Long.valueOf(result.getDpd().getTimeout()));
            conn.setInitiator(result.getInitiator());
            conn.setVpnserviceId(cloudVpn.getVpnserviceId());
            conn.setIkeId(cloudVpn.getIkeId());
            conn.setIpsecId(cloudVpn.getIpsecId());
            conn.setCreateTime(cloudVpn.getCreateTime());
            conn.setEndTime(cloudVpn.getEndTime());
            conn.setDcId(cloudVpn.getDcId());
            conn.setPayType(cloudVpn.getPayType());
            conn.setChargeState(cloudVpn.getChargeState());
            conn.setIsVisible("1");
            vpnConnDao.saveOrUpdate(conn);
        }
        return new VpnConnection();
    }

	@Override
	public boolean checkVpnNameExist(Map<String, String> map) throws Exception {
		String prjId = map.get("prjId").toString();
        String vpnId = map.get("vpnId") != null ? map.get("vpnId").toString() : null;
        String vpnName = map.get("vpnName") != null? map.get("vpnName").toString() : "";
        List<Object> params = new ArrayList<Object>();
        StringBuffer hql = new StringBuffer();
        hql.append(" from BaseCloudVpnConn      ");
        hql.append(" where 1 = 1                ");
        hql.append(" and binary(vpnName) = ?    ");
        hql.append(" and prjId = ?              ");
        params.add(vpnName);
        params.add(prjId);
        if (!StringUtils.isEmpty(vpnId)) {
            hql.append(" and vpnId <> ? ");
            params.add(vpnId);
        }
        try {
            List<BaseCloudVpnConn> vpnConnList = vpnConnDao.find(hql.toString(), params.toArray());
            if (vpnConnList == null || vpnConnList.size() == 0) {
                return checkVpnNameExistInOrder(vpnName, prjId);
            }
        } catch (Exception e) {
            log.error(e.toString(),e);
            throw e;
        }
        return true;
	}
	
    /**
     * 校验订单中的待创建或待支付的vpn资源名称是否存在，以供创建vpn的名称校验
     * @param vpnName
     * @param prjId
     * @return
     * @throws Exception
     */
    private boolean checkVpnNameExistInOrder(String vpnName, String prjId) throws Exception {
        StringBuffer sql = new StringBuffer();
        sql.append(" select count(*) ");
        sql.append("    from order_info ");
        sql.append("    left join cloudorder_vpn vpn ON vpn.order_no = order_info.order_no ");
        sql.append("    where ");
        sql.append("        order_info.order_type = 0 ");
        sql.append("        and order_info.order_state in ('1', '2') ");
        sql.append("        and vpn.vpn_name = ? ");
        sql.append("        and vpn.prj_id = ? ");
        Query query = vpnDao.createSQLNativeQuery(sql.toString(), new Object[] {vpnName, prjId});
        Object result = query.getSingleResult().toString();
        return !(result.equals("null") || result.equals("0"));
    }
    /**
	  * 根据项目ID获取客户ID
	  * @param prjId
	  * @return
	  */
	private String getCusIdBuyPrjId(String prjId){
		StringBuffer hql = new StringBuffer();
        hql.append("select ");
        hql.append("   customer_id ");
        hql.append("from ");
        hql.append("   cloud_project ");
        hql.append("where ");
        hql.append("   prj_id = ?");
        Query query = vpnDao.createSQLNativeQuery(hql.toString(), prjId);
        Object result = query.getSingleResult();
        String cusId = result == null ? "" : String.valueOf(result.toString());
		return cusId;
	}
}
