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
 * File:          $RCSfile: Debugger.java,v $
 * Version:       $Name:  $ $Revision: 1.3 $
 * Last Modified: $Date: 2007/08/30 14:49:40 $
 *****************************************************************************/
package com.ten60.netkernel.scheduler.debug;
import com.ten60.netkernel.scheduler.*;
import com.ten60.netkernel.urrequest.*;
import java.util.*;
/**
 * The debugger has the responsibility of managing the lists of breakpoints
 * and requests stopped by breakpoints. The Scheduler checks with the
 * debugger to see if a request should be stopped.
 * @author  tab
 */
public class Debugger
{
	/** the list of breakpoints **/
	private final List mBreakpoints;
	/** the list of requests stopped on breakpoints */
	private final List mBreakpointedStates;
	/** a back reference to the scheduler */
	private Scheduler mScheduler;
	/** single registered listener */
	private IDebuggerListener mListener;
	
	/** Create an initialise the Debugger */
	public Debugger(Scheduler aScheduler)
	{	mBreakpoints = new ArrayList();
		mBreakpointedStates = Collections.synchronizedList(new ArrayList());
		mScheduler = aScheduler;
	}
	
	public void setListener(IDebuggerListener aListener)
	{	mListener=aListener;
	}
	
	public IDebuggerListener getListener()
	{	return mListener;
	}
	
	/** Add a new breakpoint */
	public void addBreakpoint(IBreakpoint aBreakpoint)
	{	mBreakpoints.add(aBreakpoint);
	}
	/** remove an existing breakpoint */
	public void removeBreakpoint(IBreakpoint aBreakpoint)
	{	mBreakpoints.remove(aBreakpoint);
	}
	/** return the list of breakpoints */
	public List getBreakpoints()
	{	return mBreakpoints;
	}
	
	/** Called by the Scheduler to see if the current
	 * request state matches a breakpoint and should
	 * be stopped. If a breakpoint is found the
	 * method returns true and the request is put into
	 * either STATE_BREAKPOINT_AFTER or STATE_BREAKPOINT_BEFORE
	 * depending upon where it was when it was breakpointed
	 */
	public boolean catchBreakpoint(RequestState aState)
	{	boolean result = false;
		
		if (mListener!=null)
		{	mListener.breakpointEvent(aState);
		}
		
		if (!mBreakpoints.isEmpty())
		{	IBreakpoint found=null;
			boolean negative=false;
			for (Iterator i = mBreakpoints.iterator(); i.hasNext(); )
			{	IBreakpoint bp = (IBreakpoint)i.next();
				
				if (bp.matches(aState,this))
				{	if (bp.isPositive())
					{	if (found==null)
						{	found=bp;
						}
					}
					else
					{	negative=true;
						break;
					}
				}
			}
			if (found!=null && !negative)
			{	if (aState.getState()==RequestState.STATE_RESULT_READY)
				{	aState.setState(RequestState.STATE_BREAKPOINT_AFTER);
				}
				else
				{	aState.setState(RequestState.STATE_BREAKPOINT_BEFORE);
				}
				BreakpointedState bps = new BreakpointedState(aState, found);
				mBreakpointedStates.add(bps);
				result = true;
			}
		}
		return result;
	}
	
	/** Return the list of breakpointed states
	 */
	public List getBreakpointedStates()
	{	return mBreakpointedStates;
	}
	
	/** Release the given state */
	public void release(BreakpointedState aState)
	{	if (mBreakpointedStates.remove(aState))
		{	mScheduler.releaseBreakpointedState(aState.getState());
		}
	}
	
	/** Release all breakpointed states */
	public void releaseAll()
	{	for (Iterator i = mBreakpointedStates.iterator(); i.hasNext(); )
		{	BreakpointedState bps = (BreakpointedState)i.next();
			i.remove();
			mScheduler.releaseBreakpointedState(bps.getState());
		}
	}
	
	/** Find the request which initiated the call stack
	 * ending in aState.
	 */
	public URRequest getRootOf(RequestState aState)
	{	
		while (aState.getParent()!=null)
		{	aState=aState.getParent();
		}
		return aState.getOriginalRequest();
		/*
		URRequest request = aState.getOriginalRequest();
		while(request.getParent()!=null)
		{	request=request.getParent();
		}
		if (request!=aState.getOriginalRequest())
		{	RequestState state = mScheduler.getPendingStateFor(request);
			if (state!=null)
			{	request = state.getOriginalRequest();
			}
		}
		return request;
		 */
	}
	
	/* Return the request state for a given request
	 */
	public RequestState getStateFor(URRequest aRequest)
	{	return mScheduler.getPendingStateFor(aRequest);
	}
}