package com.eayun.virtualization.apiservice.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.charge.model.ChargeRecord;
import com.eayun.common.ConstantClazz;
import com.eayun.common.annotation.ApiMethod;
import com.eayun.common.annotation.ApiService;
import com.eayun.common.api.ApiUtil;
import com.eayun.common.constant.ApiConstant;
import com.eayun.common.constant.ApiInstanceConstant;
import com.eayun.common.constant.BillingCycleType;
import com.eayun.common.constant.EayunQueueConstant;
import com.eayun.common.constant.OrderType;
import com.eayun.common.constant.PayType;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.exception.ApiException;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.tools.ErrorsUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.CloudResourceUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.InstanceApiUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.Hook;
import com.eayun.costcenter.model.MoneyAccount;
import com.eayun.costcenter.service.AccountOverviewService;
import com.eayun.customer.model.User;
import com.eayun.customer.serivce.UserService;
import com.eayun.eayunstack.model.InterfaceAttachment;
import com.eayun.eayunstack.model.Vm;
import com.eayun.eayunstack.service.OpenstackVmService;
import com.eayun.log.service.LogService;
import com.eayun.monitor.apiservice.AlarmApiService;
import com.eayun.monitor.apiservice.EcmcAlarmApiService;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.order.model.BaseOrder;
import com.eayun.order.model.BaseOrderResource;
import com.eayun.order.model.Order;
import com.eayun.order.service.OrderService;
import com.eayun.project.service.ProjectService;
import com.eayun.sys.model.SysDataTree;
import com.eayun.syssetup.service.SysDataTreeService;
import com.eayun.virtualization.apiservice.FloatIpApiService;
import com.eayun.virtualization.apiservice.MemberApiService;
import com.eayun.virtualization.apiservice.NetworkApiService;
import com.eayun.virtualization.apiservice.PortMappingApiService;
import com.eayun.virtualization.apiservice.SecurityGroupApiService;
import com.eayun.virtualization.apiservice.SubNetworkApiService;
import com.eayun.virtualization.apiservice.VmApiService;
import com.eayun.virtualization.apiservice.VolumeApiService;
import com.eayun.virtualization.baseservice.BaseVmService;
import com.eayun.virtualization.dao.CloudVmDao;
import com.eayun.virtualization.model.BaseCloudFlavor;
import com.eayun.virtualization.model.BaseCloudPortMapping;
import com.eayun.virtualization.model.BaseCloudSecurityGroup;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.BaseCloudVm;
import com.eayun.virtualization.model.BaseCloudVmSgroup;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.CloudBatchResource;
import com.eayun.virtualization.model.CloudFloatIp;
import com.eayun.virtualization.model.CloudImage;
import com.eayun.virtualization.model.CloudNetWork;
import com.eayun.virtualization.model.CloudOrderFloatIp;
import com.eayun.virtualization.model.CloudOrderVm;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.model.CloudSecurityGroup;
import com.eayun.virtualization.model.CloudSecurityGroupRule;
import com.eayun.virtualization.model.CloudVm;
import com.eayun.virtualization.service.CloudBatchResourceService;
import com.eayun.virtualization.service.CloudFlavorService;
import com.eayun.virtualization.service.CloudOrderVmService;
import com.eayun.virtualization.service.TagService;
import com.eayun.virtualization.service.VmSecurityGroupService;

/**
 * 
 * 云主机API业务<br>
 * -----------------
 * 
 * @author zhouhaitao
 * @date 2016-12-2
 *
 */
@ApiService
@Service
@Transactional
public class VmApiServiceImpl extends BaseVmService implements VmApiService {
	private static final Logger log = LoggerFactory.getLogger(VmApiServiceImpl.class);
	@Autowired
	private CloudVmDao cloudVmDao;
	@Autowired
	private SubNetworkApiService subnetworkService;
	@Autowired
	private OpenstackVmService openstackVmService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private VmSecurityGroupService vmSgService;
	@Autowired
	private SecurityGroupApiService securityGroupApiService;
	@Autowired
	private PortMappingApiService portMappingService;
	@Autowired
	private VolumeApiService volumeApiService;
	@Autowired
	private FloatIpApiService floatIpApiService;
	@Autowired
	private AlarmApiService alarmApiService;
	@Autowired
	private EcmcAlarmApiService ecmcAlarmApiService;
	@Autowired
	private MemberApiService memberApiService;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private EayunRabbitTemplate rabbitTemplate;
	@Autowired
	private TagService tagService;
	@Autowired
	private NetworkApiService networkApiService;
	@Autowired
	private AccountOverviewService accountOverviewSerivce;
	@Autowired
	private SysDataTreeService sysDataTreeService;
	@Autowired
	private UserService userService;
	@Autowired
	private OrderService orderService;
	@Autowired
	private CloudOrderVmService cloudOrderVmService;
	@Autowired
	private CloudFlavorService cloudFlavorService;
	@Autowired
	private MessageCenterService messageCenterService;
	@Autowired
	private CloudBatchResourceService cloudBatchResourceService;
	@Autowired
	private LogService logService;

	/**
	 * 创建云主机的API<br>
	 * -------------
	 * 
	 * @author zhouhaitao
	 * @param params
	 * @return
	 */
	@ApiMethod("V1/CreateInstance")
	public JSONObject createInstance(JSONObject params) throws ApiException {
		JSONObject json = null;
		try {
			CloudOrderVm cov = new CloudOrderVm();
			boolean checkResult = validateCreateInstanceData(params, cov);
			if (checkResult) {
				User user = userService.queryAdminByCusId(cov.getCusId());
				cov.setCreateUser(user.getUserAccount());
				Order order = createVmOrder(cov, user.getUserId());
				cov.setOrderNo(order.getOrderNo());

				if ("1".equals(cov.getBuyFloatIp())) {
					CloudOrderFloatIp cloudOrderFloatIp = new CloudOrderFloatIp();
					cloudOrderFloatIp.setBuyCycle(cov.getBuyCycle());
					cloudOrderFloatIp.setCreateTime(cov.getCreateOrderDate());
					cloudOrderFloatIp.setCreUser(cov.getCreateUser());
					cloudOrderFloatIp.setDcId(cov.getDcId());
					cloudOrderFloatIp.setPrjId(cov.getPrjId());
					cloudOrderFloatIp.setOrderType(cov.getOrderType());
					cloudOrderFloatIp.setPayType(cov.getPayType());
					cloudOrderFloatIp.setProductCount(cov.getCount());
					cloudOrderFloatIp.setOrderNo(cov.getOrderNo());

					floatIpApiService.createFloatIpOrder(cloudOrderFloatIp);
				}
				cloudOrderVmService.saveOrUpdate(cov);

				List<BaseCloudVm> bcvList = createVm(cov);
				if (null != bcvList && bcvList.size() > 0) {
					json = new JSONObject();
					json.put(ApiInstanceConstant.Instance.TOTALCOUNT, bcvList.size());
					JSONArray vmIds = new JSONArray();
					for (BaseCloudVm bcv : bcvList) {
						vmIds.add(bcv.getVmId());
					}
					json.put(ApiInstanceConstant.Instance.INSTANCEIDS, vmIds);
				}
			}
		} catch (AppException e) {
			e.printStackTrace();
			String errCode = ErrorsUtil.escapeApiErrCode(e.getOriginMessage()[0]);
			throw ApiException.createApiException(errCode);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		return json;
	}

	/**
	 * 调整云主机大小的API<br>
	 * -------------
	 * 
	 * @author zhouhaitao
	 * @param params
	 * @return
	 */
	@ApiMethod("V1/ResizeInstance")
	public JSONObject resizeInstance(JSONObject params) throws ApiException {
		try {
			CloudOrderVm cov = new CloudOrderVm();
			boolean checkResult = validateResizeInstanceData(params, cov);
			if (checkResult) {
				User user = userService.queryAdminByCusId(cov.getCusId());
				cov.setCreateUser(user.getUserAccount());

				Order order = createVmOrder(cov, user.getUserId());
				cov.setOrderNo(order.getOrderNo());

				cloudOrderVmService.saveOrUpdate(cov);

				CloudVm vm = new CloudVm();
				vm.setDcId(cov.getDcId());
				vm.setPrjId(cov.getPrjId());
				vm.setVmId(cov.getVmId());
				vm.setCpus(cov.getCpu());
				vm.setRams(cov.getRam());
				vm.setDisks(cov.getDisk());
				vm.setOrderNo(cov.getOrderNo());
				vm.setCusId(cov.getCusId());
				vm.setVmName(cov.getVmName());
				vm.setSysType(cov.getSysType());
				vm.setPayType(cov.getPayType());
				vm.setEndTime(cov.getEndTime());

				resizeVm(vm);
			}
		} catch (AppException e) {
			e.printStackTrace();
			String errCode = ErrorsUtil.escapeApiErrCode(e.getOriginMessage()[0]);
			throw ApiException.createApiException(errCode);
		} catch(Exception e){
			e.printStackTrace();
			throw e;
		}

		return null;
	}

	
	/**
	 * 关闭云主机的API
	 * 
	 * @author chengxiaodong
	 * @param params
	 * @return
	 * @throws ApiException
	 */
	@ApiMethod("V1/ShutdownInstance")
	public JSONObject shutDownInstance(JSONObject params) throws ApiException {
		
		String cusId = null;
		CloudVm cloudVm = null;
		CloudProject project = null;
		
		try {
		//获取参数
		String dcId = (null != params.get(ApiInstanceConstant.Instance.DCID)) ? params.getString(ApiInstanceConstant.Instance.DCID) : "";
	    cusId = (null != params.get(ApiInstanceConstant.Instance.CUSID)) ? params.getString(ApiInstanceConstant.Instance.CUSID) : "";
	    String instanceId = (null != params.get(ApiInstanceConstant.Instance.INSTANCEID)) ? params.getString(ApiInstanceConstant.Instance.INSTANCEID) : "";
		
	    
	    project=projectService.queryProjectByDcAndCus(dcId, cusId);
	  
	    //InstanceId未设置
	    if("".equals(instanceId)){
	    	throw ApiException.createApiException("110042");
	    }
	    
	    //InstanceId不符合规则
	    if(!InstanceApiUtil.uuidRegex(instanceId, true)){
	    	throw ApiException.createApiException("110044");
	    }
	    
	    
	    //客户不存在项目
	    if(null==project){
	    	throw ApiException.createApiException("100030");
	    }
	    
	    cloudVm=queryVmById(instanceId, project.getProjectId());
	    
	    //该云主机不存在
	    if(null==cloudVm||"1".equals(cloudVm.getIsDeleted())||"0".equals(cloudVm.getIsVisable())){
	    	throw ApiException.createApiException("110043");
	    }
		
		
		//判断该云主机的计费状态
		if("1".equals(cloudVm.getChargeState())){
			throw ApiException.createApiException("100017");
		} else if ("2".equals(cloudVm.getChargeState()) || "3".equals(cloudVm.getChargeState())) {
			throw ApiException.createApiException("100019");
		}

		// 判断云主机是否是可关闭的状态
		if (!"ACTIVE".equals(cloudVm.getVmStatus()) || "2".equals(cloudVm.getIsDeleted())) {
			throw ApiException.createApiException("110048");
		}

		// 判断该云主机是否正在创建自定义镜像
		if (checkSavingSnapshot(cloudVm)) {
			throw ApiException.createApiException("110049");
		}

			// 执行关闭云主机操作
			shutdownVm(cloudVm);
			
			logService.addLog("关闭云主机", "API", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), project.getProjectId(), cusId, ConstantClazz.LOG_STATU_SUCCESS, null);
		}catch(AppException e){
			e.printStackTrace();
			String errCode = ErrorsUtil.escapeApiErrCode(e.getOriginMessage()[0]);
			logService.addLog("关闭云主机", "API", ConstantClazz.LOG_TYPE_HOST, null==cloudVm?"":cloudVm.getVmName(), null==project?"":project.getProjectId(), cusId, ConstantClazz.LOG_STATU_ERROR, e);
			throw ApiException.createApiException(errCode);
		}catch(Exception e){
			logService.addLog("关闭云主机", "API", ConstantClazz.LOG_TYPE_HOST, null==cloudVm?"":cloudVm.getVmName(), null==project?"":project.getProjectId(), cusId, ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}

		return null;
	}

	/**
	 * 启动云主机的API
	 * 
	 * @author chengxiaodong
	 * @param params
	 * @return
	 * @throws ApiException
	 */
	@ApiMethod("V1/StartInstance")
	public JSONObject startInstance (JSONObject params)throws ApiException{
		//获取参数
		String cusId = null;
		CloudVm cloudVm = null;
		CloudProject project = null;
		try{
			String dcId = (null != params.get(ApiInstanceConstant.Instance.DCID)) ? params.getString(ApiInstanceConstant.Instance.DCID) : "";
		    cusId = (null != params.get(ApiInstanceConstant.Instance.CUSID)) ? params.getString(ApiInstanceConstant.Instance.CUSID) : "";
		    String instanceId = (null != params.get(ApiInstanceConstant.Instance.INSTANCEID)) ? params.getString(ApiInstanceConstant.Instance.INSTANCEID) : "";
		  
		    project=projectService.queryProjectByDcAndCus(dcId, cusId);
		    //InstanceId未设置
		    if("".equals(instanceId)){
		    	throw ApiException.createApiException("110042");
		    }
		    
		    //InstanceId不符合规则
		    if(!InstanceApiUtil.uuidRegex(instanceId, true)){
		    	throw ApiException.createApiException("110044");
		    }
		    
		    
		    //客户不存在项目
		    if(null==project){
		    	throw ApiException.createApiException("100030");
		    }
		    
		    cloudVm=queryVmById(instanceId, project.getProjectId());
		    
		    //该云主机不存在
		    if(null==cloudVm||"1".equals(cloudVm.getIsDeleted())||"0".equals(cloudVm.getIsVisable())){
		    	throw ApiException.createApiException("110043");
		    }
			
			
			//判断该云主机的计费状态
			if("1".equals(cloudVm.getChargeState())){
				throw ApiException.createApiException("100017");
			} else if ("2".equals(cloudVm.getChargeState()) || "3".equals(cloudVm.getChargeState())) {
				throw ApiException.createApiException("100019");
			}
			
			//判断云主机是否是可启动的状态
			if(!"SHUTOFF".equals(cloudVm.getVmStatus())||"2".equals(cloudVm.getIsDeleted())){
				throw ApiException.createApiException("110056");
			}
			
			//判断该云主机是否正在创建自定义镜像
			if(checkSavingSnapshot(cloudVm)){
				throw ApiException.createApiException("110013");
			}
			
			//执行开启云主机操作
			restartVm(cloudVm);
			
			logService.addLog("启动云主机", "API", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), project.getProjectId(), cusId, ConstantClazz.LOG_STATU_SUCCESS, null);
		}catch(AppException e){
			e.printStackTrace();
			String errCode = ErrorsUtil.escapeApiErrCode(e.getOriginMessage()[0]);
			logService.addLog("启动云主机", "API", ConstantClazz.LOG_TYPE_HOST, null==cloudVm?"":cloudVm.getVmName(), null==project?"":project.getProjectId(), cusId, ConstantClazz.LOG_STATU_ERROR, e);
			throw ApiException.createApiException(errCode);
		}catch(Exception e){
			logService.addLog("启动云主机", "API", ConstantClazz.LOG_TYPE_HOST, null==cloudVm?"":cloudVm.getVmName(), null==project?"":project.getProjectId(), cusId, ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}

		return null;
	}

	/**
	 * 重启云主机的API
	 * 
	 * @author chengxiaodong
	 * @param params
	 * @return
	 * @throws ApiException
	 */
	@ApiMethod("V1/RebootInstance")
	public JSONObject rebootInstance(JSONObject params) throws ApiException {
		
		String cusId = null;
		CloudVm cloudVm = null;
		CloudProject project = null;
		
		try {
			
		String dcId = (null != params.get(ApiInstanceConstant.Instance.DCID)) ? params.getString(ApiInstanceConstant.Instance.DCID) : "";
	    cusId = (null != params.get(ApiInstanceConstant.Instance.CUSID)) ? params.getString(ApiInstanceConstant.Instance.CUSID) : "";
	    String instanceId = (null != params.get(ApiInstanceConstant.Instance.INSTANCEID)) ? params.getString(ApiInstanceConstant.Instance.INSTANCEID) : "";

	    project=projectService.queryProjectByDcAndCus(dcId, cusId);
	    //InstanceId未设置
	    if("".equals(instanceId)){
	    	throw ApiException.createApiException("110042");
	    }
	    
	    //InstanceId不符合规则
	    if(!InstanceApiUtil.uuidRegex(instanceId, true)){
	    	throw ApiException.createApiException("110044");
	    }
	    
	    
	    //客户不存在项目
	    if(null==project){
	    	throw ApiException.createApiException("100030");
	    }
	    
	    cloudVm=queryVmById(instanceId, project.getProjectId());
	    
	    //该云主机不存在
	    if(null==cloudVm||"1".equals(cloudVm.getIsDeleted())||"0".equals(cloudVm.getIsVisable())){
	    	throw ApiException.createApiException("110043");
	    }
		
		//判断该云主机的计费状态
		if("1".equals(cloudVm.getChargeState())){
			throw ApiException.createApiException("100017");
		}else if("2".equals(cloudVm.getChargeState())||"3".equals(cloudVm.getChargeState())){
			throw ApiException.createApiException("100019");
		}
		
		//判断云主机是否是可关闭的状态
		if(!"ACTIVE".equals(cloudVm.getVmStatus())||"2".equals(cloudVm.getIsDeleted())){
			throw ApiException.createApiException("110048");
		}
		
		//判断该云主机是否正在创建自定义镜像
		if(checkSavingSnapshot(cloudVm)){
			throw ApiException.createApiException("110053");
		}

		    // 执行软重启云主机操作
			softRestartVm(cloudVm);
			
			logService.addLog("重启云主机", "API", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), project.getProjectId(), cusId, ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (AppException e) {
			e.printStackTrace();
			String errCode = ErrorsUtil.escapeApiErrCode(e.getOriginMessage()[0]);
			logService.addLog("重启云主机", "API", ConstantClazz.LOG_TYPE_HOST, null==cloudVm?"":cloudVm.getVmName(), null==project?"":project.getProjectId(), cusId, ConstantClazz.LOG_STATU_ERROR, e);
			throw ApiException.createApiException(errCode);
		}catch(Exception e){
			logService.addLog("重启云主机", "API", ConstantClazz.LOG_TYPE_HOST, null==cloudVm?"":cloudVm.getVmName(), null==project?"":project.getProjectId(), cusId, ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
		
		return null;
	}

	/**
	 * 删除云主机的API
	 * 
	 * @author chengxiaodong
	 * @param params
	 * @return
	 * @throws ApiException
	 */
	@ApiMethod("V1/DeleteInstance")
	public JSONObject deleteInstance(JSONObject params) throws ApiException {
		
		String cusId = null;
		CloudVm cloudVm = null;
		CloudProject project = null;
		String IsRecycle=null;
		
		try{
			
		String dcId = (null != params.get(ApiInstanceConstant.Instance.DCID)) ? params.getString(ApiInstanceConstant.Instance.DCID) : "";
	    cusId = (null != params.get(ApiInstanceConstant.Instance.CUSID)) ? params.getString(ApiInstanceConstant.Instance.CUSID) : "";
	    String instanceId = (null != params.get(ApiInstanceConstant.Instance.INSTANCEID)) ? params.getString(ApiInstanceConstant.Instance.INSTANCEID) : "";
	    IsRecycle = (null != params.get(ApiInstanceConstant.Instance.ISRECYCLE)
	    		&& !StringUtil.isEmpty(params.getString(ApiInstanceConstant.Instance.ISRECYCLE))) ? params.getString(ApiInstanceConstant.Instance.ISRECYCLE) : "0";
	    
	    project=projectService.queryProjectByDcAndCus(dcId, cusId);
	    //InstanceId未设置
	    if("".equals(instanceId)){
	    	throw ApiException.createApiException("110042");
	    }
	    
	    //InstanceId不符合规则
	    if(!InstanceApiUtil.uuidRegex(instanceId, true)){
	    	throw ApiException.createApiException("110044");
	    }
	    
	    //IsRecycle参数不符合规则
	    if(!"0".equals(IsRecycle)&&!"1".equals(IsRecycle)){
	    	throw ApiException.createApiException("110051");
	    }
	    
	    
	    
	    
	    //客户不存在项目
	    if(null==project){
	    	throw ApiException.createApiException("100030");
	    }
	    
	    //根据cusId查询admin用户
	    User user = userService.queryAdminByCusId(cusId);
	    
	    cloudVm=queryVmById(instanceId, project.getProjectId());
	    
	    //该云主机不存在
	    if(null==cloudVm||"1".equals(cloudVm.getIsDeleted())||"0".equals(cloudVm.getIsVisable())){
	    	throw ApiException.createApiException("110043");
	    }
		 
			
	    //判断该云主机的计费状态
		if("1".equals(cloudVm.getPayType())&&"0".equals(cloudVm.getChargeState())){
			throw ApiException.createApiException("110052");
		}
		
		//云主机已到期将云主机放入回收站
		if("0".equals(IsRecycle)&&("2".equals(cloudVm.getChargeState())||"3".equals(cloudVm.getChargeState()))){
			throw ApiException.createApiException("100019");
		}
		
		//云主机余额不足将云主机放入回收站
		if("0".equals(IsRecycle)&&"1".equals(cloudVm.getChargeState())){
			throw ApiException.createApiException("100017");
		}
		
		//如果此云主机已在回收站中
        if("2".equals(cloudVm.getIsDeleted())&&"0".equals(IsRecycle)){
        	throw ApiException.createApiException("110080");
		}
		
		
		//判断云主机是否是可删除的状态
		if(!("ACTIVE".equals(cloudVm.getVmStatus())||"ERROR".equals(cloudVm.getVmStatus())||"SHUTOFF".equals(cloudVm.getVmStatus()))&&"0".equals(cloudVm.getIsDeleted())){
			throw ApiException.createApiException("110080");
		}
		
		
		
		
		//判断该云主机是否正在创建自定义镜像
		if(checkSavingSnapshot(cloudVm)){
			throw ApiException.createApiException("110050");
		}
		
		    //执行关闭云主机操作
			if("0".equals(IsRecycle)&&"0".equals(cloudVm.getIsDeleted())){
				//未在回收站的云主机放入回收站
				softDeleteVm(cloudVm,user);
				
				logService.addLog("删除云主机", "API", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), project.getProjectId(), cusId, ConstantClazz.LOG_STATU_SUCCESS, null);
			}else if("1".equals(IsRecycle)&&"0".equals(cloudVm.getIsDeleted())){
				//未在回收站的云主机彻底删除
				forceDeleteVm(cloudVm,user);
				
				logService.addLog("销毁云主机", "API", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), project.getProjectId(), cusId, ConstantClazz.LOG_STATU_SUCCESS, null);
			}else if("1".equals(IsRecycle)&&"2".equals(cloudVm.getIsDeleted())){
				//回收站的云主机彻底删除
				deleteSoftVm(cloudVm,user);
				
				logService.addLog("销毁云主机", "API", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), project.getProjectId(), cusId, ConstantClazz.LOG_STATU_SUCCESS, null);
			}

		} catch (AppException e) {
			
			e.printStackTrace();
			String errCode = ErrorsUtil.escapeApiErrCode(e.getOriginMessage()[0]);
			if("1".equals(IsRecycle)){
				logService.addLog("销毁云主机", "API", ConstantClazz.LOG_TYPE_HOST, null==cloudVm?"":cloudVm.getVmName(), null==project?"":project.getProjectId(), cusId, ConstantClazz.LOG_STATU_ERROR, e);
			}else if("0".equals(IsRecycle)){
				logService.addLog("删除云主机", "API", ConstantClazz.LOG_TYPE_HOST, null==cloudVm?"":cloudVm.getVmName(), null==project?"":project.getProjectId(), cusId, ConstantClazz.LOG_STATU_ERROR, e);
			}
			
			throw ApiException.createApiException(errCode);
			
		}catch(Exception e){
			
			if("1".equals(IsRecycle)){
				logService.addLog("销毁云主机", "API", ConstantClazz.LOG_TYPE_HOST, null==cloudVm?"":cloudVm.getVmName(), null==project?"":project.getProjectId(), cusId, ConstantClazz.LOG_STATU_ERROR, e);
			}else if("0".equals(IsRecycle)){
				logService.addLog("删除云主机", "API", ConstantClazz.LOG_TYPE_HOST, null==cloudVm?"":cloudVm.getVmName(), null==project?"":project.getProjectId(), cusId, ConstantClazz.LOG_STATU_ERROR, e);
			}
			
			throw e;
		}
		
		return null;
	}
	
	/**
	 * 查询云主机的API
	 * 
	 * @author gaoxiang
	 * @param params
	 * @return
	 * @throws ApiException
	 */
	@ApiMethod("V1/DescribeInstances")
	public JSONObject describeInstances (JSONObject params) throws ApiException {
	    /*获取参数*/
	    String dcId = (null != params.get(ApiInstanceConstant.Instance.DCID)) ? params.getString(ApiInstanceConstant.Instance.DCID) : "";
        String cusId = (null != params.get(ApiInstanceConstant.Instance.CUSID)) ? params.getString(ApiInstanceConstant.Instance.CUSID) : "";
        Object instanceObj = ((null != params.get(ApiInstanceConstant.Instance.INSTANCEIDS)) ? params.get(ApiInstanceConstant.Instance.INSTANCEIDS) : new JSONArray());
        Object imageObj = (null != params.get(ApiInstanceConstant.Instance.IMAGEIDS)) ? params.get(ApiInstanceConstant.Instance.IMAGEIDS) : new JSONArray();
        String searchWord = (null != params.get(ApiInstanceConstant.Instance.SEARCHWORD)) ? params.getString(ApiInstanceConstant.Instance.SEARCHWORD) : "";
//        JSONArray status = (null != params.get(ApiInstanceConstant.Instance.STATUS)) ? params.getJSONArray(ApiInstanceConstant.Instance.STATUS) : new JSONArray();
        String status = (null != params.get(ApiInstanceConstant.Instance.STATUS)) ? params.getString(ApiInstanceConstant.Instance.STATUS) : "";
        String ip = (null != params.get(ApiInstanceConstant.Instance.IP)) ? params.getString(ApiInstanceConstant.Instance.IP) : "";
        String offset = (null != params.get(ApiInstanceConstant.Instance.OFFSET) && !ApiInstanceConstant.SET_NULL.equals(params.getString(ApiInstanceConstant.Instance.OFFSET))) ? params.getString(ApiInstanceConstant.Instance.OFFSET) : "0";
        String limit = (null != params.get(ApiInstanceConstant.Instance.LIMIT) && !ApiInstanceConstant.SET_NULL.equals(params.getString(ApiInstanceConstant.Instance.LIMIT))) ? params.getString(ApiInstanceConstant.Instance.LIMIT) : "20";
        /*获取项目id*/
        CloudProject project = projectService.queryProjectByDcAndCus(dcId, cusId);
        if (null == project) {
            throw ApiException.createApiException("100030");
        }
        String prjId = project.getProjectId();
        try {
            /*校验instanceIds*/
            JSONArray instanceIds = null;
            if (!JSONArray.class.equals(instanceObj.getClass())) {
                throw ApiException.createApiException("110044");
            } else {
                instanceIds = (JSONArray) instanceObj;
                if (null != instanceIds && !instanceIds.isEmpty()) {
                    if (20 < instanceIds.size()) {
                        throw ApiException.createApiException("110044");
                    }
                    for (Object instanceId : instanceIds) {
                        switch (checkInstanceId(instanceId.toString(), prjId, false)) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            throw ApiException.createApiException("110044");
                        case 3:
                            throw ApiException.createApiException("110043");
                        case 4:
                            throw ApiException.createApiException("110043");
                        default:
                            return null;
                        }
                    }
                }
            }
            /*校验imageIds*/
            JSONArray imageIds = null;
            if (!JSONArray.class.equals(imageObj.getClass())) {
                throw ApiException.createApiException("110017");
            } else {
                imageIds = (JSONArray) imageObj;
                if (null != imageIds && !imageIds.isEmpty()) {
                    if (20 < imageIds.size()) {
                        throw ApiException.createApiException("110017");
                    }
                    for (Object imageId : imageIds) {
                        if (!InstanceApiUtil.uuidRegex(imageId.toString(), true)) {
                            throw ApiException.createApiException("110017");
                        }
                        List<Object> checkImageList = queryVmListByImageIdAndIp(prjId, imageId.toString(), null);
                        if (null == checkImageList || checkImageList.isEmpty()) {
                            throw ApiException.createApiException("110016");
                        }
                    }
                }
            }
            /*校验searchWord*/
            if (!StringUtil.isEmpty(searchWord)) {
                if (!searchWord.matches(ApiInstanceConstant.CLOUD_RESOURCE_NAME_REGEX)) {
                    throw ApiException.createApiException("100023");
                }
            }
            /*校验status*/
            if (null != status && !status.isEmpty()) {
                if (!ApiInstanceConstant.SearchMap.containsKey(status)) {
                    throw ApiException.createApiException("110045");
                } else {
                    status = ApiInstanceConstant.SearchMap.get(status).toString();
                }
            }
            /*校验IP*/
            if (!StringUtil.isEmpty(ip)) {
                if (!/*ip.matches(ApiInstanceConstant.IP_REGEX)*/checkIpFormat(ip)) {
                    throw ApiException.createApiException("110046");
                }
                /*List<Object> checkIpList = queryVmListByImageIdAndIp(prjId, null, ip);
                if (null == checkIpList || checkIpList.isEmpty()) {
                    throw ApiException.createApiException("110047");
                }*/
            }
            /*校验offset*/
            if (!offset.matches("^\\d+$") || 0 > Integer.valueOf(offset)) {
                throw ApiException.createApiException("100026");
            }
            /*校验limit*/
            if (!limit.matches("^\\d+$") || 0 > Integer.valueOf(limit) || 100 < Integer.valueOf(limit)) {
                throw ApiException.createApiException("100027");
            }
            /*调用接口*/
            JSONObject respJson = listVm(prjId, dcId, instanceIds, imageIds, searchWord, status, ip, offset, limit);
            logService.addLog("查询云主机", "API", ConstantClazz.LOG_TYPE_HOST, null, prjId, cusId, ConstantClazz.LOG_STATU_SUCCESS, null);
            return respJson;
        } catch (ApiException e) {
            e.printStackTrace();
            logService.addLog("查询云主机", "API", ConstantClazz.LOG_TYPE_HOST, null, prjId, cusId, ConstantClazz.LOG_STATU_ERROR, e);
            throw e;
        }
	}
	/**
	 * 校验ip格式：允许0-255的数字，允许小数点，不允许小数点相连，允许三个或三个以下的小数点
	 * @param ip
	 * @return
	 */
	private boolean checkIpFormat (String ip) {
	    if (StringUtil.isEmpty(ip)) {
	        return false;
	    }
	    String [] numbers = ip.split("\\.");
	    if (numbers.length > 4) {
	        return false;
	    }
	    for (int i = 0; i < numbers.length; i++) {
	        if (!numbers[i].matches("^(25[0-5])|(2[0-4][0-9])|(1[0-9][0-9])|([1-9]?[0-9])$") && !("".equals(numbers[i]) && (i == 0 || i == numbers.length - 1))) {
	            return false;
	        }
	    }
	    return true;
	}

	/**
	 * 用于校验镜像id和ip是否存在
	 * @param prjId
	 * @param imageId
	 * @param ip
	 * @return
	 */
    @SuppressWarnings("unchecked")
    private List<Object> queryVmListByImageIdAndIp(String prjId, String imageId, String ip) {
        List<Object> values = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT vm.vm_id as vmId ");
        sql.append(" FROM cloud_vm vm ");
        sql.append(" LEFT JOIN cloud_floatip flo ");
        sql.append("    ON vm.vm_id = flo.resource_id ");
        sql.append("    AND flo.resource_type = 'vm' ");
        sql.append("    AND flo.is_deleted = '0' ");
        sql.append(" WHERE ");
        sql.append("    vm.prj_id = ? ");
        sql.append("    AND vm.is_deleted = '0' ");
        sql.append("    AND vm.is_visable = '1' ");
        values.add(prjId);
        if (!StringUtil.isEmpty(imageId)) {
            sql.append(" AND vm.from_imageid = ? ");
            values.add(imageId);
        }
        if (!StringUtil.isEmpty(ip)) {
            sql.append(" AND (vm.vm_ip = ? or vm.self_ip = ? or flo.flo_ip = ?) ");
            values.add(ip);
            values.add(ip);
            values.add(ip);
        }
        javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), values.toArray());
        return query.getResultList();
    }
	
	/**
	 * 获取云主机列表信息
	 * 
	 * @author gaoxiang
	 * @param prjId
	 * @param dcId
	 * @param instanceIds
	 * @param imageIds
	 * @param searchWord
	 * @param status
	 * @param ip
	 * @param offset
	 * @param limit
	 * @return
	 * @throws ApiException
	 */
	private JSONObject listVm (
	        String prjId, 
	        String dcId, 
	        JSONArray instanceIds, 
	        JSONArray imageIds, 
	        String searchWord, 
	        String status, 
	        String ip, 
	        String offset, 
	        String limit) throws ApiException {
	    List<Object> values = new ArrayList<Object>();
	    StringBuffer sql = new StringBuffer();
	    sql.append(" SELECT                                                        ");
	    sql.append("   vm.vm_id as vmId                                            ");//0
	    sql.append("   ,vm.vm_name as vmName                                       ");//1
	    sql.append("   ,vm.prj_id as prjId                                         ");//2
	    sql.append("   ,vm.dc_id as dcId                                           ");//3
	    sql.append("   ,vm.vm_status as vmStatus                                   ");//4
	    sql.append("   ,vm.create_time as createTime                               ");//5
	    sql.append("   ,vm.vm_ip as vmIp                                           ");//6
	    sql.append("   ,flv.flavor_vcpus as cpus                                   ");//7
        sql.append("   ,flv.flavor_ram as rams                                      ");//8
        sql.append("   ,flv.flavor_disk as disks                                    ");//9
        sql.append("   ,prj.prj_name as prjName                                     ");//10
        sql.append("   ,flo.flo_ip as floatIp                                       ");//11
        sql.append("   ,vm.sys_type as sysType                                      ");//12
        sql.append("   ,vol.vol_size as capacityDisk                                ");//13
        sql.append("   ,vm.self_ip as selfIp                                        ");//14
        sql.append("   ,vm.pay_type as payType                                      ");//15
        sql.append("   ,vm.charge_state as chargeState                              ");//16
        sql.append("   ,vm.end_time as endTime                                      ");//17
        sql.append("   ,dc.dc_name as dcName                                        ");//18
        sql.append("   ,vm.net_id as netId                                          ");//19
        sql.append("   ,vm.subnet_id as subnetId                                    ");//20
        sql.append("   ,vm.self_subnetid as selfsubnetId                            ");//21
        sql.append("   ,vol.vol_count as volCount                                   ");//22
        sql.append("   ,vm.vm_description as vmDescription                           ");//23
	    sql.append(" FROM cloud_vm vm                                              ");
	    sql.append(" LEFT JOIN dc_datacenter dc ON vm.dc_id = dc.id                ");
        sql.append(" LEFT JOIN cloud_flavor flv ON vm.flavor_id = flv.flavor_id     ");
        sql.append(" LEFT JOIN cloud_project prj ON vm.prj_id = prj.prj_id          ");
        sql.append(" LEFT JOIN                                                      ");
        sql.append(" (                                                              ");
        sql.append("    SELECT                                                      ");
        sql.append("        sum(vol_size) as vol_size,                              ");
        sql.append("        count(vol_size) as vol_count,                           ");
        sql.append("        vm_id                                                   ");
        sql.append("    FROM cloud_volume                                           ");
        sql.append("    WHERE vol_bootable = '0'                                    ");
        sql.append("    GROUP BY vm_id                                              ");
        sql.append(" ) as vol ON vm.vm_id = vol.vm_id                               ");
        sql.append(" LEFT JOIN cloud_floatip AS flo ON vm.vm_id = flo.resource_id   ");
        sql.append("    AND flo.resource_type ='vm' AND flo.is_deleted = '0'        ");
        sql.append(" WHERE vm.is_deleted <> '1'                                     ");
        sql.append("    AND vm.is_visable = '1'                                     ");
        sql.append("    AND vm.prj_id = ?                                           ");
        sql.append("    AND vm.dc_id = ?                                            ");
        values.add(prjId);
        values.add(dcId);
        
        if (null != instanceIds && !instanceIds.isEmpty()) {
            sql.append(" AND vm.vm_id in ( ");
            for (int i = 0; i < instanceIds.size(); i++) {
                sql.append("?");
                sql.append((i != instanceIds.size() - 1) ? "," : ")");
                values.add(instanceIds.get(i));
            }
        }
        
        if (null != imageIds && !imageIds.isEmpty()) {
            sql.append(" AND vm.from_imageid in ( ");
            for (int i = 0; i < imageIds.size(); i++) {
                sql.append("?");
                sql.append((i != imageIds.size() - 1) ? "," : ")");
                values.add(imageIds.get(i));
            }
        }
        
        if (!StringUtil.isEmpty(searchWord)) {
            sql.append(" AND binary vm.vm_name like ? ");
            searchWord = searchWord.replaceAll("\\_", "\\\\_").replaceAll("\\%", "\\\\%");
            values.add("%" + searchWord + "%");
        }
        
        if (null != status && !status.isEmpty()) {
            if (CloudResourceUtil.CLOUD_CHARGESTATE_NSF_CODE.equals(status) || CloudResourceUtil.CLOUD_CHARGESTATE_EXPIRED_CODE.equals(status)) {
                sql.append(" AND vm.charge_state = ? ");
                values.add(status);
                if (CloudResourceUtil.CLOUD_CHARGESTATE_EXPIRED_CODE.equals(status)) {
                    sql.append("OR vm.charge_state = '3' ");
                }
            } else {
                sql.append(" AND vm.vm_status = ? AND vm.charge_state = '0' ");
                values.add(status);
            }
        }
        
        if (!StringUtil.isEmpty(ip)) {
            sql.append(" AND (vm.vm_Ip like ? or vm.self_ip like ? or flo.flo_ip like ?) ");
            values.add("%" + ip + "%");
            values.add("%" + ip + "%");
            values.add("%" + ip + "%");
        }
        
        sql.append(" group by vm.vm_id order by vm.create_time desc ");
        
        sql.append(" LIMIT ?,? ");
        values.add(Integer.parseInt(offset) * Integer.parseInt(limit));
        values.add(Integer.parseInt(limit));
        
        javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), values.toArray());
        @SuppressWarnings("rawtypes")
		List dataList = (List) query.getResultList();
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < dataList.size(); i++) {
            Object[] objs = (Object[]) dataList.get(i);
            CloudVm vm = new CloudVm();
            vm.setVmId(String.valueOf(objs[0]));
            vm.setVmName(String.valueOf(objs[1]));
            vm.setVmDescripstion(String.valueOf(objs[23]));
            vm.setDcId(String.valueOf(objs[3]));
            vm.setVmStatus(String.valueOf(objs[4]));
            vm.setCreateTime((Date)objs[5]);
            vm.setVmIp(String.valueOf(objs[6]));
            vm.setCpus(Integer.parseInt(objs[7] != null ? String.valueOf(objs[7]) : "0"));
            vm.setRams(Integer.parseInt(objs[8] != null ? String.valueOf(objs[8]) : "0"));
            vm.setDisks(Integer.parseInt(objs[9] != null ? String.valueOf(objs[9]) : "0"));
            vm.setFloatIp(String.valueOf(objs[11]));
            String sysType = String.valueOf(objs[12]);
            if (!StringUtils.isEmpty(sysType) && !"null".equals(sysType)) {
                SysDataTree sdt = DictUtil.getDataTreeByNodeId(sysType);
                if(null != sdt){
                    vm.setSysTypeEn(sdt.getNodeNameEn());
                    vm.setSysType(sdt.getNodeName());
                }
            }
            vm.setDataCapacity(Integer.parseInt(objs[13] != null ? String.valueOf(objs[13]) : "0"));
            vm.setSelfIp(String.valueOf(objs[14]));
            vm.setPayType(String.valueOf(objs[15]));
            vm.setChargeState(String.valueOf(objs[16]));
            vm.setEndTime((Date)objs[17]);
            vm.setNetId(String.valueOf(objs[19]));
            vm.setSubnetId(String.valueOf(objs[20]));
            vm.setSelfSubnetId(String.valueOf(objs[21]));
            vm.setVolCount(Integer.parseInt(objs[22] != null ? String.valueOf(objs[22]) : "0"));
            List<BaseCloudSecurityGroup> sgList = getSecurityGroupByVm(vm.getVmId());
            String str = "";
            for (BaseCloudSecurityGroup sg : sgList) {
                if("default".equals(sg.getSgName())){
                    sg.setSgName("默认安全组");
                }
                str = str + sg.getSgName() + "、";
            }
            if (!StringUtils.isEmpty(str)) {
                vm.setSecurityGroups(str.substring(0, str.length() - 1));
            }
            JSONObject json = escapeInstance(vm);
            jsonArray.add(json);
        }
        if (jsonArray.size() == 0) {
            throw ApiException.createApiException("100025");
        }
        JSONObject respJson = new JSONObject();
        respJson.put(ApiInstanceConstant.Instance.TOTALCOUNT, jsonArray.size());
        respJson.put(ApiInstanceConstant.Instance.INSTANCESET, jsonArray);
        return respJson;
	}

	/**
	 * 修改云主机的API
	 * 
	 * @author gaoxiang
	 * @param params
	 * @return
	 * @throws ApiException
	 */
	@ApiMethod("V1/ModifyInstance")
	public JSONObject modifyInstance (JSONObject params) throws ApiException {
	    String cusId = null;
	    String instanceName = null;
	    CloudVm cloudVm = null;
	    CloudProject project = null;
	    try {
    	    /*获取参数*/
    	    String dcId = (null != params.get(ApiInstanceConstant.Instance.DCID)) ? params.getString(ApiInstanceConstant.Instance.DCID) : "";
    	    cusId = (null != params.get(ApiInstanceConstant.Instance.CUSID)) ? params.getString(ApiInstanceConstant.Instance.CUSID) : "";
    	    String instanceId = (null != params.get(ApiInstanceConstant.Instance.INSTANCEID)) ? params.getString(ApiInstanceConstant.Instance.INSTANCEID) : "";
    	    instanceName = (null != params.get(ApiInstanceConstant.Instance.INSTANCENAME)) ? params.getString(ApiInstanceConstant.Instance.INSTANCENAME) : "";
    	    String instanceRemark = (null != params.get(ApiInstanceConstant.Instance.INSTANCEREMARK)) ? params.getString(ApiInstanceConstant.Instance.INSTANCEREMARK) : null;
    	    project = projectService.queryProjectByDcAndCus(dcId, cusId);
    	    if (null == project) {
    	        throw ApiException.createApiException("100030");
    	    }
    	    String prjId = project.getProjectId();
    	    /*格式校验*/
    	    switch (checkInstanceId(instanceId, prjId, true)) {
    	    case 0:
    	        
    	        break;
    	    case 1:
    	        throw ApiException.createApiException("110042");
    	    case 2:
    	        throw ApiException.createApiException("110044");
    	    case 3:
    	        throw ApiException.createApiException("110043");
    	    case 4:
    	        throw ApiException.createApiException("110069");
    	    case 5:
    	        throw ApiException.createApiException("100019");
    	    case 6:
    	        throw ApiException.createApiException("100017");
            default:
                return null;
    	    }
            cloudVm = queryVmById(instanceId, prjId);
            /*校验云主机业务状态是否可以修改信息*/
            if (!"ACTIVE".equals(cloudVm.getVmStatus()) && !"SHUTOFF".equals(cloudVm.getVmStatus())) {
                throw ApiException.createApiException("110081");
            }
            /*判断是否云主机名称和描述信息同时没有设置*/
            if (null == params.get(ApiInstanceConstant.Instance.INSTANCENAME) && null == params.get(ApiInstanceConstant.Instance.INSTANCEREMARK)) {
                throw ApiException.createApiException("110087");
            }
            /*如果设置了云主机名称*/
            if (null != params.get(ApiInstanceConstant.Instance.INSTANCENAME)) {
                //名称格式校验
                if (!instanceName.matches(ApiInstanceConstant.CLOUD_RESOURCE_NAME_REGEX)) {
                    throw ApiException.createApiException("110028");
                }
                /*重名校验*/
                cloudVm.setVmName(instanceName);
                cloudVm.setNumber(1);
                if (!checkVmExistByName(cloudVm)) {
                    throw ApiException.createApiException("110029");
                }
            }
            /*如果设置了云主机描述信息*/
            if (null != params.get(ApiInstanceConstant.Instance.INSTANCEREMARK)) {
                if (200 < instanceRemark.length()) {
                    throw ApiException.createApiException("110030");
                }
                cloudVm.setVmDescripstion(instanceRemark);
            }
            /*调用接口*/
            modifyVm(cloudVm);
            logService.addLog("编辑云主机", "API", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), project.getProjectId(), cusId, ConstantClazz.LOG_STATU_SUCCESS, null);
        } catch (AppException e) {
            logService.addLog("编辑云主机", "API", ConstantClazz.LOG_TYPE_HOST, StringUtil.isEmpty(instanceName) ? (null == cloudVm ? "" : cloudVm.getVmName()) : instanceName, null == project ? "" : project.getProjectId(), cusId, ConstantClazz.LOG_STATU_ERROR, e);
            e.printStackTrace();
            String errCode = ErrorsUtil.escapeApiErrCode(e.getOriginMessage()[0]);
            throw ApiException.createApiException(errCode);
        } catch (Exception e) {
            logService.addLog("编辑云主机", "API", ConstantClazz.LOG_TYPE_HOST, StringUtil.isEmpty(instanceName) ? (null == cloudVm ? "" : cloudVm.getVmName()) : instanceName, null == project ? "" : project.getProjectId(), cusId, ConstantClazz.LOG_STATU_ERROR, e);
            throw e;
        }
	    return null;
	}

	/**
	 * 修改云主机子网的API
	 * 
	 * @author gaoxiang
	 * @param params
	 * @return
	 * @throws ApiException
	 */
	@ApiMethod("V1/ModifyInstanceSubnet")
	public JSONObject modifyInstanceSubnet (JSONObject params) throws ApiException {
	    String cusId = null;
	    CloudProject project = null;
	    CloudVm cloudVm = null;
	    try {
    	    /*获取参数*/
    	    String dcId = (null != params.get(ApiInstanceConstant.Instance.DCID)) ? params.getString(ApiInstanceConstant.Instance.DCID) : "";
            cusId = (null != params.get(ApiInstanceConstant.Instance.CUSID)) ? params.getString(ApiInstanceConstant.Instance.CUSID) : "";
            String instanceId = (null != params.get(ApiInstanceConstant.Instance.INSTANCEID)) ? params.getString(ApiInstanceConstant.Instance.INSTANCEID) : "";
            String msubnetId = (null != params.get(ApiInstanceConstant.Instance.MSUBNETID)) ? params.getString(ApiInstanceConstant.Instance.MSUBNETID) : null;
            String umsubnetId = (null != params.get(ApiInstanceConstant.Instance.UMSUBNETID)) ? params.getString(ApiInstanceConstant.Instance.UMSUBNETID) : null;
            String resultMsubnetId = new String();
            String resultUmsubnetId = new String();
            /*格式校验*/
            project = projectService.queryProjectByDcAndCus(dcId, cusId);
            if (null == project) {
                throw ApiException.createApiException("100030");
            }
            String prjId = project.getProjectId();
            switch (checkInstanceId(instanceId, prjId, true)) {
            case 0:
                
                break;
            case 1:
                throw ApiException.createApiException("110042");
            case 2:
                throw ApiException.createApiException("110044");
            case 3:
                throw ApiException.createApiException("110043");
            case 4:
                throw ApiException.createApiException("110069");
            case 5:
                throw ApiException.createApiException("100019");
            case 6:
                throw ApiException.createApiException("100017");
            default:
                return null;
            }
            cloudVm = queryVmById(instanceId, prjId);
            if (!"ACTIVE".equals(cloudVm.getVmStatus()) && !"SHUTOFF".equals(cloudVm.getVmStatus())) {
                throw ApiException.createApiException("110081");
            }
            /*判断是否设置受管子网，将实际要修改的结果赋值*/
            if (null == params.get(ApiInstanceConstant.Instance.MSUBNETID)) {
                resultMsubnetId = (StringUtil.isEmpty(cloudVm.getSubnetId()) || "null".equals(cloudVm.getSubnetId())) ? ApiInstanceConstant.SET_NULL : cloudVm.getSubnetId();
            } else {
                resultMsubnetId = msubnetId;
            }
            /*判断是否设置自管子网，将实际要修改的结果赋值*/
            if (null == params.get(ApiInstanceConstant.Instance.UMSUBNETID)) {
                resultUmsubnetId = (StringUtil.isEmpty(cloudVm.getSelfSubnetId()) || "null".equals(cloudVm.getSelfSubnetId())) ? ApiInstanceConstant.SET_NULL : cloudVm.getSelfSubnetId();
            } else {
                resultUmsubnetId = umsubnetId;
            }
            /*受管子网和自管子网同时未设置*/
            if (null == params.get(ApiInstanceConstant.Instance.MSUBNETID) && null == params.get(ApiInstanceConstant.Instance.UMSUBNETID)) {
                throw ApiException.createApiException("110063");
            }
            /*设置了受管子网*/
            if (null != params.get(ApiInstanceConstant.Instance.MSUBNETID)) {
                /*如果改变后的受管子网id和原本的受管子网id不同，就需要校验是否可以解除当前受管子网*/
                if (!cloudVm.getSubnetId().equals(resultMsubnetId)) {
                    if (checkVmBindingFloatIp(instanceId)) {
                        throw ApiException.createApiException("110059");
                    }
                    if (checkVmIsLdMember(instanceId)) {
                        throw ApiException.createApiException("110060");
                    }
                    if (checkVmBindingPortMapping(instanceId)) {
                        throw ApiException.createApiException("110061");
                    }
                }
                /*受管子网不为空*/
                if (!ApiInstanceConstant.SET_NULL.equals(msubnetId)) {
                    /*校验受管子网*/
                    switch (checkSubnetId(msubnetId, cloudVm.getNetId(), "1", false)) {
                    case 0:
                        cloudVm.setSubnetId(msubnetId);
                        break;
                    case 1:
                        throw ApiException.createApiException("110005");
                    case 2:
                        throw ApiException.createApiException("110006");
                    case 3:
                        throw ApiException.createApiException("110057");
                    case 4:
                        throw ApiException.createApiException("110008");
                    }
                }
            }
            /*设置了自管子网*/
            if (null != params.get(ApiInstanceConstant.Instance.UMSUBNETID)) {
                /*设置了自管子网*/
                if (!ApiInstanceConstant.SET_NULL.equals(umsubnetId)) {
                    switch (checkSubnetId(umsubnetId, cloudVm.getNetId(), "0", false)) {
                    case 0:
                        cloudVm.setSelfSubnetId(umsubnetId);
                        break;
                    case 1:
                        throw ApiException.createApiException("110010");
                    case 2:
                        throw ApiException.createApiException("110009");
                    case 3:
                        throw ApiException.createApiException("110058");
                    case 4:
                        throw ApiException.createApiException("110062");
                    default:
                        return null;
                    }
                }
            }
            
            if (ApiInstanceConstant.SET_NULL.equals(resultMsubnetId) && ApiInstanceConstant.SET_NULL.equals(resultUmsubnetId)) {
                throw ApiException.createApiException("110063");
            }
            cloudVm.setSubnetId(resultMsubnetId);
            cloudVm.setSelfSubnetId(resultUmsubnetId);
            /*调用接口*/
            modifySubnet(cloudVm);
            logService.addLog("修改子网", "API", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), project.getProjectId(), cusId, ConstantClazz.LOG_STATU_SUCCESS, null);
        } catch (AppException e) {
            e.printStackTrace();
            String errCode = ErrorsUtil.escapeApiErrCode(e.getOriginMessage()[0]);
            logService.addLog("修改子网", "API", ConstantClazz.LOG_TYPE_HOST, null == cloudVm ? "" : cloudVm.getVmName(), null == project ? "" : project.getProjectId(), cusId, ConstantClazz.LOG_STATU_ERROR, e);
            throw ApiException.createApiException(errCode);
        } catch (Exception e) {
            logService.addLog("修改子网", "API", ConstantClazz.LOG_TYPE_HOST, null == cloudVm ? "" : cloudVm.getVmName(), null == project ? "" : project.getProjectId(), cusId, ConstantClazz.LOG_STATU_ERROR, e);
            throw e;
        }
	    return null;
	}

	/**
	 * 检验云主机id是否合法
	 * 
	 * @author gaoxiang
	 * @param instanceId
	 * @param prjId
	 * @param checkCharge
	 *            是否校验云主机计费状态正常
	 * @return 1:instanceId为空 2:instanceId格式不合法 3:instanceId不存在
	 *         4:instanceId对应的资源在回收站中 5:instanceId对应的资源到期 6:instanceId对应的资源欠费
	 */
	private int checkInstanceId(String instanceId, String prjId, boolean checkCharge) {
		if (StringUtil.isEmpty(instanceId)) {
			return 1;
		}
		if (!InstanceApiUtil.uuidRegex(instanceId, true)) {
			return 2;
		}
		BaseCloudVm baseVm = cloudVmDao.findOne(instanceId);
		if (null == baseVm || !"1".equals(baseVm.getIsVisable()) || "1".equals(baseVm.getIsDeleted())
				|| !prjId.equals(baseVm.getPrjId())) {
			return 3;
		}
		if ("2".equals(baseVm.getIsDeleted())) {
			return 4;
		}
		if (checkCharge && !"0".equals(baseVm.getChargeState())) {
			if ("1".equals(baseVm.getPayType())) {
				return 5;
			} else if ("2".equals(baseVm.getPayType())) {
				return 6;
			}
		}
		return 0;
	}

	/**
	 * 校验云主机是否绑定端口映射
	 * 
	 * @author gaoxiang
	 * @param instanceId
	 * @return
	 */
	private boolean checkVmBindingPortMapping(String instanceId) {
		List<BaseCloudPortMapping> portMappingList = portMappingService.queryPortMappingListByDestinyId(instanceId);
		return (null != portMappingList && portMappingList.size() > 0);
	}

	/**
	 * 校验子网id
	 * 
	 * @author gaoxiang
	 * @param subnetId
	 *            欲校验的子网id
	 * @param networkId
	 *            继承网络id
	 * @param subnetType
	 *            0代表自管子网，1代表受管子网
	 * @param checkHasBindRoute
	 *            是否检验子网绑定路由
	 * @return 1：subnetId格式不合法 2：subnetId不存在 3：subnetwork受管自管类型不匹配
	 *         4：subnetId不继承networkId 5：subnetId未绑定路由
	 * @throws ApiException
	 */
	private int checkSubnetId(String subnetId, String networkId, String subnetType, boolean checkHasBindRoute) {
		/* 子网id格式校验 */
		if (!InstanceApiUtil.uuidRegex(subnetId, true)) {
			return 1;
		}
		BaseCloudSubNetWork subnetwork = subnetworkService.getSubNetworkById(subnetId);
		/* 子网id存在 */
		if (subnetwork == null) {
			return 2;
		}
		/* 受管自管类型正确 */
		if (!subnetType.equals(subnetwork.getSubnetType())) {
			return 3;
		}
		/* 子网继承网络 */
		if (!subnetwork.getNetId().equals(networkId)) {
			return 4;
		}
		/* 受管子网连接路由 */
		if (checkHasBindRoute && subnetworkService.getSubBindRouteCount(subnetId) == 0) {
			return 5;
		}
		return 0;
	}

	/**
	 * 将CloudVm对象转义为API返回的格式<br>
	 * ----------------------------
	 * 
	 * @author zhouhaitao
	 * 
	 * @param cloudVm
	 * 
	 * @return
	 * @throws Exception
	 */
	private JSONObject escapeInstance(CloudVm cloudVm) {
		JSONObject json = new JSONObject();
		json.put(ApiInstanceConstant.Instance.INSTANCEID, cloudVm.getVmId());
		json.put(ApiInstanceConstant.Instance.INSTANCENAME, cloudVm.getVmName());
		json.put(ApiInstanceConstant.Instance.INSTANCEREMARK, cloudVm.getVmDescripstion());
		if (CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(cloudVm.getChargeState())) {
	        json.put(ApiInstanceConstant.Instance.STATUS, ApiInstanceConstant.InstanceStatus.getVmStatus(cloudVm.getVmStatus()));
		} else {
		    json.put(ApiInstanceConstant.Instance.STATUS, 
		            CloudResourceUtil.CLOUD_CHARGESTATE_NSF_CODE.equals(PayType.PAYBEFORE.equals(cloudVm.getPayType())) ? "EXPIRE" : "ARREARS");
		}
		json.put(ApiInstanceConstant.Instance.SYSNAME, cloudVm.getSysType());
		json.put(ApiInstanceConstant.Instance.CPU, cloudVm.getCpus());
		json.put(ApiInstanceConstant.Instance.MEMORY, cloudVm.getRams());
		json.put(ApiInstanceConstant.Instance.VOLUMECOUNT, cloudVm.getVolCount());
		json.put(ApiInstanceConstant.Instance.SYSVLOUMESIZE, cloudVm.getDisks());
		json.put(ApiInstanceConstant.Instance.VOLUMESIZE, cloudVm.getDataCapacity());
		json.put(ApiInstanceConstant.Instance.VPCID, cloudVm.getNetId());
		json.put(ApiInstanceConstant.Instance.MSUBNETID, "null".equals(cloudVm.getSubnetId()) ? "未加入" : cloudVm.getSubnetId());
		json.put(ApiInstanceConstant.Instance.UMSUBNETID, "null".equals(cloudVm.getSelfSubnetId()) ? "未加入" :cloudVm.getSelfSubnetId());
		json.put(ApiInstanceConstant.Instance.MSUBNETIP, "null".equals(cloudVm.getVmIp()) ? "未加入" : cloudVm.getVmIp());
		json.put(ApiInstanceConstant.Instance.UMSUBNETIP, "null".equals(cloudVm.getSelfIp()) ? "未加入" : cloudVm.getSelfIp());
		json.put(ApiInstanceConstant.Instance.PUBLICIP, "null".equals(cloudVm.getFloatIp()) ? "未设置" : cloudVm.getFloatIp());
		try {
			json.put(ApiInstanceConstant.Instance.REGION, ApiUtil.getDcCodeById(cloudVm.getDcId()).getString("dcName"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		json.put(ApiInstanceConstant.Instance.SECURITYGROUPNAME, cloudVm.getSecurityGroups());
		json.put(ApiInstanceConstant.Instance.PAYTYPE, InstanceApiUtil.escapePayType(cloudVm.getPayType()));
		json.put(ApiInstanceConstant.Instance.CREATETIME, DateUtil.getUTCDateZ(cloudVm.getCreateTime()));
		if (PayType.PAYBEFORE.equals(cloudVm.getPayType())) {
		    json.put(ApiInstanceConstant.Instance.EXPIRTIME, DateUtil.getUTCDateZ(cloudVm.getEndTime()));
		}
		return json;
	}

	@ApiMethod("V1/InstanceJoinSecurityGroup")
	@Override
	public JSONObject instanceJoinSecurityGroup(JSONObject params) throws ApiException {
		String dcId = (null != params.get(ApiInstanceConstant.Instance.DCID))
				? params.getString(ApiInstanceConstant.Instance.DCID) : "";// 数据中心
		String instanceId = (null != params.get(ApiInstanceConstant.Instance.INSTANCEID))
				? params.getString(ApiInstanceConstant.Instance.INSTANCEID) : ""; // 云主机ID
		String cusId = (null != params.get(ApiInstanceConstant.Instance.CUSID))
				? params.getString(ApiInstanceConstant.Instance.CUSID) : "";// 客户ID
		String securityGroupId = (null != params.get(ApiInstanceConstant.Instance.SECURITYGROUPID))
				? params.getString(ApiInstanceConstant.Instance.SECURITYGROUPID) : "";// 安全组ID
		CloudVm cloudVm = new CloudVm();
		CloudSecurityGroup cloudSecurityGroup = new CloudSecurityGroup();
		CloudProject project = new CloudProject();
		try {
			// 校验参数
			boolean checkResult = checkForVmJoinOrLeaveSecurityGroup(dcId, instanceId, securityGroupId, cusId, "join",
					cloudVm, cloudSecurityGroup, project);
			if (checkResult) {
				// 业务操作
				securityGroupApiService.instanceJoinSecurityGroup(cloudVm, cloudSecurityGroup);
			}
			logService.addLog("加入安全组", "API", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), project.getProjectId(),
					cusId, ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (AppException e) {
			e.printStackTrace();
			logService.addLog("加入安全组", "API", ConstantClazz.LOG_TYPE_HOST, cloudVm == null ? "" : cloudVm.getVmName(),
					project == null ? "" : project.getProjectId(), cusId, ConstantClazz.LOG_STATU_ERROR, e);
			String errCode = ErrorsUtil.escapeApiErrCode(e.getOriginMessage()[0]);
			throw ApiException.createApiException(errCode);
		} catch (Exception e) {
			e.printStackTrace();
			logService.addLog("加入安全组", "API", ConstantClazz.LOG_TYPE_HOST, cloudVm == null ? "" : cloudVm.getVmName(),
					project == null ? "" : project.getProjectId(), cusId, ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}

		return null;
	}

	@ApiMethod("V1/InstanceLeaveSecurityGroup")
	@Override
	public JSONObject instanceLeaveSecurityGroup(JSONObject params) throws ApiException {
		String dcId = (null != params.get(ApiInstanceConstant.Instance.DCID))
				? params.getString(ApiInstanceConstant.Instance.DCID) : "";// 数据中心
		String instanceId = (null != params.get(ApiInstanceConstant.Instance.INSTANCEID))
				? params.getString(ApiInstanceConstant.Instance.INSTANCEID) : ""; // 云主机ID
		String cusId = (null != params.get(ApiInstanceConstant.Instance.CUSID))
				? params.getString(ApiInstanceConstant.Instance.CUSID) : "";// 客户ID
		String securityGroupId = (null != params.get(ApiInstanceConstant.Instance.SECURITYGROUPID))
				? params.getString(ApiInstanceConstant.Instance.SECURITYGROUPID) : "";// 安全组ID
		CloudVm cloudVm = new CloudVm();
		CloudSecurityGroup cloudSecurityGroup = new CloudSecurityGroup();
		CloudProject project = new CloudProject();
		try {
			// 校验参数
			boolean checkResult = checkForVmJoinOrLeaveSecurityGroup(dcId, instanceId, securityGroupId, cusId, "leave",
					cloudVm, cloudSecurityGroup, project);
			if (checkResult) {
				securityGroupApiService.instanceLeaveSecurityGroup(cloudVm, cloudSecurityGroup);
			}
			logService.addLog("移除安全组", "API", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), project.getProjectId(),
					cusId, ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (AppException e) {
			e.printStackTrace();
			logService.addLog("移除安全组", "API", ConstantClazz.LOG_TYPE_HOST, cloudVm == null ? "" : cloudVm.getVmName(),
					project == null ? "" : project.getProjectId(), cusId, ConstantClazz.LOG_STATU_ERROR, e);
			String errCode = ErrorsUtil.escapeApiErrCode(e.getOriginMessage()[0]);
			throw ApiException.createApiException(errCode);
		} catch (Exception e) {
			e.printStackTrace();
			logService.addLog("移除安全组", "API", ConstantClazz.LOG_TYPE_HOST, cloudVm == null ? "" : cloudVm.getVmName(),
					project == null ? "" : project.getProjectId(), cusId, ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
		return null;
	}

	/**
	 * 云主机加入安全组和离开安全组的相关参数的校验
	 * 
	 * @author liuzhuangzhuang
	 * @return
	 */
	private boolean checkForVmJoinOrLeaveSecurityGroup(String dcId, String instanceId, String securityGroupId,
			String cusId, String type, CloudVm cloudVm, CloudSecurityGroup cloudSecurityGroup, CloudProject project) throws ApiException {
		try {
			CloudProject cloudProject = projectService.queryProjectByDcAndCus(dcId, cusId);
			if(cloudProject == null){ //该客户在该数据中心下是否存在项目
				throw ApiException.createApiException("100030");
			}
			BeanUtils.copyPropertiesByModel(project, cloudProject);
			if (StringUtil.isEmpty(instanceId)) {// 云主机ID为空判断
				throw ApiException.createApiException("110042");
			}
			if (!InstanceApiUtil.uuidRegex(instanceId, true)) {// 云主机ID格式是否正确
				throw ApiException.createApiException("110044");
			}
			if (StringUtil.isEmpty(securityGroupId)) {// 安全组ID为空判断
				throw ApiException.createApiException("110064");
			}
			if(!InstanceApiUtil.uuidRegex(securityGroupId, true)){//安全组ID格式是否正确
				throw ApiException.createApiException("110066");
			}

			CloudVm vm = queryVmById(instanceId, cloudProject.getProjectId());
			if(vm == null || (vm != null && (vm.getIsDeleted().equals("1") || !vm.getIsVisable().equals("1")))){// 云主机是否属于该客户(不统计已删除的资源)
				throw ApiException.createApiException("110043");
			}
			if(vm.getIsDeleted().equals("2")){//判断云主机是否在回收站
				if("join".equals(type)){//
					throw ApiException.createApiException("110070");
				} else if ("leave".equals(type)) {
					throw ApiException.createApiException("110071");
				}
			}
			if(!vm.getChargeState().equals("0")){//判断云主机的计费状态
				if ("1".equals(vm.getChargeState())) {//账户余额不足
					throw ApiException.createApiException("100017");
		        } else if ("2".equals(vm.getChargeState()) || "3".equals(vm.getChargeState())) {//资源已到期
		            throw ApiException.createApiException("100019");
		        }
			}
			BeanUtils.copyPropertiesByModel(cloudVm, vm);
			//根据安全组ID获取安全组的相关信息
			CloudSecurityGroup securityGroup = securityGroupApiService.getSecurityGroupBySgId(securityGroupId);
			/*if(cloudSecurityGroup == null){//判断安全组是否存在
				throw ApiException.createApiException("110065");
			}*/
			if(securityGroup == null || (securityGroup != null && !securityGroup.getCusId().equals(cusId))){//判断该安全组是否属于该客户
				throw ApiException.createApiException("110065");
			}
			List<BaseCloudVmSgroup> vmSgroupList = securityGroupApiService
					.getVmSgroupByVmIdAndSecurityGropId(instanceId, securityGroupId);
			boolean isJoin = false;
			if (vmSgroupList != null && vmSgroupList.size() > 0) {
				isJoin = true;
			}
			if ("join".equals(type) && isJoin) {// 该安全组已经加入到该云主机
				throw ApiException.createApiException("110067");
			}
			if ("leave".equals(type) && !isJoin) {// 该安全组未加入到该云主机
				throw ApiException.createApiException("110068");
			}
			BeanUtils.copyPropertiesByModel(cloudSecurityGroup, securityGroup);
		}catch(Exception e){
			throw e;
		}
		return true;
	}

	/**
	 * 查询安全组
	 * @author liuzhuangzhuang
	 * @param params
	 * @return
	 * @throws ApiException
	 */
	@ApiMethod("V1/DescribeSecurityGroups")
	public JSONObject describeSecurityGroups(JSONObject params) throws ApiException {
		// 获取参数
		String searchWord = (null != params.get(ApiInstanceConstant.Instance.SEARCHWORD))?
				params.getString(ApiInstanceConstant.Instance.SEARCHWORD):"";
		String offset = (null != params.get(ApiInstanceConstant.Instance.OFFSET) 
				&& !StringUtil.isEmpty(params.getString(ApiInstanceConstant.Instance.OFFSET)))?
				params.getString(ApiInstanceConstant.Instance.OFFSET):"0";
		String limit = (null != params.get(ApiInstanceConstant.Instance.LIMIT) 
				&& !StringUtil.isEmpty(params.getString(ApiInstanceConstant.Instance.LIMIT)))?
				params.getString(ApiInstanceConstant.Instance.LIMIT):"20";
		String dcId = (null != params.get(ApiInstanceConstant.Instance.DCID))?
				params.getString(ApiInstanceConstant.Instance.DCID):"";
		String cusId = (null != params.get(ApiInstanceConstant.Instance.CUSID))?
				params.getString(ApiInstanceConstant.Instance.CUSID):"";
		Object securityGroupIdsObj = (null != params.get(ApiInstanceConstant.SecurityGroup.SECURITYGROUPIDS))?
				params.get(ApiInstanceConstant.SecurityGroup.SECURITYGROUPIDS):null;
		CloudProject cloudProject = projectService.queryProjectByDcAndCus(dcId, cusId);
		// 参数校验
		try{
			JSONArray securityGroupIds = null;
			if(securityGroupIdsObj != null){
				if(securityGroupIdsObj.getClass().equals(JSONArray.class)){
					securityGroupIds = params.getJSONArray(ApiInstanceConstant.SecurityGroup.SECURITYGROUPIDS);
				}else{//securityGroupIds格式有误
					throw ApiException.createApiException("110066");
				}
			}
			boolean checkResult = checkDescribeSecurityGroups(dcId, cusId, securityGroupIds, searchWord, offset, limit);
			if (checkResult) {
				List<CloudSecurityGroup> groupList = new ArrayList<CloudSecurityGroup>();
				if (securityGroupIds == null) {
					groupList = securityGroupApiService.getGroupList(dcId, cusId, null, searchWord, offset, limit);
				} else {
					String[] securityGroupId = new String[securityGroupIds.size()];
					securityGroupIds.toArray(securityGroupId);
					groupList = securityGroupApiService.getGroupList(dcId, cusId, securityGroupId, searchWord, offset,
							limit);
				}
				if (groupList == null) { // 未找到查询结果
					throw ApiException.createApiException("100025");
				}
				JSONObject[] json = this.escapeSecurityGroup(groupList);
				JSONObject result = new JSONObject();
				result.put(ApiInstanceConstant.Instance.TOTALCOUNT, json.length);
				result.put(ApiInstanceConstant.SecurityGroup.SECURITYGROUPSET, json);
				logService.addLog("查询安全组", "API", ConstantClazz.LOG_TYPE_HOST, null, cloudProject.getProjectId(),
						cusId, ConstantClazz.LOG_STATU_SUCCESS, null);
				return result;
			}
		}catch (AppException e){
			e.printStackTrace();
			logService.addLog("查询安全组", "API", ConstantClazz.LOG_TYPE_HOST, null,
					cloudProject == null ? "" : cloudProject.getProjectId(), cusId, ConstantClazz.LOG_STATU_ERROR, e);
			String errCode = ErrorsUtil.escapeApiErrCode(e.getOriginMessage()[0]);
			throw ApiException.createApiException(errCode);
		}catch (Exception e){
			e.printStackTrace();
			logService.addLog("查询安全组", "API", ConstantClazz.LOG_TYPE_HOST, null,
					cloudProject == null ? "" : cloudProject.getProjectId(), cusId, ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
		return null;
	}
	
	/**
	 * 查询云主机对应的公网IP
	 * 
	 * @param vmId
	 * @return
	 */
	private CloudFloatIp queryFloatIpByVm(String vmId) {
		CloudFloatIp floatIp = null;
		StringBuffer sql = new StringBuffer();
		sql.append(" select ");
		sql.append(" 		flo_id,	");
		sql.append(" 		flo_ip,	");
		sql.append(" 		dc_id,	");
		sql.append(" 		prj_id	");
		sql.append(" from cloud_floatip ");
		sql.append(" where resource_type = 'vm' ");
		sql.append(" and resource_id= ? ");
		sql.append(" and is_deleted ='0' ");

		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[] { vmId });
		@SuppressWarnings("rawtypes")
		List list = query.getResultList();
		if (null != list && list.size() == 1) {
			floatIp = new CloudFloatIp();
			int index = 0;
			Object[] objs = (Object[]) list.get(0);

			floatIp.setFloId(String.valueOf(objs[index++]));
			floatIp.setFloIp(String.valueOf(objs[index++]));
			floatIp.setDcId(String.valueOf(objs[index++]));
			floatIp.setPrjId(String.valueOf(objs[index++]));
		}
		return floatIp;
	}

	/**
	 * 云主机软删除
	 * 
	 * @author chengxiaodong
	 * 
	 * @param cloudVm
	 */
	private void softDeleteVm(CloudVm cloudVm,User user){
		try {
			volumeApiService.debindVolsByVmId(cloudVm.getVmId());

			CloudFloatIp cloudFloatIp = queryFloatIpByVm(cloudVm.getVmId());
			if (null != cloudFloatIp) {
				cloudFloatIp.setResourceId(cloudVm.getVmId());
				cloudFloatIp.setResourceType("vm");
				floatIpApiService.unbundingResource(cloudFloatIp);
			}

			openstackVmService.softDeleteVm(cloudVm);

			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			vm.setVmStatus("SOFT_DELETING");
			vm.setDeleteUser(user.getUserAccount());//此处需要记录客户admin账号
			vm.setDeleteTime(new Date());

			cloudVmDao.merge(vm);

			alarmApiService.cleanAlarmDataAfterDeletingVM(cloudVm.getVmId());

			ecmcAlarmApiService.cleanAlarmDataAfterDeletingObject(cloudVm.getVmId());

			memberApiService.deleteMemberByVm(cloudVm.getVmId());
			
			portMappingService.deletePortMappingListByDestinyId(cloudVm.getDcId(),cloudVm.getPrjId(),cloudVm.getVmId());
			
			modifySysDiskForRecycle(cloudVm.getVmId(),user.getUserAccount(),vm.getDeleteTime());
			
			JSONObject json = new JSONObject();
			json.put("vmId", vm.getVmId());
			json.put("dcId", vm.getDcId());
			json.put("prjId", vm.getPrjId());
			json.put("vmStatus", vm.getVmStatus());
			json.put("count", "0");

			final JSONObject data = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.vmKey, data.toJSONString());
				}

			});

			cloudVm.setOpDate(vm.getDeleteTime());
			cloudVm.setCusId(user.getCusId());
			cloudVm.setVmName(vm.getVmName());

			if (PayType.PAYAFTER.equals(vm.getPayType())) {
				vmOptionCharge(cloudVm, "recycle");
			}
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}
	}

	/**
	 * 修改云主机 对应系统盘的的回收站状态
	 * 
	 * @author chengxiaodong
	 * @param vmId
	 * @return
	 */
	private boolean modifySysDiskForRecycle(String vmId, String user, Date deleteTime) {
		StringBuffer sql = new StringBuffer();

		boolean isSuccess = false;
		try {
			sql.append(" update cloud_volume set ");
			sql.append("   delete_user =  ? ");
			sql.append(" , delete_time =  ? ");
			sql.append(" where vm_id = ? ");
			sql.append(" and vol_bootable = '1' ");
			cloudVmDao.execSQL(sql.toString(), new Object[] { user, deleteTime, vmId });

			isSuccess = true;
		} catch (Exception e) {
			isSuccess = false;

		}
		return isSuccess;
	}

	/**
	 * 云主机限制服务 计费队列(type = "restrict") 云主机恢复服务 计费队列(type = "recover") 云主机放入回收站
	 * 计费队列(type = "recycle") 云主机回收站中还原 计费队列(type = "restore") 云主机回彻底删除
	 * 计费队列(type = "delete")
	 * 
	 * @param cloudVm
	 */
	public void vmOptionCharge(final CloudVm cloudVm, final String type) {
		TransactionHookUtil.registAfterCommitHook(new Hook() {
			@Override
			public void execute() {
				ChargeRecord record = new ChargeRecord();
				String queueName = null;

				record.setResourceId(cloudVm.getVmId());
				record.setOpTime(cloudVm.getOpDate());
				record.setDatecenterId(cloudVm.getDcId());
				record.setCusId(cloudVm.getCusId());
				record.setResourceType(ResourceType.VM);

				if ("restrict".equals(type)) {
					queueName = EayunQueueConstant.QUEUE_BILL_RESOURCE_RESTRICT;
				} else if ("recover".equals(type)) {
					queueName = EayunQueueConstant.QUEUE_BILL_RESOURCE_RECOVER;
				} else if ("recycle".equals(type)) {
					record.setResourceName(cloudVm.getVmName());
					queueName = EayunQueueConstant.QUEUE_BILL_RESOURCE_RECYCLE;
				} else if ("restore".equals(type)) {
					queueName = EayunQueueConstant.QUEUE_BILL_RESOURCE_RESTORE;
				} else if ("delete".equals(type)) {
					record.setResourceName(cloudVm.getVmName());
					queueName = EayunQueueConstant.QUEUE_BILL_RESOURCE_DELETE;
				}
				rabbitTemplate.send(queueName, JSONObject.toJSONString(record));
			}
		});
	}
	

	/**
	 * 
	 * 查询安全组的参数校验
	 * 
	 * @author liuzhuangzhuang
	 * @param securityGroupIds
	 * @param searchWord
	 * @param offset
	 * @param limit
	 * @throws ApiException
	 */
	private boolean checkDescribeSecurityGroups(String dcId, String cusId, JSONArray securityGroupIds, String searchWord, 
			String offset, String limit) throws ApiException {
		CloudProject cloudProject = projectService.queryProjectByDcAndCus(dcId, cusId);
		if(cloudProject == null){ //该客户在该数据中心下是否存在项目
			throw ApiException.createApiException("100030");
		}
		// securityGroupIds的相关判断
		if (securityGroupIds != null) {
			if(securityGroupIds.size() > 20){
				throw ApiException.createApiException("110066");
			}
			String[] securityGroupId = new String[securityGroupIds.size()];
			securityGroupIds.toArray(securityGroupId);
			for(String id:securityGroupId){
				if(StringUtil.isEmpty(id)){
					throw ApiException.createApiException("110064");
				}
				if(!InstanceApiUtil.uuidRegex(id, true)){
					throw ApiException.createApiException("110066");
				}
				CloudSecurityGroup cloudSecurityGroup = securityGroupApiService.getSecurityGroupBySgId(id);
				if(cloudSecurityGroup == null || (cloudSecurityGroup != null && !cloudSecurityGroup.getCusId().equals(cusId))){
					throw ApiException.createApiException("110065");//  判断该安全组是否属于该客户(是否存在)
				}
			}
		}
		if(!StringUtil.isEmpty(searchWord) && !searchWord.matches(ApiInstanceConstant.CLOUD_RESOURCE_NAME_REGEX)){//查询关键字格式判断
			throw ApiException.createApiException("100023");
		}
		// 偏移量的判断---是否是数字
		if (!offset.matches("^\\d+$")) {
			throw ApiException.createApiException("100026");
		}
		// 数据限制的判断
		if (!limit.matches("^([1-9]\\d{0,1}|100)$")) {
			throw ApiException.createApiException("100027");
		}
		return true;
	}

	/**
	 * 云主机强制删除
	 * 
	 * @author chengxiaodong
	 * @param cloudVm
	 */
	public void forceDeleteVm(CloudVm cloudVm,User user) {
		try {
			if (checkSavingSnapshot(cloudVm)) {
				throw new AppException("当前云主机正在创建自定义镜像，不允许删除");
			}

			CloudFloatIp cloudFloatIp = queryFloatIpByVm(cloudVm.getVmId());
			if (null != cloudFloatIp) {
				cloudFloatIp.setResourceId(cloudVm.getVmId());
				cloudFloatIp.setResourceType("vm");
				floatIpApiService.unbundingResource(cloudFloatIp);
			}

			volumeApiService.debindVolsByVmId(cloudVm.getVmId());

			openstackVmService.forceDelete(cloudVm);

			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			vm.setDeleteUser(user.getUserAccount());//客户admin账号
			vm.setDeleteTime(new Date());
			vm.setVmStatus("DELETING");
			cloudVm.setOpDate(vm.getDeleteTime());
			cloudVm.setVmName(vm.getVmName());

			cloudVmDao.merge(vm);

			tagService.refreshCacheAftDelRes("vm", cloudVm.getVmId());

			alarmApiService.cleanAlarmDataAfterDeletingVM(cloudVm.getVmId());

			ecmcAlarmApiService.cleanAlarmDataAfterDeletingObject(cloudVm.getVmId());

			memberApiService.deleteMemberByVm(cloudVm.getVmId());
			
			volumeApiService.deleteVolumeByVm(cloudVm.getVmId(), user.getUserAccount());
			
			portMappingService.deletePortMappingListByDestinyId(cloudVm.getDcId() ,cloudVm.getPrjId(),cloudVm.getVmId());
			
			//解绑的云主机
			JSONObject json = new JSONObject();
			json.put("vmId", vm.getVmId());
			json.put("dcId", vm.getDcId());
			json.put("prjId", vm.getPrjId());
			json.put("vmStatus", vm.getVmStatus());
			json.put("count", "0");

			final JSONObject data = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.vmKey, data.toJSONString());
				}

			});

			if (PayType.PAYAFTER.equals(vm.getPayType())) {
				vmOptionCharge(cloudVm, "delete");
			}
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}
	}

	/**
	 * 从回收站中彻底删除
	 * 
	 * @param cloudVm
	 */
	public void deleteSoftVm(CloudVm cloudVm,User user){
		openstackVmService.forceDelete(cloudVm);

		BaseCloudVm vm = new BaseCloudVm();
		vm = cloudVmDao.findOne(cloudVm.getVmId());
		vm.setVmStatus("DELETING");
		vm.setDeleteTime(new Date());
		vm.setDeleteUser(user.getUserAccount());
		vm.setIsDeleted("1");

		cloudVmDao.merge(vm);

		tagService.refreshCacheAftDelRes("vm", cloudVm.getVmId());
		
		volumeApiService.deleteVolumeByVm(cloudVm.getVmId(), user.getUserAccount());
		
		floatIpApiService.refreshFloatIpByVm(cloudVm.getVmId());

		JSONObject json = new JSONObject();
		json.put("vmId", vm.getVmId());
		json.put("dcId", vm.getDcId());
		json.put("prjId", vm.getPrjId());
		json.put("vmStatus", vm.getVmStatus());
		json.put("count", "0");

		final JSONObject data = json;
		TransactionHookUtil.registAfterCommitHook(new Hook() {
			@Override
			public void execute() {
				jedisUtil.addUnique(RedisKey.vmKey, data.toJSONString());
			}
		});
	}
	
	/**
	 * 校验购买云主机的数据<br>
	 * ---------------------
	 * 
	 * @author zhouhaitao
	 * 
	 * @param json
	 * @return
	 * @throws ApiException
	 */
	private boolean validateCreateInstanceData(JSONObject json, CloudOrderVm cov) throws ApiException {
		BaseCloudSubNetWork msubnetwork = null;
		BaseCloudSubNetWork umsubnetwork = null;

		String dcId = json.getString(ApiInstanceConstant.Instance.DCID);
		String cusId = json.getString(ApiInstanceConstant.Instance.CUSID);
		String vpcId = json.getString(ApiInstanceConstant.Instance.VPCID);
		String msubnetId = json.getString(ApiInstanceConstant.Instance.MSUBNETID);
		String umsubnetId = json.getString(ApiInstanceConstant.Instance.UMSUBNETID);
		String isBuyFloatIp = json.getString(ApiInstanceConstant.Instance.FLOATIP);
		String imageType = json.getString(ApiInstanceConstant.Instance.IMAGETYPE);
		String imageId = json.getString(ApiInstanceConstant.Instance.IMAGEID);
		String cpu = json.getString(ApiInstanceConstant.Instance.CPU);
		String memory = json.getString(ApiInstanceConstant.Instance.MEMORY);
		String loginMode = json.getString(ApiInstanceConstant.Instance.LOGINMODE);
		String keypir = json.getString(ApiInstanceConstant.Instance.KEYPAIR);
		String password = json.getString(ApiInstanceConstant.Instance.PASSWORD);
		String instanceName = json.getString(ApiInstanceConstant.Instance.INSTANCENAME);
		String instanceRemark = json.getString(ApiInstanceConstant.Instance.INSTANCEREMARK);
		String securityGroupName = json.getString(ApiInstanceConstant.Instance.SECURITYGROUPNAME);
		String count = json.getString(ApiInstanceConstant.Instance.COUNT);
		String payType = json.getString(ApiInstanceConstant.Instance.PAYTYPE);
		String payduration = json.getString(ApiInstanceConstant.Instance.PAYDURATION);

		cov.setBuyFloatIp("0");
		cov.setImageType("0");
		cov.setCount(1);
		cov.setBuyCycle(1);

		CloudProject cloudProject = validateProject(dcId, cusId);

		if (StringUtil.isEmpty(vpcId)) {
			throw ApiException.createApiException("110002");
		}
		if (!InstanceApiUtil.uuidRegex(vpcId, true)) {
			throw ApiException.createApiException("110003");
		}
		CloudNetWork network = networkApiService.queryNetworkByPrjIdAndNetId(cloudProject.getProjectId(), vpcId);
		if (null == network) {
			throw ApiException.createApiException("110001");
		}
		if (StringUtil.isEmpty(msubnetId)) {
			throw ApiException.createApiException("110004");
		}
		if (!InstanceApiUtil.uuidRegex(msubnetId, true)) {
			throw ApiException.createApiException("110005");
		}
		msubnetwork = subnetworkService.getSubNetworkById(msubnetId);
		if (null == msubnetwork) {
			throw ApiException.createApiException("110006");
		}
		if (!msubnetwork.getSubnetType().equals("1")) {
			throw ApiException.createApiException("110006");
		}
		if (!msubnetwork.getNetId().equals(vpcId)) {
			throw ApiException.createApiException("110008");
		}
		if (StringUtil.isEmpty(msubnetwork.getRouteId())) {
			throw ApiException.createApiException("110007");
		}
		if (!StringUtil.isEmpty(umsubnetId)) {
			if (!InstanceApiUtil.uuidRegex(umsubnetId, true)) {
				throw ApiException.createApiException("110010");
			}
			umsubnetwork = subnetworkService.getSubNetworkById(umsubnetId);
			if (null == umsubnetwork || !umsubnetwork.getNetId().equals(vpcId)
					|| !umsubnetwork.getSubnetType().equals("0")) {
				throw ApiException.createApiException("110009");
			}
			cov.setSelfSubnetId(umsubnetId);
			cov.setSelfSubnetName(umsubnetwork.getSubnetName());
			cov.setSelfCidr(umsubnetwork.getCidr());
		}
		if (!StringUtil.isEmpty(isBuyFloatIp)) {
			if (!isBuyFloatIp.equals(ApiInstanceConstant.BUY_FLOATIP_YES)
					&& !isBuyFloatIp.equals(ApiInstanceConstant.BUY_FLOATIP_NO)) {
				throw ApiException.createApiException("110011");
			}
			if (isBuyFloatIp.equals(ApiInstanceConstant.BUY_FLOATIP_YES) && StringUtil.isEmpty(network.getExtNetId())) {
				throw ApiException.createApiException("110012");
			}

		}
		if (!StringUtil.isEmpty(imageType)) {
			if (!imageType.equals(ApiInstanceConstant.IMAGE_TYPE_PUBLILC)
					&& !imageType.equals(ApiInstanceConstant.IMAGE_TYPE_PRIVATE)
					&& !imageType.equals(ApiInstanceConstant.IMAGE_TYPE_MARKET)) {
				throw ApiException.createApiException("110015");
			}
		}
		else{
			imageType = ApiInstanceConstant.IMAGE_TYPE_PUBLILC;
		}
		if (StringUtil.isEmpty(imageId)) {
			throw ApiException.createApiException("110014");
		}
		if (!InstanceApiUtil.uuidRegex(imageId, true)) {
			throw ApiException.createApiException("110017");
		}
		CloudImage image = getImageById(imageId);
		if (null == image) {
			throw ApiException.createApiException("110016");
		}
		if (((imageType.equals(ApiInstanceConstant.IMAGE_TYPE_PUBLILC) 
				|| StringUtil.isEmpty(imageType))
				&& (!image.getImageIspublic().equals('1') 
						|| !dcId.equals(image.getDcId())))
				|| (imageType.equals(ApiInstanceConstant.IMAGE_TYPE_PRIVATE)
						&&(!image.getImageIspublic().equals('2') 
								|| !cloudProject.getProjectId().equals(image.getPrjId())))
				||(imageType.equals(ApiInstanceConstant.IMAGE_TYPE_MARKET) 
						&&(!image.getImageIspublic().equals('3') 
								|| !dcId.equals(image.getDcId())))){
			throw ApiException.createApiException("110016");
		}
		if(imageType.equals(ApiInstanceConstant.IMAGE_TYPE_PRIVATE) && 
				(image.getImageStatus().equals("QUEUED")
						||image.getImageStatus().equals("SAVING"))){
			throw ApiException.createApiException("110073");
		}
		if((imageType.equals(ApiInstanceConstant.IMAGE_TYPE_PUBLILC) 
				|| StringUtil.isEmpty(imageType)
				||imageType.equals(ApiInstanceConstant.IMAGE_TYPE_MARKET))
				&&'2' == image.getIsUse()){
			throw ApiException.createApiException("110146");
		}
		if("DELETING".equals(image.getImageStatus())){
			throw ApiException.createApiException("110147");
		}

		if(StringUtil.isEmpty(cpu)){
			throw ApiException.createApiException("110085");
		}
		if(StringUtil.isEmpty(memory)){
			throw ApiException.createApiException("110086");
		}
		
		if (!checkCpuAndMemory(cpu, memory)) {
			throw ApiException.createApiException("110018");
		}
		int cpuValue = Integer.parseInt(cpu);
		int ramValue = 1024 * Integer.parseInt(memory);
		if (image.getMinCpu() > cpuValue || image.getMinRam() > ramValue) {
			throw ApiException.createApiException("110020");
		}
		if ((image.getMaxCpu() != 0 && image.getMaxCpu() < cpuValue)
				|| (image.getMaxRam() != 0 && image.getMaxRam() < ramValue)) {
			throw ApiException.createApiException("110019");
		}
		if (StringUtil.isEmpty(loginMode)) {
			throw ApiException.createApiException("110021");
		}
		if (!loginMode.equals(ApiInstanceConstant.LOGIN_MODEL_KEY)
				&& !loginMode.equals(ApiInstanceConstant.LOGIN_MODEL_PWD)) {
			throw ApiException.createApiException("110022");
		}
		if (ConstantClazz.DICT_CLOUD_OS_WINDOWS_NODE_ID.equals(image.getOsType())
				&& !loginMode.equals(ApiInstanceConstant.LOGIN_MODEL_PWD)) {
			throw ApiException.createApiException("110022");
		}

		if (loginMode.equals(ApiInstanceConstant.LOGIN_MODEL_KEY)) {
			// TODO 暂时不支持密钥创建，故返回 110023 格式不正确
			if (StringUtil.isEmpty(keypir)) {
				throw ApiException.createApiException("110024");
			}
			if (true) {
				throw ApiException.createApiException("110023");
			}
		}
		if (loginMode.equals(ApiInstanceConstant.LOGIN_MODEL_PWD)) {
			if (StringUtil.isEmpty(password)) {
				throw ApiException.createApiException("110025");
			}
			if (!checkVmPwd(password)) {
				throw ApiException.createApiException("110026");
			}
		}
		if (!StringUtil.isEmpty(securityGroupName)) {
			if (!securityGroupName.equals(ApiInstanceConstant.SECURITYGROUP_DEFAULT)
					&& !securityGroupName.equals(ApiInstanceConstant.SECURITYGROUP_LINUX)
					&& !securityGroupName.equals(ApiInstanceConstant.SECURITYGROUP_WINDOWS)) {
				throw ApiException.createApiException("110077");
			}
		}
		else{
			securityGroupName = ApiInstanceConstant.SECURITYGROUP_DEFAULT;
		}

		if (StringUtil.isEmpty(instanceName)) {
			throw ApiException.createApiException("110027");
		}
		if (!instanceName.matches(ApiInstanceConstant.CLOUD_RESOURCE_NAME_REGEX)) {
			throw ApiException.createApiException("110028");
		}
		if (!StringUtil.isEmpty(instanceRemark) && instanceRemark.length() > 200) {
			throw ApiException.createApiException("110030");
		}
		if (!StringUtil.isEmpty(count)) {
			if (!count.matches(ApiInstanceConstant.VM_BATCH_COUNT_REGEX)) {
				throw ApiException.createApiException("110031");
			}
			cov.setCount(Integer.parseInt(count));
		}
		CloudVm cloudVm = new CloudVm();
		cloudVm.setVmName(instanceName);
		cloudVm.setPrjId(cloudProject.getProjectId());
		cloudVm.setNumber(1);
		if(!StringUtil.isEmpty(count)){
			cloudVm.setNumber(Integer.parseInt(count));
		}
		if (!checkVmExistByName(cloudVm)) {
			throw ApiException.createApiException("110029");
		}
		if (StringUtil.isEmpty(payType)) {
			throw ApiException.createApiException("110032");
		}
		if (!payType.equals(ApiInstanceConstant.PAYTYPE_DYNAMIC)
				&& !payType.equals(ApiInstanceConstant.PAYTYPE_MONTH)) {
			throw ApiException.createApiException("110033");
		}

		if (!StringUtil.isEmpty(payduration)) {
			if (payType.equals(ApiInstanceConstant.PAYTYPE_DYNAMIC)) {
				throw ApiException.createApiException("110034");
			}

			if (!payduration.matches(ApiInstanceConstant.VM_PAYDURATION_REGEX)) {
				throw ApiException.createApiException("110035");
			}
			cov.setBuyCycle(Integer.parseInt(payduration));
		}
		cov.setDcId(dcId);
		cov.setPrjId(cloudProject.getProjectId());
		cov.setCusId(cusId);
		cov.setCreateOrderDate(new Date());
		cov.setNetId(vpcId);
		cov.setSubnetId(msubnetId);
		cov.setBuyFloatIp(ApiInstanceConstant.BUY_FLOATIP_YES.equals(isBuyFloatIp) ? "1" : "0");
		if(ApiInstanceConstant.IMAGE_TYPE_PUBLILC.equals(imageType)){
			cov.setImageType("publicImage");
			if (ConstantClazz.DICT_CLOUD_OS_WINDOWS_NODE_ID.equals(image.getOsType())) {
				cov.setDisk(60);
			} else {
				cov.setDisk(20);
			}
		}
		else if(ApiInstanceConstant.IMAGE_TYPE_PRIVATE.equals(imageType)){
			cov.setImageType("privateImage");
			cov.setDisk(image.getMinDisk().intValue());
		}
		else if(ApiInstanceConstant.IMAGE_TYPE_MARKET.equals(imageType)){
			cov.setImageType("marketImage");
			cov.setDisk(image.getSysdiskSize().intValue());
		}
		cov.setOsType(image.getOsType());
		cov.setSysType(image.getSysType());
		cov.setCpu(cpuValue);
		cov.setRam(ramValue);
		if (ConstantClazz.DICT_CLOUD_OS_WINDOWS_NODE_ID.equals(image.getOsType())) {
			cov.setUsername(ApiInstanceConstant.WINDOWS_USERNAME);
		} else {
			cov.setUsername(ApiInstanceConstant.LINUX_USERNAME);
		}
		cov.setImageId(imageId);
		cov.setPassword(password);
		cov.setSgId(securityGroupApiService.querySecurityGroupByDefaultAndPrjId(cov.getPrjId(), securityGroupName));
		if(!StringUtil.isEmpty(payduration)){
			cov.setBuyCycle(Integer.parseInt(payduration));
		}
		cov.setVmName(instanceName);
		cov.setOrderType(OrderType.NEW);
		cov.setPayType(ApiInstanceConstant.PAYTYPE_DYNAMIC.equals(payType) ? PayType.PAYAFTER : PayType.PAYBEFORE);
		cov.setNetName(network.getNetName());
		cov.setSubnetName(msubnetwork.getSubnetName());
		cov.setCidr(msubnetwork.getCidr());
		cov.setImageName(image.getImageName());
		try {
			JSONObject dc = null;
			dc = ApiUtil.getDcCodeById(dcId);
			cov.setDcName(dc.getString("dcName"));
		} catch (Exception e1) {
			e1.printStackTrace();
			throw ApiException.createApiException(e1.getMessage());
		}
		checkVmQuota(cov, true);

		checkAccountBalnce(cov);

		return true;
	}

	/**
	 * 将List<CloudSecurityGroup>对象转义为API返回的格式<br>
	 * 
	 * @author liuzhuangzhuang
	 * @param groupList
	 * @return
	 */
	private JSONObject[] escapeSecurityGroup(List<CloudSecurityGroup> groupList) {
		JSONObject[] datas = new JSONObject[groupList.size()];
		for (int i = 0; i < groupList.size(); i++) {
			CloudSecurityGroup securityGroup = groupList.get(i);
			JSONObject data = new JSONObject();
			data.put(ApiInstanceConstant.SecurityGroup.SECURITYGROUPID, securityGroup.getSgId());
			data.put(ApiInstanceConstant.SecurityGroup.SECURITYGROUPNAME, securityGroup.getSgName().equals("default")?"默认安全组":securityGroup.getSgName());
			data.put(ApiInstanceConstant.SecurityGroup.SECURITYGROUPREMARK, StringUtil.isEmpty(securityGroup.getSgDescription()) ? "":securityGroup.getSgDescription());
			//0为默认安全组，1为自定义安全组
			data.put(ApiInstanceConstant.SecurityGroup.ISDEFAULT, securityGroup.getSgName().toLowerCase().equals("default")?0:1);
			data.put(ApiInstanceConstant.Instance.REGION, securityGroup.getDcId());
			// 创建时间,格式为YYYY-MM-DDThh:mm:ssZ，为UTC时间，需要将其转化为 UTC+0 的时间。
			data.put(ApiInstanceConstant.Instance.CREATETIME, DateUtil.getUTCDateZ(securityGroup.getCreateTime()));
			// 根据安全组ID获取相关的安全组规则信息
			List<CloudSecurityGroupRule> groupRuleList = securityGroupApiService
					.getGroupRuleListByGroupId(securityGroup.getSgId());
			data.put(ApiInstanceConstant.SecurityGroup.RULECOUNT, groupRuleList == null ? 0 : groupRuleList.size());
			// 获取加入该安全组的云主机ID
			List<BaseCloudVmSgroup> vmSgroupList = securityGroupApiService.getVmSgroupByVmIdAndSecurityGropId(null, securityGroup.getSgId());
			if(vmSgroupList != null && vmSgroupList.size() > 0){
				String [] instanceIds = new String[vmSgroupList.size()];
				for(int j = 0; j<vmSgroupList.size(); j++)
					instanceIds[j] = vmSgroupList.get(j).getVmId();
				data.put(ApiInstanceConstant.SecurityGroup.ATTACHINSTANCEID, instanceIds);
			}else{
				data.put(ApiInstanceConstant.SecurityGroup.ATTACHINSTANCEID, new String[]{});
			}
			if(groupRuleList != null && groupRuleList.size()>0){
				data.put(ApiInstanceConstant.SecurityGroup.ATTACHRULESET, this.escapeSecurityGroupRule(groupRuleList));
			}else{
				data.put(ApiInstanceConstant.SecurityGroup.ATTACHRULESET, new String[]{});
			}
			datas[i] = data;
		}
		return datas;
	}

	/**
	 * 将List<CloudSecurityGroupRule>对象转义为API返回的格式<br>
	 * 
	 * @param groupRuleList
	 * @return
	 */
	private JSONObject[] escapeSecurityGroupRule(List<CloudSecurityGroupRule> groupRuleList) {
		JSONObject[] datas = new JSONObject[groupRuleList.size()];
		for (int i = 0; i < groupRuleList.size(); i++) {
			CloudSecurityGroupRule securityGroupRule = groupRuleList.get(i);
			JSONObject data = new JSONObject();
			data.put(ApiInstanceConstant.SecurityGroupRule.DIRECTION, securityGroupRule.getDirection());
			data.put(ApiInstanceConstant.SecurityGroupRule.ETHERTYPE, securityGroupRule.getEthertype());
			data.put(ApiInstanceConstant.SecurityGroupRule.IPPROTOCOl,
					StringUtil.isEmpty(securityGroupRule.getProtocol()) ? "全部"
							: securityGroupRule.getProtocol().toUpperCase().equals("ALL") ? "全部"
									: securityGroupRule.getProtocol());
			// 1. 端口或端口范围，例如：1 或者 1-65535
			// 2. 仅TCP和UDP协议时端口有值，其他协议时展示”-”表示不适用。
			if (securityGroupRule.getProtocol().toLowerCase().equals("tcp")
					|| securityGroupRule.getProtocol().toLowerCase().equals("udp")) {
				if (securityGroupRule.getPortRangeMin().equals(securityGroupRule.getPortRangeMax()))
					data.put(ApiInstanceConstant.SecurityGroupRule.PORTRANGE, securityGroupRule.getPortRangeMin());
				else
					data.put(ApiInstanceConstant.SecurityGroupRule.PORTRANGE,
							securityGroupRule.getPortRangeMin() + "-" + securityGroupRule.getPortRangeMax());
			} else {
				data.put(ApiInstanceConstant.SecurityGroupRule.PORTRANGE, "-");
			}

			if (securityGroupRule.getIcMp().equals("--")) {
				data.put(ApiInstanceConstant.SecurityGroupRule.ICMPTYPE, "-"); // 待定
				data.put(ApiInstanceConstant.SecurityGroupRule.ICMPCODE, "-"); // 待定
			} else {
				if (securityGroupRule.getIcMp().contains("/")) {
					String[] typeAndCode = securityGroupRule.getIcMp().split("/");
					data.put(ApiInstanceConstant.SecurityGroupRule.ICMPTYPE, typeAndCode[0]); // 待定
					data.put(ApiInstanceConstant.SecurityGroupRule.ICMPCODE, typeAndCode[1]); // 待定
				} else {
					data.put(ApiInstanceConstant.SecurityGroupRule.ICMPTYPE, "所有"); // 待定
					data.put(ApiInstanceConstant.SecurityGroupRule.ICMPCODE, "所有"); // 待定
				}
			}

			data.put(ApiInstanceConstant.SecurityGroupRule.SOURCE,
					!StringUtil.isEmpty(securityGroupRule.getRemoteGroupId()) ? securityGroupRule.getRemoteGroupId()
							: StringUtil.isEmpty(securityGroupRule.getRemoteIpPrefix()) ? "0.0.0.0/0(CIDR)"
									: securityGroupRule.getRemoteIpPrefix() + "(CIDR)");
			datas[i] = data;
		}
		return datas;
	}

	/**
	 * 校验 cpu 和 memory 是否在当前系统支持的范围<br>
	 * ----------------------------------------
	 * 
	 * @param cpu
	 *            待校验cpu
	 * @param memory
	 *            待校验memory
	 * 
	 * @return cpu和memory都支持返回true;否则返回 false;
	 * @throws ApiException 
	 */
	private boolean checkCpuAndMemory(String cpu, String memory) throws ApiException {
		boolean flag = false;
		
		if(!(cpu.matches("[1-9][0-9]*") && memory.matches("[1-9][0-9]*"))){
			throw ApiException.createApiException("110054");
		}
		
		Map<String, SysDataTree> cpuMap = new HashMap<String, SysDataTree>();
		List<SysDataTree> cpuList = DictUtil.getDataTreeByParentId(ConstantClazz.DICT_CLOUD_CPU_TYPE_NODE_ID);
		for (SysDataTree sdt : cpuList) {
			String nodeName = sdt.getNodeName();
			String cpuStr = nodeName.substring(0, nodeName.length() - 1);
			cpuMap.put(cpuStr, sdt);
		}
		if (cpuMap.containsKey(cpu)) {
			SysDataTree ramNode = cpuMap.get(cpu);
			List<SysDataTree> memoryList = DictUtil.getDataTreeByParentId(ramNode.getNodeId());
			Map<String, SysDataTree> memoryMap = new HashMap<String, SysDataTree>();
			for (SysDataTree sdt : memoryList) {
				String nodeName = sdt.getNodeName();
				String memoryStr = nodeName.substring(0, nodeName.length() - 2);
				memoryMap.put(memoryStr, sdt);
			}
			if (memoryMap.containsKey(memory)) {
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * 校验云主机密码格式是否正确<br>
	 * 1.密码位数8-30位<br>
	 * 2.包含0-9 a-z A-Z ~!@#$%^&*()_+`-=,。/:;'[]\<>?:"{}|<br>
	 * 3.且只是包含3种类型<br>
	 * 
	 * 同时满足以上3种情况，则返回true;否则返回false<br>
	 * -------------------------------------------------------
	 * 
	 * @author zhouhaitao
	 * @param password
	 * @return
	 * 
	 */
	private boolean checkVmPwd(String password) {
		boolean flag = false;

		int numTypeCount = 0;
		int lowerCharypeCount = 0;
		int upperCharTypeCount = 0;
		int specCharTypeCount = 0;

		if (Pattern.matches(ApiInstanceConstant.PASSWORD_REGEX, password)) {
			for (char c : password.toCharArray()) {
				String charStr = new String(new char[] { c });
				if (charStr.matches(ApiInstanceConstant.NUMBER_REGEX)) {
					numTypeCount = 1;
					continue;
				} else if (charStr.matches(ApiInstanceConstant.LOWER_REGEX)) {
					lowerCharypeCount = 1;
					continue;
				} else if (charStr.matches(ApiInstanceConstant.UPPER_REGEX)) {
					upperCharTypeCount = 1;
					continue;
				} else if (charStr.matches(ApiInstanceConstant.SPEC_CHAR_REGEX)) {
					specCharTypeCount = 1;
					continue;
				}
			}
		}
		flag = (numTypeCount + lowerCharypeCount + upperCharTypeCount + specCharTypeCount) >= 3;
		return flag;
	}

	/**
	 * 查询云主机配额信息<br>
	 * ------------------
	 * 
	 * @author zhouhaitao
	 * @param cov
	 *            云主机订单信息
	 * @param isBuy
	 *            是否是新增的，true 是新增；false是升级
	 * @return
	 * @throws ApiException
	 */
	private boolean checkVmQuota(CloudOrderVm cov, boolean isBuy) throws ApiException {
		CloudProject project = projectService.queryProjectQuotaAndUsedQuotaForVm(cov.getPrjId());
		if (isBuy) {
			if (cov.getCount() > (project.getHostCount() - project.getUsedVmCount())) {
				throw ApiException.createApiException("110036");
			}
			if (cov.getCount() > (project.getDiskCount() - project.getDiskCountUse())) {
				throw ApiException.createApiException("110040");
			}
			if ("1".equals(cov.getBuyFloatIp())
					&& (cov.getCount() > (project.getOuterIP() - project.getOuterIPUse()))) {
				throw ApiException.createApiException("110039");
			}
			if ((cov.getCount() * cov.getCpu()) > (project.getCpuCount() - project.getUsedCpuCount())) {
				throw ApiException.createApiException("110037");
			}
			if ((cov.getCount() * cov.getRam()) > (project.getMemory() - project.getUsedRam())) {
				throw ApiException.createApiException("110038");
			}
			if ((cov.getCount() * cov.getDisk()) > (project.getDiskCapacity() - project.getUsedDiskCapacity())) {
				throw ApiException.createApiException("110041");
			}
		} else {
			if ((cov.getCpu() - cov.getVmCpu()) > (project.getCpuCount() - project.getUsedCpuCount())) {
				throw ApiException.createApiException("110037");
			}
			if ((cov.getRam() - cov.getVmRam()) > (project.getMemory() - project.getUsedRam())) {
				throw ApiException.createApiException("110038");
			}
		}
		return true;
	}

	/**
	 * 校验云主机订单与账户余额<br>
	 * ---------------------------------
	 * 
	 * @author zhouhaitao
	 * @param cov
	 *            云主机订单信息
	 * @return
	 * @throws ApiException
	 * @throws Exception
	 */
	private boolean checkAccountBalnce(CloudOrderVm cov) throws ApiException {
		MoneyAccount accountMoney = new MoneyAccount();
		try {
			accountMoney = accountOverviewSerivce.getAccountInfo(cov.getCusId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (cov.getPayType().equals(PayType.PAYBEFORE)) {
			BigDecimal totalPayment = calcVmPrice(cov);
			String errCode = "110078";
			cov.setProdName(ApiInstanceConstant.PAYBEFORE_VM_PRODNAME);
			if (OrderType.UPGRADE.equals(cov.getOrderType())) {
				errCode = "110075";
				cov.setProdName(ApiInstanceConstant.UPGRADE_VM_PRODNAME);
			}
			if (accountMoney.getMoney().compareTo(totalPayment) < 0) {
				throw ApiException.createApiException(errCode);
			}
			cov.setPaymentAmount(totalPayment);
			cov.setAccountPayment(totalPayment);
			cov.setThirdPartPayment(BigDecimal.ZERO);
		} else if (PayType.PAYAFTER.equals(cov.getPayType())) {
			if (OrderType.UPGRADE.equals(cov.getOrderType())) {
				cov.setProdName(ApiInstanceConstant.UPGRADE_VM_PRODNAME);
				BigDecimal balance = BigDecimal.ZERO;
				if (accountMoney.getMoney().compareTo(balance) <= 0) {
					throw ApiException.createApiException("100017");
				}
			} else if (OrderType.NEW.equals(cov.getOrderType())) {
				cov.setProdName(ApiInstanceConstant.PAYAFTER_VM_PRODNAME);
				String buyCondition = sysDataTreeService.getBuyCondition();
				BigDecimal createResourceLimitedMoney = new BigDecimal(buyCondition);
				if (accountMoney.getMoney().compareTo(createResourceLimitedMoney) < 0) {
					throw ApiException.createApiException("110079");
				}
			}
		}
		return true;
	}

	/**
	 * 创建云主机订单<br>
	 * --------------------
	 * 
	 * @author zhouhaitao
	 * @param cov
	 * @param userId
	 * @return
	 * @throws ApiException 
	 */
	private Order createVmOrder(CloudOrderVm cov, String userId) throws ApiException {
		Order order = new Order();
		order.setOrderType(cov.getOrderType());
		order.setDcId(cov.getDcId());
		order.setProdCount(cov.getCount());
		order.setProdConfig(vmConfig(cov));
		order.setPayType(cov.getPayType());
		order.setResourceType(ResourceType.VM);
		order.setUserId(userId);
		order.setCusId(cov.getCusId());
		order.setProdName(cov.getProdName());

		if (PayType.PAYBEFORE.equals(cov.getPayType())) {
			cov.setPrice(cov.getPaymentAmount().divide(new BigDecimal(cov.getCount()), 2));
			if (OrderType.NEW.equals(cov.getOrderType())) {
				order.setBuyCycle(cov.getBuyCycle());
			} else if (OrderType.UPGRADE.equals(cov.getOrderType())) {
				order.setResourceExpireTime(cov.getEndTime());
			}
			order.setUnitPrice(cov.getPrice());
			order.setPaymentAmount(cov.getPaymentAmount());
			order.setAccountPayment(cov.getAccountPayment());
			order.setThirdPartPayment(cov.getThirdPartPayment());

		} else if (PayType.PAYAFTER.equals(cov.getPayType())) {
			order.setBillingCycle(BillingCycleType.HOUR);
		}
		try {
			orderService.createOrderForVmAPI(order);
		} catch (Exception e) {
			e.printStackTrace();
			throw ApiException.createApiException(ApiConstant.INTERNAL_ERROR_CODE);
		}
		return order;
	}

	/**
	 * 资源创建接口<br>
	 * --------------
	 * 
	 * @author zhouhaitao
	 * @param order
	 *            云主机订单信息
	 * @return
	 * 
	 * @throws AppException
	 * 
	 */
	@Transactional(noRollbackFor = AppException.class)
	public List<BaseCloudVm> createVm(CloudOrderVm order) throws AppException {
		int disStep = 0;
		List<Vm> vmList = new ArrayList<Vm>();
		List<CloudFloatIp> floatIpList = new ArrayList<CloudFloatIp>();
		BaseCloudFlavor cloudFlavor = new BaseCloudFlavor();
		List<BaseCloudVm> result = new ArrayList<BaseCloudVm>();
		try {
			if ("1".equals(order.getBuyFloatIp())) {
				floatIpList = floatIpApiService.createFloatIpByOrderno(order.getOrderNo());
			}

			cloudFlavor.setFlavorVcpus(order.getCpu());
			cloudFlavor.setFlavorRam(order.getRam());
			cloudFlavor.setFlavorDisk(order.getDisk());
			cloudFlavor.setDcId(order.getDcId());
			cloudFlavor.setPrjId(order.getPrjId());

			// 创建云主机类型
			cloudFlavorService.createFlavor(cloudFlavor);
			order.setFlavorId(cloudFlavor.getFlavorId());
			// （批量）创建云主机
			CloudVm cloudVm = new CloudVm();

			cloudVm.setDcId(order.getDcId());
			cloudVm.setPrjId(order.getPrjId());
			cloudVm.setVmName(order.getVmName());
			cloudVm.setNumber(order.getCount());
			cloudVm.setNetId(order.getNetId());
			cloudVm.setSubnetId(order.getSubnetId());
			cloudVm.setSelfSubnetId(order.getSelfSubnetId());
			cloudVm.setUsername(order.getUsername());
			cloudVm.setPassword(order.getPassword());
			cloudVm.setOsType(order.getOsType());
			cloudVm.setFromImageId(order.getImageId());
			cloudVm.setDisks(order.getDisk());
			cloudVm.setSgId(order.getSgId());

			String errors = openstackVmService.createVm(cloudVm, cloudFlavor.getFlavorId(), vmList);
			if(null !=vmList && vmList.size()>0){
				for(Vm vm:vmList){
					BaseCloudVm bcv = new BaseCloudVm();
					bcv.setVmId(vm.getId());
					result.add(bcv);
				}
			}
			if (!StringUtils.isEmpty(errors)) {
				throw new AppException("error.openstack.message", new String[] { errors });
			}
			disStep = 1;
			return result;
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new AppException("error.openstack.message");
		} finally {
			try {
				vmCreateCallback(order, disStep, cloudFlavor.getFlavorId(), floatIpList, vmList);
			} catch (AppException e) {
				throw e;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw new AppException("error.openstack.message");
			}
		}
	}

	/**
	 * 
	 * 处理批量创建云主机的处理 <br>
	 * ---------------------
	 * 
	 * @author zhouhaitao
	 * 
	 * @param order
	 *            云主机订单信息
	 * @param step
	 *            创建指针
	 * @param flavorId
	 *            云主机模板IDR
	 * @param floatIpList
	 *            公网IP创建返回信息
	 * @param vmList
	 *            云主机创建返回信息
	 * @throws Exception
	 */
	private void vmCreateCallback(CloudOrderVm order, int step, String flavorId, List<CloudFloatIp> floatIpList,
			List<Vm> vmList) throws Exception {
		List<BaseCloudVolume> volumeList = new ArrayList<BaseCloudVolume>();
		List<BaseCloudVm> succVmList = null;
		int a = 0;
		try {
			try {
				succVmList = saveVmAndVolume(order, floatIpList, vmList, flavorId, volumeList);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw new Exception(e.getMessage());
			}
			a = 1;
			if (step == 0) {
				createFailHandler(order, succVmList);
			} else if (step == 1) {
				vmSuccessHandler(order, floatIpList, succVmList, volumeList);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (a != 1) {
				createFailHandler(order, succVmList);
			}
			throw e;
		}
	}

	/**
	 * <p>
	 * 资源创建过程中失败处理
	 * </p>
	 * --------------------------
	 * 
	 * @author zhouhaitao
	 * @param order
	 *            订单信息
	 * @param floatIpList
	 *            已经创建成功的公网IP列表
	 * @param vmList
	 *            已经创建成功的主机列表
	 * 
	 * @throws Exception
	 */
	@Transactional(noRollbackFor = AppException.class)
	public void createFailHandler(CloudOrderVm order, List<BaseCloudVm> vmList) throws ApiException {
		try {
			// 调用创建资源创建失败的接口
			orderService.completeOrder(order.getOrderNo(), false, null);
			messageCenterService.addResourFailMessage(order.getOrderNo(), order.getCusId());
			if ("1".equals(order.getBuyFloatIp())) {
				floatIpApiService.releaseFloatIpByOrderNo(order.getOrderNo());
			}
			if (null != vmList && vmList.size() > 0) {
				vmFailedHandler(order);
			}
		} catch (Exception e) {
			deleteFailedHandler(order);
			log.error(e.getMessage(), e);
			throw ApiException.createApiException(ApiConstant.INTERNAL_ERROR_CODE);
		}
	}

	/**
	 * <p>
	 * 删除失败订单中 创建成功的云主机
	 * </p>
	 * -------------------------
	 * 
	 * @author zhouhaitao
	 * @param vmList
	 *            已经创建成功的云主机列表
	 */
	private void vmFailedHandler(CloudOrderVm orderVm) {
		List<CloudVm> vmList = queryVmListByOrder(orderVm.getOrderNo());
		if (null != vmList && vmList.size() > 0) {
			for (CloudVm vm : vmList) {
				deleteVmForCreateFailed(vm, orderVm.getCreateUser());

				CloudBatchResource cloudBatchResource = new CloudBatchResource();

				cloudBatchResource.setOrderNo(orderVm.getOrderNo());
				cloudBatchResource.setResourceId(vm.getVmId());

				cloudBatchResourceService.delete(cloudBatchResource);

				vmSgService.deleteByVmId(vm.getVmId());
			}
		}
	}

	/**
	 * 保存创建成功的云主机和云硬盘并进行同步
	 * 
	 * @author zhouhaitao
	 * 
	 * @param order
	 *            订单信息
	 * @param floatIpList
	 *            创建成功的公网IP列表
	 * @param vmList
	 *            创建成功的云主机列表
	 * @param volumeList
	 *            创建成功的云硬盘列表
	 * @throws Exception
	 */
	private void vmSuccessHandler(CloudOrderVm order, List<CloudFloatIp> floatIpList, List<BaseCloudVm> vmList,
			List<BaseCloudVolume> volumeList) throws Exception {
		int successCount = 0;
		if (null != vmList && vmList.size() > 0) {
			for (int i = 0; i < vmList.size(); i++) {
				BaseCloudVm tempVm = vmList.get(i);
				if ("ERROR".equals(tempVm.getVmStatus())) {
					createFailHandler(order, vmList);
					return;
				}
				if ("ACTIVE".equals(tempVm.getVmStatus())) {
					if ("1".equals(order.getBuyFloatIp())) {
						CloudFloatIp floatIp = floatIpList.get(i);
						floatIp.setResourceType("vm");
						floatIp.setResourceId(tempVm.getVmId());
						floatIp.setDcId(order.getDcId());
						floatIp.setPrjId(order.getPrjId());
						floatIp.setVmIp(tempVm.getVmIp());

						floatIpApiService.bindResource(floatIp);
					}

					if (!StringUtils.isEmpty(order.getSelfSubnetId())) {
						InterfaceAttachment interAtta = openstackVmService.bindPort(order.getDcId(), order.getPrjId(),
								tempVm.getVmId(), order.getNetId(), order.getSelfSubnetId(),
								new String[] { order.getSgId() });
						tempVm.setSelfIp(interAtta.getFixed_ips()[0].getIp_address());
						tempVm.setSelfPortId(interAtta.getPort_id());
						cloudVmDao.saveOrUpdate(tempVm);
					}
					successCount++;
				}
			}

			if (successCount != order.getCount()) {
				JSONObject json = new JSONObject();
				json.put("orderNo", order.getOrderNo());
				json.put("dcId", order.getDcId());
				json.put("prjId", order.getPrjId());
				json.put("vmName", order.getVmName());
				json.put("vmStatus", "BUILD");
				json.put("count", "0");
				json.put("flavorId", order.getFlavorId());

				json.put("number", order.getCount());
				json.put("buyFloatIp", order.getBuyFloatIp());
				json.put("netId", order.getNetId());
				json.put("selfSubnetId", order.getSelfSubnetId());
				json.put("createName", order.getCreateUser());
				json.put("createTime", order.getCreateOrderDate());
				json.put("osType", order.getOsType());
				json.put("sysType", order.getSysType());
				json.put("vmFrom", order.getImageType());
				json.put("fromImageId", order.getImageId());
				json.put("sgId", order.getSgId());
				json.put("cusId", order.getCusId());
				json.put("cpus", order.getCpu());
				json.put("rams", order.getRam());
				json.put("disks", order.getDisk());
				json.put("payType", order.getPayType());

				final JSONObject data = json;
				TransactionHookUtil.registAfterCommitHook(new Hook() {
					@Override
					public void execute() {
						try {
							jedisUtil.push(RedisKey.vmKey, data.toJSONString());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				});
			} else {
				allVmSuccessHnadler(order, floatIpList, vmList);
			}
		}

		if (null != volumeList && volumeList.size() > 0) {
			for (BaseCloudVolume vol : volumeList) {

				JSONObject json = new JSONObject();
				json.put("volId", vol.getVolId());
				json.put("dcId", vol.getDcId());
				json.put("prjId", vol.getPrjId());
				json.put("volStatus", vol.getVolStatus());
				json.put("volBootable", "1");
				json.put("count", "0");
				final JSONObject data = json;
				TransactionHookUtil.registAfterCommitHook(new Hook() {
					@Override
					public void execute() {
						try {
							jedisUtil.push(RedisKey.volKey, data.toJSONString());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		}
	}

	public void deleteVmForCreateFailed(CloudVm cloudVm, String userName) throws AppException {
		try {
			JSONObject stackVm = openstackVmService.get(cloudVm.getDcId(), cloudVm.getPrjId(), cloudVm.getVmId());
			System.out.println(stackVm.toJSONString());
			if ("ERROR".equalsIgnoreCase(stackVm.getString("status"))) {
				openstackVmService.softDeleteVm(cloudVm);
			} else {
				openstackVmService.forceDelete(cloudVm);
			}

			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			vm.setDeleteUser(userName);
			vm.setDeleteTime(new Date());
			vm.setVmStatus("DELETING");

			cloudVmDao.merge(vm);

			volumeApiService.deleteVolumeByVm(cloudVm.getVmId(), userName);

			JSONObject json = new JSONObject();
			json.put("vmId", vm.getVmId());
			json.put("dcId", vm.getDcId());
			json.put("prjId", vm.getPrjId());
			json.put("vmStatus", vm.getVmStatus());
			json.put("count", "0");

			final JSONObject data = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.vmKey, data.toJSONString());
				}

			});
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}
	}

	/**
	 * 
	 * 订单资源创建成功处理
	 * 
	 * @author zhouhaitao
	 * 
	 * @param orderVm
	 *            订单信息
	 * @param floatIpList
	 *            创建成功的公网IP列表
	 * @param vmList
	 *            创建成功的云主机列表
	 * @throws Exception
	 */
	public void allVmSuccessHnadler(CloudOrderVm orderVm, List<CloudFloatIp> floatIpList, List<BaseCloudVm> vmList)
			throws Exception {
		JSONObject json = new JSONObject();
		List<String> floatIds = new ArrayList<String>();
		List<String> vmIds = new ArrayList<String>();
		// 发送订单资源创建成功,返回订单完成时间
		List<BaseOrderResource> resourceList = new ArrayList<BaseOrderResource>();
		for (CloudFloatIp ip : floatIpList) {
			BaseOrderResource resource = new BaseOrderResource();
			resource.setOrderNo(orderVm.getOrderNo());
			resource.setResourceId(ip.getFloId());
			resource.setResourceName(ip.getFloIp());

			resourceList.add(resource);
		}
		for (BaseCloudVm vm : vmList) {
			BaseOrderResource resource = new BaseOrderResource();
			resource.setOrderNo(orderVm.getOrderNo());
			resource.setResourceId(vm.getVmId());
			resource.setResourceName(vm.getVmName());

			resourceList.add(resource);
		}
		BaseOrder order = orderService.completeOrder(orderVm.getOrderNo(), true, resourceList);
		orderVm.setOrderCompleteDate(order.getCompleteTime());
		Date completeDate = null;
		if (PayType.PAYBEFORE.equals(orderVm.getPayType())) {
			completeDate = order.getResourceExpireTime();
		} else if (PayType.PAYAFTER.equals(orderVm.getPayType())) {
			floatIpApiService.sendMessage(floatIpList, EayunQueueConstant.QUEUE_BILL_RESOURCE_PURCHASE,
					orderVm.getCusId(), orderVm.getOrderNo());
		}

		for (CloudFloatIp floatIp : floatIpList) {
			floatIds.add(floatIp.getFloId());
		}

		for (BaseCloudVm vm : vmList) {
			vmIds.add(vm.getVmId());
			if (PayType.PAYAFTER.equals(orderVm.getPayType())) {
				vmPurchaseCharge(orderVm, vm);
			}
		}

		if (floatIds.size() > 0) {
			json.put("floatIp", floatIds);
		}
		if (vmIds.size() > 0) {
			json.put("vm", vmIds);
		}
		cloudOrderVmService.updateOrderResources(orderVm.getOrderNo(), json.toJSONString());

		cloudOrderVmService.modifyResourceForVisable(vmIds, floatIds, completeDate);

		cloudBatchResourceService.deleteByOrder(orderVm.getOrderNo());
	}

	/**
	 * 校验调整云主机配置API的参数<br>
	 * -----------------------------------
	 * 
	 * @author zhouhaitao
	 * @param json
	 * @param cvo
	 * @return
	 * @throws ApiException
	 */
	private boolean validateResizeInstanceData(JSONObject json, CloudOrderVm cov) throws ApiException {
		String dcId = json.getString(ApiInstanceConstant.Instance.DCID);
		String cusId = json.getString(ApiInstanceConstant.Instance.CUSID);
		String vmId = json.getString(ApiInstanceConstant.Instance.INSTANCEID);
		String cpu = json.getString(ApiInstanceConstant.Instance.CPU);
		String memory = json.getString(ApiInstanceConstant.Instance.MEMORY);

		CloudProject cloudProject = validateProject(dcId, cusId);
		
		CloudVm cloudVm = validateResizeVm(vmId, cloudProject.getProjectId());
		
		if(null == cpu){
			cpu = cloudVm.getCpus()+"";
		}
		if(null == memory){
			memory = cloudVm.getRams()/1024+"";
		}
		
		if (!checkCpuAndMemory(cpu, memory)) {
			throw ApiException.createApiException("110018");
		}

		int cpuValue = Integer.parseInt(cpu);
		int ramValue = 1024 * Integer.parseInt(memory);

		if (PayType.PAYBEFORE.equals(cloudVm.getPayType()) && 
				(cpuValue < cloudVm.getCpus() || ramValue < cloudVm.getRams())) {
			throw ApiException.createApiException("110055");
		}
		if(cpuValue == cloudVm.getCpus() && ramValue == cloudVm.getRams()){
			throw ApiException.createApiException("110084");
		}
		
		CloudImage image = getImageById(cloudVm.getFromImageId());
		if (image.getMinCpu() > cpuValue || image.getMinRam() > ramValue) {
			throw ApiException.createApiException("110020");
		}
		if ((image.getMaxCpu() != 0 && image.getMaxCpu() < cpuValue)
				|| (image.getMaxRam() != 0 && image.getMaxRam() < ramValue)) {
			throw ApiException.createApiException("110019");
		}
		if (checkSavingSnapshot(cloudVm)) {
			throw ApiException.createApiException("110082");
		}
		if (checkVmOrderExsit(cloudVm.getVmId(),false,true)) {
			throw ApiException.createApiException("110083");
		}
		if (checkVmOrderExsit(cloudVm.getVmId(),true,false)) {
			throw ApiException.createApiException("110072");
		}

		cov.setDcId(dcId);
		cov.setPrjId(cloudProject.getProjectId());
		cov.setCusId(cusId);
		cov.setVmId(vmId);
		cov.setVmName(cloudVm.getVmName());
		cov.setSysType(cloudVm.getSysType());
		try {
			cov.setDcName(ApiUtil.getDcCodeById(cloudVm.getDcId()).getString("dcName"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		cov.setCpu(cpuValue);
		cov.setRam(ramValue);
		cov.setDisk(cloudVm.getDisks());
		cov.setVmCpu(cloudVm.getCpus());
		cov.setVmRam(cloudVm.getRams());
		cov.setOrderType(OrderType.UPGRADE);
		cov.setPayType(cloudVm.getPayType());
		cov.setCycleCount(cloudVm.getCycleCount());
		cov.setCreateOrderDate(new Date());
		cov.setProdName(ApiInstanceConstant.UPGRADE_VM_PRODNAME);
		cov.setEndTime(cloudVm.getEndTime());
		cov.setCount(1);

		checkVmQuota(cov, false);

		checkAccountBalnce(cov);

		return true;
	}

	/**
	 * 校验项目是否存在，攒在返回 project ，不存在抛出对应的异常<br>
	 * -----------------------------------------------------
	 * 
	 * @author zhouhaitao
	 * @param dcId
	 * @param cusId
	 * @return
	 * @throws ApiException
	 */
	private CloudProject validateProject(String dcId, String cusId) throws ApiException {
		CloudProject cloudProject = projectService.queryProjectByDcAndCus(dcId, cusId);
		if (null == cloudProject) {
			throw ApiException.createApiException("100030");
		}
		return cloudProject;
	}

	/**
	 * 判断云主机是否存在,适用于所有非删除操作的云主机API ---------------------------
	 * 
	 * @author zhouhaitao
	 * @param vmId
	 *            云主机ID
	 * @param prjId
	 *            项目ID
	 * @return 不存在 抛出对应的异常，存在则返回CloudVm对象
	 * 
	 */
	private CloudVm validateResizeVm(String vmId, String prjId) throws ApiException {
		/**
		 * 1. 判断云主机是够在属于该项目 2. 校验云主机的状态是否处于中间状态 3. 校验云主机的计费状态是允许操作
		 */
		if (StringUtil.isEmpty(vmId)) {
			throw ApiException.createApiException("110042");
		}
		if (!InstanceApiUtil.uuidRegex(vmId, true)) {
			throw ApiException.createApiException("110044");
		}
		CloudVm cloudVm = queryVmById(vmId, prjId);
		if (null == cloudVm) {
			throw ApiException.createApiException("110043");
		}
		if (CloudResourceUtil.CLOUD_CHARGESTATE_NSF_CODE.equals(cloudVm.getChargeState())) {
			throw ApiException.createApiException("100017");
		}
		if (CloudResourceUtil.CLOUD_CHARGESTATE_EXPIRED_CODE.equals(cloudVm.getChargeState())
				|| CloudResourceUtil.CLOUD_CHARGESTATE_EXPIRED_ED_CODE.equals(cloudVm.getChargeState())) {
			throw ApiException.createApiException("100019");
		}
		if(ApiInstanceConstant.InstanceStatus.ERROR.equals(cloudVm.getVmStatus())){
			throw ApiException.createApiException("110074");
		}
		if("2".equals(cloudVm.getIsDeleted())){
			throw ApiException.createApiException("110069");
		}
		if (!ApiInstanceConstant.InstanceStatus.SHUTOFF.getStatus().equals(cloudVm.getVmStatus())) {
			throw ApiException.createApiException("110056");
		}
		return cloudVm;
	}

	/**
	 * 调整云主机大小 ------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * @throws ApiException 
	 * @throws Exception
	 */
	@Transactional(noRollbackFor = AppException.class)
	public void resizeVm(CloudVm cloudVm) throws ApiException {
		BaseCloudFlavor cloudFlavor = new BaseCloudFlavor();
		try {
			cloudFlavor.setFlavorVcpus(cloudVm.getCpus());
			cloudFlavor.setFlavorRam(cloudVm.getRams());
			cloudFlavor.setFlavorDisk(cloudVm.getDisks());
			cloudFlavor.setDcId(cloudVm.getDcId());
			cloudFlavor.setPrjId(cloudVm.getPrjId());

			// 创建云主机类型
			cloudFlavorService.createFlavor(cloudFlavor);
			cloudVm.setResizeId(cloudFlavor.getFlavorId());
			openstackVmService.resizeVm(cloudVm);

			BaseCloudVm vm = new BaseCloudVm();

			vm = cloudVmDao.findOne(cloudVm.getVmId());
			cloudVm.setEndTime(vm.getEndTime());
			vm.setVmStatus("RESIZE");
			vm.setResizeId(cloudFlavor.getFlavorId());

			cloudVmDao.saveOrUpdate(vm);

			JSONObject json = new JSONObject();
			json.put("vmId", vm.getVmId());
			json.put("vmName", vm.getVmName());
			json.put("dcId", vm.getDcId());
			json.put("prjId", vm.getPrjId());
			json.put("vmStatus", vm.getVmStatus());
			json.put("orderNo", cloudVm.getOrderNo());
			json.put("cusId", cloudVm.getCusId());
			json.put("payType", cloudVm.getPayType());
			json.put("cpus", cloudVm.getCpus());
			json.put("rams", cloudVm.getRams());
			json.put("disks", cloudVm.getDisks());
			json.put("sysType", cloudVm.getSysType());
			json.put("endTime", cloudVm.getEndTime());
			json.put("count", "0");
			json.put("isExsit", "1");

			final JSONObject data = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.vmKey, data.toJSONString());
				}
			});
		} catch (AppException e) {
			upgradeFailHandler(cloudVm);
			throw e;
		}
	}

	/**
	 * 云主机升级失败处理
	 * 
	 * @param cloudVm
	 * @throws ApiException 
	 * @throws Exception
	 */
	public void upgradeFailHandler(CloudVm cloudVm) throws ApiException {
		try {
			orderService.completeOrder(cloudVm.getOrderNo(), false, null);
			messageCenterService.addResourFailMessage(cloudVm.getOrderNo(), cloudVm.getCusId());
		} catch (Exception e) {
			e.printStackTrace();
			throw ApiException.createApiException(ApiConstant.INTERNAL_ERROR_CODE);
		}
	}
}
