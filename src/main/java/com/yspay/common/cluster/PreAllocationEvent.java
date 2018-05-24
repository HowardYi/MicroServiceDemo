
/*
* 文件名：ProAllocationEvent.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月16日
* 修改内容：
*/

package com.yspay.common.cluster;

public class PreAllocationEvent extends AllocationEvent {
	public PreAllocationEvent(InstanceNoAllocator eventOwner) {
		super.setEventOwner(eventOwner);
	}
}
