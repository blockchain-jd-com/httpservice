package com.jd.httpservice.agent;

import java.io.Closeable;
import java.io.IOException;

public interface HttpServiceProxy extends Closeable {

	/**
	 * 服务代理连接的远端服务端点地址；
	 * 
	 * @return
	 */
	ServiceEndpoint getServiceEndpoint();

	/**
	 * 关闭服务代理的连接；
	 */
	@Override
	void close() throws IOException;
}
