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

public class CheckSessionFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(CheckSessionFilter.class);

    private String  unfilterurl;
    private String  name;
    private String  unfilters[];
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        unfilterurl = filterConfig.getInitParameter("unfilterurl");
        name = filterConfig.getInitParameter("name");
        unfilters = unfilterurl.split(";");
        log.info("过滤器名称==============" + name);
    }
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException , ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession();
        SessionUserInfo sessionUserInfo = (SessionUserInfo) session.getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        HttpServletResponse resp = (HttpServletResponse) response;
        if(null == sessionUserInfo){    //未登录
            if(checkFileType(req) && !checkUriWithUrl(unfilters , req)){
                //检测的url类型,do,html,htm      !false:否（拦截范围）
                RespJSON  respJson = new RespJSON();
                respJson.setType("CheckSessionFilter");
                respJson.setCode(ConstantClazz.FILTER_ERROR_CODE);
                PrintWriter  writer = resp.getWriter();
                log.info("当前session:"+session.getId()+",未通过登录验证，uri:"+req.getRequestURI());
                String msg = req.getHeader("Authorization");
                if("angularJS".equals(msg)){
                	writer.print(JSONObject.toJSONString(respJson).toString());
                }
                else{
                	writer.print("<script>location.href=\""+req.getContextPath()+"\"</script>");
                }
                writer.flush();
                writer.close();
            }
            else{
            	chain.doFilter(request, response);
            }
        }
        else{
        	chain.doFilter(request, response);
        }
    }
    @Override
    public void destroy() {
        unfilterurl = null;
        name = null;
    }
    /**
     * 检测是否在不拦截范围内
     *      true:是（不拦截范围）
     *      false:否（拦截范围）
     * @param urls
     * @param request
     * @return
     */
    private boolean checkUriWithUrl(String urls[], HttpServletRequest request) {
        boolean result = false;
        String uri = request.getRequestURI();

        if (urls != null && urls.length > 0) {
            String contextPath = request.getContextPath();
            uri = uri.replaceAll("//", "/");
            //log.info("当前访问的 uri=" + uri);
            if (contextPath.length() > 1) {
                contextPath = (new StringBuilder(String.valueOf(contextPath))).append("/")
                    .toString();
            } else {
                contextPath = "/";
            }
            contextPath = contextPath.replaceAll("//", "/");
            for (int i = 0; i < urls.length; i++) {
                String tempUrl = urls[i].trim();
                if ("".equals(tempUrl)) {
                    continue;
                }
                tempUrl = (new StringBuilder(String.valueOf(contextPath))).append(tempUrl)
                    .toString();
                tempUrl = tempUrl.replaceAll("//", "/");
                // 验证当前访问的url是否在容器中，是就返回true否则false；
                if (uri.startsWith(tempUrl)) {
                    return true;
                }
            }
        }
        log.info("当前拦截uri:"+uri);
        return result;
    }
    /**
     * 定义要进行检测的url类型，只对urlType数组里面定义的url特征才进行过滤器逻辑验证。
     * 
     * @param request
     * @return 2013-05 by chenyl
     */
    private boolean checkFileType(HttpServletRequest request) {
        String uri = request.getRequestURI();
        uri = uri.replaceAll("//", "/");
        if(uri.endsWith("/")){
            return false;
        }else{
            return true;
        }

    }
    public void sendRedirect(HttpServletRequest request, HttpServletResponse response,
                             String redirectPath) throws IOException {
        log.info("sendRedirect:--"+request.getRequestURI());
        try {
            if (redirectPath.indexOf("http://") >= 0 || redirectPath.indexOf("https://") >= 0) {
                response.sendRedirect(redirectPath);
            } else {
                String contextPath = request.getScheme()+"://"+request.getServerName()+
                        ":"+request.getServerPort()+request.getContextPath();
                if (contextPath.length() > 1)
                    contextPath = (new StringBuilder(String.valueOf(contextPath))).append("/")
                        .toString();
                else
                    contextPath = "/";
                contextPath = (new StringBuilder(String.valueOf(contextPath))).append(redirectPath)
                    .toString();
                log.info("contextPath:" + contextPath);
                response.sendRedirect(contextPath);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
        }
    }

}
