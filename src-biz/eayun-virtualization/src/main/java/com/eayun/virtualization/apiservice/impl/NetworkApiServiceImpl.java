package com.eayun.virtualization.apiservice.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.virtualization.apiservice.NetworkApiService;
import com.eayun.virtualization.baseservice.BaseNetworkService;
import com.eayun.virtualization.dao.CloudNetWorkDao;
import com.eayun.virtualization.model.CloudNetWork;

@Service
@Transactional
public class NetworkApiServiceImpl extends BaseNetworkService implements NetworkApiService{
	@Autowired
	private CloudNetWorkDao networkDao;
	
	/**
	 * 根据项目ID和网络ID查询私有网络
	 * --------------------------
	 * @author zhouhaitao
	 * 
	 * @param prjId				项目ID
	 * @param netId				私有网络ID
	 * @return
	 */
	public CloudNetWork queryNetworkByPrjIdAndNetId(String prjId,String netId){
		CloudNetWork network = null;
		StringBuffer sql = new StringBuffer();
		sql.append("			SELECT                                 	");
		sql.append("				net.net_id,                         ");
		sql.append("				net.net_name,                       ");
		sql.append("				route.route_id,                     ");
		sql.append("				route.net_id as outIp               ");
		sql.append("			FROM                                   	");
		sql.append("				cloud_network net                   ");
		sql.append("			LEFT JOIN cloud_route route            	");
		sql.append("				ON route.network_id = net.net_id   	");
		sql.append("			WHERE                                  	");
		sql.append("				net.charge_state = '0'              ");
		sql.append("			AND net.router_external = '0'          	");
		sql.append("			AND net.prj_id = ?                    	");
		sql.append("			AND net.net_id = ?                    	");
		
		
		javax.persistence.Query query = networkDao.createSQLNativeQuery(sql.toString(), new Object []{prjId,netId});
		@SuppressWarnings("rawtypes")
		List list = query.getResultList();
		if(null != list && list.size() == 1){
			network = new CloudNetWork();
			Object [] objs = (Object [])list.get(0);
			network.setNetId(String.valueOf(objs[0]));
			network.setNetName(String.valueOf(objs[1]));
			network.setRouteId(String.valueOf(objs[2]));
			network.setExtNetId(null == objs[3]?null:String.valueOf(objs[3]));
		}
		return network;
	}
}
