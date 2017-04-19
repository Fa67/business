package com.eayun.common.httpclient;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//用于进行Https请求的HttpClient  
public class SSLClient {
    private static final Logger log = LoggerFactory.getLogger(CloseableHttpClient.class);
    
    public static CloseableHttpClient createSSLClientDefault() {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null,
                new TrustStrategy() {
                    //信任所有
                    public boolean isTrusted(X509Certificate[] chain, String authType)
                                                                                      throws CertificateException {
                        return true;
                    }
                }).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,NoopHostnameVerifier.INSTANCE);
            return HttpClients.custom().setSSLSocketFactory(sslsf).build();
        } catch (KeyManagementException e) {
            log.error(e.getMessage(),e);
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(),e);
        } catch (KeyStoreException e) {
            log.error(e.getMessage(),e);
        }
        return HttpClients.createDefault();
    }
}