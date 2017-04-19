package com.eayun.schedule.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.eayun.database.configgroup.service.RdsDatastoreService;
import com.eayun.database.configgroup.thread.CloudRDSDatastoreInformationThread;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.sync.SyncProgressUtil;
import com.eayun.database.backup.service.CloudRDSBackupService;
import com.eayun.database.backup.thread.CloudRDSBackupThread;
import com.eayun.database.instance.service.CloudRDSInstanceService;
import com.eayun.database.instance.thread.resource.CloudRDSInstanceThread;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.notice.model.MessageStackSynFailResour;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.schedule.pool.SyncCloudResourcePool;
import com.eayun.schedule.service.CloudComputenodeService;
import com.eayun.schedule.service.CloudFirewallPolicyService;
import com.eayun.schedule.service.CloudFirewallRuleService;
import com.eayun.schedule.service.CloudFirewallService;
import com.eayun.schedule.service.CloudFloatIpService;
import com.eayun.schedule.service.CloudImageService;
import com.eayun.schedule.service.CloudLdMemberService;
import com.eayun.schedule.service.CloudLdMonitorService;
import com.eayun.schedule.service.CloudLdPoolService;
import com.eayun.schedule.service.CloudLdVipService;
import com.eayun.schedule.service.CloudLoadBalancerService;
import com.eayun.schedule.service.CloudNetworkService;
import com.eayun.schedule.service.CloudOutIpService;
import com.eayun.schedule.service.CloudPortMappingService;
import com.eayun.schedule.service.CloudProjectService;
import com.eayun.schedule.service.CloudResourceService;
import com.eayun.schedule.service.CloudRouterService;
import com.eayun.schedule.service.CloudSecurityGroupService;
import com.eayun.schedule.service.CloudSubnetService;
import com.eayun.schedule.service.CloudVmFlavorService;
import com.eayun.schedule.service.CloudVmService;
import com.eayun.schedule.service.CloudVolumeService;
import com.eayun.schedule.service.CloudVolumeSnapService;
import com.eayun.schedule.service.CloudVolumeTypeService;
import com.eayun.schedule.service.CloudVpnService;
import com.eayun.schedule.thread.resource.CloudComputeNodeThread;
import com.eayun.schedule.thread.resource.CloudDiskSnapshotThread;
import com.eayun.schedule.thread.resource.CloudDiskThread;
import com.eayun.schedule.thread.resource.CloudFirewallPolicyThread;
import com.eayun.schedule.thread.resource.CloudFirewallRuleThread;
import com.eayun.schedule.thread.resource.CloudFirewallThread;
import com.eayun.schedule.thread.resource.CloudFlavorThread;
import com.eayun.schedule.thread.resource.CloudFloatIpThread;
import com.eayun.schedule.thread.resource.CloudImageThread;
import com.eayun.schedule.thread.resource.CloudLbMemberThread;
import com.eayun.schedule.thread.resource.CloudLbMonitorThread;
import com.eayun.schedule.thread.resource.CloudLbPoolThread;
import com.eayun.schedule.thread.resource.CloudLbVipThread;
import com.eayun.schedule.thread.resource.CloudLoadBalancerThread;
import com.eayun.schedule.thread.resource.CloudNetworkThread;
import com.eayun.schedule.thread.resource.CloudOutIpThread;
import com.eayun.schedule.thread.resource.CloudPortMappingThread;
import com.eayun.schedule.thread.resource.CloudProjectThread;
import com.eayun.schedule.thread.resource.CloudRouterThread;
import com.eayun.schedule.thread.resource.CloudSecurityGroupThread;
import com.eayun.schedule.thread.resource.CloudSubnetThread;
import com.eayun.schedule.thread.resource.CloudVmThread;
import com.eayun.schedule.thread.resource.CloudVolumeTypeThread;
import com.eayun.schedule.thread.resource.CloudVpnThread;
import com.eayun.virtualization.model.BaseCloudProject;

/**
 * 数据中心同步
 * 
 * @author zhouhaitao
 * 
 */
@Transactional
@Service
@Scope("prototype")
public class CloudResourceServiceImpl implements CloudResourceService {
	private static final Log logger = LogFactory
			.getLog(CloudResourceServiceImpl.class);
	@Autowired
	private CloudProjectService bindProjectService;// 项目表
	@Autowired
	private CloudComputenodeService cloudComputenodeService;
	@Autowired
	private CloudNetworkService cloudNetworkService;
	@Autowired
	private CloudSecurityGroupService cloudSecurityGroupService;
	@Autowired
	private CloudVmService cloudVmService;
	@Autowired
	private CloudVolumeService cloudVolumeService;
	@Autowired
	private CloudVolumeSnapService cloudVolumeSnapService;
	@Autowired
	private CloudFloatIpService cloudFloatIpService;
	@Autowired
	private CloudImageService cloudImageService;
	@Autowired
	private CloudFirewallService cloudFirewallService;
	@Autowired
	private CloudFirewallPolicyService cloudFirewallPolicyService;
	@Autowired
	private CloudFirewallRuleService cloudFirewallRuleService;
	@Autowired
	private CloudVmFlavorService cloudVmFlavorService;
	@Autowired
	private CloudSubnetService cloudSubnetService;
	@Autowired
	private CloudRouterService cloudRouterService;
	@Autowired
	private CloudLdMemberService cloudLdMemberService;
	@Autowired
	private CloudLdPoolService cloudLdPoolService;
	@Autowired
	private CloudLdMonitorService cloudLdMonitorService;
	@Autowired
	private CloudLdVipService cloudLdVipService;
	@Autowired
	private CloudLoadBalancerService cloudLoadBalancerService;
	@Autowired
	private CloudOutIpService cloudOutIpService;
	@Autowired
	private CloudVpnService cloudVpnService;
	@Autowired
	private CloudPortMappingService cloudPortMappingService;
	@Autowired
	private CloudVolumeTypeService  cloudVolumeTypeService;
	@Autowired
	private CloudRDSInstanceService cloudRDSInstanceService;
    @Autowired
    private CloudRDSBackupService cloudRDSBackupService;
	@Autowired
	private RdsDatastoreService rdsDatastoreService ;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private MessageCenterService messageCenterService;
	@Autowired
	private SyncProgressUtil syncProgressUtil;
	
	@Override
	public void synchAllData(BaseDcDataCenter dataCenter) throws AppException {

		ExecutorService es = null;
		CountDownLatch cdl = null;
		try {
		    syncProgressUtil.init(dataCenter.getId());
			SyncCloudResourcePool pool = new SyncCloudResourcePool();
			es = pool.get();
			cdl = pool.getCountDownLatch(4);
			
			preposeSyncResource(es, dataCenter, cdl);
			
			cdl.await();
			
			syncResourceByDatacenter(es, dataCenter);
			
			syncByProjects(es, dataCenter);
			
			syncPoolFloatip(es, dataCenter);
			
			syncOutIp(es,dataCenter);
			
			syncVpn(es,dataCenter);
			
			syncPortMapping(es,dataCenter);
			
			sendMail();
			
		} catch (AppException e) {
		    logger.error(e.getMessage(),e);
		    //记录堆栈信息到redis
		    syncProgressUtil.saveExceptionStackTrace(dataCenter.getId(), e);
			throw e;
		} catch (Exception e) {
		    logger.error(e.getMessage(),e);
		    //记录堆栈信息到redis
		    syncProgressUtil.saveExceptionStackTrace(dataCenter.getId(), e);
			throw new AppException("", new String[] { "系统程序异常，请联系后台管理员!" });
		} finally {
		    if(es != null){
		        es.shutdown();
		    }
		}
	
	}
	
	/**
	 * 执行同步操作时，需要执行的前置同步资源（项目、安全组、网络、镜像）
	 * 
	 * @author zhouhaitao
	 * @date 2015-08-25
	 * @param es
	 * @param dataCenter
	 * @param cdl
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @throws Exception
	 */
	private void preposeSyncResource(ExecutorService es,
			BaseDcDataCenter dataCenter, CountDownLatch cdl) throws Exception {
		String flag = "";
		try {
		    String dcId = dataCenter.getId();
			// 同步数据中心下的项目
			CloudProjectThread cloudProjectThread = new CloudProjectThread(bindProjectService);
			cloudProjectThread.setDataCenter(dataCenter);
			cloudProjectThread.setCdl(cdl);
			Future<String> cloudProjectFuture = es.submit(cloudProjectThread);
			flag = cloudProjectFuture.get();
			syncProgressUtil.incrByDatacenterDone(dcId);
			logger.info("执行项目同步的结果：" + flag);
			if (!"success".equals(flag)) {
				throw new Exception();
			}

			// 同步数据中心下的网络
			CloudNetworkThread cloudNetworkThread = new CloudNetworkThread(cloudNetworkService);
			cloudNetworkThread.setCdl(cdl);
			cloudNetworkThread.setDataCenter(dataCenter);
			Future<String> cloudNetworkFuture = es.submit(cloudNetworkThread);
			flag = cloudNetworkFuture.get();
			logger.info("执行网络同步的结果：" + flag);
			syncProgressUtil.incrByDatacenterDone(dcId);
			if (!"success".equals(flag)) {
				throw new Exception();
			}

			// 同步数据中心下的安全组
			CloudSecurityGroupThread cloudSecurityGroupThread = new CloudSecurityGroupThread(cloudSecurityGroupService);
			cloudSecurityGroupThread.setCdl(cdl);
			cloudSecurityGroupThread.setDataCenter(dataCenter);
			Future<String> cloudSecurityFuture = es
					.submit(cloudSecurityGroupThread);
			flag = cloudSecurityFuture.get();
			logger.info("执行安全组同步的结果：" + flag);
			//安全组同步线程内部有安全组和安全组规则同步，所以incrByDatacenterDone放到线程方法内部中
			if (!"success".equals(flag)) {
				throw new Exception();
			}

			// 同步数据中心下的镜像
			CloudImageThread cloudImageThread = new CloudImageThread(cloudImageService);
			cloudImageThread.setCdl(cdl);
			cloudImageThread.setDataCenter(dataCenter);
			Future<String> cloudImageFuture = es.submit(cloudImageThread);
			flag = cloudImageFuture.get();
			logger.info("执行镜像同步的结果：" + flag);
			syncProgressUtil.incrByDatacenterDone(dcId);
			if (!"success".equals(flag)) {
				throw new Exception();
			}
			
			
			//同步数据中心下的云硬盘类型
	        CloudVolumeTypeThread cloudVolumeTypeThread = new CloudVolumeTypeThread(cloudVolumeTypeService);
	        cloudVolumeTypeThread.setDataCenter(dataCenter);
	        Future<String> cloudVolumeTypeFuture = es.submit(cloudVolumeTypeThread);
	        flag = cloudVolumeTypeFuture.get();
	        logger.info("执行云硬盘类型同步的结果：" + flag);
	        syncProgressUtil.incrByDatacenterDone(dcId);
	        if (!"success".equals(flag)) {
	            throw new Exception();
	        }
		   
			
		} catch (ExecutionException e) {
		    logger.error(e.getMessage(),e);
			if (e.getCause() instanceof AppException) {
				throw (AppException) e.getCause();
			}
			throw new Exception(e);
		}

	}

	/**
	 * 循环执行项目下的资源同步操作（云主机、云硬盘、云硬盘备份）
	 * 
	 * @author zhouhaitao
	 * @date 2015-08-25
	 * @param es
	 * @param dataCenter
	 */
	private void syncByProjects(ExecutorService es, BaseDcDataCenter dataCenter)
			throws Exception {
		List<BaseCloudProject> projects = bindProjectService
				.getAllProjectsByDcId(dataCenter.getId());// 获取本地库
		String flag = "";
		//设置按项目同步的项目总数
		syncProgressUtil.initByProjectTotal(dataCenter.getId(), projects == null ? 0L : projects.size());
		try {
			if (null != projects) {
				for (BaseCloudProject project : projects) {
				    syncProgressUtil.setProcessingProject(dataCenter.getId(), project.getProjectId(), project.getPrjName());
				    syncProgressUtil.clearByProjectResourcesProgress(dataCenter.getId());
					if(ConstantClazz.TROVE_MANAGED_TENANT.equals(project.getPrjName())){
					    //无需同步的项目，按项目同步的项目总数-1
					    syncProgressUtil.decrByProjectTotal(dataCenter.getId());
						continue;
					}
					// 同步项目下的云主机
					CloudVmThread cloudVmThread = new CloudVmThread(cloudVmService);
					cloudVmThread.setDataCenter(dataCenter);
					cloudVmThread.setProjectId(project.getProjectId());
					Future<String> cloudVmFuture = es.submit(cloudVmThread);
					flag = cloudVmFuture.get();
					logger.info("执行项目ID：" + project.getProjectId()
							+ "下的云主机同步的结果" + flag);
					if ("failed".equals(flag)) {
						throw new Exception();
					}

					// 同步项目下的云硬盘
					CloudDiskThread cloudDiskThread = new CloudDiskThread(cloudVolumeService);
					cloudDiskThread.setDataCenter(dataCenter);
					cloudDiskThread.setProjectId(project.getProjectId());
					Future<String> cloudDiskFuture = es.submit(cloudDiskThread);
					flag = cloudDiskFuture.get();
					logger.info("执行项目ID：" + project.getProjectId()
							+ "下的云硬盘同步的结果" + flag);
					if ("failed".equals(flag)) {
						throw new Exception();
					}
					
					

					// 同步数据中心下的浮动IP
					CloudFloatIpThread cloudFloatIpThread = new CloudFloatIpThread(cloudFloatIpService);
					cloudFloatIpThread.setDataCenter(dataCenter);
					cloudFloatIpThread.setProjectId(project.getProjectId());
					Future<String> cloudFloatIpFuture = es
							.submit(cloudFloatIpThread);
					flag = cloudFloatIpFuture.get();
					logger.info("执行项目ID：" + project.getProjectId()
							+ "下浮动Ip的同步结果：" + flag);
					if ("failed".equals(flag)) {
						throw new Exception();
					}

					//同步底层数据库版本Datastore信息
					CloudRDSDatastoreInformationThread cloudRDSDatastoreInformationThread = new CloudRDSDatastoreInformationThread(rdsDatastoreService);
					cloudRDSDatastoreInformationThread.setDcDataCenter(dataCenter);
					Future<String> cloudRDSDatastoreInformationFuture = es.submit(cloudRDSDatastoreInformationThread);
					flag = cloudRDSDatastoreInformationFuture.get();
					logger.info("执行底层Datastore数据库版本信息同步结果：" + flag);
					if ("failed".equals(flag)) {
						throw new Exception();
					}

					// 同步数据中心下的云数据库
					CloudRDSInstanceThread cloudRDSInstanceThread = new CloudRDSInstanceThread(cloudRDSInstanceService);
					cloudRDSInstanceThread.setDataCenter(dataCenter);
					cloudRDSInstanceThread.setProjectId(project.getProjectId());
					Future<String> cloudRDSInstanceFuture = es
							.submit(cloudRDSInstanceThread);
					flag = cloudRDSInstanceFuture.get();
					logger.info("执行项目ID：" + project.getProjectId()
							+ "下云数据库的同步结果：" + flag);
					if ("failed".equals(flag)) {
						throw new Exception();
					}

                    //同步数据中心下的云数据库备份
                    CloudRDSBackupThread cloudRDSBackupThread = new CloudRDSBackupThread(cloudRDSBackupService);
                    cloudRDSBackupThread.setDataCenter(dataCenter);
                    cloudRDSBackupThread.setProjectId(project.getProjectId());
                    Future<String> cloudRDSBackupFuture = es
                            .submit(cloudRDSBackupThread);
                    flag = cloudRDSBackupFuture.get();
                    logger.info("执行项目ID：" + project.getProjectId()
                            + "下云数据库备份的同步结果：" + flag);
                    if ("failed".equals(flag)) {
                        throw new Exception();
                    }
					syncProgressUtil.incrByProjectDone(dataCenter.getId());
				}
				// 同步项目下的云硬盘备份
				CloudDiskSnapshotThread cloudDiskSnapThread = new CloudDiskSnapshotThread(cloudVolumeSnapService);
				cloudDiskSnapThread.setDataCenter(dataCenter);
				Future<String> cloudDiskSnapFuture = es
						.submit(cloudDiskSnapThread);
				flag = cloudDiskSnapFuture.get();
				logger.info("执行数据中心ID：" + dataCenter.getId()
						+ "下的云硬盘备份同步的结果" + flag);
				syncProgressUtil.incrByDatacenterDone(dataCenter.getId());
				if ("failed".equals(flag)) {
					throw new Exception();
				}
			}
		} catch (ExecutionException e) {
		    logger.error(e.getMessage(),e);
			if (e.getCause() instanceof AppException) {
				throw (AppException) e.getCause();
			}
			throw new Exception(e);
		}

	}

	/**
	 * 同步数据中心下项目无关的资源 （计算节点、防火墙、子网、负载均衡、路由）
	 * 
	 * @author zhouhaitao
	 * @date 2015-08-25
	 * @param es
	 * @param dataCenter
	 * @throws Exception
	 */
	private void syncResourceByDatacenter(ExecutorService es,
			BaseDcDataCenter dataCenter) throws Exception {
		String flag = "";
		try {
		    String dcId = dataCenter.getId();
			CloudComputeNodeThread cloudComputeNodeThread = new CloudComputeNodeThread(cloudComputenodeService);
			// 同步数据中心下的计算节点
			cloudComputeNodeThread.setDataCenter(dataCenter);
			Future<String> cloudComputeNodeFuture = es
					.submit(cloudComputeNodeThread);
			flag = cloudComputeNodeFuture.get();
			logger.info("执行计算节点同步的结果：" + flag);
			syncProgressUtil.incrByDatacenterDone(dcId);
			if ("failed".equals(flag)) {
				throw new Exception();
			}

			// 同步数据中心的防火墙
			CloudFirewallThread cloudFirewallThread = new CloudFirewallThread(cloudFirewallService);
			cloudFirewallThread.setDataCenter(dataCenter);
			Future<String> cloudFirewallFuture = es.submit(cloudFirewallThread);
			flag = cloudFirewallFuture.get();
			logger.info("执行防火墙同步的结果：" + flag);
			syncProgressUtil.incrByDatacenterDone(dcId);
			if ("failed".equals(flag)) {
				throw new Exception();
			}

			// 同步数据中心下的防火墙策略
			CloudFirewallPolicyThread cloudFirewallPolicyThread = new CloudFirewallPolicyThread(cloudFirewallPolicyService);
			cloudFirewallPolicyThread.setDataCenter(dataCenter);
			Future<String> cloudFirewallPolicyFuture = es
					.submit(cloudFirewallPolicyThread);
			flag = cloudFirewallPolicyFuture.get();
			logger.info("执行防火墙策略同步的结果：" + flag);
			syncProgressUtil.incrByDatacenterDone(dcId);
			if ("failed".equals(flag)) {
				throw new Exception();
			}

			// 同步数据中心下的防火墙规则
			CloudFirewallRuleThread cloudFirewallRuleThread = new CloudFirewallRuleThread(cloudFirewallRuleService);
			cloudFirewallRuleThread.setDataCenter(dataCenter);
			Future<String> cloudFirewallRuleFuture = es
					.submit(cloudFirewallRuleThread);
			flag = cloudFirewallRuleFuture.get();
			logger.info("执行防火墙规则同步的结果：" + flag);
			syncProgressUtil.incrByDatacenterDone(dcId);
			if ("failed".equals(flag)) {
				throw new Exception();
			}

			// 同步数据中心下的云主机类型
			CloudFlavorThread cloudFlavorThread = new CloudFlavorThread(cloudVmFlavorService);
			cloudFlavorThread.setDataCenter(dataCenter);
			Future<String> cloudFlavorFuture = es.submit(cloudFlavorThread);
			flag = cloudFlavorFuture.get();
			logger.info("执行云主机类型同步的结果：" + flag);
			syncProgressUtil.incrByDatacenterDone(dcId);
			if ("failed".equals(flag)) {
				throw new Exception();
			}

			// 同步数据中心下的子网
			CloudSubnetThread cloudSubnetThread = new CloudSubnetThread(cloudSubnetService);
			cloudSubnetThread.setDataCenter(dataCenter);
			Future<String> cloudSubnetFuture = es.submit(cloudSubnetThread);
			flag = cloudSubnetFuture.get();
			logger.info("执行子网同步的结果：" + flag);
			syncProgressUtil.incrByDatacenterDone(dcId);
			if ("failed".equals(flag)) {
				throw new Exception();
			}

			// 同步数据中心下的路由
			CloudRouterThread cloudRouterThread = new CloudRouterThread(cloudRouterService);
			cloudRouterThread.setDataCenter(dataCenter);
			Future<String> cloudRouterFuture = es.submit(cloudRouterThread);
			flag = cloudRouterFuture.get();
			logger.info("执行路由同步的结果：" + flag);
			syncProgressUtil.incrByDatacenterDone(dcId);
			if ("failed".equals(flag)) {
				throw new Exception();
			}

			// 同步数据中心的负载均衡成员
			CloudLbMemberThread cloudLbMemberThread = new CloudLbMemberThread(cloudLdMemberService);
			cloudLbMemberThread.setDataCenter(dataCenter);
			Future<String> cloudLbMemberFuture = es.submit(cloudLbMemberThread);
			flag = cloudLbMemberFuture.get();
			logger.info("执行负载均衡成员同步的结果：" + flag);
			syncProgressUtil.incrByDatacenterDone(dcId);
			if ("failed".equals(flag)) {
				throw new Exception();
			}

			// 同步数据中心的负载均衡资源池
			CloudLbPoolThread cloudLbPoolThread = new CloudLbPoolThread(cloudLdPoolService);
			cloudLbPoolThread.setDataCenter(dataCenter);
			Future<String> cloudLbPoolFuture = es.submit(cloudLbPoolThread);
			flag = cloudLbPoolFuture.get();
			logger.info("执行负载均衡资源池同步的结果：" + flag);
			syncProgressUtil.incrByDatacenterDone(dcId);
			if ("failed".equals(flag)) {
				throw new Exception();
			}

			// 同步数据中心的负载均衡监控
			CloudLbMonitorThread cloudLbMonitorThread = new CloudLbMonitorThread(cloudLdMonitorService);
			cloudLbMonitorThread.setDataCenter(dataCenter);
			Future<String> cloudLbMonitorFuture = es
					.submit(cloudLbMonitorThread);
			flag = cloudLbMonitorFuture.get();
			logger.info("执行负载均衡监控同步的结果：" + flag);
			syncProgressUtil.incrByDatacenterDone(dcId);
			if ("failed".equals(flag)) {
				throw new Exception();
			}

			// 同步数据中心的负载均衡VIP
			CloudLbVipThread cloudLbVipThread = new CloudLbVipThread(cloudLdVipService);
			cloudLbVipThread.setDataCenter(dataCenter);
			Future<String> cloudLbVipFuture = es.submit(cloudLbVipThread);
			flag = cloudLbVipFuture.get();
			logger.info("执行负载均衡VIP同步的结果：" + flag);
			syncProgressUtil.incrByDatacenterDone(dcId);
			if ("failed".equals(flag)) {
				throw new Exception();
			}
			logger.info("------------------------底层数据同步结束-----------------------");
		} catch (ExecutionException e) {
		    logger.error(e.getMessage(),e);
			if (e.getCause() instanceof AppException) {
				throw (AppException) e.getCause();
			}
			throw new Exception(e);
		}
	}

	/**
	 * 同步数据中心下负载均衡器与浮动IP的关系
	 * 
	 * @author zhouhaitao
	 * @date 2015-08-25
	 * @param es
	 * @param dataCenter
	 * @throws Exception
	 */
	private void syncPoolFloatip(ExecutorService es,
			BaseDcDataCenter dataCenter) throws Exception {
		String flag = "";
		try {
			// 同步数据中心下负载均衡器与浮动IP的关系
			CloudLoadBalancerThread cloudLoadBalancerThread = new CloudLoadBalancerThread(cloudLoadBalancerService);
			cloudLoadBalancerThread.setDataCenter(dataCenter);
			Future<String> cloudLoadBalancerFuture = es
					.submit(cloudLoadBalancerThread);
			flag = cloudLoadBalancerFuture.get();
			logger.info("执行负载均衡器绑定公网IP同步的结果：" + flag);
			syncProgressUtil.incrByDatacenterDone(dataCenter.getId());
			if ("failed".equals(flag)) {
				throw new Exception();
			}
		} catch (ExecutionException e) {
		    logger.error(e.getMessage(),e);
			if (e.getCause() instanceof AppException) {
				throw (AppException) e.getCause();
			}
			throw new Exception(e);
		}
	}
	
	/**
	 * 同步数据中心下OutIp
	 * 
	 * @author zhouhaitao
	 * @date 2016-05-11
	 * @param es
	 * @param dataCenter
	 * @throws Exception
	 */
	private void syncOutIp(ExecutorService es,
			BaseDcDataCenter dataCenter) throws Exception {
		String flag = "";
		try {
			// 同步数据中心下负载均衡器与浮动IP的关系
			CloudOutIpThread cloudOutIpThread = new CloudOutIpThread(cloudOutIpService);
			cloudOutIpThread.setDataCenter(dataCenter);
			Future<String> cloudOutIpFuture = es
					.submit(cloudOutIpThread);
			flag = cloudOutIpFuture.get();
			logger.info("执行OutIp同步的结果：" + flag);
			syncProgressUtil.incrByDatacenterDone(dataCenter.getId());
			if ("failed".equals(flag)) {
				throw new Exception();
			}
		} catch (ExecutionException e) {
		    logger.error(e.getMessage(),e);
			if (e.getCause() instanceof AppException) {
				throw (AppException) e.getCause();
			}
			throw new Exception(e);
		}
	}
	/**
	 * 同步数据中心下的vpn资源
	 * @author gaoxiang
	 * @date 2016-09-01
	 * @param es
	 * @param dataCenter
	 * @throws Exception
	 */
	private void syncVpn(ExecutorService es, BaseDcDataCenter dataCenter) throws Exception {
	    String flag = "";
	    try {
	        CloudVpnThread cloudVpnThread = new CloudVpnThread(cloudVpnService);
	        cloudVpnThread.setDataCenter(dataCenter);
	        Future<String> cloudVpnFuture = es.submit(cloudVpnThread);
	        flag = cloudVpnFuture.get();
	        logger.info("执行VPN同步的结果：" + flag);
	        syncProgressUtil.incrByDatacenterDone(dataCenter.getId());
	        if ("failed".equals(flag)) {
	            throw new Exception();
	        }
	    } catch (ExecutionException e) {
	        logger.error(e.getMessage(),e);
	        if (e.getCause() instanceof AppException) {
                throw (AppException) e.getCause();
            }
            throw new Exception(e);
	    }
	}
	/**
     * 同步数据中心下的端口映射资源
     * @author gaoxiang
     * @date 2016-09-02
     * @param es
     * @param dataCenter
     * @throws Exception
     */
	private void syncPortMapping(ExecutorService es, BaseDcDataCenter dataCenter) throws Exception {
	    String flag = "";
	    try {
	        CloudPortMappingThread cloudPortMappingThread = new CloudPortMappingThread(cloudPortMappingService);
	        cloudPortMappingThread.setDataCenter(dataCenter);
            Future<String> cloudPortMappingFuture = es.submit(cloudPortMappingThread);
            flag = cloudPortMappingFuture.get();
            logger.info("执行端口映射同步的结果：" + flag);
            syncProgressUtil.incrByDatacenterDone(dataCenter.getId());
            if ("failed".equals(flag)) {
                throw new Exception();
            }
	    } catch (ExecutionException e) {
	        logger.error(e.getMessage(),e);
            if (e.getCause() instanceof AppException) {
                throw (AppException) e.getCause();
            }
            throw new Exception(e);
	    }
	}
	

	
	private void sendMail() throws Exception{
		logger.info("发送底层已经删除的资源");
		List<MessageStackSynFailResour> deleteResources = new ArrayList<MessageStackSynFailResour>();
		List<String> resources = jedisUtil.getListByRange(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, 0, -1);
		if(null != resources && resources.size() >0){
			for(String resource:resources){
				MessageStackSynFailResour delResource = new MessageStackSynFailResour();
				JSONObject json = JSONObject.parseObject(resource);
				delResource.setResourID(json.getString("resourceId"));
				delResource.setResourName(json.getString("resourceName"));
				delResource.setResourtype(json.getString("resourceType"));
				delResource.setSynTime(json.getDate("synTime"));
				deleteResources.add(delResource);
			}
			
			messageCenterService.stackSynFail(deleteResources);
			jedisUtil.delete(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE);
		}
	}
}
