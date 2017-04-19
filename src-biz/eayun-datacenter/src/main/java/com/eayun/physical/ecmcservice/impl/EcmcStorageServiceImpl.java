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

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.jpa.JpaQueryUtils;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.dao.DataCenterDao;
import com.eayun.physical.dao.CabinetDao;
import com.eayun.physical.dao.DcCabinetRfDao;
import com.eayun.physical.dao.DcStorageDao;
import com.eayun.physical.ecmcservice.EcmcStorageService;
import com.eayun.physical.ecmcvoe.DcStorageVOE;
import com.eayun.physical.model.BaseDcStorage;

@Service
@Transactional
public class EcmcStorageServiceImpl implements EcmcStorageService {
	private final Log log = LogFactory.getLog(this.getClass());

	@Autowired
	private DcStorageDao storageDao;
	
	@Autowired
	private DataCenterDao dataCenterDao;
	@Autowired
	private CabinetDao  cabinetDao;
	@Autowired
	private DcCabinetRfDao  dcCabinetRfDao;

	
	@Override
	public void queryName(String name, JSONObject object) throws AppException{
		
	//		Object objectlist=new Object();
			
			List<BaseDcStorage> list =storageDao.queryname(name);
		
			if(list!=null&&list.size()>0){
				object.put("state", "1");
			}else{
				object.put("state",'0');
			}
		
		
	}

	@SuppressWarnings({ "unchecked", "unused" })
    @Override
	public Page querybystoragelist(String dcid, String name,QueryMap queryMap) throws AppException{
		StringBuffer sql = new StringBuffer("from BaseDcStorage where 1=1");
		//List list=new ArrayList();
		int index=0;
		Page page=null;
		Object[] args = new Object[2];  
		if(name!=null&&!"".equals(name)){
			sql.append("  and name like ?");
			args[index]="%"+name+"%";
			index++;
		}
		if(dcid!=null&&!"".equals(dcid)){
			sql.append("  and dataCenterId =?");
			args[index]=dcid;
			index++;
		}
		sql.append("  order by creDate desc");
		 Object[] params = new Object[index];  
		  System.arraycopy(args, 0, params, 0, index);
		page=storageDao.pagedQuery(sql.toString(), queryMap, params);
		
		
		
		List<BaseDcStorage> pagelist = (List<BaseDcStorage>) page.getResult();
		List<DcStorageVOE> newList = new ArrayList<DcStorageVOE>();
		String id = "",pagesql = "";
		
		for(BaseDcStorage item : pagelist){
			DcStorageVOE voe = new DcStorageVOE(item);
			id = item.getDataCenterId();
			
			String dcName=dataCenterDao.getdatacenterName(id);
			if(dcName!=null&&!"".equals(dcName)){
				voe.setDataCenterName(dcName);
			}
			id = item.getCabinetId();
			
			String cabinet=cabinetDao.getcabinetName(id);
			
			if(cabinet!=null&&!"".equals(cabinet)){
				voe.setCabinetName(cabinet);
			}
			newList.add(voe);
		}
		page.setResult(newList);
		
		return page;
		
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
	public DcStorageVOE queryDcStorageById(String id) throws AppException{
		DcStorageVOE respdsv=null;
		BaseDcStorage baseDcStorage=storageDao.getstoragebyid(id);
		String equid = baseDcStorage.getId();
		String cabinetId = baseDcStorage.getCabinetId();
		String dataCenterId = baseDcStorage.getDataCenterId();
		String sql = "select min(location)  from dc_cabinet_rf where re_id=? and cabinet_id=? and data_center_id=?";
		
		Query query=storageDao.createSQLNativeQuery(sql.toString());
		List list=new ArrayList();
		list.add(equid);
		list.add(cabinetId);
		list.add(dataCenterId);
		JpaQueryUtils.setParameters(query, list);
		Object minstr=query.getSingleResult();
		String min=null;
		if(minstr!=null&&!"".equals(minstr)){
			min=minstr.toString();
		}
		
		if(min!=null&&!"".equals(min)){
			DcStorageVOE dsv = new DcStorageVOE(baseDcStorage);
			dsv.setState(min);
			
			String name=dataCenterDao.getdatacenterName(dataCenterId);
			
			if(name!=null&&!"".equals(name)){
				dsv.setDataCenterName(name);
			}
			
			
			String cabineyname=cabinetDao.getcabinetName(cabinetId);
			if(cabineyname!=null&&!"".equals(cabineyname)){
				dsv.setCabinetName(cabineyname);
				
			}
			respdsv=dsv;
		}
		return respdsv;
		
		
	}

	@Override
	public void queryDcStorageCreate(BaseDcStorage model, String state, String user) throws AppException{

		
			if(model.getCreDate()==null){
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String startTime = sdf.format(new Date()); 
				model.setCreDate(startTime);
			}
			if(model.getCreUser()==null){
				model.setCreUser(user);
			}
		
			storageDao.saveEntity(model);
			updateCabinetRf(model,state,false);
		
		
	}

	@Override
	public void queryDcStorageUpdate(BaseDcStorage model, String state)throws AppException {
			
			
			storageDao.saveOrUpdate(model);
			updateCabinetRf(model,state,false);
			
		
	}

	@Override
	public void queryDcStorageDel(String id) throws AppException{
		
			
			
			storageDao.deletestorage(id);
			BaseDcStorage model=new BaseDcStorage();
			model.setId(id);
			updateCabinetRf(model,"",true);
			
		
		
	}

	@Override
	public void getDatacenter(JSONObject object) throws AppException {
		object.put("datacenterlist",dataCenterDao.getAllList());
		
	}

	@Override
	public List<BaseDcStorage> checkNameExist(String name, String dcid) throws AppException{
		
		List<BaseDcStorage> list=storageDao.checkNameExist(name, dcid);
		return list;
	}

	@Override
	public List<BaseDcStorage> checkNameExistOfEdit(String name, String id, String dcid) throws AppException{
		
		List<BaseDcStorage> list=storageDao.checkNameExistOfEdit(name,id, dcid);
		
		
		return list;
	}


	/**
	 * 修改设备在机柜中的位置
	 * @param model存储信息
	 * @param state设备放置位置
	 * @param deleteFlag是否为删除方法调用，删除方法调用为true，否则为false
	 * */
	private void updateCabinetRf(BaseDcStorage model, String state,boolean deleteFlag)throws AppException {
		try {
		
			dcCabinetRfDao.updatefirewallORcabinetrf(model.getId());
			if(deleteFlag){
				return;
			}
			
			//将存储重新放置于机柜中
			for(int i=0;i<model.getSpec().intValue();i++){
				
				dcCabinetRfDao.updatesotageORcabinetrf(model.getId(), model.getCabinetId(), model.getDataCenterId(), (Integer.parseInt(state)+i));
				}
			
		} catch (Exception e) {
			log.error(e,e);
		}
	}
	
	
	/**
	 * 2016-04-12
	 * */
	@Override
	public int getcountstorage(String id) throws AppException{
		return storageDao.getcountstorage(id);
	}

}
