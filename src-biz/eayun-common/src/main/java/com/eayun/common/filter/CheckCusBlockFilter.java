package com.eayun.common.filter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.model.RespJSON;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.tools.SpringBeanUtil;

/**
 * @Filename: CheckCusBlockFilter.java
 * @Description: 在checkSessionFilter之后，对已登录的客户进行是否被冻结状态的拦截，主要拦截后台".do"方法进行返回
 * @Version: 1.0
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @date 2016年9月2日
 * 
 */
public class CheckCusBlockFilter implements Filter {

	private static final Logger log = LoggerFactory
			.getLogger(CheckCusBlockFilter.class);
	private static JedisUtil jedisUtil = null;
	private String unfilterurl;
	private String unfilters[];

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpSession session = req.getSession();
		SessionUserInfo sessionUserInfo = (SessionUserInfo) session
				.getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		if (null == sessionUserInfo) {// 未登录
			chain.doFilter(request, response);
		} else {// 已登录
			String cusId = sessionUserInfo.getCusId();
			if (jedisUtil == null) {
				jedisUtil = SpringBeanUtil.getBean(JedisUtil.class);
				log.info("==============过滤器CheckCusBlockFilter初始化jedisUtil成功==============");
			}
			try {
				if(isLogin(req)){
					chain.doFilter(request, response);
				}else{
					String blockStatus = jedisUtil.get(RedisKey.CUS_BLOCK + cusId);
					if ("true".equals(blockStatus)) {
						RespJSON respJson = new RespJSON();
						respJson.setType("CheckSessionFilter");
						respJson.setCode(ConstantClazz.FILTER_BLOCK_CODE);
						PrintWriter writer = response.getWriter();
						writer.print(JSONObject.toJSONString(respJson).toString());
						writer.flush();
						writer.close();
					} else {
						chain.doFilter(request, response);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public void destroy() {
		// Auto-generated method stub

	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		unfilterurl = filterConfig.getInitParameter("unfilterurl");
		unfilters = unfilterurl.split(";");
	}
	/**
	 * 对于session存在的情况下，不对白名单中的url进行处理。
	 * */
	private boolean isLogin(HttpServletRequest request){
		boolean flag = false;
		String reqURI = request.getRequestURI();
		String contextPath = request.getContextPath();
		reqURI = reqURI.replaceAll("//", "/");
		if (contextPath.length() > 1) {
            contextPath = (new StringBuilder(String.valueOf(contextPath))).append("/")
                .toString();
        } else {
            contextPath = "/";
        }
		contextPath = contextPath.replaceAll("//", "/");
		for(int i=0;i<unfilters.length;i++){
			String uri = unfilters[i].trim();
			if ("".equals(uri)) {
                continue;
            }
			uri = (new StringBuilder(String.valueOf(contextPath))).append(uri)
                    .toString();
			uri = uri.replaceAll("//", "/");
			if(uri.equals(reqURI)){
				flag = true;
				break;
			}
		}
		return flag;
	}
}
