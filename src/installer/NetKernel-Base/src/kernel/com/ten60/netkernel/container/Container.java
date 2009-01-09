/******************************************************************************
 * (c) Copyright 2002,2003,2004,2005 1060 Research Ltd
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
 * File:          $RCSfile: Container.java,v $
 * Version:       $Name:  $ $Revision: 1.49 $
 * Last Modified: $Date: 2008/03/22 14:25:37 $
 *****************************************************************************/
package com.ten60.netkernel.container;

import com.ten60.netkernel.urii.*;
import com.ten60.netkernel.urii.aspect.*;
import com.ten60.netkernel.util.*;
import com.ten60.netkernel.urrequest.*;
import com.ten60.netkernel.transport.*;
import com.ten60.netkernel.module.*;
import com.ten60.netkernel.scheduler.Scheduler;

import java.util.*;
import java.io.*;
import java.net.*;
import java.net.URI;
/**
 * Main class for NetKernel. It is responsible for starting and stopping all other components
 * @author  tab
 */
public final class Container extends ComponentImpl
{
	/** our URI **/
	public static final URIdentifier URI = new URIdentifier("netkernel:container");
	/** default path for system configuration */
	public static final String DEFAULT_CONFIG = "etc/system.xml";
	/** version number*/
	public static final String VERSION="2.8.5";
	/** kernel URN*/
	public static final String NETKERNEL_URN="urn:com:ten60:netkernel";
	/** basepath that config path and all paths in config are offset from */
	private String mBasePath;
	/** the config path */
	private String mConfig;
	/** path of the scratch directory */
	private String mScratchPath;
	/** uri of the basepath*/
	private String mBasePathURI;
	/** uri of the scratchpath*/
	private String mScratchPathURI;
	/** true if the container is started */
	private boolean mIsStarted;
	/** a reference to the scheduler component */
	private Scheduler mScheduler;
	/** a request context that is used for all requests through the requestResource() method */
	private ModuleDefinition mExternalRequestContext;
	/** the startup sequence of components */
	private Class[] mBootList = new Class[]
	{	com.ten60.netkernel.container.Config.class,
		com.ten60.netkernel.cache.Cache.class,
		com.ten60.netkernel.module.ModuleManager.class,
		com.ten60.netkernel.scheduler.Scheduler.class,
		com.ten60.netkernel.transport.TransportManager.class,
		com.ten60.netkernel.container.HouseKeeper.class,
	};
	/** startup ordered map of URI to system component instances */
	private final Map mComponentInstances = new LinkedHashMap();
	/** the transport used by the command line invocation and init process */
	private ITransport mInternalTransport;
	/** flag to indicate that an outer bootloader should restart when this container terminates */
	private boolean mIsRestart;
	/** the time the container was started */
	private long mStartTime;
	/** hook to call if JVM is stopped whilst container is running */
	private ShutdownHook mShutdownThread;
	/** handle on original thread group */
	private ThreadGroup mOriginalThreadGroup;
	/** true if in operational state */
	private boolean mIsReady;
	
	/** main method to start the component with a given basepath and optional configuration
	 * as arguments 1 and 2. Container waits for a newline on System.in before shutting down
	 * the container.
	 */
	public static void main(String[] args)
	{	if (args.length>2)
		{	System.err.println("usage: Container [<basepath> [<config>] ]");
			System.exit(0);
		}
		String basepath = (args.length>0)?args[0]:null;
		String config = (args.length>1)?args[1]:null;
		try
		{	
			boolean shouldRestart;
			do
			{	Container c = new Container(basepath,config);
				c.start();
				shouldRestart=c.isRestart().booleanValue();
			}
			while (shouldRestart);
		} 
		catch (NetKernelException e)
		{	System.out.println(e.toString());
		}
		catch (Exception e)
		{	e.printStackTrace();
		}
	}
	
	/** Creates a new instance of Container
	 * @param aBasePath basepath (filename) that config path and all paths in config are offset from
	 * @param aConfig config path (relative or absolute url) where overriden config can be found- may be null and
	 * default will be used. (etc/system.xml)
	 */
	public Container(String aBasePath)
	{	this(aBasePath,null);
	}
	public Container(String aBasePath, String aConfig)
	{	super(new URIdentifier("netkernel:container"));
		//Add local mimetype map
		FileNameMap fnm = new ExtraMimeTypes(URLConnection.getFileNameMap());
		URLConnection.setFileNameMap(fnm);
		
		if (aBasePath!=null)
		{	mBasePath = Utils.fixSlash(aBasePath);
			if(!mBasePath.endsWith("/")) mBasePath+="/";
			mBasePathURI = File.separatorChar=='/' ? "file:"+mBasePath : "file:///"+mBasePath;
			mBasePathURI=mBasePathURI.replaceAll(" ", "%20");
			System.setProperty("netkernel.basepath", mBasePath);
		}
		else throw new IllegalArgumentException("basepath must be specified");
		
		String relativeConfigPath;
		if (aConfig!=null)
		{	relativeConfigPath = Utils.fixSlash(aConfig);
		}
		else
		{	relativeConfigPath = DEFAULT_CONFIG;
		}
		mConfig = java.net.URI.create(mBasePathURI).resolve(relativeConfigPath).toString();
	}
	
	/** returns true if the outer bootloader should restart rather than terminate when this
	 * container terminates */
	public Boolean isRestart()
	{	return Boolean.valueOf(mIsRestart);
	}
	
	/** returns true if container is fully started and accepting requests */
	public boolean isReady()
	{	return mIsReady;
	}
	
	/** returns the root thread group
	 */
	public ThreadGroup getRootThreadGroup()
	{	return mOriginalThreadGroup;
	}
	
	
	/** returns the number of milliseconds of uptime
	 */
	public long getUptime()
	{	return System.currentTimeMillis()-mStartTime;
	}
	
	/** Starts the container 
	 * @param aContainer not used, implemented as part of IComponent
	 */
	public void start(Container aContainer) throws NetKernelException
	{	//Print copyright notice
		bootNotice();
		//Clear out components
		mComponentInstances.clear();
		
		//First Boot the config
		startComponent(com.ten60.netkernel.container.Config.class);
		ensureScratchFound();
		configLoggers();
		mOriginalThreadGroup=Thread.currentThread().getThreadGroup();
		
		//Now boot the rest
		SysLogger.log(SysLogger.CONTAINER, this, "Starting container...");
		startComponents(false);
		mIsStarted=true;
		SysLogger.log(SysLogger.CONTAINER, this, "Container started sucessfully");
		mStartTime = System.currentTimeMillis();
		
		// register shutdown hook
		mShutdownThread = new ShutdownHook(this);
		Runtime.getRuntime().addShutdownHook(mShutdownThread);
		
		// now look for init task and execute
		runInitProcess();
	}
	
	/** Starts the container and wait for restart or stop */
	public void start() throws NetKernelException
	{	Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
		start(this);
		
		// wait for cold restart or stop
		synchronized(this)
		{	try
			{	this.wait();
			} catch (InterruptedException e) { /* ignore */ }
		}
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
		
	}
	
	/** Runs the init process without waiting for it to complete- however on another
	 * thread it waits for completion before allowing the receiving of external requests */
	private void runInitProcess()
	{
		Config c = (Config)getComponent(Config.URI);

		URIdentifier moduleURI =new URIdentifier(c.getReadable() .getText("/system/initModule").trim());
		URIdentifier requestURI = new URIdentifier(c.getReadable().getText("/system/initURI").trim());
		if (moduleURI.toString().length()!=0 && requestURI.toString().length()!=0)
		{	
			try
			{	ensureExternalRequestContext();
				ModuleManager mm = (ModuleManager)getComponent(ModuleManager.URI);
				ModuleDefinition md = mm.getModule(moduleURI,null,null);
				final TransportManager tm = (TransportManager)getComponent(TransportManager.URI);
				final URRequest request = new URRequest(requestURI, null, createSession(), md, URRequest.RQT_SOURCE, null, null, IURAspect.class);
				Runnable r = new Runnable()
				{	public void run()
					{	try
						{	tm.innerHandleRequest(request,mInternalTransport);
						}
						catch (Exception e)
						{	SysLogger.log(SysLogger.WARNING, this, "Failed to launch init process");
							e.printStackTrace();
						}
						finally
						{	Runtime.getRuntime().gc();
							SysLogger.log(SysLogger.CONTAINER, this, "Accepting external requests...");
							tm.acceptRequests();
							mIsReady=true;
							
						}
					}
				};
				Thread t = new Thread(r);
				t.start();
			}
			catch (Exception e)
			{	SysLogger.log(SysLogger.WARNING, this, "Failed to launch init process");
				e.printStackTrace();
			}
		}
		else
		{	// no init process so accept requests now
			TransportManager tm = (TransportManager)getComponent(TransportManager.URI);
			SysLogger.log(SysLogger.CONTAINER, this, "Accepting external requests...");
			tm.acceptRequests();			
		}
	}		

	/** Iterates through all components in <code>mBootList</code> starting them
	 */
	private void startComponents(boolean aHot) throws NetKernelException
	{
		NetKernelException result = null;
		for (int i=0; i<mBootList.length; i++)
		{	Class componentClass = mBootList[i];
			try
			{	
				if (!aHot || componentClass!=TransportManager.class)
				{	startComponent(componentClass);
				}
				else
				{	IComponent component = (IComponent)mComponentInstances.get(TransportManager.URI);
					component.start(this);
				}
			} catch (NetKernelException e)
			{	if (result==null)
				{	result = new NetKernelException("Failed to start container","Some components did not start correctly",null);
				}
				result.addCause(e);
			}
		}
		mScheduler = (Scheduler)getComponent(Scheduler.URI);
		mExternalRequestContext=null;

		ModuleManager mm = (ModuleManager)getComponent(ModuleManager.URI);
		mm.completeStartup();
		
		if (result!=null)
		{	SysLogger.log(SysLogger.CONTAINER, this, result.toString());
			throw result;
		}
	}
	
	
	/**Configure the Kernel Loggers */
	private void configLoggers() throws NetKernelException
	{	Config config = (Config)getComponent(Config.URI);
		XMLReadable c=config.getReadable();
		SysLogger.config(getBasePath(),c);
	}
	
	/** Return true if the container is started */
	public boolean isStarted()
	{	return mIsStarted;
	}

	/** starts a system component with the given class
	 *@param aComponentClass the class of the component to start
	 *@exception NetKernelException thrown if there was any failure to start component
	 */
	private void startComponent(Class aComponentClass) throws NetKernelException
	{	try
		{	SysLogger.log1(SysLogger.CONTAINER, this, "Starting %1", aComponentClass.getName());
			IComponent result = (IComponent)aComponentClass.newInstance();
			mComponentInstances.put(result.getURI(), result);
			result.start(this);
		}
		catch (Exception e)
		{	NetKernelException e2 = new NetKernelException("Failed to start component",null,aComponentClass.getName());
			e2.addCause(e);
			throw e2;
		}
	}
	
	/** stops the container
	 */
	public void stop() throws NetKernelException
	{	innerStop();
		if (mShutdownThread!=null)
		{	Runtime.getRuntime().removeShutdownHook(mShutdownThread);
			mShutdownThread.cleanup();
			mShutdownThread.start();
			mShutdownThread=null;
		}
	}
	
	void innerStop() throws NetKernelException
	{	mIsReady=false;
		SysLogger.log(SysLogger.CONTAINER, this, "Stopping container");

		TransportManager tm = (TransportManager)getComponent(TransportManager.URI);
		if (tm!=null)
		{	SysLogger.log(SysLogger.CONTAINER, this, "Rejecting new requests");
			tm.rejectRequests();
		}
		
		if (mScheduler!=null)
		{	mScheduler.getDebugger().releaseAll();
			SysLogger.log(SysLogger.CONTAINER, this, "Waiting for existing requests to complete...");
			tm.join();
		}
		
		stopComponents(false);
		SysLogger.log(SysLogger.CONTAINER, this, "Container stopped sucessfully");
		mIsStarted=false;
		mIsRestart=false;
		synchronized(this)
		{	this.notify();
		}
	}

	/** Stop all kernel components in reverse start-up order.
	 * @aHot if a hot stop the transport manager isn't stopped
	 */
	private void stopComponents(boolean aHot) throws NetKernelException
	{	NetKernelException result = null;
		List l = new ArrayList(mComponentInstances.values());
		Collections.reverse(l);
		for (Iterator i = l.iterator(); i.hasNext();)
		{	IComponent component = (IComponent)i.next();
			if (!aHot || !(component instanceof TransportManager))
			{	i.remove();
				try
				{	SysLogger.log1(SysLogger.CONTAINER, this, "Stopping [%1]", component.getClass().getName());
					component.stop();
				} catch (Exception e)
				{	if (result==null)
					{	result = new NetKernelException("Exceptions thrown stopping container","Some components didnt stop correctly",null);
					}
					result.addCause(e);
				}
			}
		}
		if (result!=null)
		{	SysLogger.log(SysLogger.CONTAINER, this, result.toString());
			throw result;
		}
	}
	
	
	/** restart the container to detect an changed modules without downtime
	 */
	public void restart(boolean aHot) 
	{	mIsReady=false;
		TransportManager tm = (TransportManager)getComponent(TransportManager.URI);
		if (aHot)
		{	SysLogger.log(SysLogger.CONTAINER, this, "Hot Restart initiated, holding new requests...");
			tm.holdRequests();
		}
		else
		{	SysLogger.log(SysLogger.CONTAINER, this, "Cold Restart initiated, rejecting new requests...");
			tm.rejectRequests();
		}
		SysLogger.log(SysLogger.CONTAINER, this, "Waiting for existing requests to complete...");
		mScheduler.getDebugger().releaseAll();
		tm.join();
		SysLogger.log(SysLogger.CONTAINER, this, "Stopping Kernel");
		boolean problems=false;
		try
		{	stopComponents(aHot);
		} catch (NetKernelException e)
		{	problems=true;
		}
		if (!aHot)
		{	ModuleManager.resetModuleFactory();
			XMLUtils.destroyInstances();
			mIsRestart=true;
			if (mShutdownThread!=null)
			{	Runtime.getRuntime().removeShutdownHook(mShutdownThread);
				mShutdownThread.cleanup();
				mShutdownThread.start();
				mShutdownThread=null;
			}
			synchronized(Container.this)
			{	Container.this.notify();
			}
		}
		else
		{	SysLogger.resetStats();
			SysLogger.log(SysLogger.CONTAINER, this, "Starting Kernel");
			try
			{	startComponents(aHot);
			} catch (NetKernelException e)
			{	problems=true;
			}
			if (problems)
			{	SysLogger.log(SysLogger.CONTAINER, this, "Restart Completed but problems were encountered");
			}
			else
			{	SysLogger.log(SysLogger.CONTAINER, this, "Restart Completed sucessfully");
			}
			runInitProcess();
		}
	}
	
	/** return a component with the given URI, null if none is found
	 */
	public IComponent getComponent(URIdentifier aURI)
	{	IComponent result;
		if (aURI.equals(URI))
		{	result = this;
		}
		else
		{	result = (IComponent)mComponentInstances.get(aURI);
		}
		return result;
	}
	/** return the absolute basepath that this container is using */
	public String getBasePath()
	{	return mBasePath;
	}
	/** return the absolute scratch dir path that this container is using */
	public String getScratchPath()
	{	return mScratchPath;
	}
	/** return the absolute config URI that this container is using */
	public String getConfigURI()
	{	return mConfig;
	}
	/** return the absolute basepath URI that this container is using - use this for guaranteed *nix/Win32 platform independence*/
	public String getBasePathURI()
	{	return mBasePathURI;
	}
	/** @return the absolute scratchpath URI that this container is using - use this for guaranteed *nix/Win32 platform independence*/
	public String getScratchPathURI()
	{	return mScratchPathURI;
	}
	
	/** internal method to ensure we have got the scratch directory from the config component
	 */
	private void ensureScratchFound() throws NetKernelException
	{	System.out.println("Basepath URI: "+mBasePathURI);

		if (mScratchPath==null)
		{	Config c = (Config)getComponent(Config.URI);
			File scratch = new File(mBasePath,c.getReadable().getText("system/scratchDir").trim());
			try
			{	mScratchPath = scratch.getCanonicalPath();
				if(!mScratchPath.endsWith("/")) mScratchPath+="/";
				mScratchPath=Utils.fixSlash(mScratchPath);
				mScratchPathURI = scratch.toURI().toString().replaceAll(" ","%20");
			} catch (IOException e)
			{
			}
		}
	}
	/** periodic housekeeping involves giving all our system components a chance to do
	 * housekeeping
	 */
	public void doPeriodicHouseKeeping()
	{	for (Iterator i=mComponentInstances.values().iterator(); i.hasNext(); )
		{	IComponent comp = (IComponent)i.next();
			comp.doPeriodicHouseKeeping();
		}
	}
	
	/** API for embedded/JMX to allow requests to be made on the container for resources
	 * @param aURI the URI to get the representation for
	 * @param aRepresentationClass the form that we want the result in
	 * @param aArgs any arguments to pass through with the request
	 * @return the resulting resource representation which will implement the specified interface
	 * @exception NetKernelException thrown if we fail to execute the request for any reason
	 */
	public IURRepresentation requestResource(URIdentifier aURI, Class aAspectClass, Map aArgs) throws NetKernelException
	{	IRequestorSession session = createSession();
		
		ensureExternalRequestContext();
		URRequest request = new URRequest(aURI, null, session, mExternalRequestContext, URRequest.RQT_SOURCE, null, null, aAspectClass);
		if (aArgs!=null)
		{	for (Iterator i = aArgs.entrySet().iterator(); i.hasNext(); )
			{	Map.Entry entry = (Map.Entry)i.next();
				URIdentifier uri = new URIdentifier((String)entry.getKey());
				IURRepresentation representation = (IURRepresentation)entry.getValue();
				request.addArg(uri,representation);
			}
		}
		TransportManager tm = (TransportManager)getComponent(TransportManager.URI);
		return tm.handleRequest(request,mInternalTransport);
	}
	
	/** creates a new session for the <code>requestResource</code> method
	 */
	private IRequestorSession createSession()
	{	return new com.ten60.netkernel.transport.TransportInitiatedSession();
	}
	
	/** returns a classloader with access to all classes and resources of the module
	 * which holds the internal transport */
	public ClassLoader getExternalRequestClassLoader() throws NetKernelException
	{	ensureExternalRequestContext();
		return mExternalRequestContext.getClassLoader();
	}
	
	/** ensures the internal transport is properly initialised
	 */
	private void ensureExternalRequestContext() throws NetKernelException
	{	if (mExternalRequestContext==null)
		{	ModuleManager mm = (ModuleManager)getComponent(ModuleManager.URI);
			PairList pl = mm.getTransports();
			for (int i=0; i<pl.size(); i++)
			{	if (pl.getValue1(i).equals(TransportManager.INTERNAL_TRANSPORT))
				{	mExternalRequestContext = (ModuleDefinition)pl.getValue2(i);
					break;
				}
			}
			TransportManager tm = (TransportManager)getComponent(TransportManager.URI);
			mInternalTransport = tm.getInternalTransport();
			
			if (mExternalRequestContext==null)
			{	throw new NetKernelException("No Module defined as fulcrum for Internal Transport");
			}
		}
	}
	
	public URLConnection openConnection(URL u) throws IOException
	{	return new NetKernelURLConnection(u,this);
	}
	
	/** write the boot notice to standard out **/
	private void bootNotice()
	{	System.out.println("************************************************************");
		System.out.println("* 1060(R) NetKernel(TM) version "+VERSION );
		System.out.println("* Copyright (C) 2002-2008, 1060 Research Limited");
		System.out.println("* Licensed under the 1060 Public License v1.0");
		System.out.println("* To review this license or to obtain alternative licenses");
		System.out.println("* please visit www.1060research.com");
		System.out.println("************************************************************");
	}
	
	/** writes the state of the container out to the given stream as XML
	 */
	public void write(OutputStream aStream) throws IOException
	{
		OutputStreamWriter osw = new OutputStreamWriter(aStream);
		osw.write("<container>");
			osw.write("<basepath>");
			java.net.URI u = java.net.URI.create(getBasePathURI());
			File f = new File(u);
			osw.write(XMLUtils.escape(f.getAbsolutePath()+File.separator));
			osw.write("</basepath>");
			osw.write("<basepathURI>");
			osw.write(getBasePathURI());
			osw.write("</basepathURI>");
		osw.write("</container>");
		osw.flush();
	}
	
	/** returns the URL where the bootloader configuration is being read from
	 */
	public URL getBootloaderConfigURL() throws MalformedURLException
	{	URL result = new URL(getBasePathURI()+"etc/bootloader.cfg");
		return result;
	}
	
	/** returns the URL where the kernel classes are being loaded from,
	 * if they are being loaded from the classpath null is returned.
	 */
	public URL getKernelSourceURL() throws IOException
	{	URL result;
		URL bootloaderConfigURL = getBootloaderConfigURL();
		if (bootloaderConfigURL!=null)
		{	InputStream is = bootloaderConfigURL.openStream();
			try
			{	BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String path=br.readLine();
				if(File.separatorChar == '/') path="file:"+path;
				else path="file:///"+path;
				path=path.replace('\\', '/');
				path=path.replaceAll(" ", "%20");
				result = new URL(path);
			}
			finally
			{	is.close();
			}
		}
		else
		{	result = null;
		}
		return result;
	}
}