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
 * File:          $RCSfile: XMLReadable.java,v $
 * Version:       $Name:  $ $Revision: 1.1 $
 * Last Modified: $Date: 2004/06/18 10:15:23 $
 *****************************************************************************/
package com.ten60.netkernel.util;

import javax.xml.transform.TransformerException;
import org.w3c.dom.*;
import java.util.*;

/**
 * Thin wrapper over a DOM to provide fast XPath lookup on values in convenient ways
 * @author  tab
 */
public final class XMLReadable
{
	private Node mContextNode;
	
	/** Creates a new instance of XMLReadable 
	  @param aContextNode the context node that relative expressions are evaluated against
	 */
	public XMLReadable(Node aContextNode)
	{	mContextNode = aContextNode;
	}
	
	/** return a list of nodes that match the given expression */
	public List getNodes(String aXPath)
	{	try
		{	return FastXPath.eval(mContextNode, aXPath);
		}	catch (TransformerException e)
		{	SysLogger.log(SysLogger.WARNING, this, aXPath+" eval failed on config");
			return Collections.EMPTY_LIST;
		}
	}
	
	/** return a list of strings that are the text values of elements matching the given expression
	 * @param aTrim if true trims the resulting strings of whitespace
	 */
	public List getTexts(String aXPath, boolean aTrim)
	{	try
		{	List nodes = FastXPath.eval(mContextNode, aXPath);
			for (int i=0; i<nodes.size(); i++ )
			{	Node n = (Node)nodes.get(i);
				String text = XMLUtils.getText(n);
				if (aTrim) text=text.trim();
				nodes.set(i,text);
			}
			return nodes;
		} catch (TransformerException e)
		{	SysLogger.log(SysLogger.WARNING, this, aXPath+" eval failed on config");
			return Collections.EMPTY_LIST;
		}
	}
	
	/** return a string value of the text of a single node, returns an empty string if node not found */
	public String getText(String aXPath)
	{	String result="";
		try
		{	Node n = FastXPath.getSingleNode(mContextNode, aXPath);
			if (n!=null)
			{	result = XMLUtils.getText(n);
			}
		}	catch (TransformerException e)
		{	SysLogger.log(SysLogger.WARNING, this, aXPath+" eval failed on config");
		}
		return result;
	}
	
	/** return an integer from text value of the text at a node given by the expression.
	 * if the node isn't found or fails to parse as a number, the default value is used
	 */ 
	public int getInt(String aXPath, int aDefault)
	{	try
		{	Node n = FastXPath.getSingleNode(mContextNode, aXPath);
			String s = XMLUtils.getText(n);
			return Integer.parseInt(s);
		}	catch (Exception e)
		{	SysLogger.log(SysLogger.WARNING, this, aXPath+" eval failed on config");
			return aDefault;
		}
	}
}