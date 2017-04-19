package com.eayun.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringUtil {

    private static final Logger log = LoggerFactory.getLogger(StringUtil.class);

    public StringUtil() {
    }

    public static List<String> handleTagCondition(String name) {
        String[] strs = name.split(",");
        List<String> strList = new ArrayList<String>();
        if (null != strs && strs.length > 0) {
            for (String str : strs) {
                if (!StringUtils.isEmpty(str)) {
                    strList.add(str);
                }
            }
        }
        return strList;
    }

    /**
     * 是否空串
     * 
     * @param s
     * @return
     */
    public static boolean isEmpty(String s) {
        if (s == null || "".equals(s.trim())) {
            return true;
        }
        return false;
    }

    /**
     * 鬃贩创鬃换,贩种挝null
     * 
     * @param str
     *            创创览鬃贩创
     * @param defaultValue
     *            哪热照行适种
     * @return value 创览汉档照适
     */
    public static int getAsInt(String str, int defaultValue) {
        // 防止strParameterGeted为空
        int i_value;
        try {
            if (str == null || str.equals(""))
                i_value = defaultValue;
            else
                i_value = Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            i_value = 0;
        }
        return i_value;
    }

    /**
     * 是否是正确的email地址
     */
    public static boolean isEmail(String parameter) {
        if (parameter == null) {
            return false;
        }
        Pattern reEmail = null;
        Matcher matcher = null;
        try {
            reEmail = Pattern.compile("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$");
            matcher = reEmail.matcher(parameter);
            return matcher.find();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return true;
        }
    }
    
    /**
     * 过滤 \r \n \t \s 等符号
     * @param str
     * @return
     */
    public static String filterBlankChars(String str){
        return filterChars(str, "\\s*|\t|\r|\n");
    }
    
    /**
     * 过滤字符串中的特殊字符
     * @param str 需要过滤的字符串
     * @param regex 过滤表达式，例：\\s*|\t|\r|\n 去掉 空格、回车、换行符、制表符
     * @return
     */
    public static String filterChars(String str, String regex){
        if(str == null){
            return null;
        }
        String content = new String(str);
        Pattern p = Pattern.compile(regex);
        return p.matcher(content).replaceAll("");
    }
    
}
