package com.eayun.virtualization.service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.file.model.EayunFile;
import com.eayun.virtualization.model.BaseCloudSecretKey;
import com.eayun.virtualization.model.CloudSecretKey;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2017年2月27日
 */
public interface CloudSecretKeyService {

    /**
     * 分页查询
     * @param page
     * @param prjId
     * @param dcId
     * @param name
     * @param queryMap
     * @return
     * @throws Exception
     */
    public Page getSecretKeyList(Page page, String prjId, String dcId, String name, QueryMap queryMap) throws Exception;
    
    /**
     * 获取该项目下云主机未绑定的密钥
     * @param vmId
     * @param prjId
     * @return
     * @throws Exception
     */
    public List<BaseCloudSecretKey> getunBindVmSecretkey(String vmId, String prjId)throws Exception;
    
    /**
     * 获取该项目下云主机绑定的密钥
     * @param vmId
     * @param prjId
     * @return
     * @throws Exception
     */
    public List<BaseCloudSecretKey> getBindVmSecretkey(String vmId, String prjId)throws Exception;
    
    /**
     * 验证重名
     * @param prjId
     * @param dcId
     * @param secretkeyId
     * @param name
     * @return
     * @throws Exception
     */
    public boolean checkName(String prjId, String dcId,String secretkeyId,String name)throws Exception;
    
    /**
     * 创建密钥
     * @param datacenterId
     * @param projectId
     * @param data
     * @param cusId
     * @return
     * @throws AppException
     */
    public CloudSecretKey create(String datacenterId, String projectId, Map<String, String> data,String cusId) throws AppException;
    
    /**
     * 下载私钥
     * @param fileId
     * @return
     * @throws Exception
     */
    public InputStream downloadFile(String fileId) throws Exception;
    
    /**
     * 删除私钥
     * @param fileId
     * @return
     * @throws Exception
     */
    public boolean deleteSecretKeyFile(String fileId) throws Exception;
    
    /**
     * 获取文件信息
     * @param fileId
     * @return
     */
    public EayunFile getFileBean(String fileId);
    
    /**
     * 获取虚拟机的 user_data
     * @param datacenterId
     * @param projectId
     * @param vm_id
     * @return
     * @throws AppException
     */
    public String getVmUserData(String datacenterId, String projectId, String vm_id) throws AppException;
    /**
     * 修改密钥
     * @param secretkeyId
     * @param name
     * @param desc
     * @return
     * @throws AppException
     */
    public boolean update(String secretkeyId,String name,String desc)throws AppException;
    /**
     * 删除密钥
     * @param dcId
     * @param prjId
     * @param secretkeyId
     * @return
     * @throws AppException
     */
    public boolean delete(String dcId,String prjId,String secretkeyId)throws AppException;
    
    
    /**
     * 
     * 获取秘钥详情
     * 
     * */
    public String getById(String secretkeyId)throws AppException;
    
    /**
     * 获取项目下除已绑定密钥的云主机
     * @param secretkeyId
     * @param prjId
     * @return
     * @throws Exception
     */
    public List getUnbindKeyVmList(String secretkeyId, String prjId) throws Exception;
    
    /**
     * 获取项目下已绑定该密钥的云主机
     * @param secretkeyId
     * @param prjId
     * @return
     * @throws Exception
     */
    public List getBindKeyVmList(String secretkeyId, String prjId) throws Exception;
    
    /**
     * 获取秘钥云主机详情
     * @param secretkeyId
     * @param queryMap
     * @return
     * @throws AppException
     */
    public Page getByIdAndVmlist(String secretkeyId,QueryMap queryMap)throws AppException;

    /**
     * 绑定/解绑密钥到云主机
     * @param dcId
     * @param prjId
     * @param secretkeyId
     * @param vmIds
     * @return
     * @throws AppException
     */
    public Map<String, String> BindSecretkeyToVm(String dcId,String prjId,String secretkeyId,List<String> vmIds)throws AppException;
    
    /**
     * 云主机绑定密钥
     * @param dcId
     * @param prjId
     * @param secretkeyId
     * @param vmId
     * @return
     * @throws AppException
     */
    public String BindSecretkeyToVm(String dcId,String prjId,String secretkeyId,String vmId)throws AppException;
    
    /**
     * 云主机解绑密钥
     * @param dcId
     * @param prjId
     * @param secretkeyId
     * @param vmId
     * @return
     * @throws AppException
     */
    public String unBindSecretkeyToVm(String dcId,String prjId,String secretkeyId,String vmId)throws AppException;
    
    
    /**
     * 云主机注销
     */
    public Boolean CanceSecretkeyToVm(String dcId,String prjId,String vmId)throws AppException;
    
    /**
     * 绑定/解绑云主机中的密钥
     * @param dcId
     * @param prjId
     * @param vmId
     * @param secretkeyIds
     * @return
     * @throws AppException
     */
    public String BindVmToSecretkey(String dcId,String prjId,String vmId,List<String> secretkeyIds)throws AppException;
    
    /**
     * 解绑云主机上的所有密钥
     * @param dcId
     * @param prjId
     * @param vmId
     * @return
     * @throws AppException
     */
    public String unBindSecretkeyForVm(String dcId,String prjId,String vmId)throws AppException;
    
    /**
     * 查询项目下所有密钥
     * @param dcId
     * @param prjId
     * @return
     * @throws Exception
     */
    public List<BaseCloudSecretKey> getAllSecretkey(String dcId,String prjId)throws Exception;
    
    /**
     * 获取客户已经创建了多少个密钥
     * @param prjId
     * @return
     */
    public int getAllSecretkey(String prjId);
    
    /**
     * 根据ID查询密钥
     * @param secretKeyId		SSH密钥ID
     * @return
     * @throws Exception
     */
    public BaseCloudSecretKey getSecretKeyById(String secretKeyId);
    
}
