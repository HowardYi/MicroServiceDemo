
/*
* 文件名：IOrderIdGenerator.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月23日
* 修改内容：
*/

package com.yspay.sample.dubboprovider.service;

public interface IOrderIdGenerator {

	String nextOrderSeqId(String userId, String orderId);

	String nextOrderItemSeqId(String userId, String orderId);

}
