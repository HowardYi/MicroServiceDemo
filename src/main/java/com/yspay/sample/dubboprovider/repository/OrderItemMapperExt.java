package com.yspay.sample.dubboprovider.repository;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.type.JdbcType;

import com.github.pagehelper.PageRowBounds;
import com.yspay.sample.dubboprovider.entity.OrderItem;

public interface OrderItemMapperExt {
	@Delete({ "delete from T_ORDER_ITEM" })
	int deleteAll();

	@Select({ "select", "ITEM_ID, ORDER_ID, USER_ID, CREATE_DATE",
			"from T_ORDER_ITEM" })
	@Results({
			@Result(column = "ITEM_ID", property = "itemId", jdbcType = JdbcType.VARCHAR, id = true),
			@Result(column = "ORDER_SEQ_ID", property = "orderSeqId", jdbcType = JdbcType.VARCHAR),
			@Result(column = "ORDER_ID", property = "orderId", jdbcType = JdbcType.VARCHAR),
			@Result(column = "USER_ID", property = "userId", jdbcType = JdbcType.VARCHAR),
			@Result(column = "CREATE_DATE", property = "createDate", jdbcType = JdbcType.TIMESTAMP) })
	List<OrderItem> selectAll();

	@Select({ "select", "ITEM_ID, ORDER_ID, USER_ID, CREATE_DATE",
			"from T_ORDER_ITEM" })
	@Results({
			@Result(column = "ITEM_ID", property = "itemId", jdbcType = JdbcType.VARCHAR, id = true),
			@Result(column = "ORDER_SEQ_ID", property = "orderSeqId", jdbcType = JdbcType.VARCHAR),
			@Result(column = "ORDER_ID", property = "orderId", jdbcType = JdbcType.VARCHAR),
			@Result(column = "USER_ID", property = "userId", jdbcType = JdbcType.VARCHAR),
			@Result(column = "CREATE_DATE", property = "createDate", jdbcType = JdbcType.TIMESTAMP) })
	List<OrderItem> selectByPage(PageRowBounds row);

	@Select({ "select", "ITEM_ID, ORDER_ID, USER_ID, CREATE_DATE",
			"from T_ORDER_ITEM where USER_ID = #{userId,jdbcType=VARCHAR} and ORDER_ID = #{orderId,jdbcType=VARCHAR}" })
	@Results({
			@Result(column = "ITEM_ID", property = "itemId", jdbcType = JdbcType.VARCHAR, id = true),
			@Result(column = "ORDER_SEQ_ID", property = "orderSeqId", jdbcType = JdbcType.VARCHAR),
			@Result(column = "ORDER_ID", property = "orderId", jdbcType = JdbcType.VARCHAR),
			@Result(column = "USER_ID", property = "userId", jdbcType = JdbcType.VARCHAR),
			@Result(column = "CREATE_DATE", property = "createDate", jdbcType = JdbcType.TIMESTAMP) })
	List<OrderItem> selectByUserIdOrderId(@Param("userId") String userId,
			@Param("orderId") String orderId);

}