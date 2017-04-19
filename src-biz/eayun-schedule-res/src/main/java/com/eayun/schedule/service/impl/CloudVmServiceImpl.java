package com.eayun.schedule.service.impl;


import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.PayType;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceSyncConstant;
import com.eayun.common.constant.SyncProgress.SyncByProjectTypes;
import com.eayun.common.constant.SyncProgress.SyncType;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.sync.SyncProgressUtil;
import com.eayun.common.util.CloudResourceUtil;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.Hook;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.Image;
import com.eayun.eayunstack.model.InterfaceAttachment;
import com.eayun.eayunstack.model.Vm;
import com.eayun.eayunstack.service.OpenstackVmService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.schedule.service.CloudVmService;
import com.eayun.virtualization.dao.CloudVmDao;
import com.eayun.virtualization.dao.CloudVmSecurityGroupDao;
import com.eayun.virtualization.dao.CloudVolumeDao;
import com.eayun.virtualization.model.BaseCloudNetwork;
import com.eayun.virtualization.model.BaseCloudSecurityGroup;
import com.eayun.virtualization.model.BaseCloudVm;
import com.eayun.virtualization.model.BaseCloudVmSgroup;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.CloudFloatIp;
import com.eayun.virtualization.model.CloudOrderVm;
import com.eayun.virtualization.model.CloudVm;
import com.eayun.virtualization.service.CloudFloatIpService;
import com.eayun.virtualization.service.SecretkeyVmService;
import com.eayun.virtualization.service.VmService;
import com.eayun.virtualization.service.VolumeService;

@Service
@Transactional
public class CloudVmServiceImpl implements CloudVmService{
    private static final Logger log = LoggerFactory.getLogger(CloudVmServiceImpl.class);
    @Autowired
    private JedisUtil jedisUtil; 
	
	@Autowired
	private OpenstackVmService openStackVmService;
	@Autowired
	private CloudVmDao cloudVmDao;
	@Autowired
	private CloudVmSecurityGroupDao cloudVmSgDao ;
	@Autowired
	private CloudVolumeDao cloudVolumeDao;
	@Autowired
	private VmService vmService;
	@Autowired
	private CloudFloatIpService floatIpService;
	@Autowired
	private EcmcLogService ecmcLogService;
	@Autowired
	private VolumeService volumeService;
	@Autowired
	private SecretkeyVmService secretkeyVmService;
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
		if(null!=valueJson){
			JSONObject json = openStackVmService.get(valueJson.getString("dcId"), 
					valueJson.getString("prjId"), valueJson.getString("vmId"));
			if(null!=json){
				boolean isDeleted=json.containsKey("itemNotFound");
				if(!isDeleted){
					result=json.getJSONObject(OpenstackUriConstant.VM_DATA_NAME);
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
	 * 删除云主机
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @param isSoftDelete
	 * 				是否是软删除
	 * @return
	 */
	
	public boolean deleteVm(CloudVm vm,boolean isSoftDelete){
		boolean flag = false;
		try { 
			updateVm(vm);
			if(!isSoftDelete){
				String delSecGroupSql = " delete from cloud_vmsecuritygroup where vm_id = ? ";
				
				cloudVmDao.execSQL(delSecGroupSql,new Object[] { vm.getVmId() });
				
				volumeService.deleteDataVolumeByVm(vm.getVmId());
				
				secretkeyVmService.deleteByVm(vm.getVmId());
			}
			else{
				StringBuffer sql = new StringBuffer();
				sql.append(" update cloud_volume set is_deleted =  ? ");
				sql.append(" where vm_id = ? ");
				sql.append(" and vol_bootable = '1' ");
				cloudVmDao.execSQL(sql.toString(), new Object[]{"2",vm.getVmId()});
			}
			flag = true;
			return flag;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 删除云主机
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @param isSoftDelete
	 * 				是否是软删除
	 * @return
	 */
	
	public boolean resumeVm(CloudVm vm){
		boolean flag = false;
		StringBuffer sql = new StringBuffer();
		try { 
			sql.append(" update  cloud_vm set ");
			sql.append(" is_deleted = '0'  , ");
			sql.append(" vm_status = 'ACTIVE'  , ");
			sql.append(" delete_user = ?  , ");
			sql.append(" delete_time = ?  ");
			sql.append(" where vm_id = ?  ");
			
			cloudVmDao.execSQL(sql.toString(), new Object[]{
					null,
					null,
					vm.getVmId()
			});
			
			sql = new StringBuffer();
			sql.append(" update cloud_volume set is_deleted =  ? ");
			sql.append(" , delete_user =  ? ");
			sql.append(" , delete_time =  ? ");
			sql.append(" where vm_id = ? ");
			
			cloudVmDao.execSQL(sql.toString(), new Object[]{"0",null,null,vm.getVmId()});
			
			if(PayType.PAYAFTER.equals(vm.getPayType())){
				vm.setOpDate(new Date());
				vmService.vmOptionCharge(vm, "restore");
			}
			
			flag = true;
			return flag;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 修改云主机信息
	 * @param cloudVm
	 * @return
	 */
	
	public boolean  updateVm(BaseCloudVm cloudVm){
		boolean flag = false;
		int index = 0;
		Object[] args = new Object[10];
		try {
			StringBuffer sql = new StringBuffer ();
			sql.append(" update  cloud_vm set ");
			if(!StringUtils.isEmpty(cloudVm.getVmIp())){
				sql.append(" vm_ip = ?  , ");
				args[index]=cloudVm.getVmIp();
				index++;
			}
			if(!StringUtils.isEmpty(cloudVm.getSelfPortId())){
				sql.append(" self_portid = ?  , ");
				args[index]=cloudVm.getSelfPortId();
				index++;
			}
			if(!StringUtils.isEmpty(cloudVm.getSelfIp())){
				sql.append(" self_ip = ?  , ");
				args[index]=cloudVm.getSelfIp();
				index++;
			}
			if(!StringUtils.isEmpty(cloudVm.getHostId())){
				sql.append(" host_id = ?  , ");
				args[index]=cloudVm.getHostId();
				index++;
			}
			if(!StringUtils.isEmpty(cloudVm.getHostName())){
				sql.append(" host_name = ?  , ");
				args[index]=cloudVm.getHostName();
				index++;
			}
			if(!StringUtils.isEmpty(cloudVm.getIsDeleted())){
				sql.append(" is_deleted = ?  , ");
				args[index]=cloudVm.getIsDeleted();
				index++;
			}
			sql.append(" vm_status=? ");
			args[index]=cloudVm.getVmStatus();
			index++;
			
			sql.append(" where vm_id = ?  ");
			args[index]=cloudVm.getVmId();
			index++;
			
			Object[] params = new Object[index];
			System.arraycopy(args, 0, params, 0, index);
			cloudVmDao.execSQL(sql.toString(), params);
			flag = true;
	
			return flag;
		} catch (Exception e) {
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	
	public void synchData(BaseDcDataCenter dataCenter, String prjId) throws Exception{
		Map<String,Object> map = getStackList(dataCenter, prjId);
		List<BaseCloudVm> list =(List<BaseCloudVm>) map.get("VmList");
		List<BaseCloudVolume> volList =(List<BaseCloudVolume>) map.get("VolList");
		Map<String,List<String>> secList =(Map<String, List<String>>) map.get("SecMap");
		Map<String,BaseCloudVm> dbMap=new HashMap<String,BaseCloudVm>();             
		Map<String,BaseCloudVm> stackMap=new HashMap<String,BaseCloudVm>();      
		List<BaseCloudVm> dbList=queryCloudVmListByPrjId(prjId);
		                                                                         
		if(null!=dbList){                                                        
			for(BaseCloudVm vm:dbList){                                           
				dbMap.put(vm.getVmId(), vm);                                      
			}                                                                      
		}             
		long total = list == null ? 0L : list.size();
		syncProgressUtil.initResourceTotal(dataCenter.getId(), SyncType.PROJECT, SyncByProjectTypes.VM, total);                                                                         
		if(null!=list){                                                          
			for(BaseCloudVm vm:list){                                     
				//底层数据存在本地数据库中 更新本地数据                              
				if(dbMap.containsKey(vm.getVmId())){                       
					updateCloudVmFromStack(vm);            
				}                                                                    
				else{ 
					vm.setIsDeleted("0");
					vm.setIsVisable("0");
					cloudVmDao.save(vm);                                  
				}                                                                    
				stackMap.put(vm.getVmId(), vm);
				syncProgressUtil.incrResourceDone(dataCenter.getId(), SyncType.PROJECT, SyncByProjectTypes.VM);
			}                                                                      
		}                                                                        
		                                                                         
		if(null!=dbList){                                                        
			for(BaseCloudVm vm:dbList){                                           
				//删除本地数据库中不存在于底层的数据                                 
				if(!stackMap.containsKey(vm.getVmId())&&"0".equals(vm.getIsDeleted())){   
					vm.setIsDeleted("1");
					updateDeletedStatus(vm);
					secretkeyVmService.deleteByVm(vm.getVmId());
					
					ecmcLogService.addLog("同步资源清除数据",  toType(vm), vm.getVmName(), vm.getPrjId(),1,vm.getVmId(),null);
					
					JSONObject json = new JSONObject();
					json.put("resourceType", ResourceSyncConstant.VM);
					json.put("resourceId", vm.getVmId());
					json.put("resourceName", vm.getVmName());
					json.put("synTime", new Date());
					jedisUtil.push(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, json.toJSONString());
				}
			}                                                                      
		}  
		
		//处理云主机挂在的云硬盘
		if(null!=volList){
			for(BaseCloudVolume cv:volList){
				cv.setIsDeleted("0");
				updateVmIdById(cv);
			}
		}
		
		//维护云主机与安全组的多对多的关系
		handleRealateOnSec(secList);
		
		//处理软删除的云主机
		handleSoftDeletedVm(dataCenter.getId(),prjId,dbList);
	}
	
	/**
	 * 软删除的同步
	 * @param dcId
	 * @param prjId
	 * @param dbList
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	
	private void handleSoftDeletedVm(String dcId, String prjId,List<BaseCloudVm> dbList) throws Exception{
		Map<String,Object> map = getSoftDeletedVmList(dcId, prjId);
		List<BaseCloudVm> list =(List<BaseCloudVm>) map.get("VmList");
		List<BaseCloudVolume> volList =(List<BaseCloudVolume>) map.get("VolList");
		Map<String,List<String>> secList =(Map<String, List<String>>) map.get("SecMap");
		Map<String,BaseCloudVm> dbMap=new HashMap<String,BaseCloudVm>();             
		Map<String,BaseCloudVm> stackMap=new HashMap<String,BaseCloudVm>(); 
		
		
		if(null!=dbList){                                                        
			for(BaseCloudVm vm:dbList){                                           
				dbMap.put(vm.getVmId(), vm);                                      
			}                                                                      
		}                                                                        
		                                                                         
		if(null!=list){                                                          
			for(BaseCloudVm vm:list){                                     
				//底层数据存在本地数据库中 更新本地数据                               
				if(dbMap.containsKey(vm.getVmId())){                       
					BaseCloudVm bcvm = dbMap.get(vm.getVmId());
					bcvm.setIsDeleted("2");
					bcvm.setVmStatus("SOFT_DELETED");
					updateSoftDeletedStatus(bcvm);
				}                                                                    
				else{ 
					vm.setIsDeleted("2");
					vm.setIsVisable("0");
					vm.setVmStatus("SOFT_DELETED");
					cloudVmDao.save(vm);                                  
				}                                                                    
				stackMap.put(vm.getVmId(), vm);                   
			}                                                                      
		}
		
		if(null!=dbList){                                                        
			for(BaseCloudVm vm:dbList){   
				if(!stackMap.containsKey(vm.getVmId()) && "2".equals(vm.getIsDeleted())){
					vm.setIsDeleted("1");
					vm.setVmStatus("DELETED");
					updateSoftDeletedStatus(vm);
				}
			}
		}
		
		if(null!=volList){
			for(BaseCloudVolume cv:volList){
				cv.setIsDeleted("2");
				updateVmIdById(cv);
			}
		}
		
		handleRealateOnSec(secList);
	}
	
	
	public Map<String,Object> getStackList(BaseDcDataCenter dataCenter ,String prjId) throws Exception{
		Map<String,Object> map=new HashMap<String,Object>();
		List <BaseCloudVm> list = new ArrayList<BaseCloudVm>();                                         
		List<BaseCloudVolume> volList= new ArrayList<BaseCloudVolume>();
		Map<String,List<String>>  secMap =new HashMap<String,List<String>>();
		
		List<JSONObject> result = openStackVmService.getStackList(dataCenter, prjId);
		
		if (result != null && result.size() > 0) {                                                              
			for (JSONObject jsonObject : result) {   
					List<String> secList= new ArrayList<String>();
					String imageStr=jsonObject.getString("image");
					if("".equals(imageStr)){
						jsonObject.put("image", new Image());
					}
					Vm data = openStackVmService.json2bean(jsonObject,                                             
							 Vm.class);                                                                            
					initData(data, jsonObject);
					BaseCloudVm ccn=new BaseCloudVm(data,dataCenter.getId());                                 
//					initDataVm(ccn,jsonObject);
					initList(volList,secList,jsonObject);
					secMap.put(ccn.getVmId(), secList);
					list.add(ccn);                                                                                    
				}                                                                                                   
			}           
		
		map.put("VmList", list);
		map.put("VolList", volList);
		map.put("SecMap", secMap);
		
		return map;           
	}
	
	
	public Map<String,Object> getSoftDeletedVmList(String dcId ,String prjId) throws Exception{
		Map<String,Object> map=new HashMap<String,Object>();
		List <BaseCloudVm> list = new ArrayList<BaseCloudVm>();                                         
		List<BaseCloudVolume> volList= new ArrayList<BaseCloudVolume>();
		Map<String,List<String>>  secMap =new HashMap<String,List<String>>();
		
		List<JSONObject> result = openStackVmService.getSoftDeletedList(dcId, prjId, "?deleted=True&status=SOFT_DELETED");
		
		if (result != null && result.size() > 0) {                                                              
			for (JSONObject jsonObject : result) {   
					List<String> secList= new ArrayList<String>();
					String imageStr=jsonObject.getString("image");
					if("".equals(imageStr)){
						jsonObject.put("image", new Image());
					}
					Vm data = openStackVmService.json2bean(jsonObject,                                             
							 Vm.class);                                                                            
					initData(data, jsonObject);
					BaseCloudVm ccn=new BaseCloudVm(data,dcId);                                 
//					initDataVm(ccn,jsonObject);
					initList(volList,secList,jsonObject);
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
		vm.setHypervisor_hostname(object
				.getString("OS-EXT-SRV-ATTR:hypervisor_hostname"));
		vm.setInstance_name(object.getString("OS-EXT-SRV-ATTR:instance_name"));
		vm.setPower_state(object.getString("OS-EXT-STS:power_state"));
		vm.setVm_state(object.getString("OS-EXT-STS:vm_state"));
		vm.setLaunched_at(object.getString("OS-SRV-USG:launched_at"));
		JSONArray jsonList = (JSONArray) JSONArray.parse(object
				.getString("os-extended-volumes:volumes_attached"));
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
	 * @author zhouhaitao
	 * @param vm
	 * @param cvm
	 */
	@SuppressWarnings("unused")
    private void initDataVm(BaseCloudVm vm,JSONObject json){
		JSONObject addresses=json.getJSONObject("addresses");
		Set<String> netSet=addresses.keySet();
		String netId="";
		String vmIp="";
		if(null!=netSet&&netSet.size()==1){
			for(String netName:netSet){
				netId = queryNetWorkByName(netName,vm.getPrjId());
				JSONArray address=addresses.getJSONArray(netName);
				if(null!=address&&address.size()>0){
					JSONObject add=address.getJSONObject(0);
					vmIp=add.getString("addr");
				}
			}
		}
		vm.setNetId(netId);
		vm.setVmIp(vmIp);
	}
	
	private void initList(List<BaseCloudVolume> volList,List<String> escList,JSONObject json){
		JSONArray volArrays=json.getJSONArray("os-extended-volumes:volumes_attached");
		JSONArray secArrays=json.getJSONArray("security_groups");
		if (volArrays!=null&&volArrays.size()>0) {
			for (Object data:volArrays) {
				BaseCloudVolume volume=new BaseCloudVolume();
				volume.setVmId(json.getString("id"));
				volume.setVolId(((JSONObject)data).getString("id"));
				volList.add(volume);
			}
		}
		
		if(null!=secArrays&&secArrays.size()>0){
			for(Object data:secArrays){
				String secName=((JSONObject)data).getString("name");
				String prjId=json.getString("tenant_id");
				if(!StringUtils.isEmpty(secName)){
					String id=querySecIdByName(secName,prjId);
					if(!StringUtils.isEmpty(id)){
						escList.add(id);
					}
				}
			}
		}
	}
	
	private String queryNetWorkByName(String netName ,String dcId){
		String netId = "" ;
		StringBuffer hql  =new StringBuffer();
		hql.append(" from BaseCloudNetwork where binary(netName) = ? and prjId = ?");
		
		BaseCloudNetwork network = (BaseCloudNetwork)cloudVmDao.findUnique(hql.toString(), new Object []{netName,dcId});
		
		if(null!=network){
			netId = network.getNetId();
		}
		return netId;
	}
	private String querySecIdByName(String secName ,String prjId){
		String sgId = "" ;
		StringBuffer hql  =new StringBuffer();
		hql.append(" from BaseCloudSecurityGroup where binary(sgName) = ? and prjId = ?");
		
		@SuppressWarnings({"unchecked" })
		List<BaseCloudSecurityGroup> list = cloudVmDao.find(hql.toString(), new Object []{secName,prjId});
		
		if(null!=list&&list.size()>0){
			sgId = list.get(0).getSgId();
		}
		return sgId;
	}
	@SuppressWarnings("unchecked")
	
	public List<BaseCloudVm>  queryCloudVmListByPrjId(String prjId){
		StringBuffer hql = new StringBuffer();
		hql.append(" from BaseCloudVm  ");
		hql.append(" where prjId = ? ");
		
		return cloudVmDao.find(hql.toString(), new Object[]{prjId});
	}
	
	
	public boolean updateCloudVmFromStack(BaseCloudVm vm){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_vm set ");
			sql.append("	dc_id = ?,           ");
			sql.append("	prj_id = ?,          ");
			sql.append("	vm_name = ?,         ");
			sql.append("	vm_status = ?,       ");
			sql.append("	flavor_id = ?,       ");
//			sql.append("	net_id = ?,          ");
//			sql.append("	vm_ip = ?,           ");
			sql.append("	host_id = ?,         ");
			sql.append("	host_name = ?,       ");
			sql.append("	is_deleted = ?       ");
			sql.append(" where vm_id = ? ");
			
			cloudVmDao.execSQL(sql.toString(), new Object[]{
					vm.getDcId(),
					vm.getPrjId(),
					vm.getVmName(),
					vm.getVmStatus(),
					vm.getFlavorId(),
//					vm.getNetId(),
//					vm.getVmIp(),
					vm.getHostId(),
					vm.getHostName(),
					"0",
					vm.getVmId()
			});
			flag = true ;
		}catch(Exception e){
			flag = false;
			log.error(e.getMessage(),e);
			throw e;
		}
		
		return flag ;
	}
	
	
	public boolean updateDeletedStatus(BaseCloudVm vm){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_vm set ");
			sql.append("	is_deleted = ?,          ");
			sql.append("	delete_time = ?          ");
			sql.append(" where vm_id = ? ");
			
			cloudVmDao.execSQL(sql.toString(), new Object[]{
					vm.getIsDeleted(),
					new Date(),
					vm.getVmId()
			});
			flag = true ;
		}catch(Exception e){
			flag = false;
			log.error(e.getMessage(),e);
			throw e;
		}
		
		return flag ;
	}
	
	public boolean updateSoftDeletedStatus(BaseCloudVm vm){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_vm set ");
			sql.append("	is_deleted = ?,        ");
			sql.append("	vm_status = ?          ");
			sql.append(" where vm_id = ? ");
			
			cloudVmDao.execSQL(sql.toString(), new Object[]{
					vm.getIsDeleted(),
					vm.getVmStatus(),
					vm.getVmId()
			});
			flag = true ;
		}catch(Exception e){
			flag = false;
			log.error(e.getMessage(),e);
			throw e;
		}
		
		return flag ;
	}
	
	public boolean updateVmIdById(BaseCloudVolume cv){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_volume set  ");
			sql.append("	vm_id =?,           ");
			sql.append("	is_deleted =?           ");
			sql.append(" where vol_id = ? ");
			sql.append(" and vol_bootable = '1' ");
			
			cloudVmDao.execSQL(sql.toString(), new Object[]{
					cv.getVmId(),cv.getIsDeleted(), cv.getVolId()
			});
			flag = true ;
		}catch(Exception e){
			flag = false;
			log.error(e.getMessage(),e);
			throw e;
		}
		
		return flag ;
	}
	
	public boolean deleteByVmId(String vmId){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" delete from cloud_vmsecuritygroup   ");
			sql.append(" where vm_id = ? ");
			
			cloudVmDao.execSQL(sql.toString(), new Object[]{vmId});
			flag = true ;
		}catch(Exception e){
			flag = false;
			log.error(e.getMessage(),e);
			throw e;
		}
		return flag ;
	}
	
	/**
	 * 处理云主机与安全组的多对多的关系
	 * 		1.删除云主机下的与所有的安全组的关联关系
	 * 		2.添加云主机的安全组的关联关系
	 * -----------------------
	 * @param map
	 */
	
	public void handleRealateOnSec(Map<String,List<String>> map){
		Set<String> set=map.keySet();
		for(String vmId:set){
			List<String> secList=map.get(vmId);
			//删除云主机下的云主机与安全组的关联关系
			deleteByVmId(vmId);
			for(String sgId:secList){
				BaseCloudVmSgroup vsg=new BaseCloudVmSgroup();
				vsg.setSgId(sgId);
				vsg.setVmId(vmId);
				cloudVmSgDao.save(vsg);
			}
		}
	}
	
	
	public void addVoluneDB(BaseCloudVolume volume){
		cloudVolumeDao.saveOrUpdate(volume);
	}
	
	
	/**
	 * 确认调整
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * @throws Exception 
	 */
	@Transactional(noRollbackFor=AppException.class)
	public void resized(CloudVm cloudVm) throws Exception{
		try{
			openStackVmService.confirmResizeVm(cloudVm);
			
			BaseCloudVm vm = new BaseCloudVm();
			
			vm = cloudVmDao.findOne(cloudVm.getVmId());
			
			vm.setVmStatus("RESIZED");
			vm.setFlavorId(vm.getResizeId());
			vm.setResizeId(null);
			
			cloudVmDao.merge(vm);
		}catch(Exception e){
			vmService.upgradeFailHandler(cloudVm);
			log.error(e.getMessage(),e);
			throw e;
		}
	}
	
	/**
	 * 云主机升级成功
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * @throws Exception 
	 */
	
	public void resize(CloudVm cloudVm) throws Exception{
		updateVm(cloudVm);
		
		vmService.upgradeSuccessHandler(cloudVm);
	}
	
	/**
	 * 同步创建中的云主机
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws Exception 
	 */
	@Transactional(noRollbackFor={AppException.class,FileNotFoundException.class,Exception.class})
	public boolean syncVmInBuild(CloudVm cloudVm) throws Exception{
		boolean isAllSuccess = false;
		boolean isError = false;
		List<BaseCloudVm> cloudVmList = null;
		try{
			String url ="?flavor="+cloudVm.getFlavorId();
			List<Vm> vmList = openStackVmService.list(cloudVm.getDcId(), cloudVm.getPrjId(), url);
			cloudVmList = queryVmListByOrder(cloudVm.getOrderNo());
			List<CloudFloatIp> flaotIpList = new ArrayList<CloudFloatIp>(); 
			if("1".equals(cloudVm.getBuyFloatIp())){
				flaotIpList = queryFloatByOrderNo(cloudVm.getOrderNo(),true);
			}
			int index =0;
			
			step:
			for(BaseCloudVm cvm:cloudVmList){
				for(Vm vm:vmList){
					if(vm.getId().equals(cvm.getVmId())){
						if("ERROR".equalsIgnoreCase(vm.getVm_state())){
							isError = true;
							isAllSuccess = true;
							CloudOrderVm orderVm = new CloudOrderVm();
							orderVm.setCreateUser(cloudVm.getCreateName());
							orderVm.setOrderNo(cloudVm.getOrderNo());
							orderVm.setBuyFloatIp(cloudVm.getBuyFloatIp());
							orderVm.setCreateUser(cloudVm.getCreateName());
							orderVm.setCusId(cloudVm.getCusId());
							TransactionAspectSupport.currentTransactionStatus();
							vmService.createFailHandler(orderVm , cloudVmList);
							break step;
						}
						if("ACTIVE".equalsIgnoreCase(vm.getVm_state())){
							index++;
						}
						if("ACTIVE".equalsIgnoreCase(vm.getVm_state()) && !"ACTIVE".equalsIgnoreCase(cvm.getVmStatus())){
							if("1".equals(cloudVm.getBuyFloatIp())){
								CloudFloatIp floatIp = flaotIpList.get(index-1);
								floatIp.setResourceType("vm");
								floatIp.setVmIp(cvm.getVmIp());
								floatIp.setResourceId(cvm.getVmId());
								floatIpService.bindResource(floatIp);
							}
							if(!StringUtils.isEmpty(cloudVm.getSelfSubnetId())){
								InterfaceAttachment interAtta = openStackVmService.bindPort(cloudVm.getDcId(), cloudVm.getPrjId(), cvm.getVmId(), cloudVm.getNetId(),
										cloudVm.getSelfSubnetId(),new String []{cloudVm.getSgId()});
								cvm.setSelfIp(interAtta.getFixed_ips()[0].getIp_address());
								cvm.setSelfPortId(interAtta.getPort_id());
							}
							cvm.setVmStatus("ACTIVE");
							cvm.setHostId(vm.getHostId());
							cvm.setHostName(vm.getHypervisor_hostname());
							
							updateVm(cvm);
							
							List<String> volumeIds = vm.getVolumes_attached();
							if(null != volumeIds && volumeIds.size()>0){
								for(String volId:volumeIds){
									BaseCloudVolume baseVolume = cloudVolumeDao.findOne(volId);
									if(null == baseVolume || StringUtils.isEmpty(baseVolume.getVolId())){
										BaseCloudVolume volume = new BaseCloudVolume();
										volume.setVolId(volId);
										volume.setVolName(volId);
										volume.setCreateName(cloudVm.getCreateName());
										volume.setCreateTime(cloudVm.getCreateTime());
										volume.setPrjId(cloudVm.getPrjId());
										volume.setDcId(cloudVm.getDcId());
										volume.setVolBootable("1");
										volume.setOsType(cloudVm.getOsType());
										volume.setSysType(cloudVm.getSysType());
										volume.setDiskFrom(cloudVm.getVmFrom());
										volume.setVmId(cvm.getVmId());
										volume.setFromImageId(cloudVm.getFromImageId());
										volume.setVolSize(cloudVm.getDisks());
										volume.setVolStatus("CREATING");
										volume.setIsDeleted("0");
										volume.setPayType(cloudVm.getPayType());
										volume.setChargeState("0");
										volume.setIsVisable("0");
										volume.setVolTypeId(cloudVm.getVolTypeId());
										
										addVoluneDB(volume);
										
										JSONObject jsonVol =new JSONObject();
										jsonVol.put("volId", volId);
										jsonVol.put("dcId",volume.getDcId());
										jsonVol.put("prjId", volume.getPrjId());
										jsonVol.put("volStatus",volume.getVolStatus());
										jsonVol.put("volTypeId",volume.getVolTypeId());
										jsonVol.put("volBootable", "1");
										jsonVol.put("count", "0");
										
										final JSONObject data = jsonVol;
										TransactionHookUtil.registAfterCommitHook(new Hook() {
											@Override
											public void execute() {
												push(RedisKey.volKey, data.toJSONString());
											}
										});
									}
								}
							}
						}
						
					}
				}
			}
			
			if(index == cloudVm.getNumber()){
				List<CloudFloatIp> successFloatIpList = queryFloatByOrderNo(cloudVm.getOrderNo(), false);
				List<BaseCloudVm> successVmList = queryVmListByOrder(cloudVm.getOrderNo());
				CloudOrderVm orderVm = new CloudOrderVm();
				orderVm.setCreateUser(cloudVm.getCreateName());
				orderVm.setBuyFloatIp(cloudVm.getBuyFloatIp());
				orderVm.setOrderNo(cloudVm.getOrderNo());
				orderVm.setPayType(cloudVm.getPayType());
				orderVm.setCpu(cloudVm.getCpus());
				orderVm.setRam(cloudVm.getRams());
				orderVm.setSysTypeId(cloudVm.getVolTypeId());
				orderVm.setDisk(cloudVm.getDisks());
				orderVm.setSysType(cloudVm.getSysType());
				orderVm.setCusId(cloudVm.getCusId());
				orderVm.setDcId(cloudVm.getDcId());
				orderVm.setImageId(cloudVm.getFromImageId());
				
				vmService.allVmSuccessHnadler(orderVm,successFloatIpList,successVmList);
				isAllSuccess = true;
			}
		}catch(Exception e){
		    log.error(e.getMessage(),e);
		    if(!isError){
		    	CloudOrderVm orderVm = new CloudOrderVm();
		    	orderVm.setCreateUser(cloudVm.getCreateName());
		    	orderVm.setBuyFloatIp(cloudVm.getBuyFloatIp());
		    	orderVm.setOrderNo(cloudVm.getOrderNo());
		    	orderVm.setBuyFloatIp(cloudVm.getBuyFloatIp());
		    	orderVm.setCreateUser(cloudVm.getCreateName());
		    	orderVm.setCusId(cloudVm.getCusId());
		    	
		    	vmService.createFailHandler(orderVm , cloudVmList);
		    }
			throw e;
		}
		
		return isAllSuccess;
	}
	
	/**
	 * 查询订单下创建成功的主机
	 * 
	 * @param orderNo
	 * 				订单编号
	 * @return
	 */
	private List<BaseCloudVm> queryVmListByOrder(String orderNo){
		List<BaseCloudVm> vmList = new ArrayList<BaseCloudVm>();
		StringBuffer sql = new StringBuffer();
		
		sql.append("	SELECT                                                ");
		sql.append("		vm.vm_id,                                         ");
		sql.append("		vm.vm_name,                                         ");
		sql.append("		vm.vm_status                                      ");
		sql.append("	FROM                                                  ");
		sql.append("		cloud_batchresource cbr                           ");
		sql.append("	LEFT JOIN cloud_vm vm ON cbr.resource_id = vm.vm_id   ");
		sql.append("	AND cbr.resource_type = 'vm'                          ");
		sql.append("	where vm.is_deleted='0'                               ");
		sql.append("	and cbr.order_no = ?                                  ");
		
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[]{orderNo});
		
		@SuppressWarnings("rawtypes")
		List result = query.getResultList();
		if(null != result && result.size()>0){
			for(int i =0 ;i<result.size();i++){
				int index = 0;
				Object [] objs = (Object []) result.get(i);
				CloudVm vm = new CloudVm();
				
				vm.setVmId(String.valueOf(objs[index++]));
				vm.setVmName(String.valueOf(objs[index++]));
				vm.setVmStatus(String.valueOf(objs[index++]));
				
				vmList.add(vm);
			}
		}
		return vmList;
	}
	
	/**
	 * 查询订单下创建成功的公网IP（未绑定资源的）
	 * 
	 * @param orderNo
	 * 				订单编号
	 * @return
	 */
	private List<CloudFloatIp> queryFloatByOrderNo(String orderNo,boolean isUnbind){
		List<CloudFloatIp> floatIpList = new ArrayList<CloudFloatIp>();
		StringBuffer sql = new StringBuffer();
		
		sql.append("	SELECT                                                ");
		sql.append("		flaotIp.flo_id,                                   ");
		sql.append("		flaotIp.dc_id,                                    ");
		sql.append("		flaotIp.prj_id,                                   ");
		sql.append("		flaotIp.flo_ip                                    ");
		sql.append("	FROM                                                  ");
		sql.append("		cloud_batchresource cbr                           ");
		sql.append("	LEFT JOIN cloud_floatip flaotIp                       ");
		sql.append("	ON cbr.resource_id = flaotIp.flo_id                   ");
		sql.append("	AND cbr.resource_type = 'floatip'                     ");
		sql.append("	where flaotIp.is_deleted='0'                          ");
		if(isUnbind){
			sql.append("	and flaotIp.resource_id is null                   ");
		}
		sql.append("	and cbr.order_no = ?                                  ");
		
		javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sql.toString(), new Object[]{orderNo});
		
		@SuppressWarnings("rawtypes")
		List result = query.getResultList();
		if(null != result && result.size()>0){
			for(int i =0 ;i<result.size();i++){
				int index = 0;
				Object [] objs = (Object []) result.get(i);
				CloudFloatIp flaotIp = new CloudFloatIp();
				flaotIp.setFloId(String.valueOf(objs[index++]));
				flaotIp.setDcId(String.valueOf(objs[index++]));
				flaotIp.setPrjId(String.valueOf(objs[index++]));
				flaotIp.setFloIp(String.valueOf(objs[index++]));
				
				floatIpList.add(flaotIp);
			}
		}
		return floatIpList;
	}
	
	private String toType(BaseCloudVm vm){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StringBuffer resourceType = new StringBuffer();
		resourceType.append(ResourceSyncConstant.VM);
		resourceType.append("-").append(CloudResourceUtil.escapePayType(vm.getPayType())).append(ResourceSyncConstant.SEPARATOR);
		resourceType.append("创建时间：").append(sdf.format(vm.getCreateTime()));
		if(PayType.PAYBEFORE.equals(vm.getPayType()) && null != vm.getEndTime()){
			resourceType.append(ResourceSyncConstant.SEPARATOR).append("到期时间：").append(sdf.format(vm.getEndTime()));
		}
		
		return resourceType.toString();
	}
}
