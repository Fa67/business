package com.eayun.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;


/**
 * 对象存储工具类
 * 
 * @Filename: ObsUtil.java
 * @Description:
 * @Version: 1.0
 * @Author: yanchao.li
 * @Email: yanchao.li@eayun.com
 * @History:<br> <li>Date: 2016年1月11日</li> <li>Version: 1.0</li> <li>Content:
 *               create</li>
 * 
 */
@SuppressWarnings("deprecation")
public class ObsUtil {
    private static Object lock = new Object();
    
	private static InputStream dbInputStream = null;
	private static final String fileName = "db.properties";
	private static  Map<String, String> obsNodeMap = null;
	
	private static final Logger log = LoggerFactory.getLogger(ObsUtil.class);
	/**
	 * 获取对象存储的Host URL 得到的值为“obs.eayun.com:9090”
	 * @return URL
	 */
	public static String getEayunObsHost (){
	    synchronized (lock) {
	        if(null==obsNodeMap||null==obsNodeMap.get("eayunOBSHost")){
		        obsNodeMap = ObsUtil.findOBSNodeMap();
            }
		}
		return obsNodeMap.get("eayunOBSHost");
	}
	
	/**
	 * 获取对象存储的 obs.admin.access_key   得到的值类似为“asd3flk4jkf5dj5lsk6dfldkl”
	 * @return access_key
	 */
	public static String getAdminAccessKey (){
	    synchronized (lock) {
	        if(null==obsNodeMap||null==obsNodeMap.get("obs.admin.access_key")){
		        obsNodeMap = ObsUtil.findOBSNodeMap();
            }
		}
		return obsNodeMap.get("obs.admin.access_key");
	}
	
	/**
	 * 获取对象存储的 obs.admin.secret_key   得到的值类似为“asd3flk4jkf5dj5lsk6dfldkl”
	 * @return access_key
	 */
	public static String getAdminSecretKey (){
	    synchronized (lock) {
	        if(null==obsNodeMap||null==obsNodeMap.get("obs.admin.secret_key")){
		        obsNodeMap = ObsUtil.findOBSNodeMap();
            }
		}
		return obsNodeMap.get("obs.admin.secret_key");
	}
	
	/**
	 * 获取对象存储的请求前缀   得到的值为“http://”
	 * @return URL
	 */
	public static String getRequestHeader (){
	    synchronized (lock) {
	        if(null==obsNodeMap||null==obsNodeMap.get("eayunOBSRequest")){
		        obsNodeMap = ObsUtil.findOBSNodeMap();
            }
		}
		return obsNodeMap.get("eayunOBSRequest");
	}
	
	//添加eayunObsHost到配置文件
	private static Map<String, String> findOBSNodeMap() {
	    synchronized (lock) {
	        if(null == obsNodeMap){
                obsNodeMap = new HashMap<String,String>();
                Resource re = new ClassPathResource(fileName);
                try {
                    dbInputStream = re.getInputStream();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
                Properties p = new Properties();
                try {
                    p.load(dbInputStream);
                    obsNodeMap.put("eayunOBSHost", p.getProperty("obs.eayunOBSHost"));
                    obsNodeMap.put("eayunOBSRequest", p.getProperty("obs.eayunOBSRequest"));
                    obsNodeMap.put("obs.admin.access_key", p.getProperty("obs.admin.access_key"));
                    obsNodeMap.put("obs.admin.secret_key", p.getProperty("obs.admin.secret_key"));
                    
                } catch (IOException e1) {
                    log.error(e1.getMessage(), e1);
                }
            
            }
		}
		
		return obsNodeMap;
	}
	
	
	/** 构造 StringToSign
	 * 生成 Signature;
	 * 使用 HMAC(hash-based authentication code) 根据 StringToSign和SecretAccessKeyID生成Signature
	 */
	public static String getSignature(String httpVerb, String contentMD5,
			String contentType, String date,
			String canonicalizedEayunOBSHeaders, String canonicalizedResource) {
		StringBuffer sb = new StringBuffer();
		sb.append(httpVerb);
		sb.append("\n");
		sb.append(contentMD5);
		sb.append("\n");
		sb.append(contentType);
		sb.append("\n");
		sb.append(date);
		sb.append("\n");
		sb.append(canonicalizedEayunOBSHeaders);
		sb.append(canonicalizedResource);

		return sb.toString();
	}
	 
    /**
     * @param accessKey
     * @param secretKey
     * @return
     * @throws Exception
     */
    public static AmazonS3 createClient(String accessKey, String secretKey) throws Exception {
        String host = getEayunObsHost();
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        ClientConfiguration clientConfig = new ClientConfiguration();
        if ("http://".equals(getRequestHeader())) {
            clientConfig.setProtocol(Protocol.HTTP);
        } else {
            clientConfig.setProtocol(Protocol.HTTPS);
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null,
                new TrustStrategy() {
                    //信任所有
                    public boolean isTrusted(X509Certificate[] chain, String authType)
                                                                                      throws CertificateException {
                        return true;
                    }
                }).build();

            SSLSocketFactory ssf = new SSLSocketFactory(sslContext,
                SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            clientConfig.getApacheHttpClientConfig().setSslSocketFactory(ssf);
        }

        clientConfig.setSignerOverride("S3SignerType");//设置使用V2认证

        AmazonS3 client = new AmazonS3Client(credentials, clientConfig);
        client.setEndpoint(host);
        return client;
    }
}
