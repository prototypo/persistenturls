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

  File:          $RCSfile: Utils.java,v $
  Version:       $Name:  $ $Revision: 1.5 $
  Last Modified: $Date: 2005/09/06 09:31:37 $
 *****************************************************************************/
package com.ten60.netkernel.util;

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import org.w3c.dom.*;

/** General utility methods
 * @author tab
 */
public class Utils
{
	/** ffcpl light */
	public static URL getResource(String aPath, String aBasePath, ClassLoader aClassLoader)
	{	URL result = null;
		File f=new File(aBasePath,aPath);
		if (f.exists())
		{	try
			{	result = new URL(f.toURI().toString());
			} catch (MalformedURLException e)
			{ // just not found
			}
		}
		else
		{	if (aPath.charAt(0)=='/')
			{	aPath = aPath.substring(1);
			}
			result = aClassLoader.getResource(aPath);

		}
		return result;
	}
	
	/** copy an input stream to an outputsteam and close streams when finished
	 * @throws IOException if there are any problems
	 */
	public static void pipe(InputStream aInput, OutputStream aOutput) throws IOException
	{	byte b[] = new byte[256];
		int c;
		try
		{	while ( (c=aInput.read(b))>0 )
			{	aOutput.write(b,0,c);
			}
		}
		finally
		{	try
			{	aInput.close();
			}
			finally
			{	aOutput.close();
			}
		}
	}
	
	/** ensure windows style slashes become URI style slashes */
	public static String fixSlash(String aURI)
	{	return aURI.replace('\\', '/');
	}
	
	public static void appendUnreservedURIChar(StringBuffer sb, String aURI)
	{	int length=aURI.length();
		for (int i=0; i<length; i++)
		{	char c=aURI.charAt(i);
			if (c=='/'||c=='-'||c=='.'||c=='_'||c=='~'||Character.isLetterOrDigit(c))
			{	sb.append(c);
			}
			else
			{	sb.append('%');
				sb.append(Integer.toHexString(c));
			}
		}
	}
	
	public static String decode(String aInput)
	{	StringBuffer sb=new StringBuffer(aInput.length());
		int length = aInput.length();
		for (int i=0; i<length; i++)
		{	char c = aInput.charAt(i);
			if (c=='%')
			{	int c1 = toHex(aInput.charAt(++i));
				int c2 = toHex(aInput.charAt(++i));
				c = (char)((c1<<4) + c2);
			}
			sb.append(c);
		}
		return new String(sb);
	}
	
	
	private static int toHex(char aChar)
	{	int result=0;
		if (aChar<='9' && aChar>='0')
		{	result = aChar-'0';
		}
		else if (aChar<='Z' && aChar>='A')
		{	result= aChar-('A'-10);
		}
		return result;
	}
	
	public static Object constructFromXML(XMLReadable aConfig) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
	{	String classname=null;
		List nodes=aConfig.getNodes("*");
		Class[] classes=new Class[nodes.size()-1];
		Object[] args=new Object[nodes.size()-1];
		int j=0;
		for (int i=0; i<nodes.size(); i++)
		{	Node n=(Node)nodes.get(i);
			String name=n.getNodeName();
			String value=XMLUtils.getText(n);
			if (name.equals("class"))
			{	classname=value;
			}
			else
			{	if (name.equals("string"))
				{	classes[j]=String.class;
					args[j]=value;
				}
				else if (name.equals("int"))
				{	classes[j]=int.class;
					args[j]=new Integer(value);
				}
				else if (name.equals("long"))
				{	classes[j]=long.class;
					args[j]=new Long(value);
				}
				else if (name.equals("boolean"))
				{	classes[j]=boolean.class;
					args[j]=Boolean.valueOf(value);
				}
				else
				{	throw new IllegalArgumentException("Unknown constructor type "+name);
				}
				j++;
			}
		}
		if (classname==null || j!=nodes.size()-1)
		{	throw new IllegalArgumentException("Class must be specified once");
		}
		Class c = Class.forName(classname);
		Constructor cs=c.getConstructor(classes);
		Object instance=cs.newInstance(args);
		return instance;
	}
	
}
