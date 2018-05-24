
/*
* 文件名：ReAllocationInstanceNoListener.java
* 版权：Copyright by www.ysepay.com
* 修改人：Cindy
* 修改时间：2018年5月17日
* 修改内容：
*/

package com.yspay.common.cluster;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 当分配不成功时，定时重新分配
 * 
 * @author Cindy
 * @version 2018年5月17日
 * @see ReAllocationInstanceNoListener
 * @since
 */
public class ReAllocationInstanceNoListener implements InstanceNoListener {

	@Override
	public void onPostAllocation(final PostAllocationEvent event) {
		if (!event.isStatus()) {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					InstanceNoAllocator eventOwner = (InstanceNoAllocator) (event
							.getEventOwner());
					eventOwner.reAllocationInstanceNo();
				}

			}, 5 * 1000l);
		}
	}

	@Override
	public void onPreAllocation(PreAllocationEvent event) {

	}

	@Override
	public void onLostAllocation(LostAllocationEvent event) {

	}

}
