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
 * File:          $RCSfile: MappingDebug.java,v $
 * Version:       $Name:  $ $Revision: 1.3 $
 * Last Modified: $Date: 2004/08/11 12:33:16 $
 *****************************************************************************/
package com.ten60.netkernel.module;

/**
 * An entry in the captured debug in the request resolution process
 * @author  tab
 */
public final class MappingDebug
{
	public static final int DBG_SEARCH=0;
	public static final int DBG_START_MODULE=1;
	public static final int DBG_REWRITE=2;
	public static final int DBG_ENTER_IMPORT=3;
	public static final int DBG_ENTER_SUPER=4;
	public static final int DBG_IGNORE_MAPPING=5;
	public static final int DBG_FAIL_MAPPING=6;
	public static final int DBG_MATCH_MAPPING=7;
	public static final int DBG_NO_MAPPING=8;
	public static final int DBG_SKIP=9;
	
	private int mDebugState;
	private Object mData;
	
	/** Creates a new instance of MappingDebug */
	public MappingDebug(int aState, Object aData)
	{	mDebugState = aState;
		mData = aData;
	}
	
	public String getStateString()
	{	String result;
		switch (mDebugState)
		{	case DBG_SEARCH:
				result = "Searching for"; break;
			case DBG_START_MODULE:
				result = "Starting Module"; break;
			case DBG_REWRITE:
				result = "Rewriting Request to"; break;
			case DBG_ENTER_IMPORT:
				result = "Entering Import Module"; break;
			case DBG_ENTER_SUPER:
				result = "Entering Parent Module"; break;
			case DBG_IGNORE_MAPPING:
				result = "Ignoring pre-checked Mapping"; break;
			case DBG_FAIL_MAPPING:
				result = "Checked Unmatched Mapping"; break;
			case DBG_MATCH_MAPPING:
				result = "Matched on Mapping"; break;
			case DBG_NO_MAPPING:
				result = "No Match Found for"; break;
			case DBG_SKIP:
				result = "Matched on Mapping"; break;
			default:
				result="Unknown Debug State"; break;
		}
		return result;
	}
	
	public String toString()
	{	return getStateString()+" "+mData.toString();
	}
	
}
