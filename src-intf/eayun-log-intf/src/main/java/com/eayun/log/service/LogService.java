package com.eayun.log.service;

import java.util.Date;
import java.util.List;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.log.bean.ExcelLog;
import com.eayun.log.model.SysLog;

/**
 *                       
 * @Filename: LogService.java
 * @Description: 
 * @Version: 1.0
 * @Author:wang ning
 * @Email: ning.wang@eayun.com
 * @History:<br>
 *<li>Date: 2015年9月14日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public interface LogService {

    /**
     * 插入日志（此接口仅用于登录失败时调用）
     * @param actItem       操作项
     * @param actPerson		操作者
     * @param ResourceType  资源类型
     * @param ResourceName	资源名称
     * @param prjId         项目id
     * @param cusId         客户id
     * @param statu         状态
     * @param e             异常
     */
    public void addLog(String actItem, String actPerson, String resourceType, String resourceName,
                       String prjId, String cusId, String statu, Exception e);

    /**
     * @param actItem        操作项
     * @param resourceType   资源类型 
     * @param resourceName   资源名称
     * @param prjId          项目id
     * @param statu          状态
     * @param e              异常
     */
    public void addLog(String actItem, String resourceType, String resourceName, String prjId,
                       String statu, Exception e);

    /**
     * 具备IP入参的添加日志的方法，目前用于ECSC续费消息监听中添加日志。
     * @param actItem
     * @param actPerson
     * @param resourceType
     * @param resourceName
     * @param prjId
     * @param cusId
     * @param statu
     * @param operatorIp
     * @param e
     */
    void addLog(String actItem, String actPerson, String resourceType, String resourceName,
                String prjId, String cusId, String statu, String operatorIp, Exception e);
    
    /**
     * 分页查询日志信息(mongo)
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
    public Page getLogListMongo(Page page, Date beginTime, Date endTime, String actItem, String statu,
            String prjId, String cusId, String resourceType,String resourceName,String ip, String operator, QueryMap queryMap) throws Exception;

    /**
     * 导出Excel数据(mongo)
     * @param beginTime
     * @param endTime
     * @param actItem
     * @param statu
     * @param prjId
     * @param cusId
     * @return
     * @throws Exception
     */
	public List<ExcelLog> queryLogExcelFromMongo(Date beginTime, Date endTime,
			String actItem, String statu, String prjId, String cusId) throws Exception;

	/**
	 * 获取登录客户最新5条日志，用于总览显示
	 * 不计登录和退出
	 * @param sessionUser
	 * @return
	 */
	public List<SysLog> getLastLogs(SessionUserInfo sessionUser);
}
