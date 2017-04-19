package com.eayun.versions.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.exception.AppException;
import com.eayun.customer.filter.SystemConfig;
import com.eayun.versions.ecmcservice.EcmcVersionsService;

@Transactional
@Service
public class EcmcVersionsServiceImpl implements EcmcVersionsService {
	private static final Logger log = LoggerFactory.getLogger(SystemConfig.class);
	private final String fileName = "versions.txt";
	
	private static List<String> map = new ArrayList<String>();
	@Override
	public Object queryVersions() throws AppException {

		if (map.size()==0) {
			this.findNodeMap();
		}

		return map;
	}

	/**
	 * 读取version.txt文件
	 * 
	 * @return
	 */
	public List<String> findNodeMap() {
		Resource config = new ClassPathResource(fileName);
		//p = new Properties();
		boolean fag = true;
		boolean fagone = true;
		InputStream inputStream = null;
		BufferedReader bf=null;
		
		try {
			inputStream = config.getInputStream();
			if (inputStream == null) {
				log.error("配置文件读取失败", "version.txt文件位置错误无法获取！");
				return map;
			}
			//p.load(inputStream);
			bf=new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			String linstr=null;
			while (null!=(linstr=bf.readLine())) {
				map.add(linstr);
				
			}
			
		} catch (IOException e1) {
			log.error(e1.getMessage(), e1);

		} finally {
			IOUtils.closeQuietly(inputStream);
		}
		return map;
	}

}
