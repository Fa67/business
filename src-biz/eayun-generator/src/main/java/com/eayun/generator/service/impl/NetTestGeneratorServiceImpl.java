package com.eayun.generator.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.generator.constant.CusGeneratorConstant;
import com.eayun.generator.service.NetTestGeneratorService;
import com.eayun.virtualization.dao.CloudNetWorkDao;
import com.eayun.virtualization.dao.CloudRouteDao;
import com.eayun.virtualization.dao.CloudSubNetWorkDao;
import com.eayun.virtualization.dao.CloudVolumeDao;
import com.eayun.virtualization.model.BaseCloudNetwork;
import com.eayun.virtualization.model.BaseCloudRoute;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.model.CloudRoute;


@Transactional
@Service
public class NetTestGeneratorServiceImpl implements NetTestGeneratorService {

	private final static Logger log = LoggerFactory.getLogger(NetTestGeneratorServiceImpl.class);
	
	@Autowired
	private CloudNetWorkDao netWorkDao;
	
	@Autowired
	private CloudRouteDao routeDao;
	
	@Autowired
	private CloudVolumeDao volumeDao;
	
	@Autowired
	private CloudSubNetWorkDao subNetDao;
	
	@Autowired
	private JedisUtil jedisUtil;
	
	/**
	 * 压力测试的每一个新建项目下创建50个私有网络和路由
	 * @Author: duanbinbin
	 *<li>Date: 2016年12月21日</li>
	 */
	@Override
	public void createBatchNet() {
		List<CloudProject> prjList = getTestPrj();
		if(!prjList.isEmpty()){
			for(CloudProject pro:prjList){
				for(int i = 0;i<50;i++){
					BaseCloudNetwork baseNetWork = new BaseCloudNetwork();
					baseNetWork.setNetId(UUID.randomUUID().toString().replace("-", ""));
			        baseNetWork.setNetName(this.getNameprefix(CusGeneratorConstant.BATCH_NET));
			        baseNetWork.setCreateTime(new Date());     
			        baseNetWork.setAdminStateup("1");
			        baseNetWork.setIsShared("1");
			        baseNetWork.setRouterExternal("0");
			        baseNetWork.setNetStatus("ACTIVE");
			        baseNetWork.setCreateName(pro.getCustomerName());
			        baseNetWork.setPrjId(pro.getProjectId());
			        baseNetWork.setDcId(pro.getDcId());
			        baseNetWork.setChargeState("0");
			        if(i%2==0){
			        	baseNetWork.setPayType("1");
				        baseNetWork.setEndTime(DateUtil.getExpirationDate(baseNetWork.getCreateTime(), i+1, DateUtil.PURCHASE));
			        }else{
			        	baseNetWork.setPayType("2");
			        }
			        baseNetWork.setIsVisible("1");
			        netWorkDao.save(baseNetWork);
			        CloudRoute cloudRoute = new CloudRoute();
			        cloudRoute.setRouteName(this.getNameprefix(CusGeneratorConstant.BATCH_ROUTE));
			        cloudRoute.setPrjId(pro.getProjectId());
			        cloudRoute.setDcId(pro.getDcId());
			        cloudRoute.setCreateName(pro.getCustomerName());
			        cloudRoute.setRate(2);
			        cloudRoute.setNetWorkId(baseNetWork.getNetId());
			        this.addRoute(cloudRoute);
				}
			}
		}
	}
	
	private void addRoute(CloudRoute cloudRoute){
		BaseCloudRoute baseRoute = new BaseCloudRoute();
		BeanUtils.copyPropertiesByModel(baseRoute, cloudRoute);
		baseRoute.setRouteId(UUID.randomUUID().toString().replace("-", ""));
		baseRoute.setRouteName(cloudRoute.getRouteName());
		baseRoute.setRouteStatus("ACTIVE");
		baseRoute.setCreateTime(new Date());
		baseRoute.setRate(cloudRoute.getRate());
		routeDao.save(baseRoute);
    }

	@Override
	public List<CloudProject> getTestPrj(){
		String prefix = CusGeneratorConstant.CUS_PREFIX;
		
		List<CloudProject> prjList = new ArrayList<CloudProject>();
		List<String> list = new ArrayList<String>();
		StringBuffer strb = new StringBuffer();
		strb.append(" SELECT cp.prj_id,cp.prj_name ,cp.customer_id ,cp.dc_id,ss.user_account ");
		strb.append(" FROM cloud_project cp LEFT JOIN sys_selfuser ss ON cp.customer_id = ss.cus_id ");
		strb.append(" WHERE ss.user_account LIKE ? AND ss.is_admin ='1' ");
		list.add(prefix + "%");
		Query query = netWorkDao.createSQLNativeQuery(strb.toString(), list.toArray());
		if(null != query && query.getResultList().size() > 0){
			List newList = (List)query.getResultList();
			for(int i=0;i<newList.size();i++){
				Object[] obj = (Object[]) newList.get(i);
				CloudProject pro = new CloudProject();
				pro.setProjectId(String.valueOf(obj[0]));
				pro.setPrjName(String.valueOf(obj[1]));
				pro.setCustomerId(String.valueOf(obj[2]));
				pro.setDcId(String.valueOf(obj[3]));
				pro.setCustomerName(String.valueOf(obj[4]));
				prjList.add(pro);
			}
		}
    	return prjList;
    }
	
	@Override
	public String getNameprefix(String type){
		Date now =new Date();
		SimpleDateFormat sdf=new SimpleDateFormat("MMdd");
		String dateStr=sdf.format(now);
		String prefix = CusGeneratorConstant.TEST_BATCH_+type;
		
		long serialNum = jedisUtil.increase(CusGeneratorConstant.TEST_BATCH_+":"+type+":"+dateStr);
        StringBuffer result = new StringBuffer();
        result.append(prefix);
        result.append(dateStr);
        
        int zeroLenth = 6 - String.valueOf(serialNum).length();
        for (int i = 0; i < zeroLenth; i++) {
            result.append("0");
        }
        result.append(serialNum);
    	return result.toString();
    }
	
	@Override
	public List<BaseCloudNetwork> getBatchNets() {
		List<String> list = new ArrayList<String>();
		StringBuffer strb = new StringBuffer();
		strb.append("from BaseCloudNetwork where netName like ?");
		list.add(CusGeneratorConstant.TEST_BATCH_+CusGeneratorConstant.BATCH_NET + "%");
		List<BaseCloudNetwork> baseNetList = netWorkDao.find(strb.toString(), list.toArray());
		return baseNetList;
	}
	
	@Override
	public List<BaseCloudVolume> getBatchVolumes() {
		List<String> list = new ArrayList<String>();
		StringBuffer strb = new StringBuffer();
		strb.append("from BaseCloudVolume where volName like ? and volBootable='0' ");
		list.add(CusGeneratorConstant.TEST_BATCH_+CusGeneratorConstant.BATCH_VOLUME + "%");
		List<BaseCloudVolume> baseVolList = volumeDao.find(strb.toString(), list.toArray());
		return baseVolList;
	}
	
	@Override
	public List<BaseCloudSubNetWork> getBatchSubnets() {
		List<String> list = new ArrayList<String>();
		StringBuffer strb = new StringBuffer();
		strb.append("from BaseCloudSubNetWork where subnetName like ? ");
		list.add(CusGeneratorConstant.TEST_BATCH_+CusGeneratorConstant.BATCH_SUB + "%");
		List<BaseCloudSubNetWork> baseSubList = subNetDao.find(strb.toString(), list.toArray());
		return baseSubList;
	}
}
