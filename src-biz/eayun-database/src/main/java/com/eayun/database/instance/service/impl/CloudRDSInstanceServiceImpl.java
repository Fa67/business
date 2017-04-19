package com.eayun.database.instance.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.PayType;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceSyncConstant;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.constant.SyncProgress.SyncByProjectTypes;
import com.eayun.common.constant.SyncProgress.SyncType;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.sync.SyncProgressUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.CloudResourceUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.database.account.model.CloudRDSAccount;
import com.eayun.database.account.service.CloudDBUserService;
import com.eayun.database.account.service.RDSAccountService;
import com.eayun.database.backup.service.RDSBackupService;
import com.eayun.database.database.service.CloudDatabaseService;
import com.eayun.database.database.service.RDSDatabaseService;
import com.eayun.database.instance.dao.CloudRDSInstanceDao;
import com.eayun.database.instance.model.BaseCloudRDSInstance;
import com.eayun.database.instance.model.CloudOrderRDSInstance;
import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.database.instance.service.CloudRDSInstanceService;
import com.eayun.database.instance.service.RDSInstanceService;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.RDSInstance;
import com.eayun.eayunstack.service.OpenstackRDSInstanceService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.monitor.bean.MonitorAlarmUtil;
import com.eayun.monitor.service.AlarmService;
import com.eayun.notice.model.MessageOrderResourceNotice;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class CloudRDSInstanceServiceImpl implements CloudRDSInstanceService {
	private static final Logger log = LoggerFactory.getLogger(CloudRDSInstanceServiceImpl.class);
	@Autowired
    private JedisUtil jedisUtil;
	@Autowired
	private OpenstackRDSInstanceService openstackRDSInstanceService;
	@Autowired
	private RDSInstanceService rdsInstanceService;
	@Autowired
	private CloudRDSInstanceDao cloudRDSInstanceDao;
	@Autowired
	private RDSDatabaseService rdsDatabaseService;
	@Autowired
	private RDSBackupService rdsBackupService;
	@Autowired
	private EcmcLogService ecmcLogService;
	@Autowired
	private RDSAccountService rdsAccountService;
	@Autowired
	private AlarmService alarmService;
	@Autowired
	private CloudDatabaseService cloudDatabaseService;
	@Autowired
	private OrderService orderService;
	@Autowired
	private MessageCenterService messageCenterService;
	@Autowired
	private CloudDBUserService cloudDBUserService;
	@Autowired
	private SyncProgressUtil syncProgressUtil;

	/**
	 * 查询当前队列的长度
	 * @param groupKey
	 * @return
	 */
	@Override
	public long size(String groupKey) {
		return jedisUtil.sizeOfList(groupKey);
	}

	/**
	 * 重新放入队列
	 * @param groupKey
	 * @param value
	 * @return
	 */
	@Override
	public boolean push(String groupKey, String value) {
		boolean flag = false;
		try {
			flag=  jedisUtil.push(groupKey, value);
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			flag = false;
		}
		return flag;
	}

	@Override
	public String pop(String groupKey) {
		String value = null;
        try {
            value = jedisUtil.pop(groupKey);
        } catch (Exception e){
            log.error(e.getMessage(),e);
            return null;
        }
        return value;
	}

	/**
	 * 同步创建中的云数据库实例资源
	 * @param cloudRDSInstance
	 * @return
	 * @throws Exception 
	 */
	@Override
	public boolean syncRDSInstanceInBuild(CloudRDSInstance cloudRDSInstance) throws Exception {
		boolean isSuccess = false;
		try{
			if(!StringUtil.isEmpty(cloudRDSInstance.getRdsId())){
				BaseCloudRDSInstance succRdsInstance = new BaseCloudRDSInstance();
				BeanUtils.copyProperties(succRdsInstance, cloudRDSInstance);
				RDSInstance rdsInstance = openstackRDSInstanceService.getById(cloudRDSInstance.getDcId(), 
						cloudRDSInstance.getPrjId(), cloudRDSInstance.getRdsId());
				if("ERROR".equals(rdsInstance.getStatus().toUpperCase())){
					isSuccess = true;
					CloudOrderRDSInstance order = new CloudOrderRDSInstance();
					order.setOrderNo(cloudRDSInstance.getOrderNo());
					order.setCreateUser(cloudRDSInstance.getCreateName());
					order.setCusId(cloudRDSInstance.getCusId());
					rdsInstanceService.createFailHandler(order, succRdsInstance);
				}else if ("ACTIVE".equals(rdsInstance.getStatus().toUpperCase())){
					CloudOrderRDSInstance order = new CloudOrderRDSInstance();
					order.setOrderNo(cloudRDSInstance.getOrderNo());
					order.setCreateUser(cloudRDSInstance.getCreateName());
					order.setCusId(cloudRDSInstance.getCusId());
					order.setDcId(cloudRDSInstance.getDcId());
					order.setPrjId(cloudRDSInstance.getPrjId());
					order.setRdsId(cloudRDSInstance.getRdsId());
					order.setPayType(cloudRDSInstance.getPayType());
					// 用于计费
					order.setCpu(cloudRDSInstance.getCpu());
					order.setRam(cloudRDSInstance.getRam());
					order.setVolumeSize(cloudRDSInstance.getVolumeSize());
					order.setVolumeTypeName(cloudRDSInstance.getVolumeTypeName());
					order.setPassword(cloudRDSInstance.getPassword());
					succRdsInstance.setVmId(rdsInstance.getVmId());
					rdsInstanceService.enableRootAndlog(order, succRdsInstance);
					isSuccess = true;
				}
			}
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw e;
		}
		return isSuccess;
	}

	/**
	 * 获取底层指定ID的资源，底层异常为null
	 * ------------------
	 * @param valueJson
	 * 
	 */
	@Override
	public JSONObject get(JSONObject valueJson) throws Exception{
		JSONObject result = null ;
		if(null!=valueJson){
			JSONObject json = openstackRDSInstanceService.get(valueJson.getString("dcId"), 
					valueJson.getString("prjId"), valueJson.getString("rdsId"));
			if(null!=json){
				boolean isDeleted=json.containsKey("itemNotFound");
				if(!isDeleted){
					result=json.getJSONObject(OpenstackUriConstant.RDS_DATA_NAME);
				}
				else{
					result =new JSONObject();
					result.put("deletingStatus", isDeleted+"");
				}
			}
		}
		return result;
		
	}

	/**
	 * 更新云数据库实例的状态
	 * 
	 * @param cloudRDSInstance
	 * 						-- 云数据库实例信息
	 * @param isConfig
	 * 						-- 是否需要更新配置文件
	 */
	@Override
	public void updateRdsInstance(CloudRDSInstance cloudRDSInstance, String status, 
			boolean isConfig) {
		int index = 0;
		Object[] args = new Object[10];
		StringBuffer sql = new StringBuffer();
		sql.append(" update cloud_rdsinstance set rds_status =  ? ");
		args[index++] = status;
		if(!StringUtil.isEmpty(cloudRDSInstance.getRdsStatus()) && 
				"DETACH".equals(cloudRDSInstance.getRdsStatus())){
			sql.append(", is_master = '1' ");
			sql.append(", master_id = '' ");
		}
		if(isConfig && !StringUtil.isEmpty(cloudRDSInstance.getConfigId())){
			sql.append(", config_id = ? ");
			args[index++] = cloudRDSInstance.getConfigId();
		}
		sql.append(" where rds_id = ? ");
		args[index++] = cloudRDSInstance.getRdsId();
		Object[] params = new Object[index];
		System.arraycopy(args, 0, params, 0, index);
		cloudRDSInstanceDao.execSQL(sql.toString(), params);
	}

	@Override
	public void deleteRdsInstance(CloudRDSInstance cloudRDSInstance) throws Exception {
		// 修改云数据库实例的状态
		StringBuffer sql = new StringBuffer();
		sql.append(" update cloud_rdsinstance set is_deleted =  '1' ");
		sql.append(" where rds_id = ? ");
		cloudRDSInstanceDao.execSQL(sql.toString(), new Object[]{cloudRDSInstance.getRdsId()});
		// 删除数据库实例对应的数据库和用户（只删除上层数据库中的数据）
		rdsDatabaseService.deleteAllDatabaseByInstanceId(cloudRDSInstance.getRdsId());
		rdsAccountService.deleteAllAccountByInstanceId(cloudRDSInstance.getRdsId());
		// 更新删除备份的时间
		rdsBackupService.handleBackupsOfDeletedInstance(cloudRDSInstance.getRdsId());
		//清除报警信息 
		alarmService.cleanAlarmDataAfterDeletingVM(cloudRDSInstance.getVmId());
		// 清除监控信息
		alarmService.deleteMonitorByResource(MonitorAlarmUtil.MonitorResourceType.RDS.toString(), cloudRDSInstance.getVmId());
	}

	/**
	 * 升级云数据库实例规格成功后，升级数据盘大小
	 * 
	 * @param cloudRDSInstance
	 * 						-- 待升级的云数据库实例的信息
	 * @throws Exception
	 * 
	 */
	@Override
	@Transactional(noRollbackFor=AppException.class)
	public void resizeRdsInstanceVolume(CloudRDSInstance cloudRDSInstance, String status) throws Exception {
		if("ACTIVE".equals(status)){
			// 此次升级成功，调用升级成功接口
			rdsInstanceService.resizeRdsInstanceVolume(cloudRDSInstance, true);
		}else{
			// 升级失败，调用订单失败接口
			rdsInstanceService.upgradeFailHandler(cloudRDSInstance, false);
			// 将底层的状态回写到上层
			this.updateRdsInstance(cloudRDSInstance, status, false);
		}
	}

	/**
	 * 升级云数据库实例的规格和云硬盘的大小均成功
	 * 
	 * @param cloudRDSInstance
	 * 						-- 待升级的云数据库实例的信息
	 * @throws Exception
	 * 
	 */
	@Override
	public void upgradeSuccess(CloudRDSInstance cloudRDSInstance, String status) throws Exception {
		if("ACTIVE".equals(status)){
			// 此次升级成功，调用升级成功接口
			rdsInstanceService.upgradeSuccessHandler(cloudRDSInstance);
		}else{
			// 升级失败，调用订单失败接口
			rdsInstanceService.upgradeFailHandler(cloudRDSInstance, false);
			// 将底层的状态回写到上层
			this.updateRdsInstance(cloudRDSInstance, status, false);
		}
	}

	@Override
	public void rebootSuccessForDetach(CloudRDSInstance cloudRDSInstance) throws Exception {
		rdsInstanceService.rdsInstanceAttachConfiguration(cloudRDSInstance);
	}
	
	/**
	 * 底层资源同步
	 * 
	 * @param dataCenter
	 * 					-- 数据中心信息
	 * @param prjId
	 * 					-- 项目ID
	 * @throws Exception
	 * 
	 */
	@Override
	public void synchData(BaseDcDataCenter dataCenter, String prjId) throws Exception{
		Map<String, BaseCloudRDSInstance> dbMap = new HashMap<String, BaseCloudRDSInstance>();
        Map<String, RDSInstance> stackMap = new HashMap<String, RDSInstance>();
        try {
            List<BaseCloudRDSInstance> dbList= queryCloudRdsInstanceListByDcId(dataCenter.getId(), prjId);
            
            List<RDSInstance> stackList = openstackRDSInstanceService.getStackList(dataCenter, prjId);
            /*map存储上层数据库资源数据*/
            if (null != dbList) {
                for (BaseCloudRDSInstance rdsInstance : dbList) {
                    dbMap.put(rdsInstance.getRdsId(), rdsInstance);
                }
            }
            long total = stackList == null ? 0L : stackList.size();
            syncProgressUtil.initResourceTotal(dataCenter.getId(), SyncType.PROJECT, SyncByProjectTypes.RDS, total);
            /*底层数据更新本地数据库*/
            if (null != stackList) {
                for (RDSInstance rdsInstance : stackList) {
                	if(dbMap.containsKey(rdsInstance.getId())){  
                        //底层数据存在本地数据库中 更新本地数据
                		updateRdsInstanceFromStack(rdsInstance);
                    } else {
                        /*底层有 上层没有的数据 添加进本地数据库 不可见*/
                    	saveRdsInstanceFromStack(rdsInstance, dataCenter.getId(), prjId);
                    }
                    stackMap.put(rdsInstance.getId(), rdsInstance);
                    if(rdsInstance.getReplica_of() == null && rdsInstance.getStatus().equalsIgnoreCase("ACTIVE")){ // 数据库实例为主库,主库的数据库和用户需要同步到cloud层
                    	cloudDatabaseService.synchData(dataCenter.getId(), prjId, rdsInstance.getId());
						cloudDBUserService.synchData(dataCenter.getId(), prjId, rdsInstance.getId());
                    }
                    syncProgressUtil.incrResourceDone(dataCenter.getId(), SyncType.PROJECT, SyncByProjectTypes.RDS);
                }
            }
            /*删除本地存在 底层不存在的数据资源*/
            if (null != dbList) {
                for (BaseCloudRDSInstance rdsInstance : dbList) {
                    //删除本地数据库中不存在于底层的数据
                    if (!stackMap.containsKey(rdsInstance.getRdsId())) {
                    	StringBuffer sql = new StringBuffer();
            			sql.append(" update cloud_rdsinstance set ");
            			sql.append("	is_deleted = ?,          ");
            			sql.append("	delete_time = ?          ");
            			sql.append(" where rds_id = ? ");
            			
            			cloudRDSInstanceDao.execSQL(sql.toString(), new Object[]{
            					"1",
            					new Date(),
            					rdsInstance.getRdsId()
            			});  
    					ecmcLogService.addLog("同步资源清除数据",  toType(rdsInstance), rdsInstance.getRdsName(), rdsInstance.getPrjId(),1,rdsInstance.getRdsId(),null);
    					
    					JSONObject json = new JSONObject();
    					json.put("resourceType", ResourceSyncConstant.RDS);
    					json.put("resourceId", rdsInstance.getRdsId());
    					json.put("resourceName", rdsInstance.getRdsName());
    					json.put("synTime", new Date());
    					jedisUtil.push(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, json.toJSONString());
    					// 删除数据库实例对应的数据库和用户（只删除上层数据库中的数据）
						rdsDatabaseService.deleteAllDatabaseByInstanceId(rdsInstance.getRdsId());
						rdsAccountService.deleteAllAccountByInstanceId(rdsInstance.getRdsId());
                    }
                }
            }
        }catch (Exception e) {
            log.error(e.getMessage(),e);
			throw  e;
        }
	}

	
	private void saveRdsInstanceFromStack(RDSInstance rdsInstance, String dcId, String prjId) throws Exception {
		BaseCloudRDSInstance cloudRDSInstance = new BaseCloudRDSInstance();
		cloudRDSInstance.setRdsId(rdsInstance.getId());
		cloudRDSInstance.setRdsName(rdsInstance.getName());
		cloudRDSInstance.setRdsStatus(rdsInstance.getStatus());
		cloudRDSInstance.setRdsIp(rdsInstance.getIp()[0]);
		cloudRDSInstance.setFlavorId(rdsInstance.getFlavor().getId());
		cloudRDSInstance.setVersionId(getVersionId(rdsInstance.getDatastore().getType(), rdsInstance.getDatastore().getVersion()));
		cloudRDSInstance.setPrjId(prjId);
		cloudRDSInstance.setDcId(dcId);
		cloudRDSInstance.setIsDeleted("0");
		cloudRDSInstance.setIsVisible("0");
		if (rdsInstance.getReplica_of() != null) {
			cloudRDSInstance.setMasterId(rdsInstance.getReplica_of().getId());
		}
		cloudRDSInstanceDao.saveOrUpdate(cloudRDSInstance);
	}

	private boolean updateRdsInstanceFromStack(RDSInstance rdsInstance) {
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_rdsinstance set ");
			sql.append("	rds_name = ?,         ");
			sql.append("	rds_status = ?,       ");
			sql.append("	flavor_id = ?,        ");
			sql.append("	rds_ip = ?,           ");
			sql.append("	version_id = ?,       ");
			sql.append("	volume_size = ?,      ");
			sql.append("	is_deleted = ?        ");
			sql.append("    where rds_id = ?      ");
			
			cloudRDSInstanceDao.execSQL(sql.toString(), new Object[]{
					rdsInstance.getName(),
					rdsInstance.getStatus(),
					rdsInstance.getFlavor().getId(),
					rdsInstance.getIp()[0],
					getVersionId(rdsInstance.getDatastore().getType(), rdsInstance.getDatastore().getVersion()),
					rdsInstance.getVolume().getSize(),
					"0",
					rdsInstance.getId()
			});
			flag = true ;
		}catch(Exception e){
			flag = false;
			log.error(e.getMessage(),e);
			throw e;
		}
		return flag ;
	}

	/**
	 * 获取上层数据库对应的数据
	 * 
	 * @param dcId  
	 * 				-- 数据中心ID
	 * @param prjId
	 * 				-- 项目ID
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<BaseCloudRDSInstance> queryCloudRdsInstanceListByDcId(String dcId, String prjId) {
		StringBuffer hql = new StringBuffer();
        hql.append(" from BaseCloudRDSInstance ");
        hql.append(" where dcId = ? and prjId = ? and isDeleted = '0'");
        
        return cloudRDSInstanceDao.find(hql.toString(), new Object[]{dcId, prjId});
	}
	
	private String toType(BaseCloudRDSInstance rds){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StringBuffer resourceType = new StringBuffer();
		resourceType.append(ResourceSyncConstant.RDS);
		resourceType.append("-").append(CloudResourceUtil.escapePayType(rds.getPayType())).append(ResourceSyncConstant.SEPARATOR);
		if (null != rds.getEndTime()) {
			resourceType.append("创建时间：").append(sdf.format(rds.getCreateTime()));
		}
		if(PayType.PAYBEFORE.equals(rds.getPayType()) && null != rds.getEndTime()){
			resourceType.append(ResourceSyncConstant.SEPARATOR).append("到期时间：").append(sdf.format(rds.getEndTime()));
		}
		
		return resourceType.toString();
	}
	
	/**
	 * 根据公共租户的项目获取公共租户的Id 和默认安全组的ID
	 * @return
	 */
	private String getVersionId(String type, String name){
		StringBuffer sql = new StringBuffer();
		sql.append(" select cdv.id ");
		sql.append(" from cloud_datastoreversion cdv  ");
		sql.append(" LEFT JOIN cloud_datastore cd on cdv.datastore_id = cd.id ");
		sql.append(" where cdv.name = ? and cd.name = ?");
		javax.persistence.Query query = cloudRDSInstanceDao.createSQLNativeQuery(sql.toString(), new Object[]{name, type});
		Object result = query.getSingleResult();
		if(null != result)
			return result.toString();
		return null;
	}
	/**
	 * 获取项目下未删除的实例列表
	 * 用于计划任务查询磁盘使用率
	 * @Author: duanbinbin
	 * @param projectId
	 * @return
	 *<li>Date: 2017年3月7日</li>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<CloudRDSInstance> getRDSListByPrjId(String projectId) {
		StringBuffer hql = new StringBuffer(" from BaseCloudRDSInstance where prjId = ? and isDeleted = '0' and isVisible = '1' ");
		List<BaseCloudRDSInstance> rdsList =  cloudRDSInstanceDao.find(hql.toString(), projectId);
		List<CloudRDSInstance> resultList = new ArrayList<CloudRDSInstance>();
		if(!rdsList.isEmpty()){
			for(BaseCloudRDSInstance baserds: rdsList){
				CloudRDSInstance rds = new CloudRDSInstance();
				BeanUtils.copyPropertiesByModel(rds, baserds);
				resultList.add(rds);
			}
		}
		return resultList;
	}

	/**
	 * 从库升主库成功以后，需要给用户启用root用户并且发送信息，并且同步底层的数据库和用户信息
	 * @param cloudRDSInstance
	 * 						-- 实例信息
	 * @param status
	 * 						-- 底层实例的状态
	 * @throws Exception 
	 */
	@Override
	public void detachReplicaSuccess(CloudRDSInstance cloudRDSInstance, String status) throws Exception {
		// 更新实例的状态（更新后的状态为底层返回的状态）
		this.updateRdsInstance(cloudRDSInstance, status, false);
		if("ACTIVE".equalsIgnoreCase(status)){// 从库升主库成功
			// 启用root用户,并且发送短信
			CloudRDSAccount  account = new CloudRDSAccount();
			account.setDcId(cloudRDSInstance.getDcId());
			account.setPrjId(cloudRDSInstance.getPrjId());
			account.setInstanceId(cloudRDSInstance.getRdsId());
			account.setAccountName("root");
			account.setInstanceName(cloudRDSInstance.getRdsName()); // 用户给客户发送短信
			account.setRemark("");
			rdsAccountService.createAccountRoot(account);
			// 创建完成以后需要将底层的数据库和用户同步上来
			rdsDatabaseService.synchronDBCreate(cloudRDSInstance.getDcId(), cloudRDSInstance.getPrjId(), cloudRDSInstance.getRdsId());
			rdsAccountService.synchronAccountCreate(cloudRDSInstance.getDcId(), cloudRDSInstance.getPrjId(), cloudRDSInstance.getRdsId());
		}
	}

	@Override
	public void deleteBuildInstance(CloudRDSInstance cloudRDSInstance) throws Exception {
		try{
			// 重置实例的状态为ERROR
			openstackRDSInstanceService.resetStatus(cloudRDSInstance.getDcId(), cloudRDSInstance.getRdsId());
			// 删除实例
			openstackRDSInstanceService.delete(cloudRDSInstance.getDcId(), cloudRDSInstance.getPrjId(), cloudRDSInstance.getRdsId());

			// is_deleted需要置为1，表示已删除
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_rdsinstance set is_deleted =  '1' ");
			sql.append(" where rds_id = ? ");
			cloudRDSInstanceDao.execSQL(sql.toString(), new Object[]{cloudRDSInstance.getRdsId()});
		}catch (Exception e){
			// 删除失败需要发送删除失败的邮件
			MessageOrderResourceNotice resource = new MessageOrderResourceNotice();
			resource.setOrderNo(cloudRDSInstance.getOrderNo());
			resource.setResourceId(cloudRDSInstance.getRdsId());
			resource.setResourceName(cloudRDSInstance.getRdsName());
			resource.setResourceType(ResourceType.getName(ResourceType.RDS));
			List<MessageOrderResourceNotice> resources = new ArrayList<MessageOrderResourceNotice>();
			resources.add(resource);
			messageCenterService.delecteResourFailMessage(resources, cloudRDSInstance.getOrderNo());
			log.error(e.getMessage(),e);
			throw e;
		}finally {
			// 调用资源创建失败的接口
			orderService.completeOrder(cloudRDSInstance.getOrderNo(), false, null);
			// 发送购买资源失败的邮件
			messageCenterService.addResourFailMessage(cloudRDSInstance.getOrderNo(), cloudRDSInstance.getCusId());
		}
	}
}
