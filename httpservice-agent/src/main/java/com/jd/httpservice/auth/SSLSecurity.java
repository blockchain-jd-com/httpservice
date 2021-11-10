package com.jd.httpservice.auth;

/**
 * @description: SSL连接配置
 * @author: imuge
 * @date: 2021/11/10
 **/
public class SSLSecurity {
    // 认证模式
    private SSLMode sslMode;
    // keystore 类型
    private String keyStoreType;
    // keystore 路径
    private String keyStore;
    // key别名
    private String keyStoreAlias;
    // 密码
    private String keyStorePassword;
    // 信任库路径
    private String trustStore;
    // 信任库密码
    private String trustStorePassword;
    // 信任库类型
    private String trustStoreType;

    public SSLSecurity() {
        this.sslMode = SSLMode.OFF;
    }

    public SSLSecurity(SSLMode sslMode, String keyStoreType, String keyStore, String keyStoreAlias, String keyStorePassword) {
        this.sslMode = sslMode;
        this.keyStoreType = keyStoreType;
        this.keyStore = keyStore;
        this.keyStoreAlias = keyStoreAlias;
        this.keyStorePassword = keyStorePassword;
    }

    public SSLSecurity(SSLMode sslMode, String keyStoreType, String keyStore, String keyStoreAlias, String keyStorePassword, String trustStore, String trustStorePassword, String trustStoreType) {
        this.sslMode = sslMode;
        this.keyStoreType = keyStoreType;
        this.keyStore = keyStore;
        this.keyStoreAlias = keyStoreAlias;
        this.keyStorePassword = keyStorePassword;
        this.trustStore = trustStore;
        this.trustStorePassword = trustStorePassword;
        this.trustStoreType = trustStoreType;
    }

    public String getTrustStoreType() {
        return trustStoreType;
    }

    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }

    public SSLMode getSslMode() {
        return sslMode;
    }

    public void setSslMode(SSLMode sslMode) {
        this.sslMode = sslMode;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public String getKeyStoreAlias() {
        return keyStoreAlias;
    }

    public void setKeyStoreAlias(String keyStoreAlias) {
        this.keyStoreAlias = keyStoreAlias;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }
}
