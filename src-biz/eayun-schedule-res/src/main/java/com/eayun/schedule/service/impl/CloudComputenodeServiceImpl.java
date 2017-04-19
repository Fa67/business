package com.eayun.schedule.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.constant.SyncProgress.SyncByDatacenterTypes;
import com.eayun.common.constant.SyncProgress.SyncType;
import com.eayun.common.sync.SyncProgressUtil;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.service.OpenstackHypervisorService;
import com.eayun.physical.model.BaseDcServer;
import com.eayun.schedule.service.CloudComputenodeService;
import com.eayun.virtualization.dao.CloudComputenodeDao;
import com.eayun.virtualization.model.BaseCloudComputenode;

@Transactional
@Service
public class CloudComputenodeServiceImpl implements CloudComputenodeService{
	private static final Logger log = LoggerFactory.getLogger(CloudComputenodeServiceImpl.class);
	@Autowired
	private OpenstackHypervisorService openstackService;
	@Autowired
	private CloudComputenodeDao	computenodeDao;
	@Autowired
	private SyncProgressUtil syncProgressUtil;
	
	/**
	 * 同步底层数据
	 * -----------------
	 * @author zhouhaitao
	 * @param ccnList
	 * @param dcId
	 */
	public void synchData (BaseDcDataCenter dataCenter){
		List<BaseCloudComputenode> stackList = null;
		List<BaseCloudComputenode> dbList = null;
		List<BaseCloudComputenode> serverList = null;
		Map<String,BaseCloudComputenode> mapDb=new HashMap<String,BaseCloudComputenode>();
		Map<String,BaseCloudComputenode> map=new HashMap<String ,BaseCloudComputenode>();
		Map<String,BaseCloudComputenode> mapServer=new HashMap<String ,BaseCloudComputenode>();
		try{
			stackList = openstackService.getStackList(dataCenter);
			dbList = queryComputenodeListByDcId(dataCenter.getId());
			serverList = queryServerListByDcId(dataCenter.getId());
			if(null!=dbList){
				for(BaseCloudComputenode ccn :dbList){
					mapDb.put(ccn.getHostId(), ccn);
				}
			}
			long total = stackList == null ? 0L : stackList.size();
			syncProgressUtil.initResourceTotal(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.COMPUTE_NODE, total);
			if(null!=stackList){
				log.info("数据中心ID:"+dataCenter.getId()+" 下存在"+stackList.size()+"个计算节点");
				for(BaseCloudComputenode c:stackList){
					if(mapDb.containsKey(c.getHostId())){
						updateComputenode(c);
					}
					else{
						String id = UUID.randomUUID().toString();
						
						c.setId(id);
						computenodeDao.saveOrUpdate(c);
					}
					map.put(c.getHostId(), c);
					syncProgressUtil.incrResourceDone(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.COMPUTE_NODE);
				}
			}
			
			if(null!=dbList){
				for(BaseCloudComputenode ccn :dbList){
					if(!map.containsKey(ccn.getHostId())){
						computenodeDao.delete(ccn.getId());;
					}
				}
			}
			
			if(null!=serverList){
				for(BaseCloudComputenode ccn :serverList){
					mapServer.put(ccn.getHostId(), ccn);
				}
			}
			
			if(null!=stackList){
				for(BaseCloudComputenode c:stackList){
					if(mapServer.containsKey(c.getHostId())){
						updateDcServer(c);
					}
					else{
						String id = UUID.randomUUID().toString();
						
						BaseDcServer dc =new BaseDcServer();
						dc.setId(id);
						dc.setDatacenterId(c.getDcId());
						dc.setName(c.getHostName());
						dc.setServerInnetIp(c.getHostIp());
						dc.setServerUses("计算节点");
						dc.setMemo("计算节点");
						dc.setServerModelId("-1");
						dc.setIsComputenode("0");
						dc.setNodeId(c.getHostId());
						dc.setIsMonitor("1");
						dc.setServerId(id);
						
						addDcServer(dc);
					}
				}
			}
			
			if(null!=serverList){
				for(BaseCloudComputenode ccn :serverList){
					if(!map.containsKey(ccn.getHostId())){
						deleteDcServer(ccn.getId());
					}
				}
			}
		} catch (Exception e) {
			log.error("同步本地计算节点出错：" + e.getMessage(),e);
			throw e;
		} 
	}
	
	/**
	 * 修改底层数据与数据库不一致的信息
	 * ----------------------
	 * @author zhouhaitao
	 * @param bccn
	 * @return
	 */
	public boolean updateComputenode(BaseCloudComputenode bccn){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_computenode set ");
			sql.append("  host_name = ?,   ");  
			sql.append("  host_ip = ?,     ");  
			sql.append("  running_vms = ?, ");  
			sql.append("  vcpus = ?,       ");  
			sql.append("  vcpu_used = ?,   ");  
			sql.append("  memory_mb = ?,   ");  
			sql.append("  memory_used = ?, ");  
			sql.append("  free_disk = ?  ,");  
			sql.append("  free_ram = ? ,    ");  
			sql.append("  host_status = ?  ");  
			sql.append(" where host_id = ? ");
			sql.append(" and dc_id = ? ");
			
			computenodeDao.execSQL(sql.toString(), new Object []{
				bccn.getHostName(),
				bccn.getHostIp(),
				bccn.getRunningVms(),
				bccn.getVcpus(),
				bccn.getVcpuUsed(),
				bccn.getMemoryMb(),
				bccn.getMemoryUsed(),
				bccn.getFreeDisk(),
				bccn.getFreeRam(),
				bccn.getHostStatus(),
				bccn.getHostId(),
				bccn.getDcId()
			});
			flag = true;
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			flag = false ;
		}
		return flag;
	}
	
	/**
	 * 查询当前数据中心的计算节点列表
	 * ------------------
	 * @author zhouhaitao
	 * @param dcId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<BaseCloudComputenode> queryComputenodeListByDcId(String dcId){
		StringBuffer hql = new StringBuffer();
		hql.append(" from  BaseCloudComputenode ");
		hql.append(" where 1=1   ");
		hql.append(" and dcId = ?   ");
		return computenodeDao.find(hql.toString(), new Object []{dcId});
	}
	
	
	@SuppressWarnings("rawtypes")
	public List<BaseCloudComputenode> queryServerListByDcId(String dcId){
		List<BaseCloudComputenode> list = new ArrayList<BaseCloudComputenode>() ;
		StringBuffer sql = new StringBuffer();
		sql.append(" select  id,node_id,datacenter_id ");
		sql.append(" from dc_server ");
		sql.append(" where datacenter_id=? ");
		sql.append(" and is_computenode = ? ");
		
		Query query = computenodeDao.createSQLNativeQuery(sql.toString(), new Object[]{dcId,"0"});
		List listResult = (List) query.getResultList();
		for (int i = 0; i < listResult.size(); i++) {
			Object[] obj = (Object[]) listResult.get(i);
			BaseCloudComputenode ccn = new BaseCloudComputenode ();
			ccn.setId(obj[0]+"");
			ccn.setHostId(obj[1]+"");
			ccn.setDcId(obj[2]+"");
			
			list.add(ccn);
		}
		return list ;
	}
	
	public boolean addDcServer (BaseDcServer compute){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" insert into dc_server ");
			sql.append(" (");
			sql.append(" 	id,");
			sql.append(" 	datacenter_id,");
			sql.append(" 	name,");
			sql.append(" 	server_innet_ip,");
			sql.append(" 	server_uses,");
			sql.append(" 	memo,");
			sql.append(" 	server_model_id,");
			sql.append(" 	is_computenode,");
			sql.append(" 	node_id,");
			sql.append(" 	is_monitor,");
			sql.append(" 	server_id");
			sql.append(" ) ");
			sql.append("  values   ");  
			sql.append("  (?,?,?,?,?,?,?,?,?,?,?)");  
			
			computenodeDao.execSQL(sql.toString(), new Object []{
				compute.getId(),
				compute.getDatacenterId(),
				compute.getName(),
				compute.getServerInnetIp(),
				compute.getServerUses(),
				compute.getMemo(),
				compute.getServerModelId(),
				compute.getIsComputenode(),
				compute.getNodeId(),
				compute.getIsMonitor(),
				compute.getServerId()
			});
			flag = true;
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			flag = false ;
		}
		return flag;
	}
	
	public boolean updateDcServer (BaseCloudComputenode compute){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update dc_server set  ");
			sql.append(" name = ? ,  ");
			sql.append(" server_innet_ip = ? ");
			sql.append(" where node_id =? and datacenter_id = ?  ");
			
			computenodeDao.execSQL(sql.toString(), new Object []{
				compute.getHostName(),
				compute.getHostIp(),
				compute.getHostId(),
				compute.getDcId()
			});
			flag = true;
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			flag = false ;
		}
		return flag;
	}
	public boolean deleteDcServer (String id){
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" delete from dc_server ");
			sql.append(" where id = ? ");
			
			computenodeDao.execSQL(sql.toString(), new Object []{id});
			flag = true;
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			flag = false ;
		}
		return flag;
	}
	
	
}
