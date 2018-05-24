
/*
* 文件名：VirtualAndLogicShardBean.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月21日
* 修改内容：
*/

package com.yspay.common.shard.consistedhash.config;

public class ShardInfo {
	/**
	 * 虚拟节点编号
	 */
	private int virtualShardNo;

	/**
	 * 逻辑节点编号
	 */
	private int logicShardNo;

	/**
	 * 数据库表节点
	 */
	private DatasourceTable dbTable;

	public int getVirtualShardNo() {

		return virtualShardNo;
	}

	public void setVirtualShardNo(int virtualShardNo) {
		this.virtualShardNo = virtualShardNo;
	}

	public int getLogicShardNo() {

		return logicShardNo;
	}

	public void setLogicShardNo(int logicShardNo) {
		this.logicShardNo = logicShardNo;
	}

	public DatasourceTable getDbTable() {

		return dbTable;
	}

	public void setDbTable(DatasourceTable dbTable) {
		this.dbTable = dbTable;
	}

}
