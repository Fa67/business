package com.eayun.database.instance.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eayun.project.service.ProjectService;
import com.eayun.virtualization.model.BaseCloudProject;
import org.apache.commons.lang.StringUtils;
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
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.CloudResourceUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.Hook;
import com.eayun.costcenter.model.MoneyAccount;
import com.eayun.costcenter.service.AccountOverviewService;
import com.eayun.database.account.model.CloudRDSAccount;
import com.eayun.database.account.service.RDSAccountService;
import com.eayun.database.backup.service.RDSBackupService;
import com.eayun.database.configgroup.model.datastore.Datastore;
import com.eayun.database.database.service.RDSDatabaseService;
import com.eayun.database.instance.dao.CloudRDSInstanceDao;
import com.eayun.database.instance.model.BaseCloudRDSInstance;
import com.eayun.database.instance.model.CloudOrderRDSInstance;
import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.database.instance.service.CloudOrderRDSInstanceService;
import com.eayun.database.instance.service.RDSInstanceService;
import com.eayun.database.log.service.RDSLogService;
import com.eayun.eayunstack.model.RDSInstance;
import com.eayun.eayunstack.service.OpenstackRDSInstanceService;
import com.eayun.notice.model.MessageCloudDataBaseRollBackModel;
import com.eayun.notice.model.MessageOrderResourceNotice;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.order.model.BaseOrder;
import com.eayun.order.model.BaseOrderResource;
import com.eayun.order.model.Order;
import com.eayun.order.service.OrderService;
import com.eayun.price.bean.ParamBean;
import com.eayun.price.bean.PriceDetails;
import com.eayun.price.bean.UpgradeBean;
import com.eayun.price.service.BillingFactorService;
import com.eayun.syssetup.service.SysDataTreeService;
import com.eayun.virtualization.model.BaseCloudFlavor;
import com.eayun.virtualization.service.CloudFlavorService;

/**
 * ECSC云数据库实例相关操作service实现类
 * @author liuzhuangzhuang
 */
@Transactional
@Service
public class RDSInstanceServiceImpl extends BaseRDSInstanceService implements RDSInstanceService {
	
	private static final Logger log = LoggerFactory.getLogger(RDSInstanceServiceImpl.class);
	
	@Autowired
	private OpenstackRDSInstanceService openstackRDSInstanceService;
	@Autowired
	private AccountOverviewService accountOverviewSerivce;
	@Autowired
	private SysDataTreeService sysDataTreeService;
	@Autowired
	private BillingFactorService billingFactorService;
	@Autowired
	private OrderService orderService;
	@Autowired
	private CloudOrderRDSInstanceService cloudOrderRDSInstanceService;
	@Autowired
	private CloudRDSInstanceDao cloudRDSInstanceDao;
	@Autowired
	private CloudFlavorService cloudFlavorService;
	@Autowired
	private EayunRabbitTemplate rabbitTemplate;
	@Autowired
	private MessageCenterService messageCenterService;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private RDSBackupService rdsBackupService;
	@Autowired
	private RDSAccountService rdsAccountService;
	@Autowired
	private RDSLogService rdsLogService;
	@Autowired
	private RDSDatabaseService rdsDatabaseService;
	@Autowired
	private ProjectService projectService;
	/**
	 * 购买云数据库实例
	 * @param cloudOrder  -- 云数据库实例信息，包括订单信息
	 * @param user        -- 当前用户信息
	 * @return  成功返回null对象，失败返回错误信息
	 * @throws Exception
	 */
	@Override
	public String buyRDSInstance(CloudOrderRDSInstance cloudOrder, SessionUserInfo user) throws Exception {
		String errMsg = null;
		try{
			cloudOrder.setCreateUser(user.getUserName());
			cloudOrder.setCusId(user.getCusId());
			cloudOrder.setCreateOrderDate(new Date());
			// 数据库实例配额是否满足创建条件
			// 从库实例配额是否满足创建条件
			errMsg = checkInstanceQuota(cloudOrder);
			if(!StringUtil.isEmpty(errMsg)){
				return errMsg;
			}
			// 包年包月计费 -- 判断订单金额是否发生变动
			if(PayType.PAYBEFORE.equals(cloudOrder.getPayType())){
				BigDecimal totalPayment = calcRdsInstancePrice(cloudOrder);
				if(totalPayment.compareTo(cloudOrder.getPrice()) !=0){
					errMsg = "CHANGE_OF_BILLINGFACTORY";
					return errMsg;
				}
				
			}else if (PayType.PAYAFTER.equals(cloudOrder.getPayType())) {  // 按需计费 -- 判断账户余额是否满足购买开服务
				MoneyAccount accountMoney = accountOverviewSerivce.getAccountInfo(user.getCusId());
				String buyCondition = sysDataTreeService.getBuyCondition();
				BigDecimal createResourceLimitedMoney = new BigDecimal(buyCondition);
				if (accountMoney.getMoney().compareTo(createResourceLimitedMoney) <= 0) {
					errMsg = "NOT_SUFFICIENT_FUNDS";
					return errMsg;
				}
			}
			// 创建从库时需要判断主库的规格是否发生变动
			if("0".equals(cloudOrder.getIsMaster())){
				CloudRDSInstance masterRdsInstance = this.queryRdsInstanceChargeById(cloudOrder.getMasterId());
				if(masterRdsInstance.getCpu() != cloudOrder.getCpu()
						|| masterRdsInstance.getRam() != cloudOrder.getRam()
						|| masterRdsInstance.getVolumeSize() != cloudOrder.getVolumeSize()){
					errMsg = "CHANGE_OF_MASTER_STANDARD"; //主库实例规格发生变动
					return errMsg;
				}
			}
			
			// 保存订单信息
			Order order = createRdsInstanceOrder(cloudOrder, user);
			cloudOrder.setOrderNo(order.getOrderNo());
			// 保存云数据库实例订单信息
			cloudOrderRDSInstanceService.saveOrUpdate(cloudOrder);
			if(PayType.PAYAFTER.equals(cloudOrder.getPayType())){
				try{
					this.createRdsInstance(cloudOrder);
				}catch(Exception e){
					log.error(e.getMessage(),e);
					throw new Exception(e.getMessage());
				}
			}
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw e;
		}
		return null;
	}

	/**
	 * 创建订单信息
	 * @param cloudOrder
	 * @param user
	 * @return
	 * @throws Exception 
	 */
	private Order createRdsInstanceOrder(CloudOrderRDSInstance cloudOrder, SessionUserInfo user) throws Exception {

		Order order = new Order();

		order.setOrderType(cloudOrder.getOrderType());
		order.setDcId(cloudOrder.getDcId());
		order.setProdCount(1);
		order.setProdConfig(rdsInstanceConfig(cloudOrder));
		order.setPayType(cloudOrder.getPayType());
		order.setResourceType(ResourceType.RDS);
		order.setUserId(user.getUserId());
		order.setCusId(user.getCusId());
		order.setProdName(cloudOrder.getProdName());
		
		if (PayType.PAYBEFORE.equals(cloudOrder.getPayType())) {
			if(OrderType.NEW.equals(cloudOrder.getOrderType())){
				order.setBuyCycle(cloudOrder.getBuyCycle());
			}
			else if(OrderType.UPGRADE.equals(cloudOrder.getOrderType())){
				order.setResourceExpireTime(cloudOrder.getEndTime());
			}
			order.setUnitPrice(cloudOrder.getPrice());
			order.setPaymentAmount(cloudOrder.getPrice());
			order.setAccountPayment(cloudOrder.getAccountPayment());
			order.setThirdPartPayment(cloudOrder.getThirdPartPayment());
			
		} else if (PayType.PAYAFTER.equals(cloudOrder.getPayType())) {
			order.setBillingCycle(BillingCycleType.HOUR);
		}

		orderService.createOrder(order);
		return order;
	}

	/**
	 * 云数据库实例计费总价
	 * 
	 * @param cloudOrder -- 订单信息
	 * 
	 * @return 云数据库实例总价
	 */
	private BigDecimal calcRdsInstancePrice(CloudOrderRDSInstance cloudOrder) {
		BigDecimal totalPrice = null;
		if(OrderType.NEW.equals(cloudOrder.getOrderType())){
			ParamBean paramBean = new ParamBean();
			
			paramBean.setCloudMySQLCPU(cloudOrder.getCpu());
			paramBean.setCloudMySQLRAM(cloudOrder.getRam());
			switch (cloudOrder.getVolumeTypeName()) {
			case "Normal":
				paramBean.setStorageMySQLOrdinary(cloudOrder.getVolumeSize());
				break;
			case "Medium":
				paramBean.setStorageMySQLBetter(cloudOrder.getVolumeSize());
				break;
			default:
				break;
			}
			paramBean.setDcId(cloudOrder.getDcId());
			paramBean.setPayType(cloudOrder.getPayType());
			paramBean.setCycleCount(cloudOrder.getBuyCycle());
			paramBean.setNumber(1);
			totalPrice = billingFactorService.getPriceByFactor(paramBean);
		}
		if(OrderType.UPGRADE.equals(cloudOrder.getOrderType())){
			UpgradeBean upgradeBean = new UpgradeBean();
			upgradeBean.setDcId(cloudOrder.getDcId());
			if((cloudOrder.getCpu() - cloudOrder.getRdsInstanceCpu())>0){
				upgradeBean.setCloudMySQLCPU(cloudOrder.getCpu() - cloudOrder.getRdsInstanceCpu());
			}
			if((cloudOrder.getRam() - cloudOrder.getRdsInstanceRam())>0){
				upgradeBean.setCloudMySQLRAM((cloudOrder.getRam() - cloudOrder.getRdsInstanceRam()));
			}
			if((cloudOrder.getVolumeSize() - cloudOrder.getDiskSize())>0){
				switch (cloudOrder.getVolumeTypeName()) {
				case "Normal":
					upgradeBean.setStorageMySQLOrdinary(cloudOrder.getVolumeSize() - cloudOrder.getDiskSize());
					break;
				case "Medium":
					upgradeBean.setStorageMySQLBetter(cloudOrder.getVolumeSize() - cloudOrder.getDiskSize());
					break;
				default:
					break;
				}
			}
			upgradeBean.setCycleCount(cloudOrder.getCycleCount());
			
			totalPrice = billingFactorService.updateConfigPrice(upgradeBean);
		}
		
		return totalPrice.setScale(2, RoundingMode.FLOOR);
	}
	
	/**
	 * 根据云主机订单组装 云主机配置<br>
	 * ---------------------------------
	 * @author zhouhaitao
	 * @param order
	 * @return
	 */
	public String rdsInstanceConfig(CloudOrderRDSInstance order) {
		StringBuffer buffer = new StringBuffer();
		if(OrderType.NEW.equals(order.getOrderType())){
			buffer.append("数据中心：").append(order.getDcName()).append("<br>");
			buffer.append("私有网络：").append(order.getNetName()).append("<br>");
			buffer.append("受管子网：").append(order.getSubnetName() + "(" + order.getSubnetCidr() + ")").append("<br>");
			buffer.append("规格：").append(order.getCpu() + "核/" + order.getRam() + "GB/" +
					this.getVolumeTypeStrByVolumeTypeName(order.getVolumeTypeName()) + order.getVolumeSize() + "GB")
			.append("<br>");
			buffer.append("版本：").append(order.getVersionName());
		}
		else if(OrderType.UPGRADE.equals(order.getOrderType())){
			buffer.append("数据中心：").append(order.getDcName()).append("<br>");
			buffer.append("实例ID：").append(order.getRdsId()).append("<br>");
			buffer.append("实例名称：").append(order.getRdsName()).append("<br>");
			buffer.append("版本号：").append(order.getVersionName()).append("<br>");
			buffer.append("当前规格：").append(order.getRdsInstanceCpu() + "核/" + order.getRdsInstanceRam()+"GB/"
									+ this.getVolumeTypeStrByVolumeTypeName(order.getVolumeTypeName()) + order.getDiskSize() + "GB").append("<br>");
			buffer.append("升级后规格：").append(order.getCpu() + "核/" + order.getRam()+"GB/"
									+ this.getVolumeTypeStrByVolumeTypeName(order.getVolumeTypeName()) + order.getVolumeSize() + "GB");
		}

		return buffer.toString();
	}
	/**
	 * 创建云数据库实例
	 * @param cloudOrder
	 */
	@Transactional(noRollbackFor=AppException.class)
	public void createRdsInstance(CloudOrderRDSInstance cloudOrder) 
			 throws AppException {
		int disStep = 0;
		RDSInstance rdsInstance = new RDSInstance();
		BaseCloudFlavor cloudFlavor = new BaseCloudFlavor();
		try{
			
			cloudFlavor.setFlavorVcpus(cloudOrder.getCpu());
			cloudFlavor.setFlavorRam(cloudOrder.getRam() * 1024);
			cloudFlavor.setFlavorDisk(ConstantClazz.RDS_FLAVOR_DISK);
			cloudFlavor.setDcId(cloudOrder.getDcId());
			cloudFlavor.setPrjId(cloudOrder.getPrjId());
			// 创建云主机类型
			cloudFlavorService.createFlavor(cloudFlavor);
			Object[] dataStoreVersion = getTypeAndVersionByVersionId(cloudOrder.getVersionId());
			Object[] troveInfos = getTroveInfo(cloudOrder.getDcId());
			// 创建云数据库实例
			CloudRDSInstance cloudRdsInstance = new CloudRDSInstance();
			cloudRdsInstance.setDcId(cloudOrder.getDcId());
			cloudRdsInstance.setPrjId(cloudOrder.getPrjId());
			cloudRdsInstance.setTorvePrjId(troveInfos[0].toString());
			cloudRdsInstance.setTroveSecurityGroupId(troveInfos[1].toString());
			cloudRdsInstance.setNetId(cloudOrder.getNetId());
			cloudRdsInstance.setSubnetId(cloudOrder.getSubnetId());
			cloudRdsInstance.setRdsName(cloudOrder.getRdsName());
			cloudRdsInstance.setVolumeSize(cloudOrder.getVolumeSize());
			cloudRdsInstance.setVolumeType(cloudOrder.getVolumeType());
			cloudRdsInstance.setFlavorId(cloudFlavor.getFlavorId());
			cloudRdsInstance.setType(dataStoreVersion[0].toString());
			cloudRdsInstance.setVersion(dataStoreVersion[1].toString());
			if(!StringUtil.isEmpty(cloudOrder.getConfigId())){
				cloudRdsInstance.setConfigId(cloudOrder.getConfigId());
			}
			if(!StringUtil.isEmpty(cloudOrder.getBackupId())){
				cloudRdsInstance.setBackupId(cloudOrder.getBackupId());
			}
			if(!StringUtil.isEmpty(cloudOrder.getMasterId())){
				cloudRdsInstance.setMasterId(cloudOrder.getMasterId());
			}
			String errors = openstackRDSInstanceService.createRdsInstance(cloudRdsInstance, rdsInstance);
			
			if (!StringUtil.isEmpty(errors)) {
				throw new AppException("error.openstack.message", new String[] { errors });
			} 
			disStep = 1;
			
		}catch (AppException e) {
			throw e;
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			throw new AppException("error.openstack.message");
		} finally {
			try {
				rdsInstanceCreateCallback(cloudOrder, disStep, cloudFlavor.getFlavorId(), rdsInstance);
			} catch (AppException e) {
				throw e;
			} catch (Exception e) {
			    log.error(e.getMessage(),e);
				throw new AppException("error.openstack.message");
			}
		}
	}
	/**
	 * 创建云数据库实例处理
	 * @param cloudOrder -- 云数据库实例订单信息
	 * @param disStep -- 底层是否创建成功 1->success 0->failed
	 * @param flavorId -- 云数据库实例规格ID
	 * @param rdsInstance -- 调用底层创建云数据库实例接口返回的云数据库实例信息
	 * @throws Exception
	 */
	public void rdsInstanceCreateCallback(CloudOrderRDSInstance cloudOrder, int disStep, String flavorId,
			RDSInstance rdsInstance) throws Exception {
		int a = 0;
		BaseCloudRDSInstance succRdsInstance = new BaseCloudRDSInstance();
		try{
			if(disStep == 1 && cloudOrder.getIsMaster().equals("0")){
				setRdsInstanceStatusToBackup(cloudOrder.getMasterId());
			}
			try{
				succRdsInstance = saveRdsInstance(cloudOrder, flavorId, rdsInstance);
			}catch(Exception e){
				log.error(e.getMessage(), e);
				throw new Exception(e.getMessage());
			}
			a =1;
			if(disStep == 1){ // 底层创建成功
				createSuccessHandler(cloudOrder, succRdsInstance);
			}else if(disStep == 0){ //底层创建时失败
				createFailHandler(cloudOrder, succRdsInstance);
			}
		}catch(Exception e){
		    log.error(e.getMessage(), e);
		    if(a != 1){
		    	createFailHandler(cloudOrder, succRdsInstance);
		    }
			throw e;
		}
	}

	/**
	 * 创建从库时，主库置为backup状态,并且发送状态同步任务
	 * @param masterId -- 主库状态
	 */
	public void setRdsInstanceStatusToBackup(String masterId) {
		BaseCloudRDSInstance rdsInstance = new BaseCloudRDSInstance();
		rdsInstance = cloudRDSInstanceDao.findOne(masterId);
		rdsInstance.setRdsStatus("BACKUP");
		
		cloudRDSInstanceDao.merge(rdsInstance);
		
		JSONObject json = new JSONObject();
		json.put("rdsId", rdsInstance.getRdsId());
		json.put("dcId", rdsInstance.getDcId());
		json.put("prjId", rdsInstance.getPrjId());
		json.put("rdsStatus", rdsInstance.getRdsStatus());
		json.put("count", "0");

		final JSONObject data = json;
		TransactionHookUtil.registAfterCommitHook(new Hook() {
			@Override
			public void execute() {
				jedisUtil.addUnique(RedisKey.rdsKey, data.toJSONString());
			}
			
		});
	}

	/**
	 * 资源创建过程中失败处理
	 * 
	 * @param cloudOrder -- 云数据库实例订单信息
	 * 
	 * @param succRdsInstance -- 调用上层创建云数据库实例接口返回的云数据库实例信息
	 * @throws Exception 
	 */
	public void createFailHandler(CloudOrderRDSInstance cloudOrder, BaseCloudRDSInstance succRdsInstance) 
			throws Exception {
		try{
			// 调用资源创建失败的接口
			orderService.completeOrder(cloudOrder.getOrderNo(), false, null);
			// 发送购买资源失败的邮件
			messageCenterService.addResourFailMessage(cloudOrder.getOrderNo(), cloudOrder.getCusId());
			if(!StringUtil.isEmpty(succRdsInstance.getRdsId())){
				deleteRdsInstanceForCreateFailed(succRdsInstance, cloudOrder.getCreateUser());
			}
		}catch(Exception e){
			// 底层删除失败场景
			deleteFailedHandler(cloudOrder);
			log.error(e.getMessage(),e);
			throw e;
		}
	}

	/**
	 * 创建资源失败，底层删除已创建的资源时失败的处理
	 * 
	 * @param cloudOrder
	 * 					-- 订单信息
	 */
	public void deleteFailedHandler(CloudOrderRDSInstance cloudOrder) {
		MessageOrderResourceNotice resource = new MessageOrderResourceNotice();
		resource.setOrderNo(cloudOrder.getOrderNo());
		resource.setResourceId(cloudOrder.getRdsId());
		resource.setResourceName(cloudOrder.getRdsName());
		resource.setResourceType(ResourceType.getName(ResourceType.RDS));
		List<MessageOrderResourceNotice> resources = new ArrayList<MessageOrderResourceNotice>();
		resources.add(resource);
		messageCenterService.delecteResourFailMessage(resources, cloudOrder.getOrderNo());
	}

	/**
	 * 删除资源（底层与上层都创建成功，但是状态不正常（ERROR））
	 * 
	 * @param succRdsInstance  -- 已创建的云数据库实例
	 *  
	 * @param userName  -- 创建人
	 * 
	 */
	public void deleteRdsInstanceForCreateFailed(BaseCloudRDSInstance succRdsInstance,String userName) {
		
		try{
			// 调用底层接口删除资源
			openstackRDSInstanceService.delete(succRdsInstance.getDcId(), 
					succRdsInstance.getPrjId(), succRdsInstance.getRdsId());
			// 修改上层数据库的状态为删除中
			BaseCloudRDSInstance rdsInstance = new BaseCloudRDSInstance();
			rdsInstance = cloudRDSInstanceDao.findOne(succRdsInstance.getRdsId());
			rdsInstance.setDeleteUser(userName);
			rdsInstance.setDeleteTime(new Date());
			rdsInstance.setRdsStatus("SHUTDOWN");
			
			cloudRDSInstanceDao.merge(rdsInstance);
			
			JSONObject json = new JSONObject();
			json.put("rdsId", rdsInstance.getRdsId());
			json.put("dcId", rdsInstance.getDcId());
			json.put("prjId", rdsInstance.getPrjId());
			json.put("rdsStatus", rdsInstance.getRdsStatus());
			json.put("count", "0");

			final JSONObject data = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.rdsKey, data.toJSONString());
				}
				
			});
			
		}catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}
	}

	/**
	 * 底层与上层均创建成功
	 * @param cloudOrder -- 云数据库实例订单信息
	 * @param succRdsInstance -- 调用上层创建云数据库实例接口返回的云数据库实例信息
	 * @throws Exception 
	 */
	public void createSuccessHandler(CloudOrderRDSInstance cloudOrder, BaseCloudRDSInstance succRdsInstance) 
			throws Exception {
		
		if(!StringUtil.isEmpty(succRdsInstance.getRdsId())){
			if(succRdsInstance.getRdsStatus().equals("ERROR")){
		    	createFailHandler(cloudOrder, succRdsInstance);
				return ;
			}
			if(succRdsInstance.getRdsStatus().equals("ACTIVE")){
				enableRootAndlog(cloudOrder, succRdsInstance);
			}
			// 发送状态同步的消息
			JSONObject json = new JSONObject();
			json.put("orderNo", cloudOrder.getOrderNo());
			json.put("dcId", cloudOrder.getDcId());
			json.put("prjId", cloudOrder.getPrjId());
			json.put("rdsId", succRdsInstance.getRdsId());
			json.put("rdsName", cloudOrder.getRdsName());
			json.put("rdsStatus", "BUILD");
			json.put("count", "0");
			
			json.put("createName", cloudOrder.getCreateUser());
			json.put("createTime", cloudOrder.getCreateOrderDate());
			json.put("cusId", cloudOrder.getCusId());
			json.put("cpu", cloudOrder.getCpu());
			json.put("ram", cloudOrder.getRam());
			json.put("volumeSize", cloudOrder.getVolumeSize());
			json.put("volumeTypeName", cloudOrder.getVolumeTypeName());
			json.put("payType", cloudOrder.getPayType());
			json.put("isMaster", cloudOrder.getIsMaster());
			json.put("password", cloudOrder.getPassword()); // 实例密码
			
			final JSONObject data = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					try {
						jedisUtil.push(RedisKey.rdsKey, data.toJSONString());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			});
		}
	}
	/**
	 * 创建成功后启用日志和root用户，并且开启自动备份的计划
	 * @param cloudOrder -- 云数据库实例订单信息
	 * @param succRdsInstance -- 调用上层创建云数据库实例接口返回的云数据库实例信息
	 * @throws Exception 
	 */
	public void enableRootAndlog(CloudOrderRDSInstance cloudOrder, BaseCloudRDSInstance succRdsInstance) 
			throws Exception{
		try{
			// 启用日志功能
			rdsLogService.enableLog(cloudOrder.getDcId(), 
					cloudOrder.getPrjId(), cloudOrder.getRdsId());
			
			if("1".equals(succRdsInstance.getIsMaster())){ 
				// 由于从库不展示数据库和用户，所以此时不需要启用root用户和同步底层的数据库以及用户信息
				// 启用root用户,并且发送短信
				CloudRDSAccount  account = new CloudRDSAccount();
				account.setDcId(cloudOrder.getDcId());
				account.setPrjId(cloudOrder.getPrjId());
				account.setInstanceId(cloudOrder.getRdsId());
				account.setAccountName("root");
				account.setPassword(cloudOrder.getPassword());
				account.setInstanceName(succRdsInstance.getRdsName());
				account.setRemark("");
				rdsAccountService.createAccountRoot(account);
				// 创建完成以后需要将底层的数据库和用户同步上来
				rdsDatabaseService.synchronDBCreate(cloudOrder.getDcId(), cloudOrder.getPrjId(), cloudOrder.getRdsId());
				rdsAccountService.synchronAccountCreate(cloudOrder.getDcId(), cloudOrder.getPrjId(), cloudOrder.getRdsId());
			}
			
			// 开启自动备份计划
			rdsBackupService.addBackupSchedule(cloudOrder.getDcId(), cloudOrder.getPrjId(),
					cloudOrder.getCusId(), cloudOrder.getRdsId());
			createSuccessRDSInstance(cloudOrder, succRdsInstance);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			this.createFailHandler(cloudOrder, succRdsInstance);
		}
	}
	/**
	 * 创建云数据库实例成功--最后一步
	 * @param cloudOrder -- 云数据库实例订单信息
	 * @param succRdsInstance  创建成功的云数据库实例
	 * @throws Exception
	 */
	public void createSuccessRDSInstance(CloudOrderRDSInstance cloudOrder, BaseCloudRDSInstance succRdsInstance) throws Exception{
		List<BaseOrderResource> resourceList = new ArrayList<BaseOrderResource>();
        BaseOrderResource resource = new BaseOrderResource();
        resource.setOrderNo(cloudOrder.getOrderNo());
        resource.setResourceId(succRdsInstance.getRdsId());
        resource.setResourceName(succRdsInstance.getRdsName());
        resourceList.add(resource);
        BaseOrder order = orderService.completeOrder(cloudOrder.getOrderNo(), true, resourceList);
        cloudOrder.setOrderCompleteDate(order.getCompleteTime());
        cloudOrder.setRdsId(succRdsInstance.getRdsId());
        // 将数据库实例的ID补充到订单表中
        cloudOrderRDSInstanceService.udpateOrder(cloudOrder);
        Date completeDate = null;
        if(PayType.PAYBEFORE.equals(cloudOrder.getPayType())){
			completeDate = order.getResourceExpireTime();
		}
        // 后付费资源发送计费消息
        if(PayType.PAYAFTER.equals(cloudOrder.getPayType())){
        	rdsInstancePurchaseCharge(cloudOrder, succRdsInstance);
        }
        // 资源状态置为显示，并且更新到期时间
        cloudOrderRDSInstanceService.modifyResourceForVisable(succRdsInstance.getRdsId(), completeDate, succRdsInstance.getVmId());
	}

	/**
	 * 根据底层返回的信息创建云数据库实例
	 * @param cloudOrder -- 云数据库实例订单信息
	 * @param flavorId  -- 云数据库实例规格ID
	 * @param rdsInstance -- 底层创建返回的云数据库实例信息
	 * @return
	 */
	public BaseCloudRDSInstance saveRdsInstance(CloudOrderRDSInstance cloudOrder, String flavorId,
			RDSInstance rdsInstance) {
		BaseCloudRDSInstance result = new BaseCloudRDSInstance();
		if(!StringUtil.isEmpty(rdsInstance.getId())){
			RDSInstance newRdsInstance = openstackRDSInstanceService.getById(cloudOrder.getDcId(), 
					cloudOrder.getPrjId(), rdsInstance.getId());
			if(!StringUtil.isEmpty(newRdsInstance.getId()) 
					&& newRdsInstance.getId().equals(rdsInstance.getId())){
				 BaseCloudRDSInstance temp = new BaseCloudRDSInstance();
				 temp.setRdsId(newRdsInstance.getId());
				 temp.setCreateTime(new Date());
				 temp.setEndTime(cloudOrder.getEndTime());
				 temp.setCreateName(cloudOrder.getCreateUser());
				 temp.setDcId(cloudOrder.getDcId());
				 temp.setPrjId(cloudOrder.getPrjId());
				 temp.setPayType(cloudOrder.getPayType());
				 temp.setChargeState(CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE);
				 temp.setIsVisible("0");
				 temp.setRdsName(newRdsInstance.getName());
				 temp.setRdsStatus(newRdsInstance.getStatus().toUpperCase());
				 temp.setIsMaster(cloudOrder.getIsMaster());
				 if(cloudOrder.getIsMaster().equals("0")){
					 temp.setMasterId(cloudOrder.getMasterId());
				 }
				 temp.setNetId(cloudOrder.getNetId());
				 temp.setSubnetId(cloudOrder.getSubnetId());
				 temp.setRdsIp(rdsInstance.getIp()[0]);
				 temp.setPortId(rdsInstance.getPortId());
				 temp.setConfigId(cloudOrder.getConfigId());
				 temp.setVersionId(cloudOrder.getVersionId()); 
				 temp.setFlavorId(flavorId);
				 temp.setVolumeSize(cloudOrder.getVolumeSize());
				 temp.setVolumeType(cloudOrder.getVolumeType());
				 temp.setIsDeleted("0");
				 if(!StringUtil.isEmpty(newRdsInstance.getVmId())){
					 temp.setVmId(newRdsInstance.getVmId());
				 }
				 cloudRDSInstanceDao.saveEntity(temp);
				 BeanUtils.copyPropertiesByModel(result, temp);
			}
		}
		return result;
	}
	
	/**
	 * 新购云数据库实例（后付费）发送计费消息
	 * @param order -- 订单信息
	 * @param rdsInstance -- 数据库实例信息
	 */
	public void rdsInstancePurchaseCharge(final CloudOrderRDSInstance order, final BaseCloudRDSInstance rdsInstance) {
		TransactionHookUtil.registAfterCommitHook(new Hook() {
			@Override
			public void execute() {
				ChargeRecord record = new ChargeRecord();
				ParamBean paramBean = new ParamBean();
				paramBean.setCloudMySQLCPU(order.getCpu());
				paramBean.setCloudMySQLRAM(order.getRam());
				switch (order.getVolumeTypeName()) {
				case "Normal":
					paramBean.setStorageMySQLOrdinary(order.getVolumeSize());
					break;
				case "Medium":
					paramBean.setStorageMySQLBetter(order.getVolumeSize());
					break;
				default:
					break;
				}
				record.setParam(paramBean);
				record.setDatecenterId(order.getDcId());
				record.setOrderNumber(order.getOrderNo());
				record.setCusId(order.getCusId());
				record.setResourceId(order.getRdsId());
				record.setResourceType(ResourceType.RDS);
				record.setChargeFrom(order.getOrderCompleteDate());
				rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_PURCHASE, JSONObject.toJSONString(record));
			}
		});
	}

	/**
	 * 云数据库实例续费
	 * @param map  
	 * @param sessionUser
	 * @return
	 * @throws Exception
	 */
	public JSONObject renewRdsOrderConfirm(Map<String ,String> map, SessionUserInfo sessionUser)throws Exception{
		JSONObject jsonResult = new JSONObject ();
		String userId = sessionUser.getUserId();
    	String userName = sessionUser.getUserName();
    	String cusId = sessionUser.getCusId();
    	String opIp = sessionUser.getIP();
    	map.put("operatorIp", opIp);//操作者ip
		boolean flag = false;
		flag = this.checkRdsInstanceOrderExsit(map.get("rdsId").toString());
		if(!flag){
			jsonResult = this.createRdsRenewOrder(map, userId,userName,cusId);
		}else{
			jsonResult.put("respCode", 1);
			jsonResult.put("message", "您当前有未完成订单，不允许提交新订单！");
		}
		return jsonResult;
	}

	/**
	 * 创建云数据库实例续费订单
	 * @param map  -- 实例信息参数
	 * @param userId  -- 用户ID
	 * @param userName -- 用户ID
	 * @param cusId  -- 客户ID
	 * @return
	 * @throws Exception 
	 */
	public JSONObject createRdsRenewOrder(Map<String, String> map, String userId,
			String userName, String cusId) throws Exception {
		
		JSONObject jsonResult = new JSONObject ();
		String aliPay = (String) map.get("aliPay");//支付宝付款金额
		String accountPay = (String) map.get("accountPay");//余额付款金额
		String totalPay = (String) map.get("totalPay");
		String isAccountPay = (String) map.get("isCheck");
		String rdsId = (String) map.get("rdsId");
		
		BigDecimal orgTotalPay = new BigDecimal(totalPay);
		BigDecimal price = null;
		CloudRDSInstance cloudRdsInstance = this.queryRdsInstanceChargeById(map.get("rdsId").toString());
		if(Integer.parseInt(map.get("ram")) != cloudRdsInstance.getRam() ||
				Integer.parseInt(map.get("cpu")) != cloudRdsInstance.getCpu() ||
				Integer.parseInt(map.get("volumeSize")) != cloudRdsInstance.getVolumeSize()){
			jsonResult.put("respCode", 11);
			jsonResult.put("message", "您的订单规格发生变动，请重新确认订单！");
			return jsonResult;
		}
		price = billingFactorService.getPriceByFactor(organizParamBean(map, cloudRdsInstance));
        price = price.setScale(2, BigDecimal.ROUND_FLOOR);
		if(orgTotalPay.compareTo(price)==0){
			if("false".equals(isAccountPay)|| null==isAccountPay){// 直接"创建订单，跳向支付宝支付页面！";
				Order order = orderService.createOrder(organizOrder(map, userId,cusId));
				this.saveOrUpdateCloudOrderRdsInstance(order, rdsId, cusId, userName);//创建订单后，回写业务信息
				if(aliPay.compareTo("0")==0){//说明没有勾选月支付，且支付金额为零
					jsonResult.put("respCode", 10);
					jsonResult.put("message", order.getProdName());
				}else{
					jsonResult.put("respCode", 0);
					jsonResult.put("message", order.getOrderNo());
				}
				
				
			}else{//勾选余额支付
				BigDecimal nowAccountMoney = null;
				MoneyAccount accountMoney = accountOverviewSerivce.getAccountInfo(cusId);
				nowAccountMoney = accountMoney.getMoney();
				
				if(nowAccountMoney.compareTo(new BigDecimal(accountPay))>=0){//当前账户余额>=余额支付的金额
					Order order = orderService.createOrder(organizOrder(map, userId,cusId));
					this.saveOrUpdateCloudOrderRdsInstance(order, rdsId, cusId, userName);//创建订单后，回写业务信息
					if(new BigDecimal(aliPay).compareTo(new BigDecimal(0))==0){//如果未用支付宝，只用余额支付
						// "创建订单，然后直接跳转订单完成页面"
						
						jsonResult.put("respCode", 10);
						jsonResult.put("message", order.getProdName());
						
					}else{//有用余额+支付宝 混合支付
						jsonResult.put("respCode", 0);
						jsonResult.put("message", order.getOrderNo());
					}
				}else{//“账户余额发生变动，请重新确认订单！”;
					jsonResult.put("respCode", 3);
					jsonResult.put("message", "您的余额发生变动，请重新确认订单！");
				}
			}
			
		}else{//产品金额发生变动
			jsonResult.put("respCode", 2);
			jsonResult.put("message", "您的订单金额发生变动，请重新确认订单！");
		}
		
		return jsonResult;
	
	}

	/**
	 * 创建订单后，创建云数据库实例订单（续费）
	 * @param order 
	 * 				-- 订单信息
	 * @param rdsId
	 * 				-- 实例ID
	 * @param cusId
	 *  			-- 客户ID
	 * @param userName
	 *              -- 用户名称
	 */
	private void saveOrUpdateCloudOrderRdsInstance(Order order, String rdsId, String cusId, String userName) {
		BaseCloudRDSInstance baseRds = cloudRDSInstanceDao.findOne(rdsId);
		CloudOrderRDSInstance cloudOrder = new CloudOrderRDSInstance();
		cloudOrder.setOrderNo(order.getOrderNo());
		cloudOrder.setRdsId(rdsId);
		cloudOrder.setDcId(baseRds.getDcId());
		cloudOrder.setPrjId(baseRds.getPrjId());
		cloudOrder.setCycleCount(1);
		cloudOrder.setCusId(cusId);
		cloudOrder.setCreateOrderDate(order.getCreateTime());
		cloudOrder.setCreateUser(userName);
		cloudOrder.setOrderType(order.getOrderType());
		cloudOrder.setPayType(baseRds.getPayType());
		cloudOrderRDSInstanceService.saveOrUpdate(cloudOrder);
	}

	/**
	 * 组织云数据库实例的计费信息
	 * @param map
	 * @return
	 */
	private ParamBean organizParamBean(Map<String, String> map, CloudRDSInstance cloudRdsInstance) {
		ParamBean paramBean = new ParamBean();
		paramBean.setDcId(map.get("dcId"));
		paramBean.setPayType(map.get("payType"));
		paramBean.setNumber(1);
		paramBean.setCycleCount(Integer.parseInt(map.get("buyCycle")));
		paramBean.setCloudMySQLCPU(cloudRdsInstance.getCpu()); // CPU
		paramBean.setCloudMySQLRAM(cloudRdsInstance.getRam()); //内存
		String volumeTypeName = map.get("volumeTypeName").toString();
		switch (volumeTypeName) {
		case "Normal":
			paramBean.setStorageMySQLOrdinary(cloudRdsInstance.getVolumeSize());
			break;
		case "Medium":
			paramBean.setStorageMySQLBetter(cloudRdsInstance.getVolumeSize());
			break;
		default:
			break;
		}
		return paramBean;
	}

	/**
	 * 组织订单参数
	 * **/
	private Order organizOrder(Map<String , String> map ,String userId,String cusId){
		Order order = new Order();
		// 根据rdsId获取实例信息，目的：使用新的到期日期，而不能使用前台传的到期日期。
		BaseCloudRDSInstance baseRds = cloudRDSInstanceDao.findOne(map.get("rdsId").toString());
		order.setOrderType(OrderType.RENEW);
		order.setProdName("MySQL主库实例-续费");
		order.setDcId(map.get("dcId").toString());
		order.setProdCount(1);
		StringBuffer buf = new StringBuffer();
		buf.append("数据中心："+ map.get("dcName").toString() +"<br>");
		buf.append("实例ID："+ map.get("rdsId").toString() +"<br>");
		buf.append("实例名称："+ baseRds.getRdsName() +"<br>");
		buf.append("版本号：" + map.get("versionName").toString() + "<br>");
		buf.append("规格："+map.get("cpu")+"核/"+map.get("ram")+"GB/"+ this.getVolumeTypeStrByVolumeTypeName(map.get("volumeTypeName").toString())
				+map.get("volumeSize")+"GB"+"<br>");
		order.setProdConfig(buf.toString());
		order.setPayType(PayType.PAYBEFORE);
		order.setBuyCycle(Integer.parseInt(map.get("buyCycle").toString()));
		order.setUnitPrice(new BigDecimal(map.get("totalPay").toString()));
		order.setResourceType(ResourceType.RDS);
		order.setPaymentAmount(new BigDecimal(map.get("totalPay").toString()));
		order.setAccountPayment(new BigDecimal(map.get("accountPay").toString()));
		order.setThirdPartPayment(new BigDecimal(map.get("totalPay").toString()).subtract(new BigDecimal(map.get("accountPay").toString())));
		
		JSONObject params = new JSONObject();
	    params.put("resourceId", map.get("rdsId"));
	    params.put("resourceName", baseRds.getRdsName());
	    params.put("resourceType", ResourceType.RDS);
	    params.put("expirationDate", baseRds.getEndTime());
	    params.put("duration", map.get("buyCycle"));
	    params.put("operatorIp", map.get("operatorIp"));
	    order.setParams(params.toJSONString());
		
		order.setCusId(cusId);
		order.setUserId(userId);
		return order;
	}
	
	/**
	 * 根据versionId获取数据库类型和版本
	 * @param versionId
	 * @return
	 */
	private Object [] getTypeAndVersionByVersionId(String versionId){
		Object[] obj = null;
		StringBuffer sql = new StringBuffer();
		
		sql.append("   select cd.name as type, cdv.name version                   ");
		sql.append("   from cloud_datastoreversion cdv                            ");
		sql.append("   LEFT JOIN cloud_datastore cd on cdv.datastore_id = cd.id   ");
		sql.append("   where cdv.id = ?                                           ");
		
		javax.persistence.Query query = cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(), new Object[]{versionId});
        if(query.getResultList()!=null && query.getResultList().size() > 0) {
            obj = (Object[]) query.getResultList().get(0);
        }
		return obj;
	}
	
	/**
	 * 根据公共租户的项目获取公共租户的Id 和默认安全组的ID
	 * @return
	 */
	private Object [] getTroveInfo(String dcId){
		Object[] obj = null;
		StringBuffer sql = new StringBuffer();
		
		sql.append("   select cp.prj_id, cd.sg_id                                  ");
		sql.append("   from cloud_project cp                                       ");
		sql.append("   LEFT JOIN cloud_securitygroup cd on cp.prj_id = cd.prj_id   ");
		sql.append("   where cd.sg_name = 'default'                                ");
		sql.append("   and cp.dc_id = ?                                            ");
		sql.append("   and cp.prj_name = ?                                         ");

		javax.persistence.Query query = cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(), new Object[]{dcId, ConstantClazz.TROVE_MANAGED_TENANT});
        if(query.getResultList()!=null && query.getResultList().size() > 0) {
            obj = (Object[]) query.getResultList().get(0);
        }
		return obj;
	}
	/**
	 * 校验该数据库实例是否有未完成的订单
	 * @param rdsId
	 * @return
	 */
	private boolean checkRdsInstanceOrderExsit(String rdsId) {
		return checkRdsInstanceOrderExsit(rdsId, true, true);
	}
	/**
	 * 查询数据库实例是否有正在处理的订单
	 * 
	 * @param rdsId
	 * 				  -- 数据库实例ID
	 * @param isResize
	 *                -- 升级
	 * @param isRenew
	 *                -- 续费
	 * @return
	 */
	public boolean checkRdsInstanceOrderExsit(String rdsId,boolean isResize,boolean isRenew){
		StringBuffer sql = new StringBuffer();
		sql.append("   SELECT                          ");
		sql.append("   	rds.orderrds_id                ");
		sql.append("   FROM                            ");
		sql.append("   	cloudorder_rdsinstance rds     ");
		sql.append("   LEFT JOIN order_info oi         ");
		sql.append("   ON rds.order_no = oi.order_no   ");
		sql.append("   WHERE                           ");
		sql.append("   	rds.rds_id = ?                 ");
		sql.append("   AND (                           ");
		sql.append("   	oi.order_state = '1'           ");
		sql.append("   	OR oi.order_state = '2'        ");
		sql.append("   )                               ");
		sql.append("   AND (                           ");
		sql.append("   	1 <> 1                         ");

		if(isResize){
			sql.append(" OR rds.order_type = '2'       ");
		}
		if(isRenew){
			sql.append(" OR rds.order_type = '1'       ");
		}
		sql.append("		)                          ");
		
		javax.persistence.Query query = cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(), new Object[]{rdsId});
		@SuppressWarnings("rawtypes")
		List resultList = query.getResultList();
		return resultList != null && resultList.size()>0 ;
	}

	/**
	 * 
	 * @param rdsId
	 * 				-- 数据库实例ID
	 * @param resourceState
	 * 				-- 资源需要变成的状态
	 * @param endTime
	 * 				-- 资源的新到期时间 后付费 不需要此参数
	 * @param isRestict
	 * 				-- 是否超过保留时长（true:超过，false：没有超过）
	 * @param isResumable
	 * 				--是否可回复（true:是，false:否）
	 * @throws Exception
	 */
	@Override
	public void modifyStateForRdsInstance(String rdsId, String resourceState, Date endTime, boolean isRestict, boolean isResumable) throws Exception {
		BaseCloudRDSInstance rdsInstance = cloudRDSInstanceDao.findOne(rdsId);
		BaseCloudProject project = projectService.findProject(rdsInstance.getPrjId());
		rdsInstance.setChargeState(resourceState);
		if(null != endTime){
			rdsInstance.setEndTime(endTime);
		}
		if(isRestict){
			//限制服务
			if (!"1".equals(jedisUtil.get(RedisKey.CHARGE_RDS_RESTRICTED + rdsId))) {
				log.info("该资源" + rdsId + "未被限制过服务，即将开始限制并将redis对应的限制服务标志位置为1");
				String key = "BILL_RESOURCE_RESTRICT";
				ChargeRecord chargeRecord = new ChargeRecord();
				chargeRecord.setDatecenterId(rdsInstance.getDcId());
				chargeRecord.setResourceId(rdsInstance.getRdsId());
				chargeRecord.setResourceType(ResourceType.RDS);
				chargeRecord.setCusId(project.getCustomerId());
				chargeRecord.setOpTime(new Date());
				jedisUtil.set(RedisKey.CHARGE_RDS_RESTRICTED + rdsId, "1");
				//调用消息队列发送接口
				rabbitTemplate.send(key, JSONObject.toJSONString(chargeRecord));
			}else{
				log.info("该资源" + rdsId +"已经被限制过服务了");
			}
		}
		if(isResumable){
			// 恢复服务
			String key = "BILL_RESOURCE_RECOVER";
			ChargeRecord chargeRecord = new ChargeRecord();
			chargeRecord.setDatecenterId(rdsInstance.getDcId());
			chargeRecord.setResourceId(rdsInstance.getRdsId());
			chargeRecord.setResourceType(ResourceType.RDS);
			chargeRecord.setCusId(project.getCustomerId());
			chargeRecord.setOpTime(new Date());
			/* 调用消息队列发送接口 */
			rabbitTemplate.send(key, JSONObject.toJSONString(chargeRecord));
			log.info("该资源" + rdsInstance.getRdsId() + "已被恢复服务，将redis对应的限制服务标志位置为0");
			jedisUtil.set(RedisKey.CHARGE_RDS_RESTRICTED + rdsInstance.getRdsId(), "0");
		}
		cloudRDSInstanceDao.saveOrUpdate(rdsInstance);
	}

	/**
	 * 根据数据库实例ID获取数据库实例的Bean
	 * @param rdsId
	 * 				-- 数据库实例ID
	 * @return
	 */
	@Override
	public CloudRDSInstance findRDSInstanceByRdsId(String rdsId) {
		BaseCloudRDSInstance rdsInstance = cloudRDSInstanceDao.findOne(rdsId);
		if(null != rdsInstance){
			CloudRDSInstance cloudRDSInstance = new CloudRDSInstance();
			BeanUtils.copyPropertiesByModel(cloudRDSInstance, rdsInstance);
			return cloudRDSInstance;
		}
		return null;
	}

	/**
	 * 资源升级接口(CPU，内存和数据盘大小)
	 * 
	 * @param cloudOrder
	 * 					-- 订单信息
	 * @param user
	 * 					-- 用户信息
	 * @return
	 * @throws Exception
	 * 
	 */
	@Override
	public String resizeRdsInstance(CloudOrderRDSInstance cloudOrder, SessionUserInfo user) throws Exception {

		String errMsg = null;
		try {
			CloudRDSInstance cloudRdsInstance = this.queryRdsInstanceChargeById(cloudOrder.getRdsId());
			cloudOrder.setCreateUser(user.getUserName());
			cloudOrder.setCreateOrderDate(new Date());
			cloudOrder.setPayType(cloudRdsInstance.getPayType());
			cloudOrder.setRdsName(cloudRdsInstance.getRdsName());
			cloudOrder.setEndTime(cloudRdsInstance.getEndTime());
			cloudOrder.setRdsInstanceCpu(cloudRdsInstance.getCpu());
			cloudOrder.setRdsInstanceRam(cloudRdsInstance.getRam());
			cloudOrder.setDiskSize(cloudRdsInstance.getVolumeSize());
			cloudOrder.setCusId(user.getCusId());
			if(!StringUtils.isEmpty(cloudOrder.getIsMaster()) && "1".equals(cloudOrder.getIsMaster())){
				cloudOrder.setMasterId(null); // 避免从库的升级master_id出现null(字符串)值(cloudorder_rdsinstance)
			}
			if (cloudOrder.getRdsInstanceRam() != cloudRdsInstance.getRam() ||
					cloudOrder.getRdsInstanceCpu() != cloudRdsInstance.getCpu() ||
					cloudOrder.getDiskSize() != cloudRdsInstance.getVolumeSize()){
				errMsg = "CHANGE_OF_STANDARD";
				return errMsg;
			}
			if (PayType.PAYAFTER.equals(cloudOrder.getPayType())) {
				MoneyAccount accountMoney = accountOverviewSerivce.getAccountInfo(user.getCusId());
				BigDecimal zero = new BigDecimal(0);
				if (accountMoney.getMoney().compareTo(zero) <= 0) {
					errMsg = "ARREARS_OF_BALANCE";
					return errMsg;
				}
			}
			if(PayType.PAYBEFORE.equals(cloudOrder.getPayType())){
				cloudOrder.setCycleCount(cloudRdsInstance.getCycleCount());
				BigDecimal totalPayment = calcRdsInstancePrice(cloudOrder);
				if(cloudOrder.getPrice().compareTo(totalPayment)!= 0){
					return "CHANGE_OF_BILLINGFACTORY";
				}
			}
			if(checkRdsInstanceOrderExsit(cloudOrder.getRdsId())){
				errMsg = "UPGRADING_OR_INORDER";
				return errMsg;
			}
			
			Order order = createRdsInstanceOrder(cloudOrder, user);
			cloudOrder.setOrderNo(order.getOrderNo());

			cloudOrderRDSInstanceService.saveOrUpdate(cloudOrder);
			
			if (PayType.PAYAFTER.equals(cloudOrder.getPayType())) {
				try{
					this.resizeRdsInstance(cloudOrder);
				}catch(Exception e){
				    log.error(e.getMessage(),e);
					throw new Exception(e.getMessage());
				}
			}
			return errMsg;
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			throw e;
		}
	}

	/**
	 * 判断需要升级的种类（包括三种：只升级规格，只升级数据盘，二者都升级）
	 * 
	 * @param order
	 * 						-- 数据库信息
	 * @throws Exception
	 */
	@Transactional(noRollbackFor=AppException.class)
	public void resizeRdsInstance(CloudOrderRDSInstance order) throws Exception {
		CloudRDSInstance cloudRdsInstance = this.queryRdsInstanceChargeById(order.getRdsId()); // 获取oldFlavorId
		CloudRDSInstance rdsInstance = new CloudRDSInstance();
		rdsInstance.setDcId(order.getDcId());
		rdsInstance.setPrjId(order.getPrjId());
		rdsInstance.setRdsId(order.getRdsId());
		rdsInstance.setCpu(order.getCpu());
		rdsInstance.setRam(order.getRam());
		rdsInstance.setVolumeSize(order.getVolumeSize());
		rdsInstance.setVolumeTypeName(cloudRdsInstance.getVolumeTypeName());
		rdsInstance.setOrderNo(order.getOrderNo());
		rdsInstance.setCusId(order.getCusId());
		rdsInstance.setRdsName(order.getRdsName());
		rdsInstance.setPayType(order.getPayType());
		rdsInstance.setOldFlavorId(cloudRdsInstance.getOldFlavorId());
		// 只升级规格
		if(cloudRdsInstance.getCpu() != order.getCpu() || 
				cloudRdsInstance.getRam() != order.getRam()){
			if(cloudRdsInstance.getVolumeSize() == order.getVolumeSize()){
				// 只升级规格
				resizeRdsInstanceFlavor(rdsInstance, false);
			}else{
				// 即升级规格又升级数据盘
				resizeRdsInstanceFlavor(rdsInstance, true);
			}
			
		}else if(cloudRdsInstance.getVolumeSize() != order.getVolumeSize()){
			// 只升级数据盘
			rdsInstance.setFlavorId(cloudRdsInstance.getOldFlavorId());//  规格保持不变
			resizeRdsInstanceVolume(rdsInstance, false);
		}
	}
	/**
	 * 云数据库实例升级(内存和CPU)
	 * 
	 * @param rdsInstance
	 * 					-- 数据库实例信息
	 * @throws Exception
	 */
	private void resizeRdsInstanceFlavor(CloudRDSInstance rdsInstance, boolean isResizeVolume) throws Exception {
		BaseCloudFlavor cloudFlavor = new BaseCloudFlavor();
		try{
			cloudFlavor.setFlavorVcpus(rdsInstance.getCpu());
			cloudFlavor.setFlavorRam(rdsInstance.getRam() * 1024);
			cloudFlavor.setFlavorDisk(ConstantClazz.RDS_FLAVOR_DISK);
			cloudFlavor.setDcId(rdsInstance.getDcId());
			cloudFlavor.setPrjId(rdsInstance.getPrjId());
			
			// 创建云主机类型
			cloudFlavorService.createFlavor(cloudFlavor);
			// 数据库实例升级操作（规格）
			openstackRDSInstanceService.resizeFlavor(rdsInstance.getDcId(), rdsInstance.getPrjId(), 
					rdsInstance.getRdsId(), cloudFlavor.getFlavorId());
			
			BaseCloudRDSInstance rds = new BaseCloudRDSInstance();
			rds = cloudRDSInstanceDao.findOne(rdsInstance.getRdsId());
			rdsInstance.setEndTime(rds.getEndTime());
			rds.setRdsStatus("RESIZE");
			cloudRDSInstanceDao.saveOrUpdate(rds);
			rdsInstance.setFlavorId(cloudFlavor.getFlavorId());
			JSONObject json = new JSONObject();
			json.put("rdsId", rds.getRdsId());
			json.put("rdsName", rds.getRdsName());
			json.put("dcId", rds.getDcId());
			json.put("prjId", rds.getPrjId());
			json.put("rdsStatus", rds.getRdsStatus());
			json.put("orderNo", rdsInstance.getOrderNo());
			json.put("cusId", rdsInstance.getCusId());
			json.put("payType", rdsInstance.getPayType());
			json.put("cpu", rdsInstance.getCpu());
			json.put("ram", rdsInstance.getRam());
			json.put("volumeSize", rdsInstance.getVolumeSize());
			json.put("volumeTypeName", rdsInstance.getVolumeTypeName());
			json.put("endTime", rdsInstance.getEndTime());
			json.put("resizeType", isResizeVolume?"1":"0");
			json.put("oldFlavorId", rdsInstance.getOldFlavorId());
			json.put("flavorId", rdsInstance.getFlavorId());
			json.put("count", "0");
			final JSONObject data = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.rdsKey, data.toJSONString());
				}
				
			});
			
		}catch (AppException e) {
			upgradeFailHandler(rdsInstance, false);
			throw e;
		} catch (Exception e) {
			upgradeFailHandler(rdsInstance, false);
			log.error(e.getMessage(),e);
			throw new AppException("error.openstack.message");
		}
	}
	
	/**
	 * 升级云硬盘操作
	 * 
	 * @param cloudRDSInstance
	 * 						-- 需要升级的数据库实例的信息
	 * @param isNeedRollback
	 * 						-- 是否需要回滚（规格），true:需要（对应resizeType=1），false：不需要(对应resizeType=2)
	 * @throws Exception
	 */
	@Transactional(noRollbackFor=AppException.class)
	public void resizeRdsInstanceVolume(CloudRDSInstance cloudRDSInstance, boolean isNeedRollback) throws Exception {
		try{
			openstackRDSInstanceService.resizeVolume(cloudRDSInstance.getDcId(), 
					cloudRDSInstance.getPrjId(), cloudRDSInstance.getRdsId(),
					cloudRDSInstance.getVolumeSize());
			if(!isNeedRollback){
				BaseCloudRDSInstance rds = new BaseCloudRDSInstance();
				rds = cloudRDSInstanceDao.findOne(cloudRDSInstance.getRdsId());
				rds.setRdsStatus("RESIZE");
				cloudRDSInstanceDao.saveOrUpdate(rds);
			}
			JSONObject json = new JSONObject();
			json.put("rdsId", cloudRDSInstance.getRdsId());
			json.put("rdsName", cloudRDSInstance.getRdsName());
			json.put("dcId", cloudRDSInstance.getDcId());
			json.put("prjId", cloudRDSInstance.getPrjId());
			json.put("rdsStatus", "RESIZE");
			json.put("orderNo", cloudRDSInstance.getOrderNo());
			json.put("cusId", cloudRDSInstance.getCusId());
			json.put("payType", cloudRDSInstance.getPayType());
			json.put("cpu", cloudRDSInstance.getCpu());
			json.put("ram", cloudRDSInstance.getRam());
			json.put("volumeSize", cloudRDSInstance.getVolumeSize());
			json.put("volumeTypeName", cloudRDSInstance.getVolumeTypeName());
			json.put("endTime", cloudRDSInstance.getEndTime());
			json.put("oldFlavorId", cloudRDSInstance.getOldFlavorId());
			json.put("flavorId", cloudRDSInstance.getFlavorId());
			json.put("resizeType", "2");
			json.put("count", "0");
			final JSONObject data = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.rdsKey, data.toJSONString());
				}
				
			});
		}catch (AppException e) {
			upgradeFailHandler(cloudRDSInstance, isNeedRollback);
			throw e;
		} catch (Exception e) {
			upgradeFailHandler(cloudRDSInstance, isNeedRollback);
			log.error(e.getMessage(),e);
			throw new AppException("error.openstack.message");
		}
	}

	/**
	 * 升级失败处理（升级云主机规格失败）
	 * @param rdsInstance
	 * 					-- 数据库实例信息
	 * @throws Exception
	 */
	@Override
	public void upgradeFailHandler(CloudRDSInstance rdsInstance, boolean isRollBack) throws Exception {
		try{
			if(isRollBack){
				// 回滚云主机规格
				openstackRDSInstanceService.resizeFlavor(rdsInstance.getDcId(), rdsInstance.getPrjId(), 
						rdsInstance.getRdsId(), rdsInstance.getOldFlavorId());
				JSONObject json = new JSONObject();
				json.put("rdsId", rdsInstance.getRdsId());
				json.put("rdsName", rdsInstance.getRdsName());
				json.put("dcId", rdsInstance.getDcId());
				json.put("prjId", rdsInstance.getPrjId());
				json.put("rdsStatus", "RESIZE");
				json.put("orderNo", rdsInstance.getOrderNo());
				json.put("cusId", rdsInstance.getCusId());
				json.put("payType", rdsInstance.getPayType());
				json.put("cpu", rdsInstance.getCpu());
				json.put("ram", rdsInstance.getRam());
				json.put("volumeSize", rdsInstance.getVolumeSize());
				json.put("volumeTypeName", rdsInstance.getVolumeTypeName());
				json.put("endTime", rdsInstance.getEndTime());
				json.put("resizeType", "3");
				json.put("oldFlavorId", rdsInstance.getOldFlavorId());
				json.put("count", "0");
				final JSONObject data = json;
				TransactionHookUtil.registAfterCommitHook(new Hook() {
					@Override
					public void execute() {
						jedisUtil.addUnique(RedisKey.rdsKey, data.toJSONString());
					}
					
				});
			}
		}catch(Exception e){
			Order order = orderService.getOrderByNo(rdsInstance.getOrderNo());
			MessageCloudDataBaseRollBackModel cloudDataBaseRollBackModel = new MessageCloudDataBaseRollBackModel();
			cloudDataBaseRollBackModel.setCloudDataId(rdsInstance.getRdsId());
			cloudDataBaseRollBackModel.setOrderNo(rdsInstance.getOrderNo());
			cloudDataBaseRollBackModel.setOrderName(order.getProdName());
			cloudDataBaseRollBackModel.setCloudDataBaseName(rdsInstance.getRdsName());
			cloudDataBaseRollBackModel.setOrderCancelTime(new Date());
			cloudDataBaseRollBackModel.setResourceTypeName(ResourceType.getName(ResourceType.RDS));
			List<MessageCloudDataBaseRollBackModel> resoureList = new ArrayList<MessageCloudDataBaseRollBackModel>();
			resoureList.add(cloudDataBaseRollBackModel);
			messageCenterService.cloudDataBaseRollBackFail(rdsInstance.getCusId(), resoureList);
			throw e;
		}finally{
			// 订单完成接口（失败）
			orderService.completeOrder(rdsInstance.getOrderNo(), false, null);
			messageCenterService.addResourFailMessage(rdsInstance.getOrderNo(), rdsInstance.getCusId());		
		}
	}

	/**
	 * 云主机升级成功处理
	 * 
	 * @param cloudRdsInstance
	 * @throws Exception 
	 */
	@Override
	public void upgradeSuccessHandler(CloudRDSInstance cloudRdsInstance) throws Exception{
		List<BaseOrderResource> orderResources = new ArrayList<BaseOrderResource>();
		BaseOrderResource orderResource = new BaseOrderResource();
		orderResource.setResourceId(cloudRdsInstance.getRdsId());
		orderResource.setResourceName(cloudRdsInstance.getRdsName());
		orderResource.setOrderNo(cloudRdsInstance.getOrderNo());
		orderResources.add(orderResource);
		
		BaseOrder order = orderService.completeOrder(cloudRdsInstance.getOrderNo(), true, orderResources,false,cloudRdsInstance.getEndTime());
		cloudRdsInstance.setOpDate(order.getCompleteTime());
		if(PayType.PAYAFTER.equals(cloudRdsInstance.getPayType())){
			rdsInstanceUpgradeCharge(cloudRdsInstance);
		}
		// 升级成功后，更新上层数据库中的数据
		BaseCloudRDSInstance baseRds = cloudRDSInstanceDao.findOne(cloudRdsInstance.getRdsId());
		baseRds.setVolumeSize(cloudRdsInstance.getVolumeSize());
		baseRds.setFlavorId(cloudRdsInstance.getFlavorId());
		baseRds.setRdsStatus("ACTIVE");
		cloudRDSInstanceDao.saveOrUpdate(baseRds);
	}
	
	/**
	 * 云数据库实例升级计费
	 * 
	 * @param cloudRdsInstance
	 * 							-- 待升级的云数据库实例的信息
	 */
	public void rdsInstanceUpgradeCharge(final CloudRDSInstance cloudRdsInstance) {
		TransactionHookUtil.registAfterCommitHook(new Hook() {
			@Override
			public void execute() {
				ChargeRecord record = new ChargeRecord();
				ParamBean param = new ParamBean();
				param.setNumber(1);
				param.setCloudMySQLCPU(cloudRdsInstance.getCpu());
				param.setCloudMySQLRAM(cloudRdsInstance.getRam());
				switch (cloudRdsInstance.getVolumeTypeName()) {
				case "Normal":
					param.setStorageMySQLOrdinary(cloudRdsInstance.getVolumeSize());
					break;
				case "Medium":
					param.setStorageMySQLBetter(cloudRdsInstance.getVolumeSize());
					break;
				default:
					break;
				}
				record.setParam(param);
				record.setDatecenterId(cloudRdsInstance.getDcId());
				record.setOrderNumber(cloudRdsInstance.getOrderNo());
				record.setCusId(cloudRdsInstance.getCusId());
				record.setResourceId(cloudRdsInstance.getRdsId());
				record.setResourceType(ResourceType.RDS);
				record.setChargeFrom(cloudRdsInstance.getOpDate());
				
				rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_UPGRADE, JSONObject.toJSONString(record));
			}
		});
	}
	/**
	 * 根据数据库实例ID获取数据库实例的信息，用户升级是计费
	 * 
	 * @param rdsId
	 * 				-- 数据库实例ID
	 * @return
	 */
	public CloudRDSInstance queryRdsInstanceChargeById(String rdsId) {
		CloudRDSInstance cloudRdsInstance = new CloudRDSInstance();
		StringBuffer sql = new StringBuffer();
		
		sql.append("   select cr.rds_id,                                              ");
		sql.append("   cr.pay_type,                                                   ");
		sql.append("   cr.volume_size,                                                ");
		sql.append("   cr.rds_name,                                                   ");
		sql.append("   cr.end_time,                                                   ");
		sql.append("   cf.flavor_vcpus,                                               ");
		sql.append("   cf.flavor_ram,                                                 ");
		sql.append("   cf.flavor_id,                                                  ");
		sql.append("   cv.type_name                                                   ");
		sql.append("   from cloud_rdsinstance cr                                      ");
		sql.append("   LEFT JOIN cloud_flavor cf on cr.flavor_id = cf.flavor_id       ");
		sql.append("   LEFT JOIN cloud_volumetype cv on cr.volume_type = cv.type_id   ");
		sql.append("   where cr.rds_id = ?                                            ");
		sql.append("   and cr.is_deleted = ?                                          ");

		javax.persistence.Query query = cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(), new Object[]{rdsId, '0'});
		@SuppressWarnings("rawtypes")
		List list = new ArrayList();
		if(null != query){
			list = query.getResultList();
		}
		if(list != null && list.size() == 1){
			Object[] objs = (Object[]) list.get(0);
			int ind = 0;
			cloudRdsInstance = new CloudRDSInstance();
			cloudRdsInstance.setRdsId(String.valueOf(objs[ind++]));
			cloudRdsInstance.setPayType(String.valueOf(objs[ind++]));
			cloudRdsInstance.setVolumeSize(Integer.parseInt(objs[ind++] != null ? String.valueOf(objs[ind-1]) : "0"));
			cloudRdsInstance.setRdsName(String.valueOf(objs[ind++]));
			cloudRdsInstance.setEndTime((Date)objs[ind++]);
			cloudRdsInstance.setCpu(Integer.parseInt(objs[ind++] != null ? String.valueOf(objs[ind-1]) : "0"));
			cloudRdsInstance.setRam(Integer.parseInt(objs[ind++] != null ? String.valueOf(objs[ind-1]) : "0") / 1024);
			cloudRdsInstance.setOldFlavorId(String.valueOf(objs[ind++]));
			cloudRdsInstance.setVolumeTypeName(String.valueOf(objs[ind++]));
			if(null != cloudRdsInstance.getEndTime()){
				cloudRdsInstance.setCycleCount(DateUtil.getUgradeRemainDays(new Date(), cloudRdsInstance.getEndTime()));
			}
		}
		return cloudRdsInstance;
	}

	/**
	 * 查询资源是否存在的接口
	 * 
	 * @param rdsId
	 * 				-- 资源（数据库实例）ID
	 * @return
	 * 
	 */
	@Override
	public ResourceCheckBean isExistsByResourceId(String rdsId) {
		ResourceCheckBean bean = new ResourceCheckBean();
		boolean isExist = false;
		
		StringBuffer sql = new StringBuffer();
		
		sql.append("     SELECT                           ");
		sql.append("     		rds.rds_id,               ");
		sql.append("     		rds.rds_name              ");
		sql.append("     FROM cloud_rdsinstance rds       ");
		sql.append("     WHERE                            ");
		sql.append("     		rds.rds_id  = ?           ");
		sql.append("     AND rds.is_visible = '1'         ");
		sql.append("     AND rds.is_deleted = '0'         ");

		
		javax.persistence.Query query = cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(), new Object[]{rdsId});
		@SuppressWarnings("rawtypes")
		List listResult = query.getResultList();
		if(null != listResult && listResult.size() == 1) {
			isExist = true;
			Object [] objs = (Object [])listResult.get(0);
			bean.setResourceName(String.valueOf(objs[1]));
		}
		
		bean.setExisted(isExist);
		return bean;
	}

	/**
	 * 根据数据库实例ID判断是否为主库
	 * 
	 * @param rdsId
	 * 				-- 实例ID
	 * @return
	 * 				true:主库；false:从库(资源不存在也会返回false)
	 * 
	 */
	@Override
	public boolean isMasterRdsInstance(String rdsId) {
		StringBuffer sql = new StringBuffer();
		
		sql.append("   SELECT rds.is_master         ");
		sql.append("   FROM cloud_rdsinstance rds   ");
		sql.append("   WHERE rds.rds_id = ?         ");

		javax.persistence.Query query = cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(), new Object[]{rdsId});
		@SuppressWarnings("rawtypes")
		List listResult = query.getResultList();
		if(null != listResult && listResult.size() == 1){
			if("1".equals(listResult.get(0).toString()))
				return true;
		}
		return false;
	}

	/**
	 * 获取到期时间<=endTime的状态为state的payType主机信息
	 * 
	 * @param prjId		-- 项目ID
	 * @param endTime   -- 到期时间
	 * @param state		-- 实例状态 正常/已过期/已欠费
	 * @param payType   -- 付款方式 1为预付费 2为后付费
	 * @param cusState  -- 客户状态 0为正常 1为已冻结 null为所有
	 * @return
	 * @throws Exception
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<CloudRDSInstance> queryNormalRdsInstance(String prjId, Date endTime, String state, String payType,
			String cusState) throws Exception {
		List<CloudRDSInstance> cloudRdsInstanceList = new ArrayList<CloudRDSInstance>();
		List list = new ArrayList();
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT cr.rds_id, cr.rds_name FROM cloud_rdsinstance cr  ");
		if (cusState != null && cusState.length() > 0) {
			sb.append(" LEFT JOIN cloud_project prj on cr.prj_id=prj.prj_id  ");
			sb.append(" LEFT JOIN sys_selfcustomer cus on cus.cus_id=prj.customer_id ");
		}
		sb.append(" where cr.is_deleted = '0' ");
		if(prjId!=null&&prjId.length()>0){
			sb.append(" and cr.prj_id=? ");
			list.add(prjId);
		}
		if (endTime != null) {
			sb.append(" and cr.end_time<=? ");
			list.add(endTime);
		}
		if (state != null && state.length() > 0) {
			sb.append(" and cr.charge_state=? ");
			list.add(state);
		}
		if (payType != null && payType.length() > 0) {
			sb.append(" and cr.pay_type=? ");
			list.add(payType);
		}
		if(cusState!=null&&cusState.length()>0){
			sb.append(" and cus.is_blocked=? ");
			list.add(cusState);
		}
		sb.append(" and cr.is_visible ='1' ");
		javax.persistence.Query query= cloudRDSInstanceDao.createSQLNativeQuery(sb.toString(),list.toArray());
		List result = query.getResultList();
		if(result!=null&&result.size()>0){
			for (int i = 0; i < result.size(); i++) {
				Object[] objs = (Object[])result.get(i);
				CloudRDSInstance cloudRdsInstance = new CloudRDSInstance();
				cloudRdsInstance.setRdsId(String.valueOf(objs[0]));
				cloudRdsInstance.setRdsName(String.valueOf(objs[1]));
				cloudRdsInstanceList.add(cloudRdsInstance);
			}
		}
		return cloudRdsInstanceList;
	}

	/**
	 * 获取价格（明细，规格和数据盘的价格）
	 * 
	 * @param paramBean
	 * 
	 * @return
	 */
	@Override
	public PriceDetails getPriceDetails(ParamBean paramBean) {
		PriceDetails priceDetails = billingFactorService.getPriceDetails(paramBean);
		BigDecimal total = priceDetails.getTotalPrice();
		BigDecimal specPrice = new BigDecimal(0); // 实例规格价格
		BigDecimal volumePrice = new BigDecimal(0); // 实例规格价格
		BigDecimal number = new BigDecimal(paramBean.getNumber()); // 批量数，为1
		if(null != priceDetails.getCpuPrice() && null != priceDetails.getRamPrice()){
			specPrice = number.multiply(priceDetails.getCpuPrice().add(priceDetails.getRamPrice()));
		}
		if(null != priceDetails.getDataDiskPrice()){
			volumePrice = number.multiply(priceDetails.getDataDiskPrice());
		}
		if(paramBean.getPayType().equals(PayType.PAYAFTER)){
			total = handleMinValue(total);
			specPrice = handleMinValue(specPrice);
			volumePrice = handleMinValue(volumePrice);
		}
		priceDetails.setTotalPrice(total.setScale(2, RoundingMode.FLOOR));
		if(null !=priceDetails.getCpuPrice() && null != priceDetails.getRamPrice()){
			priceDetails.setCpuPrice(specPrice.setScale(2, RoundingMode.FLOOR)); // 对应前台实例规格的价格
		}
		return priceDetails;
	}
	private BigDecimal handleMinValue(BigDecimal decimal){
		BigDecimal minValue = new BigDecimal(0.01);
		if(decimal.compareTo(BigDecimal.ZERO)>0 && decimal.compareTo(minValue)<0){
			return minValue;
		}
		return decimal;
	}

	/**
	 * 获取MySQL的版本
	 * 
	 * @param dcId
	 * 				-- 数据中心ID
	 * @return
	 */
	@Override
	public List<Map<String, String>> getVersionList(String dcId) {
		List<Map<String, String>> versionList = null;
		StringBuffer sql = new StringBuffer();
		sql.append("   SELECT cdv.name,                       ");
		sql.append("   		cdv.id                            ");
		sql.append("   FROM cloud_datastore cd                ");
		sql.append("   LEFT JOIN cloud_datastoreversion cdv   ");
		sql.append("   ON cd.id = cdv.datastore_id            ");
		sql.append("   WHERE cd.name = 'mysql'                ");
		sql.append("   AND cd.dc_id = ?                       ");
		javax.persistence.Query query= cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(),new Object[]{dcId});
		List result = query.getResultList();
		if(result!=null&&result.size()>0){
			versionList = new ArrayList<Map<String, String>>();
			for (int i = 0; i < result.size(); i++) {
				Object[] objs = (Object[])result.get(i);
				Map<String, String> map = new HashMap<String, String>();
				map.put("versionName", Datastore.getTypeName("mysql") + " " + String.valueOf(objs[0]));
				map.put("versionId", String.valueOf(objs[1]));
				versionList.add(map);
			}
		}
		return versionList;
	}

	/**
	 * 根据订单编号获取云数据库实例订单信息，用于重新下单
	 * 
	 * @param orderNo
	 * 
	 * @return
	 */
	@Override
	public CloudOrderRDSInstance getInstanceByOrderNo(String orderNo) {
		return cloudOrderRDSInstanceService.getRdsOrderByOrderNo(orderNo);
	}

	/**
	 * 根据实例ID获取该实例的规格和数据盘大小信息
	 * @param rdsId
	 * @return
     */
	@Override
	public Map<String, Integer> getStandardByRdsId(String rdsId) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		StringBuffer sql = new StringBuffer();
		sql.append("   SELECT cf.flavor_ram,                                      ");
		sql.append("   			cf.flavor_vcpus,                                    ");
		sql.append("   			cr.volume_size                                      ");
		sql.append("   FROM cloud_rdsinstance cr                                  ");
		sql.append("   LEFT JOIN cloud_flavor cf ON cr.flavor_id = cf.flavor_id   ");
		sql.append("   WHERE rds_id = ?                                           ");
		javax.persistence.Query query= cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(),new Object[]{rdsId});
		List result = query.getResultList();
		if(result != null && result.size() > 0){
			Object [] obj = (Object[])result.get(0);
			map.put("volumeSize", Integer.parseInt(obj[2] != null ? String.valueOf(obj[2]) : "0"));
			map.put("cpu", Integer.parseInt(obj[1] != null ? String.valueOf(obj[1]) : "0"));
			map.put("ram", Integer.parseInt(obj[0] != null ? String.valueOf(obj[0]) : "0") / 1024);
		}
		return map;
	}
}