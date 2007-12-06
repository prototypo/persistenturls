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
 * File:          $RCSfile: MappingCache.java,v $
 * Version:       $Name:  $ $Revision: 1.4 $
 * Last Modified: $Date: 2005/11/03 12:53:49 $
 *****************************************************************************/
package com.ten60.netkernel.module;

import com.ten60.netkernel.urrequest.*;
import com.ten60.netkernel.urii.*;
import java.util.*;
/**
 * Quick lookup cache for the request resolution process
 * @author  tab
 */
public class MappingCache
{
	private Map mModules;
	private int mSize;
	private int mCullSize;
	
	//private int h1, h2;
	
	private static class MCKey
	{	private IRequestorContext mModule;
		private String mRequestURIString;
		private List mSuperStack;
		public MCKey(URRequest aRequest)
		{	mModule =  aRequest.getContext();
			mRequestURIString = aRequest.getURI().toString();
			int i=mRequestURIString.indexOf('#');
			if (i>=0)
			{	mRequestURIString=mRequestURIString.substring(0,i);
			}
			mSuperStack = aRequest.getSuperStack();
		}
		
		public boolean equals(Object aOther)
		{	MCKey other = (MCKey)aOther;
			boolean result = mModule==other.mModule && mRequestURIString.equals(other.mRequestURIString);
			if (result)
			{	result=mSuperStack.equals(other.mSuperStack);
			}
			return result;
		}
		
		public int hashCode()
		{	return mRequestURIString.hashCode();
		}
	}
	
	private static class MCValue
	{	private IRequestorContext mModule;
		private String mURA;
		private URIdentifier mMappedURI;
		private List mSuper;
		private int mDepth;
		public MCValue(MappedRequest aRequest)
		{	mModule = aRequest.getMappedRequest().getContext();
			mURA = aRequest.getAccessorClass();
			mMappedURI = aRequest.getMappedRequest().getURI();
			mSuper = aRequest.getMappedRequest().getSuperStack();
			mDepth=aRequest.getContextDepth();
		}
		public MappedRequest getRequest(URRequest aOriginal)
		{	URRequest mapped = aOriginal.rewrite(mMappedURI);
			mapped.setCurrentContext(mModule, mSuper);
			return new MappedRequest(mURA, mapped, mDepth);
		}
	}
	
	
	/** Creates a new instance of MappingCache */
	public MappingCache(int aSize)
	{	mSize = aSize;
		mCullSize = aSize/8;
		mModules = new LinkedHashMap(aSize*3/2, 0.9f);
	}
	
	/** try to find how the requests will be resolved from the cache */
	public MappedRequest get(URRequest aRequest)
	{	MappedRequest result=null;
		//h1++;
		MCValue value;
		MCKey key = new MCKey(aRequest);
		synchronized(mModules)
		{	value = (MCValue)mModules.get(key);
		}

		if (value!=null)
		{	result = value.getRequest(aRequest);
			//h2++;
		}
		//if ( (h1%32)==0) System.out.println("Hit% = "+((h2*100)/h1));
		return result;
	}
	
	/** once a resolution has been performed it can be added to the cache */
	public void put(URRequest aRequest, MappedRequest aMapped)
	{	MCValue value = new MCValue(aMapped);
		MCKey key = new MCKey(aRequest);
		synchronized(mModules)
		{	if (mModules.size()>mSize)
			{	Iterator entries = mModules.entrySet().iterator();
				for (int i=0; i<mCullSize; i++)
				{	entries.next();
					entries.remove();
				}
			}
			mModules.remove(key);
			mModules.put(key, value);
		}
	}
}