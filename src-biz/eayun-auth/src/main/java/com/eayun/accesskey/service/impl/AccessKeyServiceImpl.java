package com.eayun.accesskey.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.accesskey.dao.AccessKeyDao;
import com.eayun.accesskey.model.AccessKey;
import com.eayun.accesskey.model.BaseAccessKey;
import com.eayun.accesskey.service.AccessKeyService;
import com.eayun.accesskey.service.ObsService;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.AkSkUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.HmacSHA1Util;
import com.eayun.common.util.ObsUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.customer.bean.PhoneVerify;
import com.eayun.customer.model.BaseCusServiceState;
import com.eayun.customer.model.Customer;
import com.eayun.customer.model.User;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.customer.serivce.PhoneVerifyService;
import com.eayun.customer.serivce.UserService;
import com.eayun.obs.base.service.ObsBaseService;
import com.eayun.obs.model.ObsAccessBean;
import com.eayun.obs.service.ObsOpenService;
import com.eayun.obs.service.ObsStorageService;

@Service
@Transactional
public class AccessKeyServiceImpl implements AccessKeyService {
	private static final Logger log = LoggerFactory
			.getLogger(AccessKeyServiceImpl.class);
	@Autowired
	private AccessKeyDao accessKeyDao;

	@Autowired
	private ObsService obsService;

	@Autowired
	private PhoneVerifyService phoneVerifyService;

	@Autowired
	private JedisUtil jedisUtil;

	@Autowired
	private ObsBaseService obsBaseService;
	@Autowired
	private ObsStorageService obsStorageService;
	@Autowired
	private ObsOpenService obsOpenService;
	@Autowired
	private UserService userService;

	@Override
	public String operatorIsPass(Date time) throws Exception {
		if (time == null) {
			return "false";
		} else {
			Date now = new Date();
			if ((now.getTime() - time.getTime()) / 60000 > 30) {
				return "false";
			}
		}
		return "success";
	}

	@SuppressWarnings({ "rawtypes", "unused" })
    @Override
	public AccessKey startAcck(Map map) throws Exception {
		// 向底层通过指定akId和accessKey来创建ak
		// 获取底层返回的结果，得到scretKey
		try {
			String akStr;
			akStr = jedisUtil.get(RedisKey.AK_AKID + map.get("akId").toString());

			AccessKey akJson = JSONObject.toJavaObject(
					JSONObject.parseObject(akStr), AccessKey.class);
			String userId = akJson.getUserId();
			String accessKey = akJson.getAccessKey();
			String secretKey = akJson.getSecretKey();
			if (obsOpenService.getObsByCusId(akJson.getUserId())!=null&&"1".equals(obsOpenService.getObsByCusId(userId).getObsState())
					&& !obsStorageService.obsIsStopService(userId)) {
				String result = null;
				String ak = ObsUtil.getAdminAccessKey();
				String sk = ObsUtil.getAdminSecretKey();
				String date = DateUtil.getRFC2822Date(new Date());

				String url = "/admin/user";
				String signature = ObsUtil.getSignature("PUT", "", "", date,
						"", url);

				String hmacSHA1 = HmacSHA1Util.getEncrypt(signature, sk);
				String host = ObsUtil.getEayunObsHost();
				String header = ObsUtil.getRequestHeader();
				String strPut = header + host + url + "?key&uid=" + userId
						+ "&access-key=" + accessKey + "&secret-key="
						+ secretKey;

				ObsAccessBean obsBean = new ObsAccessBean();
				obsBean.setAccessKey(ak);
				obsBean.setHmacSHA1(hmacSHA1);
				obsBean.setHost(host);
				obsBean.setPutHeaderHost(host);
				obsBean.setRFC2822Date(date);
				obsBean.setHttp("http://".equals(header));
				obsBean.setUrl(strPut);
				result = (obsBaseService.put(obsBean)).toJSONString();
			}

			// 将信息更新到数据库
			BaseAccessKey bak = new BaseAccessKey();
			bak.setAkId(akJson.getAkId());
			bak.setUserId(akJson.getUserId());
			bak.setAccessKey(akJson.getAccessKey());
			bak.setSecretKey(akJson.getSecretKey());
			bak.setAcckIsShow(akJson.getAcckIsShow());
			bak.setAcckState("0");
			bak.setIsStopService(akJson.getIsStopService());
			bak.setCreateDate(DateUtil.timestampToDate(map.get("createDate")
					.toString()));
			bak.setIsDefault(akJson.getIsDefault());
			BaseAccessKey baks = (BaseAccessKey) accessKeyDao.saveEntity(bak);

			jedisUtil.set(RedisKey.AK_AKID + baks.getAkId(),
					JSONObject.toJSONString(baks));
			jedisUtil.set(RedisKey.AK_ACCESSKEY+baks.getAccessKey(), JSONObject.toJSONString(baks));
			AccessKey aks = new AccessKey();
			BeanUtils.copyPropertiesByModel(aks, baks);
			return aks;
		} catch (Exception e1) {
			throw e1;
		}
	}

	@SuppressWarnings({ "rawtypes", "unused" })
    @Override
	public AccessKey blockAcck(Map map) throws Exception {
		// 底层删除ak
		String akStr = jedisUtil.get(RedisKey.AK_AKID + map.get("akId").toString());

		AccessKey akJson = JSONObject.toJavaObject(
				JSONObject.parseObject(akStr), AccessKey.class);
		String accessKey = map.get("accessKey").toString();
		String result = null;
		try {
			if (obsOpenService.getObsByCusId(akJson.getUserId())!=null&&"1".equals(obsOpenService.getObsByCusId(akJson.getUserId())
					.getObsState())
					&& !obsStorageService.obsIsStopService(akJson.getUserId())) {
				String ak = ObsUtil.getAdminAccessKey();
				String sk = ObsUtil.getAdminSecretKey();
				String date = DateUtil.getRFC2822Date(new Date());

				String url = "/admin/user";
				String signature = ObsUtil.getSignature("DELETE", "", "", date,
						"", url);

				String hmacSHA1 = HmacSHA1Util.getEncrypt(signature, sk);
				String host = ObsUtil.getEayunObsHost();
				String header = ObsUtil.getRequestHeader();
				String strPut = header + host + url + "?key&access-key="
						+ accessKey;

				ObsAccessBean obsBean = new ObsAccessBean();
				obsBean.setAccessKey(ak);
				obsBean.setHmacSHA1(hmacSHA1);
				obsBean.setHost(host);
				obsBean.setPutHeaderHost(host);
				obsBean.setRFC2822Date(date);
				obsBean.setHttp("http://".equals(header));
				obsBean.setUrl(strPut);
				result = obsBaseService.deleteAccessKey(obsBean);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
		// 数据库更新状态
			try {

				BaseAccessKey bak = new BaseAccessKey();
				bak.setAkId(akJson.getAkId());
				bak.setUserId(akJson.getUserId());
				bak.setAccessKey(akJson.getAccessKey());
				bak.setSecretKey(akJson.getSecretKey());
				bak.setAcckIsShow(akJson.getAcckIsShow());
				bak.setIsStopService(akJson.getIsStopService());
				bak.setAcckState("1");
				bak.setCreateDate(DateUtil.timestampToDate(map
						.get("createDate").toString()));
				bak.setIsDefault(akJson.getIsDefault());
				BaseAccessKey baks = (BaseAccessKey) accessKeyDao
						.saveEntity(bak);

				jedisUtil.set(RedisKey.AK_AKID + baks.getAkId(),
						JSONObject.toJSONString(baks));
				jedisUtil.set(RedisKey.AK_ACCESSKEY+baks.getAccessKey(), JSONObject.toJSONString(baks));
				AccessKey ak = new AccessKey();
				BeanUtils.copyPropertiesByModel(ak, baks);
				return ak;
			} catch (Exception e) {
				throw e;
			}
	}

	@SuppressWarnings("rawtypes")
    @Override
	public String deleteAcck(Map map) throws Exception {
		// 底层删除密钥
		String accessKey = map.get("accessKey").toString();
		String result = "";
		String acckState = map.get("acckState").toString();
		String cusId = map.get("userId").toString();
		// 如果当前密钥为启用状态，已开通对象存储服务,服务可用状态,则先执行底层删除
		if ("0".equals(acckState)&&obsOpenService.getObsByCusId(cusId)!=null
				&& "1".equals(obsOpenService.getObsByCusId(cusId).getObsState())
				&& !obsStorageService.obsIsStopService(cusId)) {
			try {
				String ak = ObsUtil.getAdminAccessKey();
				;
				String sk = ObsUtil.getAdminSecretKey();
				String date = DateUtil.getRFC2822Date(new Date());

				String url = "/admin/user";
				String signature = ObsUtil.getSignature("DELETE", "", "", date,
						"", url);

				String hmacSHA1 = HmacSHA1Util.getEncrypt(signature, sk);
				String host = ObsUtil.getEayunObsHost();
				String header = ObsUtil.getRequestHeader();
				String strPut = header + host + url + "?key&access-key="
						+ accessKey;

				ObsAccessBean obsBean = new ObsAccessBean();
				obsBean.setAccessKey(ak);
				obsBean.setHmacSHA1(hmacSHA1);
				obsBean.setHost(host);
				obsBean.setPutHeaderHost(host);
				obsBean.setRFC2822Date(date);
				obsBean.setHttp("http://".equals(header));
				obsBean.setUrl(strPut);
				result = obsBaseService.deleteAccessKey(obsBean);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw e;
			}
		}
		if (result != null) {
			String akId = map.get("akId").toString();
			StringBuffer hql = new StringBuffer();
			hql.append("delete BaseAccessKey where akId = ?");
			accessKeyDao.executeUpdate(hql.toString(), akId);
			try {
				jedisUtil.delete(RedisKey.AK_AKID + akId);
				jedisUtil.removeFromSet(RedisKey.AK_CUSID + cusId, akId);
				jedisUtil.delete(RedisKey.AK_ACCESSKEY+accessKey);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw e;
			}
			return "success";
		} else {
			return "faild";
		}
	}

	@SuppressWarnings("unchecked")
    @Override
	public AccessKey addAcck(String cusId) throws Exception {
		// 底层创建ak
		try {
			String accesskey = null;
			String secretkey = null;
			// 判重(accessKey)
			while (true) {
				accesskey = AkSkUtil.getAccessKeyStr();
				List<BaseAccessKey> list = accessKeyDao.find(
						"from BaseAccessKey where accessKey=?", accesskey);
				if (list == null || list.size() == 0) {
					break;
				}
			}
			// 判重(secretKey)
			while (true) {
				secretkey = AkSkUtil.getSecretKeyStr();
				List<BaseAccessKey> list = accessKeyDao.find(
						"from BaseAccessKey where secretKey=?", secretkey);
				if (list == null || list.size() == 0) {
					break;
				}
			}
			if (accesskey != null && secretkey != null) {
				if (obsOpenService.getObsByCusId(cusId) != null
						&& "1".equals(obsOpenService.getObsByCusId(cusId)
								.getObsState())
						&& !obsStorageService.obsIsStopService(cusId)) {
					addToObs(cusId, accesskey, secretkey);
				}
				AccessKey ak = addToDataBase(cusId, accesskey, secretkey);
				return ak;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}

		return null;
	}

	private AccessKey addToDataBase(String cusId, String accesskey,
			String secretkey) throws Exception {
		BaseAccessKey bak = new BaseAccessKey();
		bak.setAccessKey(accesskey);
		bak.setAcckIsShow("1");
		bak.setAcckState("0");
		bak.setCreateDate(new Date());
		bak.setIsDefault("1");
		bak.setSecretKey(secretkey);
		bak.setUserId(cusId);
		bak.setIsStopService(obsStorageService.obsIsStopService(cusId));
		bak = (BaseAccessKey) accessKeyDao.saveEntity(bak);
		jedisUtil.addToSet(RedisKey.AK_CUSID + cusId, bak.getAkId());
		jedisUtil.set(RedisKey.AK_AKID + bak.getAkId(), JSONObject.toJSONString(bak));
		jedisUtil.set(RedisKey.AK_ACCESSKEY+bak.getAccessKey(), JSONObject.toJSONString(bak));
		AccessKey ak = new AccessKey();
		BeanUtils.copyPropertiesByModel(ak, bak);
		return ak;
	}

	@SuppressWarnings("unused")
    private void addToObs(String cusId, String accesskey, String secretkey)
			throws Exception {
		String result;
		String aks = ObsUtil.getAdminAccessKey();
		String sks = ObsUtil.getAdminSecretKey();
		String date = DateUtil.getRFC2822Date(new Date());

		String url = "/admin/user";
		String signature = ObsUtil.getSignature("PUT", "", "", date, "", url);

		String hmacSHA1 = HmacSHA1Util.getEncrypt(signature, sks);
		String host = ObsUtil.getEayunObsHost();
		String header = ObsUtil.getRequestHeader();
		String strPut = header + host + url + "?key&uid=" + cusId
				+ "&access-key=" + accesskey + "&secret-key=" + secretkey;

		ObsAccessBean obsBean = new ObsAccessBean();
		obsBean.setAccessKey(aks);
		obsBean.setHmacSHA1(hmacSHA1);
		obsBean.setHost(host);
		obsBean.setPutHeaderHost(host);
		obsBean.setRFC2822Date(date);
		obsBean.setHttp("http://".equals(header));
		obsBean.setUrl(strPut);
		result = (obsBaseService.put(obsBean)).toJSONString();
	}

	@Override
	public AccessKey getDefaultAK(String cusId) throws Exception {
		Set<String> set = jedisUtil.getSet(RedisKey.AK_CUSID + cusId);
		JSONObject akObj = null;
		for (String akid : set) {
			String akStr = jedisUtil.get(RedisKey.AK_AKID + akid);
			JSONObject akJsonObj = JSONObject.parseObject(akStr);
			String isDefault = akJsonObj.getString("isDefault");
			if ("0".equals(isDefault)) {
				akObj = akJsonObj;
				break;
			}
		}
		if (akObj == null) {
			return null;
		}
		AccessKey ak = JSONObject.toJavaObject(akObj, AccessKey.class);
		return ak;
	}

	@SuppressWarnings("rawtypes")
    @Override
	public AccessKey checkShow(Map params) throws Exception {
		String akStr = jedisUtil
				.get(RedisKey.AK_AKID + params.get("akId").toString());
		AccessKey akJson = JSONObject.toJavaObject(
				JSONObject.parseObject(akStr), AccessKey.class);
		// String time = params.get("createDate").toString();
		// long date = Long.parseLong(time);
		BaseAccessKey bak = new BaseAccessKey();
		bak.setAccessKey(akJson.getAccessKey());
		bak.setAcckState(akJson.getAcckState());
		bak.setAkId(akJson.getAkId());
		bak.setCreateDate(akJson.getCreateDate());
		bak.setIsDefault(akJson.getIsDefault());
		bak.setSecretKey(akJson.getSecretKey());
		bak.setUserId(akJson.getUserId());
		bak.setIsStopService(akJson.getIsStopService());
		String isShow = akJson.getAcckIsShow();
		// 1为隐藏 0为显示
		if ("1".equals(isShow)) {
			bak.setAcckIsShow("0");
		} else {
			bak.setAcckIsShow("1");
		}
		BaseAccessKey bak2 = (BaseAccessKey) accessKeyDao.saveEntity(bak);

		try {
			jedisUtil.set(RedisKey.AK_AKID + bak2.getAkId(),
					JSONObject.toJSONString(bak2));
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw e;
		}
		AccessKey ak = new AccessKey();
		BeanUtils.copyPropertiesByModel(ak, bak2);
		return ak;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
	public Page getAKListPage(String cusId) throws Exception {
		Set<String> set = jedisUtil.getSet(RedisKey.AK_CUSID + cusId);
		List<AccessKey> list = new ArrayList<AccessKey>();
		for (String akId : set) {

			String akStr = jedisUtil.get(RedisKey.AK_AKID + akId);
			AccessKey ak = JSONObject.toJavaObject(
					JSONObject.parseObject(akStr), AccessKey.class);
			if ("1".equals(ak.getIsDefault())) {
				Date date = ak.getCreateDate();
				ak.setCreDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(date));
				if ("1".equals(ak.getAcckIsShow())) {
					ak.setSecretKey(null);
				}
				list.add(ak);
			}
		}
		Collections.sort(list, new Comparator() {
			@Override
			public int compare(Object arg0, Object arg1) {
				AccessKey ak0 = (AccessKey) arg0;
				AccessKey ak1 = (AccessKey) arg1;
				return ak1.getCreateDate().compareTo(ak0.getCreateDate());
			}
		});
		Page page = new Page(0, list.size(), 10, list);
		return page;
	}

	@Override
	public boolean checkCode(String userId, String verCode, String oldPhone) {
		log.info("校验短信验证码");
		if (StringUtil.isEmpty(verCode)) {
			throw new AppException("请输入验证码!");
		}
		PhoneVerify phoneVerify = phoneVerifyService.findByUserAndPh(userId,
				oldPhone, "0");
		if (phoneVerify == null || phoneVerify.getId() == null) {
			throw new AppException("您还未发送验证码");
		}
		if (!verCode.equals(phoneVerify.getPhoneCode())) {
			throw new AppException("手机验证码不正确，请重新输入");
		}
		if (phoneVerify.isVerify()) {
			throw new AppException("手机验证码已使用，请重新获取");
		}
		Date date = new Date();
		if (date.after(phoneVerify.getInvalidTime())) {
			throw new AppException("手机验证码已过期，请重新获取");
		}
		if (verCode.equals(phoneVerify.getPhoneCode())) {
			phoneVerifyService.updatePhoneByVerify(phoneVerify);
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
    @Override
	public List<BaseAccessKey> getSkList(String secretkey) throws Exception {
		List<BaseAccessKey> list = accessKeyDao.find(
				"from BaseAccessKey where secretKey=?", secretkey);
		return list;
	}

	@SuppressWarnings("unchecked")
    @Override
	public List<BaseAccessKey> getAkList(String accesskey) throws Exception {
		List<BaseAccessKey> list = accessKeyDao.find(
				"from BaseAccessKey where accessKey=?", accesskey);
		return list;
	}

	@Override
	public BaseAccessKey saveAk(BaseAccessKey bak) throws Exception {
		BaseAccessKey baks = (BaseAccessKey) accessKeyDao.saveEntity(bak);
		return baks;
	}

	@Override
	public String flush(String cusId) throws Exception {
		Set<String> set = jedisUtil.getSet(RedisKey.AK_CUSID + cusId);
		List<AccessKey> list = new ArrayList<AccessKey>();
		for (String akId : set) {

			String akStr = jedisUtil.get(RedisKey.AK_AKID + akId);
			AccessKey ak = JSONObject.toJavaObject(
					JSONObject.parseObject(akStr), AccessKey.class);
			if ("1".equals(ak.getIsDefault())) {
				Date date = ak.getCreateDate();
				ak.setCreDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(date));

				list.add(ak);
			}
		}
		for (AccessKey accessKey : list) {
			accessKey.setAcckIsShow("1");
			BaseAccessKey bak = new BaseAccessKey();
			bak.setAccessKey(accessKey.getAccessKey());
			bak.setAcckState(accessKey.getAcckState());
			bak.setAkId(accessKey.getAkId());
			bak.setCreateDate(accessKey.getCreateDate());
			bak.setIsDefault(accessKey.getIsDefault());
			bak.setSecretKey(accessKey.getSecretKey());
			bak.setUserId(accessKey.getUserId());
			bak.setAcckIsShow(accessKey.getAcckIsShow());
			BaseAccessKey bak2 = (BaseAccessKey) accessKeyDao.merge(bak);
			try {
				jedisUtil.set(RedisKey.AK_AKID + bak2.getAkId(),
						JSONObject.toJSONString(bak2));
			} catch (Exception e) {
			    log.error(e.getMessage(),e);
				return "error";
			}
		}

		return "success";
	}

	@SuppressWarnings("unchecked")
    @Override
	public List<AccessKey> getAllAk() throws Exception {
		List<BaseAccessKey> baseAccessKeyList = accessKeyDao
				.find("from BaseAccessKey");
		List<AccessKey> accessKeyList = new ArrayList<AccessKey>();
		for (BaseAccessKey baseAccessKey : baseAccessKeyList) {
			AccessKey accessKey = new AccessKey();
			BeanUtils.copyPropertiesByModel(accessKey, baseAccessKey);
			accessKeyList.add(accessKey);
		}
		return accessKeyList;
	}

	/**
	 * 查询客户下的非默认的ak
	 * 
	 * @author liyanchao
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<AccessKey> stopRunningAkExceptDefaultByCusId(String cusId,String checked)
			throws Exception {
		List<BaseAccessKey> baseAccessKeyList = new ArrayList<BaseAccessKey>();
		baseAccessKeyList = accessKeyDao
				.getRunningAkExceptDefaultByCusId(cusId);
		for (BaseAccessKey key : baseAccessKeyList) {
			this.deleteAk(key.getAkId());
		}
		if("0".equals(checked)){//如果是停服务,则需要更改计费状态
			// 将当前的客户所有的用户的is_stopservice改为已停用
			List<BaseAccessKey> accessKeyList = accessKeyDao.getAkByCusId(cusId);
			List<AccessKey> keyList = new ArrayList<AccessKey>();
			List<BaseAccessKey> lastList = new ArrayList<BaseAccessKey>();
			for (BaseAccessKey accessKey : accessKeyList) {
				AccessKey key = new AccessKey();
				accessKey.setIsStopService(true);
				accessKeyDao.merge(accessKey);
				BeanUtils.copyPropertiesByModel(key, accessKey);
				lastList.add(accessKey);
				keyList.add(key);
			}
			//更新redis中的数据
			Set<String> set = jedisUtil.getSet(RedisKey.AK_CUSID + cusId);
			for (String akid : set) {
				for(BaseAccessKey baseAccessKey :lastList){
					if(akid.equals(baseAccessKey.getAkId())){
						jedisUtil.set(RedisKey.AK_AKID + akid ,JSONObject.toJSONString(baseAccessKey));
						jedisUtil.set(RedisKey.AK_ACCESSKEY+baseAccessKey.getAccessKey(), JSONObject.toJSONString(baseAccessKey));
						break;
					}

				}
			}
			
			return keyList;
		}
		return null;
	}

	/**
	 * 查询客户下的非默认的ak
	 * 
	 * @author liyanchao
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<AccessKey> resumeAkExceptDefaultByCusId(String cusId,String checked)
			throws Exception {
		AccessKey ak = getDefaultAK(cusId);
		if(null != ak){
			boolean isStopService = ak.getIsStopService();
			if("1".equals(checked)&&!isStopService||"0".equals(checked)){//解冻并且obs未停服务  || 恢复服务
				List<BaseAccessKey> baseAccessKeyList = new ArrayList<BaseAccessKey>();
				baseAccessKeyList = accessKeyDao
						.getRunningAkExceptDefaultByCusId(cusId);
				for (BaseAccessKey key : baseAccessKeyList) {
					this.resumeAk(key.getAkId());
				}
			}
		}
		if("0".equals(checked)){
			// 将当前的客户所有的用户的is_stopservice改为解冻
			List<BaseAccessKey> accessKeyList = accessKeyDao.getAkByCusId(cusId);
			List<AccessKey> keyList = new ArrayList<AccessKey>();
			List<BaseAccessKey> lastList = new ArrayList<BaseAccessKey>();
			for (BaseAccessKey accessKey : accessKeyList) {
				AccessKey key = new AccessKey();
				accessKey.setIsStopService(false);
				accessKeyDao.merge(accessKey);
				BeanUtils.copyPropertiesByModel(key, accessKey);
				lastList.add(accessKey);
				keyList.add(key);
			}
			//更新redis中的数据
			Set<String> set = jedisUtil.getSet(RedisKey.AK_CUSID + cusId);
			for (String akid : set) {
				for(BaseAccessKey baseAccessKey :lastList){
					if(akid.equals(baseAccessKey.getAkId())){
						jedisUtil.set(RedisKey.AK_AKID + akid ,JSONObject.toJSONString(baseAccessKey));
						jedisUtil.set(RedisKey.AK_ACCESSKEY+baseAccessKey.getAccessKey() ,JSONObject.toJSONString(baseAccessKey));
						break;
					}
				}
			}
			return keyList;
		}
		return null;
	}

	/**
	 * 删除obs底层ak
	 * 
	 * @throws Exception
	 *
	 **/
	private void deleteAk(String akId) throws Exception {
		String akStr;
		akStr = jedisUtil.get(RedisKey.AK_AKID + akId);
		AccessKey akJson = JSONObject.toJavaObject(
				JSONObject.parseObject(akStr), AccessKey.class);
		String userId = akJson.getUserId();
		String accessKey = akJson.getAccessKey();
		String str = jedisUtil.get(RedisKey.CUSSERVICESTATE_CUSID + userId);

		BaseCusServiceState cusServiceState = new BaseCusServiceState();
		if (null != str) {
			cusServiceState = JSONObject.toJavaObject(
					JSONObject.parseObject(str), BaseCusServiceState.class);
		}
		if(null != cusServiceState){
			if ("1".equals(cusServiceState.getObsState())) {
				// 底层删除ak
				String result = null;
				String ak = ObsUtil.getAdminAccessKey();
				String sk = ObsUtil.getAdminSecretKey();
				String date = DateUtil.getRFC2822Date(new Date());

				String url = "/admin/user";
				String signature = ObsUtil.getSignature("DELETE", "", "", date, "",
						url);

				String hmacSHA1 = HmacSHA1Util.getEncrypt(signature, sk);
				String host = ObsUtil.getEayunObsHost();
				String header = ObsUtil.getRequestHeader();
				String strPut = header + host + url + "?key&access-key="
						+ accessKey;

				ObsAccessBean obsBean = new ObsAccessBean();
				obsBean.setAccessKey(ak);
				obsBean.setHmacSHA1(hmacSHA1);
				obsBean.setHost(host);
				obsBean.setPutHeaderHost(host);
				obsBean.setRFC2822Date(date);
				obsBean.setHttp("http://".equals(header));
				obsBean.setUrl(strPut);
				result = obsBaseService.deleteAccessKey(obsBean);
				log.error(result);
			}
		}
		

	}

	/**
	 * 恢复obs底层ak
	 * 
	 * @throws Exception
	 *
	 **/
	private void resumeAk(String akId) throws Exception {
		String akStr;
		akStr = jedisUtil.get(RedisKey.AK_AKID + akId);
		AccessKey akJson = JSONObject.toJavaObject(
				JSONObject.parseObject(akStr), AccessKey.class);
		String userId = akJson.getUserId();
		String accessKey = akJson.getAccessKey();
		String secretKey = akJson.getSecretKey();
		String str = jedisUtil.get(RedisKey.CUSSERVICESTATE_CUSID + userId);

		BaseCusServiceState cusServiceState = new BaseCusServiceState();
		if (null != str) {
			cusServiceState = JSONObject.toJavaObject(
					JSONObject.parseObject(str), BaseCusServiceState.class);
		}
		if(null != cusServiceState){
			if ("1".equals(cusServiceState.getObsState())) {// 表示客户开通了obs服务，然后进行底层ak恢复调用
				addToObs(userId, accessKey, secretKey);
			}
		}
		
	}

	@Override
	public String checkPhoneIsPass(String userId) throws Exception {
		User user=userService.findUserById(userId);
		if(!user.getIsPhoneValid()){
    		throw new AppException("手机验证未通过");
    	}
		return user.getUserPhone();
	}
}
