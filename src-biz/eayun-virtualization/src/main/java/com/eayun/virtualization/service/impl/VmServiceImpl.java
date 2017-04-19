package com.eayun.virtualization.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.charge.bean.ResourceCheckBean;
import com.eayun.charge.model.ChargeRecord;
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.*;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.*;
import com.eayun.common.util.TransactionHookUtil.Hook;
import com.eayun.costcenter.model.MoneyAccount;
import com.eayun.costcenter.service.AccountOverviewService;
import com.eayun.customer.model.Customer;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.Image;
import com.eayun.eayunstack.model.InterfaceAttachment;
import com.eayun.eayunstack.model.Vm;
import com.eayun.eayunstack.service.OpenstackSecretkeyService;
import com.eayun.eayunstack.service.OpenstackVmService;
import com.eayun.eayunstack.service.RestService;
import com.eayun.monitor.bean.MonitorAlarmUtil.MonitorResourceType;
import com.eayun.monitor.ecmcservice.EcmcAlarmService;
import com.eayun.monitor.service.AlarmService;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.order.model.BaseOrder;
import com.eayun.order.model.BaseOrderResource;
import com.eayun.order.model.Order;
import com.eayun.order.service.OrderService;
import com.eayun.price.bean.ParamBean;
import com.eayun.price.bean.PriceDetails;
import com.eayun.price.service.BillingFactorService;
import com.eayun.project.service.ProjectService;
import com.eayun.sys.model.SysDataTree;
import com.eayun.syssetup.service.SysDataTreeService;
import com.eayun.virtualization.baseservice.BaseVmService;
import com.eayun.virtualization.dao.CloudImageDao;
import com.eayun.virtualization.dao.CloudSubNetWorkDao;
import com.eayun.virtualization.dao.CloudVmDao;
import com.eayun.virtualization.model.*;
import com.eayun.virtualization.service.*;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * VmServiceImpl
 * 
 * @Filename: VmServiceImpl.java
 * @Description:
 * @Version: 1.0
 * @Author: chenhao
 * @Email: hao.chen@eayun.com
 * @History:<br>
 * 				<li>Date: 2015年9月1日</li>
 *               <li>Version: 1.0</li>
 *               <li>Content: create</li>
 *
 */
@Transactional
@Service
public class VmServiceImpl extends BaseVmService implements VmService {
	private static final Logger log = LoggerFactory.getLogger(VmServiceImpl.class);
	@Autowired
	private CloudVmDao cloudVmDao;
	@Autowired
	private CloudImageDao cloudImageDao;
	@Autowired
	private CloudSubNetWorkDao cloudWorkDao;
	@Autowired
	private OpenstackVmService openstackVmService;
	@Autowired
	private RestService restService;
	@Autowired
	private CloudFlavorService cloudFlavorService;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private TagService tagService;
	@Autowired
	private VolumeService volumeService;
	@Autowired
	private AlarmService alarmService;
	@Autowired
	private MemberService memberService;
	@Autowired
	private EcmcAlarmService ecmcAlarmService;
	@Autowired
	private OrderService orderService;
	@Autowired
	private EayunRabbitTemplate rabbitTemplate;
	@Autowired
	private CloudBatchResourceService cloudBatchResourceService;
	@Autowired
	private CloudOrderVmService cloudOrderVmService;
	@Autowired
	private VolumeOrderService cloudOrderVolumeService;
	@Autowired
	private AccountOverviewService accountOverviewSerivce;
	@Autowired
	private CloudFloatIpService floatIpService;
	@Autowired
	private BillingFactorService billingFactorService;
	@Autowired
	private VmSecurityGroupService vmSgService;
	@Autowired
	private PortMappingService portMappingService;
	@Autowired 
	private MessageCenterService messageCenterService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private SysDataTreeService sysDataTreeService;
	@Autowired
	private CustomerService customerService;
	@Autowired
	private CloudSecretKeyService secretKeyService;
	@Autowired
	private SecretkeyVmService secretkeyVmService;
	@Autowired
    private OpenstackSecretkeyService openstackSecretKeyService;
	@Autowired
	private VolumeTypeService volTypeService;
	
	/**
	 * 校验当前主机的受管子网是否可以切换
	 * <p>
	 * 1. 没有绑定公网IP
	 * </p>
	 * <p>
	 * 2. 没有关联负载均衡器的成员
	 * </p>
	 * <p>
	 * 3. 没有作为端口映射的对应
	 * </p>
	 * <p>
	 * 满足以上3点，返回false;否则 返回 true
	 * </p>
	 * ---------------------------------------
	 * 
	 * @author zhouhaitao
	 * @param vm
	 * @return
	 */
	public boolean checkVmIpUsed(CloudVm vm) {
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
		
		List<BaseCloudPortMapping> portMappingList = portMappingService.queryPortMappingListByDestinyId(vm.getVmId());
		if(null!= portMappingList && portMappingList.size() > 0){
			isVmIpUsed = true;
			return isVmIpUsed;
		}
		
		isVmIpUsed = checkLdMemberByVm(vm.getVmId());
		return isVmIpUsed;
	}

	@Override
	public List<CloudVm> getUnDeletedVmListByProject(String prjId) {
		List<BaseCloudVm> list = cloudVmDao.getUnDeletedListByProject(prjId);
		List<CloudVm> result = new ArrayList<CloudVm>();
		for (BaseCloudVm baseCloudVm : list) {
			CloudVm cloudVm = new CloudVm();
			BeanUtils.copyPropertiesByModel(cloudVm, baseCloudVm);
			result.add(cloudVm);
		}

		return result;
	}

	@Override
	public CloudVm findVm(String vmId) {
		BaseCloudVm baseCloudVm = cloudVmDao.findOne(vmId);
		if (baseCloudVm != null) {
			CloudVm cloudVm = new CloudVm();
			BeanUtils.copyPropertiesByModel(cloudVm, baseCloudVm);

			return cloudVm;
		}
		return null;
	}

	/**
	 * 查询云主机列表<br>
	 * ------------
	 * 
	 * @author zhouhaitao
	 * @param page
	 *            分页结果集
	 * @param map
	 *            查询条件
	 * @param sessionUser
	 *            当前用户
	 * @param queryMap
	 *            分页条件
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Page listVm(Page page, ParamsMap map, SessionUserInfo sessionUser, QueryMap queryMap) throws Exception {
		int index = 0;
		Object[] args = new Object[10];
		String projectId = "";
		String datacenterId = "";
		String name = "";
		String type = "";
		String vmStatus = "";
		String vmSystem = "";
		String vmCpu = "";

		if (null != map && null != map.getParams()) {
			projectId = map.getParams().get("prjId") != null ? map.getParams().get("prjId") + "" : "";
			datacenterId = map.getParams().get("dcId") != null ? map.getParams().get("dcId") + "" : "";
			name = map.getParams().get("title") != null ? map.getParams().get("title") + "" : "";
			type = map.getParams().get("queryType") != null ? map.getParams().get("queryType") + "" : "";
			vmStatus = map.getParams().get("status") != null ? "" + map.getParams().get("status") : "";
			vmSystem = map.getParams().get("system") != null ? map.getParams().get("system") + "" : "";
			vmCpu = map.getParams().get("cpu") != null ? map.getParams().get("cpu") + "" : "";
		}

		StringBuffer sql = new StringBuffer();
		sql.append(" select ");
		sql.append("   	vm.vm_id as vmId,");
		sql.append("   	vm.vm_name as vmName,");
		sql.append("   	vm.prj_id as prjId,");
		sql.append("   	vm.dc_id as dcId,");
		sql.append("   	vm.vm_status as vmStatus,");
		sql.append("   	vm.create_time as createTime,");
		sql.append("   	vm.vm_Ip as vmIp ,");
		sql.append("   	flv.flavor_vcpus as cpus,");
		sql.append("   	flv.flavor_ram as rams, ");
		sql.append("   	flv.flavor_disk as disks,");
		sql.append("   	prj.prj_name as prjName,");
		sql.append("   	flo.flo_ip as floatIp ,");
		sql.append("   	vm.sys_type as sysType ,");
		sql.append("   	vol.vol_size as capacityDisk,");
		sql.append("   	vm.self_ip as selfIp ,");
		sql.append("   	vm.pay_type as payType ,");
		sql.append("   	vm.charge_state as chargeState ,");
		sql.append("   	vm.end_time as endTime,");
		sql.append("   	vm.from_imageid as fromImageId,");
		sql.append("   	dc.dc_name as dcName");
		sql.append(" from cloud_vm vm ");
		sql.append(" left join dc_datacenter dc ON vm.dc_id=dc.id");
		sql.append(" left join cloud_flavor flv ON vm.flavor_id=flv.flavor_id");
		sql.append(" left join cloud_project prj ON vm.prj_id=prj.prj_id");
		sql.append(" left join ");
		sql.append(" (");
		sql.append(" 	select sum(vol_size) as vol_size,vm_id ");
		sql.append(" 	from cloud_volume  ");
		sql.append(" 	where vol_bootable = '0'  ");
		sql.append(" 	group by vm_id ");
		sql.append(" ) as vol on vm.vm_id=vol.vm_id");
		sql.append(
				" left join cloud_floatip as flo on vm.vm_id=flo.resource_id and flo.resource_type ='vm' and flo.is_deleted = '0'");
		sql.append(" where vm.is_deleted = '0'");
		sql.append(" and vm.is_visable = '1'");
		sql.append(" and vm.prj_id=?");
		args[index] = projectId;
		index++;
		sql.append(" and vm.dc_id=?");
		args[index] = datacenterId;
		index++;
		if (!StringUtils.isEmpty(vmStatus)) {
			if ("1".equals(vmStatus)) {
				sql.append(" and vm.charge_state = ? ");
			} else if ("2".equals(vmStatus)) {
				sql.append(" and (vm.charge_state = '3' or vm.charge_state = ?) ");
			} else {
				if ("BUILDING".equals(vmStatus)) {
					sql.append(" and (vm.vm_status = ? or vm.vm_status = 'BUILD')");
					sql.append(" and vm.charge_state = '0' ");
				} else {
					sql.append(" and vm.vm_status = ? ");
					sql.append(" and vm.charge_state = '0' ");
				}
			}
			args[index] = vmStatus;
			index++;
		}
		if (!StringUtils.isEmpty(vmSystem)) {
			sql.append(" and vm.sys_type = ? ");
			args[index] = vmSystem;
			index++;
		}
		if (!StringUtils.isEmpty(vmCpu)) {
			sql.append(" and flv.flavor_vcpus = ? ");
			args[index] = vmCpu;
			index++;
		}
		if (!StringUtils.isEmpty(name) && "name".equals(type)) {
			sql.append(" and binary vm.vm_name like ?");
			name = name.replaceAll("\\_", "\\\\_").replaceAll("\\%", "\\\\%");
			args[index] = "%" + name + "%";
			index++;
		} else if (!StringUtils.isEmpty(name) && "tag".equals(type)) {
			sql.append(" and vm.vm_id  in (" + handleQueryTagCondition(name) + ") ");
		} else if (!StringUtils.isEmpty(name) && "ips".equals(type)) {
			sql.append(" and (vm.vm_Ip like ? or vm.self_ip like ? or flo.flo_ip like ?) ");
			name = name.replaceAll("\\_", "\\\\_").replaceAll("\\%", "\\\\%");
			args[index] = "%" + name + "%";
			index++;
			args[index] = "%" + name + "%";
			index++;
			args[index] = "%" + name + "%";
			index++;
		}
		sql.append(" group by vm.vm_id order by vm.create_time desc");

		Object[] params = new Object[index];
		System.arraycopy(args, 0, params, 0, index);
		page = cloudVmDao.pagedNativeQuery(sql.toString(), queryMap, params);
		List newList = (List) page.getResult();
		for (int i = 0; i < newList.size(); i++) {
			Object[] objs = (Object[]) newList.get(i);
			CloudVm vm = new CloudVm();
			vm.setVmId(String.valueOf(objs[0]));
			vm.setVmName(String.valueOf(objs[1]));
			vm.setPrjId(String.valueOf(objs[2]));
			vm.setDcId(String.valueOf(objs[3]));
			vm.setVmStatus(String.valueOf(objs[4]));
			vm.setCreateTime((Date) objs[5]);
			vm.setVmIp(String.valueOf(objs[6]));
			vm.setCpus(Integer.parseInt(objs[7] != null ? String.valueOf(objs[7]) : "0"));
			vm.setRams(Integer.parseInt(objs[8] != null ? String.valueOf(objs[8]) : "0"));
			vm.setDisks(Integer.parseInt(objs[9] != null ? String.valueOf(objs[9]) : "0"));
			vm.setPrjName(String.valueOf(objs[10]));
			vm.setFloatIp(String.valueOf(objs[11]));
			String sysType = String.valueOf(objs[12]);
			vm.setSelfIp(String.valueOf(objs[14]));
			vm.setPayType(String.valueOf(objs[15]));
			vm.setChargeState(String.valueOf(objs[16]));
			vm.setEndTime((Date) objs[17]);
			vm.setFromImageId(String.valueOf(null!=objs[18]?objs[18]:""));
			vm.setDcName(String.valueOf(objs[19]));
			vm.setPayTypeStr(CloudResourceUtil.escapePayType(vm.getPayType()));
			vm.setVmStatusStr(CloudResourceUtil.escapseChargeState(vm.getChargeState()));
			if (!StringUtils.isEmpty(sysType) && !"null".equals(sysType)) {
				SysDataTree sdt = DictUtil.getDataTreeByNodeId(sysType);
				if(null != sdt){
					vm.setSysTypeEn(sdt.getNodeNameEn());
					vm.setSysType(sdt.getNodeName());
				}
			}
			vm.setDataCapacity(Integer.parseInt(objs[13] != null ? String.valueOf(objs[13]) : "0"));
			if ("BUILD".equalsIgnoreCase(vm.getVmStatus())) {
				vm.setVmStatus("BUILDING");
			}
			if (CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(vm.getChargeState())) {
				vm.setVmStatusStr(DictUtil.getStatusByNodeEn("vm", vm.getVmStatus()));
			}
			if(!StringUtil.isEmpty(vm.getFromImageId())){
				String imageId = queryChargeImageId(vm.getFromImageId());
				if(!StringUtil.isEmpty(imageId)){
					vm.setSourceId(imageId);
				}
			}
			newList.set(i, vm);
		}
		return page;
	}
	
	/**
	 * 查询回收站的云主机列表
	 * ------------
	 * @author zhouhaitao
	 * @param page 分页结果集
	 * @param map 查询条件
	 * @param sessionUser 当前用户
	 * @param queryMap 分页条件
	 * @return 
	 * @throws AppException
	 */
	@SuppressWarnings("unchecked")
	public Page getRecycleVmList(Page page,ParamsMap map,SessionUserInfo sessionUser, QueryMap queryMap)throws Exception{

		int index = 0;
		Object[] args = new Object[10];
		String name = "";
		String dcId = "";

		if (null != map && null != map.getParams()) {
			name = map.getParams().get("name") != null ? map.getParams().get("name") + "" : "";
			dcId = map.getParams().get("dcId") != null ? map.getParams().get("dcId") + "" : "";
		}

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
		sql.append("   	vm.vm_status");
		sql.append(" from cloud_vm vm ");
		sql.append(" left join cloud_flavor flv ON vm.flavor_id=flv.flavor_id and vm.dc_id = flv.dc_id");
		sql.append(" left join cloud_network net ON vm.net_id = net.net_id");
		sql.append(" left join cloud_subnetwork sub ON vm.subnet_id = sub.subnet_id");
		sql.append(" left join cloud_subnetwork selfSub ON vm.self_subnetid = selfSub.subnet_id");
		sql.append(" left join dc_datacenter dc ON vm.dc_id=dc.id");
		sql.append(" left join (");
		if (sessionUser.getIsAdmin()) {
			sql.append(" cloud_project prj ");
		} else {
			sql.append(" ( ");
			sql.append(" select  ");
			sql.append("  	s.project_id as prj_id,");
			sql.append("  	p.dc_id ,");
			sql.append("  	p.customer_id ,");
			sql.append("  	p.prj_name");
			sql.append(" from sys_selfuserprj s ");
			sql.append(" left join cloud_project p ");
			sql.append(" on s.project_id=p.prj_id ");
			sql.append(" where 1=1 ");
			sql.append(" and user_id = ? ");
			sql.append(" ) prj");
			
			args[index] = sessionUser.getUserId();
			index++;
		}
		sql.append(" ) ON vm.prj_id=prj.prj_id");
		sql.append(" where vm.is_deleted = '2'");
		sql.append(" and vm.is_visable = '1'");
		sql.append(" and prj.customer_id = ?");
		args[index] = sessionUser.getCusId();
		index++;
		
		if (!StringUtils.isEmpty(dcId)) {
			sql.append(" and vm.dc_id = ? ");
			args[index] = dcId;
			index++;
		}
		if (!StringUtils.isEmpty(name)) {
			sql.append(" and binary vm.vm_name like ?");
			name = name.replaceAll("\\_", "\\\\_").replaceAll("\\%", "\\\\%");
			args[index] = "%" + name + "%";
			index++;
		}
		sql.append(" order by vm.delete_time desc");

		Object[] params = new Object[index];
		System.arraycopy(args, 0, params, 0, index);
		page = cloudVmDao.pagedNativeQuery(sql.toString(), queryMap, params);
		@SuppressWarnings("rawtypes")
		List newList = (List) page.getResult();
		for (int i = 0; i < newList.size(); i++) {
			int ind =0;
			Object[] objs = (Object[]) newList.get(i);
			CloudVm vm = new CloudVm();
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
			vm.setVmStatusStr(DictUtil.getStatusByNodeEn("vm", vm.getVmStatus()));
			
			if (!StringUtils.isEmpty(sysType) && !"null".equals(sysType)) {
				vm.setSysType(DictUtil.getDataTreeByNodeId(sysType).getNodeName());
			}
			newList.set(i, vm);
		}
		return page;
	
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
		sql.append("   	vm.vm_status");
		sql.append(" from cloud_vm vm ");
		sql.append(" left join cloud_flavor flv ON vm.flavor_id=flv.flavor_id");
		sql.append(" left join cloud_network net ON vm.net_id = net.net_id");
		sql.append(" left join cloud_subnetwork sub ON vm.subnet_id = sub.subnet_id");
		sql.append(" left join cloud_subnetwork selfSub ON vm.self_subnetid = selfSub.subnet_id");
		sql.append(" left join dc_datacenter dc ON vm.dc_id=dc.id");
		sql.append(" left join cloud_project prj ON vm.prj_id=prj.prj_id");
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
			vm.setVmStatusStr(DictUtil.getStatusByNodeEn("vm", vm.getVmStatus()));
			
			if (!StringUtils.isEmpty(sysType) && !"null".equals(sysType)) {
				vm.setSysType(DictUtil.getDataTreeByNodeId(sysType).getNodeName());
			}
		}
		return vm;
	
	
	}

	/**
	 * 查询当前登录用户所属客户的创建的项目的列表(数据中心-项目名称)<br>
	 * --------------------------
	 * 
	 * @author zhouhaitao
	 * @param sessionUser
	 * @return
	 */
	public List<CloudProject> findDcAndPrj(SessionUserInfo sessionUser) {
		List<Object> list = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" select  ");
		sql.append(" 	d.dc_name ,	  ");
		sql.append(" 	d.id ,	      ");
		sql.append(" 	p.prj_name ,  ");
		sql.append(" 	p.prj_id	  ");
		sql.append(" from ");
		if (sessionUser.getIsAdmin()) {
			sql.append(" cloud_project p ");
		} else {
			sql.append(" ( ");
			sql.append(" select  ");
			sql.append("  	s.project_id as prj_id,");
			sql.append("  	p.dc_id ,");
			sql.append("  	p.customer_id ,");
			sql.append("  	p.prj_name");
			sql.append(" from sys_selfuserprj s ");
			sql.append(" left join cloud_project p ");
			sql.append(" on s.project_id=p.prj_id ");
			sql.append(" where 1=1 ");
			sql.append(" and user_id = ? ");
			sql.append(" ) p");

			list.add(sessionUser.getUserId());
		}
		sql.append(" LEFT JOIN dc_datacenter d ON p.dc_id = d.id ");
		sql.append(" WHERE 1=1 ");
		sql.append(" and p.customer_id = ? ");
		list.add(sessionUser.getCusId());
		sql.append(" order by d.dc_name desc ");

		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), list.toArray());
		List<CloudProject> cloudProjectList = new ArrayList<CloudProject>();
		@SuppressWarnings("rawtypes")
		List listResult = query.getResultList();
		for (int i = 0; i < listResult.size(); i++) {
			Object[] obj = (Object[]) listResult.get(i);
			CloudProject cloudProject = new CloudProject();
			cloudProject.setDcName(String.valueOf(obj[0]));
			cloudProject.setDcId(String.valueOf(obj[1]));
			cloudProject.setPrjName(String.valueOf(obj[2]));
			cloudProject.setProjectId(String.valueOf(obj[3]));
			cloudProjectList.add(cloudProject);
		}

		return cloudProjectList;
	}

	/**
	 * 根据Id查询云主机详情信息 ---------------
	 * 
	 * @author zhouhaitao
	 * @param vmId
	 * @return 查询结果对象
	 * @throws Exception
	 */
	@Override
	public CloudVm getById(String vmId) throws Exception {
		StringBuffer sql = new StringBuffer();
		sql.append(" select ");
		sql.append(" 	vm.vm_id as vmId,	");
		sql.append(" 	vm.vm_name as vmName,	");
		sql.append(" 	vm.prj_id as prjId,	");
		sql.append(" 	vm.dc_id as dcId,	");
		sql.append(" 	vm.vm_status as vmStatus,	");
		sql.append(" 	vm.vm_ip as vmIp,	");
		sql.append(" 	flv.flavor_vcpus as cpus,	");
		sql.append(" 	flv.flavor_ram as rams,	");
		sql.append(" 	flv.flavor_disk as disks,	");
		sql.append(" 	prj.prj_name as prjName,	");
		sql.append(" 	vm.sys_type as sysType,	");
		sql.append(" 	vm.vm_description as vmDescription,	");
		sql.append(" 	net.net_name as netName,	");
		sql.append(" 	flo.flo_ip as floatIp,	");
		sql.append(" 	flo.flo_id as floatId,	");
		sql.append(" 	dc.dc_name as dcName,	");
		sql.append(" 	subnet.subnet_name,	");
		sql.append("   	vol.vol_size as capacityDisk,");
		sql.append(" 	vm.os_type as osType,");
		sql.append(" 	vm.charge_state as chargeState,");
		sql.append(" 	vm.pay_type as payType,");
		sql.append(" 	vm.create_time as createTime,");
		sql.append(" 	vm.end_time as endTime,");
		sql.append(" 	vm.self_subnetid as selfSubnetId,");
		sql.append(" 	vm.self_ip as selfIp,");
		sql.append(" 	selfsubnet.subnet_name as selfSubnetName,");
		sql.append(" 	vm.net_id as netId ,");
		sql.append(" 	vm.subnet_id as subnetId,");
		sql.append("   	image.image_name as imageName,");
		sql.append(" 	vm.from_imageid as imageId");
		sql.append(" from cloud_vm vm ");
		sql.append(" left join cloud_flavor flv on vm.flavor_id=flv.flavor_id and vm.dc_id=flv.dc_id");
		sql.append(" left join cloud_network net on vm.net_id=net.net_id");
		sql.append(" left join cloud_project prj on vm.prj_id=prj.prj_id");
		sql.append(" left join dc_datacenter dc on dc.id = vm.dc_id ");
		sql.append(" left join cloud_floatip flo on flo.resource_id = vm.vm_id ");
		sql.append(" and flo.resource_type ='vm' and flo.is_deleted ='0'  ");
		sql.append(" left join cloud_subnetwork subnet on subnet.subnet_id = vm.subnet_id ");
		sql.append(" left join cloud_subnetwork selfsubnet on selfsubnet.subnet_id = vm.self_subnetid ");
		sql.append(" left join cloud_image image ON vm.from_imageid=image.image_id");
		sql.append(" left join ");
		sql.append(" (");
		sql.append(" 	select sum(vol_size) as vol_size,vm_id ");
		sql.append(" 	from cloud_volume  ");
		sql.append(" 	where vol_bootable = '0'  ");
		sql.append(" 	group by vm_id ");
		sql.append(" ) as vol on vm.vm_id=vol.vm_id");
		sql.append(" where 1=1");
		sql.append(" and vm.vm_id= ? ");
		sql.append(" and vm.is_deleted= ? ");

		CloudVm cloudVm = null;
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[] { vmId, "0" });
		@SuppressWarnings("rawtypes")
		List list = new ArrayList();
		if (null != query) {
			list = query.getResultList();
		}
		if (null != list && list.size() == 1) {
			Object[] objs = (Object[]) list.get(0);
			cloudVm = new CloudVm();

			cloudVm.setVmId(String.valueOf(objs[0]));
			cloudVm.setVmName(String.valueOf(objs[1]));
			cloudVm.setPrjId(String.valueOf(objs[2]));
			cloudVm.setDcId(String.valueOf(objs[3]));
			cloudVm.setVmStatus(String.valueOf(objs[4]));
			cloudVm.setVmIp(String.valueOf(objs[5]));
			cloudVm.setCpus(Integer.parseInt(objs[6] != null ? String.valueOf(objs[6]) : "0"));
			cloudVm.setRams(Integer.parseInt(objs[7] != null ? String.valueOf(objs[7]) : "0"));
			cloudVm.setDisks(Integer.parseInt(objs[8] != null ? String.valueOf(objs[8]) : "0"));
			cloudVm.setPrjName(String.valueOf(objs[9]));
			String systemType = String.valueOf(objs[10]);
			if (!StringUtils.isEmpty(systemType) && !"null".equals(systemType)) {
				cloudVm.setSysType(DictUtil.getDataTreeByNodeId(systemType).getNodeName());
			}
			cloudVm.setVmDescripstion(String.valueOf(objs[11] == null ? "" : objs[11]));
			cloudVm.setNetName(String.valueOf(objs[12]));
			cloudVm.setFloatIp(String.valueOf(objs[13] == null ? "" : objs[13]));
			cloudVm.setFloatId(String.valueOf(objs[14] == null ? "" : objs[14]));
			cloudVm.setDcName(String.valueOf(objs[15]));
			cloudVm.setSubnetName(String.valueOf(objs[16] == null ? "" : objs[16]));
			cloudVm.setDataCapacity(Integer.parseInt(objs[17] != null ? String.valueOf(objs[17]) : "0"));
			String osType = String.valueOf(objs[18]);
			cloudVm.setChargeState(String.valueOf(objs[19] == null ? "" : objs[19]));
			cloudVm.setPayType(String.valueOf(objs[20] == null ? "" : objs[20]));
			cloudVm.setCreateTime((Date) objs[21]);
			cloudVm.setEndTime((Date) objs[22]);
			cloudVm.setSelfSubnetId(String.valueOf(objs[23] == null ? "" : objs[23]));
			cloudVm.setSelfIp(String.valueOf(objs[24] == null ? "" : objs[24]));
			cloudVm.setSelfSubnetName(String.valueOf(objs[25] == null ? "" : objs[25]));
			cloudVm.setNetId(String.valueOf(objs[26] == null ? "" : objs[26]));
			cloudVm.setSubnetId(String.valueOf(objs[27] == null ? "" : objs[27]));
			cloudVm.setImageName(String.valueOf(objs[28] == null ? "" : objs[28]));
			cloudVm.setFromImageId(String.valueOf(objs[29] == null ? "" : objs[29]));

			cloudVm.setPayTypeStr(CloudResourceUtil.escapePayType(cloudVm.getPayType()));
			cloudVm.setVmStatusStr(CloudResourceUtil.escapseChargeState(cloudVm.getChargeState()));
			if (!StringUtils.isEmpty(osType) && !"null".equals(osType)) {
				cloudVm.setOsType(DictUtil.getDataTreeByNodeId(osType).getNodeName());
			}
			if ("BUILD".equalsIgnoreCase(cloudVm.getVmStatus())) {
				cloudVm.setVmStatus("BUILDING");
			}
			List<BaseCloudSecurityGroup> sgList = getSecurityGroupByVm(cloudVm.getVmId());
			String str = "";
			for (BaseCloudSecurityGroup sg : sgList) {
				if("default".equals(sg.getSgName())){
					sg.setSgName("默认安全组");
				}
				str = str + sg.getSgName() + "、";
			}
			if (!StringUtils.isEmpty(str)) {
				cloudVm.setSecurityGroups(str.substring(0, str.length() - 1));
			}
			if (CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(cloudVm.getChargeState())) {
				cloudVm.setVmStatusStr(DictUtil.getStatusByNodeEn("vm", cloudVm.getVmStatus()));
			}
			
			cloudVm.setSshCount(secretkeyVmService.SSHCountbyVm(cloudVm.getVmId())); 
		}

		return cloudVm;

	}

	/**
	 * <p>
	 * 购买云主机--提交订单
	 * </p>
	 * 
	 * @author zhouhaitao
	 * @param cloudOrder
	 *            提交的云主机订单信息
	 * @param user
	 *            当前登录用户信息
	 * @return 错误信息标示，成功 返回 <code>null</code>
	 * 
	 */
	public String buyVm(CloudOrderVm cloudOrder, SessionUserInfo user) throws Exception {
		String errMsg = null;
		try {
			cloudOrder.setCreateUser(user.getUserName());
			cloudOrder.setCusId(user.getCusId());
			cloudOrder.setCreateOrderDate(new Date());
			errMsg = checkVmQuota(cloudOrder,"buy");
			if (!StringUtils.isEmpty(errMsg)) {
				errMsg = "OUT_OF_QUOTA";
				return errMsg;
			}
			if(PayType.PAYBEFORE.equals(cloudOrder.getPayType())){
				BigDecimal totalPayment = calcVmPrice(cloudOrder);
				if(totalPayment.compareTo(cloudOrder.getPaymentAmount()) !=0){
					errMsg = "CHANGE_OF_BILLINGFACTORY";
					return errMsg;
				}
				
			}
			else if (PayType.PAYAFTER.equals(cloudOrder.getPayType())) {
				MoneyAccount accountMoney = accountOverviewSerivce.getAccountInfo(user.getCusId());
				String buyCondition = sysDataTreeService.getBuyCondition();
				BigDecimal createResourceLimitedMoney = new BigDecimal(buyCondition);
				if (accountMoney.getMoney().compareTo(createResourceLimitedMoney) <= 0) {
					errMsg = "NOT_SUFFICIENT_FUNDS";
					return errMsg;
				}
			}
			
			Order order = createVmOrder(cloudOrder, user);
			cloudOrder.setOrderNo(order.getOrderNo());
			
			if ("1".equals(cloudOrder.getBuyFloatIp())) {
				CloudOrderFloatIp cloudOrderFloatIp = new CloudOrderFloatIp();
				cloudOrderFloatIp.setBuyCycle(cloudOrder.getBuyCycle());
				cloudOrderFloatIp.setCreateTime(cloudOrder.getCreateOrderDate());
				cloudOrderFloatIp.setCreUser(cloudOrder.getCreateUser());
				cloudOrderFloatIp.setDcId(cloudOrder.getDcId());
				cloudOrderFloatIp.setPrjId(cloudOrder.getPrjId());
				cloudOrderFloatIp.setOrderType(cloudOrder.getOrderType());
				cloudOrderFloatIp.setPayType(cloudOrder.getPayType());
				cloudOrderFloatIp.setProductCount(cloudOrder.getCount());
				cloudOrderFloatIp.setOrderNo(cloudOrder.getOrderNo());
				
				floatIpService.buyFloatIp(cloudOrderFloatIp, false);
			}
			
			
			cloudOrderVmService.saveOrUpdate(cloudOrder);
			
			//如果购买云主机时一并购买了数据盘
			if(cloudOrder.getDataDisk()>0){
				createVolumeOrder(cloudOrder);
			}

			if (PayType.PAYAFTER.equals(cloudOrder.getPayType())) {
				try{
					createVm(cloudOrder);
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
	 * 资源升级接口 
	 * --------------
	 * 
	 * @author zhouhaitao
	 * @param cloudOrder
	 *            云主机订单信息
     * @param user
	 * 
	 * @return
	 * @throws Exception 
	 * 
	 */
	public String resizeVm(CloudOrderVm cloudOrder ,SessionUserInfo user) throws Exception {
		String errMsg = null;
		try {
			CloudVm cloudVm = queryVmChargeById(cloudOrder.getVmId());
			cloudOrder.setCreateUser(user.getUserName());
			cloudOrder.setCreateOrderDate(new Date());
			cloudOrder.setPayType(cloudVm.getPayType());
//			cloudOrder.setVmCpu(cloudVm.getCpus());
//			cloudOrder.setVmRam(cloudVm.getRams());
			cloudOrder.setSysDiskType(cloudOrder.getSysDiskType());
			cloudOrder.setDisk(cloudVm.getDisks());
			cloudOrder.setSysType(cloudVm.getSysType());
			cloudOrder.setSysTypeEn(cloudVm.getSysTypeEn());
			cloudOrder.setVmName(cloudVm.getVmName());
			cloudOrder.setEndTime(cloudVm.getEndTime());
			cloudOrder.setCusId(user.getCusId());
			if(cloudOrder.getVmRam() !=cloudVm.getRams()  || cloudOrder.getVmCpu() != cloudVm.getCpus()){
				return "CHANGE_OF_RESOURCESIZE";
			}
			errMsg = checkVmQuota(cloudOrder,"resize");
			if (!StringUtils.isEmpty(errMsg)) {
				return "OUT_OF_QUOTA";
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
				cloudOrder.setCycleCount(cloudVm.getCycleCount());
				BigDecimal totalPayment = calcVmPrice(cloudOrder);
				if(cloudOrder.getPaymentAmount().compareTo(totalPayment)!= 0){
					return "CHANGE_OF_BILLINGFACTORY";
				}
			}
			if(checkVmOrderExsit(cloudOrder.getVmId())){
				errMsg = "UPGRADING_OR_INORDER";
				return errMsg;
			}
			if(checkCreatingImageCount(cloudOrder.getVmId())){
				errMsg = "IMAGE_OF_CREATING";
				return errMsg;
			}
			
			Order order = createVmOrder(cloudOrder, user);
			cloudOrder.setOrderNo(order.getOrderNo());

			cloudOrderVmService.saveOrUpdate(cloudOrder);
			
			if (PayType.PAYAFTER.equals(cloudOrder.getPayType())) {
				CloudVm vm = new CloudVm();
				vm.setDcId(cloudOrder.getDcId());
				vm.setPrjId(cloudOrder.getPrjId());
				vm.setVmId(cloudOrder.getVmId());
				vm.setCpus(cloudOrder.getCpu());
				vm.setRams(cloudOrder.getRam());
				vm.setDisks(cloudOrder.getDisk());
				vm.setOrderNo(cloudOrder.getOrderNo());
				vm.setCusId(user.getCusId());
				vm.setVmName(cloudVm.getVmName());
				vm.setSysType(cloudVm.getSysType());
				vm.setPayType(cloudVm.getPayType());
				vm.setEndTime(cloudVm.getEndTime());
				vm.setFromImageId(cloudVm.getFromImageId());
				
				try{
					resizeVm(vm);
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
	 * 编辑云主机 ------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void modifyVm(CloudVm cloudVm) throws AppException {
		try {
			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			if (!cloudVm.getVmName().equals(vm.getVmName())) {
				openstackVmService.modifyVm(cloudVm);
			}

			vm.setVmName(cloudVm.getVmName());
			vm.setVmDescripstion(cloudVm.getVmDescripstion());

			cloudVmDao.merge(vm);

		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}
	}

	/**
	 * 删除云主机<br>
	 * 
	 * 1.修改云主机删除标志及删除时间 <br>
	 * 2.调用删除云主机任务<br>
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * @param sessionUser
	 * @throws Exception 
	 */
	@Transactional(noRollbackFor=AppException.class)
	public void deleteVm(CloudVm cloudVm, SessionUserInfo sessionUser) throws Exception {
		cloudVm.setCusId(sessionUser.getCusId());
		/**
    	 * 判断资源是否有未完成的订单 --@author zhouhaitao
    	 */
		if(checkVmOrderExsit(cloudVm.getVmId(),true,true)){
			throw new AppException("该资源有未完成的订单，请取消订单后再进行删除操作！");
		}
		
		if("0".equals(cloudVm.getDeleteType())){
			softDeleteVm(cloudVm, sessionUser);
		}
		else if("1".equals(cloudVm.getDeleteType())){
			forceDeleteVm(cloudVm, sessionUser.getUserName());
		}
		else if("2".equals(cloudVm.getDeleteType())){
			openstackVmService.forceDelete(cloudVm);

			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			vm.setVmStatus("DELETING");
			vm.setDeleteTime(new Date());
			vm.setDeleteUser(sessionUser.getUserName());
			vm.setIsDeleted("1");

			cloudVmDao.merge(vm);
			
			tagService.refreshCacheAftDelRes("vm", cloudVm.getVmId());
			alarmService.deleteMonitorByResource(MonitorResourceType.VM.toString(), cloudVm.getVmId());
			volumeService.deleteVolumeByVm(cloudVm.getVmId(), sessionUser.getUserName());
			
			floatIpService.refreshFloatIpByVm(cloudVm.getVmId());
			
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
	 *
	 * 恢复云主机（从回收站）
	 * 
	 * @param cloudVm
	 * @param sessionUser
	 * @throws Exception 
	 */
	public void restoreVm(CloudVm cloudVm, SessionUserInfo sessionUser) throws Exception{
		BaseCloudVm baseCloudVm = cloudVmDao.findOne(cloudVm.getVmId());
		if(!"SOFT_DELETED".equals(baseCloudVm.getVmStatus()) || !"2".equals(baseCloudVm.getIsDeleted())){
			throw new AppException("该云主机已不在回收站中");
		}
		if(PayType.PAYAFTER.equals(baseCloudVm.getPayType())){
			MoneyAccount accountMoney = accountOverviewSerivce.getAccountInfo(sessionUser.getCusId());
			BigDecimal createResourceLimitedMoney = new BigDecimal(0);
			if (accountMoney.getMoney().compareTo(createResourceLimitedMoney) < 0) {
				throw new AppException("您的账户已欠费，请充值后操作");
			}
		}
		CloudProject project = queryPrjQuato(baseCloudVm.getPrjId());
		BaseCloudFlavor cf = cloudFlavorService.queryFlavorByFlavorId(baseCloudVm.getFlavorId());
		if(cf != null && null != project &&(
				(project.getHostCount()-project.getUsedVmCount())<1
				||(project.getCpuCount()-project.getUsedCpuCount())<cf.getFlavorVcpus()
				||(project.getMemory()-project.getUsedRam())<cf.getFlavorRam())){
			throw new AppException("您的配额已满，请申请配额");
			
		}
		openstackVmService.restorVm(cloudVm);
		BaseCloudVm vm = new BaseCloudVm();
		vm = cloudVmDao.findOne(cloudVm.getVmId());
		vm.setVmStatus("SOFT_RESUME");
		if(PayType.PAYAFTER.equals(baseCloudVm.getPayType())){
			vm.setChargeState("0");
			Customer customer = customerService.findCustomerById(sessionUser.getCusId());
			if(null != customer.getOverCreditTime()){
                boolean isBeyondRetentionTime = isBeyondRetentionTime(customer);
                if(isBeyondRetentionTime){
                    vm.setChargeState("1");
                }else{
                    vm.setChargeState("0");
                }
			}
		}
		
		cloudVmDao.merge(vm);
		
		JSONObject json = new JSONObject();
		json.put("vmId", vm.getVmId());
		json.put("dcId", vm.getDcId());
		json.put("prjId", vm.getPrjId());
		json.put("vmStatus", vm.getVmStatus());
		json.put("payType", baseCloudVm.getPayType());
		json.put("cusId", sessionUser.getCusId());
		json.put("count", "0");

		final JSONObject data = json;
		TransactionHookUtil.registAfterCommitHook(new Hook() {
			@Override
			public void execute() {
				jedisUtil.addUnique(RedisKey.vmKey, data.toJSONString());
			}
			
		});
	}

    private boolean isBeyondRetentionTime(Customer customer) {
        String recoveryTime = sysDataTreeService.getRecoveryTime();
        int retentionTime = Integer.valueOf(recoveryTime);
        Date overCreditTime = customer.getOverCreditTime();
        Date currentTime = new Date();

        if (overCreditTime != null) {
            long timeSpan = currentTime.getTime() - overCreditTime.getTime();
            return timeSpan >= (retentionTime * 60 * 60 * 1000) ? true : false;
        }
        return false;
    }
	/**
	 * 启动云主机 ------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void restartVm(CloudVm cloudVm) throws AppException {
		try {
			openstackVmService.restartVm(cloudVm);

			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			vm.setVmStatus("STARTING");

			cloudVmDao.merge(vm);

			JSONObject json = new JSONObject();
			json.put("vmId", vm.getVmId());
			json.put("dcId", vm.getDcId());
			json.put("prjId", vm.getPrjId());
			json.put("vmStatus", vm.getVmStatus());
			json.put("perStatus", "SHUTOFF");
			json.put("count", "0");
			json.put("isExsit", "0");

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
	 * 关闭云主机 ------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void shutdownVm(CloudVm cloudVm) throws AppException {
		try {
			openstackVmService.shutdownVm(cloudVm);

			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			vm.setVmStatus("SHUTOFFING");

			cloudVmDao.merge(vm);
			JSONObject json = new JSONObject();
			json.put("vmId", vm.getVmId());
			json.put("dcId", vm.getDcId());
			json.put("prjId", vm.getPrjId());
			json.put("vmStatus", vm.getVmStatus());
			json.put("perStatus", "ACTIVE");
			json.put("count", "0");
			json.put("isExsit", "0");

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
	 * <p>根据受管子网查询对应的主机</p>
	 * 
	 * @author zhouhaitao
	 * @param subnetId
	 * 				受管子网ID
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<BaseCloudVm> queryVmListBySubnet(String subnetId){
		StringBuffer hql = new StringBuffer();
		hql.append("from BaseCloudVm where subnetId = ? and isDeleted = '0' and isVisable = '1' ");
		return cloudVmDao.find(hql.toString(), new Object[]{subnetId});
	}
	
	/**
	 * 软重启云主机 ------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void softRestartVm(CloudVm cloudVm) throws AppException {
		try {
			openstackVmService.softRestartVm(cloudVm);

			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			vm.setVmStatus("REBOOT");

			cloudVmDao.merge(vm);

			JSONObject json = new JSONObject();
			json.put("vmId", vm.getVmId());
			json.put("dcId", vm.getDcId());
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

		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}
	}

	/**
	 * 硬重启云主机 ------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void hardRestartVm(CloudVm cloudVm) throws AppException {
		try {
			openstackVmService.hardRestartVm(cloudVm);

			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			vm.setVmStatus("HARD_REBOOT");

			cloudVmDao.merge(vm);

			JSONObject json = new JSONObject();
			json.put("vmId", vm.getVmId());
			json.put("dcId", vm.getDcId());
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

		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}
	}

	/**
	 * 创建自定义镜像 ------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void createSnapshot(CloudVm cloudVm, SessionUserInfo userInfo) throws AppException {
		try {
			CloudProject project = projectService.findProject(cloudVm.getPrjId());
			boolean flag = checkImageCount(cloudVm.getPrjId(), project.getImageCount());
			
			if (flag) {
				throw new AppException("最多可创建"+project.getImageCount()+"个自定义镜像，请删除不需要的镜像再操作");
			}
			Image image = openstackVmService.createSnapshot(cloudVm);
			if (image != null) {
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

				cloudImage.setImageId(image.getId());
				cloudImage.setImageName(cloudVm.getImageName());
				cloudImage.setDcId(cloudVm.getDcId());
				cloudImage.setPrjId(cloudVm.getPrjId());
				cloudImage.setDiskFormat(image.getDisk_format());
				cloudImage.setOwnerId(image.getOwner());
				cloudImage.setOsType(vm.getOsType());
				cloudImage.setSysType(vm.getSysType());
				cloudImage.setMinCpu((long) cloudVm.getCpus());
				cloudImage.setImageIspublic('2');
				cloudImage.setCreateName(userInfo.getUserName());
				cloudImage.setCreatedTime(new Date());
				cloudImage.setImageDescription(cloudVm.getImageDesc());
				cloudImage.setFromVmId(cloudVm.getVmId());
				
				String minDisk = image.getMin_disk();
				if (minDisk != null) {
					cloudImage.setMinDisk(Long.valueOf(minDisk));
				} else {
					cloudImage.setMinDisk(null);
				}
				String minRam = image.getMin_ram();
				if (minDisk != null) {
					cloudImage.setMinRam(Long.valueOf(minRam));
				} else {
					cloudImage.setMinRam(null);
				}
				if (!StringUtils.isEmpty(image.getStatus())) {
					cloudImage.setImageStatus(image.getStatus().toUpperCase());
				}
				if (image.getSize() != null) {
					cloudImage.setImageSize(new BigDecimal(image.getSize()));
				} else {
					cloudImage.setImageSize(null);
				}
				if (image.getContainer_format() != null) {
					cloudImage.setContainerFormat(image.getContainer_format());
				} else {
					cloudImage.setContainerFormat(null);
				}

				cloudImageDao.save(cloudImage);

				JSONObject json = new JSONObject();
				json.put("imageId", cloudImage.getImageId());
				json.put("dcId", cloudImage.getDcId());
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
			}

		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}
	}

	/**
	 * 调整云主机大小
	 * ------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * @throws Exception 
	 */
	@Transactional(noRollbackFor=AppException.class)
	public void resizeVm(CloudVm cloudVm) throws Exception {
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
			
			//获取系统盘
			CloudVolume cloudVolume=volumeService.getSysVolumeByVmId(cloudVm.getVmId());
			

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
			json.put("volType", cloudVolume.getVolType());
			json.put("disks", cloudVm.getDisks());
			json.put("sysType", cloudVm.getSysType());
			json.put("endTime", cloudVm.getEndTime());
			json.put("count", "0");
			json.put("isExsit", "1");
			json.put("fromImageId",cloudVm.getFromImageId());

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
		} catch (Exception e) {
			upgradeFailHandler(cloudVm);
			log.error(e.getMessage(),e);
			throw new AppException("error.openstack.message");
		}
	}

	/**
	 * 云主机升级失败处理
	 * 
	 * @param cloudVm
	 * @throws Exception 
	 */
	public void upgradeFailHandler(CloudVm cloudVm) throws Exception{
		orderService.completeOrder(cloudVm.getOrderNo(), false, null);
		
		messageCenterService.addResourFailMessage(cloudVm.getOrderNo(), cloudVm.getCusId());
	}
	
	/**
	 * 云主机升级成功处理
	 * 
	 * @param cloudVm
	 * @throws Exception 
	 */
	public void upgradeSuccessHandler(CloudVm cloudVm) throws Exception{
		List<BaseOrderResource> orderResources = new ArrayList<BaseOrderResource>();
		BaseOrderResource orderResource = new BaseOrderResource();
		orderResource.setResourceId(cloudVm.getVmId());
		orderResource.setResourceName(cloudVm.getVmName());
		orderResource.setOrderNo(cloudVm.getOrderNo());
		orderResources.add(orderResource);
		
		BaseOrder order = orderService.completeOrder(cloudVm.getOrderNo(), true, orderResources,false,cloudVm.getEndTime());
		cloudVm.setOpDate(order.getCompleteTime());
		
		if(PayType.PAYAFTER.equals(cloudVm.getPayType())){
			vmUpgradeCharge(cloudVm);
		}
	}
	
	
	/**
	 * 确认调整云主机 ------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void confirmResizeVm(CloudVm cloudVm) throws AppException {
		try {
			openstackVmService.confirmResizeVm(cloudVm);

			BaseCloudVm vm = new BaseCloudVm();

			vm = cloudVmDao.findOne(cloudVm.getVmId());

			vm.setVmStatus("RESIZED");
			vm.setFlavorId(vm.getResizeId());
			vm.setResizeId(null);

			cloudVmDao.merge(vm);

			JSONObject json = new JSONObject();
			json.put("vmId", vm.getVmId());
			json.put("dcId", vm.getDcId());
			json.put("prjId", vm.getPrjId());
			json.put("vmStatus", vm.getVmStatus());
			json.put("perStatus", "VERIFY_RESIZE");
			json.put("count", "0");
			json.put("isExsit", "0");

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
	 * 取消调整云主机 ------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void revertResizeVm(CloudVm cloudVm) throws AppException {
		try {
			openstackVmService.revertResizeVm(cloudVm);

			BaseCloudVm vm = new BaseCloudVm();

			vm = cloudVmDao.findOne(cloudVm.getVmId());

			vm.setVmStatus("REVERT_RESIZE");
			vm.setResizeId(null);

			cloudVmDao.merge(vm);

			JSONObject json = new JSONObject();
			json.put("vmId", vm.getVmId());
			json.put("dcId", vm.getDcId());
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

		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}
	}

	/**
	 * 打开云主机控制台 ------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public String consoleVm(CloudVm cloudVm) throws AppException {
		String url = "";
		try {
			url = openstackVmService.consoleVm(cloudVm);
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}
		return url;
	}

	/**
	 * 获取云主机日志 ------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public String getVmLogs(CloudVm cloudVm) throws AppException {
		String logs = null;
		try {
			logs = openstackVmService.getVmLogs(cloudVm);
			if (!StringUtils.isEmpty(logs)) {
				logs = logs.replaceAll("\n", "<br>");
			}
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}

		return logs;

	}

	/**
	 * 编辑云主机安全组 ------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void editVmSecurityGroup(CloudVm cloudVm) throws AppException {
		List<BaseCloudSecurityGroup> adds = new ArrayList<BaseCloudSecurityGroup>();
		List<BaseCloudSecurityGroup> dels = new ArrayList<BaseCloudSecurityGroup>();
		Map<String, BaseCloudSecurityGroup> oriMap = new HashMap<String, BaseCloudSecurityGroup>();
		Map<String, BaseCloudSecurityGroup> map = new HashMap<String, BaseCloudSecurityGroup>();
		try {
			List<BaseCloudSecurityGroup> oriGroups = getSecurityGroupByVm(cloudVm.getVmId());
			BaseCloudSecurityGroup[] groups = cloudVm.getBcsgs();
			if (null != oriGroups && oriGroups.size() > 0) {
				for (BaseCloudSecurityGroup bcsp : oriGroups) {
					oriMap.put(bcsp.getSgId(), bcsp);
				}
			}

			if (null != groups && groups.length > 0) {
				for (BaseCloudSecurityGroup bcsp : groups) {
					if (!oriMap.containsKey(bcsp.getSgId())) {
						adds.add(bcsp);
					}

					map.put(bcsp.getSgId(), bcsp);
				}
			}
			if (null != oriGroups && oriGroups.size() > 0) {
				for (BaseCloudSecurityGroup bcsp : oriGroups) {
					if (!map.containsKey(bcsp.getSgId())) {
						dels.add(bcsp);
					}
				}
			}

			openstackVmService.editVmSecurityGroup(cloudVm, adds, dels);

			vmSgService.deleteByVmId(cloudVm.getVmId());

			if (null != groups && groups.length > 0) {
				for (BaseCloudSecurityGroup bsp : groups) {
					BaseCloudVmSgroup vsg = new BaseCloudVmSgroup();
					vsg.setSgId(bsp.getSgId());
					vsg.setVmId(cloudVm.getVmId());

					vmSgService.merge(vsg);
				}
			}

		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}
	}

	@Override
	public Page listDisk(Page page, Object object, String string, Object object2, QueryMap queryMap) {
		return null;
	}

	/**
	 * 查询云主机创建的镜像来源 --------------
	 * 
	 * @author zhouhaitao
	 * @return
	 * 
	 */
	public List<SysDataTree> getOsList() {
		List<SysDataTree> sysList = new ArrayList<SysDataTree>();
		StringBuffer sql = new StringBuffer();
		
		sql.append("	SELECT                          ");
		sql.append("		node_id,                    ");
		sql.append("		node_name,                  ");
		sql.append("		parent_id,                   ");
		sql.append("		icon                   ");
		sql.append("	FROM                            ");
		sql.append("		sys_data_tree               ");
		sql.append("	WHERE                           ");
		sql.append("	   node_id = ?  ");
		sql.append("	OR	parent_id = ? ");
		
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[]{ConstantClazz.DICT_CLOUD_OS_WINDOWS_NODE_ID,ConstantClazz.DICT_CLOUD_OS_LINUX_NODE_ID});
		@SuppressWarnings("rawtypes")
		List result = query.getResultList();
		if (result != null && result.size() > 0) {
			for (int i = 0; i < result.size(); i++) {
				int index = 0;
				Object[] objs = (Object[]) result.get(i);
				SysDataTree sdt = new SysDataTree();
				sdt.setNodeId(String.valueOf(objs[index++]));
				sdt.setNodeName(String.valueOf(objs[index++]));
				sdt.setParentId(String.valueOf(objs[index++]));
				sdt.setIcon(String.valueOf(objs[index++]));
				
				sysList.add(sdt);
			}
		}
		
		return sysList;
		
	}

	/**
	 * 查询sys_data_tree表中的数据根据 parentId --------------
	 * 
	 * @author zhouhaitao
	 * @return
	 * 
	 */
	public List<SysDataTree> getSysDataListByParentId(String parentId) {
		return DictUtil.getDataTreeByParentId(parentId);
	}

	/**
	 * 查询当前用户创建的项目及已使用情况信息 -----------------
	 * 
	 * @author zhouhaitao
	 * @param sessionUser
	 *            当前登录用户
	 * @return
	 * 
	 */
	public List<CloudProject> getProListByCustomer(SessionUserInfo sessionUser) {
		List<CloudProject> cloudProjectList = new ArrayList<CloudProject>();
		StringBuffer sql = new StringBuffer();
		Object[] args = new Object[3];
		int index = 0;

		sql.append(" select   ");
		sql.append(" cp.prj_id as prjId , ");
		sql.append(" cp.prj_name as prjName,  ");
		sql.append(" cp.dc_id as dcId,  ");
		sql.append(" cp.cpu_count as cpuCount,  ");
		sql.append(" cp.host_count as hostCount,  ");
		sql.append(" cp.memory as memory,  ");
		sql.append(" cp.disk_capacity as diskCapacity,  ");
		sql.append(" v.usedVmCount, ");
		sql.append(" v.usedCpuCount,  ");
		sql.append(" v.usedRam,  ");
		sql.append(" vol.usedVolumeCount , ");
		sql.append(" vol.usedDiskCapacity , ");
		sql.append(" snap.usedSnapshotCapacity , ");
		sql.append(" cp.disk_count as volCount ");

		sql.append(" from  ");
		if (sessionUser.getIsAdmin()) {
			sql.append(" cloud_project ");
		} else {
			sql.append(" ( ");
			sql.append(" select  ");
			sql.append("  	s.project_id as prj_id,");
			sql.append("  	p.dc_id ,");
			sql.append("  	p.customer_id ,");
			sql.append("  	p.prj_name ,");
			sql.append("  	p.cpu_count ,");
			sql.append("  	p.host_count ,");
			sql.append("  	p.disk_count ,");
			sql.append("  	p.memory ,");
			sql.append("  	p.disk_capacity ");
			sql.append(" from sys_selfuserprj s ");
			sql.append(" left join cloud_project p ");
			sql.append(" on s.project_id=p.prj_id ");
			sql.append(" where 1=1 ");
			sql.append(" and s.user_id = ? ");
			sql.append(" )");
			args[index++] = sessionUser.getUserId();
		}
		sql.append(" cp");
		sql.append(" left join   ");
		sql.append(" (  ");
		sql.append("  	select   ");
		sql.append("   	vm.prj_id,");
		sql.append("   	count(vm.vm_id) as usedVmCount,");
		sql.append("    sum(cf.flavor_vcpus) as usedCpuCount,");
		sql.append("    sum(cf.flavor_ram) as usedRam ");
		sql.append("    from ");
		sql.append(" 	( ");
		sql.append("  		select");
		sql.append("  			cv.vm_id ,");
		sql.append("  			cv.dc_id ,");
		sql.append("  		 	cv.prj_id ,");
		sql.append("  		 	cv.is_deleted ,");
		sql.append(
				"  			case when cv.resize_id is not null then cv.resize_id else cv.flavor_id end as flavor_id ");
		sql.append("  		from cloud_vm cv ");
		sql.append(" 	) vm ");
		sql.append("    LEFT JOIN cloud_flavor cf");
		sql.append("   	ON vm.flavor_id = cf.flavor_id ");
		sql.append("   	and vm.dc_id = cf.dc_id	  ");
		sql.append("    where vm.is_deleted = '0' ");
		sql.append("    GROUP BY vm.prj_id    ");
		sql.append(" ) v ");
		sql.append(" on cp.prj_id = v.prj_id");
		sql.append(" left join   ");
		sql.append(" (  ");
		sql.append("    select ");
		sql.append("    vo.prj_id,");
		sql.append("    count(vo.vol_id) as usedVolumeCount,");
		sql.append("    sum(vo.vol_size) as usedDiskCapacity ");
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

		sql.append(" where cp.customer_id = ? ");
		args[index++] = sessionUser.getCusId();

		Object[] params = new Object[index];
		System.arraycopy(args, 0, params, 0, index);
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), params);
		@SuppressWarnings("rawtypes")
		List listResult = query.getResultList();
		for (int i = 0; i < listResult.size(); i++) {
			Object[] obj = (Object[]) listResult.get(i);
			CloudProject cloudProject = new CloudProject();
			cloudProject.setProjectId(String.valueOf(obj[0]));
			cloudProject.setPrjName(String.valueOf(obj[1]));
			cloudProject.setDcId(String.valueOf(obj[2]));
			cloudProject.setCpuCount(Integer.parseInt(String.valueOf(obj[3]) == null ? "0" : String.valueOf(obj[3])));
			cloudProject.setHostCount(Integer.parseInt(String.valueOf(obj[4]) == null ? "0" : String.valueOf(obj[4])));
			cloudProject.setMemory(1024 * Integer.parseInt(String.valueOf(obj[5]) == null ? "0" : String.valueOf(obj[5])));
			cloudProject.setDiskCapacity(Integer.parseInt(obj[6] == null ? "0" : String.valueOf(obj[6])));
			cloudProject.setUsedVmCount(Integer.parseInt(obj[7] == null ? "0" : String.valueOf(obj[7])));
			cloudProject.setUsedCpuCount(Integer.parseInt(obj[8] == null ? "0" : String.valueOf(obj[8])));
			cloudProject.setUsedRam(Integer.parseInt(obj[9] == null ? "0" : String.valueOf(obj[9])));
			cloudProject.setDiskCountUse(Integer.parseInt(obj[10] == null ? "0" : String.valueOf(obj[10])));
			cloudProject.setUsedDiskCapacity(Integer.parseInt(obj[11] == null ? "0" : String.valueOf(obj[11])));
			cloudProject.setUsedSnapshotCapacity(Integer.parseInt(obj[12] == null ? "0" : String.valueOf(obj[12])));
			cloudProject.setDiskCount(Integer.parseInt(obj[13] == null ? "0" : String.valueOf(obj[13])));
			cloudProject
					.setUsedDataCapacity(cloudProject.getUsedDiskCapacity() + cloudProject.getUsedSnapshotCapacity());
			cloudProject.setUsedVmPrecent(100 * cloudProject.getUsedVmCount() / cloudProject.getHostCount());
			cloudProject.setUsedCpuPrecent(100 * cloudProject.getUsedCpuCount() / cloudProject.getCpuCount());
			cloudProject.setUsedRamPrecent((int) (100 * cloudProject.getUsedRam() / cloudProject.getMemory()));
			cloudProject.setUsedDataDiskPrecent(
					(int) (100 * cloudProject.getUsedDataCapacity() / cloudProject.getDiskCapacity()));

			cloudProjectList.add(cloudProject);
		}

		return cloudProjectList;
	}

	/**
	 * <p>校验云主机在同数据中心下是否重名 </p>
	 * --------------------------
	 * 
	 * <p>1.查询cloud_vm表中是否存在主机名称</p>
	 * 
	 * <p>2.查询cloudorder_vm表中是否存在主机名称</p>
	 * 
	 * <p>3.查询cloud_vm处于回收站是否存在主机名称</p>
	 * 
	 * 若 满足 上述 1,2,3都不存在则返回 true;否则 返回 false;
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 *            请求参数
	 * @return
	 */
	public boolean checkVmExistByName(CloudVm cloudVm) {
		boolean flag = false;
		String[] nameList = null;

		int num = cloudVm.getNumber();
		if (num != 0) {
			nameList = new String[num];
		}
		if (num == 1) {
			nameList[0] = cloudVm.getVmName();
		} else if (num > 1) {
			for (int i = 1; i <= num; i++) {
				nameList[i - 1] = cloudVm.getVmName() + "_" + i;
			}
		}

		StringBuffer hql = new StringBuffer();
		hql.append(" SELECT count(*)  ");
		hql.append(" from  ");
		hql.append(" BaseCloudVm v where 1=1 ");
		hql.append(" and v.prjId = :prjId ");
		hql.append(" and v.isDeleted in ('0','2') ");
		hql.append(" and v.isVisable = '1' ");
		hql.append(" and binary(v.vmName) in (:names)");
		if (!StringUtils.isEmpty(cloudVm.getVmId())) {
			hql.append(" and v.vmId <> :vmId");
		}
		Query query = cloudVmDao.createQuery(hql.toString());
		query.setParameter("prjId", cloudVm.getPrjId());
		query.setParameterList("names", nameList);
		if (!StringUtils.isEmpty(cloudVm.getVmId())) {
			query.setParameter("vmId", cloudVm.getVmId());
		}
		int vmCount = Integer.parseInt(String.valueOf(query.uniqueResult()));
		flag = vmCount == 0;
		if(flag){
			boolean isContains = false;
			int suffixNum = 0;
			String perfixName = "";
			String regex = "";
			String vmName = cloudVm.getVmName();
			int _index = vmName.lastIndexOf('_');
			if(_index != -1){
				perfixName = vmName.substring(0, _index);
				String suffixName = vmName.substring(_index+1);
				Pattern pattern = Pattern.compile("[1-9]|1[0-9]|20"); 
				Matcher isNum = pattern.matcher(suffixName);
				if(isNum.matches()){
					isContains = true;
					suffixNum = Integer.parseInt(suffixName);
				}
			}
			if(cloudVm.getNumber()>1){
				regex = "^"+vmName + "_";
				if(cloudVm.getNumber() > 1 && cloudVm.getNumber()<=9){
					regex = regex + "([1-"+cloudVm.getNumber()+"])$";
				}
				else if(cloudVm.getNumber()== 10){
					regex = regex + "([1-9]|10)$";
				}
				else if (cloudVm.getNumber()>10 && cloudVm.getNumber()<=19){
					regex = regex + "([1-9]|1[0-"+(cloudVm.getNumber()-10)+"])$";
				}
				else{
					regex = regex + "([1-9]|1[0-9]|20)$";
				}
			}
			StringBuffer orderVmHql = new StringBuffer();
			orderVmHql.append("	SELECT                                                 ");
			orderVmHql.append("		count(cov.ordervm_id)                              ");
			orderVmHql.append("	FROM                                                   ");
			orderVmHql.append("		cloudorder_vm cov                                  ");
			orderVmHql.append("	LEFT JOIN order_info oi ON oi.order_no = cov.order_no  ");
			orderVmHql.append("	WHERE                                                  ");
			orderVmHql.append("		1=1                                                ");
			orderVmHql.append("	AND cov.order_type = '0'                               ");
			orderVmHql.append("	AND oi.order_state in ('1','2')                        ");
			orderVmHql.append("	AND cov.prj_id = ?                                     ");
			if(cloudVm.getNumber() == 1){
				orderVmHql.append("AND ( ( binary(cov.vm_name) = '"+vmName+"' and count = 1) ");
				if(isContains){
					orderVmHql.append(" or ( binary(cov.vm_name) = '"+perfixName+"' and count >= "+suffixNum+" ) )");
				}
				else{
					orderVmHql.append(")");
				}
			}
			else if(cloudVm.getNumber() > 1){
				orderVmHql.append("AND ( ( binary(cov.vm_name) = '"+vmName+"' and count > 1) ");
				orderVmHql.append(" or ( binary(cov.vm_name) REGEXP '"+regex+"' and count = 1)	 ");
				orderVmHql.append(" ) ");
			}
			javax.persistence.Query orderVmQuery = cloudVmDao.createSQLNativeQuery(orderVmHql.toString(),new Object[]{cloudVm.getPrjId()});
			Object obj = orderVmQuery.getSingleResult();
			int objInt = Integer.parseInt(String.valueOf(obj));
			if(objInt >0){
				flag = false;
			}
		}
		
		return flag;
	}

	/**
	 * 获取CPU配置信息列表 ------------------
	 * 
	 * @author zhouhaitao
	 * @return
	 */
	public List<SysDataTree> getCpuList() {
		return getSysDataListByParentId(ConstantClazz.DICT_CLOUD_CPU_TYPE_NODE_ID);
	}

	/**
	 * 根据CPU获取内存配置信息列表 ------------------
	 * 
	 * @author zhouhaitao
	 * @param cpuId
	 * @return
	 */
	public List<SysDataTree> getRamListByCpu(String cpuId) {
		return getSysDataListByParentId(cpuId);
	}

	/**
	 * 根据系统类型获取操作系统列表 ------------------
	 * 
	 * @author zhouhaitao
	 * @param osId
	 * @return
	 */
	public List<SysDataTree> getSysTypeList(String osId) {
		return getSysDataListByParentId(osId);
	}
	
	/**
	 * 获取市场镜像的业务类型信息列表 <br>
	 * ------------------
	 * 
	 * @author zhouhaitao
	 * @return
	 */
	public List<SysDataTree> getMarketImageTypeList(){
		return getSysDataListByParentId(ConstantClazz.DICT_CLOUD_IMAGE_MARKETIMAGE__NODE_ID);
	}

	/**
	 * 获取项目下的镜像
	 * ------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * @return
	 */
	public List<CloudImage> getImageList(CloudVm cloudVm) {
		List<CloudImage> imageList = new ArrayList<CloudImage>();
		Object[] args = new Object[8];
		int index = 0;
		
		StringBuffer sql = new StringBuffer();
		sql.append("	SELECT                          ");
		sql.append("		ci.image_id,                ");
		sql.append("		ci.image_name,              ");
		sql.append("		ci.image_ispublic,          ");
		sql.append("		ci.min_cpu,                 ");
		sql.append("		ci.min_ram,                 ");
		sql.append("		ci.max_cpu,                 ");
		sql.append("		ci.max_ram,                 ");
		sql.append("		ci.os_type,                 ");
		sql.append("		ci.sys_type,                ");
		sql.append("		ci.sysdisk_size,            ");
		sql.append("		ci.min_disk,                ");
		sql.append("		img.image_id as sourceId  , ");
		sql.append("		img.image_ispublic as sourceType ");
		sql.append("	FROM                            ");
		sql.append("		cloud_image ci              ");
		sql.append("	LEFT JOIN cloud_image img       ");
		sql.append("	ON ci.source_id = img.image_id  ");
		sql.append("	AND ci.image_ispublic = '2'     ");
		sql.append("	WHERE                           ");
		sql.append("	ci.image_status =  'ACTIVE'  ");
		sql.append("	AND ci.image_ispublic = ?       ");
		args[index++] = cloudVm.getVmFrom();
		if ("1".equals(cloudVm.getVmFrom())) {
			sql.append("	AND ci.is_use = '1'       ");
			sql.append("	AND ci.dc_id =   ?          ");
			args[index++] = cloudVm.getDcId();
			if(!StringUtils.isEmpty(cloudVm.getSysType())){
				sql.append("	AND ci.sys_type =  ?        ");
				args[index++] = cloudVm.getSysType();
			}
			if(!StringUtils.isEmpty(cloudVm.getOsType())){
				sql.append("	AND	ci.os_type =  ?             ");
				args[index++] = cloudVm.getOsType();
			}
		}
		if ("2".equals(cloudVm.getVmFrom())) {
			sql.append("	AND ci.prj_id = ?           ");
			args[index++] = cloudVm.getPrjId();
			if(!StringUtil.isEmpty(cloudVm.getSourceType())){
				sql.append("	AND img.image_ispublic = ?           ");
				args[index++] = cloudVm.getSourceType();
			}
		}
		if ("3".equals(cloudVm.getVmFrom())) {
			sql.append("	AND ci.is_use = '1'       ");
			sql.append("	AND ci.dc_id =   ?          ");
			args[index++] = cloudVm.getDcId();
			
			if(!StringUtil.isEmpty(cloudVm.getProfessionType())){
				sql.append("	AND ci.profession_type =   ?          ");
				args[index++] = cloudVm.getProfessionType();
			}
		}
		
		Object[] params = new Object[index];
		System.arraycopy(args, 0, params, 0, index);
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), params);
		@SuppressWarnings("rawtypes")
		List listResult = query.getResultList();
		for (int i = 0; i < listResult.size(); i++) {
			Object[] obj = (Object[]) listResult.get(i);
			int ind = 0;
			CloudImage image = new CloudImage();
			
			image.setImageId(String.valueOf(obj[ind++]));
			image.setImageName(String.valueOf(obj[ind++]));
			image.setImageIspublic(String.valueOf(obj[ind++]).charAt(0));
			image.setMinCpu(Long.parseLong(obj[ind++] == null ? "0" : String.valueOf(obj[ind-1])));
			image.setMinRam(Long.parseLong(obj[ind++] == null ? "0" : String.valueOf(obj[ind-1])));
			image.setMaxCpu(Integer.parseInt(obj[ind++] == null ? "0" : String.valueOf(obj[ind-1])));
			image.setMaxRam(Integer.parseInt(obj[ind++] == null ? "0" : String.valueOf(obj[ind-1])));
			image.setOsType(String.valueOf(obj[ind++]));
			image.setSysType(String.valueOf(obj[ind++]));
			image.setSysdiskSize(Long.parseLong(obj[ind++] == null ? "0" : String.valueOf(obj[ind-1])));
			image.setMinDisk(Long.parseLong(obj[ind++] == null ? "0" : String.valueOf(obj[ind-1])));
			image.setSourceId(obj[ind++] == null ? "" : String.valueOf(obj[ind-1]));
			image.setSourceType(obj[ind++] == null ? "" : String.valueOf(obj[ind-1]));
			
			imageList.add(image);
		}

		return imageList;

	}

	/**
	 * 获取项目下的子网列表 ------------------
	 * 
	 * @author zhouhaitao
	 * @param prjId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<BaseCloudSubNetWork> getSubNetList(String prjId) {
		StringBuffer hql = new StringBuffer();

		hql.append(" from BaseCloudSubNetWork ");
		hql.append(" where 1=1  ");
		hql.append(" and prjId = ? ");

		return cloudWorkDao.find(hql.toString(), new Object[] { prjId });
	}

	/**
	 * 根据项目Id查询云主机列表
	 * 
	 * @param prjId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<BaseCloudVm> queryVmListByPrjId(String prjId) {
		List<BaseCloudVm> vmList = null;
		StringBuffer hql = new StringBuffer();
		hql.append(" from BaseCloudVm ");
		hql.append(" where prjId = ? and isDeleted = '0' and vmStatus in ('SHUTOFF','ACTIVE','SUSPENDED')   ");

		vmList = cloudVmDao.find(hql.toString(), new Object[] { prjId });
		return vmList;
	}

	/**
	 * 查询云主机状态列表 ------------------
	 * 
	 * @author zhouhaitao
	 * 
	 * @return
	 */
	public List<SysDataTree> getVmStatusList() {
		return getSysDataListByParentId(ConstantClazz.DICT_CLOUD_VMSTAUS_TYPE_NODE_ID);
	}

	/**
	 * 查询操作系统型号列表 ------------------
	 * 
	 * @author zhouhaitao
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<SysDataTree> getVmSysList() {
		StringBuffer hql = new StringBuffer();
		hql.append(" from  BaseSysDataTree ");
		hql.append(" where nodeId like ?  ");
		hql.append(" and length(nodeId) =16  ");
		hql.append(" and nodeId <> ? ");

		return cloudVmDao.find(hql.toString(), new Object[] { ConstantClazz.DICT_CLOUD_SYS_TYPE_NODE_ID + "%",
				ConstantClazz.DICT_CLOUD_QTSYS_NODE_ID });
	}

	/**
	 * 查询项目下的未关联云主机的安全组信息 ------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public List<BaseCloudSecurityGroup> getSecurityGroupByPrj(CloudVm cloudVm) {
		List<BaseCloudSecurityGroup> list = new ArrayList<BaseCloudSecurityGroup>();
		StringBuffer sql = new StringBuffer();
		sql.append(" select sg_id,sg_name from  ");
		sql.append(" cloud_securitygroup ");
		sql.append(" where prj_id = ? ");
		sql.append(" and  sg_id not in ");
		sql.append("  	( ");
		sql.append("  		select sg_id from cloud_vmsecuritygroup");
		sql.append("  		where vm_id = ? ");
		sql.append("  	)");

		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(),
				new Object[] { cloudVm.getPrjId(), cloudVm.getVmId() });
		@SuppressWarnings("rawtypes")
		List listResult = query.getResultList();
		for (int i = 0; i < listResult.size(); i++) {
			Object[] obj = (Object[]) listResult.get(i);
			BaseCloudSecurityGroup cloudSg = new BaseCloudSecurityGroup();
			cloudSg.setSgId(String.valueOf(obj[0]));
			cloudSg.setSgName(String.valueOf(obj[1]));
			if("default".equals(cloudSg.getSgName())){
				cloudSg.setSgName("默认安全组");
			}

			list.add(cloudSg);
		}
		return list;
	}

	/**
	 * 查询项目下的已关联云主机的安全组信息 ------------------
	 * 
	 * @author zhouhaitao
	 * @param vmId
	 * 
	 * @throws AppException
	 */
	public List<BaseCloudSecurityGroup> getSecurityGroupByVm(String vmId) {
		List<BaseCloudSecurityGroup> list = new ArrayList<BaseCloudSecurityGroup>();
		StringBuffer sql = new StringBuffer();
		sql.append(" select vsg.sg_id,sg.sg_name from  ");
		sql.append(" cloud_vmsecuritygroup vsg ");
		sql.append(" left join cloud_securitygroup sg ");
		sql.append(" on vsg.sg_id = sg.sg_id ");
		sql.append(" where vsg.vm_id = ? ");

		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[] { vmId });
		@SuppressWarnings("rawtypes")
		List listResult = query.getResultList();
		for (int i = 0; i < listResult.size(); i++) {
			Object[] obj = (Object[]) listResult.get(i);
			BaseCloudSecurityGroup cloudSg = new BaseCloudSecurityGroup();
			cloudSg.setSgId(String.valueOf(obj[0]));
			cloudSg.setSgName(String.valueOf(obj[1]));
			if("default".equals(cloudSg.getSgName())){
				cloudSg.setSgName("默认安全组");
			}
			
			list.add(cloudSg);
		}
		return list;
	}

	/**
	 * 查询项目下的云主机配额信息和已使用量统计 
	 * ----------------------------------
	 * 
	 * @author zhouhaitao
	 * 
	 * @param prjId
	 *            项目ID
	 * @return 项目配额及使用情况信息
	 */
	public CloudProject queryPrjQuato(String prjId) {
		CloudProject project = new CloudProject();
		StringBuffer sql = new StringBuffer();

		sql.append("			SELECT                                                          	   		");
		sql.append("				cp.prj_id,                                                    	   		");
		sql.append("				cp.host_count,                                                	   		");
		sql.append("				cp.cpu_count,                                                 	   		");
		sql.append("				cp.memory,                                                    	   		");
		sql.append("				cp.disk_count,                                                	   		");
		sql.append("				cp.disk_capacity,                                             	   		");
		sql.append("				cp.outerip,                                                   	   		");
		sql.append("				vm.usedHostCount,                                             	   		");
		sql.append("				vm.usedCpu,                                                   	   		");
		sql.append("				vm.usedRam,                                                   	   		");
		sql.append("				vol.usedVolumeCount as usedVolumeCount1,                        		");
		sql.append("				vol.usedVolumeCapacity as usedVolumeCapacity1,                 	   		");
		sql.append("				floatip.usedFloatipCount as usedFloatipCount1,                       	");
		sql.append("				ordervm.usedHostCount as usedHostCount1,                            	");
		sql.append("				ordervm.usedCpu as usedCpu2,                                     		");
		sql.append("				ordervm.usedRam as usedRam2,                                   	   		");
		sql.append("				ordervm.usedDisk as usedDisk2,                                   	   	");
		sql.append("				ordervol.usedVolumeCount as usedVolumeCount2,                        	");
		sql.append("				ordervol.usedVolumeCapacity as usedVolumeCapacity,               		");
		sql.append("				orderfloatip.usedFloatipCount as     usedFloatipCount                	");
		sql.append("			FROM                                                            	   		");
		sql.append("				cloud_project cp                                              	   		");
		sql.append("			LEFT JOIN (                                                     	   		");
		sql.append("				SELECT                                                        	   		");
		sql.append("					vm.prj_id,                                                  	  	");
		sql.append("					count(1) AS usedHostCount,                                   		");
		sql.append("					sum(flavor.flavor_vcpus) AS usedCpu,                       	   		");
		sql.append("					sum(flavor.flavor_ram) AS usedRam                          	   		");
		sql.append("				FROM                                                          	   		");
		sql.append("					cloud_vm vm                                                         ");
		sql.append("				LEFT JOIN cloud_flavor flavor ON flavor.flavor_id = vm.flavor_id 		");
		sql.append("				AND flavor.dc_id = vm.dc_id                                   	   		");
		sql.append("				WHERE                                                         	   		");
		sql.append("					vm.prj_id = ?                                               	  	");
		sql.append("				AND vm.is_deleted = '0'                                       	   		");
		sql.append("				AND vm.is_visable = '1'                                       	   		");
		sql.append("			) vm ON cp.prj_id = vm.prj_id                                   	   		");
		sql.append("			LEFT JOIN (                                                     	   		");
		sql.append("				SELECT                                                        	   		");
		sql.append("					ordervm.prj_id,                                             		");
		sql.append("					sum(ordervm.count) AS usedHostCount,                     	   		");
		sql.append("					sum(ordervm.cpu) AS usedCpu,                                        ");
		sql.append("					sum(ordervm.ram) AS usedRam,                    		            ");
		sql.append("					sum(ordervm.disk) AS usedDisk                  	   	                ");
		sql.append("				FROM	(                                                      	   		");
		sql.append("			SELECT                                                                      ");
		sql.append("				cov.prj_id,                                                             ");
		sql.append("				cov.order_no,                                                           ");
		sql.append("				cov.order_type,                                                         ");
		sql.append("				CASE cov.order_type                                                     ");
		sql.append("			WHEN '2' THEN                                                               ");
		sql.append("				0                                                                       ");
		sql.append("			ELSE                                                                        ");
		sql.append("				cov.count                                                               ");
		sql.append("			END AS count,                                                               ");
		sql.append("				CASE cov.order_type                                                     ");
		sql.append("			WHEN '2' THEN                                                               ");
		sql.append("				(cov.cpu - cf.flavor_vcpus)                                             ");
		sql.append("			ELSE                                                                        ");
		sql.append("				cov.count*cov.cpu                                                       ");
		sql.append("			END AS cpu,                                                                 ");
		sql.append("			 CASE cov.order_type                                                        ");
		sql.append("			WHEN '2' THEN                                                               ");
		sql.append("				(cov.ram - cf.flavor_ram)                                               ");
		sql.append("			ELSE                                                                        ");
		sql.append("				cov.count*cov.ram                                                       ");
		sql.append("			END AS ram,                                                                 ");
		sql.append("			 CASE cov.order_type                                                        ");
		sql.append("			WHEN '2' THEN                                                               ");
		sql.append("				0                                                                       ");
		sql.append("			ELSE                                                                        ");
		sql.append("				cov.count*cov.disk                                                      ");
		sql.append("			END AS disk                                                                 ");
		sql.append("			FROM                                                                        ");
		sql.append("				cloudorder_vm cov                                                       ");
		sql.append("			LEFT JOIN cloud_vm vm ON cov.vm_id = vm.vm_id                               ");
		sql.append("			LEFT JOIN cloud_flavor cf ON vm.flavor_id = cf.flavor_id                    ");
		sql.append("					)ordervm                                     	   		            ");
		sql.append("				LEFT JOIN order_info info ON info.order_no = ordervm.order_no 	   		");
		sql.append("				WHERE                                                       	   		");
		sql.append("					ordervm.prj_id = ?                                         	   		");
		sql.append("				AND (                                                         	   		");
		sql.append("					info.order_state = '1'                                        		");
		sql.append("					OR info.order_state = '2'                                   		");
		sql.append("				)                                                             	   		");
		sql.append("				AND (                                                         	   		");
		sql.append("					ordervm.order_type = '0'                                       		");
		sql.append("					OR ordervm.order_type = '2'                                   		");
		sql.append("				)                                                             	   		");
		sql.append("			) ordervm ON ordervm.prj_id = cp.prj_id                         	   		");
		sql.append("			LEFT JOIN (                                                     	   		");
		sql.append("				SELECT                                                        	   		");
		sql.append("					cfl.prj_id,                                                 	  	");
		sql.append("					count(1) usedFloatipCount                                   	  	");
		sql.append("				FROM                                                          	   		");
		sql.append("					cloud_floatip cfl                                           	  	");
		sql.append("				WHERE                                                         	   		");
		sql.append("					cfl.is_visable = '1'                                         		");
		sql.append("				AND cfl.is_deleted = '0'                                      	   		");
		sql.append("				AND cfl.prj_id = ?                                           	   		");
		sql.append("			) floatip ON floatip.prj_id = cp.prj_id                         	   		");
		sql.append("			LEFT JOIN (                                                     	   		");
		sql.append("				SELECT                                                        	   		");
		sql.append("					cof.prj_id,                                                 	  	");
		sql.append("					sum(cof.product_count) AS usedFloatipCount                  	   	");
		sql.append("				FROM                                                          	   		");
		sql.append("					cloudorder_floatip cof                                      	   	");
		sql.append("				LEFT JOIN order_info info ON info.order_no = cof.order_no     	   		");
		sql.append("				WHERE                                                         	   		");
		sql.append("					cof.prj_id = ?                                               	   	");
		sql.append("				AND (                                                         	   		");
		sql.append("					info.order_state = '1'                                      	   	");
		sql.append("					OR info.order_state = '2'                                   	   	");
		sql.append("				)                                                             	   		");
		sql.append("				AND (                                                         	   		");
		sql.append("					cof.order_type = '0'                                        		");
		sql.append("					OR cof.order_type = '2'                                     		");
		sql.append("				)                                                             	   		");
		sql.append("			) orderfloatip ON orderfloatip.prj_id = cp.prj_id               	   		");
		sql.append("			LEFT JOIN (                                                     	   		");
		sql.append("				SELECT                                                        	   		");
		sql.append("					cvol.prj_id,                                                	   	");
		sql.append("					count(1) AS usedVolumeCount,                                	   	");
		sql.append("					sum(cvol.vol_size) AS usedVolumeCapacity                    	   	");
		sql.append("				FROM                                                          	   		");
		sql.append("					cloud_volume cvol                                           	   	");
		sql.append("				WHERE                                                         	   		");
		sql.append("					cvol.is_visable = '1'                                       	   	");
		sql.append("				AND (cvol.is_deleted = '0' or cvol.is_deleted = '2')                    ");
		sql.append("				AND cvol.prj_id = ?                                           	   		");
		sql.append("			) vol ON vol.prj_id = cp.prj_id                                 	   		");
		sql.append("			LEFT JOIN (                                                     	   		");
		sql.append("				SELECT                                                        	   		");
		sql.append("					ordervol.prj_id,                                            	  	");
		sql.append("					sum(ordervol.vol_number) AS usedVolumeCount,                 		");
		sql.append("					sum(ordervol.vol_size) AS usedVolumeCapacity    ");
		sql.append("				FROM  (                                                        	   		");
		sql.append("				SELECT                                               					");
		sql.append("					clov.prj_id,                                       					");
		sql.append("					clov.order_type,                                   					");
		sql.append("					clov.order_no,                                     					");
		sql.append("					CASE clov.order_type                               					");
		sql.append("					WHEN 2 THEN 0                                     					");
		sql.append("					ELSE clov.vol_number                           						");
		sql.append("					END AS vol_number,                             						");
		sql.append("					CASE clov.order_type                           						");
		sql.append("					WHEN 2 THEN clov.vol_size - cv.vol_size            					");
		sql.append("					ELSE clov.vol_number * clov.vol_size               					");
		sql.append("					END AS vol_size                                    					");
		sql.append("				FROM cloudorder_volume clov                          					");
		sql.append("				LEFT JOIN cloud_volume cv ON cv.vol_id = clov.vol_id 					");
		sql.append("					  ) as ordervol                                                		");
		sql.append("				LEFT JOIN order_info info ON info.order_no = ordervol.order_no	   		");
		sql.append("				WHERE                                                         	   		");
		sql.append("					ordervol.prj_id = ?                                      	   		");
		sql.append("				AND (                                                         	   		");
		sql.append("					info.order_state = '1'                                     	   		");
		sql.append("					OR info.order_state = '2'                                  	   		");
		sql.append("				)                                                             	   		");
		sql.append("				AND (                                                         	   		");
		sql.append("					ordervol.order_type = '0'                                        	");
		sql.append("					OR ordervol.order_type = '2'                                     	");
		sql.append("				)                                                             	   		");
		sql.append("			) ordervol ON ordervol.prj_id = cp.prj_id                       	   		");
		sql.append("			WHERE                                                           	   		");
		sql.append("				cp.prj_id = ?                                               	   		");

		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(),
				new Object[] { prjId, prjId, prjId, prjId, prjId, prjId, prjId });

		@SuppressWarnings("rawtypes")
		List result = query.getResultList();
		if (result != null && result.size() == 1) {
			int index = 0;
			Object[] objs = (Object[]) result.get(0);

			project.setProjectId(String.valueOf(objs[index++]));
			project.setHostCount(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			project.setCpuCount(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			project.setMemory(1024*Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			project.setDiskCount(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			project.setDiskCapacity(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			project.setOuterIP(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			project.setUsedVmCount(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			project.setUsedCpuCount(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			project.setUsedRam(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			project.setDiskCountUse(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			project.setUsedDiskCapacity(
					Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			project.setOuterIPUse(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			int orderHostCount = Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0");
			int orderCpuCount = Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0");
			int orderRamCount = Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0");
			int orderDiskCount = Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0");
			int orderVolumeCount = Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0") +orderHostCount;
			int orderVolCapacityCount = Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0") +orderDiskCount;
			int orderFloatIpCount = Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0");

			project.setUsedVmCount(project.getUsedVmCount() + orderHostCount);
			project.setUsedCpuCount(project.getUsedCpuCount() + orderCpuCount);
			project.setUsedRam(project.getUsedRam() + orderRamCount);
			project.setDiskCountUse(project.getDiskCountUse() + orderVolumeCount);
			project.setUsedDiskCapacity(project.getUsedDiskCapacity() + orderVolCapacityCount);
			project.setOuterIPUse(project.getOuterIPUse() + orderFloatIpCount);
		}

		return project;
	}

	public Map<String, Object> getStackList(BaseDcDataCenter dataCenter, String prjId) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		List<BaseCloudVm> list = new ArrayList<BaseCloudVm>();
		List<BaseCloudVolume> volList = new ArrayList<BaseCloudVolume>();
		Map<String, List<String>> secMap = new HashMap<String, List<String>>();

		List<JSONObject> result = openstackVmService.getStackList(dataCenter, prjId);

		if (result != null && result.size() > 0) {
			for (JSONObject jsonObject : result) {
				List<String> secList = new ArrayList<String>();
				String imageStr = jsonObject.getString("image");
				if ("".equals(imageStr)) {
					jsonObject.put("image", new Image());
				}
				Vm data = restService.json2bean(jsonObject, Vm.class);
				initData(data, jsonObject);
				BaseCloudVm ccn = new BaseCloudVm(data, dataCenter.getId());
				initDataVm(ccn, jsonObject);
				initList(volList, secList, jsonObject);
				secMap.put(ccn.getVmId(), secList);
				list.add(ccn);
			}
		}

		map.put("VmList", list);
		map.put("VolList", volList);
		map.put("SecMap", secMap);

		return map;
	}

	/**
	 * 私有方法，用于将JSONObject对象中的一些无法自动转换的参数，手动设置到java对象中
	 * 
	 * @param vm
	 * @param object
	 */
	private void initData(Vm vm, JSONObject object) {
		vm.setDiskConfig(object.getString("OS-DCF:diskConfig"));
		vm.setAvailability_zone(object.getString("OS-EXT-AZ:availability_zone"));
		vm.setHost(object.getString("OS-EXT-SRV-ATTR:host"));
		vm.setHypervisor_hostname(object.getString("OS-EXT-SRV-ATTR:hypervisor_hostname"));
		vm.setInstance_name(object.getString("OS-EXT-SRV-ATTR:instance_name"));
		vm.setPower_state(object.getString("OS-EXT-STS:power_state"));
		vm.setVm_state(object.getString("OS-EXT-STS:vm_state"));
		vm.setLaunched_at(object.getString("OS-SRV-USG:launched_at"));
		JSONArray jsonList = (JSONArray) JSONArray.parse(object.getString("os-extended-volumes:volumes_attached"));
		List<String> volumeList = new ArrayList<String>();
		if (jsonList != null && jsonList.size() > 0) {
			for (Object data : jsonList) {
				volumeList.add(((JSONObject) data).getString("id"));
			}
		}
		vm.setVolumes_attached(volumeList);
	}

	/**
	 * 处理底层返回的数据
	 * 
	 * @author zhouhaitao
	 * @param vm
	 * @param json
	 */
	private void initDataVm(BaseCloudVm vm, JSONObject json) {
		JSONObject addresses = json.getJSONObject("addresses");
		Set<String> netSet = addresses.keySet();
		String netId = "";
		String vmIp = "";
		String vmFrom = "";
		if (null != netSet && netSet.size() == 1) {
			for (String netName : netSet) {
				netId = queryNetWorkByName(netName, vm.getDcId());
				JSONArray address = addresses.getJSONArray(netName);
				if (null != address && address.size() > 0) {
					JSONObject add = address.getJSONObject(0);
					vmIp = add.getString("OS-EXT-IPS:type") + ":" + add.getString("addr");
				}
			}
		}
		String fromImageId = vm.getFromImageId();
		String fromVolumeId = vm.getFromVolumeId();
		if (!StringUtils.isEmpty(fromVolumeId)) {
			vmFrom = "disk";
		} else if (!StringUtils.isEmpty(fromImageId)) {
			BaseCloudImage image = cloudImageDao.findOne(fromImageId);
			if (null != image) {
				if ("1".equals(image.getImageIspublic() + "")) {
					vmFrom = "publicImage";
				} else if ("2".equals(image.getImageIspublic() + "")) {
					vmFrom = "privateImage";
				}
			}
		}
		vm.setNetId(netId);
		vm.setVmIp(vmIp);
		vm.setVmFrom(vmFrom);

	}

	private void initList(List<BaseCloudVolume> volList, List<String> escList, JSONObject json) {
		JSONArray volArrays = json.getJSONArray("os-extended-volumes:volumes_attached");
		JSONArray secArrays = json.getJSONArray("security_groups");
		if (volArrays != null && volArrays.size() > 0) {
			for (Object data : volArrays) {
				BaseCloudVolume volume = new BaseCloudVolume();
				volume.setVmId(json.getString("id"));
				volume.setVolId(((JSONObject) data).getString("id"));
				volList.add(volume);
			}
		}

		if (null != secArrays && secArrays.size() > 0) {
			for (Object data : secArrays) {
				String secName = ((JSONObject) data).getString("name");
				String prjId = json.getString("tenant_id");
				if (!StringUtils.isEmpty(secName)) {
					String id = querySecIdByName(secName, prjId);
					if (!StringUtils.isEmpty(id)) {
						escList.add(id);
					}
				}
			}
		}
	}

	private String queryNetWorkByName(String netName, String dcId) {
		String netId = "";
		StringBuffer hql = new StringBuffer();
		hql.append(" from BaseCloudNetwork where netName = ? and dcId = ?");

		BaseCloudNetwork network = (BaseCloudNetwork) cloudVmDao.findUnique(hql.toString(),
				new Object[] { netName, dcId });

		if (null != network) {
			netId = network.getNetId();
		}
		return netId;
	}

	private String querySecIdByName(String secName, String dcId) {
		String sgId = "";
		StringBuffer hql = new StringBuffer();
		hql.append(" from BaseCloudSecurityGroup where sgName = ? and dcId = ?");

		BaseCloudSecurityGroup network = (BaseCloudSecurityGroup) cloudVmDao.findUnique(hql.toString(),
				new Object[] { secName, dcId });

		if (null != network) {
			sgId = network.getSgId();
		}
		return sgId;
	}

	private String handleQueryTagCondition(String tagName) {
		List<String> tagNames = StringUtil.handleTagCondition(tagName);
		StringBuffer sql = new StringBuffer();
		if (tagNames != null && tagNames.size() > 0) {
			sql.append(" select DISTINCT tgres_resourceid from sys_tagresource ");
			for (int i = 0; i < tagNames.size(); i++) {
				// String name = tagNames.get(i).replaceAll("\\_", "\\\\_");
				String name = tagNames.get(i);

				sql.append(" INNER JOIN ( ");
				sql.append(" 	select ts.tgres_resourceid from sys_tagresource ts 	");
				sql.append("  	left join sys_tag t on t.tg_id =ts.tgres_tagid 	");
				sql.append("  	where tgres_resourcetype='vm' ");
				sql.append("  	and binary t.tg_name = '" + name + "'");
				sql.append(" ) " + ("t" + i));
				sql.append("   using (tgres_resourceid) ");
			}
		} else {
			sql.append(" '' ");
		}

		return sql.toString();
	}

	@Override
	public int getUnDeletedVmCountByProject(String prjId) {
		int count = 0;
		count = cloudVmDao.getUnDeletedVmCountByPrjId(prjId);
		return count;
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
	 * 判断当前云主机是否存在的创建中的自定义镜像
	 * 
	 * @param vmId
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public boolean checkCreatingImageCount(String vmId) {
		boolean flag = false;
		StringBuffer sql = new StringBuffer();
		sql.append("  select count(1) from cloud_image where from_vmid = ? and image_ispublic = '2' and (image_status ='SAVING' or image_status ='QUEUED') ");
		
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[] { vmId });
		
		List list = query.getResultList();
		if (null != list && list.size() > 0) {
			BigInteger bi = (BigInteger) list.get(0);
			int count = bi.intValue();
			if (count >= 1) {
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * 查询项目下可绑定云硬盘的云主机列表
	 * 
	 * @Author: chengxiaodong
	 * @param prjId
	 * @return
	 * 		<li>Date: 2016年5月11日</li>
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public List<CloudVm> getCanBindCloudVmList(String prjId) throws Exception {
		List<CloudVm> list = new ArrayList<CloudVm>();
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
	 * @author zhouhaitao
	 * 
	 * @param resourceId
	 *            云主机资源ID
	 * @param resourceState
	 *            资源需要改变成的状态
	 * @param date
	 *            资源的新到期时间 后付费 不需要此参数
	 * @param isShutdown
	 *            是否停止服务 true需要停止服务
	 * @param isResumable
	 *            是否开启服务 true需要启动服务
	 * 
	 * @return
	 * 
	 */
	public boolean modifyStateForVm(String resourceId, String resourceState, Date date, boolean isShutdown,
			boolean isResumable) {
		CloudVm cloudVm = queryVmChargeById(resourceId);
		
		if(isShutdown){
			if("ACTIVE".equals(cloudVm.getVmStatus())){
				shutdownVm(cloudVm);
			}
			if(PayType.PAYAFTER.equals(cloudVm.getPayType())){
				cloudVm.setOpDate(new Date());
				vmOptionCharge(cloudVm,"restrict");
			}
		}
		
		if(isResumable){
			if("SHUTOFF".equals(cloudVm.getVmStatus())){
				restartVm(cloudVm);
			}
			if(PayType.PAYAFTER.equals(cloudVm.getPayType())){
				cloudVm.setOpDate(new Date());
				vmOptionCharge(cloudVm,"recover");
			}
			
		}
		BaseCloudVm bcv = cloudVmDao.findOne(resourceId);
		bcv.setChargeState(resourceState);
		if(null != date){
			bcv.setEndTime(date);
		}
		cloudVmDao.saveOrUpdate(bcv);
//		modifyVmForChargeStatus(resourceState,resourceId,date);
		return false;
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
	@Transactional(noRollbackFor=AppException.class)
	public List<BaseCloudVm> createVm(CloudOrderVm order) throws AppException {
		int disStep = 0;
		List<Vm> vmList = new ArrayList<Vm>();
		List<CloudFloatIp> floatIpList = new ArrayList<CloudFloatIp>();
		BaseCloudFlavor cloudFlavor = new BaseCloudFlavor();
		List<BaseCloudVm> result = new ArrayList<BaseCloudVm>();
		try {
			if ("1".equals(order.getBuyFloatIp())) {
				floatIpList = floatIpService.addFloatIp(order.getOrderNo(), false);
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
			if(ConstantClazz.VM_LOGIN_TYPE_PWD.equals(order.getLoginType())){
				cloudVm.setPassword(order.getPassword());
			}
			else if(ConstantClazz.VM_LOGIN_TYPE_SSH.equals(order.getLoginType())){
				BaseCloudSecretKey secretKey = secretKeyService.getSecretKeyById(order.getSecretKey());
				cloudVm.setSecretPublicKey(secretKey.getPublicKey());
			}
			cloudVm.setOsType(order.getOsType());
			cloudVm.setFromImageId(order.getImageId());
			cloudVm.setDisks(order.getDisk());
			cloudVm.setSgId(order.getSgId());
			cloudVm.setLoginType(order.getLoginType());

			String errors = openstackVmService.createVm(cloudVm, cloudFlavor.getFlavorId(), vmList);

			if (!StringUtils.isEmpty(errors)) {
				throw new AppException("error.openstack.message", new String[] { errors });
			} 
			disStep = 1;
			return result;
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			throw new AppException("error.openstack.message");
		} finally {
			try {
				vmCreateCallback(order, disStep, cloudFlavor.getFlavorId(), floatIpList, vmList);
			} catch (AppException e) {
				throw e;
			} catch (Exception e) {
			    log.error(e.getMessage(),e);
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
		List<BaseCloudVolume> dataVolumeList=new ArrayList<BaseCloudVolume>();
		List<BaseCloudVm> succVmList  = null;
		int a = 0;
		try{
			try{
				succVmList = saveVmAndVolume(order, floatIpList, vmList, flavorId, volumeList);
			}catch(Exception e){
			    log.error(e.getMessage(), e);
				throw new Exception(e.getMessage());
			}
			a = 1;
			if (step == 0) {
				createFailHandler(order, succVmList);
			} else if (step == 1) {
				vmSuccessHandler(order, floatIpList, succVmList, volumeList,dataVolumeList);
			}
		}catch(Exception e){
		    log.error(e.getMessage(), e);
		    if(step == 1){
		    	createFailVmsAndVolumes(order, succVmList,dataVolumeList);
		    }
		    if(a != 1){
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
	 * @param vmList
	 *            已经创建成功的主机列表
	 * 
	 * @throws Exception
	 */
	@Transactional(noRollbackFor=AppException.class)
	public void createFailHandler(CloudOrderVm order, List<BaseCloudVm> vmList)
			throws Exception {
		try {
			//调用创建资源创建失败的接口
			orderService.completeOrder(order.getOrderNo(), false,null);
			messageCenterService.addResourFailMessage(order.getOrderNo(), order.getCusId());
			if ("1".equals(order.getBuyFloatIp())) {
				floatIpService.releaseFloatIpByOrderNo(order.getOrderNo());
			}
			if (null != vmList && vmList.size() > 0) {
				vmFailedHandler(order);
			}
		} catch (Exception e) {
			deleteFailedHandler(order);
			log.error(e.getMessage(),e);
			throw e;
		} 
	}

	/**
	 * <p>
	 * 删除失败订单中 创建成功的云主机
	 * </p>
	 * -------------------------
	 * 
	 * @author zhouhaitao
	 * @param orderVm
	 *            已经创建成功的云主机列表
	 */
	private void vmFailedHandler(CloudOrderVm orderVm) {
		List<CloudVm> vmList = queryVmListByOrder(orderVm.getOrderNo());
		if (null != vmList && vmList.size() > 0) {
			for (CloudVm vm : vmList) {
				deleteVmForCreateFailed(vm,orderVm.getCreateUser());

				CloudBatchResource cloudBatchResource = new CloudBatchResource();

				cloudBatchResource.setOrderNo(orderVm.getOrderNo());
				cloudBatchResource.setResourceId(vm.getVmId());

				cloudBatchResourceService.delete(cloudBatchResource);
				
				vmSgService.deleteByVmId(vm.getVmId());
				
				secretkeyVmService.deleteByVm(vm.getVmId());
			}
		}
	}

	/**
	 * 云主机强制删除
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 */
	@Transactional(noRollbackFor=AppException.class)
	public void forceDeleteVm(CloudVm cloudVm , String userName) {
		try {
			if (checkSavingSnapshot(cloudVm)) {
				throw new AppException("当前云主机正在创建自定义镜像，不允许删除");
			}
			
			CloudFloatIp cloudFloatIp = queryFloatIpByVm(cloudVm.getVmId());
			if(null != cloudFloatIp){
				cloudFloatIp.setResourceId(cloudVm.getVmId());
				cloudFloatIp.setResourceType("vm");
				floatIpService.unbundingResource(cloudFloatIp);
			}
			
			volumeService.debindVolsByVmId(cloudVm.getVmId());
			
			openstackVmService.forceDelete(cloudVm);

			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			vm.setDeleteUser(userName);
			vm.setDeleteTime(new Date());
			vm.setVmStatus("DELETING");
			cloudVm.setOpDate(vm.getDeleteTime());
			cloudVm.setVmName(vm.getVmName());

			cloudVmDao.merge(vm);
			
			tagService.refreshCacheAftDelRes("vm", cloudVm.getVmId());
			
			alarmService.cleanAlarmDataAfterDeletingVM(cloudVm.getVmId());
			alarmService.deleteMonitorByResource(MonitorResourceType.VM.toString(), cloudVm.getVmId());
			
			ecmcAlarmService.cleanAlarmDataAfterDeletingObject(cloudVm.getVmId());
			
			memberService.deleteMemberByVm(cloudVm.getVmId());
			
			volumeService.deleteVolumeByVm(cloudVm.getVmId(), userName);
			
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
	 * 云主机软删除
	 * 
	 * @author zhouhaitao
	 * 
	 * @param cloudVm
	 */
	@Transactional(noRollbackFor=AppException.class)
	public void softDeleteVm(CloudVm cloudVm, SessionUserInfo sessionUser){
		try {
			if (checkSavingSnapshot(cloudVm)) {
				throw new AppException("当前云主机正在创建自定义镜像，不允许删除");
			}
			
			volumeService.debindVolsByVmId(cloudVm.getVmId());
			
			CloudFloatIp cloudFloatIp = queryFloatIpByVm(cloudVm.getVmId());
			if(null != cloudFloatIp){
				cloudFloatIp.setResourceId(cloudVm.getVmId());
				cloudFloatIp.setResourceType("vm");
				floatIpService.unbundingResource(cloudFloatIp);
			}
			
			openstackVmService.softDeleteVm(cloudVm);

			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			vm.setVmStatus("SOFT_DELETING");
			vm.setDeleteUser(sessionUser.getUserName());
			vm.setDeleteTime(new Date());
			
			cloudVmDao.merge(vm);
			
			alarmService.cleanAlarmDataAfterDeletingVM(cloudVm.getVmId());
			
			ecmcAlarmService.cleanAlarmDataAfterDeletingObject(cloudVm.getVmId());
			
			memberService.deleteMemberByVm(cloudVm.getVmId());
			
			portMappingService.deletePortMappingListByDestinyId(cloudVm.getDcId(),cloudVm.getPrjId(),cloudVm.getVmId());
			
			modifySysDiskForRecycle(cloudVm.getVmId(),sessionUser.getUserName(),vm.getDeleteTime());
			
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
			cloudVm.setCusId(sessionUser.getCusId());
			cloudVm.setVmName(vm.getVmName());
			
			if(PayType.PAYAFTER.equals(vm.getPayType())){
				vmOptionCharge(cloudVm,"recycle");
			}
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("error.openstack.message");
		}
	}
	
	public void deleteVmForCreateFailed(CloudVm cloudVm,String userName) throws AppException{
		try {
			JSONObject stackVm = openstackVmService.get(cloudVm.getDcId(), cloudVm.getPrjId(), cloudVm.getVmId());
			System.out.println(stackVm.toJSONString());
			if("ERROR".equalsIgnoreCase(stackVm.getString("status"))){
				openstackVmService.softDeleteVm(cloudVm);
			}
			else{
				openstackVmService.forceDelete(cloudVm);
			}

			BaseCloudVm vm = new BaseCloudVm();
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			vm.setDeleteUser(userName);
			vm.setDeleteTime(new Date());
			vm.setVmStatus("DELETING");

			cloudVmDao.merge(vm);
			
			volumeService.deleteVolumeByVm(cloudVm.getVmId(), userName);
			
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
	 * 修改主机的删除状态
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 */
	public void modifyForIsDelete(CloudVm cloudVm){
		
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
			List<BaseCloudVolume> volumeList,List<BaseCloudVolume> dataVolumeList) throws Exception {
		int successCount = 0;
		if (null != vmList && vmList.size() > 0) {
			for (int i =0;i<vmList.size();i++) {
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
						
						floatIpService.bindResource(floatIp);
					}
					
					if(!StringUtils.isEmpty(order.getSelfSubnetId())){
						InterfaceAttachment interAtta = openstackVmService.bindPort(order.getDcId(), order.getPrjId(), tempVm.getVmId(), order.getNetId(),
								order.getSelfSubnetId(),new String []{order.getSgId()});
						tempVm.setSelfIp(interAtta.getFixed_ips()[0].getIp_address());
						tempVm.setSelfPortId(interAtta.getPort_id());
						cloudVmDao.saveOrUpdate(tempVm);
					}
					successCount++;
				}
			}
			
			if(order.getDataDisk()>0){
				//如果购买云主机同时购买数据盘
				if(order.getDataDisk()>0){
					CloudOrderVolume volOrder=cloudOrderVolumeService.getByOrder(order.getOrderNo());
					volumeService.addVmsAndVolumes(volOrder,dataVolumeList);
				}
				
				JSONObject json = new JSONObject();
				json.put("orderNo", order.getOrderNo());
				json.put("dcId", order.getDcId());
				json.put("prjId", order.getPrjId());
				json.put("vmName", order.getVmName());
				json.put("vmStatus", "BUILD");
				json.put("count", "0");
				json.put("flavorId",order.getFlavorId() );
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
				json.put("dateDisks", order.getDataDisk());
				json.put("volTypeId",order.getSysTypeId());
				json.put("payType", order.getPayType());
				final JSONObject data = json;
    				TransactionHookUtil.registAfterCommitHook(new Hook() {
    					@Override
    					public void execute() {
    						try {
    							jedisUtil.push(RedisKey.volAttVmKey, data.toJSONString());
    						} catch (Exception e) {
    							e.printStackTrace();
    						}
    					}
    					
    				});
				
			}else if ((order.getDataDisk()==0)&&(successCount != order.getCount())) {
				JSONObject json = new JSONObject();
				json.put("orderNo", order.getOrderNo());
				json.put("dcId", order.getDcId());
				json.put("prjId", order.getPrjId());
				json.put("vmName", order.getVmName());
				json.put("vmStatus", "BUILD");
				json.put("count", "0");
				json.put("flavorId",order.getFlavorId() );
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
				json.put("volTypeId",order.getSysTypeId());
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
				if(order.getDataDisk()==0){
					allVmSuccessHnadler(order, floatIpList, vmList);
				}
			}
		}

		if (null != volumeList && volumeList.size() > 0) {
			for (BaseCloudVolume vol : volumeList) {

				JSONObject json = new JSONObject();
				json.put("volId", vol.getVolId());
				json.put("dcId", vol.getDcId());
				json.put("prjId", vol.getPrjId());
				json.put("volTypeId",order.getSysTypeId());
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
	public void allVmSuccessHnadler(CloudOrderVm orderVm, List<CloudFloatIp> floatIpList, List<BaseCloudVm> vmList) throws Exception {
		JSONObject json = new JSONObject();
		List<String> floatIds = new ArrayList<String>();
		List<String> vmIds = new ArrayList<String>();
		//发送订单资源创建成功,返回订单完成时间
		List<BaseOrderResource> resourceList = new ArrayList<BaseOrderResource>();
		for(CloudFloatIp ip:floatIpList){
			BaseOrderResource resource = new BaseOrderResource();
			resource.setOrderNo(orderVm.getOrderNo());
			resource.setResourceId(ip.getFloId());
			resource.setResourceName(ip.getFloIp());
			
			resourceList.add(resource);
		}
		for(BaseCloudVm vm :vmList){
			BaseOrderResource resource = new BaseOrderResource();
			resource.setOrderNo(orderVm.getOrderNo());
			resource.setResourceId(vm.getVmId());
			resource.setResourceName(vm.getVmName());
			
			resourceList.add(resource);
		}
		BaseOrder order = orderService.completeOrder(orderVm.getOrderNo(), true,resourceList);
		orderVm.setOrderCompleteDate(order.getCompleteTime());
		Date completeDate = null;
		if(PayType.PAYBEFORE.equals(orderVm.getPayType())){
			completeDate = order.getResourceExpireTime();
		}
		else if(PayType.PAYAFTER.equals(orderVm.getPayType())){
			floatIpService.sendMessage(floatIpList, EayunQueueConstant.QUEUE_BILL_RESOURCE_PURCHASE, orderVm.getCusId(), orderVm.getOrderNo());
		}

		for (CloudFloatIp floatIp : floatIpList) {
			floatIds.add(floatIp.getFloId());
		}
		
		CloudVolumeType type=volTypeService.getVolumeTypeById(orderVm.getDcId(), orderVm.getSysTypeId());
		
		for (BaseCloudVm vm : vmList) {
			vmIds.add(vm.getVmId());
			if(PayType.PAYAFTER.equals(orderVm.getPayType())){
				if(null!=type&&!"".equals(type.getTypeId())){
					orderVm.setSysDiskType(type.getVolumeType());
				}
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
		
		cloudOrderVmService.modifyResourceForVisable(vmIds,floatIds,completeDate);
		
		cloudBatchResourceService.deleteByOrder(orderVm.getOrderNo());
	}
	
	/**
	 * 云主机升级配置计费
	 * 
	 * @author zhouhaitao
	 * 
	 * @param cloudVm
	 *            云主机信息
	 * 
	 */
	public void vmUpgradeCharge(final CloudVm cloudVm) {
		TransactionHookUtil.registAfterCommitHook(new Hook() {
			@Override
			public void execute() {
				ChargeRecord record = new ChargeRecord();
				ParamBean param = new ParamBean();
				
				param.setNumber(1);
				param.setCpuSize(cloudVm.getCpus());
				param.setRamCapacity(cloudVm.getRams() / 1024);
				String imageId = queryChargeImageId(cloudVm.getFromImageId());
				param.setImageId(imageId);
				
				if(null!=cloudVm.getVolType()&&"1".equals(cloudVm.getVolType())){
					param.setSysDiskOrdinary(cloudVm.getDisks());
				}else if(null!=cloudVm.getVolType()&&"2".equals(cloudVm.getVolType())){
					param.setSysDiskBetter(cloudVm.getDisks());
				}else if(null!=cloudVm.getVolType()&&"3".equals(cloudVm.getVolType())){
					param.setSysDiskBest(cloudVm.getDisks());
				}else{
					param.setSysDiskCapacity(cloudVm.getDisks());
				}
				
				
				record.setParam(param);
				record.setDatecenterId(cloudVm.getDcId());
				record.setOrderNumber(cloudVm.getOrderNo());
				record.setCusId(cloudVm.getCusId());
				record.setResourceId(cloudVm.getVmId());
				record.setResourceType(ResourceType.VM);
				record.setChargeFrom(cloudVm.getOpDate());
				
				rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_UPGRADE, JSONObject.toJSONString(record));
			}
		});
	}
	
	/**
	 * 云主机限制服务 计费队列(type = "restrict")
	 * 云主机恢复服务 计费队列(type = "recover")
	 * 云主机放入回收站 计费队列(type = "recycle")
	 * 云主机回收站中还原 计费队列(type = "restore")
	 * 云主机回彻底删除 计费队列(type = "delete")
	 * 
	 * @param cloudVm
	 */
	public void vmOptionCharge(final CloudVm cloudVm,final String type) {
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
				
				if("restrict".equals(type)){
					queueName = EayunQueueConstant.QUEUE_BILL_RESOURCE_RESTRICT;
				}
				else if("recover".equals(type)){
					queueName = EayunQueueConstant.QUEUE_BILL_RESOURCE_RECOVER;
				}
				else if("recycle".equals(type)){
					record.setResourceName(cloudVm.getVmName());
					queueName = EayunQueueConstant.QUEUE_BILL_RESOURCE_RECYCLE;
				}
				else if("restore".equals(type)){
					queueName = EayunQueueConstant.QUEUE_BILL_RESOURCE_RESTORE;
				}
				else if("delete".equals(type)){
					record.setResourceName(cloudVm.getVmName());
					queueName = EayunQueueConstant.QUEUE_BILL_RESOURCE_DELETE;
				}
				rabbitTemplate.send(queueName, JSONObject.toJSONString(record));
			}
		});
	}
	

	/**
	 * 校验本次订单信息是否超配 ---------------------
	 * 
	 * @author zhouhaitao
	 * @param orderVm
	 *            云主机订单信息
	 * @return 返回的错误信息标示
	 */
	private String checkVmQuota(CloudOrderVm orderVm ,String type) {
		String errMsg = null;
		CloudProject project = queryPrjQuato(orderVm.getPrjId());
		if("buy".equalsIgnoreCase(type)){
			if (orderVm.getCount() > (project.getHostCount() - project.getUsedVmCount())) {
				errMsg = "OUTOF_VMCOUNT_QUOTA";
				return errMsg;
			}
			if(orderVm.getDataDisk()>0){
				if (orderVm.getCount()*2 > (project.getDiskCount() - project.getDiskCountUse())) {
					errMsg = "OUTOF_VOLUMECOUNT_QUOTA";
					return errMsg;
				}	
			}else{
				if (orderVm.getCount() > (project.getDiskCount() - project.getDiskCountUse())) {
					errMsg = "OUTOF_VOLUMECOUNT_QUOTA";
					return errMsg;
				}
			}
			
			if ("1".equals(orderVm.getBuyFloatIp()) && (orderVm.getCount() > (project.getOuterIP() - project.getOuterIPUse()))) {
				errMsg = "OUTOF_FLOATIPCOUNT_QUOTA";
				return errMsg;
			}
			if ((orderVm.getCount() * orderVm.getCpu()) > (project.getCpuCount() - project.getUsedCpuCount())) {
				errMsg = "OUTOF_CPU_QUOTA";
				return errMsg;
			}
			if ((orderVm.getCount() * orderVm.getRam()) > (project.getMemory() - project.getUsedRam())) {
				errMsg = "OUTOF_MEMORY_QUOTA";
				return errMsg;
			}
			if ((orderVm.getCount() * (orderVm.getDisk()+orderVm.getDataDisk())) > (project.getDiskCapacity() - project.getUsedDiskCapacity())) {
				errMsg = "OUTOF_DISKCAPACITY_QUOTA";
				return errMsg;
			}
		}
		else if ("resize".equals(type)){
			if ((orderVm.getCpu() - orderVm.getVmCpu()) > (project.getCpuCount() - project.getUsedCpuCount())) {
				errMsg = "OUTOF_CPU_QUOTA";
				return errMsg;
			}
			if ((orderVm.getRam() - orderVm.getVmRam()) > (project.getMemory() - project.getUsedRam())) {
				errMsg = "OUTOF_MEMORY_QUOTA";
				return errMsg;
			}
		}

		return errMsg;
	}

	/**
	 * 创建云主机的订单
	 * 
	 * @param cloudOrder
	 *            云主机购买信息
	 * @param user
	 *            当前用户
	 * @return 订单信息
	 * @throws Exception
	 */
	private Order createVmOrder(CloudOrderVm cloudOrder, SessionUserInfo user) throws Exception {
		Order order = new Order();

		order.setOrderType(cloudOrder.getOrderType());
		order.setDcId(cloudOrder.getDcId());
		order.setProdCount(cloudOrder.getCount());
		order.setProdConfig(vmConfig(cloudOrder));
		order.setPayType(cloudOrder.getPayType());
		order.setResourceType(ResourceType.VM);
		order.setUserId(user.getUserId());
		order.setCusId(user.getCusId());
		order.setProdName(cloudOrder.getProdName());
		
		if (PayType.PAYBEFORE.equals(cloudOrder.getPayType())) {
			cloudOrder.setPrice(cloudOrder.getPaymentAmount().divide(new BigDecimal(cloudOrder.getCount()),2));
			if(OrderType.NEW.equals(cloudOrder.getOrderType())){
				order.setBuyCycle(cloudOrder.getBuyCycle());
			}
			else if(OrderType.UPGRADE.equals(cloudOrder.getOrderType())){
				order.setResourceExpireTime(cloudOrder.getEndTime());
			}
			order.setUnitPrice(cloudOrder.getPrice());
			order.setPaymentAmount(cloudOrder.getPaymentAmount());
			order.setAccountPayment(cloudOrder.getAccountPayment());
			order.setThirdPartPayment(cloudOrder.getThirdPartPayment());
			
		} else if (PayType.PAYAFTER.equals(cloudOrder.getPayType())) {
			order.setBillingCycle(BillingCycleType.HOUR);
		}

		orderService.createOrder(order);

		return order;
	}

	/**
	 * 查询云主机的操作系统名称
	 * 
	 * @param vmId
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unused" })
	@Override
	public String getOSNameByVmId(String vmId) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("select dt.node_name from cloud_vm vm ")
				.append("left join sys_data_tree dt on vm.sys_type = dt.node_id ").append("where vm.vm_id=?");
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sb.toString(), vmId);
		List resultList = query.getResultList();
		String osName = "";
		for (int i = 0; i < resultList.size(); i++) {
			osName = (String) resultList.get(i);
			break;
		}
		return osName;
	}
	
	/**
	 * 见该批次的资源全部置为显示状态
	 * 
	 * @param chargeStatus
	 * @param vmId 
	 * @param date 
	 * @return
	 */
	@SuppressWarnings("unused")
    private boolean modifyVmForChargeStatus(String chargeStatus,String vmId,Date date){
		StringBuffer vmsql = new StringBuffer ();
		boolean isSuccess = false;
		Object [] args = new Object[3];
		int index =0 ;
		try{
			vmsql.append(" update cloud_vm set charge_state =  ? ");
			args[index++] = chargeStatus;
			if(null != date){
				vmsql.append(" ,end_time =  ? ");
				args[index++] = date;
			}
			vmsql.append(" where vm_id = ? ");
			args[index++] = vmId;
			Object[] params = new Object[index];
			System.arraycopy(args, 0, params, 0, index);
			
			cloudVmDao.execSQL(vmsql.toString(), params);
			
			isSuccess = true;
		}catch(Exception e){
			isSuccess = false;
			log.error(e.getMessage(),e);
		}
		return isSuccess;
	}
	
	
	/**
	 * 查询项目下的网络列表
	 * 
	 * @author zhouhaitao
	 * @param prjId
	 * @return
	 */
	public List<CloudNetWork> queryNetListByPrjId(String prjId){
		List<CloudNetWork> netList = new ArrayList<CloudNetWork>();
		StringBuffer sql = new StringBuffer();
		sql.append("		SELECT                          ");
		sql.append("			cn.net_id,                  ");
		sql.append("			cn.net_name,                ");
		sql.append("			cr.gateway_ip               ");
		sql.append("		FROM                            ");
		sql.append("			cloud_network cn            ");
		sql.append("		LEFT JOIN cloud_route cr        ");
		sql.append("		ON cn.net_id = cr.network_id    ");
		sql.append("		WHERE                           ");
		sql.append("			cn.prj_id = ?               ");
		sql.append("		AND cn.charge_state = '0'       ");
		sql.append("		AND cn.router_external = '0'    ");
		sql.append("");
		
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[]{prjId});
		@SuppressWarnings("rawtypes")
		List result = query.getResultList();
		if (result != null && result.size() > 0) {
			for (int i = 0; i < result.size(); i++) {
				int index = 0;
				Object[] objs = (Object[]) result.get(i);
				CloudNetWork net = new CloudNetWork();
				net.setNetId(String.valueOf(objs[index++]));
				net.setNetName(String.valueOf(objs[index++]));;
				net.setGatewayIp(String.valueOf(objs[index++]));;

				netList.add(net);
			}
		}
		
		return netList;
	}
	
	/**
	 * 查询项目下的安全组列表
	 * 
	 * @author zhouhaitao
	 * @param prjId
	 * @return
	 */
	public List<CloudSecurityGroup> querySgListByPrjId(String prjId){
		List<CloudSecurityGroup> sgList = new ArrayList<CloudSecurityGroup>();
		StringBuffer sql = new StringBuffer();
		sql.append("	SELECT                     ");
		sql.append("		cs.sg_id,              ");
		sql.append("		cs.sg_name             ");
		sql.append("	FROM                       ");
		sql.append("		cloud_securitygroup cs ");
		sql.append("	WHERE                      ");
		sql.append("		cs.prj_id = ?          ");
		sql.append("	AND cs.default_group = ?   ");
		
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[]{prjId,"defaultGroup"});
		@SuppressWarnings("rawtypes")
		List result = query.getResultList();
		if (result != null && result.size() > 0) {
			for (int i = 0; i < result.size(); i++) {
				int index = 0;
				Object[] objs = (Object[]) result.get(i);
				CloudSecurityGroup sg = new CloudSecurityGroup();
				sg.setSgId(String.valueOf(objs[index++]));
				sg.setSgName(String.valueOf(objs[index++]));;
				if("default".equals(sg.getSgName())){
					sg.setSgName("默认安全组");
				}
				
				sgList.add(sg);
			}
		}
		
		return sgList;
	
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<CloudVm> queryNormalVm(String prjId,Date endTime, String state, boolean isDeleted,boolean isRecycle,
			String payType, String cusState) throws Exception {
		List<CloudVm> cloudVmList = new ArrayList<CloudVm>();
		List list = new ArrayList();
		StringBuffer sb = new StringBuffer();
		sb.append(" select vm.vm_id,vm.vm_name from cloud_vm vm ");
		if (cusState != null && cusState.length() > 0) {
			sb.append(" left join cloud_project prj on vm.prj_id=prj.prj_id  ");
			sb.append(" left join sys_selfcustomer cus on cus.cus_id=prj.customer_id ");
		}
		if(isRecycle){
			sb.append(" where vm.is_deleted !=? ");
			list.add("1");
		}else{
			sb.append(" where vm.is_deleted =? ");
			list.add(isDeleted ? "1" : "0");
		}
		if(prjId!=null&&prjId.length()>0){
			sb.append(" and vm.prj_id=? ");
			list.add(prjId);
		}
		if (endTime != null) {
			sb.append(" and vm.end_time<=? ");
			list.add(endTime);
		}
		if (state != null && state.length() > 0) {
			sb.append(" and vm.charge_state=? ");
			list.add(state);
		}
		if (payType != null && payType.length() > 0) {
			sb.append(" and vm.pay_type=? ");
			list.add(payType);
		}
		if(cusState!=null&&cusState.length()>0){
			sb.append(" and cus.is_blocked=? ");
			list.add(cusState);
		}
		sb.append(" and vm.is_visable ='1' ");
		javax.persistence.Query query= cloudVmDao.createSQLNativeQuery(sb.toString(),list.toArray());
		List result=query.getResultList();
		if(result!=null&&result.size()>0){
			for (int i = 0; i < result.size(); i++) {
				Object[] objs = (Object[])result.get(i);
				CloudVm cloudVm=new CloudVm();
				cloudVm.setVmId(String.valueOf(objs[0]));
				cloudVm.setVmName(String.valueOf(objs[1]));
				cloudVmList.add(cloudVm);
			}
		}
		return cloudVmList;
	}
	
	/**
	 * 根据云主机ID查询云主机的计费队列需要的信息
	 * 
	 * @param vmId
	 * @return
	 */
	public CloudVm queryVmChargeById(String vmId){
		CloudVm cloudVm = new CloudVm();

		StringBuffer sql = new StringBuffer();
		sql.append(" select ");
		sql.append(" 	vm.vm_id as vmId,	");
		sql.append(" 	vm.vm_name as vmName,	");
		sql.append(" 	vm.prj_id as prjId,	");
		sql.append(" 	vm.dc_id as dcId,	");
		sql.append(" 	vm.vm_status as vmStatus,	");
		sql.append(" 	flv.flavor_vcpus as cpus,	");
		sql.append(" 	flv.flavor_ram as rams,	");
		sql.append(" 	flv.flavor_disk as disks,	");
		sql.append(" 	vm.sys_type as sysType,	");
		sql.append(" 	vm.charge_state as chargeState,");
		sql.append(" 	vm.pay_type as payType,");
		sql.append(" 	vm.end_time as endTime,");
		sql.append(" 	vm.delete_time as deleteTime,");
		sql.append(" 	prj.customer_id as cusId,");
		sql.append(" 	vm.from_imageid as imageId");
		sql.append(" from cloud_vm vm ");
		sql.append(" left join cloud_flavor flv on vm.flavor_id=flv.flavor_id and vm.dc_id=flv.dc_id");
		sql.append(" left join cloud_project prj on vm.prj_id=prj.prj_id");
		sql.append(" where 1=1");
		sql.append(" and vm.vm_id= ? ");
		sql.append(" and vm.is_deleted= ? ");

		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[] { vmId, "0" });
		@SuppressWarnings("rawtypes")
		List list = new ArrayList();
		if (null != query) {
			list = query.getResultList();
		}
		if (null != list && list.size() == 1) {
			Object[] objs = (Object[]) list.get(0);
			int index = 0;
			cloudVm = new CloudVm();

			cloudVm.setVmId(String.valueOf(objs[index++]));
			cloudVm.setVmName(String.valueOf(objs[index++]));
			cloudVm.setPrjId(String.valueOf(objs[index++]));
			cloudVm.setDcId(String.valueOf(objs[index++]));
			cloudVm.setVmStatus(String.valueOf(objs[index++]));
			cloudVm.setCpus(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index-1]) : "0"));
			cloudVm.setRams(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index-1]) : "0"));
			cloudVm.setDisks(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index-1]) : "0"));
			String systemType = String.valueOf(objs[index++]);
			if (!StringUtils.isEmpty(systemType) && !"null".equals(systemType)) {
				SysDataTree sdt = DictUtil.getDataTreeByNodeId(systemType);
				cloudVm.setSysType(systemType);
				cloudVm.setSysTypeEn(sdt.getNodeNameEn());
			}
			cloudVm.setChargeState(String.valueOf(objs[index++] == null ? "" : objs[index-1]));
			cloudVm.setPayType(String.valueOf(objs[index++] == null ? "" : objs[index-1]));
			cloudVm.setEndTime((Date) objs[index++]);
			cloudVm.setDeleteTime((Date) objs[index++]);
			cloudVm.setCusId(String.valueOf(objs[index++]));
			cloudVm.setFromImageId(objs[index++] != null ? String.valueOf(objs[index-1]) : "");
			if(null != cloudVm.getEndTime()){
				cloudVm.setCycleCount(DateUtil.getUgradeRemainDays(new Date(), cloudVm.getEndTime()));
			}

		}

		return cloudVm;
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
	 * 云主机续费，提交按钮校验当前主机是否有未完成订单，以及当前账户金额是否充足与提交订单	
	 * 
	 * @author liyanchao	
	 * @param map
	 * @param userId
	 * @param cusId
	 * @return JSONObject
	 */
	public JSONObject renewVmOrderConfirm(Map<String ,String> map,String userId,String userName,String cusId)throws Exception{
		JSONObject jsonResult = new JSONObject ();
		
		boolean flag = false;
		flag = this.checkVmOrderExsit(map.get("vmId").toString());
		if(!flag){
			jsonResult = this.createVmRenewOrder(map, userId,userName,cusId);
		}else{
			jsonResult.put("respCode", 1);
			jsonResult.put("message", "您当前有未完成订单，不允许提交新订单！");
		}
		return jsonResult;
	
	}
	
	/**
	 * @param map
	 * @param userId
	 * @param userName
     * @param cusId
	 * @return JSONObject
	 * @throws Exception
	 */
	private JSONObject createVmRenewOrder(Map<String ,String> map,String userId,String userName,String cusId)throws Exception{
		
		JSONObject jsonResult = new JSONObject ();
		String aliPay = (String) map.get("aliPay");//支付宝付款金额
		String accountPay = (String) map.get("accountPay");//余额付款金额
		String totalPay = (String) map.get("totalPay");
		String isAccountPay = (String) map.get("isCheck");
		String vmId = (String) map.get("vmId");
		
		BigDecimal orgTotalPay = new BigDecimal(totalPay);
		BigDecimal price = checkRenewAmount(map, vmId);
        price = price.setScale(2, BigDecimal.ROUND_FLOOR);
		if(orgTotalPay.compareTo(price)==0){
			if("false".equals(isAccountPay)|| null==isAccountPay){// 直接"创建订单，跳向支付宝支付页面！";
				if(aliPay.compareTo("0")==0){//说明没有勾选月支付，且支付金额为零
					Order order = orderService.createOrder(organizOrder(map, userId,cusId));
					this.saveOrUpdateCloudOrderVm(order, vmId, cusId, userName);//创建订单后，回写业务信息
					
					jsonResult.put("respCode", 10);
					jsonResult.put("message", order.getProdName());
				}else{
					Order order = orderService.createOrder(organizOrder(map, userId,cusId));
					this.saveOrUpdateCloudOrderVm(order, vmId, cusId, userName);//创建订单后，回写业务信息
					
					jsonResult.put("respCode", 0);
					jsonResult.put("message", order.getOrderNo());
				}
				
				
			}else{//勾选余额支付
				BigDecimal nowAccountMoney = null;
				MoneyAccount accountMoney = accountOverviewSerivce.getAccountInfo(cusId);
				nowAccountMoney = accountMoney.getMoney();
				
				if(nowAccountMoney.compareTo(new BigDecimal(accountPay))>=0){//当前账户余额>=余额支付的金额
					if(new BigDecimal(aliPay).compareTo(new BigDecimal(0))==0){//如果未用支付宝，只用余额支付
						// "创建订单，直接跳转订单完成页面"
						Order order = orderService.createOrder(organizOrder(map, userId,cusId));
						this.saveOrUpdateCloudOrderVm(order, vmId, cusId, userName);//创建订单后，回写业务信息
						
						jsonResult.put("respCode", 10);
						jsonResult.put("message", order.getProdName());
						
					}else{//有用余额+支付宝 混合支付
						Order order = orderService.createOrder(organizOrder(map, userId,cusId));
						this.saveOrUpdateCloudOrderVm(order, vmId, cusId, userName);//创建订单后，回写业务信息
						
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
			jsonResult.put("message", "您的订单金额或资源配置发生变动，请重新确认订单！");
		}
		
		return jsonResult;
	}

    private BigDecimal checkRenewAmount(Map<String, String> map, String vmId) throws Exception {
        //重新获取云主机后台配置，计算订单金额使用后台配置计算
        CloudVm vm = getById(vmId);
        int cpuSize = vm.getCpus();
        int ramCapacity = vm.getRams();
        int sysDiskCapacity = vm.getDisks();

        ParamBean paramBean = new ParamBean();
        paramBean.setDcId(map.get("dcId"));
        paramBean.setPayType(map.get("payType"));
        paramBean.setNumber(Integer.parseInt(map.get("number")));
        paramBean.setCycleCount(Integer.parseInt(map.get("cycleCount")));
        paramBean.setCpuSize(cpuSize);
        paramBean.setRamCapacity(ramCapacity/1024);
        String volType = map.get("volType")==null?"":map.get("volType").toString();
        if("1".equals(volType)){
            paramBean.setSysDiskOrdinary(sysDiskCapacity);
        }else if("2".equals(volType)){
            paramBean.setSysDiskBetter(sysDiskCapacity);
        }else if("3".equals(volType)){
            paramBean.setSysDiskBest(sysDiskCapacity);
        }else{
            paramBean.setSysDiskCapacity(sysDiskCapacity);
        }
        if(null!=map.get("imageId") &&!"".equals(map.get("imageId"))){
            String imageId = queryChargeImageId(map.get("imageId"));
            paramBean.setImageId(imageId);
        }
        BigDecimal price = billingFactorService.getPriceByFactor(paramBean);
        return price;
    }

    /**
	 * 组织订单参数
	 * **/
	private Order organizOrder(Map<String , String> map ,String userId,String cusId){
		//map.get("totalPay") :续费的总价格  //map.get("accountPay") 余额支付的金额
		BaseCloudVm baseVm = cloudVmDao.findOne(map.get("vmId").toString());
		
		Order order = new Order();
		order.setOrderType(OrderType.RENEW);
		order.setProdName("云主机-续费");
		order.setDcId(baseVm.getDcId());
		order.setProdCount(1);
		StringBuffer buf = new StringBuffer();
		buf.append("数据中心："+map.get("dcName")+"<br>");
		buf.append("云主机ID："+baseVm.getVmId()+"<br>");
		buf.append("云主机名称："+baseVm.getVmName()+"<br>");
		buf.append("主机规格："+map.get("cpuSize")+"核/"+map.get("ramCapacity")+"GB"+"<br>");
		buf.append("系统盘："+(null!=map.get("volumeTypeAs")&&!"null".equals(map.get("volumeTypeAs"))?map.get("volumeTypeAs"):"")+map.get("sysDiskCapacity")+"GB"+"<br>");
		buf.append("系统："+map.get("sysType"));
		order.setProdConfig(buf.toString());
		order.setPayType(PayType.PAYBEFORE);
		order.setBuyCycle(Integer.parseInt(map.get("buyCycle").toString()));
		// 这个单价需要调接口查一下，不能直接前台传
		order.setUnitPrice(new BigDecimal(map.get("totalPay").toString()));
		order.setResourceType(ResourceType.VM);
		order.setPaymentAmount(new BigDecimal(map.get("totalPay").toString()));
		order.setAccountPayment(new BigDecimal(map.get("accountPay").toString()));
		order.setThirdPartPayment(new BigDecimal(map.get("totalPay").toString()).subtract(new BigDecimal(map.get("accountPay").toString())));
		
		JSONObject params = new JSONObject();
	    params.put("resourceId", map.get("vmId"));
	    params.put("resourceName", baseVm.getVmName());
	    params.put("resourceType", ResourceType.VM);
	    params.put("expirationDate", baseVm.getEndTime());
	    params.put("duration", map.get("buyCycle"));
	    params.put("operatorIp", map.get("operatorIp"));
	    order.setParams(params.toJSONString());
		
		order.setCusId(cusId);
		order.setUserId(userId);
		return order;
	}
	/**
	 * 创建订单后，回写业务信息
	 * **/
	private BaseCloudVm saveOrUpdateCloudOrderVm(Order order,String vmId,String cusId,String createUser){
		BaseCloudVm baseVm = cloudVmDao.findOne(vmId);
		CloudOrderVm orderVm = new CloudOrderVm();
		orderVm.setOrderNo(order.getOrderNo());
		orderVm.setVmId(vmId);
		orderVm.setDcId(baseVm.getDcId());
		orderVm.setPrjId(baseVm.getPrjId());
		orderVm.setCount(1);
		orderVm.setCusId(cusId);
		orderVm.setCreateOrderDate(order.getCreateTime());
		orderVm.setCreateUser(createUser);
		orderVm.setOrderType(order.getOrderType());
		orderVm.setPayType(baseVm.getPayType());
		cloudOrderVmService.saveOrUpdate(orderVm);
		return baseVm;
	}
	
	/**
	 * 查询购买类型
	 * 
	 * @return
	 */
	public List<SysDataTree> queryBuyCycleType(){
		String parentId = ConstantClazz.DICT_CLOUD_BUYCYCLE_NODE_ID ;
		return DictUtil.getDataTreeByParentId(parentId);
	}
	
	/**
	 * 查询购买周期时长
	 * @param nodeId
	 * @return
	 */
	public List<SysDataTree> queryBuyCycleList(String nodeId){
		return DictUtil.getDataTreeByParentId(nodeId);
	}
	
	/**
	 * 根据订单编号查询订单信息
	 * @param orderNo
	 * @return
	 */
	public CloudOrderVm queryCloudOrderByOrderNo(String orderNo){
		return cloudOrderVmService.getByOrder(orderNo);
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
    	
    	return cloudVmDao.find(sql.toString(), new Object[]{subnet.getNetId(),subnet.getSubnetType()});
    	
    }
    
    /**
	 * 查询云主机计费因子价格（乘以数量）和总价
	 * @param paramBean
	 * @return
	 */
	public PriceDetails getPriceDetails(ParamBean paramBean){
		PriceDetails priceDetails = billingFactorService.getPriceDetails(paramBean);
		BigDecimal number = new BigDecimal(paramBean.getNumber());
		BigDecimal total = priceDetails.getTotalPrice();
		BigDecimal specPrice = new BigDecimal(0);
		BigDecimal sysDiskPrice = new BigDecimal(0);
		BigDecimal dataDiskPrice = new BigDecimal(0);
		BigDecimal imagePrice = new BigDecimal(0);
		BigDecimal ipPrice = new BigDecimal(0);
		if(null != priceDetails.getCpuPrice() && null != priceDetails.getRamPrice()){
			specPrice = number.multiply(priceDetails.getCpuPrice().add(priceDetails.getRamPrice()));
		}
		if(null != priceDetails.getSysDiskPrice()){
			sysDiskPrice = number.multiply(priceDetails.getSysDiskPrice());
		}
		if(null != priceDetails.getDataDiskPrice()){
			dataDiskPrice = number.multiply(priceDetails.getDataDiskPrice());
		}
		if(null != priceDetails.getImagePrice()){
			imagePrice = number.multiply(priceDetails.getImagePrice());
		}
		if(null != priceDetails.getIpPrice()){
			ipPrice = number.multiply(priceDetails.getIpPrice());
		}
		if(paramBean.getPayType().equals(PayType.PAYAFTER)){
			total = handleMinValue(total);
			specPrice = handleMinValue(specPrice);
			sysDiskPrice = handleMinValue(sysDiskPrice);
			if(null != priceDetails.getImagePrice()){
				imagePrice = handleMinValue(imagePrice);
			}
			if(null != priceDetails.getIpPrice()){
				ipPrice = handleMinValue(ipPrice);
			}
		}
		
		priceDetails.setTotalPrice(total.setScale(2, RoundingMode.FLOOR));
		if(null !=priceDetails.getCpuPrice() && null != priceDetails.getRamPrice()){
			priceDetails.setCpuPrice(specPrice.setScale(2, RoundingMode.FLOOR));
		}
		if(null != priceDetails.getSysDiskPrice()){
			priceDetails.setSysDiskPrice(sysDiskPrice.setScale(2, RoundingMode.FLOOR));
		}
		if(null != priceDetails.getDataDiskPrice()){
			priceDetails.setDataDiskPrice(dataDiskPrice.setScale(2, RoundingMode.FLOOR));
		}
		if(null != priceDetails.getImagePrice()){
			priceDetails.setImagePrice(imagePrice.setScale(2, RoundingMode.FLOOR));
		}
		if(null != priceDetails.getIpPrice()){
			priceDetails.setIpPrice(ipPrice.setScale(2, RoundingMode.FLOOR));
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
	 * 查询该订单对应的升级或续费资源是否存在
	 * -----------------------
	 * @author zhouhaitao
	 * @param orderNo
	 * @return
	 */
	public boolean isExistsByOrderNo(String orderNo){
		boolean isExist = false;
		StringBuffer sql = new StringBuffer();
		
		sql.append("		SELECT                       ");
		sql.append("			vm.vm_id,                ");
		sql.append("			vm.vm_name               ");
		sql.append("		FROM                         ");
		sql.append("			cloud_vm vm              ");
		sql.append("		LEFT JOIN cloudorder_vm cov  ");
		sql.append("		ON cov.vm_id = vm.vm_id      ");
		sql.append("		AND (                        ");
		sql.append("			cov.order_type = '1'     ");
		sql.append("			OR cov.order_type = '2'  ");
		sql.append("		)                            ");
		sql.append("		WHERE                        ");
		sql.append("			order_no = ?             ");
		sql.append("		AND vm.is_deleted = '0'      ");
		sql.append("		AND vm.is_visable = '1'      ");
		
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[]{orderNo});
		@SuppressWarnings("rawtypes")
		List listResult = query.getResultList();
		if(null != listResult && listResult.size() == 1) {
			isExist = true;
		}
		
		return isExist;
	}
	
	/**
	 * 查询资源是否存在的接口
	 * 
	 * @author zhouhaitao
	 * @param resId
	 * @return
	 */
	public ResourceCheckBean isExistsByResourceId(String resId){
		ResourceCheckBean bean = new ResourceCheckBean();
		boolean isExist = false;
		StringBuffer sql = new StringBuffer();
		
		sql.append("		SELECT                       ");
		sql.append("			vm.vm_id,                ");
		sql.append("			vm.vm_name               ");
		sql.append("		FROM                         ");
		sql.append("			cloud_vm vm              ");
		sql.append("		WHERE                        ");
		sql.append("			vm.vm_id = ?             ");
		sql.append("		AND vm.is_deleted = '0'      ");
		sql.append("		AND vm.is_visable = '1'      ");
		
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[]{resId});
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
	 * 查询已在回收站过期的云主机列表
	 * 
	 * @author zhouhaitao
	 * @param seconds
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public List<CloudVm> queryRecycleVmList(long seconds){
		StringBuffer sql = new StringBuffer();
		List<CloudVm> vmList = new ArrayList<CloudVm>();
		sql.append("		SELECT                    ");
		sql.append("			vm.dc_id,             ");
		sql.append("			vm.prj_id,            ");
		sql.append("			vm.vm_id,             ");
		sql.append("			vm.vm_name            ");
		sql.append("		FROM                      ");
		sql.append("			cloud_vm vm           ");
		sql.append("		WHERE                     ");
		sql.append("			TIMESTAMPDIFF(        ");
		sql.append("				SECOND,           ");
		sql.append("				vm.delete_time,   ");
		sql.append("				NOW()             ");
		sql.append("			) > ?                 ");
		sql.append("		AND vm.is_deleted = '2'   ");
		
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[]{seconds});
		
		List list = query.getResultList();
		if(null != list &&  list.size()>0){
			for(int i =0 ;i<list.size();i++){
				int index = 0;
				Object [] objs = (Object []) list.get(i);
				CloudVm cloudVm = new CloudVm();
				
				cloudVm.setDcId(String.valueOf(objs[index++]));
				cloudVm.setPrjId(String.valueOf(objs[index++]));
				cloudVm.setVmId(String.valueOf(objs[index++]));
				cloudVm.setVmName(String.valueOf(objs[index++]));
				
				vmList.add(cloudVm);
			}
		}
		return vmList;
	}
	
	/**
	 * 查询主机是否有正在处理的订单
	 * 
	 * @author zhouhaitao
	 * @param vmId
	 * @return
	 */
	public boolean checkVmOrderExsit(String vmId){
		return checkVmOrderExsit(vmId, true, true);
	}
	
	
	/**
	 * 查询指定镜像创建的云主机个数（包括底层同步上来的）
	 */
	public int countVmByImageId(String imageId) {
		int count=0;
		count=cloudVmDao.countVmByImageId(imageId);
		return count;
	}
	/**
	 * 购买云主机时同时也购买数据盘生成cloudorder_volume记录
	 * @author chengxiaodong
	 * @throws Exception 
	 */
	private boolean createVolumeOrder(CloudOrderVm cloudOrder) throws Exception{
		CloudOrderVolume volumeOrder=new CloudOrderVolume();
		volumeOrder.setDcId(cloudOrder.getDcId());
		volumeOrder.setDcName(cloudOrder.getDcName());
		volumeOrder.setDiskFrom("blank");
		volumeOrder.setBuyCycle(cloudOrder.getBuyCycle());
		volumeOrder.setCreateOrderDate(cloudOrder.getCreateOrderDate());
		
		volumeOrder.setCreateUser(cloudOrder.getCreateUser());
		volumeOrder.setCusId(cloudOrder.getCusId());
		volumeOrder.setOrderNo(cloudOrder.getOrderNo());
		volumeOrder.setOrderType(cloudOrder.getOrderType());
		volumeOrder.setPrjId(cloudOrder.getPrjId());
		volumeOrder.setVolNumber(cloudOrder.getCount());
		volumeOrder.setVolSize(cloudOrder.getDataDisk());
		volumeOrder.setVolTypeId(cloudOrder.getDataTypeId());
		volumeOrder.setPayType(cloudOrder.getPayType());
		volumeOrder.setBuyCycle(cloudOrder.getBuyCycle());
		
		try {
			cloudOrderVolumeService.addOrderVolume(volumeOrder);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
		return true;
		
	}

	/**
	 * 购买云主机并且购买数据盘失败
	 */
	@Override
	public void createFailVmsAndVolumes(CloudOrderVm order,
			List<BaseCloudVm> vmList, List<BaseCloudVolume> volList){
		try {
			//调用创建资源创建失败的接口
			orderService.completeOrder(order.getOrderNo(), false,null);
			messageCenterService.addResourFailMessage(order.getOrderNo(), order.getCusId());
			if ("1".equals(order.getBuyFloatIp())) {
				floatIpService.releaseFloatIpByOrderNo(order.getOrderNo());
			}
			if (null != vmList && vmList.size() > 0) {
				vmFailedHandler(order);
			}
			//数据盘创建失败
			if(null != volList && volList.size() > 0){
				volumeService.volumeFailedHandler(order);
			}
		} catch (Exception e) {
			deleteFailedHandler(order);
			log.error(e.getMessage(),e);
		} 
		
	}
	
	

	/**
	 * 云主机购买数据盘成功
	 * @param orderVm
	 * @param floatIpList
	 * @param vmList
	 * @param volList
	 * @throws Exception
	 */
	public void allVmAndVolumesSuccessHnadler(CloudOrderVm orderVm, List<CloudFloatIp> floatIpList, List<BaseCloudVm> vmList,List<BaseCloudVolume> volList) throws Exception {
		JSONObject json = new JSONObject();
		JSONObject volJson = new JSONObject();
		List<String> floatIds = new ArrayList<String>();
		List<String> vmIds = new ArrayList<String>();
		List<String> volIds = new ArrayList<String>();
		CloudOrderVolume orderVolume=null;
		//发送订单资源创建成功,返回订单完成时间
		List<BaseOrderResource> resourceList = new ArrayList<BaseOrderResource>();
		for(CloudFloatIp ip:floatIpList){
			BaseOrderResource resource = new BaseOrderResource();
			resource.setOrderNo(orderVm.getOrderNo());
			resource.setResourceId(ip.getFloId());
			resource.setResourceName(ip.getFloIp());

			resourceList.add(resource);
		}
		for(BaseCloudVm vm :vmList){
			BaseOrderResource resource = new BaseOrderResource();
			resource.setOrderNo(orderVm.getOrderNo());
			resource.setResourceId(vm.getVmId());
			resource.setResourceName(vm.getVmName());
			
			resourceList.add(resource);
		}
		
		for(BaseCloudVolume volume:volList){
			BaseOrderResource resource = new BaseOrderResource();
			resource.setOrderNo(orderVm.getOrderNo());
			resource.setResourceId(volume.getVolId());
			resource.setResourceName(volume.getVolName());
			
			resourceList.add(resource);
		}
		
		BaseOrder order = orderService.completeOrder(orderVm.getOrderNo(), true,resourceList);
		orderVm.setOrderCompleteDate(order.getCompleteTime());
		Date completeDate = null;
		if(PayType.PAYBEFORE.equals(orderVm.getPayType())){
			completeDate = order.getResourceExpireTime();
		}
		else if(PayType.PAYAFTER.equals(orderVm.getPayType())){
			floatIpService.sendMessage(floatIpList, EayunQueueConstant.QUEUE_BILL_RESOURCE_PURCHASE, orderVm.getCusId(), orderVm.getOrderNo());
		}

		for (CloudFloatIp floatIp : floatIpList) {
			floatIds.add(floatIp.getFloId());
		}
		
		if(null!=volList){
			 orderVolume=new CloudOrderVolume();
			 orderVolume.setOrderNo(orderVm.getOrderNo());
			 orderVolume.setOrderType(orderVm.getOrderType());
			 orderVolume.setCreateUser(orderVm.getCreateUser());
			 orderVolume.setCusId(orderVm.getCusId());
		}
		for (BaseCloudVolume volume:volList) {
			volIds.add(volume.getVolId());
			if(PayType.PAYAFTER.equals(orderVm.getPayType())){
				volumeService.volStartCharge(orderVolume,volume);
			}
		}
		
		CloudVolumeType type=volTypeService.getVolumeTypeById(orderVm.getDcId(), orderVm.getSysTypeId());
		
		for (BaseCloudVm vm : vmList) {
			vmIds.add(vm.getVmId());
			if(PayType.PAYAFTER.equals(orderVm.getPayType())){
				if(null!=type&&!"".equals(type.getTypeId())){
					orderVm.setSysDiskType(type.getVolumeType());
				}
				vmPurchaseCharge(orderVm, vm);
			}
		}
		
		if (floatIds.size() > 0) {
			json.put("floatIp", floatIds);
		}
		if (vmIds.size() > 0) {
			json.put("vm", vmIds);
		}
		if(volIds.size()>0){
			volJson.put("volume",volIds);
		}
		
		cloudOrderVmService.updateOrderResources(orderVm.getOrderNo(), json.toJSONString());
		
		cloudOrderVmService.modifyResourceForVisable(vmIds,floatIds,completeDate);
		
		cloudOrderVolumeService.updateOrderResources(orderVm.getOrderNo(),volJson.toJSONString());
		
		volumeService.modifyVolVisableByOrder(orderVm.getOrderNo());
		
		cloudBatchResourceService.deleteByOrder(orderVm.getOrderNo());
	}
	
	/**
	 * <p>查询项目下未关联制定云主机的SSH密钥列表</p>
	 * ------------------------
	 * @author zhouhaitao
	 * 
	 * @param prjId			项目ID
	 * @param vmId			云主机ID
	 * @return
	 */
	public List<CloudSecretKey> getUnbindSecretkeyByPrj(String prjId,String vmId){
		List<CloudSecretKey> list = new ArrayList<CloudSecretKey>();
		StringBuffer sql = new StringBuffer();
		
		sql.append("			SELECT                            ");
		sql.append("				cs.secretkey_id,              ");
		sql.append("				cs.secretkey_name             ");
		sql.append("			FROM                              ");
		sql.append("				cloud_secretkey cs            ");
		sql.append("			WHERE                             ");
		sql.append("				cs.prj_id = ?                 ");
		sql.append("			AND cs.secretkey_id NOT IN (      ");
		sql.append("				SELECT secretkey_id FROM      ");
		sql.append("				secretkey_vm WHERE vm_id = ?  ");
		sql.append("			)                                 ");
		sql.append("			order by cs.create_time desc      ");
		
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[]{prjId,vmId});
		@SuppressWarnings("rawtypes")
		List result = query.getResultList();
		if(null != result && result.size() > 0 ){
			for(int i = 0; i<result.size(); i++ ){
				int index = 0;
				CloudSecretKey csk = new CloudSecretKey();
				Object [] objs = (Object [])result.get(i);
				
				csk.setSecretkeyId(String.valueOf(objs[index++]));
				csk.setSecretkeyName(String.valueOf(objs[index++]));
				
				list.add(csk);
			}
			return list;
		}
		return null;
	}
	
	/**
	 * <p>查询云主机关联制定云主机的SSH密钥列表</p>
	 * ------------------------
	 * @author zhouhaitao
	 * 
	 * @param vmId			云主机ID
	 * @return
	 */
	public List<CloudSecretKey> getBindSecretkeyByVm(String vmId){
		List<CloudSecretKey> list = new ArrayList<CloudSecretKey>();
		StringBuffer sql = new StringBuffer();
		
		sql.append("			SELECT                                     ");
		sql.append("				cs.secretkey_id,                       ");
		sql.append("				cs.secretkey_name                      ");
		sql.append("			FROM                                       ");
		sql.append("				secretkey_vm sv                        ");
		sql.append("			LEFT JOIN cloud_secretkey cs               ");
		sql.append("				ON sv.secretkey_id = cs.secretkey_id   ");
		sql.append("			WHERE   vm_id = ?                          ");
		
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[]{vmId});
		@SuppressWarnings("rawtypes")
		List result = query.getResultList();
		if(null != result && result.size() > 0 ){
			for(int i = 0; i<result.size(); i++ ){
				int index = 0;
				CloudSecretKey csk = new CloudSecretKey();
				Object [] objs = (Object [])result.get(i);
				
				csk.setSecretkeyId(String.valueOf(objs[index++]));
				csk.setSecretkeyName(String.valueOf(objs[index++]));
				
				list.add(csk);
			}
			return list;
		}
		return null;
	}
	
	/**
	 * <p>绑定/解绑SSH密钥</p>
	 * -------------------------
	 * @author zhouhaitao
	 * 
	 * @param cloudVm
	 */
	@SuppressWarnings("unchecked")
	public void editSecretKey(CloudVm cloudVm){
		CloudVm cv = queryRouteByVm(cloudVm.getVmId());
		if(null != cv && !"SHUTOFF".equals(cv.getVmStatus())){
			throw new AppException("请将云主机关机");
		}
		if(null != cv && StringUtil.isEmpty(cv.getRouteId())){
			throw new AppException("请将云主机所在子网连接路由");
		}
		
		Set<String> usedCsks = new HashSet<String>();
		Set<String> csks = cloudVm.getCsks();
		Set<String> addSets = new HashSet<String>();
		Set<String> deleteSets = new HashSet<String>();
		
		List<CloudSecretKey> list = getBindSecretkeyByVm(cloudVm.getVmId());
		if(null != list && list.size()>0){
			for(CloudSecretKey key : list){
				usedCsks.add(key.getSecretkeyId());
				
				if(!csks.contains(key.getSecretkeyId())){
					BaseCloudSecretKey  bcs = secretKeyService.getSecretKeyById(key.getSecretkeyId());
					deleteSets.add(bcs.getPublicKey());
					cloudVm.setSshDelCount(cloudVm.getSshDelCount()+1);
				}
			}
		}
		
		if(csks !=null && csks.size() > 0){
			for(String key : csks){
				if(!usedCsks.contains(key)){
					BaseCloudSecretKey  bcs = secretKeyService.getSecretKeyById(key); 
					addSets.add(bcs.getPublicKey());
					cloudVm.setSshAddCount(cloudVm.getSshAddCount()+1);
				}
			}
		}
		
		if(addSets.size() == 0&& deleteSets.size() == 0){
			return;
		}
		String userData = openstackSecretKeyService.
				getVmUserData(cloudVm.getDcId(), cloudVm.getPrjId(), cloudVm.getVmId());
		
		Yaml yaml = new Yaml();
		Map<String,Object> userDataMap = yaml.loadAs(userData,Map.class);
		
		List<String> auth_keys = (List<String>) userDataMap.get("ssh_authorized_keys");
		List<String> deletd_auth_keys = (List<String>) userDataMap.get("ssh_authorized_keys_deleted");
		
		if(null == auth_keys ){
			auth_keys = new ArrayList<String>();
		}
		if(null == deletd_auth_keys){
			deletd_auth_keys = new ArrayList<String>();
		}
		if(null != deleteSets && deleteSets.size()>0){
			deletd_auth_keys.addAll(deleteSets);
			auth_keys.removeAll(deleteSets);
		}
		if(null != addSets && addSets.size()>0){
			auth_keys.addAll(addSets);
			deletd_auth_keys.removeAll(addSets);
		}
		userDataMap.put("ssh_authorized_keys", auth_keys);
		userDataMap.put("ssh_authorized_keys_deleted", deletd_auth_keys);
		
		String user_data = yaml.dumpAsMap(userDataMap);
		
		JSONObject data = new JSONObject();
		data.put("user_data", Base64Utils.encodeToString(user_data.getBytes()));
		openstackSecretKeyService.bindSecretKey(cloudVm.getDcId(), cloudVm.getPrjId(),
				cloudVm.getVmId(), data);
		
		secretkeyVmService.deleteByVm(cloudVm.getVmId());
		for(String str: csks){
			BaseSecretkeyVm bsv = new BaseSecretkeyVm();
			
			bsv.setVmId(cloudVm.getVmId());
			bsv.setSecretkeyId(str);
			
			secretkeyVmService.saveOrUpdate(bsv);
		}
	}
	
	/**
	 * <p>查询云主机链接路由的情况</p>
	 * ------------------------------
	 * @author zhouhaitao
	 * @param vmId
	 * @return
	 */
	public CloudVm queryRouteByVm(String vmId){
		StringBuffer sql = new StringBuffer();
		
		sql.append("		SELECT                             ");
		sql.append("			cv.vm_id,                      ");
		sql.append("			cv.vm_status,                  ");
		sql.append("			cv.subnet_id,                  ");
		sql.append("			cs.route_id                    ");
		sql.append("		FROM                               ");
		sql.append("			cloud_vm cv                    ");
		sql.append("		LEFT JOIN cloud_subnetwork cs      ");
		sql.append("		ON cv.subnet_id = cs.subnet_id     ");
		sql.append("		WHERE vm_id = ?                    ");
		
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object []{vmId});
		@SuppressWarnings("rawtypes")
		List list = query.getResultList();
		if(list != null && list.size() == 1){
			CloudVm cloudVm = new CloudVm();
			Object [] objs = (Object [])list.get(0);
			
			cloudVm.setVmId(String.valueOf(objs[0]));
			cloudVm.setVmStatus(String.valueOf(objs[1]));
			cloudVm.setSubnetId(objs[2] != null ? String.valueOf(objs[2]):null);
			cloudVm.setRouteId(objs[3] != null ? String.valueOf(objs[3]):null);
			
			return cloudVm;
		}
		return null;
	}
	
	/**
	 * <p>修改云主机密码</p>
	 * ------------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 */
	public void modifyPwd(CloudVm cloudVm){
		JSONObject data = new JSONObject();
		CloudVm cvm = queryRouteByVm(cloudVm.getVmId());
		if(null != cvm && StringUtil.isEmpty(cvm.getSubnetId())){
			throw new AppException("请将主机加入受管子网，同时请确保受管子网连接到了路由");
		}
		else if(StringUtil.isEmpty(cvm.getRouteId())){
			throw new AppException("请将主机所在的受管子网连接路由后，再修改密码");
		}
		CloudVm cv = queryVmById(cloudVm.getVmId(), cloudVm.getPrjId());
		cloudVm.setOsType(cv.getOsType());

		if(null != cv && ConstantClazz.DICT_CLOUD_OS_LINUX_NODE_ID.equals(cv.getOsType())){
			String userData = openstackSecretKeyService.
					getVmUserData(cloudVm.getDcId(), cloudVm.getPrjId(), cloudVm.getVmId());
			
			Yaml yaml = new Yaml();
			@SuppressWarnings("unchecked")
			Map<String,Object> userDataMap = yaml.loadAs(userData,Map.class);
			
			userDataMap.put("password", cloudVm.getPassword());
			String user_data = yaml.dumpAsMap(userDataMap);
			
			data.put("user_data", Base64Utils.encodeToString(user_data.getBytes()));
		}
		openstackVmService.modifyVmPassword(cloudVm, data);
	}
	
}