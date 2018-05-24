
/*
* 文件名：IOrderServiceApi.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月8日
* 修改内容：
*/

package com.yspay.sample.dubboprovider.api;

import com.yspay.sample.dubboprovider.entity.Order;

/**
 * 面对客户端的接口
 * 
 * @author Cindy
 * @version 2018年5月8日
 * @see IOrderServiceApi
 * @since
 */
public interface IOrderServiceApi {
	void create(Order order);

	Order getOrder(String userId, String orderId);
}
