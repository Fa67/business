package com.eayun.virtualization.ecmcservice.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.charge.model.ChargeRecord;
import com.eayun.common.constant.EayunQueueConstant;
import com.eayun.common.constant.PayType;
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
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.Hook;
import com.eayun.eayunstack.model.Volume;
import com.eayun.eayunstack.service.OpenstackVolumeService;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.virtualization.baseservice.BaseVolumeService;
import com.eayun.virtualization.dao.CloudVolumeDao;
import com.eayun.virtualization.ecmcservice.EcmcCloudSnapshotService;
import com.eayun.virtualization.ecmcservice.EcmcCloudVolumeService;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.CloudVolume;
import com.eayun.virtualization.service.TagService;

@Service
@Transactional
public class EcmcCloudVolumeServiceImpl extends BaseVolumeService implements EcmcCloudVolumeService {

	private static final Logger log = LoggerFactory
			.getLogger(EcmcCloudVolumeServiceImpl.class);

	@Autowired
	private OpenstackVolumeService volumeService;
	@Autowired
	private CloudVolumeDao cloudVolumeDao;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private TagService tagService;
	@Autowired
	private EayunRabbitTemplate rabbitTemplate;
	@Autowired
	private EcmcCloudSnapshotService snapService;

	/**
	 * 根据项目查询云硬盘使用量
	 */
	@Override
	public int getCountByPrjId(String prjId) {
		return cloudVolumeDao.getCountByPrjId(prjId);
	}

	/**
	 * 查询云硬盘列表
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
	public Page getVolumeList(Page page, String prjId, String dcId,
			String queryName, String queryType, QueryMap queryMap)
			throws Exception {
		List<Object> list = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("select vol.vol_id AS volId,vol.vol_name AS volName,vol.vol_status AS volStatus,vol.vol_size AS volSize,vol.disk_from AS diskFrom,vol.create_time AS createTime,vol.dc_id as dcId,vol.prj_id as prjId,prj.prj_name as prjName,vol.vm_id as vmId,vm.vm_name AS vmName,vol.vol_description as volDescription,vol.bind_point as bindPoint,count(snap.snap_id) as SnapNum,vm.vm_status as vmStatus,vol.vol_bootable as volBootable,dc.dc_name as dcName,cus.cus_id as cusId,cus.cus_org as cusOrg,vm.os_type as osType,vol.pay_type as payType,vol.end_time as endTime ");
		sql.append(" ,vol.charge_state as chargeState ");
		sql.append(" from cloud_volume vol ");
		sql.append(" left join cloud_vm vm ON vol.vm_id=vm.vm_id");
		sql.append(" left join dc_datacenter dc  ON vol.dc_id=dc.id");
		sql.append(" left join cloud_project prj ON vol.prj_id=prj.prj_id");
		sql.append(" left join cloud_disksnapshot snap ON vol.vol_id = snap.vol_id");
		sql.append(" left join sys_selfcustomer cus ON prj.customer_id = cus.cus_id ");
		sql.append(" where 1=1 and vol.is_deleted='0' and (vol.is_visable='1' or vol.is_visable is null) ");
		if (!"null".equals(prjId) && null != prjId && !"".equals(prjId)&& !"undefined".equals(prjId)) {
			sql.append(" and vol.prj_id=?");
			list.add(prjId);
		}
		if (!"null".equals(dcId) && null != dcId && !"".equals(dcId)&& !"undefined".equals(dcId)) {
			sql.append(" and vol.dc_id=?");
			list.add(dcId);
		}
		if (null != queryName && !"".equals(queryName)) {
			if (!"".equals(queryType) && null != queryType&& "name".equals(queryType)) {
				queryName = queryName.replaceAll("\\_", "\\\\_");
				sql.append(" and binary vol.vol_name like ?");
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
		sql.append(" group by vol.vol_id order by vol.dc_id,vol.prj_id,vol.create_time desc");
		page = cloudVolumeDao.pagedNativeQuery(sql.toString(), queryMap, list.toArray());
		List newList = (List) page.getResult();
		for (int i = 0; i < newList.size(); i++) {
			Object[] objs = (Object[]) newList.get(i);
			CloudVolume volume = new CloudVolume();
			volume.setVolId(String.valueOf(objs[0]));
			volume.setVolName(String.valueOf(objs[1]));
			volume.setVolStatus(String.valueOf(objs[2]));
			volume.setVolSize(Integer.parseInt(String.valueOf(objs[3])));
			volume.setDiskFrom(String.valueOf(objs[4]));
			volume.setCreateTime((Date) objs[5]);
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
			volume.setDcName(String.valueOf(objs[16]));
			volume.setCusId(String.valueOf(objs[17]));
			volume.setCusOrg(String.valueOf(objs[18]));
			String vmOsType=String.valueOf(objs[19]);
			volume.setPayType(String.valueOf(objs[20]));
			volume.setEndTime((Date)objs[21]);
			volume.setChargeState(String.valueOf(objs[22]));
			volume.setStatusForDis(CloudResourceUtil.escapseChargeState(volume.getChargeState()));
			if(null!=vmOsType&&"0007002002001".equals(vmOsType)){
        		volume.setBindPoint(null);
        	}
			if (null == volume.getChargeState() 
					|| "".equals(volume.getChargeState()) 
					|| "null".equals(volume.getChargeState()) 
					|| CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(volume.getChargeState())
					|| "DELETING".equals(volume.getVolStatus())) {
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
	 * 根据ID查询云硬盘详情
	 */
	@SuppressWarnings("rawtypes")
    @Override
	public CloudVolume getVolumeById(String volId)
			throws Exception {
		int index = 0;
		Object[] args = new Object[1];
		StringBuffer sql = new StringBuffer();
		sql.append("select vol.vol_id AS volId,vol.vol_name AS volName,vol.vol_status AS volStatus,vol.vol_size AS volSize,vol.vol_bootable AS volBootable,vol.disk_from AS diskFrom,vol.create_time AS createTime,vol.vm_id as vmId,vm.vm_name AS vmName,vol.vol_description AS volDescription,vol.dc_id as dcId,dc.dc_name as dcName,vol.prj_id as prjId,prj.prj_name as prjName,count(snap.snap_id) as SnapNum,tree.node_name as sysType,cus.cus_id as cusId,cus.cus_org as cusOrg ");
		sql.append(" ,vol.end_time as endTime,vol.bind_point as bindPoint,vol.pay_type as payType ");
		sql.append(" ,vol.charge_state as chargeState,vol.is_deleted as isDeleted ");
		sql.append(" ,vol.delete_time as deleteTime ");
		sql.append(" ,vm.os_type as osType ");
		sql.append(" ,vol.vol_typeid as volTypeId,type.volume_type as volType,type.max_size as maxSize");
		sql.append(" from cloud_volume vol");
		sql.append(" left join cloud_vm vm ON vol.vm_id = vm.vm_id");
		sql.append(" left join cloud_volumetype type ON vol.vol_typeid=type.type_id");
		sql.append(" left join cloud_disksnapshot snap ON vol.vol_id = snap.vol_id");
		sql.append(" left join cloud_project prj ON vol.prj_id=prj.prj_id");
		sql.append(" left join dc_datacenter dc ON vol.dc_id=dc.id");
		sql.append(" left join sys_data_tree tree ON vol.sys_type=tree.node_id");
		sql.append(" left join sys_selfcustomer cus ON prj.customer_id = cus.cus_id ");
		sql.append(" where 1=1 ");
		if (!"null".equals(volId) && null != volId && !"".equals(volId)
				&& !"undefined".equals(volId)) {
			sql.append(" and vol.vol_id=?");
			args[index] = volId;
			index++;
		}
		Object[] params = new Object[index];
		System.arraycopy(args, 0, params, 0, index);
		javax.persistence.Query query = cloudVolumeDao.createSQLNativeQuery(
				sql.toString(), params);
		List listResult = query.getResultList();
		CloudVolume volume = new CloudVolume();
		if(0 != query.getResultList().size() ){
			Object[] objs = (Object[]) listResult.get(0);
			if(objs[0] == null){
				return null;
			}
			volume.setVolId(String.valueOf(objs[0]));
			volume.setVolName(String.valueOf(objs[1]));
			volume.setVolStatus(String.valueOf(objs[2]));
			volume.setVolSize(Integer.parseInt(String.valueOf(objs[3])));
			volume.setVolBootable(String.valueOf(objs[4]));
			volume.setDiskFrom(String.valueOf(objs[5]));
			volume.setCreateTime((Date) objs[6]);
			volume.setVmId(String.valueOf(objs[7]));
			volume.setVmName(String.valueOf(objs[8]));
			volume.setVolDescription(String.valueOf(objs[9]));
			volume.setDcId(String.valueOf(objs[10]));
			volume.setDcName(String.valueOf(objs[11]));
			volume.setPrjId(String.valueOf(objs[12]));
			volume.setPrjName(String.valueOf(objs[13]));
			volume.setSnapNum(String.valueOf(objs[14]));
			volume.setSysType(String.valueOf(objs[15]));
			volume.setCusId(String.valueOf(objs[16]));
			volume.setCusOrg(String.valueOf(objs[17]));
			volume.setEndTime((Date)objs[18]);
			volume.setBindPoint(String.valueOf(objs[19]));
			volume.setPayType(String.valueOf(objs[20]));
			volume.setChargeState(String.valueOf(objs[21]));
			volume.setIsDeleted(String.valueOf(objs[22]));
			volume.setDeleteTime((Date)objs[23]);
			String vmOsType = String.valueOf(objs[24]);
			
			volume.setVolTypeId(String.valueOf(objs[25]));
        	volume.setVolType(String.valueOf(objs[26]));
        	volume.setMaxSize(Integer.parseInt(null!=objs[27]?String.valueOf(objs[27]):"2048"));
        	String volumeTypeAs=getVolumeTypeForDis(String.valueOf(objs[26]));
        	volume.setVolumeTypeAs(volumeTypeAs);
			
			volume.setStatusForDis(CloudResourceUtil.escapseChargeState(volume.getChargeState()));
			if(null!=vmOsType&&"0007002002001".equals(vmOsType)){
        		volume.setBindPoint(null);
        	}
			if (null == volume.getChargeState() || "".equals(volume.getChargeState()) 
					|| "null".equals(volume.getChargeState()) 
					|| "DELETING".equals(volume.getVolStatus())
					|| CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(volume.getChargeState())) {
				volume.setStatusForDis(DictUtil.getStatusByNodeEn("volume", volume.getVolStatus()));
			} 
		}else {
			return null;
		}
		return volume;
	}

	/**
	 * 验证云硬盘重名
	 */
	@SuppressWarnings("rawtypes")
    @Override
	public boolean getVolumeByName(Map<String, String> map) throws Exception {
		boolean isExist = false;
		try {
			String dcId = null;
			String prjId = null;
			String volName = null;
			String volId = null;
			//批量创建数量
			int volNumber = 1;

			prjId = map.get("prjId")!=null?map.get("prjId").toString():"";
			dcId = map.get("dcId")!=null?map.get("dcId").toString():"";
			volName = map.get("volName") != null ? map.get("volName")
					.toString() : "";
			volId = map.get("volId") != null ? map.get("volId").toString()
					: null;
			if(map.get("volNumber") != null && Integer.parseInt(map.get("volNumber")) > 0){
				volNumber = Integer.parseInt(map.get("volNumber"));
			}
			
			if (null == volName || "".equals(volName)) {
				return false;
			}
			StringBuffer sql = new StringBuffer();
			int index = 0;
			Object[] args = new Object[4];
			sql.append("select vol.vol_id,vol.vol_name from cloud_volume vol where (is_deleted='0' or is_deleted='2') ");
			// 数据中心
			if (!"".equals(dcId) && dcId != null && !"undefined".equals(dcId)
					&& !"null".equals(dcId)) {
				sql.append("and vol.dc_id = ? ");
				args[index] = dcId;
				index++;
			}
			// 项目
			if (!"".equals(prjId) && prjId != null
					&& !"undefined".equals(prjId) && !"null".equals(prjId)) {
				sql.append("and vol.prj_id = ? ");
				args[index] = prjId;
				index++;
			}
			// 云硬盘名称
			if (!"".equals(volName) && volName != null
					&& !"undefined".equals(volName) && !"null".equals(volName)) {
				sql.append("and binary vol.vol_name = ? ");
				args[index] = volName.trim();
				index++;
			}

			// 云硬盘ID
			if (!"".equals(volId) && volId != null
					&& !"undefined".equals(volId) && !"null".equals(volId)) {
				sql.append("and vol.vol_id <> ? ");
				args[index] = volId.trim();
				index++;
			}

			Object[] params = new Object[index];
			System.arraycopy(args, 0, params, 0, index);
			javax.persistence.Query query = cloudVolumeDao
					.createSQLNativeQuery(sql.toString(), params);
			List listResult = query.getResultList();

			if (listResult.size() > 0) {
				isExist = true;// 返回true 代表存在此名称
			} else {
				StringBuffer orderVolHql = new StringBuffer();
				orderVolHql.append("	SELECT                                                 ");
				orderVolHql.append("		cov.vol_number,                                    ");
				orderVolHql.append("		cov.vol_name                                       ");
				orderVolHql.append("	FROM                                                   ");
				orderVolHql.append("		cloudorder_volume cov                              ");
				orderVolHql.append("	LEFT JOIN order_info oi ON oi.order_no = cov.order_no  ");
				orderVolHql.append("	WHERE                                                  ");
				orderVolHql.append("		binary(cov.vol_name) = ?                           ");
				orderVolHql.append("	AND cov.order_type = '0'                               ");
				orderVolHql.append("	AND (oi.order_state = '1' or oi.order_state = '2')     ");
				orderVolHql.append("	AND cov.prj_id=?                                       ");
				javax.persistence.Query orderVolQuery = cloudVolumeDao.createSQLNativeQuery(orderVolHql.toString(),
						new Object[] {volName, prjId});
				List orderVolList = orderVolQuery.getResultList();
				for (int i = 0; i < orderVolList.size(); i++) {
					Object[] obj = (Object[]) orderVolList.get(i);
					int order_volNumber = Integer.parseInt(String.valueOf(obj[0]));
					String order_volName = String.valueOf(obj[1]);
					if (order_volNumber == 1 && volNumber == 1 && order_volName.equals(volName)) {
						isExist = true;
						break;
					}
					if (order_volNumber > 1 && volNumber > 1 && order_volName.equals(volName)) {
						isExist = true;
						break;
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return isExist;
	}

	// 创建云硬盘
	@Override
	public BaseCloudVolume createVolume(String dcId, String prjId,
			String createName, String from, String volName, int size,
			String description) throws AppException {
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("name", volName);
		temp.put("size", size);
		temp.put("imageRef", null);
		temp.put("description", description);
		data.put("volume", temp);

		try {
			// 创建云硬盘
			Volume result = volumeService.create(dcId, prjId, data);
			BaseCloudVolume volume = new BaseCloudVolume();

			// openstack平台创建成功后，保存到数据库
			if (result != null) {
				volume.setVolId(result.getId());
				volume.setVolName(result.getName());
				volume.setCreateTime(new Date());
				volume.setCreateName(createName);
				volume.setDcId(dcId);
				volume.setPrjId(prjId);
				if (true==result.getBootable()) {
					volume.setVolBootable("1");
				} else {
					volume.setVolBootable("0");
				}

				volume.setDiskFrom(from);
				volume.setVolSize(Integer.parseInt(result.getSize()));
				volume.setVolStatus(result.getStatus().toUpperCase());
				volume.setVolDescription(result.getDescription());
				volume.setIsDeleted("0");
				cloudVolumeDao.saveOrUpdate(volume);

				if (null != volume.getVolStatus()
						&& !"AVAILABLE".equals(volume.getVolStatus())) {
					// TODO 同步新增云硬盘状态
					JSONObject json = new JSONObject();
					json.put("volId", volume.getVolId());
					json.put("dcId", volume.getDcId());
					json.put("prjId", volume.getPrjId());
					json.put("volStatus", volume.getVolStatus());
					json.put("count", "0");
					//jedisUtil.push(RedisKey.volKey,json.toJSONString());
					final JSONObject datas = json;
					TransactionHookUtil.registAfterCommitHook(new Hook() {
						@Override
						public void execute() {
							jedisUtil.addUnique(RedisKey.volKey, datas.toJSONString());
						}
					});
				}
			}
			return volume;
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.toString(),e);
			throw new AppException("error.openstack.message");
		}
	}

	/**
	 * 查询当前项目下未挂载的数据盘列表
	 * 
	 * @Author: duanbinbin
	 * @param cloudVm
	 * @return <li>Date: 2016年4月25日</li>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<CloudVolume> getUnBindDisk(String prjId) {
		log.info("查询当前项目下未挂载的数据盘");
		List<CloudVolume> volList = new ArrayList<CloudVolume>();
		List<Object> list = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer();
		hql.append(" from BaseCloudVolume where prjId = ? and vmId is null and isDeleted = '0' and volBootable = '0' and chargeState = '0' and isVisable = '1' ");
		list.add(prjId);
		List<BaseCloudVolume> baseVolList = cloudVolumeDao.find(hql.toString(),
				list.toArray());
		for (int i = 0; i < baseVolList.size(); i++) {
			CloudVolume vol = new CloudVolume();
			BeanUtils.copyPropertiesByModel(vol, baseVolList.get(i));
			volList.add(vol);
		}
		return volList;
	}

	/**
	 * 删除云硬盘
	 */
	@Override
	public boolean deleteVolume(CloudVolume vol, BaseEcmcSysUser user) throws AppException{
		boolean isTrue=false;
		try{
			/**
        	 * 判断资源是否有未完成的订单 --@author zhouhaitao
        	 */
			if(checkVolOrderExsit(vol.getVolId())){
				throw new AppException("该资源有未完成的订单，请取消订单后再进行删除操作！");
			}
			
			String cusId = getCusIdBuyPrjId(vol.getPrjId());
			vol.setCusId(cusId);
			if("2".equals(vol.getIsDeleted())){
				BaseCloudVolume volume=cloudVolumeDao.findOne(vol.getVolId());
				volume.setDeleteUser(user.getAccount());
				volume.setDeleteTime(new Date());
				volume.setIsDeleted("2");
				cloudVolumeDao.saveOrUpdate(volume);
				

				//给计费模块发消息
				ChargeRecord record = new ChargeRecord ();
				record.setDatecenterId(volume.getDcId());
				record.setCusId(vol.getCusId());
				record.setResourceId(volume.getVolId());
				record.setResourceType(ResourceType.VDISK);
				record.setResourceName(volume.getVolName());
				record.setOpTime(new Date());
				rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_RECYCLE, JSONObject.toJSONString(record));
	
				if("2".equals(vol.getIsDeSnaps())){
					snapService.deleteAllSnaps(vol.getVolId(), vol.getCusId(), "2", user);
				}
				
				isTrue=true;
				
			}else{
				isTrue = volumeService.delete(vol.getDcId(), vol.getPrjId(), vol.getVolId());
				if(isTrue){
					BaseCloudVolume volume=cloudVolumeDao.findOne(vol.getVolId());
					volume.setDeleteUser(user.getAccount());
					volume.setVolStatus("DELETING");
					volume.setDeleteTime(new Date());
					cloudVolumeDao.saveOrUpdate(volume);
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
						record.setCusId(vol.getCusId());
						record.setResourceId(volume.getVolId());
						record.setResourceType(ResourceType.VDISK);
						record.setResourceName(volume.getVolName());
						record.setOpTime(new Date());
						rabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_DELETE, JSONObject.toJSONString(record));
					}
					
				}
				
				if(null!=vol.getIsDeSnaps()&&"1".equals(vol.getIsDeSnaps())){
					snapService.deleteAllSnaps(vol.getVolId(), vol.getCusId(), "1", user);
				}
	
			}
		
		}catch(AppException e){
			throw e;
		}catch(Exception e){
			log.error(e.toString(),e);
			throw new AppException ("error.openstack.message");
		}
		return isTrue;
	}

	
	/**
	 * 删除指定云主机下的所有云硬盘
	 *//*
	@Override
	public boolean deleteVolumeByVm(String vmId, String deleteName)
			throws AppException {
		List<CloudVolume> list = new ArrayList<CloudVolume>();
		List<String> errors=new ArrayList<String>();
		int count=0;
		boolean flag=false;
		try {
			list=this.queryVolumesByVm(vmId);
			for(CloudVolume volume :list){
				try{
					flag=this.deleteVolume(volume.getDcId(), volume.getPrjId(), volume.getVolId(), deleteName);
					if(flag){
						count++;
					}
				}catch(AppException e){
					errors.add(e.getArgsMessage()[0]);
				}
			}
			if(count==list.size()){
				return true;
			}else{
				String[] args=new String[errors.size()];
				for(int i=0;i<errors.size();i++){
					args[i]=errors.get(i);
				}
				throw new AppException("error.openstack.message",args);
			}
			
		} catch (Exception e) {
			log.error(e.toString(),e);
		}
		return false;
	}
	*/
	
	
	
	
	/**
	 * 更新云硬盘
	 */
	@Override
	public boolean updateVolume(CloudVolume volume)throws AppException {
		try{
			//拼装用于提交的数据
			JSONObject data=new JSONObject();
			JSONObject temp=new JSONObject();
			temp.put("name", volume.getVolName());
			temp.put("description", volume.getVolDescription());
			data.put("volume", temp);
			Volume result=volumeService.update(volume.getDcId(), volume.getPrjId(), data, volume.getVolId());
			
			if(null!=result){
				BaseCloudVolume vol=cloudVolumeDao.findOne(volume.getVolId());
				vol.setVolName(volume.getVolName());
				vol.setVolDescription(volume.getVolDescription());
				cloudVolumeDao.saveOrUpdate(vol);
				return true;
			}
		}catch(AppException e){
			throw e;
		}
		return false;
	}

	
	/**
	 * 挂载云硬盘
	 */
	@Override
	@Transactional(noRollbackFor=AppException.class)
	public boolean bindVolume(String dcId, String prjId, String vmId,
			String volId) throws AppException {
		boolean isTrue=false;
		try {
			isTrue=volumeService.bind(dcId, prjId, volId, vmId);
			if(isTrue){
				BaseCloudVolume volume=cloudVolumeDao.findOne(volId);
				volume.setVmId(vmId);
				volume.setVolStatus("ATTACHING");
				cloudVolumeDao.saveOrUpdate(volume);
				
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
	
	
	
	/**
	 * 解绑云硬盘
	 */
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public boolean debindVolume(String dcId, String prjId, String vmId,
			String volId) throws AppException {
		boolean isTrue=false;
		try {
			isTrue=volumeService.debind(dcId, prjId, volId, vmId);
			if(isTrue){
				BaseCloudVolume volume=cloudVolumeDao.findOne(volId);
				volume.setVmId(null);
				volume.setBindPoint(null);
				volume.setVolStatus("DETACHING");
				cloudVolumeDao.saveOrUpdate(volume);
				
				//TODO 解绑云硬盘的自动任务
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
	
	
	
	/**
	 * 查询指定云主机下挂载的云硬盘
	 */
	@Override
	public List<CloudVolume> queryVolumesByVm(String vmId) throws Exception {
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
    	sql.append(" where vol.vm_id = ? and vol.is_deleted='0' ");
    	javax.persistence.Query query = cloudVolumeDao.createSQLNativeQuery(sql.toString(), new Object []{vmId});
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

	
	/**
	 *查询指定云主机下挂载的云硬盘数量
	 */
	@Override
	public int getCountByVnId(String vmId) throws Exception {
		 int vnCount = cloudVolumeDao.getVmCount(vmId);
	     return vnCount;
	}

	//将云主机系统盘数据插入数据库
	@Override
	public void insertVolumeDB(BaseCloudVolume cloudVolume) {
		cloudVolumeDao.saveOrUpdate(cloudVolume);
	}

	//删除云主机时删除系统盘
	@Override
	public void deleteVolumeByVm(String vmId, String deleteUser) {
		StringBuffer querySql = new StringBuffer();
		querySql.append("from BaseCloudVolume where vmId = ? and volBootable ='1'");
		@SuppressWarnings("unchecked")
		List<BaseCloudVolume> volumeList = cloudVolumeDao.find(querySql.toString(), new Object[]{vmId});
		for(BaseCloudVolume vol:volumeList){
			vol.setVolStatus("DELETING");
			vol.setDeleteTime(new Date());
			vol.setDeleteUser(deleteUser);
			cloudVolumeDao.merge(vol);
			
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
	public boolean updateVol(CloudVolume cloudVolume) {
		boolean flag = false ;
		try{
			BaseCloudVolume volume = cloudVolumeDao.findOne(cloudVolume.getVolId());
			volume.setVolStatus(cloudVolume.getVolStatus());
			if(!StringUtils.isEmpty(cloudVolume.getVolBootable())){
				volume.setVolBootable(cloudVolume.getVolBootable());
			}
			if(!StringUtils.isEmpty(cloudVolume.getBindPoint())){
				volume.setBindPoint(cloudVolume.getBindPoint());
			}
			cloudVolumeDao.saveOrUpdate(volume);
			flag = true ;
		}catch(Exception e){
		    log.error(e.toString(),e);
			flag = false;
		}
		return flag ;
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
			org.hibernate.Query query = cloudVolumeDao.getHibernateSession().createQuery(hql.toString());
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
					debindVolume(volume.getDcId(),volume.getPrjId(),volume.getVmId(),volume.getVolId());
					count++;
				}
			}
			
			if(count==queryList.size()){
				return true;
			}else{
				return false;
			}
			
		}

	@SuppressWarnings("unchecked")
    @Override
	public Page getRecycleVolList(Page page, ParamsMap map, BaseEcmcSysUser user, QueryMap queryMap) {
		List<Object> list = new ArrayList<Object>();
		String queryName = "";
		String queryType = "";
		String dcId = null == map.getParams().get("dcId")?"":map.getParams().get("dcId").toString();
		if (null != map.getParams()) {
			queryName = map.getParams().get("queryName") != null ? map.getParams().get("queryName") + "" : "";
			queryType = map.getParams().get("queryType") != null ? map.getParams().get("queryType") + "" : "";
		}

		StringBuffer sql = new StringBuffer();
		sql.append("select vol.vol_id AS volId,vol.vol_name AS volName,vol.vol_status AS volStatus,vol.vol_size AS volSize,vol.disk_from AS diskFrom,");
		sql.append(" vol.create_time AS createTime,vol.delete_time as deleteTime,vol.dc_id as dcId,vol.prj_id as prjId,prj.prj_name as prjName,");
		sql.append(" vol.vm_id as vmId,vm.vm_name AS vmName,vol.vol_description as volDescription,vol.bind_point as bindPoint,vm.vm_status as vmStatus,");
		sql.append(" vol.vol_bootable as volBootable,vol.pay_type as payType,vol.end_time as endTime,vol.charge_state as chargeState,dc.dc_name as dcName ");
		sql.append(" ,cus.cus_org as cusOrg,cus.cus_id as cusId ");
		sql.append(" ,vol.is_deleted as isDeleted ");
		sql.append(" from cloud_volume vol");
		sql.append(" left join cloud_vm vm ON vol.vm_id=vm.vm_id");
		sql.append(" left join cloud_project prj ON vol.prj_id=prj.prj_id");
		sql.append(" left join dc_datacenter dc ON vol.dc_id=dc.id");
		sql.append(" left join sys_selfcustomer cus ON prj.customer_id = cus.cus_id ");
		sql.append(" where  vol.is_visable='1' and vol.is_deleted = '2'");
		
		if(!"null".equals(dcId) && !"".equals(dcId) && !"undefined".equals(dcId)){
			sql.append("and vol.dc_id = ? ");
			list.add(dcId);
		}
		if (null != queryName && !queryName.trim().equals("")) {
            if (queryType.equals("volName")) {
            	queryName = queryName.replaceAll("\\_", "\\\\_");
                //根据云硬盘名称模糊查询
                sql.append(" and binary vol.vol_name like ? ");
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
		sql.append(" group by vol.vol_id order by vol.delete_time desc");

		page = cloudVolumeDao.pagedNativeQuery(sql.toString(), queryMap, list.toArray());
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
			vol.setCusOrg(String.valueOf(objs[ind++]));
			vol.setCusId(String.valueOf(objs[ind++]));
			vol.setIsDeleted(String.valueOf(objs[ind++]));
			newList.set(i, vol);
		}
		return page;
	}
	
	@SuppressWarnings("rawtypes")
    @Override
	public int getUsedVolumeCountByPrjId(String prjId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT  ");
		sql.append("     vol.usedVolumeCount AS usedVolumeCount1, ");
		sql.append("     ordervol.usedVolumeCount AS usedVolumeCount2, ");
		sql.append("     ordervm.usedHostCount ");
		sql.append(" FROM ");
		sql.append("     cloud_project cp ");
		sql.append("         LEFT JOIN ");
		sql.append("     (SELECT  ");
		sql.append("         cvol.prj_id, COUNT(1) AS usedVolumeCount ");
		sql.append("     FROM ");
		sql.append("         cloud_volume cvol ");
		sql.append("     WHERE ");
		sql.append("         cvol.is_visable = '1' ");
		sql.append("             AND (cvol.is_deleted = '0' ");
		sql.append("             OR cvol.is_deleted = '2') ");
		sql.append("             AND cvol.prj_id = ?) vol ON cp.prj_id = vol.prj_id ");
		sql.append("         LEFT JOIN ");
		sql.append("     (SELECT  ");
		sql.append("         ordervol.prj_id, SUM(ordervol.vol_number) AS usedVolumeCount ");
		sql.append("     FROM ");
		sql.append(" ( SELECT                                            					 ");
		sql.append("   clov.prj_id,                                       					 ");
		sql.append("   clov.order_type,                                   					 ");
		sql.append("   clov.order_no,                                     					 ");
		sql.append("   CASE clov.order_type                               					 ");
		sql.append("   WHEN 2 THEN 0                                     					 ");
		sql.append("   ELSE clov.vol_number                           						 ");
		sql.append("   END AS vol_number                            						 ");
		sql.append("       FROM cloudorder_volume clov                          	         ");
		sql.append(" 	   LEFT JOIN cloud_volume cv ON cv.vol_id = clov.vol_id 			 ");
		sql.append(" ) ordervol ");
		sql.append("     LEFT JOIN order_info info ON info.order_no = ordervol.order_no ");
		sql.append("     WHERE ");
		sql.append("         ordervol.prj_id = ? ");
		sql.append("             AND (info.order_state = '1' ");
		sql.append("             OR info.order_state = '2') ");
		sql.append("             AND (ordervol.order_type = '0' ");
		sql.append("             OR ordervol.order_type = '2')) ordervol ON ordervol.prj_id = cp.prj_id ");
		sql.append("         LEFT JOIN ");
		sql.append("     (SELECT  ");
		sql.append("         ordervm.prj_id, SUM(ordervm.count) AS usedHostCount ");
		sql.append("     FROM ");
		sql.append("         (SELECT  ");
		sql.append("         cov.prj_id, ");
		sql.append("             cov.order_no, ");
		sql.append("             cov.order_type, ");
		sql.append("             CASE cov.order_type ");
		sql.append("                 WHEN '2' THEN 0 ");
		sql.append("                 ELSE cov.count ");
		sql.append("             END AS count ");
		sql.append("     FROM ");
		sql.append("         cloudorder_vm cov ");
		sql.append("     LEFT JOIN cloud_vm vm ON cov.vm_id = vm.vm_id ");
		sql.append("     LEFT JOIN cloud_flavor cf ON vm.flavor_id = cf.flavor_id) ordervm ");
		sql.append("     LEFT JOIN order_info info ON info.order_no = ordervm.order_no ");
		sql.append("     WHERE ");
		sql.append("         ordervm.prj_id = ? ");
		sql.append("             AND (info.order_state = '1' ");
		sql.append("             OR info.order_state = '2') ");
		sql.append("             AND (ordervm.order_type = '0' ");
		sql.append("             OR ordervm.order_type = '2')) ordervm ON ordervm.prj_id = cp.prj_id ");
		sql.append(" WHERE ");
		sql.append("     cp.prj_id = ? ");
		javax.persistence.Query query = cloudVolumeDao.createSQLNativeQuery(sql.toString(),new Object[] { prjId, prjId, prjId, prjId});
		List result = query.getResultList();
		int usedVolumeCount = 0;
		if (result != null && result.size() == 1) {
			Object[] objs = (Object[]) result.get(0);
			for(Object obj : objs){
				usedVolumeCount += Integer.parseInt(obj != null ? String.valueOf(obj) : "0");
			}
		}
		return usedVolumeCount;
	}

	@SuppressWarnings("rawtypes")
    @Override
	public int getUsedVolumeCapacityByPrjId(String prjId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT  ");
		sql.append("     vol.usedVolumeCapacity as usedVolumeCapacity1, ");
		sql.append("     ordervol.usedVolumeCapacity as usedVolumeCapacity2, ");
		sql.append("     ordervm.usedDisk ");
		sql.append(" FROM ");
		sql.append("     cloud_project cp ");
		sql.append("         LEFT JOIN ");
		sql.append("     (SELECT  ");
		sql.append("         cvol.prj_id, sum(cvol.vol_size) AS usedVolumeCapacity ");
		sql.append("     FROM ");
		sql.append("         cloud_volume cvol ");
		sql.append("     WHERE ");
		sql.append("         cvol.is_visable = '1' ");
		sql.append("             AND (cvol.is_deleted = '0' ");
		sql.append("             OR cvol.is_deleted = '2') ");
		sql.append("             AND cvol.prj_id = ?) vol ON cp.prj_id = vol.prj_id ");
		sql.append("         LEFT JOIN (");
		sql.append(" SELECT                                                        	   		     ");
		sql.append(" 	ordervol.prj_id,                                            	  	     ");
		sql.append(" 	sum(ordervol.vol_size) AS usedVolumeCapacity                             ");
		sql.append(" FROM  (                                                        	   		 ");
		sql.append(" SELECT                                               	                     ");
		sql.append(" 	clov.prj_id,                                       	                     ");
		sql.append(" 	clov.order_type,                                   	                     ");
		sql.append(" 	clov.order_no,                                                           ");
		sql.append(" 	CASE clov.order_type                                                     ");
		sql.append(" 	WHEN 2 THEN clov.vol_size - cv.vol_size            	                     ");
		sql.append(" 	ELSE clov.vol_number * clov.vol_size               	                     ");
		sql.append(" 	END AS vol_size                                    	                     ");
		sql.append(" FROM cloudorder_volume clov                          	                     ");
		sql.append(" LEFT JOIN cloud_volume cv ON cv.vol_id = clov.vol_id 	                     ");
		sql.append(" 	  ) as ordervol                                                		     ");
		sql.append(" LEFT JOIN order_info info ON info.order_no = ordervol.order_no	   		     ");
		sql.append(" WHERE                                                         	   		     ");
		sql.append(" 	ordervol.prj_id = ?                                     	   		     ");
		sql.append(" AND (                                                         	   		     ");
		sql.append(" 	info.order_state = '1'                                     	   		     ");
		sql.append(" 	OR info.order_state = '2'                                  	   		     ");
		sql.append(" )                                                             	   		     ");
		sql.append(" AND (                                                         	   		     ");
		sql.append(" 	ordervol.order_type = '0'                                        	     ");
		sql.append(" 	OR ordervol.order_type = '2'                                     	     ");
		sql.append(" )                                                             	   		     ");
		sql.append("                                   ) ordervol ON ordervol.prj_id = cp.prj_id ");
		sql.append("         LEFT JOIN (                                                         ");
		sql.append(" SELECT                                                        	   		     ");
		sql.append(" 		ordervm.prj_id,                                                      ");
		sql.append(" 		sum(ordervm.disk) AS usedDisk                  	   	                 ");
		sql.append(" FROM(                                                      	   		     ");
		sql.append(" SELECT                                                                      ");
		sql.append(" 	cov.prj_id,                                                              ");
		sql.append(" 	cov.order_no,                                                            ");
		sql.append(" 	cov.order_type,                                                          ");
		sql.append(" 	CASE cov.order_type                                                      ");
		sql.append(" WHEN '2' THEN                                                               ");
		sql.append(" 	0                                                                        ");
		sql.append(" ELSE                                                                        ");
		sql.append(" 	cov.count                                                                ");
		sql.append(" END AS count,                                                               ");
		sql.append("  CASE cov.order_type                                                        ");
		sql.append(" WHEN '2' THEN                                                               ");
		sql.append(" 	0                                                                        ");
		sql.append(" ELSE                                                                        ");
		sql.append(" 	cov.count*cov.disk                                                       ");
		sql.append(" END AS disk                                                                 ");
		sql.append(" FROM                                                                        ");
		sql.append(" 	cloudorder_vm cov                                                        ");
		sql.append(" LEFT JOIN cloud_vm vm ON cov.vm_id = vm.vm_id                               ");
		sql.append(" LEFT JOIN cloud_flavor cf ON vm.flavor_id = cf.flavor_id                    ");
		sql.append(" 		) ordervm                                     	   		             ");
		sql.append(" 	LEFT JOIN order_info info ON info.order_no = ordervm.order_no 	   		 ");
		sql.append(" 	WHERE                                                       	   		 ");
		sql.append(" 		ordervm.prj_id = ?                                      	   		 ");
		sql.append(" 	AND (                                                         	   		 ");
		sql.append(" 		info.order_state = '1'                                        		 ");
		sql.append(" 		OR info.order_state = '2'                                   		 ");
		sql.append(" 	)                                                             	   		 ");
		sql.append(" 	AND (                                                         	   		 ");
		sql.append(" 		ordervm.order_type = '0'                                       		 ");
		sql.append(" 		OR ordervm.order_type = '2'                                   		 ");
		sql.append(" 	)                    	   		 ");
		sql.append(" ) ordervm ON ordervm.prj_id = cp.prj_id ");
		sql.append(" WHERE ");
		sql.append("     cp.prj_id = ? ");
		javax.persistence.Query query = cloudVolumeDao.createSQLNativeQuery(sql.toString(),new Object[] { prjId, prjId, prjId, prjId});
		List result = query.getResultList();
		int usedVolumeCount = 0;
		if (result != null && result.size() == 1) {
			Object[] objs = (Object[]) result.get(0);
			for(Object obj : objs){
				usedVolumeCount += Integer.parseInt(obj != null ? String.valueOf(obj) : "0");
			}
		}
		return usedVolumeCount;
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
		Query query = cloudVolumeDao.createSQLNativeQuery(hql.toString(), prjId);
		Object result = query.getSingleResult();
		String cusId = result == null ? "" : String.valueOf(result.toString());
		return cusId;
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
				after=volumeType;
			}
		}
		
		return after;

	}
}
