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
 * File:          $RCSfile: StringAspect.java,v $
 * Version:       $Name:  $ $Revision: 1.2 $
 * Last Modified: $Date: 2004/07/30 11:16:15 $
 *****************************************************************************/
package com.ten60.netkernel.urii.aspect;

import com.ten60.netkernel.urii.*;
import com.ten60.netkernel.urii.representation.*;
import java.io.*;
/**
 * Implementation of IAspectString and IAspectReadableBinaryStream that holds
 * a java.lang.String
 * @author  tab
 */
public class StringAspect implements IAspectReadableBinaryStream, IAspectString
{
	/** our data */
	private String mString;
	private String mEncoding;
	private byte[] mBytes;
	
	/** Creates a new instance of StringAspect
	 * @param aString the value for the aspect
	 */
	public StringAspect(String aString)
	{	this(aString, "UTF-8");
	}

	/** Creates a new instance of StringAspect
	 * @param aString the value for the aspect
	 * @param aEncoding the prefered character encoding for writing
	 */
	public StringAspect(String aString, String aEncoding)
	{	mString=aString;
		mEncoding=aEncoding;
	}
	
	/** Return the string */
	public String getString()
	{	return mString;
	}
	
	/** Write state to OutputStream
	 * @param aStream the output stream
	 * @exception IOException thrown if we fail to write
	 */
	public void write(OutputStream aStream) throws IOException
	{	ensureBytes();
		aStream.write(mBytes);
		aStream.flush();
	}
	
	/** Return the length of the stream */
	public int getContentLength()
	{	ensureBytes();
		return mBytes.length;
	}
	
	/** Return an input stream to read the data */
	public InputStream getInputStream() throws IOException
	{	ensureBytes();
		return new ByteArrayInputStream(mBytes);
	}
	
	private void ensureBytes()
	{	if (mBytes==null)
		{	try
			{	mBytes = mString.getBytes(mEncoding);
			} catch (UnsupportedEncodingException e)
			{	throw new IllegalArgumentException("unsupported encoding "+mEncoding);
			}
		}
	}
	
	/** Create an IURRepresentation holding the StringAspect
	 * @param aMeta the meta for the representation
	 * @param aString the value of the string aspect
	 * @deprecated

	 */
	public static IURRepresentation create(IURMeta aMeta, String aString)
	{	return new MonoRepresentationImpl(aMeta, new StringAspect(aString));
	}
	
	/* Get encoding */
	public String getEncoding()
	{	return mEncoding;
	}	
	
	public Reader getReader()
	{	return new StringReader(mString);
	}
	
}
