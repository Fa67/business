package com.eayun.virtualization.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import com.eayun.charge.bean.ResourceCheckBean;
import com.eayun.charge.model.ChargeRecord;
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.BillingCycleType;
import com.eayun.common.constant.EayunQueueConstant;
import com.eayun.common.constant.OrderType;
import com.eayun.common.constant.PayType;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.CloudResourceUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.costcenter.model.MoneyAccount;
import com.eayun.costcenter.service.AccountOverviewService;
import com.eayun.eayunstack.model.Vpn;
import com.eayun.eayunstack.model.VpnConnection;
import com.eayun.eayunstack.model.VpnIkePolicy;
import com.eayun.eayunstack.model.VpnIpSecPolicy;
import com.eayun.eayunstack.service.OpenstackVpnConnectionService;
import com.eayun.eayunstack.service.OpenstackVpnIkePolicyService;
import com.eayun.eayunstack.service.OpenstackVpnIpSecPolicyService;
import com.eayun.eayunstack.service.OpenstackVpnService;
import com.eayun.notice.model.MessageOrderResourceNotice;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.order.model.BaseOrderResource;
import com.eayun.order.model.Order;
import com.eayun.order.service.OrderService;
import com.eayun.price.bean.ParamBean;
import com.eayun.price.bean.PriceDetails;
import com.eayun.price.service.BillingFactorService;
import com.eayun.project.service.ProjectService;
import com.eayun.syssetup.service.SysDataTreeService;
import com.eayun.virtualization.baseservice.BaseVpnService;
import com.eayun.virtualization.dao.CloudVpnConnDao;
import com.eayun.virtualization.dao.CloudVpnDao;
import com.eayun.virtualization.dao.CloudVpnIkePolicyDao;
import com.eayun.virtualization.dao.CloudVpnIpSecPolicyDao;
import com.eayun.virtualization.model.BaseCloudOrderVpn;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.BaseCloudVpn;
import com.eayun.virtualization.model.BaseCloudVpnConn;
import com.eayun.virtualization.model.BaseCloudVpnIkePolicy;
import com.eayun.virtualization.model.BaseCloudVpnIpSecPolicy;
import com.eayun.virtualization.model.CloudOrderVpn;
import com.eayun.virtualization.model.CloudVpn;
import com.eayun.virtualization.service.CloudOrderVpnService;
import com.eayun.virtualization.service.VpnService;

@Service
@Transactional
public class VpnServiceImpl extends BaseVpnService implements VpnService {
    private static final Logger log = LoggerFactory.getLogger(VpnServiceImpl.class);
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
    private CloudOrderVpnService orderVpnService;
    @Autowired
    private JedisUtil jedisUtil;
    @Autowired
    private AccountOverviewService accountOverviewSerivce;
    @Autowired
    private BillingFactorService billingFactorService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private EayunRabbitTemplate rabbitTemplate;
    @Autowired
    private MessageCenterService messageCenterService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private SysDataTreeService sysDataTreeService;

    @Override
    public boolean modifyStatusForSynchronization (CloudVpn cloudVpn) {
        boolean flag = false;
        try {
            BaseCloudVpnConn vpnConn = vpnConnDao.findOne(cloudVpn.getVpnId());
            vpnConn.setVpnStatus(cloudVpn.getVpnStatus());
            vpnConnDao.saveOrUpdate(vpnConn);
            flag = true;
        } catch (Exception e) {
            log.error(e.toString(),e);
            flag = false;
        }
        return flag;
    }
    
    @Override
    public String pop(String groupKey){
        String value = null;
        try {
            value = jedisUtil.pop(groupKey);
        } catch (Exception e){
            log.error(e.toString(),e);
            return null;
        }
        return value;
    }

    
    /**
     * 校验订单中的待创建或待支付的vpn资源名称是否存在，以供创建vpn的名称校验
     * @author gaoxiang
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

    @Override
    public Map<String, String> renewVpn(SessionUserInfo sessionUserInfo, Map param) throws Exception {
        Map<String, String> respMap = new HashMap<>();

        String cusId = sessionUserInfo.getCusId();
        String userId = sessionUserInfo.getUserId();
        String opIp = sessionUserInfo.getIP();
        boolean isUsingBalance = (boolean) param.get("isSelected");

        //前台传入的应付金额
        BigDecimal frontChargeMoney = new BigDecimal(String.valueOf(param.get("chargeMoney")));
        //前台传入的余额支付的金额
        BigDecimal frontBalancePay = new BigDecimal(String.valueOf(param.get("deduction")));
        //前台传入的第三方应支付的金额，鉴于JS精度问题，需要setScale
        BigDecimal frontPayable = new BigDecimal(String.valueOf(param.get("payable")));
        frontPayable = frontPayable.setScale(2, BigDecimal.ROUND_HALF_UP);

        PriceDetails priceDetails = calculatePriceDetailBackend(param);
        BigDecimal backendChargeMoney = priceDetails.getTotalPrice();
        backendChargeMoney = backendChargeMoney.setScale(2, BigDecimal.ROUND_FLOOR);
        //如果前台传入应支付金额与后台计算的应支付金额不想等，则无法提交订单，前台提示用户
        if(frontChargeMoney.compareTo(backendChargeMoney)!=0){
            respMap.put(ConstantClazz.WARNING_CODE,"您的订单金额或资源配置发生变动，请重新确认订单!");
            return respMap;
        }

        if(isUsingBalance){
            //2.如果使用余额支付，则需要后台获取当前账户余额判断与“余额支付”金额的大小关系
            //如果当前余额小于前台传入的余额支付金额，则无法提交订单，前台提示用户
            BigDecimal currentBalance = accountOverviewSerivce.getAccountInfo(cusId).getMoney();
            if(currentBalance.compareTo(frontBalancePay)<0){
                respMap.put(ConstantClazz.WARNING_CODE,"您的余额发生变动，请重新确认订单!");
                return respMap;
            }
            //3.创建订单
            respMap = createVpnOrder(param, priceDetails.getVpnPrice(), backendChargeMoney, frontBalancePay, frontPayable, userId, cusId, opIp);
            if(currentBalance.compareTo(backendChargeMoney)>=0  && frontPayable.compareTo(BigDecimal.ZERO)==0 ){
                //如果使用账户余额可以支付全部的产品金额，则通知前台跳转订单完成页面
                respMap.put(ConstantClazz.SUCCESS_CODE, "BALANCE_PAY_ALL");
            }else{
                //如果使用账户余额支付部分的产品金额，即需要第三方支付，则通知前台跳转支付页面
                respMap.put(ConstantClazz.SUCCESS_CODE, "BALANCE_PAY_PART");
            }
        }else{
            respMap = createVpnOrder(param, priceDetails.getVpnPrice(), backendChargeMoney, frontBalancePay, frontPayable, userId, cusId, opIp);
            if(frontPayable.compareTo(BigDecimal.ZERO)==0){
                respMap.put(ConstantClazz.SUCCESS_CODE, "BALANCE_PAY_ALL");
            }
        }
        return respMap;
    }

    /**
     * 创建VPN续费订单
     * @param param 前台参数
     * @param vpnPrice vpn单价
     * @param backendChargeMoney 后台计算的产品金额
     * @param frontBalancePay 前台传入的余额支付金额
     * @param frontPayable 前台传入的第三方应付金额
     * @param userId 用户ID
     * @param cusId 客户ID
     * @return
     * @throws Exception
     */
    private Map<String,String> createVpnOrder(Map param, BigDecimal vpnPrice,
                                              BigDecimal backendChargeMoney, BigDecimal frontBalancePay, BigDecimal frontPayable,
                                              String userId, String cusId, String opIp) throws Exception {
        Map<String, String> respMap = new HashMap<>();
        String dcName = param.get("dcName")==null?"":param.get("dcName").toString();
        String vpnId = param.get("vpnId")==null?"":param.get("vpnId").toString();
        String vpnName = param.get("vpnName") ==null?"":param.get("vpnName").toString();
        String vpnNetwork = param.get("vpnNetwork") ==null?"":param.get("vpnNetwork").toString();
        String vpnSubnet = param.get("vpnSubnet") ==null?"":param.get("vpnSubnet").toString();
        String vpnPeerAddress = param.get("vpnPeerAddress") ==null?"":param.get("vpnPeerAddress").toString();
        String vpnPeerCidrs = param.get("vpnPeerCidrs") ==null?"":param.get("vpnPeerCidrs").toString();

        BaseCloudVpnConn vpnConn = vpnConnDao.findOne(vpnId);

        Map paramBean = (Map) param.get("paramBean");
        Integer cycle = Integer.valueOf((String)paramBean.get("cycleCount"));
//        Date endTime = new Date((Long)param.get("endTime"));
//        Date expireTime = new Date((Long)param.get("lastTime"));
        Date endTime = vpnConn.getEndTime();
        Date expireTime = DateUtil.getExpirationDate(endTime, cycle, DateUtil.RENEWAL);

        Order order = new Order();
        order.setOrderType(OrderType.RENEW);
        order.setProdName("VPN-续费");
        order.setDcId(String.valueOf(paramBean.get("dcId")));
        order.setProdCount(1);

        StringBuffer prodConfig = new StringBuffer();
        prodConfig.append("数据中心："+dcName)
                .append("<br>VPN名称："+vpnName)
                .append("<br>本端网络："+vpnNetwork)
                .append("<br>本端子网："+vpnSubnet)
                .append("<br>对端网关："+vpnPeerAddress)
                .append("<br>对端子网："+vpnPeerCidrs);

        order.setProdConfig(prodConfig.toString());
        order.setPayType(PayType.PAYBEFORE);
        order.setUnitPrice(vpnPrice);
        order.setBuyCycle(cycle);//续费时长
        order.setResourceType(ResourceType.VPN);
        order.setPaymentAmount(backendChargeMoney);//以后台计算的应支付金额为准
        order.setAccountPayment(frontBalancePay);//账户支付金额入参为前台传入的余额支付的金额
        order.setThirdPartPayment(frontPayable);//账户第三方支付应付款金额为前台传入的应付款金额
        order.setUserId(userId);
        order.setCusId(cusId);
        order.setResourceExpireTime(expireTime);

        Map params = new HashMap();
        params.put("resourceId", vpnId);
        params.put("resourceName", vpnName);
        params.put("resourceType", ResourceType.VPN);
        params.put("expirationDate", endTime);
        params.put("duration", cycle);
        params.put("operatorIp", opIp);
        order.setBusinessParams(params);

        try{
            order = orderService.createOrder(order);
            saveVpnOrderInfo(order, vpnId, cycle);
        }catch(Exception e){
            if(e.getMessage().equals("余额不足")){
                respMap.put(ConstantClazz.WARNING_CODE,"您的余额发生变动，请重新确认订单!");
            }else {
                throw e;
            }
        }
        respMap.put(ConstantClazz.SUCCESS_CODE,"RENEW_SUCCESS");
        respMap.put("orderNo", order.getOrderNo());
        return respMap;
    }

    private PriceDetails calculatePriceDetailBackend(Map param) {
        Map paramBean = (Map) param.get("paramBean");
        String dcId = String.valueOf(paramBean.get("dcId"));
        Integer cycle = Integer.valueOf((String)paramBean.get("cycleCount"));
        int vpnCount = (int)paramBean.get("vpnCount");
        ParamBean pb = new ParamBean();
        pb.setDcId(dcId);
        pb.setPayType(PayType.PAYBEFORE);
        pb.setNumber(1);
        pb.setCycleCount(cycle);
        pb.setVpnCount(vpnCount);
        PriceDetails priceDetails = billingFactorService.getPriceDetails(pb);
        return priceDetails;
    }

    private void saveVpnOrderInfo(Order order, String vpnId, Integer cycle) throws Exception {
        CloudVpn vpn = getVpnById(vpnId);
        CloudOrderVpn orderVpn = new CloudOrderVpn();
        orderVpn.setOrderNo(order.getOrderNo());
        orderVpn.setOrderType(OrderType.RENEW);
        orderVpn.setBuyCycle(cycle);
        orderVpn.setPayType(PayType.PAYBEFORE);
        orderVpn.setPrice(order.getPaymentAmount());
        orderVpn.setVpnId(vpnId);
        orderVpn.setVpnName(vpn.getVpnName());
        orderVpn.setRouteId(vpn.getRouteId());
        orderVpn.setSubnetId(vpn.getSubnetId());
        orderVpn.setPeerAddress(vpn.getPeerAddress());
        orderVpn.setPeerId(vpn.getPeerId());
        orderVpn.setPeerCidrs(vpn.getPeerCidrs());
        orderVpn.setMtu(vpn.getMtu());
        orderVpn.setDpdAction(vpn.getDpdAction());
        orderVpn.setDpdInterval(vpn.getDpdInterval());
        orderVpn.setPskKey(vpn.getPskKey());
        orderVpn.setInitiator(vpn.getInitiator());
        orderVpn.setIkeEncryption(vpn.getIkeEncryption());
//        orderVpn.setCreateName(vpn.get);//fixme vpn中无create_name
        orderVpn.setCreateTime(vpn.getCreateTime());
        orderVpn.setDcId(vpn.getDcId());
        orderVpn.setPrjId(vpn.getPrjId());
        orderVpn.setCusId(vpn.getCusId());

        BaseCloudOrderVpn baseOrderVpn = new BaseCloudOrderVpn();
        BeanUtils.copyProperties(baseOrderVpn, orderVpn);
        orderVpnService.save(baseOrderVpn);
    }

    @Override
    public CloudVpn getVpnById(String vpnId) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT ");
        sql.append("        conn.vpn_id");
        sql.append("        ,conn.vpnservice_id");
        sql.append("        ,conn.ike_id");
        sql.append("        ,conn.ipsec_id");
        sql.append("        ,conn.prj_id");
        sql.append("        ,conn.vpn_name");
        sql.append("        ,conn.vpn_status");
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
        sql.append("        ,conn.end_time");
        sql.append("        ,conn.dc_id");
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
        sql.append(" WHERE conn.vpn_id=?");
        Query query = vpnDao.createSQLNativeQuery(sql.toString(), vpnId);
        List resultList = query.getResultList();
        Object[] objs = (Object[]) resultList.get(0);
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
        vpn.setEndTime((Date)objs[index++]);
        vpn.setDcId(String.valueOf(objs[index++]));

        vpn.setPayTypeStr(CloudResourceUtil.escapePayType(vpn.getPayType()));

        if (CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(vpn
                .getChargeState())) {
            vpn.setVpnStatusStr(DictUtil.getStatusByNodeEn("vpn", vpn.getVpnStatus()));
        } else {
            vpn.setVpnStatusStr(CloudResourceUtil.escapseChargeState(vpn
                    .getChargeState()));
        }
        return vpn;
    }

    @Override
    public Page getVpnList(Page page, ParamsMap paramsMap) {
        String vpnName = paramsMap.getParams().get("vpnName").toString();
        String prjId = paramsMap.getParams().get("prjId").toString();
        List<String> values = new ArrayList<String>();
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT ");
        sql.append("        conn.vpn_id");
        sql.append("        ,conn.vpnservice_id");
        sql.append("        ,conn.ike_id");
        sql.append("        ,conn.ipsec_id");
        sql.append("        ,conn.prj_id");
        sql.append("        ,conn.vpn_name");
//        sql.append("        ,conn.vpn_status");
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
        sql.append("        ,conn.end_time");
        sql.append("        ,conn.dc_id");
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
        sql.append(" WHERE ");
        sql.append("        conn.is_visible = 1 ");

        if (!StringUtil.isEmpty(vpnName)) {
            vpnName = vpnName.replaceAll("\\_", "\\\\_");
            sql.append(" and conn.vpn_name like ?");
            values.add("%" + vpnName + "%");
        }
        if (!StringUtil.isEmpty(prjId)) {
            sql.append(" and conn.prj_id = ?");
            values.add(prjId);
        }
        sql.append(" order by conn.create_time desc");

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
            vpn.setEndTime((Date)objs[index++]);
            vpn.setDcId(String.valueOf(objs[index++]));

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
//        sql.append("    ,conn.vpn_status ");
        sql.append("    ,vpn.vpn_status ");
        sql.append("    ,conn.peer_address ");
        sql.append("    ,conn.peer_id ");
        sql.append("    ,conn.peer_cidrs ");
        sql.append("    ,conn.pay_type ");
        sql.append("    ,conn.charge_state ");
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
            cloudVpn.setChargeState(String.valueOf(objs[index++]));
            cloudVpn.setEndTime((Date) objs[index++]);

            cloudVpn.setIkeId(String.valueOf(objs[index++]));
            cloudVpn.setIkeVersion(String.valueOf(objs[index++]));
            cloudVpn.setIkeNegotiation(String.valueOf(objs[index++]));
            cloudVpn.setIkeAuthAlgorithm(String.valueOf(objs[index++]));
            cloudVpn.setIkeEncryption(String.valueOf(objs[index++]));
            cloudVpn.setIkeDhAlgorithm(String.valueOf(objs[index++]));
            cloudVpn.setIkeLifetimeValue(Long.valueOf(objs[index] == null ? "0" : objs[index].toString()));
            index++;
            
            cloudVpn.setIpsecId(String.valueOf(objs[index++]));
            cloudVpn.setIpSecTransform(String.valueOf(objs[index++]));
            cloudVpn.setIpSecEncapsulation(String.valueOf(objs[index++]));
            cloudVpn.setIpSecAuthAlgorithm(String.valueOf(objs[index++]));
            cloudVpn.setIpSecEncryption(String.valueOf(objs[index++]));
            cloudVpn.setIpSecDhAlgorithm(String.valueOf(objs[index++]));
            cloudVpn.setIpSecLifetimeValue(Long.valueOf(objs[index] == null ? "0" : objs[index].toString()));
            index++;
            
            cloudVpn.setMtu(Long.valueOf(objs[index] == null ? "0" : objs[index].toString()));
            index++;
            cloudVpn.setDpdAction(String.valueOf(objs[index++]));
            cloudVpn.setDpdInterval(Long.valueOf(objs[index] == null ? "0" : objs[index].toString()));
            index++;
            cloudVpn.setDpdTimeout(Long.valueOf(objs[index] == null ? "0" : objs[index].toString()));
            index++;
            cloudVpn.setInitiator(String.valueOf(objs[index++]));
            
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
    
    public BigDecimal getPrice(CloudOrderVpn cloudOrderVpn) {
        return getTotalPrice(cloudOrderVpn);
    }
    /**
     * vpn获取价格配置的私有接口
     * @author gaoxiang
     * @param cloudOrderVpn
     * @return
     */
    private BigDecimal getTotalPrice(CloudOrderVpn cloudOrderVpn) {
        BigDecimal price = new BigDecimal(0);
        ParamBean paramBean = new ParamBean();
        paramBean.setDcId(cloudOrderVpn.getDcId());
        paramBean.setPayType(cloudOrderVpn.getPayType());
        paramBean.setNumber(1);
        paramBean.setVpnCount(1);
        paramBean.setCycleCount(cloudOrderVpn.getBuyCycle());
        price = billingFactorService.getPriceByFactor(paramBean);
        if (PayType.PAYBEFORE.equals(cloudOrderVpn.getPayType())) {
            price = price.setScale(2, BigDecimal.ROUND_FLOOR);
        }
        return price;
    }
    /**
     * vpn拼装预付费order类
     * @author gaoxiang
     * @param cloudOrderVpn
     * @param orderType
     * @param resourceType
     * @param userId
     * @return
     */
    private Order getOrderBeforeByOrderVpn(CloudOrderVpn cloudOrderVpn, String orderType, String resourceType, String userId) {
        Order order = new Order();
        order.setOrderType(orderType);
        StringBuffer prodConfig = new StringBuffer();
        if (OrderType.NEW.equals(order.getOrderType())) {
            order.setProdName("VPN-包年包月");
            order.setBuyCycle(cloudOrderVpn.getBuyCycle());
            prodConfig.append("数据中心：" + cloudOrderVpn.getDcName())
            .append("<br>VPN名称：" + cloudOrderVpn.getVpnName())
            .append("<br>本端网络：" + cloudOrderVpn.getNetName())
            .append("<br>本端子网：" + cloudOrderVpn.getSubnetName() + "(" + cloudOrderVpn.getSubnetCidr() + ")")
            .append("<br>对端网关：" + cloudOrderVpn.getPeerAddress())
            .append("<br>对端子网：" + cloudOrderVpn.getPeerCidrs());
        } else if (OrderType.RENEW.equals(order.getOrderType())) {
            order.setProdName("VPN-续费");
            order.setBuyCycle(cloudOrderVpn.getBuyCycle());
        }
        order.setProdCount(1);
        order.setProdConfig(prodConfig.toString());
        order.setDcId(cloudOrderVpn.getDcId());
        order.setPayType(cloudOrderVpn.getPayType());
        order.setResourceType(resourceType);
        order.setUnitPrice(cloudOrderVpn.getPrice());
        order.setPaymentAmount(cloudOrderVpn.getPrice());
        order.setAccountPayment(cloudOrderVpn.getAccountPayment());
        order.setThirdPartPayment(cloudOrderVpn.getThirdPartPayment());
        order.setUserId(userId);
        order.setCusId(cloudOrderVpn.getCusId());
        return order;
    }
    
    private Order getOrderAfterByOrderVpn(CloudOrderVpn cloudOrderVpn, String orderType, String resourceType, String userId) {
        Order order = new Order();
        order.setOrderType(orderType);
        StringBuffer prodConfig = new StringBuffer();
        if (OrderType.NEW.equals(order.getOrderType())) {
            order.setProdName("VPN-按需付费");
            prodConfig.append("数据中心：" + cloudOrderVpn.getDcName())
            .append("<br>VPN名称：" + cloudOrderVpn.getVpnName())
            .append("<br>本端网络：" + cloudOrderVpn.getNetName())
            .append("<br>本端子网：" + cloudOrderVpn.getSubnetName() + "(" + cloudOrderVpn.getSubnetCidr() + ")")
            .append("<br>对端网关：" + cloudOrderVpn.getPeerAddress())
            .append("<br>对端子网：" + cloudOrderVpn.getPeerCidrs());
        }
        order.setProdCount(1);
        order.setProdConfig(prodConfig.toString());
        order.setDcId(cloudOrderVpn.getDcId());
        order.setPayType(cloudOrderVpn.getPayType());
        order.setBillingCycle(BillingCycleType.HOUR);
        order.setResourceType(resourceType);
//      order.setUnitPrice(cloudOrderPool.getPrice());
//      order.setPaymentAmount(cloudOrderPool.getPrice());
//      order.setAccountPayment(cloudOrderPool.getAccountPayment());
//      order.setThirdPartPayment(cloudOrderPool.getThirdPartPayment());
        order.setUserId(userId);
        order.setCusId(cloudOrderVpn.getCusId());
        return order;
    }
    
    @Override
    public String buyVpn(CloudOrderVpn cloudOrderVpn, SessionUserInfo sessionUser) throws Exception {
        String errMsg = null;
        String userId = sessionUser.getUserId();
        String cusId = sessionUser.getCusId();
        cloudOrderVpn.setCreateName(sessionUser.getUserName());
        cloudOrderVpn.setCusId(cusId);
        if (getVpnQuotasByPrjId(cloudOrderVpn.getPrjId()) < 1) {
            errMsg = "OUT_OF_QUOTA";
            return errMsg;
        } else if (PayType.PAYAFTER.equals(cloudOrderVpn.getPayType())) {
            MoneyAccount accountMoney = accountOverviewSerivce.getAccountInfo(cusId);
            String buyCondition = sysDataTreeService.getBuyCondition();
            BigDecimal createResourceLimitedMoney = new BigDecimal(buyCondition);
            if (accountMoney.getMoney().compareTo(createResourceLimitedMoney) < 0) {
                errMsg = "NOT_SUFFICIENT_FUNDS";
                return errMsg;
            }
        } else if (PayType.PAYBEFORE.equals(cloudOrderVpn.getPayType())) {
            BigDecimal totalPrice = getTotalPrice(cloudOrderVpn);
            if (cloudOrderVpn.getPrice().compareTo(totalPrice) != 0) {
                //TODO 前台显示的价格和后台获取的价格不统一,做前台警告处理
                errMsg = "CHANGE_OF_BILLINGFACTORY";
                return errMsg;
            }
        }
        try {
            if(PayType.PAYBEFORE.equals(cloudOrderVpn.getPayType())){
                /* 拼装Order类 */
                Order order = getOrderBeforeByOrderVpn(cloudOrderVpn, OrderType.NEW, ResourceType.VPN, userId);
                /* 调用订单创建接口 */
                Order reorder = orderService.createOrder(order);
                cloudOrderVpn.setOrderNo(reorder.getOrderNo());
                BaseCloudOrderVpn orderVpn = new BaseCloudOrderVpn();
                BeanUtils.copyPropertiesByModel(orderVpn, cloudOrderVpn);
                orderVpn.setCreateTime(new Date());
                orderVpnService.save(orderVpn);
            } else {
                /* 拼装Order类 */
                Order order = getOrderAfterByOrderVpn(cloudOrderVpn, OrderType.NEW, ResourceType.VPN, userId);
                /* 调用订单创建接口 */
                Order reorder = orderService.createOrder(order);
                cloudOrderVpn.setOrderNo(reorder.getOrderNo());
                BaseCloudOrderVpn orderVpn = new BaseCloudOrderVpn();
                BeanUtils.copyPropertiesByModel(orderVpn, cloudOrderVpn);
                orderVpn.setCreateTime(new Date());
                orderVpnService.save(orderVpn);
                CloudOrderVpn result = new CloudOrderVpn();
                try {
                    result = createVpn(cloudOrderVpn);
                } catch (AppException e) {
                    log.error(e.getMessage(), e);
                    throw new Exception(e);
                }
                /*if(result != null) {
                     调用消息队列发送接口 
                    ParamBean param = new ParamBean();
                    param.setVpnCount(1);
                    ChargeRecord chargeRecord = new ChargeRecord();
                    chargeRecord.setParam(param);
                    chargeRecord.setDatecenterId(result.getDcId());
                    chargeRecord.setOrderNumber(reorder.getOrderNo());
                    chargeRecord.setCusId(cusId);
                    chargeRecord.setResourceId(result.getVpnId());
                    chargeRecord.setResourceType(ResourceType.VPN);
                    chargeRecord.setChargeFrom(new Date());
                    rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_PURCHASE, JSONObject.toJSONString(chargeRecord));
                }*/
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw e;
        }
        return errMsg;
    }

    @Override
    @Transactional(noRollbackFor=AppException.class)
    public CloudOrderVpn createVpn(CloudOrderVpn cloudOrderVpn) throws AppException {
        int createStep = 0;
        try {
            Vpn vpnResult = createVpnService(cloudOrderVpn);
            cloudOrderVpn.setVpnserviceId(vpnResult.getId());
            createStep++;

            VpnIkePolicy ikePolicyResult = createIkePolicy(cloudOrderVpn);
            cloudOrderVpn.setIkeId(ikePolicyResult.getId());
            createStep++;

            VpnIpSecPolicy vpnIpSecPolicyResult = createIpSecPolicy(cloudOrderVpn);
            cloudOrderVpn.setIpsecId(vpnIpSecPolicyResult.getId());
            createStep++;

            VpnConnection vpnConnection = createVpnConnection(cloudOrderVpn);
            cloudOrderVpn.setVpnId(vpnConnection.getId());
            
            orderVpnService.update(cloudOrderVpn.getOrderNo(), cloudOrderVpn.getVpnId());
            createStep = -1;
            if ("ACTIVE".equals(vpnResult.getStatus()) 
                    && ("ACTIVE".equals(vpnConnection.getStatus()) || "DOWN".equals(vpnConnection.getStatus()))) {
                vpnCreateSuccessHandle(cloudOrderVpn.getDcId(), 
                        cloudOrderVpn.getOrderNo(), 
                        cloudOrderVpn.getCusId(), 
                        cloudOrderVpn.getVpnId(), 
                        cloudOrderVpn.getVpnName(), 
                        cloudOrderVpn.getPayType());
                BaseCloudVpnConn baseConn = vpnConnDao.findOne(cloudOrderVpn.getVpnId());
                baseConn.setIsVisible("1");
                vpnConnDao.save(baseConn);
            } else {
                JSONObject json = new JSONObject();
                json.put("orderNo", cloudOrderVpn.getOrderNo());
                json.put("vpnserviceId", vpnResult.getId());
                json.put("vpnId", vpnConnection.getId());
                json.put("vpnStatus", vpnResult.getStatus());
                json.put("connStatus", vpnConnection.getStatus());
                json.put("dcId", cloudOrderVpn.getDcId());
                json.put("prjId", cloudOrderVpn.getPrjId());
                json.put("cusId", cloudOrderVpn.getCusId());
                json.put("count","0");
                jedisUtil.push(RedisKey.vpnKey, json.toJSONString());
            }
        } catch (AppException e) {
            log.error(e.toString(),e);
            throw e;
        } catch (Exception e) {
            log.error(e.toString(),e);
        } finally {
            try {
                if (createStep > -1) {
                    vpnCreateCallback(cloudOrderVpn, createStep);
                }
            } catch (Exception e) {
                log.error(e.toString(),e);
                throw new AppException("orderException");
            }
        }
        return cloudOrderVpn;
    }
    
    /**
     * vpn创建失败的底层回滚
     * @author gaoxiang
     * @param cloudOrderVpn
     * @param createStep
     * @author gaoxiang
     */
    @Override
    @Transactional(noRollbackFor=AppException.class)
    public void vpnCreateCallback(CloudOrderVpn cloudOrderVpn, int createStep) throws Exception {
        orderService.completeOrder(cloudOrderVpn.getOrderNo(), false, null);
        messageCenterService.addResourFailMessage(cloudOrderVpn.getOrderNo(), cloudOrderVpn.getCusId());
        try {
            if (createStep > 0) {
                if (createStep > 1) {
                    if (createStep > 2) {
                        if (createStep > 3) {
                            boolean vpnConn = deleteVpnConnection(cloudOrderVpn);
                            if (vpnConn) {
                                vpnConnDao.delete(cloudOrderVpn.getVpnId());
                            }
                        }
                        boolean ipsec = deleteIpSecPolicy(cloudOrderVpn);
                        if (ipsec) {
                            ipSecDao.delete(cloudOrderVpn.getIpsecId());
                        }
                    }
                    boolean ike = deleteIkePolicy(cloudOrderVpn);
                    if (ike) {
                        ikeDao.delete(cloudOrderVpn.getIkeId());
                    }
                }
                boolean vpn = deleteVpnService(cloudOrderVpn);
                if (vpn) {
                    vpnDao.delete(cloudOrderVpn.getVpnserviceId());
                }
            }
        } catch (Exception e) {
            List<MessageOrderResourceNotice> list = new ArrayList<MessageOrderResourceNotice>();
            MessageOrderResourceNotice orderRe = new MessageOrderResourceNotice();
            orderRe.setResourceName(cloudOrderVpn.getVpnName());
            orderRe.setResourceType(ResourceType.getName(ResourceType.VPN));
            list.add(orderRe);
            messageCenterService.delecteResourFailMessage(list, cloudOrderVpn.getOrderNo());
        }
    }
    /**
     * vpn创建成的业务处理
     * @author gaoxiang
     * @param orderNo
     * @param vpnId
     * @param vpnName
     * @throws Exception
     */
    public void vpnCreateSuccessHandle(String dcId, String orderNo, String cusId, String vpnId, String vpnName, String payType) throws Exception{
        if (PayType.PAYAFTER.equals(payType)) {
            ParamBean param = new ParamBean();
            param.setVpnCount(1);
            ChargeRecord chargeRecord = new ChargeRecord();
            chargeRecord.setParam(param);
            chargeRecord.setDatecenterId(dcId);
            chargeRecord.setOrderNumber(orderNo);
            chargeRecord.setCusId(cusId);
            chargeRecord.setResourceId(vpnId);
            chargeRecord.setResourceType(ResourceType.VPN);
            chargeRecord.setChargeFrom(new Date());
            rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_PURCHASE, JSONObject.toJSONString(chargeRecord));
        }
        
        List<BaseOrderResource> resourceList = new ArrayList<BaseOrderResource>();
        BaseOrderResource resource = new BaseOrderResource();
        resource.setOrderNo(orderNo);
        resource.setResourceId(vpnId);
        resource.setResourceName(vpnName);
        resourceList.add(resource);
        orderService.completeOrder(orderNo, true, resourceList);
    }
    /**
     * vpn本端配置创建私有接口
     *
     * @param cloudOrderVpn
     * @return
     * @author gaoxiang
     */
    private Vpn createVpnService(CloudOrderVpn cloudOrderVpn) {
        JSONObject vpnservice = new JSONObject();
        vpnservice.put("subnet_id", cloudOrderVpn.getSubnetId());
        vpnservice.put("router_id", cloudOrderVpn.getRouteId());
        vpnservice.put("name", cloudOrderVpn.getVpnName());
        vpnservice.put("admin_state_up", "true");
        JSONObject data = new JSONObject();
        data.put("vpnservice", vpnservice);
        Vpn result = openstackVpnService.create(cloudOrderVpn.getDcId(),
                cloudOrderVpn.getPrjId(), data);
        if (result != null) {
            BaseCloudVpn vpn = new BaseCloudVpn();
            vpn.setVpnServiceId(result.getId());
            vpn.setVpnServiceName(result.getName());
            vpn.setVpnServiceStatus(result.getStatus());
            vpn.setPrjId(result.getTenant_id());
            vpn.setNetworkId(cloudOrderVpn.getNetworkId());
            vpn.setRouteId(result.getRouter_id());
            vpn.setSubnetId(result.getSubnet_id());
            vpnDao.save(vpn);
        }
        return result;
    }

    /**
     * ike策略配置创建私有接口
     *
     * @param cloudOrderVpn
     * @return
     * @author gaoxiang
     */
    private VpnIkePolicy createIkePolicy(CloudOrderVpn cloudOrderVpn) {
        JSONObject lifetime = new JSONObject();
        lifetime.put("units", "seconds");
        lifetime.put("value", cloudOrderVpn.getIkeLifetime());
        JSONObject ikepolicy = new JSONObject();
        ikepolicy.put("phase1_negotiation_mode", cloudOrderVpn.getIkeNegotiation());
        ikepolicy.put("auth_algorithm", cloudOrderVpn.getIkeAuth());
        ikepolicy.put("encryption_algorithm", cloudOrderVpn.getIkeEncryption());
        ikepolicy.put("pfs", cloudOrderVpn.getIkeDh());
        ikepolicy.put("lifetime", lifetime);
        ikepolicy.put("ike_version", cloudOrderVpn.getIkeVersion());
        ikepolicy.put("name", cloudOrderVpn.getVpnName());
        JSONObject data = new JSONObject();
        data.put("ikepolicy", ikepolicy);
        VpnIkePolicy result = openstackVpnIkePolicyService.create(
                cloudOrderVpn.getDcId(), cloudOrderVpn.getPrjId(), data);
        if (result != null) {
            BaseCloudVpnIkePolicy ike = new BaseCloudVpnIkePolicy();
            ike.setIkeId(result.getId());
            ike.setIkeName(result.getName());
            ike.setIkeVersion(result.getIke_version());
            ike.setPrjId(result.getTenant_id());
            ike.setAuthAlgorithm(result.getAuth_algorithm());
            ike.setEncryption(result.getEncryption_algorithm());
            ike.setNegotiation(result.getPhase1_negotiation_mode());
            ike.setLifetimeValue(Long.valueOf(result.getLifetime().getValue()));
            ike.setDhAlgorithm(result.getPfs());
            ikeDao.save(ike);
        }
        return result;
    }

    /**
     * ipsec策略配置创建私有接口
     *
     * @param cloudOrderVpn
     * @return
     * @author gaoxiang
     */
    private VpnIpSecPolicy createIpSecPolicy(CloudOrderVpn cloudOrderVpn) {
        JSONObject lifetime = new JSONObject();
        lifetime.put("units", "seconds");
        lifetime.put("value", cloudOrderVpn.getIpsecLifetime());
        JSONObject ipsecpolicy = new JSONObject();
        ipsecpolicy.put("name", cloudOrderVpn.getVpnName());
        ipsecpolicy.put("transform_protocol", cloudOrderVpn.getIpsecProtocol());
        ipsecpolicy.put("auth_algorithm", cloudOrderVpn.getIpsecAuth());
        ipsecpolicy.put("encapsulation_mode", cloudOrderVpn.getIpsecEncapsulation());
        ipsecpolicy.put("encryption_algorithm", cloudOrderVpn.getIpsecEncryption());
        ipsecpolicy.put("pfs", cloudOrderVpn.getIpsecDh());
        ipsecpolicy.put("lifetime", lifetime);
        JSONObject data = new JSONObject();
        data.put("ipsecpolicy", ipsecpolicy);
        VpnIpSecPolicy result = openstackVpnIpSecPolicyService.create(
                cloudOrderVpn.getDcId(), cloudOrderVpn.getPrjId(), data);
        if (result != null) {
            BaseCloudVpnIpSecPolicy ipsec = new BaseCloudVpnIpSecPolicy();
            ipsec.setIpSecId(result.getId());
            ipsec.setIpSecName(result.getName());
            ipsec.setPrjId(result.getTenant_id());
            ipsec.setTransform(result.getTransform_protocol());
            ipsec.setAuthAlgorithm(result.getAuth_algorithm());
            ipsec.setEncapsulation(result.getEncapsulation_mode());
            ipsec.setEncryption(result.getEncryption_algorithm());
            ipsec.setLifetimeValue(Long
                    .valueOf(result.getLifetime().getValue()));
            ipsec.setDhAlgorithm(result.getPfs());
            ipSecDao.save(ipsec);
        }
        return result;
    }

    /**
     * vpn管道配置创建私有接口
     *
     * @param cloudOrderVpn
     * @return
     * @author gaoxiang
     */
    private VpnConnection createVpnConnection(CloudOrderVpn cloudOrderVpn) {
        JSONObject dpd = new JSONObject();
        dpd.put("action", cloudOrderVpn.getDpdAction());
        dpd.put("interval", cloudOrderVpn.getDpdInterval());
        dpd.put("timeout", cloudOrderVpn.getDpdTimeout());
        JSONObject vpnconnection = new JSONObject();
        vpnconnection.put("psk", cloudOrderVpn.getPskKey());
        vpnconnection.put("initiator", cloudOrderVpn.getInitiator());
        vpnconnection.put("peer_id", cloudOrderVpn.getPeerId());
        vpnconnection.put("peer_cidrs", cloudOrderVpn.getPeerCidrs().split(","));
        vpnconnection.put("peer_address", cloudOrderVpn.getPeerAddress());
        vpnconnection.put("mtu", cloudOrderVpn.getMtu());
        vpnconnection.put("name", cloudOrderVpn.getVpnName());
        vpnconnection.put("vpnservice_id", cloudOrderVpn.getVpnserviceId());
        vpnconnection.put("ikepolicy_id", cloudOrderVpn.getIkeId());
        vpnconnection.put("ipsecpolicy_id", cloudOrderVpn.getIpsecId());
        vpnconnection.put("admin_state_up", "true");
        vpnconnection.put("dpd", dpd);
        JSONObject data = new JSONObject();
        data.put("ipsec_site_connection", vpnconnection);
        VpnConnection result = openstackVpnConnectionService.create(
                cloudOrderVpn.getDcId(), cloudOrderVpn.getPrjId(), data);
        if (result != null) {
            BaseCloudVpnConn conn = new BaseCloudVpnConn();
            conn.setVpnId(result.getId());
            conn.setVpnName(result.getName());
            conn.setVpnStatus(result.getStatus());
            conn.setDcId(cloudOrderVpn.getDcId());
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
            conn.setVpnserviceId(result.getVpnservice_id());
            conn.setIkeId(result.getIkepolicy_id());
            conn.setIpsecId(result.getIpsecpolicy_id());
            conn.setCreateTime(new Date());
            conn.setEndTime(DateUtil.getExpirationDate(new Date(), cloudOrderVpn.getBuyCycle(), DateUtil.PURCHASE));
            conn.setPayType(cloudOrderVpn.getPayType());
            conn.setChargeState(CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE);
            conn.setIsVisible("0");
            vpnConnDao.save(conn);
        }
        return result;
    }

    @Override
    public CloudVpn updateVpn(CloudVpn cloudVpn) throws AppException {
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
    public boolean deleteVpn(CloudOrderVpn cloudOrderVpn) throws AppException {
        int deleteStep  = 0;
        try {
        	/**
        	 * 判断资源是否有未完成的订单 --@author zhouhaitao
        	 */
        	if(checkVpnOrderExist(cloudOrderVpn.getVpnId())){
        		throw new AppException("该资源有未完成的订单，请取消订单后再进行删除操作！");
        	}
        	
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
        /*else {
            List<MessageOrderResourceNotice> list = new ArrayList<MessageOrderResourceNotice>();
            MessageOrderResourceNotice orderRe = new MessageOrderResourceNotice();
            orderRe.setResourceName(cloudOrderVpn.getVpnName());
            orderRe.setResourceType(ResourceType.VPN);
            list.add(orderRe);
            messageCenterService.delecteResourFailMessage(list, cloudOrderVpn.getOrderNo());
        }*/
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

    @Override
    public List<Map<String, Object>> getCloudNetworkList(String prjId) {
        StringBuffer sql = new StringBuffer();
        sql.append(" select                                         ");
        sql.append("        network.net_id as netId,                ");
        sql.append("        network.net_name as netName,            ");
        sql.append("        route.route_id as routeId,              ");
        sql.append("        route.gateway_ip as gatewayIp           ");
        sql.append(" from                                           ");
        sql.append("    cloud_network network                       ");
        sql.append(" left join                                      ");
        sql.append("    cloud_route route                           ");
        sql.append(" on                                             ");
        sql.append("    route.network_id = network.net_id           ");
        sql.append(" where                                          ");
        sql.append("    network.router_external = '0'               ");
        sql.append("    and network.charge_state = '0'              ");
        sql.append("    and route.gateway_ip is not null            ");
        /*networkId不存在于已存在的vpn资源的本段配置之中*/
        sql.append("    and not exists (                            ");
        sql.append("        select                                  ");
        sql.append("            *                                   ");
        sql.append("        from                                    ");
        sql.append("            cloud_vpnservice vpn                ");
        sql.append("        where                                   ");
        sql.append("            vpn.network_id = network.net_id     ");
        sql.append("    )                                           ");
        /*networkId不存在于订单待创建或待支付的vpn资源之中*/
        sql.append("    and not exists (                            ");
        sql.append("        select                                  ");
        sql.append("            *                                   ");
        sql.append("        from                                    ");
        sql.append("            cloudorder_vpn ov                   ");
        sql.append("        left join                               ");
        sql.append("            order_info info                     ");
        sql.append("        on                                      ");
        sql.append("            info.order_no = ov.order_no         ");
        sql.append("        where                                   ");
        sql.append("            info.order_type = '0'               ");
        sql.append("            and info.resource_type = '7'        ");
        sql.append("            and info.order_state in ('1','2')   ");
        sql.append("            and ov.network_id = network.net_id  ");
        sql.append("    )                                           ");
        sql.append("    and network.charge_state = '0'              ");
        sql.append("    and network.prj_id = ?                      ");
        Query query = vpnDao.createSQLNativeQuery(sql.toString(), prjId);
        List resultList = query.getResultList();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < resultList.size(); i++) {
            Object[] objs = (Object [])resultList.get(i);
            Map map = new HashMap();
            map.put("netId", objs[0]);
            map.put("netName", objs[1]);
            map.put("routeId", objs[2]);
            map.put("gatewayIp", objs[3]);
            list.add(map);
        }
//        List<Map<String, Object>> list = vpnDao.findNetworkByPrjId(prjId);
        return list;
    }

    @Override
    public int getCountByPrjId(String prjId) {
        int vpnCount = vpnDao.getCountByPrjId(prjId);
        int orderCount = getVpnCountInOrder(prjId);
        return vpnCount + orderCount;
    }

    @Override
    public String getVpnInfoById(String vpnId) throws Exception {
        StringBuffer info = new StringBuffer();
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT ");
        sql.append("    network.net_name ");
        sql.append("    ,route.gateway_ip ");
        sql.append("    ,conn.peer_address ");
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
        sql.append(" WHERE ");
        sql.append("    conn.vpn_id = ?");
        javax.persistence.Query query = vpnDao.createSQLNativeQuery(
                sql.toString(), vpnId);
        if (query.getResultList() != null && query.getResultList().size() > 0) {
            Object[] objs = (Object[]) query.getResultList().get(0);
            info.append("本端VPC：");
            info.append(String.valueOf(objs[0]));
            info.append("(");
            info.append(String.valueOf(objs[1]));
            info.append(") 对端网关：");
            info.append(String.valueOf(objs[2]));
        }
        return info.toString();
    }

    @Override
    public boolean modifyStateForVPN(String resourceId, String chargeState,
                                     Date endTime, boolean isLimit, boolean isResumable) {
        BaseCloudVpnConn baseCloudVpnConn = vpnConnDao.findOne(resourceId);
        BaseCloudProject project = projectService.findProject(baseCloudVpnConn.getPrjId());
        baseCloudVpnConn.setChargeState(chargeState);
        if (endTime != null) {
            baseCloudVpnConn.setEndTime(endTime);
        }
        vpnConnDao.saveOrUpdate(baseCloudVpnConn);
        if (isLimit) {
            //TODO 限制服务
            /* 调用消息队列发送接口 */
            String key = "BILL_RESOURCE_RESTRICT";
            ChargeRecord chargeRecord = new ChargeRecord();
            chargeRecord.setDatecenterId(baseCloudVpnConn.getDcId());
            chargeRecord.setResourceId(baseCloudVpnConn.getVpnId());
            chargeRecord.setResourceType(ResourceType.VPN);
            chargeRecord.setCusId(project.getCustomerId());
            chargeRecord.setOpTime(new Date());
            rabbitTemplate.send(key, JSONObject.toJSONString(chargeRecord));
        } else if (isResumable) {
            /* 调用消息队列发送接口 */
            String key = "BILL_RESOURCE_RECOVER";
            ChargeRecord chargeRecord = new ChargeRecord();
            chargeRecord.setDatecenterId(baseCloudVpnConn.getDcId());
            chargeRecord.setResourceId(baseCloudVpnConn.getVpnId());
            chargeRecord.setResourceType(ResourceType.VPN);
            chargeRecord.setCusId(project.getCustomerId());
            chargeRecord.setOpTime(new Date());
            rabbitTemplate.send(key, JSONObject.toJSONString(chargeRecord));
        }
        return true;
    }

    @Override
    public List<CloudVpn> findVpnByCharge(String prjId, String chargeState,
                                          String payType, Date endTime) {
        StringBuffer hql = new StringBuffer();
        List<Object> paramsList = new ArrayList<Object>();
        hql.append(" FROM ");
        hql.append("    BaseCloudVpnConn conn");
        hql.append(" WHERE ");
        hql.append("    1 = 1");
        hql.append(" AND conn.isVisible='1' ");
        if (!StringUtil.isEmpty(prjId)) {
            hql.append("    AND conn.prjId = ? ");
            paramsList.add(prjId);
        }
        if (!StringUtil.isEmpty(chargeState)) {
            hql.append("    AND conn.chargeState = ? ");
            paramsList.add(chargeState);
        }
        if (!StringUtil.isEmpty(payType)) {
            hql.append("    AND conn.payType = ? ");
            paramsList.add(payType);
        }
        if (endTime != null) {
            hql.append("    AND conn.endTime <= ? ");
            paramsList.add(endTime);
        }
        List<BaseCloudVpnConn> list = vpnConnDao.find(hql.toString(),
                paramsList.toArray());
        List<CloudVpn> cloudVpnList = new ArrayList<CloudVpn>();
        for (BaseCloudVpnConn baseCloudVpnConn : list) {
            CloudVpn cloudVpn = new CloudVpn();
            BeanUtils.copyPropertiesByModel(cloudVpn, baseCloudVpnConn);
            cloudVpnList.add(cloudVpn);
        }
        return cloudVpnList;
    }
    @Override
    public int getVpnQuotasByPrjId(String prjId) {
        BaseCloudProject basePrj = projectService.findProject(prjId);
        int vpnUsed = getCountByPrjId(prjId);
        return basePrj.getCountVpn() - vpnUsed;
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
    public String getVpnNameById(String vpnId){
        BaseCloudVpnConn vpnConn = vpnConnDao.findOne(vpnId);
        if (vpnConn != null) {
            return vpnConn.getVpnName();
        } else {
            return "";
        }
        /*StringBuffer sb = new StringBuffer();
        sb.append("SELECT vpn_name FROM cloud_vpnconn where vpn_id=?");
        Query query = vpnDao.createSQLNativeQuery(sb.toString(), vpnId);
        Object result = query.getSingleResult();
        String vpnName = result==null?"":String.valueOf(result);
        return vpnName;*/
    }
    @Override
    public boolean isExistsByOrderNo(String orderNo) {
        CloudOrderVpn orderVpn = orderVpnService.getOrderVpnByOrderNo(orderNo);
        return isExistsByResourceId(orderVpn.getVpnId()).isExisted();
    }
    @Override
    public ResourceCheckBean isExistsByResourceId(String resourceId) {
        ResourceCheckBean resourceCheckBean = new ResourceCheckBean();
        StringBuffer hql = new StringBuffer();
        hql.append(" from BaseCloudVpnConn where vpnId = ? and isVisible = 1");
        BaseCloudVpnConn conn = (BaseCloudVpnConn) vpnConnDao.findUnique(hql.toString(), new Object[]{resourceId});
        if (conn != null) {
            resourceCheckBean.setResourceName(conn.getVpnName());
            resourceCheckBean.setExisted(true);
        } else {
            resourceCheckBean.setExisted(false);
        }
        return resourceCheckBean;
    }
}
