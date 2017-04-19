package com.eayun.common.util;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.annotation.ApiMethod;
import com.eayun.common.annotation.ApiService;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.exception.ApiException;
import com.eayun.common.exception.ErrorType;
import com.eayun.common.constant.ApiConstant;
import com.eayun.common.model.ApiServiceLog;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.tools.DictUtil;
import com.eayun.sys.model.SysDataTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Component
public class ApiUtil {
    private static final String ERROR_INFO = "系统当前还未配置任何的业务模块,请配置后重启API服务." ;
    private static final Logger logger = LoggerFactory.getLogger(ApiUtil.class);
    private static Properties ERR_CODE_MESSAGE;
    private static Properties ACTION_METHOD_MAPPING;
    private static final Long REDISKEY_TIMEOUT = 90L ;
    private static ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = new ThreadLocal<SimpleDateFormat>(){
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMddHHmm");//时间精确到分
        }
    } ;
    static {
        if (ERR_CODE_MESSAGE == null) {
            ERR_CODE_MESSAGE = new Properties() ;
            InputStream inputStream = ApiUtil.class.getClassLoader().getResourceAsStream(
                    ApiConstant.ERROR_CODE_MESSAGE_MAPPING_PROPERTY_FILE
            );
            try {
                ERR_CODE_MESSAGE.load(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (ACTION_METHOD_MAPPING == null) {
            ACTION_METHOD_MAPPING = getApiServiceClasses(ApiConstant.API_SERVICE_PACKAGE_NAME) ;
            Integer size = ACTION_METHOD_MAPPING.size() ;
            if (size == 0) {
                logger.error(ERROR_INFO);
            }else {
                logger.info("All Service : " + ACTION_METHOD_MAPPING);
                logger.info("All Service Count : " + size);
            }
        }
    }

    /**
     * 取得Redis中需要用到的时间戳字符串
     * @return
     */
    public static String redisKeyTimestamp(Date date){
        return dateFormatThreadLocal.get().format(date);
    }

    public static String getMappingMessageByAction(String action){
        return ACTION_METHOD_MAPPING.getProperty(action, null);
    }
    public static String getErrMsgByErrCode(String errCode){
        return String.valueOf(ERR_CODE_MESSAGE.get(errCode));
    }
    public static ApiException createApiException(String errorCode) {
        String[] msgs = getErrMsgByErrCode(errorCode).split(":");
        return new ApiException(null, errorCode, msgs[1], ErrorType.valueOf(msgs[0]));
    }
    public static String postContent(InputStream inputStream){
        try {
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
            inputStream.close();
            return stringBuilder.toString() ;
        }catch (Exception e){
            e.printStackTrace();
            return null ;
        }
    }
    public static Properties getApiServiceClasses(String pack) {
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        boolean recursive = true;
        String packageName = pack;
        String packageDirName = packageName.replace('.', '/');
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(
                    packageDirName);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    cycleTask(packageName, filePath,
                            recursive, classes);
                } else if ("jar".equals(protocol)) {
                    JarFile jar;
                    try {
                        jar = ((JarURLConnection) url.openConnection())
                                .getJarFile();
                        Enumeration<JarEntry> entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            if (name.charAt(0) == '/') {
                                name = name.substring(1);
                            }
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                if (idx != -1) {
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                if ((idx != -1) || recursive) {
                                    if (name.endsWith(".class")
                                            && !entry.isDirectory()) {
                                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                                        String resolveClassName = packageName + '.' + className;
                                        try {
                                            classes.add(Class.forName(resolveClassName));
                                        } catch (Throwable e) {
                                            System.out.println(resolveClassName + " ---> " + e.getMessage());
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {e.printStackTrace();}
        Properties properties = new Properties();
        Set<String> temp = new LinkedHashSet<String>() ;
        for (Class c : classes){
            if (c.isAnnotationPresent(ApiService.class)) {
                if ((!Modifier.isInterface(c.getModifiers())) && (!Modifier.isAbstract(c.getModifiers()))) {
                    for (Method method : c.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(ApiMethod.class)){
                            if (JSONObject.class != method.getReturnType()){
                                logger.error("类:" + c.getName() + " 中的方法:" + method.getName() + " 返回值类型必须为JSONObject,请检查修改!");
                            }
                            Class[] clss = method.getParameterTypes() ;
                            if (clss.length == 1 && JSONObject.class == clss[0]) {
                                String actionValue = method.getAnnotation(ApiMethod.class).value();
                                if (actionValue == null || "".equals(actionValue.trim())) {
                                    logger.error("类:" + c.getName() + " 中的方法:" + method.getName() + " Action值不允许为空,请检查!");
                                } else if (!checkVersionIsRight(actionValue)) {
                                    logger.error("类:" + c.getName() + " 中的方法:" + method.getName() + " Action值没有携带版本号或者版本号错误或版本号大小写拼写错误,请检查后修改重试!");
                                } else {
                                    if (temp.contains(actionValue)) {
                                        logger.error("类:" + c.getName() + " 中的方法:" + method.getName() + " 配置了重复的Action值，请检查系统中配置的其它服务是否有重名!");
                                    } else {
                                        temp.add(actionValue);
                                        properties.put(actionValue, c.getName()+":"+method.getName());
                                    }
                                }
                            }else {
                                logger.error("类:" + c.getName() + " 中的方法:" + method.getName() + " 参数必须为JSONObject类型，并且只有一个参数，请检查！");
                            }
                        }
                    }
                }else {
                    logger.error("类: " + c.getName() + " 注解错误，对不起，@ApiService接口只能修饰业务的实现类，不能用于抽象类或者接口！") ;
                }
            }
        }
        temp = null ;
        return properties ;
    }
    private static void cycleTask(String packageName, String packagePath, final boolean recursive, Set<Class<?>> classes) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] dirfiles = dir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return (recursive && file.isDirectory())
                        || (file.getName().endsWith(".class"));
            }
        });
        for (File file : dirfiles) {
            if (file.isDirectory()) {
                cycleTask(packageName + "."
                                + file.getName(), file.getAbsolutePath(), recursive, classes);
            } else {
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private static  boolean checkVersionIsRight(String actionValue){
        for (String versionPrefix : ApiConstant.VERSION_PREFIX.split(",")){
            if (actionValue.startsWith(versionPrefix)){
                return true ;
            }
        }
        return false;
    }

    /**
     * 根据查询关键字查询所有对应的NodeId集合
     * @param name
     * @return
     */
    public static String getNodeIdByKeywordSearch(String version, String name){
        String actionRootNodeId = "0016001" ;
        StringBuilder builder = new StringBuilder() ;
        for (SysDataTree sysDataTree : DictUtil.getDataTreeByParentId(actionRootNodeId)) {
            if (version.equals(sysDataTree.getNodeName())) {
                for (SysDataTree sysDataTree1 : DictUtil.getDataTreeByParentId(sysDataTree.getNodeId())) {
                    for (SysDataTree sysDataTree2 : DictUtil.getDataTreeByParentId(sysDataTree1.getNodeId())) {
                        if (sysDataTree2.getNodeNameEn().toLowerCase().contains(name.toLowerCase())) {
                            builder.append(sysDataTree2.getNodeId());
                            builder.append(",");
                        }
                    }
                }
            }
        }
        if ("".equals(builder.toString().trim())){
            return null ;
        }else {
            return builder.toString().substring(0, builder.toString().length() - 1);
        }
    }

    /**
     * 获取指定版本的API名称或者API类型的NodeId编号
     * @param version       版本号
     * @param name          API名称或者API类型
     * @param isApiType     是否是API名称还是API类型的一个标识
     * @return
     */
    public static String getNodeIdByRedisData(String version, String name, boolean isApiType){
        String actionRootNodeId = "0016001" ;
        if (StringUtil.isEmpty(version) || StringUtil.isEmpty(name)){
            return "-----" ;
        }
        for (SysDataTree sysDataTree : DictUtil.getDataTreeByParentId(actionRootNodeId)) {
            if (version.equals(sysDataTree.getNodeName())) {
                if (isApiType) {
                    for (SysDataTree sysDataTree1 : DictUtil.getDataTreeByParentId(sysDataTree.getNodeId())) {
                        if (name.equals(sysDataTree1.getNodeName())) {
                            return sysDataTree1.getNodeId();
                        }
                    }
                } else {
                    for (SysDataTree sysDataTree1 : DictUtil.getDataTreeByParentId(sysDataTree.getNodeId())) {
                        for (SysDataTree sysDataTree2 : DictUtil.getDataTreeByParentId(sysDataTree1.getNodeId())) {
                            if (name.equals(sysDataTree2.getNodeName())) {
                                return sysDataTree2.getNodeId();
                            }
                        }
                    }
                }
            }
        }
        return "-----" ;
    }




    /**
     * API调用时同时记录必要的监控与报警数据
     * @param redisKeyTimestamp
     * @param log
     */
    public static void storeRedisDataForAPILog(String redisKeyTimestamp, ApiServiceLog log){

        JedisUtil jedisUtil = JedisUtil.getInstance();
        String weiduMessage =
                          (log.getoperatorId() == null ? "-" : log.getoperatorId())
                        + ":"
                        + log.getip()
                        + ":"
                        + (log.getregionId()   == null ? "-" : log.getregionId()) ;
        logger.info("获取维度信息:" + redisKeyTimestamp + "," + weiduMessage);
        jedisUtil.increaseHMap(RedisKey.API_MONITORINGALARM_SERVICE_LOG_BASE_ALLNUMBER       + redisKeyTimestamp, weiduMessage, 1L);
        jedisUtil.increaseHMap(RedisKey.API_MONITORINGALARM_SERVICE_LOG_BASE_DATENUMBER      + redisKeyTimestamp, weiduMessage, log.gettakeTime());
        if (Integer.parseInt(log.getstatus()) == 1) {
        jedisUtil.increaseHMap(RedisKey.API_MONITORINGALARM_SERVICE_LOG_BASE_RIGHTNUMBER     + redisKeyTimestamp, weiduMessage, 1L);}
        if (!log.isServerError()) {
        jedisUtil.increaseHMap(RedisKey.API_MONITORINGALARM_SERVICE_LOG_BASE_AVAILABLENUMBER + redisKeyTimestamp, weiduMessage, 1L);}
        logger.info(" ----- ");
        logger.info("访问总次数:"   + jedisUtil.getHashMapKeyValue(RedisKey.API_MONITORINGALARM_SERVICE_LOG_BASE_ALLNUMBER       + redisKeyTimestamp, weiduMessage));
        logger.info("访问总时间:"   + jedisUtil.getHashMapKeyValue(RedisKey.API_MONITORINGALARM_SERVICE_LOG_BASE_DATENUMBER      + redisKeyTimestamp, weiduMessage));
        logger.info("访问正确次数:" + jedisUtil.getHashMapKeyValue(RedisKey.API_MONITORINGALARM_SERVICE_LOG_BASE_RIGHTNUMBER     + redisKeyTimestamp, weiduMessage));
        logger.info("访问可用次数:" + jedisUtil.getHashMapKeyValue(RedisKey.API_MONITORINGALARM_SERVICE_LOG_BASE_AVAILABLENUMBER + redisKeyTimestamp, weiduMessage));
        logger.info(" ----- ");
        //90L
        jedisUtil.expireKey(RedisKey.API_MONITORINGALARM_SERVICE_LOG_BASE_ALLNUMBER +       redisKeyTimestamp, REDISKEY_TIMEOUT);
        jedisUtil.expireKey(RedisKey.API_MONITORINGALARM_SERVICE_LOG_BASE_DATENUMBER +      redisKeyTimestamp, REDISKEY_TIMEOUT);
        jedisUtil.expireKey(RedisKey.API_MONITORINGALARM_SERVICE_LOG_BASE_AVAILABLENUMBER + redisKeyTimestamp, REDISKEY_TIMEOUT);
        jedisUtil.expireKey(RedisKey.API_MONITORINGALARM_SERVICE_LOG_BASE_RIGHTNUMBER +     redisKeyTimestamp, REDISKEY_TIMEOUT);
    }
}