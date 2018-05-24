
/*
* 文件名：IdGeneratorInstanceNoListener.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月17日
* 修改内容：
*/

package com.yspay.common.idgenerator;

import com.yspay.common.cluster.InstanceNoListener;
import com.yspay.common.cluster.LostAllocationEvent;
import com.yspay.common.cluster.PostAllocationEvent;
import com.yspay.common.cluster.PreAllocationEvent;

public class IdGeneratorInstanceNoAllocatorListener
		implements InstanceNoListener {
	private IdGenerator idGenerator;

	public void setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}

	public IdGeneratorInstanceNoAllocatorListener(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}

	@Override
	public void onPostAllocation(PostAllocationEvent event) {
		idGenerator.setInstanceNo(event.getInstanceNo());
	}

	@Override
	public void onPreAllocation(PreAllocationEvent event) {
		idGenerator.setInstanceNo(-1);
	}

	@Override
	public void onLostAllocation(LostAllocationEvent event) {
		idGenerator.setInstanceNo(-1);
	}
}
