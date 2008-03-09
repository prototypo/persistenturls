/******************************************************************************
  (c) Copyright 2002 - $Date: 2005/03/24 11:22:45 $ 1060 Research Ltd

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

  File:          $RCSfile: TransactionWrapperAccessor.java,v $
  Version:       $Name:  $ $Revision: 1.3 $
  Last Modified: $Date: 2005/03/24 11:22:45 $
 *****************************************************************************/

package org.ten60.rdbms.accessor;

import com.ten60.netkernel.urii.IURRepresentation;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.INKFRequestReadOnly;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.impl.NKFAccessorImpl;
import org.ten60.rdbms.representation.IAspectDBConnectionPool;
import org.ten60.rdbms.representation.IAspectTransactedDBConnectionPool;
import java.util.Iterator;

/**
 *	A Wrapper which aquires a transactional database connection for use
 * by a sub-service. This is then commited or rolledback depending upon
 * whether an exception was thrown by the underlying service.
 * @author  tab
 */
public class TransactionWrapperAccessor extends NKFAccessorImpl
{
	public static final String ARG_SERVICE="service";
	public static final String ARG_CONFIGURATION="rdbmsConfig";
	
	/** Creates a new instance of RDBMSAccessorImpl */
	public TransactionWrapperAccessor()
	{	super(0,true,INKFRequestReadOnly.RQT_SOURCE);
	}
	
	public void processRequest(INKFConvenienceHelper context) throws Exception
	{	
		String configURI;
		if (context.getThisRequest().argumentExists(ARG_CONFIGURATION))
		{	configURI= "this:param:"+ARG_CONFIGURATION;
		}
		else
		{	configURI=RDBMSAccessorImpl2.DEFAULT_CONFIG;
		}
		IAspectTransactedDBConnectionPool connectionPool = (IAspectTransactedDBConnectionPool)context.sourceAspect(configURI,IAspectTransactedDBConnectionPool.class);
		IAspectDBConnectionPool cp = connectionPool.getTransactedConnection();
		
		// form subrequest based on this request
		INKFRequest request = context.createSubRequest();
		INKFRequestReadOnly thisReq=context.getThisRequest();
		request.setURI(thisReq.getArgument(ARG_SERVICE));
		for (Iterator i=thisReq.getArguments(); i.hasNext(); )
		{	String name=(String)i.next();
			if (!name.equals(ARG_SERVICE) && !name.equals(ARG_CONFIGURATION))
			{	String uri = thisReq.getArgument(name);
				IURRepresentation rep = thisReq.getArgumentValue(uri);
				if (rep!=null)
				{	request.addArgument(name,rep);
				}
				else
				{	request.addArgument(name,uri);
				}
			}
		}
		request.addArgument(ARG_CONFIGURATION,cp);
		request.setAspectClass(thisReq.getAspectClass());
			
		boolean commit=false;
		try
		{	IURRepresentation result = context.issueSubRequest(request);
			INKFResponse response = context.createResponseFrom(result);
			context.setResponse(response);
			connectionPool.commitConnection(cp);
			commit=true;
		}
		finally
		{	if (!commit)
			{	connectionPool.rollbackConnection(cp);
			}
		}
	}
}