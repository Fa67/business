package com.eayun.common.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.StringUtil;
/**
 * API工具类
 *                       
 * @Filename: ApiUtil.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2016年11月24日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class ApiUtil {
	
	private static final Logger log = LoggerFactory.getLogger(ApiUtil.class);
	
	private static JedisUtil jedisUtil;
	
	/**
	 * 获取数据中心下API开关状态，没有时默认返回true已开启
	 * @param dcId
	 * @return
	 */
	public static boolean getApiStatusUtil(String dcId){
		jedisUtil = JedisUtil.getInstance();
		try {
			String isok = jedisUtil.get(RedisKey.API_SWITCH_STATUS+dcId);
			log.info("此时数据中心："+dcId+"的API开关的状态为："+isok);
			if("0".equals(isok)){
				return false;
			}else{
				return true;
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return true;
	}
	/**
	 * 根据Region标识获取数据中心信息
	 * 返回内容为：{"dcId":"","dcName":""}
	 * @param apiDcCode
	 * @return
	 * @throws Exception 
	 */
	public static JSONObject getDcByCode(String apiDcCode) throws Exception{
		JSONObject dcMap = new JSONObject();
		jedisUtil = JedisUtil.getInstance();
		try {
			String dcStr = jedisUtil.get(RedisKey.API_DC_CODE+apiDcCode);
			log.info("此时Region标识："+apiDcCode+"的数据中心信息为："+dcStr);
			dcMap = JSONObject.parseObject(dcStr);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw e;
		}
		return dcMap;
	}
	
	/**
	 * 根据数据中心id查询数据中心Region标识信息
	 * 返回内容为：{"apiDcCode":"","dcName":""}
	 * @param dcId
	 * @return
	 * @throws Exception 
	 */
	public static JSONObject getDcCodeById(String dcId) throws Exception{
		JSONObject dcMap = new JSONObject();
		jedisUtil = JedisUtil.getInstance();
		try {
			String dcStr = jedisUtil.get(RedisKey.API_DC_DCID+dcId);
			log.info("此时id："+dcId+"的数据中心信息为："+dcStr);
			dcMap = JSONObject.parseObject(dcStr);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw e;
		}
		return dcMap;
	}
	
	
	/**
	 * 根据cusId检查黑名单客户中是否有此记录
	 * @return boolean
	 */
	public static boolean checkBlackCustomer(String cusId) throws Exception{
		log.info("查询黑名单客户中是否包含客户ID为：         " + cusId + "   的客户开始");
		jedisUtil = JedisUtil.getInstance();
		boolean flag = false;
		String str = jedisUtil.get(RedisKey.API_BLACK_BLACKCUS + cusId);
		if (null != str && !"".equals(str)) {
			flag = true;
		}
		log.info("查询黑名单客户中是否包含客户ID为：         " + cusId + "   的客户结束");
		return flag;
	}
	
	/**
	 * 根据访问检查黑名单IP中是否有此记录
	 * @return boolean
	 */
	public static boolean checkBlackIp(String ip) throws Exception{
		log.info("查询黑名单IP中是否包含ip为：         " + ip + "   的名单开始 ");
		jedisUtil = JedisUtil.getInstance();
		boolean flag = false;
		String str = jedisUtil.get(RedisKey.API_BLACK_BLACKIP + ip);
		if (null != str && !"".equals(str)) {
			flag = true;
		}
		log.info("查询黑名单IP中是否包含ip为：         " + ip + "   的名单结束");
		return flag;

	}
	/**
	 * 根据cusId查询客户是否被冻结。
	 * true:被冻结、false：未被冻结。
	 * @return boolean
	 * @throws Exception 
	 */
	public static boolean checkCustomerIsBlock(String cusId) throws Exception{
		jedisUtil = JedisUtil.getInstance();
		boolean flag = false;
		String blockStatus = jedisUtil.get(RedisKey.CUS_BLOCK + cusId);
		if (null != blockStatus && "true".equals(blockStatus)) {
			flag = true;
		}
		return flag;
	}
	

	/**
	 * 根据客户id,版本号,action获取访问限制信息
	 * <br>如果没有该action则返回null
	 * <br>如果有该action则返回对应限制信息
	 * <br>用于api鉴权
	 * @param cusId
	 * @param version
	 * @param actionName
	 * @return
	 * @throws Exception
	 */
	public static JSONObject getRestrictRequestCountByAction(String cusId ,String version ,String actionName) throws Exception{
		jedisUtil = JedisUtil.getInstance();
		String apiRestrictStr=jedisUtil.get(RedisKey.API_REQUEST_COUNT+cusId+":"+ version+":"+actionName);
		if(StringUtil.isEmpty(apiRestrictStr)){//客户未自定义访问次数,获取系统默认次数
			String value=jedisUtil.get(RedisKey.API_REQUEST_COUNT_DEFAULT+version+":"+actionName);
			if(!StringUtil.isEmpty(value)){
				JSONObject json=JSONObject.parseObject(value);
				return json;
			}
			return null;
		}else{
			//获取自定义访问次数
			JSONObject json=JSONObject.parseObject(apiRestrictStr);
			return json;
		}
	}
}
