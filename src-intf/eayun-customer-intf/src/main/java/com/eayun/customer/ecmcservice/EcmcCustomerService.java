package com.eayun.customer.ecmcservice;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.customer.model.BaseCustomer;
import com.eayun.customer.model.Customer;
import com.eayun.customer.model.User;

public interface EcmcCustomerService {
	/**
     * 查询所有开通了obs服务的客户
     */
    public List<Customer> getObsCustomer() throws Exception;
	
	/**
	 * 校验客户名称是否重复
	 * @param customerName 客户名称
	 * @return  true：可用     false：不可用
	 */
	public boolean checkCusOrg(String cusOrg);
	
	/**
	 * 校验客户电话号码是否重复
	 * @param cusPhone 客户电话号码
	 * @return  true：可用     false：不可用
	 */
	public boolean checkCusPhone(String cusPhone, String cusId);
	
	/**
	 * 校验客户邮箱是否重复
	 * @param cusEmail 客户邮箱
	 * @return  true：可用     false：不可用
	 */
	public boolean checkCusEmail(String cusEmail, String cusId);
	
	/**
	 * 校验客户管理账号是否重复
	 * @param cusNumber 客户管理账号
	 * @return  true：可用     false：不可用
	 */
	public boolean checkCusAdmin(String cusNumber);
	
	/**
	 * 校验客户中文名称是否重复
	 * @param cusCpname 客户公司中文名
	 * @return  true：可用     false：不可用
	 */
	public boolean checkCusCpname(String cusCpname, String cusId);
	
	/**
	 * 查询客户列表
	 * @param searchKey 客户名称搜索关键字
	 * @param 客户状态
	 * @param pageNo 页码
	 * @return
	 */
	public Page getCustomerList(String searchKey, QueryMap queryMap, String isBlocked);
	
	/**
	 * 根据ID查询客户（包含管理员账号）
	 * @param customerId
	 * @return
	 * @throws Exception 
	 */
	public Customer getCusWithAdminById(String customerId) throws Exception;
	
	/**
	 * 根据ID查询客户
	 * @param customerId
	 * @return
	 */
	public Customer getCustomerById(String customerId);
	
	/**
	 * 更新客户信息
	 * @param baseCustomer
	 * @return
	 * @throws Exception 
	 */
	public Customer updateCustomer(Customer customer) throws Exception;
	
	/**
	 * 更新客户信息
	 * @param baseCustomer
	 * @param updateForWorkorder 是否注册工单模块创建客户
	 * @return
	 * @throws Exception 
	 */
	public Customer updateCustomer(Customer customer, boolean updateForWorkorder) throws Exception;
	
	/**
	 * 添加客户信息
	 * @param baseCustomer
	 * @return
	 */
	public Customer addCustomer(Customer customer) throws Exception;
	
	/**
	 * 获取客户全部名称，用于客户下拉框选项
	 * @return
	 * @throws Exception 
	 */
	public List<Customer> getAllCustomerOrg() throws Exception;

	/**
	 * 根据客户查询用户
	 * @return
	 * @throws Exception 
	 */
	public List<User> getUserAccountByCusId(String cusId) throws Exception;

	/**
	 * 客户总览
	 * @return
	 */
	public Map<String, Object> getCustomerOverview();
	
	
	/**
	 * 重置客户超级管理员密码
	 * @param cusId
	 * @throws Exception 
	 */
	public void resetCusAdminPass(String cusId) throws Exception;
	/**
	 * 冻结客户
	 * @return
	 */
	public BaseCustomer blockCustomer(String cusId) throws Exception;
	/**
	 * 解冻账户
	 * @param cusId
	 * @throws AppException 
	 */
	public BaseCustomer unblockCustomer(String cusId) throws Exception;
	/*
	 * 保存客户
	 * */
	public BaseCustomer mergeBaseCustomer(BaseCustomer customer) throws AppException;
	
	/**
	 * 修改客户的信用额度
	 * @param cusId 客户ID
	 * @param creditLines 信用额度
	 */
	public BaseCustomer updateCreditLines(String cusId, BigDecimal creditLines);
	
	/**
	 * 获取客户费用报表
	 * @param cusId 客户ID
	 * @return 费用报表分页集合
	 */
	public Page getCusReport(QueryMap queryMap, String cusId);
	
	/**
	 * 获取客户交易记录
	 * @param cusId 客户ID
	 * @return 交易记录分页集合
	 */
	public Page getCusRecords(QueryMap queryMap, String cusId);
	
	/**
	 * 更新到期或欠费资源保留时长
	 * @param customerId 客户ID
	 * @param expireKeepTime 时长（小时）
	 */
	public void updateExpireKeepTime(String customerId, int expireKeepTime);
	
	/**
	 * 三天内到期的资源列表
	 * @param customerId 客户ID
	 * @return 资源集合
	 */
	public Page getExpireResourceList(QueryMap queryMap, String customerId);
	
	/**
	 * 获取工单关闭但未创建的客户数
	 * @return
	 */
	public int getUncreatedCusNum();
	
	/**
	 * 获取工单关闭但未创建的客户列表
	 * @param queryMap 分页参数
	 * @return
	 */
	public Page getUncreatedCusList(QueryMap queryMap);
	/**
	 * 同步ECMC客户冻结状态到缓存中
	 * @return boolean
	 */
	public boolean syncCustomerBlockStatus();
	
	/**
	 * 模糊查询客户关键字的不在黑名单的客户
	 * @return boolean
	 * @throws Exception 
	 */
	public List<Customer> getCustExceptBlackCus (String cusName);
	
	 /**
     * 根据客户名称查询
     * @author liyanchao
     * @return
     */
	public BaseCustomer getCustomerByCusOrg (String cusOrg);
}
