package com.eayun.log.ecmcsevice;

import java.util.Date;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.log.model.OperLogPname;

/**
 * ECMC日志服务接口
 * @author jingang.liu@eayun.com
 * @Date 2016-03-29
 */
public interface EcmcLogService {

    
    /**
     * 添加日志(添加到mongo数据库中)
     * @param busiName       操作项
     * @param resourceType   资源类型
     * @param resourceName   资源名称
     * @param prjId          项目ID
     * @param returnFlag     状态
     * @param resourceId     资源ID
     * @param requestUrl     请求地址
     * @param e
     */
    public void addLog(String busiName, String resourceType, String resourceName, String prjId,
    		Integer returnFlag,String resourceId, Exception e);
    /**
     * 删除日志（定时任务）
     * @param date 删除这个时间之前的日志（如果没有传入默认180天前）
     */
    public void deleteLog(Date date);

    /**
     * 查询日志详情
     * @param id
     * @return
     * @throws Exception
     */
    public OperLogPname  getOneEcmcLogFromMongo(String id)throws AppException;

    /**
     * 从mongo中查询ECSC日志并分页
     * @param page
     * @param beginTime
     * @param endTime
     * @param actItem
     * @param status
     * @param prjId
     * @param resourceType
     * @param ip
     * @param resourceName
     * @param queryMap
     * @return
     * @throws Exception
     */
	public Page getLogListMongo(Page page, Date beginTime, Date endTime,
			String actItem, String status, String prjId, String resourceType,
			String ip, String resourceName, String operator, QueryMap queryMap) throws Exception;
	/**
	 * 从mongo中查询ECMC日志并分页
	 * @param page
	 * @param beginTime
	 * @param endTime
	 * @param actItem
	 * @param status
	 * @param prjId
	 * @param resourceType
	 * @param ip
	 * @param resourceName
	 * @param queryMap
	 * @return
	 * @throws Exception
	 */
	public Page getEcmcLogListMongo(Page page, Date beginTime, Date endTime,
			String actItem, String status, String prjId, String resourceType,
			String ip, String resourceName, QueryMap queryMap) throws Exception;
	
	/**
	 * 将ecsc日志信息转移到mongodb中
	 * @return
	 */
	public boolean syncLog() throws Exception;
	
	/**
	 * 将ecmc日志信息转移到mongodb中
	 * @return
	 */
	public boolean syncEcmcLog() throws Exception;
}
