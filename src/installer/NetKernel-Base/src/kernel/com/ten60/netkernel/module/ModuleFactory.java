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
 * File:          $RCSfile: ModuleFactory.java,v $
 * Version:       $Name:  $ $Revision: 1.9 $
 * Last Modified: $Date: 2006/08/30 08:17:45 $
 *****************************************************************************/
package com.ten60.netkernel.module;
//import com.ten60.netkernel.container.*;
import com.ten60.netkernel.util.*;
//import com.ten60.netkernel.urii.*;
import java.util.*;
import java.net.*;
import java.io.*;
import org.w3c.dom.*;
/**
 * ModuleFactory keeps tabs on existing module definitions and will reuse them on hotRestarts
 * outputs summary of installed and uninstalled modules
 * @author  tab
 */

public class ModuleFactory
{
	private Map mModules = new HashMap();
	private Set mUsedModules = new HashSet();
	private Set mNewModules = new HashSet();
	private List mOldModules = new ArrayList();
	
	private Map mPreParsed = new HashMap();
	
	private static class PreparseStruct
	{	long mTimestamp;
		Document mDocument;
		public PreparseStruct(long aTimestamp, Document aDocument)
		{	mTimestamp=aTimestamp;
			mDocument=aDocument;
		}
	}
	
	/** pass in the list of module URI strings so that we know what is still used
	 */
	public void preParseModules(List aModuleURIStrings) throws NetKernelException
	{	HashSet preParsed = new HashSet();
		for (Iterator i=aModuleURIStrings.iterator(); i.hasNext(); )
		{	String moduleURIString=(String)i.next();
			try
			{	URL moduleConfig = new URL(moduleURIString+ModuleDefinition.MODULE_CONFIG);
				URLConnection c = moduleConfig.openConnection();
				InputStream is = c.getInputStream();
				long timestamp = c.getLastModified();
				Document d = XMLUtils.parse(is);
				XMLReadable readable = new XMLReadable(d);
				String uri =readable.getText("module/identity/uri");
				String version = readable.getText("module/identity/version");
				String key = uri+version;
				preParsed.add(key);
				mPreParsed.put(moduleURIString,new PreparseStruct(timestamp,d));
			}
			catch (Throwable t)
			{	SysLogger.log2(SysLogger.SEVERE, this, "Failed to Load Module at [%1] : %2",moduleURIString, t.toString());
			}
		}
		// invalidate modules that are not going to be here next time around
		for (Iterator i = mModules.entrySet().iterator(); i.hasNext(); )
		{	Map.Entry entry = (Map.Entry)i.next();
			String key = (String)entry.getKey();
			ModuleDefinition md = (ModuleDefinition)entry.getValue();
			if (!preParsed.contains(key))
			{	//System.out.println("invalidate: "+key);
				md.getClassLoader().invalidate();
			}
		}
	}
	
	
	/** Return a ModuleDefinition for a given source URI
	 */
	public ModuleDefinition getModuleFor(String aModuleURIString, ModuleManager aManager) throws Exception
	{
		PreparseStruct pps = (PreparseStruct)mPreParsed.get(aModuleURIString);
		if (pps==null)
		{	throw new Exception("Module not available");
		}
		XMLReadable readable = new XMLReadable(pps.mDocument);
		String uri =readable.getText("module/identity/uri");
		String version = readable.getText("module/identity/version");
		
		String key = uri+version;
		if (mUsedModules.contains(key))
		{	throw new NetKernelException("Duplicate Module",null,key);
		}
		ModuleDefinition md = (ModuleDefinition)mModules.get(key);
		boolean createNew = (md==null || md.getClassLoader().isInvalid());
		if (createNew)
		{	if (md!=null)
			{	mOldModules.add(md);
			}
			md = new ModuleDefinition(aModuleURIString, pps.mTimestamp, pps.mDocument, aManager);
			mModules.put(key, md);
			mNewModules.add(key);
		}
		mUsedModules.add(key);
		return md;
	}
	
	/** Output summary of changes since last complete()
	 */
	public void complete()
	{	boolean noChanges=true;
		
		// these modules have been replaced
		for (Iterator i = mOldModules.iterator(); i.hasNext(); )
		{	ModuleDefinition md = (ModuleDefinition)i.next();
			md.cleanup();
			SysLogger.log3(SysLogger.CONTAINER,this, "  %1 module [%2 v%3]","Decommisioning",md.getURI().toString(),md.getVersion().toString(3));
			noChanges=false;
		} 
		
		for (Iterator i = mModules.entrySet().iterator(); i.hasNext(); )
		{	Map.Entry entry = (Map.Entry)i.next();
			String key = (String)entry.getKey();
			ModuleDefinition md = (ModuleDefinition)entry.getValue();
			String message=null;
			if (!mUsedModules.contains(key))
			{	message = "Decommissioning";
				i.remove();
				md.cleanup();
				noChanges=false;
			}
			else if (mNewModules.contains(key))
			{	message = "Commissioning";
				noChanges=false;
			}
			if (message!=null)
			{	SysLogger.log3(SysLogger.CONTAINER,this, "  %1 module [%2 v%3]",message,md.getURI().toString(),md.getVersion().toString(3));
			}
		}
		if (noChanges)
		{	SysLogger.log(SysLogger.INFO,this, "No changes in module configuration detected");
		}
		mUsedModules.clear();
		mNewModules.clear();
		mOldModules.clear();
		mPreParsed.clear();
	}
	
}
