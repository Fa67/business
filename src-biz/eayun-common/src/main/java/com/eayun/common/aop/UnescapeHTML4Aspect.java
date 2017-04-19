package com.eayun.common.aop;

import org.apache.commons.lang3.StringEscapeUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Repository;

import com.eayun.common.util.MethodUtil;

/**
 * 逆转义字符串，防XSS攻击
 *                       
 * @Filename: UnescapeController.java
 * @Description: 
 * @Version: 1.0
 * @Author: fan.zhang
 * @Email: fan.zhang@eayun.com
 * @History:<br>
 *<li>Date: 2015年11月19日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Aspect
@Repository
public class UnescapeHTML4Aspect {

    @Around("execution(public String com.eayun.*.controller.*Controller.*(..))")
    //这里只针对返回值为String类型的controller进行unescape
    public Object unescape(ProceedingJoinPoint pjp) throws Throwable {
    	
    	//判断有没有不需进行unescape的方法
    	int count=MethodUtil.getMETHOD_MAPPING().size();
		if(count>0){
			String methodName=pjp.getStaticPart().getSignature().getName().toString();
	    	String className=pjp.getStaticPart().getSignature().getDeclaringType().toString();
	    	if(null!=methodName&&!"".equals(methodName)){
    			String unescapeName=MethodUtil.getMETHOD_MAPPING().get(methodName);
    			if(null!=unescapeName&&!"".equals(unescapeName)){
    				String[] args=className.split(" ");
    				if(args.length>0){
    					if(unescapeName.equals(args[1].toString().trim())){
    						return pjp.proceed();
    					}
    				}
    			}
	    	}
		}
    	

        Object originalData = pjp.proceed();
        if (originalData != null && originalData instanceof String) {
            originalData = StringEscapeUtils.unescapeHtml4(originalData.toString());
        }
        return originalData;
        
    }

}
