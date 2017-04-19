package com.eayun.costcenter.service.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.constant.PayType;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.service.SerialNumService;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.StringUtil;
import com.eayun.common.zk.DistributedLockService;
import com.eayun.costcenter.bean.ExcelRecord;
import com.eayun.costcenter.dao.MoneyAccountDao;
import com.eayun.costcenter.dao.MoneyRecordDao;
import com.eayun.costcenter.model.BaseMoneyAccount;
import com.eayun.costcenter.model.BaseMoneyRecord;
import com.eayun.costcenter.model.MoneyAccount;
import com.eayun.costcenter.model.MoneyRecord;
import com.eayun.costcenter.service.AccountOverviewService;
/**
 * 费用中心-账户总览实现类
 * @author xiangyu.cao@eayun.com
 *
 */
@Service
@Transactional
public class AccountOverviewServiceImpl implements AccountOverviewService {
	private static final Logger log = LoggerFactory.getLogger(AccountOverviewServiceImpl.class);
	@Autowired
	private MoneyAccountDao moneyAccountDao;
	@Autowired
	private MoneyRecordDao moneyRecordDao;
	@Autowired
	private DistributedLockService distributedLockService;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private SerialNumService serialNumService;
	@Override
	public MoneyAccount changeBalanceByCharge(MoneyRecord moneyRecord) throws Exception{
		BaseMoneyRecord baseMoneyRecord = genRecordByRefresh(moneyRecord);
		MoneyAccount moneyAccount=changeAccountBalanceByRefresh(baseMoneyRecord);
		mongoTemplate.insert(javaToJson(baseMoneyRecord),MongoCollectionName.LOG_MONEY_SUCCESS);
		return moneyAccount;
	}
	/**---------------------------新增  专为第三方支付购买订单使用      开始-------------------------------**/
	@Override
	public MoneyAccount changeBalanceForThird(MoneyRecord moneyRecord) throws Exception{
		BaseMoneyRecord baseMoneyRecord = genRecordByRefresh(moneyRecord);
		MoneyAccount moneyAccount=changeAccountBalanceByRefresh(baseMoneyRecord);
		mongoTemplate.insert(javaToJson(baseMoneyRecord),MongoCollectionName.LOG_MONEY_SUCCESS);
		return moneyAccount;
	}
	/**---------------------------新增  专为第三方支付购买订单使用      结束-------------------------------**/
	private MoneyAccount changeAccountBalanceByRefresh(BaseMoneyRecord baseMoneyRecord){
		baseMoneyRecord=(BaseMoneyRecord)moneyRecordDao.saveEntity(baseMoneyRecord);
		MoneyAccount moneyAccount=getAccountInfoByRefresh(baseMoneyRecord.getCusId());
		BigDecimal money=moneyAccount.getMoney();
		BaseMoneyAccount baseMoneyAccount=new BaseMoneyAccount();
		baseMoneyAccount.setCusId(moneyAccount.getCusId());
		baseMoneyAccount.setMonId(moneyAccount.getMonId());
		if(baseMoneyRecord.getIncomeType().equals("1")){
			baseMoneyAccount.setMoney(moneyAccount.getMoney().add(baseMoneyRecord.getMoney()));
		}else{
			baseMoneyAccount.setMoney(moneyAccount.getMoney().subtract(baseMoneyRecord.getMoney()));
		}
		updateAccountInfo(baseMoneyRecord,baseMoneyAccount);
		BeanUtils.copyPropertiesByModel(moneyAccount, baseMoneyAccount);
		if(money.compareTo(BigDecimal.ZERO)>0&&baseMoneyAccount.getMoney().compareTo(BigDecimal.ZERO)<0){
			moneyAccount.setSendMessage(true);
		}
		return moneyAccount;
	}
	
	private MoneyAccount getAccountInfoByRefresh(String cusId) {
		List<BaseMoneyAccount> baseMoneyAccountList=moneyAccountDao.find("from BaseMoneyAccount where cusId=?", cusId);
		MoneyAccount moneyAccount=new MoneyAccount();
		BaseMoneyAccount baseMoneyAccount=new BaseMoneyAccount();
		if(baseMoneyAccountList.size()==0){
			BaseMoneyAccount bma=new BaseMoneyAccount();
			bma.setMoney(new BigDecimal(0.000));
			bma.setCusId(cusId);
			baseMoneyAccount=addAccountInfo(bma);
		}else{
			baseMoneyAccount=baseMoneyAccountList.get(0);
		}
		moneyAccountDao.refresh(baseMoneyAccount);
		BeanUtils.copyPropertiesByModel(moneyAccount, baseMoneyAccount);
		String money=moneyAccount.getMoney().toString();
		moneyAccount.setMoney(new BigDecimal(money));
		moneyAccount.setBalancePositive(moneyAccount.getMoney().abs());;
		return moneyAccount;
	}

	private void updateAccountInfo(BaseMoneyRecord baseMoneyRecord,
			BaseMoneyAccount baseMoneyAccount) {
		String cusId=baseMoneyAccount.getCusId();
		String symbol="";
		if(baseMoneyRecord.getIncomeType().equals("1")){
			symbol="+";
		}else{
			symbol="-";
		}
		String hql="update BaseMoneyAccount set money=money"+symbol+"? where cusId=?";
		List<Object> params=new ArrayList<Object>();
		params.add(baseMoneyRecord.getMoney());
		params.add(cusId);
		moneyAccountDao.executeUpdate(hql, params.toArray());
	}
	private BaseMoneyRecord genRecordByRefresh(MoneyRecord moneyRecord) throws Exception{
		BaseMoneyRecord baseMoneyRecord=new BaseMoneyRecord();
		baseMoneyRecord.setIncomeType(moneyRecord.getIncomeType());
		baseMoneyRecord.setMonEcmcRemark(moneyRecord.getMonEcmcRemark());
		baseMoneyRecord.setMonEcscRemark(moneyRecord.getMonEcscRemark());
		baseMoneyRecord.setMoney(moneyRecord.getMoney());
		Date exchangeTime=moneyRecord.getMonTime();
		baseMoneyRecord.setMonTime(exchangeTime);
		baseMoneyRecord.setMonPaymonth(formatDate(exchangeTime,"yyyy-MM"));
		baseMoneyRecord.setOperType(moneyRecord.getOperType());
		baseMoneyRecord.setOrderNo(moneyRecord.getOrderNo());
		baseMoneyRecord.setPayType(moneyRecord.getPayType());
		baseMoneyRecord.setProductName(moneyRecord.getProductName());
		baseMoneyRecord.setResourceId(moneyRecord.getResourceId());
		baseMoneyRecord.setResourceName(moneyRecord.getResourceName());
		baseMoneyRecord.setMonStart(moneyRecord.getMonStart());
		baseMoneyRecord.setMonEnd(moneyRecord.getMonEnd());
		baseMoneyRecord.setResourceType(moneyRecord.getResourceType());
		baseMoneyRecord.setCusId(moneyRecord.getCusId());
		baseMoneyRecord.setDcId(moneyRecord.getDcId());
		baseMoneyRecord.setMonConfigure(moneyRecord.getMonConfigure());
		baseMoneyRecord.setMonContract(moneyRecord.getMonContract());
		baseMoneyRecord.setSerialNumber(getSerialNum());
		BigDecimal accountBalance=getAccountInfoByRefresh(moneyRecord.getCusId()).getMoney();
		BigDecimal newAccountBalance=null;
		if(moneyRecord.getIncomeType().equals("1")){
			newAccountBalance=accountBalance.add(moneyRecord.getMoney());
		}else{
			newAccountBalance=accountBalance.subtract(moneyRecord.getMoney());
		}
		log.info("客户"+moneyRecord.getCusId()+",修改金额前余额:"+accountBalance+",修改金额后应为:"+newAccountBalance);
		baseMoneyRecord.setAccountBalance(newAccountBalance);
		if(accountBalance.compareTo(BigDecimal.ZERO)==1){
			if(accountBalance.compareTo(moneyRecord.getMoney())==-1){
				baseMoneyRecord.setMonRealPay(accountBalance);
			}else{
				baseMoneyRecord.setMonRealPay(moneyRecord.getMoney());
			}
		}else{
			baseMoneyRecord.setMonRealPay(new BigDecimal(0));
		}
		if("2".equals(moneyRecord.getPayType())){
			baseMoneyRecord.setPayState(newAccountBalance.compareTo(BigDecimal.ZERO)==-1?"2":"1");
		}
		return baseMoneyRecord;
	}
	
	@Override
	public MoneyAccount changeBalanceByPay(MoneyRecord moneyRecord) throws Exception{
			BaseMoneyRecord baseMoneyRecord =genRecordByRefresh(moneyRecord);
			MoneyAccount moneyAccount=getAccountInfoByRefresh(moneyRecord.getCusId());
			if("3".equals(moneyRecord.getOperType())&&"2".equals(moneyRecord.getIncomeType())){
				BigDecimal accountMoney=moneyAccount.getMoney();
				if(accountMoney.compareTo(moneyRecord.getMoney())==-1){
					moneyAccount.setCanPay(false);
				}
			}
			if(moneyAccount.isCanPay()){
				moneyAccount=changeAccountBalanceByRefresh(baseMoneyRecord);
			}
			mongoTemplate.insert(javaToJson(baseMoneyRecord),MongoCollectionName.LOG_MONEY_SUCCESS);
		return moneyAccount;
	}
	
	private String formatDate(Date date,String pattern){
		SimpleDateFormat sdf=new SimpleDateFormat(pattern);
		return sdf.format(date);
	}
	private BaseMoneyRecord genRecord(MoneyRecord moneyRecord) throws Exception{
		BaseMoneyRecord baseMoneyRecord=new BaseMoneyRecord();
		baseMoneyRecord.setIncomeType(moneyRecord.getIncomeType());
		baseMoneyRecord.setMonEcmcRemark(moneyRecord.getMonEcmcRemark());
		baseMoneyRecord.setMonEcscRemark(moneyRecord.getMonEcscRemark());
		baseMoneyRecord.setMoney(moneyRecord.getMoney());
		Date exchangeTime=moneyRecord.getMonTime();
		baseMoneyRecord.setMonTime(exchangeTime);
		baseMoneyRecord.setMonPaymonth(formatDate(exchangeTime,"yyyy-MM"));
		baseMoneyRecord.setOperType(moneyRecord.getOperType());
		baseMoneyRecord.setOrderNo(moneyRecord.getOrderNo());
		baseMoneyRecord.setPayType(moneyRecord.getPayType());
		baseMoneyRecord.setProductName(moneyRecord.getProductName());
		baseMoneyRecord.setResourceId(moneyRecord.getResourceId());
		baseMoneyRecord.setResourceName(moneyRecord.getResourceName());
		baseMoneyRecord.setMonStart(moneyRecord.getMonStart());
		baseMoneyRecord.setMonEnd(moneyRecord.getMonEnd());
		baseMoneyRecord.setResourceType(moneyRecord.getResourceType());
		baseMoneyRecord.setCusId(moneyRecord.getCusId());
		baseMoneyRecord.setDcId(moneyRecord.getDcId());
		baseMoneyRecord.setMonConfigure(moneyRecord.getMonConfigure());
		baseMoneyRecord.setMonContract(moneyRecord.getMonContract());
		baseMoneyRecord.setSerialNumber(getSerialNum());
		BigDecimal accountBalance=getAccountInfo(moneyRecord.getCusId()).getMoney();
		BigDecimal newAccountBalance=null;
		if(moneyRecord.getIncomeType().equals("1")){
			newAccountBalance=accountBalance.add(moneyRecord.getMoney());
		}else{
			newAccountBalance=accountBalance.subtract(moneyRecord.getMoney());
		}
		log.info("客户"+moneyRecord.getCusId()+",修改金额前余额:"+accountBalance+",修改金额后应为:"+newAccountBalance);
		baseMoneyRecord.setAccountBalance(newAccountBalance);
		if(accountBalance.compareTo(BigDecimal.ZERO)==1){
			if(accountBalance.compareTo(moneyRecord.getMoney())==-1){
				baseMoneyRecord.setMonRealPay(accountBalance);
			}else{
				baseMoneyRecord.setMonRealPay(moneyRecord.getMoney());
			}
		}else{
			baseMoneyRecord.setMonRealPay(new BigDecimal(0));
		}
		if("2".equals(moneyRecord.getPayType())){
			baseMoneyRecord.setPayState(newAccountBalance.compareTo(BigDecimal.ZERO)==-1?"2":"1");
		}
		return baseMoneyRecord;
	}
	private String getSerialNum(){
		Date now =new Date();
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
		String dateStr=sdf.format(now);
		String serialNum=serialNumService.getSerialNum("03"+dateStr, 8);
		return serialNum;
	}
	@Override
	public MoneyAccount getAccountInfo(String cusId) {
		List<BaseMoneyAccount> baseMoneyAccountList=moneyAccountDao.find("from BaseMoneyAccount where cusId=?", cusId);
		MoneyAccount moneyAccount=new MoneyAccount();
		BaseMoneyAccount baseMoneyAccount=new BaseMoneyAccount();
		if(baseMoneyAccountList.size()==0){
			BaseMoneyAccount bma=new BaseMoneyAccount();
			bma.setMoney(new BigDecimal(0.000));
			bma.setCusId(cusId);
			baseMoneyAccount=addAccountInfo(bma);
		}else{
			baseMoneyAccount=baseMoneyAccountList.get(0);
		}
		BeanUtils.copyPropertiesByModel(moneyAccount, baseMoneyAccount);
		String money=moneyAccount.getMoney().toString();
//		money=formatTwo(money);
		moneyAccount.setMoney(new BigDecimal(money));
		moneyAccount.setBalancePositive(moneyAccount.getMoney().abs());;
		return moneyAccount;
	}
	@Override
	public MoneyAccount getAccountBalance(String cusId) {
		MoneyAccount moneyAccount=getAccountInfo(cusId);
		BigDecimal balance=moneyAccount.getMoney();
		moneyAccount.setMoney(new BigDecimal(formatTwo(balance.toString())));
		moneyAccount.setBalancePositive(new BigDecimal(formatTwo(balance.toString())).abs());
		return moneyAccount;
	}
	private String formatTwo(String num){
		if(num.indexOf(".")==-1){
			num=num+".000";
		}
		return num.substring(0,num.indexOf(".")+3);
	}
	private BaseMoneyAccount addAccountInfo(BaseMoneyAccount bma) {
		BaseMoneyAccount baseMoneyAccount=moneyAccountDao.save(bma);
		return baseMoneyAccount;
	}
	
	private MoneyAccount changeAccountBalance(BaseMoneyRecord baseMoneyRecord){
		baseMoneyRecord=(BaseMoneyRecord)moneyRecordDao.saveEntity(baseMoneyRecord);
		MoneyAccount moneyAccount=getAccountInfo(baseMoneyRecord.getCusId());
		BigDecimal money=moneyAccount.getMoney();
		BaseMoneyAccount baseMoneyAccount=new BaseMoneyAccount();
		baseMoneyAccount.setCusId(moneyAccount.getCusId());
		baseMoneyAccount.setMonId(moneyAccount.getMonId());
		if(baseMoneyRecord.getIncomeType().equals("1")){
			baseMoneyAccount.setMoney(moneyAccount.getMoney().add(baseMoneyRecord.getMoney()));
		}else{
			baseMoneyAccount.setMoney(moneyAccount.getMoney().subtract(baseMoneyRecord.getMoney()));
		}
		updateAccountInfo(baseMoneyRecord,moneyAccount.getCusId());
		BeanUtils.copyPropertiesByModel(moneyAccount, baseMoneyAccount);
		if(money.compareTo(BigDecimal.ZERO)>0&&baseMoneyAccount.getMoney().compareTo(BigDecimal.ZERO)<0){
			moneyAccount.setSendMessage(true);
		}
		return moneyAccount;
	}

	private int updateAccountInfo(BaseMoneyRecord baseMoneyRecord,String cusId) {
		String symbol="";
		if(baseMoneyRecord.getIncomeType().equals("1")){
			symbol="+";
		}else{
			symbol="-";
		}
		String hql="update BaseMoneyAccount set money=money"+symbol+"? where cusId=?";
		List<Object> params=new ArrayList<Object>();
		params.add(baseMoneyRecord.getMoney());
		params.add(cusId);
		int num=moneyAccountDao.executeUpdate(hql, params.toArray());
		return num;
	}
	
	@Override
	public Page getRecordPage(Page page, String cusId, Date beginTime,
			Date endTime,String incomeType, QueryMap queryMap) throws Exception {
		List<Object> list = new ArrayList<Object>();
		String hql=genHql(incomeType,cusId,beginTime,endTime,list);
		page=moneyRecordDao.pagedQuery(hql, queryMap, list.toArray());
		List<BaseMoneyRecord> result=(List<BaseMoneyRecord>) page.getResult();
		for (int i =0;i<result.size();i++) {
			BaseMoneyRecord baseMoneyRecord=result.get(i);
			MoneyRecord moneyRecord=new MoneyRecord();
			BeanUtils.copyPropertiesByModel(moneyRecord, baseMoneyRecord);
			moneyRecord.setBalancePositive(moneyRecord.getAccountBalance().abs());
			if(PayType.PAYBEFORE.equals(moneyRecord.getPayType())){
				String money=moneyRecord.getMoney().toString();
				money=formatTwo(money)+"0";
				moneyRecord.setMoney(new BigDecimal(money));
			}
			result.set(i, moneyRecord);
		}
		return page;
	}

	private String genHql(String incomeType,String cusId, Date beginTime, Date endTime,List<Object> list) {
		StringBuffer sb=new StringBuffer("from BaseMoneyRecord ");
		sb.append(" where cusId=? ");
		list.add(cusId);
		if(beginTime!=null){
			sb.append(" and monTime>=? ");
			list.add(beginTime);
		}
		if(endTime!=null){
			sb.append(" and monTime<? ");
			list.add(endTime);
		}
		if(incomeType!=null&&incomeType.length()>0){
			sb.append(" and incomeType=?");
			list.add(incomeType);
		}
		sb.append(" order by serialNumber desc");
		return sb.toString();
	}

	@Override
	public List<ExcelRecord> queryRecordExcel(String incomeType, Date begin, Date end, String cusId, boolean isEcmc)
			throws Exception {
	    SimpleDateFormat formatExcelDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		List<Object> list = new ArrayList<Object>();
		String hql=genHql(incomeType,cusId,begin,end,list);
		List<BaseMoneyRecord> baseMoneyRecordList=moneyRecordDao.find(hql, list.toArray());
		List<ExcelRecord> excelList=new ArrayList<ExcelRecord>();
		for (BaseMoneyRecord baseMoneyRecord : baseMoneyRecordList) {
			ExcelRecord excel=new ExcelRecord();
			String time=formatExcelDate.format(baseMoneyRecord.getMonTime());
			excel.setMonTime(time);
			
			excel.setSerialNumber(baseMoneyRecord.getSerialNumber());
			excel.setIncomeType("1".equals(baseMoneyRecord.getIncomeType())?"收入":"支出");
			String remark=null;
			if(isEcmc){
				remark = baseMoneyRecord.getMonEcmcRemark();
			}else{
				remark = baseMoneyRecord.getMonEcscRemark();
			}
			remark=remark.replace("<br>", " ");
			if("2".equals(baseMoneyRecord.getOperType())){
				remark=remark+" 订单号："+baseMoneyRecord.getOrderNo();
			}
			if(isEcmc && ("4".equals(baseMoneyRecord.getOperType()) || "5".equals(baseMoneyRecord.getOperType()))){
				remark=remark+" 合同号："+baseMoneyRecord.getMonContract();
			}
			excel.setMonEcscRemark(remark);
			if("1".equals(baseMoneyRecord.getIncomeType())){
				excel.setMoney("￥ "+baseMoneyRecord.getMoney());
			}else{
				excel.setMoney("￥ -"+baseMoneyRecord.getMoney());
			}
			BigDecimal number=baseMoneyRecord.getAccountBalance();
			if(number.compareTo(BigDecimal.ZERO)==-1){
				excel.setBalance("￥ "+number);
			}else{
				excel.setBalance("￥ "+number);
			}
			excelList.add(excel);
		}
		return excelList;
	}
	private String formatThree(BigDecimal number) {
		number=number.compareTo(BigDecimal.ZERO)==-1?number.multiply(new BigDecimal(-1)):number;
		number=number.setScale(2, BigDecimal.ROUND_HALF_UP);
		DecimalFormat df = new DecimalFormat("0.000");
		String result=df.format(number.doubleValue());
		return result;
	}

	@Override
	public boolean thisOrderIsRefunded(String orderNo) throws Exception {
		List<Object> list=new ArrayList<Object>();
		list.add("1");
		list.add(orderNo);
		Query query = moneyRecordDao.createSQLNativeQuery("select count(order_no) from money_record where income_type=? and order_no=? ",
				list.toArray());
		Object o = query.getSingleResult();
		int totalCount = 0;
		if (o instanceof BigInteger) {
			totalCount = ((BigInteger) o).intValue();
		}
		if(totalCount>0){
			return true;
		}
		return false;
	}
	private String javaToJson(Object obj){
		JSONObject json=(JSONObject) JSONObject.toJSON(obj);
		json.put("timestamp", new Date());
		return json.toJSONString();
	}
}
