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
 * File:          $RCSfile: ListModules.java,v $
 * Version:       $Name:  $ $Revision: 1.1 $
 * Last Modified: $Date: 2004/06/18 10:20:38 $
 *****************************************************************************/
package com.ten60.netkernel.embedded.examples;

import com.ten60.netkernel.embedded.*;
import com.ten60.netkernel.urii.aspect.IAspectString;
import java.io.*;
import java.lang.reflect.*;
import java.net.URI;
import org.w3c.dom.*;
/**
 * Example to run the system module introspection and output the result to System.out
 * @author  tab
 */
public class ListModules
{
	public static void main(String[] aArgs)
	{	if (aArgs.length<1)
		{	System.err.println("usage: ListModules <basepath>");
		}
		IEmbeddedAPI embedded = EmbeddedAPIFactory.create(aArgs[0]);
		embedded.setLogging(IEmbeddedAPI.LOG_LEVEL_FINE, false);
		embedded.setLogging(IEmbeddedAPI.LOG_LEVEL_INFO, false);
		embedded.setLogging(IEmbeddedAPI.LOG_LEVEL_CONTAINER, true);
		try
		{	embedded.start();
			URI request = URI.create("jmx://remote/introspect/modules");
			EmbeddedToolkit.writeResource(embedded, request, null, System.out);
		}
		catch (EmbeddedException e)
		{	StringWriter sw = new StringWriter(1024);
			try
			{	e.appendXML(sw);
			} catch (IOException e2) {}
			System.err.println(sw.toString());
		}
		finally
		{	try
			{	embedded.stop();
			} catch (EmbeddedException e) { }
		}
	}
}