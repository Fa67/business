package com.eayun.customer.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.customer.model.BaseCustomer;
import com.eayun.customer.model.BaseUser;

public interface CustomerDao extends IRepository<BaseCustomer, String>{
    
    @Query("select count(*) from BaseCustomer t where t.cusCpname = ? and t.cusId <> ?")
    public int getCountBycpName(String cusCpname, String modelCusid);
    
    @Query("select count(*) from BaseCustomer t where t.cusPhone = ? and t.cusFalg in ('0','1') ")
    public int getCountByPhone(String newPhone);
    
    @Query("select count(*) from BaseCustomer t where t.cusEmail = ? and t.cusFalg in ('0','1') ")
    public int getCountByMail(String newMail);
    
    @Query("from BaseCustomer t where t.cusFalg = 1")
    public List<BaseCustomer> getAllCustomerList();
    
    /**
     * 查询所有客户数量
     * @author zengbo
     * @return
     */
    @Query("select count(*) from BaseCustomer t where t.cusFalg = 1 and t.isBlocked is not null and t.cusId in (select a.cusId from BaseUser a where a.isAdmin = 1)")
    public int getAllCount();
    
    /**
     * 根据客户名称查询
     * @author zengbo
     * @return
     */
    public BaseCustomer findByCusOrg(String cusOrg);
    
    /**
     * 根据客户号码查询
     * @author zengbo
     * @return
     */
    @Query("from BaseCustomer where cusPhone= :cusPhone and cusFalg in ('0','1') and (:cusId = null or cusId != :cusId )")
    public List<BaseCustomer> findByCusPhone(@Param("cusPhone")String cusPhone, @Param("cusId")String cusId);
    
    /**
     * 根据客户邮箱查询
     * @author zengbo
     * @return
     */
    @Query("from BaseCustomer where cusEmail= :cusEmail and cusFalg in ('0','1') and (:cusId = null or cusId != :cusId )")
    public List<BaseCustomer> findByCusEmail(@Param("cusEmail")String cusEmail, @Param("cusId")String cusId);
    
    /**
     * 根据客户公司中文名称查询
     * @author zengbo
     * @return
     */
    @Query("from BaseCustomer where cusCpname= :cusCpname and (:cusId = null or cusId != :cusId )")
    public List<BaseCustomer> findByCusCpname(@Param("cusCpname")String cusCpname, @Param("cusId")String cusId);
    
    /**
     * 查询全部客户的ID和组织名称
     * @author zengbo
     * @return
     */
    @Query("select new BaseCustomer(cusId, cusOrg) from BaseCustomer where cusFalg = '1' and cusOrg is not null")
    public List<BaseCustomer> findAllCustomerOrg();

	/**
	 * 根据客户查询用户名称
	 * @author zengbo
	 * @return
	 */
    @Query("select new BaseUser(userId, userAccount, userPhone, userEmail, isMailValid, isPhoneValid) from BaseUser where cusId=?")
	public List<BaseUser> getUserAccountByCusId(String cusId);


    /**
     * 根据客户名称查询所有的客户信息
     * @param cusName
     * @return
     */
    @Query("from BaseCustomer where cusOrg= :cusName")
    public List<BaseCustomer> findByCusName(@Param("cusName")String cusName) ;

    /**
     * 根据客户名称关键字查询出所有的客户信息
     * @param keyWord
     * @return
     */
    @Query("from BaseCustomer where cusOrg like :keyWord")
    public List<BaseCustomer> findByCusNameKeyWord(@Param("keyWord")String keyWord) ;
   
}
