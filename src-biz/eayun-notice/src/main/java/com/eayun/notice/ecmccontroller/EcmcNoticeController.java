package com.eayun.notice.ecmccontroller;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.DateUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.notice.ecmcservice.EcmcNoticeService;
import com.eayun.notice.model.BaseNotice;
import com.eayun.unit.service.EcscRecordService;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年4月1日
 */
@Controller
@RequestMapping("ecmc/system/notice")
public class EcmcNoticeController {

	private final Log log = LogFactory.getLog(EcmcNoticeController.class);
	
	@Autowired
	private EcmcNoticeService ecmcnoticeservice;
	@Autowired
	private EcmcLogService ecmclogservice;
	
	@Autowired
	private EcscRecordService ecscRecordService; 
	
	/**
	 * 查询公告日志
	 * @param request
	 * @param pageSize
	 * @param pageNumber
	 * @param beginTime
	 * @param endTime
	 * @param memo
	 * @param isUsed
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("listNotice")
	@ResponseBody
	public Object queryNotice(HttpServletRequest request,Page page, @RequestBody ParamsMap mapparams)throws AppException {
		log.info("查询公告");
		try {
			Date beginTime = DateUtil.timestampToDate(mapparams.getParams().get("beginTime") == null ? null : mapparams.getParams().get("beginTime").toString());
			Date endTime = DateUtil.timestampToDate(mapparams.getParams().get("endTime") == null ? null : mapparams.getParams().get("endTime").toString());
			String memo = mapparams.getParams().get("memo") == null ? null :  mapparams.getParams().get("memo").toString();
			String isUsed = mapparams.getParams().get("isUsed") == null ? null :  mapparams.getParams().get("isUsed").toString();
			String title = mapparams.getParams().get("title") == null ? null :  mapparams.getParams().get("title").toString();
			
			QueryMap queryMap=new QueryMap();
	        queryMap.setPageNum(mapparams.getPageNumber());
	        queryMap.setCURRENT_ROWS_SIZE(mapparams.getPageSize());
			page = ecmcnoticeservice.queryNoticeList(page, queryMap, beginTime, endTime,memo,isUsed,title);
			
		} catch (Exception e) {
			throw new AppException("ecmc.system.notice.listNotice:"+e.getMessage());
		}
		return page;
	}
	/**
	 * 添加公告
	 * @param request
	 * @param notice
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("createNotice")
	@ResponseBody
	public Object save(HttpServletRequest request,@RequestBody BaseNotice notice)throws AppException{
		log.info("添加公告");
		EayunResponseJson respJson = new EayunResponseJson();
		try {
			ecmcnoticeservice.save(notice);
			ecmclogservice.addLog("添加公告", "公告", notice.getMemo(), null, 1, notice.getId(), null);
			respJson.setMessage("公告添加成功！");
			respJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			log.error(e,e);
			ecmclogservice.addLog("添加公告", "公告", notice.getMemo(), null, 0, notice.getId(), e);
			respJson.setMessage("公告添加失败！");
			respJson.setRespCode(ConstantClazz.ERROR_CODE);
			throw new AppException("error.ecmc.system.notice",e);
		}
		return respJson;
	}
	/**
	 * 修改公告
	 * @param request
	 * @param notice
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("modifyNotice")
	@ResponseBody
	public Object update(HttpServletRequest request,@RequestBody BaseNotice notice)throws AppException{
		log.info("修改公告");
		EayunResponseJson respJson = new EayunResponseJson();
		try {
			ecmcnoticeservice.edit(notice);
			ecmclogservice.addLog("修改公告", "公告", notice.getMemo(), null, 1, notice.getId(), null);
			respJson.setMessage("公告修改成功！");
			respJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		}  catch (Exception e) {
			log.error(e,e);
			ecmclogservice.addLog("修改公告", "公告", notice.getMemo(), null, 0, notice.getId(), e);
			respJson.setMessage("公告修改失败！");
			respJson.setRespCode(ConstantClazz.ERROR_CODE);
			throw new AppException("error.ecmc.system.notice",e);
		}
		return respJson;
	}
	/**
	 * 删除公告
	 * @param request
	 * @param notice
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("deleteNotice")
	@ResponseBody
	public Object deleteNotice(HttpServletRequest request,@RequestBody BaseNotice notice)throws AppException{
		log.info("删除公告");
		EayunResponseJson respJson = new EayunResponseJson();
		try {
			ecmcnoticeservice.deleteNotice(notice);
			ecmclogservice.addLog("删除公告", "公告", notice.getMemo(), null, 1, notice.getId(), null);
			respJson.setMessage("成功删除1条记录！");
			respJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			log.error(e,e);
			ecmclogservice.addLog("删除公告", "公告", notice.getMemo(), null, 0, notice.getId(), e);
			respJson.setMessage("操作失败！");
			respJson.setRespCode(ConstantClazz.ERROR_CODE);
			throw new AppException("error.ecmc.system.notice",e);
		}
		return respJson;
	}
	
	
	
	
	/**
	 * 运营邮件查询
	 * */
	@RequestMapping(value = "/getMailImg")
	public void downRecordFile(HttpServletRequest request,HttpServletResponse response) throws Exception{
		log.info("img");
		OutputStream out = null;
		InputStream inputStream = null;
		try {
			inputStream = ecscRecordService.downloadFile(request.getParameter("fileid"));
			// 将ContentType设为"image/jpeg"，让浏览器识别图像格式。
    		response.setContentType("image/jpg");
			out = response.getOutputStream();
			IOUtils.copy(inputStream, out);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}finally {
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(out);
		}
	}
	
	
	
	
}
