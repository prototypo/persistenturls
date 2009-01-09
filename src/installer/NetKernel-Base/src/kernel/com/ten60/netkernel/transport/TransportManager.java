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
 * File:          $RCSfile: TransportManager.java,v $
 * Version:       $Name:  $ $Revision: 1.16 $
 * Last Modified: $Date: 2005/10/14 21:24:05 $
 *****************************************************************************/
package com.ten60.netkernel.transport;

import com.ten60.netkernel.urii.*;
import com.ten60.netkernel.container.*;
import com.ten60.netkernel.util.*;
import com.ten60.netkernel.module.*;
import com.ten60.netkernel.urrequest.*;
import com.ten60.netkernel.scheduler.*;
import com.ten60.netkernel.urii.aspect.*;

import java.util.*;
import java.text.*;
import java.io.*;
/**
 *	Transport Manager system component manages the startup and shutdown of all registered
 * transports. It passes requests from transports through the throttle and into the scheduler
 * @author  tab
 */
public class TransportManager extends ComponentImpl implements IURRequestor
{
	/** our URI */
	public static final URIdentifier URI = new URIdentifier("netkernel:tm");
	/** identifier for internal transport that Contain implements and exhibits as an API for
	 * JMX and Embedded request making
	 */
	public static final String INTERNAL_TRANSPORT="InternalTransport";
	/** list of started transports */
	private List mTransports = new ArrayList();
	/** the scheduler */
	private Scheduler mUS;
	/** holding table for returned results from the scheduler */
	private Map mResultTable = Collections.synchronizedMap(new IdentityHashMap());
	/** our throttle */
	private Throttle mThrottle = new Throttle();
	
	/**  All requests must pass though this control point to see if they need to be blocked */
	private RequestBlocker mBlocker = new RequestBlocker();
	
	
	/** true until the transport manager is told to stop accepting requests during the shutdown process */
	private boolean mAcceptingRequests=true;
	
	private Container mContainer;
	
	private ITransport mInternalTransport;
	
	/** service overload exception */
	public static final String EX_SERVICE_UNAVAILABLE="Service Unavailable";
	
	/** number of memory stats kept */
	private int mStatBufferSize;
	/** number of housework periods per memory stat period */
	private int mStatFreqDivider;
	/** format of timestamp applied to memory stats */
	private DateFormat mDateFormat;
	/** counter for implementing frequency divider */
	private int mFreqDivider;
	/** index into rolling statistics buffer **/
	private int mBufferIndex;
	/** period in seconds of each sample- used for normalizing work stats **/
	private int mWorkPeriod;
	/** stats timestamp */
	 private long[] mTimeStamps;
	 /** throttle stats */
	 private float[] mThrottleStats;
	 
	private int mQueueSize;
	private int mRejectedRequests;
	private int mConcurrentReq;
	private long mTotalRequests;
	
	/** Creates a new instance of TransportManager */
	public TransportManager()
	{	super(URI);
		mInternalTransport = new ITransport()
		{
			public void initialise(Container aContainer, ModuleDefinition aContext)
			{
			}
			public void destroy()
			{
			}
			public void setContext(IRequestorContext aContext)
			{
			}
			public String getDescription()
			{	return "Internal Transport";
			}
		};
	}
	
	public ITransport getInternalTransport()
	{	return mInternalTransport;
	}
	
	/** Start the transport manager. Creates and starts all the transports
	 */
	public void start(Container aContainer) throws NetKernelException
	{	
		mContainer = aContainer;
		mUS = (Scheduler)aContainer.getComponent(Scheduler.URI);
		Config config = (Config)aContainer.getComponent(Config.URI);
		int throttle = config.getReadable().getInt("system/throttle", 5);
		mThrottle.setMaxCount(throttle);
		int throttleQueue = config.getReadable().getInt("system/throttleQueue", 10);
		mThrottle.setMaxQueue(throttleQueue);
		
		ModuleManager mm = (ModuleManager)mContainer.getComponent(ModuleManager.URI);
		PairList transports = mm.getTransports();
		holdRequests();
		refresh(transports);
	}
	
	/** Loads current transport configuration from module manager and starts/stops what has
	 * changed
	 */
	public void refresh(PairList aTransports)
	{	Config config = (Config)mContainer.getComponent(Config.URI);
		XMLReadable cr = config.getReadable();
		mStatBufferSize =cr.getInt("system/statistics/historySize", 60);
		mStatFreqDivider =cr.getInt("system/statistics/frequencyDivisor", 10);
		int houseKeepingPeriod = cr.getInt("system/houseKeepingPeriod", 500);
		mDateFormat = new SimpleDateFormat(cr.getText("system/statistics/timestampFormat").trim());
		mTimeStamps	=new long[mStatBufferSize*2];
		mThrottleStats = new float[mStatBufferSize*3];
		mWorkPeriod = houseKeepingPeriod*mStatFreqDivider/1000;
		if (mWorkPeriod==0) mWorkPeriod=1;
		
		NetKernelException exception=null;
		
		// shutdown as necessary
		mBlocker.block();
		List stoppedTransports = new ArrayList();
		for (Iterator i = mTransports.iterator(); i.hasNext(); )
		{	TransportDeploymentRecord tdr = (TransportDeploymentRecord)i.next();
			String transportClassString = tdr.getTransport().getClass().getName();
			ModuleDefinition md = tdr.getModule();
			if (!aTransports.contains(transportClassString, md))
			{	ITransport transport = tdr.getTransport();
				if (!transport.getDescription().equals("Internal Transport"))
				{	stoppedTransports.add(transport);
					mBlocker.interrupt(transport);
				}
			}
		}
		mBlocker.releaseInterrupted();
		for (Iterator i = stoppedTransports.iterator(); i.hasNext(); )
		{	ITransport transport = (ITransport)i.next();
			try
			{	transport.destroy();
				SysLogger.log1(SysLogger.CONTAINER,this, "  Uninstalled transport [%1]", transport.getDescription());
			}
			catch (Throwable e)
			{	if (exception==null)
				{	exception = new NetKernelException("transport refresh had problems");
				}
				exception.addCause(e);
			}
		}
		// start up as necessary
		List newTransports = new ArrayList();
		for (int i=0; i<aTransports.size(); i++)
		{	String transportClassString = (String)aTransports.getValue1(i);
			ModuleDefinition md = (ModuleDefinition)aTransports.getValue2(i);
			if (transportClassString.equals(INTERNAL_TRANSPORT))
			{	TransportDeploymentRecord tdr = new TransportDeploymentRecord(mInternalTransport, md, mStatBufferSize);
				newTransports.add(tdr);
				continue; // don't need to load this
			}
			boolean started=false;
			for (Iterator j = mTransports.iterator(); j.hasNext(); )
			{	TransportDeploymentRecord tdr = (TransportDeploymentRecord)j.next();
				if (tdr.getModule().equals(md) && tdr.getTransportClass().equals(transportClassString))
				{	// keep this transport alive and hand over to new module
					tdr = new TransportDeploymentRecord(tdr.getTransport(), md, mStatBufferSize);
					tdr.getTransport().setContext(md);
					newTransports.add(tdr);
					started=true;
					break;
				}
			}
			if (!started)
			{	try
				{	ClassLoader cl=md.getClassLoader();
					Thread.currentThread().setContextClassLoader(cl);
					ITransport transport = createTransport(transportClassString,cl);
					transport.initialise(mContainer,md);
					TransportDeploymentRecord tdr = new TransportDeploymentRecord(transport, md, mStatBufferSize);
					newTransports.add(tdr);
					SysLogger.log1(SysLogger.CONTAINER,this, "  Installed transport [%1]", transport.getDescription());
				}
				catch (Throwable e)
				{	if (exception==null)
					{	exception = new NetKernelException("transport refresh had problems");
					}
					exception.addCause(e);
				}
			}
		}
		mTransports = newTransports;
		if (exception!=null)
		{	SysLogger.log(SysLogger.SEVERE, this, exception.toString());
		}	
	}

	/** All new requests will be held until a start()
	 */
	public void holdRequests()
	{	mBlocker.block();
	}
	/** All new requests will be rejected
	 */
	public void rejectRequests()
	{	mAcceptingRequests=false;
	}
	
	public void acceptRequests()
	{	mAcceptingRequests=true;
		mBlocker.release();
		mBlocker.clear();
	}
	
	
	/** Stops all the transports
	 */
	public void stop() throws NetKernelException
	{	rejectRequests();
		refresh(new PairList(1));
		mContainer=null;
		mUS=null;
	}
	
	/** internal method to create a transport form the given class name from the given classloader
	 * @param aTransportClassString The name of the transport class
	 * @param aClassLoader The classloader to find the class with
	 * @exception  NetKernelException throws if we fail to create the transport for any reason
	 */
	private ITransport createTransport(String aTransportClassString, ClassLoader aClassLoader) throws NetKernelException
	{	
		try
		{	Class c = aClassLoader.loadClass(aTransportClassString);
			ITransport result = (ITransport)c.newInstance();
			return result;
		} 
		catch (Throwable e)
		{	NetKernelException e2 = new NetKernelException("Failed to create transport class",null,aTransportClassString);
			e2.addCause(e);
			throw e2;
		}
	}
	
	/** Called from a Transport this method processes a synchronous request
	 * @param aRequest the request to execute
	 * @param aTransportId the transport making this request
	 * @return the result, exception or not
	 */
	public IURRepresentation handleRequest(URRequest aRequest, ITransport aTransport)
	{	try
		{	doBlockerAndThrottle(aTransport);
			
			aRequest.setSession(new TransportInitiatedSession());
			IURRepresentation result;
			try
			{	result = innerHandleRequest(aRequest,aTransport);
			} finally
			{	mThrottle.notifyOfReturn();
			}
			cleanup(result,aTransport);
			return result;
		}
		catch (InterruptedException e)
		{	NetKernelException e2 = new NetKernelException(EX_SERVICE_UNAVAILABLE,"Request Interrupted",null);
			return NetKernelExceptionAspect.create(e2);
		} catch (ThrottleOverloadException e)
		{	synchronized(this)
			{	mRejectedRequests++;
			}
			SysLogger.log1(SysLogger.WARNING, this, "Request %1 rejected due to throttle overload", aRequest.getURI().toString());
			NetKernelException e2 = new NetKernelException(EX_SERVICE_UNAVAILABLE,"Max Concurrent Requests Exceeded",null);
			return NetKernelExceptionAspect.create(e2);
		}
	}
	
	/** Separate method so that Container can call init process without worry of being blocked whilst
	 * all else is kept at bay
	 */
	public IURRepresentation innerHandleRequest(URRequest aRequest, ITransport aTransport)
		throws InterruptedException
	{		aRequest.setRequestor(this);
			mUS.requestAsync(aRequest);
			URResult result;
			synchronized(aRequest)
			{   do
				{   result = (URResult)mResultTable.remove(aRequest);
					if (result==null)
					{   aRequest.wait();
					}
				} while (result==null);
			}
			return result.getResource();
	}

	
	/** Called from a Transport this method processes an asynchronous request
	 * @param aRequest the request to execute
	 * @param aTransportId the transport making this request
	 * @return an exception caused by being interrupted or overload otherwise null
	 */
	public NetKernelException handleAsyncRequest(URRequest aRequest, ITransport aTransport)
	{	NetKernelException failureResult=null;
		try
		{	doBlockerAndThrottle(aTransport);
			aRequest.setSession(new TransportInitiatedSession());
			IURRequestor intermediary = new RequestorIntermediary(aRequest.getRequestor(),aTransport);
			aRequest.setRequestor(intermediary);
			mUS.requestAsync(aRequest);
		}
		catch (InterruptedException e)
		{	failureResult = new NetKernelException(EX_SERVICE_UNAVAILABLE,"Request Interrupted",null);
		} catch (ThrottleOverloadException e)
		{	synchronized(this)
			{	mRejectedRequests++;
			}
			SysLogger.log1(SysLogger.WARNING, this, "Request %1 rejected due to throttle overload", aRequest.getURI().toString());
			failureResult = new NetKernelException(EX_SERVICE_UNAVAILABLE,"Max Concurrent Requests Exceeded",null);
		}
		return failureResult;
	}
	
	/** Holds up thread if external requests are blocked or queued by throttle
	 * @exception InterruptedException thrown if request is rejected because transport is in process of shutting down
	 * @exception ThrottleOverloadException thrown if throttle is overloaded
	 */
	private void doBlockerAndThrottle(ITransport aTransport) throws InterruptedException, ThrottleOverloadException
	{	//hold requests if necessary
			mBlocker.check(aTransport);
			// reject requests if necessary
			if (!mAcceptingRequests)
			{	throw new InterruptedException();
			}
			// throttle requests
			mThrottle.throttle();
	}
	
	/** Common code to execute when a result has been returned
	 */
	private void cleanup(IURRepresentation aResult, ITransport aTransport)
	{	
		//capture work stats for transport
		for (int i=mTransports.size()-1; i>=0; i--)
		{	TransportDeploymentRecord tdr = (TransportDeploymentRecord)mTransports.get(i);
			if (tdr.getTransport()==aTransport)
			{	IURMeta meta = aResult.getMeta();
				tdr.accumulateWork(meta.getCreationCost()+meta.getUsageCost(),mBufferIndex);
				break;
			}
		}
		
		//capture overall request stats
		synchronized(this)
		{	mTotalRequests++;
		}
		
	}
		
	/** Intermediary for asynchronous request results, this ensures that any
	 *loose ends are cleaned up whilst still returning result to the
	 *original requestor
	 */
	private class RequestorIntermediary implements IURRequestor
	{	private IURRequestor mRequestor;
		private ITransport mTransport;
		
		public RequestorIntermediary(IURRequestor aRequestor, ITransport aTransport)
		{	mRequestor=aRequestor;
			mTransport=aTransport;
		}
		
		public void receiveAsyncException(URResult aResult)
		{	mThrottle.notifyOfReturn();
			cleanup(aResult.getResource(),mTransport);
			mRequestor.receiveAsyncException(aResult);
		}
		
		public void receiveAsyncResult(URResult aResult)
		{	mThrottle.notifyOfReturn();
			cleanup(aResult.getResource(),mTransport);
			mRequestor.receiveAsyncResult(aResult);
		}
	}

	/** We get told when synchronous requests complete
	 */
	public void receiveAsyncResult(URResult aResult)
	{	returnResult(aResult);
	}
	/** We get told when synchronous requests complete
	 */
	public void receiveAsyncException(URResult aResult)
	{	returnResult(aResult);
	}
	private void returnResult(URResult aResult)
	{	URRequest request = aResult.getRequest();
		mResultTable.put(request,aResult);
		synchronized(request)
		{	request.notify();
		}
	}
	
	/** Wait for all unblocked requests to be completed
	 * <br/>It will poll every quarter of a second and requires
	 * a clear period of half a second before declaring all-clear
	 */
	public void join()
	{	int count=2;
		while(count>0)
		{	if (mThrottle.isBusy())
			{	count=2;
			}
			else
			{	count--;
			}
			try
			{	Thread.sleep(50);
			} catch (InterruptedException e) {	}
		}
	}
	
	public void doPeriodicHouseKeeping()
	{
		mQueueSize+=mThrottle.getQueueSize();
		mConcurrentReq+=mThrottle.getConcurrentCount();
		
		mFreqDivider = (mFreqDivider+1)%mStatFreqDivider;
		if (mFreqDivider==0)
		{	long now=System.currentTimeMillis();
			int p = mBufferIndex*2;
			mTimeStamps[p]=now;
			synchronized(this)
			{	mTimeStamps[p+1]=mTotalRequests;
			}
			// work statistics
			for (int i=mTransports.size()-1; i>=0; i--)
			{	TransportDeploymentRecord tdr = (TransportDeploymentRecord)mTransports.get(i);
				tdr.accumulateWork(0, mBufferIndex);
			}
			//throttle statistics
			float factor = 1/((float)mStatFreqDivider);
			float concurrent = ((float)mConcurrentReq)*factor;
			float queue = ((float)mQueueSize)*factor;
			float reject = ((float)mRejectedRequests)/((float)mWorkPeriod);
			mQueueSize=0;
			mConcurrentReq=0;
			mRejectedRequests=0;
			p = mBufferIndex*3;
			mThrottleStats[p]=concurrent;
			mThrottleStats[p+1]=queue;
			mThrottleStats[p+2]=reject;
			mBufferIndex = (mBufferIndex+1)%(mStatBufferSize);
			
		}
	}
	
	
	public void write(OutputStream aStream) throws IOException
	{
		OutputStreamWriter osw = new OutputStreamWriter(aStream);
		osw.write("<transports>");
		for (Iterator j = mTransports.iterator(); j.hasNext(); )
		{	TransportDeploymentRecord tdr = (TransportDeploymentRecord)j.next();
			osw.write("<transport>");
			write(osw,"module",tdr.getModule().getURI().toString());
			write(osw,"version",tdr.getModule().getVersion().toString(3));
			write(osw,"class",tdr.getTransportClass());
			write(osw,"desc",tdr.getTransport().getDescription());
			osw.write("<work>");
			long[] stats = tdr.getWork();
			int index = mBufferIndex;
			for (int i=0; i<mStatBufferSize; i++)
			{	index++;
				if (index>=mStatBufferSize) index=0;
				osw.write("<stat>");
				write(osw,"work", Long.toString(stats[index]/mWorkPeriod));
				osw.write("</stat>");
			}
			osw.write("</work>");
			osw.write("</transport>");
		}
		osw.write("<throttle>");
		int index = mBufferIndex;
		for (int i=0; i<mStatBufferSize; i++)
		{	
			osw.write("<stat>");
				int p=index*3;
				write(osw,"concurrency", Float.toString(mThrottleStats[p]));
				write(osw,"queue", Float.toString(mThrottleStats[p+1]));
				write(osw,"rejected", Float.toString(mThrottleStats[p+2]));
				p=index*2;
				write(osw,"total", Long.toString(mTimeStamps[p+1]));
				long time = mTimeStamps[p];
				if (time==0)
				{	write(osw,"time","-");
				}
				else
				{	write(osw,"time",mDateFormat.format(new java.util.Date(time)));
				}
			osw.write("</stat>");
			index+=1;
			if (index>=mStatBufferSize) index=0;
		}
		osw.write("</throttle>");
		osw.write("</transports>");
		osw.flush();
	}

	private static void write(Writer osw, String aName, String aValue) throws IOException
	{	XMLUtils.writeEscaped(osw,aName, aValue);
	}	
}