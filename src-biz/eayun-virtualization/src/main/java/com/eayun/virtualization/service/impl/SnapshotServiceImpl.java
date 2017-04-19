package com.eayun.virtualization.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.Hook;
import com.eayun.costcenter.model.MoneyAccount;
import com.eayun.costcenter.service.AccountOverviewService;
import com.eayun.customer.model.Customer;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.eayunstack.model.BackUp;
import com.eayun.eayunstack.model.Restore;
import com.eayun.eayunstack.service.OpenstackSnapshotService;
import com.eayun.eayunstack.service.OpenstackVolumeService;
import com.eayun.notice.model.MessageOrderResourceNotice;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.order.model.BaseOrder;
import com.eayun.order.model.BaseOrderResource;
import com.eayun.order.model.Order;
import com.eayun.order.service.OrderService;
import com.eayun.price.bean.ParamBean;
import com.eayun.price.service.BillingFactorService;
import com.eayun.project.service.ProjectService;
import com.eayun.syssetup.service.SysDataTreeService;
import com.eayun.virtualization.dao.CloudSnapshotDao;
import com.eayun.virtualization.dao.CloudVolumeDao;
import com.eayun.virtualization.model.BaseCloudOrderSnapshot;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.BaseCloudSnapshot;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.CloudOrderSnapshot;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.model.CloudSnapshot;
import com.eayun.virtualization.service.CloudBatchResourceService;
import com.eayun.virtualization.service.SnapshotOrderService;
import com.eayun.virtualization.service.SnapshotService;
import com.eayun.virtualization.service.TagService;

@Service
@Transactional
public class SnapshotServiceImpl implements SnapshotService {
    private static final Logger log = LoggerFactory.getLogger(SnapshotServiceImpl.class);
	@Autowired
	private OpenstackSnapshotService snapshotService;
	@Autowired
	private OpenstackVolumeService volumeService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private SnapshotOrderService    orderSnapService;
	@Autowired
	private TagService tagService;
	@Autowired
	private CloudSnapshotDao snapDao;
	@Autowired
	private CloudVolumeDao volumeDao;
	@Autowired
	private JedisUtil jedisUtil;
	
	@Autowired
	private OrderService orderService;
	@Autowired
	private SnapshotOrderService snapOrderService;
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
	
	
	
	
	
	/**
	 * 回滚云硬盘
	 * @throws AppException 
	 */
	@Override
	public void rollBackVolume(CloudSnapshot snapshot,SessionUserInfo sessionUser) throws AppException {
		try{
			JSONObject data = new JSONObject();
			JSONObject json = new JSONObject();
			json.put("backup_id", snapshot.getSnapId());
			json.put("volume_id", snapshot.getVolId());
			data.put("restore", json);
			
			Restore restore=volumeService.restoreVolume(snapshot.getDcId(), snapshot.getPrjId(), snapshot.getSnapId(), data);
			if(null!=restore){
				BaseCloudSnapshot baseCloudSnap=snapDao.getSnapshotById(snapshot.getSnapId());
				baseCloudSnap.setSnapStatus("RESTORING");	
				snapDao.saveOrUpdate(baseCloudSnap);
				
				//TODO 同步新增云硬盘备份状态（回滚中变为正常）
				JSONObject jsons =new JSONObject();
				jsons.put("snapId", baseCloudSnap.getSnapId());
				jsons.put("dcId",baseCloudSnap.getDcId());
				jsons.put("prjId",baseCloudSnap.getPrjId());
				jsons.put("snapStatus",baseCloudSnap.getSnapStatus());
				jsons.put("count", "0");
				//jedisUtil.push(RedisKey.volSphKey, jsons.toJSONString());
				final JSONObject datas = jsons;
				TransactionHookUtil.registAfterCommitHook(new Hook() {
					@Override
					public void execute() {
						jedisUtil.addUnique(RedisKey.volSphKey, datas.toJSONString());
					}
				});
				
				BaseCloudVolume baseVolume=volumeDao.findOne(snapshot.getVolId());
				baseVolume.setVolStatus("RESTORING-BACKUP");
				volumeDao.saveOrUpdate(baseVolume);
				//TODO 同步新增云硬盘状态(回滚中变为正常)
				JSONObject bason =new JSONObject();
				bason.put("volId", baseVolume.getVolId());
				bason.put("dcId",baseVolume.getDcId());
				bason.put("prjId", baseVolume.getPrjId());
				bason.put("volStatus",baseVolume.getVolStatus());
				bason.put("count", "0");
				//jedisUtil.push(RedisKey.volKey, bason.toJSONString());
				final JSONObject quate = bason;
				TransactionHookUtil.registAfterCommitHook(new Hook() {
					@Override
					public void execute() {
						jedisUtil.addUnique(RedisKey.volKey, quate.toJSONString());
					}
				});
				
			}
			
		}catch(AppException e){
			throw e;
		}catch(Exception e){
			log.error(e.toString(),e);
		}
		
	}
	
	
	
	
	/**
	 * 购买云硬盘备份
	 */
	@Override
	public String buySnapshot(CloudOrderSnapshot orderSnap,SessionUserInfo sessionUser) throws Exception {

		String errMsg = null;
        try {
        	
        	if(null!=orderSnap.getFromVmId()&&!"".equals(orderSnap.getFromVmId())&&!"null".equals(orderSnap.getFromVmId())){
        		orderSnap.setFromVmId(null);
        	}
        	if(null!=orderSnap.getFromVolId()&&!"".equals(orderSnap.getFromVolId())&&!"null".equals(orderSnap.getFromVolId())){
        		orderSnap.setFromVolId(null);
        	}
        	
        	orderSnap.setCreateUser(sessionUser.getUserName());
        	orderSnap.setCusId(sessionUser.getCusId());
        	orderSnap.setCreateOrderDate(new Date());
			errMsg = checkSnapshotQuota(orderSnap);//验证配额
			if(!StringUtils.isEmpty(errMsg)){
				errMsg ="OUT_OF_QUOTA";
				return errMsg;
			}
			if(PayType.PAYAFTER.equals(orderSnap.getPayType())){
				MoneyAccount accountMoney = accountOverviewSerivce.getAccountInfo(sessionUser.getCusId());
				//TODO 开通服务余额限定值
				String buyCondition = sysDataTreeService.getBuyCondition();
				BigDecimal createResourceLimitedMoney = new BigDecimal(buyCondition);
				if(accountMoney.getMoney().compareTo(createResourceLimitedMoney)<0){
					errMsg = "NOT_SUFFICIENT_FUNDS";
					return errMsg;
				}
			}
			
			Order order = createSnapshotOrder(orderSnap, sessionUser);
			
			orderSnap.setOrderNo(order.getOrderNo());//设置订单编号
			snapOrderService.addOrderSnapshot(orderSnap);
			
			if(PayType.PAYAFTER.equals(orderSnap.getPayType())){
				try{
					addVolumeBack(order.getOrderNo(),sessionUser);
				}catch(Exception e){
				    log.error(e.getMessage(),e);
					throw new Exception(e.getMessage());
				}
				
			}
			return errMsg;
			
		} catch (Exception e) {
			log.error(e.toString(),e);
			throw e;
		}
		
	}
	
	
	




	/**
	 * 创建云硬盘备份
	 */
	@Transactional(noRollbackFor=AppException.class)
	public void addVolumeBack(String orderNo, SessionUserInfo sessionUser)throws AppException {
		BaseCloudOrderSnapshot  snapOrder=null;
		BaseCloudSnapshot snapshot=null;
		try {
			snapOrder=orderSnapService.getSnapOrderByOrderNo(orderNo);
			JSONObject data = new JSONObject();
			JSONObject json = new JSONObject();
			json.put("description", snapOrder.getSnapDescription());
			json.put("container", null);
			json.put("name", snapOrder.getSnapName());
			json.put("volume_id", snapOrder.getVolId());
			json.put("force", "true");
			data.put("backup", json);
			BackUp backUp=volumeService.createBackUps(snapOrder.getDcId(), snapOrder.getPrjId(), data);
			if(null!=backUp){
				snapshot=new BaseCloudSnapshot();
				snapshot.setSnapId(backUp.getId());
				snapshot.setSnapName(snapOrder.getSnapName());
				snapshot.setCreateTime(new Date());
				snapshot.setCreateName(sessionUser.getUserName());
				snapshot.setPrjId(snapOrder.getPrjId());
				snapshot.setDcId(snapOrder.getDcId());
				snapshot.setSnapSize(snapOrder.getSnapSize());
				snapshot.setSnapType(snapOrder.getSnapType());
				snapshot.setVolId(snapOrder.getVolId());
				snapshot.setSnapStatus("CREATING");
				snapshot.setSnapDescription(snapOrder.getSnapDescription());
				snapshot.setPayType("2");
				snapshot.setChargeState(CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE);
				snapshot.setIsDeleted("0");
				snapshot.setSnapType(snapOrder.getSnapType());
				snapshot.setIsVisable("0");
				
				snapDao.saveOrUpdate(snapshot);
				
				if(null!=snapshot.getSnapStatus()){
					//TODO 同步新增云硬盘备份状态
					JSONObject jsons =new JSONObject();
					jsons.put("snapId", snapshot.getSnapId());
					jsons.put("orderNo", snapOrder.getOrderNo());
					jsons.put("cusId", sessionUser.getCusId());
					jsons.put("createName",snapOrder.getCreateUser());
					jsons.put("createTime",snapOrder.getCreateOrderDate());
					jsons.put("dcId",snapshot.getDcId());
					jsons.put("prjId",snapshot.getPrjId());
					jsons.put("snapStatus",snapshot.getSnapStatus());
					jsons.put("count", "0");
					//jedisUtil.push(RedisKey.volSphKey, jsons.toJSONString());
					
					final JSONObject quate = jsons;
					TransactionHookUtil.registAfterCommitHook(new Hook() {
						@Override
						public void execute() {
							jedisUtil.addUnique(RedisKey.volSphKey, quate.toJSONString());
						}
					});
					
					
				}
				
				BaseCloudVolume baseVolume=volumeDao.findOne(snapOrder.getVolId());
				
				if(null!=baseVolume){
					baseVolume.setVolStatus("BACKING-UP");
					volumeDao.saveOrUpdate(baseVolume);
					//TODO 同步新增备份时云硬盘状态(备份中变为正常)
					JSONObject bason =new JSONObject();
					bason.put("volId", baseVolume.getVolId());
					bason.put("dcId",baseVolume.getDcId());
					bason.put("prjId", baseVolume.getPrjId());
					bason.put("volStatus",baseVolume.getVolStatus());
					bason.put("count", "0");
					//jedisUtil.push(RedisKey.volKey, bason.toJSONString());
					
					final JSONObject datas = bason;
					TransactionHookUtil.registAfterCommitHook(new Hook() {
						@Override
						public void execute() {
							jedisUtil.addUnique(RedisKey.volKey, datas.toJSONString());
						}
					});
				}
				
			}

		}catch(AppException e){
			//回调订单接口
			//发消息
			CloudOrderSnapshot order=new CloudOrderSnapshot();
			BeanUtils.copyPropertiesByModel(order, snapOrder);
			order.setCusId(sessionUser.getCusId());
			snapOrderFail(snapshot,order);
			throw e;
			
		}catch (Exception e) {
			log.error(e.toString(),e);
			CloudOrderSnapshot order=new CloudOrderSnapshot();
			BeanUtils.copyPropertiesByModel(order, snapOrder);
			order.setCusId(sessionUser.getCusId());
			snapOrderFail(snapshot,order);
			throw new AppException("error.openstack.message");
		}
		
	}
	
	
/**
 * 获取备份列表
 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
	public Page getSnapshotList(Page page, String prjId, String dcId,
			String snapName, String isDeleted,QueryMap queryMap) throws Exception{
		snapName = snapName.replaceAll("\\_", "\\\\_");
		int index=0;
		Object [] args=new Object[4];
		StringBuffer sql=new StringBuffer();
		sql.append("select snap.snap_id as snapId,snap.snap_name as snapName,");
		sql.append(" snap.snap_size as snapSize,snap.snap_status as snapStatus,snap.snap_description as snapDescription,");
		sql.append(" snap.prj_id as prjId,snap.dc_id as dcId,snap.create_time as createTime,snap.vol_id as volId,vol.vol_name as volName,");
		sql.append(" count(volume.vol_id) as volNum,prj.prj_name as prjName,dc.dc_name as dcName,snap.pay_type as payType,snap.charge_state as chargeState,");
		sql.append(" snap.snap_type as snapType");
		sql.append(" from cloud_disksnapshot snap ");
		sql.append(" left join (select vol_id,vol_name from cloud_volume  where is_deleted='0' and is_visable='1' ) vol on snap.vol_id = vol.vol_id");
		sql.append(" left join cloud_volume volume on snap.snap_id=volume.from_snapid and volume.is_deleted=0");
		sql.append(" left join cloud_project prj on snap.prj_id=prj.prj_id");
		sql.append(" left join dc_datacenter dc on snap.dc_id=dc.id");
		sql.append(" where 1=1 and snap.is_visable='1'");
		
		if (!"null".equals(isDeleted)&&null!=isDeleted&&!"".equals(isDeleted)&&!"undefined".equals(isDeleted)) {
			sql.append(" and snap.is_deleted=?");
			args[index]=isDeleted;
			index++;
		}
		if (!"null".equals(dcId)&&null!=dcId&&!"".equals(dcId)&&!"undefined".equals(dcId)) {
			sql.append(" and snap.dc_id=?");
			args[index]=dcId;
			index++;
		}
		if (!"null".equals(prjId)&&null!=prjId&&!"".equals(prjId)&&!"undefined".equals(prjId)) {
			sql.append(" and snap.prj_id=?");
			args[index]=prjId;
			index++;
		}
		
		if(null!=snapName&&!"".equals(snapName)){
		    	sql.append(" and binary snap.snap_name like ?");
		    	args[index]="%"+snapName+"%";
				index++;
		}
		sql.append(" group by snap.snap_id order by snap.create_time desc");
		
		Object[] params = new Object[index];  
        System.arraycopy(args, 0, params, 0, index);
        page=snapDao.pagedNativeQuery(sql.toString(),queryMap,params);
        List newList = (List)page.getResult();
        for(int i=0;i<newList.size();i++){
        	Object[] objs = (Object[])newList.get(i);
        	CloudSnapshot snapshot= new CloudSnapshot();
        	snapshot.setSnapId(String.valueOf(objs[0]));
        	snapshot.setSnapName(String.valueOf(objs[1]));
        	snapshot.setSnapSize(Integer.parseInt(String.valueOf(objs[2])));
        	snapshot.setSnapStatus(String.valueOf(objs[3]));
        	snapshot.setSnapDescription(String.valueOf(objs[4]));
        	snapshot.setPrjId(String.valueOf(objs[5]));
        	snapshot.setDcId(String.valueOf(objs[6]));
        	snapshot.setCreateTime((Date)objs[7]);
        	snapshot.setCreateTimeForDis(DateUtil
					.dateToString((Date)objs[7]));
        	snapshot.setVolId(String.valueOf(objs[8]));
        	snapshot.setVolName(String.valueOf(objs[9]));
        	snapshot.setVolNum(Integer.parseInt(String.valueOf(objs[10])));
        	snapshot.setPrjName(String.valueOf(objs[11]));
        	snapshot.setDcName(String.valueOf(objs[12]));
        	snapshot.setPayType(String.valueOf(objs[13]));
        	snapshot.setChargeState(String.valueOf(objs[14]));
        	snapshot.setSnapType(String.valueOf(objs[15]));
        	snapshot.setStatusForDis(CloudResourceUtil.escapseChargeState(snapshot.getChargeState()));
        	if (null == snapshot.getChargeState()
        			|| "".equals(snapshot.getChargeState())
        			|| "null".equals(snapshot.getChargeState())
        			|| CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(snapshot.getChargeState())
        			|| "DELETING".equals(snapshot.getSnapStatus())) {
        		snapshot.setStatusForDis(DictUtil.getStatusByNodeEn("snapshot",snapshot.getSnapStatus()));
			}
        	newList.set(i, snapshot);
        }
		return page;
	}
	

	/**
	 *删除备份
	 */
	@Override
	public boolean deleteSnapshot(CloudSnapshot snap,SessionUserInfo sessionUser) throws AppException {
		try{
			BaseCloudSnapshot snapshot=snapDao.findOne(snap.getSnapId());
			if("2".equals(snap.getIsDeleted())){
				snapshot.setDeleteTime(new Date());
				snapshot.setDeleteUser(sessionUser.getUserName());
				snapshot.setIsDeleted("2");
				snapDao.saveOrUpdate(snapshot);
				
				//通知计费模块
				ChargeRecord record = new ChargeRecord ();
				record.setDatecenterId(snapshot.getDcId());
				record.setCusId(sessionUser.getCusId());
				record.setResourceId(snapshot.getSnapId());
				record.setResourceName(snapshot.getSnapName());
				record.setResourceType(ResourceType.DISKSNAPSHOT);
				record.setOpTime(new Date());
				rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_RECYCLE, JSONObject.toJSONString(record));
				return true;
			}else{
				boolean flag=volumeService.deleteBackUps(snapshot.getDcId(), snapshot.getPrjId(), snapshot.getSnapId());
				if(flag){
					//通知计费模块
					if(!"2".equals(snapshot.getIsDeleted())){
						ChargeRecord record = new ChargeRecord ();
						record.setDatecenterId(snapshot.getDcId());
						record.setCusId(sessionUser.getCusId());
						record.setResourceId(snapshot.getSnapId());
						record.setResourceType(ResourceType.DISKSNAPSHOT);
						record.setResourceName(snapshot.getSnapName());
						record.setOpTime(new Date());
						rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_DELETE, JSONObject.toJSONString(record));
					}
					
					
					snapshot.setSnapStatus("DELETING");
					snapDao.saveOrUpdate(snapshot);
					tagService.refreshCacheAftDelRes("diskSnapshot",snap.getSnapId());
					//TODO 删除云硬盘备份的自动任务
					JSONObject json =new JSONObject();
					json.put("snapId", snapshot.getSnapId());
					json.put("dcId",snapshot.getDcId());
					json.put("prjId",snapshot.getPrjId());
					json.put("snapStatus",snapshot.getSnapStatus());
					json.put("count", "0");
					//jedisUtil.addUnique(RedisKey.volSphKey, json.toJSONString());
					
					final JSONObject datas = json;
					TransactionHookUtil.registAfterCommitHook(new Hook() {
						@Override
						public void execute() {
							jedisUtil.addUnique(RedisKey.volSphKey, datas.toJSONString());
						}
					});
					
					
				}
			}
			return true;
		}catch(AppException e){
			throw e;
		}
	}
	

	
	
	/**
	 * 更新备份
	 */
	@Override
	public boolean updateSnapshot(CloudSnapshot snap) throws AppException {
		try{
			BaseCloudSnapshot baseSnapshot=snapDao.findOne(snap.getSnapId());
			baseSnapshot.setSnapName(snap.getSnapName());
			baseSnapshot.setSnapDescription(snap.getSnapDescription());
			snapDao.saveOrUpdate(baseSnapshot);
			return true;
		}catch(AppException e){
			throw e;
		}
	}
	
	
	/**
	 * 验证重名
	 */
	@Override
	public boolean getSnapByName(CloudSnapshot snap) throws AppException {
		boolean flag = false;
		//云硬盘备份重名查询
		try{
			StringBuffer hql = new StringBuffer();
			hql.append(" from  ");
			hql.append(" BaseCloudSnapshot v where 1=1 ");
			hql.append(" and v.prjId = :prjId ");
			hql.append(" and v.isDeleted in ('0','2') ");
			hql.append(" and binary(v.snapName) = :names");
			if (!StringUtils.isEmpty(snap.getSnapId())) {
				hql.append(" and v.snapId <> :snapId");
			}
			org.hibernate.Query query = volumeDao.getHibernateSession().createQuery(hql.toString());
			query.setParameter("prjId", snap.getPrjId());
			query.setParameter("names", snap.getSnapName());
			if (!StringUtils.isEmpty(snap.getSnapId())) {
				query.setParameter("snapId", snap.getSnapId());
			}
			@SuppressWarnings("unchecked")
			List<BaseCloudSnapshot> queryList = query.list();
			flag = (null == queryList || queryList.size() == 0);
			
			if(flag){
				StringBuffer orderSnapHql = new StringBuffer();
				orderSnapHql.append("	SELECT                                                 ");
				orderSnapHql.append("		cov.snap_name                                      ");
				orderSnapHql.append("	FROM                                                   ");
				orderSnapHql.append("		cloudorder_snapshot cov                            ");
				orderSnapHql.append("	LEFT JOIN order_info oi ON oi.order_no = cov.order_no  ");
				orderSnapHql.append("	WHERE                                                  ");
				orderSnapHql.append("		binary(cov.snap_name) = ?                          ");
				orderSnapHql.append("	AND cov.order_type = '0'                               ");
				orderSnapHql.append("	AND oi.order_state in ('1','2')                        ");
				orderSnapHql.append("	AND cov.prj_id=?                                       ");
				javax.persistence.Query orderSnapQuery = snapDao.createSQLNativeQuery(orderSnapHql.toString(),new Object[]{snap.getSnapName()},snap.getPrjId());
				@SuppressWarnings("rawtypes")
				List orderSnapList = orderSnapQuery.getResultList();
				for(int i = 0;i<orderSnapList.size();i++){
					Object [] obj = (Object [])orderSnapList.get(i);
					String snapName = String.valueOf(obj[0]);
					if(snap.getSnapName().equals(snapName)){
						flag = false;
						break;
					}
					
				}
			}
			
		}catch(Exception e){
			throw e;
		}
		return flag;
		
	}
	
	
	/**
	 * 查询指定云硬盘创建的备份
	 */
	@Override
	public List<CloudSnapshot> getSnapListByVolId(String volId) throws Exception {
		List<BaseCloudSnapshot> listSnap=snapDao.getSnapListByVolId(volId);
    	List<CloudSnapshot> result = new ArrayList<CloudSnapshot>();
		for (BaseCloudSnapshot baseCloudSnapshot : listSnap) {
			if("0".equals(baseCloudSnapshot.getIsDeleted())&&"1".equals(baseCloudSnapshot.getIsVisable())){
				CloudSnapshot cloudSnapshot= new CloudSnapshot();
				BeanUtils.copyPropertiesByModel(cloudSnapshot, baseCloudSnapshot);
				cloudSnapshot.setStatusForDis(CloudResourceUtil.escapseChargeState(cloudSnapshot.getChargeState()));
	        	if (null==cloudSnapshot.getChargeState()||"".equals(cloudSnapshot.getChargeState())||"null".equals(cloudSnapshot.getChargeState())||CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(cloudSnapshot.getChargeState())) {
	        		cloudSnapshot.setStatusForDis(DictUtil.getStatusByNodeEn("snapshot", baseCloudSnapshot.getSnapStatus()));
				}    
				result.add(cloudSnapshot);
			}
			
		}
		
		return result;
	}
	
	public boolean deleteSnap (CloudSnapshot cloudSnapshot){
		boolean flag = false ;
		try{
			snapDao.delete(cloudSnapshot.getSnapId());
			flag = true ;
		}catch(Exception e){
		    log.error(e.toString(),e);
			flag = false;
		}
		return flag ;
	}
	
	public boolean updateSnap (CloudSnapshot cloudSnapshot){
		boolean flag = false ;
		try{
			BaseCloudSnapshot snapshot = snapDao.findOne(cloudSnapshot.getSnapId());
			snapshot.setSnapStatus(cloudSnapshot.getSnapStatus());
			snapDao.saveOrUpdate(snapshot);
			flag = true ;
		}catch(Exception e){
		    log.error(e.toString(),e);
			flag = false;
		}
		return flag ;
	}
	
	
	@Override
	public int countSnapshotByPrjId(String prjId) throws AppException {
		int countSnap=snapDao.countSnapshotByPrjId(prjId);
		return countSnap;
	}
	
	
	/**
	 * 删除某云硬盘下所有的备份
	 * 可以放入回收站
	 */
	@Override
	public void deleteAllSnaps(String volId, String isDeleted,SessionUserInfo user)
			throws AppException {
		
		List<BaseCloudSnapshot> list=snapDao.getUnDelSnapListByVolId(volId);
	
			for(BaseCloudSnapshot snapshot :list){
				if("2".equals(isDeleted)){
					snapshot.setIsDeleted("2");
					snapshot.setDeleteTime(new Date());
					snapshot.setDeleteUser(user.getUserName());
					snapDao.saveOrUpdate(snapshot);
					
					//给计费模块发消息
					ChargeRecord record = new ChargeRecord ();
					record.setDatecenterId(snapshot.getDcId());
					record.setCusId(user.getCusId());
					record.setResourceId(snapshot.getSnapId());
					record.setResourceName(snapshot.getSnapName());
					record.setResourceType(ResourceType.DISKSNAPSHOT);
					record.setOpTime(new Date());
					rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_RECYCLE, JSONObject.toJSONString(record));
					
				}else{
					boolean flag=volumeService.deleteBackUps(snapshot.getDcId(), snapshot.getPrjId(), snapshot.getSnapId());
					if(flag){
					  snapDao.delete(snapshot);
					}
					
					//给计费模块发消息
					if(!"2".equals(snapshot.getIsDeleted())){
						ChargeRecord record = new ChargeRecord ();
						record.setDatecenterId(snapshot.getDcId());
						record.setCusId(user.getCusId());
						record.setResourceId(snapshot.getSnapId());
						record.setResourceType(ResourceType.DISKSNAPSHOT);
						record.setResourceName(snapshot.getSnapName());
						record.setOpTime(new Date());
						rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_DELETE, JSONObject.toJSONString(record));
					}
					
				}
				
			}		
			
	}
	
	
	/**
	 * 根据云硬盘备份订单组装 云硬盘备份配置
	 * 
	 * @param order
	 * @return
	 */
	private String snapshotConfig(CloudOrderSnapshot order){
		StringBuffer buffer = new StringBuffer();
		if(OrderType.NEW.equals(order.getOrderType())){
			buffer.append("数据中心：").append(order.getDcName()).append("<br>");
			buffer.append("云硬盘名称：").append(order.getVolName()).append("<br>");
			buffer.append("备份大小：").append(order.getSnapSize()+"GB");
		}
		return buffer.toString();

	}

	@SuppressWarnings("rawtypes")
    @Override
	public String getSnapshotNameById(String id) throws Exception{
		StringBuffer sb = new StringBuffer();
		sb.append("select snap_name from cloud_disksnapshot where snap_id=?");
		Query query = snapDao.createSQLNativeQuery(sb.toString(), id);
		List list = query.getResultList();
		String snapshotName = "";
		for(int i=0;i<list.size();i++){
			snapshotName = (String) list.get(i);
		}
		return snapshotName;
	}
	
	
	/**
	 * 创建备份订单
	 * @param orderSnap
	 * @param sessionUser
	 * @return
	 * @throws Exception
	 */
	private Order createSnapshotOrder(CloudOrderSnapshot orderSnap,SessionUserInfo user) throws Exception{
		Order order = new Order();
		order.setOrderType(OrderType.NEW);
		order.setDcId(orderSnap.getDcId());
		order.setProdCount(1);
		order.setProdConfig(snapshotConfig(orderSnap));
		order.setPayType(orderSnap.getPayType());
		order.setResourceType(ResourceType.DISKSNAPSHOT);
		order.setUserId(user.getUserId());
		order.setCusId(user.getCusId());
		
	
	    if(PayType.PAYAFTER.equals(orderSnap.getPayType())){
			order.setProdName("云硬盘备份-按需付费");
			order.setBillingCycle(BillingCycleType.HOUR);
		}
		orderService.createOrder(order);
		return order;
		
	}
	
	
	
	/**
	 * @author chengxiaodong
	 * 
	 * @param snapId
	 * 				云资源ID
	 * @param chargeState
	 * 				资源需要改变成的状态
	 * @param isClose
	 * 				是否停止服务    true需要停止服务
	 * @return 
	 * 		
	 */
	@Override
	public void modifyStateForSnap(String snapId, String chargeState,boolean isClose) throws Exception {
		BaseCloudSnapshot baseSnap=snapDao.findOne(snapId);
		BaseCloudProject project = projectService.findProject(baseSnap.getPrjId());
		baseSnap.setChargeState(chargeState);
		snapDao.saveOrUpdate(baseSnap);
		
		if("1".equals(chargeState)&&"2".equals(baseSnap.getPayType())&&isClose){
			ChargeRecord record = new ChargeRecord ();
			record.setDatecenterId(baseSnap.getDcId());
			record.setCusId(project.getCustomerId());
			record.setResourceId(baseSnap.getSnapId());
			record.setResourceType(ResourceType.DISKSNAPSHOT);
			record.setOpTime(new Date());
			rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_RESTRICT, JSONObject.toJSONString(record));
			
		}
		
		if("0".equals(chargeState)){
			ChargeRecord record = new ChargeRecord ();
			record.setDatecenterId(baseSnap.getDcId());
			record.setCusId(project.getCustomerId());
			record.setResourceId(baseSnap.getSnapId());
			record.setResourceType(ResourceType.DISKSNAPSHOT);
			record.setOpTime(new Date());
			rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_RECOVER, JSONObject.toJSONString(record));
			
		}

	}



/**
 * 从回收站恢复备份
 */
	@Override
	public boolean recoverSnapshot(String snapId,SessionUserInfo sessionUser) throws Exception {
		BaseCloudSnapshot baseSnapshot=snapDao.findOne(snapId);
		if((null==baseSnapshot)||!"2".equals(baseSnapshot.getIsDeleted())){
			throw new Exception("该备份已不在回收站中");
		}
		if(PayType.PAYAFTER.equals(baseSnapshot.getPayType())){
			//后付费资源 判断当前余额
			MoneyAccount accountMoney = accountOverviewSerivce.getAccountInfo(sessionUser.getCusId());
			BigDecimal createResourceLimitedMoney = new BigDecimal(0);
			if (accountMoney.getMoney().compareTo(createResourceLimitedMoney) < 0) {
				throw new Exception("账户余额不足，请充值后再恢复");
			}
		}
		
		baseSnapshot.setDeleteTime(null);
		baseSnapshot.setDeleteUser(null);
		baseSnapshot.setIsDeleted("0");
		
		if(PayType.PAYAFTER.equals(baseSnapshot.getPayType())){
			baseSnapshot.setChargeState("0");
			Customer customer = customerService.findCustomerById(sessionUser.getCusId());
			if(null != customer.getOverCreditTime()){
                boolean isBeyondRetentionTime = isBeyondRetentionTime(customer);
                if(isBeyondRetentionTime){
                    baseSnapshot.setChargeState("1");
                }else{
                    baseSnapshot.setChargeState("0");
                }
			}
		}
		
		snapDao.saveOrUpdate(baseSnapshot);
		
		if(PayType.PAYAFTER.equals(baseSnapshot.getPayType())){
			ChargeRecord record = new ChargeRecord ();
			record.setDatecenterId(baseSnapshot.getDcId());
			record.setCusId(sessionUser.getCusId());
			record.setResourceId(baseSnapshot.getSnapId());
			record.setResourceType(ResourceType.DISKSNAPSHOT);
			record.setOpTime(new Date());
			rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_RESTORE, JSONObject.toJSONString(record));	
		}
		return true;
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
 * 校验本次订单信息是否超配
 * ---------------------
 * @author chengxiaodong
 * @param orderSnapshot
 * 			备份订单信息
 * @return
 * 			返回的错误信息标示
 */
public String checkSnapshotQuota (CloudOrderSnapshot orderSnapshot){
	String errMsg = null;
	CloudProject project = queryProjectQuotaAndUsed(orderSnapshot.getPrjId());
	
	if(orderSnapshot.getOrderType().equals(OrderType.NEW)){
		if(1>(project.getDiskSnapshot() - project.getDiskSnapshotUse())){
			errMsg = "OUTOF_SNAPSHOTCOUNT_QUOTA" ;
			return errMsg;
		}
		
		if((orderSnapshot.getSnapSize())>(project.getSnapshotSize() - project.getUsedSnapshotCapacity())){
			errMsg = "OUTOF_SNAPSHOTCAPACITY_QUOTA" ;
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
@SuppressWarnings("rawtypes")
public CloudProject queryProjectQuotaAndUsed(String prjId){
	CloudProject project = new CloudProject();
	StringBuffer sql = new StringBuffer();

	sql.append("			SELECT                                                          	   		");
	sql.append("				cp.prj_id,                                                    	   		");
	sql.append("				cp.disk_snapshot,                                                	    ");
	sql.append("				cp.snapshot_size,                                             	   		");
	sql.append("				snap.usedSnapCount,                                          	   		");
	sql.append("				snap.usedSnapCapacity,                                       	   		");
	sql.append("				ordersnap.usedSnapCount1,                                     	   		");
	sql.append("				ordersnap.usedSnapCapacity1                                   	   		");
	sql.append("			FROM                                                            	   		");
	sql.append("				cloud_project cp                                              	   		");
	sql.append("			LEFT JOIN (                                                     	   		");
	sql.append("				SELECT                                                        	   		");
	sql.append("					csnap.prj_id,                                                	   	");
	sql.append("					count(1) AS usedSnapCount,                                	   	    ");
	sql.append("					sum(csnap.snap_size) AS usedSnapCapacity                    	   	");
	sql.append("				FROM                                                          	   		");
	sql.append("					cloud_disksnapshot csnap                                           	");
	sql.append("				WHERE                                                         	   		");
	sql.append("					csnap.is_visable = '1'                                       	   	");
	sql.append("				AND (csnap.is_deleted = '0' or csnap.is_deleted = '2')                  ");
	sql.append("				AND csnap.prj_id = ?                                           	   		");
	sql.append("			) snap ON snap.prj_id = cp.prj_id                                 	   		");
	sql.append("			LEFT JOIN (                                                     	   		");
	sql.append("				SELECT                                                        	   		");
	sql.append("					ordersnap.prj_id,                                            	  	");
	sql.append("					count(1) AS usedSnapCount1,                 		                ");
	sql.append("					sum(ordersnap.snap_size) AS usedSnapCapacity1             	   		");
	sql.append("				FROM                                                          	   		");
	sql.append("					cloudorder_snapshot ordersnap                                 	    ");
	sql.append("				LEFT JOIN order_info info ON info.order_no = ordersnap.order_no	   		");
	sql.append("				WHERE                                                         	   		");
	sql.append("					ordersnap.prj_id = ?                                      	   		");
	sql.append("				AND (                                                         	   		");
	sql.append("					info.order_state = '1'                                     	   		");
	sql.append("					OR info.order_state = '2'                                  	   		");
	sql.append("				)                                                             	   		");
	sql.append("			) ordersnap ON ordersnap.prj_id = cp.prj_id                       	   		");
	sql.append("			WHERE                                                           	   		");
	sql.append("				cp.prj_id = ?                                               	   		");

	javax.persistence.Query query = snapDao.createSQLNativeQuery(sql.toString(),new Object[] { prjId, prjId, prjId});
	List result = query.getResultList();
	if (result != null && result.size() == 1) {
		int index = 0;
		Object[] objs = (Object[]) result.get(0);

		project.setProjectId(String.valueOf(objs[index++]));
		project.setDiskSnapshot(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
		project.setSnapshotSize(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
		project.setDiskSnapshotUse(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
		project.setUsedSnapshotCapacity(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
		int orderSnapCount = Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0");
		int orderSnapCapacity = Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0");
		project.setDiskSnapshotUse(project.getDiskSnapshotUse() + orderSnapCount);
		project.setUsedSnapshotCapacity(project.getUsedSnapshotCapacity() + orderSnapCapacity);
		
	}

	return project;
}



/**
 * 根据参数查询备份
 * @param prjId 项目id
 * @param isDeleted  1放入回收站 0正常
 * @param payType  1预付费 2后付费
 * @param chargeState  0正常 1余额不足
 */
@Override
public List<CloudSnapshot> getSnapshotsBySome(String prjId, String isDeleted,
		String payType, String chargeState) throws Exception {
	List<CloudSnapshot> list=new ArrayList<CloudSnapshot>();
	StringBuffer hql = new StringBuffer();
	hql.append(" from  ");
	hql.append(" BaseCloudSnapshot v where 1=1 and v.isVisable = :isVisable");
	if(null!=isDeleted){
		hql.append(" and v.isDeleted = :isDeleted ");
	}
	if(null!=chargeState){
		hql.append(" and v.chargeState = :chargeState ");
	}
	if(null!=payType){
		hql.append(" and v.payType = :payType ");
	}
	if(null!=prjId){
		hql.append(" and v.prjId = :prjId ");
	}
	org.hibernate.Query query = snapDao.getHibernateSession().createQuery(hql.toString());
	query.setParameter("isVisable", "1");
	if(null!=isDeleted){
		query.setParameter("isDeleted", isDeleted);
	}
	if(null!=chargeState){
		query.setParameter("chargeState", chargeState);
	}
	if(null!=payType){
		query.setParameter("payType", payType);
	}
	if(null!=prjId){
		query.setParameter("prjId", prjId);
	}
	@SuppressWarnings("unchecked")
	List<BaseCloudSnapshot> queryList = query.list();
	if(queryList.size()>0){
		for(BaseCloudSnapshot baseSnapshot :queryList){
			CloudSnapshot snapshot=new CloudSnapshot();
			BeanUtils.copyPropertiesByModel(snapshot, baseSnapshot);	
			list.add(snapshot);
		}
	}
	return list;
	
}

/**
 * 如果创建失败的整单的全部删除
 * @param list
 */
public void snapOrderFail(BaseCloudSnapshot snap,CloudOrderSnapshot snapOrder){
	List<MessageOrderResourceNotice> messageList=new ArrayList<MessageOrderResourceNotice>();
	try{
		//调用创建资源创建失败的接口
		orderService.completeOrder(snapOrder.getOrderNo(), false,null);
		messageCenterService.addResourFailMessage(snapOrder.getOrderNo(), snapOrder.getCusId());
		//BaseCloudProject project = projectService.findProject(snap.getPrjId());
		BaseCloudSnapshot baseSnap=null;
		try{
			baseSnap=snapDao.findOne(snap.getSnapId());
		}catch(Exception e){
			log.error(e.toString(),e);
		}

		if(null!=baseSnap){
			volumeService.deleteBackUps(snap.getDcId(), snap.getPrjId(), snap.getSnapId());
			snapDao.delete(snap.getSnapId());
		}
		
	}catch(Exception e){
	    log.error(e.toString(),e);
		MessageOrderResourceNotice notice=new MessageOrderResourceNotice();
		notice.setOrderNo(snapOrder.getOrderNo());
		notice.setResourceId(snap.getSnapId());
		notice.setResourceName(snap.getSnapName());
		notice.setResourceType(ResourceType.getName(ResourceType.DISKSNAPSHOT));
		messageList.add(notice);
		messageCenterService.delecteResourFailMessage(messageList, snapOrder.getOrderNo());
	}
	
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
public void snapOrderSuccess (CloudOrderSnapshot orderSnap,BaseCloudSnapshot baseSnapshot) throws Exception{
	try{
		JSONObject json = new JSONObject();
		List<BaseOrderResource> resourceList = new ArrayList<BaseOrderResource>();
		List<String> snapIds = new ArrayList<String>();
		
		BaseOrderResource resource = new BaseOrderResource();
		resource.setOrderNo(orderSnap.getOrderNo());
		resource.setResourceId(baseSnapshot.getSnapId());
		resource.setResourceName(baseSnapshot.getSnapName());
		resourceList.add(resource);
		
		
		//更新订单
		BaseOrder order = orderService.completeOrder(orderSnap.getOrderNo(), true,resourceList);
		
		
		//将备份置为可见
		BaseCloudSnapshot snapshot=snapDao.findOne(baseSnapshot.getSnapId());
		snapshot.setIsVisable("1");
		snapDao.saveOrUpdate(snapshot);
			
		
		
		snapStartCharge(orderSnap, baseSnapshot);
		
		
		snapIds.add(snapshot.getSnapId());
		if (snapIds.size() > 0) {
			json.put("snapshot", snapIds);
		}
		
		//更新cloudorder_snapshot表中的resources
		orderSnapService.updateOrderResources(orderSnap.getOrderNo(),json.toJSONString());
		
	
	}catch(Exception e){
	    log.error(e.toString(),e);
		snapOrderFail(baseSnapshot,orderSnap);
	}
	
}


/**
 * 给计费模块发消息
 * 
 * @author chengxiaodong
 * @param record
 * 
 */
public void snapStartCharge (CloudOrderSnapshot orderSnapshot ,BaseCloudSnapshot snap){
	ChargeRecord record = new ChargeRecord ();
	ParamBean param = new ParamBean();
	
	param.setSnapshotSize(snap.getSnapSize());
	
	record.setParam(param);
	record.setDatecenterId(snap.getDcId());
	record.setOrderNumber(orderSnapshot.getOrderNo());
	record.setCusId(orderSnapshot.getCusId());
	record.setResourceId(snap.getSnapId());
	record.setResourceType(ResourceType.DISKSNAPSHOT);
	record.setChargeFrom(new Date());
	
	rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_PURCHASE, JSONObject.toJSONString(record));
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
		isSuccess =orderSnapService.updateOrderResources(orderNo, resourceJson);
	}catch(Exception e){
		isSuccess = false;
		log.error(e.toString(),e);
		throw e;
	}
	return isSuccess;
}



/**
 * 查询回收站的备份列表
 * ------------
 * @author chengxiaodong
 * @param page 分页结果集
 * @param map 查询条件
 * @param sessionUser 当前用户
 * @param queryMap 分页条件
 * @return 
 * @throws AppException
 */
@SuppressWarnings("unchecked")
public Page getRecycleSnapList(Page page, ParamsMap map,SessionUserInfo sessionUser, QueryMap queryMap) throws Exception {
	int index = 0;
	Object[] args = new Object[10];
	String name = "";
	String dcId= "";

	if (null != map && null != map.getParams()) {
		name = map.getParams().get("name") != null ? map.getParams().get("name") + "" : "";
		dcId = map.getParams().get("dcId") != null ? map.getParams().get("dcId") + "" : "";
	}
	
	StringBuffer sql=new StringBuffer();
	sql.append("select snap.snap_id as snapId,snap.snap_name as snapName,");
	sql.append(" snap.snap_size as snapSize,snap.snap_status as snapStatus,snap.snap_description as snapDescription,");
	sql.append(" snap.prj_id as prjId,snap.dc_id as dcId,snap.create_time as createTime,snap.delete_time as deleteTime,vol.vol_id as volId,vol.vol_name as volName,");
	sql.append(" prj.prj_name as prjName,dc.dc_name as dcName,snap.pay_type as payType,snap.charge_state as chargeState,");
	sql.append(" snap.snap_type as snapType,snap.is_deleted as isDeleted");
	sql.append(" from cloud_disksnapshot snap ");
	sql.append(" left join (select vol_id ,vol_name  from cloud_volume where is_deleted='0' or is_deleted='2') as vol on snap.vol_id = vol.vol_id ");
	sql.append(" left join dc_datacenter dc on snap.dc_id=dc.id");
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
	
	sql.append(" ) ON snap.prj_id=prj.prj_id");
	sql.append(" where snap.is_deleted = '2'");
	sql.append(" and snap.is_visable = '1'");
	sql.append(" and prj.customer_id = ?");
	args[index] = sessionUser.getCusId();
	index++;
	
	if (!"null".equals(dcId) && !StringUtils.isEmpty(dcId)) {
		sql.append(" and snap.dc_id = ? ");
		args[index] = dcId;
		index++;
	}

	if (!StringUtils.isEmpty(name)) {
		sql.append(" and binary snap.snap_name like ?");
		name = name.replaceAll("\\_", "\\\\_").replaceAll("\\%", "\\\\%");
		args[index] = "%" + name + "%";
		index++;
	} 
	sql.append(" group by snap.snap_id order by snap.delete_time desc");

	Object[] params = new Object[index];
	System.arraycopy(args, 0, params, 0, index);
	page = snapDao.pagedNativeQuery(sql.toString(), queryMap, params);
	@SuppressWarnings("rawtypes")
	List newList = (List) page.getResult();
	for (int i = 0; i < newList.size(); i++) {
		int ind =0;
		Object[] objs = (Object[]) newList.get(i);
		CloudSnapshot snapshot = new CloudSnapshot();
		snapshot.setSnapId(String.valueOf(objs[ind++]));
		snapshot.setSnapName(String.valueOf(objs[ind++]));
		snapshot.setSnapSize(Integer.parseInt(String.valueOf(objs[ind++])));
		snapshot.setSnapStatus(String.valueOf(objs[ind++]));
		snapshot.setSnapDescription(String.valueOf(objs[ind++]));
		snapshot.setPrjId(String.valueOf(objs[ind++]));
		snapshot.setDcId(String.valueOf(objs[ind++]));
		snapshot.setCreateTime((Date)objs[ind++]);
		snapshot.setDeleteTime((Date)objs[ind++]);
		snapshot.setVolId(String.valueOf(objs[ind++]));
		snapshot.setVolName(String.valueOf(objs[ind++]));
		snapshot.setPrjName(String.valueOf(objs[ind++]));
		snapshot.setDcName(String.valueOf(objs[ind++]));
		snapshot.setPayType(String.valueOf(objs[ind++]));
		snapshot.setChargeState(String.valueOf(objs[ind++]));
		snapshot.setSnapType(String.valueOf(objs[ind++]));
		snapshot.setIsDeleted(String.valueOf(objs[ind++]));
		newList.set(i, snapshot);
	}
	return page;
}


/**
 * 根据订单编号查询订单信息
 * @param orderNo
 * @return
 * @throws Exception 
 */
public CloudOrderSnapshot queryCloudOrderByOrderNo(String orderNo) throws Exception{
	return snapOrderService.getOrderByOrderNo(orderNo);
}



/**
 * 根据snapId查询指定备份信息
 */
@SuppressWarnings("rawtypes")
@Override
public CloudSnapshot getSnapshotById(String snapId) {
	int index=0;
	CloudSnapshot snapshot= null;
	StringBuffer sql=new StringBuffer();
	sql.append("select snap.snap_id as snapId,snap.snap_name as snapName,");
	sql.append(" snap.snap_size as snapSize,snap.snap_status as snapStatus,snap.snap_description as snapDescription,");
	sql.append(" snap.prj_id as prjId,snap.dc_id as dcId,snap.create_time as createTime,snap.vol_id as volId,");
	sql.append(" prj.prj_name as prjName,dc.dc_name as dcName,snap.pay_type as payType,snap.charge_state as chargeState,");
	sql.append(" snap.snap_type as snapType");
	sql.append(" from cloud_disksnapshot snap ");
	sql.append(" left join cloud_project prj on snap.prj_id=prj.prj_id");
	sql.append(" left join dc_datacenter dc on snap.dc_id=dc.id");
	sql.append(" where 1=1 and snap.is_visable='1' and snap.snap_id=?");
	
	javax.persistence.Query query = snapDao.createSQLNativeQuery(sql.toString(),new Object[] {snapId});
	List result = query.getResultList();
	if (result != null && result.size() == 1) {
		snapshot= new CloudSnapshot();
		 for(int i=0;i<result.size();i++){
		    	Object[] objs = (Object[])result.get(i);
		    	snapshot.setSnapId(String.valueOf(objs[index++]));
		    	snapshot.setSnapName(String.valueOf(objs[index++]));
		    	snapshot.setSnapSize(Integer.parseInt(String.valueOf(objs[index++])));
		    	snapshot.setSnapStatus(String.valueOf(objs[index++]));
		    	snapshot.setSnapDescription(String.valueOf(objs[index++]));
		    	snapshot.setPrjId(String.valueOf(objs[index++]));
		    	snapshot.setDcId(String.valueOf(objs[index++]));
		    	snapshot.setCreateTime((Date)objs[index++]);
		    	snapshot.setVolId(String.valueOf(objs[index++]));
		    	snapshot.setPrjName(String.valueOf(objs[index++]));
		    	snapshot.setDcName(String.valueOf(objs[index++]));
		    	snapshot.setPayType(String.valueOf(objs[index++]));
		    	snapshot.setChargeState(String.valueOf(objs[index++]));
		    	snapshot.setSnapType(String.valueOf(objs[index++]));
		    	snapshot.setStatusForDis(CloudResourceUtil.escapseChargeState(snapshot.getChargeState()));
		    	if (null==snapshot.getChargeState()||"".equals(snapshot.getChargeState())||"null".equals(snapshot.getChargeState())||CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(snapshot.getChargeState())) {
		    		snapshot.setStatusForDis(DictUtil.getStatusByNodeEn("snapshot",snapshot.getSnapStatus()));
				}
	        }
	    }
	
	return snapshot;
}




/**
 * 根据资源id查询指定资源
 */
public ResourceCheckBean isExistsByResourceId(String resourceId) {
	BaseCloudSnapshot baseSnap=snapDao.isExistsByResourceId(resourceId);
	ResourceCheckBean checkBean=new ResourceCheckBean();
	if(null!=baseSnap){
		checkBean.setResourceName(baseSnap.getSnapName());
		checkBean.setExisted(true);
	}else{
		checkBean.setExisted(false);
	}
	
	
	return checkBean;
}

/**
 * 查询已在回收站过期的云硬盘备份列表
 * 
 * @author zhouhaitao
 * @param seconds
 * @return
 */
@SuppressWarnings("rawtypes")
public List<CloudSnapshot> queryRecycleSnapshotList(long seconds){
	StringBuffer sql = new StringBuffer();
	List<CloudSnapshot> snapList = new ArrayList<CloudSnapshot>();
	sql.append("		SELECT                     ");
	sql.append("			snap.dc_id,            ");
	sql.append("			snap.prj_id,           ");
	sql.append("			snap.snap_id,          ");
	sql.append("			prj.customer_id        ");
	sql.append("		FROM                       ");
	sql.append("			cloud_disksnapshot snap");
	sql.append("		left join cloud_project prj");
	sql.append("		on prj.prj_id = snap.prj_id");
	sql.append("		WHERE                      ");
	sql.append("			TIMESTAMPDIFF(         ");
	sql.append("				SECOND,            ");
	sql.append("				snap.delete_time,  ");
	sql.append("				NOW()              ");
	sql.append("			) > ?                  ");
	sql.append("		AND snap.is_deleted = '2'  ");
	
	javax.persistence.Query query = snapDao.createSQLNativeQuery(sql.toString(), new Object[]{seconds});
	
	List list = query.getResultList();
	if(null != list &&  list.size()>0){
		for(int i =0 ;i<list.size();i++){
			int index = 0;
			Object [] objs = (Object []) list.get(i);
			CloudSnapshot snap = new CloudSnapshot();
			
			snap.setDcId(String.valueOf(objs[index++]));
			snap.setPrjId(String.valueOf(objs[index++]));
			snap.setSnapId(String.valueOf(objs[index++]));
			snap.setCusId(String.valueOf(objs[index++]));
			
			snapList.add(snap);
		}
	}
	return snapList;
}	
	
	

}
