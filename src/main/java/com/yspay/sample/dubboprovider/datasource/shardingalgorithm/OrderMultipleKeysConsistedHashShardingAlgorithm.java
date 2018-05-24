
/*
* 文件名：OrderMultipleKeysShardingAlgorithm.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月23日
* 修改内容：
*/

package com.yspay.sample.dubboprovider.datasource.shardingalgorithm;

import java.util.Collection;
import java.util.Collections;

import com.alibaba.dubbo.container.spring.SpringContainer;
import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.MultipleKeysShardingAlgorithm;
import com.yspay.common.shard.consistedhash.algorithm.ConsistedHashRouter;
import com.yspay.common.shard.consistedhash.algorithm.IConsistedHashRouter;
import com.yspay.common.shard.consistedhash.config.ShardInfo;

public abstract class OrderMultipleKeysConsistedHashShardingAlgorithm
		implements MultipleKeysShardingAlgorithm {
	private IConsistedHashRouter router;

	public IConsistedHashRouter getRouter() {
		return router;
	}

	public void setRouter(IConsistedHashRouter router) {
		this.router = router;
	}

	@Override
	public Collection<String> doSharding(
			Collection<String> availableTargetNames,
			Collection<ShardingValue<?>> shardingValues) {
		if (this.router == null) {
			this.router = SpringContainer.getContext()
					.getBean(ConsistedHashRouter.class);
		}

		String userIdColumnName = "user_id";
		String userIdValue = "";

		String orderIdColumnName = "order_id";
		String orderIdValue = "";

		String orderSeqIdColumnName = "order_seq_id";
		String orderSeqIdValue = "";

		for (ShardingValue<?> shardingValue : shardingValues) {
			if (shardingValue.getColumnName()
					.equalsIgnoreCase(userIdColumnName)) {
				userIdValue = (String) shardingValue.getValue();
			}

			if (shardingValue.getColumnName()
					.equalsIgnoreCase(orderIdColumnName)) {
				orderIdValue = (String) shardingValue.getValue();
			}

			if (shardingValue.getColumnName()
					.equalsIgnoreCase(orderSeqIdColumnName)) {
				orderSeqIdValue = (String) shardingValue.getValue();
			}
		}

		if ((userIdValue.trim().equals("") || orderIdValue.trim().equals(""))
				&& (orderSeqIdValue.trim().equals(""))) {
			throw new RuntimeException("数据库路由失败，没有设置路由字段值!");
		}

		ShardInfo shardInfo = null;

		if (!orderSeqIdValue.trim().equals("")) {
			shardInfo = router.getDatasourceTable(
					Long.parseLong(orderSeqIdValue.substring(16), 16));
		}

		if (!userIdValue.trim().equals("") && !orderIdValue.trim().equals("")) {
			shardInfo = router.getShardInfo(userIdValue + "_" + orderIdValue);
		}
		for (String each : availableTargetNames) {
			if (isMatchTarget(each, shardInfo)) {
				return Collections.singletonList(each);
			}
		}

		throw new UnsupportedOperationException();

	}

	/**
	 * 是否匹配到了数据库和表
	 * 
	 * @param targetName
	 * @param shardInfo
	 * @return boolean
	 */
	abstract boolean isMatchTarget(String targetName, ShardInfo shardInfo);
}
