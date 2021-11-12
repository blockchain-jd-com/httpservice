package com.jd.httpservice.agent;

import com.jd.httpservice.auth.SSLSecurity;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.Closeable;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class ServiceConnectionManager implements Closeable {

    /**
     * 重新验证方法，取消SSL验证（信任所有证书）
     */
    private static TrustManager trustManager = new X509TrustManager() {

        @Override
        public void checkClientTrusted(X509Certificate[] ax509certificate, String s) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] ax509certificate, String s) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

    };
    private PoolingHttpClientConnectionManager connectionManager;

    public ServiceConnectionManager() {
        this(new SSLSecurity());
    }

    public ServiceConnectionManager(SSLSecurity security) {
        Registry<ConnectionSocketFactory> factories = null;
        switch (security.getSslMode()) {
            case OFF:
                factories = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", createSSLIgnoreConnectionSocketFactory())
                        .build();
                break;
            case ON_WAY:
                factories = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", createOneWaySSLConnectionSocketFactory(security))
                        .build();
                break;
            case TWO_WAY:
                factories = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", createTwoWaySSLConnectionSocketFactory(security))
                        .build();
                break;
        }
        this.connectionManager = new PoolingHttpClientConnectionManager(factories);

        setMaxTotal(100).setDefaultMaxPerRoute(20);
    }

    /**
     * 创建一个不受管理的连接；
     *
     * @param serviceEndpoint
     * @return
     */
    public static ServiceConnection connect(ServiceEndpoint serviceEndpoint) {
        SSLSecurity sslSecurity = serviceEndpoint.getSslSecurity();
        SSLConnectionSocketFactory csf = null;
        switch (sslSecurity.getSslMode()) {
            case OFF:
                csf = createSSLIgnoreConnectionSocketFactory();
                break;
            case ON_WAY:
                csf = createOneWaySSLConnectionSocketFactory(sslSecurity);
                break;
            case TWO_WAY:
                csf = createTwoWaySSLConnectionSocketFactory(sslSecurity);
                break;
        }

        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        httpClientBuilder.setSSLSocketFactory(csf);
        return new HttpServiceConnection(serviceEndpoint, httpClientBuilder.build());
    }

    private static CloseableHttpClient createHttpClient(ServiceConnectionManager connectionManager) {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        HttpClientConnectionManager httpConnMng = connectionManager.getHttpConnectionManager();
        httpClientBuilder.setConnectionManager(httpConnMng).setConnectionManagerShared(true);
        return httpClientBuilder.build();
    }

    /**
     * 创建忽略证书的SSL安全连接
     *
     * @return
     */
    private static SSLConnectionSocketFactory createSSLIgnoreConnectionSocketFactory() {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[]{trustManager}, null);
            return new SSLConnectionSocketFactory(context, NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * 创建SSL单向安全连接
     *
     * @return
     */
    private static SSLConnectionSocketFactory createOneWaySSLConnectionSocketFactory(SSLSecurity security) {
        try {
            // 创建信任库管理工厂实例
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            // 信任库类型
            KeyStore trustStore = KeyStore.getInstance("JKS");
            // 加载信任库，即服务端公钥
            trustStore.load(new FileInputStream(security.getTrustStore()), security.getTrustStorePassword().toCharArray());
            // 初始化信任库
            tmf.init(trustStore);
            TrustManager[] tms = tmf.getTrustManagers();
            // 建立TLS连接
            SSLContext context = SSLContext.getInstance("TLS");
            // 初始化SSLContext
            context.init(null, tms, new SecureRandom());

            return new SSLConnectionSocketFactory(context);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * 创建SSL双向安全连接
     *
     * @return
     */
    private static SSLConnectionSocketFactory createTwoWaySSLConnectionSocketFactory(SSLSecurity security) {
        try {
            // 客户端证书类型
            KeyStore clientStore = KeyStore.getInstance(security.getTrustStoreType());
            // 加载客户端证书，即自己的私钥
            clientStore.load(new FileInputStream(security.getKeyStore()), security.getKeyStorePassword().toCharArray());
            // 创建密钥管理工厂实例
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            // 初始化客户端密钥库
            kmf.init(clientStore, security.getKeyStorePassword().toCharArray());
            KeyManager[] kms = kmf.getKeyManagers();
            // 创建信任库管理工厂实例
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            // 信任库类型
            KeyStore trustStore = KeyStore.getInstance(security.getTrustStoreType());
            // 加载信任库，即服务端公钥
            trustStore.load(new FileInputStream(security.getTrustStore()), security.getTrustStorePassword().toCharArray());
            // 初始化信任库
            tmf.init(trustStore);
            TrustManager[] tms = tmf.getTrustManagers();
            // 建立TLS连接
            SSLContext context = SSLContext.getInstance("TLS");
            // 初始化SSLContext
            context.init(kms, tms, new SecureRandom());

            return new SSLConnectionSocketFactory(context);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public ServiceConnectionManager setMaxTotal(int maxConn) {
        connectionManager.setMaxTotal(maxConn);
        return this;
    }

    public ServiceConnectionManager setDefaultMaxPerRoute(int maxConnPerRoute) {
        connectionManager.setDefaultMaxPerRoute(maxConnPerRoute);
        return this;
    }

    HttpClientConnectionManager getHttpConnectionManager() {
        return connectionManager;
    }

    /**
     * 创建一个受此连接管理器管理的连接
     *
     * @param serviceEndpoint
     * @return
     */
    public ServiceConnection create(ServiceEndpoint serviceEndpoint) {
        CloseableHttpClient httpClient = createHttpClient(this);
        return new HttpServiceConnection(serviceEndpoint, httpClient);
    }

    @Override
    public void close() {
        PoolingHttpClientConnectionManager cm = connectionManager;
        if (cm != null) {
            connectionManager = null;
            cm.close();
        }
    }

}
