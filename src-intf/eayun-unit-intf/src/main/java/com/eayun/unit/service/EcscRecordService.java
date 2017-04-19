package com.eayun.unit.service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.unit.model.ApplyWebs;
import com.eayun.unit.model.BaseApplyInfo;
import com.eayun.unit.model.BaseCloudArea;
import com.eayun.unit.model.BaseUnitInfo;
import com.eayun.unit.model.BaseWebDataCenterIp;
import com.eayun.unit.model.RecordMultipartFile;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年12月20日
 */
public interface EcscRecordService {

	/**
	 * 行政区域查询
	 * @param parentcode
	 * @return
	 */
	public List<BaseCloudArea> getAreaList(String parentcode);
	
	/**
	 * 查询区域名称包含父级名称
	 * @param code
	 * @return
	 */
	public String getAreaName(String code);
	/**
	 * 查询备案列表
	 * @param page
	 * @param recordType
	 * @param status
	 * @param Id
	 * @return
	 */
	public Page getrecordList(Page page, String recordType, String status,String cusId,QueryMap querymap);
	/**
	 * 查询单个备案详情
	 * @param applyId
	 * @return
	 * @throws Exception
	 */
	public ApplyWebs getApplyOneDetail(String applyId)throws Exception;
	/**
	 * 查询主体详情
	 * @param applyId
	 * @return
	 * @throws Exception
	 */
	public BaseUnitInfo getUnitOneDetail(String unitId)throws Exception;
	
	/**
	 * 根据状态查询下面有几条数据
	 * @param cusId
	 * @param status
	 * @return
	 */
	public int selectCount(String cusId,Integer status);
	
	/**
	  * 首次备案添加信息
	  * maps 包含unit_info和website_info
	  */
	public BaseApplyInfo addfirstrecord(Map<String,Object> maps) throws Exception;
	
	/**
	  * 删除指定备案信息
	  */
	public void deletefirstrecord(String id) throws Exception;
	
	/**
	 * 上传备案材料
	 * @param multipartFiles
	 * @return
	 */
	public List<Map<String, String>> uploadRecordFile(List<RecordMultipartFile> multipartFiles,String userId);
	/**
     * 下载文件
     * @param fileId：存储文件记录id
     */
    public InputStream downloadFile(String fileId) throws Exception;
    
    /**
     * 删除文件
     * @param fileId
     * @return
     * @throws Exception
     */
    public boolean deleteFile(String fileId) throws Exception;
    /**
     * 新增接入
     * @param maps
     * @return
     * @throws Exception
     */
    public BaseApplyInfo addAccessRecord(Map<String,Object> maps) throws Exception;
    

    /**
     * 查询客户所有备案成功列表
     * */
     public Page getrecordListapply(String cusid,QueryMap qm) throws Exception;

     /**
      * 查询公网IP
      * @param cusId   当前用户
      * @param resource_type  资源类型 vm（云主机） lb(负载均衡)
      * @param dc_Id   数据中心ID
      * @return
      * @throws Exception
      */
     public List<String> getFloatIp(String cusId,String resource_type,String dc_Id) throws Exception;
	
     /**
      * 查询客户注册邮箱
      * @param cusId
      * @return
      * @throws Exception
      */
     public String getCusEmail(String cusId)throws Exception;
     /**
      * 根据IP删除网站信息
      * (提供给IP释放时)
      * @param IP
      * @return
      * @throws Exception
      */
     public boolean deleteWebsiteByIP(String IP)throws Exception;
     /**
      * 更具IP查询该IP是否被备案
      * @param IP
      * @return
      */
     public List<BaseWebDataCenterIp> getWebDataCenterIp(String IP);
     
}
