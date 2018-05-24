
/*
* 文件名：InstanceNameListener.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月16日
* 修改内容：
*/

package com.yspay.common.cluster;

public interface InstanceNoListener {
	/**
	 * 分配集群实例编号之后
	 */
	void onPostAllocation(PostAllocationEvent event);

	/**
	 * 分配集群实例编号之前
	 */
	void onPreAllocation(PreAllocationEvent event);

	/**
	 * 可能发生重新分配集群实例编号，当前集群实例编号不可用
	 */
	void onLostAllocation(LostAllocationEvent event);

}
