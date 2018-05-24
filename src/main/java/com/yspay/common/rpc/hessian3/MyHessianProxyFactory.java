
/*
* 文件名：MyHessianProxyFactory.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月10日
* 修改内容：
*/

package com.yspay.common.rpc.hessian3;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import com.caucho.hessian.client.HessianProxyFactory;

public class MyHessianProxyFactory extends HessianProxyFactory {
	private int connectTimeout = 30000;

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	protected URLConnection openConnection(URL url) throws IOException {
		URLConnection conn = super.openConnection(url);

		conn.setDoOutput(true);
		conn.setConnectTimeout(this.connectTimeout);
		conn.setReadTimeout(Long.valueOf(this.getReadTimeout()).intValue());
		return conn;
	}
}
