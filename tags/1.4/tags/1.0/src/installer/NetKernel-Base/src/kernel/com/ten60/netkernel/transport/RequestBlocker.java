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
 * File:          $RCSfile: RequestBlocker.java,v $
 * Version:       $Name:  $ $Revision: 1.3 $
 * Last Modified: $Date: 2004/11/19 15:05:21 $
 *****************************************************************************/
package com.ten60.netkernel.transport;


import java.util.*;
/**
 * RequestBlocker<br/>
 * once block() is called all calls to check() will block. If interrupt() is called then all calls to check() with that
 * id will thrown interrupted exceptions when release() is called and until clear() is called. All non interrupted
 * calls to check() will return normally when release is called.
 * @author  tab
 */
public class RequestBlocker
{
	/** are we blocking or not */
	private boolean mBlocked;
	/** list of ids to interrupt */
	private List mInterrupted;
	/** list of ids blocked **/
	private List mBlockedList;
	/** true if we have call releaseInterrupted **/
	private boolean mReleased;
	
	/** Blocking call to check if we are blocked or not. If the id is
	 * interrupted the method will throw an interrupted exception
	 * otherwise it will return normally
	 */
	public void check(Object aId) throws InterruptedException
	{	if (mBlocked || mInterrupted!=null)
		{	synchronized(aId)
			{	boolean interrupted = (mInterrupted!=null && mInterrupted.contains(aId));
				if (mBlocked && !(interrupted && mReleased))
				{	synchronized(this)
					{	mBlockedList.add(aId);
					}
					aId.wait();
				}
				if (interrupted)
				{	throw new InterruptedException();
				}
			}
		}
	}
	
	/** Start blocking all check requests
	 */
	public synchronized void block()
	{	if (!mBlocked)
		{	mBlocked = true;
			mInterrupted = new ArrayList();
			mBlockedList = new ArrayList();
		}
	}
	
	/** Mark this Id to be interrupted
	 */
	public synchronized void interrupt(Object aId)
	{	mInterrupted.add(aId);
	}

	/** Release all blocked requests
	 */
	public synchronized void release()
	{	if (mBlocked)
		{	mBlocked = false;
			for (Iterator i = mBlockedList.iterator(); i.hasNext(); )
			{	Object id = i.next();
				synchronized(id)
				{	id.notifyAll();
				}
			}
		}
	}
	
	/** Release only interrupted blocked requests
	 */
	public synchronized void releaseInterrupted()
	{	if (mBlocked)
		{	mReleased=true;
			for (Iterator i = mInterrupted.iterator(); i.hasNext(); )
			{	Object id = i.next();
				synchronized(id)
				{	id.notifyAll();
				}
			}
		}
	}

	/** Clear interrupted list so that all checks can proceed optimally
	 */
	public synchronized void clear()
	{	mInterrupted=null;
		release();
		mBlockedList=null;
		mReleased=false;
	}
}