package com.eayun.virtualization.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.constant.OrderStateType;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.database.instance.service.RDSInstanceService;
import com.eayun.order.model.BaseOrder;
import com.eayun.order.service.OrderService;
import com.eayun.project.service.ProjectService;
import com.eayun.virtualization.bean.AboutToExpire;
import com.eayun.virtualization.dao.CloudVmDao;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.service.CloudFloatIpService;
import com.eayun.virtualization.service.NetWorkService;
import com.eayun.virtualization.service.OverviewService;
import com.eayun.virtualization.service.PoolService;
import com.eayun.virtualization.service.PortMappingService;
import com.eayun.virtualization.service.RouteService;
import com.eayun.virtualization.service.SecurityGroupService;
import com.eayun.virtualization.service.SnapshotService;
import com.eayun.virtualization.service.SubNetWorkService;
import com.eayun.virtualization.service.VmService;
import com.eayun.virtualization.service.VolumeService;
import com.eayun.virtualization.service.VpnService;
@Service
@Transactional
public class OverviewServiceImpl implements OverviewService {
	
	private static final Logger log = LoggerFactory.getLogger(OverviewServiceImpl.class);
	
	@Autowired
    private CloudVmDao cloudVmDao;//云主机
    @Autowired
    private ProjectService projectService;
    @Autowired
    private VmService vmService;//云主机
    @Autowired
    private VolumeService volumeService;//云硬盘 
    @Autowired
    private SnapshotService snapshotService;//备份 
    @Autowired
    private NetWorkService netWorkService;//云硬盘 
    @Autowired
    private SubNetWorkService subNetWorkService;//云硬盘 
    @Autowired
    private RouteService routeService;//路由 
    @Autowired
    private SecurityGroupService securityGroupService;//安全组 
    @Autowired
    private PoolService poolService;//资源池(负载均衡使用量说的是它)
    @Autowired
    private CloudFloatIpService cloudFloatIpService;//资源池(负载均衡使用量说的是它)
    
    @Autowired
    private PortMappingService portMappingService;//资源池(负载均衡使用量说的是它)
    @Autowired
    private VpnService vpnService;//资源池(负载均衡使用量说的是它)
    @Autowired
    private JedisUtil jedisUtil;
    
    @Autowired
	private OrderService orderService;
    
    @Autowired
    private RDSInstanceService rdsInstanceService;
    
    @Override
    public CloudProject findStatisticsByPrjId(String prjId) {
    	CloudProject cloudPrj= new CloudProject();
    	if(StringUtil.isEmpty(prjId) || "null".equals(prjId)){
    		return cloudPrj;
    	}
        //云主机返回包括(云主机，cpu，内存，硬盘和备份大小的使用量)
        BaseCloudProject basePrj = projectService.findProject(prjId);
        BeanUtils.copyPropertiesByModel(cloudPrj, basePrj);
        CloudProject cloudProject = vmService.queryPrjQuato(prjId);
        CloudProject diskSnapProject = snapshotService.queryProjectQuotaAndUsed(prjId);
        
        int netWorkUse=netWorkService.findNetWorkCountByPrjId(prjId);
        int subNetWorkUse = subNetWorkService.findSubNetCountByPrjId(prjId);
        int routeCount =routeService.getCountByPrjId(prjId);
        int sgCount =securityGroupService.getCountByPrjId(prjId);
        int bandCount = routeService.getQosNumByPrjId(prjId);
        int quotaBalance = poolService.getCountByPrjId(prjId);
        int portCount = portMappingService.getCountByPrjId(prjId);
        int vpnCount = vpnService.getCountByPrjId(prjId);
        int masterInstanceUsed = rdsInstanceService.getMasterCountByPrjId(prjId);
        int slaveInstanceUsed = rdsInstanceService.getSlaveCountByPrjId(prjId);
        
        
        cloudPrj.setUsedVmCount(cloudProject.getUsedVmCount());				//主机使用量
        cloudPrj.setUsedCpuCount(cloudProject.getUsedCpuCount());			//CPU使用量
        cloudPrj.setUsedRam(cloudProject.getUsedRam());						//内存使用量，转换单位成GB
        
        cloudPrj.setDiskCountUse(cloudProject.getDiskCountUse());			//云硬盘块数使用量（个）
        cloudPrj.setUsedDiskCapacity(cloudProject.getUsedDiskCapacity());	//云硬盘使用量（GB）
        cloudPrj.setDiskSnapshotUse(diskSnapProject.getDiskSnapshotUse());				//云硬盘备份块数使用量（个）
        cloudPrj.setUsedSnapshotCapacity(diskSnapProject.getUsedSnapshotCapacity());	//云硬盘备份使用量（GB）
        
        cloudPrj.setRouteCountUse(routeCount);			//路由
        cloudPrj.setSafeGroupUse(sgCount);				//安全组	(新加订单功能不影响，因此使用原方法不变)
        cloudPrj.setNetWorkUse(netWorkUse);				//网络使用量（个）
        cloudPrj.setSubnetCountUse(subNetWorkUse);		//子网使用量（个）	(新加订单功能不影响，因此使用原方法不变)
        cloudPrj.setOuterIPUse(cloudProject.getOuterIPUse());	//公网ip使用量（个）
        cloudPrj.setCountBandUse(bandCount);			//带宽使用量（Mbps）
        cloudPrj.setUsedPool(quotaBalance);				//资源池（负载均衡）使用量（个）
        
        cloudPrj.setCountVpnUse(vpnCount);						//VPN使用量
        cloudPrj.setPortMappingUse(portCount);					//端口映射使用量
        
        int smsQuota=0;
		try {
			String smsQuo = jedisUtil.get(RedisKey.SMS_QUOTA_SENT+cloudPrj.getCustomerId()+":"+cloudPrj.getProjectId());
			smsQuota = smsQuo!=null?Integer.valueOf(smsQuo):0;
		} catch (NumberFormatException e) {
			log.error(e.toString(),e);
		} catch (Exception e) {
			log.error(e.toString(),e);
		}
        cloudPrj.setSmsQuota(smsQuota);//短信配额使用量
        
        cloudPrj.setMasterInstanceUse(masterInstanceUsed);//主实例使用数
        cloudPrj.setTotalInstanceUse(masterInstanceUsed + slaveInstanceUsed);//总RDS实例使用量
        return cloudPrj;
    }

	@Override
	public List<CloudProject> getValidDcList(SessionUserInfo sessionUser) {
		log.info("获取该登录客户已创建有项目，且登录用户有权限的数据中心列表");
		List<CloudProject> prjList = new ArrayList<CloudProject>();
		List<Object> list = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT   ");
		sql.append(" cp.prj_id as prjId,  ");
		sql.append(" cp.prj_name as prjName,  ");
		sql.append(" cp.dc_id as dcId,  ");
		sql.append(" dc.dc_name as dcName  ");
		sql.append(" from  ");
		if(sessionUser.getIsAdmin()){
			sql.append(" cloud_project ");
		}
		else{
			sql.append(" ( ");
			sql.append(" SELECT  ");
			sql.append("  	s.project_id as prj_id,");
			sql.append("  	p.prj_name as prj_name,");
			sql.append("  	p.dc_id as dc_id ,");
			sql.append("  	p.customer_id as customer_id ");
			sql.append(" from sys_selfuserprj s ");
			sql.append(" left join cloud_project p ");
			sql.append(" on s.project_id=p.prj_id ");
			sql.append(" where s.user_id = ? ");
			sql.append(" )");
			list.add(sessionUser.getUserId());
		}
		sql.append(" cp");
		sql.append(" join dc_datacenter dc on cp.dc_id = dc.id");
		sql.append(" where cp.customer_id = ? ");
		list.add(sessionUser.getCusId());
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), list.toArray());
		@SuppressWarnings("rawtypes")
		List resultList = query.getResultList();
		for (int i = 0; i < resultList.size(); i++) {
            Object[] obj = (Object[]) resultList.get(i);
            CloudProject prj = new CloudProject();
            prj.setProjectId(String.valueOf(obj[0]));
            prj.setPrjName(String.valueOf(obj[1]));
            prj.setDcId(String.valueOf(obj[2]));
            prj.setDcName(String.valueOf(obj[3]));
            prjList.add(prj);
		}
		return prjList;
	}

	/**
	 * 获取客户即将到期的资源列表，5条一页
	 * @param page
	 * @param queryMap
	 * @param cusId	客户id
	 * @param prjId	项目id
	 * @return
	 */
	@Override
	public Page getToExpireResources(Page page, QueryMap queryMap,
			String cusId, String prjId) {
		Date now = new Date();
		Date threeDay = DateUtil.addDay(now, new int[]{0,0,3});
		List<Object> paramList = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" vm.vm_id AS resources_id, vm.vm_name AS resources_name, vm.create_time AS create_time,");
		sql.append(" vm.end_time AS end_time, '云主机' AS resources_type, vm.prj_id AS prj_id ");
		sql.append(" FROM cloud_vm vm ");
		sql.append(" WHERE vm.pay_type = '1' AND vm.is_deleted = '0' AND vm.charge_state = '0' ");
		sql.append(" AND vm.end_time < ? AND vm.end_time >? AND vm.prj_id = ? ");
		paramList.add(threeDay);
		paramList.add(now);
		paramList.add(prjId);
		sql.append(" UNION ");
		sql.append(" SELECT ");
		sql.append(" vol.vol_id AS resources_id, vol.vol_name AS resources_name, vol.create_time AS create_time, ");
		sql.append(" vol.end_time AS end_time, '云硬盘' AS resources_type, vol.prj_id AS prj_id ");
		sql.append(" FROM cloud_volume vol ");
		sql.append(" WHERE vol.pay_type = '1' AND vol.is_deleted = '0' AND vol.charge_state = '0' ");
		sql.append(" AND vol.end_time < ? AND vol.end_time >? AND vol.prj_id = ? ");
		paramList.add(threeDay);
		paramList.add(now);
		paramList.add(prjId);
		sql.append(" UNION ");
		sql.append(" SELECT ");
		sql.append(" net.net_id AS resources_id, net.net_name AS resources_name, net.create_time AS create_time, ");
		sql.append(" net.end_time AS end_time, '私有网络' AS resources_type, net.prj_id AS prj_id ");
		sql.append(" FROM cloud_network net ");
		sql.append(" WHERE net.pay_type = '1' AND net.charge_state = '0' ");
		sql.append(" AND net.end_time < ? AND net.end_time >? AND net.prj_id = ? ");
		paramList.add(threeDay);
		paramList.add(now);
		paramList.add(prjId);
		sql.append(" UNION ");
		sql.append(" SELECT ");
		sql.append(" pool.pool_id AS resources_id, pool.pool_name AS resources_name, pool.create_time AS create_time, ");
		sql.append(" pool.end_time AS end_time, '负载均衡' AS resources_type, pool.prj_id AS prj_id");
		sql.append(" FROM cloud_ldpool pool ");
		sql.append(" WHERE pool.pay_type = '1' AND pool.charge_state = '0' ");
		sql.append(" AND pool.end_time < ? AND pool.end_time >? AND pool.prj_id = ? ");
		paramList.add(threeDay);
		paramList.add(now);
		paramList.add(prjId);
		sql.append(" UNION ");
		sql.append(" SELECT ");
		sql.append(" flo.flo_id AS resources_id, flo.flo_ip AS resources_name, flo.create_time AS create_time, ");
		sql.append(" flo.end_time AS end_time, '弹性公网IP' AS resources_type, flo.prj_id AS prj_id ");
		sql.append(" FROM cloud_floatip flo ");
		sql.append(" WHERE flo.pay_type = '1' AND flo.is_deleted = '0' AND flo.charge_state = '0' ");
		sql.append(" AND flo.end_time < ? AND flo.end_time >? AND flo.prj_id = ? ");
		paramList.add(threeDay);
		paramList.add(now);
		paramList.add(prjId);
		sql.append(" UNION ");
		sql.append(" SELECT ");
		sql.append(" vpn.vpn_id AS resources_id, vpn.vpn_name AS resources_name, vpn.create_time AS create_time, ");
		sql.append(" vpn.end_time AS end_time, 'VPN' AS resources_type, ser.prj_id AS prj_id ");
		sql.append(" FROM cloud_vpnconn vpn ");
		sql.append(" JOIN cloud_vpnservice ser ON vpn.vpnservice_id = ser.vpnservice_id ");
		sql.append(" WHERE vpn.pay_type = '1' AND vpn.charge_state = '0' ");
		sql.append(" AND vpn.end_time < ? AND vpn.end_time >? AND ser.prj_id = ? ");
		paramList.add(threeDay);
		paramList.add(now);
		paramList.add(prjId);
		sql.append(" ORDER BY end_time ");
		page = cloudVmDao.pagedNativeQuery(sql.toString(),queryMap,paramList.toArray());
		List resultList = (List)page.getResult();
		for (int i = 0; i < resultList.size(); i++) {
			 Object[] obj = (Object[]) resultList.get(i);
			 AboutToExpire toExpire = new AboutToExpire();
			 toExpire.setResourcesId(String.valueOf(obj[0]));;
			 toExpire.setResourcesName(String.valueOf(obj[1]));
			 toExpire.setCreateTime((Date) obj[2]);
			 toExpire.setEndTime((Date) obj[3]);
			 toExpire.setResourcesType(String.valueOf(obj[4]));
			 toExpire.setPrjId(String.valueOf(obj[5]));
			 
			 resultList.set(i, toExpire);
		 }
		return page;
	}

	@Override
	public Page getToPayOrderPage(Page page, QueryMap queryMap, String cusId) {
		BaseOrder order = new BaseOrder();
		order.setCusId(cusId);
		order.setOrderState(OrderStateType.TO_BE_PAID);
		return orderService.getOrderList(queryMap, null, null, order);
	}

}
