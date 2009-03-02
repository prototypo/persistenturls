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
 * File:          $RCSfile: IEmbeddedAPI.java,v $
 * Version:       $Name:  $ $Revision: 1.1 $
 * Last Modified: $Date: 2004/06/18 10:20:38 $
 *****************************************************************************/
package com.ten60.netkernel.embedded;

import com.ten60.netkernel.util.SysLogger;
import com.ten60.netkernel.urii.IURRepresentation;
import org.w3c.dom.Document;
import java.net.URI;
/**
 * The embedded coprocessor API
 * @author  tab
 */
public interface IEmbeddedAPI
{
	/** log level for  debug */
	public static final int LOG_LEVEL_DEBUG = SysLogger.DEBUG;
	/** log level for  fine */
	public static final int LOG_LEVEL_FINE = SysLogger.FINE;
	/** log level for info */
	public static final int LOG_LEVEL_INFO = SysLogger.INFO;
	/** log level for container */
	public static final int LOG_LEVEL_CONTAINER = SysLogger.CONTAINER;
	/** log level for warnings */
	public static final int LOG_LEVEL_WARNING = SysLogger.WARNING;
	/** log level for severe errors */
	public static final int LOG_LEVEL_SEVERE = SysLogger.SEVERE;
	/** log level for cache */
	public static final int LOG_LEVEL_CACHE = SysLogger.CACHE;
	/** log level for application */
	public static final int LOG_LEVEL_APPLICATION = SysLogger.APPLICATION;
	
	/** Starts the instance of the NetKernel
	 * @exception EmbeddedException thrown if there is a failure of any kind during startup
	 */
	void start() throws EmbeddedException;
	/** Stops the instance of the NetKernel
	 * @exception EmbeddedException thrown if there is a failure of any kind during shutdown
	 */
	void stop() throws EmbeddedException;
	/** Checks whether the NetKernel instance is running
	 * @return true if the instance is started, false if not
	 */
	boolean isStarted();
	/** Request the sourcing of a resource with the given URI and arguments passed by value
	 * @param aURI the URI of the resource to request
	 * @param aResponseClass the class of aspect to return.
	 * @param aArgs The set of any pass-by-value arguments to the request, may be null
	 * @exception EmbeddedException thrown if we fail to process the request for any
	 * reason including the instance not being started and the URI not being resolvable.
	 * @return an IURAspect that implements the response class
	 */
	Object requestResource(URI aURI, Class aResponseClass,RequestArgs aArgs) throws EmbeddedException;
	IURRepresentation requestRepresentation(URI aURI, Class aResponseClass,RequestArgs aArgs) throws EmbeddedException;
	/** Set the logging level of the NetKernel instance
	 * @param aLevel one of the constant logging levels defined (LOG_LEVEL_*)
	 * @param aEnabled true or false to enable or disable logging
	 */
	void setLogging(int aLevel, boolean aEnabled);
	/** Return the classloader for the context that the requests are made to.
	 * This provides a mechanism for getting classes of aspect that requests
	 * might return when they are not in the regular classpath.
	 */
	ClassLoader getClassloader() throws EmbeddedException;
}