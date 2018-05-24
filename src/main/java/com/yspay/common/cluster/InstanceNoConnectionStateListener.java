
/*
* 文件名：InstanceNoConnectionStateListener.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月16日
* 修改内容：
*/

package com.yspay.common.cluster;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处理与zk之间的连接异常
 * 
 * @author Cindy
 * @version 2018年5月17日
 * @see InstanceNoConnectionStateListener
 * @since
 */
public class InstanceNoConnectionStateListener
		implements ConnectionStateListener {
	private static final Logger logger = LoggerFactory
			.getLogger(InstanceNoConnectionStateListener.class);

	private DistributionInstanceNoAllocator instanceNoAllocation;

	public void setInstanceNoAllocation(
			DistributionInstanceNoAllocator instanceNoAllocation) {
		this.instanceNoAllocation = instanceNoAllocation;
	}

	@Override
	public void stateChanged(CuratorFramework client,
			ConnectionState newState) {
		if (newState == ConnectionState.LOST) {
			// session确定丢失，临时节点资源会消失，意味着失去了集群实例编号，暂停相关需要集群实例编号资源的工作
			instanceNoAllocation.lostAllocation();

			logger.warn("session确定丢失: " + Long.toHexString(
					this.instanceNoAllocation.getCoordinator().getSessionID()));
		} else if (newState == ConnectionState.RECONNECTED) {
			// 重连后，判断sessionid是否改变，如果改变了，则需要重新生成实例编号；
			// 如果sessionid没改变，判断临时节点是否存在，不存在则重新申请集群实例编号；如果存在则不做任何处理
			logger.warn("重新连接，sessionId: " + Long.toHexString(
					this.instanceNoAllocation.getCoordinator().getSessionID()));

			instanceNoAllocation.reAllocationInstanceNo();
		} else if (newState == ConnectionState.SUSPENDED) {
			// 只是连接丢失，zk session还在，临时节点资源不会马上消失，集群实例编号还在；等到Lost事件发生，才确定集群实例编号丢失
			// 此处记录日志，不做处理，可以用来跟踪日志，suspended和lost可能会成对出现；也可能短暂失连，迅速重连
			logger.warn("连接丢失，session暂时还在: " + Long.toHexString(
					this.instanceNoAllocation.getCoordinator().getSessionID()));
		}
	}

}
