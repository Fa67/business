package com.eayun.ecmcauthority.filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eayun.common.ConstantClazz;
import com.eayun.ecmcauthority.model.BaseEcmcSysAuthority;
import com.eayun.ecmcuser.util.EcmcSessionUtil;

/**
 * @Author fangjun.yang
 * @Date 2016年3月8日
 */
public class HttpAuthFilter implements Filter {

	private final static Logger log = LoggerFactory.getLogger(HttpAuthFilter.class);
	/**
	 * 不需要过滤的url
	 */
	private String  unfilterurl;
	/**
	 * 过滤器名称
	 */
    private String  name;
    /**
     * 不需要过滤的url集合
     */
    private String  unfiltersUrlArray[];
    
    /**
     * 只需要登录 就可访问的url
     */
    private String onlyNeedLoginUrlArray[];

	public void init(FilterConfig filterConfig) throws ServletException {
		name = filterConfig.getInitParameter("name");
		log.info("初始化过滤器：{}", name);
		
		unfilterurl = filterConfig.getInitParameter("unfilterUrl");
	    unfiltersUrlArray = unfilterurl.split(ConstantClazz.SPLITCHARACTER);
	    
	    // 只需要登录 就可访问的url
	    String onlyNeedLoginUrl = filterConfig.getInitParameter("onlyNeedLoginUrl");
	    if (StringUtils.isNotBlank(onlyNeedLoginUrl)) {
	    	onlyNeedLoginUrlArray = onlyNeedLoginUrl.split(ConstantClazz.SPLITCHARACTER);
	    }
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		HttpSession session = req.getSession();
		String servletPath = req.getServletPath();
		if(isUrlinArray(unfiltersUrlArray, servletPath)){
			// 不拦截
			chain.doFilter(request, response);
			return;
		}
		
		// 开始拦截逻辑
		if(EcmcSessionUtil.getUser() != null){//已登录
			if (isUrlinArray(onlyNeedLoginUrlArray, servletPath)) {
				// 登录即可
				chain.doFilter(request, response);
			} else {
				// 需要验证权限
				if(hasPermissionInAuths(servletPath, EcmcSessionUtil.getUserAuths())){
					chain.doFilter(request, response);
				}else{
					log.info("当前session："+session.getId()+",未通过权限验证，uri:" + servletPath);
					resp.sendError(HttpServletResponse.SC_FORBIDDEN, "无权访问");
				}
			}
		}else{//未登录
			log.info("当前session："+session.getId()+",未通过登录验证，uri:" + servletPath);
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未登录");
		}
			
	}
	
	public void destroy() {
		unfilterurl = null;
        name = null;
        unfiltersUrlArray = null;
        onlyNeedLoginUrlArray = null;
	}
	
	protected boolean hasPermissionInAuths(String uri, List<BaseEcmcSysAuthority> auths){
		if(CollectionUtils.isEmpty(auths)){
			return false;
		}
		for (BaseEcmcSysAuthority baseEcmcSysAuthority : auths) {
			if(StringUtils.contains(baseEcmcSysAuthority.getPermission(), uri))
				return true;
		}
		return false;
	}
	
	/**
     * 检测url是否在array中
     * 
     * @param urlArray
     * @param url
     * @return
     */
    private boolean isUrlinArray(String urlArray[], String url) {
    	if (urlArray == null || urlArray.length == 0) {
    		return false;
    	}
    	
    	String urlArrayItem = null;
    	for (int i = 0; i < urlArray.length; i++) {
    		urlArrayItem = urlArray[i];
    		if (urlArrayItem == null) {
    			continue;
    		}
    		if (StringUtils.equals(urlArrayItem.trim(), url)) {
    			return true;
    		}
		}
    	
    	return false;
    }
	
}
