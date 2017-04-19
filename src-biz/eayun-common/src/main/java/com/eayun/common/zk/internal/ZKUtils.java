package com.eayun.common.zk.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class ZKUtils {
    private static Object lock = new Object();
	private static final Logger log = LoggerFactory.getLogger(ZKUtils.class);
    private static  Map<String, String> zookeeperNodeMap = null;
    private static InputStream dbInputStream = null;
	private static final String fileName = "db.properties";
	/**
     * 获取zookeeper配置相关信息
     * @return
     */
    private static Map<String, String> findZookeeperNodeMap() {
        synchronized (lock) {
            if(null == zookeeperNodeMap){
		        zookeeperNodeMap = new HashMap<String,String>();
	            Resource re = new ClassPathResource(fileName);
	            try {
	                dbInputStream = re.getInputStream();
	            } catch (IOException e) {
	                log.error(e.getMessage(), e);
	            }
	            Properties p = new Properties();
	            try {
	                p.load(dbInputStream);
	                zookeeperNodeMap.put("zookeeperHost", p.getProperty("dubbo.registry.address"));
	            } catch (IOException e1) {
	                log.error(e1.getMessage(), e1);
	            }
            }
		}
		
		return zookeeperNodeMap;
	}
    /**
     * 获取zookeeper地址(例如:192.168.8.22:2181)
     * @return
     */
    public static String getEayunZookeeperHost (){
        synchronized (lock) {
            if(null==zookeeperNodeMap){
	            zookeeperNodeMap = findZookeeperNodeMap();
            }
		}
		String zkHost=zookeeperNodeMap.get("zookeeperHost");
		if(zkHost!=null&&zkHost.length()>0){
			if(zkHost.indexOf("zookeeper://")!=-1){
				zkHost=zkHost.replace("zookeeper://", "");
			}
			if(zkHost.indexOf("?backup=")!=-1){
				zkHost=zkHost.replace("?backup=", ",");
			}
		}
		return zkHost;
	}
}
