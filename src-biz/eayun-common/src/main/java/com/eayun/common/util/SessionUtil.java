package com.eayun.common.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Session工具
 * 
 * @author zhujun
 * @date 2016年8月18日
 *
 */
public class SessionUtil {

	/**
	 * 获取http request
	 * 
	 * <p>需要配置 org.springframework.web.context.request.RequestContextListener</p>
	 * 
	 * @author zhujun
	 *
	 * @return
	 */
	public static HttpServletRequest getRequest() {
		return getRequest(true);
	}

	/**
	 * 获取http request
	 * 
	 * <p>需要配置 org.springframework.web.context.request.RequestContextListener</p>
	 * 
	 * @author zhujun
	 *
	 * @return
	 */
	public static HttpServletRequest getRequest(boolean throwExceptionIfNull) {
		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		if(servletRequestAttributes == null ){
			if(throwExceptionIfNull){
				throw new RuntimeException("无法获取http request,请确保在servlet线程中执行,并配置 org.springframework.web.context.request.RequestContextListener");
			}else{
				return null;
			}
		}
		HttpServletRequest request = servletRequestAttributes.getRequest();
		if(throwExceptionIfNull && request == null){
			throw new RuntimeException("无法获取http request,请确保在servlet线程中执行,并配置 org.springframework.web.context.request.RequestContextListener");
		}
		
		return request;
	}
	
	/**
	 * 获取http session
	 * 
	 * <p>需要配置 org.springframework.web.context.request.RequestContextListener</p>
	 * 
	 * @author zhujun
	 *
	 * @return
	 */
	public static HttpSession getSession() {
		HttpServletRequest httpServletRequest = getRequest(false);
		if(httpServletRequest == null){
			return null;
		}else{
			return httpServletRequest.getSession();
		}
	}
	
}
