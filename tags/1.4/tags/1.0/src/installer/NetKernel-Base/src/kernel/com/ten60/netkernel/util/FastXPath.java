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
 * File:          $RCSfile: FastXPath.java,v $
 * Version:       $Name:  $ $Revision: 1.2 $
 * Last Modified: $Date: 2004/08/06 08:37:53 $
 *****************************************************************************/
package com.ten60.netkernel.util;

import org.apache.xpath.objects.XObject;
import javax.xml.transform.TransformerException;

import org.w3c.dom.*;
import java.util.*;
/**
 * A Fast implementation XPath for simple expressions.
 * Evaluation shows it is an order of magnitude faster than xalan and will
 * work for about 2/3 of expressions in normal usage.
 * <br/>It will evaluate xpaths of the forms:
 * <ul><li> /a/b (absolute)
 * </li><li> a/b (relative)
 * </li><li> /a/* /c (disregard name)
 * </li><li> /a/b[1] (element position)
 * </li></ul>
 * @author  tab
 */
public class FastXPath
{	/** all expressions are cached for quick evaluation */
	private static Map mCachedEvals = new HashMap(128);

	/** Inner class to store pre-parsed xpath expression */
	private static class SimpleEvalStruct
	{	String[] mNames;
		int[] mIndices;
		/** construct/parse a new xpath expression */
		public SimpleEvalStruct(String aXPath)
		{	
			StringTokenizer stz = new StringTokenizer(aXPath,"/");
			int length = stz.countTokens();
			mNames = new String[length];
			mIndices = new int[length];
			for (int i=0; i<length; i++)
			{	String token = stz.nextToken();
				int i1 = token.indexOf('[');
				if (i1>=0)
				{	int i2 = token.indexOf(']');
					String indexString = token.substring(i1+1,i2);
					mIndices[i] = Integer.parseInt(indexString);
					mNames[i] = token.substring(0,i1);
				}
				else
				{	mIndices[i]=-1;
					mNames[i] = token;
				}
				if (mNames[i].equals("*"))
				{	mNames[i]=null;
				}
			}
		}
		/** finds all nodes that match as the children of the given node at the
		 * given depth - will return null if none match
		 */
		public final List findMatch(Node aNode, int aDepth)
		{	List result=null;
			int foundIndex=0;
			String name = mNames[aDepth];
			int index = mIndices[aDepth];
			for (Node n = aNode.getFirstChild(); n!=null; n=n.getNextSibling())
			{	if (n instanceof Element && ( name==null || n.getNodeName().equals(name)))
				{	foundIndex++;
					if (index==-1 || foundIndex==index)
					{	if (result==null)
						{	result = new ArrayList(16);
						}
						result.add(n);
					}
				}
			}
			return result;
		}
		/** return the depth of the xpath expression */
		public final int size()
		{	return mNames.length;
		}
	}
	
	/** Returns a nodeset of matching nodes for the evaluation of the xpath
	 * at the given context node
	 */
	public static List eval(Node aContextNode, String aXPath) throws TransformerException
	{	XObject result;
		List ns = new ArrayList();
		Node current;
		Node found=null;
		// absolute expression
		if (aXPath.charAt(0)=='/' && !(aContextNode instanceof Document))
		{	aContextNode = aContextNode.getOwnerDocument();
		}
		// parse simple xpath and cache
		SimpleEvalStruct levels = (SimpleEvalStruct)mCachedEvals.get(aXPath);
		if (levels==null)
		{	levels = new SimpleEvalStruct(aXPath);
			mCachedEvals.put(aXPath,levels);
		}
		// recursive descent
		try
		{	if (levels.size()>0)
			{	evalDescend(ns,aContextNode,levels,0);
			}
			else
			{	ns.add(aContextNode);
			}
			return ns;
		} catch (Exception e)
		{	throw new TransformerException(aXPath+" is malformed");
		}
	}
	
	public static Node getSingleNode(Node aContextNode, String aXPath) throws TransformerException
	{	List ns = eval(aContextNode, aXPath);
		if (ns.size()==0)
		{	return null;
		}
		return (Node)ns.get(0);
	}
	
	
	/** internal method to perform the iteration down the document
	 * @param aResult the nodeset to add final results too
	 * @param aCurrent the current node
	 * @param aLevels the parsed xpath structure
	 * @param aDepth the current depth
	 */
	private static void evalDescend(List aResult, Node aCurrent, SimpleEvalStruct aLevels, int aDepth)
	{	
		List ns = aLevels.findMatch(aCurrent,aDepth);
		if (ns!=null)
		{	if (aDepth!=aLevels.size()-1)
			{	for (int i=ns.size()-1; i>=0; i--)
				{	Node n = (Node)ns.get(i);
					evalDescend(aResult,n,aLevels,aDepth+1);
				}
			}
			else
			{	aResult.addAll(ns);
			}
		}
	}	
	
	/** Returns true if it is possible to evaluate the xpath using this engine
	 */
	public static boolean isSuitable(String aXPath)
	{	int length = aXPath.length();
		boolean simple=(length>0);
		char c1 = 0;
		for (int i=length-1; i>=0; i--)
		{	char c = aXPath.charAt(i);
			boolean validChar = (c>='a' && c<='z') || (c>='A' && c<='[') || (c>='/' && c<='9') || c==']' || c=='*';
			boolean doubleSlash = (c=='/' && c1=='/');
			if (!validChar || doubleSlash )
			{	simple=false;
				break;
			}
			c1=c;
		}
		return simple;
	}
}