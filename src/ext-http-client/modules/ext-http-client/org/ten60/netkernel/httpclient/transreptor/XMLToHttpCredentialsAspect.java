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

  File:          $RCSfile: XMLToHttpCredentialsAspect.java,v $
  Version:       $Name:  $ $Revision: 1.6 $
  Last Modified: $Date: 2007/09/18 11:47:15 $
 *****************************************************************************/

package org.ten60.netkernel.httpclient.transreptor;

import org.ten60.netkernel.layer1.nkf.*;
import org.ten60.netkernel.layer1.nkf.impl.*;
import com.ten60.netkernel.urii.aspect.*;
import org.ten60.netkernel.layer1.representation.*;
import org.ten60.netkernel.xml.representation.*;
import org.ten60.netkernel.xml.xda.*;
import org.ten60.netkernel.httpclient.representation.*;
import org.apache.commons.httpclient.*;

/**
 *
 * @author  pjr
 */
public class XMLToHttpCredentialsAspect extends NKFTransreptorImpl
{
	/** Creates a new instance of XMLtoHttpCredentialsAspect */
	public XMLToHttpCredentialsAspect() {
	}
	
	public boolean supports(com.ten60.netkernel.urii.IURRepresentation aFrom, Class aTo)
	{	return IAspectHttpCredentials.class.isAssignableFrom(aTo);
	}
	
	protected void transrepresent(INKFConvenienceHelper context) throws Exception
	{	IXAspect xa=(IXAspect)context.sourceAspect(INKFRequestReadOnly.URI_SYSTEM, IXAspect.class);
		IXDAReadOnly xda=xa.getXDA();
		String realm=null;
		if(xda.isTrue("/httpCredentials/realm"))
		{	realm=xda.getText("/httpCredentials/realm",true);
		}
		String host=xda.getText("/httpCredentials/host",true);
		String username=xda.getText("/httpCredentials/username",true);
		String password=xda.getText("/httpCredentials/password",true);
		int port=80;
		if(xda.isTrue("/httpCredentials/port"))
		{	port=Integer.parseInt(xda.getText("/httpCredentials/port",true));			
		}
		String nthost=null;
		if(xda.isTrue("/httpCredentials/NTRequestHost"))
		{	nthost=xda.getText("/httpCredentials/NTRequestHost",true);
		}
		String ntdomain=null;
		if(xda.isTrue("/httpCredentials/NTDomain"))
		{	ntdomain=xda.getText("/httpCredentials/NTDomain",true);
		}
		Credentials cred=null;
		if(ntdomain!=null && ntdomain!=null)
		{	cred=new NTCredentials(username, password, nthost, ntdomain);
		}
		else
		{	cred=new UsernamePasswordCredentials(username, password);
		}
		HttpCredentialsAspect ca=new HttpCredentialsAspect(realm, host, port, cred);
		
		INKFResponse resp=context.createResponseFrom(ca);
		context.setResponse(resp);
		
	}
	
}
