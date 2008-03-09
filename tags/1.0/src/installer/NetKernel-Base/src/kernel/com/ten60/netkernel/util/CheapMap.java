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
 * File:          $RCSfile: CheapMap.java,v $
 * Version:       $Name:  $ $Revision: 1.2 $
 * Last Modified: $Date: 2005/09/12 16:36:00 $
 *****************************************************************************/
package com.ten60.netkernel.util;

import java.util.*;

/**
 * Low memory and setup cost map for small (<10) entries. It makes
 * a better trade off between lookup time for small collections
 * @author  tab
 */
public final class CheapMap implements Map
{
	/** implemented as a list of entries */
	private List mList;
	
	private static final class Entry implements Map.Entry
	{	private Object mKey;
		private Object mValue;
		public Entry(Object aKey, Object aValue)
		{	mKey=aKey;
			mValue=aValue;
		}
		public Object getKey()
		{	return mKey;
		}
		public Object getValue()
		{	return mValue;
		}
		public Object setValue(Object aValue)
		{	Object result = mValue;
			mValue=aValue;
			return result;
		}
		
	};
	
	
	/** Creates a new instance of CheapMap */
	public CheapMap(int aInitialSize)
	{	mList = new ArrayList(aInitialSize);
	}
	
	/** Removes all mappings from this map (optional operation).
	 *
	 * @throws UnsupportedOperationException clear is not supported by this
	 * 		  map.
	 *
	 */
	public void clear()
	{	mList.clear();
	}
	
	/** Returns <tt>true</tt> if this map contains a mapping for the specified
	 * key.  More formally, returns <tt>true</tt> if and only if
	 * this map contains at a mapping for a key <tt>k</tt> such that
	 * <tt>(key==null ? k==null : key.equals(k))</tt>.  (There can be
	 * at most one such mapping.)
	 *
	 * @param key key whose presence in this map is to be tested.
	 * @return <tt>true</tt> if this map contains a mapping for the specified
	 *         key.
	 *
	 * @throws ClassCastException if the key is of an inappropriate type for
	 * 		  this map (optional).
	 * @throws NullPointerException if the key is <tt>null</tt> and this map
	 *            does not not permit <tt>null</tt> keys (optional).
	 *
	 */
	public boolean containsKey(Object key)
	{	boolean result=false;
		for (Iterator i=mList.iterator(); i.hasNext(); )
		{	Entry e = (Entry)i.next();
			if (e.getKey().equals(key))
			{	result=true;
				break;
			}
		}
		return result;
	}
	
	/** Returns <tt>true</tt> if this map maps one or more keys to the
	 * specified value.  More formally, returns <tt>true</tt> if and only if
	 * this map contains at least one mapping to a value <tt>v</tt> such that
	 * <tt>(value==null ? v==null : value.equals(v))</tt>.  This operation
	 * will probably require time linear in the map size for most
	 * implementations of the <tt>Map</tt> interface.
	 *
	 * @param value value whose presence in this map is to be tested.
	 * @return <tt>true</tt> if this map maps one or more keys to the
	 *         specified value.
	 * @throws ClassCastException if the value is of an inappropriate type for
	 * 		  this map (optional).
	 * @throws NullPointerException if the value is <tt>null</tt> and this map
	 *            does not not permit <tt>null</tt> values (optional).
	 *
	 */
	public boolean containsValue(Object value)
	{	boolean result=false;
		for (Iterator i=mList.iterator(); i.hasNext(); )
		{	Entry e = (Entry)i.next();
			if (e.getValue().equals(value))
			{	result=true;
				break;
			}
		}
		return result;
	}
	
	/** <b>Not supported</b><br/>
	 Returns a set view of the mappings contained in this map.  Each element
	 * in the returned set is a {@link Map.Entry}.  The set is backed by the
	 * map, so changes to the map are reflected in the set, and vice-versa.
	 * If the map is modified while an iteration over the set is in progress,
	 * the results of the iteration are undefined.  The set supports element
	 * removal, which removes the corresponding mapping from the map, via the
	 * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>, <tt>removeAll</tt>,
	 * <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not support
	 * the <tt>add</tt> or <tt>addAll</tt> operations.
	 *
	 * @return a set view of the mappings contained in this map.
	 *
	 */
	public Set entrySet()
	{	throw new IllegalArgumentException("not supported");
	}
	
	/** Returns the value to which this map maps the specified key.  Returns
	 * <tt>null</tt> if the map contains no mapping for this key.  A return
	 * value of <tt>null</tt> does not <i>necessarily</i> indicate that the
	 * map contains no mapping for the key; it's also possible that the map
	 * explicitly maps the key to <tt>null</tt>.  The <tt>containsKey</tt>
	 * operation may be used to distinguish these two cases.
	 *
	 * <p>More formally, if this map contains a mapping from a key
	 * <tt>k</tt> to a value <tt>v</tt> such that <tt>(key==null ? k==null :
	 * key.equals(k))</tt>, then this method returns <tt>v</tt>; otherwise
	 * it returns <tt>null</tt>.  (There can be at most one such mapping.)
	 *
	 * @param key key whose associated value is to be returned.
	 * @return the value to which this map maps the specified key, or
	 * 	       <tt>null</tt> if the map contains no mapping for this key.
	 *
	 * @throws ClassCastException if the key is of an inappropriate type for
	 * 		  this map (optional).
	 * @throws NullPointerException key is <tt>null</tt> and this map does not
	 * 		  not permit <tt>null</tt> keys (optional).
	 *
	 * @see #containsKey(Object)
	 *
	 */
	public Object get(Object key)
	{	Object result=null;
		for (int i=mList.size()-1; i>=0; i--)
		{	Entry e = (Entry)mList.get(i);
			if (e.getKey().equals(key))
			{	result=e.getValue();
				break;
			}
		}
		return result;
	}
	
	/** Returns <tt>true</tt> if this map contains no key-value mappings.
	 *
	 * @return <tt>true</tt> if this map contains no key-value mappings.
	 *
	 */
	public boolean isEmpty()
	{	return mList.size()==0;
	}
	
	/** Returns a set view of the keys contained in this map.  The set is
	 * backed by the map, so changes to the map are reflected in the set, and
	 * vice-versa.  If the map is modified while an iteration over the set is
	 * in progress, the results of the iteration are undefined.  The set
	 * supports element removal, which removes the corresponding mapping from
	 * the map, via the <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
	 * <tt>removeAll</tt> <tt>retainAll</tt>, and <tt>clear</tt> operations.
	 * It does not support the add or <tt>addAll</tt> operations.
	 *
	 * @return a set view of the keys contained in this map.
	 *
	 */
	public Set keySet()
	{	Set c=new HashSet(mList.size());
		for (Iterator i = mList.iterator(); i.hasNext(); )
		{	Map.Entry e = (Map.Entry)i.next();
			c.add(e.getKey());
		}
		return c;
	}
	
	/** Associates the specified value with the specified key in this map
	 * (optional operation).  If the map previously contained a mapping for
	 * this key, the old value is replaced by the specified value.  (A map
	 * <tt>m</tt> is said to contain a mapping for a key <tt>k</tt> if and only
	 * if {@link #containsKey(Object) m.containsKey(k)} would return
	 * <tt>true</tt>.))
	 *
	 * @param key key with which the specified value is to be associated.
	 * @param value value to be associated with the specified key.
	 * @return previous value associated with specified key, or <tt>null</tt>
	 * 	       if there was no mapping for key.  A <tt>null</tt> return can
	 * 	       also indicate that the map previously associated <tt>null</tt>
	 * 	       with the specified key, if the implementation supports
	 * 	       <tt>null</tt> values.
	 *
	 * @throws UnsupportedOperationException if the <tt>put</tt> operation is
	 * 	          not supported by this map.
	 * @throws ClassCastException if the class of the specified key or value
	 * 	          prevents it from being stored in this map.
	 * @throws IllegalArgumentException if some aspect of this key or value
	 * 	          prevents it from being stored in this map.
	 * @throws NullPointerException this map does not permit <tt>null</tt>
	 *            keys or values, and the specified key or value is
	 *            <tt>null</tt>.
	 *
	 */
	public Object put(Object key, Object value)
	{	Entry e = new Entry(key, value);
		mList.add(e);
		return null;
	}
	
	/** <b>Not supported</b><br/>
	 * Copies all of the mappings from the specified map to this map
	 * (optional operation).  The effect of this call is equivalent to that
	 * of calling {@link #put(Object,Object) put(k, v)} on this map once
	 * for each mapping from key <tt>k</tt> to value <tt>v</tt> in the
	 * specified map.  The behavior of this operation is unspecified if the
	 * specified map is modified while the operation is in progress.
	 *
	 * @param t Mappings to be stored in this map.
	 *
	 * @throws UnsupportedOperationException if the <tt>putAll</tt> method is
	 * 		  not supported by this map.
	 *
	 * @throws ClassCastException if the class of a key or value in the
	 * 	          specified map prevents it from being stored in this map.
	 *
	 * @throws IllegalArgumentException some aspect of a key or value in the
	 * 	          specified map prevents it from being stored in this map.
	 * @throws NullPointerException the specified map is <tt>null</tt>, or if
	 *         this map does not permit <tt>null</tt> keys or values, and the
	 *         specified map contains <tt>null</tt> keys or values.
	 *
	 */
	public void putAll(Map t)
	{	throw new IllegalArgumentException("not supported");
	}
	
	/** Removes the mapping for this key from this map if it is present
	 * (optional operation).   More formally, if this map contains a mapping
	 * from key <tt>k</tt> to value <tt>v</tt> such that
	 * <code>(key==null ?  k==null : key.equals(k))</code>, that mapping
	 * is removed.  (The map can contain at most one such mapping.)
	 *
	 * <p>Returns the value to which the map previously associated the key, or
	 * <tt>null</tt> if the map contained no mapping for this key.  (A
	 * <tt>null</tt> return can also indicate that the map previously
	 * associated <tt>null</tt> with the specified key if the implementation
	 * supports <tt>null</tt> values.)  The map will not contain a mapping for
	 * the specified  key once the call returns.
	 *
	 * @param key key whose mapping is to be removed from the map.
	 * @return previous value associated with specified key, or <tt>null</tt>
	 * 	       if there was no mapping for key.
	 *
	 * @throws ClassCastException if the key is of an inappropriate type for
	 * 		  this map (optional).
	 * @throws NullPointerException if the key is <tt>null</tt> and this map
	 *            does not not permit <tt>null</tt> keys (optional).
	 * @throws UnsupportedOperationException if the <tt>remove</tt> method is
	 *         not supported by this map.
	 *
	 */
	public Object remove(Object key)
	{	Object result=null;
		for (int i=0; i<mList.size(); i++)
		{	Entry e = (Entry)mList.get(i);
			if (e.getKey().equals(key))
			{	result=e.getValue();
				mList.remove(i);
				break;
			}
		}
		return result;
	}
	
	/** Returns the number of key-value mappings in this map.  If the
	 * map contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
	 * <tt>Integer.MAX_VALUE</tt>.
	 *
	 * @return the number of key-value mappings in this map.
	 *
	 */
	public int size()
	{	return mList.size();
	}
	
	/** Returns a collection view of the values contained in this map.  The
	 * collection is backed by the map, so changes to the map are reflected in
	 * the collection, and vice-versa.  If the map is modified while an
	 * iteration over the collection is in progress, the results of the
	 * iteration are undefined.  The collection supports element removal,
	 * which removes the corresponding mapping from the map, via the
	 * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
	 * <tt>removeAll</tt>, <tt>retainAll</tt> and <tt>clear</tt> operations.
	 * It does not support the add or <tt>addAll</tt> operations.
	 *
	 * @return a collection view of the values contained in this map.
	 *
	 */
	public Collection values()
	{	Collection c=new ArrayList(mList.size());
		for (Iterator i = mList.iterator(); i.hasNext(); )
		{	Map.Entry e = (Map.Entry)i.next();
			c.add(e.getValue());
		}
		return c;
	}
	
}
