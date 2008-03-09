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
 * File:          $RCSfile: NetKernelError.java,v $
 * Version:       $Name:  $ $Revision: 1.3 $
 * Last Modified: $Date: 2005/09/12 12:46:27 $
 *****************************************************************************/
package com.ten60.netkernel.util;
import java.io.*;
import java.util.*;

/**
 * NetKernel exception is a generic exception class used for most exceptions in the NetKernel. It is
 * nestable and allows multiple exceptions to be nested at any depth. Its most important characteristic
 * is that its state can be recursively dumped as XML giving a clear picture of an exception scenario is
 * a document.
 * @author  tab
 */
public class NetKernelError extends java.lang.Error implements IXMLException
{
	/** depth that java exception stack traces will be captured - configured by Config system component */
	private static int sTraceDepth=4;
	/** exception Id */
	private String mId;
	/** request that caused exception */
	private String mRequestId;
	/** list of causes of the exception */
	private ArrayList mCauses=null;
	
	/**
	 * Constructs an instance of <code>NetKernelException</code> with the specified detail message.
	 * @param aMessage the detail message.
	 */
	public NetKernelError(String aMessage)
	{	super(aMessage);
	}
	
	/**
	 * Constructs an instance of <code>NetKernelException</code> with the specified Id, detail message, and
	 * request id.
	 * @param aId an id for the exception
	 * @param aMessage the detail message.
	 * @param aRequestId id of request that caused the exception
	 */
	public NetKernelError(String aId, String aMessage, String aRequestId)
	{	super(aMessage);
		mId = aId;
		mRequestId = aRequestId;
	}
	
	/** @Return the id for the exception
	 */
	public String getId()
	{	return mId;
	}
	
	/** Adds an exception cause to this exception
	 */
	public void addCause(Throwable aThrowable)
	{	if (mCauses==null)
		{	mCauses = new ArrayList(4);
		}
		mCauses.add(aThrowable);
	}
	
	public String getDeepestId()
	{	String result=null;
		if (mCauses!=null)
		{	for (Iterator i=mCauses.iterator(); i.hasNext(); )
			{	Throwable t = (Throwable)i.next();
				if (t instanceof NetKernelException)
				{	result = ((NetKernelException)t).getDeepestId();
				}
				else
				{	result = t.getClass().getName();
				}
				break;
			}
		}
		if (result==null)
		{	result = getId();
		}
		return result;
	}
	
	/** Recursively output the state of this exception and its causes to the given writer
	 */
	public void appendXML(Writer aWriter) throws IOException
	{	aWriter.write("<ex>");
		if (mId!=null) XMLUtils.write(aWriter,"id",mId);
		else XMLUtils.write(aWriter,"id",this.getClass().getName());
		if (getMessage()!=null) XMLUtils.write(aWriter,"message",getMessage());
		if (mRequestId!=null) XMLUtils.write(aWriter,"requestid",mRequestId);
		XMLUtils.writeStack(aWriter,this.getStackTrace(),sTraceDepth);

		if (mCauses!=null)
		{	for (int i=0; i<mCauses.size(); i++)
			{	Throwable t = (Throwable)mCauses.get(i);
				if (t instanceof IXMLException)
				{	((IXMLException)t).appendXML(aWriter);
				}
				else
				{	NetKernelException.writeThrowable(t,aWriter);
				}
			}
		}
		aWriter.write("</ex>");
	}
	
	/** return XML representation of recursive exception pretty-printed with indent
	 */
	public String toString()
	{	StringWriter sw=new StringWriter(1024);
		try
		{	appendXML(sw);
			org.w3c.dom.Document d = XMLUtils.parse(new StringReader(sw.toString()));
			return XMLUtils.getInstance().toXML(d,true,true);
		} catch (Exception e) { e.printStackTrace(); System.out.println(sw); }
		return "";
	}
	
	/** configure the depth that java exceptions stack traces will be captured
	 */
	public static void setTraceDepth(int aDepth)
	{	sTraceDepth = aDepth;
	}
	
	public Throwable getCause()
	{	Throwable result=null;
		if (mCauses!=null)
		{	result = (Throwable)mCauses.get(0);
		}
		return result;
	}
}
