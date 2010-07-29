/******************************************************************************
  (c) Copyright 2002-2005, 1060 Research Ltd                                   

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

  File:          $RCSfile: HttpResponseCodeFilterAccessor.java,v $
  Version:       $Name:  $ $Revision: 1.1 $
  Last Modified: $Date: 2005/09/02 09:41:28 $
 *****************************************************************************/

package org.ten60.netkernel.httpclient.accessor;

import org.ten60.netkernel.layer1.nkf.*;
import org.ten60.netkernel.layer1.nkf.impl.*;
import org.ten60.netkernel.httpclient.representation.*;
import com.ten60.netkernel.util.NetKernelException;
import com.ten60.netkernel.urii.*;

/**
 * A simple filter to examine an HttpResponseCodeAspect. Anything that is not a 200 response code is thrown as a NetKernel Exception.
 * If the representation does not have an HttpRespnseCodeAspect or the response code is 200 then the resource is returned.
 * @author  pjr
 */
public class HttpResponseCodeFilterAccessor extends NKFAccessorImpl
{
	
	/** Creates a new instance of ThrowHttpResponseAsExceptionAccessor */
	public HttpResponseCodeFilterAccessor()
	{ super(2, true, INKFRequestReadOnly.RQT_SOURCE);
	}
	
	public void processRequest(INKFConvenienceHelper context) throws Exception
	{	IURRepresentation rep=context.source("this:param:operand");
		if(rep.hasAspect(HttpResponseCodeAspect.class))
		{	HttpResponseCodeAspect asp=(HttpResponseCodeAspect)rep.getAspect(HttpResponseCodeAspect.class);
			if(asp.getCode()!=200)
			{	throw new NetKernelException("HttpResponseCode: "+asp.getCode(), asp.getMessage(),null);
			}
		}
		INKFResponse resp=context.createResponseFrom(rep);
		context.setResponse(resp);
	}
}
