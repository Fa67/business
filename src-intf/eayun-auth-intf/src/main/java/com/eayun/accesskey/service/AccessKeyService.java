package com.eayun.accesskey.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.eayun.accesskey.model.AccessKey;
import com.eayun.accesskey.model.BaseAccessKey;
import com.eayun.common.dao.support.Page;

public interface AccessKeyService {
	/**
	 * 判断是否应该进行操作
	 * @param time
	 * @return
	 * @throws Exception
	 */
	public String operatorIsPass(Date time) throws Exception;
	/**
	 * 开启ak
	 * @param map
	 * @return
	 */
	@SuppressWarnings("rawtypes")
    public AccessKey startAcck(Map map) throws Exception;
	/**
	 * 停用ak
	 * @param map
	 * @return
	 */
	@SuppressWarnings("rawtypes")
    public AccessKey blockAcck(Map map) throws Exception;
	/**
	 * 删除ak
	 * @param map
	 * @return
	 */
	@SuppressWarnings("rawtypes")
    public String deleteAcck(Map map) throws Exception;
	/**
	 * 创建ak
	 * @param cusId
	 * @return
	 */
	public AccessKey addAcck(String cusId) throws Exception;
	/**
	 * 获取用户创建时产生的默认ak
	 * @param cusId 客户id
	 * @return
	 * @throws Exception 
	 */
	public AccessKey getDefaultAK(String cusId) throws Exception;
	/**
	 * 改变显示状态
	 * @param params
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
    public AccessKey checkShow(Map params) throws Exception;
	/**
	 * 获取ak列表
	 * @param cusId
	 * @return
	 * @throws Exception 
	 */
	public Page getAKListPage(String cusId) throws Exception;
	 /**
     * 校验手机验证码
     * @param userId
     * @param verCode
     * @return
     */
    public boolean checkCode(String userId , String verCode , String oldPhone);
    /**
	 * 获取sk列表
	 * @param cusId
	 * @return
	 * @throws Exception 
	 */
	public List<BaseAccessKey> getSkList(String secretkey) throws Exception;
	/**
	 * 获取ak列表
	 * @param cusId
	 * @return
	 * @throws Exception 
	 */
	public List<BaseAccessKey> getAkList(String accesskey) throws Exception;
	/**
	 * 保存ak
	 * @param cusId
	 * @return
	 * @throws Exception 
	 */
	public BaseAccessKey saveAk(BaseAccessKey bak) throws Exception;
	public String flush(String cusId) throws Exception;
	/**
	 * 获取所有ak
	 */
	public List<AccessKey> getAllAk() throws Exception;
	
	/**
     * 停掉客户下的非默认的启用状态下的ak
     * @author liyanchao
     * @param checked 标识是停服务还是冻结账户  0是停服务  1是冻结账户
     * @return
     */
	public List<AccessKey> stopRunningAkExceptDefaultByCusId(String cusId,String checked) throws Exception;
	/**
     * 恢复obs_accessKey中客户下的非默认的启用状态下的ak；仅限于底层层面的恢复
     * @author liyanchao
     * @param checked 标识是停服务还是冻结账户  0是恢复服务  1是解冻账户
     * @return
     */
	public List<AccessKey> resumeAkExceptDefaultByCusId(String cusId,String checked) throws Exception;
	
	/**
	 * 验证该用户的手机号是否通过验证
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public String checkPhoneIsPass(String userId) throws Exception;
}
