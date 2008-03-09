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
 * File:          $RCSfile: URIMapping.java,v $
 * Version:       $Name:  $ $Revision: 1.3 $
 * Last Modified: $Date: 2004/08/11 12:09:50 $
 *****************************************************************************/
package com.ten60.netkernel.module;
import com.ten60.netkernel.module.rewrite.*;
import java.util.regex.*;
import java.util.*;
/**
 * A 'compiled' entry from the mapping section in a module
 * @author  tab
 */
public final class URIMapping
{
	public static final int TYPE_THIS=1;
	public static final int TYPE_SUPER=2;
	
	private Matcher mMatcher;
	private Matcher[] mMatchers;
	private String mURAClass;
	private ModuleDefinition mImportModule;
	private int mType=0;
	private RegexRewriterRule mRewrite;
	private int mSkipCount=0;
	
	public URIMapping(String aMatch, int aType)
	{	mMatcher = createMatcher(aMatch);
		mType=aType;
	}
	/** Creates a new instance of URIMapping */
	public URIMapping(List aMatches, ModuleDefinition aImport)
	{	mMatchers = new Matcher[aMatches.size()];
		for (int i=0; i<aMatches.size(); i++)
		{	String match = (String)aMatches.get(i);
			mMatchers[i] =createMatcher(match);
		}
		mImportModule=aImport;

	}
	public URIMapping(String aMatch, String aURAClass)
	{	mMatcher = createMatcher(aMatch);
		mURAClass = aURAClass;
	}
	
	public URIMapping(RegexRewriterRule aRewrite)
	{	mRewrite = aRewrite;
	}
	
	public URIMapping(int aSkipCount, String aMatch)
	{	mMatcher = createMatcher(aMatch);
		mSkipCount = aSkipCount;
	}
	
	private static Matcher createMatcher(String aRegex)
	{	return Pattern.compile(aRegex).matcher("");
	}
	
	public String getURAClass()
	{	return mURAClass;
	}
	
	public RegexRewriterRule getRewriterRule()
	{	return mRewrite;
	}
	
	public ModuleDefinition getImportModule()
	{	return mImportModule;
	}
	public int getType()
	{	return mType;
	}
	
	public int getSkipCount()
	{	return mSkipCount;
	}
	
	public synchronized boolean matches(CharSequence aString)
	{	boolean result=false;
		if (mMatcher!=null)
		{	mMatcher.reset(aString);
			result = mMatcher.matches();
		}
		else if (mMatchers!=null)
		{	for (int i=0; i<mMatchers.length; i++)
			{	Matcher matcher = mMatchers[i];
				matcher.reset(aString);
				if (matcher.matches())
				{	result=true;
					break;
				}
			}
		}
		else if (mRewrite!=null)
		{	result = mRewrite.matches(aString);
		}
		
		return result;
	}
	
	public String toString()
	{	StringBuffer sb = new StringBuffer(64);
		if (mMatcher!=null)
		{	sb.append(mMatcher.pattern().pattern());
			sb.append(' ');
		}
		else
		{	sb.append("(many) ");
		}
		if (mType==TYPE_SUPER)
		{	sb.append("SUPER");
		}
		else if (mType==TYPE_THIS)
		{	sb.append("THIS");
		}
		else if (mURAClass!=null)
		{	sb.append(" to accessor ");
			sb.append(mURAClass);
		}
		else if (mImportModule!=null)
		{	sb.append("to import ");
			sb.append(mImportModule.getURI().toString());
		}
		else if (mRewrite!=null)
		{	sb.append("rewrite");
		}
		else if (mSkipCount>0)
		{	sb.append(" skipping next ");
			sb.append(mSkipCount);
			sb.append(" mappings");
		}
		return new String(sb);
	}	
}