package com.eayun.physical.ecmcservice.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.eayun.common.util.SeqManager;
import com.eayun.datacenter.dao.DataCenterDao;
import com.eayun.physical.dao.CabinetDao;
import com.eayun.physical.dao.DcServerModelDao;
import com.eayun.physical.dao.ServerDao;
import com.eayun.physical.ecmcservice.EcmcServerService;
import com.eayun.physical.ecmcvoe.DcServerVOE;
import com.eayun.physical.model.BaseDcServer;
import com.eayun.physical.model.DcServerModel;


@Service
@Transactional
public class EcmcServerServiceImpl implements EcmcServerService{
	@SuppressWarnings("unused")
    private final Log log = LogFactory.getLog(this.getClass());
@Autowired
private ServerDao serverDao;
	
@Autowired
private DataCenterDao dataCenterDao;

@Autowired
private CabinetDao  cabinetDao;
@Autowired
private DcServerModelDao  dcServerModelDao;



	@SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
	public Page queryserver(String dcid, String type, String anyName,QueryMap queryMap) throws AppException {
		StringBuffer sql = new  StringBuffer("");
		sql.append("select t.id as id, t.name as name, t.server_uses as serverUses, ");
		sql.append(" t.server_innet_ip as serverInnetIp, t.cabinet_id as cabinetId, t.datacenter_id as datacenterId, ");
		sql.append(" t.cpu as cpu, t.memory as memory, t.disk_capacity as diskCapacity, ");
		sql.append(" t.spec as spec, t.memo as memo, t.is_monitor as isMonitor, ");
		sql.append(" t.memory_unit as memoryUnit, t.disk_unit as diskUnit, t.respon_person as responPerson, ");
		sql.append(" t.server_model_id as serverModelId, t.respon_person_mobile as responPersonMobile, t.server_id as serverId, ");
		sql.append(" t.is_computenode as isComputenode, t.server_outnet_ip as serverOutnetIp, t.cre_user as creUser,");
		sql.append(" t.cre_date as creDate, ");
		//物理服务器参数--end
		
		sql.append(" m.name as dcServerModelName, ");//服务器型号名称
		sql.append(" c.name as cabinetName, ");//机柜名称
		sql.append(" d.dc_name as datacenterName, ");//数据中心名称
		sql.append(" r.location as state ");//所属机柜开始位置
		sql.append(" from dc_server t  ");
		sql.append(" left join dc_datacenter d on  t.datacenter_id=d.id ");
		sql.append(" left join dc_cabinet c on  t.cabinet_id=c.id ");
		sql.append(" left join dc_server_model m on  t.server_model_id=m.id ");
		sql.append(" left join (select data_center_id,re_id,cabinet_id,re_type,flag,min(location) as location "
							+ "from dc_cabinet_rf GROUP BY re_id,data_center_id,cabinet_id)"
							+ " r on  t.cabinet_id=r.cabinet_id "
							+ "and r.data_center_id=t.datacenter_id and r.re_id=t.id  ");
		sql.append(" where 1=1 "); 
		int index=0;
		Object[] args = new Object[4];  
		if(!"".equals(dcid)&&dcid!=null){
		sql.append(" and t.datacenter_id=?");
		args[index]=dcid;
		index++;
		}
		if("1".equals(type) && null!=anyName&&!anyName.equals("")){
			sql.append(" and t.name like ?");
			args[index]="%"+anyName+"%";
			index++;
		}
		if("2".equals(type) && null!=anyName&&!anyName.equals("")){
			sql.append(" and m.name like ?");
			args[index]="%"+anyName+"%";
			index++;
		
		}
		
		if("3".equals(type) && null!=anyName&&!anyName.equals("")){
			sql.append(" and c.name like?");
			args[index]="%"+anyName+"%";
			index++;
		}
		sql.append(" order by t.cre_date desc");
//		 query=entityManager.createNativeQuery(sql.toString());
		 Object[] params = new Object[index];  
		  System.arraycopy(args, 0, params, 0, index);
		  Page page=serverDao.pagedNativeQuery(sql.toString(), queryMap, params);
		  
		
		List<Object[]>list=(List<Object[]>) page.getResult();
		List pagelist= new ArrayList();
		for(int i=0;i<list.size();i++){
			 Object[] object=list.get(i);
			 Map<String, Object> map=new HashMap<String,Object>();
			 map.put("id", object[0]==null?"":object[0]);
			 map.put("name", object[1]==null?"":object[1]);
			 map.put("serverUses", object[2]==null?"":object[2]);
			 map.put("serverInnetIp", object[3]==null?"":object[3]);
			 map.put("cabinetId", object[4]==null?"":object[4]);
			 map.put("datacenterId", object[5]==null?"":object[5]);
			 map.put("cpu", object[6]==null?"":object[6]);
			 map.put("memory", object[7]==null?"":object[7]);
			 map.put("diskCapacity", object[8]==null?"":object[8]);
			 map.put("spec", object[9]==null?"":object[9]);
			 map.put("memo", object[10]==null?"":object[10]);
			 map.put("isMonitor", object[11]==null?"":object[11]);
			 map.put("memoryUnit", object[12]==null?"":object[12]);
			 map.put("diskUnit", object[13]==null?"":object[13]);
			 map.put("responPerson", object[14]==null?"":object[14]);
			 map.put("serverModelId", object[15]==null?"":object[15]);
			 map.put("responPersonMobile", object[16]==null?"":object[16]);
			 map.put("serverId", object[17]==null?"":object[17]);
			 map.put("isComputenode", object[18]==null?"":object[18]);
			 map.put("serverOutnetIp", object[19]==null?"":object[19]);
			 map.put("creUser", object[20]==null?"":object[20]);
			 map.put("creDate", object[21]==null?"":object[21]);
			 map.put("dcServerModelName", object[22]==null?"":object[22]);
			 map.put("cabinetName", object[23]==null?"":object[23]);
			 map.put("datacenterName", object[24]==null?"":object[24]);
			 map.put("state", object[25]==null?"":object[25]);
			 String fag=object[18]==null?"":object[18].toString();
			 
			 if ("0".equals(fag)) {//如果是计算节点
				 Object [] values={object[1],object[5]};
					String vmsql ="select count(vm_id) sum  from  cloud_vm  where host_name=? and is_deleted='0'and dc_id=?" ;
					int count = 0;
					Object sum=serverDao.createSQLNativeQuery(vmsql.toString(),values).getSingleResult();
					if(sum!=null){
						 map.put("vmNumber",sum);
						 }else{
							
								 map.put("vmNumber",count);
							
						 }
					
				}else{
					 map.put("vmNumber",0);
				}
			 
			pagelist.add(map);
		}
		page.setResult(pagelist);
	
		
		
		return page;
		
		
		
	}

	

	
	@Override
	public List<BaseDcServer> querybyid(String dcid, String id, String name)throws AppException {
		
		if(id!=null){
			return serverDao.querybyid(name, dcid, id);
		}else{
			return serverDao.querybyid(name, dcid);
		}
		
		
				
	}

	@Override
	public List<DcServerModel> queryByServerModel() throws AppException{
		return serverDao.queryByServerModel();
	}

	@Override
	public DcServerModel getByServerModel(String serverid) throws AppException{
		
		return serverDao.queryByServerModelId(serverid);
	}

	@Override
	public void saveServer(BaseDcServer dcserver, String user, String startlocation) throws AppException{
		dcserver.setId(SeqManager.getSeqMang().getSeqForDate());
		dcserver.setServerId(UUID.randomUUID().toString());
		dcserver.setCreDate(new Timestamp(new Date().getTime()));
		dcserver.setCreUser(user);
		if(!"0".equals(dcserver.getIsMonitor())){
			dcserver.setIsMonitor("1");
		}
		dcserver.setIsComputenode("1");
	
		serverDao.saveEntity(dcserver);
		String id=dcserver.getId().toString();
		
		if(!"".equals(id)&& id!=null){
			this.updateCabinetRf(dcserver, startlocation);
		}
		
	}


	/**
	 * 重新将交换机放置于机柜中
	 * @param datacenterId  数据中心
	 * @param cabinetId 机柜
	 * @param spec 起始位置
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public void updateCabinetRf(BaseDcServer model, String state) throws AppException{
			// 重新将服务器放置于机柜中
			for (int i = 0; i < model.getSpec().doubleValue(); i++) {
				List list=new ArrayList<>();
				String sql = "update dc_cabinet_rf set flag='1',re_id=?,re_type='0' where cabinet_id=? "
					+"	 and data_center_id=? and location=?";
				//sqlV.add(sql);
				list.add(model.getId());
				list.add(model.getCabinetId());
				list.add(model.getDatacenterId());
				if("".equals(state)){
					list.add(i);
				}else{
					list.add((Integer.parseInt(state) + i));
				}
				Query query=serverDao.createSQLNativeQuery(sql.toString());
				JpaQueryUtils.setParameters(query, list);
				query.executeUpdate();
			}
	}

	@Override
	public void deleteserver(String id) throws AppException{
		serverDao.delete(id);
	}

	@Override
	public void deletecabinetrf( String idstr) throws AppException{
			//清空服务器占用的机柜位置
			String sql="update dc_cabinet_rf set flag='0',re_id='',re_type='' where re_id=?";
			
			Query query=serverDao.createSQLNativeQuery(sql.toString());
			query.setParameter(1,idstr);
			query.executeUpdate();
	}

	@Override
	public  DcServerVOE getByDcServerId(String id) throws AppException {
		BaseDcServer dcserver=serverDao.queryById(id);
		DcServerVOE dsv = new DcServerVOE(dcserver);
		
			String sql = "select min(location)  from dc_cabinet_rf where cabinet_id=? and data_center_id=? and re_id=?";
			Query query=serverDao.createSQLNativeQuery(sql.toString());
			query.setParameter(1, dsv.getCabinetId());
			query.setParameter(2, dsv.getDatacenterId());
			query.setParameter(3, dsv.getId());
			Object min=query.getSingleResult();
			if(min!=null&&!"".equals(min)){
				dsv.setState(min.toString());
			}
			

			String name=dataCenterDao.getdatacenterName(dsv.getDatacenterId());
			
			if(name!=null&&!"".equals(name)){
				dsv.setDatacenterName(name);
			}
			
			
			String cabineyname=cabinetDao.getcabinetName(dsv.getCabinetId());
			if(cabineyname!=null&&!"".equals(cabineyname)){
				dsv.setCabinetName(cabineyname);
			}
			String modelname=dcServerModelDao.getservermodelName(dsv.getServerModelId());
			
			if(modelname!=null&&!"".equals(modelname)){
				dsv.setDcServerModelName(modelname);
			}
			String modeldisk=dcServerModelDao.getservermodeldisk(dsv.getServerModelId());
			if(modeldisk!=null&&!"".equals(modeldisk)){
				dsv.setDiskUnit(modeldisk);
			}
			
		
		return dsv;
	}

	@Override
	public void update(BaseDcServer dcserver, String startlocation) throws AppException{
		
			//清空交服务器占用的机柜位置
			this.deleteCabinetRf(dcserver.getDatacenterId(), dcserver.getCabinetId(), dcserver.getId());
			if(!"0".equals(dcserver.getIsMonitor())){
				dcserver.setIsMonitor("1");
			}
			DcServerModel model=new DcServerModel();
			model.setId(dcserver.getServerModelId());
			//DcServerModel serverModel=this.getByServerModel(model);//根据服务器型号id获取服务器型号名称
			if("true".equals(dcserver.getIsComputenode())){
				dcserver.setIsComputenode("0");
			}
			else if("false".equals(dcserver.getIsComputenode())){
				dcserver.setIsComputenode("1");
			}
		
			serverDao.saveOrUpdate(dcserver);
			//重新将服务器放置于机柜中
			this.updateCabinetRf(dcserver, startlocation);
			
		
		
		
	}
	/**
	 * 清空服务器占用的机柜位置
	 * @param datacenterId 数据中心id
	 * @param cabinetId 机柜id
	 * @param ids 物理服务器id
	 */
	public void deleteCabinetRf(String datacenterId,String cabinetId,String ids) throws AppException{
		
			//清空服务器占用的机柜位置
			String sql="update dc_cabinet_rf set flag='0',re_id='',re_type='' where re_id=?";
			Query query=serverDao.createSQLNativeQuery(sql.toString());
			query.setParameter(1, ids).executeUpdate();
		
	}
	
	
	/**
	 * 2016-04-12
	 * **/
	@Override
	public int getcountserver(String id) throws AppException{
		
		return serverDao.getcountserver(id);
	}

}
