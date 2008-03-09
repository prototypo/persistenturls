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
 * File:          $RCSfile: RequestState.java,v $
 * Version:       $Name:  $ $Revision: 1.13 $
 * Last Modified: $Date: 2007/08/30 14:49:40 $
 *****************************************************************************/
package com.ten60.netkernel.scheduler;

import com.ten60.netkernel.urrequest.*;
import com.ten60.netkernel.urii.*;
import com.ten60.netkernel.urii.accessor.*;
import com.ten60.netkernel.urii.aspect.*;
import com.ten60.netkernel.util.*;

/**
 * The state of a request throughout its processing by the Scheduler
 * @author  tab
 */
public final class RequestState
{
	public static final int STATE_MAP_REQUEST=0;
	public static final int STATE_BREAKPOINT_BEFORE=1;
	public static final int STATE_TEST_CACHE=2;
	public static final int STATE_PENDING_OTHERS_RESULT=3;
	public static final int STATE_PENDING_ACCESSOR=4;
	public static final int STATE_REQUEST_REPRESENTATION=5;
	public static final int STATE_PENDING_REPRESENTATION=6;
	public static final int STATE_BUSY_ACCESSOR=7;
	public static final int STATE_RELEASED_ACCESSOR=8;
	public static final int STATE_FRAGMENTATION=9;
	public static final int STATE_TRANSREPRESENTATION=10;
	public static final int STATE_RESULT_READY=11;
	public static final int STATE_BREAKPOINT_AFTER=12;
	public static final int STATE_RETURN_RESULT=13;
	public static final int STATE_COMPLETE=14;
	
	private URRequest mOriginalRequest;
	private URRequest mMappedRequest;
	private String mAccessorClass;
	private IURAccessor mAccessor;
	private IURRepresentation mUncastResult;
	private IURRepresentation mResult;
	private int mState;
	private long mTotal;
	private long mLastStart;
	private long mFirstStart;
	private long mLastStop;
	private boolean mException;
	private String mFragment;
	private boolean mNeedsCaching;
	private RequestState mParent;
	
	/** Creates a new instance of RequestState */
	public RequestState(URRequest aOriginalRequest, RequestState aParent)
	{	mParent=aParent;
		mFragment=aOriginalRequest.getURI().getFragment();
		mOriginalRequest = aOriginalRequest;
		mMappedRequest = aOriginalRequest;
		if (aOriginalRequest.getType()==URRequest.RQT_TRANSREPRESENT)
		{	mState = STATE_TRANSREPRESENTATION;
			mUncastResult = aOriginalRequest.getArg(URRequest.URI_SYSTEM);
		}
		else if (aOriginalRequest.getType()==URRequest.RQT_FRAGMENT)
		{	mState = STATE_FRAGMENTATION;
			mUncastResult = aOriginalRequest.getArg(URRequest.URI_SYSTEM);
		}
		else
		{	mState = STATE_MAP_REQUEST;
		}
		mLastStart = 0;
	}
	
	public RequestState getParent()
	{	return mParent;
	}
	
	public boolean resultNeedsCaching()
	{	return mNeedsCaching;
	}
	public void setResultNeedsCaching()
	{	mNeedsCaching=true;
	}
	
	public URRequest getMappedRequest()
	{	return mMappedRequest;
	}
	public URRequest getOriginalRequest()
	{	return mOriginalRequest;
	}
	
	public int getState()
	{	return mState;
	}
	
	public void setMappedRequest(URRequest aRequest)
	{	mMappedRequest = aRequest;
	}
	
	public void setState(int aState)
	{	mState = aState;
	}
	
	public void setException(Throwable aThrowable)
	{	if (aThrowable instanceof Exception)
		{	NetKernelException e = new NetKernelException("Exception during request processing","whilst "+RequestState.typeToString(getState()),getOriginalRequest().toString());
			e.addCause(aThrowable);
			mResult = NetKernelExceptionAspect.create(e);
		}
		else
		{	NetKernelError e = new NetKernelError("Error during request processing","whilst "+RequestState.typeToString(getState()),getOriginalRequest().toString());
			e.addCause(aThrowable);
			mResult = NetKernelExceptionAspect.create(e);
		}
		mException = true;
		setState(STATE_RESULT_READY);
	}
	
	public void setAccessorClass(String aAccessorClass)
	{	mAccessorClass=aAccessorClass;
	}
	public String getAccessorClass()
	{	return mAccessorClass;
	}
	
	public void setAccessor(IURAccessor aAccessor)
	{	mAccessor=aAccessor;
	}
	public IURAccessor getAccessor()
	{	return mAccessor;
	}
	public void setUncastResult(IURRepresentation aResult)
	{	mUncastResult=aResult;
	}
	public IURRepresentation getUncastResult()
	{	return mUncastResult;
	}
	public void setResult(IURRepresentation aResult)
	{	mException=false;
		mResult=aResult;
	}
	public IURRepresentation getResult()
	{	return mResult;
	}
	public boolean wasException()
	{	return mException;
	}
	
	public String getFragment()
	{	return mFragment;
	}
	
	public void pauseTimer()
	{	if (mLastStart!=0)
		{	long now = System.currentTimeMillis();
			mTotal+=(now-mLastStart);
			mLastStart=0;
			mLastStop=now;
		}
	}
	
	public void resumeTimer()
	{	mLastStart=System.currentTimeMillis();
		if (mFirstStart==0)
		{	mFirstStart = mLastStart;
		}
	}
	
	public long getCummulativeTime()
	{	return mLastStop-mFirstStart;
	}
	
	public long getRequestTime()
	{	if (mLastStart!=0)
		{	pauseTimer();
		}
		return mTotal;
	}

	public static String typeToString(int aType)
	{	String result;
		switch(aType)
		{	case STATE_MAP_REQUEST:				result = "mapping request"; break;
			case STATE_TEST_CACHE:	result = "looking for cached result"; break;
			case STATE_PENDING_OTHERS_RESULT: result="pending result from another"; break;
			case STATE_PENDING_ACCESSOR:	result = "requesting accessor"; break;
			case STATE_REQUEST_REPRESENTATION:		result = "requesting result from accessor"; break;
			case STATE_PENDING_REPRESENTATION:		result = "waiting for result..."; break;
			case STATE_BUSY_ACCESSOR:			result = "waiting for busy accessor..."; break;
			case STATE_RELEASED_ACCESSOR:	result = "accessor released"; break;
			case STATE_FRAGMENTATION:	result = "fragmenting result"; break;
			case STATE_TRANSREPRESENTATION:			result = "transrepresenting result"; break;
			case STATE_RESULT_READY:	result = "result ready"; break;
			case STATE_RETURN_RESULT:		result = "returning result"; break;
			case STATE_COMPLETE:			result = "complete"; break;
			case STATE_BREAKPOINT_BEFORE:			result = "breakpoint before"; break;
			case STATE_BREAKPOINT_AFTER:			result = "breakpoint after"; break;
			default:						result = "??"; break;
		}
		return result;
	}	
}