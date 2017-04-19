package com.eayun.work.service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.eayun.common.exception.AppException;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.work.model.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.customer.model.Customer;
import com.eayun.sys.model.SysDataTree;

public interface WorkorderService {
	/**
	 * 添加普通工单
	 * @param workorder,workFileList
	 * @return
	 */
	public Workorder addWorkorder(Workorder workorder) throws Exception;
	/**
     * 添加配额工单
     * @param workorder,workFileList
     * @return
     */
	public Workorder addQuotaWorkorder(Workorder workorder,WorkQuota workQuota) throws Exception;
	/**
     * 添加注册工单
     * @return
     */
	public Workorder addRegisterWorkorder(Customer customer) throws Exception;
	/**
	 * 获取云指定datatree(eg:工单类型，工单级别等)
	 * @param parentId--父类id
	 * @return
	 */
	public List<SysDataTree> getDataTree(String parentId);
	
	/**
	 * 修改工单
	 * @param workorder
	 * @return
	 */
	public Workorder updateWorkorder(Workorder workorder);
	/**
     * 修改工单状态
     * @param workorder
     * @return
     */
	public Workorder updateWorkorderForFalg(Workorder workorder) throws Exception;
	/**
	 * 删除工单
	 * @param workorder
	 * @return
	 */
	public boolean deleteWorkorder(Workorder workorder);
	/**
	 * 查询工单
	 * @param page
	 * @return
	 */
	public Page getWorkorderList(Page page,ParamsMap paramsMap);
	
	public int unHandleWorkCount(Map<String,String> map);
	
	public List<Workorder> unHandleWorkNum(Map<String,String> map);
	
	
	/**
	 * 根据id查询工单
	 * @param workId
	 * @return
	 */
	public Workorder findWorkorderByWorkId(String workId);
	/**
	 * 新增回复
	 * @return
	 */
	public WorkOpinion addWorkOpinion(Workorder workorder,String content) throws Exception;
	/**
	 * 根据工单id查询回复
	 * @return
	 */
	public List<WorkOpinion> getWorkOpinionList(String workId);
	/**
	 * 投诉
	 * @return
	 */
	public Workorder updateWorkForFc(String workId) throws Exception;
    /**
     * 添加附件
     * @return
     */
    public List<WorkFile> addWorkFile(Iterator<String> itr,MultipartHttpServletRequest request,String userId)throws Exception;
    /**
     * 添加附件
     * @return
     */
    public List<WorkFile> getWorkFileListByWorkId(String workId);
    
    public SessionUserInfo getUserInfo(SessionUserInfo session);

	/**
	 * 查询分析得出不同工单状态的工单信息集合
	 * @param state	对应的工单状态
	 * @return
	 * @throws AppException
     */
	public List<Workorder> getWorkordersByState(WorkOrderState state) throws AppException ;

	/**
	 * 根据工单ID获取配额类工单的配额信息
	 * @param workId
	 * @return
     */
	CloudProject getStatisticsByWorkId(String workId);
}