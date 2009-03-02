/******************************************************************************
 * (c) Copyright 2002,2005, 1060 Research Ltd
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
 * File:          $RCSfile: XMLUtils.java,v $
 * Version:       $Name:  $ $Revision: 1.14 $
 * Last Modified: $Date: 2005/12/06 17:34:58 $
 *****************************************************************************/
package com.ten60.netkernel.util;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import java.io.*;
import org.xml.sax.*;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;

import org.apache.xml.serializer.*;

//Required for setting properties
//import org.apache.xerces.parsers.DOMParser;

/**
 * A set of low level DOM manipulation methods
 * @author  tab
 */
public class XMLUtils
{
	/** singletons indexed by thread */
	private static Map sSingletons = new WeakHashMap();
	
	private static DocumentBuilderFactory sFactory;
	private static DocumentBuilder sBuilder;
	
	static
	{
		sFactory = DocumentBuilderFactory.newInstance();
		sFactory.setNamespaceAware(true);
		try
		{
			/////////////////////////////////////////////////////////////////////////////////////
			//Turn off external entity resolution - we may turn this on later and use EntityResolvers
			//Warning these may only apply to Apache Xerces implementation of JAX Parser
			/////////////////////////////////////////////////////////////////////////////////////
			//Turn off External DTD resolution
			//sFactory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", new Boolean(false));
			sFactory.setValidating(false);
			// don't use deferred expansion
			sFactory.setAttribute("http://apache.org/xml/features/dom/defer-node-expansion", new Boolean(false));
		}
		catch(IllegalArgumentException iae)
		{	SysLogger.log(SysLogger.WARNING, sFactory, "JAXP Config Warning, be careful with external references:"+iae.getMessage());
		}
		try
		{	sBuilder = sFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e)
		{	SysLogger.log(SysLogger.SEVERE, sFactory, "ParserConfigurationException:"+e.getMessage());
		}
	}
	
	/** Creates a new instance of XMLUtils */
	private XMLUtils()
	{
	}
	
	
	/** a different instance is returned for each thread
	 */
	public static XMLUtils getInstance()
	{   // make thread safe - one instance per thread
		XMLUtils singleton = (XMLUtils)sSingletons.get(Thread.currentThread());
		if (singleton==null)
		{   try
			{	singleton = new XMLUtils();
				sSingletons.put(Thread.currentThread(),singleton);
			} catch (Exception e)
			{	e.printStackTrace();
			}
		}
		return singleton;
	}
	
	/** clear out all singletons
	 */
	public static void destroyInstances()
	{	sSingletons.clear();
		//sFactory=null;
		//sBuilder=null;
	}
	
	/** removes singleton associated with the current thread
	 */
	public static void destroyInstance()
	{   sSingletons.remove(Thread.currentThread());
	}
	
	/** return a new empty document
	 */
	public static Document newDocument()
	{   synchronized(sBuilder)
		{	return sBuilder.newDocument();
		}
	}
	
	
	/** write a node out recursively to the given writer
	 */
	public void toXML(Writer aWriter, Node aNode, boolean aIndent, boolean aOmitDeclaration, String aEncoding, int aIndentChars)
	throws IOException
	{	Properties format = new Properties();
		format.setProperty( OutputKeys.METHOD,"xml");
		format.setProperty( OutputKeys.ENCODING,aEncoding);
		format.setProperty( OutputKeys.OMIT_XML_DECLARATION, aOmitDeclaration ? "yes" : "no");
		format.setProperty( OutputKeys.INDENT, aIndent ? "yes" : "no");
		format.setProperty("{http://xml.apache.org/xalan}indent-amount", Integer.toString(aIndentChars));
		Serializer s = SerializerFactory.getSerializer(format);
		
		s.setWriter(aWriter);
		s.asDOMSerializer().serialize(aNode);
	}
	
	/** serialise the given node recursively to a string
	 */
	public String toXML(Node aNode, boolean aIndent, boolean aOmitDeclaration)
	{	try
		{   StringWriter sw = new StringWriter(1024);
			toXML(sw,aNode,aIndent,aOmitDeclaration,"UTF-8",4);
			return new String(sw.getBuffer());
		} catch (IOException e)
		{	/* shouldn't happen */
			return "";
		}
		
	}
	
	/** escape the given string into a form safe to embed into an XML stream
	 */
	public static String escape(String aXML)
	{   int length = aXML.length();
		StringBuffer result = new StringBuffer(length*2);
		boolean changed = false;
		for (int i=0; i<length; i++)
		{   char c = aXML.charAt(i);
			switch(c)
			{   case '<':
					result.append("&lt;");
					changed = true;
					break;
				case '>':
					result.append("&gt;");
					changed = true;
					break;
				case '\'':
					result.append("&apos;");
					changed = true;
					break;
				case '"':
					result.append("&quot;");
					changed = true;
					break;
				case '&':
					result.append("&amp;");
					changed = true;
					break;
				default:
					result.append(c);
					break;
			}
		}
		return (changed)?new String(result):aXML;
	}
	
	/** parse the document in the given reader
	 */
	public static Document parse(Reader aReader)
	throws IOException, SAXException
	{   InputSource is = new InputSource(aReader);
		Thread t=Thread.currentThread();
		ClassLoader old=t.getContextClassLoader();
		t.setContextClassLoader(XMLUtils.class.getClassLoader());
		try
		{	synchronized(sBuilder)
			{	return sBuilder.parse(is);
			}
		}
		finally
		{	t.setContextClassLoader(old);
		}
	}
	/** parse the document in the given input stream
	 */
	public static Document parse(InputStream aStream)
	throws IOException, SAXException
	{   InputSource is = new InputSource(aStream);
		Thread t=Thread.currentThread();
		ClassLoader old=t.getContextClassLoader();
		t.setContextClassLoader(XMLUtils.class.getClassLoader());
		try
		{	synchronized(sBuilder)
			{	return sBuilder.parse(is);
			}
		}
		finally
		{	t.setContextClassLoader(old);
		}
	}
	
	public static String getPathFor(Node aNode)
	{
		StringBuffer result = new StringBuffer(128);
		Node node = aNode;
		while(node!=null)
		{   switch(node.getNodeType())
			{   case Node.ATTRIBUTE_NODE:
					result.insert(0,aNode.getNodeName());
					result.insert(0,"/@");
					Attr a=(Attr)node;
					node=a.getOwnerElement();
					break;
				case Node.ELEMENT_NODE:
					int countInParent=0;
					int indexInParent=-1;
					Node parent=node.getParentNode();
					String name = node.getNodeName();
					for (Node n=parent.getFirstChild(); n!=null; n=n.getNextSibling())
					{   if (n instanceof Element)
						{   if (n==node)
							{	indexInParent=countInParent;
								countInParent++;
							}
							else if (n.getNodeName().equals(name))
							{   countInParent++;
								if (indexInParent>=0)
								{   break;
								}
							}
						}
					}
					if (countInParent>1)
					{   result.insert(0,']');
						result.insert(0,indexInParent+1);
						result.insert(0,'[');
					}
					result.insert(0,name);
					result.insert(0,'/');
					node=parent;
					break;
				default:
					node=node.getParentNode();
					break;
			}
		}
		return new String(result);
	}
	
	public static String getText(Node aNode)
	{   StringBuffer result = null;
		String result2 = null;
		for (Node n=aNode.getFirstChild(); n!=null; n=n.getNextSibling())
		{   if (n.getNodeType()==Node.TEXT_NODE || n.getNodeType()==Node.CDATA_SECTION_NODE)
			{   String s = n.getNodeValue();
				if (result2==null)
				{	result2 = s;
				}
				else if (result==null)
				{	result = new StringBuffer(result2.length()+s.length());
					result.append(result2);
					result.append(s);
				}
				else
				{	result.append(s);
				}
			}
		}
		if (result==null)
		{	if (result2==null)
			{	result2="";
			}
		}
		else
		{	result2 = new String(result);
		}
		return result2;
	}
	
	public static void setText(Element aElement, String aText)
	{   for (Node n=aElement.getFirstChild(); n!=null; )
		{   if (n.getNodeType()==Node.TEXT_NODE || n.getNodeType()==Node.CDATA_SECTION_NODE)
			{   Node oldn = n;
				n=n.getNextSibling();
				aElement.removeChild(oldn);
			}
			else
			{   n=n.getNextSibling();
			}
		}
		Text t = aElement.getOwnerDocument().createTextNode(aText);
		aElement.appendChild(t);
	}
	
	/**
	 * finds the next sibling element
	 */
	public static Element getNextSiblingElement(Node aNode)
	{   Element result=null;
		for (Node n=aNode.getNextSibling(); n!=null; n=n.getNextSibling())
		{   if (n instanceof Element)
			{   result=(Element)n;
				break;
			}
		}
		return result;
	}
	/**
	 * finds the first child element
	 */
	public static Element getFirstChildElement(Node aNode)
	{   Element result=null;
		for (Node n=aNode.getFirstChild(); n!=null; n=n.getNextSibling())
		{   if (n instanceof Element)
			{   result=(Element)n;
				break;
			}
		}
		return result;
	}
	
	/** try down, then across, not ( up-across ) */
	public static Element depthFirstTraversalNextElement(Node aNode)
	{
		Element result=getFirstChildElement(aNode);
		if (result==null)
		{   result = getNextSiblingElement(aNode);
		}
		return result;
	}
	
	/** try down, then across, then ( up-across )
	 * @param aNode next element after this node
	 * @param aRoot we will not traverse higher than this node (if null ignored)
	 */
	public static Element inOrderTraversalNext(Node aNode, Node aRoot)
	{
		Element result=getFirstChildElement(aNode);
		if (result==null)
		{   if (aNode!=aRoot)
			{	result = getNextSiblingElement(aNode);
			}
			else
			{	return null;
			}
		}
		if (result==null)
		{   Node parent=aNode.getParentNode();
			if (parent!=aRoot && parent instanceof Element)
			{   result = getNextSiblingElement(parent);
			}
		}
		return result;
	}
	
	/** Returns the document for a node regardless of type
	 * - why is DOM so inconsistent?
	 */
	public static Document getDocumentFor(Node aNode)
	{	Document result;
		if (aNode instanceof Document)
		{	result = (Document)aNode;
		}
		else
		{	result = aNode.getOwnerDocument();
		}
		return result;
	}
	
	/**
	 * Creates a named element with text
	 * @param aParent node to add element below
	 * @param aName name of element
	 * @param aValue text value below element
	 */
	public static void appendTextedElement(Node aParent,String aName, String aValue)
	{   Document d = aParent.getOwnerDocument();
		Element e = d.createElement(aName);
		Text t = d.createTextNode(aValue);
		e.appendChild(t);
		aParent.appendChild(e);
	}
	
	/** Write an XML element with text to the writer
	 */
	public static void write(Writer osw, String aName, String aValue) throws IOException
	{
		osw.write('<');
		osw.write(aName);
		osw.write('>');
		osw.write(aValue);
		osw.write("</");
		osw.write(aName);
		osw.write('>');
	}
	
	/** Write an escaped XML element with text to the writer
	 */
	public static void writeEscaped(Writer osw, String aName, String aValue) throws IOException
	{	write(osw,aName,escape(aValue));
	}		
	
	/** Write an stack trace to the writer
	 */
	public static void writeStack(Writer osw, StackTraceElement[] aStack, int aDepth) throws IOException
	{	osw.write("<stack>");
		for (int i=0; i<aDepth && i<aStack.length; i++)
		{	StackTraceElement ste = aStack[i];
			String message=ste.getClassName()+'.'+escape(ste.getMethodName())+"()";
			if (ste.getLineNumber()>=0)
			{	message+=" line:"+Integer.toString(ste.getLineNumber());
			}
			write(osw, "level", message);
		}
		osw.write("</stack>");		
	}
}