package com.eayun.common.listener;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 *                       
 * @Filename: SessionListener.java
 * @Description: 
 * @Version: 1.0
 * @Author: chenhao
 * @Email: hao.chen@eayun.com
 * @History:<br>
 *<li>Date: 2016年5月19日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class SessionListener implements HttpSessionListener, HttpSessionAttributeListener {

    private static final Logger log = LoggerFactory.getLogger(SessionListener.class);

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        log.info("session创建:" + event.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        log.info("session销毁:" + event.getSession().getId());
    }

    @Override
    public void attributeAdded(HttpSessionBindingEvent event) {
        log.info("session:" + event.getSession().getId() + ",添加属性:" + event.getName() + ",设置为："
                 + event.getValue());
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent event) {
        log.info("session:" + event.getSession().getId() + ",移除属性:" + event.getName());
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent event) {
        log.info("session:" + event.getSession().getId() + ",更改属性:" + event.getName()+ ",设置为："
                + event.getSession().getAttribute(event.getName()));
    }

}
