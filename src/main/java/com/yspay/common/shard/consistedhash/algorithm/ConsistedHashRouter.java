
/*
* 文件名：ConsistedHashRouter.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月21日
* 修改内容：
*/

package com.yspay.common.shard.consistedhash.algorithm;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.yspay.common.cluster.DistributionInstanceNoAllocator;
import com.yspay.common.cluster.InstanceNoAllocator;
import com.yspay.common.coordinator.Coordinator;
import com.yspay.common.coordinator.ZookeeperConfiguration;
import com.yspay.common.idgenerator.DefaultIdGeneratorImpl;
import com.yspay.common.idgenerator.IdGenerator;
import com.yspay.common.idgenerator.IdGeneratorInstanceNoAllocatorListener;
import com.yspay.common.shard.consistedhash.config.DatasourceTable;
import com.yspay.common.shard.consistedhash.config.Range;
import com.yspay.common.shard.consistedhash.config.ShardInfo;
import com.yspay.common.shard.consistedhash.config.ShardInfoConfig;

/**
 * hash一致性算法路由，为了把数据根据row key的hash值路由到正确的数据库表中，建立一种hash值和数据库表之间的映射关系;
 * 
 * hash(rowkey)->物理库的真实表(数据源+表名称)
 * 
 * 路由算法目标：达到数据均衡分布，单调性(增加节点或者删除节点，数据变动范围很小)，调整物理库热点压力
 * 
 * 为了分布数据存储，我们把一张大表划分为多个小表，暂且我们叫小表为逻辑分区表，
 * 一个物理数据库上可以存放多个逻辑分区表，逻辑分区表可以存放在数据库集群中的任何一个物理库中；
 * 
 * 路由算法修改为：hash(row key)->逻辑分区->物理库的真实表(数据源+表名称)
 * 
 * 为了扩容物理数据库，采用预置逻辑分区法，可以轻易把逻辑分区表整表拷贝到另外一个数据库上，再修改一下路由表即可实现；
 * 比如我们预分配4个逻辑分区（即一个大表拆分为4个小表），物理库有2个，2个表放在库a，2个表放在库b；
 * 如果需要扩容1个数据库，只需要把其中一个逻辑分区表整体拷贝到新库中即可，在修改一下路由表，客户端感知到逻辑分区表1在新库上；
 * 此方法目前在Redis3,ElasticSearch5中使用，实现物理服务器节点的扩容；
 * 最大能扩展到初始逻辑分区表的个数，这是一个弊端，逻辑分区表初始太大，会增加表个数，查询效率低，管理复杂，初始化逻辑分区个数小，会影响后续的扩容容量。
 * 下面采用虚拟分区来解决此问题。
 * 
 * 有时候出现数据热点往单个数据库表写入或者容量超出单个数据库表承受能力，需要对逻辑分区表进行裂变，实现逻辑分区的扩容；
 * 假设增加一个逻辑分区，原有分区算法是不是可以适用？ 比如取余数算法，row
 * key=5,原来2个逻辑分区表，5%2=1，会路由到逻辑分区1上，增加1个逻辑分区表后 5%3=2，这个时候路由到逻辑分区2上，这样
 * 会发生很大的数据路由变动，原来路由到逻辑分区1的，会路由到分区2上，很显然是不可接受的；
 * 有人会说我成倍增加逻辑分区，2个分区变4个分区，5%4=1,还是路由到逻辑分区1上，不过后面4变8的时候，是不是有点头疼？ 能不能只增加1个逻辑分区就好？
 * 很幸运，有算法解决此问题了，聪明的先驱们想出了hash一致性算法解决扩容中的单调性问题，请参考网上的hash一致性算法介绍。
 * 
 * 假设你已经理解了hash一致性算法，在一致性hash算法中，引入虚拟节点解决数据均衡性问题，我们可以引入虚拟分区概念，解决数据均匀的落到每个逻辑分区上。
 * 根据一致性hash算法把rowkey映射到虚拟分区上，根据实际每个逻辑分区承受的数据容量，把虚拟分区划分到逻辑分区中去。
 * 同时我们还可以利用虚拟分区可以归集到任意一个逻辑分区的特点，我们可以拆分逻辑分区，比如虚拟分区1，2，3，4归置到逻辑分区表1中，
 * 我们可以拆分逻辑分区表1为两个逻辑分区表1，5（假设原有4个逻辑分区，新增的表命名为5），然后把虚拟分区3，4拷贝到逻辑分区5中，
 * 这样就实现了拆分逻辑分区， 其他逻辑分区不受到影响，同时逻辑分区1拆分出的逻辑分区5还在同一个物理数据库服务器上，不发生网络传输，只是表级别的数据拷贝。
 * 这样就实现了逻辑分区表可以扩容而不发生数据大量迁移。
 * 
 * 总结一下：虚拟分区是最小的分区单元，不能再次裂变；
 * 一个逻辑分区包括N(>=1)个虚拟分区，虚拟分区是不可以再次裂变的，逻辑分区以虚拟分区为最小单位进行裂变成多个小的逻辑分区；
 * 
 * 路由算法过程变为：hash(row key)->虚拟分区->逻辑分区->物理库的真实表(数据源+表名称)
 * 
 * 虚拟分区编号是一个非常有用的信息，可以由row key计算出来，放入任何一个表的其他字段中，实现非row key字段的快速查询定位(无需二级索引支持)。
 * 这个特点是mongodb, hbase根据hash范围算法所不具备的哦，缺点是牺牲了逻辑表的裂变次数，最多裂变为虚拟分区的个数，而hbase,
 * mongodb可以无限的根据hash值范围进行裂变逻辑分区，
 * 理论上可以裂变为hash值范围的值个数（非常巨大，如果是32位的hash值，就达到256*256*256*256>36亿）。
 * 不过我们可以把虚拟节点的个数设置的比较大，比如65536，我们即可实现6万个实际物理节点的扩容，一般业务完全满足了。
 * 
 * 总结一下，扩展和数据再均衡的特点，初始物理库可以很小，初始逻辑分区表也可以很小，但是虚拟分区需要设置大一些，理论上物理库和表可以扩容到虚拟分区的个数。
 * 数据再均衡的实现，只需要更改虚拟分区映射到物理库表的关系即可实现数据再均衡。
 * 
 * @author Cindy
 * @version 2018年5月21日
 * @see ConsistedHashRouter
 * @since
 */
public class ConsistedHashRouter implements IConsistedHashRouter {
	/**
	 * 默认的编码方式
	 */
	public static final String CHARSET = "UTF-8";

	/**
	 * 2^32次方
	 */
	public static final long MAX_HASH_VALUE = 0x100000000l;

	private ShardInfoConfig config = null;

	/**
	 * hash值（0-2^32）与逻辑分区之间的映射
	 */
	private SortedMap<Long, ShardInfo> hashKeyShardMap = new TreeMap<Long, ShardInfo>();

	/**
	 * 虚拟分区和逻辑分区之间的映射
	 */
	private SortedMap<Long, ShardInfo> virtualShardMap = new TreeMap<Long, ShardInfo>();

	private static final Logger logger = LoggerFactory
			.getLogger(ConsistedHashRouter.class);

	public ConsistedHashRouter(ShardInfoConfig config) {
		this.config = config;
	}

	@PostConstruct
	public void init() {
		int virtualShardNum = config.getVirtualShardNum();

		logger.info("consisted hash table初始化......");
		for (int i = 0; i < virtualShardNum; i++) {
			ShardInfo virtualAndLogicShardBean = this
					.getVirtualAndLogicShardBean(i);
			// 把2^32范围的hash值

			long hashKey = (MAX_HASH_VALUE / virtualShardNum) * (i + 1);

			this.hashKeyShardMap.put(Long.valueOf(hashKey),
					virtualAndLogicShardBean);
			this.virtualShardMap.put(Long.valueOf(i), virtualAndLogicShardBean);

			logger.info("hashKey: [" + Long.toHexString(hashKey)
					+ "] virtualShardNo: ["
					+ virtualAndLogicShardBean.getVirtualShardNo()
					+ "] logicShardNo: ["
					+ virtualAndLogicShardBean.getLogicShardNo()
					+ "] dsTable: ["
					+ virtualAndLogicShardBean.getDbTable().getDatasourceIdRef()
					+ "->"
					+ virtualAndLogicShardBean.getDbTable().getActualTableName()
					+ "]");
		}
		logger.info("consisted hash table初始化结束......");
	}

	/**
	 * 根据虚拟分区编号，取得对应的逻辑分区编号
	 * 
	 * @param i
	 * @return
	 * @see
	 */
	private ShardInfo getVirtualAndLogicShardBean(int virtualShardNo) {
		ShardInfo virtualAndLogicShardBean = null;
		Map<Integer, Range> logicVirtualShardMap = config
				.getLogicShardMapVirtualShards();
		Iterator<Entry<Integer, Range>> iter = logicVirtualShardMap.entrySet()
				.iterator();
		while (iter.hasNext()) {
			Entry<Integer, Range> entry = iter.next();
			Range range = entry.getValue();
			Integer logicShardNo = entry.getKey();
			if (virtualShardNo >= range.getBegin()
					&& virtualShardNo <= range.getEnd()) {
				virtualAndLogicShardBean = new ShardInfo();
				virtualAndLogicShardBean.setLogicShardNo(logicShardNo);
				virtualAndLogicShardBean.setVirtualShardNo(virtualShardNo);
				virtualAndLogicShardBean.setDbTable(
						config.getLogicShardMapDBTable().get(logicShardNo));
				break;
			}
		}

		return virtualAndLogicShardBean;
	}

	@Override
	public ShardInfo getShardInfo(String rowKey) {
		byte[] hashByte = getHashValue(rowKey);

		long hashLong = ((long) (hashByte[0] & 0xFF) << 24)
				| ((long) (hashByte[1] & 0xFF) << 16)
				| ((long) (hashByte[2] & 0xFF) << 8)
				| (long) (hashByte[3] & 0xFF);

		Log.debug("rowKey: " + rowKey + " -> hash[" + Long.toHexString(hashLong)
				+ "]");
		SortedMap<Long, ShardInfo> tail = hashKeyShardMap.tailMap(hashLong);
		if (tail.isEmpty()) {
			return hashKeyShardMap.get(hashKeyShardMap.firstKey());
		}
		return tail.get(tail.firstKey());
	}

	/**
	 * 获取hash值，最好是生成32位的，不能低于32位
	 * 
	 * @param rowKey
	 * @return
	 * @throws Exception
	 * @see
	 */
	protected byte[] getHashValue(String rowKey) {
		HashFunction hf = Hashing.murmur3_32();

		byte[] hashByte = {};
		try {
			hashByte = hf.newHasher()
					.putBytes(rowKey.getBytes(ConsistedHashRouter.CHARSET))
					.hash().asBytes();
		} catch (UnsupportedEncodingException e) {
			logger.error("不支持" + ConsistedHashRouter.CHARSET, e);
			throw new RuntimeException("不支持" + ConsistedHashRouter.CHARSET, e);
		}

		return hashByte;
	}

	@Override
	public ShardInfo getDatasourceTable(long virtualShardNo) {
		return this.virtualShardMap.get(virtualShardNo);
	}

	public static void main(String[] args) {
		ShardInfoConfig config = new ShardInfoConfig();
		// 初始虚拟节点256, 0-255
		int virtShardNum = 0xff + 1;
		config.setVirtualShardNum(virtShardNum);
		Map<Integer, Range> logicShardMapVirtualShards = new HashMap<Integer, Range>();
		Map<Integer, DatasourceTable> logicShardMapDBTable = new HashMap<Integer, DatasourceTable>();

		// 配置4个逻辑分区表，2个物理库
		for (int i = 0; i < 4; i++) {
			int begin = (virtShardNum >>> 2) * i;
			int end = (virtShardNum >>> 2) * (i + 1) - 1;
			logicShardMapVirtualShards.put(Integer.valueOf(i),
					new Range(begin, end));
			DatasourceTable dsTable = new DatasourceTable("ds_" + i % 2,
					"t_order_" + i / 2);
			logicShardMapDBTable.put(Integer.valueOf(i), dsTable);

			System.out.println("逻辑分区" + String.valueOf(i) + ": 包括虚拟分区范围["
					+ begin + "-" + end + "]" + " 对应物理分区表："
					+ dsTable.getDatasourceIdRef() + "->"
					+ dsTable.getActualTableName());
		}
		config.setLogicShardMapVirtualShardsConfig(logicShardMapVirtualShards);

		config.setLogicShardMapDBTableConfig(logicShardMapDBTable);

		ConsistedHashRouter router = new ConsistedHashRouter(config);
		router.init();

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

		for (int i = 0; i < 100; i++) {
			String rowKey = Long.toHexString(idGenerator.nextId());
			ShardInfo dsTable = router.getShardInfo(rowKey);
			System.out.println(rowKey + " 对应物理分区表："
					+ dsTable.getDbTable().getDatasourceIdRef() + "->"
					+ dsTable.getDbTable().getActualTableName() + " 逻辑分区["
					+ dsTable.getLogicShardNo() + "]" + " 虚拟分区["
					+ dsTable.getVirtualShardNo() + "]");
		}
	}
}
