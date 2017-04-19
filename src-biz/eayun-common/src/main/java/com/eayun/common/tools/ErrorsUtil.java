package com.eayun.common.tools;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.eayun.common.constant.ApiConstant;

@SuppressWarnings("rawtypes")
public class ErrorsUtil {
    private static final Logger log = LoggerFactory.getLogger(ErrorsUtil.class);
	private static Map<String, String> map;
	
	private static Map<String, String> apiErrorMap;

	private Map<String, String> parserXml() {
		map = new HashMap<String, String>();
		String rootPath = getClass().getResource("/").getFile().toString();
		try {
			File f = new File(rootPath + "/errors.xml");
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(f);
			NodeList nl = doc.getElementsByTagName("Error");
			for (int i = 0; i < nl.getLength(); i++) {

				map.put(doc.getElementsByTagName("error").item(i)
						.getFirstChild().getNodeValue(), doc
						.getElementsByTagName("transfer").item(i)
						.getFirstChild().getNodeValue());
			}
		} catch (Exception e1) {
		    log.error(e1.getMessage(), e1);
		}
		return map;

	}

	public String[] transfer(String[] args) {
		if (map == null) {
			map = this.parserXml();
		}

		Iterator it = map.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			String[] errs = {};
			String r = null;

			if (key.contains("@")) {
				int flag = 0;
				key=key.trim();
				errs = key.split("@");
				for (int j = 0; j < args.length; j++) {
					r = args[j];
					for (int i = 0; i < errs.length; i++) {
						if (r.contains(errs[i])) {
							flag++;
						}
					}
				}
				if (flag >= errs.length) {
					String[] ss = { map.get(key) };
					return ss;
				}

			} else {
				for (int i = 0; i < args.length; i++) {
					r = args[i];
					if (r.contains(key)) {
						String[] ss = { map.get(key) };
						return ss;

					}
				}
			}

		}
		return args;

	}
	
	private static Map<String,String> loadApiErrorMap(){
		apiErrorMap = new HashMap<String,String>();
		
		String rootPath = ErrorsUtil.class.getClassLoader().getResource("/").toString();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(rootPath +"api_errors.xml");
			NodeList nl = doc.getElementsByTagName("Error");
			for (int i = 0; i < nl.getLength(); i++) {
				apiErrorMap.put(doc.getElementsByTagName("error").item(i)
						.getFirstChild().getNodeValue(), doc
						.getElementsByTagName("code").item(i)
						.getFirstChild().getNodeValue());
			}
		} catch (Exception e) {
		    log.error(e.getMessage(), e);
		}
		return apiErrorMap;
	}
	
	public static String escapeApiErrCode(String appExcepMsg){
		String errCode = ApiConstant.INTERNAL_ERROR_CODE;
		if(null == apiErrorMap || apiErrorMap.size() ==0){
			apiErrorMap = loadApiErrorMap();
		}
		
		Iterator it = apiErrorMap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			String[] errs = {};

			if (key.contains("@")) {
				int flag = 0;
				key=key.trim();
				errs = key.split("@");
				for (int i = 0; i < errs.length; i++) {
					if (appExcepMsg.contains(errs[i])) {
						flag++;
					}
				}
				if (flag >= errs.length) {
					errCode = apiErrorMap.get(key);
					return errCode;
				}

			} else {
				if (appExcepMsg.contains(key)) {
					errCode = apiErrorMap.get(key);
					return errCode;
				}
			}
		}
		return errCode;
	}

}
