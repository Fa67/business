package com.eayun.virtualization.service.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
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

import com.eayun.accesskey.service.AccessKeyService;
import com.eayun.charge.bean.ResourceCheckBean;
import com.eayun.charge.model.ChargeRecord;
import com.eayun.charge.service.ChargeRecordService;
import com.eayun.common.constant.PayType;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.common.zk.DistributedLockBean;
import com.eayun.common.zk.DistributedLockService;
import com.eayun.common.zk.LockService;
import com.eayun.customer.model.CusServiceState;
import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.database.instance.service.RDSInstanceService;
import com.eayun.notice.model.MessagePayAsYouGoResourcesStopModel;
import com.eayun.notice.model.MessagePayUpperLimitResourcesStopModel;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.obs.service.ObsOpenService;
import com.eayun.project.service.ProjectService;
import com.eayun.syssetup.service.SysDataTreeService;
import com.eayun.virtualization.bean.AboutToExpire;
import com.eayun.virtualization.dao.CloudVmDao;
import com.eayun.virtualization.model.CloudFloatIp;
import com.eayun.virtualization.model.CloudLdPool;
import com.eayun.virtualization.model.CloudNetWork;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.model.CloudSnapshot;
import com.eayun.virtualization.model.CloudVm;
import com.eayun.virtualization.model.CloudVolume;
import com.eayun.virtualization.model.CloudVpn;
import com.eayun.virtualization.service.CloudFloatIpService;
import com.eayun.virtualization.service.NetWorkService;
import com.eayun.virtualization.service.PoolService;
import com.eayun.virtualization.service.ResourceDisposeService;
import com.eayun.virtualization.service.SnapshotService;
import com.eayun.virtualization.service.VmService;
import com.eayun.virtualization.service.VolumeService;
import com.eayun.virtualization.service.VpnService;

/**
 * 云资源到期欠费处理实现类
 * 
 * @author xiangyu.cao@eayun.com
 *
 */
@Service
public class ResourceDisposeServiceImpl implements ResourceDisposeService {
	private static final Logger log = LoggerFactory
			.getLogger(ResourceDisposeServiceImpl.class);
	@Autowired
	private VmService vmService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private CloudFloatIpService cloudFloatIpService;
	@Autowired
	private VolumeService volumeService;
	@Autowired
	private NetWorkService netWorkService;
	@Autowired
	private PoolService poolService;
	@Autowired
	private VpnService vpnService;
	@Autowired
	private AccessKeyService accessKeyService;
	@Autowired
	private SnapshotService snapshotService;
	@Autowired
	private MessageCenterService messageCenterService;
	@Autowired
	private ChargeRecordService chargeRecordService;
	@Autowired
	private CloudVmDao cloudVmDao;
	@Autowired
	private ObsOpenService obsOpenService;
	@Autowired
	private SysDataTreeService sysDataTreeService;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private DistributedLockService distributedLockService;
	@Autowired
	private RDSInstanceService rdsInstanceService;
	
	@Override
	public void resourceExpiration() throws Exception {
		Date endTime = getTodayZero();
		log.info("开始执行预付费资源到期处理,endTime:"+endTime);
		modifyForVmByExpiration(endTime); // 云主机到期处理 √
		modifyForVolumeByExpiration(endTime); // 云硬盘到期处理
		modifyForNetWorkByExpiration(endTime); // 私有网络到期处理
		modifyForLdPoolByExpiration(endTime); // 负载均衡到期处理
		modifyForFloadIpByExpiration(endTime); // 公网ip到期处理
		modifyForVpnByExpiration(endTime); // vpn到期处理
		modifyForRDSByExpiration(endTime);//rds到期处理
	}

	private void modifyForRDSByExpiration(Date endTime) throws Exception{
		List<CloudRDSInstance> list=rdsInstanceService.queryNormalRdsInstance(null, endTime, "0",  PayType.PAYBEFORE, null);
		log.info("开始执行预付费RDS到期处理,RDSSize:"+list.size());
		for (CloudRDSInstance cloudRDSInstance : list) {
			try {
				log.info("开始执行预付费RDS到期处理,RDSId:"+cloudRDSInstance.getRdsId());
				if(cloudRDSInstance!=null&&!StringUtil.isEmpty(cloudRDSInstance.getRdsId())){
					rdsInstanceService.modifyStateForRdsInstance(cloudRDSInstance.getRdsId(), "2", null,false,false);
				}
			} catch (Exception e) {
				log.error("预付费RDS到期处理失败,rdsId:"+cloudRDSInstance.getRdsId(),e);
			}
		}
	}

	@Override
	public void resourceExceed() throws Exception {
		Date endTime = getNowZero();
		int hours = Integer.parseInt(sysDataTreeService.getRecoveryTime());
		endTime = DateUtil.addDay(endTime, new int[] { 0, 0, 0, -hours });
		log.info("开始执行预付资源超过保留时长处理,endTime:"+endTime);
		modifyForVmByExceed(endTime); // 云主机过期处理√
		modifyForVolumeByExceed(endTime); // 云硬盘过期处理
		modifyForNetWorkByExceed(endTime); // 私有网络过期处理
		modifyForLdPoolByExceed(endTime); // 负载均衡过期处理
		modifyForFloadIpByExceed(endTime); // 公网ip过期处理
		modifyForVpnByExceed(endTime);//vpn过期处理
		modifyForRDSByExceed(endTime);//RDS过期处理
	}

	private void modifyForRDSByExceed(Date endTime) throws Exception{
		List<CloudRDSInstance> list=rdsInstanceService.queryNormalRdsInstance(null, endTime, "2", PayType.PAYBEFORE, "0");
		log.info("开始执行预付费RDS超过保留时长处理,RDSSize:"+list.size());
		for (CloudRDSInstance cloudRDSInstance : list) {
			log.info("开始执行预付费rds超过保留时长处理,rdsId:"+cloudRDSInstance.getRdsId());
			try {
				if(cloudRDSInstance!=null&&!StringUtil.isEmpty(cloudRDSInstance.getRdsId())){
					rdsInstanceService.modifyStateForRdsInstance(cloudRDSInstance.getRdsId(), "3", endTime,false,false);
				}
			} catch (Exception e) {
				log.error("rds超过保留时长处理失败,rdsId:"+cloudRDSInstance.getRdsId(),e);
			}
		}
	}

	private void modifyForVpnByExceed(Date endTime) {
		List<CloudVpn> list = vpnService.findVpnByCharge(null, "2",
				PayType.PAYBEFORE, endTime);
		log.info("开始执行预付费vpn超过保留时长处理,VPNSize:"+list.size());
		for (CloudVpn cloudVpn : list) {
			log.info("开始执行预付费vpn超过保留时长处理,VPNId:"+cloudVpn.getVpnId());
			try {
				if(cloudVpn!=null&&!StringUtil.isEmpty(cloudVpn.getVpnId())){
					vpnService.modifyStateForVPN(cloudVpn.getVpnId(), "3", null, false,
							false);
				}
			} catch (Exception e) {
				log.error("vpn超过保留时长处理失败,vpnId:"+cloudVpn.getVpnId(),e);
			}
		}
	}

	/**
	 * vpn到期处理
	 * 
	 * @param endTime
	 * @throws Exception
	 */
	private void modifyForVpnByExpiration(Date endTime) throws Exception {
		List<CloudVpn> list = vpnService.findVpnByCharge(null, "0",
				PayType.PAYBEFORE, endTime);
		log.info("开始执行预付费vpn到期处理,VPNSize:"+list.size());
		for (CloudVpn cloudVpn : list) {
			try {
				log.info("开始执行预付费vpn到期处理,VPNId:"+cloudVpn.getVpnId());
				if(cloudVpn!=null&&!StringUtil.isEmpty(cloudVpn.getVpnId())){
					vpnService.modifyStateForVPN(cloudVpn.getVpnId(), "2", null, false,
							false);
				}
			} catch (Exception e) {
				log.error("预付费vpn到期处理失败,vpnId:"+cloudVpn.getVpnId(),e);
			}
		}
	}

	/**
	 * 公网ip到期处理
	 * 
	 * @param endTime
	 */
	private void modifyForFloadIpByExpiration(Date endTime) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("endTime", endTime);
		map.put("prjId", null);
		map.put("isDelete", "0");
		map.put("chargeState", "0");
		map.put("payType", PayType.PAYBEFORE);
		List<CloudFloatIp> list = cloudFloatIpService.getCloudFloatIpByMap(map);
		log.info("开始执行预付费公网Ip到期处理,FloatIpSize:"+list.size());
		for (CloudFloatIp cloudFloatIp : list) {
			try {
				log.info("开始执行预付费公网Ip到期处理,FloatIpId:"+cloudFloatIp.getFloId());
				if(cloudFloatIp!=null&&!StringUtil.isEmpty(cloudFloatIp.getFloId())){
					cloudFloatIpService.modifyStateForFloatIp(cloudFloatIp.getFloId(),
							"2", null);
				}
			} catch (Exception e) {
				log.error("公网Ip到期处理失败,FloatIpId:"+cloudFloatIp.getFloId(),e);
			}
		}
	}

	/**
	 * 负载均衡到期处理
	 * 
	 * @param endTime
	 */
	private void modifyForLdPoolByExpiration(Date endTime) {
		List<CloudLdPool> list = poolService.findLdPoolByCharge(null, "0",
				PayType.PAYBEFORE, endTime);
		log.info("开始执行预付费负载均衡到期处理,LdPoolSize:"+list.size());
		for (CloudLdPool cloudLdPool : list) {
			try {
				log.info("开始执行预付费负载均衡到期处理,LdPoolId:"+cloudLdPool.getPoolId());
				if(cloudLdPool!=null&&!StringUtil.isEmpty(cloudLdPool.getPoolId())){
					poolService.modifyStateForLdPool(cloudLdPool.getPoolId(), "2",
							null, false, false);
				}
			} catch (Exception e) {
				log.error("负载均衡到期处理失败,LdPoolId:"+cloudLdPool.getPoolId(),e);
			}
		}
	}

	/**
	 * 私有网络到期处理
	 * 
	 * @param endTime
	 * @throws Exception
	 */
	private void modifyForNetWorkByExpiration(Date endTime) throws Exception {
		List<CloudNetWork> list = netWorkService.findNetWorkByCharge(null, "0",
				PayType.PAYBEFORE, endTime);
		log.info("开始执行预付费私有网络到期处理,netSize:"+list.size());
		for (CloudNetWork cloudNetWork : list) {
			try {
				log.info("开始执行预付费私有网络到期处理,netId:"+cloudNetWork.getNetId());
				if(cloudNetWork!=null&&!StringUtil.isEmpty(cloudNetWork.getNetId())){
					netWorkService.modifyStateForNetWork(cloudNetWork.getNetId(), "2",
							null, false, false);
				}
			} catch (Exception e) {
				log.error("私有网络到期处理失败,netId:"+cloudNetWork.getNetId(),e);
			}
		}
	}

	/**
	 * 云硬盘到期处理
	 * 
	 * @param endTime
	 * @throws Exception
	 */
	private void modifyForVolumeByExpiration(Date endTime) throws Exception {
		List<CloudVolume> cloudVolumeList = volumeService.getVolumesBySome(
				endTime, "0", "0",true, PayType.PAYBEFORE, null, "0"); // 获取cloud_volum表中到期时间<=今天0点的状态正常的未被删除的预付费数据盘信息
		log.info("开始执行预付费云硬盘到期处理,volSize:"+cloudVolumeList.size());
		for (CloudVolume cloudVolume : cloudVolumeList) {
			try {
				log.info("开始执行预付费云硬盘到期处理,volId:"+cloudVolume.getVolId());
				if(cloudVolume!=null&&!StringUtil.isEmpty(cloudVolume.getVolId())){
					volumeService.modifyStateForVol(cloudVolume.getVolId(), "2", false);
				}
			} catch (Exception e) {
				log.error("云硬盘到期处理失败,volId:"+cloudVolume.getVolId(),e);
			}
		}
	}

	/**
	 * 云主机到期处理
	 * 
	 * @param endTime
	 * @throws Exception
	 */
	private void modifyForVmByExpiration(Date endTime) throws Exception {
		List<CloudVm> cloudVmList = vmService.queryNormalVm(null, endTime, "0",
				false,true, PayType.PAYBEFORE, null); // 获取cloud_vm表中到期时间<=今天0点的状态为正常的未被删除(或者在回收站)的预付费主机信息
		log.info("开始执行预付费云主机到期处理,vmSize:"+cloudVmList.size());
		for (CloudVm cloudVm : cloudVmList) {
			try {
				log.info("开始执行预付费云主机到期处理,vmId:"+cloudVm.getVmId());
				if(cloudVm!=null&&!StringUtil.isEmpty(cloudVm.getVmId())){
					vmService.modifyStateForVm(cloudVm.getVmId(), "2", null, false,
							false); // 修改主机状态为已过期
					CloudVolume cloudVolume = volumeService.getOsVolumeByVmId(cloudVm
							.getVmId()); // 获取该主机下的系统盘
					if(cloudVolume!=null&&!StringUtil.isEmpty(cloudVolume.getVolId())){
						volumeService.modifyStateForVol(cloudVolume.getVolId(), "2", false); // 修改改主机下的系统盘的状态为已过期
					}
				}
			} catch (Exception e) {
				log.error("云主机到期处理失败,vmId:"+cloudVm.getVmId(),e);
			}
		}
	}

	/**
	 * 公网ip过期处理(超过保留时长)
	 * 
	 * @param endTime
	 * @throws Exception
	 */
	private void modifyForFloadIpByExceed(Date endTime) throws Exception {
		
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("endTime", endTime);
			map.put("prjId", null);
			map.put("isDelete", "0");
			map.put("chargeState", "2");
			map.put("payType", PayType.PAYBEFORE);
			List<CloudFloatIp> list = cloudFloatIpService.getCloudFloatIpByMap(map);
			log.info("开始执行预付费公网ip超过保留时长处理,floatIpSize:"+list.size());
			for (CloudFloatIp cloudFloatIp : list) {
				try {
					log.info("开始执行预付费公网ip超过保留时长处理,floatIpId:"+cloudFloatIp.getFloId());
					String resourceId = cloudFloatIp.getResourceId();
					String resourceType = cloudFloatIp.getResourceType();
					if (!StringUtil.isEmpty(resourceId)
							&& !StringUtil.isEmpty(resourceType)) {
						cloudFloatIpService.unbundingResource(cloudFloatIp);
					}
					cloudFloatIpService.releaseFloatIp(cloudFloatIp);
				} catch (Exception e) {
					log.error("预付费公网ip超过保留时长处理失败,floatIpId:"+cloudFloatIp.getFloId(),e);
				}
			}
		
	}

	/**
	 * 负载均衡过期处理(超过保留时长)
	 * 
	 * @param endTime
	 * @throws Exception
	 */
	private void modifyForLdPoolByExceed(Date endTime) throws Exception {
		List<CloudLdPool> list = poolService.findLdPoolByCharge(null, "2",
				PayType.PAYBEFORE, endTime);
		log.info("开始执行预付费负载均衡超过保留时长处理,ldPoolSize:"+list.size());
		for (CloudLdPool cloudLdPool : list) {
			try {
				log.info("开始执行预付费负载均衡超过保留时长处理,ldPoolId:"+cloudLdPool.getPoolId());
				poolService.modifyStateForLdPool(cloudLdPool.getPoolId(), "3",
							null, true, false);
			} catch (Exception e) {
				log.error("预付费负载均衡超过保留时长处理失败,ldPoolId:"+cloudLdPool.getPoolId(),e);
			}
		}
	}

	/**
	 * 私有网络过期处理(超过保留时长)
	 * 
	 * @param endTime
	 * @throws Exception
	 */
	private void modifyForNetWorkByExceed(Date endTime) throws Exception {
		// 获取cloud_network表中到期时间<=今天0点的状态为已到期的预付费的私有网络信息
		List<CloudNetWork> list = netWorkService.findNetWorkByCharge(null, "2",
				PayType.PAYBEFORE, endTime);
		log.info("开始执行预付费私有网络超过保留时长处理,netSize:"+list.size());
		for (CloudNetWork cloudNetWork : list) {
			try {
				log.info("开始执行预付费私有网络超过保留时长处理,netId:"+cloudNetWork.getNetId());
				netWorkService.modifyStateForNetWork(cloudNetWork.getNetId(), "3",
							null, true, false);
			} catch (Exception e) {
				log.error("预付费私有网络超过保留时长处理失败,netId:"+cloudNetWork.getNetId(),e);
			}
		}
	}

	/**
	 * 云硬盘过期处理(超过保留时长)
	 * 
	 * @param endTime
	 */
	private void modifyForVolumeByExceed(Date endTime) throws Exception {
			List<CloudVolume> cloudVolumeList = volumeService.getVolumesBySome(
					endTime, "2", "0",false, PayType.PAYBEFORE, null, "0");// 获取cloud_volum表中到期时间<=今天0点的状态正常的未被删除的预付费数据盘信息
			log.info("开始执行预付费云硬盘超过保留时长处理,volSize:"+cloudVolumeList.size());
			for (CloudVolume cloudVolume : cloudVolumeList) {
				try {
					log.info("开始执行预付费云硬盘超过保留时长处理,volId:"+cloudVolume.getVolId());
					volumeService.modifyStateForVol(cloudVolume.getVolId(), "3", true);
				} catch (Exception e) {
					log.error("预付费云硬盘超过保留时长处理失败,volId:"+cloudVolume.getVolId(),e);
				}
			}
		
	}

	/**
	 * 云主机过期处理(超过保留时长)
	 * 
	 * @param endTime
	 * @throws Exception
	 */
	private void modifyForVmByExceed(Date endTime) throws Exception {
			List<CloudVm> cloudVmList = vmService.queryNormalVm(null, endTime, "2",
					false, false,PayType.PAYBEFORE, "0"); // 获取客户状态为未冻结的主机到期时间为<=今天0点-3天的状态为已过期的未被删除的预付费云主机信息
			log.info("开始执行预付费云主机超过保留时长处理,vmSize:"+cloudVmList.size());
			for (CloudVm cloudVm : cloudVmList) {
				try {
					log.info("开始执行预付费云主机超过保留时长处理,vmId:"+cloudVm.getVmId());
					// 解绑公网ip
					CloudFloatIp cloudFloatIp = cloudFloatIpService
							.getCloudFloatIpByResId(cloudVm.getVmId(), "vm");
					if (cloudFloatIp != null) {
						cloudFloatIpService.unbundingResource(cloudFloatIp);
					}
					// 解绑数据盘
					volumeService.debindVolsByVmId(cloudVm.getVmId());
					// 修改主机服务状态并挂载云主机
					vmService.modifyStateForVm(cloudVm.getVmId(), "3", null, true,
							false);
				} catch (Exception e) {
					log.error("预付费云主机超过保留时长处理失败,vmId:"+cloudVm.getVmId(),e);
				}
			}
		
	}

	/**
	 * 获取当日0点
	 * 
	 * @return
	 */
	private Date getTodayZero() {
		Date endTime = new Date();
		// 获取当日0点
		Calendar c = Calendar.getInstance();
		c.setTime(endTime);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		endTime = c.getTime();
		return endTime;
	}
	/**
	 * 获取当前小时整点
	 * 
	 * @return
	 */
	private Date getNowZero() {
		Date endTime = new Date();
		// 获取当日0点
		Calendar c = Calendar.getInstance();
		c.setTime(endTime);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		endTime = c.getTime();
		return endTime;
	}

	@Override
	public void inRententionTime(String cusId) throws Exception {
		log.info("后付费资源欠费(未到保留时长)处理,cusId:"+cusId);
		List<CloudProject> projectList = projectService
				.getProjectListByCustomer(cusId);
		if (projectList != null && projectList.size() > 0) {
			for (CloudProject cloudProject : projectList) {
				modifyForVmByInTime(cloudProject);
				modifyForVolumeByInTime(cloudProject);
				modifyForSnapshotByInTime(cloudProject);
				modifyForNetWorkByInTime(cloudProject);
				modifyForLdPoolByInTime(cloudProject);
				modifyForFloatIpByInTime(cloudProject);
				modifyForVpnByInTime(cloudProject);
				modifyForRDSByInTime(cloudProject);
			}
		}
	}

	private void modifyForRDSByInTime(CloudProject cloudProject) throws Exception{
		List<CloudRDSInstance> list=rdsInstanceService.queryNormalRdsInstance(cloudProject.getProjectId(), null, "0", PayType.PAYAFTER, null);
		log.info("后付费资源欠费(未到保留时长)处理,RDSSize:"+list.size());
		for (CloudRDSInstance cloudRdsInstance : list) {
			try {
				log.info("后付费资源欠费(未到保留时长)处理,RDSId:"+cloudRdsInstance.getRdsId());
				if(cloudRdsInstance!=null&&!StringUtil.isEmpty(cloudRdsInstance.getRdsId())){
					rdsInstanceService.modifyStateForRdsInstance(cloudRdsInstance.getRdsId(), "1", null,false,false);
				}
			} catch (Exception e) {
				log.error("后付费rds欠费(未到保留时长)处理失败,rdsId:"+cloudRdsInstance.getRdsId(),e);
			}
		}
	}

	/**
	 * 修改vpn(未超过保留时长)
	 * 
	 * @param cloudProject
	 */
	private void modifyForVpnByInTime(CloudProject cloudProject) {
		List<CloudVpn> list = vpnService.findVpnByCharge(
				cloudProject.getProjectId(), "0", PayType.PAYAFTER, null);
		log.info("后付费资源欠费(未到保留时长)处理,VPNSize:"+list.size());
		for (CloudVpn cloudVpn : list) {
			try {
				log.info("后付费资源欠费(未到保留时长)处理,vpnId:"+cloudVpn.getVpnId());
				if(cloudVpn!=null&&!StringUtil.isEmpty(cloudVpn.getVpnId())){
					vpnService.modifyStateForVPN(cloudVpn.getVpnId(), "1", null, false,
							false);
				}
			} catch (Exception e) {
				log.error("后付费vpn欠费(未到保留时长)处理失败,vpnId:"+cloudVpn.getVpnId(),e);
			}
		}

	}

	/**
	 * 修改公网ip(未超过保留时长)
	 * 
	 * @param cloudProject
	 * @throws Exception
	 */
	private void modifyForFloatIpByInTime(CloudProject cloudProject)
			throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("endTime", null);
		map.put("prjId", cloudProject.getProjectId());
		map.put("isDelete", "0");
		map.put("chargeState", "0");
		map.put("payType", PayType.PAYAFTER);
		List<CloudFloatIp> list = cloudFloatIpService.getCloudFloatIpByMap(map);
		log.info("后付费资源公网ip欠费(未到保留时长)处理,floatIPSize:"+list.size());
		for (CloudFloatIp cloudFloatIp : list) {
			try {
				log.info("后付费资源欠费(未到保留时长)处理,floatIpId:"+cloudFloatIp.getFloId());
				if(cloudFloatIp!=null&&!StringUtil.isEmpty(cloudFloatIp.getFloId())){
					cloudFloatIpService.modifyStateForFloatIp(cloudFloatIp.getFloId(),
							"1", null);
				}
			} catch (Exception e) {
				log.error("后付费公网ip欠费(未到保留时长)处理失败,floatIpId:"+cloudFloatIp.getFloId(),e);
			}
		}
	}

	/**
	 * 修改负载均衡(未超过保留时长)
	 * 
	 * @param cloudProject
	 */
	private void modifyForLdPoolByInTime(CloudProject cloudProject) {
		List<CloudLdPool> list = poolService.findLdPoolByCharge(
				cloudProject.getProjectId(), "0", PayType.PAYAFTER, null);
		log.info("后付费资源负载均衡欠费(未到保留时长)处理,poolSize:"+list.size());
		for (CloudLdPool cloudLdPool : list) {
			try {
				log.info("后付费资源欠费(未到保留时长)处理,poolId:"+cloudLdPool.getPoolId());
				if(cloudLdPool!=null&&!StringUtil.isEmpty(cloudLdPool.getPoolId())){
					poolService.modifyStateForLdPool(cloudLdPool.getPoolId(), "1",
							null, false, false);
				}
			} catch (Exception e) {
				log.error("后付费负载均衡欠费(未到保留时长)处理失败,poolId:"+cloudLdPool.getPoolId(),e);
			}
		}

	}

	/**
	 * 修改私有网络(未超过保留时长)
	 * 
	 * @param cloudProject
	 */
	private void modifyForNetWorkByInTime(CloudProject cloudProject) {
		List<CloudNetWork> list = netWorkService.findNetWorkByCharge(
				cloudProject.getProjectId(), "0", PayType.PAYAFTER, null);
		log.info("后付费资源欠费(未到保留时长)处理,netSize:"+list.size());
		for (CloudNetWork cloudNetWork : list) {
			try {
				log.info("后付费资源欠费(未到保留时长)处理,netId:"+cloudNetWork.getNetId());
				if(cloudNetWork!=null&&!StringUtil.isEmpty(cloudNetWork.getNetId())){
					netWorkService.modifyStateForNetWork(cloudNetWork.getNetId(), "1",
							null, false, false);
				}
			} catch (Exception e) {
				log.error("后付费私有网络欠费(未到保留时长)处理失败,netId:"+cloudNetWork.getNetId(),e);
			}
		}
	}

	/**
	 * 修改备份(未超过保留时长)
	 * 
	 * @param cloudProject
	 * @throws Exception
	 */
	private void modifyForSnapshotByInTime(CloudProject cloudProject)
			throws Exception {
		List<CloudSnapshot> list = snapshotService.getSnapshotsBySome(
				cloudProject.getProjectId(), "0", PayType.PAYAFTER, "0");
		log.info("后付费资源欠费(未到保留时长)处理,snapSize:"+list.size());
		for (CloudSnapshot cloudSnapshot : list) {
			try {
				log.info("后付费资源欠费(未到保留时长)处理,SnapId:"+cloudSnapshot.getSnapId());
				if(cloudSnapshot!=null&&!StringUtil.isEmpty(cloudSnapshot.getSnapId())){
					snapshotService.modifyStateForSnap(cloudSnapshot.getSnapId(), "1",
							false);
				}
			} catch (Exception e) {
				log.error("后付费备份欠费(未到保留时长)处理失败,snapId:"+cloudSnapshot.getSnapId(),e);
			}
		}

	}

	/**
	 * 修改云硬盘(未超过保留时长)
	 * 
	 * @param cloudProject
	 * @throws Exception
	 */
	private void modifyForVolumeByInTime(CloudProject cloudProject)
			throws Exception {
		List<CloudVolume> list = volumeService.getVolumesBySome(null, "0", "0",false,
				PayType.PAYAFTER, cloudProject.getProjectId(), "0");
		log.info("后付费资源欠费(未到保留时长)处理,volSize:"+list.size());
		for (CloudVolume cloudVolume : list) {
			try {
				log.info("后付费资源欠费(未到保留时长)处理,volId:"+cloudVolume.getVolId());
				if(cloudVolume!=null&&!StringUtil.isEmpty(cloudVolume.getVolId())){
					volumeService.modifyStateForVol(cloudVolume.getVolId(), "1", false);
				}
			} catch (Exception e) {
				log.error("后付费云硬盘欠费(未到保留时长)处理失败,volId:"+cloudVolume.getVolId(),e);
			}
		}
	}

	/**
	 * 修改云主机(未超过保留时长)
	 * 
	 * @param cloudProject
	 * @throws Exception
	 */
	private void modifyForVmByInTime(CloudProject cloudProject)
			throws Exception {
		List<CloudVm> list = vmService.queryNormalVm(
				cloudProject.getProjectId(), null, "0", false, false,
				PayType.PAYAFTER, null);
		log.info("后付费资源欠费(未到保留时长)处理,vmSize:"+list.size());
		for (CloudVm cloudVm : list) {
			try {
				log.info("后付费云主机资源欠费(未到保留时长)处理,vmId:"+cloudVm.getVmId());
				if(cloudVm!=null&&!StringUtil.isEmpty(cloudVm.getVmId())){
					vmService.modifyStateForVm(cloudVm.getVmId(), "1", null, false,
							false);
					CloudVolume cloudVolume=volumeService.getOsVolumeByVmId(cloudVm.getVmId());
					if(cloudVolume!=null&&!StringUtil.isEmpty(cloudVolume.getVolId())){
						volumeService.modifyStateForVol(cloudVolume.getVolId(), "1", false);
					}
				}
			} catch (Exception e) {
				log.error("后付费云主机欠费(未到保留时长)处理失败,vmId:"+cloudVm.getVmId(),e);
			}
		}
	}

	@Override
	public void outRententionTime(final String cusId,Date time) throws Exception {
		log.info("后付费资源欠费(达到保留时长)处理,cusId:"+cusId);
		List<CloudProject> projectList = projectService
				.getProjectListByCustomer(cusId);
		final List<MessagePayUpperLimitResourcesStopModel> list = new ArrayList<MessagePayUpperLimitResourcesStopModel>();
		if (projectList != null && projectList.size() > 0) {
			for (CloudProject cloudProject : projectList) {
				modifyForVmByOutTime(cloudProject, list, time);
				modifyForVolumeByOutTime(cloudProject, list, time);
				modifyForSnapshotByOutTime(cloudProject, list, time);
				modifyForNetWorkByOutTime(cloudProject, list, time);
				modifyForLdPoolByOutTime(cloudProject, list, time);
				modifyForFloatIpByOutTime(cloudProject, list, time);
				modifyForVpnByOutTime(cloudProject, list, time);
				modifyForRDSByOutTime(cloudProject, list, time);
			}
			modifyForObsByOutTime(cusId, list, time);
		}
		if(list.size()>0){
			DistributedLockBean dlBean=new DistributedLockBean();
			dlBean.setGranularity("outRententionTimeCus-"+cusId);
			dlBean.setLockService(new LockService() {
				
				@Override
				public Object doService() throws Exception {
					String result=jedisUtil.get(RedisKey.OUT_RENTENTION_TIME+cusId);
					if(result==null){
						log.info("客户:"+cusId+"发送资源停用短信邮件");
						messageCenterService.payUpperLimitResourStopMessage(list, cusId);
						jedisUtil.set(RedisKey.OUT_RENTENTION_TIME+cusId, "1");
					}
					return null;
				}
			});
			distributedLockService.doServiceByLock(dlBean);
		}
	}

	private void modifyForRDSByOutTime(CloudProject cloudProject,
			List<MessagePayUpperLimitResourcesStopModel> mes, Date time) throws Exception{
		List<CloudRDSInstance> list=rdsInstanceService.queryNormalRdsInstance(cloudProject.getProjectId(), null, "1", PayType.PAYAFTER, null);
		log.info("后付费资源欠费(达到保留时长)处理,RDSSize:"+list.size());
		for (CloudRDSInstance cloudRDSInstance : list) {
			try {
				log.info("后付费资源欠费(达到保留时长)处理,RDSId:"+cloudRDSInstance.getRdsId());
				rdsInstanceService.modifyStateForRdsInstance(cloudRDSInstance.getRdsId(), "1", null,true,false);
				MessagePayUpperLimitResourcesStopModel resource = new MessagePayUpperLimitResourcesStopModel();
				resource.setExpireDate(time);
				resource.setResourcesName(cloudRDSInstance.getRdsName());
				resource.setResourcesType("MySQL");
				mes.add(resource);
			} catch (Exception e) {
				log.error("后付费RDS欠费(达到保留时长)处理失败,RDSId:"+cloudRDSInstance.getRdsId(),e);
			}
		}
	}

	private void modifyForVpnByOutTime(CloudProject cloudProject,
			List<MessagePayUpperLimitResourcesStopModel> list, Date now) {
		
			List<CloudVpn> cloudVpnList = vpnService.findVpnByCharge(
					cloudProject.getProjectId(), "1", PayType.PAYAFTER, null);
			log.info("后付费资源欠费(达到保留时长)处理,vpnSize:"+cloudVpnList.size());
			for (CloudVpn cloudVpn : cloudVpnList) {
				try {
					log.info("后付费资源欠费(达到保留时长)处理,VpnId:"+cloudVpn.getVpnId());
					vpnService.modifyStateForVPN(cloudVpn.getVpnId(), "1", null, true,
							false);
					MessagePayUpperLimitResourcesStopModel resource = new MessagePayUpperLimitResourcesStopModel();
					resource.setExpireDate(now);
					resource.setResourcesName(cloudVpn.getVpnName());
					resource.setResourcesType("VPN");
					list.add(resource);
				} catch (Exception e) {
					log.error("后付费vpn欠费(达到保留时长)处理失败,VpnId:"+cloudVpn.getVpnId(),e);
				}
			}
		
	}

	private void modifyForSnapshotByOutTime(CloudProject cloudProject,
			List<MessagePayUpperLimitResourcesStopModel> list, Date now) throws Exception{
		
			List<CloudSnapshot> cloudSnapshotList = snapshotService.getSnapshotsBySome(
					cloudProject.getProjectId(), "0", PayType.PAYAFTER, "1");
			log.info("后付费资源欠费(达到保留时长)处理,snapSize:"+cloudSnapshotList.size());
			for (CloudSnapshot cloudSnapshot : cloudSnapshotList) {
				try {
					log.info("后付费资源欠费(达到保留时长)处理,snapId:"+cloudSnapshot.getSnapId());
					snapshotService.modifyStateForSnap(cloudSnapshot.getSnapId(), "1",
							true);
					MessagePayUpperLimitResourcesStopModel resource = new MessagePayUpperLimitResourcesStopModel();
					resource.setExpireDate(now);
					resource.setResourcesName(cloudSnapshot.getSnapName());
					resource.setResourcesType("云硬盘备份");
					list.add(resource);
				} catch (Exception e) {
					log.error("后付费备份欠费(达到保留时长)处理失败,snapId:"+cloudSnapshot.getSnapId(),e);
				}
			}
		
	}

	/**
	 * 修改对象存储(超过保留时长)
	 * 
	 * @param cusId
	 * @param list
	 * @param now
	 * @throws Exception
	 */
	private void modifyForObsByOutTime(String cusId,
			List<MessagePayUpperLimitResourcesStopModel> list, Date now)
			throws Exception {
		try {
			CusServiceState cusServiceState=obsOpenService.getObsByCusId(cusId);
			if(cusServiceState!=null&&"1".equals(cusServiceState.getObsState())){
				log.info("后付费资源欠费(达到保留时长)处理,对象存储");
				accessKeyService.stopRunningAkExceptDefaultByCusId(cusId,"0");
				MessagePayUpperLimitResourcesStopModel resource = new MessagePayUpperLimitResourcesStopModel();
				resource.setExpireDate(now);
				resource.setResourcesName("对象存储服务");
				resource.setResourcesType("对象存储");
				list.add(resource);
			}
		} catch (Exception e) {
			log.error("后付费对象存储欠费(达到保留时长)处理失败",e);
		}
	}

	/**
	 * 修改公网ip(超过保留时长)
	 * 
	 * @param cloudProject
	 * @param payUpperLimitResourcesStopModelList
	 * @param now
	 */
	private void modifyForFloatIpByOutTime(CloudProject cloudProject,
			List<MessagePayUpperLimitResourcesStopModel> payUpperLimitResourcesStopModelList, Date now) throws Exception{
		
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("endTime", null);
			map.put("prjId", cloudProject.getProjectId());
			map.put("isDelete", "0");
			map.put("chargeState", "1");
			map.put("payType", PayType.PAYAFTER);
			List<CloudFloatIp> list = cloudFloatIpService.getCloudFloatIpByMap(map);
			log.info("后付费资源欠费(达到保留时长)处理,floatIpSize:"+list.size());
			for (CloudFloatIp cloudFloatIp : list) {
				try {
					log.info("后付费资源欠费(达到保留时长)处理,FloatIpId:"+cloudFloatIp.getFloId());
					String resourceId = cloudFloatIp.getResourceId();
					String resourceType = cloudFloatIp.getResourceType();
					if (!StringUtil.isEmpty(resourceId)
							&& !StringUtil.isEmpty(resourceType)) {
						cloudFloatIpService.unbundingResource(cloudFloatIp);
					}
					cloudFloatIp.setDeleteTime(now);
					cloudFloatIpService.deleteFloatIp(cloudFloatIp, cloudProject.getCustomerId());
					MessagePayUpperLimitResourcesStopModel resource = new MessagePayUpperLimitResourcesStopModel();
					resource.setExpireDate(now);
					resource.setResourcesName(cloudFloatIp.getFloIp());
					resource.setResourcesType("公网IP");
					payUpperLimitResourcesStopModelList.add(resource);
				} catch (Exception e) {
					log.error("后付费公网IP欠费(达到保留时长)处理失败,FloatIpId:"+cloudFloatIp.getFloId(),e);
				}
			}
	}

	/**
	 * 修改负载均衡(超过保留时长)
	 * 
	 * @param cloudProject
	 * @param payUpperLimitResourcesStopModelList
	 * @param now
	 */
	private void modifyForLdPoolByOutTime(CloudProject cloudProject,
			List<MessagePayUpperLimitResourcesStopModel> payUpperLimitResourcesStopModelList, Date now) {
		
			List<CloudLdPool> list = poolService.findLdPoolByCharge(
					cloudProject.getProjectId(), "1", PayType.PAYAFTER, null);
			log.info("后付费资源欠费(达到保留时长)处理,poolSize:"+list.size());
			for (CloudLdPool cloudLdPool : list) {
				try {
					log.info("后付费资源欠费(达到保留时长)处理,LdPoolId:"+cloudLdPool.getPoolId());
					poolService.modifyStateForLdPool(cloudLdPool.getPoolId(), "1",
							null, true, false);
					MessagePayUpperLimitResourcesStopModel resource = new MessagePayUpperLimitResourcesStopModel();
					resource.setExpireDate(now);
					resource.setResourcesName(cloudLdPool.getPoolName());
					resource.setResourcesType("负载均衡器");
					payUpperLimitResourcesStopModelList.add(resource);
				} catch (Exception e) {
					log.error("后付费负载均衡欠费(达到保留时长)处理失败,LdPoolId:"+cloudLdPool.getPoolId(),e);
				}
			}
		
	}

	/**
	 * 修改私有网络(超过保留时长)
	 * 
	 * @param cloudProject
	 * @param payUpperLimitResourcesStopModelList
	 * @param now
	 */
	private void modifyForNetWorkByOutTime(CloudProject cloudProject,
			List<MessagePayUpperLimitResourcesStopModel> payUpperLimitResourcesStopModelList, Date now) {
		
			List<CloudNetWork> list = netWorkService.findNetWorkByCharge(
					cloudProject.getProjectId(), "1", PayType.PAYAFTER, null);
			log.info("后付费资源欠费(达到保留时长)处理,netSize:"+list.size());
			for (CloudNetWork cloudNetWork : list) {
				try {
					log.info("后付费资源欠费(达到保留时长)处理,netId:"+cloudNetWork.getNetId());
					netWorkService.modifyStateForNetWork(cloudNetWork.getNetId(), "1",
							null, true, false);
					MessagePayUpperLimitResourcesStopModel resource = new MessagePayUpperLimitResourcesStopModel();
					resource.setExpireDate(now);
					resource.setResourcesName(cloudNetWork.getNetName());
					resource.setResourcesType("私有网络");
					payUpperLimitResourcesStopModelList.add(resource);
				} catch (Exception e) {
					log.error("后付费私有网络欠费(达到保留时长)处理失败,netId:"+cloudNetWork.getNetId(),e);
				}
			}
		
	}

	/**
	 * 修改云硬盘(超过保留时长)
	 * 
	 * @param cloudProject
	 * @param list
	 * @param now
	 * @throws Exception
	 */
	private void modifyForVolumeByOutTime(CloudProject cloudProject,
			List<MessagePayUpperLimitResourcesStopModel> list, Date now)
			throws Exception {
		
			List<CloudVolume> cloudVolumeList = volumeService.getVolumesBySome(
					null, "1", "0",false, PayType.PAYAFTER, cloudProject.getProjectId(),
					"0");// 获取cloud_volum表中到期时间<=今天0点的状态正常的未被删除的预付费数据盘信息
			log.info("后付费资源欠费(达到保留时长)处理,volSize:"+cloudVolumeList.size());
			for (CloudVolume cloudVolume : cloudVolumeList) {
				try {
					log.info("后付费资源欠费(达到保留时长)处理,volId:"+cloudVolume.getVolId());
					volumeService.modifyStateForVol(cloudVolume.getVolId(), "1", true);
					MessagePayUpperLimitResourcesStopModel resource = new MessagePayUpperLimitResourcesStopModel();
					resource.setExpireDate(now);
					resource.setResourcesName(cloudVolume.getVolName());
					resource.setResourcesType("云硬盘");
					list.add(resource);
				} catch (Exception e) {
					log.error("后付费云硬盘欠费(达到保留时长)处理失败,volId:"+cloudVolume.getVolId(),e);
				}
			}
		
	}

	/**
	 * 修改云主机(超过保留时长)
	 * 
	 * @param cloudProject
	 * @param PayUpperLimitResourcesStopModelList
	 * @param now
	 * @throws Exception
	 */
	private void modifyForVmByOutTime(CloudProject cloudProject,
			List<MessagePayUpperLimitResourcesStopModel> PayUpperLimitResourcesStopModelList, Date now)
			throws Exception {
		
			List<CloudVm> list = vmService.queryNormalVm(
					cloudProject.getProjectId(), null, "1", false, false,
					PayType.PAYAFTER, null);
			log.info("后付费资源欠费(达到保留时长)处理,vmSize:"+list.size());
			for (CloudVm cloudVm : list) {
				try {
					log.info("后付费资源欠费(达到保留时长)处理,vmId:"+cloudVm.getVmId());
					CloudFloatIp cloudFloatIp = cloudFloatIpService
							.getCloudFloatIpByResId(cloudVm.getVmId(), "vm");
					if (cloudFloatIp != null && !StringUtils.isEmpty(cloudFloatIp.getFloId())) {
						cloudFloatIpService.unbundingResource(cloudFloatIp);
					}
					// 解绑数据盘
					volumeService.debindVolsByVmId(cloudVm.getVmId());
					// 修改主机服务状态并挂载云主机
					vmService.modifyStateForVm(cloudVm.getVmId(), "1", null, true,
							false);
					MessagePayUpperLimitResourcesStopModel resource = new MessagePayUpperLimitResourcesStopModel();
					resource.setExpireDate(now);
					resource.setResourcesName(cloudVm.getVmName());
					resource.setResourcesType("云主机");
					PayUpperLimitResourcesStopModelList.add(resource);
				} catch (Exception e) {
					log.error("后付费云主机欠费(达到保留时长)处理失败,vmId:"+cloudVm.getVmId(),e);
				}
			}
		
	}

	@Override
	@Transactional
	public List<AboutToExpire> getExpireResources(String cusId, Date time,
			String chargeState) throws Exception {
		List<Object> paramList = new ArrayList<Object>();
		StringBuffer sql = genSql(cusId, time, PayType.PAYBEFORE, chargeState,
				paramList);
		Query query = cloudVmDao.createSQLNativeQuery(sql.toString(),
				paramList.toArray());
		List list = query.getResultList();
		List<AboutToExpire> aboutToExpireList = new ArrayList<AboutToExpire>();
		for (int i = 0; i < list.size(); i++) {
			Object[] obj = (Object[]) list.get(i);
			AboutToExpire toExpire = new AboutToExpire();
			toExpire.setResourcesId(String.valueOf(obj[0]));
			toExpire.setResourcesName(String.valueOf(obj[1]));
			toExpire.setCreateTime((Date) obj[2]);
			toExpire.setEndTime((Date) obj[3]);
			toExpire.setResourcesType(String.valueOf(obj[4]));
			toExpire.setPrjId(String.valueOf(obj[5]));
			aboutToExpireList.add(toExpire);
		}
		return aboutToExpireList;
	}

	/**
	 * 生成sql
	 * 
	 * @param cusId
	 *            客户id
	 * @param threeDay
	 *            到期时间
	 * @param payType
	 *            付费类型
	 * @param chargeState
	 *            服务状态
	 * @param paramList
	 * @return
	 */
	private StringBuffer genSql(String cusId, Date threeDay, String payType,
			String chargeState, List<Object> paramList) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" vm.vm_id AS resources_id, vm.vm_name AS resources_name, vm.create_time AS create_time,");
		sql.append(" vm.end_time AS end_time, '云主机' AS resources_type, vm.prj_id AS prj_id ");
		sql.append(" FROM cloud_vm vm ");
		sql.append(" LEFT JOIN cloud_project prj on prj.prj_id=vm.prj_id ");
		sql.append(" LEFT JOIN sys_selfcustomer cus on cus.cus_id=prj.customer_id ");
		sql.append(" WHERE vm.pay_type = ? AND vm.is_deleted = '0' AND vm.is_visable ='1' ");
		paramList.add(payType);
		if(!StringUtil.isEmpty(chargeState)){
			sql.append(" AND vm.charge_state = ?  ");
			paramList.add(chargeState);
		}
		sql.append(" AND cus.cus_id = ? ");
		paramList.add(cusId);
		if (threeDay != null) {
			sql.append("  AND vm.end_time = ? ");
			paramList.add(threeDay);
		}
		sql.append(" UNION ");
		sql.append(" SELECT ");
		sql.append(" vol.vol_id AS resources_id, vol.vol_name AS resources_name, vol.create_time AS create_time, ");
		sql.append(" vol.end_time AS end_time, '云硬盘' AS resources_type, vol.prj_id AS prj_id ");
		sql.append(" FROM cloud_volume vol ");
		sql.append(" LEFT JOIN cloud_project prj on prj.prj_id=vol.prj_id ");
		sql.append(" LEFT JOIN sys_selfcustomer cus on cus.cus_id=prj.customer_id ");
		sql.append(" WHERE vol.pay_type = ? AND vol.is_deleted = '0' AND vol.vol_bootable='0' AND vol.is_visable ='1' ");
		paramList.add(payType);
		if(!StringUtil.isEmpty(chargeState)){
			sql.append(" AND vol.charge_state = ? ");
			paramList.add(chargeState);
		}
		sql.append(" AND cus.cus_id = ? ");
		paramList.add(cusId);
		if (threeDay != null) {
			sql.append(" AND vol.end_time = ? ");
			paramList.add(threeDay);
		}
		if(PayType.PAYAFTER.equals(payType)){
			sql.append(" UNION ");
			sql.append(" SELECT ");
			sql.append(" snap.snap_id AS resource_id,snap.snap_name AS resource_name,snap.create_time AS create_time,'' AS end_time,'云硬盘备份' AS resources_type ,snap.prj_id AS prj_id ");
			sql.append(" FROM cloud_disksnapshot snap ");
			sql.append(" LEFT JOIN cloud_project prj on prj.prj_id=snap.prj_id  ");
			sql.append(" LEFT JOIN sys_selfcustomer cus on cus.cus_id=prj.customer_id ");
			sql.append(" WHERE snap.pay_type='2' AND snap.is_deleted='0'  AND snap.is_visable ='1'  ");
			if(!StringUtil.isEmpty(chargeState)){
				sql.append(" AND snap.charge_state= ? ");
				paramList.add(chargeState);
			}
			sql.append(" AND cus.cus_id= ? ");
			paramList.add(cusId);
		}
		
		sql.append(" UNION ");
		sql.append(" SELECT ");
		sql.append(" net.net_id AS resources_id, net.net_name AS resources_name, net.create_time AS create_time, ");
		sql.append(" net.end_time AS end_time, '私有网络' AS resources_type, net.prj_id AS prj_id ");
		sql.append(" FROM cloud_network net ");
		sql.append(" LEFT JOIN cloud_project prj on prj.prj_id=net.prj_id ");
		sql.append(" LEFT JOIN sys_selfcustomer cus on cus.cus_id=prj.customer_id ");
		sql.append(" WHERE net.pay_type = ? AND net.is_visible='1' ");
		paramList.add(payType);
		if(!StringUtil.isEmpty(chargeState)){
			sql.append(" AND net.charge_state = ? ");
			paramList.add(chargeState);
		}
		sql.append(" AND cus.cus_id = ? ");
		paramList.add(cusId);
		if (threeDay != null) {
			sql.append(" AND net.end_time = ? ");
			paramList.add(threeDay);
		}
		sql.append(" UNION ");
		sql.append(" SELECT ");
		sql.append(" pool.pool_id AS resources_id, pool.pool_name AS resources_name, pool.create_time AS create_time, ");
		sql.append(" pool.end_time AS end_time, '负载均衡器' AS resources_type, pool.prj_id AS prj_id");
		sql.append(" FROM cloud_ldpool pool ");
		sql.append(" LEFT JOIN cloud_project prj on prj.prj_id=pool.prj_id ");
		sql.append(" LEFT JOIN sys_selfcustomer cus on cus.cus_id=prj.customer_id ");
		sql.append(" WHERE pool.pay_type = ? AND pool.is_visible='1' ");
		paramList.add(payType);
		if(!StringUtil.isEmpty(chargeState)){
			sql.append(" AND pool.charge_state = ? ");
			paramList.add(chargeState);
		}
		sql.append(" AND cus.cus_id = ? ");
		paramList.add(cusId);
		if (threeDay != null) {
			sql.append(" AND pool.end_time = ? ");
			paramList.add(threeDay);
		}
		sql.append(" UNION ");
		sql.append(" SELECT ");
		sql.append(" flo.flo_id AS resources_id, flo.flo_ip AS resources_name, flo.create_time AS create_time, ");
		sql.append(" flo.end_time AS end_time, '弹性公网IP' AS resources_type, flo.prj_id AS prj_id ");
		sql.append(" FROM cloud_floatip flo ");
		sql.append(" LEFT JOIN cloud_project prj on prj.prj_id=flo.prj_id ");
		sql.append(" LEFT JOIN sys_selfcustomer cus on cus.cus_id=prj.customer_id ");
		sql.append(" WHERE flo.pay_type = ? AND flo.is_visable='1' ");
		paramList.add(payType);
		if(PayType.PAYAFTER.equals(payType)){
			sql.append(" AND flo.is_deleted = '0'  ");
		}else{
			if(!StringUtil.isEmpty(chargeState)){
				if("2".equals(chargeState)){
					sql.append("  AND flo.is_deleted = '0' ");
				}else if("3".equals(chargeState)){
					sql.append(" AND flo.is_deleted = '1' ");
				}else if("0".equals(chargeState)){
				 	sql.append("  AND flo.is_deleted = '0' ");
				}
			}
		}
		if(!StringUtil.isEmpty(chargeState)){
			sql.append(" AND flo.charge_state = ? ");
			paramList.add(chargeState);
		}
		sql.append(" AND cus.cus_id = ? ");
		paramList.add(cusId);
		if (threeDay != null) {
			sql.append(" AND flo.end_time = ? ");
			paramList.add(threeDay);
		}
		sql.append(" UNION ");
		sql.append(" SELECT ");
		sql.append(" vpn.vpn_id AS resources_id, vpn.vpn_name AS resources_name, vpn.create_time AS create_time, ");
		sql.append(" vpn.end_time AS end_time, 'VPN' AS resources_type, ser.prj_id AS prj_id ");
		sql.append(" FROM cloud_vpnconn vpn ");
		sql.append(" JOIN cloud_vpnservice ser ON vpn.vpnservice_id = ser.vpnservice_id ");
		sql.append(" LEFT JOIN cloud_project prj on prj.prj_id=ser.prj_id ");
		sql.append(" LEFT JOIN sys_selfcustomer cus on cus.cus_id=prj.customer_id ");
		sql.append(" WHERE vpn.pay_type = ? AND vpn.is_visible='1'");
		paramList.add(payType);
		if(!StringUtil.isEmpty(chargeState)){
			sql.append(" AND vpn.charge_state = ? ");
			paramList.add(chargeState);
		}
		sql.append(" AND cus.cus_id = ? ");
		paramList.add(cusId);
		if (threeDay != null) {
			sql.append(" AND vpn.end_time = ? ");
			paramList.add(threeDay);
		}
		sql.append(" UNION ");
		sql.append(" SELECT ");
		sql.append(" rds.rds_id AS resources_id, rds.rds_name AS resources_name, rds.create_time AS create_time,");
		sql.append(" rds.end_time AS end_time, 'MySQL' AS resources_type, rds.prj_id AS prj_id ");
		sql.append(" FROM cloud_rdsinstance rds ");
		sql.append(" LEFT JOIN cloud_project prj on prj.prj_id=rds.prj_id ");
		sql.append(" LEFT JOIN sys_selfcustomer cus on cus.cus_id=prj.customer_id ");
		sql.append(" WHERE rds.pay_type = ? AND rds.is_deleted = '0' AND rds.is_visible ='1' ");
		paramList.add(payType);
		if(!StringUtil.isEmpty(chargeState)){
			sql.append(" AND rds.charge_state = ?  ");
			paramList.add(chargeState);
		}
		sql.append(" AND cus.cus_id = ? ");
		paramList.add(cusId);
		if (threeDay != null) {
			sql.append("  AND rds.end_time = ? ");
			paramList.add(threeDay);
		}
		sql.append(" ORDER BY end_time ");
		return sql;
	}
	@Override
	@Transactional
	public int getWillStopResourceCount(String cusId, Date date) {
		List<Object> paramList = new ArrayList<Object>();
		StringBuffer sb = new StringBuffer(" SELECT count(*) from ( ");
		sb.append(genSql(cusId, null, PayType.PAYAFTER, null, paramList));
		sb.append(") as resCount");
		Query query = cloudVmDao.createSQLNativeQuery(sb.toString(),
				paramList.toArray());
		Object o = query.getSingleResult();
		int totalCount = 0;
		if (o instanceof BigInteger) {
			totalCount = ((BigInteger) o).intValue();
		}
		CusServiceState cusServiceState=obsOpenService.getObsByCusId(cusId);
		if(cusServiceState!=null&&"1".equals(cusServiceState.getObsState())){
			totalCount++;
		}
		return totalCount;
	}

	@Override
	@Transactional
	public void sendMessageForReachCreditlimit(final String cusId, Date time)
			throws Exception {
		log.info("开始准备发送资源到达信用额度消息提醒");
		int hours = Integer.parseInt(sysDataTreeService.getRecoveryTime());
		time = DateUtil.addDay(time, new int[] { 0, 0, 0, hours });
		List<ChargeRecord> chargeRecordList = chargeRecordService
				.getAllValidChargeRecordByCusId(cusId);
		final List<MessagePayAsYouGoResourcesStopModel> resourList = new ArrayList<MessagePayAsYouGoResourcesStopModel>();
		for (ChargeRecord chargeRecord : chargeRecordList) {
			MessagePayAsYouGoResourcesStopModel resource = new MessagePayAsYouGoResourcesStopModel();
			resource.setExpireDate(time);
			if (ResourceType.VM.equals(chargeRecord.getResourceType())) {
				log.info("云主机资源到达信用额度,vmId:"+chargeRecord.getResourceId());
				CloudVm cloudVm = vmService.findVm(chargeRecord
						.getResourceId());
				if(cloudVm!=null&&!StringUtil.isEmpty(cloudVm.getVmName())){
					log.info("云主机资源到达信用额度,vmName:"+cloudVm.getVmName());
					resource.setResourcesName(cloudVm.getVmName());
				}
			} else if (ResourceType.VDISK
					.equals(chargeRecord.getResourceType())) {
				log.info("云硬盘资源达到信用额度,volId:"+chargeRecord.getResourceId());
				String name = volumeService.getVolumeNameById(chargeRecord
						.getResourceId());
				if(!StringUtil.isEmpty(name)){
					log.info("云硬盘资源达到信用额度,volName:"+name);
					resource.setResourcesName(name);
				}
			} else if (ResourceType.DISKSNAPSHOT
					.equals(chargeRecord.getResourceType())) {
				log.info("云硬盘备份资源达到信用额度,SnapId:"+chargeRecord.getResourceId());
				String name = snapshotService.getSnapshotNameById(chargeRecord.getResourceId());
				if(!StringUtil.isEmpty(name)){
					log.info("云硬盘备份资源达到信用额度,snapName:"+name);					
					resource.setResourcesName(name);
				}
			} else if (ResourceType.FLOATIP
					.equals(chargeRecord.getResourceType())) {
				log.info("公网ip资源达到信用额度,floId:"+chargeRecord.getResourceId());
				String name = cloudFloatIpService.getIpInfoById(chargeRecord
						.getResourceId());
				if(!StringUtil.isEmpty(name)){
					log.info("公网ip资源达到信用额度,floName:"+name);
					resource.setResourcesName(name);
				}
			} else if (ResourceType.NETWORK
					.equals(chargeRecord.getResourceType())) {
				log.info("私有网络资源达到信用额度,netId:"+chargeRecord.getResourceId());
				String name = netWorkService.getVPCNameById(chargeRecord
						.getResourceId());
				if(!StringUtil.isEmpty(name)){
					log.info("私有网络资源达到信用额度,netName:"+name);
					resource.setResourcesName(name);
				}
			} else if (ResourceType.QUOTAPOOL
					.equals(chargeRecord.getResourceType())) {
				log.info("负载均衡资源达到信用额度,poolId:"+chargeRecord.getResourceId());
				String name = poolService.getLBNameById(chargeRecord
						.getResourceId());
				if(!StringUtil.isEmpty(name)){
					log.info("负载均衡资源达到信用额度,poolName:"+name);
					resource.setResourcesName(name);
				}
			} else if (ResourceType.VPN
					.equals(chargeRecord.getResourceType())) {
				log.info("vpn资源达到信用额度,vpnId:"+chargeRecord.getResourceId());
				String name = vpnService.getVpnInfo(chargeRecord
						.getResourceId()).getVpnName();
				if(!StringUtil.isEmpty(name)){
					log.info("vpn资源达到信用额度,vpnName:"+name);					
					resource.setResourcesName(name);
				}
			} else if(ResourceType.RDS.equals(chargeRecord.getResourceType())){
				log.info("RDS资源达到信用额度,RDSId:"+chargeRecord.getResourceId());
				ResourceCheckBean resourceCheckBean = rdsInstanceService.isExistsByResourceId(chargeRecord.getResourceId());
				String name="";
				if(resourceCheckBean!=null){
					name=resourceCheckBean.getResourceName();
				}
				if(!StringUtil.isEmpty(name)){
					log.info("RDS资源达到信用额度,RDSName:"+name);					
					resource.setResourcesName(name);
				}
			}
			if(!StringUtil.isEmpty(resource.getResourcesName())){
				log.info("资源达到信用额度,资源类型:"+getResourceTypeName(chargeRecord.getResourceType()));
				resource.setResourcesType(getResourceTypeName(chargeRecord.getResourceType()));
				resourList.add(resource);
			}
		}
		CusServiceState cusServiceState=obsOpenService.getObsByCusId(cusId);
		if(cusServiceState!=null&&"1".equals(cusServiceState.getObsState())){
			MessagePayAsYouGoResourcesStopModel resource = new MessagePayAsYouGoResourcesStopModel();
			resource.setResourcesType("对象存储");
			resource.setExpireDate(time);
			resource.setResourcesName("对象存储服务");
			resourList.add(resource);
		}
		if(resourList.size()>0){
			DistributedLockBean dlBean=new DistributedLockBean();
			dlBean.setGranularity("reachCreditLimitCus-"+cusId);
			dlBean.setLockService(new LockService() {
				
				@Override
				public Object doService() throws Exception {
					String result=jedisUtil.get(RedisKey.REACH_CREDIST_LIMIT+cusId);
					if(result==null){
						log.info("客户:"+cusId+"发送付费资源欠费上限停用通知");
						messageCenterService.payAsYouGoResourStopMessage(resourList, cusId);
						jedisUtil.set(RedisKey.REACH_CREDIST_LIMIT+cusId, "1");
					}
					return null;
				}
			});
			distributedLockService.doServiceByLock(dlBean);
		}
	}
	private String getResourceTypeName(String resourceType) {
		if (ResourceType.VM.equals(resourceType)) {
			return "云主机";
		} else if (ResourceType.VDISK.equals(resourceType)) {
			return "云硬盘";
		} else if (ResourceType.DISKSNAPSHOT.equals(resourceType)) {
			return "云硬盘备份";
		} else if (ResourceType.NETWORK.equals(resourceType)) {
			return "私有网络";
		} else if (ResourceType.QUOTAPOOL.equals(resourceType)) {
			return "负载均衡器";
		} else if (ResourceType.OBS.equals(resourceType)) {
			return "对象存储";
		} else if (ResourceType.VPN.equals(resourceType)) {
			return "VPN";
		} else if (ResourceType.FLOATIP.equals(resourceType)) {
			return "公网IP";
		} else if(ResourceType.RDS.equals(resourceType)){
			return "MySQL";
		}
		return "";
	}
}
