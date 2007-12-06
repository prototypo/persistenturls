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
 * File:          $RCSfile: TransportDeploymentRecord.java,v $
 * Version:       $Name:  $ $Revision: 1.1 $
 * Last Modified: $Date: 2004/06/18 10:15:23 $
 *****************************************************************************/
package com.ten60.netkernel.transport;

import com.ten60.netkernel.module.ModuleDefinition;
/**
 * TransportDeploymentRecord keeps track of a transport deployment so that a hot restart
 * knows which transports must to be stopped or started on a hot restart.
 * @author  tab
 */
public class TransportDeploymentRecord
{
	private final ITransport mTransport;
	private final ModuleDefinition mModule;
	private int mIndex;
	private long[] mStats;
	private long mWork;
	
	/** Creates a new instance of TransportDeploymentRecord
	 * @param aTransport
	 * @param aModule
	 * @param aTransportClass
	 */
	public TransportDeploymentRecord(ITransport aTransport, ModuleDefinition aModule, int aStatBufferSize)
	{	mTransport = aTransport;
		mModule = aModule;
		mStats = new long[aStatBufferSize];
	}
	
	/**
	 * @return the Transport reference
	 */	
	public ITransport getTransport()
	{	return mTransport;
	}
	/**
	 * @return the assigned Id of the transport
	 */	
	public String getTransportClass()
	{	return mTransport.getClass().getName();
	}
	/**
	 * @return the module the transport belongs to
	 */	
	public ModuleDefinition getModule()
	{	return mModule;
	}
	
	public void accumulateWork(int aWork, int aIndex)
	{	if (aIndex!=mIndex)
		{	mStats[aIndex]=mWork*2/3;
			mIndex=aIndex;
			mWork=mWork/3;
		}
		mWork+=aWork;
	}
	public long[] getWork()
	{	return mStats;
	}
}