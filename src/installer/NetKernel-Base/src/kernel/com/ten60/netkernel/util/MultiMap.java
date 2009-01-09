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
 * File:          $RCSfile: MultiMap.java,v $
 * Version:       $Name:  $ $Revision: 1.1 $
 * Last Modified: $Date: 2004/06/18 10:15:23 $
 *****************************************************************************/
package com.ten60.netkernel.util;
import java.util.*;

/**
 * Implementation of a MultiMap- a map that can hold multiple values under each key.
 * <br/> Not Threadsafe- use external sychronization
 * @author  tab
 */
public final class MultiMap
{
	/** first line map that may point to a list or a single value */
	private Map mMap;
	/** value from construction of how many values we expect per key- used to size second level lists */
	private int mMultiExpectancy;
	/** the current number of elements in the collection */
	private int mSize;
	
	/** Creates a new instance of MultiMap
	*  @param aInitialCapacity the size of the hash map used at the first level
	 * @param aMultiExpectancy how many values we expect per key
	 */
	public MultiMap(int aInitialCapacity, int aMultiExpectancy)
	{	mMap = new HashMap(aInitialCapacity);
		mMultiExpectancy = aMultiExpectancy;
		mSize=0;
	}
	
	/** put a value into the map with the key, no existing values will be displaced */
	public void put(Object aKey, Object aValue)
	{	Object lookup = mMap.get(aKey);
		if (lookup==null)
		{	mMap.put(aKey, aValue);
		}
		else if (lookup instanceof List)
		{	List pendingList = (List)lookup;
			pendingList.add(aValue);
		}
		else
		{	List pendingList = new ArrayList(mMultiExpectancy);
			pendingList.add(lookup);
			pendingList.add(aValue);
			mMap.put(aKey, pendingList);
		}
		mSize++;
	}
	
	/** retrieve all values for a given key */
	public List get(Object aKey)
	{	List result;
		Object lookup = mMap.get(aKey);
		if (lookup==null)
		{	result = Collections.EMPTY_LIST;
		}
		else if (lookup instanceof List)
		{	result = (List)lookup;
		}
		else
		{	result = Collections.singletonList(lookup);
		}
		return result;
	}
	
	/** remove all values for the given key */
	public List remove(Object aKey)
	{	List list = (List)mMap.remove(aKey);
		mSize-=list.size();
		return list;
	}
	
	/** remove a given value index under a given key
	 * @return true if the value was removed
	 */
	public boolean remove(Object aKey, Object aValue)
	{	boolean removed=false;
		Object lookup = mMap.get(aKey);
		if (lookup instanceof List)
		{	List l = (List)lookup;
			removed=l.remove(aValue);
		}
		else if (lookup==aValue)
		{	mMap.remove(aKey);
			removed=true;
		}
		if (removed) mSize--;
		return removed;
	}
	
	/** return the number of entries in the map
	 */
	public int size()
	{	return mSize;
	}
	
	/** remove all entriues from the map
	 */
	public void clear()
	{	mSize=0;
		mMap.clear();
	}
	
	/** return an iterator over all values **/
	public Iterator valueIterator()
	{	return new MultiIterator();
	}
	
	/** return an  iterator over all keys **/
	public Iterator keyIterator()
	{	return mMap.keySet().iterator();
	}
	
	/** internal implementation of iterator over all values */
	private class MultiIterator implements Iterator
	{
		private Iterator mMapIterator; 
		private Iterator mListIterator;
		private Object mNext;
		
		public MultiIterator()
		{	mMapIterator = mMap.values().iterator();
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
		{	mNext=null;
			while (mNext==null)
			{	if (mListIterator==null)
				{	if (mMapIterator.hasNext())
					{	Object o = mMapIterator.next();
						if (o instanceof List)
						{	mListIterator = ((List)o).iterator();
						}
						else
						{	mNext = o;
						}
					}
					else
					{	break;
					}
				}
				else
				{	if (mListIterator.hasNext())
					{	mNext = mListIterator.next();
					}
					else
					{	mListIterator=null;
					}
				}
			}
		}
	
	}
	
}
