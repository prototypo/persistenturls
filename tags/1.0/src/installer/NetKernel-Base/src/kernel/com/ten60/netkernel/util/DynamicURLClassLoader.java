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
 * File:          $RCSfile: DynamicURLClassLoader.java,v $
 * Version:       $Name:  $ $Revision: 1.14 $
 * Last Modified: $Date: 2006/02/22 16:31:02 $
 *****************************************************************************/
package com.ten60.netkernel.util;
import java.util.*;
import java.net.*;
import java.util.jar.*;
import java.io.*;

/**
 * DynamicURLClassLoader locates class and resources from a list of URLs before resorting to
 * parent classloader.
 * @author  tab
 */
public class DynamicURLClassLoader extends ClassLoader
{
	private List mURLs;
	
	private static byte[] mBuffer1 = new byte[40000]; // hopefully big enough for the largest class
	private static Thread mBuffer1User = null;
	private static int mBuffer1Count=0;
	
	private static byte[] mBuffer2 = new byte[40000]; // hopefully big enough for the largest class
	private static Thread mBuffer2User = null;
	private static int mBuffer2Count=0;
	
	private static Integer mBufferSync = new Integer(0);
	
	private static class ClassPathElement
	{	public JarFile mJarFile;
		public File mDirectory;
		public String mBaseURL;
		public ClassPathElement(JarFile aJar, String aBaseURL)
		{	mJarFile = aJar;
			mBaseURL = aBaseURL;
		}
		public ClassPathElement(File aDir, String aBaseURL)
		{	mDirectory = aDir;
			mBaseURL = aBaseURL;
		}
	}
	
	public DynamicURLClassLoader(List aURLs)
	{	mURLs =  parseURLs(aURLs);
	}
	
	public void cleanup()
	{	mURLs=null;
	}
	
	private List parseURLs(List aURLs)
	{	List result = new ArrayList(aURLs.size());
		for (Iterator i=aURLs.iterator(); i.hasNext(); )
		{	String url = (String)i.next();
			String message=null;
			try
			{	if (url.startsWith("file:") && url.endsWith("/"))
				{	URI uri = URI.create(url);
					File f = new File(uri);
					if (f.exists())
					{	ClassPathElement cpe = new ClassPathElement(f,url);
						result.add(cpe);
					}
					else
					{	message = "file doesn't exist";
					}
				}
				else if (url.startsWith("jar:file:") && url.endsWith("!/"))
				{	String fileURI = url.substring(4,url.length()-2);
					File f = new File(URI.create(fileURI));
					JarFile jf = new JarFile(f);
					ClassPathElement cpe = new ClassPathElement(jf,url);
					result.add(cpe);
				}
				else
				{	message = "this URL isn't valid";
				}
			} catch (Exception e)
			{	message = "Unhandled exception "+e.getClass().getName()+": "+e.getMessage();
			}
			if (message!=null)
			{	System.out.println("DynamicURLClassLoader failed to parse "+url+": "+message);
			}
			
		}
		return result;
	}
	
	public Class loadClass(String aName, boolean aResolve) throws ClassNotFoundException
	{	Class c= loadClass(aName);
		if (aResolve)
		{	resolveClass(c);
		}
		return c;
	}	
	
	public Class loadClass(String aName) throws ClassNotFoundException
	{	Class result = innerLocalLoadClass(aName);
		if (result==null)
		{	throw new ClassNotFoundException(aName);
		}
		return result;
	}
	
	protected Class innerLocalLoadClass(String aName)
	{	Class result = findLoadedClass(aName);
		if (result==null)
		{	result = innerLoadClass(aName);
		}
		return result;
	}
	
	public URL getResource(String aName)
	{	try
		{	boolean found=false;
			for (Iterator i = mURLs.iterator(); i.hasNext(); )
			{	ClassPathElement cpe = (ClassPathElement)i.next();
				if (cpe.mJarFile!=null)
				{	JarEntry je = cpe.mJarFile.getJarEntry(aName);
					found=(je!=null);
				}
				else
				{	File f = new File(cpe.mDirectory, aName);
					found=f.exists();
				}
				if (found)
				{	StringBuffer sb = new StringBuffer(aName.length()+cpe.mBaseURL.length());
					sb.append(cpe.mBaseURL);
					Utils.appendUnreservedURIChar(sb, aName);
					return new URL(sb.toString());
				}
					
			}
		} catch (MalformedURLException e)
		{	// just return null
		}
		return null;
	}
	
    /** create or return a buffer that is big enough. This buffer is designed
	 * to be reused as much as possible to avoid memory thrashing but avoids
	 * a possible deadlock scenario when two threads are loading in parallel
	 * (very rare)
     * @param aLength the minimum size of buffer acceptable
     * @return the buffer
     */
    private static byte[] getBuffer(int aLength)
	{	byte[] result;
		synchronized(mBufferSync)
		{	Thread current = Thread.currentThread();
			if (mBuffer1User!=null && mBuffer1User!=current)
			{	if (mBuffer2User!=null && mBuffer2User!=current)
				{	result = new byte[aLength];
				}
				else
				{	if (mBuffer2.length<aLength)
					{   mBuffer2 = new byte[aLength];
					}
					result=mBuffer2;
					mBuffer2User = current;
					mBuffer2Count++;					
				}
			}
			else
			{	if (mBuffer1.length<aLength)
				{   mBuffer1 = new byte[aLength];
				}
				result=mBuffer1;
				mBuffer1User = current;
				mBuffer1Count++;
			}
		}
		return result;
    }
	
	/** release buffer for reuse
	 */
	private static void releaseBuffer(byte[] aBuffer)
	{	synchronized(mBufferSync)
		{	if (aBuffer==mBuffer1)
			{	if (--mBuffer1Count==0)
				{	mBuffer1User=null;
				}
			}
			else if (aBuffer==mBuffer2)
			{	if (--mBuffer2Count==0)
				{	mBuffer2User=null;
				}
			}
		}
	}
	
	private Class innerLoadClass(String aName)
	{	StringBuffer sb=new StringBuffer(aName.length()+6);
		sb.append(aName.replace('.','/'));
		sb.append(".class");
		String resource = sb.toString();
		Class result = null;
		try
		{	for (Iterator i = mURLs.iterator(); i.hasNext(); )
			{	ClassPathElement cpe = (ClassPathElement)i.next();
				if (cpe.mJarFile!=null)
				{	JarEntry je = cpe.mJarFile.getJarEntry(resource);
					if (je!=null)
					{	result = createClassFromInputStream(aName,cpe.mJarFile.getInputStream(je),(int)je.getSize());
						break;
					}
				}
				else
				{	File f = new File(cpe.mDirectory, resource);
					if (f.exists())
					{	result = createClassFromInputStream(aName,new FileInputStream(f), (int)f.length());
						break;
					}
				}
			}
		} catch (IOException e)
		{	/* just return null */
		}
		return result;
	}

	protected Class createClassFromInputStream(String aName,InputStream aStream, int aLength) throws IOException
	{	Class result;
		int j=0;
		byte[] buffer = getBuffer(aLength);
		try
		{	while (j<aLength)
			{   int r = aStream.read(buffer,j, aLength-j);
				j+=r;
			}
			aStream.close();
			result = defineClass(aName,buffer,0,aLength);
			
			int i = aName.lastIndexOf('.');
			if (i != -1)
			{	String pkgname = aName.substring(0, i);
				Package pkg = getPackage(pkgname);
				if (pkg==null)
				{	definePackage(pkgname, null, null, null, null, null, null, null );
				}
			}

			return result;
		}
		finally
		{	releaseBuffer(buffer);
		}
		
	}
	
	/** append XML representation of classloader structure
	 */
	public void appendXML(Writer aWriter) throws IOException
	{	aWriter.write("<DynamicURLClassLoader>");
		for (Iterator i=mURLs.iterator(); i.hasNext(); )
		{	ClassPathElement cpe = (ClassPathElement)i.next();
			XMLUtils.write(aWriter, "url", XMLUtils.escape(cpe.mBaseURL));
		}
		aWriter.write("</DynamicURLClassLoader>");
	}
}