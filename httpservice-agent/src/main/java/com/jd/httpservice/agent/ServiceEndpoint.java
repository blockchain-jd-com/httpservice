package com.jd.httpservice.agent;

import utils.net.SSLSecurity;
import org.springframework.util.StringUtils;
import utils.net.NetworkAddress;

/**
 * 服务器设置；
 *
 * @author haiq
 */
public class ServiceEndpoint extends NetworkAddress implements Cloneable {

    private static final long serialVersionUID = 128018335830143965L;

    private String contextPath;
    private SSLSecurity sslSecurity;

    public ServiceEndpoint(String host, int port, boolean secure) {
        this(host, port, secure, null);
    }

    public ServiceEndpoint(NetworkAddress networkAddress) {
        this(networkAddress.getHost(), networkAddress.getPort(), networkAddress.isSecure(), null);
    }

    public ServiceEndpoint(String host, int port, boolean secure, String contextPath) {
        super(host, port, secure);
        contextPath = StringUtils.cleanPath(contextPath);
        if (StringUtils.isEmpty(contextPath)) {
            this.contextPath = "/";
        } else {
            this.contextPath = contextPath;
        }
    }

    public String getContextPath() {
        return contextPath;
    }

    public SSLSecurity getSslSecurity() {
        return sslSecurity;
    }

    public void setSslSecurity(SSLSecurity sslSecurity) {
        this.sslSecurity = sslSecurity;
    }

    @Override
    public ServiceEndpoint clone() {
        try {
            ServiceEndpoint endpoint = (ServiceEndpoint) super.clone();
            endpoint.setSslSecurity(this.sslSecurity);
            return endpoint;
        } catch (CloneNotSupportedException e) {
            throw new UnsupportedOperationException(e.getMessage(), e);
        }
    }

}
