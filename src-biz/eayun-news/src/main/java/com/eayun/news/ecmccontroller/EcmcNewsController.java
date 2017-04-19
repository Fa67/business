package com.eayun.news.ecmccontroller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.DateUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.news.ecmcnewsservice.EcmcNewsService;
import com.eayun.news.model.BaseNewsSend;
import com.eayun.news.model.NewsSendVOE;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年3月30日
 */
@Controller
@RequestMapping("/ecmc/system/news")
public class EcmcNewsController {

	private final Log log = LogFactory.getLog(EcmcNewsController.class);
	@Autowired
	private EcmcNewsService ecmcNewsService;
	@Autowired
	private EcmcLogService ecmclogservice;

	/**
	 * 获取消息集合（分页）
	 * 
	 * @param request
	 * @param pageSize
	 * @param pageNumber
	 * @param title
	 * @param begin
	 * @param end
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/getnewslist")
	@ResponseBody
	public Object getNewsList(HttpServletRequest request, Page page, @RequestBody ParamsMap mapparams)
			throws AppException {
		SessionUserInfo sessionUserInfo = (SessionUserInfo) request.getSession()
				.getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		String userAccount = sessionUserInfo == null ? null : sessionUserInfo.getUserName(); // 用户Account

		Date beginTime = DateUtil.timestampToDate(
				mapparams.getParams().get("begin") == null ? null : mapparams.getParams().get("begin").toString());
		Date endTime = DateUtil.timestampToDate(
				mapparams.getParams().get("end") == null ? null : mapparams.getParams().get("end").toString());
		String title = mapparams.getParams().get("title") == null ? null
				: mapparams.getParams().get("title").toString();
		String issys = mapparams.getParams().get("issyssend") == null ? null
				: mapparams.getParams().get("issyssend").toString();
		QueryMap queryMap = new QueryMap();
		queryMap.setPageNum(mapparams.getPageNumber());
		queryMap.setCURRENT_ROWS_SIZE(mapparams.getPageSize());
		page = ecmcNewsService.getNewsList(page, queryMap, beginTime, endTime, title, userAccount, issys);

		return page;
	}

	/**
	 * 添加消息
	 * 
	 * @param nsv
	 * @param respJson
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/createnews", consumes = "application/json")
	@ResponseBody
	public Object save(@RequestBody Map<String, String> params) throws AppException {
		EayunResponseJson respJson = new EayunResponseJson();
		BaseNewsSend nsv = new BaseNewsSend();

		try {

			nsv.setIs_syssend("0");
			String date = params.get("sendDate");
			date = date.replace("Z", " UTC");
			SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
			Date date2 = format2.parse(date);
			nsv.setSendDate(date2);
			nsv.setMemo(params.get("memo"));
			nsv.setNewsTitle(params.get("newsTitle"));
			nsv.setRecType(params.get("recType"));
			nsv.setSendPerson(params.get("sendPerson"));
			nsv.setIsSended("2");
			nsv.setCusId(params.get("cusId"));
			
			ecmcNewsService.save(nsv);
			respJson.setMessage("消息添加成功！");
			respJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmclogservice.addLog("添加消息", "消息", nsv.getNewsTitle(), null, 1, nsv.getId(), null);

		} catch (Exception e) {
			log.error(e, e);
			respJson.setMessage("消息添加失败！");
			respJson.setRespCode(ConstantClazz.ERROR_CODE);
			ecmclogservice.addLog("添加消息", "消息", nsv.getNewsTitle(), null, 0, nsv.getId(), e);
			throw new AppException("error.globe.system", e);
		}
		return respJson;
	}

	/**
	 * 检查消息过期
	 * 
	 * @param request
	 * @param newsSendVOE
	 * @param respJson
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("getTimeFlag")
	@ResponseBody
	public Object getTimeFlag(HttpServletRequest request, @RequestBody NewsSendVOE newsSendVOE,
			EayunResponseJson respJson) throws AppException {
		try {
			if (ecmcNewsService.timeFlag(newsSendVOE.getSendDate().getTime())) {
				respJson.setMessage("该条消息已经生效！无法修改！");
				respJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			}
		} catch (Exception e) {
			log.error(e, e);
		}
		return respJson;
	}

	/**
	 * 修改消息
	 * 
	 * @param request
	 * @param newsSendVOE
	 * @param respJson
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("updateNewsSendVOE")
	@ResponseBody
	public Object updateNewsSendVOE(HttpServletRequest request, @RequestBody NewsSendVOE newsSendVOE,
			EayunResponseJson respJson) throws AppException {
		try {
			ecmcNewsService.editNewsRec(newsSendVOE);
			ecmcNewsService.edit(newsSendVOE);
			respJson.setMessage("消息修改成功！");
			respJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmclogservice.addLog("修改消息", "消息", newsSendVOE.getNewsTitle(), null, 1, newsSendVOE.getId(), null);
		} catch (Exception e) {
			log.error(e, e);
			respJson.setMessage("消息修改失败！");
			respJson.setRespCode(ConstantClazz.ERROR_CODE);
			ecmclogservice.addLog("修改消息", "消息", newsSendVOE.getNewsTitle(), null, 0, newsSendVOE.getId(), e);
			throw new AppException("error.globe.system", e);
		}
		return respJson;
	}

	/**
	 * 根据ID删除消息
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("deleteById")
	@ResponseBody
	public Object deleteById(@RequestBody Map<String, String> params) throws AppException {
		EayunResponseJson respJson = new EayunResponseJson();
		Map<Boolean,String>map=ecmcNewsService.deleteById(params.get("id"));
				String str=map.get(true);
				String str1=map.get(false);
		if (null!=str) {
			respJson.setMessage(str);
			respJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmclogservice.addLog("删除消息", "消息", params.get("name"), null, 1, params.get("id"), null);
		} else if(null!=str1) {
			respJson.setMessage(str1);
			respJson.setRespCode(ConstantClazz.ERROR_CODE);
			ecmclogservice.addLog("删除消息", "消息", params.get("name"), null, 0, params.get("id"), null);
		}
		return respJson;
	}

	/**
	 * 获取消息相关计数
	 */
	@RequestMapping("/getCount")
	@ResponseBody
	public Object getCount(HttpServletRequest request, @RequestBody NewsSendVOE nsv) throws Exception {
		EayunResponseJson respJson = new EayunResponseJson();
		try {
			respJson.setData(ecmcNewsService.getCount(nsv));
			respJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			log.error(e, e);
			respJson.setMessage(e.getMessage());
			respJson.setRespCode(ConstantClazz.ERROR_CODE);
		}
		return respJson;
	}

	/**
	 * 更新两个下拉菜单款项
	 */
	@RequestMapping("/getList")
	@ResponseBody
	public Object getCustomer(HttpServletRequest request) throws Exception {
		EayunResponseJson respJson = new EayunResponseJson();
		try {
			respJson.setData(ecmcNewsService.getList());
			respJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			log.error(e, e);
			respJson.setMessage(e.getMessage());
			respJson.setRespCode(ConstantClazz.ERROR_CODE);
		}
		return respJson;
	}

}
