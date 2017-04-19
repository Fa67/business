package com.eayun.work.ecmcservice;

import java.util.List;
import java.util.Map;

import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.support.Page;
import com.eayun.customer.model.User;
import com.eayun.ecmcuser.model.EcmcSysUser;
import com.eayun.sys.model.SysDataTree;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.work.model.WorkOpinion;
import com.eayun.work.model.WorkReport;
import com.eayun.work.model.Workorder;

public interface EcmcWorkorderService {
	/**
	 * 查询工单类别
	 * @param parentId
	 * @return
	 */
	public List<SysDataTree> getDataTree(String parentId);
	/**
	 * 查询未完成工单的状态
	 * @return
	 */
	public List<SysDataTree> getNoDoneFlagList();
	/**
	 * 查询已完成工单的状态
	 * @return
	 */
	public List<SysDataTree> getDoneFlagList();
	/**
	 * 查询工单的状态
	 * @return
	 */
	public List<SysDataTree> getWorkFlagList();
	/**
	 * 查询所有责任人
	 * @return
	 */
	public List<EcmcSysUser> getWorkHeadList(String type,String parentId);
	
	/**
	 * 添加普通工单
	 * @param workorder
	 * @return
	 */
	public Workorder addWorkorder(Workorder workorder) throws Exception;
	/**
	 * 受理工单
	 * @param map
	 * @return
	 */
	public Workorder acceptanceWork(Map<String,String> map) throws Exception;
	
	/**
	 * 根据id查询工单
	 * @param workId
	 * @return
	 */
	public Workorder findWorkByWorkId(String workId);
	/**
	 * 根据工单id查询回复
	 * @return
	 */
	public List<WorkOpinion> getWorkOpinionList(String workId);
	/**
	 * 获取所有未完成工单
	 * @return
	 */
	public Page getNotDoneWorkList(Page page,ParamsMap paramsMap);
	/**
	 * 获取所有已完成工单
	 * @return
	 */
	public Page getDoneWorkList(Page page,ParamsMap paramsMap);
	/**
	 * 编辑工单标题和工单内容
	 * @param map
	 * @return
	 */
	public Workorder updateEcmcWorkorder(Map<String,String> map) throws Exception;
	/**
	 * 修改工单级别
	 * @param map
	 * @return
	 */
	public Workorder updateEcmcWorkForWorkLevel(Map<String,String> map) throws Exception;
	/**
	 * 添加回复
	 * @param workOpinion
	 * @return
	 */
	public WorkOpinion addEcmcWorkopinion(WorkOpinion workOpinion) throws Exception;
	/**
	 * 求助或者指派
	 * @param map
	 * @return
	 */
	public Workorder trunToOtherUser(Map<String,String> map) throws Exception;
	/**
	 * 审核通过
	 * @param map
	 * @return
	 */
	public Workorder auditPassWork(Map<String,String> map) throws Exception;
	/**
	 * 审核不通过
	 * @param map
	 * @return
	 */
	public Workorder auditNotPassWork(Map<String,String> map) throws Exception;
	/**
	 * 统计所有管理员和运维人员的已完成工单
	 * @param map
	 * @return
	 */
	public List<WorkReport> countAllUserAcceptWorkorder(Map<String,String> map);
	/**
	 * 统计指定管理员或者运维的已完成工单
	 * @param map
	 * @return
	 */
	public List<WorkReport> countUserAcceptWorkorder(Map<String,String> map);
	/**
	 * 获取ecmc管理员和运维
	 * @param userNam
	 * @return
	 */
	public List<EcmcSysUser> getEcmcAdminAndCpis(String userNam);
	/**
	 * 获取ecmc有接受未按时受理和响应的工单时接受邮件的用户
	 * @return
	 */
	public List<EcmcSysUser> getEcmcAdmin();
	/**
	 * 获取可被求助的工程师
	 */
	public List<EcmcSysUser> getEcmcAdminAndCpis();
	/**
	 * 获取所有待处理和未响应的工单
	 * @return
	 */
	public List<Workorder> getWorkorderListByFlag();
	/**
	 * 获取工单待处理条数
	 * @return
	 */
	public int getWorkCountForFlag(Map<String,String> map);
	/**
	 * 获取客服的工单状态
	 * @param workFalg 
	 * @return
	 */
	public List<SysDataTree> getWorkFlagListForOrdinary(String workFalg);
	/**
	 * 修改工单的发送邮件状态
	 * @param workId
	 * @param sendMessage
	 */
	public void editWorkSendMessage(String workId,String sendMessage);

	/**
	 * @param userId
     * @return
     */
	public User findUserByUserid(String userId);
	/**
	 * 获取指定客户下的某种状态的工单总数
	 * flag: -1--所有状态（总数）  0--待处理 1--处理中 2--已解决 3--已完结  4--已取消  5--已删除
	 * @param cusId
	 * @param flag
	 * @return
	 */
	public int countWorkByCusId(String cusId, String flag);

	/**
	 * 根据工单ID获取配额类工单的配额信息
	 * @param workId
	 * @return
     */
	CloudProject getStatisticsByWorkId(String workId);
}
