package com.yspay.common.rpc.hessian3;

import java.lang.reflect.InvocationTargetException;
import java.net.SocketTimeoutException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemoteAccessor;
import org.springframework.util.StringUtils;

import com.caucho.hessian.client.HessianConnectionException;

public class MyHessionClientInterceptor extends RemoteAccessor
		implements MethodInterceptor, InitializingBean {
	private static Logger log = LoggerFactory
			.getLogger(MyHessionClientInterceptor.class);
	private Object hessianProxy;

	// 远程hessian地址
	private String url;

	// 连接超时时间，默认30秒
	private int connectTimeout = 30;

	// 读取超时时间，默认30秒
	private int readTimeout = 30;

	@Override
	public void afterPropertiesSet() {
		Class<?> interfaceClass = this.getServiceInterface();
		MyHessianProxyFactory factory = new MyHessianProxyFactory();
		factory.setReadTimeout(readTimeout * 1000);
		factory.setConnectTimeout(connectTimeout * 1000);
		try {
			hessianProxy = factory.create(interfaceClass, url);
		} catch (Throwable t) {
			log.error("创建hessian客户端代理失败！", t);
			throw new RuntimeException("创建hessian客户端代理失败", t);
		}
	}

	public Object getHessianProxy() {

		return hessianProxy;
	}

	public void setHessianProxy(Object hessianProxy) {
		this.hessianProxy = hessianProxy;
	}

	public String getUrl() {

		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getConnectTimeout() {

		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getReadTimeout() {

		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		if (this.hessianProxy == null) {
			throw new IllegalStateException(
					"HessianClientInterceptor is not properly initialized!");
		}

		ClassLoader originalClassLoader = overrideThreadContextClassLoader();
		try {
			Object result = invocation.getMethod().invoke(this.hessianProxy,
					invocation.getArguments());
			return result;
		} catch (InvocationTargetException ex) {
			Throwable targetEx = ex.getTargetException();
			// Hessian 4.0 check: another layer of InvocationTargetException.
			if (targetEx instanceof InvocationTargetException) {
				targetEx = ((InvocationTargetException) targetEx)
						.getTargetException();
			}

			if (targetEx instanceof HessianConnectionException) {
				if (targetEx.getCause() != null) {
					if (targetEx.getCause() instanceof SocketTimeoutException) {
						if (!StringUtils
								.isEmpty(targetEx.getCause().getMessage())
								&& targetEx.getCause().getMessage()
										.equals("Read timed out")) {
							throw targetEx.getCause();
						}
					}
				}

				log.error(
						"Hession connection exception when invoke Hessian proxy for remote service ["
								+ this.getServiceInterface().getName() + "]",
						targetEx);
				throw targetEx;
			} else {
				log.error(
						"Failed to invoke Hessian proxy for remote service ["
								+ this.getServiceInterface().getName() + "]",
						ex);
				throw targetEx;
			}
		} catch (Throwable ex) {
			log.error("Failed to invoke Hessian proxy for remote service ["
					+ this.getServiceInterface().getName() + "]", ex);

			throw ex;
		} finally {
			resetThreadContextClassLoader(originalClassLoader);
		}

	}
}
