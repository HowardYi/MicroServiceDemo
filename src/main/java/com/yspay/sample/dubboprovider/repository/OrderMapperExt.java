
/*
* 文件名：OrderMapperExt.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月4日
* 修改内容：
*/

package com.yspay.sample.dubboprovider.repository;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.JdbcType;

import com.yspay.sample.dubboprovider.entity.Order;

public interface OrderMapperExt {
	@Update({ "update T_ORDER ", "set STATUS = #{status,jdbcType=VARCHAR} ",
			"where USER_ID = #{userId,jdbcType=VARCHAR} and ORDER_ID = #{orderId,jdbcType=VARCHAR}" })
	int updateStatusByUserIdOrderId(Order record);

	@Select({ "select * from T_ORDER ",
			"where USER_ID = #{userId,jdbcType=DECIMAL} and ORDER_ID = #{orderId,jdbcType=DECIMAL}" })
	@Results({
			@Result(column = "ORDER_SEQ_ID", property = "orderSeqId", jdbcType = JdbcType.VARCHAR, id = true),
			@Result(column = "ORDER_ID", property = "orderId", jdbcType = JdbcType.VARCHAR),
			@Result(column = "USER_ID", property = "userId", jdbcType = JdbcType.VARCHAR),
			@Result(column = "STATUS", property = "status", jdbcType = JdbcType.VARCHAR),
			@Result(column = "CREATE_DATE", property = "createDate", jdbcType = JdbcType.TIMESTAMP) })
	Order selectByUserIdOrderId(@Param("userId") String userId,
			@Param("orderId") String orderId);

	@Select({ "select", "ORDER_ID, USER_ID, STATUS, CREATE_DATE",
			"from T_ORDER",
			"where USER_ID = #{userId,jdbcType=VARCHAR} and ORDER_ID = #{orderId,jdbcType=VARCHAR}",
			"for update" })
	@Results({
			@Result(column = "ORDER_SEQ_ID", property = "orderSeqId", jdbcType = JdbcType.VARCHAR, id = true),
			@Result(column = "ORDER_ID", property = "orderId", jdbcType = JdbcType.VARCHAR),
			@Result(column = "USER_ID", property = "userId", jdbcType = JdbcType.VARCHAR),
			@Result(column = "STATUS", property = "status", jdbcType = JdbcType.VARCHAR),
			@Result(column = "CREATE_DATE", property = "createDate", jdbcType = JdbcType.TIMESTAMP) })
	Order selectByUserIdOrderIdForUpdate(@Param("userId") String userId,
			@Param("orderId") String orderId);

	@Delete({ "delete from T_ORDER" })
	int deleteAll();

	@Select({ "select", "ORDER_ID, USER_ID, STATUS, CREATE_DATE",
			"from T_ORDER" })
	@Results({
			@Result(column = "ORDER_SEQ_ID", property = "orderSeqId", jdbcType = JdbcType.VARCHAR, id = true),
			@Result(column = "ORDER_ID", property = "orderId", jdbcType = JdbcType.VARCHAR),
			@Result(column = "USER_ID", property = "userId", jdbcType = JdbcType.VARCHAR),
			@Result(column = "STATUS", property = "status", jdbcType = JdbcType.VARCHAR),
			@Result(column = "CREATE_DATE", property = "createDate", jdbcType = JdbcType.TIMESTAMP) })
	List<Order> selectAll();

	@Select({ "select", "ORDER_ID, USER_ID, STATUS, CREATE_DATE",
			"from T_ORDER" })
	@Results({
			@Result(column = "ORDER_SEQ_ID", property = "orderSeqId", jdbcType = JdbcType.VARCHAR, id = true),
			@Result(column = "ORDER_ID", property = "orderId", jdbcType = JdbcType.VARCHAR),
			@Result(column = "USER_ID", property = "userId", jdbcType = JdbcType.VARCHAR),
			@Result(column = "STATUS", property = "status", jdbcType = JdbcType.VARCHAR),
			@Result(column = "CREATE_DATE", property = "createDate", jdbcType = JdbcType.TIMESTAMP) })
	List<Order> selectByPage(RowBounds row);
}
