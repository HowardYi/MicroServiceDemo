
/*
* 文件名：IIdGenerator.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月17日
* 修改内容：
*/

package com.yspay.common.idgenerator;

public interface IdGenerator {

	void setWaitSeconds(short waitSeconds);

	/**
	 * 获得下一个ID (该方法是线程安全的)
	 * 
	 * ID = 时间戳（精确到秒 4byte）+ 当前集群实例编号（2byte ）+ 单Jvm内自然序列（2byte）
	 * 
	 * @return SnowflakeId
	 */
	long nextId();

	void setInstanceNo(int instanceNo);

}
