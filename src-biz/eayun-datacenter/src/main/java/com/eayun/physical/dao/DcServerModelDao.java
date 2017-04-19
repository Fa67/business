package com.eayun.physical.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.eayun.common.dao.IRepository;
import com.eayun.physical.model.BaseDcServer;
import com.eayun.physical.model.DcServerModel;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年4月5日
 */
public interface DcServerModelDao extends IRepository<DcServerModel,String>{

	@Query(" from DcServerModel t where t.name=? ")
	public List<DcServerModel> queryByName(String name);
	
	@Query(" from DcServerModel t where t.name=? and id<>? ")
	public List<DcServerModel> queryByNameNoID(String name,String id);
	
	@Query(" from BaseDcServer t where t.serverModelId=? ")
	public List<BaseDcServer> checkUseorNo(String DcServerModelID);
	
	
	
	
	@Query("select name from DcServerModel where id=?")
	public String getservermodelName(String id);
	@Query("select disk from DcServerModel where id=?")
	public String getservermodeldisk(String id);
	
}
