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
 * File:          $RCSfile: URIdentifier.java,v $
 * Version:       $Name:  $ $Revision: 1.3 $
 * Last Modified: $Date: 2004/10/06 10:42:02 $
 *****************************************************************************/
package com.ten60.netkernel.urii;
/**
 * A Universal Resource Identifier. This implementation is a simple wrapper over a string that
 * minimises processing and storage costs if all you want to do is pass a URI around and use
 * it as a string.
 * @author  tab
 */
public final class URIdentifier
{
	/** internal storage */
	private String mString;
	
	/** Creates a new instance of URIdentifier */
	public URIdentifier(String aString)
	{	mString=aString;
	}
	/** @return a string representation of a URI */
	public String toString()
	{	return mString;
	}
	/** @return true if two URIs are equal */
	public boolean equals(Object aOther)
	{	boolean result = false;
		if (aOther instanceof URIdentifier)
		{	URIdentifier other = (URIdentifier)aOther;
			result = other.mString.equals(mString);
		}
		return result;
	}
	/** @return a hashcode for the URI */
	public int hashCode()
	{	return mString.hashCode();
	}
	
	/** constantly incrementing number for generating unique URIs */
	private static long sTag;
	/**  we need an object to synchronize on to stop concurrent access to sTag */
	private static Object sSynchDummy = new Boolean(true);
	/** @return a unique URI with the given prefix and ending in a unique number */
	public static URIdentifier getUnique(String aPrefix)
	{	long tag;
		synchronized(sSynchDummy)
		{	tag = sTag++;
		}
		return new URIdentifier(aPrefix+Long.toString(tag));
	}
	/** @return a URI built from an escaped (with %) string */
	public static URIdentifier fromEscaped(String aURI)
	{	return new URIdentifier(decode(aURI));
	}
	/** private method to unescape URI string */
	private static String decode(String aInput)
	{	StringBuffer sb=new StringBuffer(aInput.length());
		int length = aInput.length();
		for (int i=0; i<length; i++)
		{	char c = aInput.charAt(i);
			char r=c;
			if (c=='%')
			{	int c1 = toHex(aInput.charAt(++i));
				int c2 = toHex(aInput.charAt(++i));
				r = (char)((c1<<4) + c2);
			}
			sb.append(r);
		}
		return new String(sb);
	}	
	/** private method to decode a character [0-9 A-F] to a hexadecimal value */
	private static int toHex(char aChar)
	{	int result=0;
		if (aChar<='9' && aChar>='0')
		{	result = aChar-'0';
		}
		else if (aChar<='Z' && aChar>='A')
		{	result= aChar-('A'-10);
		}
		return result;
	}
	
	/* return the first scheme found in a this URI, null if not found */
	public String getScheme()
	{	String result=null;
		int i1 = mString.indexOf(':');
		if (i1>0)
		{	result = mString.substring(0,i1);
		}
		return result;
	}
	
	/** return the last fragment in this URI, null if not found */
	public String getFragment()
	{	String result=null;
		int i1 = mString.indexOf('#');
		if (i1>=0 && i1<mString.length()-1 )
		{	result = mString.substring(i1+1);
		}
		return result;
	}
	
	/** return this URI without the last fragment */
	public URIdentifier withoutFragment()
	{	URIdentifier result=this;
		int i1 = mString.indexOf('#');
		if (i1>=0)
		{	result = new URIdentifier(mString.substring(0,i1));
		}
		return result;
	}
	
	/** return this URI without the fragment set as specified */
	public URIdentifier withFragment(String aFragment)
	{	URIdentifier result=new URIdentifier(this.withoutFragment().toString()+"#"+aFragment);
		return result;
	}

	/** return the scheme specific part of this URI, I.e. what is between the first scheme and the last fragment */
	public String getSchemeSpecificPart()
	{	String result;
		int i1 = mString.indexOf(':');
		if (i1<0) i1=0;
		else i1+=1;
		int i2 = mString.indexOf('#');
		if (i2<0) i2=mString.length();
		result = mString.substring(i1,i2);
		return result;
	}
}