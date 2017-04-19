package com.eayun.customer.serivce.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.exception.AppException;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.StringUtil;
import com.eayun.customer.dao.CustomerDao;
import com.eayun.customer.model.BaseCustomer;
import com.eayun.customer.model.Customer;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.invoice.service.InvoiceService;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {
    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);
	
	@Autowired
	private CustomerDao customerDao;
	
	@Autowired
	private InvoiceService invoiceService;

	@Override
	@SuppressWarnings("unchecked")
	public List<Customer> getListByCustomer(Customer customer) throws AppException {
		List<String> list = new ArrayList<String>();
		StringBuffer strb = new StringBuffer();
		strb.append("from BaseCustomer where 1=1");
		if(StringUtil.isEmpty(customer.getCusPhone())){
			strb.append(" and cusPhone= ?");
			list.add(customer.getCusPhone());
		}
		if(StringUtil.isEmpty(customer.getCusEmail())){
			strb.append(" and cusEmail= ?");
			list.add(customer.getCusEmail());
		}
		
		if(StringUtil.isEmpty(customer.getParUserId())){
			strb.append(" and parUserId=?");
			list.add(customer.getParUserId());
		}
		if(StringUtil.isEmpty(customer.getCusName())){
			strb.append(" and cusName like ?");
			list.add(customer.getCusName());
		}
		List<BaseCustomer> baseCusList = customerDao.find(strb.toString(), list.toArray());
		List<Customer> cusList = new ArrayList<Customer>();
		for (BaseCustomer baseCustomer : baseCusList) {
			Customer cus = new Customer();
			BeanUtils.copyPropertiesByModel(cus, baseCustomer);
			cusList.add(cus);
		}
		return cusList;
	}

	@Override
	public Customer findCustomerById(String cusId){
	    log.info("获取客户信息");
		BaseCustomer baseCustomer= customerDao.findOne(cusId);
		Customer customer = new Customer();
        if(baseCustomer != null ){
            BeanUtils.copyPropertiesByModel(customer, baseCustomer);
            return customer;
        }else {
            return null;
        }
	}

	@Override
	public Customer findCustomerByName(String cusName) throws AppException {
		log.info("根据客户名称获取客户信息");
		List<BaseCustomer> customers = customerDao.findByCusName(cusName) ;
		BaseCustomer baseCustomer = null ;
		if (customers != null && customers.size() != 0) {
			baseCustomer = customers.get(0) ;
		}
		Customer customer = new Customer();
		if(baseCustomer != null ){
			BeanUtils.copyPropertiesByModel(customer, baseCustomer);
			return customer;
		}else {
			return null;
		}
	}

	@Override
	public List<BaseCustomer> findCustomersByNameKeyword(String keyword) throws Exception {
		return customerDao.findByCusNameKeyWord("%" + keyword + "%") ;
	}

	@Override
	public Customer addCustomer(Customer customer){
	    log.info("添加客户");
		List<Customer> cusList = this.getListByCustomer(customer);
		BaseCustomer baseCustomer= new BaseCustomer();
		if (cusList != null && cusList.size() > 0) {
			customer=null;
			return customer;
		}
		
		customer.setCreatTime(new Date());
		BeanUtils.copyPropertiesByModel(baseCustomer, customer);
		customerDao.saveEntity(baseCustomer);
		//初始化客户可开票金额
		invoiceService.initBillableAmount(baseCustomer.getCusId());
		return customer;
	}

	@Override
	public boolean deleteCustomer(Customer customer){
	    log.info("删除客户信息");
		BaseCustomer baseCustomer = new BaseCustomer();
		BeanUtils.copyPropertiesByModel(baseCustomer, customer);
		customerDao.delete(baseCustomer);
		return true;
	}

	@Override
	public Customer updateCustomer(Customer customer){
	    log.info("更新客户");
		BaseCustomer baseCustomer = new BaseCustomer();
		BeanUtils.copyPropertiesByModel(baseCustomer, customer);
		customerDao.saveOrUpdate(baseCustomer);
		BeanUtils.copyPropertiesByModel(customer, baseCustomer);
		return customer;
	}

    @Override
    public boolean checkCpname(String modelCusid, String cusCpname) {
        int count = customerDao.getCountBycpName(cusCpname, modelCusid);
        
        boolean isSameName = false;
        if(count > 0){
            isSameName = false;
        }else{
            isSameName = true;
        }
        return isSameName;
    }

	@Override
	public BaseCustomer save(BaseCustomer baseCustomer) {
		return customerDao.save(baseCustomer);
	}

	@SuppressWarnings("rawtypes")
    @Override
	public List findBy(String sql, Map<String, Object> map) {
		return customerDao.findBy(sql, map);
	}
	
	public List<BaseCustomer> getAllCustomerList(){
		return customerDao.getAllCustomerList();
	}

	@Override
	public void updateCreditTime(Customer customer) throws Exception {
		log.info("更新客户首次达信用额度时间");
		BaseCustomer baseCustomer = new BaseCustomer();
		BeanUtils.copyProperties(baseCustomer, customer);
		customerDao.merge(baseCustomer);
	}

	@SuppressWarnings("unchecked")
    @Override
	public List<Customer> findNotFreeze() throws Exception {
		StringBuffer sb=new StringBuffer(" from BaseCustomer ");
		sb.append(" where cusFalg=? ");
		sb.append(" and  isBlocked=? ");
		List<Object> list=new ArrayList<Object>();
		list.add('1');
		list.add(false);
		List<BaseCustomer> baseCustomerList=customerDao.find(sb.toString(), list.toArray());
		List<Customer> customerList=new ArrayList<Customer>();
		for (BaseCustomer baseCustomer : baseCustomerList) {
			Customer customer=new Customer();
			BeanUtils.copyPropertiesByModel(customer, baseCustomer);
			customerList.add(customer);
		}
		return customerList;
	}

}
