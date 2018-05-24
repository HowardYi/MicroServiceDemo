
/*
* 文件名：BusinessIdGenerator.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月23日
* 修改内容：
*/

package com.yspay.sample.dubboprovider.service;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.yspay.common.idgenerator.IdGenerator;
import com.yspay.common.shard.consistedhash.algorithm.IConsistedHashRouter;

@Component
public class OrderIdGenerator implements IOrderIdGenerator {

	@Resource
	private IdGenerator idGenerator;

	public IdGenerator getIdGenerator() {
		return idGenerator;
	}

	public void setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}

	@Resource
	private IConsistedHashRouter router;

	public IConsistedHashRouter getRouter() {
		return router;
	}

	public void setRouter(IConsistedHashRouter router) {
		this.router = router;
	}

	@Override
	public String nextOrderSeqId(String userId, String orderId) {
		int virtualShardNo = router.getShardInfo(userId + "_" + orderId)
				.getVirtualShardNo();

		// 填充后最大16位
		String snowFlakeId = StringUtils
				.leftPad(Long.toHexString(this.idGenerator.nextId()), 16, '0');

		// 这个填充后总长度根据逻辑分片的最大值决定
		String shardNo = StringUtils
				.leftPad(Integer.toHexString(virtualShardNo), 2, '0');

		return snowFlakeId.concat(shardNo);
	}

	@Override
	public String nextOrderItemSeqId(String userId, String orderId) {
		return this.nextOrderSeqId(userId, orderId);
	}
}
