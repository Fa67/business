package com.eayun.virtualization.ecmcservice.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.charge.model.ChargeRecord;
import com.eayun.common.constant.EayunQueueConstant;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.CloudResourceUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.Hook;
import com.eayun.eayunstack.model.Restore;
import com.eayun.eayunstack.model.Snapshot;
import com.eayun.eayunstack.model.Volume;
import com.eayun.eayunstack.service.OpenstackSnapshotService;
import com.eayun.eayunstack.service.OpenstackVolumeService;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.virtualization.dao.CloudSnapshotDao;
import com.eayun.virtualization.dao.CloudVolumeDao;
import com.eayun.virtualization.ecmcservice.EcmcCloudSnapshotService;
import com.eayun.virtualization.model.BaseCloudSnapshot;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.model.CloudSnapshot;
import com.eayun.virtualization.model.CloudVolume;
import com.eayun.virtualization.service.TagService;

@Service
@Transactional
public class EcmcCloudSnapshotServiceImpl implements EcmcCloudSnapshotService{
    private static final Logger log = LoggerFactory.getLogger(EcmcCloudSnapshotServiceImpl.class);
	
	@Autowired
	private OpenstackSnapshotService snapshotService;
	@Autowired
	private OpenstackVolumeService volumeService;
	@Autowired
	private CloudSnapshotDao cloudSnapshotDao;
	@Autowired
	private CloudVolumeDao cloudVolumeDao;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private TagService tagService;
	@Autowired
	private EayunRabbitTemplate rabbitTemplate;

	@Override
	public int countSnapshotByPrjId(String prjId) {
		return cloudSnapshotDao.countSnapshotByPrjId(prjId);
	}
	
	
	public Page getSnapshotList(Page page, String prjId, String dcId,
			String queryName,String queryType, QueryMap queryMap, String isDeleted) throws Exception{
		List<Object> list = new ArrayList<Object>();
		StringBuffer sql=new StringBuffer();
		sql.append("select snap.snap_id as snapId,snap.snap_name as snapName,");
		sql.append(" snap.snap_size as snapSize,snap.snap_status as snapStatus,snap.snap_description as snapDescription,");
		sql.append(" snap.prj_id as prjId,snap.dc_id as dcId,snap.create_time as createTime,snap.vol_id as volId,vol.vol_name as volName,");
		sql.append(" count(volume.vol_id) as volNum,prj.prj_name as prjName,dc.dc_name as dcName,cus.cus_id as cusId,cus.cus_org as cusOrg");
		sql.append(" ,snap.pay_type as payType  ");
		sql.append(" ,snap.charge_state as chargeState  ");
		sql.append(" from cloud_disksnapshot snap ");
		sql.append(" left join (select vol_id,vol_name from cloud_volume  where is_deleted='0' and is_visable='1' ) vol on snap.vol_id = vol.vol_id");
		sql.append(" left join cloud_volume volume on snap.snap_id=volume.from_snapid and volume.is_deleted = '0'");
		sql.append(" left join cloud_project prj on snap.prj_id=prj.prj_id");
		sql.append(" left join dc_datacenter dc on snap.dc_id=dc.id");
		sql.append(" left join sys_selfcustomer cus ON prj.customer_id = cus.cus_id ");
		sql.append(" where 1=1 and snap.is_visable='1'");
		if (!"null".equals(isDeleted)&&null!=isDeleted&&!"".equals(isDeleted)&&!"undefined".equals(isDeleted)) {
			sql.append(" and snap.is_deleted=?");
			list.add(isDeleted);
		}
		if (!"null".equals(dcId)&&null!=dcId&&!"".equals(dcId)&&!"undefined".equals(dcId)) {
			sql.append(" and snap.dc_id=?");
			list.add(dcId);
		}
		if (!"null".equals(prjId)&&null!=prjId&&!"".equals(prjId)&&!"undefined".equals(prjId)) {
			sql.append(" and snap.prj_id=?");
			list.add(prjId);
		}
		
		if(null!=queryName&&!"".equals(queryName)){
			if (!"".equals(queryType) && null != queryType&& "name".equals(queryType)) {
				queryName = queryName.replaceAll("\\_", "\\\\_");
				sql.append(" and binary snap.snap_name like ?");
				list.add("%" + queryName + "%");
			}else if("cusOrg".equals(queryType)){
				//根据所属客户精确查询
				String[] cusOrgs = queryName.split(",");
				sql.append(" and ( ");
				for(String org:cusOrgs){
					sql.append(" binary cus.cus_org = ? or ");
					list.add(org);
				}
				sql.append(" 1 = 2 ) ");
				
			}else if("prjName".equals(queryType)){
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
		sql.append(" group by snap.snap_id order by snap.dc_id,snap.prj_id,snap.create_time desc");
        page=cloudSnapshotDao.pagedNativeQuery(sql.toString(),queryMap,list.toArray());
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
        	snapshot.setCreateTimeForDis(DateUtil
					.dateToString((Date)objs[7]));
        	snapshot.setVolId(String.valueOf(objs[8]));
        	snapshot.setVolName(String.valueOf(objs[9]));
        	snapshot.setVolNum(Integer.parseInt(String.valueOf(objs[10])));
        	snapshot.setPrjName(String.valueOf(objs[11]));
        	snapshot.setDcName(String.valueOf(objs[12]));
        	snapshot.setCusId(String.valueOf(objs[13]));
        	snapshot.setCusOrg(String.valueOf(objs[14]));
        	snapshot.setPayType(String.valueOf(objs[15]));
        	snapshot.setChargeState(String.valueOf(objs[16]));
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
	@Override
	public BaseCloudSnapshot addSnapshot(CloudSnapshot snap) throws AppException{
		BaseCloudSnapshot snapshot=null;
		try{
			JSONObject data=new JSONObject();
			JSONObject temp=new JSONObject();
			temp.put("name", snap.getSnapName());
			temp.put("description", snap.getSnapDescription());
			temp.put("volume_id", snap.getVolId());
			temp.put("force",true);
			data.put("snapshot", temp);
			Snapshot result=snapshotService.create(snap.getDcId(), snap.getPrjId(), data);
			if(null!=result){
				snapshot=new BaseCloudSnapshot();
				snapshot.setSnapId(result.getId());
				snapshot.setSnapName(result.getName());
				snapshot.setSnapSize(Integer.parseInt(result.getSize()));
				snapshot.setSnapStatus(result.getStatus().toUpperCase());
				snapshot.setCreateTime(new Date());
				snapshot.setSnapDescription(result.getDescription());
				snapshot.setDcId(snap.getDcId());
				snapshot.setPrjId(snap.getPrjId());
				snapshot.setVolId(snap.getVolId());
				cloudSnapshotDao.save(snapshot);
				
				if(null!=snapshot.getSnapStatus()&&!"AVAILABLE".equals(snapshot.getSnapStatus())){
					//TODO 同步新增云硬盘快照状态
					JSONObject json =new JSONObject();
					json.put("snapId", snapshot.getSnapId());
					json.put("dcId",snapshot.getDcId());
					json.put("prjId",snapshot.getPrjId());
					json.put("snapStatus",snapshot.getSnapStatus());
					json.put("count", "0");
					//jedisUtil.push(RedisKey.volSphKey, json.toJSONString());
					final JSONObject datas = json;
					TransactionHookUtil.registAfterCommitHook(new Hook() {
						@Override
						public void execute() {
							jedisUtil.addUnique(RedisKey.volSphKey, datas.toJSONString());
						}
					});
				}
			}
		}catch(AppException e){
			throw e;
		}catch(Exception e){
		    log.error(e.toString(),e);
			throw new AppException ("error.openstack.message");
		}
		
		return snapshot;
		
	}
	@Override
	public boolean deleteSnapshot(CloudSnapshot snap, BaseEcmcSysUser user) throws AppException {
		try{
			BaseCloudSnapshot snapshot=cloudSnapshotDao.findOne(snap.getSnapId());
			snap.setCusId(this.getCusIdBuyPrjId(snap.getPrjId()));
			if("2".equals(snap.getIsDeleted())){
				snapshot.setDeleteTime(new Date());
				snapshot.setDeleteUser(user.getAccount());
				snapshot.setIsDeleted("2");
				cloudSnapshotDao.saveOrUpdate(snapshot);
				
				//通知计费模块
				ChargeRecord record = new ChargeRecord ();
				record.setDatecenterId(snapshot.getDcId());
				record.setCusId(snap.getCusId());
				record.setResourceId(snapshot.getSnapId());
				record.setResourceType(ResourceType.DISKSNAPSHOT);
				record.setResourceName(snapshot.getSnapName());
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
						record.setCusId(snap.getCusId());
						record.setResourceId(snapshot.getSnapId());
						record.setResourceType(ResourceType.DISKSNAPSHOT);
						record.setResourceName(snapshot.getSnapName());
						record.setOpTime(new Date());
						rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_DELETE, JSONObject.toJSONString(record));
					}
					snapshot.setSnapStatus("DELETING");
					cloudSnapshotDao.saveOrUpdate(snapshot);
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
	@Override
	public BaseCloudVolume addVolumeBySnapshot(CloudVolume vol, String createName)
			throws AppException {
		JSONObject data=new JSONObject();
		JSONObject temp=new JSONObject();
		temp.put("name", vol.getVolName());
		temp.put("size", vol.getVolSize());
		temp.put("snapshot_id", vol.getFromSnapId());
		temp.put("description", vol.getVolDescription());
		data.put("volume", temp);
		BaseCloudVolume volume=new BaseCloudVolume();
		try {
			//创建云硬盘
			Volume result=volumeService.create(vol.getDcId(), vol.getPrjId(), data);
			//openstack平台创建成功后，保存到数据库
			if(result!=null){
				volume.setVolId(result.getId());
				volume.setVolName(result.getName());
				volume.setCreateTime(new Date());
				volume.setCreateName(createName);
				volume.setDcId(vol.getDcId());
				volume.setPrjId(vol.getPrjId());
				if(true==result.getBootable()){
					volume.setVolBootable("1");
				}else{
					volume.setVolBootable("0");
				}
				volume.setDiskFrom(vol.getDiskFrom());
				volume.setVolSize(Integer.parseInt(result.getSize()));
				volume.setVolStatus(result.getStatus().toUpperCase());
				volume.setVolDescription(result.getDescription());
				volume.setIsDeleted("0");
				volume.setFromSnapId(vol.getFromSnapId());
				cloudVolumeDao.save(volume);
				
				if(null!=volume.getVolStatus()&&!"AVAILABLE".equals(volume.getVolStatus())){
					//TODO 同步新增云硬盘状态
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
				}
			}
		}catch(AppException e){
			throw e;
		}catch(Exception e){
			log.error(e.toString(),e);
			throw new AppException ("error.openstack.message");
		}
		return volume;
	}
	
	
	
	@Override
	public boolean updateSnapshot(CloudSnapshot snap) throws AppException {
		try{
			BaseCloudSnapshot baseSnapshot = cloudSnapshotDao.findOne(snap.getSnapId());
			baseSnapshot.setSnapName(snap.getSnapName());
			baseSnapshot.setSnapDescription(snap.getSnapDescription());
			cloudSnapshotDao.saveOrUpdate(baseSnapshot);
			return true;
		}catch(AppException e){
			throw e;
		}
	}
	
	
	
	@Override
	public boolean getSnapByName(CloudSnapshot snap) throws AppException {
		boolean isExist = false;
		StringBuffer sql = new StringBuffer();
		int index=0;
		Object [] args=new Object[4];
		sql.append("select snap.snap_id,snap.snap_name from cloud_disksnapshot snap where 1=1 ");
		//数据中心
		if (!"".equals(snap.getDcId())&&snap.getDcId()!=null&&!"undefined".equals(snap.getDcId())&&!"null".equals(snap.getDcId())) {
			sql.append("and snap.dc_id = ? ");
			args[index]=snap.getDcId();
			index++;
		}
		//项目
		if (!"".equals(snap.getPrjId())&&snap.getPrjId()!=null&&!"undefined".equals(snap.getPrjId())&&!"null".equals(snap.getPrjId())) {
			sql.append("and snap.prj_id = ? ");
			args[index]=snap.getPrjId();
			index++;
		}
		//云硬盘备份名称
		if (!"".equals(snap.getSnapName())&&snap.getSnapName()!=null&&!"undefined".equals(snap.getSnapName())&&!"null".equals(snap.getSnapName())) {
			sql.append("and binary snap.snap_name = ? ");
			args[index]=snap.getSnapName().trim();
			index++;
		}
		//云硬盘备份ID
		if (!"".equals(snap.getSnapId())&&snap.getSnapId()!=null&&!"undefined".equals(snap.getSnapId())&&!"null".equals(snap.getSnapId())) {
			sql.append("and snap.snap_id <> ? ");
			args[index]=snap.getSnapId().trim();
			index++;
		}
		
		Object[] params = new Object[index];  
        System.arraycopy(args, 0, params, 0, index);
		javax.persistence.Query query = cloudSnapshotDao.createSQLNativeQuery(sql.toString(), params);
	    List listResult = query.getResultList();
	        
	    if(listResult.size()>0){
				isExist = true;//返回true 代表存在此名称
	    }
		return isExist;
	}
	
	
	@Override
	public List<CloudSnapshot> getSnapListByVolId(
			String volId) throws Exception {
		List<BaseCloudSnapshot> listSnap=cloudSnapshotDao.getSnapListByVolId(volId);
    	List<CloudSnapshot> result = new ArrayList<CloudSnapshot>();
		for (BaseCloudSnapshot baseCloudSnapshot : listSnap) {
			if("0".equals(baseCloudSnapshot.getIsDeleted())&&"1".equals(baseCloudSnapshot.getIsVisable())){
				CloudSnapshot cloudSnapshot= new CloudSnapshot();
				BeanUtils.copyPropertiesByModel(cloudSnapshot, baseCloudSnapshot);
				cloudSnapshot.setCreateTimeForDis(DateUtil.dateToString(cloudSnapshot.getCreateTime()));
				cloudSnapshot.setStatusForDis(CloudResourceUtil.escapseChargeState(cloudSnapshot.getChargeState()));
	        	if (null==cloudSnapshot.getChargeState() 
	        			|| "".equals(cloudSnapshot.getChargeState()) 
	        			|| "null".equals(cloudSnapshot.getChargeState()) 
	        			|| CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(cloudSnapshot.getChargeState())
	        			|| "DELETING".equals(cloudSnapshot.getSnapStatus())) {
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
			cloudSnapshotDao.delete(cloudSnapshot.getSnapId());
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
			BaseCloudSnapshot snapshot = cloudSnapshotDao.findOne(cloudSnapshot.getSnapId());
			snapshot.setSnapStatus(cloudSnapshot.getSnapStatus());
			cloudSnapshotDao.saveOrUpdate(snapshot);
			flag = true ;
		}catch(Exception e){
		    log.error(e.toString(),e);
			flag = false;
		}
		return flag ;
	}


	@Override
	public void rollBackVolume(CloudSnapshot snapshot) throws AppException{
		// TODO 云硬盘回滚操作
		try{
			JSONObject data = new JSONObject();
			JSONObject json = new JSONObject();
			json.put("backup_id", snapshot.getSnapId());
			json.put("volume_id", snapshot.getVolId());
			data.put("restore", json);
			
			Restore restore=volumeService.restoreVolume(snapshot.getDcId(), snapshot.getPrjId(), snapshot.getSnapId(), data);
			if(null!=restore){
				BaseCloudSnapshot baseCloudSnap=cloudSnapshotDao.getSnapshotById(snapshot.getSnapId());
				baseCloudSnap.setSnapStatus("RESTORING");	
				cloudSnapshotDao.saveOrUpdate(baseCloudSnap);
				
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
				
				BaseCloudVolume baseVolume=cloudVolumeDao.findOne(snapshot.getVolId());
				baseVolume.setVolStatus("RESTORING-BACKUP");
				cloudVolumeDao.saveOrUpdate(baseVolume);
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

	@Override
	public void deleteAllSnaps(String volId, String cusId, String isDeleted, BaseEcmcSysUser user) 
			throws AppException {
		List<BaseCloudSnapshot> list = cloudSnapshotDao.getUnDelSnapListByVolId(volId);
		
		for(BaseCloudSnapshot snapshot :list){
			if("2".equals(isDeleted)){
				snapshot.setIsDeleted("2");
				snapshot.setDeleteTime(new Date());
				snapshot.setDeleteUser(user.getAccount());
				cloudSnapshotDao.saveOrUpdate(snapshot);
				
				//给计费模块发消息
				ChargeRecord record = new ChargeRecord ();
				record.setDatecenterId(snapshot.getDcId());
				record.setCusId(cusId);
				record.setResourceId(snapshot.getSnapId());
				record.setResourceType(ResourceType.DISKSNAPSHOT);
				record.setResourceName(snapshot.getSnapName());
				record.setOpTime(new Date());
				rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_RECYCLE, JSONObject.toJSONString(record));
				
			}else{
				boolean flag=volumeService.deleteBackUps(snapshot.getDcId(), snapshot.getPrjId(), snapshot.getSnapId());
				if(flag){
					cloudSnapshotDao.delete(snapshot);
				}
				
				//给计费模块发消息
				if(!"2".equals(snapshot.getIsDeleted())){
					ChargeRecord record = new ChargeRecord ();
					record.setDatecenterId(snapshot.getDcId());
					record.setCusId(cusId);
					record.setResourceId(snapshot.getSnapId());
					record.setResourceType(ResourceType.DISKSNAPSHOT);
					record.setResourceName(snapshot.getSnapName());
					record.setOpTime(new Date());
					rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_DELETE, JSONObject.toJSONString(record));
		       }
			}
			
		}
	}


	@Override
	public Page getRecycleSnapList(Page page, ParamsMap map, BaseEcmcSysUser user, QueryMap queryMap) {
		List<Object> list = new ArrayList<Object>();
		String queryName = "";
		String queryType = "";
		String dcId = null == map.getParams().get("dcId")?"":map.getParams().get("dcId").toString();
		queryName = map.getParams().get("queryName") != null ? map.getParams().get("queryName") + "" : "";
		queryType = map.getParams().get("queryType") != null ? map.getParams().get("queryType") + "" : "";
		
		StringBuffer sql=new StringBuffer();
		sql.append("select snap.snap_id as snapId,snap.snap_name as snapName,");
		sql.append(" snap.snap_size as snapSize,snap.snap_status as snapStatus,snap.snap_description as snapDescription,");
		sql.append(" snap.prj_id as prjId,snap.dc_id as dcId,snap.create_time as createTime,snap.delete_time as deleteTime,vol.vol_id as volId,vol.vol_name as volName,");
		sql.append(" prj.prj_name as prjName,dc.dc_name as dcName,snap.pay_type as payType,snap.charge_state as chargeState,");
		sql.append(" snap.snap_type as snapType,cus.cus_id as cusId,cus.cus_org as cusOrg ");
		sql.append(" ,snap.is_deleted as isDeleted ");
		sql.append(" from cloud_disksnapshot snap ");
		sql.append(" left join (select vol_id ,vol_name  from cloud_volume where is_deleted='0' or is_deleted='2') as vol on snap.vol_id = vol.vol_id");
		sql.append(" left join cloud_project prj on snap.prj_id=prj.prj_id");
		sql.append(" left join dc_datacenter dc on snap.dc_id=dc.id");
		sql.append(" left join sys_selfcustomer cus ON prj.customer_id = cus.cus_id ");
		sql.append(" where 1=1 and snap.is_visable='1' and snap.is_deleted = '2'");

		if(!"null".equals(dcId) && !"".equals(dcId) && !"undefined".equals(dcId)){
			sql.append("and snap.dc_id = ? ");
			list.add(dcId);
		}
		if (null != queryName && !queryName.trim().equals("")) {
            if (queryType.equals("snapName")) {
            	queryName = queryName.replaceAll("\\_", "\\\\_");
                //根据云硬盘名称模糊查询
                sql.append(" and binary snap.snap_name like ? ");
                list.add("%" + queryName + "%");

            } else if (queryType.equals("cusOrg")) {
                //根据所属客户精确查询
                String[] cusOrgs = queryName.split(",");
                sql.append(" and ( ");
                for (String org : cusOrgs) {
                    sql.append(" binary cus.cus_org = ? or ");
                    list.add(org);
                }
                sql.append(" 1 = 2 ) ");

            } else if (queryType.equals("prjName")) {
                //根据项目名称精确查询
                String[] prjName = queryName.split(",");
                sql.append(" and ( ");
                for (String prj : prjName) {
                    sql.append(" binary prj.prj_name = ? or ");
                    list.add(prj);
                }
                sql.append(" 1 = 2 ) ");
            } else {
                sql.append(" and  1 = 2 ");
            }
        }
		sql.append(" group by snap.snap_id order by snap.delete_time desc");

		page = cloudSnapshotDao.pagedNativeQuery(sql.toString(), queryMap, list.toArray());
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
			snapshot.setCusId(String.valueOf(objs[ind++]));
			snapshot.setCusOrg(String.valueOf(objs[ind++]));
			snapshot.setIsDeleted(String.valueOf(objs[ind++]));
			newList.set(i, snapshot);
		}
		return page;
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
	private CloudProject queryProjectQuotaAndUsed(String prjId){
		CloudProject project = new CloudProject();
		StringBuffer sql = new StringBuffer();

		sql.append("			SELECT                                                          	   		");
		sql.append("				cp.prj_id,                                                    	   		");
		sql.append("				cp.disk_snapshot,                                                	   		");
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
		sql.append("					count(1) AS usedSnapCount,                                	   	");
		sql.append("					sum(csnap.snap_size) AS usedSnapCapacity                    	   	");
		sql.append("				FROM                                                          	   		");
		sql.append("					cloud_disksnapshot csnap                                           	   	");
		sql.append("				WHERE                                                         	   		");
		sql.append("					csnap.is_visable = '1'                                       	   	");
		sql.append("				AND (csnap.is_deleted = '0' or csnap.is_deleted = '2')                  ");
		sql.append("				AND csnap.prj_id = ?                                           	   		");
		sql.append("			) snap ON snap.prj_id = cp.prj_id                                 	   		");
		sql.append("			LEFT JOIN (                                                     	   		");
		sql.append("				SELECT                                                        	   		");
		sql.append("					ordersnap.prj_id,                                            	  	");
		sql.append("					count(1) AS usedSnapCount1,                 		");
		sql.append("					sum(ordersnap.snap_size) AS usedSnapCapacity1             	   		");
		sql.append("				FROM                                                          	   		");
		sql.append("					cloudorder_snapshot ordersnap                                 	   		");
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

		javax.persistence.Query query = cloudSnapshotDao.createSQLNativeQuery(sql.toString(),new Object[] { prjId, prjId, prjId});
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
	
	@Override
	public int getUsedSnapshotCount(String prjId){
		CloudProject projectBean = this.queryProjectQuotaAndUsed(prjId);
		return projectBean.getDiskSnapshotUse();
	}


	@Override
	public int getUsedSnapshotCapacity(String prjId) throws Exception {
		CloudProject projectBean = this.queryProjectQuotaAndUsed(prjId);
		return projectBean.getUsedSnapshotCapacity();
	}
	/**
	 * 根据项目ID获取客户ID
	 * 
	 * @param prjId
	 * @return
	 */
	private String getCusIdBuyPrjId(String prjId) {
		StringBuffer hql = new StringBuffer();
		hql.append("select ");
		hql.append("   customer_id ");
		hql.append("from ");
		hql.append("   cloud_project ");
		hql.append("where ");
		hql.append("   prj_id = ?");
		Query query = cloudSnapshotDao.createSQLNativeQuery(hql.toString(), prjId);
		Object result = query.getSingleResult();
		String cusId = result == null ? "" : String.valueOf(result.toString());
		return cusId;
	}
}
