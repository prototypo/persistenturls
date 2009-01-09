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
 * File:          $RCSfile: ICachelet.java,v $
 * Version:       $Name:  $ $Revision: 1.1 $
 * Last Modified: $Date: 2004/06/18 10:15:23 $
 *****************************************************************************/
package com.ten60.netkernel.cache;

import com.ten60.netkernel.container.Container;
import com.ten60.netkernel.urrequest.*;
import com.ten60.netkernel.urii.*;
import com.ten60.netkernel.module.ModuleDefinition;

import java.io.*;
/**
 * The cachelet interface
 * @author  tab
 */
public interface ICachelet
{
	/** Initialises the cachelet with the container it will be serving
	 * @param the Container the cache will be serving 
	 * @param the Module the cache is deployed into 
	 */
	void init(Container aContainer, ModuleDefinition aModule);
	/** Attempt to get a representation that will satisfy the given request
	 * @param aRequest the request
	 * @return a representation to satisfy the request or null
	 */
	IURRepresentation get(URRequest aRequest);
	/** Gives the cachelet the opportunity to update or invalidate the cache based
	 * on the result that has been issued
	 * @aResult the result
	 */
	void put(URResult aResult);
	/** If this cache has a backing cache return it, otherwise null
	 */
	ICachelet getBackingCache();
	/** Write the state of the cache out to the outputstream in unparsed xml in a cachelet
	 * specific manner.
	 * @aWriter the writer to output to
	 * @exception IOException thrown if we fail to write to the stream
	 */
	void write(Writer aWriter) throws IOException;
}
