package com.eayun.schedule.service.impl;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceSyncConstant;
import com.eayun.common.constant.SyncProgress.SyncByDatacenterTypes;
import com.eayun.common.constant.SyncProgress.SyncType;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.sync.SyncProgressUtil;
import com.eayun.common.util.CloudResourceUtil;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.BackUp;
import com.eayun.eayunstack.service.OpenstackVolumeService;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.schedule.service.CloudVolumeSnapService;
import com.eayun.virtualization.dao.CloudSnapshotDao;
import com.eayun.virtualization.dao.CloudVolumeDao;
import com.eayun.virtualization.model.BaseCloudSnapshot;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.CloudOrderSnapshot;
import com.eayun.virtualization.model.CloudSnapshot;
import com.eayun.virtualization.service.SnapshotService;

@Transactional
@Service
public class CloudVolumeSnapServiceImpl implements CloudVolumeSnapService{
    private static final Logger log = LoggerFactory.getLogger(CloudVolumeSnapServiceImpl.class);
    @Autowired
    private JedisUtil jedisUtil; 
	
	@Autowired
	private EcmcLogService ecmcLogService;
	@Autowired
	private OpenstackVolumeService volumeService;
	@Autowired
	private SnapshotService snapshotService;
	@Autowired
	private CloudSnapshotDao snapDao;
	@Autowired
	private CloudVolumeDao volDao;
	@Autowired
    private SyncProgressUtil syncProgressUtil;

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
	 * 重新放入队列
	 * @param groupKey
	 * @param value
	 * @return
	 */
	@Override
	public	boolean push(String groupKey,String value){
		boolean flag = false;
		try {
			flag=  jedisUtil.push(groupKey, value);
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			flag = false;
		}
		return flag;
	}
	
	/**
	 * 查询当前队列的长度
	 * @param groupKey
	 * @return
	 */
	public long size (String groupKey){
		return jedisUtil.sizeOfList(groupKey);
	}
	
	/**
	 * 获取底层指定ID的资源，底层异常为null
	 * ------------------
	 * @author zhouhaitao
	 * @param value
	 * 
	 */
	@Override
	public JSONObject get(JSONObject valueJson) throws Exception{
		JSONObject result = null ;
		try{
		if(null!=valueJson){
			BackUp json = volumeService.getBackUp(valueJson.getString("dcId"), valueJson.getString("prjId"), valueJson.getString("snapId"));
			if(null!=json){
				result =new JSONObject();
				result.put("id", json.getId());
				result.put("status", json.getStatus());
				result.put("volume_id", json.getVolume_id());
				result.put("size", json.getSize());
			}else{
				result =new JSONObject();
				result.put("deletingStatus", true+"");
			}
		}
		}catch(AppException e){
		    log.error(e.getMessage(),e);
			result =new JSONObject();
			result.put("deletingStatus", true+"");
		}
		return result;
	}
	
	/**
	 * 删除云硬盘备份
	 * @param cloudVm
	 * @return
	 */
	public boolean deleteVolSnap(CloudSnapshot cloudSnapshot){
		boolean flag = false ;
		try{
			BaseCloudSnapshot snapshot=snapDao.findOne(cloudSnapshot.getSnapId());
			if(null==snapshot){
				flag = true ;
			}else{
				snapDao.delete(cloudSnapshot.getSnapId());
				flag = true ;
			}
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			flag = false;
		}
		return flag ;
	}
	
	
	
	/**
	 * 修改云硬盘备份信息
	 * @param cloudVm
	 * @return
	 */
	public boolean  syncSnapshotInBuild(CloudSnapshot cloudSnapshot){
		boolean flag = false ;
		CloudOrderSnapshot orderSnap = new CloudOrderSnapshot();
		BaseCloudSnapshot snapshot=null;
		try{
			snapshot = snapDao.findOne(cloudSnapshot.getSnapId());
			snapshot.setSnapStatus(cloudSnapshot.getSnapStatus());
			snapDao.saveOrUpdate(snapshot);
			
			if("AVAILABLE".equals(cloudSnapshot.getSnapStatus())){
				flag = true ;
				orderSnap.setCreateUser(cloudSnapshot.getCreateName());
				orderSnap.setOrderNo(cloudSnapshot.getOrderNo());
				orderSnap.setPayType(cloudSnapshot.getPayType());
				orderSnap.setCusId(cloudSnapshot.getCusId());
				snapshotService.snapOrderSuccess(orderSnap, snapshot);
				
			}else if("ERROR".equals(cloudSnapshot.getSnapStatus())){
				orderSnap.setCreateUser(cloudSnapshot.getCreateName());
				orderSnap.setOrderNo(cloudSnapshot.getOrderNo());
				orderSnap.setPayType(cloudSnapshot.getPayType());
				orderSnap.setCusId(cloudSnapshot.getCusId());
				snapshotService.snapOrderFail(snapshot, orderSnap);
			}
			
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			orderSnap.setCreateUser(cloudSnapshot.getCreateName());
			orderSnap.setOrderNo(cloudSnapshot.getOrderNo());
			orderSnap.setPayType(cloudSnapshot.getPayType());
			orderSnap.setCusId(cloudSnapshot.getCusId());
			snapshotService.snapOrderFail(snapshot, orderSnap);
		}
		return flag ;
	}
	
	/**
	 * 修改云硬盘备份信息
	 * @param cloudVm
	 * @return
	 */
	public boolean  updateVolSnap(CloudSnapshot cloudSnapshot){
		boolean flag = false ;
		try{
			BaseCloudSnapshot snapshot = snapDao.findOne(cloudSnapshot.getSnapId());
			snapshot.setSnapStatus(cloudSnapshot.getSnapStatus());
			snapDao.saveOrUpdate(snapshot);
			flag = true ;
		}catch(Exception e){
		    log.error(e.getMessage(),e);
		    flag = false;
		}
		return flag ;
	}

	/**
	 * 同步底层备份
	 * @author chengxiaodong
	 * @throws Exception 
	 */
	public void synchData(BaseDcDataCenter dataCenter) throws Exception {
		try {
		    
			Map<String,BaseCloudSnapshot> voeMap=new HashMap<String,BaseCloudSnapshot>();
			Map<String,BackUp> map=new HashMap<String,BackUp>();
			List<BaseCloudSnapshot> voeList=queryDiskSnapshotsByDcId(dataCenter.getId());
			
			List<BackUp> list = volumeService.getAllBackUps(dataCenter.getId());
			
			
			if(null!=voeList&&voeList.size()>0){
				for(BaseCloudSnapshot voe:voeList){
					voeMap.put(voe.getSnapId(), voe);
				}
			}
			
			if(null!=list&&list.size()>0){
				for(BackUp c:list){
					map.put(c.getId(), c);
				}
			}
			long total = list == null ? 0L : list.size();
			syncProgressUtil.initResourceTotal(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.DISK_SNAPSHOT, total);
			if(null!=list&&list.size()>0){
				for(BackUp back:list){
					//底层数据存在于本地的数据库中 修改本地数据
					if(voeMap.containsKey(back.getId())){
						BaseCloudSnapshot baseSnap=new BaseCloudSnapshot();
						baseSnap.setSnapId(back.getId());
						baseSnap.setSnapSize(Integer.parseInt(back.getSize()));
						baseSnap.setVolId(back.getVolume_id());
						baseSnap.setSnapStatus(back.getStatus().toUpperCase());
						updateFromOpenstack(baseSnap);
					}
					//底层数据不存在于本地数据库 新增本地数据
					else{
						BaseCloudVolume vol=getVolumeBySnapshot(back.getVolume_id());
						BaseCloudSnapshot baseSnap=new BaseCloudSnapshot();
						baseSnap.setDcId(dataCenter.getId());
						if(null != vol){
							baseSnap.setPrjId(vol.getPrjId());
						}
						baseSnap.setSnapId(back.getId());
						baseSnap.setSnapSize(Integer.parseInt(back.getSize()));
						baseSnap.setVolId(back.getVolume_id());
						baseSnap.setSnapStatus(back.getStatus().toUpperCase());
						baseSnap.setSnapName(back.getName());
						baseSnap.setCreateTime(new Date());
						baseSnap.setIsDeleted("0");
						baseSnap.setIsVisable("0");
						snapDao.save(baseSnap);
					}
					syncProgressUtil.incrResourceDone(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.DISK_SNAPSHOT);
				}
			}
			//删除本地数据不存在于底层的数据
			if(null!=voeList&&voeList.size()>0){
				for(BaseCloudSnapshot voe:voeList){
					if(!map.containsKey(voe.getSnapId())){
						snapDao.delete(voe.getSnapId());
						
						//同步时删除本地底层不存在的资源，记录ecmc日志
						ecmcLogService.addLog("同步资源清除数据",  toType(voe), voe.getSnapName(), voe.getPrjId(),1,voe.getSnapId(),null);
						
						//将所删除的数据存入redis,以供发邮件使用
						JSONObject json = new JSONObject();
						json.put("resourceType", ResourceSyncConstant.SNAPSHOT);
						json.put("resourceId",voe.getSnapId());
						json.put("resourceName", voe.getSnapName());
						json.put("synTime", new Date());
						jedisUtil.push(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, json.toJSONString());
	
					}
				}
			}
			
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			throw e;
		} 
	}
	
	public BaseCloudVolume getVolumeBySnapshot(String volId) {
		BaseCloudVolume volume=volDao.findOne(volId);
		return volume;
	}

	@SuppressWarnings("unchecked")
	public List<BaseCloudSnapshot> queryDiskSnapshotListByDcId (String prjId){
		StringBuffer hql = new StringBuffer();
		hql.append(" from  BaseCloudSnapshot ");
		hql.append(" where prjId = ? ");
		
		return snapDao.find(hql.toString(), new Object[]{prjId});
	}
	
	@SuppressWarnings("unchecked")
	public List<BaseCloudSnapshot> queryDiskSnapshotsByDcId (String dcId){
		StringBuffer hql = new StringBuffer();
		hql.append(" from  BaseCloudSnapshot ");
		hql.append(" where dcId = ? ");
		
		return snapDao.find(hql.toString(), new Object[]{dcId});
	}
	
	public boolean updateFromOpenstack(BaseCloudSnapshot cloudDisksnapshot){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_disksnapshot set  ");
			sql.append("   snap_size = ?,       ");
			sql.append("   vol_id = ?,          ");
			sql.append("   snap_status = ?     ");
			sql.append(" where snap_id = ? ");
			
			snapDao.execSQL(sql.toString(), new Object[]{
					cloudDisksnapshot.getSnapSize(),
					cloudDisksnapshot.getVolId(),
					cloudDisksnapshot.getSnapStatus(),
					cloudDisksnapshot.getSnapId()
			});
			flag = true ;
		}catch(Exception e){
			flag = false;
			log.error(e.getMessage(),e);
			throw e;
		}
		
		return flag ;
	}
	
	
	private String toType(BaseCloudSnapshot snapshot){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StringBuffer resourceType = new StringBuffer();
		resourceType.append(ResourceSyncConstant.SNAPSHOT);
		resourceType.append("-").append(CloudResourceUtil.escapePayType(snapshot.getPayType())).append(ResourceSyncConstant.SEPARATOR);
		resourceType.append("创建时间：").append(sdf.format(snapshot.getCreateTime()));
		return resourceType.toString();
	}
	
}
