package com.eayun.physical.ecmcservice.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.jpa.JpaQueryUtils;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.dao.DataCenterDao;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.physical.dao.CabinetDao;
import com.eayun.physical.dao.DcCabinetRfDao;
import com.eayun.physical.dao.FirewallDao;
import com.eayun.physical.ecmccontroller.EcmcFirewallController;
import com.eayun.physical.ecmcservice.EcmcFirewallService;
import com.eayun.physical.ecmcvoe.DcFirewallVOE;
import com.eayun.physical.model.BaseDcCabinet;
import com.eayun.physical.model.BaseDcFirewall;

@Service
@Transactional
public class EcmcFirewallServiceImpl implements EcmcFirewallService {
	@SuppressWarnings("unused")
    private final Log log = LogFactory.getLog(EcmcFirewallController.class);
	@Autowired
	private FirewallDao firewallDao;
	@Autowired
	private DataCenterDao dataCenterDao;
	@Autowired
	private CabinetDao cabinetDao;
	@Autowired
	private DcCabinetRfDao dcCabinetRfDao;
	
	
	@SuppressWarnings({ "unused", "unchecked" })
    @Override
	public Page query( String name, String dcId,QueryMap queryMap)throws AppException {
		StringBuffer hql = new StringBuffer("from BaseDcFirewall where 1=1 ");	
		int index=0;
		Page page=null;
		Object[] args = new Object[2];  
		if (name != null && !"".equals(name)) {
			hql.append("and name like ?");
			args[index]="%"+name+"%";
			index++;
		
		}
		if (dcId != null && !"".equals(dcId)) {
			hql.append("and dataCenterId=?");
			args[index]=dcId;
			index++;
		}
		
			
		
		
		hql.append(" order by creDate desc");
		
		 Object[] params = new Object[index];  
		  System.arraycopy(args, 0, params, 0, index);
		  page=firewallDao.pagedQuery(hql.toString(), queryMap, params);
		  List<DcFirewallVOE> list = (List<DcFirewallVOE>) page.getResult();
			List<DcFirewallVOE> newList = new ArrayList<DcFirewallVOE>();
			String id = "",sql = "";
			
			for(BaseDcFirewall item : list){
				DcFirewallVOE voe = new DcFirewallVOE(item);
				 
				id = item.getDataCenterId();
				
				BaseDcDataCenter	datacenter=dataCenterDao.getdatacenterbyid(voe.getDataCenterId());
				if(datacenter!=null){
					String dcName = datacenter.getName();
					voe.setDataCenterName(dcName);
				}
				id = item.getCabinetId();
				
				BaseDcCabinet cabinet= cabinetDao.getcabinetByid(id);
				if(cabinet!=null){
					String cabinetName = cabinet.getName();
					voe.setCabinetName(cabinetName);
				}
				newList.add(voe);
			}
			page.setResult(newList);
		
		
		
		return page;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
	public DcFirewallVOE queryById(String id) throws AppException {
		DcFirewallVOE voemodel=null;
		BaseDcFirewall model=firewallDao.queryById(id);
		String equid=model.getId();
		String cabinetId=model.getCabinetId();
		String dataCenterId=model.getDataCenterId();
		String sql = "select min(location)  from dc_cabinet_rf where re_id=? and cabinet_id=? and data_center_id=?";
		
		Query query=firewallDao.createSQLNativeQuery(sql.toString());
		List list=new ArrayList();
		list.add(equid);
		list.add(cabinetId);
		list.add(dataCenterId);
		JpaQueryUtils.setParameters(query, list);
		String min=query.getSingleResult().toString();
		if(min!=null&&!"".equals(min)){
			DcFirewallVOE dsv = new DcFirewallVOE(model);
			dsv.setState(min);
			
			String name=dataCenterDao.getdatacenterName(dataCenterId);
			
			if(name!=null&&!"".equals(name)){
				dsv.setDataCenterName(name);
			}
			
			
			String cabineyname=cabinetDao.getcabinetName(cabinetId);
			if(cabineyname!=null&&!"".equals(cabineyname)){
				dsv.setCabinetName(cabineyname);
			}
			voemodel=dsv;
		}
			
		
		return voemodel;
	}

	@Override
	public void createfirewall(BaseDcFirewall model, String state, String userid)throws AppException  {
		
		
		if(model.getCreDate()==null){
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String startTime = sdf.format(new Date()); 
				model.setCreDate(startTime);
			}
			if(model.getCreUser()==null){
				model.setCreUser(userid);
			}
			firewallDao.saveEntity(model);
			updateCabinetRf(model,state,false);
		
			
		
		
	}

	/**
	 * 修改设备在机柜中的位置
	 * @param model存储信息
	 * @param state设备放置位置
	 * @param deleteFlag是否为删除方法调用，删除方法调用为true，否则为false
	 * */
	private void updateCabinetRf(BaseDcFirewall model, String state,boolean deleteFlag)throws AppException {
	
			
			dcCabinetRfDao.updatefirewallORcabinetrf(model.getId());
			if(deleteFlag){
				return;
			}
			
			//将交换机重新放置于机柜中
			for(int i=0;i<model.getSpec().intValue();i++){
				
				dcCabinetRfDao.updatefirewallORcabinetrf(model.getId(), model.getCabinetId(), model.getDataCenterId(), (Integer.parseInt(state)+i));
			}
		
	}

	@Override
	public void updatefirewall(BaseDcFirewall model, String state) throws AppException{
		
			firewallDao.saveEntity(model);
			updateCabinetRf(model,state,false);
		
		
	}

	@Override
	public void delete(String id) throws AppException {
		
		BaseDcFirewall basedcfirewall=firewallDao.queryById(id);
		firewallDao.remove(basedcfirewall);
		updateCabinetRf(basedcfirewall,"",true);
		
	}

	@Override
	public boolean addcheckNameExist(String name, String id) throws AppException{
		List<BaseDcFirewall>list=firewallDao.queryById(name, id);
		if(list.size()==0){
			return true;
		}else{
			return false;
		}
		 
		
	}

	@Override
	public boolean updatecheckNameExist(String name, String id, String datacenterid) throws AppException{
		//DcFirewall model=new DcFirewall();
		List<BaseDcFirewall>list=firewallDao.queryById(name, datacenterid,id);
		if(list.size()==0){
			return true;
		}else{
			return false;
		}
	}

	
	/**
	 * 2016-04-12
	 * */
	
	@Override
	public int getcountfirewall(String id) throws AppException{
		
		return firewallDao.getcountfirewall(id);
	}
		
	
	
}
