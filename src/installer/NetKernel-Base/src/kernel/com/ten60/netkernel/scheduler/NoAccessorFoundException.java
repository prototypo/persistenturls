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
 * File:          $RCSfile: NoAccessorFoundException.java,v $
 * Version:       $Name:  $ $Revision: 1.5 $
 * Last Modified: $Date: 2004/08/06 08:37:53 $
 *****************************************************************************/
package com.ten60.netkernel.scheduler;

import com.ten60.netkernel.util.*;
import com.ten60.netkernel.module.MappingDebug;
import com.ten60.netkernel.urii.URIdentifier;
import java.util.*;
import java.io.*;
/**
 * An XML exception that contains an accessor resolution trace
 * @author  tab
 */
public class NoAccessorFoundException extends Exception implements IXMLException
{
	private List mDebugList;
	private URIdentifier mRequest;
	private URIdentifier mModule;
	/** Creates a new instance of NoAccessorFoundException */
	public NoAccessorFoundException(URIdentifier aRequest, URIdentifier aModule, List aDebugList)
	{	mDebugList = aDebugList;
		mRequest = aRequest;
		mModule = aModule;
	}
	
	public void appendXML(Writer aWriter) throws IOException
	{
		aWriter.write("<ex>");
		write("id",getDeepestId(),aWriter,false);
		write("message","The request "+XMLUtils.escape(mRequest.toString())+" in module "+XMLUtils.escape(mModule.toString())+" failed because no module could be located to handle it. See Exception trace for details of resolution process.",aWriter,false);
		write("requestid",mRequest.toString(),aWriter,true);
		aWriter.write("<trace>");
		for (Iterator i = mDebugList.iterator(); i.hasNext(); )
		{	MappingDebug debug = (MappingDebug)i.next();
			write("step",debug.toString(), aWriter,true);
		}
		aWriter.write("</trace>");
		aWriter.write("</ex>");
	}
	
	public String getDeepestId()
	{	return "URI Resolution Failure";
	}

	private void write(String aName, String aText, Writer aWriter, boolean aEscape) throws IOException
	{	aWriter.write('<');
		aWriter.write(aName);
		aWriter.write('>');
		if (aEscape)
		{	aWriter.write(XMLUtils.escape(aText));
		}
		else
		{	aWriter.write(aText);
		}
		aWriter.write("</");
		aWriter.write(aName);
		aWriter.write('>');
	}
}