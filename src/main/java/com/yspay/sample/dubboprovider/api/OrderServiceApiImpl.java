
/*
* 文件名：OrderServiceApiImpl.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月8日
* 修改内容：
*/

package com.yspay.sample.dubboprovider.api;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.yspay.sample.dubboprovider.entity.Order;
import com.yspay.sample.dubboprovider.service.IOrderService;

/**
 * 客户端接口的实现，粒度尽量大，通常不包括事务，整合多个内部接口实现
 * 
 * @author Cindy
 * @version 2018年5月8日
 * @see OrderServiceApiImpl
 * @since
 */
@Service("orderServiceApiImpl")
public class OrderServiceApiImpl implements IOrderServiceApi {

	@Resource
	private IOrderService orderService;

	@Override
	public void create(Order order) {
		orderService.createOrder(order);
	}

	@Override
	public Order getOrder(String userId, String orderId) {
		return orderService.getOrder(userId, orderId);
	}

}
