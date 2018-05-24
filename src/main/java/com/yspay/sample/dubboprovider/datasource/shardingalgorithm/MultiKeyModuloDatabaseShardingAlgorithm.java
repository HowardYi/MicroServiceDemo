/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.yspay.sample.dubboprovider.datasource.shardingalgorithm;

import java.util.Collection;
import java.util.Collections;

import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.MultipleKeysDatabaseShardingAlgorithm;

public final class MultiKeyModuloDatabaseShardingAlgorithm
		implements MultipleKeysDatabaseShardingAlgorithm {
	@Override
	public Collection<String> doSharding(
			Collection<String> availableTargetNames,
			Collection<ShardingValue<?>> shardingValues) {
		String userIdColumnName = "user_id";
		String userIdValue = "";

		String orderIdColumnName = "order_id";
		String orderIdValue = "";

		for (ShardingValue<?> shardingValue : shardingValues) {
			if (shardingValue.getColumnName()
					.equalsIgnoreCase(userIdColumnName)) {
				userIdValue = (String) shardingValue.getValue();
			}

			if (shardingValue.getColumnName()
					.equalsIgnoreCase(orderIdColumnName)) {
				orderIdValue = (String) shardingValue.getValue();
			}
		}

		if (userIdValue.trim().equals("") || orderIdValue.trim().equals("")) {
			throw new RuntimeException("数据库路由失败，没有设置路由字段值!");
		}

		for (String each : availableTargetNames) {
			if (each.endsWith("_" + Long.parseLong(userIdValue) % 2)) {
				return Collections.singletonList(each);
			}
		}

		throw new UnsupportedOperationException();
	}
}