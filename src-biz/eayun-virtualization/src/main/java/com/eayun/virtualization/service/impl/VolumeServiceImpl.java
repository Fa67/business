package com.eayun.virtualization.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Query;

import com.eayun.virtualization.model.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.charge.bean.ResourceCheckBean;
import com.eayun.charge.model.ChargeRecord;
import com.eayun.common.constant.BillingCycleType;
import com.eayun.common.constant.EayunQueueConstant;
import com.eayun.common.constant.OrderType;
import com.eayun.common.constant.PayType;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.CloudResourceUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.Hook;
import com.eayun.costcenter.model.MoneyAccount;
import com.eayun.costcenter.service.AccountOverviewService;
import com.eayun.customer.model.Customer;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.Volume;
import com.eayun.eayunstack.service.OpenstackVolumeService;
import com.eayun.eayunstack.service.RestService;
import com.eayun.notice.model.MessageOrderResourceNotice;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.order.model.BaseOrder;
import com.eayun.order.model.BaseOrderResource;
import com.eayun.order.model.Order;
import com.eayun.order.service.OrderService;
import com.eayun.price.bean.ParamBean;
import com.eayun.price.bean.UpgradeBean;
import com.eayun.price.service.BillingFactorService;
import com.eayun.project.service.ProjectService;
import com.eayun.syssetup.service.SysDataTreeService;
import com.eayun.virtualization.baseservice.BaseVolumeService;
import com.eayun.virtualization.dao.CloudImageDao;
import com.eayun.virtualization.dao.CloudSnapshotDao;
import com.eayun.virtualization.dao.CloudVolumeDao;
import com.eayun.virtualization.service.CloudBatchResourceService;
import com.eayun.virtualization.service.SnapshotService;
import com.eayun.virtualization.service.TagService;
import com.eayun.virtualization.service.VolumeOrderService;
import com.eayun.virtualization.service.VolumeService;
import com.eayun.virtualization.service.VolumeTypeService;

@Service
@Transactional
public class VolumeServiceImpl extends BaseVolumeService implements VolumeService {
    private final static Logger log = LoggerFactory.getLogger(VolumeServiceImpl.class);
    @Autowired
	private OpenstackVolumeService volumeService;
	@Autowired
	private RestService restService;
	@Autowired
	private TagService tagService;
	@Autowired
	private VolumeOrderService volOrderService;
	@Autowired
	private SnapshotService snapService;
	@Autowired
	private CloudVolumeDao volumeDao;
	@Autowired
	private CloudSnapshotDao snapDao;
	@Autowired
	private CloudImageDao cloudImageDao ;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private OrderService orderService;
	@Autowired
	private EayunRabbitTemplate rabbitTemplate;
	@Autowired
	private CloudBatchResourceService cloudBatchResourceService;
	@Autowired
	private AccountOverviewService accountOverviewSerivce;
	@Autowired
	private BillingFactorService billingFactorService;
	@Autowired
	private SysDataTreeService sysDataTreeService;
	@Autowired 
	private MessageCenterService messageCenterService;
	@Autowired
	private CustomerService customerService;
	@Autowired
	private VolumeTypeService volTypeService;
	
	
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
	public Page getVolumeList(Page page, String projectId, String datacenterId,
			String name,String type,String isDeleted,String volStatus, QueryMap queryMap) throws Exception {
			
			int index=0;
			Object [] args=new Object[4];
			StringBuffer sql=new StringBuffer();
			sql.append("select vol.vol_id AS volId,vol.vol_name AS volName,vol.vol_status AS volStatus,vol.vol_size AS volSize,vol.disk_from AS diskFrom,vol.create_time AS createTime,vol.dc_id as dcId,vol.prj_id as prjId,prj.prj_name as prjName,vol.vm_id as vmId,vm.vm_name AS vmName,vol.vol_description as volDescription,vol.bind_point as bindPoint,count(snap.snap_id) as SnapNum,vm.vm_status as vmStatus,vol.vol_bootable as volBootable,vm.os_type as osType,vol.pay_type as payType,vol.end_time as endTime,vol.charge_state as chargeState,dc.dc_name as dcName ,");
			sql.append(" vol.vol_typeid as volTypeId,type.volume_type as volType,type.max_size as maxSize from cloud_volume vol");
			sql.append(" left join cloud_vm vm ON vol.vm_id=vm.vm_id");
			sql.append(" left join cloud_project prj ON vol.prj_id=prj.prj_id");
			sql.append(" left join dc_datacenter dc ON vol.dc_id=dc.id");
			sql.append(" left join cloud_volumetype type ON vol.vol_typeid=type.type_id");
			sql.append(" left join cloud_disksnapshot snap ON vol.vol_id = snap.vol_id");
			sql.append(" left join sys_tagresource tagr ON vol.vol_id=tagr.tgres_resourceid");
			sql.append(" left join sys_tag tag ON tagr.tgres_tagid=tag.tg_id");
			sql.append(" where  vol.is_visable='1' ");
			
			if (!StringUtils.isEmpty(volStatus) && !volStatus.equals("null")) {
				if ("1".equals(volStatus)) {
					sql.append(" and vol.charge_state = ? ");
				} else if ("2".equals(volStatus)) {
					sql.append(" and (vol.charge_state = '3' or vol.charge_state = ?) ");
				} else {
						sql.append(" and vol.vol_status = ? ");
						sql.append(" and vol.charge_state = '0' ");
					
				}
				args[index] = volStatus;
				index++;
			}
			if(null!=isDeleted&&!"".equals(isDeleted)){
				sql.append(" and vol.is_deleted=?");
				args[index]=isDeleted;
				index++;
			}
			if (!"null".equals(projectId)&&null!=projectId&&!"".equals(projectId)&&!"undefined".equals(projectId)) {
				sql.append(" and vol.prj_id=?");
				args[index]=projectId;
				index++;
			}
			if (!"null".equals(datacenterId)&&null!=datacenterId&&!"".equals(datacenterId)&&!"undefined".equals(datacenterId)) {
				sql.append(" and vol.dc_id=?");
				args[index]=datacenterId;
				index++;
			}
			if(null!=name&&!"".equals(name)){
					if(!"".equals(type)&&null!=type&&"name".equals(type)){
						name = name.replaceAll("\\_", "\\\\_");
				    	sql.append(" and binary vol.vol_name like ?");
				    	args[index]="%"+name+"%";
						index++;
					}
					if(!"".equals(type)&&null!=type&&"tag".equals(type)){
						sql.append(" and vol.vol_id  in ("+handleQueryTagCondition(name)+") ");
					}
			}
			sql.append(" group by vol.vol_id order by vol.create_time desc");
			
			Object[] params = new Object[index];  
	        System.arraycopy(args, 0, params, 0, index);
			page = volumeDao.pagedNativeQuery(sql.toString(),queryMap,params);
			 List newList = (List)page.getResult();
		        for(int i=0;i<newList.size();i++){
		        	Object[] objs = (Object[])newList.get(i);
		        	CloudVolume volume=new CloudVolume();
		        	volume.setVolId(String.valueOf(objs[0]));
		        	volume.setVolName(String.valueOf(objs[1]));
		        	volume.setVolStatus(String.valueOf(objs[2]));
		        	volume.setVolSize(Integer.parseInt(String.valueOf(objs[3])));
		        	volume.setDiskFrom(String.valueOf(objs[4]));
		        	volume.setCreateTime((Date)objs[5]);
		        	volume.setDcId(String.valueOf(objs[6]));
		        	volume.setPrjId(String.valueOf(objs[7]));
		        	volume.setPrjName(String.valueOf(objs[8]));
		        	volume.setVmId(String.valueOf(objs[9]));
		        	volume.setVmName(String.valueOf(objs[10]));
		        	volume.setVolDescription(String.valueOf(objs[11]));
		        	volume.setBindPoint(String.valueOf(objs[12]));
		        	volume.setSnapNum(String.valueOf(objs[13]));
		        	volume.setVmStatus(String.valueOf(objs[14]));
		        	volume.setVolBootable(String.valueOf(objs[15]));
		        	String vmOsType=String.valueOf(objs[16]);
		        	volume.setPayType(String.valueOf(objs[17]));
		        	volume.setEndTime((Date)objs[18]);
		        	volume.setChargeState(String.valueOf(objs[19]));
		        	volume.setDcName(String.valueOf(objs[20]));
		        	volume.setVolTypeId(String.valueOf(objs[21]));
		        	volume.setVolType(String.valueOf(objs[22]));
		        	volume.setMaxSize(Integer.parseInt(null!=objs[23]?String.valueOf(objs[23]):"2048"));
		        	String volumeTypeAs=getVolumeTypeForDis(String.valueOf(objs[22]));
		        	volume.setVolumeTypeAs(volumeTypeAs);
		        	
		        	
		        	if(!"DELETING".equals(volume.getVolStatus())){
		        		volume.setStatusForDis(CloudResourceUtil.escapseChargeState(volume.getChargeState()));
		        	}else{
		        		volume.setStatusForDis(DictUtil.getStatusByNodeEn("volume", volume.getVolStatus()));
		        	}
		        	if(null!=vmOsType&&"0007002002001".equals(vmOsType)){
		        		volume.setBindPoint(null);
		        	}
		        	if (null==volume.getChargeState()||"".equals(volume.getChargeState())||"null".equals(volume.getChargeState())||CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(volume.getChargeState())) {
		        		volume.setStatusForDis(DictUtil.getStatusByNodeEn("volume", volume.getVolStatus()));
					}
		        	if("1".equals(volume.getVolBootable())&&"DELETING".equals(volume.getVolStatus())){
		    			volume.setVmId(null);
		    			volume.setVmName(null);
		    		}
		        	newList.set(i, volume);
		        }
	    return page;
		
	}
	
	
	/**
	 * 购买云硬盘并提交订单
	 */
	@Override
	public String buyVolumes(CloudOrderVolume orderVolume,SessionUserInfo sessionUser) throws Exception {
		String errMsg = null;
        try {
        	
        	if(null!=orderVolume.getFromVolId()&&!"".equals(orderVolume.getFromVolId())&&!"null".equals(orderVolume.getFromVolId())){
        		orderVolume.setFromVolId(null);
        	}
        	if(null!=orderVolume.getVolId()&&!"".equals(orderVolume.getVolId())&&!"null".equals(orderVolume.getVolId())){
        		orderVolume.setVolId(null);
        	}
        	
        	orderVolume.setEndTime(DateUtil.getExpirationDate(new Date(), orderVolume.getBuyCycle(), DateUtil.PURCHASE));
        	orderVolume.setCreateUser(sessionUser.getUserName());
        	orderVolume.setCusId(sessionUser.getCusId());
        	orderVolume.setCreateOrderDate(new Date());
			errMsg = checkVolumeQuota(orderVolume);//0代表购买2代表扩容
			if(!StringUtils.isEmpty(errMsg)){
				errMsg = "OUT_OF_QUOTA";
				return errMsg;
			}
			if(PayType.PAYBEFORE.equals(orderVolume.getPayType())){
				BigDecimal totalPayment = calcVolumePrice(orderVolume);
				if(totalPayment.compareTo(orderVolume.getPaymentAmount()) !=0){
					errMsg = "CHANGE_OF_BILLINGFACTORY";
					return errMsg;
				}
				
			}
			
			if(PayType.PAYAFTER.equals(orderVolume.getPayType())){
				MoneyAccount accountMoney = accountOverviewSerivce.getAccountInfo(sessionUser.getCusId());
				//TODO 开通服务余额限定值
				String buyCondition = sysDataTreeService.getBuyCondition();
				BigDecimal createResourceLimitedMoney = new BigDecimal(buyCondition);
				if(accountMoney.getMoney().compareTo(createResourceLimitedMoney)<0){
					errMsg = "NOT_SUFFICIENT_FUNDS";
					return errMsg;
				}
			}
			
			Order order = createVolumeOrder(orderVolume, sessionUser);
			
			orderVolume.setOrderNo(order.getOrderNo());//设置订单编号
			volOrderService.addOrderVolume(orderVolume);
			
			if(PayType.PAYAFTER.equals(orderVolume.getPayType())){
				try{
					if(null==orderVolume.getFromSnapId()){
						addVolume(orderVolume);
					}else{
						addVolumeBySnapshot(orderVolume);
					}
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
	 * 扩容云硬盘
	 */
	@Override
	public String extendVolume(CloudOrderVolume orderVolume,SessionUserInfo sessionUser) throws Exception {
		String errMsg = null;
        try {
        	orderVolume.setCreateUser(sessionUser.getUserName());
        	orderVolume.setCusId(sessionUser.getCusId());
        	orderVolume.setCreateOrderDate(new Date());
			errMsg = checkVolumeQuota(orderVolume);
			if(!StringUtils.isEmpty(errMsg)){
				errMsg = "OUT_OF_QUOTA";
				return errMsg;
			}
			
			if (PayType.PAYAFTER.equals(orderVolume.getPayType())) {
				MoneyAccount accountMoney = accountOverviewSerivce.getAccountInfo(sessionUser.getCusId());
				BigDecimal zero = new BigDecimal(0);
				if (accountMoney.getMoney().compareTo(zero) <= 0) {
					errMsg = "ARREARS_OF_BALANCE";
					return errMsg;
				}
			}
			
			if(PayType.PAYBEFORE.equals(orderVolume.getPayType())){
				BigDecimal totalPayment = calcVolumePrice(orderVolume);
				if(totalPayment.compareTo(orderVolume.getPaymentAmount()) !=0){
					errMsg = "CHANGE_OF_BILLINGFACTORY";
					return errMsg;
				}
				
			}
		
			if(checkVolOrderExsit(orderVolume.getVolId())){
				errMsg = "UPGRADING_OR_INORDER";
				return errMsg;
			}
			
			Order order = createVolumeOrder(orderVolume, sessionUser);
			
			orderVolume.setOrderNo(order.getOrderNo());//设置订单编号
			volOrderService.addOrderVolume(orderVolume);
			
			if(PayType.PAYAFTER.equals(orderVolume.getPayType())){
				try{
					largeVolume(orderVolume);
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
	 * 根据备份创建云硬盘
	 */
	@Transactional(noRollbackFor=AppException.class)
	public boolean addVolumeBySnapshot(CloudOrderVolume volumeOrder)throws AppException {
		
		List<BaseCloudVolume> volList=new ArrayList<BaseCloudVolume>();

		try {
			//创建云硬盘
			JSONObject data=new JSONObject();
			JSONObject temp=new JSONObject();
			temp.put("name", volumeOrder.getVolName());
			temp.put("size", volumeOrder.getVolSize());
		    temp.put("imageRef", null);
		    if(null!=volumeOrder.getVolDescription()){
		    	temp.put("description", volumeOrder.getVolDescription());
		    }
			
			if(null!=volumeOrder.getVolTypeId()&&!"".equals(volumeOrder.getVolTypeId())){
				temp.put("volume_type", volumeOrder.getVolTypeId());
			}
			data.put("volume", temp);
			Volume result=volumeService.create(volumeOrder.getDcId(), volumeOrder.getPrjId(), data);
			BaseCloudVolume volume=new BaseCloudVolume();
			volume.setVolId(result.getId());
			volume.setVolName(result.getName());
			volume.setCreateTime(new Date());
			volume.setCreateName(volumeOrder.getCreateUser());
			volume.setDcId(volumeOrder.getDcId());
			volume.setPrjId(volumeOrder.getPrjId());
			if(true==result.getBootable()){
				volume.setVolBootable("1");
			}else{
				volume.setVolBootable("0");
			}
			volume.setDiskFrom(volumeOrder.getDiskFrom());
			volume.setVolSize(volumeOrder.getVolSize());
			volume.setVolStatus("CREATING");
			volume.setVolDescription(null!=result.getDescription()?result.getDescription():null);
			volume.setVolTypeId(volumeOrder.getVolTypeId());
			volume.setChargeState(CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE);
			volume.setIsDeleted("0");
			volume.setFromSnapId(volumeOrder.getFromSnapId());
			volume.setPayType(volumeOrder.getPayType());
			volume.setIsVisable("0");
			volume.setTypeSuccess("1");
			if("1".equals(volumeOrder.getPayType())){
				volume.setEndTime(DateUtil.getExpirationDate(volume.getCreateTime(), volumeOrder.getBuyCycle(), DateUtil.PURCHASE));
			}
			volumeDao.save(volume);
			volList.add(volume);
			
			BaseCloudBatchResource resource=new BaseCloudBatchResource();
			resource.setOrderNo(volumeOrder.getOrderNo());
			resource.setResourceId(volume.getVolId());
			resource.setResourceType(CloudBatchResource.RESOURCE_VOLUME);
			cloudBatchResourceService.save(resource);
			
			if(null!=volume){
				//TODO 同步新增云硬盘状态
				JSONObject bson =new JSONObject();
				bson.put("orderNo", volumeOrder.getOrderNo());
				bson.put("volNumber", volumeOrder.getVolNumber());
				bson.put("payType", volumeOrder.getPayType());
				bson.put("cusId",volumeOrder.getCusId());
				bson.put("createName",volumeOrder.getCreateUser());
				bson.put("createTime",volumeOrder.getCreateOrderDate());
				bson.put("dcId",volumeOrder.getDcId());
				bson.put("volBootable","0");
				bson.put("prjId", volumeOrder.getPrjId());
				bson.put("volStatus","CREATING");
				bson.put("count", "0");
				bson.put("fromSnapId", volumeOrder.getFromSnapId());
				final JSONObject datas = bson;
				TransactionHookUtil.registAfterCommitHook(new Hook() {
					@Override
					public void execute() {
						jedisUtil.addUnique(RedisKey.volKey, datas.toJSONString());
					}
				});
			}
			
			return true;
		}catch(AppException e){
		    log.error(e.getMessage(),e);
			volOrderFail(volList,volumeOrder);
			throw e;
		}catch(Exception e){
			volOrderFail(volList,volumeOrder);
			log.error(e.getMessage(),e);
			throw new AppException ("error.openstack.message");
		}
		
	}
	
	
	
/**
 * 批量创建云硬盘
 */
	@Transactional(noRollbackFor=AppException.class)
	public List<BaseCloudVolume> addVolume(CloudOrderVolume volOrder)
			throws AppException {
		
		List<BaseCloudVolume> volList=new ArrayList<BaseCloudVolume>();
		
		try{
			int count=volOrder.getVolNumber();
			String volName=volOrder.getVolName();
			for(int i=1;i<=count;i++){
				JSONObject data=new JSONObject();
				JSONObject temp=new JSONObject();
				if(count==1){
					temp.put("name", volName);
				}else if(count>1){
					if(i>=10){
						temp.put("name", volName+"_"+i);
					}else{
						temp.put("name", volName+"_0"+i);
					}
				}
				temp.put("size", volOrder.getVolSize());
			    temp.put("imageRef", null);
			    if(null!=volOrder.getVolDescription()){
			    	temp.put("description", volOrder.getVolDescription());
			    }
				if(null!=volOrder.getVolTypeId()&&!"".equals(volOrder.getVolTypeId())){
					temp.put("volume_type", volOrder.getVolTypeId());
				}
				data.put("volume", temp);
				Volume result=volumeService.create(volOrder.getDcId(), volOrder.getPrjId(), data);
				BaseCloudVolume volume=new BaseCloudVolume();
				volume.setVolId(result.getId());
				volume.setVolName(result.getName());
				volume.setCreateTime(new Date());
				volume.setCreateName(volOrder.getCreateUser());
				volume.setDcId(volOrder.getDcId());
				volume.setPrjId(volOrder.getPrjId());
				if(true==result.getBootable()){
					volume.setVolBootable("1");
				}else{
					volume.setVolBootable("0");
				}
				volume.setDiskFrom(volOrder.getDiskFrom());
				volume.setVolSize(volOrder.getVolSize());
				volume.setVolStatus(result.getStatus().toUpperCase());
				volume.setVolDescription(result.getDescription());
				volume.setVolTypeId(volOrder.getVolTypeId());
				volume.setIsVisable("0");
				volume.setIsDeleted("0");
				volume.setTypeSuccess("1");
				volume.setChargeState(CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE);
				volume.setPayType(volOrder.getPayType());
				if(volOrder.getPayType().equals("1")){
					volume.setEndTime(DateUtil.getExpirationDate(volume.getCreateTime(), volOrder.getBuyCycle(), DateUtil.PURCHASE));
				}
				
				volumeDao.saveOrUpdate(volume);
				
				BaseCloudBatchResource resource=new BaseCloudBatchResource();
				resource.setOrderNo(volOrder.getOrderNo());
				resource.setResourceId(volume.getVolId());
				resource.setResourceType(CloudBatchResource.RESOURCE_VOLUME);
				cloudBatchResourceService.save(resource);

				volList.add(volume);
				
				if("ERROR".equals(volume.getVolStatus().toUpperCase())){
					volOrderFail(volList,volOrder);
					break;
				}
				
			}

			if(count==volList.size()){
				//TODO 同步新增云硬盘状态
				JSONObject json =new JSONObject();
				json.put("orderNo", volOrder.getOrderNo());
				json.put("volNumber", volOrder.getVolNumber());
				json.put("payType", volOrder.getPayType());
				json.put("cusId",volOrder.getCusId());
				json.put("createName",volOrder.getCreateUser());
				json.put("createTime",volOrder.getCreateOrderDate());
				json.put("dcId",volOrder.getDcId());
				json.put("volBootable","0");
				json.put("prjId", volOrder.getPrjId());
				json.put("volStatus","CREATING");
				json.put("count", "0");
				final JSONObject datas = json;
				TransactionHookUtil.registAfterCommitHook(new Hook() {
					@Override
					public void execute() {
						jedisUtil.addUnique(RedisKey.volKey, datas.toJSONString());
					}
				});
				
			}else{
				volOrderFail(volList,volOrder);
			}
	
		}catch(AppException e){
		    log.error(e.getMessage(),e);
			volOrderFail(volList,volOrder);
			throw e;
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			volOrderFail(volList,volOrder);
			throw new AppException ("error.openstack.message");
		}
		 return volList;	
	}
	
	
	/**
	 * 扩容云硬盘
	 * @throws Exception 
	 */
	@Transactional(noRollbackFor=AppException.class)
	public boolean largeVolume(CloudOrderVolume orderVol) throws Exception {
		boolean flag=false;
		try{
			JSONObject data = new JSONObject();
			JSONObject json = new JSONObject();
			json.put("new_size", orderVol.getVolSize());
			data.put("os-extend", json);
			flag=volumeService.extendVolume(orderVol.getDcId(), orderVol.getPrjId(), orderVol.getVolId(), data);
			if(flag){
				List<BaseOrderResource> resourceList = new ArrayList<BaseOrderResource>();
				BaseCloudVolume baseVol=volumeDao.findOne(orderVol.getVolId());
				baseVol.setVolSize(orderVol.getVolSize());
				volumeDao.saveOrUpdate(baseVol);
				
				//回调订单接口
			    BaseOrderResource resource = new BaseOrderResource();
				resource.setOrderNo(orderVol.getOrderNo());
				resource.setResourceId(baseVol.getVolId());
				resource.setResourceName(baseVol.getVolName());
				resourceList.add(resource);
				
				orderService.completeOrder(orderVol.getOrderNo(), true,resourceList,false,baseVol.getEndTime());
				
				if(PayType.PAYAFTER.equals(orderVol.getPayType())){
					ChargeRecord record = new ChargeRecord ();
					ParamBean param = new ParamBean();
					CloudVolumeType type=null;
					if(null!=baseVol.getVolTypeId()&&!"null".equals(baseVol.getVolTypeId())){
						type=volTypeService.getVolumeTypeById(baseVol.getDcId(), baseVol.getVolTypeId());
					}
					if(null!=type&&"1".equals(type.getVolumeType())){
						param.setDataDiskOrdinary(orderVol.getVolSize());
					}else if(null!=type&&"2".equals(type.getVolumeType())){
						param.setDataDiskBetter(orderVol.getVolSize());
					}else if(null!=type&&"3".equals(type.getVolumeType())){
						param.setDataDiskBest(orderVol.getVolSize());
					}else{
						param.setDataDiskCapacity(orderVol.getVolSize());
					}
					record.setParam(param);
					record.setDatecenterId(orderVol.getDcId());
					record.setOrderNumber(orderVol.getOrderNo());
					record.setCusId(orderVol.getCusId());
					record.setResourceId(orderVol.getVolId());
					record.setResourceType(ResourceType.VDISK);
					record.setChargeFrom(new Date());
					rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_UPGRADE, JSONObject.toJSONString(record));
				}
				
				
			}else{
				//扩容失败回调订单模块
				orderService.completeOrder(orderVol.getOrderNo(), false,null);
				messageCenterService.addResourFailMessage(orderVol.getOrderNo(), orderVol.getCusId());
			}
			
		}catch(AppException e){
		    log.error(e.getMessage(),e);
			orderService.completeOrder(orderVol.getOrderNo(), false,null);
			messageCenterService.addResourFailMessage(orderVol.getOrderNo(), orderVol.getCusId());
			throw e;
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			orderService.completeOrder(orderVol.getOrderNo(), false,null);
			messageCenterService.addResourFailMessage(orderVol.getOrderNo(), orderVol.getCusId());
		}
		return flag;
		
	}
	


	@Override
	public boolean deleteVolume(CloudVolume vol,SessionUserInfo user)throws AppException {
		boolean isTrue=false;
		try{
			/**
        	 * 判断资源是否有未完成的订单 --@author zhouhaitao
        	 */
			if(checkVolOrderExsit(vol.getVolId())){
				throw new AppException("该资源有未完成的订单，请取消订单后再进行删除操作！");
			}
			
			if("2".equals(vol.getIsDeleted())){
				BaseCloudVolume volume=volumeDao.findOne(vol.getVolId());
				volume.setDeleteUser(user.getUserName());
				volume.setDeleteTime(new Date());
				volume.setIsDeleted("2");
				volumeDao.saveOrUpdate(volume);
				
				//给计费模块发消息
				ChargeRecord record = new ChargeRecord ();
				record.setDatecenterId(volume.getDcId());
				record.setCusId(user.getCusId());
				record.setResourceId(volume.getVolId());
				record.setResourceType(ResourceType.VDISK);
				record.setResourceName(volume.getVolName());
				record.setOpTime(new Date());
				rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_RECYCLE, JSONObject.toJSONString(record));
	
				if("2".equals(vol.getIsDeSnaps())){
					snapService.deleteAllSnaps(vol.getVolId(), "2", user);
				}
				
				isTrue=true;
				
			}else{
				isTrue=volumeService.delete(vol.getDcId(), vol.getPrjId(), vol.getVolId());
				if(isTrue){
					BaseCloudVolume volume=volumeDao.findOne(vol.getVolId());
					volume.setDeleteUser(user.getUserName());
					volume.setVolStatus("DELETING");
					if(null==volume.getDeleteTime()){
						volume.setDeleteTime(new Date());
					}
					volumeDao.saveOrUpdate(volume);
					tagService.refreshCacheAftDelRes("volume",vol.getVolId());
					JSONObject json =new JSONObject();
					json.put("volId", volume.getVolId());
					json.put("dcId",volume.getDcId());
					json.put("prjId", volume.getPrjId());
					json.put("volStatus",volume.getVolStatus());
					json.put("count", "0");
					//jedisUtil.push(RedisKey.volKey, json.toJSONString());
					
					final JSONObject datas = json;
					TransactionHookUtil.registAfterCommitHook(new Hook() {
						@Override
						public void execute() {
							jedisUtil.addUnique(RedisKey.volKey, datas.toJSONString());
						}
					});
					
					//给计费模块发消息
				 if(PayType.PAYAFTER.equals(volume.getPayType())&&!"2".equals(volume.getIsDeleted())){
					ChargeRecord record = new ChargeRecord ();
					record.setDatecenterId(volume.getDcId());
					record.setCusId(user.getCusId());
					record.setResourceId(volume.getVolId());
					record.setResourceType(ResourceType.VDISK);
					record.setResourceName(volume.getVolName());
					record.setOpTime(new Date());
					rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_DELETE, JSONObject.toJSONString(record));
				 }
				}
				
				if(null!=vol.getIsDeSnaps()&&"1".equals(vol.getIsDeSnaps())){
					snapService.deleteAllSnaps(vol.getVolId(), "1", user);
				}
	
			}
		
		}catch(AppException e){
			throw e;
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			throw new AppException ("error.openstack.message");
		}
		return isTrue;
	}


	
	@Override
	public boolean updateVolume(CloudVolume volume)throws AppException {
		BaseCloudVolume vol=null;
		try{
			//拼装用于提交的数据
			JSONObject data=new JSONObject();
			JSONObject temp=new JSONObject();
			temp.put("name", volume.getVolName());
			temp.put("description", volume.getVolDescription());
			data.put("volume", temp);
			Volume result=volumeService.update(volume.getDcId(), volume.getPrjId(), data, volume.getVolId());
			
			if(null!=result){
				vol=volumeDao.findOne(volume.getVolId());
				vol.setVolName(volume.getVolName());
				vol.setVolDescription(volume.getVolDescription());
				volumeDao.saveOrUpdate(vol);
				return true;
			}
		
		
		}catch(AppException e){
			throw e;
		}
		return false;
		
	}


	@SuppressWarnings("rawtypes")
    @Override
	public CloudVolume getVolumeById(String dcId, String prjId, String volId)throws Exception {
		int index=0;
		Object [] args=new Object[3];
		StringBuffer sql=new StringBuffer();
		sql.append("select vol.vol_id AS volId,vol.vol_name AS volName,vol.vol_status AS volStatus,vol.vol_size AS volSize,vol.vol_bootable AS volBootable,vol.disk_from AS diskFrom,vol.create_time AS createTime,vol.vm_id as vmId,vm.vm_name AS vmName,vol.vol_description AS volDescription,vol.dc_id as dcId,dc.dc_name as dcName,vol.prj_id as prjId,prj.prj_name as prjName,count(snap.snap_id) as SnapNum,tree.node_name as sysType,vol.bind_point as bindPoint,vol.pay_type as payType,vol.end_time as endTime,vol.charge_state as chargeState,vol.is_deleted as isDeleted,vm.os_type as osType,vol.delete_time as deleteTime,");
		sql.append(" vol.vol_typeid as volTypeId,type.volume_type as volType,type.max_size as maxSize from cloud_volume vol");
		sql.append(" left join cloud_vm vm ON vol.vm_id = vm.vm_id");
		sql.append(" left join cloud_volumetype type ON vol.vol_typeid=type.type_id");
		sql.append(" left join cloud_disksnapshot snap ON vol.vol_id = snap.vol_id");
		sql.append(" left join cloud_project prj ON vol.prj_id=prj.prj_id");
		sql.append(" left join dc_datacenter dc ON vol.dc_id=dc.id");
		sql.append(" left join sys_data_tree tree ON vol.sys_type=tree.node_id");
		sql.append(" where 1=1 ");
		if(!"null".equals(dcId)&&null!=dcId&&!"".equals(dcId)&&!"undefined".equals(dcId)){
			sql.append(" and vol.dc_id=?");
			args[index]=dcId;
			index++;
		}
		if(!"null".equals(prjId)&&null!=prjId&&!"".equals(prjId)&&!"undefined".equals(prjId)){
			sql.append(" and vol.prj_id=?");
			args[index]=prjId;
			index++;
		}
		if (!"null".equals(volId)&&null!=volId&&!"".equals(volId)&&!"undefined".equals(volId)) {
			sql.append(" and vol.vol_id=?");
			args[index]=volId;
			index++;
		}
		Object[] params = new Object[index];  
        System.arraycopy(args, 0, params, 0, index);
        javax.persistence.Query query = volumeDao.createSQLNativeQuery(sql.toString(), params);
        List listResult=new ArrayList();
        CloudVolume volume=null;
        if(null!=query){
        	listResult = query.getResultList();

        }
        if (null != listResult && listResult.size() == 1) {
        	Object[] objs = (Object[])listResult.get(0);
        	volume=new CloudVolume();
        	volume.setVolId(String.valueOf(objs[0]));
        	volume.setVolName(String.valueOf(objs[1]));
        	volume.setVolStatus(String.valueOf(objs[2]));
        	volume.setVolSize(Integer.parseInt(String.valueOf(objs[3])));
        	volume.setVolBootable(String.valueOf(objs[4]));
        	volume.setDiskFrom(String.valueOf(objs[5]));
        	volume.setCreateTime((Date)objs[6]);
        	volume.setVmId(String.valueOf(objs[7]));
        	volume.setVmName(String.valueOf(objs[8]));
        	volume.setVolDescription(String.valueOf(objs[9]));
        	volume.setDcId(String.valueOf(objs[10]));
        	volume.setDcName(String.valueOf(objs[11]));
        	volume.setPrjId(String.valueOf(objs[12]));
        	volume.setPrjName(String.valueOf(objs[13]));
        	volume.setSnapNum(String.valueOf(objs[14]));
        	volume.setSysType(String.valueOf(objs[15]));
        	volume.setBindPoint(String.valueOf(objs[16]));
        	volume.setPayType(String.valueOf(objs[17]));
        	volume.setEndTime((Date)objs[18]);
        	volume.setChargeState(String.valueOf(objs[19]));
        	volume.setIsDeleted(String.valueOf(objs[20]));
        	String vmOsType=String.valueOf(objs[21]);
        	volume.setDeleteTime((Date)objs[22]);
        	volume.setVolTypeId(String.valueOf(objs[23]));
        	volume.setVolType(String.valueOf(objs[24]));
        	volume.setMaxSize(Integer.parseInt(null!=objs[25]?String.valueOf(objs[25]):"2048"));
        	String volumeTypeAs=getVolumeTypeForDis(String.valueOf(objs[24]));
        	volume.setVolumeTypeAs(volumeTypeAs);
        	if(null!=vmOsType&&"0007002002001".equals(vmOsType)){
        		volume.setBindPoint(null);
        	}
        	if(!"DELETING".equals(volume.getVolStatus().toUpperCase())){
        		volume.setStatusForDis(CloudResourceUtil.escapseChargeState(volume.getChargeState()));
        	}else{
        		volume.setStatusForDis(DictUtil.getStatusByNodeEn("volume", volume.getVolStatus()));
        	}
        	
        	if (null==volume.getChargeState()||"".equals(volume.getChargeState())||"null".equals(volume.getChargeState())||CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(volume.getChargeState())) {
        		volume.setStatusForDis(DictUtil.getStatusByNodeEn("volume", volume.getVolStatus()));
			}       	

        } 	
		return volume;
	}


	


	@Override
	public boolean bindVolume(CloudVolume vol) throws AppException{
		boolean isTrue=false;
		try {
			isTrue=volumeService.bind(vol.getDcId(), vol.getPrjId(), vol.getVolId(), vol.getVmId());
			if(isTrue){
				BaseCloudVolume volume=volumeDao.findOne(vol.getVolId());
				volume.setVolStatus("ATTACHING");
				volume.setVmId(vol.getVmId());
				volumeDao.saveOrUpdate(volume);
				
				//TODO 挂载云硬盘的自动任务
				JSONObject json =new JSONObject();
				json.put("volId", volume.getVolId());
				json.put("dcId",volume.getDcId());
				json.put("prjId", volume.getPrjId());
				json.put("volStatus",volume.getVolStatus());
				json.put("count", "0");
				//jedisUtil.addUnique(RedisKey.volKey, json.toJSONString());
				
				final JSONObject datas = json;
				TransactionHookUtil.registAfterCommitHook(new Hook() {
					@Override
					public void execute() {
						jedisUtil.addUnique(RedisKey.volKey, datas.toJSONString());
					}
				});
				
			}
			
		}catch (AppException e) {
			throw e;
		}
		return isTrue;
	}


	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public boolean debindVolume(CloudVolume vol) throws AppException {
		boolean isTrue=false;
		try {
			isTrue=volumeService.debind(vol.getDcId(), vol.getPrjId(), vol.getVolId(), vol.getVmId());
			if(isTrue){
				BaseCloudVolume volume=volumeDao.findOne(vol.getVolId());
				volume.setVmId(null);
				volume.setBindPoint(null);
				volume.setVolStatus("DETACHING");
				volumeDao.saveOrUpdate(volume);
				
				//TODO 解绑云硬盘的自动任务
				JSONObject json =new JSONObject();
				json.put("volId", volume.getVolId());
				json.put("dcId",volume.getDcId());
				json.put("prjId", volume.getPrjId());
				json.put("volStatus",volume.getVolStatus());
				json.put("count", "0");
				
				final JSONObject data = json;
				TransactionHookUtil.registAfterCommitHook(new Hook() {
					@Override
					public void execute() {
						jedisUtil.addUnique(RedisKey.volKey, data.toJSONString());
					}
				});
			}
			
		}catch (AppException e) {
			throw e;
		}
		return isTrue;
	}


	/**
	 * 验证重名
	 */
	@Override
	public boolean getVolumeByName(CloudVolume vol)throws Exception {
		boolean flag = false;
		int num=1;
		String[] nameList = null;

		if(vol.getVolNumber()>0){
			num = vol.getVolNumber();
			if(vol.getVolNumber()>20){
				num=20;
			}
		}
		
		if (num != 0) {
			nameList = new String[num];
		}
		if (num == 1) {
			nameList[0] = vol.getVolName();
		} else if (num > 1) {
			for (int i = 1; i <= num; i++) {
			   if(i>=10){
					nameList[i-1] = vol.getVolName() + "_" + i;
			   }else{
					nameList[i-1]=vol.getVolName() +"_0"+i;
			   }
				
			}
		}
	
		try{
			StringBuffer hql = new StringBuffer();
			hql.append(" select count(*) from  ");
			hql.append(" BaseCloudVolume v where 1=1 ");
			hql.append(" and v.prjId = :prjId ");
			hql.append(" and v.isDeleted in ('0','2') ");
			hql.append(" and binary(v.volName) in (:names)");
			if (!StringUtils.isEmpty(vol.getVolId())) {
				hql.append(" and v.volId <> :volId");
			}
			org.hibernate.Query query = volumeDao.getHibernateSession().createQuery(hql.toString());
			query.setParameter("prjId", vol.getPrjId());
			query.setParameterList("names", nameList);
			if (!StringUtils.isEmpty(vol.getVolId())) {
				query.setParameter("volId", vol.getVolId());
			}
			
			int count = Integer.parseInt(String.valueOf(query.uniqueResult()));
			flag = count == 0;
			
			if(flag){
				boolean isContains = false;
				int suffixNum = 0;
				String perfixName = "";
				String regex = "";
				String volName = vol.getVolName();
				int _index = volName.lastIndexOf('_');
				if(_index != -1){
					perfixName = volName.substring(0, _index);
					String suffixName="";
					if(num>0&&num<10){
						suffixName = volName.substring(_index+2);
					}else{
						suffixName = volName.substring(_index+1);
					}
					Pattern pattern = Pattern.compile("[1-9]|1[0-9]|20"); 
					Matcher isNum = pattern.matcher(suffixName);
					if(isNum.matches()){
						isContains = true;
						suffixNum = Integer.parseInt(suffixName);
					}
				}
				if(num>1){
					regex = "^"+volName + "_";
					if(num > 1 && num<=9){
						
						regex=regex +"0"+"([1-"+num+"])$";
					}
					else if(num== 10){
						regex = regex + "([1-9]|10)$";
					}
					else if (num>10 && num<=19){
						regex = regex + "([1-9]|1[0-"+(num-10)+"])$";
					}
					else{
						regex = regex + "([1-9]|1[0-9]|20)$";
					}
				}
				
				StringBuffer orderVolHql = new StringBuffer();
				orderVolHql.append("	SELECT                                                 ");
				orderVolHql.append("		count(cov.ordervol_id)                             ");
				orderVolHql.append("	FROM                                                   ");
				orderVolHql.append("		cloudorder_volume cov                              ");
				orderVolHql.append("	LEFT JOIN order_info oi ON oi.order_no = cov.order_no  ");
				orderVolHql.append("	WHERE                                                  ");
				orderVolHql.append("		1=1                                                ");
				orderVolHql.append("	AND cov.order_type = '0'                               ");
				orderVolHql.append("	AND oi.order_state in ('1','2')                        ");
				orderVolHql.append("	AND cov.prj_id = ?                                     ");
				
				if(num == 1){
					orderVolHql.append("AND ( ( binary(cov.vol_name) = '"+volName+"' and cov.vol_number = 1) ");
					if(isContains){
						orderVolHql.append(" or ( binary(cov.vol_name) = '"+perfixName+"' and cov.vol_number >= "+suffixNum+" ) )");
					}
					else{
						orderVolHql.append(")");
					}
				}
				else if(num > 1){
					orderVolHql.append("AND ( ( binary(cov.vol_name) = '"+volName+"' and cov.vol_number > 1) ");
					orderVolHql.append(" or ( binary(cov.vol_name) REGEXP '"+regex+"' and cov.vol_number = 1)	 ");
					orderVolHql.append(" ) ");
				}
				
				javax.persistence.Query orderVolQuery = volumeDao.createSQLNativeQuery(orderVolHql.toString(),new Object[]{vol.getPrjId()});
				Object obj = orderVolQuery.getSingleResult();
				int objInt = Integer.parseInt(String.valueOf(obj));
				if(objInt >0){
					flag = false;
				}
			}

		}catch(Exception e){
		    log.error(e.getMessage(),e);
			throw e;
		}
		
		return flag;
	}

    @Override
    public int getCountByVnId(String vmId) throws AppException {
        int vnCount = volumeDao.getVmCount(vmId);
        return vnCount;
    }

    /**
     * 
     */
    public List<CloudVolume> queryVolumesByVm(String vmId)throws AppException{
    	StringBuffer sql = new  StringBuffer ();
    	List<CloudVolume> list = new ArrayList<CloudVolume> ();
    	sql.append(" select  ");
    	sql.append("  vol.vol_id , ");
    	sql.append("  vol.vol_name , ");
    	sql.append("  vol.vol_status, ");
    	sql.append("  vol.vol_size, ");
    	sql.append("  vol.disk_from, ");
    	sql.append("  vm.vm_name, ");
    	sql.append("  vol.bind_point, ");
    	sql.append("  vol.dc_id , ");
    	sql.append("  vol.prj_id , ");
    	sql.append("  vol.vm_id , ");
    	sql.append("  vol.vol_bootable, ");
    	sql.append("  vol.charge_state ");
    	sql.append(" from  cloud_volume vol ");
    	sql.append(" left join cloud_vm vm on vol.vm_id = vm.vm_id  ");
    	sql.append(" where vol.vm_id = ? and vol.is_deleted='0'");
    	sql.append(" order by vol.vol_bootable desc ");
    	javax.persistence.Query query = volumeDao.createSQLNativeQuery(sql.toString(), new Object []{vmId});
 		@SuppressWarnings("rawtypes")
		List listResult = query.getResultList();
        for(int i=0;i<listResult.size();i++){
        	
        	Object[] objs = (Object[])listResult.get(i);
        	CloudVolume volume=new CloudVolume();
        	volume.setVolId(String.valueOf(objs[0]));
        	volume.setVolName(String.valueOf(objs[1]));
        	volume.setVolStatus(String.valueOf(objs[2]));
        	volume.setVolSize(Integer.parseInt(String.valueOf(objs[3])));
        	volume.setDiskFrom(String.valueOf(objs[4]));
        	volume.setVmName(String.valueOf(objs[5]));
        	volume.setBindPoint(String.valueOf(objs[6]));
        	volume.setDcId(String.valueOf(objs[7]));
        	volume.setPrjId(String.valueOf(objs[8]));
        	volume.setVmId(String.valueOf(objs[9]));
        	volume.setVolBootable(String.valueOf(objs[10]));
        	volume.setChargeState(String.valueOf(objs[11]));
        	volume.setStatusForDis(DictUtil.getStatusByNodeEn("volume", volume.getVolStatus()));
        	
        	list.add(volume);
        }
    	return list;
    }


	@Override
	public int CountVolByPrjId(String prjId) throws AppException {
		int volCount=volumeDao.getCountByPrjId(prjId);
		return volCount;
	}
	
    public boolean updateVol(CloudVolume cloudVolume){
    	boolean flag = false ;
		try{
			BaseCloudVolume volume = volumeDao.findOne(cloudVolume.getVolId());
			volume.setVolStatus(cloudVolume.getVolStatus());
			if(!StringUtils.isEmpty(cloudVolume.getVolBootable())){
				volume.setVolBootable(cloudVolume.getVolBootable());
			}
			if(!StringUtils.isEmpty(cloudVolume.getBindPoint())){
				volume.setBindPoint(cloudVolume.getBindPoint());
			}
			volumeDao.saveOrUpdate(volume);
			flag = true ;
		}catch(Exception e){
		    log.error(e.getMessage(),e);
		    flag = false;
		}
		return flag ;
    }

    public List<BaseCloudVolume> getStackList(BaseDcDataCenter dataCenter,String prjId) throws Exception {
    	List<BaseCloudVolume> list = new ArrayList<BaseCloudVolume> ();
    	List<JSONObject> result = volumeService.getStackList(dataCenter, prjId);
    	if (result != null && result.size() > 0) {                                                              
			for (JSONObject jsonObject : result) {                                                                
				 Volume data = restService.json2bean(jsonObject,                                             
						 Volume.class);                                                                            
				 BaseCloudVolume ccn=new BaseCloudVolume(data,dataCenter.getId(),prjId);                                 
				 initDiskFrom(ccn);
				list.add(ccn);
				
				}                                                                                                   
			}                                                                                                     
    	return list;
    }
    
    /**
	 * 查询初始化 CloudVolume 的 diskFrom 字段
	 * @param cv
	 */
	private void initDiskFrom(BaseCloudVolume cv){
		String str="blank";
		if(!StringUtils.isEmpty(cv.getFromSnapId())){
			str="snapshot";
		}
		else if(!StringUtils.isEmpty(cv.getFromImageId())){
			BaseCloudImage cloudImage=cloudImageDao.findOne(cv.getFromImageId());
			if(null!=cloudImage){
				if("1".equals(cloudImage.getImageIspublic().toString())){
					str="publicImage";
				}
				else if("2".equals(cloudImage.getImageIspublic().toString())){
					str="privateImage";
				}
			}
		}
		cv.setDiskFrom(str);
	}
	
	/**
	 * 根据标签名查询云硬盘
	 * @param tagName
	 * @return
	 */
	private String handleQueryTagCondition (String tagName){
		List<String> tagNames = StringUtil.handleTagCondition(tagName);
		StringBuffer sql = new StringBuffer();
		if(tagNames!=null && tagNames.size()>0){
			sql.append(" select DISTINCT tgres_resourceid from sys_tagresource ");
			for(int i = 0 ;i<tagNames.size();i++){
//				String name = tagNames.get(i).replaceAll("\\_", "\\\\_");
				String name = tagNames.get(i);
				
				sql.append(" INNER JOIN ( ");
				sql.append(" 	select ts.tgres_resourceid from sys_tagresource ts 	");
				sql.append("  	left join sys_tag t on t.tg_id =ts.tgres_tagid 	");
				sql.append("  	where tgres_resourcetype='volume' ");
				sql.append("  	and binary t.tg_name = '"+name+"'");
				sql.append(" ) "+("t"+i));
				sql.append("   using (tgres_resourceid) ");
			}
		}
		else{
			sql.append(" '' ");
		}
		
		return sql.toString();
	}
	
	public void insertVolumeDB(BaseCloudVolume cloudVolume){
		volumeDao.saveOrUpdate(cloudVolume);
	}
	
	public void deleteVolumeByVm(String vmId,String deleteUser){
		StringBuffer querySql = new StringBuffer();
		querySql.append("from BaseCloudVolume where vmId = ? and volBootable ='1'");
		@SuppressWarnings("unchecked")
		List<BaseCloudVolume> volumeList = volumeDao.find(querySql.toString(), new Object[]{vmId});
		for(BaseCloudVolume vol:volumeList){
			vol.setVolStatus("DELETING");
			vol.setDeleteTime(new Date());
			vol.setDeleteUser(deleteUser);
			
			volumeDao.merge(vol);
			
			tagService.refreshCacheAftDelRes("volume",vol.getVolId());
			
			JSONObject json =new JSONObject();
			json.put("volId", vol.getVolId());
			json.put("dcId",vol.getDcId());
			json.put("prjId", vol.getPrjId());
			json.put("volStatus",vol.getVolStatus());
			json.put("count", "0");
			//jedisUtil.push(RedisKey.volKey, json.toJSONString());
			
			final JSONObject datas = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.volKey, datas.toJSONString());
				}
			});
			
			
		}
	}


	@Override
	public List<CloudVolume> getUnUsedVolumeList(String projectId, String datacenterId)
			throws Exception {
		List<CloudVolume> list = new ArrayList<CloudVolume>();
		int index=0;
		Object [] args=new Object[2];
		StringBuffer sql=new StringBuffer();
		sql.append("select vol.vol_id AS volId,vol.vol_name AS volName,vol.vol_status AS volStatus,vol.vol_size AS volSize,vol.disk_from AS diskFrom,vol.create_time AS createTime,vol.dc_id as dcId,vol.prj_id as prjId,vol.vol_description as volDescription,vol.vol_bootable as volBootable  from cloud_volume vol");
		sql.append(" where vol.is_deleted='0' and vol.vm_id is null");
		sql.append(" and vol_bootable = '0' and vol_status ='AVAILABLE' ");
		sql.append(" and charge_state = '0' and is_visable = '1' ");
		if (!"null".equals(projectId)&&null!=projectId&&!"".equals(projectId)&&!"undefined".equals(projectId)) {
			sql.append(" and vol.prj_id=?");
			args[index]=projectId;
			index++;
		}
		if (!"null".equals(datacenterId)&&null!=datacenterId&&!"".equals(datacenterId)&&!"undefined".equals(datacenterId)) {
			sql.append(" and vol.dc_id=?");
			args[index]=datacenterId;
			index++;
		}
		sql.append(" group by vol.vol_id order by vol.create_time desc");
		
		Object[] params = new Object[index];  
        System.arraycopy(args, 0, params, 0, index);
        
        javax.persistence.Query query = volumeDao.createSQLNativeQuery(sql.toString(), params);
 		@SuppressWarnings("rawtypes")
        List listResult = query.getResultList();
        for(int i=0;i<listResult.size();i++){
        	Object[] objs = (Object[])listResult.get(i);
        	CloudVolume volume=new CloudVolume();
        	volume.setVolId(String.valueOf(objs[0]));
        	volume.setVolName(String.valueOf(objs[1]));
        	volume.setVolStatus(String.valueOf(objs[2]));
        	volume.setVolSize(Integer.parseInt(String.valueOf(objs[3])));
        	volume.setDiskFrom(String.valueOf(objs[4]));
        	volume.setCreateTime((Date)objs[5]);
        	volume.setDcId(String.valueOf(objs[6]));
        	volume.setPrjId(String.valueOf(objs[7]));
        	volume.setVolDescription(String.valueOf(objs[8]));
        	volume.setVolBootable(String.valueOf(objs[9]));
        	list.add(volume);
        }
    	return list;
	}

	
	/**
	 * 根据云硬盘订单组装 云硬盘配置
	 * 
	 * @param order
	 * @return
	 */
	private String volConfig(CloudOrderVolume order){
		StringBuffer buffer = new StringBuffer();
		if(OrderType.NEW.equals(order.getOrderType())){
			buffer.append("数据中心：").append(order.getDcName()).append("<br>");
			buffer.append("类型：").append(order.getVolumeTypeAs()).append("<br>");
			buffer.append("云硬盘容量：").append(order.getVolSize()+"GB").append("<br>");

		}
		else if(OrderType.UPGRADE.equals(order.getOrderType())){
			buffer.append("数据中心：").append(order.getDcName()).append("<br>");
			buffer.append("云硬盘ID：").append(order.getVolId()).append("<br>");
			buffer.append("云硬盘名称：").append(order.getVolName()).append("<br>");
			buffer.append("类型：").append(order.getVolumeTypeAs()).append("<br>");
			buffer.append("当前容量：").append(order.getVolOldSize()+"GB").append("<br>");
			buffer.append("扩容后容量：").append(order.getVolSize()+"GB");
		}

		return buffer.toString();
	
	}
	
	
	
	/**
	 * 如果创建有失败的整单的全部删除
	 * @param list
	 */
	public void volOrderFail(List<BaseCloudVolume>  list,CloudOrderVolume volOrder){
		//调用创建资源创建失败的接口
		try {
			orderService.completeOrder(volOrder.getOrderNo(), false,null);
			messageCenterService.addResourFailMessage(volOrder.getOrderNo(), volOrder.getCusId());
		
		// TODO 发送创建失败的消息
		List<MessageOrderResourceNotice> messageList=new ArrayList<MessageOrderResourceNotice>();
			if(null!=list&&list.size()>0){
				for(BaseCloudVolume volume :list){
					try{
						volumeService.delete(volume.getDcId(), volume.getPrjId(), volume.getVolId());
						volumeDao.delete(volume.getVolId());

						//删除云硬盘和订单之间的关系表数据
						CloudBatchResource cloudBatchResource = new CloudBatchResource();
						cloudBatchResource.setOrderNo(volOrder.getOrderNo());
						cloudBatchResource.setResourceId(volume.getVolId());
						cloudBatchResourceService.delete(cloudBatchResource);
					}catch(AppException e){
					    log.error(e.getMessage(),e);
					    MessageOrderResourceNotice notice=new MessageOrderResourceNotice();
						notice.setOrderNo(volOrder.getOrderNo());
						notice.setResourceId(volume.getVolId());
						notice.setResourceName(volume.getVolName());
						notice.setResourceType(ResourceType.getName(ResourceType.VDISK));
						messageList.add(notice);
					}catch(Exception e1){
					    log.error(e1.getMessage(),e1);
					    MessageOrderResourceNotice notice=new MessageOrderResourceNotice();
						notice.setOrderNo(volOrder.getOrderNo());
						notice.setResourceId(volume.getVolId());
						notice.setResourceName(volume.getVolName());
						notice.setResourceType(ResourceType.getName(ResourceType.VDISK));
						messageList.add(notice);
					}
					
				}
				
				if(null!=messageList&&messageList.size()>0){
					//给运维发邮件
					messageCenterService.delecteResourFailMessage(messageList, volOrder.getOrderNo());
				}
				
			}
		} catch (Exception e2) {
		    log.error(e2.getMessage(),e2);
		}
		
	}
	
	
	/**
	 * 创建云硬盘的订单
	 * @param cloudOrder
	 * 			云硬盘购买信息
	 * @param user
	 * 			当前用户
	 * @return
	 * 			订单信息
	 * @throws Exception
	 */
	private Order createVolumeOrder(CloudOrderVolume cloudOrder, SessionUserInfo user) throws Exception{

		Order order = new Order();
		order.setOrderType(cloudOrder.getOrderType());
		order.setDcId(cloudOrder.getDcId());
		order.setProdCount(cloudOrder.getVolNumber());
		order.setProdConfig(volConfig(cloudOrder));
		order.setPayType(cloudOrder.getPayType());
		order.setResourceType(ResourceType.VDISK);
		order.setUserId(user.getUserId());
		order.setCusId(user.getCusId());
		
		
		if(PayType.PAYBEFORE.equals(cloudOrder.getPayType())){
			
			cloudOrder.setPrice(cloudOrder.getPaymentAmount().divide(new BigDecimal(cloudOrder.getVolNumber()),2));
			
			if(OrderType.NEW.equals(cloudOrder.getOrderType())){
				order.setProdName("云硬盘-包年包月");
				order.setBuyCycle(cloudOrder.getBuyCycle());
			}
			else if(OrderType.UPGRADE.equals(cloudOrder.getOrderType())){
				order.setProdName("云硬盘-扩容");
				order.setResourceExpireTime(cloudOrder.getEndTime());
			}
			order.setUnitPrice(cloudOrder.getPrice());
			order.setPaymentAmount(cloudOrder.getPaymentAmount());
			order.setAccountPayment(cloudOrder.getAccountPayment());
			order.setThirdPartPayment(cloudOrder.getThirdPartPayment());
	
		
		}
		else if(PayType.PAYAFTER.equals(cloudOrder.getPayType())){
			if(OrderType.NEW.equals(cloudOrder.getOrderType())){
				order.setProdName("云硬盘-按需付费");
			}
			else if(OrderType.UPGRADE.equals(cloudOrder.getOrderType())){
				order.setProdName("云硬盘-扩容");
			}
			order.setBillingCycle(BillingCycleType.HOUR);
		}
		
		orderService.createOrder(order);
		return order;
	}
	

	
	@SuppressWarnings("rawtypes")
    @Override
	public String getVolumeNameById(String id) throws Exception{
		StringBuffer sb = new StringBuffer();
		sb.append("select vol_name from cloud_volume where vol_id=?");
		Query query = volumeDao.createSQLNativeQuery(sb.toString(), id);
		List list = query.getResultList();
		String volumeName = "";
		for (int i=0; i<list.size(); i++){
			volumeName = (String) query.getResultList().get(0);
		}
		return volumeName;
	}
	
	
	
	/**
	 * 给计费模块发消息
	 * 
	 * @author chengxiaodong
	 * @param record
	 * 
	 */
	public void volStartCharge (CloudOrderVolume orderVolume ,BaseCloudVolume vol){
		ChargeRecord record = new ChargeRecord ();
		ParamBean param = new ParamBean();
		CloudVolumeType type=null;
		
		if(null!=vol.getVolTypeId()&&!"null".equals(vol.getVolTypeId())){
			type=volTypeService.getVolumeTypeById(vol.getDcId(), vol.getVolTypeId());
		}
		
		if(null!=type&&"1".equals(type.getVolumeType())){
			param.setDataDiskOrdinary(vol.getVolSize());
		}else if(null!=type&&"2".equals(type.getVolumeType())){
			param.setDataDiskBetter(vol.getVolSize());
		}else if(null!=type&&"3".equals(type.getVolumeType())){
			param.setDataDiskBest(vol.getVolSize());
		}else{
			param.setDataDiskCapacity(vol.getVolSize());
		}
		
		
		record.setParam(param);
		record.setDatecenterId(vol.getDcId());
		record.setOrderNumber(orderVolume.getOrderNo());
		record.setCusId(orderVolume.getCusId());
		record.setResourceId(vol.getVolId());
		record.setResourceType(ResourceType.VDISK);
		record.setChargeFrom(new Date());
		
		rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_PURCHASE, JSONObject.toJSONString(record));
	}
	
	
	
	
	/**
	 * 云硬盘计费总价
	 * -----------------
	 * @author chengxiaodong
	 * 
	 * @param orderVolume
	 * 			订单信息
	 * @return
	 * 			配置总价
	 */
	private BigDecimal calcVolumePrice(CloudOrderVolume orderVolume){
		BigDecimal money=new BigDecimal(0);
		if(OrderType.NEW.equals(orderVolume.getOrderType())){
			ParamBean paramBean = new ParamBean();
			paramBean.setDcId(orderVolume.getDcId());
			paramBean.setPayType(orderVolume.getPayType());
			paramBean.setNumber(orderVolume.getVolNumber());
			paramBean.setCycleCount(orderVolume.getBuyCycle());
			
			if(null!=orderVolume.getVolType()&&"1".equals(orderVolume.getVolType())){
				paramBean.setDataDiskOrdinary(orderVolume.getVolSize());
			}else if(null!=orderVolume.getVolType()&&"2".equals(orderVolume.getVolType())){
				paramBean.setDataDiskBetter(orderVolume.getVolSize());
			}else if(null!=orderVolume.getVolType()&&"3".equals(orderVolume.getVolType())){
				paramBean.setDataDiskBest(orderVolume.getVolSize());
			}else{
				paramBean.setDataDiskCapacity(orderVolume.getVolSize());
			}
            money= billingFactorService.getPriceByFactor(paramBean);
		}else if(OrderType.UPGRADE.equals(orderVolume.getOrderType())){
			BaseCloudVolume baseCloudVolume=volumeDao.findOne(orderVolume.getVolId());
			orderVolume.setVolOldSize(baseCloudVolume.getVolSize());
			orderVolume.setEndTime(baseCloudVolume.getEndTime());
			UpgradeBean upgradeBean=new UpgradeBean();
			upgradeBean.setDcId(orderVolume.getDcId());
			if(null!=orderVolume.getVolType()&&"1".equals(orderVolume.getVolType())){
				upgradeBean.setDataDiskOrdinary(orderVolume.getVolSize()-orderVolume.getVolOldSize());
			}else if(null!=orderVolume.getVolType()&&"2".equals(orderVolume.getVolType())){
				upgradeBean.setDataDiskBetter(orderVolume.getVolSize()-orderVolume.getVolOldSize());
			}else if(null!=orderVolume.getVolType()&&"3".equals(orderVolume.getVolType())){
				upgradeBean.setDataDiskBest(orderVolume.getVolSize()-orderVolume.getVolOldSize());
			}else{
				upgradeBean.setDataDiskCapacity(orderVolume.getVolSize()-orderVolume.getVolOldSize());
			}
			upgradeBean.setCycleCount(DateUtil.getUgradeRemainDays(new Date(), orderVolume.getEndTime()));
			money=billingFactorService.updateConfigPrice(upgradeBean);
		}
		
		return money.setScale(2, RoundingMode.FLOOR);
		
	}
	
	
	
	/**
	 * 根据订单编号 查询对应的已经创建的云硬盘列表
	 * -----------------------------------
	 * @author chengxiaodong
	 * @param orderNo
	 * 			订单编号
	 * @return
	 * 
	 */
	@SuppressWarnings("unused")
	private List<CloudVolume> queryVolListByOrder(String orderNo){
		List<CloudVolume> volList = new ArrayList<CloudVolume>();
		StringBuffer sql = new StringBuffer();
		
		sql.append("		SELECT                                                ");
		sql.append("			vol.dc_id,                                         ");
		sql.append("			vol.prj_id,                                        ");
		sql.append("			vol.vol_id                                          ");
		sql.append("		FROM                                                  ");
		sql.append("			cloud_batchresource cbr                           ");
		sql.append("		LEFT JOIN cloud_vol vol ON vol.vol_id = cbr.resource_id   ");
		sql.append("		AND resource_type = 'volume'                              ");
		sql.append("		WHERE                                                 ");
		sql.append("			cbr.order_no = ?                                  ");
		
		javax.persistence.Query query = volumeDao.createSQLNativeQuery(sql.toString(), new Object[]{
				orderNo
		});
		
		@SuppressWarnings("rawtypes")
		List result = query.getResultList();
		if(result != null && result.size() >0){
			for(int i=0;i<result.size();i++){
				int index = 0;
				Object [] objs = (Object []) result.get(i);
				CloudVolume cloudVolume = new CloudVolume();
				
				cloudVolume.setDcId(String.valueOf(objs[index++]));
				cloudVolume.setPrjId(String.valueOf(objs[index++]));
				cloudVolume.setVolId(String.valueOf(objs[index++]));
				
				volList.add(cloudVolume);
			}
		}
		
		return volList;
	}
	
	
	
	/**
	 * 订单完成
	 * 
	 * @author chengxiaodong
	 * 
	 * @param orderNo
	 * @throws Exception 
	 */
	@SuppressWarnings("unused")
	public void volOrderSuccess (CloudOrderVolume orderVolume,List<BaseCloudVolume> volList) throws Exception{
		try{
			JSONObject json = new JSONObject();
			List<BaseOrderResource> resourceList = new ArrayList<BaseOrderResource>();
			List<String> volIds = new ArrayList<String>();
			for(BaseCloudVolume vol :volList){
				BaseOrderResource resource = new BaseOrderResource();
				resource.setOrderNo(orderVolume.getOrderNo());
				resource.setResourceId(vol.getVolId());
				resource.setResourceName(vol.getVolName());
				resourceList.add(resource);
			}
			
			//更新订单
			BaseOrder order = orderService.completeOrder(orderVolume.getOrderNo(), true,resourceList);
			
			for (BaseCloudVolume vol :volList) {
				//将云硬盘置为可见
				BaseCloudVolume baseVolume=volumeDao.findOne(vol.getVolId());
				baseVolume.setIsVisable("1");
				volumeDao.saveOrUpdate(baseVolume);
				
				volIds.add(vol.getVolId());
				if(PayType.PAYAFTER.equals(orderVolume.getPayType())){
					volStartCharge(orderVolume, baseVolume);
				}
			}
			
			if (volIds.size() > 0) {
				json.put("volume", volIds);
			}
			
			//更新cloudorder_Volume表中的resources
			volOrderService.updateOrderResources(orderVolume.getOrderNo(),json.toJSONString());
			
			//将云硬盘和订单之间的中间表数据删除
			cloudBatchResourceService.deleteByOrder(orderVolume.getOrderNo());
		
		}catch(Exception e){
		    log.error(e.getMessage(),e);
		    volOrderFail(volList, orderVolume);
		}
		
	}
	
	
	
	
	
	/**
	 * 修改配置表已创建订单的资源ids
	 * 
	 * @author chengxiaodong
	 * 
	 * @param orderNo
	 * 				订单编号
	 * @param resourceJson
	 * 				资源id的JSON
	 * 
	 * @return
	 * @throws Exception 
	 * 
	 */
	public boolean updateOrderResources(String orderNo,String resourceJson) throws Exception{
		
		boolean isSuccess = false;
		try{
			
			isSuccess =volOrderService.updateOrderResources(orderNo, resourceJson);
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			isSuccess = false;
			throw e;
		}
		
		return isSuccess;
	}
	
	
	
	/**
	 * 订单完成，订单下的云硬盘置为可见
	 * 
	 * @author chengxiaodong
	 * 
	 * @param orderNo
	 */
	public void modifyVolVisableByOrder (String orderNo){
		StringBuffer sql = new StringBuffer();
		sql.append("update cloud_volume set is_visable = '1' where vol_id in (");
		sql.append(" select resource_id from cloud_batchresource where order_no = ? and resource_type = 'volume'");
		sql.append(" )");
	    volumeDao.execSQL(sql.toString(), new Object[]{orderNo});
	}
	
	
	
	/**
	 * 校验本次订单信息是否超配
	 * ---------------------
	 * @author chengxiaodong
	 * @param orderVm
	 * 			云主机订单信息
	 * @return
	 * 			返回的错误信息标示
	 */
	public String checkVolumeQuota (CloudOrderVolume orderVolume){
		String errMsg = null;
		CloudProject project = queryProjectQuotaAndUsed(orderVolume.getPrjId());
		
		if(OrderType.NEW.equals(orderVolume.getOrderType())){
			if(orderVolume.getVolNumber()>(project.getDiskCount() - project.getDiskCountUse())){
				errMsg = "OUTOF_VOLUMECOUNT_QUOTA" ;
				return errMsg;
			}
			
			if((orderVolume.getVolNumber()*orderVolume.getVolSize())>(project.getDiskCapacity() - project.getUsedDiskCapacity())){
				errMsg = "OUTOF_DISKCAPACITY_QUOTA" ;
				return errMsg;
			}
		}else if(OrderType.UPGRADE.equals(orderVolume.getOrderType())){
			if((orderVolume.getVolSize()-orderVolume.getVolOldSize())>(project.getDiskCapacity()- project.getUsedDiskCapacity())){
				errMsg = "OUTOF_DISKCAPACITY_QUOTA" ;
				return errMsg;
			}
			
		}
		return errMsg;
	}
	
	
	/**
	 * 查询项目下的配额信息和已使用量统计
	 * ----------------------------------
	 * @author chengxiaodong
	 * 
	 * @param prjId
	 * 			项目ID
	 * @return
	 * 			项目配额及使用情况信息
	 */
	public CloudProject queryProjectQuotaAndUsed(String prjId){
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

		javax.persistence.Query query = volumeDao.createSQLNativeQuery(sql.toString(),
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
			project.setUsedDiskCapacity(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
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
	
	/**
	 * @author chengxiaodong
	 * 
	 * @param volId
	 * 				云资源ID
	 * @param chargeState
	 * 				资源需要改变成的状态
	 * @param isDebind
	 * 				是否停止服务    true需要停止服务
	 * @return 
	 * 		
	 */
	@Override
	public void modifyStateForVol(String volId, String chargeState,
			boolean isDebind) throws AppException {
		
		CloudVolume volume=null;
		BaseCloudVolume baseVol=volumeDao.findOne(volId);
		baseVol.setChargeState(chargeState);
		volumeDao.saveOrUpdate(baseVol);
		BaseCloudProject project = projectService.findProject(baseVol.getPrjId());
		//云硬盘欠费超过信用值且超过保留时常
		if(isDebind){
			volume=new CloudVolume();
			BeanUtils.copyPropertiesByModel(volume, baseVol);
			if(null!=volume.getVmId()&&!"".equals(volume.getVmId())&&!"null".equals(volume.getVmId())){
				if("IN-USE".equals(volume.getVolStatus().toUpperCase())){
					debindVolume(volume);
				}
			}
		}
		
		if(("1".equals(chargeState)||"3".equals(chargeState))&&"2".equals(baseVol.getPayType())&&"0".equals(baseVol.getVolBootable())&&isDebind){
			ChargeRecord record = new ChargeRecord ();
			record.setDatecenterId(baseVol.getDcId());
			record.setCusId(project.getCustomerId());
			record.setResourceId(baseVol.getVolId());
			record.setResourceType(ResourceType.VDISK);
			record.setOpTime(new Date());
			rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_RESTRICT, JSONObject.toJSONString(record));
			
		}
		
		//恢复计费
		if("0".equals(chargeState)&&"2".equals(baseVol.getPayType())&&"0".equals(baseVol.getVolBootable())){
			ChargeRecord record = new ChargeRecord ();
			record.setDatecenterId(baseVol.getDcId());
			record.setCusId(project.getCustomerId());
			record.setResourceId(baseVol.getVolId());
			record.setResourceType(ResourceType.VDISK);
			record.setOpTime(new Date());
			rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_RECOVER, JSONObject.toJSONString(record));
		}
		
		
	}

	/**
	 * @author chengxiaodong
	 * 
	 * @param volId
	 * 				云资源ID
	 * @param chargeState
	 * 				资源需要改变成的状态
	 * @param date
	 * 				资源的新到期时间   后付费 不需要此参数
	 * @param isDebind
	 * 				是否停止服务    true需要停止服务
	 * @return 
	 * 		
	 */
	@Override
	public void modifyStateForVol(String volId, String chargeState,
			Date endTime, boolean isDebind) throws Exception {
		BaseCloudVolume  baseVolume=volumeDao.findOne(volId);
		
		if(null!=chargeState&&!"".equals(chargeState)){
			baseVolume.setChargeState(chargeState);
		}
		if(null!=endTime){
			baseVolume.setEndTime(endTime);
		}
		volumeDao.saveOrUpdate(baseVolume);

	}


/**
 * 解绑一台云主机下所有的数据盘
 */
	@Override
	@Transactional(noRollbackFor=AppException.class)
	public boolean debindVolsByVmId(String vmId) throws AppException {
		StringBuffer hql = new StringBuffer();
		hql.append(" from  ");
		hql.append(" BaseCloudVolume v where 1=1 and v.volBootable= :volBootable");
		hql.append(" and v.vmId = :vmId ");
		hql.append(" and v.isDeleted = :delFlag ");
		org.hibernate.Query query = volumeDao.getHibernateSession().createQuery(hql.toString());
		query.setParameter("volBootable", "0");
		query.setParameter("vmId", vmId);
		query.setParameter("delFlag", "0");
		@SuppressWarnings("unchecked")
		List<BaseCloudVolume> queryList = query.list();
		int count=0;
		if(queryList.size()>0){
			for(BaseCloudVolume baseVolume :queryList){
				CloudVolume volume=new CloudVolume();
				BeanUtils.copyPropertiesByModel(volume, baseVolume);
				debindVolume(volume);
				count++;
			}
		}
		
		if(count==queryList.size()){
			return true;
		}else{
			return false;
		}
		
	}


/**
 * 从回收站恢复云硬盘
 */
	@Override
	public boolean recoverVolume(String volId,SessionUserInfo sessionUser) throws AppException {
		
		try{
			BaseCloudVolume baseVolume=volumeDao.findOne(volId);
			
			if((null==baseVolume)||!"2".equals(baseVolume.getIsDeleted())){
				throw new AppException("该云硬盘已不在回收站中");
			}
			if(PayType.PAYAFTER.equals(baseVolume.getPayType())){
				//后付费资源 判断当前余额
				MoneyAccount accountMoney = accountOverviewSerivce.getAccountInfo(sessionUser.getCusId());
				BigDecimal createResourceLimitedMoney = new BigDecimal(0);
				if (accountMoney.getMoney().compareTo(createResourceLimitedMoney) < 0) {
					throw new AppException("账户余额不足，请充值后再恢复");
				}
			}
			
			baseVolume.setDeleteTime(null);
			baseVolume.setDeleteUser(null);
			baseVolume.setIsDeleted("0");
			
			if(PayType.PAYAFTER.equals(baseVolume.getPayType())){
				baseVolume.setChargeState("0");
				Customer customer = customerService.findCustomerById(sessionUser.getCusId());
				if(null != customer.getOverCreditTime()){
                    boolean isBeyondRetentionTime = isBeyondRetentionTime(customer);
                    if(isBeyondRetentionTime){
                        baseVolume.setChargeState("1");
                    }else{
                        baseVolume.setChargeState("0");
                    }
				}
			}
			
			volumeDao.saveOrUpdate(baseVolume);
			
			if(PayType.PAYAFTER.equals(baseVolume.getPayType())){
				ChargeRecord record = new ChargeRecord ();
				record.setDatecenterId(baseVolume.getDcId());
				record.setCusId(sessionUser.getCusId());
				record.setResourceId(baseVolume.getVolId());
				record.setResourceType(ResourceType.VDISK);
				record.setOpTime(new Date());
				rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_RESTORE, JSONObject.toJSONString(record));
			}
			
			return true;
			
		}catch(AppException e){
			throw e;
		}catch(Exception e1){
		    log.error(e1.getMessage(), e1);
		}
		return false;
		
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
 * 查询指定云主机下的系统盘
 */
@Override
public CloudVolume getOsVolumeByVmId(String vmId) throws Exception {
	CloudVolume volume=new CloudVolume();
	StringBuffer hql = new StringBuffer();
	hql.append(" from  ");
	hql.append(" BaseCloudVolume v where 1=1 and v.volBootable= :volBootable");
	hql.append(" and v.vmId = :vmId ");
	hql.append(" and v.isDeleted = :delFlag ");
	org.hibernate.Query query = volumeDao.getHibernateSession().createQuery(hql.toString());
	query.setParameter("volBootable", "1");
	query.setParameter("vmId", vmId);
	query.setParameter("delFlag", "0");
	@SuppressWarnings("unchecked")
	List<BaseCloudVolume> queryList = query.list();
	if(queryList.size()>0){
		for(BaseCloudVolume baseVolume :queryList){
			BeanUtils.copyPropertiesByModel(volume, baseVolume);	
		}
	}
	return volume;
}


/**
 * 综合查询云硬盘
 */
@Override
public List<CloudVolume> getVolumesBySome(Date endTime, String chargeState,
		String isDeleted,boolean isInRecycle, String payType,String prjId,String volBootable) throws Exception {
	List<CloudVolume> list=new ArrayList<CloudVolume>();
	StringBuffer hql = new StringBuffer();
	hql.append(" from  ");
	hql.append(" BaseCloudVolume v where 1=1");
	hql.append(" and v.chargeState = :chargeState ");
	hql.append(" and v.payType = :payType ");
	hql.append(" and v.volBootable = :volBootable ");
	if(isInRecycle){
		hql.append(" and v.isDeleted != '1' ");
	}else{
		hql.append(" and v.isDeleted = :isDeleted ");
	}
	if(null!=endTime){
		hql.append(" and v.endTime <= :endTime ");
	}
	if(null!=prjId){
		hql.append(" and v.prjId = :prjId ");
	}
	hql.append(" and isVisable='1' ");
	org.hibernate.Query query = volumeDao.getHibernateSession().createQuery(hql.toString());
	if(!isInRecycle){
		query.setParameter("isDeleted", isDeleted);
	}
	query.setParameter("chargeState", chargeState);
	query.setParameter("payType", payType);
	query.setParameter("volBootable", volBootable);
	if(null!=endTime){
		query.setParameter("endTime", endTime);
	}
	if(null!=prjId){
		query.setParameter("prjId", prjId);
	}
	
	@SuppressWarnings("unchecked")
	List<BaseCloudVolume> queryList = query.list();
	if(queryList.size()>0){
		for(BaseCloudVolume baseVolume :queryList){
			CloudVolume volume=new CloudVolume();
			BeanUtils.copyPropertiesByModel(volume, baseVolume);	
			list.add(volume);
		}
	}
	return list;
}


/*****************************云硬盘续费后台开始***************************/

/**
 * 云硬盘续费，提交按钮校验当前主机是否有未完成订单，以及当前账户金额是否充足与提交订单	
 * 
 * @author liyanchao	
 * @param map
 * @param ParamBean
 * @param userId
 * @return JSONObject
 */
public JSONObject renewVolumeOrderConfirm(Map<String ,String> map,String userId,String userName,String cusId)throws Exception{
	JSONObject jsonResult = new JSONObject ();
	
	boolean flag = false;
	flag = this.checkVolOrderExsit(map.get("volId").toString());
	if(!flag){
		jsonResult = this.createVolumeRenewOrder(map, userId,userName,cusId);
	}else{
		jsonResult.put("respCode", 1);
		jsonResult.put("message", "您当前有未完成订单，不允许提交新订单！");
	}
	return jsonResult;

}

/**
 * @param map
 * @param paramBean
 * @param userId
 * @return JSONObject
 * @throws Exception
 */
private JSONObject createVolumeRenewOrder(Map<String ,String> map,String userId,String userName,String cusId)throws Exception{
	
	JSONObject jsonResult = new JSONObject ();
	String aliPay = (String) map.get("aliPay");//支付宝付款金额
	String accountPay = (String) map.get("accountPay");//余额付款金额
	String totalPay = (String) map.get("totalPay");
	String isAccountPay = (String) map.get("isCheck");
	String volId = (String) map.get("volId");

	
	BigDecimal orgTotalPay = new BigDecimal(totalPay);
	BigDecimal price = checkRenewAmount(map, volId);;
    price = price.setScale(2, BigDecimal.ROUND_FLOOR);
	if(orgTotalPay.compareTo(price)==0){
		if("false".equals(isAccountPay)|| null== isAccountPay){// 直接"创建订单，跳向支付宝支付页面！";
			if(aliPay.compareTo("0")==0){//说明没有勾选余额支付，且支付金额为零
				Order order = orderService.createOrder(organizOrder(map, userId,cusId));
				this.saveOrUpdateCloudOrderVolume(order, volId, cusId, userName);//创建订单后，回写业务信息
				
				jsonResult.put("respCode", 10);
				jsonResult.put("message", order.getProdName());
			}else{//跳向支付宝--没用余额
			Order order = orderService.createOrder(organizOrder(map, userId,cusId));
			this.saveOrUpdateCloudOrderVolume(order, volId, cusId, userName);//创建订单后，回写业务信息
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
					this.saveOrUpdateCloudOrderVolume(order, volId, cusId, userName);//创建订单后，回写业务信息
					jsonResult.put("respCode", 10);
					jsonResult.put("message", order.getProdName());
					
				}else{//有用余额+支付宝 混合支付
					Order order = orderService.createOrder(organizOrder(map, userId,cusId));
					this.saveOrUpdateCloudOrderVolume(order, volId, cusId, userName);//创建订单后，回写业务信息
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
    private BigDecimal checkRenewAmount(Map<String, String> map, String volId) throws Exception {
        //重新获取云硬盘后台配置，计算订单金额使用后台配置计算
        BaseCloudVolume volume = volumeDao.findOne(volId);
        int dataDiskCapacity = volume.getVolSize();

        ParamBean paramBean = new ParamBean();
        paramBean.setDcId(map.get("dcId"));
        paramBean.setPayType(map.get("payType"));
        paramBean.setNumber(Integer.parseInt(map.get("number")));
        paramBean.setCycleCount(Integer.parseInt(map.get("cycleCount")));
        String volType = map.get("volType")==null?"":map.get("volType").toString();
        if("1".equals(volType)){
            paramBean.setDataDiskOrdinary(dataDiskCapacity);
        }else if("2".equals(volType)){
            paramBean.setDataDiskBetter(dataDiskCapacity);
        }else if("3".equals(volType)){
            paramBean.setDataDiskBest(dataDiskCapacity);
        }else{
            paramBean.setDataDiskCapacity(dataDiskCapacity);
        }
        BigDecimal price = billingFactorService.getPriceByFactor(paramBean);
        return price;
    }
/**
 * 组织订单参数
 * **/
private Order organizOrder(Map<String , String> map ,String userId,String cusId){
	//map.get("totalPay") :续费的总价格  //map.get("accountPay") 余额支付的金额
	BaseCloudVolume baseVolume = volumeDao.findOne(map.get("volId").toString());
	
	Order order = new Order();
	order.setOrderType(OrderType.RENEW);
	order.setProdName("云硬盘-续费");
	order.setDcId(baseVolume.getDcId());
	order.setProdCount(1);
	StringBuffer buf = new StringBuffer();
	buf.append("数据中心："+map.get("dcName")+"<br>");
	buf.append("云硬盘ID："+baseVolume.getVolId()+"<br>");
	buf.append("云硬盘名称："+baseVolume.getVolName()+"<br>");
	buf.append("类型："+(null!=map.get("volumeTypeAs")&&!"null".equals(map.get("volumeTypeAs"))?map.get("volumeTypeAs").toString():"")+"<br>");
	buf.append("云硬盘容量："+baseVolume.getVolSize()+"GB");
	order.setProdConfig(buf.toString());
	order.setPayType(PayType.PAYBEFORE);
	order.setBuyCycle(Integer.parseInt(map.get("buyCycle").toString()));
	order.setUnitPrice(new BigDecimal(map.get("totalPay").toString()));
	order.setResourceType(ResourceType.VDISK);
	order.setPaymentAmount(new BigDecimal(map.get("totalPay").toString()));
	order.setAccountPayment(new BigDecimal(map.get("accountPay").toString()));
	order.setThirdPartPayment(new BigDecimal(map.get("totalPay").toString()).subtract(new BigDecimal(map.get("accountPay").toString())));
	
	JSONObject params = new JSONObject();
    params.put("resourceId", map.get("volId"));
    params.put("resourceName", baseVolume.getVolName());
    params.put("resourceType", ResourceType.VDISK);
    params.put("expirationDate", baseVolume.getEndTime());
    params.put("duration", map.get("buyCycle"));
    params.put("operatorIp", map.get("operatorIp"));
    order.setParams(params.toJSONString());

	
	order.setCusId(cusId);
	order.setUserId(userId);
	return order;
}
/**
 * 创建订单后，回写业务信息
 * @throws Exception 
 * **/
private BaseCloudVolume saveOrUpdateCloudOrderVolume(Order order,String volId,String cusId,String createUser) throws Exception{
	BaseCloudVolume baseVolume = volumeDao.findOne(volId);
	CloudOrderVolume orderVolume = new CloudOrderVolume();
	orderVolume.setOrderNo(order.getOrderNo());
	orderVolume.setVolId(volId);
	orderVolume.setDcId(baseVolume.getDcId());
	orderVolume.setPrjId(baseVolume.getPrjId());
	orderVolume.setOrderType(order.getOrderType());
	orderVolume.setPayType(baseVolume.getPayType());
	orderVolume.setBuyCycle(order.getBuyCycle());
	orderVolume.setCreateOrderDate(new Date());
	orderVolume.setCreateUser(createUser);
	orderVolume.setPrice(order.getPaymentAmount());
	orderVolume.setVolSize(baseVolume.getVolSize());
	orderVolume.setVolNumber(1);
	orderVolume.setVolName(baseVolume.getVolName());
	orderVolume.setCusId(cusId);
	orderVolume.setVolTypeId(baseVolume.getVolTypeId());
	volOrderService.addOrderVolume(orderVolume);
	return baseVolume;
}
/**
 * 组织主机计费参数
 * **/
private ParamBean organizParamBean(Map<String,String> map){
	ParamBean paramBean = new ParamBean();
	paramBean.setDcId(map.get("dcId"));
	paramBean.setPayType(map.get("payType"));
	paramBean.setNumber(Integer.parseInt(map.get("number")));
	paramBean.setCycleCount(Integer.parseInt(map.get("cycleCount")));
	if(null!=map.get("volType")&&"1".equals(map.get("volType"))){
		paramBean.setDataDiskOrdinary(Integer.parseInt(map.get("dataDiskCapacity")));
	}else if(null!=map.get("volType")&&"2".equals(map.get("volType"))){
		paramBean.setDataDiskBetter(Integer.parseInt(map.get("dataDiskCapacity")));
	}else if(null!=map.get("volType")&&"3".equals(map.get("volType"))){
		paramBean.setDataDiskBest(Integer.parseInt(map.get("dataDiskCapacity")));
	}else{
		paramBean.setDataDiskCapacity(Integer.parseInt(map.get("dataDiskCapacity")));
	}
	
	return paramBean;
}
/******************************云硬盘续费后台结束***************************************/



/**
 * 查询回收站的云硬盘列表
 * ------------
 * @author chengxiaodong
 * @param page 分页结果集
 * @param map 查询条件
 * @param sessionUser 当前用户
 * @param queryMap 分页条件
 * @return 
 * @throws Exception
 */
@SuppressWarnings({ "unchecked", "unused" })
public Page getRecycleVolList(Page page,ParamsMap map,SessionUserInfo sessionUser, QueryMap queryMap)throws Exception{

	int index = 0;
	Object[] args = new Object[10];
	String name = "";
	String type = "";
	String dcId="";

	if (null != map && null != map.getParams()) {
		name=map.getParams().get("name")!=null?map.getParams().get("name").toString():"";
		type = map.getParams().get("queryType") != null ? map.getParams().get("queryType") + "" : "";
		dcId = map.getParams().get("dcId") != null ? map.getParams().get("dcId") + "" : "";
	}

	StringBuffer sql = new StringBuffer();
	sql.append("select vol.vol_id AS volId,vol.vol_name AS volName,vol.vol_status AS volStatus,vol.vol_size AS volSize,vol.disk_from AS diskFrom,vol.is_deleted AS isDeleted,");
	sql.append(" vol.create_time AS createTime,vol.delete_time as deleteTime,vol.dc_id as dcId,vol.prj_id as prjId,prj.prj_name as prjName,");
	sql.append(" vol.vm_id as vmId,vm.vm_name AS vmName,vol.vol_description as volDescription,vol.bind_point as bindPoint,vm.vm_status as vmStatus,");
	sql.append(" vol.vol_bootable as volBootable,vol.pay_type as payType,vol.end_time as endTime,vol.charge_state as chargeState,dc.dc_name as dcName  from cloud_volume vol");
	sql.append(" left join cloud_vm vm ON vol.vm_id=vm.vm_id");
	sql.append(" left join dc_datacenter dc ON vol.dc_id=dc.id");
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
	sql.append(" ) ON vol.prj_id=prj.prj_id");
	sql.append(" where vol.is_deleted = '2'");
	sql.append(" and vol.is_visable = '1'");
	sql.append(" and prj.customer_id = ?");
	args[index] = sessionUser.getCusId();
	index++;
	
	if (!"null".equals(dcId) && !StringUtils.isEmpty(dcId)) {
		sql.append(" and vol.dc_id = ? ");
		args[index] = dcId;
		index++;
	}

	if (!StringUtils.isEmpty(name)) {
		sql.append(" and binary vol.vol_name like ?");
		name = name.replaceAll("\\_", "\\\\_").replaceAll("\\%", "\\\\%");
		args[index] = "%" + name + "%";
		index++;
	} 
	sql.append(" group by vol.vol_id order by vol.delete_time desc");

	Object[] params = new Object[index];
	System.arraycopy(args, 0, params, 0, index);
	page = volumeDao.pagedNativeQuery(sql.toString(), queryMap, params);
	@SuppressWarnings("rawtypes")
	List newList = (List) page.getResult();
	for (int i = 0; i < newList.size(); i++) {
		int ind =0;
		Object[] objs = (Object[]) newList.get(i);
		CloudVolume vol = new CloudVolume();
		vol.setVolId(String.valueOf(objs[ind++]));
		vol.setVolName(String.valueOf(objs[ind++]));
		vol.setVolStatus(String.valueOf(objs[ind++]));
		vol.setVolSize(Integer.parseInt(String.valueOf(objs[ind++])));
		vol.setDiskFrom(String.valueOf(objs[ind++]));
		vol.setIsDeleted(String.valueOf(objs[ind++]));
		vol.setCreateTime((Date)objs[ind++]);
		vol.setDeleteTime((Date)objs[ind++]);
		vol.setDcId(String.valueOf(objs[ind++]));
		vol.setPrjId(String.valueOf(objs[ind++]));
		vol.setPrjName(String.valueOf(objs[ind++]));
		vol.setVmId(String.valueOf(objs[ind++]));
		vol.setVmName(String.valueOf(objs[ind++]));
		vol.setVolDescription(String.valueOf(objs[ind++]));
		vol.setBindPoint(String.valueOf(objs[ind++]));
		vol.setVmStatus(String.valueOf(objs[ind++]));
		vol.setVolBootable(String.valueOf(objs[ind++]));
		vol.setPayType(String.valueOf(objs[ind++]));
		vol.setEndTime((Date)objs[ind++]);
		vol.setChargeState(String.valueOf(objs[ind++]));
		vol.setDcName(String.valueOf(objs[ind++]));
		newList.set(i, vol);
	}
	return page;

}

/**
 * 根据订单编号查询订单信息
 * @param orderNo
 * @return
 */
public CloudOrderVolume queryCloudOrderByOrderNo(String orderNo){
	return volOrderService.getByOrder(orderNo);
}


/**
 * 根据云硬盘ID查询云硬盘的计费队列需要的信息
 * @param volId
 * @return
 */
public CloudVolume queryVolChargeById(String volId){
	
	BaseCloudVolume baseCloudVolume =volumeDao.findOne(volId);
	CloudVolume volume=new CloudVolume();
	BeanUtils.copyPropertiesByModel(volume, baseCloudVolume);

	if(null != volume.getEndTime()){
		volume.setCycleCount(DateUtil.getUgradeRemainDays(new Date(), volume.getEndTime()));
	}

	return volume;
}


/**
 * 查询该订单对应的升级或续费资源是否存在
 * -----------------------
 * @author chengxiaodong
 * @param orderNo
 * @return
 */
public boolean isExistsByOrderNo(String orderNo) {
	boolean isExist = false;
	StringBuffer sql = new StringBuffer();
	
	sql.append("		SELECT                            ");
	sql.append("			vol.vol_id,                   ");
	sql.append("			vol.vol_name                  ");
	sql.append("		FROM                              ");
	sql.append("			cloud_volume vol              ");
	sql.append("		LEFT JOIN cloudorder_volume cov   ");
	sql.append("		ON cov.vol_id = vol.vol_id        ");
	sql.append("		AND (                             ");
	sql.append("			cov.order_type = '1'          ");
	sql.append("			OR cov.order_type = '2'       ");
	sql.append("		)                                 ");
	sql.append("		WHERE                             ");
	sql.append("			cov.order_no = ?              ");
	sql.append("		AND vol.is_deleted = '0'          ");
	sql.append("		AND vol.is_visable = '1'          ");
	
	javax.persistence.Query query = volumeDao.createSQLNativeQuery(sql.toString(), new Object[]{orderNo});
	@SuppressWarnings("rawtypes")
	List listResult = query.getResultList();
	if(null != listResult && listResult.size() == 1) {
		isExist = true;
	}
	
	return isExist;
}


/**
 * 根据资源id查询指定资源
 */
public ResourceCheckBean isExistsByResourceId(String resourceId) {
	BaseCloudVolume baseVol=volumeDao.isExistsByResourceId(resourceId);
	ResourceCheckBean checkBean=new ResourceCheckBean();
	if(null!=baseVol){
		checkBean.setResourceName(baseVol.getVolName());
		checkBean.setExisted(true);
	}else{
		checkBean.setExisted(false);
	}
	
	
	return checkBean;
}

/**
 * 查询已在回收站过期的云硬盘列表
 * 
 * @author zhouhaitao
 * @param seconds
 * @return
 */
@SuppressWarnings("rawtypes")
public List<CloudVolume> queryRecycleVolumeList(long seconds){
	StringBuffer sql = new StringBuffer();
	List<CloudVolume> volList = new ArrayList<CloudVolume>();
	sql.append("		SELECT                    ");
	sql.append("			vol.dc_id,            ");
	sql.append("			vol.prj_id,           ");
	sql.append("			vol.vol_id,           ");
	sql.append("			prj.customer_id       ");
	sql.append("		FROM                      ");
	sql.append("			cloud_volume vol      ");
	sql.append("		left join cloud_project prj");
	sql.append("		on prj.prj_id = vol.prj_id");
	sql.append("		WHERE                     ");
	sql.append("			TIMESTAMPDIFF(        ");
	sql.append("				SECOND,           ");
	sql.append("				vol.delete_time,  ");
	sql.append("				NOW()             ");
	sql.append("			) > ?                 ");
	sql.append("		AND vol.is_deleted = '2'  ");
	
	javax.persistence.Query query = volumeDao.createSQLNativeQuery(sql.toString(), new Object[]{seconds});
	
	List list = query.getResultList();
	if(null != list &&  list.size()>0){
		for(int i =0 ;i<list.size();i++){
			int index = 0;
			Object [] objs = (Object []) list.get(i);
			CloudVolume volume = new CloudVolume();
			
			volume.setDcId(String.valueOf(objs[index++]));
			volume.setPrjId(String.valueOf(objs[index++]));
			volume.setVolId(String.valueOf(objs[index++]));
			volume.setCusId(String.valueOf(objs[index++]));
			
			volList.add(volume);
		}
	}
	return volList;
}

	/**
	 * 刷新云主机对应的数据盘状态
	 * @param vmId 云主机ID
	 */
	public void deleteDataVolumeByVm(String vmId){
		StringBuffer querySql = new StringBuffer();
		
		querySql.append("from BaseCloudVolume where vmId = ? and volBootable ='0'");
		@SuppressWarnings("unchecked")
		List<BaseCloudVolume> volumeList = volumeDao.find(querySql.toString(), new Object[]{vmId});
		for(BaseCloudVolume vol:volumeList){
			vol.setVolStatus("DETACHING");
			vol.setBindPoint(null);
			vol.setVmId(null);
			
			volumeDao.merge(vol);
			
			JSONObject json =new JSONObject();
			json.put("volId", vol.getVolId());
			json.put("dcId",vol.getDcId());
			json.put("prjId", vol.getPrjId());
			json.put("volStatus",vol.getVolStatus());
			json.put("count", "0");
			
			final JSONObject datas = json;
			TransactionHookUtil.registAfterCommitHook(new Hook() {
				@Override
				public void execute() {
					jedisUtil.addUnique(RedisKey.volKey, datas.toJSONString());
				}
			});
		}
	}
	
	
	
	/**
	 * 购买云主机时同时也购买数据盘，数据盘创建方法
	 */
	@Transactional(noRollbackFor=AppException.class)
	public List<BaseCloudVolume> addVmsAndVolumes(CloudOrderVolume volOrder,List<BaseCloudVolume> dataVolumeList)throws AppException {

		try{
		    int count=volOrder.getVolNumber();
			
			for(int i=1;i<=count;i++){
                String volName=AutoVolumeName(volOrder.getDcId(), volOrder.getPrjId());
				JSONObject data=new JSONObject();
				JSONObject temp=new JSONObject();
				
				temp.put("name", volName);
				temp.put("size", volOrder.getVolSize());
			    temp.put("imageRef", null);
				if(null!=volOrder.getVolTypeId()&&!"".equals(volOrder.getVolTypeId())){
					temp.put("volume_type", volOrder.getVolTypeId());
				}
				data.put("volume", temp);
				Volume result=volumeService.create(volOrder.getDcId(), volOrder.getPrjId(), data);
				BaseCloudVolume volume=new BaseCloudVolume();
				volume.setVolId(result.getId());
				volume.setVolName(result.getName());
				volume.setCreateTime(new Date());
				volume.setCreateName(volOrder.getCreateUser());
				volume.setDcId(volOrder.getDcId());
				volume.setPrjId(volOrder.getPrjId());
				if(true==result.getBootable()){
					volume.setVolBootable("1");
				}else{
					volume.setVolBootable("0");
				}
				volume.setDiskFrom(volOrder.getDiskFrom());
				volume.setVolSize(volOrder.getVolSize());
				volume.setVolStatus(result.getStatus().toUpperCase());
				volume.setVolDescription(result.getDescription());
				volume.setVolTypeId(volOrder.getVolTypeId());
				volume.setIsVisable("0");
				volume.setIsDeleted("0");
				volume.setTypeSuccess("1");
				volume.setChargeState(CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE);
				volume.setPayType(volOrder.getPayType());
				if(volOrder.getPayType().equals("1")){
					volume.setEndTime(DateUtil.getExpirationDate(volume.getCreateTime(), volOrder.getBuyCycle(), DateUtil.PURCHASE));
				}
				
				volumeDao.saveOrUpdate(volume);
				
				BaseCloudBatchResource resource=new BaseCloudBatchResource();
				resource.setOrderNo(volOrder.getOrderNo());
				resource.setResourceId(volume.getVolId());
				resource.setResourceType(CloudBatchResource.RESOURCE_VOLUME);
				cloudBatchResourceService.save(resource);
				dataVolumeList.add(volume);
			}

		}catch(AppException e){
		    log.error(e.getMessage(),e);
			throw e;
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			throw new AppException ("error.openstack.message");
		}
		 return dataVolumeList;	
	}

	
	/**
	 * 
	 * 
	 */
	private String AutoVolumeName(String dcId,String prjId){
		String volName="";
		try {
			while(true){
				String dateStr=new Date().getTime()+"";
				volName="yp_"+dateStr;
				CloudVolume volume=new CloudVolume();
				volume.setDcId(dcId);
				volume.setPrjId(prjId);
				volume.setVolName(volName);
				if(getVolumeByName(volume)){
					break;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return volName;
	}
	
	private String  getVolumeTypeForDis(String volumeType){
		String after="";
		
		if(null!=volumeType){
			if("1".equals(volumeType)){
				after="普通型"; 
			}else if("2".equals(volumeType)){
				after="性能型"; 
			}else if("3".equals(volumeType)){
				after="超高性能型"; 
			}else{
				after="普通型";
			}
		}
		
		return after;

	}


	/**
	 * 购买云主机时同时购买数据盘失败
	 */
	@Override
	public void volumeFailedHandler(CloudOrderVm order) {
		List<CloudVolume> volList = queryVolumeListByOrder(order.getOrderNo());
		if (null != volList && volList.size() > 0) {
			for (CloudVolume vol : volList) {
				
				volumeService.delete(vol.getDcId(), vol.getPrjId(), vol.getVolId());
				volumeDao.delete(vol.getVolId());
				
				CloudBatchResource cloudBatchResource = new CloudBatchResource();
				cloudBatchResource.setOrderNo(order.getOrderNo());
				cloudBatchResource.setResourceId(vol.getVolId());

				cloudBatchResourceService.delete(cloudBatchResource);
				
			}
		}
		
	}
	
	
	/**
	 * 根据订单编号 查询对应的已经创建的云硬盘列表 
	 * 
	 * @param orderNo
	 *            订单编号
	 * @return
	 * 
	 */
	public List<CloudVolume> queryVolumeListByOrder(String orderNo) {
		List<CloudVolume> volList = new ArrayList<CloudVolume>();
		StringBuffer sql = new StringBuffer();
		sql.append("		SELECT                                                      ");
		sql.append("			vol.dc_id,                                              ");
		sql.append("			vol.prj_id,                                             ");
		sql.append("			vol.vol_id,                                             ");
		sql.append("			vol.vol_status                                          ");
		sql.append("		FROM                                                        ");
		sql.append("			cloud_batchresource cbr                                 ");
		sql.append("		LEFT JOIN cloud_volume vol ON vol.vol_id = cbr.resource_id  ");
		sql.append("		AND resource_type = 'volume'                                ");
		sql.append("		WHERE                                                       ");
		sql.append("			cbr.order_no = ?                                        ");

		javax.persistence.Query query = volumeDao.createSQLNativeQuery(sql.toString(), new Object[] { orderNo });

		@SuppressWarnings("rawtypes")
		List result = query.getResultList();
		if (result != null && result.size() > 0) {
			for (int i = 0; i < result.size(); i++) {
				int index = 0;
				Object[] objs = (Object[]) result.get(i);
				CloudVolume cloudVolume = new CloudVolume();
				cloudVolume.setDcId(String.valueOf(objs[index++]));
				cloudVolume.setPrjId(String.valueOf(objs[index++]));
				cloudVolume.setVolId(String.valueOf(objs[index++]));
				cloudVolume.setVolStatus(String.valueOf(objs[index++]));
				volList.add(cloudVolume);
			}
		}
		return volList;
	}


	
	/**
	 * 查询指定云主机的系统盘信息
	 */
	@Override
	public CloudVolume getSysVolumeByVmId(String vmId) {
		int index=0;
		Object [] args=new Object[1];
		StringBuffer sql=new StringBuffer();
		sql.append("select vol.vol_id AS volId,vol.vol_name AS volName,vol.vol_status AS volStatus,vol.vol_size AS volSize,vol.vol_bootable AS volBootable,vol.disk_from AS diskFrom,vol.create_time AS createTime,vol.vm_id as vmId,vol.vol_description AS volDescription,vol.dc_id as dcId,vol.prj_id as prjId,vol.bind_point as bindPoint,vol.pay_type as payType,vol.end_time as endTime,vol.charge_state as chargeState,vol.is_deleted as isDeleted,vol.delete_time as deleteTime,");
		sql.append(" vol.vol_typeid as volTypeId,type.volume_type as volType,type.max_size as maxSize from cloud_volume vol");
		sql.append(" left join cloud_volumetype type ON vol.vol_typeid=type.type_id");
		sql.append(" where 1=1 and vol.vol_bootable='1' ");
		if (!"null".equals(vmId)&&null!=vmId&&!"".equals(vmId)&&!"undefined".equals(vmId)) {
			sql.append(" and vol.vm_id=?");
			args[index]=vmId;
			index++;
		}
		Object[] params = new Object[index];  
        System.arraycopy(args, 0, params, 0, index);
        javax.persistence.Query query = volumeDao.createSQLNativeQuery(sql.toString(), params);
        List listResult=new ArrayList();
        CloudVolume volume=null;
        if(null!=query){
        	listResult = query.getResultList();
        }
        if (null != listResult && listResult.size() == 1) {
        	Object[] objs = (Object[])listResult.get(0);
        	volume=new CloudVolume();
        	volume.setVolId(String.valueOf(objs[0]));
        	volume.setVolName(String.valueOf(objs[1]));
        	volume.setVolStatus(String.valueOf(objs[2]));
        	volume.setVolSize(Integer.parseInt(String.valueOf(objs[3])));
        	volume.setVolBootable(String.valueOf(objs[4]));
        	volume.setDiskFrom(String.valueOf(objs[5]));
        	volume.setCreateTime((Date)objs[6]);
        	volume.setVmId(String.valueOf(objs[7]));
        	volume.setVolDescription(String.valueOf(objs[8]));
        	volume.setDcId(String.valueOf(objs[9]));
        	volume.setPrjId(String.valueOf(objs[10]));
        	volume.setBindPoint(String.valueOf(objs[11]));
        	volume.setPayType(String.valueOf(objs[12]));
        	volume.setEndTime((Date)objs[13]);
        	volume.setChargeState(String.valueOf(objs[14]));
        	volume.setIsDeleted(String.valueOf(objs[15]));
        	volume.setDeleteTime((Date)objs[16]);
        	volume.setVolTypeId(String.valueOf(objs[17]));
        	volume.setVolType(String.valueOf(objs[18]));
        	volume.setMaxSize(Integer.parseInt(null!=objs[19]?String.valueOf(objs[19]):"2048"));
        	String volumeTypeAs=getVolumeTypeForDis(String.valueOf(objs[18]));
        	volume.setVolumeTypeAs(volumeTypeAs);
        	if(!"DELETING".equals(volume.getVolStatus().toUpperCase())){
        		volume.setStatusForDis(CloudResourceUtil.escapseChargeState(volume.getChargeState()));
        	}else{
        		volume.setStatusForDis(DictUtil.getStatusByNodeEn("volume", volume.getVolStatus()));
        	}
        	if (null==volume.getChargeState()||"".equals(volume.getChargeState())||"null".equals(volume.getChargeState())||CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(volume.getChargeState())) {
        		volume.setStatusForDis(DictUtil.getStatusByNodeEn("volume", volume.getVolStatus()));
			}       	

        } 	
		return volume;
	}
	
	
}
