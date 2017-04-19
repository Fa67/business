package com.eayun.ecmcapi.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.customer.ecmcservice.EcmcCustomerService;
import com.eayun.customer.model.BaseCustomer;
import com.eayun.customer.model.Customer;
import com.eayun.ecmcapi.dao.EcmcApiBlackListDao;
import com.eayun.ecmcapi.model.ApiBlackList;
import com.eayun.ecmcapi.model.BaseApiBlackList;
import com.eayun.ecmcapi.service.EcmcBlackService;

@Service
@Transactional
public class EcmcBlackServiceImpl implements EcmcBlackService {
	private static final Logger log = LoggerFactory
			.getLogger(EcmcBlackServiceImpl.class);
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private EcmcApiBlackListDao ecmcApiBlackListDao;
	@Autowired
	private EcmcCustomerService ecmcCustomerService;
	

	

	/**
	 * 查询黑名单客户展示一页内容
	 * 
	 * @return page
	 */
	public Page getBlackCustomer(Page page, QueryMap queryMap) {

		List<Object> list = new ArrayList<Object>();

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT cus.cus_org AS cusOrg, b.create_time AS createTime, b.api_id as apiId ");
		sql.append(" FROM api_blacklist b");
		sql.append(" LEFT JOIN sys_selfcustomer cus ON b.api_value = cus.cus_id");
		sql.append(" WHERE 1=1 and b.api_type = 'blackCus' ");
		sql.append(" order by b.create_time desc ");

		page = ecmcApiBlackListDao.pagedNativeQuery(sql.toString(), queryMap,
				list.toArray());
		List listResult = (List) page.getResult();

		for (int i = 0; i < listResult.size(); i++) {
			Object[] objs = (Object[]) listResult.get(i);
			ApiBlackList black = new ApiBlackList();
			black.setCusOrg(String.valueOf(objs[0]));
			black.setCreateTime(DateUtil
					.stringToDate(String.valueOf(objs[1]) == "null" ? ""
							: String.valueOf(objs[1])));
			black.setApiId(String.valueOf(objs[2]));
			listResult.set(i, black);
		}

		return page;
	}

	/**
	 * 查询黑名单IP展示一页内容
	 * 
	 * @return page
	 */
	public Page getBlackIp(Page page, QueryMap queryMap) {

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT b.api_value AS blackIp, b.create_time AS createTime, b.api_id as apiId ");
		sql.append(" FROM api_blacklist b");
		sql.append(" WHERE 1=1 and b.api_type = 'blackIp' ");
		sql.append(" order by b.create_time desc ");

		page = ecmcApiBlackListDao.pagedNativeQuery(sql.toString(), queryMap, null);
		List listResult = (List) page.getResult();

		for (int i = 0; i < listResult.size(); i++) {
			Object[] objs = (Object[]) listResult.get(i);
			ApiBlackList black = new ApiBlackList();
			black.setApiValue(String.valueOf(objs[0]));
			black.setCreateTime(DateUtil
					.stringToDate(String.valueOf(objs[1]) == "null" ? ""
							: String.valueOf(objs[1])));
			black.setApiId(String.valueOf(objs[2]));
			listResult.set(i, black);
		}

		return page;
	}

	/**
	 * 添加黑名单客户
	 * 
	 * @param ApiBlackList
	 * @return boolean
	 * @throws Exception
	 */
	public BaseApiBlackList addBlack(ApiBlackList blackList) throws Exception {
//		boolean flag = false;
		BaseApiBlackList baseBlack = new BaseApiBlackList();
		if ("blackCus".equals(blackList.getApiType())) {
			//根据客户名称查找客户
			String cusOrg = blackList.getCusOrg();
			BaseCustomer customer = ecmcCustomerService.getCustomerByCusOrg(cusOrg);
			
			baseBlack.setApiType(blackList.getApiType());
			baseBlack.setApiValue(customer.getCusId());
			baseBlack.setCreateTime(new Date());
			baseBlack.setMemo("黑名单客户");
			baseBlack = ecmcApiBlackListDao.save(baseBlack);
			jedisUtil.set(
					RedisKey.API_BLACK_BLACKCUS + customer.getCusId(),
					JSONObject.toJSONString(baseBlack));
//			flag = true;

		} else if ("blackIp".equals(blackList.getApiType())) {
			StringBuffer sb = new StringBuffer();

			sb.append(blackList.getIpPartOne() + ".");
			sb.append(blackList.getIpPartTwo() + ".");
			sb.append(blackList.getIpPartThree() + ".");
			sb.append(blackList.getIpPartFour());

			baseBlack.setApiValue(sb.toString());
			baseBlack.setApiType(blackList.getApiType());
			baseBlack.setCreateTime(new Date());
			baseBlack.setMemo("黑名单IP");

			baseBlack = ecmcApiBlackListDao.save(baseBlack);
			jedisUtil.set(RedisKey.API_BLACK_BLACKIP + baseBlack.getApiValue(),
					JSONObject.toJSONString(baseBlack));
//			flag = true;
		}

		return baseBlack;
	}

	/**
	 * 添加黑名单客户
	 * 
	 * @param ApiBlackList
	 * @return boolean
	 * @throws Exception
	 */
	public boolean deleteBlack(String apiId) throws Exception {
		boolean flag = false;
		BaseApiBlackList baseApi = ecmcApiBlackListDao.findOne(apiId);
		String apiType = baseApi.getApiType();
		ecmcApiBlackListDao.delete(baseApi);
		if ("blackCus".equals(apiType)) {

			jedisUtil.delete(RedisKey.API_BLACK_BLACKCUS
					+ baseApi.getApiValue());
			flag = true;
		} else if ("blackIp".equals(apiType)) {

			jedisUtil
					.delete(RedisKey.API_BLACK_BLACKIP + baseApi.getApiValue());
			flag = true;
		}

		return flag;
	}
	
	/**
	 * 根据apiId查询黑名单（blackCus另附加cusOrg）
	 * @param apiId
	 * @return ApiBlackList
	 * @throws Exception
	 */
	public ApiBlackList getApiBlack(String apiId) throws Exception {
		
		BaseApiBlackList baseApi = ecmcApiBlackListDao.findOne(apiId);
		
		ApiBlackList black = new ApiBlackList();
		Customer cus = new Customer();
		
		String apiType = baseApi.getApiType();
		if ("blackCus".equals(apiType)) {
			cus = ecmcCustomerService.getCustomerById(baseApi.getApiValue());
			black.setCusOrg(cus.getCusOrg());
		}
		if(null != baseApi){
			BeanUtils.copyPropertiesByModel(black, baseApi);
		}
		return black;
	}

	/**
	 * 同步ECMC黑名单客户与IP到缓存中
	 * @return boolean
	 * @throws Exception 
	 */
	public boolean synchronizeBlack() throws Exception {
		
		boolean flag = syncBlackCus("blackCus");
		boolean vag = syncBlackCus("blackIp");
		if(flag && vag){
			return true;
		}else{
			return false;
		}
	
	}

	private boolean syncBlackCus(String apiType) throws Exception {
		boolean flag = false;
		int blackCount = 0;
		Iterator<String> blackKeys = null;
		// 首先删除记录客户黑名单的缓存数据
		if("blackCus".equals(apiType)){
			blackKeys = jedisUtil.keys(RedisKey.API_BLACK_BLACKCUS + "*").iterator();
			//从数据库中分页方式取出客户，每一次取出的客户都去存到缓存中；
			blackCount = ecmcApiBlackListDao.getBlackCusCount();
		}else if("blackIp".equals(apiType)){
			blackKeys = jedisUtil.keys(RedisKey.API_BLACK_BLACKIP + "*").iterator();
			//从数据库中分页方式取出客户，每一次取出的客户都去存到缓存中；
			blackCount = ecmcApiBlackListDao.getBlackIpCount();
			
		}
		
		String black_key = null;
		while (blackKeys.hasNext()) {
			black_key = (String) blackKeys.next();
			jedisUtil.delete(black_key);
		}

		int pageSize = 10;// 每页包含10条
		int pageNumber = 1;// 第几页

		QueryMap queryMap = new QueryMap();
		queryMap.setCURRENT_ROWS_SIZE(pageSize);
		queryMap.setPageNum(pageNumber);

		// 获取总页数
		int totalCount = 0; // 总共分多少页
		if (blackCount % pageSize == 0) {
			totalCount = blackCount / pageSize;
		} else {
			totalCount = blackCount / pageSize + 1;
		}
		
		if(totalCount == 0){
			flag = true;
		}else{
			// 将每页循环（查出来，存入缓存）
			for (int i = 1; i <= totalCount; i++) {
				if (saveBlackListToRedis(i, queryMap, apiType)) {
					flag = true;
				}else{
					flag = false;
				}
			}
		}
		
		return flag;
	}

	// 保存到缓存redis中
	@SuppressWarnings("unused")
	private boolean saveBlackListToRedis(int pageNumber, QueryMap queryMap,String apiType) {
		boolean flag = false;
		queryMap.setPageNum(pageNumber);
		Page page = null;
		StringBuffer sb = new StringBuffer();
		sb.append("from BaseApiBlackList t where t.apiType = '" + apiType + "'");
		page = ecmcApiBlackListDao.pagedQuery(sb.toString(), queryMap, null);
		List<BaseApiBlackList> baseBlackList = (List) page.getResult();
		Iterator<BaseApiBlackList> blackFromMysql = baseBlackList.iterator();

		while (blackFromMysql.hasNext()) {
			BaseApiBlackList black = blackFromMysql.next();
			try {
				if ("blackCus".equals(apiType)) {
					jedisUtil.set(RedisKey.API_BLACK_BLACKCUS + black.getApiValue(),JSONObject.toJSONString(black));
				} else if ("blackIp".equals(apiType)) {
					jedisUtil.set(RedisKey.API_BLACK_BLACKIP + black.getApiValue(),JSONObject.toJSONString(black));
				}

				flag = true;

			} catch (Exception e) {
				log.error(e.getMessage(), e);
				flag = false;
			}
		}
		return flag;
	}
    /**
     * 校验重复IP
     * @param ApiBlackList
	 * @return boolean
	 * @throws Exception
     * */
	public boolean checkBlackIpExist(ApiBlackList blackList) throws Exception{
		StringBuffer sb = new StringBuffer();
		sb.append(blackList.getIpPartOne() + ".");
		sb.append(blackList.getIpPartTwo() + ".");
		sb.append(blackList.getIpPartThree() + ".");
		sb.append(blackList.getIpPartFour());
		String result = jedisUtil.get(RedisKey.API_BLACK_BLACKIP+sb.toString());
		if(null!= result && !"".equals(result)){
			return true;
		}else{
			return false;
		}
	}
	
}
