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
 * File:          $RCSfile: HouseKeeper.java,v $
 * Version:       $Name:  $ $Revision: 1.8 $
 * Last Modified: $Date: 2005/11/15 15:33:03 $
 *****************************************************************************/
package com.ten60.netkernel.container;

import com.ten60.netkernel.util.*;
import com.ten60.netkernel.urii.*;

import java.io.*;
import java.text.*;

/**
 * HouseKeeper system component periodically polls all other system components to do their
 * housework. Is a job of its own it keeps a close eye on system memory.
 * @author  tab
 */
public class HouseKeeper extends ComponentImpl implements Runnable
{
	/** our URI */
	public static final URIdentifier URI = new URIdentifier("netkernel:hk");
	/** housework interval */
	private long mPeriod;
	/** number of memory stats kept */
	private int mStatBufferSize;
	/** number of housework periods per memory stat period */
	private int mStatFreqDivider;
	/** format of timestamp applied to memory stats */
	private DateFormat mDateFormat;
	/** our thread */
	private Thread mThread;
	/** true if we have been told to stop */
	private boolean mStop;
	/** the container we are operating in */
	private Container mContainer;
	/** maximum memory **/
	private long mMax;
	
	private static final int STAT_COUNT=4;
	
	/** Creates a new instance of HouseKeeper */
	public HouseKeeper()
	{	super(URI);
		mMax = Runtime.getRuntime().maxMemory()>>10;
		if (System.getProperties().getProperty("java.version").startsWith("1.4.1"))
		{	// work around of bug in JDK - 4686462
			SysLogger.log(SysLogger.WARNING, this, "Possible available memory misreporting due to JDK Bug 4686462 running on JDK1.4.1");
			mMax-=64*1024;
		}
	}
	
	/** starts the housekeeper. It gets configuration from Config component and starts its thread */
	public void start(Container aContainer) throws NetKernelException
	{	mContainer = aContainer;
		Config config = (Config)aContainer.getComponent(Config.URI);
		XMLReadable cr = config.getReadable();
		mPeriod =cr.getInt("system/houseKeepingPeriod", 500);
		mStatBufferSize =cr.getInt("system/statistics/historySize", 60);
		mStatFreqDivider =cr.getInt("system/statistics/frequencyDivisor", 10);
		mDateFormat = new SimpleDateFormat(cr.getText("system/statistics/timestampFormat").trim());
		mStats =  new long[mStatBufferSize*STAT_COUNT];
		Runtime r = Runtime.getRuntime();
		mBaseLine = r.totalMemory()-r.freeMemory();
		mLast = mBaseLine;
		mPeak = mBaseLine;

		mThread = new Thread(mContainer.getRootThreadGroup(),this,"HouseKeeper");
		mThread.start();
	}
	
	/** stops its thread */
	public void stop()
	{	mStop=true;
		mThread.interrupt();
		try
		{	mThread.join();
		} catch (InterruptedException e) { }
	}
	
	/** loops until stopped performing housework and sleeping between */
	public void run()
	{	while (!mStop)
		{	try
			{	Thread.sleep(mPeriod);
				mContainer.doPeriodicHouseKeeping();
			}
			catch (Exception e) {}
		}
		Thread.currentThread().setContextClassLoader(HouseKeeper.class.getClassLoader());
	}
	
	/** the history of memory statistics */
	private long[] mStats;
	/** a smoothed baseline figure to take account of our sampling period error */
	private long mBaseLine;
	/** the last real GC baseline we captured */
	private long mRealBaseLine;
	/** a smoothed peak memory usage to take account of our sampling period error */
	private long mPeak;
	/** the last real GC peak memory we captured */
	private long mRealPeak;
	/** the last memory usage sample we took */
	private long mLast;
	/** counter for implementing frequency divider */
	private int mFreqDivider;
	/** index into rolling statistics buffer **/
	private int mBufferIndex;
	
	/** calculate memory statistics by watching for GC events
	 **/
	public void doPeriodicHouseKeeping()
	{	Runtime r = Runtime.getRuntime();
		long used = r.totalMemory()-r.freeMemory();
		if (used<mLast)
		{	
			
			
			if (used<mRealBaseLine)
			{	mRealBaseLine = used;
			}
			else
			{	mRealBaseLine=used/6 + (mRealBaseLine*5/6);
			}
			if (mLast>mRealPeak)
			{	mRealPeak = mLast;
			}
			else
			{	mRealPeak=mLast/6 + (mRealPeak*5/6);
			}
			//System.out.println( "GC occured "+(mLast>>20)+" ("+(mPeak>>20)+") "+(used>>20)+" ("+(mBaseLine>>20)+")");
		}
		mBaseLine = (mBaseLine*7/8) + mRealBaseLine/8;
		mPeak = (mPeak*7/8) + mRealPeak/8;
		mLast = used;
		
		mFreqDivider = (mFreqDivider+1)%mStatFreqDivider;
		if (mFreqDivider==0)
		{	mBufferIndex = (mBufferIndex+STAT_COUNT)%(mStatBufferSize*STAT_COUNT);
			mStats[ mBufferIndex+0 ] = System.currentTimeMillis();
			mStats[ mBufferIndex +1] = mBaseLine;
			mStats[ mBufferIndex+2 ] = mPeak;
			mStats[ mBufferIndex+3 ] = r.totalMemory();
		}
	}

	
	/** write memory statistics out as XML */
	public void write(OutputStream aStream) throws IOException
	{	OutputStreamWriter osw = new OutputStreamWriter(aStream);
		osw.write("<memory>");
		write(osw,"max",Long.toString(mMax));
		int index = mBufferIndex;
		for (int i=0; i<mStatBufferSize; i++)
		{	index+=STAT_COUNT;
			if (index>=mStatBufferSize*STAT_COUNT) index=0;
			osw.write("<stat>");
			write(osw,"baseline",Long.toString(mStats[index+1]>>10));
			write(osw,"peak",Long.toString(mStats[index+2]>>10));
			write(osw,"alloc",Long.toString(mStats[index+3]>>10));
			long time = mStats[index+0];
			if (time==0)
			{	write(osw,"time","-");
			}
			else
			{	write(osw,"time",mDateFormat.format(new java.util.Date(time)));
			}
			osw.write("</stat>");
		}
			
		osw.write("</memory>");
		osw.flush();
	}
		
	private static void write(OutputStreamWriter osw, String aName, String aValue) throws IOException
	{	XMLUtils.writeEscaped(osw,aName, aValue);
	}
	
	/** return a filtered estimate of our GC baseline memory usage- i.e how much memory
	 * are we actually holding that cannot be released */
	public final long getBaselineMemory()
	{	return mRealBaseLine;
	}
	
	public final long getMaxMemory()
	{	return mMax<<10;
	}
	
	/** return a filtered estimate of our peak memory usage0-  i.e. how much memory are will
	 * using as a working set
	 */
	public final long getPeakMemory()
	{	return mRealPeak;
	}
}