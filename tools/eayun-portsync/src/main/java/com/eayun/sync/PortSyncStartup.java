package com.eayun.sync;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class PortSyncStartup {
	private final static Logger log = LoggerFactory.getLogger(PortSyncStartup.class);
	public static ClassPathXmlApplicationContext context = null;

	public static void main(String[] args) {
		try {
			String dcId = args[0];
			context = new ClassPathXmlApplicationContext("classpath*:spring/*.xml");

			Map<String, Object> map = getDataCenter(dcId);
			if (null != map) {
				String username = (String) map.get("v_center_username");
				String password = (String) map.get("v_center_password");
				String url = (String) map.get("dc_address");
				String prjId = (String) map.get("os_admin_project_id");

				JSONObject resJson = getToken(username, password, prjId, url);
				String tokenId = (((JSONObject) ((JSONObject) resJson.get("access")).get("token")).get("id"))
						.toString();

				String portUrl = url.substring(0, url.lastIndexOf(":")) + ":9696/v2.0/ports?device_owner=compute:None";
				log.info("调用底层的端口URL："+portUrl);
				JSONObject json = get(portUrl, tokenId);
				JSONArray array = json.getJSONArray("ports");
				if (array != null && array.size() > 0) {
					for (int i = 0; i < array.size(); i++) {
						JSONObject data = (JSONObject) array.get(i);
						String vmId = data.getString("device_id");
						String portId = data.getString("id");
						updateVm(portId, vmId);
					}
				}
				log.info("同步云主机端口成功，程序退出");
			} else {
				log.error(dcId + "---没有对应的数据中心信息，请确认数据中心ID是否正确");
			}

		} catch (Exception e) {
			log.error("同步云主机端口失败，失败信息：" + e.getMessage(),e);
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static JSONObject getToken(String userName, String password, String projectId, String url) {
		JSONObject jsonObject0 = new JSONObject();

		JSONObject jsonObject1 = new JSONObject();
		jsonObject1.put("username", userName);
		jsonObject1.put("password", password);

		jsonObject0.put("passwordCredentials", jsonObject1);
		jsonObject0.put("tenantId", projectId);

		JSONObject auth = new JSONObject();
		auth.put("auth", jsonObject0);

		HttpClient httpclient = HttpClients.createDefault();
		HttpPost post = new HttpPost(url + "/tokens");
		JSONObject resJson = null;
		String resData = null;
		int response = 0;
		try {
			StringEntity s = new StringEntity(auth.toString());
			s.setContentEncoding("UTF-8");
			s.setContentType("application/json");
			post.setEntity(s);
			post.setHeader("Accept", "application/json");
			HttpResponse res = httpclient.execute(post);
			response = res.getStatusLine().getStatusCode();
			HttpEntity resEntity = res.getEntity();
			if (resEntity != null) {
				resData = EntityUtils.toString(res.getEntity());
				if (resData != null) {
					resJson = JSONObject.parseObject(resData);
				}
			}
		} catch (Exception e) {
			log.error("获取底层token失败，失败信息：" + e.getMessage(),e);
			e.printStackTrace();
		}
		// 获取tokenId
		String tokenId = (((JSONObject) ((JSONObject) resJson.get("access")).get("token")).get("id")).toString();
		JSONArray array = (JSONArray) ((JSONObject) resJson.get("access")).get("serviceCatalog");
		log.info(resJson.toString());
		log.info("Token:" + tokenId);
		log.info("Services Array :" + array.toJSONString());
		return resJson;
	}

	private static JSONObject get(String serviceUrl, String tokenId) {
		HttpClient httpclient = HttpClients.createDefault();
		HttpGet get = new HttpGet(serviceUrl);
		log.info("请求url:" + serviceUrl);
		JSONObject resJson = null;
		String resData = null;
		@SuppressWarnings("unused")
		int response = 0;
		try {
			get.setHeader("Content-Type", "application/json");
			get.setHeader("Accept", "application/json");
			get.setHeader("X-Auth-Token", tokenId);
			HttpResponse res = httpclient.execute(get);
			response = res.getStatusLine().getStatusCode();
			HttpEntity resEntity = res.getEntity();
			if (resEntity != null) {
				resData = EntityUtils.toString(resEntity);
				if (resData != null) {
					try {
						resJson = JSONObject.parseObject(resData);
						log.info("返回信息：" + resJson);
					} catch (Exception e) {
						log.error("调用底层端口URL错误1，错误信息："+e.getMessage(),e);
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			log.error("调用底层端口URL错误1，错误信息："+e.getMessage(),e);
			e.printStackTrace();
		}
		return resJson;
	}

	public static void updateVm(String portId, String vmId) {
		JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
		StringBuffer sql = new StringBuffer();

		sql.append(" update cloud_vm set port_id = ? where vm_id = ?");

		jdbcTemplate.update(sql.toString(), new Object[] { portId, vmId });
	}

	public static Map<String, Object> getDataCenter(String dcId) {
		Map<String, Object> map = new HashMap<String, Object>();
		try{
			JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
			StringBuffer sql = new StringBuffer();
			
			sql.append("	SELECT                           ");
			sql.append("		id,                          ");
			sql.append("		dc_name,                     ");
			sql.append("		dc_address,                  ");
			sql.append("		os_admin_project_id,         ");
			sql.append("		v_center_username,           ");
			sql.append("		v_center_password            ");
			sql.append("	FROM                             ");
			sql.append("		dc_datacenter                ");
			sql.append("	WHERE                            ");
			sql.append("		id = ?                       ");
			
			map = jdbcTemplate.queryForMap(sql.toString(), new Object[] { dcId });
			if (null != map && map.size() > 0) {
				log.info(dcId + "数据中心的查询结果" + map);
				return map;
			}
		}catch(Exception e){
			log.error(dcId + "---没有对应的数据中心信息，请确认数据中心ID是否正确",e);
			e.printStackTrace();
			throw e;
		}

		return null;
	}
}
