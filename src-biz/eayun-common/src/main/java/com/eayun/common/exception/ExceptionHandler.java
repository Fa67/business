package com.eayun.common.exception;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.model.RespJSON;
import com.eayun.common.tools.ParamUtils;

/***
 * Title: 异常解析器<br>
 * 整合了工程中所有的异常页面，根据不同的渠道标识进行不同的页面跳转。
 * Description: 处理异常信息，对controller层抛出的异常进行处理、跳转<br>
 */
public class ExceptionHandler implements HandlerExceptionResolver {
    private static final Log           log        = LogFactory.getLog(ExceptionHandler.class);
    @SuppressWarnings("unused")
    private static Map<String, String> errorMsgZh = new HashMap<String, String>();

    @Autowired
    private MessageSource              messageResource;

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         Object obj, Exception exception) {
        log.error(exception,exception);

        ModelAndView mav = new ModelAndView();
        String locale = ParamUtils.getParameter(request, "locale");
        if ("".equals(locale)) {
            locale = "zh";
        }
        String busiCode = (String) ParamUtils.getAttribute(request, "busiCode");
        log.info("busiCode...." + busiCode);
        log.info("locale...." + request.getParameter("locale"));

        RespJSON rj = new RespJSON();
        String code = "010120";
        String msg = "系统繁忙,请稍后重试。";
        String msgEn = "The system is abnormal,Please try again later.";
        try {
            if (exception instanceof AppException) {
                rj.setType("AppException");
                AppException ae = (AppException) exception;
                String[] args = ae.getArgsMessage();
                StringBuffer sb = new StringBuffer();
                if(args!=null && args.length>0){
                    for(int i=0;i<args.length;i++){
                        sb.append(args[i]);
                        if(i!=args.length-1){
                            sb.append(",");
                        }
                    }
                    msg = sb.toString();
                }
                else
                	msg = ae.getErrorMessage();
            }else if (exception instanceof HttpRequestMethodNotSupportedException) {
                if (locale.equals("zh")) {//中文
                    msg = "您的访问不合法，请确认后重试！";
                } else {//英文
                    msg = "Your request is not legitimate!";
                }
            } else if (exception instanceof UndeclaredThrowableException) {
                AppException ae = (AppException) exception.getCause();
                if (ae.getArgsMessage() != null && ae.getArgsMessage().length != 0) {
                    log.info("系统发生了异常：" + ae.getArgsMessage()[0]);
                }
                if (!locale.equals("zh")) {//中文
                    msg = msgEn;
                }
            } else {
                log.error(exception, exception);
            }
        } catch (Exception e) {
            log.error(e, e);
        }
        rj.setCode(code);
        rj.setMessage(msg);
        request.setAttribute("errorJson", rj);
        try {
            response.getWriter().write(JSONObject.toJSONString(rj));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return mav;
    }

    public void setMessageResource(MessageSource messageResource) {
        this.messageResource = messageResource;
    }
}
