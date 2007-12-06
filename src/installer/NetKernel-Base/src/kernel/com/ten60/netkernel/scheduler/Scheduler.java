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
 * File:          $RCSfile: Scheduler.java,v $
 * Version:       $Name:  $ $Revision: 1.51 $
 * Last Modified: $Date: 2007/10/24 15:35:43 $
 *****************************************************************************/
package com.ten60.netkernel.scheduler;

import com.ten60.netkernel.container.*;
import com.ten60.netkernel.urii.*;
import com.ten60.netkernel.urrequest.*;
import com.ten60.netkernel.util.*;
import com.ten60.netkernel.urii.aspect.*;
import com.ten60.netkernel.urii.accessor.*;
import com.ten60.netkernel.urii.representation.*;
import com.ten60.netkernel.module.*;
import com.ten60.netkernel.cache.*;
import com.ten60.netkernel.scheduler.debug.Debugger;
import com.ten60.netkernel.transport.TransportInitiatedSession;
import com.ten60.netkernel.urii.fragment.IFragmentor;

import java.util.*;
import java.io.*;

/**
 * The Scheduler system component. Responsible for taking a request from a transport or accessor
 * and mapping it to an accessor and then executing that accessor.
 * @author  tab
 */
public final class Scheduler extends ComponentImpl implements IURRequestee, IURSynchRequestee, IURRequestor
{
	public static final URIdentifier URI = new URIdentifier("netkernel:scheduler");

	private RequestTable mTable = new RequestTable();
	private PendingRequestMap mPendingResults = new PendingRequestMap();
	private WorkerThreadPool mThreadPool;
	private ModuleManager mModuleManager;
	private Cache mCache;
	private Container mContainer;
	private SynchronousRequestor mSynchronousRequestor;
	private BusyAccessorTable mBusyAccessors = new BusyAccessorTable();
	private long mDeadlockPeriod;
	private Debugger mDebugger;
	private Map mSessionToLastRequestMap = Collections.synchronizedMap(new WeakHashMap());
	private Map mRequestThreads = new HashMap();
	private int mInitialThreadCount;
	private int mActiveThreadCount;
	
	/** Creates a new instance of Scheduler */
	public Scheduler()
	{	super(URI);
		mSynchronousRequestor = new SynchronousRequestor();
		mStats = new RequestStatistics();
		mDebugger = new Debugger(this);
	}
	
	public Debugger getDebugger()
	{	return mDebugger;
	}
	
	public RequestState getPendingStateFor(URRequest aRequest)
	{	return mPendingResults.get(aRequest);
	}
	
	public URResult requestSynch(URRequest aRequest) throws NetKernelException
	{	aRequest.setRequestor(mSynchronousRequestor);
		innerRequestAsynch(aRequest,true);
		SynchronousRequestor.ResultStruct result=null;
		try 
		{	while (result==null)
			{	synchronized(aRequest)
				{	result=(SynchronousRequestor.ResultStruct)mSynchronousRequestor.get(aRequest);
					if (result==null)
					{	aRequest.wait();
					}
				}
			}
			
			Thread.currentThread().setContextClassLoader(((ModuleDefinition)aRequest.getContext()).getClassLoader());
			if (result.isException)
			{	IAspectNetKernelException aspect = (IAspectNetKernelException)result.result.getResource().getAspect(IAspectNetKernelException.class);
				if (aspect.getError()!=null)
				{	// runtime error
					throw aspect.getError();
				}
				else
				{	// regular exception
					throw aspect.getException();
				}
			}
			else
			{	return result.result;
			}				
		} catch (InterruptedException e)
		{	NetKernelError e2 = new NetKernelError("Interrupted");
			throw e2;
		}
	}
	
	
	public void requestAsync(URRequest aRequest)
	{	innerRequestAsynch(aRequest,false);
	}
	
	private void innerRequestAsynch(URRequest aRequest, boolean aExecInline)
	{
		RequestState state;
		URRequest parent =	aRequest.getParent();
		if (parent!=null)
		{	RequestState parentState = (RequestState)mPendingResults.get(parent);
			state = new RequestState(aRequest, parentState);
			if (parentState!=null)
			{	parentState.pauseTimer();
			}
		}
		else
		{	state=new RequestState(aRequest,null);
		}
		
		if (aExecInline)
		{	processRequest(state,false);
		}
		else
		{	mTable.put(state);
		}
	}
 
	public void start(Container aContainer) throws NetKernelException
	{	mContainer=aContainer;
		mModuleManager = (ModuleManager)aContainer.getComponent(ModuleManager.URI);
		mCache = (Cache)aContainer.getComponent(Cache.URI);
		XMLReadable config = ((Config)aContainer.getComponent(Config.URI)).getReadable();
		mInitialThreadCount = config.getInt("system/schedulerThreads",1);
		mDeadlockPeriod = config.getInt("system/deadlockPeriod",8000);
		mActiveThreadCount=0;
		mThreadPool = new WorkerThreadPool("Scheduler", mInitialThreadCount, mContainer.getRootThreadGroup() )
		{	public void process()
			{	processRequest();
			}
		};
		mThreadPool.start();
	}
	
	public void stop()
	{	long startTime=System.currentTimeMillis();
		
		// poll until all asynch requests have completed
		while(System.currentTimeMillis()-startTime<mDeadlockPeriod)
		{	if (mPendingResults.size()==0)
			{	break;
			}
			else
			{	try
				{	Thread.currentThread().sleep(100);
				}
				catch (InterruptedException e)
				{	break;
				}
			}
		}
		// set thread pool size to 0
		mThreadPool.stop();
		// interrupt any threads
		mTable.interrupt();
		// wait for thread pool to complete
		try
		{	mThreadPool.join(1000);
		} catch (InterruptedException e)
		{ /* ignore */ }
	}
	
	public void releaseBreakpointedState(RequestState aState)
	{	switch (aState.getState())
		{	case RequestState.STATE_BREAKPOINT_BEFORE:
				aState.setState(RequestState.STATE_TEST_CACHE);
				mTable.put(aState);
				break;
			case RequestState.STATE_BREAKPOINT_AFTER:
				aState.setState(RequestState.STATE_RETURN_RESULT);
				mTable.put(aState);
				break;
			default:
				// request may have been killed or freed some other way
		}
	}
	
	protected void processRequest()
	{	RequestState state;
		try
		{	state = mTable.take();
		} catch (InterruptedException e)
		{	state = null;
		}
		if (state!=null)
		{	processRequest(state,true);
		}
	}

	protected void processRequest(RequestState aState,boolean aNewThread)
	{	Thread t = Thread.currentThread();
		synchronized(mRequestThreads)
		{	mRequestThreads.put(aState,t);
			if (aNewThread)
			{	mActiveThreadCount++;
			}
		}
		
		try
		{
			mSessionToLastRequestMap.put(aState.getOriginalRequest().getSession(), new Long(System.currentTimeMillis()));
			boolean workToDo=true;
			while(workToDo)
			{	//System.out.println(RequestState.typeToString(aState.getState())+" "+aState.getOriginalRequest().getURI().toString());
				try
				{	switch (aState.getState())
					{	case RequestState.STATE_MAP_REQUEST:
							stateMapRequest(aState);
							break;
						case RequestState.STATE_TEST_CACHE:
							stateTestCache(aState);
							break;		
						case RequestState.STATE_PENDING_ACCESSOR:
							stateReturnAccessor(aState);
							break;
						case RequestState.STATE_REQUEST_REPRESENTATION:
							workToDo=stateRequestRepresentation(aState);
							break;
						case RequestState.STATE_RELEASED_ACCESSOR:
							stateReleasedAccessor(aState);
							break;
						case RequestState.STATE_FRAGMENTATION:
							stateFragmentation(aState);
							break;
						case RequestState.STATE_TRANSREPRESENTATION:
							stateTransrepresent(aState);
							break;
						case RequestState.STATE_RESULT_READY:
							stateResultReady(aState);
							break;
						case RequestState.STATE_RETURN_RESULT:
							stateReturnResult(aState);
							break;
						case RequestState.STATE_PENDING_OTHERS_RESULT:
						case RequestState.STATE_BUSY_ACCESSOR:
						case RequestState.STATE_PENDING_REPRESENTATION:
						case RequestState.STATE_COMPLETE:
						case RequestState.STATE_BREAKPOINT_BEFORE:
						case RequestState.STATE_BREAKPOINT_AFTER:
							workToDo=false;
							break;
						default:
							aState.setException(new NetKernelException("undefined state in processRequest()"));
							break;
					}
				}
				catch (Throwable th)
				{	if (aState.getState()!=RequestState.STATE_COMPLETE)
					{	// error in this request
						URRequest req = aState.getMappedRequest();
						if (req!=null)
						{	mPendingResults.remove(req);
						}
						aState.setException(th);
						workToDo=true;
						//mTable.put(aState);
					}
					else
					{	// error in returning result to parent
						URRequest parentRequest = aState.getOriginalRequest().getParent();
						if (parentRequest!=null)
						{	RequestState parentState = mPendingResults.remove(parentRequest);
							if (parentState!=null)
							{	parentState.setException(th);
								mTable.put(parentState);
							}
							workToDo=false;
						}
					}
				}
			}
		}
		finally
		{	synchronized(mRequestThreads)
			{	mRequestThreads.remove(aState);
				if (aNewThread)
				{	mActiveThreadCount--;
				}
			}
		}
	}
	
	public void receiveAsyncResult(URResult aResult)
	{	RequestState state= mPendingResults.remove(aResult.getRequest());
		if (state!=null)
		{	state.pauseTimer();
			IURRepresentation resource = aResult.getResource();
			if (resource!=null)
			{	stateReceiveRepresentation(state,resource);
			}
			else
			{	state.setException(new NetKernelException("Null result returned"));
			}
			processRequest(state,false);
		}
		else
		{	SysLogger.log(SysLogger.WARNING,this,"received unexpected result for "+aResult.getRequest().toString());	
		}
	}
	
	public void receiveAsyncException(URResult aResult)
	{	RequestState state=mPendingResults.remove(aResult.getRequest());
		if (state!=null)
		{	state.pauseTimer();
			IAspectNetKernelException nkep = (IAspectNetKernelException)aResult.getResource().getAspect(IAspectNetKernelException.class);
			if (nkep.getError()!=null)
			{	state.setException(nkep.getError());
			}
			else
			{	state.setException(nkep.getException());
			}
			processRequest(state,false);
		}
		else
		{	SysLogger.log(SysLogger.WARNING,this,"received unexpected result for "+aResult.getRequest().toString());	
		}
	}
	
	private void stateMapRequest(RequestState aState) throws Throwable
	{	URRequest originalRequest = aState.getOriginalRequest();
		if (aState.getFragment()!=null)
		{	originalRequest=originalRequest.rewrite(originalRequest.getURI().withoutFragment());
		}
		MappedRequest mappedRequest = mModuleManager.getAccessorForRequest(originalRequest,false);
		String accessorClass = mappedRequest.getAccessorClass();
		if (accessorClass==null)
		{	MappedRequest mappedRequest2 = mModuleManager.getAccessorForRequest(originalRequest,true);
			ModuleDefinition md = (ModuleDefinition)originalRequest.getContext();
			NoAccessorFoundException e = new NoAccessorFoundException(originalRequest.getURI(),md.getURI(),mappedRequest2.getDebug());
			throw e;
		}
		aState.setMappedRequest(mappedRequest.getMappedRequest());
		aState.setAccessorClass(accessorClass);

		if (!mDebugger.catchBreakpoint(aState))
		{	aState.setState(RequestState.STATE_TEST_CACHE);
		}
	}
	
	private void stateTestCache(RequestState aState)
	{	IURRepresentation representation=null;
		try
		{	URRequest mapped=aState.getMappedRequest();
			if (aState.getFragment()!=null)
			{	URRequest mappedWithFrag = mapped.rewrite(mapped.getURI().withFragment(aState.getFragment()));
				representation = mCache.get(mappedWithFrag);
				if (representation==Cache.EXPIRED_RESOURCE)
				{	representation=null;
				}
				else if (representation!=null)
				{	aState.setState(RequestState.STATE_TRANSREPRESENTATION);
					aState.setUncastResult(representation);
				}
			}
			if (representation==null)
			{	representation = mCache.get(mapped);
				if (representation==Cache.EXPIRED_RESOURCE)
				{	if (!mPendingResults.hasEquivalentInProgress(aState))
					{	representation=null;
					}
				}	
				else if (representation!=null)
				{	aState.setState(RequestState.STATE_FRAGMENTATION);
					aState.setUncastResult(representation);
				}
			}
		}
		catch (NetKernelException e)
		{	SysLogger.log(SysLogger.WARNING, this, e.toString());
		}
		if (representation==null)
		{	aState.setState(RequestState.STATE_PENDING_ACCESSOR);
		}
	}
	
	private void stateReturnAccessor(RequestState aState) throws Throwable
	{	String accessorClass=aState.getAccessorClass();
		ModuleDefinition module = (ModuleDefinition)aState.getMappedRequest().getContext();
		IURAccessor accessor = module.getAccessor(accessorClass,mContainer);
		aState.setAccessor(accessor);
		aState.setState(RequestState.STATE_REQUEST_REPRESENTATION);
	}
	
	private boolean stateRequestRepresentation(RequestState aState) throws Throwable
	{	boolean result=false;
		IURAccessor accessor = aState.getAccessor();
		if (accessor.getAccessorMeta().supportsRequestType(aState.getOriginalRequest().getType()))
		{
			boolean accessorAvailable = true;
			boolean isThreadSafe = accessor.getAccessorMeta().isThreadSafe();
			if (!isThreadSafe)
			{	aState.setState(RequestState.STATE_BUSY_ACCESSOR);
				accessorAvailable = mBusyAccessors.addBlockedRequest(aState);
			}
			if (accessorAvailable)
			{
				URRequest mappedRequest = aState.getMappedRequest();
				Thread.currentThread().setContextClassLoader(((ModuleDefinition)mappedRequest.getContext()).getClassLoader());
				aState.setState(RequestState.STATE_PENDING_REPRESENTATION);
				mPendingResults.put(mappedRequest, aState);
				aState.resumeTimer();
				try
				{	accessor.requestAsync(mappedRequest);
				} finally
				{	synchronized(mRequestThreads)
					{	mRequestThreads.put(aState,Thread.currentThread());
					}
				}
			}
		}
		else
		{	String message = URRequest.typeToString(aState.getOriginalRequest().getType())+" unsupported on "+aState.getAccessorClass();
			throw new NetKernelException("Unsupported Request Type",message,aState.getMappedRequest().toString());
		}
		return  result;
	}
	
	private void stateReleasedAccessor(RequestState aState)
	{	URRequest mappedRequest = aState.getMappedRequest();
		Thread.currentThread().setContextClassLoader(((ModuleDefinition)mappedRequest.getContext()).getClassLoader());
		aState.setState(RequestState.STATE_PENDING_REPRESENTATION);
		mPendingResults.put(mappedRequest, aState);
		aState.getAccessor().requestAsync(mappedRequest);
	}

	private void stateReceiveRepresentation(RequestState aState, IURRepresentation aResult)
	{	releaseAccessorLock(aState);
		aState.setUncastResult(aResult);
		aState.setState(RequestState.STATE_FRAGMENTATION);
		aState.setResultNeedsCaching();
	}
	
	private void stateFragmentation(RequestState aState) throws Throwable
	{	int type = aState.getOriginalRequest().getType();
		if (aState.getFragment()==null || (type!=URRequest.RQT_SOURCE && type!=URRequest.RQT_TRANSREPRESENT && type!=URRequest.RQT_FRAGMENT))
		{	aState.setState(RequestState.STATE_TRANSREPRESENTATION);
		}
		else
		{	// build request
			URRequest parent=aState.getOriginalRequest();
			URRequest request = new URRequest(parent.getURI(), null, parent.getSession(), null, URRequest.RQT_FRAGMENT, parent.getCWU(), parent, parent.getAspectClass());
			List superStack = parent.getSuperStackClone();
			superStack.add(parent.getContext());
			IURRepresentation from = aState.getUncastResult();
			request.addArg(parent.getURI().withoutFragment(), from);
			request.setCurrentContext(parent.getContext(),superStack);
			
			// find fragmentor
			IFragmentor fragmentor = mModuleManager.getFragmentorFor(request);
			if (fragmentor==null)
			{	String fragment = aState.getOriginalRequest().getURI().getFragment();
				String mime = from.getMeta().getMimeType();
				throw new NetKernelException("Unrecognised Fragment Identifier","No fragmentor found for a fragment identifier of [#"+fragment+"] on resource of type ["+mime+"]",null);
			}
			else
			{	if (aState.resultNeedsCaching())
				{	// store unfragmented result in case we need the whole later
					try
					{	URResult result = new URResult(aState.getMappedRequest(), aState.getUncastResult());
						mCache.put(result);
					}
					catch (NetKernelException e)
					{	SysLogger.log(SysLogger.WARNING, this, e.toString());
					}				
				}
				
				// now fragment
				request.setCurrentContext(fragmentor.getModule(),superStack);
				Thread.currentThread().setContextClassLoader(((ModuleDefinition)request.getContext()).getClassLoader());
				RequestState state = new RequestState(request,aState);
				mPendingResults.put(request,state);
				boolean error=true;
				try
				{	state.resumeTimer();
					IURRepresentation result = fragmentor.fragment(request);
					aState.setUncastResult(result);
					aState.setResultNeedsCaching();
					error=false;
					//there are extreme cases where this is not bad so don't log
					//if (result.getMeta().isIntermediate()!=aState.getUncastResult().getMeta().isIntermediate())
					//{	SysLogger.log(SysLogger.WARNING,this,"Fragmentor "+fragmentor.getClass().getName()+" not preserving isIntermediate()");
					//}
				}
				finally
				{	state.pauseTimer();
					mStats.addStatisticsFor(fragmentor, state.getCummulativeTime(), state.getRequestTime(),error);
					mPendingResults.remove(request);
				}
				aState.setState(RequestState.STATE_TRANSREPRESENTATION);
			}
		}
	}
	
	private void stateTransrepresent(RequestState aState) throws Throwable
	{	if (!aState.getUncastResult().hasAspect(aState.getOriginalRequest().getAspectClass()))
		{	if (!transrepresent(aState))
			{	if (aState.getOriginalRequest().getType()==URRequest.RQT_SOURCE && !aState.resultNeedsCaching()) // because it came from the cache
				{	// try to recreate from scratch
					aState.setState(RequestState.STATE_PENDING_ACCESSOR);
				}
				else
				{	StringBuffer fromString = new StringBuffer(128);
					IURRepresentation from = aState.getUncastResult();
					Class to = aState.getOriginalRequest().getAspectClass();
					Collection fromInterfaces = from.getAspects();
					for (Iterator  i=fromInterfaces.iterator(); i.hasNext(); )
					{	Object aspect = i.next();
						fromString.append(aspect.getClass().getName());
						if (i.hasNext()) fromString.append(", ");
					}
					throw new NetKernelException("No Transreptor Found","No suitable transreptor found for conversion from ["+fromString+"] to ["+to.getName()+"]",null);
				}
			}
		}
		else
		{	aState.setResult(aState.getUncastResult());
			aState.setState(RequestState.STATE_RESULT_READY);
		}
	}
	
	private boolean transrepresent(RequestState aState) throws Throwable
	{	IURRepresentation from = aState.getUncastResult();
		Class to = aState.getOriginalRequest().getAspectClass();
		ITransrepresentor transreptor = mModuleManager.getTransrepresentorFor(from,to, aState.getOriginalRequest());
		if (transreptor!=null)
		{	URRequest parent=aState.getMappedRequest();
			URRequest request = new URRequest(parent.getURI(), null, parent.getSession(), null, URRequest.RQT_TRANSREPRESENT, parent.getCWU(), parent, parent.getAspectClass());
			List superStack = parent.getSuperStackClone();
			superStack.add(parent.getContext());
			request.setCurrentContext(transreptor.getModule(),superStack);
			request.addArg(URRequest.URI_SYSTEM, from);
			Thread.currentThread().setContextClassLoader(((ModuleDefinition)request.getContext()).getClassLoader());
			RequestState state = new RequestState(request,aState);
			mPendingResults.put(request,state);
			try
			{	state.resumeTimer();
				IURRepresentation result = transreptor.transrepresent(from,request);
				aState.setResult(result);
				aState.setResultNeedsCaching();
				//there are extreme cases where this is not bad so don't log
				//if (result.getMeta().isIntermediate()!=aState.getUncastResult().getMeta().isIntermediate())
				//{	SysLogger.log(SysLogger.WARNING,this,"Transreptor "+transreptor.getClass().getName()+" not preserving isIntermediate()");
				//}
			}
			finally			{	state.pauseTimer();
				mStats.addStatisticsFor(transreptor, state.getCummulativeTime(), state.getRequestTime(), aState.getResult()==null);
				mPendingResults.remove(request);
			}
			aState.setState(RequestState.STATE_RESULT_READY);

		}
		return (transreptor!=null);
	}
	
	private void stateResultReady(RequestState aState)
	{	if (!mDebugger.catchBreakpoint(aState))
		{	aState.setState(RequestState.STATE_RETURN_RESULT);
		}
	}
	
	private void stateReturnResult(RequestState aState) throws Throwable
	{	boolean wasException = aState.wasException();
		mStats.addStatisticsFor(aState, wasException);
		URRequest originalRequest = aState.getOriginalRequest();

		//store final result in cache
		if (aState.resultNeedsCaching())
		{	try
			{	URRequest request = aState.getMappedRequest();
				if (aState.getFragment()!=null)
				{	request = request.rewrite(request.getURI().withFragment(aState.getFragment()));
				}
				URResult result = new URResult(request, aState.getResult());
				mCache.put(result);
			}
			catch (NetKernelException e)
			{	SysLogger.log(SysLogger.WARNING, this, e.toString());
			}
			mPendingResults.notifyOfAvailableResult(aState,mTable);
		}
		
		URRequest parent =	originalRequest.getParent();
		if (parent!=null)
		{	RequestState state = (RequestState)mPendingResults.get(parent);
			if (state!=null)
			{	state.resumeTimer();
			}
		}

		aState.setState(RequestState.STATE_COMPLETE);
		
		IURRequestor requestor = originalRequest.getRequestor();
		URResult result = new URResult(aState.getOriginalRequest(),aState.getResult());
		Thread.currentThread().setContextClassLoader(((ModuleDefinition)originalRequest.getContext()).getClassLoader());
		if (wasException)
		{	releaseAccessorLock(aState);
			requestor.receiveAsyncException(result);
		}
		else
		{	requestor.receiveAsyncResult(result);
		}
	}
	
	private void releaseAccessorLock(RequestState aState)
	{	IURAccessor accessor = aState.getAccessor();
		if (accessor!=null)
		{	RequestState released = mBusyAccessors.releaseAccessor(aState.getAccessorClass());
			if (released!=null)
			{	if (released.getState()==RequestState.STATE_BUSY_ACCESSOR)
				{	released.setState(RequestState.STATE_RELEASED_ACCESSOR);
					mTable.put(released);
				}
				else
				{	SysLogger.log(SysLogger.SEVERE,this,"scheduler unstable- accessor blocked state wrong");
				}
			}
		}
	}
	
	public void write(OutputStream aStream) throws IOException
	{	OutputStreamWriter osw = new OutputStreamWriter(aStream);
		osw.write("<scheduler>");
		mStats.write(osw);
		writeSessions(osw);
		osw.write("</scheduler>");
		osw.flush();
	}
	
	private void writeSessions(Writer aWriter) throws IOException
	{	aWriter.write("<sessions>");
		long now = System.currentTimeMillis();
		synchronized(mSessionToLastRequestMap)
		{	for (Iterator i=mSessionToLastRequestMap.entrySet().iterator(); i.hasNext(); )
			{	Map.Entry entry = (Map.Entry)i.next();
				long time = ((Long)entry.getValue()).longValue();
				IRequestorSession session = (IRequestorSession)entry.getKey();
				URRequest root = mPendingResults.getRootRequestForSession(session);
				RequestState newest=mPendingResults.getNewestStateForSession(session);
				String uriString;
				long start;
				if (root!=null)
				{	uriString = root.getURI().toString();
					start = root.getTime();
					
					aWriter.write("<session>");
					XMLUtils.write(aWriter, "id", Long.toString(session.getId()));
					XMLUtils.writeEscaped(aWriter, "uri", uriString);
					XMLUtils.writeEscaped(aWriter, "newest", newest.getMappedRequest().getURI().toString());
					XMLUtils.write(aWriter, "age", Long.toString(now-start));
					XMLUtils.write(aWriter, "active", Long.toString(now-time));
					aWriter.write("</session>");
				}
			}
		}
		aWriter.write("</sessions>");
		
	}
	
	
	/* @return 0=failed to find, 1=attempted but not successful, 2=success
	 */
	public int killSession(long aSession, String aId) throws InterruptedException
	{	TransportInitiatedSession session = new TransportInitiatedSession(aSession);
		int result=0;
		// try to locate and kill the thread it is running on
		// loop several times if necessary to try and catch a thread in action
		// finally force an orphaned response if no thread is found executing request
		for (int i=0; i<10 && result==0; i++)
		{	if (mPendingResults.kill(session, mRequestThreads, mTable, (i==0),aId))
			{	result = 1;
			}
			else
			{	Thread.currentThread().sleep(20);
			}
		}
		
		
		// see if it has been killed
		if (result==1)
		{	for (int i=0; i<10 && result==1; i++)
			{	Thread.currentThread().sleep(200);
				RequestState state = mPendingResults.getNewestStateForSession(session);
				if (state==null)
				{	result=2;
				}
			}
		}
		return result;
	}
	
	private int mHouseKeepingFreqDivider;
	private int mDeadlockLevel;
	private RequestState mDeadlockedState;
	
	
	private static final int DLL_OK=0;
	private static final int DLL_INC_THREADS=1;
	private static final int DLL_DIRECT=2;
	private static final int DLL_KILL=3;
	private static final int DLL_STOP=4;
	
	private RequestStatistics mStats;

	public void doPeriodicHouseKeeping()
	{	
		mHouseKeepingFreqDivider = (mHouseKeepingFreqDivider+1)%4;
		if (mHouseKeepingFreqDivider!=0) return;
		
		long oldestTime = Long.MAX_VALUE;
		RequestState oldestState = null;
		//IRequestorSession oldestSession=null;
		synchronized(mSessionToLastRequestMap)
		{	for (Iterator i=mSessionToLastRequestMap.entrySet().iterator(); i.hasNext(); )
			{	Map.Entry entry = (Map.Entry)i.next();
				long time = ((Long)entry.getValue()).longValue();
				if (time<oldestTime)
				{	RequestState state = mPendingResults.getNewestStateForSession((IRequestorSession)entry.getKey());
					if (state!=null)
					{	oldestTime = time;
						oldestState=state;
					}
				}
			}
		}
		
		long age = System.currentTimeMillis()-oldestTime;
		if (age<mDeadlockPeriod)
		{	oldestState=null;
		}
		
		if (mDeadlockedState!=oldestState && mDeadlockedState!=null)
		{	if (mDeadlockLevel ==DLL_INC_THREADS)
			{	SysLogger.log(SysLogger.WARNING,this, "Deadlock resolved by increasing thread count, consider increasing kernel threads");
			}
			mDeadlockedState=null;
			mDeadlockLevel=DLL_OK;
		}
		
		if (oldestState!=null)
		{	mDeadlockedState=oldestState;
			kill(mDeadlockLevel, mDeadlockedState);
		}
	}
	
	private void kill(int aLevel, RequestState aState)
	{
		long age = System.currentTimeMillis()-aState.getOriginalRequest().getTime();
		String message = aState.getMappedRequest().getURI()+" (Lifeless for "+age+"ms)";
		if (aLevel==DLL_OK)
		{	int freeThreadCount = mThreadPool.getSetThreadCount() - mActiveThreadCount;
			if (freeThreadCount!=0)
			{	aLevel=DLL_DIRECT;
			}
		}
		switch (aLevel)
		{	case DLL_OK:
			{	int threadCount = mThreadPool.getSetThreadCount()*2;
				mThreadPool.setCount(threadCount);
				mDeadlockLevel=DLL_INC_THREADS;
				SysLogger.log(SysLogger.WARNING,this, "deadlock detected; temporarily increasing thread count to "+threadCount+" for "+message);
				break;
			}
			case DLL_INC_THREADS:
			{	// restore thread count
				SysLogger.log(SysLogger.SEVERE,this, "killing deadlocked request "+message);
				int threadCount = mThreadPool.getSetThreadCount()/2;
				mThreadPool.setCount(threadCount);
				// no break here, continue on to DLL_DIRECT
			}
			case DLL_DIRECT:
			{	//kill
				IRequestorSession session = aState.getOriginalRequest().getSession();
				try
				{	killSession(session.getId(),"Deadlock detected");
				} catch(InterruptedException e)
				{
				}
				mDeadlockLevel=DLL_KILL;
				break;
			}
			case DLL_KILL:
				mDeadlockLevel=DLL_STOP;
				break;
		}
	}
}