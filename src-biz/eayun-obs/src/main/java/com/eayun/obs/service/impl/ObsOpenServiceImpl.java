package com.eayun.obs.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.accesskey.model.AccessKey;
import com.eayun.accesskey.model.BaseAccessKey;
import com.eayun.accesskey.service.AccessKeyService;
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.BillingCycleType;
import com.eayun.common.constant.OrderType;
import com.eayun.common.constant.PayType;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.AkSkUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.HmacSHA1Util;
import com.eayun.common.util.ObsUtil;
import com.eayun.costcenter.service.AccountOverviewService;
import com.eayun.customer.model.BaseCusServiceState;
import com.eayun.customer.model.CusServiceState;
import com.eayun.customer.model.Customer;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.obs.base.service.ObsBaseService;
import com.eayun.obs.dao.CusServiceStateDao;
import com.eayun.obs.dao.ObsUserDao;
import com.eayun.obs.model.BaseObsUser;
import com.eayun.obs.model.ObsAccessBean;
import com.eayun.obs.model.ObsUser;
import com.eayun.obs.service.ObsOpenService;
import com.eayun.order.model.BaseOrderResource;
import com.eayun.order.model.Order;
import com.eayun.order.service.OrderService;
import com.eayun.sys.model.SysDataTree;
import com.eayun.syssetup.service.SysDataTreeService;

@Service
@Transactional
public class ObsOpenServiceImpl implements ObsOpenService {
	private static final Logger log = LoggerFactory
			.getLogger(ObsOpenServiceImpl.class);
	@Autowired
	private ObsUserDao obsUserDao;
	@Autowired
	private CusServiceStateDao cusServiceStateDao;
	@Autowired
	private AccessKeyService accessKeyService;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private ObsBaseService obsBaseService;
	@Autowired
	private SysDataTreeService sysDataTreeService;
	@Autowired
	private AccountOverviewService accountOverviewService;
	@Autowired
	private OrderService orderService;
	@Autowired
	private CustomerService customerService;

	@Override
	public CusServiceState getObsByCusId(String cusId) {
		String cssStr = null;
		try {
			cssStr = jedisUtil.get(RedisKey.CUSSERVICESTATE_CUSID + cusId);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		// List<BaseCusServiceState>
		// list=cusServiceStateDao.find("from BaseCusServiceState where cusId=? and obsState=?",
		// cusId,"1");
		// 判断服务开通状态
		if (cssStr == null) {
			return null;
		} else {
			JSONObject cssJson = (JSONObject) JSONObject.parse(cssStr);

			// 判断服务开通状态 ，0为未开通
			if ("0".equals(cssJson.getString("obsState"))) {
				return null;
			} else {
				CusServiceState cusServiceState = JSONObject.toJavaObject(
						cssJson, CusServiceState.class);
				return cusServiceState;
			}
		}
	}

	@Override
	public ObsUser addObsUser(String cusId, String cusOrg) throws Exception {
		String result = null;
		JSONObject jsonObj = null;
		String accesskey = null;
		String secretkey = null;
		Set<String> set = null;
		ObsUser ou=null;
		set = jedisUtil.getSet(RedisKey.AK_CUSID + cusId);
		boolean flag = true;
		// 判重(accessKey)
		while (true) {
			accesskey = AkSkUtil.getAccessKeyStr();
			List<BaseAccessKey> list = accessKeyService.getAkList(accesskey);
			if (list == null || list.size() == 0) {
				break;
			}
		}
		// 判重(secretKey)
		while (true) {
			secretkey = AkSkUtil.getSecretKeyStr();
			List<BaseAccessKey> list = accessKeyService.getAkList(secretkey);
			if (list == null || list.size() == 0) {
				break;
			}
		}

		if (accesskey != null && secretkey != null && flag) {
			String ak = ObsUtil.getAdminAccessKey();
			String sk = ObsUtil.getAdminSecretKey();
			String date = DateUtil.getRFC2822Date(new Date());

			String url = "/admin/user";
			String signature = ObsUtil.getSignature("PUT", "", "", date, "",
					url);

			String hmacSHA1 = HmacSHA1Util.getEncrypt(signature, sk);
			String host = ObsUtil.getEayunObsHost();
			String header = ObsUtil.getRequestHeader();
			String strPut = header + host + url + "?uid=" + cusId
					+ "&display-name=" + cusOrg + "&access-key=" + accesskey
					+ "&secret-key=" + secretkey+"&exclusive=true";
			ObsAccessBean obsBean = new ObsAccessBean();
			obsBean.setAccessKey(ak);
			obsBean.setHmacSHA1(hmacSHA1);
			obsBean.setHost(host);
			obsBean.setPutHeaderHost(host);
			obsBean.setRFC2822Date(date);
			obsBean.setUrl(strPut);
			obsBean.setHttp("http://".equals(header));
			result = (obsBaseService.put(obsBean)).toJSONString();
			// result = openstackUserService.put(cusId, cusName, accesskey,
			// secretkey);
			jsonObj = JSONObject.parseObject(result);

			// 存入数据库obs_user表中
			BaseObsUser bou = new BaseObsUser();
			bou.setCaps(jsonObj.getString("caps"));
			bou.setCusId(cusId);
			bou.setDisplayName(cusOrg);
			bou.setMaxBuckets(jsonObj.getIntValue("max_buckets"));
			bou.setSubUsers(jsonObj.getString("subusers"));
			bou.setSuspended(jsonObj.getString("suspended"));
			bou.setSwiftKeys(jsonObj.getString("swift_keys"));
			bou.setUserId(cusId);
			BaseObsUser baseUser = (BaseObsUser) obsUserDao.saveEntity(bou);
			jedisUtil.set(RedisKey.OBSUSER_CUSID + cusId,
					JSONObject.toJSONString(baseUser));

			// 存入数据库access_key表中
			BaseAccessKey bak = new BaseAccessKey();
			bak.setAccessKey(accesskey);
			bak.setSecretKey(secretkey);
			bak.setUserId(cusId);
			bak.setAcckIsShow("1");
			bak.setAcckState("0");
			bak.setCreateDate(new Date());
			bak.setIsDefault("0");
			bak.setIsStopService(false);
			bak = accessKeyService.saveAk(bak);
			jedisUtil.addToSet(RedisKey.AK_CUSID + cusId, bak.getAkId());
			jedisUtil.set(RedisKey.AK_AKID + bak.getAkId(),
					JSONObject.toJSONString(bak));
			jedisUtil.set(RedisKey.AK_ACCESSKEY+accesskey, JSONObject.toJSONString(bak));
			// 更新数据库cus_service_state
			String cssStr = jedisUtil.get(RedisKey.CUSSERVICESTATE_CUSID + cusId);
			BaseCusServiceState css = null;
			if (cssStr == null) {
				css = new BaseCusServiceState();
				css.setObsState("1");
				css.setCusId(cusId);

			} else {
				JSONObject cssJson = JSONObject.parseObject(cssStr);
				css = JSONObject.toJavaObject(cssJson,
						BaseCusServiceState.class);
				css.setObsState("1");
			}
			css = (BaseCusServiceState) cusServiceStateDao.saveEntity(css);

			jedisUtil.set(RedisKey.CUSSERVICESTATE_CUSID + cusId,
					JSONObject.toJSONString(css));

			ou = new ObsUser();
			BeanUtils.copyPropertiesByModel(ou, baseUser);
		}
		if (!flag) {
			ou = new ObsUser();
			ou.setUserId("isOpen");
		}
		if (set != null) {
			for (String str : set) {
				String akStr = jedisUtil.get(RedisKey.AK_AKID + str);
				AccessKey akJson = JSONObject.toJavaObject(
						JSONObject.parseObject(akStr), AccessKey.class);
				if ("0".equals(akJson.getAcckState())) {

					String newaccesskey = akJson.getAccessKey();
					String newsecretkey = akJson.getSecretKey();
					String ak = ObsUtil.getAdminAccessKey();
					;
					String sk = ObsUtil.getAdminSecretKey();
					String date = DateUtil.getRFC2822Date(new Date());

					String url = "/admin/user";
					String signature = ObsUtil.getSignature("PUT", "", "",
							date, "", url);

					String hmacSHA1 = HmacSHA1Util.getEncrypt(signature, sk);
					String host = ObsUtil.getEayunObsHost();
					String header = ObsUtil.getRequestHeader();
					String strPut = header + host + url + "?key&uid=" + cusId
							+ "&access-key=" + newaccesskey + "&secret-key=" + newsecretkey;
					ObsAccessBean obsBean = new ObsAccessBean();
					obsBean.setAccessKey(ak);
					obsBean.setHmacSHA1(hmacSHA1);
					obsBean.setHost(host);
					obsBean.setPutHeaderHost(host);
					obsBean.setRFC2822Date(date);
					obsBean.setHttp("http://".equals(header));
					obsBean.setUrl(strPut);
					result = (obsBaseService.put(obsBean)).toJSONString();
				}
				if ("0".equals(akJson.getIsDefault())) {
					flag = false;
				}
			}
		}
		return ou;
	}

	@Override
	public boolean isOpenObsServiceAndWhiteList(String userName) {
		SysDataTree isOpenObsService = null;
		try {
			isOpenObsService = DictUtil.getDataTreeByNodeId("0009003");
			String memo = isOpenObsService.getMemo();
			if ("1".equals(memo)) {
				return true;
			} else {
				SysDataTree isInWhiteList = DictUtil
						.getDataTreeByNodeId("0009004");
				String whiteList = isInWhiteList.getMemo();
				if (whiteList != null && whiteList.length() > 0) {
					String[] strs = whiteList
							.split(ConstantClazz.SPLITCHARACTER);
					for (String userAccount : strs) {
						if (userName.equalsIgnoreCase(userAccount)) {
							return true;
						}
					}
				}
				return false;
			}
		} catch (AppException e) {
			return false;
		}
	}

	@Override
	public boolean isPassForLimitValue(String limit, String cusId)
			throws Exception {
		BigDecimal limitValue = new BigDecimal(limit);
		BigDecimal money = accountOverviewService.getAccountInfo(cusId)
				.getMoney();
		if (money.compareTo(limitValue) == -1) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public String getLimitValue() throws Exception {
		String limit=sysDataTreeService.getBuyCondition();
		if (limit != null && limit.trim().length() > 0) {
			return limit;
		} else {
			return "0";
		}
	}

	@Override
	public Order createObsOrder(String cusId,String userId) throws Exception {
		Order order = new Order();
		order.setCusId(cusId);
		order.setUserId(userId);
		order.setProdName("对象存储服务");
		order.setCreateTime(new Date());
		order.setProdCount(1);
		order.setProdConfig("标准");
		order.setPayType(PayType.PAYAFTER);
		order.setBillingCycle(BillingCycleType.HOUR);
		order.setAuditFlag("0");
		order.setOrderType(OrderType.NEW);
		order.setResourceType(ResourceType.OBS);
		order = orderService.createOrder(order);
		return order;
	}

	@Override
	public void completeObsOrder(String orderNo, boolean isResourceOpened,String userId)
			throws Exception {
		BaseOrderResource baseOrderResource=new BaseOrderResource();
		baseOrderResource.setResourceId(userId);
		baseOrderResource.setResourceName("对象存储服务");
		baseOrderResource.setOrderNo(orderNo);
		List<BaseOrderResource> list=new ArrayList<BaseOrderResource>();
		list.add(baseOrderResource);
		orderService.completeOrder(orderNo, isResourceOpened,list);
	}

	@Override
	public JSONObject openObs(String userName, String userId, String cusId,boolean isAdmin)
			throws Exception {
		boolean isOpenObsServiceAndWhiteList=isOpenObsServiceAndWhiteList(userName);
		CusServiceState cusServiceState=getObsByCusId(cusId);
		JSONObject json=new JSONObject();
		if(isOpenObsServiceAndWhiteList){
			String limit=getLimitValue();
			if(isPassForLimitValue(limit,cusId)){
				if(isAdmin){
					if(cusServiceState!=null&&"1".equals(cusServiceState.getObsState())){
						json.put("state", "obsIsOpened");
					}else{
						Order order=createObsOrder(cusId,userId);
						Customer cus=customerService.findCustomerById(cusId);
						String cusOrg=cus.getCusOrg();
						if(cusOrg.indexOf(" ")!=-1){
							cusOrg=cusOrg.replace(" ", "");
						}
						ObsUser obsUser=null;
						try {
							obsUser=addObsUser(cusId, cusOrg);
						} catch(Exception e){
							log.error(e.getMessage(),e);
							completeObsOrder(order.getOrderNo(),false,cusId);
							throw e;
						}
						completeObsOrder(order.getOrderNo(),true,cusId);
						json.put("state", "obsIsOpen");
					}
				}else{
					json.put("state", "isNotAdmin");
				}
			}else{
				json.put("limit", limit);
				json.put("state", "balanceIsNotEnough");
			}
		}else{
			json.put("state", "obsIsNotOpen");
		}
		return json;
	}

	@Override
	public JSONObject isAllowOpen(String userName,String cusId) throws Exception {
		boolean isOpenObsServiceAndWhiteList=isOpenObsServiceAndWhiteList(userName);
		JSONObject json=new JSONObject();
		if(isOpenObsServiceAndWhiteList){
			String limit=getLimitValue();
			if(isPassForLimitValue(limit,cusId)){
				json.put("state", "obsIsAllowOpen");
			}else{
				json.put("limit", limit);
				json.put("state", "balanceIsNotEnough");
			}
		}else{
			json.put("state", "obsIsNotOpen");
		}
		return json;
	}
}
