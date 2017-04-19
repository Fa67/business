package com.eayun.customer.serivce;

import java.util.List;
import java.util.Map;

import com.eayun.common.exception.AppException;
import com.eayun.customer.model.BaseCustomer;
import com.eayun.customer.model.Customer;

public interface CustomerService {
	/**
	 * 根据条件查询客户(customer为空查询全部)
	 * @return
	 * @throws AppException
	 */
	public List<Customer> getListByCustomer(Customer customer) throws AppException;
	/**
	 * 根据id查询指定客户
	 * @param cusId
	 * @return
	 * @throws AppException
	 */
	public Customer findCustomerById(String cusId) throws AppException;
	/**
	 * 根据客户名称查询指定客户
	 * @param cusName
	 * @return
	 * @throws AppException
	 */
	public Customer findCustomerByName(String cusName) throws AppException;

	public List<BaseCustomer> findCustomersByNameKeyword(String keyword) throws Exception ;

	/**
	 * 添加客户
	 * @param customer
	 * @return
	 * @throws AppException
	 */
	public Customer addCustomer(Customer customer) throws AppException;
	/**
	 * 删除客户
	 * @param customer
	 * @return
	 * @throws AppException
	 */
	public boolean deleteCustomer(Customer customer) throws AppException;
	/**
	 * 修改客户
	 * @param customer
	 * @return
	 * @throws AppException
	 */
	public Customer updateCustomer(Customer customer) throws AppException;
	
	public boolean checkCpname(String modelCusid , String cusCpname);
	
	public BaseCustomer save(BaseCustomer baseCustomer);
	
	@SuppressWarnings("rawtypes")
    public List findBy(String sql, Map<String,Object> map);
	
	public List<BaseCustomer> getAllCustomerList();

	void updateCreditTime(Customer customer) throws Exception;
	/**
	 * 获取所有未被冻结的客户信息
	 * @return
	 * @throws Exception
	 */
	public List<Customer> findNotFreeze() throws Exception;
	
	
}
