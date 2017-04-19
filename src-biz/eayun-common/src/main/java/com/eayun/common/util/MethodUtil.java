package com.eayun.common.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.eayun.common.annotation.GeneralMethod;
import com.eayun.common.constant.ApiConstant;

@Component
public class MethodUtil {
	private static final String ERROR_INFO = "系统当前还未配置任何不需unescape的方法" ;
    private static final Logger log = LoggerFactory.getLogger(MethodUtil.class);
    private static Map<String,String> METHOD_MAPPING;
   
    static {
        if (METHOD_MAPPING == null) {
            METHOD_MAPPING = getServiceClasses(ApiConstant.API_SERVICE_PACKAGE_NAME) ;
            Integer size = METHOD_MAPPING.size();
            if (size == 0) {
                log.error(ERROR_INFO);
            }else {
                log.info("All Method : " + METHOD_MAPPING);
                log.info("All Method Count : " + size);
            }
        }
    }
    
    
    
    public static Map<String, String> getServiceClasses(String pack) {
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
        } catch (IOException e) {
        	e.printStackTrace();
        }
     
        Map<String, String> methodMap=new HashMap<String,String>();
        for (Class c : classes){
            for (Method method : c.getDeclaredMethods()) {
                if (method.isAnnotationPresent(GeneralMethod.class)){
                	methodMap.put(method.getName(),c.getName());
                }
            }
            
       }
        return methodMap;
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
    
    
    
    
	public static Map<String, String> getMETHOD_MAPPING() {
		return METHOD_MAPPING;
	}
	public static void setMETHOD_MAPPING(Map<String, String> mETHOD_MAPPING) {
		METHOD_MAPPING = mETHOD_MAPPING;
	}
 
}