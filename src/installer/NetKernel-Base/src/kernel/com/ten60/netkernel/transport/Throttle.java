/******************************************************************************
 * (c) Copyright 2002,2003, 1060 Research Ltd
 *
 * This Software is licensed to You, the licensee, for use under the terms of
 * the 1060 Public License v1.0. Please read and agree to the 1060 Public
 * License v1.0 [www.1060research.com/license] before using or redistributing
 * this software.
 *
 * In summary the 1060 Public license has the following conditions.
 * A. You may use the Software free of charge provided you agree to the terms
 * laid out in the 1060 Public License v1.0
 * B. You are only permitted to use the Software with components or applications
 * that provide you with OSI Certified Open Source Code [www.opensource.org], or
 * for which licensing has been approved by 1060 Research Limited.
 * You may write your own software for execution by this Software provided any
 * distribution of your software with this Software complies with terms set out
 * in section 2 of the 1060 Public License v1.0
 * C. You may redistribute the Software provided you comply with the terms of
 * the 1060 Public License v1.0 and that no warranty is implied or given.
 * D. If you find you are unable to comply with this license you may seek to
 * obtain an alternative license from 1060 Research Limited by contacting
 * license@1060research.com or by visiting www.1060research.com
 *
 * NO WARRANTY:  THIS SOFTWARE IS NOT COVERED BY ANY WARRANTY. SEE 1060 PUBLIC
 * LICENSE V1.0 FOR DETAILS
 *
 * THIS COPYRIGHT NOTICE IS *NOT* THE 1060 PUBLIC LICENSE v1.0. PLEASE READ
 * THE DISTRIBUTED 1060_Public_License.txt OR www.1060research.com/license
 *
 * File:          $RCSfile: Throttle.java,v $
 * Version:       $Name:  $ $Revision: 1.3 $
 * Last Modified: $Date: 2004/09/20 13:58:39 $
 *****************************************************************************/
package com.ten60.netkernel.transport;

/**
 * The throttle limits the number of concurrent requests that may pass from the transport manager
 * into the scheduler. It forces requests to wait until there is capacity to accomodate them.
 * @author  tab
 */
public class Throttle
{
	/** the maximum number of concurrent requests we will allow */
	private int mMaxCount;
	/** the maximum number of queued requests we will allow */
	private int mQueueMax;
	/** the actual number of concurrent requests we current have */
	private int mCount;
	/** the number of requests queued */
	private int mQueue;
	
	/** Creates a new instance of Throttle with a default size of 5 */
	public Throttle()
	{	setMaxCount(5);
		setMaxQueue(10);
	}
	
	/** Configure the number of concurrent requests we will permit */
	public void setMaxCount(int aCount)
	{	mMaxCount=aCount;
	}
	/** Configure the maximum queue size for blocked requests */
	public void setMaxQueue(int aCount)
	{	mQueueMax=aCount;
	}
	
	/** A blocking call to obtain permission to proceed with request.
	 * when the method returns normally permission to proceed has
	 * been granted
	 * @exception InterruptedException thrown if system interrupted
	 * @exception ThrottleOverloadException thrown if queue size becomes too large
	 * before permission has been given
	 */
	public synchronized void throttle() throws InterruptedException, ThrottleOverloadException
	{	
		if (mCount<mMaxCount)
		{	mCount++;
		}
		else
		{	if (mQueue<mQueueMax)
			{	mQueue++;
				while (mCount>=mMaxCount)
				{	wait();
				}
				mCount++;
				mQueue--;
			}
			else
			{	throw new ThrottleOverloadException();
			}
		}
	}
	
	/** Informs the throttle that a request has completed and thus if
	 * necessary can release another request to execute
	 */
	public synchronized void notifyOfReturn()
	{	if (mQueue>0)
		{	notify();
		}
		mCount--;
	}
	
	/** Returns true if there is any work to do
	 */
	public synchronized boolean isBusy()
	{	return mCount>0 || mQueue>0;
	}
	
	public int getConcurrentCount()
	{	return mCount;
	}
	
	public int getQueueSize()
	{	return mQueue;
	}
	
}