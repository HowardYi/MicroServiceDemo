
/**
* 文件名：MyHessianProxyFactoryBean.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2016年3月29日
* 修改内容：
*/

package com.yspay.common.rpc.hessian3;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;

public class MyHessianProxyFactoryBean extends MyHessionClientInterceptor
		implements FactoryBean<Object> {
	private Object serviceProxy;

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		this.serviceProxy = new ProxyFactory(getServiceInterface(), this)
				.getProxy(getBeanClassLoader());
	}

	public Object getObject() {
		return this.serviceProxy;
	}

	public Class<?> getObjectType() {
		return getServiceInterface();
	}

	public boolean isSingleton() {
		return true;
	}
}
