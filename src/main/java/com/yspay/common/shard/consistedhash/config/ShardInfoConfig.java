
/*
* 文件名：RouteTables.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月18日
* 修改内容：
*/

package com.yspay.common.shard.consistedhash.config;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 * 
 * @author Cindy
 * @version 2018年5月18日
 * @see ShardInfoConfig
 */
public class ShardInfoConfig {

	/**
	 * 虚拟分区个数： 默认1024个虚拟分区，把2^32的范围映射到这些虚拟分区上
	 */
	private int virtualShardNum = 0x400;

	/**
	 * 逻辑分区和虚拟分区之间的映射 key 为逻辑分区编号； value Range 虚拟分区的起始编号； 1个逻辑分区包括一个连续范围的虚拟分区
	 * 
	 */
	private Map<Integer, Range> logicShardMapVirtualShardsConfig = new TreeMap<Integer, Range>();

	/**
	 * 逻辑分区和数据库表之间的映射；1个逻辑分区和1个数据库表对应，1对1的关系
	 * 
	 */
	private Map<Integer, DatasourceTable> logicShardMapDBTableConfig = new TreeMap<Integer, DatasourceTable>();

	public int getVirtualShardNum() {
		return virtualShardNum;
	}

	public void setVirtualShardNum(int virtualShardNum) {
		this.virtualShardNum = virtualShardNum;
	}

	public Map<Integer, Range> getLogicShardMapVirtualShards() {
		return logicShardMapVirtualShardsConfig;
	}

	public void setLogicShardMapVirtualShardsConfig(
			Map<Integer, Range> logicShardMapVirtualShards) {
		this.logicShardMapVirtualShardsConfig
				.putAll(logicShardMapVirtualShards);
	}

	public Map<Integer, DatasourceTable> getLogicShardMapDBTable() {
		return logicShardMapDBTableConfig;
	}

	public void setLogicShardMapDBTableConfig(
			Map<Integer, DatasourceTable> logicShardMapDBTable) {
		this.logicShardMapDBTableConfig.putAll(logicShardMapDBTable);
	}

}
