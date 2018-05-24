
/*
* 文件名：IdGenerator.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月16日
* 修改内容：
*/

package com.yspay.common.idgenerator;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yspay.common.cluster.DistributionInstanceNoAllocator;
import com.yspay.common.cluster.InstanceNoAllocator;
import com.yspay.common.coordinator.Coordinator;
import com.yspay.common.coordinator.ZookeeperConfiguration;

public class DefaultIdGeneratorImpl implements IdGenerator {
	private static final Logger logger = LoggerFactory
			.getLogger(DefaultIdGeneratorImpl.class);

	public static Object ID_GENERATOR_LOCK = new Object();

	/**
	 * 在获得锁时，判断是否等待超过此时间，如果超过则报超时
	 */
	private short waitSeconds = 3;

	/**
	 * 集群实例编号,支持的最大集群实例编号是1024
	 */
	private int instanceNo = -1;

	/**
	 * 集群实例编号 所占的位数
	 */
	private final long instanceNoBits = 10L;

	/**
	 * 支持的最大集群实例编号，结果是1024
	 */
	private final long maxInstanceNo = -1L ^ (-1L << instanceNoBits);

	/**
	 * 序列在id中占的位数
	 */
	private final long sequenceBits = 12L;

	/**
	 * 集群实例编号 向左移16位
	 */
	private final long instanceNoShift = sequenceBits;

	/**
	 * 时间截向左移22位(12+10)
	 */
	private final long timestampLeftShift = sequenceBits + instanceNoBits;

	/**
	 * 生成序列的掩码，这里为4095 (0b1111 1111 1111=0xfff=4095)
	 */
	private final long sequenceMask = -1L ^ (-1L << sequenceBits);

	/**
	 * 毫秒内序列(0~4095)
	 */
	private long sequence = 0L;

	/**
	 * 上次生成ID的时间截
	 */
	private long lastTimestamp = -1L;

	/**
	 * 开始时间戳，id里面的时间戳不是真是时间戳，是与这个值得差值；这个值代表2018:05:18 00:00:00
	 */
	private long twepoch = 1526572800000L;

	public DefaultIdGeneratorImpl() {
	}

	public DefaultIdGeneratorImpl(int instanceNo) {
		if (instanceNo > maxInstanceNo || instanceNo < 0) {
			throw new RuntimeException(
					"集群实例编号范围错误，不能大于>" + this.maxInstanceNo + ";不能小于0");
		}

		setInstanceNo(instanceNo);
	}

	public short getWaitSeconds() {
		return waitSeconds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yspay.common.idgenerator.IIdGenerator#setWaitSeconds(short)
	 */

	@Override
	public void setWaitSeconds(short waitSeconds) {
		this.waitSeconds = waitSeconds;
	}

	/**
	 * ID = 时间戳（精确到毫秒 4byte）+ 当前集群实例编号（2byte ）+ 单Jvm内自然序列（2byte）
	 * 
	 * @return
	 * @see
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yspay.common.idgenerator.IIdGenerator#nextId()
	 */

	@Override
	public long nextId() {
		long startTimestamp = System.currentTimeMillis();

		synchronized (ID_GENERATOR_LOCK) {
			// 等待instanceNo的合法分配，如果超时waitSeconds还没有设置正确的集群实例id，则抛出异常
			// 如果是动态获取的instanceNo，在当前实例与zk断开连接导致失去session之后，instanceNo需要被重新分配，在获取新的
			// instanceNo之前，此处需要阻塞一段时间，不要轻易抛出异常，影响体验，正常情况下10s内能获得新的instanceNo
			long currentTimestamp = System.currentTimeMillis();
			while (instanceNo > maxInstanceNo || instanceNo < 0) {
				if (currentTimestamp - startTimestamp > waitSeconds * 1000) {
					throw new RuntimeException(
							"等待" + waitSeconds + "s后，集群实例编号没有被正确赋值，不能大于>"
									+ this.maxInstanceNo + ";不能小于0");
				}

				try {
					Thread.sleep(100l);
				} catch (InterruptedException e) {
					logger.error("等待分配集群实例编号期间休眠被中断", e);
				}
				currentTimestamp = System.currentTimeMillis();
			}

			long timestamp = timeGen();

			// 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
			if (timestamp < lastTimestamp) {
				logger.error(
						"当前时间比最后一次产生id的时间早，估计是机器时间向前拨了，在机器时间迟于最后一次产生id之前不能产生id；"
								+ "当前时间:" + timestamp + "； 最后一次产生id时间："
								+ lastTimestamp);
				throw new RuntimeException(
						"当前时间比最后一次产生id的时间早，估计是机器时间向前拨了，在机器时间迟于最后一次产生id之前不能产生id");
			}

			// 如果是同一时间生成的，则进行毫秒内序列
			if (lastTimestamp == timestamp) {
				sequence = (sequence + 1) & sequenceMask;
				// 毫秒内序列溢出
				if (sequence == 0) {
					// 阻塞到下一个毫秒,获得新的时间戳
					timestamp = tilNextMillis(lastTimestamp);
				}
			}
			// 时间戳改变，毫秒内序列重置
			else {
				sequence = 0L;
			}

			// 上次生成ID的时间截
			lastTimestamp = timestamp;

			// 移位并通过或运算拼到一起组成64位的ID
			return ((timestamp - twepoch) << timestampLeftShift) //
					| (instanceNo << instanceNoShift) //
					| sequence;
		}
	}

	/**
	 * 阻塞到下一个毫秒，直到获得新的时间戳
	 * 
	 * @param lastTimestamp
	 *            上次生成ID的时间截
	 * @return 当前时间戳
	 */
	protected long tilNextMillis(long lastTimestamp) {
		long timestamp = timeGen();
		while (timestamp <= lastTimestamp) {
			timestamp = timeGen();
		}
		return timestamp;
	}

	/**
	 * 返回以毫秒为单位的当前时间
	 * 
	 * @return 当前时间(毫秒)
	 */
	protected long timeGen() {
		return System.currentTimeMillis();
	}

	public int getInstanceNo() {
		return instanceNo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yspay.common.idgenerator.IIdGenerator#setInstanceNo(int)
	 */

	@Override
	public void setInstanceNo(int instanceNo) {
		synchronized (ID_GENERATOR_LOCK) {
			this.instanceNo = instanceNo;
		}
	}

	public static void main(String[] args) {
		Coordinator coordinator = new Coordinator(
				new ZookeeperConfiguration("10.213.32.120:2181"));
		// 初始化，开启协调器客户端
		coordinator.start();

		final IdGenerator idGenerator = new DefaultIdGeneratorImpl();
		IdGeneratorInstanceNoAllocatorListener instanceNoListener = new IdGeneratorInstanceNoAllocatorListener(
				idGenerator);

		InstanceNoAllocator allocation = new DistributionInstanceNoAllocator(
				"dubboProviderSample", coordinator);
		allocation.setInstanceNoListener(instanceNoListener);

		// 初始分配
		allocation.allocationInstanceNo();

		ExecutorService executor = Executors.newFixedThreadPool(100);
		final CountDownLatch main = new CountDownLatch(100);

		for (int i = 0; i < 100; i++) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					long startTimestamp = System.currentTimeMillis();
					long currentTimestamp = System.currentTimeMillis();
					while (currentTimestamp - startTimestamp < 5 * 60 * 1000) {
						try {
							long id = idGenerator.nextId();
							logger.debug("二进制: " + Long.toBinaryString(id));
							logger.debug("十六进制：" + Long.toHexString(id));
							logger.debug("十进制：" + id);
						} catch (RuntimeException e) {
							logger.error("获取id失败！", e);
						}

						try {
							Thread.sleep(200l);
						} catch (InterruptedException e) {
							logger.error("休眠被中断", e);
						}
						currentTimestamp = System.currentTimeMillis();
					}

					main.countDown();
				}

			});
		}

		try {
			main.await();
		} catch (InterruptedException e) {
			logger.error("", e);
		}

		executor.shutdown();

		// 关闭协调器客户端
		coordinator.close();
	}
}
