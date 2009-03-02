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
 * File:          $RCSfile: MetaImpl.java,v $
 * Version:       $Name:  $ $Revision: 1.3 $
 * Last Modified: $Date: 2007/10/24 09:41:47 $
 *****************************************************************************/
package com.ten60.netkernel.urii.meta;

import com.ten60.netkernel.urii.*;

/**
 * Implementation of IURMeta for representations created by the NetKernel
 * @author  tab
 */
public class MetaImpl implements IURMeta
{
	/** our mime type */
	private String mMimeType;
	/** our pessimistic expiry time */
	private long mExpiryTime;
	/** our cost of creation */
	private int mCost;

	/** Creates a new instance of ComponentMeta
	 * @param aMimeType our mime type
	 * @param aExpiryTime our pessimistic expiry time
	 * @param aCreationCost our cost of creation
	 */
	public MetaImpl(String aMimeType, long aExpiryTime, int aCreationCost)
	{	mMimeType=aMimeType;
		mExpiryTime=aExpiryTime;
		mCost=aCreationCost;
	}
	
	/** Return our mime type */
	public String getMimeType()
	{	return mMimeType;
	}
	/** update our mime type **/
	public void setMimeType(String aMimeType)
	{	mMimeType = aMimeType;
	}
	/** update our pessimistic expiry time */
	protected void setPessimisticExpiryTime(long aExpiryTime)
	{	mExpiryTime=aExpiryTime;
	}
	/** Return our pessimistic expiry time */
	public long getPessimisticExpiryTime()
	{	return mExpiryTime;
	}
	/** default behaviour is always to be expired after expiry time */
	public boolean isExpired()
	{	return mExpiryTime<System.currentTimeMillis();
	}
	/** Return creation cost */
	public int getCreationCost()
	{	return mCost; 
	}
	/** Return usage cost */
	public int getUsageCost()
	{	return 0;
	}
	/** Never intermediate by default */
	public boolean isIntermediate()
	{	return false;
	}
	/** increment the creation cost */
	protected final void incrementCreationCost(int aCost)
	{	mCost+= aCost;
	}
	/** Return the depth of sensitivity on the calling context (super stack). 0 means no sensitivity
	 */
	public int getContextSensitivity()
	{	return 0;
	}
	
	public String toString()
	{	return "MetaImpl: "+mMimeType;
	}
}