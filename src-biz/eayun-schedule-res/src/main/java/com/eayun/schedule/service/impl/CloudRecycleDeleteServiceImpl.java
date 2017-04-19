package com.eayun.schedule.service.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.schedule.service.CloudRecycleDeleteService;
import com.eayun.syssetup.service.SysDataTreeService;
import com.eayun.virtualization.model.CloudSnapshot;
import com.eayun.virtualization.model.CloudVm;
import com.eayun.virtualization.model.CloudVolume;
import com.eayun.virtualization.service.SnapshotService;
import com.eayun.virtualization.service.VmService;
import com.eayun.virtualization.service.VolumeService;

@Service
public class CloudRecycleDeleteServiceImpl implements CloudRecycleDeleteService {
	
    private final Log log = LogFactory.getLog(CloudRecycleDeleteServiceImpl.class);
    
	@Autowired
	private VmService vmService;
	@Autowired
	private VolumeService volumeService;
	@Autowired
	private SnapshotService snapshotService;
	@Autowired
	private SysDataTreeService sysDataTreeService;
	
	
	/**
	 * 处理回收站中已经过去的云主机
	 * -----------------------
	 * @author zhouhaitao
	 * @param vmList
	 * @throws Exception 
	 * @throws AppException 
	 */
	private boolean handleExpireRecycleVm(long seconds) throws AppException, Exception{
		boolean result = true;
		List<CloudVm> vmList = vmService.queryRecycleVmList(seconds);
		
		SessionUserInfo sessionUser = new SessionUserInfo();
		sessionUser.setUserName("--");
		if(null != vmList && vmList.size()>0){
			for(CloudVm vm: vmList){
				try{
					vm.setDeleteType("2");
					vmService.deleteVm(vm, sessionUser);
				}catch(Exception e){
				    log.error(e.getMessage(), e);
					result = false;
				}
			}
		}
		
		return result;
	}
	/**
	 * 处理回收站中已经过期的云硬盘
	 * -----------------------
	 * @author zhouhaitao
	 * @param vmList
	 */
	private boolean handleExpireRecycleVolume(long seconds){
		boolean result = true;
		List<CloudVolume> volList = volumeService.queryRecycleVolumeList(seconds);
		
		SessionUserInfo sessionUser = new SessionUserInfo();
		sessionUser.setUserName("--");
		if(null != volList && volList.size()>0){
			for(CloudVolume vol: volList){
				try{
					sessionUser.setCusId(vol.getCusId());
					vol.setIsDeleted("1");
					volumeService.deleteVolume(vol, sessionUser);
				}
				catch(Exception e){
				    log.error(e.getMessage(), e);
					result = false;
				}
			}
		}
		return result;
	}
	
	/**
	 * 处理回收站中已经过期的云硬盘备份
	 * -----------------------
	 * @author zhouhaitao
	 * @param seconds
	 */
	private boolean handleExpireRecycleSnapshot(long seconds){
		boolean result = true;
		List<CloudSnapshot> snapList = snapshotService.queryRecycleSnapshotList(seconds);
		
		SessionUserInfo sessionUser = new SessionUserInfo();
		sessionUser.setUserName("--");
		if(null != snapList && snapList.size()>0){
			for(CloudSnapshot snapshot: snapList){
				try{
					sessionUser.setCusId(snapshot.getCusId());
					snapshot.setIsDeleted("1");
					snapshotService.deleteSnapshot(snapshot, sessionUser);
				}catch(Exception e){
				    log.error(e.getMessage(), e);
					result = false;
				}
			}
		}
		return result;
	}
	
	/**
	 * 处理回收站中已经过期的资源
	 * -----------------------
	 * @author zhouhaitao
	 * @param seconds
	 * @throws Exception 
	 * @throws AppException 
	 */
	public boolean handleExpireRecycleReource() throws AppException, Exception{
		boolean vmResult =true;
		boolean volResult =true;
		boolean snapResult =true;
		long seconds = getRecycleRetentionTime();
		
		try{
			vmResult = handleExpireRecycleVm(seconds);
		}catch(Exception e){
		    log.error(e.getMessage(), e);
		}
		
		try{
			volResult = handleExpireRecycleVolume(seconds);
		}catch(Exception e){
		    log.error(e.getMessage(), e);
		}
		
		try{
			snapResult = handleExpireRecycleSnapshot(seconds);
		}catch(Exception e){
		    log.error(e.getMessage(), e);
		}
		return vmResult&&volResult&&snapResult;
	}
	
	/**
	 * 获取回收站的资源的保留时长
	 * @return
	 */
	private long getRecycleRetentionTime(){
		long retention = 0L;
		String sdt = sysDataTreeService.getRetainTime();
		if(null != sdt){
			retention = Long.parseLong(sdt);
			retention = 60*60*retention;
		}
		return retention;
	}
}
