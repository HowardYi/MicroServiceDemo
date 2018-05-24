
/*
* 文件名：RangeConfig.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月21日
* 修改内容：
*/

package com.yspay.common.shard.consistedhash.config;

public class Range {
	private int begin;

	private int end;

	public Range() {

	}

	public Range(int begin, int end) {
		this.begin = begin;
		this.end = end;
	}

	public int getBegin() {

		return begin;
	}

	public void setBegin(int begin) {
		this.begin = begin;
	}

	public int getEnd() {

		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

}
