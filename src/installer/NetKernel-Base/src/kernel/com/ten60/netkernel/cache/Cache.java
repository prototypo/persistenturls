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
 * File:          $RCSfile: Cache.java,v $
 * Version:       $Name:  $ $Revision: 1.6 $
 * Last Modified: $Date: 2005/11/10 20:46:03 $
 *****************************************************************************/
package com.ten60.netkernel.cache;

import com.ten60.netkernel.container.*;
import com.ten60.netkernel.urii.*;
import com.ten60.netkernel.module.*;
import com.ten60.netkernel.urrequest.*;
import com.ten60.netkernel.util.*;
import com.ten60.netkernel.urii.aspect.VoidAspect;

import java.util.*;
import java.io.*;
/**
 * NetKernel Cache Component- responsible for locating a cachelet for getting or putting an
  * IURRepresentation from an URRequest.
 * @author  tab
 */
public class Cache extends ComponentImpl
{	/** our static URI **/
	public static final URIdentifier URI = new URIdentifier("netkernel:cache");
	/** expired resource in cache **/
	public static final IURRepresentation EXPIRED_RESOURCE=VoidAspect.create();
	/** set of modules that we have used that have cachelets instantiated **/
	private Set mCacheModules;
	/** default cache */
	private ICachelet mDefaultCache;
	
	/** Creates a new instance of Cache */
	public Cache()
	{	super(URI);
		mCacheModules = new HashSet();
	}
	
	public void registerCacheletModule(ModuleDefinition aModule)
	{	mCacheModules.add(aModule);
	}
	public void registerDefaultCacheletModule(ModuleDefinition aModule)
	{	mDefaultCache=aModule.getCache();
	}
	
	/** Determine a cachelet for a request
	 * @param aRequest the URRequest
	 * @return a cachelet or null if none is found
	 */
	private ICachelet getCacheForRequest(URRequest aRequest)
	{	ICachelet result;
		if (mCacheModules.size()>1)
		{
			ModuleDefinition module = ((ModuleDefinition)aRequest.getContext());
			result=module.getCache();
			if (result==null)
			{	List superStack = aRequest.getSuperStack();
				for (int i=superStack.size()-1; i>=0; i--)
				{	module =(ModuleDefinition)superStack.get(i);
					result = module.getCache();
					if (result!=null) break;
				}
			}
			if (result==null)
			{	result = mDefaultCache;
			}
		}
		else
		{	result = mDefaultCache;
		}
		return result;
	}
	
	/** Initialise the cache, finding the default cachelet
	 */
	public void start(Container aContainer) throws NetKernelException
	{	
	}
	
	public void stop() throws NetKernelException
	{	// remove references to cachlets to reduce change of leaks
		mDefaultCache=null;
		mCacheModules.clear();
	}

	/** public interface for getting a IURRepresentation that will satisfy the given request
	 * @param aRequest the request
	 * @return a representation that will satisfy the request 
	 * @exception NetKernelException thrown if the cachelet fails in anyway
	 */
	public IURRepresentation get(URRequest aRequest) throws NetKernelException
	{	IURRepresentation result = null;
		ICachelet cache = getCacheForRequest(aRequest);
		if (cache!=null)
		{	try
			{	result = cache.get(aRequest);
			}
			catch (Exception ex)
			{	NetKernelException e = new NetKernelException("Unhandled exception in Cache.get()",null, aRequest.toString());
				e.addCause(ex);
				throw e;
			}
		}
		return result;
	}
	
	/** public interface for putting informing the cache of a result- it is its choice whether to use it or not
	 * @param aResult a URResult that has been issued
	 * @exception NetKernelException thrown if the cachelet fails in any way
	 */
	public void put(URResult aResult) throws NetKernelException
	{	ICachelet cache = getCacheForRequest(aResult.getRequest());
		if (cache!=null)
		{	try
			{	cache.put(aResult);
			}
			catch (Exception ex)
			{	NetKernelException e = new NetKernelException("Unhandled exception in Cache.put()",null, aResult.getRequest().toString());
				e.addCause(ex);
				throw e;
			}
		}
	}
	
	/** outputs the state of the cache with each if its cachelets in XML
	 * @param aStream an output stream to write the XML to
	 * @exception IOException thrown if we fail to write to the stream
	 */
	public void write(OutputStream aStream) throws IOException
	{	OutputStreamWriter osw = new OutputStreamWriter(aStream);
		osw.write("<cache>");
		for (Iterator i = mCacheModules.iterator(); i.hasNext(); )
		{	ModuleDefinition md = (ModuleDefinition)i.next();
			ICachelet cache = (ICachelet)md.getCache();
			writeCachelet(osw,md,cache);
			cache = cache.getBackingCache();
			while (cache!=null)
			{	writeCachelet(osw, md,cache);
				cache = cache.getBackingCache();
			}
		}
		osw.write("</cache>");
		osw.flush();
	}		
	
	private void writeCachelet(OutputStreamWriter aStream, ModuleDefinition aModule, ICachelet aCachelet) throws IOException
	{	aStream.write("<cachelet>");
		XMLUtils.writeEscaped(aStream,"module", aModule.getURI().toString());
		XMLUtils.writeEscaped(aStream,"class", aCachelet.getClass().getName());
		XMLUtils.writeEscaped(aStream,"toString", aCachelet.toString());
		aStream.write("<data>");
		aCachelet.write(aStream);
		aStream.write("</data>");
		aStream.write("</cachelet>");
	}
}