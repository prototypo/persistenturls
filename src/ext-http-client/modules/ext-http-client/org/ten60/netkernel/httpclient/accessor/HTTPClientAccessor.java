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

  File:          $RCSfile: HTTPClientAccessor.java,v $
  Version:       $Name:  $ $Revision: 1.23 $
  Last Modified: $Date: 2007/09/18 11:47:15 $
 *****************************************************************************/
/******************************************************************************
  Modified by David Wood (david at http://zepheira.com) to implement support
  for the HTTP HEAD operation.
  Last Modified: 2009/03/04
 *****************************************************************************/

package org.ten60.netkernel.httpclient.accessor;

import org.ten60.netkernel.layer1.nkf.*;
import org.ten60.netkernel.layer1.nkf.impl.*;
import com.ten60.netkernel.util.*;
import com.ten60.netkernel.urii.*;
import com.ten60.netkernel.urii.aspect.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.util.*;
import java.util.*;
import org.ten60.netkernel.httpclient.util.*;
import org.ten60.netkernel.httpclient.representation.*;
import org.ten60.netkernel.layer1.representation.*;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;
import org.ten60.netkernel.xml.representation.IAspectXDA;

import java.io.*;

/**
 *  An HTTP client accessor.
 * @author  pjr
 */
public class HTTPClientAccessor extends NKFAccessorImpl
{
	private MultiThreadedHttpConnectionManager mManager;
	private static final String ACTIVE_HTTP_STATE="active:httpState";
	private static final String NETKERNEL_USER_AGENT="1060 NetKernel HTTP Client 0.9.0  http://www.1060research.com";
	private static final String HTTP_HEADER_CONTENT_LENGTH="Content-Length";
	
	private static final String HTTP_METHOD_GET="httpGet";
	private static final int HTTP_METHOD_GET_INT=1;
	private static final String HTTP_METHOD_PUT="httpPut";
	private static final int HTTP_METHOD_PUT_INT=2;
	private static final String HTTP_METHOD_POST="httpPost";
	private static final int HTTP_METHOD_POST_INT=3;
	private static final String HTTP_METHOD_HEAD="httpHead";
	private static final int HTTP_METHOD_HEAD_INT=4;
	private static final String HTTP_METHOD_DELETE="httpDelete";
	private static final int HTTP_METHOD_DELETE_INT=5;
	
	private static final String CONTENT_TYPE_HEADER="Content-Type";
	private static final String EXPIRES_HEADER="Expires";
	
	private static final HashMap mModes=new HashMap();
	{
		mModes.put(HTTP_METHOD_GET, new Integer(HTTP_METHOD_GET_INT));
		mModes.put(HTTP_METHOD_PUT, new Integer(HTTP_METHOD_PUT_INT));		
		mModes.put(HTTP_METHOD_POST, new Integer(HTTP_METHOD_POST_INT));
		mModes.put(HTTP_METHOD_HEAD, new Integer(HTTP_METHOD_HEAD_INT));
		mModes.put(HTTP_METHOD_DELETE, new Integer(HTTP_METHOD_DELETE_INT));
	}
	
	/** Creates a new instance of HTTPClientAccessor */
	public HTTPClientAccessor()
	{	super(
			8,
			true,
			INKFRequestReadOnly.RQT_SOURCE
		);
		System.getProperties().setProperty("httpclient.useragent", NETKERNEL_USER_AGENT);
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
		mManager=new MultiThreadedHttpConnectionManager();
	}
	
	public void processRequest(INKFConvenienceHelper context) throws Exception
	{	IURAspect result=null;
		HttpState state=null;
		Object stateref=null;
		long expires=0;
		String mimetype=null;
		String url=context.getThisRequest().getArgument("url");

        boolean wantsResponseCode=context.getThisRequest().getAspectClass().equals(HttpResponseCodeAspect.class);
		
		//Gonfiguration
		String configuri=null;
		if(context.getThisRequest().argumentExists("config"))
		{	configuri=context.getThisRequest().getArgument("config");
		}
		else if (context.exists("ffcpl:/etc/HTTPClientConfig.xml"))
		{	configuri="ffcpl:/etc/HTTPClientConfig.xml";
		}
		else
		{	configuri="ffcpl:/etc/DefaultHTTPClientConfig.xml";
		}
		IAspectHttpClientConfig cfg=(IAspectHttpClientConfig)context.sourceAspect(configuri, IAspectHttpClientConfig.class);
		//mManager.setMaxConnectionsPerHost(cfg.maxConnectionsPerHost());
		mManager.getParams().setDefaultMaxConnectionsPerHost(cfg.maxConnectionsPerHost());
		//mManager.setMaxTotalConnections(cfg.maxTotalConnections());
		mManager.getParams().setMaxTotalConnections(cfg.maxTotalConnections());
		
		//Retry handler
		DefaultMethodRetryHandler retryhandler = new DefaultMethodRetryHandler();
		retryhandler.setRequestSentRetryEnabled(false);
		retryhandler.setRetryCount(cfg.retryAttempts());
		
		//Get state if there is any
		String stateuri=context.getThisRequest().getArgument("state");
		
		if(stateuri!=null)
		{	if(!stateuri.startsWith(HTTPStateAccessor.ACTIVE_HTTP_STATE))
			{	IAspectURI ua=(IAspectURI)context.sourceAspect("this:param:state", IAspectURI.class);
				stateuri=ua.getURI().toString();
			}
			IAspectHttpState sa=(IAspectHttpState)context.sourceAspect(stateuri, IAspectHttpState.class);
			state=sa.getState();
			stateref=state.getCookies();
		}
		Integer mode=(Integer)mModes.get(context.getThisRequest().getActiveType());
		switch (mode.intValue())
		{	case HTTP_METHOD_GET_INT:
				GetMethod get=new GetMethod(url);
				get.setMethodRetryHandler(retryhandler);
				get.setFollowRedirects(cfg.followRedirects());
				try
				{	HttpClient client=getHttpClient(state,cfg);
                    int statusCode=client.executeMethod(get);
					if(statusCode !=HttpStatus.SC_OK || wantsResponseCode)
					{	String body=getResponseString(get);
						result=new HttpResponseCodeAspect(statusCode, body);
					}
					else
					{	result=getResponse(get);	
					}
					mimetype=getContentType(get.getResponseHeader(CONTENT_TYPE_HEADER));
					expires=getExpires(get.getResponseHeader(EXPIRES_HEADER));
				}
				finally
				{	get.releaseConnection();						
				}
			break;
			case HTTP_METHOD_POST_INT:
				PostMethod post=new PostMethod(url);
				post.setMethodRetryHandler(retryhandler);
				//post.setFollowRedirects(cfg.followRedirects());  Entity enclosing methods cannot follow redirects without user intervention
				if(context.getThisRequest().argumentExists("nvp"))
				{	IAspectNVP nvpa=(IAspectNVP)context.sourceAspect("this:param:nvp",IAspectNVP.class);
					Set n=nvpa.getNames();
					Iterator it=n.iterator();
					while(it.hasNext())
					{	String name=(String)it.next();
						List values=nvpa.getValues(name);
						Iterator it2=values.iterator();
						while(it2.hasNext())
						{	String value=(String)it2.next();
							post.addParameter(name, value);
						}
					}
				}
				else
				{	Iterator it=context.getThisRequest().getArguments();
					//Currently we're only supporting single argument - can have any name.
					while(it.hasNext())
					{	String arg=(String)it.next();
						if(!arg.equals("url") && !arg.equals("state"))
						{	IURRepresentation rep=context.source("this:param:"+arg);
							InputStream is=null;
							IAspectReadableBinaryStream rbsa=null;
							if(rep.hasAspect(IAspectReadableBinaryStream.class))
							{	rbsa=(IAspectReadableBinaryStream)rep.getAspect(IAspectReadableBinaryStream.class);
							}
							else
							{	rbsa=(IAspectReadableBinaryStream)context.transrept(rep.getAspect(IURAspect.class), IAspectReadableBinaryStream.class);
							}
							//post.setRequestBody(rbsa.getInputStream());
							//post.setRequestContentLength(rbsa.getContentLength());
							//post.setRequestHeader(CONTENT_TYPE_HEADER, rep.getMeta().getMimeType());
							post.setRequestEntity(new InputStreamRequestEntity(rbsa.getInputStream(), rbsa.getContentLength(), rep.getMeta().getMimeType()));
							break;
						}
					}
				}
				try
				{	HttpClient client=getHttpClient(state,cfg);
                    int statusCode=client.executeMethod(post);
					if(statusCode !=HttpStatus.SC_OK || wantsResponseCode)
					{	String body=getResponseString(post);
						result=new HttpResponseCodeAspect(statusCode, body);
					}
					else
					{	result=getResponse(post);	
					}
					mimetype=getContentType(post.getResponseHeader(CONTENT_TYPE_HEADER));
					expires=getExpires(post.getResponseHeader(EXPIRES_HEADER));
				}
				finally
				{	post.releaseConnection();						
				}
			break;
			case HTTP_METHOD_PUT_INT:
				PutMethod put=new PutMethod(url);
				put.setMethodRetryHandler(retryhandler);
				//put.setFollowRedirects(cfg.followRedirects());  Entity enclosing methods cannot follow redirects without user intervention
				Iterator it=context.getThisRequest().getArguments();
				//Currently we're only supporting single argument - can have any name.
				while(it.hasNext())
				{	String arg=(String)it.next();
					if(!arg.equals("url") && !arg.equals("state"))
					{	IURRepresentation rep=context.source("this:param:"+arg);
						InputStream is=null;
						IAspectReadableBinaryStream rbsa=null;
						if(rep.hasAspect(IAspectReadableBinaryStream.class))
						{	rbsa=(IAspectReadableBinaryStream)rep.getAspect(IAspectReadableBinaryStream.class);
						}
						else
						{	rbsa=(IAspectReadableBinaryStream)context.transrept(rep.getAspect(IURAspect.class), IAspectReadableBinaryStream.class);
						}
						//put.setRequestBody(rbsa.getInputStream());
						//put.setRequestContentLength(rbsa.getContentLength());
						//put.setRequestHeader(CONTENT_TYPE_HEADER, rep.getMeta().getMimeType());
						put.setRequestEntity(new InputStreamRequestEntity(rbsa.getInputStream(), rbsa.getContentLength(), rep.getMeta().getMimeType()));
						break;
					}
				}
				try
				{	HttpClient client=getHttpClient(state,cfg);
                    int statusCode=client.executeMethod(put);
					if(statusCode !=HttpStatus.SC_OK)
					{	String body=getResponseString(put);
						result=new HttpResponseCodeAspect(statusCode, body);
					}
					else
					{	result=new org.ten60.netkernel.layer1.representation.VoidAspect();
					}
					mimetype="application/void";
					expires=-1;
				}
				finally
				{	put.releaseConnection();						
				}
			break;
			case HTTP_METHOD_HEAD_INT:
				HeadMethod head=new HeadMethod(url);
				head.setMethodRetryHandler(retryhandler);
				head.setFollowRedirects(cfg.followRedirects());
				try
				{	HttpClient client=getHttpClient(state,cfg);
                    int statusCode=client.executeMethod(head);
					result=new HttpResponseCodeAspect(statusCode, null);
					mimetype=getContentType(head.getResponseHeader(CONTENT_TYPE_HEADER));
					expires=getExpires(head.getResponseHeader(EXPIRES_HEADER));
				}
				finally
				{	head.releaseConnection();						
				}
			break;
			case HTTP_METHOD_DELETE_INT:
				DeleteMethod delete=new DeleteMethod(url);
				delete.setMethodRetryHandler(retryhandler);
				delete.setFollowRedirects(cfg.followRedirects());
				try
				{	HttpClient client=getHttpClient(state,cfg);
                    int statusCode=client.executeMethod(delete);
					if(statusCode !=HttpStatus.SC_OK)
					{	String body=getResponseString(delete);
						result=new HttpResponseCodeAspect(statusCode, body);
					}
					else
					{	result=new org.ten60.netkernel.layer1.representation.VoidAspect();
					}
					mimetype="application/void";
					expires=-1;
				}
				finally
				{	delete.releaseConnection();						
				}
				
			break;
		}
		
		//Sink updated state
		if(state!=null && !state.getCookies().equals(stateref))
		{	HttpStateAspect sa=new HttpStateAspect(state);
			context.sinkAspect(stateuri, sa);
		}
		
		INKFResponse resp= context.createResponseFrom(result);
		resp.setMimeType(mimetype);
		if(expires>0)
		{	resp.setCacheable();
			resp.setExpiryPeriod(expires);
		}
		resp.setCreationCost(64);
		
		context.setResponse(resp);
	}
	
	public void destroy()
	{	mManager.shutdownAll();
	}
	
	private ByteArrayAspect getResponse(HttpMethodBase base) throws Exception
	{	return new ByteArrayAspect(getResponseInner(base).toByteArray());
	}
	
	private String getResponseString(HttpMethodBase base) throws Exception
	{	String result=null;
		try
		{	String encoding=base.getRequestCharSet();
			if (encoding==null)
			{	result=getResponseInner(base).toString();
			}
			else
			{	result=getResponseInner(base).toString(encoding);
			}
		}
		catch(Exception e)
		{	result="";
		}
		return result;
	}
	
	private ByteArrayOutputStream getResponseInner(HttpMethodBase base) throws Exception
	{	int size;
		try
		{	Header h=base.getRequestHeader(HTTP_HEADER_CONTENT_LENGTH);
			size=Integer.parseInt(h.getValue());
		}
		catch(Exception e)
		{	//default to 1024 if no length specified
			size=1024;						
		}
		ByteArrayOutputStream baos=new ByteArrayOutputStream(size);
		com.ten60.netkernel.util.Utils.pipe(
			base.getResponseBodyAsStream(),
			new BufferedOutputStream(baos)
		);
		return baos;
	}

	private String getContentType(Header h)
	{	String result=null;
		if(h!=null)
		{	String val=h.getValue();
			int i=val.indexOf(';');
			if(i>0)	result=val.substring(0,i);
			else result=val;
		}
		else result="content/unknown";
		return result;
	}
	
	private long getExpires(Header h)
	{	long result=-1;
		try
		{	if(h!=null)
			{	Date d2=DateUtil.parseDate( h.getValue());
				result=d2.getTime()-System.currentTimeMillis();
			}
		}
		catch(DateParseException e){/*Must be expired*/}
		return result;
	}
	
	private HttpClient getHttpClient(HttpState state, IAspectHttpClientConfig cfg)
	{	HttpClient client=new HttpClient(mManager);
		if(state!=null) client.setState(state);
		client.setConnectionTimeout(cfg.getConnectTimeout());
		client.setTimeout(cfg.getTimeout());
		String proxyHost=cfg.getProxyHost();
		if(proxyHost!=null)
		{	client.getHostConfiguration().setProxy( proxyHost, cfg.getProxyPort() );
		}
		return client;
	}
	
}
