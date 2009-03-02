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
 * File:          $RCSfile: RegexRewriterRule.java,v $
 * Version:       $Name:  $ $Revision: 1.1 $
 * Last Modified: $Date: 2004/06/18 10:15:23 $
 *****************************************************************************/
package com.ten60.netkernel.module.rewrite;
import com.ten60.netkernel.urii.*;
import java.util.regex.*;
import com.ten60.netkernel.util.SysLogger;

/**
 * Regular Expression based URL Rewriter Rule.
 * 
 *	eg  Maps  Regex --> New
 *
 * Capturing groups are substituted into New using the apache
 * mod_rewrite $1...$N convention.
 *
 * @author  pjr
 */
public final class RegexRewriterRule
{
	
	private Matcher mMatcher;
	private Matcher mToMatcher;
	private String mTo;
	
	/** Creates a new instance of RegexRewriterRule.
	 *	Matches are created and cached to reduce wasted objects.
	 */		
	public RegexRewriterRule(String aPattern, String aTo)
	{	Pattern pattern=Pattern.compile(aPattern);
		mMatcher=pattern.matcher("");

		Pattern groupPattern=Pattern.compile("\\$(e?\\d+)");
		mTo=aTo;
		mToMatcher=groupPattern.matcher(aTo);
	}

	/**
	 * Map a URI using the preconfigured Matchers
	 */
	 public synchronized URIdentifier map(URIdentifier aURI)
	 {	URIdentifier result = aURI;
		mMatcher=mMatcher.reset(aURI.toString());
		String replace=mTo;
		if(mMatcher.matches())
		{	mToMatcher.reset(replace);
			StringBuffer sb=new StringBuffer(replace.length()*2);
			while (mToMatcher.find())
			{	String toMatch = mToMatcher.group(1);
				char action = ' ';
				if (!Character.isDigit(toMatch.charAt(0)))
				{	action=toMatch.charAt(0);
					toMatch = toMatch.substring(1);
				}
				int j=Integer.parseInt(toMatch);
				String replacement = mMatcher.group(j);
				switch (action)
				{	case 'e':
						replacement = encode(replacement);
						break;
				}
				mToMatcher.appendReplacement(sb, replacement);
			 }
			 mToMatcher.appendTail(sb);			
			
			result = new URIdentifier(sb.toString());
			if(SysLogger.shouldLog(SysLogger.FINE, this))
			{	SysLogger.log(SysLogger.FINE, this, "Rewrote: "+aURI.toString()+" to "+result+" using "+mMatcher.pattern().pattern());
			}
		}
		return result;
	 }
	
	/** simple test to see if it matches without doing anything
	 */
	public synchronized boolean matches(CharSequence aString)
	{	mMatcher.reset(aString);
		return mMatcher.matches();
	}
	
	public static String encode(String aInput)
	{	StringBuffer sb=new StringBuffer(aInput.length()*3/2);
		int length = aInput.length();
		for (int i=0; i<length; i++)
		{	char c = aInput.charAt(i);
			switch (c)
			{	case ' ': sb.append("%20"); break;
				case '%': sb.append("%25"); break;
				case ':': sb.append("%3A"); break;
				case '#': sb.append("%23"); break;
				case '+': sb.append("%2B"); break;
				case '@': sb.append("%40"); break;
				case '=': sb.append("%3D"); break;
				default: sb.append(c);
			}
		}
		return new String(sb);
	}	
	
}
