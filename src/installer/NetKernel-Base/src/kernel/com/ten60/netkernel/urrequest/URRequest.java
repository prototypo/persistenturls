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
 * File:          $RCSfile: URRequest.java,v $
 * Version:       $Name:  $ $Revision: 1.7 $
 * Last Modified: $Date: 2005/11/02 14:18:19 $
 *****************************************************************************/
package com.ten60.netkernel.urrequest;

import com.ten60.netkernel.urii.*;
import com.ten60.netkernel.util.*;

import java.util.*;

/**
 * A request made by a requestor to a requestee
 * @author  tab
 */
public final class URRequest
{
	/** the URI of the request */
    private URIdentifier mURI;
	/** the current working uri of a request */
	private URIdentifier mCWU;
	/** the requestor */
    private IURRequestor mRequestor;
	/** the request session */
	private IRequestorSession mSession;
	/** the request context */
	private IRequestorContext mContext;
	/** the parent request, i.e. the request that caused this request to be made */
	private URRequest mParent;
	/* the type of request- what to do with the uri */
    private int mType;
	/** the aspect that any resulting representation must contain */
	private Class mAspectClass;
	/** the time the request was created */
	private long mTime;
	/** any by value arguments that are passed with the request */
	private Map mArgs;
	/** a list of IRequestorContexts that the request belongs above the current one */
	private List mSuper;

	/** a request to get the data for a URI */
    public static final int RQT_SOURCE=1;
	/** a request to set the data for a URI */
    public static final int RQT_SINK=2;
	/** a request to see if a resource exists */
    public static final int RQT_EXISTS=4;
	/** a request to delete a resource */
    public static final int RQT_DELETE=8;
	/** a request to create a new resource */
    public static final int RQT_NEW=16;
	/** a request to transpresent data for a resource to a new aspect */
    public static final int RQT_TRANSREPRESENT=32;
	/** a request to fragment a resource */
    public static final int RQT_FRAGMENT=64;
	/** a request constant as a combined mask for all types */
	public static final int RQT_ALL=0x7F;
	
	/** our URI */
	public static final URIdentifier URI_SYSTEM=new URIdentifier("literal:uri_system");
	
	/** @return a human readable representation of the request type */
	public static String typeToString(int aType)
	{	String result;
		switch(aType)
		{	case RQT_SOURCE:	result = "SOURCE"; break;
			case RQT_SINK:		result = "SINK"; break;
			case RQT_EXISTS:	result = "EXISTS"; break;
			case RQT_DELETE:	result = "DELETE"; break;
			case RQT_NEW:		result = "NEW"; break;
			case RQT_TRANSREPRESENT:	result = "TRANSREPRESENT"; break;
			case RQT_FRAGMENT:	result = "FRAGMENT"; break;
			default:			 result = "??"; break;
		}
		return result;
	}
    
	/** Creates a new instance of URRequest
	  @param aURI The URI that is the subject of the request
	  @param aRequestor The requestor
	  @param aSession The session that the request is being made in
	  @param aContext The context that the request is being made in
	  @param aType The type of request
	  @param aCWU The current working uri that relative requests will be made against in the
	 * request
	  @param aParent The parent request of this request
	  @param aRepresentationInterface The java object that an result must implement
	 */
    public URRequest(URIdentifier aURI, IURRequestor aRequestor, IRequestorSession aSession,
		IRequestorContext aContext, int aType,  URIdentifier aCWU, URRequest aParent, Class aAspectClass)
    {   mURI = aURI;
        mRequestor = aRequestor;
		mContext = aContext;
		mSession = aSession;
        mType=aType;
		mAspectClass=aAspectClass;
		mCWU = aCWU;
		mParent = aParent;
		if (mParent!=null)
		{	mSuper = mParent.mSuper;
		}
		else
		{	mSuper = Collections.EMPTY_LIST;
		}
		mTime = System.currentTimeMillis();
    }
    
	/** @return the URI that is the subject of this request */
    public URIdentifier getURI()
    {   return mURI;
    }

    /** @return the URI that is the current working URI of this request */
    public URIdentifier getCWU()
    {   return mCWU;
    }
    /** @return the requestor */
    public IURRequestor getRequestor()
    {   return mRequestor;
    }
	/** @return The session that the request is being made in */
	public IRequestorSession getSession()
	{	return mSession;
	}
	/** @return The context that the request is being made in */
	public IRequestorContext getContext()
	{	return mContext;
	}
    /** @return The type of request */
    public int getType()
    {   return mType;
    }
	/** @return the Time the request was created */
	public long getTime()
	{	return mTime;
	}
	/** @return the parent of this request */
	public URRequest getParent()
	{	return mParent;
	}
	/** @return the java interface that a result to this request should implement */
	public Class getAspectClass()
	{	return mAspectClass;
	}
	/** human readable debug representation of the request */
	public String toString()
	{	StringBuffer sb=new StringBuffer(128);
		sb.append('[');
		sb.append(typeToString(mType));
		sb.append(' ');
		sb.append(mURI.toString());
		sb.append(" in ");
		sb.append(mContext.toString());
		if (mAspectClass!=null)
		{	sb.append(" as ");
			sb.append(mAspectClass.getName());
		}
		sb.append(']');
		return new String(sb);
	}
	/** rewrite the URI of the given request
	 * @param aRewritten the new URI
	 * @return a new Request with the URI rewritten
	 */
	public URRequest rewrite(URIdentifier aRewritten)
	{	URRequest result = new URRequest(aRewritten, mRequestor, mSession, mContext, mType,  mCWU, mParent, mAspectClass );
		result.mArgs=mArgs;
		result.mSuper=mSuper;
		return result;
	}
	/** @return the number of pass-by-value arguments on the request */
	public int argSize()
	{	int result;
		if (mArgs!=null)
		{	result = mArgs.size();
		}
		else
		{	result = 0;
		}
		return result;
	}
	/** @return a pass-by-value argument for the given URI */
	public IURRepresentation getArg(URIdentifier aURI)
	{	IURRepresentation result=null;
		if (mArgs!=null)
		{	result = (IURRepresentation)mArgs.get(aURI);
		}
		return result;
	}
	/** @return the collection of all pass-by-value arguments URIs */
	public Collection getArgs()
	{	Collection result;
		if (mArgs!=null)
		{	result = mArgs.keySet();
		}
		else
		{	result = Collections.EMPTY_LIST;
		}
		return result;
	}
	
	/** Adds a pass-by-value argument to the request
	 * @param aURI the URI of the argument
	 * @param aArg the value of the argument
	 */
	public void addArg(URIdentifier aURI, IURRepresentation aArg)
	{	if (mArgs==null)
		{	mArgs = new CheapMap(4);
		}
		mArgs.put(aURI,aArg);
	}
	
	/** Return the list of super-request contexts
	 */
	public List getSuperStack()
	{	return mSuper;
	}
	/** Return a clone of the list of super-request contexts
	 */
	public List getSuperStackClone()
	{	List result;
		if (mSuper==Collections.EMPTY_LIST)
		{	result = new ArrayList(4);
		}
		else
		{	result = new ArrayList(4+mSuper.size());
			result.addAll(mSuper);
		}
		return result;
	}
	/** @returns true if aOther is an ancestor of this or equal to this */
	public boolean isSubRequest(URRequest aOther)
	{	boolean result=false;
		URRequest r = this;
		while (r!=null)
		{	if (r==aOther)
			{	result=true;
				break;
			}
			else
			{	r=r.getParent();
			}
		}
		return result;
	}
	/** sets the current context and super list of this */
	public void setCurrentContext(IRequestorContext aContext, List aSuper)
	{	mContext=aContext;
		mSuper=aSuper!=null?aSuper:Collections.EMPTY_LIST;
	}
	/** changes the requestor of this request */
	public void setRequestor(IURRequestor aRequestor)
	{	mRequestor = aRequestor;
	}	
	/** changes the session for this request */
	public void  setSession( IRequestorSession aSession)
	{	mSession=aSession;
	}
	
	public boolean equals(Object aOther)
	{	boolean result=false;
		if (aOther instanceof URRequest)
		{	URRequest other=(URRequest)aOther;
			result=(this.mURI.equals(other.mURI))
				&& (this.mType==other.mType)
				&& (this.mContext==other.mContext)
				&& (this.mSuper==other.mSuper);
		}
		return result;
	}
	
	public int hashCode()
	{	return mURI.hashCode();
	}
}