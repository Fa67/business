package com.eayun.physical.ecmcservice.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.SeqManager;
import com.eayun.datacenter.dao.DataCenterDao;
import com.eayun.physical.dao.CabinetDao;
import com.eayun.physical.dao.DcCabinetRfDao;
import com.eayun.physical.dao.SwitchDao;
import com.eayun.physical.ecmcservice.EcmcSwitchService;
import com.eayun.physical.ecmcvoe.DcSwitchVOE;
import com.eayun.physical.model.BaseDcSwitch;
import com.eayun.physical.model.DcSwitch;

@Service
@Transactional
public class EcmcSwitchServiceImpl implements EcmcSwitchService {
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	private SwitchDao switchDao;

	@Autowired
	private DataCenterDao dataCenterDao;
	
	@Autowired
	private CabinetDao cabinetDao;
	
	@Autowired
	private DcCabinetRfDao dcCabinetRfDao;
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
	public Page query(String datacenterid,String SwitchName, QueryMap queryMap) throws AppException{
		Page page=null;
		StringBuffer hql = new StringBuffer("from BaseDcSwitch where 1=1");
		int index=0;
		Object[] args = new Object[2];  
		if(SwitchName!=null&&!"".equals(SwitchName)){
			hql.append("  and name like ?");
			args[index]="%"+SwitchName+"%";
			index++;
			
		}
		if(datacenterid!=null&&!"".equals(datacenterid)){
			hql.append("  and dataCenterId =?");
			args[index]=datacenterid;
			index++;
		}
		hql.append("  order by creDate desc");
		 Object[] params = new Object[index];  
		  System.arraycopy(args, 0, params, 0, index);
		  page =switchDao.pagedQuery(hql.toString(), queryMap, params);
		  List<BaseDcSwitch> list = (List<BaseDcSwitch>) page.getResult();
			List<DcSwitchVOE> newList = new ArrayList<DcSwitchVOE>();
			String id = "",sql = "";
			
			for(BaseDcSwitch item : list){
				DcSwitchVOE voe = new DcSwitchVOE(item);
				id = item.getDataCenterId();
				
				String dcName=dataCenterDao.getdatacenterName(id);
			
				if(dcName!=null&&!"".equals(dcName)){
					
					voe.setDataCenterName(dcName);
				}
				id = item.getCabinetId();
				
				String cabinetName=cabinetDao.getcabinetName(id);
				
				if(cabinetName!=null&&!"".equals(cabinetName)){
					
					voe.setCabinetName(cabinetName);
				}
				sql = "select min(location) from dc_cabinet_rf where cabinet_id=? and data_center_id=? and re_id=? and re_type='3' and flag='1'";
				Query query=switchDao.createSQLNativeQuery(sql.toString());
				query.setParameter(1,item.getCabinetId());
				query.setParameter(2,item.getDataCenterId());
				query.setParameter(3,item.getId());
				List querylist=new ArrayList<>();
				querylist.add(item.getCabinetId());
				querylist.add(item.getDataCenterId());
				querylist.add(item.getId());
				JpaQueryUtils.setParameters(query, querylist);
				Object min=query.getSingleResult();
				
				if(min!=null){
				
					voe.setState(min.toString());
				}
				newList.add(voe);
			}
			page.setResult(newList);
		  
		return page;
	}

	@Override
	public void delete(String switchId)throws AppException {
	
		switchDao.delete(switchId);
		
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
	public DcSwitchVOE queryById(String switchid) throws AppException{
		DcSwitch model=new DcSwitch();
		BeanUtils.copyPropertiesByModel(model,switchDao.queryById(switchid) );
		
		List list=new ArrayList<>();
		list.add(model.getCabinetId());
		list.add(model.getDataCenterId());
		list.add(model.getId());
		String sql="select min(LOCATION)  from dc_cabinet_rf where cabinet_id=? and data_center_id=? and re_id=? and re_type='3' and flag='1'";
		Query query=switchDao.createSQLNativeQuery(sql.toString());
		JpaQueryUtils.setParameters(query, list);
		Object obj=query.getSingleResult();
		DcSwitchVOE voe = new DcSwitchVOE(model);
		if(obj==null){
			voe.setState("1");
		}else{
			voe.setState(obj.toString());
		}
		String name=dataCenterDao.getdatacenterName(model.getDataCenterId());
		
		if(name!=null&&!"".equals(name)){
			voe.setDataCenterName(name);
		}
		
		
		String cabineyname=cabinetDao.getcabinetName(model.getCabinetId());
		if(cabineyname!=null&&!"".equals(cabineyname)){
			voe.setCabinetName(cabineyname);
			
		}
		
		return voe;
	}

	@Override
	public void update(BaseDcSwitch model, String state) throws AppException{
	
		switchDao.saveOrUpdate(model);
		updateCabinetRf(model,state,false);
		
		
	}
	/**
	 * 修改设备在机柜中的位置
	 * @param model防火墙
	 * @param state设备放置位置
	 * @param deleteFlag是否为删除方法调用，删除方法调用为true，否则为false
	 * */
	private void updateCabinetRf(BaseDcSwitch model, String state,boolean deleteFlag)throws AppException {
		try {
			
			dcCabinetRfDao.updatefirewallORcabinetrf(model.getId());
			if(deleteFlag){
				return;
			}
			//将防火墙重新放置于机柜中
			for(int i=0;i<model.getSpec().intValue();i++){
				dcCabinetRfDao.updateswitchORcabinetrf(model.getId(), model.getCabinetId(), model.getDataCenterId(), Integer.parseInt(state)+i);
			}
			
		} catch (Exception e) {
			log.error(e,e);
		}
	}

	@Override
	public List<BaseDcSwitch> checkNameExist(String name, String dcid) throws AppException {
	//	DcSwitch model=new DcSwitch();
		
		List<BaseDcSwitch>list=switchDao.checkNameExist(name, dcid);
		
		
		return list;
	}
	
	@Override
	public List<BaseDcSwitch> checkNameExist(String name, String dcid,String id) throws AppException {
	//	DcSwitch model=new DcSwitch();
		List<BaseDcSwitch>list =switchDao.checkNameExist(name, dcid,id);
		
		return list;
	}

	@Override
	public void addswitch(BaseDcSwitch model, String user, String state) throws AppException {
		
			if(model.getId()==null||"".equals(model.getId())){
				model.setId(SeqManager.getSeqMang().getSeqForDate());
			}
			model.setSwitchId(UUID.randomUUID().toString());
			model.setCreUser(user);
			model.setCreDate(new Timestamp(new Date().getTime()));
		
			switchDao.saveEntity(model);
			//在机柜中添加设备
			updateCabinetRf(model,state,false);
		
		
		
	}

	
	/**
	 * 2016-04-012
	 * **/
	@Override
	public int getcountswitch(String id) throws AppException{
		
		return switchDao.getcountswitch(id);
	}

}
