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
 * File:          $RCSfile: NetKernelExceptionAspect.java,v $
 * Version:       $Name:  $ $Revision: 1.3 $
 * Last Modified: $Date: 2004/09/28 12:08:58 $
 *****************************************************************************/
package com.ten60.netkernel.urii.aspect;

import com.ten60.netkernel.urii.IURMeta;
import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.util.*;
import com.ten60.netkernel.urii.meta.MetaImpl;
import com.ten60.netkernel.urii.representation.MonoRepresentationImpl;

import java.io.*;
/**
 * Implementation of an aspect that implements IAspectNetKernelException and IRepresentationBinaryStream
 * @author  tab
 */
public class NetKernelExceptionAspect implements IAspectNetKernelException, IAspectBinaryStream
{
	private static IURMeta sMeta = new MetaImpl("application/vnd.netkernel-exception",0,0);
	
	/** the exception we are holding */
	private NetKernelException mException;
	/** the error we are holding */
	private NetKernelError mError;
	
	/** Creates a new instance of NetKernelExceptionAspect */
	NetKernelExceptionAspect(NetKernelException aException)
	{	mException = aException;
	}
	/** Creates a new instance of NetKernelExceptionAspect */
	NetKernelExceptionAspect(NetKernelError aError)
	{	mError = aError;
	}
	
	/** Return the exception */
	public NetKernelException getException()
	{	if (mException!=null)
		{	return mException;
		}
		else
		{	NetKernelError e = new NetKernelError("Assumed Exception not Error");
			e.addCause(mError);
			throw e;
		}
	}
	/** Return the error */
	public NetKernelError getError()
	{	return mError;
	}
	
	public IXMLException getXMLException()
	{	IXMLException result;
		if (mError!=null)
		{	result = mError;
		}
		else
		{	result = mException;
		}
		return result;
	}
	
	/** Write the exception as XML to the outputstream */
	public void write(OutputStream aStream) throws IOException
	{	OutputStreamWriter osw = new OutputStreamWriter(aStream, "UTF-8");
		if (mException!=null)
		{	mException.appendXML(osw);
		}
		else
		{	mError.appendXML(osw);
		}
		osw.flush();
	}
	
	/** Create a new representation to hold and exception */
	public static IURRepresentation create(NetKernelException aException)
	{	return new MonoRepresentationImpl(sMeta, new NetKernelExceptionAspect(aException));
	}
	/** Create a new representation to hold and exception */
	public static IURRepresentation create(NetKernelError aError)
	{	return new MonoRepresentationImpl(sMeta, new NetKernelExceptionAspect(aError));
	}
	
	/* Get encoding */
	public String getEncoding()
	{	return "UTF-8";
	}
	
}
