package com.eayun.customer.filter;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class SystemConfig {
    private static final Logger log = LoggerFactory.getLogger(SystemConfig.class);
	private final String fileName = "db.properties";
	
	/**
	 * 读取systemConfig.xml文件节点
	 * 
	 * @return
	 */
	public Map<String, String> findNodeMap() {

		Resource config = new ClassPathResource(fileName);
		Properties p  =   new  Properties();
		Map<String,String> map = new HashMap<String,String>();
		try {
			InputStream inputStream  = config.getInputStream();
			if(inputStream == null){
				log.error("配置文件读取失败","db.properties配置文件位置错误无法获取！");
				return map;
			}
			p.load(inputStream);
			map.put("imgUrl",  p.getProperty("imgUrl"));
			map.put("ecscUrl", p.getProperty("ecscUrl"));
			map.put("ecmcUrl", p.getProperty("ecmcUrl"));
		} catch (IOException e1) {
		    log.error(e1.getMessage(), e1);
		}
		return map;
		/* 读取配置xml配置文件
		 try {
			EhcacheInfo ehcache = EhcacheInfo.getInstance();
			Map<String, String> map = ehcache.getConfigMap();
			if (map.isEmpty()) {
				String fullPath = this.getClass().getResource("/").getPath() + fileName;
				fullPath = fullPath.replace("%20", " ");
				File f = new File(fullPath);
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document doc = (Document) builder.parse(f);
				NodeList nodelist = doc.getElementsByTagName("root");
				for (int i = 0; i < nodelist.getLength(); i++) {
					NodeList nodesub = nodelist.item(i).getChildNodes();
					for (int j = 0; j < nodesub.getLength(); j++) {
						Node nodes = nodesub.item(j);
						NamedNodeMap map1 = nodes.getAttributes();
						if (nodes.getNodeName().indexOf("#") < 0) {
							map.put(map1.getNamedItem("name").getNodeValue(),
									nodes.getTextContent());
						}
					}
				}
				ehcache.setConfigMap(map);
			}
			return map;
		} catch (Exception e) {
			
		}
		return null;*/
	}
}
