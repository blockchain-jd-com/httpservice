package com.jd.httpservice.agent;

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
import utils.GmSSLProvider;
import utils.StringUtils;
import utils.net.SSLMode;
import utils.net.SSLSecurity;

import javax.net.ssl.*;
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
        this(false, new SSLSecurity());
    }

    public ServiceConnectionManager(boolean secure, SSLSecurity security) {
        Registry<ConnectionSocketFactory> factories;
        SSLMode sslMode = security.getSslMode(true);
        if (!secure || sslMode.equals(SSLMode.OFF)) {
            factories = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", createSSLIgnoreConnectionSocketFactory(security))
                    .build();
        } else if (sslMode.equals(SSLMode.ONE_WAY)) {
            factories = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", createOneWaySSLConnectionSocketFactory(security))
                    .build();
        } else {
            factories = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", createTwoWaySSLConnectionSocketFactory(security))
                    .build();
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
        return new HttpServiceConnection(serviceEndpoint, buildHttpClient(serviceEndpoint));
    }

    /**
     * 构建HTTP连接客户端
     *
     * @param serviceEndpoint
     * @return
     */
    public static CloseableHttpClient buildHttpClient(ServiceEndpoint serviceEndpoint) {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        if (serviceEndpoint.isSecure()) {
            SSLSecurity sslSecurity = serviceEndpoint.getSslSecurity();
            SSLConnectionSocketFactory csf = null;
            switch (sslSecurity.getSslMode(true)) {
                case OFF:
                    csf = createSSLIgnoreConnectionSocketFactory(sslSecurity);
                    break;
                case ONE_WAY:
                    csf = createOneWaySSLConnectionSocketFactory(sslSecurity);
                    break;
                case TWO_WAY:
                    csf = createTwoWaySSLConnectionSocketFactory(sslSecurity);
                    break;
            }
            httpClientBuilder.setSSLSocketFactory(csf);
        }
        return httpClientBuilder.build();
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
    private static SSLConnectionSocketFactory createSSLIgnoreConnectionSocketFactory(SSLSecurity security) {
        try {
            SSLContext context = SSLContext.getInstance(security.getProtocol());
            context.init(null, new TrustManager[]{trustManager}, null);
            return createSSLConnectionSocketFactory(context, security);
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
            KeyStore trustStore = KeyStore.getInstance(security.getTrustStoreType());
            // 加载信任库，即服务端公钥
            trustStore.load(new FileInputStream(security.getTrustStore()), security.getTrustStorePassword().toCharArray());
            // 初始化信任库
            tmf.init(trustStore);
            TrustManager[] tms = tmf.getTrustManagers();
            // 建立TLS连接
            SSLContext context = SSLContext.getInstance(security.getProtocol());
            // 初始化SSLContext
            context.init(null, tms, new SecureRandom());

            return createSSLConnectionSocketFactory(context, security);
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
            KeyManager[] kms = null;
            if (!StringUtils.isEmpty(security.getKeyStore())) {
                // 客户端证书类型
                KeyStore clientStore = KeyStore.getInstance(security.getKeyStoreType());
                // 加载客户端证书，即自己的私钥
                clientStore.load(new FileInputStream(security.getKeyStore()), security.getKeyStorePassword().toCharArray());
                // 创建密钥管理工厂实例
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                // 初始化客户端密钥库
                kmf.init(clientStore, security.getKeyStorePassword().toCharArray());
                kms = kmf.getKeyManagers();
            }
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
            SSLContext context = SSLContext.getInstance(security.getProtocol());
            // 初始化SSLContext
            context.init(kms, tms, new SecureRandom());

            return createSSLConnectionSocketFactory(context, security);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }


    private static SSLConnectionSocketFactory createSSLConnectionSocketFactory(SSLContext context, SSLSecurity security){

        HostnameVerifier hostnameVerifier = security.isNoopHostnameVerifier() ? NoopHostnameVerifier.INSTANCE : SSLConnectionSocketFactory.getDefaultHostnameVerifier();

        if(GmSSLProvider.isGMSSL(security.getProtocol())){
            return new SSLConnectionSocketFactory(context, GmSSLProvider.ENABLE_PROTOCOLS, GmSSLProvider.ENABLE_CIPHERS, hostnameVerifier);
        }
        return new SSLConnectionSocketFactory(context, security.getEnabledProtocols(), security.getCiphers(), hostnameVerifier);
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
