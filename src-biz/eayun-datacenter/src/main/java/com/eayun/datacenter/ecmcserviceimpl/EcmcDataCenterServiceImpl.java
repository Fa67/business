package com.eayun.datacenter.ecmcserviceimpl;


import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.SeqManager;
import com.eayun.common.util.StringUtil;
import com.eayun.datacenter.dao.DataCenterDao;
import com.eayun.datacenter.ecmcservice.EcmcDataCenterService;
import com.eayun.datacenter.ecmcvoe.DcDataCenterVOE;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.datacenter.model.DcDataCenter;
import com.eayun.eayunstack.service.OpenstackTenantService;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.physical.ecmcservice.EcmcCabinetService;
import com.eayun.physical.ecmcservice.EcmcFirewallService;
import com.eayun.physical.ecmcservice.EcmcServerService;
import com.eayun.physical.ecmcservice.EcmcStorageService;
import com.eayun.physical.ecmcservice.EcmcSwitchService;



/**
 * EcmcDataCenterServiceImpl
 *                       
 * @Filename: EcmcDataCenterServiceImpl.java
 * @Description: 
 * @Version: 1.0
 * @Author: chenglong
 * @Email: long.cheng@eayun.com
 * @History:<br>
 *<li>Date: 2016年3月29日</li>
 
 *
 */

@Service
@Transactional
public class EcmcDataCenterServiceImpl implements EcmcDataCenterService {
    private final Log log = LogFactory.getLog(EcmcDataCenterServiceImpl.class);
	@Autowired
	private DataCenterDao dataCenterDao;
	
	@Autowired
	private EcmcCabinetService ecmcCabinetService;
	@Autowired
	private EcmcServerService ecmcServerService;
	@Autowired
	private EcmcSwitchService ecmcSwitchService;
	@Autowired
	private EcmcStorageService ecmcStorageService;
	@Autowired
	private EcmcFirewallService ecmcFirewallService;
	@Autowired
	private OpenstackTenantService osTentService;

	@Autowired
	private EcmcLogService  ecmcLogService; 
	
	@Autowired
    private JedisUtil jedisUtil;
	
	/**
	 * 分页查询
	 * */
	@SuppressWarnings("unchecked")
    @Override
	public Page query(String dataCenterName ,QueryMap queryMap) throws AppException {
		Page page=null;
		
			StringBuffer hql = new StringBuffer("from BaseDcDataCenter b where 1=1 ");
			int index=0;
			Object[] args = new Object[1];  
			if (dataCenterName != null && !"".equals(dataCenterName)) {
				hql.append("and b.name like ?");
				args[0]="%"+dataCenterName+"%";
				index++;
			}
			hql.append("ORDER BY creDate DESC");
			  Object[] params = new Object[index];  
			  System.arraycopy(args, 0, params, 0, index);
				 page =dataCenterDao.pagedQuery(hql.toString(), queryMap, params);
				 
				 List<BaseDcDataCenter> list = (List<BaseDcDataCenter>) page.getResult();
					List<DcDataCenterVOE> newList = new ArrayList<DcDataCenterVOE>();
					String sum="";
					if(null!=list){
						    
						for(BaseDcDataCenter item : list){
							DcDataCenterVOE voe = new DcDataCenterVOE(item);
							//机柜数量
							 sum =ecmcCabinetService.getcountcabinet(voe.getId())+"";
							voe.setCabinetNum(sum+"/"+voe.getCabinetCapacity()+"  已使用");

							//服务器数量
							voe.setServerNum(ecmcServerService.getcountserver(voe.getId())+"");
							//交换机数量
							
							voe.setSwitchNum(ecmcSwitchService.getcountswitch(voe.getId())+"");
							//存储服务器数量
							
							voe.setStorageNum(ecmcStorageService.getcountstorage(voe.getId())+"");
							//防火墙数量
							
							voe.setFirewallNum(ecmcFirewallService.getcountfirewall(voe.getId())+"");
							
							newList.add(voe);
						}
					}
					if(newList.size()>0){
						page.setResult(newList);
						
					}		 
			
		
		return page;
	
		
		
	}
	
	
	/**
	 * 根基ID查询数据
	 * */
	@Override
	public DcDataCenter querybyid(String id) throws AppException {
		DcDataCenter model=new DcDataCenter();
		BeanUtils.copyPropertiesByModel(model,dataCenterDao.getdatacenterbyid(id) );
		return model;
	}

	/**
	 * 删除
	 * */
	@Override
	public void delete(String dataCenterId) throws AppException {
		try{
			/**
			 * API1.0添加：删除数据中心，清除API相关redis
			 */
			String apiDcCode = dataCenterDao.getApiCodeById(dataCenterId);
			jedisUtil.delete(RedisKey.API_DC_DCID+dataCenterId);
			jedisUtil.delete(RedisKey.API_DC_CODE+apiDcCode);
			jedisUtil.delete(RedisKey.API_SWITCH_STATUS+dataCenterId);
			
			dataCenterDao.deletedatacenter(dataCenterId);
			ecmcLogService.addLog("删除数据中心", "数据中心", "数据中心", null, 1, dataCenterId, null);
		}catch(Exception e){
			ecmcLogService.addLog("删除数据中心", "数据中心", "数据中心", null, 0, dataCenterId, e);
			throw new AppException("删除数据中心异常",e);
		}
	}

/**
 * 修改
 * */
	@Override
	public void update(BaseDcDataCenter model) throws AppException {
	
		try{
		dataCenterDao.saveOrUpdate(model);
		ecmcLogService.addLog("修改数据中心", "数据中心", model.getName(), null, 1, model.getId(), null);
		}catch(Exception e){
			ecmcLogService.addLog("修改数据中心", "数据中心", model.getName(), null, 0, model.getId(), e);
			throw new AppException("修改数据中心异常",e);
		}
		
		
	}

	/**
	 * 添加
	 * */
	@Override
	public void add(BaseDcDataCenter model, String user) throws AppException {
		if(model.getId()==null||"".equals(model.getId())){
			model.setId(SeqManager.getSeqMang().getSeqForDate());
		}
		model.setDcType("openstack");
		
		model.setCreUser(user);
		model.setCreDate(new Timestamp(new Date().getTime()));
		try{
			/**
			 * API1.0添加：添加数据中心，维护两个Region标识相关redis,开关状态默认为true
			 */
			JSONObject json = new JSONObject();
			json.put("dcId", model.getId());
			json.put("dcName", model.getName());
			jedisUtil.set(RedisKey.API_DC_CODE+model.getApiDcCode(), json.toJSONString());
			json.clear();
			json.put("apiDcCode", model.getApiDcCode());
			json.put("dcName", model.getName());
			jedisUtil.set(RedisKey.API_DC_DCID+model.getId(), json.toJSONString());
			model.setApiStatus(true);
			
			dataCenterDao.saveEntity(model);
			ecmcLogService.addLog("新增数据中心", "数据中心", model.getName(), null, 1, model.getId(), null);
		}
		catch(Exception e){
			ecmcLogService.addLog("新增数据中心", "数据中心", model.getName(), null, 0, model.getId(), e);
			throw new AppException("新增数据中心异常",e);
		}
	}

	
	/**
	 * 删除前检查
	 * **/
	@Override
	public boolean checkDataCenterRemoveCannot( String dataCenterId) throws AppException {
		StringBuffer message=new StringBuffer();
		boolean flag=true;//该数据中心是否可以删除
		
		int count=ecmcCabinetService.getcountcabinet(dataCenterId);
		if(count>0){
		message.append("该数据中心下面存在机柜，不能删除！");
		flag=false;
		}
		return flag;
	}

/**
 * 检查数据中心
 * */
	@Override
	public boolean checkDataCenterLinked(BaseDcDataCenter model) throws AppException {
		 model.setDcType("openstack");
		   //数据中心是否可以链接
			if("openstack".equals(model.getDcType())){
				//初始化opentack平台连接
				DcDataCenter dcDataCenter = new DcDataCenter();
				
					try {
						org.apache.commons.beanutils.BeanUtils.copyProperties(dcDataCenter, model);
					} catch (IllegalAccessException e) {
						throw new AppException("", e);
					} catch (InvocationTargetException e) {
						throw new AppException("", e);
					}
				
				
				return osTentService.checkLinked(dcDataCenter);
			}else if("ovirt".equals(model.getDcType())){
				return true;
			}else if("vmware".equals(model.getDcType())){
				return true;
			}
			return true;
	}

	
	/**
	 * 检查数据中心
	 * */
		@Override
		public boolean checkDataCenterLinkeddcid(BaseDcDataCenter model) throws AppException {
				//model.setDcType("openstack");
				   //数据中心是否可以链接
				if("openstack".equals(model.getDcType())){
					//初始化opentack平台连接
					DcDataCenter datacenter=new DcDataCenter();
					try {
						org.apache.commons.beanutils.BeanUtils.copyProperties(datacenter, model);
					} catch (IllegalAccessException e) {
					    log.error(e.getMessage(), e);
					} catch (InvocationTargetException e) {
					    log.error(e.getMessage(), e);
					}
					
					boolean fag= osTentService.checkLinked(datacenter);
					if(fag){
						/**
						 * API1.0添加：编辑数据中心，维护两个Region标识相关redis
						 */
						JSONObject json = new JSONObject();
						json.put("dcId", model.getId());
						json.put("dcName", model.getName());
						String apiDcCode = dataCenterDao.getApiCodeById(model.getId());
						try {
							if(StringUtil.isEmpty(apiDcCode) || !apiDcCode.equals(model.getApiDcCode())){
								jedisUtil.delete(RedisKey.API_DC_CODE+apiDcCode);
							}
							jedisUtil.set(RedisKey.API_DC_CODE+model.getApiDcCode(), json.toJSONString());
							json.clear();
							json.put("apiDcCode", model.getApiDcCode());
							json.put("dcName", model.getName());
							jedisUtil.set(RedisKey.API_DC_DCID+model.getId(), json.toJSONString());
						} catch (Exception e) {
							log.error(e.toString(), e);
						}
						
						dataCenterDao.merge(model);
					}
					return fag;
				}else if("ovirt".equals(model.getDcType())){
					return true;
				}else if("vmware".equals(model.getDcType())){
					return true;
				}
					return true;				
		}
	
/**
 * 检查名称
 * */
	@Override
	public boolean checkNameExist(String name) throws AppException {
		List <BaseDcDataCenter> list=dataCenterDao.checkNameExist(name);
		if(list.size()>0){
			return false;
		}else{
			return true;
		}
		
	}
/**
 * 检查名称及id
 * */
	@Override
	public boolean checkNameExist(String name, String id)throws AppException  {
		List <BaseDcDataCenter> list=dataCenterDao.checkNameExist(name, id);
		if(list.size()==0){
			return true;
		}else{
			return false;
		}
				
	}
	
	/**
	 * 获取数据中心下的机柜
	 * */

	@Override
	public int queryDatacentercabinetNum(String id)throws AppException  {
	
		return ecmcCabinetService.getcountcabinet(id);
	}

	/**
	 * 查询IP 查询
	 * */
	@SuppressWarnings("rawtypes")
    @Override
	public List queryip(String ip)throws AppException  {
		return dataCenterDao.queryip(ip);
	}


	@Override
	public List<DcDataCenter> getAllList() throws AppException{
		List<DcDataCenter> list=new ArrayList<DcDataCenter>();
		List<BaseDcDataCenter> model=dataCenterDao.getAllList();
		for(int i=0;i<model.size();i++){
			DcDataCenter datacenter=new DcDataCenter();
		BeanUtils.copyPropertiesByModel(datacenter,model.get(i) );
		list.add(datacenter);
		}
		return list;
	}

	@Override
	public DcDataCenter getdatacenterbyid(String id) throws AppException{
		DcDataCenter model=new DcDataCenter();
		BeanUtils.copyPropertiesByModel(model,dataCenterDao.getdatacenterbyid(id));
		return model;
	}

	@Override
	public BaseDcDataCenter getdatacenterbyname(String name) throws AppException {
		return dataCenterDao.getdatacenterbyname(name);
	}
	@Override
	public BaseDcDataCenter querysyndatacenterbyid(String dcid) throws AppException{
		return dataCenterDao.getdatacenterbyid(dcid);
	}


	@Override
	public String getdatacenterName(String id) throws AppException{
		
		return dataCenterDao.getdatacenterName(id);
	}

	@Override
	public String getProvinces(String id) {
	    return dataCenterDao.getProvinces(id);
	}

	
	@Override
	public int operationApiSwitchById(String operation, String dcId) throws AppException{
		boolean isOpen = false;
		if("1".equals(operation)){
			isOpen = true;
		}
		StringBuffer sb = new StringBuffer();
        sb.append("update BaseDcDataCenter set apiStatus=? where id=? ");
        int count = dataCenterDao.executeUpdate(sb.toString(), isOpen, dcId);
		return count;
	}


	/**
	 * Region标识重名校验
	 * @param apiDcCode
	 * @param dcId
	 * @return
	 */
	@Override
	public boolean checkApiDcCode(String apiDcCode, String dcId) {
		StringBuffer sb = new StringBuffer();
        sb.append("select count(*) from BaseDcDataCenter where apiDcCode = ? and id<>? ");
        List<Long> list = dataCenterDao.find(sb.toString(), apiDcCode,dcId);
        long count = list.get(0);
        return count == 0;
	}

}
