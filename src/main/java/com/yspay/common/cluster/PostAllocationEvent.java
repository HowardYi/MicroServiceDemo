
/*
* 文件名：PostAllocationEvent.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月16日
* 修改内容：
*/

package com.yspay.common.cluster;

public class PostAllocationEvent extends AllocationEvent {
	private int instanceNo;

	// true为分配成功，false分配失败
	private boolean status;

	public PostAllocationEvent(int instanceNo, boolean status,
			InstanceNoAllocator eventOwner) {
		super.setEventOwner(eventOwner);
		this.instanceNo = instanceNo;
		this.status = status;
	}

	public int getInstanceNo() {

		return instanceNo;
	}

	public boolean isStatus() {

		return status;
	}

}
