/******************************************************************************
  (c) Copyright 2002 - $Date: 2005/03/24 11:22:45 $ 1060 Research Ltd

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

  File:          $RCSfile: ElementEncoderDecoderAccessor.java,v $
  Version:       $Name:  $ $Revision: 1.6 $
  Last Modified: $Date: 2005/03/24 11:22:45 $
 *****************************************************************************/

package org.ten60.rdbms.accessor;

import org.ten60.rdbms.accessor.*;
import org.ten60.rdbms.util.*;

import org.ten60.netkernel.xml.xahelper.*;
import org.ten60.netkernel.xml.xda.*;
import org.ten60.netkernel.xml.representation.*;
import org.ten60.netkernel.xml.util.*;

import com.ten60.netkernel.urii.*;

import java.text.*;
import java.util.*;
import java.io.*;

/**
 *	Encode or Decode an element and it's sub-tree From XML to RDBMS or RDBMS to XML.
 *	Element is specified by a canonical XPath passed as the operator.	
 * @author  pjr
 */
public class ElementEncoderDecoderAccessor extends XAccessor
{
	/** Creates a new instance of TimeStampAccessor */
	public ElementEncoderDecoderAccessor()
	{	declareArgument(OPERAND, true, true);
		declareArgument(OPERATOR, true, false);
	}
	
	protected com.ten60.netkernel.urii.IURRepresentation source(XAHelper aHelper) throws Exception
	{	IURRepresentation result=null;
		if(aHelper.getType().equals("SQLEncodeElement"))
		{	result=encode(aHelper);
		}
		if(aHelper.getType().equals("SQLDecodeElement"))
		{	result=decode(aHelper);
		}
		return result;
	}	
	

	public IURRepresentation encode(XAHelper aHelper) throws Exception
	{	DOMXDA decoded=new DOMXDA(aHelper.getOperand().getReadOnlyDocument());
		String xpath=aHelper.getOperator().getXDA().getText("/xpath", true);
		IXDAIterator it=decoded.iterator(xpath);
		while(it.hasNext())
		{	it.next();
			StringWriter sw=new StringWriter();
			IXDAIterator it2=it.iterator("*");
			while(it2.hasNext())
			{	it2.next();
				it2.serialize(sw,".",false);
			}
			String serialized=SQLXMLUtil.inescape(sw.getBuffer().toString());
			it.delete("*");
			it.setText(".", serialized);
		}
		return DOMXDAAspect.create(aHelper.getDependencyMeta("text/xml",8),decoded);
	}
	
	public IURRepresentation decode(XAHelper aHelper) throws Exception
	{	DOMXDA decoded=new DOMXDA(aHelper.getOperand().getReadOnlyDocument());
		String xpath=aHelper.getOperator().getXDA().getText("/xpath", true);
		IXDAIterator it=decoded.iterator(xpath);
		while(it.hasNext())
		{	it.next();
			String entryText=it.getText(".", false);
			DOMXDA entry=new DOMXDA(XMLUtils.parse(new StringReader(entryText)), false);
			it.appendPath(".", "replaceme",null);
			it.replace(entry,"/", "replaceme");
			it.setText(".", "");
		}
		
		return DOMXDAAspect.create(aHelper.getDependencyMeta("text/xml",8),decoded);
	}
	
	
}
