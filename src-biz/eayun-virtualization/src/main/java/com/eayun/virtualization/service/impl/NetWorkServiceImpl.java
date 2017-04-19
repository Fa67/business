package com.eayun.virtualization.service.impl;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import com.eayun.eayunstack.model.Network;
import com.eayun.eayunstack.model.Route;
import com.eayun.eayunstack.service.OpenstackNetworkService;
import com.eayun.eayunstack.service.OpenstackRouterService;
import com.eayun.notice.model.MessageOrderResourceNotice;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.order.model.BaseOrderResource;
import com.eayun.order.model.Order;
import com.eayun.order.service.OrderService;
import com.eayun.price.bean.ParamBean;
import com.eayun.price.bean.PriceDetails;
import com.eayun.price.bean.UpgradeBean;
import com.eayun.price.service.BillingFactorService;
import com.eayun.project.service.ProjectService;
import com.eayun.syssetup.service.SysDataTreeService;
import com.eayun.virtualization.baseservice.BaseNetworkService;
import com.eayun.virtualization.dao.CloudNetWorkDao;
import com.eayun.virtualization.dao.CloudRouteDao;
import com.eayun.virtualization.model.BaseCloudNetwork;
import com.eayun.virtualization.model.BaseCloudOrderNetWork;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.BaseCloudRoute;
import com.eayun.virtualization.model.CloudNetWork;
import com.eayun.virtualization.model.CloudOrderNetWork;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.model.CloudRoute;
import com.eayun.virtualization.service.CloudOrderNetWorkService;
import com.eayun.virtualization.service.EayunQosService;
import com.eayun.virtualization.service.NetWorkService;
import com.eayun.virtualization.service.RouteService;
import com.eayun.virtualization.service.TagService;

/**
 * NetWorkServiceImpl
 * 
 * @Filename: NetWorkServiceImpl.java
 * @Description:
 * @Version: 1.0
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br> <li>Date: 2015年11月9日</li> <li>Version: 1.0</li> <li>Content:
 *               create</li>
 * 
 */
@Service
@Transactional
public class NetWorkServiceImpl extends BaseNetworkService implements NetWorkService {
    
    private final static Logger log = LoggerFactory.getLogger(NetWorkServiceImpl.class);
	@Autowired
	private CloudNetWorkDao netWorkDao;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private OpenstackNetworkService openStackNetWorkService;
	@Autowired
	private TagService tagService;
	@Autowired
	private EayunQosService eayunQosService;
	@Autowired
	private CloudRouteDao routeDao;
	@Autowired
	private RouteService routeService;
	@Autowired
	private OpenstackRouterService openstackService;
	@Autowired
	private EayunRabbitTemplate rabbitTemplate;
	@Autowired
	private OrderService orderService;
	@Autowired
	private CloudOrderNetWorkService orderNetWorkService;
	@Autowired
	private AccountOverviewService accountOverviewSerivce;
	@Autowired
	private BillingFactorService billingFactorService;
	@Autowired
	private MessageCenterService messageCenterService;
	@Autowired
	private SysDataTreeService sysDataTreeService;
	@Autowired
    private JedisUtil jedisUtil;
	
	@SuppressWarnings("unused")
    private static Map<String,String> getStatusNameMap(){
	    Map<String,String> map = new HashMap<String,String>();
	    map.put("ACTIVE", "成功");
	    return map;
	}
	
	@SuppressWarnings("unchecked")
    public List<BaseCloudNetwork> getCloudNetworkList(String sql,Object... values){
		List<BaseCloudNetwork> list =new ArrayList<BaseCloudNetwork>();
		list = netWorkDao.find(sql, values);
		return list;
	}
	
	private Order getOrderBeforeByOrderNetWork(CloudOrderNetWork cloudOrderNetWork, String orderType, String resourceType, String userId){
		Order order = new Order();
		order.setOrderType(orderType);
		StringBuffer prodConfig = new StringBuffer();
		if (OrderType.NEW.equals(order.getOrderType())) {
		    order.setProdName("私有网络-包年包月");
		    prodConfig.append("数据中心：" + cloudOrderNetWork.getDcName());
		    prodConfig.append("<br>私有网络名称：" + cloudOrderNetWork.getNetName());
		    prodConfig.append("<br>私有网络带宽：" + cloudOrderNetWork.getRate() + "Mbps");
		    order.setBuyCycle(cloudOrderNetWork.getBuyCycle());
		    order.setResourceExpireTime(cloudOrderNetWork.getEndTime()); 
		} else if (OrderType.UPGRADE.equals(order.getOrderType())) {
		    order.setProdName("私有网络-更改带宽");
		    prodConfig.append("数据中心：" + cloudOrderNetWork.getDcName());
	        prodConfig.append("<br>私有网络ID：" + cloudOrderNetWork.getNetId());
	        prodConfig.append("<br>私有网络名称：" + cloudOrderNetWork.getNetName());
	        prodConfig.append("<br>原带宽：" + cloudOrderNetWork.getRateOld() + "Mbps");
	        prodConfig.append("<br>调整后带宽：" + cloudOrderNetWork.getRate() + "Mbps");
		    order.setResourceExpireTime(cloudOrderNetWork.getEndTime()); 
		} else if (OrderType.RENEW.equals(order.getOrderType())) {
		    order.setProdName("私有网络-续费");
		    order.setBuyCycle(cloudOrderNetWork.getBuyCycle());
            order.setResourceExpireTime(cloudOrderNetWork.getEndTime()); 
		}
		order.setProdCount(1);
		order.setProdConfig(prodConfig.toString());
		order.setDcId(cloudOrderNetWork.getDcId());
		order.setPayType(cloudOrderNetWork.getPayType());
		order.setResourceType(resourceType);
		order.setUnitPrice(cloudOrderNetWork.getPrice());
		order.setPaymentAmount(cloudOrderNetWork.getPrice());
        order.setAccountPayment(cloudOrderNetWork.getAccountPayment());
        order.setThirdPartPayment(cloudOrderNetWork.getThirdPartPayment());
		order.setUserId(userId);
		order.setCusId(cloudOrderNetWork.getCusId());
		return order;
	}
	
	private Order getOrderAfterByOrderNetWork(CloudOrderNetWork cloudOrderNetWork, String orderType, String resourceType, String userId){
		Order order = new Order();
		order.setOrderType(orderType);
		StringBuffer prodConfig = new StringBuffer();
		if (OrderType.NEW.equals(order.getOrderType())) {
		    order.setProdName("私有网络-按需付费");
		    prodConfig.append("数据中心：" + cloudOrderNetWork.getDcName());
            prodConfig.append("<br>私有网络名称：" + cloudOrderNetWork.getNetName());
            prodConfig.append("<br>私有网络带宽：" + cloudOrderNetWork.getRate() + "Mbps");
		} else if (OrderType.UPGRADE.equals(order.getOrderType())) {
		    order.setProdName("私有网络-更改带宽");
		    prodConfig.append("数据中心：" + cloudOrderNetWork.getDcName());
	        prodConfig.append("<br>私有网络ID：" + cloudOrderNetWork.getNetId());
	        prodConfig.append("<br>私有网络名称：" + cloudOrderNetWork.getNetName());
	        prodConfig.append("<br>原带宽：" + cloudOrderNetWork.getRateOld() + "Mbps");
	        prodConfig.append("<br>调整后带宽：" + cloudOrderNetWork.getRate() + "Mbps");
		}
		order.setProdCount(1);
		order.setProdConfig(prodConfig.toString());
		order.setDcId(cloudOrderNetWork.getDcId());
		order.setPayType(cloudOrderNetWork.getPayType());
		order.setBillingCycle(BillingCycleType.HOUR);
		order.setResourceType(resourceType);
//		order.setUnitPrice(cloudOrderNetWork.getPrice());
//		order.setPaymentAmount(cloudOrderNetWork.getPrice());
//		order.setAccountPayment(cloudOrderNetWork.getAccountPayment());
//		order.setThirdPartPayment(cloudOrderNetWork.getThirdPartPayment());
		order.setUserId(userId);
		order.setCusId(cloudOrderNetWork.getCusId());
		return order;
	}
	/**
	 * order拼装CloudNetWork
	 * @author gaoxiang
	 * @param cloudOrderNetWork
	 * @return
	 */
	private CloudNetWork assembleModelByOrder(CloudOrderNetWork cloudOrderNetWork) {
	    CloudNetWork network = new CloudNetWork();
	    network.setNetId(cloudOrderNetWork.getNetId());
	    network.setDcId(cloudOrderNetWork.getDcId());
	    network.setPrjId(cloudOrderNetWork.getPrjId());
	    network.setPayType(cloudOrderNetWork.getPayType());
	    
	    return network;
	}
	/*
	 * 陈鹏飞
	 */
    @SuppressWarnings("unchecked")
    @Override
    public Page getNetWorkListByPrjId(Page page,ParamsMap paramsMap) throws Exception {
        String netName = String.valueOf(paramsMap.getParams().get("keyWord"));
        Map<String,Object> map = paramsMap.getParams();
        List<String> values = new ArrayList<String>();
        StringBuffer sql = new StringBuffer();
//        sql.append("select cn.net_id,cn.net_name,cn.net_status,cn.admin_stateup,cn.prj_id,cn.dc_id,count(cs.subnet_id) subNetCount, rou.rate,rou.net_name extNetName,rou.route_name,rou.route_id,rou.gateway_ip from cloud_network cn");
        sql.append("select");
        sql.append("	cn.net_id");						//0
        sql.append("	,cn.net_name");						//1
        sql.append("	,cn.net_status");					//2
        sql.append("	,cn.admin_stateup");				//3
        sql.append("	,cn.prj_id");						//4
        sql.append("	,cn.dc_id");						//5
        sql.append("	,count(cs.subnet_id) subNetCount");	//6
        sql.append("	,rou.rate");						//7
        sql.append("	,rou.net_name extNetName");			//8
        sql.append("	,rou.route_name");					//9
        sql.append("	,rou.route_id");					//10
        sql.append("	,rou.gateway_ip");					//11
        sql.append("	,cn.pay_type");						//12
        sql.append("	,cn.charge_state");					//13
        sql.append("	,cn.create_time");					//14
        sql.append("	,cn.end_time");						//15
        sql.append("    ,dc.dc_name");                      //16
        sql.append(" from");
        sql.append("	cloud_network cn");
        sql.append(" left outer join cloud_subnetwork cs on cn.net_id=cs.net_id");
        sql.append(" left outer join dc_datacenter dc on cn.dc_id = dc.id");
        sql.append(" left outer join");
        sql.append(" (select cr.rate,cn1.net_name,cr.network_id,cr.route_name,cr.route_id,cr.gateway_ip from cloud_route cr left outer join cloud_network cn1 on cn1.net_id=cr.net_id) rou");
        sql.append(" on rou.network_id=cn.net_id");
        sql.append(" where cn.router_external='0' and cn.prj_id = ?  ");
        sql.append("    and cn.is_visible = '1'  ");
        values.add(map.get("prjId").toString());
        if(!StringUtil.isEmpty(netName)){
        	netName=netName.replaceAll("\\_", "\\\\_");
            sql.append(" and binary cn.net_name like ?");
            values.add("%"+netName+"%");
        }
        
        sql.append(" group by cn.net_id order by cn.create_time desc");
        QueryMap queryMap=new QueryMap();
        int pageSize = paramsMap.getPageSize();
        int pageNumber = paramsMap.getPageNumber();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        page= netWorkDao.pagedNativeQuery(sql.toString(), queryMap,values.toArray());
        List<Object> netWorkList = (List<Object>) page.getResult();
        int i=0;
        for (Object object : netWorkList) {
            Object[] objs = (Object[]) object;
            CloudNetWork cloudNetWork = new CloudNetWork();
            cloudNetWork.setNetId(String.valueOf(objs[0]));
            cloudNetWork.setNetName(String.valueOf(objs[1]));
            cloudNetWork.setNetStatus(String.valueOf(objs[2]));
            cloudNetWork.setAdminStateup(String.valueOf(objs[3]));
            cloudNetWork.setAdminStaName(String.valueOf(objs[3]).equals("1")?"UP":"DOWN");
            cloudNetWork.setPrjId(String.valueOf(objs[4]));
            cloudNetWork.setDcId(String.valueOf(objs[5]));
            cloudNetWork.setSubNetCount(Integer.valueOf(String.valueOf(objs[6])));
            cloudNetWork.setRate(Integer.valueOf(String.valueOf(objs[7]==null?0:objs[7])));
            cloudNetWork.setExtNetName(objs[8]==null ? null:String.valueOf(objs[8]));
            cloudNetWork.setRouteName(String.valueOf(objs[9]));
            cloudNetWork.setRouteId(objs[10]==null?null:String.valueOf(objs[10]));
            cloudNetWork.setGatewayIp(objs[11]==null?null:String.valueOf(objs[11]));
            /* 用户中心改版计费相关 */
            cloudNetWork.setPayType(String.valueOf(objs[12]));
            cloudNetWork.setChargeState(String.valueOf(objs[13]));
            cloudNetWork.setCreateTime((Date)objs[14]);
            cloudNetWork.setEndTime((Date)objs[15]);
            cloudNetWork.setDcName(String.valueOf(objs[16]));
            cloudNetWork.setPayTypeStr(CloudResourceUtil.escapePayType(cloudNetWork.getPayType()));
            if ("0".equals(cloudNetWork.getChargeState())) {
            	cloudNetWork.setNetStatusName(DictUtil.getStatusByNodeEn("netWork", cloudNetWork.getNetStatus()));
            } else {
            	cloudNetWork.setNetStatusName(CloudResourceUtil.escapseChargeState(cloudNetWork.getChargeState()));
            }
            netWorkList.set(i, cloudNetWork);
            i++;
        }
        return page;
    }
    @SuppressWarnings("unchecked")
    @Override
    public boolean checkNetWorkName(String netId,String netName,String prjId) {
        List<String> values = new ArrayList<String>();
        StringBuffer hql = new StringBuffer();
        hql.append("from BaseCloudNetwork where binary(netName) =? and prjId=?");
        values.add(netName);
        values.add(prjId);
        if(netId!=null){
            hql.append(" and netId<>?");
            values.add(netId);
        }
        List<BaseCloudNetwork> netWorkList = netWorkDao.find(hql.toString(), values.toArray());
        if (netWorkList.size() == 0) {
            return checkNetworkNameInOrder(netName, prjId);
        } else {
            return false;
        }
    }
    /**
     * 校验名称在订单状态为待创建或者创建中的资源中是否存在
     * @author gaoxiang
     * @param netName
     * @param prjId
     * @return
     */
    private boolean checkNetworkNameInOrder(String netName, String prjId) {
        StringBuffer sql = new StringBuffer();
        sql.append("select ");
        sql.append("   count(*) ");
        sql.append("from ");
        sql.append("   order_info ");
        sql.append("left join ");
        sql.append("   cloudorder_network network ");
        sql.append("on ");
        sql.append("   order_info.order_no = network.order_no ");
        sql.append("where ");
        sql.append("   order_info.order_type = 0 ");
        sql.append("   and order_info.resource_type = 3 ");
        sql.append("   and (order_info.order_state = 1 or order_info.order_state = 2)");
        sql.append("   and network.net_name = ?");
        sql.append("   and network.prj_id = ?");
        List<Object> values = new ArrayList<Object>();
        values.add(netName);
        values.add(prjId);
        Query query = netWorkDao.createSQLNativeQuery(sql.toString(), values.toArray());
        Object result = query.getSingleResult().toString();
        return result.equals("null") || result.equals("0");
    }
    /**
     * 获取价格的后台接口
     * @author gaoxiang
     * @param orderNetWork
     * @return
     */
    private BigDecimal getTotalPrice(CloudOrderNetWork orderNetWork) {
        BigDecimal price = new BigDecimal(0);
        if (OrderType.UPGRADE.equals(orderNetWork.getOrderType()) && PayType.PAYBEFORE.equals(orderNetWork.getPayType())) {
            UpgradeBean upgradeBean=new UpgradeBean();
            upgradeBean.setDcId(orderNetWork.getDcId());
            upgradeBean.setBandValue(orderNetWork.getRate() - orderNetWork.getRateOld());
            if (null != orderNetWork.getNetId()) {
                BaseCloudNetwork network = netWorkDao.findOne(orderNetWork.getNetId());
                upgradeBean.setCycleCount(DateUtil.getUgradeRemainDays(new Date(), network.getEndTime()));
            } else {
                upgradeBean.setCycleCount(DateUtil.getUgradeRemainDays(new Date(), orderNetWork.getEndTime()));
            }
            price = billingFactorService.updateConfigPrice(upgradeBean);
        } else {
            ParamBean paramBean = new ParamBean();
            paramBean.setDcId(orderNetWork.getDcId());
            paramBean.setPayType(orderNetWork.getPayType());
            paramBean.setNumber(1);
            paramBean.setBandValue(orderNetWork.getRate());
            paramBean.setCycleCount(orderNetWork.getBuyCycle());
            price = billingFactorService.getPriceByFactor(paramBean);
            
        }
        if (PayType.PAYBEFORE.equals(orderNetWork.getPayType())) {
            price = price.setScale(2, BigDecimal.ROUND_FLOOR);
        }
        return price;
    }
    @Override
    public String buyNetWork(CloudOrderNetWork cloudOrderNetWork, SessionUserInfo sessionUser) throws Exception {
    	String userId = sessionUser.getUserId();
    	String cusId = sessionUser.getCusId();
    	cloudOrderNetWork.setCreateName(sessionUser.getUserName());
    	cloudOrderNetWork.setCusId(cusId);
    	if (getNetworkQuotasByPrjId(cloudOrderNetWork.getPrjId()) < 1
    	        || getRateQuotasByPrjId(cloudOrderNetWork.getPrjId()) < cloudOrderNetWork.getRate()) {
    	    boolean countFlag = false;
    	    boolean rateFlag = false;
    	    if (getNetworkQuotasByPrjId(cloudOrderNetWork.getPrjId()) < 1) {
    	        countFlag = true;
    	    }
    	    if (getRateQuotasByPrjId(cloudOrderNetWork.getPrjId()) < cloudOrderNetWork.getRate()) {
    	        rateFlag = true;
    	    }
    	    return (countFlag ? "COUNT" : "") + (rateFlag ? "RATE" : "") + "_OUT_OF_QUOTA";
    	} else if (PayType.PAYAFTER.equals(cloudOrderNetWork.getPayType())) {
            MoneyAccount accountMoney = accountOverviewSerivce.getAccountInfo(cusId);
            String buyCondition = sysDataTreeService.getBuyCondition();
            BigDecimal createResourceLimitedMoney = new BigDecimal(buyCondition);
            if (accountMoney.getMoney().compareTo(createResourceLimitedMoney) < 0) {
                return "NOT_SUFFICIENT_FUNDS";
            }
        } else if (PayType.PAYBEFORE.equals(cloudOrderNetWork.getPayType())) {
            BigDecimal totalPrice = getTotalPrice(cloudOrderNetWork);
            if (cloudOrderNetWork.getPrice().compareTo(totalPrice) != 0) {
                return "CHANGE_OF_BILLINGFACTORY";
            }
        }
    	try {
	    	if(PayType.PAYBEFORE.equals(cloudOrderNetWork.getPayType())) {
	    		/* 拼装order类 */
	    		Order order = getOrderBeforeByOrderNetWork(cloudOrderNetWork, OrderType.NEW, ResourceType.NETWORK, userId);
	    		/* 调用订单创建接口 */
	    		Order reorder = orderService.createOrder(order);
	    		/* 拼装私有网络订单类入库 */
	    		cloudOrderNetWork.setOrderNo(reorder.getOrderNo());
	    		BaseCloudOrderNetWork orderNetWork = new BaseCloudOrderNetWork();
	    		BeanUtils.copyPropertiesByModel(orderNetWork, cloudOrderNetWork);
	    		orderNetWork.setOrderNo(reorder.getOrderNo());
	    		orderNetWork.setCreateTime(new Date());
	    		orderNetWorkService.save(orderNetWork);
	    	} else {
	    		/* 拼装order类 */
	    		Order order = getOrderAfterByOrderNetWork(cloudOrderNetWork, OrderType.NEW, ResourceType.NETWORK, userId);
	    		/* 调用订单创建接口 */
    		    Order reorder = orderService.createOrder(order);
    		    cloudOrderNetWork.setOrderNo(reorder.getOrderNo());
    		    /* 拼装私有网络订单类入库 */
    		    cloudOrderNetWork.setOrderNo(reorder.getOrderNo());
    		    BaseCloudOrderNetWork orderNetWork = new BaseCloudOrderNetWork();
    		    BeanUtils.copyPropertiesByModel(orderNetWork, cloudOrderNetWork);
    		    orderNetWork.setCreateTime(new Date());
    		    orderNetWorkService.save(orderNetWork);
	    		CloudOrderNetWork result = new CloudOrderNetWork();
	    		try {
	    		    result = addNetWork(cloudOrderNetWork);
	    		} catch (AppException e){
	    		    log.error(e.getMessage(), e);
	                throw new Exception(e);
	            }
	    		if(result != null) {
	    			/* 调用消息队列发送接口 */
	    			ParamBean param = new ParamBean();
	    			param.setBandValue(result.getRate());
					ChargeRecord chargeRecord = new ChargeRecord();
					chargeRecord.setParam(param);
					chargeRecord.setDatecenterId(result.getDcId());
					chargeRecord.setOrderNumber(reorder.getOrderNo());
					chargeRecord.setCusId(cusId);
					chargeRecord.setResourceId(result.getNetId());
					chargeRecord.setResourceType(ResourceType.NETWORK);
					chargeRecord.setChargeFrom(new Date());
	    			rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_PURCHASE, JSONObject.toJSONString(chargeRecord));
	    		}
	    	}
    	} catch(Exception e) {
    	    log.error(e.getMessage(), e);
    	    throw e;
    	}
    	return null;
    }
    @Override
    @Transactional(noRollbackFor=AppException.class)
    public CloudOrderNetWork addNetWork(CloudOrderNetWork cloudOrderNetWork) throws AppException {
        int createStep = 0;
        try {
            //网络数据
            JSONObject net = new JSONObject();          
            net.put("name", cloudOrderNetWork.getNetName());
            net.put("admin_state_up", "true");
            //用于提交的完整数据
            JSONObject data = new JSONObject();
            data.put("network", net);
            Network netWork = openStackNetWorkService.create(cloudOrderNetWork.getDcId(), cloudOrderNetWork.getPrjId(), data);
            createStep++;
            if(netWork!=null){
                BaseCloudNetwork baseNetWork = new BaseCloudNetwork();
                baseNetWork.setNetId(netWork.getId());
                baseNetWork.setNetName(netWork.getName());
                baseNetWork.setCreateTime(new Date());     
                baseNetWork.setAdminStateup("1");
                baseNetWork.setIsShared("1");
                baseNetWork.setRouterExternal("0");
                baseNetWork.setNetStatus(netWork.getStatus());
                baseNetWork.setCreateName(cloudOrderNetWork.getCreateName());
                baseNetWork.setPrjId(cloudOrderNetWork.getPrjId());
                baseNetWork.setDcId(cloudOrderNetWork.getDcId());
                baseNetWork.setPayType(cloudOrderNetWork.getPayType());
                baseNetWork.setChargeState("0");
                baseNetWork.setEndTime(DateUtil.getExpirationDate(baseNetWork.getCreateTime(), cloudOrderNetWork.getBuyCycle(), DateUtil.PURCHASE));
                baseNetWork.setIsVisible("1");
                netWorkDao.save(baseNetWork);//数据库添加
                /* 更新私有网络订单数据库 */
                cloudOrderNetWork.setNetId(netWork.getId());
                orderNetWorkService.update(cloudOrderNetWork.getOrderNo(), cloudOrderNetWork.getNetId());
                /* 拼装数据调用创建路由的接口 */
                Date date = new Date();
                String dateStr = this.dateToStr(date);
                CloudRoute cloudRoute = new CloudRoute();
                cloudRoute.setRouteName(cloudOrderNetWork.getNetName()+dateStr);
                cloudRoute.setPrjId(cloudOrderNetWork.getPrjId());
                cloudRoute.setDcId(cloudOrderNetWork.getDcId());
                cloudRoute.setCreateName(cloudOrderNetWork.getCreateName());
                cloudRoute.setRate(cloudOrderNetWork.getRate());
                cloudRoute.setNetWorkId(cloudOrderNetWork.getNetId());
                boolean createRouteFlag = this.addRoute(cloudRoute);
                createStep++;
                if (createRouteFlag) {
                    return cloudOrderNetWork;
                }
            }
        } catch (AppException e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            try {
                networkCreateHandle(cloudOrderNetWork, createStep);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new AppException("orderException");
            }
        }
        return null;
    }
    private boolean addRoute(CloudRoute cloudRoute){
    	JSONObject routeJson = new JSONObject();
    	routeJson.put("name", cloudRoute.getRouteName());
    	JSONObject resultData = new JSONObject();
		resultData.put("router", routeJson);
    	Route route = openstackService.create(cloudRoute.getDcId(), cloudRoute.getPrjId(), resultData);
    	if ( null!= route) {
    		BaseCloudRoute baseRoute = new BaseCloudRoute();
    		BeanUtils.copyPropertiesByModel(baseRoute, cloudRoute);
			baseRoute.setRouteId(route.getId());
			baseRoute.setRouteName(route.getName());
			baseRoute.setRouteStatus(route.getStatus());
			baseRoute.setCreateTime(new Date());
			/************************开始设置路由带宽*****************************/
			baseRoute.setRate(cloudRoute.getRate());
			/************************添加本地库*********************/
			routeDao.save(baseRoute);
			return true;
		} else {
			return false;
		}
    }
    private String dateToStr(Date date){
    	DateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    	String dateStr="";
    	if(date!=null){
    		dateStr = sdf.format(date);
    	}
    	return dateStr;
    }
    /**
     * 创建私有网络后的业务处理
     * @author gaoxiang
     * @param cloudOrderNetWork
     * @param createStep
     */
    private void networkCreateHandle(CloudOrderNetWork cloudOrderNetWork, int createStep) throws Exception {
        if (createStep > 1) {
            List<BaseOrderResource> resourceList = new ArrayList<BaseOrderResource>();
            BaseOrderResource resource = new BaseOrderResource();
            resource.setOrderNo(cloudOrderNetWork.getOrderNo());
            resource.setResourceId(cloudOrderNetWork.getNetId());
            resource.setResourceName(cloudOrderNetWork.getNetName());
            resourceList.add(resource);
            orderService.completeOrder(cloudOrderNetWork.getOrderNo(), true, resourceList);
        } else {
            orderService.completeOrder(cloudOrderNetWork.getOrderNo(), false, null);
            messageCenterService.addResourFailMessage(cloudOrderNetWork.getOrderNo(), cloudOrderNetWork.getCusId());
            try {
                if (createStep == 1) {
                    CloudNetWork cloudNetWork = assembleModelByOrder(cloudOrderNetWork);
                    delNetWorkByNetId(cloudNetWork);
                }
            } catch (Exception e) {
                List<MessageOrderResourceNotice> list = new ArrayList<MessageOrderResourceNotice>();
                MessageOrderResourceNotice orderRe = new MessageOrderResourceNotice();
                orderRe.setResourceName(cloudOrderNetWork.getNetName());
                orderRe.setResourceType(ResourceType.getName(ResourceType.NETWORK));
                list.add(orderRe);
                messageCenterService.delecteResourFailMessage(list, cloudOrderNetWork.getOrderNo());
            }
        }
    }
    
    @Override
    public String changeNetWork(CloudOrderNetWork cloudOrderNetWork, SessionUserInfo sessionUser) throws Exception {
    	String userId = sessionUser.getUserId();
    	String cusId = sessionUser.getCusId();
    	cloudOrderNetWork.setCreateName(sessionUser.getUserName());
    	cloudOrderNetWork.setCusId(cusId);
        if (getRateQuotasByPrjId(cloudOrderNetWork.getPrjId()) < cloudOrderNetWork.getRate() - cloudOrderNetWork.getRateOld()) {
            return "RATE_OUT_OF_QUOTA";
        } else if (PayType.PAYAFTER.equals(cloudOrderNetWork.getPayType())) {
            MoneyAccount accountMoney = accountOverviewSerivce.getAccountInfo(cusId);
            BigDecimal zero = new BigDecimal(0);
            if (accountMoney.getMoney().compareTo(zero) < 0) {
                return "BALANCE_OF_ARREARS";
            }
        } else if (PayType.PAYBEFORE.equals(cloudOrderNetWork.getPayType())) {
            BigDecimal totalPrice = getTotalPrice(cloudOrderNetWork);
            if (cloudOrderNetWork.getPrice().compareTo(totalPrice) != 0) {
                return "CHANGE_OF_BILLINGFACTORY";
            }
        }
        if (checkNetworkOrderExist(cloudOrderNetWork.getNetId())) {
            return "UPGRADING_OR_RENEWING";
        }
        if (checkNetworkConfiguration(cloudOrderNetWork)) {
            return "CHANGE_OF_CONFIGURATION";
        }
    	try {
    		if (PayType.PAYBEFORE.equals(cloudOrderNetWork.getPayType())) {
    			/* 拼装order类 */
	    		Order order = getOrderBeforeByOrderNetWork(cloudOrderNetWork, OrderType.UPGRADE, ResourceType.NETWORK, userId);
	    		/* 调用订单创建接口 */
	    		Order reorder = orderService.createOrder(order);
	    		/* 拼装私有网络订单数据入库 */
	    		cloudOrderNetWork.setOrderNo(reorder.getOrderNo());
	    		BaseCloudOrderNetWork orderNetWork = new BaseCloudOrderNetWork();
	    		BeanUtils.copyPropertiesByModel(orderNetWork, cloudOrderNetWork);
	    		orderNetWork.setCreateTime(new Date());
        		orderNetWorkService.save(orderNetWork);
        	} else {
        	    /* 拼装order类 */
        		Order order = getOrderAfterByOrderNetWork(cloudOrderNetWork, OrderType.UPGRADE, ResourceType.NETWORK, userId);
        		/* 调用订单创建接口 */
        		Order reorder = orderService.createOrder(order);
        		/* 调用私有网络修改配置接口 */
        		cloudOrderNetWork.setOrderNo(reorder.getOrderNo());
        		BaseCloudOrderNetWork orderNetWork = new BaseCloudOrderNetWork();
                BeanUtils.copyPropertiesByModel(orderNetWork, cloudOrderNetWork);
                orderNetWork.setCreateTime(new Date());
                orderNetWorkService.save(orderNetWork);
        		try {
        		    updateNetWork(cloudOrderNetWork);
        		} catch (AppException e) {
        		    log.error(e.getMessage(), e);
        		    throw new Exception(e);
        		}
        		/* 调用消息队列发送接口 */
        		String key = "BILL_RESOURCE_UPGRADE";
        		ParamBean param = new ParamBean();
    			param.setBandValue(cloudOrderNetWork.getRate());
				ChargeRecord chargeRecord = new ChargeRecord();
				chargeRecord.setParam(param);
				chargeRecord.setDatecenterId(cloudOrderNetWork.getDcId());
				chargeRecord.setOrderNumber(reorder.getOrderNo());
				chargeRecord.setCusId(cusId);
				chargeRecord.setResourceId(cloudOrderNetWork.getNetId());
				chargeRecord.setResourceType(ResourceType.NETWORK);
				chargeRecord.setChargeFrom(new Date());
				rabbitTemplate.send(key, JSONObject.toJSONString(chargeRecord));
        	}
    	} catch (Exception e) {
    	    log.error(e.getMessage(), e);
    		throw e;
    	}
    	return null;
    }
    @Override
    public CloudNetWork updateNetWorkName(CloudNetWork cloudNetWork) {
    	/* 拼装调用修改底层网络资源的数据 */
    	JSONObject net = new JSONObject();
    	net.put("name", cloudNetWork.getNetName());
    	JSONObject data = new JSONObject();
    	data.put("network", net);
    	/* 调用底层修改网络资源的接口 */
    	Network result = openStackNetWorkService.update(cloudNetWork.getDcId(), cloudNetWork.getPrjId(), data, cloudNetWork.getNetId());
    	if(result != null) {
    		BaseCloudNetwork baseCloudNetwork = netWorkDao.findOne(cloudNetWork.getNetId());
    		baseCloudNetwork.setNetName(result.getName());
    		netWorkDao.saveOrUpdate(baseCloudNetwork);
    		return cloudNetWork;
    	}
    	return null;
    }
    @Override
    @Transactional(noRollbackFor=AppException.class)
    public CloudOrderNetWork updateNetWork(CloudOrderNetWork cloudOrderNetWork) throws AppException {
        int updateStep = 0;
        try {
            CloudRoute cloudRoute = this.findRouteByNetId(cloudOrderNetWork.getNetId());
            if(cloudRoute!=null && !StringUtils.isEmpty(cloudRoute.getRouteId()) ){
                this.updateRoute(cloudRoute.getRouteId(), cloudOrderNetWork.getRate());
            }
            updateStep++;
            return cloudOrderNetWork;
        } catch (AppException e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            try {
                networkUpdateHandle(cloudOrderNetWork, updateStep);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new AppException("orderException");
            }
        }
    }
    private void updateRoute(String routeId,int rate){
        /*StringBuffer sql = new StringBuffer();
        sql.append(" select ");
        sql.append("    route_id, ");           //0
        sql.append("    route_name, ");         //1
        sql.append("    create_name, ");        //2
        sql.append("    prj_id, ");             //3
        sql.append("    dc_id, ");              //4
        sql.append("    create_time, ");        //5
        sql.append("    route_status, ");       //6
        sql.append("    net_id, ");             //7
        sql.append("    rate, ");               //8
        sql.append("    rate_old, ");           //9
        sql.append("    qos_id, ");             //10
        sql.append("    default_queue_id, ");   //11
        sql.append("    network_id, ");         //12
        sql.append("    gateway_ip ");          //13
        sql.append(" from cloud_route ");
        sql.append(" where route_id = ? ");
        javax.persistence.Query query = routeDao.createSQLNativeQuery(sql.toString(), new Object[]{routeId});
        BaseCloudRoute baseRoute = new BaseCloudRoute();
        if(query.getResultList()!=null && query.getResultList().size() > 0) {
            Object[] objs = (Object[]) query.getResultList().get(0);
            baseRoute.setRouteId(String.valueOf(objs[0]));
            baseRoute.setRouteName(String.valueOf(objs[1]));
            baseRoute.setDcId(String.valueOf(objs[4]));
            baseRoute.setRate(Integer.valueOf(String.valueOf(objs[8])));
            baseRoute.setQosId(String.valueOf(objs[10]));
        }*/
		BaseCloudRoute baseRoute = routeDao.findOne(routeId);
		if(rate==baseRoute.getRate()){
			return;
		}
		if(!StringUtils.isEmpty(baseRoute.getQosId())){
		    try{
		        eayunQosService.changeQos(baseRoute.getDcId(), baseRoute.getQosId(), rate);
		    }catch(Exception e){
		        throw e;
		    }
		}
		baseRoute.setRate(rate);
		routeDao.saveOrUpdate(baseRoute);
    }
    /**
     * 升级配置私有网络后的业务处理
     * @author gaoxiang
     * @param cloudOrderNetWork
     * @param updateStep
     */
    private void networkUpdateHandle(CloudOrderNetWork cloudOrderNetWork, int updateStep) throws Exception {
        List<BaseOrderResource> resourceList = new ArrayList<BaseOrderResource>();
        BaseOrderResource resource = new BaseOrderResource();
        resource.setOrderNo(cloudOrderNetWork.getOrderNo());
        resource.setResourceId(cloudOrderNetWork.getNetId());
        resource.setResourceName(cloudOrderNetWork.getNetName());
        resourceList.add(resource);
        /*由于资源订单数据库中没有到期时间，所以通过订单中的资源id查询资源数据库，获取资源到期时间*/
        CloudNetWork network = getNetworkById(cloudOrderNetWork.getNetId());
        if (updateStep == 1) {
            orderService.completeOrder(cloudOrderNetWork.getOrderNo(), true, resourceList, false, network.getEndTime());
        } else {
            orderService.completeOrder(cloudOrderNetWork.getOrderNo(), false, resourceList);
            messageCenterService.addResourFailMessage(cloudOrderNetWork.getOrderNo(), cloudOrderNetWork.getCusId());
        }
    }
    @Override
    public boolean delNetWorkByNetId(CloudNetWork cloudNetWork) throws AppException {
        int deleteStep = 0;
        try {
        	
        	/**
        	 * 判断资源是否有未完成的订单 --@author zhouhaitao
        	 */
        	if(checkNetworkOrderExist(cloudNetWork.getNetId())){
        		throw new AppException("该资源有未完成的订单，请取消订单后再进行删除操作！");
        	}
        	
            StringBuffer hql = new StringBuffer();
//    	hql.append("from BaseCloudLdPool ldPool,BaseCloudSubNetWork subNet where subNet.netId=? and ldPool.subnetId=subNet.subnetId");
//    	org.hibernate.Query query = netWorkDao.createQuery(hql.toString(), cloudNetWork.getNetId());
//    	if(query.iterate().hasNext()){
//    		return false;
//    	}
            //删除底层
            boolean bool = openStackNetWorkService.delete(cloudNetWork.getDcId(), cloudNetWork.getPrjId(), cloudNetWork.getNetId());
            deleteStep++;
            boolean flag=false;
            if(bool){
                //删除数据库
                netWorkDao.delete(cloudNetWork.getNetId());
                netWorkDao.execSQL("delete from cloud_subnetwork where net_id ='"+cloudNetWork.getNetId()+"'");
                //删除资源后更新缓存接口
                tagService.refreshCacheAftDelRes("network", cloudNetWork.getNetId());
//			hql.delete(0, hql.length());
                hql.append(" from BaseCloudRoute route where route.netWorkId=?");
                org.hibernate.Query query1 = netWorkDao.createQuery(hql.toString(), cloudNetWork.getNetId());
                while(query1.iterate().hasNext()){
                    BaseCloudRoute baseCloudRoute = (BaseCloudRoute) query1.iterate().next();
                    flag = openstackService.delete(cloudNetWork.getDcId(),null,baseCloudRoute.getRouteId());
                    deleteStep++;
                    if(flag){
                        routeDao.delete(baseCloudRoute.getRouteId());
                        return true;
                    }
                }
            }
        } catch (AppException e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            networkDeleteHandle(cloudNetWork, deleteStep);
        }
        return false;
    }
    /**
     * 删除私有网络后的业务处理
     * @author gaoxiang
     * @param cloudNetWork
     * @param deleteStep
     */
    private void networkDeleteHandle(CloudNetWork cloudNetWork, int deleteStep) {
        if (deleteStep > 1) {
            if (PayType.PAYAFTER.equals(cloudNetWork.getPayType())) {
                /* 调用消息队列发送接口 */
                String key = "BILL_RESOURCE_DELETE";
                ChargeRecord chargeRecord = new ChargeRecord();
                chargeRecord.setDatecenterId(cloudNetWork.getDcId());
                chargeRecord.setResourceId(cloudNetWork.getNetId());
                chargeRecord.setResourceName(cloudNetWork.getNetName());
                chargeRecord.setResourceType(ResourceType.NETWORK);
                chargeRecord.setCusId(cloudNetWork.getCusId());
                chargeRecord.setOpTime(new Date());
                rabbitTemplate.send(key, JSONObject.toJSONString(chargeRecord));
            }
        }
        /*else {
            List<MessageOrderResourceNotice> list = new ArrayList<MessageOrderResourceNotice>();
            MessageOrderResourceNotice orderRe = new MessageOrderResourceNotice();
            orderRe.setResourceName(cloudNetWork.getNetName());
            orderRe.setResourceType(ResourceType.NETWORK);
            list.add(orderRe);
            messageCenterService.delecteResourFailMessage(list, cloudNetWork.getOrderNo());
        }*/
    }
    @Override
    public boolean renewNetWork(CloudOrderNetWork cloudOrderNetWork, SessionUserInfo sessionUser) throws Exception {
    	boolean flag = false;
    	String userId = sessionUser.getUserId();
    	try {
    		/* 拼装Order类 */
    		Order order = getOrderBeforeByOrderNetWork(cloudOrderNetWork, OrderType.RENEW, ResourceType.NETWORK, userId);
    		/* 调用订单创建接口 */
        	Order reorder = orderService.createOrder(order);
        	/* 拼装私有网络订单数据入库 */
        	BaseCloudOrderNetWork orderNetWork = new BaseCloudOrderNetWork();
        	BeanUtils.copyPropertiesByModel(orderNetWork, cloudOrderNetWork);
        	orderNetWork.setOrderNo(reorder.getOrderNo());
        	orderNetWork.setCreateName(sessionUser.getUserName());
            orderNetWork.setCusId(sessionUser.getCusId());
        	orderNetWork.setCreateTime(new Date());
        	orderNetWorkService.save(orderNetWork);
    	} catch(Exception e) {
    		throw e;
    	}
    	return flag;
    }
    @Override
    public void modifyStateForNetWork(String resourceId, String chargeState, Date endTime, boolean isRestict, boolean isResumable) {
        log.info("限制私有网络服务开始");
    	BaseCloudNetwork baseCloudNetWork = netWorkDao.findOne(resourceId);
    	BaseCloudProject project = projectService.findProject(baseCloudNetWork.getPrjId());
    	baseCloudNetWork.setChargeState(chargeState);
    	if(endTime != null) {
    		baseCloudNetWork.setEndTime(endTime);
    	}
    	netWorkDao.saveOrUpdate(baseCloudNetWork);
    	try {
    	    if(isRestict) {
    	        if (!"1".equals(jedisUtil.get(RedisKey.CHARGE_NETWORK_RESTRICTED + resourceId))) {
    	            log.info("该资源" + resourceId + "未被限制过服务，即将开始限制并将redis对应的限制服务标志位置为1");
    	            restrictNetWorkRate(resourceId);
    	            /* 调用消息队列发送接口 */
    	            String key = "BILL_RESOURCE_RESTRICT";
    	            ChargeRecord chargeRecord = new ChargeRecord();
    	            chargeRecord.setDatecenterId(baseCloudNetWork.getDcId());
    	            chargeRecord.setResourceId(baseCloudNetWork.getNetId());
    	            chargeRecord.setResourceType(ResourceType.NETWORK);
    	            chargeRecord.setCusId(project.getCustomerId());
    	            chargeRecord.setOpTime(new Date());
    	            rabbitTemplate.send(key, JSONObject.toJSONString(chargeRecord));
//    	            jedisUtil.setEx(RedisKey.CHARGE_NETWORK_RESTRICTED + resourceId, "1", 1800);
    	            jedisUtil.set(RedisKey.CHARGE_NETWORK_RESTRICTED + resourceId, "1");
    	        } else {
    	            log.info("该资源" + resourceId +"已经被限制过服务了");
    	        }
        	} else {
        		if(isResumable) {
        			resumeNetWorkRate(resourceId);
        			/* 调用消息队列发送接口 */
        			String key = "BILL_RESOURCE_RECOVER";
        			ChargeRecord chargeRecord = new ChargeRecord();
        			chargeRecord.setDatecenterId(baseCloudNetWork.getDcId());
        			chargeRecord.setResourceId(baseCloudNetWork.getNetId());
        			chargeRecord.setResourceType(ResourceType.NETWORK);
        			chargeRecord.setCusId(project.getCustomerId());
        			chargeRecord.setOpTime(new Date());
        			rabbitTemplate.send(key, JSONObject.toJSONString(chargeRecord));
        			log.info("该资源" + resourceId + "已被恢复服务，将redis对应的限制服务标志位置为0");
        			jedisUtil.set(RedisKey.CHARGE_NETWORK_RESTRICTED + resourceId, "0");
        		}
        	}
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}
    }
    /**
     * 限制私有网络服务私有接口
     * @author gaoxiang
     * @param netId
     */
    private void restrictNetWorkRate(String netId) {
        BaseCloudRoute base = routeDao.queryByNetworkId(netId);
        base.setRateOld(base.getRate());
        routeDao.saveOrUpdate(base);
        updateRoute(base.getRouteId(), 1);
        return;
    }
    /**
     * 恢复私有网络服务私有接口
     * @author gaoxiang
     * @param netId
     */
    private void resumeNetWorkRate(String netId) {
        BaseCloudRoute base = routeDao.queryByNetworkId(netId);
        updateRoute(base.getRouteId(), base.getRateOld());
    }
    
    @Override
    public BigDecimal getPrice(CloudOrderNetWork cloudOrderNetWork) {
        return getTotalPrice(cloudOrderNetWork);
    }
    
    @Override
    public CloudNetWork findNetWorkByNetId(String netId) {
        BaseCloudNetwork baseNetWork = netWorkDao.findOne(netId);
        CloudNetWork cloudNetWork = new CloudNetWork();
        BeanUtils.copyPropertiesByModel(cloudNetWork, baseNetWork);
        CloudProject cloudPrj = projectService.findProject(baseNetWork.getPrjId());
        cloudNetWork.setPrjName(cloudPrj.getPrjName());
        cloudNetWork.setPrjQuotaSubNet(cloudPrj.getSubnetCount());
        cloudNetWork.setDcName(cloudPrj.getDcName());
        cloudNetWork.setNetStatusName(DictUtil.getStatusByNodeEn("netWork", cloudNetWork.getNetStatus()));
        CloudRoute cloudRoute = this.findRouteByNetId(netId);
        cloudNetWork.setRouteId(cloudRoute.getRouteId());
        cloudNetWork.setRouteName(cloudRoute.getRouteName());
        cloudNetWork.setRate(cloudRoute.getRate());
        cloudNetWork.setExtNetName(cloudRoute.getNetName());//外网名称
        cloudNetWork.setGatewayIp(cloudRoute.getGatewayIp());//外网名称
        cloudNetWork.setPayTypeStr(CloudResourceUtil.escapePayType(cloudNetWork.getPayType()));
        if ("0".equals(cloudNetWork.getChargeState())) {
            cloudNetWork.setNetStatusName(DictUtil.getStatusByNodeEn("netWork", cloudNetWork.getNetStatus()));
        } else {
            cloudNetWork.setNetStatusName(CloudResourceUtil.escapseChargeState(cloudNetWork.getChargeState()));
        }
        return cloudNetWork;
    }
    private CloudRoute findRouteByNetId(String netId){
    	StringBuffer strb = new StringBuffer();
//    	strb.append("select cr.rate,cn.net_name,cn.net_id,cr.route_id,cr.gateway_ip from cloud_route cr left outer join cloud_network cn on cn.net_id=cr.net_id where cr.network_id=?");
    	strb.append("select");
    	strb.append("	cr.rate ");				//0
    	strb.append("	,cn.net_name ");		//1
    	strb.append("	,cn.net_id ");			//2
    	strb.append("	,cr.route_id ");		//3
    	strb.append("	,cr.route_name ");		//4
    	strb.append("	,cr.gateway_ip ");		//5
    	strb.append("from");
    	strb.append("	cloud_route cr ");
    	strb.append("left outer join");
    	strb.append("	cloud_network cn ");
    	strb.append("on");
    	strb.append("	cn.net_id = cr.net_id ");
    	strb.append("where");
    	strb.append("	cr.network_id = ?");
    	
    	javax.persistence.Query query =routeDao.createSQLNativeQuery(strb.toString(), netId);
    	CloudRoute cloudRoute = new CloudRoute();
    	if(query.getResultList()!=null && query.getResultList().size()>0){//路由跟网络为1对1
    		Object[] objs = (Object[]) query.getResultList().get(0);
    		cloudRoute.setRate(Integer.valueOf(String.valueOf(objs[0])));
			cloudRoute.setNetName(objs[1]==null ?null:String.valueOf(objs[1]));
			cloudRoute.setNetId(String.valueOf(objs[2]));
			cloudRoute.setRouteId(String.valueOf(objs[3]));
			cloudRoute.setRouteName(String.valueOf(objs[4]));
			cloudRoute.setGatewayIp(String.valueOf(objs[5]));
    	}
    	return cloudRoute;
    }
    /**
     * 检验升级订单的基础配置是否为最新配置
     * @author gaoxiang
     * @param cloudOrderNetwork
     * @return
     */
    private boolean checkNetworkConfiguration(CloudOrderNetWork cloudOrderNetwork) {
        StringBuffer hql = new StringBuffer();
        hql.append(" select ");
        hql.append("    r.rate ");
        hql.append(" from ");
        hql.append("    BaseCloudNetwork n, BaseCloudRoute r ");
        hql.append(" where ");
        hql.append("    n.netId = r.netWorkId ");
        hql.append("    and n.netId = ? ");
        int rate = (int) routeDao.findUnique(hql.toString(), new Object[]{cloudOrderNetwork.getNetId()});
        return cloudOrderNetwork.getRateOld() != rate;
    }
    /**
     * 获取带宽配额的私有接口
     * @author gaoxiang
     * @param prjId
     * @return
     */
    private int getRateQuotasByPrjId(String prjId) {
        BaseCloudProject basePrj = projectService.findProject(prjId);
        int rateUsed = routeService.getQosNumByPrjId(prjId);
        return basePrj.getCountBand() - rateUsed;
        
    }
    @Override
    public int getNetworkQuotasByPrjId(String prjId) {
        BaseCloudProject basePrj = projectService.findProject(prjId);
        int networkUsed = findNetWorkCountByPrjId(prjId);
        return basePrj.getNetWork() - networkUsed;
    }
    @Override
    public int getBandQuotasByPrjId(String prjId) {
        return getRateQuotasByPrjId(prjId);
    }
    @Override
    public int findNetWorkCountByPrjId(String prjId) {
//        StringBuffer hql = new StringBuffer();
//        hql.append("select count(*) from BaseCloudNetWork where prjId = ?");
        int netCount =netWorkDao.getCountByPrjId(prjId);
        int orderCount = getNetworkCountInOrder(prjId);
        return netCount + orderCount;
    }
    /**
     * 获取订单状态为待创建或者创建中的资源个数
     * @author gaoxiang
     * @param prjId
     * @return
     */
    private int getNetworkCountInOrder(String prjId) {
        StringBuffer hql = new StringBuffer();
        hql.append("select ");
        hql.append("   count(*) ");
        hql.append("from ");
        hql.append("   order_info ");
        hql.append("left join ");
        hql.append("   cloudorder_network network ");
        hql.append("on ");
        hql.append("   order_info.order_no = network.order_no ");
        hql.append("where ");
        hql.append("   order_info.order_type = 0 ");
        hql.append("   and order_info.resource_type = 3 ");
        hql.append("   and (order_info.order_state = 1 or order_info.order_state = 2)");
        hql.append("   and network.prj_id = ?");
        Query query = netWorkDao.createSQLNativeQuery(hql.toString(), prjId);
        Object result = query.getSingleResult();
        int orderCount = result == null ? 0 : Integer.parseInt(result.toString());
        return orderCount;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public String getVPCNameById(String id) throws Exception{
        BaseCloudNetwork network = netWorkDao.findOne(id);
        if (network != null) {
            return network.getNetName();
        } else {
            return "";
        }
        /*StringBuffer sb = new StringBuffer();
        sb.append("select net_name from cloud_network where net_id=?");
        Query query = netWorkDao.createSQLNativeQuery(sb.toString(), id);
		List list = query.getResultList();
		String vpnName = "";
		for(int i=0;i<list.size();i++){
			vpnName = (String) query.getResultList().get(0);
		}
        return vpnName;*/
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<CloudNetWork> findNetWorkByCharge(String prjId, String chargeState, String payType, Date endTime) {
        StringBuffer hql = new StringBuffer();
        List<Object> paramsList = new ArrayList<Object>();
        hql.append(" FROM ");
        hql.append("    BaseCloudNetwork network");
        hql.append(" WHERE ");
        hql.append("    1 = 1");
        hql.append(" AND network.isVisible = '1' ");
        if (!StringUtil.isEmpty(prjId)) {
            hql.append("    AND network.prjId = ? ");
            paramsList.add(prjId);
        }
        if (!StringUtil.isEmpty(chargeState)) {
            hql.append("    AND network.chargeState = ? ");
            paramsList.add(chargeState);
        }
        if (!StringUtil.isEmpty(payType)) {
            hql.append("    AND network.payType = ? ");
            paramsList.add(payType);
        }
        if (endTime != null) {
            hql.append("    AND network.endTime <= ? ");
            paramsList.add(endTime);
        }
        List<BaseCloudNetwork> list = netWorkDao.find(hql.toString(), paramsList.toArray());
        List<CloudNetWork> cloudNetWorkList=new ArrayList<CloudNetWork>();
        for (BaseCloudNetwork baseCloudNetwork : list) {
			CloudNetWork cloudNetWork=new CloudNetWork();
			BeanUtils.copyPropertiesByModel(cloudNetWork, baseCloudNetwork);
			cloudNetWorkList.add(cloudNetWork);
		}
        return cloudNetWorkList;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map<String, String> renewNetwork(SessionUserInfo sessionUserInfo, Map param) throws Exception {
        Map<String, String> respMap = new HashMap<>();

        String cusId = sessionUserInfo.getCusId();
        String userId = sessionUserInfo.getUserId();
        String opIp = sessionUserInfo.getIP();
        boolean isUsingBalance = (boolean) param.get("isSelected");

        //前台传入的应付金额
        BigDecimal frontChargeMoney = new BigDecimal(String.valueOf(param.get("chargeMoney")));
        //前台传入的余额支付的金额
        BigDecimal frontBalancePay = new BigDecimal(String.valueOf( param.get("deduction")));
        //前台传入的第三方应支付的金额，鉴于JS精度问题，需要setScale
        BigDecimal frontPayable = new BigDecimal(String.valueOf(param.get("payable")));
        frontPayable = frontPayable.setScale(2, BigDecimal.ROUND_HALF_UP);

        // 通过网络ID查到网络所在路由，得到当前后台路由的实际带宽.
        String netId = (String) param.get("netId");
        BaseCloudRoute route = routeDao.queryByNetworkId(netId);
        int backendRate = route.getRate();
        respMap.put("bandwidth", String.valueOf(backendRate));

        PriceDetails priceDetails = calculatePriceDetailBackend(param,backendRate);
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
            respMap = createNetworkOrder(param, priceDetails.getBandWidthPrice(), backendChargeMoney, frontBalancePay, frontPayable,userId, cusId, opIp);
            if(currentBalance.compareTo(backendChargeMoney)>=0 && frontPayable.compareTo(BigDecimal.ZERO)==0 ){
                //如果使用账户余额可以支付全部的产品金额，则通知前台跳转订单完成页面
                respMap.put(ConstantClazz.SUCCESS_CODE, "BALANCE_PAY_ALL");
            }else{
                //如果使用账户余额支付部分的产品金额，即需要第三方支付，则通知前台跳转支付页面
                respMap.put(ConstantClazz.SUCCESS_CODE, "BALANCE_PAY_PART");
            }
        }else{
            respMap = createNetworkOrder(param, priceDetails.getBandWidthPrice(), backendChargeMoney, frontBalancePay, frontPayable,userId, cusId, opIp);
            if(frontPayable.compareTo(BigDecimal.ZERO)==0){
                respMap.put(ConstantClazz.SUCCESS_CODE, "BALANCE_PAY_ALL");
            }
        }
        return respMap;
    }

    /**
     * 创建网络续费订单
     * @param param 前台参数
     * @param bandWidthPrice 带宽单价
     * @param backendChargeMoney 后台计算的产品金额
     * @param frontBalancePay 前台传入的余额支付金额
     * @param frontPayable 前台传入的第三方应付金额
     * @param userId 用户ID
     * @param cusId 客户ID
     * @return
     * @throws Exception
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Map<String,String> createNetworkOrder(Map param, BigDecimal bandWidthPrice,
                                                  BigDecimal backendChargeMoney, BigDecimal frontBalancePay, BigDecimal frontPayable,
                                                  String userId, String cusId, String opIp) throws Exception {
        Map<String, String> respMap = new HashMap<>();
        String dcName = param.get("dcName")==null?"":param.get("dcName").toString();
        String netId = param.get("netId")==null?"":param.get("netId").toString();
        String netName = param.get("netName") ==null?"":param.get("netName").toString();

        BaseCloudNetwork network = netWorkDao.findOne(netId);
        //fixme network may be null

        Map paramBean = (Map) param.get("paramBean");
        int bandValue = (int)param.get("bandValue");
//        Date endTime = new Date((Long)param.get("endTime"));
//        Date expireTime = new Date((Long)param.get("lastTime"));
        Integer cycle = Integer.valueOf((String)paramBean.get("cycleCount"));
        //在续费时，获取的到期时间，应当以后台资源的到期时间为准进行计算续费后的过期时间，以防前台回退到订单确认页面，重新提交订单。
        Date endTime = network.getEndTime();
        Date expireTime = DateUtil.getExpirationDate(endTime,cycle,DateUtil.RENEWAL);

        Order order = new Order();
        order.setOrderType(OrderType.RENEW);
        order.setProdName("私有网络-续费");
        order.setDcId(String.valueOf(paramBean.get("dcId")));
        order.setProdCount(1);

        StringBuffer prodConfig = new StringBuffer();
        prodConfig.append("数据中心："+dcName)
                .append("<br>私有网络ID："+netId)
                .append("<br>私有网络名称："+netName)
                .append("<br>私有网络带宽："+bandValue + "Mbps");

        order.setProdConfig(prodConfig.toString());
        order.setPayType(PayType.PAYBEFORE);
        order.setUnitPrice(bandWidthPrice);
        order.setBuyCycle(cycle);//续费时长
        order.setResourceType(ResourceType.NETWORK);
        order.setPaymentAmount(backendChargeMoney);//以后台计算的应支付金额为准
        order.setAccountPayment(frontBalancePay);//账户支付金额入参为前台传入的余额支付的金额
        order.setThirdPartPayment(frontPayable);//账户第三方支付应付款金额为前台传入的应付款金额
        order.setUserId(userId);
        order.setCusId(cusId);
        order.setResourceExpireTime(expireTime);

        Map params = new HashMap();
        params.put("resourceId", netId);
        params.put("resourceName", netName);
        params.put("resourceType", ResourceType.NETWORK);
        params.put("expirationDate", endTime);
        params.put("duration", cycle);
        params.put("operatorIp",opIp);
        order.setBusinessParams(params);

        try{
            order = orderService.createOrder(order);
            saveNetworkOrderInfo(order, netId, cycle);
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

    @SuppressWarnings("rawtypes")
    private PriceDetails calculatePriceDetailBackend(Map param, int backendRate) {
        //如果当前带宽已经比前台传入的带宽要大，即续费订单提交前已经完成了升级操作，此时应当取后台实际的带宽计算价格。
        Map paramBean = (Map) param.get("paramBean");
        String dcId = String.valueOf(paramBean.get("dcId"));
        Integer cycle = Integer.valueOf((String)paramBean.get("cycleCount"));
        int frontendRate = (int)param.get("bandValue");

        int bandValue = backendRate>frontendRate?backendRate:frontendRate;
        ParamBean pb = new ParamBean();
        pb.setDcId(dcId);
        pb.setPayType(PayType.PAYBEFORE);
        pb.setNumber(1);
        pb.setCycleCount(cycle);
        pb.setBandValue(bandValue);
        PriceDetails priceDetails = billingFactorService.getPriceDetails(pb);
        return priceDetails;
    }

    private void saveNetworkOrderInfo(Order order, String netId, Integer cycle) throws Exception {
        CloudNetWork net = getNetworkById(netId);
        CloudOrderNetWork orderNet = new CloudOrderNetWork();
        orderNet.setOrderNo(order.getOrderNo());
        orderNet.setOrderType(OrderType.RENEW);
        orderNet.setBuyCycle(cycle);
        orderNet.setPayType(PayType.PAYBEFORE);
        orderNet.setPrice(order.getPaymentAmount());
        orderNet.setNetId(netId);
        orderNet.setNetName(net.getNetName());
        orderNet.setRate(net.getRate());
        orderNet.setCreateName(net.getCreateName());
        orderNet.setCreateTime(net.getCreateTime());
        orderNet.setPrjId(net.getPrjId());
        orderNet.setDcId(net.getDcId());
        orderNet.setCusId(net.getCusId());

        BaseCloudOrderNetWork baseOrderNet = new BaseCloudOrderNetWork();
        BeanUtils.copyProperties(baseOrderNet, orderNet);
        orderNetWorkService.save(baseOrderNet);
    }

    /**
     * 根据netId查询私有网络
     * @author zhangfan
     * @param netId
     * @return
     */
    @SuppressWarnings("rawtypes")
    private CloudNetWork getNetworkById(String netId) {
        CloudNetWork cloudNetWork = new CloudNetWork();
        StringBuffer sql = new StringBuffer();
        sql.append("select");
        sql.append("	cn.net_id");						//0
        sql.append("	,cn.net_name");						//1
        sql.append("	,cn.net_status");					//2
        sql.append("	,cn.admin_stateup");				//3
        sql.append("	,cn.prj_id");						//4
        sql.append("	,cn.dc_id");						//5
        sql.append("	,count(cs.subnet_id) subNetCount");	//6
        sql.append("	,rou.rate");						//7
        sql.append("	,rou.net_name extNetName");			//8
        sql.append("	,rou.route_name");					//9
        sql.append("	,rou.route_id");					//10
        sql.append("	,rou.gateway_ip");					//11
        sql.append("	,cn.pay_type");						//12
        sql.append("	,cn.charge_state");					//13
        sql.append("	,cn.create_time");					//14
        sql.append("	,cn.end_time");						//15
        sql.append("    ,dc.dc_name");                      //16
        sql.append(" from");
        sql.append("	cloud_network cn");
        sql.append(" left outer join cloud_subnetwork cs on cn.net_id=cs.net_id");
        sql.append(" left outer join dc_datacenter dc on cn.dc_id = dc.id");
        sql.append(" left outer join");
        sql.append(" (select cr.rate,cn1.net_name,cr.network_id,cr.route_name,cr.route_id,cr.gateway_ip from cloud_route cr left outer join cloud_network cn1 on cn1.net_id=cr.net_id) rou");
        sql.append(" on rou.network_id=cn.net_id");
        sql.append(" where cn.router_external='0' and cn.net_id=?");

        Query query = netWorkDao.createSQLNativeQuery(sql.toString(), netId);
        List resultList = query.getResultList();
        if(null!=resultList&&resultList.size()==1){
            Object [] objs = (Object [])resultList.get(0);
            cloudNetWork.setNetId(String.valueOf(objs[0]));
            cloudNetWork.setNetName(String.valueOf(objs[1]));
            cloudNetWork.setNetStatus(String.valueOf(objs[2]));
            cloudNetWork.setAdminStateup(String.valueOf(objs[3]));
            cloudNetWork.setAdminStaName(String.valueOf(objs[3]).equals("1")?"UP":"DOWN");
            cloudNetWork.setPrjId(String.valueOf(objs[4]));
            cloudNetWork.setDcId(String.valueOf(objs[5]));
            cloudNetWork.setSubNetCount(Integer.valueOf(String.valueOf(objs[6])));
            cloudNetWork.setRate(Integer.valueOf(String.valueOf(objs[7]==null?0:objs[7])));
            cloudNetWork.setExtNetName(objs[8]==null ? null:String.valueOf(objs[8]));
            cloudNetWork.setRouteName(String.valueOf(objs[9]));
            cloudNetWork.setRouteId(objs[10]==null?null:String.valueOf(objs[10]));
            cloudNetWork.setGatewayIp(objs[11]==null?null:String.valueOf(objs[11]));
            /* 用户中心改版计费相关 */
            cloudNetWork.setPayType(String.valueOf(objs[12]));
            cloudNetWork.setChargeState(String.valueOf(objs[13]));
            cloudNetWork.setCreateTime((Date)objs[14]);
            cloudNetWork.setEndTime((Date)objs[15]);
            cloudNetWork.setDcName(String.valueOf(objs[16]));
            cloudNetWork.setPayTypeStr(CloudResourceUtil.escapePayType(cloudNetWork.getPayType()));
            if ("0".equals(cloudNetWork.getChargeState())) {
                cloudNetWork.setNetStatusName(DictUtil.getStatusByNodeEn("netWork", cloudNetWork.getNetStatus()));
            } else {
                cloudNetWork.setNetStatusName(CloudResourceUtil.escapseChargeState(cloudNetWork.getChargeState()));
            }
        }
        return cloudNetWork;
    }
	@SuppressWarnings("rawtypes")
    @Override
	public Map<String, Object> findNetWorkByRouteId(String routeId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" select ");
		sql.append(" cn.net_name as netName ");
		sql.append(" ,cn.charge_state as chargeState ");
		sql.append(" ,cr.gateway_ip as gatewayIp ");
		sql.append(" from cloud_network cn");
		sql.append(" left join cloud_route cr on cn.net_id = cr.network_id ");
		sql.append(" where cr.route_id = ?");
		Query query = netWorkDao.createSQLNativeQuery(sql.toString(), routeId);
        List resultList = query.getResultList();
        Map<String, Object> map = new HashMap<String, Object>();
        int index = 0;
        if(null!=resultList && resultList.size()==1){
        	Object [] objs = (Object [])resultList.get(0);
        	index = 0;
        	map.put("netName", objs[index++]);
        	map.put("chargeState", objs[index++]);
        	map.put("gatewayIp", objs[index++]);
        }
		return map;
	}
	@Override
	public boolean isExistsByOrderNo(String orderNo) {
	    CloudOrderNetWork orderNetwork = orderNetWorkService.getOrderNetWorkByOrderNo(orderNo);
	    return isExistsByResourceId(orderNetwork.getNetId()).isExisted();
	}
	@Override
	public ResourceCheckBean isExistsByResourceId(String resourceId) {
	    ResourceCheckBean resourceCheckBean = new ResourceCheckBean();
	    StringBuffer hql = new StringBuffer();
	    hql.append(" from BaseCloudNetwork where netId = ? and isVisible = 1 ");
	    BaseCloudNetwork network = (BaseCloudNetwork)netWorkDao.findUnique(hql.toString(), new Object[]{resourceId});
	    if (network != null) {
	        resourceCheckBean.setResourceName(network.getNetName());
	        resourceCheckBean.setExisted(true);
	    } else {
	        resourceCheckBean.setExisted(false);
	    }
	    return resourceCheckBean;
	}
	@Override
	public List<CloudNetWork> getNetworkListByPrjIdAndRouteIdNotNull (String prjId) {
	    List<CloudNetWork> list = new ArrayList<CloudNetWork>();
	    StringBuffer sql = new StringBuffer();
	    sql.append(" SELECT                                ");
	    sql.append("   network.net_id                      ");
	    sql.append("   ,network.net_name                   ");
	    sql.append("   ,route.gateway_ip                   ");
	    sql.append(" FROM cloud_network network            ");
	    sql.append(" LEFT JOIN cloud_route route           ");
	    sql.append(" ON network.net_id = route.network_id  ");
	    sql.append(" WHERE network.prj_id = ?              ");
	    sql.append("   AND network.router_external = '0'   ");
	    sql.append("   AND charge_state = '0'              ");
	    sql.append("   AND is_visible = '1'                ");
	    sql.append("   AND route.gateway_ip is not null    ");
	    Query query = netWorkDao.createSQLNativeQuery(sql.toString(), new Object[] {prjId});
	    List result = query.getResultList();
	    for (int i = 0; i < result.size(); i++) {
	        Object[] objs = (Object [])result.get(i);
	        CloudNetWork network = new CloudNetWork();
	        network.setNetId(String.valueOf(objs[0]));
	        network.setNetName(String.valueOf(objs[1]));
	        network.setGatewayIp(String.valueOf(objs[2]));
	        list.add(network);
	    }
	    return list;
	}
	
}
