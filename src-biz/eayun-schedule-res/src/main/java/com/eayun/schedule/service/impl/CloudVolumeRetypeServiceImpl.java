package com.eayun.schedule.service.impl;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.ConstantClazz;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.schedule.pool.SyncCloudResourcePool;
import com.eayun.schedule.service.CloudProjectService;
import com.eayun.schedule.service.CloudVolumeRetypeService;
import com.eayun.schedule.service.CloudVolumeService;
import com.eayun.schedule.thread.resource.CloudVolumeRetypeThread;
import com.eayun.virtualization.model.BaseCloudProject;

/**
 * retype
 * 
 * @author chengxiaodong
 * 
 */
@Transactional
@Service
@Scope("prototype")
public class CloudVolumeRetypeServiceImpl implements CloudVolumeRetypeService {
	private static final Log logger = LogFactory
			.getLog(CloudVolumeRetypeServiceImpl.class);
	@Autowired
	private CloudProjectService bindProjectService;// 项目表
	@Autowired
	private CloudVolumeService cloudVolumeService;
	@Autowired
	private JedisUtil jedisUtil;
	
	
	@Override
	public void synchAllData(BaseDcDataCenter dataCenter) throws AppException {

		ExecutorService es = null;
		try {
			SyncCloudResourcePool pool = new SyncCloudResourcePool();
			es = pool.get();
			syncByProjects(es, dataCenter);
		} catch (AppException e) {
		    logger.error(e.getMessage(),e);
			throw e;
		} catch (Exception e) {
		    logger.error(e.getMessage(),e);
			throw new AppException("", new String[] { "系统程序异常，请联系后台管理员!" });
		} finally {
		    if(es != null){
		        es.shutdown();
		    }
		}
	
	}
	
	

	/**
	 * 循环执行项目下的为设置类型的云硬盘
	 * 
	 * @author chengxiaodong
	 * @date 2017-03-20
	 * @param es
	 * @param dataCenter
	 */
	private void syncByProjects(ExecutorService es, BaseDcDataCenter dataCenter)
			throws Exception {
		List<BaseCloudProject> projects = bindProjectService
				.getAllProjectsByDcId(dataCenter.getId());// 获取本地库
		String flag = "";
		try {
			if (null != projects) {
				for (BaseCloudProject project : projects) {
					if(ConstantClazz.TROVE_MANAGED_TENANT.equals(project.getPrjName())){
						continue;
					}
					// 同步项目下需要设置type的云硬盘
					CloudVolumeRetypeThread cloudVolumeRetypeThread = new CloudVolumeRetypeThread(cloudVolumeService);
					cloudVolumeRetypeThread.setDataCenter(dataCenter);
					cloudVolumeRetypeThread.setProjectId(project.getProjectId());
					Future<String> cloudDiskFuture = es.submit(cloudVolumeRetypeThread);
					flag = cloudDiskFuture.get();
					logger.info("执行项目ID：" + project.getProjectId() + "下的云硬盘retype的结果" + flag);
	
				}
		
			}
		} catch (ExecutionException e) {
		    logger.error(e.getMessage(),e);
			if (e.getCause() instanceof AppException) {
				throw (AppException) e.getCause();
			}
			throw new Exception(e);
		}

	}

	
}
