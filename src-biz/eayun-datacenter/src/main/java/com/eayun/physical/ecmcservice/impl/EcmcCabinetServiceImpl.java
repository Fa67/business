package com.eayun.physical.ecmcservice.impl;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.jpa.JpaQueryUtils;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.SeqManager;
import com.eayun.datacenter.dao.DataCenterDao;
import com.eayun.datacenter.model.DcDataCenter;
import com.eayun.physical.dao.CabinetDao;
import com.eayun.physical.dao.DcCabinetRfDao;
import com.eayun.physical.ecmcservice.EcmcCabinetService;
import com.eayun.physical.ecmcvoe.DcCabinetEquVOE;
import com.eayun.physical.ecmcvoe.DcCabinetVOE;
import com.eayun.physical.model.BaseDcCabinet;
import com.eayun.physical.model.BaseDcCabinetRf;
import com.eayun.physical.model.DcCabinet;


@Service
@Transactional
public class EcmcCabinetServiceImpl  implements EcmcCabinetService{
	private final Log log = LogFactory.getLog(EcmcCabinetServiceImpl.class);
	@Autowired
	private CabinetDao cabinetDao;
	
	@Autowired
	private DataCenterDao dataCenterDao;
	@Autowired
	private DcCabinetRfDao dcCabinetRf;
	
	@SuppressWarnings("unchecked")
    @Override
	public Page query(String cabinetName,String datacenterId,QueryMap queryMap) throws AppException {
		StringBuffer hql = new StringBuffer("from BaseDcCabinet b where 1=1 ");
		//boolean flag=false; 
		int index=0;
		Page page=null;
		Object[] args = new Object[2];  
		if (cabinetName != null && !"".equals(cabinetName)) {
			hql.append("and b.name like ?");
			args[index]="%"+cabinetName+"%";
			index++;
		} 
		if (datacenterId != null && !"".equals(datacenterId)) {
			hql.append("and b.dataCenterId=? ");
			args[index]=datacenterId;
			index++;
		} 
		hql.append("ORDER BY creDate DESC");
		  Object[] params = new Object[index];  
		  System.arraycopy(args, 0, params, 0, index);
		  page=cabinetDao.pagedQuery(hql.toString(), queryMap, params);
		
		  List<DcCabinetVOE> newList = new ArrayList<DcCabinetVOE>();
			List<BaseDcCabinet> list = (List<BaseDcCabinet>) page.getResult();
		
			for(BaseDcCabinet dc : list){
				//获取机柜的使用情况——已经使用的容量
				int count = dcCabinetRf.getdccabinetrf(dc.getDataCenterId(), dc.getId());
				if(count>0){
					dc.setUsedCapacity(count);
				}else{
					dc.setUsedCapacity(0);
				}
				DcCabinetVOE voe = new DcCabinetVOE(dc);
				//获取机柜的使用情况——未使用的容量
				if(voe.getTotalCapacity()!=0)
				voe.setEmptyCapa(String.valueOf(voe.getTotalCapacity()-voe.getUsedCapacity()));
				
				//获取数据中心名称
				String name=dataCenterDao.getdatacenterName(dc.getDataCenterId());
				if(name!=null&&!"".equals(name)){
				
					voe.setDataCenterName(name);
				}
				newList.add(voe);
			}
			if(newList.size()>0){
				page.setResult(newList);
				
			}	
			return page;
	}

	@Override
	public String delete(String cabinetId, String dataCenterId) throws AppException {
		String result = "";
		
			int can =dcCabinetRf.getdccabinetrf(dataCenterId,cabinetId);
			if(can>0){
				result = "cant";
				return result;
			}
			cabinetDao.deletecabinet(cabinetId);
			dcCabinetRf.deletecabinetrf(cabinetId, dataCenterId);
			result = "success";
		
		
	
		return result;
		
		
	}

	@Override
	public DcCabinet queryById(String cabinetId) throws AppException {
		DcCabinet model=new DcCabinet();
		BeanUtils.copyPropertiesByModel(model,cabinetDao.getcabinetByid(cabinetId));
		
		return model;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
	public String update(String cabinetId, String dataCenterId, String cabinetName, String totalCapacity)
			throws AppException {
		String result = "";
		
		boolean fag=false;
			
			String [] count=dcCabinetRf.updatacabinetORcelectcabinetrf(cabinetId);
			if(count.length==0){
				fag=true;
			}
			for(int k=0;k<count.length;k++){
				if(count[k].equals(dataCenterId)){
					fag=true;
				}
				
			}
			if(fag){
				cabinetDao.updatecabinet(Integer.parseInt(totalCapacity), dataCenterId, cabinetName, cabinetId);
				int sum=dcCabinetRf.updatacabinetORcelectcabinetrf(cabinetId, dataCenterId);
				if(sum<Integer.parseInt(totalCapacity)){
					for(int i=sum+1;i<=Integer.parseInt(totalCapacity);i++){
						
						String sql = "insert into dc_cabinet_rf (id,cabinet_id,location,flag,data_center_id) values (?,?,?,'0',?)";
						List list=new ArrayList<>();
						list.add(SeqManager.getSeqMang().getSeqForDate());
						list.add(cabinetId);
						list.add(i);
						list.add(dataCenterId);
						Query query=cabinetDao.createSQLNativeQuery(sql.toString());
						JpaQueryUtils.setParameters(query, list);
						query.executeUpdate();
						}
					}else{
						dcCabinetRf.deletecabinetrflocation(cabinetId, dataCenterId, Integer.parseInt(totalCapacity));
						
					}
				result = "success";
				
			}else{
				result = "cant";
			}
			
		
		return result;
	}

	@Override
	public void add(BaseDcCabinet model) throws AppException {
		
		
			cabinetDao.saveEntity(model);
			//增加机柜时应该在机柜使用表(DC_CABINET_RF)中添加该机柜的可用容量的记录
			addCabinetCapacity(model);
			
		
		
	}
	
	private void addCabinetCapacity(BaseDcCabinet model)throws AppException {
			for(int i=0;i<model.getTotalCapacity();i++){
				int location = i+1;
				BaseDcCabinetRf dccabinetrf=new BaseDcCabinetRf();
				dccabinetrf.setCabinetId(model.getId());
				dccabinetrf.setData_center_id(model.getDataCenterId());
				dccabinetrf.setLocation(location);
				dccabinetrf.setFlag("0");
				dcCabinetRf.saveEntity(dccabinetrf);
			}
		
	}

	@Override
	public String[] add(String dcId, String cabinetName, String totalCapacity, String cabinetNum, String user)
			throws AppException {
		String [] ids=new String [Integer.parseInt(cabinetNum)];
				//如果增加机柜数量为1
				if(Integer.parseInt(cabinetNum)==1){
					BaseDcCabinet dc = new BaseDcCabinet();
					dc.setId(SeqManager.getSeqMang().getSeqForDate());
					dc.setName(cabinetName);
					dc.setCreUser(user);
					dc.setCreDate(new Timestamp(new Date().getTime()));
					dc.setDataCenterId(dcId);
					dc.setTotalCapacity(new Integer(totalCapacity));
					
						add(dc);
					ids[0]=dc.getId();
					
				}else{
					//批量增加机柜
					for(int i=0;i<Integer.parseInt(cabinetNum);i++){
						BaseDcCabinet dc = new BaseDcCabinet();
						dc.setId(SeqManager.getSeqMang().getSeqForDate());
						dc.setName(cabinetName+"_"+(i+1));
						dc.setCreUser(user);
						dc.setCreDate(new Timestamp(new Date().getTime()));
						dc.setDataCenterId(dcId);
						dc.setTotalCapacity(new Integer(totalCapacity));
						//dc.setCabinetId(UUID.randomUUID().toString());
						
							add(dc);
							ids[i]=dc.getId();
					}
				}
				return ids;
		}
		
	

	@SuppressWarnings("rawtypes")
    @Override
	public List getCabinet( String datacenterid, String equipmentId) throws AppException {
			String sql = "select id,name,total_capacity,used_capacity,data_center_id,cre_user,date_format(cre_date,'%y-%m-%d %h:%i:%s') cre_date,cabinet_id,memo from dc_cabinet where data_center_id=?";
			Object [] obj={datacenterid};
			List sqllist=cabinetDao.createSQLNativeQuery(sql.toString(),obj ).getResultList();
			
			//CachedRowSet crs = DBAccess.getDBTool().querySql(sql);
			List<BaseDcCabinet> list = new ArrayList<BaseDcCabinet>();
			for(int i=0;i<sqllist.size();i++){
				Object[] bojects=(Object[]) sqllist.get(i);
				BaseDcCabinet cabinet = new BaseDcCabinet();
				cabinet.setId(bojects[0]==null?"":bojects[0].toString());
				cabinet.setName(bojects[1]==null?"":bojects[1].toString());
				if(bojects[2]!=null&&!"".equals(bojects[2].toString())){
					cabinet.setTotalCapacity(new Integer(bojects[2].toString()));
				}
				if(bojects[3]!=null&&!"".equals(bojects[3].toString())){
					cabinet.setUsedCapacity(new Integer(bojects[3].toString()));
				}
				cabinet.setDataCenterId(bojects[4]==null?"":bojects[4].toString());
				cabinet.setCreUser(bojects[5]==null?"":bojects[5].toString());
				if(bojects[6]!=null&&!"".equals(bojects[6].toString())){
					Calendar calendar = Calendar.getInstance();
					try {
						Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(bojects[6].toString());
						calendar.setTime(date);
					} catch (Exception e) {
					    log.error(e.getMessage(),e);
							throw  new AppException("",e);
						
					}
					
					
					cabinet.setCreDate(new Timestamp(calendar.getTimeInMillis()));
				}
				cabinet.setCabinetId(bojects[7]==null?"":bojects[7].toString());
				cabinet.setMemo(bojects[8]==null?"":bojects[8].toString());
				list.add(cabinet);
			}
			if(equipmentId!=null){
			list=filterList(list,equipmentId);//对查找到的所有的机柜进行过滤，去掉已经用完的机柜
			}
		return list;
		
		
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
	public Page queryEquById(String id, String dcId, Page page ,QueryMap queryMap) throws AppException {
		String sql = "select id , re_id ,re_type ,count(location)  spec,max(location)  endState,min(location)  startState from "
				+ "dc_cabinet_rf where flag='1' and cabinet_id=? and data_center_id= ? and re_id in "
						+ "(select distinct re_id from dc_cabinet_rf where flag='1' and cabinet_id= ? and data_center_id=?) "
								+ "group by re_id,re_type order by startState DESC";
	
		
		Object [] values={id,dcId,id,dcId};
		
		 page=cabinetDao.pagedNativeQuery(sql, queryMap, values);
		 List list =new ArrayList();
		 list= (List) page.getResult();
		List <DcCabinetEquVOE> li =new ArrayList<DcCabinetEquVOE>();
		
		//list=query1.getResultList();
		for(int i=0;i<list.size();i++){
			Object [] object=(Object[]) list.get(i);
			DcCabinetEquVOE voe = new DcCabinetEquVOE();
			voe.setEquId(object[1]==null?"": object[1].toString());
			voe.setEquType(object[2]==null?"": object[2].toString());
			voe.setEquSpec(object[3]==null?"": object[3].toString());
			voe.setEquStartState(object[4]==null?"": object[5].toString());
			voe.setEquEndState(object[5]==null?"": object[4].toString());
			String tableName = "";
			if(voe.getEquType().equals("0")){
				voe.setEquType("服务器");
				tableName = "BaseDcServer";
			}else if(voe.getEquType().equals("1")){
				voe.setEquType("防火墙");
				tableName = "BaseDcFirewall";
			}else if(voe.getEquType().equals("2")){
				voe.setEquType("存储");
				tableName = "BaseDcStorage";
			}else if(voe.getEquType().equals("3")){
				voe.setEquType("交换机");
				tableName = "BaseDcSwitch";
			}
			String  [] valueslist={voe.getEquId()};
			sql = "select name from "+tableName +" where id=?";
			List NameList=cabinetDao.createQuery(sql.toString(),valueslist).list();
			
			if(NameList.size()>0){
				for(int j=0;j<NameList.size();j++){
					voe.setEquName(NameList.get(j).toString());
					li.add(voe);
				}
				
			}else{
				String updateSql="update dc_cabinet_rf set re_id=null ,re_type=null,flag=0 where cabinet_id=? and data_center_id=? and re_id=?";
				List querylist=new ArrayList<>();
				querylist.add(id);
				querylist.add(dcId);
				querylist.add(voe.getEquId());
				Query Upquery=cabinetDao.createSQLNativeQuery(updateSql.toString());
				JpaQueryUtils.setParameters(Upquery, querylist);
				Upquery.executeUpdate();
			}
		}
	
		page.setResult(li);
		return page;
	}

	@Override
	public int getCountByHql(String id) throws AppException {
		
		return (cabinetDao.getCountDateCenterById(id)).size();
	}

	@Override
	public DcDataCenter getDateCenterById(String id) throws AppException {
		DcDataCenter model=new DcDataCenter();
		BeanUtils.copyPropertiesByModel(model, dataCenterDao.getdatacenterbyid(id));
		return model;
				}

	@Override
	public int getMaxUsedLocation(String id) throws AppException {
		String sql="select  MAX(t.location) as maxLocation from dc_cabinet_rf t where t.cabinet_id=? and t.flag='1'";
		Query query=cabinetDao.createSQLNativeQuery(sql.toString(), null);
		query.setParameter(1, id);
		Object sum=query.getSingleResult();

		if(sum!=null&&!"".equals(sum)){
			return Integer.parseInt(sum.toString());
		}
			return -1;
	}

	@Override
	public List<BaseDcCabinet> checkNameExist(List<String> name, String datacenterid) throws AppException{
		
		return cabinetDao.checkName(name, datacenterid);
	}

	@Override
	public List<BaseDcCabinet> checkNameExist(String name, String datacenterid, String id) throws AppException{
		
		return cabinetDao.checkName(name, datacenterid,id);
	}

	@Override
	public int getCountByHql(String id, String cabinetid) throws AppException {
		
		return (cabinetDao.getcountbyid(id, cabinetid)).size();
	}

	@SuppressWarnings("rawtypes")
    @Override
	public JSONArray getstateByCabinet(String dataCenterId, String cabinetId, String spec, String id)
			throws AppException {
		
		JSONArray canUseStates = new JSONArray();
	
			List<String> canUse = new ArrayList<String>();
			//根据数据中心id和机柜id获取机柜的最大容量
			String sql = "select t.id,t.name,t.total_capacity,t.used_capacity,t.data_center_id,t.cre_user,date_format(t.cre_date,'%Y-%m-%d %H:%i:%s'),t.cabinet_id,t.memo from dc_cabinet t where t.data_center_id=? and id=?";
			
			List listargs=new ArrayList();
			Object[] args={dataCenterId,cabinetId};
			Query query=cabinetDao.createSQLNativeQuery(sql.toString(), args);
			listargs=query.getResultList();
			String capacity = "";
			if(listargs!=null&&listargs.size()>0){
				Object[] total=(Object[]) listargs.get(0);
				capacity = total[2].toString();
			}
			//根据数据中心id和机柜id从机柜使用量表(dc_cabinet_rf)中获取机柜的使用情况
			sql = "select t.id,t.cabinet_id,t.re_id,t.re_type,t.location,t.flag,t.data_center_id from dc_cabinet_rf t where t.data_center_id=? and t.cabinet_id = ? and t.flag='1'";
			
			Query query1=cabinetDao.createSQLNativeQuery(sql.toString(), args);
			
			List Listcabinetrf=query1.getResultList();
			List<String> usedStateList = null;
			if(Listcabinetrf.size()==0){
				usedStateList = new ArrayList<String>();
			}else{
				usedStateList = new ArrayList<String>();
				for(int i=0;i<Listcabinetrf.size();i++){
					Object [] objects=(Object[]) Listcabinetrf.get(i);
					usedStateList.add(objects[4].toString());
				}
			}
			//如果是修改方法的话应该不考虑设备本身占用的位置
			if(id!=null&&!"".equals(id)){
				sql = "select t.id,t.cabinet_id,t.re_id,t.re_type,t.location,t.flag,t.data_center_id from dc_cabinet_rf t where t.data_center_id=? and t.cabinet_id = ? and t.re_id=?";
				Object[] values={dataCenterId,cabinetId,id};
				Query query2=cabinetDao.createSQLNativeQuery(sql.toString(), values);
				
				List objectlist=query2.getResultList();
				List<String> temp = new ArrayList<String>();
				for(int j=0;j<objectlist.size();j++){
					Object [] listobjects=(Object[]) objectlist.get(j);
					temp.add(listobjects[4].toString());
				}
				usedStateList.removeAll(temp);
			}
			
			String[] usedState = new String[usedStateList.size()];
			for(int i=0;i<usedState.length;i++){
				usedState[i]=usedStateList.get(i);
			}
			canUse = checkCapacity(spec,usedState,capacity);
			if(canUse!=null){
				for(String state : canUse){
				JSONObject temp = new JSONObject();
				temp.put("state", state);
				canUseStates.add(temp);
				}
			}
		
		return canUseStates;
	}

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
    private List<BaseDcCabinet> filterList(List<BaseDcCabinet> list,String equipmentId) throws AppException{
		List<BaseDcCabinet> temp=new ArrayList<BaseDcCabinet>();
		BaseDcCabinet dc=null;
		String jiguiId="";
		int totalC=0;
		int totalU=0;
		for (BaseDcCabinet dcCabinet : list) {
			 dc=dcCabinet;
			jiguiId=dcCabinet.getId();
			totalC=dcCabinet.getTotalCapacity();
			totalU=dcCabinet.getUsedCapacity();
			String sql="select COUNT(*) as count from dc_cabinet_rf t where t.cabinet_id=? and t.flag='1'";
			
			Object [] obj={jiguiId};
			String sum=cabinetDao.createSQLNativeQuery(sql.toString(),obj ).getSingleResult().toString();
		
				if(sum!=null&&"".equals(sum)){
					dc.setUsedCapacity(new Integer(sum));
				}else{
					dc.setUsedCapacity(0);
				}
			}
			if(totalC==totalU&&totalU!=0&&totalC!=0){//如果机柜位置使用完了，查看该设备是否本就存在于该机柜下面
				String strSql="select COUNT(*) as count from dc_cabinet_rf t where t.cabinet_id=? and t.re_id=? and t.flag='1'";
				
				List sumquerylist=new ArrayList<>();
				sumquerylist.add(jiguiId);
				sumquerylist.add(equipmentId);
				String sum=cabinetDao.createSQLNativeQuery(strSql.toString(), sumquerylist).getSingleResult().toString();
//			
				if("0".equals(sum)){//设备不存在于该机柜下面
					}else{
						temp.add(dc);
					}
			}else{
				temp.add(dc);
			}

		return temp;
	}

	
	

	/**
	 * 获取所有在机柜中可使用的位置
	 * @param spec设备规格
	 * @param usedState机柜已经被使用的位置
	 * @param capacity机柜可用空间
	 * */
	private static List<String> checkCapacity(String spec, String[] usedState, String capacity) throws AppException{
		List<String> canUse = new ArrayList<String>();

		if(spec==null){
			return null;
		}
		//如果设备规格大于机柜容量返回
		if(Integer.parseInt(spec)>Integer.parseInt(capacity)){
			return canUse;
		}
		
		//如果被使用的空间大小等于机柜容量返回
		if(usedState.length==Integer.parseInt(capacity)){
			return canUse;
		}
		
		//获取所有机柜空置位置
		List<Integer> nullCapa = getNullCapa(capacity,usedState);
		

		//获取所有符合放置条件的第一位置的集合
		canUse = getCanUseCapa(spec,nullCapa);
		
		return canUse;
	}

	private static List<String> getCanUseCapa(String num, List<Integer> nullCapa)throws AppException {
		List<String> canUse = new ArrayList<String>();
		for(int i=0;i<nullCapa.size()-Integer.parseInt(num)+1;i++){
			int temp = nullCapa.get(i);
			int[] temps = new int[Integer.parseInt(num)];
			for(int j=0;j<Integer.parseInt(num);j++){
				temps[j]=temp+j;
			}
			
			if(isCanUse(temps,nullCapa)){
				canUse.add(String.valueOf(temp));
			}
		}
		
		return canUse;
	}

	private static boolean isCanUse(int[] temps, List<Integer> nullCapa) throws AppException{
		boolean isCanUse = true;
		
		for(int i=0;i<temps.length;i++){
			int num = temps[i];
			for(int j=0;j<nullCapa.size();j++){
				if(nullCapa.get(j).equals(num)){
					isCanUse = true;
					break;
				}
				isCanUse = false;
			}
			if(!isCanUse){
				break;
			}
		}
		
		return isCanUse;
	}
	private static List<Integer> getNullCapa(String capacity, String[] specs) throws AppException{
		List<Integer> nullCapa = new ArrayList<Integer>();
		for(int i=0;i<Integer.parseInt(capacity);i++){
			String temp = i+1+"";
			boolean isNull = true;
			for(String str : specs){
				if(str.equals(String.valueOf(temp))){
					isNull = false;
					break;
				}
			}
			if(isNull){
				nullCapa.add(Integer.parseInt(temp));
			}
		}
		
		return nullCapa;
	}
	
	
	
	
	

	@Override
	public int getcountcabinet(String id) throws AppException{
		
		return cabinetDao.getcountcabinet(id);
	}





}
