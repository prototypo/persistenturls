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
 * File:          $RCSfile: EmbeddedAPI.java,v $
 * Version:       $Name:  $ $Revision: 1.2 $
 * Last Modified: $Date: 2005/09/27 10:57:25 $
 *****************************************************************************/
package com.ten60.netkernel.embedded;

import com.ten60.netkernel.container.Container;
import com.ten60.netkernel.urii.URIdentifier;
import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.util.SysLogger;
import com.ten60.netkernel.util.NetKernelException;
import com.ten60.netkernel.*;
import java.util.*;
import org.w3c.dom.*;

/**
 * Implementation of IEmbeddedAPI
 * @author  tab
 */
class EmbeddedAPI implements IEmbeddedAPI
{
	private Container mContainer;
	
	/** Creates a new instance of EmbeddedAPI */
	public EmbeddedAPI(String aBasepath, String aConfig)
	{	mContainer = new Container(aBasepath,aConfig);
	}
	
	public boolean isStarted()
	{	return mContainer.isStarted();
	}
	
	public IURRepresentation requestRepresentation(java.net.URI aURI, Class aResponseClass, RequestArgs aArgs) throws EmbeddedException
	{	URIdentifier uri = new URIdentifier(aURI.toString());
		try
		{	Map args = null;
			if (aArgs!=null)
			{	args = aArgs.getArgs();
			}
			return mContainer.requestResource(uri, aResponseClass,args);
		} catch (NetKernelException e)
		{	throw new EmbeddedException(e);
		}
	}
	public Object requestResource(java.net.URI aURI, Class aResponseClass, RequestArgs aArgs) throws EmbeddedException
	{	return requestRepresentation(aURI,aResponseClass,aArgs).getAspect(aResponseClass);
	}

	public void setLogging(int aLevel, boolean aEnabled)
	{	SysLogger.setLoggingFor(aLevel, aEnabled);
	}
	
	public void start() throws EmbeddedException
	{	try
		{	mContainer.start(mContainer);
		} catch (NetKernelException e)
		{	throw new EmbeddedException(e);
		}
		catch (Exception e)
		{	NetKernelException e2= new NetKernelException("Unhandled Exception");
			e2.addCause(e);
			throw new EmbeddedException(e2);
		}
	}
	
	public void stop() throws EmbeddedException
	{	try
		{	mContainer.stop();
		} catch (NetKernelException e)
		{	throw new EmbeddedException(e);
		}
	}
	
	public ClassLoader getClassloader() throws EmbeddedException
	{	try
		{	return mContainer.getExternalRequestClassLoader();
		} catch (NetKernelException e)
		{	throw new EmbeddedException(e);
		}
		
	}
	
}
