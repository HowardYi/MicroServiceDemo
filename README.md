# MicroServiceDemo
微服务项目示例
整合了dubbo2.5.11+spring 4+mybatis3.4.6+sharding-jdbc1.5.4.1+apollo(集中配置)
包括微服务的rpc调用，数据库的增删改查，分布式集群唯一命名，分布式id生成，数据分布算法（hash一致性+pre-shard）

准备环境：
如果你没有apollo环境，把\src\main\resources\META-INF\spring\applicationContext.xml文件下面部分注释掉：
    <!-- 集中配置 -->
    <import resource="apolloContext.xml"/>
同时去掉pom.xml里面的apollo依赖包：
   <dependency>
			<groupId>com.ctrip.framework.apollo</groupId>
			<artifactId>apollo-client</artifactId>
			<version>${apollo.client.version}</version>
		</dependency>

缺失的配置，需要你自己用实际参数替代，比如数据库的地址，密码，zk注册中心地址等。
datasourceContext.xml，dubboCommonContext.xml，idGeneratorContext.xml三个配置文件里面需要你修改地址信息。

运行测试：
Main.java可以测试远程rpc调用方式：基本的增删改查，分库分表的主键和非主键路由算法测试，hint方式路由测试, 分页排序功能测试，多表并发查询结果归并测试。
DistributionInstanceNoAllocator.java测试集群唯一编号生成功能。
DefaultIdGeneratorImpl.java测试雪花算法id生成功能。
ConsistedHashRouter.java测试一致性hash算法数据分布功能。


