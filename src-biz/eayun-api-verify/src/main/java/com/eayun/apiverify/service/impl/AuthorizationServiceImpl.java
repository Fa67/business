package com.eayun.apiverify.service.impl;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.apiverify.service.AuthorizationService;
import com.eayun.common.api.ApiUtil;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.exception.ApiException;
import com.eayun.common.constant.ApiConstant;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.HmacSHA1Util;
import com.eayun.common.util.StringUtil;
@Service
public class AuthorizationServiceImpl implements AuthorizationService{
	
	private static final Logger log = LoggerFactory
			.getLogger(AuthorizationServiceImpl.class);
	
	@Autowired
	private JedisUtil jedisUtil;
	
	@Override
	public JSONObject checkAuthorization(JSONObject params, String contentType,
			String ip, String version) throws Exception {
		JSONObject json=new JSONObject();
		try {
			SimpleDateFormat FORMAT=new SimpleDateFormat("yyyyMMddHH");
			SimpleDateFormat UTC_FORMAT=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			//判断body是否为空
			if(params==null){
				throw ApiException.createApiException(ApiConstant.REQUEST_BODY_FORMAT_ERROR_CODE); //"Message", "BadRequest:Request body section format error"
			}
			//accesskey是否为空
			String accessKey=params.getString("AccessKey");
			boolean akIsNull=false;
			if(StringUtil.isEmpty(accessKey)){
				akIsNull=true;
			}
			
			//校验accesskey是否存在
			boolean akIsNotExist=false;
			String cusId="";
			String akJsonObj="";
			if(!akIsNull){
				akJsonObj=jedisUtil.get(RedisKey.AK_ACCESSKEY+accessKey);
				if(StringUtil.isEmpty(akJsonObj)){
					akIsNotExist=true;
				}
			}else{
				akIsNotExist=true;
			}
			
			JSONObject akJson=new JSONObject();
			if(!akIsNotExist){
				akJson=JSONObject.parseObject(akJsonObj);//获取ak对象
				//校验cusId是否为空
				if(!StringUtil.isEmpty(akJson.getString("userId"))){
					cusId=akJson.getString("userId");
					json.put(ApiConstant.TAG_CUS_ID, cusId);
				}
			}
			//判断region是否为空
			String region=params.getString("Region");
			boolean reginIsNull=false;
			if(StringUtil.isEmpty(region)){
				reginIsNull=true;
			}
			boolean reginFormatError=false;
			if(!reginIsNull){
				//判断region是否格式正确
				String reg="(?!-)(?!.*?-$)[a-z0-9-]{1,20}";		//数字小写字母横线 不允许横线开头和结尾  最长20位
				Pattern pattern = Pattern.compile(reg);
				Matcher matcher = pattern.matcher(region);
				boolean rs = matcher.matches();
				if(!rs){
					reginFormatError=true;
				}
			}else{
				reginFormatError=true;
			}
			boolean reginNotExist=false;
			JSONObject regionJson=new JSONObject();
			if(!reginFormatError){
				//根据数据中心名称查询数据中心
				regionJson=ApiUtil.getDcByCode(region);
				//数据中心是否存在
				if(regionJson==null||StringUtil.isEmpty(regionJson.getString("dcId"))){
					reginNotExist=true;
				}else{
					json.put(ApiConstant.TAG_DC_ID, regionJson.getString("dcId"));
				}
			}
			
			//判断Action是否存在 
			String action=params.getString("Action");
			boolean actionIsNull=false;
			//Action是否为空
			if(StringUtil.isEmpty(action)){
				actionIsNull=true;
			}
			boolean actionNotExist=false;
			JSONObject restrict=new JSONObject();
			if(!actionIsNull){
				restrict=ApiUtil.getRestrictRequestCountByAction(cusId, version, action);	//获取限制的信息
				if(restrict==null){	//不存在该action 
					actionNotExist=true;
				}else{
					json.put(ApiConstant.TAG_API_NAME,restrict.getString("actionName"));
					json.put(ApiConstant.TAG_RESOURCE_TYPE,restrict.getString("apiTypeName"));
				}
			}
			if(akIsNull){//ak参数为null
				throw ApiException.createApiException(ApiConstant.NOT_SET_ACCESSKEY_ERROR_CODE,json);//Message", "BadRequest:Not set AccessKey"
			}
			if(akIsNotExist){//accesskey不存在
				throw ApiException.createApiException(ApiConstant.ACCESSKEY_NOT_EXIST_ERROR_CODE,json);//"Message", "ItemNotFound:AccessKey does not exist"
			}
			if(reginIsNull){//region为空
				throw ApiException.createApiException(ApiConstant.NOT_SET_REGION_ERROR_CODE,json);	//"BadRequest:Not set Region"
			}
			if(reginFormatError){//region格式不正确
				throw ApiException.createApiException(ApiConstant.REGION_FORMAT_ERROR_CODE,json);	//"BadRequest:Region format error"
			}
			if(reginNotExist){//数据中心不存在
				throw ApiException.createApiException(ApiConstant.REGION_NOT_EXIST_ERROR_CODE,json);	//"ItemNotFound:Region does not exist"
			}
			if(actionIsNull){//action为null
				throw ApiException.createApiException(ApiConstant.NOT_SET_ACTION_ERROR_CODE,json); //"Message", "BadRequest:Not set Action"
			}
			if(actionNotExist){//不存在action
				throw ApiException.createApiException(ApiConstant.SYSTEM_ACTION_NOT_SET_ERROR_CODE,json);	//"Message", "ItemNotFound:Actiondoes not exist"
			}
			
			boolean isUnlockApi=ApiUtil.getApiStatusUtil(regionJson.getString("dcId"));
			//获取是否开启api
			if(!isUnlockApi){
				throw ApiException.createApiException(ApiConstant.SERVER_MAINTENANCE_ERROR_CODE,json);	//"Forbidden:Server maintenance"
			}
			//ip是否在黑名单
			boolean checkBlackIp=ApiUtil.checkBlackIp(ip);
			if(checkBlackIp){
				throw ApiException.createApiException(ApiConstant.IP_IS_DISABLED,json);	//"Forbidden:Your IP is disabled"
			}
			
			Date now =new Date();
			//去除分钟秒
			String nowStr=FORMAT.format(now); //2016120114
			long count=jedisUtil.increase(RedisKey.REQUEST_COUNT+cusId+":"+version+":"+action+":"+nowStr);	//当前访问次数
			Date nextTime=DateUtil.addDay(FORMAT.parse(nowStr), new int[]{0,0,0,1,5});
			jedisUtil.expireKey(RedisKey.REQUEST_COUNT+cusId+":"+version+":"+action+":"+nowStr, (nextTime.getTime()-now.getTime())/1000);//失效时间为65min
			//校验是否超过请求次数限制
			if(restrict.getIntValue("count")<count){
				throw ApiException.createApiException(ApiConstant.API_REQUEST_QUOTA_ERROR_CODE,json);	//"Quota Exceed:API access times have been used up"
			}
			
			//校验sk不为空
			if(StringUtil.isEmpty(akJson.getString("secretKey"))){
				throw ApiException.createApiException(ApiConstant.UNABLE_FIND_SK_AK_ERROR_CODE,json);  //"ItemNotFound:Unable to find SK through AK"
			}
			//校验ak是否停用
			if("1".equals(akJson.getString("acckState"))){
				throw ApiException.createApiException(ApiConstant.ACCESSKEY_IS_DISABLED_ERROR_CODE,json);	//"Message", "Forbidden:The Access Key is disabled"
			}
			
			//判断Content-Type
			if(!ApiConstant.CONTENT_TYPE.equalsIgnoreCase(contentType)){
				throw ApiException.createApiException(ApiConstant.REQUEST_HEAD_FORMAT_ERROR_CODE,json);	//BadRequest:Request structure Head section format error
			}
			//校验timestamp是否为空
			String timestampStr=params.getString("Timestamp");
			if(StringUtil.isEmpty(timestampStr)){
				throw ApiException.createApiException(ApiConstant.NOT_SET_Timestamp_ERROR_CODE,json);
			}
			//判断timestamp格式
			Date timestamp;
			try {
				timestamp=UTC_FORMAT.parse(timestampStr);
			} catch (Exception e) {
				throw ApiException.createApiException(ApiConstant.Timestamp_FORMAT_ERROR_CODE,json); //"BadRequest:Timestampformat error"
			}
			//判断timestamp是否在服务器时间前后30分钟
			long nowLong =now.getTime();
			long thirtyMin=30*60*1000;
			if(timestamp.getTime()+28800000>nowLong+thirtyMin||timestamp.getTime()+28800000<nowLong-thirtyMin){//如果传入时间大于当前时间+30min  或者  传入时间小于当前时间-30min
				throw ApiException.createApiException(ApiConstant.SIGNATURE_EXPIRED_ERROR_CODE,json);	//"BadRequest:The request signature expired"
			}
			
			//Signature是否设置
			String signature=params.getString("Signature");
			if(StringUtil.isEmpty(signature)){
				throw ApiException.createApiException(ApiConstant.NOT_SET_SIGNATURE_ERROR_CODE,json);	//"BadRequest:Not set Signature"
			}
			//Signature格式判断
			String signReg="[a-zA-Z0-9+=/]*";
			Pattern signPattern = Pattern.compile(signReg);
			Matcher signMatcher = signPattern.matcher(signature);
			boolean signRs = signMatcher.matches();
			if(!signRs){
				throw ApiException.createApiException(ApiConstant.SIGNATURE_FORMAT_ERROR_CODE,json);	//"BadRequest:Signature format error"
			}
			//Signature是否匹配
			JSONObject cofigParam=new JSONObject();
			cofigParam=params;
			cofigParam.remove("Signature");
			String[] sortedKeys=cofigParam.keySet().toArray(new String[]{});
			//对参数进行排序，注意严格区分大小写
			Arrays.sort(sortedKeys);
			StringBuffer sb=new StringBuffer();
			for (String key : sortedKeys) {
				sb.append(key);
				sb.append("=");
				sb.append(URLEncoder.encode(cofigParam.getString(key), "utf-8"));
				sb.append("&");
			}
			sb.deleteCharAt(sb.length()-1);
			String serverSignature=HmacSHA1Util.getEncrypt(sb.toString(), akJson.getString("secretKey"));
			//Signature是否匹配
			if(!signature.equals(serverSignature)){
				throw ApiException.createApiException(ApiConstant.SIGNATURE_NOT_MATCH_ERROR_CODE,json);	//"conflictingRequest:Signature does not match"
			}
			//查询客户是否被冻结
			boolean checkBlock=ApiUtil.checkCustomerIsBlock(cusId);
			if(checkBlock){
				throw ApiException.createApiException(ApiConstant.CUS_FROZEN_ERROR_CODE,json);	//BadRequest:Your account already frozen
			}
			//是否在黑名单内
			//根据客户id获取是否在黑名单内 checkCus
			boolean checkCus=ApiUtil.checkBlackCustomer(cusId);
			if(checkCus){
				throw ApiException.createApiException(ApiConstant.ACCOUNT_IS_DISABLED,json);	//"Forbidden:Your account  is disabled"
			}
//			JSONObject result=new JSONObject();
			params.put(ApiConstant.TAG_CUS_ID, cusId);										//客户id
			params.put(ApiConstant.TAG_ACTION, action);									//action名称
			params.put(ApiConstant.TAG_RESOURCE_TYPE, restrict.getString("apiTypeName"));			//api类别中文名称
			params.put(ApiConstant.TAG_DC_NAME, regionJson.getString("dcName"));					//数据中心中文名称
			params.put(ApiConstant.TAG_API_NAME, restrict.getString("actionName"));				//action中文名称
			params.put(ApiConstant.TAG_DC_ID, regionJson.getString("dcId")); 					//数据中心id
			return params;
		} catch (ApiException e) {
			throw e;
		} catch (Exception e){
			log.error(e.getMessage(),e);
			throw ApiException.createApiException(ApiConstant.INTERNAL_ERROR_CODE,json);	//"InternalError:The request processing has failed due to some unknown error, exception or failure"
		}
	}

}
