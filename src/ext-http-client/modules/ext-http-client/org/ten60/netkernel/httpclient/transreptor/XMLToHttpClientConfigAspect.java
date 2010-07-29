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

  File:          $RCSfile: XMLToHttpClientConfigAspect.java,v $
  Version:       $Name:  $ $Revision: 1.5 $
  Last Modified: $Date: 2007/03/21 09:57:38 $
 *****************************************************************************/

package org.ten60.netkernel.httpclient.transreptor;

import org.ten60.netkernel.layer1.nkf.*;
import org.ten60.netkernel.layer1.nkf.impl.*;
import com.ten60.netkernel.urii.aspect.*;
import com.ten60.netkernel.urii.*;
import org.ten60.netkernel.layer1.representation.*;
import org.ten60.netkernel.xml.representation.*;
import org.ten60.netkernel.xml.xda.*;
import org.ten60.netkernel.httpclient.representation.*;
import org.apache.commons.httpclient.*;

/**
 * Transrept XML Config to a Config Aspect
 * @author  pjr
 */
public class XMLToHttpClientConfigAspect extends NKFTransreptorImpl
{
	/** Creates a new instance of XMLtoHttpCredentialsAspect */
	public XMLToHttpClientConfigAspect() {
	}
	
	public boolean supports(com.ten60.netkernel.urii.IURRepresentation aFrom, Class aTo)
	{	return IAspectHttpClientConfig.class.isAssignableFrom(aTo);
	}
	
	protected void transrepresent(INKFConvenienceHelper context) throws Exception
	{	IURAspect config=(IURAspect)context.sourceAspect(INKFRequestReadOnly.URI_SYSTEM, IURAspect.class);
		INKFRequest req=context.createSubRequest();
		req.setURI("active:xslt");
		req.addArgument("operand",config);
		req.addArgument("operator", "ffcpl:/org/ten60/netkernel/httpclient/transreptor/MergeConfig.xsl");
		req.addArgument("default", "ffcpl:/etc/DefaultHTTPClientConfig.xml");
		req.setAspectClass(IXAspect.class);
		IURRepresentation result=context.issueSubRequest(req);
		IXAspect xa=(IXAspect)result.getAspect(IXAspect.class);
		IXDAReadOnly xda=xa.getXDA();
		
		boolean followRedirects=true;
		long acceptableContentLength=-1;
		int connectionsPerHost=4;
		int totalConnections=10;
		int stateExpirationTime=600;
		int retryAttempts=3;
		int connectTimeout=2000;
		int timeout=5000;
		String proxyHost=System.getProperty("proxyHost");
		int proxyPort=80;
		String sproxyPort=System.getProperty("proxyPort");
		if(sproxyPort!=null)
		{	proxyPort=Integer.parseInt(sproxyPort);
		}
		
		if(xda.isTrue("/config/followRedirects"))
		{	String temp=xda.getText("/config/followRedirects",true);
			followRedirects=temp.startsWith("t") || temp.equals("1");
		}
		if(xda.isTrue("/config/retryAttempts"))
		{	String temp=xda.getText("/config/retryAttempts",true);
			retryAttempts=Integer.parseInt(temp);
		}
		if(xda.isTrue("/config/maxConnectionsPerHost"))
		{	String temp=xda.getText("/config/maxConnectionsPerHost",true);
			connectionsPerHost=Integer.parseInt(temp);
		}
		if(xda.isTrue("/config/maxTotalConnections"))
		{	String temp=xda.getText("/config/maxTotalConnections",true);
			totalConnections=Integer.parseInt(temp);
		}
		if(xda.isTrue("/config/maxAcceptableContentLength"))
		{	String temp=xda.getText("/config/maxAcceptableContentLength",true);
			acceptableContentLength=Long.parseLong(temp);
		}
		if(xda.isTrue("/config/stateExpirationTime"))
		{	String temp=xda.getText("/config/stateExpirationTime",true);
			stateExpirationTime=Integer.parseInt(temp);
		}
		if(xda.isTrue("/config/connectTimeout"))
		{	String temp=xda.getText("/config/connectTimeout",true);
			connectTimeout=Integer.parseInt(temp);
		}
		if(xda.isTrue("/config/timeout"))
		{	String temp=xda.getText("/config/timeout",true);
			timeout=Integer.parseInt(temp);
		}
		if(xda.isTrue("/config/proxyHost"))
		{	proxyHost=xda.getText("/config/proxyHost",true);
		}
		if(xda.isTrue("/config/proxyPort"))
		{	String temp=xda.getText("/config/proxyPort",true);
			proxyPort=Integer.parseInt(temp);
		}
		
		
		HttpClientConfigAspect ca=new HttpClientConfigAspect(
			followRedirects,
			acceptableContentLength,
			connectionsPerHost,
			totalConnections,
			stateExpirationTime,
			retryAttempts,
			connectTimeout,
			timeout,
			proxyHost, 
			proxyPort
			);
		
		INKFResponse resp=context.createResponseFrom(ca);
		context.setResponse(resp);
		
	}
	
}
