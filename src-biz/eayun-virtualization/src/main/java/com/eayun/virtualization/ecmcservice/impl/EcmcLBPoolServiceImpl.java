package com.eayun.virtualization.ecmcservice.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.charge.model.ChargeRecord;
import com.eayun.common.constant.EayunQueueConstant;
import com.eayun.common.constant.PayType;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceSyncConstant;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.BoolUtil;
import com.eayun.common.util.CloudResourceUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.eayunstack.model.Pool;
import com.eayun.eayunstack.service.OpenstackFloatIpService;
import com.eayun.eayunstack.service.OpenstackPoolService;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.monitor.bean.MonitorAlarmUtil.MonitorResourceType;
import com.eayun.monitor.ecmcservice.EcmcAlarmService;
import com.eayun.monitor.service.AlarmService;
import com.eayun.virtualization.baseservice.BasePoolService;
import com.eayun.virtualization.dao.CloudFloatIpDao;
import com.eayun.virtualization.dao.CloudLdMemberDao;
import com.eayun.virtualization.dao.CloudLdPoolDao;
import com.eayun.virtualization.dao.CloudLdPoolMonitorDao;
import com.eayun.virtualization.dao.CloudLdVipDao;
import com.eayun.virtualization.dao.CloudSubNetWorkDao;
import com.eayun.virtualization.ecmcservice.EcmcLBPoolService;
import com.eayun.virtualization.ecmcservice.EcmcLBVipService;
import com.eayun.virtualization.ecmcvo.CloudLdpoolVoe;
import com.eayun.virtualization.model.BaseCloudFloatIp;
import com.eayun.virtualization.model.BaseCloudLdPool;
import com.eayun.virtualization.model.BaseCloudLdPoolMonitor;
import com.eayun.virtualization.model.BaseCloudLdVip;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.CloudLdPool;
import com.eayun.virtualization.model.CloudLdVip;
import com.eayun.virtualization.service.TagService;

@Service
@Transactional
public class EcmcLBPoolServiceImpl extends BasePoolService implements EcmcLBPoolService {
	private static final Log log = LogFactory.getLog(EcmcLBPoolServiceImpl.class);
    
	@Autowired
	private OpenstackPoolService openstackPoolService;
	@Autowired
	private OpenstackFloatIpService openstackFloatIpService;
	@Autowired
	private CloudLdPoolDao cloudLdpoolDao;
	@Autowired
	private CloudLdMemberDao cloudLdmemberDao;
	@Autowired
	private CloudLdPoolMonitorDao cloudLdPoolMonitorDao;
	@Autowired
	private CloudSubNetWorkDao cloudSubNetworkDao;
	@Autowired
	private CloudLdVipDao cloudLdVipDao;
	@Autowired
	private CloudFloatIpDao cloudFloatIpDao;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private TagService tagService;
	@Autowired
	private EcmcLBVipService vipService;
	@Autowired
	private EayunRabbitTemplate rabbitTemplate;
	@Autowired
	private EcmcLogService ecmcLogService;
	@Autowired
	private EcmcAlarmService ecmcAlarmService;
	
	
	public Page queryPool(ParamsMap paramsMap){
		Map<String, Object> params = paramsMap.getParams();
		List<Object> listParam = new ArrayList<Object>();

		StringBuffer sql = new StringBuffer();
		sql.append("select t.pool_id,t.pool_name,t.prj_id,t.dc_id,t.pool_description,t.pool_provider,t.subnet_id,");
		sql.append("vip.vip_id,t.pool_protocol,t.lb_method,t.pool_status,t.admin_stateup,t.create_name,t.create_time,");
		sql.append("dc.dc_name, cp.prj_name, c.cus_id,c.cus_org,sub.subnet_name,vip.vip_name, f.flo_id,");
		sql.append(" (select count(lm.pool_id) from cloud_ldpoolldmonitor lm where lm.pool_id = t.pool_id) as countNum");
		sql.append(" from cloud_ldpool as t ");
		sql.append(" left join dc_datacenter as dc on dc.id=t.dc_id");
		sql.append(" left join cloud_project as cp on t.prj_id = cp.prj_id ");
		sql.append(" left join sys_selfcustomer as c on cp.customer_id = c.cus_id ");
		sql.append(" left join cloud_subnetwork as sub on sub.subnet_id = t.subnet_id ");
		sql.append(" left join cloud_ldvip as vip on vip.pool_id = t.pool_id ");
		sql.append(" left join cloud_floatip as f on f.resource_type = 'lb' and f.resource_id = t.pool_id ");
		sql.append(" where 1=1 ");
		int idx = 0;
		// 数据中心
		if (StringUtils.isNotBlank((String)params.get("dcId"))) {
			sql.append(" and t.dc_id = ?").append(++idx);
			listParam.add(params.get("dcId"));
		}
		//项目名称
		if (StringUtils.isNotBlank((String)params.get("prjName"))) {
			sql.append(" and cp.prj_name in(?").append(++idx).append(")");
			listParam.add(Arrays.asList(StringUtils.split((String)params.get("prjName"), ",")));
		}
		// 监控类型
		if (StringUtils.isNotBlank((String)params.get("cusOrg"))) {
			sql.append(" and c.cus_org in(?").append(++idx).append(")");
			listParam.add(Arrays.asList(StringUtils.split((String)params.get("cusOrg"), ",")));
		}
		if (StringUtils.isNotBlank((String)params.get("poolName"))) {
			sql.append(" and t.pool_name like ?").append(++idx);
			listParam.add("%" + (String)params.get("poolName") + "%");
		}
		sql.append(" order by t.create_time desc");
		QueryMap queryMap = new QueryMap();
    	queryMap.setPageNum(paramsMap.getPageNumber() == null ? 1 : paramsMap.getPageNumber());
    	queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize() == null ? 10 : paramsMap.getPageSize());
		Page page = cloudLdpoolDao.pagedNativeQuery(sql.toString(), queryMap,listParam.toArray());
		
		@SuppressWarnings("unchecked")
		List<Object> listResult = (List<Object>) page.getResult();
		for (int i = 0; i < listResult.size(); i++) {
			Object[] objs = (Object[]) listResult.get(i);
			CloudLdpoolVoe voe = new CloudLdpoolVoe();
			voe.setPoolId(ObjectUtils.toString(objs[0]));
			voe.setPoolName(ObjectUtils.toString(objs[1]));
			voe.setPrjId(ObjectUtils.toString(objs[2]));
			voe.setDcId(ObjectUtils.toString(objs[3]));
			voe.setPoolDescription(ObjectUtils.toString(objs[4]));
			voe.setPoolProvider(ObjectUtils.toString(objs[5]));
			voe.setSubnetId(ObjectUtils.toString(objs[6]));
			voe.setVipId(ObjectUtils.toString(objs[7]));
			voe.setPoolProtocol(ObjectUtils.toString(objs[8]));
			voe.setLbMethod(ObjectUtils.toString(objs[9]));
			voe.setPoolStatus(ObjectUtils.toString(objs[10]));
			voe.setAdminStateup((Character)objs[11]);
			voe.setCreateName(ObjectUtils.toString(objs[12]));
			voe.setCreateTime(DateUtil.stringToDate(objs[13] == null ? "" : ObjectUtils.toString(objs[13])));
			voe.setDcName(ObjectUtils.toString(objs[14]));
			voe.setProjectName(ObjectUtils.toString(objs[15]));
			voe.setCusId(ObjectUtils.toString(objs[16]));
			voe.setCusOrg(ObjectUtils.toString(objs[17]));
			voe.setSubnetName(ObjectUtils.toString(objs[18]));
			voe.setVipName(ObjectUtils.toString(objs[19]));
			voe.setFloatId(ObjectUtils.toString(objs[20]));
			//管理员状态
			voe.setAdminStateupStr(BoolUtil.bool2Str(voe.getAdminStateup()));
			//查询资源池绑定监控数量
			voe.setCountNum(ObjectUtils.toString(objs[21]));
			voe.setLbMethodName(voe.getLbMethod());
			listResult.set(i, voe);
		}

		return page;
	}
	
	public CloudLdpoolVoe getById(String poolId) throws AppException{
		BaseCloudLdPool cloudLdpool = cloudLdpoolDao.findOne(poolId);
		if (cloudLdpool == null) {
			return null;
		}
		
		CloudLdpoolVoe result= new CloudLdpoolVoe();
		BeanUtils.copyPropertiesByModel(result, cloudLdpool);
		
		// 1.子网
		String subnetName = "";
		BaseCloudSubNetWork cloudSubNetwork = cloudSubNetworkDao.findOne(cloudLdpool.getSubnetId());
		if (cloudSubNetwork != null) {
			subnetName = cloudSubNetwork.getSubnetName();
		}
		result.setSubnetName(subnetName);

		// 2.VIP
		String vipName ="";
		if(cloudLdpool.getVipId()!=null&&!"".equals(cloudLdpool.getVipId())){
			BaseCloudLdVip cloudVip = cloudLdVipDao.findOne(cloudLdpool.getVipId());
			if (cloudVip != null) {
				vipName = cloudVip.getVipName();
			}
		}
		
		result.setVipName(vipName);
		
		// 3.项目
		result.setProjectName(cloudLdpoolDao.getProjectName(cloudLdpool.getPrjId()));
		
		// 4.管理员状态
		result.setAdminStateupStr(BoolUtil.bool2Str(cloudLdpool
				.getAdminStateup()));
		
		return result;
	}
	
	public boolean checkPoolName(String prjId, String poolName, String poolId) {
		return cloudLdpoolDao.countPoolName(prjId, poolName, poolId) > 0 ? true : false;
	}
	
	/**
	 * 添加资源池
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 * @throws AppException
	 */
	public CloudLdpoolVoe createPool(BaseCloudLdPool pool) throws AppException{
		
		JSONObject data=new JSONObject();
		JSONObject temp=new JSONObject();
		temp.put("name", pool.getPoolName());
		temp.put("protocol", pool.getPoolProtocol());
		temp.put("subnet_id", pool.getSubnetId());
		temp.put("lb_method", pool.getLbMethod());
		temp.put("admin_state_up", "1");
		temp.put("description", pool.getPoolDescription());
		data.put("pool", temp);
		
		if(this.checkPoolName(pool.getDcId(), pool.getPoolName(), null)){
			throw new AppException("该资源池在当前数据中心中已存在");
		}
		Pool result=openstackPoolService.create(pool.getDcId(), pool.getPrjId(), data);
		CloudLdpoolVoe cloudLdpoolVoe = null;
		if(result!=null){
			pool.setPoolId(result.getId());
			pool.setPoolProvider(result.getProvider());
			pool.setPoolStatus(result.getStatus().toUpperCase());
			pool.setAdminStateup(BoolUtil.bool2char(result.isAdmin_state_up()));
			BaseEcmcSysUser user = EcmcSessionUtil.getUser();
			pool.setCreateName(user == null ? null : user.getAccount());
			
			pool.setCreateTime(new Date());
			
			cloudLdpoolDao.save(pool);
			
			if(null!=result.getStatus()&&!"ACTIVE".equals(result.getStatus())){
				// 同步新增资源池状态
				JSONObject json =new JSONObject();
				json.put("poolId", pool.getPoolId());
				json.put("dcId",pool.getDcId());
				json.put("prjId", pool.getPrjId());
				json.put("poolStatus", pool.getPoolStatus());
				json.put("count", "0");
				try {
					jedisUtil.push(RedisKey.ldPoolKey, json.toJSONString());
				} catch (Exception e) {
					throw new AppException("更新缓存出错");
				}
			}
			
			cloudLdpoolVoe = new  CloudLdpoolVoe();
			BeanUtils.copyPropertiesByModel(cloudLdpoolVoe, pool);
		}
		return cloudLdpoolVoe;

	}

	/**
	 * 绑定监控

	 * @return
	 * @throws AppException
	 */
	public boolean bindHealthMonitor(String poolId, String healthMonitorId) throws AppException {
		
		BaseCloudLdPool pool = cloudLdpoolDao.findOne(poolId);
		if (pool == null) {
			return false;
		}
		
		if(openstackPoolService.bind(pool.getDcId(), pool.getPrjId(), poolId, healthMonitorId)){
			BaseCloudLdPoolMonitor pm=new BaseCloudLdPoolMonitor();
			pm.setLdmId(healthMonitorId);
			pm.setPoolId(poolId);
			cloudLdPoolMonitorDao.saveEntity(pm);
			return true;
		}else{
			return false;
		}
		
	}
	/**
	 * 修改资源池
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public CloudLdpoolVoe update(CloudLdPool pool) throws AppException {
		
		BaseCloudLdPool oldPool = cloudLdpoolDao.findOne(pool.getPoolId());
		if (oldPool == null) {
			throw new AppException("该资源池在不存在");
		}
		
		if (this.checkPoolName(oldPool.getPrjId(), pool.getPoolName(), pool.getPoolId())) {
			// 改名，判断重名
			throw new AppException("该资源池在当前数据中心已存在");
		}
		
		//拼装用于提交的数据
		JSONObject data=new JSONObject();
		JSONObject temp=new JSONObject();
		temp.put("name", pool.getPoolName());
		temp.put("lb_method", pool.getLbMethod());
		temp.put("admin_state_up", pool.getAdminStateup());
		temp.put("description", pool.getPoolDescription());
		data.put("pool", temp);
		
		try{
			Pool result=openstackPoolService.update(oldPool.getDcId(), oldPool.getPrjId(), data, pool.getPoolId());
			CloudLdpoolVoe cloudLdpoolVoe = null;
			if (result!=null) {
				oldPool.setPoolName(pool.getPoolName());
				oldPool.setAdminStateup(pool.getAdminStateup());
				oldPool.setPoolDescription(pool.getPoolDescription());
				oldPool.setPoolStatus(result.getStatus());
				oldPool.setLbMethod(pool.getLbMethod());
				//数据库保存操作
				cloudLdpoolDao.saveOrUpdate(oldPool);
				
				if(null!=result.getStatus()&&!"ACTIVE".equals(result.getStatus())){
					//TODO 启动资源池的自动任务
					JSONObject json =new JSONObject();
					json.put("poolId", oldPool.getPoolId());
					json.put("dcId",oldPool.getDcId());
					json.put("prjId", oldPool.getPrjId());
					json.put("poolStatus", oldPool.getPoolStatus());
					json.put("count", "0");
					jedisUtil.addUnique(RedisKey.ldPoolKey, json.toJSONString());
				}
				
				cloudLdpoolVoe = new CloudLdpoolVoe();
				BeanUtils.copyPropertiesByModel(cloudLdpoolVoe, oldPool);
			}
			return cloudLdpoolVoe;
	    } catch (AppException e) {
	    	 throw e; 
	    } catch (Exception e) {
	    	log.error(e, e);
	    	return  null;
    	}
	}

	/**
	 * 删除一条资源池
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public boolean delete(String poolId) throws AppException{
		BaseCloudLdPool pool = cloudLdpoolDao.findOne(poolId);
		if (pool == null) {
			return false;
		}
		
		//执行openstack删除操作成功后，进行后续操作
		if (openstackPoolService.delete(pool.getDcId(), pool.getPrjId(), poolId)) {
			cloudLdpoolDao.delete(poolId);
			//删除资源池后，将成员下所有该资源池的成员删除
			//因为删除资源池的时候，底层自动默认会删除在此基础上创建的成员，所以下面只删除本地库的member
			cloudLdmemberDao.deleteByPoolId(poolId);
			
			//删除资源后更新缓存接口
			tagService.refreshCacheAftDelRes("ldPool", poolId);
			ecmcAlarmService.deleteMonitorByResource(MonitorResourceType.POOL.toString(), pool.getPoolId());
			return true;
		}
		return false;
	}

	@Override
	public int getCountByPrjId(String prjId) {
		int poolCount = cloudLdpoolDao.getCountByPrjId(prjId);
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
        Query query = cloudLdpoolDao.createSQLNativeQuery(sql.toString(), prjId);
        Object result = query.getSingleResult();
        int orderCount = result == null ? 0 : Integer.parseInt(result.toString());
        return orderCount; 
	}
	
	public boolean bindFloatIp(String poolId, String floatId, String vipId) throws AppException{
		BaseCloudLdPool pool = cloudLdpoolDao.findOne(poolId);
		if(pool!= null){
			BaseCloudLdVip vip = cloudLdVipDao.findOne(vipId);
			//调用底层绑定
			if(vip!= null && openstackFloatIpService.bindLoadBalancerFloatIp(pool.getDcId(), pool.getPrjId(), vip.getPortId(), floatId)){
				//修改本地数据库
				return this.bindFloatIpToDB(floatId, poolId);
			}
		}
		return false;
	}
	
	/**
	 * 负载均衡绑定floatIp，修改数据库
	 * @param floatId
	 * @param poolId
	 * @return
	 * @throws AppException
	 */
	protected boolean bindFloatIpToDB(String floatId, String poolId) throws AppException{
		return cloudFloatIpDao.updateResourceByFloatId(floatId, poolId, "lb") > 0 ? true : false;
	}
	
	public boolean unbindFloatIp(String poolId, String floatId) throws AppException {
		BaseCloudFloatIp floatIp = cloudFloatIpDao.findOne(floatId);
		BaseCloudLdPool pool = cloudLdpoolDao.findOne(poolId);
		if(floatIp != null && pool != null && openstackFloatIpService.bindLoadBalancerFloatIp(pool.getDcId(), pool.getPrjId(), null, floatIp.getFloId())){
			//修改本地数据库
			return this.unbindFloatIpFromDB(floatId);
		}
		return false;
	}
	
	/**
	 * 接触本地数据库中负载均衡和floatIP的绑定
	 * @param floatId
	 * @return
	 */
	protected boolean unbindFloatIpFromDB(String floatId){
		return cloudFloatIpDao.updateResourceByFloatId(floatId, null, null) > 0 ? true : false;
	}
	
	public List<Map<String, Object>> getNotbindFloatIpPools(String subnetId) throws AppException {
		StringBuffer sql = new StringBuffer();
		sql.append("select p.pool_id,p.pool_name,vip.vip_address");
		sql.append(" from cloud_ldvip vip  LEFT JOIN cloud_ldpool p ON p.pool_id = vip.pool_id ");
		sql.append(" where p.pool_status = 'ACTIVE' and p.subnet_id = ? and p.pool_id not in( ");
		sql.append(" select resource_id from cloud_floatip f where f.resource_type = 'lb' ");
		sql.append(" and f.is_deleted = '0'");
		sql.append(" and f.charge_state = '0'");
		sql.append(")");
		sql.append(" and p.charge_state = '0' ");
		@SuppressWarnings("unchecked")
		List<Object[]> dataList = cloudLdpoolDao.createSQLNativeQuery(sql.toString(), new Object[]{subnetId}).getResultList();
		List<Map<String, Object>> resultList = null;
		if(CollectionUtils.isNotEmpty(dataList)){
			resultList = new ArrayList<Map<String, Object>>();
			for (Object[] objects : dataList) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("poolId", ObjectUtils.toString(objects[0]));
				map.put("poolName", ObjectUtils.toString(objects[1]));
				map.put("vipAddress", ObjectUtils.toString(objects[2]));
				resultList.add(map);
			}
		}
		return resultList;
	}
	
	@SuppressWarnings("unchecked")
	public List<CloudLdPool> getPoolList(String dcId, String prjId) throws AppException {
		StringBuffer hql = new StringBuffer();
		hql.append("select new com.eayun.virtualization.model.CloudLdPool(pool) from BaseCloudLdPool pool where 1=1 and (pool.vipId is null or pool.vipId = '')");
		List<Object> args = new ArrayList<>();
		if(StringUtils.isNotBlank(dcId)){
			hql.append(" and pool.dcId = ?");
			args.add(dcId);
		}
		if(StringUtils.isNotBlank(prjId)){
			hql.append(" and pool.prjId = ?");
			args.add(prjId);
		}
		return cloudLdpoolDao.find(hql.toString(), args.toArray());
	}

	@Override
	public Page getPoolList(String dcId, String poolName, String cusOrg, String prjName, QueryMap queryMap) throws Exception {
		StringBuffer sql = new StringBuffer();
		Object[] args = new Object[10];
		int index = 0;

		sql.append("	SELECT                                                                  ");
		sql.append("		pool.dc_id,                                                         ");
		sql.append("		pool.prj_id,                                                        ");
		sql.append("		pool.pool_id,                                                       ");
		sql.append("		pool.pool_name,                                                     ");
		sql.append("		pool.pool_status,                                                   ");
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
		sql.append("	END AS isCheckMonitor ,                                                  ");
		sql.append("	    cus.cus_org,                                                  ");
		sql.append("	    prj.prj_name,                                                   ");
		sql.append("	    dc.dc_name                                                   ");
		sql.append("	    ,pool.pay_type                                                      ");
		sql.append("	    ,pool.end_time                                                      ");
		sql.append("        ,pool.charge_state                                                  ");
		sql.append("        ,pool.create_time                                                   ");
		sql.append("        ,pool.mode                                                  ");
		sql.append("	FROM                                                                    ");
		sql.append("		cloud_ldpool pool                                                   ");
		sql.append("	LEFT JOIN cloud_ldvip vip ON vip.pool_id = pool.pool_id                 ");
		sql.append("	LEFT JOIN dc_datacenter dc on dc.id=pool.dc_id                 ");
		sql.append("   LEFT JOIN cloud_project prj on pool.prj_id = prj.prj_id 						");
		sql.append("   LEFT JOIN sys_selfcustomer cus on prj.customer_id = cus.cus_id ");
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
		sql.append("	AND pool.is_visible = '1'                                               ");
		// 数据中心
		if (StringUtils.isNotBlank(dcId)) {
			sql.append(" and pool.dc_id = ?");
			args[index++] = dcId;
		}
		//项目名称
		if (StringUtils.isNotBlank(prjName)) {
			List tmpList = Arrays.asList(StringUtils.split(prjName,","));
            String inStr = prepareInCondition(tmpList);
            sql.append(" AND prj.prj_name in("+inStr+")");
		}
		//客户组织名称
		if (StringUtils.isNotBlank(cusOrg)) {
			List tmpList = Arrays.asList(StringUtils.split(cusOrg,","));
            String inStr = prepareInCondition(tmpList);
            sql.append(" AND cus.cus_org in("+inStr+")");
		}
		if(!org.apache.commons.lang3.StringUtils.isEmpty(poolName)){
			sql.append(" AND binary pool.pool_name like ?                                          ");
			poolName = poolName.replaceAll("\\_", "\\\\_");
			args[index++] = "%" + poolName + "%";
		}
		sql.append("order by pool.dc_id , pool.prj_id ,pool.create_time desc ");

		Object[] params = new Object[index];
		System.arraycopy(args, 0, params, 0, index);
		Page page = cloudLdpoolDao.pagedNativeQuery(sql.toString(), queryMap, params);
		List newList = (List) page.getResult();

		for (int i = 0; i < newList.size(); i++) {
			Object[] objs = (Object[]) newList.get(i);
			int resIndex = 0;
			CloudLdPool pool = new CloudLdPool();
			pool.setDcId((String)objs[resIndex++]);
			pool.setPrjId((String)objs[resIndex++]);
			pool.setPoolId((String)objs[resIndex++]);
			pool.setPoolName((String)objs[resIndex++]);
			pool.setPoolStatus((String)objs[resIndex++]);
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
			pool.setStatusForPool(DictUtil.getStatusByNodeEn("ldPool", pool.getPoolStatus()));
			pool.setCusOrg((String)objs[resIndex++]);
			pool.setPrjName((String)objs[resIndex++]);
			pool.setDcName((String)objs[resIndex++]);
			/* 用户中心改版计费相关 */
			pool.setPayType(String.valueOf(objs[resIndex++]));
			pool.setEndTime((Date)objs[resIndex++]);
			pool.setChargeState(String.valueOf(objs[resIndex++]));
			pool.setCreateTime((Date)objs[resIndex++]);
			pool.setMode((String.valueOf(objs[resIndex++])));
			pool.setPayTypeStr(CloudResourceUtil.escapePayType(pool.getPayType()));
			pool.setMonitorStatus("未开启");
			if(pool.isCheckMonitor()){
				pool.setMonitorStatus("已开启");
			}
			if (CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(pool.getChargeState())) {
				pool.setStatusForPool(DictUtil.getStatusByNodeEn("ldPool", pool.getPoolStatus()));
            } else {
            	pool.setStatusForPool(CloudResourceUtil.escapseChargeState(pool.getChargeState()));
            }
			newList.set(i, pool);
		}
		return page;
	}

    private String prepareInCondition(List tmpList) {
        StringBuffer sb = new StringBuffer();
        for(int i=0;i <tmpList.size(); i++){
            String str = "'"+tmpList.get(i)+"'";
            sb.append(str).append(",");
        }
        return sb.substring(0, sb.length()-1);
    }

    @Override
	public CloudLdPool createBalancer(CloudLdPool pool) throws Exception {
		pool.setPoolProvider("haproxy");
		if (checkPrjQuota(pool.getPrjId())) {
			throw new AppException("error.openstack.message", new String[] { "负载均衡数量超过项目规定限额，请先申请配额！" });
		}
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		BaseEcmcSysUser user = EcmcSessionUtil.getUser();
		try {
			temp.put("name", pool.getPoolName());
			temp.put("protocol", pool.getPoolProtocol());
			temp.put("subnet_id", pool.getSubnetId());
			temp.put("lb_method", pool.getLbMethod());
			temp.put("admin_state_up", "1");
			data.put("pool", temp);
			Pool result = openstackPoolService.create(pool.getDcId(), pool.getPrjId(), data);

			if (result != null) {
				BaseCloudLdPool cloudLdpool = new BaseCloudLdPool();
				cloudLdpool.setPoolId(result.getId());
				cloudLdpool.setPoolName(result.getName());
				cloudLdpool.setPrjId(pool.getPrjId());
				cloudLdpool.setDcId(pool.getDcId());
				cloudLdpool.setPoolProvider(result.getProvider());
				cloudLdpool.setSubnetId(result.getSubnet_id());
				cloudLdpool.setPoolProtocol(result.getProtocol());
				cloudLdpool.setLbMethod(result.getLb_method());
				cloudLdpool.setPoolStatus(result.getStatus().toUpperCase());
				cloudLdpool.setAdminStateup(BoolUtil.bool2char(result.isAdmin_state_up()));
				cloudLdpool.setCreateName(user.getAccount());
				cloudLdpool.setCreateTime(new Date());

				cloudLdpoolDao.saveOrUpdate(cloudLdpool);

				if (null != result.getStatus() && !"ACTIVE".equals(result.getStatus())) {
					JSONObject json = new JSONObject();
					json.put("poolId", cloudLdpool.getPoolId());
					json.put("dcId", cloudLdpool.getDcId());
					json.put("prjId", cloudLdpool.getPrjId());
					json.put("poolStatus", cloudLdpool.getPoolStatus());
					json.put("count", "0");
					jedisUtil.push(RedisKey.ldPoolKey, json.toJSONString());
				}

				CloudLdVip vip = new CloudLdVip();
				vip.setDcId(pool.getDcId());
				vip.setPrjId(pool.getPrjId());
				vip.setVipName("vip_" + System.currentTimeMillis());
				vip.setConnectionLimit(pool.getConnectionLimit());
				vip.setVipProtocol(pool.getPoolProtocol());
				vip.setCreateName(user.getAccount());
				vip.setCreateTime(new Date());
				vip.setSubnetId(pool.getSubnetId());
				vip.setPoolId(cloudLdpool.getPoolId());
				vip.setProtocolPort(pool.getVipPort());

				vipService.addVip(vip);

				BeanUtils.copyPropertiesByModel(pool, cloudLdpool);
			}

		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
		    log.error(e.toString(),e);
			throw new AppException("error.openstack.message");
		}
		return pool;
	}

	@Override
	public CloudLdPool updateBalancer(CloudLdPool pool) throws Exception {
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("name", pool.getPoolName());
		data.put("pool", temp);
		Pool result = openstackPoolService.update(pool.getDcId(), pool.getPrjId(), data, pool.getPoolId());
		CloudLdPool cloudLdpoolVoe = null;
		if (result != null) {
			BaseCloudLdPool cloudLdpool = cloudLdpoolDao.findOne(pool.getPoolId());
			cloudLdpool.setPoolName(result.getName());
			cloudLdpool.setAdminStateup(BoolUtil.bool2char(result.isAdmin_state_up()));
			cloudLdpool.setPoolStatus(result.getStatus());
			cloudLdpoolDao.saveOrUpdate(cloudLdpool);
			cloudLdpoolVoe = new CloudLdPool();
			BeanUtils.copyPropertiesByModel(cloudLdpoolVoe, cloudLdpool);

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
		if(!org.apache.commons.lang3.StringUtils.isEmpty(pool.getVipId())){
			CloudLdVip vip = new CloudLdVip();
			vip.setVipId(pool.getVipId());
			vip.setDcId(pool.getDcId());
			vip.setPrjId(pool.getPrjId());
			vip.setConnectionLimit(pool.getConnectionLimit());
			vipService.modifyVip(vip);
		}

		return cloudLdpoolVoe;
	}

	@Override
	public boolean deleteBalancer(CloudLdPool pool) throws AppException {
		
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
		if(!org.apache.commons.lang3.StringUtils.isEmpty(pool.getVipId())){
			CloudLdVip cloudLdVip = new CloudLdVip();
			cloudLdVip.setDcId(pool.getDcId());
			cloudLdVip.setPrjId(pool.getPrjId());
			cloudLdVip.setVipId(pool.getVipId());

			vipFlag = vipService.deleteVip(cloudLdVip);

		}
		if(vipFlag|| org.apache.commons.lang3.StringUtils.isEmpty(pool.getVipId())){
			boolean poolFlag = openstackPoolService.delete(pool.getDcId(), pool.getPrjId(), pool.getPoolId());
			if (poolFlag) {
				ecmcAlarmService.clearPoolMsgAfterDeletePool(pool.getPoolId());
				cloudLdpoolDao.delete(pool.getPoolId());

				deleteMemberAndMonitor(pool);

				// 删除资源后更新缓存接口
				tagService.refreshCacheAftDelRes("ldPool", pool.getPoolId());
				ecmcAlarmService.deleteMonitorByResource(MonitorResourceType.POOL.toString(), pool.getPoolId());
				String cusId = this.getCusIdBuyPrjId(pool.getPrjId());
				pool.setCusOrg(cusId);
				this.poolDeleteHandle(pool);
				flag = true;

			}
		}
		return flag;
	}

	/**
	 * 删除后的业务处理--发送消息
	 * @author gaoxiang
	 * @param pool
	 * @param deleteStep
	 */
	private void poolDeleteHandle(CloudLdPool pool){
        if(PayType.PAYAFTER.equals(pool.getPayType())) {
            ChargeRecord chargeRecord = new ChargeRecord();
            chargeRecord.setDatecenterId(pool.getDcId());
            chargeRecord.setResourceId(pool.getPoolId());
            chargeRecord.setResourceName(pool.getPoolName());
            chargeRecord.setCusId(pool.getCusOrg());
            chargeRecord.setResourceType(ResourceType.QUOTAPOOL);
            chargeRecord.setOpTime(new Date());
            rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_DELETE, JSONObject.toJSONString(chargeRecord));
        }
	}
    @Override
    public CloudLdPool getLoadBalanceById(String poolId) throws AppException {
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
        sql.append("		CASE                                                                ");
        sql.append("	WHEN ldpm.monitorCount > 0 THEN                                         ");
        sql.append("		'true'                                                              ");
        sql.append("	ELSE                                                                    ");
        sql.append("		'false'                                                             ");
        sql.append("	END AS isCheckMonitor,                                                   ");
        sql.append("	    cus.cus_org                                                         ");
        sql.append("		,pool.charge_state                                                  ");
        sql.append("		,pool.create_time                                                   ");
        sql.append("		,pool.end_time                                                      ");
        sql.append("		,pool.pay_type                                                      ");
        sql.append("		,pool.mode                                                      ");
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
        sql.append("	LEFT JOIN sys_selfcustomer cus ON cus.cus_id = cp.customer_id          ");
        sql.append("	WHERE pool.pool_id = ?                                                  ");

        Query query = cloudLdpoolDao.createSQLNativeQuery(sql.toString(), new Object[] {poolId});
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
            pool.setCheckMonitor(Boolean.parseBoolean(String.valueOf(obj[index++])));
            pool.setCusOrg((String)obj[index++]);
            pool.setChargeState(String.valueOf(obj[index++]));
            pool.setCreateTime((Date)obj[index++]);
            pool.setEndTime((Date)obj[index++]);
            pool.setPayType(String.valueOf(obj[index++]));
            pool.setMode(String.valueOf(obj[index++]));
            pool.setStatusForPool(DictUtil.getStatusByNodeEn("ldPool", pool.getPoolStatus()));
            pool.setLbMethodCn(DictUtil.getStatusByNodeEn("ldType", pool.getLbMethod()));
            pool.setMonitorStatus("未开启");
            pool.setPayTypeStr(CloudResourceUtil.escapePayType(pool.getPayType()));
            if(pool.isCheckMonitor()){
                pool.setMonitorStatus("已开启");
            }
            if (CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(pool.getChargeState())) {
				pool.setStatusForPool(DictUtil.getStatusByNodeEn("ldPool", pool.getPoolStatus()));
            } else {
            	pool.setStatusForPool(CloudResourceUtil.escapseChargeState(pool.getChargeState()));
            }
        }
        return pool;
    }

    /**
	 * 级联删除负载均衡器成员和健康检查
	 *
	 * @author zhouhaitao
	 * @param pool
	 * @return
	 */
	private boolean deleteMemberAndMonitor(CloudLdPool pool){
		boolean flag = false;
		try{
			StringBuffer deleteMember = new StringBuffer();
			StringBuffer deleteMonitor = new StringBuffer();

			deleteMember.append("delete BaseCloudLdMember where poolId = ?");
			deleteMonitor.append("delete BaseCloudLdPoolMonitor where poolId = ?");

			cloudLdmemberDao.executeUpdate(deleteMember.toString(), pool.getPoolId());
			cloudLdmemberDao.executeUpdate(deleteMonitor.toString(), pool.getPoolId());
			flag =true;
		}catch(Exception e){
			flag = false;
			log.error(e.toString(),e);
			throw e;
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

		List floatList = cloudLdmemberDao.find(hql.toString(), new Object[]{"lb","0",pool.getPoolId()});

		return null!=floatList&&floatList.size()>0 ;
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

		Query query = cloudLdpoolDao.createSQLNativeQuery(sql.toString(), new Object[] { prjId, prjId });
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
	  * 根据项目ID获取客户ID
	  * @param prjId
	  * @return
	  */
	private String getCusIdBuyPrjId(String prjId){
		StringBuffer hql = new StringBuffer();
       hql.append("select ");
       hql.append("   customer_id ");
       hql.append("from ");
       hql.append("   cloud_project ");
       hql.append("where ");
       hql.append("   prj_id = ?");
       Query query = cloudLdpoolDao.createSQLNativeQuery(hql.toString(), prjId);
       Object result = query.getSingleResult();
       String cusId = result == null ? "" : String.valueOf(result.toString());
		return cusId;
	}

	@Override
	public List<CloudLdPool> getAllPoolList() throws Exception {
		List<BaseCloudLdPool> list=cloudLdpoolDao.find("from BaseCloudLdPool where isVisible = ?", "1");
		List<CloudLdPool> resultList=new ArrayList<CloudLdPool>();
		for (BaseCloudLdPool baseCloudLdPool : list) {
			CloudLdPool cloudLdPool=new CloudLdPool();
			BeanUtils.copyPropertiesByModel(cloudLdPool, baseCloudLdPool);
			resultList.add(cloudLdPool);
		}
		return resultList;
	}
	public void deletePool(CloudLdPool cloudLdPool) throws Exception {
		cloudLdpoolDao.delete(cloudLdPool.getPoolId());
		//如果被删的负载均衡在上层有绑定的公网ip,则把公网ip上层进行解绑
		List<BaseCloudFloatIp> floatIp=cloudFloatIpDao.find("from BaseCloudFloatIp where resourceId=? and resourceType=? and status=?", new Object[]{cloudLdPool.getPoolId(),"lb","0"});
		if(floatIp!=null&&floatIp.size()>0){
			for (BaseCloudFloatIp baseCloudFloatIp : floatIp) {
				baseCloudFloatIp.setResourceId(null);
				baseCloudFloatIp.setResourceType(null);
				baseCloudFloatIp.setFloStatus("1");
				cloudFloatIpDao.saveOrUpdate(baseCloudFloatIp);
			}
		}
		ecmcLogService.addLog("同步资源清除数据", toType(cloudLdPool), cloudLdPool.getPoolName(), cloudLdPool.getPrjId(), 1, cloudLdPool.getPoolId(), null);
		JSONObject json = new JSONObject();
		json.put("resourceType", ResourceSyncConstant.LDPOOL);
		json.put("resourceId", cloudLdPool.getPoolId());
		json.put("resourceName", cloudLdPool.getPoolName());
		json.put("synTime", new Date());
		jedisUtil.push(RedisKey.MEMBER_STAUS_SYNC_DELETED_RESOURCE, json.toJSONString());
		ecmcAlarmService.deleteMonitorByResource(MonitorResourceType.POOL.toString(), cloudLdPool.getPoolId());
		ecmcAlarmService.clearPoolMsgAfterDeletePool(cloudLdPool.getPoolId());
	}
	/**
	 * 拼装同步删除发送日志的资源类型
	 * @param pool
	 * @return
	 */
	private String toType(BaseCloudLdPool pool) {
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuffer resourceType = new StringBuffer();
        resourceType.append(ResourceSyncConstant.LDPOOL);
        resourceType.append("-").append(CloudResourceUtil.escapePayType(pool.getPayType()));
        if(null != pool && null != pool.getCreateTime()){
        	resourceType.append(ResourceSyncConstant.SEPARATOR).append("创建时间：").append(sdf.format(pool.getCreateTime()));
        }
        if (PayType.PAYBEFORE.equals(pool.getPayType())) {
            resourceType.append(ResourceSyncConstant.SEPARATOR).append("到期时间：").append(sdf.format(pool.getEndTime()));
        }
        return resourceType.toString();
	}
}
