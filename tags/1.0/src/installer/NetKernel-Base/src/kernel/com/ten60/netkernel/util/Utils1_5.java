/******************************************************************************
  (c) Copyright 2002,2005, 1060 Research Ltd                                   

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

  File:          $RCSfile: Utils1_5.java,v $
  Version:       $Name:  $ $Revision: 1.3 $
  Last Modified: $Date: 2005/09/15 12:53:15 $
 *****************************************************************************/
package com.ten60.netkernel.util;

import java.io.*;
import java.lang.reflect.*;

/** Utility methods for JDK1.5+ only
 * @author tab
 */
public class Utils1_5
{
	public static boolean isSuitable()
	{	return System.getProperty("java.version").startsWith("1.5");
	}
	
	public static void dumpThread(Writer aWriter, Thread aThread) throws IOException
	{	aWriter.write("<dump>");
		XMLUtils.write(aWriter,"thread",XMLUtils.escape(aThread.getName()));
		XMLUtils.write(aWriter,"group",XMLUtils.escape(aThread.getThreadGroup().getName()));
		
		//use reflection so that these JDK1.5 features will build on 1.4!
		try
		{	Method m=Thread.class.getMethod("getState", new Class[0]);
			String state=m.invoke(aThread, new Object[0]).toString();
			XMLUtils.write(aWriter,"state",XMLUtils.escape(state));

			m=Thread.class.getMethod("getStackTrace", new Class[0]);
			StackTraceElement[] ste=(StackTraceElement[])m.invoke(aThread, new Object[0]);
			XMLUtils.writeStack(aWriter,ste,Integer.MAX_VALUE);
		}
		catch (Exception e)
		{	// ignore as probably not on JDK1.5+
		}
		
		aWriter.write("</dump>");
	}
}