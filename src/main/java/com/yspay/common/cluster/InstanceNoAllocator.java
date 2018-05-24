
/*
* 文件名：InstanceNoAllocator.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月17日
* 修改内容：
*/

package com.yspay.common.cluster;

public interface InstanceNoAllocator {

	/**
	 * 设置监听器，感知instanceNo的变化
	 * 
	 * @param listener
	 * @see
	 */
	void setInstanceNoListener(InstanceNoListener listener);

	/**
	 * 重连后，判断sessionid是否改变，如果改变了，则需要重新生成实例编号；
	 * 如果sessionid没改变，判断临时节点是否存在，不存在则重新申请集群实例编号；如果存在则不做任何处理
	 * 
	 */
	void reAllocationInstanceNo();

	/**
	 * 算出当前集群已经注册了多少节点，找一个1-65535之间没有使用过的节点编号，赋值给目前节点； 如果节点停掉，节点编号会释放，供其它后来的节点使用
	 * 
	 */
	void allocationInstanceNo();

	/**
	 * 失去集群节点实例的分配，需要重新分配，相关数据重置
	 */
	void lostAllocation();

}
