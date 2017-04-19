package com.eayun.database.log.dao;

import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.database.log.model.BaseCloudRdsLog;

public interface CloudRDSLogDao extends IRepository<BaseCloudRdsLog, String>{

	@Query("select count(*) from BaseCloudRdsLog t where t.rdsInstanceId = ? ")
	public int countRdsLogByInstance(String rdsInstanceId); 
	
	@Query("from BaseCloudRdsLog t where t.rdsInstanceId = ? ")
	public List<BaseCloudRdsLog> queryRdsLogByInstance(String rdsInstanceId); 
	@SQLDelete(sql = "delete from BaseCloudRdsLog where rdsInstanceId = ?")
	public int deleteRdsLogByRdsInstanceId(String rdsInstanceId);
}
