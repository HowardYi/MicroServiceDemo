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

package com.yspay.sample.dubboprovider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.container.Container;
import com.alibaba.dubbo.container.spring.SpringContainer;
import com.yspay.sample.dubboprovider.api.IOrderServiceApi;
import com.yspay.sample.dubboprovider.entity.Order;
import com.yspay.sample.dubboprovider.service.IOrderService;

/**
 * 模仿dubbo启动方式，读取dubbo.properties，去掉优雅停机部分逻辑
 * 
 * @author Cindy
 * @version 2018年5月9日
 * @see Main
 * @since
 */
public class Main {
	public static final String CONTAINER_KEY = "dubbo.container";

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	private static final ExtensionLoader<Container> loader = ExtensionLoader
			.getExtensionLoader(Container.class);

	public static void main(String[] args) {
		try {
			// 设置环境变量env，apollo（集中配置）需要此变量
			String env = System.getProperty("env");
			if (env == null) {
				System.setProperty("env", "FAT");
			}

			if (args == null || args.length == 0) {
				String config = ConfigUtils.getProperty(CONTAINER_KEY,
						loader.getDefaultExtensionName());
				args = Constants.COMMA_SPLIT_PATTERN.split(config);
			}

			final List<Container> containers = new ArrayList<Container>();
			for (int i = 0; i < args.length; i++) {
				containers.add(loader.getExtension(args[i]));
			}
			logger.info("Use container type(" + Arrays.toString(args)
					+ ") to run dubbo serivce.");

			for (Container container : containers) {
				container.start();
				logger.info("Dubbo " + container.getClass().getSimpleName()
						+ " started!");
			}
			System.out.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]")
					.format(new Date()) + " Dubbo service server started!");

			// 测试代码，非dubbo远程调用
			ClassPathXmlApplicationContext applicationContext = SpringContainer
					.getContext();
			try {
				IOrderService orderService = (IOrderService) applicationContext
						.getBean("orderService");

				// 测试内部业务逻辑层
				orderService.clear();
				orderService.fooService();
				orderService.update();
				orderService.select();

			} catch (Exception e) {
				e.printStackTrace();
			}

			// 测试代码，dubbo远程调用
			try {
				IOrderServiceApi orderService = (IOrderServiceApi) applicationContext
						.getBean("orderServiceApiClient");

				Order order = new Order();
				order.setOrderId("8");
				order.setUserId("8");
				order.setCreateDate(new Date());
				order.setStatus("Create-User8-Order8");
				orderService.create(order);

				order = orderService.getOrder("8", "8");
				System.out
						.println(order.getUserId() + "----" + order.getOrderId()
								+ "----" + order.getCreateDate().toString());
			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.println("dubbo服务运行中，按任意键退出");
			System.in.read(); // 按任意键退出

			// 关闭各种container
			for (Container container : containers) {
				try {
					container.stop();
					logger.info("Dubbo " + container.getClass().getSimpleName()
							+ " stopped!");
				} catch (Throwable t) {
					logger.error(t.getMessage(), t);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			System.exit(1);
		}
	}
}
