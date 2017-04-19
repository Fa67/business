package com.eayun.common.controller;

import java.lang.reflect.Field;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

public class BaseController {

    private static final Logger log = LoggerFactory
            .getLogger(BaseController.class);
    /**
     * 在进入@RequestMapping方法之前对输入的数据做转义，防止XSS攻击
     * 
     * @param binder
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        Object target = binder.getTarget();
        if (target instanceof Map) {
            escapeValueOfMap(target);
        } else {
            try {
                escapePropertiesOfBean(target);
            } catch (Exception e) {
                log.error(e.getMessage(),e);
            }
        }

    }

    @SuppressWarnings("rawtypes")
    private void escapePropertiesOfBean(Object target) throws Exception {
        if(target == null){
            return;
        }
        Class targetClass = target.getClass().getSuperclass();
        Field[] fields = targetClass.getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            f.setAccessible(true);

            String type = f.getType().toString();
            if (type.endsWith("String")) {
                Object value = f.get(target);
                if (value != null) {
                    String escapedValue = StringEscapeUtils.escapeHtml4(String.valueOf(value));
                    f.set(target, escapedValue);
                }
            }
        }

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void escapeValueOfMap(Object target) {
        Map targetMap = (Map) target;
        for (Object key : targetMap.keySet()) {
            Object value = targetMap.get(key);
            if (value != null && value instanceof String) {
                String escapedValue = StringEscapeUtils.escapeHtml4(String.valueOf(value));
                targetMap.put(key, escapedValue);
            }
        }
    }

}
