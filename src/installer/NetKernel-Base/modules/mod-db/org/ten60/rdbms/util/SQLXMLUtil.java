/******************************************************************************
  (c) Copyright 2002 - $Date: 2005/11/16 09:06:32 $ 1060 Research Ltd

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

  File:          $RCSfile: SQLXMLUtil.java,v $
  Version:       $Name:  $ $Revision: 1.19 $
  Last Modified: $Date: 2005/11/16 09:06:32 $
 *****************************************************************************/


package org.ten60.rdbms.util;

import org.ten60.netkernel.xml.xda.*;
import org.ten60.netkernel.xml.util.*;

import java.sql.*;
import java.io.*;
import java.security.*;
import org.w3c.dom.*;

/**
 * Utilities to convert from SQL ResultSets to XML and XML NVP to SQL Update
 * @author  pjr
 */
public class SQLXMLUtil
{
	
	/** Creates a new instance of SQLXMLUtil */
	public SQLXMLUtil()
	{
	}
	
	/** Convert a Result Set to an XML document */
	public static DOMXDA resultsetToXML(ResultSet rs, String root, String itemname) throws Exception
	{	String s = resultsetToXMLUnparsed(rs,root,itemname);
		Document d=XMLUtils.getInstance().parse(new StringReader(s));
		return new DOMXDA(d, false);		
	}
	
	/** Convert a Result Set to an XML document */
	public static String resultsetToXMLUnparsed(ResultSet rs, String root, String itemname) throws Exception
	{	StringBuffer sb=new StringBuffer(4096);
		resultsetToXMLUnparsed(sb,rs,root,itemname);
		return sb.toString();		
	}
	
	public static void resultsetToXMLUnparsed(StringBuffer sb, ResultSet rs, String root, String itemname) throws Exception
	{	ResultSetMetaData rsmd=rs.getMetaData();
		int items=rsmd.getColumnCount();
		if (!rs.next())
		{	sb.append("<null/>");
		}
		else
		{	sb.append('<');
			sb.append(root);
			sb.append('>');
			do
			{	processRow(sb,rs, rsmd, itemname, items);
			} while (rs.next());
			sb.append("</");
			sb.append(root);
			sb.append('>');
		}
		
	}
	
	private static void processRow(StringBuffer sb, ResultSet rs, ResultSetMetaData rsmd, String itemname, int items) throws Exception
	{	sb.append('<');
		sb.append(itemname);
		sb.append('>');
		for(int i=0; i<items;i++)
		{	String element=rsmd.getColumnName(i+1);
			Object o=rs.getObject(i+1);
			sb.append('<');
			sb.append(element);
			sb.append('>');
			if (o!=null) sb.append(outescape(o.toString()));
			sb.append("</");
			sb.append(element);
			sb.append('>');
		}
		sb.append("</");
		sb.append(itemname);
		sb.append('>');
	}
	
	
	/** Convert a Name Value Pair XML document to an SQL Update */
	public static String xmlToUpdate(IXDAReadOnly r, String table, String qualifier) throws Exception
	{	IXDAReadOnlyIterator ir=r.readOnlyIterator("/nvp/*");
		StringBuffer sb=new StringBuffer("UPDATE "+table+" SET ");
		while(ir.hasNext())
		{	ir.next();
			sb.append(ir.eval("name ( . )").getStringValue()+" = ");
			String value=ir.getText(".",true);
			int trialint;
			try{
				trialint=Integer.parseInt(value); 
				sb.append(trialint);
			}
			catch(NumberFormatException e)
			{	sb.append(" '"+inescape(value)+"' ");
			}
			if(ir.hasNext()) sb.append(", ");
		}
		sb.append(" WHERE "+qualifier +" ;");
		return sb.toString();	
	}
	
	/** Escpae database inbound data to be correct SQL
	 * For security reaplace all single quotes and backslashes ' --> '' and \ --> \\
	 */
	public static String inescape(String s)
	{	StringBuffer sb=new StringBuffer(s.length()+10);
		for(int i=0; i<s.length(); i++)
		{	char c=s.charAt(i);
			switch(c)
			{	case '\'':
					sb.append("\'\'");
				break;
				case '\\':
					sb.append("\\\\");
				break;
				default:
					sb.append(c);
			}
		}
		return sb.toString();
	}
	
	/** Escape database outbound data to be correct XML*/
	public static String outescape(String s)
	{	StringBuffer sb=new StringBuffer(s.length());
		for(int i=0; i<s.length(); i++)
		{	char c=s.charAt(i);
			sb.append(c);
			try
			{	switch(c)
				{	case '\'':
						if(s.charAt(i+1)=='\'')
						{	i++;
						}
					break;
					case '\\':
						if(s.charAt(i+1)=='\\')
						{	i++;
						}
					break;
				}
			}
			catch(java.lang.StringIndexOutOfBoundsException e)
			{	/*We've reached the end of the string.*/
				break;
			}
		}
		return XMLUtils.getInstance().escape(sb.toString());
	}
	
	/** Get DATE TIME string */
	public static String getCurrentDateTime()
	{	long t=System.currentTimeMillis();
		return ((new java.sql.Date(t).toString())+" "+(new java.sql.Time(t).toString()));
	}
	
}
