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
import com.eayun.common.constant.ResourceSyncConstant;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.BoolUtil;
import com.eayun.common.util.CloudResourceUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.CompletionHook;
import com.eayun.costcenter.model.MoneyAccount;
import com.eayun.costcenter.service.AccountOverviewService;
import com.eayun.eayunstack.model.Pool;
import com.eayun.eayunstack.service.OpenstackPoolService;
import com.eayun.monitor.bean.MonitorAlarmUtil.MonitorResourceType;
import com.eayun.monitor.service.AlarmService;
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
import com.eayun.virtualization.dao.CloudLdMemberDao;
import com.eayun.virtualization.dao.CloudLdPoolDao;
import com.eayun.virtualization.model.BaseCloudFloatIp;
import com.eayun.virtualization.model.BaseCloudLdPool;
import com.eayun.virtualization.model.BaseCloudLdVip;
import com.eayun.virtualization.model.BaseCloudOrderLdPool;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.CloudFloatIp;
import com.eayun.virtualization.model.CloudLdPool;
import com.eayun.virtualization.model.CloudLdVip;
import com.eayun.virtualization.model.CloudOrderLdPool;
import com.eayun.virtualization.service.CloudFloatIpService;
import com.eayun.virtualization.service.CloudOrderLdPoolService;
import com.eayun.virtualization.service.PoolService;
import com.eayun.virtualization.service.TagService;
import com.eayun.virtualization.service.VipService;

/**
 * PoolServiceImpl
 *
 * @Filename: PoolServiceImpl.java
 * @Description:
 * @Version: 1.0
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br>
 * 				<li>Date: 2015年11月11日</li>
 *               <li>Version: 1.0</li>
 *               <li>Content: create</li>
 *
 */
@Service
@Transactional
public class PoolServiceImpl implements PoolService {
	private static final Logger log = LoggerFactory.getLogger(PoolServiceImpl.class);
	@Autowired
	private CloudLdPoolDao poolDao;
	@Autowired
	private CloudLdMemberDao memberDao;
	@Autowired
	private VipService vipService;
	@Autowired
	private OpenstackPoolService openStackService;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private TagService tagService;
	@Autowired
	private EayunRabbitTemplate rabbitTemplate;
	@Autowired
	private OrderService orderService;
	@Autowired
	private AccountOverviewService accountOverviewSerivce;
	@Autowired
	private BillingFactorService billingFactorService;
	@Autowired
	private MessageCenterService messageCenterService;
	@Autowired
	private CloudOrderLdPoolService orderLdPoolService;
	@Autowired
	private CloudFloatIpService floatIpService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private SysDataTreeService sysDataTreeService;
	@Autowired
	private AlarmService alarmService;

	private Order getOrderBeforeByOrderLdPool(CloudOrderLdPool cloudOrderPool, String orderType, String resourceType, String userId){
		Order order = new Order();
		order.setOrderType(orderType);
		StringBuffer prodConfig = new StringBuffer();
		if (OrderType.NEW.equals(order.getOrderType())) {
		    order.setProdName("负载均衡器-包年包月");
		    order.setBuyCycle(cloudOrderPool.getBuyCycle());
		    prodConfig.append("数据中心：" + cloudOrderPool.getDcName())
            .append("<br>负载均衡器名称：" + cloudOrderPool.getPoolName())
            .append("<br>最大连接数：" + cloudOrderPool.getConnectionLimit());
		} else if (OrderType.UPGRADE.equals(order.getOrderType())) {
		    order.setProdName("负载均衡器-更改连接数");
		    prodConfig.append("数据中心：" + cloudOrderPool.getDcName())
            .append("<br>负载均衡器ID：" + cloudOrderPool.getPoolId()) 
            .append("<br>负载均衡器名称：" + cloudOrderPool.getPoolName())
            .append("<br>原最大连接数：" + cloudOrderPool.getConnectionLimitOld())
		    .append("<br>调整后最大连接数：" + cloudOrderPool.getConnectionLimit());
		} else if (OrderType.RENEW.equals(order.getOrderType())) {
		    order.setProdName("负载均衡器-续费");
		    order.setBuyCycle(cloudOrderPool.getBuyCycle());
		}
		order.setProdCount(1);
		order.setProdConfig(prodConfig.toString());
		order.setDcId(cloudOrderPool.getDcId());
		order.setPayType(cloudOrderPool.getPayType());
		order.setResourceType(resourceType);
		order.setResourceExpireTime(DateUtil.getExpirationDate(new Date(), cloudOrderPool.getBuyCycle(), DateUtil.PURCHASE));
		order.setUnitPrice(cloudOrderPool.getPrice());
		order.setPaymentAmount(cloudOrderPool.getPrice());
		order.setAccountPayment(cloudOrderPool.getAccountPayment());
		order.setThirdPartPayment(cloudOrderPool.getThirdPartPayment());
		order.setUserId(userId);
		order.setCusId(cloudOrderPool.getCusId());
		return order;
	}

	private Order getOrderAfterByOrderLdPool(CloudOrderLdPool cloudOrderPool, String orderType, String resourceType, String userId){
		Order order = new Order();
		order.setOrderType(orderType);
		StringBuffer prodConfig = new StringBuffer();
		if (OrderType.NEW.equals(order.getOrderType())) {
		    order.setProdName("负载均衡器-按需付费");
		    prodConfig.append("数据中心：" + cloudOrderPool.getDcName())
            .append("<br>负载均衡器名称：" + cloudOrderPool.getPoolName())
            .append("<br>最大连接数：" + cloudOrderPool.getConnectionLimit());
		} else if (OrderType.UPGRADE.equals(order.getOrderType())) {
		    order.setProdName("负载均衡器-更改连接数");
		    prodConfig.append("数据中心：" + cloudOrderPool.getDcName())
            .append("<br>负载均衡器ID：" + cloudOrderPool.getPoolId()) 
            .append("<br>负载均衡器名称：" + cloudOrderPool.getPoolName())
            .append("<br>原最大连接数：" + cloudOrderPool.getConnectionLimitOld())
            .append("<br>调整后最大连接数：" + cloudOrderPool.getConnectionLimit());
		}
		order.setProdCount(1);
		order.setProdConfig(prodConfig.toString());
		order.setDcId(cloudOrderPool.getDcId());
		order.setPayType(cloudOrderPool.getPayType());
		order.setBillingCycle(BillingCycleType.HOUR);
		order.setResourceType(resourceType);
//		order.setUnitPrice(cloudOrderPool.getPrice());
//		order.setPaymentAmount(cloudOrderPool.getPrice());
//		order.setAccountPayment(cloudOrderPool.getAccountPayment());
//		order.setThirdPartPayment(cloudOrderPool.getThirdPartPayment());
		order.setUserId(userId);
		order.setCusId(cloudOrderPool.getCusId());
		return order;
	}
	/**
	 * order拼装CloudLdPool
	 * @author gaoxiang
	 * @param cloudOrderPool
	 * @return
	 */
	private CloudLdPool assembleModelByOrder(CloudOrderLdPool cloudOrderPool) {
        CloudLdPool pool = new CloudLdPool();
        pool.setPoolId(cloudOrderPool.getPoolId());
        pool.setDcId(cloudOrderPool.getDcId());
        pool.setPrjId(cloudOrderPool.getPrjId());
        pool.setPayType(cloudOrderPool.getPayType());
        
        return pool;
    }
	/**
	 * 查询负载均衡器的列表
	 *
	 * @author zhouhaitao
	 * @param page
	 * @param datacenterId
	 * @param projectId
	 * @param name
	 * @param queryMap
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Page getPoolList(Page page, String datacenterId, String projectId, String name, QueryMap queryMap)
			throws Exception {
		StringBuffer sql = new StringBuffer();
		Object[] args = new Object[10];
		int index = 0;

		sql.append("	SELECT                                                                  ");
		sql.append("		pool.dc_id,                                                         ");
		sql.append("		dc.dc_name,                                                         ");
		sql.append("		pool.prj_id,                                                        ");
		sql.append("		pool.pool_id,                                                       ");
		sql.append("		pool.pool_name,                                                     ");
		sql.append("		pool.pool_status,                                                   ");
		/* 用户中心改版计费相关 */
		sql.append("		pool.pay_type,  	                                                ");
		sql.append("		pool.charge_state,                                                  ");
		sql.append("		pool.create_time,                                                   ");
		sql.append("		pool.end_time, 	    	                                            ");

		sql.append("		pool.subnet_id,                                                     ");
		sql.append("		sub.subnet_name,                                                    ");
		sql.append("		sub.cidr,                                                           ");
		sql.append("		vip.vip_id,                                                         ");
		sql.append("		vip.vip_address,                                                    ");
		sql.append("		vip.port_id,                                                        ");
		sql.append("		vip.vip_protocol,                                                   ");
		sql.append("		vip.protocol_port,                                                  ");
		sql.append("		vip.connection_limit,                                               ");
		sql.append("		floatip.flo_id,                                                     ");
		sql.append("		floatip.flo_ip,                                                     ");
		sql.append("		ldmem.memberCount,                                                  ");
		sql.append("		CASE                                                                ");
		sql.append("	WHEN ldpm.monitorCount > 0 THEN                                         ");
		sql.append("		'true'                                                              ");
		sql.append("	ELSE                                                                    ");
		sql.append("		'false'                                                             ");
		sql.append("	END AS isCheckMonitor,                                                   ");
		sql.append("		pool.mode                                                   ");
		sql.append("	FROM                                                                    ");
		sql.append("		cloud_ldpool pool                                                   ");
		sql.append("	LEFT JOIN dc_datacenter dc ON dc.id = pool.dc_id                        ");
		sql.append("	LEFT JOIN cloud_ldvip vip ON vip.pool_id = pool.pool_id                 ");
		sql.append("	LEFT JOIN cloud_subnetwork sub ON sub.subnet_id = pool.subnet_id                 ");
		sql.append("	LEFT JOIN cloud_floatip floatip ON pool.pool_id = floatip.resource_id   ");
		sql.append("	AND floatip.resource_type = 'lb'                                        ");
		sql.append("	AND floatip.is_deleted = '0'                                            ");
		sql.append("	LEFT JOIN (                                                             ");
		sql.append("		SELECT                                                              ");
		sql.append("			count(1) memberCount,                                           ");
		sql.append("			pool_id                                                         ");
		sql.append("		FROM                                                                ");
		sql.append("			cloud_ldmember                                                  ");
		sql.append("		GROUP BY                                                            ");
		sql.append("			pool_id                                                         ");
		sql.append("	) ldmem ON ldmem.pool_id = pool.pool_id                                 ");
		sql.append("	LEFT JOIN (                                                             ");
		sql.append("		SELECT                                                              ");
		sql.append("			count(1) AS monitorCount,                                       ");
		sql.append("			pool_id                                                         ");
		sql.append("		FROM                                                                ");
		sql.append("			cloud_ldpoolldmonitor                                           ");
		sql.append("		GROUP BY                                                            ");
		sql.append("			pool_id                                                         ");
		sql.append("	) ldpm ON ldpm.pool_id = pool.pool_id                                   ");
		sql.append("	WHERE                                                                   ");
		sql.append("		1 = 1                                                               ");
		sql.append("	AND pool.is_visible = 1                                                 ");
		sql.append("	AND pool.prj_id = ?                                                     ");
		args[index++] = projectId;
		if(!StringUtils.isEmpty(name)){
			sql.append("	AND  pool.pool_name like ?                                          ");
			name = name.replaceAll("\\_", "\\\\_");
			args[index++] = "%" + name + "%";
		}
		sql.append("order by pool.create_time desc ");

		Object[] params = new Object[index];
		System.arraycopy(args, 0, params, 0, index);
		page = poolDao.pagedNativeQuery(sql.toString(), queryMap, params);
		List newList = (List) page.getResult();

		for (int i = 0; i < newList.size(); i++) {
			Object[] objs = (Object[]) newList.get(i);
			int resIndex = 0;
			CloudLdPool pool = new CloudLdPool();
			pool.setDcId((String)objs[resIndex++]);
			pool.setDcName((String)objs[resIndex++]);
			pool.setPrjId((String)objs[resIndex++]);
			pool.setPoolId((String)objs[resIndex++]);
			pool.setPoolName((String)objs[resIndex++]);
			pool.setPoolStatus((String)objs[resIndex++]);
			/* 用户中心改版计费相关 */
			pool.setPayType(String.valueOf(objs[resIndex++]));
			pool.setChargeState(String.valueOf(objs[resIndex++]));
			pool.setCreateTime((Date)objs[resIndex++]);
			pool.setEndTime((Date)objs[resIndex++]);

			pool.setSubnetId((String)objs[resIndex++]);
			pool.setSubnetName((String)objs[resIndex++]);
			pool.setSubnetCidr((String)objs[resIndex++]);
			pool.setVipId((String)objs[resIndex++]);
			pool.setSubnetIp((String)objs[resIndex++]);
			pool.setPortId((String)objs[resIndex++]);
			pool.setPoolProtocol((String)objs[resIndex++]);
			pool.setVipPort(Long.parseLong(objs[resIndex++] != null ? String.valueOf(objs[resIndex-1]) : "0"));
			pool.setConnectionLimit(Long.parseLong(objs[resIndex++] != null ? String.valueOf(objs[resIndex-1]) : "0"));
			pool.setFloatId((String)objs[resIndex++]);
			pool.setFloatIp((String)objs[resIndex++]);
			pool.setCount(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex-1]) : "0"));
			pool.setCheckMonitor(Boolean.parseBoolean(String.valueOf(objs[resIndex++])));
			pool.setMode(String.valueOf(objs[resIndex++]));
			pool.setStatusForPool(DictUtil.getStatusByNodeEn("ldPool", pool.getPoolStatus()));
			String tag=tagService.getResourceTagForShowcase("ldPool", pool.getPoolId());
			pool.setMonitorStatus("未开启");
			if(pool.isCheckMonitor()){
				pool.setMonitorStatus("已开启");
			}
			pool.setTagName(tag);
			/* 用户中心改版计费相关 */
			pool.setPayTypeStr(CloudResourceUtil.escapePayType(pool.getPayType()));
			if (CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(pool.getChargeState())) {
				pool.setStatusForPool(DictUtil.getStatusByNodeEn("ldPool", pool.getPoolStatus()));
            } else {
            	pool.setStatusForPool(CloudResourceUtil.escapseChargeState(pool.getChargeState()));
            }

			newList.set(i, pool);
		}
		return page;

	}

	/**
	 * 校验重名
	 *
	 * @param pool
	 * @return
	 */
	public boolean checkPoolNameExsit(CloudLdPool pool) {
		StringBuffer hql = new StringBuffer();
		Object[] params = new Object[5];
		int index = 0;
		hql.append(" from BaseCloudLdPool where 1=1 ");
		hql.append(" and binary(poolName) = ? ");
		hql.append(" and prjId =? ");
		params[index++] = pool.getPoolName();
		params[index++] = pool.getPrjId();
		if (!StringUtils.isEmpty(pool.getPoolId())) {
			hql.append(" and poolId <> ? ");
			params[index++] = pool.getPoolId();
		}
		Object[] args = new Object[index];
		System.arraycopy(params, 0, args, 0, index);
		@SuppressWarnings("unchecked")
		List<BaseCloudLdPool> list = poolDao.find(hql.toString(), args);
		
		if (list == null || list.size() == 0) {
		    return checkPoolNameInOrder(pool.getPoolName(), pool.getPrjId());
		} else {
		    return false;
		}

//		return list == null || list.size() == 0;
	}
	/**
	 * 校验名称在订单状态为待创建或者创建中的资源中是否存在
	 * @author gaoxiang
	 * @param poolName
	 * @param prjId
	 * @return
	 */
	private boolean checkPoolNameInOrder(String poolName, String prjId) {
	    StringBuffer sql = new StringBuffer();
        sql.append("select ");
        sql.append("  count(*) ");
        sql.append("from ");
        sql.append("  order_info ");
        sql.append("left join ");
        sql.append("  cloudorder_ldpool pool ");
        sql.append("on ");
        sql.append("  order_info.order_no = pool.order_no ");
        sql.append("where ");
        sql.append("  order_info.order_type = 0 ");
        sql.append("  and order_info.resource_type = 4");
        sql.append("  and (order_info.order_state = 1 or order_info.order_state = 2)");
        sql.append("  and pool.pool_name = ? ");
        sql.append("  and pool.prj_id = ? ");
        List<Object> values = new ArrayList<Object>();
        values.add(poolName);
        values.add(prjId);
        Query query = poolDao.createSQLNativeQuery(sql.toString(), values.toArray());
        String result = query.getSingleResult().toString();
        return result.equals("null") || result.equals("0");
	}
	/**
     * 获取价格的后台接口
     * @author gaoxiang
     * @param cloudOrderPool
     * @return
     */
    private BigDecimal getTotalPrice(CloudOrderLdPool cloudOrderPool) {
        BigDecimal price = new BigDecimal(0);
        if (OrderType.UPGRADE.equals(cloudOrderPool.getOrderType()) && PayType.PAYBEFORE.equals(cloudOrderPool.getPayType())) {
            UpgradeBean upgradeBean=new UpgradeBean();
            upgradeBean.setDcId(cloudOrderPool.getDcId());
            upgradeBean.setOldConnCount(cloudOrderPool.getConnectionLimitOld());
            upgradeBean.setNewConnCount(cloudOrderPool.getConnectionLimit());
            if (null != cloudOrderPool.getPoolId()) {
                BaseCloudLdPool pool = poolDao.findOne(cloudOrderPool.getPoolId());
                upgradeBean.setCycleCount(DateUtil.getUgradeRemainDays(new Date(), pool.getEndTime()));
            } else {
                upgradeBean.setCycleCount(DateUtil.getUgradeRemainDays(new Date(), cloudOrderPool.getEndTime()));
            }
            price = billingFactorService.updateConfigPrice(upgradeBean);
        } else {
            ParamBean paramBean = new ParamBean();
            paramBean.setDcId(cloudOrderPool.getDcId());
            paramBean.setPayType(cloudOrderPool.getPayType());
            paramBean.setNumber(1);
            paramBean.setConnCount(cloudOrderPool.getConnectionLimit());
            paramBean.setCycleCount(cloudOrderPool.getBuyCycle());
            price = billingFactorService.getPriceByFactor(paramBean);
        }
        if (PayType.PAYBEFORE.equals(cloudOrderPool.getPayType())) {
            price = price.setScale(2, BigDecimal.ROUND_FLOOR);
        }
        return price;
    }
	@Override
	public String buyBalancer(CloudOrderLdPool cloudOrderPool, SessionUserInfo sessionUser) throws Exception {
	    String userId = sessionUser.getUserId();
	    String cusId = sessionUser.getCusId();
		cloudOrderPool.setCreateName(sessionUser.getUserName());
		cloudOrderPool.setCusId(cusId);
		/*配额校验*/
		if (getPoolQuotasByPrjId(cloudOrderPool.getPrjId()) < 1) {
		    return "OUT_OF_QUOTA";
		/*后付费余额校验*/
		} else if (PayType.PAYAFTER.equals(cloudOrderPool.getPayType())) {
		    MoneyAccount accountMoney = accountOverviewSerivce.getAccountInfo(cusId);
		    String buyCondition = sysDataTreeService.getBuyCondition();
            BigDecimal createResourceLimitedMoney = new BigDecimal(buyCondition);
		    if (accountMoney.getMoney().compareTo(createResourceLimitedMoney) < 0) {
                return "NOT_SUFFICIENT_FUNDS";
            }
		/*预付费价格变动*/
		} else if (PayType.PAYBEFORE.equals(cloudOrderPool.getPayType())) {
		    BigDecimal totalPrice = getTotalPrice(cloudOrderPool);
		    if (cloudOrderPool.getPrice().compareTo(totalPrice) != 0) {
		        return "CHANGE_OF_BILLINGFACTORY";
		    }
		}
		try {
			if(PayType.PAYBEFORE.equals(cloudOrderPool.getPayType())){
				/* 拼装Order类 */
				Order order = getOrderBeforeByOrderLdPool(cloudOrderPool, OrderType.NEW, ResourceType.QUOTAPOOL, userId);
				/* 调用订单创建接口 */
				Order reorder = orderService.createOrder(order);
				cloudOrderPool.setOrderNo(reorder.getOrderNo());
				BaseCloudOrderLdPool orderPool = new BaseCloudOrderLdPool();
				BeanUtils.copyPropertiesByModel(orderPool, cloudOrderPool);
				orderPool.setCreateTime(new Date());
				orderLdPoolService.save(orderPool);
			} else {
				/* 拼装Order类 */
				Order order = getOrderAfterByOrderLdPool(cloudOrderPool, OrderType.NEW, ResourceType.QUOTAPOOL, userId);
				/* 调用订单创建接口 */
				Order reorder = orderService.createOrder(order);
				cloudOrderPool.setOrderNo(reorder.getOrderNo());
				BaseCloudOrderLdPool orderPool = new BaseCloudOrderLdPool();
                BeanUtils.copyPropertiesByModel(orderPool, cloudOrderPool);
                orderPool.setCreateTime(new Date());
                orderLdPoolService.save(orderPool);
				CloudOrderLdPool result = new CloudOrderLdPool();
				try {
				    result = createBalancer(cloudOrderPool, sessionUser);
				} catch (AppException e) {
				    log.error(e.getMessage(), e);
				    throw new Exception(e);
				}
				if(result != null) {
				    /* 调用消息队列发送接口 */
                    /*ParamBean param = new ParamBean();
                    param.setConnCount(result.getConnectionLimit());
                    ChargeRecord chargeRecord = new ChargeRecord();
                    chargeRecord.setParam(param);
                    chargeRecord.setDatecenterId(result.getDcId());
                    chargeRecord.setOrderNumber(reorder.getOrderNo());
                    chargeRecord.setCusId(cusId);
                    chargeRecord.setResourceId(result.getPoolId());
                    chargeRecord.setResourceType(ResourceType.QUOTAPOOL);
                    chargeRecord.setChargeFrom(new Date());
                    rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_PURCHASE, JSONObject.toJSONString(chargeRecord));*/
				}
			}
		} catch (Exception e) {
		    log.error(e.toString(),e);
			throw e;
		}
		return null;
	}
	/**
	 * 创建负载均衡器
	 *
	 * @author zhouhaitao
	 * @throws AppException
	 */
	/* (non-Javadoc)
	 * @see com.eayun.virtualization.service.PoolService#createBalancer(com.eayun.virtualization.model.CloudLdPool)
	 */
	@Override
	@Transactional(noRollbackFor=AppException.class)
	public CloudOrderLdPool createBalancer(CloudOrderLdPool cloudOrderPool ,SessionUserInfo sessionUser) throws AppException {
		int createStep = 0;
	    cloudOrderPool.setPoolProvider("haproxy");
		/*if (checkPrjQuota(cloudOrderPool.getPrjId())) {
			throw new AppException("error.openstack.message", new String[] { "负载均衡数量超过项目规定限额，请先申请配额！" });
		}*/
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		try {
			temp.put("name", cloudOrderPool.getPoolName());
			temp.put("protocol", cloudOrderPool.getPoolProtocol());
			temp.put("subnet_id", cloudOrderPool.getSubnetId());
			temp.put("lb_method", cloudOrderPool.getLbMethod());
			temp.put("admin_state_up", "1");
			data.put("pool", temp);
			Pool result = openStackService.create(cloudOrderPool.getDcId(), cloudOrderPool.getPrjId(), data);
			createStep++;
			if (result != null) {
				BaseCloudLdPool cloudLdpool = new BaseCloudLdPool();
				cloudLdpool.setPoolId(result.getId());
				cloudLdpool.setPoolName(result.getName());
				cloudLdpool.setPrjId(cloudOrderPool.getPrjId());
				cloudLdpool.setDcId(cloudOrderPool.getDcId());
				cloudLdpool.setPoolProvider(result.getProvider());
				cloudLdpool.setSubnetId(result.getSubnet_id());
				cloudLdpool.setPoolProtocol(result.getProtocol());
				cloudLdpool.setLbMethod(result.getLb_method());
				cloudLdpool.setPoolStatus(result.getStatus().toUpperCase());
				cloudLdpool.setAdminStateup(BoolUtil.bool2char(result.isAdmin_state_up()));
				cloudLdpool.setCreateName(cloudOrderPool.getCreateName());
				cloudLdpool.setCreateTime(new Date());

				cloudLdpool.setPayType(cloudOrderPool.getPayType());
				cloudLdpool.setEndTime(DateUtil.getExpirationDate(cloudOrderPool.getCreateTime(), cloudOrderPool.getBuyCycle(), DateUtil.PURCHASE));
				cloudLdpool.setChargeState("0");
				
				cloudLdpool.setIsVisible("0");
				cloudLdpool.setMode(cloudOrderPool.getMode());
				poolDao.saveOrUpdate(cloudLdpool);

				cloudOrderPool.setPoolId(result.getId());
				orderLdPoolService.update(cloudOrderPool.getPoolId(), cloudOrderPool.getOrderNo());
				//原本这里是需要判断底层返回状态,根据状态是否为ACTIVE来判断是否需要放入同步消息队列或直接修改订单状态,这次改动将判断负载均衡状态与vip状态放在一起,当pool与vip都为active时,才修改订单状态
				CloudLdVip vip = new CloudLdVip();
				vip.setDcId(cloudOrderPool.getDcId());
				vip.setPrjId(cloudOrderPool.getPrjId());
				vip.setVipName("vip_" + System.currentTimeMillis());
				vip.setConnectionLimit(cloudOrderPool.getConnectionLimit());
				vip.setVipProtocol(cloudOrderPool.getPoolProtocol());
				vip.setCreateName(cloudOrderPool.getCreateName());
				vip.setCreateTime(new Date());
				vip.setSubnetId(cloudOrderPool.getSubnetId());
				vip.setPoolId(cloudLdpool.getPoolId());
				vip.setProtocolPort(cloudOrderPool.getVipPort());

				BaseCloudLdVip baseVip = vipService.addVip(vip);
				if (null != result.getStatus() && !"ACTIVE".equals(result.getStatus())) {
					JSONObject json = new JSONObject();
					json.put("orderNo", cloudOrderPool.getOrderNo());
					json.put("poolId", cloudLdpool.getPoolId());
					json.put("dcId", cloudLdpool.getDcId());
					json.put("prjId", cloudLdpool.getPrjId());
					json.put("connectionLimit", cloudOrderPool.getConnectionLimit());
					json.put("poolStatus", cloudLdpool.getPoolStatus());
					json.put("cusId", sessionUser.getCusId());
					json.put("count", "0");
					json.put("vipId", baseVip.getVipId());
					jedisUtil.push(RedisKey.ldPoolKey, json.toJSONString());
				}else if(null != result.getStatus() && "ACTIVE".equals(result.getStatus())){
					JSONObject json = new JSONObject();
					json.put("orderNo", cloudOrderPool.getOrderNo());
					json.put("poolId", cloudLdpool.getPoolId());
					json.put("dcId", cloudLdpool.getDcId());
					json.put("connectionLimit", cloudOrderPool.getConnectionLimit());
					json.put("poolStatus", cloudLdpool.getPoolStatus());
					json.put("cusId", sessionUser.getCusId());
					jedisUtil.set(RedisKey.CLOUDLDPOOLSYNC+result.getId(), json.toJSONString());
				}
				if(baseVip != null){
					if("ACTIVE".equals(baseVip.getVipStatus())&&"ACTIVE".equals(cloudLdpool.getPoolStatus())){//在vip和pool状态都为ACTIVE时才修改订单状态为已完成
				    cloudLdpool.setIsVisible("1");
				    poolCreateSuccessHandle(cloudLdpool.getDcId(), 
				            cloudOrderPool.getOrderNo(), 
				            sessionUser.getCusId(), 
				            result.getId(), 
				            result.getName(), 
				            cloudOrderPool.getConnectionLimit(), 
				            cloudOrderPool.getPayType());
					    jedisUtil.delete(RedisKey.CLOUDLDPOOLSYNC+result.getId());
					    jedisUtil.delete(RedisKey.CLOUDLDVIPSYNC+baseVip.getVipId());
				}
	                /*增加vipid入库逻辑*/
	                cloudLdpool.setVipId(baseVip.getVipId());
	                poolDao.saveOrUpdate(cloudLdpool);
	                createStep++;
	                return cloudOrderPool;
				}
			}
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage());
			log.error(e.toString(),e);
			throw new AppException("error.openstack.message");
		} finally {
		    try {
		        if (createStep <= 1) {
		            poolCreateFailedHandle(cloudOrderPool, createStep);
		        }
		    } catch (Exception e) {
		        log.error(e.toString(),e);
		        throw new AppException("orderException");
		    }
		}
		return null;
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
	 * 创建负载均衡器失败后的业务处理
	 * @author gaoxiang
	 * @param cloudOrderPool
	 * @param createStep
	 */
	private void poolCreateFailedHandle(CloudOrderLdPool cloudOrderPool, int createStep) throws Exception {
        orderService.completeOrder(cloudOrderPool.getOrderNo(), false, null);
        messageCenterService.addResourFailMessage(cloudOrderPool.getOrderNo(), cloudOrderPool.getCusId());
        try {
            if (createStep == 1) {
                CloudLdPool cloudPool = assembleModelByOrder(cloudOrderPool);
                deleteBalancer(cloudPool);
            }
        } catch (Exception e) {
            List<MessageOrderResourceNotice> list = new ArrayList<MessageOrderResourceNotice>();
            MessageOrderResourceNotice orderRe = new MessageOrderResourceNotice();
            orderRe.setResourceName(cloudOrderPool.getPoolName());
            orderRe.setResourceType(ResourceType.getName(ResourceType.QUOTAPOOL));
            list.add(orderRe);
            messageCenterService.delecteResourFailMessage(list, cloudOrderPool.getOrderNo());
        }
	}
	/**
	 * 创建负载均衡器成功后的业务处理
	 * @author gaoxiang
	 * @param orderNo      订单编号
	 * @param poolId       资源id
	 * @param poolName     资源名称
	 * @throws Exception
	 */
	public void poolCreateSuccessHandle(String dcId, String orderNo, String cusId, String poolId, String poolName, Long connectionLimit, String payType) throws Exception {
	    if (PayType.PAYAFTER.equals(payType)) {
	        /* 调用消息队列发送接口 */
	        ParamBean param = new ParamBean();
	        param.setConnCount(connectionLimit);
	        ChargeRecord chargeRecord = new ChargeRecord();
	        chargeRecord.setParam(param);
	        chargeRecord.setDatecenterId(dcId);
	        chargeRecord.setOrderNumber(orderNo);
	        chargeRecord.setCusId(cusId);
	        chargeRecord.setResourceId(poolId);
	        chargeRecord.setResourceName(poolName);
	        chargeRecord.setResourceType(ResourceType.QUOTAPOOL);
	        chargeRecord.setChargeFrom(new Date());
	        rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_PURCHASE, JSONObject.toJSONString(chargeRecord));
	    }
	    /* 调用订单完成接口 */
	    List<BaseOrderResource> resourceList = new ArrayList<BaseOrderResource>();
        BaseOrderResource resource = new BaseOrderResource();
        resource.setOrderNo(orderNo);
        resource.setResourceId(poolId);
        resource.setResourceName(poolName);
        resourceList.add(resource);
        orderService.completeOrder(orderNo, true, resourceList);
	}
	
	public String changeBalancer(CloudOrderLdPool cloudOrderPool, SessionUserInfo sessionUser) throws Exception {
		String userId = sessionUser.getUserId();
		String cusId = sessionUser.getCusId();
		cloudOrderPool.setCreateName(sessionUser.getUserName());
		cloudOrderPool.setCusId(cusId);
		/*校验余额是否够按需付费条件*/
        if (PayType.PAYAFTER.equals(cloudOrderPool.getPayType())) {
            MoneyAccount accountMoney = accountOverviewSerivce.getAccountInfo(cusId);
            BigDecimal zero = new BigDecimal(0);
            if (accountMoney.getMoney().compareTo(zero) < 0) {
                return "BALANCE_OF_ARREARS";
            }
        /*预付费价格变动*/
        } else if (PayType.PAYBEFORE.equals(cloudOrderPool.getPayType())) {
            BigDecimal totalPrice = getTotalPrice(cloudOrderPool);
            if (cloudOrderPool.getPrice().compareTo(totalPrice) != 0) {
                return "CHANGE_OF_BILLINGFACTORY";
            }
        }
        /*校验该资源是否正在被续费或升级*/
        if (checkLbOrderExist(cloudOrderPool.getPoolId())) {
            return "UPGRADING_OR_RENEWING";
        }
        /*校验该资源当前配置是否已被更改*/
        if (checkPoolConfiguration(cloudOrderPool)) {
            return "CHANGE_OF_CONFIGURATION";
        }
		try {
			if(PayType.PAYBEFORE.equals(cloudOrderPool.getPayType())) {
				/* 拼装Order类 */
				Order order = getOrderBeforeByOrderLdPool(cloudOrderPool, OrderType.UPGRADE, ResourceType.QUOTAPOOL, userId);
				/* 调用订单创建接口 */
				Order reorder = orderService.createOrder(order);
				cloudOrderPool.setOrderNo(reorder.getOrderNo());
				BaseCloudOrderLdPool orderPool = new BaseCloudOrderLdPool();
				BeanUtils.copyPropertiesByModel(orderPool, cloudOrderPool);
				orderPool.setCreateTime(new Date());
				orderLdPoolService.save(orderPool);
			} else {
				/* 拼装Order类 */
				Order order = getOrderAfterByOrderLdPool(cloudOrderPool, OrderType.UPGRADE, ResourceType.QUOTAPOOL, userId);
				/* 调用订单创建接口 */
				Order reorder = orderService.createOrder(order);
				cloudOrderPool.setOrderNo(reorder.getOrderNo());
				BaseCloudOrderLdPool orderPool = new BaseCloudOrderLdPool();
                BeanUtils.copyPropertiesByModel(orderPool, cloudOrderPool);
                orderPool.setCreateTime(new Date());
                orderLdPoolService.save(orderPool);
				/* 调用负载均衡修改配置接口 */
				try {
				    updateBalancer(cloudOrderPool);
				} catch (AppException e) {
				    log.error(e.getMessage(), e);
				    throw new Exception(e);
				}
				/* 调用消息队列发送接口 */
				ParamBean param = new ParamBean();
				param.setConnCount(cloudOrderPool.getConnectionLimit());
				ChargeRecord chargeRecord = new ChargeRecord();
				chargeRecord.setParam(param);
				chargeRecord.setDatecenterId(cloudOrderPool.getDcId());
				chargeRecord.setOrderNumber(reorder.getOrderNo());
				chargeRecord.setCusId(cusId);
				chargeRecord.setResourceId(cloudOrderPool.getPoolId());
				chargeRecord.setResourceType(ResourceType.QUOTAPOOL);
				chargeRecord.setChargeFrom(new Date());
				rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_UPGRADE, JSONObject.toJSONString(chargeRecord));
			}
		} catch(Exception e) {
		    log.error(e.toString(),e);
			throw e;
		}
		return null;
	}
	@Override
	public CloudLdPool updateBalancerName(CloudLdPool cloudPool) throws AppException {
	    try {
	        JSONObject data = new JSONObject();
            JSONObject temp = new JSONObject();
            temp.put("name", cloudPool.getPoolName());
            data.put("pool", temp);
            Pool result = openStackService.update(cloudPool.getDcId(), cloudPool.getPrjId(), data, cloudPool.getPoolId());
//            CloudLdPool cloudLdpoolVoe = null;
            if (result != null) {
                BaseCloudLdPool cloudLdpool = poolDao.findOne(cloudPool.getPoolId());
                cloudLdpool.setPoolName(result.getName());
                cloudLdpool.setAdminStateup(BoolUtil.bool2char(result.isAdmin_state_up()));
                cloudLdpool.setPoolStatus(result.getStatus());
                poolDao.saveOrUpdate(cloudLdpool);
                if (null != result.getStatus() && !"ACTIVE".equals(result.getStatus())) {
                    JSONObject json = new JSONObject();
                    json.put("poolId", cloudLdpool.getPoolId());
                    json.put("dcId", cloudLdpool.getDcId());
                    json.put("prjId", cloudLdpool.getPrjId());
                    json.put("poolStatus", cloudLdpool.getPoolStatus());
                    json.put("count", "0");
                    jedisUtil.addUnique(RedisKey.ldPoolKey, json.toJSONString());
                }
            }
	    } catch (AppException e) {
	        log.error(e.toString(),e);
	        throw e;
	    }
	    return cloudPool;
	}
	/**
	 * 修改负载均衡器
	 *
	 * @author zhouhaitao
	 * @param cloudOrderPool
	 * @throws AppException
	 */
	@Transactional(noRollbackFor=AppException.class)
	public CloudOrderLdPool updateBalancer(CloudOrderLdPool cloudOrderPool) throws AppException {
	    int updateStep = 0;
	    try {
	        /*JSONObject data = new JSONObject();
	        JSONObject temp = new JSONObject();
	        temp.put("name", cloudOrderPool.getPoolName());
	        data.put("pool", temp);
	        Pool result = openStackService.update(cloudOrderPool.getDcId(), cloudOrderPool.getPrjId(), data, cloudOrderPool.getPoolId());
	        updateStep++;*/
//	        CloudLdPool cloudLdpoolVoe = null;
//	        if (result != null) {
	            BaseCloudLdPool cloudLdpool = poolDao.findOne(cloudOrderPool.getPoolId());
//	            cloudLdpool.setPoolName(result.getName());
//	            cloudLdpool.setAdminStateup(BoolUtil.bool2char(result.isAdmin_state_up()));
//	            cloudLdpool.setPoolStatus(result.getStatus());
//	            poolDao.saveOrUpdate(cloudLdpool);
//	            cloudLdpoolVoe = new CloudLdPool();
//	            BeanUtils.copyPropertiesByModel(cloudLdpoolVoe, cloudLdpool);
	            
	            /*if (null != result.getStatus() && !"ACTIVE".equals(result.getStatus())) {
	                JSONObject json = new JSONObject();
	                json.put("poolId", cloudLdpool.getPoolId());
	                json.put("dcId", cloudLdpool.getDcId());
	                json.put("prjId", cloudLdpool.getPrjId());
	                json.put("poolStatus", cloudLdpool.getPoolStatus());
	                json.put("count", "0");
	                jedisUtil.addUnique(RedisKey.ldPoolKey, json.toJSONString());
	            }*/
	            
	            if(!StringUtils.isEmpty(cloudOrderPool.getVipId())){
	                CloudLdVip vip = new CloudLdVip();
	                vip.setVipId(cloudOrderPool.getVipId());
	                vip.setDcId(cloudOrderPool.getDcId());
	                vip.setPrjId(cloudOrderPool.getPrjId());
	                vip.setConnectionLimit(cloudOrderPool.getConnectionLimit());
	                vipService.modifyVip(vip);
	            }
	            updateStep++;
//	        }
	    } catch (AppException e) {
	        log.error(e.toString(),e);
	        throw e;
	    } finally {
	        try {
	            poolUpdateHandle(cloudOrderPool, updateStep);
	        } catch (Exception e) {
	            log.error(e.toString(),e);
	            throw new AppException("orderException");
	        }
	    }

		return cloudOrderPool;
	}
	/**
     * 变配负载均衡器后的业务处理
     * @author gaoxiang
     * @param cloudOrderPool
     * @param createStep
     */
	private void poolUpdateHandle(CloudOrderLdPool cloudOrderPool, int updateStep) throws Exception {
	    List<BaseOrderResource> resourceList = new ArrayList<BaseOrderResource>();
	    BaseOrderResource resource = new BaseOrderResource();
	    resource.setOrderNo(cloudOrderPool.getOrderNo());
	    resource.setResourceId(cloudOrderPool.getPoolId());
	    resource.setResourceName(cloudOrderPool.getPoolName());
	    resourceList.add(resource);
	    /*由于资源订单数据库中没有到期时间，所以通过订单中的资源id查询资源数据库，获取资源到期时间*/
	    CloudLdPool pool = getLoadBalanceById(cloudOrderPool.getPoolId());
	    if (updateStep > 0) {
	        /*if (PayType.PAYAFTER.equals(cloudOrderPool.getPayType())) {
	             调用消息队列发送接口 
	            ParamBean param = new ParamBean();
	            param.setConnCount(cloudOrderPool.getConnectionLimit());
	            ChargeRecord chargeRecord = new ChargeRecord();
	            chargeRecord.setParam(param);
	            chargeRecord.setDatecenterId(cloudOrderPool.getDcId());
	            chargeRecord.setOrderNumber(cloudOrderPool.getOrderNo());
	            chargeRecord.setCusId(cloudOrderPool.getCusId());
	            chargeRecord.setResourceId(cloudOrderPool.getPoolId());
	            chargeRecord.setResourceType(ResourceType.QUOTAPOOL);
	            chargeRecord.setChargeFrom(new Date());
	            rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_UPGRADE, JSONObject.toJSONString(chargeRecord));
	        }*/
            orderService.completeOrder(cloudOrderPool.getOrderNo(), true, resourceList, false, pool.getEndTime());
	    } else {
	        orderService.completeOrder(cloudOrderPool.getOrderNo(), false, resourceList);
            messageCenterService.addResourFailMessage(cloudOrderPool.getOrderNo(), cloudOrderPool.getCusId());
	    }
	}
	/**
	 * 删除负载均衡器
	 *
	 * @author zhouhaitao
	 * @param pool
	 * @return
	 */
	public boolean deleteBalancer(CloudLdPool pool) {
		/**
    	 * 判断资源是否有未完成的订单 --@author zhouhaitao
    	 */
		if(checkLbOrderExist(pool.getPoolId())){
			throw new AppException("该资源有未完成的订单，请取消订单后再进行删除操作！");
		}
		boolean flag = false;
		boolean vipFlag = false;
		if(checkFloatIpByBalancer(pool)){
			throw new AppException("error.openstack.message", new String[] {"已绑定了弹性公网IP，需解绑后操作" });
		}
		int deleteStep = 0;
		try {
			jedisUtil.set(RedisKey.DELETE_POOL+pool.getPoolId(), "1");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		try {
		    if(!StringUtils.isEmpty(pool.getVipId())){
		        CloudLdVip cloudLdVip = new CloudLdVip();
		        cloudLdVip.setDcId(pool.getDcId());
		        cloudLdVip.setPrjId(pool.getPrjId());
		        cloudLdVip.setVipId(pool.getVipId());
		        
		        vipFlag = vipService.deleteVip(cloudLdVip);
		        
		    }
		    deleteStep++;
		    if(vipFlag||StringUtils.isEmpty(pool.getVipId())){
		        boolean poolFlag = openStackService.delete(pool.getDcId(), pool.getPrjId(), pool.getPoolId());
		        deleteStep++;
		        if (poolFlag) {
		            poolDao.delete(pool.getPoolId());
		            //TODO 调用彬彬清理异常信息接口
		            alarmService.clearPoolMsgAfterDeletePool(pool.getPoolId());
		            alarmService.deleteMonitorByResource(MonitorResourceType.POOL.toString(), pool.getPoolId());
		            deleteMemberAndMonitor(pool);
		            
		            // 删除资源后更新缓存接口
		            tagService.refreshCacheAftDelRes("ldPool", pool.getPoolId());
		            flag = true;
		        }
		    }
		} catch (AppException e) {
		    log.error(e.toString(),e);
		    throw e;
		} finally {
		    poolDeleteHandle(pool, deleteStep);
		}
		return flag;
	}
	/**
	 * 删除后的业务处理
	 * @author gaoxiang
	 * @param pool
	 * @param deleteStep
	 */
	private void poolDeleteHandle(CloudLdPool pool, int deleteStep){
	    if (deleteStep > 1) {
	        if(PayType.PAYAFTER.equals(pool.getPayType())) {
                ChargeRecord chargeRecord = new ChargeRecord();
                chargeRecord.setDatecenterId(pool.getDcId());
                chargeRecord.setResourceId(pool.getPoolId());
                chargeRecord.setResourceName(pool.getPoolName());;
                chargeRecord.setResourceType(ResourceType.QUOTAPOOL);
                chargeRecord.setCusId(pool.getCusId());
                chargeRecord.setOpTime(new Date());
                rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_DELETE, JSONObject.toJSONString(chargeRecord));
            }
	    }
	    /*else {
	        List<MessageOrderResourceNotice> list = new ArrayList<MessageOrderResourceNotice>();
	        MessageOrderResourceNotice orderRe = new MessageOrderResourceNotice();
	        orderRe.setResourceName(pool.getPoolName());
	        orderRe.setResourceType(ResourceType.QUOTAPOOL);
	        list.add(orderRe);
	        messageCenterService.delecteResourFailMessage(list, pool.getOrderNo());
	    }*/
	}
	/**
	 * 续费负载均衡器
	 *
	 * @author gaoxiang
	 * @param cloudOrderPool
	 * @param sessionUser
	 * @return
	 */
	@Override
	public boolean renewBalancer(CloudOrderLdPool cloudOrderPool, SessionUserInfo sessionUser) throws Exception {
		boolean flag = false;
		String userId = sessionUser.getUserId();
		try {
			Order order = getOrderBeforeByOrderLdPool(cloudOrderPool, OrderType.RENEW, ResourceType.QUOTAPOOL, userId);
			Order reorder = orderService.createOrder(order);
			BaseCloudOrderLdPool orderPool = new BaseCloudOrderLdPool();
			BeanUtils.copyPropertiesByModel(orderPool, cloudOrderPool);
			orderPool.setOrderNo(reorder.getOrderNo());
			orderPool.setCreateName(sessionUser.getCusName());
			orderPool.setCusId(sessionUser.getCusId());
			orderPool.setCreateTime(new Date());
			orderLdPoolService.save(orderPool);
		} catch(Exception e) {
			throw e;
		}
		return flag;
	}
	@Override
	public void modifyStateForLdPool(String resourceId, String chargeState, Date endTime, boolean isRestrict, boolean isResumable) {
		BaseCloudLdPool pool = poolDao.findOne(resourceId);
		BaseCloudProject project = projectService.findProject(pool.getPrjId());
		pool.setChargeState(chargeState);
		if(endTime != null) {
			pool.setEndTime(endTime);
		}
		poolDao.save(pool);
		if(isRestrict) {
            /* 拼装CloudFloatIp类，调用解绑接口 */
            BaseCloudFloatIp floatIp = getFloatIpByPoolId(resourceId);
            CloudFloatIp cloudFloatIp = new CloudFloatIp();
            if(null != floatIp){
                BeanUtils.copyPropertiesByModel(cloudFloatIp, floatIp);
                floatIpService.unbundingResource(cloudFloatIp);
            }
            /* 调用消息队列的发送接口 */
            ChargeRecord chargeRecord = new ChargeRecord();
            chargeRecord.setDatecenterId(pool.getDcId());
            chargeRecord.setResourceId(pool.getPoolId());
            chargeRecord.setResourceType(ResourceType.QUOTAPOOL);
            chargeRecord.setCusId(project.getCustomerId());
            chargeRecord.setOpTime(new Date());
            rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_RESTRICT, JSONObject.toJSONString(chargeRecord));
		} else {
			if(isResumable) {
				/* 调用消息队列的发送接口 */
				ChargeRecord chargeRecord = new ChargeRecord();
				chargeRecord.setDatecenterId(pool.getDcId());
				chargeRecord.setResourceId(pool.getPoolId());
				chargeRecord.setResourceType(ResourceType.QUOTAPOOL);
				chargeRecord.setCusId(project.getCustomerId());
				chargeRecord.setOpTime(new Date());
				rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_RECOVER, JSONObject.toJSONString(chargeRecord));
			}
		}
	}
	
	@Override
    public BigDecimal getPrice(CloudOrderLdPool cloudOrderLdPool) {
        return getTotalPrice(cloudOrderLdPool);
    }
	
	/**
	 * 查询负载均衡器的详情
	 *
	 * @author zhouhaitao
	 * @param poolId
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public CloudLdPool getLoadBalanceById(String poolId){
		CloudLdPool pool = null;
		StringBuffer sql = new StringBuffer();

		sql.append("	SELECT                                                                  ");
		sql.append("		pool.dc_id,                                                         ");
		sql.append("		dc.dc_name,                                                         ");
		sql.append("		pool.prj_id,                                                        ");
		sql.append("		cp.prj_name,                                                        ");
		sql.append("		pool.pool_id,                                                       ");
		sql.append("		pool.pool_name,                                                     ");
		sql.append("		pool.pool_status,                                                   ");
		sql.append("		pool.lb_method,                                                     ");
		sql.append("		vip.vip_id,                                                         ");
		sql.append("		vip.vip_address,                                                    ");
		sql.append("		vip.vip_protocol,                                                   ");
		sql.append("		vip.connection_limit,                                               ");
		sql.append("		vip.protocol_port,                                                  ");
		sql.append("		floatip.flo_ip,                                                     ");
		sql.append("		subnet.net_name,                                                    ");
		sql.append("		subnet.subnet_name,                                                 ");
		sql.append("		subnet.subnet_id,                                                   ");
		sql.append("		subnet.cidr,                                                        ");
		sql.append("		pool.pay_type,                                                      ");
		sql.append("		pool.charge_state,                                                  ");
		sql.append("		pool.create_time,                                                   ");
		sql.append("		pool.end_time,                                                      ");
		sql.append("		CASE                                                                ");
		sql.append("	WHEN ldpm.monitorCount > 0 THEN                                         ");
		sql.append("		'true'                                                              ");
		sql.append("	ELSE                                                                    ");
		sql.append("		'false'                                                             ");
		sql.append("	END AS isCheckMonitor,                                                   ");
		sql.append("		pool.mode                                                   ");
		sql.append("	FROM                                                                    ");
		sql.append("		cloud_ldpool pool                                                   ");
		sql.append("	LEFT JOIN cloud_ldvip vip ON vip.pool_id = pool.pool_id                 ");
		sql.append("	LEFT JOIN cloud_floatip floatip ON pool.pool_id = floatip.resource_id   ");
		sql.append("	AND floatip.resource_type = 'lb'                                        ");
		sql.append("	AND floatip.is_deleted = '0'                                            ");
		sql.append("	LEFT JOIN (                                                             ");
		sql.append("		SELECT                                                              ");
		sql.append("			sub.subnet_id,                                                  ");
		sql.append("			sub.subnet_name,                                                ");
		sql.append("			sub.cidr,                                                       ");
		sql.append("			net.net_name                                                    ");
		sql.append("		FROM                                                                ");
		sql.append("			cloud_subnetwork sub                                            ");
		sql.append("		LEFT JOIN cloud_network net ON net.net_id = sub.net_id              ");
		sql.append("	) subnet ON subnet.subnet_id = pool.subnet_id                           ");
		sql.append("	LEFT JOIN (                                                             ");
		sql.append("		SELECT                                                              ");
		sql.append("			count(1) AS monitorCount,                                       ");
		sql.append("			pool_id                                                         ");
		sql.append("		FROM                                                                ");
		sql.append("			cloud_ldpoolldmonitor                                           ");
		sql.append("		GROUP BY                                                            ");
		sql.append("			pool_id                                                         ");
		sql.append("	) ldpm ON ldpm.pool_id = pool.pool_id                                   ");
		sql.append("	LEFT JOIN dc_datacenter dc ON dc.id = pool.dc_id                        ");
		sql.append("	LEFT JOIN cloud_project cp ON cp.prj_id = pool.prj_id                   ");
		sql.append("	WHERE pool.pool_id = ?                                                  ");

		Query query = poolDao.createSQLNativeQuery(sql.toString(), new Object[] {poolId});
		List listResult = query.getResultList();
		if(null!=listResult&&listResult.size()==1){
			Object [] obj = (Object [])listResult.get(0);
			int index = 0;

			pool = new CloudLdPool();
			pool.setDcId((String)obj[index++]);
			pool.setDcName((String)obj[index++]);
			pool.setPrjId((String)obj[index++]);
			pool.setPrjName((String)obj[index++]);
			pool.setPoolId((String)obj[index++]);
			pool.setPoolName((String)obj[index++]);
			pool.setPoolStatus((String)obj[index++]);
			pool.setLbMethod((String)obj[index++]);
			pool.setVipId((String)obj[index++]);
			pool.setSubnetIp((String)obj[index++]);
			pool.setPoolProtocol((String)obj[index++]);
			pool.setConnectionLimit(Long.parseLong(obj[index++] != null ? String.valueOf(obj[index-1]) : "0"));
			pool.setVipPort(Long.parseLong(obj[index++] != null ? String.valueOf(obj[index-1]) : "0"));
			pool.setFloatIp((String)obj[index++]);
			pool.setNetName((String)obj[index++]);
			pool.setSubnetName((String)obj[index++]);
			pool.setSubnetId((String)obj[index++]);
			pool.setSubnetCidr((String)obj[index++]);
			pool.setPayType(String.valueOf(obj[index++]));
			pool.setChargeState(String.valueOf(obj[index++]));
			pool.setCreateTime((Date)obj[index++]);
			pool.setEndTime((Date)obj[index++]);
			pool.setCheckMonitor(Boolean.parseBoolean(String.valueOf(obj[index++])));
			pool.setMode(String.valueOf(obj[index++]));
			pool.setStatusForPool(DictUtil.getStatusByNodeEn("ldPool", pool.getPoolStatus()));
			pool.setLbMethodCn(DictUtil.getStatusByNodeEn("ldType", pool.getLbMethod()));
			String tag=tagService.getResourceTagForShowcase("ldPool", pool.getPoolId());
			pool.setMonitorStatus("未开启");
			if(pool.isCheckMonitor()){
				pool.setMonitorStatus("已开启");
			}
			pool.setTagName(tag);
			/*状态业务+计费*/
			pool.setPayTypeStr(CloudResourceUtil.escapePayType(pool.getPayType()));
            if (CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(pool.getChargeState())) {
                pool.setStatusForPool(DictUtil.getStatusByNodeEn("ldPool", pool.getPoolStatus()));
            } else {
                pool.setStatusForPool(CloudResourceUtil.escapseChargeState(pool.getChargeState()));
            }
		}
		return pool;
	}
	/**
	 * 检验升级订单的基础配置是否为最新配置
	 * @author gaoxiang
	 * @param cloudOrderPool
	 * @return
	 */
	private boolean checkPoolConfiguration(CloudOrderLdPool cloudOrderPool) {
	    StringBuffer hql = new StringBuffer();
	    hql.append(" select ");
	    hql.append("   vip.connectionLimit ");
	    hql.append(" from ");
	    hql.append("   BaseCloudLdPool pool, BaseCloudLdVip vip ");
	    hql.append(" where ");
	    hql.append("   pool.poolId = vip.poolId ");
	    hql.append("   and pool.poolId = ? ");
	    Long connectionLimit = (Long) poolDao.findUnique(hql.toString(), new Object[]{cloudOrderPool.getPoolId()});
	    return !connectionLimit.equals(cloudOrderPool.getConnectionLimitOld());
	}
	/**
	 * 判断负载均衡器是否超过项目配额
	 *
	 * @author zhouhaitao
	 * @param prjId
	 * @return
	 */
	private boolean checkPrjQuota(String prjId) {
		boolean flag = false;
		StringBuffer sql = new StringBuffer();

		sql.append(" select  ");
		sql.append("   cp.quota_pool,");
		sql.append("   p.usedCount");
		sql.append(" from cloud_project cp");
		sql.append(" left join   ");
		sql.append("   (");
		sql.append("     select count(1) as usedCount,prj_id from cloud_ldpool ");
		sql.append("     where prj_id = ? ");
		sql.append("   ) p on p.prj_id = cp.prj_id ");
		sql.append(" where cp.prj_id = ?  ");

		Query query = poolDao.createSQLNativeQuery(sql.toString(), new Object[] { prjId, prjId });
		@SuppressWarnings("rawtypes")
		List list = query.getResultList();
		if (null != list && list.size() == 1) {
			Object[] obj = (Object[]) list.get(0);
			int poolQuota = Integer.parseInt(obj[0] == null ? "0" : String.valueOf(obj[0]));
			int usedCount = Integer.parseInt(obj[1] == null ? "0" : String.valueOf(obj[1]));
			if (usedCount >= poolQuota) {
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * 判断负载均衡器是否绑定浮动IP
	 *
	 * @author zhouhaitao
	 * @param pool
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private boolean checkFloatIpByBalancer(CloudLdPool pool){
		StringBuffer hql = new StringBuffer ();

		hql.append("  from BaseCloudFloatIp where resourceType = ? ");
		hql.append("  and isDeleted = ? ");
		hql.append("  and resourceId = ? ");

		List floatList = memberDao.find(hql.toString(), new Object[]{"lb","0",pool.getPoolId()});

		return null!=floatList&&floatList.size()>0 ;
	}

	/**
	 * 级联删除负载均衡器成员和健康检查
	 *
	 * @author zhouhaitao
	 * @param pool
	 * @return
	 */
	private boolean deleteMemberAndMonitor(final CloudLdPool pool){
		boolean flag = false;
		try{
			StringBuffer deleteMember = new StringBuffer();
			StringBuffer deleteMonitor = new StringBuffer();

			deleteMember.append("delete BaseCloudLdMember where poolId = ?");
			deleteMonitor.append("delete BaseCloudLdPoolMonitor where poolId = ?");

			memberDao.executeUpdate(deleteMember.toString(), pool.getPoolId());
			memberDao.executeUpdate(deleteMonitor.toString(), pool.getPoolId());
			TransactionHookUtil.registAfterCompletionHook(new CompletionHook() {
				
				@Override
				public void execute(int status) {
					try {
						if(0==status){
							jedisUtil.set(RedisKey.DELETE_POOL+pool.getPoolId(), "2");
						}else{
							jedisUtil.delete(RedisKey.DELETE_POOL+pool.getPoolId());
						}
					} catch (Exception e) {
						log.error(e.getMessage(),e);
					}
				}
			});
			flag =true;
		}catch(Exception e){
			flag = false;
			log.error(e.toString(),e);
			throw e;
		}
		return flag;
	}
	@Override
	public int getPoolQuotasByPrjId(String prjId) {
	    BaseCloudProject basePrj = projectService.findProject(prjId);
	    int poolUsed = getCountByPrjId(prjId);
	    return basePrj.getQuotaPool() - poolUsed;
	}
	/*
	 * 根据prjId查询个数
	 */
	public int getCountByPrjId(String prjId) {
		int poolCount = poolDao.getCountByPrjId(prjId);
		int orderCount = getPoolCountInOrder(prjId);
		return poolCount + orderCount;
	}
	/**
	 * 获取订单状态为待创建或者创建中的资源的个数
	 * @author gaoxiang
	 * @param prjId
	 * @return
	 */
	private int getPoolCountInOrder(String prjId) {
	    StringBuffer sql = new StringBuffer();
        sql.append("select ");
        sql.append("  count(*) ");
        sql.append("from ");
        sql.append("  order_info ");
        sql.append("left join ");
        sql.append("  cloudorder_ldpool pool ");
        sql.append("on ");
        sql.append("  order_info.order_no = pool.order_no ");
        sql.append("where ");
        sql.append("  order_info.order_type = 0 ");
        sql.append("  and order_info.resource_type = 4");
        sql.append("  and (order_info.order_state = 1 or order_info.order_state = 2)");
        sql.append("  and pool.prj_id = ?");
        Query query = poolDao.createSQLNativeQuery(sql.toString(), prjId);
        Object result = query.getSingleResult();
        int orderCount = result == null ? 0 : Integer.parseInt(result.toString());
        return orderCount; 
	}

	// 根据poolId获取对象
	public BaseCloudLdPool getPoolByPoolId(String poolId) {
		return poolDao.findOne(poolId);
	}

	// 更新entity
	public void updatePool(BaseCloudLdPool entity) {
		poolDao.saveOrUpdate(entity);
	}

	public boolean updateLdp(CloudLdPool cloudLdp) {
		boolean flag = false;
		try {
			BaseCloudLdPool ldp = poolDao.findOne(cloudLdp.getPoolId());
			ldp.setPoolStatus(cloudLdp.getPoolStatus());
			poolDao.saveOrUpdate(ldp);
			flag = true;
		} catch (Exception e) {
		    log.error(e.toString(),e);
			flag = false;
		}
		return flag;
	}
	@Override
	public String getLBNameById(String id) throws Exception{
	    BaseCloudLdPool pool = poolDao.findOne(id);
	    if (pool != null) {
	        return pool.getPoolName();
	    } else {
	        return "";
	    }
        /*StringBuffer sb = new StringBuffer();
        sb.append("select pool_name from cloud_ldpool where pool_id = ?");
        Query query = poolDao.createSQLNativeQuery(sb.toString(), id);
		List list = query.getResultList();
		String lbName = "";
		for(int i=0; i<list.size(); i++){
			lbName = (String)list.get(i);
		}
		return lbName;*/
	}

	private BaseCloudFloatIp getFloatIpByPoolId(String poolId) {
		return poolDao.getFloatIpByPoolId(poolId);
	}
	
	public List<CloudLdPool> findLdPoolByCharge(String prjId, String chargeState, String payType, Date endTime) {
	    StringBuffer hql = new StringBuffer();
        List<Object> paramsList = new ArrayList<Object>();
        hql.append(" FROM ");
        hql.append("    BaseCloudLdPool pool");
        hql.append(" WHERE ");
        hql.append("    1 = 1");
        hql.append("  AND pool.isVisible='1' ");
        if (!StringUtil.isEmpty(prjId)) {
            hql.append("    AND pool.prjId = ? ");
            paramsList.add(prjId);
        }
        if (!StringUtil.isEmpty(chargeState)) {
            hql.append("    AND pool.chargeState = ? ");
            paramsList.add(chargeState);
        }
        if (!StringUtil.isEmpty(payType)) {
            hql.append("    AND pool.payType = ? ");
            paramsList.add(payType);
        }
        if (endTime != null) {
            hql.append("    AND pool.endTime <= ? ");
            paramsList.add(endTime);
        }
        List<BaseCloudLdPool> list = poolDao.find(hql.toString(), paramsList.toArray());
        List<CloudLdPool> cloudLdPoolList=new ArrayList<CloudLdPool>();
        for (BaseCloudLdPool baseCloudLdPool : list) {
			CloudLdPool cloudLdPool=new CloudLdPool();
			BeanUtils.copyPropertiesByModel(cloudLdPool, baseCloudLdPool);
			cloudLdPoolList.add(cloudLdPool);
		}
        return cloudLdPoolList;
	}

    @Override
    public Map<String, String> renewBalancer(SessionUserInfo sessionUserInfo, Map param) throws Exception {
        log.info("续费负载均衡器开始");
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

        String poolId = (String) param.get("poolId");
        CloudLdPool pool = getLoadBalanceById(poolId);
        long backendConnLimit = pool.getConnectionLimit();
        respMap.put("connections",String.valueOf(backendConnLimit));

        //获取map中的计费因子，重新计算应支付金额
        PriceDetails priceDetails = calculatePriceDetailBackend(param, backendConnLimit);
        BigDecimal backendChargeMoney = priceDetails.getTotalPrice();
        backendChargeMoney = backendChargeMoney.setScale(2, BigDecimal.ROUND_FLOOR);
        //如果前台传入应支付金额与后台计算的应支付金额不想等，则无法提交订单，前台提示用户
        if(frontChargeMoney.compareTo(backendChargeMoney)!=0){
            respMap.put(ConstantClazz.WARNING_CODE,"您的订单金额或资源配置发生变动，请重新确认订单!");
            return respMap;
        }

        if(isUsingBalance){
            //如果使用余额支付，则需要后台获取当前账户余额判断与“余额支付”金额的大小关系
            //如果当前余额小于前台传入的余额支付金额，则无法提交订单，前台提示用户
            BigDecimal currentBalance = accountOverviewSerivce.getAccountInfo(cusId).getMoney();
            if(currentBalance.compareTo(frontBalancePay)<0){
                respMap.put(ConstantClazz.WARNING_CODE,"您的余额发生变动，请重新确认订单!");
                return respMap;
            }
            //3.创建订单
            respMap = createBalancerOrder(param, priceDetails.getPoolPrice(), backendChargeMoney, frontBalancePay, frontPayable, userId, cusId, opIp);
            if(currentBalance.compareTo(backendChargeMoney)>=0  && frontPayable.compareTo(BigDecimal.ZERO)==0 ) {
                //如果使用账户余额可以支付全部的产品金额，则通知前台跳转订单完成页面
                respMap.put(ConstantClazz.SUCCESS_CODE, "BALANCE_PAY_ALL");
            }else{
                //如果使用账户余额支付部分的产品金额，即需要第三方支付，则通知前台跳转支付页面
                respMap.put(ConstantClazz.SUCCESS_CODE, "BALANCE_PAY_PART");
            }
        }else{
            respMap = createBalancerOrder(param, priceDetails.getPoolPrice(), backendChargeMoney, frontBalancePay, frontPayable, userId, cusId, opIp);
            if(frontPayable.compareTo(BigDecimal.ZERO)==0){
                respMap.put(ConstantClazz.SUCCESS_CODE, "BALANCE_PAY_ALL");
            }
        }
        return respMap;
    }

    /**
     * 创建负载均衡续费订单
     * @param param 前台参数
     * @param poolPrice 负载均衡单价（最大连接数）
     * @param backendChargeMoney 后台计算的产品金额
     * @param frontBalancePay 前台传入的余额支付金额
     * @param frontPayable 前台传入的第三方应付金额
     * @param userId 用户ID
     * @param cusId 客户ID
     * @return
     * @throws Exception
     */
    private Map<String,String> createBalancerOrder(Map param, BigDecimal poolPrice,
                                                   BigDecimal backendChargeMoney, BigDecimal frontBalancePay, BigDecimal frontPayable,
                                                   String userId, String cusId, String opIp) throws Exception {
        Map<String, String> respMap = new HashMap<>();
        String dcName = param.get("dcName")==null?"":param.get("dcName").toString();
        String poolId = param.get("poolId")==null?"":param.get("poolId").toString();
        String poolName = param.get("poolName") ==null?"":param.get("poolName").toString();

        BaseCloudLdPool lb = poolDao.findOne(poolId);

        Map paramBean = (Map) param.get("paramBean");
        int connLimit = (int)param.get("connLimit");
        Integer cycle = Integer.valueOf((String)paramBean.get("cycleCount"));
//        Date endTime = new Date((Long)param.get("endTime"));
//        Date expireTime = new Date((Long)param.get("lastTime"));
        Date endTime = lb.getEndTime();
        Date expireTime = DateUtil.getExpirationDate(endTime, cycle, DateUtil.RENEWAL);

        Order order = new Order();
        order.setOrderType(OrderType.RENEW);
        order.setProdName("负载均衡器-续费");
        order.setDcId(String.valueOf(paramBean.get("dcId")));
        order.setProdCount(1);

        StringBuffer prodConfig = new StringBuffer();
        prodConfig.append("数据中心："+dcName)
                .append("<br>负载均衡器ID："+poolId)
                .append("<br>负载均衡器名称："+poolName)
                .append("<br>最大连接数："+connLimit);
        order.setProdConfig(prodConfig.toString());
        order.setPayType(PayType.PAYBEFORE);
        order.setUnitPrice(poolPrice);
        order.setBuyCycle(cycle);//续费时长
        order.setResourceType(ResourceType.QUOTAPOOL);
        order.setPaymentAmount(backendChargeMoney);//以后台计算的应支付金额为准
        order.setAccountPayment(frontBalancePay);//账户支付金额入参为前台传入的余额支付的金额
        order.setThirdPartPayment(frontPayable);//账户第三方支付应付款金额为前台传入的应付款金额
        order.setUserId(userId);
        order.setCusId(cusId);
        order.setResourceExpireTime(expireTime);

        Map params = new HashMap();
        params.put("resourceId", poolId);
        params.put("resourceName", poolName);
        params.put("resourceType", ResourceType.QUOTAPOOL);
        params.put("expirationDate", endTime);
        params.put("duration", cycle);
        params.put("operatorIp", opIp);
        order.setBusinessParams(params);

        try{
            order = orderService.createOrder(order);
            saveLdPoolOrderInfo(order, poolId, cycle);
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

    private PriceDetails calculatePriceDetailBackend(Map param, long backendConnLimit) {
        Map paramBean = (Map) param.get("paramBean");
        String dcId = String.valueOf(paramBean.get("dcId"));
        Integer cycle = Integer.valueOf((String)paramBean.get("cycleCount"));
        int frontConnLimit = (int)param.get("connLimit");
        long connLimit = backendConnLimit>frontConnLimit?backendConnLimit:frontConnLimit;

        ParamBean pb = new ParamBean();
        pb.setDcId(dcId);
        pb.setPayType(PayType.PAYBEFORE);
        pb.setNumber(1);
        pb.setCycleCount(cycle);
        pb.setConnCount(connLimit);
        PriceDetails priceDetails = billingFactorService.getPriceDetails(pb);
        return priceDetails;
    }

    private void saveLdPoolOrderInfo(Order order, String poolId, int cycle) throws Exception {
        CloudLdPool ldPool = getLoadBalanceById(poolId);
        CloudOrderLdPool orderPool = new CloudOrderLdPool();
        orderPool.setOrderNo(order.getOrderNo());
        orderPool.setOrderType(OrderType.RENEW);
        orderPool.setBuyCycle(cycle);
        orderPool.setPayType(PayType.PAYBEFORE);
        orderPool.setPrice(order.getPaymentAmount());
        orderPool.setPoolId(poolId);
        orderPool.setPoolName(ldPool.getPoolName());
        orderPool.setConnectionLimit(ldPool.getConnectionLimit());
        orderPool.setCreateName(ldPool.getCreateName());
        orderPool.setCreateTime(ldPool.getCreateTime());
        orderPool.setPrjId(ldPool.getPrjId());
        orderPool.setDcId(ldPool.getDcId());
        orderPool.setCusId(order.getCusId());
        orderPool.setPoolDescription(ldPool.getPoolDescription());
        orderPool.setPoolProvider(ldPool.getPoolProvider());
        orderPool.setSubnetId(ldPool.getSubnetId());
        orderPool.setPoolProtocol(ldPool.getPoolProtocol());
        orderPool.setLbMethod(ldPool.getLbMethod());
        orderPool.setVipPort(ldPool.getVipPort());
        BaseCloudOrderLdPool baseCloudOrderLB = new BaseCloudOrderLdPool();
        BeanUtils.copyProperties(baseCloudOrderLB, orderPool);
        orderLdPoolService.save(baseCloudOrderLB);
    }

    @Override
    public boolean checkLbOrderExist(String lbId) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT lb.pool_id FROM cloudorder_ldpool lb ")
          .append("LEFT JOIN order_info oi ON lb.order_no = oi.order_no ")
          .append("WHERE lb.pool_id =? ")
          .append("AND (oi.order_state IN ('1' , '2')) ")
          .append("AND (lb.order_type IN ('1' , '2')) ");
        Query query = poolDao.createSQLNativeQuery(sb.toString(), lbId);
        List resultList = query.getResultList();
        return resultList != null && resultList.size()>0 ;
    }
    @Override
    public boolean isExistsByOrderNo(String orderNo) {
        CloudOrderLdPool orderPool = orderLdPoolService.getOrderLdPoolByOrderNo(orderNo);
        return isExistsByResourceId(orderPool.getPoolId()).isExisted();
    }
    @Override
    public ResourceCheckBean isExistsByResourceId(String resourceId) {
        ResourceCheckBean resourceCheckBean = new ResourceCheckBean();
        StringBuffer hql = new StringBuffer();
        hql.append(" from BaseCloudLdPool where poolId = ? and isVisible = 1 ");
        BaseCloudLdPool pool = (BaseCloudLdPool) poolDao.findUnique(hql.toString(), new Object[]{resourceId});
        if (pool != null) {
            resourceCheckBean.setResourceName(pool.getPoolName());
            resourceCheckBean.setExisted(true);
        } else {
            resourceCheckBean.setExisted(false);
        }
        return resourceCheckBean;
    }

	@Override
	public void poolCreateSuccessHandleForSync(String poolId,String orderNo,String cusId,long connectionLimit)
			throws Exception {
		BaseCloudLdPool cloudLdPool=poolDao.findOne(poolId);
		cloudLdPool.setIsVisible("1");
		poolCreateSuccessHandle(cloudLdPool.getDcId(),orderNo, cusId, poolId, cloudLdPool.getPoolName(), connectionLimit, cloudLdPool.getPayType());
		poolDao.saveOrUpdate(cloudLdPool);
		jedisUtil.delete(RedisKey.CLOUDLDVIPSYNC+cloudLdPool.getVipId());
		jedisUtil.delete(RedisKey.CLOUDLDPOOLSYNC+poolId);
	}
	
	@Override
	public CloudLdPool getPoolById(String poolId) throws Exception {
		BaseCloudLdPool baseCloudLdPool=poolDao.findOne(poolId);
		CloudLdPool cloudLdPool=new CloudLdPool();
		BeanUtils.copyPropertiesByModel(cloudLdPool, baseCloudLdPool);
		return cloudLdPool;
	}
}
