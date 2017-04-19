/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.alipay.util;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *                       
 * @Filename: AlipayXmlUtil.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2016年8月12日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class AlipayXmlUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(AlipayXmlUtil.class);
    
    public static Map<String, Object> xml2Map(String xmlText) {
        Document doc = xml2Document(xmlText);
        return doc == null ? null : elms2map(doc.getRootElement());
    }

    public static Document xml2Document(String xmlText) {
        try {
            return xmlText == null ? null : DocumentHelper.parseText(xmlText);
        } catch (DocumentException e) {
            logger.error("resolve xmlText Failed", e);
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static Map<String, Object> elms2map(Element nodeElement) {
        if (nodeElement == null) {
            return new LinkedHashMap<String, Object>();
        }
        Map<String, Object> rootMap = new LinkedHashMap<String, Object>();
        Map<String, Object> childMap = new LinkedHashMap<String, Object>();
        for (Iterator it = nodeElement.elements().iterator(); it.hasNext();) {
            Element elm = (Element) it.next();
            if (elm.elements().size() > 0) {
                childMap.put(elm.getName(), elms2map(elm).get(elm.getName()));
            } else {
                if (elm.attributeCount() > 0) {
                    childMap.put(elm.attributeValue("name"), elm.getText());
                } else {
                    childMap.put(elm.getName(), elm.getText());
                }
            }
        }
        rootMap.put(nodeElement.getName(), childMap.size() > 0 ? childMap : nodeElement.getText());
        return rootMap;
    }
}
