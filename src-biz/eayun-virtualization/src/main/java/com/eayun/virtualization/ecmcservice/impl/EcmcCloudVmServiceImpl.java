package com.eayun.virtualization.ecmcservice.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.charge.model.ChargeRecord;
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.EayunQueueConstant;
import com.eayun.common.constant.PayType;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.tools.OrderGenerator;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.CloudResourceUtil;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.Hook;
import com.eayun.costcenter.model.MoneyAccount;
import com.eayun.costcenter.service.AccountOverviewService;
import com.eayun.eayunstack.model.Image;
import com.eayun.eayunstack.model.InterfaceAttachment;
import com.eayun.eayunstack.service.OpenstackVmService;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.monitor.bean.MonitorAlarmUtil.MonitorResourceType;
import com.eayun.monitor.ecmcservice.EcmcAlarmService;
import com.eayun.monitor.service.AlarmService;
import com.eayun.project.service.ProjectService;
import com.eayun.sys.model.SysDataTree;
import com.eayun.syssetup.service.SysDataTreeService;
import com.eayun.virtualization.baseservice.BaseVmService;
import com.eayun.virtualization.dao.CloudImageDao;
import com.eayun.virtualization.dao.CloudSecurityGroupDao;
import com.eayun.virtualization.dao.CloudSubNetWorkDao;
import com.eayun.virtualization.dao.CloudVmDao;
import com.eayun.virtualization.dao.CloudVmSecurityGroupDao;
import com.eayun.virtualization.ecmcservice.EcmcCloudFlavorService;
import com.eayun.virtualization.ecmcservice.EcmcCloudFloatIPService;
import com.eayun.virtualization.ecmcservice.EcmcCloudVmService;
import com.eayun.virtualization.ecmcservice.EcmcCloudVolumeService;
import com.eayun.virtualization.ecmcservice.EcmcLBMemberService;
import com.eayun.virtualization.ecmcservice.EcmcPortMappingService;
import com.eayun.virtualization.model.BaseCloudFlavor;
import com.eayun.virtualization.model.BaseCloudFloatIp;
import com.eayun.virtualization.model.BaseCloudImage;
import com.eayun.virtualization.model.BaseCloudPortMapping;
import com.eayun.virtualization.model.BaseCloudSecurityGroup;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.BaseCloudVm;
import com.eayun.virtualization.model.BaseCloudVmSgroup;
import com.eayun.virtualization.model.CloudFloatIp;
import com.eayun.virtualization.model.CloudImage;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.model.CloudSecurityGroup;
import com.eayun.virtualization.model.CloudSubNetWork;
import com.eayun.virtualization.model.CloudVm;
import com.eayun.virtualization.service.SecretkeyVmService;
import com.eayun.virtualization.service.TagService;

@Service
@Transactional
public class EcmcCloudVmServiceImpl extends BaseVmService implements EcmcCloudVmService {
	
	private static final Logger log = LoggerFactory.getLogger(EcmcCloudVmServiceImpl.class);
	
	@Autowired
	private OpenstackVmService openstackVmService;
	
	@Autowired
	private CloudVmDao cloudVmDao;
	
	@Autowired
	private CloudSubNetWorkDao cloudWorkDao;
	
	@Autowired
	private CloudSecurityGroupDao cloudSecurityGroupDao;
	
	@Autowired
	private CloudVmSecurityGroupDao cloudVmSecurityGroupDao;
	
	@Autowired
	private CloudImageDao cloudImageDao;
	
	@Autowired
	private TagService tagService;
	
	@Autowired
	private JedisUtil jedisUtil;
	
	@Autowired
	private AlarmService alarmService;
	
	@Autowired
    private EcmcAlarmService ecmcAlarmService;
	
	@Autowired
    private EcmcLBMemberService ecmcLBMemberService;

	@Autowired
	private EcmcCloudVolumeService ecmcCloudVolumeService;
	
	@Autowired
	private EcmcCloudFlavorService ecmcCloudFlavorService;
	
	@Autowired
	private EcmcCloudFloatIPService ecmcCloudFloatIPService;
	
	@Autowired
	private EayunRabbitTemplate rabbitTemplate;
	
	@Autowired
	private EcmcPortMappingService ecmcPortMappingService;
	
	@Autowired
	private SysDataTreeService sdtService;
	
	@Autowired
	private AccountOverviewService accountOverviewSerivce;
	
	@Autowired
	private ProjectService projectService;
	
	@Autowired
	private SecretkeyVmService secretkeyVmService;

	/**
	 * 维护云主机与安全组之间的关系表
	 * 把默认的default安全组放到关联表中
	 * @Author: duanbinbin
	 * @param cloudvm
	 *<li>Date: 2016年4月26日</li>
	 */
	@SuppressWarnings("unchecked")
	public void relateVmWithSecurityGroup(BaseCloudVm cloudvm){
		List<Object> list = new ArrayList<Object>();
		StringBuffer hql= new StringBuffer();
		hql.append(" from BaseCloudSecurityGroup  ");
		hql.append(" where dcId = ?  ");
		list.add(cloudvm.getDcId());
		hql.append(" and prjId = ?  ");
		list.add(cloudvm.getPrjId());
		hql.append(" and sgName = ?  ");
		list.add("default");
		
		List<BaseCloudSecurityGroup> sgList = cloudSecurityGroupDao.find(hql.toString(), list.toArray());
		if(null!=sgList&&sgList.size()==1){
			BaseCloudVmSgroup vsg=new BaseCloudVmSgroup();
			vsg.setSgId(sgList.get(0).getSgId());
			vsg.setVmId(cloudvm.getVmId());
			
			cloudVmSecurityGroupDao.saveOrUpdate(vsg);
		}
	}
	

	/**
	 * 打开云主机控制台
	 * @Author: duanbinbin
	 * @param cloudVm
	 * @return
	 *<li>Date: 2016年4月26日</li>
	 */
	@Override
	public String ConsoleUrl(CloudVm cloudVm) throws AppException {
		log.info("打开云主机控制台");
		String url = "" ; 
		try{
			url = openstackVmService.consoleVm(cloudVm);
		}catch(AppException e){
			throw e;
		}catch(Exception e){
			throw new AppException ("error.openstack.message");
		}
		return url;
	}

	/**
	 * 升级配置
	 * @Author: duanbinbin
	 * @param cloudVm
	 *<li>Date: 2016年4月26日</li>
	 */
	@Override
	public void upgradeVm(CloudVm cloudVm) throws AppException {
		log.info("升级配置");
		BaseCloudFlavor cloudFlavor =new BaseCloudFlavor();
		try{
			String no=OrderGenerator.newOrder();
			cloudFlavor.setFlavorName("temp"+no);
			cloudFlavor.setFlavorVcpus(cloudVm.getCpus());
			cloudFlavor.setFlavorRam(cloudVm.getRams()*1024);
			cloudFlavor.setFlavorDisk(cloudVm.getDisks());
			cloudFlavor.setDcId(cloudVm.getDcId());
			cloudFlavor.setPrjId(cloudVm.getPrjId());

			//创建云主机类型
			ecmcCloudFlavorService.createFlavor(cloudFlavor);
			cloudVm.setResizeId(cloudFlavor.getFlavorId());
			openstackVmService.resizeVm(cloudVm);
			
			BaseCloudVm vm = new BaseCloudVm();
			
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			
			vm.setVmStatus("RESIZE");
			vm.setResizeId(cloudFlavor.getFlavorId());
			
			cloudVmDao.merge(vm);
			
			JSONObject json =new JSONObject();
			json.put("vmId", vm.getVmId());
			json.put("dcId",vm.getDcId());
			json.put("prjId", vm.getPrjId());
			json.put("vmStatus", vm.getVmStatus());
			json.put("count", "0");
			json.put("isExsit", "1");
			
			final JSONObject data = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.vmKey, data.toJSONString());
				}
				
			});
			
		}catch(AppException e){
			throw e;
		}catch(Exception e){
			throw new AppException ("error.openstack.message");
		}
	}

	/**
	 * 关闭云主机
	 * @Author: duanbinbin
	 * @param cloudVm
	 *<li>Date: 2016年4月26日</li>
	 */
	@Override
	public void shutdownVm(CloudVm cloudVm) throws AppException {
		log.info("关闭云主机");
		try{
			openstackVmService.shutdownVm(cloudVm);
			
			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			vm.setVmStatus("SHUTOFFING");//关机中
			
			cloudVmDao.merge(vm);
			
			JSONObject json =new JSONObject();
			json.put("vmId", vm.getVmId());
			json.put("dcId",vm.getDcId());
			json.put("prjId", vm.getPrjId());
			json.put("vmStatus", vm.getVmStatus());
			json.put("perStatus", "ACTIVE");//运行中
			json.put("count", "0");
			json.put("isExsit", "0");
			
			final JSONObject data = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.vmKey, data.toJSONString());
				}
				
			});
			
		}catch(AppException e){
			throw e;
		}catch(Exception e){
			throw new AppException ("error.openstack.message");
		}
		
	}

	/**
	 * 启动云主机
	 * @Author: duanbinbin
	 * @param cloudVm
	 *<li>Date: 2016年4月26日</li>
	 */
	@Override
	public void startVm(CloudVm cloudVm) throws AppException {
		log.info("启动云主机");
		try{
			openstackVmService.restartVm(cloudVm);
			
			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			vm.setVmStatus("STARTING");//启动中
			
			cloudVmDao.merge(vm);
			
			JSONObject json =new JSONObject();
			json.put("vmId", vm.getVmId());
			json.put("dcId",vm.getDcId());
			json.put("prjId", vm.getPrjId());
			json.put("vmStatus", vm.getVmStatus());
			json.put("perStatus", "SHUTOFF");//已关机
			json.put("count", "0");
			json.put("isExsit", "0");
			
			
			final JSONObject data = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.vmKey, data.toJSONString());
				}
				
			});
			
		}catch(AppException e){
			throw e;
		}catch(Exception e){
			throw new AppException ("error.openstack.message");
		}
		
	}

	/**
	 * 重启云主机
	 * 即原来的软重启
	 * 硬重启不要了
	 * @Author: duanbinbin
	 * @param cloudVm
	 *<li>Date: 2016年4月26日</li>
	 */
	@Override
	public void restartVm(CloudVm cloudVm) throws AppException {
		log.info("重启云主机");
		try{
			openstackVmService.softRestartVm(cloudVm);
			
			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			vm.setVmStatus("REBOOT");//重启中
			
			cloudVmDao.merge(vm);
			
			JSONObject json =new JSONObject();
			json.put("vmId", vm.getVmId());
			json.put("dcId",vm.getDcId());
			json.put("prjId", vm.getPrjId());
			json.put("vmStatus", vm.getVmStatus());
			json.put("count", "0");
			json.put("isExsit", "1");
			
			final JSONObject data = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.vmKey, data.toJSONString());
				}
				
			});
			
		}catch(AppException e){
			throw e;
		}catch(Exception e){
			throw new AppException ("error.openstack.message");
		}
	}

	/**
	 * 挂起云主机
	 * @Author: duanbinbin
	 * @param cloudVm
	 *<li>Date: 2016年4月26日</li>
	 */
	@Override
	public void suspendVm(CloudVm cloudVm) throws AppException {
		log.info("挂起云主机");
		try{
			openstackVmService.suspendVm(cloudVm);
			
			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			vm.setVmStatus("SUSPENDEDING");//挂起中
			
			cloudVmDao.merge(vm);
			
			JSONObject json =new JSONObject();
			json.put("vmId", vm.getVmId());
			json.put("dcId",vm.getDcId());
			json.put("prjId", vm.getPrjId());
			json.put("vmStatus", vm.getVmStatus());
			json.put("perStatus", "ACTIVE");//运行中
			json.put("count", "0");
			json.put("isExsit", "0");
			
			final JSONObject data = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.vmKey, data.toJSONString());
				}
				
			});
			
		}catch(AppException e){
			throw e;
		}catch(Exception e){
			throw new AppException ("error.openstack.message");
		}
	}

	/**
	 * 恢复云主机
	 * @Author: duanbinbin
	 * @param cloudVm
	 *<li>Date: 2016年4月26日</li>
	 */
	@Override
	public void resumeVm(CloudVm cloudVm) throws AppException {
		log.info("恢复云主机");
		try{
			openstackVmService.resumeVm(cloudVm);
			
			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			vm.setVmStatus("RESUMING");//恢复中
			
			cloudVmDao.merge(vm);
			
			
			JSONObject json =new JSONObject();
			json.put("vmId", vm.getVmId());
			json.put("dcId",vm.getDcId());
			json.put("prjId", vm.getPrjId());
			json.put("vmStatus", vm.getVmStatus());
			json.put("perStatus", "SUSPENDED");//挂起
			json.put("count", "0");
			json.put("isExsit", "0");
			
			final JSONObject data = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.vmKey, data.toJSONString());
				}
				
			});
			
		}catch(AppException e){
			throw e;
		}catch(Exception e){
			throw new AppException ("error.openstack.message");
		}
		
	}

	/**
	 * 创建自定义镜像
	 * @Author: duanbinbin
	 * @param sysUser
	 * @param cloudVm
	 *<li>Date: 2016年4月26日</li>
	 */
	@Override
	public void createSnapshot(BaseEcmcSysUser sysUser, CloudVm cloudVm) throws AppException {
		log.info("创建自定义镜像");
		try{
			CloudProject project = projectService.findProject(cloudVm.getPrjId());
			boolean flag = checkImageCount(cloudVm.getPrjId(), project.getImageCount());
			if (flag) {
				throw new AppException("最多可创建"+project.getImageCount()+"个自定义镜像，请删除不需要的镜像再操作");
			}
			Image image=openstackVmService.createSnapshot(cloudVm);
			
			BaseCloudImage cloudImage = new BaseCloudImage();
			BaseCloudVm vm = cloudVmDao.findOne(cloudVm.getVmId());
			
			String imageId = vm.getFromImageId();
			if(null != imageId){
				CloudImage baseImage = getImageById(imageId);
				if(null != baseImage){
					if('1'==baseImage.getImageIspublic() || '3'==baseImage.getImageIspublic()){
						cloudImage.setMaxCpu(baseImage.getMaxCpu());
						cloudImage.setMaxRam(baseImage.getMaxRam());
						cloudImage.setSourceId(baseImage.getImageId());
					}
					else if('2'==baseImage.getImageIspublic()){
						cloudImage.setMaxCpu(baseImage.getSourceMaxCpu());
						cloudImage.setMaxRam(baseImage.getSourceMaxRam());
						cloudImage.setSourceId(baseImage.getSourceId());
					}
				}
			}

			cloudImage.setFromVmId(cloudVm.getVmId());
			cloudImage.setImageId(image.getId());
			cloudImage.setImageName(cloudVm.getImageName());
			cloudImage.setDcId(cloudVm.getDcId());
			cloudImage.setPrjId(cloudVm.getPrjId());
			cloudImage.setDiskFormat(image.getDisk_format());
			cloudImage.setOwnerId(image.getOwner());
			cloudImage.setOsType(vm.getOsType());
			cloudImage.setSysType(vm.getSysType());
			cloudImage.setMinCpu((long)cloudVm.getCpus());
			cloudImage.setImageIspublic('2');	//1:公共。2：自定义
			cloudImage.setCreateName(sysUser.getAccount());
			cloudImage.setCreatedTime(new Date());
			cloudImage.setImageDescription(cloudVm.getImageDesc());
			String minDisk = image.getMin_disk();
			if(minDisk!=null){
				cloudImage.setMinDisk(Long.valueOf(minDisk));
			}else{
				cloudImage.setMinDisk(null);	
			}
			String minRam = image.getMin_ram();
			if(minDisk!=null){
				cloudImage.setMinRam(Long.valueOf(minRam));
			}else{
				cloudImage.setMinRam(null);
			}
			if(!StringUtils.isEmpty(image.getStatus())){
				cloudImage.setImageStatus(image.getStatus().toUpperCase());
			}
			if(image.getSize()!=null){
				cloudImage.setImageSize(new BigDecimal(image.getSize()));
			}else{
				cloudImage.setImageSize(null);
			}
			if(image.getContainer_format()!=null){
				cloudImage.setContainerFormat(image.getContainer_format());
			}else{
				cloudImage.setContainerFormat(null);
			}
			
			cloudImageDao.save(cloudImage);
			
			
			JSONObject json =new JSONObject();
			json.put("imageId", cloudImage.getImageId());
			json.put("dcId",cloudImage.getDcId());
			json.put("prjId", cloudImage.getPrjId());
			json.put("imageStatus", cloudImage.getImageStatus());
			json.put("count", "0");
			
			final JSONObject data = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.imageKey, data.toJSONString());
				}
				
			});
			
		}catch(AppException e){
			throw e;
		}catch(Exception e){
			throw new AppException ("error.openstack.message");
		}
		
	}

	/**
	 * 运维删除云主机
	 * @Author: duanbinbin
	 * @param sysUser
	 * @param cloudVm
	 *<li>Date: 2016年4月26日</li>
	 * @throws Exception 
	 */
	@Override
	@Transactional(noRollbackFor=AppException.class)
	public void deleteVm(BaseEcmcSysUser sysUser, CloudVm cloudVm) throws Exception {
		log.info("删除云主机");
		
		/**
    	 * 判断资源是否有未完成的订单 --@author zhouhaitao
    	 */
		if(checkVmOrderExsit(cloudVm.getVmId(),true,true)){
			throw new AppException("该资源有未完成的订单，请取消订单后再进行删除操作！");
		}
		
		if("0".equals(cloudVm.getDeleteType())){
			softDeleteVm(cloudVm, sysUser);
		}
		else if("1".equals(cloudVm.getDeleteType())){
			forceDeleteVm(cloudVm, sysUser);
		}
		else if("2".equals(cloudVm.getDeleteType())){
			openstackVmService.forceDelete(cloudVm);

			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			vm.setVmStatus("DELETING");
			vm.setDeleteTime(new Date());
			vm.setDeleteUser(sysUser.getAccount());
			vm.setIsDeleted("1");

			cloudVmDao.merge(vm);
			
			tagService.refreshCacheAftDelRes("vm", cloudVm.getVmId());
			alarmService.deleteMonitorByResource(MonitorResourceType.VM.toString(), cloudVm.getVmId());
			
			ecmcCloudVolumeService.deleteVolumeByVm(cloudVm.getVmId(), sysUser.getAccount());
			
			ecmcCloudFloatIPService.refreshFloatIpByVm(cloudVm.getVmId());
			
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
	}
	
	/**
	 * 强制删除（不进回收站）
	 * @param cloudVm
	 * @param sysUser
	 */
	@Transactional(noRollbackFor=AppException.class)
	public void forceDeleteVm(CloudVm cloudVm, BaseEcmcSysUser sysUser){
		try {
			if (checkSavingSnapshot(cloudVm)) {
				throw new AppException("当前云主机正在创建自定义镜像，不允许删除");
			}
			
			CloudFloatIp cloudFloatIp = queryFloatIpByVm(cloudVm.getVmId());
			if(null != cloudFloatIp){
				cloudFloatIp.setResourceId(cloudVm.getVmId());
				cloudFloatIp.setResourceType("vm");
				ecmcCloudFloatIPService.unbundingResource(cloudFloatIp);
			}
			
			ecmcCloudVolumeService.debindVolsByVmId(cloudVm.getVmId());
			
			openstackVmService.forceDelete(cloudVm);

			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			CloudProject project = projectService.findProject(vm.getPrjId());
			vm.setDeleteUser(sysUser.getAccount());
			vm.setDeleteTime(new Date());
			vm.setVmStatus("DELETING");
			cloudVm.setOpDate(vm.getDeleteTime());
			cloudVm.setVmName(vm.getVmName());
			cloudVm.setCusId(project.getCustomerId());

			cloudVmDao.merge(vm);
			
			tagService.refreshCacheAftDelRes("vm", cloudVm.getVmId());
			
			alarmService.cleanAlarmDataAfterDeletingVM(cloudVm.getVmId());
			ecmcAlarmService.cleanAlarmDataAfterDeletingObject(cloudVm.getVmId());
			ecmcAlarmService.deleteMonitorByResource(MonitorResourceType.VM.toString(), cloudVm.getVmId());
			
			ecmcLBMemberService.deleteMemberByVm(cloudVm.getVmId());
			
			ecmcCloudVolumeService.deleteVolumeByVm(cloudVm.getVmId(), sysUser.getAccount());
			
			ecmcPortMappingService.deletePortMappingListByDestinyId(cloudVm.getDcId() ,cloudVm.getPrjId(),cloudVm.getVmId());
			
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
			
			if(PayType.PAYAFTER.equals(vm.getPayType())){
				vmOptionCharge(cloudVm,"delete");
			}
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}
	}
	
	/**
	 * 软删除（放进回收站）
	 * @param cloudVm
	 * @param sysUser
	 */
	@Transactional(noRollbackFor=AppException.class)
	public void softDeleteVm(CloudVm cloudVm, BaseEcmcSysUser sysUser){
		try {
			if (checkSavingSnapshot(cloudVm)) {
				throw new AppException("当前云主机正在创建自定义镜像，不允许删除");
			}
			
			ecmcCloudVolumeService.debindVolsByVmId(cloudVm.getVmId());
			
			CloudFloatIp cloudFloatIp = queryFloatIpByVm(cloudVm.getVmId());
			if(null != cloudFloatIp){
				cloudFloatIp.setResourceId(cloudVm.getVmId());
				cloudFloatIp.setResourceType("vm");
				ecmcCloudFloatIPService.unbundingResource(cloudFloatIp);
			}
			
			openstackVmService.softDeleteVm(cloudVm);

			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			CloudProject project = projectService.findProject(vm.getPrjId());
			vm.setVmStatus("SOFT_DELETING");
			vm.setDeleteUser(sysUser.getAccount());
			vm.setDeleteTime(new Date());
			cloudVm.setCusId(project.getCustomerId());
			cloudVm.setVmName(vm.getVmName());
			
			cloudVmDao.merge(vm);
			
			alarmService.cleanAlarmDataAfterDeletingVM(cloudVm.getVmId());
			
			ecmcAlarmService.cleanAlarmDataAfterDeletingObject(cloudVm.getVmId());
			
			ecmcLBMemberService.deleteMemberByVm(cloudVm.getVmId());//清除成员
			
			ecmcPortMappingService.deletePortMappingListByDestinyId(cloudVm.getDcId(),cloudVm.getPrjId(),cloudVm.getVmId());
			
			modifySysDiskForRecycle(cloudVm.getVmId(),sysUser.getAccount(),vm.getDeleteTime());
			
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
			
			if(PayType.PAYAFTER.equals(vm.getPayType())){
				vmOptionCharge(cloudVm,"recycle");
			}
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}
	}

	/**
	 * 批量挂载云硬盘
	 * 带系统盘最多只能有五个
	 * @Author: duanbinbin
	 * @param volList
	 * @return
	 *<li>Date: 2016年4月26日</li>
	 */
	@Override
	@Transactional(noRollbackFor=AppException.class)
	public Map<String ,Object> bindBatchVolume(List<Map<String,String>> volList) throws AppException {
		log.info("批量挂载云硬盘");
		Map<String ,Object> resultMap = new HashMap<String ,Object>();
		boolean isok = false;
		int suCount = 0;
		int erCount = 0;
		try{
			if(null != volList && volList.size() > 0){
				for(Map<String,String> map:volList){
					try {
						boolean isTrue = ecmcCloudVolumeService.bindVolume(map.get("dcId"), map.get("prjId"), 
								map.get("vmId"), map.get("volId"));
						suCount++;
						map.put("isSuccess", isTrue+"");
					} catch (AppException e) {
						erCount++;
						log.error(e.toString(),e);
						throw e;
					}
				}
				if(suCount == volList.size()){
					isok = true;
				}
			}
		}catch(Exception e){
			throw e;
		}
		resultMap.put("isok", isok);
		resultMap.put("suCount", suCount);
		resultMap.put("erCount", erCount);
		return resultMap;
	}

	/**
	 * 查询镜像列表
	 * @Author: duanbinbin
	 * @param cloudVm
	 * @return
	 *<li>Date: 2016年4月26日</li>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<CloudImage> getImageList(CloudVm cloudVm) {
		log.info("查询镜像列表");
		List<Object> list = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer();
		hql.append(" from BaseCloudImage  ");
		hql.append(" where 1=1  ");
		hql.append(" and  imageIspublic = ? ");
		if("publicImage".equals(cloudVm.getVmFrom())){
			list.add('1');
		}else if("privateImage".equals(cloudVm.getVmFrom())){
			list.add('2');
		}else{
			list.add("");
		}
		hql.append(" and  osType =  ? ");
		list.add(cloudVm.getOsType());
		hql.append(" and  sysType = ? ");
		list.add(cloudVm.getSysType());
		hql.append(" and  minCpu <= ?");
		list.add((long)cloudVm.getCpus());
		hql.append(" and  minRam <= ?");
		list.add((long)cloudVm.getRams()*1024);
		hql.append(" and  minDisk <= ?");
		list.add((long)cloudVm.getDisks());
		if("publicImage".equals(cloudVm.getVmFrom())){
			hql.append(" and dcId = ? ");
			list.add(cloudVm.getDcId());
		}
		if("privateImage".equals(cloudVm.getVmFrom())){
			hql.append(" and  prjId = ? ");
			list.add(cloudVm.getPrjId());
		}
		
		List<CloudImage> imageList = new ArrayList<CloudImage>();
		List<BaseCloudImage> baseImageList = cloudImageDao.find(hql.toString(), list.toArray());
		for(BaseCloudImage baseImage : baseImageList){
			CloudImage image = new CloudImage();
			BeanUtils.copyPropertiesByModel(image, baseImage);
			imageList.add(image);
		}
		return imageList;
	}

	/**
	 * 查询云主机日志
	 * @Author: duanbinbin
	 * @param cloudVm
	 * @return
	 *<li>Date: 2016年4月26日</li>
	 */
	@Override
	public String getVmLog(CloudVm cloudVm) throws AppException {
		log.info("查询云主机日志");
		String log = "";
		try{
			log = openstackVmService.getVmLogs(cloudVm);
			if(!StringUtils.isEmpty(log)){
				log = log.replaceAll("\n", "<br>");
			}
		}catch(AppException e){
			throw e;
		}catch(Exception e){
			throw new AppException ("error.openstack.message");
		}
		return log;
	}

	/**
	 * 查询项目下的子网列表
	 * @Author: duanbinbin
	 * @param prjId
	 * @return
	 *<li>Date: 2016年4月25日</li>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<CloudSubNetWork> getSubNetListByPrjId(String prjId) {
		log.info("查询项目下的子网列表");
		List<CloudSubNetWork> cloudSubList = new ArrayList<CloudSubNetWork>();
		StringBuffer hql = new StringBuffer();
		hql.append(" from BaseCloudSubNetWork ");
		hql.append(" where 1=1  ");
		hql.append(" and prjId = ? ");
		List<BaseCloudSubNetWork> baseSubList = cloudWorkDao.find(hql.toString(), prjId);
		for(int i = 0;i < baseSubList.size();i++){
			CloudSubNetWork cloudSub = new CloudSubNetWork();
			BeanUtils.copyPropertiesByModel(cloudSub, baseSubList.get(i));
			cloudSubList.add(cloudSub);
		}
		return cloudSubList;
	}

	/**
	 * 云主机详情
	 * 新增关联安全组名称列表，所属客户
	 * 去掉关联标签
	 * @Author: duanbinbin
	 * @param vmId
	 * @return
	 *<li>Date: 2016年4月25日</li>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public CloudVm getVmById(String vmId) {
		log.info("获取云主机详情");
		CloudVm vm = new CloudVm();
		List<Object> list = new ArrayList<Object>();
		
		StringBuffer sql = new StringBuffer();
		StringBuffer sql1 = new StringBuffer();
		sql.append("SELECT ");
		sql.append("vm.vm_id, ");
		sql.append("vm.vm_name, ");
		sql.append("vm.prj_id, ");
		sql.append("vm.dc_id, ");
		sql.append("vm.vm_status, ");
		sql.append("vm.from_imageid, ");
		sql.append("vm.vm_ip, ");
		sql.append("vm.sys_type, ");
		sql.append("vm.os_type, ");
		sql.append("vm.vm_description, ");
		sql.append("net.net_name, ");
		sql.append("dc.dc_name, ");
		sql.append("cus.cus_org, ");
		sql.append("prj.prj_name, ");
		sql.append("fla.flavor_vcpus, ");
		sql.append("fla.flavor_ram, ");
		sql.append("fla.flavor_disk, ");
		sql.append("flo.flo_id, ");
		sql.append("flo.flo_ip, ");
		sql.append("subnet.subnet_name, ");
		sql.append("vol.vol_size,  ");
		sql.append("selfSubnet.subnet_name as selfSubnetName, ");
		sql.append("vm.pay_type, ");
		sql.append("vm.charge_state,  ");
		sql.append("vm.create_time,  ");
		sql.append("vm.end_time,  ");
		sql.append("vm.self_ip, ");
		sql.append("vm.net_id, ");
		sql.append("vm.subnet_id, ");
		sql.append("vm.self_subnetid, ");
		sql.append("img.image_name ");
		sql.append(" FROM cloud_vm vm ");
		sql.append(" LEFT JOIN cloud_flavor fla ON vm.flavor_id = fla.flavor_id ");
		sql.append(" AND vm.dc_id = fla.dc_id ");
		sql.append(" LEFT JOIN cloud_network net on vm.net_id=net.net_id ");
		sql.append(" LEFT JOIN cloud_project prj ON vm.prj_id = prj.prj_id ");
		sql.append(" LEFT JOIN sys_selfcustomer cus ON prj.customer_id = cus.cus_id ");
		sql.append(" LEFT JOIN dc_datacenter dc ON vm.dc_id = dc.id ");
		sql.append(" LEFT JOIN cloud_floatip flo ON vm.vm_id = flo.resource_id ");
		sql.append(" AND flo.resource_type = 'vm' AND flo.is_deleted = '0' ");
		sql.append(" LEFT JOIN cloud_subnetwork subnet on subnet.subnet_id = vm.subnet_id ");
		sql.append(" LEFT JOIN cloud_subnetwork selfSubnet on selfSubnet.subnet_id = vm.self_subnetid ");
		sql.append(" LEFT JOIN cloud_image img ON img.image_id = vm.from_imageid  ");
		sql.append(" LEFT JOIN ");
		sql.append(" ( ");
		sql.append(" SELECT sum(vol_size) as vol_size,vm_id FROM cloud_volume WHERE vol_bootable = '0' GROUP BY vm_id ");
		sql.append(" ) ");
		sql.append(" AS vol ON vm.vm_id=vol.vm_id ");
		
		sql.append(" WHERE vm.vm_id = ? AND vm.is_deleted = '0' ");
		list.add(vmId);
		
		sql1.append("SELECT gro.sgName FROM BaseCloudSecurityGroup gro , BaseCloudVmSgroup vsg ");
		sql1.append(" WHERE gro.sgId = vsg.sgId AND vsg.vmId = ?");
		
        javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), list.toArray());
        
        String securityName = "";
        List<String> securityNameList = new ArrayList<String>();
        securityNameList = cloudVmDao.find(sql1.toString(), vmId);
        
        if(0!=query.getResultList().size() ){
            Object[] obj = (Object[]) query.getResultList().get(0);
            vm.setVmId(String.valueOf(obj[0]));
            vm.setVmName(String.valueOf(obj[1]));
            vm.setPrjId(String.valueOf(obj[2]));
            vm.setDcId(String.valueOf(obj[3]));
            vm.setVmStatus(String.valueOf(obj[4]));
            vm.setFromImageId(String.valueOf(obj[5]));
            vm.setVmIp(String.valueOf(obj[6]));
            String systemType = String.valueOf(obj[7]);
			if(!StringUtils.isEmpty(systemType)&&!"null".equals(systemType)){
				vm.setSysType(DictUtil.getDataTreeByNodeId(systemType).getNodeName());
			}
			String operatorSystemType = String.valueOf(obj[8]);
			if(!StringUtils.isEmpty(operatorSystemType)&&!"null".equals(operatorSystemType)){
				vm.setOsType(DictUtil.getDataTreeByNodeId(operatorSystemType).getNodeName());
			}
			vm.setVmDescripstion(String.valueOf(obj[9]==null?"":obj[9]));
            vm.setNetName(String.valueOf(obj[10]));
            vm.setDcName(String.valueOf(obj[11]));
            vm.setCusOrg(String.valueOf(obj[12]));
            vm.setPrjName(String.valueOf(obj[13]));
            vm.setCpus(Integer.parseInt(null != obj[14] ? String.valueOf(obj[14]) : "0"));
            vm.setRams(Integer.parseInt(null != obj[15] ? String.valueOf(obj[15]) : "0"));
            vm.setDisks(Integer.parseInt(null != obj[16] ? String.valueOf(obj[16]) : "0"));
			vm.setFloatId(String.valueOf(obj[17]));
            vm.setFloatIp(String.valueOf(obj[18]));
            vm.setSubnetName(String.valueOf(obj[19] == null ? "" : obj[19]));
            vm.setDataCapacity(Integer.parseInt(null != obj[20] ? String.valueOf(obj[20]) : "0"));
            vm.setSelfSubnetName(String.valueOf(obj[21] == null ? "" : obj[21]));
            vm.setPayType(String.valueOf(obj[22]));
            vm.setChargeState(String.valueOf(obj[23]));
            vm.setCreateTime((Date)obj[24]);
            vm.setEndTime((Date)obj[25]);
            vm.setSelfIp(String.valueOf(obj[26]));
            vm.setNetId(String.valueOf(obj[27]));
            vm.setSubnetId(String.valueOf(obj[28]));
            vm.setSelfSubnetId(String.valueOf(obj[29]));
            vm.setImageName(obj[30] != null?String.valueOf(obj[30]):"");
            vm.setNumber(1);
            vm.setVmStatusStr(CloudResourceUtil.escapseChargeState(vm.getChargeState()));
            if("BUILD".equalsIgnoreCase(vm.getVmStatus())){
            	vm.setVmStatus("BUILDING");
			}
            if(CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(vm.getChargeState())){
            	vm.setVmStatusStr(DictUtil.getStatusByNodeEn("vm", vm.getVmStatus()));
            }
        }else{
			return null;
		}
        for(int i = 0;i < securityNameList.size();i++){
        	String str = securityNameList.get(i);
        	if("default".equals(securityNameList.get(i))){
        		str = "默认安全组";
        	}
        	if(i == securityNameList.size() -1){
        		securityName = securityName + str;
        	}else{
        		securityName = securityName + str+"、";
        	}
        }
        vm.setSecurityGroups(securityName);
        vm.setSshCount(secretkeyVmService.SSHCountbyVm(vm.getVmId()));
		return vm;
	}

	/**
	 * 查询云主机列表
	 * @Author: duanbinbin
	 * @param page
	 * @param queryMap
	 * @param dcId		数据中心id
	 * @param vmStatus	云主机状态
	 * @param sysType	操作系统
	 * @param timesort	排序
	 * @param queryType	输入框查询类型：vmName、ip、prjName、cusOrg
	 * @param queryName	输入框查询内容
	 * @return
	 *<li>Date: 2016年4月25日</li>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Page getVmPage(Page page, QueryMap queryMap, String dcId,
			String vmStatus, String sysType, String timesort, String queryType,
			String queryName) {
		//todo数据盘容量
		log.info("查询云主机列表");
		List<Object> list = new ArrayList<Object>();
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append("vm.vm_id, ");
		sql.append("vm.vm_name, ");
		sql.append("vm.prj_id, ");
		sql.append("vm.dc_id, ");
		sql.append("vm.vm_status, ");
		sql.append("vm.create_time, ");
		sql.append("vm.end_time, ");
		sql.append("vm.from_imageid, ");
		sql.append("vm.vm_ip, ");
		sql.append("vm.sys_type, ");
		sql.append("vm.host_name, ");
		sql.append("dc.dc_name, ");
		sql.append("cus.cus_org, ");
		sql.append("prj.prj_name, ");
		sql.append("fla.flavor_vcpus, ");
		sql.append("fla.flavor_ram, ");
		sql.append("fla.flavor_disk, ");
		sql.append("flo.flo_ip, ");
		sql.append("vol.vol_size,  ");
		sql.append("vm.self_ip,  ");
		sql.append("vm.pay_type,  ");
		sql.append("vm.charge_state ");
		sql.append("FROM cloud_vm vm ");
		sql.append("LEFT JOIN cloud_flavor fla ON vm.flavor_id = fla.flavor_id ");
		sql.append("AND vm.dc_id = fla.dc_id ");
		sql.append("LEFT JOIN cloud_project prj ON vm.prj_id = prj.prj_id ");
		sql.append("LEFT JOIN sys_selfcustomer cus ON prj.customer_id = cus.cus_id ");
		sql.append("LEFT JOIN dc_datacenter dc ON vm.dc_id = dc.id ");
		sql.append("LEFT JOIN cloud_floatip flo ON vm.vm_id = flo.resource_id ");
		sql.append("AND flo.resource_type = 'vm' AND flo.is_deleted = '0' ");
		sql.append("LEFT JOIN ");
		sql.append("( ");
		sql.append("SELECT sum(vol_size) as vol_size,vm_id FROM cloud_volume WHERE vol_bootable = '0' GROUP BY vm_id ");
		sql.append(") ");
		sql.append("AS vol ON vm.vm_id=vol.vm_id ");
		sql.append("WHERE vm.is_deleted = '0' ");
		sql.append("and vm.is_visable = '1' ");
		
		if(!"null".equals(dcId) && !"".equals(dcId) && !"undefined".equals(dcId)){
			sql.append("and vm.dc_id = ? ");
			list.add(dcId);
		}
		if(!"null".equals(vmStatus) && !"".equals(vmStatus) && !"undefined".equals(vmStatus)){
			if("1".equals(vmStatus)){
				sql.append("and vm.charge_state = ? ");
				list.add(vmStatus);
			}
			else if("2".equals(vmStatus)){
				sql.append("and (vm.charge_state = ? or vm.charge_state = '3') ");
				list.add(vmStatus);
			}
			else{
				sql.append("and vm.vm_status = ? ");
				sql.append("and vm.charge_state = '0' ");
				list.add(vmStatus);
			}
		}
		if(!"null".equals(sysType) && !"".equals(sysType) && !"undefined".equals(sysType)){
			sql.append("and vm.sys_type = ? ");
			list.add(sysType);
		}
		if (null != queryName && !queryName.trim().equals("")) {
			if(queryType.equals("vmName")){
				//根据云主机名称模糊查询
				queryName = queryName.replaceAll("\\_", "\\\\_");
				sql.append(" and binary vm.vm_name like ?");
	            list.add("%" + queryName + "%");
	            
			}else if(queryType.equals("ip")){
				//根据公网ip或内网ip模糊查询
				queryName = queryName.replaceAll("\\_", "\\\\_");
				sql.append(" and (binary vm.vm_ip like ? or binary flo.flo_ip like ? or vm.self_ip like ?) ");
	            list.add("%" + queryName + "%");
	            list.add("%" + queryName + "%");
	            list.add("%" + queryName + "%");
	            
			}else if(queryType.equals("cusOrg")){
				//根据所属客户精确查询
				String[] cusOrgs = queryName.split(",");
				sql.append(" and ( ");
				for(String org:cusOrgs){
					sql.append(" binary cus.cus_org = ? or ");
					list.add(org);
				}
				sql.append(" 1 = 2 ) ");
				
			}else if(queryType.equals("prjName")){
				//根据项目名称精确查询
				String[] prjName = queryName.split(",");
				sql.append(" and ( ");
				for(String prj:prjName){
					sql.append(" binary prj.prj_name = ? or ");
					list.add(prj);
				}
				sql.append(" 1 = 2 ) ");
			}
			else{
				sql.append(" and  1 = 2 ");
			}
		}
		sql.append(" ORDER BY vm.dc_id , vm.prj_id ,vm.create_time DESC ");
		
		page = cloudVmDao.pagedNativeQuery(sql.toString(), queryMap, list.toArray());
		@SuppressWarnings("rawtypes")
		List resultList = (List) page.getResult();
		for(int i = 0; i < resultList.size(); i++){
			int index = 0;
			Object[] obj = (Object[]) resultList.get(i);
			CloudVm vm = new CloudVm();
			vm.setVmId(String.valueOf(obj[index++]));
			vm.setVmName(String.valueOf(obj[index++]));
			vm.setPrjId(String.valueOf(obj[index++]));
			vm.setDcId(String.valueOf(obj[index++]));
			vm.setVmStatus(String.valueOf(obj[index++]));
			vm.setCreateTime((Date)(obj[index++]));
			vm.setEndTime(((Date)obj[index++]));
			vm.setFromImageId(String.valueOf(obj[index++]));
			vm.setVmIp(String.valueOf(obj[index++]));
			
			String type = String.valueOf(obj[index++]) ;
			if(!StringUtils.isEmpty(type)&&!"null".equals(type)){
				vm.setSysType(DictUtil.getDataTreeByNodeId(type).getNodeName());
			}
			vm.setHostName(String.valueOf(obj[index++]));
			vm.setDcName(String.valueOf(obj[index++]));
			vm.setCusOrg(String.valueOf(obj[index++]));
			vm.setPrjName(String.valueOf(obj[index++]));
			vm.setCpus(Integer.parseInt(null != obj[index++] ? String.valueOf(obj[index-1]) : "0"));
			vm.setRams(Integer.parseInt(null != obj[index++] ? String.valueOf(obj[index-1]) : "0"));
			vm.setDisks(Integer.parseInt(null != obj[index++] ? String.valueOf(obj[index-1]) : "0"));
			vm.setFloatIp(String.valueOf(obj[index++]));
			vm.setDataCapacity(Integer.parseInt(null != obj[index++] ? String.valueOf(obj[index-1]) : "0"));
			vm.setSelfIp(String.valueOf(obj[index++]));
			vm.setPayType(String.valueOf(obj[index++]));
			vm.setChargeState(String.valueOf(obj[index++]));
			vm.setVmStatusStr(CloudResourceUtil.escapseChargeState(vm.getChargeState()));
			
			if("BUILD".equalsIgnoreCase(vm.getVmStatus())){
				vm.setVmStatus("BUILDING");
			}
			if (CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(vm.getChargeState())) {
				vm.setVmStatusStr(DictUtil.getStatusByNodeEn("vm", vm.getVmStatus()));
			}
			
			resultList.set(i, vm);
		}
		return page;
	}

	/**
	 * 编辑云主机：名称或描述
	 * 修改描述时不需要去底层
	 * @Author: duanbinbin
	 * @param cloudVm
	 *<li>Date: 2016年4月26日</li>
	 */
	@Override
	public void updateVm(CloudVm cloudVm) throws AppException {
		log.info("编辑云主机");
		try{
			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			if(!cloudVm.getVmName().equals(vm.getVmName())){
				openstackVmService.modifyVm(cloudVm);
			}
			vm.setVmName(cloudVm.getVmName());
			vm.setVmDescripstion(cloudVm.getVmDescripstion());
			
			cloudVmDao.merge(vm);
			
		}catch(AppException e){
			throw e;
		}catch(Exception e){
			throw new AppException ("error.openstack.message");
		}
		
	}

	/**
	 * 编辑云主机安全组
	 * @Author: duanbinbin
	 * @param cloudVm
	 *<li>Date: 2016年4月26日</li>
	 */
	@Override
	public void editVmSecurityGroup(CloudVm cloudVm) throws AppException {
		log.info("编辑云主机安全组");
		List<BaseCloudSecurityGroup> toBeAddList = new ArrayList<BaseCloudSecurityGroup>();
		List<BaseCloudSecurityGroup> toBeDelList = new ArrayList<BaseCloudSecurityGroup>();
		Map<String,BaseCloudSecurityGroup> existMap =new HashMap<String,BaseCloudSecurityGroup>();
		Map<String,BaseCloudSecurityGroup> formMap =new HashMap<String,BaseCloudSecurityGroup>();
		
		try{
			List<CloudSecurityGroup> existList = getSecurityGroupByVm(cloudVm.getVmId(),cloudVm.getPrjId());
			BaseCloudSecurityGroup [] formGroups = cloudVm.getBcsgs();
			
			if(null!=existList&&existList.size()>0){
				for(CloudSecurityGroup csp :existList){
					BaseCloudSecurityGroup bcsp = new BaseCloudSecurityGroup();
					BeanUtils.copyPropertiesByModel(bcsp, csp);
					existMap.put(bcsp.getSgId(), bcsp);
				}
			}
			
			if(null!=formGroups&&formGroups.length>0){
				for(BaseCloudSecurityGroup bcsp :formGroups){
					if(!existMap.containsKey(bcsp.getSgId())){
						toBeAddList.add(bcsp);
					}
					formMap.put(bcsp.getSgId(), bcsp);
				}
			}
			if(null!=existList&&existList.size()>0){
				for(CloudSecurityGroup csp :existList){
					BaseCloudSecurityGroup bcsp = new BaseCloudSecurityGroup();
					BeanUtils.copyPropertiesByModel(bcsp, csp);
					if(!formMap.containsKey(bcsp.getSgId())){
						toBeDelList.add(bcsp);
					}
				}
			}
			
			openstackVmService.editVmSecurityGroup(cloudVm,toBeAddList,toBeDelList);
			
			cloudVmSecurityGroupDao.deleteByVmId(cloudVm.getVmId());
			
			if(null!=formGroups&&formGroups.length>0){
				for(BaseCloudSecurityGroup bcsp :formGroups){
					BaseCloudVmSgroup vsg =new BaseCloudVmSgroup();
					vsg.setSgId(bcsp.getSgId());
					vsg.setVmId(cloudVm.getVmId());
					
					cloudVmSecurityGroupDao.merge(vsg);
				}
			}
			
		}catch(AppException e){
			throw e;
		}catch(Exception e){
			throw new AppException ("error.openstack.message");
		}
	}

	/**
	 * 查询项目下未关联某云主机的安全组列表
	 * @Author: duanbinbin
	 * @param vmId
	 * @param prjId
	 * @return
	 *<li>Date: 2016年4月25日</li>
	 */
	@Override
	public List<CloudSecurityGroup> getSecurityByPrjNoVm(String vmId,
			String prjId) {
		log.info("查询项目下未关联某一云主机的安全组信息");
		List<Object> list = new ArrayList<Object>();
		List<CloudSecurityGroup> Groupist = new ArrayList<CloudSecurityGroup>();
		StringBuffer sql =new StringBuffer ();
		sql.append(" select sg_id,sg_name from  ");
		sql.append(" cloud_securitygroup ");
		sql.append(" where prj_id = ? ");
		sql.append(" and  sg_id not in ");
		sql.append("  	( ");
		sql.append("  		select sg_id from cloud_vmsecuritygroup");
		sql.append("  		where vm_id = ? ");
		sql.append("  	)");
		list.add(prjId);
		list.add(vmId);
		
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), list.toArray());
		@SuppressWarnings("rawtypes")
		List listResult = query.getResultList();
		for (int i = 0; i < listResult.size(); i++) {
			Object[] obj = (Object[]) listResult.get(i);
			CloudSecurityGroup cloudSg = new CloudSecurityGroup();
			cloudSg.setSgId(String.valueOf(obj[0]));
			cloudSg.setSgName(String.valueOf(obj[1]));
			if("default".equals(cloudSg.getSgName())){
				cloudSg.setSgName("默认安全组");
			}
			Groupist.add(cloudSg);
		}
		return Groupist;
	}

	/**
	 * 查询某云主机已经关联的安全组列表
	 * @Author: duanbinbin
	 * @param vmId
	 * @param prjId
	 * @return
	 *<li>Date: 2016年4月25日</li>
	 */
	@Override
	public List<CloudSecurityGroup> getSecurityGroupByVm(String vmId,
			String prjId) {
		log.info("查询已关联某云主机的安全组列表信息");
		List<Object> list = new ArrayList<Object>();
		List<CloudSecurityGroup> Groupist = new ArrayList<CloudSecurityGroup>();
		StringBuffer sql =new StringBuffer ();
		sql.append(" select vsg.sg_id,sg.sg_name from  ");
		sql.append(" cloud_vmsecuritygroup vsg ");
		sql.append(" left join cloud_securitygroup sg ");
		sql.append(" on vsg.sg_id = sg.sg_id ");
		sql.append(" where vsg.vm_id = ? ");
		sql.append(" and sg.prj_id = ? ");
		list.add(vmId);
		list.add(prjId);
		
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), list.toArray());
		@SuppressWarnings("rawtypes")
		List listResult = query.getResultList();
		for (int i = 0; i < listResult.size(); i++) {
			Object[] obj = (Object[]) listResult.get(i);
			CloudSecurityGroup cloudSg = new CloudSecurityGroup();
			cloudSg.setSgId(String.valueOf(obj[0]));
			cloudSg.setSgName(String.valueOf(obj[1]));
			if("default".equals(cloudSg.getSgName())){
				cloudSg.setSgName("默认安全组");
			}
			Groupist.add(cloudSg);
		}
		return Groupist;
	}

	/**
	 * 查询所有的云主机状态
	 * @Author: duanbinbin
	 * @return
	 *<li>Date: 2016年4月25日</li>
	 */
	@Override
	public List<SysDataTree> getVmStatusList() {
		return getSysDataListByParentId(ConstantClazz.DICT_CLOUD_VMSTAUS_TYPE_NODE_ID);
	}

	/**
	 * 查询出所有直接用于创建云主机的操作系统类型列表
	 * @Author: duanbinbin
	 * @return
	 *<li>Date: 2016年4月25日</li>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<SysDataTree> getAllVmSysList() {
		StringBuffer hql =new StringBuffer ();
		hql.append(" from  BaseSysDataTree ");
		hql.append(" where nodeId like ?  ");
		hql.append(" and length(nodeId) =16  ");
		hql.append(" and nodeId <> ? ");
		
		return cloudVmDao.find(hql.toString(),new Object[]{ConstantClazz.DICT_CLOUD_SYS_TYPE_NODE_ID+"%",ConstantClazz.DICT_CLOUD_QTSYS_NODE_ID});
	}

	/**
	 * 查询某一系统类型下的所有操作系统列表，用于创建云主机使用
	 * 如：Windows下属的所有操作系统列表
	 * @Author: duanbinbin
	 * @param osId
	 * @return
	 *<li>Date: 2016年4月25日</li>
	 */
	@Override
	public List<SysDataTree> getSysTypeListByOs(String osId) {
		return getSysDataListByParentId(osId);
	}

	/**
	 * 获取系统类型（Linux、Windows、其他）
	 * @Author: duanbinbin
	 * @return
	 *<li>Date: 2016年4月25日</li>
	 */
	@Override
	public List<SysDataTree> getOsList() {
		List<SysDataTree> sysList = new ArrayList<SysDataTree>();
		List<SysDataTree> list= getSysDataListByParentId(ConstantClazz.DICT_CLOUD_HOST_MIRROR_NODE_ID);
		for(SysDataTree tree :list){
			if(!"0007002002003".equals(tree.getNodeId())){
				sysList.add(tree);
			}
		}
		return sysList;
	}

	/**
	 * 获取CPU配置信息
	 * @Author: duanbinbin
	 * @return
	 *<li>Date: 2016年4月25日</li>
	 */
	@Override
	public List<SysDataTree> getCpuList() {
		return getSysDataListByParentId(ConstantClazz.DICT_CLOUD_CPU_TYPE_NODE_ID);
	}

	/**
	 * 根据CPU配置获取内存配置信息
	 * @Author: duanbinbin
	 * @param cpuId
	 * @return
	 *<li>Date: 2016年4月25日</li>
	 */
	@Override
	public List<SysDataTree> getRamListByCpu(String cpuId) {
		return getSysDataListByParentId(cpuId);
	}

	/**
	 * 校验重名
	 * @Author: duanbinbin
	 * @param cloudVm
	 * @return
	 *<li>Date: 2016年4月25日</li>
	 */
	@Override
	public boolean checkVmName(CloudVm cloudVm) {
		boolean flag = false;
		log.info("校验项目下云主机名称重名");
		String [] nameList = null;
		
		int num = cloudVm.getNumber();
		if(num!=0){
			nameList =new String [num];
		}
		if(num==1){
			nameList[0]=cloudVm.getVmName();
		}
		else if(num>1){
			for(int i=1; i<=num;i++){
				nameList[i-1]=cloudVm.getVmName()+"_"+i;
			}
		}
		if(null == cloudVm.getVmName()||cloudVm.getVmName().equals("")){
			return true;
		}
		
		StringBuffer hql =new StringBuffer();
		hql.append(" from  ");
		hql.append(" BaseCloudVm v where 1=1 ");
		hql.append(" and v.prjId = :prjId ");
		hql.append(" and v.isDeleted in ('0','2') ");
		hql.append(" and v.isVisable = '1' ");
		hql.append(" and binary(v.vmName) in (:names)");
		if(!StringUtils.isEmpty(cloudVm.getVmId())){
			hql.append(" and v.vmId <> :vmId");
		}
		Query query = cloudVmDao.getHibernateSession().createQuery(hql.toString());
		query.setParameter("prjId", cloudVm.getPrjId());
		query.setParameterList("names", nameList);
		if(!StringUtils.isEmpty(cloudVm.getVmId())){
			query.setParameter("vmId", cloudVm.getVmId());
		}
		@SuppressWarnings("unchecked")
		List<BaseCloudVm> queryList = query.list();
		flag = (null==queryList||queryList.size()==0);
		if(flag){
			StringBuffer orderVmHql = new StringBuffer();
			orderVmHql.append("	SELECT                                                 ");
			orderVmHql.append("		cov.count,                                         ");
			orderVmHql.append("		cov.vm_name                                        ");
			orderVmHql.append("	FROM                                                   ");
			orderVmHql.append("		cloudorder_vm cov                                  ");
			orderVmHql.append("	LEFT JOIN order_info oi ON oi.order_no = cov.order_no  ");
			orderVmHql.append("	WHERE                                                  ");
			orderVmHql.append("		binary(cov.vm_name) = ?                            ");
			orderVmHql.append("	AND cov.order_type = '0'                               ");
			orderVmHql.append("	AND oi.order_state in ('1','2')                        ");
			orderVmHql.append("	AND cov.prj_id = ?                                     ");
			javax.persistence.Query orderVmQuery = cloudVmDao.createSQLNativeQuery(orderVmHql.toString(),new Object[]{cloudVm.getVmName(),cloudVm.getPrjId()});
			@SuppressWarnings("rawtypes")
			List orderVmList = orderVmQuery.getResultList();
			for(int i = 0;i<orderVmList.size();i++){
				Object [] obj = (Object [])orderVmList.get(i);
				int count = Integer.parseInt(String.valueOf(obj[0]));
				String vmName = String.valueOf(obj[1]);
				if(count==1&&cloudVm.getNumber()==1&&cloudVm.getVmName().equals(vmName)){
					flag = false;
					break;
				}
				if(count>1&&cloudVm.getNumber()>1&&cloudVm.getVmName().equals(vmName)){
					flag = false;
					break;
				}
			}
		}
		return flag;
	}

	/**
	 * 查询云主机下已关联的云硬盘数量（包括数据盘和系统盘）
	 * @Author: duanbinbin
	 * @param vmId
	 * @return
	 *<li>Date: 2016年4月25日</li>
	 * @throws Exception 
	 */
	@Override
	public int getDiskCountByVm(String vmId) throws Exception {
		log.info("查询当前云主机已经挂载的云硬盘数");
		int volCount = 0;
		try {
			volCount = ecmcCloudVolumeService.getCountByVnId(vmId);
		} catch (Exception e) {
		    log.error(e.toString(),e);
			throw e;
		}
		return volCount;
	}
	/**
	 * 根据上级nodeId查询子级数据字典信息
	 * @Author: duanbinbin
	 * @param parentId
	 * @return
	 *<li>Date: 2016年4月25日</li>
	 */
	public List<SysDataTree> getSysDataListByParentId(String parentId){
		return DictUtil.getDataTreeByParentId(parentId);
	}
	/**
	 * 云主机解绑弹性公网Ip
	 * @Author: duanbinbin
	 * @param cloudVm
	 *<li>Date: 2016年4月27日</li>
	 */
	@Override
	public void unBindIpByVmId(CloudVm cloudVm) throws AppException {
		log.info("云主机解绑弹性公网Ip");
		BaseCloudFloatIp floatIp = ecmcCloudFloatIPService.getFloatIpByResourceId(cloudVm.getVmId(), "vm");
		if(null != floatIp){
			CloudFloatIp ip = new CloudFloatIp();
			BeanUtils.copyPropertiesByModel(ip, floatIp);
			ecmcCloudFloatIPService.unBinDingVmIp(ip);
		}
	}
	/**
	 * 查询项目下可绑定云硬盘的云主机列表
	 * @Author: duanbinbin
	 * @param prjId
	 * @return
	 *<li>Date: 2016年4月28日</li>
	 */
	@Override
	public List<CloudVm> getCanBindCloudVmList(String prjId) {
		List<CloudVm> list= new ArrayList<CloudVm>();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append("vm.vm_id, ");
		sql.append("vm.vm_name, ");
		sql.append("vm.prj_id, ");
		sql.append("vm.vm_status, ");
		sql.append("vmnum.num ");
		sql.append("FROM ");
		sql.append("cloud_vm vm ");
		sql.append("LEFT JOIN ( ");
		sql.append("SELECT COUNT(vol.vol_id) AS num, vol.vm_id ");
		sql.append("FROM cloud_volume vol GROUP BY vol.vm_id");
		sql.append(") AS vmnum ON vm.vm_id = vmnum.vm_id ");
		sql.append("WHERE vm.is_deleted = '0' ");
		sql.append("AND ( vmnum.num < 5 OR vmnum.num IS NULL ) ");
		sql.append("AND vm.prj_id = ? ");
		sql.append("AND vm.charge_state = '0' ");
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), prjId);
		@SuppressWarnings("rawtypes")
		List listResult = query.getResultList();
		for (int i = 0; i < listResult.size(); i++) {
			Object[] obj = (Object[]) listResult.get(i);
			CloudVm vm = new CloudVm();
			vm.setVmId(String.valueOf(obj[0]));
			vm.setVmName(String.valueOf(obj[1]));
			vm.setPrjId(String.valueOf(obj[2]));
			vm.setVmStatus(String.valueOf(obj[3]));
			vm.setVolCount(Integer.parseInt(null != obj[4] ? String.valueOf(obj[4]) : "0"));
			list.add(vm);
		}
		return list;
	}
	/**
	 * 根据数据中心ID查询下属项目列表及配额信息
	 * @Author: duanbinbin
	 * @param dcId
	 * @return
	 *<li>Date: 2016年5月3日</li>
	 */
	@Override
	public List<CloudProject> getproListByDcId(String dcId) {
		log.info("根据数据中心ID查询下属项目列表及配额信息");
		List<CloudProject> prjList = new ArrayList<CloudProject>();
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append("	cp.prj_id AS prjId,");
		sql.append("	cp.prj_name AS prjName,");
		sql.append("	cp.dc_id AS dcId,");
		sql.append("	cp.cpu_count AS cpuCount,");
		sql.append("	cp.host_count AS hostCount,");
		sql.append("    cp.disk_count,");
		sql.append("	cp.memory AS memory,");
		sql.append("	cp.disk_capacity AS diskCapacity,");

		sql.append("	cp.customer_id AS cusId,");

		sql.append("	v.usedVmCount,");
		sql.append("	v.usedCpuCount,");
		sql.append("	v.usedRam,");
		sql.append("	vol.usedDiskCapacity,");
		sql.append("    vol.volCount AS diskCountUse,");
		sql.append("	snap.usedSnapshotCapacity");
		sql.append(" from cloud_project cp   ");
		sql.append(" left join   ");
		sql.append(" (  ");
		sql.append("  	select   ");
		sql.append("   	vm.prj_id,");
		sql.append("   	count(vm.vm_id) as usedVmCount,");
		sql.append("    sum(cf.flavor_vcpus) as usedCpuCount,");
		sql.append("    sum(cf.flavor_ram) as usedRam ");
		sql.append("    from");
		sql.append( " ( ");
		sql.append("  		select");
		sql.append("  			cv.vm_id ,");
		sql.append("  			cv.dc_id ,");
		sql.append("  		 	cv.prj_id ,");
		sql.append("  		 	cv.is_deleted ,");
		sql.append("  			case when cv.resize_id is not null then cv.resize_id else cv.flavor_id end as flavor_id ");
		sql.append("  		from cloud_vm cv ");
		sql.append(" 	) vm ");
		sql.append("    LEFT JOIN cloud_flavor cf");
		sql.append("   	ON vm.flavor_id = cf.flavor_id	     ");
		sql.append("   	and vm.dc_id = cf.dc_id	     ");
		sql.append("    where vm.is_deleted = '0' ");
		sql.append("    GROUP BY vm.prj_id    ");
		sql.append(" ) v ");
		sql.append(" on cp.prj_id = v.prj_id");
		sql.append(" left join   ");
		sql.append(" (  ");
		sql.append("    select ");
		sql.append("    vo.prj_id,");
		sql.append("    sum(vo.vol_size) as usedDiskCapacity, ");
		sql.append("    COUNT(vo.vol_id) AS volCount");
		sql.append("    from cloud_volume vo ");
		sql.append("    where vo.is_deleted = '0' ");
		sql.append("    group by vo.prj_id");
		sql.append(" ) vol  ");
		sql.append(" on cp.prj_id = vol.prj_id  ");
		sql.append(" left join   ");
		sql.append(" (  ");
		sql.append("    select ");
		sql.append("    snap.prj_id,");
		sql.append("    sum(snap.snap_size) as usedSnapshotCapacity ");
		sql.append("    from cloud_disksnapshot snap");
		sql.append("    group by snap.prj_id ");
		sql.append(" ) snap  ");
		sql.append(" on cp.prj_id = snap.prj_id  ");
		sql.append("WHERE  cp.dc_id = ? ");
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), dcId);
		@SuppressWarnings("rawtypes")
		List listResult = query.getResultList();
		for (int i = 0; i < listResult.size(); i++) {
			Object[] obj = (Object[]) listResult.get(i);
			CloudProject cloudProject = new CloudProject();
			
			cloudProject.setProjectId(String.valueOf(obj[0]));
			cloudProject.setPrjName(String.valueOf(obj[1]));
			cloudProject.setDcId(String.valueOf(obj[2]));
			
			cloudProject.setCpuCount(Integer.parseInt(String.valueOf(obj[3])==null?"0":String.valueOf(obj[3])));
			cloudProject.setHostCount(Integer.parseInt(String.valueOf(obj[4])==null?"0":String.valueOf(obj[4])));
			cloudProject.setDiskCount(Integer.parseInt(String.valueOf(obj[5])==null?"0":String.valueOf(obj[5])));
			cloudProject.setMemory(1024*Integer.parseInt(String.valueOf(obj[6])==null?"0":String.valueOf(obj[6])));
			cloudProject.setDiskCapacity(Integer.parseInt(obj[7]==null?"0":String.valueOf(obj[7])));//硬盘/备份大小配额

			cloudProject.setCustomerId((obj[8]==null?"0":String.valueOf(obj[8])));//用户Id

			cloudProject.setUsedVmCount(Integer.parseInt(obj[9]==null?"0":String.valueOf(obj[9])));
			cloudProject.setUsedCpuCount(Integer.parseInt(obj[10]==null?"0":String.valueOf(obj[10])));
			cloudProject.setUsedRam(Integer.parseInt(obj[11]==null?"0":String.valueOf(obj[11])));
			cloudProject.setUsedDiskCapacity(Integer.parseInt(obj[12]==null?"0":String.valueOf(obj[12])));
			cloudProject.setDiskCountUse(Integer.parseInt(obj[13]==null?"0":String.valueOf(obj[13])));
			cloudProject.setUsedSnapshotCapacity(Integer.parseInt(obj[14]==null?"0":String.valueOf(obj[14])));
			
			cloudProject.setUsedDataCapacity(cloudProject.getUsedDiskCapacity()+cloudProject.getUsedSnapshotCapacity());
			prjList.add(cloudProject);
		}
		return prjList;
	}
	
	@Override
    public int getUnDeletedVmCountByProject(String prjId) {
        int count = 0;
        count = cloudVmDao.getUnDeletedVmCountByPrjId(prjId);
        return count;
    }

	/**
	 * 查询客户当前项目下的所有运行中的主机
	 * use by liyanchao
	 * @return
	 */
	public List<CloudVm> getVmListByPrjIdAndVmStatus(String prjId,String vmStatus) {
		List<BaseCloudVm> vmList = new ArrayList<BaseCloudVm>();
		List<CloudVm> vmCloudList = new ArrayList<CloudVm>();
		vmList = cloudVmDao.getVmListByPrjIdAndVmStatus(prjId, vmStatus);
		if(null!=vmList){
			for(BaseCloudVm vm :vmList){
				CloudVm cloudVm = new CloudVm();
				BeanUtils.copyPropertiesByModel(cloudVm, vm);
				vmCloudList.add(cloudVm);
			}
			return vmCloudList;
		}
		return null;
	}
	/**
	 * 修改云主机，use by liyanchao（冻结客户时调用保存云主机状态）
	 * @return
	 */
	public BaseCloudVm mergeBaseVm(BaseCloudVm vm){
		BaseCloudVm baseVm =(BaseCloudVm) cloudVmDao.merge(vm);
		return baseVm;
	}
	/**
	 * 查找云主机，use by liyanchao（根据id找baseVm）
	 * @return
	 */
	public BaseCloudVm findBaseVmByVmId(String vmId){
		BaseCloudVm baseVm = cloudVmDao.findOne(vmId);
		return baseVm;
	}
	 /**
     * 获得状态为未删除且非active的云主机list
     * @author liyanchao
     * @return vmList
     */
    public List<CloudVm> getNoActiveUnDeletedVmByPrjId(String prjId , String vmStatus){
    	List<BaseCloudVm> baseVmList = cloudVmDao.getNoActiveUnDeletedVmByPrjId(prjId, vmStatus);
    	List<CloudVm> vmList = new ArrayList<CloudVm>();
    	for(BaseCloudVm baseVm :baseVmList){
    		CloudVm vm =new CloudVm();
    		BeanUtils.copyPropertiesByModel(vm, baseVm);
    		vmList.add(vm);
    	}
    	return vmList;
    }
    
    /**
     * 查询网络下的子网
     * @param subnet
     * @return
     */
    @SuppressWarnings("unchecked")
	public List<CloudSubNetWork> querySubnetByNet(CloudSubNetWork subnet){
    	StringBuffer sql = new StringBuffer();
    	sql.append(" from BaseCloudSubNetWork where netId = ? and subnetType = ?");
    	
    	@SuppressWarnings("rawtypes")
		List list = cloudVmDao.find(sql.toString(), new Object[]{subnet.getNetId(),subnet.getSubnetType()});
    	return list;
    }
    
    /**
	 * 云主机修改子网
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 				主机信息
	 */
	public void modifySubnet(CloudVm cloudVm){

		BaseCloudVm baseCloudVm = cloudVmDao.findOne(cloudVm.getVmId());
		List<String> sgList = querySecurityGroupByVm(cloudVm.getVmId());
		String [] sgIds = sgList.toArray(new String[]{});
		if(!StringUtils.isEmpty(baseCloudVm.getSubnetId()) 
				&& !StringUtils.isEmpty(cloudVm.getSubnetId())
				&&!baseCloudVm.getSubnetId().equals(cloudVm.getSubnetId())){
			openstackVmService.unbindPort(baseCloudVm.getDcId(), baseCloudVm.getPrjId(), baseCloudVm.getVmId(), baseCloudVm.getPortId());
			InterfaceAttachment interAtt = openstackVmService.bindPort(baseCloudVm.getDcId(), baseCloudVm.getPrjId(), baseCloudVm.getVmId(), baseCloudVm.getNetId(), 
					cloudVm.getSubnetId(), sgIds);
			
			baseCloudVm.setSubnetId(cloudVm.getSubnetId());
			baseCloudVm.setPortId(interAtt.getPort_id());
			baseCloudVm.setVmIp(interAtt.getFixed_ips()[0].getIp_address());
			
		}
		if(!StringUtils.isEmpty(baseCloudVm.getSubnetId()) 
				&& StringUtils.isEmpty(cloudVm.getSubnetId())){
			openstackVmService.unbindPort(baseCloudVm.getDcId(), baseCloudVm.getPrjId(), baseCloudVm.getVmId(), baseCloudVm.getPortId());
			
			baseCloudVm.setSubnetId(null);
			baseCloudVm.setPortId(null);
			baseCloudVm.setVmIp(null);
		}
		if(StringUtils.isEmpty(baseCloudVm.getSubnetId()) 
				&& !StringUtils.isEmpty(cloudVm.getSubnetId())){
			
			InterfaceAttachment interAtt = openstackVmService.bindPort(baseCloudVm.getDcId(), baseCloudVm.getPrjId(), baseCloudVm.getVmId(), baseCloudVm.getNetId(), 
					cloudVm.getSubnetId(), sgIds);
			
			baseCloudVm.setSubnetId(cloudVm.getSubnetId());
			baseCloudVm.setPortId(interAtt.getPort_id());
			baseCloudVm.setVmIp(interAtt.getFixed_ips()[0].getIp_address());
		}
		
		
		if(!StringUtils.isEmpty(baseCloudVm.getSelfSubnetId()) 
				&& !StringUtils.isEmpty(cloudVm.getSelfSubnetId())
				&&!baseCloudVm.getSelfSubnetId().equals(cloudVm.getSelfSubnetId())){
			openstackVmService.unbindPort(baseCloudVm.getDcId(), baseCloudVm.getPrjId(), baseCloudVm.getVmId(), baseCloudVm.getSelfPortId());
			InterfaceAttachment interAtt = openstackVmService.bindPort(baseCloudVm.getDcId(), baseCloudVm.getPrjId(), baseCloudVm.getVmId(), baseCloudVm.getNetId(), 
							cloudVm.getSelfSubnetId(), sgIds);
			
			baseCloudVm.setSelfSubnetId(cloudVm.getSelfSubnetId());
			baseCloudVm.setSelfPortId(interAtt.getPort_id());
			baseCloudVm.setSelfIp(interAtt.getFixed_ips()[0].getIp_address());
		}
		if(!StringUtils.isEmpty(baseCloudVm.getSelfSubnetId()) 
				&& StringUtils.isEmpty(cloudVm.getSelfSubnetId())){
			openstackVmService.unbindPort(baseCloudVm.getDcId(), baseCloudVm.getPrjId(), baseCloudVm.getVmId(), baseCloudVm.getSelfPortId());
			
			baseCloudVm.setSelfSubnetId(null);
			baseCloudVm.setSelfPortId(null);
			baseCloudVm.setSelfIp(null);
		}
		if(StringUtils.isEmpty(baseCloudVm.getSelfSubnetId()) 
				&& !StringUtils.isEmpty(cloudVm.getSelfSubnetId())){
			InterfaceAttachment interAtt = openstackVmService.bindPort(baseCloudVm.getDcId(), baseCloudVm.getPrjId(), baseCloudVm.getVmId(), baseCloudVm.getNetId(), 
					cloudVm.getSelfSubnetId(), sgIds);
	
			baseCloudVm.setSelfSubnetId(cloudVm.getSelfSubnetId());
			baseCloudVm.setSelfPortId(interAtt.getPort_id());
			baseCloudVm.setSelfIp(interAtt.getFixed_ips()[0].getIp_address());
		}
		
		cloudVmDao.saveOrUpdate(baseCloudVm);
	
	}
	
	/**
	 * 校验当前主机的受管子网是否可以切换
	 * <p>1. 没有绑定公网IP</p>
	 * <p>2. 没有关联负载均衡器的成员</p>
	 * <p>3. 没有作为端口映射的对应</p>
	 * <p>满足以上3点，返回false;否则 返回 true</p>
	 * ---------------------------------------
	 * @author zhouhaitao
	 * @param vm
	 * @return
	 */
	public boolean checkVmIpUsed (CloudVm vm){
		boolean isVmIpUsed = false;
		BaseCloudVm baseCloudVm = cloudVmDao.findOne(vm.getVmId());
		if(vm.getSubnetId().equals(baseCloudVm.getSubnetId())){
			return isVmIpUsed;
		}
		CloudFloatIp floatIp = queryFloatIpByVm(vm.getVmId());
		if(floatIp != null){
			isVmIpUsed = true;
			return isVmIpUsed;
		}
		
		List<BaseCloudPortMapping> portList = ecmcPortMappingService.queryPortMappingListByDestinyId(vm.getVmId());
		if(null != portList && portList.size()>0){
			isVmIpUsed = true;
			return isVmIpUsed;
		}
		
		isVmIpUsed = checkLdMemberByVm(vm.getVmId());
		return isVmIpUsed;
	}
	
	/**
	 * 查询云主机关联的安全组列表
	 * 
	 * @author zhouhaitao
	 * @param vmId
	 * @return
	 */
	private List<String> querySecurityGroupByVm(String vmId){
		List<String> sgList = new ArrayList<String>();
		StringBuffer sql = new StringBuffer();
		
		sql.append("	SELECT                  ");
		sql.append("		sg_id                 ");
		sql.append("	FROM                    ");
		sql.append("		cloud_vmsecuritygroup ");
		sql.append("	WHERE                   ");
		sql.append("		vm_id = ?             ");
		
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[] {vmId});
		@SuppressWarnings("rawtypes")
		List list = query.getResultList();
		if(null != list && list.size() > 0){
			for(int i=0;i<list.size();i++){
				
				sgList.add(String.valueOf(list.get(i)));
				
			}
		}
		return sgList;
	}
	
	/**
	 * 查询云主机对应的公网IP
	 * 
	 * @param vmId
	 * @return
	 */
	private CloudFloatIp queryFloatIpByVm(String vmId){
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

		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[] {vmId});
		@SuppressWarnings("rawtypes")
		List list = query.getResultList();
		if(null != list && list.size() == 1){
			floatIp = new CloudFloatIp();
			int index = 0 ;
			Object [] objs = (Object [])list.get(0);
			
			floatIp.setFloId(String.valueOf(objs[index++]));
			floatIp.setFloIp(String.valueOf(objs[index++]));
			floatIp.setDcId(String.valueOf(objs[index++]));
			floatIp.setPrjId(String.valueOf(objs[index++]));
		}
		return floatIp;
	}
	
	/**
	 * 查询云主机对应的负载均衡成员
	 * 
	 * @param vmId
	 * @return
	 */
	private boolean checkLdMemberByVm(String vmId){
		StringBuffer sql = new StringBuffer();
		sql.append(" select ");
		sql.append(" 		member_id	");
		sql.append(" from cloud_ldmember ");
		sql.append(" where  1=1 ");
		sql.append(" and vm_id= ? ");
		
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[] {vmId});
		@SuppressWarnings("rawtypes")
		List list = query.getResultList();
		return null != list && list.size()>0;
	}
	
	/**
	 * 修改云主机 对应系统盘的的回收站状态	
	 * 
	 * @author zhouhaitao	
	 * @param vmId
	 * @return
	 */
	private boolean modifySysDiskForRecycle(String vmId,String user,Date deleteTime){
		StringBuffer sql = new StringBuffer ();
		
		boolean isSuccess = false;
		try{
			sql.append(" update cloud_volume set ");
			sql.append("   delete_user =  ? ");
			sql.append(" , delete_time =  ? ");
			sql.append(" where vm_id = ? ");
			sql.append(" and vol_bootable = '1' ");
			cloudVmDao.execSQL(sql.toString(), new Object[]{user,deleteTime,vmId});
			
			isSuccess = true;
		}catch(Exception e){
			isSuccess = false;
			log.error(e.getMessage(),e);
		}
		return isSuccess;
	}
	
	/**
	 * 云主机放入回收站 计费队列(type = "recycle")
	 * 云主机回彻底删除 计费队列(type = "delete")
	 * 
	 * @param cloudVm
	 */
	public void vmOptionCharge(CloudVm cloudVm,String type) {
		ChargeRecord record = new ChargeRecord();
		String queueName = null;
		
		record.setResourceId(cloudVm.getVmId());
		record.setOpTime(cloudVm.getOpDate());
		record.setDatecenterId(cloudVm.getDcId());
		record.setCusId(cloudVm.getCusId());
		record.setResourceType(ResourceType.VM);
		
		if("recycle".equals(type)){
			record.setResourceName(cloudVm.getVmName());
			queueName = EayunQueueConstant.QUEUE_BILL_RESOURCE_RECYCLE;
		}
		else if("delete".equals(type)){
			record.setResourceName(cloudVm.getVmName());
			queueName = EayunQueueConstant.QUEUE_BILL_RESOURCE_DELETE;
		}
		else if("recover".equals(type)){
			queueName = EayunQueueConstant.QUEUE_BILL_RESOURCE_RECOVER;
		}
		rabbitTemplate.send(queueName, JSONObject.toJSONString(record));
	}
	
	/**
	 * 查询回收站云主机列表
	 * @param page
	 * @param queryMap
	 * @param dcId
	 * @param vmStatus
	 * @param queryType
	 * @param queryName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Page getRecycleVmPage(Page page, QueryMap queryMap, String dcId,
			String vmStatus, String queryType,
			String queryName){

		//todo数据盘容量
		log.info("查询云主机列表");
		List<Object> list = new ArrayList<Object>();
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append("vm.vm_id, ");
		sql.append("vm.vm_name, ");
		sql.append("vm.prj_id, ");
		sql.append("vm.dc_id, ");
		sql.append("vm.vm_status, ");
		sql.append("vm.create_time, ");
		sql.append("vm.end_time, ");
		sql.append("vm.from_imageid, ");
		sql.append("vm.vm_ip, ");
		sql.append("vm.sys_type, ");
		sql.append("vm.host_name, ");
		sql.append("dc.dc_name, ");
		sql.append("cus.cus_org, ");
		sql.append("prj.prj_name, ");
		sql.append("fla.flavor_vcpus, ");
		sql.append("fla.flavor_ram, ");
		sql.append("fla.flavor_disk, ");
		sql.append("vol.vol_size,  ");
		sql.append("vm.self_ip,  ");
		sql.append("vm.pay_type,  ");
		sql.append("vm.delete_time,  ");
		sql.append("net.net_name,  ");
		sql.append("subnet.subnet_name,  ");
		sql.append("selfSubnet.subnet_name as selfSubnetName,  ");
		sql.append("vm.charge_state ");
		sql.append("FROM cloud_vm vm ");
		sql.append("LEFT JOIN cloud_flavor fla ON vm.flavor_id = fla.flavor_id ");
		sql.append("AND vm.dc_id = fla.dc_id ");
		sql.append("LEFT JOIN cloud_project prj ON vm.prj_id = prj.prj_id ");
		sql.append("LEFT JOIN sys_selfcustomer cus ON prj.customer_id = cus.cus_id ");
		sql.append("LEFT JOIN dc_datacenter dc ON vm.dc_id = dc.id ");
		sql.append("LEFT JOIN ");
		sql.append("( ");
		sql.append("SELECT sum(vol_size) as vol_size,vm_id FROM cloud_volume WHERE vol_bootable = '0' GROUP BY vm_id ");
		sql.append(") ");
		sql.append("AS vol ON vm.vm_id=vol.vm_id ");
		sql.append(" LEFT JOIN cloud_subnetwork subnet on subnet.subnet_id = vm.subnet_id ");
		sql.append(" LEFT JOIN cloud_network net on net.net_id = vm.net_id ");
		sql.append(" LEFT JOIN cloud_subnetwork selfSubnet on selfSubnet.subnet_id = vm.self_subnetid ");
		sql.append("WHERE vm.is_deleted = '2' ");
		sql.append("AND vm.is_visable = '1' ");
		
		if(!"null".equals(dcId) && !"".equals(dcId) && !"undefined".equals(dcId)){
			sql.append("and vm.dc_id = ? ");
			list.add(dcId);
		}
		if(!"null".equals(vmStatus) && !"".equals(vmStatus) && !"undefined".equals(vmStatus)){
			if("1".equals(vmStatus)){
				sql.append("and vm.charge_state = ? ");
				list.add(vmStatus);
			}
			else if("2".equals(vmStatus)){
				sql.append("and (vm.charge_state = ? or vm.charge_state = '3') ");
				list.add(vmStatus);
			}
			else{
				sql.append("and vm.vm_status = ? ");
				list.add(vmStatus);
			}
		}
		if (null != queryName && !queryName.trim().equals("")) {
			if(queryType.equals("vmName")){
				//根据云主机名称模糊查询
				queryName = queryName.replaceAll("\\_", "\\\\_");
				sql.append(" and binary vm.vm_name like ?");
	            list.add("%" + queryName + "%");
	            
			}else if(queryType.equals("cusOrg")){
				//根据所属客户精确查询
				String[] cusOrgs = queryName.split(",");
				sql.append(" and ( ");
				for(String org:cusOrgs){
					sql.append(" binary cus.cus_org = ? or ");
					list.add(org);
				}
				sql.append(" 1 = 2 ) ");
				
			}else if(queryType.equals("prjName")){
				//根据项目名称精确查询
				String[] prjName = queryName.split(",");
				sql.append(" and ( ");
				for(String prj:prjName){
					sql.append(" binary prj.prj_name = ? or ");
					list.add(prj);
				}
				sql.append(" 1 = 2 ) ");
			}
			else{
				sql.append(" and  1 = 2 ");
			}
		}
		sql.append(" ORDER BY vm.delete_time DESC ");
		
		page = cloudVmDao.pagedNativeQuery(sql.toString(), queryMap, list.toArray());
		@SuppressWarnings("rawtypes")
		List resultList = (List) page.getResult();
		for(int i = 0; i < resultList.size(); i++){
			int index = 0;
			Object[] obj = (Object[]) resultList.get(i);
			CloudVm vm = new CloudVm();
			vm.setVmId(String.valueOf(obj[index++]));
			vm.setVmName(String.valueOf(obj[index++]));
			vm.setPrjId(String.valueOf(obj[index++]));
			vm.setDcId(String.valueOf(obj[index++]));
			vm.setVmStatus(String.valueOf(obj[index++]));
			vm.setCreateTime((Date)(obj[index++]));
			vm.setEndTime(((Date)obj[index++]));
			vm.setFromImageId(String.valueOf(obj[index++]));
			vm.setVmIp(String.valueOf(obj[index++]));
			
			String type = String.valueOf(obj[index++]) ;
			if(!StringUtils.isEmpty(type)&&!"null".equals(type)){
				vm.setSysType(DictUtil.getDataTreeByNodeId(type).getNodeName());
			}
			vm.setHostName(String.valueOf(obj[index++]));
			vm.setDcName(String.valueOf(obj[index++]));
			vm.setCusOrg(String.valueOf(obj[index++]));
			vm.setPrjName(String.valueOf(obj[index++]));
			vm.setCpus(Integer.parseInt(null != obj[index++] ? String.valueOf(obj[index-1]) : "0"));
			vm.setRams(Integer.parseInt(null != obj[index++] ? String.valueOf(obj[index-1]) : "0"));
			vm.setDisks(Integer.parseInt(null != obj[index++] ? String.valueOf(obj[index-1]) : "0"));
			vm.setDataCapacity(Integer.parseInt(null != obj[index++] ? String.valueOf(obj[index-1]) : "0"));
			vm.setSelfIp(String.valueOf(obj[index++]));
			vm.setPayType(String.valueOf(obj[index++]));
			vm.setDeleteTime(((Date)obj[index++]));
			vm.setNetName(String.valueOf(obj[index++]));
			vm.setSubnetName(String.valueOf(obj[index++] == null ? "" : obj[index-1]));
			vm.setSelfSubnetName(String.valueOf(obj[index++] == null ? "" : obj[index-1]));
			vm.setChargeState(String.valueOf(obj[index++]));
			
			if("BUILD".equalsIgnoreCase(vm.getVmStatus())){
				vm.setVmStatus("BUILDING");
			}
			vm.setVmStatusStr(DictUtil.getStatusByNodeEn("vm", vm.getVmStatus()));
			
			resultList.set(i, vm);
		}
		return page;
	}
	
	/**
	 * 判断项目的自定义镜像是否超过上限值
	 * 
	 * @param prjId
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private boolean checkImageCount(String prjId, int num) {
		boolean flag = false;
		StringBuffer sql = new StringBuffer();
		sql.append("  select count(1) from cloud_image where prj_id = ? and image_ispublic = '2' ");

		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[] { prjId });

		List list = query.getResultList();
		if (null != list && list.size() > 0) {
			BigInteger bi = (BigInteger) list.get(0);
			int count = bi.intValue();
			if (count >= num) {
				flag = true;
			}
		}
		return flag;
	}
	/**
	 * 查询回收站云主机的信息
	 * @param vmId
	 * @return
	 */
	public CloudVm getRecycleVmById(String vmId){
		CloudVm vm =null;

		StringBuffer sql = new StringBuffer();
		sql.append(" select ");
		sql.append("   	vm.vm_id as vmId,");
		sql.append("   	vm.vm_name as vmName,");
		sql.append("   	vm.prj_id as prjId,");
		sql.append("   	vm.dc_id as dcId,");
		sql.append("   	vm.create_time as createTime,");
		sql.append("   	vm.vm_Ip as vmIp ,");
		sql.append("   	flv.flavor_vcpus as cpus,");
		sql.append("   	flv.flavor_ram as rams, ");
		sql.append("   	flv.flavor_disk as disks,");
		sql.append("   	prj.prj_name as prjName,");
		sql.append("   	vm.sys_type as sysType ,");
		sql.append("   	vm.self_ip as selfIp ,");
		sql.append("   	vm.pay_type as payType ,");
		sql.append("   	vm.end_time as endTime,");
		sql.append("   	vm.delete_time as deleteTime,");
		sql.append("   	dc.dc_name as dcName,");
		sql.append("   	net.net_name as netName,");
		sql.append("   	sub.subnet_name as subnetName,");
		sql.append("   	selfSub.subnet_name as selfSubnetName,");
		sql.append("   	vm.vm_status ");
		sql.append("    ,cus.cus_org ");
		sql.append(" from cloud_vm vm ");
		sql.append(" left join cloud_flavor flv ON vm.flavor_id=flv.flavor_id");
		sql.append(" left join cloud_network net ON vm.net_id = net.net_id");
		sql.append(" left join cloud_subnetwork sub ON vm.subnet_id = sub.subnet_id");
		sql.append(" left join cloud_subnetwork selfSub ON vm.self_subnetid = selfSub.subnet_id");
		sql.append(" left join dc_datacenter dc ON vm.dc_id=dc.id");
		sql.append(" left join cloud_project prj ON vm.prj_id=prj.prj_id");
		sql.append(" LEFT JOIN sys_selfcustomer cus ON prj.customer_id = cus.cus_id ");
		sql.append(" where vm.is_deleted = '2'");
		sql.append(" and vm.is_visable = '1'");
		sql.append(" and vm.vm_id = ?");

		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[]{vmId});
		@SuppressWarnings("rawtypes")
		List newList = (List) query.getResultList();
		for (int i = 0; i < newList.size(); i++) {
			int ind =0;
			Object[] objs = (Object[]) newList.get(i);
			vm = new CloudVm();
			vm.setVmId(String.valueOf(objs[ind++]));
			vm.setVmName(String.valueOf(objs[ind++]));
			vm.setPrjId(String.valueOf(objs[ind++]));
			vm.setDcId(String.valueOf(objs[ind++]));
			vm.setCreateTime((Date) objs[ind++]);
			vm.setVmIp(String.valueOf(objs[ind++]));
			vm.setCpus(Integer.parseInt(objs[ind++] != null ? String.valueOf(objs[ind-1]) : "0"));
			vm.setRams(Integer.parseInt(objs[ind++] != null ? String.valueOf(objs[ind-1]) : "0"));
			vm.setDisks(Integer.parseInt(objs[ind++] != null ? String.valueOf(objs[ind-1]) : "0"));
			vm.setPrjName(String.valueOf(objs[ind++]));
			String sysType = String.valueOf(objs[ind++]);
			vm.setSelfIp(String.valueOf(objs[ind++]));
			vm.setPayType(String.valueOf(objs[ind++]));
			vm.setEndTime((Date) objs[ind++]);
			vm.setDeleteTime((Date) objs[ind++]);
			vm.setDcName(String.valueOf(objs[ind++]));
			vm.setNetName(String.valueOf(objs[ind++]));
			vm.setSubnetName(String.valueOf(objs[ind++] == null ? "" : objs[ind-1]));
			vm.setSelfSubnetName(String.valueOf(objs[ind++] == null ? "" : objs[ind-1]));
			vm.setVmStatus(String.valueOf(objs[ind++]));
			vm.setCusOrg(String.valueOf(objs[ind++]));
			vm.setVmStatusStr(DictUtil.getStatusByNodeEn("vm", vm.getVmStatus()));
			
			if (!StringUtils.isEmpty(sysType) && !"null".equals(sysType)) {
				vm.setSysType(DictUtil.getDataTreeByNodeId(sysType).getNodeName());
			}
		}
		return vm;
	}
	
	/**
	 * 云主机状态同步
	 * @param vmId
	 * @throws Exception 
	 */
	public void refreshStatus(String vmId) throws Exception{
		BaseCloudVm bcv = cloudVmDao.findOne(vmId);
		if(bcv != null){
			if(PayType.PAYAFTER.equals(bcv.getPayType()) && 
					CloudResourceUtil.CLOUD_CHARGESTATE_NSF_CODE.equals(bcv.getChargeState())){
				CloudProject project = projectService.findProject(bcv.getPrjId());
				MoneyAccount accountMoney = accountOverviewSerivce.getAccountBalance(project.getCustomerId());
				BigDecimal balanceMoney  = accountMoney.getMoney();
				String str= sdtService.getRenewCondition();
				BigDecimal openLimit = new BigDecimal(str);
				if(balanceMoney.compareTo(openLimit)<0){
					throw new AppException("账户余额不足，无法修改状态");
				}
				else{
					if("SHUTOFF".equals(bcv.getVmStatus())){
						CloudVm cloudVm = new CloudVm();
						cloudVm.setDcId(bcv.getDcId());
						cloudVm.setPrjId(bcv.getPrjId());
						cloudVm.setVmId(bcv.getVmId());
						
						openstackVmService.restartVm(cloudVm);
						
						bcv.setVmStatus("STARTING");//启动中
						
						JSONObject json =new JSONObject();
						json.put("vmId", bcv.getVmId());
						json.put("dcId",bcv.getDcId());
						json.put("prjId", bcv.getPrjId());
						json.put("vmStatus", bcv.getVmStatus());
						json.put("perStatus", "SHUTOFF");//已关机
						json.put("count", "0");
						json.put("isExsit", "0");
						
						final JSONObject data = json;
						TransactionHookUtil.registAfterCommitHook(new Hook() {
							@Override
							public void execute() {
								jedisUtil.addUnique(RedisKey.vmKey, data.toJSONString());
							}
							
						});
						
						cloudVm.setOpDate(new Date());
						cloudVm.setCusId(project.getCustomerId());
						vmOptionCharge(cloudVm, "recover");
					}
					bcv.setChargeState("0");
					cloudVmDao.saveOrUpdate(bcv);
				}
				
				
			}
		}
	}
	
	/**
	 * 查询云主机
	 * @param vmId
	 */
	public CloudVm get(String vmId){
		CloudVm cloudVm = null;
		BaseCloudVm bcv = cloudVmDao.findOne(vmId);
		if(null != bcv){
			cloudVm = new CloudVm();
			BeanUtils.copyPropertiesByModel(cloudVm, bcv);
		}
		return cloudVm;
	}
	
	

	/**
	 * 根据指定镜像id查询创建的云主机个数
	 */
	@Override
	public int countVmByImageId(String imageId) {
		int count=0;
		count=cloudVmDao.countVmByImageId(imageId);
		return count;
	}
}
