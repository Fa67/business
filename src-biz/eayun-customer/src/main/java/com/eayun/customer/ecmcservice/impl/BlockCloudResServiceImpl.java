package com.eayun.customer.ecmcservice.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.accesskey.service.AccessKeyService;
import com.eayun.charge.model.ChargeRecord;
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.Hook;
import com.eayun.customer.dao.CusBlockCloudResDao;
import com.eayun.customer.ecmcservice.BlockCloudResService;
import com.eayun.customer.ecmcservice.EcmcCustomerService;
import com.eayun.customer.model.BaseCustomer;
import com.eayun.customer.model.CusBlockResource;
import com.eayun.customer.model.CusBlockResourceVoe;
import com.eayun.customer.model.Customer;
import com.eayun.eayunstack.service.OpenstackVmService;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.project.ecmcservice.EcmcProjectService;
import com.eayun.virtualization.ecmcservice.EcmcCloudVmService;
import com.eayun.virtualization.model.BaseCloudVm;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.model.CloudVm;

/**
 * @Filename: BlockCloudResServiceImpl.java
 * @Description: 用于客户管理中对资源的冻结解冻操作
 * @Version: 1.0
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com 
 * @date 2016年7月21日
 *
 */
@Service
@Transactional
public class BlockCloudResServiceImpl implements BlockCloudResService {

	private static final Logger log = LoggerFactory.getLogger(BlockCloudResServiceImpl.class);
	@Autowired
	private EcmcCloudVmService ecmcCloudVmService;
	@Autowired
	private CusBlockCloudResDao cusBlockResDao;
	@Autowired
	private EcmcCustomerService ecmcCustomerService;
	@Autowired
	private EcmcProjectService ecmcProjectService;
	@Autowired
	private AccessKeyService accesskeyService;
	@Autowired
    private EcmcLogService ecmcLogService;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private OpenstackVmService openstackVmService;
	@Autowired
	private EayunRabbitTemplate rabbitTemplate;
	
	@Autowired
	private MessageCenterService messageCenterService;
	
	
	/**
	 * 
	 * 根据冻结记录表的有无，对冻结记录表进行保存，返回值必不为空值
	 * */
	private CusBlockResource saveOrUpdateCusBlockResource(List<CloudVm> orgVmList,Date blockDate,String cusId,boolean isAllBlock){
//		根据客户id查询此客户是否存在被运维上次“冻结”失败的情况。
		CusBlockResource blockResource = new CusBlockResource();
		blockResource = cusBlockResDao.getResourceByCusId(cusId);
		JSONObject resourceJson = new JSONObject();
		JSONArray arrayJson = new JSONArray();
		if(null!=blockResource){
			resourceJson = JSONObject.parseObject(blockResource.getBlockCloudResource());
			JSONObject vmJson = resourceJson.getJSONObject("block");
			if(null !=vmJson){
				arrayJson = vmJson.getJSONArray("vm");
				if(null != arrayJson){
					for(int i=0; i<arrayJson.size(); i++){//遍历更新冻结vm字段状态的记录，并保存数据库
						CloudVm vm = new CloudVm();
						String prjId = arrayJson.getJSONObject(i).getString("prjId");
						String dcId = arrayJson.getJSONObject(i).getString("dcId");
						String vmId = arrayJson.getJSONObject(i).getString("vmId");
						String blockStatus = arrayJson.getJSONObject(i).getString("blockStatus");
						
						if(blockStatus.equals("false")){
							for(CloudVm vmOrg :orgVmList){
								if(vmOrg.getVmId().equals(vmId) && "true".equals(vmOrg.getVmBlockStatus())){
									vm.setVmBlockStatus("true");
									blockStatus = "true";
									JSONObject newBlockResource = new JSONObject();
									newBlockResource.put("prjId", prjId);
									newBlockResource.put("dcId", dcId);
									newBlockResource.put("vmId", vmId);
									newBlockResource.put("blockStatus", blockStatus);
									arrayJson.set(i, newBlockResource);
									break;
								}
							}
						}
						
					}
					
				}
			}
			//将blockVmList中提取冻结记录，重新保存数据库
			blockResource.setBlockCloudResource(resourceJson.toJSONString());
			blockResource.setBlockopStatus(isAllBlock);
			blockResource.setUpdateTime(blockDate);
			//组织ecmc登陆账号信息
			String accountBlockJson = this.getNewAccountOperationJson(blockResource, blockDate,"blockAccount");
			blockResource.setBlockAccount(accountBlockJson);
			
			cusBlockResDao.saveOrUpdate(blockResource);//更新blockCloudResource字段
		}else{
			CusBlockResource emptyBlockResource = new CusBlockResource();
			//表示第一次点击“冻结客户”
			JSONObject blockJson = new JSONObject();
			JSONObject vmJson = new JSONObject();
			JSONArray cloudArrayJson = new JSONArray();
			//组织blockcloudResource字段
			for(int i=0;i< orgVmList.size();i++){
				JSONObject cloudJson = new JSONObject();
				cloudJson.put("prjId", orgVmList.get(i).getPrjId());
				cloudJson.put("dcId", orgVmList.get(i).getDcId());
				cloudJson.put("vmId", orgVmList.get(i).getVmId());	
				cloudJson.put("blockStatus", orgVmList.get(i).getVmBlockStatus());
				cloudArrayJson.add(i, cloudJson);
			}
			vmJson.put("vm", cloudArrayJson);
			blockJson.put("block", vmJson);
			//组织bean存入数据库
			emptyBlockResource.setCusId(cusId);
			emptyBlockResource.setBlockCloudResource(blockJson.toJSONString());//存入冻结的主机字段
			emptyBlockResource.setIsBlocked(true);//存入冻结状态
			emptyBlockResource.setUpdateTime(new Date());
			emptyBlockResource.setBlockopStatus(isAllBlock);//存入冻结资源是否成功
			//组织ecmc登陆账号信息
			String accountBlockJson = this.getNewAccountOperationJson(emptyBlockResource, blockDate,"blockAccount");
			emptyBlockResource.setBlockAccount(accountBlockJson);
			cusBlockResDao.saveEntity(emptyBlockResource);
			blockResource = emptyBlockResource;
		}
		return blockResource; 
	}
	
	
	
	@Override
	@Transactional(noRollbackFor=AppException.class) 
	public CusBlockResourceVoe blockCloudResource(String cusId) throws Exception {
		//  第一步：冻结客户
		BaseCustomer baseCustomer = new BaseCustomer();
		baseCustomer = ecmcCustomerService.blockCustomer(cusId);
		//修改客户的冻结状态in Redis
		updateCusBlockStatusInRedis(cusId, "true");
		
		//  第二步：调用祝军团队提供的发消息接口，给客户的超级管理员发消息
		CusBlockResource resource = cusBlockResDao.getResourceByCusId(cusId);
		if(null==resource){
			messageCenterService.accountFrozenMessage(cusId);
		}
		
		//  第三步： 调用BlockCloudResService的blockCloudResource方法，冻结客户云资源。
		CusBlockResource blockResource = new CusBlockResource();
		List<CloudProject> projectList = ecmcProjectService.getProjectByCustomer(cusId);
		List<CloudVm> orgVmList= new ArrayList<CloudVm>();
		for(CloudProject project : projectList){
			String prjId = project.getProjectId();
			List<CloudVm> vmList = new ArrayList<CloudVm>();
			vmList = ecmcCloudVmService.getVmListByPrjIdAndVmStatus(prjId, "ACTIVE");
			for(CloudVm vm : vmList){
				orgVmList.add(vm);
			}
		}
		//vmList,循环挂起云主机。
		Date blockDate = new Date();
		boolean isAllBlock = true;
		int vmNum = orgVmList.size();
		int hadVmNum = 0;
		for(CloudVm vm :orgVmList){
			try {
				this.suspendVm(vm);
				vm.setVmBlockStatus("true");
			} catch (AppException e) {
				hadVmNum ++;
				log.error(e.getMessage(), e);
				vm.setVmBlockStatus("false");
				isAllBlock = false;
				ecmcLogService.addLog("冻结账号",ConstantClazz.LOG_TYPE_CUSTOMER, baseCustomer.getCusName(),null, 0,cusId,e);
			}catch (Exception e) {
			    log.error(e.getMessage(), e);
				isAllBlock = false;
				ecmcLogService.addLog("冻结账号",ConstantClazz.LOG_TYPE_CUSTOMER, baseCustomer.getCusName(),null, 0,cusId,e);
			}
			
		}
		
		//更新cusBlockResource表字段blockCloudResource、blockAccount  
		blockResource = this.saveOrUpdateCusBlockResource(orgVmList, blockDate, cusId, isAllBlock);
		
		baseCustomer.setBlockopStatus(isAllBlock);
		BaseCustomer cus = new BaseCustomer();
		cus = ecmcCustomerService.mergeBaseCustomer(baseCustomer);
		
		//第四步：暂停客户的AkSk，返回暂停的json信息。
		accesskeyService.stopRunningAkExceptDefaultByCusId(cusId,"1");
		CusBlockResourceVoe blockResourceVoe = new CusBlockResourceVoe();
		String errorMsg = "";
		if(hadVmNum !=0){
			errorMsg="客户"+cus.getCusOrg()+"账号冻结，批量暂停云主机"+vmNum+"台，操作失败"+hadVmNum+"台；请运维人员检查原因，排查好后，重新冻结!";
		}
		blockResourceVoe.setErrorMsg(errorMsg);
		blockResourceVoe.setCusName(cus.getCusName());
		BeanUtils.copyPropertiesByModel(blockResourceVoe, blockResource);
		return blockResourceVoe;
	}
	/**
	 * 挂起云主机
	 * @Author: liyanchao
	 * @param cloudVm
	 *<li>Date: 2016年4月26日</li>
	 */
	
	private void suspendVm(CloudVm cloudVm) throws AppException {
		try{
			openstackVmService.suspendVm(cloudVm);
			BaseCloudVm vm = new BaseCloudVm();
			vm = ecmcCloudVmService.findBaseVmByVmId(cloudVm.getVmId());
			vm.setVmStatus("SUSPENDEDING");//挂起中
			ecmcCloudVmService.mergeBaseVm(vm);
			
			JSONObject json =new JSONObject();
			json.put("vmId", vm.getVmId());
			json.put("dcId",vm.getDcId());
			json.put("prjId", vm.getPrjId());
			json.put("vmStatus", vm.getVmStatus());
			json.put("perStatus", "ACTIVE");//运行中
			json.put("count", "0");
			json.put("isExsit", "0");
			
			jedisUtil.addUnique(RedisKey.vmKey, json.toJSONString());
			
		}catch(AppException e){
			throw e;
		}catch(Exception e){
			throw new AppException ("error.openstack.message");
		}
	}
	
	/**
	 * 更新操作冻结客户记录的json
	 * */
	@SuppressWarnings("unused")
    private String getNewAccountOperationJson(CusBlockResource resource,Date blockDate,String type){
		if("blockAccount".equals(type)){
			if(null != resource && null != resource.getBlockAccount()){//表示有cusblockresource
				JSONObject accountJson = JSONObject.parseObject(resource.getBlockAccount());
				JSONArray arrayJson = accountJson.getJSONArray("block");
				
				//需要再组织json表示block_account字段
				BaseEcmcSysUser loginUser = EcmcSessionUtil.getUser();
				JSONObject newAccount = new JSONObject();
				newAccount.put("operateAccount", loginUser.getAccount());
				newAccount.put("operateTime", DateUtil.dateToString(blockDate));
				
				boolean flag = arrayJson.add(newAccount);
				return accountJson.toJSONString();
			}else{
				JSONObject accountJson = new JSONObject();
				JSONArray arrayJson = new JSONArray();
				BaseEcmcSysUser loginUser = EcmcSessionUtil.getUser();
				JSONObject newAccount = new JSONObject();
				newAccount.put("operateAccount", loginUser.getAccount());
				newAccount.put("operateTime", DateUtil.dateToString(blockDate));
				arrayJson.add(newAccount);
				accountJson.put("block", arrayJson);
				return accountJson.toJSONString();
			}
		}else {
			if(null != resource && null != resource.getUnblockAccount()){//表示有cusblockresource
				JSONObject accountJson = JSONObject.parseObject(resource.getUnblockAccount());
				JSONArray arrayJson = accountJson.getJSONArray("unblock");
				
				//需要再组织json表示block_account字段
				BaseEcmcSysUser loginUser = EcmcSessionUtil.getUser();
				JSONObject newAccount = new JSONObject();
				newAccount.put("operateAccount", loginUser.getAccount());
				newAccount.put("operateTime", DateUtil.dateToString(blockDate));
				
				boolean flag = arrayJson.add(newAccount);
				return accountJson.toJSONString();
			}else{
				JSONObject accountJson = new JSONObject();
				JSONArray arrayJson = new JSONArray();
				BaseEcmcSysUser loginUser = EcmcSessionUtil.getUser();
				JSONObject newAccount = new JSONObject();
				newAccount.put("operateAccount", loginUser.getAccount());
				newAccount.put("operateTime", DateUtil.dateToString(blockDate));
				arrayJson.add(newAccount);
				accountJson.put("unblock", arrayJson);
				return accountJson.toJSONString();
			}
		}
		
	}
	
	
	
	/**************************************解冻客户开始*****************************************/
	
	
	
	@Transactional(noRollbackFor=AppException.class) 
	public CusBlockResourceVoe unblockCloudResource(String cusId) throws Exception {
		Customer cus = ecmcCustomerService.getCustomerById(cusId);
		List<CloudVm> needUnBlockVmList = new ArrayList<CloudVm>();
		needUnBlockVmList = this.getNeedUnblockVmList(cusId);
		//解冻主机
		boolean isAllBlock = false;
		int vmNum = needUnBlockVmList.size();
		int hadVmNum = 0;
		if(null!=needUnBlockVmList && needUnBlockVmList.size()>0){
			for(CloudVm vm :needUnBlockVmList){
				try {
					this.resumeVm(vm);
					vm.setVmBlockStatus("false");
				} catch (AppException e) {
					hadVmNum ++;
					log.error(e.getMessage(), e);
					vm.setVmBlockStatus("true");
					isAllBlock = true;
					ecmcLogService.addLog("恢复解冻",ConstantClazz.LOG_TYPE_CUSTOMER, cus.getCusName(),null, 0,cusId,e);
				}catch (Exception e) {
				    log.error(e.getMessage(), e);
				    isAllBlock = true;
					ecmcLogService.addLog("恢复解冻",ConstantClazz.LOG_TYPE_CUSTOMER, cus.getCusName(),null, 0,cusId,e);
				}
			}
		}
		Date unblockDate = new Date();
		CusBlockResource blockResource = new CusBlockResource();
		CusBlockResourceVoe blockResourceVoe = new CusBlockResourceVoe();
		blockResource = cusBlockResDao.getBlockedResourceByCusId(cusId);
		if(!isAllBlock && needUnBlockVmList.size()>0){
			//调用AccessKeyService启用客户obs的AK、SK。
			accesskeyService.resumeAkExceptDefaultByCusId(cusId,"1");
			CusBlockResource cusBlockResource = this.updateCusBlockResourceForSuccessBlock(blockResource, unblockDate, needUnBlockVmList);
			//第2步解冻客户
			BaseCustomer baseCustomer = new BaseCustomer();
			baseCustomer = ecmcCustomerService.unblockCustomer(cusId);
			//修改客户的冻结状态in Redis
			updateCusBlockStatusInRedis(cusId, "false");
			
			//解冻成功后发短信，为客户发送消息：“尊敬的客户：您的账户已解冻，请登录管理控制台查看，如有问题请致电400-606-6396。【易云】”；
			messageCenterService.accountRecoveryMessage(cusId); 
			
			BeanUtils.copyPropertiesByModel(blockResourceVoe, cusBlockResource);
			blockResourceVoe.setCusName(baseCustomer.getCusName());
			// 解冻对张帆发消息，开始计费
			ChargeRecord cr = new ChargeRecord();
            cr.setOpTime(unblockDate);
            cr.setCusId(cusId);
            final JSONObject json = (JSONObject) JSONObject.toJSON(cr);
            TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					rabbitTemplate.send("BILL_CUSTOMER_UNBLOCK",json.toJSONString());
				}
			});
			return blockResourceVoe;
		}else if(needUnBlockVmList.isEmpty()){//补充一个方法，解冻时 没有vm需要解冻，需要的操作
			//调用AccessKeyService启用客户obs的AK、SK。
			accesskeyService.resumeAkExceptDefaultByCusId(cusId,"1");
			CusBlockResource  cusBlockResource = this.updateCusBlockResourceForUnblockNoneVm(blockResource, unblockDate, needUnBlockVmList);
			//解冻客户
			BaseCustomer baseCustomer = new BaseCustomer();
			baseCustomer = ecmcCustomerService.unblockCustomer(cusId);
			//修改客户的冻结状态in Redis
			updateCusBlockStatusInRedis(cusId, "false");
			
			//解冻成功后发短信，为客户发送消息：“尊敬的客户：您的账户已解冻，请登录管理控制台查看，如有问题请致电400-606-6396。【易云】”；
			messageCenterService.accountRecoveryMessage(cusId); 
			
			BeanUtils.copyPropertiesByModel(blockResourceVoe, cusBlockResource);
			blockResourceVoe.setCusName(baseCustomer.getCusName());
			// 解冻对张帆发消息，开始计费
			ChargeRecord cr = new ChargeRecord();
            cr.setOpTime(unblockDate);
            cr.setCusId(cusId);
            final JSONObject json = (JSONObject) JSONObject.toJSON(cr);
            TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					rabbitTemplate.send("BILL_CUSTOMER_UNBLOCK",json.toJSONString());
				}
			});
            
			return blockResourceVoe;
		}else{//表示解冻失败，需要组织字段unblockresource update当前数据
			CusBlockResource cusResource = this.updateCusBlockResourceForFailedBlock(blockResource, unblockDate, needUnBlockVmList);
			BaseCustomer baseCustomer = ecmcCustomerService.getCustomerById(cusId);
			String errorMsg = "";
			if(hadVmNum !=0){
				errorMsg="客户"+baseCustomer.getCusOrg()+"账号解冻，批量恢复云主机"+vmNum+"台，操作失败"+hadVmNum+"台；请运维人员检查原因，排查好后，重新解冻!";
			}
			blockResourceVoe.setErrorMsg(errorMsg);
			blockResourceVoe.setCusName(baseCustomer.getCusName());
			BeanUtils.copyPropertiesByModel(blockResourceVoe, cusResource);
			
			return blockResourceVoe;
		}
		
		
		
		
	}
	/**
	 * 解冻客户成功时，对于没有主机的更新表 CusBlockResource
	 * **/
	private CusBlockResource updateCusBlockResourceForUnblockNoneVm(CusBlockResource blockResource,Date unblockDate,List<CloudVm> needUnBlockVmList){
		blockResource.setUpdateTime(unblockDate);
		blockResource.setIsBlocked(false);
		blockResource.setBlockopStatus(false);
		String accountOperation = this.getNewAccountOperationJson(blockResource, unblockDate, "unblockAccount");
		blockResource.setUnblockAccount(accountOperation);
		return blockResource;
	}
	/**
	 * 解冻主机成功对于cusblockresource表的update：(unblockAccount/unblockCloudResource)
	 * @Author: liyanchao
	 * @param cloudVm
	 *<li>Date: 2016年7月27日</li>
	 */
	private CusBlockResource updateCusBlockResourceForSuccessBlock(CusBlockResource blockResource,Date unblockDate,List<CloudVm> needUnBlockVmList){
		
		JSONObject resourceJson = new JSONObject();
		JSONArray arrayJson = new JSONArray();
		JSONObject vmJson = new JSONObject();
		
		resourceJson = JSONObject.parseObject(blockResource.getUnblockCloudResource());
		if(null !=resourceJson){
			vmJson = resourceJson.getJSONObject("unblock");
			arrayJson = vmJson.getJSONArray("vm");
			if(null != arrayJson){
				for(int i=0; i<arrayJson.size(); i++){//遍历更新冻结vm字段状态的记录，并保存数据库
					String prjId = arrayJson.getJSONObject(i).getString("prjId");
					String dcId = arrayJson.getJSONObject(i).getString("dcId");
					String vmId = arrayJson.getJSONObject(i).getString("vmId");
					String blockStatus = arrayJson.getJSONObject(i).getString("blockStatus");
					
					if(blockStatus.equals("true")){
						for(CloudVm vmOrg :needUnBlockVmList){
							if(vmOrg.getVmId().equals(vmId) && "false".equals(vmOrg.getVmBlockStatus())){
								blockStatus = "false";
								JSONObject newBlockResource = new JSONObject();
								newBlockResource.put("prjId", prjId);
								newBlockResource.put("dcId", dcId);
								newBlockResource.put("vmId", vmId);
								newBlockResource.put("blockStatus", blockStatus);
								arrayJson.set(i, newBlockResource);
								break;
							}
						}
					}
					
				}
				
			}
			blockResource.setUnblockCloudResource(resourceJson.toJSONString());
		}else{//第一次解冻时，UnblockCloudResource字段无值；
			for(CloudVm vmOrg :needUnBlockVmList){
				
					JSONObject newBlockResource = new JSONObject();
					newBlockResource.put("prjId", vmOrg.getPrjId());
					newBlockResource.put("dcId", vmOrg.getDcId());
					newBlockResource.put("vmId", vmOrg.getVmId());
					newBlockResource.put("blockStatus", vmOrg.getVmBlockStatus());
					arrayJson.add(newBlockResource);
			}
			vmJson.put("vm", arrayJson);
			JSONObject returnBlockResource = new JSONObject();
			returnBlockResource.put("unblock", vmJson);
			blockResource.setUnblockCloudResource(returnBlockResource.toJSONString());
		}
		
		blockResource.setUpdateTime(unblockDate);
		//组织ecmc登陆账号信息
		String accountBlockJson = this.getNewAccountOperationJson(blockResource, unblockDate,"unblockAccount");
		blockResource.setUnblockAccount(accountBlockJson);
		blockResource.setIsBlocked(false);
		blockResource.setBlockopStatus(false);
		cusBlockResDao.saveOrUpdate(blockResource);//更新blockCloudResource字段
		
		return blockResource;
	}
	
	
	/**
	 * 解冻主机失败对于cusblockresource表的update：(unblockAccount/unblockCloudResource)
	 * @Author: liyanchao
	 * @param cloudVm
	 *<li>Date: 2016年7月27日</li>
	 */
	private CusBlockResource updateCusBlockResourceForFailedBlock(CusBlockResource blockResource,Date unblockDate,List<CloudVm> needUnBlockVmList){
		JSONObject resourceJson = new JSONObject();
		JSONArray arrayJson = new JSONArray();
		JSONObject vmJson = new JSONObject();
		JSONObject returnResource = new JSONObject();
		resourceJson = JSONObject.parseObject(blockResource.getUnblockCloudResource());
		if(null !=resourceJson){
			vmJson = resourceJson.getJSONObject("unblock");
			arrayJson = vmJson.getJSONArray("vm");
			if(null != arrayJson){
				for(int i=0; i<arrayJson.size(); i++){//遍历更新冻结vm字段状态的记录，并保存数据库
					String prjId = arrayJson.getJSONObject(i).getString("prjId");
					String dcId = arrayJson.getJSONObject(i).getString("dcId");
					String vmId = arrayJson.getJSONObject(i).getString("vmId");
					String blockStatus = arrayJson.getJSONObject(i).getString("blockStatus");
					
					if(blockStatus.equals("true")){
						for(CloudVm vmOrg :needUnBlockVmList){
							if(vmOrg.getVmId().equals(vmId) && "false".equals(vmOrg.getVmBlockStatus())){
								blockStatus = "false";
								JSONObject newBlockResource = new JSONObject();
								newBlockResource.put("prjId", prjId);
								newBlockResource.put("dcId", dcId);
								newBlockResource.put("vmId", vmId);
								newBlockResource.put("blockStatus", blockStatus);
								arrayJson.set(i, newBlockResource);
								break;
							}
						}
					}
					
				}
				
			}
			blockResource.setUnblockCloudResource(resourceJson.toJSONString());
		}else{//表示第一次点击解冻，且有失败的情况，需要组织unblockcloudresurce，且保存
			for(CloudVm vmOrg :needUnBlockVmList){
				String prjId = vmOrg.getPrjId();
				String dcId = vmOrg.getDcId();
				String vmId = vmOrg.getVmId();
				String blockStatus = vmOrg.getVmBlockStatus();
				
				JSONObject json = new JSONObject();
				JSONObject unblockResource = new JSONObject();
				
				unblockResource.put("prjId", prjId);
				unblockResource.put("dcId", dcId);
				unblockResource.put("vmId", vmId);
				unblockResource.put("blockStatus", blockStatus);
				arrayJson.add(unblockResource);
				json.put("vm", arrayJson);
				returnResource.put("unblock", json);
			}
			blockResource.setUnblockCloudResource(returnResource.toJSONString());
		}
		//将blockVmList中提取冻结记录，重新保存数据库
		blockResource.setUpdateTime(unblockDate);
		//组织ecmc登陆账号信息
		String accountBlockJson = this.getNewAccountOperationJson(blockResource, unblockDate,"unblockAccount");
		blockResource.setUnblockAccount(accountBlockJson);
		
		cusBlockResDao.saveOrUpdate(blockResource);//更新blockCloudResource字段
		return blockResource;
		
	}
	/**
	 * 恢复云主机
	 * @Author: liyanchao
	 * @param cloudVm
	 *<li>Date: 2016年7月27日</li>
	 */
	private List<CloudVm> getNeedUnblockVmList(String cusId) throws AppException{
		//先查出需要恢复的云主机
		List<CloudProject> projectList = ecmcProjectService.getProjectByCustomer(cusId);
		List<CloudVm> orgVmList= new ArrayList<CloudVm>();
		for(CloudProject project : projectList){
			String prjId = project.getProjectId();
			List<CloudVm> vmList = new ArrayList<CloudVm>();
			vmList = ecmcCloudVmService.getNoActiveUnDeletedVmByPrjId(prjId, "ACTIVE");
			for(CloudVm vm : vmList){
				orgVmList.add(vm);
			}
		}
		//得到冻结记录信息
		CusBlockResource blockResource = cusBlockResDao.getBlockedResourceByCusId(cusId);
		//组织需要的vmlist
		JSONObject resourceJson = new JSONObject();
		JSONArray arrayJson = new JSONArray();
		List<CloudVm> needUnBlockVmList= new ArrayList<CloudVm>();
		if(null!=blockResource){
			resourceJson = JSONObject.parseObject(blockResource.getBlockCloudResource());
			JSONObject vmJson = resourceJson.getJSONObject("block");
			if(null !=vmJson){
				arrayJson = vmJson.getJSONArray("vm");
				if(null != arrayJson){
					for(int i=0; i<arrayJson.size(); i++){//得到要解冻的vmList
						String vmId = arrayJson.getJSONObject(i).getString("vmId");
//						String blockStatus = arrayJson.getJSONObject(i).getString("blockStatus");//由于下面的if 暂时注掉此行
						
						for(CloudVm vmOrg :orgVmList){
							if(vmOrg.getVmId().equals(vmId)){// && "true".equals(blockStatus) 此处将在冻结记录中匹配现有的非active状态的主机组织返回的vmlist
								needUnBlockVmList.add(vmOrg);
								break;
							}
						}
					}
				}
			}
			
		}
		return needUnBlockVmList;
	}
	/**
	 * 恢复云主机
	 * @Author: liyanchao
	 * @param cloudVm
	 *<li>Date: 2016年7月27日</li>
	 */
	private void resumeVm(CloudVm cloudVm) throws AppException {
		try{
			openstackVmService.resumeVm(cloudVm);
			
			BaseCloudVm vm = new BaseCloudVm();
			vm = ecmcCloudVmService.findBaseVmByVmId(cloudVm.getVmId());
			vm.setVmStatus("RESUMING");//恢复中
			
			ecmcCloudVmService.mergeBaseVm(vm);
			
			final JSONObject json =new JSONObject();
			json.put("vmId", vm.getVmId());
			json.put("dcId",vm.getDcId());
			json.put("prjId", vm.getPrjId());
			json.put("vmStatus", vm.getVmStatus());
			json.put("perStatus", "SUSPENDED");//挂起
			json.put("count", "0");
			json.put("isExsit", "0");
			
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.vmKey, json.toJSONString());
				}
			});
			
		}catch(AppException e){
			throw e;
		}catch(Exception e){
			throw new AppException ("error.openstack.message");
		}
	}
	/**
	 * 修改客户的冻结状态in Redis
	 * **/
	private void updateCusBlockStatusInRedis(String cusId,String flag) throws Exception{
		jedisUtil.set(RedisKey.CUS_BLOCK+cusId, flag);
	}
}
