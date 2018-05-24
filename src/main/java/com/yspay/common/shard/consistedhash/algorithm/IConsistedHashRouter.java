
/*
* 文件名：IConsistedHashRouter.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月21日
* 修改内容：
*/

package com.yspay.common.shard.consistedhash.algorithm;

import com.yspay.common.shard.consistedhash.config.ShardInfo;

public interface IConsistedHashRouter {

	/**
	 * 根据rowkey来路由
	 * 
	 * @param key
	 * @return 虚拟分区和逻辑分区，物理库表名称
	 * @see
	 */
	ShardInfo getShardInfo(String rowKey);

	/**
	 * 根据虚拟节点编号来路由
	 * 
	 * @param virtualShardNo
	 *            虚拟节点编号
	 * @return 虚拟分区和逻辑分区，物理库表名称
	 * @see
	 */
	ShardInfo getDatasourceTable(long virtualShardNo);

}
