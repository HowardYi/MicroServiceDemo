
/*
* 文件名：IOrderService.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月4日
* 修改内容：
*/

package com.yspay.sample.dubboprovider.service;

import com.yspay.sample.dubboprovider.entity.Order;

/**
 * 内部业务接口
 * 
 * @author Cindy
 * @version 2018年5月8日
 * @see IOrderService
 * @since
 */
public interface IOrderService {

	void select();

	void clear();

	void insert();

	void createOrder(Order order);

	Order getOrder(String userId, String orderId);

	void update();

	void fooService();

	void fooServiceWithFailure();

}
