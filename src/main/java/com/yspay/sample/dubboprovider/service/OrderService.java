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

package com.yspay.sample.dubboprovider.service;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dangdang.ddframe.rdb.sharding.api.HintManager;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.PageRowBounds;
import com.yspay.sample.dubboprovider.entity.Order;
import com.yspay.sample.dubboprovider.entity.OrderItem;
import com.yspay.sample.dubboprovider.repository.OrderItemMapper;
import com.yspay.sample.dubboprovider.repository.OrderItemMapperExt;
import com.yspay.sample.dubboprovider.repository.OrderMapper;
import com.yspay.sample.dubboprovider.repository.OrderMapperExt;

/**
 * Order 服务对象.
 * 
 * @author gaohongtao
 */
@Service
public class OrderService implements IOrderService {

	@Resource
	private OrderMapper orderMapper;

	@Resource
	private OrderMapperExt orderMapperExt;

	@Resource
	private OrderItemMapper orderItemMapper;

	@Resource
	private OrderItemMapperExt orderItemMapperExt;

	@Resource
	private IOrderIdGenerator orderIdGenerator;

	@Override
	@Transactional(readOnly = true)
	public void select() {

		// 指定查询哪个库的所有记录
		HintManager hintManager = HintManager.getInstance();
		try {
			hintManager.addDatabaseShardingValue("T_ORDER", "user_id", "1");
			hintManager.addDatabaseShardingValue("T_ORDER", "order_id", "1");

			hintManager.addTableShardingValue("T_ORDER", "user_id", "1");
			hintManager.addTableShardingValue("T_ORDER", "order_id", "1");

			System.out.println(orderMapperExt.selectAll());
		} finally {
			hintManager.close();
		}

		// 查出所有库的记录
		System.out.println(orderMapperExt.selectAll());

		// 指定查询哪个库的所有记录
		hintManager = HintManager.getInstance();
		try {
			hintManager.addDatabaseShardingValue("T_ORDER_ITEM", "user_id",
					"1");
			hintManager.addDatabaseShardingValue("T_ORDER_ITEM", "order_id",
					"1");

			hintManager.addTableShardingValue("T_ORDER_ITEM", "user_id", "1");
			hintManager.addTableShardingValue("T_ORDER_ITEM", "order_id", "1");

			System.out.println(orderItemMapperExt.selectAll());
		} finally {
			hintManager.close();
		}

		// 查出所有库的记录
		System.out.println(orderItemMapperExt.selectAll());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dangdang.ddframe.rdb.sharding.example.jdbc.service.IOrderService#
	 * clear()
	 */

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public void clear() {
		// 指定删除userid=1&orderid=1所在订单表的数据
		HintManager hintManager = HintManager.getInstance();
		try {
			hintManager.addDatabaseShardingValue("T_ORDER", "user_id", "1");
			hintManager.addDatabaseShardingValue("T_ORDER", "order_id", "1");

			hintManager.addTableShardingValue("T_ORDER", "user_id", "1");
			hintManager.addTableShardingValue("T_ORDER", "order_id", "1");

			orderMapperExt.deleteAll();
		} finally {
			hintManager.close();
		}

		// 没有路由指示，删除所有库的订单表记录
		orderMapperExt.deleteAll();

		// 指定删除userid=1&orderid=1所在订单明细表的数据
		hintManager = HintManager.getInstance();
		try {
			hintManager.addDatabaseShardingValue("T_ORDER_ITEM", "user_id",
					"1");
			hintManager.addDatabaseShardingValue("T_ORDER_ITEM", "order_id",
					"1");

			hintManager.addTableShardingValue("T_ORDER_ITEM", "user_id", "1");
			hintManager.addTableShardingValue("T_ORDER_ITEM", "order_id", "1");

			orderItemMapperExt.deleteAll();
		} finally {
			hintManager.close();
		}

		// 没有路由指示，删除所有库的订单明细表记录
		orderItemMapperExt.deleteAll();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void insert() {
		Date now = new Date();

		Order order = new Order();
		order.setOrderId("1");
		order.setUserId("1");
		order.setOrderSeqId(orderIdGenerator.nextOrderSeqId(order.getUserId(),
				order.getOrderId()));
		order.setCreateDate(now);
		order.setStatus("INSERT-User1-Order1");
		orderMapper.insert(order);
		System.out.println(order.getOrderSeqId());

		OrderItem item = new OrderItem();
		item.setItemId(orderIdGenerator.nextOrderItemSeqId(order.getUserId(),
				order.getOrderId()));
		item.setOrderSeqId(order.getOrderSeqId());
		item.setUserId(order.getUserId());
		item.setOrderId(order.getOrderId());
		item.setCreateDate(now);
		this.orderItemMapper.insert(item);

		order.setUserId("1");
		order.setOrderId("2");
		order.setOrderSeqId(orderIdGenerator.nextOrderSeqId(order.getUserId(),
				order.getOrderId()));
		order.setStatus("INSERT-User1-Order2");
		order.setCreateDate(now);
		orderMapper.insert(order);
		System.out.println(order.getOrderSeqId());

		item = new OrderItem();
		item.setItemId(orderIdGenerator.nextOrderItemSeqId(order.getUserId(),
				order.getOrderId()));
		item.setOrderSeqId(order.getOrderSeqId());
		item.setUserId(order.getUserId());
		item.setOrderId(order.getOrderId());
		item.setCreateDate(now);
		this.orderItemMapper.insert(item);

		order.setUserId("2");
		order.setOrderId("1");
		order.setOrderSeqId(orderIdGenerator.nextOrderSeqId(order.getUserId(),
				order.getOrderId()));
		order.setStatus("INSERT-User2-Order1");
		order.setCreateDate(now);
		orderMapper.insert(order);
		System.out.println(order.getOrderSeqId());

		item = new OrderItem();
		item.setItemId(orderIdGenerator.nextOrderItemSeqId(order.getUserId(),
				order.getOrderId()));
		item.setOrderSeqId(order.getOrderSeqId());
		item.setUserId(order.getUserId());
		item.setOrderId(order.getOrderId());
		item.setCreateDate(now);
		this.orderItemMapper.insert(item);

		order.setUserId("2");
		order.setOrderId("2");
		order.setOrderSeqId(orderIdGenerator.nextOrderSeqId(order.getUserId(),
				order.getOrderId()));
		order.setStatus("INSERT-User2-Order2");
		order.setCreateDate(now);
		orderMapper.insert(order);
		System.out.println(order.getOrderSeqId());

		item = new OrderItem();
		item.setItemId(orderIdGenerator.nextOrderItemSeqId(order.getUserId(),
				order.getOrderId()));
		item.setOrderSeqId(order.getOrderSeqId());
		item.setUserId(order.getUserId());
		item.setOrderId(order.getOrderId());
		item.setCreateDate(now);
		this.orderItemMapper.insert(item);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public void fooService() {
		this.insert();

		Order order = new Order();
		order.setUserId("3");
		order.setOrderId("3");
		order.setOrderSeqId(orderIdGenerator.nextOrderSeqId(order.getUserId(),
				order.getOrderId()));
		order.setStatus("INSERT-User3-Order3");
		order.setCreateDate(new Date());
		orderMapper.insert(order);
		System.out.println(order.getOrderSeqId());

		OrderItem item = new OrderItem();
		String itemId = orderIdGenerator.nextOrderItemSeqId(order.getUserId(),
				order.getOrderId());
		item.setItemId(itemId);
		item.setOrderSeqId(order.getOrderSeqId());
		item.setUserId(order.getUserId());
		item.setOrderId(order.getOrderId());
		item.setCreateDate(new Date());
		this.orderItemMapper.insert(item);

		// 查出上面插入 id 的记录，根据itemid进行路由
		item = orderItemMapper.selectByPrimaryKey(itemId);
		System.out.println("itemid:" + item.getItemId() + " orderId:"
				+ item.getOrderId() + " userid:" + item.getUserId());

		// 没有路由策略可用，查出所有库里面的所有表数据，结果归并
		List<OrderItem> orderItems = orderItemMapperExt.selectAll();
		for (OrderItem orderItem : orderItems) {
			System.out.println("itemid:" + orderItem.getItemId() + " orderId:"
					+ orderItem.getOrderId() + " userid:"
					+ orderItem.getUserId());
		}

		// 没有路由策略可用，查出所有库里面的所有表数据，结果归并，分页示例，只取第一页（2条记录）
		List<OrderItem> orderItemsPage = orderItemMapperExt
				.selectByPage(new PageRowBounds(0, 2));

		PageInfo<OrderItem> page = new PageInfo<OrderItem>(orderItemsPage);
		System.out.println("分页总条数：" + page.getTotal() + " 分页总页数："
				+ page.getPages() + " 当前第n页：" + page.getPageNum());
		for (OrderItem orderItem : orderItemsPage) {
			System.out.println("itemid:" + orderItem.getItemId() + " orderId:"
					+ orderItem.getOrderId() + " userid:"
					+ orderItem.getUserId());
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void fooServiceWithFailure() {
		fooService();
		throw new IllegalArgumentException("failed");
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public void update() {
		List<Order> orders = orderMapperExt.selectAll();
		System.out.println("Before update......");
		if (orders != null && orders.size() > 0) {
			for (int i = 0; i < orders.size(); i++) {
				System.out.println(" orderId:" + orders.get(i).getOrderId()
						+ " userid:" + orders.get(i).getUserId()
						+ "order status:" + orders.get(i).getStatus());
			}
		}

		Order tobeUpdate = orderMapperExt.selectByUserIdOrderIdForUpdate("2",
				"1");
		tobeUpdate.setStatus("updated");
		orderMapperExt.updateStatusByUserIdOrderId(tobeUpdate);

		System.out.println("after update......");
		orders = orderMapperExt.selectAll();
		if (orders != null && orders.size() > 0) {
			for (int i = 0; i < orders.size(); i++) {
				System.out.println(" orderId:" + orders.get(i).getOrderId()
						+ " userid:" + orders.get(i).getUserId()
						+ "order status:" + orders.get(i).getStatus());
			}
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public void createOrder(Order order) {
		order.setOrderSeqId(orderIdGenerator.nextOrderSeqId(order.getUserId(),
				order.getOrderId()));
		order.setCreateDate(new Date());
		orderMapper.insert(order);
	}

	@Override
	public Order getOrder(String userId, String orderId) {
		return orderMapperExt.selectByUserIdOrderId(userId, orderId);
	}
}
