package com.demo.doubanApi.proxy;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author sprinng
 * @Description jdk >=1.7 兼容http https
 *
 */
public class HttpUtils {

    private static CloseableHttpClient httpClientBuilder=null;

    /**
     *    http 和 https
     * @param useProxy 是否使用代理
     * @param needCert 是否需要证书
     * @return
     */
    private static CloseableHttpClient createSSLClientDefault(boolean useProxy,boolean needCert) {
        SSLConnectionSocketFactory sslsf = null;
        try {
            if(needCert){
                InputStream instream = new FileInputStream(new File("D:/cert/client.p12"));
                InputStream instream1 = new FileInputStream(new File("D:/cert/tbb.jks"));
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                KeyStore trustStore = KeyStore.getInstance("JKS");
                try {
                    //设置客户端证书
                    keyStore.load(instream, "12345678".toCharArray());
                    //设置服务器证书
                    trustStore.load(instream1, "12345678".toCharArray());
                } catch (Exception e) {
                    ECommonUtil.getLog().error("导入证书错误" + e);
                } finally {
                    if (instream != null) {
                        instream.close();
                    }
                    if (instream1 != null) {
                        instream1.close();
                    }
                }
                SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(trustStore).loadKeyMaterial(keyStore, "12345678".toCharArray()).build();
                sslsf = new SSLConnectionSocketFactory(sslContext,SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            }else{
                SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
                    @Override
                    public boolean isTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {
                        return true;
                    }
                }).build();
                sslsf = new SSLConnectionSocketFactory(sslContext,new String []{"TLSv1.2"}, null,SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            }
            if(useProxy){
                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                AuthScope authScope = new AuthScope(PropertiesUtil.properties.getProperty("proxy_host"),Integer.parseInt(PropertiesUtil.properties.getProperty("proxy_port")));
                Credentials credentials = new  UsernamePasswordCredentials(PropertiesUtil.properties.getProperty("proxy_user"), PropertiesUtil.properties.getProperty("proxy_password"));
                credsProvider.setCredentials(authScope, credentials);
                httpClientBuilder= HttpClients.custom().setSSLSocketFactory(sslsf).setDefaultCredentialsProvider(credsProvider).build();
            }else{
                httpClientBuilder= HttpClients.custom().setSSLSocketFactory(sslsf).build();
            }
            return httpClientBuilder;
        } catch (Exception e) {
            ECommonUtil.getLog().error("创建https导入证书错误"+e);
        }
        return HttpClients.createDefault();
    }

    /**
     *
     * @param url 请求地址
     * @param map 请求参数
     * @param res 返回结果
     * @param timeOut 超时时间(min)
     * @param useProxy 是否使用代理
     * @return
     * @throws Exception
     */
    public static String get(String url,Map<String,String> map, String res, int timeOut, boolean useProxy, boolean needCert) throws Exception{
        RequestConfig config = null; CloseableHttpClient httpClient=null; CloseableHttpResponse response=null;
        if(httpClientBuilder==null){
            httpClient= HttpUtils.createSSLClientDefault(useProxy,needCert);
        }else{
            httpClient=httpClientBuilder;
        }
        URIBuilder uriBuilder=new URIBuilder(url);
        for (Entry<String, String> entry : map.entrySet()) {
            uriBuilder=uriBuilder.setParameter(entry.getKey(),entry.getValue());
        }
        URI uri=uriBuilder.build();
        HttpGet httpGet=new HttpGet(uri);
        try {
            if(useProxy){
                HttpHost proxy = new HttpHost(PropertiesUtil.properties.getProperty("proxy_host"), Integer.parseInt(PropertiesUtil.properties.getProperty("proxy_port")),"http");
                config = RequestConfig.custom().setProxy(proxy).setConnectTimeout(timeOut * 1000 * 60).build();
            }else{
                config = RequestConfig.custom().setConnectTimeout(timeOut * 1000 * 60).build();
            }

            httpGet.setConfig(config);
            ECommonUtil.getLog().info("执行get请求" + httpGet.getRequestLine());
            response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                ECommonUtil.getLog().info("响应状态:"+ response.getStatusLine());
                String rStr=EntityUtils.toString(entity,"UTF-8");
                ECommonUtil.getLog().info("响应内容:" + rStr);
                if(HttpStatus.SC_OK==response.getStatusLine().getStatusCode()){
                    res=rStr;
                }
                EntityUtils.consume(entity);
            }
        }catch (Exception e) {
            ECommonUtil.getLog().info("http请求错误"+e);
            throw e;
        }finally{
            if(httpGet!=null){
                httpGet.releaseConnection();
            }
            if(response!=null){
                response.close();
            }
            if(httpClient!=null){
                httpClient.close();
            }
        }
        return res;
    }

    /**
     *
     * @param url 请求地址
     * @param params 请求参数
     * @param res 返回结果
     * @param timeOut 超时时间(min)
     * @param useProxy 是否使用代理
     * @param needCert 是否使用证书
     * @return
     * @throws Exception
     */
    @SuppressWarnings({ "deprecation", "unused" })
    private static String post(String url, List<NameValuePair> params, String res, int timeOut, boolean useProxy, boolean needCert) throws Exception {
        RequestConfig config = null;CloseableHttpClient httpClient=null; CloseableHttpResponse response=null;
        if(httpClientBuilder==null){
            httpClient= HttpUtils.createSSLClientDefault(useProxy,needCert);
        }else{
            httpClient=httpClientBuilder;
        }
        HttpPost httpPost = new HttpPost(url);
        if(useProxy){
            HttpHost proxy = new HttpHost(PropertiesUtil.properties.getProperty("proxy_host"), Integer.parseInt(PropertiesUtil.properties.getProperty("proxy_port")),"http");
            config = RequestConfig.custom().setProxy(proxy).setConnectTimeout(timeOut * 1000 * 60).build();
        }else{
            config = RequestConfig.custom().setConnectTimeout(timeOut * 1000 * 60).build();
        }
        httpPost.setConfig(config);
        // 设置类型
        httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        try {
            response = httpClient.execute(httpPost);
            if(302 == response.getStatusLine().getStatusCode()){
                ECommonUtil.getLog().info(response.getLastHeader("Location").getValue());
                post(response.getLastHeader("Location").getValue(), params, res, timeOut, useProxy,needCert);
            }
            HttpEntity entity = response.getEntity();
            res = EntityUtils.toString(entity, "UTF-8");
            ECommonUtil.getLog().info(res);
            EntityUtils.consume(entity);
        } catch (IOException e) {
            ECommonUtil.getLog().info("请求异常"+e);
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
            if (httpClient != null) {
                httpClient.close();
            }
        }
        return res;
    }

    public static void main(String[] args) throws Exception {
        //-----------post----------------
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("req", "{\"method\":\"checkMobile\",\"timestamp\":\"A100000000000001\",\"channelCode\":\"A1\",\"queryType\":\"1\",\"telephone\":\"15301929770\"}"));
//        HttpUtilsDemo.post("http://www.baidu.com", params,"",5,true);
//        HttpUtilsDemo.post("https://localhost/spdbSjptServer/service.cgi", params, "" , 5 ,false);
//        HttpUtilsDemo.post("http://localhost:7070/spdbSjptServer/service.cgi", params, "" , 5 ,false);
        //-----------get------------------
        String method = "{\"method\":\"checkMobile\",\"timestamp\":\"A100000000000001\",\"channelCode\":\"A1\",\"queryType\":\"1\",\"telephone\":\"15301929770\"}";
        Map<String, String> map = new HashMap<String, String>();
        map.put("req", method);
//        HttpUtilsDemo.get("https://localhost/spdbSjptServer/service.cgi", map,"",5,false);
        HttpUtils.get("http://localhost:7070/spdbSjptServer/service.cgi", map, "", 5, false,false);
//        HttpUtilsDemo.get("https://localhost/spdbSjptServer/service.cgi", map, "", 5, false,true);
//        HttpUtilsDemo.get("http://www.baidu.com", map, "", 5, true,false);
    }

}