
/*
* 文件名：InstanceNameGenerator.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月15日
* 修改内容：
*/

package com.yspay.common.cluster;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yspay.common.coordinator.Coordinator;
import com.yspay.common.coordinator.ZookeeperConfiguration;

public class DistributionInstanceNoAllocator implements InstanceNoAllocator {
	private static final Logger logger = LoggerFactory
			.getLogger(DistributionInstanceNoAllocator.class);

	public static final String CLUSTER_PATH = "/ClusterNaming";
	public static final String LOCK_PATH = "/Lock";
	public static final String INSTANCE_PATH = "/Instances";

	// 分配的集群实例编号
	private Integer instanceNo = Integer.valueOf(-1);

	// 为这个应用集群生成实例编号，如果有多个环境的集群，可以拼接env+clusterName+applicationName
	private String clusterApplicationName = null;

	// 此应用集群的zk路径
	private String clusterAppPath = null;

	// 分配的当前集群实例zk路径
	private String clusterAppInstancePath = null;

	// 负责分配的当前集群实例zk session id
	private long clusterAppInstanceZKSessionId;

	// 对集群实例分配过程的监听器
	private List<InstanceNoListener> listeners = new ArrayList<InstanceNoListener>();

	// 最大的集群实例编号
	private int maxNo = 1024;

	// 当前机器标识
	final static String MACHINE_NAME = ManagementFactory.getRuntimeMXBean()
			.getName();

	/**
	 * 分布式协调器
	 */
	private Coordinator coordinator;

	public DistributionInstanceNoAllocator(String clusterApplicationName,
			Coordinator coordinator) {
		this.clusterApplicationName = clusterApplicationName;
		this.clusterAppPath = CLUSTER_PATH + "/" + this.clusterApplicationName;
		this.coordinator = coordinator;
	}

	@PostConstruct
	public void init() {
		// 失败后5s重试分配
		this.listeners.add(new ReAllocationInstanceNoListener());

		// 处理跟协调器之间的连接中断异常
		InstanceNoConnectionStateListener connectStateListenser = new InstanceNoConnectionStateListener();
		connectStateListenser.setInstanceNoAllocation(this);
		this.coordinator.getRawClient().getConnectionStateListenable()
				.addListener(connectStateListenser);

		this.allocationInstanceNo();
	}

	public Integer getInstanceNo() {
		return instanceNo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yspay.common.cluster.InstanceNoAllocator#setInstanceNoListener(com.
	 * yspay.common.cluster.InstanceNoListener)
	 */

	@Override
	public void setInstanceNoListener(InstanceNoListener listener) {
		this.listeners.add(listener);
	}

	public void setCoordinator(Coordinator coordinator) {
		this.coordinator = coordinator;
	}

	public Coordinator getCoordinator() {
		return coordinator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yspay.common.cluster.InstanceNoAllocator#reAllocationInstanceNo()
	 */

	@Override
	public void reAllocationInstanceNo() {
		if (this.getCoordinator()
				.getSessionID() != this.clusterAppInstanceZKSessionId) {
			clearInstanceNo();
			this.allocationInstanceNo();
		} else {
			boolean isExisted = false;
			try {
				if (this.clusterAppInstancePath != null) {
					isExisted = this.getCoordinator()
							.isExisted(this.clusterAppInstancePath);
				}
			} catch (Exception e) {
				logger.error("判断集群实例编号节点是否存在失败！原有节点key:"
						+ this.clusterAppInstancePath, e);
			}

			if (!isExisted) {
				clearInstanceNo();
				this.allocationInstanceNo();
			}
		}
	}

	/**
	 * 重置分配的实例编号
	 */
	private void clearInstanceNo() {
		try {
			if (this.clusterAppInstancePath != null && this.getCoordinator()
					.getRawClient().getZookeeperClient().isConnected()) {
				logger.info(
						"删除原有集群实例编号,原有节点key:" + this.clusterAppInstancePath);

				this.getCoordinator().remove(this.clusterAppInstancePath);
			}
		} catch (Exception e) {
			logger.error("删除原有集群实例编号失败！原有节点key:" + this.clusterAppInstancePath,
					e);
		}

		this.instanceNo = -1;
		this.clusterAppInstancePath = null;
		this.clusterAppInstanceZKSessionId = -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yspay.common.cluster.InstanceNoAllocator#allocationInstanceNo()
	 */

	@Override
	public void allocationInstanceNo() {
		if (this.clusterApplicationName == null
				|| this.clusterApplicationName.trim().length() == 0) {
			throw new RuntimeException("集群标识未设置，不能分配集群的实例编号");
		}

		if (this.getCoordinator() == null || this.getCoordinator()
				.getRawClient().getState() != CuratorFrameworkState.STARTED) {
			throw new RuntimeException("协调器未设置或者还没有开启，不能分配集群的实例编号");
		}

		// 发布开始分配实例编号事件，让相关任务处理，比如分配id的任务，锁住分配Id的线程，分配节点编号期间，不能生产id
		for (int i = 0; i < this.listeners.size(); i++) {
			this.listeners.get(i).onPreAllocation(new PreAllocationEvent(this));
		}

		InterProcessMutex lock = null;
		try {
			// 先获取互斥锁，整个集群只能有一个节点进行分配实例编号动作
			lock = new InterProcessMutex(coordinator.getRawClient(),
					clusterAppPath.concat(LOCK_PATH));
			logger.info("开始获取集群节点编号分配锁，，如果在10s内不能获取锁，集群节点编号分配将会失败......");
			if (!lock.acquire(10, TimeUnit.SECONDS)) {
				logger.error("很不幸，在10s内不能获取锁，集群节点编号分配失败......");
				throw new RuntimeException("10s内不能获取节点编号分配锁，集群节点编号分配失败");
			}

			logger.info("你很幸运，在10s内获取锁，节点编号分配中......");

			Stat stat = coordinator.getRawClient().checkExists()
					.forPath(clusterAppPath.concat(INSTANCE_PATH));
			if (stat == null) {
				instanceNo = Integer.valueOf(1);
			} else {
				List<String> currentInstances = coordinator.getRawClient()
						.getChildren()
						.forPath(clusterAppPath.concat(INSTANCE_PATH));
				Map<String, Integer> instanceNodeDataMap = new HashMap<String, Integer>();
				if (currentInstances == null || currentInstances.size() == 0) {
					instanceNo = Integer.valueOf(1);
				} else {
					// 缓存已经存在的节点数据，如果这期间临时节点消失，数据以-1标识，不占用1-maxNo里面的资源
					for (int j = 0; j < currentInstances.size(); j++) {
						byte[] data = null;
						try {
							data = coordinator.getRawClient().getData()
									.forPath(clusterAppPath
											.concat(INSTANCE_PATH).concat("/")
											.concat(currentInstances.get(j)));
						} catch (NoNodeException e) {
							logger.info("节点可能是临时节点,数据不存在:", e);
						}

						// 节点数据格式 instanceNo-jvmPid@hostname
						if (data != null) {
							String tempIndex = new String(data, "UTF-8");
							instanceNodeDataMap.put(currentInstances.get(j),
									Integer.parseInt(tempIndex.substring(0,
											tempIndex.indexOf("-"))));
						} else {
							instanceNodeDataMap.put(currentInstances.get(j),
									Integer.valueOf(-1));
						}
					}

					// 从1-maxNo里面取一个没有使用的节点编号
					for (int i = 1; i < maxNo; i++) {
						boolean isUsed = false;

						// 判断当前编号有没有在zk里面存在
						for (int j = 0; j < currentInstances.size(); j++) {
							Integer tempIndex = instanceNodeDataMap
									.get(currentInstances.get(j));
							if (i == tempIndex.intValue()) {
								isUsed = true;
								break;
							}
						}

						if (!isUsed) {
							instanceNo = Integer.valueOf(i);
							break;
						}
					}
				}
			}

			this.clusterAppInstancePath = coordinator.getRawClient().create()
					.creatingParentsIfNeeded().withProtection()
					.withMode(CreateMode.EPHEMERAL).forPath(
							clusterAppPath.concat(INSTANCE_PATH)
									.concat(INSTANCE_PATH),
							String.valueOf(instanceNo.intValue()).concat("-")
									.concat(MACHINE_NAME).getBytes("UTF-8"));

			clusterAppInstanceZKSessionId = this.coordinator.getSessionID();
			logger.info("节点编号分配成功，当前zk session id:"
					+ Long.toHexString(this.clusterAppInstanceZKSessionId)
					+ ", 当前节点路径：" + clusterAppInstancePath + "; 编号为："
					+ this.instanceNo);
		} catch (Exception e) {
			logger.error("分配集群编号失败，应用启动失败", e);
			// 发布分配实例编号成功事件，让相关任务处理，比如分配id的任务，设置实例编号，解锁分配Id的线程
			for (int i = 0; i < this.listeners.size(); i++) {
				this.listeners.get(i).onPostAllocation(new PostAllocationEvent(
						Integer.valueOf(-1), false, this));
			}
			throw new RuntimeException("分配集群编号失败，应用启动失败", e);
		} finally {
			try {
				if (lock != null) {
					lock.release();
				}
			} catch (Exception e) {
				logger.error("释放锁失败！", e);
			}
		}

		// 发布分配实例编号成功事件，让相关任务处理，比如分配id的任务，设置实例编号，解锁分配Id的线程
		for (int i = 0; i < this.listeners.size(); i++) {
			this.listeners.get(i).onPostAllocation(new PostAllocationEvent(
					Integer.valueOf(instanceNo), true, this));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yspay.common.cluster.InstanceNoAllocator#lostAllocation()
	 */

	@Override
	public void lostAllocation() {
		this.clearInstanceNo();

		for (int i = 0; i < this.listeners.size(); i++) {
			this.listeners.get(i)
					.onLostAllocation(new LostAllocationEvent(this));
		}
	}

	public static void main(String[] args) throws Exception {
		Coordinator coordinator = new Coordinator(
				new ZookeeperConfiguration("10.213.32.120:2181"));
		// 开启协调器客户端
		coordinator.start();

		InstanceNoAllocator allocation = new DistributionInstanceNoAllocator(
				"dubboProviderSample", coordinator);

		allocation.allocationInstanceNo();

		// 关闭协调器客户端
		coordinator.close();
	}

}
