/******************************************************************************
  (c) Copyright 2002,2003, 1060 Research Ltd                                   

  This Software is licensed to You, the licensee, for use under the terms of   
  the 1060 Public License v1.0. Please read and agree to the 1060 Public       
  License v1.0 [www.1060research.com/license] before using or redistributing   
  this software.                                                               

  In summary the 1060 Public license has the following conditions.             
  A. You may use the Software free of charge provided you agree to the terms   
  laid out in the 1060 Public License v1.0                                     
  B. You are only permitted to use the Software with components or applications
  that provide you with OSI Certified Open Source Code [www.opensource.org], or
  for which licensing has been approved by 1060 Research Limited.              
  You may write your own software for execution by this Software provided any  
  distribution of your software with this Software complies with terms set out 
  in section 2 of the 1060 Public License v1.0                                 
  C. You may redistribute the Software provided you comply with the terms of   
  the 1060 Public License v1.0 and that no warranty is implied or given.       
  D. If you find you are unable to comply with this license you may seek to    
  obtain an alternative license from 1060 Research Limited by contacting       
  license@1060research.com or by visiting www.1060research.com                 

  NO WARRANTY:  THIS SOFTWARE IS NOT COVERED BY ANY WARRANTY. SEE 1060 PUBLIC  
  LICENSE V1.0 FOR DETAILS                                                     

  THIS COPYRIGHT NOTICE IS *NOT* THE 1060 PUBLIC LICENSE v1.0. PLEASE READ     
  THE DISTRIBUTED 1060_Public_License.txt OR www.1060research.com/license      

  File:          $RCSfile: NetKernelURLConnection.java,v $
  Version:       $Name:  $ $Revision: 1.2 $
  Last Modified: $Date: 2004/10/21 13:09:05 $
 *****************************************************************************/
package com.ten60.netkernel.util;

import com.ten60.netkernel.module.*;
import com.ten60.netkernel.container.*;
import com.ten60.netkernel.scheduler.*;
import com.ten60.netkernel.urii.*;
import com.ten60.netkernel.urii.aspect.*;
import com.ten60.netkernel.urrequest.*;
import com.ten60.netkernel.util.*;
import java.net.*;
import java.io.*;
/**
 * A URLConnection to a netkernel: scheme resource - uses internal transport
 * pretty basic implementation- doesn't do mime type or other such metadata
 * @author  tab
 */
public class NetKernelURLConnection extends java.net.URLConnection
{
	private IURRepresentation mRep;
	
	/** Creates a new instance of Handler */
	public NetKernelURLConnection(URL aURL, Container aContainer) throws IOException
	{	super(aURL);
		try
		{	URIdentifier uri = new URIdentifier(aURL.getPath());
			mRep = aContainer.requestResource(uri, IAspectBinaryStream.class, null);
			if (mRep.hasAspect(IAspectNetKernelException.class))
			{	throw (Exception)((IAspectNetKernelException)mRep.getAspect(IAspectNetKernelException.class)).getXMLException();
			}
		} catch (Exception e)
		{	IOException e2 = new IOException("resource not found");
			e2.initCause(e);
			System.out.println(e.toString());
		}
	}
	
	public void setRequestHeader(String name, String value)
	{
	}
	
	public InputStream getInputStream() throws IOException
	{	if (mRep!=null)
		{	if (mRep.hasAspect(IAspectReadableBinaryStream.class))
			{	return ((IAspectReadableBinaryStream)mRep.getAspect(IAspectReadableBinaryStream.class)).getInputStream();
			}
			else
			{	IAspectBinaryStream bs = (IAspectBinaryStream)mRep.getAspect(IAspectBinaryStream.class);
				ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
				bs.write(baos);
				baos.flush();
				return new ByteArrayInputStream(baos.toByteArray());
			}
		}
		else
		{	throw  new IOException("resource not found");
		}
	}

	public void connect() throws IOException
	{	
	}	
}