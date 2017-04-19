package com.eayun.notice.ecmcservice;

import java.util.Date;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.notice.model.BaseNotice;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年4月1日
 */
public interface EcmcNoticeService {

	/**
	 * 分页查询公告
	 * @param pageSize     分页条数
	 * @param pageNumber   当前页
	 * @param beginTime    生效时间
	 * @param endTime      失效时间
	 * @param memo         标题
	 * @param isUsed       是否启用，1启用0，未启用
	 * @throws Exception
	 */
	public Page queryNoticeList(Page page, QueryMap querymap,Date beginTime,Date endTime,String memo,String isUsed,String title) throws Exception;
	/**
	 * 添加公告
	 * @param notice
	 * @throws Exception
	 */
	public void save(BaseNotice notice) throws Exception;
	
	/**
	 * 编辑公告
	 * @param notice
	 * @throws Exception
	 */
	public void edit(BaseNotice notice) throws Exception;
	
	/**
	 * 删除公告
	 * @param notice
	 * @throws Exception
	 */
	public void deleteNotice(BaseNotice notice) throws Exception;
	
}
