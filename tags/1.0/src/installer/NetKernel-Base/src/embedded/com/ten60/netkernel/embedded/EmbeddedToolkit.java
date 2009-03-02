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
 * File:          $RCSfile: EmbeddedToolkit.java,v $
 * Version:       $Name:  $ $Revision: 1.1 $
 * Last Modified: $Date: 2004/06/18 10:20:38 $
 *****************************************************************************/
package com.ten60.netkernel.embedded;

import com.ten60.netkernel.util.NetKernelException;

import org.w3c.dom.*;
import java.lang.reflect.*;
import java.net.URI;
import java.io.*;
/**
 * Methods to handle the reflection required to extract resource data from requests for common data
 * types. The code in this class can act as template for supporting further datatypes.
 * @author  tab
 */
public class EmbeddedToolkit
{
	/** Request a resource and return its result as an XML DOM
	 * @param aAPI the NetKernel instance to use
	 * @param aURI the URI of the resource
	 * @param aArgs any arguments to pass to the request by value, may be null
	 * @exception EmbeddedException thrown if the request fails for any reason.
	 * @return the Document result.
	 */
	public static Document requestResourceAsDOM(IEmbeddedAPI aAPI, URI aURI, RequestArgs aArgs) throws EmbeddedException
	{	try
		{	Class c = aAPI.getClassloader().loadClass("org.ten60.netkernel.xml.representation.IAspectDOM");
			Object result = aAPI.requestResource(aURI, c, aArgs);
			java.lang.reflect.Method m = c.getDeclaredMethod("getReadOnlyDocument", new Class[0] );
			Document d = (Document)m.invoke(result,new Object[0]);
			return d;
		} catch (Exception e)
		{	NetKernelException e2 = new NetKernelException("Exception in requestResourceAsDOM");
			e2.addCause(e);
			throw new EmbeddedException(e2);
		}
	}	

	/** Request a resource and write it to an java.io.OutputStream
	 * @param aAPI the NetKernel instance to use
	 * @param aURI the URI of the resource
	 * @param aArgs any arguments to pass to the request by value, may be null
	 * @param aOutputStream the outputstream to write to
	 * @exception EmbeddedException thrown if the request fails for any reason.
	 */
	public static void writeResource(IEmbeddedAPI aAPI, URI aURI, RequestArgs aArgs, OutputStream aStream) throws EmbeddedException
	{	try
		{	Class c = aAPI.getClassloader().loadClass("com.ten60.netkernel.urii.aspect.IAspectBinaryStream");
			Object result = aAPI.requestResource(aURI, c, aArgs);
			java.lang.reflect.Method m = c.getDeclaredMethod("write", new Class[] {OutputStream.class} );
			m.invoke(result,new Object[]{ aStream } );
		} catch (Exception e)
		{	NetKernelException e2 = new NetKernelException("Exception in writeResource");
			e2.addCause(e);
			throw new EmbeddedException(e2);
		}
	}	
}