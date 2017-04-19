package com.eayun.schedule.service.impl;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.Hook;
import com.eayun.eayunstack.model.InterfaceAttachment;
import com.eayun.eayunstack.model.Vm;
import com.eayun.eayunstack.model.Volume;
import com.eayun.eayunstack.service.OpenstackVmService;
import com.eayun.eayunstack.service.OpenstackVolumeService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.schedule.service.CloudVmAttVolService;
import com.eayun.virtualization.dao.CloudVmDao;
import com.eayun.virtualization.dao.CloudVolumeDao;
import com.eayun.virtualization.model.BaseCloudVm;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.CloudFloatIp;
import com.eayun.virtualization.model.CloudOrderVm;
import com.eayun.virtualization.model.CloudVm;
import com.eayun.virtualization.model.CloudVolume;
import com.eayun.virtualization.service.CloudFloatIpService;
import com.eayun.virtualization.service.VmService;
import com.eayun.virtualization.service.VolumeService;

@Transactional
@Service
public class CloudVmAttVolServiceImpl implements CloudVmAttVolService {
    private static final Logger log = LoggerFactory.getLogger(CloudVmAttVolServiceImpl.class);
    @Autowired
    private JedisUtil jedisUtil;
	@Autowired
	private OpenstackVolumeService openStackVolumeService;
	@Autowired
	private OpenstackVmService openStackVmService;
	@Autowired
	private VmService vmService;
	@Autowired
	private VolumeService volService;
	@Autowired
	private CloudFloatIpService floatIpService;
	@Autowired
	private CloudVolumeDao volumeDao;
	@Autowired
	private CloudVmDao cloudVmDao;
	
	@Override
	public String pop(String key) {
		String value = null;
        try {
            value = jedisUtil.pop(key);
        } catch (Exception e){
            log.error(e.getMessage(),e);
            return null;
        }
        return value;
	}

	@Override
	public JSONObject getVm(CloudVolume cloudVolume) throws Exception{
		JSONObject result = null ;
		if(null!=cloudVolume){
			JSONObject json = openStackVmService.get(cloudVolume.getDcId(), 
					cloudVolume.getPrjId(), cloudVolume.getVmId());
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

	@Override
	public JSONObject getVol(CloudVolume cloudVolume) throws Exception{
		JSONObject result = null ;
		if(null!=cloudVolume){
			JSONObject json = openStackVolumeService.get(cloudVolume.getDcId(), 
					cloudVolume.getPrjId(), cloudVolume.getVolId());
			if(null!=json){
				boolean isDeleted=json.containsKey("itemNotFound");
				if(!isDeleted){
					result=json.getJSONObject(OpenstackUriConstant.DISK_DATA_NAME);
				}
				else{
					result =new JSONObject();
					result.put("deletingStatus", isDeleted+"");
				}
			}
		}
		return result;
	}

	@Override
	public void attach(CloudVolume cloudVolume) {
		boolean flag = openStackVolumeService.bind(cloudVolume.getDcId(), cloudVolume.getPrjId(), cloudVolume.getVolId(), cloudVolume.getVmId());
		if(flag){
			updateVolume(cloudVolume);
			
			JSONObject json = new JSONObject ();
			json.put("dcId",cloudVolume.getDcId());
			json.put("prjId",cloudVolume.getPrjId());
			json.put("volId",cloudVolume.getVolId());
			json.put("count", "0");
			json.put("volStatus",cloudVolume.getVolStatus());
			
			push(RedisKey.volKey, json.toJSONString());
			
		}
	}

	@Override
	public boolean push(String key, String value) {
		boolean flag = false;
		try {
			flag=  jedisUtil.push(key, value);
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			flag = false;
		}
		return flag;
	}

	
	public boolean updateVolume(CloudVolume cloudVolume){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_volume set  ");
			sql.append("  vm_id =?,vol_status = ?, bind_point=? ");
			sql.append(" where vol_id =? ");
			
			volumeDao.execSQL(sql.toString(), new Object []{cloudVolume.getVmId(),cloudVolume.getVolStatus(),cloudVolume.getBindPoint(),cloudVolume.getVolId()});
			flag = true;
		}catch(Exception e){
			flag = false;
			throw e ;
		}
		
		
		return flag ;
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
	 * 查看是否同步全部完成
	 * @throws Exception 
	 */
	public boolean syncAllSuccess(CloudVm cloudVm) throws Exception{
	    int volCount=0;
	    int vmCount=0;
	    boolean isAllSuccess=false;
	    
	    try {
	    	List<Volume> volList = openStackVolumeService.list(cloudVm.getDcId(), cloudVm.getPrjId());
			List<BaseCloudVolume> cloudVolumeList = queryVolListByOrder(cloudVm.getOrderNo());
			for(int i=0;i<cloudVolumeList.size();i++){
				BaseCloudVolume baseVol=cloudVolumeList.get(i);
				for(int j=0;j<volList.size();j++){
					Volume vol=volList.get(j);
					if(null!=baseVol.getVolId()&&null!=vol.getId()&&(baseVol.getVolId().equals(vol.getId()))){
						if("IN-USE".equals(vol.getStatus().toUpperCase())){
							String bindPoint=vol.getAttachments()[0].getDevice();
							String vmId=vol.getAttachments()[0].getServer_id();
							baseVol.setVolStatus(vol.getStatus().toUpperCase());
							baseVol.setBindPoint(bindPoint);
							baseVol.setVmId(vmId);
							CloudVolume cloudVolume=new CloudVolume();
							BeanUtils.copyPropertiesByModel(cloudVolume, baseVol);
							updateVolume(cloudVolume);
							volCount++;
						}
						break;
					}
				}
			}
			
			List<BaseCloudVm> cloudVmList=null;
			if(volCount==cloudVolumeList.size()){
				String url ="?flavor="+cloudVm.getFlavorId();
				List<Vm> vmList = openStackVmService.list(cloudVm.getDcId(), cloudVm.getPrjId(), url);
				cloudVmList = queryVmListByOrder(cloudVm.getOrderNo());
				
				for(int i=0;i<cloudVmList.size();i++){
					BaseCloudVm baseVm=cloudVmList.get(i);
					for(int j=0;j<vmList.size();j++){
						Vm vm=vmList.get(j);
						if(null!=baseVm.getVmId()&&null!=vm.getId()&&(baseVm.getVmId().equals(vm.getId()))){
							if("ACTIVE".equals(vm.getVm_state().toUpperCase())){
								vmCount++;
							}
							break;
						}
					}
				}
			}
			
			if((cloudVm.getNumber()==volCount&&cloudVm.getNumber()==vmCount)&&(volCount>0&&vmCount>0)){
				List<CloudFloatIp> successFloatIpList = queryFloatByOrderNo(cloudVm.getOrderNo(), false);
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
				vmService.allVmAndVolumesSuccessHnadler(orderVm, successFloatIpList, cloudVmList, cloudVolumeList);
				isAllSuccess=true;
			}
	
		} catch (Exception e) {
			syncFail(cloudVm);
			throw e;
		}

		return isAllSuccess;	
	}
	
	
	
	
	/**
	 * 同步挂载
	 */
	@Transactional(noRollbackFor={AppException.class,FileNotFoundException.class,Exception.class})
	public boolean syncVolumeAttchVm(CloudVm cloudVm)throws Exception{
		List<BaseCloudVm>	cloudVmList = queryVmListByOrder(cloudVm.getOrderNo());
		List<BaseCloudVolume> cloudVolumeList = queryVolListByOrder(cloudVm.getOrderNo());
		int count=0;
		boolean isAllSuccess=false;
		boolean isError=false;
		try{
			for(int i=0;i<cloudVmList.size();i++){
				BaseCloudVm baseVm=cloudVmList.get(i);
				BaseCloudVolume volume=cloudVolumeList.get(i);
				if(null!=baseVm&&null!=volume){
					boolean flag = openStackVolumeService.bind(volume.getDcId(), volume.getPrjId(), volume.getVolId(), baseVm.getVmId());
					if(flag){
						BaseCloudVolume baseVol=volumeDao.findOne(volume.getVolId()); 
						baseVol.setVmId(baseVm.getVmId());
						volumeDao.saveOrUpdate(baseVol);
						count++;
					}else{
						isError=true;
						isAllSuccess=true;
						syncFail(cloudVm);
					}
				}
				
			}
			
			if(count==cloudVmList.size()&&count==cloudVolumeList.size()){
				putAttchSuccess(cloudVm);
				isAllSuccess=true;
			}
			
		}catch(Exception e){
			if(!isError){
				syncFail(cloudVm);
			}
			throw e;
		}
		
		return isAllSuccess;
			
	}

	/**
	 * 同步创建中的云主机
	 * 
	 * @param cloudVm
	 * 
	 * @throws Exception 
	 */
	@Transactional(noRollbackFor={AppException.class,FileNotFoundException.class,Exception.class})
	public boolean syncVmAndVolumeInBuild(CloudVm cloudVm) throws Exception{
		boolean isAllSuccess = false;
		boolean isError=false;
		int index =0;
		int volIndex =0;
		int bindIndex=0;
		List<BaseCloudVm> cloudVmList = null;
		List<BaseCloudVolume> cloudVolumeList=null;
		try{
			
			if(null==cloudVm.getVmSure()||!"true".equals(cloudVm.getVmSure())){
				
				String url ="?flavor="+cloudVm.getFlavorId();
				List<Vm> vmList = openStackVmService.list(cloudVm.getDcId(), cloudVm.getPrjId(), url);
				cloudVmList = queryVmListByOrder(cloudVm.getOrderNo());
				List<CloudFloatIp> flaotIpList = new ArrayList<CloudFloatIp>(); 
				if("1".equals(cloudVm.getBuyFloatIp())){
					flaotIpList = queryFloatByOrderNo(cloudVm.getOrderNo(),true);
				}
				
				step:
				for(BaseCloudVm cvm:cloudVmList){
					for(Vm vm:vmList){
						if(vm.getId().equals(cvm.getVmId())){
							if("ERROR".equalsIgnoreCase(vm.getVm_state())){
								isError = true;
								isAllSuccess = true;
								syncFail(cloudVm);
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
										BaseCloudVolume baseVolume = volumeDao.findOne(volId);
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
											jsonVol.put("volBootable", "1");
											jsonVol.put("volTypeId",volume.getVolTypeId());
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
							break;
						}
					}
				}
			}
			
			
			if(!isError&&(null==cloudVm.getVolumeSure()||!"true".equals(cloudVm.getVolumeSure()))){
				List<Volume> volList = openStackVolumeService.list(cloudVm.getDcId(), cloudVm.getPrjId());
				cloudVolumeList = queryVolListByOrder(cloudVm.getOrderNo());
				
				if(null==cloudVolumeList||cloudVolumeList.size()==0){
					isAllSuccess = true;
					syncFail(cloudVm);
					return isAllSuccess;
				}
				
				step:
				for(BaseCloudVolume cvol:cloudVolumeList){
					for(Volume vol:volList){
						if(vol.getId().equals(cvol.getVolId())){
							if("ERROR".equalsIgnoreCase(vol.getStatus())){
								isAllSuccess = true;
								isError=true;
								syncFail(cloudVm);
								break step;
							}
							if("AVAILABLE".equalsIgnoreCase(vol.getStatus())){
								volIndex++;
							}
							if("AVAILABLE".equalsIgnoreCase(vol.getStatus()) && !"AVAILABLE".equalsIgnoreCase(cvol.getVolStatus())){
								cvol.setVolStatus("AVAILABLE");
								BaseCloudVolume baseVol=volumeDao.findOne(cvol.getVolId());
								baseVol.setVolStatus("AVAILABLE");
								volumeDao.saveOrUpdate(baseVol);
							}
							break;
						}
					}
				}
				
			}
			
			
			
			if(null!=cloudVm.getVmSure()&&"true".equals(cloudVm.getVmSure())){
				index=cloudVm.getNumber();
			}
			
			if(null!=cloudVm.getVolumeSure()&&"true".equals(cloudVm.getVolumeSure())){
				volIndex=cloudVm.getNumber();
			}
			
			
			
			if(!isError&&index == cloudVm.getNumber()&&volIndex == cloudVm.getNumber()){
				if(null==cloudVmList){
					cloudVmList = queryVmListByOrder(cloudVm.getOrderNo());
				}
				if(null==cloudVolumeList){
					cloudVolumeList = queryVolListByOrder(cloudVm.getOrderNo());
				}
				
				for(int i=0;i<cloudVmList.size();i++){
					BaseCloudVm baseVm=cloudVmList.get(i);
					BaseCloudVolume volume=cloudVolumeList.get(i);
					if(null!=baseVm&&null!=volume){
						boolean flag = openStackVolumeService.bind(volume.getDcId(), volume.getPrjId(), volume.getVolId(), baseVm.getVmId());
						if(flag){
							BaseCloudVolume baseVol=volumeDao.findOne(volume.getVolId()); 
							baseVol.setVmId(baseVm.getVmId());
							volumeDao.saveOrUpdate(baseVol);
							bindIndex++;
						}else{
							isAllSuccess=true;
							syncFail(cloudVm);
						}
					}
					
				}
				
				if(bindIndex==cloudVmList.size()&&bindIndex==cloudVolumeList.size()){
					putAttchSuccess(cloudVm);
					isAllSuccess=true;
				}else{
					isAllSuccess=true;
					syncFail(cloudVm);
				}
			}else if(index == cloudVm.getNumber()&&volIndex != cloudVm.getNumber()){
				cloudVm.setVmSure("true");
			}else if(index != cloudVm.getNumber()&&volIndex == cloudVm.getNumber()){
				cloudVm.setVolumeSure("true");
			}

		}catch(Exception e){
			if(!isError){
				syncFail(cloudVm);
		    }
			throw e;
		}
		
		return isAllSuccess;
	}
	
	
	/**
	 * 同步创建中的云硬盘
	 * 
	 * @author chengxiaodong
	 * @param cloudVolume
	 * @throws Exception 
	 */
	@Transactional(noRollbackFor={AppException.class,FileNotFoundException.class,Exception.class})
	public boolean syncVolumeInBuild(CloudVm cloudVm) throws Exception{
		boolean isAllSuccess = false;
		boolean isError=false;
		List<BaseCloudVolume> cloudVolumeList =null;
		try{
			
			List<Volume> volList = openStackVolumeService.list(cloudVm.getDcId(), cloudVm.getPrjId());
			cloudVolumeList = queryVolListByOrder(cloudVm.getOrderNo());
			
			if(null==cloudVolumeList||cloudVolumeList.size()==0){
				isAllSuccess = true;
				syncFail(cloudVm);
				return isAllSuccess;
			}
			
			int index =0;
			step:
			for(BaseCloudVolume cvol:cloudVolumeList){
				for(Volume vol:volList){
					if(vol.getId().equals(cvol.getVolId())){
						if("ERROR".equalsIgnoreCase(vol.getStatus())){
							isAllSuccess = true;
							isError=true;
							syncFail(cloudVm);
							break step;
						}
						if("AVAILABLE".equalsIgnoreCase(vol.getStatus())){
							index++;
						}
						if("AVAILABLE".equalsIgnoreCase(vol.getStatus()) && !"AVAILABLE".equalsIgnoreCase(cvol.getVolStatus())){
							cvol.setVolStatus("AVAILABLE");
							BaseCloudVolume baseVol=volumeDao.findOne(cvol.getVolId());
							baseVol.setVolStatus("AVAILABLE");
							volumeDao.saveOrUpdate(baseVol);
						}
						break;
					}
				}
			}
			
			if(index == cloudVm.getNumber()){
				JSONObject json = new JSONObject();
				json.put("orderNo", cloudVm.getOrderNo());
				json.put("dcId", cloudVm.getDcId());
				json.put("prjId", cloudVm.getPrjId());
				json.put("vmName", cloudVm.getVmName());
				json.put("vmStatus", "BUILD");
				json.put("count", "0");
				json.put("flavorId",cloudVm.getFlavorId() );
				json.put("number", cloudVm.getNumber());
				json.put("buyFloatIp", cloudVm.getBuyFloatIp());
				json.put("netId", cloudVm.getNetId());
				json.put("selfSubnetId", cloudVm.getSelfSubnetId());
				json.put("createName", cloudVm.getCreateName());
				json.put("createTime", cloudVm.getCreateTime());
				json.put("osType", cloudVm.getOsType());
				json.put("sysType", cloudVm.getSysType());
				json.put("vmFrom", cloudVm.getVmFrom());
				json.put("fromImageId", cloudVm.getFromImageId());
				json.put("sgId", cloudVm.getSgId());
				json.put("cusId", cloudVm.getCusId());
				json.put("cpus", cloudVm.getCpus());
				json.put("rams", cloudVm.getRams());
				json.put("disks", cloudVm.getDisks());
				json.put("dateDisks", cloudVm.getDataDisks());
				json.put("payType", cloudVm.getPayType());
				json.put("volumeSure", "true");
				
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
				isAllSuccess = true;
			}
		
		}catch(Exception e){
			if(!isError){
				syncFail(cloudVm);
		    }
			throw e;
		}
		return isAllSuccess;
	}
	
	
	
	
	
	/**
	 * 查询订单下创建成功的主硬盘
	 * 
	 * @param orderNo
	 * 				订单编号
	 * @return
	 */
	private List<BaseCloudVolume> queryVolListByOrder(String orderNo){
		List<BaseCloudVolume> volList = new ArrayList<BaseCloudVolume>();
		StringBuffer sql = new StringBuffer();
		
		sql.append("	SELECT                                                ");
		sql.append("		vol.vol_id,                                       ");
		sql.append("		vol.dc_id,                                        ");
		sql.append("		vol.prj_id,                                       ");
		sql.append("		vol.vol_name,                                     ");
		sql.append("		vol.vol_size,                                     ");
		sql.append("		vol.from_snapid,                                  ");
		sql.append("		vol.vol_description,                              ");
		sql.append("		vol.vol_status,                                   ");
		sql.append("		vol.vol_typeid                                    ");
		sql.append("	FROM                                                  ");
		sql.append("		cloud_batchresource cbr                           ");
		sql.append("	LEFT JOIN cloud_volume vol ON cbr.resource_id = vol.vol_id   ");
		sql.append("	AND cbr.resource_type = 'volume'                          ");
		sql.append("	where vol.is_deleted='0'                               ");
		sql.append("	and cbr.order_no = ?                                  ");
		
		javax.persistence.Query query = volumeDao.createSQLNativeQuery(sql.toString(), new Object[]{orderNo});
		
		@SuppressWarnings("rawtypes")
		List result = query.getResultList();
		if(null != result && result.size()>0){
			for(int i =0 ;i<result.size();i++){
				int index = 0;
				Object [] objs = (Object []) result.get(i);
				BaseCloudVolume volume = new BaseCloudVolume();
				volume.setVolId(String.valueOf(objs[index++]));
				volume.setDcId(String.valueOf(objs[index++]));
				volume.setPrjId(String.valueOf(objs[index++]));
				volume.setVolName(String.valueOf(objs[index++]));
				volume.setVolSize(Integer.parseInt(String.valueOf(objs[index++])));
				volume.setFromSnapId(String.valueOf(objs[index++]));
				volume.setVolDescription(String.valueOf(objs[index++]));
				volume.setVolStatus(String.valueOf(objs[index++]));
				volume.setVolTypeId(String.valueOf(objs[index++]));
				volList.add(volume);
			}
		}
		return volList;
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
		sql.append("		vm.vm_name,                                       ");
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
	
	
	public void addVoluneDB(BaseCloudVolume volume){
		volumeDao.saveOrUpdate(volume);
	}
	
	
	/**
	 * 云主机云硬盘同步成功，重新插入队列下次不再走
	 * 同步云主机和云硬盘状态的任务
	 */
	public void putAttchSuccess(CloudVm cloudVm){
		JSONObject json = new JSONObject();
		json.put("orderNo", cloudVm.getOrderNo());
		json.put("dcId", cloudVm.getDcId());
		json.put("prjId", cloudVm.getPrjId());
		json.put("vmName", cloudVm.getVmName());
		json.put("vmStatus", "BUILD");
		json.put("count", "0");
		json.put("flavorId",cloudVm.getFlavorId() );
		json.put("number", cloudVm.getNumber());
		json.put("buyFloatIp", cloudVm.getBuyFloatIp());
		json.put("netId", cloudVm.getNetId());
		json.put("selfSubnetId", cloudVm.getSelfSubnetId());
		json.put("createName", cloudVm.getCreateName());
		json.put("createTime", cloudVm.getCreateTime());
		json.put("osType", cloudVm.getOsType());
		json.put("sysType", cloudVm.getSysType());
		json.put("vmFrom", cloudVm.getVmFrom());
		json.put("fromImageId", cloudVm.getFromImageId());
		json.put("sgId", cloudVm.getSgId());
		json.put("cusId", cloudVm.getCusId());
		json.put("cpus", cloudVm.getCpus());
		json.put("rams", cloudVm.getRams());
		json.put("volTypeId",cloudVm.getVolTypeId());
		json.put("disks", cloudVm.getDisks());
		json.put("dateDisks", cloudVm.getDataDisks());
		json.put("payType", cloudVm.getPayType());
		json.put("isAttch", "true");
		
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
		
	}
	
	
	/**
	 * 同步失败，清理订单
	 * @throws Exception 
	 */
	private void syncFail(CloudVm cloudVm) throws Exception{	
		try {
			//List<CloudFloatIp> successFloatIpList = queryFloatByOrderNo(cloudVm.getOrderNo(), false);
			List<BaseCloudVm> successVmList = queryVmListByOrder(cloudVm.getOrderNo());
			List<BaseCloudVolume> cloudVolumeList = queryVolListByOrder(cloudVm.getOrderNo());
			CloudOrderVm orderVm = new CloudOrderVm();
			orderVm.setCreateUser(cloudVm.getCreateName());
			orderVm.setBuyFloatIp(cloudVm.getBuyFloatIp());
			orderVm.setOrderNo(cloudVm.getOrderNo());
			orderVm.setPayType(cloudVm.getPayType());
			orderVm.setCpu(cloudVm.getCpus());
			orderVm.setRam(cloudVm.getRams());
			orderVm.setDisk(cloudVm.getDisks());
			orderVm.setSysType(cloudVm.getSysType());
			orderVm.setCusId(cloudVm.getCusId());
			orderVm.setDcId(cloudVm.getDcId());
			orderVm.setImageId(cloudVm.getFromImageId());
			vmService.createFailVmsAndVolumes(orderVm, successVmList, cloudVolumeList);
		} catch (Exception e) {
			throw e;
		}
		
	}
	
	
}
