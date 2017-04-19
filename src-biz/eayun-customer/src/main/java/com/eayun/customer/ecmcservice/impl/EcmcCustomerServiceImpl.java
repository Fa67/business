package com.eayun.customer.ecmcservice.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.MD5;
import com.eayun.common.util.StringUtil;
import com.eayun.costcenter.model.MoneyAccount;
import com.eayun.costcenter.service.AccountOverviewService;
import com.eayun.costcenter.service.CostReportService;
import com.eayun.customer.dao.CustomerDao;
import com.eayun.customer.dao.RoleDao;
import com.eayun.customer.dao.RolePowerDao;
import com.eayun.customer.dao.UserDao;
import com.eayun.customer.ecmcservice.EcmcCustomerService;
import com.eayun.customer.model.BaseCustomer;
import com.eayun.customer.model.BasePower;
import com.eayun.customer.model.BaseRole;
import com.eayun.customer.model.BaseRolePower;
import com.eayun.customer.model.BaseUser;
import com.eayun.customer.model.Customer;
import com.eayun.customer.model.User;
import com.eayun.mail.service.MailService;
import com.eayun.sms.service.SMSService;
import com.eayun.virtualization.bean.AboutToExpire;
import com.eayun.virtualization.service.OverviewService;
import com.eayun.work.ecmcservice.EcmcWorkorderService;

@Service
@Transactional
public class EcmcCustomerServiceImpl implements EcmcCustomerService {

	private static final Logger log = LoggerFactory.getLogger(EcmcCustomerServiceImpl.class);

	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private CustomerDao customerDao;
	@Autowired
	private RoleDao roleDao;
	@Autowired
	private RolePowerDao rolePowerDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private SMSService smsService;
	@Autowired
	private MailService mailService;
	@Value(value="#{prop.ecscUrl}")
	private String ecscUrl;
	@Autowired
	private CostReportService costReportService;
	@Autowired
	private OverviewService overviewService;
	@Autowired
	private EcmcWorkorderService ecmcWorkorderService;
	@Autowired
	private AccountOverviewService accountOverviewService;

	
	/**
	 * 查询所有开通了obs服务的客户
	 */
	public List<Customer> getObsCustomer() throws Exception {
		Set<String> keys = jedisUtil.keys(RedisKey.OBSUSER_CUSID+"*");
		List<Customer> list = new ArrayList<Customer>();
		for (String key : keys) {
			String str = jedisUtil.get(key);
			JSONObject jsonObj = JSONObject.parseObject(str);
			String cusId = jsonObj.getString("cusId");
			BaseCustomer bct = this.getCustomerById(cusId);
			if (bct != null && bct.getCusId() != null && bct.getCusId().length() > 0) {
				Customer c = new Customer();
				BeanUtils.copyPropertiesByModel(c, bct);
				list.add(c);
			}

		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
    @Override
	public Map<String, Object> getCustomerOverview() {
		Map<String, Object> cusOverview = new HashMap<String, Object>();
		List<Object> list = null;
		// 当前客户数量
		cusOverview.put("customerUsed", customerDao.getAllCount());
		// 项目数量
		String prjHql = "select count(*) from BaseCloudProject where customerId in (select distinct cusId from BaseCustomer where cusFalg = 1 and isBlocked is not null)";
		long projectCount = (long) customerDao.createQuery(prjHql, null).list().get(0);
		cusOverview.put("projectUsed", projectCount);
		// 已创建云主机数量
		String vmHql = "select count(*) from BaseCloudVm t where t.isVisable = '1' and (t.isDeleted = '0' or t.isDeleted = '2')";
		long vmCount = (long) customerDao.createQuery(vmHql, null).list().get(0);
		cusOverview.put("vmUsed", vmCount);
		// 已创建云硬盘容量
		String volumeHql = "select sum(vol.volSize) from BaseCloudVolume vol where (vol.isVisable='1' or vol.isVisable is null) and (vol.isDeleted = '0' or vol.isDeleted = '2')";
		list = customerDao.createQuery(volumeHql, null).list();
		long volumeCount = (list.get(0)==null ? 0 : (long)list.get(0));
		cusOverview.put("vdiskUsed", volumeCount);
		// 已创建VPC数量
		String vpsHql = "select count(*) from BaseCloudNetwork t where t.routerExternal=0 and t.isVisible = 1";
		long vpsCount = (long) customerDao.createQuery(vpsHql, null).list().get(0);
		cusOverview.put("vpcUsed", vpsCount);
		// 已使用带宽
		String qosHql = "select sum(t.rate) from BaseCloudRoute t";
		list=customerDao.createQuery(qosHql, null).list();
		long qosCount = (list.get(0)==null ? 0 : (long)list.get(0));
		cusOverview.put("bandwidthUsed", qosCount);
		// 已使用公网IP数量
		String fipHql = "select count(*) from BaseCloudFloatIp where isDeleted='0' and isVisable = '1'";
		long fipCount = (long) customerDao.createQuery(fipHql, null).list().get(0);
		cusOverview.put("fipUsed", fipCount);
		// 已创建负载均衡数量
		String poolHql = "select count(*) from BaseCloudLdPool where isVisible = '1'";
		long poolCount = (long) customerDao.createQuery(poolHql, null).list().get(0);
		cusOverview.put("banlancerUsed", poolCount);
		return cusOverview;
	}

	@Override
	public boolean checkCusOrg(String cusOrg) {
		BaseCustomer baseCustomer = customerDao.findByCusOrg(cusOrg);
		return baseCustomer == null ? true : false;
	}
	
	@Override
	public boolean checkCusPhone(String cusPhone, String cusId) {
		List<BaseCustomer> baseCustomerList = customerDao.findByCusPhone(cusPhone, cusId);
		if (baseCustomerList != null && baseCustomerList.size() > 0) {
			return false;
		}
		return true;
	}

	@Override
	public boolean checkCusEmail(String cusEmail, String cusId) {
		List<BaseCustomer> baseCustomerList = customerDao.findByCusEmail(cusEmail, cusId);
		if (baseCustomerList != null && baseCustomerList.size() > 0) {
			return false;
		}
		return true;
	}

	@Override
	public boolean checkCusAdmin(String cusNumber) {
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("SELECT * ");
		sqlBuffer.append("FROM sys_selfuser t ");
		sqlBuffer.append("WHERE LOWER(t.user_account) = ?");
		List<String> params = new ArrayList<String>();
		params.add(cusNumber);
		List<Object[]> result = customerDao.createSQLNativeQuery(sqlBuffer.toString(), params.toArray()).getResultList();
		if (result != null && result.size() > 0) {
			return false;
		}
		return true;
	}

	@Override
	public boolean checkCusCpname(String cusCpname, String cusId) {
		List<BaseCustomer> baseCustomerList = customerDao.findByCusCpname(cusCpname, cusId);
		if (baseCustomerList != null && baseCustomerList.size() > 0) {
			return false;
		}
		return true;
	}

	@Override
	public Page getCustomerList(String searchKey, QueryMap queryMap, String isBlocked) {
		StringBuffer sqlBuffer = new StringBuffer();
		List<String> params = new ArrayList<>();
		// 拼接SQL
		sqlBuffer.append("SELECT * from (");
		sqlBuffer.append("SELECT cus.cus_id as cusId,"); // 客户ID
		sqlBuffer.append(" cus.cus_org as cusOrg,"); // 客户姓名
		sqlBuffer.append(" ss.user_account as adminAccount,"); // 客户管理员账号（关联sys_user）
		sqlBuffer.append(" cus.cus_phone as cusPhone,"); // 客户号码
		sqlBuffer.append(" count(distinct cp.prj_id) as prjCount,"); // 客户项目数
		sqlBuffer.append(" count(distinct me.am_id) as alarmCount,"); // 客户告警数
		sqlBuffer.append(" cus.cus_name as cusName, "); // 联系人姓名
		sqlBuffer.append(" cus.is_blocked as isBlocked, "); // 冻结标识
		sqlBuffer.append(" if(cus.cus_type='',null,cus.cus_type )as custype, ");//客户类型	
		sqlBuffer.append(" cus.credit_lines as credit_lines,");//信用
		sqlBuffer.append(" ma.mon_money as money");//余额
		sqlBuffer.append(" FROM sys_selfcustomer AS cus");
		sqlBuffer.append(" LEFT JOIN ecmc_alarmmessage AS me ON cus.cus_id = me.cus_id and me.am_isprocessed='0'");
		sqlBuffer.append(" LEFT JOIN cloud_project AS cp ON cp.customer_id = cus.cus_id");
		sqlBuffer.append(" LEFT JOIN sys_selfuser AS ss ON ss.cus_id = cus.cus_id");
		sqlBuffer.append(" LEFT JOIN money_account AS ma ON ma.mon_cusid = cus.cus_id");
		sqlBuffer.append(" WHERE cus.cus_falg = 1 AND ss.is_admin = 1 and cus.is_blocked is not null ");
		// 如果searchKey不为空，则模糊查询
		if (!StringUtil.isEmpty(searchKey)) {
			sqlBuffer.append(" AND cus.cus_org LIKE ? ESCAPE '/'");
			params.add("%" + escapeSpecialChar(searchKey) + "%");
		}
		if(!StringUtil.isEmpty(isBlocked)){
			sqlBuffer.append(" AND cus.is_blocked = ?");
			params.add(isBlocked);
		}
		sqlBuffer.append(" GROUP BY cus.cus_id ORDER BY cus.creat_time DESC ");
		sqlBuffer.append(") t ");
		Page page = customerDao.pagedNativeQuery(sqlBuffer.toString(), queryMap, params.toArray());
		@SuppressWarnings("unchecked")
		List<Object[]> pageResult = (List<Object[]>)page.getResult();
		List<Customer> newPageResult = new ArrayList<Customer>();
		for (Object[] objects : pageResult) {
			Customer customer = new Customer();
			customer.setCusId(ObjectUtils.toString(objects[0], null));
			customer.setCusOrg(ObjectUtils.toString(objects[1], null));
			customer.setAdminAccount(ObjectUtils.toString(objects[2], null));
			customer.setCusPhone(ObjectUtils.toString(objects[3], null));
			customer.setPrjCount(Integer.parseInt(ObjectUtils.toString(objects[4], "0")));
			customer.setAlarmCount(Integer.parseInt(ObjectUtils.toString(objects[5], "0")));
			customer.setCusName(ObjectUtils.toString(objects[6], null));
			customer.setIsBlocked(StringUtils.equals(ObjectUtils.toString(objects[7], "0"), "1"));
			customer.setCus_type(com.eayun.common.constant.CustomerType.getName(ObjectUtils.toString(objects[8], "")));
			customer.setCreditLines(new BigDecimal(formatTwo(ObjectUtils.toString(objects[9], "0.00"))));
			customer.setBalance(new BigDecimal(formatTwo(ObjectUtils.toString(objects[10], "0.00"))));
			newPageResult.add(customer);
		}
		page.setResult(newPageResult);
		return page;
	}
	
	private String formatTwo(String num){
		if(num.indexOf(".")==-1){
			num=num+".000";
		}
		return num.substring(0,num.indexOf(".")+3);
	}

	@SuppressWarnings("unchecked")
    @Override
	public Customer getCusWithAdminById(String customerId) throws Exception {
		try {
			StringBuffer sqlBuffer = new StringBuffer();
			sqlBuffer.append("SELECT c.cus_id, ");
			sqlBuffer.append("c.cus_name, ");
			sqlBuffer.append("c.cus_cpname, ");
			sqlBuffer.append("c.cus_cpename, ");
			sqlBuffer.append("c.cus_sex, ");
			sqlBuffer.append("c.cus_phone, ");
			sqlBuffer.append("c.cus_email, ");
			sqlBuffer.append("c.cus_dept, ");
			sqlBuffer.append("c.cus_pesition, ");
			sqlBuffer.append("c.cus_desc, ");
			sqlBuffer.append("c.creat_time, ");
			sqlBuffer.append("c.cus_falg, ");
			sqlBuffer.append("c.cus_reason, ");
			sqlBuffer.append("c.cus_synopsis, ");
			sqlBuffer.append("c.par_user_id, ");
			sqlBuffer.append("c.cus_number, ");
			sqlBuffer.append("c.cus_org, ");
			sqlBuffer.append("u.user_account, ");
			sqlBuffer.append("c.is_blocked, ");
			sqlBuffer.append("c.block_opstatus, ");
			sqlBuffer.append("c.credit_lines, ");
			sqlBuffer.append(" if(c.cus_type='',null,c.cus_type) as cus_type ");
			sqlBuffer.append("FROM sys_selfcustomer c ");
			sqlBuffer.append("LEFT JOIN sys_selfuser u ON c.cus_id = u.cus_id ");
			sqlBuffer.append("WHERE u.is_admin = 1 ");
			sqlBuffer.append("AND c.cus_id = ? ");
			List<String> params = new ArrayList<String>();
			params.add(customerId);
			List<Object[]> result = customerDao.createSQLNativeQuery(sqlBuffer.toString(), params.toArray()).getResultList();
			if(result!=null && result.size()>0){
				Customer customer = new Customer();
				Object[] objects = result.get(0);
				customer.setCusId(ObjectUtils.toString(objects[0], null));
				customer.setCusName(ObjectUtils.toString(objects[1], null));
				customer.setCusCpname(ObjectUtils.toString(objects[2], null));
				customer.setCusCpename(ObjectUtils.toString(objects[3], null));
				customer.setCusSex(ObjectUtils.toString(objects[4], null));
				customer.setCusPhone(ObjectUtils.toString(objects[5], null));
				customer.setCusEmail(ObjectUtils.toString(objects[6], null));
				customer.setCusDept(ObjectUtils.toString(objects[7], null));
				customer.setCusPesition(ObjectUtils.toString(objects[8], null));
				customer.setCusDesc(ObjectUtils.toString(objects[9], null));
				customer.setCreatTime(DateUtil.stringToDate(ObjectUtils.toString(objects[10], null)));
				customer.setCusFalg(ObjectUtils.toString(objects[11], null).charAt(0));
				customer.setCusReason(ObjectUtils.toString(objects[12], null));
				customer.setCusSynopsis(ObjectUtils.toString(objects[13], null));
				customer.setParUserId(ObjectUtils.toString(objects[14], null));
				customer.setCusNumber(ObjectUtils.toString(objects[15], null));
				customer.setCusOrg(ObjectUtils.toString(objects[16], null));
				customer.setAdminAccount(ObjectUtils.toString(objects[17], null));
				boolean flag =false;
				if("1".equals(ObjectUtils.toString(objects[18], null))){
					flag =true;
				}
				customer.setIsBlocked(flag);
				boolean vag =false;
				if(null!=objects[19] && "1".equals(ObjectUtils.toString(objects[19], null))){
					vag =true;
					customer.setBlockopStatus(vag);
				}else if(null!=objects[19] && "0".equals(ObjectUtils.toString(objects[19], null))){
					vag =false;
					customer.setBlockopStatus(vag);
				} else{
					customer.setBlockopStatus(null);
				}
				customer.setCreditLines(new BigDecimal(formatTwo(ObjectUtils.toString(objects[20], "0.00"))));
				customer.setWorkorderNum(ecmcWorkorderService.countWorkByCusId(customerId, "-1"));
				customer.setUndealWorkorderNum(ecmcWorkorderService.countWorkByCusId(customerId, "0"));
				customer.setExpireResourceNum(this.getExpireResourceNum(customerId));
				customer.setCus_type(com.eayun.common.constant.CustomerType.getName(ObjectUtils.toString(objects[21], "")));
				customer.setCustypefag(ObjectUtils.toString(objects[21], ""));
				MoneyAccount moneyAccount = accountOverviewService.getAccountBalance(customerId);
				if(moneyAccount != null){
					customer.setBalance(moneyAccount.getMoney());
				}
				return customer;
			}
		} catch (Exception e) {
			throw e;
		}
		return null; 
	}
	
	@Override
	public Customer updateCustomer(Customer customer) throws Exception {
		return updateCustomer(customer, false);
	}
	
	@Override
	public Customer updateCustomer(Customer customer, boolean updateForWorkorder) throws Exception {
		try {
			BaseCustomer baseCustomer = customerDao.findOne(customer.getCusId());
			boolean addSelfUser = false;
			if (baseCustomer != null) {
				addSelfUser = baseCustomer.getCusFalg() == '0' ? false : true;
				User selfUser = this.getSelfUserByCusId(customer.getCusId());
				if (needToUpdate(baseCustomer.getCusOrg(), customer.getCusOrg())) {
					baseCustomer.setCusOrg(customer.getCusOrg());
				}
				if (needToUpdate(baseCustomer.getCusEmail(), customer.getCusEmail())) {
					baseCustomer.setCusEmail(customer.getCusEmail());
				}
				if (needToUpdate(baseCustomer.getCus_type(), customer.getCus_type())){
					baseCustomer.setCus_type(customer.getCus_type());
				}
				if(StringUtil.isEmpty(customer.getCus_type())){
					baseCustomer.setCus_type(null);
				}
				if (needToUpdate(baseCustomer.getCusName(), customer.getCusName())) {
					baseCustomer.setCusName(customer.getCusName());
				}
				if (needToUpdate(baseCustomer.getCusPhone(), customer.getCusPhone())) {
					baseCustomer.setCusPhone(customer.getCusPhone());
				}
				if (needToUpdate(baseCustomer.getCusCpname(), customer.getCusCpname())) {
					baseCustomer.setCusCpname(customer.getCusCpname());
				}
				if (needToUpdate(baseCustomer.getCusCpename(), customer.getCusCpename())) {
					baseCustomer.setCusCpename(customer.getCusCpename());
				}
				if (needToUpdate(baseCustomer.getCusDept(), customer.getCusDept())) {
					baseCustomer.setCusDept(customer.getCusDept());
				}
				if (needToUpdate(baseCustomer.getCusPesition(), customer.getCusPesition())) {
					baseCustomer.setCusPesition(customer.getCusPesition());
				}
				if (needToUpdate(baseCustomer.getCusDesc(), customer.getCusDesc())) {
					baseCustomer.setCusDesc(customer.getCusDesc());
				}
				if (needToUpdate(baseCustomer.getCusSynopsis(), customer.getCusSynopsis())) {
					baseCustomer.setCusSynopsis(customer.getCusSynopsis());
				}
				if (!addSelfUser && needToUpdate(baseCustomer.getCusFalg().toString(), customer.getCusFalg().toString())) {
					baseCustomer.setCusFalg(customer.getCusFalg());
				}
				if (!addSelfUser && needToUpdate(baseCustomer.getCusReason(), customer.getCusReason())) {
					baseCustomer.setCusReason(customer.getCusReason());
				}
				if (baseCustomer.getCusNumber() == null) {
					baseCustomer.setCusNumber(customer.getCusNumber());// 设置客户编号
				}
				if(updateForWorkorder){
					baseCustomer.setCreatTime(new Date());
				}
				baseCustomer = (BaseCustomer) customerDao.merge(baseCustomer);
				BeanUtils.copyPropertiesByModel(customer, baseCustomer);
				if(addSelfUser){
					if (selfUser == null || selfUser.getUserAccount() == null) {
						String roleId = createRoles(baseCustomer.getCusId());
						selfUser.setCusId(customer.getCusId());
						selfUser.setUserAccount(customer.getCusNumber());
						selfUser.setRoleId(roleId);
						selfUser.setCusName(customer.getCusName());
						selfUser.setUserPerson(customer.getCusName());
						selfUser.setUserEmail(customer.getCusEmail());
						selfUser.setUserPhone(customer.getCusPhone());
						selfUser.setIsPhoneValid(true);
						selfUser.setIsMailValid(false);
						selfUser.setIsAdmin(true);
						selfUser.setCreateTime(new Date());
						selfUser.setUserExplain("超级管理员");
						selfUser.setIsBlocked(false);
						this.addSelfUser(selfUser);
					} else {
						BaseUser baseUser = new BaseUser();
						baseUser.setIsBlocked(false);
						BeanUtils.copyPropertiesByModel(baseUser, selfUser);
						if (needToUpdate(baseUser.getUserEmail(), customer.getCusEmail())) {
							baseUser.setUserEmail(customer.getCusEmail());
							baseUser.setIsMailValid(false);
						}
						if (needToUpdate(baseUser.getUserPhone(), customer.getCusPhone())) {
							baseUser.setUserPhone(customer.getCusPhone());
							baseUser.setIsPhoneValid(false);
						}
						if (needToUpdate(baseUser.getUserPerson(), customer.getCusName())) {
							baseUser.setUserPerson(customer.getCusName());
						}
						userDao.merge(baseUser);
					}
				}
				return customer;
			}
		} catch (Exception e) {
			throw e;
		}
		return null;
	}
	
	/**
	 * 更新客户时判断字段是否更新
	 * @param baseStr
	 * @param str
	 * @return
	 */
	private boolean needToUpdate(String baseStr, String str) {
		return (baseStr == null || (baseStr != null && str != null && !baseStr.equals(str)));
	}
	
	 /**
     * 获取客户超级管理员帐号
     * @param cusId
     * @return
     */
	@SuppressWarnings("unchecked")
	public User getSelfUserByCusId(String cusId) {
		StringBuffer hql = new StringBuffer();
		List<String> params = new ArrayList<String>();
		hql.append("from BaseUser bu where bu.isAdmin=true and bu.cusId=?");// 审核通过，也就是生效的客户
		params.add(cusId);
		List<BaseUser> userList = userDao.find(hql.toString(), params.toArray());
		User selfUser = new User();
		if (userList.size() > 0) {
			BaseUser baseUser = userList.get(0);// 一个客户只能有1个超级管理员帐号
			BeanUtils.copyPropertiesByModel(selfUser, baseUser);
			BaseCustomer baseCus = customerDao.findOne(cusId);
			selfUser.setCusName(baseCus.getCusOrg());
		}
		return selfUser;
	}

	@Override
	public List<Customer> getAllCustomerOrg() throws Exception {
		List<BaseCustomer> baseCustomerList = customerDao.findAllCustomerOrg();
		List<Customer> customers = new ArrayList<Customer>();
		try {
			if (baseCustomerList != null && baseCustomerList.size() > 0) {
				for (BaseCustomer baseCustomer : baseCustomerList) {
					Customer customer = new Customer();
					BeanUtils.copyProperties(customer, baseCustomer);
					customers.add(customer);
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return customers;
	}
	
	 /**
     * 添加客户
     * @param customer
     * @throws Exception 
     */
	@Override
    public Customer addCustomer(Customer customer) throws Exception{
		customer.setCreatTime(new Date());
		customer.setCusFalg('1');
		
		BaseCustomer baseCustomer = new BaseCustomer();
        BeanUtils.copyPropertiesByModel(baseCustomer, customer);
        baseCustomer = customerDao.save(baseCustomer);
        BeanUtils.copyPropertiesByModel(customer, baseCustomer);
		//------添加超级管理员角色
        String roleId = createRoles(baseCustomer.getCusId());

		//------添加超级管理员用户
		User selfUser = new User();
		selfUser.setCusId(customer.getCusId());
		selfUser.setUserAccount(customer.getCusNumber());
		selfUser.setRoleId(roleId);
		selfUser.setCusName(customer.getCusName());
		selfUser.setUserPerson(customer.getCusName());
		selfUser.setUserEmail(customer.getCusEmail());
		selfUser.setUserPhone(customer.getCusPhone());
		selfUser.setIsPhoneValid(true);
		selfUser.setIsMailValid(false);
		selfUser.setIsAdmin(true);
		selfUser.setCreateTime(new Date());
		selfUser.setUserExplain("超级管理员");
		selfUser.setIsBlocked(false);
		this.addSelfUser(selfUser);
		return customer;
    }
    
    @SuppressWarnings("unchecked")
    private String createRoles(String cusId) throws BiffException, IOException{
        String rootRoleId = "";
        List<BasePower> powerList = new ArrayList<BasePower>();
        StringBuffer hql = new StringBuffer("from BasePower");
        powerList = customerDao.find(hql.toString());
        List<List<String>> rolepowList = getData();
        for(int i = 0;i < rolepowList.size();i++){
            BaseRole baseRole = new BaseRole();
            baseRole.setCusId(cusId);
            if(i == 0){
                baseRole.setRoleName("超级管理员"); 
                baseRole.setRoleDesc("超级管理员");
            }
            if(i == 1){
                baseRole.setRoleName("管理员"); 
                baseRole.setRoleDesc("管理员");
            }
            if(i == 2){
                baseRole.setRoleName("普通用户"); 
                baseRole.setRoleDesc("普通用户");
            }
            baseRole = roleDao.save(baseRole);
            if(i == 0){
                rootRoleId = baseRole.getRoleId();
            }
            
            List<String> powers = rolepowList.get(i);
            for(int j = 0;j < powerList.size();j++){
                for(int k = 0;k < powers.size();k++){
                    if(powers.get(k).equals(powerList.get(j).getPowerRoute())){
                        BaseRolePower rolePower = new BaseRolePower();
                        rolePower.setRoleId(baseRole.getRoleId());
                        rolePower.setPowerId(powerList.get(j).getPowerId());
                        rolePowerDao.save(rolePower);
                    }
                }
            }
        }
        return rootRoleId;
    }
    
    @SuppressWarnings("unused")
    private List<List<String>> getData() throws BiffException, IOException{
        String filePath = getClass().getResource("/").getFile().toString();
        InputStream is = new FileInputStream(filePath + "/role-power.xls");
        Workbook rwb = Workbook.getWorkbook(is);
        Sheet sheet = rwb.getSheet(0);
        int row =sheet.getRows();
        int col =sheet.getColumns();
        List<List<String>> rolepowList = new ArrayList<List<String>>();
        for(int i = 0;i<col;i++){
            Cell[] firstcell  = sheet.getColumn(0);
            if(i <= 1) continue;
            Cell[] cell  = sheet.getColumn(i);
            if(cell.length == 0) continue;
            List<String> powList = new ArrayList<String>();
            for (int j = 0; j < cell.length; j++) {
                if(i == 0) continue;
                String RowString  = cell[j].getContents();
                String Route  = firstcell[j].getContents();
                if(RowString.equals("Y")){
                    powList.add(Route);
                }
            }
            rolepowList.add(powList);
        }
        return rolepowList;
    }
    
    /**
     * 添加用户
     * @param selfUser
     * @throws Exception 
     */
    private void addSelfUser(User user) throws Exception{
    	user.setUserAccount(user.getUserAccount());
    	Random random = new Random();
    	int salt=random.nextInt(899999)+100000;//生成一个临时的6位数
    	user.setSalt(String.valueOf(salt));
		int temp=random.nextInt(899999)+100000;//生成一个临时的6位数
		String chars = "abcdefghijklmnopqrstuvwxyz";
		String charTemp="";
		for(int i=0;i<6;i++){
			charTemp+=chars.charAt((int)(Math.random() * 26));
		}
		charTemp+=temp;
		MD5 md5 = new MD5();
		user.setUserPassword(md5.getMD5ofStr(md5.getMD5ofStr(charTemp)+String.valueOf(salt)));
		
        BaseUser baseUser = new BaseUser();
        BeanUtils.copyPropertiesByModel(baseUser, user);
        userDao.save(baseUser);
        //--成功后给客户发送邮件和短信
        List<String> smsList = new ArrayList<String>();
        smsList.add(user.getUserPhone());
        smsService.send("尊敬的客户您好：欢迎使用Eayun公有云服务，您申请的登录账号："+user.getUserAccount()+"已创建成功，密码："+charTemp+"，初次登录管理控制台请修改密码。", smsList);
    }

	@Override
	public List<User> getUserAccountByCusId(String cusId) throws Exception {
		List<BaseUser> baseUserList = customerDao.getUserAccountByCusId(cusId);
		List<User> userList = new ArrayList<User>();
		try {
			if (baseUserList != null && baseUserList.size() > 0) {
				// 根据号码和邮件验证标识符决定号码和邮件是否显示
				for (BaseUser baseUser : baseUserList) {
					if (!baseUser.getIsPhoneValid()) {
						baseUser.setUserPhone(null);
					}
					if (!baseUser.getIsMailValid()) {
						baseUser.setUserEmail(null);
					}
					User user = new User();
					BeanUtils.copyProperties(user, baseUser);
					userList.add(user);
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return userList;
	}

	private String escapeSpecialChar(String str){
		if (StringUtils.isNotBlank(str)) {
			String[] specialChars = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|", "%" };
			for (String key : specialChars) {
				if (str.contains(key)) {
					str = str.replace(key, "/" + key);
				}
			}
		}
		return str;
	}

	@Override
	public Customer getCustomerById(String customerId) {
		BaseCustomer baseCus =  customerDao.findOne(customerId);
		Customer cus = new Customer();
		if(baseCus!=null){
			BeanUtils.copyPropertiesByModel(cus, baseCus);
			//工单总量/待处理量
			cus.setWorkorderNum(ecmcWorkorderService.countWorkByCusId(customerId, "-1"));
			cus.setUndealWorkorderNum(ecmcWorkorderService.countWorkByCusId(customerId, "0"));
		}
		return cus;
	}

	@Override
	public void resetCusAdminPass(String cusId) throws Exception {

		BaseUser baseUser = userDao.findAdminByCusId(cusId);
		Random random = new Random();
		int salt = random.nextInt(899999) + 100000;// 生成一个临时的6位数
		baseUser.setSalt(String.valueOf(salt));
		int temp = random.nextInt(899999) + 100000;// 生成一个临时的6位数
		String chars = "abcdefghijklmnopqrstuvwxyz";
		String charTemp = "";
		for (int i = 0; i < 6; i++) {
			charTemp += chars.charAt((int) (Math.random() * 26));
		}
		charTemp += temp;// 新密码
		MD5 md5 = new MD5();
		baseUser.setUserPassword(md5.getMD5ofStr(md5.getMD5ofStr(charTemp) + String.valueOf(salt)));
		baseUser.setLastTime(null);
		// baseSelfUser.setUserPassword(md5.getMD5ofStr(charTemp));
		userDao.merge(baseUser);
		// --成功后给客户发送邮件和短信
		List<String> mailList = new ArrayList<String>();
		List<String> smsList = new ArrayList<String>();
		if (baseUser.getIsMailValid()) {
			mailList.add(baseUser.getUserEmail());
			log.info(ecscUrl);
//			String ecscUrl = ReadMailHtml.getUrlMap().get("ecscUrl");
			 mailService.send("客户申请重置密码", "欢迎使用Eayun公有云服务，您申请的密码重置功能已受理，重置后【密码】"+charTemp+" ，初次登录<a href=\""+ecscUrl+"\">管理控制台</a>请修改密码。", mailList);
		}
		smsList.add(baseUser.getUserPhone());
		smsService.send("尊敬的客户：您的密码已重置，重置后密码：" + charTemp + " ，初次登录管理控制台请修改密码。", smsList);
	}
	/*
	 * 冻结账户
	 * */
	public BaseCustomer blockCustomer(String cusId) throws Exception{
		BaseCustomer cus =  customerDao.findOne(cusId);
		if(cus.getIsBlocked()){
			return cus;
		}
		//根据cus_id，更新表sys_selfcustomer的字段is_block的值为1
		BaseCustomer baseCus =  customerDao.findOne(cusId);
		baseCus.setIsBlocked(true);
		customerDao.saveOrUpdate(baseCus);
		//根据cus_id，更新表sys_selfuser的字段is_block的值为1
		List<BaseUser> userList = userDao.getUsersByCusId(cusId);
		for(BaseUser user :  userList){
			user.setIsBlocked(true);
			userDao.saveOrUpdate(user);
		}
		return baseCus;
	}
	
	
	 /**
	 * 解冻客户。
	 * @param cusId
	 * @throws AppException 
	 */
	public BaseCustomer unblockCustomer(String cusId) throws Exception{
//		 如果为true，根据cus_id更新客户表sys_selfcustomer以及sys_selfuser的字段is_block的值为0,表示此客户已解冻。
		BaseCustomer baseCus =  customerDao.findOne(cusId);
		baseCus.setIsBlocked(false);
		baseCus.setBlockopStatus(false);
		customerDao.saveOrUpdate(baseCus);
		//根据cus_id，更新表sys_selfuser的字段is_block的值为0
		List<BaseUser> userList = userDao.getUsersByCusId(cusId);
		for(BaseUser user :  userList){
			user.setIsBlocked(false);
			userDao.saveOrUpdate(user);
		}
		return baseCus;
	}

	/*
	 * 保存客户
	 * */
	public BaseCustomer mergeBaseCustomer(BaseCustomer customer) throws AppException{
		BaseCustomer obj = new BaseCustomer();
		try {
			obj = (BaseCustomer) customerDao.merge(customer);
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			return null;
		}
		return obj;
	}

	@Override
	public BaseCustomer updateCreditLines(String cusId, BigDecimal creditLines) {
		try {
			BaseCustomer customer = customerDao.findOne(cusId);
			customer.setCreditLines(creditLines);
			customerDao.saveOrUpdate(customer);
			return customer;
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public Page getCusReport(QueryMap queryMap, String cusId) {
		// TODO 调用费用中心-费用报表接口
//		return costReportService.getReportListPage(Page page, null, null, 0, null, null, null, 0, cusId);
		return null;
	}

	@Override
	public Page getCusRecords(QueryMap queryMap, String cusId) {
		// TODO 调用费用中心-交易记录接口
//		return costReportService.getRecordListPage(Page page, null, null, 0, cusId);
		return null;
	}

	@Override
	public void updateExpireKeepTime(String customerId, int expireKeepTime) {
		try {
			BaseCustomer customer = customerDao.findOne(customerId);
			customer.setExpireKeepTime(expireKeepTime);
			customerDao.saveOrUpdate(customer);
		} catch (Exception e) {
			throw e;
		}
	}
	
	private int getExpireResourceNum(String cusId){
		Date date =  new Date();
		String now = DateUtil.dateToString(date);
		String threeDay = DateUtil.dateToString(DateUtil.addDay(date, new int[]{0,0,3}));
		List<Object> paramList = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" vm.vm_id AS resources_id, vm.vm_name AS resources_name, vm.create_time AS create_time, ");
		sql.append(" vm.end_time AS end_time, '云主机' AS resources_type, vm.prj_id AS prj_id, prj.prj_name AS prj_name ");
		sql.append(" FROM cloud_vm vm ");
		sql.append(" LEFT JOIN cloud_project prj ON vm.prj_id = prj.prj_id ");
		sql.append(" WHERE vm.pay_type = '1' AND vm.is_deleted = '0' AND vm.charge_state = '0' ");
		sql.append(" AND vm.end_time < ? AND vm.end_time >? AND prj.customer_id = ? ");
		paramList.add(threeDay);
		paramList.add(now);
		paramList.add(cusId);
		sql.append(" UNION ");
		sql.append(" SELECT ");
		sql.append(" vol.vol_id AS resources_id, vol.vol_name AS resources_name, vol.create_time AS create_time, ");
		sql.append(" vol.end_time AS end_time, '云硬盘' AS resources_type, vol.prj_id AS prj_id, prj.prj_name AS prj_name ");
		sql.append(" FROM cloud_volume vol ");
		sql.append(" LEFT JOIN cloud_project prj ON vol.prj_id = prj.prj_id ");
		sql.append(" WHERE vol.pay_type = '1' AND vol.is_deleted = '0' AND vol.charge_state = '0' ");
		sql.append(" AND vol.end_time < ? AND vol.end_time >? AND prj.customer_id = ? ");
		paramList.add(threeDay);
		paramList.add(now);
		paramList.add(cusId);
		sql.append(" UNION ");
		sql.append(" SELECT ");
		sql.append(" net.net_id AS resources_id, net.net_name AS resources_name, net.create_time AS create_time, ");
		sql.append(" net.end_time AS end_time, '私有网络' AS resources_type, net.prj_id AS prj_id, prj.prj_name AS prj_name ");
		sql.append(" FROM cloud_network net ");
		sql.append(" LEFT JOIN cloud_project prj ON net.prj_id = prj.prj_id ");
		sql.append(" WHERE net.pay_type = '1' AND net.charge_state = '0' ");
		sql.append(" AND net.end_time < ? AND net.end_time >? AND prj.customer_id = ? ");
		paramList.add(threeDay);
		paramList.add(now);
		paramList.add(cusId);
		sql.append(" UNION ");
		sql.append(" SELECT ");
		sql.append(" pool.pool_id AS resources_id, pool.pool_name AS resources_name, pool.create_time AS create_time, ");
		sql.append(" pool.end_time AS end_time, '负载均衡' AS resources_type, pool.prj_id AS prj_id, prj.prj_name AS prj_name ");
		sql.append(" FROM cloud_ldpool pool ");
		sql.append(" LEFT JOIN cloud_project prj ON pool.prj_id = prj.prj_id ");
		sql.append(" WHERE pool.pay_type = '1' AND pool.charge_state = '0' ");
		sql.append(" AND pool.end_time < ? AND pool.end_time >? AND prj.customer_id = ? ");
		paramList.add(threeDay);
		paramList.add(now);
		paramList.add(cusId);
		sql.append(" UNION ");
		sql.append(" SELECT ");
		sql.append(" flo.flo_id AS resources_id, flo.flo_ip AS resources_name, flo.create_time AS create_time, ");
		sql.append(" flo.end_time AS end_time, '弹性公网IP' AS resources_type, flo.prj_id AS prj_id, prj.prj_name AS prj_name ");
		sql.append(" FROM cloud_floatip flo ");
		sql.append(" LEFT JOIN cloud_project prj ON flo.prj_id = prj.prj_id ");
		sql.append(" WHERE flo.pay_type = '1' AND flo.is_deleted = '0' AND flo.charge_state = '0' ");
		sql.append(" AND flo.end_time < ? AND flo.end_time >? AND prj.customer_id = ? ");
		paramList.add(threeDay);
		paramList.add(now);
		paramList.add(cusId);
		sql.append(" UNION ");
		sql.append(" SELECT ");
		sql.append(" vpn.vpn_id AS resources_id, vpn.vpn_name AS resources_name, vpn.create_time AS create_time, ");
		sql.append(" vpn.end_time AS end_time, 'VPN' AS resources_type, ser.prj_id AS prj_id, prj.prj_name AS prj_name ");
		sql.append(" FROM cloud_vpnconn vpn ");
		sql.append(" JOIN cloud_vpnservice ser ON vpn.vpnservice_id = ser.vpnservice_id ");
		sql.append(" LEFT JOIN cloud_project prj ON ser.prj_id = prj.prj_id ");
		sql.append(" WHERE vpn.pay_type = '1' AND vpn.charge_state = '0' ");
		sql.append(" AND vpn.end_time < ? AND vpn.end_time >? AND prj.customer_id = ? ");
		paramList.add(threeDay);
		paramList.add(now);
		paramList.add(cusId);
		sql.append(" ORDER BY end_time ");
		return customerDao.createSQLNativeQuery(sql.toString(), paramList.toArray()).getResultList().size();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
	public Page getExpireResourceList(QueryMap queryMap, String cusId) {
		Date date =  new Date();
		String now = DateUtil.dateToString(date);
		String threeDay = DateUtil.dateToString(DateUtil.addDay(date, new int[]{0,0,3}));
		List<Object> paramList = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" vm.vm_id AS resources_id, vm.vm_name AS resources_name, vm.create_time AS create_time, ");
		sql.append(" vm.end_time AS end_time, '云主机' AS resources_type, vm.prj_id AS prj_id, prj.prj_name AS prj_name ");
		sql.append(" FROM cloud_vm vm ");
		sql.append(" LEFT JOIN cloud_project prj ON vm.prj_id = prj.prj_id ");
		sql.append(" WHERE vm.pay_type = '1' AND vm.is_deleted = '0' AND vm.charge_state = '0' ");
		sql.append(" AND vm.end_time < ? AND vm.end_time >? AND prj.customer_id = ? ");
		paramList.add(threeDay);
		paramList.add(now);
		paramList.add(cusId);
		sql.append(" UNION ");
		sql.append(" SELECT ");
		sql.append(" vol.vol_id AS resources_id, vol.vol_name AS resources_name, vol.create_time AS create_time, ");
		sql.append(" vol.end_time AS end_time, '云硬盘' AS resources_type, vol.prj_id AS prj_id, prj.prj_name AS prj_name ");
		sql.append(" FROM cloud_volume vol ");
		sql.append(" LEFT JOIN cloud_project prj ON vol.prj_id = prj.prj_id ");
		sql.append(" WHERE vol.pay_type = '1' AND vol.is_deleted = '0' AND vol.charge_state = '0' ");
		sql.append(" AND vol.end_time < ? AND vol.end_time >? AND prj.customer_id = ? ");
		paramList.add(threeDay);
		paramList.add(now);
		paramList.add(cusId);
		sql.append(" UNION ");
		sql.append(" SELECT ");
		sql.append(" net.net_id AS resources_id, net.net_name AS resources_name, net.create_time AS create_time, ");
		sql.append(" net.end_time AS end_time, '私有网络' AS resources_type, net.prj_id AS prj_id, prj.prj_name AS prj_name ");
		sql.append(" FROM cloud_network net ");
		sql.append(" LEFT JOIN cloud_project prj ON net.prj_id = prj.prj_id ");
		sql.append(" WHERE net.pay_type = '1' AND net.charge_state = '0' ");
		sql.append(" AND net.end_time < ? AND net.end_time >? AND prj.customer_id = ? ");
		paramList.add(threeDay);
		paramList.add(now);
		paramList.add(cusId);
		sql.append(" UNION ");
		sql.append(" SELECT ");
		sql.append(" pool.pool_id AS resources_id, pool.pool_name AS resources_name, pool.create_time AS create_time, ");
		sql.append(" pool.end_time AS end_time, '负载均衡' AS resources_type, pool.prj_id AS prj_id, prj.prj_name AS prj_name ");
		sql.append(" FROM cloud_ldpool pool ");
		sql.append(" LEFT JOIN cloud_project prj ON pool.prj_id = prj.prj_id ");
		sql.append(" WHERE pool.pay_type = '1' AND pool.charge_state = '0' ");
		sql.append(" AND pool.end_time < ? AND pool.end_time >? AND prj.customer_id = ? ");
		paramList.add(threeDay);
		paramList.add(now);
		paramList.add(cusId);
		sql.append(" UNION ");
		sql.append(" SELECT ");
		sql.append(" flo.flo_id AS resources_id, flo.flo_ip AS resources_name, flo.create_time AS create_time, ");
		sql.append(" flo.end_time AS end_time, '弹性公网IP' AS resources_type, flo.prj_id AS prj_id, prj.prj_name AS prj_name ");
		sql.append(" FROM cloud_floatip flo ");
		sql.append(" LEFT JOIN cloud_project prj ON flo.prj_id = prj.prj_id ");
		sql.append(" WHERE flo.pay_type = '1' AND flo.is_deleted = '0' AND flo.charge_state = '0' ");
		sql.append(" AND flo.end_time < ? AND flo.end_time >? AND prj.customer_id = ? ");
		paramList.add(threeDay);
		paramList.add(now);
		paramList.add(cusId);
		sql.append(" UNION ");
		sql.append(" SELECT ");
		sql.append(" vpn.vpn_id AS resources_id, vpn.vpn_name AS resources_name, vpn.create_time AS create_time, ");
		sql.append(" vpn.end_time AS end_time, 'VPN' AS resources_type, ser.prj_id AS prj_id, prj.prj_name AS prj_name ");
		sql.append(" FROM cloud_vpnconn vpn ");
		sql.append(" JOIN cloud_vpnservice ser ON vpn.vpnservice_id = ser.vpnservice_id ");
		sql.append(" LEFT JOIN cloud_project prj ON ser.prj_id = prj.prj_id ");
		sql.append(" WHERE vpn.pay_type = '1' AND vpn.charge_state = '0' ");
		sql.append(" AND vpn.end_time < ? AND vpn.end_time >? AND prj.customer_id = ? ");
		paramList.add(threeDay);
		paramList.add(now);
		paramList.add(cusId);
		sql.append(" ORDER BY end_time ");
		Page page = customerDao.pagedNativeQuery(sql.toString(),queryMap,paramList.toArray());
		List resultList = (List)page.getResult();
		for (int i = 0; i < resultList.size(); i++) {
			 Object[] obj = (Object[]) resultList.get(i);
			 AboutToExpire toExpire = new AboutToExpire();
			 toExpire.setResourcesId(ObjectUtils.toString(obj[0], null));
			 toExpire.setResourcesName(ObjectUtils.toString(obj[1], null));
			 toExpire.setCreateTime((Date) obj[2]);
			 toExpire.setEndTime((Date) obj[3]);
			 toExpire.setResourcesType(ObjectUtils.toString(obj[4], null));
			 toExpire.setPrjId(ObjectUtils.toString(obj[5], null));
			 toExpire.setPrjName(ObjectUtils.toString(obj[6], null));
			 resultList.set(i, toExpire);
		 }
		page.setResult(resultList);
		return page;
	}

	@Override
	public int getUncreatedCusNum() {
		String sql = "select * from sys_selfcustomer c left JOIN workorder w on c.cus_id = w.apply_customer where c.cus_falg = '1' and w.flag='3' and c.cus_number is null and c.cus_org is null";
		return customerDao.createSQLNativeQuery(sql, null).getResultList().size();
	}

	@SuppressWarnings("unchecked")
    @Override
	public Page getUncreatedCusList(QueryMap queryMap) {
		String sql = "select w.order_num, w.cre_date, c.cus_name, c.cus_phone, c.cus_email, c.cus_cpname, c.cus_id from sys_selfcustomer c left JOIN workorder w on c.cus_id = w.apply_customer where c.cus_falg = '1' and w.flag='3' and c.cus_number is null and c.cus_org is null";
		Page page = customerDao.pagedNativeQuery(sql, queryMap, new ArrayList<String>().toArray());
		if(page.getResult()!=null){
			List<Object[]> objects = (List<Object[]>) page.getResult();
			List<Customer> customers = new ArrayList<Customer>();
			for (Object[] object : objects) {
				Customer customer = new Customer();
				customer.setWorkorderNo(ObjectUtils.toString(object[0], null));
				customer.setWorkorderCreateTime(DateUtil.stringToDate(ObjectUtils.toString(object[1], null)));
				customer.setCusName(ObjectUtils.toString(object[2], null));
				customer.setCusPhone(ObjectUtils.toString(object[3], null));
				customer.setCusEmail(ObjectUtils.toString(object[4], null));
				customer.setCusCpname(ObjectUtils.toString(object[5], null));
				customer.setCusId(ObjectUtils.toString(object[6], null));
				customers.add(customer);
			}
			page.setResult(customers);
		}
		return page;
	}
	/**
	 * 同步ECMC客户冻结状态到缓存中
	 * @return boolean
	 */
	public boolean syncCustomerBlockStatus(){
		int cusCount = 0;
		try {
			//删除缓存中的所有数据
			Iterator<String> keys = jedisUtil.keys(RedisKey.CUS_BLOCK+"*").iterator();
			String cus_block_key = null;
			while (keys.hasNext()) {
				cus_block_key = (String) keys.next();
				jedisUtil.delete(cus_block_key);
			}
			// 从数据库中分页方式取出客户，每一次取出的客户都去存到缓存中；
			cusCount = customerDao.getAllCount();
			
			int pageSize = 10;//每页包含10条
	        int pageNumber = 1;//第几页
	        
	        QueryMap queryMap=new QueryMap();
	        queryMap.setCURRENT_ROWS_SIZE(pageSize);
	        queryMap.setPageNum(pageNumber);
	        
			//获取总页数
			int totalCount = 0 ; //总共分多少页
			if (cusCount % pageSize == 0){
				totalCount = cusCount / pageSize;
			}else{
				totalCount = cusCount / pageSize + 1;
			}
			//将每页循环（查出来，存入缓存）
			for(int i= 1; i<=totalCount; i++){
				if(!saveCusBlockToRedis(i, queryMap)){
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			return false;
		}
	}
	//保存到缓存redis中
	@SuppressWarnings("unused")
	private boolean saveCusBlockToRedis(int pageNumber,QueryMap queryMap){
		boolean flag = false;
		queryMap.setPageNum(pageNumber);
		Page page = null;
		StringBuffer strb = new StringBuffer();
        strb.append("from BaseCustomer t where t.cusFalg = 1 and t.isBlocked is not null and t.cusId in (select a.cusId from BaseUser a where a.isAdmin = 1)");
        page = userDao.pagedQuery(strb.toString(), queryMap, null);
        List<BaseCustomer> baseCustomerList = (List) page.getResult();
        Iterator<BaseCustomer> cusFromMysql = baseCustomerList.iterator();
        while(cusFromMysql.hasNext()){
        	BaseCustomer cus = cusFromMysql.next();
        	try {
				jedisUtil.set(RedisKey.CUS_BLOCK + cus.getCusId(), JSONObject.toJSONString(cus.getIsBlocked()));
				flag = true;
			} catch (Exception e) {
				log.error(e.getMessage(),e);
				flag = false;
			}
        }
        return flag;
	}
	
	/**
	 * 模糊查询客户关键字的不在黑名单的客户
	 * @return boolean
	 * @throws Exception 
	 */
	public List<Customer> getCustExceptBlackCus(String cusOrg) {
//		以客户表为主表关联api_blacklist表，查出不在黑名单中的客户。
		StringBuffer sb = new StringBuffer();
		List<String> params = new ArrayList<>();
		sb.append("select cus.cus_id as cusId,cus.cus_name as cusName, cus.cus_org as cusOrg  from sys_selfcustomer cus where cus.cus_id not in");
		sb.append(" (select black.api_value  from api_blacklist black where black.api_type = 'blackCus' )");
		sb.append(" and cus.cus_org is not null and cus.cus_falg = '1' ");
		// 如果cusName不为空，则模糊查询
		if (!StringUtil.isEmpty(cusOrg)) {
			sb.append(" AND cus.cus_org LIKE ? ");
			params.add("%" + cusOrg + "%");
		}
		
		sb.append(" ORDER BY convert(cus.cus_org USING gbk) COLLATE gbk_chinese_ci asc ");
		
		javax.persistence.Query query  = customerDao.createSQLNativeQuery(sb.toString(),  params.toArray());
		
		List<Customer> cusList = new ArrayList<Customer>();
		List listResult = query.getResultList();
		for (int i = 0; i < listResult.size(); i++) {
			Object[] objs = (Object[]) listResult.get(i);
			Customer customer = new Customer();
			customer.setCusId(String.valueOf(objs[0]));
			customer.setCusName(String.valueOf(objs[1]));
			customer.setCusOrg(String.valueOf(objs[2]));
			cusList.add(customer);
		}
		
		return cusList;
	}
	 /**
     * 根据客户名称查询
     * @author liyanchao
     * @return
     */
	public BaseCustomer getCustomerByCusOrg (String cusOrg){
		BaseCustomer cus = customerDao.findByCusOrg(cusOrg);
		return cus;
	}
}
