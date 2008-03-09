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
 * File:          $RCSfile: RequestTable.java,v $
 * Version:       $Name:  $ $Revision: 1.2 $
 * Last Modified: $Date: 2007/09/10 21:11:48 $
 *****************************************************************************/
package com.ten60.netkernel.scheduler;
import com.ten60.netkernel.urrequest.*;

import java.util.*;

/** Simple synchronized blocking queue of request states to be processed
 * @author tab
 */
public class RequestTable
{
    /** the internal storage of the queue **/
    private final ArrayList mInternalList = new ArrayList();
    /** incrementing flag to see if take has been interrupted since starting **/
    private int mInterrupted=0;
	
	public final void put(RequestState aRequest)
    {   synchronized(mInternalList)
        {   //boolean empty = mInternalList.isEmpty();
			mInternalList.add(aRequest);
			mInternalList.notifyAll();
        }
    }
    
    /**
     * Takes an item off the queue blocking indefinately until something
     * is there unless interrupted.
     * @return The QueueItem taken from the queue
     * @exception InterruptedException thrown if we are interrupted whilst waiting for an item
     * either a real thread.interrupt or if this queue is interrupted
     */
    public final RequestState take()
    throws InterruptedException
    {   RequestState result=null;
        boolean waited=false;
        // we must keep looping because we may not acheive success even if we wait.
        // somebody else may jump in first who hasn't waited at all!
        synchronized(mInternalList)
        {   int interruptFlag = mInterrupted;
            do
            {   // see if there is something to take
                {   if (!mInternalList.isEmpty())
                    {   result = (RequestState)mInternalList.remove(0);
                    }
					//if (waited==true) System.out.println("failed take on "+Thread.currentThread().getName());
                    // if not wait until something is added
                    if (result==null)
                    {   //if (waited==false) System.out.println("take wait on "+Thread.currentThread().getName());
						mInternalList.wait(10000);
						waited=true;
                        if (mInterrupted!=interruptFlag)
                        {   // we must have been interrupted
                            throw new InterruptedException();
                        }
                    }
                }
            } while (result==null);
        }
        return result;
    }
	
	/** Take an item from the queue if its session matches. If non available
	 * return immediately
	 */
	public final RequestState takeIfAvailable(IRequestorSession aSession)
	{	RequestState result=null;
		synchronized(mInternalList)
		{	for (int i=mInternalList.size()-1; i>=0; i--)
			{	RequestState state = (RequestState)mInternalList.get(i);
				if (state.getOriginalRequest().getSession()==aSession)
				{	mInternalList.remove(i);
					result=state;
					break;
				}
			}
		}
		return result;
	}
	
	
    
    /**
     * Returns the current number of items in the queue.
     * @return The current number of items in the queue.
     */
    public final int size()
    {   synchronized(mInternalList)
        {   return mInternalList.size();
        }
    }
    
    /** Causes all threads blocking on a take() call to the object to return
     * throwing an InterruptedException
     */    
    public void interrupt()
    {   synchronized(mInternalList)
        {   mInterrupted++;
            mInternalList.notifyAll();
        }
    }
}