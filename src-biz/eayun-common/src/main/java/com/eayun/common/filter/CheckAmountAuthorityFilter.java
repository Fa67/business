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
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.model.RespJSON;

/**
 * 金额相关Controller的权限过滤器<br/>
 * 用于判断请求我们配置的金额相关Controller的用户是否具备管理员权限，如果没有，则将请求拦截掉
 *
 * @author zhangfan
 * @version 2016-09-01
 */
public class CheckAmountAuthorityFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(CheckAmountAuthorityFilter.class);

    private String filterName;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //初始化时获取配置的金额相关的.do
        filterName = filterConfig.getInitParameter("filterName");
        log.info("Filter Name <----------------> " + filterName);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession();
        SessionUserInfo sessionUserInfo = (SessionUserInfo) session.getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        HttpServletResponse resp = (HttpServletResponse) response;

            if(null != sessionUserInfo){
                String roleName = sessionUserInfo.getRoleName();
                if("普通用户".equals(roleName)){
                    //如果当前是普通用户，则请求被拦截
                    RespJSON respJson = new RespJSON();
                    respJson.setType("CheckAmountAuthorityFilter");
                    respJson.setCode(ConstantClazz.FILTER_ERROR_CODE);
                    PrintWriter writer = resp.getWriter();
                    log.info("Current user [" + sessionUserInfo.getUserName() + "] is not allowed to access uri [" + req.getRequestURI() + "]");
                    String msg = req.getHeader("Authorization");
                    if ("angularJS".equals(msg)) {
                        writer.print(JSONObject.toJSONString(respJson).toString());
                    } else {
                        writer.print("<script>location.href=\"" + req.getContextPath() + "\"</script>");
                    }
                    writer.flush();
                    writer.close();
                    return ;
                }
            }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        filterName = null;
    }
}
