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
 * File:          $RCSfile: AsynchLock.java,v $
 * Version:       $Name:  $ $Revision: 1.1 $
 * Last Modified: $Date: 2004/06/18 10:15:23 $
 *****************************************************************************/
package com.ten60.netkernel.util;

import java.util.*;

/**
 *	Atomic lock request and aquire. unlock might return a released previously unaquired lock request if one
 * exists.
 * <br/>not thread safe- external synchronization must be provided.
 * @author  tab
 */
public class AsynchLock
{	/** map of lock keys to list of unaquired lock request references */
	private Map mKeyToReferences;
	/** expected lock contention count- so as to allocate a list long enough to hold them without needing to expand */
	private int mContentionCount;
	/** value held in mKeyToReferences when a lock is held but there are no blocked requests */
	private static Object sNoPendingLocks = Boolean.FALSE;
	
	/** Creates a new instance of AsynchLock
	  @param aKeyCount size to make lock map
	 *@param aContentionCount expected lock contention count- so as to allocate a list long enough to hold them without needing to expand
	 */
	public AsynchLock(int aKeyCount, int aContentionCount)
	{	mKeyToReferences = new HashMap(aKeyCount);
		mContentionCount = aContentionCount;
	}
	
	/** Attempt to gain an exclusive lock on the given key, takes a reference object to hang onto if
	 * the lock fails to be granted that is then returned on an unlock when the lock has been granted
	 * @param aKey the key object that we want to gain an exclusive lock on
	 * @param aReference a reference object for the request to lock
	 * @return true if the lock has been granted, false if not
	 */
	public boolean lock(Object aKey, Object aReference)
	{	boolean result;
		Object lookup = mKeyToReferences.get(aKey);
		if (lookup==null)
		{	result = true;
			mKeyToReferences.put(aKey, sNoPendingLocks);
		}
		else
		{	result = false;
			List pendingList;
			if (lookup==sNoPendingLocks)
			{	pendingList = new ArrayList(mContentionCount);
				mKeyToReferences.put(aKey, pendingList);
			}
			else
			{	pendingList = (List)lookup;
			}
			pendingList.add(aReference);
		}
		return result;
	}
	
	/** releases a lock on the given key, this method must only be called after a lock has been granted.
	 * The returned lock request reference must be acted on by the client and not ignored.
	 * @param aKey the key object that we are releasing the exclusive lock on
	 * @return a lock request reference object to a previously ungranted lock request, null if
	 * there is none. 
	 */
	public Object unlock(Object aKey)
	{	Object result=null;
		Object lookup = mKeyToReferences.remove(aKey);
		if (lookup instanceof List)
		{	List pendingList = (List)lookup;
			result = pendingList.remove(0);
			if (pendingList.isEmpty())
			{	mKeyToReferences.put(aKey,sNoPendingLocks);
			}
			else
			{	mKeyToReferences.put(aKey, pendingList);
			}
		}
		return result;
	}
	
	/** returns an iterator over all pending lock request references
	 */
	public Iterator getPendingReferences()
	{	return new LockIterator();
	}
	
	/** Inner class to implement the iterator over the map of lists
	 */
	private class LockIterator implements Iterator
	{
		private Iterator mMapIterator; 
		private Iterator mListIterator;
		private Object mNext;
		
		public LockIterator()
		{	mMapIterator = mKeyToReferences.values().iterator();
			findNext();
		}
		
		public boolean hasNext()
		{	return mNext!=null;
		}
		
		public Object next()
		{	Object result = mNext;
			findNext();
			return result;
		}
		
		public void remove()
		{	throw new UnsupportedOperationException();
		}
		
		private void findNext()
		{	while (mListIterator==null || !mListIterator.hasNext())
			{	if (mMapIterator.hasNext())
				{	Object o = mMapIterator.next();
					if (o!=sNoPendingLocks)
					{	List list = (List)o;
						mListIterator = list.iterator();
					}
					else
					{	mListIterator=null;
					}
				}
				else
				{	mListIterator = null;
					break;
				}
			}
			if (mListIterator!=null)
			{	mNext=mListIterator.next();
			}
			else
			{	mNext=null;
			}
		}
	}
	
	
	
}
