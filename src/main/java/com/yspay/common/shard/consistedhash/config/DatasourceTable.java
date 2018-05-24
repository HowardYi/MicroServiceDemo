
/*
* 文件名：DatasourceWithTablesMetaData.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月21日
* 修改内容：
*/

package com.yspay.common.shard.consistedhash.config;

public class DatasourceTable {
	/**
	 * 数据源编号
	 */
	private String datasourceIdRef;

	/**
	 * 真实表名称
	 */
	private String actualTableName;

	public DatasourceTable() {

	}

	public DatasourceTable(String datasourceIdRef, String actualTableName) {
		this.datasourceIdRef = datasourceIdRef;
		this.actualTableName = actualTableName;
	}

	public String getDatasourceIdRef() {
		return datasourceIdRef;
	}

	public void setDatasourceIdRef(String datasourceIdRef) {
		this.datasourceIdRef = datasourceIdRef;
	}

	public String getActualTableName() {
		return actualTableName;
	}

	public void setActualTableName(String actualTableName) {
		this.actualTableName = actualTableName;
	}

}
