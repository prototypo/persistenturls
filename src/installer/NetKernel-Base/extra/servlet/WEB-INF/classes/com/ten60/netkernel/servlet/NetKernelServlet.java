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
 * File:          $RCSfile: NetKernelServlet.java,v $
 * Version:       $Name:  $ $Revision: 1.3 $
 * Last Modified: $Date: 2005/09/30 15:12:31 $
 *****************************************************************************/
package com.ten60.netkernel.servlet;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.ten60.netkernel.embedded.*;
import com.ten60.netkernel.urii.*;
import com.ten60.netkernel.urii.meta.MetaImpl;
import com.ten60.netkernel.urii.aspect.IAspectBinaryStream;
import com.ten60.netkernel.urii.aspect.StringAspect;
import com.ten60.netkernel.urii.representation.MonoRepresentationImpl;
import com.ten60.netkernel.util.XMLUtils;

import java.lang.reflect.*;

/**
 * Servlet wrapper for NetKernel. Kernel is started and stopped by the servlet and requests are passed through to the
 * Kernel on servlet: scheme with an argument by value of the HTTPServletRequest
 * @author  tab
 * @version
 */
public class NetKernelServlet extends HttpServlet
{
	IEmbeddedAPI mNetKernel;
	public static final String INIT_BASEPATH="basepath";
	public static final String INIT_NKCONFIG="config";
	public static final String URI_BASE="servlet://";
	public static final String ARG_HTTP_SERVLET_REQUEST="servletRequestResponse";
	public static final URIdentifier URI_HTTP_SERVLET_REQUEST=new URIdentifier("literal:servletRequestResponse");

	/** Initializes the servlet by starting the NetKernel instance
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		
		String basepath = config.getInitParameter(INIT_BASEPATH);
		String nkConfig = config.getInitParameter(INIT_NKCONFIG);
		System.out.println("starting NetKernel servlet with parameters "+basepath+" "+nkConfig);
		if (nkConfig!=null)
		{	mNetKernel = EmbeddedAPIFactory.create(basepath,nkConfig);
		}
		else
		{	mNetKernel = EmbeddedAPIFactory.create(basepath);
		}
		try
		{	mNetKernel.start();
		} catch (EmbeddedException e)
		{	System.out.println("Failed to start NetKernel Servlet");
			System.out.println(e.getReason().toString());
			ServletException e2 = new ServletException("Failed to start NetKernel",e);
			throw e2;
		}
	}
	
	/** Destroys the servlet by shutting down NetKernel instance.
	 */
	public void destroy()
	{
		if (mNetKernel!=null && mNetKernel.isStarted())
		{	try
			{	mNetKernel.stop();
			} catch (EmbeddedException e)
			{	StringWriter sw=new StringWriter(1024);
				try
				{	e.appendXML(sw);
					sw.flush();
					System.err.println(sw);
				} catch (IOException e2) {;}
			}
		}

	}
	
	/** Handles the all HTTP methods.
	 * @param request servlet request
	 * @param response servlet response
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String name = request.getServerName();
		// pass servlet request as an argument to request
		RequestArgs args = new RequestArgs();
		
		try
		{		
			Class cls = mNetKernel.getClassloader().loadClass("org.ten60.transport.servlet.HttpServletRequestResponseAspect");
			Constructor cs[] = cls.getConstructors();
			for (int i=0; i<cs.length; i++)
			{	System.out.println("constructor "+cs[i].toString());
				System.out.println(cs[i].getParameterTypes()[0].getClassLoader());
			}
			Constructor ctr = cls.getConstructor(new Class[] { HttpServletRequest.class, HttpServletResponse.class } );
			IURAspect aspect = (IURAspect)ctr.newInstance(new Object[] { request, response } );

			//HttpServletRequestResponseAspect aspect = new HttpServletRequestResponseAspect(request,response);
			IURMeta meta = new MetaImpl("servlet/HttpServletRequest", 0, 0);
			IURRepresentation rep = new MonoRepresentationImpl(meta, aspect);
			String argURI = URI_HTTP_SERVLET_REQUEST.toString();
			args.put(argURI, rep);
		

			String uriString = URI_BASE+name+request.getRequestURI();
			URI uri = URI.create(uriString);
		
		
			// issue request
			IURRepresentation responseRep = mNetKernel.requestRepresentation(uri, IAspectBinaryStream.class, args);
		}
		catch (EmbeddedException e)
		{	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.setContentType("text/xml");
			Writer w = response.getWriter();
			e.appendXML(w);
			w.flush();
			w.close();
		}
		catch (Exception e)
		{	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.setContentType("text/plain");
			Writer w = response.getWriter();
			w.write(e.toString());
			PrintWriter pw = new PrintWriter(w);
			e.printStackTrace(pw);
			pw.flush();
			w.flush();
			w.close();
		}
	}
	
	/** Returns a short description of the servlet.
	 */
	public String getServletInfo()
	{	return "NetKernel Servlet";
	}	
}