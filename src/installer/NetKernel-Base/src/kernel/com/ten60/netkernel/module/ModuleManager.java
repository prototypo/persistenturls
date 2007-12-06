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
 * File:          $RCSfile: ModuleManager.java,v $
 * Version:       $Name:  $ $Revision: 1.21 $
 * Last Modified: $Date: 2006/02/21 18:45:22 $
 *****************************************************************************/
package com.ten60.netkernel.module;
import com.ten60.netkernel.container.*;
import com.ten60.netkernel.urii.*;
import com.ten60.netkernel.cache.*;
import com.ten60.netkernel.urii.accessor.*;
import com.ten60.netkernel.util.*;
import com.ten60.netkernel.urrequest.*;
import com.ten60.netkernel.module.rewrite.*;
import com.ten60.netkernel.module.accessor.ModuleResourceAccessor;
import com.ten60.netkernel.urii.representation.ITransrepresentor;
import com.ten60.netkernel.urii.fragment.IFragmentor;

import java.util.*;
import java.lang.reflect.*;
import org.w3c.dom.*;
import java.io.*;
import java.net.*;
import java.util.jar.*;
import java.util.zip.*;
import java.util.regex.*;

/** NetKernel system component which manages all deployed modules and their
 * public/private address spaces */
public class ModuleManager extends ComponentImpl
{
	public static final String MODULE_SCHEME="ffcpl";
	public static final String JAR_SUFFIX=".jar";
	public static final URIdentifier URI = new URIdentifier("netkernel:module");
	private MultiMap mModules = new MultiMap(64,2);
	private Container mContainer;
	private File mExpandedJarDir;
	private ModuleDefinition mDefaultCacheModule;
	private MappingCache mMappingCache;
	private static ModuleFactory mModuleFactory = new ModuleFactory();
	
	private static IDynamicClassProvider sDynamicClassProvider;
	private long mDefaultResourceExpiry;
	private long mLastModificationCheck;
	
	public ModuleManager()
	{	super(URI,true);
	}
	
	public static void resetModuleFactory()
	{	mModuleFactory.complete();
		mModuleFactory = new ModuleFactory();
	}
	
	public void start(Container aContainer) throws NetKernelException
	{	mContainer = aContainer;
		
		XMLReadable config = ((Config)aContainer.getComponent(Config.URI)).getReadable();
		if (sDynamicClassProvider==null)
		{	try
			{	String className=config.getText("system/dynamicClassProvider");
				if (className.length()>0)
				{	Class c = Class.forName(className);
					Constructor cstr=c.getConstructor(new Class[] { Container.class });
					sDynamicClassProvider = (IDynamicClassProvider)cstr.newInstance(new Object[] { aContainer });
					mDefaultResourceExpiry =config.getInt("system/defaultResourceExpiry", 4000);
					SysLogger.log1(SysLogger.CONTAINER, this, "  Using [%1] to provide dynamic Java classes",className);
				}
			}
			catch (Exception e)
			{	NetKernelException ex=new NetKernelException("Failed to Instantiate DynamicClassProvider");
				ex.addCause(e);
				SysLogger.log(SysLogger.SEVERE, this, ex.toString());
			}
		}
		int requestMappingCacheSize = config.getInt("system/requestMappingCacheSize", 512);
		mMappingCache = new MappingCache(requestMappingCacheSize);
		
		reparseModules(aContainer);
		
		URIdentifier uri = new URIdentifier(config.getText("system/defaultCacheModule").trim());
		List versions = mModules.get(uri);
		if (versions.size()>0)
		{	mDefaultCacheModule = (ModuleDefinition)versions.get(0);
			((Cache)mContainer.getComponent(Cache.URI)).registerDefaultCacheletModule(mDefaultCacheModule);
		}
		else
		{	mDefaultCacheModule = null;
		}
		
		mLastModificationCheck=System.currentTimeMillis()+mDefaultResourceExpiry*2;
	}
	
	public void stop() throws NetKernelException
	{	for (Iterator i = mModules.valueIterator(); i.hasNext(); )
		{	ModuleDefinition md = (ModuleDefinition)i.next();
			md.cleanupComponents();
		}
		
	}
	
	
	public void completeStartup()
	{	mModuleFactory.complete();
	}
	
	public ModuleDefinition getDefaultCacheModule()
	{	return mDefaultCacheModule;
	}
	
	public URL getDeployedModulesURL(Container aContainer) throws MalformedURLException
	{	Config c = (Config)aContainer.getComponent(Config.URI);
		java.net.URI basePath = java.net.URI.create(aContainer.getBasePathURI());
		java.net.URI deployedModulesURI = java.net.URI.create(c.getReadable().getText("system/deployedModules"));
		deployedModulesURI = basePath.resolve(deployedModulesURI);
		return deployedModulesURI.toURL();
	}
	
	private void reparseModules(Container aContainer) throws NetKernelException
	{
		Config c = (Config)aContainer.getComponent(Config.URI);
		long defaultResourceExpiry = c.getReadable().getInt("system/defaultResourceExpiry", 0);
		ModuleResourceAccessor.setDefaultResourceExpiry(defaultResourceExpiry);
		
		try
		{
			//load up deployed modules document
			URL deployedModulesURL = getDeployedModulesURL(aContainer);
			InputStream is = deployedModulesURL.openStream();
			XMLReadable deployedModules = new XMLReadable(XMLUtils.parse(is));

			// build list of exact module source URIs
			List modules = deployedModules.getTexts("/modules/module",true);
			java.net.URI basePath = java.net.URI.create(aContainer.getBasePathURI());
			NetKernelException exception=null;
			
			List moduleURIStrings = new ArrayList(modules.size());
			for (Iterator i=modules.iterator(); i.hasNext(); )
			{	URI moduleURI = java.net.URI.create((String)i.next());
				if (basePath!=null)
				{	moduleURI = basePath.resolve(moduleURI);
				}
				File file = new File(moduleURI);
				String moduleURIString=file.toURI().toString();
				if(File.separatorChar=='\\' && !moduleURIString.startsWith("file:///"))
				{	moduleURIString="file:///"+moduleURIString.substring(6);
				}
				if (moduleURIString.endsWith(JAR_SUFFIX))
				{	moduleURIString = "jar:"+moduleURIString+"!/";
				}
				moduleURIStrings.add(moduleURIString);
			}
			
			//preparse modules to install
			mModuleFactory.preParseModules(moduleURIStrings);
			
			//iterate and initialise module definitions
			mModules.clear();
			for (Iterator i = moduleURIStrings.iterator(); i.hasNext(); )
			{	String moduleURIString = (String)i.next();
				try
				{	ModuleDefinition md = mModuleFactory.getModuleFor(moduleURIString,this);
					URIdentifier uri = md.getURI();
					mModules.put(uri,md);
				} catch (Throwable t)
				{	NetKernelException e2 = new NetKernelException("Failed to Load Module", null, moduleURIString);
					e2.addCause(t);
					SysLogger.log2(SysLogger.SEVERE, this, "Failed to Load Module at [%1] : %2",moduleURIString, t.toString());
					if (exception==null)
					{	exception=new NetKernelException("Problems with Module Loading");
					}
					exception.addCause(e2);
				}
			}

			// 2nd pass do linking
			for (Iterator i = mModules.valueIterator(); i.hasNext(); )
			{	ModuleDefinition md = (ModuleDefinition)i.next();
				try
				{	md.parseMappings(this,aContainer);
				} catch (Throwable t)
				{	if (exception==null)
					{	exception=new NetKernelException("Problems with Module Linking");
					}
					exception.addCause(t);
				}
			}
			
			//3rd pass initialise transreptors
			for (Iterator i = mModules.valueIterator(); i.hasNext(); )
			{	ModuleDefinition md = (ModuleDefinition)i.next();
				try
				{	md.parseFinal(mContainer);
				} catch (Throwable t)
				{	if (exception==null)
					{	exception=new NetKernelException("Problems with Module Linking");
					}
					exception.addCause(t);
				}
			}
			if (exception!=null)
			{	SysLogger.log(SysLogger.WARNING, this, exception.toString());
			}

		}
		catch (Exception e)
		{	NetKernelException nke = new NetKernelException("problems starting ModuleManager");
			nke.addCause(e);
			throw nke;
		}
	}
	
	/** Find the latest version of a module that satisfy the optional criteria
	 * @exception NetKernelException thrown if no match can be found
	 */
	public ModuleDefinition getModule(URIdentifier aURI, Version aMin, Version aMax) throws NetKernelException
	{	ModuleDefinition result = null;
		ModuleDefinition wrongVersion = null;
		List modules = mModules.get(aURI);
		for (Iterator i = modules.iterator(); i.hasNext(); )
		{	ModuleDefinition md = (ModuleDefinition)i.next();
			
			if (aMin!=null)
			{	if (md.getVersion().isLessThan(aMin))
				{	wrongVersion = md;
					continue;
				}
			}
			if (aMax!=null)
			{	if (md.getVersion().isGreaterThan(aMax))
				{	wrongVersion = md;
					continue;
				}
			}
			if (result==null || result.getVersion().isLessThan(md.getVersion()))
			{	result = md;
			}
		}
		
		if (result==null)
		{	String message;
			if (wrongVersion!=null)
			{	message = "imported module of correct version not found";
			}
			else
			{	message = "imported module not found ";
			}
			throw new NetKernelException(message,message,aURI.toString());
		}
		
		return result;
	}
	
	public PairList getTransports()
	{	PairList result = new PairList(16);
		for (Iterator i = mModules.valueIterator(); i.hasNext(); )
		{	ModuleDefinition md = (ModuleDefinition)i.next();
			List transports = md.getReadable().getTexts("module/transports/transport",true);
			for (Iterator j = transports.iterator(); j.hasNext(); )
			{	String transport = (String)j.next();
				result.put(transport, md);
			}
		}
		return result;
	}

	/** Performs the resolution process for a request and optionally captures debug
	 * @param aRequest the request to resolve
	 * @param aDebug true if we should capture debug trace
	 * @return a mapped request object
	 */
	public MappedRequest getAccessorForRequest(URRequest aRequest, boolean aDebug) throws NetKernelException
	{	
		boolean shouldLog = SysLogger.shouldLog(SysLogger.DEBUG,this);
		if (shouldLog) aDebug=true;
		
		MappedRequest request=null;
		if (!aDebug)
		{	request = mMappingCache.get(aRequest);
		}
		
		if (request==null)
		{	ModuleDefinition module = (ModuleDefinition)aRequest.getContext();
			List lookedAtModules = new ArrayList(16);
			List debug=null;
			if (aDebug)
			{	debug = new ArrayList(128);
				debug.add(new MappingDebug(MappingDebug.DBG_SEARCH, aRequest.getURI()));
				debug.add(new MappingDebug(MappingDebug.DBG_START_MODULE, module));
			}

			List superStack = aRequest.getSuperStackClone();
			boolean rewrite = (superStack.size()==0); // rewrite requests from outside world (transports)
			request = getAccessorInModule(module,aRequest.rewrite(aRequest.getURI()),rewrite,lookedAtModules,superStack,debug,superStack.size());
			if (request==null)
			{	request = new MappedRequest(null, aRequest, 0);
				if (aDebug)
				{	debug.add(new MappingDebug(MappingDebug.DBG_NO_MAPPING, aRequest.getURI()));
				}
			}
			else
			{	mMappingCache.put(aRequest,request);
			}
			if (aDebug)
			{	request.setDebug(debug);
				if (shouldLog)
				{	for (Iterator i = debug.iterator(); i.hasNext(); )
					{	MappingDebug md = (MappingDebug)i.next();
						SysLogger.log(SysLogger.DEBUG,this, md.toString());
					}
				}
			}
		}
		 
		return request;
	}
	
	/** Resolves a request on a given module. This method encapsulates the resolution rules
	 * for processing rewrite rules and mappings. It may involve recursive calling for imports and
	 * supers.
	 * @aModule the module to resolve against
	 * @aRequest the request to resolve
	 * @aIsExternal only when we enter a module from outside (from an import or a transport request) do we apply the rewrite section
	 * @aLookedAtModules a list of modules that we have looked at already to optimize the search path, this is reset if we rewrite the request
	 * @aSuperStack a list of modules we have traversed down via imports
	 * @aDebug an optional list that will capture all resolution steps, may be null
	 * @return a MappedRequest structure of null if nothing matched
	 */
	private MappedRequest getAccessorInModule(ModuleDefinition aModule, URRequest aRequest, boolean aIsExternal, List aLookedAtModules, List aSuperStack, List aDebug, int aDepth) throws NetKernelException
	{	MappedRequest result=null;
		boolean visitedAlready = aLookedAtModules.contains(aModule);
		if (!visitedAlready || !aIsExternal)
		{	aLookedAtModules.add(aModule);
			URIdentifier uri = aRequest.getURI();

			// perform external to internal rewrite rules
			if (aIsExternal)
			{	uri = aModule.rewrite(uri);
				if (uri!=aRequest.getURI())
				{	aLookedAtModules.clear(); 
					if (aDebug!=null)
					{	aDebug.add(new MappingDebug(MappingDebug.DBG_REWRITE,uri));
					}
				}
			}
			
			// find mapping
			String uriString = uri.toString();
			URIMapping mapping=null;
			int debugCode;
			URIMapping[] mappings = aModule.getMappings();
			for (int i=0; i<mappings.length; i++)
			{	URIMapping mapping2 = mappings[i];
				ModuleDefinition md = mapping2.getImportModule();
				debugCode = MappingDebug.DBG_IGNORE_MAPPING;
				if (md==null || !aLookedAtModules.contains(md))
				{	if (mapping2.matches(uriString))
					{	
						RegexRewriterRule rewrite = mapping2.getRewriterRule();
						if (rewrite!=null)
						{	debugCode = MappingDebug.DBG_REWRITE;
							uri = rewrite.map(uri);
							uriString = uri.toString();
							aLookedAtModules.clear();
							mapping2=null;
							if (aDebug!=null)
							{	aDebug.add(new MappingDebug(debugCode, uriString));
							}
						}
						else if (mapping2.getSkipCount()>0)
						{	debugCode = MappingDebug.DBG_SKIP;
							i+=mapping2.getSkipCount();
						}
						else
						{	debugCode = MappingDebug.DBG_MATCH_MAPPING;
							mapping=mapping2;
						}
					}
					else
					{	debugCode = MappingDebug.DBG_FAIL_MAPPING;
						if (md!=null)
						{	aLookedAtModules.add(md);
						}
					}
				}
				if (aDebug!=null && mapping2!=null)
				{	aDebug.add(new MappingDebug(debugCode, mapping2));
				}
				if (mapping!=null) break;
			}
			
			// update request with rewritten uri if necessary
			URRequest request;
			if (uri!=aRequest.getURI())
			{	request = aRequest.rewrite(uri);
			}
			else
			{	request = aRequest;
			}
			
			// process mapping
			if (mapping!=null)
			{	if (mapping.getImportModule()!=null)
				{	ModuleDefinition md = mapping.getImportModule();
					aSuperStack.add(aModule);
					if (aDebug!=null)
					{	aDebug.add(new MappingDebug(MappingDebug.DBG_ENTER_IMPORT, md));
					}
					result = getAccessorInModule(md,request,true,aLookedAtModules,aSuperStack,aDebug,aDepth);
				}
				else if (mapping.getURAClass()!=null)
				{	String accessorClassString = mapping.getURAClass();
					request.setCurrentContext(aModule,aSuperStack); 
					result = new MappedRequest(accessorClassString, request,aDepth);
				}
				else if (mapping.getType()==URIMapping.TYPE_THIS)
				{	request.setCurrentContext(aModule,aSuperStack);
					result = new MappedRequest(ModuleResourceAccessor.class.getName(), request,aDepth);
				}
				if (result==null && mapping.getType()==URIMapping.TYPE_SUPER &&  !aIsExternal)
				{	int superSize = aSuperStack.size();
					if (superSize>0)
					{	ModuleDefinition md = (ModuleDefinition)aSuperStack.remove(superSize-1);
						if (aDebug!=null)
						{	aDebug.add(new MappingDebug(MappingDebug.DBG_ENTER_SUPER, md));
						}
						result = getAccessorInModule(md, request, false,aLookedAtModules,aSuperStack,aDebug,aDepth-1);
					}
				}
			}
		}
		return result;
	}
	
	public ITransrepresentor getTransrepresentorFor(IURRepresentation aFrom, Class aTo, URRequest aRequest)
	{	ModuleDefinition md = (ModuleDefinition)aRequest.getContext();
		List lookedAtModules = new ArrayList(16);
		List superStack = aRequest.getSuperStackClone();
		
		ITransrepresentor result=null;
		while(true)
		{	result = md.getTransrepresentorFor(aFrom,aTo,lookedAtModules);
			if (result!=null)
			{	break;
			}
			else
			{	int superSize = superStack.size();
				if (superSize>0)
				{	md = (ModuleDefinition)superStack.remove(superSize-1);
				}
				else
				{	break;
				}
			}
		}
		return result;
	}
	
	public IFragmentor getFragmentorFor(URRequest aRequest)
	{	ModuleDefinition md = (ModuleDefinition)aRequest.getContext();
		List lookedAtModules = new ArrayList(16);
		List superStack = aRequest.getSuperStackClone();
		
		IFragmentor result=null;
		while(true)
		{	result = md.getFragmentorFor(aRequest, lookedAtModules);
			if (result!=null)
			{	break;
			}
			else
			{	int superSize = superStack.size();
				if (superSize>0)
				{	md = (ModuleDefinition)superStack.remove(superSize-1);
				}
				else
				{	break;
				}
			}
		}
		return result;
	}
	
	
	URL expandNestedJar(JarFile aJarFile, ZipEntry aEntry, URIdentifier aURI, Version aVersion) throws IOException, MalformedURLException
	{	if (mExpandedJarDir==null)
		{
			Config config = (Config)mContainer.getComponent(Config.URI);
			String relDir = config.getReadable().getText("system/expandedJarPool").trim();
			mExpandedJarDir = new File(mContainer.getBasePath(),relDir);
		}
		
		String filename = aURI.toString()+aVersion.toString(3)+aEntry.getName().substring(3);
		StringBuffer sb = new StringBuffer(filename.length()+4);
		for (int i=0; i<filename.length(); i++)
		{	char c = filename.charAt(i);
			if (Character.isLetterOrDigit(c))
			{	sb.append(c);
			}
			else
			{	sb.append('_');
			}
		}
		sb.append(".jar");
		filename = sb.toString();
		File jarFile = new File(mExpandedJarDir,filename);
		if (!jarFile.exists())
		{	SysLogger.log1(SysLogger.CONTAINER, this,"    expanding nested jar [%1]", aEntry.getName());
			
			FileOutputStream fos=null;
			InputStream is=null;
			fos = new FileOutputStream(jarFile);
			is = aJarFile.getInputStream(aEntry);
			Utils.pipe(is,fos);
		}
		return new URL(jarFile.toURI().toString());
	}
	
	public void write(OutputStream aStream) throws IOException
	{	OutputStreamWriter osw = new OutputStreamWriter(aStream);
		osw.write("<modules>");
		writeKernelModule(osw);
		for (Iterator i = mModules.valueIterator(); i.hasNext(); )
		{	ModuleDefinition md = (ModuleDefinition)i.next();
			md.write(osw);
		}
		osw.write("</modules>");
		osw.flush();
	}
	
	private void writeKernelModule(Writer aWriter) throws IOException
	{	aWriter.write("<module>");
		aWriter.write("<identity>");
		aWriter.write("<uri>");
		aWriter.write(Container.NETKERNEL_URN);
		aWriter.write("</uri>");
		aWriter.write("<version>");
		aWriter.write(Container.VERSION);
		aWriter.write("</version>");
		aWriter.write("</identity>");
		aWriter.write("<info>");
		aWriter.write("<name>NetKernel</name>");
		aWriter.write("<description>NetKernel microkernel</description>");
		aWriter.write("<timestamp>0</timestamp>");
		aWriter.write("<source>");
		URL url = mContainer.getKernelSourceURL();
		if (url!=null)
		{	aWriter.write(XMLUtils.escape(url.toString()));
		}
		aWriter.write("</source>");
		aWriter.write("</info>");
		aWriter.write("</module>");
	}
	
	public void doPeriodicHouseKeeping()
	{	
		if (sDynamicClassProvider!=null)
		{	if (mContainer.isReady())
			{	// only check for changes once system is up and accepting requests
				long now=System.currentTimeMillis();
				if (mLastModificationCheck+mDefaultResourceExpiry<now)
				{	mLastModificationCheck=now;
					if (sDynamicClassProvider.isModified())
					{	mLastModificationCheck=Long.MAX_VALUE-mDefaultResourceExpiry;
						SysLogger.log(SysLogger.CONTAINER, this, "Changes detected in DynamicClassProvider. Initiating hot restart");
						// issue hot restart
						Thread restartThread = new Thread()
						{	public void run()
							{	mContainer.restart(true);
							}
						};
						restartThread.start();
					}
				}
			}

		}
	}
	
	IDynamicClassProvider getDynamicClassProvider()
	{	return sDynamicClassProvider;
	}
}