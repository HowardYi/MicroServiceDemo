
/*
* 文件名：Coodinator.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月15日
* 修改内容：
*/

package com.yspay.common.coordinator;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;

public class Coordinator {
	private static final Logger logger = LoggerFactory
			.getLogger(Coordinator.class);

	private ZookeeperConfiguration zkConfig;

	private CuratorFramework client;

	public Coordinator(ZookeeperConfiguration zkConfig) {
		this.zkConfig = zkConfig;
	}

	@PostConstruct
	public void start() {
		logger.debug("Coordinator: zookeeper center init, server lists is: {}.",
				zkConfig.getServerLists());
		CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory
				.builder().connectString(zkConfig.getServerLists())
				.retryPolicy(new ExponentialBackoffRetry(
						zkConfig.getBaseSleepTimeMilliseconds(),
						zkConfig.getMaxRetries(),
						zkConfig.getMaxSleepTimeMilliseconds()))
				.namespace(zkConfig.getNamespace());
		if (0 != zkConfig.getSessionTimeoutMilliseconds()) {
			builder.sessionTimeoutMs(zkConfig.getSessionTimeoutMilliseconds());
		}
		if (0 != zkConfig.getConnectionTimeoutMilliseconds()) {
			builder.connectionTimeoutMs(
					zkConfig.getConnectionTimeoutMilliseconds());
		}
		if (!Strings.isNullOrEmpty(zkConfig.getDigest())) {
			builder.authorization("digest",
					zkConfig.getDigest().getBytes(Charsets.UTF_8))
					.aclProvider(new ACLProvider() {

						@Override
						public List<ACL> getDefaultAcl() {
							return ZooDefs.Ids.CREATOR_ALL_ACL;
						}

						@Override
						public List<ACL> getAclForPath(final String path) {
							return ZooDefs.Ids.CREATOR_ALL_ACL;
						}
					});
		}
		client = builder.build();
		client.start();
		try {
			if (!client.blockUntilConnected(
					zkConfig.getMaxSleepTimeMilliseconds()
							* zkConfig.getMaxRetries(),
					TimeUnit.MILLISECONDS)) {
				client.close();
				throw new KeeperException.OperationTimeoutException();
			}
		} catch (final Exception ex) {
			logger.error("协调器客户端连接服务器失败", ex);
			throw new RuntimeException(ex);
		}
	}

	@PreDestroy
	public void close() {
		CloseableUtils.closeQuietly(client);
	}

	public String get(final String key) throws Exception {
		return getDirectly(key);
	}

	public String getDirectly(final String key) throws Exception {
		return new String(client.getData().forPath(key), Charsets.UTF_8);
	}

	public List<String> getChildrenKeys(final String key) throws Exception {
		List<String> result = client.getChildren().forPath(key);
		Collections.sort(result, new Comparator<String>() {

			@Override
			public int compare(final String o1, final String o2) {
				return o2.compareTo(o1);
			}
		});
		return result;
	}

	public int getNumChildren(final String key) throws Exception {
		Stat stat = client.checkExists().forPath(key);
		if (null != stat) {
			return stat.getNumChildren();
		}
		return 0;
	}

	public boolean isExisted(final String key) throws Exception {
		return null != client.checkExists().forPath(key);
	}

	public void persist(final String key, final String value) throws Exception {
		if (!isExisted(key)) {
			client.create().creatingParentsIfNeeded()
					.withMode(CreateMode.PERSISTENT)
					.forPath(key, value.getBytes(Charsets.UTF_8));
		} else {
			update(key, value);
		}
	}

	public void update(final String key, final String value) throws Exception {
		client.inTransaction().check().forPath(key).and().setData()
				.forPath(key, value.getBytes(Charsets.UTF_8)).and().commit();
	}

	public void persistEphemeral(final String key, final String value)
			throws Exception {
		if (isExisted(key)) {
			client.delete().deletingChildrenIfNeeded().forPath(key);
		}
		client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
				.forPath(key, value.getBytes(Charsets.UTF_8));
	}

	public String persistSequential(final String key, final String value)
			throws Exception {
		return client.create().creatingParentsIfNeeded()
				.withMode(CreateMode.PERSISTENT_SEQUENTIAL)
				.forPath(key, value.getBytes(Charsets.UTF_8));

	}

	public void persistEphemeralSequential(final String key) throws Exception {
		client.create().creatingParentsIfNeeded()
				.withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(key);
	}

	public void remove(final String key) throws Exception {
		client.delete().deletingChildrenIfNeeded().forPath(key);
	}

	public CuratorFramework getRawClient() {
		return client;
	}

	public long getSessionID() {
		try {
			return client.getZookeeperClient().getZooKeeper().getSessionId();
		} catch (Exception e) {
			logger.error("获取session id失败！", e);
		}

		return -1l;
	}
}
