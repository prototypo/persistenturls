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
 * File:          $RCSfile: ModuleClassLoader.java,v $
 * Version:       $Name:  $ $Revision: 1.13 $
 * Last Modified: $Date: 2007/10/04 12:25:23 $
 *****************************************************************************/
package com.ten60.netkernel.module;
import com.ten60.netkernel.util.*;
import java.util.*;
import java.net.URL;
import java.util.regex.*;
import java.io.*;

/**
 * Classloader for a module, it first looks in the module itself, then in order at any
 * imports, finally at the parent classloader.
 * @author  tab
 */
public class ModuleClassLoader extends DynamicURLClassLoader
{
	private PairList mImportMap;
	private ClassLoader mParent;
	private ModuleDefinition mModule;
	private boolean mInvalidated;
	//private String mURIString;

	
	/** Creates a new instance of BetterModuleClassLoader */
	public ModuleClassLoader(ClassLoader aParent, List aURLs, ModuleDefinition aModule)
	{	super(aURLs);
		mParent = aParent;
		mModule=aModule;
		//mURIString=mModule.getURI().toString();
	}
	
	public ClassLoader getKernelClassLoader()
	{	return mParent;
	}
	
	public ModuleDefinition getModule()
	{	return mModule;
	}
	
	public void reset()
	{	mImportMap = new PairList(10);
	}
	
	public void cleanup()
	{	mImportMap=null;
		mParent=null;
		//mModule=null;
		super.cleanup();
	}
	
	public void finalize()
	{	System.out.println("finalize of ModuleClassLoader for "+mModule.getURI().toString());
		mModule=null;
	}
	
	/*
	protected void finalize() throws Throwable
	{	System.out.println("finalize of classloader for "+mURIString);
		super.finalize();
	}
	 */
	
	public void invalidate()
	{	mInvalidated=true;
	}
	
	/** returns true after a classloader has been disposed */
	public boolean isInvalid()
	{	boolean invalid=mInvalidated;
		if (!invalid)
		{
			for (int i=0; i<mImportMap.size(); i++ )
			{	ModuleDefinition md = (ModuleDefinition)mImportMap.getValue2(i);
				ModuleClassLoader mcl=md.getClassLoader();
				if (mcl==null || mcl.isInvalid())
				{	invalid=true;
					break;
				}
			}
		}
		return invalid;
	}
	
	
	/** dynamically add imports to the classloader */
	public void addImportedModule(ModuleDefinition aModule)
	{	List matches = aModule.getExportedClassMatches();
		for (Iterator i=matches.iterator(); i.hasNext(); )
		{	String match = (String)i.next();
			Matcher m = Pattern.compile(match).matcher("");
			mImportMap.put(m,aModule);
		}
	}
	
	public Class loadClass(String aName) throws ClassNotFoundException
	{	Class result;
		if (aName.startsWith("java."))
		{	result = findSystemClass(aName);
		}
		else
		{	result = innerLocalLoadClass(aName);
			if (result==null)
			{	result = loadImportedClass(aName);
			}
			if (result==null && mParent!=null)
			{	try
				{	result = mParent.loadClass(aName);
				}
				catch(java.lang.IllegalArgumentException e)
				{	throw new ClassNotFoundException(aName);
				}
			}
			if (result==null)
			{	throw new ClassNotFoundException(aName);
			}
		}
		return result;
	}
	
	public Class loadClassAvoidingParent(String aName) throws ClassNotFoundException
	{	Class result=null;
		if (aName.startsWith("java."))
		{	result = findSystemClass(aName);
		}
		if (result==null)
		{	result = innerLocalLoadClass(aName);
		}
		if (result==null)
		{	result = loadImportedClass(aName);
		}
		if (result==null)
		{	throw new ClassNotFoundException(aName);
		}
		return result;
	}
	
	private Class loadImportedClass(String aName)
	{	Class result = null;
		for (int i=0; i<mImportMap.size(); i++ )
		{	Matcher m = (Matcher)mImportMap.getValue1(i);
			boolean matches;
			synchronized(m)
			{	m.reset(aName);
				matches = m.matches();
			}
			if (matches)
			{	ModuleDefinition md = (ModuleDefinition)mImportMap.getValue2(i);
				ClassLoader cl = md.getClassLoader();
				if (cl!=null)
				{
					try
					{	result = cl.loadClass(aName);
						break;
					}
					catch (ClassNotFoundException e)
					{	/* the class wasn't found here */
					}
				}
			}
		}
		return result;
	}	
	
	public URL getResource(String aName)
	{	URL result = super.getResource(aName);
		if (result==null)
		{	String nameMatch = aName.replace('/', '.');
			for (int i=0; i<mImportMap.size(); i++ )
			{	Matcher m = (Matcher)mImportMap.getValue1(i);
				boolean matches;
				synchronized(m)
				{	m.reset(nameMatch);
					matches = m.matches();
				}
				if (matches)
				{	ModuleDefinition md = (ModuleDefinition)mImportMap.getValue2(i);
					ClassLoader cl = (ClassLoader)md.getClassLoader();
					result = cl.getResource(aName);
					if (result!=null)
					{	break;
					}
				}
			}
		}
		if (result==null)
		{	result = mParent.getResource(aName);
		}
		return result;
	}	
	
	public String toString()
	{	return "ModuleClassLoader ["+mModule.getURI().toString()+"]";
	}
	
	/** append XML representation of classloader structure
	 */
	public void appendXML(Writer aWriter) throws IOException
	{	aWriter.write("<ModuleClassLoader>");
		XMLUtils.write(aWriter, "uri",XMLUtils.escape(mModule.getURI().toString()));
		super.appendXML(aWriter);
		aWriter.write("<imports>");
		ModuleDefinition last=null;
		for (int i=0; i<mImportMap.size(); i++ )
		{	ModuleDefinition md = (ModuleDefinition)mImportMap.getValue2(i);
			if (md!=last)
			{	XMLUtils.write(aWriter, "import", XMLUtils.escape(md.getURI().toString()));
				last=md;
			}
		}
		aWriter.write("</imports>");
		for (ClassLoader parent=mParent; parent!=null; parent=parent.getParent())
		{	aWriter.write("<parent>");
			XMLUtils.write(aWriter,"toString", XMLUtils.escape(parent.toString()));
			aWriter.write("</parent>");
		}
		aWriter.write("</ModuleClassLoader>");
	}
}