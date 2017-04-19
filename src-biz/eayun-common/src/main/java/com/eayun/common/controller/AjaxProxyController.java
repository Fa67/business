package com.eayun.common.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.model.RespJSON;

/**
 * ajax请求辅助类
 */
@Controller
public class AjaxProxyController {
	/**
	 * @param request
	 * @param response
	 *            由于日志引入了spring
	 *            aop，controller层如果不抛出异常的话，导致aop无法捕获异常，如果抛出异常，调用out.print会失效，
	 *            所以创建了该辅助类，在controller异常抛出之后先记录日志，
	 *            然后由ExceptionHandler捕获后发现request设置了ajaxMsg时
	 *            ，跳转到/ajax/exception.do， 将错误信息输出到前端
	 */
	@RequestMapping(value = "/ajax/excepion")
	@ResponseBody
	public String writeMsg(HttpServletRequest request,
			HttpServletResponse response) {
		RespJSON rj = (RespJSON) request.getAttribute("errorJson");
		return JSONObject.toJSONString(rj);
	}
}
