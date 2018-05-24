
/*
* 文件名：ProAllocationEvent.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月16日
* 修改内容：
*/

package com.yspay.common.cluster;

public class LostAllocationEvent extends AllocationEvent {
	public LostAllocationEvent(InstanceNoAllocator eventOwner) {
		super.setEventOwner(eventOwner);
	}
}
