/******************************************************************************
  (c) Copyright 2002-2005, 1060 Research Ltd                                   

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

  File:          $RCSfile: GUID.java,v $
  Version:       $Name:  $ $Revision: 1.1 $
  Last Modified: $Date: 2005/03/01 11:32:58 $
 *****************************************************************************/


package org.ten60.netkernel.httpclient.util;

import java.security.*;
import java.util.*;

/**
 * GUID Generator
 * @author  pjr
 */
public class GUID
{
	
	/** Creates a new instance of GUID */
	public GUID()
	{
	}
	
	public static String toHexString(byte[] b)
	{	String r = "";
		Integer i = new Integer(0);
		for (int j = 0; j < b.length; j++)
		{	String s = i.toHexString(128 + (int) b[j]);
			if (s.length() == 1)
				s = "0" + s;
			r += s;
		}
		return r.toUpperCase();
	}
	
	public static String GUID()
	{	return GUID(null);
	}
	
	public static String GUID(String aSeed)
	{	Random r=new Random();
		StringBuffer guid=new StringBuffer(Long.toString(r.nextLong()));
		if(aSeed!=null) guid.append(aSeed);
		guid.append(Long.toString(r.nextLong()));
		guid.append(Long.toString(System.currentTimeMillis()));
		guid.append(Long.toString(r.nextLong()));
		MessageDigest md =null;
		try
		{	md	= MessageDigest.getInstance("MD5");
		}
		catch(NoSuchAlgorithmException e) {/*Can't happen*/};
		byte[] b=guid.toString().getBytes();
		md.update(b);
		return toHexString(md.digest());
	}
	
}
