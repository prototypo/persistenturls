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
 * File:          $RCSfile: RequestStatistics.java,v $
 * Version:       $Name:  $ $Revision: 1.7 $
 * Last Modified: $Date: 2005/10/03 16:37:20 $
 *****************************************************************************/
package com.ten60.netkernel.scheduler;

import com.ten60.netkernel.urii.*;
import com.ten60.netkernel.urii.accessor.IURAccessor;
import com.ten60.netkernel.urii.representation.ITransrepresentor;
import com.ten60.netkernel.util.XMLUtils;
import java.io.*;
import java.util.*;

/**
 * A table of request statistics held by the Scheduler for all accessors and transreptors used
 * @author  tab
 */
public class RequestStatistics
{
	private static class Stat
	{	public int mCount;
		public int mFailures;
		public long mElapsedTime;
		public long mLocalTime;
	};
	
	private final Map mAccessorStats = new HashMap(128);
	private final Map mTransreptorStats = new HashMap(64);
	private Map mFragmentorStats = new HashMap(64);
	
	public void addStatisticsFor(RequestState aState, boolean aFailed)
	{	IURAccessor accessor = aState.getAccessor();
		if (accessor!=null)
		{	String key = aState.getAccessorClass();
			if (key!=null)
			{	
				Stat stat;
				synchronized(mAccessorStats)
				{	stat = (Stat)mAccessorStats.get(key);
					if (stat==null)
					{	stat=new Stat();
						mAccessorStats.put(key,stat);
					}
				}
				synchronized(stat)
				{	stat.mCount++;
					if (aFailed) stat.mFailures++;
					stat.mElapsedTime+=aState.getCummulativeTime();
					stat.mLocalTime+=aState.getRequestTime();
				}
			}
		}
	}
	
	public void addStatisticsFor(Object aTransreptor, long aElapsed, long aLocal, boolean aFailed)
	{	Map map=(aTransreptor instanceof ITransrepresentor)?mTransreptorStats:mFragmentorStats;
		String key = aTransreptor.getClass().getName();
		Stat stat;
		synchronized(map)
		{	stat = (Stat)map.get(key);
			if (stat==null)
			{	stat=new Stat();
				map.put(key,stat);
			}
		}
		synchronized(stat)
		{	stat.mCount++;
			if (aFailed) stat.mFailures++;
			stat.mElapsedTime+=aElapsed;
			stat.mLocalTime+=aLocal;
		}
	}
	
	public void write(Writer aWriter) throws IOException
	{	writeMap(aWriter,mAccessorStats,"ura");
		writeMap(aWriter,mTransreptorStats,"transreptor");
		writeMap(aWriter,mFragmentorStats,"fragmentor");
	}
	
	private static void writeMap(Writer aWriter, Map aMap, String aName) throws IOException
	{	aWriter.write("<");
		aWriter.write(aName);
		aWriter.write("s>");
		synchronized(aMap)
		{	for (Iterator i = aMap.entrySet().iterator(); i.hasNext(); )
			{	Map.Entry entry = (Map.Entry)i.next();
				Object key = entry.getKey();
				Stat stat = (Stat)entry.getValue();
				write(stat, key, aName, aWriter);
			}
		}
		aWriter.write("</");
		aWriter.write(aName);
		aWriter.write("s>");
	}
		
	private static void write(Stat aStat, Object aKey, String aName, Writer aWriter) throws IOException
	{
		aWriter.write('<');
		aWriter.write(aName);
		aWriter.write('>');

		XMLUtils.write(aWriter,"key",XMLUtils.escape(aKey.toString()));
		XMLUtils.write(aWriter,"count",Integer.toString(aStat.mCount));
		XMLUtils.write(aWriter,"failures",Integer.toString(aStat.mFailures));
		XMLUtils.write(aWriter,"elapsed",Long.toString(aStat.mElapsedTime));
		XMLUtils.write(aWriter,"local",Long.toString(aStat.mLocalTime));

		aWriter.write("</");
		aWriter.write(aName);
		aWriter.write('>');
	}
}