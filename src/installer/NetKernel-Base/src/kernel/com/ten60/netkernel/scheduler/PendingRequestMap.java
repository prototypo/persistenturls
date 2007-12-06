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
 * File:          $RCSfile: PendingRequestMap.java,v $
 * Version:       $Name:  $ $Revision: 1.17 $
 * Last Modified: $Date: 2005/10/05 08:19:57 $
 *****************************************************************************/
package com.ten60.netkernel.scheduler;

import com.ten60.netkernel.urrequest.*;
import com.ten60.netkernel.util.*;
import java.util.*;
import java.io.*;

/**
 * A Map of pending request states tied to child requests.
 * @author  tab
 */
public final class PendingRequestMap
{
	private final Map mMap = new IdentityHashMap();
	private final List mPendingResults = new ArrayList(16);
	
	public synchronized void put(URRequest aRequest, RequestState aState)
	{	mMap.put(aRequest, aState);
	}
	
	public synchronized RequestState remove(URRequest aRequest)
	{	return (RequestState)mMap.remove(aRequest);
	}
	
	public synchronized RequestState get(URRequest aRequest)
	{	return (RequestState)mMap.get(aRequest);
	}
	
	public synchronized int size()
	{	return mMap.size();
	}
	
	public synchronized boolean hasEquivalentInProgress(RequestState aState)
	{	URRequest request=aState.getMappedRequest();
		for (Iterator i=mMap.keySet().iterator(); i.hasNext(); )
		{	URRequest request2=(URRequest)i.next();
			if (request2.equals(request))
			{	aState.setState(RequestState.STATE_PENDING_OTHERS_RESULT);
				mPendingResults.add(aState);
				return true;
			}
		}
		return false;
	}
	
	public synchronized void notifyOfAvailableResult(RequestState aState, RequestTable aTable)
	{	URRequest request=aState.getMappedRequest();
		for (Iterator i=mPendingResults.iterator(); i.hasNext(); )
		{	RequestState state = (RequestState)i.next();
			if (state.getMappedRequest().equals(request))
			{	i.remove();
				state.setState(RequestState.STATE_TEST_CACHE);
				aTable.put(state);
			}
		}
	}
	
	public synchronized RequestState getNewestStateForSession(IRequestorSession aSession)
	{	RequestState result=null;
		long newestTime=0;
		for (Iterator i = mMap.entrySet().iterator(); i.hasNext(); )
		{	Map.Entry entry = (Map.Entry)i.next();
			URRequest req = (URRequest)entry.getKey();
			if (req.getSession().equals(aSession))
			{	if (req.getTime()>newestTime)
				{	newestTime=req.getTime();
					result=(RequestState)entry.getValue();
				}
			}
		}
		return result;
	}
	public synchronized URRequest getRootRequestForSession(IRequestorSession aSession)
	{	URRequest result=null;
		long time=Long.MAX_VALUE;
		int depth=Integer.MAX_VALUE;
		for (Iterator i = mMap.entrySet().iterator(); i.hasNext(); )
		{	Map.Entry entry = (Map.Entry)i.next();
			URRequest req = (URRequest)entry.getKey();
			if (req.getSession().equals(aSession))
			{	if (req.getTime()<=time)
				{	int d=0;
					for (URRequest r=req.getParent(); r!=null; r=r.getParent())
					{	d++;
					}
					if (d<depth)
					{	RequestState state=(RequestState)entry.getValue();
						if (state!=null)
						{	result=state.getOriginalRequest();
							time=result.getTime();
							depth=d;
						}
					}
				}
			}
		}
		return result;
	}
	
	/** Attempt to terminate execution of a session- this will
	 * either attempt to kill thread running request or force an orphaned response
	 * if asynch session has failed to return a response in error
	 * @param aSession the session to kill
	 * @param aStateToThreadMap the map to lookup possible thread associated with request
	 * @param aTable the table of pending requests for a session
	 * @param aOrphan if true a dummy response will be returned if no thread is found for request, otherwise
	 * nothing will happen
	 * @param aErrorId the error id that failing request will throw
	 * @return true if the session was satisfactorily killed
	 */
	public synchronized boolean kill(IRequestorSession aSession, Map aStateToThreadMap, RequestTable aTable, boolean aOrphan, String aErrorId)
	{	boolean result=false;
		RequestState state = getNewestStateForSession(aSession);
		if (state!=null)
		{	NetKernelError deathError = new NetKernelError(aErrorId,"process with pid ["+aSession.getId()+"] has been terminated",null);
			synchronized(aStateToThreadMap)
			{	Thread t = (Thread)aStateToThreadMap.get(state);
				if (t!=null)
				{	result=true;
					StringWriter dump=null;
					if (Utils1_5.isSuitable())
					{	// capture thread state before will kill it
						dump=new StringWriter(2048);
						try
						{	Utils1_5.dumpThread(dump, t);
						} catch (IOException e)
						{	e.printStackTrace(); /* should never happen on a string writer */ }
					}	
					t.stop(deathError);
					if (dump!=null)
					{	try
						{	org.w3c.dom.Document d = XMLUtils.parse(new StringReader(dump.toString()));
							String tidyDUMP=XMLUtils.getInstance().toXML(d,true,true);
							SysLogger.log(SysLogger.SEVERE, this, tidyDUMP);
						} catch (Exception e)
						{	e.printStackTrace(); /* should never happen on a string writer */ }
					}
					else
					{	SysLogger.log(SysLogger.CONTAINER, this, "Thread dump of deadlock not available on JDK version < 1.5.0");
					}
				}
			}
			if (!result && aOrphan)
			{	// appears to be a dangling request
				mMap.remove(state.getMappedRequest());
				state.setException(deathError);
				aTable.put(state);
				result=true;
			}
			
		}
		return result;
	}
	
}