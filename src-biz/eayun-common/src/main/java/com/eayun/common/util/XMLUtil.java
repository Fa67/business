package com.eayun.common.util;

import net.sf.json.xml.XMLSerializer;

public class XMLUtil {
    
    public static String xml2JSON(String xml){
        return new XMLSerializer().read(xml).toString();
    }

}
