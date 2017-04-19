package com.eayun.common.httpclient;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpClientFactory {
    
    public static CloseableHttpClient getHttpClient(boolean http){
        if(http){
            return HttpClients.createDefault();
        }else{
            return SSLClient.createSSLClientDefault();
        }
    }
}
