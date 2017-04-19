package com.eayun.physical.ecmcservice;

import java.util.List;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.physical.model.BaseDcServer;
import com.eayun.physical.model.DcServerModel;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年4月5日
 */
public interface EcmcDcServerModelService {

	/**
	 * 查询型号列表
	 * @param serverName 服务器名称
	 * @param pageSize   分页条数
	 * @param pageNumber 当前页
	 * @throws Exception
	 * @return Page
	 */
	public Page queryDcServerModelList(String serverName, Page page, QueryMap querymap)throws Exception;
	
	/**
	 * 批量删除型号
	 * @param ids   id拼接字符串 格式("ID1,ID2,ID3")
	 * @return
	 * @throws Exception
	 */
	public int deleteDcServerModels(String ids)throws Exception;
	
	/**
	 * 编辑型号对象
	 * @param model
	 * @throws Exception
	 */
	public void updateDcServerModel(DcServerModel model)throws Exception;
	
	/**
	 * 根据ID获取单个型号对象
	 * @param id
	 * @return
	 */
	public DcServerModel getById(String id);
	
	/**
	 * 根据型号对象添加
	 * @param model
	 * @throws Exception
	 */
	public void addDcServerModel(DcServerModel model)throws Exception;
	
	/**
	 * 带条件的Hql获取总记录数
	 * @param sql
	 * @param params
	 * @return
	 */
	@SuppressWarnings("rawtypes")
    public int getCountByHql(String hql,List params)throws Exception;
	
	/**
	 * 通过hql条件查找
	 * @param hql
	 * @param params：查找条件
	 * @return
	 */
	@SuppressWarnings("rawtypes")
    public List queryByHql(String hql, List params);
	
	/**
	 * 根据型号ID检查服务器型号是否已经使用
	 * @param DcServerModelID
	 * @return
	 * @throws Exception
	 */
	public List<BaseDcServer> checkUseOrNo(String DcServerModelID)throws Exception;
	
	/**
	 * 根据name判断名称是否存在
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public List<DcServerModel> checkByName(String name) throws Exception;
	
	/**
	 * 判断存储是否存在
	 * @param name
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public List<DcServerModel> checkByNameNoID(String name,String id) throws Exception;
	
}
