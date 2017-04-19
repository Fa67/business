package com.eayun.virtualization.ecmcservice.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.charge.model.ChargeRecord;
import com.eayun.common.constant.EayunQueueConstant;
import com.eayun.common.constant.PayType;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.util.BeanUtils;
import com.eayun.eayunstack.model.FloatIp;
import com.eayun.eayunstack.service.OpenstackFloatIpService;
import com.eayun.unit.model.BaseWebDataCenterIp;
import com.eayun.unit.service.EcscRecordService;
import com.eayun.virtualization.baseservice.BaseFloatIpService;
import com.eayun.virtualization.dao.CloudFloatIpDao;
import com.eayun.virtualization.dao.CloudLdVipDao;
import com.eayun.virtualization.dao.CloudNetWorkDao;
import com.eayun.virtualization.ecmcservice.EcmcCloudFloatIPService;
import com.eayun.virtualization.model.BaseCloudFloatIp;
import com.eayun.virtualization.model.BaseCloudNetwork;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.BaseCloudVm;
import com.eayun.virtualization.model.CloudFloatIp;
import com.eayun.virtualization.service.TagService;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年4月6日
 */
@Service
@Transactional
public class EcmcCloudFloatIPServiceImpl extends BaseFloatIpService implements EcmcCloudFloatIPService {
    private static final Logger log = LoggerFactory.getLogger(EcmcCloudFloatIPServiceImpl.class);
	@Autowired
	private OpenstackFloatIpService service;
	@Autowired
	private CloudFloatIpDao floatipdao;
	@Autowired
	private CloudNetWorkDao networkdao;
	@Autowired
	private CloudLdVipDao ldvipdao;
	@Autowired
	private TagService tagservice;
	@Autowired
	private EayunRabbitTemplate rabbitTemplate;
	
	@Autowired
	private EcscRecordService ecscRecordService;
	
	@Override
	public List<Map<String, Object>> getIPList(String floIpmin, String floIpmax) throws AppException {
		return floatipdao.findSubNetWorkByNetIdAndGatewayIp(floIpmin, floIpmax);
	}
	
	@Override
	public List<BaseCloudVm> findFloatIpOne(String vmId) throws AppException {
		return floatipdao.findFloatIpOne(vmId);
	}

	@Override
	public Page getFloatlist(String name, String datacenterId, String projectId, Page page,QueryMap queryMap,String ip,String[] pns,String [] cns)
			throws AppException {
		List<Object> listps = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("select ft.dc_id as dcId , ft.flo_id as floId,ft.resource_id as resourceId,ft.prj_id as prjId,ft.flo_ip as floIp,cus.cus_org as cusName,CASE WHEN ft.resource_type = 'vm' THEN vm.vm_name ELSE ld.pool_name END as resName,"
				+ "cn.net_name as netName,cp.prj_name as prjName,ft.resource_type as resourceType,dc.dc_name as dcName,CASE WHEN ft.resource_type = 'vm' THEN vm.vm_ip ELSE vip.vip_address END as resIp ");
		sql.append(",ft.charge_state as chargeState,ft.pay_type as payType ");
		sql.append(" ,CASE WHEN ft.resource_type = 'vm' THEN concat('云主机 ：', vm.vm_name) ");
		sql.append("       WHEN ft.resource_type = 'lb' THEN concat('负载均衡 ：', ld.pool_name)");
		sql.append(" ELSE '' END as resourceName ");
		sql.append(" ,ft.create_time as createTime ");
		sql.append(" ,ft.end_time as endTime ");
		sql.append(" from cloud_floatip ft ");
		sql.append("left join cloud_vm vm on ft.resource_id = vm.vm_id ");
		sql.append("left join cloud_network cn on ft.net_id = cn.net_id ");
		sql.append("left join cloud_project cp on ft.prj_id = cp.prj_id ");
		sql.append("left join dc_datacenter dc on ft.dc_id = dc.id ");
		sql.append("left join cloud_ldpool ld on ft.resource_id = ld.pool_id ");
		sql.append("left join cloud_ldvip vip on ld.pool_id = vip.pool_id ");
		sql.append("left join sys_selfcustomer cus on cus.cus_id =cp.customer_id ");
		sql.append("where ft.is_deleted = '0' and ft.is_visable='1' ");
		//根据客户名称 多个查询
		if (cns!=null&&cns.length>0) {
			sql.append(" and ( ");
			for (String cn : cns) {
				sql.append(" binary cus.cus_org= ? or ");
				listps.add(cn);
            }
			sql.append(" 1 = 2 ) ");
		}
		//根据项目名称 多个查询
		if (pns!=null&&pns.length>0) {
			sql.append(" and ( ");
			for (String prj : pns) {
				sql.append(" binary cp.prj_name= ? or ");
				listps.add(prj);
            }
			sql.append(" 1 = 2 ) ");
		}
		//数据中心
		if (!"".equals(datacenterId)&&datacenterId!=null&&!"undefined".equals(datacenterId)&&!"null".equals(datacenterId)) {
			sql.append("and ft.dc_id = ? ");
			listps.add(datacenterId);
		}
		//项目id为空时，查询所有项目的资源列表
		if (!"".equals(projectId)&&projectId!=null&&!"undefined".equals(projectId)&&!"null".equals(projectId)) {
			sql.append("and ft.prj_id = ? ");
			listps.add(projectId);
		}
		
		//云主机名称
		if (!"".equals(name)&&name!=null&&!"undefined".equals(name)&&!"null".equals(name)) {
			sql.append("and vm.vm_name like ? ");
			name = name.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
			listps.add("%"+name+"%");
		}
		//IP地址
		if (!"".equals(ip)&&ip!=null&&!"undefined".equals(ip)&&!"null".equals(ip)) {
			sql.append("and ft.flo_ip like ? ");
			ip = ip.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
			listps.add("%"+ip+"%");
		}
		sql.append("order by ft.dc_id , ft.prj_id ,ft.create_time desc");
		page = floatipdao.pagedNativeQuery(sql.toString(), queryMap, listps.toArray());
		CloudFloatIp floatip = null;
		Object[] objs = null;
		List newlist = (List) page.getResult();
        int a = newlist.size();
        for (int i = 0; i < newlist.size(); i++) {
        	objs = (Object[]) newlist.get(i);
        	floatip = new CloudFloatIp();
        	floatip.setDcId(ObjectUtils.toString(objs[0]));
        	floatip.setFloId(ObjectUtils.toString(objs[1]));
        	floatip.setResourceId(String.valueOf(objs[2]));
        	floatip.setPrjId(ObjectUtils.toString(objs[3]));
        	floatip.setFloIp(ObjectUtils.toString(objs[4]));
        	floatip.setCusName(ObjectUtils.toString(objs[5]));
        	floatip.setVmName(ObjectUtils.toString(objs[6]));
        	floatip.setNetName(ObjectUtils.toString(objs[7]));
        	floatip.setPrjName(ObjectUtils.toString(objs[8]));
        	floatip.setResourceType(ObjectUtils.toString(objs[9]));
        	floatip.setDcName(ObjectUtils.toString(objs[10]));
        	floatip.setVmIp(ObjectUtils.toString(objs[11]));
        	floatip.setChargeState(ObjectUtils.toString(objs[12]));
        	floatip.setPayType(ObjectUtils.toString(objs[13]));
        	floatip.setResourceName(ObjectUtils.toString(objs[14]));
        	floatip.setCreateTime((Date)objs[15]);
        	floatip.setEndTime((Date)objs[16]);
        	String chargeName = "";
        	if ("0".equals(floatip.getChargeState())) {
                if (floatip.getResourceId() != null && !"null".equals(floatip.getResourceId())) {
                    chargeName = "已使用";
                } else {
                    chargeName = "未使用";
                }
            } else if ("1".equals(floatip.getChargeState())) {
                chargeName = "余额不足";
            } else if ("2".equals(floatip.getChargeState())) {
                chargeName = "已到期";
            }
            floatip.setChargeStateName(chargeName);
        	newlist.set(i, floatip);
        }
		return page;
	}
	
	@Override
	public BaseCloudFloatIp getById(String id) throws AppException {
		return floatipdao.findOne(id);
	}
	
	@Override
	public CloudFloatIp allocateIp(String datacenterId, String projectId)
			throws AppException {
		CloudFloatIp cloudFloatipVoe= null;
		BaseCloudFloatIp cloudFloatip= null;
		List<BaseCloudNetwork> list = networkdao.find(" from BaseCloudNetwork where dcId = ? and routerExternal = '1' ", datacenterId);
		String pool = "";
		if(list!=null && list.size()>0){
			pool = list.get(0).getNetId();
		}
		//分配网络给指定的项目
		FloatIp result=service.allocateIp(datacenterId, projectId,pool);
		if(result!=null){
			cloudFloatip = new BaseCloudFloatIp();
			cloudFloatip.setFloId(result.getId());
			cloudFloatip.setPrjId(projectId);
			cloudFloatip.setDcId(datacenterId);
			cloudFloatip.setCreateTime(new Date());
			cloudFloatip.setFloIp(result.getIp());
			cloudFloatip.setNetId(pool);
			cloudFloatip.setIsDeleted("0");
			floatipdao.saveOrUpdate(cloudFloatip);
			if(cloudFloatip!=null){
				cloudFloatipVoe=new CloudFloatIp();
				com.eayun.common.util.BeanUtils.copyPropertiesByModel(cloudFloatipVoe, cloudFloatip);
			}
			return cloudFloatipVoe;
		}
		return cloudFloatipVoe;
	}

	@Override
	public int findBindCountByPriId(String prjId) throws AppException{
		//int floatIpCount = floatipdao.findBindCountByPriId(prjId);
		int floatIpCount = floatipdao.getCountByPrjIdVisibled(prjId);
		int floIpSurplus = findFloIpSurplus(prjId);
		return floatIpCount + floIpSurplus;
	}
	
	/**
	 * 获取订单状态为待创建或者创建中的资源的浮动IP总数
	 * @param prjId
	 * @author liuzhuangzhuang
	 * @return
	 */
    private int findFloIpSurplus(String prjId) {
        StringBuilder sql = new StringBuilder();
        /*sql.append("SELECT SUM(IFNULL(oi.prod_count,0)) as ipSurplus");
        sql.append(" FROM cloud_project cp");
        sql.append(" LEFT JOIN cloud_floatip cf ON cf.prj_id = cp.prj_id and cf.is_deleted='0' and is_visable='1'");
        sql.append(" LEFT JOIN cloudorder_floatip cof on cof.prj_id = cp.prj_id");
        sql.append(" LEFT JOIN order_info oi ON oi.order_no=cof.order_no and oi.order_state in('1','2')");
        sql.append(" where cp.prj_id=?");*/
        sql.append(" select ");
        sql.append("    cof.product_count ");
        sql.append(" from ");
        sql.append("    cloudorder_floatip cof ");
        sql.append(" left join ");
        sql.append("    order_info oi ");
        sql.append(" on ");
        sql.append("    cof.order_no = oi.order_no ");
        sql.append(" where ");
        sql.append("    cof.prj_id = ? ");
        sql.append(" and ");
        sql.append("    oi.order_type = '0' ");
        sql.append(" and ");
        sql.append("    oi.order_state in ('1', '2') ");
        List<String> values = new ArrayList<>();
        values.add(prjId);
        Query query = floatipdao.createSQLNativeQuery(sql.toString(), values.toArray());
        List<Object> list = query.getResultList();
        int ipSurplus = 0;
        for (int i =0 ; i< list.size();i++) {
        	ipSurplus += Integer.valueOf(String.valueOf(list.get(i)));
        }
        return ipSurplus;
    }
	@Override
	public int getCountByPrjId(String prjId) throws AppException{
		return floatipdao.getCountByPrjId(prjId);
	}
	
	@Override
	public CloudFloatIp binLb(CloudFloatIp cloudFloatIp) throws AppException {
		String portId = null;
		if(cloudFloatIp.getResourceId()!=null){
			List list = ldvipdao.find("select portId from BaseCloudLdVip where poolId = ?", cloudFloatIp.getResourceId());
			if(list==null || list.size()<=0)throw new AppException("error.globe.system",new String[]{"resourceId is invalid,portId is null"});
			portId = ObjectUtils.toString(list.get(0));
		}
		
		if(service.bindLoadBalancerFloatIp(cloudFloatIp.getDcId(), cloudFloatIp.getPrjId(), portId, cloudFloatIp.getFloId())){
			if(cloudFloatIp.getResourceId()!=null){
				BaseCloudFloatIp baseCloudFloatIp = new BaseCloudFloatIp();
				BeanUtils.copyPropertiesByModel(baseCloudFloatIp, cloudFloatIp);
				floatipdao.merge(baseCloudFloatIp);
				BeanUtils.copyPropertiesByModel(cloudFloatIp, baseCloudFloatIp);
				return cloudFloatIp;
			}else{
				cloudFloatIp.setResourceId(null);
				cloudFloatIp.setResourceType(null);
				BaseCloudFloatIp baseCloudFloatIp = new BaseCloudFloatIp();
				BeanUtils.copyPropertiesByModel(baseCloudFloatIp, cloudFloatIp);
				floatipdao.merge(baseCloudFloatIp);
				BeanUtils.copyPropertiesByModel(cloudFloatIp, baseCloudFloatIp);
				return cloudFloatIp;
			}
		}
		return null;
	}
	@Override
	public boolean deallocateFloatIp(String datacenterId, String projectId,String id) throws AppException {
		try {
			
			 /**
	    	 * 判断资源是否有未完成的订单 --@author zhouhaitao
	    	 */
	        if(checkFloatIpOrderExist(id)){
	        	throw new AppException("该资源有未完成的订单，请取消订单后再进行删除操作！");
	        }
			
			//执行openstack删除操作成功后，进行后续操作
			boolean flag = service.deallocateFloatIp(datacenterId, projectId,id);
			if (flag) {
				tagservice.refreshCacheAftDelRes("floatIP", id);
				BaseCloudFloatIp floatIp = floatipdao.findOne(id);

				floatIp.setIsDeleted("1");
				floatIp.setFloStatus("1");//未占用
				floatIp.setResourceId(null);
				floatIp.setResourceType(null);
				floatIp.setDeleteTime(new Date());
				floatipdao.merge(floatIp);
				
				ecscRecordService.deleteWebsiteByIP(floatIp.getFloIp());		//根据IP删除网站信息
				//判断浮动ip的付款方式
				if (PayType.PAYAFTER.equals(floatIp.getPayType())) {
					StringBuffer sql = new StringBuffer("select customer_id from cloud_project where prj_id=?");
					Query query = floatipdao.createSQLNativeQuery(sql.toString(),new Object[]{floatIp.getPrjId()});
					String cusId="";
					if(query.getResultList().size()>0){
						cusId = query.getResultList().get(0).toString();
					}
					//给计费模块发消息
					ChargeRecord record = new ChargeRecord();
					record.setDatecenterId(floatIp.getDcId());
					record.setCusId(cusId);
					record.setResourceId(floatIp.getFloId());
					record.setResourceType(ResourceType.FLOATIP);
					record.setResourceName(floatIp.getFloIp());
					record.setOpTime(new Date());
					rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_DELETE, JSONObject.toJSONString(record));
				}
			}
			return true;
		}catch (AppException e) {
			throw e;
		}catch (Exception e) {
		    log.error(e.toString(),e);
			return false;
		}
	}
	@Override
	public List<BaseCloudVm> getVmBySubNetWork(String subnetworkId) throws AppException {
		StringBuffer sql = new StringBuffer();
		sql.append("select vm.vm_id,vm.vm_name,vm.vm_ip from cloud_vm vm ");
		sql.append(" where (vm.vm_status = 'ACTIVE' or vm.vm_status='SHUTOFF' or vm.vm_status ='SUSPENDED')");
		sql.append(" and vm.is_deleted='0' ");
		sql.append(" and vm.subnet_id = ? ");
		sql.append(" and vm.charge_state = '0' and vm.subnet_id is not null ");
		sql.append(" and vm.vm_id not in (");
		sql.append(" select f.resource_id ");
		sql.append(" from cloud_floatip f ");
		sql.append(" where f.resource_type='vm'");
		sql.append(" and f.is_deleted = '0'");
		sql.append(" and f.charge_state='0'");
		sql.append(")");
		List list = floatipdao.createSQLNativeQuery(sql.toString(), subnetworkId).getResultList();
		List<BaseCloudVm> listvm = new ArrayList<BaseCloudVm>();
		int a = list.size();
		Object[] objs = null;
		BaseCloudVm vm = null;
        for (int i = 0; i < a; i++) {
        	objs = (Object[]) list.get(i);
        	vm = new BaseCloudVm();
        	vm.setVmId(ObjectUtils.toString(objs[0]));
        	vm.setVmName(ObjectUtils.toString(objs[1]));
        	vm.setVmIp(ObjectUtils.toString(objs[2]));
        	listvm.add(vm);
        }
		return listvm;
	}
	
	@Override
	public int getCountByPro(String prjId) throws AppException {
		return floatipdao.getCountByPrjId(prjId);
	}
	
	@Override
	public CloudFloatIp binDingVmIp(CloudFloatIp cloudFloatIp) throws AppException {
		String datacenterId = cloudFloatIp.getDcId();
        String projectId = cloudFloatIp.getPrjId();
        String vmId = cloudFloatIp.getResourceId();
        String address = cloudFloatIp.getFloIp();
        /**首先调用Openstack底层，给浮动IP绑定云主机*/
        service.addFloatIp(datacenterId, projectId, vmId,cloudFloatIp.getVmIp(), address);
        
        BaseCloudFloatIp baseCloudFloatIp = new BaseCloudFloatIp();
        BeanUtils.copyPropertiesByModel(baseCloudFloatIp, cloudFloatIp);
        floatipdao.merge(baseCloudFloatIp);
        BeanUtils.copyPropertiesByModel(cloudFloatIp, baseCloudFloatIp);
        return cloudFloatIp;
	}
	
	@Override
	public CloudFloatIp unBinDingVmIp(CloudFloatIp cloudFloatIp) throws AppException {
        String datacenterId = cloudFloatIp.getDcId();
        String projectId = cloudFloatIp.getPrjId();
        String vmId = cloudFloatIp.getResourceId();
        String address = cloudFloatIp.getFloIp();
        service.removeFloatIp(datacenterId, projectId, vmId, address);//调用底层
        
        cloudFloatIp.setResourceId(null);
        cloudFloatIp.setResourceType(null);
        BaseCloudFloatIp baseCloudFloatIp = new BaseCloudFloatIp();
        BeanUtils.copyPropertiesByModel(baseCloudFloatIp, cloudFloatIp);
        floatipdao.merge(baseCloudFloatIp);
        BeanUtils.copyPropertiesByModel(cloudFloatIp, baseCloudFloatIp);
        return cloudFloatIp;
	}
	@Override
	public List<BaseCloudFloatIp> getUnBindFloatIp(String prjId) throws AppException {
		return floatipdao.getUnBindFloatIp(prjId);
	}

	@Override
	public BaseCloudFloatIp getFloatIpByResourceId(String resourceId,String resourceType){
		List<BaseCloudFloatIp> list = floatipdao.getFloatIpByResourceId(resourceId,resourceType);
		if(null != list && list.size()>0){
			return list.get(0);
		}
		return null;
	}

	@Override
	public List<BaseCloudNetwork> getNetworkByPrj(String prjId) throws Exception {
		StringBuffer hql =new StringBuffer();
		hql.append("from  BaseCloudNetwork where prjId= ?");
		List<BaseCloudNetwork> list = floatipdao.find(hql.toString(), new Object[]{prjId});
		return list;
	}

	@Override
	public List<BaseCloudSubNetWork> getSubnetByNetId(String netId) throws Exception {
		StringBuffer hql =new StringBuffer();
		hql.append("from  BaseCloudSubNetWork where netId= ?");
		List<BaseCloudSubNetWork> list = floatipdao.find(hql.toString(), new Object[]{netId});
		return list;
	}

	@Override
	public CloudFloatIp bindResource(CloudFloatIp floatIp) throws Exception {
		boolean flag = false;
		if("vm".equals(floatIp.getResourceType())){
			flag = service.addFloatIp(floatIp.getDcId(), floatIp.getPrjId(), floatIp.getResourceId(),floatIp.getVmIp(), floatIp.getFloIp());
		}
		else if("lb".equals(floatIp.getResourceType())){
			flag = service.bindLoadBalancerFloatIp(floatIp.getDcId(), floatIp.getPrjId(),floatIp.getPortId(),floatIp.getFloId());
		}

		if(flag){
			BaseCloudFloatIp cloudFloatIp = floatipdao.findOne(floatIp.getFloId());
			cloudFloatIp.setResourceId(floatIp.getResourceId());
			cloudFloatIp.setResourceType(floatIp.getResourceType());

			floatipdao.saveOrUpdate(cloudFloatIp);
		}

		return floatIp;
	}

	@Override
	public CloudFloatIp unbundingResource(CloudFloatIp floatIp) throws Exception {
		boolean flag = false;
		if("vm".equals(floatIp.getResourceType())){
			flag = service.removeFloatIp(floatIp.getDcId(), floatIp.getPrjId(), floatIp.getResourceId(), floatIp.getFloIp());
		}
		else if("lb".equals(floatIp.getResourceType())){
			flag = service.bindLoadBalancerFloatIp(floatIp.getDcId(), floatIp.getPrjId(),null,floatIp.getFloId());
		}

		if(flag){
			BaseCloudFloatIp cloudFloatIp = floatipdao.findOne(floatIp.getFloId());
			cloudFloatIp.setResourceId(null);
			cloudFloatIp.setResourceType(null);

			floatipdao.saveOrUpdate(cloudFloatIp);
		}

		return floatIp;
	}
	
	 /**
     * 解除已删除云主机与弹性公网IP的关系
     * @param vmId  云主机ID
     */
    public void refreshFloatIpByVm(String vmId){
    	StringBuilder hql = new StringBuilder();
        hql.append("from BaseCloudFloatIp where resourceId=? and resourceType=? and isDeleted ='0'");
        List<String> values = new ArrayList<>();
        values.add(vmId);
        values.add("vm");
        
        BaseCloudFloatIp baseCloudFloatIp = (BaseCloudFloatIp) floatipdao.findUnique(hql.toString(), values.toArray());
        if(null != baseCloudFloatIp && !StringUtils.isEmpty(baseCloudFloatIp.getFloId())){
        	baseCloudFloatIp.setResourceId(null);
        	baseCloudFloatIp.setResourceType(null);
        	floatipdao.merge(baseCloudFloatIp);
        }
    }

	@Override
	public boolean checkFloWebSite(String floIp) throws Exception {
		List<BaseWebDataCenterIp> list = ecscRecordService.getWebDataCenterIp(floIp);
		if(null == list || list.isEmpty()){
			return false;
		}
		return true;
	}

	@Override
	public JSONObject getFloatIpUsedSituation() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}