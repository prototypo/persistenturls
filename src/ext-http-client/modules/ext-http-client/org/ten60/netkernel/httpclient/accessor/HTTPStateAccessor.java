/******************************************************************************
  (c) Copyright 2002-2005, 1060 Research Ltd                                   

  This Software is licensed to You, the licensee, for use under the terms of   
  the 1060 Public License v1.0. Please read and agree to the 1060 Public       
  License v1.0 [www.1060research.com/license] before using or redistributing   
  this software.                                                               

  In summary the 1060 Public license has the following conditions.             
  A. You may use the Software free of charge provided you agree to the terms   
  laid out in the 1060 Public License v1.0                                     
  B. You are only permitted to use the Software with components or applications
  that provide you with OSI Certified Open Source Code [www.opensource.org], or
  for which licensing has been approved by 1060 Research Limited.              
  You may write your own software for execution by this Software provided any  
  distribution of your software with this Software complies with terms set out 
  in section 2 of the 1060 Public License v1.0                                 
  C. You may redistribute the Software provided you comply with the terms of   
  the 1060 Public License v1.0 and that no warranty is implied or given.       
  D. If you find you are unable to comply with this license you may seek to    
  obtain an alternative license from 1060 Research Limited by contacting       
  license@1060research.com or by visiting www.1060research.com                 

  NO WARRANTY:  THIS SOFTWARE IS NOT COVERED BY ANY WARRANTY. SEE 1060 PUBLIC  
  LICENSE V1.0 FOR DETAILS                                                     

  THIS COPYRIGHT NOTICE IS *NOT* THE 1060 PUBLIC LICENSE v1.0. PLEASE READ     
  THE DISTRIBUTED 1060_Public_License.txt OR www.1060research.com/license      

  File:          $RCSfile: HTTPStateAccessor.java,v $
  Version:       $Name:  $ $Revision: 1.11 $
  Last Modified: $Date: 2007/09/18 11:47:15 $
 *****************************************************************************/

package org.ten60.netkernel.httpclient.accessor;

import org.ten60.netkernel.layer1.nkf.*;
import org.ten60.netkernel.layer1.nkf.impl.*;
import com.ten60.netkernel.util.*;
import com.ten60.netkernel.urii.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.*;
import java.util.*;
import org.ten60.netkernel.httpclient.util.*;
import org.ten60.netkernel.httpclient.representation.*;
import org.ten60.netkernel.layer1.representation.*;

/**
 *  An accessor for managing HTTPState objects - stores HTTPConnectionManager, Cookies, credentials etc etc.
 *  Note:  This class is thread-safe but is *not* optimised wrt synchronization around the HashMaps.
 * @author  pjr
 */
public class HTTPStateAccessor extends NKFAccessorImpl
{
	private static final HashMap mStateMap=new HashMap();
	private static final HashMap mTimeStamps=new HashMap();
	public static final String ACTIVE_HTTP_STATE="active:httpState";
	public static final long DEFAULT_REAP=600000; //10 minutes
	
	/** Creates a new instance of HTTPStateAccessor */
	public HTTPStateAccessor()
	{	super(
			32000,
			true,
			INKFRequestReadOnly.RQT_NEW|
			INKFRequestReadOnly.RQT_EXISTS|
			INKFRequestReadOnly.RQT_SOURCE|
			INKFRequestReadOnly.RQT_SINK|
			INKFRequestReadOnly.RQT_DELETE
		);
	}
	
	public void processRequest(INKFConvenienceHelper context) throws Exception
	{	IURAspect result=null;
		HttpState state=null;
		String stateuri=context.getThisRequest().getArgument("id");
		switch(context.getThisRequest().getRequestType())
		{	case INKFRequestReadOnly.RQT_SOURCE:
				synchronized(mStateMap)
				{	state=(HttpState)mStateMap.get(stateuri);
				}
				if(state==null) throw new NetKernelException("Error Sourcing HttpState", context.getThisRequest().getURI()+" does not exist", null);
				result=new HttpStateAspect(state);
				touch(stateuri);
			break;
			case INKFRequestReadOnly.RQT_SINK:
				synchronized(mStateMap)
				{	state=(HttpState)mStateMap.get(stateuri);
					if(state==null) throw new NetKernelException("Error Sinking HttpState", context.getThisRequest().getURI()+" does not exist", null);
					IAspectHttpState sa=(IAspectHttpState)context.sourceAspect(INKFRequestReadOnly.URI_SYSTEM, IAspectHttpState.class);
					state=sa.getState();
					mStateMap.put(context.getThisRequest().getURI(),state);
				}
				result=new VoidAspect();
				touch(stateuri);
			break;
			case INKFRequestReadOnly.RQT_DELETE:
				synchronized(mStateMap)
				{	state=(HttpState)mStateMap.get(stateuri);
					if(state==null) throw new NetKernelException("Error Deleting HttpState", context.getThisRequest().getURI()+" does not exist", null);
					mStateMap.remove(context.getThisRequest().getURI());
				}
				result=new VoidAspect();
			break;
			case INKFRequestReadOnly.RQT_NEW:
				if (stateuri==null)
				{	synchronized(mStateMap)
					{	do
						{	stateuri=GUID.GUID();
						} while(mStateMap.get(stateuri)!=null);
					}
				}
				
				state=new HttpState();
				//Process Credentials if there are any
				String creduri=context.getThisRequest().getArgument("credentials");
				if(creduri!=null)
				{	IAspectHttpCredentials ca=(IAspectHttpCredentials)context.sourceAspect("this:param:credentials", IAspectHttpCredentials.class);
					state.setCredentials(new AuthScope(ca.getRealm(), ca.getPort(), ca.getHost()), ca.getCredentials());
				}
				synchronized(mStateMap)
				{	mStateMap.put(stateuri,state);
				}
				StringBuffer sb=new StringBuffer();
				sb.append(ACTIVE_HTTP_STATE);
				sb.append("+id@"+stateuri);
				String resultURI=sb.toString();
				result=new URIAspect(java.net.URI.create(resultURI));
				touch(stateuri);
			break;
			case INKFRequestReadOnly.RQT_EXISTS:
				synchronized(mStateMap)
				{	state=(HttpState)mStateMap.get(stateuri);
				}
				result=new BooleanAspect(state!=null);
				touch(stateuri);
			break;
			default:
				throw new NetKernelException("Unknown Request Type");
		}
		//Reap expired state
		reap();
		INKFResponse resp= context.createResponseFrom(result);
		/* 
		 Optionally we can set the resource cacheable - but this would double memory for the state!
		if(!(result instanceof VoidAspect))
		{	resp.setCacheable();
		}
		 */
		context.setResponse(resp);
	}
	
	/**
	 * Delete expired state
	 */
	private void reap()
	{	synchronized(mTimeStamps)
		{	synchronized(mStateMap)
			{	Iterator it=mTimeStamps.keySet().iterator();
				while(it.hasNext())
				{	String key=(String)it.next();
					Long timestamp=(Long)mTimeStamps.get(key);
					if(System.currentTimeMillis()>timestamp.longValue())
					{	mStateMap.remove(key);
						mTimeStamps.remove(key);
					}
				}
			}
		}
	}
	
	/**
	 * Touch state which has been interacted with so that it has a new expiry lease.
	 */
	private void touch(String stateuri)
	{	synchronized(mTimeStamps)
		{	mTimeStamps.put(stateuri,new Long(System.currentTimeMillis()+DEFAULT_REAP));
		}
	}
}
