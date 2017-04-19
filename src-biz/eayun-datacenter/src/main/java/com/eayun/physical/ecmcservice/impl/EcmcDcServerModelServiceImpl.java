package com.eayun.physical.ecmcservice.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.physical.dao.DcServerModelDao;
import com.eayun.physical.ecmcservice.EcmcDcServerModelService;
import com.eayun.physical.model.BaseDcServer;
import com.eayun.physical.model.DcServerModel;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年4月5日
 */
@Service
@Transactional
public class EcmcDcServerModelServiceImpl implements EcmcDcServerModelService {

	@Autowired
	private DcServerModelDao dcServerModelDao; 
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
	public Page queryDcServerModelList(String serverName, Page page, QueryMap querymap) throws Exception {
		List<String> listparams = new ArrayList<String>();
		StringBuffer sql = new StringBuffer();
		sql.append("select * from dc_server_model where id!='-1' ");
		if(serverName!=null&&!"".equals(serverName)){
			sql.append(" and name like ? ");
			listparams.add("%"+serverName+"%");
		}
		sql.append(" order by cre_date desc");
		page = dcServerModelDao.pagedNativeQuery(sql.toString(), querymap, listparams.toArray());
        List newlist = (List) page.getResult();
        DcServerModel dcserver = null;
        for (int i = 0; i < newlist.size(); i++) {
        	dcserver = new DcServerModel();
        	Object[] objs = (Object[]) newlist.get(i);
        	dcserver.setId(String.valueOf(objs[0]));
        	dcserver.setName(String.valueOf(objs[1]));
        	dcserver.setCpu(String.valueOf(objs[2]));
        	dcserver.setMemory(String.valueOf(objs[3]));
        	dcserver.setDisk(String.valueOf(objs[4]));
        	dcserver.setSpec(String.valueOf(objs[5]));
        	dcserver.setProcessor(String.valueOf(objs[6]));
        	dcserver.setCreUser(String.valueOf(objs[7]));
        	dcserver.setCreDate((Date)objs[8]);
        	newlist.set(i, dcserver);
        }
		return page;
	}
	@Override
	public void addDcServerModel(DcServerModel model) throws Exception {
		dcServerModelDao.saveEntity(model);
	}
	@Override
	public int deleteDcServerModels(String ids) throws Exception {
		StringBuffer sql = new StringBuffer();
		sql.append("delete from dc_server_model where id in (?) ");
		Query query = dcServerModelDao.createSQLNativeQuery(sql.toString(),ids);
		return query.executeUpdate();
	}
	@Override
	public void updateDcServerModel(DcServerModel model) throws Exception {
		dcServerModelDao.saveOrUpdate(model); 
	}
	@Override
	public DcServerModel getById(String id) {
		return dcServerModelDao.findOne(id);
	}
	@SuppressWarnings("rawtypes")
    @Override
	public int getCountByHql(String sql, List params) throws Exception {
		return ((Number)dcServerModelDao.createSQLNativeQuery(sql, params.toArray()).getSingleResult()).intValue();
	}
	@SuppressWarnings("rawtypes")
    @Override
	public List queryByHql(String hql, List params) {
		return dcServerModelDao.createSQLNativeQuery(hql, params).getResultList();
	}
	@Override
	public List<DcServerModel> checkByName(String name) throws Exception {
		return dcServerModelDao.queryByName(name);
	}
	@Override
	public List<DcServerModel> checkByNameNoID(String name, String id) throws Exception {
		return dcServerModelDao.queryByNameNoID(name, id);
	}
	@Override
	public List<BaseDcServer> checkUseOrNo(String DcServerModelID) throws Exception {
		return dcServerModelDao.checkUseorNo(DcServerModelID);
	}
}
